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
import java.util.StringTokenizer;
import java.util.Vector;

import com.fourelementscapital.db.vo.ValueObject;

 
public abstract class SearchTokenCollector {

	protected String rtn="";
	protected HashMap categoryfields=new HashMap();
	protected Vector additionalField=new Vector();
	
	private String tablename=null;
	
	public SearchTokenCollector(String tablename){
		this.tablename=tablename;	
		

	}
	
	public String getTablename(){return this.tablename;}
	
	protected String collectTokens(Map data) throws Exception {
		String rtn="";
		if(data!=null){
			for(Iterator i=data.keySet().iterator();i.hasNext();){
				String key=(String)i.next();
				rtn+=(data.get(key)!=null)?" "+key+":"+data.get(key).toString():" ";
			}
		}
		return rtn;
	}
	
	abstract void processTextTokens(String ticker) throws Exception;
	
	public String getString(){
		return this.rtn;
	}
	public Map getCategoryFields(){
		return this.categoryfields;
	}
	
	
	
	 
	protected void ignoreFields(Map data, String keys){
		StringTokenizer st= new StringTokenizer(keys,",");
		while(st.hasMoreTokens()){
			String key=st.nextToken();
			if(data.get(key)!=null){
				data.remove(key);
			}
		}
	}
	
	protected Map getCategoryFields(Map data, Map datamap){
		HashMap rtn=new HashMap();
		for(Iterator i=datamap.keySet().iterator();i.hasNext();){
			String key=(String)i.next();
			if(data.get(key)!=null){
				rtn.put(datamap.get(key), data.get(key));
			}
		}
		return rtn;
	}
	
	protected void addFields(String fieldname,String fieldvalue){
		ValueObject vo=new ValueObject();
		vo.setKey(fieldname);
		vo.setValue(fieldvalue);
		additionalField.add(vo);
	}
	
	public Vector getAdditionalFields(){
		return additionalField;
		
	}
	 
}



