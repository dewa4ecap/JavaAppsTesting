/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.rscript;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.jcs.JCS;
import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fourelementscapital.scheduler.config.Config;
import com.fourelementscapital.scheduler.error.ClientError;

public class RScriptAsyncListenerImpl implements RScriptListener{

	private Logger log = LogManager.getLogger(RScriptAsyncListenerImpl.class.getName());
	
	private String peer=null;
	private String result=null;
	//private HttpSession session=null;
	private JCS cache;
	private IElementAttributes att;	
	private Date started=null;
	public static String ALIVE="alive";
 
	public RScriptAsyncListenerImpl(JCS cache, IElementAttributes att) throws Exception {
		//this.session=session;
		this.cache=cache;
		this.att=att;		 
	}
 
	
	public void onScriptSent(RScript rscript, String peer) throws Exception {
		// TODO Auto-generated method stub
		this.peer=peer;
		log.debug("+++++  Script started....started:"+rscript.getScript()+" peer:"+peer);		
		if(this.cache!=null){
			this.cache.put(rscript.getUid(),ALIVE,this.att);
		}
		this.started=new Date();
	}

	
	 
	
	public String getPeer(){
		return this.peer;
	}
	
	public String getResult(){
		return this.result;
	}
	
	
	public void onScriptFinished(RScript rscript, String peer, String result, String status)throws Exception {
		 
		this.peer=peer;
		this.result=result;
		
		//if(this.session!=null){
		//	this.session.setAttribute(rscript.getUid(),this);
		//}
		
		log.debug("~~~~~~ rscript.getUid():"+rscript.getUid()+" result:"+result+" this.att:"+this.att);
		
		if(this.cache!=null){
			this.cache.put(rscript.getUid(),result,this.att);
		}
						
		
		String logmessage="";
		
		//SimpleDateFormat timeformat=new SimpleDateFormat("HH:mm:ss SSS");		 
		//SimpleDateFormat dateformat=new SimpleDateFormat("dd MMM-yy HH:mm:ss SSS");
		
		//logmessage+="UID:"+rscript.getUid()+"\tStatus:"+status+"\tPeer:"+rscript.getPeer()+"\tStarted:"+dateformat.format(this.started)+"\tEnded:"+timeformat.format(new Date())+"\tScript:"+readFirst(rscript.getScript(),50);
		String st_time=this.started!=null ? this.started.getTime()+"":"";
		logmessage+=status+"\t"+rscript.getPeer()+"\t"+rscript.getQueued_time()+"\t"+st_time+"\t"+new Date().getTime()+"\t"+rscript.getRequesthost()+"\t"+rscript.getUniquename();
		
		log.debug("~~~~~~ Script started....rscript.getError():"+rscript.getError());
		
		if(status.equalsIgnoreCase("fail") && rscript.getError()!=null){
			
			logmessage+="\tError:"+readFirst(rscript.getError(), 100);
		}
		
		log(logmessage);
		
	}
	
	
	public void onScriptTimedOut(RScript rscript) throws Exception {
	
		 
		this.result="<?xml version=\"1.0\"?><output status=\"TimedOut\">Script kicked out from the queue because scheduler couldn't find peer within the time limit</output>";
		//if(this.session!=null){
		//	this.session.setAttribute(rscript.getUid(),this);
		//}
		if(this.cache!=null){
			this.cache.put(rscript.getUid(),this.result,this.att);
		}
		SimpleDateFormat timeformat=new SimpleDateFormat("HH:mm SSS");		 
		SimpleDateFormat dateformat=new SimpleDateFormat("dd MMM-yy HH:mm:ss SSS");
		//String logmessage="UID:"+rscript.getUid()+"\tStatus:TimedOut\tAt:"+dateformat.format(new Date())+"\tScript:"+readFirst(rscript.getScript(),50);
		String logmessage="TimedOut\t"+""+"\t"+rscript.getQueued_time()+"\t\t"+new Date().getTime()+"\t"+rscript.getRequesthost()+"\t"+rscript.getUniquename();		
		log(logmessage);
	}
	
	protected static String readFirst(String result, int no_char) {
		
		String rtn=result.length()>no_char ?result.substring(0,no_char)+"....": result;
		rtn=rtn.trim();
		rtn=rtn.replaceAll("\n", " ");
		rtn=rtn.replaceAll("\r", " ");
		return rtn;
		
	}
	
	
	protected static void log(String logmessage){
		
		Date d = new Date();
		
		String root;
		if(Config.getString("log_executeR_folder")!=null && !Config.getString("log_executeR_folder").equals("") ){
			 root=Config.getString("log_executeR_folder");	
		}else{		
			 root=Config.getString("log_error_folder");
			
		}
		String folder =root+"direct_scripts";
		
		if (!new File(folder).isDirectory()) {
			new File(folder).mkdirs();
		}
		String fname=root+"direct_scripts"+File.separator+""+new SimpleDateFormat("dd_MM_yyyy").format(d)+".txt";
		File file=new File(fname);		
		
		if (!file.exists()) {	
			try {
				//file.createNewFile();
				FileWriter fw1 = new FileWriter(file,true);
				//fw1.append("Date, Time, Error Message\r\n");
				fw1.flush();
				fw1.close();
			}catch(Exception e){
				//log.error("error while creating file: Error:"+e.getMessage());
			}
		}
		

		try {
			boolean append = true;
			FileWriter fw = new FileWriter(file,append);
			//String timeformat="dd-MMM hh:mm:ss aaa ";
	
			
			//logmessage+="Error/Warning:"+message+"\n";
			//logmessage+=""+message+"\n";
			fw.append(logmessage+"\r\n");
			fw.flush();
			fw.close();

			
		}
		catch (Exception ex) {
			
			//log.error("Error:"+ex.getMessage());
			ClientError.reportError(ex,"Error in external log file writing");
			 
		}	
		
	}


	

}


