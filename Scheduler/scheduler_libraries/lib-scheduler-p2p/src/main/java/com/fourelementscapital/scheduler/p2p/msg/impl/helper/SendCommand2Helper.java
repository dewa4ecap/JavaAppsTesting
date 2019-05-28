/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.p2p.msg.impl.helper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.jcs.JCS;
import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fourelementscapital.fileutils.RandomString;
import com.fourelementscapital.scheduler.p2p.msg.MessageHandler;
import com.fourelementscapital.scheduler.p2p.msg.PostCallBack;
import com.fourelementscapital.scheduler.p2p.msg.helper.HelperPostMessage;

public class SendCommand2Helper extends MessageHandler implements PostCallBack,Serializable { 

	 //implements PostCallBack {

		private Logger log = LogManager.getLogger(SendCommand2Helper.class.getName());
		
		
		//private static final String STOP_CLIENT_QUEUE="yes";
		//private String stopqueue=null;
		private String command=null;
		
		private String response=null;

		public static final boolean WAIT_QUEUE_TO_FINISH=false;
		
		private boolean wait=false;
		private String cachekey=null;
		
		
		public String getCachekey() {
			return cachekey;
		}


		public void setCachekey(String cachekey) {
			this.cachekey = cachekey;
		}


		public String getResponse() {
			return response;
		}


		public void setWait(boolean flag) {
			this.wait=flag;
		}

		public void setResponse(String response) {
			this.response = response;
		}



		public String getCommand() {
			return command;
		}





		public void setCommand(String command) {
			this.command = command;
		}

		private static String currentCommand=null;


		
		public Map executeAtDestination() {
			try{
				
				
				 
				System.out.println("RequestPeerRestart: executeAtDestination() command:"+this.command);
				
				
				StringTokenizer st=new StringTokenizer(command,"\n");
				Runtime runtime = Runtime.getRuntime();
				this.response="";		
				
				while(st.hasMoreTokens()){
					
	          		   String cl=st.nextToken();
	          		   Process process = runtime.exec(cl);
	          		   BufferedReader br2 = new BufferedReader(new InputStreamReader(process.getInputStream()));          		    
	          		   //this.response+="---"+cl+"--->";
	          		   String line;
	          		   while ((line = br2.readLine()) != null)   {	            			   
	          			  this.response+="\n"+line;
	          		   }
	          		   this.response+="\n";
	          		   //this.wait(2000);
	          		   Thread.sleep(2000);
				}
				System.out.println("RequestPeerRestart: executeAtDestination() response:"+this.response);
				 
			}catch(Exception e) {
				e.printStackTrace();
				log.error("Error while executing on destination:"+ e.getMessage());
			}
			
			return null;
		}

		
		
		
		/**
		 * this will be executed on client.
		 * @param data
		 */
		public void callBack(Map data) {

			try{
				
				IElementAttributes att= gCache().getDefaultElementAttributes();
				att.setMaxLifeSeconds(30);
				
				gCache().put(getCachekey(), getResponse(),att);
			}catch(Exception e){
				e.printStackTrace();
				log.error("Error while callback e:"+e.getMessage());
			}		
	       //System.out.println("RequestPeerRestart: response:"+cachekey);
		   //System.out.println("RequestPeerRestart: response:"+response);
		}
		
		private static JCS cache=null;
		private static JCS gCache() throws Exception {
			 if(cache==null){
					cache=JCS.getInstance(SendCommand2Helper.class.getName());
			 }
			 return cache;
		}
				
		
		
		public static String sendCommand(String peername, String command) throws Exception {		
			
			    String cachekey = RandomString.getString(10);
				IElementAttributes att= gCache().getDefaultElementAttributes();
				att.setMaxLifeSeconds(30);
				
				gCache().put(cachekey, "", att);
			
				SendCommand2Helper rpr=new SendCommand2Helper();
				rpr.setCachekey(cachekey);
				//rpr.setCommand("cmd.exe /c sc stop 4EPeer  \n cmd.exe /c sc start 4EPeer");
				//rpr.setCommand("cmd.exe /c sc "+command+" 4EPeer");			
				rpr.setCommand(command);
				 
				new HelperPostMessage(rpr, peername).send();
				
				while(gCache().get(cachekey)!=null && ((String)gCache().get(cachekey)).equals("") ) {
					Thread.sleep(100);
				}
				String rtn=null;
				if(gCache().get(cachekey)!=null && !((String)gCache().get(cachekey)).equals("")){
					//System.out.println("get the response::------------------------------------>");
					//System.out.println(gCache().get(cachekey));
					rtn=(String)gCache().get(cachekey);
				}
				gCache().remove(cachekey);
				//System.out.println("end..");
				return rtn;
		}
		 
		public static String sendCommand(String peername, String command, int waitSecs) throws Exception {		
			
		    String cachekey = RandomString.getString(10);
			IElementAttributes att= gCache().getDefaultElementAttributes();
			att.setMaxLifeSeconds(waitSecs);
			
			gCache().put(cachekey, "", att);
		
			SendCommand2Helper rpr=new SendCommand2Helper();
			rpr.setCachekey(cachekey);
			//rpr.setCommand("cmd.exe /c sc stop 4EPeer  \n cmd.exe /c sc start 4EPeer");
			//rpr.setCommand("cmd.exe /c sc "+command+" 4EPeer");			
			rpr.setCommand(command);
			 
			new HelperPostMessage(rpr, peername).send();
			
			while(gCache().get(cachekey)!=null && ((String)gCache().get(cachekey)).equals("") ) {
				Thread.sleep(100);
			}
			String rtn=null;
			if(gCache().get(cachekey)!=null && !((String)gCache().get(cachekey)).equals("")){
				//System.out.println("get the response::------------------------------------>");
				//System.out.println(gCache().get(cachekey));
				rtn=(String)gCache().get(cachekey);
			}
			gCache().remove(cachekey);
			//System.out.println("end..");
			return rtn;
	}


		@Override
		public void onCallBackSendingFailed() {
			// TODO Auto-generated method stub
			
		}


		@Override
		public void onSendingFailed() {
			// TODO Auto-generated method stub
			
		}
		
}


