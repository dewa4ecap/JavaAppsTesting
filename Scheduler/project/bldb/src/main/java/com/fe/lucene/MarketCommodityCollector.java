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

public class MarketCommodityCollector extends SearchTokenCollector {


	public MarketCommodityCollector(String tablename) {
		super(tablename);
		 
	}

	public void processTextTokens(String ticker) throws Exception {
		FlexiFieldDB fldb=FlexiFieldDB.getFlexiFieldDB();
		//ReferenceDB refdb=new ReferenceDB();
		
		fldb.connectDB();	
		
		Vector<Map> fl_fdata=fldb.getFlexiFieldData4LuceneToken("commodity_flexi_data" , "commodity", ticker);		
		
		
		String content=ticker+" ";
		HashMap catfields=new HashMap();
		
	
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
 
		catfields.put("title",ticker);
		super.rtn=content;
		super.categoryfields.clear();
		super.categoryfields.putAll(catfields);
		fldb.closeDB();
		//refdb.closeDB();
		
	}

}



