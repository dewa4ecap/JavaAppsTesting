/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.iexec.common;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.fourelementscapital.auth.UserThemeAccessPermission;
import com.fourelementscapital.client.Authenticated;
import com.fourelementscapital.db.InfrastructureDB;
import com.fourelementscapital.db.SchedulerDB;

@SuppressWarnings("unchecked")
public class IExecAccessMgmt extends Authenticated {

	public IExecAccessMgmt() throws Exception {
		super();
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * constructor for jsp 
	 * @param request
	 * @throws Exception
	 */
	public IExecAccessMgmt(HttpServletRequest request) throws Exception {
		super(request);
	}
	
	/**
	 * update user preference, this preference available on pressing + button on the menu bar
	 * @param key
	 * @param val
	 * @return
	 * @throws Exception
	 */
	public boolean checkUserPermission() throws Exception {
		
				
			SchedulerDB sdb=SchedulerDB.getSchedulerDB();
			try{
				sdb.connectDB();
				UserThemeAccessPermission au = getAuthenticatedUserObj(sdb);
				ArrayList fullaccess = au.getRwx();
				ArrayList readexecaccess = au.getRx();
				ArrayList readaccess = au.getR();
				fullaccess.addAll(readexecaccess);
				fullaccess.addAll(readaccess);				
				if(fullaccess.contains("iExec"))
					return true;
				else
					return false;				
			}catch(Exception e){
				e.printStackTrace();
				throw e;
			}
			finally{
				sdb.closeDB();		
			}
			
		
	}
	
	/**
	 * update user preference, this preference available on pressing + button on the menu bar
	 * @param key
	 * @param val
	 * @return
	 * @throws Exception
	 */
	public String checkUserPermissionNew(String application) throws Exception {
		
			String access = "---";	
			SchedulerDB sdb=SchedulerDB.getSchedulerDB();
			InfrastructureDB infrastructureDB = InfrastructureDB.getInfrastructureDB();
			try{
				sdb.connectDB();
				UserThemeAccessPermission au = getAuthenticatedUserObj(sdb);
				
				infrastructureDB.connectDB();
   
				List al = infrastructureDB.getAccessByAppAndUsername(application, au.toString());
				for (int i=0; i<al.size(); i++) {
					access = al.get(i) == null ? null : al.get(i).toString();
		          }
				
			}catch(Exception e){
				e.printStackTrace();
				throw e;
			}
			finally{
				sdb.closeDB();
				infrastructureDB.closeDB();
			}
			return access;
		
	}


}



