/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mozilla.javascript.edu.emory.mathcs.backport.java.util.Arrays;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;

import com.fourelementscapital.db.SchedulerDB;
import com.fourelementscapital.db.vo.ValueObject;
import com.fourelementscapital.scheduler.balance.LoadBalancingQueue;
import com.fourelementscapital.scheduler.balance.LoadBalancingQueueItem;
import com.fourelementscapital.scheduler.config.Config;
import com.fourelementscapital.scheduler.engines.ScheduledTask;
import com.fourelementscapital.scheduler.engines.StackFrame;
import com.fourelementscapital.scheduler.error.ClientError;
import com.fourelementscapital.scheduler.p2p.P2PService;

public class SchedulerEngine {

	private Logger log = LogManager.getLogger(SchedulerEngine.class.getName());
	
	public static final String SCHEDULE_TASK_GROUP="MY_SCHEDULER_Group";
	public static final String SCHEDULE_TASK_TIMEOUT_GROUP="MY_SCHEDULER_TIMEOUT_Group";	
	 
	public static final String JOBDATA_UPDATED_TIME="jobdata_updatedtime";
	public static final String JOBDATA_INVOKED_BY="jobdata_invoked_by";
	public static final String JOBDATA_TRIGGER_ROW_ID="trigger_row_id";
	
	
	private String generateCronExpr(Map data) throws Exception {
		
		String cronex="";
		if(data.get("exp_second")!=null && !data.get("exp_second").equals("")){
			cronex+=data.get("exp_second");
		}else{
			cronex+=" 0";
		}

		if(data.get("exp_minute")!=null && !data.get("exp_minute").equals("")){
			cronex+=" "+data.get("exp_minute");
		}else{
			cronex+=" 0";
		}
		if(data.get("exp_hour")!=null && !data.get("exp_hour").equals("")){
			cronex+=" "+data.get("exp_hour");
		}else{
			cronex+=" *";
		}

		boolean day_question_mark=false;
		if(data.get("exp_day")!=null && !data.get("exp_day").equals("")){
			cronex+=" "+data.get("exp_day");
		}else{
			cronex+=" ?";
			day_question_mark=true;
		}

		if(data.get("exp_month")!=null && !data.get("exp_month").equals("")){
			cronex+=" "+data.get("exp_month");
		}else{
			cronex+=" *";
		}
		
		if(data.get("exp_week")!=null && !data.get("exp_week").equals("")){
			cronex+=" "+data.get("exp_week");
		}else{			
			cronex+=(day_question_mark)? " *":" ?";
		}
		return cronex;
	}
	
	
	private String getNext5Times(Trigger trigger, TimeZone timez)  throws Exception {
		

    	Calendar nextmin=Calendar.getInstance();    	
    	SimpleDateFormat format=new SimpleDateFormat("dd MMM, yyyy hh:mm:ss a");    	
    	SimpleDateFormat convert1=new SimpleDateFormat("dd MMM, yyyy HH:mm:ss");
    	
    	//String localtime=null;
    	//if(timezone!=null && !timezone.equals("")){
    	
    	convert1.setTimeZone(timez);
    	
    	//localtime=trigger.getTimeZone().getDisplayName();
    	
    	//}else{
    	//	localtime=TimeZone.getDefault().getDisplayName();
    	//}
    	
    	SimpleDateFormat convert2=new SimpleDateFormat("dd MMM, yyyy HH:mm:ss");    	 
    	SimpleDateFormat today_tomm=new SimpleDateFormat("hh:mm:ss a");
    	String rtn="";

    	//rtn+="--------------------------------------\n";
    	int count=0;
    	
    	Date nextfirtime=null;
    	if(trigger.getPreviousFireTime()!=null){
    		nextfirtime=trigger.getPreviousFireTime();
    	}else{
    		nextfirtime=trigger.getStartTime();
    	}
    	
    	while(trigger.getFireTimeAfter(nextfirtime)!=null && count<5){    
    		
    		
    		Date noccur1=trigger.getFireTimeAfter(nextfirtime);
    		Date noccur=convert2.parse(convert1.format(noccur1));
    		
    		Date now1=new Date();
    		Date now=convert2.parse(convert1.format(now1));
    		
    		
    		
    		Calendar cal1=Calendar.getInstance(); cal1.setTime(noccur);    		
    		Calendar cal2=Calendar.getInstance(); cal2.setTime(now);
    		
    		String line=null;
    		if(
    			cal1.get(Calendar.DAY_OF_MONTH)==cal2.get(Calendar.DAY_OF_MONTH) &&
    			cal1.get(Calendar.MONTH)==cal2.get(Calendar.MONTH) &&
    			cal1.get(Calendar.YEAR)==cal2.get(Calendar.YEAR) 
    		){
    			line="Today at "+today_tomm.format(noccur);
    		}
    		
   		
    		Calendar cal3=Calendar.getInstance(); cal3.setTime(now);
    		cal3.add(Calendar.DAY_OF_MONTH, 1);
    		if(		    				
    				cal1.get(Calendar.DAY_OF_MONTH)==cal3.get(Calendar.DAY_OF_MONTH) &&
	    			cal1.get(Calendar.MONTH)==cal3.get(Calendar.MONTH) &&
	    			cal1.get(Calendar.YEAR)==cal3.get(Calendar.YEAR)
	    		){
    			line="Tomorrow at "+today_tomm.format(noccur);
	    	}
    		
    		
    		String rtn1=null;
    		if(line==null){
    			rtn1="\n"+format.format(noccur);
    		}else{
    			rtn1="\n"+line;
    		}  		
    		//System.out.println("calcuating:"+rtn1);
    		nextfirtime=trigger.getFireTimeAfter(nextfirtime);
    		rtn+=rtn1;
    		count++;
    	}
    	return rtn;
		
	}
	
	
	public String getNext5Times(String taskname, String taskuid, TimeZone timezone,int scheduler_id) throws Exception  {
		String job_tri_name=generateUniqueJobName(taskname,taskuid,scheduler_id,0);
		SchedulerFactory sf=new StdSchedulerFactory();
		Scheduler scheduler=sf.getScheduler();		

		if(!scheduler.isStarted()){
			scheduler.start();
		}		 
		Trigger trigger = scheduler.getTrigger(new TriggerKey(job_tri_name, SCHEDULE_TASK_GROUP ));		
		//Trigger trigger=scheduler.getTrigger(job_tri_name,SCHEDULE_TASK_GROUP +taskuid);
		
		if(trigger!=null){
			String rtn=getNext5Times(trigger, timezone) ;
			return rtn;
		}else{
			return null;
		}
	}
	
	/*
	public static Vector getEnabledTaskTypes(){
		String services=Config.getString("scheduler_services_on");		
	    StringTokenizer st=new StringTokenizer(services,",");
	    Vector vservices=new Vector();
	    while(st.hasMoreTokens()){
	    	String token=st.nextToken();
	    	vservices.add(token);
	    }
	    return vservices;
	}
	*/
	
	public static Vector getEnabledTaskTypes() {
		
	    Vector vservices=new Vector();
	    SchedulerDB sdb=SchedulerDB.getSchedulerDB();		
	    try{
	    	sdb.connectDB();	
		    Vector tasks=sdb.getActiveGroups();
		    for (Iterator i=tasks.iterator();i.hasNext();){
		    	Map data=(Map)i.next();
		    	//String token=st.nextToken();
		    	vservices.add(data.get("taskuid"));
		    }
		    }catch(Exception e){
	    	
	    }finally{
	    	try{
	    		sdb.closeDB();
	    	}catch(Exception e){}
	    }
	    return vservices;
	}
	
	
	public void startSchedulerQueue() throws Exception {
		log.debug("startSchedulerQueue() called ()");	
		log.debug("scheduler queue called at:"+new Date());
		
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		sdb.connectDB();
		List<Map> datalist=sdb.listScheduler();

		Vector vs=getEnabledTaskTypes();
		String tasktypes="";
		for(Map data : datalist){		
			String taskuid=(String)data.get("taskuid");
			log.debug("taskuid:"+taskuid);
			if(vs.contains(taskuid)){
				tasktypes+=(tasktypes.equals("") ? "'"+taskuid+"'":",'"+taskuid+"'");
			}
		}
		sdb.removeQueueLogs(new Date().getTime(),tasktypes);

		log.debug("vs:"+vs);		
		for(Map data : datalist){		
			String taskuid=(String)data.get("taskuid");
			log.debug("taskuid:"+taskuid);
			if(vs.contains(taskuid)){
				Number number=(Number)data.get("id");
				Map data1=sdb.getScheduler(number.intValue());
				try{
					updateJob(data1,taskuid,sdb);
				}catch(Exception e){
					log.error("Error while adding task id:"+number+" to the queue");
					//ClientErrorMgmt.reportError(e, "Error while adding task id:"+number+" to the queue");
				}
			}
		}
		sdb.closeDB();
	}

	
	
	
	public void removeGroup2Queue(String taskuid) throws Exception {
		
		 
		
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		sdb.connectDB();
		log.debug("removeGroup2Queue() called");
		try{
			List<Map> datalist=sdb.listAllTasksByUID(taskuid);
			Vector vs=getEnabledTaskTypes();
			if(!vs.contains(taskuid)){		
				
				
				
				sdb.removeQueueLogs(new Date().getTime(),"'"+taskuid+"'");		
				//log.debug("vs:"+vs);		
				//for(Map data : datalist){		
				//	Number number=(Number)data.get("id");
				//	Map data1=sdb.getScheduler(number.intValue());
				//	updateJob(data1,taskuid,sdb);
				//}
				SchedulerFactory sf=new StdSchedulerFactory();
				Scheduler scheduler=sf.getScheduler();		
				if(!scheduler.isStarted()){
					throw new Exception("Removing tasks failed, scheduler is not started first of all");
				}
				new ScheduledTaskFactory().refreshTaskLoaded();
				
				//quartz 1.8.x to quartz 2.1.1 migration
				/*
			    String jobnames[]=scheduler.getJobNames(SCHEDULE_TASK_GROUP+taskuid);			    
			    for(int i=0;i<jobnames.length;i++){			    	
			    	 JobDetail ojob=scheduler.getJobDetail(jobnames[i],SCHEDULE_TASK_GROUP+taskuid );
			    	 
					 if(ojob!=null){
						 log.debug("Removing job from the queue: name:"+jobnames[i]);
						 scheduler.deleteJob(jobnames[i],SCHEDULE_TASK_GROUP+taskuid);				
					 }
			    }
			    */
				
			   // String jobnames[]=scheduler.getJobNames(SCHEDULE_TASK_GROUP+taskuid);
			    
				Set<JobKey> jobs= scheduler.getJobKeys(GroupMatcher.jobGroupEquals(SCHEDULE_TASK_GROUP));
			    for(Iterator<JobKey> i=jobs.iterator();i.hasNext();){
			    	 JobKey jky=i.next();
			    	 //JobDetail ojob=scheduler.getJobDetail(jobnames[i],SCHEDULE_TASK_GROUP+taskuid );
			    	 JobDetail ojob=scheduler.getJobDetail(jky);
			    	 
					 if(ojob!=null){
						 //log.debug("Removing job from the queue: name:"+jobnames[i]);
						 scheduler.deleteJob(jky);				
					 }
			    }
			    
				
			}else{
				throw new Exception("Taskuid:"+taskuid+" is active, please deactivate before remove them from queue");
			}
		}catch(Exception e){
			throw e;
		}finally{
			sdb.closeDB();
		}
		
	}
	
	
	public void addGroup2Queue(String taskuid) throws Exception {
		
		
		log.debug("addGroup2Queue() called");
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		sdb.connectDB();
		try{
			List<Map> datalist=sdb.listAllTasksByUID(taskuid);
			Vector vs=getEnabledTaskTypes();
			if(vs.contains(taskuid)){			
				sdb.removeQueueLogs(new Date().getTime(),"'"+taskuid+"'");		
				//log.debug("vs:"+vs);		
				new ScheduledTaskFactory().refreshTaskLoaded();
				
				for(Map data : datalist){		
					Number number=(Number)data.get("id");
					Map data1=sdb.getScheduler(number.intValue());
					log.debug("adding task into live queue:id:"+number);
					
					try{
						updateJob(data1,taskuid,sdb);
					}catch(Exception e){
						log.error("Error while adding task id:"+number+" to the queue");
						//ClientErrorMgmt.reportError(e, "Error while adding task id:"+number+" to the queue");
					}

				}
			}else{
				throw new Exception("Taskuid:"+taskuid+" is not active, please make it active before add them into queue");
			}
		}catch(Exception e){
			throw e;
		}finally{
			sdb.closeDB();
		}
		
	}
	


	/**
	 * 
	 * @param name
	 * @param taskuid
	 * @param scheduler_id
	 * @param sequence
	 * @return
	 */
	public String generateUniqueJobName(String name, String taskuid, int scheduler_id, int sequence){
		//this changed because, when task name or type changed the triggers are not updated once any of these changed.
		//return taskuid+"_"+name+"_"+scheduler_id+"_"+sequence;		
		return scheduler_id+"_"+sequence;
	}
	
	/*
	public String validateTaskData(Map data, String taskuid) throws Exception {
 
		//log.debug("cron Expression:"+cronex);			
		//JobDetail holds the definition for Jobs
		
		String cronex=generateCronExpr(data);
		String name1=(String)data.get("name");
		 

		String job_tri_name=generateTriggerJobName(name1,taskuid);
		
    	JobDetail jobDetail = new JobDetail(job_tri_name, SCHEDULE_TASK_GROUP,ScheduledTaskJob.class);			    	
    	jobDetail.getJobDataMap().put("data",data);
    	
    	//jobDetail.getJobDataMap().put("task",getTask(taskuid));
    	
    	String timezone=(String)data.get("timezone");
    	CronTrigger trigger=new CronTrigger(job_tri_name,SCHEDULE_TASK_GROUP,cronex);
    	if(timezone!=null && !timezone.equals("")){
    		trigger.setTimeZone(TimeZone.getTimeZone(timezone));
    	}
    	
    	String localtime=null;
    	if(timezone!=null && !timezone.equals("")){    	 
    		localtime=TimeZone.getTimeZone(timezone).getID();
    	}else{
    		localtime=TimeZone.getDefault().getID();
    	}
    	
    
    	String rtn="";
    	rtn="The followings are task execution time ("+((localtime!=null)?localtime:timezone)+") pattern:\n";
    	rtn+="--------------------------------------\n";
    	
    	rtn+=getNext5Times(trigger,trigger.getTimeZone());
    	
    	rtn+="\n\nWould you like to continue saving this schedule? \n\n";
    	return rtn;	
	}
	*/
	 
	
	
	
	public void updateJob(Map data, String taskuid, SchedulerDB sdb) throws Exception {
		
		if(Config.getValue("load_balancing_server")!=null && Config.getValue("load_balancing_server").equals(P2PService.getComputerName())){
			 
		}else{
			throw new Exception("Couldn't update as this computer configuration doesn't support load balancing");
		}
		
		SchedulerFactory sf=new StdSchedulerFactory();
		Scheduler scheduler=sf.getScheduler();			

		if(!scheduler.isStarted()){
				scheduler.start();
		}
	
	    int scheduler_id=((Number)data.get("id")).intValue();
		String name=(String)data.get("name");
		
		
        for(int i=0;i<1000;i++){
        	String job_tri_name=generateUniqueJobName(name,taskuid,scheduler_id,i);
		    //JobDetail ojob=scheduler.getJobDetail(job_tri_name,SCHEDULE_TASK_GROUP+taskuid );
        	JobDetail ojob=scheduler.getJobDetail(new JobKey(job_tri_name,SCHEDULE_TASK_GROUP));
			if(ojob!=null){
				//scheduler.deleteJob(job_tri_name,SCHEDULE_TASK_GROUP+taskuid);
				scheduler.deleteJob(new JobKey(job_tri_name,SCHEDULE_TASK_GROUP));
				log.debug("deleleting job:"+job_tri_name);		
			}else{
				i=1001;
			}
        }
        sdb.removeQueueLog(new Date().getTime(),scheduler_id);
        
		Vector trigdata=sdb.getTriggerData(scheduler_id);
		
		Number active=(Number)data.get("active");
		//activate only the active jobs.
		if(  (active==null || (active!=null && active.intValue()!=-1))
			  && trigdata!=null && trigdata.size()>0 
		   ){
			
			
			 
			
			//String cronex=generateCronExpr(data);	
			 

	    	int trigcount=0;
	    	for(Iterator i=trigdata.iterator();i.hasNext();){
	    		Map tdrow=(Map)i.next();
	    		String cronex=
				    			new SchedulerEngineUtils().generateCronExpr(
				    				(String)tdrow.get("exp_second"), 
				    				(String)tdrow.get("exp_minute"), 
				    				(String)tdrow.get("exp_hour"), 
				    				(String)tdrow.get("exp_week"), 
				    				(String)tdrow.get("exp_day"), 
				    				(String)tdrow.get("exp_month")
				    			);
	    		
	    		String job_tri_name=generateUniqueJobName(name,taskuid,scheduler_id,trigcount);
	    		 
		    	//JobDetail jobDetail = new JobDetail(job_tri_name,SCHEDULE_TASK_GROUP+taskuid ,ScheduledTaskJob.class);    
		    	//jobDetail.getJobDataMap().put("scheduler_id",scheduler_id);
		    	//jobDetail.getJobDataMap().put("taskuid",taskuid);
	    		
	    		SimpleDateFormat sdf=new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	    		
	    		JobDetail jobDetail = newJob(ScheduledTaskJob.class)
	    			    .withIdentity(job_tri_name, SCHEDULE_TASK_GROUP)	    			    
	    			    .usingJobData("scheduler_id", scheduler_id)
	    			    .usingJobData("taskuid", taskuid)
	    			    .usingJobData(SchedulerEngine.JOBDATA_TRIGGER_ROW_ID, ((Number)tdrow.get("id")).longValue())
	    			    .usingJobData(SchedulerEngine.JOBDATA_UPDATED_TIME,sdf.format(new Date()))	    			   
	    			    .build();
    			    
		    	String timezone=(String)data.get("timezone");
		    	
		    	/*
		    	CronTrigger trigger=new CronTrigger(job_tri_name,SCHEDULE_TASK_GROUP+taskuid,cronex);		    	 
		    	if(timezone!=null && !timezone.equals("")){
		    		trigger.setTimeZone(TimeZone.getTimeZone(timezone));
		    	}
		    	*/
		    	
		    	try{
			        CronScheduleBuilder csb=cronSchedule(cronex);
			        List list=Arrays.asList(TimeZone.getAvailableIDs());
			        if(timezone!=null && !timezone.trim().equals("") && list.contains(timezone.trim())) {
			        	csb.inTimeZone(TimeZone.getTimeZone(timezone));
			        }
			    	CronTrigger trigger=newTrigger()
			    			.withIdentity(job_tri_name,SCHEDULE_TASK_GROUP) 
			    			.withSchedule(csb)		    		
			    			.build();
			    	
			    	Calendar nextmin=Calendar.getInstance();	
			    	scheduler.scheduleJob(jobDetail, trigger );
			    	
			    	try{
				    	Number nid=(Number)data.get("id");
				    	TreeMap record=new TreeMap();
				    	record.put("scheduler_id", nid);
				    	record.put("trigger_time", new Long(trigger.getNextFireTime().getTime()));
				    	Vector v=new Vector();
				    	v.add(record);	    	
				    	sdb.updateQueueLog(v,new Vector(), P2PService.getComputerName());
			    	}catch(Exception e){
			    		ClientError.reportError(e, null);
			    	}
		    	}catch(Exception e){
		    		log.error("Trigger Expression "+cronex+" failed for scheduler,please review the cron syntax "+scheduler_id);
		    	}
		    	trigcount++;
	    	}
		}
		
	}
	
	
	public void runJobDelayed(Map data, String taskuid, SchedulerDB sdb, int delay_in_minutes, String invoked_by) throws Exception {
		
		SchedulerFactory sf = new StdSchedulerFactory();
		Scheduler scheduler = sf.getScheduler();

		if (!scheduler.isStarted()) {
			scheduler.start();
		}

		int scheduler_id = ((Number) data.get("id")).intValue();
		String name = (String) data.get("name");

		Calendar c = Calendar.getInstance();
		c.add(Calendar.MINUTE, delay_in_minutes);
		
		SimpleDateFormat format = new SimpleDateFormat("ddMMyyHHmm");
		
		// to avoid 2 tasks being added into the same time (minute), you need
		// minimum 1 minute distance between 2 task in queue
		log.debug("c:"+c.getTime());
		log.debug("format:"+format);
		log.debug("delay_in_minutes:"+delay_in_minutes);
		String job_tri_name = name + "_" + format.format(c.getTime());
		

		/*
		JobDetail jobDetail = new JobDetail(job_tri_name, SCHEDULE_TASK_GROUP + taskuid, ScheduledTaskJob.class);
		jobDetail.getJobDataMap().put("scheduler_id", scheduler_id);
		jobDetail.getJobDataMap().put("taskuid", taskuid);
		*/
		JobDetail jobDetail = newJob(ScheduledTaskJob.class)
			    .withIdentity(job_tri_name, SCHEDULE_TASK_GROUP)	    			    
			    .usingJobData("scheduler_id", scheduler_id)
			    .usingJobData("taskuid", taskuid)
			    .usingJobData(JOBDATA_INVOKED_BY,invoked_by)
			    .build();
		
		//String timezone = (String) data.get("timezone");
		// CronTrigger trigger=new
		// CronTrigger(job_tri_name,SCHEDULE_TASK_GROUP+taskuid,cronex);
		/*
		SimpleTrigger trigger = new SimpleTrigger(job_tri_name,
				SCHEDULE_TASK_GROUP + taskuid);
		trigger.setStartTime(c.getTime());
		trigger.setNextFireTime(c.getTime());
		trigger.setRepeatCount(0);
		*/
		
		Trigger trigger=newTrigger()
				          .withIdentity(job_tri_name, SCHEDULE_TASK_GROUP )
				          .startAt(c.getTime())
				          .endAt(c.getTime())
				          .build();
		
		
		
		//System.out.println("SchedulerEngine.runJobDelayed(): nextScheduledTime():"+format.format(trigger.getNextFireTime())+" name:"+job_tri_name);
		
		Calendar nextmin = Calendar.getInstance();
		scheduler.scheduleJob(jobDetail, trigger);

		try {
			
			Number nid = (Number) data.get("id");
			TreeMap record = new TreeMap();
			record.put("scheduler_id", nid);			
			//record.put("trigger_time", new Long(trigger.getNextFireTime()
			//		.getTime()));
			record.put("trigger_time", c.getTime().getTime());
			Vector v = new Vector();
			v.add(record);
			sdb.updateQueueLog(v,new Vector(), P2PService.getComputerName());
			
		} catch (Exception e) {			
			ClientError.reportError(e, null);
		}
		
		
	}
	
	public boolean isSchedulerStarted() throws Exception {
		SchedulerFactory sf=new StdSchedulerFactory();
		Scheduler scheduler=sf.getScheduler();			

		if(scheduler.isStarted()){
			return true;
		}else{
			return false;
		}
	}
	
	public void removeJob(long schedulerid, Map data, String taskuid) throws Exception {
		
		SchedulerFactory sf=new StdSchedulerFactory();
		Scheduler scheduler=sf.getScheduler();			

		if(!scheduler.isStarted()){
				scheduler.start();
		}
		
		String name=(String)data.get("name");
		

        for(int i=0;i<1000;i++){
        	String job_tri_name=generateUniqueJobName(name,taskuid, (int)schedulerid,i);
		    //JobDetail ojob=scheduler.getJobDetail(job_tri_name,SCHEDULE_TASK_GROUP+taskuid );
        	JobDetail ojob=scheduler.getJobDetail(new JobKey(job_tri_name,SCHEDULE_TASK_GROUP));
        	
			if(ojob!=null){
				//scheduler.deleteJob(job_tri_name,SCHEDULE_TASK_GROUP+taskuid);
				scheduler.deleteJob(new JobKey(job_tri_name,SCHEDULE_TASK_GROUP));
				log.debug("deleleting job:"+job_tri_name);
			}else{
				i=1001;
			}
        }
		
	}
	

	
	public synchronized String executeScriptExpression(String expression,String sender, String suffixcode) throws Exception  {
		
		ArrayList<ValueObject> v=new SchedulerEngineUtils().parseCodeInjection(expression); 
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		
		String rtn="";
		try{
			
			sdb.connectDB(); 
			String failed_ids=null;
			boolean success=false;
			//for(Iterator i=v.iterator();i.hasNext();){
			//	String sid=(String)i.next();
			for(ValueObject vo:v){
				log.debug("vo:key:"+vo.getKey());
			    int scheduler_id=Integer.parseInt(vo.getKey());
			    //sdb.connectDB();   
				Map data=sdb.getScheduler(scheduler_id);
				if(data!=null ){
					if(data.get("deleted")==null || (data.get("deleted")!=null && ((Number)data.get("deleted")).intValue()!=1 )) {
		    			Number active=(Number)data.get("active");	
		    			String taskuid=(String)data.get("taskuid");
		    			String name=(String)data.get("name");	
		    			String vovalue=(vo.getValue()==null)?"": vo.getValue();
		    			String inject=suffixcode!=null ? vovalue+"\n"+suffixcode:vovalue;
		    			log.debug("inject:"+inject);
		    			log.debug("vovalue:"+vovalue+" vo.key:"+vo.getKey()+" vo:"+vo.getValue());
	    				new SchedulerEngine().executeJobNow(name, taskuid,data,sdb,sender,inject);
	    				success=true;
					}else{
						//log.error("Scheduler ID:"+scheduler_id+" not exist or  deleted");
						failed_ids=(failed_ids==null)?scheduler_id+"":failed_ids+","+scheduler_id;
					}
	    			//sdb.closeDB();	    		
		  		 }else{
			      //throw new Exception("Invalid ID");
		  		 }
			}
			
			if(success)
		    	rtn="done "+(failed_ids!=null?", but "+failed_ids+" failed":"");
		    else
		    	rtn="no success";
		    
		
		    
			
			
			return rtn;
	 
		}catch(Exception e){
			
			throw e;
			
		}finally {
			try{
			sdb.closeDB();
			}catch(Exception e){}
		}
		
		
	}
	
	
	
	
	
	public synchronized void executeJobNow(String name, String taskuid, Map data, SchedulerDB sdb, String invoked_by, String inject_code) throws Exception {
		
		if(data.get("deleted")!=null && ((Number)data.get("deleted")).intValue()==1){
			throw new Exception("Deleted Task can't be executed");
		}
		
		
		SchedulerFactory sf=new StdSchedulerFactory();
		Scheduler scheduler=sf.getScheduler();			

		if(!scheduler.isStarted()){
				scheduler.start();
		}
	
		Number nid=(Number)data.get("id");
		
		//String name=(String)data.get("name");
		String job_tri_name=generateUniqueJobName(name,taskuid,nid.intValue(),0);
	    //JobDetail ojob=scheduler.getJobDetail(job_tri_name,SCHEDULE_TASK_GROUP );
		
	    Date trigtime=null;
		//if(ojob!=null){
		trigtime=new Date();
		//scheduler.triggerJob(job_tri_name,SCHEDULE_TASK_GROUP);	
 
			//ScheduledTask task1=(ScheduledTask)ojob.getJobDataMap().get("task");
		//Map data1=(Map)ojob.getJobDataMap().get("data");
		
		//if it not already in the execution queue.
		
		if(ScheduledTaskQueue.getQueuedTaskIds().contains(nid)){
			throw new Exception("This task is currently in the queue or already excuting...");
		}else{
			//StackFrame sframe=(StackFrame)ojob.getJobDataMap().get("stackframe");
			ScheduledTask task=new ScheduledTaskFactory().getTask(taskuid);
	    	StackFrame sframe=new StackFrame(task,data);
	    	sframe.setInvokedby(invoked_by);
	    	 
			sframe.setTrigger_time(new Date().getTime());			
			//Trigger trigger=scheduler.getTrigger(job_tri_name,SCHEDULE_TASK_GROUP+taskuid);
			
			
			Trigger trigger=scheduler.getTrigger(new TriggerKey(job_tri_name,SCHEDULE_TASK_GROUP));
			
			
			if(trigger!=null && trigger.getNextFireTime()!=null){
				sframe.setNexttrigger_time(trigger.getNextFireTime().getTime());
			}		

			//ScheduledTaskQueue.add(sframe);
			if(Config.getValue("load_balancing_server")!=null && Config.getValue("load_balancing_server").equals(P2PService.getComputerName())){
				LoadBalancingQueueItem li=new LoadBalancingQueueItem();
				li.setSf(sframe);
				Integer id=(Integer)data.get("id");
				li.setSchedulerid(id);
				li.setInject_code(inject_code);
				LoadBalancingQueue.getDefault().add(li);
				
			}else{
				ScheduledTaskQueue.add(sframe);
			}
	    	
	    	TreeMap record=new TreeMap();
	    	record.put("scheduler_id", nid);
	    	record.put("trigger_time",sframe.getTrigger_time());
	    	Vector v=new Vector();
	    	v.add(record);	    	
	    	sdb.updateQueueLog(v,new Vector(), P2PService.getComputerName());
	    	//sdb.closeDB();

	    	log.debug("triggering job:"+job_tri_name+" time:"+sframe.getTrigger_time()+" scheduler_Id:"+nid);
			
		}
		//this listener exected once.
			/*
			task.setAddhocListener(job_tri_name,new ScheduledTaskJobAdhocListener() {
				
				public void executeBeforeQueue(Trigger trigger, Map data){
				
					try{
						SchedulerDB sdb=new SchedulerDB();
		    			sdb.connectDB();
					 
						log.debug("triggering: current:"+trigger.getPreviousFireTime());
				    	Number nid=(Number)data.get("id");
				    	TreeMap record=new TreeMap();
				    	record.put("scheduler_id", nid);
				    	record.put("trigger_time", trigger.getPreviousFireTime().getTime());
				    	Vector v=new Vector();
				    	v.add(record);	    	
				    	sdb.updateQueueLog(v);
				    	sdb.closeDB();
				    	
			    	}catch(Exception e){
			    		ClientErrorMgmt.reportError(e, null);
			    	}
				}
			});
			*/
		
		//}
}
	
 
	/**
	 * @deprecated
	 * @return
	 * @throws Exception
	 */
	/*
	public Collection getQueueTest() throws Exception {
		
		SchedulerFactory sf=new StdSchedulerFactory();
		Scheduler scheduler=sf.getScheduler();
		//Vector r=new Vector();		
		//scheduler.shutdown();
		TreeSet r=new TreeSet();		
		SimpleDateFormat format=new SimpleDateFormat("dd MMM, yyyy HH:mm:ss");
		if(scheduler.isStarted()){
			
			String jobs[]=scheduler.getJobNames(SCHEDULE_TASK_GROUP);
			for(int i=0;i<jobs.length;i++){
				SQueuedItem record= new SQueuedItem();		
				Trigger t[]=scheduler.getTriggersOfJob(jobs[i],SCHEDULE_TASK_GROUP );
				Map data=(Map)scheduler.getJobDetail(jobs[i],SCHEDULE_TASK_GROUP).getJobDataMap().get("data");
				Integer id=(Integer)data.get("id");
				//log.debug("Job name:"+data.get("name"));
				String name=(String)data.get("name");
				record.put("id", id+"");
				record.put("name", name);
				for(int ia=0;ia<t.length;ia++){
					
					Date nf=t[ia].getNextFireTime();
					Date nf1=t[ia].getFireTimeAfter(nf);
					record.put("execute_dt", nf);
					record.put("execute_longtime", nf.getTime());
					record.put("execute_at", format.format(nf));
					record.put("execute_at1", format.format(nf1));					
					//if(dt1.get(id)!=null && nf.before(dt1.get(id))){
					//	dt1.put(id, nf);
					//}else if(dt1.get(id)==null){
					//	dt1.put(id, nf);
					//}
					r.add(record);
				}
				 
			}
			//for(Iterator i=dt1.keySet().iterator();i.hasNext();){
				//Integer ky=(Integer)i.next();
				//log.debug(" ID:"+ky+" next first occ:"+ dt1.get(ky));
			//}
			
		}else{
			throw new Exception("Scheduler isn't started yet");
		}
		return r;
	}
    */
	
	public class SQueuedItem  extends TreeMap implements Comparable {

		 
		public int compareTo(Object o) {
			 SQueuedItem o1=(SQueuedItem)o;
			 Date thisdate=(Date)get("execute_dt");
			 Date otherdate=(Date)o1.get("execute_dt");
			 
			 if(thisdate.after(otherdate)){
				 return 1;
			 }else{
				 return -1;
			 }
		}
		
		
	}
	
}


