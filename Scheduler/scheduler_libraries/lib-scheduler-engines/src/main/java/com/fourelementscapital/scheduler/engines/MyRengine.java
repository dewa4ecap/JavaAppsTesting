/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.engines;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rosuda.JRI.RMainLoopCallbacks;
import org.rosuda.JRI.Rengine;

public class MyRengine extends Rengine{
	
	private StackFrame  sframe=null;
	Logger log = LogManager.getLogger(MyRengine.class.getName());
	
	private RMainLoopCallbacks mlc=null;
	
	public MyRengine(String[] args, boolean runMainLoop, RMainLoopCallbacks initialCallbacks){
		super(args,runMainLoop,initialCallbacks);
		this.mlc=initialCallbacks;
	}
	
	  
	public void setStackFrame(StackFrame sframe){
		  log.debug("setting stack frame:"+sframe);
		  this.sframe=sframe;
	}

	public StackFrame getStackFrame(){
		  return  this.sframe;
	}
	
	public MyRengineCallbacks getRMailLoopCallback(){
		return (MyRengineCallbacks)this.mlc;
	}
	
	public void jriWriteConsole(String text, String text1){
		 
		log.debug("jriWriteConsole"+text+" text2:"+text1);
	}
	public void jriShowMessage(String message){
		log.debug("jriShowMessage"+message);
	}
}


