/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fe.servlet;

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

import com.fe.client.Authenticated;
import com.fe.common.Constant;
import com.fe.p2p.P2PServiceServlet;
import com.fourelementscapital.db.BBSyncDB;
import com.fourelementscapital.scheduler.balance.ExecutingQueueCleaner;
import com.fourelementscapital.scheduler.p2p.P2PService;

public class P2PServletContextListener implements ServletContextListener,HttpSessionListener, ServletRequestListener  {

	
	private static long sessionid=0;
	private Logger log = LogManager.getLogger(P2PServletContextListener.class.getName());

	
	private static final String ATTRIBUTE_NAME = "com.example.SessionCounter";
    //private Map<HttpSession, String> sessions = new ConcurrentHashMap<HttpSession, String>();
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
		 
		 //if(RestartTomcat.proc!=null){
			 //RestartTomcat.proc.destroy();
		 //}
		
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
		//String ip=(String)s.getAttribute(Authenticated.REMOTE_IP);
		//log.debug("Session created:"+ip);
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
		//HttpServletRequest request = (HttpServletRequest) event.getServletRequest();
        //HttpSession session = request.getSession();
        //if (session.isNew()) {
        //    sessions.put(session, request.getRemoteAddr());
        //}
		
	}
	
	
	
	 //public static P2PServletContextListener getInstance(ServletContext context) {
	 //       return (P2PServletContextListener) context.getAttribute(ATTRIBUTE_NAME);
	 //}
	 
	 
	 public static ConcurrentLinkedQueue<HttpSession> getSessions(){
		 return sessions;
	 }
	 
	
	 
	 public static HttpSession getActiveSessions(String user){
		 HttpSession rtn=null;
		 for(HttpSession sess: P2PServletContextListener.getSessions()){
			if(sess.getAttribute(Constant.SESSION_LOGGED_USER)!=null && sess.getAttribute(Constant.SESSION_LOGGED_USER).equals(user)){
				//System.out.println("invalidating session...."+sess.getId());
				//sess.invalidate();
				//sess.setMaxInactiveInterval(0);
				rtn=sess;
			}
		}
		 return rtn;
	 }
	 
	
	 
	 
	 //public int getCount(String remoteAddr) {
	 //       return Collections.frequency(sessions.values(), remoteAddr);
	 //}

}


