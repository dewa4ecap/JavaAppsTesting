/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.engines;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.Rserve.RConnection;

import com.fourelementscapital.scheduler.exception.ExceptionRScriptNullError;
import com.fourelementscapital.scheduler.exception.ExceptionRServeWindowsFailure;
import com.fourelementscapital.scheduler.exception.ExceptionRscriptError;
import com.fourelementscapital.scheduler.rserve.RServeSession;



public abstract class RServeWindows  extends AbstractRServe{

	 
	//private RConnection c=null; 
	private Process p=null;
	
	private Logger log = LogManager.getLogger(RServeWindows.class.getName());
	
	
	public RServeWindows(String name, String uid) {
		super(name, uid);
	}
	
	public RServeSession getRServeSession(int nid, String name) throws Exception {

		RServeSession rs=null;
		this.p=Runtime.getRuntime().exec("c:\\rJava\\rserve1.bat");
    	int count=0;
    	TreeMap error=new TreeMap();
    	RConnection c=null;
		while(c==null && count<20) {							
			try{c=new RConnection();}catch(Exception e){
				//errorMessage+=""
				String msg=e.getMessage();
				if(error.keySet().contains(msg)){
					int cnt=(Integer)error.get(msg);
					 cnt++;error.put(msg, cnt);
				}else{
					error.put(msg, 1);
				}
			}
			try{ Thread.sleep(500);	}catch(Exception e){}
			count++;
		}
		if(c==null){		 
	    	String msg="";
	    	for(Iterator i=error.keySet().iterator();i.hasNext();){
	    		String ky=(String)i.next();
	    		int cnt=(Integer)error.get(ky);
	    		msg+=ky+ ((cnt>1)?"("+cnt+")":"");
	    	}
	    	throw new ExceptionRServeWindowsFailure("Couldn't start RServ Connection! Error MSG:"+msg);
		 
		}else{
			rs=new RServeSession();
			rs.setRconnection(c);
		}
		return rs;
		
	}

	 
	public void evalScript(RServeSession rs,String rscript) throws Exception {
		
		  RConnection c=rs.getRconnection();
		  String tempDir = System.getProperty("java.io.tmpdir");
		  File file=new File(tempDir+File.separator+"rscript"+new SimpleDateFormat("mmddyyy_hh_mmss_").format(new Date())+new Random().nextInt()+".R");				    
		  BufferedWriter out = new BufferedWriter(new FileWriter(file));
		  
		  BufferedReader reader = new BufferedReader(new StringReader(rscript));
		  
		   String line;
		   
		  while ((line = reader.readLine()) != null)   {
		    	if(line!=null && !line.equals("")){
		    		//System.out.println("--------->line:"+line);
		    		//REXP x1=  re.eval(line);						 
		    		//System.out.println("Content"+x1.toString());
		    		out.write(line+"\n");
		    	}
		   }
		    
		   out.close();
		   reader.close();
		   log.debug("R written to temp file :"+file.getPath());
		    //System.out.println("RScriptScheduledTask.execute() file:"+file.getPath());	
		   String filepath=file.getPath().replaceAll("\\\\", "\\\\\\\\");
		
		   
	        REXP x1= c.parseAndEval("try(source(\""+filepath+"\"),silent=TRUE)");						   
		    
		    log.debug("Script from temp file Evaluated");
		    log.debug("x1:"+x1);				    
		    log.debug("error messages:"+c.getLastError());
	
		    if (x1!=null && x1.inherits("try-error") ) {					    	
		        //throw new Exception("Error in R Script: "+x1.asString());
		    	throw new ExceptionRscriptError(x1.asString());
		    }
		    
		    if(x1==null) {
		    	//sframe.setTasklog("R Script failed. See  log file for more info");
		    	//throw new Exception("R script:"+data.get("name")+" failed");
		    	throw new ExceptionRScriptNullError("Rscript Executed but unknown error message returned by RServe");						    	
		    }else{						    	
		    	this.stackframe.setTasklog(null);
		    }		
		    
		
	}

 
	public void closeRconnection(RServeSession rs) throws Exception {
		RConnection c=rs.getRconnection();
		if(c!=null){
			c.shutdown();  
			if(this.p!=null){
				p.destroy();
	    	
			}
		}
	}

	
	
}


