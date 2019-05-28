/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fourelementscapital.db.SchedulerDB;
import com.fourelementscapital.scheduler.engines.ScheduledTask;
import com.fourelementscapital.scheduler.engines.SchedulerExePlanLogs;
import com.fourelementscapital.scheduler.engines.StackFrame;
import com.fourelementscapital.scheduler.error.ClientError;
import com.fourelementscapital.scheduler.group.RScriptScheduledTask;
import com.fourelementscapital.scheduler.p2p.P2PService;

/**
 *  
 * @author Rams Kannan
 *
 */
public class ScheduledTaskQueue implements Runnable {
	private static ConcurrentLinkedQueue<StackFrame>	queue= new ConcurrentLinkedQueue<StackFrame>();
	//private static boolean threadRunning=false;
	private Logger log = LogManager.getLogger(ScheduledTaskQueue.class.getName());
	
	private static StackFrame curExecFrame=null;
	
	
	/*
	public static synchronized void add(ScheduledTask task, Map data){		
		queue.add(new StackFrame(task,data));
		if(!threadRunning){			
			threadRunning=true;
			new Thread(new ScheduledTaskQueue()).start();
		}
	}
	*/
	
	private static long lastExcecutedTime=0;
	
	private static Thread thread=null;
	/**
	 *@deprecated
	 */
	public static synchronized void add(StackFrame frame){		

		Number nid=(Number)frame.getData().get("id");
		if(!queue.contains(frame)){
			queue.add(frame);
			
			new SchedulerExePlanLogs(nid.intValue(),frame.getTrigger_time()).log("Task added to local queue ",SchedulerExePlanLogs.IGNORE_CODE);
		}else if(queue.contains(frame) && frame.isDependencyfailed()){
			new SchedulerExePlanLogs(nid.intValue(),frame.getTrigger_time()).log("Task dependency failed ",SchedulerExePlanLogs.IGNORE_CODE);
			queue.add(frame);
		}
		
		if(thread==null || (thread!=null && !thread.isAlive())){
			thread=new Thread(new ScheduledTaskQueue());
			thread.start();
		}
		/*
		if(!threadRunning){			
			threadRunning=true;
			new Thread(new ScheduledTaskQueue()).start();
		}
		*/	
	}
	
	public static long lastExcecutedTime(){
		return lastExcecutedTime;
	}
	
	public synchronized static boolean isExecutingOrQueued(){
		
		boolean rtn=false;
		if(queue.size()>0){
			if(thread==null || (thread!=null && !thread.isAlive())){
					thread=new Thread(new ScheduledTaskQueue());
					thread.start();					
			}
		}
		if((thread!=null && thread.isAlive()) ||  queue.size()>0 || curExecFrame!=null){
			rtn=true;
		}
		
		return rtn;
		
	 
	}
	
	
	/**
	 * @deprecated
	 * @return
	 */
	public static synchronized Number getExecutingTaskId(){
		if(isExecutingOrQueued()){
		//if(addOrCheckQueue(null)>0){
			return (Number)curExecFrame.getData().get("id");
		}else{
			return null;
		}
	}
	

	public static synchronized StackFrame getExecutingStackFrame(){
		if(isExecutingOrQueued()){
		//if(addOrCheckQueue(null)>0){
			return curExecFrame;
		}else{
			return null;
		}
	}

	
	
	public static void killQueueThread() throws Exception {
		try{
			ScheduledTaskQueue.thread.interrupt();	
		}catch(Exception e){
			throw e;
		}
	}
	
	
	
	public void run()  {
		 
		log.debug("thread started");
		//threadRunning=true;
		try{
			while(!queue.isEmpty()){			
				//StackFrame sframe=queue.poll();
				
				StackFrame sframe=queue.peek();		
				curExecFrame=sframe;			
				
				boolean scheduled_taskmode=true;
				ScheduledTask task=sframe.getTask();			
				log.debug("Executing task1:"+sframe.getData().get("name"));
				if(curExecFrame.getData().get("id")==null){
					scheduled_taskmode=false;
				}
				
				String status=null;
				Date sdate=new Date();
				Number nid=(Number)sframe.getData().get("id");
				try{					
					if(sframe.isDependencyfailed()){
						
						//commented because warning symbol will let the user to see the error message
						//execution overlap also added, so the below line commented to let the user know it is overlapped.						
						status=ScheduledTask.DEPENDENCY_TIMEOUT;							
						Thread.sleep(100);
					}else{
						//task.execute(sframe.getData(), null);
						log.debug("just before executing task:sframe:"+sframe);
						log.debug("just before executing task:data:"+sframe.getData());
						log.debug("task"+task);
						
						try{
							traceHostAndStart(sdate,sframe.getData(),sframe);
							//System.out.println("ScheduledTaskQueue.run() host update");
						}catch(Exception e){
							log.error("Error in updating queue log");
						}
						
						if(scheduled_taskmode){
							new SchedulerExePlanLogs(nid.intValue(),sframe.getTrigger_time()).log("Execution started ",SchedulerExePlanLogs.IGNORE_CODE);
							task.execute(sframe);	
						}else{
							//task.execute(sframe);
							new RScriptScheduledTask("Adhoc Rscript","rscript").executeScript(sframe);
						}
						
						
						log.debug("just after executed task");
						status=ScheduledTask.EXCECUTION_SUCCESS;
					
						lastExcecutedTime=new Date().getTime();
					}
				}catch(Exception e){
					log.error("error:::::"+e.getMessage());
					e.printStackTrace();
					status=ScheduledTask.EXCECUTION_FAIL;			
					ClientError.reportError(e, null);
				}finally{
						
					if(sframe.getCallBack()!=null){
						log.debug("sframe call back");
						try{
							sframe.getCallBack().callBack(sframe, status,null);
						}catch(Exception e){
							ClientError.reportError(e, null);
						}
					}
					
					try{
						log.debug("finally");
						new SchedulerExePlanLogs(nid.intValue(),sframe.getTrigger_time()).log("Execution completed, Status:"+status,SchedulerExePlanLogs.IGNORE_CODE);
					
						if(scheduled_taskmode){
							int logid=addLog(sdate,sframe.getData(),status,sframe);						
							sframe.setLogid(logid);
						}else{
							addScriptLog(sdate,sframe.getData(),status,sframe);
						}
					}catch(Exception e){
						e.printStackTrace();
						ClientError.reportError(e, null);
					}
					queue.remove(sframe);					
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			log.error("Errror while running this thread, Error:"+e.getMessage());
		}finally{
			//threadRunning=false;			
			log.debug("exiting thread:");
			curExecFrame=null;
			
		}
	}

	
 
	
	public synchronized static Map getQueuedIds(){
		Vector rtn=new Vector();
		Logger log = LogManager.getLogger(ScheduledTaskQueue.class.getName());
		//currently executing task
		TreeMap t=new TreeMap();
		if(curExecFrame!=null){
			//String uid=curExecFrame.getData().get("id")+"_"+curExecFrame.getTask().getTrigger_time();
			String uid=curExecFrame.getData().get("id")+"_"+curExecFrame.getTrigger_time();
			rtn.add(uid);
			t.put("executing", uid);
		}
		for(Iterator<StackFrame> i=queue.iterator();i.hasNext();){
			StackFrame stack=i.next();
			String uid=stack.getData().get("id")+"_"+stack.getTrigger_time();
			rtn.add(uid);
			//log.debug("uid");
		}
		t.put("alltasks", rtn);
		
		return t;
	}
	
	
	public synchronized static Vector<Number> getQueuedTaskIds(){
		Vector rtn=new Vector();
		Logger log = LogManager.getLogger(ScheduledTaskQueue.class.getName());		
		//currently executing task
		if(curExecFrame!=null && curExecFrame.getData().get("id")!=null){
			//String uid=curExecFrame.getData().get("id")+"_"+curExecFrame.getTask().getTrigger_time();
			Number uid=(Number)curExecFrame.getData().get("id");
			rtn.add(uid);
		}
		for(Iterator<StackFrame> i=queue.iterator();i.hasNext();){
			StackFrame stack=i.next();
			Number uid=(Number)stack.getData().get("id");
			rtn.add(uid);
			//log.debug("uid");
		}
		
		return rtn;
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
	    	
	    	sdb.updateQueueLog(v,new Vector(), P2PService.getComputerName());
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

	


	
}





