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

import org.apache.wiki.attachment.Attachment;
import org.silverpeas.components.wiki.SilverWikiEngine;

/**
 * A decorator of a JSPWiki attachment. It adds useful methods on the
 * {@link org.apache.wiki.attachment.Attachment} instances like the path of the attached file or
 * the unique identifier of the attachment.
 * @author mmoquillon
 */
public class SilverAttachment extends Attachment {

  private final String ATTACHMENT_ID = "id";
  private final String WIKI_ID = "instanceId";
  private final SilverWikiEngine engine;

  public static SilverAttachment decorate(final Attachment attachment) {
    return new SilverAttachment(attachment);
  }

  @SuppressWarnings("unchecked")
  private SilverAttachment(final Attachment attachment) {
    super(SilverWikiEngine.getFromCache(),
        attachment.getParentName(), attachment.getFileName());
    this.engine = SilverWikiEngine.getFromCache();
    setAcl(attachment.getAcl());
    setAuthor(attachment.getAuthor());
    setCacheable(attachment.isCacheable());
    setLastModified(attachment.getLastModified());
    setSize(attachment.getSize());
    setVersion(attachment.getVersion());
    if (attachment.hasMetadata()) {
      setHasMetadata();
    }
    attachment.getAttributes().forEach((k,v) -> this.setAttribute((String)k, v));
  }

  public String getWikiInstanceId() {
    String wikiId = (String) getAttribute(WIKI_ID);
    if (wikiId == null) {
      wikiId = getWikiEngine().getWikiInstanceId();
    }
    return wikiId;
  }

  /**
   * Gets the unique identifier of this attachment. It is set only if this attachment is stored
   * on attachment repository otherwise the identifier is null.
   * @return the unique identifier of this attachment or null if the attachment isn't yet stored
   * into the attachment repository.
   */
  public String getId() {
    return (String) getAttribute(ATTACHMENT_ID);
  }

  /**
   * Gets the unique identifier of the page to which this attachment belongs.
   * @return the unique identifier of the parent page.
   */
  public String getPageId() {
    return String.valueOf(getParentName().hashCode());
  }

  private SilverWikiEngine getWikiEngine() {
    return engine;
  }
}
  