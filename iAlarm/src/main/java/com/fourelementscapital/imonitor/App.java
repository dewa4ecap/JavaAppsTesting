/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.imonitor;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fourelementscapital.imonitor.socket.IMonotorSocketServer;
import com.fourelementscapital.imonitor.utils.ServerUtils;


/**
 * A com.fourelementscapital Class
 */
public class App {
	
	private static final Logger log = LogManager.getLogger(App.class.getName());
	
	public static void main(String[] args) {
		try {
			ServerUtils.configureServer();
			IMonotorSocketServer server = new IMonotorSocketServer();
			server.startServer();
		} catch (Exception ex) {
			log.log(Level.ERROR, ex);
		}
    }
}

