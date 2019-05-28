<%@page import="com.fourelementscapital.config.Config"%>
<%@page import="java.util.Map"%>
<%@page import="com.fourelementscapital.client.LoginMgmt"%>
<%@page import="com.fourelementscapital.config.Constant"%>

<%@page import="com.fourelementscapital.sso.SSO"%>
<%@page import="java.net.InetAddress"%>

<%@ page isErrorPage="true" import="java.io.*" %>

<%
String action=request.getParameter("action");
boolean logoutaction=false;

if(action!=null && action.equalsIgnoreCase("logout")){
	logoutaction=true;
	
	Cookie cookie[]=request.getCookies();
	if(cookie!=null){
		String remUser = null;
		String remSid = null;
		
		for(int i=0;i<cookie.length;i++){
			if(cookie[i].getName().equals("ALUSER") && cookie[i].getValue()!=null) {
				remUser = cookie[i].getValue();			
			}
			if(cookie[i].getName().equals("ALSID") && cookie[i].getValue()!=null) {
				remSid = cookie[i].getValue();
			}
		}
		
		SSO sso = new SSO();
		
		sso.invalidateSession(remSid, remUser);
	}
	
	
	session.removeAttribute(Constant.SESSION_LOGGED_USER);
	session.invalidate();
	session=request.getSession(true);
	
	Cookie killMyCookie = new Cookie("4eprevuser", null);
    killMyCookie.setMaxAge(-1);
    killMyCookie.setPath("/");
    killMyCookie.setDomain(".alphien.com");
    response.addCookie(killMyCookie);
     
	Cookie killMyCookie2 = new Cookie("4esessionuid", null);
    killMyCookie2.setMaxAge(-1);
    killMyCookie2.setPath("/");
    killMyCookie2.setDomain(".alphien.com");
    response.addCookie(killMyCookie2);
    
    Cookie alSid = new Cookie("ALSID", null);
    alSid.setPath("/");
    alSid.setDomain(".alphien.com");
    alSid.setMaxAge(0);
    response.addCookie(alSid);
     
	Cookie alUser = new Cookie("ALUSER", null);
	alUser.setPath("/");
	alUser.setDomain(".alphien.com");
    alUser.setMaxAge(0);     		
    response.addCookie(alUser);
	
}


String ky=(String)session.getAttribute(Constant.SESSION_LOGGED_USER);


if((ky==null || (ky!=null && ky.equals(""))) && !logoutaction){	
	
	
	
	String user=null;
	String pwd=null;
	Cookie cookie[]=request.getCookies();
	if(cookie!=null){
		for(int i=0;i<cookie.length;i++){
			if(cookie[i].getName().equals("4eprevuser") && cookie[i].getValue()!=null) {
				user=cookie[i].getValue();			
			}
			if(cookie[i].getName().equals("4esessionuid") && cookie[i].getValue()!=null) {
				pwd=cookie[i].getValue();
			}
		}
	}
	
	//System.out.println("login.jsp:ky not found: prev user:"+user+" pwd:"+pwd);
	if(Config.getString(Config.CONFIG_IGNORE_AUTH)!=null && Config.getString(Config.CONFIG_IGNORE_AUTH).equalsIgnoreCase("true") ) {
		 session.setAttribute(Constant.SESSION_LOGGED_USER,Config.USER_ANONYMOUS);	
	}else{
		if(user!=null && pwd!=null && !user.equals("") && !pwd.equals("")){
			LoginMgmt lm=new LoginMgmt(request);
			Map data=lm.validateRememberedUser(user,pwd);
			boolean success=(Boolean)data.get("loggedin");
			if(success){		 
	
				Cookie killMyCookie = new Cookie("4eprevuser", user);
		    	killMyCookie.setMaxAge(60*60*24*30);    //30 days
		    	killMyCookie.setPath("/");
		    	killMyCookie.setDomain(".alphien.com");
		    	response.addCookie(killMyCookie);
		     
				Cookie killMyCookie2 = new Cookie("4esessionuid", pwd);
				killMyCookie2.setMaxAge(60*60*24*30);    //30 days
				killMyCookie2.setPath("/");
				killMyCookie2.setDomain(".alphien.com");
		    	response.addCookie(killMyCookie2);
				
			}
		}
	}
	
}

ky=(String)session.getAttribute(Constant.SESSION_LOGGED_USER);	

boolean meta_redirection=false;
String referer=null;
if(ky!=null && !ky.equals("")){
	
	referer=(request.getParameter("referer")!=null)?request.getParameter("referer"):"/" + "scheduler.jsp";
	
	if(referer.startsWith("http://") || referer.startsWith("https://")){ 
		meta_redirection=true;
	}else{
       %> 
       		<jsp:forward page="<%=(request.getHeader(\"referer\") == null ? \"StrategyLaunchpad\" : request.getHeader(\"referer\") )%>"></jsp:forward>
<%
    }
}

%>
<html>
<head>
  <%
    if(meta_redirection){
    	%><meta http-equiv="refresh" content="0;url=<%=request.getHeader("referer") == null ? "StrategyLaunchpad" : request.getHeader("referer")%>"> <%
    }
  %>
  <title>Login: Alphien</title>
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
	
	<script type='text/javascript' src='dwr/interface/SSO.js'></script>
	
	<script language="JavaScript" type="text/javascript" src="jquery-1.8.2/js/jquery-1.7.1.min.js"></script>
	<script>
	     var $j = jQuery.noConflict();	 
	</script>
	<script language="JavaScript" type="text/javascript" src="jquery-1.8.2/cookie/jquery.cookie.js"></script>
	<script language="JavaScript" type="text/javascript" src="jquery-1.8.2/js/jquery-ui-1.8.2.custom.min.js"></script>
	<script language="JavaScript">
	   <%
	     String ref=request.getParameter("referer");
	     if(ref!=null && (ref.startsWith("http://") || ref.startsWith("https://")) ){
	    	 out.println("var referer='"+ref+"';");
	     }else{
	         out.println("var referer='"+((ref!=null)?"/scheduler"+ref:"")+"';");
	     }
	   %>
	 </script>
	 
	<script language="JavaScript" type="text/javascript" src="js/login.js"></script>
</head>
<body>
<div id="userloginForm">
       <form name="userlogin" method="post" action="#" onsubmit="return login('<%=request.getHeader("referer") == null ? "StrategyLaunchpad" : 
               request.getHeader("referer").contains("beta.alphien") ? "StrategyLaunchpad" :
               request.getHeader("referer")%>');">

		<h2>User Authentication</h2>
		<p id="userloginlink"></p>
			<div id="userloginprompt">
				<p>Use your Alphien Wiki username and password</p>
			</div>
			<table width="99%">
			<tr>
				<td class="mw-label"><label for='wpName1'>Username:</label></td>
				<td class="mw-input">
					<input type='text' class='loginText' name="wpName" id="wpName1"	tabindex="1" value="" size='20' />
				</td>
			</tr>
			<tr>
				<td class="mw-label"><label for='wpPassword1'>Password:</label></td>
				<td class="mw-input">
					<input type='password' class='loginPassword' name="wpPassword" id="wpPassword1"	tabindex="2"value="" size='20' />
				</td>
			</tr>
			<tr>
				<td></td>
				<td class="mw-input">
					<input type='checkbox' name="wpRemember" tabindex="4" value="1" id="wpRemember"/> <label for="wpRemember">Remember me and login automatically on this computer</label>
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
				<td colspan="2" class="mw-rememberpwd">
					Forgot the password? Goto <a target="new" href="https://wiki.alphien.com/alphienwiki/index.php?title=Special:UserLogin">Wiki to retrieve</a>		
				</td>
			</tr>
		</table>
	</form>
</div>
</body>
</html>