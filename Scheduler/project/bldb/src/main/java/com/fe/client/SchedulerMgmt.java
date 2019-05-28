/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fe.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jcs.JCS;
import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mozilla.javascript.edu.emory.mathcs.backport.java.util.Arrays;
import org.tmatesoft.svn.core.SVNLogEntry;

import EDU.oswego.cs.dl.util.concurrent.Semaphore;

import com.fe.lucene.LuceneCrawler;
import com.fe.lucene.TickerIndexRecord;
import com.fe.lucene.TokenCollectorFactory;
import com.fe.scheduler.TemplateParser;
import com.fe.svn.SVNSchedulerCommitInfo;
import com.fe.svn.SVNSync;
import com.fe.util.RestartTomcat;
import com.fe.util.WikiRFunctionManual;
import com.fourelementscapital.alarm.Alarm;
import com.fourelementscapital.alarm.AlarmType;
import com.fourelementscapital.alarm.ThemeVO;
import com.fourelementscapital.auth.UserThemeAccessPermission;
import com.fourelementscapital.db.BBSyncDB;
import com.fourelementscapital.db.ConstructQueryDB;
import com.fourelementscapital.db.InfrastructureDB;
import com.fourelementscapital.db.RFunctionDB;
import com.fourelementscapital.db.SchedulerDB;
import com.fourelementscapital.db.vo.FlexiField;
import com.fourelementscapital.db.vo.SchedulerTrigger;
import com.fourelementscapital.db.vo.ValueObject;
import com.fourelementscapital.scheduler.ScheduledTaskFactory;
import com.fourelementscapital.scheduler.ScheduledTaskQueue;
import com.fourelementscapital.scheduler.SchedulerEngine;
import com.fourelementscapital.scheduler.SchedulerEngineUtils;
import com.fourelementscapital.scheduler.balance.LoadBalancingQueue;
import com.fourelementscapital.scheduler.balance.LoadBalancingQueueItem;
import com.fourelementscapital.scheduler.balance.LoadBalancingQueueTimeout;
import com.fourelementscapital.scheduler.config.Config;
import com.fourelementscapital.scheduler.engines.ScheduledTask;
import com.fourelementscapital.scheduler.engines.ScheduledTaskField;
import com.fourelementscapital.scheduler.error.ClientError;
import com.fourelementscapital.scheduler.group.DirectRServeExecuteRUnix;
import com.fourelementscapital.scheduler.group.REngineScriptTask;
import com.fourelementscapital.scheduler.group.RServeUnixTask;
import com.fourelementscapital.scheduler.p2p.P2PService;
import com.fourelementscapital.scheduler.p2p.listener.IncomingMessage;
import com.fourelementscapital.scheduler.p2p.listener.P2PTransportMessage;
import com.fourelementscapital.scheduler.p2p.msg.PostMessage;
import com.fourelementscapital.scheduler.p2p.msg.impl.helper.SendCommand2Helper;
import com.fourelementscapital.scheduler.p2p.msg.scheduler.InstantPeerStatus;
import com.fourelementscapital.scheduler.p2p.msg.scheduler.rserve.PeerPropertiesGet;
import com.fourelementscapital.scheduler.p2p.msg.scheduler.rserve.PeerPropertiesSet;
import com.fourelementscapital.scheduler.p2p.msg.scheduler.rserve.RServeSessionQuery;
import com.fourelementscapital.scheduler.p2p.msg.scheduler.rserve.RServeSessionStat;
import com.fourelementscapital.scheduler.p2p.peer.PeerMachine;
import com.fourelementscapital.scheduler.p2p.peer.PeerManagerHSQL;
import com.fourelementscapital.scheduler.pluggin.PlugginInterface;
import com.fourelementscapital.scheduler.pluggin.SchedulerPlugginRegister;


/**
 * This class exposes data in JSON format for scheduler UI.
 * 
 * The following infos can be retrived for the UI.
 *  -Queue data
 *  -Executing tasks
 *  -Queued tasks
 *  -Online peers
 *  -Queue history
 *  -Execution Logs
 *  -Queue settings
 *  
 *  -Managing task groups and order
 *  -Managing task folders and order
 *  -Managing tasks and its order
 *  -Managing contents of tasks including Scripts and code management
 *  -Managing themes and privileges
 *  
 *  -Managing peers and association with tasks
 *  -Enabling/Disabling peers
 *  
 *  -Rendering scheduler related reports
 *  
 *  Changing anything in this class required extensive testing as it can directly impact the queue behaviours and sometimes may lead to 
 *  critical bugs and could stop certain scheduler functionalities.
 *  
 * Returned data will be parsed as JSON objects in the javascript side.    
 * 
 * Check DWR documentation to know more about why this class needed.  
 *
 */
public class SchedulerMgmt extends AbstractTeamOrgMgmt {

	
	private Logger log = LogManager.getLogger(SchedulerMgmt.class.getName());
	private HttpServletRequest request=null;
	
	
	/*
	 * Session key constant to keep some of the settings in session;
	 */
	private String ACTIVE_TAB_ATTRIBUTE="$$$ActiveSchedulerTab"; //current tab
	private String TAG_SHOWHIDE_ATTRIBUTE="$$$ShowHideTags"; //show or hide his tags;
	private String COOKIE_SCHEDULER_ALLITEMS="scheduler_showAll"; //remember the scheduler list filter (all/only mine)
	
	private static String LOG_STATUS_RESTARTED="re-executed";
	private static JCS lockcache=null;
	private static Semaphore logListLock=new Semaphore(1);	
	
	private static String CALLING_ANOTHER_SCRIPT_PATTERN="executeSchedulerScript(";
	
	
	
	
	/**
	 * for DWR invocation
	 * @throws Exception
	 */
	public SchedulerMgmt() throws Exception {
		super();
 
	}
	
	
	/**
	 * Invocation only when you have HttpRequest is available,
	 * in JSP or internally. 
	 * @param request
	 * @throws Exception
	 */
	public SchedulerMgmt(HttpServletRequest request) throws Exception {
		super(request);
	 
	}
	
 
	/**
	 * checks the current scheduler script has valid edit permission in the current request.	  
	 * @param scheduler_id
	 * @param sdb
	 * @throws Exception
	 */
    public void validateEditPrivilege(int scheduler_id, SchedulerDB sdb) throws Exception {
    	String access=getAccessPrivilege(scheduler_id, sdb);			
		if(access==null || (access!=null && access.equals("") || access!=null && access.equals(ACCESS_PRIVILEGE_R) || access!=null && access.equals(ACCESS_PRIVILEGE_RX))){				
			throw new Exception("Access denied to  scheduler ID ("+scheduler_id+") Contact Administrator.");				
		}
    }
	
	
	
	


    /**
     * This method returns Map that contains data for rendering 
     * tree menu item in the left side panel of the scheduler tasks User interface screen
     * Map contains, group, folders, tasks and tags together with the access permission.
     * 
     * @return
     * @throws Exception
     */
	public Map listScheduledItems() throws Exception {		
		return listScheduledItems2(null);
	}

	
	
	private Vector getGroupOrder() throws Exception {		
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		try{
			sdb.connectDB();
			Vector rtn=sdb.getGroupOrder();
			
			
			return rtn;
		}catch(Exception e){ throw e;}
		finally{
			sdb.closeDB();		
		}
		
	}
	
	
	/**
	 * @deprecated
	 * @return
	 * @throws Exception
	 */
	public boolean killServerTaskQueue() throws Exception {
		ScheduledTaskQueue.killQueueThread();
		return true;
	}
	
	
	private Map getGroupIconsAndColors() throws Exception {		
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		
		try{
			HashMap rtn=new HashMap();
			
			HashMap icons=new HashMap();
			HashMap colors=new HashMap();
			sdb.connectDB();
			Vector data=sdb.getActiveGroups();
			for(Iterator i=data.iterator();i.hasNext();){
				Map row=(Map)i.next();
				icons.put(row.get("taskuid"),row.get("icon"));
				colors.put(row.get("taskuid"),row.get("bar_colorcode"));
			}
			rtn.put("group_icons",icons);
			rtn.put("group_colorcodes",colors);
			return rtn;
		}catch(Exception e){ throw e;}
		finally{
			sdb.closeDB();		
		}
	}
	

	/**
	 * Search by task name, within the selected tag.
	 * @deprecated
	 * @param keyword
	 * @param tagid
	 * @return
	 * @throws Exception
	 */
	public Map searchScheduledItems(String keyword, String tagid) throws Exception {		
		//return listScheduledItems2(keyword);
		HashMap rtn=new HashMap();
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		try{
			sdb.connectDB();
			int tid=0;
			try{tid=Integer.parseInt(tagid);}catch(Exception e){}
			
			List slist=sdb.searchScheduler(keyword,tid);
			rtn.put("scheduleditems", slist);
			return rtn;
		}catch(Exception e){ throw e;}
		finally{
			sdb.closeDB();		
		}
		
	}
	
	
	
	private boolean isSchedulerShowAll() throws Exception{
		boolean showAll=false;
		
		Cookie cookies[]=getRequest().getCookies();
		for(int i=0;i<cookies.length;i++){
			if(cookies[i].getName().equals(COOKIE_SCHEDULER_ALLITEMS)){
				if(cookies[i].getValue()!=null && cookies[i].getValue().equalsIgnoreCase("yes")){
					showAll=true;
				}
			}
		}
		return showAll;		
	}
	
	
	/**
	 * @see listSchedulerItems
	 * @param keyword
	 * @return
	 * @throws Exception
	 */
	private Map listScheduledItems2(String keyword) throws Exception {
		
		HashMap rtn=new HashMap();
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		sdb.connectDB();
		List slist=new ArrayList();
		
 		boolean showAll=true;  
		
		List folders=null;
		
		// 20161124 : always null
		if(keyword==null){
			 
			if(showAll){
				slist=sdb.listSchedulerTaskUIDJoin() ;
				folders=sdb.listOfFolders(null); //null means all folders
			}else{
			  ArrayList al=new ArrayList();			
			  UserThemeAccessPermission user=null;			  
			  try{
				  user=getAuthenticatedUserObj(sdb);
				  if(user!=null){
					  log.debug("user:"+user);
					  al.addAll(user.getRwx());
					  al.addAll(user.getRx());
					  al.addAll(user.getR());
					  slist=sdb.listScheduler(al);
					  folders=sdb.listofMyFolders(al); 
				  }
			  }catch(Exception e){
				  //e.printStackTrace();
				  log.error("Error while getAuthenticatedUserObj, could be wiki database issue");		
			  }
			}
		}
		
		List<ScheduledTask> tasks= new ScheduledTaskFactory().getTasks();
		
		LinkedHashMap  tasksnames=new LinkedHashMap ();		
		Vector uniqt=new Vector();
		
		Vector orders=getGroupOrder();
		
		for(int i=0;i<=orders.size();i++){
			Object item=(i<orders.size())?orders.get(i):null;

			for(Iterator<ScheduledTask> it=tasks.iterator();it.hasNext();){
				ScheduledTask tsk=it.next();
				if(item==null || (item!=null && item.equals(tsk.getUniqueid()))){
					if(!uniqt.contains(tsk.getUniqueid())){
						if(tsk.getClass().getName().equals(REngineScriptTask.class.getName()) || tsk.getClass().getName().equals(DirectRServeExecuteRUnix.class.getName())){
							//ignore REngineScript Tasks (direct_script) as this type available only via SchedulerAPI 
						}else{
							tasksnames.put(tsk.getUniqueid(),tsk.getName());
							uniqt.add(tsk.getUniqueid());
						}
					}
				}
			}
			
		}
	
		
		
		TreeMap relationship=new TreeMap();
		for(Iterator i=tasksnames.keySet().iterator();i.hasNext();){
			String tas1=(String)i.next();
			for(Iterator ia=uniqt.iterator();ia.hasNext();){
				String tas2=(String)ia.next();				
				if(tas1.substring(0,4).equals(tas2.substring(0,4)) && !tas1.equals(tas2)){
					HashMap h ;
					if(relationship.get(tas1)==null){
						h=new HashMap();
						relationship.put(tas1,h);
					}else{
						h=(HashMap)relationship.get(tas1);
					}
					h.put(tas2,tasksnames.get(tas2));		        			
				}
			}
		}
		
		
		rtn.putAll(getThemeAccessData(sdb)); //putting team organization data example: tags 
		
		sdb.closeDB();		
		
		
		rtn.put("scheduleditems", slist);
		
		rtn.put("folders", folders);
		rtn.put("tasktypes",tasksnames );
		rtn.put("taskrelation", relationship);
		rtn.put("isShowAll", showAll); 
		 
		return rtn;
		
	}
	
	
	/**
	 * return list of task types
	 * @return
	 * @throws Exception
	 */
	public Map getScriptTypes () throws Exception  {
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		try{
			
			
		Map sitems=listScheduledItems2(null);
		Map types=(Map)sitems.get("tasktypes");

		sdb.connectDB();
		LoadBalancingQueueTimeout lqt=new LoadBalancingQueueTimeout(sdb,types.keySet());
		
		Map timeoutdata=BeanUtils.describe(lqt);
		timeoutdata.putAll(lqt.getMaxWaitingAlert());
		Map tags4new=getTags4New();
		Map rtn=(Map)tags4new.get("tag_follow");		
		rtn.put("types",types);
		rtn.put("data",timeoutdata);
		
		return rtn;
		
		}catch(Exception e){
			throw e;
		}finally{
			sdb.closeDB();
		}
	}
	
	/**
	 * Saves timeout settings, includes sql query criteria for timeout and what should be done incase of timeout and so on.
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public boolean updateTimeoutSettings(Map data) throws Exception {
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		try{
			
			sdb.connectDB();
			sdb.updateTimeoutSettings(data);			
			return true;
		}catch(Exception e){
			throw e;
		}finally{
			sdb.closeDB();
		}
	}
	
	/**
	 * called during clicking trash icon
	 * @return
	 * @throws Exception
	 */
	public List trashedItems() throws Exception {
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		try{
			
			sdb.connectDB();
			List rtn=sdb.listTrashedScheduler();			
			return rtn;
		}catch(Exception e){
			throw e;
		}finally{
			sdb.closeDB();
		}
	}
	
	
	private void updateWikiThread(final int scheduler_id){
		
		
		Thread thread=new Thread() {
			
			public void run() {
				SchedulerDB sdb=SchedulerDB.getSchedulerDB();
				try{
					sdb.connectDB();					 
					String wiki=getWikiHelp(scheduler_id+"");
					log.debug("wiki taskname:"+scheduler_id+" found: "+wiki.contains("\"noarticletext\"")+" scheduler_id:"+scheduler_id);
					if(wiki.contains("\"noarticletext\"")){
						sdb.updateWikiDone(scheduler_id, 0);				
					}else{
						sdb.updateWikiDone(scheduler_id, 1);
					}										
				}catch(Exception e){
					log.error("Error while updating");
				}finally{
					try{
					sdb.closeDB();
					}catch(Exception e){}
				}
				
			}
		}; 
		thread.start();
	
		
	}
	
	/**
	 * On cick of edit button on task item from tree menu
	 * 
	 * @param scheduleditem_id
	 * @return
	 * @throws Exception
	 */
	public Map getScheduledItem(int scheduleditem_id) throws Exception {
		
		
		HashMap rtn=new HashMap();
		
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		try{
			sdb.connectDB();
			
			Map data=sdb.getScheduler(scheduleditem_id);			
			log.debug("rscript:"+data.get("rscript"));			

			String access=getAccessPrivilege(scheduleditem_id, sdb);	
			
			if(access==null || (access!=null && access.equals(""))){				
				 
				rtn.put("access", ACCESS_PRIVILEGE_R);
			}else{
				rtn.put("access", access);
			}
			
		    log.debug("access:"+rtn.get("access"));	
		
		    //added 29/Oct/2013 no access to restricted scripts. 
			/*if(rtn.get("access")!=null && (rtn.get("access").equals(ACCESS_PRIVILEGE_R) || rtn.get("access").equals(ACCESS_PRIVILEGE_RX) )) {
				throw new Exception("No access to view this task");
			}*/
		    if(access==null){
				throw new Exception("No access to view this task");
			}
			updateWikiThread( scheduleditem_id);			
			
			Vector tdata=sdb.getTriggerData(scheduleditem_id);
			rtn.put("triggerdata", convertTriggerData(tdata));
			try{
				rtn.put("revisions",getSVNLogs(sdb,scheduleditem_id));
			}catch(Exception e){				
				log.error("error while reading revisions, ERR:"+e.getMessage());
			}			
		 
			
			if(data!=null){
				String taskuid=(String)data.get("taskuid");

 				
				ScheduledTask task=new ScheduledTaskFactory().getTask(taskuid);
				
				if(task==null){					
					throw new Exception("No engine active engine found for type "+taskuid);					
				}
				log.debug("task:"+task);
				
				
 				for(ScheduledTaskField field:task.listFormFields()){
					if(field.getPluggindata()!=null){			 
						PlugginInterface pl=SchedulerPlugginRegister.getPluggin(field.getFieldtype());			 
							
							String rec_id=(String)data.get(pl.getPlugginData().getFieldreference());
							log.debug("field:"+field.getFieldlabel()+" recordid:"+rec_id+" ref:"+pl.getPlugginData().getFieldreference()+" data:"+data);
							
							if(rec_id!=null){
								Map pluggindata=pl.fetchData(Integer.parseInt(rec_id),getRequest());
								data.put("pluggindata_field", field);
								data.put("pluggindata", pl.getPlugginData());	
								 
								data.put("pluggindata_data", pluggindata);
							}
					}
				}
				
				log.debug("pluggin processed");
				
				List<ScheduledTaskField> fields=getTaskFields(taskuid);
				ScheduledTaskField folder=null;
				for(Iterator<ScheduledTaskField> i=fields.iterator();i.hasNext();){
					ScheduledTaskField sf=i.next();
					if(sf.getShortname().equalsIgnoreCase("folder_id")) folder=sf;
				}
				
				log.debug("folder:"+folder);
				
				if(folder!=null) fields.remove(folder);
				rtn.put("fields",fields);				
				rtn.put("taskdata", data) ; 				
				rtn.put("lockedby", getLockedBy(scheduleditem_id,sdb));
				try{
					rtn.put("isAuthorized", isAuthorizedUser(sdb)) ;
					rtn.put("authorizedUser", getAuthorizedUser(sdb));
					
					String access1=(String)rtn.get("access");
							
					
					String usr=new SchedulerMgmt(getRequest()).getAuthorizedUser(sdb);
					if(getLockedBy(scheduleditem_id,sdb)==null){
						if(access1!=null && access1.equals(ACCESS_PRIVILEGE_RWX)){
							 
							refreshCache(scheduleditem_id,LOCK_AUTO_RELEASE,usr);
						}
					}
				}catch(Exception e){
					e.printStackTrace();
				}
				
				log.debug("near end");				
				rtn.put("tag_follow", getItemTags2(scheduleditem_id,sdb));				
				log.debug("end");
				
			}	
			return rtn;
		}catch(Exception e){
			//e.printStackTrace();
			ClientError.reportError(e, null);			
			throw e;
		}finally{
			sdb.closeDB();
		}
		
	}
	
	public Map getTags4New() throws Exception {
		HashMap rtn=new HashMap();
		
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		try{
			sdb.connectDB();
			rtn.put("tag_follow", getItemTags2(0,sdb));
			return rtn;
		}catch(Exception e){
			throw e;
		}finally{
			sdb.closeDB();
		}
	}
	
	public String getScriptRev(String scheduler_id, String revision,boolean flag) throws Exception {
		SVNSync sync=new SVNSync();
		String script=null;
		if(flag)
			script=sync.getScript(Integer.parseInt(scheduler_id), Long.parseLong(revision));
		else
			script=sync.diffWC(Integer.parseInt(scheduler_id), Long.parseLong(revision));
		
		return script;
		
	}
	
	
	private Vector getSVNLogs(SchedulerDB sdb, int scheduler_id) throws Exception {
		SVNSync sync=new SVNSync();
		TreeMap rtn=new TreeMap(); 
		Vector d=sync.log(scheduler_id);
		List otherlogsList=sdb.getEditLogs(scheduler_id);
        if( (d!=null && d.size()>0) || (otherlogsList!=null && otherlogsList.size()>0)){
        	
        	SimpleDateFormat format=new SimpleDateFormat("dd-MMM-yyyy hh:mm a");        	
        	for(Iterator<Map> i=otherlogsList.iterator();i.hasNext();){
        		Map d1=(Map)i.next();
        		HashMap data=new HashMap();		    	
		    	data.put("author", d1.get("username"));
		    	data.put("date", format.format((Date)d1.get("edited_datetime")));
		    	data.put("message", d1.get("message"));		     
		    	data.put("scheduler_id", scheduler_id);
		    	rtn.put(((Date)d1.get("edited_datetime")).getTime(), data);		    	
        	}
        	if(d!=null){
			    for(Iterator<SVNLogEntry> i=d.iterator();i.hasNext();){
			    	SVNLogEntry entry=i.next();	    				    	
			    	ValueObject vo=new ValueObject();
			    	HashMap data=new HashMap();			    	
			    	data.put("author", entry.getAuthor());
			    	data.put("date", format.format(entry.getDate()));
			    	data.put("message", entry.getMessage());
			    	data.put("revision", entry.getRevision());
			    	data.put("scheduler_id", scheduler_id);
			    	if(entry.getRevision()>0){
			    		//rtn.put(entry.getRevision(), data);
			    		rtn.put(entry.getDate().getTime(), data);
			    	}
			    	//System.out.println(" Rev:"+entry.getRevision()+"  Date:"+format.format(entry.getDate())+"  User:"+entry.getAuthor()+" Msg:"+entry.getMessage());
			    	
			    }
        	}
		    //return rtn.descendingMap();
		    Vector rtn1=new Vector(rtn.descendingMap().values());
		    return rtn1;
        }else{
        	return null;
        }
		
	}
	
	
	private Vector convertTriggerData(Vector v){
		
		Vector rtn=new Vector();
		for(Iterator i=v.iterator();i.hasNext();){
			Map rd=(Map)i.next();
			Vector row=new Vector();
			row.add(rd.get("exp_second"));
			row.add(rd.get("exp_minute"));
			row.add(rd.get("exp_hour"));
			row.add(rd.get("exp_week"));
			row.add(rd.get("exp_day"));
			row.add(rd.get("exp_month"));
			row.add(rd.get("inject_code"));
			rtn.add(row);
		}
		return rtn;
	}
	
	/**
	 * list all fields for the task..
	 * @param uid
	 * @return
	 * @throws Exception
	 */
	public List getTaskFields(String uid) throws Exception {
		
		try {		
			List<ScheduledTask> list=new ScheduledTaskFactory().getTasks();
			List<ScheduledTaskField> rtn=null;
			
			for(Iterator<ScheduledTask> i=list.iterator();i.hasNext();){
				ScheduledTask st=i.next();
				if(st.getUniqueid().equals(uid)){
					rtn=st.listFormFields();
				}
			}
			return rtn;
		}catch(Exception e){
			ClientError.reportError(e, null);
			throw e;
		}
	}

	
	private ScheduledTask getTask (String uid) throws Exception {
		
		try {		
			return new ScheduledTaskFactory().getTask(uid);
			//List<ScheduledTask> list=new ScheduledTaskFactory().getTasks();
			//ScheduledTask rtn=null;
			
			//for(Iterator<ScheduledTask> i=list.iterator();i.hasNext();){
			//	ScheduledTask st=i.next();
			//	if(st.getUniqueid().equals(uid)){
			//		rtn=st ;
			//	}
			//}
			//return rtn;
		}catch(Exception e){
			ClientError.reportError(e, null);
			throw e;
		}
	}

	public String move2RootGroup(int scheduler_id, String taskuid) throws Exception {
		
		boolean normalError=false;
		try {
			String msg="";
			SchedulerDB sdb=SchedulerDB.getSchedulerDB();
			sdb.connectDB();
			
					
			Map scheduler=sdb.listSchedulerItem(scheduler_id);
			String task_taskuid=(String)scheduler.get("taskuid");			
				
			if(task_taskuid!=null && taskuid!=null && task_taskuid.equals(taskuid) ){				 
					sdb.updateTaskFolder(scheduler_id, 0);	
	    			msg="Task has been moved";
				 
			}else if(
					task_taskuid!=null && taskuid!=null && 
					!task_taskuid.equals(taskuid) && 
					task_taskuid.substring(0,3).equals(taskuid.substring(0, 3))
					
			){				 
				sdb.moveItemToRootOrGroup(scheduler_id, taskuid, 0);
				new ScheduledTaskFactory().refreshTaskLoaded();
				msg="Please note, the task has been moved to different group";
				
			}else{
				normalError=true;
				throw new Exception("Moving allowed only within similar groups");
			}
    		sdb.closeDB();
    		//return listScheduledItems(); 
    		return msg;
		}catch(Exception e){
			if(!normalError){
				ClientError.reportError(e, null);
			}
			throw e;
		}
	}

	
	public String updateTaskFolder (int scheduler_id, int folder_id) throws Exception {
		
		boolean normalError=false;
		try {
			String msg="";
			SchedulerDB sdb=SchedulerDB.getSchedulerDB();
			sdb.connectDB();
			
			String folder_tuid=sdb.getFolderTaskUID(folder_id);			
			Map scheduler=sdb.listSchedulerItem(scheduler_id);
			String task_taskuid=(String)scheduler.get("taskuid");			
				
			if(task_taskuid!=null && folder_tuid!=null && task_taskuid.equals(folder_tuid) ){
				int oFolder_id=(Integer)scheduler.get("folder_id");
				if(oFolder_id==folder_id){
					msg="Task not moved! Source and destination folder are the same";
				}else{
					sdb.updateTaskFolder(scheduler_id, folder_id);	
	    			msg="Task has been moved";
				}
			}else if(
					task_taskuid!=null && folder_tuid!=null && 
					!task_taskuid.equals(folder_tuid) && 
					task_taskuid.substring(0,3).equals(folder_tuid.substring(0, 3))
					
			){				 
				sdb.moveItemToRootOrGroup(scheduler_id, folder_tuid, folder_id);
				new ScheduledTaskFactory().refreshTaskLoaded();
				msg="Please note, the task has been moved to different group";
				
			}else{
				normalError=true;
				throw new Exception("Moving allowed only within similar groups");
			}
    		sdb.closeDB();
    		//return listScheduledItems(); 
    		return msg;
		}catch(Exception e){
			if(!normalError){
				ClientError.reportError(e, null);
			}
			throw e;
		}
	}
	
	public Map  folderTasks (int folder_id, String taskuid) throws Exception {
		
		try {
			HashMap rtn=new HashMap();
			SchedulerDB sdb=SchedulerDB.getSchedulerDB();
    		sdb.connectDB();
    		rtn.put("tasks", sdb.folderTasks(folder_id));
    		rtn.put("folders", sdb.listOfFolders(taskuid));
    		sdb.closeDB();
    		return rtn; 
		}catch(Exception e){
			ClientError.reportError(e, null);
			throw e;
		}
	}
	
	
	public Map updateScheduleTask(int scheduled_id,Map<String,String> data, String taskuid,boolean confirm_flag,Map plugindata, SchedulerTrigger[]  triggerdata, String comment,Vector newtask_tags,Vector follow_tags) throws Exception {
		
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		
		try{
			HashMap rtn=new HashMap(); 
			
			log.debug(" updateScheduleTask : 1");
			
			sdb.connectDB();	
    		
			if(scheduled_id>0){
				validateEditPrivilege(scheduled_id,sdb);
			}
			log.debug(" updateScheduleTask : 2");
	    	if(confirm_flag){				 
	    		
	    		//folder id and taskuid remains same, to avoid mistakenly updated some cases
	    		
	    		boolean newtask=false;
	    		String name=(String)data.get("name");
	    		if(data==null ||(name!=null && name.trim().equalsIgnoreCase(""))){
	    			throw new Exception("Name field is empty!!");
	    		}else{
	    			if(sdb.isNameExisting(name,scheduled_id)){
	    				throw new Exception("Name:"+name+" already existing, please try different name");
	    			}
	    		}
	    		
	    		
	    		if(scheduled_id>0){
	    			Map tdata1=sdb.getScheduler(scheduled_id);
	    			//over-ride the taskuid and folder_id for existing so that can't be changed by mistake
	    			taskuid=(String)tdata1.get("taskuid");
	    			String folder_id=tdata1.get("folder_id")+"";
	    			data.put("folder_id",folder_id);	    			
	    		}else{
	    			newtask=true;
	    		}
	    		
	    		log.debug(" updateScheduleTask : 3");
	    		
	    		if(data.get("rscript")!=null) {
	    			String rscript=(String)data.get("rscript");
	    			boolean calling=isCallingAnotherScript(rscript);
	    			if(calling){
	    				data.put("calling_another_script","1");
	    			}
	    		}	    		
	    			    		
	    		ScheduledTask task=new ScheduledTaskFactory().getTask(taskuid);
	    		
	    		for(ScheduledTaskField field:task.listFormFields()){
	    			if(field.getPluggindata()!=null){
	    				//System.out.println("plugindata:"+data.get(field.getShortname()));
	    				//Map plugindata=(Map)data.get(field.getShortname()); 
	    				PlugginInterface pl=SchedulerPlugginRegister.getPluggin(field.getFieldtype());
	    				if(scheduled_id==0){
	    					int p_recordid=pl.addAction(plugindata,getRequest());
	    					data.put(pl.getPlugginData().getFieldreference(), p_recordid+"");
	    				}else{
	    					int p_recordid=pl.updateAction(scheduled_id,plugindata,getRequest());
	    					data.put(pl.getPlugginData().getFieldreference(), p_recordid+"");
	    				}
	    				data.put("plugin_txt", pl.getText(plugindata));
	    			}
	    		}

	    		log.debug(" updateScheduleTask : 4");
	    		
	    		//sdb.connectDB();
	    		//String ip=(String)getRequest().getSession().getAttribute(REMOTE_IP);
	    		String user=getAuthenticatedUser();
	    		//int scheduler_id=sdb.addOrUpdateScheduler(scheduled_id,data, taskuid,ip,comment);

	    		//SVNSchedulerCommitInfo sinfo=sdb.addOrUpdateScheduler(scheduled_id,data, taskuid,user,comment);
	    		Integer id = sdb.addOrUpdateSchedulerGetId(scheduled_id, data, taskuid);
	    		
	    		
	    		String rscript = null;
	    		
	    		if(id != null){	    			
	    			for(Iterator<ScheduledTaskField> it=task.listFormFields().iterator();it.hasNext();){
	    				ScheduledTaskField stf=it.next();
	    				if(data.get(stf.getShortname())!=null && !data.get(stf.getShortname()).equals("") ){
	    					sdb.addOrUpdateSchedulerInsertTaskdata(id, data, stf.getShortname());
	    					if(stf.getShortname().equalsIgnoreCase(FlexiField.TYPE_RSCRIPTEDITOR)){
	    						rscript=data.get(stf.getShortname());
	    					}
	    				}
	    			}
	    		}
	    		
	    		String svndata=rscript;
	    		
	    		if(rscript==null && data.get("plugin_txt")!=null ){
	    			svndata=data.get("plugin_txt");
	    		}		
	    		
	    		SVNSchedulerCommitInfo sinfo=new SVNSchedulerCommitInfo ();
	    		sinfo.setRevision(-1);
	    		
	    		//SVN Synchronizing		
	    		try{
	    			if(svndata!=null){
	    				String svnuser=Config.getString("svn_user");
	    				String svnpwd=Config.getString("svn_pwd");
	    				//String message="IP:"+clientip;
	    				//

	    				/*
	    				PreparedStatement ps5=this.connection().prepareStatement("SELECT * FROM users where ip_address=?");
	    				ps5.setString(1, clientip);				
	    				ResultSet rs5= ps5.executeQuery();
	    				
	    				if(rs5.next()){
	    					//Map row=new BasicRowProcessor().toMap(rs5);
	    					svnuser=rs5.getString("svn_username");
	    					svnpwd=rs5.getString("svn_password");
	    				}
	    				*/
	    				//Map u=sdb.getSVNUser(clientip);
	    				Map u=sdb.getSVNUser4WikiUser(user);				
	    				if(u!=null && u.get("svn_username")!=null && u.get("svn_password")!=null){
	    					svnuser=(String)u.get("svn_username");
	    					svnpwd=(String)u.get("svn_password");
	    				}
	    				
	    				SVNSync sync=new SVNSync(svnuser,svnpwd);
	    				long rev=sync.syncScript(id, svndata,comment);
	    				sinfo.setRevision(rev);
	    				
	    				//Vector tag_ids=new Vector();
	    				String user1="usr-"+svnuser.trim().toLowerCase();
	    				int tag_id=sdb.addIfTagNotExist(user1);
	    				//tag_ids.add(id);
	    				
	    				//updateTagIds4Task(thisid, tag_ids,SchedulerDB.DONT_REMOVE_BEFORE_UPDATE) ;
	    				sdb.updateLast2UsersTag(id,tag_id);
	    				
	    			
	    			}
	    		}catch(Exception e){
	    			log.error("Error on SVN sync: ERR:"+e.getMessage());
	    		}
	    		
	    		
	    		 
	    		//indexing
	    		try{
	    			TickerIndexRecord tir=new TickerIndexRecord();
	    			tir.setTablename(LuceneCrawler.INDEX_SCHEDULER);
	    	
	    			if( TokenCollectorFactory.getTokenCollector(tir)!=null){
	    				LuceneCrawler lc=new LuceneCrawler(LuceneCrawler.INDEX_SCHEDULER);
	    				lc.index(id+"");
	    			}
	    		}catch(Exception e){
	    			log.error("Error while indexing MSG:"+e.getMessage());
	    		}
	    		sinfo.setScheduler_id(id.intValue());
	    		

	    		
	    		int scheduler_id=sinfo.getScheduler_id();	    		

	    		log.debug(" updateScheduleTask : 5");
	    		
	    		
	    		
	    		
	    		
	    		/*
	    		ArrayList<Integer> ids=new ArrayList();
    			for(Iterator i=newtask_tags.iterator();i.hasNext();){
    				String tid=(String)i.next();
    				ids.add(Integer.parseInt(tid));
    			}
    			sdb.updateTaskTagIds(scheduler_id, ids);
    			    			
    			ArrayList<Integer> fids=new ArrayList();
    			for(Iterator i=follow_tags.iterator();i.hasNext();){
    				String tid=(String)i.next();
    				fids.add(Integer.parseInt(tid));
    			}
    			sdb.updateFollwerTagIds(scheduler_id, fids);
    			
    			
    			
    			
    			*/
	    		//String diff, Map tempdata, String templ_file
	    		
	    		
	    		
	    		SVNSync sync=new SVNSync();
				String diff=null;
				if(sinfo.getRevision()>=0){
					diff=sync.getLastChanged(scheduler_id);
				}else {
					diff="@@@No modification on script@@ there may be change on other than script for example trigger times, name and etc..  ";
					
				}
				
				HashMap hdata=new HashMap();
				hdata.put("task_name", name);
				hdata.put("scheduler_id", scheduler_id);
				hdata.put("current_user", getAuthorizedUser(sdb));
				hdata.put("diff", diff);
				hdata.put("comments", comment);
				String templ_filename="scheduler_modified_alert.txt";
				updateAllItemTags(scheduler_id, newtask_tags,follow_tags,sdb,sdb,name,  comment,sinfo.getRevision(), diff, hdata, templ_filename) ;
				
				
				
	    		//updateAllItemTags(scheduler_id, newtask_tags,follow_tags,sdb, name, comment,sinfo.getRevision(), );	    		
	    		//notifyLastModification(sdb,name,scheduled_id,comment,sinfo.getRevision());
	    		
    			
    			log.debug(" updateScheduleTask : 5");
	    		sdb.setTriggerData(scheduler_id, triggerdata);
	    		
	    		Map data1=sdb.getScheduler(scheduler_id);    
	    		try{
	    			SchedulerEngine engine=new SchedulerEngine();   		
	    			engine.updateJob(data1, taskuid,sdb);
	    		}catch(Exception e){
	    			log.error("Couldn't update scheduler engine as this server isn't set as load balance manager");
	    		}
	    		
	    		Map rtndata=sdb.getScheduler(scheduler_id);
	    		Vector v=new Vector();
	    		v.add(rtndata);
	    		
	    		log.debug(" updateScheduleTask : 6");
	    		
	    		
	    		//sdb.closeDB();	
	    		//return listScheduledItems();
	    		
	    		rtn.put("scheduleditems", v);
	    		rtn.put("scheduler_id", scheduler_id);	    		
	    		log.debug(" updateScheduleTask : 7");
	    		
	    		rtn.putAll(getThemeAccessData(sdb));
	    		
	    		/*
	    		AuthUser auth=getAuthenticatedUserObj(sdb);
	    		 
	    		if(auth!=null){
	    			  	rtn.put("rwx_tags",auth.getRwx());
	    			   	rtn.put("rx_tags",auth.getRx());
	    			   	rtn.put("r_tags",auth.getR());			    	
	    		}
	    		
	    		String superuser=(String)getRequest().getSession().getAttribute(ValidateSuperUser.SESSION_LOGGED_SUPERUSER);
	    		if(superuser!=null && !superuser.equals("")){
	    			rtn.put("superuser",superuser);
	    		}
	    		*/ 
	    		return rtn;
	    		
	    	}else{
	    		
	    		ScheduledTask task=new ScheduledTaskFactory().getTask(taskuid);
	    		for(ScheduledTaskField field:task.listFormFields()){
	    			if(field.getPluggindata()!=null){
	    				//rtn.put("pluggindata", field.getPluggindata());
	    				rtn.put("plugginfield", field);
	    			}
	    		}
		    	//String validation_stng=engine.validateTaskData(data,taskuid);
		    	//rtn.put("message", validation_stng);
		    	return rtn ;
		    	 
	    	}
    	
		}catch(Exception e){
			ClientError.reportError(e, null);
			e.printStackTrace();
			throw e;			
		}finally{
			sdb.closeDB();
		}
	}
	
	 
	private boolean isCallingAnotherScript(String rscript) throws Exception {
		
		//log.debug("rscript:"+rscript);
		boolean calling=false;
		StringTokenizer st=new StringTokenizer(rscript,"\r\n;{}");
		while(st.hasMoreTokens()){
			String line=st.nextToken();
			if(line.contains("#")){
				line=line.substring(0,line.indexOf("#"));
			}

			if(line.toLowerCase().contains(CALLING_ANOTHER_SCRIPT_PATTERN.toLowerCase())){
				log.debug("~~~~calling executeSchedulerScript()");
				calling=true;
			}
			log.debug("line:"+line+" contains:"+line.toLowerCase().contains(CALLING_ANOTHER_SCRIPT_PATTERN.toLowerCase()));
		}
		return calling;
		
		
	}
	
	public void updateCallingAnotherScript(int start, int end) throws Exception {
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		try{
			sdb.connectDB();
			for(int i=start;i<end;i++) {
				
				Map data=sdb.getScheduler(i);
				if(data!=null && data.get("rscript")!=null) {
					String rscript=(String)data.get("rscript");
					boolean calling=isCallingAnotherScript(rscript);
					sdb.updateCallingAnotherScript(i, calling?1:0);
				}
				
			}
		}catch(Exception e){
			ClientError.reportError(e, null);
			throw e;
		}
		
		
	}
	
	
    /*   
	public Map renameFolder(int folderid, String foldername)  throws Exception {
		try{
			SchedulerDB sdb=SchedulerDB.getSchedulerDB();
			sdb.connectDB();
			sdb.renameFolder(folderid, foldername);
			new ScheduledTaskFactory().refreshTaskLoaded();
			return listScheduledItems();
		}catch(Exception e){
			ClientErrorMgmt.reportError(e, null);
			throw e;
		}
	}
	*/
	public boolean renameFolder(String taskuid,String oldfolder, String newfolder)  throws Exception {
		try{
			SchedulerDB sdb=SchedulerDB.getSchedulerDB();
			sdb.connectDB();
			sdb.renameFolder(taskuid,oldfolder,newfolder);
			new ScheduledTaskFactory().refreshTaskLoaded();
			return true;
		}catch(Exception e){
			ClientError.reportError(e, null);
			throw e;
		}
	}

	
	public Map moveTasks(String d_tkuid,int d_fid, String s_tkuid,int s_fid)  throws Exception {
		try{
			SchedulerDB sdb=SchedulerDB.getSchedulerDB();
			sdb.connectDB();
			sdb.moveTasks(d_tkuid, d_fid, s_tkuid, s_fid);
			new ScheduledTaskFactory().refreshTaskLoaded();
			return listScheduledItems();
		}catch(Exception e){
			ClientError.reportError(e, null);
			throw e;
		}
	}
	
	
	public boolean moveFolderContents(String foldername, String taskuid, String parentfolder, String targetuid, String targetpath)  throws Exception {
		try{
			SchedulerDB sdb=SchedulerDB.getSchedulerDB();
			sdb.connectDB();
			//sdb.deleteFolder(folderid);
			ArrayList<Integer> ids=sdb.moveItems(foldername, taskuid, parentfolder, targetuid, targetpath);
			
			SchedulerEngine engine=new SchedulerEngine();
			for(Integer scheduler_id: ids){
				Map data1=sdb.getScheduler(scheduler_id);    
	    		try{    	    			
	    			engine.removeJob(scheduler_id, data1, taskuid); //remove the job first that linked with previous taskuid	    			
	    			engine.updateJob(data1, targetuid,sdb); //add job with new taskuid
	    		}catch(Exception e){
	    			log.error("Couldn't update scheduler task id:"+scheduler_id+" when folder "+foldername+" moved");
	    		}
			}   		
			new ScheduledTaskFactory().refreshTaskLoaded();
			return true;
		}catch(Exception e){
			ClientError.reportError(e, null);
			throw e;
		}
	}
	
	public Map deleteFolder(int folderid)  throws Exception {
		try{
			SchedulerDB sdb=SchedulerDB.getSchedulerDB();
			sdb.connectDB();
			sdb.deleteFolder(folderid);
			new ScheduledTaskFactory().refreshTaskLoaded();
			return listScheduledItems();
		}catch(Exception e){
			ClientError.reportError(e, null);
			throw e;
		}
	}
	
	
	public boolean addFolder(String taskuid, String foldername)  throws Exception {
		try{
			Map rtn=new HashMap();
			
			SchedulerDB sdb=SchedulerDB.getSchedulerDB();
			sdb.connectDB();
			long id=sdb.addFolder(taskuid, foldername);
			new ScheduledTaskFactory().refreshTaskLoaded();
			
		 
			
			return true;
			
			//return listScheduledItems();
		}catch(Exception e){
			ClientError.reportError(e, null);
			throw e;
		}
	}

	
	public int toggleActive(int scheduler_id)  throws Exception {
		try{
			SchedulerDB sdb=SchedulerDB.getSchedulerDB();
			sdb.connectDB();			 
			int rtn=sdb.toggleActive(scheduler_id);
			
			String message="Task "+(rtn==0 ? " Unpaused ":" Paused ");
			String user=getAuthenticatedUser();			
			Map data1=sdb.getScheduler(scheduler_id);
			
    		//sdb.closeDB();
    		//data.put("id", scheduler_id);
			sdb.addEditLogs(scheduler_id, user, message);
			
			String name=(String)data1.get("name");
			notifyLastModification(sdb,name , scheduler_id, message, 0);
			
			log.debug("just before update...");
    		String taskuid=(String)data1.get("taskuid");
    		new SchedulerEngine().updateJob(data1, taskuid,sdb); 
			
			sdb.closeDB();
			return rtn;
		}catch(Exception e){
			ClientError.reportError(e, null);			
			throw e;
			
		}
	}
	
	
	
	public String getLogMessages(int logger_id) throws Exception {
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		sdb.connectDB();
		String msgs=sdb.getLogMessages(logger_id);
		sdb.closeDB();
		return msgs;
	}
	
 
	/*
	public String getLogMessages(int log_id ) throws Exception {
		try{
			SchedulerDB sdb=new SchedulerDB();
			sdb.connectDB();
			
			String rtn=sdb.getLogMessages(log_id);
			
			
			sdb.closeDB();
			
			return rtn;
			
		}catch(Exception e){
			ClientErrorMgmt.reportError(e, null);
			throw e;
		}
		
	}
	*/
	
	
	public String getNext5Times(int scheduler_id ) throws Exception {
		
		try{
			SchedulerDB sdb=SchedulerDB.getSchedulerDB();
			sdb.connectDB();
			
			Map data=sdb.getScheduler(scheduler_id);
			
			Number active=(Number)data.get("active");
			if(active!=null && active.intValue()==-1 ){
					throw new Exception("Task is inactive and no queue for this task");
			}
			String taskuid=(String)data.get("taskuid");
			String name=(String)data.get("name");
			String timezs=(String)data.get("timezone");
			TimeZone timezone=null;
			if(timezs!=null && !timezs.equals("")){
				timezone=TimeZone.getTimeZone(timezs);
			}else{
				timezone=TimeZone.getDefault();
			}
			String time5="The followings are task execution time ("+timezone.getID()+") pattern:\n";
			time5+="--------------------------------------\n";
	    	time5+=new SchedulerEngine().getNext5Times(name, taskuid, timezone,scheduler_id);
	    	
			
			
			sdb.closeDB();
			
			return time5;
			
		}catch(Exception e){
			ClientError.reportError(e, null);
			throw e;
		}
		
	}
	
	
	public Map removeTask(int scheduler_id) throws Exception {
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		try{
				
    			sdb.connectDB();

    			List depids=sdb.getDependsThis(scheduler_id);
    			List depNotRemove = new ArrayList();
    			if (depids.size()>0) {
    				depNotRemove = sdb.searchScheduler(StringUtils.join(depids, ','));	
    			}
    			
    			if(depNotRemove.size()>0){
    				throw new Exception("Deleting faild. Task Ids "+depids+" depends this task");
    			}else{
	    			Map data=sdb.getScheduler(scheduler_id);
	    			String taskuid=(String)data.get("taskuid");
	    			new SchedulerEngine().removeJob(scheduler_id, data, taskuid);
	    			
		    		sdb.deleteScheduler(scheduler_id);
		    		sdb.removeQueueLog(new Date().getTime(),scheduler_id);
		    		sdb.closeDB();
	    			
		    		return listScheduledItems();
    			}
    	
		}catch(Exception e){
			ClientError.reportError(e, null);
			throw e;
		}finally{
			sdb.closeDB();
		}
	}
	
	public Map putBackTask(int scheduler_id) throws Exception {
		
		try{
				HashMap rtn=new HashMap(); 
			
				SchedulerDB sdb=SchedulerDB.getSchedulerDB();
    			sdb.connectDB();
    			
    			List depids=sdb.getDependsTo(scheduler_id);
    			for (int i=0; i<depids.size(); i++) {
    				int depId = ((Integer) depids.get(i)).intValue();
    				if (sdb.isDeleted(depId)) {
						Map schedulerMap = sdb.getScheduler(depId);
						String taskName = (String) schedulerMap.get("name");
    					throw new Exception("Put back task failed. This task depend to " + depId + " ('" + taskName + "'). Put back task "+depId+" first.");
    				}
    			}    			
    			
    			
	    		sdb.putBackScheduler(scheduler_id);
	    		
    			Map data=sdb.getScheduler(scheduler_id);
    			
    			String taskuid=(String)data.get("taskuid");
    			 
        		new SchedulerEngine().updateJob(data, taskuid,sdb); 
        		
    			
	    		Vector v=new Vector();
	    		v.add(data);
	    		
	    		
	    		
	    		
	    		sdb.closeDB();	
	    		//return listScheduledItems();
	    		
	    		rtn.put("scheduleditems", v);
	    		rtn.put("scheduler_id", scheduler_id);
	    		
	    		sdb.closeDB();
    		
	    		return rtn;
    	
		}catch(Exception e){
			ClientError.reportError(e, null);
			throw e;
		}
	}
	
	
	public boolean purgeTask(int scheduler_id) throws Exception {
		
		try{
				SchedulerDB sdb=SchedulerDB.getSchedulerDB();
    			sdb.connectDB();
    			
    			Map data=sdb.getScheduler(scheduler_id);
    			String taskuid=(String)data.get("taskuid");
    			new SchedulerEngine().removeJob(scheduler_id, data, taskuid);
    			
    			ScheduledTask task=new ScheduledTaskFactory().getTask(taskuid);
	    		
	    		for(ScheduledTaskField field:task.listFormFields()){
	    			if(field.getPluggindata()!=null){	    				 
	    				PlugginInterface pl=SchedulerPlugginRegister.getPluggin(field.getFieldtype());
	    				if(scheduler_id>0){
	    					try{
	    						pl.deleteAction(scheduler_id,getRequest());
	    					}catch(Exception e){
	    						log.error("Error:"+e.getMessage());
	    					}
	    				}
	    			}
	    		}
				
	    		
	    		sdb.purgeScheduler(scheduler_id);	    		
	    		sdb.removeQueueLog(new Date().getTime(),scheduler_id);
	    		sdb.closeDB();
 
	    		//return listScheduledItems();
	    	   return true;
    	
		}catch(Exception e){
			ClientError.reportError(e, null);
			throw e;
		}
	}
	
	/**
	 * @deprecated
	 * @param scheduler_id
	 * @param log_id
	 * @return
	 * @throws Exception
	 * this method to be removed later and should not be called in any new development
	 */
			
	public Collection executeTaskNow(int scheduler_id, int log_id) throws Exception {
		
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		try{

			
    			sdb.connectDB();
    			
    			log.debug("log_id:"+log_id);
    			if(log_id>0){
    				
    				sdb.updateQueueLogStatus(log_id,LOG_STATUS_RESTARTED, P2PService.getComputerName());    				
    			}
    			Map data=sdb.getScheduler(scheduler_id);
    			
    			Number active=(Number)data.get("active");
    			//if(active!=null && active.intValue()==-1 ){
    			//		throw new Exception("Inactive task can't be executed");
    			//}
    			String taskuid=(String)data.get("taskuid");
    			String name=(String)data.get("name");
    			String user=getAuthenticatedUser();  
    			String inject_code=null;
    			new SchedulerEngine().executeJobNow(name, taskuid,data,sdb,user,inject_code);
    			
	    	
    		
	    		return getQueueLogs(null);
	    	
    	
		}catch(Exception e){
			ClientError.reportError(e, null);
			throw e;
		}finally {
			sdb.closeDB();
		}
	}
	
	public Collection executeTask(int scheduler_id, int log_id,int minutes_delay) throws Exception {
		
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		try{

				
    			sdb.connectDB();
    			
    			log.debug("log_id:"+log_id);
    			if(log_id>0){
    				
    				sdb.updateQueueLogStatus(log_id,LOG_STATUS_RESTARTED, P2PService.getComputerName());    				
    			}
    			Map data=sdb.getScheduler(scheduler_id);
    			
    			Number active=(Number)data.get("active");
    			//if(active!=null && active.intValue()==-1 ){
    			//		throw new Exception("Inactive task can't be executed");
    			//}
    			String taskuid=(String)data.get("taskuid");
    			String name=(String)data.get("name");
    			String user=getAuthenticatedUser();
    			
    			String inject_code=null;
    			
    			if(minutes_delay>0){
    				new SchedulerEngine().runJobDelayed(data, taskuid,sdb,minutes_delay,user);
    			}else{
    				new SchedulerEngine().executeJobNow(name, taskuid,data,sdb,user,inject_code);
    			}
    			
	    		
    		
	    		return getQueueLogs(null);
	    	
    	
		}catch(Exception e){
			e.printStackTrace();
			
			ClientError.reportError(e, null);
			throw e;
		}finally {
			sdb.closeDB();
		}
	}
	
	
	private static JCS cache=null;
	
	 
	
	private Number convertNumber(Object obj){
		if(obj instanceof Number){
			return (Number)obj;
		}if(obj instanceof String){
			Integer num=new Integer(Integer.parseInt((String)obj));
			return  num;
		}
		return null;
	}
	
	
	
	
	
	private static boolean TIMER_RUNNING=false; 
	private synchronized  void setTimerRunning(boolean flag){
		TIMER_RUNNING=flag;
	}
	
	
	private static HashMap datamap=new HashMap();
	private synchronized static Object accessData(int action, String key){
		//action =1 to access the data
		//action =2 to remove the data.
		Object rtn=null;
		if(action==1 && key!=null && datamap!=null && datamap.containsKey(key)){
		      	rtn=datamap.get(key);
		}else if (action==2){
		      datamap.clear();
		}
		return rtn;
	}
	
	

   private String getEnbledTaskTypes(){
	   
	    Vector vs=SchedulerEngine.getEnabledTaskTypes();
		String tasktypes="";
		for(Object taskuid: SchedulerEngine.getEnabledTaskTypes()){
			tasktypes+=(tasktypes.equals("") ? "'"+taskuid+"'":",'"+taskuid+"'");
		}
		
		return tasktypes;
		
   }

   /**
    * 
    * @param dobj
    * @return
    * @throws Exception
    */
  public Collection getQueueLogs(Map dobj) throws Exception {
	  
		String datequery = ConstructQueryDB.getConstructQueryDB().constructQueueHistoryQuery(dobj);
		
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		sdb.connectDB();
		
		if(cache==null){
			cache=JCS.getInstance("logQueues");
		}	 
		IElementAttributes att= cache.getDefaultElementAttributes();
		att.setMaxLifeSeconds(2);
 
		try{
			
			sdb.setReadOnly(true);
			
			Date now=new Date();
			List c=null;
			
			Map logdata= getExecutingTasks();
			log.debug("log data"+logdata);
			
			Vector all_ids=(Vector)logdata.get("queued");
			
			//String enabledtaskuids=getEnbledTaskTypes();
			
			if(datequery==null){
				if(cache.get("c1")==null){
					 List<LoadBalancingQueueItem> qidst=LoadBalancingQueue.getDefault().getAllTasks(); //ScheduledTaskQueue.getQueuedTaskIds();
					 String ids=null;
					 Vector qids=new Vector();
					 
					 for(LoadBalancingQueueItem it:qidst){
						 // ids=(ids==null)? ""+id:ids+","+id;
						 String id_tr=it.getSchedulerid()+"_"+it.getSf().getTrigger_time();
						 ids=(ids==null)?    "'"+id_tr+"'":		 ids+",'"+id_tr+"'";
						 qids.add(id_tr);
					 }		
					
					 IElementAttributes att1= cache.getDefaultElementAttributes();
					 att1.setMaxLifeSeconds(3);
					 
					 List c1=(List)cache.get("qLogsDb");
					 if(c1==null ){						 
					 
						 c=sdb.listOfQueueLogs(ids,now.getTime());
						 if(c!=null)cache.put("qLogsDb",c,att1);
 
					 }else{
						 c1.size(); //access any of the method to make sure it wont't expire
						 c=c1;
					 }
					 
					 log.debug("c1:"+c.size()+" all_ids:"+((all_ids!=null)?all_ids.size():0)+" qidst:"+qidst.size()); 
						 
 					 
					 if(IncomingMessage.getExecutingPeersTime()!=null ){ 
						  Collection sid_times=IncomingMessage.getExecutingPeersTime().values();
						  log.debug("sid_times"+sid_times);
						  try{
							  synchronized(sid_times){
								  for(Iterator<Map> i=sid_times.iterator();i.hasNext();){
									  	Map sisd=i.next();
										if(sisd!=null){
											try{
												int scid=(Integer)sisd.keySet().iterator().next();
												long trig=(Long)sisd.values().iterator().next();	
												String id_tr1=scid+"_"+trig;
												 
												if(!qids.contains(id_tr1)){
													
													Map rec=sdb.listOfQueueLog(scid,trig);												
													//rec.put("crashed", true);													
													Number numb=(Number)rec.get("is_triggered");
													if(rec!=null && (numb==null || (numb!=null && numb.intValue()!=1)) ){
												
														//c.add(0,rec); //adds on top
														
													}
													if(numb!=null && numb.intValue()==1){
														LoadBalancingQueue.getDefault().removeFaultyProcessingTask(scid, trig);
													
													}												
												}
											}catch(Exception e){
												//log.error("Error:"+e.getMessage());
											}
									  	}else{
									  		//log.error("error: sisd=is null");
									  	}
										 
								  }
							  }
						  }catch(Exception e){
							  log.error("Error e:"+e.getMessage());
						  }
						  
						  
					 }	
					  
					 if(c!=null) cache.put("c1",c,att);
					 //System.out.println("storing...");
				}else{
					c=(List)cache.get("c1");
					//System.out.println("retrieving...");
				}
			}else{
				if(cache.get("c2")==null){
				  c=sdb.listOfHistoryQueueLogs(now.getTime(),datequery);
				  if(c!=null)cache.put("c2",c,att);
				}else{
				  c=(List)cache.get("c2");
				}
			}

			
			if(c==null) {
				return new ArrayList();
			}
			
			Vector excecuting=(Vector)logdata.get("queued");
			Map pfnames=(Map)logdata.get("pfnames");
 
			log.debug("execting:"+excecuting);
			//log.debug("c:"+c);
			populateHistoryLogs(c,pfnames,all_ids,excecuting);
	
			log.debug("c:"+c.size());
			return c;
		}catch(SQLException e1){
			
			//log.error("SQL Error:"+e1.getMessage()+"\n -->Call stack at this time:\n");			
			//log.error(SchedulerDB.collectStack4All());
			log.error("Error:"+e1.getMessage());
			
			//e1.printStackTrace();
			
			return new ArrayList();
			
			
		}catch(Exception e){	
			ClientError.reportError(e, null);
			throw e;
		}finally{
			sdb.closeDB();
		}
   }
  
  
  //public String getConsoleMsg(int scheduler_id, long trigger_time) throws Exception

  public String getConsoleMsg(String unique_id) throws Exception {
		SchedulerDB sdb = SchedulerDB.getSchedulerDB();

		try {
 			int scheduler_id=0;
   			long trigger_time=0;
   			
   			StringTokenizer st=new StringTokenizer(unique_id,"_");
		    if(st.countTokens()>=2){
		    	try{
		    		String t1=st.nextToken();
		    		String t2=st.nextToken();
		    		scheduler_id=Integer.parseInt(t1);
		    		trigger_time=Long.parseLong(t2);
		    	}catch(Exception e){log.error("error while parsing: "+unique_id);}
		    }
  
		    
			sdb.connectDB();
			String rtn= sdb.getConsoleMsg(scheduler_id, trigger_time);
			
			return rtn;
		} catch (Exception e) {
			ClientError.reportError(e, null);
			throw e;
		} finally {
			sdb.closeDB();
		}
}

  
  
  public Collection getLast15Logs(int scheduler_id) throws Exception {
		SchedulerDB sdb = SchedulerDB.getSchedulerDB();
		try {
			sdb.connectDB();
			List c = sdb.listOfLast15Logs(new Date().getTime(), scheduler_id);
			populateHistoryLogs(c, new HashMap(), new Vector(), new Vector());
			return c;
		} catch (Exception e) {
			ClientError.reportError(e, null);
			throw e;
		} finally {
			sdb.closeDB();
		}
  }
  
   private void populateHistoryLogs(List c,Map pfnames,List all_ids,List excecuting) {
		for(Iterator i=c.iterator();i.hasNext();){
			Map record=(Map)i.next();
			if(record!=null){
				//log.debug("record:"+record);
				//replace host names with friendly names
				String host=(String)record.get("host");
			    if(pfnames!=null && pfnames.get(host)!=null){
			    	record.put("host",pfnames.get(host));
			    }
				Number scheduler_id=(Number)record.get("scheduler_id");
				Number trigger_time;
				if(record.get("trigger_time") instanceof String){
					 trigger_time=(Number)record.get("trigger_time_or");
				}else{
					 trigger_time=(Number)record.get("trigger_time");	
				}
						
				String uid=scheduler_id+"_"+trigger_time;
				if(all_ids.contains(uid)){
					record.put("queued", 1);
				}else{
					record.put("queued", 0);
				}
				
				
				String tzsuffix="";
				String timezone=(String)record.get("timezone");
		    	List list=Arrays.asList(TimeZone.getAvailableIDs());
		    	TimeZone currentTZ=null;
		    	if(timezone!=null && !timezone.trim().equals("") && list.contains(timezone.trim())) {
		    		tzsuffix=" "+TimeZone.getTimeZone(timezone).getDisplayName(false, TimeZone.SHORT)+"";
		    		 
		        }
		    	
				//if(uid.equals(currentexec_id)){
				if(excecuting.contains(uid)){
					record.put("executing", 1);
				}
				record.put("unique_id", scheduler_id+"_"+trigger_time);			
				if(trigger_time!=null){
					Date d1=new Date();
					Date d2=d1;
					
					d1.setTime(trigger_time.longValue());					
					record.put("trigger_time_or",trigger_time );
					record.put("trigger_time", getFriendlyTime(d1,timezone)+tzsuffix);
				}
				int is_triggered=(record.get("is_triggered")==null?0: ((Number)record.get("is_triggered")).intValue());				
				//Date sd=(Date)record.get("start_time");
				//Date ed=(Date)record.get("end_time");
				Timestamp s=(Timestamp)record.get("start_time");
				Timestamp e=(Timestamp)record.get("end_time");
				
				if(record.get("db_connection_ids")!=null){
					//remove db_connection_ids, as it may slow down the client.
					record.remove("db_connection_ids");
				}
				
				
				 //SimpleDateFormat dateFormat1 =new SimpleDateFormat("hh:mm:ss");
				SimpleDateFormat dateFormat =new SimpleDateFormat("mm:ss");
				SimpleDateFormat dateFormat2 =new SimpleDateFormat("hh:mm:ss");
				 
				dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
				dateFormat2.setTimeZone(TimeZone.getTimeZone("GMT"));
				if(s!=null){
					 Date d3=new Date();
					 d3.setTime(trigger_time.longValue());
					 long diff3=s.getTime()-d3.getTime();
					 if(diff3>=1000){
						 record.put("started_at",dateFormat.format(new Date(diff3)));
					 }
					 //record.put("started_at",dateFormat1.format(s));
				}
				if(e!=null && s!=null){
					 long diff=e.getTime()-s.getTime();
					 if(diff>3600000){
						 record.put("duration", dateFormat2.format(new Date(diff)));
					 }else{
						 record.put("duration", dateFormat.format(new Date(diff)));
					 }
				}
			 
			} //if record !=null
		}
   }

   	public Vector getDataLogHistory(String unique_id) throws Exception {
   		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
   		try{

   			int scheduler_id=0;
   			long trigger_time=0;
   			
   			StringTokenizer st=new StringTokenizer(unique_id,"_");
		    if(st.countTokens()>=2){
		    	try{
		    		String t1=st.nextToken();
		    		String t2=st.nextToken();
		    		scheduler_id=Integer.parseInt(t1);
		    		trigger_time=Long.parseLong(t2);
		    	}catch(Exception e){log.error("error while parsing: "+unique_id);}
		    }
   			
			sdb.connectDB();
			Vector data=sdb.getDataLogHistory(scheduler_id, trigger_time);
			return data;
    	
	
   		}catch(Exception e){
   			ClientError.reportError(e, null);
   			throw e;
   		}finally{
   			sdb.closeDB();
   		}
   		
   
   	}



   	/**
   	 * 
   	 * @return
   	 * @throws Exception
   	 */
	public  Map getExecutingTasks() throws Exception {
		try{ 
	
		if(cache==null){
			cache=JCS.getInstance("logQueues");
		}	 
		IElementAttributes att= cache.getDefaultElementAttributes();
		att.setMaxLifeSeconds(2);
		
		if(cache.get("getExecutingTasks")!=null){
			Map t1=(Map)cache.get("getExecutingTasks");			 
			t1.put("servertime", new SimpleDateFormat("MMMMM dd, yyyy HH:mm:ss").format(new Date()));
			t1.put("onlinepeers", getOnlinePeers());
			return t1;
		}else{
			
			Vector rtn=new Vector();
			 
			//currently executing task
			TreeMap t=new TreeMap();
			TreeMap emachines=new TreeMap();		
			
			
			Collection<LoadBalancingQueueItem> executingTasks=LoadBalancingQueue.getDefault().getExecutingTasks();

			
			Collection<LoadBalancingQueueItem> queuedTasks=LoadBalancingQueue.getDefault().getQueuedTasks();
			
			
			//Map<String,Map> extasktimes=IncomingMessage.getExecutingPeersTime();
			
			Vector executing=new Vector();
			for(Iterator<LoadBalancingQueueItem> i=executingTasks.iterator();i.hasNext();){
				LoadBalancingQueueItem item=i.next();
				String uid=item.getSchedulerid()+"_"+item.getSf().getTrigger_time();
				rtn.add(uid);
				executing.add(uid);				
				emachines.put(uid, item.getMachine());
			}
			
	 
			for(Iterator<LoadBalancingQueueItem> i=queuedTasks.iterator();i.hasNext();){
				LoadBalancingQueueItem item=i.next();
				String uid=item.getSchedulerid()+"_"+item.getSf().getTrigger_time();
				rtn.add(uid);			 
			}
			
			/*
			log.debug("queued items:"+queuedTasks.size());
			if(extasktimes!=null){
				try{
					for(Iterator<String> i=extasktimes.keySet().iterator();i.hasNext();){
						String peer=i.next();
						Map<Integer,Long> idtime=extasktimes.get(peer);
						if(idtime!=null){
                            for(Integer s_id:  idtime.keySet()){							
								//int s_id=(Integer)idtime.keySet().iterator().next();
								//long t_id=(Long)idtime.values().iterator().next();
                            	
								long t_id=idtime.get(s_id);
								String uid=s_id+"_"+t_id;
								if(!rtn.contains(uid)){ 
									rtn.add(uid);					
									executing.add(uid);
									//executingsids.add(s_id);
									emachines.put(uid, peer);
								}
                            }
						}
					}
				}catch(Exception e){    }
			}
			*/
			
			//t.put("alltasks", rtn);
			
			TreeMap t1=new TreeMap();
			
			//Vector ids=ScheduledTaskQueue.getQueuedIds();
			//Map logdata=ScheduledTaskQueue.getQueuedIds();
			//Vector all_ids=(Vector)logdata.get("alltasks");
			//String currentexec_id=(String)logdata.get("executing");
			
			t1.put("servertime", new SimpleDateFormat("MMMMM dd, yyyy HH:mm:ss").format(new Date()));
			//t1.put("istarted", new SchedulerEngine().isSchedulerStarted());
			t1.put("lastexecutedtime", LoadBalancingQueue.getDefault().lastExcecutedTime());
			t1.put("queued", rtn);			
			if(P2PService.getComputerName().equalsIgnoreCase("4ecappcsg5") && !getRequest().getContextPath().contains("tomcat_beta")){
				
			}else{
				t1.put("onlinepeers", getOnlinePeers());				
			}			
			HashMap excount=new HashMap();
			int last10sec=10000;
			List<PeerMachine> online=new PeerManagerHSQL().getOnlinePeers(last10sec );
			for(PeerMachine pm:online){
				if(pm.getRunning().size()>0){
					excount.put(pm.getPeername(), pm.getRunning().size());
				}
			}
			t1.put("exe_count",excount);
			t1.put("executing",executing);
			t1.put("executingpeers",emachines);
			
			t1.put("pfnames", getPeerFriendlyNames());
			
			try{
				//t1.put("x_executingids",executingsids);
				//t1.put("x_completed",percentageCalc(executingsids,executingTasks));
				t1.put("x_completed",percentageCalc1(executingTasks));
				
			}catch(Exception e){
				t1.put("x_completed_error",e.getMessage());
			}
			t1.put("hostname",P2PService.getComputerName());
			cache.put("getExecutingTasks",t1,att);
			return t1;
		}
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}

	
  /**
  * @deprecated
  * @return
  * @throws Exception
  */
	private  Map getExecutingTasks_old() throws Exception {
		try{ 
	
		if(cache==null){
			cache=JCS.getInstance("logQueues");
		}	 
		IElementAttributes att= cache.getDefaultElementAttributes();
		att.setMaxLifeSeconds(2);
		
		if(cache.get("getExecutingTasks")!=null){
			Map t1=(Map)cache.get("getExecutingTasks");			 
			t1.put("servertime", new SimpleDateFormat("MMMMM dd, yyyy HH:mm:ss").format(new Date()));
			t1.put("onlinepeers", getOnlinePeers());
			return t1;
		}else{
			
			Vector rtn=new Vector();
			 
			//currently executing task
			TreeMap t=new TreeMap();
			TreeMap emachines=new TreeMap();		
			
			
			Collection<LoadBalancingQueueItem> executingTasks=LoadBalancingQueue.getDefault().getExecutingTasks();
			//System.out.println("SchedulerMgmt.getExecutingTasks(): executingTasks:"+executingTasks);
			
			Collection<LoadBalancingQueueItem> queuedTasks=LoadBalancingQueue.getDefault().getQueuedTasks();
			
			
			Map<String,Map> extasktimes=IncomingMessage.getExecutingPeersTime();
			
			Vector executing=new Vector();
			//Vector executingsids=new Vector();
			for(Iterator<LoadBalancingQueueItem> i=executingTasks.iterator();i.hasNext();){
				LoadBalancingQueueItem item=i.next();
				String uid=item.getSchedulerid()+"_"+item.getSf().getTrigger_time();
				rtn.add(uid);
				executing.add(uid);
				//executingsids.add(item.getSchedulerid());
				emachines.put(uid, item.getMachine());
			}
			
			
			
			//t.put("executing",executing);
			
	 
			for(Iterator<LoadBalancingQueueItem> i=queuedTasks.iterator();i.hasNext();){
				LoadBalancingQueueItem item=i.next();
				String uid=item.getSchedulerid()+"_"+item.getSf().getTrigger_time();
				rtn.add(uid);			 
				
			}
			
			log.debug("queued items:"+queuedTasks.size());
			if(extasktimes!=null){
				try{
					for(Iterator<String> i=extasktimes.keySet().iterator();i.hasNext();){
						String peer=i.next();
						Map<Integer,Long> idtime=extasktimes.get(peer);
						if(idtime!=null){
                            for(Integer s_id:  idtime.keySet()){							
								//int s_id=(Integer)idtime.keySet().iterator().next();
								//long t_id=(Long)idtime.values().iterator().next();
                            	
								long t_id=idtime.get(s_id);
								String uid=s_id+"_"+t_id;
								if(!rtn.contains(uid)){ 
									rtn.add(uid);					
									executing.add(uid);
									//executingsids.add(s_id);
									emachines.put(uid, peer);
								}
                            }
						}
					}
				}catch(Exception e){    }
			}
			
			//t.put("alltasks", rtn);
			
			TreeMap t1=new TreeMap();
			
			//Vector ids=ScheduledTaskQueue.getQueuedIds();
			//Map logdata=ScheduledTaskQueue.getQueuedIds();
			//Vector all_ids=(Vector)logdata.get("alltasks");
			//String currentexec_id=(String)logdata.get("executing");
			
			t1.put("servertime", new SimpleDateFormat("MMMMM dd, yyyy HH:mm:ss").format(new Date()));
			//t1.put("istarted", new SchedulerEngine().isSchedulerStarted());
			t1.put("lastexecutedtime", LoadBalancingQueue.getDefault().lastExcecutedTime());
			t1.put("queued", rtn);
			
			if(P2PService.getComputerName().equalsIgnoreCase("4ecappcsg5") && !getRequest().getContextPath().contains("tomcat_beta")){
				
			}else{
				t1.put("onlinepeers", getOnlinePeers());
			}
			
			t1.put("executing",executing);
			t1.put("executingpeers",emachines);
			
			t1.put("pfnames", getPeerFriendlyNames());
			
			try{
				//t1.put("x_executingids",executingsids);
				//t1.put("x_completed",percentageCalc(executingsids,executingTasks));
				t1.put("x_completed",percentageCalc1(executingTasks));
				
			}catch(Exception e){
				t1.put("x_completed_error",e.getMessage());
			}
			t1.put("hostname",P2PService.getComputerName());
			cache.put("getExecutingTasks",t1,att);
			return t1;
		}
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}

	
	

	private Map percentageCalc1(Collection<LoadBalancingQueueItem> executingTasks ) throws Exception {
		try{
		 
    			//Map record=(Map)i.next();
				//Number scheduler_id=(Number)record.get("scheduler_id");    			
    			//Timestamp s=(Timestamp)record.get("start_time");
				//Timestamp e=(Timestamp)record.get("end_time");
				
				long cur_dura=-1;
				TreeMap t=new TreeMap(); 
				String triggerTime=null;;
				for(Iterator<LoadBalancingQueueItem> ia=executingTasks.iterator();ia.hasNext();){
					LoadBalancingQueueItem item=ia.next();	
					if(item.getStarted()!=null){
						cur_dura=new Date().getTime()-item.getStarted().getTime();
						triggerTime=item.getSf().getTrigger_time()+"";						
						t.put(item.getSchedulerid()+"_"+triggerTime, cur_dura+"|"+item.getLastExecutedDuration());
					}
				}
				
				return t;
		}catch(Exception e){
			//ClientErrorMgmt.reportError(e, null);
			throw e;
		} 
	}
	
	
	private Map percentageCalc(Vector scheduler_ids,Collection<LoadBalancingQueueItem> executingTasks ) throws Exception {
		
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		try{
			TreeMap t=new TreeMap();
    		sdb.connectDB();
    		Vector qlogs=sdb.getLastSuccessfulQLogs(scheduler_ids) ;
    		for(Iterator i=qlogs.iterator();i.hasNext();){
    			Map record=(Map)i.next();
				Number scheduler_id=(Number)record.get("scheduler_id");    			
    			Timestamp s=(Timestamp)record.get("start_time");
				Timestamp e=(Timestamp)record.get("end_time");
				
				long cur_dura=-1;
				long his_dura=e.getTime()-s.getTime();
				//long percent=-1;
				String triggerTime=null;;
				for(Iterator<LoadBalancingQueueItem> ia=executingTasks.iterator();ia.hasNext();){
					LoadBalancingQueueItem item=ia.next();
					if(item.getSchedulerid()==scheduler_id.intValue() && item.getStarted()!=null){
						cur_dura=new Date().getTime()-item.getStarted().getTime();
						triggerTime=item.getSf().getTrigger_time()+"";
					}
				}				
				if(cur_dura>0 && his_dura>0){
					t.put(scheduler_id+"_"+triggerTime, cur_dura+"|"+his_dura);
				}
    		}    		
    	    return t;
		}catch(Exception e){
			//ClientErrorMgmt.reportError(e, null);
			throw e;
		}finally{
			sdb.closeDB();
		}
	}
	
	public boolean executeScript(String name,String script,int restart,Vector peers) throws Exception {
		
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		try{
			//TreeMap t=new TreeMap();
    		sdb.connectDB();
    		int s_id=sdb.addRScript(name, script, restart);
    		//Vector peers=new Vector();
    		//peers.add(peername);
    		LoadBalancingQueue.getDefault().executeScript(peers,s_id);
    		
    		
            return true;
		}catch(Exception e){
			//ClientErrorMgmt.reportError(e, null);
			throw e;
		}finally{
			sdb.closeDB();
		}
	}
	
	
	public Map  getPeersAndTaskExecuting(){
		return IncomingMessage.getExecutingPeers(); 
	}
	
	
	
	
	public Map getPeerData(String what) throws Exception {
			try{
			    if(what.equals("statistics")){
			    	//LoadBalancingQueue.updatePeerData(LoadBalancingQueue.STATISTICS);
			    	LoadBalancingQueue.getDefault().updatePeerData(P2PTransportMessage.COMMAND_STATISTICS);
			    }
			    if(what.equals("rpackages")){
			    	//LoadBalancingQueue.updatePeerData(LoadBalancingQueue.R_PACKAGES);
			    	LoadBalancingQueue.getDefault().updatePeerData(P2PTransportMessage.COMMAND_R_PACKAGES);
			    }
	
				try{				
					Thread.sleep(3000);
				}catch(Exception e){
					
				}
				HashMap rtn=new HashMap();
				TreeMap coldata=new TreeMap();
				Vector col=new Vector();
				Map data=null;
				if(what.equals("statistics")){
				    	data=IncomingMessage.getPeerStatistics();
				 }
				 if(what.equals("rpackages")){
					 data=IncomingMessage.getPeerRPackages();
				 }
				
	 
				    col.add("~~~");
					for(Iterator it=data.keySet().iterator();it.hasNext();){
						String peer=(String)it.next();
						String value=(String)data.get(peer);
						if(peer!=null && value!=null && !value.equals("")){						
							
							StringTokenizer st=new StringTokenizer(value,"|");
							//HashMap h=new HashMap();					 
							while(st.hasMoreTokens()){
								String pair=st.nextToken();
								StringTokenizer st1=new StringTokenizer(pair,"=");
								
								
								if(st1.countTokens()>=2){
									String ky=st1.nextToken();
									String val=st1.nextToken();
									HashMap h=null;
									if(!ky.equalsIgnoreCase("PEER")){
										if(coldata.get(ky)!=null){
											h=(HashMap)coldata.get(ky);									
										}else{																		
											h=new HashMap();
											h.put("~~~", ky);
											coldata.put(ky, h);
										}
										
										h.put(peer, val);
									}
									
									//h.put(ky, val);
								}
							}
							
							if(!col.contains(peer)) col.add(peer);
						}	
					}
				
				
	 		    rtn.put("data", coldata);
	 		    rtn.put("col", col);
	 		    return rtn;
			}catch(Exception e){
				ClientError.reportError(e, "what:"+what);
				throw e;
			}
	}
	

	
	
	public Map getOnlinePeers() throws Exception {
	
		
		if(cache==null){
			cache=JCS.getInstance("onlinePeers");
		}	 
		
		IElementAttributes att= cache.getDefaultElementAttributes();
		att.setMaxLifeSeconds(2);
		
		IElementAttributes att10= cache.getDefaultElementAttributes();
		att10.setMaxLifeSeconds(5);
	
		Map peers1=new HashMap();
		if(cache.get("peers")==null){
			
			String status="NOBUSY";
			
			/*
			HashMap peers=IncomingMessage.getCachedPeers();
			if((peers==null || (peers!=null && peers.size()==0)) && cache.get("peersBkup")!=null){
				peers=(HashMap)cache.get("peersBkup");
			}
			peers1=(Map)peers.clone();			
			if(ScheduledTaskQueue.isExecutingOrQueued()){			
				status="BUSY";
			}
			*/
			
			int last5sec=5000;
			List<PeerMachine> online=new PeerManagerHSQL().getOnlinePeers(last5sec );
			for(PeerMachine p:online){
				peers1.put(p.getPeername(), p.getBusyStatus());
			}			 
			peers1.put(P2PService.getComputerName(),status);
			log.debug("peers1:"+peers1);
			cache.put("peers",peers1,att);
			if(false){
				cache.put("peersBkup",peers1,att10);  //backup in case of data unavailability.
			}
			LoadBalancingQueue.getDefault().findAndUpdateOnlinePeers();
			
		}else{
			peers1=(Map)cache.get("peers");
			//System.out.println("SchedulerMgmt.getOnlinePeers():peers:"+peers1);
		}
		return peers1;
	}
	
	
	//private static Vector stackexecuting=new  Vector();
	 
	private Map getExecutingTasksOld() throws Exception {
		 
		TreeMap t=new TreeMap();
		//Vector ids=ScheduledTaskQueue.getQueuedIds();
		Map logdata=ScheduledTaskQueue.getQueuedIds();
		Vector all_ids=(Vector)logdata.get("alltasks");
		String currentexec_id=(String)logdata.get("executing");
		
		t.put("servertime", new SimpleDateFormat("MMMMM dd, yyyy HH:mm:ss").format(new Date()));
		t.put("istarted", new SchedulerEngine().isSchedulerStarted());
		t.put("lastexecutedtime", ScheduledTaskQueue.lastExcecutedTime());
		t.put("queued", all_ids);
		t.put("executing",currentexec_id);
		t.put("hostname",SchedulerDB.getSchedulerDB().getHostName());
		
		return t;
	}
	
	
	private  String getFriendlyTime(Date time,String timezone){
		
		//SimpleDateFormat format=new SimpleDateFormat("dd MMM, yyyy hh:mm:ss a");
		SimpleDateFormat format=new SimpleDateFormat("dd MMM, HH:mm:ss");
		SimpleDateFormat today_tomm=new SimpleDateFormat("HH:mm:ss");
		 
		Calendar cal1=Calendar.getInstance(); cal1.setTime(time);   
		Calendar cal2=Calendar.getInstance(); cal2.setTime(new Date());


		List list=Arrays.asList(TimeZone.getAvailableIDs());    	
    	if(timezone!=null && !timezone.trim().equals("") && list.contains(timezone.trim())) {    		
    		format.setTimeZone(TimeZone.getTimeZone(timezone));
    		today_tomm.setTimeZone(TimeZone.getTimeZone(timezone));		 
        }
    	
		
		String line=null;
		if(
			cal1.get(Calendar.DAY_OF_MONTH)==cal2.get(Calendar.DAY_OF_MONTH) &&
			cal1.get(Calendar.MONTH)==cal2.get(Calendar.MONTH) &&
			cal1.get(Calendar.YEAR)==cal2.get(Calendar.YEAR) 
		){
			line=" "+today_tomm.format(time);
		}
		
		
		Calendar cal3=Calendar.getInstance(); cal3.setTime(new Date());
		cal3.add(Calendar.DAY_OF_MONTH, 1);
		if(		    				
				cal1.get(Calendar.DAY_OF_MONTH)==cal3.get(Calendar.DAY_OF_MONTH) &&
    			cal1.get(Calendar.MONTH)==cal3.get(Calendar.MONTH) &&
    			cal1.get(Calendar.YEAR)==cal3.get(Calendar.YEAR)
    		){
			line="Tomorrow at "+today_tomm.format(time);
    	}

		
		Calendar cal4=Calendar.getInstance(); cal4.setTime(new Date());
		cal4.add(Calendar.DAY_OF_MONTH, -1);
		if(		    				
				cal1.get(Calendar.DAY_OF_MONTH)==cal4.get(Calendar.DAY_OF_MONTH) &&
    			cal1.get(Calendar.MONTH)==cal4.get(Calendar.MONTH) &&
    			cal1.get(Calendar.YEAR)==cal4.get(Calendar.YEAR)
    		){
			line="Yesterday at "+today_tomm.format(time);
    	}
		
		String rtn1=null;
		if(line==null){
			rtn1="\n"+format.format(time);
		}else{
			rtn1="\n"+line;
		}
		return rtn1;
	}
	
	/*
	private HttpServletRequest getRequest() throws Exception  {
		
		return (request == null && WebContextFactory.get()!=null) ? WebContextFactory.get().getHttpServletRequest() : request;


	}
	*/
	
	
	public boolean setActiveTab(int activetab ) throws Exception {
		HttpSession session=getRequest().getSession();
		session.setAttribute(ACTIVE_TAB_ATTRIBUTE,activetab);
		return true;
	}
	
	public int getActiveTab() throws Exception {
		HttpSession session=getRequest().getSession();
		int rtn=1; //default tab set to 2nd tab market contract, as the first one is very slow.
		//System.out.println("Session active tab:"+session.getAttribute(ACTIVE_TAB_ATTRIBUTE));
		if(session.getAttribute(ACTIVE_TAB_ATTRIBUTE)!=null){
			try{
				rtn=(Integer)session.getAttribute(ACTIVE_TAB_ATTRIBUTE);				
			}catch(Exception e){e.printStackTrace();}
		}
		return rtn;
	}
	
	
	public boolean setShowTags(int showtag ) throws Exception {
		HttpSession session=getRequest().getSession();
		session.setMaxInactiveInterval(86400); // set to 24 hours
		session.setAttribute(TAG_SHOWHIDE_ATTRIBUTE,showtag);
		return true;
	}
	
	private int getShowTags() throws Exception {
		HttpSession session=getRequest().getSession();
		int rtn=1; 
		if(session.getAttribute(TAG_SHOWHIDE_ATTRIBUTE)!=null){
			try{
				rtn=(Integer)session.getAttribute(TAG_SHOWHIDE_ATTRIBUTE);				
			}catch(Exception e){e.printStackTrace();}
		}
		return rtn;
	}
	
	
	public Map  getTaskPeerAssoc(String taskuid) throws Exception {
		
		TreeMap t=new TreeMap();
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		try{		
    			sdb.connectDB();
    			Vector rtn=sdb.getAssociatedPeers(taskuid);
    			Vector all=sdb.getPeersList();
    			if(rtn==null || (rtn!=null && rtn.size()<1)){
    				rtn=all;
    			}
    			t.put("associated", rtn);
    			t.put("all", all);
    			return t;
		}catch(Exception e){
			ClientError.reportError(e, null);
			throw e;
		}finally{
			sdb.closeDB();
		}
	}
	
	
	public Map getData4PeerMatrix() throws Exception {
		HashMap rtn=new HashMap();
		List<ScheduledTask> list=new ScheduledTaskFactory().getTasks();
		
		Vector tasks=new Vector();
		
		
		
		
		Vector orders=getGroupOrder();
		Vector addedTuid=new Vector();
		
		log.debug("orders:"+orders);
		
		
		for(int i=0;i<=orders.size();i++){
			Object item=(i<orders.size())?orders.get(i):null;

			for(Iterator<ScheduledTask> it=list.iterator();it.hasNext();){
				ScheduledTask tsk=it.next();
				log.debug("tsk.getUniqueuid():"+tsk.getUniqueid()+" item:"+item);
				if(item==null || (item!=null && item.equals(tsk.getUniqueid()))){
					ValueObject vo=new ValueObject();
					vo.setKey(tsk.getUniqueid());
					vo.setValue(tsk.getName());										
					//if(!tasks.contains(vo)){
					if(!addedTuid.contains(tsk.getUniqueid())){					
						tasks.add(vo);
						addedTuid.add(tsk.getUniqueid());
					}
					//}
				}
			}
			
		}
		
		
		
		
		/*
		for(Iterator<ScheduledTask> i=list.iterator();i.hasNext();){
			ScheduledTask st=i.next();
			ValueObject vo=new ValueObject();
			vo.setKey(st.getUniqueid());
			vo.setValue(st.getName());
			tasks.add(vo);
		}
		*/
		rtn.put("tasks", tasks);
		
		
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		try{	
			sdb.connectDB();
			Vector v=sdb.getAllPeerAssociation();
			rtn.put("peer_association", v);
			TreeMap p_a=new TreeMap();
			for(Iterator i=v.iterator();i.hasNext();){
				Map t=(Map)i.next();
				String val=t.get("taskuid")+"_"+t.get("peername");
				p_a.put(val,"yes");
			}
			
			List peers=sdb.getPeersList();
			peers=sortStringWithNum(peers);
			
			rtn.put("peers", peers);
			
			ArrayList rserve_p=new ArrayList();			
			rserve_p.add(RServeUnixTask.ENGINE_NAME);
			rserve_p.add(RServeUnixTask.ENGINE_EXECUTER_UNIX_NAME);			//
			rtn.put("rserve_unix_peers", sdb.getPeersList4Engine(rserve_p));
			rtn.put("peernotes", sdb.getPeersData());
			
			int last10sec=10000;
			List<PeerMachine> online=new PeerManagerHSQL().getOnlinePeers(last10sec);
			HashMap pversions=new HashMap();
			for(PeerMachine peer:online){
				pversions.put(peer.getPeername(), peer.getVersion());
			}
			rtn.put("pversion", pversions);
			rtn.put("p_a", p_a);
			
		}catch(Exception e){
			
		}finally{
			sdb.closeDB();
		}
		
		
		return rtn;
		
	}
	private static Pattern splitter = Pattern.compile("(\\d+|\\D+)");
	private List sortStringWithNum(List<String> list) {
		
		String[] strs=new String[list.size()];
		for(int i=0;i<list.size();i++){
			strs[i]=list.get(i);
		}

		Arrays.sort(strs, new Comparator()
				{
			  public int compare(Object o1, Object o2)
			  {
			    // I deliberately use the Java 1.4 syntax, 
			    // all this can be improved with 1.5's generics
			    String s1 = (String)o1, s2 = (String)o2;
			    // We split each string as runs of number/non-number strings
			    ArrayList sa1 = split(s1);
			    ArrayList sa2 = split(s2);
			    // Nothing or different structure
			    if (sa1.size() == 0 || sa1.size() != sa2.size())
			    {
			      // Just compare the original strings
			      return s1.compareTo(s2);
			    }
			    int i = 0;
			    String si1 = "";
			    String si2 = "";
			    // Compare beginning of string
			    for (; i < sa1.size(); i++)
			    {
			      si1 = (String)sa1.get(i);
			      si2 = (String)sa2.get(i);
			      if (!si1.equals(si2))
			        break;  // Until we find a difference
			    }
			    // No difference found?
			    if (i == sa1.size())
			      return 0; // Same strings!

			    // Try to convert the different run of characters to number
			    int val1, val2;
			    try
			    {
			      val1 = Integer.parseInt(si1);
			      val2 = Integer.parseInt(si2);
			    }
			    catch (NumberFormatException e)
			    {
			      return s1.compareTo(s2);  // Strings differ on a non-number
			    }

			    // Compare remainder of string
			    for (i++; i < sa1.size(); i++)
			    {
			      si1 = (String)sa1.get(i);
			      si2 = (String)sa2.get(i);
			      if (!si1.equals(si2))
			      {
			        return s1.compareTo(s2);  // Strings differ
			      }
			    }

			    // Here, the strings differ only on a number
			    return val1 < val2 ? -1 : 1;
			  }

			  ArrayList split(String s)
			  {
			    ArrayList r = new ArrayList();
			    Matcher matcher = splitter.matcher(s);
			    while (matcher.find())
			    {
			      String m = matcher.group(1);
			      r.add(m);
			    }
			    return r;
			  }
		});
		ArrayList rtn=new ArrayList();
		for(int i=0;i<strs.length;i++){
			rtn.add(strs[i]);
		}
		
		return rtn;
	}
	
    private Map getPeerFriendlyNames() throws Exception {
    	if(cache==null){
			cache=JCS.getInstance("logQueues");
		}	
    	Map data=new HashMap();
		IElementAttributes att= cache.getDefaultElementAttributes();
		att.setMaxLifeSeconds(15);
		if(cache.get("peernotesCache")==null){
	    	SchedulerDB sdb=SchedulerDB.getSchedulerDB();
	    	
			try{	
				sdb.connectDB();
				for(Iterator i=sdb.getPeersData().iterator();i.hasNext();){
					Map record=(Map)i.next();
					String friendlyname=(String)record.get("friendlyname");
					String peername=(String)record.get("peername");
					friendlyname=(friendlyname!=null && !friendlyname.equals(""))? friendlyname:peername;
					data.put(peername, friendlyname);
				}
				cache.put("peernotesCache",data,att);
			}catch(Exception e){
				throw e;
			}finally{
				sdb.closeDB();
			}
			
		}else{
			data=(Map)cache.get("peernotesCache");
		}
		return data;
    }
	
	
	public Map getPeerNotes(String peer) throws Exception {
		Map rtn=null;
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		try{	
			sdb.connectDB();
			for(Iterator i=sdb.getPeersData().iterator();i.hasNext();){
				Map record=(Map)i.next();
				if(record.get("peername").toString().equals(peer)){// && record.get("notes")!=null){
					rtn= record;//record.get("notes").toString();
				}
			}
		   return rtn;
		}catch(Exception e){
			throw e;
		}finally{
			sdb.closeDB();
		}
		
	}
	
	
	public Map savePeerNotes(String peer, String notes,String friendlyname,String cmail) throws Exception{
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		try{
			HashMap rtn=new HashMap();
			sdb.connectDB();
			sdb.updatePeersNotes(peer, notes,friendlyname,cmail);
			rtn.put("peernotes", sdb.getPeersData());
			return rtn;
		}catch(Exception e){
			throw e;
		}finally{
			sdb.closeDB();
		}
	}
	
	public Map savePeerToggle(String peer, int active) throws Exception{
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		try{
			HashMap rtn=new HashMap();
			sdb.connectDB();
			sdb.updatePeersToggle(peer, active, getAuthenticatedUser());
			rtn.put("peernotes", sdb.getPeersData());
			return rtn;
		}catch(Exception e){
			throw e;
		}finally{
			sdb.closeDB();
		}
	}
	
	
	
	
	
	public Vector  execScriptLogs() throws Exception {
		
		
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
	
		try{
			sdb.connectDB();		
			
    		Vector logs=sdb.rScriptLast5Logs(); 
    		
    		SimpleDateFormat dateFormat =new SimpleDateFormat("mm:ss");    		
    		for(Iterator i=logs.iterator();i.hasNext();){
    			Map record=(Map)i.next();
    			try{
		    		Timestamp s=(Timestamp)record.get("start_time");
					Timestamp e=(Timestamp)record.get("end_time");
					Date d1=new Date();
					d1.setTime(s.getTime());
					record.put("started", getFriendlyTime(d1,null));				
					long diff=e.getTime()-s.getTime();
					record.put("duration",dateFormat.format(new Date(diff)));
    			}catch(Exception e){
    				log.error("error:"+e.getMessage());
    			}
    		}			
			return logs; 
			
		}catch(Exception e){
			ClientError.reportError(e, null);
			throw e;
		}finally{
			sdb.closeDB();
		}
	}
	
	
	
	public String killQueuedTask(String id_time) throws Exception {
		
		 
		try{
			StringTokenizer st=new StringTokenizer(id_time,"_");
			int scheduler_id=0;
			long trigger_time=0;
			if(st.countTokens()==2){
				scheduler_id=Integer.parseInt(st.nextToken());
				trigger_time=Long.parseLong(st.nextToken());
			}
			boolean killedstatus=false;
			if(scheduler_id>0 && trigger_time>0){
				killedstatus=LoadBalancingQueue.getDefault().killQueuedTask(scheduler_id,trigger_time);
			}
			if(killedstatus){
				return id_time;
			}else{
				return null;
			}
			
		}catch(Exception e){
			ClientError.reportError(e, null);
			throw e;
		} 
		
		
	}


	
	//public String removeFaultyExecutingTask(String id_time) throws Exception {
	/**
	 * @deprecated
	 * @param scheduler_id
	 * @param trigger_time
	 * @return
	 * @throws Exception
	 */
	public String removeFaultyExecutingTask(int scheduler_id, long trigger_time) throws Exception {
		
		 
		try{
			/*
			StringTokenizer st=new StringTokenizer(id_time,"_");
			int scheduler_id=0;
			long trigger_time=0;
			if(st.countTokens()==2){
				scheduler_id=Integer.parseInt(st.nextToken());
				trigger_time=Long.parseLong(st.nextToken());
			}
			*/
			
			boolean killedstatus=false;
			if(scheduler_id>0 && trigger_time>0){
				killedstatus=LoadBalancingQueue.getDefault().removeFaultyProcessingTask(scheduler_id,trigger_time);
				
				//removes the scheduler task from peer data. 
				Map peertimes=IncomingMessage.getExecutingPeersTime();
				
				for(Iterator i=peertimes.values().iterator();i.hasNext();){
					Map coll=(Map)i.next();
					if(coll.keySet().contains(scheduler_id)){
						coll.remove(scheduler_id);
					}				
				}
				
			}
			if(killedstatus){
				return "Task has been removed";
			}else{
				return "Removing failed";
			}
			
		}catch(Exception e){
			ClientError.reportError(e, null);
			throw e;
		} 
		
		
	}
       /**
        * To be removed 	
        * @param schedulerid
        * @return
        * @throws Exception
        */
	   public Map temp_ListEx() throws Exception {
		   try{
		
			   TreeMap t=new TreeMap();
			   Collection<LoadBalancingQueueItem> executingTasks=LoadBalancingQueue.getDefault().getExecutingTasks();
			   Collection<LoadBalancingQueueItem> queuedTasks=LoadBalancingQueue.getDefault().getQueuedTasks();
			   t.put("executing",executingTasks);
			   t.put("queued",queuedTasks);
			   return t; 
		   }catch(Exception e){
			   e.printStackTrace();
			   throw e;
		   }
	   }
	   
	    
	   
	   public Vector temp_LoadBalanceQueue() throws Exception {
		   try{
			   Vector v=new Vector();
			   for(Iterator i=LoadBalancingQueue.getDefault().getQueuedTasks().iterator();i.hasNext();){
				   v.add(i.next().toString());
			   }
			   return v;			   
		   }catch(Exception e){
			   e.printStackTrace();
			   throw e;
		   }
	   }
	   
	   public Vector temp_LoadBalanceQueueProcessing() throws Exception {
		   try{
			   Vector v=new Vector();
			   for(Iterator i=LoadBalancingQueue.getDefault().getExecutingTasks().iterator();i.hasNext();){
				   v.add(i.next().toString());
			   }
			   return v;			   
		   }catch(Exception e){
			   e.printStackTrace();
			   throw e;
		   }
	   }
	   
	   public Map temp_QueuedTaskTimes() throws Exception {
		   try{
			   return IncomingMessage.getExecutingPeersTime() ;
		   }catch(Exception e){
			   e.printStackTrace();
			   throw e;
		   }
	   }
	   
	   
	   public boolean addRemovePeerTaskuid(String taskuid, String peer, boolean checked ) throws Exception {
		   

			IExecAccessMgmt iExecAccessMgmt = new IExecAccessMgmt();
			String access = iExecAccessMgmt.checkUserPermissionNew(InfrastructureDB.APPLICATION_SCHEDULER_PEER_ASSOCIATION);

			// check if user has write access
			if ( (access.indexOf('w') >= 0) || (access.indexOf('W') >= 0) ) {

				SchedulerDB sdb=SchedulerDB.getSchedulerDB();
			
				try{
					sdb.connectDB();
					if(checked){
						sdb.addPeerTaskuid(taskuid, peer,getAuthenticatedUser());
					}else{
						sdb.deletePeerTaskuid(taskuid, peer, getAuthenticatedUser());
					}
					
					return true;
				}catch(Exception e){
					ClientError.reportError(e, null);
					throw e;
				}finally{
					sdb.closeDB();
				}
			}
			else {
				return false;
			}
	   }
	   
	   
	   public void setGroupOrder(Vector taskuids) throws Exception {
		    SchedulerDB sdb=SchedulerDB.getSchedulerDB();
			try{
				sdb.connectDB();	
				sdb.setGroupOrder(taskuids);
			}catch(Exception e){
				ClientError.reportError(e, null);
				throw e;
			}finally{
				sdb.closeDB();
			}
	   }
	   
		   
	   public Vector getExecutionLogs(int scheduler_id, long trigger_time) throws Exception {	
		   SchedulerDB sdb=SchedulerDB.getSchedulerDB();
			try{
				sdb.connectDB();	
				return sdb.getSchedulerExeLogs(scheduler_id,trigger_time);
			}catch(Exception e){
				ClientError.reportError(e, null);
				throw e;
			}finally{
				sdb.closeDB();
			}
		   
	   }
	   
	   public Map getExecutionLogs2(int scheduler_id, long trigger_time) throws Exception {	
		   
		   SchedulerDB sdb=SchedulerDB.getSchedulerDB();
			try{
				HashMap map=new HashMap();
				sdb.connectDB();	
				Vector data=sdb.getSchedulerExeLogs(scheduler_id,trigger_time);
				map.put("logs", data);
				
				String con_out=Config.getString("r_script_console_logs");
				String aFile=con_out+scheduler_id+"_"+trigger_time;
				if(con_out!=null && new File(con_out).isDirectory() && new File(aFile).isFile()){	
 					 	StringBuilder contents = new StringBuilder();
 					    try {
 					      BufferedReader input =  new BufferedReader(new FileReader(aFile));
					      try {
					        String line = null; //not declared within while loop
					        
					        while (( line = input.readLine()) != null){
					          contents.append(line);
					          contents.append(System.getProperty("line.separator"));
					        }
					      }finally {
					        input.close();
					      }
					    } catch (IOException ex){
					         log.error("Error in ready file. file:"+aFile);
					    }
						map.put("console",  contents.toString());;
				}
				return map;
				
			}catch(Exception e){
				ClientError.reportError(e, null);
				throw e;
			}finally{
				sdb.closeDB();
			}
		   
		   
	   }
	   
	   public String getInjectCode4Log(String sid_tid) throws Exception {	
		   
		   SchedulerDB sdb=SchedulerDB.getSchedulerDB();
			try{
				
				sdb.connectDB();
				//String code=sdb.getInjectCode4QLog(sid_tid);
				
				int sc_id=0;
				long trig_id=0;
				StringTokenizer st=new StringTokenizer(sid_tid,"_");
				if(st.countTokens()==2){
					sc_id=Integer.parseInt(st.nextToken());
					trig_id=Long.parseLong(st.nextToken());
				}
				
				//Map data=sdb.getScheduler(sc_id);
				//String param=(String)data.get("rscript_param");				
				//String script=(String)data.get("rscript");				
				
				Map data=sdb.getQueueLog(sc_id, trig_id);
				
				return (String)data.get("executed_code"); //RScriptScheduledTask.codeInjectConcatenate(param,script,code);
				
				//return code;
			}catch(Exception e){
				ClientError.reportError(e, null);
				throw e;
			}finally{
				sdb.closeDB();
			}
		   
		   
	   }
	   
	   
	   public Vector getNext10TriggerTimings(String sec,String min, String hr, String dw,String dm,String mn,String timezone) throws Exception {
		   
		   SchedulerEngineUtils seu=new SchedulerEngineUtils();
		   return seu.getTriggerTimes(sec, min, hr, dw, dm, mn, 20,timezone);
		   
		   
	   }
	   
	   public void restartServer() throws Exception {
		   try{
			   //SchedulerAPIServlet.restartPeer(peername);
			   RestartTomcat.restartMainServer();
			   
		   }catch(Exception e){
			   ClientError.reportError(e, null);
				throw e;
		   }
	   }

	   public void restartPeer(String peername) throws Exception {
		   try{
			   //SchedulerAPIServlet.restartPeer(peername);
			  // RestartTomcat.restartMainServer();
			   
		   }catch(Exception e){
			   ClientError.reportError(e, null);
				throw e;
		   }
	   }
	
	   
	   
	   public Map getItemPrivilegeNotifications(ArrayList themes,ArrayList ftags) throws Exception {
		   SchedulerDB sdb=SchedulerDB.getSchedulerDB();
			try{				
				sdb.connectDB();				
				return  getItemPrivilegeNotifications(themes,ftags,sdb);
				//return code;
			}catch(Exception e){
				ClientError.reportError(e, null);
				throw e;
			}finally{
				sdb.closeDB();
			}
		   
		   
	   }
		
	   
	   
	   /**
	    * @deprecated
	    * @param follow
	    * @param scheduler_id
	    * @param user
	    * @return
	    * @throws Exception
	    */
	   public Map followFunction(boolean follow, int scheduler_id, String user) throws Exception {
		   
			 
			SchedulerDB sdb=SchedulerDB.getSchedulerDB();
			try{
					HashMap h=new HashMap();				
					 
					sdb.connectDB();
					/*
					if(follow){
						sdb.followFunction(scheduler_id, user);
					}else{
						sdb.unFollowFunction(scheduler_id, user);
					}
					 
					
					Vector followers=sdb.getFollowers(scheduler_id);								
					h.put("followers", followers);
					*/				
					h.put("authorizedUser", new SchedulerMgmt(getRequest()).getAuthorizedUser(sdb));
					
					return h;
			}catch(Exception e){
					ClientError.reportError(e, null);
					throw e;
			}finally{
					 
					sdb.closeDB();
			}
		 }
		

	   
	   public Map getTaskTags(int scheduler_id) throws Exception {
		   
		   SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		   
			try{
				HashMap h=new HashMap();				
				sdb.connectDB();
				Vector tagids=sdb.getTagIds4Item(scheduler_id);
				Vector tags=sdb.getTags();
				h.put("tagids", tagids);
				h.put("tags", tags);
				return h;
			}catch(Exception e){
				ClientError.reportError(e, null);
				throw e;
			}finally{
				sdb.closeDB();
			}
	   }
	   
	   
	   public Map saveTags4Task(int scheduler_id, Vector tag_ids) throws Exception {
		   
		    SchedulerDB sdb=SchedulerDB.getSchedulerDB();		    
			try{
				
				HashMap rtn=new HashMap();				
				sdb.connectDB();
			 
				sdb.updateTagIds4Item(scheduler_id, tag_ids,SchedulerDB.REMOVE_BEFORE_UPDATE) ;
	    		
	    		Map rtndata=sdb.getScheduler(scheduler_id);
	    		Vector v=new Vector();
	    		v.add(rtndata);	    		
	    		sdb.closeDB();
	    		//return listScheduledItems();	    		
	    		rtn.put("scheduleditems", v);
				return rtn;
			}catch(Exception e){
				ClientError.reportError(e, null);
				throw e;
			}finally{
				sdb.closeDB();
			}
		   
	   }
	   
	   
		private Vector getSVNUsers(int scheduler_id) throws Exception {
			SVNSync sync=new SVNSync();
			Vector rtn=new Vector(); 
			Vector d=sync.log(scheduler_id);
	        if(d!=null && d.size()>0){
	        	
			    for(Iterator<SVNLogEntry> i=d.iterator();i.hasNext();){
			    	SVNLogEntry entry=i.next();		    	
			    	if(!rtn.contains(entry.getAuthor())){
			    		rtn.add(entry.getAuthor());
			    	}
			    }
	        }
	        return rtn;
		}

		
		public ArrayList getQueryDrDown(String type) throws Exception {
			
			SchedulerDB schedulerdb=SchedulerDB.getSchedulerDB();
			try {		
				   schedulerdb.connectDB();
				   ArrayList rtn=new ArrayList();
				   if(type!=null && type.equals("tag")){
					   List  tags=schedulerdb.getTags();				
					       
					   for(Iterator i=tags.iterator();i.hasNext();){
							 Map data=(Map)i.next();
							 String tag=(String)data.get("tagname");						 
							 tag=(tag.indexOf("-")>=0)?tag.substring(tag.indexOf("-")+1):tag;
							 rtn.add(data.get("id")+"|"+tag);
							 //mkup+="<option value='"+data.get("id")+"'>"+tag+"</option>";
						}
				   }
				   if(type!=null && type.equals("host")){
					   List peers=schedulerdb.getPeersData();
					   for(Iterator i=peers.iterator();i.hasNext();){
						   Map data=(Map)i.next();
						   String pname=(String)data.get("peername");
						   String fname=(String)data.get("friendlyname");
						   
						   fname=(fname==null || (fname!=null && fname.trim().equals("")))? pname:fname+" ("+pname+")";
						   rtn.add(pname+"|"+fname);					   
					   }
				   }
				   return rtn;
			}catch(Exception e){
				ClientError.reportError(e, null);
				throw e;
			}finally{
				schedulerdb.closeDB();
			}
		}

		
		
		public String getTagMarkup() throws Exception {
			
			SchedulerDB schedulerdb=SchedulerDB.getSchedulerDB();
			try {		
				   schedulerdb.connectDB();
					Vector  tags=schedulerdb.getTags();				
					schedulerdb.closeDB();
					String mkup="";
					for(Iterator i=tags.iterator();i.hasNext();){
						 Map data=(Map)i.next();
						 String tag=(String)data.get("tagname");						 
						 tag=(tag.indexOf("-")>=0)?tag.substring(tag.indexOf("-")+1):tag;
						 mkup+="<option value='"+data.get("id")+"'>"+tag+"</option>";
					}
					return mkup;
			}catch(Exception e){
				ClientError.reportError(e, null);
				throw e;
			}finally{
				schedulerdb.closeDB();
			}
		}
		
	   
	 /**
	 * @deprecated
	 * @param start
	 * @param end_task_id
	 * @return
	 * @throws Exception
	 */
	 /*
	   public boolean updateSVNUsers(int start,int end_task_id) throws Exception {
		    SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		    
			try{
				
				HashMap rtn=new HashMap();				
				sdb.connectDB();			 
				//sdb.updateTagIds4Task(scheduler_id, tag_ids,SchedulerDB.REMOVE_BEFORE_UPDATE) ;
				
	    		for(int i=start;i<(end_task_id+1);i++){
	    			Vector v=getSVNUsers(i);
	    			Vector tag_ids=new Vector();
	    			for(Iterator it=v.iterator();it.hasNext();){
	    				String user=(String)it.next();
	    				user="usr-"+user.trim().toLowerCase();
	    				String id=sdb.addIfTagNotExist(user)+"";
	    				tag_ids.add(id);
	    			}
	    			sdb.updateTagIds4Item(i, tag_ids,SchedulerDB.REMOVE_BEFORE_UPDATE) ;
	    		}
	    		
				return true;
			}catch(Exception e){
				ClientErrorMgmt.reportError(e, null);
				throw e;
			}finally{
				sdb.closeDB();
			}		   
	   }
	  */ 
	   
	   /**
	    * @deprecated
	    * @return
	    * @throws Exception
	    */
	   public String getXHRDataScheduler() throws Exception {		
		   
		   SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		    
				
		   try{
			   sdb.connectDB();
			   JSONArray rtn=new JSONArray();
			   if( getRequest().getParameter("term")!=null){				   
				   if(getRequest().getParameter("term")!=null){
					   String kword=getRequest().getParameter("term");
					   List data=sdb.getAutoCompleteXHR(kword);						
					   for(Iterator<Map> it=data.iterator();it.hasNext(); ){
						   Map record=it.next();
						   //System.out.println("SchedulerMgmt.getXHRDataScheduler():data:"+record);
						   rtn.put(record);
					   }
				   }
			   }
			   return rtn.toString();
			   
		   }catch(Exception e){
				ClientError.reportError(e, null);
				throw e;
			}finally{
				sdb.closeDB();
			}
		   
	   }
	   
	   /**
	    * @deprecated
	    */
	   public String getXHRDataR() throws Exception {		
		   
		   RFunctionDB rfdb=RFunctionDB .getRFunctionDB();		   
				
		   try{
			   rfdb.connectDB();   
			   JSONArray rtn=new JSONArray();
			   if( getRequest().getParameter("term")!=null){				   
				   if(getRequest().getParameter("term")!=null){
					   String kword=getRequest().getParameter("term");
					   List data=rfdb.autoCompleteFunctions(kword);						
					   for(Iterator<Map> it=data.iterator();it.hasNext(); ){
						   Map record=it.next();			 
						   rtn.put(record);
					   }
				   }
			   }
			   return rtn.toString();
			   
		   }catch(Exception e){
				ClientError.reportError(e, null);
				throw e;
			}finally{
				rfdb.closeDB();
			}
		   
	   }
 
	   
	   
	   /**
	    * @deprecated
	    * @return
	    * @throws Exception
	    */
	   public String getNotificationData() throws Exception {		
		   
		   SchedulerDB sdb=SchedulerDB.getSchedulerDB();	   
		   InfrastructureDB infrastructureDB = InfrastructureDB.getInfrastructureDB();
				
		   try{
			   sdb.connectDB();
			   JSONObject json=new JSONObject();
			   JSONArray rtn=new JSONArray();			    
			   
			   infrastructureDB.connectDB();
			   List<String> themes = infrastructureDB.getThemeByUsernameAccess(getAuthenticatedUser());
			   
			   List data=sdb.getFailedLast2Days(themes);			
			   for(Iterator<Map> it=data.iterator();it.hasNext(); ){
				   Map record=it.next();			 
				   rtn.put(record);
			   }
				
			   json.put("failed_scripts", rtn);
			   return json.toString();
			   
		   }catch(Exception e){
				//ClientErrorMgmt.reportError(e, null);
				//throw e;
			   	//log.error("");
			   throw e;
			}finally{
				sdb.closeDB();
				infrastructureDB.closeDB();
			}
		   
	   }
	   
	   
	   private String getParent(String folder) throws Exception {
		   String rtn=null;
		   if(folder.indexOf("/")>=0){
			   rtn=folder.substring(0,folder.lastIndexOf("/"));
		   }
		   return rtn;
	   }
	   
	   public boolean repairFolders() throws Exception {
		   SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		    
			try{				
				sdb.connectDB();
				
				List folders=sdb.listOfFolders(null); //get all folders
				ArrayList foldernames=new ArrayList();
				ArrayList missing=new ArrayList();
				for(Iterator<Map> i=folders.iterator();i.hasNext();){
					Map data=i.next();
					foldernames.add(data.get("taskuid")+":"+data.get("folder_name"));
					//System.out.println("fssss:"+data.get("folder_name"));	
				}		
				
				for(Iterator<String> i=foldernames.iterator();i.hasNext();) {
					String f=i.next();
					if(f.indexOf("/")>=0){
						String parent= getParent(f); //f.substring(0,f.lastIndexOf("/"));
						//System.out.println("f:"+f+" parent:"+parent);
						if(!foldernames.contains(parent) && !missing.contains(parent)){
							missing.add(parent);
						}
					}
				}
				
				//checking all parents 
				ArrayList missing1=new ArrayList();
				for(Iterator<String> i=missing.iterator();i.hasNext();) {
					String m=i.next();
					
					String gp=m;
					while(getParent(gp)!=null){
						gp=getParent(gp);
						if(!foldernames.contains(gp) && !missing.contains(gp)){
							missing1.add(gp);
						}
					}
					//System.out.println("missing: taskuid:"+taskuid+" parent:"+parent);
					//sdb.addFolder(taskuid, parent);
					//log.debug("adding folder:"+parent+" on "+taskuid);
				}
				missing1.addAll(missing);
				
				//checking all parents
				log.debug("all missing folders:"+missing1);
				for(Iterator<String> i=missing1.iterator();i.hasNext();) {
					String m=i.next();
					String taskuid=m.substring(0,m.indexOf(":"));
					
					String parent=m.substring(m.indexOf(":")+1);
					try{
						sdb.addFolder(taskuid, parent);
						log.debug("adding folder:"+parent+" on "+taskuid);
					}catch(Exception e){
						log.error("error:"+e.getMessage()+" taskuid:"+taskuid+" folder:"+parent);
					}
				}
				sdb.fixSchBrokenFolders();
				
				return true;
			}catch(Exception e){
				ClientError.reportError(e, null);
				throw e;
			}finally{
				sdb.closeDB();
			}		 
		   
		   
	   }
	   
	   
	   
	   public void temp_killScriptQueue() throws Exception {
		   
		   /*
		   PostClientTestMessage pctm=new PostClientTestMessage();
		   pctm.setName("~~~~~~~~`this name");
		   pctm.setScript("~~~~~~~~`this is my script");
		   pctm.setTest("~~~~~~~~~this is test");
		   
		   PostMessage pm=new PostMessage(pctm,peername);
		   pm.send();
		   */
		   //LoadBalancingQueue lq=new LoadBalancingQueue();
		   //lq.killScriptQueue();
	 
		  
	   }
		   
	   
	 
	   
	   public Map temp_QueueDDDDDDDetail(String clientname) throws Exception {
		   InstantPeerStatus ips=new InstantPeerStatus();
		   return ips.getStatus(clientname);
		   
	   }
	   
	   

		private static JCS getLockCache() throws Exception {
			if(SchedulerMgmt.lockcache==null) SchedulerMgmt.lockcache=JCS.getInstance("lock-cache");
			return SchedulerMgmt.lockcache;
		}
	   
		private static String SCHEDULER_ID="scheduler_id";
		private static String LOCK_DURATION="duration";
		private static String USER="user";
		private static int    LOCK_AUTO_RELEASE=300;
		
	   
	   private void refreshCache(int scheduler_id, long seconds, String usr ) throws Exception {
			
	       //String ky=usr+"_"+function_id;
		   String ky=usr+scheduler_id;
	       
		   HashMap h=new HashMap();		   
		   h.put(SchedulerMgmt.SCHEDULER_ID, scheduler_id);
		   h.put(SchedulerMgmt.LOCK_DURATION, seconds);
		   h.put(SchedulerMgmt.USER,  usr);
		   
		   IElementAttributes att= getLockCache().getDefaultElementAttributes();
		   att.setMaxLifeSeconds(seconds);
		   if(getLockCache().get(ky)!=null)getLockCache().remove(ky);
		   getLockCache().put(ky,h,att);

		
	   }
	
	   private void removeLockFromCache(int scheduler_id, String usr ) throws Exception {
	       //String ky=usr+"_"+function_id;
		   String ky=usr+scheduler_id;
		   if(getLockCache().get(ky)!=null)getLockCache().remove(ky);
	   }
	   
	   
		public boolean unLockTask(int scheduler_id) throws Exception {
			SchedulerDB sdb=SchedulerDB.getSchedulerDB();
			 
			try{
				sdb.connectDB();				 
				String usr=new SchedulerMgmt(getRequest()).getAuthorizedUser(sdb);
				removeLockFromCache(scheduler_id,usr);
				 
				return true;
			}catch(Exception e){
			
				throw e;
			}finally{
				sdb.closeDB();
				 
			}
		}

		

		public boolean editorActiveDetected(int scheduler_ids[]) throws Exception{
			
			SchedulerDB sdb=SchedulerDB.getSchedulerDB();
			
			try{
				sdb.connectDB();
				
				String usr=new SchedulerMgmt(getRequest()).getAuthorizedUser(sdb);
 
				for(int i=0;i<scheduler_ids.length;i++){
						 
					String ky=usr+scheduler_ids[i];					 
					if(ky.startsWith(usr)){
						Map d=(Map)getLockCache().get(ky);
						int f_id=(Integer)d.get(SchedulerMgmt.SCHEDULER_ID);
						if(f_id==scheduler_ids[i]){
							refreshCache(f_id,LOCK_AUTO_RELEASE,usr);
						}
					}
				}
							
			   return true;
			}catch(Exception e){
			
				throw e;
			}finally{
				sdb.closeDB();
				
			}
			
			
		}
		
		public boolean updateAlertType(String alert_type, int scheduler_id) throws Exception {
			
			SchedulerDB sdb=SchedulerDB.getSchedulerDB();
			
			try{
				
				sdb.connectDB();
				
				validateEditPrivilege(scheduler_id,sdb);
				
				Map data1=sdb.getScheduler(scheduler_id);				
				String old=(String)data1.get("alert_type");
				if(!alert_type.equals(old)){
					//String message="Task alert type changed from  "+((old==null ||(old!=null && old.equals("")))?" none ":old )+" to "+((alert_type==null ||(alert_type!=null && alert_type.equals("")))?" none ":alert_type);
					String message="Task alert type changed from  "+((old==null ||(old!=null && old.equals("")))?" none ":old )+" to "+((alert_type==null ||(alert_type!=null && alert_type.equals("")))?" none ":alert_type);
					String user=getAuthenticatedUser();							
		    		//sdb.closeDB();
		    		//data.put("id", scheduler_id);
					sdb.addEditLogs(scheduler_id, user, message);
					
					String name=(String)data1.get("name");
					notifyLastModification(sdb,name , scheduler_id, message, 0);
					
				}
				
				return sdb.updateAlertType(alert_type, scheduler_id);
				
			}catch(Exception e){
			    e.printStackTrace();
				throw e;
			}finally{
				sdb.closeDB();
				
			}
		}
		
		
		public Map temp_getLockedBys() throws Exception  {
			Map caches=getLockCache().getMatching("^[A-Za-z0-9]+$");
			return caches;
		}
		
		private String getLockedBy(int scheduler_id,SchedulerDB sdb) throws Exception {
		 
			try{
				String rtn=null; 
				Map caches=getLockCache().getMatching("^[A-Za-z0-9]+$");
				if(caches!=null){
					for(Iterator i=caches.keySet().iterator();i.hasNext();){
						String ky=(String)i.next();
						Map d=(Map)caches.get(ky);	
						try{
							if(d!=null){
								int f_id=(Integer)d.get(SchedulerMgmt.SCHEDULER_ID);
								long dur=(Long)d.get(SchedulerMgmt.LOCK_DURATION);
								String usr1=(String)d.get(SchedulerMgmt.USER);							 
								if(f_id==scheduler_id) rtn=usr1;
							}
						}catch(Exception e){
							//log.error("getLockedBy() Error:"+e.getMessage());
						}
					}
				}
				return rtn;
			}catch(Exception e){
			
				throw e;
			}finally{
				//sdb.closeDB();
				//rfdb.closeDB();
			}
			
		}
		

	 
		//private void notifyLastModification(SchedulerDB sdb,String task_name,int scheduler_id, String comments,long revision) throws Exception {
			//currently do nothing...
		//}
		
		
		private void notifyLastModification(SchedulerDB sdb,String task_name,int scheduler_id, String comments,long revision) throws Exception {
			
			//SVNSync4RFunction sync=new SVNSync4RFunction();
			SVNSync sync=new SVNSync();
			String diff=null;
			if(revision>=0){
				diff=sync.getLastChanged(scheduler_id);
			}else {
				diff="@@@No modification on script@@ there may be change on other than script for example trigger times, name and etc..  ";
				
			}
 
				
			String currentuser=new SchedulerMgmt(getRequest()).getAuthorizedUser(sdb);
			
			
			
 	
			ArrayList<String> themes=sdb.getFollowTags4Item(scheduler_id);
 			String exc_user=currentuser+"@4ecap.com";
		    HashMap hdata=new HashMap();
		    hdata.put("task_name", task_name);
		    hdata.put("scheduler_id", scheduler_id);
		    hdata.put("current_user", currentuser);
		    hdata.put("diff", diff);
		    hdata.put("comments", comments);
		    
		    String content="";
			InputStream in=SchedulerMgmt.class.getResourceAsStream("scheduler_modified_alert.txt");			
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;			
			while ((strLine = br.readLine()) != null)   {		  
			   content+=(content.equals("")) ?strLine:"\n"+strLine;
			}
			br.close();
			in.close();
			//System.out.println("content:"+content);
			
			TemplateParser pt=new TemplateParser(content,hdata);
			String message=pt.parseTemplate();
			String subject=pt.getSubject();
			
			try{
				
				// convert String array list to ThemeVO array list required by Alarm.sendAlarm() :
				ArrayList<ThemeVO> themeList = new ArrayList<ThemeVO>();
				for (int i=0; i<themes.size(); i++) {
					themeList.add(new ThemeVO(themes.get(i)));
				}
				
				Alarm.sendAlarm( themeList, AlarmType.EMAIL, subject, message, false, true, false, null,exc_user);
			}catch(Exception e){
				log.error("Couldn't send scheduler update notification to themes "+themes+" the followings are error:");
				e.printStackTrace();
			}
	}
	 
	
	public String commandStopStartPeer(String peer,String startstop) throws Exception {
		try{
			String command="cmd.exe /c sc "+startstop+" 4EPeer";
			String resp=SendCommand2Helper.sendCommand(peer, command);
			return resp;
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}
	
	public Map command2Peer(String peer,String command ) throws Exception {
		try{
			//String command="cmd.exe /c sc "+startstop+" 4EPeer";
			HashMap h=new HashMap();
			String resp=SendCommand2Helper.sendCommand(peer, command);
			h.put("peer", peer);
			h.put("response", resp);
			return h;
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}
	
	public Map command2PeerWithWait(String peer,String command, int waitSecs) throws Exception {
		try{
			//String command="cmd.exe /c sc "+startstop+" 4EPeer";
			HashMap h=new HashMap();
			String resp=SendCommand2Helper.sendCommand(peer, command,waitSecs);
			h.put("peer", peer);
			h.put("response", resp);
			return h;
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}
	

	public List getRserveSessionDetails(String peername) throws Exception {
		
		// String peername="4ecapvmsg2";    	 
		 ArrayList list=new ArrayList(); 
    	 RServeSessionStat rsq=new RServeSessionStat();   	 
    	 new PostMessage(rsq, peername).send();    	 
    	 Map<String,String> stat=rsq.getPeerCachedStat(peername);
    	 if(stat!=null){
	    	 for(String value:stat.values()){
	    		 JSONObject jobj=new JSONObject(value);
	    		 HashMap h=new HashMap(); 
	    		 for(Iterator it=jobj.keys();it.hasNext();){
	    			 String key=(String)it.next();
	    			 h.put(key, jobj.get(key));
	    		 }
	    		 list.add(h);
	    	 }
    	 }
    	 log.debug("data:"+list);
    	 return list;
	}
	
	public boolean killRserveProcess(String peername, int process_id) throws Exception {		
    	 
	    	 RServeSessionQuery rsq=new RServeSessionQuery();
	    	 rsq.setKill_process(process_id);
	    	 new PostMessage(rsq, peername).send();
	    	 return true;	     
		 
	}

	public boolean getPeerProperties(String peername, int process_id) throws Exception {
		
   	 
		RServeSessionQuery rsq=new RServeSessionQuery();
   	 	rsq.setKill_process(process_id);
   	 	new PostMessage(rsq, peername).send();
   	 	return true;
    
	 
	}

	public Map getPeerSpecificStat(String peername) throws Exception {		
		PeerPropertiesGet ppg=new PeerPropertiesGet();	
   	 	new PostMessage(ppg, peername).send();
   	 	
   	 	Thread.sleep(1500); // sleep 2 seconds peer to respond.
   	 	boolean loop=true; 
   	 	Map data=null;
   	 	for(int i=0;(i<100 && loop);i++){
   	 		data=PeerPropertiesGet.getPeerCachedProp(peername);
   	 		if(data!=null) loop=false;
   	 		Thread.sleep(50);
   	 	}
   	 	return data;    
	 
	}
	
	public boolean setPeerSpecificStat(String peername,int con_sess,int max_per_sess) throws Exception {
		
		// this log error is for production debugging purpose. remove when it's done. itask 8257.
		log.error(">>>>> debug itask 8257 >>>>> SchedulerMgmt.setPeerSpecificStat() - start");
	   	 
		PeerPropertiesSet pps=new PeerPropertiesSet();
		
		pps.setPeerConcurrentThread(con_sess+"");
		pps.setPeerSessionExecutionMax(max_per_sess+"");
   	 	new PostMessage(pps, peername).send();
   	 	 
   	 	// this log error is for production debugging purpose. remove when it's done. itask 8257.
   	 	log.error(">>>>> debug itask 8257 >>>>> SchedulerMgmt.setPeerSpecificStat() - end");
   	 	   	 	
   	 	return true;
	}
	
	public String getWikiHelp(String function_name) throws Exception {
		String function_name1="SchedulerScript:"+function_name.replaceAll(" ", "_");
		log.debug("wiki pagename:"+function_name1);				
		WikiRFunctionManual wiki=new WikiRFunctionManual();		
		//String rtn=wiki.getWikiHTML(function_name1);
		
		String username=Config.getString("wiki.username");
		String password=Config.getString("wiki.password");
		String wikiurl=Config.getString("wiki.wikiurl");
		
		String rtn=wiki.getWikiHTML(username, password, wikiurl, function_name1);		
		return rtn;
		
	}
	
	
	
	
	
	public void updateSyncSVN(int start_id, int end_id) throws Exception {
		
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		String user=getAuthenticatedUser();
		if(user==null ){
			throw new Exception ("Please login first");
		}
		try{
			String svnuser=Config.getString("svn_user");
			String svnpwd=Config.getString("svn_pwd");
			
			sdb.connectDB();
		 
			for(int i=start_id;i<=end_id;i++) {
				
				int thisid=i;
				Map u=sdb.getSVNUser4WikiUser(user);				
				if(u!=null && u.get("svn_username")!=null && u.get("svn_password")!=null){
					svnuser=(String)u.get("svn_username");
					svnpwd=(String)u.get("svn_password");
				}
				
				SVNSync sync=new SVNSync(svnuser,svnpwd);
				if(sync.log(thisid)==null){
					Map data=sdb.getScheduler(thisid);
					if(data!=null){
						String script=(String)data.get(FlexiField.TYPE_RSCRIPTEDITOR);
						if(script!=null && !script.trim().equals("")){
							long rev=sync.syncScript(thisid, script,"Sychrnoized by system");			
							System.out.println("SchedulerMgmt.updateSyncSVN(): scheduler_id:"+thisid+" ");
							//todo
							
						}
					}
				}				
			}
		}catch(Exception e){
		    e.printStackTrace();
			throw e;
		}finally{
			sdb.closeDB();
			
		}
	}
	
	
	 public List getPeerAssHist4Peer(String peername) throws Exception {
		   
		   
			SchedulerDB sdb=SchedulerDB.getSchedulerDB();
			
			try{
				sdb.connectDB();
				 
				return sdb.getLast50PeerActHistory(peername)	;
			}catch(Exception e){
				ClientError.reportError(e, null);
				throw e;
			}finally{
				sdb.closeDB();
			}
	   }
	 
	 public List getPeerAssHist4Task(String taskuid) throws Exception {		   
		   
			SchedulerDB sdb=SchedulerDB.getSchedulerDB();
			
			try{
				sdb.connectDB();
				 
				return sdb.getLast50TaskActHistory(taskuid)	;
			}catch(Exception e){
				ClientError.reportError(e, null);
				throw e;
			}finally{
				sdb.closeDB();
			}
	   }

	 
		protected String getPeerIPAddress() throws Exception {		
			String rtn=(String) getRequest().getSession().getAttribute(REMOTE_IP);
			return rtn;
		}
		

		/**
		 * Do not use this as deprecated and kept for code refrerence.
		 * @deprecated
		 * @param cTab
		 * @return
		 * @throws Exception
		 */
		public Map loadInit(int cTab) throws Exception {
			try {		
				
				HashMap data=new HashMap();
				List<ScheduledTask> list=new ScheduledTaskFactory().getTasks();
				Vector rtn=new Vector();
				Vector order=getGroupOrder();
				
				for(Iterator<ScheduledTask> i=list.iterator();i.hasNext();){
					ScheduledTask st=i.next();
					ValueObject vo=new ValueObject();
					vo.setKey(st.getUniqueid());
					vo.setValue(st.getName());
					rtn.add(vo);
				}
				
				//System.out.println("1:rtn:"+rtn);
			 
				
				String bbsy=Config.getString("bloomberg_synchronization");
				Vector running=null;
				if(bbsy!=null & bbsy.equalsIgnoreCase("no")){
					running=new Vector();
				}else{
					running=SchedulerEngine.getEnabledTaskTypes();	
				}
				
				if(cTab==-1){
					cTab=getActiveTab();
				}
				
				data.put("activetab", cTab);
				
				if(cTab==0){   //tab 1
					
					data.put("tasktypes", rtn);
					data.put("timezones", TimeZone.getAvailableIDs());			
					Map data1=listScheduledItems();			
					data1.put("taskrunning", running);
					
					
					data.put("listtasks", data1);
					
					BBSyncDB syncdb=BBSyncDB.getBBSyncDB();
					syncdb.connectDB();
					Vector  fields=syncdb.getFieldMapping();				
					syncdb.closeDB();
					
					data.put("field_mapping", fields);
					
					SchedulerDB schedulerdb=SchedulerDB.getSchedulerDB();
					schedulerdb.connectDB();
					Vector  tags=schedulerdb.getTags();				
					schedulerdb.closeDB();
					data.put("tags", tags);
				    data.put("showtags", getShowTags()); 
				}
				
				if(cTab==1){
					
					data.put("qlogs", getQueueLogs(null));	
					SchedulerDB sdb=SchedulerDB.getSchedulerDB();
					try{	
						sdb.connectDB();
						data.put("peerslist", sdb.getPeersData());
					}catch(Exception e){
						log.error("Error:"+e.getMessage());
					}finally{
						sdb.closeDB();
					}
					 
				}
				if(cTab==2){
					data.put("peersdata", getData4PeerMatrix());
				}
				//String localnames[]=
				data.putAll(getGroupIconsAndColors());
				
				return data;
			}catch(Exception e){
				e.printStackTrace();
				ClientError.reportError(e, null);
				throw e;
			}
		}
}





 

