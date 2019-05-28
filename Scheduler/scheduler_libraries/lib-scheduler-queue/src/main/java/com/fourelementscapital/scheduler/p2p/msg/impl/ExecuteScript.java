/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.p2p.msg.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.jcs.JCS;
import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.rosuda.JRI.REXP;
import org.rosuda.JRI.RVector;

import com.fourelementscapital.scheduler.ScheduledTaskFactory;
import com.fourelementscapital.scheduler.balance.LoadBalancingQueue;
import com.fourelementscapital.scheduler.engines.ScheduledTask;
import com.fourelementscapital.scheduler.engines.StackFrame;
import com.fourelementscapital.scheduler.engines.StackFrameCallBack;
import com.fourelementscapital.scheduler.exception.SchedulerException;
import com.fourelementscapital.scheduler.p2p.P2PService;
import com.fourelementscapital.scheduler.p2p.listener.OutgoingMessageCallBack;
import com.fourelementscapital.scheduler.p2p.msg.PostCallBack;
import com.fourelementscapital.scheduler.p2p.msg.PostMessage;
import com.fourelementscapital.scheduler.peer.QueueAbstract;
import com.fourelementscapital.scheduler.peer.QueueFactory;
import com.fourelementscapital.scheduler.rscript.RScript;

public class ExecuteScript extends RScript  implements PostCallBack {

	
	
	 
    private Logger log = LogManager.getLogger(ExecuteScript.class.getName());

	
	public synchronized Map executeAtDestination() {
	    //System.out.println("Excecution stated:");
		
		HashMap rtndata=new HashMap();
		try{
			
			log.debug("getTaskuid():"+getTaskuid());
			
			ScheduledTask task=new ScheduledTaskFactory().getTaskFromAll(getTaskuid());
			log.debug("task:"+task);
			
			
			StackFrame sf=new StackFrame(task,null);
			 
			//log.debug("~~~ executeAtDestination() called");
			log.debug("-------->"+getUid());
			//log.debug("task class:"+task.getClass().getName());
			//log.debug("taskUid:"+getTaskuid());
			
			sf.setRscript(this);
			sf.setReplyTo( getMsgCreator()) ;
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
						
						ScriptFinished finished=new ScriptFinished();	
					    finished.setStatus(status);
					    finished.setResult(result);
					    finished.setUid(sf.getRscript().getUid());
					    finished.setError(sf.getRscript().getError());
					    finished.setJsonresult(jsonresult);
					    finished.setPriority(OutgoingMessageCallBack.PRIORITY_VERY_VERY_HIGH);					    
					    PostMessage ps=new PostMessage(finished,sf.getReplyTo());
					    ps.send();
					    
					}				
				}
			});
			
			
			boolean rtn=false;
			QueueAbstract qa=new QueueFactory().getQueue(getTaskuid());
			if(qa.isRoomForThread()){
				qa.addExThread(sf);
				rtn=true;
			}
			
			Object objs[]=qa.getExecutingStacks();
			String sc_uids="";;
			for(int i=0;i<objs.length;i++){
				StackFrame sf1=(StackFrame)objs[i];
				if(sf1.getRscript()!=null){
					sc_uids+=(sc_uids.equals(""))?sf1.getRscript().getUid():"|"+sf1.getRscript().getUid();
				}
			}
			
			rtndata.put("running_uids", sc_uids);
		}catch(Exception e){
			e.printStackTrace();
		}
		return rtndata;
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
	
	
	
	/*
	private String parseVector(REXP x1,Object rtnobj) throws Exception {		 
		if(x1.getType()==REXP.XT_VECTOR){
			
		}
		return null;
	}
	*/

	private static JCS cache=null;
	private static JCS getCache() throws Exception {
		 if(cache==null){
			cache=JCS.getInstance(ExecuteScript.class.getName());
		 }
		 return cache;
	}
	
	 
	public void callBack(Map data) {
		String uids=(String)data.get("running_uids");
		String peer=getMsgCreator();
		String ky=peer+"_delay";
		try{
			if(getCache().get(ky)==null){
				log.debug("<<--------"+getUid());
				if(uids!=null && !uids.equals("")){

					ArrayList list_uids=new ArrayList();
					StringTokenizer st=new StringTokenizer(uids,"|");
					while(st.hasMoreTokens()){
						list_uids.add(st.nextToken());
					}

					
					//ArrayList<RScript> peer_s=new ArrayList<RScript>();
					Collection<RScript> queue=LoadBalancingQueue.getExecuteRScriptDefault().getScriptProcessingQueue();
					for(Iterator<RScript> it=queue.iterator();it.hasNext();){
						RScript rs=it.next();
						if(rs.getPeer().equals(peer)){
							long waiting=new Date().getTime()-rs.getStartedtime().getTime();							
							//if more than 3 minutes
							if(waiting>(1000*60*1) && !list_uids.contains(rs.getUid())){								
								log.debug("removing task uid:"+rs.getUid()); 
							    rs.setError("Removing because it is not running on peer");
							    String result="";
							    LoadBalancingQueue.getExecuteRScriptDefault().scriptFinished(rs,result,ScheduledTask.EXCECUTION_FAIL);							    
							}
							//peer_s.add(rs);							
						}
					}
					
					
					IElementAttributes att= getCache().getDefaultElementAttributes();
		   			att.setMaxLifeSeconds(60);
		   			getCache().put(ky,"delay",att);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}


	@Override
	public void onCallBackSendingFailed() {
		// TODO Auto-generated method stub
		
	}
}


