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

import com.silverpeas.annotation.Authorized;
import com.silverpeas.blog.control.BlogService;
import com.silverpeas.blog.model.BlogRuntimeException;
import com.silverpeas.blog.model.PostCriteria;
import com.silverpeas.blog.model.PostDetail;
import com.silverpeas.web.LinkMetadataEntity;
import com.silverpeas.web.RESTWebService;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.PaginationPage;
import com.stratelia.webactiv.publication.model.PublicationDetail;
import org.silverpeas.core.admin.OrganizationController;
import org.silverpeas.persistence.repository.PaginationCriterion;
import org.silverpeas.search.indexEngine.model.IndexManager;
import org.silverpeas.util.HttpMethod;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.silverpeas.blog.web.BlogResourceURIs.BLOG_BASE_URI;
import static com.silverpeas.blog.web.BlogResourceURIs.BLOG_POSTS_URI_PART;

/**
 * A REST Web resource representing a given blog. It is a web service that provides an access to
 * a blog referenced by its URL.
 */
@RequestScoped
@Path(BLOG_BASE_URI + "/{componentId}")
@Authorized
public class PostResource extends RESTWebService {

  @PathParam("componentId")
  private String componentId;

  @Inject
  private BlogService blogService;

  @Inject
  private OrganizationController organizationController;

  /**
   * Gets the JSON representation of the specified existing blog. If the blog doesn't exist, a
   * 404 HTTP code is returned. If the user isn't authentified, a 401 HTTP code is returned. If the
   * user isn't authorized to access the blog, a 403 is returned. If a problem occurs when
   * processing the request, a 503 HTTP code is returned.
   * @return the response to the HTTP GET request with the JSON representation of the asked blog.
   * TODO add metadata on blog identifier, check user rights in order do send link metadata actions
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public BlogEntity getBlog() {
    try {
      ComponentInstLight blogInst = organizationController.getComponentInstLight(inComponentId());
      URI blogURI = getUriInfo().getAbsolutePath();
      List<LinkMetadataEntity> links = getBlogOperations();
      return asWebEntity(blogInst, identifiedBy(blogURI), redirectTo(links));
    } catch (BlogRuntimeException ex) {
      throw new WebApplicationException(ex, Status.NOT_FOUND);
    } catch (Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * @return the list of operations available on a blog application
   */
  private List<LinkMetadataEntity> getBlogOperations() {
    List<LinkMetadataEntity> links = new ArrayList<>();
    // All the client which access this blog can access the list of posts
    links.add(LinkMetadataEntity
        .from(getUriInfo().getRequestUriBuilder().path(BLOG_POSTS_URI_PART).build())
        .withRel(BLOG_POSTS_URI_PART).linkTo(HttpMethod.GET));
    return links;
  }

  /**
   * Converts the blog application into its corresponding web entity.
   * @param blog the blog component instance light.
   * @param blogURI the URI of the blog.
   * @return the corresponding post detail entity.
   */
  protected BlogEntity asWebEntity(final ComponentInstLight blog, URI blogURI,
      List<LinkMetadataEntity> links) {
    return BlogEntity.fromComponentInstance(blog).withURI(blogURI).withLinks(links);
  }

  /**
   * Gets the JSON representation of the specified existing post. If the post doesn't exist, a
   * 404 HTTP code is returned. If the user isn't authentified, a 401 HTTP code is returned. If the
   * user isn't authorized to access the post, a 403 is returned. If a problem occurs when
   * processing the request, a 503 HTTP code is returned.
   * @param onPostId the unique identifier of the post.
   * @return the response to the HTTP GET request with the JSON representation of the asked post.
   */
  @GET
  @Path(BLOG_POSTS_URI_PART + "/{postId}")
  @Produces(MediaType.APPLICATION_JSON)
  public PostEntity getPost(@PathParam("postId") String onPostId) {
    PostEntity returnedEntity;
    try {
      PostDetail postDetail = blogService().getContentById(onPostId);
      URI postURI = getUriInfo().getAbsolutePath();
      returnedEntity =
          asWebEntity(postDetail, identifiedBy(postURI), redirectTo(getPostOperations(postURI)));
    } catch (BlogRuntimeException ex) {
      throw new WebApplicationException(ex, Status.NOT_FOUND);
    } catch (Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
    checkIsValid(returnedEntity);
    return returnedEntity;
  }

  /**
   * @param postURI the post URI
   * @return list of operations available on post
   */
  private List<LinkMetadataEntity> getPostOperations(final URI postURI) {
    List<LinkMetadataEntity> links = new ArrayList<>();
    links.add(LinkMetadataEntity.from(postURI).withRel("self").linkTo(HttpMethod.GET));
    if (getGreaterUserRole().isGreaterThanOrEquals(SilverpeasRole.writer)) {
      links.add(LinkMetadataEntity.from(postURI).withRel("create").linkTo(HttpMethod.POST));
      links.add(LinkMetadataEntity.from(postURI).withRel("update").linkTo(HttpMethod.PUT));
      links.add(LinkMetadataEntity.from(postURI).withRel("delete").linkTo(HttpMethod.DELETE));
    }
    return links;
  }

  /**
   * Gets the JSON representation of all the posts on referred the resource. If the user isn't
   * authentified, a 401 HTTP code is returned. If the user isn't authorized to access the post,
   * a 403 is returned. If a problem occurs when processing the request, a 503 HTTP code is
   * returned.
   * @param page the number of page
   * @return the response to the HTTP GET request with the JSON representation of the posts on
   * the referred resource.
   */
  @GET
  @Path(BLOG_POSTS_URI_PART)
  @Produces(MediaType.APPLICATION_JSON)
  public PostEntity[] getAllPosts(@DefaultValue("-1;-1") @QueryParam("page") final String page) {
    try {
      PaginationPage paginationPage = fromPage(page);
      Collection<PostDetail> thePosts;
      if (paginationPage != null) {
        PostCriteria postCriteria = PostCriteria.from(inComponentId())
            .withPagination(PaginationCriterion.from(paginationPage));
        thePosts = blogService().getPaginatedPosts(inComponentId(), postCriteria);
      } else {
        thePosts = blogService().getAllPosts(inComponentId());
      }
      return asWebEntities(thePosts);
    } catch (BlogRuntimeException ex) {
      throw new WebApplicationException(ex, Status.NOT_FOUND);
    } catch (Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Creates a new post from its JSON representation and returns it with its URI identifying it
   * in Silverpeas. The unique identifier of the post isn't taken into account, so if the post
   * already exist, it is then cloned with a new identifier (thus with a new URI). If the user
   * isn't authentified, a 401 HTTP code is returned. If the user isn't authorized to save the
   * post, a 403 is returned. If a problem occurs when processing the request, a 503 HTTP code is
   * returned.
   * @param postToSave the post to save in Silverpeas.
   * @return the response to the HTTP POST request with the JSON representation of the saved
   * post.
   */
  @POST
  @Path(BLOG_POSTS_URI_PART)
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response saveNewPost(final PostEntity postToSave) {
    checkIsValid(postToSave);
    try {
      PostDetail post = postToSave.toPostDetail(getUserDetail());
      blogService().createPost(post);
      PostDetail savedPost = blogService().getContentById(post.getId());
      // Publish post
      blogService().draftOutPost(savedPost);
      URI postURI = getUriInfo().getRequestUriBuilder().path(savedPost.getId()).build();
      return Response.created(postURI).entity(
          asWebEntity(savedPost, identifiedBy(postURI), redirectTo(getPostOperations(postURI))))
          .build();
    } catch (BlogRuntimeException ex) {
      throw new WebApplicationException(ex, Status.CONFLICT);
    } catch (Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Updates the post from its JSON representation and returns it once updated. If the post to
   * update doesn't match with the requested one, a 400 HTTP code is returned. If the post
   * doesn't exist, a 404 HTTP code is returned. If the user isn't authentified, a 401 HTTP code is
   * returned. If the user isn't authorized to save the post, a 403 is returned. If a problem
   * occurs when processing the request, a 503 HTTP code is returned.
   * @param postId the unique identifier of the post to update.
   * @param postToUpdate the post to update in Silverpeas.
   * @return the response to the HTTP PUT request with the JSON representation of the updated post.
   */
  @PUT
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Path(BLOG_POSTS_URI_PART + "/{postId}")
  public PostEntity updatePost(@PathParam("postId") String postId, final PostEntity postToUpdate) {
    checkIsValid(postToUpdate);
    if (!postToUpdate.getId().equals(postId)) {
      throw new WebApplicationException(Status.BAD_REQUEST);
    }
    try {
      PostDetail updatedPost = getUpdatedPostDetail(postId, postToUpdate);
      // save the post
      blogService().updatePost(updatedPost);
      URI postURI = getUriInfo().getRequestUriBuilder().path(updatedPost.getId()).build();
      return asWebEntity(updatedPost, identifiedBy(postURI),
          redirectTo(getPostOperations(postURI)));
    } catch (BlogRuntimeException ex) {
      throw new WebApplicationException(ex, Status.NOT_FOUND);
    } catch (Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  private PostDetail getUpdatedPostDetail(final String postId, final PostEntity postToUpdate) {
    PostDetail updatedPost = blogService().getContentById(postId);
    PublicationDetail pub = updatedPost.getPublication();
    pub.setName(postToUpdate.getTitle());
    pub.setUpdaterId(getUserDetail().getId());
    pub.setUpdateDate(new Date());
    if (pub.isDraft()) {
      pub.setIndexOperation(IndexManager.NONE);
    }
    //updatedPost.setCategoryId(postToUpdate.getCategoryId());
    updatedPost.setDateEvent(postToUpdate.getDateEvent());
    //Date.from(ZonedDateTime.parse(postToUpdate.getDateEvent()).toInstant())
    updatedPost.setContent(postToUpdate.getContent());
    return updatedPost;
  }

  /**
   * Deletes the specified existing post. If the post doesn't exist, nothing is done, so that
   * the HTTP DELETE request remains indempotent as defined in the HTTP specification.. If the user
   * isn't authentified, a 401 HTTP code is returned. If the user isn't authorized to access the
   * post, a 403 is returned. If a problem occurs when processing the request, a 503 HTTP code
   * is returned.
   * @param onPostId the unique identifier of the post to delete.
   */
  @DELETE
  @Path(BLOG_POSTS_URI_PART + "/{postId}")
  public void deletePost(@PathParam("postId") String onPostId) {
    try {
      blogService().deletePost(onPostId, inComponentId());
    } catch (BlogRuntimeException ex) {
      Logger.getLogger(getClass().getName()).log(Level.WARNING, ex.getMessage());
    } catch (Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Gets the identifier of the Silverpeas instance to which the post content belongs.
   * @return the Silverpeas component instance identifier.
   */
  protected String inComponentId() {
    return getComponentId();
  }

  /**
   * Gets a business service on blog.
   * @return a blog service instance.
   */
  protected BlogService blogService() {
    return blogService;
  }

  /**
   * Converts the specified list of posts into their corresponding web entities.
   * @param posts the posts to convert.
   * @return an array with the corresponding post entities.
   */
  protected PostEntity[] asWebEntities(Collection<PostDetail> posts) {
    PostEntity[] entities = new PostEntity[posts.size()];
    int i = 0;
    for (PostDetail post : posts) {
      URI postURI = getUriInfo().getRequestUriBuilder().path(post.getId()).replaceQuery("").build();
      entities[i++] =
          asWebEntity(post, identifiedBy(postURI), redirectTo(getPostOperations(postURI)));
    }
    return entities;
  }

  /**
   * Converts the post detail into its corresponding web entity.
   * @param post the post detail to convert.
   * @param postURI the URI of the post.
   * @param links the list of operations available on this post
   * @return the corresponding post detail entity.
   */
  protected PostEntity asWebEntity(final PostDetail post, URI postURI,
      final List<LinkMetadataEntity> links) {
    return PostEntity.fromPost(post).withURI(postURI).withLinks(links);
  }

  protected URI identifiedBy(URI uri) {
    return uri;
  }

  protected List<LinkMetadataEntity> redirectTo(List<LinkMetadataEntity> links) {
    return links;
  }

  @Override
  public String getComponentId() {
    return this.componentId;
  }

  /**
   * Check the specified post is valid. A post is valid if the following attributes are set:
   * componentId, content identifier and its author identifier.
   * @param thePost the post to validate.
   */
  protected void checkIsValid(final PostEntity thePost) {
    if (thePost == null || !thePost.getComponentId().equals(getComponentId())) {
      throw new WebApplicationException(Status.NOT_FOUND);
    }
  }

  /**
   * Gets a comparator of posts by their identifier, from the lower to the higher one.
   * @return a comparator of posts.
   */
  //TODO get a comparator by their event date from newer to older
  protected static Comparator<PostDetail> byId() {
    return new Comparator<PostDetail>() {

      @Override
      public int compare(PostDetail left, PostDetail right) {
        return left.getId().compareTo(right.getId());
      }

    };
  }
}
