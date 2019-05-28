/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.rscript;

import java.nio.CharBuffer;
import java.util.Date;

import org.apache.catalina.websocket.MessageInbound;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RScriptAsyncWebsocket implements RScriptListener{

	Logger log = LogManager.getLogger(RScriptAsyncWebsocket.class.getName());
	
	private MessageInbound messageinbound;
	private String uniqueid;
	private Date started=null;
	
	public RScriptAsyncWebsocket(String uid,MessageInbound mib){
		this.uniqueid=uid;
		this.messageinbound=mib;
		
	}
	
	@Override
	public void onScriptSent(RScript rscript, String peer) throws Exception {
		 
		this.started=new Date();
	}

	@Override
	public void onScriptFinished(RScript rscript, String peer, String result,
			String status) throws Exception {
		
		log.debug("script :"+rscript.getUid()+" finished,  thread:"+Thread.currentThread().getName() );
		
		
		
		String logmessage="";		
 		
		String st_time=this.started!=null ? this.started.getTime()+"":"";
	
		logmessage+=status+"\t"+rscript.getPeer()+"\t"+rscript.getQueued_time()+"\t"+st_time+"\t"+new Date().getTime()+"\t"+rscript.getRequesthost()+"\t"+rscript.getUniquename();		
		//log.debug("~~~~~~ Script started....rscript.getError():"+rscript.getError());		
		if(status.equalsIgnoreCase("fail") && rscript.getError()!=null){			
			logmessage+="\tError:"+RScriptAsyncListenerImpl.readFirst(rscript.getError(), 100);
		}
		RScriptAsyncListenerImpl.log(logmessage);		
		long duration=new Date().getTime()-this.started.getTime();		
		String rtn=this.uniqueid+"~~"+result;
		try{
			this.messageinbound.getWsOutbound().writeTextMessage(CharBuffer.wrap(rtn));
			this.messageinbound.getWsOutbound().flush();
		}catch(Exception e){
			log.error("Error, :onScriptFinished:"+e.getMessage());
		}

		

		/*
		
		Future fu=IOExcecutors.threadpool.submit(
				
    			new Callable<String>() {
    					public String call(){	
    						String logmessage="";		
    				 		
    						String st_time=this.started!=null ? this.started.getTime()+"":"";
    					
    						logmessage+=status+"\t"+rscript.getPeer()+"\t"+rscript.getQueued_time()+"\t"+st_time+"\t"+new Date().getTime()+"\t"+rscript.getRequesthost()+"\t"+rscript.getUniquename();		
    						//log.debug("~~~~~~ Script started....rscript.getError():"+rscript.getError());		
    						if(status.equalsIgnoreCase("fail") && rscript.getError()!=null){			
    							logmessage+="\tError:"+RScriptAsyncListenerImpl.readFirst(rscript.getError(), 100);
    						}
    						RScriptAsyncListenerImpl.log(logmessage);		
    						long duration=new Date().getTime()-this.started.getTime();		
    						String rtn=this.uid+"~~"+result;
    						try{
    							this.messageinbound.getWsOutbound().writeTextMessage(CharBuffer.wrap(rtn));
    							this.messageinbound.getWsOutbound().flush();
    						}catch(Exception e){
    							log.error("Error, :onScriptFinished:"+e.getMessage());
    						}
    						
    						return "";
    					}
    					private RScript rscript;
    					private String peer;
    					private String result;
    					private String status;
    					private Date started;
    					private MessageInbound messageinbound;
    					private String uid;
    					
    					public Callable<String> init(String u,MessageInbound mib, RScript rs, String p, String r, String st, Date std){
    						this.uid=u;
    						this.messageinbound=mib;
    						this.rscript=rs;
        					this.peer=p;
        					this.result=r;
        					this.status=st;    	
        					this.started=std;
    						return this;
    					}
    			}.init(this.uniqueid,this.messageinbound,rscript, peer,result,status,this.started)
	    );

		*/
		
		
		
		
		
	
		
		
	}
	
	@Override
	public void onScriptTimedOut(RScript rscript) throws Exception {
		
		
	}
	
 
	

}


