/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.engines;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fourelementscapital.db.SchedulerDB;
import com.fourelementscapital.scheduler.p2p.P2PService;

public class SchedulerExePlanLogs {

	
	
	public static final int SERVER_OK_FIXEDPEER=2000;
	public static final int SERVER_OK_REMOVED_FROM_PQUEUE=2001;
	public static final int SERVER_OK_RECEIVED_STATUS_FROM_PEER=2002;
	public static final int SERVER_OK_PEER_ACCEPTED_TASK=2003;
	
	
	
	public static final int SERVER_ERROR_EXEC_TIMEDOUT=3000;
	//3001,3002 & 3003 needs to mapped on flow chart
	public static final int SERVER_ERROR_QUEUE_KILLED_BYUSER=3001;	
	public static final int SERVER_ERROR_PEER_CRASHED_REMOVED_QUEUE=3002;	
	public static final int SERVER_ERROR_PEER_NORESP_REMOVED_QUEUE=3003;
	
	public static final int SERVER_ERROR_WHILE_ADDING_QUEUE=3004;
	public static final int SERVER_ERROR_ALARM_SENT=3005;
	//3006 needs to mapped on flow chart
	public static final int SERVER_ERROR_REMOVING_QUEUE_BASEDON_LOG=3006;
	
	public static final int SERVER_ERROR_DEPENDENCY_TIMEDOUT=3007;
	
	//3008 & 3009 needs to mapped on flow chart
	public static final int SERVER_ERROR_REMOVING_QUEUE_PEER_NO_RESPONSE=3008;
	public static final int SERVER_ERROR_REMOVE_QUEUE_NOTRUNNING_INPEER=3009;
	
	
	
	public static final int SERVER_WARNING_OVERLAPPED=4000;
	public static final int SERVER_WARNING_BOUNCEDTASK_FROMPEER=4002;
	
	
	public static final int PEER_OK_BLOOMBERG_DOWNLOAD=5000;	
	public static final int PEER_OK_UNIX_RSERVE_CAPTURE_PROCESSINFO=5001;	
	public static final int PEER_OK_RSERVE_SCRIPT_EVAL_STARTING=5002;	
	public static final int PEER_OK_RENGINE_SCRIPT_EVAL_STARTING=5003;
	public static final int PEER_OK_ADDED_PEER_QUEUE=5004;
	public static final int PEER_OK_EXECUTION_STARTING=5005;
	public static final int PEER_OK_EXECUTION_COMPLETED_WITH_NOEXCEPTION=5006;
	public static final int PEER_OK_PEER_RECEVED_TASK=5007;
	public static final int PEER_OK_RESPOND_TASKCOMPLETED_WITHSTATUS=5008;
	public static final int PEER_OK_RESPOND_RSERVE_SESSIONSTARTED=5009;
	
	
	
	
	public static final int PEER_ERROR_WHILE_RECEIVINGTASK=6000;
	public static final int PEER_ERROR_EXECUTION_FAILURE=6001;
	public static final int PEER_ERROR_EXECUTION_COMPLETED_WITH_EXCEPTION=6002;
	
	public static final int PEER_WARNING_NOROOM_TO_EXEC=7000;
	
	public static final int IGNORE_CODE=0;
	
	
	private long trigger_time;
	private int scheduler_id;
	private Logger log = LogManager.getLogger(SchedulerExePlanLogs.class.getName());
	
	public SchedulerExePlanLogs(int scheduler_id, long trigger_time){
		this.scheduler_id=scheduler_id;
		this.trigger_time=trigger_time;
	}
	
	 
	/**
	 * @deprecated
	 * @param message
	 */
	public void log(String message, int repcode)   {
		
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		try{	
			//calculates different and adds in server's time
			
			sdb.connectDB();			
			sdb.addSchedulerExeLogs(this.scheduler_id, this.trigger_time, new Date(), message,repcode, P2PService.getComputerName());

		}catch(Exception e){
			log.error("error:"+e.getMessage());
		}finally{
			try{
				sdb.closeDB();
			}catch(Exception e1){
				log.error("error:"+e1.getMessage());
			}
		}
	}
	
	public void log(String key,Map data,int repCode) {
		try{
			String mess=getMessage(key, data);
			log(mess,repCode);
		}catch(Exception e){
			//throw e;
			log.error("Error:"+e.getMessage());
		}
		//System.out.print("Message:"+mess);
	}
	
	/*
	public void log(String key,Map data,SchedulerDB sdb)  {
		try{
			String mess=getMessage(key, data);
			log(mess,sdb);
		}catch(Exception e){
			log.error("Error:"+e.getMessage());
		}
		//System.out.print("Message:"+mess);
	}
	*/
	
	/**
	 * @deprecated
	 * @param message
	 * @param sdb
	 */
	public void log(String message,SchedulerDB sdb,int repCode)   {
		 
		try{	
			//calculates different and adds in server's time		 	
			sdb.addSchedulerExeLogs(this.scheduler_id, this.trigger_time, new Date(), message,repCode, P2PService.getComputerName());
		}catch(Exception e){
			log.error("error:"+e.getMessage());
		}
	}


	private String getMessage(String key, Map data) throws Exception {
		
			ResourceBundle rb=ResourceBundle.getBundle("com.fe.scheduler.logmessages");
			String val=key;
			try{
				val=rb.getString(key);
			}catch(MissingResourceException miex){
				val=key+" !key:not_found!";
			}
			//if(data!=null && data.size()>0){
			val=replaceNewValues(val,data!=null?data:new HashMap());
			//}
			return val;
		
	}
	
	private  String replaceNewValues(final String log,
		    final Map values){
		
		    final StringBuffer sb = new StringBuffer();
		    final Pattern pattern =
		        Pattern.compile("\\[(.*?)\\]", Pattern.DOTALL);
		    final Matcher matcher = pattern.matcher(log);
		    while(matcher.find()){
		        final String key = matcher.group(1);
		        if( values.get(key)!=null){
			        final String replacement = values.get(key).toString();
			        if(replacement == null){
			        	matcher.appendReplacement(sb, "");
			        }else{
			        	matcher.appendReplacement(sb, replacement);
			        }
		        }else{
		        	matcher.appendReplacement(sb, "");
		        }
		    }
		    matcher.appendTail(sb);
		    return sb.toString();

	}
	
	  
	
	
}




