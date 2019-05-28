/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.p2p.listener;

import java.util.Map;

import net.jxta.pipe.OutputPipeEvent;

import com.fourelementscapital.scheduler.p2p.MessageBean;
import com.fourelementscapital.scheduler.p2p.msg.PostMessage;
 

public abstract class OutgoingMessageCallBack {
	public static int PRIORITY_ZERO=1; 
	public static int PRIORITY_LOW=5;	
	public static int PRIORITY_NORMAL=10;
	public static int PRIORITY_HIGH=50;
	public static int PRIORITY_VERY_HIGH=100;
	public static int PRIORITY_VERY_VERY_HIGH=1000;
	
	protected String tuid=null;
	private String destination=null;
	
	private String scheduler_id;
	private Map data=null;
	private PostMessage postmessage=null;
	public OutgoingMessageCallBack(){}
	public OutgoingMessageCallBack(PostMessage pm){
		this.postmessage=pm;
	}
	public OutgoingMessageCallBack(String tuid, Map data){this.tuid=tuid;this.data=data;}
	
	protected static int PRIORITY_LOOP_FREQ=40;
	
	protected int numberOfTries=PRIORITY_ZERO;	
	public void before(OutputPipeEvent pipe){}
	public void after(OutputPipeEvent pipe){}
	public void onFail(OutputPipeEvent pipe, MessageBean mbean, String destination){}
	public boolean validateBeforeSend(){return true;}
	
	public void setPriority(int priority){
		this.numberOfTries=priority;
	}
	
	public String getTuid(){
		return this.tuid;
	}
	
	public String getDestination(){
		return this.destination;
	}
	protected void setDestination(String destination){
		this.destination=destination;
	}
	protected Map getData(){
		return this.data;
	}
	
	protected PostMessage getPostMessage(){
		return this.postmessage;
	}
}


