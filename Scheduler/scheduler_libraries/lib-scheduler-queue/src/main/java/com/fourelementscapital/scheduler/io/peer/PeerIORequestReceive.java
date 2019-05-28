/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.io.peer;

import io.netty.channel.ChannelHandlerContext;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fourelementscapital.scheduler.io.request.IOExcecutors;
import com.fourelementscapital.scheduler.io.request.IOServerRequest;
import com.fourelementscapital.scheduler.io.request.IOServerRequestCallback;
import com.fourelementscapital.scheduler.p2p.msg.MessageNames;

public class PeerIORequestReceive {

	private Map data=null;
	private Logger log = LogManager.getLogger(PeerIORequestReceive.class.getName());

	//private static ExecutorService dispatchService=Executors.newCachedThreadPool() ; // Executors.newFixedThreadPool(15);
	
	public PeerIORequestReceive(Map attachments){
		this.data=attachments;
	}
	
	public void process(ChannelHandlerContext ctx){
		
		String classname=(String)this.data.get(MessageNames.MESSAGE_BEAN_NAME);
		String loc="1";
		if(classname!=null && !classname.equals("")){			
			
			try{			
				Class c=Class.forName(classname);
				Constructor ct = c.getConstructor();
				IOServerRequest req=(IOServerRequest)ct.newInstance();
				BeanUtils.populate(req, this.data);
				
				//System.out.println("PeerIORequestReceive req:"+req);				
				log.debug("thread name:"+Thread.currentThread().getName());				
				if(req instanceof IOServerRequest){
					//req.executeAtPeer();
					Future fu=IOExcecutors.threadpool.submit(			    					
	    	    			new Callable<String>() {
	    	    					public String call(){	    	    						 
	    	    						this.request.executeAtPeer(); 
	    	    						return "";
	    	    					}
	    	    					private IOServerRequest request;
	    	    					public Callable<String> init(IOServerRequest req){					 
	    	    						this.request=req;    	    						 
	    	    						return this;
	    	    					}
	    	    			}.init(req)
	    	    	);
					//fu.get();
					
				}				
				if(req instanceof IOServerRequestCallback){
					IOServerRequestCallback req1=(IOServerRequestCallback)req;					
					Future fu=IOExcecutors.threadpool.submit(			    					
	    	    			new Callable<String>() {
	    	    					public String call(){	    	    						 
	    	    						this.request.executeAtPeer();	
	    	    						try{
		    	    						Map data=BeanUtils.describe(this.request);
		    	    						data.put(MessageNames.MESSAGE_BEAN_NAME,this.request.getClass().getName());	  
		    	    						this.ctx.writeAndFlush(data);
	    	    						}catch(Exception e){
	    	    							log.error("while invoking callback: e:"+e.getMessage());
	    	    						}
	    	    						return null;
	    	    					}
	    	    					private IOServerRequestCallback request;
	    	    					private ChannelHandlerContext ctx;
	    	    					public Callable<String> init(IOServerRequestCallback req,ChannelHandlerContext c){					 
	    	    						this.request=req;    	    	
	    	    						this.ctx=c;
	    	    						return this;
	    	    					}
	    	    			}.init(req1,ctx)
	    	    	);
					//fu.get();
				}				
				//HashMap rtn=req.executeAtPeer();
				//rtn will be passed back to client...				
			}catch(Exception e){
				e.printStackTrace();				
				log.error("process() E:"+e.getMessage()+" classname:"+classname+" loc:"+loc);
			}
		}
	}

}


