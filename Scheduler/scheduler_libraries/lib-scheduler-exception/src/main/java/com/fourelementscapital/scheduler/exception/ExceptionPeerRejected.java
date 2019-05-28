/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.exception;

public class ExceptionPeerRejected extends SchedulerException {

	 
	public ExceptionPeerRejected(String msg) {
		super(msg==null?"No Room to Execute the Script":msg);		
	}

	public int getErrorcode() {
		 
		return FAIL_PEER_REJECTED;
	}
	

}


