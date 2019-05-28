/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.exception;

public class ExceptionWarningNoFullData extends SchedulerException {

	public ExceptionWarningNoFullData(String msg) {
		super(msg);
	 
	}
	public int getErrorcode() {
		 
		return 0;
	}

}


