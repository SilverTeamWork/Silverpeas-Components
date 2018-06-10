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

import org.apache.wiki.InternalWikiException;
import org.apache.wiki.WikiEngine;
import org.apache.wiki.WikiPage;
import org.apache.wiki.api.exceptions.NoRequiredPropertyException;
import org.apache.wiki.api.exceptions.ProviderException;
import org.apache.wiki.attachment.Attachment;
import org.apache.wiki.providers.BasicAttachmentProvider;
import org.apache.wiki.providers.WikiAttachmentProvider;
import org.apache.wiki.search.QueryItem;
import org.silverpeas.components.wiki.SilverWikiEngine;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * The provider of attachments of the Wiki pages in JSPWiki. The goal of this provider is to
 * replace any {@link WikiAttachmentProvider} implementation in JSPWiki in order to serve all
 * of the Wiki application instances in Silverpeas. For performing its jobs, it delegates the tasks
 * to a {@link BasicAttachmentProvider} instance that it initializes for the current access wiki
 * instance.
 * @author mmoquillon
 */
public class SilverpeasAttachmentProvider implements WikiAttachmentProvider {

  private SilverWikiEngine engine;

  public File getAttachmentFile(final Attachment attachment) throws ProviderException {
    WikiAttachmentProvider provider = getActualAttachmentProvider();
    File attDir = getAttachmentDir(provider, attachment);
    return getFile(provider, attDir, attachment);
  }

  private static File getAttachmentDir(final WikiAttachmentProvider provider, final Attachment attachment)
      throws ProviderException {
    try {
      Method m = provider.getClass().getDeclaredMethod("findAttachmentDir", Attachment.class);
      m.setAccessible(true);
      return (File) m.invoke(provider, attachment);
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
      throw new ProviderException("No such page was found.");
    }
  }

  private static File getFile(final WikiAttachmentProvider provider, final File dir,
      final Attachment attachment) throws ProviderException {
    try {
      Method m =
          provider.getClass().getDeclaredMethod("findFile", File.class, Attachment.class);
      m.setAccessible(true);
      return (File) m.invoke(provider, dir, attachment);
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
      throw new ProviderException("No such page was found.");
    }
  }

  @Override
  public void initialize(final WikiEngine engine, final Properties properties) {
    this.engine = (SilverWikiEngine) engine;
  }

  @Override
  public String getProviderInfo() {
    return "Silverpeas Wiki Attachment Provider";
  }

  @Override
  public void putAttachmentData(final Attachment att, final InputStream data)
      throws ProviderException, IOException {
    WikiAttachmentProvider provider = getActualAttachmentProvider();
    provider.putAttachmentData(att, data);
  }

  @Override
  public InputStream getAttachmentData(final Attachment att) throws ProviderException, IOException {
    WikiAttachmentProvider provider = getActualAttachmentProvider();
    return provider.getAttachmentData(att);
  }

  @Override
  public Collection listAttachments(final WikiPage page) throws ProviderException {
    WikiAttachmentProvider provider = getActualAttachmentProvider();
    return provider.listAttachments(page);
  }

  @Override
  public Collection findAttachments(final QueryItem[] query) {
    WikiAttachmentProvider provider = getActualAttachmentProvider();
    return provider.findAttachments(query);
  }

  @Override
  public List listAllChanged(final Date timestamp) throws ProviderException {
    WikiAttachmentProvider provider = getActualAttachmentProvider();
    return provider.listAllChanged(timestamp);
  }

  @Override
  public Attachment getAttachmentInfo(final WikiPage page, final String name, final int version)
      throws ProviderException {
    WikiAttachmentProvider provider = getActualAttachmentProvider();
    return provider.getAttachmentInfo(page, name, version);
  }

  @Override
  public List getVersionHistory(final Attachment att) {
    WikiAttachmentProvider provider = getActualAttachmentProvider();
    return provider.getVersionHistory(att);
  }

  @Override
  public void deleteVersion(final Attachment att) throws ProviderException {
    WikiAttachmentProvider provider = getActualAttachmentProvider();
    provider.deleteVersion(att);
  }

  @Override
  public void deleteAttachment(final Attachment att) throws ProviderException {
    WikiAttachmentProvider provider = getActualAttachmentProvider();
    provider.deleteAttachment(att);
  }

  @Override
  public void moveAttachmentsForPage(final String oldParent, final String newParent)
      throws ProviderException {
    WikiAttachmentProvider provider = getActualAttachmentProvider();
    provider.moveAttachmentsForPage(oldParent, newParent);
  }

  private WikiAttachmentProvider getActualAttachmentProvider() {
    try {
      BasicAttachmentProvider provider = new BasicAttachmentProvider();
      provider.initialize(engine, engine.getWikiProperties());
      return provider;
    } catch (NoRequiredPropertyException | IOException e) {
      throw new InternalWikiException(e);
    }
  }
}
  