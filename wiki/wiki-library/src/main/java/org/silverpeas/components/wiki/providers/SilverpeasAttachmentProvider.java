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

package org.silverpeas.components.wiki.providers;

import org.apache.wiki.api.exceptions.ProviderException;
import org.apache.wiki.attachment.Attachment;
import org.apache.wiki.providers.BasicAttachmentProvider;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * The provider of attachments of the Wiki pages in JSPWiki. The goal of this provider is to
 * extend the {@link BasicAttachmentProvider} provider that is used by default in JSPWiki with
 * additional methods required by us.
 * @author mmoquillon
 */
public class SilverpeasAttachmentProvider extends BasicAttachmentProvider {

  public File getAttachmentFile(final Attachment attachment) throws ProviderException {
    File attDir = getAttachmentDir(attachment);
    return getFile(attDir, attachment);
  }

  private File getAttachmentDir(final Attachment attachment) throws ProviderException {
    try {
      Method m =
          BasicAttachmentProvider.class.getDeclaredMethod("findAttachmentDir", Attachment.class);
      m.setAccessible(true);
      return (File) m.invoke(this, attachment);
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
      throw new ProviderException("No such page was found.");
    }
  }

  private File getFile(final File dir, final Attachment attachment) throws ProviderException {
    try {
      Method m =
          BasicAttachmentProvider.class.getDeclaredMethod("findFile", File.class, Attachment.class);
      m.setAccessible(true);
      return (File) m.invoke(this, dir, attachment);
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
      throw new ProviderException("No such page was found.");
    }
  }
}
  