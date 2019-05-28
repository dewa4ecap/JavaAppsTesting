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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.jcs.JCS;
import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.fourelementscapital.scheduler.balance.LoadBalancingQueue;
import com.fourelementscapital.scheduler.balance.LoadBalancingQueueItem;
import com.fourelementscapital.scheduler.balance.hsqldb.LoadBalancingHSQLQueue;
import com.fourelementscapital.scheduler.error.ClientError;
import com.fourelementscapital.scheduler.p2p.websocket.TomcatWSServer;
import com.fourelementscapital.scheduler.rscript.RScript;
import com.fourelementscapital.scheduler.rscript.RScriptListener;

public class LoadBalancingExecuteRQueue extends LoadBalancingQueue    {

	
	private Logger log = LogManager.getLogger(LoadBalancingExecuteRQueue.class.getName());
	 

	//private static ExecutorService startScriptService= Executors.newFixedThreadPool(3);
	private static ExecutorService startScriptService= Executors.newCachedThreadPool();
	
	
	protected String getGroupUid(RScript item){
		return item.getTaskuid()!=null?item.getTaskuid():"general";
	}
	
	
	@Override
	public void addExecuteR(RScript item, RScriptListener listener) 	throws Exception {
		
		log.debug("adding script:"+item.getScript());
		item.setExecuting(false);
		item.setQueued_time(new Date().getTime());
		
		String qid=getGroupUid(item);
		GroupQueue gq=GroupQueue.getGroupInstance(qid);
		
		gq.getScriptQueue().add(item);
		GroupQueue.scriptQueueVec.add(item);		
	
		IElementAttributes att= gq.getCache().getDefaultElementAttributes();
		att.setMaxLifeSeconds(CACHE_GROUP_EXPIRY);
		gq.getCache().put(item.getUid(), "alive", att);
		   
		if(listener!=null) gq.getScriptQueueListener().put(item.getUid(), listener);
		
		if(gq.getFuture()==null || (gq.getFuture()!=null  && gq.getFuture().isDone())){
			 gq.start();
		}
		
		toWSAdded(item);
		
		
		
	}

	

	/**
	 * making this single threaded queue.
	 * @see com.fe.scheduler.balance.LoadBalancingQueue#startedIfNotStarted(int, long, java.lang.String)
	 */
	public final RScript startScriptIfNotStarted(RScript rscript,String peer){
		
		
		
		Future fu=startScriptService.submit(
				
			new Callable<RScript>() {
					public RScript call(){
						//return new Integer(new LoadBalancingHSQLQueue().startedIfNotStarted1(sc_id,tri_time,peer));
						RScript rtn=null;
						if(LoadBalancingQueue.getDefault() instanceof LoadBalancingHSQLQueue){
							LoadBalancingExecuteRQueue lhq=(LoadBalancingExecuteRQueue)LoadBalancingQueue.getExecuteRScriptDefault();
							rtn=lhq.startScriptIfNotStarted1(this.rscript,this.peer);
							 
						}
						return rtn;
						
						
					}
					private RScript rscript=null;
					private String peer=null;
					public Callable<RScript> init(RScript rscript,String peer){					 
						this.rscript=rscript;
						this.peer=peer;
						return this;
					}
			}.init(rscript, peer)
		);
		
		RScript rs=null;
		try{
			rs=(RScript)fu.get();
		}catch(Exception e){
			log.error("error while retriveing future result");
			//e.printStackTrace();
		}
		return rs;
	}
	
	private RScript getRScript(Collection<RScript> col,RScript rscript) {
		RScript rtn=null;
		for(RScript rr:col){
			if(rr.equals(rscript)) rtn=rr;
		}		
		return rtn;
	}
	
	
	protected synchronized RScript startScriptIfNotStarted1(RScript rscript,String peer) {				 
		
		RScript rtn=GroupQueue.scriptQueueVec.get(GroupQueue.scriptQueueVec.indexOf(rscript));	
		String qid=getGroupUid(rtn);
		GroupQueue gq=GroupQueue.getGroupInstance(qid);
		
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

	
	
	public synchronized void scriptFinished(RScript rscript, String result,String status)  {		 
		
		
		RScript rscript1=GroupQueue.scriptQueueVec.get(GroupQueue.scriptQueueVec.indexOf(rscript));		
		String qid=getGroupUid(rscript1);
		GroupQueue gq=GroupQueue.getGroupInstance(qid);
		
		gq.getScriptQueueProcessing().remove(rscript);		
		
		SimpleDateFormat sdf=new SimpleDateFormat("dd-MM HH:mm:ss");
		Date d=new Date();
		d.setTime(rscript1.getQueued_time());
		log.debug("scriptFinished called:"+sdf.format(d)+" qid:"+qid);
		
		try{			 
			
 			String ky=rscript1.getPeer()+"_"+rscript1.getTaskuid();
 			rscript1.setError(rscript.getError());
 			//gq.cache=getCache(); 		
 			
 			IElementAttributes att= gq.getGroupedCache().getDefaultElementAttributes();
 			att.setMaxLifeSeconds(CACHE_GROUP_EXPIRY);
 			gq.getGroupedCache().putInGroup(rscript1.getUid(), CACHE_GROUP_FINISHED,rscript1.getDelay(), att);
 			//getGroupedCache().getGroupKeys(CACHE_GROUP_FINISHED); 			
 			//just to retrieve so that, expired won't in the memory
			for(Object key: gq.getGroupedCache().getGroupKeys(CACHE_GROUP_FINISHED)){
				gq.getGroupedCache().getFromGroup(key,CACHE_GROUP_FINISHED);
			}			
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
		}		
		GroupQueue.scriptQueueVec.remove(rscript);
		rscript=null;
	}
	
	
	@Override
	public void removeScriptFromAllQueue(RScript rscript) {

		try{
			//getScriptProcessingQueue().remove(rscript);		
			
			RScript rscript1=GroupQueue.scriptQueueVec.get(GroupQueue.scriptQueueVec.indexOf(rscript));		
			String qid=getGroupUid(rscript1);
			GroupQueue gq=GroupQueue.getGroupInstance(qid);
			gq.getScriptQueueProcessing().remove(rscript1);
 			String ky=rscript1.getPeer()+"_"+rscript1.getTaskuid();
 			rscript1.setError(rscript.getError()); 			 			
 			gq.getCache().remove(ky);
			GroupQueue.scriptQueueVec.remove(rscript);				
			
		}catch(Exception e){			
			e.printStackTrace();
		}
		
		
	}

	
	@Override
	public Collection<RScript> getScriptQueue() throws Exception {
		
		ArrayList<RScript> list=new ArrayList<RScript>();
		for(GroupQueue gp: GroupQueue.getAllGroupQueues().values()){
			list.addAll(gp.getScriptQueue());
		}
		//java.util.Collections.sort(list);		
		return list;
		
	}

	@Override
	public Collection<RScript> getScriptProcessingQueue() throws Exception {
		ArrayList<RScript> list=new ArrayList<RScript>();
		for(GroupQueue gp: GroupQueue.getAllGroupQueues().values()){
			list.addAll(gp.getScriptQueueProcessing());
		}
		//java.util.Collections.sort(list);		
		return list;
	}
	
	
 
	private Map getScriptListeners() throws Exception {
		HashMap list=new HashMap();
		for(GroupQueue gp: GroupQueue.getAllGroupQueues().values()){
			list.putAll(gp.getScriptQueueListener());
		}
		//java.util.Collections.sort(list);		
		return list;
	}
	
	
	public  Map<String, Integer> debug_data() throws Exception {
		HashMap<String, Integer> h=new HashMap<String, Integer>();
		h.put("scriptQueueListener_size",getScriptListeners().size());
		h.put("scriptQueueVec_size",GroupQueue.scriptQueueVec.size());
		h.put("scriptQueue_size",getScriptQueue().size());
		
		h.put("queue_size",getScriptQueue().size());
		h.put("queueprocessing_size",getScriptProcessingQueue().size());
		
		return h;
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	@Override
	public List<LoadBalancingQueueItem> getAllTasks() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long lastExcecutedTime() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public void removeItemProcessing(LoadBalancingQueueItem item,
			String message, int respCode) {
		// TODO Auto-generated method stub
		
	}

	
	@Override
	public void executeScript(Vector<Object> peers, int scriptid)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void peerStarted(int scheduler_id, long trigger_time, String peername)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void executionEnded(int schedulerid) {
		// TODO Auto-generated method stub
		
	}


	
	@Override
	public void add(LoadBalancingQueueItem item) {
		// TODO Auto-generated method stub
		
	}
	
	
	
	
	@Override
	public LoadBalancingQueueItem getItemFromProcessingQueue(int scheduler_id,
			long trigger_time) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean killQueuedTask(int scheduler_id, long trigger_time) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeFaultyProcessingTask(int scheduler_id,
			long trigger_time) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int startedIfNotStarted(int schedulerid, long trigger_time,
			String machinename) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void executionFailed(int schedulerid, long trigger_time,
			String machinename) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void executionEnded(int schedulerid, long trigger_time) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void cleanupProccesingQueue(int schedulerid, String computername) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Collection<LoadBalancingQueueItem> getExecutingTasks() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<LoadBalancingQueueItem> getQueuedTasks() {
		// TODO Auto-generated method stub
		return null;
	}

	
	
	
	
	
	protected void toWSAdded(RScript item){
		
		
		try{
			
			JSONObject jsonb=new JSONObject();
			Map data=BeanUtils.describe(item);
			jsonb.put("added", data);
			jsonb.put("queue_size", getScriptQueue().size());			
			TomcatWSServer.broadcast(jsonb.toString());
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	
	protected void toWSStarted(String uid, String peer){		
		
		try{
			JSONObject jsonb=new JSONObject();
			HashMap data=new HashMap();
			data.put("uid", uid);
			data.put("peer", peer);
			
			jsonb.put("started", data);
			jsonb.put("executing_size", getScriptProcessingQueue().size());
			jsonb.put("queue_size", getScriptQueue().size());
			
			//h.put("queueprocessing_size",getScriptProcessingQueue().size());
			TomcatWSServer.broadcast(jsonb.toString());
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	protected void toWSFinished(String uid){		
		
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
		
	}

	protected void toWSTimedout(String uid){		
		
		try{
			JSONObject jsonb=new JSONObject();
			HashMap data=new HashMap();
			data.put("uid", uid);		
			jsonb.put("timedout", data);
			try{
				get30SecsData(jsonb);
			}catch(Exception e){
				log.error("Error while geting 30secs data, e:"+e.getMessage());
			}
			TomcatWSServer.broadcast(jsonb.toString());
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	
	protected void get30SecsData(JSONObject jsonb) throws Exception  {

		if(getCache().get("WS30SecData")==null){
						
			JCS gjcs=getGroupedCache();

			//just to retrieve so that, expired won't in the memory
			int counter=0;
			long totaldelay=0;
			for(Object key: gjcs.getGroupKeys(LoadBalancingQueue.CACHE_GROUP_FINISHED)){
				Long delay=(Long)gjcs.getFromGroup(key,LoadBalancingQueue.CACHE_GROUP_FINISHED);
				if(delay!=null){
					totaldelay=totaldelay+delay;	
					counter++;
				}
			}
			log.debug("totaldelay:"+totaldelay+" counter:"+counter);
			long ave_delay=(counter>0 && totaldelay>0)?(totaldelay/counter)/1000:0;
			
			//just to retrieve so that, expired won't in the memory			
			for(Object key: gjcs.getGroupKeys(LoadBalancingQueue.CACHE_GROUP_TIMEOUT)){
				gjcs.getFromGroup(key,LoadBalancingQueue.CACHE_GROUP_TIMEOUT);
			}
			
			for(Object key: gjcs.getGroupKeys(LoadBalancingQueue.CACHE_GROUP_FINISHED)){
				gjcs.getFromGroup(key,LoadBalancingQueue.CACHE_GROUP_FINISHED);
			}
			for(Object key: gjcs.getGroupKeys(LoadBalancingQueue.CACHE_GROUP_ACTIVEPEERS)){
				gjcs.getFromGroup(key,LoadBalancingQueue.CACHE_GROUP_ACTIVEPEERS);
			}
			 
			
			Set finished=gjcs.getGroupKeys(LoadBalancingQueue.CACHE_GROUP_FINISHED);
			Set timedout=gjcs.getGroupKeys(LoadBalancingQueue.CACHE_GROUP_TIMEOUT);
			Set activePeers=gjcs.getGroupKeys(LoadBalancingQueue.CACHE_GROUP_ACTIVEPEERS);
			
			HashMap h=new HashMap();
			h.put("ave_delay", (ave_delay>0?ave_delay:0));	 		
			h.put("active_peers", activePeers.size());
			h.put("finished_count", finished.size());			
			h.put("timedout_count", timedout.size());
			
			jsonb.put("ave_delay", (ave_delay>0?ave_delay:0));	 		
			jsonb.put("active_peers", activePeers.size());
			jsonb.put("finished_count", finished.size());			
			jsonb.put("timedout_count", timedout.size());
			
			
			IElementAttributes att=getCache().getDefaultElementAttributes();
			att.setMaxLifeSeconds(2);
			getCache().put("WS30SecData",h,att);			
			 		
		}else{
			Map h=(Map)getCache().get("WS30SecData");
			
			for(Iterator i=h.keySet().iterator();i.hasNext();){
				String ky=(String)i.next();
				jsonb.put(ky, h.get(ky));
			}
			 
		}
		
	}
	

}


