/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fe.scheduler;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fe.client.AdminMgmt;
import com.fe.util.PasswordService;
//import com.fe.data.BBSyncTrigger;
import com.fourelementscapital.scheduler.SchedulerEngine;
import com.fourelementscapital.scheduler.config.Config;
import com.fourelementscapital.scheduler.p2p.P2PService;

public class SchedulerInitServlet extends HttpServlet  {

	private Logger log = LogManager.getLogger(SchedulerInitServlet.class.getName());
	private static String serverRoot=null;
	
	 
 
	    
	
	
	public void init( ServletConfig servletConfig )    throws ServletException  {
		
		//System.out.println("~~~~~~~~~~~~~~~~~~~Servlet Initialized");
		
		try{
			
			serverRoot=servletConfig.getServletContext().getRealPath("");
			System.out.println("------Server Root:"+serverRoot);
			
			log.debug("p2p service started ");
			
			String bbsy=Config.getString("bloomberg_synchronization");
			if(false){
				if(P2PService.getComputerName().equalsIgnoreCase("4ecapsvsg5") && !servletConfig.getServletContext().getRealPath("").contains("tomcat_beta")){
					throw new ServletException ("scheduler doen't support dev mode");
				}
			}
			
			//in production it should be moved to below the conditions, remove this and uncomment below.
			//new SchedulerEngine().startSchedulerQueue();
			//new BloombergDownloadJob().scheduleDownloadQueries();
			
			if(Config.getValue("load_balancing_server")!=null && Config.getValue("load_balancing_server").equals(P2PService.getComputerName())){
				new SchedulerEngine().startSchedulerQueue();
				System.out.println("------Scheduer Queue started");
				
				
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
			
		//}catch(ServletException e1){
		//	throw e1;
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
	
		
	/*
	private void addScheduledJobs(Vector<XMLScheduledJob> jobs,String confolder,String database) throws Exception{
		
		log.debug(" Job size:"+jobs.size());
		for(XMLScheduledJob sc:jobs){

			if(sc.getDaysNumber()!=-1 &&  sc.getWeekdayNumber()!=-1 && sc.getHourNumber()!=-1 && sc.getMinuteNumber()!=-1 ) {
				sc.setConfolder(confolder);
				sc.setDatabase(database);
				
				String jobname=sc.getWeekday()+sc.getHour()+sc.getMinute()+sc.getFilename()+"_"+database;
				
				SchedulerFactory sf=new StdSchedulerFactory();
				Scheduler scheduler=sf.getScheduler();
				//JobDetail holds the definition for Jobs
		    	JobDetail jobDetail = new JobDetail(jobname, Scheduler.DEFAULT_GROUP,BloombergSyncJob.class);			    	
		    	jobDetail.getJobDataMap().put("job",sc);
		    	
		    	Trigger trigger = TriggerUtils.makeWeeklyTrigger("trigger4"+jobname,sc.getWeekdayNumber(),sc.getHourNumber(),sc.getMinuteNumber());
		    	
		    	if(scheduler.getJobDetail(jobname,Scheduler.DEFAULT_GROUP )==null){
		    		log.debug("job "+jobname+" added to scheduler");
		    	 	scheduler.scheduleJob(jobDetail, trigger );
		    		scheduler.start();
		    	}else{
		    		throw new Exception("Job "+jobname+" already added to scheduler"); 
		    	}
			}
			
		}
	}
	*/
	public static String getAppPath(){
		if(serverRoot==null){
			return "c:\\tomcat\\webapps\\bldb";
		}else{
			return serverRoot;
		}
	}
	
	
	

	
	

}



