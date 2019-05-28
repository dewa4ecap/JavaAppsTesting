/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.rserve;

import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RSession;

import com.fourelementscapital.fileutils.RandomString;

public class RServeSession {

	private RSession rsession=null;
	private RConnection rconnection=null;
	private String scriptname;
	private String execute_r_uid=null;
	
	


	private boolean running=false;
	private int scheduler_id;
	private long trigger_time;
	private Thread thread;
	private int processid=0;
	private String killmessage=null;
	private int noexecutions=0;
	
	public int getNoexecutions() {
		return noexecutions;
	}

	public void setNoexecutions(int noexecutions) {
		this.noexecutions = noexecutions;
	}

	public String getKillmessage() {
		return killmessage;
	}

	public void setKillmessage(String killmessage) {
		this.killmessage = killmessage;
	}
	private String uid=null;
	
	public RServeSession(){
		this.uid=RandomString.getString(40);
	}
	
	public String getUid() {
		return uid;
	}
	
	public String getScriptname() {
		return scriptname;
	}

	public void setScriptname(String scriptname) {
		this.scriptname = scriptname;
	}
	
	public void finished(RConnection rc) throws Exception {
		try{		
			this.rsession=rc.detach();
			this.running=false;
			this.scheduler_id=0;
			this.trigger_time=0;
			this.started_time=0;
			this.thread=null;
			this.rconnection=null;
			this.scriptname=null;
			
		}catch(Exception e){
			throw e;
		}
	}
	
	public int getProcessid() {
		return processid;
	}

	public void setProcessid(int processid) {
		this.processid = processid;
	}

	public RConnection getRconnection() {
		return rconnection;
	}

	public void setRconnection(RConnection rconnection) {
		this.rconnection = rconnection;
	}

	public Thread getThread() {
		return thread;
	}

	public void setThread(Thread thread) {
		this.thread = thread;
	}


	
	public long getTrigger_time() {
		return trigger_time;
	}

	public void setTrigger_time(long trigger_time) {
		this.trigger_time = trigger_time;
	}

	public RSession getRsession() {
		return rsession;
	}
	public void setRsession(RSession rsession) {		
		this.rsession = rsession;
	}
	public boolean isRunning() {
		return running;
	}
	public void setRunning(boolean running) {
		this.running = running;
	}
	public int getScheduler_id() {
		return scheduler_id;
	}
	public void setScheduler_id(int scheduler_id) {
		this.scheduler_id = scheduler_id;
	}
 
	 
	public long getStarted_time() {
		return started_time;
	}
	public void setStarted_time(long started_time) {
		this.started_time = started_time;
	}
	
	
	public String getExecute_r_uid() {
		return execute_r_uid;
	}

	public void setExecute_r_uid(String execute_r_uid) {
		this.execute_r_uid = execute_r_uid;
	}
	
	private long started_time;
	
	public boolean equals(Object other) {
		RServeSession rs=(RServeSession)other;
		if(rs.uid.equals(this.uid)){
			return true;
		}else{
			return false;
		}
	
	}
}


