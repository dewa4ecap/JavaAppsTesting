<%@page import="com.fourelementscapital.iexec.common.IExecAccessMgmt"%>
<%@page import="com.fourelementscapital.config.Constant,com.fourelementscapital.config.Config,com.fourelementscapital.p2p.*,com.fourelementscapital.scheduler.*,java.util.*,com.fourelementscapital.iexec.common.SchedulerMgmt"%>
<%



String ky=(String)session.getAttribute(Constant.SESSION_LOGGED_USER);
if(ky==null || (ky!=null && ky.equals(""))){
 
	String referer=(request.getServletPath()!=null)?request.getServletPath():"";
		
%>
	<jsp:forward page="/login.jsp">
		<jsp:param name="referer" value="<%=referer%>" /> 
	</jsp:forward>
<%}

IExecAccessMgmt ieam = new IExecAccessMgmt(request);
String access = ieam.checkUserPermissionNew("iExec");

if(access.contains("r"))
	out.print("Access Granted. Permissions: "+access);
else
{
	out.print("Access Denied. Permissions: "+access);
	%>
	<jsp:forward page="/denied.jsp">
		<jsp:param name="reason" value="accessdenied" /> 		 
	</jsp:forward>
<%
}

%>


<html>
<head>
<title>iExec</title>


<style>

body {
	font-family:Arial, Verdana, Geneva, Helvetica, sans-serif;
	font-size:medium;
 
}	

</style>
<link rel="stylesheet" type="text/css" href="jquery-1.8.2/css/cupertino/jquery-ui-1.8.2.custom.css" />
<link rel="stylesheet" type="text/css" href="jquery-1.8.2/dataTables-1.6/media/css/demo_table.css" />
<link rel="stylesheet" type="text/css" href="jquery-1.8.2/jquery-treeview/jquery.treeview.css" />
<link rel="stylesheet" type="text/css" href="jquery-1.8.2/contextmenu/jquery.contextmenu.css" />
<link rel="stylesheet" type="text/css" href="jquery-1.8.2/fixedHeaderTable/defaultTheme1.css" />
<link rel="stylesheet" type="text/css" href="jquery-1.8.2/autoSuggest/autoSuggest.css" />
<link rel="stylesheet" type="text/css" href="jquery-1.8.2/flexigrid/css/flexigrid.pack.css" />

<link rel="stylesheet" type="text/css" href="css/wiki_main.css" />
<link rel="stylesheet" type="text/css" href="css/iexec.css" />

<link rel="stylesheet" type="text/css" href="fileupload/fileuploader.css" />



<script type='text/javascript' src='dwr/engine.js'></script>
<script type='text/javascript' src='dwr/util.js'></script>
<script type='text/javascript' src='dwr/interface/IExecMgmt.js'></script>


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
<script language="JavaScript" type="text/javascript" src="jquery-1.8.2/templates/jquery.template.js"></script>

<!--script language="JavaScript" type="text/javascript" src="fileupload/fileuploader.js"></script-->

<script language="javascript" type="text/javascript" src="editarea_0_8_2/edit_area_full.js"></script>

<script language="JavaScript" type="text/javascript" src="js/iexec.js"></script>

<style>
	
#ie_tabs.ui-widget-content {
	border:0px;
}
#ie_tabs.ui-widget {
   font-family:Arial, Verdana, Geneva, Helvetica, sans-serif;
   font-size:.8em;
   height:100%;		
}

#ie_tabs.ui-tabs{
	padding:0px;
}
#ie_tabs li  .ui-icon-close { float: left; margin: 0.4em 0.2em 0 0; cursor: pointer; }
#ie_tabs > .ui-widget-header {
    background: none;
    border: 0px solid #AED0EA;    
    font-weight: bold;
}
#ie_tabs > .ui-tabs-panel{
	border:4px solid #7ABDFF;	 
	marging:0px 0px 0px 3px;
	padding:2px;
	-moz-border-radius: 0px 10px 10px 0px;
	border-radius: 0px 10px 10px 0px;	
	-moz-box-shadow: 5px 5px 5px #888;
	-webkit-box-shadow: 5px 5px 5px #888;
	box-shadow: 5px 5px 5px #888;

} 
#ie_tabs.ui-tabs .ui-tabs-nav {
	padding:0px;
}


.ie_subtab.ui-tabs{
	padding:0px;
}
.ie_subtab.ui-widget{
   font-family:Arial, Verdana, Geneva, Helvetica, sans-serif;
   font-size:.8em;     
}
.ie_subtab.ui-widget-content{
	border:0px;
}

.ie_subtab > .ui-tabs-panel{		 
	marging:0px 0px 0px 0px;
	padding:0px 0px 0px 0px;
}


.ie_subtab > .ui-corner-all {
	-moz-border-radius: 6px 6px 0px 0px;
	border-radius: 6px 6px 0px 0px;
	border:0px solid white; 
}

</style>
 
</head>
<body>
<%@ include file="include/user_menu.inc" %>
<table height="40px" style="position:absolute;z-index:100;top:0px;right:10px">
	<tr>
	<td>	
	<div id="statusMessage" align="left" style="display:none"></div>
	</td>
	<td>
	<div id="progressMessage" align="left" style="display:none"></div>
	</td>
</tr>
</table>

 <ul class="ie_toolBar">
			   	<li><a href="#" onclick="ie_toggleTree(this)"><img src="images/tree_toggle.png" ></a></li>
			   	<li>
				     <table border="0" cellpadding="0" width="400px" cellspacing="0">
				     	<tr>
				     		<td width="*"><span id="ie_searchStrategy"><input type="text"  value=""></span> </td>
							<td width="90" align="center"><input  id="ie_searchBtn" type="button" onclick="ie_search()" value="Open"> </td>
						</tr>
					 </table>	
				</li>					
				<li><a href="#" onclick="wiki_help(); return false;"><img src="images/help.png" ></a></li>
</ul>
			
<table class="main_wrapper" width="99%" height="95%" cellpadding="0" cellspacing="0">
	<tr>
		<td class="folder_view" width="500px"  valign="top">
			<div id="ie_treeMenu" class="chromeScroll"></div>
		</td>		
		<td class="editor_view" width="*" valign="top">
			<div id="ie_tabs">
				<ul></ul>
 			</div>
		</td>
	</tr>
</table>

<span id="file-uploader">       
        
</span>

 
<script language="JavaScript">
		ie_pageinit();
</script>

 
<div id="dialog_commit" title="Comments" style="display:none">
   <div class="label">   	
	Comments:	
   </div>	
	<div>   	
	  <textarea id="dialog_commit_msg" style="width:100%;height:250px"></textarea>
   </div>
</div>

<div id="dialog_tree_comm" title="Select Contract" style="display:none">
	<div class="label" style="border-bottom:1px solid grey;margin-bottom:10px;">   	
	Select Contract:	
   </div>	
    <ul id="dialog_tree_comm_ul" class="filetree"></ul>
	<div id="dialog_tree_comm_inp"  style="border-top:1px solid grey;margin-bottom:10px;padding:5px">   	
	Can't find contract in the list?
	<input class="inputbox" type="text" value="">	
	<input class="inputbtn"  type="button" value="Add Parameter Tab" onclick="ie_addContractSeleBtn()">
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

<div id="wikiDialog" title="" style="display:none;min-height:50px">
	<div id="wikiDialogBdy"></div>
</div>
<%@ include file="include/login.html" %>
</body>
</html>

