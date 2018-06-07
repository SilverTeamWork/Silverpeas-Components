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

import org.apache.wiki.auth.authorize.Role;
import org.silverpeas.core.admin.user.model.SilverpeasRole;

import java.util.stream.Stream;

/**
 * A role played by a Silverpeas user in the JSPWiki engine. The enumeration of the roles
 * does the glue between a role in Silverpeas and a role in the engine. These roles are added
 * into the JSPWiki's authorization policy; that policy is declared within the
 * <code>jspwiki.policy</code> file in the <code>WEB-INF</code> directory of the Web archive.
 * <p>
 * The JSPWiki engine defines by default a set of predefined roles:
 * </p>
 * <ul>
 * <li>{@link Role#ALL}: it defines all the privileges a user can play by default in the engine,
 * whatever its other roles. It is the common base of all others roles in the engine.</li>
 * <li>{@link Role#ANONYMOUS}: it defines an anonymous access to the wiki. This role is removed
 * from the JSPWiki's authorization policy.
 * </li>
 * <li>
 * {@link Role#ASSERTED}: it defines all the privileges a user authenticated by a cookie have.
 * This role has no meaning in Silverpeas and then it is removed from the JSPWiki's authorization
 * policy.
 * </li>
 * <li>{@link Role#AUTHENTICATED}: it is a marker for all users that were successfully
 * authenticated in the JSPWiki engine. It defines also all the privileges such users have in the
 * engine. In the Silverpeas platform, all the authenticated users will play this role but the
 * privileges they will have will be provided by the different roles defined here.</li>
 * </ul>
 * @author mmoquillon
 */
public enum WikiRole {
  /**
   * The Silverpeas user plays no specific role in the current Wiki instance.
   */
  NONE(Role.AUTHENTICATED, null),

  /**
   * The Silverpeas user plays the role of a reader in the current wiki instance.
   */
  READER(new Role("Reader"), SilverpeasRole.user),

  /**
   * The Silverpeas user plays the  role of a contributor in the current wiki instance.
   */
  CONTRIBUTOR(new Role("Contributor"), SilverpeasRole.writer),

  /**
   * The Silverpeas user plays the role of an administrator in the current wiki instance. This
   * role is already predefined in the JSPWiki engine.
   */
  ADMIN(new Role("Admin"), SilverpeasRole.admin);

  private final Role wikiRole;
  private final SilverpeasRole silverpeasRole;

  WikiRole(final Role wikiRole, final SilverpeasRole silverpeasRole) {
    this.wikiRole = wikiRole;
    this.silverpeasRole = silverpeasRole;
  }

  /**
   * Gets all the Wiki roles that are defined for the different supported Silverpeas roles.
   * @return an array of the underlying Wiki engine's roles.
   */
  public static Role[] toWikiRoles() {
    return Stream.of(WikiRole.values()).map(WikiRole::getWikiRole).toArray(Role[]::new);
  }

  /**
   * Converts the specified JSPWiki engine's role to a defined wiki role. The wiki role is the glue
   * between the wiki engine and a Silverpeas role.
   * @return the matching {@link WikiRole} instance or {@link WikiRole#NONE} if the specified role
   * doesn't match any predefined {@link WikiRole} instance.
   */
  public static final WikiRole fromJSPWikiRole(final Role role) {
    try {
      return WikiRole.valueOf(role.getName().toUpperCase());
    } catch(IllegalArgumentException e) {
      return NONE;
    }
  }

  /**
   * Converts the specified Silverpeas role to a defined Wiki role that will be used in the Wiki
   * engine.
   * @param role a Silverpeas role.
   * @return the matching role used by Silverpeas in the wiki. If there is no matching between
   * the specified Silverpeas role and a Wiki role, then {@link WikiRole#NONE} is returned.
   */
  public static WikiRole fromSilverpeasRole(final SilverpeasRole role) {
    return Stream.of(WikiRole.values()).filter(r -> r.getSilverpeasRole() == role)
          .findFirst()
          .orElse(NONE);
  }

  /**
   * Gets the representation of this role into a role in the Wiki engine.
   * @return a {@link Role} instance. Such a role must be defined in the Wiki backend.
   */
  public Role getWikiRole() {
    return this.wikiRole;
  }

  public SilverpeasRole getSilverpeasRole() {
    return this.silverpeasRole;
  }

  @Override
  public String toString() {
    return this.name().charAt(0) + this.name().substring(1).toLowerCase();
  }
}
