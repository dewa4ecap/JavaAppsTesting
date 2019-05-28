/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/


package com.fourelementscapital.scheduler.pluggin;


public class SchedulerPlugginRegister {

	
	public static PlugginInterface getPluggin(String plugginid){
		if(plugginid.equalsIgnoreCase(SchedulerBloombergPlugin.PLUGGIN_IN)){
			return  new SchedulerBloombergPlugin();
		}else{
			return null;
		}
		
	}
	
}

