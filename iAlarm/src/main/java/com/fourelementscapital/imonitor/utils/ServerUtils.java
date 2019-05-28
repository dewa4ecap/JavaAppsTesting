package com.fourelementscapital.imonitor.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import com.fourelementscapital.imonitor.config.EmailConfiguration;
import com.fourelementscapital.imonitor.config.PhoneCallConfiguration;
import com.fourelementscapital.imonitor.config.ServerConfiguration;

public class ServerUtils {
	
	private static String CONFIG_FILE_NAME = "imonitorApp.properties";

	public static void configureServer() throws FileNotFoundException, IOException {
		Properties props = loadAppProperties();
		ServerConfiguration.loadServerConfiguration(props);
		EmailConfiguration.loadEmailConfiguration(props);
		PhoneCallConfiguration.loadPhoneCallConfiguration(props);
	}
	
	private static Properties loadAppProperties() throws FileNotFoundException, IOException {
		Properties appProps = new Properties();
		appProps.load(new FileInputStream(loadResourcePath()));
		return appProps;
	}
	
	private static String loadResourcePath() {
		 URL url = null;
		 ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		 url = classLoader.getResource(CONFIG_FILE_NAME);
         if (url != null) {
             return url.getFile();
         } else { //if not found try to get from current path
        	 return CONFIG_FILE_NAME;
         }
	}
	
}
