/*
 * Copyright (C) 2013 Randy McEoin
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.mirror.model.MenuItem;
import com.google.api.services.mirror.model.MenuValue;
import com.google.api.services.mirror.model.NotificationConfig;
import com.google.api.services.mirror.model.TimelineItem;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public class RemindMeCard {
	private static final Logger LOG = Logger.getLogger(MainServlet.class.getSimpleName());

	private static final String KIND = RemindMeCard.class.getName();
	private static final String REMINDMECARDS = KIND + ".cards";
	
	public static void insert(String userId, Credential credential, HttpServletRequest req) throws IOException {
		
		String oldCardId=getCardId(userId);
		if (oldCardId!=null) {
			LOG.info("found old card: "+oldCardId);
			List<TimelineItem> timelineItems = MirrorClient.listItems(credential, 200L).getItems();
			LOG.info("timeline has "+timelineItems.size());
			for (TimelineItem timelineItem : timelineItems) {
				LOG.info(" id "+timelineItem.getId());
				if (timelineItem.getId().equals(oldCardId)) {
					// found the old card is still in the timeline
					timelineItem.setIsPinned(true);
					timelineItem.setHtml(cardHTML(userId));
					TimelineItem cardUpdated=MirrorClient.updateTimelineItem(credential, oldCardId, timelineItem);
					LOG.info("found old card and updated it: "+timelineItem+" "+cardUpdated);
					return;
				}
			}
		}
		TimelineItem timelineItem = new TimelineItem();
		timelineItem.setTitle(MainServlet.CONTACT_NAME);
		timelineItem.setHtml(cardHTML(userId));

		List<MenuItem> menuItemList = new ArrayList<MenuItem>();
		menuItemList.add(new MenuItem().setAction("REPLY"));

		List<MenuValue> menuAtHomeValues = new ArrayList<MenuValue>();
		menuAtHomeValues.add(new MenuValue().setIconUrl(WebUtil.buildUrl(req, "/static/images/1-Normal-Home-icon.png")).setDisplayName("At Home"));
		menuItemList.add(new MenuItem().setValues(menuAtHomeValues).setId("athome").setAction("CUSTOM"));

		List<MenuValue> menuAtWorkValues = new ArrayList<MenuValue>();
		menuAtWorkValues.add(new MenuValue().setIconUrl(WebUtil.buildUrl(req, "/static/images/Briefcase.png")).setDisplayName("At Work"));
		menuItemList.add(new MenuItem().setValues(menuAtWorkValues).setId("atwork").setAction("CUSTOM"));

		List<MenuValue> menuShowHomeValues = new ArrayList<MenuValue>();
		menuShowHomeValues.add(new MenuValue().setIconUrl(WebUtil.buildUrl(req, "/static/images/1-Normal-Home-icon.png")).setDisplayName("Show Home"));
		menuItemList.add(new MenuItem().setValues(menuShowHomeValues).setId("showhome").setAction("CUSTOM"));

		List<MenuValue> menuShowWorkValues = new ArrayList<MenuValue>();
		menuShowWorkValues.add(new MenuValue().setIconUrl(WebUtil.buildUrl(req, "/static/images/Briefcase.png")).setDisplayName("Show Work"));
		menuItemList.add(new MenuItem().setValues(menuShowWorkValues).setId("showwork").setAction("CUSTOM"));

		menuItemList.add(new MenuItem().setAction("TOGGLE_PINNED"));
		menuItemList.add(new MenuItem().setAction("DELETE"));

		timelineItem.setMenuItems(menuItemList);
		timelineItem.setNotification(new NotificationConfig().setLevel("DEFAULT"));
		timelineItem.setIsPinned(true);

		TimelineItem cardInserted=MirrorClient.insertTimelineItem(credential, timelineItem);
		saveCard(userId, cardInserted);
	}

	private static String cardHTML(String userId) {
		StringBuilder builder = new StringBuilder();
		builder.append("<article>");
		builder.append("<section>\n");
		
		List<LocationTag> locationTags = LocationUtil.getAllTags(userId);
		if ((locationTags != null) && (locationTags.size()>0)) {

			List<Reminder> reminders=ReminderUtil.getAllReminders(userId);
			if ((reminders!=null) && (reminders.size()>0)) {
				builder.append("<table>");
				for (Reminder reminder : reminders) {
					builder.append("<tr>");
					builder.append("<td>"+reminder.getTag()+"</td>");
					builder.append("<td>"+reminder.getReminder()+"</td>");
					builder.append("<tr>\n");
				}
				builder.append("</table>");
			} else {
				builder.append("<p>Reply to this card with a command like this:</p>");
				builder.append("<p>remind me to [do something] at [home]</p>");
			}
			
		} else {
			builder.append("<p>Use the 'At Home' and 'At Work' menu commands to set a location.</p>");
		}
			

		builder.append("</section>\n");
		builder.append("<footer>");
		builder.append("<div>");
		builder.append(MainServlet.CONTACT_NAME);
		builder.append("</div>");
		builder.append("</footer>\n");
		builder.append("</article>");
		
		return builder.toString();
	}
	public static void saveCard(String userId, TimelineItem card) {

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Entity entity = new Entity(REMINDMECARDS, userId);
		entity.setProperty("userId", userId);
		entity.setProperty("cardId", card.getId());
		Date date = new Date();
		entity.setProperty("date", date); // GMT

		datastore.put(entity);
		LOG.info("Saved cardId for " + userId);
	}

	public static String getCardId(String userId) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Key key = KeyFactory.createKey(REMINDMECARDS, userId);
		try {
			Entity entity = datastore.get(key);
			String cardId = (String) entity.getProperty("cardId");

			return cardId;
		} catch (EntityNotFoundException e) {
			return null;
		}
	}
}
