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

package org.silverpeas.components.wiki.jspwiki;

import org.silverpeas.components.wiki.SilverWikiEngine;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.mvc.controller.SilverpeasWebUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;

/**
 * Provider of a {@link SilverWikiEngine} instance for the requested Wiki application instance.
 * @author mmoquillon
 */
@Singleton
public class CurrentWikiInstanceSetter {

  private static final Pattern WIKI_ID_PATTERN = Pattern.compile("^wiki\\d+$");

  @Inject
  private SilverpeasWebUtil webUtil;

  /**
   * Gets the {@link SilverWikiEngine} instance of the wiki instance targeted by the specified
   * HTTP request. If no {@link SilverWikiEngine} instance is allocated for the targeted wiki
   * instance then creates it and initializes it. If not yet, it is cached for further use in
   * the same processing thread.
   * @param request the incoming HTTP request.
   * @return the {@link SilverWikiEngine} instance that motorizes the targeted wiki instance.
   */
  public void setCurrentAccessedWiki(final ServletRequest request) {
    final HttpServletRequest httpRequest = (HttpServletRequest) request;
    final String wikiId = webUtil.getComponentId(httpRequest)[1];
    final SilverWikiEngine wikiEngine = SilverWikiEngine.getInstance(request.getServletContext());
    if (StringUtil.isDefined(wikiId) && WIKI_ID_PATTERN.matcher(wikiId).matches()) {
      wikiEngine.setCurrentWikiInstance(wikiId);
    }
  }
}
  