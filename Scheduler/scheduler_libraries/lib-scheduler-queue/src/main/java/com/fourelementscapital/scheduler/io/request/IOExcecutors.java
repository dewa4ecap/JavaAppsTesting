/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.io.request;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IOExcecutors {
	
	//public static ExecutorService threadpool= Executors.newFixedThreadPool(30);
	public static ExecutorService threadpool= Executors.newCachedThreadPool();
	
	
}


