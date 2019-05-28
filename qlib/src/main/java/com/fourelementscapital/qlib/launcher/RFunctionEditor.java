/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.qlib.launcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jfree.util.Log;

import com.fourelementscapital.config.Config;
import com.fourelementscapital.config.Constant;
import com.fourelementscapital.qlib.common.MenuMgmtSSO;
import com.fourelementscapital.sso.SSO;

public class RFunctionEditor extends HttpServlet {
	
	/**
	 * This method does preprocess of a HTTP GET request. Called when user access RFunctionEditor URL. Steps :
	 * 1. Initialization
	 * 2. Get Cookies
	 * 3. Validate Cookies with SSO library
	 * 4. Grant / deny access
	 * @param HTTP GET request, HTTP response   
	 * @return void
	 * @throws ServletException, IOException
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		HttpSession mSession = request.getSession();
		
		//initialization
		boolean goLogin = false;
		String newLine = "";
		String checkSID = "";
		String checkUsername = "";
		String referrer = request.getScheme() + "://" +  request.getServerName() + ":" + request.getServerPort(); // https://qlib.alphien.com:8444
		String resptype="text/html";
		
		String session_logged_user = "";
		String session_logged_super_user = "";
		
		//get ALSID and ALUSER cookie
		Cookie[] cookies = request.getCookies();
		if(cookies != null) {
			for (int i = 0; i < cookies.length; i++) {
		    	  if (cookies[i].getName().equals("ALSID")) { checkSID = cookies[i].getValue(); }
		    	  if (cookies[i].getName().equals("ALUSER")) { checkUsername = cookies[i].getValue(); }
		       }
		} else { goLogin = true; }
		
		try {
			session_logged_user = mSession.getAttribute(Constant.SESSION_LOGGED_USER).toString();
		} catch (Exception e) {
			mSession.setAttribute(Constant.SESSION_LOGGED_USER, checkUsername);
		}
		
		try {
			session_logged_super_user = mSession.getAttribute(Constant.SESSION_LOGGED_SUPERUSER).toString();
		} catch (Exception e) {
			session_logged_super_user = "";
		}
		
		
   	 	response.setContentType(resptype);	
   	 	PrintWriter out = response.getWriter();
	    ClassLoader cl = getClass().getClassLoader();
	    		
		
		if (checkSID.equals("")) { goLogin = true; }
		//if cookies are null or empty than redirect to login page 
		if (goLogin) { response.sendRedirect(referrer+"/qlib/login.jsp"); }
		//if (goLogin) { response.sendRedirect(referrer+"/login.jsp"); }
		//validate cookies with SSO library
		SSO sso = new SSO();
	    boolean isPass = false;
		try {	
			isPass = sso.validateSession(checkSID, checkUsername);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//Get All Param Request and Required Vars for qlib.template (START)
		
		String ace_editor_checked="";
		String reditor_admin_checked="";
		
		String servlet_path = "";
		String request_url = "";
		String use_ace = "";
		String lite = "";
		String edit = "";
		String open_functions = "";
		String search_r = "";
		
		servlet_path = request.getServletPath();
		request_url = request.getRequestURL().toString();
		try {
			MenuMgmtSSO mgmt=new MenuMgmtSSO(request, checkUsername);
			Map<String,String> pref= new HashMap<String, String>();
			pref = mgmt.getUserPref();
			
			//if(pref.get("beta_ace_editor")!=null && pref.get("beta_ace_editor").equalsIgnoreCase("true")) {
				ace_editor_checked = "checked";
			//}
			
			//if(pref.get("beta_reditor_admin")!=null && pref.get("beta_reditor_admin").equalsIgnoreCase("true")) {
				reditor_admin_checked=" checked ";
			//}
		} catch (Exception e) {
			e.printStackTrace();
		}
		String users=Config.getString("superuser.enable");
		
		List<String> getList = new ArrayList<String>();
		
		getList = Arrays.asList(users.toLowerCase().split(","));
		
		boolean senabled=getList.contains(checkUsername.toLowerCase());
			
		try {
			use_ace = request.getParameter("use_ace").toString();
		} catch (Exception e) {
			Log.debug("!!!No use_ace");
		}
		try {
			lite = request.getParameter("lite").toString();
		} catch (Exception e) {
			Log.debug("!!!No lite");
		}
		
		try {
			edit = request.getParameter("edit").toString();
		} catch (Exception e) {
			Log.debug("!!!No Edit");
		}
		
		try {
			open_functions = request.getParameter("open_functions").toString();
		} catch (Exception e) {
			Log.debug("!!!No open_functions");
		}
		
		try {
			search_r = request.getParameter("search_r").toString();
		} catch (Exception e) {
			Log.debug("!!!No search_r");
		}
		
		//Get All Param Request and Required Vars for qlib.template (FINISH)
		
		//grant or deny access
		File file = new File(cl.getResource((isPass ? "template/qlib.template" : "template/access-denied.template")).getFile());
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = br.readLine();
		try {
			//valid cookies. Access granted
			if (isPass) {
				while (line != null) {
					newLine = line;
					out.println(newLine.contains("PARM_SERVLET_PATH") ? 
								newLine.replace("PARM_SERVLET_PATH", "\""+ servlet_path + "\"") : 
								
								newLine.contains("PARM_REQUEST_URI") ? 
								newLine.replace("PARM_REQUEST_URI",  request_url ) : 
									
								newLine.contains("PARM_USE_ACE") ? 
								newLine.replace("PARM_USE_ACE",  "\""+use_ace+ "\"" ) : 
									
								newLine.contains("PARM_LITE") ? 
								newLine.replace("PARM_LITE",  "\""+lite + "\"") :
									
								newLine.contains("PARM_EDIT") ? 
								newLine.replace("PARM_EDIT",  "\""+ edit + "\"" ) : 
												
								newLine.contains("PARM_OPEN_FUNCTIONS") ? 
								newLine.replace("PARM_OPEN_FUNCTIONS",  "\""+ open_functions + "\"" ) : 
								
								newLine.contains("PARM_SEARCH_R") ? 
								newLine.replace("PARM_SEARCH_R",  "\""+ search_r + "\"" ) : 
									
								newLine.contains("PARM_LOGGED_USER") ? 
								newLine.replace("PARM_LOGGED_USER",  "\""+checkUsername+ "\"" ) :
												
								newLine.contains("PARM_LOGGED_SUPER") ? 
								newLine.replace("PARM_LOGGED_SUPER",  "\""+ session_logged_super_user + "\"" ) : 
															
								newLine.contains("PARM_ACE_CHECKED") ? 
								newLine.replace("PARM_ACE_CHECKED",  "\""+ace_editor_checked + "\"") :
																		
								newLine.contains("PARM_REDITOR_CHECKED") ? 
								newLine.replace("PARM_REDITOR_CHECKED",  "\""+reditor_admin_checked + "\"") :
									
								newLine.contains("PARM_SENABLED") ? 
								newLine.replace("PARM_SENABLED",  (senabled?"true":"false") ) :
									
								newLine.contains("ALUSER_COOKIE") ? 
								newLine.replace("ALUSER_COOKIE",  "\""+checkUsername + "\"") :
									
								newLine.contains("ALSID_COOKIE") ? 
								newLine.replace("ALSID_COOKIE",  "\""+checkSID + "\"") :
									
								newLine);
					
			        line = br.readLine();
			        
			    }
			} 
			//invalid cookies.
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
		}
	}
}
