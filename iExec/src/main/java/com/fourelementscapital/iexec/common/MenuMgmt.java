/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.iexec.common;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.fourelementscapital.db.SchedulerDB;
import com.fourelementscapital.client.Authenticated;

/**
 * This class provides data for building menu on the web page.
 *  
 */
public class MenuMgmt extends Authenticated {

	/**
	 * default constructor
	 * @throws Exception
	 */
	public MenuMgmt() throws Exception {
		super();		 
	}

	/**
	 * constructor for jsp 
	 * @param request
	 * @throws Exception
	 */
	public MenuMgmt(HttpServletRequest request) throws Exception {
		super(request);
	}
	
	
	/**
	 * update user preference, this preference available on pressing + button on the menu bar
	 * @param key
	 * @param val
	 * @return
	 * @throws Exception
	 */
	public boolean updateUserPreference(String key, String val) throws Exception {
		
				
			SchedulerDB sdb=SchedulerDB.getSchedulerDB();
			try{
				String user=getAuthenticatedUser();
				sdb.connectDB();
				sdb.userPrefSaveOrUpdate(user, key, val);
				return true;
			}catch(Exception e){
				e.printStackTrace();
				throw e;
			}
			finally{
				sdb.closeDB();		
			}
			
		
	}

	
	/**
	 * get user preference to set necessary settings in UI
	 * @return
	 * @throws Exception
	 */
	public Map getUserPref() throws Exception {		
		
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		try{
			String user=getAuthenticatedUser();
			sdb.connectDB();
			Map data=sdb.userPrefGetAll(user);
			return data;
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
		finally{
			sdb.closeDB();		
		}
		
	
	}

	
}