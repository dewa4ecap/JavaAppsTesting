/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.balance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fourelementscapital.db.SchedulerDB;
import com.fourelementscapital.scheduler.alarm.SchedulerAlarm;
import com.fourelementscapital.scheduler.alarm.SchedulerAlarmVO;
import com.fourelementscapital.scheduler.balance.hsqldb.LoadBalancingHSQLQueue;
import com.fourelementscapital.scheduler.balance.hsqldb.LoadBalancingHSQLQueueItem;
import com.fourelementscapital.scheduler.engines.ScheduledTask;
import com.fourelementscapital.scheduler.engines.SchedulerExePlanLogs;
import com.fourelementscapital.scheduler.error.ClientError;
import com.fourelementscapital.scheduler.exception.ExceptionPeerNoResponse;
import com.fourelementscapital.scheduler.exception.ExceptionRemoveFromQ;
import com.fourelementscapital.scheduler.exception.ExceptionRemoveFromQNotInPeer;
import com.fourelementscapital.scheduler.exception.ExceptionSchedulerTeamRelated;
import com.fourelementscapital.scheduler.exception.SchedulerException;
import com.fourelementscapital.scheduler.p2p.P2PService;
import com.fourelementscapital.scheduler.p2p.listener.IncomingMessage;
import com.fourelementscapital.scheduler.p2p.peer.PeerMachine;
import com.fourelementscapital.scheduler.p2p.peer.PeerManagerHSQL;

public class ExecutingQueueCleaner extends TimerTask {

	
	private static long frequency =65; 
	private Logger log = LogManager.getLogger(ExecutingQueueCleaner.class.getName());
	
	private static TimerTask cleanerTask=null;	
	private static Timer timer=new Timer();	;	
	public static void clean(){
		
		if(cleanerTask==null){
			cleanerTask=new ExecutingQueueCleaner();
			long freq=frequency*1000;
		     //Timer timer = new Timer();		        
			ExecutingQueueCleaner.timer.scheduleAtFixedRate(cleanerTask,freq, freq);
		}
	}
	
	public static void stop(){
		 ExecutingQueueCleaner.timer.cancel();
	}
	
	public void run() {
		
		log.debug("~~~~~~~~~~~~~~~~~~~~`Cleaning thread started ");
		
		Collection<LoadBalancingQueueItem> exetasks=LoadBalancingQueue.getDefault().getExecutingTasks();
		 
		if(exetasks.size()>0){
		
			try{
				LoadBalancingQueue.getDefault().findAndUpdateOnlinePeers();
				Thread.sleep(3000); //2 seconds to get all the reply at least.
			}catch(Exception e){
				//log.error("Error:"+e.getMessage());
			}
 
		 	//the following checks database to see if any items already finished but not updated in server queue.
		 	SchedulerDB sdb1=SchedulerDB.getSchedulerDB();
			try{	
				//calculates different and adds in server's time
				sdb1.connectDB();
				ArrayList suspects=sdb1.getLast10minuteSuspectedFailure();
				for(Iterator itm=suspects.iterator();itm.hasNext();) {
					Map record=(Map)itm.next();
					Integer scd=(Integer)record.get("scheduler_id");					
					Long tr_time=(Long)record.get("trigger_time");
					//LoadBalancingQueueItem lq1=LoadBalancingQueue.getItemFromProcessingQueue(scd.intValue(), tr_time.longValue());
					//LoadBalancingQueueItem lq1=LoadBalancingQueue.getQueuedTasks()
					Collection queue=LoadBalancingQueue.getDefault().getAllTasks();
					
					boolean found=true;
					if(queue.size()>0){
						found=false;
					}
					for(Iterator<LoadBalancingQueueItem> i=queue.iterator();i.hasNext();){
						LoadBalancingQueueItem item=i.next();
						long trigg_time=0;
						if(item instanceof LoadBalancingHSQLQueueItem) {
							trigg_time= ((LoadBalancingHSQLQueueItem)item).getTrigger_time();
						}else if(item.getSf()!=null){
							trigg_time= item.getSf().getTrigger_time();
						}
						if(!found && trigg_time==tr_time.longValue() ){
							found=true;
						}
					}
					if(!found) {						
						Map log=sdb1.getQueueLog(scd.intValue(),tr_time.longValue());
						if(log.get("status")==null ||  (log.get("status")!=null && ((String)log.get("status")).equals("") )){
							
							sdb1.updateQueueNullStatus(scd.intValue(),tr_time.longValue(),"fail");
							
							String msg="Checking based on the log, the item not found in the Q,hence updating the status as failed";
							new SchedulerExePlanLogs(scd.intValue(),tr_time.longValue()).log(msg,sdb1,SchedulerExePlanLogs.SERVER_ERROR_REMOVING_QUEUE_BASEDON_LOG);
							
							ExceptionRemoveFromQ exp=new ExceptionRemoveFromQ(msg);
							sdb1.updateResponseCode(scd.intValue(),tr_time.longValue(), exp.getErrorcode());
							
		    				Map data=sdb1.getScheduler(scd.intValue());
			    			String type=(String)data.get("alert_type");
			    			if(type!=null && !type.equals("")){
			    				
			    				// send alarm : 
			    				
			    				int sc_id = scd.intValue();
			    				long tri_time = tr_time.longValue();
			    				
			    				SchedulerAlarmVO vo = new SchedulerAlarmVO();
			    				vo.setAlarmType(type);
			    				vo.setName((String)data.get("name"));
			    				vo.setSubject(SchedulerAlarm.ALARM_SUB_FAILED);
			    				vo.setMessage(msg);
			    				vo.setFrom(null);
			    				vo.setErrCode(exp.getErrorcode());
			    				vo.setExceptionSchedulerTeamRelated(exp!=null && exp instanceof ExceptionSchedulerTeamRelated);
			    				vo.setComputerName(P2PService.getComputerName());
			    				vo.setConsoleMsg(sdb1.getConsoleMsg(sc_id, tri_time));
			    				vo.setExecLogs(sdb1.getSchedulerExeLogs(sc_id, tri_time));
			    				vo.setRepCodeExist(sdb1.execLogsRepcodeExist(sc_id, tri_time, SchedulerExePlanLogs.SERVER_ERROR_ALARM_SENT));
			    				vo.setThemeTags(sdb1.getThemeTags(sc_id));
			    				vo.setOwnerTheme(sdb1.getOwnerTheme(sc_id));
			    				vo.setQueueLog(sdb1.getQueueLog(sc_id, tri_time));
			    				vo.setPeerFriendlyName(sdb1.getPeerFriendlyName(vo.getFrom()));
			    				
			    				vo.setSchedulerId(sc_id);
			    				vo.setTriggerTime(tri_time);

			    				SchedulerAlarm.sendAlarm(vo);    	
		    					new SchedulerExePlanLogs(sc_id, tri_time).log("Alarm sent",sdb1,SchedulerExePlanLogs.SERVER_ERROR_ALARM_SENT);
			    				
			    			}
						}
					}
				}
			}catch(Exception e) {
				
			}finally{
				try{sdb1.closeDB();	}catch(Exception e1){}
			}			
	
			//tasks in executing queue but peer do not run it
			//ArrayList scid_trig=new ArrayList();
			
			//for(Iterator<LoadBalancingQueueItem> itms=exetasks.iterator();itms.hasNext(); ){
	    	//	LoadBalancingQueueItem itm=itms.next();
	    	//	scid_trig.add(itm.getSchedulerid()+"_"+itm.getSf().getTrigger_time());	    		
			//}
			
			int last_milli_sec=5000; // 5minutes
			int last_3mins_ms=1000*60*3;
		 	LoadBalancingQueue lq=LoadBalancingQueue.getDefault();
		 	
		 	//the following function disabled for a while, remove the whole block of code after 6 months (disabled on 23-may-2013)
		 	if(lq instanceof LoadBalancingHSQLQueue ){
		 		LoadBalancingHSQLQueue lqhsql=(LoadBalancingHSQLQueue)lq;
		 		List<PeerMachine> onlinePeer=new PeerManagerHSQL().getOnlinePeers(last_milli_sec);
		 		List<PeerMachine> online3mins=new PeerManagerHSQL().getOnlinePeers(last_3mins_ms);
		 		List<LoadBalancingHSQLQueueItem> deaditem=lqhsql.getRunningMoreThan3Mins();
		 		
		 		for(LoadBalancingHSQLQueueItem di:deaditem){	 			
		 			
		 			String scid_trig=di.getSchedulerid()+"_"+di.getTrigger_time();
		 			PeerMachine pm=new PeerMachine(di.getMachine());
		 			boolean alert=false;
		 			String message=null;
		 			SchedulerException se=null;
		 			if(onlinePeer.indexOf(pm)>=0){
		 				//peer reports no task running then remove
			 			try{	
			 				sdb1.connectDB();
			 				if(!onlinePeer.get(onlinePeer.indexOf(pm)).getRunning().contains(scid_trig) && !sdb1.isAnyExecLogsInLast3Mins(di.getSchedulerid(),di.getTrigger_time())){
			 					message="Removing because this no longer running in peer";
			 					//lqhsql.removeItemProcessing(di,message);		 					
			 					if(!lqhsql.removeFaultyProcessingTask(di.getSchedulerid(),di.getTrigger_time())){
			 						message+=" Q Update failed";
			 					}
			 					new SchedulerExePlanLogs(di.getSchedulerid(),di.getTrigger_time()).log(message,SchedulerExePlanLogs.SERVER_ERROR_REMOVE_QUEUE_NOTRUNNING_INPEER);
			 					se=new ExceptionRemoveFromQNotInPeer(message);
			 					alert=true;
			 				}
			 			} catch (Exception e) {
							log.error("Error11A, Err: " + e.getMessage());
						}finally{
							try{sdb1.closeDB();	}catch(Exception e1){}
						}
		 			}else{
		 				
			 			try{
			 				sdb1.connectDB();
			 				if(!online3mins.contains(pm) && !sdb1.isAnyExecLogsInLast3Mins(di.getSchedulerid(),di.getTrigger_time())){
			 					message="Removed from the Q: Peer not responded last 3 or more minutes";
			 					if(!lqhsql.removeFaultyProcessingTask(di.getSchedulerid(),di.getTrigger_time())){
			 						message+=" Q Update failed";
			 					}
			 					new SchedulerExePlanLogs(di.getSchedulerid(),di.getTrigger_time()).log(message,SchedulerExePlanLogs.SERVER_ERROR_REMOVING_QUEUE_PEER_NO_RESPONSE);
			 					se=new ExceptionPeerNoResponse(message);
			 					alert=true;
			 				}
			 			} catch (Exception e) {
							log.error("Error11B, Err: " + e.getMessage());
						}finally{
							try{sdb1.closeDB();	}catch(Exception e1){}
						}
		 			}
		 			
					if (alert) {
						try {
							sdb1.connectDB();
							
							Map data = sdb1.getScheduler(di.getSchedulerid());
							String type = (String) data.get("alert_type");
							
							if (type != null && !type.equals("")) {
								Map log=sdb1.getQueueLog(di.getSchedulerid(),di.getTrigger_time());
								
								//send alert only if not success.
								if(log.get("status")==null ||  (log.get("status")!=null 
										&& !((String)log.get("status")).equals(ScheduledTask.EXCECUTION_SUCCESS) )){
									
									if(se!=null) sdb1.updateResponseCode(di.getSchedulerid(),di.getTrigger_time(), se.getErrorcode());
									
									
									// send alarm : 
									
				    				int sc_id = di.getSchedulerid();
				    				long tri_time = di.getTrigger_time();
				    				
				    				SchedulerAlarmVO vo = new SchedulerAlarmVO();
				    				vo.setAlarmType(type);
				    				vo.setName((String)data.get("name"));
				    				vo.setSubject(SchedulerAlarm.ALARM_SUB_FAILED);
				    				vo.setMessage(message);
				    				vo.setFrom(null);
				    				vo.setErrCode(se.getErrorcode());
				    				vo.setExceptionSchedulerTeamRelated(se!=null && se instanceof ExceptionSchedulerTeamRelated);
				    				vo.setComputerName(P2PService.getComputerName());
				    				vo.setConsoleMsg(sdb1.getConsoleMsg(sc_id, tri_time));
				    				vo.setExecLogs(sdb1.getSchedulerExeLogs(sc_id, tri_time));
				    				vo.setRepCodeExist(sdb1.execLogsRepcodeExist(sc_id, tri_time, SchedulerExePlanLogs.SERVER_ERROR_ALARM_SENT));
				    				vo.setThemeTags(sdb1.getThemeTags(sc_id));
				    				vo.setOwnerTheme(sdb1.getOwnerTheme(sc_id));
				    				vo.setQueueLog(sdb1.getQueueLog(sc_id, tri_time));
				    				vo.setPeerFriendlyName(sdb1.getPeerFriendlyName(vo.getFrom()));
				    				
				    				vo.setSchedulerId(sc_id);
				    				vo.setTriggerTime(tri_time);

				    				SchedulerAlarm.sendAlarm(vo);    	
			    					new SchedulerExePlanLogs(sc_id, tri_time).log("Alarm sent",sdb1,SchedulerExePlanLogs.SERVER_ERROR_ALARM_SENT);
									
								}
							}
						} catch (Exception e) {
							log.error("Error1, Err: " + e.getMessage());
						}finally{
							try{sdb1.closeDB();	}catch(Exception e1){}
						}

					}
	 				
		 		}
		 	}
	
		}
    }
	
	

		public String removeFaultyExecutingTask(int scheduler_id, long trigger_time) throws Exception {
			
			 
			try{
				
				/*
				StringTokenizer st=new StringTokenizer(id_time,"_");
				int scheduler_id=0;
				long trigger_time=0;
				if(st.countTokens()==2){
					scheduler_id=Integer.parseInt(st.nextToken());
					trigger_time=Long.parseLong(st.nextToken());
				}
				*/
				
				boolean killedstatus=false;
				if(scheduler_id>0 && trigger_time>0){					
					killedstatus=LoadBalancingQueue.getDefault().removeFaultyProcessingTask(scheduler_id,trigger_time);					
					//removes the scheduler task from peer data. 
					Map peertimes=IncomingMessage.getExecutingPeersTime();					
					//added to avoid to get currentModification error message.
					synchronized(peertimes){						
						for(Iterator i=peertimes.values().iterator();i.hasNext();){
							Map coll=(Map)i.next();
							if(coll.keySet().contains(scheduler_id)){
								coll.remove(scheduler_id);
							}				
						}
					}
				}
				if(killedstatus){
					return "Task has been removed";
				}else{
					return "Removing failed";
				}
				
			}catch(Exception e){
				ClientError.reportError(e, null);
				throw e;
			} 
			
			
		}
}


