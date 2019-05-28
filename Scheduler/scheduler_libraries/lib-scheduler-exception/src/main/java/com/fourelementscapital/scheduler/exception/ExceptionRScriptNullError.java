/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.exception;

public class ExceptionRScriptNullError extends ExceptionSchedulerTeamRelated {

	public ExceptionRScriptNullError(String msg) {
		super(msg);
		 
	}

 
	public int getErrorcode() {
		 
		return ERROR_PEER_R_RETURN_NULL;
	}

}


