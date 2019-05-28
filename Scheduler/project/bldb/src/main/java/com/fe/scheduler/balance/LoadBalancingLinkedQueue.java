/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fe.scheduler.balance;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.PipeService;
import net.jxta.protocol.PipeAdvertisement;

import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mozilla.javascript.edu.emory.mathcs.backport.java.util.Collections;

import com.fe.client.SchedulerMgmt;
import com.fourelementscapital.db.SchedulerDB;
import com.fourelementscapital.scheduler.ScheduledTaskFactory;
import com.fourelementscapital.scheduler.ScheduledTaskQueue;
import com.fourelementscapital.scheduler.SchedulerEngine;
import com.fourelementscapital.scheduler.alarm.SchedulerAlarm;
import com.fourelementscapital.scheduler.alarm.SchedulerAlarmVO;
import com.fourelementscapital.scheduler.balance.LoadBalancingQueue;
import com.fourelementscapital.scheduler.balance.LoadBalancingQueueItem;
import com.fourelementscapital.scheduler.balance.LoadBalancingQueueTimeout;
import com.fourelementscapital.scheduler.balance.WaitingQueueList;
import com.fourelementscapital.scheduler.balance.hsqldb.LoadBalancingHSQLQueue;
import com.fourelementscapital.scheduler.engines.ScheduledTask;
import com.fourelementscapital.scheduler.engines.SchedulerExePlanLogs;
import com.fourelementscapital.scheduler.engines.StackFrame;
import com.fourelementscapital.scheduler.engines.StackFrameCallBack;
import com.fourelementscapital.scheduler.error.ClientError;
import com.fourelementscapital.scheduler.exception.ExceptionDependencyTimeout;
import com.fourelementscapital.scheduler.exception.SchedulerException;
import com.fourelementscapital.scheduler.p2p.MessageBean;
import com.fourelementscapital.scheduler.p2p.P2PAdvertisement;
import com.fourelementscapital.scheduler.p2p.P2PService;
import com.fourelementscapital.scheduler.p2p.listener.IncomingMessage;
import com.fourelementscapital.scheduler.p2p.listener.OutgoingMessage;
import com.fourelementscapital.scheduler.p2p.listener.OutgoingMessageCallBack;
import com.fourelementscapital.scheduler.p2p.msg.PostMessage;
import com.fourelementscapital.scheduler.p2p.msg.impl.TenderScript;
import com.fourelementscapital.scheduler.rscript.RScript;
import com.fourelementscapital.scheduler.rscript.RScriptListener;

public class LoadBalancingLinkedQueue extends LoadBalancingQueue implements Callable   {
	
	

	//making all static variables to non-static as loabancingqueue object is made signleton 
	
    /*	
	private static WaitingQueueList<LoadBalancingQueueItem>	queue= new WaitingQueueList<LoadBalancingQueueItem>();
	private static ConcurrentLinkedQueue<LoadBalancingQueueItem>	queueprocessing= new ConcurrentLinkedQueue<LoadBalancingQueueItem>();

	private static ConcurrentLinkedQueue<RScript>	scriptQueue= new ConcurrentLinkedQueue<RScript>();
	private static ConcurrentLinkedQueue<RScript>	scriptQueueProcessing= new ConcurrentLinkedQueue<RScript>();
	private static Vector<RScript> 					scriptQueueVec=new Vector<RScript>(); 
	private static Hashtable<String, RScriptListener> scriptQueueListener=new Hashtable<String, RScriptListener>();
	private static TreeMap queue_stat=new TreeMap();  
	 
	private static boolean threadRunning=false;
	private static boolean scriptThreadRunning=false;
	
	private static long lastExcecutedTime=0;
    private static Logger log=Logger.getLogger(LoadBalancingQueue.class);
	
	private static Future<String> future=null;
	private static Future<String> futureScript=null;
	 
	private static ExecutorService executor = Executors.newCachedThreadPool();

	private static Vector<String> shuffleIteration=new Vector<String>();
	*/
	
	private WaitingQueueList<LoadBalancingQueueItem>	queue= new WaitingQueueList<LoadBalancingQueueItem>();
	private  ConcurrentLinkedQueue<LoadBalancingQueueItem>	queueprocessing= new ConcurrentLinkedQueue<LoadBalancingQueueItem>();

	private ConcurrentLinkedQueue<RScript>	scriptQueue= new ConcurrentLinkedQueue<RScript>();
	private ConcurrentLinkedQueue<RScript>	scriptQueueProcessing= new ConcurrentLinkedQueue<RScript>();
	private Vector<RScript> 					scriptQueueVec=new Vector<RScript>(); 
	private Hashtable<String, RScriptListener> scriptQueueListener=new Hashtable<String, RScriptListener>();
	private TreeMap queue_stat=new TreeMap();  
	 
	private boolean threadRunning=false;
	private boolean scriptThreadRunning=false;
	
	private long lastExcecutedTime=0;
    private Logger log = LogManager.getLogger(LoadBalancingLinkedQueue.class.getName());
	
	private Future<String> future=null;
	private Future<String> futureScript=null;
	 
	private ExecutorService executor = Executors.newCachedThreadPool();
	
	
	private static ExecutorService startScriptService= Executors.newFixedThreadPool(3);
	

	//private boolean taskMode=true;
	//public static final boolean TASK_MODE=true;
	
	//private final static String CACHE_KEY_FOR_TIMEOUTCLEANER="CACHE_KEY_FOR_TIMEOUTCLEANER";
	 
	
	private int currentMode=0;
	private TreeMap runonlyon=new TreeMap(); 
	
	private final static int MODE_TASK=1;
	private final static int MODE_SCRIPT=2;
	
	
	public LoadBalancingLinkedQueue(int mode){
		this.currentMode=mode;
	}
	
	public synchronized void add(LoadBalancingQueueItem item){		
		//queue.add(new StackFrame(task,data));
		item.setExecuting(false);
		//if(checkQueueOverlappedOrQueued(item) || !queue.contains(item) ){
		log.debug("add() item: scheduler_id:"+item.getSchedulerid()+" trigger_time:"+item.getSf().getTrigger_time());
		
		
		
		TreeMap<String, Comparable> ldata=new TreeMap<String, Comparable>();
		ldata.put("scheduler_id", item.getSchedulerid());
		ldata.put("trigger_time",item.getSf().getTrigger_time());
		ldata.put("inject_code", item.getInject_code()); 
		 
		
		boolean overlapped=false;
		if(getAllTaskIds().contains(item.getSchedulerid())){	
		
			overlapped=true;
			
			try{
				
				LoadBalancingQueueItem itemov=null;
				int count=0;			
				for(Iterator<LoadBalancingQueueItem> i=queueprocessing.iterator();i.hasNext();){
					LoadBalancingQueueItem oti=i.next();			
					if(oti.getSchedulerid()==item.getSchedulerid())  count++;  //counting, because not to all more than 2 peers take problem task
					//System.out.print("~~~~~~~~!!!!!! schedulerId:"+item.getSchedulerid()+"  item.getStarted():"+oti.getStarted()+" getOverlaptimeout:"+oti.getOverlaptimeout());					
				    if(oti.getSchedulerid()==item.getSchedulerid() && oti.getStarted()!=null && oti.getOverlaptimeout()>0){
				    	itemov=oti;
				    }
				}
				if(itemov!=null && count<=1){
			    	long diff=new Date().getTime()-itemov.getStarted().getTime();
			    	//System.out.println("~~~~~~~~@@@@@@@@ diff:"+diff+"  itemov.getOverlaptimeout():"+itemov.getOverlaptimeout()+" count:"+count);
			    	if(diff>=itemov.getOverlaptimeout()){
			    		overlapped=false;
			    		new SchedulerExePlanLogs(itemov.getSchedulerid(),itemov.getSf().getTrigger_time()).log("Task is taking more time and the same task can be overlapped in the queue",SchedulerExePlanLogs.IGNORE_CODE);
			    	}
				}
			}catch(Exception e){
				System.out.println("~~~~ LoadBalancingQueue.class Error 20:"+e.getMessage());
			}
			
		}
		
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		
		boolean logging_required=true;  //this flag is set to true when the same task triggered exactly the same by 2 different cron expression for example (every 2 minutes and every 1 hour)  
		try {
			sdb.connectDB();
			
			LoadBalancingQueueTimeout tqt=new LoadBalancingQueueTimeout(sdb,new ScheduledTaskFactory().getTaskUids());
			queue.setAlertRange( tqt.getMaxWaitingAlert());
			
			if(!overlapped){				
				
				try{
					
					new SchedulerExePlanLogs(item.getSchedulerid(),item.getSf().getTrigger_time()).log("Added to server queue by "+item.getSf().getInvokedby(),SchedulerExePlanLogs.IGNORE_CODE);
					
					//logging code injection....
					if(item.getInject_code()!=null && !item.getInject_code().equals("")){
						new SchedulerExePlanLogs(item.getSchedulerid(),item.getSf().getTrigger_time()).log(" Code Injected: "+item.getInject_code(),SchedulerExePlanLogs.IGNORE_CODE);
					}
					addLastExecutionDuration(sdb,item);
					addTimeoutAndLastExecTimes(sdb,item);
					 
				}catch(Exception e){
					 
					System.out.println("LoadBalancingQueue: Error 10:"+e.getMessage());
				}
				
				queue.add(item);	
				//exitQueueIteration=true;
			} else{
				//LoadBalancingQueueItem previous1=new LoadBalancingQueueItem();previous1.setSchedulerid(schedulerid)				
				ldata.put("status", ScheduledTask.EXCECUTION_OVERLAPPED);
				ldata.put("is_triggered",new Integer(1));    
				LoadBalancingQueueItem previous=getItemFromQueue(getQueuedTasks(), item);
				if(previous==null){
					previous=getItemFromQueue(getExecutingTasks(), item);
				}
				
				if(previous!=null){
					if(previous.getSf().getTrigger_time()==item.getSf().getTrigger_time()) {
						//the same task being trigged by 2 different cron expression
						//so will be ignored as another one on the queue with exactly same trigger time.
						logging_required=false;
					}else{
						SimpleDateFormat sdf=new SimpleDateFormat("dd/MMM hh:mm");
						Date d=new Date();d.setTime(previous.getSf().getTrigger_time());
						String message="Failed, The task is already queued, added at "+sdf.format(d);
						if(previous.getStarted()!=null){
							message="Failed, The task is already executing, started at "+sdf.format(previous.getStarted());
						} 
						new SchedulerExePlanLogs(item.getSchedulerid(),item.getSf().getTrigger_time()).log(message,SchedulerExePlanLogs.IGNORE_CODE);
					}
				}
			}
			if(logging_required){
				
				Vector<TreeMap<String, Comparable>> v=new Vector<TreeMap<String, Comparable>>();
				v.add(ldata);				 
				sdb.updateQueueLog(v,new Vector(), P2PService.getComputerName());
			}
			 
		} catch (Exception e) {
			System.out.println("~~~~ LoadBalancingQueueItem.add() Error while adding :"+e.getMessage());
		}finally{
			try{				
				sdb.closeDB();
			}catch(Exception e){}
		}
		
		if(future==null || (future!=null && future.isDone() )){
			//LoadBalancingQueue q=;			
			//future=executor.submit( new LoadBalancingLinkedQueue(LoadBalancingLinkedQueue.MODE_TASK));
			future=executor.submit( this);
		}
		
		 
		
	}
	
	
	
	
	
	
	
	
	
	private Vector<Number> getAllTaskIds(){
		
		Vector<Number> rtn=new Vector<Number>();
		for(Iterator<LoadBalancingQueueItem> i=queue.iterator();i.hasNext();){
			LoadBalancingQueueItem item=i.next();
			rtn.add(new Integer(item.getSchedulerid()));
			//log.debug("uid");
		}
		for(Iterator<LoadBalancingQueueItem> i=queueprocessing.iterator();i.hasNext();){
			LoadBalancingQueueItem item=i.next();
			rtn.add(new Integer(item.getSchedulerid()));
			//log.debug("uid");
		}
		return rtn;
	}
	
	
    public ArrayList<LoadBalancingQueueItem> getAllTasks(){
		
    	ArrayList<LoadBalancingQueueItem> rtn=new ArrayList<LoadBalancingQueueItem>();
		for(Iterator<LoadBalancingQueueItem> i=queue.iterator();i.hasNext();){
			LoadBalancingQueueItem item=i.next();
			rtn.add(item);
			//log.debug("uid");
		}
		for(Iterator<LoadBalancingQueueItem> i=queueprocessing.iterator();i.hasNext();){
			LoadBalancingQueueItem item=i.next();
			rtn.add(item);
			//log.debug("uid");
		}
		return rtn;
	}
	
	
	public long lastExcecutedTime(){
		return lastExcecutedTime;
	}
	
	public  synchronized void removeItemProcessing(LoadBalancingQueueItem item,String message,int respCode){
		
		if(message!=null){
			new SchedulerExePlanLogs(item.getSchedulerid(),item.getSf().getTrigger_time()).log(message,SchedulerExePlanLogs.IGNORE_CODE);
		}
		queueprocessing.remove(item);		
		removeTimeoutForTask(item.getSchedulerid(),item.getSf().getTrigger_time());
		//Debugger.addDebugMsg("script id:"+item.getSchedulerid()+" executing finishing removing form LB executing Queue",item.getSchedulerid()+"");
		
		log.debug("removing from processing Q schedueler_id:"+item.getSchedulerid());
		//exitQueueIteration=true;
		item.setSf(null);
		item=null;		
	}
	
	private  synchronized void addItemProcessing(LoadBalancingQueueItem item){
		queueprocessing.add(item);	
		
		//adds into timeout scheduler queue, so that it fires on timeout and removes from the queue
		if(item.getTimeoutexpiry()>0){
			addTimeoutForTask(item.getSchedulerid(),item.getSf().getTrigger_time(),item.getStarted().getTime(),item.getTimeoutexpiry());
		}
		
		log.debug("adding to processing Q schedueler_id:"+item.getSchedulerid());
		 
	}
	
	private  synchronized void removeFromQueue(LoadBalancingQueueItem item){		
		queue.remove(item);
		//exitQueueIteration=true;	 
		log.debug("removing from Queue schedueler_id:"+item.getSchedulerid());
	}
	
	
	
	
   
	/**
	 * this flag helps queue to goto top once item started to excecute
	 * to helps execute in timely order. 
	 */
	//private static boolean exitQueueIteration=false;
	
	
	private Set intersection(Collection a, Collection b) {
		  // you may swap a and b, so a would contain the smaller collection
		  Set result = new HashSet(a);
		  result.retainAll(b);
		  return result;
	}
	
	
	
	private void runTasks() {		 
		
		try{
			
		 	if(cache==null){
				//cache=JCS.getInstance("perminentpeers");
		 		getCache();
			}	 

			if(getCache().get("System_Garbage_Collector")==null){
			 	IElementAttributes att= getCache().getDefaultElementAttributes();
				att.setMaxLifeSeconds(45);
				getCache().put("System_Garbage_Collector", "yes",att);
				System.gc();  //every 90 second force to collect the garbage.......
			}
			
			//if(getCache().get(LoadBalancingQueue.CACHE_KEY_FOR_TIMEOUTCLEANER)==null && queueprocessing.size()>0){
			// 	IElementAttributes att= getCache().getDefaultElementAttributes();
			//	att.setMaxLifeSeconds(TIMEOUTCLEANER_FREQ_SECONDS);
			//	getCache().put(LoadBalancingQueue.CACHE_KEY_FOR_TIMEOUTCLEANER, "yes",att);
			//	
			//}

			
			log.debug("+++++++++++++++++++++++++++++++++++++++++++++++++>runTasks() ");
			
			while(!queue.isEmpty() ){			
				
				
				
				try{
					for(Iterator<LoadBalancingQueueItem> iq=queue.iterator();iq.hasNext() ;){						
						LoadBalancingQueueItem currentItem=iq.next();	
						try{
							
							if(processDependencyQueueItem(currentItem)){								
						    	if(processValidateQueueItem(currentItem)){						    		
						    		processQueueItem(currentItem);
						    	}
							}
							 
						}catch(Exception e){
							log.error("error while processing item:"+currentItem.getSchedulerid());
							e.printStackTrace();
						} 
				    	Thread.sleep(13);
				    	currentItem=null;
					}  
				
				 
				}catch(Exception e){
					//Thread.sleep(50);					 
					log.error("error while processing queue: err: "+e.getMessage());
				}
				
			} //while loop
			log.debug("<-------------------------------------------------------------------- exiting ..... runTasks() ");
			
		}catch(Exception e){
			
			log.error("loadbalancing queue thread terminiated: e: "+e.getMessage());
			e.printStackTrace();
			
		}finally{
			threadRunning=false;
	 
		}
		
	}
	
	
	
	
	
	private void runScripts() {		 
		
		try{
			
		 	if(cache==null){
				//cache=JCS.getInstance("perminentpeers");
		 		getCache();
			}	 

			if(getCache().get("System_Garbage_Collector")==null){
			 	IElementAttributes att= getCache().getDefaultElementAttributes();
				att.setMaxLifeSeconds(45);
				getCache().put("System_Garbage_Collector", "yes",att);
				System.gc();  //every 90 second force to collect the garbage.......
			}
			
		 	
			while(!scriptQueue.isEmpty() ){		
				try{	 
					if(!scriptQueue.isEmpty()){
						try{
							processScriptQueue();
						}catch(Exception e){
							log.error("couldn't process script, e:"+e.getMessage());
						}
					}
				}catch(Exception e){
					Thread.sleep(5);
					log.error("error while processing queue: err: "+e.getMessage());
				}
				
			} //while loop
			
		}catch(Exception e){
			
			log.error("loadbalancing queue thread terminiated: e: "+e.getMessage());
			e.printStackTrace();
			
		}finally{
			scriptThreadRunning=false;
		}
		
	}
	
	
	private void runTimeoutCleaner() {
		
		
	}
	
	
	
	/*
	public void run() {		 
	
		 if(this.taskMode){
			 runTasks();
		 }else{
			 runScripts();
		 }
		
	}
	*/

	public void run() {		
		
	}
	
	public Object call() throws Exception {		 
		
		 //if(this.taskMode){
		//	 runTasks();
		// }else{
		//	 runScripts();
		 //}
		 
		log.debug("call() called and current mode:"+this.currentMode);
		 switch (this.currentMode) {
         	case LoadBalancingLinkedQueue.MODE_TASK:  runTasks();
                break;
         	case LoadBalancingLinkedQueue.MODE_SCRIPT:  runScripts();
                break;
         	 
		 }
		 
		return "done";
	}
	
	
	/*
	private void processQueue() throws Exception {

		//for(Iterator<LoadBalancingQueueItem> iq=queue.iterator();(iq.hasNext() && !exitQueueIteration );){
		for(Iterator<LoadBalancingQueueItem> iq=queue.iterator();iq.hasNext() ;){
			
			LoadBalancingQueueItem currentItem=iq.next();					
			

	    	processQueueItem(currentItem);
			
		} //for loop
		
		
	}
	*/

	/**
	 * kill only incase of queue crashed....
	 */
	public void killScriptQueue(){
		
		scriptQueueListener.clear();
		scriptQueueVec.clear();
		scriptQueue.clear();
		
	}
	
	
	private synchronized void scriptTimedOut(RScript rs){
		try{
			if(scriptQueueListener.get(rs.getUid())!=null){
				RScriptListener rslisten=scriptQueueListener.get(rs.getUid());
				try{
					rslisten.onScriptTimedOut(rs);
				}catch(Exception e){
					ClientError.reportError(e, "Error while invoking listener");				
				}finally{
					scriptQueueListener.remove(rs.getUid());
				}
			}
		}catch(Exception e){
			log.error("Error while time out");			
		}finally{
			scriptQueueVec.remove(rs);
			scriptQueue.remove(rs);
		}
	}
	
	private Vector roundRobin=new Vector();
	
	private void processScriptQueue() throws Exception{
	

	   RScript rs=scriptQueue.peek();
	   
	   if(!rs.isExecuting()){
		   
		    cache=getCache();
		   
		    if(getCache().get(rs.getUid())==null){
		    	
		    	IElementAttributes att1= getGroupedCache().getDefaultElementAttributes();		    	
	 			att1.setMaxLifeSeconds(CACHE_GROUP_EXPIRY);
	 			getGroupedCache().putInGroup(rs.getUid(), CACHE_GROUP_TIMEOUT,"timeout", att1);	 			
	 			log.debug("adding into timeoud cache...------------>");
		    	scriptTimedOut(rs);
		    	
		    	//just to retrieve		    	 
				for(Object key: getGroupedCache().getGroupKeys(CACHE_GROUP_TIMEOUT)){
					getGroupedCache().getFromGroup(key,CACHE_GROUP_TIMEOUT);
				}
				 
		    	
		    }else{
	    	    Vector<Object> autclients=getRunOnlyOn(rs.getTaskuid());
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
				 	for(Iterator<Object> i=autclients1.iterator();i.hasNext();){		
	
				 		String clientname=(String)i.next();
				 		String ky=clientname+"_"+rs.getTaskuid();
				 		//sends only 1 post message in a second 
				 	   
				 		
				 	     IElementAttributes att= cache.getDefaultElementAttributes();
				 	     att.setMaxLifeSeconds(1);
				 		
				 		if(cache.get(ky)==null){
				 			
					 		TenderScript ts=new TenderScript();
					 		ts.setPriority(OutgoingMessageCallBack.PRIORITY_LOW );
					 		ts.setUid(rs.getUid());						 		
					 		//ts.setMsgCreator(P2PService.getComputerName());
					 		//ts.setMsgRecipient(clientname);
					 		ts.setTaskuid(rs.getTaskuid());
					 		
					  
					 		
					 		PostMessage ps=new PostMessage(ts,clientname);
					 		ps.send();
					 		cache.put(ky, "send",att);
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
	
   
	public synchronized void addExecuteR(RScript item, RScriptListener listener) throws Exception{		
		//queue.add(new StackFrame(task,data));
		
		log.debug("adding script:"+item.getScript());
		item.setExecuting(false);
		item.setQueued_time(new Date().getTime());
		
		scriptQueue.add(item);
		scriptQueueVec.add(item);
		
		 
		
		IElementAttributes att= getCache().getDefaultElementAttributes();
		att.setMaxLifeSeconds(CACHE_GROUP_EXPIRY);
		getCache().put(item.getUid(), "alive", att);
		   
		if(listener!=null) scriptQueueListener.put(item.getUid(), listener);
		
		/*
		if(!scriptThreadRunning){			
			scriptThreadRunning=true;
			new Thread(new LoadBalancingQueue(!TASK_MODE)).start();
		}
		*/
		
		if(futureScript==null || (futureScript!=null  && futureScript.isDone())){
			//LoadBalancingQueue q=;			
			//futureScript=executor.submit( new LoadBalancingLinkedQueue(LoadBalancingLinkedQueue.MODE_SCRIPT));
			futureScript=executor.submit(this);
		}
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
						LoadBalancingLinkedQueue lhq=(LoadBalancingLinkedQueue)LoadBalancingQueue.getExecuteRScriptDefault();
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
		}.init(rscript, peer));
		
		RScript rs=null;
		try{
			rs=(RScript)fu.get();
		}catch(Exception e){
			log.error("error while retriveing future result");
		}
		return rs;
	}
	
	
	private synchronized RScript startScriptIfNotStarted1(RScript rscript,String peer) {				 
 
		
			if(scriptQueue.contains(rscript)){
				scriptQueue.remove(rscript);			
				RScript rtn=scriptQueueVec.get(scriptQueueVec.indexOf(rscript));			
				rtn.setPeer(peer);
				rtn.setDelay(new Date().getTime()- rtn.getQueued_time());
				rtn.setExecuting(true);
				rtn.setStartedtime(new Date());
				scriptQueueProcessing.add(rtn);
				
				if(scriptQueueListener.get(rtn.getUid())!=null){
					RScriptListener rslisten=scriptQueueListener.get(rtn.getUid());
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
				
				return rtn;
				
			}else{
				return null;
			}
			
		 
	 }

	
	
	public synchronized void scriptFinished(RScript rscript, String result,String status)  {		 
		scriptQueueProcessing.remove(rscript);		
		
		try{
			RScript rscript1=scriptQueueVec.get(scriptQueueVec.indexOf(rscript));		
 			String ky=rscript1.getPeer()+"_"+rscript1.getTaskuid();
 			rscript1.setError(rscript.getError());
 			cache=getCache();
 			
 			
 			IElementAttributes att= getGroupedCache().getDefaultElementAttributes();
 			att.setMaxLifeSeconds(CACHE_GROUP_EXPIRY);
 			getGroupedCache().putInGroup(rscript.getUid(), CACHE_GROUP_FINISHED,rscript1.getDelay(), att);
 			//getGroupedCache().getGroupKeys(CACHE_GROUP_FINISHED); 			
 			//just to retrieve so that, expired won't in the memory
			for(Object key: getGroupedCache().getGroupKeys(CACHE_GROUP_FINISHED)){
				getGroupedCache().getFromGroup(key,CACHE_GROUP_FINISHED);
			}
			
 			if(scriptQueueListener.get(rscript1.getUid())!=null){
				RScriptListener rslisten=scriptQueueListener.get(rscript1.getUid());
				try{
					rslisten.onScriptFinished(rscript1, rscript1.getPeer(),result, status);
				}catch(Exception e){
					ClientError.reportError(e, "Error while invoking listener");				
				}
				scriptQueueListener.remove(rscript1.getUid());
				rslisten=null;
			}
 				
			cache.remove(ky);
			rscript1=null;
		}catch(Exception e){			
		}
		
	    scriptQueueVec.remove(rscript);
		rscript=null;
	}
	
	
	private boolean processDependencyQueueItem(LoadBalancingQueueItem currentItem) throws Exception {
		boolean dependpass=true;	
		if(currentItem!=null){
	    	Map data=currentItem.getSf().getData();
	    	String dids=(String)data.get(ScheduledTask.FIELD_DEPENDENCY_IDS);
	    	int depvalid=0;
	    	
	    	if(dids!=null && !dids.equals("") && data.get(ScheduledTask.FIELD_DEPENDENCY_CHECKTIME)!=null &&  !data.get(ScheduledTask.FIELD_DEPENDENCY_CHECKTIME).equals("")){
	    		
	    		String dtime=(String)data.get(ScheduledTask.FIELD_DEPENDENCY_CHECKTIME);
	    		
	    		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
				try{
					sdb.connectDB();
		    		depvalid=dependencyCheck(currentItem,dids,dtime,sdb);
		    		if(depvalid==1) dependpass=true;			    		
		    		if(depvalid==0) dependpass=false;
		    		if(depvalid==-1){
		    			dependpass=true;
		    			currentItem.getSf().setDependencyfailed(true);
		    			String msg="Task timeout! Waited for dependencies until 1 minute before next execution time";
		    		
		    			ExceptionDependencyTimeout exp=new ExceptionDependencyTimeout(msg);
	    				sdb.updateResponseCode(currentItem.getSchedulerid(), currentItem.getSf().getTrigger_time(), exp.getErrorcode());		    				
	    	
		    			currentItem.getSf().setTasklog(msg);
		    			//execute the queue locally 
		    			String type=(String)data.get("alert_type");
		    			String name=(String)data.get("name");
		    			if(type!=null && !type.equals("")){
		    				
		    				// send alarm : 
		    				
		    				int sc_id = currentItem.getSchedulerid();
		    				long tri_time = currentItem.getSf().getTrigger_time();
		    				
		    				SchedulerAlarmVO vo = new SchedulerAlarmVO();
		    				vo.setAlarmType(type);
		    				vo.setName(name);
		    				vo.setSubject(SchedulerAlarm.ALARM_SUB_TIMEOUT);
		    				vo.setMessage(msg);
		    				vo.setFrom(null);
		    				vo.setErrCode(exp.getErrorcode());
		    				vo.setExceptionSchedulerTeamRelated(false);
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
		    				
		    			}
		    			
		    			Map<String,String> d1=sdb.getTaskEventActions(currentItem.getSchedulerid(), currentItem.getSf().getTrigger_time());
		    			if(d1.containsKey(ScheduledTask.FIELD_DEPENDENCY_TIMEOUT) && d1.get(ScheduledTask.FIELD_DEPENDENCY_TIMEOUT)!=null 
								&& !d1.get(ScheduledTask.FIELD_DEPENDENCY_TIMEOUT).trim().equals("")
							){
								String expression=d1.get(ScheduledTask.FIELD_DEPENDENCY_TIMEOUT);
								String suffi=ScheduledTask.TASK_EVENT_CALL_EXP_ID_VARIABLE+"="+currentItem.getSchedulerid()+"\n";
								suffi+=ScheduledTask.TASK_EVENT_CALL_EXP_TRIGGERTIME_VARIABLE+"="+currentItem.getSf().getTrigger_time()+"\n";
								new SchedulerEngine().executeScriptExpression(expression, "onDependency timeout of "+currentItem.getSchedulerid(), suffi);
								
						}
		    			
		    			
		    			
		    		}	
				}catch(Exception e){
					log.error("Error at processDependencyQueueItem:"+e.getMessage());
				}finally{
					sdb.closeDB();
				}
	    	}
	    	data=null;
		}
		return dependpass; 
	}
	
    private boolean processValidateQueueItem(LoadBalancingQueueItem currentItem) throws Exception {

		boolean processfurther=true;
		
		//if(dependpass){
			
		//Object[] qarry=  queue.toArray();				
	    //for(int iab=0;iab<qarry.length;iab++){
		for(Iterator<LoadBalancingQueueItem> iq=queue.iterator();iq.hasNext() ;){						
			LoadBalancingQueueItem loopItem=iq.next();		
	    	
	    	//LoadBalancingQueueItem loopItem=(LoadBalancingQueueItem)qarry[iab];
	    	
	    	if(loopItem!=null && loopItem.getSf()!=null && loopItem.getSf().getTask()!=null && currentItem.getSf()!=null && currentItem.getSf().getTask()!=null && loopItem.getSf().getData()!=null ){
	    		if(     currentItem.getSchedulerid()!=loopItem.getSchedulerid() &&
	    				currentItem.getSf().getTask().getUniqueid().equals(loopItem.getSf().getTask().getUniqueid()) && 
	    				currentItem.getSf().getTrigger_time()>loopItem.getSf().getTrigger_time()				    			
	    		){
	    			Map looitmdata=loopItem.getSf().getData();
	    			String dtime=(String)looitmdata.get(ScheduledTask.FIELD_DEPENDENCY_CHECKTIME);
	    			if(dtime==null){
	    				processfurther=false;
	    			}
	    		}else{					    			
	    			
	    			//the following block added to to get high priority based on number of peers and prious executed time. 
	    			//00
	    			//ignored for a while.
	    			 
	    			Map looitmdata=loopItem.getSf().getData();
	    			String dtime=(String)looitmdata.get(ScheduledTask.FIELD_DEPENDENCY_CHECKTIME);
	    			
	    			//task with fewer gets high priority
	    			
	    			if(currentItem.getSf().getTrigger_time()==loopItem.getSf().getTrigger_time() && dtime==null	&& 
	    					currentItem.getSf().getTask().getUniqueid().equals(loopItem.getSf().getTask().getUniqueid())		
	    			){					    				
	    				if((currentItem.getLastExecutedDuration()<=0 &&  loopItem.getLastExecutedDuration()>0) 
		    				    || (loopItem.getLastExecutedDuration()>0 && currentItem.getLastExecutedDuration()>0 && currentItem.getLastExecutedDuration()>loopItem.getLastExecutedDuration())){
		    					processfurther=false;
		    			}
 
	    			}
	    			//00	
	    			if(LoadBalancingLinkedQueue.priorityQueue ){
		    			Vector grouporder=getGroupOrder();
			    		if(grouporder!=null && grouporder.indexOf(currentItem.getSf().getTask().getUniqueid())>grouporder.indexOf(loopItem.getSf().getTask().getUniqueid()) ){
			    		//queue jumping....						    			
			    			String taskuid11=loopItem.getSf().getTask().getUniqueid();
			    			log.debug("found priority group:"+taskuid11);
			    			SchedulerMgmt sm=new SchedulerMgmt();
			    			Map t=sm.getOnlinePeers();
			    			if(t!=null && t.size()>0){
				    			log.debug("online peers:"+t.keySet());
				    			Vector<Object> autclients=getRunOnlyOn(taskuid11);
				    			String peerquery="";
				    			for(Iterator i=t.keySet().iterator();i.hasNext();){
				    				String peer=(String)i.next();
				    				if(autclients!=null && autclients.contains(peer)){
				    					peerquery+=(peerquery.equals(""))?"'"+peer+"'":",'"+peer+"'";
				    				}
				    			}
				    			log.debug("peer query:"+peerquery);
				    			Vector avail=getPeers4PriorityGr(peerquery,"'"+taskuid11+"'");
				    			log.debug("avail peers for prioiry:"+avail);
				    			if(avail!=null && avail.size()>0){							    				
				    				processfurther=false;
				    			}
				    			log.debug("======================================="+avail);
				    			avail=null;
				    			autclients=null;
			    			}
			    			t=null;
			    		}
			    		looitmdata=null;
			    		grouporder=null;
	    			}
	    		} //else
	    		
	    	} 
	    }
	    //qarry=null;
		//}
		return processfurther;
    }
    
    
    
    private void processQueueItem(LoadBalancingQueueItem currentItem) throws Exception {
		//if(processfurther){			

			if(currentItem!=null && !currentItem.isExecuting()){
			    String taskuid=currentItem.getSf().getTask().getUniqueid();
		    	Vector<Object> autclients=getRunOnlyOn(taskuid);
		    	//unix server is not authorized to exeucte any task in any case.
		    	boolean authorized=false;
		    	
		    	
		    	if((authorized && !ScheduledTaskQueue.isExecutingOrQueued()) || currentItem.getSf().isDependencyfailed()){	
 						currentItem.getSf().addCallBack(new StackFrameCallBack(){								
							public void callBack(StackFrame sf,String status, SchedulerException se){				
								String sid=sf.getData().get("id")+"";
								try{
									int id=Integer.parseInt(sid);											
									//updates time of local execution....
							  		IncomingMessage.updateFinishedPeersTime(P2PService.getComputerName(),id,sf.getTrigger_time());											
									executionEnded(id);
								}catch(Exception e){
									//e.printStackTrace();
									log.error("error 28:"+e.getMessage());
								}
							}
					     });				
					ScheduledTaskQueue.add(currentItem.getSf());
					//executionStarted(currentItem.getSchedulerid(),P2PService.getComputerName());
					executionStarted(currentItem,currentItem.getSf().getTrigger_time(),P2PService.getComputerName());					
					IncomingMessage.updateExecutingPeersTime(P2PService.getComputerName(),"BUSY",currentItem.getSchedulerid(),currentItem.getSf().getTrigger_time());
						 
				}else if (autclients!=null){  //added on 12-may-2011 if (autclients!=null){   
 						
					Vector<Object> autclients1=(Vector<Object>)autclients.clone();
					if(autclients1!=null && autclients1.size()>0){
	 					//publishTender(autclients1,currentItem);
						//Debugger.addDebugMsg(" script id:"+currentItem.getSchedulerid()+" authClients found:"+autclients+" sending to client");
						//sendTask2Peer(autclients1,currentItem);
						sendTask2Peer(autclients1,currentItem.getSchedulerid(),currentItem.getSf().getTrigger_time(),currentItem.getSf().getNexttrigger_time(),currentItem.getSf().getTask().getUniqueid());
								
 					} 
					autclients1=null;
				}
		    	if(autclients!=null){
		    		autclients.clear();
		    	}
		    	autclients=null;
		    }
 			
		//}
    }
    
    
	
	private void updateLogTimes(LoadBalancingQueueItem currentItem ) {
		if(currentItem.getStarted()!=null){
			SchedulerDB sdb=SchedulerDB.getSchedulerDB();			
		    try{
		    	sdb.connectDB();	 
		    	sdb.updateServerStartEnd(currentItem.getSchedulerid(),currentItem.getSf().getNexttrigger_time(),currentItem.getStarted(),new Date());
		    }catch(Exception e){
		    	e.printStackTrace();
		    }finally{
		    	try{
		    	sdb.closeDB();
		    	}catch(Exception e1){}
		    }
		}
	}

	
	
	
	
	/**
	 * 
	 * @param machinename
	 *  
	 * @return
	 */
	/*
	public static boolean isPeerBusy( String machinename){
		//1 started at this call
		//0 is not started at this call
		//-1 invalid
		boolean rtn=false;
		for(Iterator<LoadBalancingQueueItem> i=queueprocessing.iterator();i.hasNext();){
			LoadBalancingQueueItem item=i.next();
			if(item.getMachine().equals(machinename)){			   
				rtn=true;
			}
		}		
		return rtn;
	}
	*/
	
	
	
	
	
	
	public boolean killQueuedTask(int scheduler_id, long trigger_time)  {	
		
		boolean removed=false;
		for(Iterator<LoadBalancingQueueItem> i=queue.iterator();(i.hasNext() && !removed);){
			LoadBalancingQueueItem item=i.next();
			if(item.getSchedulerid()==scheduler_id && item.getSf()!=null && item.getSf().getTrigger_time()==trigger_time){			
				new SchedulerExePlanLogs(scheduler_id,trigger_time).log("Queued task killed by user",SchedulerExePlanLogs.IGNORE_CODE);
	    		removeFromQueue(item);
	    		removed=true;
			}
		}
		return removed;
	}
	

	
	public LoadBalancingQueueItem getItemFromProcessingQueue(int scheduler_id, long trigger_time)  {
		
		LoadBalancingQueueItem item2remove=null;
		for(Iterator<LoadBalancingQueueItem> i=queueprocessing.iterator();(i.hasNext());){
			LoadBalancingQueueItem item=i.next();
			if(item.getSchedulerid()==scheduler_id && item.getSf()!=null && item.getSf().getTrigger_time()==trigger_time){				
				item2remove=item;	    		
			}
		}		 
		return item2remove;
	}

	
	public boolean removeFaultyProcessingTask(int scheduler_id, long trigger_time)  {	
		
		boolean removed=false;
		LoadBalancingQueueItem item2remove=null;
		
		for(Iterator<LoadBalancingQueueItem> i=queueprocessing.iterator();(i.hasNext() && !removed);){
			LoadBalancingQueueItem item=i.next();
			if(item.getSchedulerid()==scheduler_id && item.getSf()!=null && item.getSf().getTrigger_time()==trigger_time){				
	    		//removeFromQueue(tem);
				//removeItemProcessing(item);
				item2remove=item;	    		
			}
		}
		if(item2remove!=null) {
			//queueprocessing.remove(item2remove);
			try{				
				removeItemProcessing(item2remove,null,SchedulerExePlanLogs.IGNORE_CODE);
				removed=true;
			}catch(Exception e){  }
		}
		return removed;
	}

	public synchronized int startedIfNotStarted(int schedulerid,long trigger_time, String machinename){
		//1 started at this call
		//0 is not started at this call
		//-1 invalid
		int rtn=-1;
		for(Iterator<LoadBalancingQueueItem> i=queue.iterator();i.hasNext();){
			LoadBalancingQueueItem item=i.next();
			if(item.getSchedulerid()==schedulerid && item.isExecuting()){			   
				rtn=0;
			}
			if(item.getSchedulerid()==schedulerid && !item.isExecuting()){
				rtn=1;
				executionStarted(item,trigger_time,machinename);
			}
		}		
		return rtn;
	}
	

	public synchronized void executionFailed(int schedulerid, long trigger_time, String machinename){
		for(Iterator<LoadBalancingQueueItem> i=queueprocessing.iterator();i.hasNext();){
			LoadBalancingQueueItem item=i.next();
			if(item.getSchedulerid()==schedulerid && item.getMachine().equals(machinename)){
			 			
				item.setMachine(null);
				item.setExecuting(false);
				item.setStarted(null);		
				//exitQueueIteration=true;
				//queue.poll(); //removes once it is executed
			     removeItemProcessing(item,null,SchedulerExePlanLogs.IGNORE_CODE);
			     //add(item); //this line removed because it never add
			     queue.add(item);
			    // queue.
			}
		}
	}
	
	
	/**
	 
	 * @param machinename
	 * @param bid_scheduler_id
	 */
	/*
	public static void moveClientFailed2Queue(String machinename,int bid_scheduler_id)  {
		//1 started at this call
		//0 is not started at this call
		//-1 invalid
		boolean rtn=false;
 
		LoadBalancingQueueItem item= new LoadBalancingQueueItem();
		item.setSchedulerid(bid_scheduler_id);		
		LoadBalancingQueueItem item1=getItemFromQueue(queueprocessing,item);
		if(item1!=null){
			removeItemProcessing(item1,null);
			item.setMachine(null);
			item.setExecuting(false);
			item.setStarted(null);	
    		add(item1);
		}
		
	}
	*/
	
	
	/*
	public synchronized static void executionStarted(int schedulerid, String machinename){
		
		for(Iterator<LoadBalancingQueueItem> i=queue.iterator();i.hasNext();){
			LoadBalancingQueueItem item=i.next();
			if(item.getSchedulerid()==schedulerid){
				//System.out.println("LoadBalancingQueue:setting executing started:"+item.getSchedulerid());				
				item.setMachine(machinename);
				item.setExecuting(true);
				item.setStarted(new Date());				
				
				//queue.poll(); //removes once it is executed
				//exitQueueIteration=true;
				
				removeFromQueue(item);
				addItemProcessing(item);
				
			}
		}
		
	}
	*/
	
	private synchronized void executionStarted(LoadBalancingQueueItem item, long trigger_time, String machinename){
		
		
			
		
				//System.out.println("LoadBalancingQueue:setting executing started:"+item.getSchedulerid());				
				item.setMachine(machinename);
				item.setExecuting(true);
				item.setStarted(new Date());				
				
				//queue.poll(); //removes once it is executed
				//exitQueueIteration=true;
				
				removeFromQueue(item);
				//Debugger.addDebugMsg("script id:"+item.getSchedulerid()+" execution starting and removing from LB queue",""+item.getSchedulerid());
				
				addItemProcessing(item);
				//Debugger.addDebugMsg("script id:"+item.getSchedulerid()+" adding to LB executing Queue",""+item.getSchedulerid());
		 
		
	}

	
	
	
	public void executionEnded(int schedulerid) {
		LoadBalancingQueueItem item= new LoadBalancingQueueItem();
		item.setSchedulerid(schedulerid);		
		LoadBalancingQueueItem item1=getItemFromQueue(queueprocessing,item);		 
		
		
		if(queueprocessing.contains(item1)) removeItemProcessing(item1,null,SchedulerExePlanLogs.IGNORE_CODE);
 					  
		lastExcecutedTime=new Date().getTime();
	}
	
	public void executionEnded(int schedulerid, long trigger_time) {
		LoadBalancingQueueItem item= new LoadBalancingQueueItem();
		item.setSchedulerid(schedulerid);
		
		LoadBalancingQueueItem item1=null;
		for(Iterator<LoadBalancingQueueItem> i=queueprocessing.iterator();i.hasNext();){
			LoadBalancingQueueItem cit=i.next();
			if(cit.getSchedulerid()==schedulerid && cit.getSf()!=null &&  cit.getSf().getTrigger_time()==trigger_time ){
				item1=cit;
			}
		}		
		if(queueprocessing.contains(item1)) removeItemProcessing(item1,null,SchedulerExePlanLogs.IGNORE_CODE);
 	
		
		lastExcecutedTime=new Date().getTime();
	}
	
	
	/**
	 * @deprecated
	 */
	public void cleanupProccesingQueue(int schedulerid, String computername) {
		LoadBalancingQueueItem item= new LoadBalancingQueueItem();
		item.setSchedulerid(schedulerid);		
		LoadBalancingQueueItem item1=getItemFromQueue(queueprocessing,item);
		if(queueprocessing.contains(item1) && item1.getMachine().equals(computername)) removeItemProcessing(item1,null,SchedulerExePlanLogs.IGNORE_CODE);
	}
	
	private LoadBalancingQueueItem getItemFromQueue(Collection<LoadBalancingQueueItem> q, LoadBalancingQueueItem itm){
		LoadBalancingQueueItem rtn=null;
		if(q.contains(itm)){
			for(Iterator<LoadBalancingQueueItem> i=q.iterator();i.hasNext();){
				LoadBalancingQueueItem cit=i.next();
				if(cit.getSchedulerid()==itm.getSchedulerid()){
					rtn=cit;
				}
			}
		}
		return rtn;
	}
	
	public Collection<LoadBalancingQueueItem> getExecutingTasks(){
		return queueprocessing;
	}
	public Collection<LoadBalancingQueueItem> getQueuedTasks(){
		return queue;
	}
	
	
	
	
	
	
	
	
	/* 
	private synchronized void  sendTask2Peer(Vector<Object> peers,LoadBalancingQueueItem currentItem) throws Exception {
		 
		   
	    boolean first=true;
	    		   
		Vector<String> v2=(Vector<String>)shuffleIteration.clone();				
		Vector<Object> v3=new Vector<Object>();
		for(Iterator<String> i=v2.iterator();i.hasNext();){
			Object ob=i.next();
			if(peers.contains(ob)){v3.add(ob);};
		}
		peers.removeAll(v3);
		peers.addAll(v3);
		
		 StackFrame sframe=currentItem.getSf();
		
	 
 
	 	boolean firstElement=true;
	 	boolean sent=false;

		
	 	for(Iterator<Object> i=peers.iterator();i.hasNext();){	 		
	 		
	 		String clientname=(String)i.next();
 
		    
				TenderSchedulerTask tst=new TenderSchedulerTask();
				tst.setScheduler_id(currentItem.getSchedulerid()+"");
				tst.setTrigger_time(sframe.getTrigger_time()+"");
				tst.setNext_trigger_time(sframe.getNexttrigger_time()+"");
				tst.setTaskuid(sframe.getTask().getUniqueid());
				new PostMessage(tst,clientname).send();	
				
				Debugger.addDebugMsg("Msg to peer "+clientname+ " sc_id:"+ currentItem.getSchedulerid()+" tr_time: "+sframe.getTrigger_time(),
						clientname+ " "+ currentItem.getSchedulerid()+" "+sframe.getTrigger_time()
						);
				
				//log.debug("Sending to message to peer:"+clientname);
				
				if(first){
			    	if(shuffleIteration.contains(clientname)){
			    		shuffleIteration.remove(clientname);
			    	}
			    	shuffleIteration.add(clientname);
			    }
			    first=false;
				 
		      	Thread.sleep(5);
		      	//pipeService=null;
		      	//ogM=null;
		      	//omc=null;
		      	//pipeAdv=null;
	 		//}
	 		 
	 	}
	 	
	 	v2=null;				
		v3=null;
         
   }
	*/ 
   
	/**
	 * @deprecated	
	 * @param peers
	 * @param currentItem
	 * @throws Exception
	 */
	private synchronized void  publishTender(Vector<Object> peers,LoadBalancingQueueItem currentItem) throws Exception {
		 
		   
		    boolean first=true;
		    		   
			Vector<String> v2=(Vector<String>)shuffleIteration.clone();				
			Vector<Object> v3=new Vector<Object>();
			for(Iterator<String> i=v2.iterator();i.hasNext();){
				Object ob=i.next();
				if(peers.contains(ob)){v3.add(ob);};
			}
			peers.removeAll(v3);
			peers.addAll(v3);
		    
			
			/*
			Set executingpeers=IncomingMessage.getExecutingPeers().keySet();			 
			Vector executingpeers2 =new Vector();
			for(Iterator i=executingpeers.iterator();i.hasNext();){
				Object ob=i.next();
				if(peers.contains(ob)){executingpeers2.add(ob);};
			}
			
			//log.debug("peers:"+peers+"  executingpeers:"+executingpeers2);
			if(peers.containsAll(executingpeers2) && peers.size()==executingpeers2.size()){
				mayhavepublised2all=false;
			}
			*/		
		    //IncomingMessage.getMessages().clear();
			
			
		 	PeerGroup netPeerGroup=P2PService.getPeerGroup();
		 	StackFrame sframe=currentItem.getSf();
			MessageBean mb=new MessageBean();
			mb.setType(MessageBean.TYPE_REQUEST);
			mb.setReply(MessageBean.REPLYBACK);			
			if(sframe.getTask()!=null){
				mb.setCommand("EXECUTETASK:"+currentItem.getSchedulerid()+":"+sframe.getTrigger_time()+":"+sframe.getNexttrigger_time()+":"+sframe.getTask().getUniqueid()+":TENDER");
			}else{				
				mb.setCommand("EXECUTETASK:"+currentItem.getSchedulerid()+":"+sframe.getTrigger_time()+":"+sframe.getNexttrigger_time()+":"+":TENDER");
			}
			
		 	boolean firstElement=true;
		 	boolean sent=false;

			
		 	for(Iterator<Object> i=peers.iterator();i.hasNext();){	 		
		 		
		 		String clientname=(String)i.next();
		 		
		 		//to avoid kept sending messages continuously, it sends next message after one second
		 		//if(cache.get("tender_note_"+clientname)==null){
		 	
		 		//	IElementAttributes att= cache.getDefaultElementAttributes();
				//	att.setMaxLifeSeconds(1);
		 		//	cache.put("tender_note_"+clientname,"tendered",att);
		 		//	Debugger.addDebugMsg("Msg to peer "+clientname+ " sc_id:"+ currentItem.getSchedulerid()+" tr_time: "+sframe.getTrigger_time(),
		 		//			clientname+ " "+ currentItem.getSchedulerid()+" "+sframe.getTrigger_time()
		 		//			);
		 			
			 		PipeAdvertisement pipeAdv = new P2PAdvertisement().getPipeAdvertisement(clientname,netPeerGroup);		 		
			 		OutgoingMessageCallBack omc=new OutgoingMessageCallBack(){};
					omc.setPriority(OutgoingMessageCallBack.PRIORITY_NORMAL);				 
					OutgoingMessage ogM=new OutgoingMessage(omc,mb,clientname);
					PipeService pipeService = P2PService.getPipeService();
					
					try{					
						pipeService.createOutputPipe(pipeAdv,ogM);
					}catch(Exception e){
						e.printStackTrace();
					}
					
					 
					if(first){
				    	if(shuffleIteration.contains(clientname)){
				    		shuffleIteration.remove(clientname);
				    	}
				    	shuffleIteration.add(clientname);
				    }
				    first=false;
					 
			      	Thread.sleep(20);
			      	pipeService=null;
			      	ogM=null;
			      	omc=null;
			      	pipeAdv=null;
		 		//}
		 		 
		 	}
		 	
		 	v2=null;				
			v3=null;
             
	   }
	
 
		 
		public synchronized void executeScript(Vector<Object> peers,int scriptid) throws Exception {			 
			   
		    
		    boolean first=true;
			Vector<String> v2=(Vector<String>)shuffleIteration.clone();
			
			Vector<Object> v3=new Vector<Object>();
			for(Iterator<String> i=v2.iterator();i.hasNext();){
				Object ob=i.next();
				if(peers.contains(ob)){v3.add(ob);};
			}
			peers.removeAll(v3);
			peers.addAll(v3);
			
			 
		 	PeerGroup netPeerGroup=P2PService.getPeerGroup();
		 	 
			MessageBean mb=new MessageBean();
			mb.setType(MessageBean.TYPE_REQUEST);
			mb.setReply(MessageBean.REPLYBACK);			
			mb.setCommand("EXECUTESCRIPT:"+scriptid+":CONFIRM");		 	
		  
 
		 	for(Iterator<Object> i=peers.iterator();i.hasNext();){
	 		
		 		String clientname=(String)i.next();	 		
 
		 		PipeAdvertisement pipeAdv = new P2PAdvertisement().getPipeAdvertisement(clientname,netPeerGroup);		 		
		 		OutgoingMessageCallBack omc=new OutgoingMessageCallBack(){};
				omc.setPriority(OutgoingMessageCallBack.PRIORITY_NORMAL);				 
				OutgoingMessage ogM=new OutgoingMessage(omc,mb,clientname);
				PipeService pipeService = P2PService.getPipeService();
				
				try{					
					pipeService.createOutputPipe(pipeAdv,ogM);
				}catch(Exception e){
					e.printStackTrace();
				}
		      	Thread.sleep(20);
		 		 
		 	}
	         
	 }


	 /*
	 public static void findAndUpdateOnlinePeers_original() throws Exception {
		 
		    //IncomingMessage.getMessages().clear();
		 	PeerGroup netPeerGroup=P2PService.getPeerGroup();
		  
			MessageBean mb=new MessageBean();
			mb.setType(MessageBean.TYPE_REQUEST);
			mb.setReply(MessageBean.REPLYBACK);
			//mb.setCommand("STATUS");		 	
			mb.setCommand(P2PTransportMessage.COMMAND_STATUS);
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
		 	
	 }
	 */
		
	
	  
	 
	 
	 //public static String R_PACKAGES="R_PACKAGES";
	 //public static String STATISTICS="STATISTICS";
	 //public static String PEER_QUEUE="PEER_QUEUE";
	 
	 
	
	 
	 
 
	 
	 
	 
	
	 
	 
	 
	 private Vector getGroupOrder() throws Exception {	
		 	if(cache==null){
				//cache=JCS.getInstance("perminentpeers");
		 		getCache();
			}	
			Vector order=(Vector)cache.get("taskgroup_order");
			if(order!=null && order.size()>0) {Object obj=order.get(0); } //this line is to keep the obj in memory even after cache expires 
			if(order==null){
				SchedulerDB sdb=SchedulerDB.getSchedulerDB();
				try{
					sdb.connectDB();					
					order=sdb.getGroupOrder();
				 	IElementAttributes att= cache.getDefaultElementAttributes();
					att.setMaxLifeSeconds(7);
					cache.put("taskgroup_order",order,att);
				}catch(Exception e){ throw e;}
				finally{
					sdb.closeDB();		
				}
			}
			return order;
	}
	 
	 
	 
	 
	
	 
	 
	 
	 /*
	 private static TimerTask tt=null;
	 private void cleanUpNonResponding(){
		   if(tt==null){
			    tt=new TimerTask() {
		            public void run() {
		               
		            	TreeMap<String,Integer> executingpeers=IncomingMessage.getExecutingPeers();
		    			for(Iterator<String> i=executingpeers.keySet().iterator();i.hasNext();){
		    				String machine=i.next();
		    				int scheduler_id=executingpeers.get(machine);
		    				try{
		    					LoadBalancingQueue.removeNonRespondingFromQ(machine, scheduler_id);
		    					//System.out.println("~~~~~~~~~~~~~~~ LoadBalancingQueue removing non-responded task from the queue");
		    				}catch(Exception e){}
		    			}
		            	
		            }
		        };	      
		        long freq=90*1000;
		        Timer timer = new Timer();		        
		        timer.scheduleAtFixedRate(tt,freq, freq);
		   }
	 }
	 */
	 
	 /*
		public void runOld() {		 
			
			try{
				while(!queue.isEmpty()){
					 
					LoadBalancingQueueItem currentItem=queue.peek();
					while(!currentItem.isExecuting()){				
						if(!ScheduledTaskQueue.isExecutingOrQueued()){	
						//if(ScheduledTaskQueue.addOrCheckQueue(null)<=0){
							
							//set call back method, this will be executed once the task execution ended.
							currentItem.getSf().addCallBack(new StackFrameCallBack(){
								
								public void callBack(StackFrame sf,String status){				
									//respond(MessageBean.TYPE_RESPONSE,"EXECUTETASK:"+sf.getData().get("id")+":"+status+":FINISHED",sf.getMbean());
									String sid=sf.getData().get("id")+"";
									try{
										int id=Integer.parseInt(sid);
										executionEnded(id);
									}catch(Exception e){
										e.printStackTrace();
									}
								}
							});						
							ScheduledTaskQueue.add(currentItem.getSf());
							executionStarted(currentItem.getSchedulerid(),P2PService.getComputerName());
							//System.out.println("LoadBalancingQueue:Executing task locally:"+currentItem.isExecuting());
						}else{
							//advertish to clients to take over the task					 
							publishTender(getStaticClients(),currentItem);
						}
						//System.out.println("LoadBalancingQueue:Item.Running:"+currentItem.isExecuting());					
						Thread.sleep(400);
						lastExcecutedTime=new Date().getTime();
					}
					
					updateLogTimes(currentItem);
					queue.poll(); //removes once it is executeds
				}
			}catch(Exception e){
				
			}finally{
				threadRunning=false;
			}
		}
		*/
	 
	
	 
	  
	 
	 
		/**
		 * kill only incase of queue crashed....
		 */
		public  Map<String, Integer> debug_data(){
			HashMap<String, Integer> h=new HashMap<String, Integer>();
			h.put("scriptQueueListener_size",scriptQueueListener.size());
			h.put("scriptQueueVec_size",scriptQueueVec.size());
			h.put("scriptQueue_size",scriptQueue.size());
			
			h.put("queue_size",queue.size());
			h.put("queueprocessing_size",queueprocessing.size());
			
			return h;
			
		}

		
		@Override
		public void peerStarted(int scheduler_id, long trigger_time,
				String peername) throws Exception {
			//do nothing on LinkedQueue implementation
			
		}

	 
		public Collection<RScript> getScriptQueue() throws Exception {
		 
			return scriptQueue;
		}
		
		
		public Collection<RScript> getScriptProcessingQueue() throws Exception {
		 
			return scriptQueueProcessing;
		}

		
		 
		public  void removeScriptFromAllQueue(RScript rscript) {			
			
			try{
				scriptQueueProcessing.remove(rscript);		
				
				RScript rscript1=scriptQueueVec.get(scriptQueueVec.indexOf(rscript));		
	 			String ky=rscript1.getPeer()+"_"+rscript1.getTaskuid();
	 			rscript1.setError(rscript.getError());
	 			cache=getCache(); 			
				cache.remove(ky);
				scriptQueueVec.remove(rscript);				
				
			}catch(Exception e){			
				e.printStackTrace();
			}
			
		    
			
			
		}
		
		
		
		
		
		
	
		
}





