package edu.berkeley.eecs.e_mission;

import java.io.Serializable;

public class UserClassification implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UserClassification(String tripId, String sectionId, String userMode) {
		super();
		this.tripId = tripId;
		this.sectionId = sectionId;
		this.userMode = userMode;
	}
	private String tripId;
	private String sectionId;
	private String userMode;
	
	public String getTripId() {
		return tripId;
	}
	public String getSectionId() {
		return sectionId;
	}
	public String getUserMode() {
		return userMode;
	}
}
