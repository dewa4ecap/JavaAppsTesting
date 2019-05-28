/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.balance.hsqldb;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.jcs.JCS;
import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fourelementscapital.alarm.Alarm;
import com.fourelementscapital.alarm.AlarmType;
import com.fourelementscapital.alarm.ThemeVO;
import com.fourelementscapital.db.SchedulerDB;
import com.fourelementscapital.fileutils.RandomString;
import com.fourelementscapital.scheduler.ScheduledTaskFactory;
import com.fourelementscapital.scheduler.SchedulerEngine;
import com.fourelementscapital.scheduler.alarm.SchedulerAlarm;
import com.fourelementscapital.scheduler.alarm.SchedulerAlarmVO;
import com.fourelementscapital.scheduler.balance.LoadBalancingQueue;
import com.fourelementscapital.scheduler.balance.LoadBalancingQueueItem;
import com.fourelementscapital.scheduler.balance.LoadBalancingQueueTimeout;
import com.fourelementscapital.scheduler.balance.WaitingQueueList;
import com.fourelementscapital.scheduler.config.Config;
import com.fourelementscapital.scheduler.engines.ScheduledTask;
import com.fourelementscapital.scheduler.engines.SchedulerExePlanLogs;
import com.fourelementscapital.scheduler.engines.StackFrame;
import com.fourelementscapital.scheduler.exception.ExceptionDependencyTimeout;
import com.fourelementscapital.scheduler.p2p.P2PService;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public abstract class LoadBalancingHSQLLayerDB extends LoadBalancingQueue {

	private Connection connection=null;
	private Logger log = LogManager.getLogger(LoadBalancingHSQLLayerDB.class.getName());
	private static Semaphore dblock=new Semaphore(1,true);
	
	private static final Byte QUEUEGATE_QUEUED= new Byte("1");
	private static final Byte QUEUEGATE_RUNNING= new Byte("2");
	
	private static long TIMEOUT_MS=2000;	
	protected long lastExcecutedTime=0;
	protected static final int ADDED_TO_QUEUE=1;
	protected static final int QUEUE_OVERLAPPED=-1;
	protected static final int QUEUE_DUPLICATE_FOUND=-2;
	protected static final int QUEUE_ERROR_FOUND=0;
	
	
	//for Queue traffic alert 
	private static Timer timer=null;
	private static Map<String, String> alert_range=null;
	private static JCS cache=null;
	private static TreeMap<String, Long> task_stat_max_waiting=new TreeMap<String, Long>();
	private static int ALERT_FREQUENCY_MINUTES=5; 
	

	private static ExecutorService startTaskService= Executors.newFixedThreadPool(1);
	
	
	public static int DELAY_QUEUE_PROCESSING=75;
	
	protected void acquireLock(){
		
		try{
			
			//Date start=new Date();
			
			LoadBalancingHSQLLayerDB.dblock.tryAcquire(TIMEOUT_MS, TimeUnit.MILLISECONDS);
			//Date end=new Date();			
			//long diff=end.getTime()-start.getTime();
			//if(diff>0){			
				
				//String caller=collectStack(Thread.getAllStackTraces().get(Thread.currentThread()));
				 
			//	log.debug("                  Caller  time:"+diff+" id:"+Thread.currentThread().getId() +"\n"+caller);
			//}
			//LoadBalancingHSQLLayerDB.dblock.acquire();
		}catch(Exception e){
			log.error("Error:"+e.getMessage());
		}
	}
	

	protected void releaseLock(){
		
		try{			
			LoadBalancingHSQLLayerDB.dblock.release();
			//log.debug("....releasing lock: thread:"+Thread.currentThread().getId());
		}catch(Exception e){
			log.error("Error:"+e.getMessage());
		}
	}
	

	protected void initDB(){
		
		if(this.connection==null){		
			acquireLock();
			try{
				
				log.debug("connecting database");
				Class.forName("org.hsqldb.jdbc.JDBCDriver" );
					
					//C:\rnd\hsqldb-2.2.9\scheduler_data\queue

				   String hsqlQueueFile = Config.HSQLDB_QUEUE_FILE;
					String qdf=Config.getValue(hsqlQueueFile);
					//String qdf=Config.getValue(Config.HSQLDB_QUEUE_FILE);
					
				if(qdf==null){
					qdf="queue";
				}
				String driver="jdbc:hsqldb:file:"+qdf;	
				log.debug("************************************** driver:"+driver);
				File f=new File(qdf+".script");
				if(!f.exists()){	
					this.connection = DriverManager.getConnection(driver, "SA", "SA");
					String q=createSchemaQuery();
					Statement st=this.connection.createStatement();
					st.execute(q);
					st.close();
				}else{
					this.connection = DriverManager.getConnection(driver, "SA", "SA");
				}				
				
			}catch(Exception  e){
				e.printStackTrace();
				log.error("Error while getting db connection, Error:"+e.getMessage());	
			}finally{
				releaseLock();
			}
		}
	}
	
	protected abstract void processQueueItem(LoadBalancingHSQLQueueItem currentItem) throws Exception;
	
	private boolean isOutputExpired(String key, int delay_secs) throws Exception{
		if(getCache().get(key)==null){
			IElementAttributes att= cache.getDefaultElementAttributes();
			att.setMaxLifeSeconds(delay_secs);
			getCache().put(key,"0",att);
			return true;
	 	}else{
	 		return false;
	 	}
	 	
	}
	
	protected void queueLoop() throws Exception {
		
		int loopcount=0;
		boolean loop=true;
		while(loop) {			
			 
			Statement st=this.connection.createStatement();
			ResultSet rs0=st.executeQuery("SELECT distinct taskuid from queue");
			String query="";
			int count=0;
			while(rs0.next()){				
				String q="Select * from (select * from queue where taskuid='"+rs0.getString("taskuid")+"' AND queuegate="+QUEUEGATE_QUEUED+" AND waitingfordp=false ORDER by trigger_time,lastExecutedDuration,schedulerid limit 1) as a"+count++;					
				query+=(!query.equals(""))?" UNION "+q:q;
			}
			//add the items that have already been processed and waiting for dependents to be validated
			query+=(!query.equals(""))?" UNION ":"";
			query+="Select * from (Select * from queue WHERE queuegate="+QUEUEGATE_QUEUED+" and  waitingfordp=true) as all_dep ";			
			
 
			if(query!=null && !query.equals("")){
				
				PreparedStatement ps=this.connection.prepareStatement(query);			
 
				acquireLock();	 
			 	
				try{
					
					ResultSet rs=ps.executeQuery();					
					while(rs.next()){				
						try{
							LoadBalancingHSQLQueueItem item=getLBQItem(rs);
							if(processDependencyQueueItem(item)){
								processQueueItem(item);
							}
						}catch(Exception e){
							e.printStackTrace();
							log.error("error while processing row, error:"+e.getMessage());
						}
					}			
					ps.close();
					rs.close();
					
					PreparedStatement ps1=this.connection.prepareStatement("SELECT * from queue WHERE queuegate="+QUEUEGATE_QUEUED);
					ResultSet rs1=ps1.executeQuery();
					loopcount++;
					if(!rs1.next()){
						log.debug("no record found so exiting"+query);
						loop=false;				
					}
					
					//if(loopcount>20){
					//	loop=false;
					//}
					rs1.close();
					ps1.close();
				}catch(Exception e){
					e.printStackTrace();
					log.error("Error while looping...e:"+e.getMessage());
				}finally{
					releaseLock();
				}
				Thread.sleep(DELAY_QUEUE_PROCESSING);
				
			}else{
				loop=false;			
			}
			
		}
	}

	
	
	private boolean processDependencyQueueItem(LoadBalancingHSQLQueueItem currentItem) throws Exception {
		boolean dependpass=true;	
		if(currentItem!=null){
	    	//Map data=currentItem.getSf().getData();
	    	String dids=currentItem.getDependentids();
	    	int depvalid=0;
	    	
	    	if(dids!=null && !dids.equals("") && currentItem.getDependentchecktime()!=null &&  !currentItem.getDependentchecktime().equals("")){
	    		
	    		String dtime=currentItem.getDependentchecktime();	    		
	    		SchedulerDB sdb=SchedulerDB.getSchedulerDB();	    		
				try{
					
					sdb.connectDB();
		    		depvalid=dependencyCheck(currentItem,dids,dtime,sdb);		    		
		    		if(depvalid==1) dependpass=true;			    		
		    		if(depvalid==0) dependpass=false;
		    		if(depvalid==-1){
		    			
		    			dependpass=false;
		    			
		    			String status=ScheduledTask.DEPENDENCY_TIMEOUT;
		    			TreeMap<String, Comparable> ldata=new TreeMap<String, Comparable>();
		    			ldata.put("scheduler_id", currentItem.getSchedulerid());
		    			ldata.put("trigger_time",currentItem.getTrigger_time());
		    			ldata.put("status",ScheduledTask.DEPENDENCY_TIMEOUT);
		    			ArrayList<TreeMap<String, Comparable>> v=new ArrayList<TreeMap<String, Comparable>>();
						v.add(ldata);
						sdb.updateQueueLog(v,new Vector(), P2PService.getComputerName());
						
						String msg="Dependency timed out, because next trigger time is in a minute";
						new SchedulerExePlanLogs(currentItem.getSchedulerid(),currentItem.getTrigger_time()).log(msg,sdb,SchedulerExePlanLogs.SERVER_ERROR_DEPENDENCY_TIMEDOUT);
						
						ExceptionDependencyTimeout exp=new ExceptionDependencyTimeout(msg);
	    				sdb.updateResponseCode(currentItem.getSchedulerid(), currentItem.getSf().getTrigger_time(), exp.getErrorcode());	
		    		
							
		    			removeDependencyTimedout(currentItem.getSchedulerid(),currentItem.getTrigger_time());
		    			
		    			Map data=sdb.getScheduler(currentItem.getSchedulerid());
		    			String type=(String)data.get("alert_type");
		    			String name=(String)data.get("name");
		    			
		    		
	    				
		    			if(type!=null && !type.equals("")){

		    				// send alarm : 
		    				
		    				int sc_id = currentItem.getSchedulerid();
		    				long tri_time = currentItem.getSf().getTrigger_time();
		    				
		    				SchedulerAlarmVO vo = new SchedulerAlarmVO();
		    				vo.setAlarmType(type);
		    				vo.setName(name);
		    				vo.setSubject(SchedulerAlarm.ALARM_SUB_TIMEOUT);
		    				vo.setMessage(msg);
		    				vo.setFrom(null);
		    				vo.setErrCode(exp.getErrorcode());
		    				vo.setExceptionSchedulerTeamRelated(false);
		    				vo.setComputerName(P2PService.getComputerName());
		    				vo.setConsoleMsg(sdb.getConsoleMsg(sc_id, tri_time));
		    				vo.setExecLogs(sdb.getSchedulerExeLogs(sc_id, tri_time));
		    				vo.setRepCodeExist(sdb.execLogsRepcodeExist(sc_id, tri_time, SchedulerExePlanLogs.SERVER_ERROR_ALARM_SENT));
		    				vo.setThemeTags(sdb.getThemeTags(sc_id));
		    				vo.setOwnerTheme(sdb.getOwnerTheme(sc_id));
		    				vo.setQueueLog(sdb.getQueueLog(sc_id, tri_time));
		    				vo.setPeerFriendlyName(sdb.getPeerFriendlyName(vo.getFrom()));

		    				vo.setSchedulerId(sc_id);
		    				vo.setTriggerTime(tri_time);

		    				SchedulerAlarm.sendAlarm(vo); 
	    					new SchedulerExePlanLogs(sc_id, tri_time).log("Alarm sent",sdb,SchedulerExePlanLogs.SERVER_ERROR_ALARM_SENT);
	
		    				
		    			}
		    			
		    			Map<String,String> d1=sdb.getTaskEventActions(currentItem.getSchedulerid(), currentItem.getSf().getTrigger_time());
		    			if(d1.containsKey(ScheduledTask.FIELD_DEPENDENCY_TIMEOUT) && d1.get(ScheduledTask.FIELD_DEPENDENCY_TIMEOUT)!=null 
								&& !d1.get(ScheduledTask.FIELD_DEPENDENCY_TIMEOUT).trim().equals("")
							){
								String expression=d1.get(ScheduledTask.FIELD_DEPENDENCY_TIMEOUT);
								String suffi=ScheduledTask.TASK_EVENT_CALL_EXP_ID_VARIABLE+"="+currentItem.getSchedulerid()+"\n";
								suffi+=ScheduledTask.TASK_EVENT_CALL_EXP_TRIGGERTIME_VARIABLE+"="+currentItem.getSf().getTrigger_time()+"\n";
								new SchedulerEngine().executeScriptExpression(expression, "onDependency timeout of "+currentItem.getSchedulerid(), suffi);
								
						}
		    			
		    			
		    			
		    		}
		    		
		    		if(!currentItem.isWaitingfordp()){
						waiting4Dependent(currentItem.getSchedulerid(),currentItem.getTrigger_time());
					}
				}catch(Exception e){
					log.error("Error at processDependencyQueueItem:"+e.getMessage());
				}finally{
					sdb.closeDB();
				}
	    	}else{
	    		//no dependent 
	    		dependpass=true;	
	    	}
	     
		}
		return dependpass; 
	}
	
	
	protected void waiting4Dependent(int schedulerid, long trigger_time) throws Exception {	 
		 
		PreparedStatement ps=this.connection.prepareStatement("UPDATE queue SET waitingfordp=? WHERE schedulerid=? AND trigger_time=?");
		ps.setBoolean(1, new Boolean(true));		
		ps.setInt(2, schedulerid);
		ps.setLong(3,trigger_time);		
		int result=ps.executeUpdate();
		log.debug("waiting4Dependent() result:"+result);
		ps.close();
	}
	
	
	
	/**
	 * making this single threaded queue.
	 * @see com.fe.scheduler.balance.LoadBalancingQueue#startedIfNotStarted(int, long, java.lang.String)
	 */
	public final int startedIfNotStarted(int schedulerid,long trigger_time, String machinename){
		
		Future fu=startTaskService.submit(
		new Callable<Integer>() {
				public Integer call(){
					//return new Integer(new LoadBalancingHSQLQueue().startedIfNotStarted1(sc_id,tri_time,peer));
					int rtn=0;
					if(LoadBalancingQueue.getDefault() instanceof LoadBalancingHSQLQueue){
						LoadBalancingHSQLQueue lhq=(LoadBalancingHSQLQueue)LoadBalancingQueue.getDefault();
						rtn=lhq.startedIfNotStarted1(sc_id,tri_time,peer);
						log.debug("starting sc_id:"+sc_id+" tri_time:"+sc_id+" peer:"+peer);
					}
					return new Integer(rtn);
				}
				private int sc_id=0;
				private long tri_time=0;
				private String peer=null;
				public Callable<Integer> init(int sc_id,long tri_time,String peer){
					this.sc_id=sc_id;
					this.tri_time=tri_time;
					this.peer=peer;
					return this;
				}
		}.init(schedulerid, trigger_time, machinename));
		
		Integer status=new Integer(0);
		try{
			 status=(Integer)fu.get();
		}catch(Exception e){
			log.error("error while retriveing future result");
		}
		
		return status.intValue();
	}
	 
	protected final int startedIfNotStarted1(int schedulerid,long trigger_time, String machinename) {
		int rtn=-1;
		//acquireLock();		
		String iden=schedulerid+"_"+trigger_time;
		boolean respondpeer=false;
		try{
			//do not respond if it is already started..			
			if(getGroupedCache().getFromGroup(iden, LoadBalancingQueue.CACHE_GROUP_TENDERSCHEDULERTASK)!=null){
				respondpeer=true;
			}
		}catch(Exception e){
			log.error("error while accessing grouped cache, e:"+e.getMessage());
		}
		
		if(respondpeer){
			
			try{
				getGroupedCache().remove(iden, LoadBalancingQueue.CACHE_GROUP_TENDERSCHEDULERTASK); //removing from cache so subsequent replies from other peers for this task will be ignored.
				
				PreparedStatement ps0=this.connection.prepareStatement("SELECT * from queue WHERE schedulerid=? AND trigger_time=? AND executing=?");						
				ps0.setInt(1, schedulerid);
				ps0.setLong(2,trigger_time);		
				ps0.setBoolean(3, new Boolean(true));
				ResultSet rs0=ps0.executeQuery();
				
				if(rs0.next()){
					//already started no need to start again.
					rtn=0;
				}else{
					
					
					PreparedStatement ps=this.connection.prepareStatement("UPDATE queue SET queuegate=?,executing=?,machine=?,started=?,started_time=? WHERE schedulerid=? AND trigger_time=?");
					ps.setByte(1,LoadBalancingHSQLLayerDB.QUEUEGATE_RUNNING);
					ps.setBoolean(2, new Boolean(true));				
					ps.setString(3, machinename);	
					Date d=new Date();
					ps.setTimestamp(4,new Timestamp(d.getTime()));
					ps.setLong(5,d.getTime());				
					ps.setInt(6, schedulerid);
					ps.setLong(7,trigger_time);						
					
					int result=ps.executeUpdate();
					rtn=1;
					log.debug("startedIfNotStarted() result:"+result);
					ps.close();
					if(result>0){										
						PreparedStatement ps2=this.connection.prepareStatement("SELECT * from queue WHERE schedulerid=? AND trigger_time=?");
						ps2.setInt(1, schedulerid);
						ps2.setLong(2, trigger_time);
						ResultSet rs2=ps2.executeQuery();
						LoadBalancingHSQLQueueItem item=null;
						if(rs2.next()){			
							item=getLBQItem(rs2);
						}		
						log.debug("-----------item.getTimeoutexpiry:"+item.getTimeoutexpiry());
						if(item!=null && item.getTimeoutexpiry()>0){
							addTimeoutForTask(item.getSchedulerid(),item.getTrigger_time(),item.getStarted().getTime(),item.getTimeoutexpiry());
						}
					}else{
						//rtn=0;
						
					}				
				
				}
				ps0.close();
				rs0.close();				
				//ps.set(1, x);			

			}catch(Exception e){
				e.printStackTrace();
				log.error("Error while moving queued to running:"+e.getMessage());	
			}finally{
				//releaseLock();
			}
		}
		return rtn;
	}
	
	public boolean killQueuedTask(int scheduler_id, long trigger_time)  {	
		
 
		boolean rtn=false;
		int removed=removeFromQueue( scheduler_id, trigger_time);
		if(removed>0){
			new SchedulerExePlanLogs(scheduler_id,trigger_time).log("Queued task killed by user",SchedulerExePlanLogs.SERVER_ERROR_QUEUE_KILLED_BYUSER);
			rtn=true;
		}	   
		return rtn;
		 
	}
	
	
	private synchronized int removeFromQueue(int scheduler_id, long trigger_time){
		int rtn=0;
		acquireLock();
		try{
			PreparedStatement ps0=this.connection.prepareStatement("DELETE from queue WHERE schedulerid=? AND trigger_time=? AND queuegate=?");						
			ps0.setInt(1, scheduler_id);
			ps0.setLong(2,trigger_time);
			ps0.setByte(3,LoadBalancingHSQLLayerDB.QUEUEGATE_QUEUED);
			rtn=ps0.executeUpdate();
			log.debug("removeFromQueue(): rtn:"+rtn);
			
		}catch(Exception e){			
			e.printStackTrace();
			log.error("Error removeFromQueue():"+e.getMessage());	
		}finally{
			releaseLock();
		}
		return rtn;
		//queue.remove(item);
		//exitQueueIteration=true;	 
		//log.debug("removing from Queue schedueler_id:"+item.getSchedulerid());
	}

	
	protected int add2DBQueue(LoadBalancingHSQLQueueItem item)  {
		//acquireLock();
		int rtn=0;
		try{
			
			PreparedStatement ps0=this.connection.prepareStatement("SELECT * from queue WHERE schedulerid=?");
			ps0.setInt(1, item.getSchedulerid());			
			ResultSet rs0=ps0.executeQuery();
			
			boolean add=false;
			if(rs0.next()){
				//item already found in the queue	
				//this suitation happens when there 2 cron expression with same time, for example every 2 minutes and every 20 minutes will 3 duplicates in an hour
				PreparedStatement ps2=this.connection.prepareStatement("SELECT * from queue WHERE schedulerid=? AND trigger_time=?");
				ps2.setInt(1, item.getSchedulerid());		
				ps2.setLong(2, item.getTrigger_time());	
				ResultSet rs2=ps2.executeQuery();
				if(rs2.next()){
					rtn=QUEUE_DUPLICATE_FOUND;
				}else{
					if(item.getConcurrentexecution()>1){
						PreparedStatement ps3=this.connection.prepareStatement("SELECT count(*) thrd from queue WHERE schedulerid=?");
						ps3.setInt(1, item.getSchedulerid());	
						ResultSet rs3=ps3.executeQuery();
						if(rs3.next()){
							int t=rs3.getInt("thrd");
							if(t>=item.getConcurrentexecution()){
								rtn=QUEUE_OVERLAPPED;	
								add=false;
							}else{								
								add=true;
							}
						}
						rs3.close();
						ps3.close();
					}else{
						rtn=QUEUE_OVERLAPPED;
						add=false;
					}
				}
				
			}else{
				add=true;	
			}
			if(add){
				log.debug("adding item:"+item.getSchedulerid());
				log.debug("item last executedDuration:"+item.getLastExecutedDuration());
				log.debug("item timeout expiry:"+item.getTimeoutexpiry());
				
				PreparedStatement ps=this.connection.prepareStatement("INSERT INTO queue(schedulerid,executing,machine,started,lastExecutedDuration,overlaptimeout,inject_code,timeoutexpiry,trigger_time,nexttrigger_time,started_time,status,taskuid,queuegate,dependentids,dependentchecktime,waitingfordp,started_peers) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
				ps.setInt(1, item.getSchedulerid());
				ps.setBoolean(2, item.isExecuting());
				ps.setString(3,item.getMachine());			
				if(item.getStarted()!=null){
					ps.setTimestamp(4,new Timestamp(item.getStarted().getTime()));
				}else{
					ps.setTimestamp(4,null);
				}
				ps.setLong(5,item.getLastExecutedDuration());
				ps.setLong(6,item.getOverlaptimeout());
				ps.setString(7,item.getInject_code());
				ps.setLong(8,item.getTimeoutexpiry());
				ps.setLong(9,item.getTrigger_time());
				ps.setLong(10,item.getNexttrigger_time());
				ps.setLong(11,item.getStarted_time());
				ps.setString(12,item.getStatus());		
				ps.setString(13,item.getTaskuid());				
				
				if(item.getQueuegate()==null){
					ps.setByte(14,LoadBalancingHSQLLayerDB.QUEUEGATE_QUEUED);
				}else{
					ps.setByte(14,item.getQueuegate());
				}		
				ps.setString(15,item.getDependentids());
				ps.setString(16,item.getDependentchecktime());
				ps.setBoolean(17, item.isWaitingfordp());
				ps.setString(18,item.getStarted_peers());
				
				ps.executeUpdate();			
				rtn=ADDED_TO_QUEUE;
			}
			ps0.close();
			rs0.close();
			if(LoadBalancingHSQLLayerDB.timer==null){
				startTimer();
			}
			
		}catch(Exception e){
			e.printStackTrace();
			log.error("Error adding to db, Error:"+e.getMessage());	
		}finally{
			//releaseLock();			
		}
		return rtn;
	}
	
	
	
	public synchronized void removeItemProcessing(LoadBalancingQueueItem item0,String message,int respCode){
		
		if(item0 instanceof LoadBalancingHSQLQueueItem){
			LoadBalancingHSQLQueueItem item=(LoadBalancingHSQLQueueItem)item0;
			if(message!=null){
				new SchedulerExePlanLogs(item.getSchedulerid(),item.getTrigger_time()).log(message,respCode);
			}
			acquireLock();
			try{
				PreparedStatement ps0=this.connection.prepareStatement("DELETE from queue WHERE schedulerid=? AND trigger_time=? AND queuegate=?");						
				ps0.setInt(1, item.getSchedulerid());
				ps0.setLong(2,item.getTrigger_time());
				ps0.setByte(3,LoadBalancingHSQLLayerDB.QUEUEGATE_RUNNING);				 
				int removed=ps0.executeUpdate();
				if(removed==0){
					log.error("item scd_id:"+ item.getSchedulerid()+" trig_time:"+item.getTrigger_time()+" not found in the running queue");
				}
			}catch(Exception e){			
				e.printStackTrace();
				log.error("Error removeFromQueue():"+e.getMessage());	
			}finally{
				releaseLock();
			}			
			removeTimeoutForTask(item.getSchedulerid(),item.getTrigger_time());
			//Debugger.addDebugMsg("script id:"+item.getSchedulerid()+" executing finishing removing form LB executing Queue",item.getSchedulerid()+"");
			
			log.debug("removing from processing Q schedueler_id:"+item.getSchedulerid());
			//exitQueueIteration=true;
			//item.setSf(null);
			//item=null;	
		}
	}

	
	protected void removeDependencyTimedout(int schedulerid, long trigger_time) throws Exception {
		
		PreparedStatement ps0=this.connection.prepareStatement("DELETE from queue WHERE schedulerid=? AND trigger_time=?");						
		ps0.setInt(1, schedulerid);
		ps0.setLong(2,trigger_time);	 		 
		ps0.executeUpdate();
		
	}
	
	public boolean removeFaultyProcessingTask(int scheduler_id, long trigger_time)  {	
		
		boolean removed=false;		
		acquireLock();
		try{
			PreparedStatement ps0=this.connection.prepareStatement("DELETE from queue WHERE schedulerid=? AND trigger_time=? AND queuegate=?");						
			ps0.setInt(1, scheduler_id);
			ps0.setLong(2,trigger_time);
			ps0.setByte(3,LoadBalancingHSQLLayerDB.QUEUEGATE_RUNNING);				 
			int rtn=ps0.executeUpdate();
			if(rtn>0) removed=true;
		}catch(Exception e){			
			e.printStackTrace();
			log.error("Error removeFromQueue():"+e.getMessage());	
		}finally{
			releaseLock();
		}		
		return removed;
	}

	
	public synchronized void executionFailed(int schedulerid,long trigger_time, String machinename){
		acquireLock();
		try{
			PreparedStatement ps=this.connection.prepareStatement("UPDATE queue SET queuegate=?,executing=?,machine=? WHERE schedulerid=? AND trigger_time=?");
			ps.setByte(1,LoadBalancingHSQLLayerDB.QUEUEGATE_QUEUED);
			ps.setBoolean(2, new Boolean(false));
			ps.setString(3, null);
			ps.setLong(4, schedulerid);
			ps.setLong(5,trigger_time);			
			ps.executeUpdate();
			ps.close();
		}catch(Exception e){			
			e.printStackTrace();
			log.error("Error executionFailed():"+e.getMessage());	
		}finally{
			releaseLock();
		}	
	}
	
	
	@Override
	public void peerStarted(int scheduler_id, long trigger_time, String peername)	throws Exception {
		acquireLock();
		try{
			PreparedStatement ps=this.connection.prepareStatement("SELECT * from queue WHERE schedulerid=? AND trigger_time=?");		 
			ps.setInt(1, scheduler_id);
			ps.setLong(2,trigger_time);			
			ResultSet rs=ps.executeQuery();
			if(rs.next()){
				String started=rs.getString("started_peers");
				if(started!=null){
					log.error("The task "+scheduler_id+" trigger_time"+trigger_time+" is already started on "+started);
				}
				started=(started!=null && !started.equals(""))?started+","+peername:peername;					
				PreparedStatement ps1=this.connection.prepareStatement("UPDATE queue SET started_peers=? WHERE schedulerid=? AND trigger_time=?");
				ps1.setString(1, started);
				ps1.setInt(2, scheduler_id);
				ps1.setLong(3,trigger_time);
				ps1.executeUpdate();
				ps1.close();
			}
			ps.close();
			rs.close();
		}catch(Exception e){			
			e.printStackTrace();
			log.error("Error executionFailed():"+e.getMessage());	
		}finally{
			releaseLock();
		}	
	}
	
	
	public void executionEnded(int schedulerid, long trigger_time) {
		//LoadBalancingHSQLQueueItem item= new LoadBalancingHSQLQueueItem();
		//item.setSchedulerid(schedulerid);
		//item.setTrigger_time(trigger_time);
		
		LoadBalancingQueueItem item=getItemFromProcessingQueue(schedulerid,trigger_time);		
		removeItemProcessing(item,null,SchedulerExePlanLogs.SERVER_OK_REMOVED_FROM_PQUEUE); 	
		lastExcecutedTime=new Date().getTime();
	}
	
	
	public LoadBalancingQueueItem getItemFromProcessingQueue(int scheduler_id, long trigger_time)  {
		
		acquireLock();
		LoadBalancingHSQLQueueItem item=null;
		try{
			PreparedStatement ps=this.connection.prepareStatement("SELECT * from queue WHERE schedulerid=? AND trigger_time=?");
			ps.setInt(1, scheduler_id);
			ps.setLong(2, trigger_time);
			ResultSet rs=ps.executeQuery();
			
			if(rs.next()){			
				item=getLBQItem(rs);
			}			
			ps.close();
			rs.close();
 
		}catch(Exception e){
			log.error("Error getItemFromProcessingQueue() "+e.getMessage());
		}finally{
			releaseLock();
		}
		return item;
	}
	
	/**
	 * @deprecated
	 * @param scheduler_id
	 * @return
	 */
	protected LoadBalancingHSQLQueueItem getItemFromQueue(int scheduler_id)  {
		
		acquireLock();
		LoadBalancingHSQLQueueItem item=null;
		try{
			PreparedStatement ps=this.connection.prepareStatement("SELECT * from queue WHERE schedulerid=?");
			ps.setInt(1, scheduler_id);		 
			ResultSet rs=ps.executeQuery();
			
			if(rs.next()){			
				item=getLBQItem(rs);
			}			
			ps.close();
			rs.close();
 
		}catch(Exception e){
			log.error("Error getItemFromQueue() "+e.getMessage());
		}finally{
			releaseLock();
		}
		return item;
	}

	
	protected List<LoadBalancingHSQLQueueItem> getItemsFromQueue(int scheduler_id)  {
		
		acquireLock();
		
		//LoadBalancingHSQLQueueItem item=null;
		ArrayList list=new ArrayList();
		try{
			PreparedStatement ps=this.connection.prepareStatement("SELECT * from queue WHERE schedulerid=?");
			ps.setInt(1, scheduler_id);		 
			ResultSet rs=ps.executeQuery();
			
			while(rs.next()){			
				LoadBalancingHSQLQueueItem item=getLBQItem(rs);
				list.add(item);
			}			
			ps.close();
			rs.close();
 
		}catch(Exception e){
			log.error("Error getItemFromQueue() "+e.getMessage());
		}finally{
			releaseLock();
		}
		return list;
	}

	
	
	
	private ArrayList<LoadBalancingQueueItem> getTasks(String query){		
		
		ArrayList tasks=new ArrayList();
		acquireLock();
		try{
			PreparedStatement ps=this.connection.prepareStatement(query);
			ResultSet rs=ps.executeQuery();
			
			while(rs.next()){				
				try{
					LoadBalancingHSQLQueueItem item=getLBQItem(rs);
					tasks.add(item);
					
				}catch(Exception e){
					log.error("error while processing row, error:"+e.getMessage());
				}
			}			
			ps.close();
			rs.close();
 
		}catch(Exception e){
			e.printStackTrace();
			log.error("getTasks...e:"+e.getMessage());
		}finally{
			releaseLock();
		}
		
		return tasks;
	}
	
	
	public ArrayList<LoadBalancingQueueItem> getAllTasks(){				
		return getTasks("SELECT * from queue ORDER BY trigger_time");
	}
	
	public Collection<LoadBalancingQueueItem> getExecutingTasks(){
		return getTasks("SELECT * from queue WHERE queuegate="+LoadBalancingHSQLLayerDB.QUEUEGATE_RUNNING+" ORDER BY trigger_time");

	}
	public Collection<LoadBalancingQueueItem> getQueuedTasks(){
		return getTasks("SELECT * from queue WHERE queuegate="+LoadBalancingHSQLLayerDB.QUEUEGATE_QUEUED+" ORDER BY trigger_time");
	}
	
	
	/**
	 * @deprecated
	 */
	public void cleanupProccesingQueue(int schedulerid, String computername) {
		
		
		 
	}

	

	
	
	protected LoadBalancingHSQLQueueItem getLBQItem(ResultSet rs)  throws Exception {
		
		LoadBalancingHSQLQueueItem item=new LoadBalancingHSQLQueueItem();
		item.setId(rs.getInt("id"));
		item.setSchedulerid(rs.getInt("schedulerid"));	
		item.setExecuting(rs.getBoolean("executing"));
		item.setMachine(rs.getString("machine"));
		if(rs.getTimestamp("started")!=null){
			Date d=new Date(rs.getTimestamp("started").getTime());			
			item.setStarted(d);
		}		
		item.setLastExecutedDuration(rs.getLong("lastExecutedDuration"));
		item.setOverlaptimeout(rs.getLong("overlaptimeout"));
		item.setInject_code(rs.getString("inject_code") );
		item.setTimeoutexpiry(rs.getLong("timeoutexpiry"));
		item.setTrigger_time(rs.getLong("trigger_time"));
		item.setNexttrigger_time(rs.getLong("nexttrigger_time"));
		item.setStarted_time(rs.getLong("started_time"));
		item.setStatus(rs.getString("status"));
		item.setTaskuid(rs.getString("taskuid"));
		item.setQueuegate(rs.getByte("queuegate"));
		item.setDependentids(rs.getString("dependentids"));
		item.setDependentchecktime(rs.getString("dependentchecktime"));
		item.setWaitingfordp(rs.getBoolean("waitingfordp"));
		item.setStarted_peers(rs.getString("started_peers"));
		
		ScheduledTask task=new ScheduledTaskFactory().getTask(item.getTaskuid());
		StackFrame sf=new StackFrame(task, new HashMap());
		sf.setTrigger_time(item.getTrigger_time());
		sf.setNexttrigger_time(item.getNexttrigger_time());
		sf.setStarted_time(item.getStarted_time());		 
		item.setSf(sf);
		
		
		return item;
				
		 
	}
	
	
	private String createSchemaQuery(){
		String query="CREATE TABLE queue(";
			query+=" id IDENTITY,";
			query+=" schedulerid BIGINT,";
			query+=" executing BIT,";
			query+=" machine VARCHAR(250),";
			query+=" started DATETIME,";
			query+=" lastExecutedDuration BIGINT, ";
			query+=" overlaptimeout BIGINT, ";
			query+=" inject_code VARCHAR(250),";
			query+=" timeoutexpiry BIGINT, ";		
			query+=" trigger_time BIGINT, ";
			query+=" nexttrigger_time  BIGINT,";
			query+=" started_time BIGINT, ";	
			query+=" status VARCHAR(250),";
			query+=" lastTenderTime BIGINT, ";
			query+=" taskuid VARCHAR(250), ";
			query+=" queuegate TINYINT, ";
			query+=" dependentids VARCHAR(250), ";
			query+=" dependentchecktime VARCHAR(250), ";
			query+=" started_peers VARCHAR(250), ";
			query+=" waitingfordp BIT ";
		query+=")";		
		return query;
	}
	
	private static String nextLineChar="\r\n";
	
	private static String collectStack(StackTraceElement[] stacks) throws Exception {
		//StackTraceElement[] stacks=ex.getStackTrace();
		String rtn="";
		//if(msg!=null) rtn+=msg.trim()+nextLineChar;
		//rtn+="ERROR MSG:"+ex.getMessage()+nextLineChar;
		int startat=0;
		for(int i=0;i<stacks.length;i++){
		   if(stacks[i].getClassName().startsWith("com.fe.")){
			   startat=i;
		   }
		}
		int numbefore=2;
		startat=(startat>numbefore)?startat-numbefore:0;
		int counter=1;
		
 
	 
		
		for(int i=startat;i<stacks.length;i++){
			if((i>(startat+numbefore)) && !stacks[i].getClassName().startsWith("com.fe.")){
				
			}else{
				String space="";
				for(int ab=0;ab<counter;ab++) space+=" "; counter++;
				if(stacks[i].getClassName().startsWith("com.fe.")){
					space+="->";
				} 
				rtn+=space+""+stacks[i].getClassName()+"."+stacks[i].getMethodName()+"()"+nextLineChar;
			}
			
		}
		 
		return rtn;
	}
	
	
	/**
	 * @param scid_trigtimes
	 * @return
	 * @throws Exception
	 * @deprecated
	 * remove all instantly won't be good idea, as peer will take some milliseconds to add task into local queue, so the following query returns only items started a minute before.
	 * 
	 */
	
	
	public List<LoadBalancingHSQLQueueItem> getRunningButNotOnPeer(List<String> scid_trigtimes) {
		//'1735_1361512740000','1739_1361512740000'
		ArrayList<LoadBalancingHSQLQueueItem> rtn=new ArrayList<LoadBalancingHSQLQueueItem>();
		acquireLock();
		try{
			String wherecond="";
			for(String it:scid_trigtimes) {
				wherecond+=(wherecond.equals(""))
						?"'"+it+"'"
						:",'"+it+"'";			
			}
			if(!wherecond.equals("")){
				String query="SELECT * from queue WHERE cast(schedulerid AS VARCHAR(24))||'_'||CAST(trigger_time AS VARCHAR(25))  NOT IN ("+wherecond+") and queuegate=? AND started<dateadd('minute',-1,LOCALTIMESTAMP)";
				PreparedStatement ps=this.connection.prepareStatement(query);
				ps.setByte(1, QUEUEGATE_RUNNING);
				ResultSet rs=ps.executeQuery();
				while(rs.next()){
					LoadBalancingHSQLQueueItem lbq=getLBQItem(rs);
					rtn.add(lbq);
				}
				rs.close();
				ps.close();
			}
			
		}catch(Exception e){
			//throw e;
			log.error("getRunningButNotOnPeer() Error:"+e.getMessage());
		}finally{
			releaseLock();
		}
		return rtn;
	}
	
	
	
	public List<LoadBalancingHSQLQueueItem> getRunningMoreThan3Mins() {
		//'1735_1361512740000','1739_1361512740000'
		ArrayList<LoadBalancingHSQLQueueItem> rtn=new ArrayList<LoadBalancingHSQLQueueItem>();
		acquireLock();
		try{
				String query="SELECT * from queue WHERE queuegate=? AND started<dateadd('minute',-3,LOCALTIMESTAMP)";
				PreparedStatement ps=this.connection.prepareStatement(query);
				ps.setByte(1, QUEUEGATE_RUNNING);
				ResultSet rs=ps.executeQuery();
				while(rs.next()){
					LoadBalancingHSQLQueueItem lbq=getLBQItem(rs);
					rtn.add(lbq);
				}
				rs.close();
				ps.close();
			
		}catch(Exception e){
			//throw e;
			log.error("getRunningButNotOnPeer() Error:"+e.getMessage());
		}finally{
			releaseLock();
		}
		return rtn;
	}
	
	
	private void startTimer(){
		LoadBalancingHSQLLayerDB.timer=new Timer();
		TimerTask tm=new TimerTask() {			
			public void run() {
				try{
					LoadBalancingHSQLLayerDB.calculateQueueAndAlertTraffic();					
				}catch(Exception e){
					log.error("Error "+e.getMessage());
				}
			}
		};
		timer.schedule(tm, 60000, 60000); //start after 1 minute in every 1 minute
	}
	
	public static void setAlertRange(Map<String,  String> range) {
		alert_range=range;
	}
	protected static void calculateQueueAndAlertTraffic() throws Exception {
		
		
		Collection queue=LoadBalancingQueue.getHSQLQueue().getQueuedTasks();
		if(alert_range!=null && getOwnCache().get("alerted")==null && queue.size()>0){

			task_stat_max_waiting.clear();		
		
	    	
			for(Iterator<LoadBalancingHSQLQueueItem> it=queue.iterator();it.hasNext(); ){
				LoadBalancingHSQLQueueItem lq=(LoadBalancingHSQLQueueItem)it.next();
				
				//Map data=lq.getSf().getData();
		    	//String dids=(String)data.get(ScheduledTask.FIELD_DEPENDENCY_IDS);
		    	
		    	if(lq.getDependentids()==null){
		    		
		    		String taskuid=lq.getTaskuid();
					Long waiting=new Date().getTime()-lq.getTrigger_time();				
					if(task_stat_max_waiting.containsKey(taskuid)){				
						waiting=task_stat_max_waiting.get(taskuid)>waiting?task_stat_max_waiting.get(taskuid):waiting;				
					}
					task_stat_max_waiting.put(taskuid, waiting);
		    	}
				//String taskuid=lq
			}
			
		
			String bodymsg="";
			ScheduledTaskFactory stf=new ScheduledTaskFactory();
						
			SimpleDateFormat sdfh=new SimpleDateFormat("HH:mm:ss 'Hrs'"); sdfh.setTimeZone(TimeZone.getTimeZone("GMT"));
			SimpleDateFormat sdfm=new SimpleDateFormat("mm:ss 'Mins'"); sdfm.setTimeZone(TimeZone.getTimeZone("GMT"));
			
			Date d1=new Date();
			ArrayList groups=new ArrayList();
			for(String ky : alert_range.keySet() ){		
				
				Long al_range=null;
				try{
					al_range=Long.parseLong(alert_range.get(ky));
				}catch(Exception e){
					//error
				}
				if(al_range!=null && task_stat_max_waiting.containsKey(ky) && task_stat_max_waiting.get(ky)>(al_range*1000*60) ){
					ScheduledTask st=stf.getTask(ky);
					
					d1.setTime(task_stat_max_waiting.get(ky));				
					if(task_stat_max_waiting.get(ky)>=3600000){
						bodymsg+=st.getName()+"("+ sdfh.format(d1)+") ";
					}else{
						bodymsg+=st.getName()+"("+ sdfm.format(d1)+") ";
					}
					
					groups.add(st.getUniqueid());
					IElementAttributes att= getOwnCache().getDefaultElementAttributes();
					att.setMaxLifeSeconds((ALERT_FREQUENCY_MINUTES*60));
					getOwnCache().put("alerted", "alerted", att);

				}
			}	
			if(!bodymsg.equals("")){
				//System.out.println("WaitingQueueList.calculateQueueAndAlertTraffic() bodymsg: Task waiting :"+bodymsg);
				SimpleDateFormat format=new SimpleDateFormat("hh:mm:ss a");
				bodymsg+="<div style='height:10px; background-color:#c0c0c0;text-align:center;margin:20px 0px 0px 0px;'>Screen shot at "+format.format(new Date())+"</div>";
				ResourceBundle rb=ResourceBundle.getBundle("com.fe.scheduler.scheduleralert");
				
				Logger log = LogManager.getLogger(LoadBalancingHSQLLayerDB.class.getName());
				
				log.debug("---******00000--->Alerting queue jammed, bodymsg:"+bodymsg);				
				SchedulerDB sdb=SchedulerDB.getSchedulerDB();
				try{					
					sdb.connectDB();
					LoadBalancingQueueTimeout lbqt=new LoadBalancingQueueTimeout(sdb, stf.getTaskUids());
					String theme=lbqt.getAlert_theme();
					String type=lbqt.getAlert_type();

					String sfolder_link=rb.getString("screenshot.folder.link");
					String subj=rb.getString("alert.subject");
					String filename=null;
					try{
						filename=captureQueue(groups);
						if(filename!=null)	filename=sfolder_link+filename+".png";				
						log.debug("filename:"+filename);
					}catch(Exception e){
						bodymsg+="<small>Couldn't capture screenshot, Error:"+e.getMessage()+"</small>";
					}
					ArrayList<String> themes=new ArrayList(); 
					themes.add(theme);
					
					// convert String array list to ThemeVO array list required by Alarm.sendAlarm() :
					ArrayList<ThemeVO> themeList = new ArrayList<ThemeVO>();
					for (int i=0; i<themes.size(); i++) {
						themeList.add(new ThemeVO(themes.get(i)));
					}
					
					Alarm.sendAlarm( themeList, "email".equalsIgnoreCase(type) ? AlarmType.EMAIL : AlarmType.PHONE, subj, bodymsg, false, true, false, filename,null);			
					
				}catch(Exception e){
					e.printStackTrace();
					//System.out.println("Error WaitingQueueList.calculateQueueAndAlertTraffic(): e:"+e.getMessage());
					
				}finally{
					sdb.closeDB();
				}
				
			}
		    
		}
		//System.out.println("WaitingQueueList.calculateQueueAndAlertTraffic() Queue stat:"+taskstat);
	}
	
	private static String captureQueue(List<String> groups) throws Exception {
		
		Logger log = LogManager.getLogger(LoadBalancingHSQLLayerDB.class.getName());
	
		ResourceBundle rb=ResourceBundle.getBundle("com.fe.scheduler.scheduleralert");
	
	   String gstring=null;
	   for(String group:groups) gstring=(gstring==null)?group:gstring+","+group;
	
	   String filename=RandomString.getString(10);
	   String cp_file=rb.getString("casper.commandfile");
	   String s_queuecapture=rb.getString("script.queue_capture");
	   String sfolder=rb.getString("screenshot.folder");	
	   String s_url=rb.getString("scheduler.url");
	   //String command="casperjs.bat Z:\\IT\\casperjs_scripts\\online_peers.js --filename=Z:\\IT\\casperjs_scripts\\files\\"+filename+" --group="+gstring+" --url=http://4ecapsvsg2:8080/bldb/scheduler.jsp";
	   String enable_ssh=rb.getString("ssh.enable");
	   String ssh_ip=rb.getString("ssh.ipaddress");
			   
	   String command;
	   String result=null;
	   if(enable_ssh.equalsIgnoreCase("true")){
		   //command="ssh "+ssh_ip+" -x '"+cp_file+" "+ s_queuecapture+" --filename="+sfolder+filename+" --group="+gstring+" --url="+s_url+"'";
		   command=cp_file+" "+ s_queuecapture+" --filename="+sfolder+filename+" --group="+gstring+" --url="+s_url;
		   JSch jsch=new JSch();
			 jsch.setConfig("StrictHostKeyChecking", "no");
			 jsch.addIdentity("/root/.ssh/id_dsa");
			 Session session=jsch.getSession("root", ssh_ip, 22);
			 session.connect();			 
			 
			 ChannelExec channel=(ChannelExec)session.openChannel("exec");
		     channel.setCommand(command);
		 
		     channel.setInputStream(null);
		      
		     channel.setErrStream(System.err);
		 
		     InputStream in=channel.getInputStream();
		 
		     channel.connect();
		
		     String line;
		      result="";
		     BufferedReader br2  = new BufferedReader(new InputStreamReader(in));
		      while ((line = br2.readLine()) != null)   {
		    	  result+=line+(result.equals("")?"":"\n");			  
			   }	
		     
		     channel.disconnect();
		     session.disconnect();			 
		      
	   }else{
		  
		   command=cp_file+" "+ s_queuecapture+" --filename="+sfolder+filename+" --group="+gstring+" --url="+s_url;
		   BufferedReader br2=null;
		   try{
			   Runtime runtime = Runtime.getRuntime();		   
			   Process process = runtime.exec(command);
			   br2 = new BufferedReader(new InputStreamReader(process.getInputStream()));
		   }catch(Exception e){
			   e.printStackTrace();
			   throw e;
		   }
		   //String line; String content=command+" ("+outf.format(new Date())+")-->";
		   String line;
		   result="";
		   while ((line = br2.readLine()) != null)   {
			   result+=line+(result.equals("")?"":"\n");		
		   }	
	   }
	   
	   log.debug("command:"+command);	   	   
	   //System.out.println("LoadBalancingHSQLLayerDB.captureQueue() (java) command:"+command);  	  	   
	       
	   if(result.contains("queue captured")){
		   return filename;
	   }else{
		   log.error("couldn't capture screenshot, casperjs reports:"+result);
		   throw new Exception("Captured output:"+result);
		   //return null;
	   }
	   
	}
	
	private static String captureQueue_old	(List<String> groups) throws Exception {
		
			Logger log = LogManager.getLogger(LoadBalancingHSQLLayerDB.class.getName());
		
			ResourceBundle rb=ResourceBundle.getBundle("com.fe.scheduler.scheduleralert");
		
		   String gstring=null;
		   for(String group:groups) gstring=(gstring==null)?group:gstring+","+group;
		
		   String filename=RandomString.getString(10);
		   String cp_file=rb.getString("casper.commandfile");
		   String s_queuecapture=rb.getString("script.queue_capture");
		   String sfolder=rb.getString("screenshot.folder");	
		   String s_url=rb.getString("scheduler.url");
		   //String command="casperjs.bat Z:\\IT\\casperjs_scripts\\online_peers.js --filename=Z:\\IT\\casperjs_scripts\\files\\"+filename+" --group="+gstring+" --url=http://4ecapsvsg2:8080/bldb/scheduler.jsp";
		   String enable_ssh=rb.getString("ssh.enable");
		   String ssh_ip=rb.getString("ssh.ipaddress");
				   
		   String command;
		   if(enable_ssh.equalsIgnoreCase("true")){
			   command="ssh "+ssh_ip+" -x '"+cp_file+" "+ s_queuecapture+" --filename="+sfolder+filename+" --group="+gstring+" --url="+s_url+"'";
		   }else{
			   command=cp_file+" "+ s_queuecapture+" --filename="+sfolder+filename+" --group="+gstring+" --url="+s_url;
		   }
		   
		   log.debug("command:"+command);	   
		   
		   System.out.println("LoadBalancingHSQLLayerDB.captureQueue() (java) command:"+command);
		   BufferedReader br2=null;
		   try{
			   Runtime runtime = Runtime.getRuntime();		   
			   Process process = runtime.exec(command);
			   br2 = new BufferedReader(new InputStreamReader(process.getInputStream()));
		   }catch(Exception e){
			   e.printStackTrace();
			   throw e;
		   }
		   //String line; String content=command+" ("+outf.format(new Date())+")-->";
		   String line;
		   String content="";
		   while ((line = br2.readLine()) != null)   {
			   content+="\n"+line;
		   }	   		   
		   log.debug("captureQueue():::content:"+content);		   
		   if(content.contains("queue captured")){
			   return filename;
		   }else{
			   log.error("couldn't capture screenshot, casperjs reports:"+content);
			   throw new Exception("Captured output:"+content);
			   //return null;
		   }
		   
	}
	
	
	private static JCS getOwnCache() throws Exception {
		 if(cache==null){
				cache=JCS.getInstance(WaitingQueueList.class.getName());
		 }
		 return cache;
   }
}


