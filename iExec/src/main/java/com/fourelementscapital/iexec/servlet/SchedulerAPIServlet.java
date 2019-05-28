/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.iexec.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.jcs.JCS;
import org.apache.commons.jcs.access.CacheAccess;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fourelementscapital.config.Config;
import com.fourelementscapital.iexec.common.IExecMgmt;

/**
 * 
 * SchedulerAPI
 * 
 * @author Rams Kannan
 *
 */

@SuppressWarnings( "unchecked" )
public class SchedulerAPIServlet extends HttpServlet {	
	
	private static String EXECUTETASKS="run";
	private static String QUEUE="queue";
	private static String EXECUTE_R_QUEUEINFO="xRQInfo";	
	private static String SERVER_DATE_TIME="getCurrentDateTime";	
	private static String SCHEDULER_QUEUEGROUP_ADD="addGroupIntoQueue";	
	private static String SCHEDULER_QUEUEGROUP_REMOVE="removeGroupFromQueue";
	private static String REMOVE_EXECUTING_TASK="removeExecutingTask";	
	private static String EXECUTE_SSCRIPT_P2P="execSyncScript";
	private static String EXECUTE_ASCRIPT_P2P="execAsyncScript";	
	private static String EXECUTE_ASCRIPT_P2P_RESULT="getScriptResult";
	private static int  EXPIRY_FOR_UNRETRIEVED=30; //seconds
	private static int  EXPIRY_FOR_RETRIEVED=2;


	private static String STRATEGY_XML="strategyXML";

	
	private String msg=null;
	
	private Logger log = LogManager.getLogger(SchedulerAPIServlet.class.getName());

	public SchedulerAPIServlet() {
		if(Config.getString("queue_timeout")!=null) {
			try {
				EXPIRY_FOR_UNRETRIEVED = Integer.parseInt(Config.getString("queue_timeout"));
			} catch (Exception e) {
				log.error("Error to assign queue timeout. Default value will apply.");
			}
		}
	}

	private static CacheAccess<String, String> cache=null;
	private static CacheAccess getCache() throws Exception {
		 if(cache==null){
				cache=JCS.getInstance("direct_script");
		 }
		 return cache;
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		 
		String method = request.getParameter("method");
		this.msg="Error in current request, Method value required";
		boolean noerror=false;
		if(method!=null && !method.equals("")){


/**
			 * @deprecated
			 * recommended not to use.
			 */
			if(method.equalsIgnoreCase(SchedulerAPIServlet.STRATEGY_XML) && request.getParameter("strategy")!=null && request.getParameter("contract")!=null){
				 
					response.setContentType("text/xml");
				    PrintWriter out = response.getWriter();		
				    String contract=request.getParameter("contract");
				    if(contract==null){
				    	contract="";
				    }
				    try{
				    	IExecMgmt iemgmt=new IExecMgmt(request,false); 	
				    	
				    	out.write(iemgmt.getMyParsedXML(request.getParameter("strategy"),contract ));
				    }catch(Exception e){
				    	out.write("Error:"+e.getMessage());
				    	e.printStackTrace();
				    }
				    out.flush();				    
				 
			}
		}
		if(!noerror){
			try {
				 //no valid parameter take to the documention about this API.
			     getServletConfig().getServletContext().getRequestDispatcher("/schedulerAPI.jsp").forward(request,response);
			} catch (ServletException e) {
			    	log.error(e);
			}
		}
	}
}