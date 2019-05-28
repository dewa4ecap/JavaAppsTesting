

<link rel="stylesheet" type="text/css" href="css/main.css" />
<script type='text/javascript' src='dwr/interface/MenuMgmt.js'></script>
<script language="JavaScript" type="text/javascript" src="js/menu.js"></script>

<%@page import="com.fourelementscapital.scheduler.common.MenuMgmt"%>
<%@page import="java.util.Map"%>
<%

  MenuMgmt mgmt=new MenuMgmt(request);
  Map<String,String> pref=mgmt.getUserPref();
  String ace_editor_checked="";
  String reditor_admin_checked="";

	String users=Config.getString("superuser.enable");
	
	
	String loggeduser=(String)session.getAttribute(com.fourelementscapital.config.Constant.SESSION_LOGGED_USER);
	boolean senabled=Arrays.asList(users.toLowerCase().split(",")).contains(loggeduser.toLowerCase());

%>
<% if(pref.get("beta_ace_editor")!=null && pref.get("beta_ace_editor").equalsIgnoreCase("true")){ 
	ace_editor_checked=" checked ";
   %>
<script language="JavaScript">CODE_EDITOR=CODE_ACE_EDITOR;</script>
<%}%>
<% if(pref.get("beta_reditor_admin")!=null && pref.get("beta_reditor_admin").equalsIgnoreCase("true")){ 
	reditor_admin_checked=" checked ";
   %>
   <script language="JavaScript">REDITOR_ADMIN=true;</script>
<%}%>


<div class="fe-mainMenu">
	<ul>
	   <li><a href="login.jsp?action=logout">Logout</a>  [ <a href='#' id='user_profile_a' onclick='menu_profiletoggle(this);return false;'><%=session.getAttribute(com.fourelementscapital.config.Constant.SESSION_LOGGED_USER)%><img src='images/button_plus.gif' border='0' height='14px' style='margin-left:2px' class='btn'><img class='btn' src='images/button_minus.gif' style='display:none;margin-left:2px' border='0' height='14px'></a> ]</li>
	   
	   <%if(senabled){%>
	   <li> <%   
				String user=(String)session.getAttribute(com.fourelementscapital.config.Constant.SESSION_LOGGED_SUPERUSER);
	   	        if(user!=null && !user.equals("")) {
	        %>
	     	    <a href="superuser.jsp?action=logout&referer=<%=request.getRequestURL()%>">Exit Superuser </a>
	        <%   } else {%>
	   	       
	   	       <a href="superuser.jsp?referer=<%=request.getRequestURL()%>">Login Superuser</a> 
	   	    <% } %>
	   	</li>
	   	<%}%>
		<li><span style="font-size:12px"><a href="RELEASE-NOTES.txt" target="_blank">V.3.1.0-Beta1</a></span></li>
	</ul>
    <div id='user_profile' class='user_profile' style='display:none;'>
    	<div style='padding:20px;display:block;'>
    		<div style='background-color:#D9ECFF;padding:3px;font-weight:bold;padding-left:10px;'>Beta Features</div>
    		<div style='border:1px solid #D9ECFF;padding:10px;border-radius: 0px 0px 15px 15px;'>       
    		        <div><input type='checkbox' pref_item='beta_ace_editor' value='' <%=ace_editor_checked%> id='ace_editor_ckbox' onclick='menu_preference(this);'><label for='ace_editor_ckbox'>Use ACE Editor</label></div>    		        
    		</div>
    	
    	<div style='background-color:#D9ECFF;padding:3px;font-weight:bold;padding-left:10px;margin-top:20px'>User Preference</div>
    		<div style='border:1px solid #D9ECFF;padding:10px;border-radius: 0px 0px 15px 15px;'>     
    		        <div><input type='checkbox' pref_item='beta_reditor_admin' value='' <%=reditor_admin_checked%> id='beta_reditor_ckbox' onclick='menu_preference(this);'><label for='beta_reditor_ckbox'>Enable Drag to Move RFunctions <small>(RFunction will load very slow)</small></label></div>
    		</div>
    		
    	</div>
    </div>

</div>
<script type="text/javascript">

$j(function(){
	
	
	<% String path=(request.getServletPath()!=null)?request.getServletPath():""; out.println("var path='"+path+"';");%>

    var name = ".fe-mainMenu";  
    var menuYloc = null; 
      
    $j(document).ready(function(){  
        menuYloc = parseInt($j(name).css("top").substring(0,$j(name).css("top").indexOf("px")))  
        $j(window).scroll(function () {  
            var offset = menuYloc+$j(document).scrollTop()+"px";  
            $j(name).css("top",offset);
            //$j(name).animate({top:offset},{duration:0,queue:false});  
        });  
    });
    
    $j(name+" ul li a").each(function(idx,el){
    	var d=$j(el).attr("href");     
    	if(path!=null && path.split("/")[path.split("/").length-1]==d){
    		$j(el).addClass("fe-current");
    		//$j(el).attr("#");
    		$j(el).attr("href","#");
    		$j(el).click(function(){return false;})

    		
    	}
    });
      
});
    
</script>

