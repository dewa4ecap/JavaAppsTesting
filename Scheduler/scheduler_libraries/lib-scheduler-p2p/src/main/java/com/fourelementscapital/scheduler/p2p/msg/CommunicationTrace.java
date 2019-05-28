/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.p2p.msg;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fourelementscapital.scheduler.p2p.websocket.TomcatWSConsole;
 

 

public class CommunicationTrace {

	
	private String peername=null;
	
	public static ArrayList<String> PEERS=new ArrayList<String>();	 
	public static ArrayList<String> IGNORE_CLASS=new ArrayList<String>();
	
	
	private ArrayList ignoreprops=new ArrayList();	
	private static final int OUTGOING=1;
	private static final int INCOMING=2;
	
	private Logger log = LogManager.getLogger(CommunicationTrace.class.getName());
	
	
	public CommunicationTrace(String p){
		this.peername=p;

			 		
		if(!IGNORE_CLASS.contains("PeerOnlineStatus")){
			IGNORE_CLASS.add("PeerOnlineStatus");
		}
		
		ignoreprops.add("class");
		ignoreprops.add("RendezVousPropagatejxta-NetGroup");
		ignoreprops.add("JxtaWireHeader");
		ignoreprops.add("msgCreator");
		ignoreprops.add("msgRecipient");		
		ignoreprops.add(MessageNames.MESSAGE_BEAN_NAME);
		ignoreprops.add(MessageNames.MESSAGE_TYPE_CALLBACK);
		ignoreprops.add("next_trigger_time");
	 
	}
	
	public void outgoing(Map attachment){
		if(PEERS.contains(this.peername)){
			produceOutPut(OUTGOING,attachment);
		}
		
	}
	
	public void incoming(Map attachment){
		if(PEERS.contains(this.peername)){
			produceOutPut(INCOMING,attachment);
		}
	}
	
	private void produceOutPut(int direction ,Map attachment){
		String handlerclass=(String)attachment.get(MessageNames.MESSAGE_BEAN_NAME);
		handlerclass=handlerclass.replaceAll("^(.*\\.)(\\S+)$", "$2");
		
		SimpleDateFormat sdf=new SimpleDateFormat("HH:mm:ss S");
		
		if(!IGNORE_CLASS.contains(handlerclass)){
			String d="<span class='time'>"+sdf.format(new Date())+"</span>";
			String line="";
			String pcname=this.peername;
			try{
				pcname=pcname.toLowerCase().replaceAll("4ecap(pc|sv|lt|vm)sg(\\d+)" , "$1$2");
			}catch(Exception e){log.error("error while parsing computer name:"+e.getMessage()); }
					
			if(direction==OUTGOING)	line+=d+" :<span class='hclass'>"+handlerclass+"</span>:<span class='p "+pcname+"'>"+this.peername+"</span><span class='out'>--&gt;</span>";
			if(direction==INCOMING)	line+=d+" :<span class='hclass'>"+handlerclass+"</span>:<span class='p "+pcname+"'>"+this.peername+"</span><span class='in'>&lt;--</span>";
			for(Iterator<String> i=attachment.keySet().iterator();i.hasNext();){
				String ky=i.next();					
				if(!ignoreprops.contains(ky) &&  attachment.get(ky)!=null){
					//log.debug(""+this.peername+" class:"+handlerclass+" data:"+attachment);
					if(ky.equals("scheduler_id")){
						line+=ky+":<span class='sc_id'>"+attachment.get(ky)+"</span>, ";
					}else{
						line+=ky+":"+attachment.get(ky)+", ";
					}
				}					
			}
			//out("\n");
			out(line);
		}
	}
	
	private void out(String s) {
		//System.out.print(s);		
		//WSServer.consoleToAll(s);		
		TomcatWSConsole.consoleToAll(s);
		
	}

	
}



