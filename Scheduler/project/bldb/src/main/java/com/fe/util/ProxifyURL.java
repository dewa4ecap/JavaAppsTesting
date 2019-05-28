/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fe.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fourelementscapital.scheduler.error.ClientError;

public class ProxifyURL {

	
	private static  HttpClient httpclient = new HttpClient();
	private HttpServletRequest request;
	private HttpServletResponse response;
	private Logger log = LogManager.getLogger(ProxifyURL.class.getName());
	
	public ProxifyURL(HttpServletRequest request, HttpServletResponse response){
		this.request=request;
		this.response=response;
	}
	
	public boolean isProxified(String url_prefix){
		String queryString=this.request.getQueryString();		
		String tpf=this.request.getRequestURL().toString().replace(queryString,"");
	    log.debug("request url p:"+tpf);
	    return !tpf.equalsIgnoreCase(url_prefix);
	}
	
	public void proxifyGet(String url_prefix,String overwrite_qs)  {	
		try{
			
			String queryString=overwrite_qs==null?this.request.getQueryString():overwrite_qs;		
			String redirecting=url_prefix+"?"+queryString;
			log.debug("redirecting to:"+redirecting);
			GetMethod httpget = new GetMethod(redirecting);
			
		    int respo = httpclient.executeMethod(httpget);
		    //saveCookie(httpget);
		    this.response.getOutputStream().write(httpget.getResponseBody());
		    httpget.releaseConnection();
		    
		}catch(Exception e){
			ClientError.reportError(e, null);
		}
	}
}


