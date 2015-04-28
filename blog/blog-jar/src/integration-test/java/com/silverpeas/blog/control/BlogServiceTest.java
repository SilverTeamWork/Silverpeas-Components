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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.silverpeas.blog.control;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.test.BasicWarBuilder;
import org.silverpeas.test.rule.DbUnitLoadingRule;

@RunWith(Arquillian.class)
public class BlogServiceTest {

  private static BlogService service;

  public BlogServiceTest() {
  }

  @Rule
  public DbUnitLoadingRule dbUnitLoadingRule =
      new DbUnitLoadingRule("create-database.sql", "blog-dataset.xml");

  @Deployment
  public static Archive<?> createTestArchive() {
    return BasicWarBuilder.onWarForTestClass(BlogServiceTest.class)
        .testFocusedOn(warBuilder -> {
          warBuilder.addClasses(BlogService.class, DefaultBlogService.class, BlogServiceFactory.class);
          warBuilder.addPackages(true, "com.silverpeas.blog.model");
          warBuilder.addPackages(true, "com.silverpeas.blog.dao");
        }).build();
  }

  @Before
  public void generalSetUp() throws Exception {
    service = BlogService.get();
  }

  @Test
  public void testGetPaginatedPosts() throws Exception {
    //TODO implements this service method
  }


  @Test
  public void testGetAllPosts() throws Exception {
    //TODO implements this service method
  }

}
