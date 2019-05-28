/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fe.lucene;


 

public class TokenCollectorFactory {
	private TokenCollectorFactory(){}
	
	public static SearchTokenCollector getTokenCollector(TickerIndexRecord uio ){
		try {
			if(uio.getTablename().equals(LuceneCrawler.INDEX_TABLE_SECURITY)){
				return new MarketSecurityCollector(uio.getTablename());
				//return null;
			}
			if(uio.getTablename().equals(LuceneCrawler.INDEX_TABLE_CONTRACT)){
				return new MarketContractCollector(uio.getTablename());
				//return null;
			}
			
			if(uio.getTablename().equals(LuceneCrawler.INDEX_TABLE_COMMODITY)){
				return new MarketCommodityCollector(uio.getTablename());
				//return null;
			}
			
			
			if(uio.getTablename().equals(LuceneCrawler.INDEX_TABLE_FUNDAMENTAL_TICKR)){
				return new FundamentalTickersCollector(uio.getTablename());
				//return null;
			}
			
			if(uio.getTablename().equals(LuceneCrawler.INDEX_SCHEDULER)){
				return new SchedulerTokenCollector(uio.getTablename());
				//return null;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		} 
		
		
		return null;
		
	}

}



