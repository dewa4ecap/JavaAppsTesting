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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionException;
import org.rosuda.JRI.REXP;

import com.fourelementscapital.scheduler.config.Config;
import com.fourelementscapital.scheduler.exception.ExceptionPeerUnknown;
import com.fourelementscapital.scheduler.exception.ExceptionRscriptError;
import com.fourelementscapital.scheduler.exception.SchedulerException;
import com.fourelementscapital.scheduler.p2p.P2PService;

public abstract class ExecuteRWindows extends ScheduledTask {

	
	
	public ExecuteRWindows(String name,String taskuid ) {
		//super("Medium Priority (RServe)","rscript4rserve");
		super(name,taskuid);
		 
	}
		
	private Logger log = LogManager.getLogger(ExecuteRWindows.class.getName());
	
	
	protected static MyRengine re=null;
	
	
	public synchronized void execute(StackFrame sframe) throws  JobExecutionException,Exception 	  {
		log.debug("execute() called");
		
		if(Config.getString("p2p.ignorejri")!=null && Config.getString("p2p.ignorejri").equalsIgnoreCase("true")){
			throw new Exception("JRI is not enalbed");
		}
		
 
		String rscript=sframe.getRscript().getScript();
		
		if(rscript==null) { throw new JobExecutionException("Task Failed because no R script found or empty");  }		
		MyRengineCallbacks myrenging=new MyRengineCallbacks(); //new MyRengineCallbacks();		
        
	
		if(re==null){			 
			log.debug("Rengine is null, creating one.......");
			re=new MyRengine(null, false, myrenging );
			re.setStackFrame(sframe);
			
			
			//for the first time it loads Rinit;
		 	BufferedReader reader1 = new BufferedReader(new FileReader(Config.getString("r_script_init")));
		    String line;		    
		    while ((line = reader1.readLine()) != null)  {
		    	if(line!=null && !line.equals("")){				    		 
		    		REXP x=re.eval(line);
			    }
		    }
		    reader1.close();
		    
		    
		}else{ 
			re.setStackFrame(sframe);
			//re.addMainLoopCallbacks(myrenging);
		}
		
		re.getRMailLoopCallback().setConsoleOutput(null);
		
		if(rscript!=null && !rscript.equals("")){	 
			try{
					
				 	re.assign(".machine", P2PService.getComputerName());
				 	
					sframe.setStarted_time(new Date().getTime());
					
				    log.debug("before executing script....");
					BufferedReader reader = new BufferedReader(new StringReader(rscript));
				    
				    StringBuffer sb=new StringBuffer();
				     
				    String tempDir = System.getProperty("java.io.tmpdir");
				    File file=new File(tempDir+File.separator+"rscript"+new SimpleDateFormat("mmddyyy_hh_mmss_").format(new Date())+new Random().nextInt()+".R");
				    String line;
				    BufferedWriter out = new BufferedWriter(new FileWriter(file));
				    
				    while ((line = reader.readLine()) != null)   {				    	 
				    	if(line!=null && !line.equals("")){				    		 
				    		out.write(line+"\n");
				    	}
				    }
				    out.close();
				    reader.close();				    
	
				    
				    String filepath=file.getPath().replaceAll("\\\\", "\\\\\\\\");
				    log.debug("file:"+file.getPath()+" re:"+re);
				    REXP x1= re.eval("source(\""+filepath+"\")");
				    sframe.getRscript().setResult(x1);
				    log.debug("x1:"+x1);				    
				    file.delete();
				    
				    
				    if(x1==null) {
				    	//sframe.setTasklog("R Script failed. See  log file for more info");
				    	REXP x3= re.eval("as.character(.Traceback)");
				        if(x3.rtype==REXP.STRSXP){
				        	String a[]=x3.asStringArray();
				        	String err="\n----Error stack----";
				        	if(a.length>=2){
					        	for(int i=a.length-2;i>=0;i--){
					        		if(!a[i].startsWith("eval.with.vis")){
						        		 String ind="\n-";
						                 ind+=">"+a[i];
						                 err+=ind;
					        		}
					        	}
				        	}
				        	myrenging.externalLog(re, err);
				        	String all_err="";
				        	if(sframe.getTasklog()!=null){
				        		all_err=err+"\n"+sframe.getTasklog();
				        	}else{
				        		all_err=err;
				        	}
				        	throw new ExceptionRscriptError("R Script Error:"+all_err);
				        	
				        }else{
				        	throw new ExceptionRscriptError("R script:"+rscript+" failed, "+sframe.getTasklog());
				        }				    	
				    }else{
				    	
				    	//sframe.setTasklog(x1.toString());
				    	//executed successfully so don't store log messages
				    	
				    	sframe.setTasklog(null);
				    	re.setStackFrame(null);
				    }
				    
 
			}catch(SchedulerException se){
		    	throw se; 
			}catch(Exception e){
				
				throw new ExceptionPeerUnknown("REngineScriptTask: Error:"+e.getMessage());   		
			}
		}
		 
		//setMessages(myrenging.logMessages());		
		log.debug("~~~~~~~~~~~R Script finishing execution...");
		
	}
}


