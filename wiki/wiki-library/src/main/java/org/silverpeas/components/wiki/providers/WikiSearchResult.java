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

import org.apache.wiki.WikiPage;
import org.apache.wiki.search.SearchResult;

import java.util.Objects;

/**
 * Result of a search for a page in given wiki instance. It does the mapping between the search
 * result returned by the Silverpeas Search Engine and the search result expected by the JSPWiki
 * Search engine.
 * @author mmoquillon
 */
public class WikiSearchResult implements SearchResult {

  private final WikiPage page;
  private final int score;
  private final String[] contexts;

  WikiSearchResult(final WikiPage page, float score, String... contexts) {
    Objects.requireNonNull(page);
    this.page = page;
    this.score = (int)(score * 100);
    this.contexts = contexts == null ? new String[0] : contexts;
  }

  @Override
  public WikiPage getPage() {
    return page;
  }

  @Override
  public int getScore() {
    return score;
  }

  @Override
  public String[] getContexts() {
    return contexts;
  }
}
  