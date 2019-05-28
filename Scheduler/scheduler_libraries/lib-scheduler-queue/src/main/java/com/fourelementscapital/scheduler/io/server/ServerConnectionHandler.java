/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.io.server;

import static com.googlecode.cqengine.query.QueryFactory.equal;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fourelementscapital.scheduler.queue.QueueStackManager;
import com.googlecode.cqengine.CQEngine;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.index.navigable.NavigableIndex;
import com.googlecode.cqengine.index.unique.UniqueIndex;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.resultset.ResultSet;

public class ServerConnectionHandler extends ChannelInboundHandlerAdapter  {
	 
		private static final Logger log = LogManager.getLogger(ServerConnectionHandler.class.getName());

		//private static Vector<ServerConnection> connection=new Vector();
		private static IndexedCollection<ServerConnection> connections = CQEngine.newInstance();
		static{
			connections.addIndex(NavigableIndex.onAttribute(ServerConnection.IP));		
			connections.addIndex(UniqueIndex.onAttribute(ServerConnection.USER));
			connections.addIndex(NavigableIndex.onAttribute(ServerConnection.CONNECTEDTIME));
			connections.addIndex(UniqueIndex.onAttribute(ServerConnection.CHCONTEXT));			
		}
		
	
		
	    public void channelActive(ChannelHandlerContext ctx) throws Exception {     
	     
	    	if(getServerConnection(ctx)==null){
	    		log.debug("new connection adding connection pool");
	    		ServerConnection sc=new ServerConnection(ctx);
	    		sc.setConnectedtime(new Date().getTime());
	    		connections.add(sc);
	    		log.debug("added");
	    		
	    	}
	    	
	    }

	    @Override
	    public void channelRead(ChannelHandlerContext ctx, Object request) throws Exception {
	    	//ServerConnection pc=new ServerConnection(ctx);
	    	
	    	if(request!=null && request instanceof Map){	    		
	    		ServerConnection sc=getServerConnection(ctx);
	    		log.debug("received map data from ip:"+sc.getIp());
	    		new ServerIORequestReceive((Map)request).process(sc);
	    	}
	    	
	    }

	    @Override
	    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
	        ctx.flush();
	    }
	    
	    
	    @Override
	    public void channelInactive(ChannelHandlerContext ctx)        throws Exception {	    		
	    		removeServerConnection(ctx);
	    		log.debug("channel inactive and removing server connection");
		    	ctx.close();
	    }
                

	    @Override
	    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {	
	    	//log.debug("exception caught");
	    	log.error("exceptionCaught()", cause);	     
	    	removeServerConnection(ctx);
	    	ctx.close();	        
	    	
	    }
	 
	
	    
	    
	    private ServerConnection getServerConnection(ChannelHandlerContext ctx) throws Exception {
	        
	    	Query<ServerConnection> query=equal(ServerConnection.CHCONTEXT,ctx);
	    	ResultSet<ServerConnection> rs=connections.retrieve(query);
	    	if(!rs.isEmpty()){
	    		return rs.iterator().next(); 
	    	}else{
	    		return null;
	    	}
	    }
	    
	    private boolean removeServerConnection(ChannelHandlerContext ctx) throws Exception {
	    	Query<ServerConnection> query=equal(ServerConnection.CHCONTEXT,ctx);
	    	ResultSet<ServerConnection> rs=connections.retrieve(query);
	    	boolean success=false;
	    	if(!rs.isEmpty()){
	    		ServerConnection sc=rs.iterator().next();
	    		if(sc!=null && sc.getUser()!=null){
	    			QueueStackManager.peerDisconnected(sc.getUser());
	    			connections.remove(sc);
	    			success=true;
	    		}
	    	}
	    	return success;
	    }
	    	    
	    public static ServerConnection getServerConnection(String peername) throws Exception {
	    	Query<ServerConnection> query=equal(ServerConnection.USER,peername);
	    	ResultSet<ServerConnection> rs=connections.retrieve(query);
	    	if(!rs.isEmpty()){
	    		return rs.iterator().next(); 
	    	}else{
	    		return null;
	    	}
	    }
	    public static Iterator<ServerConnection> getAllServerConnection() throws Exception {	    	
	    	return connections.iterator();
	    }
	    
	    public static void refreshServerConnection(ServerConnection sc) throws Exception {
	    	synchronized(sc){ 
	    		connections.remove(sc);
	    		connections.add(sc);
	    	} 
	    }

	    

}


