/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.balance.hsqldb;

import com.fourelementscapital.scheduler.balance.LoadBalancingQueueItem;

public class LoadBalancingHSQLQueueItem extends LoadBalancingQueueItem {
	private long trigger_time=0;
	private long nexttrigger_time=0;
	private long started_time=0;	
	private String status=null;
	private String taskuid=null;
	private Byte queuegate=null;
	private String dependentids=null;
	private String dependentchecktime=null;
	private boolean waitingfordp=false;
	private String started_peers=null;
	private int concurrentexecution=1;
	
	public int getConcurrentexecution() {
		return concurrentexecution;
	}

	public void setConcurrentexecution(int concurrentexecution) {
		this.concurrentexecution = concurrentexecution;
	}

	public String getStarted_peers() {
		return started_peers;
	}

	public void setStarted_peers(String started_peers) {
		this.started_peers = started_peers;
	}

	private long id;
	
	
	
	public String getDependentids() {
		return dependentids;
	}

	public boolean isWaitingfordp() {
		return waitingfordp;
	}

	public void setWaitingfordp(boolean waitingfordp) {
		this.waitingfordp = waitingfordp;
	}

	public void setDependentids(String dependentids) {
		this.dependentids = dependentids;
	}

	public String getDependentchecktime() {
		return dependentchecktime;
	}

	public void setDependentchecktime(String dependentchecktime) {
		this.dependentchecktime = dependentchecktime;
	}

	public long getId() {
		return id;
	}
 
	public Byte getQueuegate() {
		return queuegate;
	}

	public void setQueuegate(Byte queuegate) {
		this.queuegate = queuegate;
	}

	public void setId(long id) {
		this.id = id;
	}
	public String getTaskuid() {
		return taskuid;
	}
	public void setTaskuid(String taskuid) {
		this.taskuid = taskuid;
	}
	
	private long lastTenderTime=0;
		
	public long getLastTenderTime() {
		return lastTenderTime;
	}
	public void setLastTenderTime(long lastTenderTime) {
		this.lastTenderTime = lastTenderTime;
	}
	public long getTrigger_time() {
		return trigger_time;
	}
	public void setTrigger_time(long trigger_time) {
		this.trigger_time = trigger_time;
	}
	public long getNexttrigger_time() {
		return nexttrigger_time;
	}
	public void setNexttrigger_time(long nexttrigger_time) {
		this.nexttrigger_time = nexttrigger_time;
	}
	public long getStarted_time() {
		return started_time;
	}
	public void setStarted_time(long started_time) {
		this.started_time = started_time;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	
}


