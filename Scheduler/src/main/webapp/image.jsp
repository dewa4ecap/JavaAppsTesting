<%@ page contentType="text/html; charset=utf-8" language="java" import="com.fourelementscapital.util.*" errorPage="" %>
<%@page import="com.fourelementscapital.scheduler.util.ImageLoader"%>
<%
	try{
		ImageLoader img=new ImageLoader(request,response);
		img.pushImage();
	}catch(Exception e){
		//e.printStackTrace();
		//System.out.println("~~~~ Error while loading image");
	}

%>