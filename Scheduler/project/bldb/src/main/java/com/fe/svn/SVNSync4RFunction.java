/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fe.svn;

import com.fourelementscapital.scheduler.config.Config;

public class SVNSync4RFunction extends SVNSyncFile{
	 
	
	public SVNSync4RFunction(String user, String pwd){
		super(user,pwd,Config.getString("svn_url_r"),Config.getString("svn_local_path_r"));
		 	 
		
	}
	 
	public SVNSync4RFunction(){
		
		super(Config.getString("svn_url_r"),Config.getString("svn_local_path_r"));
	}

	
	public String getExtension() {
		 
		return ".r";
	}
	
 
}


