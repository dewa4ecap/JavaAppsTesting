/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.balance.executeR;

import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.jcs.JCS;
import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mozilla.javascript.edu.emory.mathcs.backport.java.util.Collections;

import com.fourelementscapital.scheduler.balance.LoadBalancingQueue;
import com.fourelementscapital.scheduler.error.ClientError;
import com.fourelementscapital.scheduler.p2p.listener.OutgoingMessageCallBack;
import com.fourelementscapital.scheduler.p2p.msg.PostMessage;
import com.fourelementscapital.scheduler.p2p.msg.impl.TenderScript;
import com.fourelementscapital.scheduler.rscript.RScript;
import com.fourelementscapital.scheduler.rscript.RScriptListener;

public class GroupQueue implements Callable {
	
	private Logger log = LogManager.getLogger(GroupQueue.class.getName());
	
	private ConcurrentLinkedQueue<RScript>	scriptQueue= new ConcurrentLinkedQueue<RScript>();
	private ConcurrentLinkedQueue<RScript>	scriptQueueProcessing= new ConcurrentLinkedQueue<RScript>();	 
	private Hashtable<String, RScriptListener> scriptQueueListener=new Hashtable<String, RScriptListener>();
	
	private  ExecutorService executorService= Executors.newSingleThreadScheduledExecutor();	
	private Future<String> future=null;
	
	
	protected static Vector<RScript> scriptQueueVec=new Vector<RScript>(); 
	
	
	protected JCS cache=null;
	protected JCS cacheGrouped=null;
	
	private String uid=null;
	
	private GroupQueue(String uid) {
		this.uid=uid;
	}
	
	private static ConcurrentHashMap<String,GroupQueue> 	queueInstances=new ConcurrentHashMap<String,GroupQueue>();
	
	protected static GroupQueue getGroupInstance(String s) {
		synchronized(queueInstances){
			if(queueInstances.containsKey(s)){
				return queueInstances.get(s);
			}else{
				GroupQueue g =new GroupQueue(s);
				queueInstances.put(s,g);
				return g;
			}
		}
	}
	

	public static Map<String,GroupQueue> getAllGroupQueues(){
		return queueInstances;
	}
	
	
	protected ConcurrentLinkedQueue<RScript> getScriptQueue() {
		return scriptQueue;
	}

	protected ConcurrentLinkedQueue<RScript> getScriptQueueProcessing() {
		return scriptQueueProcessing;
	}

	protected  Hashtable<String, RScriptListener> getScriptQueueListener() {
		return scriptQueueListener;
	}

	protected  ExecutorService getExecutorService() {
		return executorService;
	}

	protected  Future<String> getFuture() {
		return future;
	}

	protected  String getUid() {
		return uid;
	}
	
	public void start(){
		this.future=executorService.submit(this);
	}


	
	public Object call() throws Exception {
	 
		log.debug("call() made");
		try{			
			while(!scriptQueue.isEmpty() ){		
				try{
					if(!scriptQueue.isEmpty()){
							processScriptQueue();							
					}
				}catch(Exception e){				
					log.error("error while processing queue: err: "+e.getMessage());
				}				
				Thread.sleep(5);				
			} //while loop
			
		}catch(Exception e){			
			log.error("loadbalancing queue thread terminiated: e: "+e.getMessage());
			e.printStackTrace();			
		}
		
		return "done";
	}
	
	
	private Vector roundRobin=new Vector();
	
	private void processScriptQueue() throws Exception{
		

		   RScript rs=scriptQueue.peek();
		   
		   if(!rs.isExecuting()){
			   
			    cache=getCache();
			    
			    if(getCache().get(rs.getUid())==null){
			    	
			    	IElementAttributes att1= getGroupedCache().getDefaultElementAttributes();
			    	
		 			att1.setMaxLifeSeconds(LoadBalancingExecuteRQueue.CACHE_GROUP_EXPIRY);
		 			getGroupedCache().putInGroup(rs.getUid(), LoadBalancingExecuteRQueue.CACHE_GROUP_TIMEOUT,"timeout", att1);	 			
		 			//log.debug("adding into timeoud cache...------------>");
			    	scriptTimedOut(rs);			    	
			    	//just to retrieve		    	 
					for(Object key: getGroupedCache().getGroupKeys(LoadBalancingExecuteRQueue.CACHE_GROUP_TIMEOUT)){
						getGroupedCache().getFromGroup(key,LoadBalancingExecuteRQueue.CACHE_GROUP_TIMEOUT);
					}
					
			    }else{
		    	    Vector<Object> autclients=LoadBalancingQueue.getExecuteRScriptDefault().getRunOnlyOn(rs.getTaskuid());
		    	    Vector<Object> autclients1;
		    	    if(rs.getExecuteAt()!=null){
		    	    	autclients1=new Vector<Object>();
		    	    	if(autclients.contains(rs.getExecuteAt())){
		    	    		autclients1.add(rs.getExecuteAt());
		    	    	}	    	    	
		    	    }else{
		    	    	//autclients1=(Vector<Object>)autclients.clone();	    	    	
		    	    	//round robin load balance of peers list  	    	
		    	    	for(Iterator i=autclients.iterator();i.hasNext();){
		    	    		String peer=(String)i.next();
		    	    		if(!roundRobin.contains(peer)){
		    	    			roundRobin.add(peer);
		    	    		}
		    	    	}
		    	    	
		    	    	autclients1=new Vector<Object>();
		    	    	for(Iterator i=roundRobin.iterator();i.hasNext();){
		    	    		String peer=(String)i.next();
		    	    		if(autclients.contains(peer)){
		    	    			autclients1.add(peer);
		    	    		}
		    	    	}
		    	    	 
		    	    	
		    	    }
					//log.debug("authorized cients found:"+autclients1 +" for taskuid:"+rs.getTaskuid());
					if(autclients1!=null && autclients1.size()>0){
						//log.debug("autclients1:"+autclients1);
					 	for(Iterator<Object> i=autclients1.iterator();i.hasNext();){		
		
					 		String clientname=(String)i.next();
					 		String ky=clientname+"_"+rs.getTaskuid();
					 		//sends only 1 post message in a second 
					 	   
					 		
					 	    IElementAttributes att= cache.getDefaultElementAttributes();
					 	    att.setMaxLifeSeconds(1);
					 	   
					 	    Long last=(Long)cache.get(ky);
					 	    
					 		if(last==null || (new Date().getTime()-last)>100 ){
					 			
						 		TenderScript ts=new TenderScript();
						 		ts.setPriority(OutgoingMessageCallBack.PRIORITY_LOW );
						 		ts.setUid(rs.getUid());						 		
						 		ts.setTaskuid(rs.getTaskuid());
						 		
						 		PostMessage ps=new PostMessage(ts,clientname);
						 		ps.send();						 		
						 		cache.put(ky, new Date().getTime(),att);
						 		ts=null;
						 		ps=null;
						 		log.debug("sending to :"+clientname);
					 		}
					 	}
					}
					if(!roundRobin.isEmpty()){
	    	    		Collections.rotate(roundRobin, -1);
	    	    	}
					autclients=null;
					autclients1=null;
			    }
		    }
		    rs=null; 
	}
	
	private synchronized void scriptTimedOut(RScript rs){
		try{
			if(scriptQueueListener.get(rs.getUid())!=null){
				RScriptListener rslisten=scriptQueueListener.get(rs.getUid());
				try{
					
					LoadBalancingExecuteRQueue lhq=(LoadBalancingExecuteRQueue)LoadBalancingQueue.getExecuteRScriptDefault();					
					rslisten.onScriptTimedOut(rs);
					
					lhq.toWSTimedout(rs.getUid());
					
				}catch(Exception e){
					ClientError.reportError(e, "Error while invoking listener");				
				}finally{
					scriptQueueListener.remove(rs.getUid());
				}
			}
		}catch(Exception e){
			log.error("Error while time out");			
		}finally{
			//scriptQueueVec.remove(rs);
			scriptQueue.remove(rs);
		}
	}
	
		
	 protected JCS getCache() throws Exception {
		 if(cache==null){
				cache=JCS.getInstance("LoadBalancingQueue");
		 }
		 return cache;
	 }
	 
	 public JCS getGroupedCache() throws Exception {
		 if(cacheGrouped==null){
			 cacheGrouped=JCS.getInstance("LoadBalancingQueueGroupped");
		 }
		 return cacheGrouped;
	 }	
	 
	 
	
}


