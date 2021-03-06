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
<%@ page import="com.mceoin.remindme.MirrorClient" %>
<%@ page import="com.mceoin.remindme.WebUtil" %>
<%@ page import="com.mceoin.remindme.LocationUtil" %>
<%@ page import="com.mceoin.remindme.Reminder" %>
<%@ page import="com.mceoin.remindme.ReminderUtil" %>
<%@ page import="java.util.List" %>

<%@ page import="com.mceoin.remindme.MainServlet" %>

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
        
  <link href="site.css" rel="stylesheet">
  <style>
    .button-icon {
      max-width: 75px;
    }

    .tile {
      border-left: 1px solid #444;
      padding: 5px;
      list-style: none;
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
      </div>
      <!--/.nav-collapse -->
    </div>
  </div>
</div>

<div class="jumbotron masthead">
  <div class="container">
    <h1>Glass RemindMe</h1>
    <p>Get Glass to help you to remember stuff when you get home or to work.</p>
    
    <p><a href="/site" class="btn btn-primary btn-large">Login</a></p>
    
    <ul class="masthead-links">
      <li>
        <a href="http://github.com/rmceoin/glass-remindme">GitHub project</a>
      </li>
      <li>
        Version 1.0
      </li>
    </ul>
  </div>
</div>

<div class="container">

      <h2>RemindMe Card</h2>

      <p>When you first sign in, Glass RemindMe inserts a card. Use
        this card to set your Home and Work locations.
        Be sure to pin the card so it's readily accessible.</p>

      <p class="text-center"><img src="/static/images/at-home-example.png" width="640" height="360"></p>
      
      <p class="text-center"><img src="/static/images/remindme-card-example.png" width="640" height="360"></p>

      <p class="text-center"><img src="/static/images/at-work-example.png" width="640" height="360"></p>
      
      <hr>
      
      <p>Upon arrival home, your Glass will be sent the reminder you had told it earlier.</p>
      
      <p class="text-center"><img src="/static/images/home-reminder-example.png" width="640" height="360"></p>
  
	  <p>Currently the Mirror API only sends your location to Glassware every 10 minutes.  This means it can take up to 10 minutes until you receive the reminder.</p> 
</div>

    <footer class="footer">
      <div class="container">
        <p>Written by Randy McEoin <a href="//plus.google.com/100846733724962082125?prsrc=3"
   rel="publisher" target="_top" style="text-decoration:none;">
<img src="//ssl.gstatic.com/images/icons/gplus-16.png" alt="Google+" style="border:0;width:16px;height:16px;"/>
</a></p>
        <p>Code licensed under <a href="http://www.apache.org/licenses/LICENSE-2.0" target="_blank">Apache License v2.0</a>..</p>
        <p><a href="http://glyphicons.com">Glyphicons Free</a> licensed under <a href="http://creativecommons.org/licenses/by/3.0/">CC BY 3.0</a>.</p>
      </div>
    </footer>

<script src="//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js"></script>
<script src="/static/bootstrap/js/bootstrap.min.js"></script>
</body>
</html>
