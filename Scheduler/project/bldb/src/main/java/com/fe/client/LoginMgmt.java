/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fe.client;

import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.directwebremoting.WebContextFactory;
import org.directwebremoting.util.Logger;

import com.fe.common.Constant;
import com.fourelementscapital.auth.SuperUserAuthentication;
import com.fourelementscapital.auth.WikiAuthentication;


/**
 * This class used authenticating the user. 
 * @author Administrator
 *
 */
public class LoginMgmt {

	
	private Logger log=Logger.getLogger(LoginMgmt.class);
	
	/**
	 * for DWR call
	 */
	public LoginMgmt(){
		
	}
	
	
	/**
	 * for JSP or internal call
	 * @param request
	 */
	public LoginMgmt(HttpServletRequest request){
		this.request=request;
	}

	//private static String dbname=Config.getString("db_wiki");
	private HttpServletRequest request=null;
	
	/**
	 * returns wiki database name for authentication and validation purpose
	 * @return
	 */
	//public static String getDBName() {
	//	return dbname;
	//}
	
 
	 
	/**
	 * Login validation for given username and password
	 * @param user
	 * @param password
	 * @return
	 * @throws Exception
	 */
	public Map login(String user,String password) throws Exception {		
		
		HashMap rtn=new HashMap();
		try{
			//log.debug("connecting wiki db...db:"+wdb.getDriver(dbname)+" driver:");

			//log.debug("connected catalog:"+wdb.connection().getCatalog());
			
			boolean success= WikiAuthentication.validateUser(user, password);
			if(!success){				
				rtn.put("message", "Invalid username or password");				
			}else{
				HttpSession session=getRequest().getSession(true);
				session.setMaxInactiveInterval(24*60*60); //1 day;				
				session.setAttribute(Constant.SESSION_LOGGED_USER, user);
				boolean cookielogin=false;
				session.setAttribute(Constant.SESSION_LOGGED_HEADER, getHeaders(getRequest(),cookielogin));			 
				String pss=WikiAuthentication.getEncryptedPwd(user);
				rtn.put("fesessionuid", pss);
			}
			rtn.put("loggedin", success);
			return rtn;
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}
	
	
	
	private Map getHeaders(HttpServletRequest req, boolean cookielogin) throws Exception {
		HashMap rtn=new HashMap();
		Enumeration<String> h=req.getHeaderNames();
		while(h.hasMoreElements()){
			String header=h.nextElement();
			rtn.put(header, req.getHeader(header));
		}
		rtn.put(Constant.SESSION_LOGGED_HEADER_COOKIELOGIN, cookielogin);
		rtn.put(Constant.SESSION_LOGGED_HEADER_IP,getRequest().getRemoteAddr());		
		return rtn;
	}
	
	
	/**
	 * Logging in as super user to over-ride some of the user privileged options
	 * 
	 * @param user
	 * @param password
	 * @param newpassword
	 * @return
	 * @throws Exception
	 */
	public Map loginSuperUser(String user,String password,String newpassword) throws Exception {		
			
		   
			HashMap rtn=new HashMap();
			try{

				boolean success=false;
				if(user.equalsIgnoreCase("administrator") && newpassword!=null && !newpassword.equals("")){
					success = SuperUserAuthentication.changePwd(password,newpassword);
					
				}else{
					success = SuperUserAuthentication.validateOrSet(password); //validation implementation to be done..
				}
				if(!success){				
					rtn.put("message", "Invalid username or password");				
				}else{
					HttpSession session=getRequest().getSession(true);									
					session.setAttribute(Constant.SESSION_LOGGED_SUPERUSER, user);					
				}
				rtn.put("loggedin", success);
				return rtn;
			}catch(Exception e){
				e.printStackTrace();
				throw e;
			}
			finally{
				//wdb.closeDB();		
			}
			
	}
	
	
	
	/**
	 * validates remembered password on browser cookie, the password is stored in md5 hash value
	 * @param user
	 * @param password
	 * @return
	 * @throws Exception
	 */
	public Map validateRememberedUser(String user,String password) throws Exception {		
		
		HashMap rtn=new HashMap();
		try{
			
			boolean success=false;
			String passcode=URLDecoder.decode(password);
			log.debug("user:"+user+" passcode:"+passcode);
			
			String pss=WikiAuthentication.getEncryptedPwd(user);
			
			if(!passcode.equals(pss)){				
				rtn.put("message", "Invalid username or password");				
			}else{

				success=true;
				HttpSession session=getRequest().getSession(true);
				session.setMaxInactiveInterval(24*60*60); //1 day;				
				session.setAttribute(Constant.SESSION_LOGGED_USER, user);
					
				boolean cookielogin=true;
				session.setAttribute(Constant.SESSION_LOGGED_HEADER, getHeaders(getRequest(),cookielogin));				

					//wdb.getEncryptedPwd(user_name)
					//String pss=wdb.getEncryptedPwd(user);
				rtn.put("fesessionuid", pss);
			}
			rtn.put("loggedin", success);
			log.debug("rtn:"+rtn);
			return rtn;
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
		
	}

	
	
	/**
	 * a standard logout 
	 * @throws Exception
	 */
	public void logout() throws Exception {		
		HttpSession session=getRequest().getSession();
		session.removeAttribute(Constant.SESSION_LOGGED_USER);
		session.invalidate();
		
	}
	
	
	private HttpServletRequest getRequest() throws Exception  {
		return (request == null && WebContextFactory.get()!=null) ? WebContextFactory.get().getHttpServletRequest() : request;

	}
	
	
	
	
}



