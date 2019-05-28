/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.io.server;

import io.netty.channel.ChannelHandlerContext;

import java.net.InetSocketAddress;

import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.attribute.SimpleNullableAttribute;
import com.googlecode.cqengine.codegen.AttributesGenerator;

public class ServerConnection {

	private String user;
	private ChannelHandlerContext chcontext;
	private String ip;
	private long connectedtime;
	
	
	
	public String getUser() {
		return user;
	}

	public long getConnectedtime() {
		return connectedtime;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public void setChcontext(ChannelHandlerContext chcontext) {
		this.chcontext = chcontext;
	}

	public void setConnectedtime(long connectedtime) {
		this.connectedtime = connectedtime;
	}

	public ChannelHandlerContext getChcontext() {
		return chcontext;
	}

	public String getIp() {
		if(this.ip==null){
		    InetSocketAddress add=(InetSocketAddress)this.chcontext.channel().remoteAddress();
		    ip=add.getHostName();
		}
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public ServerConnection(ChannelHandlerContext chc){
		this.chcontext=chc;
		
	}
	
	
	public static void main(String[] args) {
		System.out.println(AttributesGenerator.generateAttributesForPastingIntoTargetClass(ServerConnection.class));
	}

	
	
	
	/*
	public boolean equals(Object other) {
		if(other instanceof ServerConnection){
			ServerConnection ot=(ServerConnection)other;
			if(ot.chcontext.equals(this.chcontext)){
				return true;
			}else{
				return false;
			}
		}else{
			return false;
		}
	}
	*/
	


    /**
     * CQEngine attribute for accessing field {@code ServerConnection.user}.
     */
    // Note: For best performance:
    // - if this field cannot be null, replace this SimpleNullableAttribute with a SimpleAttribute
    public static final Attribute<ServerConnection, String> USER = new SimpleNullableAttribute<ServerConnection, String>("USER") {
        public String getValue(ServerConnection serverconnection) { return serverconnection.user; }
    };

    /**
     * CQEngine attribute for accessing field {@code ServerConnection.chcontext}.
     */
    // Note: For best performance:
    // - if this field cannot be null, replace this SimpleNullableAttribute with a SimpleAttribute
    public static final Attribute<ServerConnection, ChannelHandlerContext> CHCONTEXT = new SimpleAttribute<ServerConnection, ChannelHandlerContext>("CHCONTEXT") {
        public ChannelHandlerContext getValue(ServerConnection serverconnection) { return serverconnection.chcontext; }
    };

    /**
     * CQEngine attribute for accessing field {@code ServerConnection.ip}.
     */
    // Note: For best performance:
    // - if this field cannot be null, replace this SimpleNullableAttribute with a SimpleAttribute
    public static final Attribute<ServerConnection, String> IP = new SimpleNullableAttribute<ServerConnection, String>("IP") {
        public String getValue(ServerConnection serverconnection) { return serverconnection.ip; }
    };

    /**
     * CQEngine attribute for accessing field {@code ServerConnection.connectedtime}.
     */
    public static final Attribute<ServerConnection, Long> CONNECTEDTIME = new SimpleAttribute<ServerConnection, Long>("CONNECTEDTIME") {
        public Long getValue(ServerConnection serverconnection) { return serverconnection.connectedtime; }
    };

	


     

}


