/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fe.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Get RFunction Manual from Wiki
 */
public class WikiRFunctionManual {
	
	private Logger log = LogManager.getLogger(WikiRFunctionManual.class.getName());
	
	private String function;
	private static  HttpClient httpclient = new HttpClient();
	
	//public WikiRFunctionManual(String function){
		//this.function=function;
	//}
	
	/**
	 * Constructor
	 */	
	public WikiRFunctionManual( ){
	}
	
	/**
	 * Get Wiki HTML (old)
	 * @param selector selector
	 * @return Wiki HTML
	 */		
	public String getWikiHTML_old(String selector) throws Exception {
		try{
			 
			httpclient.getParams().setCookiePolicy(CookiePolicy.RFC_2109);			   
	        HttpState initialState = new HttpState();	      
	        initialState.addCookie(new Cookie(".4ecap.com", "mycookie", "stuff", "/", null, false));
	        initialState.addCookie(new Cookie(".4ecap.com", "4ecapwikidbUserName", "Rams", "/", null, false));
	       // initialState.addCookie(new Cookie(".4ecap.com", "4ecapwikidb_session", "", "/", null, false));
	        //initialState.addCookie(new Cookie(".4ecap.com", "4ecapwikidbLoggedOut", "", "/", null, false));
	        initialState.addCookie(new Cookie(".4ecap.com", "4ecapwikidbUserID", "4", "/", null, false));
	        	        
	        httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(30000);
	        httpclient.setState(initialState);
	            
	        
			GetMethod httpget = new GetMethod("http://wiki.4ecap.com/4ECwiki/"+this.function);
			
		    int respo = httpclient.executeMethod(httpget);
		    //saveCookie(httpget); 	        
		    Document doc =Jsoup.parse( httpget.getResponseBodyAsString());
		    httpget.releaseConnection();
		    saveCookie(httpget);
		    if(doc.select("h1.firstHeading").text().trim().equals("Login Required")){
		    	 
		    	log.debug("~~~ logging in ~~~~~~~~");
		    	 //PostMethod method = new PostMethod("http://wiki.4ecap.com/4ecapwiki/index.php?title=Special:UserLogin&action=submitlogin&type=login");
		    	 PostMethod method = new PostMethod("http://wiki.4ecap.com/4ecapwiki/index.php?title=Special:UserLogin&wpCookieCheck=login");		    	
		    	 method.getParams().setCookiePolicy(CookiePolicy.RFC_2109);
		    	 method.addParameter("wpName", "rams"); 
		    	 method.addParameter("wpPassword", "FECrams");
		    	 
		    	 int returnCode = httpclient.executeMethod(method);
		    	 System.out.println("!!!!!!!!!!:"+method.getResponseBodyAsString());
		    	 
                 
		    	 //Header[] h=method.getResponseHeaders();
                 //for(int i=0;i<h.length;i++){
                //	 System.out.println("name:"+h[i].getName()+"  value:"+h[i].getValue());                	 
                 //}
             
                 
                 //System.out.println("!!!!!!!!!!:"+method.getResponseBodyAsString());
		    	 
		    	 //if(returnCode==HttpStatus.SC_ACCEPTED){
		    		 saveCookie(method);
		    		 respo = httpclient.executeMethod(httpget);		    		 
		    		 doc =Jsoup.parse( httpget.getResponseBodyAsString());		    		 
		    	 //}
		    	// System.out.println(":"+method.getResponseBodyAsString());
		    	 
		         method.releaseConnection();
		    }else{
		    	log.debug("~~~ already logged in~~~~~~~~");
		    }
		    //System.out.println("page"+content);		    
	        //HttpEntity entity = response.getEntity();
		    //System.out.println("content:"+doc.select("body"));
		    return doc.select(selector).toString() ;
	        
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}
	
	
	/**
	 * Get Wiki HTML
	 * @param wikiUsername wiki username
	 * @param wikiPassword wiki password
	 * @param wikiUrl wiki url
	 * @param function_name function name
	 * @return Wiki HTML
	 */			
	public String getWikiHTML(String wikiUsername, String wikiPassword, String wikiUrl, String function_name) throws Exception {
		try{
			 
		    String rtn=null;
		    
			//String username=Config.getString("wiki.username");
			//String password=Config.getString("wiki.password");
			//String wikiurl=Config.getString("wiki.wikiurl");
			
			String username=wikiUsername;
			String password=wikiPassword;
			String wikiurl=wikiUrl;
			
			String url = wikiurl+"api.php?action=login&lgname="+username+"&lgpassword="+password+"&format=xml";
			//System.out.println(">>> url : " + url);
			
			
		  	//PostMethod method = new PostMethod("http://wiki.4ecap.com/4ecapwiki/api.php?action=login&lgname=Rams&lgpassword=FECrams");		    	
		  	PostMethod method = new PostMethod(url);
	
			int returnCode = httpclient.executeMethod(method);
		 
		    if(returnCode==HttpStatus.SC_OK){		    	
		    
		   
                 
                 //String st=getToken(method,"4ecapwikidbToken");                 
		    	
		    	 String st=getToken(method.getResponseBodyAsString());
		    	 
                 log.debug("output:"+st);
                 
                 PostMethod method1 = new PostMethod(wikiurl+"api.php?action=login&lgname="+username+"&lgpassword="+password+"&lgtoken="+st);
               
                 returnCode = httpclient.executeMethod(method1);
                 method1.releaseConnection();
                 if(returnCode ==HttpStatus.SC_OK){
                	 PostMethod method2 = new PostMethod(wikiurl+"index.php?action=render&title="+function_name);
                     returnCode = httpclient.executeMethod(method2);
                     rtn=method2.getResponseBodyAsString();
                     method2.releaseConnection();
                 }
      
		    }
		    method.releaseConnection();
			return rtn;
			
		}catch(Exception e){
			//e.printStackTrace();
			throw e;
		}
		
		
	}
	
	
	/**
	 * new wiki api pushes the token in body, earlier it used to be in header as cookie  
	 * @param response
	 * @return token
	 */
	private String getToken(String response){
		//Pattern p=Pattern.compile("^(.*?token=\")(.*?)(\".*?)$");
		Pattern p=Pattern.compile("(token=\")(.*?)(\")");		//simplified
		Matcher matcher = p.matcher(response);
		String token=null;
		if(matcher.find() && matcher.groupCount()>2){
			token=matcher.group(2);
		}
		return token;
	}
	
	
	/**
	 * Get token  
	 * @param method method
	 * @param tokenname token name
	 * @return token
	 */	
	private String getToken2 (HttpMethod method, String tokenname) {
		 
        Header[] h=method.getResponseHeaders();
        
        String rtn="";
        
        for(int i=0;i<h.length;i++){
		        String headerName = h[i].getName();
		        String headerValue = h[i].getValue();
		        
		        log.debug("header:"+headerName+" headerValue:"+headerValue);
		        

		        if (headerName == null && headerValue == null) {
		            // No more headers
		            //break;
		        }
		        if ("Set-Cookie".equalsIgnoreCase(headerName)) {
		            // Parse cookie
		            String[] fields = headerValue.split(";\\s*");

		            String cookieValue = fields[0];
		            String[] cookieValues=cookieValue.split("=");
		            String expires = null;
		            String path = null;
		            String domain = null;
		            boolean secure = false;

		            // Parse each field
		            for (int j=1; j<fields.length; j++) {
		                if ("secure".equalsIgnoreCase(fields[j])) {
		                    secure = true;
		                } else if (fields[j].indexOf('=') > 0) {
		                	
		                    String[] f = fields[j].split("=");
		                    if ("expires".equalsIgnoreCase(f[0])) {
		                        expires = f[1];
		                    } else if ("domain".equalsIgnoreCase(f[0])) {
		                        domain = f[1];
		                    } else if ("path".equalsIgnoreCase(f[0])) {
		                        path = f[1];
		                    }
		                }
		            }
		            
		            
		            //if(cookieValues.length==2 && cookieValues[0].equals("4ecapwikidbToken") ){
		            if(cookieValues.length==2 && cookieValues[0].equals(tokenname) ){
		            	//httpclient.getState().addCookie(new Cookie(".4ecap.com", cookieValues[0], cookieValues[1], "/", null, false) );
		            	rtn=cookieValues[1];
		            }
		            // Save the cookie...
		        }
		    }
        
           return rtn;
		 
	}
	
	/**
	 * Save cookie
	 * @param method method
	 */		
	private void saveCookie(HttpMethod method) {
		 
        Header[] h=method.getResponseHeaders();
        
        
        
        for(int i=0;i<h.length;i++){
		        String headerName = h[i].getName();
		        String headerValue = h[i].getValue();

		        if (headerName == null && headerValue == null) {
		            // No more headers
		            //break;
		        }
		        if ("Set-Cookie".equalsIgnoreCase(headerName)) {
		            // Parse cookie
		            String[] fields = headerValue.split(";\\s*");

		            String cookieValue = fields[0];
		            String[] cookieValues=cookieValue.split("=");
		            String expires = null;
		            String path = null;
		            String domain = null;
		            boolean secure = false;

		            // Parse each field
		            for (int j=1; j<fields.length; j++) {
		                if ("secure".equalsIgnoreCase(fields[j])) {
		                    secure = true;
		                } else if (fields[j].indexOf('=') > 0) {
		                	
		                    String[] f = fields[j].split("=");
		                    if ("expires".equalsIgnoreCase(f[0])) {
		                        expires = f[1];
		                    } else if ("domain".equalsIgnoreCase(f[0])) {
		                        domain = f[1];
		                    } else if ("path".equalsIgnoreCase(f[0])) {
		                        path = f[1];
		                    }
		                }
		            }
		            if(cookieValues.length==2){
		            	httpclient.getState().addCookie(new Cookie(".4ecap.com", cookieValues[0], cookieValues[1], "/", null, false) );
		            }
		            // Save the cookie...
		        }
		    }
        
        Cookie[] cookies = httpclient.getState().getCookies();
        for (int i = 0; i < cookies.length; i++) {
          Cookie cookie = cookies[i];
          System.err.println(
            "Cookie: " + cookie.getName() +
            ", Value: " + cookie.getValue() +
            //", IsPersistent?: " + cookie.isPersistent() +
            ", Expiry Date: " + cookie.getExpiryDate() +
            //", Comment: " + cookie.getComment()+
            ", path: " + cookie.getPath()+
            ", domain: " + cookie.getDomain());

          //cookie.setValue("My own value");
        }
		 
	}	

}


