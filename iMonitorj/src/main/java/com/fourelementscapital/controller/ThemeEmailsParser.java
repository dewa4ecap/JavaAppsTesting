package com.fourelementscapital.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class ThemeEmailsParser{
	
	
	public ArrayList<String> parsingThemeEmails(String lcTextTheme, String paramTheme){
		
		ArrayList<String> lcReturnValue = new ArrayList<String>();
		
		String tagTheme="";
		String emailTheme="";
		String emailThemes="";
		String passTheme="";
		String passThemes="";
		String fromNameTheme="";
		String fromNameThemes="";
		String userName="";
		
		String splitStr1="|";
		String splitStr2=" ";
		String strSearch="";
		strSearch="| " + paramTheme;
		
		ResourceBundle mybundle = ResourceBundle.getBundle("config");
		
		String defaultTagTheme=mybundle.getString("email.sender.name");
		String defaultEmailTheme=mybundle.getString("email.sender");
		String defaultUserName=mybundle.getString("email.username");
		String defaultpassTheme=mybundle.getString("email.password");
		String defaultFromNameTheme=mybundle.getString("email.sender.name");
		
		BufferedReader br = new BufferedReader(new StringReader(lcTextTheme));

		String line="";

		try {
			while ((line = br.readLine()) != null) {
				
				if (line.equals(strSearch)){

					tagTheme = line.replace(splitStr1,splitStr2).trim();
					
					emailTheme = br.readLine();
					emailThemes = emailTheme.replace(splitStr1,splitStr2).trim();

					passTheme = br.readLine();
					passThemes = passTheme.replace(splitStr1,splitStr2).trim();
					 
					fromNameTheme = br.readLine();
					fromNameThemes = fromNameTheme.replace(splitStr1,splitStr2).trim();
				 }	
			}
			
				//for condition if emailTheme is empty or Tag Theme is empty use default account iMonitorj
				if(emailThemes.equals("&nbsp;") || tagTheme.equals(""))
				{
					tagTheme = defaultTagTheme;
					//System.out.println(tagTheme);
					emailThemes= defaultEmailTheme;
					//System.out.println(defaultEmailTheme);
					passThemes = defaultpassTheme;
					//System.out.println(passThemes);
					fromNameThemes = defaultFromNameTheme;
				    //System.out.println(fromNameThemes);  
					userName = defaultUserName;
				}else{
					userName = emailThemes;
				}
				
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
				try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
		}
	
		 lcReturnValue.add(tagTheme);
		 lcReturnValue.add(emailThemes);
		 lcReturnValue.add(passThemes);
		 lcReturnValue.add(fromNameThemes);
		 lcReturnValue.add(userName);
		 
		 return lcReturnValue;
		
	}
}