/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fe.client;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.directwebremoting.WebContextFactory;

import com.fe.util.PasswordService;
import com.fourelementscapital.db.ConstructQueryDB;
import com.fourelementscapital.db.ContractDB;
import com.fourelementscapital.db.SuperDB;
import com.fourelementscapital.db.UtilDB;
import com.fourelementscapital.db.vo.Contract;
import com.fourelementscapital.db.vo.Strategy;
import com.fourelementscapital.scheduler.error.ClientError;


/**
 * This class methods can be accessed without authentication, 
 * this mgmt class provides method calls for authentication and certain
 * calls outside authentication.  
 */
public class AdminMgmt {

	/**
	 * key for storing certain objections in the session
	 */
	protected static String LOGGED_IN_ATTRIBUTE="adminLoggedOn";
	protected static String LOGGED_IN_USERNAME="adminLoggedUsername";	
	public static String DB_USER_PROPERTY_FILE="dbadmin.properties";
	
	private HttpServletRequest request=null;
	private Logger log = LogManager.getLogger(AdminMgmt.class.getName());
	
	
	/**
	 * to be used outside DWR
	 * @param request
	 * @throws Exception
	 */
	public AdminMgmt(HttpServletRequest request) throws Exception{
		this.request=request;
		String usrat=(String)getRequest().getSession().getAttribute(AdminMgmt.LOGGED_IN_ATTRIBUTE);
		if(usrat!=null &&  usrat.equalsIgnoreCase("true")){
			
		}else{
			//throw new Exception("You're not authorized to use this functionalities");
		}
	}
	
	/**
	 * To be used only within DWR as it requires HttpRequest object.
	 * @throws Exception
	 */
	public AdminMgmt() throws Exception{
		String usrat=(String)getRequest().getSession().getAttribute(AdminMgmt.LOGGED_IN_ATTRIBUTE);
		if(usrat!=null &&  usrat.equalsIgnoreCase("true")){
			
		}else{
			//do nothing
		}
	}
	

	/**
	 * Changing password of the authenticated user 
	 * @param oldpassword
	 * @param newpassword
	 * @return
	 * @throws Exception
	 */
	public boolean changePassword(String oldpassword,String newpassword) throws Exception {

		Properties prop = new Properties();
		try {
			
	    	prop.load(new FileInputStream(AdminMgmt.DB_USER_PROPERTY_FILE));
	    	
	    	String encpwd=PasswordService.encrypt(oldpassword);
	    	String encpwdnew=PasswordService.encrypt(newpassword);
	    	boolean success=false;
	    	
	    	if(prop.getProperty(getLoggedInUser())!=null){
	    		if(prop.getProperty(getLoggedInUser().toLowerCase()).equals(encpwd)){
	    			success=true;
	    		}
	    	}	    	
	    	if(!success){
	    		 throw new Exception("Invalid current password");
	    	}else{
	    		prop.setProperty(getLoggedInUser(), encpwdnew);
	    		prop.store(new FileOutputStream(AdminMgmt.DB_USER_PROPERTY_FILE), null);
	    		return true;
	    		
	    	}
	    } catch (IOException e) {
	    	throw e;
	    }
		
	}
	
	
	/**
	 * delete contract in the database
	 * @param db
	 * @param fieldtable
	 * @param contract
	 * @return
	 * @throws Exception
	 */
	public Map deleteContract(String db, String fieldtable, String contract) throws Exception {
		
		try{
			UtilDB udb=new SuperDB().getUtilDB(db);;			
			udb.connectDB(db);
			udb.deleteContracts( udb.connection(), fieldtable,contract);
			Vector contracts=udb.getUniqueContracts(udb.connection(), fieldtable);
			udb.closeDB();
			
			HashMap rtn=new HashMap();
			rtn.put("contracts", contracts);
			return rtn;
			
		} catch (Exception e) {
			ClientError.reportError(e, "db:"+db+",fieldtable:"+fieldtable);
			throw e;
		}
	}
	                                                                                           
	                                                                                           
	                                                                                           
	
	
	/**
	 * Get the field details for given database.
	 * @param db
	 * @param fieldtable
	 * @return
	 * @throws Exception
	 */
	public Map getFieldDetails(String db, String fieldtable) throws Exception {
		try{
			UtilDB udb=new SuperDB().getUtilDB(db);
			udb.connectDB(db);
			Vector contracts=udb.getUniqueContracts(udb.connection(), fieldtable);
			udb.closeDB();
			
			HashMap rtn=new HashMap();
			rtn.put("contracts", contracts);
			return rtn;
			
		} catch (Exception e) {
			ClientError.reportError(e, "db:"+db+",fieldtable:"+fieldtable);
			throw e;
		}
	}
	

	/**
	 * 
	 * @param db
	 * @param fieldtable
	 * @return
	 * @throws Exception
	 */
	public boolean removeFieldTable(String db, String fieldtable) throws Exception {
		try{
			UtilDB udb=new SuperDB().getUtilDB(db);
			udb.connectDB(db);
			
			boolean result=udb.removeFieldTable(udb.connection(), fieldtable);
			//boolean result=true;
			udb.closeDB();
			return result;
			
		} catch (Exception e) {
			ClientError.reportError(e, "db:"+db+",fieldtable:"+fieldtable);
			throw e;
		}
	}

	/**
	 * 
	 * @param db
	 * @param fieldtable
	 * @param originalname
	 * @return
	 * @throws Exception
	 */
	public boolean renameFieldTable(String db, String fieldtable, String originalname) throws Exception {
		try{
			UtilDB udb=new SuperDB().getUtilDB(db);
			udb.connectDB(db);			
			boolean result=udb.renameFieldTable(udb.connection(), fieldtable,originalname);			
			udb.closeDB();
			
			return true;
		} catch (Exception e) {
			ClientError.reportError(e, "db:"+db+",fieldtable:"+fieldtable);
			throw e;
		}
	}
	
	

	/**
	 * 
	 * @param db
	 * @param commodity
	 * @param contract
	 * @return
	 * @throws Exception
	 */
	public List getRecords4Contracts(String db, String commodity, String contract) throws Exception {
		
		log.debug("getRecords4Contracts() called");
		
		try{
			UtilDB udb=new SuperDB().getUtilDB(db);
			udb.connectDB(db);
			List result=udb.getRecordCount4Contracts(udb.connection(), commodity, contract);
			udb.closeDB();
			return result;
			
		} catch (Exception e) {
			ClientError.reportError(e, "db:"+db+",commodity:"+commodity);
			throw e;
		}

	}
	
	
	/**
	 * 
	 * @param dobj
	 * @param db
	 * @param tablename
	 * @param commodity
	 * @return
	 * @throws Exception
	 */
	public Vector getCommodityRawData(Map dobj,String db, String tablename, String commodity) throws Exception {
		try{
			UtilDB udb=new SuperDB().getUtilDB(db);
			udb.connectDB(db);
			
			String datequery = ConstructQueryDB.getConstructQueryDB().constructDateInputQuery(dobj);
			
			Vector result=udb.getRawData(datequery,udb.connection(), tablename, commodity);
			udb.closeDB();
			return result;
			
		} catch (Exception e) {
			ClientError.reportError(e, "db:"+db+",commodity:"+commodity);
			throw e;
		}
	}
	
	/**
	 * 
	 * @param dobj
	 * @param db
	 * @param fieldname
	 * @param commodity
	 * @return
	 * @throws Exception
	 */
	public Map getCommodityRawData2(Map dobj,String db, String fieldname, String commodity) throws Exception {
		try{
			UtilDB udb=new SuperDB().getUtilDB(db);
			udb.connectDB(db);
			
			String datequery = ConstructQueryDB.getConstructQueryDB().constructDateInputQuery(dobj);
			
			//Vector result=udb.getRawData2(datequery,udb.connection(), fieldname, commodity);
			Map result=udb.getRawData3(datequery,udb.connection(), fieldname, commodity);		
			udb.closeDB();
			return result;
			
		} catch (Exception e) {
			ClientError.reportError(e, "db:"+db+",commodity:"+commodity);
			throw e;
		}
	}
	
	/**
	 * 
	 * @param dobj
	 * @param db
	 * @param fieldname
	 * @param contract
	 * @param commodity
	 * @return
	 * @throws Exception
	 */
	public Map getCommodityRawData2Contrat(Map dobj,String db, String fieldname, String contract,String commodity) throws Exception {
		try{
			UtilDB udb=new SuperDB().getUtilDB(db);
			udb.connectDB(db);
			
			String datequery = ConstructQueryDB.getConstructQueryDB().constructDateInputQuery(dobj);
			
			//Vector result=udb.getRawData2(datequery,udb.connection(), fieldname, commodity);
			Map result=udb.getRawData2Contract(datequery,udb.connection(), fieldname, contract, commodity);
		
			udb.closeDB();
			return result;
			
		} catch (Exception e) {
			ClientError.reportError(e, "db:"+db+",commodity:"+commodity);
			throw e;
		}
	}
	
	/**
	 * 
	 * @param db
	 * @param tablename
	 * @param date
	 * @param val
	 * @param sval
	 * @param cont
	 * @return
	 * @throws Exception
	 */
	public boolean addRawData(String db,String tablename,String date,String val, String sval, String cont ) throws Exception {
		try{
		
			Date d=new SimpleDateFormat("dd/MM/yyyy").parse(date);
			Strategy contract=new Strategy();
			
			contract.setCdate(d);			
			contract.setName(cont);
			
			ContractDB cdb=new SuperDB().getContractDB(tablename,db);
			cdb.connectDB(db);
			
			if(cdb.checkSValueFieldTypeExist(cdb.connection()) && sval!=null && !sval.equals("") ){
				contract.setSvalue(sval);
				Vector<Strategy> records=new Vector();
				records.add(contract);
				cdb.addSValRecords(cdb.connection(), records);
			}else{
				contract.setValue(Double.parseDouble(val));
				Vector<Contract> records=new Vector();
				records.add(contract);
				cdb.addRecords(cdb.connection(), records);
			}
			
			cdb.closeDB();
			return true;
		
		}catch(Exception e){
			ClientError.reportError(e, null);
			throw e;
		}
		
	}
	
	
	/**
	 * 
	 * @param db
	 * @param tablename
	 * @return
	 * @throws Exception
	 */
	public int checkTableDataType(String db,String tablename) throws Exception {
		ContractDB cdb=new SuperDB().getContractDB(tablename,db);
		try{			
			cdb.connectDB(db);
            boolean issval=cdb.checkSValueFieldTypeExist(cdb.connection());
            int rtn=0;
            if(issval){            	
            	if(cdb.checkSValueField(cdb.connection())>0){
            		rtn=3; //database set to accepts numeric;
            	}else{
            		rtn=2; //database set to accepts sval
            	}
            	//override settings to acept both if the database is empty and sval field exist in the table. 
            	if(cdb.countRecords(cdb.connection())<1){
            		rtn=0;
            	}     	
            }else{
            	rtn=1; //database accepts only numeric  
            }

			return rtn;
		
		}catch(Exception e){
			ClientError.reportError(e, null);
			throw e;
		}finally{
			cdb.closeDB();
		}
	}
	
	
	/**
	 * 
	 * @param db
	 * @param tablename
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public boolean deleteRawData(String db,String tablename, Map data) throws Exception {
		UtilDB udb=new SuperDB().getUtilDB(db);
		try{
			log.debug("deleteRawData called, db:"+db+" tablename:"+tablename+ " data:"+data);
			
			udb.connectDB(db);
			if(data.get("contract")!=null && data.get("cdate1")!=null){
				udb.deleteRawData(udb.connection(), tablename, data);
				log.debug("delete method in utildb exec uted");
			}
			//udb.closeDB();
			return true;
			
		} catch (Exception e) {
			ClientError.reportError(e, null);
			throw e;
		}finally{
			udb.closeDB();
		}
	}

	/**
	 * 
	 * @param db
	 * @param tablename
	 * @param olddata
	 * @param newdata
	 * @return
	 * @throws Exception
	 */
	public boolean setUpdateRawData(String db,String tablename, Map olddata, Map newdata) throws Exception {
		UtilDB udb=new SuperDB().getUtilDB(db);
		try{
			log.debug("setUpdateRawData called, db:"+db+" tablename:"+tablename+ " old:"+olddata+" new:"+newdata);
			
			udb.connectDB(db);
			if(olddata.get("contract")!=null && olddata.get("cdate1")!=null){
				udb.updateRawData(udb.connection(), tablename, olddata, newdata);
				log.debug("update method in utildb exec uted");
			}
			
			return true;
			
		} catch (Exception e) {
			ClientError.reportError(e, null);
			throw e;
		}finally{
			udb.closeDB();
		}
	}
	
	/**
	 * 
	 * @param db
	 * @param commodity
	 * @return
	 * @throws Exception
	 */
	public boolean removCommodity(String db, String commodity) throws Exception {
		try{
			UtilDB udb=new SuperDB().getUtilDB(db);
			udb.connectDB(db);
			boolean result=udb.removeCommodityAndFields(udb.connection(), commodity);
			udb.closeDB();
			return result;
			
		} catch (Exception e) {
			ClientError.reportError(e, "db:"+db+",commodity:"+commodity);
			throw e;
		}
	}
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	private String getLoggedInUser() throws Exception{
		return (String)getRequest().getSession().getAttribute(AdminMgmt.LOGGED_IN_USERNAME);
	}
	
	
	/**
	 * 
	 * @param db
	 * @return
	 * @throws Exception
	 */
	public Map getNodeInfo(String db) throws Exception {
		TreeMap rtn=new TreeMap();
		
		UtilDB udb=new SuperDB().getUtilDB(db);

		
		udb.connectDB(db);		
		Map mar_c=udb.listAllCommoditiesAndFields(udb.connection());
		if(mar_c.size()<=5000){
			//avoid out of memory error
			List mar_c_error=udb.listAllOrphanedAssets(udb.connection());
			rtn.put(db+"_err", mar_c_error);
		}
		
		rtn.put(db, mar_c);
		udb.closeDB();
		return rtn;
		
	}
	
	
	private HttpServletRequest getRequest() throws Exception  {
		return (request == null && WebContextFactory.get()!=null) ? WebContextFactory.get().getHttpServletRequest() : request;

	}
	

}



