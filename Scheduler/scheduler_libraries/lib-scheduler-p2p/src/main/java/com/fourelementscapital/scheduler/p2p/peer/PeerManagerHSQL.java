/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.p2p.peer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLSyntaxErrorException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PeerManagerHSQL {

		
	private Logger log = LogManager.getLogger(PeerManagerHSQL.class.getName());
	
	private static Connection connection=null;	
	private static final Semaphore dblock=new Semaphore(1,true);
	private static long TIMEOUT_MS=2000;	
	
	protected void acquireLock(){
		
		try{
			Date start=new Date();
			
			PeerManagerHSQL.dblock.tryAcquire(TIMEOUT_MS, TimeUnit.MILLISECONDS);
			Date end=new Date();			
			long diff=end.getTime()-start.getTime();
			if(diff>0){			
				
				String caller=collectStack(Thread.getAllStackTraces().get(Thread.currentThread()));
				//log.debug("!!!!!!!!!!!!!!!!!acquiring lock: thread:"+Thread.currentThread().getId()+" time took:"+diff+" ms");				
				log.debug("                  Caller  time:"+diff+" id:"+Thread.currentThread().getId() +"\n"+caller);
			}
			//LoadBalancingHSQLLayerDB.dblock.acquire();
		}catch(Exception e){
			log.error("Error:"+e.getMessage());
		}
	}
	

	protected void releaseLock(){
		
		try{			
			PeerManagerHSQL.dblock.release();
			//log.debug("....releasing lock: thread:"+Thread.currentThread().getId());
		}catch(Exception e){
			log.error("Error:"+e.getMessage());
		}
	}
	
	

	private static String nextLineChar="\r\n";
	
	private static String collectStack(StackTraceElement[] stacks) throws Exception {
		//StackTraceElement[] stacks=ex.getStackTrace();
		String rtn="";
		//if(msg!=null) rtn+=msg.trim()+nextLineChar;
		//rtn+="ERROR MSG:"+ex.getMessage()+nextLineChar;
		int startat=0;
		for(int i=0;i<stacks.length;i++){
		   if(stacks[i].getClassName().startsWith("com.fe.")){
			   startat=i;
		   }
		}
		int numbefore=2;
		startat=(startat>numbefore)?startat-numbefore:0;
		int counter=1;
		
 
	 
		
		for(int i=startat;i<stacks.length;i++){
			if((i>(startat+numbefore)) && !stacks[i].getClassName().startsWith("com.fe.")){
				
			}else{
				String space="";
				for(int ab=0;ab<counter;ab++) space+=" "; counter++;
				if(stacks[i].getClassName().startsWith("com.fe.")){
					space+="->";
				} 
				rtn+=space+""+stacks[i].getClassName()+"."+stacks[i].getMethodName()+"()"+nextLineChar;
			}			
		}
		return rtn;
	}
	
	
	
	private synchronized Connection getConnection(){
	
	 		
		if(PeerManagerHSQL.connection==null){
			//synchronized(PeerManagerHSQL.connection){
				if(PeerManagerHSQL.connection==null){
						//String driver="jdbc:hsqldb:mem:peers";
					acquireLock();
					try{
						if(PeerManagerHSQL.connection==null){
							log.debug("~~~~~~~~~getConnection(-------)~~~~~~~~~~~~~~");
							Class.forName("org.hsqldb.jdbc.JDBCDriver" );
							//String driver="jdbc:hsqldb:file:C:\\rnd\\hsqldb-2.2.9\\scheduler_peers\\peers";
							String driver="jdbc:hsqldb:mem:peers";
							this.connection = DriverManager.getConnection(driver, "SA", "SA");
							String q=createSchemaQuery();
							Statement st=this.connection.createStatement();
							st.execute(q);
							st.close();
						}
					}catch(SQLSyntaxErrorException sqle){
						log.debug("Table already existing, e:"+sqle.getMessage());
					}catch(Exception e){
						e.printStackTrace();						
					}finally{
						releaseLock();
					}
				}
			//}
		}
		return PeerManagerHSQL.connection;
	}
	
	private String createSchemaQuery(){
		String query="CREATE TABLE peers(";
		query+=" id IDENTITY,";
		query+=" peername VARCHAR(50),";
		query+=" executing VARCHAR(250),";
		query+=" lastresponse DATETIME,";
		query+=" version VARCHAR(50),";
		query+=" num_tasks_running INT ";
		query+=")";		
		return query;
	}
	
	public List<PeerMachine> getOnlinePeers(int last_milli_sec){
		//i guess no need to lock
		//acquireLock();
		ArrayList rtn=new ArrayList();
		try{
			String query="Select * from peers WHERE lastresponse>dateadd('ms',-"+last_milli_sec+",LOCALTIMESTAMP)";
			PreparedStatement ps=getConnection().prepareStatement(query);
			ResultSet rs=ps.executeQuery();
			
			while(rs.next()){
				PeerMachine pm=new PeerMachine();
				pm.setExecuting(rs.getString("executing"));
				pm.setPeername(rs.getString("peername"));
				pm.setVersion(rs.getString("version"));
				pm.setNum_tasks_running(rs.getInt("num_tasks_running"));
				if(rs.getTimestamp("lastresponse")!=null){
					Date d=new Date(rs.getTimestamp("lastresponse").getTime());			
					pm.setLastresponse(d);
				}
				rtn.add(pm);
			}
			rs.close();
			ps.close();		
		}catch(Exception e){
			log.error("getOnlinePeers:"+last_milli_sec);
			e.printStackTrace();
		}finally{
			//releaseLock();
		}
		return rtn;
	}
	
	
	
	
	public void updatePeerResponse(String peername, Map executing,String version) {
		acquireLock();
		try{
			String selq="Select * from peers where peername=?";		
			String strfy=null;
			int num_running=0;
			if(executing!=null){
				num_running=executing.size();
				for(Object sc_id: executing.keySet()){				
					strfy=(strfy==null)
							?sc_id+"="+executing.get(sc_id)
							:strfy+","+sc_id+"="+executing.get(sc_id);
				}
			}
			PreparedStatement ps=this.getConnection().prepareStatement(selq);
			ps.setString(1, peername);
			ResultSet rs=ps.executeQuery();
			String query="";
			if(rs.next()){
				query="UPDATE peers SET executing=?, lastresponse=?,num_tasks_running=?, version=? WHERE peername=?";
			}else{
				query="INSERT INTO peers(executing,lastresponse,num_tasks_running,version,peername) VALUES(?,?,?,?,?)";
			}
			rs.close();
			ps.close();		
			
			PreparedStatement ps1=getConnection().prepareStatement(query);
			ps1.setString(1,strfy);
			
			//try catch block added because occasionally get the following error message. 
			//java.lang.IllegalArgumentException: YEAR
	        //at java.util.GregorianCalendar.computeTime(GregorianCalendar.java:2316)
			try{				
				ps1.setTimestamp(2,new Timestamp(new Date().getTime()));
			}catch(Exception e){;
				log.error("error, e:"+e.getMessage());
				ps1.setTimestamp(2,null);
			}
			ps1.setInt(3,num_running);
			ps1.setString(4,version);
			ps1.setString(5,peername);
			ps1.executeUpdate();
			ps1.close();
			
		}catch(Exception e){
			log.error("Executing:"+executing+" Error:"+e.getMessage());
			//e.printStackTrace();
		}finally{
			releaseLock();
		}
		
	}
	
}


