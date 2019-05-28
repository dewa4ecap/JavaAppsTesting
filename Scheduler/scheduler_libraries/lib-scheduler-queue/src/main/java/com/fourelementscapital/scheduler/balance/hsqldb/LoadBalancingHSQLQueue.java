/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.balance.hsqldb;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fourelementscapital.db.SchedulerDB;
import com.fourelementscapital.scheduler.ScheduledTaskFactory;
import com.fourelementscapital.scheduler.balance.LoadBalancingQueueItem;
import com.fourelementscapital.scheduler.balance.LoadBalancingQueueTimeout;
import com.fourelementscapital.scheduler.engines.ScheduledTask;
import com.fourelementscapital.scheduler.engines.SchedulerExePlanLogs;
import com.fourelementscapital.scheduler.p2p.P2PService;
import com.fourelementscapital.scheduler.rscript.RScript;
import com.fourelementscapital.scheduler.rscript.RScriptListener;

public class LoadBalancingHSQLQueue extends LoadBalancingHSQLLayerDB implements Callable  {
	
	private Logger log = LogManager.getLogger(LoadBalancingHSQLQueue.class.getName());
		
	private Future<String> future=null; 
	private ExecutorService executor = Executors.newCachedThreadPool();
	private static Semaphore conslock=new Semaphore(1,true);
	
	public LoadBalancingHSQLQueue(){		
		initDB();	
		future=executor.submit(this);				
	}
	
	public void add(LoadBalancingQueueItem item_parent) {
	
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		acquireLock();
		try{
			sdb.connectDB();
			
			LoadBalancingQueueTimeout tqt=new LoadBalancingQueueTimeout(sdb,new ScheduledTaskFactory().getTaskUids());
			setAlertRange( tqt.getMaxWaitingAlert());
			
			LoadBalancingHSQLQueueItem item=new LoadBalancingHSQLQueueItem();
			item.setTaskuid(item_parent.getSf().getTask().getUniqueid());
			
			//BeanUtils.copyProperties throws an error for null date properties
			if(item_parent.getStarted()==null){
				item_parent.setStarted(new Date());
			}
			
			BeanUtils.copyProperties(item, item_parent);
			BeanUtils.copyProperties(item, item_parent.getSf()); //copy items from stackframe			

			Map data=item_parent.getSf().getData();
			if(data!=null){
				String dids=(String)data.get(ScheduledTask.FIELD_DEPENDENCY_IDS);
				item.setDependentids(dids);
				String dtime=(String)data.get(ScheduledTask.FIELD_DEPENDENCY_CHECKTIME);				
				item.setDependentchecktime(dtime);						
				try{
					item.setConcurrentexecution(Integer.parseInt((String)data.get(ScheduledTask.FIELD_CONCURRENT_EXEC)));					
				}catch(Exception e){		}
			}
			
			TreeMap<String, Comparable> ldata=new TreeMap<String, Comparable>();
			ldata.put("scheduler_id", item.getSchedulerid());
			ldata.put("trigger_time",item.getTrigger_time());
			ldata.put("inject_code", item.getInject_code());
			
			//set it back to null, as the execution isn't started.
			item.setStarted(null); //as BeanUtil		

			int status=0;
			String adderror=null;
			try{
				addLastExecutionDuration(sdb, item);
				addTimeoutAndLastExecTimes(sdb,item);
				log.debug("addTimeoutAndLastExecTimes queue:"+item.getLastExecutedDuration());
				status=add2DBQueue(item);
			}catch(Exception e){
				adderror=e.getMessage();
				status=QUEUE_ERROR_FOUND;
			}
			boolean logging_required=true;
			//overlapping 
			if(status==QUEUE_OVERLAPPED){
				
				ldata.put("status", ScheduledTask.EXCECUTION_OVERLAPPED);
				ldata.put("is_triggered",new Integer(1));  
				//LoadBalancingHSQLQueueItem other_item=getItemFromQueue(item.getSchedulerid());
				List<LoadBalancingHSQLQueueItem> other_items=getItemsFromQueue(item.getSchedulerid());
				if(other_items!=null && other_items.size()>0){
					  logging_required=true;
					  SimpleDateFormat sdf=new SimpleDateFormat("dd-MMM hh:mm:ss");
					  Date d=new Date();
					  String message="";
					  log.debug("Item overlapped and adding execution log");
					  
					  if( other_items.size()==1){						
						d.setTime(other_items.get(0).getTrigger_time());
						 message="Rejected, The task is already queued, added at "+sdf.format(d);
						if(other_items.get(0).getStarted()!=null){
							message="Failed, The task is already executing, started at "+sdf.format(other_items.get(0).getStarted());							
						} 						
					  }
					  //concurrent execution overlap info.
					  if( other_items.size()>1){						  
						message="Rejected, The task is already queued/executing in "+other_items.size()+" concurrent threads and are scheduled/added at ";
						String ma="";
						for(LoadBalancingHSQLQueueItem ot_it:other_items){
							d.setTime(ot_it.getTrigger_time());
							ma+=ma.equals("")?sdf.format(d):", "+sdf.format(d);
						}
						message+=ma;
					  }
					  new SchedulerExePlanLogs(item.getSchedulerid(),item.getTrigger_time()).log(message,SchedulerExePlanLogs.SERVER_WARNING_OVERLAPPED);
					  
				}
			}else if(status==QUEUE_DUPLICATE_FOUND){				
				logging_required=false; //ignores the same scheduler id and trigger time, this could be due to multiple cron expression time overlapping 
			}else if(status==QUEUE_ERROR_FOUND){				
				if(adderror!=null){
					new SchedulerExePlanLogs(item.getSchedulerid(),item.getTrigger_time()).log("Error while adding into queue, scheduler_id: "+item.getSchedulerid()+" trig_time:"+item.getTrigger_time()+" Err:"+adderror,SchedulerExePlanLogs.SERVER_ERROR_WHILE_ADDING_QUEUE);
				}
				//ldata.put("status", "failed");
			}else{
				
			}			
			if(logging_required){
				ArrayList<TreeMap<String, Comparable>> v=new ArrayList<TreeMap<String, Comparable>>();
				v.add(ldata);
				sdb.updateQueueLog(v,new Vector(), P2PService.getComputerName());
			}			 
			if(future==null || (future!=null && future.isDone() )){
				future=executor.submit(this);				
			}
			
		}catch(Exception e){
			e.printStackTrace();
			log.error("Error while adding into queue, Error:"+e.getMessage());
		}finally{
			releaseLock();
			try{
				sdb.closeDB();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	public void end() throws Exception {
		//this.connection.close();
		System.out.println("closing the connection....");
	}
	

	/**
	 * override this method here and ignore it as LoadbalancingHSQLQueue uses semaphores
	 */
	public boolean isPeerBusyWithTask(String machinename,int bid_scheduler_id)  {
		 return false;
	}
	 
	public Object call() throws Exception { 
		log.debug("call()");
		queueLoop();
		return "done";	
	}
	
	protected void processQueueItem(LoadBalancingHSQLQueueItem currentItem) throws Exception {


			if(currentItem!=null && !currentItem.isExecuting()){
			    String taskuid=currentItem.getSf().getTask().getUniqueid();
		    	Vector<Object> autclients=getRunOnlyOn(taskuid);
		    	    //unix server is not authorized to exeucte any task in any case. 						
					Vector<Object> autclients1=(Vector<Object>)autclients.clone();
					if(autclients1!=null && autclients1.size()>0){
						//sendTask2Peer(autclients1,currentItem);
						//to be implemented here (remove the above line )
						//sendTask2Peer(autclients1,currentItem.getSchedulerid(),currentItem.getTrigger_time(),currentItem.getNexttrigger_time(),currentItem.getTaskuid());						
						//remove peers that are already rejected because of technical issues.
						if(currentItem.getStarted_peers()!=null){
							StringTokenizer st=new StringTokenizer(currentItem.getStarted_peers(),",");
							while(st.hasMoreTokens()){
								String p=st.nextToken();
								if(autclients1.contains(p)) autclients1.remove(p);
							}
						}						
						sendTask2Peer(autclients1,currentItem.getSchedulerid(),currentItem.getTrigger_time(),currentItem.getNexttrigger_time(),currentItem.getTaskuid());
						
 					} 
					autclients1=null;
					
		    	if(autclients!=null){
		    		autclients.clear();
		    	}
		    	autclients=null;
		    } 			
    }


	
	@Override
	public void addExecuteR(RScript item, RScriptListener listener) throws Exception {
		    //TODO Auto-generated method stub
	}

	
	//@Override
	//public Vector<LoadBalancingQueueItem> getAllTasks() {
	//	// TODO Auto-generated method stub
	//	return null;
	//}

	public long lastExcecutedTime(){
		return lastExcecutedTime;
	}

	@Override
	public RScript startScriptIfNotStarted(RScript rscript, String peer) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void scriptFinished(RScript rscript, String result, String status) {
		// TODO Auto-generated method stub
		
	}

	
	
	//@Override
	//public void removeItemProcessing(LoadBalancingQueueItem item, String message) {
	//	// TODO Auto-generated method stub
	//	
	//}
 

	 

	//@Override
	//public boolean removeFaultyProcessingTask(int scheduler_id,
	//		long trigger_time) {
	//	// TODO Auto-generated method stub
	//	return false;
	//}

 
	
	//@Override
	//public void executionFailed(int schedulerid, String machinename) {
		// TODO Auto-generated method stub
		
	//}
 
	
	//@Override
	//public void executionEnded(int schedulerid, long trigger_time) {
		// TODO Auto-generated method stub
	//	
	//}

	//@Override
	//public void cleanupProccesingQueue(int schedulerid, String computername) {
		// TODO Auto-generated method stub
		
	//}

	//@Override
	//public Collection<LoadBalancingQueueItem> getExecutingTasks() {
		// TODO Auto-generated method stub
	//	return null;
	//}

	//@Override
	//public Collection<LoadBalancingQueueItem> getQueuedTasks() {
	//	// TODO Auto-generated method stub
	//	return null;
	//}

	@Override
	public void executeScript(Vector<Object> peers, int scriptid)
			throws Exception {
		// TODO Auto-generated method stub		
	}

	
	/**
	 * @deprecated
	 */
	@Override
	public void executionEnded(int schedulerid) {
		// TODO Auto-generated method stub
	}

	@Override
	public Collection<RScript> getScriptQueue() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<RScript> getScriptProcessingQueue() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public  void removeScriptFromAllQueue(RScript rscript) {
		
		
	}
	
	//@Override
	//public LoadBalancingQueueItem getItemFromProcessingQueue(int scheduler_id,
	//		long trigger_time) {
	//	// TODO Auto-generated method stub
	//	return null;
	//}
	

}


