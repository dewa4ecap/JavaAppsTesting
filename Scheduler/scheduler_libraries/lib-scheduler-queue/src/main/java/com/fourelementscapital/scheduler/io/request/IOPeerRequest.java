/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.io.request;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fourelementscapital.scheduler.io.peer.PeerClientHandler;
import com.fourelementscapital.scheduler.io.server.ServerConnection;
import com.fourelementscapital.scheduler.p2p.msg.MessageNames;

public abstract class IOPeerRequest {
	
	
	private Logger log = LogManager.getLogger(IOPeerRequest.class.getName());
	public abstract void executeAtServer(ServerConnection pc);
	
	public final void send(){
		try{

			HashMap h=new HashMap();	   
		   Map data=BeanUtils.describe(this);
		   data.put(MessageNames.MESSAGE_BEAN_NAME,getClass().getName());	  
		   PeerClientHandler.getChannelHandler().writeAndFlush(data);
	
		}catch(Exception e){
			log.error("Error:"+e.getMessage());
		}
		
	}
	
	
}


