/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.kmelia;

import org.silverpeas.core.personalization.UserPreferences;
import org.silverpeas.core.personalization.service.PersonalizationServiceProvider;
import org.silverpeas.core.util.URLUtil;
import de.nava.informa.core.ChannelIF;
import de.nava.informa.core.ItemIF;
import de.nava.informa.exporters.RSS_2_0_Exporter;
import de.nava.informa.impl.basic.Channel;
import de.nava.informa.impl.basic.Item;
import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.service.AdminController;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.controller.SilverpeasWebUtil;
import org.silverpeas.core.util.MimeTypes;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

public class RssLastPublicationsServlet extends HttpServlet {

  private static final long serialVersionUID = 5196503014070113044L;
  public static final String SPACE_ID_PARAM = "spaceId";
  public static final String USER_ID_PARAM = "userId";
  public static final String PASSWORD_PARAM = "password";
  public static final String LOGIN_PARAM = "login";

  private static final SettingBundle settings = ResourceLocator.getSettingBundle(
      "org.silverpeas.kmelia.settings.kmeliaSettings");

  @Inject
  private SilverpeasWebUtil util;

  @Inject
  private AdminController adminController;

  @Inject
  private OrganizationController organizationController;

  @Override
  public void service(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String spaceId = request.getParameter(SPACE_ID_PARAM);
    String userId = request.getParameter(USER_ID_PARAM);
    String login = request.getParameter(LOGIN_PARAM);
    String password = request.getParameter(PASSWORD_PARAM);
    try {
      UserFull user = adminController.getUserFull(userId);
      if (isUserAuthorized(user, login, password, spaceId)) {

        String serverURL = getServerURL(user);
        ChannelIF channel = new Channel();
        MainSessionController mainSessionController = util.getMainSessionController(request);
        KmeliaTransversal kmeliaTransversal;
        String preferredLanguage;
        if (mainSessionController != null) {
          kmeliaTransversal = new KmeliaTransversal(mainSessionController);
          preferredLanguage = mainSessionController.getFavoriteLanguage();
        } else {
          kmeliaTransversal = new KmeliaTransversal(userId);
          preferredLanguage = getPersonalization(userId).getLanguage();
        }

        // récupération de la liste des N éléments à remonter dans le flux
        Collection<PublicationDetail> publications = getElements(kmeliaTransversal, spaceId);

        // création d'une liste de ItemIF en fonction de la liste des éléments
        for (PublicationDetail publication : publications) {
          channel.addItem(toRssItem(publication, serverURL, preferredLanguage));
        }

        // construction de l'objet Channel
        channel.setTitle(getChannelTitle(spaceId));
        // exportation du channel
        response.setContentType(MimeTypes.RSS_MIME_TYPE);
        response.setHeader("Content-Disposition", "inline; filename=feeds.rss");
        Writer writer = response.getWriter();
        RSS_2_0_Exporter rssExporter = new RSS_2_0_Exporter(writer, "UTF-8");
        rssExporter.write(channel);
      } else {
        objectNotFound(request, response);
      }
    } catch (Exception e) {
      SilverTrace.error("kmelia", "RssLastPublicationsServlet.doPost()", "root.MSG_GEN_PARAM_VALUE",
          e);
      objectNotFound(request, response);
    }
  }

  public Collection<PublicationDetail> getElements(KmeliaTransversal kmeliaTransversal,
      String spaceId) {
    int maxAge = settings.getInteger("max.age.last.publication", 0);
    int nbReturned = settings.getInteger("max.nb.last.publication", 10);
    return kmeliaTransversal.getUpdatedPublications(spaceId, maxAge, nbReturned);
  }

  public ItemIF toRssItem(PublicationDetail publication, String serverURL, String lang) throws
      MalformedURLException {
    ItemIF item = new Item();
    item.setTitle(publication.getTitle());
    StringBuilder url = new StringBuilder(256);
    url.append(serverURL);
    url.append(URLUtil.getSimpleURL(URLUtil.URL_PUBLI, publication.getPK().getId()));
    item.setLink(new URL(url.toString()));
    item.setDescription(publication.getDescription(lang));
    item.setDate(publication.getUpdateDate());
    String creatorId = publication.getUpdaterId();
    if (StringUtil.isDefined(creatorId)) {
      UserDetail creator = adminController.getUserDetail(creatorId);
      if (creator != null) {
        item.setCreator(creator.getDisplayedName());
      }
    }
    return item;
  }

  public String getChannelTitle(String spaceId) {
    SpaceInstLight space = organizationController.getSpaceInstLightById(spaceId);
    if (space != null) {
      return space.getName();
    }
    return "";
  }

  public String getServerURL(UserFull user) {
    Domain defaultDomain = adminController.getDomain(user.getDomainId());
    return defaultDomain.getSilverpeasServerURL();
  }

  public boolean isUserAuthorized(UserFull user, String login, String password, String spaceId) {
    return ((user != null) && login.equals(user.getLogin()) && password.equals(user.getPassword())
        && isSpaceAvailable(user.getId(), spaceId));
  }

  public boolean isSpaceAvailable(String userId, String spaceId) {
    return adminController.isSpaceAvailable(userId, spaceId);
  }

  protected void objectNotFound(HttpServletRequest req, HttpServletResponse res)
      throws IOException {
    boolean isLoggedIn = util.getMainSessionController(req) != null;
    if (!isLoggedIn) {
      res.sendRedirect(URLUtil.getApplicationURL() + "/admin/jsp/documentNotFound.jsp");
      return;
    }
    res.sendRedirect("/weblib/notFound.html");
  }

  /**
   * Return the personalization service layer
   * @param userId the user identifier
   * @return the UserPreferences of user identified by userId
   */
  public UserPreferences getPersonalization(String userId) {
    return PersonalizationServiceProvider.getPersonalizationService().getUserSettings(userId);
  }
}