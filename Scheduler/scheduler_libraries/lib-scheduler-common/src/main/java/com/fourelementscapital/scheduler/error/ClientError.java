/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.error;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.fourelementscapital.scheduler.config.Config;

/**
 * This class handles error logging of the system.
 * <br>
 * <pre>
 * property to configure the folder "log_error_folder"
 * and also property that sends error log into console if a config property "log_error_to_console" is set to "yes"
 * </pre>
 * @author Rams Kannan
 *
 */
public class ClientError {

	
	private static String nextLineChar="\r\n";

	/**
	 * This method wil be invoked when there was error and the error log will be sent to an external file.
	 * One log file per day will be created in this
	 * <pre>
	 * property to configure the folder "log_error_folder"
	 * and also property that sends error log into console if a config property "log_error_to_console" is set to "yes"
	 * </pre>
	 * @param ex
	 * @param msg
	 */
	public static void reportError(Exception ex, String msg){
		  
		
		msg=(msg==null)?"":msg;
		
		String root=Config.getString("log_error_folder");
		
		Date d = new Date();
		String folder =root+"errorlogs";
		if (!new File(folder).isDirectory()) {
			new File(folder).mkdirs();
		}
		String fname=root+"errorlogs"+File.separator+"error"+new SimpleDateFormat("dd_MM_yyyy").format(d)+".log";
		File file=new File(fname);
		
		try {
			boolean append = true;
			FileWriter fw = new FileWriter(file,append);
			 
			
			SimpleDateFormat sd=new SimpleDateFormat("dd-MM-yyy h:mm:ss a");
			String logmessage="-------- "+sd.format(new Date())+" ------"+
					nextLineChar+collectErrorStack(ex,msg)+"***********************"+nextLineChar;
			
			
			
			 
			if (!file.exists()) {
				file.createNewFile();
			}
			fw.append(logmessage+nextLineChar+" "+nextLineChar);
			fw.flush();
			fw.close();
			 
			
			String consoleoutput=Config.getString("log_error_to_console");			
			
			if(consoleoutput!=null && consoleoutput.equalsIgnoreCase("yes")){
				System.out.println(logmessage);
			}
			//
		}
		catch (Exception ex1) {
			ex1.printStackTrace();
			  
			 
		}	
		
	}
	
	 
	
 
	
	private static String collectErrorStack(Exception ex,String msg) throws Exception {
		StackTraceElement[] stacks=ex.getStackTrace();
		String rtn="";
		if(msg!=null) rtn+=msg.trim()+nextLineChar;
		rtn+="ERROR MSG:"+ex.getMessage()+nextLineChar;
		int startat=0;
		for(int i=0;i<stacks.length;i++){
		   if(stacks[i].getClassName().startsWith("com.fe.")){
			   startat=i;
		   }
		}
		int numbefore=4;
		startat=(startat>numbefore)?startat-numbefore:0;
		int counter=1;
		
		
		
		String consoleoutputfull=Config.getString("log_error_to_console_full");			
		
		if(consoleoutputfull!=null && consoleoutputfull.equalsIgnoreCase("yes")){
			for(int i=startat;i<stacks.length;i++){
				//if((i>(startat+numbefore)) && !stacks[i].getClassName().startsWith("com.fe.")){
					
				//}else{
					String space="  ";
					//for(int ab=0;ab<counter;ab++) space+=" "; counter++;
					if(stacks[i].getClassName().startsWith("com.fe.")){
						rtn+="->"+stacks[i].getClassName()+"."+stacks[i].getMethodName()+"()"+nextLineChar;;
					}else{
						rtn+=space+""+stacks[i].getClassName()+"."+stacks[i].getMethodName()+"()"+nextLineChar;
					}
				//}
			}
				
			
		}else{
		
			for(int i=startat;i<stacks.length;i++){
				if((i>(startat+numbefore)) && !stacks[i].getClassName().startsWith("com.fe.")){
					
				}else{
					String space="";
					for(int ab=0;ab<counter;ab++) space+=" "; counter++;
					if(stacks[i].getClassName().startsWith("com.fe.")){
						space+="->";
					} 
					rtn+=space+""+stacks[i].getClassName()+"."+stacks[i].getMethodName()+"()"+nextLineChar;
				}
				
			}
		}
		return rtn;
	}
	

}


