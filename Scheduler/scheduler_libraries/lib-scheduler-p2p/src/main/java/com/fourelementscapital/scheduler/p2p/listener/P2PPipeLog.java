/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/


package com.fourelementscapital.scheduler.p2p.listener;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fourelementscapital.scheduler.config.Config;

public class P2PPipeLog {
	
	private static String nextLineChar="\r\n";
	 
	private Vector loggable=new Vector();	
	 
	public P2PPipeLog() {
		//loggable.add("\\bEXECUTETASK\\W+(?:\\w+\\W+){1,5}?CONFIRM\\b"); //client receives from server
		//loggable.add("\\bEXECUTETASK\\W+(?:\\w+\\W+){1,6}?CONFIRM\\b"); //client receives from server
		//loggable.add("\\bEXECUTETASK\\W+(?:\\w+\\W+){1,5}?TENDER\\b"); //client receives from server
		//loggable.add("\\bEXECUTETASK\\W+(?:\\w+\\W+){1,4}?FINISHED\\b"); //server receives from client
		//loggable.add("\\bEXECUTETASK\\W+(?:\\w+\\W+){1,4}?BID\\b"); //server receives from client
		//loggable.add("^EXECUTEFAILED"); //server receives from client
		//loggable.add("^RESTART_PEER"); //server receives from client
		//loggable.add("^STATISTICS"); //server receives from client
		//
		//loggable.add("\\bEXECUTETASK\\W+(?:\\w+\\W+){1,4}?TENDER\\b"); //server receives from client
		//loggable.add("^R_PACKAGES");
		//loggable.add("^R_PACKAGES_DATA");
		//loggable.add("^EXECUTESCRIPT");
		
		
	}
	
	
	//public void logIncoming(MessageBean mb)  {
		//if(validate(mb.getCommand())){
	//	   String msg="From:"+mb.getSender()+"    Comm:"+mb.getCommand();
		   //reportLog(msg);
		//}
		
	//}
	
	
	//public void logOutgoing(MessageBean mb, String destination)  {
		//if(validate(mb.getCommand())){
		//	String msg="To:"+destination+"    Comm:"+mb.getCommand();
		//	reportLog(msg);
		///}	
		
	//}
	
	private boolean validate(String s){
		boolean rtn=false;
		for(Iterator<String> it=this.loggable.iterator();(it.hasNext() && !rtn);){
			 Pattern p = Pattern.compile(it.next(), Pattern.CASE_INSENSITIVE);
			 Matcher m = p.matcher(s);			 
			 rtn= m.find();
		}
		return rtn;
	}

	public static void sendMsg(String msg, String toPeer) {
		reportLog("To--->"+toPeer+" :"+msg);
	}
	
	public static void receiveMsg(String msg, String fromPeer) {
		reportLog("<---------From "+fromPeer+" :"+msg);
	}
	
	
	private static void reportLog(  String msg){
		  
		
		msg=(msg==null)?"":msg;
		
		String root=Config.getString("log_error_folder");
		
		Date d = new Date();
		String folder =root+"p2p";
		if (!new File(folder).isDirectory()) {
			new File(folder).mkdirs();
		}
		
		String fname=root+"p2p"+File.separator+new SimpleDateFormat("dd_MM_yyyy").format(d)+".log";
		File file=new File(fname);
		
		try {
			boolean append = true;
			FileWriter fw = new FileWriter(file,append);
			
			SimpleDateFormat sd=new SimpleDateFormat("HH:mm:ss SSS");
			String logmessage=sd.format(new Date())+"  "+msg;
			if (!file.exists()) {
				file.createNewFile();
			}
			fw.append(logmessage+nextLineChar);
			fw.flush();
			fw.close();
			//String consoleoutput=Config.getString("log_error_to_console");			
			
			//if(consoleoutput!=null && consoleoutput.equalsIgnoreCase("yes")){
				//System.out.println(logmessage);
			//}
			//
		}
		catch (Exception ex1) {
			ex1.printStackTrace();
			 
		}	
		
	}

}


