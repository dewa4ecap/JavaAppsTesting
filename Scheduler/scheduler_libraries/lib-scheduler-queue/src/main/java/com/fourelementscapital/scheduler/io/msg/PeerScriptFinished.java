/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.io.msg;

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fourelementscapital.scheduler.balance.LoadBalancingQueue;
import com.fourelementscapital.scheduler.io.request.IOPeerRequest;
import com.fourelementscapital.scheduler.io.server.ServerConnection;
import com.fourelementscapital.scheduler.queue.QueueStackManager;
import com.fourelementscapital.scheduler.rscript.RScript;

public class PeerScriptFinished extends IOPeerRequest {

	private Logger log = LogManager.getLogger(PeerScriptFinished.class.getName());
	
	private String script_uid;
	private String queue_uid;
	private String resultxml;
	private String error;
	private String status;
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getResultxml() {
		return resultxml;
	}

	public String getError() {
		return error;
	}

	public void setResultxml(String resultxml) {
		this.resultxml = resultxml;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getScript_uid() {
		return script_uid;
	}

	public String getQueue_uid() {
		return queue_uid;
	}

	public void setScript_uid(String script_uid) {
		this.script_uid = script_uid;
	}

	public void setQueue_uid(String queue_uid) {
		this.queue_uid = queue_uid;
	}

	@Override
	public void executeAtServer(ServerConnection pc) {
		// TODO Auto-generated method stub
		
		long start=new Date().getTime();
		RScript rs=new RScript();
		rs.setUid(this.getScript_uid());
	    rs.setError(this.getError());
	    rs.setResultXML(this.getResultxml());
	    
	    LoadBalancingQueue.getExecuteRScriptDefault().scriptFinished(rs, getResultxml(),getStatus());
	    long delay0=new Date().getTime()-start;
	    
	    try{	    	
	    	QueueStackManager.setStackIdle(pc.getUser(), getQueue_uid());	    	
			
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
	    long delay=new Date().getTime()-start;
	    log.debug("executeAtServer() : called:"+this.getScript_uid()+" finished, thread:"+Thread.currentThread().getName() +"   --->delay0:"+delay+" delay:"+delay);
	    
	}

	
	
}


