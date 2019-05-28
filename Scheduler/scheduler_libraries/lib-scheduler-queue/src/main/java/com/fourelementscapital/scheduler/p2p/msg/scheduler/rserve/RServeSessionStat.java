/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.p2p.msg.scheduler.rserve;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.jcs.JCS;
import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.fourelementscapital.scheduler.p2p.msg.MessageHandler;
import com.fourelementscapital.scheduler.p2p.msg.PostCallBack;
import com.fourelementscapital.scheduler.rserve.RServeConnectionPool;
import com.fourelementscapital.scheduler.rserve.RServeSession;

public class RServeSessionStat extends MessageHandler implements PostCallBack {
	
	
	private Logger log = LogManager.getLogger(RServeSessionStat.class.getName());
	
	
	public Map executeAtDestination() {
		
		HashMap h=new HashMap();
		List<RServeSession> all=RServeConnectionPool.getAllSessions();
		ArrayList<RServeSession> remove_s=new ArrayList<RServeSession> ();
		for(RServeSession rs: all){

			try {
				
				int process_id=rs.getProcessid();
				Process p=Runtime.getRuntime().exec("ps -p "+process_id+" -o %cpu,%mem --no-header");			
				String stat=processOutput(p);
				
				StringTokenizer st=new StringTokenizer(stat," ");
				JSONObject obj = new JSONObject();				
				
				if(st.countTokens()>=2){
					obj.put("CPU", st.nextToken());
					obj.put("Memeory", st.nextToken());
					obj.put("scheduler_id", rs.getScheduler_id());
					obj.put("trigger_time", rs.getTrigger_time());
					obj.put("no_executions", rs.getNoexecutions());
					obj.put("process_id", process_id);
					obj.put("scriptname", rs.getScriptname());
					obj.put("uid", rs.getUid());
					h.put(rs.getUid(), obj.toString());
				}else{
					log.error("error, the process doesn't exisit and removing process id:"+process_id+" from connection pool");
					RServeConnectionPool.remove(rs);
				}

				
			} catch (IOException e) {				
				e.printStackTrace();
			} catch (Exception e) {
				
				e.printStackTrace();
			}
		}
		 
		 
		return h;
		
	}

	
	private String processOutput(Process p) throws Exception {
	 
		InputStream inputStream = p.getInputStream();
		InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
		BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

		String line;
		int linecount=0;
		String rtn="";
		while ((line = bufferedReader.readLine()) != null)
		{
			rtn+=rtn.equals("")?line:"|"+line;
			linecount++;
		    
		}
		return rtn;
		
		
	}
	
	
	public void callBack(Map data) {
		 //System.out.println("RServeSessionQuery.callBack()....");
		try{
			IElementAttributes att= getCache().getDefaultElementAttributes();
			att.setMaxLifeSeconds(5);
			getCache().put("peer_"+this.getMsgCreator(),data,att);
		}catch(Exception e){
			log.error("error,e:"+e.getMessage());
		}
		// for(Object obj: data.values()){
		//	 System.out.println("----"+obj+" msg creator:"+this.getMsgCreator()+" recipient:"+this.getMsgRecipient());
		//}
		
	}

	public static Map getPeerCachedStat(String peername) throws Exception {		
		Map data=(Map)getCache().get("peer_"+peername);
		return data;
		
	}
	
	
	private static JCS cache=null;
	private static JCS getCache() throws Exception {
		 if(cache==null){
				cache=JCS.getInstance(RServeSessionStat.class.getName());
		 }
		 return cache;
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


