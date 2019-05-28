/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.engines;

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.Rserve.RConnection;

import com.fourelementscapital.scheduler.exception.ExceptionRScriptNullError;
import com.fourelementscapital.scheduler.exception.ExceptionRServeUnixFailure;
import com.fourelementscapital.scheduler.exception.ExceptionRscriptError;
import com.fourelementscapital.scheduler.rserve.RServeConnectionPool;
import com.fourelementscapital.scheduler.rserve.RServeSession;

public abstract class RServeUnix extends AbstractRServe {

	
	public RServeUnix(String name, String uid) {
		super(name, uid);
		 
	}

	//private RConnection c=null;
	//private RServeSession rs=null;
	private Logger log = LogManager.getLogger(RServeUnix.class.getName());
	
	public RServeSession getRServeSession(int nid, String name) throws Exception  {
		
		log.debug("before opening session----:script name:"+name);					    	
		RServeSession rs=null;
		RConnection c=null;
		
		
    	try{
    		String startup_rinit=null;
    		rs=RServeConnectionPool.getRSession(startup_rinit);
    	}catch(Exception e){
    		throw new ExceptionRServeUnixFailure("Not able to get RSession from ConnectionPool!, Error:"+e.getMessage()); 
    	}
    	
    	try{
    		log.debug("+++++++attaches Rconnection from RServeSession....+++");
    		
    		c=(rs.getRconnection()!=null) ?  rs.getRconnection(): rs.getRsession().attach();				    		
    		rs.setRconnection(c);
    	}catch(Exception e){
    		throw new ExceptionRServeUnixFailure("Not able to attach connection to RSession!, Error:"+e.getMessage()); 
    	}
	    if(c!=null){

	        log.debug("c:"+c.isConnected());
	        rs.setScriptname(name);
	        rs.setRunning(true);
	        rs.setScheduler_id(nid);
	        rs.setTrigger_time(super.stackframe.getTrigger_time());
	        rs.setStarted_time(new Date().getTime());
	        rs.setThread(Thread.currentThread());	        
	        new SchedulerExePlanLogs(nid,super.stackframe.getTrigger_time()).log("RConnection process id:"+rs.getProcessid()+" Current Hits:"+rs.getNoexecutions(),SchedulerExePlanLogs.PEER_OK_UNIX_RSERVE_CAPTURE_PROCESSINFO);
	        
	    }
	   return rs;
	}

	@Override
	public void evalScript(RServeSession rs,String rscript)
			throws Exception {
		
		RConnection c=rs.getRconnection();
		c.assign(".tmp.",rscript);							    
	    log.debug("----script:----");
	    log.debug(rscript);
	    log.debug("-----:----");
	    REXP r = c.parseAndEval("try(eval(parse(text=.tmp.)),silent=TRUE)");
	    if (r!=null && r.inherits("try-error")) {							    	
	    	log.debug("r.asString:"+r.asString());
	    	String msg=r.asString();
		    throw new ExceptionRscriptError("Error in R Script: "+msg);
	    	
	    }
	    if(r==null) {							    	 
		    	throw new ExceptionRScriptNullError("Rscript Executed but unknown error message returned by RServe");
	    }else{							    	 
	    	this.stackframe.setTasklog(null);
	    }
		
	}

	@Override
	public void closeRconnection(RServeSession rs) throws Exception {
		try{
			
    		rs.finished(rs.getRconnection());
    		RServeConnectionPool.done(rs);
    		log.debug("-------attaches Rconnection from RServeSession....----");
    	}catch(Exception e){
    		if(rs.getKillmessage()!=null){
    			throw new ExceptionRServeUnixFailure(rs.getKillmessage());
    		}else{
    			throw new ExceptionRServeUnixFailure("Not able to detach RSession!, Error:"+e.getMessage());
    		}
    	}
		
	}

}


