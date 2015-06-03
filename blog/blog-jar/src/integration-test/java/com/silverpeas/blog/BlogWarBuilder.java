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
package com.silverpeas.blog;

import org.silverpeas.test.BasicWarBuilder;

/**
 * @author ebonnet
 */
public class BlogWarBuilder extends BasicWarBuilder{

  /**
   * Constructs a war builder for the specified test class. It will load all the resources in the
   * same packages of the specified test class.
   * @param test the class of the test for which a war archive will be build.
   */
  protected <T> BlogWarBuilder(final Class<T> test) {
    super(test);
  }

  /**
   * Constructs an instance of the basic war archive builder for the specified test class.
   * @param test the test class for which a war will be built. Any resources located in the same
   * package of the test will be loaded into the war.
   * @param <T> the type of the test.
   * @return a basic builder of the war archive.
   */
  public static <T> BlogWarBuilder onWarForTestClass(Class<T> test) {
    BlogWarBuilder warBuilder = new BlogWarBuilder(test);
    warBuilder.addMavenDependencies("javax.jcr:jcr");
    warBuilder.addMavenDependenciesWithPersistence("org.silverpeas.core:lib-core");
    warBuilder.addMavenDependenciesWithPersistence("org.silverpeas.core.ejb-core:node");
    warBuilder.addMavenDependencies("org.silverpeas.core.ejb-core:tagcloud");
    warBuilder.addMavenDependencies("org.silverpeas.core.ejb-core:publication");
    warBuilder.addMavenDependencies("org.apache.tika:tika-core");
    warBuilder.addMavenDependencies("org.apache.tika:tika-parsers");
    warBuilder.addAsResource("META-INF/test-MANIFEST.MF", "META-INF/MANIFEST.MF");
    return warBuilder;
  }

}
