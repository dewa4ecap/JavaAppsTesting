/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fe.svn;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
 
import java.io.StringReader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SVNDiffPush {

	private HttpServletRequest request=null;
	private HttpServletResponse response=null;
	
	public SVNDiffPush(HttpServletRequest request, HttpServletResponse response) {
		this.request=request;
		this.response=response;
	}
	
	public void pushDiffWC() throws Exception {
		try {

			response.setHeader("Content-Disposition", "attachment; filename=comparison.diff");
			response.setContentType("application/octet-stream");
			 
			
			if(this.request.getParameter("scheduler_id")!=null 
					&& !this.request.getParameter("revision").equals("")					
					
			){
				int scheduler_id=Integer.parseInt(this.request.getParameter("scheduler_id"));
				long revstart=Long.parseLong(this.request.getParameter("revision"));				 
				SVNSync sync=new SVNSync();
				String diff=sync.diffWC(scheduler_id, revstart);
				OutputStream out = response.getOutputStream();
				out.write(diff.getBytes());
				out.flush();
				
				
			}

 
			
			//fin.close();
		} catch (Exception e) {
			e.printStackTrace();
			response.setContentType("html/text");
			response.setStatus(500);
		}
	}
	
}


