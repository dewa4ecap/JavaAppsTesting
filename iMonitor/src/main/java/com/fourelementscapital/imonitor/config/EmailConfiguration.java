package com.fourelementscapital.imonitor.config;

import java.util.Properties;

public class EmailConfiguration {

	private final static String SYSTEM_SMTP_HOST_PROP = "email.host";
	private final static String SYSTEM_SMTP_PORT_PROP = "email.port";
	private final static String SYSTEM_EMAIL_FROM_PROP = "email.from";
	private final static String SYSTEM_EMAIL_PASSWORD_PROP = "email.password";

	private static String host;
	private static String port;
    private static String emailFrom;
    private static String password;
    
    
	public static void loadEmailConfiguration(Properties prop) {
		host = prop.getProperty(SYSTEM_SMTP_HOST_PROP);
		port = prop.getProperty(SYSTEM_SMTP_PORT_PROP);
		emailFrom = prop.getProperty(SYSTEM_EMAIL_FROM_PROP);
		password = prop.getProperty(SYSTEM_EMAIL_PASSWORD_PROP);
	}
	
	public static String getHost() {
		return host;
	}

	public static String getPort() {
		return port;
	}

	public static String getEmailFrom() {
		return emailFrom;
	}

	public static String getPassword() {
		return password;
	}
}
