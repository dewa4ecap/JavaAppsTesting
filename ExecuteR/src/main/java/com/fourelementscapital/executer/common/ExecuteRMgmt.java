/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.executer.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.jcs.JCS;
import org.apache.commons.jcs.access.GroupCacheAccess;
import org.apache.commons.jcs.engine.behavior.IElementAttributes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fourelementscapital.loadbalance.LoadBalancingQueue;
import com.fourelementscapital.rscript.RScript;

/**
 * Provides AJAX data for ExecuteR monitoring interface. 
 * The jsp file for this class executeR.jsp and javascript file executer.jsp  
 * @author Administrator
 *
 */
@SuppressWarnings("unchecked")
public class ExecuteRMgmt {

	private Logger log = LogManager.getLogger(ExecuteRMgmt.class.getName());
	private HttpServletRequest request=null;	
	
	private static Semaphore refreshLock=new Semaphore(1,true);
	private static final long TIMEOUT_MS=1000;
	
	/**
	 * Semaphore lock and release machanism to make some the method calls exclusive to the current thread. 
	 */
	private void acquireLock(){		
		try{			 
			ExecuteRMgmt.refreshLock.tryAcquire(TIMEOUT_MS, TimeUnit.MILLISECONDS);
		}catch(Exception e){	log.error("Error:"+e.getMessage()); }
	}
	private void releaseLock(){		
		try{		ExecuteRMgmt.refreshLock.release(); 	}catch(Exception e){	log.error("Error:"+e.getMessage());		}
	}
	
	/**
	 * for DWR call 
	 * @throws Exception
	 */
	public ExecuteRMgmt() throws Exception {
		
	}

	/**
	 * for internal or jsp call
	 * @param request
	 * @throws Exception
	 */
	public ExecuteRMgmt(HttpServletRequest request) throws Exception {
		
	}
	
	/**
	 * get queue data in json format
	 * @return
	 * @throws Exception
	 */
	public Map getQueue() throws Exception {	
		acquireLock();
		try{
			
			HashMap rtn=new HashMap();  
			ArrayList<RScript> list2=new ArrayList();
			LoadBalancingQueue queue=LoadBalancingQueue.getExecuteRScriptDefault();
			Collection<RScript> queued=queue.getScriptQueue();
						
			Collection<RScript> p1=queue.getScriptProcessingQueue();
			
			ArrayList executing=new ArrayList();
			for(RScript rs:p1){		
				executing.add(rs.getUid());
			}
			list2.addAll(p1);
			list2.addAll(queued);			
			
			GroupCacheAccess gjcs=queue.getGroupedCache();
			
			//just to retrieve so that, expired won't in the memory
			int counter=0;
			long totaldelay=0;
			for(Object key: gjcs.getGroupKeys(LoadBalancingQueue.CACHE_GROUP_FINISHED)){
				Long delay=(Long)gjcs.getFromGroup(key,LoadBalancingQueue.CACHE_GROUP_FINISHED);
				if(delay!=null){
					totaldelay=totaldelay+delay;	
					counter++;
				}
			}
			log.debug("totaldelay:"+totaldelay+" counter:"+counter);
			long ave_delay=(counter>0 && totaldelay>0)?(totaldelay/counter)/1000:0;
			
			//just to retrieve so that, expired won't in the memory			
			for(Object key: gjcs.getGroupKeys(LoadBalancingQueue.CACHE_GROUP_TIMEOUT)){
				gjcs.getFromGroup(key,LoadBalancingQueue.CACHE_GROUP_TIMEOUT);
			}
			Set finished=gjcs.getGroupKeys(LoadBalancingQueue.CACHE_GROUP_FINISHED);
			Set timedout=gjcs.getGroupKeys(LoadBalancingQueue.CACHE_GROUP_TIMEOUT);
			Set activePeers=gjcs.getGroupKeys(LoadBalancingQueue.CACHE_GROUP_ACTIVEPEERS);

			rtn.put("queue", list2);
			rtn.put("ave_delay", (ave_delay>0?ave_delay:0));
			rtn.put("queued_count", queued.size());			
			rtn.put("executing", executing);
			rtn.put("executing_count", p1.size());
			rtn.put("active_peers", activePeers.size());
			rtn.put("finished", finished);			
			rtn.put("timedout", timedout);
			return rtn;
			
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}finally{
			releaseLock();
		}
	}
	
	/**
	 * remove failed item in the queue
	 * @param uid
	 * @throws Exception
	 */
	public void removeFromAllQueue(String uid) throws Exception {
		try{
			RScript rs=new RScript();
			rs.setUid(uid);
			LoadBalancingQueue queue=LoadBalancingQueue.getExecuteRScriptDefault();
			queue.removeScriptFromAllQueue(rs);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * returns the queue data in xml format
	 * @return
	 * @throws Exception
	 */
	public String getQueueXML() throws Exception {
		Map data=getQueue();
		String result="<result>";
		for(Iterator<String> keys=data.keySet().iterator();keys.hasNext();){
			String key=keys.next();
			
			ArrayList a=new ArrayList();			
			
			a.add("ave_delay");
			a.add("queued_count");		
			a.add("executing_count");
			a.add("active_peers");
			a.add("finished");			
			a.add("timedout");
			
			if(a.contains(key)){
				String val=data.get(key)+"";
				if(data.get(key) instanceof Collection) val=((Collection)data.get(key)).size()+"";
				result+="<"+key.toLowerCase()+">"+val+"</"+key.toLowerCase()+">";
			}
		}
		result+="</result>";
		return result;
	}
}