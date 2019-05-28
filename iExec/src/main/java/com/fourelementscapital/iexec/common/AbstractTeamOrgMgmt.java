/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.iexec.common;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fourelementscapital.alarm.Alarm;
import com.fourelementscapital.alarm.AlarmType;
import com.fourelementscapital.alarm.ThemeVO;
import com.fourelementscapital.auth.UserThemeAccessPermission;
import com.fourelementscapital.client.Authenticated;
import com.fourelementscapital.config.Constant;
import com.fourelementscapital.db.AbstractTeamOrgDB;
import com.fourelementscapital.db.SchedulerDB;
import com.fourelementscapital.scheduler.TemplateParser;

/**
 * This class is abstract and provide basic implementation for all Team Organization related moudles
 * for example:  Themes tag support on scheduler, R function *  
 * @author Administrator
 *
 */
@SuppressWarnings("unchecked")
public abstract class AbstractTeamOrgMgmt extends Authenticated{

	protected static String ACCESS_PRIVILEGE_RWX="RWX";
	protected static String ACCESS_PRIVILEGE_RX="RX";
	protected static String ACCESS_PRIVILEGE_R="R";
	
	private Logger log = LogManager.getLogger(AbstractTeamOrgMgmt.class.getName());
	
	
	
	
	/**
	 * this constructor to be used only for DWR, manual invoking to get the object using of this constructor will mis-behave   
	 * @throws Exception
	 */
	public AbstractTeamOrgMgmt() throws Exception {
		super();
		 
	}


	/**
	 * this constructor will be used only when JSP or Httprequest request is available.
	 * @param request
	 * @throws Exception
	 */
	public AbstractTeamOrgMgmt(HttpServletRequest request) throws Exception {
		super(request);
	}
	
	
	
	/**
	 * to be used in child class
	 * @param sdb
	 * @return
	 * @throws Exception
	 */
	protected Map getThemeAccessData(AbstractTeamOrgDB sdb) throws Exception {
		
		UserThemeAccessPermission auth=getAuthenticatedUserObj(sdb);
		HashMap rtn=new HashMap();
		if(auth!=null){
			  	rtn.put("rwx_tags",auth.getRwx());
			   	rtn.put("rx_tags",auth.getRx());
			   	rtn.put("r_tags",auth.getR());			    	
		}
		
		String superuser=(String)getRequest().getSession().getAttribute(Constant.SESSION_LOGGED_SUPERUSER);
		if(superuser!=null && !superuser.equals("")){
			rtn.put("superuser",superuser);
		}
		return rtn;
	}
	
	
	
	/**
	 * 
	 * @param scheduler_id
	 * @param sdb
	 * @return
	 * @throws Exception
	 */
	protected String getAccessPrivilege(int scheduler_id, AbstractTeamOrgDB sdb) throws Exception {
		String rtn="";
		UserThemeAccessPermission user=getAuthenticatedUserObj(sdb);
		if(user!=null){
			 List<String> themes=sdb.getThemeTags(scheduler_id);

			 log.debug("themes:"+themes);
			 log.debug("user.getRwx():"+user.getRwx());
			 log.debug("user.getRx():"+user.getRx());
			 log.debug("user.getR():"+user.getR());
			
			 
             for(String ttag:themes){            	 
            	 if(user.getRwx().contains(ttag)) rtn=ACCESS_PRIVILEGE_RWX;
            	 if(user.getRx().contains(ttag) && (!rtn.equals(ACCESS_PRIVILEGE_RWX) )) rtn=ACCESS_PRIVILEGE_RX; 
            	 if(user.getR().contains(ttag) && rtn.equals("")) rtn=ACCESS_PRIVILEGE_R; 
             }
             if(themes.size()==0) rtn=ACCESS_PRIVILEGE_RWX; 
           
 	        
             
             

		}
		log.debug("user:"+user);
		log.debug("getAccessPrivilege:rtn:"+rtn); 		 
		String superuser=(String)getRequest().getSession().getAttribute(Constant.SESSION_LOGGED_SUPERUSER);
		if(superuser!=null && !superuser.equals("")){
			rtn=ACCESS_PRIVILEGE_RWX;
		}
		if(rtn.equals("")) rtn=null;
		return rtn;
	}
	
	
	
	  /**
	   * @deprecated
	   * @param notity_tags
	   * @param sdb
	   * @return
	   * @throws Exception
	   */
	  protected ArrayList getNotifications(List notity_tags, AbstractTeamOrgDB sdb) throws Exception {
		   
		   Map<String,String> priv_data=getThemeHirarchy(notity_tags,sdb);		   
		   ArrayList user_notification=new ArrayList();
		   
			for(Iterator<String> i=priv_data.keySet().iterator();i.hasNext();){
				String ky=i.next();
				if(priv_data.get(ky)!=null && priv_data.get(ky).equalsIgnoreCase("rwx")){
					user_notification.add(ky);
				}
			}
			return user_notification; 
	   }
	  
	  
		
	 
	  
	  
	  /**
	   * 
	   * @param sdb
	   * @return
	   * @throws Exception
	   */
	  protected boolean isAuthorizedUser(SchedulerDB sdb) throws Exception {
			 
			String user=getAuthenticatedUser();
			if(user!=null){
				Map u=sdb.getSVNUser4WikiUser(user);
				if(u!=null && u.get("svn_username")!=null && u.get("svn_password")!=null
					&&	!((String)u.get("svn_username")).equals("") &&	!((String)u.get("svn_password")).equals("")
				){
					return true;
				}else{
					return false;
				}
			}else{
				return false;
			}
		}
		
	  /**
	   * 
	   * @param sdb
	   * @return
	   * @throws Exception
	   */
		protected String getAuthorizedUser(SchedulerDB sdb) throws Exception {
			 
			String user=getAuthenticatedUser();
			log.debug("getAuthorizedUser() user:"+user);
			if(user!=null){
				Map u=sdb.getSVNUser4WikiUser(user);
				if(u!=null && u.get("svn_username")!=null && u.get("svn_password")!=null
					&&	!((String)u.get("svn_username")).equals("") &&	!((String)u.get("svn_password")).equals("")
				){
					return (String)u.get("svn_username");
				}else{
					return null;
				}
			}else{
				return null;
			}
		}
		
		
		/**
		 * 
		 * @param scheduler_id
		 * @param sdb
		 * @return
		 * @throws Exception
		 */

		 protected Map getItemTags2(int scheduler_id,AbstractTeamOrgDB sdb) throws Exception {
			   
					HashMap h=new HashMap();				
					Vector tagids=sdb.getTagIds4Item(scheduler_id);
					Vector tags=sdb.getTags();
					Vector follow_tagids=sdb.getFollowTagIds4Item(scheduler_id);
					
					ArrayList<String> themes=sdb.getThemeNames4Item(scheduler_id);					
					
					Map priv_data=getThemeHirarchy(themes,sdb);
					if(priv_data.get("theme")!=null){
						priv_data.remove("theme");
					}
					
					List ftags=sdb.getFollowTags4Item(scheduler_id);
					List user_notifications=ftags.size()>0?getNotifications(ftags,sdb):new ArrayList();
					ArrayList all=new ArrayList(ftags);
					all.addAll(themes);
					
					h.put("tagids", tagids);
					h.put("user_privileges",priv_data);
					h.put("user_notifications",user_notifications);				
					h.put("follow_tagids",follow_tagids);
					h.put("notice_escalated", getThemeHirarchy(all,sdb));
					h.put("tags", tags);
					h.putAll(priv_data);
					
					return h;
		   }
		   
		   
		   /**
		    * 
		    * @param item_id
		    * @param newtask_tags
		    * @param follow_tags
		    * @param atodb
		    * @param sdb
		    * @param name
		    * @param comment
		    * @param rev
		    * @param diff
		    * @param tempdata
		    * @param templ_file
		    * @throws Exception
		    */
		   protected void updateAllItemTags(int item_id, List newtask_tags,List follow_tags,AbstractTeamOrgDB atodb,SchedulerDB sdb,String name, String comment,long rev,String diff, Map tempdata, String templ_file) throws Exception {
			   
				ArrayList<Integer> ids=new ArrayList();
    			for(Iterator i=newtask_tags.iterator();i.hasNext();){
    				String tid=(String)i.next();
    				ids.add(Integer.parseInt(tid));
    			}
    			atodb.updateItemTagIds(item_id, ids);
    			String owner_tag=null;
    			if(ids.size()>0){
    				owner_tag=atodb.getThemeTagName(ids.get(0));    			
    			}
    			ArrayList<Integer> fids=new ArrayList();
    			for(Iterator i=follow_tags.iterator();i.hasNext();){
    				String tid=(String)i.next();
    				fids.add(Integer.parseInt(tid));
    			}
    			atodb.updateFollwerTagIds(item_id, fids);
    			notifyLastModification(atodb,sdb,name,item_id,comment,rev,diff,tempdata,owner_tag,templ_file);
    			
		   }
		   
		   
		   private void notifyLastModification(AbstractTeamOrgDB atdb,SchedulerDB sdb,String task_name,int item_id, String comments,long revision, String diff,Map hdata,String owner_tag, String templ_filename) throws Exception {

				String currentuser=getAuthorizedUser(sdb);
				
				
	 	
				ArrayList<String> themes=atdb.getFollowTags4Item(item_id);				
				
				if(owner_tag!=null){
					if(themes.contains(owner_tag)) themes.remove(owner_tag);
					themes.add(0, owner_tag);
				}
				
				List<String> m_themes=atdb.getThemeNames4Item(item_id);
				for(String t:m_themes){
					if(!themes.contains(t)) themes.add(t);
				}
				
	 			String exc_user=currentuser+"@alphien.com";
	 		
					    String content="";
						InputStream in=AbstractTeamOrgMgmt.class.getResourceAsStream(templ_filename);
						
						BufferedReader br = new BufferedReader(new InputStreamReader(in));
						String strLine;			
						while ((strLine = br.readLine()) != null)   {		  
						   content+=(content.equals("")) ?strLine:"\n"+strLine;
						}
						br.close();
						in.close();
						
						TemplateParser pt=new TemplateParser(content,hdata);
						String message=pt.parseTemplate();
						String subject=pt.getSubject();
						
						try{
							
							// convert String array list to ThemeVO array list required by Alarm.sendAlarm() :
							ArrayList<ThemeVO> themeList = new ArrayList<ThemeVO>();
							for (int i=0; i<themes.size(); i++) {
								themeList.add(new ThemeVO(themes.get(i)));
							}
							
							Alarm.sendAlarm( themeList, AlarmType.EMAIL, subject, message, false, true, false, null,exc_user);
						}catch(Exception e){
							log.error("Couldn't send scheduler update notification to themes "+themes+" the followings are error:");
							e.printStackTrace();
						}		 
		}
		 		   
		   protected Map getItemPrivilegeNotifications(ArrayList themes,ArrayList ftags,AbstractTeamOrgDB sdb) throws Exception {
				 
				   
				   Map priv_data=getThemeHirarchy(themes,sdb);
				   HashMap h=new HashMap();		
				   
				   ArrayList all=new ArrayList(ftags);
				   all.addAll(themes);
				   List user_notifications=getNotifications(all,sdb);
				   h.put("user_notifications",user_notifications);		
				   h.put("user_privileges",priv_data);
				   h.put("notice_escalated",getThemeHirarchy(all,sdb));
					
				   return h;
			 
		   }
}



