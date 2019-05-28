/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.peer;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fourelementscapital.db.SchedulerDB;
import com.fourelementscapital.scheduler.ScheduledTaskFactory;
import com.fourelementscapital.scheduler.config.Config;
import com.fourelementscapital.scheduler.engines.ScheduledTask;
import com.fourelementscapital.scheduler.engines.StackFrame;
import com.fourelementscapital.scheduler.p2p.P2PService;
import com.fourelementscapital.scheduler.p2p.peer.PeerSpecificConfigurations;

public class QueueFactory {
	
	
	private static TreeMap queueHandlers=new TreeMap();	
	public static String KEY_STARTED_TIME="started_time";
	public static String KEY_TRIGGER_TIME="trigger_time";
	public static String KEY_SCHEDULER_ID="scheduler_id";
	public static String KEY_CURRENT_TIME="current_time";
	public static String KEY_TASK_NAME="name";
	private final static int NUM_SIMULTANEOUS_CONN=10; 
	 
	protected static boolean restartRequested=false;
 
	private Logger log = LogManager.getLogger(QueueFactory.class.getName());
	 
	public QueueFactory(){
		if(queueHandlers.size()==0){		
			initQueue();			
		}
	}

	
	public QueueAbstract getQueue(String taskuid){
		return (QueueAbstract)queueHandlers.get(taskuid);
	}
	
	public TreeMap getQueue(){
		return queueHandlers;
	}

	public static void setRestartRequested(){
		restartRequested=true;
	}

	/**
	 * this method used to send BUSY/NOBUSY signal to the server. 
	 * @return
	 */
	
	public int countExcTasksInPeer(){
		int executing=0;
		for(Iterator  i=queueHandlers.values().iterator();i.hasNext();){
			QueueAbstract aq=(QueueAbstract)i.next();
			if(aq.getExecutingStacksSize()>0){
				//executing=true;
				executing=executing+aq.getExecutingStacksSize();
			}
		}
		return executing;
	}
	
	
	/**
	 * this method will be used to send running tasks id of the peer to server.
	 * @return
	 */
	public Vector getExecutingIDs(){
		Vector executing=new Vector();
		for(Iterator  i=queueHandlers.values().iterator();i.hasNext();){
			QueueAbstract aq=(QueueAbstract)i.next();
			Object stacks[]=aq.getExecutingStacks();
			for(int a=0;a<stacks.length;a++){
				StackFrame sf=(StackFrame)stacks[a];
				if(sf.getData()!=null && !executing.contains(sf.getData().get("id"))){
					executing.add(sf.getData().get("id"));
				}
			}
		}
		return executing;
	}
	
	/**
	 * this method is used by getPeerInfo method of schedulerAPI.
	 * @return
	 */
	public Vector getExecutingTasksData(){
		Vector executing=new Vector();
		Vector rtn=new Vector();
		for(Iterator  i=queueHandlers.values().iterator();i.hasNext();){
			QueueAbstract aq=(QueueAbstract)i.next();
			Object stacks[]=aq.getExecutingStacks();
			for(int a=0;a<stacks.length;a++){
				StackFrame sf=(StackFrame)stacks[a];
				if(sf.getData()!=null && !executing.contains(sf.getData().get("id"))){
					executing.add(sf.getData().get("id"));
					Map data=new HashMap();
					data.put(KEY_SCHEDULER_ID, sf.getData().get("id"));
					data.put(KEY_STARTED_TIME, sf.getStarted_time());
					data.put(KEY_TRIGGER_TIME, sf.getTrigger_time());
					data.put(KEY_TASK_NAME, sf.getData().get("name"));
					data.put(KEY_CURRENT_TIME, new Date().getTime());
					rtn.add(data);
				}
			}
		}
		return rtn;
	}
	
	/*
	 * This method will is being used by peer to send currently  running tasks and time to server to update the scheduler user interface
	 */
	public Map<String,String> getExecutingIDAndSTimes(){
		TreeMap executing=new TreeMap();
		for(Iterator  i=queueHandlers.values().iterator();i.hasNext();){
			QueueAbstract aq=(QueueAbstract)i.next();
			Object stacks[]=aq.getExecutingStacks();
			for(int a=0;a<stacks.length;a++){
				StackFrame sf=(StackFrame)stacks[a];
				if(sf.getData()!=null){
					executing.put(sf.getData().get("id")+"", sf.getTrigger_time()+"");
				}
				//executing.add(sf.getData().get("id"));
			}
		}
		return executing;
	}
	
	private void initQueue(){
	
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		try{		
			sdb.connectDB();			
			sdb.removeAllPeerThreadStatus(P2PService.getComputerName());
		}catch(Exception e){ }finally{
			try{sdb.closeDB();}catch(Exception e1){}
		}
		
		//over-rides
		QueueAbstract rserv=new QueueAbstract("rserv"){
			public Vector getTaskUids(){
				Vector v=new Vector();
				
				
				
				SchedulerDB sdb=SchedulerDB.getSchedulerDB();
				try{		
					sdb.connectDB();			
					Vector grp=sdb.getGroups("rscript4rserve");
					//Vector allgroups=sdb.getAllGroups();
			    	for(Iterator i=grp.iterator();i.hasNext();){
			    		Map data=(Map)i.next();
			    		String taskuid=(String)data.get("taskuid");
			    		v.add(taskuid);
			    	}
					
				}catch(Exception e){ }finally{
					try{sdb.closeDB();}catch(Exception e1){}
				}
				
				//v.add(new RServeScheduledTask().getUniqueid());				
				//v.add(new RServeLowPriorityTask().getUniqueid());
				//v.add(new RServeUnixTask().getUniqueid());
				//v.add(new RScript4Weather().getUniqueid());
				
				return v;
			}
			public int getConcurrentThreads(){
				if(System.getProperty("os.name").toLowerCase().startsWith("windows")){
					return 1;
				}else{
					int rtn=NUM_SIMULTANEOUS_CONN;
					try{
						if(Config.getValue(Config.CONFIG_NUMBEROF_RSERVE_THREADS)!=null){
							rtn=Integer.parseInt(Config.getValue(Config.CONFIG_NUMBEROF_RSERVE_THREADS).trim());
							log.debug("group rscript4rserve found peer specific thread size, currently"+rtn);
						}
					}catch(Exception e){
						System.out.println("QueueFactory.initQueue() Error:"+e.getMessage());
					}
					return rtn;
				}
				
			}
			
		};
		add2Qhandler(rserv);
		
		
		//over-rides
		QueueAbstract rservunix=new QueueAbstract("rservunix"){
			public Vector getTaskUids(){
				Vector v=new Vector();
						
				v.add("direct_script_unix");		
				
				
				SchedulerDB sdb=SchedulerDB.getSchedulerDB();
				try{		
					sdb.connectDB();			
					Vector grp=sdb.getGroups("rscript4rserveunix");
					//Vector allgroups=sdb.getAllGroups();
			    	for(Iterator i=grp.iterator();i.hasNext();){
			    		Map data=(Map)i.next();
			    		String taskuid=(String)data.get("taskuid");
			    		v.add(taskuid);
			    	}
				}catch(Exception e){ }finally{
					try{sdb.closeDB();}catch(Exception e1){}
				}			
				return v;
			}
			public int getConcurrentThreads(){	
				
				int rtn=NUM_SIMULTANEOUS_CONN;
				try{
					//if(Config.getValue(Config.CONFIG_NUMBEROF_RSERVE_THREADS)!=null){
					//	rtn=Integer.parseInt(Config.getValue(Config.CONFIG_NUMBEROF_RSERVE_THREADS).trim());
					//	log.debug("group rscript4rservunix found peer specific thread size, currently"+rtn);
					//}					
					String val=PeerSpecificConfigurations.getProperties().getProperty(PeerSpecificConfigurations.KEY_CONCURRENT_SESSION);
					if(val!=null && !val.trim().equals("")){
						rtn=Integer.parseInt(val);
					}
					
				}catch(Exception e){System.out.println("QueueFactory.initQueue() Error:"+e.getMessage());}
				return rtn;
			}
		};
		add2Qhandler(rservunix);
		
		//over-rides
		QueueAbstract rscript=new QueueAbstract("rscript"){
			public Vector getTaskUids(){
				Vector v=new Vector();
				
				//add exclicitly direct_script into rscript group to make single threaded.
				v.add("direct_script");
				 
				SchedulerDB sdb=SchedulerDB.getSchedulerDB();
				try{		
					sdb.connectDB();			
					Vector grp=sdb.getGroups("rscript");
					//Vector allgroups=sdb.getAllGroups();
			    	for(Iterator i=grp.iterator();i.hasNext();){
			    		Map data=(Map)i.next();
			    		String taskuid=(String)data.get("taskuid");	    	
			    		if(taskuid!=null ){
			    			v.add(taskuid);
			    		}
			    	}
					
				}catch(Exception e){ }finally{
					try{sdb.closeDB();}catch(Exception e1){}
				}
			
				
				return v;
			}
			public int getConcurrentThreads(){
				return 1;
			}			
		};
		
		add2Qhandler(rscript);		
				
		QueueAbstract othercripts=new QueueAbstract("othercripts"){
			public Vector getTaskUids(){
				Vector v=new Vector();
				List tasks=new ScheduledTaskFactory().getAllConfiguredTasks();
				for(Iterator i=tasks.iterator();i.hasNext();){
					ScheduledTask st=(ScheduledTask)i.next();
					if(!queueHandlers.containsKey(st.getUniqueid())){
						v.add(st.getUniqueid());
					}
				}
				return v;
			}
			public int getConcurrentThreads(){
				return 1;
			}
		};
		add2Qhandler(othercripts);
		
	}
	
	
	private void add2Qhandler(QueueAbstract qa){
		for(Iterator i=qa.getTaskUids().iterator();i.hasNext();){
			queueHandlers.put(i.next(), qa);
		}
	}
	
	
	
	
}


