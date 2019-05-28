/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.p2p.msg.scheduler;

import org.apache.jcs.JCS;
import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PeerCacheLock {

	
	private static synchronized void  lockPeer(String peer, String taskuid) throws Exception{
		try{
			 
  			IElementAttributes att= getCache().getDefaultElementAttributes();
			att.setMaxLifeSeconds(1); 
		    getCache().put("peer_locked_"+peer+taskuid, "locked",att);
		}catch(Exception e){}
	}

	public static synchronized boolean  lockPeerIfFree(String peer, String taskuid) {
		
		Logger log = LogManager.getLogger(PeerCacheLock.class.getName());
		
		boolean rtn=true;
		try{
			if(getCache()!=null &&  getCache().get("peer_locked_"+peer+taskuid)!=null ){
				 rtn=false;
			}else{
				lockPeer(peer,taskuid);
			}
		}catch(Exception e){}
		log.debug("locking the peer "+peer+" taskuid:"+taskuid);
		return rtn;
	}

	
	public static synchronized void  releasePeer(String peer,String taskuid) {
		 try{	
			if(getCache()!=null){
				getCache().remove(peer);				 
			}
		}catch(Exception e){}
	}
	
	private static JCS cache=null;
	private static JCS getCache() throws Exception {
		 if(cache==null){
				cache=JCS.getInstance("PeerCacheLock");
		 }
		 return cache;
	}
	
	
}


