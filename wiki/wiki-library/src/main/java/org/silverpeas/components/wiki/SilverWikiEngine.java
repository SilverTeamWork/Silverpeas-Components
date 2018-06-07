/*
 * Copyright (C) 2000 - 2018 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.wiki;

import org.apache.wiki.InternalWikiException;
import org.apache.wiki.WikiEngine;
import org.apache.wiki.api.exceptions.WikiException;
import org.apache.wiki.attachment.AttachmentManager;
import org.apache.wiki.providers.BasicAttachmentProvider;
import org.apache.wiki.providers.FileSystemProvider;
import org.apache.wiki.util.PropertyReader;
import org.silverpeas.components.wiki.ui.ServletContextWrapper;
import org.silverpeas.components.wiki.ui.WikiTemplateManager;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.cache.service.CacheServiceProvider;

import javax.servlet.ServletContext;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * The Wiki engine in Silverpeas. It wraps the true Wiki engine of the underlying wiki backend
 * (in our case JSPWiki) to be used by a given Wiki application instance. In order to be used by
 * our own services, it must be put first in the request processing cache thread by invoking the
 * {@link SilverWikiEngine#putInCache()} method.
 * <p>
 * The JSPWiki engine is designed to motorize a single wiki application. For doing a single
 * instance of it is mapped to one servlet context so that it is a singleton for the whole web
 * application. As Silverpeas is a web portal within which an application can be
 * deployed several times, this can cause some troubles. Indeed, each application instance has their
 * own location to store the users' contributions in Silverpeas but the JSPWiki engine knows only
 * one location to store all of its resources (Wiki pages, pages'attachments and indexes). One
 * way to resolve this issue is to have for each wiki instance a different servlet context but
 * it's not possible with our own MVC framework in which a servlet acts as a router for an entire
 * application (and then for all of its instances). So, another way is to extend the JSPWiki engine
 * by this class so that is can be multi-instantiated, one per wiki application instance, and then
 * to manage the mapping between the current accessed wiki instance and the servlet context
 * underlying the wiki Silverpeas application. For doing, <strong>it is based upon some
 * details on the implementation of the {@link WikiEngine} class</strong> and it maintains an
 * in-memory cache of all of the {@link SilverWikiEngine} instances.
 * </p>
 * @author mmoquillon
 */
public class SilverWikiEngine extends WikiEngine {

  private static final String ATTR_WIKIENGINE = "org.apache.wiki.WikiEngine";
  private static final Map<String, SilverWikiEngine> engines = new ConcurrentHashMap<>(5);
  private String instanceId;
  private int hashCode = -1;

  /**
   * Gets the {@link SilverWikiEngine} instance that motorizes the specified Wiki application
   * instance. If there is no such {@link SilverWikiEngine} instance, then creates it and maps it
   * to the Wiki instance with the specified {@link ServletContext} representing the web access
   * endpoint of a Wiki application.
   * <p>
   * The {@link SilverWikiEngine} instance isn't put in the cache of the current request processing
   * thread. This must be done explicitly.
   * </p>
   * @param context the {@link ServletContext} of the web endpoint of the Silverpeas Wiki
   * application.
   * @param wikiInstanceId the unique identifier of a Wiki application instance.
   * @return a {@link SilverWikiEngine} instance.
   */
  public static SilverWikiEngine getInstance(final ServletContext context,
      final String wikiInstanceId) {
    Objects.requireNonNull(context);
    Objects.requireNonNull(wikiInstanceId);
    final WikiEngine engine = (WikiEngine) context.getAttribute(ATTR_WIKIENGINE);
    SilverWikiEngine silverEngine = null;
    if (engine instanceof SilverWikiEngine) {
      silverEngine = (SilverWikiEngine) engine;
    }
    if (silverEngine == null || !silverEngine.getWikiInstanceId().equals(wikiInstanceId)) {
      silverEngine = engines.computeIfAbsent(wikiInstanceId, newWikiEngine(context));
      if (engine != null && !(engine instanceof SilverWikiEngine)) {
        // to simulate the JSPWiki engine alongside the SessionMonitor
        silverEngine.setHashCode(engine.hashCode());
      }
    }
    return silverEngine;
  }

  /**
   * Shutdown all the allocated {@link SilverWikiEngine} instances.
   */
  public static void shutdownAll() {
    engines.forEach((id, e) -> e.shutdown());
  }

  /**
   * Shutdown the {@link SilverWikiEngine} instance that was allocated for the specified Wiki
   * application instance.
   * @param wikiInstanceId the unique identifier of a Wiki instance in Silverpeas.
   */
  static void shutdown(final String wikiInstanceId) {
    engines.get(wikiInstanceId).shutdown();
  }

  private void setHashCode(final int hashCode) {
    this.hashCode = hashCode;
  }

  /**
   * Gets the {@link SilverWikiEngine} instance that is set in the cache of the current request
   * processing thread.
   * @return a {@link SilverWikiEngine} instance. If there is no available {@link SilverWikiEngine}
   * instance in the current request processing context, then an {@link InternalWikiException} is
   * thrown.
   */
  public static SilverWikiEngine getFromCache() {
    SilverWikiEngine engine = CacheServiceProvider.getRequestCacheService()
        .getCache()
        .get(ATTR_WIKIENGINE, SilverWikiEngine.class);
    if (engine == null) {
      throw new InternalWikiException("No Wiki Engine for the current wiki application instance");
    }
    return engine;
  }

  private SilverWikiEngine(final ServletContext context, final String appid, final Properties props)
      throws WikiException {
    super(context, appid, props);
    this.instanceId = appid;
    try {
      Field templateManager = WikiEngine.class.getDeclaredField("m_templateManager");
      templateManager.setAccessible(true);
      templateManager.set(this, new WikiTemplateManager(this, props));
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new InternalWikiException(e);
    }
  }

  @Override
  protected void shutdown() {
    engines.remove(this.getWikiInstanceId());
    super.shutdown();
  }

  /**
   * Gets the unique identifier of the wiki application instance that is motorized by this engine.
   * @return the unique identifier of a wiki application instance.
   */
  public String getWikiInstanceId() {
    return this.instanceId;
  }

  /**
   * Puts this {@link SilverWikiEngine} in the cache of the current request processing thread.
   */
  public void putInCache() {
    CacheServiceProvider.getRequestCacheService().getCache().put(ATTR_WIKIENGINE, this);
  }

  /**
   * The base URL of the wiki instance this engine motorizes.
   * @return the base URL of the wiki instance motorized by this Wiki engine.
   */
  @Override
  public String getBaseURL() {
    String contextPath = this.getServletContext().getContextPath();
    return contextPath + "/Rwiki/" + this.getWikiInstanceId();
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SilverWikiEngine)) {
      return false;
    }
    final SilverWikiEngine engine = (SilverWikiEngine) o;
    return hashCode == engine.hashCode && Objects.equals(instanceId, engine.instanceId);
  }

  @Override
  public int hashCode() {
    return hashCode == -1 ? Objects.hashCode(instanceId) : this.hashCode;
  }

  @Override
  public ServletContext getServletContext() {
    return new ServletContextWrapper(super.getServletContext());
  }

  private static Function<String, SilverWikiEngine> newWikiEngine(final ServletContext context) {
    return (String wikiInstanceId) -> {
      final Properties props = PropertyReader.loadWebAppProps(context);
      try {
        WikiSettings wikiSettings = new WikiSettings(wikiInstanceId);
        User user = User.getCurrentRequester();
        props.setProperty(FileSystemProvider.PROP_PAGEDIR, wikiSettings.getWikiPageDirPath());
        props.setProperty(BasicAttachmentProvider.PROP_STORAGEDIR,
            wikiSettings.getWikiAttachmentDirPath());
        props.setProperty(AttachmentManager.PROP_MAXSIZE,
            String.valueOf(wikiSettings.getAttachmentMaxSize()));
        props.setProperty("jspwiki.defaultprefs.template.language",
            user.getUserPreferences().getLanguage());
        return new SilverWikiEngine(context, wikiInstanceId, props);
      } catch (WikiException e) {
        throw new InternalWikiException(e);
      }
    };
  }

  private boolean isAttachmentEndpoint(final String resource) {
    return resource.equals("attach");
  }

  private boolean isSyndicationEndpoint(final String resource) {
    return resource.equals("atom");
  }
}
  