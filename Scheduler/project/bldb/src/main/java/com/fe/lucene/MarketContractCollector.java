/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fe.lucene;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import com.fourelementscapital.db.FlexiFieldDB;
import com.fourelementscapital.db.ReferenceDB;

public class MarketContractCollector extends SearchTokenCollector {


	public MarketContractCollector(String tablename) {
		super(tablename);
		 
	}

	public void processTextTokens(String ticker) throws Exception {
		//FlexiFieldDB fldb=new FlexiFieldDB();
		ReferenceDB refdb=ReferenceDB.getReferenceDB();
		
		refdb.connectDB();
		
		//Vector<Map> fl_fdata=fldb.getFlexiFieldData4LuceneToken("commodity_flexi_data" , "commodity", ticker);		
		Vector bb_syncdata=refdb.getContractBBInfo(ticker);
		
		String content=ticker+" ";
		HashMap catfields=new HashMap();
		
		
		/*
		for(Iterator<Map> i=fl_fdata.iterator();i.hasNext();){
			Map record=i.next();
			if(record.get("val")!=null){
				String val=(String)record.get("val");
				content+=" "+val;
				if(record.get("fieldlabel")!=null){
					catfields.put(record.get("fieldlabel")+"",val );
				}			
			}
		}
		*/
		for(Iterator<Map> i=bb_syncdata.iterator();i.hasNext();){
			Map record=i.next();
			if(record.get("value")!=null){
				String val=(String)record.get("value");
				content+=" "+val;
				if(record.get("friendly_name")!=null){
					catfields.put(record.get("friendly_name")+"",val );
				}		
				if(record.get("bb_fieldname")!=null){
					catfields.put(record.get("bb_fieldname")+"",val );
				}		
			}
		}
		catfields.put("title",ticker);
		super.rtn=content;
		super.categoryfields.clear();
		super.categoryfields.putAll(catfields);
		//fldb.closeDB();
		refdb.closeDB();
		
	}

}



