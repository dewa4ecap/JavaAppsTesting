 <%@page import="com.fourelementscapital.executer.common.ExecuteRMgmt"%>
 <%@page import="com.fourelementscapital.p2p.websocket.*"%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
<title>Execute R</title>

<link rel="shortcut icon" href="images/executer.png" type="image/x-icon">
<link rel="icon" href="images/executer.png" type="image/x-icon">

<style>
body {
	font-family:Arial, Verdana, Geneva, Helvetica, sans-serif;
	font-size:medium;
}
</style>
 
<link rel="stylesheet" type="text/css" href="jquery-1.8.2/css/cupertino/jquery-ui-1.8.2.custom.css" />

<script type='text/javascript' src='dwr/engine.js'></script>
<script type='text/javascript' src='dwr/util.js'></script>
<script type='text/javascript' src='dwr/interface/ExecuteRMgmt.js'></script>

 
<script language="JavaScript" type="text/javascript" src="jquery-1.8.2/js/jquery-1.4.2.min.js"></script>
<script>
     var $j = jQuery.noConflict();	 
</script>

<script language="JavaScript" type="text/javascript" src="jquery-1.8.2/js/jquery-ui-1.8.2.custom.min.js"></script> 
<script language="JavaScript" type="text/javascript" src="jquery-1.8.2/jquery-treeview/jquery.cookie.js"></script>
<script language="JavaScript" type="text/javascript" src="jquery-1.8.2/templates/jquery.template.js"></script>
<script language="JavaScript" type="text/javascript" src="js/executeR.js"></script>

<style>

#queueTable .executing {
	/*background-color:red;*/
}

#queueTable .finished td:first-child  {
	background:url(images/task_executed.gif) no-repeat ;
	background-position:center center;
	opacity:0.7;
}
#queueTable .timedout td:first-child  {
	background:url(images/task_timedout.png) no-repeat 25% 5%;
	background-position:center center;
}
#queueTable .executing td:first-child {
	background:url(images/task_executing.gif) no-repeat 25% 5%;
	background-position:center center;		
}
#queueTable tr.executing {
	color:#308330;
	
}


#queueTable tr td,  #queueTable tr th {
	border-left:1px solid #CFCFCF;
}
tr:nth-child(even) {background: #f5f5f5;}
tr:nth-child(odd) {background: #efefef;}
 
 #queueTable tr th {
 	background-color:#CFCFCF;
 	padding:5px;
 	box-shadow:1px 2px 3px #C0C0C0;
 	
 }
 #queueTable tr td { 	
 	padding:2px;
 }
 

#queueTable tr td:first-child,  #queueTable tr th:first-child {
	border-left:none;
}


.er_mainbox {
	border: 5px solid #CFCFCF;
	box-shadow:-1px -1px 15px #d0d0d0;	
	border-radius: 10px 10px 10px 10px;
	overflow-x:hidden;
	overflow-y:auto;
	height:850px;

}


.chromeScroll {
	overflow:auto;	
	-moz-box-shadow: 5px 5px 5px #888;
	-webkit-box-shadow: 5px 5px 5px #888;
	box-shadow: 5px 5px 5px #c0c0c0;
}


.chromeScroll::-webkit-scrollbar {
	width: 10px;
	height: 5px;
}

.chromeScroll::-webkit-scrollbar-track-piece {
	background-color:#929292;
	-webkit-border-radius: 20px;
}
.chromeScroll::-webkit-scrollbar-track-piece:vertical { 
	
}

.chromeScroll::-webkit-scrollbar-thumb:vertical {
	height: 10px;
	width:5px;
	background-color: #757575;
	-webkit-border-radius: 3px;
}
.chromeScroll::-webkit-scrollbar-thumb:horizontal {
	width: 5px;
	background-color:  #757575;
	-webkit-border-radius: 3px;
}

.er_counterbox_dv {
	text-align:center; 
	color:#2779AA;
	font-size:17px;
	text-decoration:underline;
	margin-top:50px;
	margin-left:10px;
	padding:10px;
}

.er_counterbox { 
	border:3px solid #e0e0e0;
	background-color:#e0e0e0;	 
	margin:0px 0px 10px 10px;
	border-radius:8px;
	box-shadow: 5px 5px 8px #c0c0c0;	
}

.er_counterbox .label{
	background-color:#e0e0e0;
	text-align: center;
	vertical-align: middle;
	font-size:12px;
	border-radius:8px 8px 0px 0px;
	padding:2px;
}
.er_counterbox .countbg{
	background-color:#858584;
	padding:2px 0px;
	border-radius:0px 0px 8px 8px;
	
}
.er_counterbox .count{
	font-size:60px;
	color:white;
	font-weight: bold;
	text-align: center;
	vertical-align: middle;
	/*height:80px;*/
}

#er_timedout_count {
	color: #F8A5A5;
}

.focused_out {
	opacity:0.25;
}
</style>
 
 
</head>
<body>
<table height="40px" style="position:absolute;z-index:100;top:0px;right:10px;display:none" border="0" >
	<tr>
	<td>	
	<div id="statusMessage" align="left" style="display:none"></div>
	</td>
	<td>
	<div id="progressMessage" align="left" style="display:none"></div>
	</td>
</tr>
</table>
<div style="height:30px"></div>

<div style="width:100%;display:inline-block;">

	 <div style="float:left;width:89%;" class="er_mainbox chromeScroll">
		<table id="queueTable" width="100%" border="0" cellspacing="0px" cellpadding="0px">
			<thead>
				<tr>
					<th width="20px"></th>
					<th width="120px">Queued Time</th>
					<th width="120px">Sender</th>			
					<th width="150px">Executing In</th>
					<th width="*">Script</th>
				</tr>		
			</thead>
			<tbody>	
			</tbody>
		</table>
		
	</div>

	<div  style="float:left;width:10%;" >

	   <div class="er_counterbox" >
	            <div class="label"># of Queued</div>
	            <div class="countbg"><div id="er_queue_count" class="count"></div></div>
	   </div>
	   	<div class="er_counterbox" >
	            <div class="label"># of Executing</div>
	            <div class="countbg"><div id="er_executing_count" class="count"></div></div>
	   </div>
	   	<div class="er_counterbox" >
	            <div class="label">Options</div>
	            <div class="countbg">
	            	<input type="checkbox" onclick="er_dontremove()" id="er_no_remexec" name="er_no_remexec" value="">Do not remove executed 
	            </div>
	   </div>
	   
	   <div class="er_counterbox_dv">Last 30 Seconds</div>
	  <div class="er_counterbox" >
	            <div class="label">Success</div>
	            <div class="countbg"><div id="er_finised_count" class="count"></div></div>
	   </div>
	   <div class="er_counterbox" >
	            <div class="label">Ave Delay(secs)</div>
	            <div class="countbg"><div id="er_ave_delay" class="count"></div></div>
	   </div>
	   
	   <div class="er_counterbox" >
	            <div class="label">Timedout</div>
	            <div class="countbg"><div id="er_timedout_count" class="count"></div></div>
	   </div>
	   <div class="er_counterbox" >
	            <div class="label">Active Peers</div>
	            <div class="countbg"><div id="er_peer_count" class="count"></div></div>
	   </div>
	</div>
</div>
<script language="JavaScript">
   <%
	if(request.getParameter("classic")!=null){
		out.println("er_pageinit();");
	}else{
		out.println("er_wsocketinit();");
	}
    %>	
</script>


</body>
</html>