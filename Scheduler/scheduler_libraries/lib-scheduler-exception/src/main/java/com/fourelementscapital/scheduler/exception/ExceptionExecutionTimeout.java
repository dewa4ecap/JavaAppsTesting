/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.exception;

public class ExceptionExecutionTimeout extends ExceptionSchedulerTeamRelated {

	public ExceptionExecutionTimeout(String msg) {
		super(msg);		
	}

	public int getErrorcode() {
		 
		return EXECUTION_TIMEOUT;
	}
}


