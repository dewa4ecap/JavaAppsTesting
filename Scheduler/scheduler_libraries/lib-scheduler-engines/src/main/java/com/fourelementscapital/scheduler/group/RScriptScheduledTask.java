/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.group;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionException;
import org.rosuda.JRI.REXP;

import com.fourelementscapital.scheduler.config.Config;
import com.fourelementscapital.scheduler.engines.MyRengine;
import com.fourelementscapital.scheduler.engines.MyRengineCallbacks;
import com.fourelementscapital.scheduler.engines.REngineWindows;
import com.fourelementscapital.scheduler.engines.StackFrame;

public class RScriptScheduledTask extends REngineWindows {
	
	protected static MyRengine re=null;
	
	private Logger log = LogManager.getLogger(RScriptScheduledTask.class.getName());
	
	private StackFrame stackframe=null;
	
	public RScriptScheduledTask(String name, String taskuid) {
		
		//super("High Priority (R Engine)","rscript");		 
		super(name,taskuid);
		 
	}
	
	
	private static String rVersion=null;
	private static boolean jriCompatible=false;
	private static String packageVersions=null; 
	
	
	
	/**
	 * @deprecated
	 */
	public static String getRVersion(){
		if(rVersion==null){
			//getRVersionFirstTime();
		}
		return rVersion;
	}
	
	/**
	 * @deprecated
	 */
	public static String getRPackageVersion(){
		if(packageVersions==null){
			//updatePackageVersion();
		}
		return packageVersions;
	}
	
	/**
	 * @deprecated
	 */
	public static boolean getJRICompatible(){
		if(rVersion==null){
			//getRVersionFirstTime();
		}
		return jriCompatible;
	}
	
 
	/**
	 * @deprecated
	 */
	public static void updatePackageVersion(){
		 //updates R Package version. 
		 
	}


	/**
	 * @deprecated
	 */
	public void executeScript(StackFrame sframe) throws  JobExecutionException,Exception 	  {
		
		if(Config.getString("p2p.ignorejri")!=null && Config.getString("p2p.ignorejri").equalsIgnoreCase("true")){
			throw new Exception("JRI is not enalbed");
		}
		
		//log.debug("RScriptScheduledTask.execute() called");
		this.stackframe=sframe;
		Map<String, Object> data=sframe.getData();
		String rscript=(String)data.get("script");
		Number restart=(Number)data.get("restart");
		Number script_id=(Number)data.get("script_id");
		
		if(rscript==null) { throw new JobExecutionException("Task Failed because no R script found or empty");  }		
		MyRengineCallbacks myrenging=new MyRengineCallbacks();		

		if(re!=null && restart.intValue()==1){			
			re.end();
			
			//log.debug("restarting R session....");
			//re.interrupt();
			
			log.debug("interupting R thread....");
			
			re=null;
			try{
				Thread.sleep(2000);
			}catch(Exception e){	}
			
		}
	
		if(re==null){			 
			log.debug("Rengine is null, creating one.......");
			re=new MyRengine(null, false, myrenging );
			re.setStackFrame(sframe);
		}else{ 
			re.setStackFrame(sframe);
			//re.addMainLoopCallbacks(myrenging);
		}
		
		if(rscript!=null && !rscript.equals("")){	 
			try{
					 
	 
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
				    REXP x1= re.eval("source(\""+filepath+"\")");
				    
				    log.debug("x1:"+x1);
				   
				    file.delete();
				    if(x1==null) {
				    	//sframe.setTasklog("R Script failed. See  log file for more info");
				    	throw new Exception("R script:"+data.get("name")+" failed");
				    }else{
				    	sframe.setTasklog(null);
				    	re.setStackFrame(null);
				    }
			}catch(Exception e){
				//e.printStackTrace();
				//ClientErrorMgmt.reportError(e,null);
				throw e;
			}
		}
		 
		//setMessages(myrenging.logMessages());		
		log.debug("~~~~~~~~~~~R Script finishing execution...");
		
	}
	
	
	 
	

	 
	
	public static String codeInjectConcatenate(String default_param, String rscript, String injected_code) throws Exception {
		
		String finalcode="";
		if(default_param!=null && !default_param.equals("")) {
			finalcode+="## Default Header Code ##\n";
			finalcode+=default_param+"\n";
			finalcode+="## --------------------## \n";
		}

		if(injected_code!=null && !injected_code.equals("")) {
			finalcode+="\n\n## Injected Code ##\n";
			finalcode+=injected_code+"\n";
			finalcode+="## --------------## \n";
		}

		
		if(rscript!=null && !rscript.equals("")) {			 
			finalcode+=rscript+"\n";			 
		}
		return finalcode;
		
	}
	
}


