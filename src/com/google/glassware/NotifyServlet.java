/*
 * Copyright (C) 2013 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.glassware;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.mirror.Mirror;
import com.google.api.services.mirror.model.Location;
import com.google.api.services.mirror.model.MenuItem;
import com.google.api.services.mirror.model.Notification;
import com.google.api.services.mirror.model.NotificationConfig;
import com.google.api.services.mirror.model.Subscription;
import com.google.api.services.mirror.model.TimelineItem;
import com.google.api.services.mirror.model.UserAction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Handles the notifications sent back from subscriptions
 * 
 * @author Jenny Murphy - http://google.com/+JennyMurphy
 */
@SuppressWarnings("serial")
public class NotifyServlet extends HttpServlet {
	private static final Logger LOG = Logger.getLogger(NotifyServlet.class.getSimpleName());

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Respond with OK and status 200 in a timely fashion to prevent
		// redelivery
		response.setContentType("text/html");
		Writer writer = response.getWriter();
		writer.append("OK");
		writer.close();

		// Get the notification object from the request body (into a string so
		// we
		// can log it)
		BufferedReader notificationReader = new BufferedReader(new InputStreamReader(request.getInputStream()));
		String notificationString = "";

		// Count the lines as a very basic way to prevent Denial of Service
		// attacks
		int lines = 0;
		while (notificationReader.ready()) {
			notificationString += notificationReader.readLine();
			lines++;

			// No notification would ever be this long. Something is very wrong.
			if (lines > 1000) {
				throw new IOException("Attempted to parse notification payload that was unexpectedly long.");
			}
		}

		LOG.info("got raw notification " + notificationString);

		JsonFactory jsonFactory = new JacksonFactory();

		// If logging the payload is not as important, use
		// jacksonFactory.fromInputStream instead.
		Notification notification = jsonFactory.fromString(notificationString, Notification.class);

		LOG.info("Got a notification with ID: " + notification.getItemId());

		// Figure out the impacted user and get their credentials for API calls
		String userId = notification.getUserToken();
		Credential credential = AuthUtil.getCredential(userId);
		Mirror mirrorClient = MirrorClient.getMirror(credential);

		if (notification.getCollection().equals("locations")) {
			LOG.info("Notification of updated location");
			Mirror glass = MirrorClient.getMirror(credential);
			// item id is usually 'latest'
			Location location = glass.locations().get(notification.getItemId()).execute();

			LOG.info("New location is " + location.getLatitude() + ", " + location.getLongitude());
//			Location previousLocation = LocationUtil.get(userId);
			LocationUtil.save(userId, location);

			List<Reminder> reminders = LocationUtil.checkTags(userId, location);
			if (reminders.size()>0) {
				// update the RemindMe card so it doesn't list the reminder(s) we just sent
				RemindMeCard.insert(userId, credential, request, false, reminders);
				// now send all the reminders
				for (Reminder reminder : reminders) {
					ReminderUtil.sendReminder(credential, reminder);
				}
			}
		} else if (notification.getCollection().equals("timeline")) {
			// Get the impacted timeline item
			TimelineItem timelineItem = mirrorClient.timeline().get(notification.getItemId()).execute();
			LOG.info("Notification impacted timeline item with ID: " + timelineItem.getId());

			if (notification.getUserActions().contains(new UserAction().setType("CUSTOM").setPayload("athome"))) {
				LOG.info("custom at home");

				Location location = LocationUtil.get(userId);
				if (location != null) {
					LOG.info("got location");
					LocationUtil.saveTag(userId, location, LocationTag.HOME, LocationTag.STATUS_AT);
					RemindMeCard.insert(userId, credential, request, false, null);
					sendMap(credential, userId, location, LocationTag.HOME);
				} else {
					LOG.info("missing location");
					sendSorryCard(credential, "", "Sorry, haven't seen your location yet.  Try again in 10 minutes.");
					checkLocationSubscription(credential, userId, request);
				}
			} else if (notification.getUserActions().contains(new UserAction().setType("CUSTOM").setPayload("atwork"))) {
				LOG.info("custom at work");

				Location location = LocationUtil.get(userId);
				if (location != null) {
					LOG.info("got location");
					LocationUtil.saveTag(userId, location, LocationTag.WORK, LocationTag.STATUS_AT);
					RemindMeCard.insert(userId, credential, request, false, null);
					sendMap(credential, userId, location, LocationTag.WORK);
				} else {
					LOG.info("missing location");
					sendSorryCard(credential, "", "Sorry, haven't seen your location yet.  Try again in 10 minutes.");
					checkLocationSubscription(credential, userId, request);
				}
			} else if (notification.getUserActions().contains(new UserAction().setType("CUSTOM").setPayload("showhome"))) {
				LocationTag locationTag = LocationUtil.getTag(userId, LocationTag.HOME);
				if (locationTag != null) {
					LOG.info("show home got location");
					sendMap(credential, userId, locationTag.getLocation(), locationTag.getTag());
				}
			} else if (notification.getUserActions().contains(new UserAction().setType("CUSTOM").setPayload("showwork"))) {
				LocationTag locationTag = LocationUtil.getTag(userId, LocationTag.WORK);
				if (locationTag != null) {
					LOG.info("show work got location");
					sendMap(credential, userId, locationTag.getLocation(), locationTag.getTag());
				}
			} else if (notification.getUserActions().contains(new UserAction().setType("REPLY"))) {
				LOG.info("got a REPLY: " + timelineItem.getText());
				if (!ReplyParser.parse(userId, timelineItem.getText())) {
					sendSorryCard(credential, "Sorry, did not understand:", timelineItem.getText());
				} else {
					RemindMeCard.insert(userId, credential, request, true, null);
				}
			} else {
				LOG.warning("I don't know what to do with this notification, so I'm ignoring it." + notification.getUserActions());
			}
		}
	}

	private void checkLocationSubscription(Credential credential, String userId, HttpServletRequest req) {
		boolean locationSubscriptionExists = false;

		// Mirror glass = MirrorClient.getMirror(credential);
		List<Subscription> subscriptions;
		try {
			subscriptions = MirrorClient.listSubscriptions(credential).getItems();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		LOG.info("subscriptions = " + subscriptions);
		if (subscriptions != null) {
			for (Subscription subscription : subscriptions) {
				if (subscription.getId().equals("locations")) {
					locationSubscriptionExists = true;
				}
			}
		}
		if (locationSubscriptionExists == false) {
			LOG.info("need to add a subscription to location");
			try {
				MirrorClient.insertSubscription(credential, WebUtil.buildUrl(req, "/notify"), userId, "locations");
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	private void sendSorryCard(Credential credential, String headline, String message) throws IOException {
		TimelineItem timelineItem = new TimelineItem();

		StringBuilder builder = new StringBuilder();
		builder.append("<article>\n");
		builder.append("<section>\n");
		builder.append("<p>" + headline + "</p>");
		builder.append("<p>" + message + "</p>");
		builder.append("</section>\n");
		builder.append("<footer>");
		builder.append("<div>");
		builder.append(MainServlet.CONTACT_NAME);
		builder.append("</div>");
		builder.append("</footer>\n");
		builder.append("</article>");

		timelineItem.setHtml(builder.toString());
		timelineItem.setNotification(new NotificationConfig().setLevel("DEFAULT"));

		List<MenuItem> menuItemList = new ArrayList<MenuItem>();
		menuItemList.add(new MenuItem().setAction("REPLY"));
		timelineItem.setMenuItems(menuItemList);
		
		MirrorClient.insertTimelineItem(credential, timelineItem);
	}

	private void sendMap(Credential credential, String userId, Location location, String name) throws IOException {

		TimelineItem locationMap = new TimelineItem();

		StringBuilder builder = new StringBuilder();
		builder.append("<article>\n");
		builder.append("<figure>\n");
		builder.append("<img src=\"glass://map?w=240&h=360&marker=0;");
		builder.append(location.getLatitude());
		builder.append(",");
		builder.append(location.getLongitude());
		builder.append("\" height=\"360\" width=\"240\">");
		builder.append("</figure>\n");
		builder.append("<section>\n");
		builder.append("<div class=\"text-auto-size\">");
		builder.append(name);
		builder.append("</div>\n");
		builder.append("</section>\n");
		builder.append("<footer>");
		builder.append("<div>");
		builder.append(MainServlet.CONTACT_NAME);
		builder.append("</div>");
		builder.append("</footer>\n");
		builder.append("</article>");
		locationMap.setHtml(builder.toString());
		LOG.info("html=" + builder.toString());
		locationMap.setTitle(name);
		locationMap.setNotification(new NotificationConfig().setLevel("DEFAULT"));
		MirrorClient.insertTimelineItem(credential, locationMap);
	}
}
