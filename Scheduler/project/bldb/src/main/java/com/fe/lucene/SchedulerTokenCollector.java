/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fe.lucene;

import java.util.Map;

import com.fourelementscapital.db.SchedulerDB;

public class SchedulerTokenCollector extends SearchTokenCollector {
	
	public SchedulerTokenCollector(String tablename) {
		super(tablename);
		 
	}

	public void processTextTokens(String scheduler_id) throws Exception {
		
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		sdb.connectDB();
		
		Map data=sdb.getScheduler(Integer.parseInt(scheduler_id));
		
		
		super.categoryfields.clear();
		super.categoryfields.putAll(data);
		
		if(data.get("name")!=null){
			super.categoryfields.put("title",data.get("name"));
			super.rtn=(String)data.get("name");	
		}
		
		sdb.closeDB();
 
		
	}

}



