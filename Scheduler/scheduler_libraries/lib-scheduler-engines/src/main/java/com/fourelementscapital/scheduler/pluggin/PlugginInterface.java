/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.pluggin;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public interface PlugginInterface {
	
	public int addAction(Map data, HttpServletRequest request) throws Exception ;	 
	public int updateAction(int scheduler_id, Map data,HttpServletRequest request) throws Exception ;
	public Map fetchData(int record_id, HttpServletRequest request) throws Exception ;
	public void deleteAction(int scheduler_id, HttpServletRequest request) throws Exception ;
	//public void upadteAction(int recordid, Map data,HttpServletRequest request) throws Exception;
	//public boolean deleteAction(int recordid,HttpServletRequest request) throws Exception;
	public PlugginData getPlugginData();
	public String getText(Map data);
	


}


