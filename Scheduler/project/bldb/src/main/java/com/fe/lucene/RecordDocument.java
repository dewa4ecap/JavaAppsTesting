/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fe.lucene;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

 

public class RecordDocument {
	
	 public static Document Document(String content, String ticker, Map fields)
     throws java.io.FileNotFoundException {
		 Document doc = new Document();
		 doc.add(new Field("content", content, Field.Store.YES, Field.Index.TOKENIZED));
		 doc.add(new Field("id", ticker, Field.Store.YES, Field.Index.UN_TOKENIZED));
		 //doc.add(new Field("ticker", ticker, Field.Store.YES, Field.Index.UN_TOKENIZED));
		// doc.add(new Field("security", ticker, Field.Store.YES, Field.Index.UN_TOKENIZED));
		 //doc.add(new Field("contract", ticker, Field.Store.YES, Field.Index.UN_TOKENIZED));
		 //doc.add(new Field("commodity", ticker, Field.Store.YES, Field.Index.UN_TOKENIZED));
		 
		 for(Iterator i=fields.keySet().iterator();i.hasNext();){
			 String key=(String)i.next();
			 if(key!=null && !key.equals("")  && fields.get(key)!=null && fields.get(key)!=null && !fields.get(key).equals("") ){
				 doc.add(new Field(key, fields.get(key).toString(), Field.Store.YES, Field.Index.TOKENIZED));
			 }
		 } 
		 return doc;
	 }

}



