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

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Bootstraps at Silverpeas startup a {@link SilverWikiEngine} instance in place of the
 * {@link org.apache.wiki.WikiEngine} one in order to serve all the Wiki application instances
 * deployed in the Silverpeas platform.
 * It shutdowns also the {@link SilverWikiEngine} instance at the stop of Silverpeas.
 * @author mmoquillon
 */
public class WikiEngineBootstrap implements ServletContextListener {

  private SilverWikiEngine wikiEngine;

  /**
   * Creates and initializes a {@link SilverWikiEngine} instance.
   * @param sce the event about the initialization of the servlet context.
   */
  @Override
  public void contextInitialized(final ServletContextEvent sce) {
    wikiEngine = SilverWikiEngine.getInstance(sce.getServletContext());
  }

  /**
   * Shutdown the {@link SilverWikiEngine} instance.
   * @param sce the event about the end of the servlet context.
   */
  @Override
  public void contextDestroyed(final ServletContextEvent sce) {
    wikiEngine.shutdown();
  }
}
  