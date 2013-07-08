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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.logging.Logger;

public class ReplyParser {
	private static final Logger LOG = Logger.getLogger(ReplyParser.class.getSimpleName());

	/**
	 * @param userId
	 * @param reply
	 * @return true if matched the reply string
	 */
	public static boolean parse(String userId, String reply) {
		// remind me to [do something] at [home]
		// remind me to [do something] when i get [home]
		// remind me to [do something] when i get to [work]
		Pattern pattern = Pattern.compile("^remind me to (.*) (at|when i get|when i get to) ([a-z]+)$", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(reply);
		if (matcher.find()) {
			String reminder = matcher.group(1);
			String middle = matcher.group(2);
			String tag = matcher.group(3);
			LOG.info("matched: " + reminder + " - " + middle + " - " + tag);
			ReminderUtil.saveReminder(userId, tag, reminder, Reminder.DIRECTION_ARRIVE);
			return true;
		}
		pattern = Pattern.compile("^remind me to (.*) (when i leave) ([a-z]+)$", Pattern.CASE_INSENSITIVE);
		matcher = pattern.matcher(reply);
		if (matcher.find()) {
			String reminder = matcher.group(1);
			String middle = matcher.group(2);
			String tag = matcher.group(3);
			LOG.info("matched: " + reminder + " - " + middle + " - " + tag);
			ReminderUtil.saveReminder(userId, tag, reminder, Reminder.DIRECTION_DEPART);
			return true;
		}
		return false;
	}
}
