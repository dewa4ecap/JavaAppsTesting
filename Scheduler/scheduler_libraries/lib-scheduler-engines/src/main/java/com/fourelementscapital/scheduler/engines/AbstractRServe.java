/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.engines;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionException;
import org.rosuda.REngine.Rserve.RConnection;

import com.fourelementscapital.scheduler.config.Config;
import com.fourelementscapital.scheduler.exception.ExceptionRServeUnixFailure;
import com.fourelementscapital.scheduler.exception.ExceptionUnixPeerUnknown;
import com.fourelementscapital.scheduler.exception.SchedulerException;
import com.fourelementscapital.scheduler.p2p.P2PService;
import com.fourelementscapital.scheduler.rserve.RServeSession;

public abstract class AbstractRServe extends AbstractRScript{

	public AbstractRServe(String name, String uid) {
		super(name, uid);		 
	}
	
	private Logger log = LogManager.getLogger(AbstractRServe.class.getName());
	protected StackFrame stackframe=null;

	public abstract RServeSession getRServeSession(int nid,String name) throws Exception;
	public abstract void evalScript(RServeSession rs, String rscript) throws Exception;
	public abstract void closeRconnection(RServeSession rs) throws Exception;
	
	public  void execute(StackFrame sframe) throws  JobExecutionException,Exception 	  {
			
			this.stackframe=sframe;
			Map<String, Object> data=sframe.getData();
			String rscript=(String)data.get("rscript");
			Number nid=(Number)data.get("id");
			String name=sframe.getData().get("name").toString();
			
			
			if(rscript==null) { throw new JobExecutionException("Task Failed because no R script found or empty");  }
			

			//log.debug("R Engine initiated.");
			if(rscript!=null && !rscript.equals("")){	 
				try{
						 
					    //System.out.println(" RServeLowPriorityTask.execute(): executed 11");
					    String line;
		 
					    log.debug("R init source file initiated.");
											    
					    StringBuffer sb=new StringBuffer();				    
					    log.debug("------------------<<<<r_script_init:"+Config.getString("r_script_init"));
					    
					    //collecting Rinit.r content into stringbuffer
					    if(Config.getString("r_script_init")!=null && new File(Config.getString("r_script_init")).exists()){
					    	log.debug("including r_script_init");
						    //adding Rinit.r file on top of the script...
						    BufferedReader reader1 = new BufferedReader(new FileReader(Config.getString("r_script_init")));
						    String line1;
						    while ((line1 = reader1.readLine()) != null)  {
						    	if(line1!=null && !line1.equals("")){		    		 
						    		sb.append(line1+"\n");
							    }
						    }					    
						    reader1.close();
						    log.debug("R init source file initiated.");					    
					    }
					    //collecting main script...
					    BufferedReader reader = new BufferedReader(new StringReader(rscript));
					    while ((line = reader.readLine()) != null)	    {					    	 
					    	if(line!=null && !line.equals("")){					    	 
					    		//out.write(line+"\n");
					    		sb.append(line+"\n");
					    	}
					    }					    
					    //out.close();
					    reader.close();
					    
					    RConnection c=null;
					    RServeSession rs=null;
					    File temp=null;
					    try{

					    	rs=getRServeSession(nid.intValue(), name);
					    	
					    	//c=getRconnection(nid.intValue(), name);
						    if(rs!=null && rs.getRconnection()!=null ){
						    	
						    	new SchedulerExePlanLogs(nid.intValue(),sframe.getTrigger_time()).log("RServe session started..",SchedulerExePlanLogs.PEER_OK_RESPOND_RSERVE_SESSIONSTARTED);
						    	
						    	
						        c=rs.getRconnection();
 
						       	c.assign(".trigger_time", sframe.getTrigger_time()+"");
							    c.assign(".scheduler_id", nid+"");
							    c.assign(".machine", P2PService.getComputerName());
							    c.assign(".enginetype","RServeUnix");
							    c.assign(".connection_ids","");
							  							    
							    //sink staring here....
							    
							    
							    temp = File.createTempFile(nid.intValue()+"_"+sframe.getTrigger_time(),".log");
							   
							    temp.getParentFile().setWritable(true, false);
							    
								String ff=temp.getPath();
								if(ff.contains("\\")){
									ff=ff.replaceAll("\\\\", "\\\\\\\\");
								}								
								temp.delete();
								
								log.debug("---- deleting ff:"+ff);
							    String fileass="file(\""+ff+"\", open = \"wt\");";
							    log.debug("file assign------fileass:"+fileass);
							    c.eval(".console_msg_zz<-"+fileass);
							    c.eval("sink(.console_msg_zz)");
							    c.eval("sink(.console_msg_zz, type = \"message\")") ;
							    log.debug("----sink----");							    
							    
							    if(false){
							    	//ignored as it is not useful anymore
							    	c.eval("if(length(.r_scripts)>12){ .r_scripts=.r_scripts[(length(.r_scripts)-10):length(.r_scripts)] }"); //keep only last 10 when it goes more than 12 to avoid keeping too many things in memory.
							    	SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd_HHmmss");
							    	String tl=nid+"|"+sframe.getTrigger_time()+"|"+sdf.format(new Date());							    
							    	c.eval(".r_scripts=append(.r_scripts,\""+tl+"\")");							    
							    }
							    
							    new SchedulerExePlanLogs(nid.intValue(),sframe.getTrigger_time()).log("RServe Script evaluation starting..",SchedulerExePlanLogs.PEER_OK_RSERVE_SCRIPT_EVAL_STARTING);
							    
							    evalScript(rs,sb.toString());

						    }else{
						    	throw new ExceptionRServeUnixFailure("Couldn't start RServ Connection!, Connection is null"); 
						    }
						    
						    
						    
					    }catch(SchedulerException se){
					    	throw se;
					    }catch(Exception e){
					    	    sframe.setTasklog(e.getMessage());			    	
					    	    throw new ExceptionUnixPeerUnknown("RServeUnixTask: Error:"+e.getMessage());
					    	    
					    }finally{
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
						    if(c!=null){		
						        //sink ending here....							    
							    c.eval("sink(type = \"message\")");
							    c.eval("sink()");		
							    c.eval("close(.console_msg_zz)");	
						    	closeRconnection(rs); 
						    }
						    //Runtime.getRuntime().gc();
					    }
					    try{ 	Thread.sleep(200);   }catch(Exception e){} //200 milliseconds to remove the resource completely from memory
					    log.debug(" end of execute() script :"+name);
					   
				}catch(Exception e){
			
					//ClientErrorMgmt.reportError(e,null);
					throw e;
				} 
			}			
		}
	
	
}


