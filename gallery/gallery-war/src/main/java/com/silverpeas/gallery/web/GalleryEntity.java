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

package com.silverpeas.gallery.web;


import com.silverpeas.web.ApplicationWebEntity;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The gallery entity is a gallery object that is exposed in the web as an entity (web
 * entity). As such, it publishes only some of its attributes It represents a gallery
 * application in Silverpeas with some additional information such as the URI for accessing it.
 * @author ebonnet
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class GalleryEntity extends ApplicationWebEntity<GalleryEntity> {

  public static GalleryEntity fromComponentInstance(ComponentInstLight componentInst) {
    return new GalleryEntity(componentInst);
  }

  /**
   * Default constructor
   * @param componentInst
   */
  public GalleryEntity(ComponentInstLight componentInst) {
    super(componentInst);
  }

}
