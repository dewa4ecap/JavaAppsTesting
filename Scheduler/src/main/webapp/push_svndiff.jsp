<%@ page contentType="text/html; charset=utf-8" language="java" import="com.fourelementscapital.svn.*" errorPage="" %>
<%@page import="com.fourelementscapital.scheduler.svn.SVNDiffPush"%>
<%
	try{
		SVNDiffPush diff=new SVNDiffPush(request,response);
		diff.pushDiffWC();
	}catch(Exception e){
		e.printStackTrace();
	}

%>