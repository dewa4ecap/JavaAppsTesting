/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.exception;

public class ExceptionDependencyTimeout extends SchedulerException {
	
	
	public ExceptionDependencyTimeout(String msg) {
		super(msg);		
	}

	public int getErrorcode() {
		 
		return DEPENDENCY_TIMEDOUT;
	}
}


