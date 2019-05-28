/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fe.p2p.websocket.cmd;

import java.util.Iterator;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.fourelementscapital.scheduler.balance.LoadBalancingQueue;
import com.fourelementscapital.scheduler.balance.executeR.LoadBalancingNewExecuteRQueue;
import com.fourelementscapital.scheduler.p2p.websocket.CommandAbstract;
import com.fourelementscapital.scheduler.queue.QueueStack;
import com.fourelementscapital.scheduler.queue.QueueStackManager;

public class Queue extends CommandAbstract {

	@Override
	public String executeValidCommand(CommandLine cmd, String command) {
		String result="";
		 if(cmd.hasOption("o")){		 
			//result="Not implemented yet";
			 LoadBalancingQueue leq=LoadBalancingQueue.getExecuteRScriptDefault();
			 if(leq instanceof LoadBalancingNewExecuteRQueue){				 
				 LoadBalancingNewExecuteRQueue leqnew=(LoadBalancingNewExecuteRQueue)leq;
				 try{
					 int q_size=leqnew.getScriptQueue().size();
					 int p_size=leqnew.getScriptProcessingQueue().size();
					 int o_size=leqnew.getAllScriptObjectsSize();
					 result+="Queued               :"+q_size+"\n";
					 result+="Processing           :"+p_size+"\n";
					 result+="Total Objects in Mem :"+o_size+"\n";					 
				 }catch(Exception e){
					 result+=" Error, e:"+e.getMessage();
				 }
			 }else{
				 result+="Current queue implementation doesn't support this feature";
			 }
		 }else{			 
			 //result=showHelp();
			 try{
				 Iterator<QueueStack> qst= QueueStackManager.getAllQueueStacks().iterator();
				 while(qst.hasNext()){
					 QueueStack qs=qst.next();
					 result+=qs.getPeername()+":"+qs.getUid()+"  -----> "+(qs.isRunning()?"Busy":"Idle")+"     "+qs.getSupportedtaskuids()+"\n";

				 }
				 if(result.equals("")){
					 result="No queue from peers";
				 }
			 }catch(Exception e){
				 result="Error, "+e.getMessage();
			 }
		 }
		 return result;
	}

	@Override
	public Options getOptions() {
		Options options = new Options();	    
	    //options.addOption("s", "show", false, "Show online peers");	    
	    //options.addOption("p", "peer", true, "Show queue stack of server side - example: -q 4ecappcsg2");	    
	    options.addOption("o", "queue-objects", false, "Show number of queued and processing objects");
	    
	    
	    return options;
	}

	@Override
	public String getHeader() {
		return "Show queue stacks and objects in the different queue in server side";
	}

	@Override
	public String getFooter() {
		return "";
	}

}


