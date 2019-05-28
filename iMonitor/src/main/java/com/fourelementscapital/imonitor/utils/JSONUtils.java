package com.fourelementscapital.imonitor.utils;


import org.json.JSONObject;

public class JSONUtils {

	public static String getString(JSONObject object, String fieldName) {
		String retVal = null;
		if ((object != null) && (object.has(fieldName)) && !(object.get(fieldName).equals(JSONObject.NULL))) {
			retVal = object.get(fieldName).toString();
		}
		return retVal;
	}

	public static String getString(JSONObject object, String fieldName, String defaultString) {
		String retVal = getString(object, fieldName);
		if (retVal == null) {
			retVal = defaultString;
		}
		return retVal;
	}
}
