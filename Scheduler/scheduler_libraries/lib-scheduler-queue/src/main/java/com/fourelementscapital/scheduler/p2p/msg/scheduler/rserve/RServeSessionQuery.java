/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.p2p.msg.scheduler.rserve;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.rosuda.REngine.Rserve.RConnection;

import com.fourelementscapital.scheduler.p2p.msg.MessageHandler;
import com.fourelementscapital.scheduler.p2p.msg.PostCallBack;
import com.fourelementscapital.scheduler.rserve.RServeConnectionPool;
import com.fourelementscapital.scheduler.rserve.RServeSession;

public class RServeSessionQuery extends MessageHandler implements PostCallBack {

 
	private String closeAll="";	
	private int  kill_process=0;
 

	public int getKill_process() {
		return kill_process;
	}

	public void setKill_process(int kill_process) {
		this.kill_process = kill_process;
	}

	public String getCloseAll() {
		return closeAll;
	}

	public void setCloseAll(String closeAll) {
		this.closeAll = closeAll;
	}

	@Override
	public Map executeAtDestination() {
		
		HashMap h=new HashMap();
		List<RServeSession> all=RServeConnectionPool.getAllSessions();
		ArrayList<RServeSession> remove_s=new ArrayList<RServeSession> ();
		for(RServeSession rs: all){

			if(	this.closeAll!=null && this.closeAll.equalsIgnoreCase("yes")) {
				try{
					if(rs.getRconnection()!=null){
						//System.out.println("RServeSessionQuery.exeuteAtDestination()....intrrupting..");
						//rs.getThread().interrupt();
						//System.out.println("RServeSessionQuery.exeuteAtDestination()....stopping..");
						//rs.getThread().stop();
						//System.out.println("RServeSessionQuery.exeuteAtDestination()....connection:..: isConnected:"+rs.getRconnection().isConnected());
						//System.out.println("RServeSessionQuery.exeuteAtDestination()....connection:..: serverShutdown:");
						rs.getRconnection().serverShutdown();
						
					}else{
						RConnection rc=rs.getRsession().attach();
						//System.out.println("RServeSessionQuery.exeuteAtDestination()....connection:..: shutDown():");
						rc.shutdown();						
					}
					
					
					
					//System.out.println("RServeSessionQuery.exeuteAtDestination()....removing conn..");
					RServeConnectionPool.remove(rs);
				}catch(Exception e){
					e.printStackTrace();					
				}
			}else{				
				if(this.kill_process>0 && rs.getProcessid()>0 && this.kill_process==rs.getProcessid()){
					int process_id=rs.getProcessid();
					try{
						//System.out.println("RServeSessionQuery.exeuteAtDestination() killing connection id:"+this.kill_process);
						
						Process p=Runtime.getRuntime().exec("kill -9 " + process_id);
						//Process p=Runtime.getRuntime().exec("ls -lht ");
						rs.setKillmessage("This RServe connectivity killed by force, pid:"+process_id);
						processOutput(p);
						
						String detail="killing sc_id:"+rs.getScheduler_id()+" tri_id:"+rs.getTrigger_time() +" process_id:"+rs.getProcessid();
						h.put(rs.getScheduler_id()+"_"+rs.getTrigger_time(), detail);
						remove_s.add(rs);
					}catch(Exception e){
						e.printStackTrace();
					}
				}else{
					SimpleDateFormat sdf=new SimpleDateFormat("dd-MMM HH:mm:ss");
					String time=(rs.getStarted_time()>0) ? sdf.format(new Date(rs.getStarted_time())) :"";
					String detail="Running sc_id:"+rs.getScheduler_id()+" tri_id:"+rs.getTrigger_time()+" started:"+time +" process_id:"+rs.getProcessid();
					h.put(rs.getUid(), detail);
				}
			}
			
			
		}
		for(RServeSession rs:remove_s){
			RServeConnectionPool.remove(rs);
		}
		//for(Iterator )
		System.out.println("RServeSessionQuery.exeuteAtDestination()....");
		return h;
		
	}

	
	private void processOutput(Process p) throws Exception {
	 
		InputStream inputStream = p.getInputStream();
		InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
		BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

		String line;
		while ((line = bufferedReader.readLine()) != null)
		{
		    System.out.println("RServeSessionQuery. console_output:"+line);
		}
		
		
	}
	
	@Override
	public void callBack(Map data) {
		System.out.println("RServeSessionQuery.callBack()....");
		
		 for(Object obj: data.values()){
			 System.out.println("----"+obj);
		 }
		
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


