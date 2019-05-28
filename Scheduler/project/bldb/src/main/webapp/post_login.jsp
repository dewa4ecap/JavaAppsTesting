<%@page import="com.fe.common.Constant"%>
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
<title>Authenticated User</title>


<style>
body {
	font-family:Arial, Verdana, Geneva, Helvetica, sans-serif;
	font-size:medium;
 
}
</style>

<link rel="stylesheet" type="text/css" href="css/wiki_main.css" />
<link rel="stylesheet" type="text/css" href="css/iexec.css" />


<script type='text/javascript' src='dwr/engine.js'></script>
<script type='text/javascript' src='dwr/util.js'></script>
<script type='text/javascript' src='dwr/interface/IExecMgmt.js'></script>


<script language="JavaScript" type="text/javascript" src="jquery-1.8.2/js/jquery-1.4.2.min.js"></script>
<script>
     var $j = jQuery.noConflict();	 
</script>


 
 
</head>
<body>
                   <div style="margin: 50px 30px;border: 10px solid #e0e0e0;padding: 30px;text-align: center;font-size: 1.3em;background-color: #F8F8F8;box-shadow: 0 2px 40px 0 rgba(0, 0, 0, 0.09);">
                   			<span>You have successfully logged into the system, start using chrome extension.</span>                   
                   </div>
</body>
</html>

