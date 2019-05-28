/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.queue;

import static com.googlecode.cqengine.query.QueryFactory.and;
import static com.googlecode.cqengine.query.QueryFactory.ascending;
import static com.googlecode.cqengine.query.QueryFactory.equal;
import static com.googlecode.cqengine.query.QueryFactory.in;
import static com.googlecode.cqengine.query.QueryFactory.orderBy;
import static com.googlecode.cqengine.query.QueryFactory.queryOptions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fourelementscapital.scheduler.peer.QueueAbstract;
import com.fourelementscapital.scheduler.peer.QueueFactory;
import com.googlecode.cqengine.CQEngine;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.index.hash.HashIndex;
import com.googlecode.cqengine.index.navigable.NavigableIndex;
import com.googlecode.cqengine.query.Query;
 

public class QueueStackManager {


	private static Logger log = LogManager.getLogger(QueueStackManager.class.getName());
	
	private static IndexedCollection<QueueStack> qstack = CQEngine.newInstance();
	
	static{		
		qstack.addIndex(NavigableIndex.onAttribute(QueueStack.PEERNAME));		
		qstack.addIndex(NavigableIndex.onAttribute(QueueStack.NAME));
		qstack.addIndex(NavigableIndex.onAttribute(QueueStack.PRIORITY));
		qstack.addIndex(NavigableIndex.onAttribute(QueueStack.RUNNING));
		qstack.addIndex(NavigableIndex.onAttribute(QueueStack.AVAILABLE));
		qstack.addIndex(NavigableIndex.onAttribute(QueueStack.UID));		
		qstack.addIndex(HashIndex.onAttribute(QueueStack.SUPPORTEDTASKUIDS));
	}
	
	public static void buildQueue4Peer(String peername) {	

		QueueFactory qf=new QueueFactory();
		Map<String, QueueAbstract> q=qf.getQueue();
		ArrayList<QueueAbstract> uniquelist=new ArrayList();
		for(QueueAbstract qa:q.values()){
			if(!uniquelist.contains(qa)){
				uniquelist.add(qa);
				for(int i=0;i<qa.getConcurrentThreads();i++){
				  if(qa.getTaskUids().size()>0){	
					QueueStack qs=new QueueStack();
					qs.setName(qa.getName());
					qs.setPeername(peername);
					qs.setSupportedtaskuids(qa.getTaskUids());				
					qs.setUid(qa.getName()+i);
					qs.setRunning(false);
					qs.setAvailable(false);
					qstack.add(qs);
				  }
				}			
			}
		}
	}

	
	public static Set<QueueStack> getAllQueueStacks() throws Exception {		
		return QueueStackManager.qstack;
	}

	public static void peerDisconnected(String peername){
		if(peername!=null ){
			Query<QueueStack> query3 = equal(QueueStack.PEERNAME, peername);	 
			Iterator<QueueStack> it3=qstack.retrieve(query3,queryOptions(orderBy(ascending(QueueStack.UID)))).iterator();
			while(it3.hasNext()) {			
				qstack.remove(it3.next());
			}
		}
	}
	
	
	private static void refreshQS(QueueStack qs)  {
		synchronized(qs){
			QueueStackManager.qstack.remove(qs);
			QueueStackManager.qstack.add(qs);
		}
	}
	
	
	public static void setStackBusy(String peername, String q_uid) throws Exception  {
		setStackRunning(peername,q_uid,true);
	}
	
	public static void setStackIdle(String peername, String q_uid) throws Exception {
		setStackRunning(peername,q_uid,false);
	}
	
	
	private static void  setStackRunning(String peername, String q_uid,boolean flag) throws Exception {
		Query<QueueStack> query3 = and(equal(QueueStack.PEERNAME, peername),equal(QueueStack.UID, q_uid));
		Iterator<QueueStack> it3=QueueStackManager.qstack.retrieve(query3).iterator();
		if(it3!=null && it3.hasNext()){
			QueueStack qs=it3.next();
			qs.setRunning(flag);
			refreshQS(qs);
		}
	}
	
	public static String getPeerQueueStatForServer() throws Exception {
		Iterator<QueueStack> qst=QueueStackManager.qstack.iterator();
		String result="";
		while(qst.hasNext()){
			 QueueStack qs=qst.next();
			 result+=qs.getUid()+"="+qs.isRunning()+"|";
		}
		return result;
	}
	

	public static void server2SyncPeerQueue(String peername,String qstring) throws Exception {
		StringTokenizer st=new StringTokenizer(qstring,"|");
		while(st.hasMoreTokens()){
			String tkn=st.nextToken();
			//log.debug("server2SyncPeerQueue() tkn:"+tkn);
			if(tkn!=null && !tkn.trim().equals("")){
			    Pattern p=Pattern.compile("^(.*?)=(.*?)$");
			    Matcher matcher = p.matcher(tkn);
			    //log.debug("server2SyncPeerQueue() find:"+matcher.find()+" groupcount : "+matcher.groupCount());			    
			    if(matcher.find() && matcher.groupCount()==2){			    	
			    	
			    	String uid=matcher.group(1);
			    	String r=matcher.group(2);
			    	//log.debug("server2SyncPeerQueue() uid:"+uid+" r:"+r);
			    	boolean running=r.toLowerCase().equals("true")?true:false;
			    	boolean available=true;
			    	Query<QueueStack> query3 = and(equal(QueueStack.PEERNAME, peername),equal(QueueStack.UID, uid));
					Iterator<QueueStack> it3=QueueStackManager.qstack.retrieve(query3).iterator();
					if(it3!=null && it3.hasNext()){
						QueueStack qs=it3.next();
						qs.setRunning(running);
						qs.setAvailable(available); //it is available on peer.
						log.debug("adding queue, q:"+qs.getUid()+" name:"+qs.getName()+" peer:"+qs.getPeername());
						refreshQS(qs);
					}
			    }		
			}
		}
		  //this block is synchronizes number queue stack that available on peers. 
		
	    Query<QueueStack> q1 = and(equal(QueueStack.AVAILABLE, false),equal(QueueStack.PEERNAME, peername));			    
	    for (QueueStack q : QueueStackManager.qstack.retrieve(q1)) {	    	
	    	//need to check isAvailable because Object query filter is not working properly.
	    	if(!q.isAvailable()){
	    		log.debug("removing from server queue, q:"+q.getUid()+" name:"+q.getName()+" peer:"+q.getPeername()+" available:"+q.isAvailable());
	    		QueueStackManager.qstack.remove(q); //remove which are not available in peers, so that each peer can have its own number concurrent threads.
	    	}
	    }
	    
		
	}

	public static QueueStack useNextAvailableQueue(List peers, String taskuid) throws Exception {
		QueueStack rtn=null;
		Query<QueueStack> query3 =null;
		if(peers.size()>1){
			query3 = and(in(QueueStack.PEERNAME, (Collection)peers),equal(QueueStack.SUPPORTEDTASKUIDS, taskuid),equal(QueueStack.RUNNING,false));
		}else {
			query3 = and(equal(QueueStack.PEERNAME,(String)peers.get(0)),equal(QueueStack.SUPPORTEDTASKUIDS, taskuid),equal(QueueStack.RUNNING,false));
		}
		
		Iterator<QueueStack> it3=QueueStackManager.qstack.retrieve(query3,queryOptions(orderBy(ascending(QueueStack.EXECUTIONCOUNT)))).iterator();
		if(it3!=null && it3.hasNext()){
			rtn=it3.next();
			rtn.setExecutioncount(rtn.getExecutioncount()+1);
			rtn.setRunning(true);
			refreshQS(rtn);
		}
		return rtn;
	}
	
	
}


