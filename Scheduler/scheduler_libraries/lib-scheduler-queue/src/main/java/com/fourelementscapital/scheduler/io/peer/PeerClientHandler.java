/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.io.peer;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fourelementscapital.scheduler.io.msg.PeerRequestLogin;
import com.fourelementscapital.scheduler.p2p.P2PService;
import com.fourelementscapital.scheduler.queue.QueueStackManager;

public class PeerClientHandler  extends ChannelInboundHandlerAdapter {

    private static final Logger log = LogManager.getLogger(PeerClientHandler.class.getName());
    private static ChannelHandlerContext connection2server=null;
    
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception{ 
    	
    		log.debug("channelActive");
    		
    		if(connection2server==null || (connection2server!=null && !connection2server.channel().isActive()) ){
    			connection2server=ctx;    			
    			PeerRequestLogin login=new PeerRequestLogin();    			
    			String computername=P2PService.getComputerName(); 
    			log.debug("computer name");
    			login.setPeername(computername);	    			
    			try{
    				//in-case of earlier had somthing...
    				//QueueStackManager.peerDisconnected(computername);
    				if(QueueStackManager.getAllQueueStacks().size()>0){	    					
    				}else{
    					QueueStackManager.buildQueue4Peer(computername);
    				}   
    				
    				login.setQueuestring(QueueStackManager.getPeerQueueStatForServer());
    			}catch(Exception e){
    				e.printStackTrace();
    			}
    			login.send();
    			//new Post2Server(login).send();    			
    		}
    		log.debug("channelActive end");
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception{
    	ctx.close();
    	if(ctx.channel().isOpen()){    		 
    		ctx.disconnect();
    	}
    	
    	connection2server=null;    	 
    	
    	System.out.println("PeerClientHandler() channel inactive");
    	
    }
    
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object request) throws Exception {
    	
    	//System.out.println("PeerClientHandler +"+request);
    	if(request!=null && request instanceof Map){ 		
    		new PeerIORequestReceive((Map)request).process(ctx);
    	}
    
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //logger.log(Level.WARNING, "Unexpected exception from downstream.", cause);
        connection2server=null;
        System.out.println("PeerClientHandler() channel exceptionCaught");
        log.error("exceptionCaught()", cause);
        ctx.close();
       
    	if(ctx.channel().isOpen()){
    		ctx.disconnect();    		
    	}
     

    }
    
    public static ChannelHandlerContext getChannelHandler(){
    	return PeerClientHandler.connection2server;
    }
}


