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
import org.apache.wiki.providers.VersioningFileProvider;
import org.apache.wiki.providers.WikiPageProvider;
import org.apache.wiki.search.QueryItem;
import org.silverpeas.components.wiki.SilverWikiEngine;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * The provider of Wiki pages in JSPWiki. Its goal is to replace both the
 * {@link org.apache.wiki.providers.FileSystemProvider} and the
 * {@link org.apache.wiki.providers.VersioningFileProvider} classes in JSPWiki to serve the
 * Wiki pages for each Wiki application instance in Silverpeas. To accomplish its job, this provider
 * uses an instance of {@link VersioningFileProvider} initialized for the current accessed Wiki
 * instance.
 * @author mmoquillon
 */
public class SilverpeasPageProvider implements WikiPageProvider {

  private SilverWikiEngine engine;

  @Override
  public void putPageText(final WikiPage page, final String text) throws ProviderException {
    WikiPageProvider provider = getActualPageProvider();
    provider.putPageText(page, text);
  }

  @Override
  public boolean pageExists(final String page) {
    WikiPageProvider provider = getActualPageProvider();
    return provider.pageExists(page);
  }

  @Override
  public boolean pageExists(final String page, final int version) {
    WikiPageProvider provider = getActualPageProvider();
    return provider.pageExists(page, version);
  }

  @Override
  public Collection findPages(final QueryItem[] query) {
    WikiPageProvider provider = getActualPageProvider();
    return provider.findPages(query);
  }

  @Override
  public WikiPage getPageInfo(final String page, final int version) throws ProviderException {
    WikiPageProvider provider = getActualPageProvider();
    return provider.getPageInfo(page, version);
  }

  @Override
  public Collection getAllPages() throws ProviderException {
    WikiPageProvider provider = getActualPageProvider();
    return provider.getAllPages();
  }

  @Override
  public Collection getAllChangedSince(final Date date) {
    WikiPageProvider provider = getActualPageProvider();
    return provider.getAllChangedSince(date);
  }

  @Override
  public int getPageCount() throws ProviderException {
    WikiPageProvider provider = getActualPageProvider();
    return provider.getPageCount();
  }

  @Override
  public List getVersionHistory(final String page) throws ProviderException {
    WikiPageProvider provider = getActualPageProvider();
    return provider.getVersionHistory(page);
  }

  @Override
  public String getPageText(final String page, final int version) throws ProviderException {
    WikiPageProvider provider = getActualPageProvider();
    return provider.getPageText(page, version);
  }

  @Override
  public void deleteVersion(final String pageName, final int version) throws ProviderException {
    WikiPageProvider provider = getActualPageProvider();
    provider.deleteVersion(pageName, version);
  }

  @Override
  public void deletePage(final String pageName) throws ProviderException {
    WikiPageProvider provider = getActualPageProvider();
    provider.deletePage(pageName);
  }

  @Override
  public void movePage(final String from, final String to) throws ProviderException {
    WikiPageProvider provider = getActualPageProvider();
    provider.movePage(from, to);
  }

  @Override
  public void initialize(final WikiEngine engine, final Properties properties) {
    this.engine = (SilverWikiEngine) engine;
  }

  @Override
  public String getProviderInfo() {
    return "Silverpeas Wiki Page Provider";
  }

  private WikiPageProvider getActualPageProvider() {
    try {
      final VersioningFileProvider provider = new VersioningFileProvider();
      provider.initialize(engine, engine.getWikiProperties());
      return provider;
    } catch (NoRequiredPropertyException | IOException e) {
      throw new InternalWikiException(e);
    }
  }
}
  