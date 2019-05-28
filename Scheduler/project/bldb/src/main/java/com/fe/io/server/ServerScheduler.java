/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fe.io.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import com.fourelementscapital.scheduler.io.server.ServerConnectionHandler;

 

public class ServerScheduler implements Runnable {

	private int port;
    
    public ServerScheduler(int port) {
        this.port = port;
    }
    
    public void run() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap(); // (2)
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class) // (3)
             .childOption(ChannelOption.TCP_NODELAY, true)
             .childOption(ChannelOption.SO_KEEPALIVE, true)
             .childHandler(new ChannelInitializer<SocketChannel>() { // (4)                 
                 public void initChannel(SocketChannel ch) throws Exception {                     
                     ChannelPipeline pipeline = ch.pipeline();                     
                     pipeline.addLast(new ObjectEncoder(),   new ObjectDecoder(ClassResolvers.cacheDisabled(null)), new ServerConnectionHandler());
                     
                 }
            });
            
            b.bind(port).sync().channel().closeFuture().sync();
        }catch(Exception e){
        	e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
    
    public static void main(String[] args) throws Exception {
        int port;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = 8888;
        }
        
    }
}



