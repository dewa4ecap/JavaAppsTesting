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
import org.tmatesoft.svn.core.wc.SVNMoveClient;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.SVNWCClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import com.fourelementscapital.scheduler.config.Config;

public abstract class SVNSyncFile {

private Logger log = LogManager.getLogger(SVNSync4RFunction.class.getName());
	
	private String svnurl=null ;//"svn://10.153.64.3/rfunctions_test";
	
	private String local ;//="C:\\temp_repotest1\\rfunctions";
	//private static String FILE_EXTENSIION=".r";
	
	//private String user="svnrams";
	//private String pwd="svnrams";
	
	private File dstPath = null;
	
	public abstract String getExtension();
	
	SVNClientManager cm=null;
	 

	/*
	private void init(String user, String pwd) {
		setupLibrary();		
	
		cm = SVNClientManager.newInstance(SVNWCUtil.createDefaultOptions(true),user,pwd);
 
		this.svnurl=Config.getString("svn_url_r");
		this.local=Config.getString("svn_local_path_r");
		this.dstPath = new File(this.local);

	}
	*/
	
	
	private void init(String user, String pwd, String svnurl, String svn_path) {
		setupLibrary();		
	
		cm = SVNClientManager.newInstance(SVNWCUtil.createDefaultOptions(true),user,pwd);
 
		this.svnurl=svnurl;
		this.local=svn_path;
		this.dstPath = new File(this.local);

	}
	
	public SVNSyncFile(String user, String pwd, String svn_url, String local_path ){
 
		init(user,pwd,svn_url,local_path);
	}
	
	public SVNSyncFile(String svn_url, String local_path ){
		String user=Config.getString("svn_user");
		String pwd=Config.getString("svn_pwd");
		init(user,pwd,svn_url,local_path);
	}
	

	public void syncFile(String function_name,String script, String message){
	
		try {
		
			SVNURL url = SVNURL.parseURIEncoded(this.svnurl);			

			log.debug("this.svnurl:"+this.svnurl);
			
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

		    File file=new File((local.endsWith(File.separator)? local:local+File.separator),function_name+getExtension());
		    
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
			cc.doCommit(new File[] {file}, false, message, false, true);
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}	
	
	
	public void deleteFile(String function_name){
		
		try {
		
			SVNURL url = SVNURL.parseURIEncoded(this.svnurl);			

			log.debug("this.svnurl:"+this.svnurl);
			
	 


		    File file=new File((local.endsWith(File.separator)? local:local+File.separator),function_name+getExtension());
		    
		    
		    			
			SVNWCClient wcc = this.cm.getWCClient();
			
			try{
				wcc.doDelete(file, true,false);
			}catch(SVNException e){
				log.error("Error occured while deleting, Error:"+e.getMessage());
			}
 
			SVNCommitClient cc = cm.getCommitClient();
			cc.doCommit(new File[] {file}, false, "deleted", false, true);
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void renameFile(String old_function, String new_function,String script, String message){
		
		try {
		
			SVNURL url = SVNURL.parseURIEncoded(this.svnurl);			

			log.debug("this.svnurl:"+this.svnurl);
	
			File n_file=new File((local.endsWith(File.separator)? local:local+File.separator),new_function+getExtension());
			File o_file=new File((local.endsWith(File.separator)? local:local+File.separator),old_function+getExtension());
			
			
			String line;
			BufferedReader reader = new BufferedReader(new StringReader(script));
		    StringBuffer sb=new StringBuffer();     
		    
		    BufferedWriter out = new BufferedWriter(new FileWriter(o_file));

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
				wcc.doInfo(o_file, SVNRevision.HEAD);
			}catch(SVNException e){
				if(e.getMessage().contains("is not under version control")){
					wcc.doAdd(o_file, false, false, false, true);
					log.debug("~~~~~ file:"+n_file+" is not under version control");
				}else{
					log.debug("~~~~~ file:"+n_file+" is under version control");
				}
			}
 			 
			
			SVNMoveClient mc = this.cm.getMoveClient();
			mc.doMove(o_file, n_file);
			    
			SVNCommitClient cc = cm.getCommitClient();
			cc.doCommit(new File[] {o_file,n_file}, false, message, false, true);
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}	
	
	
	
	public Vector<SVNLogEntry> log(String function_name) throws Exception {
		
		
		//File file=new File(local,"script_"+scheduler_id+FILE_EXTENSIION);
		  File file=new File((local.endsWith(File.separator)? local:local+File.separator),function_name+getExtension());
		  
		
		//Vector v=new Vector();
		Vector  v=null;
		if(file.exists()){
			SVNLogClient logc= this.cm.getLogClient();
			long limit=250; 
			
			LogEntryHandler leh=new LogEntryHandler();
			
			logc.doLog(new File[] {file}, SVNRevision.create(0),SVNRevision.create(0), SVNRevision.HEAD, false, true,limit,leh);
			
			
			while(leh.getLogMessages()==null){				
				Thread.sleep(100);
			}
			v=leh.getLogMessages();
			Thread.sleep(100);

		}
		return v;
		
	}
	

	
	
	
	
	
	public TreeSet<Long> revisions(String function_name) throws Exception {
		
		//File file=new File(local,"script_"+scheduler_id+FILE_EXTENSIION);
		  File file=new File((local.endsWith(File.separator)? local:local+File.separator),function_name+getExtension());
		  
		
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

	
	public Long firstRevision(String function_name) throws Exception {
		TreeSet<Long> rev=revisions(function_name);
		return rev.size()>0?rev.first():0;
	}
	
	public Long lastRevision(String function_name) throws Exception {
		TreeSet<Long> rev=revisions(function_name);
		return rev.size()>0?rev.last():0;
	}
	
	public String getWhatChanged(String function_name, Long c_rev) throws Exception {
		TreeSet<Long> rev=revisions(function_name);		
		Vector v=new Vector(rev);
		String diff=null;
		if(v.indexOf(c_rev)>0){
			long prev=(Long)v.get(v.indexOf(c_rev)-1);
			diff=diff(function_name,prev,c_rev.longValue());
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
	
	public String getLastChanged(String function_name) throws Exception {
		TreeSet<Long> rev=revisions(function_name);		
		Vector v=new Vector(rev);
		String diff=null;
		if(rev.size()>0 && v.indexOf(rev.last())>0){
			long prev=(Long)v.get(v.indexOf(rev.last())-1);
			diff=diff(function_name,prev,rev.last().longValue());
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
	
	
	
	
    public String diff(String function_name, long start, long end) throws Exception {
		
		//File file=new File(local,"script_"+scheduler_id+".R");
    	 File file=new File((local.endsWith(File.separator)? local:local+File.separator),function_name+getExtension());
    	  
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
	
    
    public String diffWC(String function_name, long rev) throws Exception {
		
		//File file=new File(local,"script_"+scheduler_id+".R");
		//File file1=new File(local,"script_"+scheduler_id+"_rev"+start+"_rev"+end+".diff");
    	File file=new File((local.endsWith(File.separator)? local:local+File.separator),function_name+getExtension());
    	
		String rtn=null;
		if(file.exists()){
			SVNDiffClient diffc= this.cm.getDiffClient();
			
			//StringWriter sw=new StringWriter();
			//BufferedWriter out = new BufferedWriter(sw);
			//FileOutputStream fos=new FileOutputStream(file1) ;
			 ByteArrayOutputStream fos = new ByteArrayOutputStream( );       
			 diffc.doDiff(file, SVNRevision.create(rev),SVNRevision.create(rev),SVNRevision.HEAD, true, false,fos );
			 rtn=fos.toString();
		}else{
			throw new Exception("File:"+file.getPath()+" doesn't exist");
		}
		return rtn;
	}
   
	
 
    public String getScript(String function_name, long revision,String path) throws Exception {
    	
    	SVNRepository repository=this.cm.createRepository( SVNURL.parseURIEncoded(this.svnurl), false);
    	
    	String filepath=function_name+getExtension();
    	if(path!=null && !path.trim().equals("")){
    		if(path.indexOf(",")>=0){
    			StringTokenizer st=new StringTokenizer(path,",");
    			boolean found=false;
    			while(st.hasMoreTokens() && !found){
    				String thispath=st.nextToken();
    				SVNNodeKind nodeKind1 = repository.checkPath( thispath ,revision );
    				if(nodeKind1==SVNNodeKind.FILE){
    					filepath=thispath;
    					found=true;
    				}
    			}
    		}else{
    			filepath=path;
    		}
    	}
    	
    	
    	
    	log.debug("filepath:"+filepath);   	
    	 
    	SVNNodeKind nodeKind = repository.checkPath( filepath ,revision );
    	
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
}


