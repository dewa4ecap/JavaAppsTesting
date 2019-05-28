/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.p2p;

import java.io.File;
import java.net.InetAddress;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fourelementscapital.scheduler.config.Config;

import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.discovery.DiscoveryService;
import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.PipeService;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.platform.NetworkManager;
import net.jxta.protocol.DiscoveryResponseMsg;
import net.jxta.protocol.PeerAdvertisement;
import net.jxta.protocol.PipeAdvertisement;

public class P2PService extends Thread implements DiscoveryListener {

    
	public final static String MESSAGE_NAME_SPACE = "PipeTaskMsg";
	private static transient NetworkManager manager=null;
	protected static HashMap machineList=new HashMap();
	//protected static Vector messages=new Vector();
	public static Vector messages=new Vector();
	//private static long discoveryWaitTime=1000L;
	
    private static transient DiscoveryService discovery=null;
	private static PipeService pipeService=null;    
    //private static InputPipe inputPipe = null;  
    //private static PipeAdvertisement pipeAdv;
    private static PeerGroup netPeerGroup=null;
    private static Date startedTime=null;
	//private static PeerAdvertisement peerAdv=null;
	
	private Logger log=LogManager.getLogger(P2PService.class.getName());
	
	
	private static String defaultP2PGroup="DiscoveryServer"; 
	private static String defaultP2PCache=""; //  addition because there is a possibility that .cache folder cause an error when restarting .. bug when restarting (20140411)
	
	public static void setCache(){
		// Addition because there is a possibility that .cache folder cause an error when restarting .. bug when restarting (20140411)
		 if(Config.getValue("p2p.cachepath")!=null && !Config.getValue("p2p.cachepath").equals("") ){
			 defaultP2PCache=Config.getValue("p2p.cachepath")+".cache";
		 } else{
			 defaultP2PCache=".cache";
		 }
	}
	public static void removeCache(){
		 setCache();
		 String cachePath=defaultP2PCache;
		 File tempFile = new File(cachePath);
		 try {
			 if(tempFile.isDirectory()){
				 FileUtils.deleteDirectory(tempFile);
				 System.out.println("removing .Cache Directory: " + cachePath);
			 } else {
				 System.out.println(".Cache Directory Does Not Exits");
			 }				 
		 } catch (Exception e) {
	           // e.printStackTrace();
	           // System.out.println("P2PService.Removing .cache :Error::"+e.getMessage());			           
	      }
	}
	
	public static NetworkManager getManager(){
		
		Logger log = LogManager.getLogger(P2PService.class.getName());
		
		if(P2PService.manager==null){
			 try {
				 if(Config.getValue("p2p.groupname")!=null && !Config.getValue("p2p.groupname").equals("") ){
					 defaultP2PGroup= Config.getValue("p2p.groupname");
				 }
				 String manname=defaultP2PGroup+getComputerName();	
				 setCache();				 
				 String cachePath=defaultP2PCache;
				 System.out.println("set .Cache Directory: " + cachePath);
				 P2PService.manager = new NetworkManager(NetworkManager.ConfigMode.ADHOC, manname, new File(new File(cachePath), manname).toURI());
				 
				 if(Config.getValue(Config.P2P_NO_MULTICAST)!=null 
						 && Config.getValue(Config.P2P_NO_MULTICAST).equalsIgnoreCase("true")){
					 
					 	NetworkConfigurator configurator=P2PService.manager.getConfigurator();
					 	
					 	try{
					 		configurator.setUseMulticast(false);
					 		
					 		configurator.setHttpEnabled(true);
					 		configurator.setHttpIncoming(true);
					 		//configurator.setTcpEnabled(true);
					 		//configurator.setTcpIncoming(true);
					 		
					 		log.info("Disabling IP Multicast");
					 	}catch(Exception e){
					 		log.error("Error while configuring network, e:"+e.getMessage());
					 	}
				 }
				 
				//Logger log= Logger.getLogger(P2PService.class);
				 
				PeerGroup pg= P2PService.manager.startNetwork();
				
				log.debug("pg: group_name:"+pg.getPeerGroupName());
				log.debug("pg: peer_peer:"+pg.getPeerName());
				log.debug("pg: peer name:"+pg.getPeerAdvertisement().getName());
				//log.debug("pg: pipe advertisment:"+));
				log.debug("pg: getPeerID unique value:"+pg.getPeerID().getUniqueValue());
				
				
				
				
		       } catch (Exception e) {
		            e.printStackTrace();
		            System.out.println("----------------------- P2PService.getManager() :Error::"+e.getMessage());
		            //System.exit(-1);
		      }
		}
		return P2PService.manager;
	}
	
	public static void stopNetwork(){
		if(P2PService.manager!=null){
			 try {
				 P2PService.manager.stopNetwork();
				 P2PService.discovery=null;
				 P2PService.netPeerGroup=null;				 
				 P2PService.manager=null;
		      } catch (Exception e) {
		    	  e.printStackTrace();
		      }
		}

	}
	
	
	
	
	public static PeerGroup getPeerGroup(){
		if(netPeerGroup==null){
			 netPeerGroup = getManager().getNetPeerGroup();
		}
		return netPeerGroup;
	}
	
	public static DiscoveryService getDiscoveryService() {
			if(discovery==null){
				discovery = getPeerGroup().getDiscoveryService();
			}
		return discovery;
		
	}
	public static PipeService getPipeService() {
		if(pipeService==null){
			pipeService = getPeerGroup().getPipeService();
		}
		return pipeService;
		
	}

	public static PipeService getNewPipeService() {		
			pipeService = getPeerGroup().getPipeService();
			return pipeService;
	}
	
	/*
	public static PeerAdvertisement getPeerAdvertisement() {
		
		if(peerAdv==null){
			 P2PAdvertisement ad=new P2PAdvertisement();
			 try{
				 peerAdv = ad.getPeerAdvertisement(getComputerName(),getPeerGroup());
			 }catch(Exception e){
				 e.printStackTrace();
			 }
		}
		return peerAdv;
		
	}
	*/
	/*
	
	public void run()  {
		try{
			netPeerGroup = getManager().getNetPeerGroup();
		        // get the discovery service
			discovery = netPeerGroup.getDiscoveryService();
			pipeService = netPeerGroup.getPipeService();
		        
			long waittime =  10 * 1000L;
			 
			String computername=getComputerName();
			
			P2PAdvertisement ad=new P2PAdvertisement();
	     	
	     	PeerAdvertisement mdadv = ad.getPeerAdvertisement(computername,netPeerGroup);
	     	discovery.publish(mdadv,DiscoveryService.INFINITE_LIFETIME,DiscoveryService.INFINITE_LIFETIME);
	        discovery.remotePublish(mdadv,DiscoveryService.INFINITE_LIFETIME);
	         
	        pipeAdv = ad.getPipeAdvertisement(computername,netPeerGroup);
	        inputPipe = pipeService.createInputPipe(pipeAdv, new IncomingMessage(null) );
	        log.debug("----------->publishing client node:"+computername);
	     	 
	     	 //while (true) {  
	           //     try {
	            //       // System.out.println("Sleeping for :" + waittime);
	             //       Thread.sleep(waittime);
	             //     log.debug("thread sleeping "+waittime);
	             //   } catch (Exception e) {// ignored
	              //  }
	     	 //}
	     	  
		}catch(Exception e){
			log.error("p2p thread couldn't be started, task scheduler load balancing might not work in this computer");
			
		}
	}
	*/
	 
 
	
	public static Date getPeerStartedTime(){
		return startedTime;
	}
	
	//protected static void setPeerStarted(){
	public static void setPeerStarted(){
		startedTime=new Date();
	}
	
	
	
	/**
	 * @deprecated
	 * @param clientname
	 * @param message
	 * @throws Exception
	 */
	public static void sendMessage(String clientname, String message) throws Exception {
		try{
			manager.getNetPeerGroup();
			PipeAdvertisement pipeAdv = new P2PAdvertisement().getPipeAdvertisement(clientname,netPeerGroup);
			
			//OutgoingMessageCompose omc=new OutgoingMessageCompose(){
			//};		
			//OutgoingMessage ogM=new OutgoingMessage(null,message);
			//pipeService = P2PService.getPipeService();			 
			//pipeService.createOutputPipe(pipeAdv,ogM);
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	 
	
	private static String THIS_HOST_NAME=null;
	public synchronized static  String getComputerName() {
		String computername="[unknown]";
        try{
        	if(P2PService.THIS_HOST_NAME==null){
        		computername=InetAddress.getLocalHost().getHostName();
        		P2PService.THIS_HOST_NAME=computername;
        	}else{
        		computername=P2PService.THIS_HOST_NAME;
        	}
        	
         }catch(Exception e){        	
        	e.printStackTrace();
        }
        return computername;
	}
	
	
	public Map getClientList() {		
		
		DiscoveryService discovery = P2PService.getDiscoveryService();       
		
		
        new Thread("Adv poll") {
        	
            public void run() {
            	
               int sleepy=3000;              
               DiscoveryService discovery = P2PService.getDiscoveryService();
               discovery.addDiscoveryListener(new P2PService());
               P2PService.getDiscoveryService().getRemoteAdvertisements(null,DiscoveryService.PEER,null,null,1);
               log.debug("Adv poll created");
               try {
                      sleep(sleepy);
               }
                catch(InterruptedException e) {}
            }
        }.start();

		
        try {
             sleep(1000);
        } catch(InterruptedException e) {}
		return machineList;
	}
	

	public static Map getClientListFromMemory() {
	 
		return machineList;
	}

	 
    public void discoveryEvent(DiscoveryEvent ev) {
    	
    	if(Config.getValue("p2p.groupname")!=null && !Config.getValue("p2p.groupname").equals("") ){
			 defaultP2PGroup= Config.getValue("p2p.groupname");
		 }
    	
        DiscoveryResponseMsg res = ev.getResponse();
        PeerAdvertisement adv;
        Enumeration en = ev.getSearchResults();
        log.debug("discoveryEvent() called");
        if (en != null) {
            while (en.hasMoreElements()) {
            	 adv = (PeerAdvertisement) en.nextElement();  
                log.debug(" adv.getName(): "+adv.getName());
                
                if(adv.getName().startsWith(defaultP2PGroup)){
                	//String s = adv.getName().replace("DiscoveryServer","");
                	//PipeAdvertisement av=new P2PAdvertisement().getPipeAdvertisement(s,P2PService.getManager().getNetPeerGroup());
                	//P2PService.machineList.put(av.getPipeID(),s);
                }else{        
                	PipeAdvertisement av=new P2PAdvertisement().getPipeAdvertisement(adv.getName(),P2PService.getManager().getNetPeerGroup());
                	P2PService.machineList.put(av.getPipeID(),adv.getName());
                }
            }
        }
        //System.out.println("Module specific id :"+res.getPeerAdvertisement());
        
    }
	
}


