/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.pluggin;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import com.fourelementscapital.db.BBSyncDB;
import com.fourelementscapital.db.SchedulerDB;
import com.fourelementscapital.db.vo.BBSyncTrigger;

public class SchedulerBloombergPlugin implements PlugginInterface {

	 
	public static String PLUGGIN_IN="bloomberg_pluggin";
	
	public int addAction(Map data,HttpServletRequest request)  throws Exception {
		try{
			
			//BBSyncMgmt bbsyncmgmt=new BBSyncMgmt(request);
			
			//System.out.println("data:"+data);
			String name=(String)data.get("qdata.name");
			String mkt_securities=(String)data.get("mkt_securities");
			boolean mkt_securities1=false;
			if(mkt_securities.equalsIgnoreCase("true")){
				mkt_securities1=true;
			}
			 
			String filtervalue=(String)data.get("filtervalue");
			String date1s=(String)data.get("date1s");
			String date2s=(String)data.get("date2s");
			String number=(String)data.get("number");
			
			String fieldids=(String)data.get("fieldids");
			StringTokenizer token=new StringTokenizer(fieldids,",");
			Vector fieldids1=new Vector();
			while(token.hasMoreTokens()){
				fieldids1.add(token.nextToken());
			}
			
			String contracts=(String)data.get("contracts");
			
			String marketsector=(String)data.get("marketsector");
			//System.out.println("name:"+name+"  mkt_securities:"+ mkt_securities1+"  filtervalue:"+filtervalue+"  date1s:"+ date1s+"  date2s:"+date2s+"  number:"+ number+"  fieldids:"+ fieldids+"  contracts:"+ contracts+"  marketsector:"+marketsector);
			
			//int downloadid=bbsyncmgmt.saveSchedule2(0, name, mkt_securities1, filtervalue, date1s, date2s, number, fieldids1, contracts, null, null, null, marketsector, null);
			int downloadid=saveSchedule2(0, name, mkt_securities1, filtervalue, date1s, date2s, number, fieldids1, contracts, null, null, null, marketsector, null);
	 
			return downloadid;
		}catch(Exception e){
			throw e;			
		}
	
	}
	
	private int saveSchedule2(
			int id,
			String name,
			boolean mkt_secdb,							
			String dateoption,
			String datefrom,
			String dateto,
			String datenumber,							
			Vector fields,
			String contracts,
			BBSyncTrigger daily,
			BBSyncTrigger weekly,
			BBSyncTrigger monthly,
			String marketsector,
			String timezone
	) throws Exception {

		try{
			//log.debug("saveSchedule called");
			//String fields="";

			//if(blbfields.size()==dbfields.size()){			 
			//	for(int  i=0;i<blbfields.size();i++){
			//		String f=blbfields.get(i)+"~"+dbfields.get(i);
			//		fields+=(i>0)?"|"+f:f;
			//	}
			//}

			BBSyncDB syncdb=BBSyncDB.getBBSyncDB();

			BBSyncTrigger trigobj=new BBSyncTrigger();
			if(daily!=null && weekly==null && monthly==null){
				trigobj=daily;
				trigobj.setSynctype(BBSyncTrigger.HOURLY);
			}
			if(daily==null && weekly!=null && monthly==null){
				trigobj=weekly;
				trigobj.setSynctype(BBSyncTrigger.WEEKLY);
			}
			if(daily==null && weekly==null && monthly!=null){
				trigobj=monthly;
				trigobj.setSynctype(BBSyncTrigger.MONTHLY);
			}
			//log.debug("trigobj:"+trigobj);


			try{
				syncdb.connectDB();
				//log.debug("after connected");

				java.sql.Date d1=null;
				java.sql.Date d2=null;
				//log.debug("before parsing:"+datefrom);	
				SimpleDateFormat sf=new SimpleDateFormat("DD/mm/yyyy");

				if(datefrom!=null){

					//log.debug("parseing:"+sf.parse(datefrom));	
					d1=new java.sql.Date((sf.parse(datefrom)).getTime());
					//new java.sql.Date(datefrom.getTime());
				}
				if(dateto!=null){

					d2=new java.sql.Date((sf.parse(dateto)).getTime());
				}		

				int datenumber1=0;
				try{
					datenumber1=Integer.parseInt(datenumber);
				}catch(Exception e){}
				int nid=syncdb.saveSchedule(
						id,	
						name,
						mkt_secdb+"",				
						dateoption,
						d1,
						d2,	
						datenumber1,
						fields,		
						contracts,
						trigobj,
						marketsector,
						timezone
				);
				syncdb.closeDB();
				return nid;	
			}catch(Exception e){
				//ClientErrorMgmt.reportError(e, "Couldn't save scheduling data");
				//e.printStackTrace();
				throw e;
			}

			 
			
		}catch(Exception e){
			//ClientErrorMgmt.reportError(e, null);		 
			throw e;
		}
	}	

	
	public int updateAction(int scheduler_id,Map data,HttpServletRequest request)  throws Exception {
		try{
			
			//BBSyncMgmt bbsyncmgmt=new BBSyncMgmt(request);
			SchedulerDB sdb=SchedulerDB.getSchedulerDB();
			sdb.connectDB();
			
			Map sdata=sdb.getScheduler(scheduler_id);
			sdb.closeDB();
			
			String d_id=(String)sdata.get(getPlugginData().getFieldreference());
			if(d_id!=null){
				
				//System.out.println("data:"+data);
				String name=(String)data.get("qdata.name");
				String mkt_securities=(String)data.get("mkt_securities");
				boolean mkt_securities1=false;
				if(mkt_securities.equalsIgnoreCase("true")){
					mkt_securities1=true;
				}
				 
				String filtervalue=(String)data.get("filtervalue");
				String date1s=(String)data.get("date1s");
				String date2s=(String)data.get("date2s");
				String number=(String)data.get("number");
				
				String fieldids=(String)data.get("fieldids");
				StringTokenizer token=new StringTokenizer(fieldids,",");
				Vector fieldids1=new Vector();
				while(token.hasMoreTokens()){
					fieldids1.add(token.nextToken());
				}
				
				String contracts=(String)data.get("contracts");
				
				String marketsector=(String)data.get("marketsector");
				//System.out.println("name:"+name+"  mkt_securities:"+ mkt_securities1+"  filtervalue:"+filtervalue+"  date1s:"+ date1s+"  date2s:"+date2s+"  number:"+ number+"  fieldids:"+ fieldids+"  contracts:"+ contracts+"  marketsector:"+marketsector);
				
				//int downloadid=bbsyncmgmt.saveSchedule2(Integer.parseInt(d_id), name, mkt_securities1, filtervalue, date1s, date2s, number, fieldids1, contracts, null, null, null, marketsector, null);
				int downloadid=saveSchedule2(Integer.parseInt(d_id), name, mkt_securities1, filtervalue, date1s, date2s, number, fieldids1, contracts, null, null, null, marketsector, null);
		 
				return downloadid;
			}else{
				throw new Exception("Problem in updating Bloomberg data for this task");
			}
		}catch(Exception e){
			throw e;			
		}
	
	}
	
	 
	//public boolean deleteAction(int recordid,HttpServletRequest request) throws Exception {
	//	 
	//	return true;
	//}

 
	public PlugginData getPlugginData() {
		PlugginData pdata=new PlugginData();
		pdata.setJsnew("bbpl_jsNew");
		pdata.setJscreate("bbpl_jsCreate");
		pdata.setJsfetch("bbpl_jsFetch");
		//pdata.setJsmodify("bbpl_jsUpdate");
		pdata.setFieldreference("download_id");
		
		return pdata;
		 
		
	}

	
	public Map fetchData(int recordid, HttpServletRequest request) throws Exception {
		//BBSyncMgmt bbsyncmgmt=new BBSyncMgmt(request);
		//return bbsyncmgmt.getDownloadQuery(recordid);	
		return getDownloadQuery(recordid);	
	}
	 
	private Map getDownloadQuery(long id) throws Exception {
		try{
			BBSyncDB syncdb=BBSyncDB.getBBSyncDB();
			syncdb.connectDB();
			Map m=syncdb.getDownloadQuery(id);		
						
			//System.out.println("BBSyncMgmt.getDownloadQuery():m:"+m);
			
			Object b=m.get("date_from");
			if(b!=null){
				m.put("date_from",new SimpleDateFormat("dd/MM/yyyy").format((Date)b));
			}
			Object b1=m.get("date_to");
			if(b1!=null){
				m.put("date_to",new SimpleDateFormat("dd/MM/yyyy").format((Date)b1));
			}
			
		
			
			
			
			if(m.get("trigger_time")!=null){
				try{
					Date d=new SimpleDateFormat("HH:mm").parse((String)m.get("trigger_time"));
					Calendar c=Calendar.getInstance();
					c.setTime(d);				
					m.put("trigger_hour",c.get(Calendar.HOUR_OF_DAY));
					m.put("trigger_minute",c.get(Calendar.MINUTE));
				}catch(Exception e){}
			}
			
			
			if(m.get("fields")!=null){
				String fields1=(String)m.get("fields");
				StringTokenizer st=new StringTokenizer(fields1,"|");
				TreeMap fields=new TreeMap();
				while(st.hasMoreTokens()){
					String cfield=st.nextToken();
					
					StringTokenizer st1=new StringTokenizer(cfield,"~");
					if(st1.countTokens()==2){
						String ky=st1.nextToken();
						String value=st1.nextToken();
						
						fields.put(ky,value);
					}
				}
				m.put("fieldsmap",fields);
			}
			
			//put logs if there is
			
			/*
			Vector logs=syncdb.getSyncLogs((int)id);
			for(Iterator<Map>i= logs.iterator();i.hasNext();) {
				 Map record=i.next();
				 try{
					 Timestamp s=(Timestamp)record.get("start_time");
					 Timestamp e=(Timestamp)record.get("end_time");
					 if(s!=null && e!=null){
						 SimpleDateFormat df=new SimpleDateFormat("dd MMM, yyyy");
						 SimpleDateFormat tf=new SimpleDateFormat("h:mm a");
						 record.put("start_time",tf.format(s));
						 record.put("end_time",tf.format(e));
						 record.put("start_date",df.format(s));
						 
						 SimpleDateFormat dateFormat =new SimpleDateFormat("mm:ss");
						 dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
						 long diff=e.getTime()-s.getTime();
						 record.put("duration", dateFormat.format(new Date(diff)));
					 }
				 }catch(Exception e){
					 
				 }
			}
			
			m.put("logs",logs );
			*/
			
			
			
			syncdb.closeDB();		
			return m;
		}catch(Exception e){
			//ClientErrorMgmt.reportError(e, null);
			throw e;
		}
	}	

	
	public void deleteAction(int scheduler_id, HttpServletRequest request) throws Exception {
		
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		sdb.connectDB();
		
		Map sdata=sdb.getScheduler(scheduler_id);
		sdb.closeDB();
		
		
		String d_id=(String)sdata.get(getPlugginData().getFieldreference());
		if(d_id!=null){
			//BBSyncMgmt bbsyncmgmt=new BBSyncMgmt(request);
			//bbsyncmgmt.deleteQuery(Integer.parseInt(d_id));		
			deleteQuery(Integer.parseInt(d_id));	
		} else{
			throw new Exception("Problem in deleting Bloomberg data for this task");
		}
		
		
	}

	
	private Map deleteQuery(int id) throws Exception {
		try{
			BBSyncDB syncdb=BBSyncDB.getBBSyncDB();
			syncdb.connectDB();
			syncdb.deleteQuery(id);	
			
			//new BloombergDownloadJob().scheduleDownloadQueries();
			
			Map rtn=loadFirst();
			rtn.remove("field_mapping");
			syncdb.closeDB();
			
			return rtn; 
		}catch(Exception e){
			//e.printStackTrace();
			//ClientErrorMgmt.reportError(e, "id:"+id);
			throw e;
		}
	}	
	
	private Map loadFirst() throws Exception {
		BBSyncDB syncdb=BBSyncDB.getBBSyncDB();
		syncdb.connectDB();
		TreeMap rtn=new TreeMap();
		Vector v=syncdb.listAll();
		Vector  fields=syncdb.getFieldMapping();
		for(int i=0;i<v.size();i++){
			Map m=(Map)v.get(i);			
			Object b=m.get("last_executed");			
			if(b instanceof java.sql.Timestamp && b!=null ){
				String recent=new SimpleDateFormat("d MMM,yy  HH:mm").format((Date)b);
				//m.put("name",((m.get("name")!=null)?m.get("name"):"")+ " <small>(Last Executed:"+recent+")</small>") ;
				m.put("last_executed", recent) ;
			}
		}
		syncdb.closeDB();
		rtn.put("schedules",v);
		rtn.put("field_mapping", fields);
		return rtn;
	}	

 
	public String getText(Map data) {
		
		 
		String rtn="Name:"+data.get("qdata.name")+"\n";
		rtn+="Is_securities:"+data.get("mkt_securities")+"\n";		
		rtn+="Filter:"+data.get("filtervalue")+"\n";
		rtn+="Date_option1:"+data.get("date1s")+"\n";
		rtn+="Date_option2:"+data.get("date2s")+"\n";
		rtn+="Date_option3:"+data.get("number")+"\n";	 
		rtn+="Fieldids:"+data.get("fieldids")+"\n";
		rtn+="Tickers:"+data.get("contracts")+"\n";		
		rtn+="Sector:"+data.get("marketsector")+"\n";	 
		
		return rtn;
		 
	}

	 
	//public void upadteAction(int recordid, Map data,HttpServletRequest request)throws Exception {
		 
	//	
	//}
 
	

}


