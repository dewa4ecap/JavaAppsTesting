<%@page import="com.fe.common.Constant,com.fourelementscapital.scheduler.config.Config,com.fe.p2p.*,com.fe.scheduler.*,java.util.*,com.fe.client.SchedulerMgmt"%>
<html>
<head>
<title>R Functions</title>
<style>

<%
String ky=(String)session.getAttribute(Constant.SESSION_LOGGED_USER);
if(ky==null || (ky!=null && ky.equals(""))){
 
	String referer=(request.getServletPath()!=null)?request.getServletPath():"";
	
	
%>
	<jsp:forward page="/login.jsp">
		<jsp:param name="referer" value="<%=referer%>" /> 
	</jsp:forward>
<%}%>



body {
	font-family:Arial, Verdana, Geneva, Helvetica, sans-serif;	
 
}	

</style>
<link rel="stylesheet" type="text/css" href="jquery-1.8.2/css/cupertino/jquery-ui-1.8.2.custom.css" />
<link rel="stylesheet" type="text/css" href="jquery-1.8.2/dataTables-1.6/media/css/demo_table.css" />


<link rel="stylesheet" type="text/css" href="jquery-1.8.2/jquery-treeview/jquery.treeview.css" />
<link rel="stylesheet" type="text/css" href="jquery-1.8.2/contextmenu/jquery.contextmenu.css" />
<link rel="stylesheet" type="text/css" href="jquery-1.8.2/fixedHeaderTable/defaultTheme1.css" />
<link rel="stylesheet" type="text/css" href="jquery-1.8.2/autoSuggest/autoSuggest.css" />
<link rel="stylesheet" type="text/css" href="jquery-1.8.2/flexigrid/css/flexigrid.pack.css" />
<link rel="stylesheet" type="text/css" href="jquery-1.8.2/qtip/jquery.qtip.min.css" />

<link rel="stylesheet" type="text/css" href="css/wiki_main.css" />
<link rel="stylesheet" type="text/css" href="css/comment_dialog.css" />
<link rel="stylesheet" type="text/css" href="css/revision_list.css" />
<link rel="stylesheet" type="text/css" href="css/ace_r.css">
<link rel="stylesheet" type="text/css" href="css/r.css" />
<link rel="stylesheet" type="text/css" href="css/teamorg.css" />
<link rel="stylesheet" type="text/css" href="css/jq_ui.css">
<link rel="stylesheet" type="text/css" href="css/search_r.css" />

<link rel="stylesheet" type="text/css" href="fileupload/fileuploader.css" />


<link rel="stylesheet" type="text/css" href="js/jquery/multiselect.css" />



<script language="JavaScript" type='text/javascript' src='dwr/engine.js'></script>
<script language="JavaScript" type='text/javascript' src='dwr/util.js'></script>
<script language="JavaScript" type='text/javascript' src='dwr/interface/RFunctionMgmt.js'></script>


<script language="JavaScript" type="text/javascript" src="js/prototype.js"></script>
<script language="JavaScript" type="text/javascript" src="jquery-1.8.2/js/jquery-1.4.2.min.js"></script>
<script>
     var $j = jQuery.noConflict();	 
</script>

<script language="JavaScript" type="text/javascript" src="jquery-1.8.2/js/jquery-ui-1.8.2.custom.min.js"></script>
<script language="JavaScript" type="text/javascript" src="jquery-1.8.2/dataTables-1.6/media/js/jquery.dataTables.min.js"></script>
<script language="JavaScript" type="text/javascript" src="jquery-1.8.2/expander/jquery.expander.js"></script>
<script language="JavaScript" type="text/javascript" src="jquery-1.8.2/contextmenu/jquery.contextmenu.js"></script>

<script language="JavaScript" type="text/javascript" src="jquery-1.8.2/jquery-treeview/jquery.treeview.min.js"></script>
<script language="JavaScript" type="text/javascript" src="jquery-1.8.2/jquery-treeview/jquery.cookie.js"></script>
<script language="JavaScript" type="text/javascript" src="jquery-1.8.2/fixedHeaderTable/jquery.fixedheadertable.min.js"></script>
<script language="JavaScript" type="text/javascript" src="jquery-1.8.2/scrollTo/jquery.scrollTo-1.4.2-min.js"></script>
<script language="JavaScript" type="text/javascript" src="jquery-1.8.2/autoSuggest/jquery.autoSuggest.minified.js"></script>
<script language="JavaScript" type="text/javascript" src="jquery-1.8.2/flexigrid/js/flexigrid.pack.js"></script>
<script language="JavaScript" type="text/javascript" src="jquery-1.8.2/inlineEdit/jquery.inlineedit.js"></script>
<script language="JavaScript" type="text/javascript" src="jquery-1.8.2/qtip/jquery.qtip.min.js"></script>
<script language="JavaScript" type="text/javascript" src="fileupload/fileuploader.js"></script>


<script language="JavaScript" type="text/javascript" src="moment/moment.min.js"></script>

<script language="javascript" type="text/javascript" src="editarea_0_8_2/edit_area_full.js"></script>
<script src="ace/src/ace.js" data-ace-base="ace" charset="utf-8"></script>
<script src="ace/src/mode-r.js" type="text/javascript" charset="utf-8"></script>
<script src="ace/src/theme-dreamweaver.js" type="text/javascript" charset="utf-8"></script>
<script language="JavaScript" src="js/jquery/multiselect.js"></script>

<script language="JavaScript" type="text/javascript" src="js/jquery/commondialog.js"></script>



<script language="JavaScript" type="text/javascript" src="js/r.js"></script>
<script language="JavaScript" type="text/javascript" src="js/teamorg.js"></script>

<%if(request.getParameter("use_ace")!=null){%>
	<script language="JavaScript">
	CODE_EDITOR=CODE_ACE_EDITOR; 
	</script>
<%}%>


<style>
	
#rf_tabs.ui-widget-content {
	border:0px;
}
#rf_tabs.ui-widget {
   font-family:Arial, Verdana, Geneva, Helvetica, sans-serif;
   font-size:.7em;
   height:100%;		
}

#rf_tabs.ui-tabs{
	padding:0px;
}
#rf_tabs li  .ui-icon-close { float: left; margin: 0.4em 0.2em 0 0; cursor: pointer; }
#rf_tabs > .ui-widget-header {
    background: none;
    border: 0px solid #AED0EA;    
    font-weight: bold;
    
}
#rf_tabs > .ui-tabs-panel{
	border:4px solid #7ABDFF;	 
	marging:0px 0px 0px 3px;
	padding:2px;
	-moz-border-radius: 0px 10px 10px 0px;
	border-radius: 0px 10px 10px 0px;	
	-moz-box-shadow: 5px 5px 5px #888;
	-webkit-box-shadow: 5px 5px 5px #888;
	box-shadow: 5px 5px 5px #888;
	position:relative;

} 
#rf_tabs.ui-tabs .ui-tabs-nav {
	padding:0px;
}


.rf_subtab.ui-tabs{
	padding:0px;
}
.rf_subtab.ui-widget{
   font-family:Arial, Verdana, Geneva, Helvetica, sans-serif;
   
}
.rf_subtab.ui-widget-content{
	border:0px;
}

.rf_subtab > .ui-tabs-panel{		 
	marging:0px 0px 0px 0px;
	padding:0px 0px 0px 0px;
}


.rf_subtab > .ui-corner-all {
	-moz-border-radius: 6px 6px 0px 0px;
	border-radius: 6px 6px 0px 0px;
	border:0px solid white; 
}

.ace_editor { 
    height: 90%;
}

.ace_toolbar {
	display:inline-block;
	width:95%;		
	
}
.ace_toolbar .savebtn{
	display:inline;
	float:left;
	margin-top:3px;
	margin-left:10px;
	
}
.ace_toolbar .deletebtn{
	display:inline;	
	float:left;	
	margin-top:1px;
	margin-left:50%;
}
.ace_toolbar .deletebtn img{
	height:26px;	
}

.icon_fullscreen.adjust_max {
	margin-top:1px;
	margin-right:1px; 
}
.icon_fullscreen.adjust_max a {
	padding-top:10px;
}
.fullScreen .comment_dialog,
.fullScreen .status-top,
.fullScreen .ui-widget-overlay
 {
	z-index:10003 !important;	
}
.fullScreen .status-top {
	right:50% !important;
}
</style>
 
</head>
<body>

<%@ include file="include/user_menu.inc" %>

<table height="40px" style="position:absolute;z-index:100;top:0px;right:10px" class='status-top'>
	<tr>
	<td>	
	<div id="statusMessage" align="left" style="display:none"></div>
	</td>
	<td>
	<div id="progressMessage" align="left" style="display:none"></div>
	</td>
</tr>
</table>

 <ul class="rf_toolBar">
			   	<li><a href="#" onclick="rf_toggleTree(this)"><img src="images/tree_toggle.png" ></a></li>
			   	<li>
				    <!--div class="rf_searchBox"-->
					     <table border="0" cellpadding="0" width="500px" cellspacing="0">
					     	<tr>
					     		<td width="*"><span id="rf_searchFunction"><input id="rf_searchFunctionInp" title="Type Function Name &#x21B5;" type="text"  value=""></span> </td>
								<td width="90" align="center" valign="top"><input style="margin-top:1px;width:70px;border-radius:7px;height:28px;"  id="rf_searchBtn" type="button" onclick="rf_search()" value="View"> </td>
							</tr>
						 </table>	
				    <!--/div-->
				</li>					
				<li><a href="#" onclick="wiki_help(); return false;"><img src="images/help.png" ></a></li>
</ul>
			
<table class="main_wrapper" width="99%" height="95%" cellpadding="0" cellspacing="0">
	<tr>
		<td class="folder_view" width="600px"  valign="top">
			   
				<div id="rf_treeMenu" class="chromeScroll"></div>
			   
		</td>		
		<!--td class="folder_view_toggle" valign="middle"><input type="button" id="folder_view_toggle_btn" value="<"></td-->
		
		<td class="editor_view" width="*" valign="top">
			
				<!--div id="rf_editor" style="display:none"><textarea id="rf_editor_area" name="content" cols="120" rows="50"></textarea></div-->
				<div id="rf_tabs">
					<ul>
						<!--li><a href="#tabs-1">Nunc tincidunt</a> <span class="ui-icon ui-icon-close">Remove Tab</span></li-->
					</ul>
					<!--div id="tabs-1">
						<p>Proin elit arcu, rutrum commodo, vehicula tempus, commodo a, risus. Curabitur nec arcu. Donec sollicitudin mi sit amet mauris. Nam elementum quam ullamcorper ante. Etiam aliquet massa et lorem. Mauris dapibus lacus auctor risus. Aenean tempor ullamcorper leo. Vivamus sed magna quis ligula eleifend adipiscing. Duis orci. Aliquam sodales tortor vitae ipsum. Aliquam nulla. Duis aliquam molestie erat. Ut et mauris vel pede varius sollicitudin. Sed ut dolor nec orci tincidunt interdum. Phasellus ipsum. Nunc tristique tempus lectus.</p>
					</div-->
				</div>
			
		</td>
	</tr>
</table>

<span id="file-uploader">       
        
</span>

 
<script language="JavaScript">
	//scd_parseSchedulerPane();
	<%
	    boolean readonly=true;
		boolean lite=false;
	    if(request.getParameter("lite")!=null && request.getParameter("lite").equalsIgnoreCase("true")){
	    	lite=true;
	    }
	    if(request.getParameter("edit")!=null && request.getParameter("edit").equalsIgnoreCase("true")){
	    	readonly=false;
	    }
		if(request.getParameter("open_functions")!=null){			
	  	  	out.println("fr_pageinit('"+request.getParameter("open_functions")+"',"+readonly+","+lite+")");
		}else if(request.getParameter("search_r")!=null){
		 
			out.println("rf_searchFrURL('"+request.getParameter("search_r")+"')");
		}else{
			out.println("fr_pageinit()");		
		}
	%>
	
</script>

 
<div id="dialog_commit" title="Comments" style="display:none">
   <div class="label">   	
	Comments:	
   </div>	
	<div>   	
	  <textarea id="dialog_commit_msg" style="width:100%;height:250px"></textarea>
   </div>
</div>
 
<div id="dialog_lockFunc" title="" style="display:none;min-height:50px">
	<div class="label">  
	 Lock Release options:
	</div>
	<div>    	
	    <select id="dialog_lockFunc_opt">
	    	<option value="0">Manual</option>		 
			<option value="300">Manual or release after 5 minutes from closed of this page</option>
	    </select>
   </div>
</div>

<div id="dialog_packageAction" title="Confirm" style="display:none">
   <div class="label">   	
	 	
   </div>	
	<div class="ul_list chromeScroll">   	
	 	<ul></ul>
   </div>
</div>


 <div id="rf_search_dialog" title="Search Result" style="display:none"> 
    	 <div id="rf_search_dialogBdy"></div>
 </div>

<div id="wikiDialog" title="" style="display:none;min-height:50px">
	<div id="wikiDialogBdy"></div>
</div>
<div id="dialog_folders" title="" style="display:none;min-height:50px">
	<div class="label">  
	 Choose Folder to put back
	</div>
	<div id="dialog_foldersBdy"></div>
</div>

<%@ include file="include/login.html" %>

</body>
</html>

