/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/


package com.fourelementscapital.scheduler.balance;

import java.util.Date;

import com.fourelementscapital.scheduler.engines.StackFrame;


public class LoadBalancingQueueItem {

	private int schedulerid;
	private StackFrame sf;
	private boolean executing=false;
	private String machine=null;
	private Date started=null;
	private long lastExecutedDuration=0;
	private long overlaptimeout=0;
	private String inject_code=null;
	private long timeoutexpiry=0;
	 
	public long getTimeoutexpiry() {
		return timeoutexpiry;
	}

	public void setTimeoutexpiry(long timeoutexpiry) {
		this.timeoutexpiry = timeoutexpiry;
	}

	public String getInject_code() {
		return inject_code;
	}

	public void setInject_code(String inject_code) {
		this.inject_code = inject_code;
	}

	public long getOverlaptimeout() {
		return overlaptimeout;
	}

	public void setOverlaptimeout(long overlaptimeout) {
		this.overlaptimeout = overlaptimeout;
	}

	public Date getStarted() {
		return started;
	}

	public void setStarted(Date started) {
		this.started = started;
	}

	public int getSchedulerid() {
		return schedulerid;
	}

	public void setSchedulerid(int schedulerid) {
		this.schedulerid = schedulerid;
	}

	public StackFrame getSf() {
		return sf;
	}

	public void setSf(StackFrame sf) {
		this.sf = sf;
	}

	public boolean isExecuting() {
		return executing;
	}

	public void setExecuting(boolean executing) {
		this.executing = executing;
	}

	public String getMachine() {
		return machine;
	}

	public void setMachine(String machine) {
		this.machine = machine;
	}

	public long getLastExecutedDuration() {
		return lastExecutedDuration;
	}

	public void setLastExecutedDuration(long lastExecutedDuration) {
		this.lastExecutedDuration = lastExecutedDuration;
	}

	public boolean equals1(Object other) {   
		
        if(other!=null && ((LoadBalancingQueueItem)other).getSchedulerid()==this.getSchedulerid()){
        	return true;
        }else{
        	return false;
        }
		 
    }
	
	public boolean equals(Object o) {   
		LoadBalancingQueueItem other=(LoadBalancingQueueItem)o;
        if(other!=null && (other.getSf()==null || this.getSf()==null )&& other.getSchedulerid()==this.getSchedulerid()){
        	return true;
        } else if(other!=null && other.getSf()!=null && this.getSf()!=null && other.getSf().getTrigger_time()==this.getSf().getTrigger_time() && other.getSchedulerid()==this.getSchedulerid()){
        	return true;
        }else{
        	return false;
        }
		 
    }
	
 

	public String toString(){
		return "Running;"+this.executing+" id:"+this.schedulerid;
		
	}
	
}

