/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/


package com.fourelementscapital.scheduler.config;

import java.io.File;
import java.io.FileInputStream;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
 


/**
 * This class populates the configuration value from property file 
 * 
 *
 */

public class Config {
	
	
	/**
	 * Ignores authentication for the user
	 */
	public static final String CONFIG_IGNORE_AUTH="ignore.authentication";
	
	/**
	 * Number of maximum thread in Rserve
	 */
	public static final String CONFIG_NUMBEROF_RSERVE_THREADS="rserve.thread.size";	
	
	/**
	 * Accepts anonymous users 
	 */
	public static final String USER_ANONYMOUS="anonymous";
	

	/**
	 * windows server/tomcat peer configuration file
	 */
	private static final String BUNDLE_NAME = "com.fe.config_windows"; //$NON-NLS-1$
	
	/**
	 * unix server/tomcat peer configuration file
	 */
	private static final String BUNDLE_NAME_UNIX = "com.fe.config_unix"; //$NON-NLS-1$
	

	/**
	 * windows peer configuration 
	 */
	private static final String PROPERTY_FILE_WIN = "peer_windows.properties"; //$NON-NLS-1$
	
	/**
	 * unix peer/server configuration file
	 */
	private static final String PROPERTY_FILE_UNIX = "peer_unix.properties"; //$NON-NLS-1$

	
	
	public static String CONFIG_PROPERTY_LOCATION=null;
	
	
	/**
	 * ignore p2p multicasting
	 */
	public static String P2P_NO_MULTICAST="p2p.nomulticast";
	
	
	/**
	 * hsql data file property
	 */
	public static String HSQLDB_QUEUE_FILE="hsql.queue.datafile";
	
	private static Properties confpro=null;

	//private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	private Config() {
	}

	/**
	 * returns the property value of specified key.
	 * @param key
	 * @return
	 */
	public static String getString(String key) {
		 
		
		if(CONFIG_PROPERTY_LOCATION==null){
			try {
				//return RESOURCE_BUNDLE.getString(key);
				return getResourceBuddle().getString(key);
			} catch (MissingResourceException e) {
				return '!' + key + '!';
			}
		}else{
			try {
				String ky= getPeerProperty(key);				
				if(ky==null) throw new Exception();
				else return ky;
			} catch (Exception e) {
				return '!' + key + '!';
			}
		}
	}
	
	
	/**
	 * returns the property value of specified key.
	 * @param key
	 * @return
	 */
	public static String getValue(String key) {
		 
		if(CONFIG_PROPERTY_LOCATION==null){
			try {
				 
				return getResourceBuddle().getString(key);
			} catch (MissingResourceException e) {
				return null;
			}
		}else{
			try {
				return getPeerProperty(key);
			} catch (Exception e) {
				return null;
			}
		}
	}
	
	
	
	
	private static ResourceBundle resourceBundle=null;
	
	private synchronized static  ResourceBundle getResourceBuddle(){
		
		if(resourceBundle==null){
			String os="win";
			resourceBundle= ResourceBundle.getBundle(BUNDLE_NAME); //windows 
			if(System.getProperty("os.name").toLowerCase().equals("freebsd")) {
				resourceBundle=ResourceBundle.getBundle(BUNDLE_NAME_UNIX);
			    os="freebsd";
			}    
			if(System.getProperty("os.name").toLowerCase().contains("linux")) {
				resourceBundle=ResourceBundle.getBundle(BUNDLE_NAME_UNIX);
			    os="linux";
			}
			LogManager.getLogger(Config.class.getName()).info("resourceBundle:"+resourceBundle+" os:"+os);
		} 
		return resourceBundle;		 
	}
	
	
	private static String getPeerProperty(String key) throws Exception {
		try{
			if(confpro==null){
				String propertyfilename=PROPERTY_FILE_WIN;
				if(System.getProperty("os.name").toLowerCase().equals("freebsd"))
					propertyfilename=PROPERTY_FILE_UNIX;
				
				if(System.getProperty("os.name").toLowerCase().contains("linux"))
					propertyfilename=PROPERTY_FILE_UNIX;				
				
				String folder=CONFIG_PROPERTY_LOCATION;
				folder=folder.endsWith(File.separator)?folder:folder+File.separator;
				confpro=new Properties();
				String filename=folder+"conf"+File.separator+propertyfilename;
				confpro.load(new FileInputStream(filename));
			}	
			return confpro.getProperty(key);
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}
}

