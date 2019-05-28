/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.io.msg;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.rosuda.JRI.REXP;
import org.rosuda.JRI.RVector;

import com.fourelementscapital.scheduler.ScheduledTaskFactory;
import com.fourelementscapital.scheduler.engines.ScheduledTask;
import com.fourelementscapital.scheduler.engines.StackFrame;
import com.fourelementscapital.scheduler.engines.StackFrameCallBack;
import com.fourelementscapital.scheduler.error.ClientError;
import com.fourelementscapital.scheduler.exception.SchedulerException;
import com.fourelementscapital.scheduler.io.request.IOServerRequest;
import com.fourelementscapital.scheduler.p2p.P2PService;
import com.fourelementscapital.scheduler.queue.QueueStackManager;
import com.fourelementscapital.scheduler.rscript.RScript;

 

public class ServerExecuteScript extends IOServerRequest {

	
	private Logger log = LogManager.getLogger(ServerExecuteScript.class.getName());
	
	private String script_uid;	
	private String script; 	
	private String taskuid;
	private String queue_uid;
	


	public String getTaskuid() {
		return taskuid;
	}


	public String getQueue_uid() {
		return queue_uid;
	}


	public void setTaskuid(String taskuid) {
		this.taskuid = taskuid;
	}


	public void setQueue_uid(String queue_uid) {
		this.queue_uid = queue_uid;
	}


	public String getScript() {
		return script;
	}


	public String getScript_uid() {
		return script_uid;
	}


	public void setScript_uid(String script_uid) {
		this.script_uid = script_uid;
	}


	public void setScript(String script) {
		this.script = script;
	}

 

	public void executeAtPeer() {
		
		String peername=P2PService.getComputerName();
		
		try{
			
			QueueStackManager.setStackBusy(peername, this.queue_uid);
			
			ScheduledTask task=new ScheduledTaskFactory().getTaskFromAll(getTaskuid());
			
			StackFrame sf=new StackFrame(task,null);
			RScript rs=new RScript();
			rs.setUid(getScript_uid());
			rs.setScript(getScript());		
			sf.setRscript(rs);
			sf.setQueue_uid(this.queue_uid);
			sf.setStarted_time(new Date().getTime());
			sf.addCallBack(new StackFrameCallBack(){				
				public void callBack(StackFrame sf,String status,SchedulerException se){
					String result=null;
					if(sf.getRscript()!=null){
						result="<?xml version=\"1.0\"?><output status=\""+status+"\" peer=\""+P2PService.getComputerName()+"\">";						
						if(sf.getRscript().getResultXML()!=null){
							result+="<result>"+sf.getRscript().getResultXML()+"</result>";
						}
						REXP x1=sf.getRscript().getResult();
						String jsonresult="";
						if(x1!=null){					
							RVector rv=x1.asVector();
							String prtn="";
							prtn=parse(x1,prtn);
							try{
								jsonresult=parseJSON(x1, jsonresult);
							}catch(Exception e){
								String errmsg="Error while parsing to json";
								log.error(errmsg);
								sf.getRscript().setError(
									sf.getRscript().getError()!=null?sf.getRscript().getError()+", "+errmsg:" Error while parsing result to JSON "
								);
							}
							result+="<result>"+prtn+"</result>";					
						}
						
						SimpleDateFormat format=new SimpleDateFormat("d MMM yyyy HH:mm:ss SSS");
						String err=sf.getRscript().getError()!=null ?sf.getRscript().getError():"";
						result+="<error>"+err+"</error>";					
						result+="<startedTime>"+format.format(new Date(sf.getStarted_time()))+"</startedTime>";
						result+="<duration in=\"milliseconds\">"+(new Date().getTime()-sf.getStarted_time())+"</duration>";
						result+="</output>";						
						try{
							String peername=P2PService.getComputerName();
							QueueStackManager.setStackIdle(peername, sf.getQueue_uid());
						}catch(Exception e){
							log.error("error while settting queue stack idle,e:"+e.getMessage());
						}
						
						log.debug("script finished and call back, just before sending back to server");
						PeerScriptFinished psf=new PeerScriptFinished();
						psf.setScript_uid(sf.getRscript().getUid());
						psf.setQueue_uid(getQueue_uid());
						psf.setError(err);
						psf.setResultxml(result);
						psf.setStatus(status);
						psf.send();
						
					}				
				}
			});
			log.debug("before executing script, task:"+task);
			
			String status=ScheduledTask.EXCECUTION_FAIL;
			try{					
					log.debug("just before executed task:"+task.getClass().getName());
					task.execute(sf);				
					log.debug("just after executed task");
					status=ScheduledTask.EXCECUTION_SUCCESS;
					
					
			}catch(Exception e){
				log.error("error:::::"+e.getMessage()+" task:"+task+" getTaskuid():"+getTaskuid());
				e.printStackTrace();
				//Number nid=(Number)this.frame.getData().get("id");			
				status=ScheduledTask.EXCECUTION_FAIL;			
				ClientError.reportError(e, null);
				
			}finally{
				if(sf.getCallBack()!=null){					
					try{
						sf.getCallBack().callBack(sf, status,null);
					}catch(Exception e){
						ClientError.reportError(e, null);
					}				
				}			
				log.debug("thread finised....");
			}
		
		}catch(Exception e){
			
		}finally{
			try{
				//QueueStackManager.setStackIdle(peername, this.queue_uid);
			}catch(Exception e){
				log.error("error while settting queue stack idle,e:"+e.getMessage());
				
			}
		}
		
		
		
		
	}
	
	

	private String parse(REXP rxp, String rtn) {
		
		if(rxp.getType()==REXP.XT_VECTOR ){
			RVector rv=rxp.asVector();
			for(Iterator i=rv.iterator();i.hasNext();){
				Object obj=i.next();
				if(obj instanceof REXP){
					//log.debug("~~~~~Parsing.......obj:"+obj);
					rtn+=parse((REXP)obj, "");
				}
			}
		}
		
		
		if(rxp.getType()==REXP.XT_DOUBLE){
			rtn+="<double>"+rxp.asDouble()+"</double>";
		}
		
		if(rxp.getType()==REXP.XT_ARRAY_DOUBLE){
			double[] ar=rxp.asDoubleArray();
			rtn+="<array>";
			for(int i=0;i<ar.length;i++){
				rtn+="<double>"+ar[i]+"</double>";
			}
			rtn+="</array>";
		}
	
		
		if(rxp.getType()==REXP.XT_STR){
			rtn+="<string>"+rxp.asString()+"</string>";
		}				 
		//if(rxp.getType()==REXP.XT_STR || rxp.getType()==REXP.XT_VECTOR || rxp.getType()==REXP.XT_DOUBLE || rxp.getType()==REXP.XT_ARRAY_DOUBLE){}
		//else log.debug("rxp.getType():"+rxp.getType());
		return rtn;
	}
	
	private String parseJSON(REXP rxp, String rtn) throws Exception {
		
		if(rxp.getType()==REXP.XT_VECTOR ){
			RVector rv=rxp.asVector();
			for(Iterator i=rv.iterator();i.hasNext();){
				Object obj=i.next();
				if(obj instanceof REXP){
					//log.debug("~~~~~Parsing.......obj:"+obj);
					rtn+=parseJSON((REXP)obj, "");
				}
			}
		}		
		JSONArray arr=new JSONArray();
		
		if(rxp.getType()==REXP.XT_DOUBLE){			
			arr.put(rxp.asDouble());
		}
		
		if(rxp.getType()==REXP.XT_ARRAY_DOUBLE){
			double[] ar=rxp.asDoubleArray();			
			for(int i=0;i<ar.length;i++){			
				arr.put(ar[i]);
			}
		}
	
		
		if(rxp.getType()==REXP.XT_STR){
			//rtn+="<string>"+rxp.asString()+"</string>";
			arr.put(rxp.asString());
		}				 
		//if(rxp.getType()==REXP.XT_STR || rxp.getType()==REXP.XT_VECTOR || rxp.getType()==REXP.XT_DOUBLE || rxp.getType()==REXP.XT_ARRAY_DOUBLE){}
		//else log.debug("rxp.getType():"+rxp.getType());
		return arr.toString();
	}

}


