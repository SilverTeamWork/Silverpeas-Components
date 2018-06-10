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

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.i18n.I18n;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.file.FileUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Properties;

import static java.nio.file.StandardOpenOption.CREATE_NEW;

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
   * Name of the subdirectory in the wiki page directory that contains all the versioning of the
   * Wiki pages.
   */
  private static final String WIKI_VERSIONING_DIR = "OLD";

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
  WikiSettings(final String wikiInstanceId) {
    this.wikiHomePath = FileRepositoryManager.getAbsolutePath(wikiInstanceId);
  }

  /**
   * Gets the absolute path of the home directory of the Wiki application instance. All the content
   * provided by the users will be added into it.
   * @return the path of the wiki instance home directory.
   */
  final String getWikiHomePath() {
    return this.wikiHomePath;
  }

  /**
   * Gets the absolute path of the directory into which the pages created by the users will be
   * stored.
   * @return the path of the wiki's page directory.
   */
  final String getWikiPageDirPath() {
    return Paths.get(this.wikiHomePath, WIKI_PAGE_DIR).toString();
  }

  /**
   * Gets the absolute path of the directory into which the version history of the Wiki page is
   * maintained.
   * @return the path of the wiki's page versioning information directory.
   */
  final String getWikiPageVersioningDirPath() {
    return Paths.get(this.wikiHomePath, WIKI_PAGE_DIR, WIKI_VERSIONING_DIR).toString();
  }

  /**
   * Gets the absolute path of the directory into which the attachments of pages uploaded by the
   * users will be stored.
   * @return the path of of the directory of all of the pages' attachments.
   */
  final String getWikiAttachmentDirPath() {
    return Paths.get(this.wikiHomePath, WIKI_ATTACHMENT_DIR).toString();
  }

  /**
   * Gets the maximum size supported by Silverpeas to upload the attachments.
   * @return the maximum size of the attachments supported by Silverpeas.
   */
  final long getAttachmentMaxSize() {
    return FileRepositoryManager.getUploadMaximumFileSize();
  }

  /**
   * Initializes the directory layout of the underlying wiki application instance for use by
   * JSPWiki. If the directory layout already exists, nothing is done.
   * @throws IOException if an error occurs while initializing the directory layout to store the
   * resources of the wiki application instance.
   */
  final void initialize() throws IOException {
    if (!Files.exists(Paths.get(wikiHomePath))) {
      Files.createDirectories(Paths.get(wikiHomePath));
      Files.createDirectory(Paths.get(getWikiPageDirPath()));
      Files.createDirectory(Paths.get(getWikiAttachmentDirPath()));
      Files.createDirectory(Paths.get(getWikiPageVersioningDirPath()));
      createEditHelpPage(I18n.get().getDefaultLanguage());
    }
  }

  /**
   * Cleans up the resources that were saved for the underlying wiki application instance such as
   * the Wiki pages, the attachments, and so on. Actually it does delete the Wiki home directory
   * of the wiki application instance.
   * @throws IOException if an error occurs while cleaning up the wiki resources.
   */
  final void cleanUp() throws IOException {
    if (Files.exists(Paths.get(wikiHomePath))) {
      FileUtil.forceDeletion(new File(wikiHomePath));
    }
  }

  /**
   * Creates the edition help page with the specified language for the current wiki instance.
   * @param lang an ISO-639-1 code.
   * @throws IOException if an error occurs while creating the help page.
   */
  private void createEditHelpPage(final String lang) throws IOException {
    User author = User.getCurrentRequester();
    if (author == null) {
      author = User.getById("0");
    }
    final InputStream input =
        getClass().getResourceAsStream(WIKI_RESOURCES_LOCATION + getSourceEditHelpPage(lang));
    Files.copy(input, Paths.get(getWikiPageDirPath(), getEditHelpPage()));
    Files.createDirectory(Paths.get(getWikiPageVersioningDirPath(), EDIT_HELP_PAGE));
    Properties props = new Properties();
    props.setProperty("1.author", author.getDisplayedName());
    props.store(Files.newOutputStream(
        Paths.get(getWikiPageVersioningDirPath(), EDIT_HELP_PAGE, "page.properties"), CREATE_NEW),
        (new Date()).toString());
  }

  private String getSourceEditHelpPage(final String lang) {
    return EDIT_HELP_PAGE + "_" + lang + ".txt";
  }

  private String getEditHelpPage() {
    return EDIT_HELP_PAGE + ".txt";
  }
}
  