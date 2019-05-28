/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.exception;

 

public class ExceptionPeerUnknown extends ExceptionSchedulerTeamRelated {

	 
	public ExceptionPeerUnknown(String msg) {
		super(msg);
		
		
	}

	public int getErrorcode() {
		 
		return ERROR_PEER_UNKNOWN;
	}

}


