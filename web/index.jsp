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
<%@ page import="com.google.glassware.Reminder" %>
<%@ page import="com.google.glassware.ReminderUtil" %>
<%@ page import="java.util.List" %>

<%@ page import="com.google.glassware.MainServlet" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<!doctype html>
<%
  String appBaseUrl = WebUtil.buildUrl(request, "/");
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
  </style>
</head>
<body onload="initialize()">
<div class="navbar navbar-inverse navbar-fixed-top">
  <div class="navbar-inner">
    <div class="container">
      <a class="brand" href="#">Glass RemindMe</a>
      <div class="nav-collapse collapse">
        <form class="navbar-form pull-right" action="/site" method="post">
          <button type="submit" class="btn">Login</button>
        </form>
      </div>
      <!--/.nav-collapse -->
    </div>
  </div>
</div>

<div class="container">

  <!-- Example row of columns -->
  <div class="row" id="firstrow">
    <div class="span8">
      <h2>RemindMe Card</h2>

      <p>When you first sign in, Glass RemindMe inserts a card. Use
        this card to set your Home and Work locations.
        Be sure to pin the card so it's readily accessible.</p>

      <a href="/site">Login</a>
    </div>

  </div>
  
    <!-- Main hero unit for a primary marketing message or call to action -->
  <div id="timeline" class="hero-unit">
    <h1>Recent Timeline</h1>
    <div style="clear:both;"></div>
  </div>
  
</div>

<script
    src="//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js"></script>
<script src="/static/bootstrap/js/bootstrap.min.js"></script>
</body>
</html>
