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
<%@ page import="com.mceoin.remindme.MirrorClient" %>
<%@ page import="com.mceoin.remindme.WebUtil" %>
<%@ page import="com.mceoin.remindme.LocationUtil" %>
<%@ page import="com.mceoin.remindme.LocationTag" %>
<%@ page import="com.mceoin.remindme.Reminder" %>
<%@ page import="com.mceoin.remindme.ReminderUtil" %>
<%@ page import="com.mceoin.remindme.MainServlet" %>
<%@ page import="java.util.List" %>
<%@ page import="com.google.api.services.mirror.model.Contact" %>
<%@ page import="com.google.api.services.mirror.model.TimelineItem" %>
<%@ page import="com.google.api.services.mirror.model.Subscription" %>
<%@ page import="com.google.api.services.mirror.model.Attachment" %>
<%@ page import="com.google.api.services.mirror.model.Location" %>
<%@ page import="com.google.api.services.oauth2.model.Userinfo" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<!doctype html>
<%
  String userId = com.mceoin.remindme.AuthUtil.getUserId(request);
  String appBaseUrl = WebUtil.buildUrl(request, "/");

  Credential credential = com.mceoin.remindme.AuthUtil.getCredential(userId);

  LocationTag locationHome = LocationUtil.getTag(userId, LocationTag.HOME);
  LocationTag locationWork = LocationUtil.getTag(userId, LocationTag.WORK);


%>
<html>
<head>
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Glass RemindMe</title>
  <link href="/static/bootstrap/css/bootstrap.min.css" rel="stylesheet"
        media="screen">
  <link href="/site.css" rel="stylesheet">
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
    #map-canvas-home { height: 300px; width: 400px; }
    /* need the img and label hack due to conflict between bootstrap and maps */
    #map-canvas-home img { max-width: none; }
    #map-canvas-home label { width: auto; display:inline; }
    #map-canvas-work { height: 300px; width: 400px; }
    #map-canvas-work img { max-width: none; }
    #map-canvas-work label { width: auto; display:inline; }
  </style>
    <script type="text/javascript"
      src="https://maps.googleapis.com/maps/api/js?key=AIzaSyAPt_lMcqqZ6hkW6_RrnNJE_QtPUCtVg1g&sensor=false">
    </script>
    <script type="text/javascript" src="/locationtags.js"></script>
    <script type="text/javascript">
      var markerhome;
      var markerwork;
      function initialize() {
      		console.log("initialize");
      <% if (locationHome!=null) { %>
	      markerhome=initmap(<%= locationHome.getLocation().getLatitude() %>, <%= locationHome.getLocation().getLongitude() %>, "<%= locationHome.getTag() %>", "map-canvas-home");
	      $( "#editmarkerhome" )
	      	.button()
      		.click(function() {
      		console.log("clicked");
	        	markerEditable(markerhome, true)
    	  	})
	  <% }
	  	 if (locationWork!=null) { %>
	      markerwork=initmap(<%= locationWork.getLocation().getLatitude() %>, <%= locationWork.getLocation().getLongitude() %>, "<%= locationWork.getTag() %>", "map-canvas-work");
	  <% } %>
      }
      google.maps.event.addDomListener(window, 'load', initialize);
    </script>
</head>
<body>
<div class="navbar navbar-inverse navbar-fixed-top">
  <div class="navbar-inner">
    <div class="container">
      <a class="brand" href="#">Glass RemindMe</a>
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
    <div class="span7">
      <h2>RemindMe Card</h2>

      <p>When you first sign in, Glass RemindMe inserts a card. Use
        this card to set your Home and Work locations.
        Be sure to pin the card so it's readily accessible.</p>

      <form action="<%= WebUtil.buildUrl(request, "/main") %>" method="post">
        <p class="text-center"><input type="hidden" name="operation" value="insertRemindMe">
        <button class="btn" type="submit">Insert Remind Me card</button></p>
      </form>
      
      <p>To set a reminder, use the card on Glass and REPLY to it by saying a 
      sentence in the following format:</p>
      
      <pre>remind me to "do something" at [home]
remind me to "do something" when i get [home]
remind me to "do something" when i get to [work]</pre>

      <pre>remind me to "do something" when i leave [work]</pre>

	  <p>Currently the Mirror API only sends your location to Glassware every 10 minutes.  This means it can take up to 10 minutes until you receive the reminder.</p>
	  
	  <h2>Reminders</h2>
	  
	  <table class="table">
		<tr>
			<th> Reminder </th>
			<th></th>
			<th> Tag </th>
			<th></th>
			<th> Created </th>
		</tr>
	  <%
		List<Reminder> reminders=ReminderUtil.getAllReminders(userId);
		if (reminders!=null) {
			for (Reminder reminder : reminders) { %>
			<tr>
				<td> <%= reminder.getReminder() %> </td>
				<td>
				<% if (reminder.getDirection().contentEquals(Reminder.DIRECTION_ARRIVE)) { %>
					<i class="icon-arrow-right"></i>
				<% } %>
				</td>
				<td> <%= reminder.getTag() %> </td>
				<td>
				<% if (reminder.getDirection().contentEquals(Reminder.DIRECTION_DEPART)) { %>
					<i class="icon-arrow-right"></i>
				<% } %>
				</td>
				<td> <%= reminder.getCreated() %> </td>
			</tr>
		<%	}
		}
	  %>
	  </table>
    </div>

    <div class="span5">
      <h2>Location Tags</h2>

	   	<h3>Home</h3>
      <% if (locationHome==null) { %>
        <p>No home set</p>
      <% } else { %>
        <button id="editmarkerhome">Edit</button>
        <div id="map-canvas-home"></div>
      <% } %>
      	<h3>Work</h3>
      <% if (locationWork==null) { %>
        <p>No work set</p>
      <% } else { %>
        <div id="map-canvas-work"></div>
      <% } %>

    </div>

  </div>
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

<script
    src="//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js"></script>
<script src="/static/bootstrap/js/bootstrap.min.js"></script>
</body>
</html>
