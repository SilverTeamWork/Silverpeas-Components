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

package org.silverpeas.components.wiki;

import org.silverpeas.core.util.file.FileRepositoryManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Settings of a Wiki application
 * @author mmoquillon
 */
public class WikiSettings {

  /**
   * The location of the directory that contains all the JSP files (in our case all of the
   * JSPWiki's JSP files).
   */
  public static final String WIKI_BASE_DIR = "/wiki/jsp";

  /**
   * Name of the subdirectory in the wiki home directory that contains all the Wiki pages.
   */
  private static final String WIKI_PAGE_DIR = "pages";

  /**
   * Name of the subdirectory in the wiki home directory that contains all the attachments of wiki
   * pages.
   */
  private static final String WIKI_ATTACHMENT_DIR = "attachments";

  /**
   * The base name of the Wiki page about the edition help.
   */
  private static final String EDIT_HELP_PAGE = "EditPageHelp";

  private static final String WIKI_RESOURCES_LOCATION =
      "/org/silverpeas/components/wiki/resources/";

  private final String wikiHomePath;

  /**
   * Constructs a {@link WikiSettings} object for the specified wiki application instance.
   * @param wikiInstanceId the unique identifier of a wiki instance.
   */
  public WikiSettings(final String wikiInstanceId) {
    this.wikiHomePath = FileRepositoryManager.getAbsolutePath(wikiInstanceId);
  }

  /**
   * Gets the absolute path of the home directory of the Wiki application instance. All the content
   * provided by the users will be added into it.
   * @return the path of the wiki instance home directory.
   */
  public final String getWikiHomePath() {
    return this.wikiHomePath;
  }

  /**
   * Gets the absolute path of the directory into which the pages created by the users will be
   * stored.
   * @return the path of the wiki's page directory.
   */
  public final String getWikiPageDirPath() {
    return Paths.get(this.wikiHomePath, WIKI_PAGE_DIR).toString();
  }

  /**
   * Gets the absolute path of the directory into which the attachments of pages uploaded by the
   * users will be stored.
   * @return the path of of the directory of all of the pages' attachments.
   */
  public final String getWikiAttachmentDirPath() {
    return Paths.get(this.wikiHomePath, WIKI_ATTACHMENT_DIR).toString();
  }

  /**
   * Gets the maximum size supported by Silverpeas to upload the attachments.
   * @return the maximum size of the attachments supported by Silverpeas.
   */
  public final long getAttachmentMaxSize() {
    return FileRepositoryManager.getUploadMaximumFileSize();
  }

  /**
   * Creates the edition help page with the specified language for the current wiki instance.
   * @param lang an ISO-639-1 code.
   * @throws IOException if an error occurs while creating the help page.
   */
  public final void createEditHelpPage(final String lang) throws IOException {
    final InputStream input =
        getClass().getResourceAsStream(WIKI_RESOURCES_LOCATION + getSourceEditHelpPage(lang));
    Files.copy(input, Paths.get(getWikiPageDirPath(), getEditHelpPage()));
  }

  private String getSourceEditHelpPage(final String lang) {
    return EDIT_HELP_PAGE + "_" + lang + ".txt";
  }

  private String getEditHelpPage() {
    return EDIT_HELP_PAGE + ".txt";
  }
}
  