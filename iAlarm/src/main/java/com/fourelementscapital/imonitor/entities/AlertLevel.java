/**
 * 
 */
package com.fourelementscapital.imonitor.entities;

import java.util.HashMap;
import java.util.Map;


/**
 * @author abhisekshukla
 *
 */
public enum AlertLevel {
	
	HIGH("High", "h", 10),
	MEDIUM("Medium", "m", 5),
	LOWER("Lower", "l", 1),
	UNKNOWN("Lower", "l", -1);
	

	private String displayName;
	private String abbreviation;
	private int priority;
	private static final Map<String, AlertLevel> alerts_BY_ABBR = new HashMap<String, AlertLevel>();

	/* static initializer */
	static {
		for (AlertLevel alert : values()) {
			alerts_BY_ABBR.put(alert.getAbbreviation(), alert);
		}
	}
	
	/**
	 * Constructs a new AlertLevel.
	 * 
	 * @param name
	 *            the alert's name.
	 * @param abbreviation
	 *            the alert's abbreviation.
	 */
	AlertLevel(String displayName, String abbreviation, int priority) {
		this.displayName = displayName;
		this.abbreviation = abbreviation;
		this.priority = priority;
	}

	/**
	 * Returns the alert's abbreviation.
	 * 
	 * @return the alert's abbreviation.
	 */
	public String getAbbreviation() {
		return abbreviation;
	}

	/**
	 * Gets the enum constant with the specified abbreviation.
	 * 
	 * @param abbr
	 *            the alert's abbreviation.
	 * @return the enum constant with the specified abbreviation.
	 * @throws IllegalArgumentException
	 *             if the abbreviation is invalid.
	 */
	public static AlertLevel valueOfAbbreviation(final String abbr) {
		final AlertLevel alert = alerts_BY_ABBR.get(abbr);
		if (alert != null) {
			return alert;
		} else {
			return UNKNOWN;
		}
	}

	/**
	 * @param displayName
	 * @return
	 */
	public static AlertLevel valueOfName(final String displayName) {
		final String enumName = displayName.toUpperCase();
		try {
			return valueOf(enumName);
		} catch (final IllegalArgumentException e) {
			return AlertLevel.UNKNOWN;
		}
	}

	@Override
	public String toString() {
		return displayName;
	}
	
}
