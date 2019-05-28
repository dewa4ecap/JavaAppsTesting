/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.p2p.msg.scheduler;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import net.jxta.pipe.OutputPipeEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fourelementscapital.scheduler.ScheduledTaskFactory;
import com.fourelementscapital.scheduler.balance.LoadBalancingQueue;
import com.fourelementscapital.scheduler.engines.ScheduledTask;
import com.fourelementscapital.scheduler.engines.SchedulerExePlanLogs;
import com.fourelementscapital.scheduler.group.RServeUnixTask;
import com.fourelementscapital.scheduler.p2p.MessageBean;
import com.fourelementscapital.scheduler.p2p.listener.IncomingMessage;
import com.fourelementscapital.scheduler.p2p.listener.OutgoingMessageCallBack;
import com.fourelementscapital.scheduler.p2p.listener.P2PPipeLog;
import com.fourelementscapital.scheduler.p2p.msg.PostCallBack;
import com.fourelementscapital.scheduler.p2p.msg.PostMessage;
import com.fourelementscapital.scheduler.peer.QueueFactory;

public class TenderSchedulerTask extends TaskMessage implements PostCallBack {

 
	private Logger log = LogManager.getLogger(TenderSchedulerTask.class.getName());
	
		
	public synchronized Map executeAtDestination() {
		
		log.debug("task request:"+getScheduler_id()+" tri_time:"+getTrigger_time()+" task uid:"+getTaskuid()); 
		
		HashMap map=new HashMap();
		if(new QueueFactory().getQueue(getTaskuid()).isRoomForThread()){ //&& cache.get("resp2server_on_"+tuid)==null){
	  		/*
			OutgoingMessageCallBack omc=new OutgoingMessageCallBack(getTaskuid(),null){		  			
	  			public boolean validateBeforeSend(){
	  				if(getTuid()!=null){
	  					if(new QueueFactory().getQueue(getTuid()).isRoomForThread()){			  					
	  						return true;
	  					}else{
	  						return false;
	  					}
	  				}else{
	  					return true;
	  				}
	  			}
	  		};
	  		*/ 
	  		//respond(MessageBean.TYPE_REQUEST,"EXECUTETASK:"+id+":"+tri_time+":"+ntri_time+":"+tuid+":BID",this.mbean,omc);
			
	  		map.put("startTask", "YES");
  		}else{
  			map.put(IGNORE_CALLBACK, "");
  		}

		return map;
	}

	
	
	
	/**
	 * Run on server side
	 */
	public synchronized void callBack(Map retdata) {
		
		log.debug("callback:"+getScheduler_id()+" tri_time:"+getTrigger_time()+" task uid:"+getTaskuid()+" returned data:"+retdata); 
		
		//Debugger.addDebugMsg("Task accepted frm peer "+getMsgCreator()+ " sc_id:"+ getScheduler_id()+" tr_time: "+getTrigger_time(),getMsgCreator()+ " "+ getScheduler_id()+" "+getTrigger_time()
		//		);
		
		if(retdata!=null &&retdata.get("startTask")!=null && ((String)retdata.get("startTask")).equalsIgnoreCase("YES")){
			int sc_id=0; try{sc_id=Integer.parseInt(getScheduler_id());}catch(Exception e){}
			long tri_time=0; try{tri_time=Long.parseLong(getTrigger_time());}catch(Exception e){}
			long ntri_time=0; try{ntri_time=Long.parseLong(getNext_trigger_time());}catch(Exception e){}
			String peer=getMsgCreator();
			if(sc_id>=0 && peer!=null && !peer.trim().equals("")){
		  		
				
				//log.debug("LoadBalancingQueue.getDefault().isPeerBusyWithTask(peer,sc_id):"+LoadBalancingQueue.getDefault().isPeerBusyWithTask(peer,sc_id)+" PeerCacheLock.lockPeerIfFree(peer,getTaskuid()):"+PeerCacheLock.lockPeerIfFree(peer,getTaskuid()));
	  			//try{
				//}catch(Exception e){}
				
				ScheduledTask task=new ScheduledTaskFactory().getTask(getTaskuid());
				//RServeUnixTask can handle more than script in a time
				
				LoadBalancingQueue lb=LoadBalancingQueue.getDefault();
				boolean isPeerBusy=lb.isPeerBusyWithTask(peer,sc_id);
				log.debug("isPeerBusy:"+isPeerBusy);

				if(	task instanceof RServeUnixTask || (!isPeerBusy && PeerCacheLock.lockPeerIfFree(peer,getTaskuid())) ){ //&& !isPeerCommCached(this.mbean.getSender(),tuid) ){
		  			//noTender4Seconds(this.mbean.getSender(),tuid);
		  			//lockPeer(this.mbean.getSender());
					
		  			int status=LoadBalancingQueue.getDefault().startedIfNotStarted(sc_id,tri_time,peer);
		  			
		  			log.debug("status:"+status+" scd_id:"+sc_id+" tri_time:"+tri_time);		  			
		  				  			
		  			if(status==1){
		  				
		  				HashMap data=new HashMap();
			  			try{
			  				data.put("scheduler_id", sc_id);
			  				data.put("trigger_time", tri_time);
			  			}catch(Exception e){
			  				
			  			}
			  			
			  			OutgoingMessageCallBack omc=new OutgoingMessageCallBack(getTaskuid(),data){
			  				public void onFail(OutputPipeEvent pipe, MessageBean mbean, String destination){
			  					
			  					StringTokenizer st=new StringTokenizer(mbean.getCommand(),":");
			  					int id=0;
			  				  	if(st.countTokens()>=5){
			  				  			st.nextToken();
			  				  		    String sid=st.nextToken();
			  				  		    try{id=Integer.parseInt(sid);}catch(Exception e){}
			  				  	}
			  				  	if(id>0){
			  				  		//System.out.println("IncomingMessageParser.parse();, execution failed, added back to queue again");
			  				  		Long trigger_time=(Long)getData().get("trigger_time");			  				  	
			  				  		LoadBalancingQueue.getDefault().executionFailed(id,trigger_time.longValue(),destination);
			  				  	}
			  				  	try{
			  				  		PeerCacheLock.releasePeer(getDestination(),tuid);
			  				  	}catch(Exception e){}
			  				  LoadBalancingQueue.getDefault().releasePeersCache4PriorityGr();
			  				    
			  				}
			  				
			  				public void after(OutputPipeEvent event){
			  					//releasePeer(getDestination());
			  					LoadBalancingQueue.getDefault().releasePeersCache4PriorityGr();
			  					Map data=getData();
			  					if(data!=null && data.get("scheduler_id")!=null && data.get("trigger_time")!=null){
			  						int scheduler_id=(Integer)data.get("scheduler_id");
			  						long tigger_time=(Long)data.get("trigger_time");
			  						IncomingMessage.updateExecutingPeersTime(getDestination(),"BUSY",scheduler_id,tigger_time);			  						
			  						new SchedulerExePlanLogs(scheduler_id,tigger_time).log("Server fixes peer "+getDestination()+" for execution",SchedulerExePlanLogs.SERVER_OK_FIXEDPEER);
			  						P2PPipeLog.sendMsg("Fixing peer to run scheduler_id:"+scheduler_id+" trigger_time:"+tigger_time, getDestination());
			  					}
			  				}			  				 
			  			};
			  			
			  			omc.setPriority(OutgoingMessageCallBack.PRIORITY_VERY_HIGH);
		  				//respond(MessageBean.TYPE_RESPONSE,"EXECUTETASK:"+id+":"+tri_time+":"+ntri_time+":"+new Date().getTime()+":"+tuid+":CONFIRM",this.mbean,omc);
		  				SendTask st=new SendTask();
		  				st.setScheduler_id(getScheduler_id());
		  				st.setTrigger_time(getTrigger_time());
		  				st.setNext_trigger_time(getNext_trigger_time());
		  				st.setTaskuid(getTaskuid());
		  				st.setStarted_time(new Date().getTime()+"");
		  				new PostMessage(st,peer,omc).send();
		  				
		  			}
		  		} //task instanceof RServeUnixTask ||...
		  	} //if(sc_id>=0 && peer!=null && !peer.trim().equals(""))
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


