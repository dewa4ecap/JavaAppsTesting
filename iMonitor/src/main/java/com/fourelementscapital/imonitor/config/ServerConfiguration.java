package com.fourelementscapital.imonitor.config;

import java.util.Properties;

public class ServerConfiguration {

	private final static String SERVER_PORT_PROP = "server.port";
	private final static String SERVER_THREADS_PROP = "server.threads";
	private final static String SERVER_BACKLOG_PROP_NAME = "server.backlog";

	private static int port;
	private static int threads;
    private static int backlog;

	public static void loadServerConfiguration(Properties prop) {
		port = Integer.parseInt(prop.getProperty(SERVER_PORT_PROP));
		threads = Integer.parseInt(prop.getProperty(SERVER_THREADS_PROP));
		backlog = Integer.parseInt(prop.getProperty(SERVER_BACKLOG_PROP_NAME));
	}
	
    public static int getPort() {
		return port;
	}
	
	public static int getThreads() {
		return threads;
	}
	
	public static int getBacklog() {
		return backlog;
	}
}
