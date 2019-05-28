/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.exception;

public abstract class SchedulerException extends Exception {

	 
	
	
	public static final int ERROR_PEER_R_RETURN_NULL=2311;
	public static final int ERROR_SERVER_GENERAL_SCRIPT_ERROR=2312;
	public static final int ERROR_PEER_FAILED_2START_WIN_RSERVE=2113;
	public static final int ERROR_PEER_FAILED_2START_UNIX_RSERVE=2214;
	//public static final int ERROR_SERVER_GENERAL_EXE_TIMEOUT=2315;
	public static final int ERROR_SERVER_GENERAL_QUEUE_KICKCOUT=2316;
	public static final int ERROR_PEER_UNKNOWN=2319;
	public static final int ERROR_SERVER_UNKNOWN=2320;
	public static final int FAIL_PEER_REJECTED=2317;
	public static final int ERROR_UNIX_PEER_UNKNOWN= 2220;
	public static final int EXECUTION_TIMEOUT= 2315;
	public static final int DEPENDENCY_TIMEDOUT=2321;
	public static final int ERROR_SERVER_REMOVE_PEER_NO_RESPONSE=2317;
	public static final int ERROR_SERVER_REMOVE_NOT_RUNNING_INPEER=2318;
	
	
	private String message;
	
	public SchedulerException(String msg){
		this.message=msg;
	}
	
 
	public String getExceptionclass() {
		return this.getClass().getName();
	}
	
	public abstract int getErrorcode(); 
	 
	public String getMessage() {
		return message;
	}
	 
	protected void setMessage(String msg) {
		message=msg;
	}
	
	
}


