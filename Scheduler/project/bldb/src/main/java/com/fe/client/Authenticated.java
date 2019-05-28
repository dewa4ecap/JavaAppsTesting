/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fe.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.directwebremoting.WebContextFactory;

import com.fe.common.Constant;
import com.fourelementscapital.auth.UserThemeAccessPermission;
import com.fourelementscapital.db.AbstractTeamOrgDB;
import com.fourelementscapital.db.InfrastructureDB;
import com.fourelementscapital.scheduler.error.ClientError;

/**
 * This class is abstraction layer for the secured data, which means can be accessed only after logged in.
 * Any class that exposes user privileged data should be extended from Authenticated class
 * @author Rams
 *
 */
public abstract class Authenticated {

	private HttpServletRequest request=null;
	public static String REMOTE_IP="$clientIpAddress";	
	private Logger log = LogManager.getLogger(Authenticated.class.getName());
	public static final String SESS_AUTHENTICATED_USER="$$AuthenticatedUserObj"; 
	
	
	/**
	 * No authentication, which means this constructed to be used internally within JVM.
	 * @param ignoreAuthentication
	 */
	protected Authenticated(boolean ignoreAuthentication){
		
	}
	
	
	/**
	 * Constructor for DWR 
	 * @throws Exception
	 */
	public Authenticated() throws Exception {
		
		if(getAuthenticatedUser()==null){
			throw new Exception("SESSION LOGGED OUT: Access denied for this request");
		}
				
		try{
			if(getRequest().getSession().getAttribute(REMOTE_IP)==null){
				getRequest().getSession().setAttribute(REMOTE_IP, getRequest().getRemoteAddr());
			}
		}catch(Exception e){
			log.error("Error while setting up session variable remote-ipaddress ");
		}
		
	}
	
	
	/**
	 * This constructor to be used within JSP or interal call within java runtime.
	 * @param request
	 * @throws Exception
	 */
	public Authenticated(HttpServletRequest request) throws Exception {
		this.request=request;
		if(getAuthenticatedUser()==null){
			throw new Exception("Requested user is not authenticated ");
		}
		
		try{
			if(getRequest().getSession().getAttribute(REMOTE_IP)==null){
				getRequest().getSession().setAttribute(REMOTE_IP, getRequest().getRemoteAddr());
			}
		}catch(Exception e){
			log.error("Error while setting up session variable remote-ipaddress ");
		}

	}

	 
	protected UserThemeAccessPermission getAuthenticatedUserObj( AbstractTeamOrgDB sdb) throws Exception {
			String currentuser=getAuthenticatedUser();		
			//String currentuser="ls"; //getAuthenticatedUser();
			UserThemeAccessPermission auser=(UserThemeAccessPermission)getRequest().getSession().getAttribute(Authenticated.SESS_AUTHENTICATED_USER);
			InfrastructureDB infrastructureDB = InfrastructureDB.getInfrastructureDB();
			try{			
				if(auser==null){
					infrastructureDB.connectDB();
					Map<String,String> themes=infrastructureDB.getThemes4Users(currentuser);
					
					auser = new UserThemeAccessPermission(currentuser);
					auser.addPermissionWithThemes(themes);
					 
					getRequest().getSession().setAttribute(Authenticated.SESS_AUTHENTICATED_USER,auser);
				}
			} catch(Exception e) {
				throw e;
			} finally {
				infrastructureDB.closeDB();
			}
			return auser;
		}
		
		
 
	/**
	 * Get authenticated user in any DWR call.
	 * @return
	 * @throws Exception
	 */
	protected String getAuthenticatedUser() throws Exception {
		String ky=(String)getRequest().getSession().getAttribute(Constant.SESSION_LOGGED_USER);
		String usr=null;
		log.debug("getAuthenticatedUser ----ky:"+ky);
		if((ky==null || (ky!=null && ky.equals(""))) ){
			
			String user=null;
			String pwd=null;
			Cookie cookie[]=getRequest().getCookies();
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
			if(user!=null && pwd!=null){
				LoginMgmt lm=new LoginMgmt();
				log.debug("user:"+user+" pwd:"+pwd);
				Map dt=lm.validateRememberedUser(user, pwd);
				log.debug("dt:"+dt);				
				if((Boolean)dt.get("success")){
					usr=user;
				}
			}
			//usr=user;
		}else{
			usr=ky;
		}
		log.debug("getAuthenticatedUser ----@@@ky:"+ky);
		
		//System.out.println("Authenticated.class: getAuthenticatedUser():"+usr);
		if(usr!=null && usr.equals("")){
			usr=null;
		}		
		return usr;
	}
	
	protected void setRequest(HttpServletRequest request) throws Exception  {
		  this.request=request;
	}
		
	protected HttpServletRequest getRequest() throws Exception  {
		return (request == null && WebContextFactory.get()!=null) ? WebContextFactory.get().getHttpServletRequest() : request;
	}
	
 
	

	protected Map getThemeHirarchy(List<String> themes, AbstractTeamOrgDB sdb) throws Exception {
		   
		    //SchedulerDB sdb=SchedulerDB.getSchedulerDB();	
			InfrastructureDB infrastructureDB = InfrastructureDB.getInfrastructureDB();
			try{
				String th="";				
				HashMap rtn=new HashMap();
				//StringTokenizer st=new StringTokenizer(themes,",");
				//while(st.hasMoreTokens()){
				for(String tkn: themes ){
					//String tkn=st.nextToken();
					th+=th.equals("")?"'"+tkn+"'":",'"+tkn+"'";
				}			
				infrastructureDB.connectDB();
				Map<String, String> data=!th.equals("")?infrastructureDB.getTeamOrg(th, getPermissionHierarchy()):new HashMap();
				
				Map<String,String> user_privileges=UserThemeAccessPermission.replacePermissions(data);
				
				return user_privileges;
				
			}catch(Exception e){
				ClientError.reportError(e, null);
				e.printStackTrace();
				throw e;
			}finally{
				//sdb.closeDB();
				infrastructureDB.closeDB();
			}
	 }
	 
	
	 private List getPermissionHierarchy(){
		 
		 	ArrayList h=new ArrayList();
			h.add("X");
			h.add("M");
			h.add("S");
			h.add("B");				
			h.add("C");
			h.add("U");
			h.add("N");
			return h;
	 }

}


