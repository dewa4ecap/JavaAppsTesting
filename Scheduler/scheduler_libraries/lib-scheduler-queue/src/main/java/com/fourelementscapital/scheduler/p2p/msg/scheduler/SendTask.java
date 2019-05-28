/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.p2p.msg.scheduler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fourelementscapital.db.SchedulerDB;
import com.fourelementscapital.scheduler.ScheduledTaskFactory;
import com.fourelementscapital.scheduler.balance.LoadBalancingQueue;
import com.fourelementscapital.scheduler.engines.ScheduledTask;
import com.fourelementscapital.scheduler.engines.SchedulerExePlanLogs;
import com.fourelementscapital.scheduler.engines.StackFrame;
import com.fourelementscapital.scheduler.engines.StackFrameCallBack;
import com.fourelementscapital.scheduler.error.ClientError;
import com.fourelementscapital.scheduler.exception.ExceptionPeerRejected;
import com.fourelementscapital.scheduler.exception.ExceptionPeerUnknown;
import com.fourelementscapital.scheduler.exception.SchedulerException;
import com.fourelementscapital.scheduler.group.RScriptScheduledTask;
import com.fourelementscapital.scheduler.p2p.listener.OutgoingMessageCallBack;
import com.fourelementscapital.scheduler.p2p.listener.P2PPipeLog;
import com.fourelementscapital.scheduler.p2p.msg.PostCallBack;
import com.fourelementscapital.scheduler.p2p.msg.PostMessage;
import com.fourelementscapital.scheduler.peer.QueueAbstract;
import com.fourelementscapital.scheduler.peer.QueueFactory;

public class SendTask extends TaskMessage implements PostCallBack {

	private Logger log = LogManager.getLogger(SendTask.class.getName());
	
	private String started_time;
	
	private static String EXCEPTION_CLASS_NAME="exception_class";
	private static String EXCEPTION_CLASS_MESSAGE="exception_message";
	
	private static Semaphore slock=new Semaphore(1,true);
	private static final long TIMEOUT_MS=1000;
	
	public String getStarted_time() {
		return started_time;
	}

	public void setStarted_time(String started_time) {
		this.started_time = started_time;
	}

	
	private void acquireLock(){
		
		try{
			 
			SendTask.slock.tryAcquire(TIMEOUT_MS, TimeUnit.MILLISECONDS);
			 
			//LoadBalancingHSQLLayerDB.dblock.acquire();
		}catch(Exception e){
			log.error("Error:"+e.getMessage());
		}
	}
	

	private void releaseLock(){
		
		try{			
			SendTask.slock.release();
			//log.debug("....releasing lock: thread:"+Thread.currentThread().getId());
		}catch(Exception e){
			log.error("Error:"+e.getMessage());
		}
	}

	/**
	 * this method will be executed at the peer side.
	 */
	
	public synchronized Map executeAtDestination() {   
		
		HashMap rtn=new HashMap();
		
		int sc_id=0; try{sc_id=Integer.parseInt(getScheduler_id());}catch(Exception e){}
		long tri_time=0; try{tri_time=Long.parseLong(getTrigger_time());}catch(Exception e){}
		long ntri_time=0; try{ntri_time=Long.parseLong(getNext_trigger_time());}catch(Exception e){}
		long started=0;try{started=Long.parseLong(getStarted_time());}catch(Exception e){}
		if(sc_id>0 && tri_time>0){
				new SchedulerExePlanLogs(sc_id,tri_time).log("peer.sendtask.received",new HashMap(),SchedulerExePlanLogs.PEER_OK_PEER_RECEVED_TASK);
		}		
		log.debug("got task: sc_id:"+sc_id+" tri_time:"+tri_time+" uid:"+getTaskuid());
		QueueFactory qfactory=new QueueFactory();
		QueueAbstract qa=qfactory.getQueue(getTaskuid());
		SchedulerException se=null;
		//String specific_excec_log=null;
		try{
		
			acquireLock();
		    if(!qa.isRoomForThread()){                    	
		    	throw new ExceptionPeerRejected(null);
		    }else{
					if(sc_id>=0){				 
							String replyTo=getMsgCreator();
							executeTask(sc_id,tri_time,ntri_time,started,replyTo);						 
							rtn.putAll( qfactory.getExecutingIDAndSTimes());					 
					}else{
						throw new ExceptionPeerUnknown("sc_id: not found");
					}
			}
		    releaseLock();
		}catch(ExceptionPeerRejected epr){
			 se=epr;
			 rtn.put("failed", "YES");			 
			 if(sc_id>0 && tri_time>0){
					new SchedulerExePlanLogs(sc_id,tri_time).log("peer.sendtask.noroom2execute",new HashMap(),SchedulerExePlanLogs.PEER_WARNING_NOROOM_TO_EXEC);
			 }
		     
		}catch(SchedulerException se1){
			 se=se1;
			 rtn.put("failed", "YES");		
			 if(sc_id>0 && tri_time>0){
				    HashMap h=new HashMap(); h.put("error_message", se1.getMessage());
					new SchedulerExePlanLogs(sc_id,tri_time).log("peer.sendtask.unknown",h,SchedulerExePlanLogs.PEER_ERROR_WHILE_RECEIVINGTASK);
			 }
		}catch(Exception e){
			e.printStackTrace();
			//no specic error at this point			
			new SchedulerExePlanLogs(sc_id,tri_time).log("Error caught at SendTask.executeAtDestination(), Error:"+e.getMessage(),SchedulerExePlanLogs.PEER_ERROR_WHILE_RECEIVINGTASK);
		}finally{			
			if(se!=null){
				exception(se);
				setPriority(OutgoingMessageCallBack.PRIORITY_VERY_VERY_HIGH);
				rtn.put(EXCEPTION_CLASS_NAME, se.getExceptionclass());
				rtn.put(EXCEPTION_CLASS_MESSAGE, se.getMessage());
			}
		}
		return rtn;
	}
	
	
	

 
	public synchronized void callBack(Map data) {
		
		log.debug("****SendTask**** callback");
		String peer=getMsgCreator();
		if(data!=null && data.get("failed")!=null && ((String)data.get("failed")).equalsIgnoreCase("YES") ){
			int sc_id=0; try{sc_id=Integer.parseInt(getScheduler_id());}catch(Exception e){}
			long tri_time=0; try{tri_time=Long.parseLong(getTrigger_time());}catch(Exception e){}
			
			P2PPipeLog.receiveMsg("Received failed Msg:"+"Schduler ID:"+getScheduler_id()+" Trigger Time:"+getTrigger_time(),peer);
			if(sc_id>0 && tri_time>0){
				new SchedulerExePlanLogs(sc_id,tri_time).log("Sent task bounced back at the server, looking for another peer",SchedulerExePlanLogs.SERVER_WARNING_BOUNCEDTASK_FROMPEER);
			}			
			if(sc_id>0){
	  			LoadBalancingQueue.getDefault().executionFailed(sc_id,tri_time, peer);
	  			LoadBalancingQueue.getDefault().releasePeersCache4PriorityGr();
	  		}
		}else{
			
			if(data.size()>0){
				int sc_id=0; try{sc_id=Integer.parseInt(getScheduler_id());}catch(Exception e){}
				long tri_time=0; try{tri_time=Long.parseLong(getTrigger_time());}catch(Exception e){}
				
				P2PPipeLog.receiveMsg("Task Execution Started confirmation Schduler ID:"+getScheduler_id()+" Trigger Time:"+getTrigger_time(),peer);				
				log.debug("peer queue data received");
			
				PeerOnlineStatus pos=new PeerOnlineStatus();
				pos.setStatus("BUSY");
				pos.updatePeerStatus(data,peer);
				if(sc_id>0 && tri_time>0){
					try{
						LoadBalancingQueue.getDefault().peerStarted(sc_id,tri_time, peer);
						new SchedulerExePlanLogs(sc_id,tri_time).log("Peer accepted task and responded server ",SchedulerExePlanLogs.SERVER_OK_PEER_ACCEPTED_TASK);
					}catch(Exception e){
						log.error("callBack(), Error:"+e.getMessage());
					}
				}
				
			}
		}
	}
	
	
	
	/**
	 * This will be executed on peer side. 
	 * 
	 * 
	 * @param scheduler_id
	 * @param trig_time
	 * @param ntrig_time
	 * @param startedtime
	 * @param replyTo
	 * @throws Exception
	 */
	
	private synchronized  void executeTask(int scheduler_id,long trig_time,long ntrig_time,long startedtime, String replyTo) throws Exception,SchedulerException,ExceptionPeerRejected {
		
		SchedulerDB sdb=null;
		try{
			sdb=SchedulerDB.getSchedulerDB();			
			Map data=new HashMap();
			try{
				sdb.connectDB();
				data=sdb.getScheduler(scheduler_id);
			}catch(Exception e){
				throw new ExceptionPeerRejected("Error while accessing database, Error:"+e.getMessage());
			}
			String taskuid=(String)data.get("taskuid");			
			//the following block where the code injection takes place.
			if(data.get("rscript")!=null){
				String scd_trig=scheduler_id+"_"+trig_time;
				
				String code="";
				try{
					code=sdb.getInjectCode4QLog(scd_trig);
				}catch(Exception e){
					throw new ExceptionPeerRejected("Error while accessing database, Error:"+e.getMessage());
				}
				//String code=sdb.getInjectCode4QLog(scd_trig);				
				String param=(String)data.get("rscript_param");				
				String script=(String)data.get("rscript");				
				String newcode= RScriptScheduledTask.codeInjectConcatenate(param,script,code);
				data.put("rscript", newcode);
			}
			
			
			//ScheduledTask task=new ScheduledTaskFactory().getTask(taskuid);
			//added 
			ScheduledTask task=new ScheduledTaskFactory().getTaskFromAll(taskuid);
			
			StackFrame sf=new StackFrame(task,data);
			sf.setTrigger_time(trig_time);
			sf.setNexttrigger_time(ntrig_time);
			//sf.setMbean(this.mbean);
			sf.setReplyTo(replyTo);
			sf.setStarted_time(startedtime);
			if(data.get("rscript")!=null) sf.setExecuted_code((String)data.get("rscript"));
			sf.addCallBack(new StackFrameCallBack(){
				public void callBack(StackFrame sf,String status,SchedulerException se){
					 Number nid=(Number)sf.getData().get("id");			
					 new SchedulerExePlanLogs(nid.intValue(),sf.getTrigger_time()).log("Task completed, Reply server, STATUS:"+status,SchedulerExePlanLogs.PEER_OK_RESPOND_TASKCOMPLETED_WITHSTATUS);				 
					 PeerFinishedTask pft=new PeerFinishedTask();					 
					 pft.exception(se);					 
					 //added to see if it sending task for sure....
					 pft.setPriority(OutgoingMessageCallBack.PRIORITY_VERY_VERY_HIGH);
					 
					 pft.setScheduler_id(getScheduler_id());
					 pft.setTrigger_time(getTrigger_time());
					 pft.setTaskuid(getTaskuid());
					 pft.setStatus(status);
					 log.debug("reply to :"+sf.getReplyTo());
					 new PostMessage(pft,sf.getReplyTo()).send();				 
				}
			});			
			//boolean rtn=false;
			QueueAbstract qa=new QueueFactory().getQueue(taskuid);
			if(qa.isRoomForThread()){
				qa.addExThread(sf);
				//rtn=true;
			}else{
				throw new ExceptionPeerRejected(null);
			}
			
			//return rtn;
	
		}catch(ExceptionPeerRejected rjse){
			ClientError.reportError(rjse, "ExceptionPeerRejected..");
			throw rjse;
		}catch(SchedulerException se){
			ClientError.reportError(se, "SchedulerException..");
			throw se;			
		}catch(Exception e){
			ClientError.reportError(e, "Exception");
			throw e;
		}finally{			
			if(sdb!=null)sdb.closeDB();
		}
	}

	@Override
	public void onCallBackSendingFailed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSendingFailed() {
		// TODO Auto-generated method stub
		
	}

}


