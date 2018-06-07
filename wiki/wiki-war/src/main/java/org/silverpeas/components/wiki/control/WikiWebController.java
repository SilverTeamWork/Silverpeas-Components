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

import org.apache.wiki.WikiSession;
import org.apache.wiki.auth.SessionMonitor;
import org.apache.wiki.auth.WikiSecurityException;
import org.silverpeas.components.wiki.SilverWikiEngine;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.webcomponent.annotation.Homepage;
import org.silverpeas.core.web.mvc.webcomponent.annotation.LowestRoleAccess;
import org.silverpeas.core.web.mvc.webcomponent.annotation.RedirectTo;
import org.silverpeas.core.web.mvc.webcomponent.annotation.RedirectToInternalJsp;
import org.silverpeas.core.web.mvc.webcomponent.annotation.WebComponentController;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * The Web controller in the Silverpeas MVC architecture. It does the glue between the Silverpeas
 * platform and the JSPWiki application. All the incoming HTTP requests against a Wiki application
 * instance are caught first by this controller before passing them to the JSPWiki application.
 * <p>
 * In the Silverpeas MVC architecture, each servlet context is defined as the endpoint of a given
 * Silverpeas application and thereby the servlet instance, named the router, serves the incoming
 * HTTP requests for all of the instances of the application that are deployed in the Silverpeas
 * platform. Because JSPWiki is designed to be a single web application per servlet context, in
 * order to be used into a multi-application instances context, all the incoming HTTP requests are
 * first handled by this web controller in order to prepare the correct execution context expected
 * by the JSPWiki engine as it is running within a single application.
 * </p>
 * @author mmoquillon
 */
@WebComponentController("wiki")
public class WikiWebController
    extends org.silverpeas.core.web.mvc.webcomponent.WebComponentController<WikiWebRequestContext> {

  private static final String TRANSLATIONS_PATH = "org.silverpeas.wiki.multilang.wiki";
  private static final String MAIN_PAGE = "Wiki.jsp";

  /**
   * Constructs a new Web controller for the specified context and with the
   * {@link MainSessionController} instance that is specific to the user behind the access to the
   * underlying application instance.
   * @param controller the main session controller for the current user.
   * @param context the context identifying among others the targeted application instance.
   */
  public WikiWebController(final MainSessionController controller, final ComponentContext context) {
    super(controller, context, TRANSLATIONS_PATH);
  }

  /**
   * On instantiation of this web controller, a contextual wiki engine is set up for the underlying
   * wiki application instance and an automatic authentication of the user is performed with
   * the engine. The Wiki engine is put into the processing context cache before the authentication
   * (required by our own authentication mechanism that makes the glue between Silverpeas and
   * JSPWiki).
   * <p>
   * Normally, the allocation of a {@link SilverWikiEngine} instance for the current accessed Wiki
   * application instance should have been done by the
   * {@link org.silverpeas.components.wiki.jspwiki.filters.SilverWikiServletFilter} single
   * instance as well
   * as the authentication of the current user behind the request.
   * </p>
   * @param context the web request context.
   */
  @Override
  protected void onInstantiation(final WikiWebRequestContext context) {
    final SilverWikiEngine wikiEngine =
        SilverWikiEngine.getInstance(context.getRequest().getServletContext(),
            context.getComponentInstanceId());
    wikiEngine.putInCache();
    WikiSession session =
        SessionMonitor.getInstance(wikiEngine).find(context.getRequest().getSession());
    if (!session.isAuthenticated()) {
      try {
        if (!wikiEngine.getAuthenticationManager().login(context.getRequest())) {
          throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
      } catch (WikiSecurityException e) {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      }
    }
  }

  /**
   * The main page is the main JSPWiki page of the wiki application.
   * @param context the request context.
   */
  @GET
  @Path("Main")
  @Homepage
  @RedirectToInternalJsp(MAIN_PAGE)
  public void home(final WikiWebRequestContext context) {
    // just to redirect to the man JSPWiki page
  }

  /**
   * An attachment of a Wiki page is asked.
   * @param context the request context.
   */
  @GET
  @Path("attach/{page}/{attachment}")
  @RedirectTo("/wiki/attach/{attachmentView}")
  @LowestRoleAccess(SilverpeasRole.writer)
  public void getAttachedFile(final WikiWebRequestContext context) {
    final String page = context.getPathVariables().get("page");
    final String attachement = context.getPathVariables().get("attachment");
    context.addRedirectVariable("attachmentView", page + "/" + attachement);
  }

  /**
   * A JSPWiki view component is asked. This can be a Wiki page, a page editor, or whatever.
   * The method just redirects the request to the JSPWiki view component.
   * @param context the request context.
   */
  @GET
  @Path("{view}")
  @RedirectToInternalJsp("{jspView}")
  public void jspWikiRenderer(final WikiWebRequestContext context) {
    processRequest(context, "view", "jspView");
  }

  /**
   * A file is uploaded to be attached to the Wiki page identified in the query string of the
   * request URL. The method just redirects the request to the
   * {@link org.silverpeas.components.wiki.jspwiki.servlets.SilverAttachmentServlet} servlet.
   * @param context the request context.
   */
  @POST
  @Path("attach")
  @RedirectTo("/wiki/attach")
  @LowestRoleAccess(SilverpeasRole.writer)
  public void attachFile(final WikiWebRequestContext context) {
    // just to redirect to the corresponding wiki attachment servlet
  }


  /**
   * A processing is invoked from a JSPWiki edition component. The processing can be the creation or
   * the update of a wiki page or any stuffs that the JSPWiki takes in charge. The method just
   * redirects the request to the JSPWiki component that will process the asked treatment.
   * @param context the request context.
   */
  @POST
  @Path("{editor}")
  @RedirectToInternalJsp("{jspEditor}")
  @LowestRoleAccess(SilverpeasRole.writer)
  public void jspWikiProcessor(final WikiWebRequestContext context) {
    processRequest(context, "editor", "jspEditor");
  }

  private void processRequest(final WikiWebRequestContext context, final String function,
      final String nextPage) {
    String pageToRender = context.getPathVariables().getOrDefault(function, MAIN_PAGE);
    if (!context.getRequest().getQueryString().isEmpty()) {
      pageToRender += "?" + context.getRequest().getQueryString();
    }
    context.addRedirectVariable(nextPage, pageToRender);
  }
}
  