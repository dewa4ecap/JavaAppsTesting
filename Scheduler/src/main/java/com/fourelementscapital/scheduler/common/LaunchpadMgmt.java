/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.jcs.JCS;
import org.apache.commons.jcs.access.CacheAccess;
import org.apache.commons.jcs.engine.behavior.IElementAttributes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.fourelementscapital.config.Config;
import com.fourelementscapital.db.SchedulerDB;
import com.fourelementscapital.error.ClientError;
import com.fourelementscapital.ldap.LDAP;
import com.fourelementscapital.loadbalance.SchedulerEngine;
import com.fourelementscapital.p2p.P2PService;
import com.fourelementscapital.p2p.peer.PeerMachine;
import com.fourelementscapital.p2p.peer.PeerManagerHSQL;
import com.fourelementscapital.sha.SHA;

import com.fourelementscapital.loadbalance.InstantPeerStatus;
import com.fourelementscapital.loadbalance.LoadBalancingQueue;

@SuppressWarnings("unchecked")
public class LaunchpadMgmt {
	
	private Logger log = LogManager.getLogger(SchedulerMgmt.class.getName());
	private static String LOG_STATUS_RESTARTED="re-executed";
	public JSONObject jsonObj;
	
	/**
	 * This constructor called from DWR
	 * @throws Exception
	 */
	public LaunchpadMgmt() throws Exception {
		jsonObj = new JSONObject();
	}
	
	/**
	 * Method to get contents of pre and post strategy script line by line
	 * @param fileName : specify script file path   
	 * @return String : contents of pre / post config strategy script
	 * @throws Exception
	 */
	public String getConfigContent(String fileName) throws Exception {
		String fResult = "";
		String readLine = "";
		File file = new File(fileName);
		try {
			BufferedReader b = new BufferedReader(new FileReader(file));
	        while ((readLine = b.readLine()) != null) { fResult = fResult + readLine + ";"; }
		} catch (Exception e) {
			//log.error("Error while reading config's content");
			e.printStackTrace();
		}
		System.out.println("fResult : " + fResult);
		return (fResult.equals("") ? fResult : fResult.substring(0,fResult.length()-1));
	}
	
	/**
	 * Method to get Backtest Result of executed Strategy from Peer
	 * @param scheduler_id : executed strategy's ID    
	 * @return String[2] : String[0] is backtest result file name, String[1] is the contents of that backtest result file
	 * @throws Exception
	 */
	public String[] getBacktestResult(int scheduler_id, String validUser) throws Exception {
		String[] result = new String[2];
		String fResult = "";
		boolean validFile = false;
		
		try {
			SchedulerDB sdb=SchedulerDB.getSchedulerDB();
			sdb.connectDB();
			result[0] = sdb.checkCompletedSignalFromPeer(scheduler_id);
			sdb.closeDB();
			
			String prefixPath=Config.getString("backtest_result_prefix_path");
			
			if (result[0]!=null) { // this condition runs if only Server has received completed signal from Peer
				if (result[0].contains("success")) { //success
					try {
						File folder = new File(prefixPath+validUser+"/plot/");
						
						while (!validFile) {
							
							if (folder.listFiles().length < 1) { //executed scripts don't have backtest result
								result[0] = "";
								result[1] = "";
								validFile = true;
							} else {
								for (final File fileEntry : folder.listFiles()) { //only one file
									String resPath = fileEntry.getPath();
									
									if((resPath.contains(".html"))||(resPath.contains(".png"))) {
										
										//copy html / png file to accessable location
										SHA sha = new SHA();
										
										String randomFileName = sha.getRandomSHA256Hash(128);
										result[1] = randomFileName + (resPath.contains(".html") ? ".html" : ".png");
										File dest = new File("/var/lib/tomcat7/webapps/scheduler/backtest/" + randomFileName + (resPath.contains(".html") ? ".html" : ".png"));
										
										try {
											if (!dest.exists() && !dest.isDirectory()) { //check file existence
												try {
												    FileUtils.copyFile(fileEntry, dest);
												} catch (IOException e) {
												    log.error("Error while copy backtest result to accessable location");
												    e.printStackTrace();
												}
											}
										} catch (Exception e) {
											log.error("Error while check existing backtest file");
										}
										
										File rFile = new File(resPath);
										BufferedReader b = new BufferedReader(new FileReader(rFile));

							            String readLine = "";

							            while ((readLine = b.readLine()) != null) {
							                fResult = fResult + readLine;
							            }
							            jsonObj.put("backtest", fResult);
							            result[0] = jsonObj.toString();
							            
							            validFile = true;
									} else {
										log.error("No html / png file found !");
									}
							    }
							}
						}
					} catch (Exception e) {
						result[0] = "";
						result[1] = "";
						log.error("Your respective directory not found !");
					}
				} else { 
					//fail , not doing anything
					result[0] = "";
					result[1] = "";
				}
			}
			
		} catch (Exception e) {
			log.error("Error while get backtest result from launchpad ");
		}
		return result; 
	}
	
	/**
	 * Method to get Console Result of executed Strategy from Peer
	 * @param scheduler_id : executed strategy's ID   
	 * @return String : console result of executed Strategy
	 * @throws Exception
	 */	
	public String getConsole2ndScheduler(int scheduler_id) throws Exception {
		//String[] consoleMessage = new String[2];
		String consoleMessage = "";
		
		try {
			SchedulerDB sdb=SchedulerDB.getSchedulerDB();
			sdb.connectDB();
			consoleMessage = sdb.getConsoleMsg(scheduler_id);
			sdb.closeDB();
			if (consoleMessage!=null) {
				if (consoleMessage.equals("")) {
					//Rscript contains syntax error
					jsonObj.put("consoleoutput", "Result is Empty. Please check the Peer");
					consoleMessage = jsonObj.toString();
				} else {
					jsonObj.put("consoleoutput", consoleMessage);
					consoleMessage = jsonObj.toString();
				}
				
			}
			
			//System.out.println("===== " + getElapsedTimeFromExecutingPeer(scheduler_id));
			
			
		} catch (Exception e) {
			log.error("Error while get console message from launchpad ");
		}
		
		return consoleMessage;
	}
	
	/**
	 * Method to send Strategy to Peer for execution
	 * @param scheduler_id : executed strategy's ID, log_id : strategy's log ID, username : user that executed this strategy   
	 * @return scheduler_id
	 * @throws Exception
	 */	
	public int executeTask2ndScheduler(int scheduler_id, int log_id, String username) throws Exception {
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		try{
    			sdb.connectDB();
    			if(log_id>0) { sdb.updateQueueLogStatus(log_id,LOG_STATUS_RESTARTED, P2PService.getComputerName()); }
    			Map data=sdb.getScheduler(scheduler_id);
    			
    			String taskuid=(String)data.get("taskuid");
    			String name=(String)data.get("name");
    			String user=username;
    			//String inject_code=null;
    			String inject_code="";
    			
    			System.out.println("Name : " + name);
    			System.out.println("Taskuid : " + taskuid);
    			System.out.println("User : " + user);
    			System.out.println("inject code : " + inject_code);
    			System.out.println("Data : " + (data == null ? "null" : "not null" ));
    			
    			new SchedulerEngine().executeJobNow(name, taskuid,data,sdb,user,inject_code);

	    		return scheduler_id;
		}catch(Exception e){
			e.printStackTrace();
			ClientError.reportError(e, null);
			throw e;
		}finally {
			sdb.closeDB();
		}
	}
	
	/**
	 * Method to query Strategy for execution
	 * @param rscript : Strategy's script, process : 0 - Run Strategy; 1 - Submit Strategy, username : user that executed this strategy   
	 * @return scheduler_id
	 * @throws Exception
	 */	
	public Integer addScriptFrom2ndScheduler(String rscript, int process, String username, String script_description) throws Exception {
		
		int owner_tag_id = 0;
		Integer id = 0;
		Map<String,String> data = new HashMap<String, String>();
		String finalScript = "";
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		
		//delete old result from user's plot directory
/*		try {
			File folder = new File("/mnt/public/Alphien/Temp/Backtest/" + username + "/plot/");
			 
			for (final File fileEntry : folder.listFiles()) {
				String resPath = fileEntry.getPath();
				
				System.out.println(resPath);
				
				File rFile = new File(resPath);
				rFile.delete();
				
		    }
    	} catch(Exception e){
    		log.error("Error while delete old result from user's plot directory !");
    		e.printStackTrace();
    	}*/
		
		try {
			String prefixPath=Config.getString("backtest_result_prefix_path");
			File folder = new File("/mnt/public/Alphien/Temp/Backtest/" + username + "/plot/");
			//File folder = new File(prefixPath+username+"/plot/");
			 
			FileUtils.cleanDirectory(folder);
			
    	} catch(Exception e){
    		log.error("Error while delete old result from user's plot directory !");
    		e.printStackTrace();
    	}
    	
		//get user's tag ID
		try {
			sdb.connectDB();
			owner_tag_id = sdb.addIfTagNotExist("usr-"+username);
		} catch (Exception e) {
			log.error("Error while check user in bbsync.tags table");
			e.printStackTrace();
		}
		
		//prepare to submit Strategy to Peer
		data.put("owner_tag_id",String.valueOf(owner_tag_id));
		data.put("name","2nd-Schd-" + username + "-" + Long.toString(new Date().getTime()));
		data.put("folder_id","0");
		data.put("calling_another_script","0");
		data.put("is_wiki_done","0");
		data.put("active","0");
		data.put("taskuid",Config.getString("launchpad_peer_group"));
		data.put("timezone","");

		//System.out.println("PRE : " + Config.getString("launchpad_pre_run_config"));
		//System.out.println("POST : " + Config.getString("launchpad_post_run_config"));
		
		//create finalScript from pre, strategy and post script. Process == 0 means user hit run button, process == 1 means submit button
		finalScript = getConfigContent(Config.getString((process == 0 ? "launchpad_pre_run_config" : "launchpad_pre_submit_config"))) + 
				";" + rscript.trim() + ";" + getConfigContent(Config.getString((process == 0 ? "launchpad_post_run_config" : "launchpad_post_submit_config"))); 
		System.out.println("final script : " + finalScript);
		//LDAP process
		try {
			LDAP ldap = new LDAP();
			String repUsername = finalScript.replace("${username}", username);
			String repUID = repUsername.replace("${uidNumber}", ldap.getAttr(username, "uid"));
			String repGID = repUID.replace("${gidNumber}", ldap.getAttr(username, "gid"));
			finalScript = repGID;
		} catch (Exception e) {
			log.error("Error in LDAP Process !");
			e.printStackTrace();
		}
		
		//Process below is only for submit button
		if(process==1 && !script_description.equals("")) {
			String repSubmit = finalScript.replace("${description}", script_description);
			data.put("rscript",repSubmit);
		} else {
			data.put("rscript",finalScript);
		}
		
		//query Strategy for execution. Update bbsync.scheduler_taskdata table
		try {
			id = sdb.addOrUpdateSchedulerGetId(0, data, Config.getString("launchpad_peer_group"));
			sdb.updateLast2UsersTag(id,owner_tag_id);
			sdb.addOrUpdateSchedulerInsertTaskdata(id, data, "name");
			sdb.addOrUpdateSchedulerInsertTaskdata(id, data, "rscript");
			sdb.closeDB();
			
		} catch (Exception e) {
			log.error("Error while update bbsync.scheduler_taskdata table");
			e.printStackTrace();
		} 
		
		return id;
	}
	
	/**
	 * Method to get SSO Session Expiry Date Time 
	 * @param sid : ALSID cookies from user's browser   
	 * @return session expiry date time
	 * @throws Exception
	 */	
	public Date getExpiryDate(String sid) throws Exception {
		
		Date timeFromDB = null;
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		
		try{
			sdb.connectDB("sso");
			Map map = sdb.getExpiryDate(sid);			
			if ((map!=null && map.size()>0)){ timeFromDB = (Timestamp) map.get("expiryDatetime"); }
		} catch(Exception e) {
			log.error("Error while get Expiry Date");
		}
		return timeFromDB;
	}
	
	
/*	public String getElapsedTimeFromExecutingPeer(int scheduler_id) throws Exception {
		
		String getTime = "";
		InstantPeerStatus ips = new InstantPeerStatus();
		Map<String,String> result = new HashMap<String,String>();
		result = ips.executeAtDestination(); // ==> result NULL
		
		for ( String key : result.keySet() ) {
		    System.out.println( key );
		    if (key.equals(""+scheduler_id)) {
		    	getTime = result.get(key);
		    }
			
			getTime = getTime + "Key : " + key + "\n Value : " + result.get(key) + "\n\n";
		}
		
		return getTime;
	}*/
	
	
	private static CacheAccess cache=null;
	
	private static CacheAccess getCache() throws Exception {
		 if(cache==null){
				cache=JCS.getInstance("direct_script");
		 }
		 return cache;
	}
	
	public Map getOnlinePeers() throws Exception {
		if(cache==null){
			cache=JCS.getInstance("onlinePeers");
		}	 
		IElementAttributes att= cache.getDefaultElementAttributes();
		att.setMaxLife(2);
		IElementAttributes att10= cache.getDefaultElementAttributes();
		att10.setMaxLife(5);
		Map peers1=new HashMap();
		if(cache.get("peers")==null){
			String status="NOBUSY";
			int last5sec=5000;
			List<PeerMachine> online=new PeerManagerHSQL().getOnlinePeers(last5sec );
			for(PeerMachine p:online){
				peers1.put(p.getPeername(), p.getBusyStatus());
			}			 
			peers1.put(P2PService.getComputerName(),status);
			log.debug("peers1:"+peers1);
			cache.put("peers",peers1,att);
			if(false){
				cache.put("peersBkup",peers1,att10);  //backup in case of data unavailability.
			}
			LoadBalancingQueue.getDefault().findAndUpdateOnlinePeers();
		}else{
			peers1=(Map)cache.get("peers");
		}
		return peers1;
	}
	
}
