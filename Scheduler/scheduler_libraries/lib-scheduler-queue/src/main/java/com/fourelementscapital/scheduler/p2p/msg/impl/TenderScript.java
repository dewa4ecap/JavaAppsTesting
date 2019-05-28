/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.p2p.msg.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.jcs.JCS;
import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fourelementscapital.scheduler.balance.LoadBalancingQueue;
import com.fourelementscapital.scheduler.p2p.msg.MessageHandler;
import com.fourelementscapital.scheduler.p2p.msg.PostCallBack;
import com.fourelementscapital.scheduler.p2p.msg.PostMessage;
import com.fourelementscapital.scheduler.peer.QueueFactory;
import com.fourelementscapital.scheduler.rscript.RScript;

public class TenderScript extends MessageHandler implements PostCallBack {
 
	private String uid;
	private String taskuid;
	protected JCS cache=null;
	private String requestid=null;
	private Logger log = LogManager.getLogger(TenderScript.class.getName());
	private static long TIMEOUT_MS=2000;
	
	
	private static Semaphore lock=new Semaphore(1,true);
	

	public TenderScript(){
		this.requestid=new Date().getTime()+"-"+Thread.currentThread().getName();
	}
	
	public String getRequestid() {
		return requestid;
	}

	public void setRequestid(String requestid) {
		this.requestid = requestid;
	}
	
	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getTaskuid() {
		return taskuid;
	}

	public void setTaskuid(String taskuid) {
		this.taskuid = taskuid;
	}
	
	public Map executeAtDestination() {
	
		HashMap h=new HashMap();
		int p=0;
		try{
			acquireLock();
			
			log.debug("getTaskuid()"+getTaskuid());
			log.debug("getQueue():"+new QueueFactory().getQueue(getTaskuid()));
			log.debug("Excecuting threads:"+new QueueFactory().getQueue(getTaskuid()).getExecutingStacks());
			log.debug("taskuid:"+getTaskuid());
			log.debug("getUid():"+getUid());
			log.debug("getCache():"+getCache());
			
			
			p=1;
			
	 	    Long last=(Long)getCache().get(getUid());
	 	    p=2;
			if(last==null || (new Date().getTime()-last)>100){
				p=3;
				if(new QueueFactory().getQueue(getTaskuid()).isRoomForThread()){
					p=4;
					h.put("okStart", getUid());
					p=5;
					IElementAttributes att= getCache().getDefaultElementAttributes();
					att.setMaxLifeSeconds(1);				
					p=6;
					getCache().put(getUid(), new Date().getTime(), att);
					p=7;
				}
			}else{
				h.put(IGNORE_CALLBACK, "");
			}
		}catch(Exception e){
			e.printStackTrace();
			log.error("Error executeAtDestination(), E:"+e.getMessage()+" p:"+p);
		}finally{
			releaseLock();
		}
		return h;
		
	}

	public synchronized void callBack(Map data) {
		
		log.debug("call back tender script:");
		log.debug("data:"+data+" okStart:"+data.get("okStart"));
		if(data.get("okStart")!=null){
			
			//log.debug("this.getUid():"+this.getUid());
			RScript rs=new ExecuteScript();
			
			
			rs.setUid(this.getUid());
			RScript started=LoadBalancingQueue.getExecuteRScriptDefault().startScriptIfNotStarted(rs,getMsgCreator());
			//log.debug("started:"+started);
			if(started!=null){
				//log.debug("received:"+data);
				ExecuteScript es=new ExecuteScript();				 
				es.setUid(started.getUid());	
				es.setTaskuid(this.getTaskuid());	
				es.setScript(started.getScript()); 
				new PostMessage(es,getMsgCreator()).send();				
				
			}
			rs=null;
			started=null;
		}
		
	}
	 protected JCS getCache() throws Exception {
		 if(cache==null){			    			    
				cache=JCS.getInstance("TenderScript");
				log.debug("cache:"+cache);
		 }
		log.debug("cache1:"+cache);
		 return cache;
	 }

	@Override
	public void onCallBackSendingFailed() {
		// TODO Auto-generated method stub
		log.error("postcallback failed, uid:"+this.uid);
	}

	@Override
	public void onSendingFailed() {
		// TODO Auto-generated method stub
		log.error("Sending failed.. script uid:"+this.uid);
	}
	
		 
	
	private void acquireLock() throws Exception{				
			TenderScript.lock.tryAcquire(TIMEOUT_MS, TimeUnit.MILLISECONDS);		
	}

	private void releaseLock(){		
			TenderScript.lock.release();
		
	}

}


