/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.exception;

public class ExceptionUnixPeerUnknown extends ExceptionSchedulerTeamRelated {

	public ExceptionUnixPeerUnknown(String msg) {
		super(msg);
		
		
	}
	
 
	public int getErrorcode() {
		 return ERROR_UNIX_PEER_UNKNOWN;
	}

}


