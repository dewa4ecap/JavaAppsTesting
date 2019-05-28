/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fe.lucene;


import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.fourelementscapital.scheduler.config.Config;

 

public class MyIndexer {
	
	private Logger log = LogManager.getLogger(MyIndexer.class.getName());
	private String filename=null;
	private String account;
	private String dbname;
	
	private final int MAX_BUFFERED_DELETED_TERMS=20;
	
	public MyIndexer(String dbname) {
		
		this.dbname=dbname;
		//this.filename=GlobalConfiguration.getConfig().getConfigValue(Preferences.GLOBAL_CONFIG_SEARCH_INDEX_FOLDER)+account+File.separator+tablename;
		String root=Config.getString("lucene_index_folder");
		this.filename=root+this.dbname;
		
		if (!new File(this.filename).isDirectory()) {
			new File(this.filename).mkdirs();
			try{
				IndexWriter iw= new IndexWriter(new File(this.filename), new StandardAnalyzer(), true,IndexWriter.MaxFieldLength.LIMITED);
				iw.optimize();
				iw.close();
				log.info("Lucene Index created");
			}catch(Exception e){
				log.error("ERROR in creating index: MSG:"+e.getMessage());
			}
			 
		}
		
	}
	
	public synchronized IndexReader getIndexReader() throws Exception {
		
		return IndexReader.open(this.filename);
	}
	
	public synchronized IndexWriter getIndexWriter() throws Exception {
		Directory idir=FSDirectory.getDirectory(new File(this.filename));
		if(IndexWriter.isLocked(idir)){
			//LuceneLog.log(LuceneLog.STATUS_FAILED, this.account, this.tablename, 0, "Forced to unlock the directory");
			log.error("Forced to unclock index of file:"+this.filename);
			IndexWriter.unlock(idir);
		}
		IndexWriter idx=new IndexWriter(new File(this.filename), new StandardAnalyzer(), false,IndexWriter.MaxFieldLength.LIMITED);
		 
		return idx;
	}
	
	public synchronized IndexSearcher getIndexSearcher() throws Exception {
		 return new IndexSearcher(this.filename);  
	}
}


