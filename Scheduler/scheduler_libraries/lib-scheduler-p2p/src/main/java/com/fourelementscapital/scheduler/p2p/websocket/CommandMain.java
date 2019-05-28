/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.p2p.websocket;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class CommandMain {

	
	private Logger log = LogManager.getLogger(CommandMain.class.getName());
	
	private String command;
	
	public CommandMain(String cmd){
		this.command=cmd;
	}
	
	public String validate() {
		String cmds[]=this.command.split(" ");
		String msg="";
		if(cmds.length>0){
			//char[] stringArray = cmds[0].toCharArray();
			//stringArray[0] = Character.toUpperCase(stringArray[0]);
			//String commandclass = this.getClass().getPackage().getName()+".cmd."+ new String(stringArray);
			String clss=command2Class(cmds[0]);
			String commandclass = this.getClass().getPackage().getName()+".cmd."+ clss;
			
			//System.out.println("CommandMain: his.getClass().getPackage():"+commandclass);
			//ClassLoader myClassLoader = ClassLoader.getSystemClassLoader();
			ClassLoader myClassLoader =this.getClass().getClassLoader();
			log.debug("commandclass:"+commandclass);
			
			try {
				Class myClass = myClassLoader.loadClass(commandclass);
				CommandAbstract cmdobj=(CommandAbstract)myClass.newInstance();
				 
				
				msg=cmdobj.executeCommand(this.command);
				

			} catch (ClassNotFoundException e) {				 
				msg="Invalid command";
				
			} catch (InstantiationException e) {
				 
				e.printStackTrace();
			} catch (IllegalAccessException e) {			 
				e.printStackTrace();
			} catch (Exception e) {
				 
				e.printStackTrace();
			}
			 
			
			return msg;
		}else{
			msg="Invalid command";
		}
		return msg;
	}
	
	public static String command2Class(String command) {
		try {
			Pattern p1 = Pattern.compile("(\\_)([a-z]+?)",Pattern.DOTALL);
	        final Matcher matcher = p1.matcher(command);
	        final StringBuffer sb = new StringBuffer();
	        String grp="";
	        while(matcher.find()){       	        
	        	if( matcher.groupCount()>1){
	                matcher.appendReplacement(sb, matcher.group(2).toUpperCase());
	        	}
	        }
	        matcher.appendTail(sb);
	        String cls=sb.toString();
	        char[] stringArray = cls.toCharArray();
			stringArray[0] = Character.toUpperCase(stringArray[0]);		
			return new String(stringArray);
		}catch(Exception e){
			
			return "";
		}
			
	}
	public static String classToCommand(String classname) {
		try {
			char[] stringArray = classname.toCharArray();
			stringArray[0] = Character.toLowerCase(stringArray[0]);		
			classname= new String(stringArray);
			 
			Pattern p1 = Pattern.compile("([A-Z]+?)",Pattern.DOTALL);
	        final Matcher matcher = p1.matcher(classname);
	        final StringBuffer sb = new StringBuffer();
	        String grp="";
	        while(matcher.find()){       	        
	        	if( matcher.groupCount()>0){
	                matcher.appendReplacement(sb, "_"+matcher.group(1).toLowerCase());
	        	}
	        }
	        matcher.appendTail(sb);	        
			return sb.toString();
		}catch(Exception e){
			
			return "";
		}
			
	}
	
}


