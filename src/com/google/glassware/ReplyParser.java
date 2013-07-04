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
	private static final Logger LOG = Logger.getLogger(MainServlet.class.getSimpleName());

	public static void parse(String userId, String reply) {
		Pattern pattern = Pattern.compile("^remind me to (.*) at ([a-z]+)$");
		Matcher matcher = pattern.matcher(reply);
		if (matcher.find()) {
			String action = matcher.group(1);
			String tag = matcher.group(2);
			LOG.info("matched: " + action + " at " + tag);
			ReminderUtil.saveReminder(userId, tag, action);
		}
	}
}
