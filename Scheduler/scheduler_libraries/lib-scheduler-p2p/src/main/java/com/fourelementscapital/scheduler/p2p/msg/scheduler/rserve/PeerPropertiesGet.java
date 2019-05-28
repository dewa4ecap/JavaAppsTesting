/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.p2p.msg.scheduler.rserve;

import java.util.Map;
import java.util.Properties;

import org.apache.jcs.JCS;
import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fourelementscapital.scheduler.p2p.msg.MessageHandler;
import com.fourelementscapital.scheduler.p2p.msg.PostCallBack;
import com.fourelementscapital.scheduler.p2p.peer.PeerSpecificConfigurations;

public class PeerPropertiesGet extends MessageHandler implements PostCallBack {



	private Logger log = LogManager.getLogger(PeerPropertiesGet.class.getName());
	
	public Map executeAtDestination() {
		
		Properties p=null;
		try{
			p=PeerSpecificConfigurations.getProperties();		
		}catch(Exception e){
			log.error("Error:"+e.getMessage());			
		}
		return p;
	}


	public void callBack(Map data) {
	
		try{
			IElementAttributes att= getCache().getDefaultElementAttributes();
			att.setMaxLifeSeconds(20);
			getCache().put("peer_"+this.getMsgCreator(),data,att);
		}catch(Exception e){			
			log.error("Error:"+e.getMessage());			
		}
		
	}

	public static Map getPeerCachedProp(String peername) throws Exception {		
		Map data=(Map)getCache().get("peer_"+peername);
		return data;
		
	}
	
	private static JCS cache=null;
	private static JCS getCache() throws Exception {
		 if(cache==null){
				cache=JCS.getInstance(PeerPropertiesGet.class.getName());
		 }
		 return cache;
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


