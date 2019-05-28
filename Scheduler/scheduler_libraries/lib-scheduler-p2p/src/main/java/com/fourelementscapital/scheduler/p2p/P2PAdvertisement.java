/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.p2p;

import java.net.URISyntaxException;
import java.security.MessageDigest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fourelementscapital.scheduler.config.Config;

import net.jxta.document.AdvertisementFactory;
import net.jxta.id.IDFactory;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.pipe.PipeID;
import net.jxta.pipe.PipeService;
import net.jxta.protocol.PeerAdvertisement;
import net.jxta.protocol.PipeAdvertisement;
 

public class P2PAdvertisement {

	   public final static String PIPEIDSTR = "urn:jxta:uuid-59616261646162614E50472050325033C0C1DE89719B456691A596B983BA0E1004";
	   
	   private String taskpip="taskpip";
	   
	   private Logger log = LogManager.getLogger(P2PAdvertisement.class.getName());

	   
	   public P2PAdvertisement () {
		   if(Config.getValue("p2p.taskpip")!=null && !Config.getValue("p2p.taskpip").equals("") ){
			   taskpip= Config.getValue("p2p.taskpip");
		   }
		   
	   }
	   
	   public P2PAdvertisement (boolean flag_helper) {
		   //taskpip="taskpip_helper";		   
		   //if(Config.getValue("p2p.taskpip.helper")!=null && !Config.getValue("p2p.taskpip.helper").equals("") ){
			//   taskpip= Config.getValue("p2p.taskpip.helper");
		   //}

		   if(Config.getValue("p2p.taskpip")!=null && !Config.getValue("p2p.taskpip").equals("") ){
			   taskpip= Config.getValue("p2p.taskpip");
		   }
		   
	   }
	   
	   
	   
	   public PipeAdvertisement getPipeAdvertisement(String computername,PeerGroup pg) {
		     //if(Config.getValue("p2p.taskpip")!=null && !Config.getValue("p2p.taskpip").equals("") ){
			 //  taskpip= Config.getValue("p2p.taskpip");
			 //}		  
		     log.debug("creating advertishment for computer:"+computername+" pg:"+pg.getPeerGroupName()+" peerrname:"+pg.getPeerName());
		   
	    	 PipeID pipeID = null;
	         try {	        	 
	        	 pipeID =createPipeID(pg.getPeerGroupID(),computername,taskpip);
	        	 log.debug("Pipe Advert ID:"+pipeID);
	         } catch (URISyntaxException use) {
	             use.printStackTrace();
	         } catch (Exception ex) {
	        	 ex.printStackTrace();
	         }
	        PipeAdvertisement advertisement = (PipeAdvertisement) AdvertisementFactory.newAdvertisement(PipeAdvertisement.getAdvertisementType());
	        advertisement.setPipeID(pipeID);
	        advertisement.setType(PipeService.PropagateType);
	        advertisement.setName(computername);
	        return advertisement;
	    }


	   public PipeAdvertisement getPipeAdvertisement(PipeID pipeID, PeerGroup pg) {	     
	        PipeAdvertisement advertisement = (PipeAdvertisement) AdvertisementFactory.newAdvertisement(PipeAdvertisement.getAdvertisementType());
	        advertisement.setPipeID(pipeID);
	        advertisement.setType(PipeService.PropagateType);	       
	        return advertisement;
	    }

	   
	   public PeerAdvertisement getPeerAdvertisement(String computername,PeerGroup pg) throws Exception  {
			 PeerID peerID = null;
			 // if(Config.getValue("p2p.taskpip")!=null && !Config.getValue("p2p.taskpip").equals("") ){
			 //	   taskpip= Config.getValue("p2p.taskpip");
			 // }
			 log.debug("creating advertishment for computer:"+computername+" pg:"+pg.getPeerGroupName()+" peerrname:"+pg.getPeerName());
	         try {
	        	 
	        	 peerID =createPeerID(pg.getPeerGroupID(),computername+"peer",taskpip);
	         } catch (URISyntaxException use) {
	             use.printStackTrace();
	         } catch (Exception ex) {
	        	 ex.printStackTrace();
	         }
	         
			PeerAdvertisement advertisement = (PeerAdvertisement)   AdvertisementFactory.newAdvertisement(PeerAdvertisement.getAdvertisementType());
	        //PipeID pipeID =MD4ID.createPipeID(pg.getPeerGroupID(),computername,"taskpip");
	        advertisement.setPeerID(IDFactory.newPeerID(PeerGroupID.worldPeerGroupID));
	        //advertisement.setPeerID(peerID);
	        advertisement.setPeerGroupID(IDFactory.newPeerGroupID(PeerGroupID.defaultNetPeerGroupID));	       
	         
	        advertisement.setName(computername);
	        return advertisement;
	    }
	    
		
    
		private  final byte[] generateHash(String clearTextID, String function)	throws Exception {
			String id;
			if (function == null) {
				id = clearTextID;
			} else {
				id = clearTextID + "" + function;
			}
			byte[] buffer = id.getBytes();
			MessageDigest algorithm = null;
			algorithm = MessageDigest.getInstance("MD5");
			// Generate the digest.
			algorithm.reset();
			algorithm.update(buffer);
			byte[] digest1 = algorithm.digest();
			return digest1;
		}

		private  final PipeID createPipeID( PeerGroupID peerGroupID
				                                                                 , String clearTextID
				                                                                , String function) throws Exception {
			     byte[] digest = generateHash(clearTextID, function);
				   return IDFactory.newPipeID(peerGroupID, digest );
		}
		
		
		private  final PeerID createPeerID(PeerGroupID peerGroupID
                , String clearTextID
               , String function) throws Exception {
				byte[] digest = generateHash(clearTextID, function);
				return IDFactory.newPeerID(peerGroupID, digest );
		}
		
}


