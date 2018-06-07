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

package org.silverpeas.components.wiki.control;

import org.apache.wiki.url.DefaultURLConstructor;
import org.silverpeas.components.wiki.SilverWikiEngine;
import org.silverpeas.core.util.StringUtil;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * URL constructor that will be used by the JSPWiki engine. Its gaols is to extend the
 * {@link DefaultURLConstructor} that it replaces in order to customize the URL computation by
 * taking into account the peculiar characteristics of Silverpeas (and its MVC framework). For
 * example, in the case of the Wiki application, any request to an instance of such an application,
 * the URL are constructed by default from a base URL <code>/Rwiki/WIKI_INSTANCE_ID/</code> with
 * <code>WIKI_INSTANCE_ID</code> the unique identifier of the accessed wiki instance. This is
 * done unless the resource for which an URL has to be constructed is a basic web resource (CSS,
 * Javascript, ...) or is the path of an existing Wiki page's attachment.
 * @author mmoquillon
 */
public class SilverpeasURLConstructor extends DefaultURLConstructor {

  @Override
  public String makeURL(final String context, final String name, final boolean absolute,
      final String parameters) {
    initWithCurrentWikiEngine(context, name);
    return super.makeURL(context, name, absolute, parameters);
  }

  @Override
  public String parsePage(final String context, final HttpServletRequest request,
      final String encoding) throws UnsupportedEncodingException {
    initWithCurrentWikiEngine(context, request.getRequestURI());
    String page = super.parsePage(context, request, encoding);
    if (StringUtil.isDefined(page)) {
      // in the case of a path to an attachment, we have to decode it to be retrieved from the
      // filesystem.
      page = URLDecoder.decode(page, encoding);
    }
    return page;
  }

  @Override
  public String getForwardPage(final HttpServletRequest request) {
    return "Rwiki" + request.getPathInfo();
  }

  /**
   * Initializes the current URL constructor with the specified Wiki invocation context and with
   * the specified resource for which an URL will be constructed.
   * <p>
   * A Wiki engine must be had put into the cache, meaning the URL constructor is invoked within
   * the context of a wiki instance. With the wiki engine, we can then compute the base URL for all
   * of the further URL construction from the context of the current accessed wiki instance and
   * from the specified resource for which the URL will be made.
   * </p>
   * @param context the name of the Wiki context under which the URL constructor is initialized.
   * @param resourceName the name of the resource for which an URL will be constructed. The resource
   * can be a path, a JSP, an URL endpoint identifying a Wiki service, ...
   */
  private void initWithCurrentWikiEngine(final String context, final String resourceName) {
    this.m_engine = SilverWikiEngine.getFromCache();
    this.m_pathPrefix = this.m_engine.getServletContext().getContextPath();
    if (!isAbsolutePath(resourceName)) {
      if (isJSP(resourceName) || isAttachmentEndpoint(resourceName) ||
          isSyndicationEndpoint(resourceName) || !context.isEmpty()) {
        this.m_pathPrefix = this.m_engine.getBaseURL();
      } else {
        this.m_pathPrefix += "/wiki/jsp";
      }
    }
    this.m_pathPrefix += "/";
  }

  private boolean isAbsolutePath(final String resource) {
    return resource.startsWith(this.m_engine.getServletContext().getContextPath());
  }

  private boolean isAttachmentEndpoint(final String resource) {
    return resource.equals("attach");
  }

  private boolean isSyndicationEndpoint(final String resource) {
    return resource.equals("atom");
  }

  private boolean isJSP(final String resource) {
    return resource.endsWith(".jsp");
  }

}
  