/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.exception;

public class ExceptionRscriptError extends SchedulerException {

	public ExceptionRscriptError(String msg) {
		super(msg);
		 
	}

 
	public int getErrorcode() {
	 
		return ERROR_SERVER_GENERAL_SCRIPT_ERROR;
	}

}


