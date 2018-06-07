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

package org.silverpeas.components.wiki.jspwiki.servlets;

import org.apache.wiki.attachment.AttachmentServlet;
import org.silverpeas.components.wiki.SilverWikiEngine;
import org.silverpeas.components.wiki.jspwiki.SilverWikiEngineProvider;

import javax.inject.Inject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet that handles the requests for the attachments. It extends the JSPWiki
 * {@link AttachmentServlet} class in order to set up the servlet with the Wiki engine that
 * motorizes the current accessed Wiki application instance.
 * <p>
 * In JSPWiki, there is one Wiki engine per wiki instance and he wiki engine is mapped
 * to the servlet context meaning there is a direct one-to-one relationship between a servlet and
 * a wiki instance. All of the JSPWikis's components are designed with this characteristic in mind.
 * Unfortunately in Silverpeas a servlet is mapped to one single application and it listens
 * for incoming requests for all of the instances of that application; in Silverpeas an application
 * can be instantiated several times. So, to adapt the default behavior of JSPWiki, its use of
 * {@link org.apache.wiki.WikiEngine} is replaced by our own {@link SilverWikiEngine} class. The
 * latter extends the behavior of the {@link org.apache.wiki.WikiEngine} class by adding the
 * feature to be instantiated several times for one single servlet context, each of them for a
 * different wiki instance. In such a design, several components of JSPWiki requires then to
 * be extended by our own so that they are initialized with our own wiki instance before performing
 * their task. This is the case of the {@link AttachmentServlet} servlet.
 * </p>
 * @author mmoquillon
 */
public class SilverAttachmentServlet extends AttachmentServlet {

  @Inject
  private SilverWikiEngineProvider engineProvider;

  @Override
  public void init(final ServletConfig config) {
    // does nothing
  }

  @Override
  public void doGet(final HttpServletRequest req, final HttpServletResponse res)
      throws IOException, ServletException {
    SilverWikiEngine engine = engineProvider.getSilverWikiEngine(req);
    super.init(new SilverServletConfig(engine.getServletContext()));
    super.doGet(req, res);
  }

  @Override
  public void doPost(final HttpServletRequest req, final HttpServletResponse res)
      throws IOException, ServletException {
    SilverWikiEngine engine = engineProvider.getSilverWikiEngine(req);
    super.init(new SilverServletConfig(engine.getServletContext()));
    super.doPost(req, res);
  }


}
  