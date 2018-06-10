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
import org.apache.wiki.providers.BasicAttachmentProvider;
import org.apache.wiki.providers.FileSystemProvider;
import org.apache.wiki.util.PropertyReader;
import org.silverpeas.components.wiki.ui.ServletContextWrapper;
import org.silverpeas.components.wiki.ui.WikiTemplateManager;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.file.FileRepositoryManager;

import javax.servlet.ServletContext;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

/**
 * The Wiki engine in Silverpeas. It wraps the true Wiki engine of the underlying wiki backend
 * (in our case JSPWiki) in order to communicate with the {@link WikiInstanceManager} single
 * instance to known what is the Wiki application instance to currently serve.
 * <p>
 * The JSPWiki engine is designed to motorize a single wiki application. For doing a single
 * instance of it is set to the servlet context of the web application. As Silverpeas is a web
 * portal within which an application can be deployed several times, this can cause some troubles.
 * Indeed, each application instance has their own location to store the users' contributions in
 * Silverpeas but the JSPWiki engine knows only one location to store all of its resources
 * (Wiki pages, pages'attachments, indexes, history, ...). To resolve this issue, the JSPWiki engine
 * is extended by the {@link SilverWikiEngine} class and it maintains a relationship with the
 * {@link WikiInstanceManager} single instance to known at any time the Wiki instance application
 * being currently accessed so that it can then set the Wiki properties (such as the location of
 * the Wiki's resources) accordingly to that Wiki instance.
 * </p>
 * @author mmoquillon
 */
public class SilverWikiEngine extends WikiEngine {

  private static final String ATTR_WIKIENGINE = "org.apache.wiki.WikiEngine";

  protected SilverWikiEngine(final ServletContext context, final Properties props)
      throws WikiException {
    super(context, "SILVERPEAS WIKI", props);
    try {
      Field templateManager = WikiEngine.class.getDeclaredField("m_templateManager");
      templateManager.setAccessible(true);
      templateManager.set(this, new WikiTemplateManager(this, props));
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new InternalWikiException(e);
    }
  }

  public static SilverWikiEngine getInstance(final ServletContext context) {
    Objects.requireNonNull(context);
    SilverWikiEngine engine = (SilverWikiEngine) context.getAttribute(ATTR_WIKIENGINE);
    if (engine == null) {
      final String wikiTemp = FileRepositoryManager.getTemporaryPath() + "jspwiki";
      final Properties props = PropertyReader.loadWebAppProps(context);
      props.setProperty(FileSystemProvider.PROP_PAGEDIR, wikiTemp);
      props.setProperty(BasicAttachmentProvider.PROP_STORAGEDIR, wikiTemp);
      props.setProperty(WikiEngine.PROP_WORKDIR, wikiTemp);
      try {
        engine = new SilverWikiEngine(context, props);
      } catch (WikiException e) {
        throw new InternalWikiException(e);
      }
    }
    return engine;
  }

  @Override
  protected void shutdown() {
    super.shutdown();
  }

  /**
   * Gets the manager of the different Wiki application instances in Silverpeas.
   * @return the {@link WikiInstanceManager} single instance.
   */
  private WikiInstanceManager getWikiInstanceManager() {
    return ServiceProvider.getService(WikiInstanceManager.class);
  }

  /**
   * Gets the unique identifier of the wiki application instance that is motorized by this engine.
   * @return the unique identifier of a wiki application instance.
   */
  public String getWikiInstanceId() {
    final WikiInstance wiki = getWikiInstanceManager().getCurrentWikiInstance()
        .orElseThrow(() -> new InternalWikiException(
            "The current process should be performed under the context of a given Wiki instance"));
    return wiki.getWikiId();
  }

  @Override
  public Properties getWikiProperties() {
    Optional<WikiInstance> wiki = getWikiInstanceManager().getCurrentWikiInstance();
    final Properties props;
    if (wiki.isPresent()) {
      props = wiki.get().getProperties();
    } else {
      props = super.getWikiProperties();
    }
    return props;
  }

  /**
   * The base URL of the wiki instance this engine motorizes.
   * @return an URL relative to the servlet context path.
   */
  @Override
  public String getBaseURL() {
    Optional<WikiInstance> wiki = getWikiInstanceManager().getCurrentWikiInstance();
    final String url;
    if (wiki.isPresent()) {
      url = wiki.get().getBaseUrl();
    } else {
      url = getServletContext().getContextPath();
    }
    return url;
  }

  /**
   * The URL of the location of the web pages that made up a Wiki instance.
   * @return an URL relative to the servlet context path.
   */
  public String getWebPagesURL() {
    return getWikiInstanceManager().getWikiWebPath(getServletContext());
  }

  @Override
  public String getWorkDir() {
    Optional<WikiInstance> wiki = getWikiInstanceManager().getCurrentWikiInstance();
    final String workDir;
    if (wiki.isPresent()) {
      workDir = wiki.get().getWorkDir();
    } else {
      workDir = super.getWorkDir();
    }
    return workDir;
  }

  @Override
  public String getTemplateDir() {
    Optional<WikiInstance> wiki = getWikiInstanceManager().getCurrentWikiInstance();
    final String templateDir;
    if (wiki.isPresent()) {
      templateDir = wiki.get().getTemplateDir();
    } else {
      templateDir = super.getTemplateDir();
    }
    return templateDir;
  }

  @Override
  public ServletContext getServletContext() {
    return new ServletContextWrapper(super.getServletContext());
  }

  /**
   * Gets the URL's absolute path of the specified JSPWiki's template.
   * @param template the relative path of a template.
   * @return the path of the specified template relative to the servlet context.
   */
  public String getTemplatePath(final String template) {
    return getWikiInstanceManager().getTemplatePath(template);
  }

  /**
   * Indicates that the current processing thread is to serve the specified wiki instance.
   * @param wikiInstanceId the unique identifier of a wiki application instance.
   */
  public void setCurrentWikiInstance(final String wikiInstanceId) {
    getWikiInstanceManager().setCurrentWikiInstance(this, wikiInstanceId);
  }
}
  