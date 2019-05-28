/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.p2p.peer;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import org.jfree.util.Log;

public class PeerSpecificConfigurations {

	private static String propertyfile="peer_specific.properties";
	 
	
	private static  Properties defa=new Properties();
	
	
	public static final String KEY_CONCURRENT_SESSION="rserve.concurrent.sessions";
	public static final String KEY_MAX_EXEC_SESSION="rserve.max.executions.in.session";
	public static Properties getDefaultProperties(){		
		defa.put(KEY_CONCURRENT_SESSION,"15");
		defa.put(KEY_MAX_EXEC_SESSION,"10000");		
		return defa;
		
	}
	
	
	public static Properties getProperties() throws Exception {
		
		Properties p=new Properties();
			
		
		try{
			FileInputStream fin=new FileInputStream(PeerSpecificConfigurations.propertyfile);
			p.load(fin);
		}catch(Exception e){
			Log.error("No property file found, or error:"+e.getMessage());
		}
		
		Properties d=getDefaultProperties();
		
		for(Object key:d.keySet()){
			if(!p.containsKey(key)){
				p.put(key, d.getProperty((String)key));
			}
		}
		return p;
	}

	
	public static void syncPartial(Properties newp) throws Exception {
		Properties oldp=getProperties();
		
		for(Object key:newp.keySet()){
			oldp.put(key, newp.getProperty((String)key));
		}
		FileOutputStream fos=new FileOutputStream(PeerSpecificConfigurations.propertyfile);	
		oldp.store(fos, null);
		
		
	}
	
	
}


