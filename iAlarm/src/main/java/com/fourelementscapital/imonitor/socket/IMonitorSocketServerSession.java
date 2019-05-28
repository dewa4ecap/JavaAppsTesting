package com.fourelementscapital.imonitor.socket;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
//import java.util.function.Consumer;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fourelementscapital.imonitor.process.AlarmParser;
import com.fourelementscapital.imonitor.queue.IMonitorQueue;

import static java.nio.ByteBuffer.allocateDirect;

import java.io.IOException;

/*Notes from Gama :
Consumer  is an interface that only exist on Java 8 and above.*/

public class IMonitorSocketServerSession {

	private static final Logger log = LogManager.getLogger(IMonotorSocketServer.class.getName());
    
	//Java 8
	/*Consumer<String> readFunction;
    CompletionHandler<Integer, ByteBuffer> readCompletionHandler = createCompletionHandler((byteCount, byteBuffer) -> onFinishRead(byteCount), (ex, byteBuffer) -> onError(ex));*/
    
	// Modified to Java 7
    CompletionHandler<Integer, ByteBuffer> readCompletionHandler = new CompletionHandler<Integer, ByteBuffer>() {
    	public void completed(Integer result, ByteBuffer attachment) {
    		onFinishRead(result);
    	}
    	public void failed(Throwable exception, ByteBuffer attachment) {
    		onError(exception);
    	}
    };
    
    ByteBuffer byteBuffer = allocateDirect(2000);
    AsynchronousSocketChannel channel;

    //Java 8
    /*public IMonitorSocketServerSession(AsynchronousSocketChannel channel, Consumer<String> readFunction) {
    	this.channel = channel;
    	this.readFunction = readFunction;
    }*/
    
    // Modified to Java 7
    public IMonitorSocketServerSession(AsynchronousSocketChannel channel) {
        this.channel = channel;
    }
    
    public void startReading () {
    	readNext();
    }

    private void onFinishRead(Integer byteCount) {
        if(byteCount.equals(-1)) return;
        String message = readData(byteBuffer);
        IMonitorQueue.AddDataToQueue(AlarmParser.createAlarm(message, channel)); // execute after read finished
        try {
			channel.close();
		} catch (IOException ex) {
			log.log(Level.ERROR, ex.getMessage());
		}
    }
    
    private void onError(Throwable ex) {
    	 log.log(Level.ERROR, ex);
    }

    private void readNext() {
        channel.read(byteBuffer, byteBuffer, readCompletionHandler);
    }
    
    private String readData(ByteBuffer buffer) {
        buffer.flip();
        StringBuilder builder = new StringBuilder();
        while(buffer.remaining() > 0) {
            builder.append((char) buffer.get());
        }
        buffer.clear();
        return builder.toString();
    }
}
