/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.exception;

public class ExceptionPeerNoResponse extends SchedulerException {
 
	
	public ExceptionPeerNoResponse(String msg) {
		super(msg);
		 
	}

	public int getErrorcode() {
		 return ERROR_SERVER_REMOVE_PEER_NO_RESPONSE;
	}

}


