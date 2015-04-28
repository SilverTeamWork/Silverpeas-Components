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

import com.silverpeas.web.HATEOASWebEntity;
import com.silverpeas.web.LinkMetadataEntity;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.util.List;


/**
 * The blog entity is a blog object that is exposed in the web as an entity (web entity). As
 * such, it publishes only some of its attributes It represents a blog application in Silverpeas
 * with some additional information such as the URI for accessing it.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class BlogEntity implements HATEOASWebEntity {

  @XmlElement(defaultValue = "")
  private URI uri;
  @XmlElement(required = true)
  @NotNull
  @Size(min = 2)
  private String componentId;
  @XmlElement(required = true)
  @NotNull
  private String name;
  @XmlElement(defaultValue = "")
  private String description;
  @XmlElement
  private List<LinkMetadataEntity> links;

  public static BlogEntity fromComponentInstance(ComponentInstLight componentInst) {
    return new BlogEntity(componentInst);
  }

  /**
   * Default constructor
   * @param componentInst
   */
  public BlogEntity(ComponentInstLight componentInst) {
    this.componentId = componentInst.getId();
    this.name = componentInst.getLabel();
    this.description = componentInst.getDescription();
  }

  /**
   * Sets a URI to this entity. With this URI, it can then be accessed through the Web.
   * @param uri the web entity URI.
   * @return itself.
   */
  public BlogEntity withURI(final URI uri) {
    this.uri = uri;
    return this;
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

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(final String description) {
    this.description = description;
  }

  /**
   * Sets a list of links to this entity. Each link represents an operation available on this
   * entity.
   * @param links the list of operations available from this entity for current user.
   * @return itself.
   */
  public BlogEntity withLinks(List<LinkMetadataEntity> links) {
    this.links = links;
    return this;
  }

  public List<LinkMetadataEntity> getLinks() {
    return links;
  }

  // Must have no argument constructor
  protected BlogEntity() {
  }

}
