/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fe.p2p;

import java.io.File;
import java.io.FileInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import net.jxta.discovery.DiscoveryService;
import net.jxta.pipe.InputPipe;
import net.jxta.protocol.PeerAdvertisement;
import net.jxta.protocol.PipeAdvertisement;

import org.apache.commons.digester.Digester;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fe.io.peer.PeerClient;
import com.fe.io.server.ServerScheduler;
import com.fourelementscapital.db.RFunctionDB;
import com.fourelementscapital.db.SchedulerDB;
import com.fourelementscapital.db.SuperDB;
import com.fourelementscapital.db.UtilDB;
import com.fourelementscapital.db.vo.PeerPackage;
import com.fourelementscapital.scheduler.balance.ExecutingQueueCleaner;
import com.fourelementscapital.scheduler.config.Config;
import com.fourelementscapital.scheduler.group.RScriptScheduledTask;
import com.fourelementscapital.scheduler.p2p.MessageBean;
import com.fourelementscapital.scheduler.p2p.P2PAdvertisement;
import com.fourelementscapital.scheduler.p2p.P2PService;
import com.fourelementscapital.scheduler.p2p.listener.IncomingMessage;

public class P2PServiceServlet  extends HttpServlet  {

	
 

	private static Thread jxtaInpThread=null;
	private static Thread jxtaInpHelperThread=null;
	private InputPipe inputPipe=null;
    private static volatile boolean pipspin;
    private static volatile boolean pipspinhelper;
    private   Timer timer=null; 
    private Logger log = LogManager.getLogger(P2PServiceServlet.class.getName());
	public static final String helper_prefix="_helper";
	private static String defaultP2PCache=""; //  addition because there is a possibility that .cache folder cause an error when restarting .. bug when restarting (20140411)
    
	@Override
	public void init(ServletConfig servletConfig ) throws ServletException {		
		super.init(servletConfig);
		start();
	}
	
	private void print(String pr) {
		System.out.println("P2PServiceServlet:"+pr);
	}
		
	public void start() {
		 	P2PService.removeCache(); // for the server
			P2PService.setPeerStarted();
			updatePeerName();		
			print("Peer updated");
			startJxta();
			print("starteed Jxta");
			
			try {
				capturePackageVersions();
			} catch (Exception e1) {
				 
				log.error("!!!!! Error while capturing package information...");
			}
			
			try{
				
				if(Config.getString("p2p.ignorejri")!=null && Config.getString("p2p.ignorejri").equalsIgnoreCase("true")){}else{
					print("Before capturing r version");
					RScriptScheduledTask.getRVersion();
				}
				print("end of start");
				
			}catch(Exception e){
				log.error("STARTUP ERROR:rJava support is not available on this peer");
			}			
		 
	}
	

	
	public void startHelper() {
		P2PService.removeCache(); //the .cache forlder will be locked when peer_helper is started
		P2PService.setPeerStarted();		
		startHelperJxta();
		
   }

	
	 	
	public static boolean isJxtaInpThreadRunning(){
		if(jxtaInpThread!=null && jxtaInpThread.isAlive())
			return true;
		else 
			return false;
	}
	
	private void startJxta(){
		 try {
				
				System.out.println("starting network....................................");
				//System.out.println("OS:"+System.getProperty("os.name"));

				java.util.logging.Logger.getLogger("net.jxta").setLevel(Level.SEVERE);
				
				//NetworkManager manager=P2PService.getManager();
				//PeerGroup netPeerGroup = manager.getNetPeerGroup();
			        // get the discovery service
				DiscoveryService discovery = P2PService.getDiscoveryService();
				//PipeService pipeService = netPeerGroup.getPipeService();
				
				long waittime =  10 * 1000L;
				 
				String computername=P2PService.getComputerName();		
				
				P2PAdvertisement ad=new P2PAdvertisement();
				
				 
				
				PeerAdvertisement mdadv = ad.getPeerAdvertisement(computername,P2PService.getPeerGroup());
				
				discovery.publish(mdadv,DiscoveryService.NO_EXPIRATION,DiscoveryService.NO_EXPIRATION);
				discovery.remotePublish(mdadv,DiscoveryService.NO_EXPIRATION);

				
				jxtaInpThread=new Thread("Input poll") {
					
					private Logger log = LogManager.getLogger(P2PServiceServlet.class.getName());
		            public void run() {
		            	
		               log.debug("jxtaInpThread run()11 ");
		               
		               int sleepy=3000*60;              
		               String computername=P2PService.getComputerName();
		               log.debug("computer name :"+computername);
		               P2PAdvertisement ad=new P2PAdvertisement();
		               log.debug("ad :"+ad);
		               PipeAdvertisement pipeAdv = ad.getPipeAdvertisement(computername,P2PService.getPeerGroup());
		   			   log.debug("creating input pipe advertisement ");
		   			   try{
		   				    inputPipe = P2PService.getPipeService().createInputPipe(pipeAdv, new IncomingMessage(null) );
		   				    //inputPipe = P2PService.getPipeService().createInputPipe(pipeAdv );
		   				    log.debug("waiting input pipe");
		   				    
		   			   }catch(Exception e){
		   				   System.out.println("~~~~~~~~~~~~~FATAL Error P2PServiceServlet2$.run()  on inputpipe service................");
		   				   e.printStackTrace();
		   			   }
		   			
		   			   
		   			   P2PServiceServlet.pipspin=true;
		               while(P2PServiceServlet.pipspin) {
		                  try {
		                      sleep(sleepy);		                   
		                  }
		                  catch(InterruptedException e) {log.error("Waiting thread intrupted...........");}
		               }
		            }
		        };
		        jxtaInpThread.start();
		        log.debug("jxtaInpThread started ");
		        TimerTask tt=new TimerTask() {
		            public void run() {
		               // System.out.format("Time's up!%n");
		            	if(!jxtaInpThread.isAlive()){
		            		//Logger log=Logger.getLogger(P2PServiceServlet.class);
		            		//log.error("jxtaInpThread is not alive restarting the tread");
		            		jxtaInpThread.start();
		            	}
		            }
		        };
		        //checks every 1 minute to see if a thread still running....
		        long freq=90*1000;
		        timer = new Timer();		        
		        timer.scheduleAtFixedRate(tt,freq, freq);
		        
	        
		        TimerTask dbCleaner=new TimerTask() {
		        	Logger log = LogManager.getLogger(P2PServiceServlet.class.getName());
		            public void run() {
		            	
		            	int db_close_timeout = SuperDB.getDbCloseTimeout();
		            	
		            	UtilDB udb=UtilDB.getUtilDB4SQLServer();
		        		try{
		        			
		        			SuperDB.killConnections(db_close_timeout);
		        			Thread.sleep(300);
		        			udb.connectDB(getUtilDBName());
		        			ArrayList al=udb.showOldConnections(db_close_timeout,P2PService.getComputerName());
		        			if(al != null && al.size()>0){
		        				
		        				
		        				
		        				
		        				log.error("~~~~~~Killing "+al.size()+" physical connections in database after "+db_close_timeout+" minutes");
		        				log.error("===While killing the following found left unclosed==");
		        				log.error(SchedulerDB.collectStack4All());
		        				log.error("=====");
		        				
		        				
		        				udb.killConnections(al);
		        			}
		        			
		        		}catch(Exception e){
		        			e.printStackTrace();		        			
		        		}finally{
		        			try{
		        			udb.closeDB();
		        			}catch(Exception e){}
		        		}

		            }
		        };
		        
		        //checks every 5 minute to see if a thread still running....
		        long fivemins=60*1000*5;
		        Timer dbtimer = new Timer();		        
		        dbtimer.scheduleAtFixedRate(dbCleaner,fivemins, fivemins);
		        
		        String tname="jett_thread_"+(new Date().getTime()/1000);
		        
		        
		    	if(Config.getValue("load_balancing_server")!=null && Config.getValue("load_balancing_server").equals(P2PService.getComputerName())){
		    		ExecutingQueueCleaner.clean(); //clean up the dead tasks in every 30 second.		    		
		    	} 
		    	
		    	
		    	
		    	
		    	/*
		    	 * launching NIO server
		    	 * 
		    	 */
		    	if(Config.getValue("nio.server.mode")!=null && Config.getValue("nio.server.mode").trim().equalsIgnoreCase("true") && 
		    			Config.getValue("nio.server.port")!=null && !Config.getValue("nio.server.port").trim().equals("") 	
		    	){
		    		int port=1818;		    	
		    		try{
		    			port=Integer.parseInt(Config.getValue("nio.server.port"));
		    		}catch(Exception e){}
		    		Thread t=new Thread(new ServerScheduler(port),tname);
		            t.start();
		            System.out.println("--->ServerScheduler starting....in port:"+port);
		            
		    	}else{
		    		
		    		
		    		
			    	if(Config.getValue("nio.server.address")!=null && !Config.getValue("nio.server.address").trim().equals("") && 
			    			Config.getValue("nio.server.port")!=null && !Config.getValue("nio.server.port").trim().equals("")
			    	){
			    		System.out.println("--->PeerScheduler will start ...in IP:"+Config.getValue("nio.server.address")+" port:"+Config.getValue("nio.server.port"));
			    		
			    		int port=1818;
				    	String server=Config.getValue("nio.server.address");
			    		try{
			    			port=Integer.parseInt(Config.getValue("nio.server.port"));
			    		}catch(Exception e){}
			    		log.debug("server:"+server+" port:"+port+" tname:"+tname);
			    		Thread t=new Thread(new PeerClient(server, port),tname);
			    		log.debug("t:"+t);
			            t.start();
			            log.debug("started:");
			            //System.out.println("<---PeerScheduler starting...., "+server+":"+port);
			    	}
		    	}
	            
		         
			  }catch(Exception e){
				e.printStackTrace();  
			  }
	}
	
 
	private void startHelperJxta(){
		
		 try {
				
				 

				java.util.logging.Logger.getLogger("net.jxta").setLevel(Level.SEVERE);				
			 
				DiscoveryService discovery = P2PService.getDiscoveryService();
				 
			        
				long waittime =  10 * 1000L;
				 
				String computername=P2PService.getComputerName();		
				
				P2PAdvertisement ad=new P2PAdvertisement();
				 
		  	
				PeerAdvertisement mdadv = ad.getPeerAdvertisement(computername+helper_prefix,P2PService.getPeerGroup());
				
				discovery.publish(mdadv,DiscoveryService.NO_EXPIRATION,DiscoveryService.NO_EXPIRATION);
				discovery.remotePublish(mdadv,DiscoveryService.NO_EXPIRATION);

				
				jxtaInpThread=new Thread("Input poll") {
					
					private Logger log = LogManager.getLogger(P2PServiceServlet.class.getName());
		            public void run() {
		               
		               int sleepy=3000*60;              
		               
		               String computername=P2PService.getComputerName();	
		               P2PAdvertisement ad=new P2PAdvertisement();
		               PipeAdvertisement pipeAdv = ad.getPipeAdvertisement(computername+helper_prefix,P2PService.getPeerGroup());
		   			   
		   			   try{
		   				   
		   				  inputPipe = P2PService.getPipeService().createInputPipe(pipeAdv, new IncomingMessage(null) );
		   				    //inputPipe = P2PService.getPipeService().createInputPipe(pipeAdv );
		   				  log.debug("waiting input pipe");

		   			   }catch(Exception e){
		   				   System.out.println("~~~~~~~~~~~~~FATAL Error P2PServiceServlet2$.run()  on inputpipe service................");
		   				   e.printStackTrace();
		   			   }
		   			   
		   			   P2PServiceServlet.pipspin=true;
		               while(P2PServiceServlet.pipspin) {
		                  try {
		                      sleep(sleepy);		                   
		                  }
		                  catch(InterruptedException e) {log.error("Waiting thread intrupted...........");}
		               }
		            }
		        };
		        jxtaInpThread.start();
		        
		        TimerTask tt=new TimerTask() {
		            public void run() {
		               // System.out.format("Time's up!%n");
		            	if(!jxtaInpThread.isAlive()){
		            		//Logger log=Logger.getLogger(P2PServiceServlet.class);
		            		//log.error("jxtaInpThread is not alive restarting the tread");
		            		jxtaInpThread.start();
		            	}
		            }
		        };
		        //checks every 1 minute to see if a thread still running....
		        long freq=90*1000;
		        timer = new Timer();		        
		        timer.scheduleAtFixedRate(tt,freq, freq);
		        
	        
		        TimerTask dbCleaner=new TimerTask() {
		        	Logger log = LogManager.getLogger(P2PServiceServlet.class.getName());
		            public void run() {
		            	
		            	int db_close_timeout = SuperDB.getDbCloseTimeout();
		            	
		            	UtilDB udb=UtilDB.getUtilDB4SQLServer();
		        		try{
		        			
		        			SuperDB.killConnections(db_close_timeout);
		        			Thread.sleep(300);
		        			udb.connectDB(getUtilDBName());
		        			ArrayList al=udb.showOldConnections(db_close_timeout,P2PService.getComputerName());
		        			if(al != null && al.size()>0){
		        				
		        				
		        				
		        				
		        				log.error("~~~~~~Killing "+al.size()+" physical connections in database after "+db_close_timeout+" minutes");
		        				log.error("===While killing the following found left unclosed==");
		        				log.error(SchedulerDB.collectStack4All());
		        				log.error("=====");
		        				
		        				
		        				udb.killConnections(al);
		        			}
		        			
		        		}catch(Exception e){
		        			
		        			
		        		}finally{
		        			try{
		        			udb.closeDB();
		        			}catch(Exception e){}
		        		}

		            }
		        };
		         
		        //checks every 5 minute to see if a thread still running....
		        long fivemins=60*1000*5;
		        Timer dbtimer = new Timer();		        
		        dbtimer.scheduleAtFixedRate(dbCleaner,fivemins, fivemins);
		        
		        
		        if(Config.getValue("load_balancing_server")!=null && Config.getValue("load_balancing_server").equals(P2PService.getComputerName())){
		        	ExecutingQueueCleaner.clean(); //clean up the dead tasks in every 30 second.
		        }
			  }catch(Exception e){
				e.printStackTrace();  
			  }
	}
	 
	
 
	
	private static MessageBean parseMessage(String msg) throws Exception  {
		
		  Digester digester = new Digester();
		  digester.setValidating(false);
		 		  
		  digester.addObjectCreate("message", MessageBean.class);
		  digester.addBeanPropertySetter("message/type");
		  digester.addBeanPropertySetter("message/command");
		  digester.addBeanPropertySetter("message/sender");
		  digester.addBeanPropertySetter("message/reply");
		  MessageBean ve= (MessageBean) digester.parse(new StringReader(msg));
        return ve;		
	}
	
	private void updatePeerName()  {
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();			
	    try{
	    	String peername=P2PService.getComputerName();
	    	sdb.connectDB();	 
	    	sdb.updatePeersList(peername);
	    }catch(Exception e){
	    	e.printStackTrace();
	    }finally{
	    	try{
	    	sdb.closeDB();
	    	}catch(Exception e1){}
	    }
	}
	
	private void capturePackageVersions() throws Exception {
					
		RFunctionDB rfdb=RFunctionDB .getRFunctionDB();
		int loc=0;
		try {
			
			String peername=P2PService.getComputerName();
			rfdb.connectDB();
			loc=100;
			
			LinkedHashMap<String , String> p=new LinkedHashMap<String, String>();
			String rlib_path=System.getenv("R_HOME");
			rlib_path+=rlib_path.endsWith(File.separator)?"library":File.separator+"library";
			File[] libs=new File(rlib_path).listFiles();
			int count=0; 
			ArrayList plist=new ArrayList(); 
			loc=200;
			for(int i=0;i<libs.length;i++){
				File lib=new File(libs[i].getPath()+File.separator+"DESCRIPTION");
				loc=300;
				if(lib.exists()){
					loc=400;
					try{
						StringWriter stringWriter = new StringWriter();
						IOUtils.copy(new FileInputStream(lib), stringWriter);					
						StringTokenizer st=new StringTokenizer(stringWriter.toString(),"\n\r");					
						PeerPackage pp=new PeerPackage(); 
						pp.setPackagename(libs[i].getName());
						pp.setLastchecked(new Date());
						loc=500;
						while(st.hasMoreTokens()){
							String line=st.nextToken();						
							if(line.startsWith("Version:") && line.split(":").length>1){
								pp.setVersion(line.split(":")[1]);
							}
						}
						plist.add(pp);
					}catch(Exception e){
						log.error("Error while getting info of package "+libs[i].getName());
					}
				}
			}
			rfdb.updatePeerPackage(peername, plist);
			log.debug("peername:"+peername+" plist:"+plist);
			
		}catch(Exception e){			
			log.error("Error ,MSG:"+e.getMessage()+" loc:"+loc);
			//e.printStackTrace();
			throw e;
		}finally{
			rfdb.closeDB();
		}
		
		
		
		
		
	}
	
	
	/*
	public void pipeMsgEvent(PipeMsgEvent event) {
		   
		   Message msg;
	       try {
	            // Obtain the message from the event
	        	//System.out.println("pipeMsgEvent() called");
	            msg = event.getMessage();
	            
	            
	            if (msg == null) {
	                System.out.println("Received an empty message");
	                return;
	            }
	            
	       } catch (Exception e) {
	            e.printStackTrace();
	            return;
	       }

	        // get all the message elements
	       Message.ElementIterator en = msg.getMessageElements();

	       if (!en.hasNext()) {
	            return;
	       }
	        // get the message element in the name space PipeClient.MESSAGE_NAME_SPACE
	       MessageElement msgElement = msg.getMessageElement(null, P2PService.MESSAGE_NAME_SPACE);
	       P2PService.messages.add(msgElement.toString());
	       String message=msgElement.toString();
	        // Get message
	         */
	
	       /*
	       if(message.startsWith("[REPLYTO:")){
	    	   String replytoPart=message.substring(0,"]");
	    	   PeerGroup netPeerGroup =  P2PService.getPeerGroup();
			   PipeAdvertisement pipeAdv = new P2PAdvertisement().getPipeAdvertisement("Renee",netPeerGroup);
				//OutgoingMessageCompose omc=new OutgoingMessageCompose(){
				//};		
				OutgoingMessage ogM=new OutgoingMessage(null,"Your question will be answereed soon");
				PipeService pipeService = P2PService.getPipeService();
				try{
					pipeService.createOutputPipe(pipeAdv,ogM);
				}catch(Exception e){
					e.printStackTrace();
				}
	    	   
	       }
	        */
	      /*
	       if (msgElement.toString() == null) {
	            System.out.println("null msg received");
	       } else {
	            Date date = new Date(System.currentTimeMillis());
	            System.out.println("Message received at :" + date.toString());
	            System.out.println("Message  created at :" + msgElement.toString());	     
	       }
	       
	}
	
	*/

		
	 
	public void destroy() {
	   jxtaInpThread.interrupt();	   
	   P2PServiceServlet.pipspin=false;
	   if(timer!=null){
		   timer.cancel();
	   }
	   P2PService.stopNetwork();
	}
	
	private String getUtilDBName() {
		//String db = "master";
		//if ("mariadb".equalsIgnoreCase( SuperDB.getDbType() )) {
		//	db = "mysql";
		//}		
		String db = "mysql";
		return db;
	}	
	
}


