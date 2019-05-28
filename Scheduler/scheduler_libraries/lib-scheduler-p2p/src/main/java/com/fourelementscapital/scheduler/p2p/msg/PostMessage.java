/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.p2p.msg;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fourelementscapital.scheduler.p2p.MessageBean;
import com.fourelementscapital.scheduler.p2p.P2PAdvertisement;
import com.fourelementscapital.scheduler.p2p.P2PService;
import com.fourelementscapital.scheduler.p2p.listener.OutgoingMessage;
import com.fourelementscapital.scheduler.p2p.listener.OutgoingMessageCallBack;

import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.OutputPipeEvent;
import net.jxta.pipe.PipeService;
import net.jxta.protocol.PipeAdvertisement;

public class PostMessage {

	//private String sender;
	//private String message;
	protected MessageHandler mha;
	protected String recipient=null;
	
	protected Logger log = LogManager.getLogger(PostMessage.class.getName());
	
	protected boolean callback=false;
	protected OutgoingMessageCallBack omc=null;
	
	public PostMessage(MessageHandler mha,String recipient){
		this.mha=mha;
		this.recipient=recipient;
		
		this.omc=new OutgoingMessageCallBack(this){
			@Override
			public void onFail(OutputPipeEvent pipe, MessageBean mbean,String destination) {
				if(getPostMessage().callback && getPostMessage().mha instanceof PostCallBack){
					((PostCallBack)(getPostMessage().mha)).onCallBackSendingFailed();
				}else{
					getPostMessage().mha.onSendingFailed();
				}
			}
		};
		omc.setPriority(this.mha.getPriority());
	}
	
	public PostMessage(MessageHandler mha,String recipient,OutgoingMessageCallBack omc1){
		this.mha=mha;
		this.recipient=recipient;
		this.omc=omc1;
	}
	
	
	public void setCallBack(){
		this.callback=true;
	}
	
	
	protected boolean getHelperAdvertKey() {
		return false;
	}
	
	
	
	
	public void send(){
		try{
					    
		   PeerGroup netPeerGroup =  P2PService.getPeerGroup();
		   
		   /*
		   PipeAdvertisement pipeAdv = null;
		   if(!getHelperAdvertKey()) {
			   pipeAdv= new P2PAdvertisement().getPipeAdvertisement(this.recipient,netPeerGroup);
		   }else{
			   log.debug("~~~~~~` Posting message: PeerHelper");
			   pipeAdv=new P2PAdvertisement(getHelperAdvertKey()).getPipeAdvertisement(this.recipient,netPeerGroup); 
		   }
		   */
		   
		   PipeAdvertisement pipeAdv =new P2PAdvertisement().getPipeAdvertisement(this.recipient,netPeerGroup);
		   MessageBean response=new MessageBean();
		   
		   response.setReply(MessageBean.REPLYBACK);
		   response.setType(MessageBean.TYPE_REQUEST);   
		   //response.setCommand("NEWPROTOCOL:myTestId");
		   
		   OutgoingMessage ogM=new OutgoingMessage(this.omc,response,this.recipient);
		   HashMap h=new HashMap();
		   
		   this.mha.setMsgCreator(P2PService.getComputerName());
		   this.mha.setMsgRecipient(this.recipient);
		   Map data=BeanUtils.describe(this.mha);
		   if(this.callback){
			   data.put(MessageNames.MESSAGE_TYPE_CALLBACK, MessageNames.MESSAGE_TYPE_CALLBACK);
		   }
		   data.put(MessageNames.MESSAGE_BEAN_NAME,this.mha.getClass().getName());		  
		   //System.out.println("------->IncomingMessageParser:data:"+data);
		   
		   
			HashMap debugd=new HashMap();
			debugd.putAll(data);
			debugd.remove("msgCreator");
			debugd.remove("responseJSON");
			debugd.remove("priority");
			debugd.remove("class");
			debugd.remove("__msgBeanName__");
			debugd.remove("taskuid");
			
		   
		   log.debug("~~~~~~ PostMessage:class:"+this.mha.getClass().getName()+" to:"+this.recipient+":data:"+debugd);
		   
		   ogM.setAttachment(data);
		   //ogM.setAttachment(MessageNames.RSCRIPT,"asdf aghjasdfjasdfasd g;adfajsd gasl;sgjasd gjasdg lasdg jasdlgjasdg;l asdjglasd gjsdlagjsdgasdjg;sdagsdgasdg");
		   
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


