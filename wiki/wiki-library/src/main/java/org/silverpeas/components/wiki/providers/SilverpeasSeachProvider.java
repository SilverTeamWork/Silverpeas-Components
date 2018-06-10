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
import org.apache.wiki.WikiContext;
import org.apache.wiki.WikiEngine;
import org.apache.wiki.WikiPage;
import org.apache.wiki.api.exceptions.ProviderException;
import org.apache.wiki.attachment.Attachment;
import org.apache.wiki.auth.AuthorizationManager;
import org.apache.wiki.auth.permissions.PagePermission;
import org.apache.wiki.providers.WikiAttachmentProvider;
import org.apache.wiki.providers.WikiPageProvider;
import org.apache.wiki.search.SearchProvider;
import org.silverpeas.components.wiki.SilverWikiEngine;
import org.silverpeas.core.index.indexing.model.FullIndexEntry;
import org.silverpeas.core.index.indexing.model.IndexEngineProxy;
import org.silverpeas.core.index.indexing.model.IndexEntryKey;
import org.silverpeas.core.index.search.PlainSearchResult;
import org.silverpeas.core.index.search.SearchEngine;
import org.silverpeas.core.index.search.SearchEngineProvider;
import org.silverpeas.core.index.search.model.MatchingIndexEntry;
import org.silverpeas.core.index.search.model.ParseException;
import org.silverpeas.core.index.search.model.QueryDescription;
import org.silverpeas.core.index.search.model.SearchResult;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

/**
 * The JSPWiki search service used by the wiki engine handles both the indexation and the search of
 * the pages that were created in the wiki. The goal here is to use the indexation and the search
 * service of Silverpeas to both index and retrieve by search the different pages in each Wiki
 * application instance in the Silverpeas platform.
 * @author mmoquillon
 */
public class SilverpeasSeachProvider implements SearchProvider {

  private WikiEngine wikiEngine;

  @Override
  public void pageRemoved(final WikiPage page) {
    Objects.requireNonNull(page);
    requiresWikiInstanceId();
    try {
      IndexEngineProxy.removeIndexEntry(
          new IndexEntryKey(getWikiInstanceId(), page.getClass().getSimpleName(),
              page.getName()));
    } catch (InternalWikiException e) {
      logNotInSilverpeasWikiContext(e);
    }
  }

  @Override
  public void reindexPage(final WikiPage page) {
    Objects.requireNonNull(page);
    requiresWikiInstanceId();
    try {
      FullIndexEntry indexEntry =
          new FullIndexEntry(getWikiInstanceId(), page.getClass().getSimpleName(),
              page.getName());
      indexEntry.setLastModificationUser(page.getAuthor());
      indexEntry.setLastModificationDate(page.getLastModified());

      if (page instanceof Attachment) {
        WikiAttachmentProvider provider = wikiEngine.getAttachmentManager().getCurrentProvider();
        Attachment attachment = (Attachment) page;
        if (provider instanceof SilverpeasAttachmentProvider) {
          SilverpeasAttachmentProvider silverpeasProvider = (SilverpeasAttachmentProvider) provider;
          try {
            File attachmentFile = silverpeasProvider.getAttachmentFile(attachment);
            indexEntry.addFileContent(attachmentFile.getAbsolutePath(), null, null, null);
          } catch (ProviderException e) {
            SilverLogger.getLogger(this).error(e);
          }
        }
      } else {
        indexEntry.addTextContent(wikiEngine.getPureText(page));
      }

      IndexEngineProxy.addIndexEntry(indexEntry);
    } catch (InternalWikiException e) {
      logNotInSilverpeasWikiContext(e);
    }
  }

  @Override
  public Collection findPages(final String query, final WikiContext wikiContext)
      throws ProviderException {
    Objects.requireNonNull(query);
    Objects.requireNonNull(wikiContext);
    requiresWikiInstanceId();
    final AuthorizationManager auth = wikiEngine.getAuthorizationManager();
    final SearchEngine searchEngine = SearchEngineProvider.getSearchEngine();
    final QueryDescription queryTxt = new QueryDescription(query);
    try {
      final PlainSearchResult response = searchEngine.search(queryTxt);
      final List<WikiSearchResult> wikiResults = new ArrayList<>(response.getEntries().size());
      for (final MatchingIndexEntry entry : response.getEntries()) {
        final SearchResult result = SearchResult.fromIndexEntry(entry);
        final String pageType = result.getType();
        final String pageName = result.getId();
        final String wikiId = result.getInstanceId();
        if (getWikiInstanceId().equals(wikiId) &&
            pageType.equals(WikiPage.class.getSimpleName())) {
          WikiPage page = wikiEngine.getPage(pageName, WikiPageProvider.LATEST_VERSION);
          PagePermission perm = new PagePermission(page, PagePermission.VIEW_ACTION);
          if (auth.checkPermission(wikiContext.getWikiSession(), perm)) {
            wikiResults.add(new WikiSearchResult(page, result.getScore(), result.getKeywords()));
          }
        }
      }
      return wikiResults;
    } catch (ParseException e) {
      throw new ProviderException(e.getMessage());
    }
  }

  @Override
  public void initialize(final WikiEngine engine, final Properties properties) {
    this.wikiEngine = engine;
  }

  @Override
  public String getProviderInfo() {
    return "Silverpeas Wiki Search Provider";
  }

  private String getWikiInstanceId() {
    String wikiId = "";
    if (this.wikiEngine instanceof SilverWikiEngine) {
      wikiId = ((SilverWikiEngine) this.wikiEngine).getWikiInstanceId();
    }
    return wikiId;
  }

  private void requiresWikiInstanceId() {
    if (StringUtil.isNotDefined(getWikiInstanceId())) {
      throw new IllegalArgumentException(
          "The Search provider is invoked out of a Wiki instance context");
    }
  }

  private void logNotInSilverpeasWikiContext(final InternalWikiException e) {
    SilverLogger.getLogger(this)
        .silent(e)
        .warn("The Silverpeas Search Provider is invoked out of the Silverpeas Wiki Engine " +
            "context");
  }

}
  