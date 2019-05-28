/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fe.servlet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fourelementscapital.db.RFunctionDB;

public class AceModeRSyntax  extends HttpServlet {
public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException { 
		
		
		
		
		String realpath=request.getSession().getServletContext().getRealPath("");		
		//System.out.println("~~~~~~~~~~realpath:"+realpath); 
		//FileInputStream fin=null;
		
		String fullpath=realpath+File.separator+"ace"+File.separator+"src"+File.separator+"mode-r.js";
		response.setContentType("application/javascript");
		BufferedReader in = new BufferedReader(new FileReader(fullpath));
		String rtn="";
	    String str;
	    while ((str = in.readLine()) != null) {
	    	rtn+=str+"\r\n";
	    }
	    
	    RFunctionDB rfdb=RFunctionDB.getRFunctionDB();
	    String functionanames="";
	    try{
	    	rfdb.connectDB();
	    	List fnames=rfdb.listAllRFunctionNames();
	    	for(Iterator i=fnames.iterator();i.hasNext();){
	    		String fname=(String)i.next();
	    		fname=""+fname+"";
	    		functionanames+=functionanames.equals("")?fname: "|"+fname;
	    	}
	    }catch(Exception ex){
	    	ex.printStackTrace();
	    	
	    }finally{
	    	try{
	    	rfdb.closeDB();
	    	}catch(Exception e1){}
	    }
	    
	    //rtn=replacePlaceHolders(rtn,"myfunctions","'getRACEUniverse','getContractReference','abc','eee'");
	    rtn=replacePlaceHolders(rtn,"myfunctions",functionanames);
	    
	    in.close();	    
		OutputStream out = response.getOutputStream();		
		out.write(rtn.getBytes());
		out.flush();
		
	}
	
	
	
   private String replacePlaceHolders(String css,String holder, String value){ 	
		//Map<String, String> data = new HashMap<String, String>();
		//...
		Pattern p = Pattern.compile("\\[\\[([a-zA-Z.]+)\\]\\]");
		Matcher m = p.matcher(css);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
		    String reg1 = m.group(1);
		    if(reg1.equals("myfunctions")){	   
		    	m.appendReplacement(sb, Matcher.quoteReplacement(value == null ? "" : value));
		    }
		}
		m.appendTail(sb);
		return sb.toString();
   }
}


