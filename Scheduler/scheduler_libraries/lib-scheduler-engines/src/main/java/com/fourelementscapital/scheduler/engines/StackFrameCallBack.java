/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.engines;

import com.fourelementscapital.scheduler.exception.SchedulerException;

public interface StackFrameCallBack {

	public void callBack(StackFrame sframe, String status,SchedulerException se);
	
}


