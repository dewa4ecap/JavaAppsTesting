/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.engines;

import java.io.File;
import java.io.FileWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.fourelementscapital.db.BBSyncDB;
import com.fourelementscapital.db.InfrastructureDB;
import com.fourelementscapital.fileutils.SplitString;
import com.fourelementscapital.scheduler.config.Config;
import com.fourelementscapital.scheduler.exception.ExceptionWarningNoFullData;

public class BloombergDownloadJob implements Job {

	private Logger log = LogManager.getLogger(BloombergDownloadJob.class.getName());
	
	public static String BB_DOWNLOAD_GROUP="BB_DOWNLOAD_GROUP";
	private Vector<String> uniqueContracts=new Vector<String>();
	
	private boolean invokedByUser=true;
	
	private String schedulerLog=null;
	
	public void execute(JobExecutionContext context) throws JobExecutionException {
		this.invokedByUser=false;
		Map data=(Map)context.getJobDetail().getJobDataMap().get("data");		
		//log.debug("triggered data:"+data);
		int id=(Integer)data.get("id");
		String name=(String)data.get("name");
		Date started=new Date();
		try{
			
			//downloadData(data);
			
		}catch(Exception e){
			//logging
			//externalLog(id,name,started,new Date(), null,null,0,false);
			//ClientErrorMgmt.reportError(e, "error while synchronizing download id:"+data.get("id")+" name:"+data.get("name"));
			
			throw e;
		}
	}
	
	public void setInvokedByScheduler(){
		this.invokedByUser=false;
	}
	
	
	/**
	 * @deprecated
	 * @param bb_syncid
	 * @throws Exception
	 */
	public void rescheduleJobs(long bb_syncid) throws Exception {
		
	}
	
	
	
	private String checkCommoditySpecified(String contract, Map scommodities,HashMap<String,String> mc) {
		String commodity=null;	
	
		//parses data rediction tickers (For Example: CLZ9->CLZ99 )  download CLZ9 ticker and keep it in both CLZ9 and CLZ99  
		
		Pattern pattern = Pattern.compile("^(.*?)->(.*?)$");
		Matcher matcher = pattern.matcher(contract);
		if (matcher.find()) {
			contract = matcher.group(1);
			String copy=matcher.group(2);		

			if(copy!=null){				 
				 //StringTokenizer st=new StringTokenizer(copy,",");
				 //while(st.hasMoreTokens()) list.add(st.nextToken());				 
				String cont=contract;
				if(cont.indexOf("{")>=0 && cont.indexOf("}")>0){
					String comm=cont.substring(cont.indexOf("{")+1,cont.indexOf("}"));		
					cont=cont.replaceAll("\\{"+comm+"\\}", "");						
				}
				mc.put(cont, copy);
			}
		}
		
		if(contract.indexOf("{")>=0 && contract.indexOf("}")>0){
			commodity=contract.substring(contract.indexOf("{")+1,contract.indexOf("}"));		
			contract=contract.replaceAll("\\{"+commodity+"\\}", "");
			scommodities.put(contract, commodity);
		}
		return contract;
		
	}
	
	private ArrayList<ArrayList<String>> splitTickers(ArrayList<String> tickers, int each) {
		 
			 
		 
			
			ArrayList<ArrayList<String>> all=new ArrayList<ArrayList<String>>();
			if(tickers.size()>each) {
				all.add(new ArrayList<String>());
				for(String tick:tickers){
					if((all.get(all.size()-1).size()>each)){
						all.add(new ArrayList<String>());
					}
					all.get(all.size()-1).add(tick);
				}
			}else{
				all.add(tickers);
			}
			return all; 
			/*
			for(ArrayList<String> t1:all){
				System.out.print("\n----------------");
				for(String tick:t1){
					System.out.print(tick +",");
				}
			}
			*/
		
	}
	
	public synchronized void downloadData(Map m,StackFrame sframe) throws Exception {		
		
		int scheduler_id=((Number)sframe.getData().get("id")).intValue();
		long trigger_time=sframe.getTrigger_time();
		
		java.sql.Timestamp startedat=new java.sql.Timestamp(new Date().getTime());	
		
		TreeMap fields=new TreeMap();

		processDate(m);
		
		String date_from=(String)m.get("date_from");
		String date_to=(String)m.get("date_to");		
		String marketsector=(String)m.get("marketsector");
		String commString=(String)m.get("tickers");
				
		ArrayList<String> tickers=new ArrayList<String>();
		List tickers1=SplitString.split(commString);
		HashMap scommodities=new HashMap ();
		
		HashMap<String,String> multiplecopy=new HashMap<String,String>();
		
		for(Iterator<String>i = tickers1.iterator();i.hasNext();){
			String contractraw=i.next();
			tickers.add(checkCommoditySpecified(contractraw,scommodities,multiplecopy));
		}
		
		BBSyncDB syncdb=BBSyncDB.getBBSyncDB();
		syncdb.connectDB();
		
		int id=(Integer)m.get("id");
		ArrayList fmap=syncdb.fieldMapping4BBSync(id);
		for(int i=0;i<fmap.size();i++){
			Map rdata=(Map)fmap.get(i);
			fields.put(rdata.get("bb_field"),rdata.get("db_field"));	
		}
		syncdb.closeDB();

		log.debug("date_from:"+date_from);
		log.debug("date_to:"+date_to);
		log.debug("market sector:"+marketsector);
		log.debug("fields:"+fields.keySet());  //Bloomberg field names i.e "PX_CLOSE"
		log.debug("db fields:"+fields.values());  //friendly field names i.e "Close Price"
		log.debug("tickers:"+tickers);
		
		 
		
		
		boolean is_market_sec=false;
		if(m.get("is_mkt_securitydb")!=null && ((String)m.get("is_mkt_securitydb")).toLowerCase().equals("true")){
			is_market_sec=true;
		}
		
		ArrayList bb_fields=new ArrayList(fields.keySet());
		
		if(date_from!=null && date_to!=null && marketsector!=null && bb_fields.size()>0 && tickers.size()>0){
			
			ArrayList<ArrayList<String>> all=splitTickers(tickers,100);
			for(ArrayList<String> batch:all){
				
				//TreeMap data=new ConnectBloomberg(tickers,date_from,date_to,bb_fields,marketsector,new MigrationMgmt(),true).getMultipleData();				
				//processBBData(null,data,is_market_sec,fields,tickers,scommodities,multiplecopy); //original
				//processBBData(marketsector,data,is_market_sec,fields,tickers,scommodities,multiplecopy); //cloned copy of market sector for example CL_Commdty_CLOSE_Price
				
				new SchedulerExePlanLogs(scheduler_id,trigger_time).log("# of tickers in this request:"+batch.size(),SchedulerExePlanLogs.PEER_OK_BLOOMBERG_DOWNLOAD);
				//TreeMap data=new ConnectBloomberg(batch,date_from,date_to,bb_fields,marketsector,new MigrationMgmt(),true).getMultipleData();				
				//processBBData(null,data,is_market_sec,fields,batch,scommodities,multiplecopy); //original
				//processBBData(marketsector,data,is_market_sec,fields,batch,scommodities,multiplecopy); //cloned copy of market sector for example CL_Commdty_CLOSE_Price
				
			}
			
		}	
		
		//UPDATE CONTRACT LOGS.
		log.debug("unique contracts:"+uniqueContracts);
		
		syncdb.closeDB();
		
		BBSyncDB syncdb1=BBSyncDB.getBBSyncDB();		
		syncdb1.connectDB();
		
		if(uniqueContracts.size()>0 &&  !is_market_sec){
			java.sql.Timestamp lastsync=new java.sql.Timestamp(new Date().getTime());
			syncdb1.updateContractLogs(uniqueContracts,marketsector,lastsync,scommodities,fields.values());
			
		}else if(uniqueContracts.size()>0  && is_market_sec){
			java.sql.Timestamp lastsync=new java.sql.Timestamp(new Date().getTime());
			syncdb1.updateSecuritiesLogs(uniqueContracts,marketsector, lastsync,fields.values());
		}
				
		//update bloomberg counter;
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
		sdf.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));		
	    String japan_time=sdf.format(new Date());	    
	    int count=uniqueContracts.size()* fields.keySet().size();
	    
    	InfrastructureDB infrastructureDB = InfrastructureDB.getInfrastructureDB();
    	infrastructureDB.connectDB();
	    if(count>0){
	    	infrastructureDB.updateBloomberDailyCounter(japan_time, count);
	    }	 
	    infrastructureDB.closeDB();
		
	    //log.debug("job "+name+" excecuted");
		log.debug("after updated unique tickers on ref database");
		
		java.sql.Timestamp endedat=new java.sql.Timestamp(new Date().getTime());
		
		syncdb1.updateTriggeredDate(id, startedat, endedat);
		
		Date st=(Date)m.get("date_from_date");
		Date end=(Date)m.get("date_to_date");
		String name=(String)m.get("name");
		
		log.debug("writting external log");
		
		externalLog(id,name,startedat,endedat,st,end,uniqueContracts.size(),true,syncdb1);
		syncdb1.closeDB();
		
		schedulerLog="";
		
		log.debug("writting scheduler logs");
		
		schedulerLog=schedulerLog+"Date Between "+new SimpleDateFormat("dd-MMM-yy").format(st)+" and "+new SimpleDateFormat("dd-MMM-yy").format(end);
		schedulerLog=schedulerLog+"\nNo of Tickers Requested:"+tickers.size();
		schedulerLog=schedulerLog+"\nNo of Tickers Responded:"+uniqueContracts.size();
		String missingtick=null;
		for(Iterator it=tickers.iterator();it.hasNext();){
			String tick=(String)it.next();
			if(tick!=null && !tick.equals("") && uniqueContracts.indexOf(tick)<0){
				missingtick=(missingtick==null)?tick:missingtick+"\n"+tick;	
			}
		}
		if(missingtick!=null){
			schedulerLog=schedulerLog+"\nNo data found for the following tickers:\n"+missingtick;
			throw new ExceptionWarningNoFullData("No data found for some tickers  ");
		}else{
			schedulerLog=null;
		}
		log.debug("--------------end of downloadData() -----------------------");
 
	}

	public String getSchedulerLog(){
		return this.schedulerLog;
	}
	
	
	

	private void processDate(Map m){
		
 
		String date_option=(String)m.get("date_option");
		int date_recentnumber=(Integer)m.get("date_recentnumber");
		
		Date date_from=null;
		Date date_to=null;
		
		try{
			if(m.get("date_from") instanceof Date){
				date_from=(Date)m.get("date_from");
			}else{
				log.error("date_from is not date instance");
			}
			if(m.get("date_to") instanceof Date){
				date_to=(Date)m.get("date_to");
			}else{
				log.error("date_to is not date instance");
			}
		}catch(Exception e){
			//ClientErrorMgmt.reportError(e, "Error while parsing date, download ID:"+m.get("id"));
			
			throw e;
		}
		
		
		if(date_option.toLowerCase().equals("daterange")){
			 
			
		}else if(date_option.toLowerCase().equals("datefrom")){			 
			date_to=new Date();			 
		}else{			 
			date_to=new Date();			 
			date_from=null;
			Calendar today=Calendar.getInstance();
			today.setTime(date_to);
			boolean processed=false;
			
			if(date_option.toLowerCase().equals("ndays")){
				today.add(Calendar.DAY_OF_YEAR, -(date_recentnumber-1));
				processed=true;				
			}else if(date_option.toLowerCase().equals("nweeks")){
				today.add(Calendar.WEEK_OF_YEAR, -date_recentnumber );
				processed=true;
			}else if(date_option.toLowerCase().equals("nmonths")){
				today.add(Calendar.MONTH, -date_recentnumber );
				processed=true;
			}else if(date_option.toLowerCase().equals("nyears")){
				today.add(Calendar.YEAR, -date_recentnumber );
				processed=true;				
			}
			if(processed){
				date_from=today.getTime();
			}		
			
		}
		
		if(date_from!=null){
			m.put("date_from",new SimpleDateFormat("yyyyMMdd").format((Date)date_from));
			m.put("date_from_date",date_from);
		}
		
		if(date_to!=null){
			
			if(m.get("enddt_adjustment")!=null){
				Integer dadj=(Integer)m.get("enddt_adjustment");				
				Calendar dt=Calendar.getInstance();
				dt.setTime(date_to);
				dt.add(Calendar.DAY_OF_YEAR, dadj);			
				m.put("date_to",new SimpleDateFormat("yyyyMMdd").format(dt.getTime()));
				m.put("date_to_date",dt.getTime());		
			}else{
				m.put("date_to",new SimpleDateFormat("yyyyMMdd").format((Date)date_to));
				m.put("date_to_date",date_to);
			}
		}
		
	}

	
	private void externalLog(int id,String name,Date started, Date ended,Date period_from,Date period_to,long num_contracts, boolean flag ,BBSyncDB syncdb){

		boolean SUCCESS=true;
		boolean FAILED=false;
		 
		//String filename=new File(job.getConfolder()).getParent()+File.separator+"scheduler.log";
		//File file=new File(filename);
		
		String root=Config.getString("log_error_folder");
		
		Date d = new Date();
		String folder =root+"bloomberg_sync";
		if (!new File(folder).isDirectory()) {
			new File(folder).mkdirs();
		}
		String fname=root+"bloomberg_sync"+File.separator+""+new SimpleDateFormat("dd_MM_yyyy").format(d)+".csv";
		File file=new File(fname);		
		
		if (!file.exists()) {	
			try {
				//file.createNewFile();
				FileWriter fw1 = new FileWriter(file,true);
				fw1.append("Date, Time, Duration(mins), Period, # of Tickers, Manual/Scheduler, Status, ID, Download Name \r\n");
				fw1.flush();
				fw1.close();
			}catch(Exception e){
				log.error("error while creating file: Error:"+e.getMessage());
			}
		}
		

		try {
			boolean append = true;
			FileWriter fw = new FileWriter(file,append);
			//String timeformat="dd-MMM hh:mm:ss aaa ";
			SimpleDateFormat timeformat=new SimpleDateFormat("h:mm a");
			
			SimpleDateFormat durationformat =new SimpleDateFormat("mm:ss");
			durationformat.setTimeZone(TimeZone.getTimeZone("GMT"));
			
			SimpleDateFormat dateformat=new SimpleDateFormat("dd MMM-yy");
			
			
			String logmessage="";
			logmessage+=dateformat.format(started)+",";
			logmessage+=timeformat.format(started)+",";
			
			long diff=ended.getTime()-started.getTime();
			logmessage+=durationformat.format(new Date(diff))+",";
			
			
			long mills_per_day = 1000 * 60 * 60 * 24; 

			long day_diff = ( period_to.getTime() - period_from.getTime() ) / mills_per_day;
			logmessage+=(Math.round(day_diff)+1)+" days from "+dateformat.format(period_from)+",";
			
			logmessage+=num_contracts+",";
			
			logmessage+=(this.invokedByUser?"Manual":"Scheduler")+",";
			logmessage+=( flag==SUCCESS?"Success":"Failed")+",";
			logmessage+=id+",";
			logmessage+=name+",";
			
			/*
				logmessage+="ID:"+id;
			    logmessage+="\tDownload Name:"+name;
				logmessage+="\tStarted At:"+new SimpleDateFormat(format).format(started);
			
			if(flag==SUCCESS){
				logmessage+="\tEnded At:"+new SimpleDateFormat(format).format(ended);
				logmessage+="\tStatus:Success";
				
			}else{
				logmessage+="\tStatus:Failed";
			}
			
			if(period_from!=null && period_to!=null){
				logmessage+="\t Date Between("+sf.format(period_from)+" AND "+sf.format(period_to)+")";
			}
			*/
			 
		
			fw.append(logmessage+"\r\n");
			fw.flush();
			fw.close();
			
			
			//add to database.
			//BBSyncDB syncdb=BBSyncDB.getBBSyncDB();
			//syncdb.connectDB();
			syncdb.addSyncLogs(id, new Timestamp(started.getTime()), new Timestamp(ended.getTime()), null, ((flag)?"Success":"Failed"),(this.invokedByUser?"M":"S"));
			//syncdb.closeDB();
			//syncdb.closeDB();
			
		}
		catch (Exception ex) {
			
			log.error("Error:"+ex.getMessage());
			//ClientErrorMgmt.reportError(ex,"Error in external log file writing");
			
			ex.printStackTrace();
			 
		}	

	}
	
	
}


