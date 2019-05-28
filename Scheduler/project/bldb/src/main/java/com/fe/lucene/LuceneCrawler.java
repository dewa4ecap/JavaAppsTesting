/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fe.lucene;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fourelementscapital.scheduler.error.ClientError;

 

public class LuceneCrawler implements Runnable{

	
	public static String INDEX_TABLE_SECURITY="market_securities";
	public static String INDEX_TABLE_CONTRACT="market_contracts";
	public static String INDEX_TABLE_COMMODITY="market_commodity";
	public static String INDEX_TABLE_FUNDAMENTAL_TICKR="fundamental_ticker";
	public static String INDEX_SCHEDULER="scheduler";
	
	 
	private String tablename;
	
	private static ConcurrentLinkedQueue queue=new ConcurrentLinkedQueue();
	
	private static Hashtable optimizedAccounts=new Hashtable();
	
	
	private Logger log = LogManager.getLogger(LuceneCrawler.class.getName());
	

	
	
	public LuceneCrawler(String tablename){
	 
		this.tablename=tablename;
	}
 
	
	private long indexid;
	
 
	private LuceneCrawler(){
		 
	}
	
	private static boolean threadExecuting=false;
	
	public synchronized void index(String ticker) throws Exception {
		 
		//System.out.println("CandidateCrawler: added id:"+candidateid);
		addToQueue(ticker,SearchSession.ACTION_INDEX);
	
		
	}
	
	
	private void addToQueue(String ticker, int action){
		TickerIndexRecord uobj=new TickerIndexRecord();
		 
		uobj.setTicker(ticker);
		uobj.setTablename(this.tablename);
		uobj.setAction(action);
		if(!LuceneCrawler.queue.contains(uobj)){
			LuceneCrawler.queue.add(uobj);
		}
		
		if(!LuceneCrawler.threadExecuting){
			LuceneCrawler.threadExecuting=true;
			 
			Thread th=new Thread(new LuceneCrawler());
			th.start();
		}
	}
	

	public void deleteIndex(String ticker) throws Exception {
		addToQueue(ticker,SearchSession.ACTION_DELETE);
	 
	}
	
	
	
	
	public void run() {

		try{
			
			//index creates a separate thread to index all queued items 
			//only one index will be running irrelevent of accounts or tablename
			//once all queued items index, then after specified interval count in global.xml index will be optimized.
			//this is to avoid big files being indexed very often that slows down the performance of the system.
			//if no specificed optimization interval count is not specified then it is 1 by default (hard coded below)
			
			
			while (!LuceneCrawler.queue.isEmpty()) {
				
				TickerIndexRecord uobj =(TickerIndexRecord)LuceneCrawler.queue.poll();
				//System.out.println("CandidateCrawler: indexing:"+uobj.getAccount()+":"+uobj.getRecordid());
				MyIndexer myin=new MyIndexer(uobj.getTablename());
				SearchSession ss=new SearchSession(myin);
			
				SearchTokenCollector ctc=TokenCollectorFactory.getTokenCollector(uobj) ;//new CandidateTokenCollector(uobj.getAccount());
				
				try{
					//log.debug("add action:"+uobj.getAction()+" recordid:"+uobj.getRecordid()+" ac:"+uobj.getAccount()+" table:"+uobj.getTablename());
					ss.action(uobj.getAction(),ctc,uobj.getTicker());
					log.debug("added to searchsession");
					if(SearchSession.ACTION_INDEX==uobj.getAction()){
						//LuceneLog.log(LuceneLog.STATUS_SUCCESS, uobj.getAccount(), uobj.getDbname(), uobj.getRecordid(), "");
					}else if(SearchSession.ACTION_DELETE==uobj.getAction()){
						//LuceneLog.log(LuceneLog.STATUS_DELETED, uobj.getAccount(), uobj.getDbname(), uobj.getRecordid(), "");
					}
					 
					
					
				}catch (Exception e){
					ClientError.reportError(e, "Error while indexing, Action:"+uobj.getAction()+" id:"+uobj.getTicker()+" db:"+uobj.getTablename());
					
					
				}
				int count=1;
				
				if(optimizedAccounts.containsKey(uobj.getTablename())){
					 
					TreeMap t=(TreeMap)optimizedAccounts.get(uobj.getTablename());
					//log.debug("t:"+t+" uobj tablename:"+uobj.getDbname());
					if(t.containsKey(uobj.getTablename())){
						count=(Integer)	t.get(uobj.getTablename());
						//log.debug("count a:"+count);
						count=count+1;
						//log.debug("count b:"+count);
					}
					
					t.put(uobj.getTablename(), count);
					
				}else{
					TreeMap t=new TreeMap();
					t.put(uobj.getTablename(),count);
					optimizedAccounts.put(uobj.getTablename(),t);
				}
				log.debug("optimized accounts"+optimizedAccounts);
 
			} //while loop	
			
			//for(int i=0;i<optimizeAccounts.size();i++){
			
			int optcount=5;//default (if no entry found);
			
			try{
				//String opc=new GeneralConfiguration().getProperty(GeneralConfiguration.LUCENE_OPTIMIZATION_COUNT);
				//optcount=Integer.parseInt(opc);
			}catch(Exception e){}
			
			Vector removeAccounts=new Vector();
			for(Iterator i=optimizedAccounts.keySet().iterator();i.hasNext();){
				String account=(String)i.next();
				TreeMap table=(TreeMap)optimizedAccounts.get(account);
				Vector removeTable=new Vector();
				for(Iterator ia=table.keySet().iterator();ia.hasNext();){
					String tablena=(String)ia.next();
					Integer count=(Integer)table.get(tablena);
					if(count>=optcount){
						removeTable.add(tablena);
						MyIndexer myin1=new MyIndexer(account);
						SearchSession ss1=new SearchSession(myin1);
						//LuceneLog.log(LuceneLog.STATUS_SUCCESS, account, tablena, 0, "*******Optimization started****");
						ss1.action(SearchSession.ACTION_OPTIMIZE,null,null);
						//LuceneLog.log(LuceneLog.STATUS_SUCCESS, account, tablena, 0, "*******Optimization ended********");
					}
				}
				for(Iterator ia=removeTable.iterator();ia.hasNext();){
					Object obj=ia.next();
					table.remove(obj);
				}
			
				if(table.size()<=0){
					removeAccounts.add(account);
				}
			}
			for(Iterator ia=removeAccounts.iterator();ia.hasNext();){
				Object obj=ia.next();
				optimizedAccounts.remove(obj);
			}
			log.debug("optimized accounts at last:"+optimizedAccounts);
			
		}catch(Exception e){
			//System.out.println("CandidateCrawler: Error:"+e.getLocalizedMessage());
			//e.printStackTrace();
			ClientError.reportError(e, null);
			//LuceneLog.log(LuceneLog.STATUS_FAILED, "", this.tablename, 0, "");
			//throw e;
			
		}finally{
			LuceneCrawler.threadExecuting=false;
		}
		
		
		//meanwhile while optimizing if there were documents added to index.
		//process the queue.
		if(LuceneCrawler.queue.size()>0){
			LuceneCrawler.threadExecuting=true;
			Thread th=new Thread(new LuceneCrawler());
			th.start();
		}
		 
		 
	}
	
	public Hashtable getOptimizationQueueObj(){
	
		 return optimizedAccounts;
	}
	
	public String getOptimizationStatus(){
		String rtn="";
		for(Iterator i=optimizedAccounts.keySet().iterator();i.hasNext();){
			String account=(String)i.next();
			TreeMap table=(TreeMap)optimizedAccounts.get(account);
			Vector removeTable=new Vector();
			for(Iterator ia=table.keySet().iterator();ia.hasNext();){
				String tablena=(String)ia.next();
				Integer count=(Integer)table.get(tablena);
				rtn+="Account:"+account+", table:"+tablena+", count:"+count+"\n";
			}
		}
		return rtn;
	}
	

	
	public static int getQueueStat() throws Exception {
		return queue.size();
	}
	
	
	 
}



