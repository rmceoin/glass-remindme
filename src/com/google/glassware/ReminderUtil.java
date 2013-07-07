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

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.mirror.model.Location;
import com.google.api.services.mirror.model.NotificationConfig;
import com.google.api.services.mirror.model.TimelineItem;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

public class ReminderUtil {
	private static final Logger LOG = Logger.getLogger(MainServlet.class.getSimpleName());

	private static final String KIND = ReminderUtil.class.getName();
	private static final String REMINDERS = KIND + ".reminders";

	public static void saveReminder(String userId, String tag, String reminder) {

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Entity entity = new Entity(REMINDERS, userId + tag);
		entity.setProperty("userId", userId);
		Date date = new Date();
		entity.setProperty("created", date); // GMT
		entity.setProperty("tag", tag);
		entity.setProperty("reminder", reminder);

		datastore.put(entity);
		LOG.info("Saved reminder for " + userId + " tag " + tag);
	}

	public static List<Reminder> getAllReminders(String userId, String tag) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		Filter userIdFilter = new FilterPredicate("userId", FilterOperator.EQUAL, userId);
		Filter tagFilter = new FilterPredicate("tag", FilterOperator.EQUAL, tag);
		Query reminderQuery = new Query(REMINDERS).setFilter(userIdFilter).setFilter(tagFilter);
		Iterable<Entity> reminderEntities = datastore.prepare(reminderQuery).asIterable();

		List<Reminder> reminders = new ArrayList<Reminder>();
		for (Entity reminderEntity : reminderEntities) {
			LOG.info("found: " + userId + " " + reminderEntity.getProperty("tag"));
			Reminder reminder = new Reminder();
			reminder.setUserId(userId);
			reminder.setTag(tag);
			reminder.setCreated((Date) reminderEntity.getProperty("created"));
			reminder.setReminder((String) reminderEntity.getProperty("reminder"));

			reminders.add(reminder);
		}
		return reminders;
	}

	public static List<Reminder> getAllReminders(String userId) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		Filter userIdFilter = new FilterPredicate("userId", FilterOperator.EQUAL, userId);
		Query reminderQuery = new Query(REMINDERS).setFilter(userIdFilter);
		Iterable<Entity> reminderEntities = datastore.prepare(reminderQuery).asIterable();

		List<Reminder> reminders = new ArrayList<Reminder>();
		for (Entity reminderEntity : reminderEntities) {
			LOG.info("found: " + userId + " " + reminderEntity.getProperty("tag"));
			Reminder reminder = new Reminder();
			reminder.setUserId(userId);
			reminder.setTag((String) reminderEntity.getProperty("tag"));
			reminder.setCreated((Date) reminderEntity.getProperty("created"));
			reminder.setReminder((String) reminderEntity.getProperty("reminder"));

			reminders.add(reminder);
		}
		return reminders;
	}
	
	public static void sendReminder(Credential credential, Reminder reminder, Location location) throws IOException {
		// send the reminder
		TimelineItem reminderItem = new TimelineItem();
		reminderItem.setTitle(MainServlet.CONTACT_NAME);

		StringBuilder builder = new StringBuilder();
		builder.append("<article>");
		builder.append("<section>\n");
		builder.append("<p class=\"text-auto-size\">");
		builder.append("At <b>"+reminder.getTag()+"</b>, remember to <b>"+reminder.getReminder()+"</b>.");
		builder.append("</p>\n");
		builder.append("</section>\n");
		builder.append("<footer>");
		builder.append("<div>");
		builder.append(MainServlet.CONTACT_NAME);
		builder.append("</div>");
		builder.append("</footer>\n");
		builder.append("</article>");

		reminderItem.setHtml(builder.toString());
		
		reminderItem.setNotification(new NotificationConfig().setLevel("DEFAULT"));
		MirrorClient.insertTimelineItem(credential, reminderItem);
	}
}
