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

import org.apache.wiki.auth.WikiPrincipal;
import org.apache.wiki.auth.authorize.Role;
import org.silverpeas.core.admin.user.model.User;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Custom Silverpeas JAAS Login Module for JSPWiki. It does the glue between Silverpeas and the
 * JSPWiki engine.
 * <p>
 * JSPWiki defines a set of hard-coded roles from which two are mandatory in the JSPWiki engine:
 * </p>
 * <ul>
 *   <li>{@link Role#ALL} that defines all the privileges a user has by default,</li>
 *   <li>{@link Role#AUTHENTICATED} that is actually a marker for users authenticated in
 *   the JSPWiki engine and by the same time it defines all the privileges such users have.</li>
 * </ul>
 * <p>
 * So, when an authenticated user in Silverpeas accesses the wiki, the login succeeds transparently
 * in the JSPWiki engine and the user receives automatically the {@link Role#AUTHENTICATED} role.
 * </p>
 * @author mmoquillon
 */
public class WikiLoginModule implements LoginModule {

  private Subject subject;
  private final Set<Principal> principals = new HashSet<>();

  @Override
  public void initialize(final Subject subject, final CallbackHandler callbackHandler,
      final Map<String, ?> sharedState, final Map<String, ?> options) {
    Objects.requireNonNull(subject, "The subject of an authentication cannot be null");
    this.subject = subject;
    principals.clear();
  }

  @Override
  public boolean login() throws LoginException {
    final User user = User.getCurrentRequester();
    if (user == null) {
      throw new LoginException("No Silverpeas user behind the access to the wiki!");
    }
    principals.add(new WikiPrincipal(user.getLogin(), WikiPrincipal.LOGIN_NAME));
    principals.add(new WikiPrincipal(user.getDisplayedName(), WikiPrincipal.FULL_NAME));
    principals.add(new WikiPrincipal(user.getDisplayedName(), WikiPrincipal.WIKI_NAME));
    principals.add(Role.AUTHENTICATED);
    return true;
  }

  @Override
  public boolean commit() {
    boolean succeeded = true;
    if (this.principals.isEmpty()) {
      succeeded = false;
      removeSubjectPrincipals(Role.AUTHENTICATED);
    } else {
      Set<WikiPrincipal> anyPreviousPrincipals = this.subject.getPrincipals(WikiPrincipal.class);
      removeSubjectPrincipals(anyPreviousPrincipals);
      removeSubjectPrincipals(WikiPrincipal.GUEST, Role.ANONYMOUS, Role.ASSERTED);
      this.subject.getPrincipals().addAll(this.principals);
    }
    return succeeded;
  }

  @Override
  public boolean abort() {
    removeSubjectPrincipals(this.principals);
    return true;
  }

  @Override
  public boolean logout() {
    removeSubjectPrincipals(this.principals);
    this.principals.clear();
    return true;
  }

  private void removeSubjectPrincipals(final Principal... principals) {
    removeSubjectPrincipals(Arrays.asList(principals));
  }

  private void removeSubjectPrincipals(final Collection<? extends Principal> principals) {
    this.subject.getPrincipals().removeAll(principals);
  }

}
  