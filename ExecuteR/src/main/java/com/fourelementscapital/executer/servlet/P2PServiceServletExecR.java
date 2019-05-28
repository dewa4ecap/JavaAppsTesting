/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

/*package com.fourelementscapital.p2p;*/
package com.fourelementscapital.executer.servlet;

import java.io.StringReader;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.commons.digester.Digester;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.SimpleMessage;

import com.fourelementscapital.loadbalance.ExecutingQueueCleaner;
import com.fourelementscapital.loadbalance.ServerScheduler;
import com.fourelementscapital.config.Config;
import com.fourelementscapital.group.RScriptScheduledTask;
import com.fourelementscapital.p2p.MessageBean;
import com.fourelementscapital.p2p.P2PAdvertisement;
import com.fourelementscapital.p2p.P2PService;
import com.fourelementscapital.p2p.listener.IncomingMessage;

import net.jxta.discovery.DiscoveryService;
import net.jxta.pipe.InputPipe;
import net.jxta.protocol.PeerAdvertisement;
import net.jxta.protocol.PipeAdvertisement;

@SuppressWarnings("deprecation")
public class P2PServiceServletExecR  extends HttpServlet  {

	private boolean isExecuteRPeer = false;

	private static Thread jxtaInpThread=null;
	private InputPipe inputPipe=null;
    private static volatile boolean pipspin;
    private   Timer timer=null; 
    private Logger log = LogManager.getLogger(P2PServiceServletExecR.class.getName());
    
	@Override
	public void init(ServletConfig servletConfig ) throws ServletException {
		super.init(servletConfig);
		start();
	}
	
	private void print(String pr) {
		System.out.println("P2PServiceServletExecR:"+pr);
	}
		
	public void start() {
			
			P2PService.removeCache(); // for the server
			startJxta();
			print("JXTA started");
			
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
	
	private void startJxta(){
		 try {
				
				System.out.println("starting network....................................");

				java.util.logging.Logger.getLogger("net.jxta").setLevel(Level.SEVERE);
				
				DiscoveryService discovery = P2PService.getDiscoveryService();
				
				long waittime =  10 * 1000L;
				 
				String computername=P2PService.getComputerName();		
				
				P2PAdvertisement ad=new P2PAdvertisement();
				
				PeerAdvertisement mdadv = ad.getPeerAdvertisement(computername,P2PService.getPeerGroup());
				
				discovery.publish(mdadv,DiscoveryService.NO_EXPIRATION,DiscoveryService.NO_EXPIRATION);
				discovery.remotePublish(mdadv,DiscoveryService.NO_EXPIRATION);

				jxtaInpThread=new Thread("Input poll") {
					
					private Logger log = LogManager.getLogger(P2PServiceServletExecR.class.getName());
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
		   			   
		   			   P2PServiceServletExecR.pipspin=true;
		               while(P2PServiceServletExecR.pipspin) {
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
		    	
		    	//Launching NIO
		    		int port=1818;		    	
		    		try{
		    			port=Integer.parseInt(Config.getValue("nio.server.port"));
		    		}catch(Exception e){}
		    		Thread t=new Thread(new ServerScheduler(port),tname);
		            t.start();
		            System.out.println("--->Starting....in port:"+port);
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
	
	public void destroy() {
	   jxtaInpThread.interrupt();	   
	   P2PServiceServletExecR.pipspin=false;
	   if(timer!=null){
		   timer.cancel();
	   }
	   P2PService.stopNetwork();
	}
}