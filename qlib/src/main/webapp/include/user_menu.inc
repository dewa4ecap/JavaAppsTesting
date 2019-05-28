

<link rel="stylesheet" type="text/css" href="css/main.css" />
<link rel="stylesheet" type="text/css" href="css/display_view.css" />
<!--Intan start-->
<style>
.modal-package {
display: none; /* Hidden by default */
position: fixed; /* Stay in place */
z-index: 1; /* Sit on top */
padding-top: 100px; /* Location of the box */
padding-bottom: 100px; /* Location of the box */
left: 0;
top: 0;
width: 100%; /* Full width */
height: 100%; /* Full height */
overflow: auto; /* Enable scroll if needed */
background-color: rgb(0,0,0); /* Fallback color */
background-color: rgba(0,0,0,0.4); /* Black w/ opacity */
}

/* Modal Content */
.modal-content-package {
background-color: #fefefe;
margin: auto;
padding: 20px;
border: 1px solid #888;
width: 70%;
height: 100%;
overflow: auto; /* Enable scroll if needed */
}

/* The Close Button */
.close-package {
color: #aaaaaa;
float: right;
font-size: 28px;
font-weight: bold;
}

.close-package:hover,
.close-package:focus {
color: #000;
text-decoration: none;
cursor: pointer;
}

.modal {
display: none; /* Hidden by default */
position: fixed; /* Stay in place */
z-index: 1; /* Sit on top */
padding-top: 100px; /* Location of the box */
padding-bottom: 100px; /* Location of the box */
left: 0;
top: 0;
width: 100%; /* Full width */
height: 80%; /* Full height */
overflow: auto; /* Enable scroll if needed */
background-color: rgb(0,0,0); /* Fallback color */
background-color: rgba(0,0,0,0.4); /* Black w/ opacity */
}

/* Modal Content */
.modal-content {
background-color: #fefefe;
margin: auto;
padding-top: 20px;
padding-bottom: 20px;
border: 1px solid #888;
width: 100%;
height: 100%;
}

/* The Close Button */
.close {
color: #aaaaaa;
float: right;
font-size: 28px;
font-weight: bold;
}

.close:hover,
.close:focus {
color: #000;
text-decoration: none;
cursor: pointer;
}
</style>
<!--Intan finish-->
<script type='text/javascript' src='dwr/interface/MenuMgmt.js'></script>
<script language="JavaScript" type="text/javascript" src="js/menu.js"></script>
<!--Intan start-->
<!-- The Modal -->

<div id="packageModal" class="modal-package">

  <!-- Modal content -->

  <div class="modal-content-package">
    <span class="close-package">&times;</span>
    <p id="modalContent"></p>
  </div>

</div>
<!--Intan finish-->
<!--Intan start -->
<script> // Get the modal 
    function packageModal(){ var packagemodal = document.getElementById("packageModal"); 
    // Get the button that opens the modal 
    var packagebtn = document.getElementById("change-version"); 
    // Get the <span> element that closes the modal 
    var packagespan = document.getElementsByClassName("close-package")[0]; 
    var vFile = new XMLHttpRequest(); 
    vFile.open("GET", "NEWS", false); 
    vFile.onreadystatechange = function () { 
        if(vFile.readyState === 4) { 
            if(vFile.status === 200 || vFile.status == 0) { 
                var allText = vFile.responseText; 
                var customTextElement = document.getElementById('modalContent'); 
                customTextElement.innerHTML = "<pre>"+allText+"</pre>";
        }
    }
}

vFile.send(null);
packagemodal.style.display = "block";

// When the user clicks on <span> (x), close the modal
packagespan.onclick = function() {
packagemodal.style.display = "none";
}

// When the user clicks anywhere outside of the modal, close it
window.onclick = function(event) {
if (event.target == packagemodal) {
packagemodal.style.display = "none";
}
}
}
</script>

<!--Intan finish -->


<%@page import="com.fourelementscapital.qlib.common.MenuMgmt"%>
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
<%-- <% if(pref.get("beta_ace_editor")!=null && pref.get("beta_ace_editor").equalsIgnoreCase("true")){
        ace_editor_checked=" checked ";
   %> --%>
<script language="JavaScript">CODE_EDITOR=CODE_ACE_EDITOR;</script>
<%-- <%}%> --%>
<%-- <% if(pref.get("beta_reditor_admin")!=null && pref.get("beta_reditor_admin").equalsIgnoreCase("true")){
        reditor_admin_checked=" checked ";
   %> --%>
   <script language="JavaScript">REDITOR_ADMIN=true;</script>
<%-- <%}%> --%>



<div class="fe-mainMenu">
	<ul>
	   <li><a href="login.jsp?action=logout">Logout</a>  [ <a href='#' id='user_profile_a' onclick='menu_profiletoggle(this);return false;'><%=session.getAttribute(com.fourelementscapital.config.Constant.SESSION_LOGGED_USER)%><img src='images/button_plus.gif' border='0' height='14px' style='margin-left:2px' class='btn'><img class='btn' src='images/button_minus.gif' style='display:none;margin-left:2px' border='0' height='14px'></a> ]</li>
	   
	   <!--<li><a href="#" onclick="window.open('https://wiki.alphien.com/ALwiki/Qlib_Introduction')">Help</a></li>-->
	   
	   <li><a href="#" id="help">help</a></li>
   <!-- The Modal -->
	<div id="helpModal" class="modal">

   <!-- Modal content -->
	<div class="modal-content">
		<span class="close">&times;</span>
		<iframe width='100%' height='80%' src="https://wiki.alphien.com/ALwiki/Qlib\_Introduction"></iframe>
	</div>
	</div>
	
	<script>
// Get the modal
var modal = document.getElementById('helpModal');

// Get the button that opens the modal
var btn = document.getElementById("help");

// Get the <span> element that closes the modal
var span = document.getElementsByClassName("close")[0];

// When the user clicks the button, open the modal
btn.onclick = function() {
modal.style.display = "block";
}

// When the user clicks on <span> (x), close the modal
span.onclick = function() {
modal.style.display = "none";
}

// When the user clicks anywhere outside of the modal, close it
window.onclick = function(event) {
if (event.target == modal) {
modal.style.display = "none";
}
}
</script>
	   
	   
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
		<!--<li><span style="font-size:12px"><a href="RELEASE-NOTES.txt" target="_blank">V.3.0.1</a></span></li>-->
		<li><span style="font-size:12px"><a href="#" onclick="packageModal()" id="change-version" data-toggle="modal" data-target="#myModal">V.3.0.1</a></span></li>
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