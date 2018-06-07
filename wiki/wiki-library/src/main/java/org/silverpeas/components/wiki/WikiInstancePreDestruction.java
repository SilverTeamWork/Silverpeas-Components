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

import org.silverpeas.core.admin.component.ComponentInstancePreDestruction;
import org.silverpeas.core.util.logging.SilverLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Deletes all the Wiki pages that were created before the Wiki application instance is destructed.
 * @author mmoquillon
 */
public class WikiInstancePreDestruction implements ComponentInstancePreDestruction {
  @Override
  public void preDestroy(final String componentInstanceId) {
    SilverWikiEngine.shutdown(componentInstanceId);
    WikiSettings settings = new WikiSettings(componentInstanceId);
    try {
      Files.deleteIfExists(Paths.get(settings.getWikiHomePath()));
    } catch (IOException e) {
      SilverLogger.getLogger(this).error(e);
    }
  }
}
  