/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.p2p;


public class MessageBean {

	
	public  static String REPLYBACK="reply"; 
	public  static String TYPE_REQUEST="request";
	public  static String TYPE_RESPONSE="response";
	public  static String TYPE_INFO="info";
	
	
	private String sender="";
	private String type=TYPE_INFO;
	private String command="";
	private String reply="";
	
	public String getSender() {
		if(sender.equals("")){
			sender=P2PService.getComputerName();
		}
		
		return sender;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getCommand() {
		return command;
	}
	public void setCommand(String command) {
		this.command = command;
	}
	public String getReply() {
		return reply;
	}
	public void setReply(String reply) {
		this.reply = reply;
	}
	
	
	
	
	
}


