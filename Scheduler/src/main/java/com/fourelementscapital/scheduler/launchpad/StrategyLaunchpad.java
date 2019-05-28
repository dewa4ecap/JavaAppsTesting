/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.launchpad;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fourelementscapital.loadbalance.SchedulerEngine;
import com.fourelementscapital.scheduler.common.LaunchpadMgmt;
import com.fourelementscapital.sso.SSO;

import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;

public class StrategyLaunchpad extends HttpServlet {
	
	private void executeScript(HttpServletRequest request, HttpServletResponse response, String username){
		
		try{			
			LaunchpadMgmt pad = new LaunchpadMgmt();
			Integer id = pad.addScriptFrom2ndScheduler(request.getParameter("script"), 0, username, "");
			int data = pad.executeTask2ndScheduler(id, 0, username);
			
			String result = null;
			
			while(result==null) {
				result = pad.getConsole2ndScheduler(data);
				TimeUnit.SECONDS.sleep(1);
			}
			
			response.setContentType("application/json");
		    PrintWriter out = response.getWriter();		    
		    out.println(result);		    
		    out.flush();
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * This method does preprocess of a HTTP GET request. Called when user access StrategyLaunchpad URL. Steps :
	 * 1. Initialization
	 * 2. Get Cookies
	 * 3. Validate Cookies with SSO library
	 * 4. Grant / deny access
	 * @param HTTP GET request, HTTP response   
	 * @return void
	 * @throws ServletException, IOException
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String getMethod = request.getParameter("method");
		String getScript = request.getParameter("script");
		
		String checkSID = "";
		String checkUsername = "";
		boolean goLogin = false;
		String referrer = request.getScheme() + "://" +  request.getServerName() + ":" + request.getServerPort();	// https://scheduler.alphien.com:8443
		String newLine = "";
		String resptype="text/html";
   	 	response.setContentType(resptype);
		PrintWriter out = response.getWriter();
		ClassLoader cl = getClass().getClassLoader();
		
		//get ALSID and ALUSER cookie
		Cookie[] cookies = request.getCookies();
		if(cookies != null) {
			for (int i = 0; i < cookies.length; i++) {
		    	  if (cookies[i].getName().equals("ALSID")) { 
					checkSID = cookies[i].getValue(); 
				}
		    	  if (cookies[i].getName().equals("ALUSER")) { 
					checkUsername = cookies[i].getValue(); 
				}
		       }
		} else { goLogin = true; }
		if (checkSID.equals("")) { goLogin = true; }
		
		//if cookies are null or empty than redirect to login page 
		if (goLogin) { response.sendRedirect(referrer+"/scheduler/login.jsp"); }
		
		//validate cookies with SSO library
		SSO sso = new SSO();
	    boolean isPass = false;
		try {	
			isPass = sso.validateSession(checkSID, checkUsername);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if ((getMethod!=null)&&(getScript!=null)) {
			
			
			if (isPass) {
				//API Start
				
				if(getMethod.equals("run")  ){
					try{
						executeScript(request,response,checkUsername);
						
					}catch(Exception e){
						
						response.setContentType("text/xml");
					    PrintWriter outError = response.getWriter();		    
					    outError.println("<?xml version=\"1.0\"?>");		    
					    outError.println("<result><error>"+e.getMessage()+"</error></result>");
					    outError.flush();
					}
					
				}
				
				//API Finish
			} else {
				File file = new File(cl.getResource("template/access-denied.template").getFile());
				BufferedReader br = new BufferedReader(new FileReader(file));
				String line = br.readLine();
				try {
					//invalid cookies. Access denied
				    while (line != null) {
				    	out.println(line);
				        line = br.readLine();
				    }
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
				    br.close();
				    out.flush();
				}
				
			}
			
		} else {
			
			//grant or deny access
			File file = new File(cl.getResource((isPass ? "template/strategy-launchpad.template" : "template/access-denied.template")).getFile());
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = br.readLine();
			try {
				//valid cookies. Access granted
				if (isPass) {
				    while (line != null) {
				    	newLine = line;
				    	out.println(newLine.contains("CURRENT_USER") ? newLine.replace("CURRENT_USER", checkUsername) : 
				    		newLine.contains("CURRENT_TOKEN") ? newLine.replace("CURRENT_TOKEN", checkSID ) : newLine);
				        line = br.readLine();
				    }
				} 
				//invalid cookies. Access denied
				else {
				    while (line != null) {
				    	out.println(line);
				        line = br.readLine();
				    }
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
			    br.close();
			    out.flush();
			}
		}
	}
}
