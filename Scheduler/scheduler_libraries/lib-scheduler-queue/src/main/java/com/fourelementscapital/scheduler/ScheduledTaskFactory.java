/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import com.fourelementscapital.db.SchedulerDB;
import com.fourelementscapital.scheduler.engines.ScheduledTask;
import com.fourelementscapital.scheduler.group.BBDownloadScheduledTask;
import com.fourelementscapital.scheduler.group.DirectRServeExecuteRUnix;
import com.fourelementscapital.scheduler.group.REngineScriptTask;
import com.fourelementscapital.scheduler.group.RScriptScheduledTask;
import com.fourelementscapital.scheduler.group.RServeScheduledTask;
import com.fourelementscapital.scheduler.group.RServeUnixTask;

public class ScheduledTaskFactory {

	//private static Vector<ScheduledTask> scheduledTasks=new Vector();
	//private static Vector<ScheduledTask> allTasks=new Vector();

	private static List<ScheduledTask> scheduledTasks=Collections.synchronizedList(new ArrayList());
	private static List<ScheduledTask> allTasks=Collections.synchronizedList(new ArrayList());

	
	public ScheduledTaskFactory(){
		init();
	}

	public synchronized void refreshTaskLoaded(){
		synchronized(scheduledTasks){
			if(allTasks.size()>0 || scheduledTasks.size()>0){
				allTasks.clear();
				scheduledTasks.clear();
				init();
			}
		}
	}
	
	private synchronized void init(){
		synchronized(scheduledTasks){
			if(allTasks.size()>0 || scheduledTasks.size()>0){
				
			}else{
				allTasks.clear();
				scheduledTasks.clear();
				
				Vector venabled=SchedulerEngine.getEnabledTaskTypes();
	  
				 SchedulerDB sdb=SchedulerDB.getSchedulerDB();		
				    try{
				    	sdb.connectDB();
				    	Vector allgroups=sdb.getAllGroups();
				    	for(Iterator i=allgroups.iterator();i.hasNext();){
				    		
				    		Map data=(Map)i.next();
				    		String taskuid=(String)data.get("taskuid");
				    		String name=(String)data.get("name");
				    		String enginetype=(String)data.get("enginetype");			    		
				    		ScheduledTask st=null;			    		
				    		if(enginetype.equalsIgnoreCase("rscript")){st=new RScriptScheduledTask(name,taskuid); }
				    		if(enginetype.equalsIgnoreCase("rscript4rserve")){st=new RServeScheduledTask(name,taskuid); }
				    		if(enginetype.equalsIgnoreCase("bb_download")){st=new BBDownloadScheduledTask(name,taskuid); }
				    		if(enginetype.equalsIgnoreCase(RServeUnixTask.ENGINE_NAME)){st=new RServeUnixTask(name,taskuid); }
				    		if(enginetype.equalsIgnoreCase("direct_script")){st=new REngineScriptTask(name,taskuid); }		
				    		if(enginetype.equalsIgnoreCase("direct_script_unix")){st=new DirectRServeExecuteRUnix(name,taskuid);}
				    		//if(enginetype.equalsIgnoreCase("rscript4rconsole")){st=new RConsoleScript(name,taskuid);}
				    		
				    		if(st!=null){
				    			allTasks.add(st);
				    		}
				    	}
					
				    }catch(Exception e){
				    	
				    }finally{
				    	try{
				    		sdb.closeDB();
				    	}catch(Exception e){}
				    }
	 			 
	
				
				for(Iterator<ScheduledTask> tasks=allTasks.iterator();tasks.hasNext(); ){
					ScheduledTask task=tasks.next();
					if(venabled.contains(task.getUniqueid())){
						scheduledTasks.add(task);
					}
				}
				
				
				//scheduledTasks.add(new MyTestScheduledTask());
			}
		}
	}
	
	
	public Set getTaskUids() {
		TreeSet ts=new TreeSet();
		synchronized(scheduledTasks){
			for(Iterator<ScheduledTask>  i=this.scheduledTasks.iterator();i.hasNext();){
				ScheduledTask st=i.next();
				ts.add(st.getUniqueid());
			}
		}
		return ts;
	}
	
	public List<ScheduledTask> getTasks(){
		return this.scheduledTasks;
	}

	
	public List<ScheduledTask> getAllConfiguredTasks(){
		return this.allTasks;
	}
	
	
	public ScheduledTask getTask(String uid){
		ScheduledTask rtn=null;		
		synchronized(scheduledTasks){
			for(Iterator<ScheduledTask> i=getTasks().iterator();i.hasNext();){
				ScheduledTask st=i.next();
				if(st.getUniqueid().equals(uid)){
					rtn=st ;
				}
			}
		}
		return rtn;
		
	}
	
	public ScheduledTask getTaskFromAll(String uid){
		ScheduledTask rtn=null;
		
		for(Iterator<ScheduledTask> i=this.allTasks.iterator();i.hasNext();){
			ScheduledTask st=i.next();
			if(st.getUniqueid().equals(uid)){
				rtn=st ;
			}
		}
		return rtn;
		
	}
	
}


