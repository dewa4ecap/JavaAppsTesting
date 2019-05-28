package com.fourelementscapital.db;
import java.sql.*;
import java.util.ArrayList;
import java.util.ResourceBundle;

import com.fourelementscapital.controller.ThemeEmailsParser;  


public class ThemeEmailsDB{
	
	public ArrayList<String> getThemeEmailsFromDB(String lcParam){
		String strParam="";  
		ArrayList<String> lcReturn = new ArrayList<String>();
		
		ResourceBundle mybundle = ResourceBundle.getBundle("config");

		
		String lcHost = mybundle.getString("theme.host");
		String lcPort = mybundle.getString("theme.port");
		String lcDb   = mybundle.getString("theme.db");
		String lcUser = mybundle.getString("theme.userdb");
		String lcPass = mybundle.getString("theme.passdb");
		
		try{
		
		Class.forName("com.mysql.jdbc.Driver");  

		//here 4EwikiDB1 is database name, 4ecremoteuser is username and 4ecrmt2011 is password  
		Connection con=DriverManager.getConnection("jdbc:mysql://"+lcHost+":"+lcPort+"/"+lcDb,lcUser,lcPass);  
		
		Statement stmt=con.createStatement();  

		String lcCmd =" SELECT CAST( old_text AS CHAR( 50000 ) ";
	       		lcCmd = lcCmd + " CHARACTER SET utf8 ) AS txt FROM text WHERE old_id = ( SELECT r.rev_text_id ";
	       		lcCmd = lcCmd + " FROM revision AS r, page AS p WHERE p.page_id = r.rev_page ";
	       		lcCmd = lcCmd + " AND p.page_title = 'Theme_Emails' ORDER BY r.rev_id DESC LIMIT 1 ); ";
	 
		ResultSet rs=stmt.executeQuery(lcCmd);  
		while(rs.next())  
		{
			strParam = rs.getString("txt");		
	    }
		
		con.close();  

	}catch(Exception e){ System.out.println(e);}  
		
		ThemeEmailsParser tep = new ThemeEmailsParser();
		lcReturn = tep.parsingThemeEmails(strParam, lcParam);
		
		return lcReturn;
		
	}
	
	
	
}