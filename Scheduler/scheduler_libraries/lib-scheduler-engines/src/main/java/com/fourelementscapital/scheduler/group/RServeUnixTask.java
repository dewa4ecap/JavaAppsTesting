/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.group;

import com.fourelementscapital.scheduler.engines.RServeUnix;

public class RServeUnixTask extends RServeUnix {
	
	public static final String ENGINE_NAME="rscript4rserveunix";
	public static final String ENGINE_EXECUTER_UNIX_NAME="direct_script_unix";
	
	
	public RServeUnixTask(String name,String taskuid ) {
		super(name,taskuid);
	}
	 
	 
	  

		
}


