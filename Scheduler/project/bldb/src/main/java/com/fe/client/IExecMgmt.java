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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.jcs.JCS;
import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tmatesoft.svn.core.SVNLogEntry;

import com.fe.svn.SVNIExecXML;
import com.fe.util.StringPlaceHolder4ChildStrategy;
import com.fe.util.WikiRFunctionManual;
import com.fourelementscapital.db.IExecDB;
import com.fourelementscapital.db.RFunctionDB;
import com.fourelementscapital.db.SchedulerDB;
import com.fourelementscapital.db.vo.ValueObject;
import com.fourelementscapital.fileutils.StringPlaceHolder;
import com.fourelementscapital.scheduler.config.Config;
import com.fourelementscapital.scheduler.error.ClientError;


/**
 * This class exposes the for iExec interface. 
  * @author Administrator
 *
 */
public class IExecMgmt extends Authenticated{
	

	private HttpServletRequest request=null;
	private Logger log = LogManager.getLogger(IExecMgmt.class.getName());	

	
	/**
	 * not accessible to DWR, for invocation.
	 * @param request
	 * @param ignoreSecurity
	 * @throws Exception
	 */
	protected IExecMgmt(HttpServletRequest request,boolean ignoreSecurity) throws Exception {
		super(ignoreSecurity);
		setRequest(request);
	}
	
	
	public IExecMgmt() throws Exception {
		super();

	}
	
	
	/**
	 * not accessible for DWR, for internal invocation 
	 * @param request
	 * @throws Exception
	 */
	public IExecMgmt(HttpServletRequest request) throws Exception {
		super(request);
	
	}

	/**
	 * Returns data for generating tree menu on iexec
	 * 
	 * @return
	 * @throws Exception
	 */
	public Map getTreeViewData() throws Exception {
		
		IExecDB iedb=IExecDB.getIExcecDB();
		
		try{
			HashMap rtn=new HashMap();
			
			iedb.connectDB();			
			
			List glist=iedb.listGroups();
			
			List folders=iedb.listFolders();
			List strategies=iedb.listStrategies();
			
		 
			Vector groups=new Vector();
			HashMap colors=new HashMap();			
			for(Iterator it=glist.iterator();it.hasNext();){
				Map data=(Map)it.next();
              
				ValueObject vo=new ValueObject();
				vo.setKey((String)data.get("group_uid"));
				vo.setValue((String)data.get("group_name"));
				groups.add(vo);
                colors.put(data.get("group_uid"), data.get("color_code"));
			}
			rtn.put("folders", folders);
			rtn.put("groups",groups);
			rtn.put("group_colors",colors);
			rtn.put("strategies",strategies);
			
			return rtn;
			
			
		}catch(Exception e){
			ClientError.reportError(e, null);
			throw e;	
		}finally{
			iedb.closeDB();
		}
	}
	
	
	public Map getStrategy(int strategy_id,boolean readonly) throws Exception {
		
		IExecDB iedb=IExecDB.getIExcecDB();
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		try{
			
			iedb.connectDB();			 
			sdb.connectDB();
			
			Map data=iedb.getStrategy(strategy_id);
		
				
			
			Map rtn=getDataBundle4Strategy(data,iedb,sdb);

			
			String usr_alreadylocked=(String)((Map)rtn.get("data")).get("lockedby");
			String usr=new SchedulerMgmt(getRequest()).getAuthorizedUser(sdb);	
			
			//System.out.println("RFunctionMgmt.getRFunction():usr:"+usr+" usr_alreadylocked:"+usr_alreadylocked);
			if(!readonly){				
				if(usr_alreadylocked!=null && !usr_alreadylocked.equals("") && !usr_alreadylocked.equalsIgnoreCase(usr)){
					//dont relock
				}else{													   			 
					refreshCache(strategy_id, 300,usr);
				}				
			}else{
				rtn.put("readonly", true);
			}
			
			//rtn.put("content", "");
			return rtn;
			
			
		}catch(Exception e){
			ClientError.reportError(e, null);
			throw e;	
		}finally{
			iedb.closeDB();
			sdb.closeDB();
			 
		}
		
	}
	
	public Map getPlaceHolder(int strategy_id, String contract) throws Exception {
		
		IExecDB iedb=IExecDB.getIExcecDB();
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		try{
			
			iedb.connectDB();			 
			sdb.connectDB();
			
			Map data=iedb.getStrategy(strategy_id);
			String strategy_name=(String)data.get("strategy_name");
			 
			Map rtn=getDataBundle4Strategy(data,iedb,sdb);
			
			HashMap h=new HashMap();			
			Map values=iedb.getParameterValues(strategy_name, contract);
			String parent_name=(String)data.get("parent_strategy");
			
			log.debug("parent_name:"+parent_name);		
			
			if(rtn.get("content")!=null){
				String template = (String) rtn.get("content");
				boolean isParentNameNotNull = (parent_name!=null && !parent_name.trim().equals(""));
				if (isParentNameNotNull) {
					h.put("att_ph", StringPlaceHolder4ChildStrategy.getAttributePH(template));					
					h.put("ele_ph", StringPlaceHolder4ChildStrategy.getElementPH(template));
				}
				else {
					Vector att=StringPlaceHolder.getAttributePH(template);
					att.addAll(StringPlaceHolder4ChildStrategy.getAttributePH(template));
					h.put("att_ph", att);
					h.put("att_ph_c", StringPlaceHolder4ChildStrategy.getAttributePH(template));
					Vector elem=StringPlaceHolder.getElementPH(template);
					elem.addAll(StringPlaceHolder4ChildStrategy.getElementPH(template));
					h.put("ele_ph", elem);
					h.put("ele_ph_c", StringPlaceHolder4ChildStrategy.getElementPH(template));				
				}
			}
			h.put("ph_data", values);
			
			return h;
		 
			
		}catch(Exception e){
			ClientError.reportError(e, null);
			throw e;	
		}finally{
			iedb.closeDB();
			sdb.closeDB();
			 
		}
		
	}
	
	
	/**
	 * Using strategy name and contract, parses the data with xml input and return parsed XML
	 * 
	 * @param strategy_name
	 * @param contract
	 * @return
	 * @throws Exception
	 */
	public String getMyParsedXML(String strategy_name, String contract) throws Exception {
		
		IExecDB iedb=IExecDB.getIExcecDB();
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		try{
			
			iedb.connectDB();			 
			sdb.connectDB();
			
			Map data=iedb.getStrategy(strategy_name);
			String parent_s=strategy_name;
			boolean child_strategy=false;
			if(data.get("parent_strategy")!=null && !((String)data.get("parent_strategy")).equals("")){
				parent_s=(String)data.get("parent_strategy");
				child_strategy=true;
			}
			
			
			
			Map rtn=getDataBundle4Strategy(data,iedb,sdb);
			String xml="";

			
			
			Map st1=iedb.getParameterValues(parent_s, contract);
			
			Pattern pattern =       Pattern.compile("^([A-Za-z\\s]+)([0-9]+)S$", Pattern.DOTALL);		 
			String commodity=null;
			Matcher matcher = pattern.matcher(contract);
			if(matcher.find()){
				commodity = matcher.group(1);
				 							
			}
			if(commodity!=null){
				 Map values1=iedb.getParameterValues(parent_s, commodity);			    
		    	mergeMaps(st1, values1);
			}

			Map defa=iedb.getParameterValues(parent_s, "Default");
			mergeMaps(st1, defa); 
			log.debug("parent_s:"+parent_s);
			
			if(rtn.get("content")!=null){
				xml=StringPlaceHolder.parse((String)rtn.get("content"), st1);
			}
			log.debug("xml:"+xml);
			if(child_strategy){
				//Map para_c=iedb.getParameterValuesUnique(strategy_name, contract);
				Map st2=iedb.getParameterValues(strategy_name, contract);				
				log.debug("strategy_name:"+strategy_name+" contract:"+contract);
				log.debug("st2:"+st2);
				if(commodity!=null){				 			
					Map values1=iedb.getParameterValues(strategy_name, commodity);			    
				    mergeMaps(st2, values1);
				}   
				Map defa2=iedb.getParameterValues(strategy_name, "Default");
				mergeMaps(st2, defa2);
				
				//merge from parent also 
				mergeMaps(st2,st1);
				
				xml=StringPlaceHolder4ChildStrategy.parse(xml, st2);
				
				
				
				//
				//ParseXMLPlaceHolder pxph=new ParseXMLPlaceHolder((String)rtn.get("content"));	
			}
			return xml;	
		}catch(Exception e){
			ClientError.reportError(e, null);
			throw e;	
		}finally{
			iedb.closeDB();
			sdb.closeDB();
			 
		}
		
	}

	
	private void mergeMaps(Map child, Map parent){
		if(parent!=null && child!=null){
			for(Iterator i=parent.keySet().iterator();i.hasNext();){
				String pkey=(String)i.next();
				if(!child.containsKey(pkey) ||(child.containsKey(pkey) && ((String)child.get(pkey)).equals(""))){
					child.put(pkey,parent.get(pkey));
				}
			}
		}
	}
	
	
	public boolean deleteStrategy(int function_id) throws  Exception {
		   //create folder;
		   Map fld=createFolderIfNotExist("Trash","trash");
		   int folder_id=(Integer)fld.get("folder_id");
		   moveFile2Folder(function_id, folder_id);
		   return true;
	}
	
	private Map createFolderIfNotExist(String folder_name, String group_id) throws Exception {
		IExecDB iedb=IExecDB.getIExcecDB();
		
		try{
			String folder=Config.getString("iexec_source_folder");
			
			//String oldfile=folder+old_folder;
			String newfile=folder+folder_name;			
			
		 
				iedb.connectDB();
				
				int folder_id=iedb.getFolderID(folder_name);
				if(folder_id<=0){
					folder_id=iedb.createFolder(folder_name, group_id);
				}
				File nfile=new File(newfile);
				if(!nfile.isDirectory() && !nfile.isFile()) {
					nfile.mkdirs();
				}
				HashMap rtn=new HashMap();
				
				List folders=iedb.listOfFolders(group_id);
				rtn.put("folders", folders);
				rtn.put("folder_id", folder_id);
				return rtn;
			
		}catch(Exception e){
			ClientError.reportError(e, "folder_id:"+folder_name+" group_id:"+group_id);
			throw e;	
		}finally{
			iedb.closeDB();
		}
	}
	
	
	private String convertStreamToString(InputStream is) throws IOException {
		/*
		 * To convert the InputStream to String we use the Reader.read(char[]
		 * buffer) method. We iterate until the Reader return -1 which means
		 * there's no more data to read. We use the StringWriter class to
		 * produce the string.
		 */
		if (is != null) {
			
			Writer writer = new StringWriter();
			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(new InputStreamReader(is,
						"UTF-8"));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				is.close();
			}
			return writer.toString();
		} else {
			return "";
		}
	}

	
	/**
	 * Create child strategy 
	 * 
	 * @param parent_st_id
	 * @param strategy_name
	 * @return
	 * @throws Exception
	 */
	public Map createChildStrategy(int parent_st_id,String strategy_name) throws Exception {
		
					
		IExecDB iedb=IExecDB.getIExcecDB();
		
		
		try{
			
			HashMap rtn=new HashMap();
			
			iedb.connectDB();
			
		    Map existing=iedb.getStrategy(strategy_name);
		    
		    if(existing!=null && existing.size()>0){
		        throw new Exception("ERROR: Strategy name: "+strategy_name+" alerady existing, please try different name");	
		    }		    
		    
		    Map p_data=iedb.getStrategy(parent_st_id);
		    String parent_name=(String)p_data.get("strategy_name");
		    
			 				
		    int id=iedb.createChildStrategy(parent_name,strategy_name);
			
			 
			Map data=iedb.getStrategy(id);
				
		 
				
			String content="";
			Map func_data=new HashMap();
			func_data.put("content", content);
			func_data.put("data", data);
					
				
			Vector functions=new Vector();
			functions.add(data);
				
				
			rtn.put("strategies", functions);
			boolean readonly=true;
			rtn.put("strategy_data", getStrategy(id,!readonly));
				 
				 
				
			//}
			//rtn=getRFunction(id);
		    //String content=getContent(filepath);
			//rtn.put("content", content);
			//rtn.put("data", data);
			//return getRFunction(id);
			
			return rtn;
		}catch(Exception e){
			ClientError.reportError(e, "parent_st_id:"+parent_st_id+" fname:"+strategy_name+"  strategy_name:"+strategy_name);
			throw e;	
		}finally{
			iedb.closeDB();
		}
		
	}

	
	
	/**
	 * to create new strategy
	 *  
	 * @param folder_id
	 * @param strategy_name
	 * @param script
	 * @return
	 * @throws Exception
	 */
	
	public Map createStrategy(String folder_id,String strategy_name, String script) throws Exception {
		
		String folder=Config.getString("iexec_source_folder");			
		IExecDB iedb=IExecDB.getIExcecDB();
		
		
		try{
			
			//log.debug(" script: "+script);
			if(script==null || (script!=null && script.trim().equalsIgnoreCase(""))){
				//InputStream in=Config.class.getResourceAsStream("strategy_name_template.xml");
				//FileUtils
				
				String rtn="";
				try{
					InputStream in=Config.class.getResourceAsStream("strategy_new_template.xml");				
					rtn=convertStreamToString(in);
				}catch(Exception e){
					log.error("Error while reading strategy_new_template.xml file");
				}
				//log.debug("rtn:"+rtn);
				if(rtn!=null && !rtn.trim().equals("")){
					log.debug("template found ");
					HashMap para=new HashMap();
					para.put("name", strategy_name);
					script=StringPlaceHolder.parse(rtn, para);
					//log.debug("script:"+script);
				}
				
			}
			
		    
			HashMap rtn=new HashMap();
			
			iedb.connectDB();
			
		    Map existing=iedb.getStrategy(strategy_name);
		    
		    if(existing!=null && existing.size()>0){
		        throw new Exception("ERROR: Strategy name: "+strategy_name+" alerady existing, please try different name");	
		    }
		    String subfolder=iedb.getFolderName(Integer.parseInt(folder_id));
		    
		    String filename=strategy_name+new SVNIExecXML().getExtension();
		    String filepath=folder+((subfolder!=null)?subfolder+File.separator:"")+filename;
		    
		    if(!new File(filepath).isFile()){
		    	createContent(filepath,script);
		    }
		    
			int id=iedb.createStrategy(Integer.parseInt(folder_id),strategy_name, filename);
			
			synchrnizeSVN(strategy_name,script,null,id);
			
			//if(false){
			Map data=iedb.getStrategy(id);
				
				//String filepath1=(String)data.get("script_file");
				//filepath1=folder+""+filepath1;
			   
			String filepath1=(String)data.get("file_name");
			filepath1=folder+((subfolder!=null)?subfolder+File.separator:"")+filepath1;
	
				
			String content=getContent(filepath1);
			Map func_data=new HashMap();
			func_data.put("content", content);
			func_data.put("data", data);
					
				
			Vector functions=new Vector();
			functions.add(data);
				
				
			rtn.put("strategies", functions);
			boolean readonly=true;
			rtn.put("strategy_data", getStrategy(id,!readonly));
				 
				
			//}
			//rtn=getRFunction(id);
		    //String content=getContent(filepath);
			//rtn.put("content", content);
			//rtn.put("data", data);
			//return getRFunction(id);
			
			return rtn;
		}catch(Exception e){
			ClientError.reportError(e, "folder_id:"+folder_id+" fname:"+strategy_name+"  script:"+script);
			throw e;	
		}finally{
			iedb.closeDB();
		}
		
	}
	
	
	
	public Map modifyStrategy(int strategy_id, String script, String message) throws Exception {
		
		String folder=Config.getString("iexec_source_folder");		
		IExecDB iedb=IExecDB.getIExcecDB();
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		try{

			HashMap rtn=new HashMap();		
			
			iedb.connectDB();
			sdb.connectDB();
			
			String usr=new SchedulerMgmt(getRequest()).getAuthorizedUser(sdb);
			String lockedby=getLockedBy(strategy_id, sdb, iedb);
			if(lockedby!=null && !usr.equalsIgnoreCase(getLockedBy(strategy_id, sdb, iedb))){
				throw new Exception("This function is currently locked by user "+lockedby);
			}
		  
			Map data=iedb.getStrategy(strategy_id);
			
		    
		    String strategy_name=(String)data.get("strategy_name");
		    //String filename=(String)data.get("file_name");
		    String filename=strategy_name+new SVNIExecXML().getExtension();
		    String sub_folder="";
		    try{
				sub_folder=iedb.getFolderName((Integer)data.get("folder_id"));
			
			}catch(Exception e){}
		    

		    String filepath=folder+((sub_folder!=null)?sub_folder+File.separator:"")+filename;
		    createContent(filepath,script);
		    
			synchrnizeSVN(strategy_name,script,message,strategy_id);			
			
			Map data1=iedb.getStrategy(strategy_id);; //this gets with latest tag after SVN sycnrhonize
		 
	    	Vector v=new Vector();
	    	v.add(data1);	    		
	    		
	    		//return listScheduledItems();	    		
	    	rtn.put("rfunctions", v);

			return rtn;
				
		    //String content=getContent(filepath);
			//rtn.put("content", content);
			//rtn.put("data", data);
			
		}catch(Exception e){
			e.printStackTrace();
			//ClientErrorMgmt.reportError(e,null);			
			throw e;	
			
		}finally{
			sdb.closeDB();
			iedb.closeDB();
		}
		
	}

	
	public Map createFolder(String folder_name, String group_id) throws Exception {
		IExecDB iedb=IExecDB.getIExcecDB();
		try{
			String folder=Config.getString("iexec_source_folder");		
			//String oldfile=folder+old_folder;
			String newfile=folder+folder_name;			
			
			if(new File(newfile).isDirectory() || new File(newfile).isFile()){
				throw new Exception("Folder already existing folder failed....Path:"+newfile);				
			}else{
				iedb.connectDB();
				
				int folder_id=iedb.getFolderID(folder_name);
				if(folder_id<=0){
					folder_id=iedb.createFolder(folder_name, group_id);
				}
				File nfile=new File(newfile);
				if(!nfile.isDirectory() && !nfile.isFile()) {
					nfile.mkdirs();
				}
				HashMap rtn=new HashMap();		
				List folders=iedb.listOfFolders(group_id);
				rtn.put("folders", folders);
				rtn.put("folder_id", folder_id);
				return rtn;
			}
			
		}catch(Exception e){
			ClientError.reportError(e, "folder_id:"+folder_name+" group_id:"+group_id);
			throw e;	
		}finally{
			iedb.closeDB();
		}
	}
	
	public boolean moveFolder(int folder_id, String new_group_id) throws Exception {
		 
		IExecDB iedb=IExecDB.getIExcecDB();
		
		try{
				
			iedb.connectDB();
			iedb.moveFolder(folder_id, new_group_id);
			return true;
			
		}catch(Exception e){
			ClientError.reportError(e, "folder_id:"+folder_id+" new_group_id:"+new_group_id);
			throw e;	
		}finally{
			iedb.closeDB();
		}
	}
	
	
	public boolean moveFile2Folder(int strategy_id, int new_folder_id) throws Exception {
		IExecDB iedb=IExecDB.getIExcecDB();
		
		try{
			
			String folder=Config.getString("iexec_source_folder");		
			
			iedb.connectDB();
			
			Map func=iedb.getStrategy(strategy_id);
			String src_folder=null;
			try{
				src_folder=iedb.getFolderName((Integer)func.get("folder_id"));
			}catch(Exception e){}
			String dest_folder=iedb.getFolderName(new_folder_id);
			String script_file=(String)func.get("file_name");
			if(src_folder.equals(dest_folder)) {
				throw new Exception("Moving failed! source and destination are the same..");
			}
				 
			String oldfile=folder+((src_folder!=null)?src_folder+File.separator:"")+script_file;
			String newfile=folder+((dest_folder!=null)?dest_folder+File.separator:"")+script_file;
			

			log.debug("old file:"+oldfile+" new :"+newfile);
			
			if(new File(oldfile).renameTo(new File(newfile))){
				log.debug("new folder_id:"+strategy_id+" function_id:"+new_folder_id);
				iedb.updateStrategyFolder(strategy_id,new_folder_id);
			}else{
				throw new Exception("Moving failed failed....");
			}
			
			return true;
		}catch(Exception e){
			ClientError.reportError(e, "function_id:"+strategy_id+" new_folder_id:"+new_folder_id);
			throw e;	
		}finally{
			iedb.closeDB();
		}
		
		
	}
	
	 public void setGroupOrder(Vector groupids) throws Exception {
		 IExecDB iedb=IExecDB.getIExcecDB();
			try{
				iedb.connectDB();
				iedb.setGroupOrder(groupids);
			}catch(Exception e){
				ClientError.reportError(e, null);
				throw e;
			}finally{
				iedb.closeDB();
			}
	   }
	 
	
	public boolean updateParameter(ArrayList<Map> data, String strategy_name) throws Exception {
		
		 
		IExecDB iedb=IExecDB.getIExcecDB();
 
		try{

			 
			iedb.connectDB();
			iedb.addParameters(data, strategy_name);
			return true;
			 
				
		    //String content=getContent(filepath);
			//rtn.put("content", content);
			//rtn.put("data", data);
		 
			
			
		}catch(Exception e){
			//e.printStackTrace();
			//ClientErrorMgmt.reportError(e,null);
			
			throw e;	
		}finally{
			 
			iedb.closeDB();
		}
		
	}
	
	private boolean synchrnizeSVN(String strategy_name, String xml, String message, int strategy_id) throws Exception {
		
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
			
 
			//Map u=sdb.getSVNUser(clientip);
			Map u=sdb.getSVNUser4WikiUser(user);
			if(u!=null && u.get("svn_username")!=null && u.get("svn_password")!=null){
				svnuser=(String)u.get("svn_username");
				svnpwd=(String)u.get("svn_password");
			}
			//rtn.put("usr",rs5.getString("svn_username"));
			//rtn.put("pwd",rs5.getString("svn_password"));
			
			
			SVNIExecXML sync=new SVNIExecXML(svnuser,svnpwd);
			sync.syncFile(strategy_name, xml,message);
			 
			return true;
		}catch(Exception e){
			log.error("Error while committing function into SVN ");
			throw e;
		}finally{
			try{sdb.closeDB();}catch(Exception e) {	log.error("error while cloing db:"+e.getMessage());	}
		}
		
	}
	
	public boolean removeContract(String strategy_name, String contract )throws Exception {
		IExecDB iedb=IExecDB.getIExcecDB();
		try{			 
			iedb.connectDB();		 
			iedb.removeContract(strategy_name, contract);
			 
			return true;
		}catch(Exception e){
		
			throw e;
		}finally{
			iedb.closeDB();			
		}
	}
	
	
	public Vector getSVNLogs(String function_name) throws Exception {
		SVNIExecXML sync=new SVNIExecXML();
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
	public String getScriptRev(String strategy_name, String revision,boolean flag,String path) throws Exception {
		try{
			SVNIExecXML sync=new SVNIExecXML();
			String script=null;
			if(flag)
				script=sync.getScript(strategy_name, Long.parseLong(revision),path);
			else
				//script=sync.diffWC(function_name, Long.parseLong(revision));
				script=sync.getWhatChanged(strategy_name, Long.parseLong(revision));
				if(script==null){
					script="No changes were made or first revision of this function";
				}
			
			return script;
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
		
	}
	
	private Map getDataBundle4Strategy(Map data, IExecDB iedb,SchedulerDB sdb) throws Exception{
		HashMap rtn=new HashMap();
		String folder=Config.getString("iexec_source_folder");
		String subfolder=""; 
		if(data.get("folder_id")!=null){
			subfolder=iedb.getFolderName((Integer)data.get("folder_id"));
		}
	    int strategy_id=(Integer)data.get("id");
	    
	    
	    String strategy_name=(String)data.get("strategy_name");
	    String original_strategy=(String)data.get("strategy_name");
	    
	    String parent_name=(String)data.get("parent_strategy");
	    boolean child_strategy=false;
	    if(parent_name!=null && !parent_name.equals("")) {
	    	 Map parent_data=iedb.getStrategy(parent_name);
	    	 strategy_name=(String)parent_data.get("strategy_name");
	    	 subfolder=iedb.getFolderName((Integer)parent_data.get("folder_id"));
	    	 child_strategy=true;
	    }
	    
	    
	    
	   
		   // String filepath=(String)data.get("file_name");
	    String filepath=strategy_name+new SVNIExecXML().getExtension();
	    
	    log.debug("getDataBundle4Strategy() child_strategy:"+child_strategy);
	    log.debug("getDataBundle4Strategy() content strategy_name:"+strategy_name);
	    log.debug("getDataBundle4Strategy() Content:"+filepath);
	    
	    
	    String scriptname=filepath;
	    filepath=folder+((subfolder!=null)?subfolder+File.separator:"")+filepath;
	    String content="";
	    log.debug("getDataBundle4Strategy() filepath:"+filepath);
	    
	    try{
	    	content=getContent(filepath);
	    }catch(Exception e){
	    	content="<error>"+e.getMessage()+"</error>";
	    }
	    //log.debug("getDataBundle4Strategy() Content:"+content);
	    
		String tl=(String)data.get("lockedby");
		if(tl==null || (tl!=null && tl.equals("")) ){
			data.put("lockedby",getLockedBy(strategy_id, sdb, iedb));
		}
		
		
		rtn.put("element_ph_found", false);
		
		if (child_strategy) {
	 		if(StringPlaceHolder4ChildStrategy.getElementPH(content).size()>0){
				rtn.put("element_ph_found", true);
			}
		}
		else {
	 		if(StringPlaceHolder.getElementPH(content).size()>0){
				rtn.put("element_ph_found", true);
			}			
		}
					
			
		rtn.put("content", content);
		rtn.put("data", data);
		rtn.put("isParent",  iedb.isParent(original_strategy));
		
		try{
			rtn.put("isAuthorized", new SchedulerMgmt(getRequest()).isAuthorizedUser(sdb));
			rtn.put("authorizedUser", new SchedulerMgmt(getRequest()).getAuthorizedUser(sdb));
		}catch(Exception e){
			//log.error("Error:"+e.getMessage());
		}
		Vector v=new Vector(); 
		v.add("Default");
		Vector v1=iedb.getUniqueContracts(original_strategy);
		if(v1.contains("Default")) v1.remove("Default");
		v.addAll(v1);
		rtn.put("contracts",v);
		
		
		return rtn;
		
	}

	
	public boolean renameStrategy(String new_st_name, int strategy_id) throws Exception {
		String folder=Config.getString("iexec_source_folder");
		IExecDB iedb=IExecDB.getIExcecDB();
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		
		try{

			 
		
			iedb.connectDB();
			
		    Map existing=iedb.getStrategy(new_st_name);
		    if(existing!=null && existing.size()>0){
		        throw new Exception("ERROR: Strategy name: "+new_st_name+" alerady existing, please try different name");	
		    }
			HashMap rtn=new HashMap();			
			iedb.connectDB();
			sdb.connectDB();
			
			String usr=new SchedulerMgmt(getRequest()).getAuthorizedUser(sdb);
			String lockedby=getLockedBy(strategy_id, sdb, iedb);
			if(lockedby!=null && !usr.equalsIgnoreCase(getLockedBy(strategy_id, sdb, iedb))){
				throw new Exception("This strategy is currently locked by user "+lockedby);
			}
		  
			Map old_data=iedb.getStrategy(strategy_id);		
			
			String parent=(String)old_data.get("parent_strategy");
			if(parent!=null && !parent.trim().equals("")){
				iedb.renameStrategy(strategy_id, new_st_name, null);
			}else{
				String new_filename=new_st_name+new SVNIExecXML().getExtension();			
				String old_filename=(String)old_data.get("file_name");
				String old_strategy_name=(String)old_data.get("strategy_name");
				String sub_folder="";
				try{
						sub_folder=iedb.getFolderName((Integer)old_data.get("folder_id"));
				}catch(Exception e){}
					
				
				String oldfile=folder+((sub_folder!=null)?sub_folder+File.separator:"")+old_filename;
				String newfile=folder+((sub_folder!=null)?sub_folder+File.separator:"")+new_filename;
				
				Map script_data=getDataBundle4Strategy(old_data,iedb,sdb);
				String script=(String)script_data.get("content");
				String message="";
				//if(script!=null){
					//script="#"+new_st_name+" "+System.getProperty("line.separator")+script;
					message="Renamed  "+old_strategy_name+"  --> " +new_st_name;
				//}
				
				
				log.debug("old file:"+oldfile+" new :"+newfile);
				
				if(new File(oldfile).renameTo(new File(newfile))){
					
					//iedb.renameStrategy(strategy_id, new_st_name, new_filename);
					iedb.renameParentStrategy(strategy_id, new_st_name, new_filename,old_strategy_name);
					
				}else{					
					throw new Exception("Renaming failed");
				}
			
	
				Map new_data=iedb.getStrategy(strategy_id);		
			
			   
			    //String filepath=folder+((sub_folder!=null)?sub_folder+File.separator:"")+new_filename;
			    //createContent(filepath,script,sub_folder,new_filename);
			    
			    renameSVN(old_strategy_name,new_st_name,script,message,strategy_id);			
			
			}
			//Map data1=iedb.getStrategy(strategy_id); //this gets with latest tag after SVN sycnrhonize
		 
	    	//Vector v=new Vector();
	    	//v.add(data1);	    		
	    		
	    		//return listScheduledItems();	    		
	    	//rtn.put("strategies", v);
	    	
			
			return true;
			
			
		}catch(Exception e){
			e.printStackTrace();
			//ClientErrorMgmt.reportError(e,null);
			
			throw e;	
		}finally{
			sdb.closeDB();
			iedb.closeDB();
		}
	}
	
	 private boolean renameSVN(String old_strategy_name,String new_strategy_name, String strategy, String message, int function_id) throws Exception {
			
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
				

				//Map u=sdb.getSVNUser(clientip);
				Map u=sdb.getSVNUser4WikiUser(user);
				if(u!=null && u.get("svn_username")!=null && u.get("svn_password")!=null){
					svnuser=(String)u.get("svn_username");
					svnpwd=(String)u.get("svn_password");
				}
				//rtn.put("usr",rs5.getString("svn_username"));
				//rtn.put("pwd",rs5.getString("svn_password"));
			
				SVNIExecXML sync=new SVNIExecXML(svnuser,svnpwd);
				sync.renameFile(old_strategy_name, new_strategy_name, strategy, message);
				
				
			 
				 
				return true;
			}catch(Exception e){
				log.error("Error while committing function into SVN ");
				throw e;
			}finally{
				try{sdb.closeDB();}catch(Exception e) {	log.error("error while cloing db:"+e.getMessage());	}
			}
			
			
		}
	
	private void createContent(String fullpath, String script) throws Exception {
		 
		  Writer output = null;		 
		  File file = new File(fullpath);
		  output = new BufferedWriter(new FileWriter(file));
		  output.write(script);
		  output.close();
		   
	}
	
	public String getStrategyWiki(String strategy) throws Exception {
		WikiRFunctionManual wiki=new WikiRFunctionManual();		
		//String rtn=wiki.getWikiHTML(strategy);
		
		String username=Config.getString("wiki.username");
		String password=Config.getString("wiki.password");
		String wikiurl=Config.getString("wiki.wikiurl");
		
		String rtn=wiki.getWikiHTML(username, password, wikiurl, strategy);		
		return rtn;
		
	}
	
	public Vector getStrategies(String strategy_names,boolean readonly) throws Exception {
		
		IExecDB iedb=IExecDB.getIExcecDB();
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		try{
	
			StringTokenizer st=new StringTokenizer(strategy_names,",");
			Vector fnames=new Vector();
			while(st.hasMoreTokens()){
				fnames.add(st.nextToken());
			}
			Vector rtn=new Vector();
			iedb.connectDB();
			sdb.connectDB();
			//Map data=rfdb.getRFunction(function_id);
			//rtn.put("lockedby", getLockedBy(function_id))
			Vector data1=iedb.getStrategies(fnames);
			
			String usr=new SchedulerMgmt(getRequest()).getAuthorizedUser(sdb);
			
			for(Iterator i=data1.iterator();i.hasNext();){
				Map data=(Map)i.next();
				Map func=getDataBundle4Strategy(data,iedb,sdb);
				int function_id=(Integer)data.get("id");
				String usr_alreadylocked=(String)((Map)func.get("data")).get("lockedby");			
				if(!readonly){				
					if(usr_alreadylocked!=null && !usr_alreadylocked.equals("") && !usr_alreadylocked.equalsIgnoreCase(usr)){
						//dont relock
					}else{													   			 
						refreshCache(function_id, 300,usr);
					}
				}else{
					func.put("readonly", true);
				}
				
				rtn.add(func);
			}
			return rtn;

			
		}catch(Exception e){
			ClientError.reportError(e, null);
			throw e;	
		}finally{
			iedb.closeDB();
			sdb.closeDB();
		}
		
	}
	
	
	
	public Map getContractComm() throws Exception {
		IExecDB iedb=IExecDB.getIExcecDB();
		try{
			
			
			iedb.connectDB();
			TreeMap rtn=new TreeMap();
			
	 
			TreeMap cont=new TreeMap();
			
			Vector<String> comm=iedb.getCommodityTree();
			Vector contract=iedb.getContractTree();
			//TreeMap t=new TreeMap();
			for(Iterator i=contract.iterator();i.hasNext();){
				Map row=(Map)i.next();
				String comm1=(String)row.get("commodity");
				String contract1=(String)row.get("contract");
				Vector v;
				
				if(cont.get(comm1)==null) { v=new Vector();cont.put(comm1.trim(),v); }
				else{v=(Vector)cont.get(comm1);}				
				v.add(comm1+(v.size()+1)+"S");
				//t.put(contract1, comm1);
			}
			
			rtn.put("commodity", comm);
			rtn.put("contract", cont);
			//rtn.put("all", t);  //this is not needed, just for testing, remove related code to this line
			
			return rtn;
		}catch(Exception e){
			e.printStackTrace();
			throw e;	
		}finally{		 
			iedb.closeDB();
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
	
	
	public boolean lockFunction(int strategy_id, long seconds) throws Exception {
		SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		RFunctionDB rfdb=RFunctionDB .getRFunctionDB();
		try{
			sdb.connectDB();
			rfdb.connectDB();
			String usr=new SchedulerMgmt(getRequest()).getAuthorizedUser(sdb);
			if(seconds>0){			   			 
			   refreshCache(strategy_id, seconds,usr);		
			   
			   //rtn=cachedPeers.getMatching("^[A-Za-z0-9]+$");
			}else{
				rfdb.updateLock(strategy_id, usr);
			}
			return true;
		}catch(Exception e){
		
			throw e;
		}finally{
			sdb.closeDB();
			rfdb.closeDB();
		}
	}
	
	
	public boolean unLockStrategyFromCache(int function_id) throws Exception {
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
	
	private void removeLockFromCache(int strategy_id, String usr ) throws Exception {
	       //String ky=usr+"_"+function_id;
		   String ky=usr+strategy_id;
		   if(getLockCache().get(ky)!=null)getLockCache().remove(ky);
		
	}
	
	private String getLockedBy(int iexec_id,SchedulerDB sdb,IExecDB iedb) throws Exception {
		//SchedulerDB sdb=SchedulerDB.getSchedulerDB();
		//RFunctionDB rfdb=RFunctionDB .getRFunctionDB();
		try{
			//rfdb.connectDB(getDBName());
			//String usr=new SchedulerMgmt(getRequest()).getAuthorizedUser(sdb);
			Map data=iedb.getStrategy(iexec_id);
			String rtn=null;
			if(data.get("lockedby")!=null ){			   			 
			   //refreshCache(function_id, seconds,usr);			   
			   //rtn=cachedPeers.getMatching("^[A-Za-z0-9]+$");
				rtn=(String)data.get("lockedby");
			}else{
				//rfdb.updateLock(function_id, usr);
				Map caches=getLockCache().getMatching("^[A-Za-z0-9]+$");
				if(caches!=null){
					for(Iterator i=caches.keySet().iterator();i.hasNext();){
						String ky=(String)i.next();
						
							Map d=(Map)caches.get(ky);							
							int f_id=(Integer)d.get(IExecMgmt.STRATEGY_ID);
							long dur=(Long)d.get(IExecMgmt.LOCK_DURATION);
							String usr1=(String)d.get(IExecMgmt.USER);
							//if(ky.startsWith(usr)){
							if(f_id==iexec_id) rtn=usr1;
							//}
					}
				}	
			}
			return rtn;
		}catch(Exception e){
			
			throw e;
			
		}finally{
			//sdb.closeDB();
			//rfdb.closeDB();
		}	
	}
	
	private void refreshCache(int strategy_id, long seconds, String usr ) throws Exception {
		
	       //String ky=usr+"_"+function_id;
		   String ky=usr+strategy_id;
	       
		   HashMap h=new HashMap();
		   h.put(IExecMgmt.STRATEGY_ID, strategy_id);
		   h.put(IExecMgmt.LOCK_DURATION, seconds);
		   h.put(IExecMgmt.USER,  usr);
		   
		   IElementAttributes att= getLockCache().getDefaultElementAttributes();
		   att.setMaxLifeSeconds(seconds);
		   if(getLockCache().get(ky)!=null)getLockCache().remove(ky);
		   getLockCache().put(ky,h,att);

		
	}
	
	private static String STRATEGY_ID="function_id";
	private static String LOCK_DURATION="duration";
	private static String USER="user";
	
	private static JCS lockcache=null;
	
	private static JCS getLockCache() throws Exception {
		if(IExecMgmt.lockcache==null) IExecMgmt.lockcache=JCS.getInstance(IExecMgmt.class.getName());
		return IExecMgmt.lockcache;
	}
	
	
	
	
	
	
}



