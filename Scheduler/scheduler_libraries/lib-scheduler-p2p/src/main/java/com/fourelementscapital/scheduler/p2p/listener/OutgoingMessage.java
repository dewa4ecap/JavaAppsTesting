/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.p2p.listener;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fourelementscapital.scheduler.p2p.MessageBean;
import com.fourelementscapital.scheduler.p2p.msg.CommunicationTrace;

import net.jxta.document.Advertisement;
import net.jxta.endpoint.Message;
import net.jxta.endpoint.StringMessageElement;
import net.jxta.pipe.OutputPipe;
import net.jxta.pipe.OutputPipeEvent;
import net.jxta.pipe.OutputPipeListener;

public class OutgoingMessage implements OutputPipeListener{
	
	 private static int maxDebugErrCounter=0;
	 private OutgoingMessageCallBack omc=null;
	 public final static String MESSAGE_NAME_SPACE = "PipeTaskMsg"; 
 
	 private MessageBean smsg;
	 
	 private Logger log = LogManager.getLogger(OutgoingMessage.class.getName());
	 
	 private String destination=null;
	 private boolean messageSent=false;
 
	 
	 private Map<String,String> attachments=null;
	 //private String attachmentName=null;
	 
	 public OutgoingMessage(OutgoingMessageCallBack omc, MessageBean msg, String destination){
		 this.omc=omc;
		 this.smsg=msg;		
		 this.destination=destination;		 
		 if(this.omc!=null){
			 this.omc.setDestination(destination);
		 }
	 }
	
	 public void setAttachment(Map<String,String> attachments) {
		 this.attachments=attachments;
	 }
	 
	 public void outputPipeEvent(OutputPipeEvent event) {
             
	    	//log.debug("message sent...cmd:"+this.smsg.getCommand());
	        OutputPipe outputPipe = event.getOutputPipe();
	        
	         
	        
	        if(this.omc!=null){this.omc.before(event);}
	        Message msg;
	        
	        try {
	        	
	        	Advertisement adv=outputPipe.getAdvertisement();
	        	
	        	//log.debug("message sent...cmd:"+this.smsg.getCommand()+", adv:type:");
	        	
	            msg = new Message();
	            StringMessageElement sme = new StringMessageElement(MESSAGE_NAME_SPACE, getXML(this.smsg), null);
	            msg.addMessageElement(null, sme);
	            if(attachments!=null){
	            	for(Iterator<String> it=attachments.keySet().iterator();it.hasNext();){
	            		String ky=it.next(); 
	            		String val=attachments.get(ky);
	            		if(val!=null){
	            			StringMessageElement sme1=new StringMessageElement(ky,val,null);
	            			msg.addMessageElement(null, sme1);
	            		}
	            	}
	            	//System.out.println("~~~~~~~~~~~~OutgoingMessage.attachment:"+msg.toString());
	            }	            
	            //boolean msgsent=false;
	            //tries 5 times, maximum waits upto 400 //nano seconds
	            int numofTries=(omc!=null)?omc.numberOfTries:OutgoingMessageCallBack.PRIORITY_ZERO;
	            Date start=new Date();
	            
	            for(int i=0;(i<numofTries && !this.messageSent);i++){
	            	
	            	boolean taskvalided=true;
	            	if(this.omc!=null && !this.omc.validateBeforeSend()){
	            		taskvalided=false;
	            	}
	            	if(this.omc==null || taskvalided){	            		
	            		this.messageSent=outputPipe.send(msg);	            		
	            	}else{	            		
	            		i=numofTries;	            		
	            		//exit the loop;
	            	}
	            	log.debug("Msg:sent to client:getMessageElements:"+msg.getMessageElements().toString()+" this.messageSent:"+this.messageSent);
	            	if(!this.messageSent && taskvalided){
	            		try{
	            			//log.error("Output pipe of "+this.destination+" is not responding waiting "+40*i+" ms" +" ~~Msg:"+getXML(this.smsg));	            			 
	            			Thread.sleep(50);	            			
	            		}catch(Exception e){}
	            	}
	            }
	            long diff=new Date().getTime()-start.getTime();
	            
	            if(!this.messageSent && OutgoingMessage.maxDebugErrCounter<1000){
	            	//display only 1000 errors, this will be reset on restarting the server/peer to avoid too many messsages in error log.
	            	log.error("Message couldn't be sent to peer "+this.destination+" Tried:"+numofTries+"times Took:"+diff+"ms Msg:"+getXML(this.smsg));	   
	            	OutgoingMessage.maxDebugErrCounter++;
	            }
	            
            	if(this.messageSent && attachments!=null){
        			new CommunicationTrace(this.destination).outgoing(this.attachments);
        		}

	            	            
	            	            
	            //log.debug("Msg:"+getXML(this.smsg));
	            if(this.smsg!=null){
	 	    	   //new P2PPipeLog().logOutgoing(this.smsg,this.destination);
	 	        }
	        } catch (IOException e) {
	        	e.printStackTrace();
	            //System.exit(-1);
	        	//ClientErrorMgmt.reportError(e, "To Peer:"+ this.destination+" command:"+this.smsg.getCommand());
	        }finally{
	        	if(this.omc!=null){this.omc.after(event);}
	        	if(this.omc!=null && !this.messageSent){this.omc.onFail(event,this.smsg,this.destination);}
	        }
	        //msg=null;
	        //outputPipe=null;
	 }

	 
	 
	 private String getXML(MessageBean msg){
		   String rtn="<?xml version=\"1.0\"?><message>";		   
		   rtn+="<sender>"+msg.getSender()+"</sender>";
		   rtn+="<reply>"+msg.getReply()+"</reply>";
		   rtn+="<type>"+msg.getType()+"</type>";
		   rtn+="<command>"+msg.getCommand()+"</command>";
		   rtn+="</message>";		   
		   return rtn;
	 }
}


