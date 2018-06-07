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

package org.silverpeas.components.wiki.ui;

import org.apache.wiki.WikiEngine;
import org.apache.wiki.ui.TemplateManager;
import org.silverpeas.components.wiki.WikiSettings;

import javax.servlet.ServletContext;
import javax.servlet.jsp.PageContext;
import java.util.Objects;
import java.util.Properties;

/**
 * Extension of the JSPWiki's {@link TemplateManager} dedicated to replace it. The problem with the
 * actual {@link TemplateManager} class is that the search of a template is done relative to the
 * {@link ServletContext} path and this is hardcoded! In the integration of JSPWiki in Silverpeas,
 * all the JSP files, and among them the templates, are relative to the <code>wiki/jsp/</code>
 * directory. The {@link WikiTemplateManager} takes into account this fact.
 * @author mmoquillon
 */
public class WikiTemplateManager extends TemplateManager {

  /**
   * Creates a new TemplateManager.  There is typically one manager per engine.
   * @param engine The owning engine.
   * @param properties The property list used to initialize this.
   */
  public WikiTemplateManager(final WikiEngine engine, final Properties properties) {
    super(engine, properties);
  }

  @Override
  public String findJSP(final PageContext pageContext, final String name) {
    final String jsp = super.findJSP(new PageContextWrapper(pageContext), name);
    return fixTemplatePath(jsp);
  }

  @Override
  public String findJSP(final PageContext pageContext, final String template, final String name) {
    Objects.requireNonNull(template);
    Objects.requireNonNull(name);
    final String jsp = super.findJSP(new PageContextWrapper(pageContext), template, name);
    return fixTemplatePath(jsp);
  }

  private String fixTemplatePath(final String template) {
    String path = template;
    if (path.startsWith("/templates")) {
      path = WikiSettings.WIKI_BASE_DIR + path;
    }
    return path;
  }
}
  