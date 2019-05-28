/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.engines;

import com.fourelementscapital.db.vo.FlexiField;
import com.fourelementscapital.scheduler.pluggin.PlugginData;

public class ScheduledTaskField extends FlexiField {

	private String shortname;
	private PlugginData pluggindata;
	private String fineprint;
	private String placementform="general"; //default is general
	
	
	
	public String getPlacementform() {
		return placementform;
	}

	public void setPlacementform(String placementform) {
		this.placementform = placementform;
	}

	public String getFineprint() {
		return fineprint;
	}

	public void setFineprint(String fineprint) {
		this.fineprint = fineprint;
	}

	public PlugginData getPluggindata() {
		return pluggindata;
	}

	public void setPluggindata(PlugginData pluggindata) {
		this.pluggindata = pluggindata;
	}

	public String getShortname() {
		return shortname;
	}

	public void setShortname(String shortname) {
		this.shortname = shortname;
	}
	
	
	
	
	public boolean equals(Object other) {
		if(other instanceof ScheduledTaskField){
			if(((ScheduledTaskField)other).getShortname().equals(this.getShortname())){
				return true;
			}else{
				return false;
			}
		}
		return false;
	}
	
	public String toString(){
		return getShortname()+"."+getFieldlabel();
	}
	
}



