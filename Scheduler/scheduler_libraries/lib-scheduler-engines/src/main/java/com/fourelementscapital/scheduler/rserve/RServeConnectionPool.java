/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.rserve;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rosuda.REngine.Rserve.RConnection;

import com.fourelementscapital.scheduler.p2p.peer.PeerSpecificConfigurations;

public class RServeConnectionPool {
	
	private static ConcurrentLinkedQueue<RServeSession> sessions=new ConcurrentLinkedQueue();
	private static ConcurrentLinkedQueue<RServeSession> sessInUse=new ConcurrentLinkedQueue();
  
    private static final Semaphore sessLock=new Semaphore(1,true);
	private static long TIMEOUT_MS=2000;	

	/**
	 * gets RSession from connection pool, if no live connection available, it creates new and store it in pool 
	 * @return
	 * @throws Exception
	 */

	
	private static void acquireLock(){
		
		try{
			Date start=new Date();
			
			RServeConnectionPool.sessLock.tryAcquire(TIMEOUT_MS, TimeUnit.MILLISECONDS);
		}catch(Exception e){
			LogManager.getLogger(RServeConnectionPool.class.getName()).error("acquireLock() error:"+e.getMessage());
		}
	}
	

	private static void releaseLock(){
		
		try{			
			RServeConnectionPool.sessLock.release();
			//log.debug("....releasing lock: thread:"+Thread.currentThread().getId());
		}catch(Exception e){
			LogManager.getLogger(RServeConnectionPool.class.getName()).error("releaseLock() error:"+e.getMessage());
		}
	}
	
	public static synchronized RServeSession getRSession(String rinit) throws Exception {
		//new RConnection()
		acquireLock();
		try{
			RServeSession  rtn=null;
			if(RServeConnectionPool.sessions.size()>0){
				//rtn=(RServeSession)RServeConnectionPool.sessions.lastElement();
				rtn=(RServeSession)RServeConnectionPool.sessions.poll();
				//RServeConnectionPool.sessions.remove(rtn);
				RServeConnectionPool.sessInUse.add(rtn);			
	
			}else{
				
				int count=0;
				RConnection c=null;
				
				while(c==null && count<10) {				 
					try{c= new RConnection();}catch(Exception e){   ;}				
					try{ Thread.sleep(200);	}catch(Exception e){}
					count++;
				}		    	
			    if(c!=null){
			    	rtn=new RServeSession();	    	
			    	c.assign(".r_session_id",rtn.getUid());
			    	
			    	//loading rinit at the first time of opening connection.
			    	if(rinit!=null && !rinit.trim().equals("")){
			    		try{
			    		c.parseAndEval(rinit);
			    		}catch(Exception e){
			    			throw new Exception("Not able to load Rnint at RServeConnectionPool.getRsession(), for rinit: "+rinit);
			    		}
			    	}
			    	//c.eval(".r_scripts=NULL");
			    	int processid=c.eval("Sys.getpid()").asInteger();
			    	rtn.setProcessid(processid);
			    	rtn.setRconnection(c);
			    	//rtn.setRsession(c.detach()); 
			    	RServeConnectionPool.sessInUse.add(rtn);
			    }
			}
			if(rtn!=null){
				rtn.setNoexecutions(rtn.getNoexecutions()+1);
			}
			return rtn;
		}finally{
			releaseLock();
		}
	}
	
	
	 
	
	public static List<RServeSession> getAllSessions(){
		ArrayList rtn=new ArrayList();
		for(RServeSession rs: RServeConnectionPool.sessions){
			rtn.add(rs);
		}
		for(RServeSession rs: RServeConnectionPool.sessInUse){
			rtn.add(rs);
		}
		return rtn;
	}
 
	public static void remove(RServeSession rs){
		 if(RServeConnectionPool.sessInUse.contains(rs))RServeConnectionPool.sessInUse.remove(rs);
		 if(RServeConnectionPool.sessions.contains(rs))RServeConnectionPool.sessions.remove(rs);
	}
	
	
	
	public static synchronized void done(RServeSession rs) throws Exception {		
		acquireLock();
		try{			
			
			int rtn=0;
			String val=PeerSpecificConfigurations.getProperties().getProperty(PeerSpecificConfigurations.KEY_MAX_EXEC_SESSION);			
			if(val!=null && !val.trim().equals("")){
				rtn=Integer.parseInt(val);
			}
			if(rtn>0 && rs.getNoexecutions()>=rtn){
				Process p=Runtime.getRuntime().exec("kill -9 " + rs.getProcessid());
				processOutput(p);
				
				remove(rs);
			}else{
			
				RServeConnectionPool.sessInUse.remove(rs);		
				RServeConnectionPool.sessions.add(rs);
			}
		}finally{
			releaseLock();
		}
	}
	
	private static void processOutput(Process p) throws Exception {
		 
		InputStream inputStream = p.getInputStream();
		InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
		BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

		String line;
		while ((line = bufferedReader.readLine()) != null)
		{
		    //System.out.println("RServeSessionQuery. console_output:"+line);
		}
		
		
	}
}


