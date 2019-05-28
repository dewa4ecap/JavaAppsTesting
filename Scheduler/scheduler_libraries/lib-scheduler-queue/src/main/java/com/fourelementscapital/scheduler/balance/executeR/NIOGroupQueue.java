/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.balance.executeR;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.jcs.JCS;
import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fourelementscapital.scheduler.balance.LoadBalancingQueue;
import com.fourelementscapital.scheduler.error.ClientError;
import com.fourelementscapital.scheduler.io.msg.ServerExecuteScript;
import com.fourelementscapital.scheduler.io.request.IOExcecutors;
import com.fourelementscapital.scheduler.queue.QueueStack;
import com.fourelementscapital.scheduler.queue.QueueStackManager;
import com.fourelementscapital.scheduler.rscript.RScript;
import com.fourelementscapital.scheduler.rscript.RScriptListener;

public class NIOGroupQueue implements Callable {

	private Logger log = LogManager.getLogger(NIOGroupQueue.class.getName());
	
	private ConcurrentLinkedQueue<RScript>	scriptQueue= new ConcurrentLinkedQueue<RScript>();
	private ConcurrentLinkedQueue<RScript>	scriptQueueProcessing= new ConcurrentLinkedQueue<RScript>();	 
	private Hashtable<String, RScriptListener> scriptQueueListener=new Hashtable<String, RScriptListener>();
	
	//private  ExecutorService executorService= Executors.newCachedThreadPool();
	
	//private static ExecutorService dispatchService=Executors.newCachedThreadPool();  // Executors.newFixedThreadPool(20);
	//private static ExecutorService dispatchService=Executors.newFixedThreadPool(10);  // Executors.newFixedThreadPool(20);
	
	
	
	private Future<String> future=null;
	
	
	protected static Vector<RScript> scriptQueueVec=new Vector<RScript>(); 
	
	
	protected JCS cache=null;
	protected JCS cacheGrouped=null;
	
	private String uid=null;
	
	private NIOGroupQueue(String uid) {
		this.uid=uid;
	}
	
	private static ConcurrentHashMap<String,NIOGroupQueue> 	queueInstances=new ConcurrentHashMap<String,NIOGroupQueue>();
	
	public static NIOGroupQueue getGroupInstance(String s) {
		synchronized(queueInstances){
			if(queueInstances.containsKey(s)){
				return queueInstances.get(s);
			}else{
				NIOGroupQueue g =new NIOGroupQueue(s);
				queueInstances.put(s,g);
				return g;
			}
		}
	}
	
	public static Map<String,NIOGroupQueue> getAllGroupQueues(){
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
		//return executorService;
		return IOExcecutors.threadpool;
	}

	protected  Future<String> getFuture() {
		return future;
	}

	protected  String getUid() {
		return uid;
	}
	
	public void start(){
		//this.future=executorService.submit(this);
		this.future=IOExcecutors.threadpool.submit(this);
	}


	private long prevtime=0;
	public Object call() throws Exception {
	 
		log.debug("call() made");
		try{			
			while(!scriptQueue.isEmpty() ){		
				try{
					//if(!scriptQueue.isEmpty()){
					processScriptQueue();							
					//}
				}catch(Exception e){			
					e.printStackTrace();
				}				
				Thread.sleep(2);				
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
		    	   // Vector<Object> autclients1;
		    	    ArrayList al=new ArrayList();		    	    
		    	   
	    	    	if(rs.getExecuteAt()!=null){	    	    		 
	    	    		al.add(rs.getExecuteAt());
	    	    	}else{
	    	    		al.addAll(autclients);
	    	    	}
	    	    	
	    	    	if(al.size()>0){
	    	    		
		    	    	QueueStack qs=QueueStackManager.useNextAvailableQueue(al, rs.getTaskuid());
		    	    	log.debug("qs:"+qs+" rs.getTaskuid():"+rs.getTaskuid());    	    	
		    	    	 
		    	    	if(qs!=null){
			    	    	//Future fu=dispatchService.submit(
		    	    		Future fu=IOExcecutors.threadpool.submit(
			    	    			new Callable<RScript>() {
			    	    					public RScript call(){
			    	    						RScript started=null;
			    	    						if(qs!=null){
			    	    							started=LoadBalancingQueue.getExecuteRScriptDefault().startScriptIfNotStarted(rscript,qs.getPeername());		    	    		
			    				    	    		//send implementation will be done here..
			    				    	    		if(started!=null){
			    				    	    			ServerExecuteScript ses=new ServerExecuteScript();
			    				    	    			ses.setQueue_uid(qs.getUid());
			    				    	    			ses.setScript(rscript.getScript());
			    				    	    			ses.setTaskuid(rscript.getTaskuid());
			    				    	    			ses.setScript_uid(rscript.getUid());
			    				    	    			ses.send(qs.getPeername());
			    				    	    		}
			    				    	    	}
			    	    						return started;		    	    						
			    	    					}
			    	    					private RScript rscript=null;
			    	    					private QueueStack qs=null;
			    	    					public Callable<RScript> init(RScript rscript,QueueStack q){					 
			    	    						this.rscript=rscript;
			    	    						this.qs=q;
			    	    						return this;
			    	    					}
			    	    			}.init(rs, qs)
			    	    	);
			    	    	
		    	    		
		    	    		//behaves badly if
		    	    		//I removes the folloing block;
		    	    		//extensive test needed while removing the followings
	    	    			RScript rs1=null;
	    	    			try{
	    	    				rs1=(RScript)fu.get();	    	    			
	    	    			}catch(Exception e){
	    	    				log.error("error while retriveing future result");
	    	    				e.printStackTrace();
	    	    			}
		    	    	
		    	    	}
		    	    	 	 
		    	    	
		    	    	/*
		    	    	RScript started=LoadBalancingQueue.getExecuteRScriptDefault().startScriptIfNotStarted(rs,qs.getPeername());		    	    		
	    	    		//send implementation will be done here..
	    	    		if(started!=null){
	    	    			ServerExecuteScript ses=new ServerExecuteScript();
	    	    			ses.setQueue_uid(qs.getUid());
	    	    			ses.setScript(rs.getScript());
	    	    			ses.setTaskuid(rs.getTaskuid());
	    	    			ses.setScript_uid(rs.getUid());
	    	    			ses.send(qs.getPeername());
	    	    		}
	    	    	    */
    	    			
 	    	    	}
					autclients=null;
				 
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
			//
			scriptQueue.remove(rs);
			scriptQueueVec.remove(rs);
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


