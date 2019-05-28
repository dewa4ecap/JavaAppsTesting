/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.p2p.msg;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fourelementscapital.scheduler.p2p.listener.OutgoingMessageCallBack;

public abstract class MessageHandler {

	//private Properties
	//public MessageHandlerAbstract
	
	private String msgCreator;
	private String msgRecipient;
	

	private String responseJSON;

	public String getMsgCreator() {
		return msgCreator;
	}





	public void setMsgCreator(String msgCreator) {
		this.msgCreator = msgCreator;
	}





	public String getMsgRecipient() {
		return msgRecipient;
	}



	public void setMsgRecipient(String msgRecipient) {
		this.msgRecipient = msgRecipient;
	}



	public String getResponseJSON() {
		return responseJSON;
	}


	public void setResponseJSON(String responseJSON) {
		this.responseJSON = responseJSON;
	}


	public void setPriority(int priority) {
		this.priority = priority;
	}
	
	//private String messageFrom=null;
	

	 

	private int priority=OutgoingMessageCallBack.PRIORITY_NORMAL;
	
	//private Logger log = LogManager.getLogger(MessageHandler.class.getName());
	
	//public abstract MessageHandlerAbstract getThisMessageHandler();
	public abstract Map executeAtDestination();
	
	 
	
	public int getPriority() {
		return this.priority ;
	}
	
	
	public abstract void onSendingFailed();
	
	
}



