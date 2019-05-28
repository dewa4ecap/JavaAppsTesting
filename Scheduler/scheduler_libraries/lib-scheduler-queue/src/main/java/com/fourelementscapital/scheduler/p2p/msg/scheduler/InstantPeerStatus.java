/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.p2p.msg.scheduler;

 
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.Vector;

import org.apache.jcs.JCS;
import org.apache.jcs.engine.behavior.IElementAttributes;

import com.fourelementscapital.scheduler.p2p.listener.OutgoingMessageCallBack;
import com.fourelementscapital.scheduler.p2p.msg.MessageHandler;
import com.fourelementscapital.scheduler.p2p.msg.PostCallBack;
import com.fourelementscapital.scheduler.p2p.msg.PostMessage;
import com.fourelementscapital.scheduler.peer.QueueFactory;

public class InstantPeerStatus extends MessageHandler implements PostCallBack {


	
	private String uid=null;	
	
	
	public String getUid() {
		return uid;
	}


	public void setUid(String uid) {
		this.uid = uid;
	}


	
	public int getPriority() {
		return  OutgoingMessageCallBack.PRIORITY_LOW;
	}
	
	public Map getStatus(String clientname) throws Exception {
		
		InstantPeerStatus ins=new InstantPeerStatus();
		ins.setUid(UUID.randomUUID().toString());
		new PostMessage(ins,clientname).send();
		int count=0;
		Map data=null;
		while(count < 20 && data==null){
			data=(Map)getCache().get(ins.getUid());
			try{Thread.sleep(100);}catch(Exception e){}
			count++;
		}
		//System.out.println("<><><><><><><><>retrived:"+ins.getUid());
		return data;
		
	}
	
	
	
	public Map executeAtDestination() {
	 
		Map h=new HashMap();
		QueueFactory qfactory=new QueueFactory();
		SimpleDateFormat sdf=new SimpleDateFormat("HH:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		  
		Vector v= qfactory.getExecutingTasksData();
	    for(Iterator i=v.iterator();i.hasNext();){
	    	Map row=(Map)i.next();
	    	if(row.get(QueueFactory.KEY_SCHEDULER_ID)!=null){
	    		
	    		Long st=(Long)row.get(QueueFactory.KEY_STARTED_TIME );
	    		Long cu=(Long)row.get(QueueFactory.KEY_CURRENT_TIME );
	    		long dur=cu-st;
	    		Date d=new Date(dur);
	    		 
	    		String rd="Task:"+row.get(QueueFactory.KEY_TASK_NAME)+" ("+row.get(QueueFactory.KEY_SCHEDULER_ID)+")  Running:"+sdf.format(d);
	    		h.put(row.get(QueueFactory.KEY_SCHEDULER_ID),rd);
	    	}
	    }
	    
	    //System.out.println("@@@@@@@@@@@@@@@@ -------->Data collected at client: data: "+h+" ins-id:"+getUid());
	    
		return h;
		
	}

 
	public void callBack(Map data) {
		//System.out.println("<-----@@@@@@@ Data received at server:uid:"+getUid()+" data: "+data);
		
		try{
			String uid=getUid();
			IElementAttributes att= getCache().getDefaultElementAttributes();
			att.setMaxLifeSeconds(20);
			getCache().put(uid, data);
		}catch(Exception  e){
			e.printStackTrace();
		}
		
	}

	
	
	
	private static JCS cache=null;
	private static JCS getCache() throws Exception {
		 if(cache==null){
				cache=JCS.getInstance(InstantPeerStatus.class.getName());
		 }
		 return cache;
	}


	@Override
	public void onCallBackSendingFailed() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onSendingFailed() {
		// TODO Auto-generated method stub
		
	}
}


