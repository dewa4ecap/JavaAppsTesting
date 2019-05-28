/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler;

import java.util.Date;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.fourelementscapital.db.SchedulerDB;
import com.fourelementscapital.scheduler.balance.LoadBalancingQueue;
import com.fourelementscapital.scheduler.balance.LoadBalancingQueueItem;
import com.fourelementscapital.scheduler.config.Config;
import com.fourelementscapital.scheduler.engines.ScheduledTask;
import com.fourelementscapital.scheduler.engines.StackFrame;
import com.fourelementscapital.scheduler.p2p.P2PService;

public class ScheduledTaskJob implements Job {
	
	private Logger log = LogManager.getLogger(ScheduledTaskJob.class.getName());

	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		//ate sdate=new Date();
		//log.debug("Excecuted task previous fire time:"+context.getTrigger().getPreviousFireTime().getTime()+"  next firetime:"+context.getTrigger().getNextFireTime().getTime());
		
		/*
		Map data=(Map)context.getJobDetail().getJobDataMap().get("data");		
		ScheduledTask task=(ScheduledTask)context.getJobDetail().getJobDataMap().get("task");		
		StackFrame sframe=(StackFrame)context.getJobDetail().getJobDataMap().get("stackframe");
		*/
		
		int scheduler_id=(Integer)context.getJobDetail().getJobDataMap().get("scheduler_id");		
    	String taskuid=(String)context.getJobDetail().getJobDataMap().get("taskuid");
    	String invoked_by=(String)context.getJobDetail().getJobDataMap().get(SchedulerEngine.JOBDATA_INVOKED_BY);
    	String updatedtime=(String)context.getJobDetail().getJobDataMap().get(SchedulerEngine.JOBDATA_UPDATED_TIME);
    	Number trigger_row_id=(Number)context.getJobDetail().getJobDataMap().get(SchedulerEngine.JOBDATA_TRIGGER_ROW_ID);
    	//System.out.println("ScheduledTaskJob.execute() 2be removed later:taskuid:"+taskuid+" scheduler_id:"+scheduler_id);
    	
    	
    	
    	try{
    		
	    	//Map data=getSchedulerData(scheduler_id);
    		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
    		Map data=null;
    		String inject_code=null;
    		try{
    			sdb.connectDB();
    			data=sdb.getScheduler(scheduler_id);    
    			if(trigger_row_id!=null){
    				Map trig=sdb.getOneRowTriggerData(trigger_row_id.longValue());
    				inject_code=(String)trig.get("inject_code");
    			}
    		}catch(Exception e){
    			throw e;
    		}finally {
    			sdb.closeDB();
    		}
    		
    		if(data.get("deleted")!=null && ((Number)data.get("deleted")).intValue()==1){
    			throw new Exception("Deleted Task can't be executed");
    		}
    		
    		log.debug("trigger_row_id:"+trigger_row_id+"    inject_code:"+inject_code);
	    	
	    	ScheduledTask task=new ScheduledTaskFactory().getTask(taskuid);
	    	if(task==null) {
	    		throw new Exception("Task Group not found for the task:"+scheduler_id);
	    	}
	    	StackFrame sframe=new StackFrame(task,data);
	    	if(invoked_by!=null && !invoked_by.equals("")) {
	    		sframe.setInvokedby(invoked_by);
	    	}else{
	    		if(updatedtime!=null){
	    			sframe.setInvokedby("Scheduler ("+updatedtime+")");
	    		}else{
	    			sframe.setInvokedby("Scheduler");
	    		}
	    	}
			
			if(context.getTrigger().getPreviousFireTime()!=null){
				sframe.setTrigger_time(context.getTrigger().getPreviousFireTime().getTime());
			}else{
				sframe.setTrigger_time(new Date().getTime());
			}
			
			if(context.getTrigger().getNextFireTime()!=null){
				sframe.setNexttrigger_time(context.getTrigger().getNextFireTime().getTime());
			}			
		
			
			if(Config.getValue("load_balancing_server")!=null && Config.getValue("load_balancing_server").equals(P2PService.getComputerName())){
				LoadBalancingQueueItem li=new LoadBalancingQueueItem();
				li.setSf(sframe);
				Integer id=(Integer)data.get("id");
				li.setInject_code(inject_code);
				li.setSchedulerid(id);
				LoadBalancingQueue.getDefault().add(li);
				
			}else{
				/*ScheduledTaskQueue.add(sframe);*/
			}
			
			log.debug("adding task to queue: task:"+data.get("name"));
    	}catch(Exception e){
    		e.printStackTrace();    		
    		//ClientErrorMgmt.reportError(e, "Error while triggering task");
    	} 
		 
		//String status=null;
		//try{		
			//task.execute();
			//status=task.EXCECUTION_SUCCESS;
		//}catch(Exception e){
		//	status=task.EXCECUTION_FAIL;			
		//	ClientErrorMgmt.reportError(e, null);
		//}finally{
		//	try{
		//		addLog(sdate,data,status);
		//	}catch(Exception e){
		//		ClientErrorMgmt.reportError(e, null);
		//	}			
		//}
		
    	
    	
	}

	
 
 

	/**
	 * @deprecated
	 */
	private void addLog(Date start, Map data, String status) throws Exception {
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		sdb.connectDB();
		//System.out.println("ScheduledTaskJob.addLog() data:"+data);
		Number nid=(Number)data.get("id");
		String timezone=(String)data.get("timezone");
		Date end=new Date();
		int id= nid.intValue();
		sdb.addSchedulerLog(id, start, end, timezone, status,null);
		sdb.closeDB();
		
	}

 
}


