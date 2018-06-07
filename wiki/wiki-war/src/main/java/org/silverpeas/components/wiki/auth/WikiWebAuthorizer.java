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

package org.silverpeas.components.wiki.auth;

import org.apache.wiki.WikiEngine;
import org.apache.wiki.WikiSession;
import org.apache.wiki.auth.authorize.Role;
import org.apache.wiki.auth.authorize.WebAuthorizer;
import org.silverpeas.components.wiki.SilverWikiEngine;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.web.http.SilverpeasPrincipal;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Collection;
import java.util.Properties;
import java.util.stream.Stream;

/**
 * The {@link WebAuthorizer} the JSPWiki engine should use to perform authorization control over
 * the access of the authenticated Silverpeas user. It does the glue between the authorization
 * mechanism of Silverpeas and the one of the JSPWiki.
 * <p>
 * The {@link WebAuthorizer} is used to check the user behind a session or an HTTP request plays
 * one or several of the roles it supports. In that case, the
 * {@link org.apache.wiki.auth.AuthenticationManager} will add all of them among the roles the user
 * plays in its Wiki session for further authorization that will be done by the
 * {@link org.apache.wiki.auth.AuthorizationManager} against the invoked Wiki operations.
 * </p>
 * @author mmoquillon
 */
public class WikiWebAuthorizer implements WebAuthorizer {

  private static final Principal[] SUPPORTED_ROLES =
      Stream.of(WikiRole.toWikiRoles(), new Role[]{Role.ALL})
          .flatMap(Stream::of)
          .toArray(Principal[]::new);

  @Override
  public boolean isUserInRole(final HttpServletRequest request, final Principal role) {
    User user = User.getCurrentRequester();
    return isUserInRole(user, role);
  }

  @Override
  public Principal[] getRoles() {
    return SUPPORTED_ROLES;
  }

  @Override
  public Principal findRole(final String role) {
    return Stream.of(SUPPORTED_ROLES)
        .filter(r -> r.getName().equals(role))
        .findFirst()
        .orElse(null);
  }

  @Override
  public void initialize(final WikiEngine engine, final Properties props) {
    // nothing to initialize
  }

  @Override
  public boolean isUserInRole(final WikiSession session, final Principal role) {
    Principal[] principals = session.getPrincipals();
    boolean isInRole = false;
    for (int i = 0; i < principals.length && !isInRole; i++) {
      if (principals[i] instanceof SilverpeasPrincipal) {
        User user = ((SilverpeasPrincipal) principals[i]).getUser();
        isInRole = isUserInRole(user, role);
      } else {
        isInRole = principals[i] instanceof Role && principals[i].equals(role);
      }
    }
    return isInRole;
  }

  private boolean isUserInRole(final User user, final Principal role) {
    // if the authorizer is invoked, it means the user is authenticated. The two special roles
    // AUTHENTICATED and ALL are then always true
    boolean isInRole = role == Role.AUTHENTICATED || role == Role.ALL;
    if (!isInRole) {
      // we check the roles the user plays in the wiki application instance
      final String wikiId = SilverWikiEngine.getFromCache().getWikiInstanceId();
      Collection<SilverpeasRole> roles =
          OrganizationController.get().getUserSilverpeasRolesOn(user, wikiId);
      isInRole = roles.stream()
          .map(r -> WikiRole.fromSilverpeasRole(r).getWikiRole())
          .anyMatch(r -> r.getName().equals(role.getName()));
    }
    return isInRole;
  }
}
  