/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.p2p.websocket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.servlet.http.HttpServletRequest;

import org.apache.catalina.websocket.MessageInbound;
import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.WebSocketServlet;
import org.apache.catalina.websocket.WsOutbound;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

 
public class TomcatWSConsole extends WebSocketServlet {

	private Logger log = LogManager.getLogger(TomcatWSConsole.class.getName());

    private final static Set<ChatMessageInbound> connections =      new CopyOnWriteArraySet<ChatMessageInbound>();
    

    @Override
    protected StreamInbound createWebSocketInbound(String subProtocol,     HttpServletRequest request) {
    	//System.out.println("TomcatWSServer.createWebSocketInbound()");
        return new ChatMessageInbound();

    }

    
    
    private final class ChatMessageInbound  extends MessageInbound {

        protected void onOpen(WsOutbound outbound) {
            connections.add(this);   
           // System.out.println("TomcatWSServer.onOpen()");
            log.debug("connected...");
        }

        @Override
        protected void onClose(int status) {
            connections.remove(this);
            log.debug("closed...");
           // System.out.println("TomcatWSServer.onClose()");
        }

        @Override
        protected void onBinaryMessage(ByteBuffer message) throws IOException {
            throw new UnsupportedOperationException(
                    "Binary message not supported.");
        }

        @Override
        protected void onTextMessage(CharBuffer message) throws IOException {
            // Never trust the client
            //String filteredMessage = String.format("%s: %s",                    nickname, HTMLFilter.filter(message.toString()));
            //broadcast(message.toString());
            log.debug("msg received:"+message);
        	//this.getWsOutbound().writeTextMessage(message);
        	
        	CommandMain cm=new  CommandMain(message.toString());
    		String val=cm.validate();
    		
    		JSONObject json=new JSONObject();
    		try{
    			json.put("rs", val);
    			this.getWsOutbound().writeTextMessage(CharBuffer.wrap(json.toString()));
    			//conn.send(json.toString());
    		}catch(Exception e){
    			log.error("Error onMessage:"+e.getMessage());
    		}
        	
        }

       
    }
    
   
    public static void consoleToAll(String msg){
    	
    	for (ChatMessageInbound connection : connections) {

    		try {
    			JSONObject obj=new JSONObject();			
				try {
					obj.put("c", msg);
				} catch (JSONException e) {  }
                CharBuffer buffer = CharBuffer.wrap(obj.toString());
                connection.getWsOutbound().writeTextMessage(buffer);
            } catch (IOException ignore) {
                // Ignore
            }
        }
		
		 
	}

}


