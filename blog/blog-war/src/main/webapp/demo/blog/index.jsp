<%--
  Copyright (C) 2000 - 2015 Silverpeas

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.

  As a special exception to the terms and conditions of version 3.0 of
  the GPL, you may redistribute this Program in connection with Free/Libre
  Open Source Software ("FLOSS") applications as described in Silverpeas's
  FLOSS exception. You should have received a copy of the text describing
  the FLOSS exception, and it is also available here:
  "https://www.silverpeas.org/legal/floss_exception.html"

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  GNU Affero General Public License for more details.

  You should have received a copy of the GNU Affero General Public License
  along with this program. If not, see <http://www.gnu.org/licenses/>.
  --%>

<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>Demo blog</title>
<view:looknfeel/>
<view:includePlugin name="popup"/>
<view:progressMessage/>

<script type="text/javascript">
function displayPosts() {
  // Check if component identifier has been correctly set
  if (!$("#blogId").val()) {
    showInformation("Please fill blog identifier");
  } else {
    componentId = $("#blogId").val();
    $.get("<c:url value='/services/blogs/'/>" + componentId + "/posts", function(data) {
          $.closeProgressMessage();
          alert('data = ' + data);
          $("#jsonResult").html(data);
        }, 'text');
    $.progressMessage();
  }
}

function displayPost() {
  // Check if component identifier and post identifierv has been correctly set
  if (!$("#blogId").val() && !$("#postId").val()) {
    showInformation("Please fill blog identifier and post identifier");
  } else {
    var componentId = $("#blogId").val();
    var postId = $("#postId").val();
    $.get("<c:url value='/services/blogs/'/>" + componentId + "/posts/" + postId, function(data) {
          $.closeProgressMessage();
          $("#post_componentId").val(data.componentId);
          $("#post_title").val(data.title);
          $("#post_postId").val(data.id);
          $("#post_content").val(data.content);
          var currentTime = new Date(data.dateEvent);
          var month = currentTime.getMonth() + 1;
          var day = currentTime.getDate();
          var year = currentTime.getFullYear();
          var date = day + "/" + month + "/" + year;
          $("#post_eventDate").val(date);
          $("#post_categoryId").val(data.categoryId);
          $("#jsonResult").html(data);
        }, 'json');
    $.progressMessage();
  }
}

function updatePost() {
  if (!$("#post_componentId").val() && !$("#post_postId").val()) {
    showInformation("Please fill blog identifier and post identifier form or get existing post");
  } else {
    var postData = {
      "id" : $('#post_postId').val(),
      "componentId" : $('#post_componentId').val(),
      "title" : $('#post_title').val(),
      "content" : $('textarea#post_content').val(),
      "dateEvent" : $('#post_eventDate').val(),
      "nbComments" : 0
    };
    var request = $.ajax({
      url : '/silverpeas/services/blogs/' + $("#post_componentId").val() + "/posts/" + $("#post_postId").val(),
      type : 'PUT',
      dataType: 'json',
      contentType: 'application/json; charset=utf-8',
      data : JSON.stringify(postData)
    });
    request.done(function(msg) {
      $("#log").html(msg);
    });
    request.fail(function(jqXHR, textStatus) {
      alert("Request failed: " + textStatus);
    });
  }
}

function createPost() {
  if (!$("#post_componentId").val() || !$('#post_title').val()) {
    showInformation("Please fill blog identifier or blog title");
  } else {
    var postData = {
      "componentId" : $('#post_componentId').val(),
      "title" : $('#post_title').val(),
      "content" : $('textarea#post_content').val(),
      "dateEvent" : $('#post_eventDate').val(),
      "nbComments" : 0
    };

    var request = $.ajax({
      url : '/silverpeas/services/blogs/' + $("#post_componentId").val() + '/posts',
      type : 'POST',
      dataType: 'json',
      contentType: 'application/json; charset=utf-8',
      data : JSON.stringify(postData)
    });
    request.done(function(msg) {
      $("#log").html(msg);
    });
    request.fail(function(jqXHR, textStatus) {
      alert("Request failed: " + textStatus);
    });
  }
}

function deletePost() {
  if (!$("#post_componentId").val() && !$("#post_postId").val()) {
    showInformation("Please fill blog identifier and post identifier form or get existing post");
  } else {
    var request = $.ajax({
      url : '/silverpeas/services/blogs/' + $("#post_componentId").val() + "/posts/" + $("#post_postId").val(),
      type : 'DELETE',
      dataType : 'json',
      contentType : 'application/json; charset=utf-8',
    });
    request.done(function(msg) {
      $("#log").html(msg);
    });
    request.fail(function(jqXHR, textStatus) {
      alert("Request failed: " + textStatus);
    });
  }
}


function showInformation(msg) {
  $('#message').html(msg);
  $('#message').popup('information', {
    callback : function() {
      return true;
    }
  });
}

</script>
</head>
<body>
<h1>Usage examples of Silverpeas blog REST api</h1>
<ul>
  <li><a href="javascript:onclick=displayPosts()">Display posts of a blog application</a></li>
  <li><a href="javascript:onclick=displayPost()">Retrieve post data and display it in a form</a>
  </li>
  <li><a href="javascript:onclick=updatePost()">Update a post data</a></li>
  <li><a href="javascript:onclick=createPost()">Create a new post</a></li>
  <li><a href="javascript:onclick=deletePost()">Delete post</a></li>
</ul>
<p>
  Blog identifier :<input type="text" id="blogId" value="" name="blogId"/><br/>
  Post identifier :<input type="text" id="postId" value="" name="postId"/><br/>
</p>

<div id="message" style="display: none">
  Content of the message to be displayed...
</div>
<div id="result">
  <div id="postForm">
    post_componentId <input type="text" id="post_componentId" value="" name="componentId"/><br/>
    post_postId <input type="text" id="post_postId" value="" name="ppostId"/><br/>
    post_title <input type="text" id="post_title" value="" name="title"/><br/>
    post_content
    <textarea name="textarea" id="post_content" rows="10" cols="50" name="content"></textarea>
    <br/>
    post_eventDate
    <input type="text" id="post_eventDate" value="" maxlength="10" size="12" name="DateEvent" class="dateToPick hasDatepicker"/><br/>
    post_categoryId <input type="text" id="post_categoryId" value=""/><br/>
    nbComments <input type="text" id="post_nbComments"/>
  </div>
  JSON Response :
  <div id="jsonResult">
  </div>
  <div id="log">
  </div>
</div>

<view:progressMessage/>
</body>
</html>