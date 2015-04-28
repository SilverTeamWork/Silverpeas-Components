/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.blog.web;

import com.silverpeas.blog.model.PostDetail;
import com.silverpeas.web.HATEOASWebEntity;
import com.silverpeas.web.LinkMetadataEntity;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.publication.model.PublicationDetail;
import org.silverpeas.search.indexEngine.model.IndexManager;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.util.Date;
import java.util.List;


/**
 * The post entity is a post object that is exposed in the web as an entity (web entity). As
 * such, it publishes only some of its attributes It represents a blog post in Silverpeas with some
 * additional information such as the URI for accessing it.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class PostEntity implements HATEOASWebEntity {

  @XmlElement(defaultValue = "")
  private URI uri;
  @XmlElement(defaultValue = "")
  private String id;
  @XmlElement(required = true)
  @NotNull
  @Size(min = 2)
  private String componentId;
  @XmlElement(required = true)
  @NotNull
  private String title;
  @XmlElement(defaultValue = "")
  private String content;
  @XmlElement
  private Date dateEvent;
  @XmlElement
  private int nbComments;
  @XmlElement
  private List<LinkMetadataEntity> links;


  public static PostEntity fromPost(PostDetail postDetail) {
    return new PostEntity(postDetail);
  }

  /**
   * Default constructor
   * @param postDetail
   */
  public PostEntity(PostDetail postDetail) {
    this.id = postDetail.getId();
    this.componentId = postDetail.getComponentInstanceId();
    this.title = postDetail.getTitle();
    this.content = postDetail.getContent();
    this.nbComments = postDetail.getNbComments();
    this.dateEvent = postDetail.getDateEvent();
  }

  /**
   * Sets a URI to this entity. With this URI, it can then be accessed through the Web.
   * @param uri the web entity URI.
   * @return itself.
   */
  public PostEntity withURI(final URI uri) {
    this.uri = uri;
    return this;
  }

  /**
   * Gets the post detail business objet this entity represent.
   * TODO handle post category in another version
   * @return a Post detail instance.
   */
  public PostDetail toPostDetail(UserDetail ud) {
    String categoryId = "";
    PublicationDetail pub =
        new PublicationDetail("X", this.title, "", null, null, null, null, "1", null, null, "");
    pub.getPK().setComponentName(getComponentId());
    pub.setCreatorId(ud.getId());
    pub.setCreatorName(ud.getDisplayedName());
    pub.setCreationDate(new Date());
    pub.setIndexOperation(IndexManager.NONE);
    PostDetail newPost = new PostDetail(pub, categoryId, getDateEvent());
    newPost.setContent(this.content);
    return newPost;
  }


  /**
   * Gets the URI of this post entity.
   * @return the URI with which this entity can be access through the Web.
   */
  @Override
  public URI getURI() {
    return uri;
  }

  public String getComponentId() {
    return componentId;
  }

  public void setComponentId(final String componentId) {
    this.componentId = componentId;
  }

  public String getId() {
    return id;
  }

  public void setId(final String id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(final String title) {
    this.title = title;
  }

  public String getContent() {
    return content;
  }

  public void setContent(final String content) {
    this.content = content;
  }

  public Date getDateEvent() {
    return dateEvent;
  }

  public void setDateEvent(final Date dateEvent) {
    this.dateEvent = dateEvent;
  }

  public int getNbComments() {
    return nbComments;
  }

  public void setNbComments(final int nbComments) {
    this.nbComments = nbComments;
  }

  /**
   * Sets a list of links to this entity. Each link represents an operation available on this
   * entity.
   * @param links the list of operations available from this entity for current user.
   * @return itself.
   */
  public PostEntity withLinks(List<LinkMetadataEntity> links) {
    this.links = links;
    return this;
  }

  public List<LinkMetadataEntity> getLinks() {
    return links;
  }

  // Must have no argument constructor
  protected PostEntity() {
  }
}
