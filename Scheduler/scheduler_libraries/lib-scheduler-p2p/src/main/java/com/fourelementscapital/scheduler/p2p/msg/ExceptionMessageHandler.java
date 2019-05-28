/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.p2p.msg;

import java.lang.reflect.Constructor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fourelementscapital.scheduler.exception.SchedulerException;

 

 

public abstract class ExceptionMessageHandler extends MessageHandler {

	private Logger log = LogManager.getLogger(ExceptionMessageHandler.class.getName());
	
	private String exceptionMessage=null;
	private String exceptionClass=null;
	private SchedulerException ex=null;
	
	public void exception(SchedulerException se){
		if(se!=null){
			this.exceptionClass=se.getClass().getName();
			this.exceptionMessage=se.getMessage();
		}
	}
	
	public SchedulerException exception(){
		//SchedulerException se=null;
		if(this.ex==null && this.exceptionClass!=null){
			
			try{
				Class c=Class.forName(this.exceptionClass);				
				Constructor ct = c.getConstructor(String.class);
				this.ex=(SchedulerException)ct.newInstance(this.exceptionMessage);
			}catch(Exception e){				
				log.error("Exception class:"+this.exceptionClass+",  error message:"+e.getMessage());
			}
			
		} 
		return this.ex; 
		
	}

	
	
	
	
	
	public String getExceptionMessage() {
		return exceptionMessage;
	}
	public void setExceptionMessage(String exceptionMessage) {
		this.exceptionMessage = exceptionMessage;
	}
	public String getExceptionClass() {
		return exceptionClass;
	}
	public void setExceptionClass(String exceptionClass) {
		this.exceptionClass = exceptionClass;
	}
	
	
	
	
	
	
	
}



