/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.p2p.peer;

import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

public class PeerMachine {

	public PeerMachine(){}
	public PeerMachine(String peer){this.peername=peer;}
	
	private String executing=null;
	private Date lastresponse=null;
	private int num_tasks_running=0;
	private String peername=null;
	private String version="";
	private String peerOS="";  // added by andy 2014-02-20
	
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getExecuting() {
		return executing;
	}
	public void setExecuting(String executing) {
		this.executing = executing;
	}
	public Date getLastresponse() {
		return lastresponse;
	}
	public void setLastresponse(Date lastresponse) {
		this.lastresponse = lastresponse;
	}
	 
	public int getNum_tasks_running() {
		return num_tasks_running;
	}
	public void setNum_tasks_running(int num_tasks_running) {
		this.num_tasks_running = num_tasks_running;
	}
	public String getPeername() {
		return peername;
	}
	public void setPeername(String peername) {
		this.peername = peername;
	}
	
	public void setPeerOS(String peeros){ // added by andy 2014-02-20
		this.peerOS = peeros;
	}
	
	public String getPeerOS(){ // added by andy 2014-02-20
		return peerOS;
	}

	public String getBusyStatus(){
		if(this.executing!=null && !this.executing.trim().equals("")){
			return "BUSY";
		}else{
			return "NOBUSY";
		}
	}
	
	public ArrayList getRunning(){
		ArrayList rtn=new ArrayList();
		if(this.executing!=null && !this.executing.trim().equals("")){
			StringTokenizer st=new StringTokenizer(this.executing,",");
			while(st.hasMoreTokens()){
				String token=st.nextToken();
			    StringTokenizer st2=new StringTokenizer(token,"=");
			    if(st2.countTokens()>=2){
			    	String scid=st2.nextToken();
			    	String trigtime=st2.nextToken();
			    	rtn.add(scid+"_"+trigtime);
			    }
			}
		}
		return rtn;
	}
	
	public boolean equals(Object other){
		boolean rtn=false;
		if(other!=null && other instanceof PeerMachine){
			PeerMachine other1=(PeerMachine)other;
			if(other1.getPeername()!=null && this.peername!=null &&  other1.getPeername().equals(this.peername)){
				rtn=true;
			}
		}
		return rtn;
	}
	
	
	private static ArrayList<String> ver=new ArrayList();	
	public static String getLastVersion(){	

		ver.add("2.1");  //added on 13-june-2013
		
		return ver.get(ver.size()-1);
		
	}
}


