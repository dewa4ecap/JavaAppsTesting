/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.common;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fourelementscapital.config.Config;

@SuppressWarnings("unchecked")
public class SchedulerReportClient {
	private static String REPORTS_FOLDER="scheduler.report.folder";
	private Logger log = LogManager.getLogger(SchedulerReportClient.class.getName());
	
	private ArrayList<String> getFileNames() {
		String folder=Config.getString(REPORTS_FOLDER);
		ArrayList rtn=new ArrayList();
		log.debug("folder:"+folder);
		if(folder!=null && new File(folder).exists()){
			File[] list=new File(folder).listFiles();
			for(int i=0;i<list.length;i++){
				rtn.add(list[i].getName());
				log.debug("adding filename:"+list[i].getName());
			}
		}
		Collections.sort(rtn );
		return rtn;
	}

	public String generateMarkup(){
		ArrayList<String> files=getFileNames();
		String ul="";
		String div="";
		String folder=Config.getString(REPORTS_FOLDER);
		int count=0;
		for(String filename: files){
			String filepart=filename.substring(0,filename.indexOf("."));
			String ext=filename.substring(filename.indexOf(".")+1);
			log.debug("filepart:"+filepart+" ext:"+ext);
			if(ext.equalsIgnoreCase("png") || ext.equalsIgnoreCase("html") || ext.equalsIgnoreCase("htm")){
				String id="scd_monitors-tabs-"+count;
				String img_id="scd_monitors-tabs-img"+count;
				ul+=(ul.equals(""))?"<ul>":"";
				ul+="<li><a href=\"#"+id+"\">"+filepart+"</a></li>";
				if(ext.equalsIgnoreCase("png") ){
					div+="<div class=\"chromeScroll\" id=\""+id+"\"><img fileparam=\""+folder+filename+"\" src=\"image.jsp?file="+folder+filename+"&t="+new Date().getTime()+"\"  id=\""+img_id+"\"></div>";				    
				}else if(ext.equalsIgnoreCase("html") || ext.equalsIgnoreCase("htm")){
					div+="<div  class=\"chromeScroll\" id=\""+id+"\" style=\"overflow:hidden\">";
					div+="<iframe fileparam=\""+folder+filename+"\" class=\"chromeScroll\" src=\"html.jsp?file="+folder+filename+"&t="+new Date().getTime()+"\" width=\"100%\" height=\"100%\"  scrolling=\"yes\" style=\"border:0px\">";
					div+="<p>Your browser does not support iframes.</p>";
					div+="</iframe>";				
					div+="</div>";
				}
			}
			count++;
		}
		ul+=(ul.equals(""))?"":"</ul>";
		return ul+div;
	}
}