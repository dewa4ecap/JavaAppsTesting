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

import com.fourelementscapital.scheduler.io.server.ServerConnection;
import com.fourelementscapital.scheduler.io.server.ServerConnectionHandler;
import com.fourelementscapital.scheduler.p2p.msg.MessageNames;

public abstract class IOServerRequest  {

	public abstract void executeAtPeer();
	
	private Logger log = LogManager.getLogger(IOServerRequest.class.getName());
	
	
	//private static long lastsent=0 ;
	
	
	public final void send(String peername){
		try{
		
		   
		   HashMap h=new HashMap();	   
		   Map data=BeanUtils.describe(this);
		   data.put(MessageNames.MESSAGE_BEAN_NAME,getClass().getName());	  
		   ServerConnection sc=ServerConnectionHandler.getServerConnection(peername);		   
		   if(sc!=null){
			   sc.getChcontext().writeAndFlush(data);		   
		   }
		   
		   log.debug("sending this:peername"+peername);
		   
		   /*
		   if(lastsent>0){
			  long delay=new Date().getTime()-lastsent;
			  if(delay>1000){
				  log.error("delay >500ms: "+delay);
			  }
			  
		   }
		   lastsent=new Date().getTime();
		   */
		   
		}catch(Exception e){
			log.error("Error:"+e.getMessage());
		}
	}
	
	/**
	 * @deprecated
	 * @param sc
	 */
	private final void send(ServerConnection sc){
		try{
		
		   HashMap h=new HashMap();	   
		   Map data=BeanUtils.describe(this);
		   data.put(MessageNames.MESSAGE_BEAN_NAME,getClass().getName());  
		   log.debug("sc:"+sc);
		   if(sc!=null){
			   sc.getChcontext().writeAndFlush(data);
		   }
	
		}catch(Exception e){
			log.error("Error:"+e.getMessage());
		}
	}	
}


