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

package com.silverpeas.blog.model;

import org.silverpeas.persistence.model.identifier.ExternalIntegerIdentifier;
import org.silverpeas.persistence.model.jpa.AbstractJpaCustomEntity;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * @author ebonnet
 */
@Entity
@Table(name = "sc_blog_post")
@NamedQuery(name = "blog.posts",
    query = "from Post p where p.instanceId = :instanceId")
@AttributeOverride(name = "id", column = @Column(name = "pubId", columnDefinition = "int"))
public class Post extends AbstractJpaCustomEntity<Post, ExternalIntegerIdentifier>
    implements Serializable {

  @Column(name = "instanceId")
  private String instanceId;

  @Column
  private String dateEvent;


  protected Post() {
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(final String instanceId) {
    this.instanceId = instanceId;
  }

  public String getDateEvent() {
    return dateEvent;
  }

  public void setDateEvent(final String dateEvent) {
    this.dateEvent = dateEvent;
  }
}
