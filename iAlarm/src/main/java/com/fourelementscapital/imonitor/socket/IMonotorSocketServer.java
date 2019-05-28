
package com.fourelementscapital.imonitor.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fourelementscapital.imonitor.config.ServerConfiguration;

import java.nio.ByteBuffer;

/*Notes from Gama :
Lambda expression (-> sign) only exist on Java 8 and above.*/

public class IMonotorSocketServer {
	
	private static final Logger log = LogManager.getLogger(IMonotorSocketServer.class.getName());
    private final AsynchronousChannelGroup channelGroup;
    AsynchronousServerSocketChannel listener;
    
    //Java 8
    /*private CompletionHandler<AsynchronousSocketChannel, Void> acceptCompletionHandler =  createCompletionHandler((channel, attach) -> onSuccess(channel), (ex, attach) -> onError(ex));;*/
    
    // Modified to Java 7
    private CompletionHandler<AsynchronousSocketChannel, Void> acceptCompletionHandler = new CompletionHandler<AsynchronousSocketChannel, Void>() {
    	public void completed(AsynchronousSocketChannel channel, Void v) {
    		onSuccess(channel);
    	}
    	
    	public void failed(Throwable exception, Void v) {
    		onError(exception);
    	}
    };
    
	public IMonotorSocketServer() throws Exception {
		channelGroup = AsynchronousChannelGroup.withFixedThreadPool(ServerConfiguration.getThreads(), Executors.defaultThreadFactory());
		listener = AsynchronousServerSocketChannel
	    		.open(channelGroup)
	    		.setOption(StandardSocketOptions.SO_REUSEADDR, true) 
	    		.bind(new InetSocketAddress(ServerConfiguration.getPort()), ServerConfiguration.getBacklog()); 
	}	
	
	public void startServer() throws IOException {
	    listener.accept(null, acceptCompletionHandler);
	    log.info("Server is listening at: " + ServerConfiguration.getPort());
	}
	
	private void onSuccess(AsynchronousSocketChannel clientChannel) {
		listener.accept(null, acceptCompletionHandler);
		
		//Java 8
		/*new IMonitorSocketServerSession(clientChannel, message -> {
			IMonitorQueue.AddDataToQueue(AlarmParser.createAlarm(message, clientChannel));
		}).startReading();*/
		
		// Modified to Java 7
		IMonitorSocketServerSession iMSoc = new IMonitorSocketServerSession(clientChannel);
		iMSoc.startReading();
		
	}
	
	private void onError(Throwable ex) {
      log.log(Level.ERROR, ex);
    }

    public void stop() throws IOException {
    	listener.close();
    }
}