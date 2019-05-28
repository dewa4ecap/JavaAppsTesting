/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.p2p.listener;

import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.commons.digester.Digester;
import org.apache.jcs.JCS;
import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fourelementscapital.scheduler.p2p.MessageBean;
import com.fourelementscapital.scheduler.p2p.msg.MessageNames;
import com.fourelementscapital.scheduler.p2p.msg.ReceiveMessage;

import net.jxta.endpoint.Message;
import net.jxta.endpoint.MessageElement;
import net.jxta.pipe.PipeMsgEvent;
import net.jxta.pipe.PipeMsgListener;

public class IncomingMessage implements PipeMsgListener  {

	private IncomingMessageCallBack imi=null;
	 
	public IncomingMessage(IncomingMessageCallBack imi ){
		 this.imi=imi;		 
	}
	
	private Logger log = LogManager.getLogger(IncomingMessage.class.getName());
	
	
	public final static String MESSAGE_NAME_SPACE = "PipeTaskMsg"; 
	//protected static Vector<MessageBean> messages=new Vector<MessageBean>();
	public static Vector<MessageBean> messages=new Vector<MessageBean>();
	
	//protected static TreeMap<String,String> cachedPeers=new TreeMap<String,String>();
	private static JCS cachedPeers=null;
	
	protected static Map<String,Integer> executingpeers=Collections.synchronizedMap(new TreeMap<String,Integer>());
	
	protected static Map<String,Map> executingpeerstime=Collections.synchronizedMap(new TreeMap<String,Map>());
	
	protected static TreeMap<String,String> cachedStatistics=new TreeMap<String,String>();
	protected static TreeMap<String,String> cachedRPackages=new TreeMap<String,String>();
	
	protected static TreeMap<String,String> cachedPeerQueueStat=new TreeMap<String,String>();
	
	
	
 
	

	/*
	
	private static ExecutorService incomingService= Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	public void pipeMsgEvent(PipeMsgEvent event) {
		
		
		Future fu=incomingService.submit(
		new Callable<String>() {
				public String call(){
					//return new Integer(new LoadBalancingHSQLQueue().startedIfNotStarted1(sc_id,tri_time,peer));
					IncomingMessage im=new IncomingMessage(this.imi);
					im.pipeMsgEvent2(this.event);
					return "";
				}
				private IncomingMessageCallBack imi=null;
				private PipeMsgEvent event=null;
				public Callable<String> init(IncomingMessageCallBack imi,PipeMsgEvent event){					 
					this.imi=imi;
					this.event=event;
					return this;
				}
		}.init(this.imi, event));
		
		 
	}
	*/
	
	
	public void pipeMsgEvent(PipeMsgEvent event) {
		
		   log.debug("message received..."); 
	       try{	
			   if(this.imi!=null){this.imi.before(event);}
			   Message msg;
		       try {
		    	   
		            msg = event.getMessage();
		            if (msg == null) {
		                log.error("message is null");
		                return;
		            }
		            
		       } catch (Exception e) {
		            e.printStackTrace();
		            return;
		       }
	
		        // get all the message elements
		       Message.ElementIterator en = msg.getMessageElements();		       
		       if (!en.hasNext()) {
		    	    log.error("en has no elements");
		            return;
		       } 
		        // get the message element in the name space PipeClient.MESSAGE_NAME_SPACE
		       MessageElement msgElement = msg.getMessageElement(null, MESSAGE_NAME_SPACE);	    
		       MessageBean  mb=null;
		       String xmsg=msgElement.toString();
		    	   
	    
		       try{
		    	   mb= parseMessage(xmsg);
		    	   log.debug("message from :"+mb.getSender()+" msg:"+mb.getCommand());
		       }catch(Exception e){
		    	   //e.printStackTrace();
		    	   log.error("Error 1:"+e.getMessage());
		       }
		    
			
	
		       if(this.imi!=null){this.imi.after(event);}
		       if(mb!=null){
		    	   
			       HashMap attachments=new HashMap();
			       
		    	   while(en.hasNext()){
		    		   MessageElement me=(MessageElement)en.next();
		    		   if(!me.getElementName().equals(MESSAGE_NAME_SPACE)){
		    			   //attachmentName=me.getElementName();
		    			   //attachment=me.toString();
		    			   attachments.put(me.getElementName(),me.toString());
		    		   }
		    	   }
		    	   
		    	   //new P2PPipeLog().logIncoming(mb);	
		    	   try{
		    		   
		    		   if(attachments.get(MessageNames.MESSAGE_BEAN_NAME)!=null ){
		    			   new ReceiveMessage(attachments).process();
		    		   }else{
/*		    			   
		    			   IncomingMessageParser imp= new IncomingMessageParser(mb,msgElement);
		    			   imp.setAttachement(attachments);
		    			   imp.parse();
*/		    			   
		    		   }
		    	   }catch(Exception e){
		    		   
		    		   log.error("Error while msg:"+msgElement.toString()+" Error:"+e.getMessage());
		    	   }
		       }
		       
		       
		       
	       }catch(Exception e){
	    	 //  log.error("Error Msg:"+e.getMessage());
	    	   log.error("Error while parsing or getting data:"+e.getMessage());
	    	   e.printStackTrace();
	       }
	}
	
	public static Vector<MessageBean> getMessages(){
		return messages;
	}
	
	private MessageBean parseMessage(String msg) throws Exception  {
		
		  Digester digester = new Digester();
		  digester.setValidating(false);
		 		  
		  digester.addObjectCreate("message", MessageBean.class);
		  digester.addBeanPropertySetter("message/type");
		  digester.addBeanPropertySetter("message/command");
		  digester.addBeanPropertySetter("message/sender");
		  digester.addBeanPropertySetter("message/reply");

		  MessageBean ve= (MessageBean) digester.parse(new StringReader(msg));
          return ve;
		
	}
	
	//0 to delete
	//-1 delete all
	//
	public static int ACTION_PEER_ADD=1;
	public static int ACTION_PEER_REMOVE=0;
	public static int ACTION_PEER_REMOVEALL=-1;
	
	/*
	public synchronized static void peersUpdate(int action, String peer,String status){
		if(action==ACTION_PEER_REMOVE && cachedPeers.containsKey(peer)) cachedPeers.remove(peer);
		if(action==ACTION_PEER_REMOVEALL) cachedPeers.clear();
		if(action==ACTION_PEER_ADD) cachedPeers.put(peer,status);
	}
	*/
	
	public synchronized static void peersUpdate(String peer,String status){
		try{
			if(cachedPeers==null){				
				cachedPeers=JCS.getInstance("cachedPeers$$");
			}
			IElementAttributes att= cachedPeers.getDefaultElementAttributes();
			att.setMaxLifeSeconds(4);
			cachedPeers.put(peer, status,att);
			
		}catch(Exception e){
			
		}
		//if(action==ACTION_PEER_REMOVE && cachedPeers.containsKey(peer)) cachedPeers.remove(peer);
		//if(action==ACTION_PEER_REMOVEALL) cachedPeers.clear();
		//if(action==ACTION_PEER_ADD) cachedPeers.put(peer,status);
		
	}
	
	
	public synchronized static void updateExecutingPeers(String peer,String status, int scheduler_id) {
		if(status.equalsIgnoreCase("BUSY")&& scheduler_id>0){
			executingpeers.put(peer, scheduler_id);
		}
		if(status.equalsIgnoreCase("NOBUSY")){
			executingpeers.remove(peer);
		}
	}
	
	/**
	 * @deprecated
	 * @param peer
	 * @param status
	 * @param scheduler_id
	 * @param tr_time
	 * this method should be removed eventually once all the HSQL supported peer
	 */
	public synchronized static void updateExecutingPeersTime(String peer,String status, int scheduler_id,long tr_time) {
		if(status.equalsIgnoreCase("BUSY")&& scheduler_id>0){
			Map tm;
			if(executingpeerstime.get(peer)==null){
				tm=Collections.synchronizedMap(new TreeMap());				
			}else{
				tm=(Map)executingpeerstime.get(peer);
			}
			if(tm!=null){
				tm.put(scheduler_id, tr_time);
				executingpeerstime.put(peer, tm);
			}
		}
		if(status.equalsIgnoreCase("NOBUSY")){
			executingpeerstime.remove(peer);
		}
			
	}
	
	/**
	 * @deprecated
	 * @param peer
	 * @param scheduler_id
	 * @param trigger_timg
	 * this method should be removed eventually once all the HSQL supported peer 
	 */
	
	public synchronized static void updateFinishedPeersTime(String peer,  int scheduler_id, long trigger_timg) {
		if(scheduler_id>0){			
			Map tm;			
			if(executingpeerstime.get(peer)!=null){				
				tm=(Map)executingpeerstime.get(peer);
				if(tm!=null && tm.get(scheduler_id)!=null && ((Long)tm.get(scheduler_id)).longValue()==trigger_timg || trigger_timg==0){
					if(tm!=null){
						tm.remove(scheduler_id);
					}
				}
				if(tm!=null && tm.keySet().size()==0){
					executingpeerstime.remove(peer);
				}
			}
		}
	}
	
	
	public synchronized static void updatePeerStatistics(String peer,String status) {		
		cachedStatistics.put(peer, status);		
	}

	public synchronized static Map getPeerStatistics() {		
		return cachedStatistics;		
	}


	public synchronized static void updatePeerQueueStat(String peer,String status) {		
		cachedPeerQueueStat.put(peer, status);		
	}

	public synchronized static Map getPeerQueueStat() {		
		return cachedPeerQueueStat;		
	}

	
	
	
	
	public synchronized static void updatePeerRPackages(String peer,String pkgs) {		
		cachedRPackages.put(peer, pkgs);		
	}

	public synchronized static Map getPeerRPackages() {		
		return cachedRPackages;		
	}

	
	public static Map<String,Integer> getExecutingPeers(){
		return executingpeers;
	}

	public static synchronized Map<String,Map> getExecutingPeersTime(){
		return executingpeerstime;
	}
	
	public static HashMap getCachedPeers(){
		
		HashMap rtn=null;
		if(cachedPeers!=null){
			rtn=cachedPeers.getMatching("^[A-Za-z0-9]+$");
		}
		return  (rtn!=null)?rtn:new HashMap(); //all alpha numeric keys.
		
	}
	
	
	
	private static JCS cachedPeerResp4Task=null;
	/**
	 * This method is to store the executing task in cache that expires  
	 * after certain period, so that scheduler kicks out the task from the queue, after 5 minutes of non-response of the task of the peer.
	 * @param scheduler_id
	 * @param trigger_time
	 */
	public synchronized static void peerRespRecencyOnTask(int scheduler_id, long trigger_time){
		try{
			if(cachedPeerResp4Task==null){				
				cachedPeerResp4Task=JCS.getInstance("cachedPeerResp4Task$$$");
			}
			IElementAttributes att= cachedPeerResp4Task.getDefaultElementAttributes();
			att.setMaxLifeSeconds(180); //3 minutes
			String value=scheduler_id+"_"+trigger_time;
			//if(cachedPeerResp4Task.get(value)!=null)  cachedPeerResp4Task.remove(value);
			cachedPeerResp4Task.put(value,"true",att);
			
		    
		}catch(Exception e){
			Logger log = LogManager.getLogger(IncomingMessage.class.getName());
			
			log.error("Error:"+e.getMessage());
		}
	 
		
	}
	/**
	 * this method to check wheather the executing task is responded by the peer
	 * to kick out the task from the queue, after 5 minutes of non-response of the task of the peer.
	 * @param scheduler_id
	 * @param trigger_time
	 * @return
	 */
	
	public synchronized static boolean isRespRecencyOnTask(int scheduler_id, long trigger_time){
		
			String value=scheduler_id+"_"+trigger_time;
			if(cachedPeerResp4Task!=null && cachedPeerResp4Task.get(value)!=null){				
				return true;
			}
			return false;
	}
	
	
	
}



