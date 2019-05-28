/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fe.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.jcs.JCS;
import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tmatesoft.svn.core.SVNLogEntry;

import com.fe.common.Constant;
import com.fe.svn.SVNSync4RFunction;
import com.fe.util.WikiRFunctionManual;
import com.fourelementscapital.auth.UserThemeAccessPermission;
import com.fourelementscapital.db.RFunctionDB;
import com.fourelementscapital.db.SchedulerDB;
import com.fourelementscapital.db.vo.ValueObject;
import com.fourelementscapital.fileutils.FindStringInFiles;
import com.fourelementscapital.scheduler.config.Config;
import com.fourelementscapital.scheduler.error.ClientError;


/**
 * This class exposes data to R function editor in Ajax call 
 * Remember all methods in the class are required user to be logged in
 */
public class RFunctionMgmt extends AbstractTeamOrgMgmt {
	
	private HttpServletRequest request=null;
	private Logger log = LogManager.getLogger(RFunctionMgmt.class.getName());
	private static JCS lockcache=null;	
	private  String cache_packaged_key="packaged_functions";	
	public static String RFUNCTION_FILE_EXTENSION=".r";
	
	private static String FUNCTION_ID="function_id";
	private static String LOCK_DURATION="duration";
	private static String USER="user";

	

	/**
	 * DWR invocation 
	 * @throws Exception
	 */
	public RFunctionMgmt() throws Exception {
		super();
	}
	
	/**
	 * JSP or internal invocation 
	 * @param request
	 * @throws Exception
	 */
	public RFunctionMgmt(HttpServletRequest request) throws Exception {
		super(request);
 
	}


	/**
	 * JCS caching for speed data retrival and not necessary to hit the database for every user.
	 * @return
	 * @throws Exception
	 */
	private static JCS getLockCache() throws Exception {
		if(RFunctionMgmt.lockcache==null) RFunctionMgmt.lockcache=JCS.getInstance("lock-cache");
		return RFunctionMgmt.lockcache;
	}
	
	
	
	
	/**
	 * Locking r function while a user is editing 
	 * @param function_id
	 * @param seconds
	 * @return
	 * @throws Exception
	 */
	public boolean lockFunction(int function_id, long seconds) throws Exception {
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		RFunctionDB rfdb=RFunctionDB .getRFunctionDB();
		try{
			sdb.connectDB();
			rfdb.connectDB();
			String usr=new SchedulerMgmt(getRequest()).getAuthorizedUser(sdb);
			if(seconds>0){			   			 
			   refreshCache(function_id, seconds,usr);		
			   
			   //rtn=cachedPeers.getMatching("^[A-Za-z0-9]+$");
			}else{
				rfdb.updateLock(function_id, usr);
			}
			return true;
		}catch(Exception e){
		
			throw e;
		}finally{
			sdb.closeDB();
			rfdb.closeDB();
		}
		
	}

	
	/**
	 * provides the user name who is currently locked R function or editing 
	 * @param function_id
	 * @param sdb
	 * @param rfdb
	 * @return
	 * @throws Exception
	 */
	private String getLockedBy(int function_id,SchedulerDB sdb,RFunctionDB rfdb) throws Exception {
	 
		try{
		 
			String usr=new SchedulerMgmt(getRequest()).getAuthorizedUser(sdb);
			Map data=rfdb.getRFunction(function_id);
			String rtn=null;
			if(data.get("lockedby")!=null ){			   			 
 				rtn=(String)data.get("lockedby");
			}else{
 				Map caches=getLockCache().getMatching("^[A-Za-z0-9]+$");
				if(caches!=null){
					for(Iterator i=caches.keySet().iterator();i.hasNext();){
						String ky=(String)i.next();
						
							Map d=(Map)caches.get(ky);
							try{
								int f_id=(Integer)d.get(RFunctionMgmt.FUNCTION_ID);
								long dur=(Long)d.get(RFunctionMgmt.LOCK_DURATION);
								String usr1=(String)d.get(RFunctionMgmt.USER);							
								if(f_id==function_id) rtn=usr1;
							}catch(Exception e){
								 
							}
							
					}
				}	
			}
			return rtn;
		}catch(Exception e){
		
			throw e;
		}finally{
			 
		}
	}
	
	/**
	 * deletes R function, which basically moves the r function to trash, internal there is flag set for example deleted=1 to identify the deleted item and later can be removed permanently   
	 * @param function_id
	 * @return
	 * @throws Exception
	 */
	public boolean deleteFunction(int function_id) throws  Exception {
		
		   
		RFunctionDB rfdb=RFunctionDB.getRFunctionDB();
		try{
		   rfdb.connectDB();
		   Map fld=createFolderIfNotExist("Trash","trash");
		   int folder_id=(Integer)fld.get("folder_id");
		   moveFile2Folder(function_id, folder_id);		   
		   rfdb.updateFunctionDeleted(function_id);
		   return true;
		   
		}catch(Exception e){
			
			throw e;
			
		}finally{
			
			rfdb.closeDB();
		}
	}
	
	
	/**
	 * unlocks the function from the cache after certain time lapsed
	 * @param function_id
	 * @return
	 * @throws Exception
	 */
	public boolean unLockFunctionFromCache(int function_id) throws Exception {
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		try{
			sdb.connectDB();
			String usr=new SchedulerMgmt(getRequest()).getAuthorizedUser(sdb);
			removeLockFromCache(function_id,usr);
			return true;
		}catch(Exception e){	
			e.printStackTrace();
			throw e;
		}finally{
			sdb.closeDB();

		}
		
	}
	
	
	/**
	 * Locking the function
	 * @param function_id
	 * @return
	 * @throws Exception
	 */
	public boolean unLockFunction(int function_id) throws Exception {
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		RFunctionDB rfdb=RFunctionDB.getRFunctionDB();
		try{
			sdb.connectDB();
			rfdb.connectDB();
			String usr=new SchedulerMgmt(getRequest()).getAuthorizedUser(sdb);
			removeLockFromCache(function_id,usr);
			rfdb.updateLock(function_id, null);
			return true;
		}catch(Exception e){
		
			throw e;
		}finally{
			sdb.closeDB();
			rfdb.closeDB();
		}
		
	}
	
	/**
	 * editor active detected, so the system can alert if the user is not saved while closing the tab without saving his work
	 * @return
	 * @throws Exception
	 */
	public boolean editorActiveDetected() throws Exception{
		
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		 
		try{
			sdb.connectDB();
			
			String usr=new SchedulerMgmt(getRequest()).getAuthorizedUser(sdb);
		 
			Map data=getLockCache().getMatching("^[A-Za-z0-9]+$");
			if(data!=null){
				for(Iterator i=data.keySet().iterator();i.hasNext();){
					String ky=(String)i.next();
					if(ky.startsWith(usr)){
						Map d=(Map)data.get(ky);
						int f_id=(Integer)d.get(RFunctionMgmt.FUNCTION_ID);
						long dur=(Long)d.get(RFunctionMgmt.LOCK_DURATION);
						String usr1=(String)d.get(RFunctionMgmt.USER);
						if(usr1.equals(usr)) {refreshCache(f_id,dur,usr1);}
					}
				}
			}			
			return true;
		}catch(Exception e){
		
			throw e;
		}finally{
			sdb.closeDB();
			
		}
		
		
	}

 

	
	
	
	private void removeLockFromCache(int function_id, String usr ) throws Exception {
	       //String ky=usr+"_"+function_id;
		   String ky=usr+function_id;
		   if(getLockCache().get(ky)!=null)getLockCache().remove(ky);
		
	}

	private void refreshCache(int function_id, long seconds, String usr ) throws Exception {
		
	       //String ky=usr+"_"+function_id;
		   String ky=usr+function_id;
	       
		   HashMap h=new HashMap();
		   h.put(RFunctionMgmt.FUNCTION_ID, function_id);
		   h.put(RFunctionMgmt.LOCK_DURATION, seconds);
		   h.put(RFunctionMgmt.USER,  usr);
		   
		   IElementAttributes att= getLockCache().getDefaultElementAttributes();
		   att.setMaxLifeSeconds(seconds);
		   if(getLockCache().get(ky)!=null)getLockCache().remove(ky);
		   getLockCache().put(ky,h,att);

		
	}
	
	
	/**
	 * Get folder panel data, this data will be used to render tree menus 
	 * @param open_functions
	 * @param readonly
	 * @param ignore_treedata
	 * @return
	 * @throws Exception
	 */
	public Map getFolderPanelData(String open_functions,boolean readonly,boolean ignore_treedata) throws Exception {
		
		RFunctionDB rfdb=RFunctionDB.getRFunctionDB();
	 
		
		try{
			
			HashMap rtn=new HashMap();
			
			rfdb.connectDB();
	 
			
			if(!ignore_treedata){
				List functions=rfdb.listRFunctions();
				List glist=rfdb.listFunctionGroups();
				List<Map> folders=rfdb.listOfFolders();
				
				HashMap ufolders=new HashMap();
				for(Map fold: folders){
					if(fold.get("folder_name")!=null)ufolders.put(fold.get("folder_name"),new Integer(0));
				}
				
				
				List unbuildfunc=getUnPackagedSources(ufolders);
				
				//TreeMap groups=new TreeMap();
				Vector groups=new Vector();
				HashMap colors=new HashMap();			
				for(Iterator it=glist.iterator();it.hasNext();){
					Map data=(Map)it.next();
	                //groups.put(data.get("group_uid"),data.get("group_name"));
					
					ValueObject vo=new ValueObject();
					vo.setKey((String)data.get("group_uid"));
					vo.setValue((String)data.get("group_name"));
					groups.add(vo);
	                colors.put(data.get("group_uid"), data.get("color_code"));
				}
				
				rtn.put("func_2build", unbuildfunc);
				rtn.put("folder_2build", ufolders);
				rtn.put("rfunctions", functions);
				rtn.put("folders", folders);
				rtn.put("groups",groups);
				rtn.put("tags",rfdb.getTags());
				rtn.put("group_colors",colors);
				rtn.putAll(getThemeAccessData(rfdb)); //putting team organization data example: tags
			}
			if(open_functions!=null){
				rtn.put("open_functions", getRFunctions(open_functions, readonly));
			}
			rtn.put("sourced_functions",getSourcedFunctions());
			rtn.put("lite",ignore_treedata);
			
			return rtn;
			
			
		}catch(Exception e){
			ClientError.reportError(e, null);
			throw e;	
		}finally{
			rfdb.closeDB();
		 
		}
	}
	
	/**
	 * getting notifiable tags for the current user for that 
	 * @param themes
	 * @param ftags
	 * @return
	 * @throws Exception
	 */
	public Map getItemPrivilegeNotifications(ArrayList themes,ArrayList ftags) throws Exception {
		   RFunctionDB rfdb=RFunctionDB.getRFunctionDB();
			try{				
				rfdb.connectDB();		
				return  getItemPrivilegeNotifications(themes,ftags,rfdb);
				//return code;
			}catch(Exception e){
				ClientError.reportError(e, null);
				throw e;
			}finally{
				rfdb.closeDB();
			}
		   
		   
	}

	   

	/**
	 * Update function order upon dragging and dropping vertical direction in the UI.
	 * @param folder_id
	 * @param function_ids
	 * @return
	 * @throws Exception
	 */
	public boolean updateFunctionOrder(int folder_id, int[] function_ids) throws Exception {
		RFunctionDB rfdb=RFunctionDB.getRFunctionDB();
		
		try{
			rfdb.connectDB();
	 
			rfdb.updateFunctionOrder(function_ids);
			String dest_folder=rfdb.getFolderName(folder_id);
			List function_order=rfdb.listAllRScriptNames(folder_id);	
			addLineInSource(dest_folder, null, function_order,new Integer(0)); //synchronize the order on source file.
			return true;
		}catch(Exception e){
			ClientError.reportError(e, null);
			throw e;	
		}finally{
			rfdb.closeDB();
		}
		
	}
	
	/**
	 * Saves the order of the folders, this will be called after drag event completes on the UI.
	 * @param folder_ids
	 * @return
	 * @throws Exception
	 */
	public boolean updateFolderOrder(int[] folder_ids) throws Exception {
		RFunctionDB rfdb=RFunctionDB.getRFunctionDB();
		
		try{
			rfdb.connectDB();
			rfdb.updateFolderOrder(folder_ids);
			syncFoldersWith4ESource(rfdb);
			return true;
		}catch(Exception e){
			ClientError.reportError(e, null);
			throw e;	
		}finally{
			rfdb.closeDB();
		}
		
	}
	
	
	/**
	 * return data for folder generation.
	 * @return
	 * @throws Exception
	 */
	public Map getFolderTree() throws Exception {
		
		RFunctionDB rfdb=RFunctionDB.getRFunctionDB();
		
		try{
			HashMap rtn=new HashMap();
			
			rfdb.connectDB();			
			List glist=rfdb.listFunctionGroups();
			List folders=rfdb.listOfFolders();
			
			//TreeMap groups=new TreeMap();
			Vector groups=new Vector();
						
			for(Iterator it=glist.iterator();it.hasNext();){
				Map data=(Map)it.next();
                //groups.put(data.get("group_uid"),data.get("group_name"));
				
				ValueObject vo=new ValueObject();
				vo.setKey((String)data.get("group_uid"));
				vo.setValue((String)data.get("group_name"));
				groups.add(vo);

			}
			rtn.put("folders", folders);
			rtn.put("groups",groups);

			return rtn;
			
			
		}catch(Exception e){
			ClientError.reportError(e, null);
			throw e;	
		}finally{
			
		}
	}
	
	
	/**
	 * Saves the group order, this method will be called only on 
	 * @param groupids
	 * @throws Exception
	 */
	 public void setGroupOrder(Vector groupids) throws Exception {
		 RFunctionDB rfdb=RFunctionDB.getRFunctionDB();;
			try{
				rfdb.connectDB();
				rfdb.setGroupOrder(groupids);
				syncFoldersWith4ESource(rfdb);
			}catch(Exception e){
				ClientError.reportError(e, null);
				throw e;
			}finally{
				rfdb.closeDB();
			}
	}
	
	
	 /**
	  * get R function for generating editor on UI 
	  * @param function_id
	  * @param readonly
	  * @return
	  * @throws Exception
	  */
	public Map getRFunction(int function_id,boolean readonly) throws Exception {
		
		RFunctionDB rfdb=RFunctionDB.getRFunctionDB();
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		try{
			
			
			rfdb.connectDB();
			sdb.connectDB();
			Map data=rfdb.getRFunction(function_id);
			//rtn.put("lockedby", getLockedBy(function_id));
			
			String function_name=(String)data.get("function_name");
			String wiki=getFunctionWiki(function_name);
			if(wiki.contains("\"noarticletext\"")){
				rfdb.updateWikiDone(function_id, 0);				
			}else{
				rfdb.updateWikiDone(function_id, 1);
			}
			
			
			
			Map rtn=getDataBundle4Function(data,rfdb,sdb);

			String usr_alreadylocked=(String)((Map)rtn.get("data")).get("lockedby");
			String usr=new SchedulerMgmt(getRequest()).getAuthorizedUser(sdb);	
			
			
			
			String access=getAccessPrivilege(function_id, rfdb);			
			if(access==null || (access!=null && access.equals(""))){		
				rtn.put("access", ACCESS_PRIVILEGE_R);
				access=ACCESS_PRIVILEGE_R;
			}else{
				rtn.put("access", access);
			}			
			rtn.put("isAuthorized", isAuthorizedUser(sdb)) ;
			rtn.put("authorizedUser", getAuthorizedUser(sdb));
			rtn.put("tag_follow", getItemTags2(function_id,rfdb));
			
			
			
			//System.out.println("RFunctionMgmt.getRFunction():usr:"+usr+" usr_alreadylocked:"+usr_alreadylocked);
			if(!readonly){				
				if(usr_alreadylocked!=null && !usr_alreadylocked.equals("") && !usr_alreadylocked.equalsIgnoreCase(usr)){
					//dont relock
				}else{		
					if(access.equalsIgnoreCase(ACCESS_PRIVILEGE_RWX)){
						refreshCache(function_id, 300,usr);	
					}
				}				
			}else{
				rtn.put("readonly", true);
			}
			
			return rtn;
			
			
		}catch(Exception e){			
			ClientError.reportError(e, null);
			throw e;	
		}finally{
			rfdb.closeDB();
			sdb.closeDB();
		}
		
	}
	
	
	/**
	 * This function updates wiki page done icon in the tree list.
	 * @param start
	 * @param end
	 * @return
	 * @throws Exception
	 */
	public boolean updateAllWikiIcon(int start,int end) throws Exception {
		
		RFunctionDB rfdb=RFunctionDB.getRFunctionDB();
		try{
			rfdb.connectDB();		
			Map<Integer,String> fnames=rfdb.getAllFunctionNamesID(" (is_wiki_done<>1 or is_wiki_done is null) AND id BETWEEN "+start+" AND "+end );			 
			for(Integer function_id: fnames.keySet()){
				 
				String fname=fnames.get(function_id);
				String wiki=getFunctionWiki(fname);
				log.info("updating rfunction:"+fname);				
				if(wiki.contains("\"noarticletext\"")){
					rfdb.updateWikiDone(function_id, 0);				
				}else{
					rfdb.updateWikiDone(function_id, 1);
				}
			}
			return true;
		}catch(Exception e){
			ClientError.reportError(e, null);
			throw e;	
		}finally{
			rfdb.closeDB();
			 
		}
	}

	/**
	 * 
	 * @param function_names
	 * @param readonly
	 * @return
	 * @throws Exception
	 */
	public Vector getRFunctions(String function_names,boolean readonly) throws Exception {
		
		RFunctionDB rfdb=RFunctionDB.getRFunctionDB();
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		try{
	
			StringTokenizer st=new StringTokenizer(function_names,",");
			Vector fnames=new Vector();
			while(st.hasMoreTokens()){
				fnames.add(st.nextToken());
			}
			Vector rtn=new Vector();
			rfdb.connectDB();
			sdb.connectDB();
			//Map data=rfdb.getRFunction(function_id);
			//rtn.put("lockedby", getLockedBy(function_id))
			Vector data1=rfdb.getRFunctions(fnames);
			
			String usr=new SchedulerMgmt(getRequest()).getAuthorizedUser(sdb);
			
			for(Iterator i=data1.iterator();i.hasNext();){
				Map data=(Map)i.next();
				Map func=getDataBundle4Function(data,rfdb,sdb);
				int function_id=(Integer)data.get("id");
				String usr_alreadylocked=(String)((Map)func.get("data")).get("lockedby");
				
				String access=getAccessPrivilege(function_id, rfdb);			
				if(access==null || (access!=null && access.equals(""))){		
					func.put("access", ACCESS_PRIVILEGE_R);
					access=ACCESS_PRIVILEGE_R;
				}else{
					func.put("access", access);
				}	
				
				if(!readonly){				
					if(usr_alreadylocked!=null && !usr_alreadylocked.equals("") && !usr_alreadylocked.equalsIgnoreCase(usr)){
						//dont relock
					}else{			
						if(access.equalsIgnoreCase(ACCESS_PRIVILEGE_RWX)){
							refreshCache(function_id, 300,usr);
						}
					}
				}else{
					func.put("readonly", true);
				}
				
					
				func.put("isAuthorized", isAuthorizedUser(sdb)) ;
				func.put("authorizedUser", getAuthorizedUser(sdb));
				func.put("tag_follow", getItemTags2(function_id,rfdb));
				
				rtn.add(func);
			}
			return rtn;

			
		}catch(Exception e){
			ClientError.reportError(e, null);
			throw e;	
		}finally{
			rfdb.closeDB();
			sdb.closeDB();
		}
		
	}

	
	/**
	 * get data bundle for R function, this includes r code and lock information 
	 * @param data
	 * @param rfdb
	 * @param sdb
	 * @return
	 * @throws Exception
	 */
	private Map getDataBundle4Function(Map data, RFunctionDB rfdb,SchedulerDB sdb) throws Exception{
		HashMap rtn=new HashMap();
		String folder=Config.getString("r_function_source_folder");
		String pathurl=Config.getString("r_function_path_url_prefix");
		
	    String subfolder=rfdb.getFolderName((Integer)data.get("folder_id"));
	    int function_id=(Integer)data.get("id");
	    
	    String filepath=(String)data.get("script_file");
	    String scriptname=filepath;
	    filepath=folder+((subfolder!=null)?subfolder+File.separator:"")+filepath;
		String content=getContent(filepath);
		String tl=(String)data.get("lockedby");
		if(tl==null || (tl!=null && tl.equals("")) ){
			try{
			data.put("lockedby",getLockedBy(function_id, sdb, rfdb));
			}catch(Exception e){
				log.error("getDataBundle4Function() error:"+e.getMessage()); 
			}
		}
		rtn.put("content", content);
		rtn.put("data", data);
		rtn.put("isAuthorized", new SchedulerMgmt(getRequest()).isAuthorizedUser(sdb));
		rtn.put("authorizedUser", new SchedulerMgmt(getRequest()).getAuthorizedUser(sdb));
		rtn.put("path", pathurl+"\\"+subfolder+"\\"+scriptname);
		return rtn;
	}
	
	
	
	/**
	 * called on moving folder up or down 
	 * @param folder_id
	 * @param new_group_id
	 * @return
	 * @throws Exception
	 */
	public boolean moveFolder(int folder_id, String new_group_id) throws Exception {
		 
		RFunctionDB rfdb=RFunctionDB.getRFunctionDB();
		
		try{
			
			///Thread.sleep(3000);
			
			rfdb.connectDB();
			rfdb.moveFolder(folder_id, new_group_id);
			return true;
			
		}catch(Exception e){
			ClientError.reportError(e, "folder_id:"+folder_id+" new_group_id:"+new_group_id);
			throw e;	
		}finally{
			rfdb.closeDB();
		}
	}
	
	
	
	
	/**
	 * new function creation
	 * @param folder_id
	 * @param fname
	 * @param script
	 * @param function_type
	 * @return
	 * @throws Exception
	 */
	public Map createRFunction(String folder_id,String fname, String script,int function_type) throws Exception {
		
		String folder=Config.getString("r_function_source_folder");		
		RFunctionDB rfdb=RFunctionDB.getRFunctionDB();
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
 
					
		try{
			
			Pattern pattern = Pattern.compile( "^[a-z][a-zA-Z0-9.]{2,50}+$" );
			Matcher m = pattern.matcher( fname );  // Matchers are used both for matching and finding.
		    if(!m.matches()){
		    	throw new Exception("Illegal function name, It should start with lower case, It should be Alpha-numeric, Min 2 characters, Max 50 chars, No space and No special characters except dot (.)");
		    }
		    
			HashMap rtn=new HashMap();
			
			rfdb.connectDB();
			sdb.connectDB();
			
		    Map existing=rfdb.getRFunction(fname);
		    if(existing!=null && existing.size()>0){
		        throw new Exception("ERROR: Function name: "+fname+" alerady existing, please try different name");	
		    }
		    if(script==null || (script!=null && script.equals(""))){
		    	script="#"+fname+".r\n";
		    	script+="#created on "+new SimpleDateFormat("dd-MMM-yyyy HH:mm").format(new Date());
		    }
		   
		    //String filepath=(String)data.get("script_file");
		    String subfolder=rfdb.getFolderName(Integer.parseInt(folder_id));
		    
		    String filename=fname+RFUNCTION_FILE_EXTENSION;
		    String filepath=folder+((subfolder!=null)?subfolder+File.separator:"")+filename;
		    List function_names=rfdb.listAllRScriptNames(Integer.parseInt(folder_id));
		    if(!new File(filepath).isFile()){
		    	createContent(filepath,script,subfolder,filename,function_names, function_type);
		    }
		    
			int id=rfdb.createFunction(Integer.parseInt(folder_id),fname, filename,function_type);
			
			synchrnizeSVN(fname,script,null,rfdb,sdb,id);
			
		 
			Map data=rfdb.getRFunction(id);
 
			   
			String filepath1=(String)data.get("script_file");
		    filepath1=folder+((subfolder!=null)?subfolder+File.separator:"")+filepath1;

			
			String content=getContent(filepath1);
			Map func_data=new HashMap();
			func_data.put("content", content);
			func_data.put("data", data);
				
			
			Vector functions=new Vector();
			functions.add(data);
			
			
			rtn.put("rfunctions", functions);
			boolean readonly=true;
			rtn.put("func_data", getRFunction(id,!readonly));
			 
 
			
			return rtn;
		}catch(Exception e){
			ClientError.reportError(e, "folder_id:"+folder_id+" fname:"+fname+"  script:"+script);
			throw e;	
		}finally{
			rfdb.closeDB();
			sdb.closeDB();
		}
		
	}
	
	
	/**
	 * Called while modifying R function, and saves the data with message also will be synchronized with SVN repository
	 * @param function_id
	 * @param script
	 * @param message
	 * @param theme_tag_ids
	 * @param follow_tag_ids
	 * @return
	 * @throws Exception
	 */
	public Map modifyRFunction(int function_id, String script, String message, List theme_tag_ids, List follow_tag_ids) throws Exception {
		
		String folder=Config.getString("r_function_source_folder");		
		RFunctionDB rfdb=RFunctionDB.getRFunctionDB();
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		try{

			HashMap rtn=new HashMap();			
			rfdb.connectDB();
			sdb.connectDB();
			
			String usr=new SchedulerMgmt(getRequest()).getAuthorizedUser(sdb);
			String lockedby=getLockedBy(function_id, sdb, rfdb);
			if(lockedby!=null && !usr.equalsIgnoreCase(getLockedBy(function_id, sdb, rfdb))){
				throw new Exception("This function is currently locked by user "+lockedby);
			}
		  
			Map data=rfdb.getRFunction(function_id);
			
		    String filename=(String)data.get("script_file");
		    String function_name=(String)data.get("function_name");
		    Number is_class=(Number)data.get("is_class");
		    
		    String sub_folder="";
		    
		    try{
				sub_folder=rfdb.getFolderName((Integer)data.get("folder_id"));
			}catch(Exception e){}		    

		    String filepath=folder+((sub_folder!=null)?sub_folder+File.separator:"")+filename;
		    List function_order=rfdb.listAllRScriptNames((Integer)data.get("folder_id"));
		    createContent(filepath,script,sub_folder,filename,function_order,is_class);
		    
		    SVNSync4RFunction sync=synchrnizeSVN(function_name,script,message,rfdb,sdb,function_id);			
			
			String diff=sync.getLastChanged(function_name);
			 
			Map data1=rfdb.getRFunction(function_id); //this gets with latest tag after SVN sycnrhonize
			int oid=0;
			if(theme_tag_ids!=null && theme_tag_ids.size()>0){
				try{
					oid=Integer.parseInt(theme_tag_ids.get(0)+"");;
				}catch(Exception e){log.error("error while converting owner id, e:"+e.getMessage());}
			}
			rfdb.updatedOwnerIDNow(oid,function_id);
	    	Vector v=new Vector();
	    	v.add(data1);	    		
	    	
    		
	    	HashMap ufolders=new HashMap(); ufolders.put(sub_folder,(Integer)data.get("folder_id"));
    		 
	    	List unbuildfunc=getUnPackagedSources(ufolders);
			
 
			HashMap hdata=new HashMap();	 
			
			hdata.put("function_name", function_name);
			hdata.put("current_user",  getAuthorizedUser(sdb));
			hdata.put("diff", diff);
			hdata.put("comments", message);
		
			String templ_filename="function_modified_alert.txt";
			long rev=sync.lastRevision(function_name);
			updateAllItemTags(function_id, theme_tag_ids,follow_tag_ids,rfdb,sdb,function_name,  message,rev, diff, hdata, templ_filename) ;
	    	
	    		//return listScheduledItems();	    		
	    	rtn.put("rfunctions", v);
	    	rtn.put("function_id", function_id);
	    	rtn.put("func_2build", unbuildfunc);
	    	rtn.put("revisions", getSVNLogs(function_name));
	    	rtn.put("function_name", function_name);
	    	
	    	rtn.putAll(getThemeAccessData(rfdb));
	    	
	    	
	    	//notifyLastModification(rfdb,sdb,function_name,function_id,message);
	    	 
	    	
			return rtn;
		 
			
			
		}catch(Exception e){
			e.printStackTrace();
			//ClientErrorMgmt.reportError(e,null);
			
			throw e;	
		}finally{
			sdb.closeDB();
			rfdb.closeDB();
		}
		
	}
	
	
	
   private void createRFunction11(String folder_id,String fname, String script, String file_exten) throws Exception {
		
		String folder=Config.getString("r_function_source_folder");		
		RFunctionDB rfdb=RFunctionDB.getRFunctionDB();
		
		try{
			HashMap rtn=new HashMap();
			
			rfdb.connectDB();
			
		    Map existing=rfdb.getRFunction(fname);
		    if(existing!=null && existing.size()>0){
		        throw new Exception("ERROR: Function name: "+fname+" alerady existing, please try different name");	
		    }
		    if(script==null || (script!=null && script.equals(""))){
		    	script="#"+fname+".r\n";
		    	script+="#created on "+new SimpleDateFormat("dd-MMM-yyyy HH:mm").format(new Date());
		    }
		   
		    //String filepath=(String)data.get("script_file");
		    String subfolder=rfdb.getFolderName(Integer.parseInt(folder_id));
		    
		    String filename=fname+(file_exten==null?RFUNCTION_FILE_EXTENSION:file_exten);
		    String filepath=folder+((subfolder!=null)?subfolder+File.separator:"")+filename;
		    
		    if(!new File(filepath).isFile()){
		    	createContent(filepath,script,subfolder,filename,new Vector(),new Integer(0));
		    }
		    
		    //boolean isclass=false;
		    
			int id=rfdb.createFunction(Integer.parseInt(folder_id),fname, filename,RFunctionDB.FUNCTION_TYPE_NORMAL);
			
			synchrnizeSVN11(fname,script,null,rfdb,id);
		
			
			 
			
			
		}catch(Exception e){
			//ClientErrorMgmt.reportError(e, "folder_id:"+folder_id+" fname:"+fname+"  script:"+script);
			throw e;	
		}finally{
			rfdb.closeDB();
		}
		
	}
  
   
   private boolean renameSVN(String old_function_name,String new_function_name, String function, String message, RFunctionDB rfdb,int function_id) throws Exception {
		
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		
		try{
			sdb.connectDB();
			
			String svnuser=Config.getString("svn_user_r");
			String svnpwd=Config.getString("svn_pwd_r");
			
			SchedulerMgmt smgmt=new SchedulerMgmt(getRequest());
			String clientip=smgmt.getPeerIPAddress();
			String user=getAuthenticatedUser();
			
			if(message==null){
				message="created on IP:"+clientip+" By:"+user;
			}
			
 
		 
			Map u=sdb.getSVNUser4WikiUser(user);
			
			if(u!=null && u.get("svn_username")!=null && u.get("svn_password")!=null){
				svnuser=(String)u.get("svn_username");
				svnpwd=(String)u.get("svn_password");
			}
		 
			
			
			SVNSync4RFunction sync=new SVNSync4RFunction(svnuser,svnpwd);
			sync.renameFile(old_function_name, new_function_name, function, message);
			
			
		 
			String user1="usr-"+svnuser.trim().toLowerCase();
			int tag_id=rfdb.addIfTagNotExist(user1);			
			rfdb.updateLast2UsersTag(function_id,tag_id);
		 
		 
			return true;
		}catch(Exception e){
			log.error("Error while committing function into SVN ");
			throw e;
		}finally{
			try{sdb.closeDB();}catch(Exception e) {	log.error("error while cloing db:"+e.getMessage());	}
		}
		
		
	}
	
  
	private SVNSync4RFunction synchrnizeSVN(String function_name, String function, String message, RFunctionDB rfdb,SchedulerDB sdb,int function_id) throws Exception {
		
		 
		
		try{
		 
			
			String svnuser=Config.getString("svn_user_r");
			String svnpwd=Config.getString("svn_pwd_r");
			
			SchedulerMgmt smgmt=new SchedulerMgmt(getRequest());
			String clientip=smgmt.getPeerIPAddress();
			String user=getAuthenticatedUser();
			
			if(message==null){
				message="created on IP:"+clientip+" By:"+user;
			}
			
 
			 
			Map u=sdb.getSVNUser4WikiUser(user);
			
			if(u!=null && u.get("svn_username")!=null && u.get("svn_password")!=null){
				svnuser=(String)u.get("svn_username");
				svnpwd=(String)u.get("svn_password");
			}
		 
			
			
			SVNSync4RFunction sync=new SVNSync4RFunction(svnuser,svnpwd);
			sync.syncFile(function_name, function,message);
			
 
			if(false){
				String user1="usr-"+svnuser.trim().toLowerCase();
				int tag_id=rfdb.addIfTagNotExist(user1);			
				rfdb.updateLast2UsersTag(function_id,tag_id);
			}
	 
			return sync;	 
		}catch(Exception e){
			log.error("Error while committing function into SVN ");
			throw e;
		} 
		
		
	}
	

	/**
	 * 
	 * @param function_name
	 * @param function
	 * @param message
	 * @param rfdb
	 * @param function_id
	 * @return
	 * @throws Exception
	 */
	private boolean synchrnizeSVN11(String function_name, String function, String message, RFunctionDB rfdb,int function_id) throws Exception {
		
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		
		try{
			sdb.connectDB();
			
			String svnuser=Config.getString("svn_user_r");
			String svnpwd=Config.getString("svn_pwd_r");
			
			SchedulerMgmt smgmt=new SchedulerMgmt(getRequest());
			String clientip=smgmt.getPeerIPAddress();
			String user=getAuthenticatedUser();
			
			if(message==null){
				message="created on IP:"+clientip+" By:"+user;
			}
			
 
			 
			Map u=sdb.getSVNUser4WikiUser(user);
			
			if(u!=null && u.get("svn_username")!=null && u.get("svn_password")!=null){
				svnuser=(String)u.get("svn_username");
				svnpwd=(String)u.get("svn_password");
			}
 
			
			
			SVNSync4RFunction sync=new SVNSync4RFunction(svnuser,svnpwd);
			sync.syncFile(function_name, function,message);
			
	 
			return true;
		}catch(Exception e){
			log.error("Error while committing function into SVN ");
			throw e;
		}finally{
			try{sdb.closeDB();}catch(Exception e) {	log.error("error while cloing db:"+e.getMessage());	}
		}
		
		
	}
	
	

	
	/**
	 * moving file to different folder, invoked on drag and drop of r function to folder.
	 * @param function_id
	 * @param new_folder_id
	 * @return
	 * @throws Exception
	 */
	public boolean moveFile2Folder(int function_id, int new_folder_id) throws Exception {
		RFunctionDB rfdb=RFunctionDB.getRFunctionDB();
		
		try{
			String folder=Config.getString("r_function_source_folder");		
			
			rfdb.connectDB();
			
			Map func=rfdb.getRFunction(function_id);
			Number is_class=(Number)func.get("is_class");
			
			String src_folder=null;
			try{
				src_folder=rfdb.getFolderName((Integer)func.get("folder_id"));
			}catch(Exception e){}
			String dest_folder=rfdb.getFolderName(new_folder_id);
			String script_file=(String)func.get("script_file");
			if(src_folder.equals(dest_folder)) {
				throw new Exception("Moving failed! source and destination are the same..");
			}
				 
			String oldfile=folder+((src_folder!=null)?src_folder+File.separator:"")+script_file;
			String newfile=folder+((dest_folder!=null)?dest_folder+File.separator:"")+script_file;
			

			log.debug("old file:"+oldfile+" new :"+newfile);
			
			if(new File(oldfile).renameTo(new File(newfile))){
				log.debug("new folder_id:"+function_id+" function_id:"+new_folder_id);
				rfdb.updateFunctionFolder(function_id,new_folder_id);
			}else{
				throw new Exception("Moving failed failed....");
			}
			
			List functions_newfolder=rfdb.listAllRScriptNames(new_folder_id);
			updateSource4Moved(src_folder,dest_folder,script_file,functions_newfolder,is_class);
			
			return true;
		}catch(Exception e){
			ClientError.reportError(e, "function_id:"+function_id+" new_folder_id:"+new_folder_id);
			throw e;	
		}finally{
			rfdb.closeDB();
		}
		
		
	}
	
	
	
	
	/**
	 * put back the deleted function into normal  
	 * @param function_id
	 * @param new_folder_id
	 * @return
	 * @throws Exception
	 */
	public boolean putbackFunction(int function_id, int new_folder_id) throws Exception {
		RFunctionDB rfdb=RFunctionDB.getRFunctionDB();
		
		try{
			String folder=Config.getString("r_function_source_folder");		
			
			rfdb.connectDB();
			
			Map func=rfdb.getRFunction(function_id);
			String src_folder=null;
			try{
				src_folder=rfdb.getFolderName((Integer)func.get("folder_id"));
			}catch(Exception e){}
			
			String dest_folder=rfdb.getFolderName(new_folder_id);
			String script_file=(String)func.get("script_file");
	 			 
			String oldfile=folder+((src_folder!=null)?src_folder+File.separator:"")+script_file;
			String newfile=folder+((dest_folder!=null)?dest_folder+File.separator:"")+script_file;
			
			if(new File(oldfile).renameTo(new File(newfile))){
				log.debug("new folder_id:"+function_id+" function_id:"+new_folder_id);
				rfdb.updateFunctionFolder(function_id,new_folder_id);
				List fnames=rfdb.listAllRScriptNames(new_folder_id);
				Number is_class=(Number)func.get("is_class");
				try{
					  addLineInSource(dest_folder,script_file,fnames,is_class);
				}catch(Exception e){
					  log.error("ERROR:"+e.getMessage());
					  ClientError.reportError(e, null);
				}
			}else{
				throw new Exception("Putting back the file from trash failed....");
			}
	 		
			return true;
		}catch(Exception e){
			ClientError.reportError(e, "function_id:"+function_id+" new_folder_id:"+new_folder_id);
			throw e;	
		}finally{
			rfdb.closeDB();
		}
		
		
	}
	
	
	/**
	 * renaming the folder, this will invoked on double clikcing the function name and input box appears to over-write the name.
	 * @param folder_id
	 * @param foldername
	 * @return
	 * @throws Exception
	 */
	public boolean renameFolder(int folder_id, String foldername) throws Exception {
		RFunctionDB rfdb=RFunctionDB.getRFunctionDB();
		
		try{
			String folder=Config.getString("r_function_source_folder");		
			
			rfdb.connectDB();
		 
			String old_folder=rfdb.getFolderName(folder_id);
			
			String oldfile=folder+old_folder;
			String newfile=folder+foldername;

			
			
			log.debug("renaming folder from "+oldfile+" to "+newfile);
			
			if(new File(oldfile).renameTo(new File(newfile))){
				rfdb.renameFolder(folder_id, foldername);
				
				//creating source file that contains all functions of that folder so that, this source file will be included in source loading on R 
				String rfolder=Config.getString("r_auto_source_inc_folder");
				if(rfolder!=null ){
					  File oldsource=new File(rfolder+old_folder+RFUNCTION_FILE_EXTENSION);
					  File newsource=new File(rfolder+foldername+RFUNCTION_FILE_EXTENSION);
					  if(oldsource.exists()){
						  oldsource.renameTo(newsource);
					  } 
					  if(!newsource.exists()) newsource.createNewFile();
					  
				}
				
			}else{
				throw new Exception("Renaming folder failed....");
			}
			syncFoldersWith4ESource(rfdb);
			
			return true;
		}catch(Exception e){
			ClientError.reportError(e, "folder_id:"+folder_id+" foldername:"+foldername);
			throw e;	
		}finally{
			rfdb.closeDB();
		}
		
		
	}
	
	
	/**
	 * creating new folder, this is linked with new folder button on group bar
	 * @param folder_name
	 * @param group_id
	 * @return
	 * @throws Exception
	 */
	public Map createFolder(String folder_name, String group_id) throws Exception {
		RFunctionDB rfdb=RFunctionDB.getRFunctionDB();		
		try{
			String folder=Config.getString("r_function_source_folder");	
		 
			String newfile=folder+folder_name;			
			
			if(new File(newfile).isDirectory() || new File(newfile).isFile()){
				throw new Exception("Folder already existing folder failed....Path:"+newfile);				
			}else{
				rfdb.connectDB();
				
				int folder_id=rfdb.getFolderID(folder_name);
				if(folder_id<=0){
					folder_id=rfdb.createFolder(folder_name, group_id);
				}
				File nfile=new File(newfile);
				if(!nfile.isDirectory() && !nfile.isFile()) {
					nfile.mkdirs();
				}
				
				
				//creating source file that contains all functions of that folder so that, this source file will be included in source loading on R 
				String rfolder=Config.getString("r_auto_source_inc_folder");
				if(rfolder!=null ){
					  File sfile=new File(rfolder+folder_name+RFUNCTION_FILE_EXTENSION);
					  if(!sfile.exists()) sfile.createNewFile();
				}	
			
				syncFoldersWith4ESource(rfdb);
				
				HashMap rtn=new HashMap();		
				List folders=rfdb.listOfFolders(group_id);
				rtn.put("folders", folders);
				rtn.put("folder_id", folder_id);
				return rtn;
			}
			
		}catch(Exception e){
			ClientError.reportError(e, "folder_id:"+folder_name+" group_id:"+group_id);
			throw e;	
		}finally{
			rfdb.closeDB();
		}
		
		
	}
	
	/**
	 * deletes empty folder, associated with right click and delete action  
	 * @param folder_name
	 * @param folder_id
	 * @return
	 * @throws Exception
	 */
	public boolean deleteFolder(String folder_name, int folder_id) throws Exception {
		RFunctionDB rfdb=RFunctionDB.getRFunctionDB();		
		try{
			
			String folder=Config.getString("r_function_source_folder");
			String delfolder=folder+folder_name;
			if(new File(delfolder).exists() && new File(delfolder).delete()){
				
				String rfolder=Config.getString("r_auto_source_inc_folder");
				log.debug("folder_name:"+folder_name+" folder_id:"+folder_id);
				rfdb.connectDB();
				rfdb.removeFolder(folder_id);			
	
				if(rfolder!=null ){
					  File sfile=new File(rfolder+folder_name+RFUNCTION_FILE_EXTENSION);
					  sfile.delete();
					  log.debug("Folder:" +sfile.getPath());					
					  syncFoldersWith4ESource(rfdb);

				}	  
			}else{
				throw new Exception("No physical folder "+delfolder+" found! or not able to delete");
			}
			return true;	  
		}catch(Exception e){
			ClientError.reportError(e, "folder_id:"+folder_name+" folder_id:"+folder_id);
			throw e;	
		}finally{
			rfdb.closeDB();
		}
	}
	
	private Map createFolderIfNotExist(String folder_name, String group_id) throws Exception {
		RFunctionDB rfdb=RFunctionDB.getRFunctionDB();
		
		try{
			String folder=Config.getString("r_function_source_folder");		
			
			//String oldfile=folder+old_folder;
			String newfile=folder+folder_name;			
			
		 
				rfdb.connectDB();
				
				int folder_id=rfdb.getFolderID(folder_name);
				if(folder_id<=0){
					folder_id=rfdb.createFolder(folder_name, group_id);
				}
				File nfile=new File(newfile);
				if(!nfile.isDirectory() && !nfile.isFile()) {
					nfile.mkdirs();
				}
				
				
				HashMap rtn=new HashMap();		
				List folders=rfdb.listOfFolders(group_id);
				rtn.put("folders", folders);
				rtn.put("folder_id", folder_id);
				return rtn;
			 
			
		}catch(Exception e){
			ClientError.reportError(e, "folder_id:"+folder_name+" group_id:"+group_id);
			throw e;	
		}finally{
			rfdb.closeDB();
		}
		
		
	}
	
	private String getContent(String fullpath) throws Exception {
		BufferedReader in = new BufferedReader(new FileReader(fullpath));
		String rtn="";
	    String str;
	    while ((str = in.readLine()) != null) {
	    	rtn+=str+"\r\n";
	    }
	    in.close();
		return rtn;
	}
	

	/**
	 * Rename function by double click on the function, textbox appears and over-write name and press save button.
	 * @param new_func_name
	 * @param function_id
	 * @return
	 * @throws Exception
	 */
	public boolean renameFunction(String new_func_name, int function_id) throws Exception {
		String folder=Config.getString("r_function_source_folder");		
		RFunctionDB rfdb=RFunctionDB.getRFunctionDB();
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		
		try{

			Pattern pattern = Pattern.compile( "^[a-z][a-zA-Z0-9.]{1,50}+$" );
			Matcher m = pattern.matcher( new_func_name );  // Matchers are used both for matching and finding.
		    if(!m.matches()){
		    	throw new Exception("Illegal function name, It should start with lower case, It should be Alpha-numeric, Min 2 characters, Max 50 chars, No space and No special characters except dot (.)");
		    }
		    
		
			rfdb.connectDB();
			
		    Map existing=rfdb.getRFunction(new_func_name);
		    if(existing!=null && existing.size()>0){
		        throw new Exception("ERROR: Function name: "+new_func_name+" alerady existing, please try different name");	
		    }
			HashMap rtn=new HashMap();			
			
			sdb.connectDB();
			
			String usr=new SchedulerMgmt(getRequest()).getAuthorizedUser(sdb);
			String lockedby=getLockedBy(function_id, sdb, rfdb);
			if(lockedby!=null && !usr.equalsIgnoreCase(getLockedBy(function_id, sdb, rfdb))){
				throw new Exception("This function is currently locked by user "+lockedby);
			}		  
			Map old_data=rfdb.getRFunction(function_id);			
			String new_filename=new_func_name+RFUNCTION_FILE_EXTENSION;
			
			String old_filename=(String)old_data.get("script_file");
			String old_function_name=(String)old_data.get("function_name");
			Number is_class=(Number)old_data.get("is_class");
			String sub_folder="";
			try{
					sub_folder=rfdb.getFolderName((Integer)old_data.get("folder_id"));
			}catch(Exception e){}
				
			
			String oldfile=folder+((sub_folder!=null)?sub_folder+File.separator:"")+old_filename;
			String newfile=folder+((sub_folder!=null)?sub_folder+File.separator:"")+new_filename;
			
			Map script_data=getDataBundle4Function(old_data,rfdb,sdb);
			String script=(String)script_data.get("content");
			String message="";
 			
			message="Renamed  "+old_function_name+"  --> " +new_func_name;

			log.debug("old file:"+oldfile+" new :"+newfile);
			
			if(new File(oldfile).renameTo(new File(newfile))){
				rfdb.renameFunction(function_id, new_func_name, new_filename);
			}else{
				throw new Exception("Renaming failed");
			}
		

			Map new_data=rfdb.getRFunction(function_id);		
			int folder_id=(Integer)new_data.get("folder_id");
			List function_inFolder=rfdb.listAllRScriptNames(folder_id);
 
	 	    
		    renameSVN(old_function_name,new_func_name,script,message,rfdb,function_id);			
			
			Map data1=rfdb.getRFunction(function_id); //this gets with latest tag after SVN sycnrhonize
		 
	    	Vector v=new Vector();
	    	v.add(data1);	    		
 
	    	rtn.put("rfunctions", v);
 
			removeLineInSource(sub_folder,old_filename); 
			addLineInSource(sub_folder,new_filename,function_inFolder,is_class);
			
			return true;
			
			
		}catch(Exception e){			 
			ClientError.reportError(e,null);			
			throw e;	
		}finally{
			sdb.closeDB();
			rfdb.closeDB();
		}
	}
	
	/**
	 * Purse deleted function so that it completely removed from the disk
	 * @param function_id
	 * @return
	 * @throws Exception
	 */
	public boolean purgeFunction(int function_id) throws Exception {
		String folder=Config.getString("r_function_source_folder");		
		RFunctionDB rfdb=RFunctionDB.getRFunctionDB();
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		
		try{
 
			rfdb.connectDB();
			sdb.connectDB();
			 
		  
			Map old_data=rfdb.getRFunction(function_id);			
			 
			
			String old_filename=(String)old_data.get("script_file");
			String old_function_name=(String)old_data.get("function_name");
			
			String sub_folder="";
			try{
					sub_folder=rfdb.getFolderName((Integer)old_data.get("folder_id"));
			}catch(Exception e){}
				
			//deleting physical file		
			String oldfile=folder+((sub_folder!=null)?sub_folder+File.separator:"")+old_filename;
			 
			File file=new File(oldfile);
			if(file.exists()){
				file.delete();
			}
 
			String svnuser=Config.getString("svn_user_r");
			String svnpwd=Config.getString("svn_pwd_r");
			

			Map new_data=rfdb.getRFunction(function_id);		
			int folder_id=(Integer)new_data.get("folder_id");
			List function_inFolder=rfdb.listAllRScriptNames(folder_id);
			
			
			String user=getAuthenticatedUser();		 
			Map u=sdb.getSVNUser4WikiUser(user);
			
			if(u!=null && u.get("svn_username")!=null && u.get("svn_password")!=null){
				svnuser=(String)u.get("svn_username");
				svnpwd=(String)u.get("svn_password");
			}
			
			SVNSync4RFunction sync=new SVNSync4RFunction(svnuser,svnpwd);
			sync.deleteFile(old_function_name);

					
			rfdb.connectDB();
			rfdb.deleteFunction(function_id);
			
			//removing from source			
			removeLineInSource(sub_folder,old_filename); 
			 
			
			return true;
			
			
		}catch(Exception e){
			//e.printStackTrace();
			ClientError.reportError(e,null);			
			throw e;	
		}finally{
			sdb.closeDB();
			rfdb.closeDB();
		}
	}

	private void updateSource4Moved(String old_subfolder, String new_subfolder, String fname, List functions_newfolder,Number is_class) throws Exception {
		
		 String rfolder=Config.getString("r_auto_source_inc_folder");
		 String filepref=Config.getString("r_auto_source_inc_prefix");
		 if(rfolder!=null && filepref!=null){			  
			 try{
				  removeLineInSource(old_subfolder,fname);
				  addLineInSource(new_subfolder,fname,functions_newfolder,is_class);
			  }catch(Exception e){
				  log.error("ERROR:"+e.getMessage());
				  ClientError.reportError(e, null);
			  }
			  //adding into new source folder.
		 }
		
	}
	
	private void removeLineInSource(String subfolder,String fname) throws Exception{
		
		 String rfolder=Config.getString("r_auto_source_inc_folder");
		 String filepref=Config.getString("r_auto_source_inc_prefix");
		 
		 File old_s=new File(rfolder+subfolder+RFUNCTION_FILE_EXTENSION);
		  if(old_s.exists() && old_s.isFile()){
			  ArrayList lst=new ArrayList();
			  List lines=FileUtils.readLines(old_s);
			  String tline=filepref+subfolder+"/"+fname;
			  for(Iterator i=lines.iterator();i.hasNext();){
				  String line=(String)i.next();
				  String data="source(universalPath(\""+tline+"\")";				  
				  if(line.toUpperCase().indexOf(data.toUpperCase())>=0){  }
				  else if(!line.trim().equals("")){
					  lst.add(line);
				  }
			  }
			  FileUtils.writeLines(old_s, lst);
		  }
	}
	
	
	public List getUnPackagedSources(HashMap<String,Integer> subfolders) throws Exception{
		 String rfolder=Config.getString("r_auto_source_inc_folder");
		 String filepref=Config.getString("r_auto_source_inc_prefix");
		 
		 ArrayList list=new ArrayList();
		 for(String subfolder: subfolders.keySet()){
			 File old_s=new File(rfolder+subfolder+RFUNCTION_FILE_EXTENSION);
			 int count=0;
			 if(old_s.exists() && old_s.isFile()){
				  ArrayList lst=new ArrayList();
				  List lines=FileUtils.readLines(old_s);
				  //String tline=filepref+subfolder+"/"+fname;
				  for(Iterator i=lines.iterator();i.hasNext();){
					  String line=(String)i.next();
					  String pref= filepref+subfolder+"/";
					  if(line.contains(pref ) && line.indexOf(RFUNCTION_FILE_EXTENSION)>0 ){
						  //System.out.println("RFunctionMgmt.getUnpackagedSources() Indexof:prf:"+line+" idx:"+ line.indexOf(pref));					  
						  String rFile=line.substring(line.indexOf(pref)+pref.length(),line.indexOf(RFUNCTION_FILE_EXTENSION));
						  list.add(rFile);
						  count++;
					  }
				  }
			 }
			 subfolders.put(subfolder,new Integer(count));
		 }
		 return list;
	}
	

	 
	
	
	
	
	
	 
	

	
	private void addLineInSource( String subfolder, String fname, List functionnames_order,Number is_class) throws Exception {
		
		  String rfolder=Config.getString("r_auto_source_inc_folder");
		  String filepref=Config.getString("r_auto_source_inc_prefix");
		  
		  int pass=0;
		  if(rfolder!=null && filepref!=null){
			  File sfile=new File(rfolder+subfolder+RFUNCTION_FILE_EXTENSION);
			  //boolean lastempty=true;
			  boolean src_included=false;
			  try{
				   
				  List lines=null;
				  
				 // log.debug("lines:"+lines);
				  if(sfile.exists() && sfile.isFile()) {
					  lines=FileUtils.readLines(sfile);
				  }
						  
				  pass=1;	  
				  if(lines!=null && fname!=null){
					  
					  	  String tline=filepref+subfolder+"/"+fname;    
						  String data="source(universalPath(\""+tline+"\")";	
						  //if(lines.contains(data)){
						  if(checkExist(lines,data)!=null){
							  src_included=true;
						  }
						  pass=2;	 
					}
				  pass=3;
				  if(subfolder.equalsIgnoreCase("Trash")) src_included=true; //omits 
				  if(!src_included){
				 	  String tline1=filepref+subfolder+"/"+fname;
					  //String tline2= "source(\""+tline1+"\")";	
				 	  
				 	  String tline2=(is_class!=null && is_class.intValue()>=1?"#":"")+"source(universalPath(\""+tline1+"\")";
				 	 
					  lines.add(tline2);
					  
				  }
				  pass=6;
				  //ignores the trash folder...
				  if(!subfolder.equalsIgnoreCase("Trash")){
					  BufferedWriter out = new BufferedWriter(new FileWriter(sfile,false));
					  out.write("#updated this file on "+new Date().toString()+"\n");
					  for(Iterator i=functionnames_order.iterator();i.hasNext();){
						  String cf=(String)i.next();
						  String tline1=filepref+subfolder+"/"+cf;
	
						  String tline2= "tryCatch({source(universalPath(\""+tline1+"\"))}, error = function(e) Say(\"Check source file "+cf+"\"))";
						  String tline3= "source(universalPath(\""+tline1+"\")";
						  //if(lines.contains(tline3)){
						  String exist=checkExist(lines,tline3);
						  if(exist!=null){
							  if(exist.trim().startsWith("#"))  out.append("#"+tline2+"\n");
							  else  out.append(tline2+"\n"); 
						  }
						  pass=7;
					  }
					  out.flush();
					  out.close();
					  sfile.setWritable(true);
					  pass=8;
				  }
				  if(getLockCache().get(cache_packaged_key)!=null){	
					  getLockCache().remove(cache_packaged_key);
				  }
				  
			  }catch(Exception e){
				  log.error("ERROR:"+e.getMessage());
				  ClientError.reportError(e, "subfolder:"+subfolder+" fname:"+fname+" pass:"+pass+" is_class:"+is_class);
			  }
		  }
	}
	
	private String checkExist(List lines,String source) throws Exception {
		String found=null;
		for(Iterator<String> i=lines.iterator();i.hasNext();){
			String line=i.next();
			if(line.contains(source)) found=line;
		}
		return found;
	}
	
	private void syncFoldersWith4ESource(RFunctionDB rfdb) throws Exception {
		  String rfolder=Config.getString("r_auto_source_inc_folder");
		  String filepref=Config.getString("r_auto_source_inc_prefix");	
		  
		  
		  String fe_source_filename=Config.getString("r_source_4e_function_file");	
		  
		  String inc_prefix=Config.getString("r_source_4e_function_file_path_perfix");	
		  
		  File sfile=null; 
		  //log.debug("4e source file:"+fe_source_filename+" file exist:"+new File(fe_source_filename).isFile());
		  
		  if(fe_source_filename!=null && new File(fe_source_filename).isFile()){
			   //List lines=FileUtils.readLines(new File(fe_source_filename));	
			 
			  
			   ArrayList lines=new ArrayList();
			   List folders=rfdb.listOfFolders();
			   for(Iterator i=folders.iterator();i.hasNext();){
				  Map folder=(Map)i.next();
				  String  subfolder=(String)folder.get("folder_name"); 
				  File sfile1=new File(rfolder+subfolder+RFUNCTION_FILE_EXTENSION);
				  if(sfile1.isFile()){
					  String fileline=inc_prefix+subfolder+RFUNCTION_FILE_EXTENSION;		
					  lines.add(fileline);
					  log.debug("file_line:"+fileline);
				  }else{
					  log.debug("file note found:"+sfile1.getPath());
				  }
			  }
			  sfile=new File(fe_source_filename);
			  if(lines.size()>0){
				  BufferedWriter out = new BufferedWriter(new FileWriter(sfile,false));
				  out.write("#updated this file on "+new Date().toString()+"\n");
				  for(Iterator i=lines.iterator();i.hasNext();){
					  String cf=(String)i.next();
					  String r_file="";
					  try{
						  StringTokenizer st=new StringTokenizer(cf,"\\");
						  if(st.countTokens()>1){
							  while(st.hasMoreTokens()) r_file=st.nextToken();
						  }
						  if(!r_file.toLowerCase().endsWith(".r")){
							  r_file=cf;
						  }
					  }catch(Exception e){
						  
					  }
					  out.append("tryCatch({source(universalPath(\""+cf+"\"))}, error = function(e) Say(\"Check source file for "+r_file+"\"))"+"\n"); 
				  }
				  out.flush();
				  out.close();
				  sfile.setWritable(true);
				  
				  
			  }
		  }else{
			  log.error("currently main source file configured to:"+fe_source_filename);
			  log.error("main source file that contains folder named source R not found or invlid, check :r_source_4e_function_file key on config file");
			  
		  }
		  
	}
	
	
	
	
	private Map getSourcedFunctions() throws Exception {
		  String rfolder=Config.getString("r_auto_source_inc_folder");
		  String filepref=Config.getString("r_auto_source_inc_prefix");
		  RFunctionDB rfdb=RFunctionDB.getRFunctionDB();
		  String cache_packaged_key="packaged_functions";
		  if(getLockCache().get(cache_packaged_key)!=null){			 
			  Map rtn=(Map)getLockCache().get(cache_packaged_key);			  
		      return rtn;
		  }else{
		  
			  HashMap rtn=new HashMap();
			  try{
				  rfdb.connectDB();
				  List folders=rfdb.listOfFolders();
				  for(Iterator i=folders.iterator();i.hasNext();){
					  Map folder=(Map)i.next();
					  
					  Integer f_id=(Integer)folder.get("id");
					  String  subfolder=(String)folder.get("folder_name"); 
					  File sfile=new File(rfolder+subfolder+RFUNCTION_FILE_EXTENSION);
					  if(sfile.exists() && sfile.isFile()){
						  List lines=FileUtils.readLines(sfile);			  
						  List<Map> functions=rfdb.listOfFunctions(f_id);
						  for(Iterator<Map> it=functions.iterator();it.hasNext();){
							  Map data=it.next();
							  
							  String tline1=filepref+subfolder+"/"+data.get("script_file");
							  String tline2= "source(universalPath(\""+tline1+"\")";					
							  if(lines.contains(tline2)){
								  rtn.put(data.get("id"),true);
							  }
							  
						  }
						  
					  }
				  }
				  IElementAttributes att= getLockCache().getDefaultElementAttributes();
				  att.setMaxLifeSeconds(6000); //every 60 minutes
				  getLockCache().put(cache_packaged_key,rtn,att);
				  return rtn;
			  	
			  }catch(Exception e){
				  ClientError.reportError(e, null);
				  throw e;
			  }finally{
				  rfdb.closeDB();
			  }
		  }//end if
		  
	}
	
	private void addLineInSource_old( String subfolder, String fname) throws Exception {
		  String rfolder=Config.getString("r_auto_source_inc_folder");
		  String filepref=Config.getString("r_auto_source_inc_prefix");
		  
		  if(rfolder!=null && filepref!=null){
			  File sfile=new File(rfolder+subfolder+RFUNCTION_FILE_EXTENSION);
			  boolean lastempty=true;
			  boolean src_included=false;
			  try{
				  String tline=filepref+subfolder+"/"+fname;
				  if(sfile.exists() && sfile.isFile()){
					  List lines=FileUtils.readLines(sfile);					  
					  for(Iterator i=lines.iterator();i.hasNext();){
						  String line=(String)i.next();
						  String data="source(universalPath(\""+tline+"\")";	
						  if(line.toUpperCase().indexOf(data.toUpperCase())>=0){
							  src_included=true;
						  }
						  lastempty=line.equals("")?true:false;
					  }
				  }
				  if(subfolder.equalsIgnoreCase("Trash")) src_included=true; //omits 
				  if(!src_included){
					  String data=(lastempty?"":System.getProperty("line.separator"))+"source(\""+tline+"\")";
					  //FileUtils.writeStringToFile(sfile,data );					  
					  BufferedWriter out = new BufferedWriter(new FileWriter(sfile,true));
					  out.append(data);
					  out.flush();
					  out.close();
					  sfile.setWritable(true);
					  
				  }
				  
			  }catch(Exception e){
				  log.error("ERROR:"+e.getMessage());
				  ClientError.reportError(e, "subfolder:"+subfolder+" fname:"+fname);
			  }
			  
		  }
		  
	}
	
	private void createContent(String fullpath, String script, String subfolder, String fname, List function_names,Number is_class) throws Exception {
 
		  Writer output = null;		 
		  File file = new File(fullpath);
		  output = new BufferedWriter(new FileWriter(file));
		  output.write(script);
		  output.close();
		  addLineInSource( subfolder,  fname,function_names,is_class);
	}
	
	
	/**
	 * returns svn logs for the given R function name
	 * @param function_name
	 * @return
	 * @throws Exception
	 */
	
	public Vector getSVNLogs(String function_name) throws Exception {
		SVNSync4RFunction sync=new SVNSync4RFunction();
		TreeMap rtn=new TreeMap(); 
		Vector d=sync.log(function_name);
        if(d!=null && d.size()>0){
        	
		    for(Iterator<SVNLogEntry> i=d.iterator();i.hasNext();){
		    	SVNLogEntry entry=i.next();		    	
		    	SimpleDateFormat format=new SimpleDateFormat("dd-MMM-yyyy hh:mm a");
		    	ValueObject vo=new ValueObject();
		    	HashMap data=new HashMap();
		    	data.put("author", entry.getAuthor());
		    	data.put("date", format.format(entry.getDate()));
		    	data.put("message", entry.getMessage());
		    	data.put("revision", entry.getRevision());
		    	data.put("function_name", function_name);
		    	
		    	String path=null;
		    	if(entry.getChangedPaths()!=null){
		    		for(Iterator it=entry.getChangedPaths().keySet().iterator();it.hasNext();){
		    		//if(entry.getChangedPaths().keySet().size()>0){
		    			String thispath=(String)it.next();
		    			thispath=thispath.substring(thispath.lastIndexOf("/")+1);    			
		    			path=(path==null)?thispath:path+","+thispath;
		    		}
		    		if(path!=null){
		    		
				    	data.put("path", path);
		    		}
		    	}
		    	rtn.put(entry.getRevision(), data);
		    	//System.out.println(" Rev:"+entry.getRevision()+"  Date:"+format.format(entry.getDate())+"  User:"+entry.getAuthor()+" Msg:"+entry.getMessage());
		    }
		    return new Vector(rtn.descendingMap().values());
        }else{
        	return null;
        }
		
	}
 
	
	/**
	 * get script Revition.
	 * @param function_name
	 * @param revision
	 * @param flag
	 * @param path
	 * @return
	 * @throws Exception
	 */
	public String getScriptRev(String function_name, String revision,boolean flag,String path) throws Exception {
		try{
			SVNSync4RFunction sync=new SVNSync4RFunction();
			String script=null;
			if(flag)
				script=sync.getScript(function_name, Long.parseLong(revision),path);
			else
				//script=sync.diffWC(function_name, Long.parseLong(revision));
				script=sync.getWhatChanged(function_name, Long.parseLong(revision));
				if(script==null){
					script="No changes were made or first revision of this function";
				}
			
			return script;
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
		
	}
	
	/**
	 * get wiki for the given function name
	 * @param function_name
	 * @return
	 * @throws Exception
	 */
	public String getFunctionWiki(String function_name) throws Exception {
		WikiRFunctionManual wiki=new WikiRFunctionManual();		
		//String rtn=wiki.getWikiHTML(function_name);
		
		String username=Config.getString("wiki.username");
		String password=Config.getString("wiki.password");
		String wikiurl=Config.getString("wiki.wikiurl");
		
		String rtn=wiki.getWikiHTML(username, password, wikiurl, function_name);		
		return rtn;
		
	}
	
	/**
	 * update tags of the functions, usually invoked while drag and drop functions from available box to selected theme box.
	 * @param function_id
	 * @param tag_id
	 * @param isadd
	 * @return
	 * @throws Exception
	 */
	public Map updateTags4Function(int function_id, int tag_id,boolean isadd) throws Exception {
		RFunctionDB rfdb=RFunctionDB.getRFunctionDB();
	    
		try{

			HashMap rtn=new HashMap();				
			rfdb.connectDB();
		    if(isadd){
		    	rfdb.addTagIds4Function(function_id, tag_id);			    	
		    }else {
		    	rfdb.removeTagIds4Function(function_id, tag_id);
		    }
		    	
		    Map rtndata=rfdb.getRFunction(function_id);
    		Vector v=new Vector();
    		v.add(rtndata);	    		
    		
    		 
    		rtn.put("rfunctions", v);
			return rtn;
			

		}catch(Exception e){
			ClientError.reportError(e, null);
			throw e;
		}finally{
			rfdb.closeDB();
		}
	   
   }
	
	
	
	/**
	 * this function needed at the begining to migration to new features.
	 * @deprecated
	 * @param foldernames
	 * @return
	 * @throws Exception
	 * 
	 * 
	 */
	
   public boolean migrateFiles (String foldernames) throws Exception {
	   try{
		   
		   Vector impfolder=new Vector();
		   if(foldernames!=null && !foldernames.equals("")){
			   StringTokenizer st=new StringTokenizer(foldernames,",");
			   while(st.hasMoreTokens()){
				   impfolder.add(st.nextToken());
			   }
		   }
		   
		   String rfolder=Config.getString("r_function_source_folder");		
		   File[] folders=new File(rfolder).listFiles();	
		   int count=0;
		   for(int i=0;i<folders.length;i++){			   
			   if(folders[i].isDirectory() && (impfolder.size()==0 || impfolder.contains(folders[i].getName())) ){
				   File[] files=folders[i].listFiles();
				   
				   //create folder;
				   Map fld=createFolderIfNotExist(folders[i].getName(),"imported");
				   int folder_id=(Integer)fld.get("folder_id");
				   
				   for(int ia=0;ia<files.length;ia++){
					   if(files[ia].isFile()){
						   
						    
						   String content=getContent(files[ia].getPath());
						   
						   String shortfilename=files[ia].getName().lastIndexOf(".")>=0? files[ia].getName().substring(0,files[ia].getName().lastIndexOf(".")) :files[ia].getName();
						   String file_ext=files[ia].getName().lastIndexOf(".")>=0? files[ia].getName().substring(files[ia].getName().lastIndexOf(".")) :null;
						   if(folder_id>0){
							   
							   try{
								   createRFunction11(folder_id+"", shortfilename, content,file_ext);
								   //System.out.println("~~Importing:"+shortfilename);
							   }catch(Exception e){
								  log.error(e);
							   }
						   }
						   content=null;
						   //System.out.println("::::file:::::"+shortfilename);
						   //count++;
					   }
				   }		   
			   }
		   }
		   return true;
	   }catch(Exception e){
		   e.printStackTrace();
		   throw e;
	   }
   }
   
   
   /**
    * Search word within all function, and display finder UI with result and occurances 
    * @param word
    * @return
    * @throws Exception
    */
   public Map getSearchR(String word) throws Exception  {
	   try{
	  
		   Map r_result = FindStringInFiles.search(word, Config.getString("svn_local_path_r"), RFunctionMgmt.RFUNCTION_FILE_EXTENSION);
		   
		   String svnLocalPath = Config.getString("svn_local_path");
		   
		   Map scd_result = FindStringInFiles.search(word, svnLocalPath, ".R");
		   //Map scd_result=new FindStringInFiles(Config.getString("svn_local_path")).search(word,".R");
		   
		   HashMap h=new HashMap();
		   h.put("r", r_result);
		   h.put("scd", scd_result);
		   return h;
		   
	   }catch(Exception e){
		   e.printStackTrace();
		   throw e;
	   }
   }

   
   /**
    * this is equivalent to find4E R function 
    * @param filename
    * @return
    * @throws Exception
    */
   public Map getSearchScript(String filename) throws Exception  {
	   
	    
	   try{
	 
		   HashMap rtn=new HashMap();
		    
		   
		   String folder=null;
		   if(filename.contains("script_")){
			   
			   folder = Config.getString("svn_local_path");
					   
			   Pattern p=Pattern.compile("^(script_)([0-9]+)(.*)$", Pattern.DOTALL);
			   Matcher m=p.matcher(filename);
			   if(m.find() && m.groupCount()>2){
				   SchedulerDB sdb=SchedulerDB.getSchedulerDB();
				   sdb.connectDB();
				   try{
					   int scd_id=Integer.parseInt(m.group(2));
					   Map d=sdb.getScheduler(scd_id);
					   rtn.put("item_id", scd_id);
					   rtn.put("item_type", "scheduler");
					   if(d.get("deleted")!=null && ((Number)d.get("deleted")).intValue()==1){
						   rtn.put("deleted", true);
					   }
					   
				   }catch(Exception e){
					   log.error("Error while getSearchScript() scd, e:"+e.getMessage());
				   }finally{
					   sdb.closeDB();
				   }
			   }
		   }else{
			   folder=Config.getString("svn_local_path_r");
			   
			   RFunctionDB rfdb=RFunctionDB.getRFunctionDB();
			   
			   try{
				   
				   
				   rfdb.connectDB();				   
				   Map func=rfdb.getRFunctionForScriptFile(filename);
				   rtn.put("item_type", "function");
				   if(func!=null && func.keySet().size()>0){
					   
					   rtn.put("item_id", func.get("function_name"));
					   if(func.get("folder_name")!=null && func.get("folder_name").toString().equalsIgnoreCase("trash")){
						   rtn.put("deleted", true);
					   }
				   }
				    
			   }catch(Exception e){
				   log.error("Error while getSearchScript() scd, e:"+e.getMessage());
			   }finally{
				   rfdb.closeDB();
			   }
			   
		   }
		  
		   
		   String ffile=folder.endsWith(File.separator)? folder : folder+File.separator;
		   ffile+=filename;
		   StringWriter stringWriter = new StringWriter();
		   IOUtils.copy(new FileInputStream(new File(ffile)), stringWriter);
		   rtn.put("script",stringWriter.toString());
		   
		   return rtn;
		   
	   }catch(Exception e){
		   e.printStackTrace();
		   throw e;
	   } 
   }
   
   
   /**
    * get package info 
    * this will be called on clicking of folder's package info button
    * @param pack
    * @return
    * @throws Exception
    */
   public Map getPackageInfo(String pack) throws Exception {
	   
	   
	   HashMap h=new HashMap();
	   RFunctionDB rfdb=RFunctionDB.getRFunctionDB();
	   SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		try{
			

			String unixp=Config.getString("package.repository.unix");
			String winp=Config.getString("package.repository.win");
			
			Map ud=new HashMap(),wd=new HashMap();
			try{
				ud=getPackageDescInfo(unixp,pack);
			}catch(Exception e){log.error("Error collecint unix package info e:"+e.getMessage());}
			try{
				wd=getPackageDescInfo(winp,pack);
			}catch(Exception e){log.error("Error collecint windows package info e:"+e.getMessage());}
			
			String sourceloader="";
			try{
				String rfolder=Config.getString("r_auto_source_inc_folder");
				File sf=new File(rfolder+pack+RFUNCTION_FILE_EXTENSION);
				sourceloader=(sf.exists())?FileUtils.readFileToString(sf):"";
			}catch(Exception e) {
				log.error("error while loading source file e:"+e.getMessage());
			}
			 
			rfdb.connectDB();
			sdb.connectDB();
			
		 
			
			Map pinfo=rfdb.getPackageInfo(pack);
			
			int pid=rfdb.getFolderID( pack);
					
					
			h.put("package_id",pid );								
			h.put("folders", rfdb.listOfFolders());		
			h.put("required_packages", rfdb.listRelatedFolderIds(pid));
			h.put("pinfo", pinfo);
			h.put("unix",ud);
			h.put("win", wd);
			h.put("sourceloader", sourceloader);
			h.put("access", getPackageAccessPrivilege(pid,rfdb));
			
			
			return h;

		}catch(Exception e){
			ClientError.reportError(e, null);
			throw e;
		}finally{
			rfdb.closeDB();
			sdb.closeDB();
		}   
   }
   

   
   /**
    * reset package hirarchial boxes, this info is used to build packages  
    * @param packname
    * @return
    * @throws Exception
    */
   public List resetPackageHirDep(String packname) throws Exception {
	   
	    RFunctionDB rfdb=RFunctionDB.getRFunctionDB();	    
		try{			
			rfdb.connectDB();	
			List<Integer> h=rfdb.getDefaultHierarchyDependsIds(packname);
			int pid=rfdb.getFolderID(packname);
			rfdb.updateRelatedFolderIds(pid, h);
			
			return h;
			
		}catch(Exception e){
			ClientError.reportError(e, null);
			throw e;
		}finally{
			rfdb.closeDB();
		}   
	   
   }
   
	
   /**
    * syncPackageHirDep 
    * 
    * 
    * @deprecated
    * @param folder_id
    * @param ids
    * @return
    * @throws Exception
    */
   public boolean syncPackageHirDep(int folder_id, List<Integer> ids) throws Exception {
	   
	    
	   
	   RFunctionDB rfdb=RFunctionDB.getRFunctionDB();
	    
		try{			
			rfdb.connectDB();	
			
			rfdb.updateRelatedFolderIds(folder_id, ids);
			 
			return true;
			
		}catch(Exception e){
			ClientError.reportError(e, null);
			throw e;
		}finally{
			rfdb.closeDB();
		}   
	   
   }
   
   /**
    * get package member info 
    * @param pack_id
    * @return
    * @throws Exception
    */
   public Map getPackageMembersInfo(int pack_id) throws Exception {
	    RFunctionDB rfdb=RFunctionDB.getRFunctionDB();
		try {
			HashMap rtn=new HashMap();
			rfdb.connectDB();
			Vector tags = rfdb.getTags();
			rtn.put("tags", tags);
			rtn.put("theme_tags", rfdb.getTagIds4Folder(pack_id, "folder_tags"));
			rtn.put("notification_tags", rfdb.getTagIds4Folder(pack_id, "folder_followtags"));
			rtn.put("access", getPackageAccessPrivilege(pack_id,rfdb));
			
			return rtn;
		} catch (Exception e) {
			
			ClientError.reportError(e, null);
			throw e;
		} finally {
			rfdb.closeDB();
		}
	
	   
   }
   
	
   /**
    * update package themes 
    * @param pack_id
    * @param tagids
    * @param tblname
    * @return
    * @throws Exception
    */
   public boolean updatePackageThemes(int pack_id, List tagids, String tblname) throws Exception {
	    RFunctionDB rfdb=RFunctionDB.getRFunctionDB();
		try {
			rfdb.connectDB();			
			rfdb.updateTagIds4Folder(pack_id, tagids, SchedulerDB.REMOVE_BEFORE_UPDATE, tblname);
			return true;
		} catch (Exception e) {
			
			ClientError.reportError(e, null);
			throw e;
		} finally {
			rfdb.closeDB();
		}
	
	   
  }
   
   
   
   private static String PACKAGE_ACTION_OVERWRITEOWNER="overwrite_owner";
   private static String PACKAGE_ACTION_ADDTAG="add_tag";
   private static String PACKAGE_ACTION_REMOVETAG="remove_tag";
   
   
   private static String PACKAGE_ACTION_ADDNOTIFICATION="add_noti";
   private static String PACKAGE_ACTION_REMOVENOTIFICATION="remove_noti";
   
   
   
   /**
    * package memeber action 
    * @param folder_id
    * @param tag_id
    * @param function_ids
    * @param action
    * @return
    * @throws Exception
    */
   public boolean packageMemberAction(int folder_id, int tag_id, int function_ids[], String action) throws Exception {
	   
	    
	   	RFunctionDB rfdb=RFunctionDB.getRFunctionDB();
 		try {
 			boolean rtn=false;
 			boolean make_owner_also=true;
 			boolean add_also=true;
 			
 			rfdb.connectDB();
 			
 			log.debug("packageMemberAction() folder_id:"+folder_id+" tag_id:"+tag_id+" action:"+action);
 			
 			if(action.equals(PACKAGE_ACTION_OVERWRITEOWNER)) { 				
 				rfdb.addTag4Package(tag_id, function_ids,make_owner_also);
 				rtn=true;
 			}
 			if(action.equals(PACKAGE_ACTION_ADDTAG)) {	 				
 				rfdb.addTag4Package(tag_id,function_ids, !make_owner_also);
 				rtn=true;
 			}
 			if(action.equals(PACKAGE_ACTION_REMOVETAG)) {	 				
 				rfdb.removeTag4Package(tag_id,function_ids);
 				rtn=true;
 			}
 			
 			if(action.equals(PACKAGE_ACTION_ADDNOTIFICATION)) {
 				
 				rfdb.addNotificationTag4Package(tag_id, function_ids, add_also);
 				rtn=true;
 			}
 			if(action.equals(PACKAGE_ACTION_REMOVENOTIFICATION)) {
 			 
 				rfdb.addNotificationTag4Package(tag_id, function_ids, !add_also);
 				rtn=true;
 			}
 			
 			
 			return rtn;
 		} catch (Exception e) {
 			
 			ClientError.reportError(e, null);
 			throw e;
 		} finally {
 			rfdb.closeDB();
 		}
   }
	   

   /** 
    * package desc info
    * 
    * @param path
    * @param packagename
    * @return
    * @throws Exception
    */
   
   private Map getPackageDescInfo(String path, final String packagename) throws Exception {
		
		try {
			 
			File[] files = new File(path).listFiles();
	
			//sort file names
			Arrays.sort(files, new Comparator<File>(){
			    public int compare(File f1, File f2)
			    {
			        return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
			    } });
			
			
			String rtn=null;
			
			//filter files within the package
			FilenameFilter filter = new FilenameFilter() {
		        public boolean accept(File directory, String fileName) {
		            return fileName.startsWith(packagename);
		        }
		    };
			File[] pfile=files[0].listFiles(filter);  
			String ex="";
			if(pfile.length>0){
			 
				
				 FileInputStream fin = new FileInputStream(pfile[0]);
				
				 if(pfile[0].getPath().endsWith(".tar.gz")){
					 
					 ex="_nix";
					 TarArchiveInputStream tai=new TarArchiveInputStream( new GZIPInputStream(fin));					 
					 TarArchiveEntry ze1 = null; 
				     while ((ze1 = tai.getNextTarEntry()) != null) {
				    		if(ze1.getName().equals(packagename+"/DESCRIPTION")){
				    			 byte[] bytes= new byte[(int)ze1.getSize()];
				    			 tai.read(bytes, 0, bytes.length);
				    			 rtn= new String( bytes );
				    			// System.out.println("strUnzipped:"+rtn);
				    			 
				    		}
				     }
				     tai.close();
				     fin.close();
				 }else{
					 ex="_win";
					 ZipInputStream zin;
					 zin = new ZipInputStream(fin);
					 ZipEntry ze = null; 
				     while ((ze = zin.getNextEntry()) != null) { 
				    	 
				    	if(ze.getName().equals(packagename+"/DESCRIPTION")){				    		

				    		 byte[] bytes= new byte[(int)ze.getSize()];
				             zin.read(bytes, 0, bytes.length);
				             rtn= new String( bytes);
				          
				    	}
				     }
				     
				     fin.close();
				     zin.close();
				     
				 }
				 
			     
			}
			LinkedHashMap h=null;
			 
			File file = new File("c:\\rnd\\temp\\rtn"+ex+".txt");
			FileUtils.writeStringToFile(file, rtn);
			
			if(rtn!=null){
				h=new LinkedHashMap();
				 
				Pattern p1 = Pattern.compile("([A-Za-z]+)(:)((\\W)*(\\S)*(.)*)");
				 
				
				
	            final Matcher matcher = p1.matcher(rtn);
	            
	            while(matcher.find()){
	            	if(matcher.groupCount()>=3){
	            		final String key = matcher.group(1);
	            		final String value = matcher.group(3);
	            		 
	            		h.put(key,value);
	            	}
	              
	            }
			}
			
			
			return h;
	 
		} catch (Exception e) {	
			throw e;
		} 
	}

   	private String getPackageAccessPrivilege(int folder_id, RFunctionDB rfdb) throws Exception {
		String rtn="";
		UserThemeAccessPermission user=getAuthenticatedUserObj(rfdb);
		if(user!=null){
			List<String> themes=rfdb.getThemeTags4Folder(folder_id);			
			for(String ttag:themes){            	 
           	 if(user.getRwx().contains(ttag)) rtn=ACCESS_PRIVILEGE_RWX;            
            }
            if(themes.size()==0) rtn=ACCESS_PRIVILEGE_RWX; 

		}
		log.debug("user:"+user);
 		 
		String superuser=(String)getRequest().getSession().getAttribute(Constant.SESSION_LOGGED_SUPERUSER);
		if(superuser!=null && !superuser.equals("")){
			rtn=ACCESS_PRIVILEGE_RWX;
		}
		if(rtn.equals("")) rtn=null;
		return rtn;
	}
   

   	/**
	 * Get folder tagname by R function id
	 * @param r_function_id r function id
	 * @return folder name
	 * @throws Exception
	 */
   	public String getFolderThemeByRFunctionId(int r_function_id) throws Exception {
   		String result = "";
	   	RFunctionDB rfdb=RFunctionDB.getRFunctionDB();
 		try {
 			rfdb.connectDB();
 			result = rfdb.getFolderThemeByRFunctionId(r_function_id);
		} catch (Exception e) {	
			throw e;
		}  		
   		return result;
	}	
	
	

	
}
