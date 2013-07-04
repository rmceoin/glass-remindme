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
import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpHeaders;
import com.google.api.services.mirror.model.Contact;
import com.google.api.services.mirror.model.MenuItem;
import com.google.api.services.mirror.model.MenuValue;
import com.google.api.services.mirror.model.NotificationConfig;
import com.google.api.services.mirror.model.TimelineItem;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Handles POST requests from index.jsp
 * 
 * @author Jenny Murphy - http://google.com/+JennyMurphy
 */
@SuppressWarnings("serial")
public class MainServlet extends HttpServlet {
	@SuppressWarnings("unused")
	private static final String KIND = MainServlet.class.getName();

	/**
	 * Private class to process batch request results.
	 * 
	 * For more information, see
	 * https://code.google.com/p/google-api-java-client/wiki/Batch.
	 */
	private final class BatchCallback extends JsonBatchCallback<TimelineItem> {
		private int success = 0;
		private int failure = 0;

		@Override
		public void onSuccess(TimelineItem item, HttpHeaders headers)
				throws IOException {
			++success;
		}

		@Override
		public void onFailure(GoogleJsonError error, HttpHeaders headers)
				throws IOException {
			++failure;
			LOG.info("Failed to insert item: " + error.getMessage());
		}
	}

	private static final Logger LOG = Logger.getLogger(MainServlet.class
			.getSimpleName());
	public static final String CONTACT_NAME = "Glass RemindMe";

	/**
	 * Do stuff when buttons on index.jsp are clicked
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res)
			throws IOException {

		String userId = AuthUtil.getUserId(req);
		Credential credential = AuthUtil.newAuthorizationCodeFlow()
				.loadCredential(userId);
		String message = "";

		if (req.getParameter("operation").equals("insertSubscription")) {

			// subscribe (only works deployed to production)
			try {
				MirrorClient.insertSubscription(credential,
						WebUtil.buildUrl(req, "/notify"), userId,
						req.getParameter("collection"));
				message = "Application is now subscribed to updates.";
			} catch (GoogleJsonResponseException e) {
				LOG.warning("Could not subscribe "
						+ WebUtil.buildUrl(req, "/notify") + " because "
						+ e.getDetails().toPrettyString());
				message = "Failed to subscribe. Check your log for details";
			}

		} else if (req.getParameter("operation").equals("deleteSubscription")) {

			// subscribe (only works deployed to production)
			MirrorClient.deleteSubscription(credential,
					req.getParameter("subscriptionId"));

			message = "Application has been unsubscribed.";

		} else if (req.getParameter("operation").equals("insertRemindMe")) {
			LOG.fine("Inserting Remind Me");

			TimelineItem timelineItem = new TimelineItem();
			timelineItem.setTitle(CONTACT_NAME);
			timelineItem.setText("Remind Me");

			List<MenuItem> menuItemList = new ArrayList<MenuItem>();
			menuItemList.add(new MenuItem().setAction("REPLY"));

			List<MenuValue> menuAtHomeValues = new ArrayList<MenuValue>();
			menuAtHomeValues.add(new MenuValue().setIconUrl(
					WebUtil.buildUrl(req, "/static/images/1-Normal-Home-icon.png"))
					.setDisplayName("At Home"));
			menuItemList.add(new MenuItem().setValues(menuAtHomeValues)
					.setId("athome").setAction("CUSTOM"));

			List<MenuValue> menuAtWorkValues = new ArrayList<MenuValue>();
			menuAtWorkValues.add(new MenuValue().setIconUrl(
					WebUtil.buildUrl(req, "/static/images/Briefcase.png"))
					.setDisplayName("At Work"));
			menuItemList.add(new MenuItem().setValues(menuAtWorkValues)
					.setId("atwork").setAction("CUSTOM"));

			List<MenuValue> menuShowHomeValues = new ArrayList<MenuValue>();
			menuShowHomeValues.add(new MenuValue().setIconUrl(
					WebUtil.buildUrl(req, "/static/images/1-Normal-Home-icon.png"))
					.setDisplayName("Show Home"));
			menuItemList.add(new MenuItem().setValues(menuShowHomeValues)
					.setId("showhome").setAction("CUSTOM"));

			List<MenuValue> menuShowWorkValues = new ArrayList<MenuValue>();
			menuShowWorkValues.add(new MenuValue().setIconUrl(
					WebUtil.buildUrl(req, "/static/images/Briefcase.png"))
					.setDisplayName("Show Work"));
			menuItemList.add(new MenuItem().setValues(menuShowWorkValues)
					.setId("showwork").setAction("CUSTOM"));

			menuItemList.add(new MenuItem().setAction("TOGGLE_PINNED"));
			menuItemList.add(new MenuItem().setAction("DELETE"));

			timelineItem.setMenuItems(menuItemList);
			timelineItem.setNotification(new NotificationConfig()
					.setLevel("DEFAULT"));

			MirrorClient.insertTimelineItem(credential, timelineItem);

			message = "Insert a Set Location card.";

		} else if (req.getParameter("operation").equals("insertContact")) {
			if (req.getParameter("iconUrl") == null
					|| req.getParameter("name") == null) {
				message = "Must specify iconUrl and name to insert contact";
			} else {
				// Insert a contact
				LOG.fine("Inserting contact Item");
				Contact contact = new Contact();
				contact.setId(req.getParameter("name"));
				contact.setDisplayName(req.getParameter("name"));
				contact.setImageUrls(Lists.newArrayList(req
						.getParameter("iconUrl")));
				MirrorClient.insertContact(credential, contact);

				message = "Inserted contact: " + req.getParameter("name");
			}

		} else if (req.getParameter("operation").equals("deleteContact")) {

			// Insert a contact
			LOG.fine("Deleting contact Item");
			MirrorClient.deleteContact(credential, req.getParameter("id"));

			message = "Contact has been deleted.";

		} else if (req.getParameter("operation").equals("insertItemAllUsers")) {
			if (req.getServerName().contains(
					"glass-java-starter-demo.appspot.com")) {
				message = "This function is disabled on the demo instance.";
			}

			// Insert a contact
			List<String> users = AuthUtil.getAllUserIds();
			LOG.info("found " + users.size() + " users");
			if (users.size() > 10) {
				// We wouldn't want you to run out of quota on your first day!
				message = "Total user count is " + users.size()
						+ ". Aborting broadcast " + "to save your quota.";
			} else {
				TimelineItem allUsersItem = new TimelineItem();
				allUsersItem.setText("Hello Everyone!");
				allUsersItem.setCanonicalUrl("http://hello.com/");

				BatchRequest batch = MirrorClient.getMirror(null).batch();
				BatchCallback callback = new BatchCallback();

				// TODO: add a picture of a cat
				for (String user : users) {
					Credential userCredential = AuthUtil.getCredential(user);
					MirrorClient.getMirror(userCredential).timeline()
							.insert(allUsersItem).queue(batch, callback);
				}

				batch.execute();
				message = "Successfully sent cards to " + callback.success
						+ " users (" + callback.failure + " failed).";
			}

		} else {
			String operation = req.getParameter("operation");
			LOG.warning("Unknown operation specified " + operation);
			message = "I don't know how to do that";
		}
		WebUtil.setFlash(req, message);
		res.sendRedirect(WebUtil.buildUrl(req, "/"));
	}

}
