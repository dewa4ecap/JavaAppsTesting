/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.exception;

public class ExceptionRServeUnixFailure extends ExceptionSchedulerTeamRelated {
	
	
	public ExceptionRServeUnixFailure(String msg) {
		super(msg);
	 
	}
	 
	public int getErrorcode() {
		 
		return ERROR_PEER_FAILED_2START_UNIX_RSERVE;
	}

}


