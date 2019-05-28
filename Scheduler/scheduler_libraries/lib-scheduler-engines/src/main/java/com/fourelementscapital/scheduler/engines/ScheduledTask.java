/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.engines;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionException;

import com.fourelementscapital.db.SchedulerDB;
import com.fourelementscapital.db.vo.FlexiField;
import com.fourelementscapital.scheduler.exception.SchedulerException;

public abstract class ScheduledTask   {
	
	
	
	
	public static String FIELD_DEPENDENCY_IDS="dependentids";
	public static String FIELD_DEPENDENCY_CHECKTIME="dependentchecktime";
	
	public static String FIELD_DEPENDENCY_SUCCESS="onsuccess";
	public static String FIELD_DEPENDENCY_FAIL="onfail";
	public static String FIELD_DEPENDENCY_TIMEOUT="ontimeout";
	public static String FIELD_CONCURRENT_EXEC="concurrent_execution";
	
	
	
	private String name=null;
	private String uniqueid=null;
	private Vector<ScheduledTaskField> flexifields=new Vector<ScheduledTaskField>();
	
	public static String EXCECUTION_SUCCESS="success";
	public static String EXCECUTION_FAIL="fail";
	public static String DEPENDENCY_TIMEOUT="dep_timeout";
	public static String EXCECUTION_OVERLAPPED="overlapped";
	public static String EXCECUTION_WARNING="warning";
	public static String TIMOUT_WARNING="timeout";
	public static String TASK_QUEUED="queued";
	
	
	public static String TASK_EVENT_CALL_EXP_ID_VARIABLE=".caller_id";
	public static String TASK_EVENT_CALL_EXP_TRIGGERTIME_VARIABLE=".caller_trigger_time";
	public static String TASK_EVENT_CALL_EXP_ERRORMSG_VARIABLE=".caller_error_message";
	
	
	public static final int PEER_ASSOCIATION_HISTORY_ADDED=1;
	public static final int PEER_ASSOCIATION_HISTORY_REMOVED=0;
	public static final int PEER_ASSOCIATION_HISTORY_PEER_ACTIVE=1;
	public static final int PEER_ASSOCIATION_HISTORY_PEER_NOACTIVE=0;
	
	private Logger log = LogManager.getLogger(ScheduledTask.class.getName());

	public ScheduledTask(String name, String uid) {
		this.name=name;
		this.uniqueid=uid;
		addNameField();		 
	}
	
	public String getName() {
		return this.name;
	}
	public String getUniqueid() {
		return this.uniqueid;
	}
	
	public List<ScheduledTaskField> listFormFields(){
		return this.flexifields;
	}

	
	private void addNameField(){
 
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		
		try{
			sdb.connectDB();
			List<Map> folders=sdb.listOfFolders(getUniqueid());
			
			ScheduledTaskField f1=new ScheduledTaskField();
			f1.setShortname("folder_id");
			f1.setFieldlabel("Folder");		
			f1.setFieldtype(FlexiField.TYPE_DROP_DOWNLIST);
			String foption="";
			for(Map record: folders){			
				String line=record.get("id")+"~"+record.get("folder_name");
				foption+=foption.equals("")?line:"|"+line;
			
			}
			f1.setFieldoptions(foption);
			this.flexifields.add(f1);
			
		}catch(Exception e){
			log.error("error:"+e.getMessage());
		}finally{
			try{
			sdb.closeDB();
			}catch(Exception e1){
				log.error("couldn't close sdb");
			}
		}
		
		ScheduledTaskField fname=new ScheduledTaskField();
		fname.setShortname("name");
		fname.setFieldtype(FlexiField.TYPE_TEXTBOX);
		fname.setFieldlabel("Name");
		
		ScheduledTaskField depids=new ScheduledTaskField();
		depids.setShortname(FIELD_DEPENDENCY_IDS);
		depids.setFieldtype(FlexiField.TYPE_TEXTBOX);
		depids.setFieldlabel("Dependent Task IDs");
		depids.setFineprint("Runs only when those were successfully executed. Multiple IDs separated by comma. For example: 243,140,245 (Leave empty if not applicable)");
		depids.setPlacementform("dependency");
		
		ScheduledTaskField deptime=new ScheduledTaskField();
		deptime.setShortname(FIELD_DEPENDENCY_CHECKTIME);
		deptime.setFieldtype(FlexiField.TYPE_TEXTBOX);
		deptime.setFieldlabel("Dependents Recency");
		deptime.setFineprint(" Recency of all dependent task in Minutes (applicable only if Dependent task IDs entered)");
		deptime.setPlacementform("dependency");
		
		this.flexifields.add(fname);
		this.flexifields.add(depids);
		this.flexifields.add(deptime);
		
		
		ScheduledTaskField success=new ScheduledTaskField();
		success.setShortname(FIELD_DEPENDENCY_SUCCESS);
		success.setFieldtype(FlexiField.TYPE_FREETEXT);
		success.setFieldlabel("On Success");
		//success.setFineprint(" Execute tasks when the status is success");
		success.setPlacementform("events");		
		this.flexifields.add(success);
		
		ScheduledTaskField fail=new ScheduledTaskField();
		fail.setShortname(FIELD_DEPENDENCY_FAIL);
		fail.setFieldtype(FlexiField.TYPE_FREETEXT);
		fail.setFieldlabel("On Error/Fail");
		//fail.setFineprint(" Execute tasks when there is an error");
		fail.setPlacementform("events");
		this.flexifields.add(fail);
		
		ScheduledTaskField timeout=new ScheduledTaskField();
		timeout.setShortname(FIELD_DEPENDENCY_TIMEOUT);
		timeout.setFieldtype(FlexiField.TYPE_FREETEXT);
		timeout.setFieldlabel("On Timeout");
		//timeout.setFineprint(" Execute tasks when there is timeout.");
		timeout.setPlacementform("events");
		this.flexifields.add(timeout);
		

		ScheduledTaskField overlap=new ScheduledTaskField();
		overlap.setShortname(FIELD_CONCURRENT_EXEC);
		overlap.setFieldtype(FlexiField.TYPE_DROP_DOWNLIST);
		overlap.setPlacementform("concur");
		overlap.setFieldlabel("Number of Concurrent Executions");
		overlap.setFineprint(" >1 will allow task overlapping");
		overlap.setFieldoptions("2|3|4|5|6|7|8|9|10");
		overlap.setDropdowninitial("1");
		this.flexifields.add(overlap);
		
		
	}

	
	
	protected void removeAllFields(){
		this.flexifields.removeAllElements();
		addNameField();
	}

	
	
	public void addFormFields(Collection<ScheduledTaskField> fields ) throws Exception{
			for(Iterator<ScheduledTaskField> ffi=fields.iterator();ffi.hasNext();){
				ScheduledTaskField ff=ffi.next();	
				if(!flexifields.contains(ff)){
					this.flexifields.add(ff);
				}else{
					throw new Exception("Shortname is unique, Duplicate shortname are not allowed, or name shortname is already reserved.");		
				}
			}
	}
	
	
 
	public abstract  void execute(StackFrame sframe) throws JobExecutionException,SchedulerException,Exception ;
	
	/*
	
	public void setAddhocListener(String triggername,ScheduledTaskJobAdhocListener listner) {
		this.adhoclisteners.put(triggername,listner);
	}
	
	public ScheduledTaskJobAdhocListener getAddhocListener(String triggername) {
		return this.adhoclisteners.get(triggername);
	}
	public ScheduledTaskJobAdhocListener removeAddhocListener(String triggername) {
		return this.adhoclisteners.remove(triggername);
	}
   */
 
}




