/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.engines;

import java.util.Vector;

import com.fourelementscapital.db.vo.FlexiField;

public abstract class AbstractRScript extends ScheduledTask{
	
	public AbstractRScript(String name, String uid) {
		super(name, uid);
		try{			
			addFormFields(getMyFields());		
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	 

	protected Vector<ScheduledTaskField> getMyFields(){
		Vector fields=new Vector();
		
		//list
	 
		ScheduledTaskField f3=new ScheduledTaskField();
		f3.setShortname("rscript_param");
		f3.setFieldlabel("");		
		f3.setFieldtype(FlexiField.TYPE_RSCRIPTEDITOR_PARAM);	
		f3.setPlacementform("codeinject");	
		//f3.setFineprint("header code fine print");
		fields.add(f3);	
		

		ScheduledTaskField f4=new ScheduledTaskField();
		f4.setShortname("rscript");
		f4.setFieldlabel("R Script");		
		f4.setFieldtype(FlexiField.TYPE_RSCRIPTEDITOR);		
	 
		 
		
		fields.add(f4);	
		//fields.add(f5);
		
		fields.addAll(getAdditionalRScriptField());
		
		return fields;
		
	}
	
	public Vector<ScheduledTaskField> getAdditionalRScriptField(){
		Vector fields=new Vector();
		
		ScheduledTaskField f5=new ScheduledTaskField();
		f5.setShortname("trigger_commodity");
		f5.setFieldlabel("Trigger Commodity");		
		f5.setFieldtype(FlexiField.TYPE_TEXTBOX);	
		//f5.setFineprint("Commodity research ticker that defines the trigger time for Zero-day lag indicators e.g: CL,C");
		f5.setFineprint("Building block which fixing time defines the trigger time for Zero-day lag indicators e.g: CL,C,JNso");
		
		f5.setPlacementform("buildingblock");
		fields.add(f5);
		
		
		ScheduledTaskField f6=new ScheduledTaskField();
		f6.setShortname("trigger_frequency");
		f6.setFieldlabel("Trigger Frequency");		
		f6.setFieldtype(FlexiField.TYPE_TEXTBOX);	
		f6.setFineprint("Specific Intervals: 1/3 (starting at 1 and every 3 minutes)");
		f6.setPlacementform("buildingblock");
		fields.add(f6);
		return fields;
	}
}


