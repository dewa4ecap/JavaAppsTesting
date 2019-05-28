/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fe.scheduler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;


public class TestJob implements Job {
	
	private Logger log = LogManager.getLogger(TestJob.class.getName());
	public void execute(JobExecutionContext context) throws JobExecutionException {
        //System.out.println("~~~~~~~~~~~~~~~~~~~~"+context.getJobDetail().getName());
		//log.debug("Job Executed:"+context.getJobDetail().getName());
		log.debug("Job Excecuted:"+context.getJobDetail().getJobDataMap().get("p1"));
    }

}


