/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.p2p.websocket;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class CommandAbstract {

	
	private Logger log = LogManager.getLogger(CommandAbstract.class.getName());
	
	public Options getOptionsWithHelp(){
		Options opt=getOptions();
		opt.addOption("h", "help", false, "Show help of this command");
		
		
		return opt;
	}
 
	
	public String showHelp() {
	    HelpFormatter h = new HelpFormatter();
	    StringWriter sw=new StringWriter();
	    String firstcommand=this.getClass().getSimpleName();
	    firstcommand=CommandMain.classToCommand(firstcommand);
	    //firstcommand=firstcommand.replaceFirst("Command", "");    
	    h.printHelp(new PrintWriter(sw),200,firstcommand, getHeader(), getOptionsWithHelp(),0,5,getFooter(),true);
	    return sw.toString();	    
	}
	
	public String getCommand() {
	    String firstcommand=this.getClass().getSimpleName();
	    firstcommand=CommandMain.classToCommand(firstcommand);
	    return firstcommand;
	}
	
	
	public String executeCommand (String command) throws Exception {
		String msg="";
		try{
		
			CommandLineParser parser = new PosixParser();
			CommandLine cmd = parser.parse(getOptionsWithHelp(), command.split(" "));
			
			if(cmd.hasOption('h')){
				msg=showHelp();
			}else{
				log.debug("executeValidCommand()");
				msg=executeValidCommand(cmd, command);
			}
				
			
			
		}catch(ParseException pe){			
			
			msg=showHelp();
			
		}catch(Exception e){
		
			msg=e.getMessage();
	 
		}
		return msg;
		
	}
	
	public abstract String executeValidCommand(CommandLine cmd, String command);
	public abstract Options getOptions();
	public abstract String getHeader();
	public abstract String getFooter();
	
	
	
	
	
	
}


