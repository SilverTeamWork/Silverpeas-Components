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

package org.silverpeas.components.wiki.jspwiki.filters;

import org.apache.wiki.InternalWikiException;
import org.apache.wiki.WikiEngine;
import org.apache.wiki.ui.WikiJSPFilter;
import org.silverpeas.components.wiki.SilverWikiEngine;
import org.silverpeas.components.wiki.jspwiki.SilverWikiEngineProvider;

import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.Field;

/**
 * An extension of the {@link org.apache.wiki.ui.WikiJSPFilter} of the JSPWiki distribution.
 * Its aims is to setup the {@link org.silverpeas.components.wiki.SilverWikiEngine} instance for
 * the current accessed wiki instance instead of the {@link org.apache.wiki.WikiEngine} single
 * instance.
 * @author mmoquillon
 */
public class SilverWikiJSPFilter extends WikiJSPFilter {

  @Inject
  private SilverWikiEngineProvider engineProvider;

  @Inject
  private SilverWikiServletFilter filter;

  @Override
  public void init(final FilterConfig config) throws ServletException {
    // does nothing here: we don't setup the WikiEngine
  }

  /**
   * Retrieves the {@link SilverWikiEngine} instance for the current accessed Wiki application
   * instance. If no such {@link SilverWikiEngine} instance exists, then creates it, initializes
   * it and attaches it to the servlet context to be retrieved later by the underlying JSPWiki
   * backend. The {@link SilverWikiEngine} instance is put in the current processing thread to be
   * get by our own business code. Then passes the hand to the JSPWiki {@link WikiJSPFilter}
   * filter.
   * @param request the incoming HTTP request.
   * @param response the output HTTP response.
   * @param chain the chain of processing.
   * @throws IOException if an IO error occurs with the web client.
   * @throws ServletException if an error occurs during the processing of both the request and the
   * response.
   * @see WikiJSPFilter#doFilter(ServletRequest, ServletResponse, FilterChain)
   */
  @Override
  public void doFilter(final ServletRequest request, final ServletResponse response,
      final FilterChain chain) throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    if (httpRequest.getRequestURI().endsWith(".jsp")) {
      this.m_engine = engineProvider.getSilverWikiEngine(request);
      setUpInheritedFields(this.m_engine);
      super.doFilter(request, response, chain);
    } else {
      filter.doFilter(request, response, chain);
    }
  }

  private void setUpInheritedFields(final WikiEngine wikiEngine) {
    try {
      final Field wikiEncoding = WikiJSPFilter.class.getDeclaredField("m_wiki_encoding");
      wikiEncoding.setAccessible(true);
      wikiEncoding.set(this, wikiEngine.getWikiProperties().getProperty(WikiEngine.PROP_ENCODING));

      final Field useEncoding = WikiJSPFilter.class.getDeclaredField("useEncoding");
      useEncoding.setAccessible(true);
      useEncoding.set(this, !(Boolean.valueOf(wikiEngine.getWikiProperties()
          .getProperty(WikiEngine.PROP_NO_FILTER_ENCODING, "false")
          .trim())));
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new InternalWikiException(e);
    }
  }
}
  