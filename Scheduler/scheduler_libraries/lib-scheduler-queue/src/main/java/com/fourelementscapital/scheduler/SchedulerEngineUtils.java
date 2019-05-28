/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mozilla.javascript.edu.emory.mathcs.backport.java.util.Arrays;
import org.quartz.CronScheduleBuilder;
import org.quartz.Trigger;

import com.fourelementscapital.db.vo.ValueObject;

public class SchedulerEngineUtils {

	private Logger log = LogManager.getLogger(SchedulerEngineUtils.class.getName());
	
	public String generateCronExpr(String sec,String min, String hr, String dw,String dm,String mn) throws Exception {
		
		String cronex="";
		cronex+=(sec!=null && !sec.equals(""))?sec:" 0";
		cronex+=(min!=null && !min.equals(""))?" "+min:" 0";
		cronex+=(hr!=null && !hr.equals(""))?" "+hr:" *";		
		cronex+=(dm!=null && !dm.equals(""))?" "+dm:" ?"; 

		boolean day_question_mark=false;
		if(dm!=null && !dm.equals("")){}else{
			day_question_mark=true;
		}
		cronex+=(mn!=null && !mn.equals(""))?" "+mn:" *";		
		cronex+=(dw!=null && !dw.equals(""))?" "+dw:((day_question_mark)? " *":" ?");
		//System.out.println("SchedulerEngineUtils: cronex:"+cronex);
		return cronex;
	}
	
	
	public Vector getTriggerTimes(String sec,String min, String hr, String dw,String dm,String mn, int numoftimes,String timezone) throws Exception {
		String cronExp=generateCronExpr(sec,min,hr,dw,dm,mn);
		
		//CronTrigger trigger=new CronTrigger("test","default",cronExp);
	    CronScheduleBuilder csb=cronSchedule(cronExp);
        List list=Arrays.asList(TimeZone.getAvailableIDs());
        if(timezone!=null && !timezone.trim().equals("") && list.contains(timezone.trim())) {
        	csb.inTimeZone(TimeZone.getTimeZone(timezone));
        }
	        
		Trigger trigger=newTrigger()
    			.withIdentity("test","default") 
    			.withSchedule(csb)
    			.build();
		
		
		return getNext5Times(trigger,null,numoftimes,timezone);
	}
	
   private Vector getNext5Times(Trigger trigger, TimeZone timez, int numoftimes,String timezone)  throws Exception {
		
    	Calendar nextmin=Calendar.getInstance();    	
    	SimpleDateFormat format=new SimpleDateFormat("dd MMM, yyyy hh:mm:ss a (EEE)");    	
    	SimpleDateFormat convert1=new SimpleDateFormat("dd MMM, yyyy HH:mm:ss");   	
    	//convert1.setTimeZone(timez);
    	SimpleDateFormat convert2=new SimpleDateFormat("dd MMM, yyyy HH:mm:ss");
    	SimpleDateFormat today_tomm=new SimpleDateFormat("hh:mm:ss a");
    	
    	String rtn="";

    	String tzsuffix="";
    	List list=Arrays.asList(TimeZone.getAvailableIDs());
    	if(timezone!=null && !timezone.trim().equals("") && list.contains(timezone.trim())) {
    		tzsuffix=" "+TimeZone.getTimeZone(timezone).getDisplayName(false, TimeZone.SHORT)+"";
        }
    	
    	log.debug("Timezone:suffix:"+tzsuffix);
    	
        Vector rtnSDates=new Vector();
    	
    	int count=0;
    	
    	Date nextfirtime=null;
    	if(trigger.getPreviousFireTime()!=null){
    		nextfirtime=trigger.getPreviousFireTime();
    	}else{
    		nextfirtime=trigger.getStartTime();
    	}
    	
    	while(trigger.getFireTimeAfter(nextfirtime)!=null && count<numoftimes){   
    		
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
    			line="Today at "+today_tomm.format(noccur) ;
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
    			rtn1="\n"+format.format(noccur)+tzsuffix;;
    			rtnSDates.add(format.format(noccur)+tzsuffix);
    		}else{
    			rtn1="\n"+line+tzsuffix;
    			rtnSDates.add(line+tzsuffix);
    		}

    		
    		//System.out.println("calcuating:"+rtn1);
    		nextfirtime=trigger.getFireTimeAfter(nextfirtime);
    		rtn+=rtn1;
    		count++;
    	}
    	return rtnSDates;
		
	}
	
   
    public ArrayList<ValueObject> parseCodeInjection(String txt) {
    	
    	Pattern p = Pattern.compile("(\\[)([0-9]+)(:inj\\])(.*?)(\\[inj\\])",Pattern.DOTALL);
		Matcher m = p.matcher(txt);
		ArrayList al=new ArrayList();
			//System.out.println("count:"+m.groupCount());				
		while(m.find()){
			if(m.groupCount()==5){
				//System.out.println("id:"+m.group(2));
				//System.out.println("code:"+m.group(4));
				ValueObject vobj=new ValueObject();
				vobj.setKey(m.group(2));
				vobj.setValue(m.group(4));
				al.add(vobj );
			}
		}
		if(al.size()==0){
			
			StringTokenizer st=new StringTokenizer(txt,",");
			
			while(st.hasMoreTokens()){
				ValueObject vobj=new ValueObject();
				vobj.setKey(st.nextToken());
				al.add(vobj );
			}
			
		}
		log.debug("al:"+al);
		return al;    					
    }
}


