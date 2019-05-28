/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.engines;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rosuda.JRI.RMainLoopCallbacks;
import org.rosuda.JRI.Rengine;

import com.fourelementscapital.scheduler.config.Config;
import com.fourelementscapital.scheduler.rscript.RScript;
 
 

public  class MyRengineCallbacks implements RMainLoopCallbacks {
 
 
  private Logger log = LogManager.getLogger(MyRengineCallbacks.class.getName());
  
  //private Vector errorMessages=new Vector();
  
  private File outputConsole=null;
  
  
  
  private String logmessages=null;
  
  
  public String logMessages() {
	  return this.logmessages;
  }
  
  
  public void setConsoleOutput(File file) {
	  this.outputConsole=file;
  }
  
  public void rWriteConsole(Rengine re, String text, int oType) {
     if(this.outputConsole!=null ){
    	 try{
    		FileWriter fstream = new FileWriter(this.outputConsole,true);
    	  	BufferedWriter out = new BufferedWriter(fstream);
    	  	out.write(text);
    	  	out.close();
    	 }catch(Exception e)  {log.error("error:"+e.getMessage());} 		    
		   
		    
		    //	log.debug("$$$$$$$$$$ log file folder not exist, folder:"+con_out);
		    //}
     }
	  
	 if(oType==1){
	     log.debug("rWriteConsole():");
		 externalLog((MyRengine)re,text);
		 
	 }
  }
  
  public void rBusy(Rengine re, int which) {
     // System.out.println("MyRengine.rBusy("+which+")");
 
  }
  
  public String rReadConsole(Rengine re, String prompt, int addToHistory) {
     // System.out.print("prompt:"+prompt);
      try {
          BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
          String s=br.readLine();
          log.debug("rReadConsole():s");
          return (s==null||s.length()==0)?s:s+"\n";
          
      } catch (Exception e) {
          System.out.println("jriReadConsole exception: "+e.getMessage());
      }
      return null;
  }
  
  public void rShowMessage(Rengine re, String message) {
	  log.debug("rShowMessage():message:");
      //System.out.println("MyRengine.rShowMessage \""+message+"\"");
	  //log.debug("Renginge instanceof:"+(re instanceof MyRengine)+" obj:"+((MyRengine)re).getStackFrame().getData());
	  externalLog((MyRengine)re,message);
	  
  }

  //public Vector getErrorMessages(){
	//  return this.errorMessages;
  //}
  
  
  
  public String rChooseFile(Rengine re, int newFile) {
	FileDialog fd = new FileDialog(new Frame(), (newFile==0)?"Select a file":"Select a new file", (newFile==0)?FileDialog.LOAD:FileDialog.SAVE);
	fd.show();
	String res=null;
	if (fd.getDirectory()!=null) res=fd.getDirectory();
	if (fd.getFile()!=null) res=(res==null)?fd.getFile():(res+fd.getFile());
	return res;
  }
  
  public void   rFlushConsole (Rengine re) {
  }
	
  public void   rLoadHistory  (Rengine re, String filename) {
  }			
  
  public void   rSaveHistory  (Rengine re, String filename) {
  }
  
  public void externalLog(MyRengine mre, String message ){
	  
		if(mre.getStackFrame()!=null && mre.getStackFrame().getRscript()!=null){
			RScript rs=mre.getStackFrame().getRscript();
			if(rs.getError()==null){
				rs.setError(message);
			}else{
				rs.setError(rs.getError()+"\n"+message);
			}
		}else{
			externalLog1(mre,message);
		}
  }
 
  
  public void externalLog1(MyRengine mre, String message ){

		this.logmessages=(this.logmessages==null)?message:this.logmessages+"\n"+message;
	  
		
		log.debug("mre.getStackFrame():"+mre.getStackFrame());
		if(mre.getStackFrame()!=null){ 
			if(mre.getStackFrame().getTasklog()==null) { 
				mre.getStackFrame().setTasklog(message); 
			}else{
				mre.getStackFrame().setTasklog(mre.getStackFrame().getTasklog()+" "+message);
			} 
		}
				
		//log.debug("setting log message on sframe:"+message);
		//log.debug("setting log message on sframe11:"+mre.getStackFrame().getTasklog());
		 
		
		String root=Config.getString("log_error_folder");
		
		Date d = new Date();
		String folder =root+"r_scripts";
		if (!new File(folder).isDirectory()) {
			new File(folder).mkdirs();
		}
		String fname=root+"r_scripts"+File.separator+""+new SimpleDateFormat("dd_MM_yyyy").format(d)+".txt";
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
			SimpleDateFormat timeformat=new SimpleDateFormat("h:mm a");
			 
			SimpleDateFormat dateformat=new SimpleDateFormat("dd MMM-yy");
			
			
			String logmessage="";
			logmessage+="Date:"+dateformat.format(new Date())+" Time:"+timeformat.format(new Date())+":";
			//logmessage+="Error/Warning:"+message+"\n";
			logmessage+=""+message+"\n";
			fw.append(logmessage+"\r\n");
			fw.flush();
			fw.close();

			
		}
		catch (Exception ex) {
			
			//log.error("Error:"+ex.getMessage());
			//ClientErrorMgmt.reportError(ex,"Error in external log file writing");
			
			ex.printStackTrace();
			 
		}	

	}
}


