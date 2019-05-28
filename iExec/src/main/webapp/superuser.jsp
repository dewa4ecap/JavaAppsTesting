<%@page import="java.util.Map"%>
<%@page import="com.fourelementscapital.client.LoginMgmt"%>
<%@page import="com.fourelementscapital.config.Constant"%>
<%@ page isErrorPage="true" import="java.io.*" %>

<%
boolean meta_redirection=false;
String action=request.getParameter("action");
boolean logoutaction=false;
String referer=null;
String loginstyle="";
String delaymessage="";
int delayseconds=0;
if(action!=null && action.equalsIgnoreCase("logout")){
	logoutaction=true;
	session.removeAttribute(Constant.SESSION_LOGGED_SUPERUSER);
	//session.invalidate();
	//session=request.getSession(true);
	meta_redirection=true;
	referer=(request.getParameter("referer")!=null)?request.getParameter("referer"):"index.jsp";
	loginstyle="display:none;";
	delaymessage="Please wait while signing out Superuser...";
	delayseconds=1;
}


String ky=(String)session.getAttribute(Constant.SESSION_LOGGED_SUPERUSER);


if(ky!=null && !ky.equals("")){	
	referer=(request.getParameter("referer")!=null)?request.getParameter("referer"):"/index.jsp";
	//System.out.println("login.jsp: servletpath:"+referer);
	if(referer.startsWith("http://") || referer.startsWith("https://")){
		meta_redirection=true;
	}else{
       %><jsp:forward page="<%=referer%>"></jsp:forward><%
    }
}

%>



<html>
<head>
  <%
    if(meta_redirection){
    	%><meta http-equiv="refresh" content="<%=delayseconds %>;url=<%=referer%>"> <%
    }
  %>
  <title>Super User Access: Four Elements</title>
	<style>
		
		body {
			font-family:Arial, Verdana, Geneva, Helvetica, sans-serif;
			font-size:medium;
		 
		}	
		
	</style>
	
	<link rel="stylesheet" type="text/css" href="jquery-1.8.2/css/cupertino/jquery-ui-1.8.2.custom.css" />
	<link rel="stylesheet" type="text/css" href="css/login.css" />

	<script type='text/javascript' src='dwr/engine.js'></script>
	<script type='text/javascript' src='dwr/util.js'></script>
	<script type='text/javascript' src='dwr/interface/LoginMgmt.js'></script>
	
	
	<script language="JavaScript" type="text/javascript" src="jquery-1.8.2/js/jquery-1.7.1.min.js"></script>
	<script>
	     var $j = jQuery.noConflict();	 
	</script>
	<script language="JavaScript" type="text/javascript" src="jquery-1.8.2/cookie/jquery.cookie.js"></script>
	<script language="JavaScript" type="text/javascript" src="jquery-1.8.2/js/jquery-ui-1.8.2.custom.min.js"></script>
	<script language="JavaScript" type="text/javascript" src="js/superuser.js"></script>
	<script language="JavaScript">
	   <%
	     String ref=request.getParameter("referer");
	     if(ref!=null && (ref.startsWith("http://") || ref.startsWith("https://")) ){
	    	 out.println("su.referer='"+ref+"';");
	     }else{
	         out.println("su.referer='"+((ref!=null)?"/iexec"+ref:"")+"';");
	     }
	     
	   %>
	 </script>
	
	
</head>
<body>


<% if(delaymessage!=null && !delaymessage.equals("")){%>
	<div class="delayMessage">
		<%=delaymessage%>
	</div>
<%} %>

<div id="userloginForm" style="<%=loginstyle%>">
	<form name="userlogin" method="post" action="#" onsubmit="return su.login();">
		<h2>Super User Access</h2>
		<p id="userloginlink"></p>
			<table width="99%">
			<tr>
				<td class="mw-label"><label for='wpName1'>Username:</label></td>
				<td class="mw-input">
					<input type='text' class='loginText' name="wpName" id="wpName1"	tabindex="1" value="" size='20' />
				</td>
			</tr>
			<tr>
				<td class="mw-label" id='wpOldPassword'>Password:</td>
				<td class="mw-input">
					<input type='password' class='loginPassword' name="wpPassword" id="wpPassword1"	tabindex="2"value="" size='20' />
				</td>
			</tr>
			
			<tr class='mw-changepwd' style='display:none'>
				<td class="mw-label"><label for='wpPassword1'>New Password:</label></td>
				<td class="mw-input">
					<input type='password' class='loginPassword' name="wpNewPassword" id="wpNewPassword"	tabindex="2"value="" size='20' />
				</td>
			</tr>
			
			<tr>
			 <td class="mw-status-tr" colspan="2"><div class="mw-status"></div></td>
			</tr>
			<tr>
				<td></td>
				<td class="mw-submit">
					<input type='submit' name="wpLoginattempt" id="wpLoginattempt" tabindex="5" value="Log in" />					
				</td>
			</tr>
			<tr>
			 <td colspan="2" align="left"><input type="checkbox" id='wpChangePwdCkbox' value="changepwd" value="1" onclick="su.changepass(this);">Tick here to change password </td>
			</tr>
		</table>
	</form>
</div>

 

</body>
</html>