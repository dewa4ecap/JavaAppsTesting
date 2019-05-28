/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.peer;

import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fourelementscapital.db.SchedulerDB;
import com.fourelementscapital.scheduler.engines.SchedulerExePlanLogs;
import com.fourelementscapital.scheduler.engines.StackFrame;
import com.fourelementscapital.scheduler.exception.ExceptionPeerRejected;
import com.fourelementscapital.scheduler.p2p.P2PService;

public abstract class QueueAbstract {
	
	private long lastExcecutedTime;	
	private Logger log = LogManager.getLogger(QueueAbstract.class.getName());
	
	private ConcurrentLinkedQueue threads=new ConcurrentLinkedQueue();
	
	private static Semaphore queueCheckLock=new Semaphore(1,true);
	private static final long TIMEOUT_MS=1000;
	
	private void acquireLock(){
		
		try{
			 
			QueueAbstract.queueCheckLock.tryAcquire(TIMEOUT_MS, TimeUnit.MILLISECONDS);
			 
			//LoadBalancingHSQLLayerDB.dblock.acquire();
		}catch(Exception e){
			log.error("Error:"+e.getMessage());
		}
	}
	

	private void releaseLock(){
		
		try{			
			QueueAbstract.queueCheckLock.release();
			//log.debug("....releasing lock: thread:"+Thread.currentThread().getId());
		}catch(Exception e){
			log.error("Error:"+e.getMessage());
		}
	}
	
	
	
	
	private String name=null; 
	public QueueAbstract(String name){
		this.name=name;		
	}
	
	
	public String getName(){
		return this.name;
	}
	
	public abstract Vector getTaskUids(); 
	public abstract int getConcurrentThreads();	 	

	protected void setLastExecutedTime(long date){
		this.lastExcecutedTime=date;
	}
		
	public long getLastExecutedTime(){
		return this.lastExcecutedTime;
	}
	
	
	public synchronized boolean isRoomForThread(){
	 
			acquireLock();
			log.debug("====>> currently "+this.threads+" in queue and maximum in the Q can be "+this.getConcurrentThreads());
			if(this.threads.size()<this.getConcurrentThreads() && !QueueFactory.restartRequested){
				try{
					//this is to make sure that the tasks are equality distributed on multi-thread running peers.
					if(this.threads.size()>0){
						releaseLock();
						Thread.sleep(200*this.threads.size());
					}else{
						releaseLock();
					}
				}catch(Exception e){
					log.error("Error while delaying thread");
				}
				 
				return true;
			}else{
				releaseLock(); 
				return false;
			}
		 
	}

	
	public Object[] getExecutingStacks() {		
		return this.threads.toArray();		  
	}

	public int getExecutingStacksSize() {
		return this.threads.size();		  
	}

	
	public synchronized void addExThread(StackFrame frame) throws Exception  {
		acquireLock();
		try{
			if(this.threads.size()<this.getConcurrentThreads() && !QueueFactory.restartRequested){
	
				Number nid=null;
				if(frame.getData()!=null){
					nid=(Number)frame.getData().get("id");
					new SchedulerExePlanLogs(nid.intValue(),frame.getTrigger_time()).log("Task added into local queue at client side",SchedulerExePlanLogs.PEER_OK_ADDED_PEER_QUEUE);
				}
				
				this.threads.add(frame);
				
				Thread thread=new Thread(new QueueExeThread(this,frame));
				thread.start();			
	
				if(frame.getData()!=null){
					SchedulerDB sdb=SchedulerDB.getSchedulerDB();
					try{					
						sdb.connectDB();			
						sdb.addPeerThreadStatus(P2PService.getComputerName(), this.name, frame.getTask().getUniqueid(), this.getConcurrentThreads(), nid.intValue());
					}catch(Exception e){ log.error("error:"+e.getMessage()); }finally{
						try{sdb.closeDB();}catch(Exception e1){log.error("error:"+e1.getMessage());	}
					}
				}
				 
			}else{
				 throw new ExceptionPeerRejected(null);
			}
		
		}catch(Exception e){			
			log.error(e.getMessage());
			throw e;
		}finally{
			releaseLock();
		}
		
	}
		
	
	
	/**
	 * this updates the peer running status in the database.
	 * 
	 * @param frame
	 * 
	 */
	
	protected void finishedExec(StackFrame frame){
		acquireLock();
		try{
			this.threads.remove(frame);
		}catch(Exception e){
			log.error(e.getMessage());
		}finally{
			releaseLock();
		}
		if(frame.getData()!=null){
			SchedulerDB sdb=SchedulerDB.getSchedulerDB();
			try{	
				Number nid=(Number)frame.getData().get("id");
				sdb.connectDB();			
				sdb.removePeerThreadStatus(P2PService.getComputerName(),nid.intValue());
			}catch(Exception e){ }finally{
				try{sdb.closeDB();}catch(Exception e1){log.error("error:"+e1.getMessage());	}
				if(QueueFactory.restartRequested){
					//RestartTomcat.restartPeerLater();
				}
			}
		}
	}
	
	 
}


