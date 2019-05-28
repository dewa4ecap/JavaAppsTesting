/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.servlet;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fourelementscapital.client.AdminMgmt;
import com.fourelementscapital.util.PasswordService;
import com.fourelementscapital.loadbalance.SchedulerEngine;
import com.fourelementscapital.config.Config;
import com.fourelementscapital.p2p.P2PService;

public class SchedulerInitServlet extends HttpServlet  {
	private Logger log = LogManager.getLogger(SchedulerInitServlet.class.getName());
	private static String serverRoot=null;
	
	public void init( ServletConfig servletConfig )    throws ServletException  {
		try{
			serverRoot=servletConfig.getServletContext().getRealPath("");
			System.out.println("------Server Root:"+serverRoot);
			log.debug("p2p service started ");
			String bbsy=Config.getString("bloomberg_synchronization");
			if(false){
				if(P2PService.getComputerName().equalsIgnoreCase("4ecapsvsg5") && !servletConfig.getServletContext().getRealPath("").contains("tomcat_beta")){
					throw new ServletException ("scheduler doesn't support dev mode");
				}
			}
			
			//in production it should be moved to below the conditions, remove this and uncomment below.
			//new SchedulerEngine().startSchedulerQueue();
			//new BloombergDownloadJob().scheduleDownloadQueries();
			if(Config.getValue("load_balancing_server")!=null && Config.getValue("load_balancing_server").equals(P2PService.getComputerName())){
				new SchedulerEngine().startSchedulerQueue();
				System.out.println("------Scheduler Queue started");
				//new WSServer( 10008, new Draft_17() ).start();				 
				System.out.println("------WebSocket Server started..");
			}
			if(false){
				if(bbsy!=null & bbsy.equalsIgnoreCase("no")){
				//under development
					throw new Exception("Bloomberg Synchronization ignored, as it is not production server");
				}
			}
			log.debug("init()");
			if(Config.getValue("load_balancing_server")==null){
				//shouldn't build any queue.
				//new SchedulerEngine().startSchedulerQueue();
			}
			log.debug("init()1");
			//new BloombergDownloadJob().scheduleDownloadQueries();
			initAdminDefaultPwd();			
		}catch(Exception e){
			e.printStackTrace();
			log.error("####Error while scheduling bloomberg sync ERROR MSG:"+e.getMessage());
		}
	}

	private void initAdminDefaultPwd() {
		try{
			Properties prop=new Properties();
    		prop.load(new FileInputStream(AdminMgmt.DB_USER_PROPERTY_FILE));
		}catch(java.io.FileNotFoundException fnf){
			try{
				Properties prop = new Properties();
				String password=PasswordService.encrypt("admin");
				prop.setProperty("admin", password);
				prop.store(new FileOutputStream(AdminMgmt.DB_USER_PROPERTY_FILE), null);
			}catch(Exception ex1){}
			
		}catch(Exception e){
			//e.printStackTrace();
		}
	}
}