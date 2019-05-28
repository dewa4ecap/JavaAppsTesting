/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fe.io.peer;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fourelementscapital.scheduler.io.peer.PeerClientHandler;

public class PeerClient implements Runnable  {

	private final String host;
	private final int port;
	private static  Timer timer=null; 
	private Logger log = LogManager.getLogger(PeerClient.class.getName());	
	 
	public PeerClient(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public void run()  {
	        EventLoopGroup group = new NioEventLoopGroup(10);
	        try {
	        	
	        	if(timer==null){
		            TimerTask tt=new TimerTask() {
			            public void run() {
			            	String tname="peercleint_timer_"+(new Date().getTime()/1000);
			            	
			            	ChannelHandlerContext chc=PeerClientHandler.getChannelHandler();
			            	//System.out.println("PeerClient timer:"+chc);
			            	if(chc!=null){
			            		//System.out.println("PeerClient timer: active:"+chc.channel().isActive());
			            		//System.out.println("PeerClient timer: open:"+chc.channel().isOpen());
			            		if(!chc.channel().isOpen()){
			            			Thread t=new Thread(new PeerClient(this.host,this.port),tname);
			            			t.start();
			            		}
			            	}else{
		            			Thread t=new Thread(new PeerClient(this.host,this.port),tname);
		            			t.start();
			            	}
			            }
			            private String host=null;
			        	private int port=0;
			            public TimerTask init(String h,int p){
			            	this.host = h;
			        		this.port = p;
			        		return this;
			            }
			        }.init(this.host, this.port);
			        //checks every 1 minute to see if a thread still running....
			        long freq=10*1000;
			        PeerClient.timer = new Timer("scheduler_con_monitor");		        
			        PeerClient.timer.scheduleAtFixedRate(tt,freq, freq);   
	        	}
	        	
	            Bootstrap b = new Bootstrap();
	            b.group(group)
	             .channel(NioSocketChannel.class)
	             .handler(new ChannelInitializer<Channel>() {
						@Override
						protected void initChannel(Channel ch) throws Exception {
							ChannelPipeline pipeline=ch.pipeline();	        
					        pipeline.addLast("decoder", new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
					        pipeline.addLast("encoder", new ObjectEncoder());
					        // and then business logic.
					        pipeline.addLast("handler", new PeerClientHandler());
						}				
	             	}
	             );	             

	            // Start the connection attempt.
	            //System.out.println("~~~~~~~~~PeerClient starting....");
	            log.debug("before bootstrap starting...");
	            Channel ch = b.connect(host, port).sync().channel();
	            
	            //Read commands from the stdin.
	            //ChannelFuture lastWriteFuture = null;	     
                //lastWriteFuture = ch.writeAndFlush(p);
                //If user typed the 'bye' command, wait until the server closes
                //the connection.                
                //ch.closeFuture().sync();                
	            //Wait until all messages are flushed before closing the channel.                
	            //if (lastWriteFuture != null) {
	            //    lastWriteFuture.sync();
	            //}	            
	            //} catch(ConnectException ce){   
	        	//System.out.println("PeerClient() exception:~~~~~~~");
	            //ce.printStackTrace();
	        	
	        } catch(Exception e){   
	        	group.shutdownGracefully();
	        	log.error("error while PeerClient:"+e.getMessage());
	        } finally {
	            //group.shutdownGracefully();
	        	//System.out.println("PeerClient() finally:~~~~~~~");
	        }
	}        

}


