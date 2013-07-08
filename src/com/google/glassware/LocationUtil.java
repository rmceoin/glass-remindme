package com.google.glassware;

import com.google.api.services.mirror.model.Location;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

public class LocationUtil {
	private static final Logger LOG = Logger.getLogger(MainServlet.class.getSimpleName());

	private static final String KIND = LocationUtil.class.getName();
	private static final String LOCATION_CURRENT = KIND + ".current";
	private static final String LOCATION_TAGS = KIND + ".tags";

	// meters surrounding a tag location that is considered within the tag
	public static final int TAG_RADIUS = 100;

	/**
	 * Save the glass wearer's current location.
	 * 
	 * @param userId
	 * @param location
	 */
	public static void save(String userId, Location location) {

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Entity entity = new Entity(LOCATION_CURRENT, userId);
		entity.setProperty("userId", userId);
		entity.setProperty("latitude", location.getLatitude());
		entity.setProperty("longitude", location.getLongitude());
		Date date = new Date();
		entity.setProperty("date", date); // GMT

		datastore.put(entity);
		LOG.info("Saved location for " + userId);
	}

	public static Location get(String userId) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Key key = KeyFactory.createKey(LOCATION_CURRENT, userId);
		try {
			Entity entity = datastore.get(key);
			Date storedDate = (Date) entity.getProperty("date");

			LOG.info("storedDate " + storedDate);
			Long storedTime = storedDate.getTime();
			Date now = new Date();
			Long currentTime = now.getTime();
			// determine how old the stored entry is by seconds
			Long timeDifference = (currentTime - storedTime) / 1000;
			LOG.info("timeDifference = " + Long.valueOf(timeDifference));

			if (timeDifference > 60 * 15) {
				// if older than 15 minutes than ignore old location data
				return null;
			}
			Location location = new Location();
			location.setLatitude((Double) entity.getProperty("latitude"));
			location.setLongitude((Double) entity.getProperty("longitude"));
			return location;
		} catch (EntityNotFoundException e) {
			return null;
		}
	}

	public static void saveTag(String userId, Location location, String tag, String status) {

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Entity entity = new Entity(LOCATION_TAGS);

		Filter userIdFilter = new FilterPredicate("userId", FilterOperator.EQUAL, userId);
		Filter tagFilter = new FilterPredicate("tag", FilterOperator.EQUAL, tag);
		Query tagQuery = new Query(LOCATION_TAGS).setFilter(userIdFilter).setFilter(tagFilter);
		List<Entity> tagEntities = datastore.prepare(tagQuery).asList(FetchOptions.Builder.withLimit(1));

		if (tagEntities.size()>0) {
			entity = tagEntities.get(0);
		}
		
		entity.setProperty("userId", userId);
		entity.setProperty("latitude", location.getLatitude());
		entity.setProperty("longitude", location.getLongitude());
		Date date = new Date();
		entity.setProperty("date", date); // GMT
		entity.setProperty("tag", tag);
		entity.setProperty("status", status);

		datastore.put(entity);
		LOG.info("Saved location for " + userId + " tag " + tag);
	}

	public static LocationTag getTag(String userId, String tag) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		Filter userIdFilter = new FilterPredicate("userId", FilterOperator.EQUAL, userId);
		Filter tagFilter = new FilterPredicate("tag", FilterOperator.EQUAL, tag);
		Query tagQuery = new Query(LOCATION_TAGS).setFilter(userIdFilter).setFilter(tagFilter);
		List<Entity> tagEntities = datastore.prepare(tagQuery).asList(FetchOptions.Builder.withLimit(1));

		if (tagEntities.size()>0) {
			Entity entity = tagEntities.get(0);

			Location location = new Location();
			location.setLatitude((Double) entity.getProperty("latitude"));
			location.setLongitude((Double) entity.getProperty("longitude"));

			LocationTag locationTag = new LocationTag();
			locationTag.setUserId(userId);
			locationTag.setLocation(location);
			locationTag.setTag((String) entity.getProperty("tag"));
			locationTag.setStatus((String) entity.getProperty("status"));
			return locationTag;
		} else {
			return null;
		}

	}

	public static List<LocationTag> getAllTags(String userId) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		Filter userIdFilter = new FilterPredicate("userId", FilterOperator.EQUAL, userId);
		Query tagQuery = new Query(LOCATION_TAGS).setFilter(userIdFilter);
		Iterable<Entity> userEntities = datastore.prepare(tagQuery).asIterable();

		List<LocationTag> tags = new ArrayList<LocationTag>();
		for (Entity tagEntity : userEntities) {
			LOG.info("found: " + userId + " " + tagEntity.getProperty("tag"));
			Location location = new Location();
			location.setLatitude((Double) tagEntity.getProperty("latitude"));
			location.setLongitude((Double) tagEntity.getProperty("longitude"));

			LocationTag tag = new LocationTag();
			tag.setUserId(userId);
			tag.setLocation(location);
			tag.setTag((String) tagEntity.getProperty("tag"));
			tag.setStatus((String) tagEntity.getProperty("status"));

			tags.add(tag);
		}
		return tags;
	}

	public static double distanceBetweenLocations(Location location1, Location location2) {
		LatLng point1 = new LatLng(location1.getLatitude(), location1.getLongitude());
		LatLng point2 = new LatLng(location2.getLatitude(), location2.getLongitude());
		double distanceInMeters = LatLngTool.distance(point1, point2, LengthUnit.METER);
		return distanceInMeters;
	}

	public static LocationTag enterTag(String userId, Location previous, Location current) {
		if ((previous == null) || (current == null)) {
			LOG.info("previous or current was null");
			return null;
		}
		List<LocationTag> locationTags = getAllTags(userId);
		if (locationTags == null) {
			// no tags for this user
			LOG.info("no tags for this user");
			return null;
		}
		// if (distanceBetweenLocations(previous, current) < 100) {
		// LOG.info("not enough distance between previous and current");
		// return null;
		// }
		LOG.info("size of locationTags=" + locationTags.size());
		LocationTag foundLocationTag = null;
		for (LocationTag locationTag : locationTags) {

			double tagDistanceFromCurrent = distanceBetweenLocations(locationTag.getLocation(), current);
			String status = locationTag.getStatus();
			LOG.info("tag=" + locationTag.getTag() + " distance=" + tagDistanceFromCurrent + " status=" + locationTag.getStatus());
			if ((tagDistanceFromCurrent <= TAG_RADIUS) && ((status == null) || (!status.contentEquals(LocationTag.STATUS_AT)))) {
				// We are at the tag location and were previously away from it
				LOG.info("arrived at " + locationTag.getTag());
				saveTag(userId, locationTag.getLocation(), locationTag.getTag(), LocationTag.STATUS_AT);
				foundLocationTag = locationTag;
			} else if ((tagDistanceFromCurrent > TAG_RADIUS) && ((status == null) || (!status.contentEquals(LocationTag.STATUS_AWAY)))) {
				// We are away from the tag location and were previously at it
				// ... we have left the tag location
				LOG.info("left " + locationTag.getTag());
				saveTag(userId, locationTag.getLocation(), locationTag.getTag(), LocationTag.STATUS_AWAY);
			}
			/*
			 * double
			 * tagDistanceFromPrevious=distanceBetweenLocations(locationTag
			 * .getLocation(), previous); LOG.info("previous from " +
			 * locationTag.getTag() + ": " + tagDistanceFromPrevious);
			 * LOG.info("current from " + locationTag.getTag() + ": " +
			 * tagDistanceFromCurrent); if ((tagDistanceFromPrevious >
			 * TAG_RADIUS) && (tagDistanceFromCurrent <= TAG_RADIUS)) {
			 * LOG.info("matched tag location: " + locationTag.getTag()); return
			 * locationTag; }
			 */
		}
		if (foundLocationTag == null) {
			LOG.info("no location found");
		}
		return foundLocationTag;
	}
}
