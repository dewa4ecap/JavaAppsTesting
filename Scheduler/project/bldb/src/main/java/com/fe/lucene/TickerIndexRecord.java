/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fe.lucene;



public class TickerIndexRecord {
	 
	private String ticker;
	private String tablename;
	
	private int action;
	
	 
	public int getAction() {
		return action;
	}
	public void setAction(int action) {
		this.action = action;
	}
	public String getTablename() {
		return tablename;
	}
	public void setTablename(String tablename) {
		this.tablename = tablename;
	}
	 
	public String getTicker() {
		return ticker;
	}
	public void setTicker(String ticker) {
		this.ticker = ticker;
	}
	
	
	public boolean equals(Object oth){
		TickerIndexRecord other=(TickerIndexRecord)oth;
		if(this.getTablename().equals(other.getTablename()) && this.getTicker()==other.getTicker()){
			return true;
		}else{
			return false;
		}
	}
	

}




