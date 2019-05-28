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
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Random;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionException;
import org.rosuda.JRI.REXP;

import com.fourelementscapital.scheduler.config.Config;
import com.fourelementscapital.scheduler.exception.ExceptionPeerUnknown;
import com.fourelementscapital.scheduler.exception.ExceptionRScriptNullError;
import com.fourelementscapital.scheduler.exception.ExceptionRscriptError;
import com.fourelementscapital.scheduler.exception.SchedulerException;
import com.fourelementscapital.scheduler.p2p.P2PService;

public abstract class REngineWindows extends AbstractRScript {

	
	private Logger log = LogManager.getLogger(REngineWindows.class.getName());
	
	private StackFrame stackframe=null;
	
	protected static MyRengine re=null;
	
	private static String rVersion=null;
	private static boolean jriCompatible=false;
	private static String packageVersions=null; 
	
	
	public REngineWindows(String name,String taskuid ) {		 
		super(name,taskuid);		 
	}
	
 
	public synchronized void execute(StackFrame sframe) throws  JobExecutionException,Exception 	  {
		
		log.debug("RScriptScheduledTask.execute() called");
		this.stackframe=sframe;
		Map<String, Object> data=sframe.getData();

		Number nid=(Number)data.get("id");
		
		String rscript=(String)data.get("rscript");
		log.debug("before starting R script log id:"+sframe.getLogid());
		log.debug("~~~~~~~~~~R Script started excecuting...");
		if(rscript==null) { throw new JobExecutionException("Task Failed because no R script found or empty");  }
		MyRengineCallbacks myrenging=new MyRengineCallbacks();
		 
		if(re==null){
			 
			re=new MyRengine(null, false, myrenging );
			re.setStackFrame(sframe);
 
		}else{ 
			
			re.setStackFrame(sframe);
			//re.addMainLoopCallbacks(myrenging);
		}
		re.getRMailLoopCallback().setConsoleOutput(null);
		
		log.debug("R Engine initiated.");
		if(rscript!=null && !rscript.equals("")){
			
			File temp=null;
			
			//bug fix that sink close has been put in finally method/
			boolean sinked=false;
			try{
					 
				 	BufferedReader reader1 = new BufferedReader(new FileReader(Config.getString("r_script_init")));
				    String line;
				    
				   
				    
				    
			    	re.assign(".trigger_time", sframe.getTrigger_time()+"");
				    re.assign(".scheduler_id", nid+"");
				    re.assign(".machine", P2PService.getComputerName());
				    re.assign(".enginetype","REngine");
				    re.assign(".connection_ids","");
				    
				 
				    
				    while ((line = reader1.readLine()) != null)  {
				    	if(line!=null && !line.equals("")){				    		 
				    		REXP x=re.eval(line);
					    }
				    }
				    reader1.close();
				    log.debug("R init source file initiated.");
 
					BufferedReader reader = new BufferedReader(new StringReader(rscript));
					
				     
   			        StringBuffer sb=new StringBuffer();
				     
				    String tempDir = System.getProperty("java.io.tmpdir");
				    
				    File file=new File(tempDir+File.separator+"rscript"+new SimpleDateFormat("mmddyyy_hh_mmss_").format(new Date())+new Random().nextInt()+".R");
				    
				    BufferedWriter out = new BufferedWriter(new FileWriter(file));

				    while ((line = reader.readLine()) != null)
				    {
				    	 
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
				    
				    
				    //sink staring here....
				    temp = File.createTempFile(nid.intValue()+"_"+sframe.getTrigger_time(),".log");
					String ff=temp.getPath();
					if(ff.contains("\\")){
						ff=ff.replaceAll("\\\\", "\\\\\\\\");
					}								
					temp.delete();
					log.debug("---- deleting ff:"+ff);
				    String fileass="file(\""+ff+"\", open = \"wt\");";
				    log.debug("file assign------fileass:"+fileass);
				    re.eval(".console_msg_zz<-"+fileass);
				    re.eval("sink(.console_msg_zz)");
				    re.eval("sink(.console_msg_zz, type = \"message\")") ;
				    log.debug("----sink----");				    
				    sinked=true;
				    
				    
				    /*
				    String con_out=Config.getString("r_script_console_logs");
				    if(con_out!=null && new File(con_out).isDirectory()){		
				    	File file1=new File(con_out+nid+"_"+sframe.getTrigger_time()+".log");
				    	re.getRMailLoopCallback().setConsoleOutput(file1);
	 
				    }
					
					*/
				    
				    new SchedulerExePlanLogs(nid.intValue(),sframe.getTrigger_time()).log("REngine Script evaluation starting..",SchedulerExePlanLogs.PEER_OK_RENGINE_SCRIPT_EVAL_STARTING);
				    
				    REXP x1= re.eval("source(\""+filepath+"\")");

				    
				    try{
					    REXP genv=null;
					    REXP x5=re.eval("as.character(.connection_ids)");
					    log.debug("~~~~~~~~~~~~~~x5:"+x5.rtype);
					    log.debug("~~~~~~~~~~~~~~x5:asString:"+x5.asStringArray());
					    String ar[]=x5.asStringArray();
					    if(ar!=null){
					    	for(int i=0;i<ar.length;i++){
					    		//log.debug("~~~~~~~~~~~ ar["+i+"]:"+ar[i]);
					    		if(ar[i]!=null && !ar[i].equals("")) {
					    			sframe.getDbConnectionIds().add(ar[i]);
					    		}
					    	}
					    }
				    }catch(Exception e){
				    	log.error("Error:"+e.getMessage());
				    }
				    
				    
				    log.debug("Script from temp file Evaluated");
				    
				    log.debug("x1:"+x1);				    
			    
				    if(!file.delete()){
				    	file.deleteOnExit();
				    }
				    
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
						                 for(int ia=0;ia<i;ia++){
						                	 ind+="-";
						                 }
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
				        	throw new ExceptionRScriptNullError("R script:"+data.get("name")+" failed");
				        }
				    	//System.out.println("x3:"+x3);
				    	//System.out.println("x3.rtype:"+x3.rtype);
				    	
				    	
				    }else{
				    	//sframe.setTasklog(x1.toString());
				    	//executed successfully so don't store log messages 
				    	sframe.setTasklog(null);
				    	re.setStackFrame(null);
				    }
				    
				    //sink ending here....
				    
				  
				    
				    updatePackageVersion();
				    
			}catch(SchedulerException se){
			    	throw se;   
			}catch(Exception e){
				//e.printStackTrace();
				//ClientErrorMgmt.reportError(e,null);
	    		//sframe.setTasklog("RServe is not running on "+P2PService.getComputerName());
	    		throw new ExceptionPeerUnknown("RserveScheduleedTask: Error:"+e.getMessage());   		
			
			} finally{
				
				if(sinked && re!=null){					
					  re.eval("sink(type = \"message\")");
					  re.eval("sink()");
					  re.eval("close(.console_msg_zz)");
				}
				
				if(temp!=null && temp.exists()){
				    FileInputStream inputStream = new FileInputStream(temp);
				    try{
				    	sframe.setConsole_message(IOUtils.toString(inputStream));
				    }catch(Exception e){
				    	log.error("error while accessing the file, Error:"+e.getMessage());
				    }finally{
				    	inputStream.close();
				    }	
		    		temp.delete();					    		
		    	}
				
			} 
		}
		if(re==null){
			re.end();
			re=null;
		}
		//setMessages(myrenging.logMessages());		
		log.debug("~~~~~~~~~~~R Script finishing execution...");
		
	}
	
	
	public static void updatePackageVersion(){
		 //updates R Package version. 
		
		if(Config.getString("p2p.ignorejri")!=null && Config.getString("p2p.ignorejri").equalsIgnoreCase("true")){
			
		}else{
		
			
			if(re==null){
				re=new MyRengine(null, false, new MyRengineCallbacks() );
			}
		    //REXP x=re.eval("necessaryRemote <- as.character(read.table(file='//10.153.64.10/Public/Libs/mandatoryPackages.txt')[,1]);");
			//REXP x=re.eval("necessaryRemote2 <- as.character(read.table(file='//10.153.64.10/Public/Libs/mandatoryExternalPackages.txt')[,1]);");
			REXP x=re.eval("necessaryRemote2 <- c(as.character(read.table(file='//10.153.64.10/Public/Libs/mandatoryExternalPackages.txt')[,1]),as.character(read.table(file='//10.153.64.10/Public/Libs/mandatory4ecapPackages.txt')[,1]))");
			
			x=re.eval("IP = installed.packages(); ");
			x=re.eval("paste(as.character(IP[, 'Package'])[as.character(IP[, 'Package']) %in% necessaryRemote2], as.character(IP[, 'Version'])[as.character(IP[, 'Package']) %in% necessaryRemote2], sep=\"=\");");
			
			if(x!=null){
				String st1[]=x.asStringArray();
				 
				String result="PEER="+P2PService.getComputerName();
				for(int i=0;i<st1.length;i++){						 
					result+="|"+st1[i];
				}

				packageVersions=result;
			}
		}
	}
	
	 
	
}


