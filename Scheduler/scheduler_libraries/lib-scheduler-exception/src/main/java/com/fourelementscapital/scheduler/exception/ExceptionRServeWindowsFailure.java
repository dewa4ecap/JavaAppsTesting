/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.exception;

public class ExceptionRServeWindowsFailure extends ExceptionSchedulerTeamRelated {

	
	
	 
	public ExceptionRServeWindowsFailure(String msg) {
		super(msg);
	 
	}

	public int getErrorcode() {
	 
		return ERROR_PEER_FAILED_2START_WIN_RSERVE;
	}

}


