/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fe.svn;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNProperty;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNCommitClient;
import org.tmatesoft.svn.core.wc.SVNDiffClient;
import org.tmatesoft.svn.core.wc.SVNLogClient;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.SVNWCClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import com.fourelementscapital.scheduler.config.Config;

public class SVNSync {

	
	private Logger log = LogManager.getLogger(SVNSync.class.getName());
	
	private String svnurl="svn://10.153.64.3/scheduler";
	private String local="C:\\temp_repotest1\\sc3";	
	//private String user="svnrams";
	//private String pwd="svnrams";
	
	private File dstPath = null;	 
	
	SVNClientManager cm=null;
	public SVNSync(String user, String pwd){
		init(user,pwd);
	}

	private void init(String user, String pwd) {
		setupLibrary();		
	
		cm = SVNClientManager.newInstance(SVNWCUtil.createDefaultOptions(true),user,pwd);
 
		this.svnurl=Config.getString("svn_url");
		this.local=Config.getString("svn_local_path");
		this.dstPath = new File(this.local);

	}
	
	public SVNSync(){
		String user=Config.getString("svn_user");
		String pwd=Config.getString("svn_pwd");
		init(user,pwd);
	}
	

	public long syncScript(int scheduler_id,String script, String message){
		long revision=-1;
		try {
		
			SVNURL url = SVNURL.parseURIEncoded(this.svnurl);
			
			
			 /*
		     * SVN operations
		     */
			//SVNClientManager cm = SVNClientManager.newInstance(SVNWCUtil.createDefaultOptions(true),user,pwd);
			SVNUpdateClient uc = this.cm.getUpdateClient();
			
			if(!SVNWCUtil.isVersionedDirectory(dstPath)){				
				uc.doCheckout(url, dstPath, SVNRevision.UNDEFINED, SVNRevision.HEAD, true);
				uc.doUpdate(dstPath, SVNRevision.HEAD, true);
			}else{
				log.debug("~~~~~~"+dstPath.getPath()+" it is already working copy....");
			}
			
			/*
			 creating file   
			 */
			String line;
			BufferedReader reader = new BufferedReader(new StringReader(script));
		    StringBuffer sb=new StringBuffer();	     

		    File file=new File(local,"script_"+scheduler_id+".R");
		    
		    BufferedWriter out = new BufferedWriter(new FileWriter(file));

		    while ((line = reader.readLine()) != null)
		    {	
		    	if(line!=null && line.equals("")){		    	 
		    		out.write(line+"\n");
		    	}
		    	if(line!=null && !line.equals("")){		    	 
		    		out.write(line+"\n");
		    	}
		    }
		    out.close();
		    reader.close();
		    
		   
			
			SVNWCClient wcc = this.cm.getWCClient();
			
			try{
				wcc.doInfo(file, SVNRevision.HEAD);
			}catch(SVNException e){
				if(e.getMessage().contains("is not under version control")){
					wcc.doAdd(file, false, false, false, true);
					log.debug("~~~~~ file:"+file+" is not under version control");
				}else{
					log.debug("~~~~~ file:"+file+" is under version control");
				}
			}
 
			SVNCommitClient cc = cm.getCommitClient();
			SVNCommitInfo info= cc.doCommit(new File[] {file}, false, message, false, true);
			
			revision=info.getNewRevision();
			//System.out.println("~~~~~~~~~~~~~~~SVNSync.syncScript() version:"+info.getNewRevision());
			//System.out.println("~~~~~~~~~~~~~~~SVNSync.syncScript() toString:"+info.toString());
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return revision;
	}	
	
	
	
	public String getLastChanged(int scheduler_id) throws Exception {
		
		String filename="script_"+scheduler_id+".R";
		File file=new File(local,filename);
		
		TreeSet<Long> rev=revisions(file);		
		Vector v=new Vector(rev);
		String diff=null;
		if(rev.size()>0 && v.indexOf(rev.last())>0){
			long prev=(Long)v.get(v.indexOf(rev.last())-1);
			diff=diff(scheduler_id,prev,rev.last().longValue());
			if(diff!=null){
				StringTokenizer st=new StringTokenizer(diff,"\n\r");
				if(st.countTokens()>=4){
					st.nextToken();st.nextToken();st.nextToken();st.nextToken();//st.nextToken();
					diff="";
					while(st.hasMoreTokens()){
						diff+=st.nextToken()+"\n";
					}
				}
				
			}
		}
		return diff;
	}
	
	
	public Vector<SVNLogEntry> log(int scheduler_id) throws Exception {
		
		
		File file=new File(local,"script_"+scheduler_id+".R");
		
		//Vector v=new Vector();
		Vector  v=null;
		if(file.exists()){
			SVNLogClient logc= this.cm.getLogClient();
			long limit=250; 
			
			LogEntryHandler leh=new LogEntryHandler();
			
			logc.doLog(new File[] {file}, SVNRevision.create(0),SVNRevision.create(0), SVNRevision.HEAD, true, true,limit,leh);
			
			
			while(leh.getLogMessages()==null){				
				Thread.sleep(100);
			}
			v=leh.getLogMessages();
			Thread.sleep(100);

		}
		return v;
		
	}
	
	public Vector<SVNDirEntry> logTest() throws Exception {
		 
			SVNLogClient logc= this.cm.getLogClient();
			long limit=50; 
			
			LogEntryHandler leh=new LogEntryHandler();
			SVNURL url = SVNURL.parseURIEncoded(this.svnurl);
			//logc.doList(url, SVNRevision.create(0), SVNRevision.HEAD, true, true,leh);
			
			//while(leh.getLogDirMessages()==null){				
			//	Thread.sleep(100);
			//}
			//Vector v=leh.getLogMessages();
			logc.doLog(url,new String[] {}, SVNRevision.create(0),SVNRevision.create(0), SVNRevision.HEAD, true, true,1,leh);
			
			while(leh.getLogMessages()==null){				
				Thread.sleep(100);
			}
			Vector v=leh.getLogMessages();
			
			Thread.sleep(100);
			return v;
			 
 
		
	}
	
    public String diff(int scheduler_id, long start, long end) throws Exception {
		
		File file=new File(local,"script_"+scheduler_id+".R");
		//File file1=new File(local,"script_"+scheduler_id+"_rev"+start+"_rev"+end+".diff");
		String rtn=null;
		if(file.exists()){
			SVNDiffClient diffc= this.cm.getDiffClient();
			
			//StringWriter sw=new StringWriter();
			//BufferedWriter out = new BufferedWriter(sw);
			//FileOutputStream fos=new FileOutputStream(file1) ;
			 ByteArrayOutputStream fos = new ByteArrayOutputStream( );       
			 diffc.doDiff(file, SVNRevision.create(start),SVNRevision.create(start),SVNRevision.create(end), true, false,fos );
			 rtn=fos.toString();
		}
		return rtn;
	}
	
    
    public String diffWC(int scheduler_id, long rev) throws Exception {
		
		File file=new File(local,"script_"+scheduler_id+".R");
		//File file1=new File(local,"script_"+scheduler_id+"_rev"+start+"_rev"+end+".diff");
		String rtn=null;
		if(file.exists()){
			SVNDiffClient diffc= this.cm.getDiffClient();
			
			//StringWriter sw=new StringWriter();
			//BufferedWriter out = new BufferedWriter(sw);
			//FileOutputStream fos=new FileOutputStream(file1) ;
			 ByteArrayOutputStream fos = new ByteArrayOutputStream( );       
			 diffc.doDiff(file, SVNRevision.create(rev),SVNRevision.create(rev),SVNRevision.HEAD, true, false,fos );
			 rtn=fos.toString();
		}
		return rtn;
	}
   
	
 
    public String getScript(int scheduler_id, long revision) throws Exception {
    	
    	String filepath="script_"+scheduler_id+".R";
    	
    	SVNRepository repository=this.cm.createRepository( SVNURL.parseURIEncoded(this.svnurl), false);
    	SVNNodeKind nodeKind = repository.checkPath( filepath , -1 );
    	if ( nodeKind == SVNNodeKind.NONE ) {
             throw new Exception( "There is no entry at '" + this.svnurl + "'." );
              
        } else if ( nodeKind == SVNNodeKind.DIR ) {
        	 throw new Exception( "The entry at '" + this.svnurl + "' is a directory while a file was expected." );
              //System.exit( 1 );
        }
    	
    	SVNProperties fileProperties = new SVNProperties( );
        ByteArrayOutputStream baos = new ByteArrayOutputStream( );        
        repository.getFile( filepath , revision, fileProperties , baos );
        
        
        
        Map fileProperties1=fileProperties.asMap();
        String mimeType =  (String)fileProperties1.get( SVNProperty.MIME_TYPE );
        boolean isTextType = SVNProperty.isTextMimeType( mimeType );

        
        /*
        
        Iterator iterator = fileProperties1.keySet( ).iterator( );
        while ( iterator.hasNext( ) ) {
            String propertyName = ( String ) iterator.next( );
            String propertyValue = ( String ) fileProperties1.get( propertyName );
           // System.out.println( "File property: " + propertyName + "=" + propertyValue );
        }
         */
        
        String rtn=null;
        
        if ( isTextType ) {
           // System.out.println( "File contents:" );
            //System.out.println( );
            
                //baos.writeTo( System.out );
                //baos.writeTo( sw );
            	rtn=baos.toString();
            	
             
        } else {
            System.out.println( "Not a text file." );
        }
        return rtn;
    	
    }
    
	
	  /*
     * Initializes the library to work with a repository via 
     * different protocols.
     */
    private static void setupLibrary() {
        /*
         * For using over http:// and https://
         */
        DAVRepositoryFactory.setup();
        /*
         * For using over svn:// and svn+xxx://
         */
        SVNRepositoryFactoryImpl.setup();
        
        /*
         * For using over file:///
         */
        FSRepositoryFactory.setup();
    }
    
    public TreeSet<Long> revisions( File file) throws Exception {
		
		//File file=new File(local,"script_"+scheduler_id+FILE_EXTENSIION);
		   
		
		//Vector v=new Vector();
		  /*
		TreeSet  v=new TreeSet(new Comparator(){
			 
			public int compare(Object o1, Object o2) {		 
				long i1=((Long)o1).longValue();
				long i2=((Long)o2).longValue();
				if(i1>i2) return -1;
				else if(i1 > i2)	return +1;
				else return 0;
			}
			
		}); */
    	
		TreeSet  v=new TreeSet();
		
		if(file.exists()){
			SVNLogClient logc= this.cm.getLogClient();			 
			
			LogEntryHandler leh=new LogEntryHandler();
			
			logc.doLog(new File[] {file}, SVNRevision.create(0),SVNRevision.create(0), SVNRevision.HEAD, true, true,0,leh);
			
			
			while(leh.getLogMessages()==null){				
				Thread.sleep(100);
			}
			Vector logs=leh.getLogMessages();			
			
			Thread.sleep(100);
			
			for(Iterator it=logs.iterator();it.hasNext(); ){
				SVNLogEntry log=(SVNLogEntry)it.next();
				v.add(log.getRevision());
			}
				

		}
		return v;
		
	}

}


