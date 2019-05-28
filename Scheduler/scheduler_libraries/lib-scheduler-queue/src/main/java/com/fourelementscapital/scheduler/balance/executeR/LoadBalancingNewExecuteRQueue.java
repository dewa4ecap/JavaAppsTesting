/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.balance.executeR;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.fourelementscapital.scheduler.error.ClientError;
import com.fourelementscapital.scheduler.io.request.IOExcecutors;
import com.fourelementscapital.scheduler.p2p.websocket.TomcatWSServer;
import com.fourelementscapital.scheduler.rscript.RScript;
import com.fourelementscapital.scheduler.rscript.RScriptListener;

public class LoadBalancingNewExecuteRQueue extends LoadBalancingExecuteRQueue{

	private Logger log = LogManager.getLogger(LoadBalancingNewExecuteRQueue.class.getName());
	
	@Override
	public synchronized void addExecuteR(RScript item, RScriptListener listener) 	throws Exception {
		
		log.debug("adding script:"+item.getScript()+" uid:"+item.getUid());
		item.setExecuting(false);
		item.setQueued_time(new Date().getTime());
		
		String qid=getGroupUid(item);
		NIOGroupQueue gq=NIOGroupQueue.getGroupInstance(qid);

		//script with existing token id will not be added into the queue
		//instead the existing script with the same token will be 
		//replaced with new script and everything else remains as old.
		boolean added=false;
		RScript existing=null;
		for(Iterator<RScript> i=gq.getScriptQueue().iterator();i.hasNext();){
			RScript cur=i.next();
			if(cur.getUniquename().equals(item.getUniquename())){
				existing=cur;
			}
		}
		
		
		if(existing!=null){
			log.debug("script id:"+item.getUid()+" existing");			
			existing.setScript(item.getScript());
			gq.getCache().remove(item.getUid());
		}else{		
			gq.getScriptQueue().add(item);		
			NIOGroupQueue.scriptQueueVec.add(item);
			if(listener!=null) gq.getScriptQueueListener().put(item.getUid(), listener);
			added=true;			
		}
		
		IElementAttributes att= gq.getCache().getDefaultElementAttributes();
		att.setMaxLifeSeconds(CACHE_GROUP_EXPIRY);
		gq.getCache().put(item.getUid(), "alive", att);		
		
		if(gq.getFuture()==null || (gq.getFuture()!=null  && gq.getFuture().isDone())){			
			 gq.start();
		}
		if(added){
			toWSAdded(item);
		}
		
	}
	
	
	@Override
	protected synchronized  RScript startScriptIfNotStarted1(RScript rscript,String peer) {				 
		
		RScript rtn=NIOGroupQueue.scriptQueueVec.get(NIOGroupQueue.scriptQueueVec.indexOf(rscript));	
		String qid=getGroupUid(rtn);
		NIOGroupQueue gq=NIOGroupQueue.getGroupInstance(qid);
		
		if(gq.getScriptQueue().contains(rscript)){			
			gq.getScriptQueue().remove(rscript);							
			rtn.setPeer(peer);
			rtn.setDelay(new Date().getTime()- rtn.getQueued_time());
			rtn.setExecuting(true);
			rtn.setStartedtime(new Date());
			gq.getScriptQueueProcessing().add(rtn);
			
			if(gq.getScriptQueueListener().get(rtn.getUid())!=null){
				RScriptListener rslisten=gq.getScriptQueueListener().get(rtn.getUid());
				try{
					rslisten.onScriptSent(rtn, peer);					
				}catch(Exception e){
					ClientError.reportError(e, "Error while invoking listener");
				}
			}
			
			try{
				IElementAttributes att1= getGroupedCache().getDefaultElementAttributes();				
	 			att1.setMaxLifeSeconds(CACHE_GROUP_EXPIRY);
	 			getGroupedCache().putInGroup(peer, CACHE_GROUP_ACTIVEPEERS,"active", att1);	 
			}catch(Exception e){
				log.error("error while caching active peer");
			}
			toWSStarted(rscript.getUid(),peer);
			return rtn;
				
		}else{
				return null;
		}	 
	 }
	
	@Override
	public synchronized void scriptFinished(RScript rscript, String result,String status)  {		 
		
		
		
		
		RScript rscript1=NIOGroupQueue.scriptQueueVec.get(NIOGroupQueue.scriptQueueVec.indexOf(rscript));		
		String qid=getGroupUid(rscript1);
		
		
		
		NIOGroupQueue gq=NIOGroupQueue.getGroupInstance(qid);
		
		gq.getScriptQueueProcessing().remove(rscript);		
		
		SimpleDateFormat sdf=new SimpleDateFormat("dd-MM HH:mm:ss");
		Date d=new Date();
		d.setTime(rscript1.getQueued_time());
		
		 
		
		
		//log.debug("scriptFinished called:"+sdf.format(d)+" qid:"+qid);
		
		try{			 
			
 			String ky=rscript1.getPeer()+"_"+rscript1.getTaskuid();
 			rscript1.setError(rscript.getError());
 			//gq.cache=getCache(); 		
 			
 			IElementAttributes att= gq.getGroupedCache().getDefaultElementAttributes();
 			att.setMaxLifeSeconds(CACHE_GROUP_EXPIRY);
 			
 			 	
 			
 			
 			//gq.getGroupedCache().putInGroup(rscript1.getUid(), CACHE_GROUP_FINISHED,rscript1.getDelay(), att);
 			
 			 
 			

 			
 			//getGroupedCache().getGroupKeys(CACHE_GROUP_FINISHED); 			
 			//just to retrieve so that, expired won't in the memory
			//for(Object key: gq.getGroupedCache().getGroupKeys(CACHE_GROUP_FINISHED)){
			//	gq.getGroupedCache().getFromGroup(key,CACHE_GROUP_FINISHED);
			//}		
			
			
			
 			if(gq.getScriptQueueListener().get(rscript1.getUid())!=null){
 			
 				
				RScriptListener rslisten=gq.getScriptQueueListener().get(rscript1.getUid());
				try{					
					rslisten.onScriptFinished(rscript1, rscript1.getPeer(),result, status);					
				}catch(Exception e){
					ClientError.reportError(e, "Error while invoking listener");
				}
				gq.getScriptQueueListener().remove(rscript1.getUid());
				rslisten=null;
			} 				
 			gq.getCache().remove(ky);
			rscript1=null;
			toWSFinished(rscript.getUid());
			 
			
			
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{		
			NIOGroupQueue.scriptQueueVec.remove(rscript);
		}
		 
		
		rscript=null;
	}
	
	
	@Override
	public void removeScriptFromAllQueue(RScript rscript) {

		try{
			//getScriptProcessingQueue().remove(rscript);		
			
			RScript rscript1=NIOGroupQueue.scriptQueueVec.get(NIOGroupQueue.scriptQueueVec.indexOf(rscript));		
			String qid=getGroupUid(rscript1);
			NIOGroupQueue gq=NIOGroupQueue.getGroupInstance(qid);
			gq.getScriptQueueProcessing().remove(rscript1);
 			String ky=rscript1.getPeer()+"_"+rscript1.getTaskuid();
 			rscript1.setError(rscript.getError()); 			 			
 			gq.getCache().remove(ky);
 			NIOGroupQueue.scriptQueueVec.remove(rscript);				
			
		}catch(Exception e){			
			e.printStackTrace();
		}
		
		
	}

	
	@Override
	public Collection<RScript> getScriptQueue() throws Exception {
		
		ArrayList<RScript> list=new ArrayList<RScript>();
		for(NIOGroupQueue gp: NIOGroupQueue.getAllGroupQueues().values()){
			list.addAll(gp.getScriptQueue());
		}
		//java.util.Collections.sort(list);		
		return list;
		
	}

	@Override
	public Collection<RScript> getScriptProcessingQueue() throws Exception {
		ArrayList<RScript> list=new ArrayList<RScript>();
		for(NIOGroupQueue gp: NIOGroupQueue.getAllGroupQueues().values()){
			list.addAll(gp.getScriptQueueProcessing());
		}
		//java.util.Collections.sort(list);		
		return list;
	}
	

	public int getAllScriptObjectsSize() throws Exception {
	 
		return NIOGroupQueue.scriptQueueVec.size();
	}

	
 
	public Map getScriptListeners() throws Exception {
		HashMap list=new HashMap();
		for(NIOGroupQueue gp: NIOGroupQueue.getAllGroupQueues().values()){
			list.putAll(gp.getScriptQueueListener());
		}
		//java.util.Collections.sort(list);		
		return list;
	}
	
	
	@Override
	protected void toWSStarted(String uid, String peer){		
		
		
			Future fu=IOExcecutors.threadpool.submit(
					
	    			new Callable<String>() {
	    					public String call(){	   	    						 
	    					
	    						JSONObject jsonb=new JSONObject();
	    						HashMap data=new HashMap();
	    						data.put("uid", uid);
	    						data.put("peer", peer);
	    						try{
	    							jsonb.put("started", data);
	    							jsonb.put("executing_size", getScriptProcessingQueue().size());
	    							jsonb.put("queue_size", getScriptQueue().size());
	    						}catch(Exception e){
	    							e.printStackTrace();
	    						}
	    						//h.put("queueprocessing_size",getScriptProcessingQueue().size());
	    						TomcatWSServer.broadcast(jsonb.toString());
	    						return "";
	    					}
	    					private String uid;
	    					private String peer;
	    					public Callable<String> init(String u, String p){					 
	    						this.uid=u;
	    						this.peer=p;
	    						return this;
	    					}
	    			}.init(uid,peer)
    	    );
			
			
			
		
	}
	
	@Override
	protected void toWSFinished(String uid){		
		
		/*
		try{
			JSONObject jsonb=new JSONObject();
			HashMap data=new HashMap();
			data.put("uid", uid);			
			jsonb.put("finished", data);
			jsonb.put("executing_size", getScriptProcessingQueue().size());
			try{
				get30SecsData(jsonb);
			}catch(Exception e){
				log.error("Error while geting 30secs data, e:"+e.getMessage());
			}
			
			TomcatWSServer.broadcast(jsonb.toString());
			
		}catch(Exception e){
			e.printStackTrace();
		}
		*/
		
		Future fu=IOExcecutors.threadpool.submit(
				
    			new Callable<String>() {
    					public String call(){	   	    						 
    					
    						JSONObject jsonb=new JSONObject();
    						HashMap data=new HashMap();
    						data.put("uid", uid);
    						try{
	    						jsonb.put("finished", data);
	    						jsonb.put("executing_size", getScriptProcessingQueue().size());
	    						try{
	    							get30SecsData(jsonb);
	    						}catch(Exception e){
	    							log.error("Error while geting 30secs data, e:"+e.getMessage());
	    						}
    						}catch(Exception e){
    							log.error("toWSFinished()"+e.getMessage()); 
    						}
    						
    						TomcatWSServer.broadcast(jsonb.toString());
    						return "";
    					}
    					private String uid;
    				 
    					public Callable<String> init(String u){					 
    						this.uid=u;
    					 
    						return this;
    					}
    			}.init(uid)
	    );
		
		
	}
}


