/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.p2p.msg.helper;

import java.util.HashMap;
import java.util.Map;

import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.PipeService;
import net.jxta.protocol.PipeAdvertisement;

import org.apache.commons.beanutils.BeanUtils;

import com.fourelementscapital.scheduler.p2p.MessageBean;
import com.fourelementscapital.scheduler.p2p.P2PAdvertisement;
import com.fourelementscapital.scheduler.p2p.P2PService;
import com.fourelementscapital.scheduler.p2p.listener.OutgoingMessage;
import com.fourelementscapital.scheduler.p2p.listener.OutgoingMessageCallBack;
import com.fourelementscapital.scheduler.p2p.msg.MessageHandler;
import com.fourelementscapital.scheduler.p2p.msg.MessageNames;
import com.fourelementscapital.scheduler.p2p.msg.PostMessage;

public class HelperPostMessage extends PostMessage {

	public static final String helper_prefix="_helper";
	
	public HelperPostMessage(MessageHandler mha, String recipient) {
		super(mha, recipient);		 
	}

	public HelperPostMessage(MessageHandler mha,String recipient,OutgoingMessageCallBack omc1){
		 super(mha,recipient,omc1);
	}
		

	public void send(){
		try{
					    
		   PeerGroup netPeerGroup =  P2PService.getPeerGroup();		 
		   
		   //PipeAdvertisement pipeAdv =new P2PAdvertisement().getPipeAdvertisement(super.recipient+P2PServiceServlet.helper_prefix,netPeerGroup);
		   PipeAdvertisement pipeAdv =new P2PAdvertisement().getPipeAdvertisement(super.recipient+helper_prefix,netPeerGroup);
		   
		   MessageBean response=new MessageBean();
		   
		   response.setReply(MessageBean.REPLYBACK);
		   response.setType(MessageBean.TYPE_REQUEST);   
		   //response.setCommand("NEWPROTOCOL:myTestId");
		   
		   OutgoingMessage ogM=new OutgoingMessage(super.omc,response,super.recipient);
		   HashMap h=new HashMap();
		   
		   super.mha.setMsgCreator(P2PService.getComputerName());
		   super.mha.setMsgRecipient(super.recipient);
		   Map data=BeanUtils.describe(super.mha);
		   if(super.callback){
			   data.put(MessageNames.MESSAGE_TYPE_CALLBACK, MessageNames.MESSAGE_TYPE_CALLBACK);
		   }
		   data.put(MessageNames.MESSAGE_BEAN_NAME,super.mha.getClass().getName());		  
		   //System.out.println("------->IncomingMessageParser:data:"+data);
		   log.debug("~~~~~~` Posting message:to:"+super.recipient+":data:"+data);
		   
		   ogM.setAttachment(data);		 
		   
		   PipeService pipeService = P2PService.getPipeService();
		   try{
			    //System.out.println("------->IncomingMessageParser: Sending MSG TO:"+mbean.getSender()+":"+command.toString());
				pipeService.createOutputPipe(pipeAdv,ogM);
		   }catch(Exception e){
			   	  e.printStackTrace();
			   	  PipeService pipeService1 = P2PService.getNewPipeService();
			   	  try{
			   		  pipeService1.createOutputPipe(pipeAdv,ogM);
			   	  }catch(Exception e1){			   		  
			   		  e1.printStackTrace();
			   	  }
			   	pipeService1=null;
				 
		   }

		   //cleaning up 
		   netPeerGroup =  null;
		   pipeAdv = null;
		   response=null;		   
		   omc=null;
		   ogM=null;
		   data=null;
		   pipeService = null;
	
		}catch(Exception e){
			log.error("Error:"+e.getMessage());
		}
		
	}
	
}


