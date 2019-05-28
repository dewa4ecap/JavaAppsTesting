/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.servlet;

import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fourelementscapital.client.Authenticated;
import com.fourelementscapital.config.Constant;
import com.fourelementscapital.db.BBSyncDB;
import com.fourelementscapital.loadbalance.ExecutingQueueCleaner;
import com.fourelementscapital.p2p.P2PService;

public class P2PServletContextListener implements ServletContextListener,HttpSessionListener, ServletRequestListener  {
	
	private static long sessionid=0;
	private Logger log = LogManager.getLogger(P2PServletContextListener.class.getName());
    private static ConcurrentLinkedQueue<HttpSession> sessions=new ConcurrentLinkedQueue<HttpSession>();
	 
	public void contextDestroyed(ServletContextEvent event) {
		 BBSyncDB syncdb=BBSyncDB.getBBSyncDB();
		 try{			 
				syncdb.connectDB();
				syncdb.peerStopped(P2PService.getComputerName(), P2PServletContextListener.sessionid, new Date());
		 }catch(Exception e){
			 e.printStackTrace();
		 }finally{
			 try{syncdb.closeDB();}catch(Exception e){}
		 }
		 System.out.println("~~~~~~~~~~~~cleaning and stopping all running services.....");
		 new P2PServiceServlet().destroy();
		 ExecutingQueueCleaner.stop();
		 System.exit(0);
	}
 
	public void contextInitialized(ServletContextEvent event) {
		// TODO Auto-generated method stub
		 sessionid=new Date().getTime();
		 BBSyncDB syncdb=BBSyncDB.getBBSyncDB();
		 try{			 
				syncdb.connectDB();
				syncdb.peerStarted(P2PService.getComputerName(), P2PServletContextListener.sessionid, new Date());
		 }catch(Exception e){
			 e.printStackTrace();
		 }finally{
			 try{syncdb.closeDB();}catch(Exception e){}
		 }
	}

	public void sessionCreated(HttpSessionEvent hse) {
		HttpSession s=hse.getSession();
		sessions.add(s);
	}

	public void sessionDestroyed(HttpSessionEvent hse) {
		// TODO Auto-generated method stub
		HttpSession s=hse.getSession();
		String ip=(String)s.getAttribute(Authenticated.REMOTE_IP);
		log.debug("Session before destroyed:"+sessions.size());
		sessions.remove(s);		
		log.debug("Session after destroyed:"+sessions.size());
	}

	public void requestDestroyed(ServletRequestEvent arg0) {
		// TODO Auto-generated method stub
	}

	public void requestInitialized(ServletRequestEvent event) {
		// TODO Auto-generated method stub
	}
	 
	 public static ConcurrentLinkedQueue<HttpSession> getSessions(){
		 return sessions;
	 }
	 
	 public static HttpSession getActiveSessions(String user){
		 HttpSession rtn=null;
		 for(HttpSession sess: P2PServletContextListener.getSessions()){
			if(sess.getAttribute(Constant.SESSION_LOGGED_USER)!=null && sess.getAttribute(Constant.SESSION_LOGGED_USER).equals(user)){
				rtn=sess;
			}
		}
		 return rtn;
	 }
}