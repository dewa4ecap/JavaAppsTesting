/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fe.lucene;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;

import com.fourelementscapital.db.vo.ValueObject;
 
 
public class SearchSession {
	
	 
	//private IndexWriter index;
	//private IndexReader ir;
	//private IndexSearcher searcher;
	private Logger log = LogManager.getLogger(SearchSession.class.getName());
	private int maxpage    = 1000;  
    private MyIndexer myin;
    
    public SearchSession(MyIndexer myin){
    	this.myin=myin;
    }
	//public SearchSession(IndexWriter index,IndexReader ir){
	//	this.index=index;
	//	this.ir=ir;
	//}

	//public SearchSession(IndexSearcher searcher) {
	//	this.searcher=searcher;
	//}
	
	public void setMaxPerPage(int mp){
		this.maxpage=mp;
	}
	
	
	/*
	public synchronized void  addUpdate(long id, String content, Map fields) throws Exception {
		  
	 
		 if(this.ir==null || this.index==null) throw new Exception("IndexWriter or IndexReader is empty");
		
		 Term termid=new Term("id", ""+id);
		 TermEnum uidIter = ir.terms(termid);
		 Document doc=RecordDocument.Document(content, id, fields);
		 
		 doc.add(new Field("updated",
		                     DateTools.timeToString(new Date().getTime(), DateTools.Resolution.MINUTE),
		                     Field.Store.YES, Field.Index.UN_TOKENIZED));
		 
		 if(uidIter.next()){
			 index.updateDocument(termid, doc);
			 //update
		 }else{
			 doc.add(new Field("added",
			                     DateTools.timeToString(new Date().getTime(), DateTools.Resolution.MINUTE),
			                     Field.Store.YES, Field.Index.UN_TOKENIZED));
			 index.addDocument(doc);
			 //add
		 }
		 
	}
	*/
	
	
	public final static int ACTION_INDEX=1;
	public final static int ACTION_DELETE=2;
	public final static int ACTION_OPTIMIZE=3;
	
	/**
	 * the one method for indexing and deleting because it should be synchronized, while doing this operation index will be locked.
	 * 
	 * @param actiontype
	 * @param id
	 * @param content
	 * @param fields
	 * @throws Exception
	 */
	public synchronized void action(int actiontype,SearchTokenCollector ctc, String ticker) throws Exception {
 
		synchronized (this) { 
			//System.out.println("action started:id"+id);
			log.debug("action() invoked");
			IndexReader ir=myin.getIndexReader();
			IndexWriter index=myin.getIndexWriter();
			try{

				if(actiontype==ACTION_INDEX){
					try{
						ctc.processTextTokens(ticker);
					}catch(Exception e){
						e.printStackTrace();
						throw new Exception("Error while processing at ticker:"+ticker+" Msg:"+e.getLocalizedMessage());
					}
					if(ctc.getString()!=null){
						//candidate with this id already existing
						String content=ctc.getString();
						
						Map cfields=ctc.getCategoryFields();
						
						log.debug("cfields:"+cfields);
						log.debug("content:"+content);
						
						Term termid=new Term("id", ""+ticker);
						TermEnum uidIter = ir.terms(termid);
						Document doc=RecordDocument.Document(content, ticker, cfields);
	
				
	
						doc.add(new Field("updated",
						                  DateTools.timeToString(new Date().getTime(), DateTools.Resolution.DAY),
						                  Field.Store.YES, Field.Index.TOKENIZED));
						
						
						//in-case of additional fields to be added to the index.
						for(Iterator i=ctc.getAdditionalFields().iterator();i.hasNext();){
							ValueObject v=(ValueObject)i.next();
							if(v.getKey()!=null && !v.getKey().equals("") && v.getValue()!=null && !v.getValue().equals(""))
							doc.add(new Field(v.getKey(),  v.getValue(),     Field.Store.YES, Field.Index.TOKENIZED));
						}
	
						if(uidIter.next()){
							log.debug("updating document");
							index.updateDocument(termid, doc);
							//update
						}else{
							doc.add(new Field("added",
							                  DateTools.timeToString(new Date().getTime(), DateTools.Resolution.DAY),
							                  Field.Store.YES, Field.Index.TOKENIZED));
							index.addDocument(doc);
							//add
							log.debug("adding document");
						}
						
						
					}else{
						throw new Exception("No content collected  at id:"+ticker+" Msg:");
					}

				}else if(actiontype==ACTION_DELETE){

					if(ir==null || index==null) throw new Exception("IndexWriter or IndexReader is empty");

					Term termid=new Term("id", ""+ticker);
					TermEnum uidIter = ir.terms(termid);

					if(uidIter.next()){
						index.deleteDocuments(termid);
						//update
						log.debug("deleting document");
					} 
				}else if(actiontype==ACTION_OPTIMIZE){
					log.debug("optimizing indexwriter");
					index.optimize();
					
				}
			}catch(Exception e){

				//throw e;
				throw new Exception("Error at id:"+ticker+" Msg:"+e.getMessage());
			}finally{

				ir.close();
				index.close();

			}
			//System.out.println("action ended:id"+id+" ********");
		}
	}
	
	

	
	private int totalHits=0;
	
	public int getTotalHits(){
		return this.totalHits;
	}
	
	public Vector results(String querystring /*, Session session */, int startindex ) throws  Exception{
		IndexSearcher searcher=this.myin.getIndexSearcher();
		Vector rtn=null;
		try{


			Analyzer analyzer = new StandardAnalyzer();           
			QueryParser qp = new QueryParser("content", analyzer);
			qp.setDefaultOperator(QueryParser.AND_OPERATOR);
			Query  query = qp.parse(querystring); 
			//Query query = QueryParser.parse(queryString, "contents", analyzer);

			Hits hits = searcher.search(query); 
			this.totalHits=hits.length();
			//System.out.println("hits legnth:"+hits.length());        
			if (hits.length() == 0) {    

				// System.out.println("SearchSession: no result found"+new Date());

			}else{
				rtn=new Vector();

				int end=((startindex+maxpage)<=hits.length())?startindex+maxpage: hits.length();


				for (int i = startindex; i < end ; i++) {  // for each element
					Document doc = hits.doc(i);                    //get the next document 
					//String highlighting=extractFragment(doc, query);
					//LuceneResultItem li=  new LuceneResultItem();
					//li.setHtext(highlighting);
					//try{
					//	li.setId(Long.parseLong(doc.get("id")));
					//}catch(Exception e){}
					//li.setScore(hits.score(i));
					rtn.add(doc.get("id"));

				}

			}
		}catch(Exception e){
			throw e;
		}finally{
			searcher.close();
		}

		return rtn;
		
	}
	
}	
	 
	 


 
