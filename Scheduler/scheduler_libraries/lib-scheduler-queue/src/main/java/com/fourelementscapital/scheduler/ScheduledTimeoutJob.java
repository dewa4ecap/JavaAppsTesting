/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.fourelementscapital.scheduler.balance.LoadBalancingQueue;

public class ScheduledTimeoutJob implements Job {

	 
	public static final String SCHEDULER_ID="scheduler_id";
	public static final String TRIGGER_TIME="trigger_time"; 
	public static final String STARTED_TIME="started_time";
	
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		int scheduler_id=(Integer)context.getJobDetail().getJobDataMap().get(ScheduledTimeoutJob.SCHEDULER_ID);		
    	Number trigger_time=(Number)context.getJobDetail().getJobDataMap().get(ScheduledTimeoutJob.TRIGGER_TIME);
    	Number started_time=(Number)context.getJobDetail().getJobDataMap().get(ScheduledTimeoutJob.STARTED_TIME);
    	LoadBalancingQueue.getDefault().taskTimedOut(scheduler_id,trigger_time.longValue(),started_time.longValue());
    	//System.out.println("---Executing task");
    	//System.out.println("scheduler_id:"+scheduler_id);
    	//System.out.println("trigger_id:"+trigger_time);
	}

}


