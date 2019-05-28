/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.alarm;

import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fourelementscapital.alarm.Alarm;
import com.fourelementscapital.alarm.AlarmType;
import com.fourelementscapital.alarm.ThemeVO;
import com.fourelementscapital.scheduler.config.Config;

public class SchedulerAlarm {
	
	public static String ALARM_SUB_FAILED="Failed";
	public static String ALARM_SUB_TIMEOUT="Timeout";
	public static String ALARM_SUB_CRASHED="Crashed";
	
	public static String SCHEDULER_TEAM_THEME="computing";	
	
	private static final int SERVER_ERROR_ALARM_SENT=3005;	

	/**
	 * Construct alarm message using Scheduler data and send it to iMonitor API via lib-alarm
	 * @param vo SchedulerAlarmVO
	 * @throws Exception
	 */
	public static void sendAlarm(SchedulerAlarmVO vo) throws Exception {	
		
		final Logger log = LogManager.getLogger(Alarm.class.getName());
		
		String bypass_phone_call_alarms = Config.getString("ignore.phone.alarm");
		List<String> errList = Arrays.asList(bypass_phone_call_alarms.split(","));

	    boolean isIgnorePhone = "phone".equalsIgnoreCase(vo.getAlarmType()) && errList.contains(Integer.toString(vo.getErrCode()));
		if(isIgnorePhone){	
			vo.setAlarmType("email"); // pass phone alarm. update alarmType to email.
		}
		
		boolean failed=vo.isRepCodeExist();
		if(failed){
			log.error("--tried sending alarm for alarm sent failure, trigger_time:"+vo.getTriggerTime()+" scheduler_id:"+vo.getSchedulerId());
		}

			
		SimpleDateFormat sdf=new SimpleDateFormat("hh:mm:ss");
		ArrayList<String> themes=(ArrayList) vo.getThemeTags();
		
		//put owner theme on top...
		String owner_them=vo.getOwnerTheme();
		if(owner_them!=null && themes.contains(owner_them)){
			themes.remove(owner_them);
			themes.add(0, owner_them);
		}
		
		Map hlog=vo.getQueueLog();
		
		int respCode=0;
		try{
			respCode=((Number)hlog.get("response_code")).intValue();
		}catch(Exception e){
			//no code exist.
		}
		String msgfrom="";
		if(vo.getFrom()!=null) {		 
			String friendlyname=vo.getPeerFriendlyName();
			msgfrom=""+(friendlyname!=null ?friendlyname+"("+vo.getFrom()+") " :vo.getFrom()+" ");
		}			
		if(themes!=null && themes.size()>0){				
			String themes1="";
		    for(Iterator<String> i=themes.iterator();i.hasNext();) {		    	
		    	themes1+=themes1.equals("")?i.next():","+i.next();
		    }
    
		    String subject1="Task "+vo.getSchedulerId()+" ("+vo.getName()+") "+vo.getSubject()+((respCode>0)?",Err Code:"+respCode:""); 

		    String composed="<div style='marging-bottom:30px;border:3px solid #D1D1D1;font-size:1.3em;background-color:#F0F0F0;padding:10px 20px;border-radius: 5px;'>";
		    composed+="<div>Script:<strong>"+vo.getName()+" ("+vo.getSchedulerId()+")"+"</strong></div>" ;
		    composed+="<div>Scheduled At:<strong>"+sdf.format(new Date(vo.getTriggerTime()))+"</strong></div>" ;
		    composed+="<div>Peer:<strong>"+msgfrom+"</strong></div>";
		    composed+="<div>Status:<strong>"+vo.getSubject()+"</strong></div>" ;
		    composed+="<div>Response Code:<strong>"+respCode+"</strong></div>" ;
		    composed+="<div><u>Message:</u></div>" ;
		    composed+="<div><pre style='margin:0px;color:red'>"+vo.getMessage()+"</pre></div>";
		    composed+="</div>";				    
		    composed+="<div>";
		    log.debug("resp_code:"+composed.length());
		    composed+=collectExecLogs(vo.getExecLogs());
		    if(respCode>0)composed+=getRespCodeError(respCode);
		    composed+=getConsoleMsg(vo.getConsoleMsg());
		    composed+="</div>";
		    log.debug("composed:"+composed);
		    
		    boolean say=true;
		    
		    if (vo.isExceptionSchedulerTeamRelated()) {
		    	themes.add(SCHEDULER_TEAM_THEME);
		    	subject1="[TO_COMPUTING]"+subject1;
		    }
			    
			// convert String array list to ThemeVO array list required by Alarm.sendAlarm() :
			ArrayList<ThemeVO> themeList = new ArrayList<ThemeVO>();
			for (int i=0; i<themes.size(); i++) {
				themeList.add(new ThemeVO(themes.get(i)));
			}
			
		    Alarm.sendAlarm(themeList, "email".equalsIgnoreCase(vo.getAlarmType()) ? AlarmType.EMAIL : AlarmType.PHONE, subject1, composed, false, "email".equalsIgnoreCase(vo.getAlarmType()), "phone".equalsIgnoreCase(vo.getAlarmType()), null, null);

		}
	}	
	
	
	/**
	 * Collect execution logs, format them in html 
	 * @param execLogs Execution logs
	 * @return Execution logs in html
	 * @throws Exception
	 */		
	private static String collectExecLogs(List<Map> execLogs) throws Exception {
		
		final Logger log = LogManager.getLogger(SchedulerAlarm.class.getName());
		
		try {
			String rtn="<br><span style='margin-top:30px;margin-bottom:0px;font-size:1.5em'>Execution Log</span><table width='100%' cellpadding='3' cellspacing='0' border='1' style='border:1px solid #D1D1D1;border-collapse:collapse;'><thead><tr>  <th align='left'>Date&Time</th> <th align='left'>Machine</th> <th align='left'>Code</th> <th align='left'>Message</th>  </tr></thead>";
			log.debug("collectExecLogs, list:"+execLogs);
			if(execLogs==null || (execLogs!=null && execLogs.size()==0)){
				return null;
			}
			rtn+="<tbody>";
			for(Map record:execLogs){
				String cls="";
				int repcode=0;
				try{
					repcode=((Number)record.get("repcode")).intValue();
					if((repcode>=3000 && repcode<=4999) || (repcode>=6000 && repcode<=7999)){
						cls=" style='color:#d14836' ";
					}
				}catch(Exception e){
					// do nothing
				}
				if(repcode!=SERVER_ERROR_ALARM_SENT){
					rtn+="<tr "+cls+">";			
					rtn+="<td width='width:105px'>"+record.get("trans_datetime1")+"</td>";
					rtn+="<td>"+record.get("machine")+"</td>";
					rtn+="<td>"+((repcode>0)?repcode+"":"")+"</td>";
					rtn+="<td>"+record.get("message")+"</td>";			
					rtn+="</tr>";
				}
			}
			rtn+="</tbody></table>";
			return rtn;
		} catch (Exception e) {
			 return "Couldn't get execution logs, error:"+e.getMessage();
		}
	}
	
	
	/**
	 * Get error description from html file, format it in html
	 * @param code Error code
	 * @return error description in html
	 */		
	private static String getRespCodeError(int code) throws Exception {
		
		final Logger log = LogManager.getLogger(SchedulerAlarm.class.getName());
		
		ClassLoader cLoader = Alarm.class.getClassLoader();
        InputStream inputStream = cLoader.getResourceAsStream("codehelp"+File.separator+code+".html");
		if (inputStream != null && inputStream.available() > 0) {
			StringWriter writer = new StringWriter();
			IOUtils.copy(inputStream, writer);
			return "<br><span style='margin-top:30px;margin-bottom:0px;font-size:1.5em'>Learn more about the issue</span>"+writer.toString();
		}else{		
			log.error("codehelp"+File.separator+code+".html doesn't exist");
			return "";
		}
	}
	
	
	/**
	 * Get scheduler console message, format it in html
	 * @param consoleMsg Console message
	 * @return Console message in html
	 */		
	private static String getConsoleMsg(String consoleMsg)  {
		try {
			if(consoleMsg!=null && !consoleMsg.trim().equals("")){
				String rtn="<br><span style='margin-top:30px;margin-bottom:0px;font-size:1.5em'>Console Output</span><div style='font-family: courier;'><pre style='margin-top:0px'>"+consoleMsg+"</pre></div>";
				return rtn;
			}else{
				return "";
			}
		} catch (Exception e) {
			return "Error: couldn't get console message, Error:"+e.getMessage();
		}
	}
	
	
}


