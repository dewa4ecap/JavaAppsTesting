package com.fourelementscapital.imonitor.config;

import java.util.Properties;

public class PhoneCallConfiguration {

	private final static String PHONE_CALL_ACCESS_TOKEN_PROP = "phone.accessToken";
	private final static String PHONE_CALL_APP_ID_PROP = "phone.appId";
  
	private static String accessToken;
	private static String appID;

	public static void loadPhoneCallConfiguration(Properties prop) {
		accessToken = prop.getProperty(PHONE_CALL_ACCESS_TOKEN_PROP);
		appID = prop.getProperty(PHONE_CALL_APP_ID_PROP);
	}

	public static String getAccessToken() {
		return accessToken;
	}

	public static String getAppId() {
		return appID;
	}
}
