<%@page import="com.fe.common.Constant,com.fourelementscapital.scheduler.config.Config,java.util.*"%>
<%
String ky=(String)session.getAttribute(Constant.SESSION_LOGGED_USER);
if(ky==null || (ky!=null && ky.equals(""))){
 
	String referer=(request.getServletPath()!=null)?request.getServletPath():"";
	
	
%>
	<jsp:forward page="/login.jsp">
		<jsp:param name="referer" value="<%=referer%>" /> 
	</jsp:forward>
<%}%>


<html>
<head>
<title>Execute R</title>

<style>

body {
	font-family:Arial, Verdana, Geneva, Helvetica, sans-serif;
	font-size:medium;
 
}	

</style>
 
 
 

 

 
<script language="JavaScript" type="text/javascript" src="jquery-1.8.2/js/jquery-1.4.2.min.js"></script>
<script>
     var $j = jQuery.noConflict();	 
</script>

 
 
</head>
<body>
<%@ include file="include/user_menu.inc" %>

<iframe src="http://localhost:9090/bldb/executeR.jsp" style="width:100%;height:95%;border:0px;"></iframe>
 
 
<%@ include file="include/login.html" %>
</body>
</html>

