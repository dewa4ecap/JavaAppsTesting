/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.rscript;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import org.rosuda.JRI.REXP;

import com.fourelementscapital.scheduler.p2p.msg.MessageHandler;

public class RScript extends MessageHandler {
	
	
	private String uid;	
	private String script;	 
	private REXP result;
	private String resultXML;
	private String error;
	private boolean executing;
	private String taskuid;
	private String peer;
	private String executeAt;
	private Long  queued_time;
	private String uniquename;
    private String requesthost;
    private long delay;
    private Date startedtime;
    private String resultJSON;
    
    
    
    //private String queuefriendlytime;
    
	
	
	public String getResultJSON() {
		return resultJSON;
	}

	public void setResultJSON(String resultJSON) {
		this.resultJSON = resultJSON;
	}

	public Date getStartedtime() {
		return startedtime;
	}

	public void setStartedtime(Date startedtime) {
		this.startedtime = startedtime;
	}

	public String getRequesthost() {
		return requesthost;
	}

	public long getDelay() {
		return delay;
	}

	public void setDelay(long delay) {
		this.delay = delay;
	}

	public void setRequesthost(String requesthost) {
		this.requesthost = requesthost;
	}

	public String getUniquename() {
		return uniquename;
	}

	public void setUniquename(String uniquename) {
		this.uniquename = uniquename;
	}

	public Long getQueued_time() {
		return queued_time;
	}

	public void setQueued_time(Long queued_time) {
		this.queued_time = queued_time;
	}

	public String getExecuteAt() {
		return executeAt;
	}

	public void setExecuteAt(String executeAt) {
		this.executeAt = executeAt;
	}

	public String getPeer() {
		return peer;
	}

	public void setPeer(String peer) {
		this.peer = peer;
	}

	public String getTaskuid() {
		return taskuid;
	}

	public void setTaskuid(String taskuid) {
		this.taskuid = taskuid;
	}

	public boolean isExecuting() {
		return executing;
	}

	public void setExecuting(boolean executing) {
		this.executing = executing;
	}

	public RScript() {
		this.uid=UUID.randomUUID().toString();

	}
	
	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}

	public REXP getResult() {
		return result;
	}

	public void setResult(REXP result) {
	
		this.result = result;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}
	

	public String getResultXML() {
		return resultXML;
	}

	public void setResultXML(String resultXML) {
		this.resultXML = resultXML;
	}

	public boolean equals(Object o) {   
		RScript other=(RScript)o;
        if(other!=null && other.getUid().equals(this.getUid()) ){
        	return true;
        } else{
        	return false;
        }
		 
    }
	
	
	public int compareTo(RScript o) {
        return this.queued_time<o.queued_time?-1:
               this.queued_time>o.queued_time?1:0;
    }
	
	
	public Map executeAtDestination() {
		return null;
	}
	
	public String getScriptPreview(){
		if(this.script.trim().length()>=100){
			return this.script.trim().substring(0,99)+"....";
		}else{
			return this.script.trim();
		}
	}
	
	public String getQueuefriendlytime(){
		
		try{
			SimpleDateFormat sdf=new SimpleDateFormat("dd-MMM HH:mm:ss");
			Date d=new Date(this.queued_time);
			d.setTime(this.queued_time);
			return sdf.format(d);
		}catch(Exception e){
			return e.getMessage();
		}
	}

	@Override
	public void onSendingFailed() {
		// TODO Auto-generated method stub
		
	}
	
}


