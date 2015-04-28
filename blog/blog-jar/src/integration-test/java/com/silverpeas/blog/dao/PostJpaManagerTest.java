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
package com.silverpeas.blog.dao;

import com.silverpeas.blog.BlogWarBuilder;
import com.silverpeas.blog.control.BlogService;
import com.silverpeas.blog.control.BlogServiceFactory;
import com.silverpeas.blog.control.DefaultBlogService;
import com.silverpeas.blog.model.Post;
import com.silverpeas.blog.model.PostCriteria;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.persistence.repository.PaginationCriterion;
import org.silverpeas.test.rule.DbUnitLoadingRule;
import org.silverpeas.util.ServiceProvider;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

@RunWith(Arquillian.class)
public class PostJpaManagerTest {

  private static PostRepository repo;

  public PostJpaManagerTest() {
  }

  @Rule
  public DbUnitLoadingRule dbUnitLoadingRule =
      new DbUnitLoadingRule("create-database.sql", "blog-dataset.xml");

  @Deployment
  public static Archive<?> createTestArchive() {
    return BlogWarBuilder.onWarForTestClass(PostJpaManagerTest.class).testFocusedOn(warBuilder -> {
      warBuilder.addClasses(BlogService.class, DefaultBlogService.class, BlogServiceFactory.class);
      warBuilder.addPackages(true, "com.silverpeas.blog.model");
      warBuilder.addPackages(true, "com.silverpeas.blog.dao");
    }).build();
  }

  @Before
  public void generalSetUp() throws Exception {
    repo = ServiceProvider.getService(PostRepository.class);
  }

  @Test
  public void testGetPaginatedPosts() throws Exception {
    String instanceId = "blog5";
    PostCriteria postCriteria = PostCriteria.from(instanceId).withPagination(
        new PaginationCriterion(1, 3));
    List<Post> posts = repo.getPaginatedPosts(postCriteria);
    assertThat(posts, hasSize(3));
    postCriteria = PostCriteria.from(instanceId).withPagination(
        new PaginationCriterion(3, 3));
    posts = repo.getPaginatedPosts(postCriteria);
    assertThat(posts, hasSize(1));
  }

}
