/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

/*package com.fourelementscapital.p2p;*/
package com.fourelementscapital.scheduler.servlet;

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

import com.fourelementscapital.loadbalance.PeerClient;
import com.fourelementscapital.loadbalance.ServerScheduler;
import com.fourelementscapital.db.RFunctionDB;
import com.fourelementscapital.db.SchedulerDB;
import com.fourelementscapital.db.SuperDB;
import com.fourelementscapital.db.UtilDB;
import com.fourelementscapital.db.vo.PeerPackage;
import com.fourelementscapital.loadbalance.ExecutingQueueCleaner;
import com.fourelementscapital.config.Config;
import com.fourelementscapital.group.RScriptScheduledTask;
import com.fourelementscapital.p2p.MessageBean;
import com.fourelementscapital.p2p.P2PAdvertisement;
import com.fourelementscapital.p2p.P2PService;
import com.fourelementscapital.p2p.listener.IncomingMessage;

@SuppressWarnings({"unchecked","deprecation"})
public class P2PServiceServlet  extends HttpServlet  {

	private static Thread jxtaInpThread=null;
	private InputPipe inputPipe=null;
    private static volatile boolean pipspin;
    private   Timer timer=null; 
    private Logger log = LogManager.getLogger(P2PServiceServlet.class.getName());
	public static final String helper_prefix="_helper";
    
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
			print("JXTA started");
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
		P2PService.removeCache(); //the .cache folder will be locked when peer_helper is started
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
				
				System.out.println("Starting network....................................");
				java.util.logging.Logger.getLogger("net.jxta").setLevel(Level.SEVERE);
				DiscoveryService discovery = P2PService.getDiscoveryService();
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
		            	if(!jxtaInpThread.isAlive()){
		            		jxtaInpThread.start();
		            	}
		            }
		        };
		        String tname="jett_thread_"+(new Date().getTime()/1000);
		    	if(Config.getValue("load_balancing_server")!=null && Config.getValue("load_balancing_server").equals(P2PService.getComputerName())){
		    		ExecutingQueueCleaner.clean(); //clean up the dead tasks in every 30 second.		    		
		    	} 
		    	
		    	/*
		    	 * launching NIO server
		    	 * 
		    	 */
		    	int port =1818;
		    	if(Config.getValue("nio.server.mode")!=null && Config.getValue("nio.server.mode").trim().equalsIgnoreCase("true") && 
		    			Config.getValue("nio.server.port")!=null && !Config.getValue("nio.server.port").trim().equals("") 	
		    	){
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
				    	String server=Config.getValue("nio.server.address");
			    		try{
			    			port=Integer.parseInt(Config.getValue("nio.server.port"));
			    		}catch(Exception e){}
			    		log.debug("server:"+server+" port:"+port+" tname:"+tname);
			    		Thread t=new Thread(new PeerClient(server, port),tname);
			    		log.debug("t:"+t);
			            t.start();
			            log.debug("started:");
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
		            	if(!jxtaInpThread.isAlive()){
		            		jxtaInpThread.start();
		            	}
		            }
		        };
		        if(Config.getValue("load_balancing_server")!=null && Config.getValue("load_balancing_server").equals(P2PService.getComputerName())){
		        	ExecutingQueueCleaner.clean(); //clean up the dead tasks in every 30 second.
		        }
			  }catch(Exception e){
				e.printStackTrace();  
			  }
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
		try {
			String peername=P2PService.getComputerName();
			rfdb.connectDB();
			LinkedHashMap<String , String> p=new LinkedHashMap<String, String>();
			String rlib_path=System.getenv("R_HOME");
			rlib_path+=rlib_path.endsWith(File.separator)?"library":File.separator+"library";
			File[] libs=new File(rlib_path).listFiles();
			int count=0; 
			ArrayList plist=new ArrayList(); 
			for(int i=0;i<libs.length;i++){
				File lib=new File(libs[i].getPath()+File.separator+"DESCRIPTION");
				if(lib.exists()){
					try{
						StringWriter stringWriter = new StringWriter();
						IOUtils.copy(new FileInputStream(lib), stringWriter);					
						StringTokenizer st=new StringTokenizer(stringWriter.toString(),"\n\r");					
						PeerPackage pp=new PeerPackage(); 
						pp.setPackagename(libs[i].getName());
						pp.setLastchecked(new Date());
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
			log.error("Error ,MSG:"+e.getMessage());
			//e.printStackTrace();
			throw e;
		}finally{
			rfdb.closeDB();
		}
	}
	 
	public void destroy() {
	   jxtaInpThread.interrupt();	   
	   P2PServiceServlet.pipspin=false;
	   if(timer!=null){
		   timer.cancel();
	   }
	   P2PService.stopNetwork();
	}
}