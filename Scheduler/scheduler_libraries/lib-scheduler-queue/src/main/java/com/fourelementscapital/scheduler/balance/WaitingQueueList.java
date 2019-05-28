/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.balance;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.jcs.JCS;
import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fourelementscapital.alarm.Alarm;
import com.fourelementscapital.alarm.AlarmType;
import com.fourelementscapital.alarm.ThemeVO;
import com.fourelementscapital.db.SchedulerDB;
import com.fourelementscapital.scheduler.ScheduledTaskFactory;
import com.fourelementscapital.scheduler.engines.ScheduledTask;

public class WaitingQueueList<E> extends ConcurrentLinkedQueue {

	private static TreeMap<String, Long> task_stat_max_waiting=new TreeMap<String, Long>();
	
	private static Timer timer=null;
	private static Map<String, String> alert_range=null;
	private static JCS cache=null;
	private Logger log = LogManager.getLogger(WaitingQueueList.class.getName());
	private static int ALERT_FREQUENCY_MINUTES=5; 
	private static WaitingQueueList localqueue=null;
	
	public boolean add(Object itm){
		boolean rtn=super.add(itm);		

		if(WaitingQueueList.localqueue==null){
			WaitingQueueList.localqueue=this;
		}
		
		//try{
		//	calculateQueueAndAlertTraffic();
		//}catch(Exception e){
		//	Log.debug("Error while alerting the scheduelr queue traffic, Error:"+e.getMessage());
		//}
		if(WaitingQueueList.timer==null){
			startTimer();
		}
	
		
		return rtn;
	}
	
	public static void setAlertRange(Map<String,  String> range) {
		alert_range=range;
	}
	
	public boolean remove(Object itm){
		boolean rtn=super.remove(itm);
		//try{
		//	calculateQueueAndAlertTraffic();
		//}catch(Exception e){
		//	Log.debug("Error while alerting the scheduelr queue traffic, Error:"+e.getMessage());
		//}
		return rtn;
	}
	
	protected static void calculateQueueAndAlertTraffic() throws Exception {
		
		if(alert_range!=null && getCache().get("alerted")==null && WaitingQueueList.localqueue.size()>0){

			task_stat_max_waiting.clear();		
		
	    	
			for(Iterator<LoadBalancingQueueItem> it=WaitingQueueList.localqueue.iterator();it.hasNext(); ){
				LoadBalancingQueueItem lq=(LoadBalancingQueueItem)it.next();
				
				Map data=lq.getSf().getData();
		    	String dids=(String)data.get(ScheduledTask.FIELD_DEPENDENCY_IDS);
		    	
		    	if(dids==null){
		    		String taskuid=lq.getSf().getTask().getUniqueid();
					Long waiting=new Date().getTime()-lq.getSf().getTrigger_time();
				
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
					
					IElementAttributes att= getCache().getDefaultElementAttributes();
					att.setMaxLifeSeconds((ALERT_FREQUENCY_MINUTES*60));
				    getCache().put("alerted", "alerted", att);

				}
			}	
			if(!bodymsg.equals("")){
				//System.out.println("WaitingQueueList.calculateQueueAndAlertTraffic() bodymsg: Task waiting :"+bodymsg);
				SchedulerDB sdb=SchedulerDB.getSchedulerDB();
				try{					
					sdb.connectDB();
					LoadBalancingQueueTimeout lbqt=new LoadBalancingQueueTimeout(sdb, stf.getTaskUids());
					String theme=lbqt.getAlert_theme();
					String type=lbqt.getAlert_type();
					String subj="Slow moving of Scheduler Queue";
					
					ArrayList<String> themes=new ArrayList(); 
					themes.add(theme);
					
					// convert String array list to ThemeVO array list required by Alarm.sendAlarm() :
					ArrayList<ThemeVO> themeList = new ArrayList<ThemeVO>();
					for (int i=0; i<themes.size(); i++) {
						themeList.add(new ThemeVO(themes.get(i)));
					}
					
					Alarm.sendAlarm( themeList, "email".equalsIgnoreCase(type) ? AlarmType.EMAIL : AlarmType.PHONE, subj, bodymsg, false, true, false, null,null);
					
					
				}catch(Exception e){
					System.out.println("Error WaitingQueueList.calculateQueueAndAlertTraffic(): e:"+e.getMessage());
				}finally{
					
				}
				
			}
		    
		}
		//System.out.println("WaitingQueueList.calculateQueueAndAlertTraffic() Queue stat:"+taskstat);
	}

	private static JCS getCache() throws Exception {
		 if(cache==null){
				cache=JCS.getInstance(WaitingQueueList.class.getName());
		 }
		 return cache;
	 }
 
	private void startTimer(){
		WaitingQueueList.timer=new Timer();
		TimerTask tm=new TimerTask() {			
			public void run() {
				try{
					WaitingQueueList.calculateQueueAndAlertTraffic();					
				}catch(Exception e){
					log.error("Error "+e.getMessage());
				}
			}
		};
		timer.schedule(tm, 60000, 60000); //start after 1 minute in every 1 minute
	}
}

