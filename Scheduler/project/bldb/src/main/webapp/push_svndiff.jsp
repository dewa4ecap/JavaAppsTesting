<%@ page contentType="text/html; charset=utf-8" language="java" import="com.fe.svn.*" errorPage="" %>
<%
	try{
		SVNDiffPush diff=new SVNDiffPush(request,response);
		diff.pushDiffWC();
	}catch(Exception e){
		e.printStackTrace();
	}

%>