/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.iexec.common;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("unchecked")
public class SchedulerMgmt extends AbstractTeamOrgMgmt {
	
	private Logger log = LogManager.getLogger(SchedulerMgmt.class.getName());
	private HttpServletRequest request=null;
	private static String USER="user";

	/**
	 * for DWR invocation
	 * @throws Exception
	 */
	public SchedulerMgmt() throws Exception {
		super();
 
	}
	
	/**
	 * Invocation only when you have HttpRequest is available,
	 * in JSP or internally. 
	 * @param request
	 * @throws Exception
	 */
	public SchedulerMgmt(HttpServletRequest request) throws Exception {
		super(request);
	 
	}
	 
	protected String getPeerIPAddress() throws Exception {		
		String rtn=(String) getRequest().getSession().getAttribute(REMOTE_IP);
		return rtn;
	}
}