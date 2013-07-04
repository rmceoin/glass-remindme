<!--
Copyright (C) 2013 Google Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->
<%@ page import="com.google.api.client.auth.oauth2.Credential" %>
<%@ page import="com.google.api.services.mirror.model.Contact" %>
<%@ page import="com.google.glassware.MirrorClient" %>
<%@ page import="com.google.glassware.WebUtil" %>
<%@ page import="com.google.glassware.LocationUtil" %>
<%@ page
    import="java.util.List" %>
<%@ page import="com.google.api.services.mirror.model.TimelineItem" %>
<%@ page import="com.google.api.services.mirror.model.Subscription" %>
<%@ page import="com.google.api.services.mirror.model.Attachment" %>
<%@ page import="com.google.api.services.mirror.model.Location" %>
<%@ page import="com.google.api.services.oauth2.model.Userinfo" %>

<%@ page import="com.google.glassware.MainServlet" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<!doctype html>
<%
  String userId = com.google.glassware.AuthUtil.getUserId(request);
  String appBaseUrl = WebUtil.buildUrl(request, "/");

  Credential credential = com.google.glassware.AuthUtil.getCredential(userId);
  Userinfo userInfo = MirrorClient.getUserinfo(userId);

  List<TimelineItem> timelineItems = MirrorClient.listItems(credential, 3L).getItems();

  Location locationHome = LocationUtil.getTag(userId, "home");
  if (locationHome==null) {
    locationHome = new Location();
  }
  Location locationWork = LocationUtil.getTag(userId, "work");
  if (locationWork==null) {
    locationWork = new Location();
  }


%>
<html>
<head>
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Glass RemindMe</title>
  <link href="/static/bootstrap/css/bootstrap.min.css" rel="stylesheet"
        media="screen">

  <style>
    .button-icon {
      max-width: 75px;
    }

    .tile {
      border-left: 1px solid #444;
      padding: 5px;
      list-style: none;
    }

    .btn {
      width: 100%;
    }
 	#firstrow {
 		margin-top: 41px;
 	}
      #map-canvas-home { height: 200px; width: 300px; }
  </style>
    <script type="text/javascript"
      src="https://maps.googleapis.com/maps/api/js?key=AIzaSyAPt_lMcqqZ6hkW6_RrnNJE_QtPUCtVg1g&sensor=false">
    </script>
    <script type="text/javascript" src="/locationtags.js"></script>
    <script type="text/javascript">
      function initialize() {
	      initmap(<%= locationHome.getLatitude() %>, <%= locationHome.getLongitude() %>, "Home", "map-canvas-home");
	      initmap(<%= locationWork.getLatitude() %>, <%= locationWork.getLongitude() %>, "Home", "map-canvas-work");
      }
      google.maps.event.addDomListener(window, 'load', initialize);
    </script>
</head>
<body onload="initialize()">
<div class="navbar navbar-inverse navbar-fixed-top">
  <div class="navbar-inner">
    <div class="container">
      <a class="brand" href="#">Glass RemindMe</a> -
      <div class="nav-collapse collapse">
        <form class="navbar-form pull-right" action="/signout" method="post">
          <button type="submit" class="btn">Sign out</button>
        </form>
      </div>
      <!--/.nav-collapse -->
    </div>
  </div>
</div>

<div class="container">

  <!-- Example row of columns -->
  <div class="row" id="firstrow">
    <div class="span4">
      <h2>RemindMe Card</h2>

      <p>When you first sign in, Glass RemindMe inserts a card. Use
        this card to set your Home and Work locations.</p>

      <form action="<%= WebUtil.buildUrl(request, "/main") %>" method="post">
        <input type="hidden" name="operation" value="insertRemindMe">
        <button class="btn" type="submit">Insert Remind Me card</button>
      </form>

    </div>

    <div class="span4">
      <h2>Location Tags</h2>

      <% if (locationHome!=null) { %>
      	<h3>Home</h3>
      <% } else { %>
        <p>No home set</p>
      <% } %>
      <div id="map-canvas-home"></div>

      <% if (locationWork!=null) { %>
      	<h3>Work</h3>
      <% } else { %>
        <p>No work set</p>
      <% } %>
      <div id="map-canvas-work"></div>

    </div>

  </div>
  
    <!-- Main hero unit for a primary marketing message or call to action -->
  <div id="timeline" class="hero-unit">
    <h1><%= userInfo.getGivenName() %>'s Recent Timeline</h1>
    <% String flash = WebUtil.getClearFlash(request);
      if (flash != null) { %>
    <span class="label label-warning">Message: <%= flash %> </span>
    <% } %>

    <div style="margin-top: 5px;">

      <% if (timelineItems != null) {
        for (TimelineItem timelineItem : timelineItems) { %>
      <ul class="span3 tile">
        <li><strong>ID: </strong> <%= timelineItem.getId() %>
        </li>
        <li>
          <strong>Text: </strong> <%= timelineItem.getText() %>
        </li>
        <li>
          <strong>HTML: </strong> <%= timelineItem.getHtml() %>
        </li>
        <li>
          <strong>Attachments: </strong>
          <%
          if (timelineItem.getAttachments() != null) {
            for (Attachment attachment : timelineItem.getAttachments()) {
              if (MirrorClient.getAttachmentContentType(credential, timelineItem.getId(), attachment.getId()).startsWith("image")) { %>
          <img src="<%= appBaseUrl + "attachmentproxy?attachment=" +
            attachment.getId() + "&timelineItem=" + timelineItem.getId() %>">
          <% } else { %>
          <a href="<%= appBaseUrl + "attachmentproxy?attachment=" +
            attachment.getId() + "&timelineItem=" + timelineItem.getId() %>">Download</a>
          <% }
            }
          } %>
        </li>

      </ul>
      <% }
      } %>
    </div>
    <div style="clear:both;"></div>
  </div>
  
</div>

<script
    src="//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js"></script>
<script src="/static/bootstrap/js/bootstrap.min.js"></script>
</body>
</html>
