/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.executer.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.jcs.JCS;
import org.apache.commons.jcs.access.CacheAccess;
import org.apache.commons.jcs.access.exception.CacheException;
import org.apache.commons.jcs.engine.behavior.IElementAttributes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fourelementscapital.config.Config;
import com.fourelementscapital.loadbalance.LoadBalancingQueue;
import com.fourelementscapital.rscript.RScript;
import com.fourelementscapital.rscript.RScriptAsyncListenerImpl;

/**
 * 
 * SchedulerAPI
 * 
 * @author Rams Kannan
 *
 */

@SuppressWarnings( "unchecked" )
public class SchedulerAPIServlet extends HttpServlet {	
	
	private static String EXECUTE_SSCRIPT_P2P="execSyncScript";
	private static String EXECUTE_ASCRIPT_P2P="execAsyncScript";	
	private static String EXECUTE_ASCRIPT_P2P_RESULT="getScriptResult";
	private static int  EXPIRY_FOR_UNRETRIEVED=30; //seconds
	private static int  EXPIRY_FOR_RETRIEVED=2;
	
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
				if (request.getParameter("webSubmit")!=null) {
					try {
						String script=request.getParameter("script");			
						RScript rs=new RScript();
						rs.setUniquename(rs.getUid());
						if(request.getRemoteHost()!=null){
							rs.setRequesthost(request.getRemoteHost());
						}
						rs.setTaskuid("direct_script_unix");
						rs.setScript(script);		   		
				   		String id=rs.getUid();
				   		
				   		IElementAttributes att= getCache().getDefaultElementAttributes();
				   		att.setMaxLife(EXPIRY_FOR_UNRETRIEVED);
			   			RScriptAsyncListenerImpl liten=new RScriptAsyncListenerImpl(getCache(),att);
			   			
			   			getCache().put(id, RScriptAsyncListenerImpl.ALIVE,att); //listener
			   			
			   			LoadBalancingQueue.getExecuteRScriptDefault().addExecuteR(rs,liten);
			   			
			   			String resptype="text/plain";
				   		response.setContentType(resptype);
				   		
				   		PrintWriter out = response.getWriter();
					    out.println(id);
					    out.flush();
			   			
			   			/*String ri=null;
		   				ri=(String)getCache().get(id);
		   				Boolean finish = false;
		   				
		   				while (!finish) {
		   					if(ri!=null && !ri.equalsIgnoreCase(RScriptAsyncListenerImpl.ALIVE)){
								 String resptype=(request.getParameter("responseType")!=null)?request.getParameter("responseType"):"text/xml";
							   	 response.setContentType(resptype);					 
								 PrintWriter out = response.getWriter();	
								 out.print(ri);
								 out.flush();
								 
								 //remove the result from cache and set the storance once it is retrieved.
								 try{
									 IElementAttributes att2= getCache().getDefaultElementAttributes();
									 att2.setMaxLife(EXPIRY_FOR_RETRIEVED);			   			
									 getCache().remove(id);
									 getCache().put(id,ri,att2);
								 }catch(Exception e){
									 log.error("Error while resetting token in cache");
								 }
								 finish = true;
							 }
		   				}*/
		   				
			   			
					} catch (Exception ex) {
						
					}
					
		   			
		   			
			   		
				} else {
					String method = request.getParameter("method");
					
					if(method!=null && !method.equals("") && (method.equals(SchedulerAPIServlet.EXECUTE_SSCRIPT_P2P) || method.equals(SchedulerAPIServlet.EXECUTE_ASCRIPT_P2P))){
						log.debug("debug doPost: method:"+method);
						try{
							String script=request.getParameter("script");			
							RScript rs=new RScript();
							
							rs.setUniquename(rs.getUid());
							if(request.getParameter("uniquename")!=null && !request.getParameter("uniquename").trim().equals("")){
								rs.setUniquename(request.getParameter("uniquename"));
							}

							//set remote host from getRemoteHost
							if(request.getRemoteHost()!=null){
								rs.setRequesthost(request.getRemoteHost());
							}
							
							rs.setTaskuid("direct_script");
							if(request.getParameter("engine")!=null && request.getParameter("engine").equalsIgnoreCase("rserve")){
								rs.setTaskuid("direct_script_unix");
							}
							
					   		if(request.getParameter("executeAt")!=null && !request.getParameter("executeAt").equals("")){
					   			rs.setExecuteAt(request.getParameter("executeAt"));
					   		}
					   				   		
					   		rs.setScript(script);		   		
					   		String id=rs.getUid();

					   		IElementAttributes att= getCache().getDefaultElementAttributes();
					   		att.setMaxLife(EXPIRY_FOR_UNRETRIEVED);
				   			RScriptAsyncListenerImpl liten=new RScriptAsyncListenerImpl(getCache(),att);
				   			
				   			/*Note Gama : data loaded to cache*/
				   			getCache().put(id, RScriptAsyncListenerImpl.ALIVE,att); //listener
				   			
				   			LoadBalancingQueue.getExecuteRScriptDefault().addExecuteR(rs,liten);	   			
					   		boolean finished=false;
					   		String rtn=null;
					   		if(method.equals(SchedulerAPIServlet.EXECUTE_SSCRIPT_P2P)){
						   		while(!finished){
						   			
						   			String ri=(String)getCache().get(id);
						   			if(ri!=null && !ri.equalsIgnoreCase(RScriptAsyncListenerImpl.ALIVE)){
						   				getCache().remove(id);
						   				rtn=ri;
						   				finished=true;
						   			}
						   			if(ri==null){
						   				finished=true;
						   			}
						   			try{
						   				Thread.sleep(10);
						   			}catch(Exception e){
						   				e.printStackTrace();
						   			}		   			
						   		}
						   		String resptype=(request.getParameter("responseType")!=null)?request.getParameter("responseType"):"text/xml";
						   		response.setContentType(resptype);
					   		}else{		   	
					   			response.setContentType("text/plain");
					   			rtn=id;
					   		}
						    PrintWriter out = response.getWriter();
						    out.println(rtn);
						    out.flush();
						    
						}catch(Exception e){
							e.printStackTrace();
						}
					}else{
						//report error saying no method parameter found;
					}
				}

	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		 
		String method = request.getParameter("method");
		this.msg="Error in current request, Method value required";
		boolean noerror=false;
		if(method!=null && !method.equals("")){
			
			/**
			 * Get the result of previously executed executeR script in asynchronous mode, using the returned token 
			 */
			if(method.equals(SchedulerAPIServlet.EXECUTE_ASCRIPT_P2P_RESULT) && request.getParameter("token")!=null && !request.getParameter("token").equals("") ){
				
				 String ri=null;
				 String token=null;
				 try{
	   			    
	   				ri=(String)getCache().get(request.getParameter("token").trim());
	   				token=request.getParameter("token").trim();
				 }catch(Exception e){
					log.error("Error:"+e.getMessage());
				 }
				 if(ri!=null && !ri.equalsIgnoreCase(RScriptAsyncListenerImpl.ALIVE)){
					 String resptype=(request.getParameter("responseType")!=null)?request.getParameter("responseType"):"text/xml";
				   	 response.setContentType(resptype);					 
					 PrintWriter out = response.getWriter();	
					 out.print(ri);
					 out.flush();
					 
					 //remove the result from cache and set the storance once it is retrieved.
					 try{
						 IElementAttributes att= getCache().getDefaultElementAttributes();
						 att.setMaxLife(EXPIRY_FOR_RETRIEVED);			   			
						 getCache().remove(token);
						 getCache().put(token,ri,att);
					 }catch(Exception e){
						 log.error("Error while resetting token in cache");
					 }
			   					
				 }else if(ri!=null && ri.equalsIgnoreCase(RScriptAsyncListenerImpl.ALIVE)) {
					 	String resptype=(request.getParameter("responseType")!=null)?request.getParameter("responseType"):"text/xml";
				   	 	response.setContentType(resptype);	
					    PrintWriter out = response.getWriter();		
					    out.println("<?xml version=\"1.0\"?>");
					    out.println("<result>not ready</result>");
					    out.flush();
					    
				 }else {
					    String resptype=(request.getParameter("responseType")!=null)?request.getParameter("responseType"):"text/xml";
				   	 	response.setContentType(resptype);	
					    PrintWriter out = response.getWriter();		
					    out.println("<?xml version=\"1.0\"?>");
					    out.println("<result>No script associated to this token, Result for the token expires in "+EXPIRY_FOR_UNRETRIEVED+" seconds. </result>");
					    out.flush();
				 }
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