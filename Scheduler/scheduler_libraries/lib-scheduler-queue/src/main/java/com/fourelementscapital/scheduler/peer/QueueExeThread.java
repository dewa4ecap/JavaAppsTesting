/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.peer;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fourelementscapital.db.SchedulerDB;
import com.fourelementscapital.scheduler.engines.ScheduledTask;
import com.fourelementscapital.scheduler.engines.SchedulerExePlanLogs;
import com.fourelementscapital.scheduler.engines.StackFrame;
import com.fourelementscapital.scheduler.error.ClientError;
import com.fourelementscapital.scheduler.exception.ExceptionPeerUnknown;
import com.fourelementscapital.scheduler.exception.ExceptionWarningNoFullData;
import com.fourelementscapital.scheduler.exception.SchedulerException;
import com.fourelementscapital.scheduler.group.RScriptScheduledTask;
import com.fourelementscapital.scheduler.p2p.P2PService;

public class QueueExeThread implements Runnable {


	private Logger log = LogManager.getLogger(QueueExeThread.class.getName());
	
	private QueueAbstract qa;
	private StackFrame frame;
	private String status;
	
	public QueueExeThread(QueueAbstract qa,StackFrame frame){
		this.qa=qa;
		this.frame=frame;
	}
	
	public void run() {
		
		this.status=null;
		try{
			log.debug("run():"+this.frame.getRscript());
			if(this.frame.getRscript()!=null){
				runRScript();
			}else{
				runTask();
			}	
		}catch(Exception e){
			e.printStackTrace();
			log.error("Error:"+e.getMessage());
		}
	}
		
	
	private void runRScript() {
				
		ScheduledTask task=this.frame.getTask();
		
		try{				
	
				log.debug("just before executed task:"+task);				
				task.execute(this.frame);				
				log.debug("just after executed task");
				this.status=ScheduledTask.EXCECUTION_SUCCESS;
				this.qa.setLastExecutedTime(new Date().getTime());
				
		}catch(Exception e){
			log.error("error:::::"+e.getMessage());
			e.printStackTrace();
			//Number nid=(Number)this.frame.getData().get("id");			
			this.status=ScheduledTask.EXCECUTION_FAIL;			
			ClientError.reportError(e, null);
			
		}finally{
			if(this.frame.getCallBack()!=null){
				log.debug("sframe call back");
				try{
					this.frame.getCallBack().callBack(this.frame, this.status,null);
				}catch(Exception e){
					ClientError.reportError(e, null);
				}				
			}
			
			this.qa.finishedExec(this.frame);
			log.debug("thread finised....");
		}
		
	}
	

	 
	private void runTask() {
		
		boolean scheduled_taskmode=true;
		ScheduledTask task=this.frame.getTask();			
		 
		if(this.frame.getData().get("id")==null){
			scheduled_taskmode=false;
		}		
	 
		Date sdate=new Date();
		Number nid=(Number)this.frame.getData().get("id");

		SchedulerException se=null;
		try{	
			
			if(this.frame.isDependencyfailed()){
				
				this.status=ScheduledTask.EXCECUTION_FAIL;						
				Thread.sleep(100);
				
			}else{
			 
				log.debug("just before executing task:data:"+this.frame.getData());
				log.debug("task"+task);
				
				try{
					traceHostAndStart(sdate,this.frame.getData(),this.frame);
					
				}catch(Exception e){
					log.error("Error in updating queue log");
				}
				
				if(scheduled_taskmode){
					
					new SchedulerExePlanLogs(nid.intValue(),frame.getTrigger_time()).log("Execution starting..",SchedulerExePlanLogs.PEER_OK_EXECUTION_STARTING);
					task.execute(this.frame);					
					//new SchedulerExePlanLogs(nid.intValue(),frame.getTrigger_time()).log("Execution completed");					
				}else{					
					//new ExecuteRWindowsHighPriority("Adhoc Rscript","rscript").execute(this.frame);
					//ignored because multiple layers
					new RScriptScheduledTask("Adhoc Rscript","rscript").executeScript(this.frame);
				}			
				
				log.debug("just after executed task");
				this.status=ScheduledTask.EXCECUTION_SUCCESS;				
				this.qa.setLastExecutedTime(new Date().getTime());
			}
		}catch(ExceptionWarningNoFullData ewnd){
			this.status=ScheduledTask.EXCECUTION_WARNING;
			se=ewnd;
		}catch(SchedulerException se1){
			this.status=ScheduledTask.EXCECUTION_FAIL;			
			se=se1;
		}catch(Exception e){
			
			new SchedulerExePlanLogs(nid.intValue(),frame.getTrigger_time()).log("Execution failed: MSG:"+e.getMessage(),SchedulerExePlanLogs.PEER_ERROR_EXECUTION_FAILURE);
			this.status=ScheduledTask.EXCECUTION_FAIL;			
			se=new ExceptionPeerUnknown("Error Occured at QueueExceThread:"+e.getMessage());
			ClientError.reportError(e, null);
			
		}finally{
			
			try{			
				if(se!=null){
					if(se instanceof ExceptionWarningNoFullData){
						
					}else{
						new SchedulerExePlanLogs(nid.intValue(),frame.getTrigger_time()).log(se.getMessage()+"  Err Code:"+se.getErrorcode(),SchedulerExePlanLogs.PEER_ERROR_EXECUTION_COMPLETED_WITH_EXCEPTION);
					}
				}else{
					new SchedulerExePlanLogs(nid.intValue(),frame.getTrigger_time()).log("Execution completed, Status:"+this.status,SchedulerExePlanLogs.PEER_OK_EXECUTION_COMPLETED_WITH_NOEXCEPTION);
				}
				log.debug("finally");				
				try{
					if(scheduled_taskmode){
						this.status=(this.frame.getStatus()!=null)?this.frame.getStatus() : this.status;						
						int logid=addLog(sdate,this.frame.getData(),this.status,this.frame);						
						this.frame.setLogid(logid);
					}else{
						addScriptLog(sdate,this.frame.getData(),this.status,this.frame);
					}
				}catch(Exception e){
					//e.printStackTrace();
					throw e;
				}
			}catch(Exception e){
				ClientError.reportError(e, null);
			}finally{				
				//this.qa.finishedExec(this.frame);				
			}
			
			try{
			if(this.frame.getCallBack()!=null){
				log.debug("sframe call back");
				try{
					this.frame.getCallBack().callBack(this.frame, this.status,se);
				}catch(Exception e){
					ClientError.reportError(e, null);
				}				
			}
			}catch(Exception e){
				ClientError.reportError(e, null);
			}
			this.qa.finishedExec(this.frame);
			log.debug("thread finised....");
			
		}
		
	}
	
	
 
	
	
	private void traceHostAndStart(Date start,Map data, StackFrame sframe) throws Exception {
		
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		sdb.connectDB();		
		Number nid=(Number)data.get("id");		
		sdb.updateHostAndStarted(nid.intValue(), sframe.getTrigger_time(), start, P2PService.getComputerName());
		sdb.closeDB();
		
	}
	
	
	private  int addLog(Date start, Map data, String status,StackFrame sframe) throws Exception {
		
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		sdb.connectDB();
		//System.out.println("ScheduledTaskJob.addLog() data:"+data);
		Number nid=(Number)data.get("id");
		String timezone=(String)data.get("timezone");
		Date end=new Date();
		int id= nid.intValue();
		int logid=sdb.addSchedulerLog(id, start, end, timezone, status,null);
		
		//calculates different and adds in server's time
		if(sframe.getStarted_time()>0){
			long diff=end.getTime()-start.getTime();
			start.setTime(sframe.getStarted_time());
			long endtime=sframe.getStarted_time()+diff;
			end.setTime(endtime);
		}
		
		
		try{
	    	TreeMap record=new TreeMap();
	    	record.put("scheduler_id", nid);
	    	record.put("trigger_time", new Long(sframe.getTrigger_time()));
	    	record.put("start_time", start);
	    	record.put("end_time", end);
	    	record.put("status", status);
	    	record.put("is_triggered",new Integer(1));    	
	    	record.put("log_id",new Integer(logid));
	    	record.put("host",P2PService.getComputerName());
	    	record.put("console_message",sframe.getConsole_message());
	    	
	    	log.debug("addLog:sframe.getDbConnectionIds():"+sframe.getDbConnectionIds());
	    	
	    	//Vector connection_id=c;
	    	Map cd=null;
	    	if(sframe.getDbConnectionIds()!=null && sframe.getDbConnectionIds().size()>0) {
	    		String con_ids="";
	    		for(Iterator it=sframe.getDbConnectionIds().iterator();it.hasNext();){
	    			con_ids+=(con_ids.equals("")?"":",")+"'"+it.next()+"'";
	    		}	    		
	    		log.debug("###########con_ids::"+con_ids);
	    		
	    		cd=sdb.getDBLogSummary(con_ids);
	    		if(cd!=null && cd.size()>0){
	    			//record.put("db_connection_ids",con_ids);
	    			record.putAll(cd);
	    		}
	    	}
	    	Vector v=new Vector();
	    	v.add(record);
	    	
	    	if(sframe.getNexttrigger_time()>0){
	    		TreeMap record1=new TreeMap();
	    		record1.put("scheduler_id", nid);
	    		record1.put("trigger_time", new Long(sframe.getNexttrigger_time()));
	    		record.put("log_id",new Integer(logid));
	    		v.add(record1);
	    	}
	    	
	    	log.debug("logging job:+"+sframe.getTrigger_time()+" scheduler_Id:"+nid);
	    		    		    	
	    	//this tries 10 times with interval of 500 milliseconds;
	    	for(int i=0;i<10;i++){
				try{
					sdb.updateQueueLog(v,(cd!=null && cd.size()>0)?sframe.getDbConnectionIds():new Vector(), P2PService.getComputerName());
					i=100;
				}catch(Exception e){
					log.error("SQL Error:"+e.getMessage()+" trying "+i);
					Thread.sleep(500);
					//sdb.updateQueueNullStatus(sc_id, tri_time, getStatus());
					
				}
	    	}
	    	if(sframe.getExecuted_code()!=null){
		    	for(int i=0;i<10;i++){
					try{
						sdb.updateExecutedCodeQLog(nid.intValue(),sframe.getTrigger_time(),sframe.getExecuted_code());
						i=100;
					}catch(Exception e){
						log.error("Error:"+e.getMessage()+" trying "+i);		Thread.sleep(500);
						
					}
		    	}
		    }
    	}catch(Exception e){
    		ClientError.reportError(e, null);
    	}
		
    	
    	log.debug("before adding into db sframe.getTaskLog():"+sframe.getTasklog());
    	if(sframe.getTasklog()!=null && !sframe.getTasklog().equals("")){
			try{			
				sdb.updateSchedulerLogMsg(logid,sframe.getTasklog());
			}catch(Exception e){
				log.error("Error while updating log message of R Engine:"+e.getMessage());			
			}
		}
    	
		
		sdb.closeDB();
		return logid;
	}

	
	private void addScriptLog(Date start, Map data, String status,StackFrame sframe)  throws Exception {
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		sdb.connectDB();
		//System.out.println("ScheduledTaskJob.addLog() data:"+data);
		Number script_id=(Number)data.get("script_id");
		//String timezone=(String)data.get("timezone");
		Date end=new Date();		
		sdb.addRScriptLog(script_id.intValue(), P2PService.getComputerName(), status, start, end, sframe.getTasklog());
		
		sdb.closeDB();
		
	}
}


