/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.io.msg;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fourelementscapital.scheduler.io.request.IOPeerRequest;
import com.fourelementscapital.scheduler.io.server.ServerConnection;
import com.fourelementscapital.scheduler.io.server.ServerConnectionHandler;
import com.fourelementscapital.scheduler.queue.QueueStackManager;

public class PeerRequestLogin extends IOPeerRequest {

	private String peername=null;
	private Logger log = LogManager.getLogger(PeerRequestLogin.class.getName());
	private String queuestring=null;
	
	
	public String getQueuestring() {
		return queuestring;
	}

	public void setQueuestring(String queuestring) {
		this.queuestring = queuestring;
	}

	public String getPeername() {
		return peername;
	}

	public void setPeername(String peername) {
		this.peername = peername;
	}
	
	

	
	@Override
	public void executeAtServer(ServerConnection sc) {
		log.debug(" logged-in message...from peer:"+this.peername+" ServerConnection, IP:"+sc.getIp()+" connected time:"+sc.getConnectedtime()+" qstring:"+getQueuestring());
		sc.setUser(this.peername);
		try{			
			ServerConnectionHandler.refreshServerConnection(sc);			
			QueueStackManager.buildQueue4Peer(this.peername);
			QueueStackManager.server2SyncPeerQueue(this.peername, getQueuestring());

		}catch(Exception e){
			log.error("error while refreshing, e:"+e.getMessage());
		}
	}

}


