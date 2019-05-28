/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.p2p.msg.scheduler;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fourelementscapital.db.SchedulerDB;
import com.fourelementscapital.scheduler.SchedulerEngine;
import com.fourelementscapital.scheduler.alarm.SchedulerAlarm;
import com.fourelementscapital.scheduler.alarm.SchedulerAlarmVO;
import com.fourelementscapital.scheduler.balance.LoadBalancingQueue;
import com.fourelementscapital.scheduler.engines.ScheduledTask;
import com.fourelementscapital.scheduler.engines.SchedulerExePlanLogs;
import com.fourelementscapital.scheduler.exception.ExceptionRServeUnixFailure;
import com.fourelementscapital.scheduler.exception.ExceptionRServeWindowsFailure;
import com.fourelementscapital.scheduler.exception.ExceptionSchedulerTeamRelated;
import com.fourelementscapital.scheduler.exception.ExceptionWarningNoFullData;
import com.fourelementscapital.scheduler.p2p.P2PService;
import com.fourelementscapital.scheduler.p2p.listener.IncomingMessage;
import com.fourelementscapital.scheduler.p2p.listener.P2PPipeLog;
import com.fourelementscapital.scheduler.p2p.msg.ExceptionMessageHandler;

public class PeerFinishedTask extends ExceptionMessageHandler {

	private String scheduler_id;
	private String trigger_time;
	private String taskuid;
	private String status;
	
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}


	private Logger log = LogManager.getLogger(PeerFinishedTask.class.getName());
 
	public String getTaskuid() {
		return taskuid;
	}


	public void setTaskuid(String taskuid) {
		this.taskuid = taskuid;
	}


	public String getScheduler_id() {
		return scheduler_id;
	}


	public void setScheduler_id(String scheduler_id) {
		this.scheduler_id = scheduler_id;
	}


	public String getTrigger_time() {
		return trigger_time;
	}


	public void setTrigger_time(String trigger_time) {
		this.trigger_time = trigger_time;
	}

	
	
    /**
     * will be executed on server side.
     */
	public Map executeAtDestination() {
		
		
		log.debug("finished:"+getScheduler_id()+" tri_time:"+getTrigger_time()+" from :"+getMsgCreator());
		log.debug("===========>PeerFinishedTask receiving message with Exception "+exception());
		if(exception()!=null){
			log.debug("   ===========>Message "+exception().getMessage());
		}
		
		P2PPipeLog.receiveMsg("Task Execution Finished:"+"Schduler ID:"+getScheduler_id()+" Trigger Time:"+getTrigger_time(),getMsgCreator()+" Status:"+getStatus());		
		
		//Debugger.addDebugMsg("Msg from peer "+getMsgCreator()+ " sc_id:"+ getScheduler_id()+" tr_time: "+getTrigger_time(),getMsgCreator()+" "+ getScheduler_id()+" "+getTrigger_time());		
		
		int sc_id=0; try{sc_id=Integer.parseInt(getScheduler_id());}catch(Exception e){}
		
		long tri_time=0; try{tri_time=Long.parseLong(getTrigger_time());}catch(Exception e){}

		PeerCacheLock.releasePeer(getMsgCreator(),getTaskuid());		
		LoadBalancingQueue.getDefault().releasePeersCache4PriorityGr(); 
	  	
		if( (getExceptionClass()!=null && getExceptionClass().equals(ExceptionRServeUnixFailure.class.getName()) ) || (getExceptionClass()!=null && getExceptionClass().equals(ExceptionRServeWindowsFailure.class.getName()) ) ){		   
			//re-scheduled...
			SchedulerDB sdb=SchedulerDB.getSchedulerDB();	
  			try{
  				sdb.connectDB();	  
  				LoadBalancingQueue.getDefault().executionFailed(sc_id,tri_time, getMsgCreator());
  	  			LoadBalancingQueue.getDefault().releasePeersCache4PriorityGr();
  				LoadBalancingQueue.getDefault().peerStarted(sc_id,tri_time,  getMsgCreator());
  				sdb.updateQueueLogStatus(sc_id,tri_time,null, P2PService.getComputerName()); //over-ridding failed long with null message;
  			}catch(Exception e) {  				
  				log.error("error while saving peerstarted Error:"+e.getMessage()+" scd_id:"+sc_id+" trig_time:"+tri_time);  				
  			}finally{
  				try {sdb.closeDB();	} catch (Exception e) {log.error("Error while closing sdb connection, error:"+e.getMessage());				}
  			}
  			
		} else{
		
		
		  	if(sc_id>=0){			
		  		
		  		new SchedulerExePlanLogs(sc_id,tri_time).log("Server rcvd completed signal, Status:"+getStatus(),SchedulerExePlanLogs.SERVER_OK_RECEIVED_STATUS_FROM_PEER);				  		
		  		LoadBalancingQueue.getDefault().executionEnded(sc_id,tri_time);			  						  		
		  		//IncomingMessage.updateFinishedPeersTime(this.mbean.getSender(),id,trg_time);
		  		IncomingMessage.updateFinishedPeersTime(getMsgCreator(),sc_id,tri_time);	  		
		  		SchedulerDB sdb=SchedulerDB.getSchedulerDB();								
		  		try{
		  			
		  			
		  			sdb.connectDB();	  
		  			
		  			try{
		  				if(exception()!=null ){
		  					sdb.updateResponseCode(sc_id, tri_time, exception().getErrorcode());	  			
		  				}
		  			}catch(Exception e){
		  				log.error("Couldn't get Scheduler Exception, Error:"+e.getMessage());
		  			}
		  			
		  			
		  			if(getStatus()!=null && !getStatus().equals("")){	  				
		  				
		  				//this tries 5 times with interval of 500 milliseconds;
		  				for(int i=0;i<5;i++){
			  				try{
			  					sdb.updateQueueNullStatus(sc_id, tri_time, getStatus());
			  					i=100;
			  				}catch(Exception e){
			  					log.error("Error:"+e.getMessage()+" trying "+i);
			  					Thread.sleep(500);
			  					//sdb.updateQueueNullStatus(sc_id, tri_time, getStatus());
			  				}
		  				}
		  			}
		  			
		  			//IElementAttributes att= cache.getDefaultElementAttributes();
		  			//att.setMaxLifeSeconds(1);
					//cache.put("just_finished_id_"+id,"yes",att);	
		  			
					String errorLog=sdb.getErrorMessageEvenNull(sc_id,tri_time);
					
					if(errorLog==null && getExceptionMessage()!=null){
						if(getExceptionClass().equalsIgnoreCase(ExceptionWarningNoFullData.class.getName())){
							
						}else{
							errorLog=getExceptionMessage();
						}
					}else if(errorLog!=null && getExceptionMessage()!=null){
						errorLog+="\n "+getExceptionMessage();
					}
					log.debug("~~~~ Server rcvd completed signal:"+errorLog+" sc_id:"+sc_id+" tri_time:"+tri_time);
					
					if(errorLog!=null){
							
						Map data=sdb.getScheduler(sc_id);					
		    			String type=(String)data.get("alert_type");
		    			String name=(String)data.get("name");
		    			if(type!=null && !type.equals("")){

		    				// send alarm : 
		    				
		    				SchedulerAlarmVO vo = new SchedulerAlarmVO();
		    				vo.setAlarmType(type);
		    				vo.setName(name);
		    				vo.setSubject(SchedulerAlarm.ALARM_SUB_FAILED);
		    				vo.setMessage(errorLog);
		    				vo.setFrom(getMsgCreator());
		    				vo.setErrCode(exception().getErrorcode());
		    				vo.setExceptionSchedulerTeamRelated(exception()!=null && exception() instanceof ExceptionSchedulerTeamRelated);
		    				vo.setComputerName(P2PService.getComputerName());
		    				vo.setConsoleMsg(sdb.getConsoleMsg(sc_id, tri_time));
		    				vo.setExecLogs(sdb.getSchedulerExeLogs(sc_id, tri_time));
		    				vo.setRepCodeExist(sdb.execLogsRepcodeExist(sc_id, tri_time, SchedulerExePlanLogs.SERVER_ERROR_ALARM_SENT));
		    				vo.setThemeTags(sdb.getThemeTags(sc_id));
		    				vo.setOwnerTheme(sdb.getOwnerTheme(sc_id));
		    				vo.setQueueLog(sdb.getQueueLog(sc_id, tri_time));
		    				vo.setPeerFriendlyName(sdb.getPeerFriendlyName(vo.getFrom()));
		    				
		    				vo.setSchedulerId(sc_id);
		    				vo.setTriggerTime(tri_time);

		    				SchedulerAlarm.sendAlarm(vo);    	
	    					new SchedulerExePlanLogs(sc_id, tri_time).log("Alarm sent",sdb,SchedulerExePlanLogs.SERVER_ERROR_ALARM_SENT);
		    				
		    			}
					}
					Map<String,String> data=sdb.getTaskEventActions(sc_id, tri_time);				
					String statuskey="status";
					if( data.containsKey(statuskey) && data.get(statuskey).equals(ScheduledTask.EXCECUTION_SUCCESS)
						&& data.containsKey(ScheduledTask.FIELD_DEPENDENCY_SUCCESS) && data.get(ScheduledTask.FIELD_DEPENDENCY_SUCCESS)!=null 
						&& !data.get(ScheduledTask.FIELD_DEPENDENCY_SUCCESS).trim().equals("")
					){
						String expression=data.get(ScheduledTask.FIELD_DEPENDENCY_SUCCESS);
						String suffi=ScheduledTask.TASK_EVENT_CALL_EXP_ID_VARIABLE+"="+sc_id+"\n";
						suffi+=ScheduledTask.TASK_EVENT_CALL_EXP_TRIGGERTIME_VARIABLE+"="+tri_time+"\n";
						new SchedulerEngine().executeScriptExpression(expression, "onSuccess of "+sc_id, suffi);	
					}
					
					if( data.containsKey(statuskey) && data.get(statuskey).equals(ScheduledTask.EXCECUTION_FAIL)
							&& data.containsKey(ScheduledTask.FIELD_DEPENDENCY_FAIL) && data.get(ScheduledTask.FIELD_DEPENDENCY_FAIL)!=null 
							&& !data.get(ScheduledTask.FIELD_DEPENDENCY_FAIL).trim().equals("")
						){
							String expression=data.get(ScheduledTask.FIELD_DEPENDENCY_FAIL);
							String suffi=ScheduledTask.TASK_EVENT_CALL_EXP_ID_VARIABLE+"="+sc_id+"\n";
							suffi+=ScheduledTask.TASK_EVENT_CALL_EXP_TRIGGERTIME_VARIABLE+"="+tri_time+"\n";
							if(errorLog!=null){
								suffi+=ScheduledTask.TASK_EVENT_CALL_EXP_ERRORMSG_VARIABLE+"=\""+tri_time+"\";\n";
							}
							new SchedulerEngine().executeScriptExpression(expression, "onFailure of "+sc_id, suffi);
					}
		  		}catch(Exception e){
		  			e.printStackTrace();
		  			//log.error("error s:"+e.getMessage());
		  			
		  		}finally{try{sdb.closeDB();}catch(Exception e1){}}
		  		
		  	}
	  	
		}
	  	return null;
		
	}

	@Override
	public void onSendingFailed() {
		// TODO Auto-generated method stub
		
	}

	
}


