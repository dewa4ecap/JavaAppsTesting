/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.engines;

import java.util.Map;
import java.util.Vector;

import com.fourelementscapital.scheduler.p2p.MessageBean;
import com.fourelementscapital.scheduler.rscript.RScript;

public class StackFrame {
	public ScheduledTask	task	= null;
	public Map 				data = null;

	private long trigger_time=0;
	private long nexttrigger_time=0;
	private long started_time=0;	
	private String status=null;
	
	private RScript rscript=null;
	private String replyTo;
	private String invokedby="";
	private Vector dbConnectionIds=new Vector();
	private String executed_code=null;
	private String console_message=null;
	
	private String queue_uid=null;
	
	
	
	public String getQueue_uid() {
		return queue_uid;
	}

	public void setQueue_uid(String queue_uid) {
		this.queue_uid = queue_uid;
	}

	public String getConsole_message() {
		return console_message;
	}

	public void setConsole_message(String console_message) {
		this.console_message = console_message;
	}

	public String getExecuted_code() {
		return executed_code;
	}

	public void setExecuted_code(String executed_code) {
		this.executed_code = executed_code;
	}

	public Vector getDbConnectionIds() {
		return dbConnectionIds;
	}

	public String getInvokedby() {
		return invokedby;
	}

	public void setInvokedby(String invokedby) {
		this.invokedby = invokedby;
	}

	public String getReplyTo() {
		return replyTo;
	}

	public void setReplyTo(String replyTo) {
		this.replyTo = replyTo;
	}

	public RScript getRscript() {
		return rscript;
	}

	public void setRscript(RScript rscript) {
		this.rscript = rscript;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	private String tasklog=null;
	private boolean dependencyfailed=false;
	
	
	public boolean isDependencyfailed() {
		return dependencyfailed;
	}

	public void setDependencyfailed(boolean dependencyfailed) {
		this.dependencyfailed = dependencyfailed;
	}

	private StackFrameCallBack callback=null; 
	
	private MessageBean mbean=null;
	
	
	/**
	 * @deprecated
	 * 
	 */
	public MessageBean getMbean() {
		return mbean;
	}

	/**
	 * @deprecated
	 * @param mbean
	 */
	public void setMbean(MessageBean mbean) {
		this.mbean = mbean;
	}
	public void addCallBack(StackFrameCallBack callback){
		this.callback=callback;
	}
	
	public StackFrameCallBack getCallBack(){
		return this.callback;
	}
	
	public String getTasklog() {
		return tasklog;
	}

	public void setTasklog(String tasklog) {
		this.tasklog = tasklog;
	}

	public long getNexttrigger_time() {
		return nexttrigger_time;
	}

	public void setNexttrigger_time(long nexttrigger_time) {
		this.nexttrigger_time = nexttrigger_time;
	}

	public long getTrigger_time() {
		return trigger_time;
	}

	public void setTrigger_time(long trigger_time) {
		this.trigger_time = trigger_time;
	}

	private int logid=0;
	
	
	public int getLogid() {
		return logid;
	}

	public void setLogid(int logid) {
		this.logid = logid;
	}

	
	public StackFrame(ScheduledTask task, Map data) {
		this.task = task;
		this.data = data;
	}
	
	public ScheduledTask getTask(){
		return this.task;
	}
	
	public Map getData(){
		return this.data;
	}
	
	public long getStarted_time() {
		return started_time;
	}

	public void setStarted_time(long started_time) {
		this.started_time = started_time;
	}

	
	public boolean equals(Object other) {  
		
        if (!(other instanceof StackFrame) ) return false;
        
        final StackFrame other1 = (StackFrame) other;
		if(this.rscript!=null){
		    if(this.rscript!=null && other1.rscript!=null && this.rscript.getUid().equals(other1.rscript.getUid())){
		    	return true;
		    }else{
		    	return false;
		    }
			
		}else{
			//validation for all types
			return equals1(other);
		}
	}
	
	public boolean equals1(Object other) {       
        
		if(this.data==null) return false;
        //if ( !(other instanceof StackFrame) ) return false;
        
        final StackFrame other1 = (StackFrame) other;
        if(other1.data==null) return false; 
        
        
        if(other1.data.get("id")!=null && this.data.get("id")!=null){
        	Number onid=(Number)other1.data.get("id");
        	Number nid=(Number)this.data.get("id");        
        	if (onid.intValue()==nid.intValue())  return true;
        }
        if(other1.data.get("script_id")!=null && this.data.get("script_id")!=null){
        	Number onid=(Number)other1.data.get("script_id");
        	Number nid=(Number)this.data.get("script_id");        
        	if (onid.intValue()==nid.intValue())  return true;
        }        
        
        return false;
    }
}


