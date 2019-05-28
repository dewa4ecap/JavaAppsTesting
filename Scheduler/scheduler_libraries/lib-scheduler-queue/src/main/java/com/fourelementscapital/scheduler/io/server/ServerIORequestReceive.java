/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.io.server;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fourelementscapital.scheduler.io.request.IOExcecutors;
import com.fourelementscapital.scheduler.io.request.IOPeerRequest;
import com.fourelementscapital.scheduler.io.request.IOServerRequestCallback;
import com.fourelementscapital.scheduler.p2p.msg.MessageNames;

public class ServerIORequestReceive {

	private Map data=null;
	private Logger log = LogManager.getLogger(ServerIORequestReceive.class.getName());
	
	//private static ExecutorService dispatchService= Executors.newFixedThreadPool(30);
	
	
	
	public ServerIORequestReceive(Map attachments){
		this.data=attachments;
	}	
	public void process( ServerConnection pc){
		
		String classname=(String)this.data.get(MessageNames.MESSAGE_BEAN_NAME);
		String loc="1";
		if(classname!=null && !classname.equals("")){
			try{
				Class c=Class.forName(classname);
				Constructor ct = c.getConstructor();
				Object req=ct.newInstance();				
				BeanUtils.populate(req, this.data);
								 
				//Future fu=dispatchService.submit(
				Future fu=IOExcecutors.threadpool.submit(
						
	    			new Callable<String>() {
	    					public String call(){	   	    						 
	    						if(this.request instanceof IOPeerRequest){
	    							IOPeerRequest req1=(IOPeerRequest)this.request;
	    							req1.executeAtServer(this.pc);
	    						}
	    						if(this.request instanceof IOServerRequestCallback){
	    							IOServerRequestCallback req2=(IOServerRequestCallback)this.request;
	    							req2.callBack();
	    						}
	    						return null;
	    					}
	    					private Object request;
	    					private ServerConnection pc;
	    					public Callable<String> init(Object req,ServerConnection p){					 
	    						this.request=req;
	    						this.pc=p;
	    						return this;
	    					}
	    			}.init(req,pc)
    	    	);
    	    	
    	    	 
				
				

				
			 	/*
			 	 * this block is not good, as it is creating so many threds after some times
			 	 * the performance is really going bad.		    					
    			Thread th= new Thread(	new Runnable() {
    					public void run(){	   	    						 
    						if(this.request instanceof IOPeerRequest){
    							IOPeerRequest req1=(IOPeerRequest)this.request;
    							req1.executeAtServer(this.pc);
    						}
    						if(this.request instanceof IOServerRequestCallback){
    							IOServerRequestCallback req2=(IOServerRequestCallback)this.request;
    							req2.callBack();
    						}
    						 
    					}
    					private Object request;
    					private ServerConnection pc;
    					public Runnable init(Object req,ServerConnection p){					 
    						this.request=req;
    						this.pc=p;
    						return this;
    					}
    			}.init(req,pc));
    			th.start();
	    		*/	
	    		
	    			
    	    	 
				
				log.debug("received, data:classname:"+classname+":script_uid:"+this.data.get("script_uid"));
				
				//fu.get();
				/*				
				if(req instanceof IOPeerRequest){
					IOPeerRequest req1=(IOPeerRequest)req;
					req1.executeAtServer(pc);
				}
				if(req instanceof IOServerRequestCallback){
					IOServerRequestCallback req2=(IOServerRequestCallback)req;
					req2.callBack();
				}
				*/
				
				//rtn will be passed back to client...
				
			}catch(Exception e){
				e.printStackTrace();				
				log.error("process() E:"+e.getMessage()+" classname:"+classname+" loc:"+loc);
			}
		}
	}
}


