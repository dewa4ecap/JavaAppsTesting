<%@page import="com.fe.client.SchedulerReportClient"%>
<%@page import="com.fourelementscapital.auth.*"%>
<%@page import="com.fe.common.Constant"%>
<%@ page import="com.fourelementscapital.scheduler.config.Config,com.fe.p2p.*,com.fourelementscapital.scheduler.p2p.*,com.fe.scheduler.*,com.fourelementscapital.scheduler.*,com.fourelementscapital.scheduler.engines.*,com.fourelementscapital.scheduler.alarm.*,java.util.*,com.fe.client.SchedulerMgmt" errorPage="error.jsp"  %>

<%
String ky=(String)session.getAttribute(Constant.SESSION_LOGGED_USER);
if(ky==null || (ky!=null && ky.equals(""))){ 
	String referer=(request.getServletPath()!=null)?request.getServletPath():"";
%>
	<jsp:forward page="/login.jsp">
			<jsp:param name="referer" value="<%=referer%>" /> 
	</jsp:forward>
<%}%>

<%
if(Config.getValue("load_balancing_server")!=null && !Config.getValue("load_balancing_server").equals(P2PService.getComputerName())){ 
 //response.sendError(403);
}
%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
<title>Scheduler</title>

<link rel="shortcut icon" href="favorite.png" type="image/x-icon">
<link rel="icon" href="favorite.png" type="image/x-icon">


<script type='text/javascript' src='dwr/engine.js'></script>
<script type='text/javascript' src='dwr/util.js'></script>
<script type='text/javascript' src='dwr/interface/SchedulerMgmt.js'></script>

<style>
body {
	font-family:Arial, Verdana, Geneva, Helvetica, sans-serif;
	font-size:.9em;
 
}
.scd_paneHeader {
	background-color:#E5E2E4;
	padding:1px;
	 
		 
}
.scd_paneBody {
	
	
	background-color:#E5E2E4; 
	padding:1px;
 
	 
}

small{
	font-size:.7em;
	font-style:italic;
	color:red;
	
	 
}

.smalltips {
	font-size:.7em;
}

#dld_timeZone {
    width:15em; /* set width here or else widget will expand to fit its container */
    padding-bottom:2em;
}

.ygtvfocus {
	background-color:#FFFFFF;
	
	
}

 
#dld_editor1 td{
	border:1px solid #c2c2c2;
}


.jProgressBar_Green .border {
    background-color: #000000;
}
.jProgressBar_Green .border .background {
    background-color: #ffffff;
}
.jProgressBar_Green .border .background .bar {
    background: #6dd436 url(images/jProgressBarBackground.png) right top;
} 


#scd_onlinepeers {
  font-family:Arial,Verdana,Geneva,Helvetica,sans-serif;
  display:inline-block;


}
#scd_onlinepeers div.peer{
	margin:7px 2px 2px 2px;
	-moz-border-radius:4px;
	border-radius:4px;
	border:1px inset grey;
	width:85px;
 
	font-size:.79em;
	/*display:table-cell;*/
	padding:3px 7px 2px 6px;	
	text-align:center;
	float:left;
	color:#9E9E9E;
	background:url('images/peer_offline.gif') 100% 40% no-repeat #e0e0e0;
	
}
#scd_onlinepeers div.peer.online {
	
	border:1px outset #666666;
	color:#000000;
	background-color:#49BF58;
	background-image:none;
	
	
}

#scd_onlinepeers div.peer.running{
	/*font-weight:bold;*/ 
	/*color:#046408;*/
	/*background-color:#EDD311;*/	 
	/*border:1px inset #368EC3;	
	background-color:#FCCACA;*/
	background-color:#F2F222;
	background-image:url('images/peer_exec_bg.gif');
	background-position:2px 50%; 
	background-repeat:no-repeat;

	-moz-box-shadow: 0 0 4px #06BA2A;
    -webkit-box-shadow: 0 0 4px#06BA2A;
    box-shadow: 0 0 4px #06BA2A;
	border:1px outset #06BA2A;

}
#scd_onlinepeers div.batch{
   	float:right;
	vertical-align:top;
	/*-moz-border-radius-bottomleft:15px;
	border-radius:0px 0px 0px 15px;
	*/
	/*-moz-border-radius:13px;
	border-radius:13px;*/
	/*background-color:#CC3300;*/	
	background-image:url('images/peer_exec.gif');
	background-position:center; 
	background-repeat:no-repeat;
	font-weight:bold;			 
	padding:0px 5px 0px 5px;
	margin-top:-10px;
	margin-right:-12px;
	height:16px;
	width:16px;	
	color:#142861;/*#CC3300;*/
	
} 	 

.flex_field_name {
	width:500px;
	
}

.unixPeerSetup  {

	float:right;
	width:180px;
	border:1px solid #e0e0e0;
	box-shadow: 2px 2px 2px #CCC;
}

.unixPeerSetup .label {
	background-color:#e0e0e0;
	font-size:1.1em;
	text-align:center;
}

.unixPeerSetup ul {
	list-style-type: none;
	padding-left: 0px;
}

.unixPeerSetup ul li{
	padding: 2px 7px;
	border: 2px solid #E9ECE9;
	background-color: #EDF4FD;
	-moz-box-shadow: 2px 2px 2px #ccc;
	-webkit-box-shadow: 2px 2px 2px #CCC;
	box-shadow: 2px 2px 2px #CCC;
	border-radius: 5px;
	margin: 2px 5px 5px 5px;
	cursor: pointer;
	text-align:center;
}


</style>

<!--
<link rel="stylesheet" type="text/css" href="yui/build/tabview/assets/skins/sam/tabview.css">   
<link rel="stylesheet" type="text/css" href="yui2_7_0/build/container/assets/container.css">
<link rel="stylesheet" type="text/css" href="yui2_7_0/build/autocomplete/assets/skins/sam/autocomplete.css" />
<link type="text/css" rel="stylesheet" href="yui2_7_0/build/treeview/assets/skins/sam/treeview.css"> 
<link type="text/css" rel="stylesheet" href="yui2_7_0/build/datatable/assets/skins/sam/datatable.css">
-->


<link rel="stylesheet" type="text/css" href="yui/build/tabview/assets/skins/sam/tabview.css">   
<link type="text/css" rel="stylesheet" href="yui/build/treeview/assets/skins/sam/treeview.css">  
<link rel="stylesheet" type="text/css" href="yui/build/container/assets/skins/sam/container.css" />
<link rel="stylesheet" type="text/css" href="yui2_7_0/build/container/assets/skins/sam/container.css">  
<link type="text/css" rel="stylesheet" href="yui2_7_0/build/datatable/assets/skins/sam/datatable.css"> 
<link rel="stylesheet" type="text/css" href="yui2_7_0/build/autocomplete/assets/skins/sam/autocomplete.css" />
<link rel="stylesheet" type="text/css" href="yui2_7_0/build/button/assets/skins/sam/button.css" />
<link rel="stylesheet" type="text/css" href="yui2_7_0/build/menu/assets/skins/sam/menu.css" />


<link rel="stylesheet" type="text/css" href="jquery-1.8.2/css/cupertino/jquery-ui-1.8.2.custom.css" />
<link rel="stylesheet" type="text/css" href="jquery-1.8.2/dataTables-1.6/media/css/demo_table.css" />

<link rel="stylesheet" type="text/css" href="jquery-1.8.2/jquery-treeview/jquery.treeview.css" />
<link rel="stylesheet" type="text/css" href="jquery-1.8.2/contextmenu/jquery.contextmenu.css" />
<link rel="stylesheet" type="text/css" href="jquery-1.8.2/fixedHeaderTable/defaultTheme1.css" />
<link rel="stylesheet" type="text/css" href="jquery-1.8.2/autoSuggest/autoSuggest.css" />
<link rel="stylesheet" type="text/css" href="jquery-1.8.2/cluetips/jquery.cluetip.css" />
<link rel="stylesheet" type="text/css" href="jquery-1.8.2/dropdown/dropdown.css" />


<link rel="stylesheet" type="text/css" href="js/jquery/multiselect.css" />

<link rel="stylesheet" type="text/css" href="css/wiki_main.css" />
<link type="text/css" rel="stylesheet" href="sh_util/sh_bright.css">
<link rel="stylesheet" type="text/css" href="css/revision_list.css" />
<link rel="stylesheet" type="text/css" href="css/comment_dialog.css" />
<link rel="stylesheet" type="text/css" href="css/mytable.css"> 
<link rel="stylesheet" type="text/css" href="css/scheduler.css">
<link rel="stylesheet" type="text/css" href="css/jq_ui.css">
<link rel="stylesheet" type="text/css" href="css/ace_r.css">
<link rel="stylesheet" type="text/css" href="css/teamorg.css" />

<script language="JavaScript" type="text/javascript" src="js/prototype.js"></script>

<script language="JavaScript" type="text/javascript" src="jquery-1.8.2/js/jquery-1.4.2.min.js"></script>
<!--  script language="JavaScript" type="text/javascript" src="jquery-1.8.2/js/jquery-1.8.0.min.js"></script-->

<script language="JavaScript" type="text/javascript" src="jquery-1.8.2/js/jquery-ui-1.8.2.custom.min.js"></script>
<script language="JavaScript" type="text/javascript" src="jquery-1.8.2/dataTables-1.6/media/js/jquery.dataTables.min.js"></script>
<script language="JavaScript" type="text/javascript" src="jquery-1.8.2/expander/jquery.expander-1.4.3.js"></script>
<script language="JavaScript" type="text/javascript" src="jquery-1.8.2/contextmenu/jquery.contextmenu.js"></script>

<script language="JavaScript" type="text/javascript" src="jquery-1.8.2/jquery-treeview/jquery.treeview.min.js"></script>
<script language="JavaScript" type="text/javascript" src="jquery-1.8.2/jquery-treeview/jquery.cookie.js"></script>
<script language="JavaScript" type="text/javascript" src="jquery-1.8.2/fixedHeaderTable/jquery.fixedheadertable.min.js"></script>
<script language="JavaScript" type="text/javascript" src="jquery-1.8.2/scrollTo/jquery.scrollTo-1.4.2.js"></script>
<script language="JavaScript" type="text/javascript" src="jquery-1.8.2/autoSuggest/jquery.autoSuggest.js"></script>
<script language="JavaScript" type="text/javascript" src="jquery-1.8.2/templates/jquery.template.js"></script>
<script language="JavaScript" type="text/javascript" src="jquery-1.8.2/cluetips/jquery.cluetip.js"></script>
<script language="JavaScript" type="text/javascript" src="jquery-1.8.2/dropdown/dropdown.js"></script> 
<script language="JavaScript" type="text/javascript" src="jquery-1.8.2/datetimepicker/jquery-ui-timepicker.js"></script>




<script>
     var $j = jQuery.noConflict();
	 
	 <%if(request.getParameter("version")!=null && request.getParameter("version").equalsIgnoreCase("lite")){
	    out.print("var full_version=false;");
	}else{
		out.print("var full_version=true;");
	}%>
</script>

<!--script language="JavaScript" type="text/javascript" src="jquery-1.8.2/js/jProgressBar.js"></script-->
 
<script type="text/javascript" src="yui2_7_0/build/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="yui2_7_0/build/element/element-min.js"></script>
<script type="text/javascript" src="yui2_7_0/build/datasource/datasource-min.js"></script>
<script type="text/javascript" src="yui2_7_0/build/autocomplete/autocomplete-min.js"></script> 
<script type="text/javascript" src="yui2_7_0/build/animation/animation-min.js"></script>
<script type="text/javascript" src="yui2_7_0/build/container/container-min.js"></script>
<script type="text/javascript" src="yui2_7_0/build/tabview/tabview-min.js"></script>
<script type="text/javascript" src="yui2_7_0//build/treeview/treeview-min.js"></script>
<script type="text/javascript" src="yui2_7_0/build/datatable/datatable-min.js"></script> 
<script type="text/javascript" src="yui2_7_0/build/dragdrop/dragdrop-min.js" ></script> 
<script language="javascript" type="text/javascript" src="editarea_0_8_2/edit_area_full.js"></script>

<script language="JavaScript" type="text/javascript" src="js/jquery/commondialog.js"></script>

<script language="JavaScript" src="js/jquery/multiselect.js"></script>

<script language="JavaScript" type="text/javascript" src="moment/moment.min.js"></script>


<script src="ace/src/ace.js" data-ace-base="ace" charset="utf-8"></script>
<script src="ace/src/mode-r.js" type="text/javascript" charset="utf-8"></script>
<script src="ace/src/theme-dreamweaver.js" type="text/javascript" charset="utf-8"></script>

<script language="JavaScript" type="text/javascript" src="js/utils/table.js"></script>
<script language="JavaScript" type="text/javascript" src="js/utils/linktable.js"></script>
<script language="JavaScript" type="text/javascript" src="js/treednd.js"></script>
<!--  script language="JavaScript" type="text/javascript" src="js/scheduler.js"></script-->
<script language="JavaScript" type="text/javascript" src="js/scheduler.js"></script>
<script language="JavaScript" type="text/javascript" src="js/teamorg.js"></script>
<script language="JavaScript" type="text/javascript" src="js/datevalidate.js"></script> <!--for date funtionality-->
<script language="JavaScript" type="text/javascript" src="js/trading.js"></script> <!--for date funtionality-->
<script language="JavaScript" type="text/javascript" src="js/scheduler_bbpluggin.js"></script>

<script language="JavaScript" type="text/javascript" src="js/mytable.js"></script>
 
<script language="JavaScript" type="text/javascript" src="js/rhino_debugger.js"></script>

<script type="text/javascript" src="sh_util/sh_main.js"></script>
<script type="text/javascript" src="sh_util/sh_slang.js"></script>
<script type="text/javascript" src="sh_util/sh_javascript.js"></script>



<style>


.ui-widget{
   font-family:Arial, Verdana, Geneva, Helvetica, sans-serif;
   font-size:.8em;		
}
 	.tabs-bottom .ui-tabs-panel { height: 680px; overflow: auto; min-width:500px }
	#scd_taskpane .ui-tabs-panel{ padding:10px 1px 1px 3px;} 
	/*.tabs-bottom .ui-tabs-panel { height: 600px; overflow: auto; }*/
	
	/*
	.tabs-bottom { position: relative; }
	.tabs-bottom .ui-tabs-nav { position: absolute !important; left: 0; bottom: 0; right:0; padding: 0 0.2em 0.2em 0; } 
	.tabs-bottom .ui-tabs-nav li { margin-top: -2px !important; margin-bottom: 1px !important; border-top: none; border-bottom-width: 1px; }
	.ui-tabs-selected { margin-top: -3px !important; }
	*/
.task-tabs .ui-tabs-nav li {
 
}	
	
	#tabface .yui-content{
		padding:0px;
	}
.ygtvfocus {
	background-color:#FFFFFF;
	
	
}
.ygtvtable {
	width:98%;
	margin:0px;
	paddign:7px;	
} 

.ygtvtable td{
	paddign:0px;		
} 

.scd_itembtn {
	font-size:.6em;
	margin-right:1px;
	padding:0px 1px 0px 1px;
	background: #fff;	
	border : 1px solid #aaa;
	cursor:pointer;
	
	-moz-border-radius: 2px;
	border-radius: 2px;
	 -moz-box-shadow: 1px 1px 1px #ccc;
  	-webkit-box-shadow: 1px 1px 1px #ccc;
 	 box-shadow: 1px 1px 1px #ccc;
}

img {
	vertical-align:middle;
}

.scd_subheading {
	margin-top:2px;
	border-bottom:2px solid gray;
	font-weight:bold;
	
	
}

.scd_subheader {
    border-bottom: 2px solid #7ABDFF;
    font-size: 1.3em;
    margin-left: px;
    margin-top:25px;
    padding-left:5px;
    padding-bottom:2px;
}



.simpletable table {
    border-collapse: collapse;
    border: 2px solid #000;
    /*font: normal 60% / 110% arial, helvetica, sans-serif;*/
	font-family:arial, helvetica, sans-serif;
	/*font-size:.3em;*/
	font-size:9px;
    color: #555;
    background: #fff;
}

.simpletable td, th {
    border: 1px dotted #bbb;
    padding: 2px;
}

.simpletable caption {
    /*padding: 0 0 .5em 0;*/
    text-align: left;
    font-size: 1em;
    font-weight: bold;
    text-transform: uppercase;
    color: #333;
    background: transparent;
}
 
.simpletable thead th, tfoot th {
    border: 2px solid #000;
    /*text-align: left;*/
    font-size: .9em;
    font-weight: bold;
    color: #333;
    background: transparent;
}

.simpletable tfoot td {
    border: 2px solid #000;
}
/* =body
 ----------------------------------------------- */
.simpletable tbody th, tbody td {
    vertical-align: top;
    /*text-align: left;*/
}

.simpletable tbody th {
    white-space: nowrap;
}
 

.simpletable tbody tr:hover {
    background: #fafafa;
}


.treeTaskItem {
	padding:2px 0px 2px 0px;
	margin:0px;
	background-color:#F6F6F7;
	border-bottom:1px solid #E0E0E0;
}
.treeTaskItem:hover {
	background-color:#DFF0FF;
}
.selTaskHighlight{
	/*border:3px solid #132769;*/
	background-color: #B0CEF7 !important;
	/*font-weight:bold;*/
	/*color:#FF9E3D;*/
	background-image:url('./images/right_arrow.gif');
	background-position: right center; 
    background-repeat:no-repeat;	
}

.treefldrHld {
	background-color:#B0CEF7 !important; 
}







.queuetable table {
    border-collapse: collapse;
    border: 2px solid #000;
    /*font: normal 60% / 110% arial, helvetica, sans-serif;*/
	font-family:arial, helvetica, sans-serif;
	/*font-size:.3em;*/
	
    
}

.queuetable td, th {
    border: 1px dotted #bbb;
    padding: 0px;
}

.queuetable caption {
    /*padding: 0 0 .5em 0;*/
    text-align: left;
    font-size: 1em;
    font-weight: bold;
    text-transform: uppercase;
    color: #333;
    background: transparent;
}
 
 /*
.queuetable thead th, tfoot th {
   
	border: 1px solid #c0c0c0;
   
    font-size: .9em;
    font-weight: bold;
    color: #333;
    background: transparent;
}
*/

.queuetable tfoot td {
    border: 2px solid #000;	
}
/* =body
 ----------------------------------------------- */
.queuetable tbody th, tbody td {
    vertical-align: top;
	font-size:.9em;    
    /*text-align: left;*/
}

.queuetable tbody th {
    white-space: nowrap;
}
 

.queuetable tbody tr:hover {
    background: #fafafa;
}

.queuetable  tbody td:nth-child(2) {
    text-align:right;
	font-size:.8em;
	
	padding-right:7px;
}
.queuetable  tbody td:nth-child(3) {
    padding-left:3px;
	font-size:.8em;
	 
}


#scd_histLogsList .queuetable  tbody td:nth-child(6),
#scd_histLogsList .queuetable  tbody td:nth-child(7) {
    text-align:right;
	padding-right:7px;	
}


.log_table .db_insert,.log_table .db_update,.log_table .db_delete {
   padding:0px 0px 0px 12px;
   margin-right:3px;
   
   background-repeat:no-repeat;
   background-position:left center; 
   /*border:1px solid #b0b0b0;*/
   background-color:#b0b0b0;
   -moz-border-radius: 3px;
   border-radius: 3px;
   font-size:0.9em;
   
} 
.log_table  .db_insert{
  background-image:url('images/db_insert.png');
}

.log_table .db_update{
  background-image:url('images/db_update.png');
}
.log_table .db_delete{
  background-image:url('images/db_delete.png');
}
 



.pageTitle{
	font-family:Arial, Verdana, Geneva, Helvetica, sans-serif;	 
	font-weight:bold;
	color:#055EA8;	 
	font-size:large;
}	
.widgetTitle{
	font-family:Arial, Verdana, Geneva, Helvetica, sans-serif;
	 
	font-weight:bold;
	color:#2F3337;
	margin-left:5px;
	font-size:medium;
}
.widgetBox{
	border:2px solid #AAAEAA;
	margin-bottom:10px;	
	border-collapse:collapse;
	width:100%;
	
}
.widgetBox td{
	padding:4px;
}
 
.widgetCommodity td{
	padding:0px;
}
 
body {
	font-family:Arial, Verdana, Geneva, Helvetica, sans-serif;
	font-size:medium;
}

small {
	font-family:Arial, Verdana, Geneva, Helvetica, sans-serif;
	font-size:11px;
}



ul.draglist { 
    position: relative;
    width: 150px; 
    height:135px;
    background: #f7f7f7;
    border: 1px solid gray;
    list-style: none;
    margin:0;
    padding:0;
	overflow:scroll;
	overflow-x:hidden;
}

ul.draglist li {
    margin: 1px;
    cursor: move;
    zoom: 1;
}


ul.draglistwide { 
    position: relative;
    width: 350px; 
    height:235px;
    background: #f7f7f7;
    border: 1px solid gray;
    list-style: none;
    margin:0;
    padding:0;
	overflow:scroll;
	overflow-x:hidden;
}

ul.draglistwide li {
    margin: 1px;
    cursor: move;
    zoom: 1;
}

li.dndlistitem {
    background-color: #D1E6EC;
    border:1px solid #7EA6B2; 
	padding-bottom:2px;
}

#dld_timeZone {
    width:15em; /* set width here or else widget will expand to fit its container */
    padding-bottom:2em;
}
#dld_timeZone1 {
    width:15em; /* set width here or else widget will expand to fit its container */
    padding-bottom:2em;
}
	
	
.ui-widget-overlay {
	background:#b5b5b5;
	opacity:0.9;
}


.peerTaskAssociated {
	background-color:#006633;
	
}

.peer_matrix{
	/*width:100%;*/
}
.peer_matrix,
.peer_matrix td{
	border:1px solid #d0d0d0;	
	
}

.peer_matrix td {
	vertical-align:middle;	
	padding:0px;
}

.peer_matrix td {
	vertical-align:middle;	
	padding:2px 2px;
}
.peer_matrix tbody tr:nth-child(even){
	background: #FFFFC2
}

 

.peer_matrix .peers {
	background-color:#E0E0E0;
	
	/*height:50px;*/
	
	width:30px;
	height:350px;
	vertical-align: bottom;
	/*text-align:right;*/
}


.peer_matrix .tasks {
	background-color:#E0E0E0;
	text-align:right;
	/*-webkit-transform: rotate(90deg); 
	-moz-transform: rotate(90deg);
	width:50px;
	height:150px; */
    
}

.peer_matrix .tasks .taskdetails {
	margin-left:10px;
	display:inline;
}

.peer_matrix .taskname {
 -webkit-transform: rotate(-90deg);
  -moz-transform: rotate(-90deg);
  -ms-transform: rotate(-90deg);
  -o-transform: rotate(-90deg);
  transform: rotate(-90deg);

  /* also accepts left, right, top, bottom coordinates; not required, but a good idea for styling */


  filter: progid:DXImageTransform.Microsoft.BasicImage(rotation=3);
  white-space: nowrap;
  width:30px;
  padding-left:10px;
   -webkit-transform-origin: 10% 0%;
  -moz-transform-origin: 10% 00%;
  -ms-transform-origin: 10% 0%;
  -o-transform-origin: 10% 0%;
  transform-origin: 10% 20%;
}
.peer_matrix .taskname {
 display:block;
}
.peer_matrix .plink .notes {
  display:block;
}
.peer_matrix .ccount{
	margin-top:10px;
	font-size:0.8em;
	color:grey;
}

.peer_matrix .plink .peerbtn {
	float:left;
	padding-left:6px;
	text-align: center;
	border-top: 1px dotted grey;
	padding-top: 5px;
	margin-top: 10px;

}

.peer_matrix tbody tr td.peers div.plink{
	font-size:1.1em;	
	color:#00000;
	text-decoration: none;	 
	background-repeat:no-repeat;
	background-position:right; 
	padding-right:20px;
	display:table-cell;
}

.peer_matrix tbody tr td.peers a.plink:hover{		
	text-decoration: underline;	
}



.peer_matrix tbody tr td.peers div.toolbar{
	padding:0px 30px 0px 3px;
	/*margin-right:40px;*/
	display:table-cell;
	 

}

.peer_matrix tbody tr td.peers span.friendlyname {
	padding:0px 5px 0px 3px;
	 
}

.peer_matrix tbody tr td.peers div.ccount {	
	width;100%;
	padding:0px 3px 0px 3px;
	margin-right:0px;
	float:right;
}

/*
.peer_matrix .hl_col {
	border-left:1px solid red;
	border-right:1px solid red;	 
}
.peer_matrix .hl_row {
	border-top:1px solid red;
	border-bottom:1px solid red;	 
}
*/
.peer_matrix .hl_row,.peer_matrix .hl_col {
	background-color:#E28299;
	border:1px solid #E28299;
}
.peer_matrix .hl_row.peerTaskAssociated,.peer_matrix .hl_col.peerTaskAssociated {
	background-color:#006633;
}



.pa_matrix_offpeer {
	background-image:url('images/peer_off.gif');
	background-repeat:no-repeat;
	background-position: 10px 0px; 
}


.pa_matrix_onpeer {
	background-image:url('images/peer_on.gif') ;
	background-repeat:no-repeat;
	background-position: 10px 0px; 
	
}



.ui-droppable-active{   
   border:2px dotted red;
   
}

.emptyfoldercls {	
	color:#D0D0D0;
}

.peer_matrix .peers > .ccount, .peer_matrix .peers > .notes  {
  /* display:none;*/
}


 

#scd_main_tree ul.treeview  {
	/*list-style-type: none; margin: 0; padding: 0;*/	
}

.td_folder_tree.hide {
  	display:none;	
}


#scd_taskpane_holder > .ui-tabs-panel.leftbr{ 
	
	border-left:4px solid #7ABDFF;
	/*-moz-border-radius: 10px 5px 5px 0px;
	border-radius: 10px 5px 5px 0px;*/
		
}

.TaskGroupBar{
	/*background-color:#243356;*/
	background-image:url(images/button_bkgnd.gif);
	-moz-border-radius: 5px;
	border-radius: 5px;	
	border:1px solid #A2BCC0;
	/*width:98%;*/
	margin-bottom:5px;
	margin-right:4px;
	padding:4px 5px 6px 8px;
}
.TaskGroupBarBtn{
	margin-left:2px;
	font-size:.7em;
	/*background-color: #568BBF;*/
	-moz-border-radius: 3px;
	border-radius: 3px;	
	padding:1px;
	border:1px solid #243356;

}

.TaskGroupBar.ui-state-active {
	border:2px double red;
	padding:2px; 
}

.roundedBorder{
	padding:4px;
	-moz-border-radius: 4px;
	border-radius: 4px; 
}

.TaskGroupBarTrash {
  height:38px;
}
.TaskGroupBarTrash div.label_groupname {
    background: url("images/trash_medium.png") no-repeat scroll left center transparent;
     
    margin-top: 20px;
    padding-bottom: 20px;
    padding-left: 20px;
    padding-right: 20px;    
    cursor:pointer;
}

/*
.scd_revisionList {
	list-style-type:none;
	height:550px;
	overflow:auto;
}
.scd_revisionList li {
	border:1px solid #64B2D9;
	-moz-border-radius: 3px;
	border-radius: 3px;
	 
	margin-bottom:7px;
	padding:7px;
	background-color: #AED0EA;
 
	
	 -moz-box-shadow: 2px 2px 2px #ccc;
  	-webkit-box-shadow: 2px 2px 2px #ccc;
 	 box-shadow: 2px 2px 2px #ccc;
	
}
*/

.taskNameOLWR {
	position: relative;
	margin:1px 0px 3px -3px;
	padding-top:1px;
	padding-bottom:3px;
}
.taskNameOL{
	position:absolute;
	top:0px;
	left:0px;
	z-index:100;
	padding-left:3px;
	padding-top:2px;
	padding-bottom:3px;
}

.taskNameOL_PT {
	top:1px
	left:0px;	
	z-index:101;
	position:absolute;
	text-align:right;
	width:100%;
}
.taskNameOL_P {
	/*background-image:url('images/task_progress.gif');
	background-repeat:repeat-x;*/  
	background-color:#7094FF;
	position:absolute;
	margin-bottom:3px;
	pading-bottom:2px;
	
	top:1px
	left:0px;	
	z-index:99;
	height:17px;
		
	-moz-box-shadow: 0 0 5px #888;
	-webkit-box-shadow: 0 0 5px #888;
	box-shadow: 0 0 5px #888;
	 
}

#scd_peer_stattab {
	font-size:1em;
}


#progressMessage {
	padding:5px;
	border-color:#FF9875;	  
	background-color:#FAD8CD;
		 
	/*display:none;*/ 
	vertical-align:middle;margin-bottom:0px
	
	
	
}
#statusMessage {	   
	background-color:#C5E5FC; 
	border-color:#3CABFA;
	vertical-align:middle;
	/*display:none;*/
	margin-bottom:0px
   
}

#progressMessage, #statusMessage{
	border-width:2px;
	border-style:solid;
	padding:5px;	 
	-moz-border-radius: 5px;
	 border-radius: 5px;
	-moz-box-shadow: 5px 5px 5px #ccc;
  	-webkit-box-shadow: 5px 5px 5px #ccc;
 	box-shadow: 5px 5px 5px #ccc;
	 
}


#scd_monitors.ui-tabs.ui-widget{
	font-size:.75em;
}



#scd_taskpane_holder.ui-widget-content {
	border:0px;
}
#scd_taskpane_holder.ui-widget {
   font-family:Arial, Verdana, Geneva, Helvetica, sans-serif;
   font-size:.8em;
   height:100%;		
}

#scd_taskpane_holder.ui-tabs{
	padding:0px;
	margin-left:4px;
}
#scd_taskpane_holder li  .ui-icon-close { float: left; margin: 0.4em 0.2em 0 0; cursor: pointer; }
#scd_taskpane_holder > .ui-widget-header {
    background: none;
    border: 0px solid #AED0EA;    
    font-weight: bold;
}
#scd_taskpane_holder > .ui-tabs-panel{
	border:4px solid #7ABDFF;
	border-left:0px;	 
	marging:0px 0px 0px 3px;
	padding:2px;
	-moz-border-radius: 0px 10px 10px 0px;
	border-radius: 0px 10px 10px 0px;
	
	-moz-box-shadow: 5px 5px 5px #888;
	-webkit-box-shadow: 5px 5px 5px #888;
	box-shadow: 5px 5px 5px #888;
	position:relative;

} 
#scd_taskpane_holder.ui-tabs .ui-tabs-nav {
	padding:0px;
}
#tabface .yui-content {
	padding:5px;
}

.scd_toolBar {
	list-style:none;
 	padding:0px;
	margin:2px 0px 0px 0px;;	
}
.scd_toolBar > li {
	display:inline-block;
	margin:2px 0px 5px 2px;
	padding:2px;
	background:#7ABDFF;
	
	-moz-border-radius: 5px;
	border-radius: 7px;
	-moz-box-shadow: 2px 2px 2px #888;
	-webkit-box-shadow: 2px 2px 2px #888;
	box-shadow: 2px 2px 2px #888;
	height:30px;
	vertical-align:middle;
 
}


.scd_tabopen {
	background-color:#D9D9D9;
	box-shadow: 1px 2px 3px #888;
	/*border:1px solid #c0c0c0;*/
	
}
.scd_tabactive.scd_tabopen {
	background-color:#7ABDFF !important;
	box-shadow: 1px 2px 3px #888;
	/*border:1px solid #c0c0c0;*/
}

ul.as-selections li.as-selection-item {
	font-size: 11px;
    padding: 2px 5px;
}
ul.as-selections li.as-original input {    
    /*height: 13px;*/
    padding-top: 0;
}
li.as-original input {
	font-size: 11x;
}
ul.as-selections {   
    padding: 2px 0 2px 2px;
}


.pluginLabel {
	font-weight:bolder;
	font-size:.9em;	
	display:inline;
	margin-right:30px;
	vertical-align:top;
}
.editorBox {
	border:0px solid grey;
	border-bottom:none;
	height:100%;
	margin-top:10px;
}
.scd_toggle {
	border:1px solid #D4D4D4;
}

.scd_lockedBy {
	display:none;	
}

.scd_lockedBy.showOn {
	
	position:absolute;
	z-index:100;	
	display: inherit;   
    margin-bottom: -25px;   
    top: 10;
    right: 10;
	border:2px solid red; 
    -moz-border-radius: 10px 10px 10px 10px ;
	border-radius: 10px 10px 10px 10px ;
	width:auto;	 
	/*-moz-box-shadow: 5px 5px 2px #ccc;
  	-webkit-box-shadow: 5px 5px 2px #ccc;
  	box-shadow: 5px 5px 5px #ccc;
  	*/
 	 
	 padding:3px 3px 3px 3px;
	 font-weight:bold;
	 background-color:#F7BEC4;
	 text-align:center;
    
}

.data_log {
	height:500px;
	overflow:scroll;
	overflow-x:hidden;
}

.data_log .action_D,.data_log .action_N1 {
	text-decoration:line-through;
	color:#E32D14;
}
.data_log .action_I,.data_log .action_N  {	 
	color:#069406;
}

.data_log .action_N1,.data_log .action_N  {	 
	background-color:#F2C694;
}

.data_log .session {   
   font-size:1.3em;  
   font-weight: bold;

}
.data_log .session .tablename {
  color:#FF331C;
}   
.data_log .session .time {
   font-size:0.55em;
  
}


.data_log .type_2000,.data_log .type_5000 {

}

.data_log .type_3000,.data_log .type_4000,.data_log .type_6000 {	
	color:#E01B32;
}


.data_log table {  
   margin-top:2px;
   margin-bottom:15px;
}
.data_log table thead th{
   font-weight: bold;   
   border:1px solid;
   border-collapse:collapse;
   background:#d0d0d0;
}

.data_log .action_D td:first-child {
   padding-left: 14px;    
   background:url("images/db_delete.png") no-repeat 2px center; 
   
}
.data_log .action_I td:first-child {
   padding-left: 14px;   
   background:url("images/db_insert.png") no-repeat 2px center;
}
.data_log .action_NU td:first-child {
   padding-left: 14px;   
   background:url("images/db_noupdate.png") no-repeat 2px center;
}


.data_log .action_N td:first-child,.data_log .action_N1 td:first-child {
   padding-left: 14px;   
   background:url("images/db_update.png") no-repeat 2px center;
   
}

.following_panel .scd_socialbtn input {
    background: none repeat scroll 0 0 #BBE0F2;
    border: 2px solid #7ABDFF;
    border-radius: 10px 10px 10px 10px;
    cursor: pointer;
    font-size: 1.4em;
    margin-left: 300px;
    margin-top: 10px;
    padding: 5px;
    z-index: 1000;
}
 
 
.following_panel {
    overflow: auto;
}
 
.following_panel ul {
    list-style: none outside none;
}
 
.following_panel ul li {
    border: 2px solid #7ABDFF;
    border-radius: 10px 10px 10px 10px;
    box-shadow: 3px 3px 2px #CCCCCC;
    display: inline-block;
    font-size: 1.8em;
    margin: 5px;
    padding: 2px 10px;
}

.ui-tabs-vertical { width: 55em; border:none;background:none;}
.ui-tabs-vertical .ui-tabs-nav { padding: .2em 0em .2em .2em; float: left; width: 8%; border:none; background:none; margin-right:2px; margin-top:30px; }
.ui-tabs-vertical .ui-tabs-nav li { clear: left; width: 100%;border-width:2px; border-bottom: 2px solid #2694e8 !important; border-right-width: 0 !important; margin: 0 -1px .2em 0; margin-bottom:5px !important;background-color: #e0e0e0;padding-left:0px; }
.ui-tabs-vertical .ui-tabs-nav li a { display:block; width:99%; padding-left:5px;}
.ui-tabs-vertical .ui-tabs-nav li.ui-tabs-active { padding-bottom: 0; padding-right: .1em; border-right-width: 1px; border-right-width: 0px; background-color: #2779aa;}
.ui-tabs-vertical .ui-tabs-panel { padding: 0; float: left; width: 91%; height:100%; overflow-x:hidden;overflow-y:scroll; border:2px solid #3baae3;border-radius:5px;}


#dialog_Rserveconfig_proc ul {
	list-style: none;
	padding-left:0px;
	margin:10px;
}
#dialog_Rserveconfig_proc ul li {
	border:1px solid #9DA0A5;	
	margin:0px 0px 5px 5px;
	padding:5px;
	box-shadow:2px 2px 5px #c0c0c0;
	border-radius:2px;
	display:inline-block;
	width:96%;
}

#dialog_Rserveconfig_proc ul li.running {	
	background-image: url(images/task_executing.gif);
	background-repeat: no-repeat;
	background-position: 5 50%;
	
}

#dialog_Rserveconfig_proc ul li .deletebtn {
	float:right;
	height:18px;
	margin-left:20px;
}


#dialog_Rserveconfig_proc ul li .scriptname{	
	width: 220px;
	float: left;
	height: 18px;
	margin-left:20px;
	
}

#dialog_Rserveconfig_proc ul li .otherinfo {
	display:inline-block;
	float:left;
	font-size:.9em;

}

#dialog_Rserveconfig_proc ul li .otinfo {
	display:inline;
    color:#536880;
    padding-left:10px;
}

.dialogRservBox {	
	border:3px solid #D3D3D3;
	margin-bottom:10px;
}
.dialogRservBox .label {
	background-color:#D3D3D3;
	font-size:1.5em;
	color:#7E7A7A;
	font-weight: bold;
	
}

#dialog_box_console_msg_data{	
	white-space: pre;
	font-family: courier;
	background-color: #000;
	color: #FFF;
	margin: 1px;
	padding: 5px;
	height: 98%;
	overflow: auto;
	
}

ul.ui-autocomplete.queue_search {
	min-width:500px;
	/*background-color:#7E7A7A;*/
	background-image:none;
	
	list-style-type: none;
	/*
	border-top: 1px solid #888;
	border-bottom: 1px solid #B6B6B6;
	border-left: 1px solid #AAA;
	border-right: 1px solid #AAA;
	*/
	padding: 4px 0 4px 4px;
	margin: 0;	
	background-color: #FFF;
	box-shadow: 5px 1px 10px #888;
	border-radius:0px 0px 5px 5px;
	 

}

ul.ui-autocomplete.queue_search li.ui-menu-item{

	border-bottom:1px solid #E8EFFF;
}
ul.ui-autocomplete.queue_search .ui-state-hover{
	background-color:#3668d9;
	background-image:none;
	border-radius:0px;
	color:#ffffff;
	
	
}

</style>




<%if(Config.getValue("tab_body_bg")!=null){%>
<style>
	
	.yui-skin-sam .yui-navset .yui-content  {
		background:none repeat scroll 0 0 <%=Config.getValue("tab_body_bg")%>;  
	}
	
</style>
<%}%>


<%if(request.getParameter("use_ace")!=null){%>
	<script language="JavaScript">
	CODE_EDITOR=CODE_ACE_EDITOR; 
	</script>
<%}%>
<body class="yui-skin-sam" >

<%@ include file="include/user_menu.inc" %>

<!--div align="right" style="display:inline;float:right">
 	<a href="index.jsp">Database Manager</a> 		
</div-->
<div align="left" style="display:inline">		
<%if(Config.getValue("db_testing_version")!=null){%><span style='color:#ff0000'><%=Config.getValue("db_testing_version")%></span><%}%>		
</div>
<table height="40px" style="position:absolute;z-index:100;top:0px;right:10px">
	<tr>
	<td>	
	<div id="statusMessage" align="left" style="display:none" nostyle="padding:5px;   background-color:#F34A29; vertical-align:middle;display:none;margin-bottom:0px"></div>
	</td>
	<td>
	<div id="progressMessage" align="left" style="display:none"  nostyle="padding:5px; background-color:#FFDAB5; border:1px solid #F34A29; display:none; vertical-align:middle;margin-bottom:0px"></div>
	</td>
</tr>
</table>


<div id="tabface" class="yui-navset">   
	<ul class="yui-nav">
	    
	 <li class="selected"><a href="#tab1"><em><B>Tasks</B></em></a></li>   
	 <li><a href="#tab2"><em><B>Queue</B></em></a></li>
	 <li><a href="#tab4"><em><B>Peer Association</B></em></a></li>
	 <li><a href="#tab4a"><em><B>Monitor</B></em></a></li>	 
	 <!--li><a href="#tab3"><em><B>Javascript Debugger</B></em></a></li-->
	  
	<!--li><a href="#tab1"><em><B>Bloomberg Query</B></em></a></li-->
	</ul>
<div class="yui-content">  
<div>
<div>
			<ul class="scd_toolBar">
			<li><a href="#" onclick="scd_toggleTree(this)"><img src="images/tree_toggle.png" ></a></li>	
			<!--  li>				 
				<div style="padding:3px;padding-left:10px ">
					<form action="#" method="post" style="margin:0px" onsubmit="return scd_searchTask(this)">					 
						 Task Name:	 <input type="text" name="scd_keyword"  id="scd_keyword" value="">
						<select id="scd_tagsdropdown" style="">
							<option value="">[Any]</option>					
						</select>
						<input type="submit" value="Search">
						<input type="button" value="Show All" onclick="scd_showAllTasks()">
						 
					</form>			
				</div> 
			</li -->
			<!--  li>
			   <div>
			      <input type="button" value="All" id="scd_showAllBtn"  disabled="disabled" onclick="scd_setAll('yes')">
			      <input type="button" value="Mine" id="scd_showMineBtn" disabled="disabled" onclick="scd_setAll('no')">
			   </div>
			</li -->
			<!-- li>
				<div style="padding:3px;padding-left:10px ">
						<form action="#" method="post" style="margin:0px" onsubmit="return scd_editTask(this)">
							Task ID:<input type="text" size="6" name="scd_edittaskid" value=""><input type="submit" value="Edit">
						</form>
				</div>				
			</li-->
			<li>
					<table border="0" cellpadding="0" width="500px" cellspacing="0">
				     	<tr>
				     		<td width="*"><span id="scd_searchTasks" style="font-size:1.6em"><input type="text" id="scd_searchTasksInp" title="Type Name or ID &#x21B5;" value=""></span> </td>
							<td width="90" align="center"><input  id="scd_searchBtn" type="button" onclick="scd_search()" value="Edit" style="margin-top: 1px;width:70px;border-radius:7px;height:28px;"> </td>
						</tr>
					 </table>
			</li>
			<li><div width="100%" style="padding:3px;padding-left:10px "><a href="#" onclick="scd_refreshFlowChart(); return false;" style="margin-right:20px">Show Flowchart</a> <a  style="margin-right:30px" href='#' onclick='scd_showHideTags(); return false;'>Show/Hide Tags</a></div></li>
		</ul>
</div>
<table width="100%" cellpadding="0">

<tr>	
<td  valign="top" width="*" class="td_folder_tree" height="100%">
	<!--div width="99%" style="border:0px solid #c0c0c0; margin-bottom:5px;min-width:750px ">
		
	 			
	</div-->
	<div id="scd_main_tree" class="chromeScroll" style="height:774px;overflow:auto;margin-right:3px ;width:100%;"></div>
</td>
<td  valign="top"  width="850px" height="100%">
	
 
	   
	       <div id="scd_taskpane_holder">
	       			<ul>		</ul>
	       	</div>
		
  
 	
</td>
</tr>
</table>
</div>


<div desc="queuepane">
   <table width="100%">
		 <tr>
		 	<td width="40%" valign="top" class="roundedBorder" style="border:1px solid #c5c5c5">
		 		
		 		<div style="background-color:#c5c5c5;margin:-4px -4px 0px -4px;-moz-border-radius:4px 4px 0px 0px;border-radius:4px 4px 0px 0px" class="roundedBorder" >
		 		    <h3 style="display:inline; margin-right:50px">Current Task Queue</h3> 
		 		    <div style="display:inline">
		 		       <input type="button" onclick="SchedulerMgmt.getQueueLogs(null,scd_genQLogs)" value="Refresh Queue"> 
		 		       <input style="margin-left:20px" type="button" onclick="scd_executeScript()" value="Execute R Script">
		 		       <a style="margin-left:20px" href="#" onclick="scd_dialogQueueSetting(); return false;"><img src="images/queue_alert_setting.png" border="0"></a>
		 		    </div>
		 		</div>
				<div style="height:700px;width:auto;overflow:auto">	
					<div id="scd_qLogsList"></div>
				</div>
			</td>
			<td width="60%" valign="top">
				<div style="border:1px solid grey;margin-bottom:5px" class="roundedBorder">
				     
				   <div style="background-color:#c5c5c5;margin:-4px -4px 0px -4px;-moz-border-radius:4px 4px 0px 0px;border-radius:4px 4px 0px 0px;padding:4px" >
						<strong>  Server Time:</strong>  <span style="margin-right:50px" id="timecontainer"></span>
						<!--<strong>   Scheduler Status:</strong>   <span id="schedulerstatus"></span> 
						<span style="margin-left:50px"><strong>Host:</strong>  </span> <span id="scd_hostname"></span>-->
						<!--span style="margin-left:50px"><strong>Peers Online:</strong>  </span> <span id="scd_onlinepeers"></span-->
						<span style="margin-left:30px"><a href="#" onclick="scd_showPeersInfo()">Peers Info</a></strong>
						<span style="margin-left:30px"><a href="#" onclick="scd_showPeerPackages()">Peer Pkgs</a></strong>
					</div>
					<div id="scd_onlinepeers"></div>		
				</div>
				
				<form action="#" onsubmit="scd_showHistory(); return false;">				
                <table width="100%" cellpadding="0" cellspacing="0">
                	<tr><td>	
					<div style="border:2px solid grey;background-color:#E4E4E4;padding:0px;-moz-border-radius:4px 4px 0px 0px;border-radius:4px 4px 0px 0px" class="roundedBorder" align="middle" >
					<div style="background-color:#c5c5c5;display:inline-block;width:100%;">
					  <div style="display:inline; margin-left:10px;float:left;font-size:1.3em"><strong>Queue History</strong></div>
					  <div style="display:inline;float:right;"> <input type="submit" value="Get History"></div>
					 </div> 
                	<table cellpadding="5" cellspacing="0" width="100%" >
                    <tr>
                    	<!--  td align="right" valign="middle">Field:</td-->
                        <td valign="top" style="border-right:1px solid #c5c5c5">
                        	<div style="width:135px"> 
                        	<select id="scd_filterfield" >
                        		  <option value="">--Select Field--</option>
								  <option value="name">Name (contains)</option>
								  <option value="scheduler_id">Task ID</option>								  
								  <option value="host">Peer</option>
								  <option value="tag">Tag</option>	
                        	</select>                        	
							    <span> <input id="scd_filterfieldval" size="25" type="text" value="" style="display:none;margin-top:3px"></span>
								<select id="scd_filterdd" style="display:none;margin-top:3px"></select>
							</div>
							
                        </td>
						
                    	<!-- td align="right" valign="middle">Type:</td-->
						<td valign="top" style="border-right:1px solid #c5c5c5">
                             <select  id="scd_typefilter">
                             	<option value="" selected> -----Select Task Type------</option>
                             	<%
								  ScheduledTaskFactory stf=new ScheduledTaskFactory();
								  List<ScheduledTask>  sts=stf.getAllConfiguredTasks();
								  for(Iterator<ScheduledTask> i=sts.iterator();i.hasNext();){
								  		ScheduledTask st=i.next();
								      								  
								%>							
								 <option value="<%=st.getUniqueid()%>"><%=st.getName()%></option>
								 <%}%>
                             </select>	
                        </td>
                    	
                    	<!-- td align="right" valign="middle">Period:</td-->
                        <td valign="top" style="border-right:1px solid #c5c5c5">
                            <select onchange="onDateOption(this)" id="scd_datefilter">
                                <option value="" selected> -----Select Period------</option>
                                <optgroup label="Specifiy Range">
                                    <option type="daterange" value="daterange">Date Range</option>
                                    <option type="datefrom" value="datefrom">Date From</option>
                                    <option type="number" value="ndays">Last n Days</option>
                                    <option type="number" value="nweeks">Last n Weeks</option>
                                    <option type="number" value="nmonths">Last n Months</option>
                                    <option type="number" value="nyears">Last n Years</option>
                                </optgroup>
                                <optgroup label="Recent">                               
								    <option selected type="" value="last1Hour">Last 1 hour</option>
									<option type="" value="last2Hour">Last 2 hours</option>
									<option type="" value="last5Hour">Last 5 hours</option>
									<option type="" value="last10Hour">Last 10 hours</option>									
								    <option type="" value="last1Day">Last 1 day</option>										
									<option type="" value="last2Day">Last 2 days</option>
									<option type="" value="last3Day">Last 3 days</option>
                                    <option type="" value="last1Week">Last 1 Weeks</option>
                                    <option type="" value="last10Week">Last 10 Weeks</option>
                                    <option type="" value="last1Month">Last 1 Month</option>
                                    <option type="" value="last2Month">Last 2 Months</option>
                                    <option type="" value="last3Month">Last 3 Months</option>
                                    <option type="" value="last6Month">Last 6 Months</option>
                                    <option type="" value="last12Month">Last 12 Months</option>
                                    <option type="" value="last18Month">Last 18 Months</option>
                                    <option type="" value="last24Month">Last 24 Months</option>
                                    <option type="" value="last3Year">Last 3 Years</option>
                                    <option type="" value="last4Year">Last 4 Years</option>
                                    <option type="" value="last5Year">Last 5 Years</option>
                                    <option type="" value="last10Year">Last 10 Years</option>
                                </optgroup>
                           </select>
                            <div  style="display:inline-block;" id="dateOptionArea">
                            </div>

                        </td>
                        <!--  td align="right" valign="middle">Status:</td-->
						<td valign="top">
                             <select  id="scd_statusfilter">
                             	 <option value="" selected> -----All Status------</option>
                             	 
								 <option value="success">Success</option>
								 <option value="re-executed">Re-Executed</option>
								 <option value="fail" >Error/Warning</option>
								 <option value="{null}"> Failed </option>
								 <option value="#success">Non-success</option>
								 
                             </select>	
                        </td>
	 
                        <!--  td valign="middle">
                           
                        </td-->
						
                    </tr>    
                
					</table>
					<div>
					</td>
				    </tr>
					<tr>
						<td colspan1="7">
							
							<div style="height:700px;width:auto;overflow1:auto;">	
							<div id="scd_histLogsList" class='log_table'></div>
							</div>
							
						</td>
					</tr>
                </table>
				</form>   

			</td>
		</tr>
	</table>
</div>
   
<div desc="peersTasks">	

	<table width="100%">
	<tr>	    
		
		<td width="*">
			<div id="peersToolBar">	</div>
		</td>
		<td width="200px" rowspan="2">
		  <div id="unixPeerSetup" class="unixPeerSetup"></div>
		</td>
			
	</tr>
	<tr>
	   <td>
		<div id="dialog_box_peermatrix_data"  style="hight:750px"></div>	
		</td>
	</tr>
	</table>
</div>




<div desc="queueMonitor">	
	<div id="scd_monitors-tabs" style="height:800px;width: 100%;">
		<%=new SchedulerReportClient().generateMarkup()%>
	</div>
</div>
   	
 
   

</div> <!--yui-content-->
</div><!--class="yui-navset"-->


 <div id="scd_logDialogbox" title="Execution Log" style="display:none">
    <!--  div class="hd">Logs</div-->
    <!--  div class="bd"-->
    	<form>
    	<div id="scd_logDialogboxBD">please wait....</div>
		</form>
		<div id="scd_logDialogboxMsgs" style="display:none"></div>
     <!--  /div -->
</div>

 <div id="scd_manageFolders" style="display:none">
    <div class="hd">Manage Folders</div>
    <div class="bd">    	 
		<div style="padding:10px;" id="scd_manageFoldersbd"></div>
		<div><small>Subfolders can be entered as "Root/Subfolder/Sub Subfolder" and create parent folders first.  Exmple: "Root", "Root/Subfolder" and then "Root/Subfolder/Sub Subfolder"</small></div>
    </div>
</div>


 <div id="scd_managePeerAssoc" style="display:none">
    <div class="hd">Peer Associations</div>
    <div class="bd">    	 
		<div style="padding:10px;" id="scd_managePeerbd"></div>		
    </div>
</div>



<div id="bb_pluggin">

<form id="dld_queryEditorForm1" onsubmit="return false;" style="margin:0px">
	<table class="widgetBox" id="dld_editor1"  style="display:none">
		 
		<tr>								
			<td align="right" valign="top"><strong>Database:</strong></td>
			<td ><input type="radio" id="dld_mkt_securites1" name="dld_databaseoption1" value="1" checked>Market Securities <br>
			<input type="radio"  id="dld_mkt_contracts1"  value="2" name="dld_databaseoption1">Market Contracts</td>
		</tr>		
		<tr>
			<!--td colspan="2">Last <input id="lastnmonthsxl" size="4" type="text" value="120"> Months<div id="fieldTablesxl"></div></td-->
			<td  align="right"><strong>Date:</strong></td>
			<td>
				<select onchange="onDateOption(this)" id="dld_datefilter1">
					<option value="" selected>-----Date Range------</option>
					<optgroup label="Specifiy Range">
						<option type="daterange" value="daterange">Date Range</option>
						<option type="datefrom" value="datefrom">Date From</option>
						<option type="number"  value="ndays">Last n Days</option>
						<option type="number"  value="nweeks">Last n Weeks</option>
						<option type="number"  value="nmonths">Last n Months</option>
						<option type="number"  value="nyears">Last n Years</option>
					</optgroup>
					<optgroup label="Recent">
						<option type=""  value="ndays$1">Last 1 day</option>
						<option type=""  value="nweeks$1">Last 1 Weeks</option>
						<option type=""  value="nweeks$10">Last 10 Weeks</option>
						<option type=""  value="nmonths$1">Last 1 Month</option>
						<option type=""  value="nmonths$2">Last 2 Months</option>
						<option type=""  value="nmonths$3">Last 3 Months</option>
						<option type=""  value="nmonths$6">Last 6 Months</option>
						<option type=""  value="nmonths$12">Last 12 Months</option>
						<option type=""  value="nmonths$18">Last 18 Months</option>
						<option type=""  value="nmonths$24">Last 24 Months</option>
						<option type=""  value="nyears$3">Last 3 Years</option>
						<option type=""  value="nyears$4">Last 4 Years</option>
						<option type=""  value="nyears$5">Last 5 Years</option>
						<option type=""  value="nyears$10">Last 10 Years</option>
					</optgroup>
				</select>
			   <div style="margin-top:5px" id="dateOptionArea"></div>
			 <!--input type="button" value="test" onclick="alert(DWRUtil.toDescriptiveString(getDateInputs('tr_datefilter'),2))"/-->
			 </td>
		</tr>
		<!--tr id="dld_commoditybox" style="display:none">
			<td align="right">
				 <strong>Commodity:</strong><br>									 
			</td>	 
			<td align="left">
				<input id="dld_commodity" type="text"	size="20"/>
			</td>
		</tr-->
		 
		 
		<tr>
			<td width="50%" valign="top" align="right">
				<strong>Tickers:</strong><br><small style="width:150px">for multiple tickers <br>use either comma (,) or Enter.  <br>Leave empty <br>if you do not want<br> to specify</small>
			</td>
			<td width="50%" >
				<textarea id="dld_contracts1" cols="50" rows="12"></textarea>
			</td>	
		</tr> 
		<tr>
			<td width="50%" align="right">
				<strong>Identifier Suffix:</strong>
			</td>
			<td width="50%" >
				<input id="dld_marketsector1" type="text"	size="20"/>
				<select id="sec_id_suffix" onclick="bbpl_dld_suffixValidate(this)">
					<option value=""></option>
					<option value="Comdty">Comdty</option>
					<option value="Index">Index</option>
					<option value="Curncy">Curncy</option>
					<option value="Equity">Equity</option>
					<option value="Govt">Govt</option>
					<option value="Corp">Corp</option>
					<option value="Mtge">Mtge</option>
					<option value="M-Mkt">M-Mkt</option>
					<option value="Muni">Muni</option>
					<option value="Pfd">Pfd</option>								
				</select>
			</td>	
		 
		<tr>
			<td width="50%" valign="top" align="right">
				<strong>Fields to Synchronize:</strong><br><br>
				<small>To maintain dropdown list items <a href="#" onclick="return dld_modifyFields()">click here</a></small>
			</td>
			<td>
				
				<table width="auto"  cellspacing="0" id="dld_fields1" border="1px solid gray">
					 
					<tr> 									 
						<td><select id="dld_blbfield1"></select></td>
						<td><input type="image" src="images/button_plus.gif" onclick="return bbpl_field_UI_AddRemove(this)"></td>
					</tr>										
					
				</table>
				
			</td>
		</tr>
	</table>
	</form>

</div>

<div style="display:none" id="dld_fieldMappingPane">	
			<div class="hd">Field Mapping</div> 			
			<div class="bd">
				<div style="height:500px; overflow:auto">
				<table id="dld_fieldMappingDiv" width="100%">
					<tr>
						<td><b>DB Field Name:</b></td>
						<td><b>Bloomberg Fieldname</b></td>
						<td>&nbsp;</td>
					</tr>
					<tr id="dld_fieldAddRow">
						<td><input type="text" value="" id="dld_fieldAdd1"> </td>
						<td><input type="text" value="" id="dld_fieldAdd2"></td>
						<td><input type="button" value="Add" onclick="dld_addFieldMap()"></td>
					</tr>				
				</table>
				</div>
	</div>
														
</div>	
 
<div id="dlg-msg" title="Next 20 Triggers"><div id="dlg-msg-bd"></div></div>	
 
<div id="dialog_box_pstat" title="Peers Information" style="display:none">
	<div id="dialog_box_pstat_data"  style="hight:750px"></div>	
</div> 

<div id="dialog_box_explog" title="Execution Logs" style="display:none">
	<div id="dialog_box_explog_data"  style="hight:650px"></div>	
</div> 

<div id="dialog_box_tags" title="Tags" style="display:none">
	<div id="dialog_box_tags_data"></div>	
</div> 

<div id="dialog_box_console_msg" title="Console Messages" style="display:none">
	<div id="dialog_box_console_msg_data"></div>	
</div> 


<div id="dialog_box_svnScript" style="display:none">
	<h3 id="dialog_box_svnScriptHD">Revision:<h3>
	<pre id="dialog_box_svnScriptBD"  style="width:700px;height:500px;overflow:auto;border:1px inset #ddd;padding:5px"></pre>
</div> 

<div id="dialog_runTask" title="Execute Task" style="display:none">
	<div style="font-size:1.4em;padding:20px;" align="center">   	
	  Delay:<input id="dialog_runTask_delay" type="text" value="0" name="delay_in_minutes" size="4"> minutes
   </div>
</div>

<div id="dialog_peerCommand" title="Send Command To Peer " style="display:none">
	<div style="font-size:1.4em;padding:20px;">   	
	   <div style="margin:20px 5px;border:2px solid grey; padding:5px;text-align: center;"><a style="margin-right:20px;" href="#" cvalue='stop' onclick="scd_sendPeerCommand(this); return false;">Stop</a>
	        <a href="#" cvalue='start' style="margin-right:20px;" onclick="scd_sendPeerCommand(this); return false;">Start</a>
	   </div>
	   <div style="margin:10px 5px;border:2px solid grey; padding:5px;text-align: center;">Send Command: 
	        <textarea id="peerCommandInput" cols="30" rows="3"></textarea>
	        <input type="button" value='Send Command' cvalue="command" onclick="scd_sendPeerCommand(this); return false;">
	   </div> 
	   <div id="peerCommandOutput" style="height:200px;width:450px;overflow:auto;font-size:.5em;white-space: pre;">
	   </div> 
	    
   </div>
</div>


<div id="dialog_Rserveconfig" title="Rserve on Unix - Configuration" style="display:none">
      <div class='dialogRservBox'>
	     <div class='label'>Settings</div>
     	 <div style="display:inline-block;padding:10px">
     	   <form action="#" onsubmit="scd_rservesettings(); return false;">
     	 	<div style="display:inline">Concurrent sessions :<input size="4" id="rserve_noofsessions" type="" value=""></div>
     	 	<div style="display:inline">Max no of scripts per session:<input size="4"  id="rserve_noofexec" type="" value=""></div>
     	 	<div style="display:inline"><input type="submit" value="Save"></div>
     	 	<div style="font-size:.8em;color:red">Changing these values required executeR peers to be restarted to take effect</div>
     	 	<input type="hidden" value="" id="rserve_peer"/>
     	 	</form>
     	 </div>
     </div>
     <div class='dialogRservBox'>
	     <div class='label'>RServe Sessions</div>
		 <div id="dialog_Rserveconfig_proc"><ul class="Rprocesses"></ul></div>
	 </div>
</div>


<div id="dialog_queueSettings" title="Queue Settings " style="display:none">
	<div style="font-size:1.4em;padding:20px;"> 
	  	<h3 class="scd_subheader" style="margin-top:5px;margin-bottom:2px;">Script Timeout Alert</h3>	  	
 		<table width="100%" cellpadding="5" cellspacing="5">
 			<tr>
 			    <td>Expiry Criteria</td>
 			    <td colspan="2"><textarea id="scd_qx_criteria" name="criteriaQuery" cols="80" size="40"></textarea> 			     
 			</tr>
 			<tr>
 			    <td>IF Below</td>
 			    <td><input type="textbox" id="scd_qx_certain_mins" name="fewerminutes"/> Minutes </td>		
 			    <td>Expires after <input type="textbox" id="scd_qx_certain_minsexpiry" name="fewerminutesexpiry"/> Minutes </td>		     
 			</tr>
 			<tr>
 			    <td>Otherwise </td>
 			    <td colspan="2"><input type="textbox" id="scd_qx_otherwise" name="elsecritieriaxtime" /> x Times </td>		
 			</tr>
 		</table>
 		<small>Please note that every script has its on alert type</small>
	    <h3 class="scd_subheader" style="margin-bottom:2px;">Slow Queue Alert</h3>
	    <div style="display:block;">
	    	<div style="float:right;padding-top:10px;margin-right:15px">
	    	   <h3><u>Alert</u></h3>
	    	   <div>Theme:<select id="scd_qx_delayalert_theme" name="alert_theme"></select></div>
	    	   <div>Type :
	    	         <select id="scd_qx_delayalert_type" name="alert_type">
	    	             <option value="Email">Email</option>
	    	             <option value="Phone">Phone</option>
	    	             <option value="No Alarm">No Alert</option>
	    	         </select> 
	    	    </div>
	    	 </div>
	    	<div id="scd_qx_delayalert"></div>
	    </div>
	    <div><input type="button" value="Save" onclick="scd_queueSettingSave()" style="float:right"></div>
   </div>
</div>



<div id="dialog_box_peernbox" title="Peer Notes" style="display:none">
	<div id="dialog_box_peernbox_name"></div>
	<div>
		<table>
		<tr><td align="right">Friendly Name:</td><td><input type="text" size="10" name="dialog_box_peernbox_fname" id="dialog_box_peernbox_fname"><small> Max 10 chars</small></td></tr>
		<!--  tr><td align="right">Contact Email:</td><td><input type="text" size="50" name="dialog_box_peernbox_mail" id="dialog_box_peernbox_mail"></td></tr-->
		<tr><td align="right">Notes:</td><td><textarea cols="50" rows="10" id="dialog_box_peernbox_notes"></textarea></td></tr>
		</table>
	</div>	
	<div align='right'><input value='Save' type='button' onclick='peerNotesEditSave()'></div>
</div> 


<div id="dialog_commit" title="Comments" style="display:none">
   <div class="label">   	
	Comments:	
   </div>	
	<div>   	
	  <textarea id="dialog_commit_msg" style="width:100%;height:250px"></textarea>
   </div>
</div>

<div id="dialog_trash" title="Trash" style="display:none">  
	<div id="dialog_trash_bdy" style="height:500px; overflow:auto">   	
	   
   </div>
</div>

<div id="dialog_peerAsHist" title="History" style="display:none">  
	<div id="dialog_peerAsHist_bdy" style="height:700px; overflow:auto">   	
	   
   </div>
</div>


<%@ include file="include/login.html" %>


<script language="JavaScript">
	//scd_parseSchedulerPane();
	<%if(request.getParameter("scheduler_id")!=null){%>
		scd_direct_open('<%=request.getParameter("scheduler_id")%>');
	<%}else{%>	
	scd_pageinit();
	<%}%>
	
	
</script>
</body>
</html>
