/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.p2p.msg;

import java.util.Map;

public interface PostCallBack {
 
	public static String IGNORE_CALLBACK="IGNORE_CALLBACK";
	public void callBack(Map data);
	public void onCallBackSendingFailed();
	
}


