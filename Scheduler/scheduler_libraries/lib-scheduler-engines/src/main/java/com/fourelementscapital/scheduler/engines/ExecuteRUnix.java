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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionException;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.RList;
import org.rosuda.REngine.Rserve.RConnection;

import com.fourelementscapital.scheduler.config.Config;
import com.fourelementscapital.scheduler.exception.ExceptionRServeUnixFailure;
import com.fourelementscapital.scheduler.p2p.P2PService;
import com.fourelementscapital.scheduler.rserve.RServeConnectionPool;
import com.fourelementscapital.scheduler.rserve.RServeSession;
//import com.fourelementscapital.client.ClientErrorMgmt;

public abstract class ExecuteRUnix extends ScheduledTask {

	private Logger log = LogManager.getLogger(ExecuteRUnix.class.getName());
	
	private StackFrame stackframe=null;
	
	public ExecuteRUnix(String name,String taskuid ) {
	 
		super(name,taskuid);
		 
		 
	}
	
	 public void execute(StackFrame sframe) throws  JobExecutionException,Exception 	  {
			
			
			this.stackframe=sframe;
			Map<String, Object> data=sframe.getData();
 
			
			if(sframe.getRscript()==null) throw new JobExecutionException("Script failed as StackFrame doesn't containt RScript object");
			String rscript=sframe.getRscript().getScript();
			
			if(rscript==null) { throw new JobExecutionException("Task Failed because no R script found or empty");  }	
			
			log.debug("R Engine initiated.");
			if(rscript!=null && !rscript.equals("")){	 
				try{
						 
					    //System.out.println(" RServeLowPriorityTask.execute(): executed 11");
					    
					    String line;
		 
					    log.debug("R init source file initiated.");
	 
						//BufferedReader reader = new BufferedReader(new StringReader(rscript));
					    
					    String tempDir = System.getProperty("java.io.tmpdir");
					    log.debug("set temporary directory:"+tempDir);
					    File file=new File(tempDir+File.separator+"rscript"+new SimpleDateFormat("mmddyyy_hh_mmss_").format(new Date())+new Random().nextInt()+".R");
					    
					    BufferedWriter out = new BufferedWriter(new FileWriter(file));
					    
					    //adding Rinit.r file on top of the script...
					    BufferedReader reader1 = new BufferedReader(new FileReader(Config.getString("r_script_init")));
					    StringBuffer sb=new StringBuffer();
					    String line1;
					    while ((line1 = reader1.readLine()) != null)  {
					    	if(line1!=null && !line1.equals("")){		    		 
					    		//out.write(line1+"\n");
					    		sb.append(line1+"\n");
						    }
					    }					    
					    reader1.close();
					    log.debug("R init source file initiated.");				    
					    String filepath=file.getPath().replaceAll("\\\\", "\\\\\\\\");
					    RConnection c=null;
					    RServeSession rs=null;

					    try{
					    	
					    	log.debug("before opening session----<");					      	
					    	log.debug("rs:"+rs);
					    	
					    	rs=getRServeSession(sframe.getRscript().getUid(),sb.toString());
					    						    	
					    	if(rs!=null && rs.getRconnection()!=null ){
					    		c=rs.getRconnection();
						        log.debug("c:"+c.isConnected());
						    	REXP sid1=c.eval(".r_session_id");
						    	if(sid1!=null){
						    		 String sess_id=sid1.asString();
						    	 	 log.debug(".r_session_id:"+sess_id);
						    	}
						    	
						 	    c.assign(".machine", P2PService.getComputerName());						    	
						 	    
						 	    log.debug("rscript:"+rscript);
						 	    
						    	REXP x1= c.parseAndEval(rscript);
						    	
						    	 
						    	String result=parse(x1);

						    	sframe.getRscript().setResultXML(result);
						    	
							    
							    log.debug("Script from temp file Evaluated");
							    log.debug("x1:"+x1);				    
							    							    
							    if (x1!=null && x1.inherits("try-error")) {							    	 
							    	throw new Exception("Error in R Script: "+x1.asString());
							    }
							    
							    if(x1==null) {						    	 
							    	throw new Exception("R script:"+data.get("name")+" failed");
							    }else{							    	 
							    	sframe.setTasklog(null);
							    }
						    }
						    file.delete();
						    
					    }catch(Exception e){
					    	    sframe.setTasklog(e.getMessage());
					    	    sframe.getRscript().setError(e.getMessage());
					    		throw new Exception ("Error:"+e.getMessage());
					    	    //throw e;
					    }finally{
						    if(c!=null){						    	
						    	//ConnectionPool.done(c.detach());
						    	closeRconnection(rs); 
						    	//c.close();						    	
						    	//c.shutdown();
						    }
						    Runtime.getRuntime().gc();
					    }
					    //try{ 	Thread.sleep(5);   }catch(Exception e){} //200 milliseconds to remove the resource completely from memory
					    
					   
				}catch(Exception e){
					//e.printStackTrace();
					//ClientErrorMgmt.reportError(e,null);
					throw e;
				} 
			}
			
			
		}
	 
	 
	 
	 
	    /**
	     * 
	     * @param rxps
	     * @return
	     * @throws Exception
	     */
	 	private String parse(REXP rxp) throws Exception {
			
	
	 		String rtn="";
			
			if(rxp.isNumeric()){
				rtn+="<double>"+rxp.asDouble()+"</double>";
			}
			if(rxp.isInteger()){
				rtn+="<integer>"+rxp.asInteger()+"</integer>";
			}
			if(rxp.isList()){
				RList ar=rxp.asList();
				rtn+="<array>";
				for(Iterator i=ar.iterator();i.hasNext();){
					Object obj=i.next();
					if(obj instanceof REXP){
						if(rxp.isNumeric())	rtn+="<double>"+((REXP)obj).asDouble()+"</double>";
						if(rxp.isString())	rtn+="<string>"+((REXP)obj).asString()+"</string>";
						if(rxp.isString())	rtn+="<integer>"+((REXP)obj).asInteger()+"</integer>";
					}
				}
				rtn+="</array>";
			}
		
			
			if(rxp.isString()){
				rtn+="<string>"+rxp.asString()+"</string>";
			}				 
			//if(rxp.getType()==REXP.XT_STR || rxp.getType()==REXP.XT_VECTOR || rxp.getType()==REXP.XT_DOUBLE || rxp.getType()==REXP.XT_ARRAY_DOUBLE){}
			//else log.debug("rxp.getType():"+rxp.getType());
			return rtn;
		}
	 	
	 	
		public RServeSession getRServeSession(String script_uid, String startup_rinit) throws Exception  {
			
			 	    	
			RServeSession rs=null;
			RConnection c=null;
			
			
	    	try{
	    		
	    		rs=RServeConnectionPool.getRSession(startup_rinit);
	    	}catch(Exception e){
	    		throw new ExceptionRServeUnixFailure("Not able to get RSession from ConnectionPool!, Error:"+e.getMessage()); 
	    	}
	    	
	    	try{
	    		log.debug("+++++++attaches Rconnection from RServeSession....+++");
	    		
	    		c=(rs.getRconnection()!=null) ?  rs.getRconnection(): rs.getRsession().attach();				    		
	    		rs.setRconnection(c);
	    	}catch(Exception e){
	    		throw new ExceptionRServeUnixFailure("Not able to attach connection to RSession!, Error:"+e.getMessage()); 
	    	}
		    if(c!=null){

		        log.debug("c:"+c.isConnected());
		        
		        rs.setRunning(true);
		        rs.setExecute_r_uid(script_uid);
		        rs.setStarted_time(new Date().getTime());
		        rs.setThread(Thread.currentThread());	        
		        		        
		    }
		   return rs;
		}
		
		 
		private void closeRconnection(RServeSession rs) throws Exception {
			try{
				
	    		rs.finished(rs.getRconnection());
	    		RServeConnectionPool.done(rs);
	    		
	    	}catch(Exception e){
	    		if(rs.getKillmessage()!=null){
	    			throw new ExceptionRServeUnixFailure(rs.getKillmessage());
	    		}else{
	    			throw new ExceptionRServeUnixFailure("Not able to detach RSession!, Error:"+e.getMessage());
	    		}
	    	}
			
		}
}


