/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fe.p2p.websocket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.catalina.websocket.MessageInbound;
import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.WebSocketServlet;
import org.apache.catalina.websocket.WsOutbound;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fourelementscapital.scheduler.balance.LoadBalancingQueue;
import com.fourelementscapital.scheduler.rscript.RScript;
import com.fourelementscapital.scheduler.rscript.RScriptAsyncWebsocket;

 
public class TomcatWSExecuteRService extends WebSocketServlet {

	
	private Logger log = LogManager.getLogger(TomcatWSExecuteRService.class.getName());

    private final static Set<ChatMessageInbound> connections =      new CopyOnWriteArraySet<ChatMessageInbound>();
    

    @Override
    protected StreamInbound createWebSocketInbound(String subProtocol,     HttpServletRequest request) {
    	//System.out.println("TomcatWSServer.createWebSocketInbound()");
        return new ChatMessageInbound(request.getRemoteHost());
    }

  
    private final class ChatMessageInbound  extends MessageInbound {

    	private String ip;
    	public ChatMessageInbound(String ip) {
    		this.ip=ip;
    	}
    	
        protected void onOpen(WsOutbound outbound) {
            connections.add(this);  
            //System.out.println("TomcatWSServer.onOpen()");
            log.debug("connected...");
        }

        @Override
        protected void onClose(int status) {
            connections.remove(this);
            log.debug("closed...");
            //System.out.println("TomcatWSServer.onClose()");
        }

        @Override
        protected void onBinaryMessage(ByteBuffer message) throws IOException {
            throw new UnsupportedOperationException("Binary message not supported.");
        }

        @Override
        protected void onTextMessage(CharBuffer message) throws IOException {
            // Never trust the client
            //String filteredMessage = String.format("%s: %s",                    nickname, HTMLFilter.filter(message.toString()));
            //broadcast(message.toString());
            //log.debug("msg received:"+message);
        	String s=message.toString();
        	
        	Pattern pattern = Pattern.compile("^(.*?)~~(.*?)~~(.*?)$");
        	Matcher matcher = pattern.matcher(s);
        	
        	//String id=s.replaceAll("^(.*?)~~(.*?)~~(.*?)$", "$1");
        	//String engine=s.replaceAll("^(.*?)~~(.*?)~~(.*?)$", "$2");
        	//String script=s.replaceAll("^(.*?)~~(.*?)~~(.*?)$", "$3");
        	//System.out.println("matcher.groupCount:"+matcher.groupCount());
        	//System.out.println("matcher.find():"+ matcher.find());
        	
        	if(matcher.find() && matcher.groupCount()>2){ 

            	//String id=s.replaceAll("^(.*?)~~(.*?)~~(.*?)$", "$1");
            	//String engine=s.replaceAll("^(.*?)~~(.*?)~~(.*?)$", "$2");
            	//String script=s.replaceAll("^(.*?)~~(.*?)~~(.*?)$", "$3");

            	String id=matcher.group(1);
            	String engine=matcher.group(2);
            	String script=matcher.group(3);
            	//System.out.println("id:"+ id+" engine:"+engine);
            	//System.out.println("script:"+script);
            	//System.out.println("host:"+this.ip);
        		
        		RScript rs=new RScript();
				//set unique name from the parameter
				rs.setUniquename(id);			
				rs.setRequesthost(this.ip);
				 		
				rs.setTaskuid("direct_script");
				if(engine!=null && engine.equalsIgnoreCase("rserve")){
					rs.setTaskuid("direct_script_unix");
				}
		   	 
		   		rs.setScript(script);		   		
		   	 
	   			RScriptAsyncWebsocket listener=new RScriptAsyncWebsocket(id, this); 
	   			try{
	   				LoadBalancingQueue.getExecuteRScriptDefault().addExecuteR(rs,listener);
	   				
	   			}catch(Exception e){
	   				log.error("error:"+e.getMessage());
	   			}
        		
        	}else{
        		String rtn="Script can't executed without ID or script body, format: unique-id~~script_content";        		
        		this.getWsOutbound().writeTextMessage(CharBuffer.wrap(rtn));
        		this.getWsOutbound().flush();
        	}
        	//System.out.println("id:"+id);
        	//System.out.println("script:"+script);
        }
    }
    
    public static void broadcast(String message) {
        for (ChatMessageInbound connection : connections) {
        	Logger log = LogManager.getLogger(TomcatWSExecuteRService.class.getName());
        	log.debug("# connections:"+connections.size());
        	log.debug("# Msg:"+message);
            try {
            	
                CharBuffer buffer = CharBuffer.wrap(message);
                connection.getWsOutbound().writeTextMessage(buffer);
            } catch (IOException ignore) {
                // Ignore
            }
        }
    }
}


