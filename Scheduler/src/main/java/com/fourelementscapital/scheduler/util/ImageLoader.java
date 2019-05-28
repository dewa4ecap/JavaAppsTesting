/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.util;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Image Loader
 */	
public class ImageLoader {
	private HttpServletRequest request=null;
	private HttpServletResponse response=null;
	
	/**
	 * Constructor
	 * @param request request
	 * @param response response
	 */			
	public ImageLoader(HttpServletRequest request, HttpServletResponse response) {
		this.request=request;
		this.response=response;
	}
	
	/**
	 * Push Image
	 * @throws Exception
	 */		
	public void pushImage() throws Exception {
		try {
			response.setHeader("Content-Disposition", "inline");
			FileInputStream fin=null;
			if(this.request.getParameter("file")!=null && !this.request.getParameter("file").equals("")){
				fin = new FileInputStream(this.request.getParameter("file"));
				if(this.request.getParameter("file").toLowerCase().endsWith(".svg")){
					response.setContentType("image/svg+xml");
				}else{
					response.setContentType("image/png");
				}
			}else{
				fin = new FileInputStream("/home/fileserv/Sharing/Public/Research/Monitors/SchedulerActivityMonitor.png");
			 	response.setContentType("image/png");
			}
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] buf = new byte[1024];
			int len;
			while ((len = fin.read(buf)) > 0) {
				// instead of writing to a ByteArrayOutputStream you can
				// write to the FileOutputStream here (see the comments later
				// after creating the byte[] variable called data
				bos.write(buf, 0, len);
			}
			byte[] data = bos.toByteArray();
			OutputStream out = response.getOutputStream();
			out.write(data);
			out.flush();
			fin.close();
		} catch (Exception e) {
			//e.printStackTrace();
			response.setContentType("html/text");
			response.setStatus(500);
		}
	}
}