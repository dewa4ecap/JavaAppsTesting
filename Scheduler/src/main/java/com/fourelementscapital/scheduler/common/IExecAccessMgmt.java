/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.common;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fourelementscapital.auth.UserThemeAccessPermission;
import com.fourelementscapital.db.InfrastructureDB;
import com.fourelementscapital.db.SchedulerDB;
import com.fourelementscapital.client.Authenticated;

@SuppressWarnings("unchecked")
public class IExecAccessMgmt extends Authenticated {
	private Logger log = LogManager.getLogger(IExecAccessMgmt.class.getName());

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
	 * @param application
	 * @return access
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
				log.error("==============================================");
				log.error("Please check :");
				log.error("*. config_db.properties");
				log.error("*. .4E.config file");
				log.error("*. bbsync and infrastructure database in MySQL");
				log.error("==============================================");
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