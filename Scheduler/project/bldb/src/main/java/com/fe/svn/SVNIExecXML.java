/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fe.svn;

import com.fourelementscapital.scheduler.config.Config;

public class SVNIExecXML extends SVNSyncFile{
	
	public SVNIExecXML(String user, String pwd){
		super(user,pwd,Config.getString("svn_url_ie"),Config.getString("svn_local_path_ie"));
		 	 
		
	}
	 
	public SVNIExecXML(){
		
		super(Config.getString("svn_url_ie"),Config.getString("svn_local_path_ie"));
	}

	
	public String getExtension() {
		 
		return ".xml";
	}
}


