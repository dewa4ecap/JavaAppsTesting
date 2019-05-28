/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.p2p.msg.scheduler;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.jcs.JCS;
import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fourelementscapital.db.SchedulerDB;
import com.fourelementscapital.scheduler.p2p.listener.IncomingMessage;
import com.fourelementscapital.scheduler.p2p.listener.OutgoingMessageCallBack;
import com.fourelementscapital.scheduler.p2p.msg.MessageHandler;
import com.fourelementscapital.scheduler.p2p.msg.PostCallBack;
import com.fourelementscapital.scheduler.p2p.peer.PeerMachine;
import com.fourelementscapital.scheduler.p2p.peer.PeerManagerHSQL;
import com.fourelementscapital.scheduler.peer.QueueFactory;

public class PeerOnlineStatus extends MessageHandler implements PostCallBack {

	
	private String status=null;
	private String peerversion=null;
	

	public String getPeerversion() {
		return peerversion;
	}

	public void setPeerversion(String peerversion) {
		this.peerversion = peerversion;
	}

	public String getStatus() {
		return status;
	}


	public void setStatus(String status) {
		this.status = status;
	}

	private Logger log = LogManager.getLogger(PeerOnlineStatus.class.getName());
	
	public int getPriority() {
		return  OutgoingMessageCallBack.PRIORITY_LOW;
	}
	 
	
	/**
	 * Will be executed in peer side.
	 */
	public Map executeAtDestination() {
		
		Map h=new HashMap();
		QueueFactory qfactory=new QueueFactory();
		setPeerversion(PeerMachine.getLastVersion());
		if(qfactory.countExcTasksInPeer()>0){	
			 //Number scheduler_id=(Number)ScheduledTaskQueue.getExecutingStackFrame().getData().get("id");
			 String ids="";
			 String times="";
			 h=qfactory.getExecutingIDAndSTimes();			  
			 setStatus("BUSY");
		}else{
			 setStatus("NOBUSY");
		}
		return h;
		
	}

	public void callBack(Map data) {
		
	    String peer=getMsgCreator();
	    
	    updatePeerStatus(data,peer);
	    log.debug("Message reccied from client:~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ peer "+peer+" ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	    
		//remove finished ones from time, if no busy then removes from the timing collection.
		
  		
  		 
		
	}

	
	public void updatePeerStatus(Map data, String peer) {

	
		
		try{
	        if(getCache().get("peer_"+peer)==null){
	        	IElementAttributes att= getCache().getDefaultElementAttributes();
	        	att.setMaxLifeSeconds(30);
	        	getCache().put("peer_"+peer,peer,att);
	        	updateLastOnline(peer);
	        	log.debug("----->caching peer:"+peer);
	        }
		}catch(Exception e){
			log.error("Error while caching peer online status");
		}		
  		IncomingMessage.peersUpdate(peer,getStatus()); 		
  		new PeerManagerHSQL().updatePeerResponse(peer, data,getPeerversion());
  		
	}
	
	
	private static JCS cache=null;
	private static JCS getCache() throws Exception {
		 if(cache==null){
				cache=JCS.getInstance("PeerOnlineStatus");
		 }
		 return cache;
	}
	
	
	public synchronized void updateLastOnline(String peername)  {
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		try{
				
			sdb.connectDB();	
			sdb.updatePeersLastOnline(peername, new Date().getTime());
			
		}catch(Exception e){
			log.error("Error:"+e.getMessage());
		}finally{
			try{sdb.closeDB();}catch(Exception e1){}
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
	
	public static Collection getOnlineRespondingPeers() throws Exception {
		Map peers=getCache().getMatching("^[A-Za-z0-9_]+$");
		return peers.values();
		
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


