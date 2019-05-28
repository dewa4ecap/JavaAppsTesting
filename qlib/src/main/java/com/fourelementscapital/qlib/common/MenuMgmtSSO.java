/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.qlib.common;

import java.util.Map;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import com.fourelementscapital.db.SchedulerDB;
import com.fourelementscapital.client.Authenticated;

/**
 * This class provides data for building menu on the web page.
 *  
 */
public class MenuMgmtSSO {
	
	private String user;

	/**
	 * default constructor
	 * @throws Exception
	 */
	public MenuMgmtSSO() throws Exception {}

	/**
	 * constructor for jsp 
	 * @param request
	 * @throws Exception
	 */
	public MenuMgmtSSO(HttpServletRequest request, String username) throws Exception { this.user = username; }
	
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
	public Map<String,String> getUserPref() throws Exception {		
		
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		try{
			sdb.connectDB();
			Map<String,String> data= new HashMap<String,String>();
			data = sdb.userPrefGetAll(user);
			 
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