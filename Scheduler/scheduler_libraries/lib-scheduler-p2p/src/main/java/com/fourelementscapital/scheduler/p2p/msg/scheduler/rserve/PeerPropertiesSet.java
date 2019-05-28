/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.p2p.msg.scheduler.rserve;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fourelementscapital.scheduler.p2p.msg.MessageHandler;
import com.fourelementscapital.scheduler.p2p.peer.PeerSpecificConfigurations;

public class PeerPropertiesSet extends MessageHandler {

	
	private String peerConcurrentThread=null;
	private String peerSessionExecutionMax=null;
	private Logger log = LogManager.getLogger(PeerPropertiesSet.class.getName());

	public Map executeAtDestination() {
		
		// this log error is for production debugging purpose. remove when it's done. itask 8257.
		log.error(">>>>> debug itask 8257 >>>>> PeerPropertiesSet.executeAtDestination() - start");
		log.error(">>>>> debug itask 8257 >>>>> PeerPropertiesSet.getPeerConcurrentThread() : " + getPeerConcurrentThread());
		log.error(">>>>> debug itask 8257 >>>>> PeerPropertiesSet.getPeerSessionExecutionMax() : " + getPeerSessionExecutionMax());
		
		try{
			Properties p=new Properties();
			p.setProperty(PeerSpecificConfigurations.KEY_CONCURRENT_SESSION, getPeerConcurrentThread());
			p.setProperty(PeerSpecificConfigurations.KEY_MAX_EXEC_SESSION, getPeerSessionExecutionMax());
			PeerSpecificConfigurations.syncPartial(p);
		}catch(Exception e){
			log.error("Error:"+e.getMessage());
		}

		// this log error is for production debugging purpose. remove when it's done. itask 8257.
		log.error(">>>>> debug itask 8257 >>>>> PeerPropertiesSet.executeAtDestination() - end");		
		
		return new HashMap();
		
		///return null;
		
	}


	public String getPeerConcurrentThread() {
		return peerConcurrentThread;
	}


	public void setPeerConcurrentThread(String peerConcurrentThread) {
		this.peerConcurrentThread = peerConcurrentThread;
	}


	public String getPeerSessionExecutionMax() {
		return peerSessionExecutionMax;
	}


	public void setPeerSessionExecutionMax(String peerSessionExecutionMax) {
		this.peerSessionExecutionMax = peerSessionExecutionMax;
	}


	@Override
	public void onSendingFailed() {
		// TODO Auto-generated method stub
		
	}

}


