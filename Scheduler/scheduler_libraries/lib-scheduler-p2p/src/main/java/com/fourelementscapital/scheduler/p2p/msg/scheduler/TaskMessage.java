/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.p2p.msg.scheduler;

import com.fourelementscapital.scheduler.p2p.msg.ExceptionMessageHandler;


public abstract class TaskMessage extends ExceptionMessageHandler {

	private String scheduler_id="";		
	private String trigger_time="";
	private String next_trigger_time="";
	private String taskuid="";	
	
	public String getScheduler_id() {
		return scheduler_id;
	}
	
	public void setScheduler_id(String scheduelr_id) {
		this.scheduler_id = scheduelr_id;
	}

	public String getTrigger_time() {
		return trigger_time;
	}

	public void setTrigger_time(String trigger_time) {
		this.trigger_time = trigger_time;
	}

	public String getNext_trigger_time() {
		return next_trigger_time;
	}

	public void setNext_trigger_time(String next_trigger_time) {
		this.next_trigger_time = next_trigger_time;
	}

	public String getTaskuid() {
		return taskuid;
	}

	public void setTaskuid(String taskuid) {
		this.taskuid = taskuid;
	}
 
	
	
	
	 

}


