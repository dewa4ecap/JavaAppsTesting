/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.exception;

public class ExceptionRemoveFromQ extends ExceptionSchedulerTeamRelated {

	 
	public ExceptionRemoveFromQ(String msg) {
		super(msg);
		 
	}

	public int getErrorcode() {
		 
		return ERROR_SERVER_GENERAL_QUEUE_KICKCOUT;
	}

}


