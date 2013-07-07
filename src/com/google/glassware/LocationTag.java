package com.google.glassware;

import com.google.api.services.mirror.model.Location;

public class LocationTag {

	public static final String STATUS_AWAY = "away";
	public static final String STATUS_AT = "at";

	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public Location getLocation() {
		return location;
	}
	public void setLocation(Location location) {
		this.location = location;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	private String userId;
	private Location location;
	private String tag;
	private String status;
}
