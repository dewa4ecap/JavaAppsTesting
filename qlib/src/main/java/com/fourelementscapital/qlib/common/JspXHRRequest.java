/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.qlib.common;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import com.fourelementscapital.db.InfrastructureDB;
import com.fourelementscapital.db.RFunctionDB;
import com.fourelementscapital.db.SchedulerDB;
import com.fourelementscapital.error.ClientError;
import com.fourelementscapital.ldap.LDAP;
import com.fourelementscapital.client.Authenticated;

/**
 * This class is used for making several XHR requests, for example jquery autocomplete and autosuggest feature. 
 * @author Administrator
 *
 */
@SuppressWarnings("unchecked")
public class JspXHRRequest extends Authenticated {
	
	//private LDAP ldap = null;
	
	/**
	 * make sure you are not passing null request, otherwise it won't work properly
	 * @param request
	 * @throws Exception
	 */
	public JspXHRRequest(HttpServletRequest request) throws Exception { super(request); }
	   
	/**
	 * Autocomplete search for scheduler name search on UI
	 * @return rtn.toString()
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
	 * Generic XHR data returned
	 * @return rtn.toString()
	 * @throws Exception
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
	   } catch(Exception e){
			ClientError.reportError(e, null);
			throw e;
		} finally { rfdb.closeDB(); }
	}
	
	public String getXHRDataRSSO(String choosenGroup) throws Exception {
		
		//List<String> themes = null;
		Map<String,String> themes = new TreeMap<String,String>();
		String listThemes = null;
		InfrastructureDB inf = InfrastructureDB.getInfrastructureDB();
		String highestGroup = "";
		
		LDAP ldap = new LDAP();
		
		try {
			inf.connectDB();
			
			//themes = inf.getUserThemes((getStatusTOMUser() ? getAuthenticatedUser() : choosenGroup));
			themes = inf.getThemes4Users((getStatusTOMUser() ? getAuthenticatedUser() : choosenGroup));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			inf.closeDB();
		}
		
		if (themes != null) {
			try {
				String temp = "";
				//for(String thm : themes) { temp = temp + "'thm-" + thm +"',"; }
				for(Map.Entry<String, String> thm : themes.entrySet()) {
					temp = temp + "'thm-" + thm.getKey() +"',";
				}
				listThemes = temp.substring(0,temp.length()-1);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	   RFunctionDB rfdb=RFunctionDB .getRFunctionDB();		   
		
	   try{
		   rfdb.connectDB();   
		   JSONArray rtn=new JSONArray();
		   if( getRequest().getParameter("term")!=null){				   
			   String kword=getRequest().getParameter("term");
			   //List data=rfdb.autoCompleteSSO(kword,listThemes);
			   List data=rfdb.autoCompleteFunctions(kword,listThemes);
			   for(Iterator<Map> it=data.iterator();it.hasNext(); ){
				   Map record=it.next();			 
				   rtn.put(record);
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
	 * Notification related data in JSON format.
	 * @return json.toString()
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
		   throw e;
		}finally{
			sdb.closeDB();
			infrastructureDB.closeDB();
		}
	}

   /**
    * JSON formatted data.
    * @return rtn.toString()
    * @throws Exception
    */
	public String getJSON() throws Exception {
		
	   String callback=getRequest().getParameter("callback");
	   SchedulerDB sdb=SchedulerDB.getSchedulerDB();
	    
	   try{
		   sdb.connectDB();
		   JSONArray rtn=new JSONArray();
		   if( getRequest().getParameter("search_name")!=null || getRequest().getParameter("search_id")!=null){
			   String query="";
			   if(getRequest().getParameter("search_name")!=null){
				   String kword=getRequest().getParameter("search_name");
				   query = sdb.getTop20IdNameByNameQuery(kword);
			   }
			   if(getRequest().getParameter("search_id")!=null){
				   String kword=getRequest().getParameter("search_id");
				   query = sdb.getTop20IdNameByIdQuery(kword);					   
			   }
			   List data=sdb.getSingleColData(query);
			
			   for(Iterator it=data.iterator();it.hasNext(); ){
				   String rec=(String)it.next();
				   rtn.put(rec);
			   }
		   }
		   return callback+"("+rtn.toString()+");";
	   }catch(Exception e){
			ClientError.reportError(e, null);
			throw e;
		}finally{
			sdb.closeDB();
		}		   
   }
	   
	/**
	 * returns execution logs for the selected log item in the queue
	 * @param scheduler_id
	 * @param trigger_time
	 * @return rtn.toString()
	 * @throws Exception
	 */
    public String getExecutionLogs(int scheduler_id, long trigger_time) throws Exception {	
	   SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		try{
			sdb.connectDB();	
			JSONArray rtn=new JSONArray();
			List list=sdb.getSchedulerExeLogs(scheduler_id,trigger_time);
			rtn.put(list);
			return rtn.toString();
		}catch(Exception e){
			ClientError.reportError(e, null);
			throw e;
		}finally{
			sdb.closeDB();
		}
   }
	   
   /**
    * List last 2 days failed details.
    * @param scheduelr_id
    * @param stat
    * @return json.toString()
    * @throws Exception
    */
   public String listLast2DaysFailedDetails(long scheduelr_id, String stat) throws Exception  {
	   
	   SchedulerDB sdb=SchedulerDB.getSchedulerDB();
	   try{
		   sdb.connectDB();
		   String datequery= sdb.getHistoryQueueLogsDateQuery(scheduelr_id, stat);
		   Date now=new Date();
		   List list=sdb.listOfHistoryQueueLogs(now.getTime(),datequery);
		   JSONObject json=new JSONObject();
		   json.put("list", list);
		   return json.toString();
		   
	   }catch(Exception e){
		   ClientError.reportError(e, null);
			throw e;
	   }finally{
		   sdb.closeDB();
	   }	
   }

}