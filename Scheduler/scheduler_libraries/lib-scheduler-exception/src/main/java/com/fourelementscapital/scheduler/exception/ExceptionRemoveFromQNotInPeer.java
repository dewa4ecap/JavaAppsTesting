/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.exception;

public class ExceptionRemoveFromQNotInPeer extends SchedulerException {

	public ExceptionRemoveFromQNotInPeer(String msg) {
		super(msg);
		 
	}

	 
	public int getErrorcode() {
 
		return ERROR_SERVER_REMOVE_NOT_RUNNING_INPEER;
	}

}


