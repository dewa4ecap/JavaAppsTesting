/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/


package com.fourelementscapital.scheduler.engines;

import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jfree.util.Log;
import org.quartz.JobExecutionException;

import com.fourelementscapital.db.BBSyncDB;
import com.fourelementscapital.db.vo.FlexiField;
import com.fourelementscapital.scheduler.exception.ExceptionWarningNoFullData;
import com.fourelementscapital.scheduler.pluggin.SchedulerBloombergPlugin;

public class BloombergDownload extends ScheduledTask {
	
	Logger log = LogManager.getLogger(BloombergDownload.class.getName());
	
	public BloombergDownload(String name, String taskuid) {
		//super("Bloomberg Download","bb_download");		
		super(name,taskuid);
		try{
			addFormFields(getMyFields());		
		}catch(Exception e){
			e.printStackTrace();
		}		
	}

	
	public synchronized void execute(StackFrame sframe) throws  JobExecutionException,ExceptionWarningNoFullData,Exception	  {
		
		
		Map data=sframe.getData();
		if(data.get("download_id")==null || (data.get("download_id")!=null && ((String)data.get("download_id")).equals("")) ) {
			throw new JobExecutionException("Job can't be executed as you did not not select download query");
		}else{
			
			try{
				
				String id=(String)data.get("download_id");
				
				String id1=data.get("id").toString();
				log.debug("sc_id:"+id1+":before opening database ...");
				
			
				
				
				String rlsdt_adjustment=(String)data.get("rlsdt_adjustment");				
				BBSyncDB syncdb=BBSyncDB.getBBSyncDB();
	
				log.debug("sc_id:"+id1+":syncdb init");
				
				syncdb.connectDB();
				
				if(true){
					
					//BoneCP bcp=syncdb.getConnectionPool();
					//log.debug("sc_id:"+id1+"----------------------------------------");
					//if(bcp!=null){
						//log.debug("sc_id:"+id1+":syncdb CP Free connection :"+bcp.getStatistics().getTotalFree());
						//log.debug("sc_id:"+id1+":syncdb CP Leased connection :"+bcp.getStatistics().getTotalLeased());
						//log.debug("sc_id:"+id1+":syncdb CP Total Created connection :"+bcp.getStatistics().getTotalCreatedConnections());
						//log.debug("sc_id:"+id1+":syncdb CP Cache Hits :"+bcp.getStatistics().getCacheHits());
						//log.debug("sc_id:"+id1+":syncdb CP Cache miss:"+bcp.getStatistics().getCacheMiss());
					//}
					
				}

				log.debug("sc_id:"+id1+":connected");
				Map data1=syncdb.getDownloadQuery(Integer.parseInt(id));
				log.debug("sc_id:"+id1+":after getDownloadQuery called");
				if(rlsdt_adjustment!=null && !rlsdt_adjustment.equals("")){
					try{
						int dadj=Integer.parseInt(rlsdt_adjustment);						
						data1.put("enddt_adjustment",new Integer(dadj));
						
						
					}catch(Exception e){
						//ClientErrorMgmt.reportError(e, "Error while parsing release date adjument to Integer");
						throw e;
					}
				}
				
				if(data1!=null){			
					log.debug("sc_id:"+id+":after before bloombergDownloadJob init");
					BloombergDownloadJob bjdo= new BloombergDownloadJob();
					bjdo.setInvokedByScheduler();
					try{
						
						bjdo.downloadData(data1,sframe);
						
						log.debug("sc_id:"+id1+":after before downloadData called");
					}catch(ExceptionWarningNoFullData wnd){
						throw wnd;
					}catch(Exception e1){
						throw new JobExecutionException(e1);
					}finally{
						
						sframe.setTasklog(bjdo.getSchedulerLog());
						if(bjdo.getSchedulerLog()!=null && !bjdo.getSchedulerLog().equals("")){
							sframe.setStatus(ScheduledTask.EXCECUTION_WARNING);
						}
					}
				}else{
					throw new JobExecutionException("Job can't be executed, relevant download query not found (you may have deleted)");
				}
				syncdb.closeDB();
			}catch(ExceptionWarningNoFullData wnd1){
				throw wnd1;
			}catch(Exception e){
				//ClientErrorMgmt.reportError(e, "id:"+id);
				throw new JobExecutionException(e);
			}
		}
		
	}

	
	
	public List<ScheduledTaskField> listFormFields(){
		super.removeAllFields();
		try{
			super.addFormFields(getMyFields());
		}catch(Exception e){
			Log.error("Error in refreshing fields");
		}
		return super.listFormFields();		
	}

	
	
	private Vector<ScheduledTaskField> getMyFields() throws Exception {
		Vector fields=new Vector();	
		
		ScheduledTaskField f3=new ScheduledTaskField();
		f3.setShortname("rlsdt_adjustment");		
		f3.setFieldlabel("Release Date Adjustment");		
		f3.setFieldtype(FlexiField.TYPE_TEXTBOX);	
		fields.add(f3);

		
		ScheduledTaskField f1=new ScheduledTaskField();
		f1.setShortname(SchedulerBloombergPlugin.PLUGGIN_IN);
		f1.setFieldlabel("Bloomberg Query");		
		f1.setFieldtype(FlexiField.TYPE_BLOOMBERG_PLUGGIN);	
		f1.setPluggindata(new SchedulerBloombergPlugin().getPlugginData());
		fields.add(f1);
		
		ScheduledTaskField f2=new ScheduledTaskField();
		f2.setShortname(new SchedulerBloombergPlugin().getPlugginData().getFieldreference());
		f2.setFieldlabel("download_id");		
		f2.setFieldtype(FlexiField.TYPE_HIDDENFIED);	
		fields.add(f2);


		
		return fields;
		
	}
}

