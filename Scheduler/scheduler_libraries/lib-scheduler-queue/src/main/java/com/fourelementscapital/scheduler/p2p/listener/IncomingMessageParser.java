/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.p2p.listener;

import java.io.File;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Vector;

import net.jxta.endpoint.MessageElement;
import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.OutputPipeEvent;
import net.jxta.pipe.PipeService;
import net.jxta.protocol.PipeAdvertisement;

import org.apache.jcs.JCS;
import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fourelementscapital.db.SchedulerDB;
import com.fourelementscapital.scheduler.ScheduledTaskFactory;
import com.fourelementscapital.scheduler.ScheduledTaskQueue;
import com.fourelementscapital.scheduler.balance.LoadBalancingQueue;
import com.fourelementscapital.scheduler.balance.LoadBalancingQueueItem;
import com.fourelementscapital.scheduler.engines.ScheduledTask;
import com.fourelementscapital.scheduler.engines.SchedulerExePlanLogs;
import com.fourelementscapital.scheduler.engines.StackFrame;
import com.fourelementscapital.scheduler.engines.StackFrameCallBack;
import com.fourelementscapital.scheduler.error.ClientError;
import com.fourelementscapital.scheduler.exception.SchedulerException;
import com.fourelementscapital.scheduler.group.RScriptScheduledTask;
import com.fourelementscapital.scheduler.p2p.MessageBean;
import com.fourelementscapital.scheduler.p2p.P2PAdvertisement;
import com.fourelementscapital.scheduler.p2p.P2PService;
import com.fourelementscapital.scheduler.peer.QueueAbstract;
import com.fourelementscapital.scheduler.peer.QueueFactory;
//import com.fe.p2p.PeerNotificationTray;

public class IncomingMessageParser {
	
	private MessageBean mbean=null;
	private MessageElement messageEle=null;
	
	private Map<String,String> attachments=new HashMap();
	 
	
	private Logger log = LogManager.getLogger(IncomingMessageParser.class.getName());
	
	public IncomingMessageParser(MessageBean mb, MessageElement messageEle){
		this.mbean=mb;
		this.messageEle=messageEle;
	}
	
	private static JCS cache=null;
	
	public void setAttachement(Map<String,String> att){
		this.attachments=att;
		 
	}
	
	/**
	 * @deprecated
	 */
	public void parse(){
		//protected synchronized void parse(){
			QueueFactory qfactory=new QueueFactory();
			boolean processed=false;
			
			/*
			 * this block is deprecated
			 */
			if(this.mbean.getReply().equalsIgnoreCase(MessageBean.REPLYBACK) && this.mbean.getType().equals(MessageBean.TYPE_REQUEST) && this.mbean.getCommand().equalsIgnoreCase(P2PTransportMessage.COMMAND_STATUS) ){
				
				//the following lines ignored, because peer responds not only task id, also triggered time. 
				//if(ScheduledTaskQueue.isExecutingOrQueued()){				  
				//respond(MessageBean.TYPE_RESPONSE,"ONLINE:BUSY:"+ScheduledTaskQueue.getExecutingTaskId(),this.mbean,null);				 	
				//System.out.println("IncomingMessageParser.parse()::: Total Processes Size :"+qfactory.countExcTasksInPeer());
				if(qfactory.countExcTasksInPeer()>0){	
					 //Number scheduler_id=(Number)ScheduledTaskQueue.getExecutingStackFrame().getData().get("id");
					 String ids="";
					 String times="";
					 Map idstime=qfactory.getExecutingIDAndSTimes();
					 for(Iterator it=idstime.keySet().iterator();it.hasNext();){
						 Object id=it.next();
						 Object time=idstime.get(id);
						 ids+=(ids.equals(""))?id:","+id;						 
						 times+=(times.equals(""))?time:","+time;
					 }
					 respond(MessageBean.TYPE_RESPONSE,"ONLINE:BUSY:"+ids+":"+times,this.mbean,null); 
				}else{
					 respond(MessageBean.TYPE_RESPONSE,"ONLINE:NOBUSY",this.mbean,null);
				}
				processed=true;
	       	}
			
			
			
			/*
			 * this block is deprecated
			 */
			if(this.mbean.getType().equals(MessageBean.TYPE_RESPONSE) && this.mbean.getCommand().startsWith("ONLINE") ){
				   //if(this.mbean.getSender())
				   //IncomingMessage.cachedPeers.add(this.mbean.getSender());
				   //processed=true;
				StringTokenizer st=new StringTokenizer(this.mbean.getCommand(),":");
		  		String status="NOBUSY";
             //int scheduler_id=0;
		  		Vector scheduler_ids=new Vector();
             //long trigger_time=0;
		  		Vector triggertime=new Vector();
		  		if(st.countTokens()==2){
		  			st.nextToken();
		  			status=st.nextToken();
		  		}
		  		if(st.countTokens()>=3){
			  			st.nextToken();
			  			status=st.nextToken();
			  			String sc_id=st.nextToken();

			  			//if still have more token
			  			if(st.hasMoreTokens()){
			  				String ti_time=st.nextToken();
			  				if(ti_time!=null){
			  					try{
			  						//trigger_time=Long.parseLong(ti_time);
			  						StringTokenizer tm1=new StringTokenizer(ti_time,",");
					  				while(tm1.hasMoreTokens()){
					  					triggertime.add(tm1.nextToken());
					  				}
			  					}catch(Exception e){}
			  				}
			  			}
			  			try{
			  				//scheduler_id=Integer.parseInt(sc_id);
			  				StringTokenizer strt=new StringTokenizer(sc_id,",");
			  				while(strt.hasMoreTokens()){			  					
			  					scheduler_ids.add(strt.nextToken());
			  				}
			  				
			  			}catch(Exception e){log.error("Error in parsing number:"+e.getMessage());}
			  	}
		  				//System.out.println("IncomingMessageParser.parse() command:"+this.mbean.getCommand()+" trigger_time:"+trigger_time+" st.countTokens()"+st.countTokens());
		  		if(status.equals("BUSY") && scheduler_ids.size()>0){
		  			 //LoadBalancingQueue.updateNoBusyPeer(this.mbean.getSender(),scheduler_id);
		  			try{
		  				for(Iterator i=scheduler_ids.iterator();i.hasNext();){
		  					int scheduler_id=Integer.parseInt((String)i.next());
		  					//LoadBalancingQueue.removeNonRespondingFromQ(this.mbean.getSender(),scheduler_id);
		  				}
		  			}catch(Exception e){
		  				log.error("error:"+e.getMessage());
		  			}
		  		}else{
		  			//remove finished ones from time, if no busy then removes from the timing collection.
		  			IncomingMessage.updateExecutingPeersTime(this.mbean.getSender(),status,0,0);
		  			boolean taskexecuting=false;
		  			//boolean exitloop=false;
		  			 
		  			//for(Iterator i=LoadBalancingQueue.getExecutingTasks().iterator();i.hasNext()|| exitloop;){
		  			for(Iterator i=LoadBalancingQueue.getDefault().getExecutingTasks().iterator();i.hasNext();){
		  				LoadBalancingQueueItem li=(LoadBalancingQueueItem)i.next();
		  				if(li.getMachine()!=null && li.getMachine().equalsIgnoreCase(this.mbean.getSender())){
		  					taskexecuting=true; //exitloop=true;
		  				}
		  			}
		  			if(!taskexecuting){
		  				removePeerThreadStatus(this.mbean.getSender());
		  			}
		  			//sdb.removeAllPeerThreadStatus(P2PService.getComputerName());
		  		}
		  		
		  		//IncomingMessage.peersUpdate(IncomingMessage.ACTION_PEER_ADD,this.mbean.getSender(),status);
		  		IncomingMessage.peersUpdate(this.mbean.getSender(),status);
		  		
		  		for(int i=0;i<scheduler_ids.size();i++){
		  			
		  			int scheduler_id=Integer.parseInt((String)scheduler_ids.get(i));		  			
					IncomingMessage.updateExecutingPeers(this.mbean.getSender(),status,scheduler_id);
					if(scheduler_ids.size()==triggertime.size()){
						long trigger_time=Long.parseLong((String)triggertime.get(i));
						
						if(status.equals("BUSY")){
							IncomingMessage.peerRespRecencyOnTask(scheduler_id,trigger_time);
						}
						if(cache!=null && cache.get("just_finished_id_"+scheduler_id)!=null){}else{
							//do not update just finished task, as communication overlaps.
							IncomingMessage.updateExecutingPeersTime(this.mbean.getSender(),status,scheduler_id,trigger_time);
						}
			  		}	
					//tobe checked later as the time doesn't corresponds to scheduler_ids anymore after made to new peer queue (QueueAbstract)					
		  		}
				processed=true;
	       	}
			
			//if(this.mbean.getType().equals(MessageBean.TYPE_REQUEST) && this.mbean.getCommand().equalsIgnoreCase("RESTART_PEER") ){
			//	RestartTomcat.restartPeerNow();
	       	//}
			
			if(this.mbean.getType().equals(MessageBean.TYPE_REQUEST) && this.mbean.getCommand().startsWith("RESTART_PEER_LATER") ){
				StringTokenizer st=new StringTokenizer(this.mbean.getCommand(),":");
	  			int id=0;
		  		if(st.countTokens()==2){
		  			st.nextToken();
		  		    String sid=st.nextToken();
		  		    try{
		  		    	id=Integer.parseInt(sid);
		  		    }catch(Exception e){}
					
		  		}
		  		
				//RestartTomcat.restartPeerLater(id);		  			
		  		
				

				
	       	}

			
			
			if(this.mbean.getReply().equalsIgnoreCase(MessageBean.REPLYBACK) && this.mbean.getType().equals(MessageBean.TYPE_REQUEST) && this.mbean.getCommand().equalsIgnoreCase(P2PTransportMessage.COMMAND_STATISTICS) ){

				String stat=null;
				try{
					stat=getStatistics();
				}catch(Exception e){
					log.error("Error while processing statistics");
				}
				if(stat!=null){
					OutgoingMessageCallBack omc=new OutgoingMessageCallBack(){};
					omc.setPriority(OutgoingMessageCallBack.PRIORITY_HIGH);					 
					respond(MessageBean.TYPE_RESPONSE,"STATISTICS_DATA:"+stat,this.mbean,omc);					
				}
			}
			
			
			if(this.mbean.getType().equals(MessageBean.TYPE_RESPONSE) && this.mbean.getCommand().startsWith("STATISTICS_DATA") ){				 
				StringTokenizer st=new StringTokenizer(this.mbean.getCommand(),":");		  		 
				String stat=null;
				if(st.countTokens()>=2){
		  			st.nextToken();
		  			stat=st.nextToken();
		  		}
				IncomingMessage.updatePeerStatistics(this.mbean.getSender(),stat);
				processed=true;
	       	 }
			
			//if(this.mbean.getReply().equalsIgnoreCase(MessageBean.REPLYBACK) && this.mbean.getType().equals(MessageBean.TYPE_REQUEST) && this.mbean.getCommand().equalsIgnoreCase(LoadBalancingQueue.PEER_QUEUE) ){
			if(this.mbean.getReply().equalsIgnoreCase(MessageBean.REPLYBACK) && this.mbean.getType().equals(MessageBean.TYPE_REQUEST) && this.mbean.getCommand().equalsIgnoreCase(P2PTransportMessage.COMMAND_PEER_QUEUE) ){
				String stat=null;
				try{
					stat=getPeerQueueStat();
				}catch(Exception e){
					log.error("Error while processing statistics");
				}
				if(stat!=null){
					OutgoingMessageCallBack omc=new OutgoingMessageCallBack(){};
					omc.setPriority(OutgoingMessageCallBack.PRIORITY_HIGH);					 
					respond(MessageBean.TYPE_RESPONSE,LoadBalancingQueue.getDefault().PEER_QUEUE_RESP+":"+stat,this.mbean,omc);					
				}
			}
			
			if(this.mbean.getType().equals(MessageBean.TYPE_RESPONSE) && this.mbean.getCommand().startsWith(LoadBalancingQueue.getDefault().PEER_QUEUE_RESP) ){				 
				StringTokenizer st=new StringTokenizer(this.mbean.getCommand(),":");		  		 
				String stat=null;
				if(st.countTokens()>=2){
		  			st.nextToken();
		  			stat=st.nextToken();
		  		}
				IncomingMessage.updatePeerQueueStat(this.mbean.getSender(),stat);
				processed=true;
	       	 }
			
			if(this.mbean.getReply().equalsIgnoreCase(MessageBean.REPLYBACK) && this.mbean.getType().equals(MessageBean.TYPE_REQUEST) && this.mbean.getCommand().equalsIgnoreCase(P2PTransportMessage.COMMAND_R_PACKAGES) ){

				String packg=null;
				try{
					packg=RScriptScheduledTask.getRPackageVersion();
				}catch(Exception e){
					log.error("Error while processing statistics");
				}
				if(packg!=null){
					 OutgoingMessageCallBack omc=new OutgoingMessageCallBack(){};
					 omc.setPriority(OutgoingMessageCallBack.PRIORITY_HIGH);					 
					 //respond(MessageBean.TYPE_RESPONSE,"R_PACKAGES_DATA:"+packg,this.mbean,omc);					
					 respond(MessageBean.TYPE_RESPONSE,P2PTransportMessage.COMMAND_R_PACKAGES_DATA+":"+packg,this.mbean,omc);
				}else{
					log.debug("packg: returned null");
				}
			}
			
			
			//if(this.mbean.getType().equals(MessageBean.TYPE_RESPONSE) && this.mbean.getCommand().startsWith("R_PACKAGES_DATA") ){
			if(this.mbean.getType().equals(MessageBean.TYPE_RESPONSE) && this.mbean.getCommand().startsWith(P2PTransportMessage.COMMAND_R_PACKAGES_DATA) ){				
				StringTokenizer st=new StringTokenizer(this.mbean.getCommand(),":");
				String stat=null;
				if(st.countTokens()>=2){
		  			st.nextToken();
		  			stat=st.nextToken();
		  		}
				IncomingMessage.updatePeerRPackages(this.mbean.getSender(),stat);
				processed=true;
	       	}

		
			
		
			
			//client side
		  	if(this.mbean.getReply().equalsIgnoreCase(MessageBean.REPLYBACK) && 
		  	   this.mbean.getType().equals(MessageBean.TYPE_REQUEST) && 
		  	   this.mbean.getCommand().startsWith("EXECUTETASK:") &&
		  	   this.mbean.getCommand().endsWith("TENDER")
		  	){
		  		
		  		respond2Server();
		  		processed=true;
		  	}
		  	
		  	
			//client side
		  	if(this.mbean.getReply().equalsIgnoreCase(MessageBean.REPLYBACK) && 
		  	   this.mbean.getType().equals(MessageBean.TYPE_REQUEST) && 
		  	   this.mbean.getCommand().startsWith("ISEXECUTING:")  
		  	){
	  			StringTokenizer st=new StringTokenizer(this.mbean.getCommand(),":");
	  			Number id=0;
		  		if(st.countTokens()==2){
		  			st.nextToken();
		  		    String sid=st.nextToken();
		  		    try{
		  		    	id=Integer.parseInt(sid);
		  		    }catch(Exception e){}
		  		}
		  		
		  		boolean executing=false;
		  		
		  		Vector<Number> ids=qfactory.getExecutingIDs(); //ScheduledTaskQueue.getQueuedTaskIds();		  		
		  		if(ids!=null && ids.size()>0){
		  			executing=ids.contains(id);
		  		}
		  		
		  		OutgoingMessageCallBack omc=new OutgoingMessageCallBack(){};
				omc.setPriority(OutgoingMessageCallBack.PRIORITY_VERY_HIGH);
		  		respond(MessageBean.TYPE_RESPONSE,"ISEXECUTING:"+id+":"+(executing+"").toUpperCase(),this.mbean,omc);
		  		processed=true;		  		
		  	}
		  	
			//sever side
		  	if(this.mbean.getType().equals(MessageBean.TYPE_RESPONSE) && 
		  	   this.mbean.getCommand().startsWith("ISEXECUTING:") && 
		  	   this.mbean.getCommand().endsWith("FALSE")		  	    
		  	){
	  			StringTokenizer st=new StringTokenizer(this.mbean.getCommand(),":");
	  			Integer id=0;
	  			
		  		if(st.countTokens()==3){
		  			st.nextToken();
		  		    String sid=st.nextToken();
		  		    try{
		  		    	id=Integer.parseInt(sid);
		  		    }catch(Exception e){}
		  		}
		  		LoadBalancingQueue.getDefault().cleanupProccesingQueue(id, this.mbean.getSender());
		  	}
		  	
		    //server side  //sending confirmation message to peer
		  	if(this.mbean.getReply().equalsIgnoreCase(MessageBean.REPLYBACK) && 
		  	   this.mbean.getType().equals(MessageBean.TYPE_REQUEST) && 
		  	   this.mbean.getCommand().startsWith("EXECUTETASK:") &&
		  	   this.mbean.getCommand().endsWith("BID")
		  	){
		  		
			  	int id=-1;
			  	long tri_time=0;
				long ntri_time=0;
			  	StringTokenizer st=new StringTokenizer(this.mbean.getCommand(),":");
			  	String tuid="";
			  	if(st.countTokens()>=5){
			  			st.nextToken();
			  		    String sid=st.nextToken();
			  		    String tid=st.nextToken();
			  		    String ntid=st.nextToken();;
			  		    try{
			  		    	tri_time=Long.parseLong(tid);
			  		    	id=Integer.parseInt(sid);
			  		    	ntri_time=Long.parseLong(ntid);
			  		    }catch(Exception e){}
			  		    if(st.hasMoreTokens()){
			  		    	tuid=st.nextToken();
			  		    }
			  	}
			  	
			  	if(id>=0){
			  		
		  			//try{
					//}catch(Exception e){}
			  		if(!LoadBalancingQueue.getDefault().isPeerBusyWithTask(this.mbean.getSender(),id) && lockPeerIfFree(this.mbean.getSender()) ) { //&& !isPeerCommCached(this.mbean.getSender(),tuid) ){
			  			//noTender4Seconds(this.mbean.getSender(),tuid);
			  			//lockPeer(this.mbean.getSender());
			  			int status=LoadBalancingQueue.getDefault().startedIfNotStarted(id,tri_time,this.mbean.getSender());
						
			  			HashMap data=new HashMap();
			  			try{
			  				data.put("scheduler_id", id);
			  				data.put("trigger_time", tri_time);
			  			}catch(Exception e){
			  				
			  			}
			  			
			  			OutgoingMessageCallBack omc=new OutgoingMessageCallBack(tuid,data){
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
			  				  		LoadBalancingQueue.getDefault().executionFailed(id,trigger_time,destination);
			  				  	}
			  				    releasePeer(getDestination());
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
			  						new SchedulerExePlanLogs(scheduler_id,tigger_time).log("Server fixes peer "+getDestination()+" for execution",SchedulerExePlanLogs.IGNORE_CODE);
			  					}
			  				}			  				 
			  			};
			  			omc.setPriority(OutgoingMessageCallBack.PRIORITY_VERY_HIGH);
			  			if(status==1){
			  				//lockPeer(this.mbean.getSender());
			  				respond(MessageBean.TYPE_RESPONSE,"EXECUTETASK:"+id+":"+tri_time+":"+ntri_time+":"+new Date().getTime()+":"+tuid+":CONFIRM",this.mbean,omc);		  				
			  			}
			  		}
			  	}
			  	processed=true;
		  		//client replies
	  			//respond(MessageBean.TYPE_RESPONSE,"EXECUTETASK:"+id+":CONFIRM",this.mbean);
		  	}

		  	    //server side
			  	if(this.mbean.getType().equals(MessageBean.TYPE_RESPONSE) && 
			  	   this.mbean.getCommand().startsWith("EXECUTETASK:") &&
			  	   this.mbean.getCommand().endsWith("FINISHED")
			  	){
			  		releasePeer(this.mbean.getSender());
			  		LoadBalancingQueue.getDefault().releasePeersCache4PriorityGr();
			  		
			  		
				  	int id=-1;
				  	long trg_time=0;
				  	StringTokenizer st=new StringTokenizer(this.mbean.getCommand(),":");
				  	String status="";
				  	if(st.countTokens()>=4){
				  			st.nextToken();
				  		    String sid=st.nextToken();
				  		    status=st.nextToken();
				  		    try{
				  		    	id=Integer.parseInt(sid);
				  		    }catch(Exception e){}
				  		    if(st.hasMoreTokens()){
				  		    	try{
				  		    		trg_time=Long.parseLong(st.nextToken());
				  		    	}catch(Exception e){ }
				  		    }
				  	}
				  	
				  	if(id>=0){			
				  		new SchedulerExePlanLogs(id,trg_time).log("Server rcvd completed signal",SchedulerExePlanLogs.IGNORE_CODE);				  		
				  		LoadBalancingQueue.getDefault().executionEnded(id,trg_time);			  						  		
				  		//IncomingMessage.updateFinishedPeersTime(this.mbean.getSender(),id,trg_time);
				  		IncomingMessage.updateFinishedPeersTime(this.mbean.getSender(),id,trg_time);
				  		try{
				  			IElementAttributes att= cache.getDefaultElementAttributes();
				  			att.setMaxLifeSeconds(1);
							cache.put("just_finished_id_"+id,"yes",att);								
							//remove the once it is processed.														
							//cache.get("tender_note_"+clientname,null,)
				  		}catch(Exception e){}
				  		
				  	}
				  	
				  	//client replies
			  		//respond(MessageBean.TYPE_RESPONSE,"EXECUTETASK:"+id+":CONFIRM",this.mbean);
				  	processed=true;
			  	}
		  			  	
		    //client side
		  	if( this.mbean.getType().equals(MessageBean.TYPE_RESPONSE) && 
				  	   this.mbean.getCommand().startsWith("EXECUTETASK:") &&
				  	   this.mbean.getCommand().endsWith("CONFIRM")
				  	){				  		
		  		    
		  		    //System.out.println("------->IncomingMessageParser:SERVER Confirm the execution here.....");   
		  		  	int id=-1;
		  		  	long tri_time=0;
		  		  	long ntri_time=0;
		  		  	long started_time=0;
					StringTokenizer st=new StringTokenizer(this.mbean.getCommand(),":");
					
					//System.out.println("IncomingMessageParser.parse() this.mbean.getCommand():"+this.mbean.getCommand());
					
					String tuid="";
					if(st.countTokens()>=6){
					  		st.nextToken();
					  		String sid=st.nextToken(); 	
					  		String tritime=st.nextToken();
					  		String ntritime=st.nextToken();
					  		String startedtm=st.nextToken();
					  		if(st.hasMoreTokens()){
					  		    	tuid=st.nextToken();
					  		}
					  		try{
					  		    	id=Integer.parseInt(sid);
					  		    	tri_time=Long.parseLong(tritime);
					  		    	ntri_time=Long.parseLong(ntritime);
					  		    	started_time=Long.parseLong(startedtm);
					  		}catch(Exception e){e.printStackTrace();}
					}
					
					//if(ScheduledTaskQueue.isExecutingOrQueued() ){
					//System.out.println("IncomingMessageParser.parse()qfactory.getQueue(tuid):"+qfactory.getQueue(tuid));
					
					//System.out.println("IncomingMessageParser.parse()qfactory.getQueue(tuid) is room?:"+qfactory.getQueue(tuid).isRoomForThread());
					 
	  				//try{
	  					//this was loced 5 seconds while bidding to avoid pickup few tasks at the same time.
	  					//cache.remove("resp2server_on_"+tuid);
	  				//}catch(Exception e){};		  				
		  			 
					 
					if(id>0 && tri_time>0){
							new SchedulerExePlanLogs(id,tri_time).log("Task Rcvd frm server ",SchedulerExePlanLogs.IGNORE_CODE);
					}				 
					
                    if(!qfactory.getQueue(tuid).isRoomForThread()){                    	
						 OutgoingMessageCallBack omc=new OutgoingMessageCallBack(){};
						 omc.setPriority(OutgoingMessageCallBack.PRIORITY_VERY_HIGH);
		  				 respond(MessageBean.TYPE_RESPONSE,"EXECUTEFAILED:"+id,this.mbean,omc);		  				  
		  				 log.error("Execute Failed, as more than a task tried adding into the queue");
		  				 
						 if(id>0 && tri_time>0){
								new SchedulerExePlanLogs(id,tri_time).log("Task failed, no room for execution",SchedulerExePlanLogs.IGNORE_CODE);
						 }
		  			 
		  			}else{
						if(id>=0){
							try{
								//System.out.println("------->IncomingMessageParser:task:"+id+" added to local queue");   
								//excecutes the task and responds to the server after executed.								
								//System.out.println("IncomingMessageParser.parse() id: before "+id);	
								boolean success=executeTask(id,tri_time,ntri_time,started_time);
								//System.out.println("IncomingMessageParser.parse() after: status:"+success);	
								
								if(!success){
					  				 if(id>0 && tri_time>0){
											new SchedulerExePlanLogs(id,tri_time).log("Task failed, excution failed at client",SchedulerExePlanLogs.IGNORE_CODE);
					  				 }

									 OutgoingMessageCallBack omc=new OutgoingMessageCallBack(){};
									 omc.setPriority(OutgoingMessageCallBack.PRIORITY_VERY_HIGH);
					  				 respond(MessageBean.TYPE_RESPONSE,"EXECUTEFAILED:"+id,this.mbean,omc);
								}
								
								//ask loadbalance queue to restart from to top so that queue will be processed in timely manner.
								 
								
							}catch(Exception e){e.printStackTrace();}
						}
		  			}
				  	//respond(MessageBean.TYPE_RESPONSE,"EXECUTETASK:"+id+":BID",this.mbean);
					processed=true;
					
			}
		  	
		    //client side
		  	if( this.mbean.getType().equals(MessageBean.TYPE_REQUEST) && 
				  	   this.mbean.getCommand().startsWith("EXECUTESCRIPT:") &&
				  	   this.mbean.getCommand().endsWith("CONFIRM")
				  	){				  		

		  		    //System.out.println("------->IncomingMessageParser:SERVER Confirm the execution here.....");   
		  		  	int script_id=-1;
					StringTokenizer st=new StringTokenizer(this.mbean.getCommand(),":");
					if(st.countTokens()==3){
					  		st.nextToken();
					  		String sid=st.nextToken(); 	
					  		try{
					  			script_id=Integer.parseInt(sid);					  		    	 
					  		}catch(Exception e){e.printStackTrace();}
					}
					if(ScheduledTaskQueue.isExecutingOrQueued() ){
					 
		  			}else{
						if(script_id>=0){
							try{
								 
								boolean success=executeScript(script_id);
																 
								
								//ask loadbalance queue to restart from to top so that queue will be processed in timely manner.
								 
								
							}catch(Exception e){e.printStackTrace();}
						}
		  			}
				  	//respond(MessageBean.TYPE_RESPONSE,"EXECUTETASK:"+id+":BID",this.mbean);
					processed=true;
					
			}

		  	
		  	if( this.mbean.getType().equals(MessageBean.TYPE_RESPONSE) && 
				  	   this.mbean.getCommand().startsWith("EXECUTEFAILED:")
				  	){
		  			StringTokenizer st=new StringTokenizer(this.mbean.getCommand(),":");
		  			int id=0;
		  			if(st.countTokens()==2){
		  				st.nextToken();
				  		String sid=st.nextToken();
				  		try{
			  		    	id=Integer.parseInt(sid);
				  		}catch(Exception e){}
				  		if(id>0){
				  			//LoadBalancingQueue.moveClientFailed2Queue(this.mbean.getSender(),id);
				  			LoadBalancingQueue.getDefault().executionFailed(id,0, this.mbean.getSender());
				  			LoadBalancingQueue.getDefault().releasePeersCache4PriorityGr();
				  		}
		  			}
		  		   processed=true;
		  	}	
		  	
		  	
		  	//getting R Script;
			if( this.mbean.getType().equals(MessageBean.TYPE_REQUEST) && 
				  	   this.mbean.getCommand().startsWith("RSCRIPT:")
				  	){
				
				  // System.out.println("~~~~~~~~ IncomingMessageParser.RSCRIPT:"+this.mbean.getCommand());
				
				   //System.out.println("~~~~~~~~ IncomingMessageParser.AttachmentName:"+this.attachmentName);
				   //System.out.println("~~~~~~~~ IncomingMessageParser.AttachmentName:"+this.attachment);;
				   
				   for(Iterator<String> it=this.attachments.keySet().iterator();it.hasNext();){
					   String key=it.next();
					   String value=this.attachments.get(key);					  
					   //System.out.println("~~~~~~~~ IncomingMessageParser.key:"+key+" value:"+value);
				   }
				
		  		   processed=true;
		  	}	
			
	        //if(!this.mbean.getReply().equalsIgnoreCase(MessageBean.REPLYBACK) && this.mbean.getType().equals(MessageBean.TYPE_RESPONSE)){
		  	if(!processed) {
	    	   IncomingMessage.messages.add(this.mbean);
		  	}  
	}
	
	
	private synchronized void  lockPeer(String peer){
		try{
			if(cache==null)	cache=JCS.getInstance("multi-purpose");
  			IElementAttributes att= cache.getDefaultElementAttributes();
			att.setMaxLifeSeconds(5); 
		    cache.put("peer_locked_"+peer, "locked",att);
		}catch(Exception e){}
	}

	private synchronized boolean  lockPeerIfFree(String peer){
		boolean rtn=true;
		if(cache!=null &&  cache.get("peer_locked_"+peer)!=null ){
			 rtn=false;
		}else{
			lockPeer(peer);
		}
		return rtn;
	}

	
	private synchronized void  releasePeer(String peer){
		if(cache!=null){
			try{
				cache.remove(peer);
			}catch(Exception e){}
		}
	}
	

	private synchronized void respond2Server(){
		
		if(cache==null){
			try{
				cache=JCS.getInstance("multi-purpose");
			}catch(Exception e){}
		}   
		
		//once it bids it never bids again within a second
		//boolean respondServer=(cache!=null && cache.get("responded2server")!=null)?false:true;
		
  		//if(respondServer){  			

			String id="";
	  		String tri_time="";
	  		String ntri_time="";
	  		String tuid="";
	  		StringTokenizer st=new StringTokenizer(this.mbean.getCommand(),":");
	  		if(st.countTokens()>=5){
	  			st.nextToken();
	  		    id=st.nextToken();
	  		    tri_time=st.nextToken();
	  		    ntri_time=st.nextToken();
	  		    if(st.hasMoreTokens()){
	  		    	tuid=st.nextToken();
	  		    }
	  		}
	  		
	  		//if(cache.get("resp2server_on_"+tuid)!=null){
	  		 	 //System.out.println("resp2server_on_"+tuid+" is set for id:"+cache.get("resp2server_on_"+tuid)+" new id:"+id);
	  		//}
	  		
	  		if(new QueueFactory().getQueue(tuid).isRoomForThread()){ //&& cache.get("resp2server_on_"+tuid)==null){
	  			
		  		//try{			  			
		  			//IElementAttributes att= cache.getDefaultElementAttributes();
					//att.setMaxLifeSeconds(1); //for testing purpose I add 200 seconds waiting time, this should be only 3-5 on production.
					//cache.put("resp2server_on_"+tuid,id,att);	
		  		//}catch(Exception e){}
		  		//client replies  		
	  			
	  			
  			    
		  		OutgoingMessageCallBack omc=new OutgoingMessageCallBack(tuid,null){		  			
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
	  			respond(MessageBean.TYPE_REQUEST,"EXECUTETASK:"+id+":"+tri_time+":"+ntri_time+":"+tuid+":BID",this.mbean,omc);
	  		}
  		//}

		
	}
	
	/*
	private static JCS peercommcache=null;
	
	private static boolean isPeerCommCached(String peername, String taskuid){
 
		    boolean rtn=false;
			if(peercommcache!=null ){
				if(peercommcache.get(peername+"_"+taskuid)!=null){
					rtn=true;
				}
			}
			return rtn;
			 
		 
	}
	private void removePeerLock(String peername, String taskuid){
		try{
			if(peercommcache!=null){
				peercommcache.remove(peername+"_"+taskuid);		 	
			}

		}catch(Exception e){
			
		}
	}
	
	private static void noTender4Seconds(String peername, String taskuid){
		try{
			if(peercommcache==null){
		 		peercommcache=JCS.getInstance("delay-peer-comm");
			}
			IElementAttributes att= peercommcache.getDefaultElementAttributes();
			att.setMaxLifeSeconds(2);
			peercommcache.put(peername+"_"+taskuid, "yes", att);
		}catch(Exception e){
			
		}
	}
	*/
	
	private static void respond(String messageType, String command, MessageBean mbean, OutgoingMessageCallBack sentcallback) {
	//private static synchronized void respond(String messageType, String command, MessageBean mbean, OutgoingMessageCallBack sentcallback) {
		   PeerGroup netPeerGroup =  P2PService.getPeerGroup();
		   PipeAdvertisement pipeAdv = new P2PAdvertisement().getPipeAdvertisement(mbean.getSender(),netPeerGroup);
		   MessageBean response=new MessageBean();
		   response.setReply(MessageBean.REPLYBACK);
		   response.setType(messageType);   
		   response.setCommand(command);
		   response.setSender(P2PService.getComputerName());
		   
		   OutgoingMessage ogM=new OutgoingMessage(sentcallback,response,mbean.getSender());
		   
		   PipeService pipeService = P2PService.getPipeService();
		   try{
			    //System.out.println("------->IncomingMessageParser: Sending MSG TO:"+mbean.getSender()+":"+command.toString());
				pipeService.createOutputPipe(pipeAdv,ogM);
		   }catch(Exception e){
			   	  e.printStackTrace();
			   	  PipeService pipeService1 = P2PService.getNewPipeService();
			   	  try{
			   		  pipeService1.createOutputPipe(pipeAdv,ogM);
			   	  }catch(Exception e1){			   		  
			   		  e1.printStackTrace();
			   	  }
				 
		   }
	}

	
	
	private synchronized  boolean executeScript(int script_id) throws Exception {
		
		try{

			SchedulerDB sdb=SchedulerDB.getSchedulerDB();
			sdb.connectDB();			
			Map data=sdb.getRScript(script_id);
			//HashMap data=new HashMap();
			data.put("script_id", script_id);
			//data.put("script",script);
			
			//ScheduledTask task=new ScheduledTaskFactory().getTask(taskuid);
			//added 
			//ScheduledTask task=new ScheduledTaskFactory().getTaskFromAll( );
			
			StackFrame sf=new StackFrame(null,data);
			//sf.setTrigger_time(trig_time);
			//sf.setNexttrigger_time(ntrig_time);
			sf.setMbean(this.mbean);
			//sf.setStarted_time(startedtime);
			
			sf.addCallBack(new StackFrameCallBack(){				
				public void callBack(StackFrame sf,String status, SchedulerException se){

					try{
						 //PeerNotificationTray.systemTray(null);
					}catch(Exception e){}

					//to-do
					//log the details
					
					/*
					try{
					 PeerNotificationTray.systemTray(null);
					}catch(Exception e){}
					//System.out.println("------->IncomingMessageParser:id:"+sf.getData().get("id")+" Successfully executed");
					 OutgoingMessageCallBack omc=new OutgoingMessageCallBack(){};
					 omc.setPriority(OutgoingMessageCallBack.PRIORITY_VERY_VERY_HIGH);
					 respond(MessageBean.TYPE_RESPONSE,"EXECUTETASK:"+sf.getData().get("id")+":"+status+":FINISHED",sf.getMbean(),omc);
					 */
				}
				
			});
			
			boolean rtn=false;
			if(!ScheduledTaskQueue.isExecutingOrQueued() ){
				ScheduledTaskQueue.add(sf);
				//PeerNotificationTray.systemTray("Task:"+data.get("name"));
				//int status=ScheduledTaskQueue.addOrCheckQueue(sf);
				//if(status==1){
				//	return true;
				//}
				rtn=true;
			}
			sdb.closeDB();
			return rtn;
		}catch(Exception e){
			ClientError.reportError(e, null);
			throw e;
		}
	}
	
	public synchronized void removePeerThreadStatus(String peername)  {
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		try{
				
			sdb.connectDB();	
			sdb.removeAllPeerThreadStatus(peername);
			
		}catch(Exception e){
			log.error("Error:"+e.getMessage());
		}finally{
			try{sdb.closeDB();}catch(Exception e1){}
		}
		
	}
	
	
	/**
	 * this is not being used currently. the new way is com.fe.p2p.msg.SendTask.java
	 * @deprecated
	 * @param scheduler_id
	 * @param trig_time
	 * @param ntrig_time
	 * @param startedtime
	 * @return
	 * @throws Exception
	 */
	private synchronized  boolean executeTask(int scheduler_id,long trig_time,long ntrig_time,long startedtime) throws Exception {
		
		try{

			SchedulerDB sdb=SchedulerDB.getSchedulerDB();
			sdb.connectDB();			
			Map data=sdb.getScheduler(scheduler_id);
			String taskuid=(String)data.get("taskuid");
			
			//ScheduledTask task=new ScheduledTaskFactory().getTask(taskuid);
			//added 
			ScheduledTask task=new ScheduledTaskFactory().getTaskFromAll(taskuid);
			
			StackFrame sf=new StackFrame(task,data);
			sf.setTrigger_time(trig_time);
			sf.setNexttrigger_time(ntrig_time);
			sf.setMbean(this.mbean);
			sf.setStarted_time(startedtime);
			sf.addCallBack(new StackFrameCallBack(){				
				public void callBack(StackFrame sf,String status,SchedulerException se){
					Number nid=(Number)sf.getData().get("id");					
					try{
					// PeerNotificationTray.systemTray(null);
					}catch(Exception e){}
					//System.out.println("------->IncomingMessageParser:id:"+sf.getData().get("id")+" Successfully executed");
					 OutgoingMessageCallBack omc=new OutgoingMessageCallBack(){};
					 omc.setPriority(OutgoingMessageCallBack.PRIORITY_VERY_VERY_HIGH);
					 new SchedulerExePlanLogs(nid.intValue(),sf.getTrigger_time()).log("Task completed, Reply server, STATUS:"+status,SchedulerExePlanLogs.IGNORE_CODE);					 
					 respond(MessageBean.TYPE_RESPONSE,"EXECUTETASK:"+sf.getData().get("id")+":"+status+":"+sf.getTrigger_time()+":FINISHED",sf.getMbean(),omc);
				}
			});
			
			
			boolean rtn=false;
			QueueAbstract qa=new QueueFactory().getQueue(taskuid);
			if(qa.isRoomForThread()){
				qa.addExThread(sf);
				rtn=true;
			}
			/*
			if(!ScheduledTaskQueue.isExecutingOrQueued() ){
				ScheduledTaskQueue.add(sf);
				//PeerNotificationTray.systemTray("Task:"+data.get("name"));
				//int status=ScheduledTaskQueue.addOrCheckQueue(sf);
				//if(status==1){
				//	return true;
				//}
				rtn=true;
			}
			*/
			sdb.closeDB();
			return rtn;
		}catch(Exception e){
			ClientError.reportError(e, null);
			throw e;
		}
	}
	
	/**
	 * @deprecated
	 * @param scheduler_id
	 * @param trig_time
	 * @param ntrig_time
	 * @param startedtime
	 * @return
	 * @throws Exception
	 */
	private synchronized  boolean executeTaskOld(int scheduler_id,long trig_time,long ntrig_time,long startedtime) throws Exception {
		
		try{

			SchedulerDB sdb=SchedulerDB.getSchedulerDB();
			sdb.connectDB();			
			Map data=sdb.getScheduler(scheduler_id);
			String taskuid=(String)data.get("taskuid");
			
			//ScheduledTask task=new ScheduledTaskFactory().getTask(taskuid);
			//added 
			ScheduledTask task=new ScheduledTaskFactory().getTaskFromAll(taskuid);
			
			StackFrame sf=new StackFrame(task,data);
			sf.setTrigger_time(trig_time);
			sf.setNexttrigger_time(ntrig_time);
			sf.setMbean(this.mbean);
			sf.setStarted_time(startedtime);
			sf.addCallBack(new StackFrameCallBack(){				
				public void callBack(StackFrame sf,String status,SchedulerException se){
					try{
					 //PeerNotificationTray.systemTray(null);
					}catch(Exception e){}
					//System.out.println("------->IncomingMessageParser:id:"+sf.getData().get("id")+" Successfully executed");
					 OutgoingMessageCallBack omc=new OutgoingMessageCallBack(){};
					 omc.setPriority(OutgoingMessageCallBack.PRIORITY_VERY_VERY_HIGH);
					 respond(MessageBean.TYPE_RESPONSE,"EXECUTETASK:"+sf.getData().get("id")+":"+status+":FINISHED",sf.getMbean(),omc);
				}
			});
			
			
			boolean rtn=false;
		 
			 
			if(!ScheduledTaskQueue.isExecutingOrQueued() ){
				ScheduledTaskQueue.add(sf);
				//PeerNotificationTray.systemTray("Task:"+data.get("name"));
				//int status=ScheduledTaskQueue.addOrCheckQueue(sf);
				//if(status==1){
				//	return true;
				//}
				rtn=true;
			}
			 
			sdb.closeDB();
			return rtn;
		}catch(Exception e){
			ClientError.reportError(e, null);
			throw e;
		}
	}
	
	
	public static String getServerStatistics() throws Exception {
		return getStatistics();
	}
	
	public static String getServerPeerQueueStat() throws Exception {
		return getPeerQueueStat();
	}
	
	
	private static String getPeerQueueStat() throws Exception {
		String stat="";
		Vector taskData=new QueueFactory().getExecutingTasksData();
		for(Iterator i=taskData.iterator();i.hasNext();){
			Map data=(Map)i.next();
			stat+="peer="+P2PService.getComputerName();
			stat+=",scheduler_id="+data.get(QueueFactory.KEY_SCHEDULER_ID);
			stat+=",trigger_time="+data.get(QueueFactory.KEY_TRIGGER_TIME);
			stat+=",started_time="+data.get(QueueFactory.KEY_STARTED_TIME);
			stat+="|";
		}
		return stat;
		
	}
	
	
	private static String getStatistics() throws Exception {
		
		try{
			SimpleDateFormat dateFormat1 =new SimpleDateFormat("HH 'Hrs' mm 'Mns'");
			dateFormat1.setTimeZone(TimeZone.getTimeZone("GMT"));
			
			 //SimpleDateFormat dateFormat =new SimpleDateFormat("mm 'Mns'");
			SimpleDateFormat dateFormat2 =new SimpleDateFormat("D 'days' HH 'Hrs' mm 'Mns'");
			dateFormat2.setTimeZone(TimeZone.getTimeZone("GMT"));
	
			String diff=null;
			if(P2PService.getPeerStartedTime()!=null){
			 long diffl=new Date().getTime()-P2PService.getPeerStartedTime().getTime();
			 if(diffl>86400000){
				 diff=dateFormat2.format(new Date(diffl));
			 }else{
				 diff=dateFormat1.format(new Date(diffl));
			 }
			}
	
			String stat="PEER="+P2PService.getComputerName();
			stat+="|IP="+InetAddress.getLocalHost().getHostAddress();
			stat+="|R_Version="+RScriptScheduledTask.getRVersion();
			stat+="|JRI_Compatibility="+RScriptScheduledTask.getJRICompatible();
			stat+="|OS="+System.getProperty("os.name");
			
			SimpleDateFormat dateFormat3 =new SimpleDateFormat("hh.mm.ssa");
			stat+="|CLOCK_TIME="+dateFormat3.format(new Date())+" "+TimeZone.getDefault().getDisplayName();;	
			stat+="|PEER_UPDATED_ON="+getSourceUpdated();
			stat+="|SYS_ARCH="+System.getProperty("sun.arch.data.model")+" bit";
			
			Vector taskids=new QueueFactory().getExecutingIDs();
			//if(ScheduledTaskQueue.getExecutingTaskId()!=null){
			if(taskids!=null && taskids.size()>0){
				//stat+="|EXECUTING_TASK_ID="+ScheduledTaskQueue.getExecutingTaskId();
				String tids="";
				for(Iterator i=taskids.iterator();i.hasNext();){
					tids+=(tids.equals("")) ?i.next():","+i.next();
				}
				stat+="|EXECUTING_TASK_ID="+tids;
				
			}else{
				stat+="|EXECUTING_TASK_ID=NONE";
			}
			if(diff!=null){
				stat+="|PEER_RUNNING="+diff;
			}else{
				stat+="|PEER_RUNNING=";
			}
			return stat;
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
		
	}
	
	
	
	
    private static Date rootlist(File filenames,Date date ) {
		//File filenames = new File(fname);
		if (!filenames.isDirectory()) {
			//System.out.println(fname);
			Date filemodified=new Date(filenames.lastModified());
			if(date==null || (date!=null && date.before(filemodified))){
				date=filemodified;
			}
			//System.out.println("folder:"+filenames.getPath() +" date:"+date);
			return date;
			
		}
		File filelists[] = filenames.listFiles();
		for (int i = 0; i < filelists.length; i++) {
			date=rootlist( filelists[i],date );
		}
		return date;
	}
    private static String getSourceUpdated()    {
    	
    	
    	String classfolder=IncomingMessageParser.class.getResource("").getPath().replace("com/fe/p2p/listener", "");
    	String root=IncomingMessageParser.class.getResource("").getPath().replace("WEB-INF/classes/com/fe/p2p/listener", "js");
    	
    	Date date=null;
    	date=rootlist(new File(classfolder),date);    	
    	date=rootlist(new File(root),date);
    	SimpleDateFormat sdf=new SimpleDateFormat("dd MMM,yyyy hh.mma");
    	return sdf.format(date);
    	//return new File(this.getClass().getResource("").getPath()).getParent();
    	
    	
    }
	

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    /**
     * 
     * @deprecated
     */
    
    
}


