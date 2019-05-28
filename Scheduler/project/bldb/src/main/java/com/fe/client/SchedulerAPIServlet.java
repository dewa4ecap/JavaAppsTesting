/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fe.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.PipeService;
import net.jxta.protocol.PipeAdvertisement;

import org.apache.jcs.JCS;
import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jfree.util.Log;

import com.fe.common.Constant;
import com.fe.scheduler.balance.LoadBalancingLinkedQueue;
import com.fe.util.ProxifyURL;
import com.fe.util.RestartTomcat;
import com.fourelementscapital.db.SchedulerDB;
import com.fourelementscapital.scheduler.SchedulerEngine;
import com.fourelementscapital.scheduler.balance.ExecutingQueueCleaner;
import com.fourelementscapital.scheduler.balance.LoadBalancingQueue;
import com.fourelementscapital.scheduler.balance.LoadBalancingQueueItem;
import com.fourelementscapital.scheduler.error.ClientError;
import com.fourelementscapital.scheduler.p2p.MessageBean;
import com.fourelementscapital.scheduler.p2p.P2PAdvertisement;
import com.fourelementscapital.scheduler.p2p.P2PService;
import com.fourelementscapital.scheduler.p2p.listener.IncomingMessage;
import com.fourelementscapital.scheduler.p2p.listener.OutgoingMessage;
import com.fourelementscapital.scheduler.p2p.listener.P2PTransportMessage;
import com.fourelementscapital.scheduler.rscript.RScript;
import com.fourelementscapital.scheduler.rscript.RScriptAsyncListenerImpl;

/**
 * 
 * SchedulerAPI  
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * <h1>run</h1>
 * Executes scheduled tasks
 * <pre>
 * <u>Parameters:</u>ids={id1,id2...}
 * <u>Returns:</u>  
 * {@code
 * <?xml version="1.0"?>
 * <result>done</result>
 * }
 * <u>Example:</u> http://server-ip-or-name/bldb/schedulerAPI?method=run&ids=21,22
 * <hr> 
 * </pre>
 * 
 * 
 * 
 * <h1>livepeers</h1>
 * This method lists out all live peers 
 * <pre>
 * <u>Parameters:</u>Nil
 * <u>Returns:</u>  
 * {@code
 * <?xml version="1.0"?>
 * <result>
 * 	<peer status="NOBUSY">4ECAPSVSG1</peer>
 * 	<peer status="NOBUSY">4ecappcsg11</peer>
 * 	<peer status="NOBUSY">4ecappcsg12</peer>
 * 	<peer status="BUSY">4ecappcsg14</peer>
 * 	<peer status="BUSY">4ecappcsg7</peer>
 * 	<peer status="BUSY">4ecapsvsg5</peer>
 * 	<peer status="BUSY">dev-server</peer>
 * </result>
 * }
 * <u>Example:</u> http://server-ip-or-name/bldb/schedulerAPI?method=livepeers
 * <hr> 
 * </pre>
 *   
 * 
 * <h1>queue</h1>
 * This method lists all tasks that currently executing by peers and ready to execute by peer in any moment. 
 * <pre>
 * <u>Parameters:</u>Nil
 * <u>Returns:</u>  
 * {@code
 * <?xml version="1.0"?>
 * <result>
 * 	<task status="EXCECUTING" id="1174" peer="dev-server"/>
 * 	<task status="EXCECUTING" id="1401" peer="4ecappcsg11"/>
 * </result>
 * }
 * <u>Example:</u> http://server-ip-or-name/bldb/schedulerAPI?method=queue
 * <hr> 
 * </pre> 
 *
 * 
 * 
 *     
 * 
 * <h1>syncContract</h1>
 * This method synchronize bloomberg data with Market Contracts those mapped with reference database. 
 * Accepts optional parameter.<br>
 * Parameter <b>contract</b> synchronizes only specified contract<br>
 * Nil parameter synchronizes all new contracts that haven't synchronized so far 
 * <pre>
 * <u>Parameters:</u>[contract={@code <contract_ticker>}] if no parameter specified, this synchronizes all new contracts those haven't synchronized yet.
 * <u>Returns:</u>  
 * {@code
 * <?xml version="1.0"?>
 * <result>true/false</result>
 * }
 * <u>Example:</u> 
 * http://server-ip-or-name/bldb/schedulerAPI?method=syncContract&contract=CL010
 * http://server-ip-or-name/bldb/schedulerAPI?method=syncContract 
 * <hr> 
 * </pre> 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * <h1>syncSecurity</h1>
 * This method synchronize bloomberg data with Securities those mapped with reference database. 
 * Accepts optional parameter.<br>
 * Parameter <b>security</b> synchronizes only specified contract<br>
 * Nil parameter synchronizes all new securities that haven't synchronized so far 
 * <pre>
 * <u>Parameters:</u>[security={@code <security_ticker>}] if no parameter specified, this synchronizes all new securities those haven't synchronized yet.
 * <u>Returns:</u>  
 * {@code
 * <?xml version="1.0"?>
 * <result>true/false</result>
 * }
 * <u>Example:</u> 
 * http://server-ip-or-name/bldb/schedulerAPI?method=syncSecurity&contract=AIGC
 * http://server-ip-or-name/bldb/schedulerAPI?method=syncSecurity 
 * <hr> 
 * </pre> 
 *  
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * @author Rams Kannan
 *
 */
public class SchedulerAPIServlet extends HttpServlet {	
	
	
	private static String EXECUTETASKS="run";
	private static String GETLIVE="livepeers";
	private static String QUEUE="queue";
	private static String SYNC_REF_CONTRACT="syncContract";	
	private static String EXECUTE_R_QUEUEINFO="xRQInfo";	
	private static String EXECUTE_R_QUEUE="executeR_queue";
	
	private static String PEER_PACKAGES="getPeerPackages";
	private static String PEER_INFO="getPeerInfo";
	private static String PEER_QUEUE="getPeerQueue";
	
	private static String SERVER_DATE_TIME="getCurrentDateTime";	
	private static String SYNC_REF_SECURITY="syncSecurity";
	
	private static String SCHEDULER_QUEUEGROUP_ADD="addGroupIntoQueue";	
	private static String SCHEDULER_QUEUEGROUP_REMOVE="removeGroupFromQueue";
	
	private static String RESTART_PEER="restartPeer";
	private static String RESTART_PEER_LATER="restartAfterDone";
	private static String RESTART_SERVER="restartServer";
	
	private static String PRIORITY_QUEUE="priorityQueue";		
	private static String REMOVE_EXECUTING_TASK="removeExecutingTask";	
	private static String STRATEGY_XML="strategyXML";
	
	private static String EXECUTE_SSCRIPT_P2P="execSyncScript";
	private static String EXECUTE_ASCRIPT_P2P="execAsyncScript";	
	private static String EXECUTE_ASCRIPT_P2P_RESULT="getScriptResult";
	
	private static int  EXPIRY_FOR_UNRETRIEVED=30; //seconds
	private static int  EXPIRY_FOR_RETRIEVED=2;
	
	private static String AUTHENTICATION_CHECK="check_auth";
	
	
	private String msg=null;
	
	private Logger log = LogManager.getLogger(SchedulerAPIServlet.class.getName());

	private static JCS cache=null;
	private static JCS getCache() throws Exception {
		 if(cache==null){
				cache=JCS.getInstance("direct_script");
		 }
		 return cache;
	}
	
	public static Map getAllTokens() throws Exception {
		
		Map rtn=getCache().getMatching("^[A-Za-z0-9\\-]+$");		 
		return  (rtn!=null)?rtn:new HashMap(); //all alpha numeric keys.

	}
	
	
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String method = request.getParameter("method");
		
		if(method!=null && !method.equals("") && (method.equals(SchedulerAPIServlet.EXECUTE_SSCRIPT_P2P) || method.equals(SchedulerAPIServlet.EXECUTE_ASCRIPT_P2P))){
			log.debug("debug doPost: method:"+method);
			try{
				String script=request.getParameter("script");			
				RScript rs=new RScript();
		   		
				
				//set unique name from the parameter
				//rs.setUniquename("[no-unique-name]");
				
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
	   			att.setMaxLifeSeconds(EXPIRY_FOR_UNRETRIEVED);
	   			
	   			RScriptAsyncListenerImpl liten=new RScriptAsyncListenerImpl(getCache(),att);
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
	
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		 
		String method = request.getParameter("method");
		this.msg="Error in current request, Method value required";
		boolean noerror=false;
		if(method!=null && !method.equals("")){
			
			
			/**
			 * executing tasks, accepts parameter ids=<multiple ids separated by comma or expression>
			 * 
			 * expression with code injection can be like the following
			 * [23:inj] your_code_here [inj][2454:inj] your_code_here [inj] 
			 */
			if(method.equals(SchedulerAPIServlet.EXECUTETASKS) && request.getParameter("ids")!=null && !request.getParameter("ids").equals("") && ( request.getParameter("delay")==null || (request.getParameter("delay")!=null && request.getParameter("delay").equals("")))   ){			     
				noerror=executeTask(request,response);
			}else if(method.equals(SchedulerAPIServlet.EXECUTETASKS) && request.getParameter("ids")!=null && !request.getParameter("ids").equals("")
					&&(request.getParameter("delay")!=null && !request.getParameter("delay").equals(""))
					){
					int delay=0;
					boolean validdelay=false;
					try{
						delay=Integer.parseInt(request.getParameter("delay"));
						validdelay=true;
					}catch(Exception e){
						validdelay=false;				
						
					}
					if(validdelay){
						if(delay==0) noerror=executeTask(request,response);
						if(delay>0){
							noerror=executeTaskDelayed(request,response);
						}
					}else{
						this.msg="Integer value expected for delay";
						noerror=false;
						
					}
				
			}else{
				this.msg="Error in current request, Method value required";
				noerror=true;
			}			
			

			/**
			 * Get peer packages, this request will be forwarded to all online peer and wait few seconds to receive all the responses and returns to UI. 
			 * The responses will includes installed packages and version nos 
			 */
			if(method.equals(SchedulerAPIServlet.PEER_PACKAGES)  ){
				String status=null;
				try{
					status=getPeerInfo("rpackages");
				}catch(Exception e){
					status="<error>"+e.getMessage()+"</error>";
				}
				generateResult(response,status);
			}
			

			/**
			 * This returns info about all online peers and the response will includes peer started time, executing tasks, JRI version, OS and R version
			 */
			if(method.equals(SchedulerAPIServlet.PEER_INFO)  ){
				String status=null;
				try{
					status=getPeerInfo("statistics");
					
				}catch(Exception e){
					status="<error>"+e.getMessage()+"</error>";
				}
				generateResult(response,status);
			}

			
			/**
			 * This will return live queue data, such as queued,executing and finished tasks with its trigger time.
			 */
			if(method.equals(SchedulerAPIServlet.EXECUTE_R_QUEUEINFO)  ){
				String status=null;
				try{
					ExecuteRMgmt erm=new ExecuteRMgmt(request);
					status=erm.getQueueXML();
					
				}catch(Exception e){
					status="<error>"+e.getMessage()+"</error>";
				}
				generateResult(response,status);
			}
			

			/**
			 * This will return executeR queue, including number of scripts queued, executing and successfull. 
			 * ExecuteR server is not the same as scheduler server, the request is proxyfied to executeR server  
			 */
			if(method.equals(SchedulerAPIServlet.EXECUTE_R_QUEUE) && request.getParameter("server_ip")!=null ){
				 
				ProxifyURL purl=new ProxifyURL(request,response);
				purl.proxifyGet("http://"+request.getParameter("server_ip")+":8080/bldb/schedulerAPI","method="+SchedulerAPIServlet.EXECUTE_R_QUEUEINFO);
				//System.out.println("SchedulerAPIServerlet.class: proxyfy");
				
			}

			
			
			/**
			 * peer queue information
			 * recommended not to use this, as there are live stream connection between peers and server available and
			 * eventually the queue will be maitained at the server level.
			 * @deprecated
			 */
			if(method.equals(SchedulerAPIServlet.PEER_QUEUE)  ){			
					String status=null;
					try{
						status=getPeerInfo("peerqueue");
					}catch(Exception e){
						status="<error>"+e.getMessage()+"</error>";
					}
					generateResult(response,status);				 
			}

			
			/**
			 * This will be used in other applications to check the current session is authenticated			 
			 * For example: Tools done in .NET and PHP.  
			 */
			if(method.equals(SchedulerAPIServlet.AUTHENTICATION_CHECK)){
		         try{           
		        	 authenticationCheck(request,response);
		         }catch(Exception e){
		        	 generateResult(response,"<status>Error:"+e.getMessage()+"</status>");
		         }
				
			}
						
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
					Log.error("Error:"+e.getMessage());
				 }
				 //if(ri!=null && ri.isFinished()){
				 if(ri!=null && !ri.equalsIgnoreCase(RScriptAsyncListenerImpl.ALIVE)){
					 String resptype=(request.getParameter("responseType")!=null)?request.getParameter("responseType"):"text/xml";
				   	 response.setContentType(resptype);					 
					 PrintWriter out = response.getWriter();	
					 out.print(ri);
					 out.flush();
					 
					 //remove the result from cache and set the storance once it is retrieved.
					 try{
						 IElementAttributes att= getCache().getDefaultElementAttributes();
						 att.setMaxLifeSeconds(EXPIRY_FOR_RETRIEVED);			   			
						 getCache().remove(token);
						 getCache().put(token,ri,att);
					 }catch(Exception e){
						 log.error("Error while resetting token in cache");
					 }
			   					
				 }else if(ri!=null && ri.equalsIgnoreCase(RScriptAsyncListenerImpl.ALIVE)) {
					 //generateResult(response,"<status>not ready</status>");
					    //response.setContentType("text/xml");
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

			
			if(method.equals(SchedulerAPIServlet.SCHEDULER_QUEUEGROUP_ADD) && request.getParameter("taskuid")!=null && !request.getParameter("taskuid").equals("") ){
				String status=addRemoveGroup2Queue(request, response, method);
				generateResult(response,"<status>"+status+"</status>");
			}
			
			
			if(method.equals(SchedulerAPIServlet.REMOVE_EXECUTING_TASK) 
					&& request.getParameter("task_id")!=null && !request.getParameter("task_id").equals("") 
					&& request.getParameter("trigger_time")!=null && !request.getParameter("trigger_time").equals("")
			){
				String status=removeTaskID(request, response);
				generateResult(response,"<status>"+status+"</status>");
			}
			
			
			/**
			 * adding and removing scheduler group, i.e Bloomberg download,  
			 */
			if(method.equals(SchedulerAPIServlet.SCHEDULER_QUEUEGROUP_REMOVE) && request.getParameter("taskuid")!=null && !request.getParameter("taskuid").equals("") ){
				String status=addRemoveGroup2Queue(request, response, method);
				generateResult(response,"<status>"+status+"</status>");
			}

			/**
			 * Restart peer, this method is not very reliable as it uses OS dependent tools to kill the running process and it works only on windows peers and still not reliable and consistent. 
			 */
			if((method.equals(SchedulerAPIServlet.RESTART_PEER) || method.equals(SchedulerAPIServlet.RESTART_PEER_LATER)) 
				&& request.getParameter("peer")!=null && !request.getParameter("peer").equals("")
			){
				String status=restartPeer(method,request);
				generateResult(response,"<status>"+status+"</status>");
			}

			
			
			if(method.equals(SchedulerAPIServlet.SERVER_DATE_TIME)){
				SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS z");
				Date d=new Date();
				generateResult(response,"<time milliseconds=\""+d.getTime()+"\" formatted=\""+format.format(d)+"\" />");
			}
			
			
			
			/**
			 * ment for chaning queue dispatch algorithm, not working well.
			 * @deprecated
			 */
			if(method.equals(SchedulerAPIServlet.PRIORITY_QUEUE) ){
				if(request.getParameter("enable")!=null && request.getParameter("enable").equalsIgnoreCase("true")){
					LoadBalancingLinkedQueue.priorityQueue=true;
				}
				if(request.getParameter("enable")!=null && request.getParameter("enable").equalsIgnoreCase("false")){
					LoadBalancingLinkedQueue.priorityQueue=false;
				}				
				generateResult(response,"<status>"+LoadBalancingLinkedQueue.priorityQueue+"</status>");
			}

			if(method.equals(SchedulerAPIServlet.GETLIVE)){			     
				noerror=getLive(request,response);
			}
			if(method.equals(SchedulerAPIServlet.QUEUE)){			     
				noerror=getQueue(request,response);
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


	
	public String addRemoveGroup2Queue(HttpServletRequest request, HttpServletResponse response,String method){
		String taskuid=request.getParameter("taskuid");
		try{
			 
			if(taskuid!=null && !taskuid.equals("")){
				if(method.equals(SchedulerAPIServlet.SCHEDULER_QUEUEGROUP_ADD)){
					new SchedulerEngine().addGroup2Queue(taskuid);
				}
				if(method.equals(SchedulerAPIServlet.SCHEDULER_QUEUEGROUP_REMOVE)){
					new SchedulerEngine().removeGroup2Queue(taskuid);
				}
				
			}else{
			   throw new Exception("Parameter taskuid not found");	
			}
            return "success";			
		}catch(Exception e){
			return "Error:" +e.getMessage();
					
		}
		
	}
	
	
	
	private String removeTaskID (HttpServletRequest request, HttpServletResponse response){
		int scheduler_id=0;
		long trigger_time=0;
		try{
			scheduler_id=Integer.parseInt( request.getParameter("task_id"));
			trigger_time=Long.parseLong( request.getParameter("trigger_time"));
			
				new ExecutingQueueCleaner().removeFaultyExecutingTask(scheduler_id, trigger_time);
			
			return "success";
		}catch(Exception e){
			return "ERROR:"+e.getMessage();
		}
		
	}
 
	private boolean executeTask(HttpServletRequest request, HttpServletResponse response){

		
		try{			
			
			String ids_exp=request.getParameter("ids");
			String suffixcode=null;
			String status=new SchedulerEngine().executeScriptExpression(ids_exp, "SchedulerAPI",suffixcode);
			response.setContentType("text/xml");
		    PrintWriter out = response.getWriter();		    
		    out.println("<?xml version=\"1.0\"?>");		    
		    out.println("<result>"+status+"</result>");
		    out.flush();
			
			return true;
		}catch(Exception e){
			log.error("executeTask() Error:"+e.getMessage());
			this.msg="Error occured: MSG:"+e.getMessage();
			return false;
		}
	}
	
	private boolean executeTaskDelayed(HttpServletRequest request, HttpServletResponse response){
		StringTokenizer st=new StringTokenizer(request.getParameter("ids"),",");
		Vector v=new Vector();
		while(st.hasMoreTokens()){
			v.add(st.nextToken());
		}
		
		int delay=Integer.parseInt(request.getParameter("delay"));
		
		try{			
			SchedulerDB sdb=SchedulerDB.getSchedulerDB();			
			sdb.connectDB();    			
			for(Iterator i=v.iterator();i.hasNext();){
				String sid=(String)i.next();
			    int scheduler_id=Integer.parseInt(sid);
			    
				Map data=sdb.getScheduler(scheduler_id);
				if(data!=null){
	    			Number active=(Number)data.get("active");
	    			String taskuid=(String)data.get("taskuid");
	    			String name=(String)data.get("name");					
    				new SchedulerEngine().runJobDelayed(data,taskuid,sdb,delay,"SchedulerAPI");
			   } 
			}
			sdb.closeDB();			
			response.setContentType("text/xml");
		    PrintWriter out = response.getWriter();		    
		    out.println("<?xml version=\"1.0\"?>");
		    out.println("<result>done</result>");
		    out.flush();
			return true;
		}catch(Exception e){
			ClientError.reportError(e, null);
			//throw e;
			this.msg="Error occured: MSG:"+e.getMessage();
			return false;
		}
	}

	
	private void generateResult(HttpServletResponse response,String status){
		
		try{
			response.setContentType("text/xml");
		    PrintWriter out = response.getWriter();		
		    out.println("<?xml version=\"1.0\"?>");
		    out.println("<result>"+status+"</result>");
		    out.flush();
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	

	private boolean getLive(HttpServletRequest request, HttpServletResponse response) {
		
		    try{
		    	
		    	SchedulerMgmt sm=new SchedulerMgmt();
			    Map t=sm.getOnlinePeers();
			 			
				response.setContentType("text/xml");
			    PrintWriter out = response.getWriter();
			    
			    out.println("<?xml version=\"1.0\"?>");
			    
			    out.println("<result>");
			    for(Iterator i=t.keySet().iterator();i.hasNext();){
			    	String peer=(String)i.next();
			    	out.println("<peer status=\""+t.get(peer)+"\">"+peer+"</peer>");
			    }
			    out.println("</result>");
			    out.flush();			
			    
			    return true;
			    
		    }catch(Exception e){
		    	return false;
		    }
	}
	
	
	private boolean getQueue(HttpServletRequest request, HttpServletResponse response) {
		try{
			Collection<LoadBalancingQueueItem> executingTasks=LoadBalancingQueue.getDefault().getExecutingTasks();
			Collection<LoadBalancingQueueItem> queuedTasks=LoadBalancingQueue.getDefault().getQueuedTasks();
			
			response.setContentType("text/xml");
		    PrintWriter out = response.getWriter();
			out.println("<?xml version=\"1.0\"?>");			    
			out.println("<result>");
			
			for(Iterator<LoadBalancingQueueItem> i=executingTasks.iterator();i.hasNext();){
				LoadBalancingQueueItem item=i.next();
				 
				
				out.println("<task status=\"EXCECUTING\" id=\""+item.getSchedulerid()+"\" trigger_time=\""+item.getSf().getTrigger_time()+"\" task_sent_time=\""+item.getStarted().getTime()+"\" peer=\""+item.getMachine()+"\"></task> ");
			}
			for(Iterator<LoadBalancingQueueItem> i=queuedTasks.iterator();i.hasNext();){
				 LoadBalancingQueueItem item=i.next();
				 item.getSchedulerid() ;
				 out.println("<task status=\"WAITING\" id=\""+item.getSchedulerid()+"\"  trigger_time=\""+item.getSf().getTrigger_time()+"\" task_sent_time=\"\" peer=\"\"></task> ");
			}
			 out.println("</result>");
			 out.flush();
		    return true;
	    }catch(Exception e){
	    	return false;
	    }
		
	}
	
	/**
	 * @deprecated
	 * not working well. 
	 * Recommended not to try this method in production, as it is not very reliable and also windows dependent, linux/unix peers won't work
	 * @return
	 */
	public static String restartServer() {
		   TimerTask tt=new TimerTask() {
	            public void run() {
	           
	            	RestartTomcat.restartMainServer();
	            }
	        };
	        //checks every 1 minute to see if a thread still running....
	        long delay=4*1000;
	        Timer timer = new Timer();		        
	        timer.schedule(tt,delay);
	        
		
		 return "Server has been restarted, Please check the server status after 1 minute ";
	}
	
	 
	private void authenticationCheck(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String ky=(String)request.getSession().getAttribute(Constant.SESSION_LOGGED_USER);


		if((ky==null || (ky!=null && ky.equals(""))) ){	
			
			
			
			String user=null;
			String pwd=null;
			Cookie cookie[]=request.getCookies();
			if(cookie!=null){
				for(int i=0;i<cookie.length;i++){
					if(cookie[i].getName().equals("4eprevuser") && cookie[i].getValue()!=null) {
						user=cookie[i].getValue();			
					}
					if(cookie[i].getName().equals("4esessionuid") && cookie[i].getValue()!=null) {
						pwd=cookie[i].getValue();
					}
				}
			}
			 
			if(user!=null && pwd!=null && !user.equals("") && !pwd.equals("")){
				LoginMgmt lm=new LoginMgmt(request);
				try{
					Map data=lm.validateRememberedUser(user,pwd);
					boolean success=(Boolean)data.get("loggedin");
					if(success){		 

						Cookie killMyCookie = new Cookie("4eprevuser", user);
				    	killMyCookie.setMaxAge(60*60*24*30);    //30 days 
				    	response.addCookie(killMyCookie);
				     
						Cookie killMyCookie2 = new Cookie("4esessionuid", pwd);
						killMyCookie2.setMaxAge(60*60*24*30);    //30 days     
				    	response.addCookie(killMyCookie2);
					}
				}catch(Exception e){
					log.error("Erorr:"+e.getMessage());
				}
			}

		}
		ky=(String)request.getSession().getAttribute(Constant.SESSION_LOGGED_USER);
	 
		response.setContentType("application/javascript");
		PrintWriter out = response.getWriter();
 
		 
		if(ky==null || (ky!=null && ky.equals(""))){
			if(request.getParameter("redirect")!=null){
				out.print("window.location='http://4ecapsvsg2:8080/bldb/login.jsp?referer="+request.getParameter("redirect")+"'");
			}else{
				out.print("window.location='http://4ecapsvsg2:8080/bldb/login.jsp'");
			}
		}
		if(ky!=null && !ky.equals("")) {
			out.print("var loggedin_user=new Object();loggedin_user.username='"+ky+"'");
		}
			
	 
		 
		out.flush();
	}
	
	
	
	/**
	 * @deprecated
	 * @param method
	 * @param request
	 * @return
	 */
	public static String restartPeer(String method,HttpServletRequest request) {
 
		
			   String peer=request.getParameter("peer");
		      
			   PeerGroup netPeerGroup =  P2PService.getPeerGroup();
			   PipeAdvertisement pipeAdv = new P2PAdvertisement().getPipeAdvertisement(peer,netPeerGroup);
			   MessageBean mb=new MessageBean();
			 
			   mb.setType(MessageBean.TYPE_REQUEST);   
			   if(method.equals(SchedulerAPIServlet.RESTART_PEER))  mb.setCommand("RESTART_PEER");
			   if(method.equals(SchedulerAPIServlet.RESTART_PEER_LATER)) { 
				   if(request.getParameter("kill")!=null &&!request.getParameter("kill").equals("")){
					   mb.setCommand("RESTART_PEER_LATER:"+request.getParameter("kill"));
				   }else{
					   mb.setCommand("RESTART_PEER_LATER");
				   }
			   
			   }
			   mb.setSender(P2PService.getComputerName());
			   
			   OutgoingMessage ogM=new OutgoingMessage(null,mb,peer);
			   
			   PipeService pipeService = P2PService.getPipeService();
			   try{
				    
					pipeService.createOutputPipe(pipeAdv,ogM);
					
			   }catch(Exception e){
				   	  e.printStackTrace();
				   	  
				   	  PipeService pipeService1 = P2PService.getNewPipeService();
				   	  try{
				   		  pipeService1.createOutputPipe(pipeAdv,ogM);
				   	  }catch(Exception e1){			   		  
				   		  e1.printStackTrace();
				   	  }
					 
			   }
			   return "Restart request has been sent to peer  "+peer;
	}
	
	
	
	 private String getPeerInfo(String what) throws Exception{
		  try{
			    if(what.equals("statistics")){
			    	 
			    	LoadBalancingQueue.getDefault().updatePeerData(P2PTransportMessage.COMMAND_STATISTICS);
			    }
			    if(what.equals("rpackages")){
			    	 
			    	LoadBalancingQueue.getDefault().updatePeerData(P2PTransportMessage.COMMAND_R_PACKAGES);
			    }
			    if(what.equals("peerqueue")){
			    	 
			    	LoadBalancingQueue.getDefault().updatePeerData(	P2PTransportMessage.COMMAND_PEER_QUEUE);
			    
			    }
			    
	
				try{				
					Thread.sleep(1000);
				}catch(Exception e){
					
				}
				
				String rtn="";
				 
				Map data=null;
				 
				if(what.equals("statistics") || what.equals("rpackages")){
					
				    data=(what.equals("statistics"))?IncomingMessage.getPeerStatistics():IncomingMessage.getPeerRPackages();		
 		    
				    
				    Vector allKeys=new Vector();				    
				    HashMap pdata=new HashMap();
				    for(Iterator it=data.keySet().iterator();it.hasNext();){
						String peer=(String)it.next();
						String value=(String)data.get(peer);
						if(peer!=null && value!=null && !value.equals("")){					
						 
							StringTokenizer st=new StringTokenizer(value,"|");
							HashMap h=new HashMap(); 					
							while(st.hasMoreTokens()){
								String pair=st.nextToken();
								StringTokenizer st1=new StringTokenizer(pair,"=");					
								if(st1.countTokens()>=2){
									String ky=st1.nextToken();
									String val=st1.nextToken();
									if(!allKeys.contains(ky)) allKeys.add(ky);
									h.put(ky, val);
								}
							}
							pdata.put(peer, h);							 
							 
						}	
					}
				    
				    for(Iterator i=pdata.keySet().iterator();i.hasNext();){
				    	String peer=(String)i.next();
				    	Map pack=(Map)pdata.get(peer);
				    	rtn+="<peer ";
				    	for(Iterator ia=allKeys.iterator();ia.hasNext();){
				    		String ky=(String)ia.next();
							String val=(pack.get(ky)==null)?"0":(String)pack.get(ky);							
							rtn+=ky+"=\""+val+"\" ";		
				    	}
				    	rtn+="/>";
				    }
				}
				
				if(what.equals("peerqueue")){					 
					 data=IncomingMessage.getPeerQueueStat();
					 
					 for(Iterator it=data.keySet().iterator();it.hasNext();){
							String peer=(String)it.next();
							String value=(String)data.get(peer);
							if(peer!=null && value!=null && !value.equals("")){						
								
								StringTokenizer st=new StringTokenizer(value,"|");
								
								while(st.hasMoreTokens()){
									
									rtn+="<task ";
									String taskd=st.nextToken();
									StringTokenizer st0=new StringTokenizer(taskd,",");		
									while(st0.hasMoreTokens()){
										String pair=st0.nextToken();
										StringTokenizer st1=new StringTokenizer(pair,"=");					
										if(st1.countTokens()>=2){
											String ky=st1.nextToken();
											String val=st1.nextToken();
											rtn+=ky+"=\""+val+"\" ";								
										}
									}
									rtn+="/>";
								}								
							}	
						}	
					 
				}
	  		    return rtn;
			}catch(Exception e){
				ClientError.reportError(e, "what:"+what);
				throw e;
			}
	  }
}



