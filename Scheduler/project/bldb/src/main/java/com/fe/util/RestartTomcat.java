/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fe.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Vector;

import com.fe.scheduler.SchedulerInitServlet;
import com.fourelementscapital.scheduler.peer.QueueFactory;

public class RestartTomcat {

	
	 private static Vector ignoredIds4Restart=new Vector();
	 
 
	
	 
	
	public static void restartPeerLater(){
		//thse 2 lines commented because, apart from scheduler task, there will be tasks also running, which will not have id.
	    //Vector executingIds=new QueueFactory().getExecutingIDs();
	    //if(executingIds.size()==0) {	    	
	    int  count=new QueueFactory().countExcTasksInPeer();
		if(count==0) {	
			
	    	RestartTomcat.restartPeerNow();
	    }else{
	    	Vector executingIds=new QueueFactory().getExecutingIDs();
	    	System.out.println("RestartTomcat.java:: executingIds:"+executingIds+" ignoredIds4Restart:"+ignoredIds4Restart);
		    if(executingIds.containsAll(ignoredIds4Restart) && executingIds.size()==ignoredIds4Restart.size()){
		    	RestartTomcat.restartPeerNow();
		    	//System.out.println("RestartTomcat.java:: restarting now, it has killed id");
		    }
	    }

	}
	
	
	
	public static void restartPeerLater(int task_2b_killed){
		    //System.out.println("RestartTomcat.java:: restartPeerLater("+task_2b_killed+") called");
		
		    QueueFactory.setRestartRequested();
		    if(task_2b_killed>0){
		    	ignoredIds4Restart.add(task_2b_killed);
		    }
		    RestartTomcat.restartPeerLater();
		
	}
	
	
	
	
	
	public static void restartPeerNow(){
		
		try{
		    String tempDir = System.getProperty("java.io.tmpdir");
		    File file=new File(tempDir+File.separator+"restart.bat");
		    String line;
		    BufferedWriter out = new BufferedWriter(new FileWriter(file));
		    
		    String service="Tomcat6";		    		    
			out.write("net stop \""+service+"\" \n");
			
			out.write("ping -n 8 localhost > nul\n");
    	
			// killing tasks in case of the related services are not stoppeed.
    		out.write("taskkill /f /t /im Rserve.exe \n");
    		out.write("taskkill /f /t /im tomcat6.exe \n");

    		out.write("ping -n 3 localhost > nul\n");   		
    		
    		//copy the peer code from repository 
    		out.write("xcopy \\\\4ecapsvsg2\\Public\\IT\\TomcatDev\\peer_classes\\. "+SchedulerInitServlet.getAppPath()+"\\WEB-INF /S /Y /D \n");    		
    		
    		out.write("net start \""+service+"\"\n");    		
    		out.write("ping -n 3 localhost > nul\n");
    		
    		out.write("EXIT \n");
		    out.close();		    

		    Runtime rt = Runtime.getRuntime();
		    String[] commandArgs = new String[]{"cmd", "/C", "start", file.getPath() };
		   // String[] commandArgs = new String[]{"cmd", "/C", file.getPath()};
		    Process proc = rt.exec(commandArgs);
		    
 
		    
		}catch(Exception e){
			e.printStackTrace();
		}

		
		
	}
	
	
	public static void restartMainServer(){
		
		try{
		    //String tempDir = System.getProperty("java.io.tmpdir");
		    //File file=new File(tempDir+File.separator+"restart.bat");
		    //String line;
		    //BufferedWriter out = new BufferedWriter(new FileWriter(file));
		    //out.write("rem restarting of service..\n");
    		//out.write("net stop \"Tomcat6\"\n");
    		//out.write("ping -n 5 localhost > nul\n");
    		//out.write("xcopy \\\\4ecapsvsg2\\Public\\IT\\TomcatDev\\peer_classes\\. "+SchedulerInitServlet.getAppPath()+"\\WEB-INF /S /Y \n");
    		//out.write("net start \"Tomcat6\"\n");
		    //out.close();
		    
		    
		    //Process p = Runtime.getRuntime().exec(file.getPath());
		    Runtime rt = Runtime.getRuntime();
		    String[] commandArgs = new String[]{"nohup", "/home/p2pserver/restart.sh", "&"};
		    Process proc = rt.exec(commandArgs);
            
		    
		    
		    
		}catch(Exception e){
			e.printStackTrace();
		}

		
		
	}
}


