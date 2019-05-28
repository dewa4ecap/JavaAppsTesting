/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.balance;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.PipeService;
import net.jxta.protocol.PipeAdvertisement;

import org.apache.jcs.JCS;
import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleTrigger;
import org.quartz.impl.StdSchedulerFactory;

import com.fourelementscapital.db.SchedulerDB;
import com.fourelementscapital.scheduler.ScheduledTaskFactory;
import com.fourelementscapital.scheduler.ScheduledTimeoutJob;
import com.fourelementscapital.scheduler.SchedulerEngine;
import com.fourelementscapital.scheduler.alarm.SchedulerAlarm;
import com.fourelementscapital.scheduler.alarm.SchedulerAlarmVO;
import com.fourelementscapital.scheduler.balance.executeR.LoadBalancingNewExecuteRQueue;
import com.fourelementscapital.scheduler.balance.hsqldb.LoadBalancingHSQLQueue;
import com.fourelementscapital.scheduler.balance.hsqldb.LoadBalancingHSQLQueueItem;
import com.fourelementscapital.scheduler.engines.ScheduledTask;
import com.fourelementscapital.scheduler.engines.SchedulerExePlanLogs;
import com.fourelementscapital.scheduler.exception.ExceptionExecutionTimeout;
import com.fourelementscapital.scheduler.exception.ExceptionSchedulerTeamRelated;
import com.fourelementscapital.scheduler.p2p.MessageBean;
import com.fourelementscapital.scheduler.p2p.P2PAdvertisement;
import com.fourelementscapital.scheduler.p2p.P2PService;
import com.fourelementscapital.scheduler.p2p.listener.IncomingMessage;
import com.fourelementscapital.scheduler.p2p.listener.IncomingMessageParser;
import com.fourelementscapital.scheduler.p2p.listener.OutgoingMessage;
import com.fourelementscapital.scheduler.p2p.listener.P2PTransportMessage;
import com.fourelementscapital.scheduler.p2p.msg.PostMessage;
import com.fourelementscapital.scheduler.p2p.msg.scheduler.PeerOnlineStatus;
import com.fourelementscapital.scheduler.p2p.msg.scheduler.TenderSchedulerTask;
import com.fourelementscapital.scheduler.rscript.RScript;
import com.fourelementscapital.scheduler.rscript.RScriptListener;

/**
 * This class is abstract for all load balancing algorithms,
 * For example there are some commmon events for all queue processing, 
 * 
 * 
 * Some queue implementations for example queue implementation for ExecuteR server is not necessary to implement 
 * 
 * 
 * ***** Scheduler Task queue events ****
 * add()
 * startedIfNotStarted()
 * executionEnded()
 * taskTimedOut()
 * getQueuedTasks()
 * getItemFromProcessingQueue()
 * getAllTasks()
 * removeItemProcessing()
 * killQueuedTask()
 * executionFailed()
 * cleanupProcessingQueue()
 * 
 * 
 * ******* ExecuteR Script queue events 
 * addExecuteRScript()
 * startedScriptIfNotStarted()
 * scriptFinished()
 * getScriptQueue()
 * getScriptProcessingQueue()
 * 
 * 
 * also other common events
 * peerStarted()
 * updatePeerData()
 * findAndUpdateOnlinePeers()
 * 
 * 
 * 
 * 
 *
 */
public abstract class LoadBalancingQueue {
	
	private static Logger log = LogManager.getLogger(LoadBalancingQueue.class.getName());
	
	//if your queue management implementation support scheduler task and 
	//executeR then it is necessary to explicitly set mode, otherwise it is not nessary	
	private final static int MODE_TASK=1;
	private final static int MODE_SCRIPT=2;
	

	private static LoadBalancingQueue loadBalancingQ=null;
	private static LoadBalancingQueue loadERBalancingQ=null;
	public static boolean priorityQueue=false;
	public String PEER_QUEUE_RESP="PEER_QUEUE_RESP";
	
	
	//java caching and attributes constants
	protected JCS cache=null;
	protected JCS cacheGrouped=null;	
	public static String CACHE_GROUP_TIMEOUT="cached_timedout";
	public static String CACHE_GROUP_FINISHED="cached_finished";
	public static String CACHE_GROUP_ACTIVEPEERS="cached_activepeers";
	public static String CACHE_GROUP_TENDERSCHEDULERTASK="cached_tenderschedulertask";	
	public static int    CACHE_GROUP_EXPIRY=45;	
	
	
	protected Vector<String> shuffleIteration=new Vector<String>();
	private static Semaphore lock=new Semaphore(1,true);
	
	public static synchronized LoadBalancingQueue getDefault(){
		try{
			lock.tryAcquire(1000,TimeUnit.MICROSECONDS);
			if(LoadBalancingQueue.loadBalancingQ==null){				
				//LoadBalancingQueue.loadBalancingQ=new LoadBalancingLinkedQueue(MODE_TASK);;
				LoadBalancingQueue.loadBalancingQ=new LoadBalancingHSQLQueue();;
			}
			lock.release();
			
		}catch(Exception e){
			log.error("Error while acquiring lock");
		}
		return LoadBalancingQueue.loadBalancingQ;
			
	}
	
	 
	/**
	 * Get current default implementation for scheduler queue mangement
	 * @return
	 */
	public static synchronized LoadBalancingQueue getHSQLQueue(){
		
		if(LoadBalancingQueue.loadBalancingQ==null){				
			LoadBalancingQueue.loadBalancingQ=new LoadBalancingHSQLQueue();				
		}
		return LoadBalancingQueue.loadBalancingQ;
	}
	

	/**
	 * Get current default implementation for executeR queue management.
	 * @return
	 */
	public static synchronized LoadBalancingQueue getExecuteRScriptDefault(){
		
			if(LoadBalancingQueue.loadERBalancingQ==null){
				LoadBalancingQueue.loadERBalancingQ=new LoadBalancingNewExecuteRQueue();				
			}
			return LoadBalancingQueue.loadERBalancingQ;		
	}
	
	protected Vector getPeers4PriorityGr(String peer_query, String taskuid) throws Exception {	
		 	
		 
		    if(cache==null){
				//cache=JCS.getInstance("perminentpeers");
		    	getCache();
			}	
		 	IElementAttributes att= cache.getDefaultElementAttributes();
			att.setMaxLifeSeconds(4);
		 	Vector peers=(Vector)cache.get("peers_priority_available");
		 	
		 	if(peers!=null && peers.size()>0) {Object obj=peers.get(0);} //keep the obj in memory even after it expires.
		 	
			if(peers==null){
				SchedulerDB sdb=SchedulerDB.getSchedulerDB();
				try{
					sdb.connectDB();
					peers=sdb.getAvailablePeers(peer_query, taskuid);
					cache.put("peers_priority_available",peers,att);
				}catch(Exception e){ log.error("ERROR:"+e.getMessage()+" Peer Query:"+peer_query+" taskuid:"+taskuid);}
				finally{
					sdb.closeDB();		
				}
			}
			 
			return peers;
	} 
	 
	 public void releasePeersCache4PriorityGr()  {	
			if(cache!=null && cache.get("peers_priority_available")!=null){
				try{
					cache.remove("peers_priority_available");
				}catch(Exception e){
				   
				}
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
	 
	 
	 public Vector<Object> getRunOnlyOn(String taskuid) {
			
			try{		

	    			Vector<Object> rtn=(Vector<Object>)getCache().get("available_p_"+taskuid);   			
	    			
	    			if(rtn!=null && rtn.size()>0){Object obj=rtn.get(0);  }  //to keep the object in memory even after it expires.
	    			
	    			if(rtn==null){    				
	    				SchedulerDB sdb=SchedulerDB.getSchedulerDB();
	    				sdb.connectDB();
	    				try{
	    					rtn=sdb.getAssoAvailablePeers(taskuid);
	    				}finally{
	    					sdb.closeDB();
	    				}  				
	        			if(getCache()==null) getCache();
	        			IElementAttributes att= getCache().getDefaultElementAttributes();
	        			att.setMaxLifeSeconds(5);
	        			getCache().put("available_p_"+taskuid,rtn,att);
	        			
	    			}
	    			return rtn;
	    			
			}catch(Exception e){
				//ClientErrorMgmt.reportError(e, null);
				log.error("Error in retrieving associated peers ");
				return new Vector<Object>();
			}finally{			
				
			}
	}
	 
	 protected synchronized void  sendTask2Peer(Vector<Object> peers,int scheduler_id, long trigger_time, long nextTrigger_time, String taskuid ) throws Exception {
		 
		   
		    boolean first=true;		    		   
			Vector<String> v2=(Vector<String>)shuffleIteration.clone();				
			Vector<Object> v3=new Vector<Object>();
			for(Iterator<String> i=v2.iterator();i.hasNext();){
				Object ob=i.next();
				if(peers.contains(ob)){v3.add(ob);};
			}
			peers.removeAll(v3);
			peers.addAll(v3);			
			// StackFrame sframe=currentItem.getSf();		
		 	boolean firstElement=true;
		 	boolean sent=false;
			
		 	for(Iterator<Object> i=peers.iterator();i.hasNext();){	 		
		 		
		 			String clientname=(String)i.next();	
					TenderSchedulerTask tst=new TenderSchedulerTask();
					tst.setScheduler_id(scheduler_id+"");
					tst.setTrigger_time(trigger_time+"");
					tst.setNext_trigger_time(nextTrigger_time+"");
					tst.setTaskuid(taskuid);
					
					
					String iden=scheduler_id+"_"+trigger_time;
					
					//add into cache so it server doesn't have to answer tender respond call once it is started.
					if(getGroupedCache().getFromGroup(iden, LoadBalancingQueue.CACHE_GROUP_TENDERSCHEDULERTASK)!=null){
						getGroupedCache().remove(iden, LoadBalancingQueue.CACHE_GROUP_TENDERSCHEDULERTASK);
					}
					IElementAttributes att1= getGroupedCache().getDefaultElementAttributes();				
		 			att1.setMaxLifeSeconds(CACHE_GROUP_EXPIRY);
		 			getGroupedCache().putInGroup(iden, LoadBalancingQueue.CACHE_GROUP_TENDERSCHEDULERTASK, "0");
		 			//
					
					new PostMessage(tst,clientname).send();
					//Debugger.addDebugMsg("Msg to peer "+clientname+ " sc_id:"+ scheduler_id+" tr_time: "+trigger_time,clientname+ " "+ scheduler_id+" "+trigger_time);					
					//log.debug("Sending to message to peer:"+clientname);
					if(first){
				    	if(shuffleIteration.contains(clientname)){
				    		shuffleIteration.remove(clientname);
				    	}
				    	shuffleIteration.add(clientname);
				    }
				    first=false;					 
			      	Thread.sleep(5);
		 	}
		 	v2=null;				
			v3=null;
	         
	   }
	
	 
	 public final void updatePeerData(String what) throws Exception {
		 
		    //IncomingMessage.getMessages().clear();
		 	PeerGroup netPeerGroup=P2PService.getPeerGroup();
		  
			MessageBean mb=new MessageBean();
			mb.setType(MessageBean.TYPE_REQUEST);
			mb.setReply(MessageBean.REPLYBACK);
			mb.setCommand(what);		 	
			Vector peers=getStaticClients();
			
		 	for(Iterator i=peers.iterator();i.hasNext();){
		 		
		 		String clientname=(String)i.next();;
		 		PipeAdvertisement pipeAdv = new P2PAdvertisement().getPipeAdvertisement(clientname,netPeerGroup);		 		
				OutgoingMessage ogM=new OutgoingMessage(null,mb,clientname);				
				PipeService pipeService = P2PService.getPipeService();	
				try{
					pipeService.createOutputPipe(pipeAdv,ogM);
				}catch(Exception e){
					e.printStackTrace();
				}
		 	}

		 	
		 	//if(what.equals(STATISTICS)){
		 	if(what.equals(P2PTransportMessage.COMMAND_STATISTICS)){	
		 	//update server's statistics 
		 		IncomingMessage.updatePeerStatistics(P2PService.getComputerName(),IncomingMessageParser.getServerStatistics());
		 	}
		 	//if(what.equals(PEER_QUEUE)){
		 	if(what.equals(P2PTransportMessage.COMMAND_PEER_QUEUE)){	
			 	//update server's statistics 
			 		IncomingMessage.updatePeerQueueStat(P2PService.getComputerName(),IncomingMessageParser.getServerPeerQueueStat());
			 }
		 	
		 	//if(what.equals(R_PACKAGES)){
		 	if(what.equals(P2PTransportMessage.COMMAND_R_PACKAGES)){
			 	//update server's package information 
		 		try{
			 		//IncomingMessage.updatePeerRPackages(P2PService.getComputerName(),RScriptScheduledTask.getRPackageVersion());
		 			IncomingMessage.updatePeerRPackages(P2PService.getComputerName(),null);
		 		}catch(Exception e){
		 			//ClientErrorMgmt.reportError(e, null);
		 			throw e;
		 		}
		 	}
	 }
	 
	 
	 private  Vector getStaticClients() throws Exception {
		 	if(cache==null){
				//cache=JCS.getInstance("perminentpeers");
		 		getCache();
			}	 
			IElementAttributes att= cache.getDefaultElementAttributes();
			att.setMaxLifeSeconds(7);
			Vector peers1=(Vector)cache.get("perminentpeers");
			
			if(peers1!=null && peers1.size()>0) {Object obj=peers1.get(0);}  //to keep the object in memory even after cache expires.
			
			if(peers1==null){
				SchedulerDB sdb=SchedulerDB.getSchedulerDB();
				sdb.connectDB();
				try{				
					peers1=sdb.getPeersList();					
				}finally{
					sdb.closeDB();
				}
				peers1.remove(P2PService.getComputerName());
				//System.out.println("SchedulerMgmt.getOnlinePeers():peers:storing:"+peers1);
				cache.put("perminentpeers",peers1,att);			
				 //System.out.println("storing...");
			}
		    return peers1;
	 }
	 
	 public final void findAndUpdateOnlinePeers() throws Exception {
			
			Vector peers=getStaticClients();			
		 	for(Iterator i=peers.iterator();i.hasNext();){		 		
		 		String clientname=(String)i.next();;
		 		PeerOnlineStatus pos=new PeerOnlineStatus();	 							 		 
		 		PostMessage ps=new PostMessage(pos,clientname);
		 		ps.send();		 		
		 	}
		 	
	 }

	 public boolean isPeerBusyWithTask(String machinename,int bid_scheduler_id)  {
			//1 started at this call
			//0 is not started at this call
			//-1 invalid
			boolean rtn=false;
			SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		
			try{
				Vector<LoadBalancingQueueItem> nonresp=new Vector<LoadBalancingQueueItem>();
				Collection queuep= getExecutingTasks();
				sdb.connectDB();	
				for(Iterator<LoadBalancingQueueItem> i=queuep.iterator();i.hasNext();){
					
					LoadBalancingQueueItem item=i.next();
					long trigger_time=0;
					if(item instanceof LoadBalancingHSQLQueueItem) {
						trigger_time=((LoadBalancingHSQLQueueItem) item).getTrigger_time();
					}else{
						trigger_time=item.getSf().getTrigger_time();
					}
					if(item.getMachine().equals(machinename)){	
						//nonresp.add(item);
						//checks if bid response id and executing queque scheduler id is not the same, that means peer could have completed or crashed the 
						//previous task that sent to the peer.
						
						if(item.getSchedulerid()!=bid_scheduler_id){						
							Map data=sdb.getQueueLog(item.getSchedulerid(),trigger_time);
			    			if(data.get("status")!=null && data.get("host")!=null && data.get("end_time")!=null){
			    				removeItemProcessing(item,"LoadBalancingQueue removing it from processing Queue",SchedulerExePlanLogs.SERVER_ERROR_PEER_CRASHED_REMOVED_QUEUE);
			    			}else{
			    				item.getSf().setTasklog("No response from "+machinename+" for executed task, probably peer could have been crashed");
			    				item.getSf().setDependencyfailed(true);
			    				item.setExecuting(false);
			    				removeItemProcessing(item,"No response from "+machinename+" for executed task, probably peer could have been crashed",SchedulerExePlanLogs.SERVER_ERROR_PEER_NORESP_REMOVED_QUEUE);
			    			}	    			
						}else{
							rtn=true;	
						}
						 
					}
				}	
				
				/*
				if(nonresp.size()>0){
					rtn=true;	
				}
				for(Iterator<LoadBalancingQueueItem> i=nonresp.iterator();i.hasNext();){
					LoadBalancingQueueItem item=i.next();
					Map data=sdb.getQueueLog(item.getSchedulerid(),item.getSf().getTrigger_time());
	    			if(data.get("status")!=null && data.get("host")!=null && data.get("end_time")!=null){
	    				removeItemProcessing(item);
	    			}else{
	    				item.getSf().setTasklog("No response from "+machinename+" for executed task, probably peer could have been crashed");
	    				item.getSf().setDependencyfailed(true);
	    				item.setExecuting(false);
	    				removeItemProcessing(item);
	    			}	
				}
				*/
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				try{
					sdb.closeDB();
				}catch(Exception e){ }
			}
			return rtn;
		}
	 
	 
	protected final void removeTimeoutForTask(int scheduler_id, long trigger_time) {
			try{
				SchedulerFactory sf = new StdSchedulerFactory();
				Scheduler scheduler = sf.getScheduler();		
				if(scheduler.isStarted()){
					String job_tri_name=getTimeoutJobName(scheduler_id,trigger_time);
					 
					boolean isdeleted=scheduler.deleteJob(new JobKey(job_tri_name,SchedulerEngine.SCHEDULE_TASK_TIMEOUT_GROUP));
					if(isdeleted){
						//System.out.println("LoadBalancingQueue:removing timeout into queue scheduler_id:"+scheduler_id);
					}
				}

			}catch(Exception e){
				//System.out.println("LoadBalancingQueue:removing timeout into queue"+"error while removing timeout for the task, error:"+e.getMessage());
				log.error("error while removing timeout for the task, error:"+e.getMessage()+" scheduler_id:"+scheduler_id);
			}
	}
	
	
	protected void addLastExecutionDuration(SchedulerDB sdb, LoadBalancingQueueItem item) throws Exception {
		try{
			Map record=sdb.getLastSuccessfulQLog(item.getSchedulerid()) ;			
			if(record!=null){    			
				Timestamp s=(Timestamp)record.get("start_time");
				Timestamp e=(Timestamp)record.get("end_time");				
				long his_dura=e.getTime()-s.getTime();
				item.setLastExecutedDuration(his_dura);				
			}
			
		}catch(Exception e){
			log.error("addLastExecutionDuration():"+e.getMessage());
		}
	}
	
	protected void addTimeoutAndLastExecTimes(SchedulerDB sdb, LoadBalancingQueueItem item) throws Exception {
		
		
		
		
		try{
			long timeout=sdb.getMaxDurationInLast50Exec(item.getSchedulerid());
			
			item.setOverlaptimeout(timeout);
			
			LoadBalancingQueueTimeout tqt=new LoadBalancingQueueTimeout(sdb,new ScheduledTaskFactory().getTaskUids());
			
			 
//			if(tqt.getCriteriaQuery()!=null && !tqt.getCriteriaQuery().trim().equals("")) {
			
				
			// move the query inside this method
				long tc=sdb.getTimeoutCriteriaInMs(item.getSchedulerid());					
			
				if(tc>0){
					int mins=Math.round(tc/1000/60);							
					if(mins<=tqt.getFewerminutes()){
						item.setTimeoutexpiry(tqt.getFewerminutesexpiry()*1000*60);
					}else{
						item.setTimeoutexpiry(tc*tqt.getElsecritieriaxtime());
					}
				} 
				 
			
//			}
			
		}catch(Exception e){
			e.printStackTrace();
			//System.out.println("~~~~ LoadBalancingQueue.class Error 21:"+e.getMessage());
		}
	}
	
	protected synchronized final void addTimeoutForTask(int scheduler_id, long trigger_time, long started_time, long expiry_time)  {
		try{
			SchedulerFactory sf = new StdSchedulerFactory();
			Scheduler scheduler = sf.getScheduler();
			if(scheduler.isStarted() && expiry_time>0){

				String job_tri_name=getTimeoutJobName(scheduler_id,trigger_time);
				 
				JobDetail ojob=scheduler.getJobDetail(new JobKey(job_tri_name,SchedulerEngine.SCHEDULE_TASK_TIMEOUT_GROUP));
				if(ojob!=null){						 
					 scheduler.deleteJob(new JobKey(job_tri_name,SchedulerEngine.SCHEDULE_TASK_TIMEOUT_GROUP));
				} 
					
	    		JobDetail jobDetail = newJob(ScheduledTimeoutJob.class)
	    			    .withIdentity(job_tri_name, SchedulerEngine.SCHEDULE_TASK_TIMEOUT_GROUP)	    			    
	    			    .usingJobData(ScheduledTimeoutJob.SCHEDULER_ID, scheduler_id)
	    			    .usingJobData(ScheduledTimeoutJob.TRIGGER_TIME, trigger_time)
	    			    .usingJobData(ScheduledTimeoutJob.STARTED_TIME, started_time)
	    			    .build();
 
	    		Date exp=new Date();
	    		exp.setTime(started_time+expiry_time);
		    	SimpleTrigger trigger = (SimpleTrigger) newTrigger() 
		    			    .withIdentity(job_tri_name, SchedulerEngine.SCHEDULE_TASK_TIMEOUT_GROUP)
		    			    .startAt(exp) // some Date		    			    
		    			    .build();
		    	
		    	scheduler.scheduleJob(jobDetail, trigger );		    	
		    	SimpleDateFormat sdf=new SimpleDateFormat("dd.MMM.yyyy HH:mm:ss");		    	
		    	log.debug("----------LoadBalancingQueue:adding timeout into queue will be fired :"+sdf.format(exp)+" scheduler_id:"+scheduler_id);
			}
		}catch(Exception e ) {
			log.error("error while setting timeout for the task, error:"+e.getMessage());
		}	
	}
	
	private String getTimeoutJobName(int scheduler_id, long trigger_time) {
		return "timeout_"+scheduler_id+"_"+trigger_time;
	}
	
	
	public final void taskTimedOut(int scheduler_id, long trigger_time, long started_time) {
		
		int step=0;
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();		
	    try{
	    	
	    	LoadBalancingQueueItem item=getItemFromProcessingQueue(scheduler_id,trigger_time);
	    	removeItemProcessing(item,null,SchedulerExePlanLogs.SERVER_ERROR_EXEC_TIMEDOUT);
	    	//removeFaultyProcessingTask(scheduler_id,trigger_time);
	    	step=1;
	    	sdb.connectDB();
	    	Map data=sdb.getScheduler(scheduler_id);
	    	step=2;
	    	String type=(String)data.get("alert_type");
			String name=(String)data.get("name");
			
	    	String host=item.getMachine();
	    	step=3;
	    	IncomingMessage.updateFinishedPeersTime(host,scheduler_id,trigger_time); //remove from the updated
	    	
	    	Map log=sdb.getQueueLog(scheduler_id, trigger_time);
	    	step=4;
	    	boolean success=false;
	    	if(log!=null && log.get("status")!=null){
	    		String status=(String)log.get("status");
	    		if(status!=null && status.equalsIgnoreCase("success")){
	    			success=true;
	    		}
	    	}		
	    	step=5;
	    	if(!success){
	    		new SchedulerExePlanLogs(scheduler_id,trigger_time).log("Execution TimedOut, removed from the Queue",SchedulerExePlanLogs.SERVER_ERROR_EXEC_TIMEDOUT);	    		
		    	TreeMap<String, Comparable> ldata=new TreeMap<String, Comparable>();
				ldata.put("scheduler_id", scheduler_id);
				ldata.put("trigger_time",trigger_time);		
				ldata.put("host",host);	
				ldata.put("status", ScheduledTask.TIMOUT_WARNING);
				
				Vector<TreeMap<String, Comparable>> v=new Vector<TreeMap<String, Comparable>>();
				v.add(ldata);				 
				sdb.updateQueueLog(v,new Vector(), P2PService.getComputerName());					
				step=6;
			
				
				
				Map<String,String> d1=sdb.getTaskEventActions(scheduler_id, trigger_time);
    			if(d1.containsKey(ScheduledTask.FIELD_DEPENDENCY_TIMEOUT) && d1.get(ScheduledTask.FIELD_DEPENDENCY_TIMEOUT)!=null 
						&& !d1.get(ScheduledTask.FIELD_DEPENDENCY_TIMEOUT).trim().equals("")
					){
						String expression=d1.get(ScheduledTask.FIELD_DEPENDENCY_TIMEOUT);
						String suffi=ScheduledTask.TASK_EVENT_CALL_EXP_ID_VARIABLE+"="+scheduler_id+"\n";
						suffi+=ScheduledTask.TASK_EVENT_CALL_EXP_TRIGGERTIME_VARIABLE+"="+trigger_time+"\n";
						new SchedulerEngine().executeScriptExpression(expression, "onExecution timeout of "+scheduler_id, suffi);
				}
    			
				
    			step=7;
				
				//SchedulerAlert sa=new SchedulerAlert(scheduler_id,trigger_time);
				SimpleDateFormat sdf=new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
				Date start_t=new Date(); start_t.setTime(started_time);
				Date current=new Date();
				String msg="Task timed-out! Task started at "+sdf.format(start_t)+" on peer "+host+" and no response till "+sdf.format(current)+" and removed from the queue";
				
				step=8;
				ExceptionExecutionTimeout exp=new ExceptionExecutionTimeout(msg);
				sdb.updateResponseCode(scheduler_id, trigger_time, exp.getErrorcode());
				step=9;
				
				// send alarm : 
				
				int sc_id = scheduler_id;
				long tri_time = trigger_time;
				
				SchedulerAlarmVO vo = new SchedulerAlarmVO();
				vo.setAlarmType(type);
				vo.setName(name);
				vo.setSubject(SchedulerAlarm.ALARM_SUB_TIMEOUT);
				vo.setMessage(msg);
				vo.setFrom(null);
				vo.setErrCode(exp.getErrorcode());
				vo.setExceptionSchedulerTeamRelated(exp!=null && exp instanceof ExceptionSchedulerTeamRelated);
				vo.setComputerName(P2PService.getComputerName());
				vo.setConsoleMsg(sdb.getConsoleMsg(sc_id, tri_time));
				vo.setExecLogs(sdb.getSchedulerExeLogs(sc_id, tri_time));
				vo.setRepCodeExist(sdb.execLogsRepcodeExist(sc_id, tri_time, SchedulerExePlanLogs.SERVER_ERROR_ALARM_SENT));
				vo.setThemeTags(sdb.getThemeTags(sc_id));
				vo.setOwnerTheme(sdb.getOwnerTheme(sc_id));
				vo.setQueueLog(sdb.getQueueLog(sc_id, tri_time));
				vo.setPeerFriendlyName(sdb.getPeerFriendlyName(vo.getFrom()));

				vo.setSchedulerId(sc_id);
				vo.setTriggerTime(tri_time);

				SchedulerAlarm.sendAlarm(vo);   
				new SchedulerExePlanLogs(sc_id, tri_time).log("Alarm sent",sdb,SchedulerExePlanLogs.SERVER_ERROR_ALARM_SENT);

				step=10;
	    	}
			/*
			log.debug("sending alert message:"+msg);				
			String stop="cmd.exe /c sc  stop 4EPeer";
			String start="cmd.exe /c sc  start 4EPeer";
			String resp="<pre>"+SendCommand2Helper.sendCommand(host, stop);
			Thread.sleep(3000);
			resp+="<br>"+SendCommand2Helper.sendCommand(host, start);
			resp+="</pre>";
			new SchedulerExePlanLogs(scheduler_id,trigger_time).log("Peer Restarted, Output:"+resp);	
			*/
			
	    }catch(Exception e){
	    	e.printStackTrace();
	    	log.error("error while writing timedout log, e:"+e.getMessage()+" reached step:"+step);
	    	
	    }finally{
	    	try{
	    	sdb.closeDB();
	    	}catch(Exception e1){}
	    }
	}
	
	/**
	 * 
	 * @param currentItem
	 * @param ids
	 * @param timecheck
	 * @return 1=pass, 0=not pass, -1 =time out
	 */
	protected int dependencyCheck(LoadBalancingQueueItem currentItem, String ids, String timecheck,SchedulerDB sdb)  {
		
		int rtn=1;
		
		long nexttrig=currentItem.getSf().getNexttrigger_time();
		
		//time out just before 1 minute next trigger time
		if(nexttrig>0){
			Date next=new Date(nexttrig);
			Calendar c1=Calendar.getInstance();
			c1.setTime(next);
			c1.add(Calendar.MINUTE, -1);
			//log.debug("next trigger time:"+c1);	
			
			Date now=new Date();
			if(now.after(c1.getTime())){
				
				rtn=-1;
			    return rtn;
			}
		}
		
		StringTokenizer st=new StringTokenizer(ids,",");
		TreeMap<Number, String> idNumb=new TreeMap<Number, String> ();
		Integer timespan=null;
		while(st.hasMoreTokens()){
			String cid=st.nextToken();
			try{
				idNumb.put(Integer.parseInt(cid),"fail");
			}catch(Exception e){}
		}
		try{
			timespan= Integer.parseInt(timecheck);
		}catch(Exception e){}
		if(idNumb.size()>0 && timespan>0){
				 String ids1=null;
			     for(Iterator<Number> i=idNumb.keySet().iterator();i.hasNext();){
			    	 ids1=(ids1==null)?""+i.next():ids1+","+i.next();
			     }
			    
		         Calendar c=Calendar.getInstance();
		         c.add(Calendar.MINUTE, -timespan);
		         Date pdate=c.getTime();
		     	 //SchedulerDB sdb=SchedulerDB.getSchedulerDB();			
			     try{
			    	//log.debug(timespan+" minutes before "+pdate);
			    	List data=(List)getCache().get("depids:"+ids);			    		
		    		if(data!=null && data.size()>0) { Object obj=data.get(0); }//this is to make sure the object doesn't expire even if the cache expires.

		    		
			    	if(data==null){			    		
					    IElementAttributes att= getCache().getDefaultElementAttributes();
						att.setMaxLifeSeconds(3); //seconds maximum wait
						data=sdb.listDependencyList(ids1,pdate);
						getCache().put("depids:"+ids,data,att);
			    	}			    	
			    	
			    	//log.debug("data: size()"+data.size());
			    	if(data.size()>0){
			    		//rtn=0;
			    		for(Iterator reco=data.iterator();reco.hasNext();){
			    			Map rdata=(Map)reco.next();
			    			if(rdata.get("scheduler_id")!=null){
			    				idNumb.put((Integer)rdata.get("scheduler_id"),"pass");
			    			}
			    		}
			    		if(idNumb.containsValue("fail")){
			    			rtn=0;
			    		}
			    		//log.debug("idNumb:"+idNumb);
			    	}else{
			    		rtn=0;
			    	}
			     }catch(Exception e){
			    	//e.printStackTrace();
			     }finally{
			    	 try{
			    		// sdb.closeDB();
			    	 }catch(Exception e1){}
			     }
		}
		//log.debug("rtn:"+rtn);
        return rtn;		
		
		
		
	}
	
	
	public abstract void add(LoadBalancingQueueItem item);
	public abstract void addExecuteR(RScript item, RScriptListener listener) throws Exception;
	public abstract List<LoadBalancingQueueItem> getAllTasks();
	
	public abstract long lastExcecutedTime();
	
	public abstract RScript startScriptIfNotStarted(RScript rscript,String peer);
	public abstract void scriptFinished(RScript rscript, String result,String status);	

	public abstract void removeScriptFromAllQueue(RScript rscript);
	
	
	public abstract void removeItemProcessing(LoadBalancingQueueItem item,String message,int respCode);
	public abstract LoadBalancingQueueItem getItemFromProcessingQueue(int scheduler_id, long trigger_time);  
	
	
	public abstract boolean killQueuedTask(int scheduler_id, long trigger_time);
	
	//public abstract void taskTimedOut(int scheduler_id, long trigger_time, long started_time);
	
	public abstract boolean removeFaultyProcessingTask(int scheduler_id, long trigger_time);  

	public abstract int startedIfNotStarted(int schedulerid,long trigger_time, String machinename);
	
	public abstract void executionFailed(int schedulerid,long trigger_time, String machinename);
	
	//public abstract void executionStarted(LoadBalancingQueueItem item,long trigger_time, String machinename);
	
	public  abstract void executionEnded(int schedulerid, long trigger_time);
	
	/**
	 * @deprecated
	 */
	public abstract void cleanupProccesingQueue(int schedulerid, String computername);
	
	public abstract Collection<LoadBalancingQueueItem> getExecutingTasks();
	
	public abstract Collection<LoadBalancingQueueItem> getQueuedTasks();
	
	public abstract void executeScript(Vector<Object> peers,int scriptid) throws Exception;
	
	public abstract void peerStarted(int scheduler_id, long trigger_time, String peername) throws Exception; 
 
	 
	/**
	 * @deprecated
	 * @param schedulerid
	 */
	public abstract void executionEnded(int schedulerid);
	
	public abstract Collection<RScript> getScriptQueue() throws Exception;
	public abstract Collection<RScript> getScriptProcessingQueue() throws Exception;



	
	
	
}


