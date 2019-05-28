/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.p2p.msg.impl;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fourelementscapital.scheduler.balance.LoadBalancingQueue;
import com.fourelementscapital.scheduler.p2p.msg.MessageHandler;
import com.fourelementscapital.scheduler.rscript.RScript;

public class ScriptFinished extends MessageHandler{

	private String status;
	private String result;
	private String uid;
	private String error;
	private String jsonresult;
	

	public String getJsonresult() {
		return jsonresult;
	}

	public void setJsonresult(String jsonresult) {
		this.jsonresult = jsonresult;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	private Logger log = LogManager.getLogger(ScriptFinished.class.getName());
	

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	
	
	
	public Map executeAtDestination() {
	
		//log.debug("received signal from peer, script finished,");
		log.debug("status:"+status);
		log.debug("result:"+result);
		log.debug("error:"+error);

		
		
		RScript rs=new ExecuteScript();
		rs.setUid(this.getUid());
	    rs.setError(this.getError());
	    rs.setResultJSON(this.jsonresult);
	    
	    LoadBalancingQueue.getExecuteRScriptDefault().scriptFinished(rs, this.result,this.status);
	    	
		return null;
		
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	@Override
	public void onSendingFailed() {
		log.error("sending failed, uid:"+this.uid);
		
	}

}


