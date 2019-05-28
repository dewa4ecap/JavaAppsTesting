/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.balance;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;

import com.fourelementscapital.db.SchedulerDB;

public class LoadBalancingQueueTimeout {

	
	public LoadBalancingQueueTimeout(SchedulerDB sdb, Set types) throws Exception {
	
		Map data=sdb.getTimeoutSettings();
		HashMap dset=new HashMap();
		//LoadBalancingQueueTimeout lqt=new LoadBalancingQueueTimeout();
		BeanUtils.populate(this, data);
		for(Iterator tkeys=types.iterator();tkeys.hasNext();){
			String tkey=(String)tkeys.next();
			dset.put(tkey, data.get(tkey));
		}
		this.setMaxWaitingAlert(dset);
	}
	
	private int fewerminutes=1;
	private int fewerminutesexpiry=2;
	private int elsecritieriaxtime=2;
	private String alert_theme="itools";
	private String alert_type="Phone";
	
	private HashMap<String,String>  maxWaitingAlert=new HashMap();
	
	public String getAlert_theme() {
		return alert_theme;
	}
	public void setAlert_theme(String alert_theme) {
		this.alert_theme = alert_theme;
	}
	public String getAlert_type() {
		return alert_type;
	}
	public void setAlert_type(String alert_type) {
		this.alert_type = alert_type;
	}

	public HashMap<String,String> getMaxWaitingAlert() {
		return maxWaitingAlert;
	}
	public void setMaxWaitingAlert(HashMap<String,String>  maxWaitingAlert) {
		this.maxWaitingAlert = maxWaitingAlert;
	}

	public int getFewerminutes() {
		return fewerminutes;
	}
	public void setFewerminutes(int fewerminutes) {
		this.fewerminutes = fewerminutes;
	}
	public int getFewerminutesexpiry() {
		return fewerminutesexpiry;
	}
	public void setFewerminutesexpiry(int fewerminutesexpiry) {
		this.fewerminutesexpiry = fewerminutesexpiry;
	}
	public int getElsecritieriaxtime() {
		return elsecritieriaxtime;
	}
	public void setElsecritieriaxtime(int elsecritieriaxtime) {
		this.elsecritieriaxtime = elsecritieriaxtime;
	}
	
	
	/*
	public TreeMap<String, String> getMaxWaitingAlert() {
		TreeMap<String,String> range=new TreeMap<String, String>();
		range.put("rscript4rserve11", "5");
		range.put("xavier", "1");
		range.put("rscript4rconsole_p1", "10");
		range.put("rhinoscript1","1");
		range.put("bb_download2", "1");
		range.put("rhinoscript4priority", "1");
		range.put("rscript4rserveunix", "20");
		
		
		return range;
	}
	*/
	
	
}


