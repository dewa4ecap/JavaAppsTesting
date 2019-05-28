//Main tab initialization 
var mainTabView=null;

//progress bar for each running task, ie. percentage of completed 
var show_progressBar=true;

//to cache group icons & colors
var scd_groupIcons=new Object();
var scd_groupColor=new Object();

//auto suggestion for task input
var scd_tasks_autosug=new Array();
var temp_slist=null;

var CODE_EDITOR_DEFAULT="default";
var CODE_ACE_EDITOR="ace";
var CODE_EDITOR= CODE_ACE_EDITOR; // CODE_ACE_EDITOR;
var ACE_EDITORS=new Object();

//useful when debugging, for example 
var IGNORE_LIVEUPDATE_PAGE=false;

var scd_spaneparsed=false;
var loadedTabs=new Array();

function scd_pageinit(ctab) {
    
    if(ctab==null){
        ctab=-1;
    }else{
        loadedTabs[ctab]=true;
    }
    var respBack = function(data){
                
        progress_message(null);
        
        if(data.group_icons!=null){
            scd_groupIcons=data.group_icons;
        }
        if (data.group_colorcodes != null) {
            scd_groupColor=data.group_colorcodes;
        }
         
        if (data.activetab != null && data.activetab >= 0) {            
            loadedTabs[data.activetab]=true;
            mainTabView.set('activeIndex', data.activetab);
        }
        
        if (scd_tzlist == null) {
            if(data.timezones!=null){
                scd_timeZones=data.timezones;
            }
        }   
        
        // it should be above tree generation, because tag items use populated
        // values set by scd_tagPopulate function.
        if(data.tags!=null){
            scd_tagPopulate(data.tags,data.showtags);
        }
        
        //to generate tree menu with list of scheduler items.
        if (data.listtasks!=null) {
            $j(treeSelector).html("<span style='color:#E01B3F;padding:20px 10px 10px 10px;font-size:1.2em;'>Please wait while building tree..</small>"); 
            setTimeout(function(){scd_treeListGen(data.listtasks);},10);
        }
        
        if (scd_showTagsFlag == 1) {$j(".task_holder").show();}else{$j(".task_holder").hide();  }
        
        if(data!=null && data.field_mapping!=null){
            field_mapping=data.field_mapping;   
        }
        
        //data to produce peer association tab.
        if(data.peersdata!=null) {
            peerAssociationMatrix(data.peersdata);
            setTimeout("showExecutingQueue()", 500);
        }
        
        //to online peers.
        if(data.peerslist!=null){   
                
            for (iab = 0; iab < data.peerslist.length; iab++) {
                var tdata=data.peerslist[iab];
                //ensure not null
                if($j("#scd_onlinepeers div[peername='"+tdata.peername+"']") != null) {
                	if($j("#scd_onlinepeers div[peername='"+tdata.peername+"']").length==0){                
                        $j("#scd_onlinepeers").append("<div title='"+(tdata.friendlyname==null?tdata.peername:tdata.friendlyname+"("+tdata.peername+")")+"|Please wait..' class='peer' peername='"+tdata.peername+"'>"+(tdata.friendlyname==null?tdata.peername:tdata.friendlyname)+"</div>");
                    }
                }
            }
            
            $j("#scd_onlinepeers div.peer").cluetip({
                splitTitle: '|',cluetipClass: 'jtip', arrows: true,width:400, dropShadow: true,
                onShow:function(ct,c){
                    var respBack=function(data){
                        $j(c).html("");
                        for(ky in data){
                            $j(c).append("<div style='font-size:1.1em'><pre>"+data[ky]+"</pre></div>");
                        }
                    }
                    $j(c).html("<img src='images/loading.gif'>Loading... ");
                    SchedulerMgmt.temp_QueueDDDDDDDetail($j(this).attr("peername"),respBack);
                }
            });
        }
        $j("#scd_monitorImg").attr("src","image.jsp?"+new Date().toUTCString());
        try{
            setInterval(function(){SchedulerMgmt.editorActiveDetected(scd_getOpenedEditableScripts(),function(){})},90000);
        }catch(e){
            if(typeof console!='undefined'){
                console.errro("Error while setInterval() "+e);
            }
        }
        if (data.qlogs != null) {
            scd_genQLogs(data.qlogs);
        }
    }
    progress_message("Please wait...");
    SchedulerMgmt.loadInit(ctab,respBack);
    if (mainTabView == null) {
        mainTabView = new YAHOO.widget.TabView("tabface");
        mainTabView.on("click", onSCDClickTabView);
    }
        
    var changeFucn=function(objval){
        $j("#scd_filterdd").hide();
        $j("#scd_filterfieldval").hide();
        var optsel=$j(objval).val();
        
        $j("#scd_filterfieldval").val("");
        if(optsel==''){         
            $j("#scd_filterfieldval").hide();
            $j( "#scd_filterfieldval" ).autocomplete({disabled:true});          
        }else if(optsel=='tag' || optsel=='host'){          
            var respBack=function(data){
                $j("#scd_filterdd").show();
                $j("#scd_filterdd").val("");
                $j("#scd_filterdd").find('option').remove();                
                if(data!=null){                 
                    for(iab=0;iab<data.length;iab++){
                        var optval=data[iab].split("|")
                        if(optval.length>1) $j("#scd_filterdd").append("<option value='"+optval[0]+"'>"+optval[1]+"</option>");
                    }
                }
            }           
            SchedulerMgmt.getQueryDrDown(optsel,respBack);
        }else if(optsel=='name' || optsel=='scheduler_id'){
            $j( "#scd_filterfieldval" ).show();
            $j( "#scd_filterfieldval" ).autocomplete({
                  disabled:false,               
                  source: function( request, response ) {
                    $j.ajax({
                      url: "autocomplete_scheduler.jsp",
                      dataType: "jsonp",
                      data: optsel=='name'? {search_name: $j( "#scd_filterfieldval" ).val()}:{search_id: $j( "#scd_filterfieldval" ).val()},
                      success: function( data ) {                         
                          response( $j.map( data, function( item ) {
                          return {                          
                            label: "<div style='display:inline-block;width:60px'>["+item.split("|")[0]+"]</div>"+item.split("|")[1],
                            value: optsel=='name'?item.split("|")[1]: item.split("|")[0]
                          }
                        }));
                      }
                    });
                  },
                  minLength: 2,
                  select: function( event, ui ) {
                    console.log( ui.item ?
                      "Selected: " + ui.item.label :
                      "Nothing selected, input was " + this.value);
                  },
                  open: function(event,ui) {
                    var widg=$j(this).autocomplete("widget");
                    if(!widg.hasClass("queue_search")) widg.addClass("queue_search");
                  },
                  close: function() {
                  }
             });
        }else{      
            $j( "#scd_filterfieldval" ).autocomplete({disabled:true});
            $j("#scd_filterfieldval").show();
            $j("#scd_filterfieldval").val("");          
        }
    } // var changeFucn=function(objval)
    
    $j("#scd_filterfield").bind("change",function(evt,tobj){
         changeFucn(this)
    });
    
    $j("#scd_filterfield").val("");
    $j("#scd_filterfieldval").val("");
    changeFucn("#scd_filterfield");

    $j("#scd_taskTab" ).tabs();
     
    $j( "#scd_monitors-tabs" ).tabs({ 
        select: function( event, ui ) {
            if($j(ui.panel).children()[0].nodeName=='IFRAME' || $j(ui.panel).children()[0].nodeName=='IMG'){
                var src_url=$j(ui.panel).children().attr("src");
                var file_p=$j(ui.panel).children().attr("fileparam");
                src_url=src_url.split("?")[0]+"?file="+file_p+"&t="+new Date().getTime();
                
                var child = $j(ui.panel).children();
                child.fadeOut(1000, function () {
                    child.attr("src",src_url);                 
                    child.fadeIn(1000);
                });
            }
        }
    }).addClass( "ui-tabs-vertical ui-helper-clearfix" );
    $j( "#scd_monitors-tabs li" ).removeClass( "ui-corner-top" ).addClass( "ui-corner-left" );
}

var scd_TagColors=new Object();
var scd_showTagsFlag=1;
function scd_tagPopulate(tags,flag){
    if ($j("#scd_tagsdropdown option").length == 1) {
        for (ic = 0; ic < tags.length; ic++) {
            
            var tg=$j.trim(tags[ic].tagname);                                   
            tg1=(tg.indexOf("-")>0)?tg.substring(tg.indexOf("-")+1):tg;
            $j("#scd_tagsdropdown").append("<option value='"+tags[ic].id+"'>"+tg1+"</option>");
            scd_TagColors[tags[ic].tagname]=tags[ic].background_color;      
        }
    }
    if (flag != null) {
        scd_showTagsFlag = flag;
    }
    
}

function scd_showHideTags(){
    if(scd_showTagsFlag==1){
        SchedulerMgmt.setShowTags(0,function(data){});
        $j(".task_holder").hide();
        scd_showTagsFlag=0;
    }else{
        SchedulerMgmt.setShowTags(1,function(data){});
        $j(".task_holder").show();
        scd_showTagsFlag=1;
    }
}

function updateItemPrivilgeNotification(themes, tags, itemid, respBack) {
    SchedulerMgmt.getItemPrivilegeNotifications(themes,tags,respBack);
}

function scd_followThisFun(follow,thisobj,  data,sc_id,panel_id){
     
    var respBack=function(data,sc_id,panel_id){
        var socialbtn="<input type='button' class='scd_followBtn' value='Follow This Script' onclick='scd_followThisFun(true,this)' panel_id='"+panel_id+"'  scheduler_id='"+sc_id+"' user='"+data.authorizedUser+"'>";
        var htm="";
        if (data.followers != null) {           
        
            for(ic=0;ic<data.followers.length;ic++){
                htm+="<li>"+data.followers[ic]+"</li>";
                if(data.authorizedUser!=null && data.followers[ic]!=null&& data.authorizedUser.toLowerCase()==data.followers[ic].toLowerCase()){
                    var socialbtn="<input type='button' value='Unfollow This Script' class='scd_followBtn' onclick='scd_followThisFun(false,this)' panel_id='"+panel_id+"'  scheduler_id='"+sc_id+"' user='"+data.authorizedUser+"'>";
                }
            }       
            htm="<ul>"+htm+"</ul>";          
        }
        socialbtn=(data.authorizedUser!=null)?"<div class='scd_socialbtn'>"+socialbtn+"</div>":"";                  
        $j(panel_id+" .following_panel").html(htm+socialbtn);

    }
    
    if (data != null) {     
        respBack(data,sc_id,panel_id);
        return;
    }else {         
        var scheduler_id= $j(thisobj).attr("scheduler_id");
        var usr = $j(thisobj).attr("user");
        var panel_id = $j(thisobj).attr("panel_id");            
        progress_message("Please wait...");
        SchedulerMgmt.followFunction(follow, scheduler_id, usr, function(data){progress_message();respBack(data,scheduler_id,panel_id);});
    }
}

function onSCDClickTabView(){
    var active=this.get('activeIndex');
    
    SchedulerMgmt.setActiveTab(active,function(data){});    
    if (loadedTabs[active]== null) {
        scd_pageinit(active);
    }
}

function scd_refreshQueueOnClick(){
    loadedTabs[1]=null;
}

window.setInterval("refreshQueueTab();",300000);

function refreshQueueTab(){ 
   if(mainTabView!=null && mainTabView.get('activeIndex')==1 && !executeScriptMode){
       if (!$j("#scd_qLogsList").hasClass('lastFocus')) {
        window.location.reload();
       }
   }
}

function scd_onBlur() {
     $j("#scd_qLogsList").fadeTo("slow", 0.25);
     $j("#scd_qLogsList").addClass('lastFocus');
};
function scd_onFocus(){
    $j("#scd_qLogsList").fadeTo("fast", 1); 
    $j("#scd_qLogsList").removeClass('lastFocus');
    
    if (mainTabView!=null && mainTabView.get('activeIndex') == 1) {
        SchedulerMgmt.getQueueLogs(null, scd_genQLogs);
    }
     
     // $j("#scd_qLogsList").fadeIn();
};

if (/* @cc_on!@ */false) { // check for Internet Explorer
    document.onfocusin = scd_onFocus;
    document.onfocusout = scd_onBlur;
} else {
    window.onfocus = scd_onFocus;
    window.onblur = scd_onBlur;
}

var scd_search_mode=false; 

function scd_searchTask(thisobj){
    var stagid=$j("#scd_tagsdropdown").val();
    if(thisobj.scd_keyword.value=='' && stagid==''){
        alert("Invalid input")
        return false;       
    }
    
    var respBack=function(data){
        scd_search_mode=true;
        
        // scd_treeListGen(data);
        scd_searchRefresh(data);
        progress_message(null);
    }
    progress_message("Please wait searching...");
    
    // SchedulerMgmt.searchScheduledItems(thisobj.scd_keyword.value,respBack);
    SchedulerMgmt.searchScheduledItems(thisobj.scd_keyword.value,stagid,respBack);
    
    return false;
}
 
function scd_editTask(thisobj){
    if(thisobj.scd_edittaskid.value==''){
        alert("Invalid input")
        return false;       
    }   
    var linkTaskWithTree=true;
    scd_loadTask(thisobj.scd_edittaskid.value,linkTaskWithTree);
    return false;
}

function scd_showAllTasks(){
    progress_message("Please wait searching...");
    $j(treeSelector +" li[taskitem='yes']").each(
            function(idx,el){
               $j(el).show();   
            }
        );
        $j(treeSelector +' li[folderitem="yes"]').each(
            function(idx,el){
               $j(el).show();   
            }
        );
        $j(treeSelector + ' ul.filetree  li.collapsable[groupname]').each(
            function(idx,el){
             $j(el).children("div.hitarea").trigger('click');
            } 
        );
    progress_message();
}

function scd_showQLogErrorMsg(thisobj){
    var respBack=function(data){
        if(data!=null){
            alert(data);
        }
    }
    SchedulerMgmt.getLogMessages(thisobj.getAttribute("logid"), respBack);
    return false;
}

function scd_showQLogError(tobj){
    if (scd_logDialogBox == null) {
                    $("scd_logDialogbox").show();
                    scd_logDialogBox = new YAHOO.widget.Dialog("scd_logDialogbox", {
                        width: "520px",
                        fixedcenter: true,
                        visible: false,             
                        constraintoviewport: true,
                        draggable:true,     
                        buttons: [{
                            text: "close",
                            handler: function(){this.cancel();}
                        }]
                    });
                    var listeners = new YAHOO.util.KeyListener(document, { keys : 27 }, {fn:function(){this.cancel();},scope:scd_logDialogBox,correctScope:true} );
                    scd_logDialogBox.cfg.queueProperty("keylisteners", listeners);
                    scd_logDialogBox.render();
    }
    scd_logDialogBox.show();
    var noclosebtn=true;
    scd_showLogMessages(tobj,noclosebtn);
    return false;
}

var previousExecuting=new Array();
function scd_genQLogs(data,divid){
				
				if ((data!=null)&&(data.length > 0))  { // To ensure that data is always non null when populate Current Task Queue and Queue History. Fix issue : popup message shows "Cannot read property 'length' of null". Feb 27, 2018. Gama.
	                for(iab=0;iab<data.length;iab++){
	                    if(scd_groupIcons[data[iab].taskuid]!=null){
	                        data[iab].taskuidimg="<img src='"+scd_groupIcons[data[iab].taskuid]+"' alt='"+data[iab].taskuid+"' border='0'>";
	                    }else{
	                        data[iab].taskuidimg="";
	                    }
	                    if (data[iab].name!= null) {
	                    	if(data[iab].name.length>25){
		                        data[iab].name=data[iab].name.substring(0,22)+"...";
		                    }
	                    }
	                    
	                    if(data[iab].calling_another_script!=null && data[iab].calling_another_script==1){                      
	                        data[iab].name=data[iab].name+"<img src=\"images/calling_another_script.png\" style=\"margin-left:2px\" title=\"This script calls another script inside\"/>"
	                    } 
	                    if(data[iab].dependencies!=null && data[iab].dependencies!=''){                     
	                        data[iab].name=data[iab].name+"<img src=\"images/dependencies.png\" style=\"margin-left:2px\" title=\"This script has dependents\"/>"
	                    } 
	                    data[iab].statimage="";
	                     if (data[iab].executing!=null && data[iab].executing==1 && data[iab].queued != null && data[iab].queued == 1) {
	                            data[iab].statimage = "<img src='images/task_executing.gif' alt='Excecuting' border='0'>";
	                      }else if(data[iab].queued!=null && data[iab].queued==1){
	                        data[iab].statimage="<img src='images/task_queued.gif' alt='Excecuted' border='0'>";
	                     }else if(data[iab].is_triggered!=null && data[iab].is_triggered==1 && data[iab].queued!=null && data[iab].queued==0){
	                        data[iab].statimage="<img src='images/task_executed.gif' alt='Excecuted' border='0'>";
	                    }else{
	                        data[iab].statimage="";
	                    }
	                    data[iab].logs="<a href='#' onclick='scd_ExecLogs(this); return false;' unique_id='" + data[iab].unique_id + "' ttl='" + data[iab].name +" ["+data[iab].trigger_time+"]"+ "'  ><img src='images/logbtn.gif' border='0'></a>";
	                    if(data[iab].inject_code_length!=null &&  data[iab].inject_code_length>0 && data[iab].executed_code_length!=null &&  data[iab].executed_code_length>0){
	                        data[iab].inject="<a href='#' onclick='scd_showinject_code(this); return false;'  unique_id='" + data[iab].unique_id + "' ttl='" + data[iab].name +" ["+data[iab].trigger_time+"]"+ "'  ><img src='images/inject.png' border='0'></a>";
	                    }else{
	                        data[iab].inject="";
	                    }
	                    if(data[iab].console_message_size!=null &&  data[iab].console_message_size>0){
	                        data[iab].inject="<a href='#' onclick='scd_show_console_msg(this); return false;' scheduler_id='"+data[iab].scheduler_id+"' trigger_time='"+data[iab].trigger_time+"' unique_id='" + data[iab].unique_id + "' ttl='" + data[iab].name +" ["+data[iab].trigger_time+"]"+ "'  ><img src='images/console_message.png' border='0'></a>";
	                    }
	                    if (divid != null && data[iab].is_triggered!=null && data[iab].is_triggered==1 && data[iab].status=='fail') {       
	                            if (data[iab].error_logid!=null) {
	                                data[iab].statimage = "<a href='#' logid='"+data[iab].error_logid+"' onclick='scd_showQLogError(this)'><img src='images/task_error.gif' alt='Failed' border='0'></a>";
	                            } else {
	                                data[iab].statimage = "<img src='images/task_error.gif' alt='Failed' border='0'>";
	                            }
	                            data[iab].status="Error";
	                            data[iab].duration = "<input type='button' value='Execute Now' log_id='"+ data[iab].qlog_id+"' scheduler_id='" + data[iab].scheduler_id + "' onclick=scd_executeTaskNow(this)>";
	                    }
	                    if (divid != null && data[iab].is_triggered!=null && data[iab].is_triggered==1 && data[iab].status=='warning') {        
	                            if (data[iab].error_logid!=null) {
	                                data[iab].statimage = "<a href='#' logid='"+data[iab].error_logid+"' onclick='scd_showQLogError(this)'><img src='images/task_warning.gif' alt='Failed' border='0'></a>";
	                            } else {
	                                data[iab].statimage = "<img src='images/task_warning.gif' alt='Failed' border='0'>";
	                            }
	                            data[iab].status="Warning";
	                    }
	                    if (divid != null && data[iab].is_triggered!=null && data[iab].is_triggered!=1 ) {
	                        if (data[iab].queued != null && data[iab].queued == 1) {
	                            // this block already executed.
	                            data[iab].statimage = "";
	                        }else{
	                            data[iab].statimage = "<img src='images/task_failed.gif' alt='Failed' border='0'>";
	                            data[iab].duration = "<input type='button' value='Execute Now' log_id='"+ data[iab].qlog_id+"' scheduler_id='" + data[iab].scheduler_id + "' onclick=scd_executeTaskNow(this)>";
	                        }
	                    }
	                    if (divid != null && data[iab].status == 'success') {
	                            data[iab].statimage = "<img src='images/task_executed.gif' alt='Re-Executed' border='0'>";
	                    }
	                    if (divid != null && data[iab].status == 're-executed') {
	                            data[iab].statimage = "<img src='images/task_reexecuted.gif' alt='Re-Executed' border='0'>";
	                    }
	                    if (divid != null && data[iab].status == 'overlapped') {
	                            data[iab].statimage = "<img src='images/task_overlapped.gif' alt='Overlapped' border='0'>";
	                    }
	                    if (divid != null && data[iab].status == 'dep_timeout') {
	                            data[iab].statimage = "<img src='images/task_dep_timeout.gif' alt='Overlapped' border='0'>";
	                    }
	                    if (data[iab].status=='timeout') {  
	                        data[iab].statimage = "<img src='images/task_timedout.png' alt='Timed-Out' border='0'>";                         
	                    }
	                    if (divid != null && (data[iab].db_insert !=null || data[iab].db_update !=null || data[iab].db_delete !=null )) {
	                            var cont1="";
	                            if(data[iab].db_insert !=null){
	                                 cont1+="<span class='db_insert'>"+data[iab].db_insert +"</span>";  
	                            }
	                            if(data[iab].db_update !=null){
	                                 cont1+="<span class='db_update'>"+data[iab].db_update +"</span>";  
	                            }
	                            if(data[iab].db_delete !=null){
	                                 cont1+="<span class='db_delete'>"+data[iab].db_delete +"</span>";  
	                            }
	                            data[iab].db_action ="<a href='#' unique_id='" + data[iab].unique_id + "'  scheduler_id='"+data[iab].scheduler_id+"' onclick='scd_showScriptData(this)'>"+cont1+"</a>";
	                    }
	                    if (divid == null) {
	                        data[iab].host="";
	                    }
	                }
				}
                var atable=new LinkTable();  
                if (divid != null) {
                    atable.init($(divid));
                } else {
                    atable.init($("scd_qLogsList"));
                }       
                var width=true;
                if(width){
                    atable.addColumn(" ","statimage","10px");
                    atable.addColumn("ID","scheduler_id","37px");
                    atable.addColumn("Name","name","300px");
                    atable.addColumn("Type","taskuidimg","22px");
                    atable.addColumn("Scheduled","trigger_time","150px");
                    if (divid != null) {
                        atable.addColumn("Delay(Mins)", "started_at", "60px");                  
                        atable.addColumn("Duration", "duration", "40px");
                        atable.addColumn("Status", "status", "40px");
                        atable.addColumn("Executed On", "host", "40px");
                        atable.addColumn("Log", "logs", "20px");
                        atable.addColumn("", "inject", "20px");
                        atable.addColumn("Data", "db_action","120px");
                        atable.addColumn("Resp Code", "response_code","55px");
                    }else{
                        atable.addColumn("Executing On", "host", "40px");   
                    }               
                }else{
                    
                    atable.addColumn(" ","statimage");
                    atable.addColumn("ID","scheduler_id");
                    atable.addColumn("Name","name");
                    atable.addColumn("Type","taskuidimg");
                    atable.addColumn("Scheduled","trigger_time");
                    if (divid != null) {
                        atable.addColumn("Delay(Mins)", "started_at");                  
                        atable.addColumn("Duration", "duration");
                        atable.addColumn("Status", "status");
                        atable.addColumn("Executed On", "host");
                        atable.addColumn("Log", "logs");
                        atable.addColumn("", "inject");
                        atable.addColumn("Data", "db_action");
                        atable.addColumn("Resp Code", "response_code");
                    }else{
                        atable.addColumn("Executing On", "host");   
                    }   
                }
                atable.setRowId("unique_id");   
                atable.setRowClassProp("taskuid");
                atable.selectionBox(false);
                atable.setTableClass("queuetable");
                atable.setTableWidth("100%");
         
                atable.updateDataNew(data); 
    
                if (!showExecutingQueueStarted) {
                    setTimeout(showExecutingQueue,1500);
                    $("scd_histLogsList").innerHTML="Please wait...";
                    setTimeout(scd_showHistory,3000);
                }
                if (divid != null) {
                    $j("#"+divid+" .queuetable").fixedHeaderTable({ footer: false, cloneHeadToFoot: false, fixedColumn: true});                 
                } else {                 
                    
                }
}

function scd_show_console_msg(thisobj){
    
    var uid=$j(thisobj).attr("unique_id");
    var respBack=function(data){
        $j("#dialog_box_console_msg").commonDialog({width:800, height:750,modal:true});     
        $j("#dialog_box_console_msg_data").html(data!=null?data:"");
    }
    
    SchedulerMgmt.getConsoleMsg(uid,respBack);
}

function scd_showScriptData(thisobj) {
    var u_id=$j(thisobj).attr("unique_id");  
    var respBack=function(data){
        progress_message();
        
     $j("#dialog_box_explog").dialog({close: function(event, ui) {}, modal:true,title:"",width: 760,position:'center' });
        
        $j("#dialog_box_explog_data").html("");
        var markup1="<table width='99%' border='1' cellpadding='2' cellspacing='0' style='border-collapse:collapse;'><thead><tr> <th>Date</th> <th>Contract</th> <th>Val</th> <th>Sval</th> </tr></thead><tbody>";
        var markup="";
        var uncon_id="";        
        for(ia=0;ia<data.length;ia++){
            if(data[ia].uid!=uncon_id){
                if(uncon_id!=''){
                    markup+="</tbody></table>";
                }
                markup+="<div class='session'><span class='database'>"+data[ia].dbase+"</span>.<span class='tablename'>"+data[ia].tablename+"</span> <span class='time'>("+data[ia].trans_date1+") </span> </div>"+markup1;
            }           
            markup+="<tr  class='action_"+data[ia].action_t+"'>";           
            markup+="<td>"+data[ia].cdate1+"</td>";
            markup+="<td>"+data[ia].contract+"</td>";
            markup+="<td>"+(data[ia].val==null?"":data[ia].val)+"</td>";
            markup+="<td>"+(data[ia].sval==null?"":data[ia].sval)+"</td>";          
            markup+="</tr>";
            uncon_id=data[ia].uid;
        }
        markup+="</tbody></table>";
        $j("#dialog_box_explog_data").html("<div class='data_log'>"+markup+"</div>");
    }
    progress_message("Please wait...")  
    SchedulerMgmt.getDataLogHistory(u_id,respBack);
}

function scd_ExecLogs(thisobj) {
    var respBack=function(data){
            for(abc=0;abc<data.length;abc++){
                if(data[abc].repcode!=null && data[abc].repcode>0){
                    data[abc].rowclass="type_"+(data[abc].repcode-(data[abc].repcode%1000));
                }
            }
            $j("#dialog_box_explog_data").html("<div class='data_log'></div>");
    
            logtable=new LinkTable();        
            logtable.init($j("#dialog_box_explog_data .data_log")[0]);                      
            logtable.addColumn("Date & Time ","trans_datetime1","130px");
            logtable.addColumn("Host/Peer","machine","85px"); 
            logtable.addColumn("Message","message",null);
            logtable.setRowId("id");
            logtable.setRowClassProp("rowclass");
            logtable.selectionBox(false);
            logtable.setTableClass("queuetable");               
            logtable.setTableWidth("100%");      
            logtable.updateData(data);  
    }
    var ttl=$j(thisobj).attr("ttl");
    $j("#dialog_box_explog").dialog("open");
    $j("#dialog_box_explog").dialog({close: function(event, ui) {}, modal:true,title:ttl,width: 760 });
    var id_tm=$j(thisobj).attr("unique_id");
    if(id_tm.split("_").length==2){
        var s_id=id_tm.split("_")[0];
        var tritim=id_tm.split("_")[1];
        
        $j("#dialog_box_explog_data").html("Please wait...");
        SchedulerMgmt.getExecutionLogs(parseInt(s_id),parseInt(tritim), respBack);
    }
} 

function scd_showinject_code(thisobj) {

    var id_tm=$j(thisobj).attr("unique_id");
    var ttl=$j(thisobj).attr("ttl");
    var previd="prv_code_indject_"+id_tm;
        
    $j("#dialog_box_explog").dialog("open");
    $j("#dialog_box_explog").dialog({close: function(event, ui) {
         editAreaLoader.delete_instance(previd);
    }, modal:true,title:ttl,width: 760 });

    var respBack=function(data) {       
        $j("#dialog_box_explog_data").html("<div class='data_codeInj'><textarea style='width:100%;height:500px;' id='"+previd+"'>"+data+"</textarea></div>");
        
         editAreaLoader.init({
            id: previd,
            syntax: "r",
            start_highlight: true,
            toolbar: "|",
            allow_toggle: false,
            font_size: 8,                               
            font_family: "verdana, monospace",
            plugins: "reditor_task",                                
            is_editable:false
        });
    }
    SchedulerMgmt.getInjectCode4Log(id_tm,respBack);
}

var showExecutingQueueStarted=false;
var que2berefreshedfinal=false;
var servertimestring=null;
var lastExcecutedTime=0;

function showExecutingQueue(){
    showExecutingQueueStarted=true;
    var respBack=function(data){
        if(IGNORE_LIVEUPDATE_PAGE) { return ; }
        if (data != null) {
            var que2berefreshed = false;
            if (data.servertime != null) {
                var servertimestring1 = data.servertime;
                if (servertimestring == null) {
                    servertimestring = servertimestring1;
                    new showLocalTime("timecontainer", "server-ssi", 0, "short");
                }
                servertimestring = data.servertime;
            }
			
            /**
             * count number of tasks running on this peer
             */         
            /*
             * var taskcount=new Object(); if (data.executingpeers != null) {
             * for (a in data.executingpeers) { if
             * (taskcount[data.executingpeers[a]] == null) {
             * taskcount[data.executingpeers[a]] = 1; } else {
             * taskcount[data.executingpeers[a]] =
             * taskcount[data.executingpeers[a]] + 1; } } }
             */
            taskcount=data.exe_count;
            if (data.onlinepeers != null) {
                var p1 = "";
                $j("#scd_onlinepeers div.peer").each(function(idx,el){
                    $j(el).removeClass("online");
                });             
                
                // the following lines to show online peers in peer association
                // tab.
                $j("#pa_matrix_tbl .peers[peer] div.plink").removeClass("pa_matrix_onpeer");
                $j("#pa_matrix_tbl .peers[peer] div.plink").addClass("pa_matrix_offpeer");
				
                for (key in data.onlinepeers) {
                    // the following lines to show online peers in peer
                    // association tab.
                    $j('#pa_matrix_tbl .peers[peer="'+key+'"] div.plink').addClass("pa_matrix_onpeer");
                    $j('#pa_matrix_tbl .peers[peer="'+key+'"] div.plink').removeClass("pa_matrix_offpeer");
                
                    var stat = data.onlinepeers[key];
                    var pfname=(data.pfnames!=null && data.pfnames[key]!=null) ?data.pfnames[key] :key;
                    
                    var batchTxt="0"
                        
                    if (taskcount[key] != null) {
                        batchTxt = taskcount[key];
                    }
                    
                    var peerdv= $j('#scd_onlinepeers div.peer[peername="'+key+'"]');
                    peerdv.addClass("online");
                    
                    var pn = '<div class="peer">' + pfname + '</div>';
                    
                    // peerdv.find(".batch").fadeOut(1000, function(){
                    // $j(this).remove();});
                    peerdv.find(".batch").remove();
                    
                    if (stat == 'BUSY') {
                          // pn = '<div class="peer running"><div
                            // class="batch">'+batchTxt+'</div>' + pfname +
                            // '</div>';
                          peerdv.addClass("running");
                          peerdv.append('<div class="batch">'+batchTxt+'</div>');
                    }else{
                          peerdv.removeClass("running");
                                                  
                    }
                    
                    if (p1 == "") 
                        p1 += pn;
                    else 
                        p1 += "" + pn;
                    
                }
                if(executeScriptMode){
                    scd_executeScriptPeersUpd(data.onlinepeers);
                }
            }
            
            if (data.istarted != null && data.istarted) {
            // $("schedulerstatus").innerHTML="Running";
            }
            else {
            // $("schedulerstatus").innerHTML="Not Running/Not found (please
            // refresh this page)";
            }
            
            if (data.hostname != null) {
            // $("scd_hostname").innerHTML = data.hostname;
            
            }
            
            if (data.lastexecutedtime != null && data.lastexecutedtime > 0 && data.lastexecutedtime != lastExcecutedTime) {
                lastExcecutedTime = data.lastexecutedtime;
                if (data.queued != null && data.queued.length == 0) {
                    que2berefreshed = true;
                }
            }
            if (data.queued != null) {
                for (iab = 0; iab < data.queued.length; iab++) {
                    if (previousExecuting.indexOf(data.queued[iab]) == -1) {
                        previousExecuting[previousExecuting.length] = data.queued[iab];
                        que2berefreshed = true;
                    }
                    var row = $("scd_qLogsList").select('[rowid="' + data.queued[iab] + '"]');
                    if (row[0] != null) {
                        var cell0 = row[0].cells[0];
                        var cell4 = row[0].cells[5];
                        var cell1 = row[0].cells[2];                        
                        if (cell0 != null) {
                            if (show_progressBar) {
                                if (cell1.getAttribute("name") == null) {
                                    cell1.setAttribute("name", cell1.innerHTML);
                                }
                                try {
                                    if (data.x_completed != null) {
                                        var hash = $H(data.x_completed);
                                        var rowid = row[0].getAttribute("rowid");
                                        if (rowid != null && hash.get(rowid) != null) {
                                            var pr_data = hash.get(rowid);
                                            if (pr_data.indexOf("|") > 0) {
                                                var prdataarr = pr_data.split("|");
                                                var curr = parseInt(prdataarr[0]);
                                                var hist = parseInt(prdataarr[1]);
                                                var perc = (curr / hist) * 100;
                                                perc = Math.round(perc);
                                                if (cell1.getAttribute("name") != null) {
                                                    var tskname=$j.trim(cell1.getAttribute("name"));                                                    
                                                    tskname=(tskname.length>=40 && !tskname.indexOf(">")) ? tskname.substring(0,40)+"...":tskname;
                                                    if (perc < 100) {
                                                        cell1.innerHTML = "<div class='taskNameOLWR'><div class='taskNameOL'>" + tskname+ "</div><div class='taskNameOL_PT'><small>&nbsp;&nbsp;&nbsp;&nbsp;(" + perc + "%)</small></div><div class='taskNameOL_P' style='width:"+perc+"%'></div></div>";
                                                    } else {
                                                        cell1.innerHTML = "<div class='taskNameOLWR'><div class='taskNameOL'>" + tskname + "</div><div class='taskNameOL_P' style='width:100%'></div></div>";
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } 
                                catch (ex) {
                                }
                            }
                            
                            que2berefreshedfinal = true;
                            var star = "";
                            row[0].style.textDecoration = "";
                            
                            var img1 = "images/task_queued.gif";
                            var isexecuting = false;
                            if (data.executing != null && data.executing.indexOf(data.queued[iab]) >= 0) {
                                img1 = "images/task_executing.gif";
                                isexecuting = true;
                                $j(row).addClass("executing");
                            }else{
                                $j(row).removeClass("executing");
                            }
                            if (cell0.getAttribute('executing') != null && cell0.getAttribute('executing') == isexecuting + '') {
                                
                            }else {
                                cell0.innerHTML = "<img alt='Executing' src='" + img1 + "' style='valign:middle' border='0'>";
                            }
                            
                            $j(row[0]).attr("executing", isexecuting + '');
                            cell0.setAttribute("executing", isexecuting + '');
                            
                            if (!isexecuting) {
                                cell4.innerHTML = "";
                                var anc = document.createElement('a');
                                anc.appendChild(document.createTextNode('Kill'));
                                anc.setAttribute("href", "#");
                                anc.setAttribute("rowid", data.queued[iab]);
                                anc.style.fontSize = ".7em";
                                anc.onclick = function(){
                                    var resp = function(data){
                                        if (data != null) {
                                            que2berefreshed = true;
                                            var row1 = $("scd_qLogsList").select('[rowid="' + data + '"]');
                                            if (row1[0] != null && row1[0].cells[0] != null) {
                                                row1[0].cells[0].innerHTML = "<img alt='Executed' src='images/task_failed.gif' border='0'>";// +
                                                                                                                                            // cont;
                                            }
                                        }
                                    }
                                    SchedulerMgmt.killQueuedTask(this.getAttribute("rowid"), resp);                                 
                                    $j(this).fadeOut(300, function(){ $j(this).remove();});
                                    return false;
                                }
                                cell4.appendChild(anc);
                            }
                        }
                        if (cell4 != null) {
                            if (data.executingpeers != null && data.executingpeers[data.queued[iab]] != null) {
                                var key=data.executingpeers[data.queued[iab]];
                                var pfname=(data.pfnames!=null && data.pfnames[key]!=null) ?data.pfnames[key] :key;
                                cell4.innerHTML = pfname;
                            }
                        }
                    }
                }
            }
            
            var justremoved = new Array();
            for (iab = 0; iab < previousExecuting.length; iab++) {
                var cid = previousExecuting[iab];
                
                if (data.queued.indexOf(cid) == -1) {
                                    
                    justremoved[justremoved.length] = cid;
                    var row = $("scd_qLogsList").select('[rowid="' + cid + '"]');
                    if (row[0] != null) {
                        var cell0 = row[0].cells[0];
                        var cell1 = row[0].cells[2];
                        if (cell1.getAttribute("name") != null) {
                            cell1.innerHTML = cell1.getAttribute("name");
                        }                       
                        if (cell0 != null) {
                            cell0.innerHTML = "<img alt='Executed' src='images/task_executed.gif' border='0'>";// +
                        }
                    }
                }
            }
            
            for (iab = 0; iab < justremoved.length; iab++) {
                previousExecuting = previousExecuting.without(justremoved[iab]);
            }
            if (que2berefreshed) {
                SchedulerMgmt.getQueueLogs(null, scd_genQLogs)
                que2berefreshed = false;
            }
            if (que2berefreshedfinal && previousExecuting.length == 0) {
                SchedulerMgmt.getQueueLogs(null, scd_genQLogs)
                que2berefreshedfinal = false;
            }
            clearTimeout(queueDispPollTimeout);         
            if (mainTabView.get('activeIndex') == 2) {
                queueDispPollTimeout=setTimeout("showExecutingQueue()", 5000);
            }
            else {
                queueDispPollTimeout=setTimeout("showExecutingQueue()", 1200);
            }   
        }
    }   
    var respBackErr=function() { 
        if (mainTabView.get('activeIndex') == 2) {
            queueDispPollTimeout=setTimeout("showExecutingQueue()", 5000);
        }else {
            queueDispPollTimeout=setTimeout("showExecutingQueue()", 1200);
        }
    }
    var callMetaData = {
        callback: respBack,      
        exceptionHandler: respBackErr
    };
    
    
    
    if (mainTabView.get('activeIndex') == 1 || mainTabView.get('activeIndex') == 2) {
        if(!IGNORE_LIVEUPDATE_PAGE) {
            SchedulerMgmt.getExecutingTasks(callMetaData);
        }else{respBackErr();}
    }else{
        respBackErr();      
    }
}
var queueDispPollTimeout=null;

function scd_managePeerAssocSave(){
    var taskuid=null
    var peers=new Array(); 
    var chbox=$("scd_managePeerbd").select('[peer_assoc="yes"]');
    for(ib=0;ib<chbox.length;ib++){
        if (taskuid == null) {
            taskuid=chbox[ib].getAttribute('taskid')
        }
        if(chbox[ib].checked){
            peers[peers.length]=chbox[ib].value;
        }
    }
    var respBack=function(data){
        scd_managePeerAssocBox.cancel();
        if(data!=null && data){
            message_status("Peer Association has been set");            
        }
    }
    SchedulerMgmt.setTaskPeerAssoc(taskuid,peers,respBack);
    
}

var scd_managePeerAssocBox=null;
function scd_managePeerAssoc(thisobj){
    var taskid=null;
    var taskid="";
    if (typeof thisobj == 'object') {
        taskname = thisobj.getAttribute("taskname");    
        taskid = thisobj.getAttribute("taskid");
    }else{
        taskid =thisobj;
    }
    $("scd_managePeerAssoc").show();
    if(scd_managePeerAssocBox==null){
        scd_managePeerAssocBox=new YAHOO.widget.Dialog("scd_managePeerAssoc", {
                        width: "450px",
                        fixedcenter: true,
                        visible: false,     
                        draggable:true,
                        buttons: [{text: "Save",handler: scd_managePeerAssocSave} ,{text: "Cancel",handler: function(){this.cancel();}}         ]
        });
        var listeners = new YAHOO.util.KeyListener(document, { keys : 27 }, {fn:function(){this.cancel();},scope:scd_managePeerAssocBox,correctScope:true} );
        scd_managePeerAssocBox.cfg.queueProperty("keylisteners", listeners);
        scd_managePeerAssocBox.render();
    }
    scd_managePeerAssocBox.setHeader("Peer Association for "+taskname +"");
    var respBack=function(data){
        if(data!=null && data.all!=null){
            scd_managePeerAssocBox.show();
            
        	$("scd_managePeerbd").innerHTML="";
            for(iab=0;iab<data.all.length;iab++){
                var chbox=document.createElement('input');
                chbox.type='checkbox';
                chbox.setAttribute("value",data.all[iab]);
                chbox.setAttribute("peer_assoc",'yes');
                chbox.setAttribute("taskid",taskid);
                if(data.associated.indexOf(data.all[iab])>=0){
                    chbox.checked=true;
                }
                var dv=document.createElement('div');
                dv.style.fontSize="1.1em";              
                dv.appendChild(chbox);
                dv.appendChild(document.createTextNode(data.all[iab]));                             
                $("scd_managePeerbd").appendChild(dv);
            }  
                 
        }
    }
    SchedulerMgmt.getTaskPeerAssoc(taskid,respBack);
}

var scd_manageFoldersBox=null;

function scd_createNewTask(thisobj){
    
    scd_currentScTaskId=0; 
    var taskid=thisobj.getAttribute("taskid");
    var taskname=thisobj.getAttribute("taskname");
    if ($j("#scd_tabs-" + scd_currentScTaskId).length > 0) {
        message_status("Please save new task that you are already working on first")
    }
    
    var tmpl = $j.template(tmpl_txt);
    scd_currentTabCotent = tmpl.apply({
            task_id: scd_currentScTaskId + ""
    });
    scd_editorTab.tabs("add", "#scd_tabs-" + scd_currentScTaskId, "[Unnamed Task]");
    
    $j("#scd_btn_save_"+scd_currentScTaskId).val("Save");               
    $j("#scd_btn_save_"+scd_currentScTaskId).attr("taskuid",taskid);
    $j("#scd_btn_save_"+scd_currentScTaskId).show();
     
    $j("#scd_taskTab_"+scd_currentScTaskId).tabs();
    $j("#scd_taskTab_"+scd_currentScTaskId).tabs("option","disabled", [2]);
    myTriggerTableObj[scd_currentScTaskId]=getTriggerTable("trigger_table_"+scd_currentScTaskId); //create table on Triggers tab
    
    if (false) {
        var children = $("scd_taskform").childElements();
        for (intb = 0; intb < children.length; intb++) {
            children[intb].remove();
        }
        $("scd_btn_save").show();
        $("scd_btn_save").value = "Save";
        
        $j('#scd_taskTab').tabs( "select" , 0 );
        $j('#scd_taskTab').tabs("option","disabled", [2]);  

    }
     
    // select first tab
    // deactivate option revisions tab.
    scd_getFormFields(taskid,"scd_form_general_"+scd_currentScTaskId,scd_currentScTaskId);
    var respBack=function(data){
        if(data.tag_follow!=null){
                data.tag_follow.authorizedUser=data.authorizedUser; 
                to_showTags4Item("#scd_taskTab_0",data.tag_follow,scd_currentScTaskId,null,"Theme / Alert / Notification");
        }
    }
    SchedulerMgmt.getTags4New(respBack);
}

function scd_setAll(flag){
    if(flag=='yes'){
        // set cookie scheduler_showAll='yes'
        jQuery.cookie('scheduler_showAll','yes'); 
    }else{
        // remove cookie scheduler_showAl
        jQuery.cookie('scheduler_showAll',null);
    }
    location.reload();
}

var treeSelector="div#scd_main_tree";
var treeMenuRendered=false;
var jqtree=null;

function consoleLogTime(uid){
    console.log("consoleLogTime():"+uid+":"+new Date().getHours()+":"+new Date().getMinutes()+":"+new Date().getSeconds()+" "+new Date().getMilliseconds());
}

function scd_treeListGen(data1){

    var pop_warning=null;
    
    if (data1 != null && typeof data1 == 'object') {

            /**
             * creates group bar
             */
            
            consoleLogTime("groups");
            
            if (data1.tasktypes != null) {
                
                $j(treeSelector).html("");  // removes if there any loading
                                            // message.
                $j(treeSelector).append("<ul class='filetree'></ul>");
                var groupnodes="";      
                for (uik in data1.tasktypes) {
                
                    var htmlid = "root_task_" + uik;
                     
                    var stylecode=(scd_groupColor[uik]!=null && scd_groupColor[uik]!='')? " style=' -moz-box-shadow:2px 2px 2px grey;; -webkit-box-shadow:-2px 2px 2px grey;background-image:none;background-color:"+scd_groupColor[uik]+"'":"";                    
                    var nodehtm = "<div class='TaskGroupBar' groupTarget='" + uik + "' id='" + htmlid + "' "+stylecode+" ><span style='width:530px;display:block;float:left'><img src='" + scd_groupIcons[uik] + "' border='0'> <b>" + data1.tasktypes[uik] + "</b></span>";
                
                    nodehtm += "<input type='button'  class='TaskGroupBarBtn' taskid='" + uik + "' taskname='" + data1.tasktypes[uik] + "' value='New Task' onclick='scd_createNewTask(this)'>";
                    nodehtm += "<input type='button'  class='TaskGroupBarBtn'  taskid='" + uik + "' taskname='" + data1.tasktypes[uik] + "' value='New Folder' onclick='ctxCreateFolder(\"\",\""+uik+"\")'>";
                    nodehtm += "<input type='button'  class='TaskGroupBarBtn'  taskid='" + uik + "' taskname='" + data1.tasktypes[uik] + "' value='Peers' onclick='scd_managePeerAssoc(this)'><span style='float:right'><img src='images/draghandle.png' class='dragholder' style='cursor:move'></span>";
                    nodehtm += "</div>";
                    var spn = "<span class='folder'>" + data1.tasktypes[uik] + "</span>";
                    var ulgrpid="ul_groupname_"+uik;
                    groupnodes += "<li class='closed' groupname='" + uik + "'>" + nodehtm + "<ul id='"+ulgrpid+"' groupname='" + uik + "'></ul></li>";
                    
                }
                groupnodes += "";
                $j(treeSelector + " ul.filetree").append(groupnodes);
                
                // adding trash
                var nodehtm = "<div class='TaskGroupBarTrash' groupTarget='trash' id='root_task_trash'>";
                nodehtm += "<div class='label_groupname' style='display:inline;vertical-align:top;'><b>&nbsp;</b></div></div>";
                nodehtm = "<li class='closed' groupname='trash' onclick='scd_openTrash()' title='Click here to view contents' >" + nodehtm + "</li>";
                $j(treeSelector + " ul.filetree").append(nodehtm);  
            
             }
            
            
                
            // this block toggles Mine/All buttons
            $j("input#scd_showAllBtn").attr("disabled","disabled");
            $j("input#scd_showMineBtn").attr("disabled","disabled");
            
            if(data1.isShowAll){
                $j("input#scd_showMineBtn").removeAttr("disabled");
            }else{              
                $j("input#scd_showAllBtn").removeAttr("disabled");
            }
            
            
            consoleLogTime("folders");
            
            /**
             * create tree menue folder items.
             */
            
            var idGen=function(folder,element,prefix){
                 
                folder=folder.replace(/-|\//g, "-");
                folder=folder.replace(/ /g,"_");
                folder=folder.replace(/&/g,"__");
                
                folder=element+"_"+prefix+"_"+folder;   
                return folder;
            }
            
            
            var addFolder=function(uniqfolder,taskuid,folderpath,foldername,fullpath ,folderid , parent_id /*
                                                                                                             * pulid
                                                                                                             * or
                                                                                                             * ulgrpid
                                                                                                             */ ) {
                if ($j('span[uniquefid="' + uniqfolder + '"]').length == 0) {
                    var ulid=idGen(folderpath,'ul',taskuid);
                    var liid=idGen(folderpath,'li',taskuid);
                    var folder = '<li id="'+liid+'" class="closed" folderitem="yes" foldername="' + folderpath + '" taskuid="' + taskuid + '"><span uniquefid="' + 
                    uniqfolder + '" class="folder">' + foldername + '</span><ul id="'+ulid+'" foldername="' + fullpath + '"  folderid="' + 
                    folderid + '"></ul></li>';                  
                    folderULs[data1.folders[ibc].id]=ulid;                               
                    $j('ul#'+parent_id).append(folder);
                }
                
            }
            
            
            
            //generating folders.
            
            var folderULs=new Array(); 
            if (data1.folders != null) {                
                for (ibc = 0; ibc < data1.folders.length; ibc++) {                  
                    
                        var taskuid=data1.folders[ibc].taskuid;
                                                
                        var folderpath=data1.folders[ibc].folder_name;
                        var uniqfolder=data1.folders[ibc].taskuid+"_"+folderpath;
                        
                                                
                        if (folderpath.indexOf("/")>=0  ) {
                                                                                    
                            var fsplit = folderpath.split("/");
                            var current=fsplit[fsplit.length-1];
                            // var parent= folderpath.replace("/"+current,"");
                            // //commented this line because bug when parent
                            // folder contains currret folder name it gets
                            // replaced.
                            var parent= folderpath.substring(0,folderpath.lastIndexOf("/"));
                            var pulid=idGen(parent,'ul',taskuid);
                            var ulgrpid="ul_groupname_"+taskuid;
                            
                            
                            // parent folder and group should exist
                            if($j('ul#'+pulid).length==0 && $j('ul#'+ulgrpid).length>0){
                                // console.log("parent :"+pulid+" not found ");
                                pop_warning="Warning : One or more folders are corrupted! <input type='button' value='Repair' onclick='scheduler_fldr_repair()' style='margin:0px 20px'>"
                            }
                           
                            addFolder(uniqfolder,taskuid,folderpath,current,data1.folders[ibc].folder_name,data1.folders[ibc].id , pulid );
                            
                            /*console.log("+++ 1");
                            console.log("uniqfolder : "+uniqfolder);
                            console.log("taskuid : "+taskuid);
                            console.log("folderpath : "+folderpath);
                            console.log("current : "+current);
                            console.log("data1.folders[ibc].folder_name : "+data1.folders[ibc].folder_name);
                            console.log("data1.folders[ibc].id : "+data1.folders[ibc].id);
                            console.log("pulid : "+pulid);*/
                            
                        }else {
                            var ulgrpid="ul_groupname_"+taskuid;
                            var current=data1.folders[ibc].folder_name;
                            addFolder(uniqfolder,taskuid,folderpath,current,data1.folders[ibc].folder_name,data1.folders[ibc].id , ulgrpid );
                            
                            /*console.log("+++ 2");
                            console.log("uniqfolder : "+uniqfolder);
                            console.log("taskuid : "+taskuid);
                            console.log("folderpath : "+folderpath);
                            console.log("current : "+current);
                            console.log("data1.folders[ibc].folder_name : "+data1.folders[ibc].folder_name);
                            console.log("data1.folders[ibc].id : "+data1.folders[ibc].id);
                            console.log("pulid : "+pulid);*/
                            
                            
                        }
                    }
             }
            
            
            consoleLogTime("scheduler items");
            
            /**
             * creates or updates folder tasks items.
             * generates scheduler items.
             */
            
            if(data1.scheduleditems!=null ){
                for(iac=0;iac<data1.scheduleditems.length;iac++){
                        
                        var taskid=data1.scheduleditems[iac].taskuid
                        var ulgrpid="ul_groupname_"+taskid;                   
                        var sc_id=data1.scheduleditems[iac].id;            
                    
                            // var
                            // parentnode=(scd_FolderMnArry[data1.scheduleditems[iac].folder_id]!=null)?scd_FolderMnArry[data1.scheduleditems[iac].folder_id]:scd_MainMnArry[taskid];
                            // var
                            // spwidth=(scd_FolderMnArry[data1.scheduleditems[iac].folder_id]!=null)?"372px":"390px";
                            var spwidth="352px";
                            
                            var activimag=(data1.scheduleditems[iac].active==-1)?"images/pause_button.gif":"images/running_button.gif";
                            var folderid=data1.scheduleditems[iac].folder_id;
                            
                            var htmlid="scheduler_id_"+sc_id;

                            var oldclass="treeTaskItem";

                            /*
                             * if ($j(treeSelector + ' #' + htmlid).length > 0) {
                             * oldclass=$j(treeSelector + ' #' +
                             * htmlid).attr("class"); }
                             */
                            
                            var domhtmlid= $j(' div#' + htmlid);                            
                            if (domhtmlid.length > 0) {
                                oldclass=domhtmlid.attr("class");
                            }
                            
                            // var nodehtml="<div class='"+oldclass+"'
                            // folderid='"+folderid+"' id='"+htmlid+"' > <span
                            // style='width:"+spwidth+";display:block;float:left;font-size:.9em'>"+data1.scheduleditems[iac].name+"</span>";
                            var nodehtml="<div  class='"+oldclass+"' folderid='"+folderid+"' id='"+htmlid+"' >";
                            
                            var tskname=$j.trim(data1.scheduleditems[iac].name);
                            tskname=(tskname.length>=35) ? tskname.substring(0,35)+"...":tskname;
                                                    
                            //if (!tskname.includes("2nd-Schd-")) {
                            if (!tskname.includes("2nd")) {
                            
	                            var alert_type=data1.scheduleditems[iac].alert_type;
	                            alert_type=alert_type==null?'':alert_type;
	                            
	                            var a_icon="alert_none.png";
	                            if(alert_type=='phone') a_icon="alert_phoneemail.png";
	                            if(alert_type=='email') a_icon="alert_email.png";
	                                                        
	                            
	                            // tag manipulation start ~~~~~~
	                            var t1="";
	                            var leng_tag="";
	                            var more_tags="";
	                            
	                            var access_class="access_noaccess";
	                            
	                            var themefound=false;
	                            
	                            if (data1.scheduleditems[iac].stags != null && data1.scheduleditems[iac].stags != '') {
	                                var tags=data1.scheduleditems[iac].stags.split(",");
	                                var view_btn = false;
	                                var execonly_btn = false;
	                                for(iae=0;iae<tags.length;iae++){
	                                    var tg=$j.trim(tags[iae]);
	                                    
	                                    var owner_tg=data1.scheduleditems[iac].owner_tag!=null ? $j.trim(data1.scheduleditems[iac].owner_tag):null;
	                                    
	                                    if(tg.indexOf("thm")>=0) themefound=true;                                   
	                                    tg1=(tg.indexOf("-")>0)?tg.substring(tg.indexOf("-")+1):tg;
	                                    
	                                    leng_tag+=tg1;
	                                    if (tg != '' && tg.indexOf("usr-")==-1) {
	                                    // if (tg != '' ) {
	                                                                                
	                                    	 if(data1.r_tags!=null && $j.inArray(tg1,data1.r_tags)>=0  && access_class=="access_noaccess") access_class="access_readonly";
	                                         if((data1.rwx_tags!=null && $j.inArray(tg1,data1.rwx_tags)>=0) || (data1.rx_tags!=null && $j.inArray(tg1,data1.rx_tags)>=0)) access_class="access_readwrite";                                            
	                                         if(data1.rwx_tags!=null && $j.inArray(tg1,data1.rwx_tags)>=0 && access_class.indexOf("access_rwx")==-1) access_class+=" access_rwx";
	                                         if(data1.rx_tags !=null && $j.inArray(tg1,data1.rx_tags)>=0 && access_class.indexOf("access_rx")==-1) access_class+=" access_rx";
	                                         if(data1.r_tags!=null && $j.inArray(tg1,data1.r_tags)>=0 && access_class.indexOf("access_r")==-1) access_class+=" access_r";
	                                        
	                                         if(data1.r_tags!=null && $j.inArray(tg1,data1.r_tags)>=0) view_btn = true;
	                                         if(data1.rx_tags!=null && $j.inArray(tg1,data1.rx_tags)>=0) view_btn = true;
	                                         if(data1.rx_tags!=null && $j.inArray(tg1,data1.rx_tags)>=0) execonly_btn = true;
	
	                                         
	                                         // added line 2115 -  2118 to fix the display issue of the scheduler 
	                                         if ((data1.rwx_tags!=null && $j.inArray(tg1,data1.rwx_tags)>=0) || access_class.indexOf("access_rwx") > -1){
													execonly_btn = false;
													view_btn = false;
											 }
	                                        
	                                        var clas = "task_tag ";
	                                        if(owner_tg!=null){                                                                             
	                                            owner_tg=(owner_tg.indexOf("-")>0)?owner_tg.substring(owner_tg.indexOf("-")+1):owner_tg;
	                                            if(owner_tg==tg1){
	                                                clas+="item_owner_tag ";
	                                            }
	                                        }
	                                         
	                                        if (leng_tag.length < 45) {                                         
	                                            if (scd_TagColors[tg] != null && scd_TagColors[tg] != '') {
	                                                t1 += "<span class='" + clas + "' style='background-color:" + scd_TagColors[tg] + "'>" + tg1 + "</span>";
	                                            } else {
	                                                t1 += "<span class='" + clas + "'>" + tg1 + "</span>";
	                                            }
	                                        }else{
	                                            clas+="task_tag_more ";
	                                            more_tags="<span class='" + clas + "'>...</span>";          
	                                        }                               
	                                    }                                   
	                                }
	                                
	                            }
	                            if(!themefound){
	                                    access_class="access_readwrite ";
	                            }
	                            if(data1.superuser!=null && data1.superuser!=''){
	                                access_class="access_readwrite";
	                            }
	                            t1+=more_tags;
	                            
	                            nodehtml+="<span font-size:.9em' access='"+access_class+"' title='"+data1.scheduleditems[iac].name+"'>"+tskname+"</span>";
	                                                        
	                            nodehtml+="<div class='task_holder' style='margin-left:10px'>"+t1+"</div>";
	                            
	                            
	                            if(data1.scheduleditems[iac].calling_another_script!=null && data1.scheduleditems[iac].calling_another_script=='1'){
	                                nodehtml+="<img src='images/calling_another_script.png' style='margin-left:2px' title='This script calls another script inside'>";
	                            }
	                            
	                            
	                            if(data1.scheduleditems[iac].following!=null && data1.scheduleditems[iac].following!=''){
	                                nodehtml+="<img src='images/following.png' style='margin-left:2px' title='You are following this'>";
	                            }
	                            
	                            // tag manipulate end....
	                            nodehtml+="<div style='float:right;margin-right:7px;width:200px'>";
	                            
	                                
	                            nodehtml+="<div class='alert_box' style=''><div  alert_type='"+alert_type+"' scheduler_id='"+sc_id+"' ";
	                            nodehtml+=(access_class.indexOf("access_readwrite")>=0) ? " onmouseover='scd_buildAlertMenu(this)' ":"";                             
	                            nodehtml+="style='position: relative;' class='dropdown'><img src='images/"+a_icon+"'></div></div><small> "+sc_id+" </small>";
	                            
	                            var noPrivMsg = "No Priviledge to activate or deativate the script";
	                            nodehtml+="<img class='scd_togglebtn' scheduler_id='"+sc_id+"' active_value='"+data1.scheduleditems[iac].active+"'  title='Activate or Deactive' ";
	                            nodehtml+=(access_class.trim() != "access_readwrite" && access_class.indexOf("access_rwx") == -1 ) ? " onclick='message_status(\"" + noPrivMsg + "\")'" : " onclick='scd_toggleActiveate(this)' ";
	                            //nodehtml+=" cls='" + access_class + "' ";
	                            nodehtml+=" src='"+activimag+"' style='margin-right:5px;cursor:pointer;vertical-align:middle'>";                            
	                            //nodehtml+="<input type='button' class='scd_itembtn scd_edit' scheduler_id='"+sc_id+"' scheduler_name='"+data1.scheduleditems[iac].name+"' value='"+(access_class.indexOf("access_readwrite")>=0 ?"Edit":"View")+"' onclick='scd_loadTask(this)'>";
	                            //if(view_btn)
	                            	//nodehtml+="<input type='button' class='scd_itembtn scd_edit' scheduler_id='"+sc_id+"' scheduler_name='"+data1.scheduleditems[iac].name+"' value='"+"View"+"' onclick='scd_loadTask(this)'>";
	                            //fix that removes view button for restricted scripts
	                            if(access_class.indexOf("access_readwrite")>=0)
	                            {
	                            	if(execonly_btn)
	                            		nodehtml+="<input type='button' class='scd_itembtn scd_edit' scheduler_id='"+sc_id+"' scheduler_name='"+data1.scheduleditems[iac].name+"' value='View' onclick='scd_loadTask(this)'>";
	                            	else
	                            		nodehtml+="<input type='button' class='scd_itembtn scd_edit' scheduler_id='"+sc_id+"' scheduler_name='"+data1.scheduleditems[iac].name+"' value='Edit' onclick='scd_loadTask(this)'>";    
	                            }
	                            else
	                        	{
	                        		if(view_btn)
	                        			{
	                        				nodehtml+="<input type='button' class='scd_itembtn scd_edit' scheduler_id='"+sc_id+"' scheduler_name='"+data1.scheduleditems[iac].name+"' value='View' onclick='scd_loadTask(this)'>";
	                        				
	                        			}
	                        	}
	                            if(!view_btn)
	                            	nodehtml+="<input type='button' class='scd_itembtn scd_delete' scheduler_id='"+sc_id+"' value='Del' title='Delete Task' onclick='scd_deleteTask(this)'>";
	
	                            nodehtml+="<input type='button' class='scd_itembtn' scheduler_name='"+data1.scheduleditems[iac].name+"' scheduler_id='"+sc_id+"' value='Log' title='Log History' onclick='scd_getLog(this)'>";
	                            
	                            if(!view_btn || execonly_btn)
	                            	nodehtml+="<input type='button' class='scd_itembtn scd_execute' scheduler_id='"+sc_id+"' value='Exc' title='Execute Now' onclick='scd_executeTaskNow(this)'>";                           
	                            if(data1.scheduleditems[iac].is_wiki_done!=null && data1.scheduleditems[iac].is_wiki_done){
	                                nodehtml+="<span><img src='images/white_box.png' border='0' style='opacity:0.1'/></span>";
	                            }else{                              
	                                nodehtml+="<span title='No Wiki Page for this Function' style='background-image: url(images/wiki_warn.png);background-position: 3px 3px; background-repeat: no-repeat;'><img src='images/wiki.png' border='0' style='opacity:0.7'/></span>";
	                            }                           
	                            nodehtml+="</div></div>";
	                            
	                            var item="<li taskitem='yes' class='"+access_class+"' scheduler_id='"+sc_id+"'>"+nodehtml+"</li>";
	                            
	                            // var oldfid=$j(treeSelector + ' #' +
	                            // htmlid).attr("folderid");
	                            var oldfid=domhtmlid.attr("folderid");
	                            
	                            // if($j(treeSelector + ' #' + htmlid).length > 0 &&
	                            // oldfid==folderid){
	                             
	                            
	                            if(domhtmlid.length > 0 && oldfid==folderid){
	                                    // $j(treeSelector + ' #' +
	                                    // htmlid).parent().replaceWith(item);
	                                    domhtmlid.parent().replaceWith(item);
	                                    
	                            }else{
	                                // $j(treeSelector + ' #' +
	                                // htmlid).parent().remove();
	                                domhtmlid.parent().remove();
	                                // domhtmlid.parent().fadeOut(1000, function(){
	                                // $j(this).remove();});
	                                
	                                if (folderid == null || (folderid != null && folderid == 0) || data1.scheduleditems[iac].folder_name==null) {
	                                    
	                                    // $j(treeSelector + ' ul.filetree
	                                    // li[groupname="' + taskid + '"]
	                                    // ul[groupname="' + taskid +
	                                    // '"]').append(item);
	                                    
	                                    // main folder are missing.
	                                    // if(folderid>0 &&
	                                    // data1.scheduleditems[iac].folder_name==null
	                                    // && $j("ul#"+ulgrpid).length>0){
	                                    // pop_warning="Warning: One or more folders
	                                    // are corrupted! <input type='button'
	                                    // value='Repair'
	                                    // onclick='scheduler_fldr_repair()'
	                                    // style='margin:0px 20px'>"
	                                    // }
	                                    $j("ul#"+ulgrpid).append(item);
	                                    
	                                    
	                                }else {
	                                        // $j(treeSelector + ' ul.filetree
	                                        // ul[folderid="' + folderid +
	                                        // '"]').append(item);
	                                        
	                                        foldeulid=folderULs[folderid];
	                                        if (foldeulid != null) {                                            
	                                            
	                                            if($j('ul#'+foldeulid).length==0 && $j("ul#"+ulgrpid).length>0){    
	                                            	alert("val = "  + $j("ul#"+ulgrpid).val);
	                                                pop_warning="Warning : One or more folders are corrupted! <input type='button' value='Repair' onclick='scheduler_fldr_repair()' style='margin:0px 20px'>"
	                                            }
	                                            $j('ul#' + foldeulid).append(item);
	                                            
	                                        }else{
	                                            // incase of adding new task
	                                            $j(treeSelector + ' ul.filetree ul[folderid="' + folderid + '"]').append(item);
	                                        }
	                                }
	
	                            }
	                            // if(data1.scheduleditems[iac])
                            
                            
                            }      
                            
 
                }   
            }
            
            
            
            /**
             * this block will be invoked only on load (first time)
             */
            if (!treeMenuRendered) {                
                     
                    consoleLogTime("setting access privilege");
                     
                    $j(treeSelector+" div.treeTaskItem > span:not([access='access_noaccess'])").each(function(idx,el){
                        var idx=scd_tasks_autosug.length;
                        scd_tasks_autosug[idx]=new Object();
                        scd_tasks_autosug[idx].id=$j(el).parent().parent().attr("scheduler_id");
                        scd_tasks_autosug[idx].name=$j(el).html();
                    
                    });
                
                    if(false){
                        $j("#scd_searchTasks input").autoSuggest(scd_tasks_autosug, {
                            startText: "Type Task Names",
                            selectedItemProp: "name",
                            searchObjProps: "name",
                            selectedValuesProp: "id"
                            
                        });
                    }else{
                        $j('#scd_searchTasksInp').multiSelect({ 
                            width:"100%",
                            postcall:scd_search,
                            source: function( request, response ) {
                                var xhr = new XMLHttpRequest();
                                xhr.open("GET", "autocomplete_scheduler.jsp?xhr_scheduler=yes&term="+request.term, true);
                                
                                xhr.onreadystatechange = function() {
                                  if (xhr.readyState == 4) {             
                                    //var resp = eval("var data=" + xhr.responseText + "");
                                    var data=JSON.parse(xhr.responseText);
                                    var respdata=[];
                                    var selected=$j('#scd_searchTasksInp').multiSelect("getSelected");
                                    for(iab=0;iab<data.length;iab++){
                                        var loc="";
                                        //var loc="<span class='group'>";
                                        //loc+=data[iab].icon!=null? "<img src='"+data[iab].icon+"'>":"";
                                        //loc+=data[iab].group_name+"</span>";
                                        loc+=(data[iab].folder_name!=null? "<span class='specific'> "+data[iab].folder_name.replace(/\//g, " > ")+"</span>":"");
                                        var dt=data[iab].edited_datetime!=null
                                                            ?moment(data[iab].edited_datetime, "YYYY-MM-DD HH:mm:ss").fromNow()
                                                            :"";
                                        if(dt!=null){
                                            dt+=data[iab].username!=null ? " ("+data[iab].username+")" :"";
                                        }           
                                        var themses="";
                                        if(data[iab].stags!=null){
                                            var arr=data[iab].stags.split(",");
                                            arr.forEach(function(ele){
                                                themses+=ele.indexOf("thm-")>=0 ? "<div class='tag'>"+ele.substring(ele.indexOf("thm-")+4,ele.length)+"</div>":"";
                                            });
                                        }
                                        var icon="";
                                        var bgimage=(data[iab].icon!=null && data[iab].icon!='') ? "background: url('"+data[iab].icon+"') no-repeat center center;":"";                                     
                                        icon+="<div style=\"width:30px;"+bgimage+"\" class='iconbox'>";                                     
                                        icon+="</div>";
                                        if($j.inArray(data[iab].id+"",selected)==-1){
                                            var obj={
                                                value:data[iab].name                        
                                                ,label:icon+"<div class='autocomp_item'><div class='item_name'><span style='display:inline-block;float:right;padding-right:5px;'>"+data[iab].id+"</span>"+data[iab].name+"</div>  <div class='item_foot'><div class='item_f1'>"+loc+"</div><div class='item_f3'>"+themses+"</div><div class='item_f2'>"+dt+"</div></div></div>"
                                                ,uid:data[iab].id
                                            }
                                            respdata[respdata.length]=obj;
                                        }
                                        
                                    } 
                                    response(respdata);             
                                  }
                                }
                                xhr.send();
                            }
                        });
                    
                        
                    }
                
                 jqtree = $j(treeSelector + " ul.filetree").treeview({
                    animated: 100,
                    unique: false,
                    persist: "cookie",
                    cookieId: "navigationtree"
                     

                });
                
                /**
                 * show greyed color folder if no content in the folder.
                 */
                
                
                
                $j(treeSelector + " ul[folderid]").each(function(idx, el){
                    if ($j(el).children("li").length == 0) {
                        // $j(el).parent().css("color", "#D0D0D0");
                        $j(el).parent().addClass("emptyfoldercls");
                        $j(el).parent().attr("emptyfolder", "yes");                 
                    }
                });
                
                
                
            if(full_version){
                /**
                 * enable drag and drop
                 */
                $j(treeSelector + " ul[folderid]").parent().draggable({
                    revert: true,
                    handle:"span.folder",
                    // drag:function(event,ui){
                        // console.log("event:"+event);
                        // console.log("event:"+ui);
                        // $j(this).children("div.hitarea").trigger('click');
                    // }
                    
                });


                /*
                 * happens when you drop into main group
                 */             
                $j(treeSelector + " div[groupTarget]").droppable({
                    hoverClass: "ui-state-active",
                    // hoverClass:"ui-droppable-active",
                    // accept1: '[foldername][folderitem="yes"]',
                    // accept: ('[foldername][folderitem="yes"]',
                    // 'li[taskitem="yes"]'),
                    accept: function(ui) {
                        // alert(ui.attr('foldername'));
                        // return true;
                        if(ui.attr("folderitem")=="yes" || ui.attr("taskitem")=="yes"){
                            return true;
                        }else{
                            return false;
                        }
                    },
                    drop: function(event, ui){
                        var tobj = this;
                        var sobj = ui.draggable;
                        
                        // if($j("#scd_taskpane_holder
                        // div[id^='scd_tabs-']").length>0){
                        // alert("Please close all the opened task on right side
                        // before moving folder or task");
                        // return ;
                        // }
        
                        if (sobj.attr("folderitem") == "yes") {
                            var cb = function(){
                                $j(treeSelector + ' ul.filetree  li[groupname="' + $j(tobj).attr("groupTarget") + '"]').append(sobj);
                            }
                            moveFolder(this, ui.draggable, cb);
                        }
                        if (sobj.attr("taskitem") == "yes") {
                            var scd_id=$j(sobj).attr("scheduler_id");
                            var taskuid=$j(tobj).attr("groupTarget");                           
                            var cb = function(data){
                                    message_status(data);                               
                                    $j(treeSelector + ' ul.filetree  li[groupname="' + $j(tobj).attr("groupTarget") + '"]').append(sobj);                               
                            }
                            progress_message("Please wait while moving task")
                            SchedulerMgmt.move2RootGroup(parseInt(scd_id),taskuid,cb);
                            // cb();
                        }
                        
                    }
                });
                
                /*
                 * happens when you drop into another folder.
                 */
                
                $j(treeSelector + " span.folder").droppable({
                    hoverClass1: "ui-state-active",
                    hoverClass:"ui-droppable-active",
                    accept: '[foldername][folderitem="yes"]', 
                    drop: function(event, ui){
                        // if($j("#scd_taskpane_holder
                        // div[id^='scd_tabs-']").length>0){
                        // alert("Please close all the opened task on right side
                        // before moving folder or task");
                        // return ;
                        // }
                        var tobj=this;
                        var sobj=ui.draggable[0];
                        var cb = function(){
                            var sel = $j(tobj).parent().children("ul[folderid]");
                            sel.append($j(sobj));
                        }           
                        moveFolder(this,ui.draggable[0],cb);
 
                        
                    }
                });
                
                
                var menu1 = [ 
                  {'Create Subfolder': {
                    onclick: function(menuItem, menu){                      
                        // alert($j(this).parent().children("ul[folderid]").attr('foldername'));
                        var fldrname=$j(this).parent(" li[foldername][taskuid]").attr('foldername');
                        var tuid=$j(this).parent(" li[foldername][taskuid]").attr('taskuid');
                     
                        setTimeout(function(){ctxCreateFolder(fldrname,tuid);},100);
                    },icon:'images/icon_folder_new.gif'  
                  }}, 
                  
                  $j.contextMenu.separator, 
                  
                  {'Rename':{onclick:function(menuItem,menu) { 
                        // alert("You clicked Option 2!");
                        var fldrname=$j(this).parent(" li[foldername][taskuid]").attr('foldername');
                        var tuid=$j(this).parent(" li[foldername][taskuid]").attr('taskuid');
                        var cfolder=$j(this).text();
                        setTimeout(function(){ctxRenameFolder(fldrname,cfolder,tuid); },100);
                     },
                     icon:'images/icon_folder_rename.gif' }
                  } 
                ]; 
                
                var menu2 = [ 
                  {'Create Subfolder':{onclick:function(menuItem,menu) { 
                        var fldrname=$j(this).parent(" li[foldername][taskuid]").attr('foldername');
                        var tuid=$j(this).parent(" li[foldername][taskuid]").attr('taskuid');
                        setTimeout(function(){ctxCreateFolder(fldrname,tuid);},100);
                    },
                    icon:'images/icon_folder_new.gif' 
                  }},
                   
                  $j.contextMenu.separator,
                   
                  {'Rename':{onclick:function(menuItem,menu) { 
                        var fldrname=$j(this).parent(" li[foldername][taskuid]").attr('foldername');
                        var tuid=$j(this).parent(" li[foldername][taskuid]").attr('taskuid');
                        var cfolder=$j(this).text();
                         setTimeout(function(){ctxRenameFolder(fldrname,cfolder,tuid); },100); 
                    },
                    icon:'images/icon_folder_rename.gif' }
                  },
                  
                  $j.contextMenu.separator,
                   
                  {'Delete':{onclick:function(menuItem,menu) {
                     if (confirm("Are you sure you wish to delete?")) {
                        var tobj=this;
                        var folderid=$j(this).parent().children(" ul[folderid]").attr('folderid');
                        // SchedulerMgmt.deleteFolder(folderid,
                        // function(data){$j(tobj).parent().remove();message_status("Folder
                        // deteled")});
                        SchedulerMgmt.deleteFolder(folderid, function(data){$j(tobj).parent().fadeOut(1000, function(){ $j(this).remove();});message_status("Folder deleted")});
                     }
                    },
                    icon:'images/icon_folder_delete.gif'} 
                  }
                ]; 

                
                var mOptions={
                    theme:'vista',
                    showSpeed:100, hideSpeed:100, showTransition:'fadeIn', hideTransition:'fadeOut', 
                    beforeShow: function() {
                        // var
                        // foldername=$j(this.target).parent().attr("foldername");
                        // console.log('foldername:'+foldername);
                        return true; 
                    }, 
                    showCallback:function(){
                        // var
                        // foldername=$j(this.target).parent().attr("foldername");
                        // console.log('on opening foldername:');
                        if(prevCtxClk){
                            $j(prevCtxClk).removeClass("treefldrHld");
                        }
                        $j(this.target).addClass("treefldrHld");
                        prevCtxClk=this.target;
                    },
                    hideCallback:function(){
                        // var
                        // foldername=$j(this.target).parent().attr("foldername");
                        // console.log('on closing foldername:');
                        $j(this.target).removeClass("treefldrHld");
                    }
                }
                
                /*
                 * $j(treeSelector +'
                 * li[folderitem="yes"]:not(li[emptyfolder="yes"])').each(
                 * function(idx,el){
                 * $j(el).children("span.folder").contextMenu(menu1,mOptions); } );
                 */
                $j(treeSelector +' li[folderitem="yes"]:not(li[emptyfolder="yes"])').children("span.folder").contextMenu(menu1,mOptions);  
                
                $j(treeSelector +' li[folderitem="yes"][emptyfolder="yes"]').children("span.folder").contextMenu(menu2,mOptions); 
                
                
                
                
                
                // enable draggable feature for function name
                $j(treeSelector + ' li[scheduler_id][taskitem="yes"]').draggable({
                    revert: true,
                    handle:"div.treeTaskItem",
                
                });
                
                
                
                // accept dragged items into the folder
                $j(treeSelector + ' li[foldername][folderitem="yes"]').droppable({                  
                    hoverClass:"ui-droppable-active",
                    accept: 'li[taskitem="yes"]', 
                    greedy: true,
                    drop: function(event, ui){
                        
                        // if($j("#scd_taskpane_holder
                        // div[id^='scd_tabs-']").length>0){
                        // alert("Please close all the opened task on right side
                        // before moving folder or task");
                        // return ;
                        // }
                        
                        var tobj=this;
                        var sobj=ui.draggable[0];
                        var sel = $j(tobj).children("ul[folderid]");    
                        var cb = function(data){
                            progress_message(); 
                            if (data!=null) {            
                                message_status(data);
                                sel.append($j(sobj));                                 
                            }
                        }
                        // console.log("on drop called");
                        var new_folder_id=$j(sel).attr("folderid");                      
                        var scd_id=$j(sobj).attr("scheduler_id");                        
 
                        progress_message("Please wait...");
                        // console.log("scheduler_id:"+scd_id+"
                        // folder_id:"+new_folder_id);
                        // console.log("taskuid1:"+taskuid1+"
                        // taskuid2:"+taskuid2);
                        // cb(true);
                        SchedulerMgmt.updateTaskFolder(parseInt(scd_id),parseInt(new_folder_id),cb)
 
                    }
                });
                
                
                /*
                 * $j(treeSelector +'
                 * li[folderitem="yes"][emptyfolder="yes"]').each(
                 * function(idx,el){
                 * $j(el).children("span.folder").contextMenu(menu2,mOptions); } );
                 */  
               $j(treeSelector + ' span.folder').unbind('click',false); 
               $j(treeSelector + ' span.folder').css('cursor','move');
               
               
               
               /**
                 * Re-arrage the group and saves the status.
                 * 
                 * @param {Object}
                 *            event
                 * @param {Object}
                 *            ui
                 */
               $j(treeSelector + ' ul.treeview').sortable(
                {stop:
                    function(event,ui){
                           var items=$j(treeSelector + ' ul.treeview li[groupname]');
                           var taskuids=new Array();
                           for(iab=0;iab<items.length;iab++){
                               // console.log("
                                // script:"+$j(items[iab]).attr("groupname"));
                               taskuids[taskuids.length]=$j(items[iab]).attr("groupname");
                           }
                           SchedulerMgmt.setGroupOrder(taskuids,function(){});
                    },
                    handle:'img.dragholder',
                }
               );
               
               
              } // full version
            }else{
                if(full_version){
                    // after menu rendered, enable drag and drop for newly
                    // created item.
                    $j(treeSelector + ' li[scheduler_id][taskitem="yes"]:not(.ui-draggable)').draggable({
                        revert: true,
                        handle:"div.treeTaskItem",
                    });
                }
            }
            treeMenuRendered=true;
            
            consoleLogTime("at last");
    }
    if(pop_warning!=null) {
        message_status(pop_warning);
    }
}

function scheduler_fldr_repair() {
    var respBack=function(data){
        progress_message(); 
    }
    progress_message("please wait");
    SchedulerMgmt.repairFolders(respBack);
}

function scd_buildAlertMenu(divobj) {
           var div=$j(divobj).parent();
           var alert_type=$j(divobj).attr("alert_type");
           var sc_id=$j(divobj).attr("scheduler_id");
           $j(divobj).remove();
            new Dropdown({
                        mode: 'form',
                        data: [{img:'images/alert_none.png',text:'',val:'',scheduler_id:sc_id,title:'No Alert on Failure/Error/Timeout'}, 
                               {img:'images/alert_phoneemail.png',text:'',scheduler_id:sc_id,val:'phone',title:'Alert theme members via Phone & Email on Failure/Error/Timeout'}, 
                               {img:'images/alert_email.png',text:'',scheduler_id:sc_id,val:'email',title:'Alert theme members via Email on Failure/Error/Timeout'}
                              ],
                        fieldName: 'animal',
                        appendTo:div,
                        selected:alert_type,
                        func:function(obj){
                            var scheduler_id=parseInt($j(obj).attr("scheduler_id"));
                            var alert_type=$j(obj).attr("val");
                            progress_message("Please wait while updating alert");
                            SchedulerMgmt.updateAlertType(alert_type,scheduler_id,function(bool){message_status("Alert updated")})
                        }
            });
}

function moveFolder(thisobj,source,cbfun){
        var staskuid=$j(source).attr("taskuid");        
        var spath=$j(source).attr("foldername");
        var folder="";
        var parentf="";
        sparray=spath.split("/");
        if(spath!=null && spath!='' && sparray.length>1){       
                            
            for(iab=0;iab<(sparray.length-1);iab++){
                parentf+=(iab==0)?sparray[iab]:"/"+sparray[iab];
            }
            folder=sparray[sparray.length-1];
        }else{
            folder=spath;
        }
        var tfolder=""; 
        var ttaskuid="" ;                       
        if ($j(thisobj).parent().attr("taskuid") != null) {
            tfolder=$j(thisobj).parent().attr("foldername");
            ttaskuid=$j(thisobj).parent().attr("taskuid");
        }else{
            ttaskuid=$j(thisobj).parent().attr("groupname");
        }
        if(staskuid.substring(0,3)!=ttaskuid.substring(0,3)){
            alert("Moving completely different type of tasks into this group are not allowed");
            return false;
        }
        if (confirm("Are you really wish to move folder?")) {
            var respBack = function(data){
                var sel=$j(thisobj).parent().children("ul[folderid]");
                sel.append($j(source));
                
                $j(thisobj).parent().removeAttr("emptyfolder");
                $j(thisobj).parent().removeClass("emptyfoldercls");
        
                cbfun.call(this, data);
            }
            SchedulerMgmt.moveFolderContents(folder, staskuid, parentf, ttaskuid, tfolder, respBack);
        }
}

var prevCtxClk=null;
function ctxCreateFolder(parentfolder, taskuid){
    var fname=prompt("Folder:");
    if(fname!=null && fname!=''){
        var fname1=fname;
        if(parentfolder!=null && parentfolder!=''){
            fname1=parentfolder+"/"+fname
        }
        var respBack=function(data){
        	
            progress_message();
            scd_treeListGen(data); //data null
            location.reload(true);
        } 
        progress_message("Please wait..");
        SchedulerMgmt.addFolder(taskuid,fname1,respBack);
    }
}

function validateFolderName(flName){
	var regexp = new RegExp("^[A-Za-z][-A-Za-z0-9_ ]*$");
	return regexp.test(flName);
}

function ctxRenameFolder(folderfullpath,foldername,taskuid){
     var newname=prompt("Foldername:",foldername);
     if (newname != null && newname != '' && foldername != newname) {
    	if (validateFolderName(newname)){
	        var newpath = "";
	        if (folderfullpath.indexOf("/") >= 0) {
	            var folders = folderfullpath.split("/");
	            for (iae = 0; iae < (folders.length - 1); iae++) {
	                newpath += (iae == 0) ? folders[iae] : "/" + folders[iae];
	            }
	            newpath += "/" + newname;
	        }
	        else {
	            newpath += newname;
	        }
	        var respBack = function(data){
	            progress_message();
	            location.reload(true);
	        }
	        progress_message("Please wait..")
	        SchedulerMgmt.renameFolder(taskuid, folderfullpath, newpath, respBack);
    	} else {
    		message_status("Folder Name NOT Valid, Please use underscore(_), space( ), or minus(-)")
    	}
    }
}

var trashDialog=null;
function scd_openTrash(){
    if(trashDialog==null){
        trashDialog=$j( "#dialog_trash" ).dialog({resizable: false,width:850,modal: true,dialogClass: 'comment_dialog showheader'});
    }else{
        $j( "#dialog_trash" ).dialog("open");
    }
    
    $j("#dialog_trash_bdy").html("<span style='color:#E01B3F;padding:20px 10px 10px 10px;font-size:1.2em;'>Please wait while building tree..</small>");
    
    var respBack=function(data){
          if(data==null){
            return null;
          }
          $j("#dialog_trash_bdy").html("<ul class='trashList'></ul>")
          for(iac=0;iac<data.length;iac++){
                        
                var taskid=data[iac].taskuid
                var sc_id=data[iac].id;                     

                var nodehtml="<div  class='trashItem treeTaskItem'>";
                
                var tskname=$j.trim(data[iac].name);
                tskname=(tskname.length>=35) ? tskname.substring(0,35)+"...":tskname;                                               
                nodehtml+="<span font-size:.9em' title='"+data[iac].name+"'>"+tskname+"</span>";                            

                // tag manipulation start ~~~~~~
                var t1="";
                var leng_tag="";
                var more_tags="";
                if (data[iac].stags != null && data[iac].stags != '') {
                    var tags=data[iac].stags.split(",");
                    
                    for(iae=0;iae<tags.length;iae++){
                        var tg=$j.trim(tags[iae]);                                  
                        tg1=(tg.indexOf("-")>0)?tg.substring(tg.indexOf("-")+1):tg;
                        leng_tag+=tg1;
                        if (tg != '') {
                            if (leng_tag.length < 45) {
                                var clas = "task_tag ";
                                if (scd_TagColors[tg] != null && scd_TagColors[tg] != '') {
                                    t1 += "<span class='" + clas + "' style='background-color:" + scd_TagColors[tg] + "'>" + tg1 + "</span>";
                                } else {
                                    t1 += "<span class='" + clas + "'>" + tg1 + "</span>";
                                }
                            }else{
                                more_tags="<span class='task_tag task_tag_more'>...</span>";            
                            }                               
                        }                                   
                    }
                }
                t1+=more_tags;
                                            
                nodehtml+="<div class='task_holder' style='margin-left:10px'>"+t1+"</div> <small style='margin-left:15px'>"+data[iac].deletedon_format+"</small>";                      
                nodehtml+="<div style='float:right;margin-right:7px;'>";                            
                nodehtml+="<input type='button' class='trash_button' scheduler_id='"+sc_id+"' value='Put Back' onclick='scd_putback(this)'>";
                
                if(data[iac].deleteddays!=null && parseInt(data[iac].deleteddays)>=7){
                    nodehtml+="<input type='button' class='trash_button' scheduler_id='"+sc_id+"' value='Purge' onclick='scd_purgeItem(this)'>";
                }
                
                nodehtml+="</div></div>";
                var item="<li  scheduler_id='"+sc_id+"'>"+nodehtml+"</li>";
                $j("#dialog_trash_bdy ul.trashList").append(item);
            }

    };
    SchedulerMgmt.trashedItems(respBack);
}

function scd_putback(thisobj) {
    var scd_id=$j(thisobj).attr("scheduler_id");
    var respBack=function(data){        
        
        $j(".trashList li[scheduler_id='"+scd_id+"']").fadeOut(1000, function(){ $(this).remove();});
        
        // $j(".trashList li[scheduler_id='"+scd_id+"']").remove();
        scd_treeListGen(data);
    }
    SchedulerMgmt.putBackTask(scd_id,respBack);
}

function scd_purgeItem(thisobj) {
    var scd_id=$j(thisobj).attr("scheduler_id");
    var respBack=function(data){
        progress_message();     
        if(data){
            message_status("Deleted Task has been purged successfully");
            $j(".trashList li[scheduler_id='"+scd_id+"']").fadeOut(1000, function(){ $(this).remove();});
        }
    }
  if(confirm("This option will remove the function perminently, Are you really sure ?")) {  
    SchedulerMgmt.purgeTask(parseInt(scd_id),respBack);
    progress_message("Please wait while processing your request");
   }
}

function scd_searchRefresh(data1){
    if(data1.scheduleditems==null){
            message_status("No task found");
    }   
        
    if (data1.scheduleditems != null) {
                
            
                
        $j(treeSelector +" li[taskitem='yes']").each(
            function(idx,el){
               $j(el).hide();   
            }
        );
        
        $j(treeSelector +' li[folderitem="yes"]').each(
            function(idx,el){
               $j(el).hide();   
            }
        );
        
        
        
        for (iac = 0; iac < data1.scheduleditems.length; iac++) {
            var taskid = data1.scheduleditems[iac].taskuid
            var sc_id = data1.scheduleditems[iac].id;
            var foldername=data1.scheduleditems[iac].folder_name;           
            var folderid = data1.scheduleditems[iac].folder_id;
            
        
            // show task item folder.
            var uniqfolder1=taskid+"_"+foldername;
            $j(treeSelector + ' span[uniquefid="' + uniqfolder1 + '"]').parent().show();
            // $j(treeSelector + ' span[uniquefid="' + uniqfolder1 +
            // '"]').parent('li.expandable[folderitem="yes"]').children("div.hitarea").trigger('click');
            $j(treeSelector + ' span[uniquefid="' + uniqfolder1 + '"]').parent('li[folderitem="yes"]').children("div.hitarea").trigger('click');
            
            // show task item's all parent folders.
            if (foldername != null) {
                var fsplit = foldername.split("/");
                var uniqfolder = taskid + "_";
                for (iad = 0; iad < fsplit.length; iad++) {
                    // var uniqfolder=taskid+"_"+fsplit[iad];
                    uniqfolder += fsplit[iad]
                    $j(treeSelector + ' span[uniquefid="' + uniqfolder + '"]').parent().show();
                    // $j(treeSelector + ' span[uniquefid="' + uniqfolder +
                    // '"]').parent('li.expandable[folderitem="yes"]').children('div.hitarea').trigger('click');
                    $j(treeSelector + ' span[uniquefid="' + uniqfolder + '"]').parent('li[folderitem="yes"]').children('div.hitarea').trigger('click');
                    uniqfolder += "/"; // for next loop add the path separator
                }
            }
            var htmlid="scheduler_id_"+sc_id;           
            if ($j(treeSelector + ' #' + htmlid).length > 0) {
                $j(treeSelector + ' #' + htmlid).parent().show();
            }
            
        }
        // jqtree.treeview({collapsed:false});
        $j(treeSelector + ' ul.filetree  li.expandable[groupname]').each(
            function(idx,el){
             $j(el).children("div.hitarea").trigger('click');
            } 
        );
        
    }
    
    
}

var scd_MainMnArry=new Array();
var scd_FolderMnArry=new Array();
var scd_taskRelation=new Object();
var scd_tree=null;

var scd_ddsources=new Hash();
var scd_ddtargets=new Hash();
var scd_logDialogBox=null;
function scd_getLog(thisobj){
    

    var schedulername=thisobj.getAttribute("scheduler_name");   
    var taskid=thisobj.getAttribute("scheduler_id");
    
    var respBack=function(data){ 
        progress_message(null);
        // scd_treeListGen(data);
        if(data!=null && typeof data=='object'){
 
            
            $j("#scd_logDialogbox").commonDialog({width:600, height:750,modal:true});
            $j("#scd_logDialogbox").commonDialog({title:"Log entries "+schedulername+" ("+taskid+")"});
        
            // scd_genQLogs(data,"scd_logDialogboxBD","pa_matrix_tbl");
            
            var divid="scd_logDialogboxBD";
            for(iab=0;iab<data.length;iab++){
                
                // data[iab].taskuidimg="<img
                // src='images/"+data[iab].taskuid+".gif'
                // alt='"+data[iab].taskuid+"' border='0'>";
                data[iab].taskuidimg="<img src='"+scd_groupIcons[data[iab].taskuid]+"' alt='"+data[iab].taskuid+"' border='0'>";
                // scd_groupIcons
                
                 
                data[iab].statimage = "<img src='images/task_failed.gif' alt='Failed' border='0'>";
                if (data[iab].executing!=null && data[iab].executing==1 && data[iab].queued != null && data[iab].queued == 1) {
                    // data[iab].statimage="<img src='images/task_queued.gif'
                    // alt='Excecuted' border='0'>&nbsp;&nbsp;"+data[iab].name;
                        data[iab].statimage = "<img src='images/task_executing.gif' alt='Excecuting' border='0'>";
                  }else if(data[iab].queued!=null && data[iab].queued==1){
                    // data[iab].statimage="<img src='images/task_queued.gif'
                    // alt='Excecuted' border='0'>&nbsp;&nbsp;"+data[iab].name;
                    data[iab].statimage="<img src='images/task_queued.gif' alt='Excecuted' border='0'>";
                 }else if(data[iab].is_triggered!=null && data[iab].is_triggered==1 && data[iab].queued!=null && data[iab].queued==0){
                    // data[iab].statimage="<img src='images/task_executed.gif'
                    // alt='Excecuted' border='0'>&nbsp;&nbsp;"+data[iab].name;
                    data[iab].statimage="<img src='images/task_executed.gif' alt='Excecuted' border='0'>";
                }else{
                    // data[iab].statimage="&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"+data[iab].name;
                    data[iab].statimage="";
                }
                 
                
                data[iab].logs="<a href='#' onclick='scd_ExecLogs(this); return false;' unique_id='" + data[iab].unique_id + "' ttl='" + data[iab].name +" ["+data[iab].trigger_time+"]"+ "'  ><img src='images/logbtn.gif' border='0'></a>";
                
                if(data[iab].inject_code_length!=null &&  data[iab].inject_code_length>0 && data[iab].executed_code_length!=null &&  data[iab].executed_code_length>0){
                    data[iab].inject="<a href='#' onclick='scd_showinject_code(this); return false;' unique_id='" + data[iab].unique_id + "' ttl='" + data[iab].name +" ["+data[iab].trigger_time+"]"+ "'  ><img src='images/inject.png' border='0'></a>";
                }else{
                    data[iab].inject="";
                }
                
                
                if (divid != null && data[iab].is_triggered!=null && data[iab].is_triggered==1 && data[iab].status=='fail') {       
                        if (data[iab].error_logid!=null) {
                            data[iab].statimage = "<a href='#' logid='"+data[iab].error_logid+"' onclick='scd_showQLogErrorMsg(this)'><img src='images/task_error.gif' alt='Failed' border='0'></a>";
                        } else {
                            data[iab].statimage = "<img src='images/task_error.gif' alt='Failed' border='0'>";
                        }
                        data[iab].status="Error";
                        // data[iab].duration = "<input type='button'
                        // value='Execute Now' log_id='"+ data[iab].qlog_id+"'
                        // scheduler_id='" + data[iab].scheduler_id + "'
                        // onclick=scd_executeTaskNow(this)>";
                }
                
                if (divid != null && data[iab].is_triggered!=null && data[iab].is_triggered==1 && data[iab].status=='warning') {        
                        if (data[iab].error_logid!=null) {
                            data[iab].statimage = "<a href='#' logid='"+data[iab].error_logid+"' onclick='scd_showQLogErrorMsg(this)'><img src='images/task_warning.gif' alt='Failed' border='0'></a>";
                        } else {
                            data[iab].statimage = "<img src='images/task_warning.gif' alt='Failed' border='0'>";
                        }
                        data[iab].status="Warning";
                        
                }
                
            
        
                
                if (divid != null && data[iab].status == 'success') {
                        data[iab].statimage = "<img src='images/task_executed.gif' alt='Re-Executed' border='0'>";
                }
                
                if (divid != null && data[iab].status == 're-executed') {
                        data[iab].statimage = "<img src='images/task_reexecuted.gif' alt='Re-Executed' border='0'>";
                }
                if (divid != null && data[iab].status == 'overlapped') {
                        data[iab].statimage = "<img src='images/task_overlapped.gif' alt='Overlapped' border='0'>";
                }
                if (divid != null && data[iab].status == 'dep_timeout') {
                        data[iab].statimage = "<img src='images/task_dep_timeout.gif' alt='Overlapped' border='0'>";
                }

                if (data[iab].status=='timeout') {  
                    data[iab].statimage = "<img src='images/task_timedout.png' alt='Timed-Out' border='0'>";                         
                                        
                }
                if(data[iab].statimage==''){
                    data[iab].statimage = "<img src='images/task_failed.gif' alt='Failed' border='0'>";
                }

            }
            
            var atable=new LinkTable(); 
            atable.init($("scd_logDialogboxBD"));
            atable.addColumn(" ","statimage","10px");           
            atable.addColumn("Scheduled","trigger_time","150px");           
            atable.addColumn("Delay(Mins)", "started_at", "60px");                  
            atable.addColumn("Duration", "duration", "40px");
            atable.addColumn("Status", "status", "40px");
            atable.addColumn("Executed On", "host", "40px");
            atable.addColumn("Log", "logs", "20px");
            atable.addColumn("", "inject", "20px");      
            atable.setRowId("unique_id");
            atable.selectionBox(false);
            atable.setTableClass("queuetable");
            atable.setTableWidth("100%");
     
            atable.updateDataNew(data); 

            
            
            /*
             * 
             * 
             * 
             * 
             * 
             * var atable=new LinkTable(); atable.init($("scd_logDialogboxBD"));
             * 
             * atable.addColumn(" ","statimage","10px");
             * atable.addColumn("ID","scheduler_id","37px");
             * atable.addColumn("Name","name","300px");
             * atable.addColumn("Type","taskuidimg","22px");
             * atable.addColumn("Scheduled","trigger_time","150px");
             * atable.addColumn("Delay(Mins)", "started_at", "60px");
             * atable.addColumn("Duration", "duration", "40px");
             * atable.addColumn("Status", "status", "40px");
             * atable.addColumn("Executed On", "host", "40px");
             * atable.addColumn("Log", "logs", "20px"); atable.addColumn("",
             * "inject", "20px"); atable.addColumn("Data", "db_action","120px");
             * 
             * atable.setRowId("unique_id"); atable.selectionBox(false);
             * atable.setTableClass("queuetable"); atable.setTableWidth("100%");
             * 
             * atable.updateDataNew(data);
             */     
        }
        
    }
    
     
    
    progress_message("Please wait...");

    // ?/var dobj=new Object();
    // dobj.query+= " a.scheduler_id="+taskid+" ";
     
    progress_message("Please wait...");
    SchedulerMgmt.getLast15Logs(parseInt(taskid),respBack); 
     
}

function scd_showLogMessages(thisobj,noclose) {
    var respBack=function(data){ 
        progress_message(null);
 
        if(data!=null){
            $("scd_logDialogboxMsgs").innerHTML="";
            var msg=document.createElement('div');
            msg.style.overflow="auto";
            var pre=document.createElement('textarea');
            pre.wrap="off";
            pre.cols="60";pre.rows="25";            
            pre.value=data;
            msg.appendChild(pre);
            var closebtn=document.createElement('input');
            closebtn.type="button";
            closebtn.value="<<< Back";
            closebtn.onclick=function(){
                $("scd_logDialogboxMsgs").hide();
                $("scd_logDialogboxBD").show();
            }
            var dv=document.createElement('div');
            dv.align="right";
            if(noclose!=null && noclose){               
            }else{
                dv.appendChild(closebtn);
            }
            
            $("scd_logDialogboxMsgs").appendChild(dv);
            $("scd_logDialogboxMsgs").appendChild(msg);
            $("scd_logDialogboxMsgs").show();
            $("scd_logDialogboxBD").hide();
        }else{
            alert("[No Message found]");
        }
    }
    
    if(thisobj.getAttribute("logid")!=null){
        SchedulerMgmt.getLogMessages(thisobj.getAttribute("logid"), respBack);
    }
    return false;
}

function scd_toggleActiveate(thisobj){
    var thisobj1=thisobj;
    var taskid=thisobj.getAttribute("scheduler_id");
    var a_value=thisobj.getAttribute("active_value");
    
    var respBack=function(data){
        thisobj1.setAttribute("active_value",data+"");
        thisobj1.src=(data==-1)?"images/pause_button.gif":"images/running_button.gif";
        progress_message(null);
        scd_refreshQueueOnClick();   
    }
    var msg="Are you sure you wish to "+(a_value=='-1'?"activate":"deactivate")+" this task now?"
    if (confirm(msg)) {
        progress_message("Please wait...");
        SchedulerMgmt.toggleActive(taskid, respBack);
    }
} 

function scd_executeTaskNow(thisobj){
    var taskid=thisobj.getAttribute("scheduler_id");
    var log_id=thisobj.getAttribute("log_id");
    if(log_id==null){
        log_id="0";
    }
    var executeFn=function() {
        progress_message("Please wait...");      
        $j("#dialog_runTask").dialog( "close" );
        var delay_minutes=parseInt($j("#dialog_runTask_delay").val())
        SchedulerMgmt.executeTask(taskid,log_id,delay_minutes,function(data){ 
            progress_message(null);                     
            message_status("Added into the execution queue")
            scd_genQLogs(data);
            if(log_id!=null && log_id!="0"){
                // $(thisobj).remove();
                $j(thisobj).fadeOut(1000, function(){ $j(this).remove();});
                
                scd_showHistory();
            }
        });
    }
                
    $j( "#dialog_runTask" ).dialog({
        resizable: false,            
        width:300,
        modal: true,        
        dialogClass: 'scd_dialog',      
        buttons: {
            "Cancel": function() {
                $j( this ).dialog( "close" );
            },                  
            "Execute": executeFn
        }
    });
    
    $j('#dialog_runTask').keyup(function(e) {
        if (e.keyCode == 13) {
            executeFn();
        }
    });
    $j('#dialog_runTask').dialog( "option", "title", 'Execute Task :'+taskid );
    $j('#dialog_runTask_delay').focus();
    
    
    

} 

function scd_deleteTask(thisobj){
    
    var taskid=thisobj.getAttribute("scheduler_id");
    
    var respBack=function(data){ 
        progress_message(null);
        // scd_treeListGen(data);
        
        var htmlid="scheduler_id_"+taskid;
        if ($j(treeSelector + ' #' + htmlid).length > 0) {
            $j(treeSelector + ' #' + htmlid).parent().fadeOut(1000, function(){ $(this).remove();});            
            // $j(treeSelector + ' #' + htmlid).parent().remove();
        }
        scd_refreshQueueOnClick();
        message_status("Task has been deleted");
        // $("scd_taskpane").hide();
    }
    
    
    if (confirm("Are you sure you wish to remove this task")) {
        progress_message("Please wait...");
        SchedulerMgmt.removeTask(taskid, respBack);
    }
    
}

function scd_copycombi_code(scd_id){
    var combo="scd_codeinject_preview_"+scd_id; 
    var rs_id=$j("#scd_form_general_"+scd_id+" textarea.flex_field_rscript").attr("id");    
    var hc_id=$j("#scd_form_codeinject_"+scd_id+" textarea.flex_field_rscript_param").attr("id");
    
    var ccode1=editAreaLoader.getValue(hc_id)+"\n"+editAreaLoader.getValue(rs_id);
    editAreaLoader.setValue(combo,ccode1);
    editAreaLoader.setSelectionRange(combo, 0, editAreaLoader.getValue(combo).length)
}

var previousLoadedTask=null;
var myTriggerTableObj=new Object();
             
var tmpl_txt="";    
tmpl_txt+='<div class="scd_lockedBy" id="scd_lockedBy_${task_id}"></div>'; 
tmpl_txt+=' <div width="100%" style1="border:0px solid #c0c0c0; margin-bottom:5px" style="margin:2px">';             
tmpl_txt+='     <div id="scd_taskTab_${task_id}"  class="tabs-bottom task-tabs" >';
tmpl_txt+='         <div align="right" style="display:inline;float:right;padding-top:3px;"><input id="scd_btn_save_${task_id}" z-index="1000" style="display:none" type="button" task_id="${task_id}" onclick="scd_gatherInputs(this)" value="Save"></div>';
tmpl_txt+='         <ul>';
tmpl_txt+='             <li><a href="#scd_tsktabs-1_${task_id}">Code</a></li>';
tmpl_txt+='             <li><a href="#scd_tsktabs-1a_${task_id}">Code Injection</a></li>';
tmpl_txt+='             <li><a href="#scd_tsktabs-1b_${task_id}">Dependencies</a></li> ';
tmpl_txt+='             <li><a href="#scd_tsktabs-2_${task_id}">Triggers</a></li>       ';                   
tmpl_txt+='             <li><a href="#scd_tsktabs-3_${task_id}">Revisions</a></li>';
tmpl_txt+='             <li><a href="#scd_tsktabs-4_${task_id}">Themes & Notification</a></li>'; 
tmpl_txt+='             <li><a href="#scd_tsktabs-5_${task_id}">Wiki</a></li>';
tmpl_txt+='         </ul>   ';
tmpl_txt+='         <div id="scd_tsktabs-1_${task_id}" class="chromeScroll" style="height:680px;overflow:auto">     ';
tmpl_txt+='             <div id="scd_form_general_${task_id}" style="padding:1px"><form class="scd_form"><table class="scd_form_table" width="100%" cellspacing="2" cellpadding="2"></table></form></div>';
tmpl_txt+='         </div>';
tmpl_txt+='         <div id="scd_tsktabs-1a_${task_id}" class="chromeScroll" style="height:680px;overflow:auto" >       ';
tmpl_txt+='             <h3 class="scd_subheader" style="margin-bottom:0px;">Header Code</h3>   ';
tmpl_txt+='             <div id="scd_form_codeinject_${task_id}" style="padding:1px"><form class="scd_form"><table class="scd_form_table" width="100%" cellspacing="2" cellpadding="2"></table></form></div>';
tmpl_txt+='             <h3 class="scd_subheader">Combined Preview</h3> ';
tmpl_txt+='             <div style="float:right"><input type="button" value="Hightlight Code" id="scd_codeinject_preview_hbtn_${task_id}"  onclick="scd_copycombi_code(${task_id})"></div>  ';
tmpl_txt+='             <div style="padding:1px;display:none;height:350px;font-size:1.2em;" class="scd_code_preview"><textarea style="width:100%;height:100%;" id="scd_codeinject_preview_${task_id}"></textarea></div>';
tmpl_txt+='         </div>';                
tmpl_txt+='         <div id="scd_tsktabs-1b_${task_id}" class="chromeScroll" style="height:680px;overflow:auto" >       ';
tmpl_txt+='             <h3 class="scd_subheader">Depends on</h3>   ';
tmpl_txt+='             <div id="scd_form_dependency_${task_id}" style="padding:1px"><form class="scd_form"><table class="scd_form_table" width="100%" cellspacing="2" cellpadding="2"></table></form></div>';
tmpl_txt+='             <h3 class="scd_subheader">Events</h3>   ';
tmpl_txt+='             <div><small>Run the tasks on an event<br>Example:</br>[23:inj]  your_code_here [inj]<br>[2454:inj] your_code_here [inj]<br><br>Or you can specify only ids 23,2454</div>    </small>';
tmpl_txt+='             <div id="scd_form_events_${task_id}" style="padding:1px"><form class="scd_form"><table class="scd_form_table" width="100%" cellspacing="2" cellpadding="2"></table></form></div>';
tmpl_txt+='         </div>';                    
tmpl_txt+='         <div id="scd_tsktabs-2_${task_id}"  class="chromeScroll" style="height:680px;overflow:auto">    ';
tmpl_txt+='             <h3 class="scd_subheader">Triggers</h3> ';  
tmpl_txt+='             <div id="trigger_table_${task_id}" style1="margin-left:30px"></div>';
tmpl_txt+='             <h3 class="scd_subheader">Building Block Related</h3>   ';
tmpl_txt+='             <div id="scd_form_buildingblock_${task_id}" style="padding:1px"><form class="scd_form"><table class="scd_form_table" width="100%" cellspacing="2" cellpadding="2"></table></form></div>';
tmpl_txt+='             <h3 class="scd_subheader">Concurrent Execution</h3>   ';
tmpl_txt+='             <div id="scd_form_concur_${task_id}" style="padding:1px"><form class="scd_form"><table class="scd_form_table" width="100%" cellspacing="2" cellpadding="2"></table></form></div>';
tmpl_txt+='             <h3 class="scd_subheader">Time Zone</h3>    ';
tmpl_txt += '           <table width="auto" style="margin-top:5px" cellpadding="0" cellpadding="0">';
tmpl_txt += '                    <tr>';
tmpl_txt += '                        <td align="right" width="50%">';
tmpl_txt += '                            TimeZone: ';
tmpl_txt += '                        </td>';
tmpl_txt += '                        <td valign="middle">';
tmpl_txt += '                            <table width="auto" cellpadding="0" cellpadding="0">';
tmpl_txt += '                                <tr>';
tmpl_txt += '                                    <td valign="top">';
tmpl_txt += '                                        <div id="scd_timeZone_${task_id}" style="width:200px">';
tmpl_txt += '                                            <div id="scd_tzContainer_${task_id}">';
tmpl_txt += '                                            </div>';
tmpl_txt += '                                            <input id="scd_tzInput_${task_id}" name="timezone" type="text">';
tmpl_txt += '                                        </div>';
tmpl_txt += '                                    </td>';
tmpl_txt += '                                    <td>';
tmpl_txt += '                                        <span class="scd_timezone_tips"><a href="#" ttype="tips" tips="Type few characters of Location for example: Singapore or New York. Leave empty if it is your local time. ">?</a></span>';
tmpl_txt += '                                    </td>';
tmpl_txt += '                               </tr>';
tmpl_txt += '                            </table>';
tmpl_txt += '                        </td>';
tmpl_txt += '                    </tr>';
tmpl_txt += '            </table>';
tmpl_txt += '         ';
tmpl_txt += '       </div>';
tmpl_txt+='         <div id="scd_tsktabs-3_${task_id}" class="chromeScroll" style="height:680px;overflow:auto" >        ';
tmpl_txt+='             <ul id="scd_revisionList_${task_id}" class="revisionList" style="padding:5px;"></ul>';
tmpl_txt+='         </div>';
tmpl_txt+='        <div id="scd_tsktabs-4_${task_id}">';             
tmpl_txt+='           <h3 class="scd_subheader">Tags</h3>   ';
tmpl_txt+='           <div class="tags_panel" style="width:796px"></div>';
tmpl_txt+='        </div>';
tmpl_txt+='        <div id="scd_tsktabs-5_${task_id}">';             
tmpl_txt+='           <div class="wiki_panel" task_name="${task_name}" style="height:89%"></div>';
tmpl_txt+='        </div>';
tmpl_txt+='     </div>';             
tmpl_txt+='</div>';


function scd_loadTask(thisobj,linkTaskWithTree){
    
    if (typeof thisobj == 'object') {
        var taskid = thisobj.getAttribute("scheduler_id");
        var scheduler_name = thisobj.getAttribute("scheduler_name");
    }else{
        taskid=thisobj+"";
    }
    
    scd_currentScTaskId=parseInt(taskid);
    
    var respBack=function(data,editrefresh){
        if(editrefresh==null){  
           editrefresh=false; // by default
        }
        
        
        if (data != null && data.taskdata!=null) {

            if (!editrefresh) {
                var tmpl = $j.template(tmpl_txt);
                scd_currentTabCotent = tmpl.apply({
                    task_id: scd_currentScTaskId + "",task_name:data.taskdata.name
                });
                scd_editorTab.tabs("add", "#scd_tabs-" + scd_currentScTaskId, data.taskdata.name);
            }else{
                $j('#scd_taskpane_holder ul.ui-tabs-nav li a[href="#scd_tabs-'+scd_currentScTaskId+'"]').html(data.taskdata.name);
            }
            var parent_id="scd_taskpane_"+scd_currentScTaskId;
            
            currentTaskUID = data.taskdata.taskuid;
            
            if (data.fields != null) {
                var flexdiv="scd_taskform_"+scd_currentScTaskId;
                scd_flexiForm(data,flexdiv , data.taskdata,scd_currentScTaskId);                
                
            }           
            $j("#scd_taskpane_"+scd_currentScTaskId).show();
            
            if (data.isAuthorized != null && data.isAuthorized) {
                $j("#scd_btn_save_"+scd_currentScTaskId).val("Update");
                $j("#scd_btn_save_"+scd_currentScTaskId).attr("taskuid",currentTaskUID);                
                $j("#scd_btn_save_"+scd_currentScTaskId).show();
                
            }else{
                
                $j("#scd_btn_save_"+scd_currentScTaskId).hide();
                message_status("Readonly Access: Accessing from unknown computer, you can't modify task data");
            }
            var lockedMsg=null;
            
            if(data.lockedby!=null){
                if (data.isAuthorized && data.authorizedUser == data.lockedby) {
                     $j("#scd_btn_save_"+scd_currentScTaskId).show();               
                }else{
                    $j("#scd_btn_save_"+scd_currentScTaskId).hide();
                    // message_status("Currently Edited by "+data.lockedby);
                    lockedMsg="Currently Edited by "+data.lockedby;
                }               
            }
            
            $j("#scd_revisionList_"+scd_currentScTaskId).html("");
            $j("#scd_taskTab_"+scd_currentScTaskId).tabs({
                selected: 0,
                select: function(event, ui){
                    if (typeof ui.panel.isLoaded == 'undefined' && ui.index == 6) {
                        var scd_wiki=$j(ui.panel).find(".wiki_panel").attr("task_name");                        
                        //scd_getWiki(scd_wiki, ui.panel.id);                       
                        scd_getWiki(scd_currentScTaskId, ui.panel.id);                      
                        ui.panel.isLoaded = true;
                    }
                }
            });
            if(data.revisions!=null){
                $j('#scd_taskTab_'+scd_currentScTaskId).tabs("option","disabled", []);
                scd_listRevisions(data.revisions,"scd_revisionList_"+scd_currentScTaskId);
            }else{                              
                if ($j("#scd_taskTab_"+scd_currentScTaskId).tabs("option", "selected") == 2) {
                    $j("#scd_taskTab_"+scd_currentScTaskId).tabs("select", 0);
                }
                $j("#scd_taskTab_"+scd_currentScTaskId).tabs("option","disabled", [4]); 
            }
            
            // disable code injection for non-rscript type tasks.
            if( (data.taskdata.rscript!=null && data.taskdata.rscript!='' ) ||               
                (data.taskdata.rscript_param!=null && data.taskdata.rscript_param!='')
               ){
                
                     var prfid="scd_codeinject_preview_"+scd_currentScTaskId;
                     var ccode=(data.taskdata.rscript_param!=null?data.taskdata.rscript_param+"\n":"")+(data.taskdata.rscript!=null ?data.taskdata.rscript:"");
                     $j("#"+prfid).val(ccode);
                       
                   
                    if(CODE_EDITOR==CODE_EDITOR_DEFAULT){                    
                       var tm = function(){                   
                           editAreaLoader.init({
                                id: prfid,
                                syntax: "r",
                                start_highlight: true,
                                toolbar: "|",
                                allow_toggle: false,
                                font_size: 8,                               
                                font_family: "verdana, monospace",
                                // allow_resize: "both",
                                plugins: "reditor_task",                                
                                is_editable:false
                            });
                        }                      
                        $j("#scd_taskTab_"+scd_currentScTaskId+" div.scd_code_preview").show();
                        setTimeout(tm,500);
                        
                   }else if(CODE_EDITOR==CODE_ACE_EDITOR){

                        var ttl=data["taskdata"].id+": "+data["taskdata"].name+"[Combined Preview]";
                        $j("#scd_codeinject_preview_hbtn_"+scd_currentScTaskId).remove(); // no
                                                                                            // highlighter
                                                                                            // button
                        var tm = function(prfid,ccode,ttl){
                            var dv1=$j("#"+prfid).parent();
                            $j("#"+prfid).remove();                 
                            $j(dv1).append("<div class='icon_fullscreen'><a href='#' fid='"+prfid+"' screentitle='"+ttl+"' onclick='scd_acefull(this); return false;'><img src='images/expand_fullscreen.png'></a></div><div class='ace_editor' id='"+prfid+"'></div>");
                            $j(dv1).show();                      
                            ACE_EDITORS[prfid]= ace.edit(prfid);
                            ACE_EDITORS[prfid].getSession().setValue(ccode);
                            var is_readonly=true;
                            setACE_Default(ACE_EDITORS[prfid],is_readonly);                     
                        }
                        setTimeout(tm,500,prfid,ccode,ttl);
                        
                   }
                       
                       
                       
                       
                       
                       
                       
                       
                       
                       
                       
                       
                       
                       
                
                        
             }else{
                                
                var dsbld = $j('#scd_taskTab_'+scd_currentScTaskId).tabs( "option", "disabled" );
                dsbld[dsbld.length]=1; // disabling code inject                             
                $j("#scd_taskTab_"+scd_currentScTaskId).tabs("option","disabled", dsbld);
                
             
             }  
        
            
            if(data.tag_follow!=null ){
                data.tag_follow.authorizedUser=data.authorizedUser; 
                to_showTags4Item("#scd_taskTab_"+scd_currentScTaskId,data.tag_follow,scd_currentScTaskId,data.access,"Theme / Alert / Notification");
                
            }
            
            // to make the owner appears on top.
            if(data.taskdata.owner_tag_id!=null && data.taskdata.owner_tag_id>0){
                var otag=$j("#scd_taskTab_"+scd_currentScTaskId+" .task_tag_owner li[tag_id='"+data.taskdata.owner_tag_id+"']");
                if(otag!=null && otag.length>0 && otag.siblings().length>0){
                    otag.insertBefore(otag.siblings(':eq(0)'));
                }               
            }
            // myTriggerTable.update(data.triggerdata);
            myTriggerTableObj[scd_currentScTaskId]=getTriggerTable("trigger_table_"+scd_currentScTaskId);
            myTriggerTableObj[scd_currentScTaskId].update(data.triggerdata);
            
            scd_timeZoneInit('scd_tzInput_'+scd_currentScTaskId,'scd_tzContainer_'+scd_currentScTaskId);
            $j("#scd_tzInput_"+scd_currentScTaskId).val(data.taskdata.timezone!=null?data.taskdata.timezone:"");
            
            if (data.taskdata.pluggindata != null && data.taskdata.pluggindata_data != null && data.taskdata.pluggindata_field != null) {
                var efunc = function(){
                };
                eval("efunc=" + data.taskdata.pluggindata.jsfetch);             
                var fid = "flxi_"+scd_currentScTaskId+'_' + data.taskdata.pluggindata_field.shortname + "_" + data.taskdata.pluggindata_field.id;               
                
                efunc.call(this, fid, data.taskdata.pluggindata_data);
            }
            
            $j("#scd_tabs-"+scd_currentScTaskId).attr("script_access",data.access);                        
            if(data.access!=null && ( data.access=='R' || data.access=='RX') ){
                $j("#scd_btn_save_"+scd_currentScTaskId).hide();
                lockedMsg="<img src='images/lock.png'>";                
                $j("#scd_taskTab_"+scd_currentScTaskId).find(".ui-tabs-panel input:not(.scd_followBtn),.ui-tabs-panel select,.ui-tabs-panel text").attr("disabled","disabled"); 
                $j("#scd_taskTab_"+scd_currentScTaskId).find(".ui-tabs-panel input:not(.scd_followBtn),.ui-tabs-panel select,.ui-tabs-panel text").css("background-color","#E0E0E0");
            }
            
            if(data.taskdata.deleted!=null &&  data.taskdata.deleted==1){
                $j("#scd_btn_save_"+scd_currentScTaskId).hide();
                lockedMsg="Deleted Item";               
                $j("#scd_taskTab_"+scd_currentScTaskId).find(".ui-tabs-panel input:not(.scd_followBtn),.ui-tabs-panel select,.ui-tabs-panel text").attr("disabled","disabled"); 
                $j("#scd_taskTab_"+scd_currentScTaskId).find(".ui-tabs-panel input:not(.scd_followBtn),.ui-tabs-panel select,.ui-tabs-panel text").css("background-color","#E0E0E0");
                
            }
            if(lockedMsg!=null){
                $j("#scd_lockedBy_"+scd_currentScTaskId).html(lockedMsg);
                $j("#scd_lockedBy_"+scd_currentScTaskId).addClass("showOn");
            }
            
            


            
        }else{
            alert("Task data not exist");
        }
        
        if(scd_editorTab.length==1){
                  scd_highLightFunc(scd_currentScTaskId);
        }
        
        progress_message(null);
    }

    progress_message("Please wait...");
    
    if ($j("#scd_tabs-" + scd_currentScTaskId).length > 0) {
        processTb = false;       
        var index=$j('#scd_taskpane_holder a[href="#scd_tabs-'+ scd_currentScTaskId+'"]').parent().index()
        scd_editorTab.tabs('select', index);   
        SchedulerMgmt.getScheduledItem(taskid, function(data){
            respBack(data,true);
        });     
    }else {
        progress_message("Please wait...");
        SchedulerMgmt.getScheduledItem(taskid, respBack);
    }
    // SchedulerMgmt.getScheduledItem(taskid,respBack);
}

function scd_getWiki(task_id,panel_id,exeCallBk){
    var respBack = function(rdata){
        progress_message();
        if (rdata != null) {
            
            $j("#"+panel_id+" div.wiki_panel").html('<div class="function_wiki"><div class="wiki_tb"><input  task_id="'+task_id+'" panel_id="'+panel_id+'" type="button" value="Edit This Wiki" onclick="wiki_inlineEditorObj(this)"></div>'+rdata+'</div>');
            
            if($j("#"+panel_id+" div.wiki_panel .function_wiki").text().trim().indexOf("There is currently no text in this page, you can search for this page title in other pages or edit this page")>=0){
            //no page found for this function.
                if($j("#"+panel_id+" div.wiki_panel .function_wiki a.external.text").html()=='edit this page'){
                    var link=$j("#"+panel_id+" div.wiki_panel .function_wiki a.external.text").attr("href");
                    //alert(link);
                    $j("#"+panel_id+" div.wiki_panel").html('<div class="function_wiki"><center><div class="create_wiki">No documentation found for this function <a href="#">Add Wiki</a></div></center></div>');
                    $j("#"+panel_id+" div.wiki_panel .function_wiki .create_wiki a").bind('click',function(){
                        var wind=window.open( link, "wiki", "width=900,height=800;location=0,status=0,scrollbars=1");
                        wind.focus(); 
                        return false;                       
                    });
                } 
                
            }else{                   
                $j("#"+panel_id+" div.wiki_panel a[href^='http']").attr('target','_blank');         
                $j("#"+panel_id+" div.wiki_panel a[href^='http']").bind('click',function(){
                        var wind=window.open( $j(this).attr('href'), "foo", "width=900,height=800;location=0,status=0,scrollbars=1");
                        wind.focus(); 
                        //return false;
                        //wiki_inlineEditor($j(this).attr('href'),task_id,panel_id);
                        
                        return false;
                }); 
                
                //replacing images...               
                $j("#"+panel_id+" div.wiki_panel img[src^='/']").each(function(){
                    $j(this).attr("src","https://wiki.4ecap.com"+$j(this).attr("src"));
                });
            }
            
        }else{
            $j("#"+panel_id+" div.wiki_panel").append('<div class="function_wiki"><h3>No Information found on Wiki about this function</h3></div>');
        }
        if(exeCallBk!=null){
            exeCallBk.call(this);
        }
    }
    progress_message("please wait....");
    SchedulerMgmt.getWikiHelp(task_id,respBack);
    
    
}

function wiki_inlineEditorObj(thisobj){
    var task_id=$j(thisobj).attr("task_id")
    var panel_id=$j(thisobj).attr("panel_id");
    wiki_inlineEditor(task_id,panel_id);
}

function wiki_inlineEditor(task_id,panel_id){
    var url1="https://wiki.4ecap.com/4ecapwiki/index.php?title=SchedulerScript:"+task_id+"&action=edit";
    $j("#"+panel_id+" div.wiki_panel").html('<div class="function_wiki_editor"><div class="wiki_tb"><input task_id="'+task_id+'" panel_id="'+panel_id+'" type="button" value="Finished Editing" onclick="fn_closeWikiEditor(this)"></div></div>');
    $j("#"+panel_id+" div.wiki_panel").append('<div class="wiki_editor"><iframe width="100%" height="94%" src="'+url1+'" scrolling="yes"></iframe></div>'); 
}

function fn_closeWikiEditor(thisobj){
    var task_id=$j(thisobj).attr("task_id")
    var panel_id=$j(thisobj).attr("panel_id");
    scd_getWiki(task_id,panel_id);
}

function scd_highLightFunc(func_id){    
    $j(".scd_tabopen").removeClass("scd_tabopen");
    $j(".scd_tabactive").removeClass("scd_tabactive");
    
    $j("#scd_taskpane_holder div[id^='scd_tabs-']").each(function(idx,elem){
        var this_id=$j(elem).attr("id");
        var fid=this_id.replace("scd_tabs-","");
        
        $j("#scheduler_id_" + fid).addClass("scd_tabopen");     
        

        if (!$j(elem).hasClass("ui-tabs-hide")) {
            func_id=fid;
            $j("#scheduler_id_" + func_id).addClass("scd_tabactive");           
            
            // opens all closed tree nodes
            $j("#scheduler_id_" + func_id).parents().each(function(idx,el){
                  $j(el).children("div.hitarea.expandable-hitarea").trigger('click');
            });
            setTimeout(function(){
                try{
                $j(treeSelector).scrollTo("#scheduler_id_" + func_id,700, {offset: {top:-410}} );
                }catch(ex){console.log('error while scrollTo()');}
                
            },300);
        }   
    });
}

function scd_listRevisions(rdata,divid){
    for(rev in rdata){
        if (rdata[rev].scheduler_id != null ) {
            li = "<li>" + rdata[rev].author + " &nbsp;<span class='date'>(" + rdata[rev].date + ")</span>";
            li+="<div class='message' style='width:380px'>"+rdata[rev].message+"</div>";
            if(rdata[rev].revision != null){
                li += "<div style='float:right'>";
                li += "Compare:";
                li += "<a style='margin:0px 1px 0px 3px' href='#' onclick='scd_showRev(this,false); return false;' scheduler_id='" + rdata[rev].scheduler_id + "' revision='" + rdata[rev].revision + "' ><img src='images/popup.gif' border='0'></a>";
                li += "<a style='margin:0px 15px 0px 1px' href='push_svndiff.jsp?scheduler_id=" + rdata[rev].scheduler_id + "&revision=" + rdata[rev].revision + "'><img src='images/external_link.gif' border='0'></a>";
                li += "Script:<a style='margin:0px 10px 0px 1px' href='#' onclick='scd_showRev(this,true); return false;' scheduler_id='" + rdata[rev].scheduler_id + "' revision='" + rdata[rev].revision + "' ><img src='images/popup.gif' border='0'></a>";
                li += "</div>";
            }
            li += "</li>";
            $j("#"+divid).append(li);
        }
    }
}

function scd_showRev(tobj,flag) {
    sc_id=$j(tobj).attr("scheduler_id");
    rev=$j(tobj).attr("revision");
    
    var respBack=function(data){
        progress_message(null);
        if(data!=null){
            // alert(data);
            $j("#dialog_box_svnScriptHD").html("Revision:"+rev);
            $j("#dialog_box_svnScript").show();
            $j("#dialog_box_svnScript").dialog("open");
            $j("#dialog_box_svnScript").dialog({close: function(event, ui) {}, modal:true,width: 730 });
            
            try{
                $j("#dialog_box_svnScriptBD").html(data);
            }catch(ex){
                // some cases there will be error as it conflicts with html
                // codes.
                $j("#dialog_box_svnScriptBD").text(data);   
            }
            if (flag) {
                $j("#dialog_box_svnScriptBD").addClass("sh_slang")
                sh_highlightDocument(); 
            }else{
                $j("#dialog_box_svnScriptBD").removeClass("sh_slang")
            }   
        }
    }
    progress_message("Please wait...");
    
    SchedulerMgmt.getScriptRev(sc_id,rev,flag,respBack);
}

var currentTaskUID=null;

function scd_getFormFields(taskname,scd_formid,sc_id) {
    currentTaskUID=taskname;
    var respback=function(respdata){
        progress_message(null);
        if(respdata!=null){
            
            var data=new Object();
            data.fields=respdata;
            scd_flexiForm(data,scd_formid,null,sc_id);
        }
    } 
    progress_message("Please wait...");
    SchedulerMgmt.getTaskFields(taskname,respback);
}

function scd_flexiForm(data,flexipanediv,formdata,sc_id){
        if(data!=null && data.fields){  
            progress_message(null);
            var fields=data.fields; 
            
            if(false) { 
                var tbl=document.createElement('table');
                tbl.setAttribute('width','100%');           
                tbl.setAttribute('cellspacing','0');
                tbl.setAttribute('cellpadding','1');
     
                var wrap=document.createElement('form');                                    
                
                wrap.appendChild(tbl);
                
                var children=$(flexipanediv).childElements();            
                for (intb = 0; intb < children.length; intb++) {
                    children[intb].remove();
                }
     
                $(flexipanediv).show();
                $(flexipanediv).appendChild(wrap);          
            }
            
            // remove everything
            // for(iab=0;iab<fields.length;iab++){
            // var
            // plmt=(fields[iab].placementform!=null)?fields[iab].placementform:"general";
            // var tbl=$j("div#scd_taskTab_"+sc_id+"
            // #scd_form_"+plmt+"_"+sc_id).find("table.scd_form_table")[0];
            // for(iac=0;iac<tbl.rows.length){
            // $j(tbl.rows[i]).remove();
            // }
            // }
            var tbl=$j("div#scd_taskTab_"+sc_id).find("table.scd_form_table tr").remove();
            
            
            for(iab=0;iab<fields.length;iab++){
                
                var plmt=(fields[iab].placementform!=null)?fields[iab].placementform:"general";
                                
                var tbl=$j("div#scd_taskTab_"+sc_id+" #scd_form_"+plmt+"_"+sc_id).find("table.scd_form_table")[0];
                                
                if (fields[iab].fieldtype != 'hidden' && tbl!=null) {
                    var trow = tbl.insertRow(tbl.rows.length);
                    
                    var c0 = trow.insertCell(0);
                    c0.vAlign = "top";
                    c0.style.borderBottom="1px dotted #DBDBDB";
                    c0.style.paddingTop="3px";
                    
                    if(fields[iab].fieldlabel!=null && fields[iab].fieldlabel!=''){ 
                        var dv=document.createElement("div");                       
                        dv.appendChild(document.createTextNode(fields[iab].fieldlabel + ":"));
                        $j(dv).addClass("pluginLabel");
                        c0.appendChild(dv);                  
                    }
                    
                    var fval = null;
                    if (formdata != null) {
                        if (formdata[fields[iab].shortname] != null) {
                            fval = formdata[fields[iab].shortname];
                        }
                    }
                    
                    // scd_produceField(c1, fields[iab], fval);
                    scd_produceField(c0, fields[iab], fval,(data.taskdata!=null?data.taskdata.id:0),data);
                    
                    if(fields[iab]!=null && fields[iab].fineprint!=null){
                        var spn=document.createElement('div');
                        spn.style.fontSize="0.7em";
                        spn.style.color="#FF0000";                      
                        spn.style.float="right";
                        spn.style.width="350px";
                        spn.style.display="inline";
                        spn.appendChild(document.createTextNode(fields[iab].fineprint));
                        // c1.appendChild(spn);
                        c0.appendChild(spn);
                    }
                }
                                
            }
          
        }
}

function scd_produceField(scell, field,fieldval,scd_id,data){
    
        var fid="flxi_"+scd_id+"_"+field.shortname+"_"+ field.id;
        if (field.fieldtype == 'textbox') {
            var inpt = document.createElement('input');
            inpt.setAttribute('type', 'textbox');           
            inpt.setAttribute('id',fid);
            if (fieldval != null) {
                inpt.setAttribute('value', fieldval);
                inpt.value=fieldval;
            }
            scell.appendChild(inpt);
            inpt.setAttribute("fieldname",field.shortname);
            $j(inpt).addClass("flex_field_"+field.shortname);
            
            
        }else if(field.fieldtype == 'textarea'){
            var inpt = document.createElement('textarea');
            // inpt.setAttribute('type', 'checkbox');
            inpt.setAttribute('id',fid);
            inpt.setAttribute('wrap','off');
            inpt.rows="25";
            inpt.cols="80";
            inpt.setAttribute("fieldname",field.shortname);
            $j(inpt).addClass("flex_field_"+field.shortname);
            if (fieldval != null) {
                inpt.value=fieldval;
            }
            scell.appendChild(inpt);
        }else if(field.fieldtype.indexOf('rscript')==0 
                 || field.fieldtype.indexOf('rscript_param')==0 
                 || field.fieldtype.indexOf('rhinoscript')==0
               ){
            var dv=document.createElement('div');
            dv.setAttribute('id',fid+"dv");
            

            var dv1=document.createElement('div');
            dv1.setAttribute('id',fid+"ext");
            $j(dv1).addClass("editorBox");
            
            var inpt = document.createElement('textarea');                      
            inpt.setAttribute('id',fid);
            inpt.style.display="none";
            inpt.style.width="100%";
            inpt.style.height="100%";
            inpt.style.border="2px solid grey";
            
            inpt.setAttribute("fieldname",field.shortname);
            $j(inpt).addClass("flex_field_"+field.shortname);
            if (fieldval != null) {
                inpt.value=fieldval;
            }
            dv1.appendChild(inpt);
            scell.appendChild(dv);
            scell.appendChild(dv1);
            if(field.fieldtype.indexOf('rscript_param')==0 ){
                scell.style.height="150px";
            }else{
                scell.style.height="600px";
            }
           if(field.fieldtype.indexOf('rscript')==0){
           }
           if (field.fieldtype.indexOf('rhinoscript') == 0 || field.fieldtype.indexOf('rscript')==0 || field.fieldtype.indexOf('rscript_param')==0) {
               if(CODE_EDITOR==CODE_EDITOR_DEFAULT){
                    try{
                        editAreaLoader.delete_instance(fid);
                    }catch(ex){}    
                    var tm = function(){
                        editAreaLoader.init({
                            id: fid,
                            syntax: "r",
                            start_highlight: true,
                            toolbar: "|",
                            allow_toggle: false,
                            font_size: 8,
                            font_family: "verdana, monospace",
                            allow_resize: "both",
                            plugins: "reditor_task",                                
                            is_editable:(data.access=='R' || data.access=='RX') ?false: true
                        });             
                    }
                    
                    setTimeout(tm,500);
               }else if(CODE_EDITOR==CODE_ACE_EDITOR){
                  
                    $j(inpt).remove();                   
                    var fieldval1=fieldval==null?"":fieldval
                    var ttl="[Unnamed task]";
                    if(data["taskdata"]!=null){     
                        ttl=data["taskdata"].id+": "+data["taskdata"].name
                    } 
                    $j(dv1).append("<div class='icon_fullscreen'><a href='#' fid='"+fid+"' screentitle='"+ttl+"' onclick='scd_acefull(this); return false;'><img src='images/expand_fullscreen.png'></a></div><div class='ace_editor' fieldname='"+field.shortname+"' style='display:none' id='"+fid+"'></div>");
                    var tm = function(fid,fieldval1){
                        $j("#"+fid).show();                      
                        ACE_EDITORS[fid]= ace.edit(fid);
                        ACE_EDITORS[fid].getSession().setValue(fieldval1);
                        var is_readonly=(data.access=='R' || data.access=='RX') ?true: false
                        setACE_Default(ACE_EDITORS[fid],is_readonly);                       
                    }
                    setTimeout(tm,500,fid,fieldval1);
                    
               }
            }
            
        }else if(field.fieldtype == 'bloomberg_pluggin'){
            var dv = document.createElement('div');                     
            dv.setAttribute('id',fid);
            dv.setAttribute("fieldname",field.shortname);
            $j(dv).addClass("flex_field_"+field.shortname);
            scell.appendChild(dv);
            if(field.pluggindata!=null){
                var efunc=function(){};
                eval("efunc="+field.pluggindata.jsnew);
                efunc.call(this,fid);
            } 
        }else if(field.fieldtype == 'checkbox'){
            var inpt = document.createElement('input');
            inpt.setAttribute('type', 'checkbox');          
            inpt.setAttribute('id',fid);
            inpt.setAttribute("fieldname",field.shortname);
            $j(inpt).addClass("flex_field_"+field.shortname);
            scell.appendChild(inpt);
            if(fieldval=="1"){
                inpt.checked=true;          
            }
            if (fieldval != null) {
                inpt.setAttribute('value', fieldval);
            }
        }else if(field.fieldtype == 'radio' && field.fieldoptions!=null){
            
            var opts=field.fieldoptions.split("|")
            
            
            for (ic = 0; ic < opts.length; ic++) {
            
                var w1=document.createElement('div');       
                scell.appendChild(w1);
                        
                var inpt =null;     
                try{
                    // works only on IE
                    var inpt = document.createElement('<input name="'+fid+'">');                    
                }catch(ex){
                    var inpt = document.createElement('input');
                    inpt.setAttribute('name','radiobtn1');
                }
                inpt.setAttribute('type', 'radio');
                inpt.setAttribute('id',fid+"_"+opts[ic]);
                inpt.setAttribute("fieldname",field.shortname);
                $j(inpt).addClass("flex_field_"+field.shortname);
                inpt.setAttribute("value",opts[ic]);
                 
                w1.appendChild(inpt)
                if (fieldval == opts[ic]) {
                    inpt.checked = true;
                    inpt.selected=true;
                    inpt.setAttribute("selected","selected");
                }
                if (fieldval != null) {
                    inpt.setAttribute('value', fieldval);
                }
                w1.appendChild(document.createTextNode(opts[ic]));
            }
         }else if(field.fieldtype == 'dropdown' && field.fieldoptions!=null){
            
            var opts=field.fieldoptions.split("|")
            
            var selobj=document.createElement('select');
            selobj.setAttribute('id',fid);          
            selobj.setAttribute('name', fid);           
            selobj.setAttribute("fieldname",field.shortname);
            $j(selobj).addClass("flex_field_"+field.shortname);
            selobj.options.length=opts.length+1;
            
              
            var inpt = selobj.options[0];
            inpt.value="";
            if(field.dropdowninitial!=null){
                inpt.text=field.dropdowninitial;
            }else{                   
                 inpt.text="---Select---";
            }
            
            for (ic = 0; ic < opts.length; ic++) {
                var inpt = selobj.options[ic+1];// document.createElement('option');
                
                
                inpt.value=opts[ic];
                inpt.text=opts[ic];
                
                if (opts[ic] != null && opts[ic] != '' && opts[ic].indexOf("~") >= 0) {
                    var sobj=opts[ic].split("~");
                    if(sobj.length>=2){
                        inpt.value=sobj[0];
                        inpt.text=sobj[1];
                    }
                } 
                                
                if (fieldval == inpt.value) {
                    inpt.selected=true;             
                }
            }
            scell.appendChild(selobj);
        }
}

function scd_acefull(tobj) {
    
    var fid=$j(tobj).attr("fid");   
    var title=$j(tobj).attr("screentitle");
    var dom = require("ace/lib/dom");
    dom.toggleCssClass(document.body, "fullScreen")
    dom.toggleCssClass(ACE_EDITORS[fid].container, "fullScreen-editor")
    ACE_EDITORS[fid].resize();
    if($j("div#ace_fullscreenclose").length>0){
        $j("div#ace_fullscreenclose").remove();
    }else{
        $j("body").append("<div id='ace_fullscreenclose'  class='ace_fullscreenclose'><span></span><a href='#' fid='"+fid+"' onclick='scd_acefull(this);return false;'><img src='images/collapse_fullscreen.png'></a><small>[Esc]</small></div>");
        if(title!=null && title!=''){
            $j("div#ace_fullscreenclose span").text(title);
        }
    }
    ACE_EDITORS[fid].focus();
}

function setACE_Default(editor,ro_flag) {
    editor.setTheme("ace/theme/chrome");
    editor.getSession().setMode("ace/mode/r");                  
    editor.setShowPrintMargin(false);
    editor.setFontSize(".9em"); 
    if(ro_flag!=null){
        editor.setReadOnly(ro_flag);
    }
    editor.session.setFoldStyle("markbegin");
    editor.setShowFoldWidgets(true);
    
    editor.commands.addCommand({
        name: 'fullscreen',
        bindKey: {win: 'Esc',  mac: 'Esc'},
        exec: function(editor) {
            // alert('escape')
            if($j(editor.container).hasClass('fullScreen-editor')){
                var anc="<a fid='"+$j(editor.container).attr("id")+"' screentitle=''></a>";
                scd_acefull($j(anc));
            }
        },
        readOnly: true // false if this command should not apply in readOnly
                        // mode
    });
}

var scd_currentScTaskId=0;

function scd_gatherInputs(tobj){
    if(tobj==null){
        alert("tobj is required");
        return;
    }
    
    var scd_id=$j(tobj).attr("task_id");    
    var c_taskuid=$j(tobj).attr("taskuid");
    
    var scriptName = "";
    var buttonName = $j(tobj).val().toString();
    
    if ( buttonName == "Update") {
    	scriptName    = $j("#flxi_"+ scd_id +"_name_0").val();
    } else if (buttonName == "Save") {
    	scriptName = $j("#flxi_0_name_0").val();
    }
    
    if((scriptName == null) || (scriptName == "" )) {
    	message_status("Script name can't be empty");
    	return;
    }
    
    var collectFormValues=function(fm1,obj){
        var elem=fm1.elements;
        if(obj==null) var obj=new Object();
        for(iab=0;iab<elem.length;iab++){

            if (elem[iab].getAttribute("fieldname") != null) {
                
                if(elem[iab].type=='checkbox'){
                    if(elem[iab].checked){
                        obj[elem[iab].getAttribute("fieldname")]="1";
                    }else{
                        obj[elem[iab].getAttribute("fieldname")]="0";
                    }
                }else if(elem[iab].type=='radio'){               
                    if(elem[iab].checked){
                        obj[elem[iab].getAttribute("fieldname")]= elem[iab].value;                      
                    }else{
                    }
                }else if(elem[iab].type=='textarea' && elem[iab].getAttribute("fieldname")!=null &&
                  (elem[iab].getAttribute("fieldname").indexOf("rhinoscript")>=0  || elem[iab].getAttribute("fieldname").indexOf("rscript")>=0 || elem[iab].getAttribute("fieldname").indexOf("rscript_param")>=0) )
                 {                  
                        obj[elem[iab].getAttribute("fieldname")]=editAreaLoader.getValue(elem[iab].getAttribute("id")); 
                }else{
                    obj[elem[iab].getAttribute("fieldname")] = elem[iab].value;
                }
            }
        }
        
        if(CODE_EDITOR==CODE_ACE_EDITOR){
           $j(fm1).find("div.ace_editor").each(function(idx,elem){
               var eid=$j(elem).attr("id")
               if(ACE_EDITORS[eid]!=null){
                   obj[elem.getAttribute("fieldname")]=ACE_EDITORS[eid].getSession().getValue();
               }else{
                obj[elem.getAttribute("fieldname")]=editAreaLoader.getValue(eid);   
               }
           });
       }
       return obj;
    }
    
    var obj=new Object();       
    obj.timezone=$("scd_tzInput_"+scd_id).value;
    var fms=$j("div#scd_taskTab_"+scd_id+" form.scd_form").each(function(idx,fm){
        collectFormValues(fm,obj);
    });
    
    
    
    
    // if (fm.length > 0) {
    // var obj=collectFormValues(fm[0]);
    // obj.timezone=$("scd_tzInput_"+scd_id).value;
    // }
    
    var tagids=new Array();
    // $j("#scd_taskTab_"+scd_id+" .task_tag_dialog_ul
    // input:checked:not([disabled])").each(function(idx,el){
    $j("#scd_taskTab_"+scd_id+" .tags_panel .task_tag_owner li[tag_id]").each(function(idx,el){
        tagids[tagids.length]=$j(el).attr("tag_id");
    });
    
    obj.owner_tag_id="0";
    if($j("#scd_taskTab_"+scd_id+" .tags_panel .task_tag_owner li[tag_id]:eq(0)").length>0){
        obj.owner_tag_id=$j("#scd_taskTab_"+scd_id+" .tags_panel .task_tag_owner li[tag_id]:eq(0)").attr("tag_id");
    }
 
    
    var followids=new Array();
    // $j("#scd_taskTab_"+scd_id+" .task_tag_dialog_ul
    // input:checked:not([disabled])").each(function(idx,el){
    $j("#scd_taskTab_"+scd_id+" .tags_panel .task_tag_follower li[tag_id]").each(function(idx,el){
        followids[followids.length]=$j(el).attr("tag_id");
    });
    
    
    var respBack=function(data){
        // alert(data);
        progress_message(null);     
        if(data!=null){
            var svdata=null;
            if(data.plugginfield!=null &&  data.plugginfield.pluggindata!=null){
                
                var efunc=function(){};
                eval("efunc="+data.plugginfield.pluggindata.jscreate);
                var fid="flxi_"+scd_id+'_'+data.plugginfield.shortname+"_"+ data.plugginfield.id;
                
                svdata=efunc.call(this,fid);
                
                svdata.name=obj.name;
                 
            } 
            
            
            // alert(DWRUtil.toDescriptiveString(obj,2));
            
            // if(confirm(data.message)){
                
                
                
                     $j( "#dialog_commit" ).dialog({
                        resizable: false,            
                        width:700,
                        modal: true,        
                        dialogClass: 'comment_dialog',  
                        autoOpen: false,    
                        // ,
                        open: function(event, ui) {
                            console.dir(ui);                 
                            $j("#dialog_commit ~.ui-dialog-buttonpane .ui-button:last-child").attr("disabled","disabled");
                            $j("#dialog_commit_msg").unbind("keyup");
                            $j("#dialog_commit_msg").bind("keyup",function(){
                                console.log("key pressed.");
                                if($j.trim($j("#dialog_commit_msg").val()).length>5){
                                    $j("#dialog_commit ~.ui-dialog-buttonpane .ui-button:last-child").removeAttr("disabled");
                                }else{
                                    $j("#dialog_commit ~.ui-dialog-buttonpane .ui-button:last-child").attr("disabled","disabled");
                                }
                            }); 
                        }
                    });
                    var btn={
                            "Cancel": function() {
                            $j( this ).dialog( "close" );
                            },                  
                            "Save": function() {
                                progress_message("Saving..");                   
                                // $j( "#dialog_commit").dialog( "close" );
                                $j( this ).dialog( "close" );
                                // $j("#dialog_commit_msg").val()//
                                
                                // progress_message("Saving...");
                                SchedulerMgmt.updateScheduleTask(parseInt(scd_id),obj,c_taskuid,true,svdata,myTriggerTableObj[scd_id].getDBFormattedData(),$j("#dialog_commit_msg").val(),tagids,followids,
                                    function(data){
                                        // remove new task tab.
                                        if(parseInt(scd_id)==0){
                                            var index = $j( "li", scd_editorTab ).index( $j( '#scd_taskpane_holder a[href="#scd_tabs-'+scd_id+'"]' ).parent() );
                                            scd_editorTab.tabs( "remove", index );  
                                        }
                                        progress_message(null);                      
                                        if(data.scheduler_id!=null){
                                            scd_loadTask(data.scheduler_id);                                
                                        }
                                        scd_treeListGen(data);
                                        // SchedulerMgmt.getQueueLogs(null,scd_genQLogs)
                                        scd_refreshQueueOnClick(); // to reload
                                                                    // tabs when
                                                                    // it
                                                                    // clicked
                                                                    // instead
                                                                    // of
                                                                    // loading
                                                                    // everything
                                                                    // at first
                                                                    // place.
                                    }
                                );
                            }
                    };
                    $j('#dialog_commit').dialog("option","buttons",btn);
                    $j('#dialog_commit').dialog("open");
                    $j('#dialog_commit_msg').focus();
 
                
            // }
        }
    }
    

    if(tagids.length<1){        
        
        $j("#scd_taskTab_"+scd_id).tabs( "select" , 5);     
        alert("Failed: Minimum 1 tag required. Please select tag from tag tab");
        return ;
    }else{  
        
        //if(followids<1 && !confirm("Changes on this task in feature will not be notified anyone! Would you still wish to continue without any 'Notification of Changes' tags ?")){
        //  return ;
        //}
        
        progress_message("Please wait validating...");  
        SchedulerMgmt.updateScheduleTask(parseInt(scd_id), obj, c_taskuid, false, null, myTriggerTableObj[scd_id].getDBFormattedData(),"",tagids,followids,respBack);

    }
    
    
    
    // alert(DWRUtil.toDescriptiveString(obj,2));
}

function scd_scheduleCBoxVldte(ckbox,value,setflag) {
        if(setflag!=null && typeof setflag=='boolean'){
            ckbox.checked=setflag;
        }
        if (ckbox.getAttribute('bodylinkid') != null) {
            if (ckbox.checked) {
                $(ckbox.getAttribute('bodylinkid')).show();
                var inps=$(ckbox.getAttribute('bodylinkid')).select("input");
                for (iac = 0; iac < inps.length; iac++) {
                    if (value != null) {inps[iac].value = value;}
                }
            }else {             
                                
                var inps=$(ckbox.getAttribute('bodylinkid')).select("input");
                for(iac=0;iac<inps.length;iac++){
                    if (value != null) {
                        inps[iac].value=value;
                    }else {
                        // firefox compliant
                        // ie compliant
                         
                        if (inps[iac].defaultvalue != null) {                               
                            inps[iac].value = inps[iac].defaultvalue;
                        } else {
                            inps[iac].value = "";
                        }
                        
                    }       
                }
                $(ckbox.getAttribute('bodylinkid')).hide();
            }
        }   
    } 

var scd_timeZones=["Africa/Abidjan", "Africa/Accra", "Africa/Addis_Ababa", "Africa/Algiers", "Africa/Asmara", "Africa/Bamako", "Africa/Bangui", "Africa/Banjul", "Africa/Bissau", "Africa/Blantyre", "Africa/Brazzaville", "Africa/Bujumbura", "Africa/Cairo", "Africa/Casablanca", "Africa/Ceuta", "Africa/Conakry", "Africa/Dakar", "Africa/Dar_es_Salaam", "Africa/Djibouti", "Africa/Douala", "Africa/El_Aaiun", "Africa/Freetown", "Africa/Gaborone", "Africa/Harare", "Africa/Johannesburg", "Africa/Kampala", "Africa/Khartoum",
 "Africa/Kigali", "Africa/Kinshasa", "Africa/Lagos", "Africa/Libreville", "Africa/Lome", "Africa/Luanda", "Africa/Lubumbashi", "Africa/Lusaka", "Africa/Malabo", "Africa/Maputo", "Africa/Maseru", "Africa/Mbabane", "Africa/Mogadishu", "Africa/Monrovia", "Africa/Nairobi", "Africa/Ndjamena", "Africa/Niamey", "Africa/Nouakchott", "Africa/Ouagadougou", "Africa/Porto-Novo", "Africa/Sao_Tome", "Africa/Tripoli", "Africa/Tunis", "Africa/Windhoek", "America/Adak", "America/Anchorage", "America/Anguilla", "America/Antigua", 
 "America/Araguaina", "America/Argentina/Buenos_Aires", "America/Argentina/Catamarca", "America/Argentina/Cordoba", "America/Argentina/Jujuy", "America/Argentina/La_Rioja", "America/Argentina/Mendoza", "America/Argentina/Rio_Gallegos", "America/Argentina/Salta", "America/Argentina/San_Juan", "America/Argentina/San_Luis", "America/Argentina/Tucuman", "America/Argentina/Ushuaia", "America/Aruba", "America/Asuncion", "America/Atikokan", "America/Bahia", "America/Barbados", "America/Belem", "America/Belize", 
 "America/Blanc-Sablon", "America/Boa_Vista", "America/Bogota", "America/Boise", "America/Cambridge_Bay", "America/Campo_Grande", "America/Cancun", "America/Caracas", "America/Cayenne", "America/Cayman", "America/Chicago", "America/Chihuahua", "America/Costa_Rica", "America/Cuiaba", "America/Curacao", "America/Danmarkshavn", "America/Dawson", "America/Dawson_Creek", "America/Denver", "America/Detroit", "America/Dominica", "America/Edmonton", "America/Eirunepe", "America/El_Salvador", "America/Fortaleza", 
 "America/Glace_Bay", "America/Godthab", "America/Goose_Bay", "America/Grand_Turk", "America/Grenada", "America/Guadeloupe", "America/Guatemala", "America/Guayaquil", "America/Guyana", "America/Halifax", "America/Havana", "America/Hermosillo", "America/Indiana/Indianapolis", "America/Indiana/Knox", "America/Indiana/Marengo", "America/Indiana/Petersburg", "America/Indiana/Tell_City", "America/Indiana/Vevay", "America/Indiana/Vincennes", "America/Indiana/Winamac", "America/Inuvik", "America/Iqaluit", 
 "America/Jamaica", "America/Juneau", "America/Kentucky/Louisville", "America/Kentucky/Monticello", "America/La_Paz", "America/Lima", "America/Los_Angeles", "America/Maceio", "America/Managua", "America/Manaus", "America/Marigot", "America/Martinique", "America/Mazatlan", "America/Menominee", "America/Merida", "America/Mexico_City", "America/Miquelon", "America/Moncton", "America/Monterrey", "America/Montevideo", "America/Montreal", "America/Montserrat", "America/Nassau", "America/New_York", "America/Nipigon", 
 "America/Nome", "America/Noronha", "America/North_Dakota/Center", "America/North_Dakota/New_Salem", "America/Panama", "America/Pangnirtung", "America/Paramaribo", "America/Phoenix", "America/Port-au-Prince", "America/Port_of_Spain", "America/Porto_Velho", "America/Puerto_Rico", "America/Rainy_River", "America/Rankin_Inlet", "America/Recife", "America/Regina", "America/Resolute", "America/Rio_Branco", "America/Santarem", "America/Santiago", "America/Santo_Domingo", "America/Sao_Paulo", "America/Scoresbysund", 
 "America/Shiprock", "America/St_Barthelemy", "America/St_Johns", "America/St_Kitts", "America/St_Lucia", "America/St_Thomas", "America/St_Vincent", "America/Swift_Current", "America/Tegucigalpa", "America/Thule", "America/Thunder_Bay", "America/Tijuana", "America/Toronto", "America/Tortola", "America/Vancouver", "America/Whitehorse", "America/Winnipeg", "America/Yakutat", "America/Yellowknife", "Antarctica/Casey", "Antarctica/Davis", "Antarctica/DumontDUrville", "Antarctica/Mawson", "Antarctica/McMurdo", 
 "Antarctica/Palmer", "Antarctica/Rothera", "Antarctica/South_Pole", "Antarctica/Syowa", "Antarctica/Vostok", "Arctic/Longyearbyen", "Asia/Aden", "Asia/Almaty", "Asia/Amman", "Asia/Anadyr", "Asia/Aqtau", "Asia/Aqtobe", "Asia/Ashgabat", "Asia/Baghdad", "Asia/Bahrain", "Asia/Baku", "Asia/Bangkok", "Asia/Beirut", "Asia/Bishkek", "Asia/Brunei", "Asia/Choibalsan", "Asia/Chongqing", "Asia/Colombo", "Asia/Damascus", "Asia/Dhaka", "Asia/Dili", "Asia/Dubai", "Asia/Dushanbe", "Asia/Gaza", "Asia/Harbin", "Asia/Ho_Chi_Minh", 
 "Asia/Hong_Kong", "Asia/Hovd", "Asia/Irkutsk", "Asia/Jakarta", "Asia/Jayapura", "Asia/Jerusalem", "Asia/Kabul", "Asia/Kamchatka", "Asia/Karachi", "Asia/Kashgar", "Asia/Katmandu", "Asia/Kolkata", "Asia/Krasnoyarsk", "Asia/Kuala_Lumpur", "Asia/Kuching", "Asia/Kuwait", "Asia/Macau", "Asia/Magadan", "Asia/Makassar", "Asia/Manila", "Asia/Muscat", "Asia/Nicosia", "Asia/Novosibirsk", "Asia/Omsk", "Asia/Oral", "Asia/Phnom_Penh", "Asia/Pontianak", "Asia/Pyongyang", "Asia/Qatar", "Asia/Qyzylorda", "Asia/Rangoon", 
 "Asia/Riyadh", "Asia/Sakhalin", "Asia/Samarkand", "Asia/Seoul", "Asia/Shanghai", "Asia/Singapore", "Asia/Taipei", "Asia/Tashkent", "Asia/Tbilisi", "Asia/Tehran", "Asia/Thimphu", "Asia/Tokyo", "Asia/Ulaanbaatar", "Asia/Urumqi", "Asia/Vientiane", "Asia/Vladivostok", "Asia/Yakutsk", "Asia/Yekaterinburg", "Asia/Yerevan", "Atlantic/Azores", "Atlantic/Bermuda", "Atlantic/Canary", "Atlantic/Cape_Verde", "Atlantic/Faroe", "Atlantic/Madeira", "Atlantic/Reykjavik", "Atlantic/South_Georgia", "Atlantic/St_Helena", 
 "Atlantic/Stanley", "Australia/Adelaide", "Australia/Brisbane", "Australia/Broken_Hill", "Australia/Currie", "Australia/Darwin", "Australia/Eucla", "Australia/Hobart", "Australia/Lindeman", "Australia/Lord_Howe", "Australia/Melbourne", "Australia/Perth", "Australia/Sydney", "Europe/Amsterdam", "Europe/Andorra", "Europe/Athens", "Europe/Belgrade", "Europe/Berlin", "Europe/Bratislava", "Europe/Brussels", "Europe/Bucharest", "Europe/Budapest", "Europe/Chisinau", "Europe/Copenhagen", "Europe/Dublin", 
 "Europe/Gibraltar", "Europe/Guernsey", "Europe/Helsinki", "Europe/Isle_of_Man", "Europe/Istanbul", "Europe/Jersey", "Europe/Kaliningrad", "Europe/Kiev", "Europe/Lisbon", "Europe/Ljubljana", "Europe/London", "Europe/Luxembourg", "Europe/Madrid", "Europe/Malta", "Europe/Mariehamn", "Europe/Minsk", "Europe/Monaco", "Europe/Moscow", "Europe/Oslo", "Europe/Paris", "Europe/Podgorica", "Europe/Prague", "Europe/Riga", "Europe/Rome", "Europe/Samara", "Europe/San_Marino", "Europe/Sarajevo", "Europe/Simferopol", 
 "Europe/Skopje", "Europe/Sofia", "Europe/Stockholm", "Europe/Tallinn", "Europe/Tirane", "Europe/Uzhgorod", "Europe/Vaduz", "Europe/Vatican", "Europe/Vienna", "Europe/Vilnius", "Europe/Volgograd", "Europe/Warsaw", "Europe/Zagreb", "Europe/Zaporozhye", "Europe/Zurich", "Indian/Antananarivo", "Indian/Chagos", "Indian/Christmas", "Indian/Cocos", "Indian/Comoro", "Indian/Kerguelen", "Indian/Mahe", "Indian/Maldives", "Indian/Mauritius", "Indian/Mayotte", "Indian/Reunion", "Pacific/Apia", "Pacific/Auckland", 
 "Pacific/Chatham", "Pacific/Easter", "Pacific/Efate", "Pacific/Enderbury", "Pacific/Fakaofo", "Pacific/Fiji", "Pacific/Funafuti", "Pacific/Galapagos", "Pacific/Gambier", "Pacific/Guadalcanal", "Pacific/Guam", "Pacific/Honolulu", "Pacific/Johnston", "Pacific/Kiritimati", "Pacific/Kosrae", "Pacific/Kwajalein", "Pacific/Majuro", "Pacific/Marquesas", "Pacific/Midway", "Pacific/Nauru", "Pacific/Niue", "Pacific/Norfolk", "Pacific/Noumea", "Pacific/Pago_Pago", "Pacific/Palau", "Pacific/Pitcairn", "Pacific/Ponape", 
 "Pacific/Port_Moresby", "Pacific/Rarotonga", "Pacific/Saipan", "Pacific/Tahiti", "Pacific/Tarawa", "Pacific/Tongatapu", "Pacific/Truk", "Pacific/Wake", "Pacific/Wallis"];
 
var scd_tzlist=null;
function scd_timeZoneInit(tzInput, tzCont){
    scd_tzlist=function() {    
        var oDS = new YAHOO.util.LocalDataSource(scd_timeZones);
        // Optional to define fields for single-dimensional array
        oDS.responseSchema = {fields : ["tzlist"]};
    
        // Instantiate the AutoComplete
        // var oAC = new YAHOO.widget.AutoComplete("scd_tzInput",
        // "scd_tzContainer", oDS);
        var oAC = new YAHOO.widget.AutoComplete(tzInput, tzCont, oDS);
        oAC.prehighlightClassName = "yui-ac-prehighlight";
        oAC.useShadow = true;
        oAC.forceSelection=true;     
        oAC.queryMatchContains=true; 
        oAC.animHoriz=true;
         
        return {
            oDS: oDS,
            oAC: oAC
        };
        
    }();


    var tip=$j('#'+tzCont).parent().parent('.scd_timezone_tips a[ttype="tips"]');// .select('a[ttype="tips"]');
    if(tip.length>0){
            var myTooltip = new YAHOO.widget.Tooltip("myTooltip", {autodismissdelay:15000,width:400, context:tip[0], text:tip[0].getAttribute("tips") } ); 
                
    }
     
}

/**
 * This function is used to show progress message while communicating to the
 * server
 * 
 * @param {Object}
 *            msg
 */
function progress_message(msg){
    
     if (msg != null) {
        var img     = document.createElement('img');
        img.setAttribute('src', 'images/loading.gif');
        img.style.width="auto";
        img.style.height="auto";
        var spn=document.createElement('span');
        spn.appendChild(document.createTextNode(msg));
        spn.style.verticalAlign="middle";       
        spn.style.paddingLeft="15px";
        $("progressMessage").innerHTML="";
        $("progressMessage").appendChild(img);
        $("progressMessage").appendChild(spn);      
        $("progressMessage").show();
        
        $("statusMessage").innerHTML="";
        $("statusMessage").hide();
     }else{     
        $("progressMessage").innerHTML = "";
        $("progressMessage").hide();
     }
}

/**
 * To display status message on screen after the server responded for client
 * request
 * 
 * @param {Object}
 *            msg
 */
function message_status(msg){
     if (msg != null) {
        progress_message(null);
        var span=document.createElement('div');
        $j(span).append(msg);
        
        span.style.verticalAlign="middle";
        span.style.padding="2px 5px 2px 2px";
        $j(span).css("float","left");
        span.style.display="inline";
        $("statusMessage").innerHTML = "";
        $("statusMessage").appendChild(span);
        var closeBtn=document.createElement('a');
        var img=document.createElement("img");
        img.src="images/closebtn.png";
        img.style.border="0px";
        closeBtn.href="#";
        closeBtn.appendChild(img);
        closeBtn.onclick=function(){
            message_status(null);
            return false;
        }
        var cpan=document.createElement('div');     
        cpan.style.display="inline";
        cpan.appendChild(closeBtn);
        $("statusMessage").appendChild(cpan);
        $("statusMessage").style.verticalAlign="middle";
        $("statusMessage").show();
     }else{     
        $("statusMessage").innerHTML = "";
        $("statusMessage").hide();
     }
}

DWREngine.setErrorHandler(errorHandleDWR);

function errorHandleDWR(message){
    message_status(message);
}

var r_editorModes=new Array();
    
r_editor_click1Msg="Click here to edit R Script";
r_editor_click2Msg="Click here to validate R Script";
function container_clickfn(elementid){
     if (!r_editorModes[elementid]) {
 
            var ta=document.createElement("textarea");
            
            var elementid1=elementid;
            ta.onblur=function(){
                var inpfield=$($(elementid1).getAttribute("fieldid"));          
                inpfield.value =this.value;
            }
            ta.style.width="100%"
            ta.rows="23";
            ta.value=$($(elementid).getAttribute("fieldid")).value;
            ta.setAttribute("wrap","off");      
            var ep1=$(elementid).select('[elementid="'+elementid+'"]');         
            ep1[0].innerHTML=r_editor_click2Msg;
            var ep=$(elementid).select('[reditorpane="true"]');
            ep[0].innerHTML="";         
            ep[0].appendChild(ta);
        }else{
            
            var pr=document.createElement("pre");
            pr.className="sh_slang";
            
            var tarea=$(elementid).select('textarea');
            
            var inpfield=$($(elementid).getAttribute("fieldid"));
            
            inpfield.value =tarea[0].value;         
            
            pr.appendChild(document.createTextNode(inpfield.value));
            pr.style.height="400px";
         
            var ep=$(elementid).select('[reditorpane="true"]');         
            ep[0].innerHTML="";
            ep[0].appendChild(pr);
            
            var ep1=$(elementid).select('[elementid="'+elementid+'"]');         
            ep1[0].innerHTML=r_editor_click1Msg;    
             
            sh_highlightDocument();
                        
        }
        r_editorModes[elementid]=!r_editorModes[elementid]
}

var js_editorModes=new Array();
    
js_editor_click1Msg="Click here to edit Rhino JavaScript";
js_editor_click2Msg="Click here to validate Rhino JavaScript";
function js_container_clickfn(elementid){
     if (!js_editorModes[elementid]) {
            var ta=document.createElement("textarea");
            var elementid1=elementid;
            ta.onblur=function(){
                var inpfield=$($(elementid1).getAttribute("fieldid"));          
                inpfield.value =this.value;
            }
            ta.style.width="100%"
            ta.rows="23";
            ta.value=$($(elementid).getAttribute("fieldid")).value;
            ta.setAttribute("wrap","off");      
            var ep1=$(elementid).select('[elementid="'+elementid+'"]');         
            ep1[0].innerHTML=js_editor_click2Msg;
            var ep=$(elementid).select('[reditorpane="true"]');
            ep[0].innerHTML="";         
            ep[0].appendChild(ta);
        }else{
            var pr=document.createElement("pre");
            pr.className="sh_javascript";
            
            var tarea=$(elementid).select('textarea');
            
            var inpfield=$($(elementid).getAttribute("fieldid"));
            
            inpfield.value =tarea[0].value;         
            
            pr.appendChild(document.createTextNode(inpfield.value));
            pr.style.height="400px";
         
            var ep=$(elementid).select('[reditorpane="true"]');         
            ep[0].innerHTML="";
            ep[0].appendChild(pr);
            
            var ep1=$(elementid).select('[elementid="'+elementid+'"]');         
            ep1[0].innerHTML=js_editor_click1Msg;   
             
            sh_highlightDocument();
                        
        }
        js_editorModes[elementid]=!js_editorModes[elementid]
    }

function scd_showHistory(){
    
    if(IGNORE_LIVEUPDATE_PAGE) return;
    if($j("#scd_datefilter").val()==''){
        $j("#scd_datefilter").val("Last1Hour");
    }
    
    var dobj=getDateInputs("scd_datefilter","trigger_datetime");
    
    if (dobj.queryCheck == false) {
          
          alert("Please select valid period");
          return false;
        }           
    else{
        
        var statfilter=$("scd_statusfilter").value;
        dobj.statfilter = statfilter;
        dobj.scd_typefilter = $("scd_typefilter").value;
     
        var fld1=$j("#scd_filterfield").val();
        dobj.fld1 = fld1;
        dobj.scd_filterfieldval = $j("#scd_filterfieldval").val();     
        
        dobj.scd_filterddval = $j("#scd_filterdd").val();
          
    }

    // alert(dobj.query)
    progress_message("Please wait...");
    SchedulerMgmt.getQueueLogs(dobj,function(data){
        progress_message(null);
        scd_genQLogs(data,"scd_histLogsList");
    });        
    
}

$j(function() {
        $j("#dialog_box_pstat").dialog({
            autoOpen: false,
            width: 950,modal:true    
        });
});

function scd_dialogPeerCommand(thisobj) {
    currentPeer4Command=$j(thisobj).attr("peer");
    var name=$j(thisobj).attr("name");
    $j("#dialog_peerCommand").commonDialog({width:500, height:550});
    $j("#dialog_peerCommand").commonDialog({title:"Command to "+name});
} 

function scd_dialogQueueSetting() {
    
    var respBack=function(data){
        progress_message();
        $j("#dialog_queueSettings").commonDialog({width:900, height:650,modal:true});
        if(data!=null ) {       
            var htm="";
            htm+="<table width='99%' style='padding:10px 0px;'>";
            for (uik in data.types) {
                htm+="<tr group='"+uik+"'><td width='300px' style='border-bottom:1px dotted grey;'>"+data.types[uik]+"</td><td style='border-bottom:1px dotted grey;'>Alert if <input name='"+uik+"' type='text' size='4' id='' value=''/>minutes delay</td></tr>";
            }
            htm+="</table>";
            $j("#scd_qx_delayalert").html(htm);
            $j("#scd_qx_delayalert").css("width","550px");
            $j("#scd_qx_delayalert").css("border-right","1px dotted grey");

            if(data.tags!=null){
                for(iab=0;iab<data.tags.length;iab++){
                    if(data.tags[iab].tagname.trim().indexOf("thm-")==0){
                        var tag=data.tags[iab].tagname.trim().replace("thm-","");
                        $j("#scd_qx_delayalert_theme").append("<option value='"+tag+"'>"+tag+"</option>")
                    }
                }
            }
            if(data.data!=null){
                for(ky in data.data) {                  
                    $j("#dialog_queueSettings [name='"+ky+"']").val(data.data[ky])
                }
            }
        }   
         
    }
    progress_message("Please wait...")
    SchedulerMgmt.getScriptTypes(respBack);
} 

function scd_queueSettingSave(){
    var data=new Object();
    $j("#dialog_queueSettings input,#dialog_queueSettings select,#dialog_queueSettings textarea").each( function(idx,el){
        var ky=$j(el).attr("name");
        if(ky!=null && ky!=''){
            data[ky]=$j(el).val();
        }
    })
    var respBack=function(data){
        message_status("Data updated");
    }
    progress_message("Please wait...")
    SchedulerMgmt.updateTimeoutSettings(data,respBack); 
}

function scd_sendPeerCommand(thisobj) {
    var cvalue=$j(thisobj).attr("cvalue");
    if(cvalue=='start' || cvalue=='stop') {
        $j("#peerCommandOutput").html("Please wait while sending your command...");
        SchedulerMgmt.commandStopStartPeer(currentPeer4Command,cvalue,function(data){
            $j("#peerCommandOutput").html(data);
        });
    }
    if(cvalue=='command'){
        $j("#peerCommandOutput").html("Please wait while sending your command...");
        var cvalue=$j("#peerCommandInput").val();
        SchedulerMgmt.command2PeerWithWait(currentPeer4Command,cvalue,5,function(data){
            $j("#peerCommandOutput").html(data.response);
        });
    }
}

function scd_getPeerThreads(peer, unixpeers){
    var unixPeersName = Array();
    $j.each(unixpeers,function(index,item){unixPeersName.push(item.peername)});
    var respBack=function(data){
        if(data!=null){
           $j('#peerthread-'+peer).html("["+data["rserve.concurrent.sessions"]+" th]");
        }
    }   
    if ($j.inArray( peer,  unixPeersName) > -1 ){
        SchedulerMgmt.getPeerSpecificStat(peer,respBack);
    } 
}
 
var currentPeer4Command=null;
function peerAssociationMatrix(data){
	
	var respBack = function(rdata) {
		if ((rdata.indexOf("r")>=0) || (rdata.indexOf("R")>=0)) {
	        if (data != null) {         
	            if(data.peernotes!=null) {
	                $j("#peersToolBar").append("Send Commands To: <ul class='peerTool'>");
	                for(iab=0;iab<data.peernotes.length;iab++){
	                    var pname=(data.peernotes[iab].friendlyname!=null && data.peernotes[iab].friendlyname!='')? data.peernotes[iab].friendlyname :data.peernotes[iab].peername;
	                    var respB=function(data,peerid){
	                        if(data!=null && data.response!=null){
	                            $j("#peersToolBar ul li[peer='"+data.peer+"']").css("border","2px solid #20B320");
	                        }
	                    }                   
	                    var waitSecs=5;
	                    SchedulerMgmt.command2PeerWithWait(data.peernotes[iab].peername,"cmd.exe /c sc query 4EPeer",waitSecs,respB );
	                    var ver="";
	                    if(data.pversion!=null && data.pversion[data.peernotes[iab].peername]!=null){
	                        ver="<span class='pversionBatch'>ver: "+data.pversion[data.peernotes[iab].peername]+"</span>"
	                    }
	                    $j("#peersToolBar ul").append("<li onclick='scd_dialogPeerCommand(this)' name='"+pname+"' peer='"+data.peernotes[iab].peername+"'><span title='"+data.peernotes[iab].peername+"'>"+pname+"</span>"+ver+"</li>");
	                }
	            }
	            
	            if(data.rserve_unix_peers!=null) {
	                $j("#unixPeerSetup");
	                var upeers=data.rserve_unix_peers;
	                $j("#unixPeerSetup").html("<div class='label'>RServe Config</div><ul></ul>")
	                for(iab=0;iab<upeers.length;iab++){
	                    var pname=($j.trim(upeers[iab].friendlyname)!='') ?upeers[iab].friendlyname :upeers[iab].peername;
	                    $j("#unixPeerSetup ul").append("<li peer='"+upeers[iab].peername+"' onclick='scd_openRConfig(this)'>"+pname+"</li>")
	                }               
	            }
	            
	            if(data.peers!=null && data.tasks!=null){
	                $j("#dialog_box_peermatrix_data").html("<table id='pa_matrix_tbl' class='peer_matrix' cellspacing='0'><thead><tr></tr></thead><tbody></tbody></table>");                
	     
	                
	                 
	                for (iac = -1; iac < data.tasks.length; iac++) {    
	                    var rw=null;
	                    var c1row="&nbsp;";
	                    
	                    if(iac>=0){
	                            rw= $j("#dialog_box_peermatrix_data table tbody");                              
	                            rw.append("<tr></tr>");                     
	                        rw= $j("#dialog_box_peermatrix_data table tbody tr:last-child");
	                    }else{
	                        rw=$j("#dialog_box_peermatrix_data table thead");
	                        rw.append("<tr></tr>");
	                        rw=$j("#dialog_box_peermatrix_data table thead tr:last-child");
	                    }                           
	                    var cellclass="";
	                   
	                    for(iab=-1;iab<data.peers.length;iab++){                            
	                        var titletd=null;
	                        var cellcont="";

	                        if(iac==-1){
	                         
	                            if (iac == -1 && iab >= 0) {
	                                
	                                var fname="";
	                                try{
	                                    fname=data.peers[iab].toUpperCase().substring(0,3);
	                                    var gp=(/4ECAP(PC|SV|LT|VM)SG(\d+)/g).exec(data.peers[iab].toUpperCase())
	                                    if(gp!=null && gp.length>2){
	                                        fname=gp[1].substring(0,1)+gp[2];
	                                    }
	                                }catch(ex){}
	                                    
	                                    cellcont="<div class='plink'><div class='taskname'> <a  href='#'font-size:1.3em;text-decoration: none; hover {text-decoration: underline;} onclick='peerNotesEdit(this)' peer='"+data.peers[iab]+"'>"+data.peers[iab]+"</a>" +
	                                            "</div><div  align='center' class='peerthread' id='peerthread-"+data.peers[iab]+"'>[1 th]</div><div class='peerbtn'><div class='shortname'>"+fname+"</div><a href='#' peername='"+data.peers[iab]+"' onclick='scd_PA_history4Peer(this);return false;' ><img title='Show History' src='images/show_history.png'></a><div class='ccount'></div></div></div>";
	                                    cellclass=" class='peers' align='right' peer='"+data.peers[iab]+"' ";
	                                    scd_getPeerThreads(data.peers[iab],data.rserve_unix_peers);
	                            }
	                        }else{                                      
	                            
	                            if (iac>=0 && iab==-1) {
	                                titletd=data.tasks[iac].key;                             
	                                var p='<div class="taskdetails">'+data.tasks[iac].value+'</div>';                
	                                cellcont=p+'<div class="taskdetails" align="center"><img src="'+scd_groupIcons[data.tasks[iac].key]+'"> <a  taskuid="'+titletd+'" onclick="scd_PA_history4Task(this);return false;" href="#" onclick="return false;" ><img title="Show History" src="images/show_history.png"></a>  </div><div class="ccount taskdetails"></div>';
	                                cellclass=" class='tasks' taskuid='"+data.tasks[iac].key+"' ";
	                                
	                            }
	                        }
	                        if (iac==-1 && iab==-1) {
	                            cellcont="<a href='https://wiki.4ecap.com/4ECwiki/Four_Elements_Capital_Systems' target='_new'>More Info on Peers</a>";
	                            cellclass=" align='center' ";
	                        }
	                        
	                        
	                        var cellid="";
	                        var eid=null;
	                        if (iac >= 0 && iab >= 0) {
	                            var eid=data.tasks[iac].key+"_"+data.peers[iab];
	                                                        
	                            var ckd="";
	                            if(data.p_a!=null && data.p_a[eid]!==null && data.p_a[eid]=='yes'){
	                                ckd=" checked ";
	                            }
	                            var attrb=" peer='"+data.peers[iab]+"' taskuid='"+data.tasks[iac].key+"' ";
	                            cellcont="<input onclick='cellColors(this)' type='checkbox' style='border:2px solid red' id='"+eid+"' "+ckd+attrb+">";
	                            cellclass=" align='center' ";
	                        }else{
	                            // cellclass="class='peerOrTask' ";
	                        }           
	                        
	                        if((eid != null && $j('td input#' + eid).length > 0 ) || titletd!=null) {   }else {
	                            rw.append("<td " + cellclass + ">" + cellcont + "</td>");
	                        }
	                        if(titletd!=null &&  $j('td.tasks[taskuid="'+titletd+'"] ').length==0){                         
	                            rw.append("<td " + cellclass + ">" + cellcont + "</td>");
	                        }
	                    } // for loop
	                } // for loop
	                cellColorsGreen();
	                
	                $j("#pa_matrix_tbl tbody tr td:not(:first-child)").each(function(idx,el) {       
	                       $j(el).mouseenter(function(evobj){                                                             
	                            var col=$j(evobj.target).index()+1;         
	                            $j("#pa_matrix_tbl tbody tr td:nth-child("+col+"),#pa_matrix_tbl thead tr td:nth-child("+col+")").addClass("hl_col");                            
	                            $j(evobj.target).parent().find("td").addClass("hl_row");
	                            
	                       });
	                       $j(el).mouseleave(function(evobj){
	                            $j("#pa_matrix_tbl td.hl_col").removeClass("hl_col");
	                            $j("#pa_matrix_tbl td.hl_row").removeClass("hl_row");                               
	                       });
	                       
	                });

	                
	                
	                if(data.peernotes!=null){
	                    peerNotesUpdate(data.peernotes);
	                }
	            }
	        }
		} else {
			message_status("You don't have right to access Peer Association");
		}
	}
	
	IExecAccessMgmt.checkUserPermissionNew("Scheduler.PeerAssociation", respBack);

}

function scd_PA_history4Peer(thisobj){
    var peer=$j(thisobj).attr("peername")    
    $j("#dialog_peerAsHist").commonDialog({close: function(event, ui){},modal: true,title: peer,width: 600,position: 'center'});
    var tbl=$j("#dialog_peerAsHist_bdy").html("");
    var respBack=function(resdata){
        var tbl=$j("#dialog_peerAsHist_bdy").html("<table class='myTableCSS' width='99%'><tbody></tbody></table>").find("table.myTableCSS");        
        for(var iab=0;iab<resdata.length;iab++){
                var val="";                
                if(resdata[iab].taskuid==null){
                    val='<img style="margin-left:5px" border="0" src="'+((resdata[iab].action==1)?"images/running_button.gif":"images/pause_button.gif")+'">';                    
                } else{                   
                   if(resdata[iab].action==1){
                       val="<div style='display:inline-block;padding:3px;background:#063;margin-right:5px'><input type='checkbox' disabled='disabled' checked='checked'></div>"+resdata[iab].name;
                   } else{
                       val="<div style='display:inline-block;padding:3px;margin-right:5px'><input type='checkbox' disabled='disabled' ></div>"+resdata[iab].name;
                   }                   
               }          
               var dt=moment(resdata[iab].action_datetime).fromNow();
              tbl.append("<tr><td>"+val+"</td><td>"+resdata[iab].username+"</td><td>"+dt+"</td></tr>"); 
        }
        
    }
    SchedulerMgmt.getPeerAssHist4Peer(peer,respBack);
}

function scd_PA_history4Task(thisobj){
    var taskuid=$j(thisobj).attr("taskuid")    
    $j("#dialog_peerAsHist").commonDialog({close: function(event, ui){},modal: true,width: 600,position: 'center'});    
    var tbl=$j("#dialog_peerAsHist_bdy").html("");
    var respBack=function(resdata){
        var tbl=$j("#dialog_peerAsHist_bdy").html("<table class='myTableCSS' width='99%'><tbody></tbody></table>").find("table.myTableCSS");
        if(resdata.length){
            $j("#dialog_peerAsHist").commonDialog("option","title",resdata[0].name);
        }
        for(var iab=0;iab<resdata.length;iab++){
                var val="";
                var p=resdata[iab].peername+((resdata[iab].friendlyname!=null && resdata[iab].friendlyname!='')?"("+resdata[iab].friendlyname+")":"");                          
                if(resdata[iab].action==1){
                      val="<div style='display:inline-block;padding:3px;background:#063;margin-right:5px'><input type='checkbox' disabled='disabled' checked='checked'></div>"+p;
                } else{
                       val="<div style='display:inline-block;padding:3px;margin-right:5px'><input type='checkbox' disabled='disabled' ></div>"+p;
                }                   
                          
               var dt=moment(resdata[iab].action_datetime).fromNow();
              tbl.append("<tr><td>"+val+"</td><td>"+resdata[iab].username+"</td><td>"+dt+"</td></tr>"); 
        }
    }
    SchedulerMgmt.getPeerAssHist4Task(taskuid,respBack);
}

var scd_onOpenRConfigOn=true;

function scd_onopenRConfig(peer){
     console.log("open:"+peer);
     var peer1=peer;
     
     var respBack=function(data){
         if(data!=null){
             
             var run_ids=[];
             for(iab=0;iab<data.length;iab++) {
                 run_ids[run_ids.length]=data[iab].uid;
                 var cname=data[iab].scriptname!=null? "running":"";
                 
                 if($j("li#"+data[iab].uid).length==0){
                     var sname= data[iab].scriptname!=null? "<div class='scriptname'>"+data[iab].scriptname+"</div>":"<div class='scriptname'></div>";
                    
                     var mkup="<li "+cname+" id='"+data[iab].uid+"' processid='"+data[iab].process_id+"'>"+sname+"<div class='otherinfo'><div class='otinfo cpu'>CPU:<strong>"+data[iab].CPU+"</strong>% </div><div class='otinfo memory'>Mem:<strong>"+data[iab].Memeory+"</strong>%</div><div class='otinfo exec'>Exec:<strong>"+data[iab].no_executions+"</strong></div></div><div class='deletebtn'><a href='#' onclick='scd_removeProcess(this);return false;' uid='"+data[iab].uid+"' peername='"+peer1+"' processid='"+data[iab].process_id+"'> <img src='images/remove_button.gif'></a></div></li>";
                     $j("#dialog_Rserveconfig_proc ul").append(mkup);                    
                 }else{
                     $j("li#"+data[iab].uid).find(".scriptname").html((data[iab].scriptname!=null)?data[iab].scriptname:"");
                     $j("li#"+data[iab].uid).find(".cpu").html("CPU:<strong>"+data[iab].CPU+"</strong>%");
                     $j("li#"+data[iab].uid).find(".memory").html("Mem:<strong>"+data[iab].Memeory+"</strong>%");
                     $j("li#"+data[iab].uid).find(".exec").html("Exec:<strong>"+data[iab].no_executions+"</strong>");
                 }
                 if(data[iab].scriptname!=null) $j("li#"+data[iab].uid).addClass("running") 
                 else $j("li#"+data[iab].uid).removeClass("running") 
             }
             $j("#dialog_Rserveconfig_proc ul li").each(function(idx,ele){
                 var id=$j(ele).attr("id");
                 if($j.inArray(id,run_ids)==-1){
                     $j(ele).remove();               }
             });
             
         }
     }
     SchedulerMgmt.getRserveSessionDetails(peer1,respBack);
     
     setTimeout(
        function(){ 
          if($j("#dialog_Rserveconfig").dialog("isOpen") && scd_onOpenRConfigOn){
              scd_onopenRConfig(peer1);
          }
        }
     ,1000);
}

function scd_openRConfig(thisobj) {
    var peer=$j(thisobj).attr("peer");
    
    var respBack=function(data){
        if(data!=null){
            $j("#rserve_noofsessions").val(data["rserve.concurrent.sessions"]);
            $j("#rserve_noofexec").val(data["rserve.max.executions.in.session"]);
            $j("#rserve_peer").val(peer);
        }
    }   
    SchedulerMgmt.getPeerSpecificStat(peer,respBack);
    $j("#dialog_Rserveconfig").commonDialog({
        autoOpen: true,
        width: 550,
        modal: true,
        open: function( event, ui ) {scd_onopenRConfig(peer)}
    });
}

function scd_removeProcess(thisobj){
    var uid=$j(thisobj).attr("uid");
    var pid=$j(thisobj).attr("processid");
    var peer=$j(thisobj).attr("peername");
    if(confirm("Are you sure you wish to kill the process?")){
        SchedulerMgmt.killRserveProcess(peer,+pid,function(){});
    }
}

function scd_rservesettings(){
    var p1=$j("#rserve_noofsessions").val();
    var p2=$j("#rserve_noofexec").val();
    var peer=$j("#rserve_peer").val();
    SchedulerMgmt.setPeerSpecificStat(peer,+p1,+p2,function(data){message_status("Settings sent to peer");})
    return false;
}

$j(function() {
    $j("#dialog_box_peernbox").dialog({
                autoOpen: false,
                width: 550,
                modal: true
    });
});

function peerNotesEdit(thisobj) {
    var peer=$j(thisobj).attr("peer");
    var respBack = function(data){
        $j("#dialog_box_peernbox").dialog("open");
        $j("#dialog_box_peernbox_notes").attr("peer",peer);
        if (data != null && data.notes!=null) {
            $j("#dialog_box_peernbox_notes").val(data.notes);           
        }else{
            $j("#dialog_box_peernbox_notes").val('');
        }
        
        if (data != null && data.friendlyname!=null) {
            $j("#dialog_box_peernbox_fname").val(data.friendlyname);            
        }else{
            $j("#dialog_box_peernbox_fname").val('');
        }
    }
    SchedulerMgmt.getPeerNotes(peer,respBack);
}

function peerNotesEditSave(){
    var notes=$j("#dialog_box_peernbox_notes").val();
    var friendlyname=$j("#dialog_box_peernbox_fname").val();
    var peer=$j("#dialog_box_peernbox_notes").attr("peer");
    $j("#dialog_box_peernbox").dialog("close");
    var respBack = function(data){
        if(data.peernotes!=null){
            peerNotesUpdate(data.peernotes);
        }   
    }
    SchedulerMgmt.savePeerNotes(peer,notes,friendlyname,"",respBack);
}

function peerNotesUpdate(pdata){
    for(iad=0;iad<pdata.length;iad++){
        var peer=pdata[iad].peername;
        var cell=$j('#pa_matrix_tbl thead tr td.peers[peer="'+peer+'"]');
        if(cell.length>0 && pdata[iad].notes!=null &&  $j.trim(pdata[iad].notes)!=''){
            cell.css("background-image","url(images/stickynote.png)");
            cell.css("background-repeat","no-repeat");
            cell.css("background-position","5px 10px");
        }
        if(cell.length>0 && pdata[iad].friendlyname!=null && pdata[iad].friendlyname!=''){
            if($j(cell).find('span.friendlyname').length>0){
                $j(cell).find('span.friendlyname').html(" ("+pdata[iad].friendlyname+")");              
            }else{
                $j(cell).find('div.plink a[peer]').after("<span class='friendlyname'>  ("+pdata[iad].friendlyname+")</span>");
            }
        }
        
        if(pdata[iad].notes==null || (pdata[iad].notes!=null && pdata[iad].notes=='')){
            
        }else{
            $j(cell).attr("title",pdata[iad].notes);
        }
        
        var activimag=(pdata[iad].active==-1)?'<a href="#" onclick="scd_togglePeer(\''+peer+'\',0); return false;"><img border="0" src="images/pause_button.gif"></a>':'<a href="#" onclick="scd_togglePeer(\''+peer+'\',-1); return false;"><img border="0" src="images/running_button.gif"></a>';
        if($j(cell).find('.peerbtn div.toolbar').length>0){
            $j(cell).find('.peerbtn div.toolbar').html(activimag);              
        }else{
            $j(cell).find('.peerbtn').prepend("<div class='toolbar'>"+activimag+"</div>");
        }
    }
}

function scd_togglePeer(peer,enb){
    var respBack = function(data){
        if(data.peernotes!=null){
            peerNotesUpdate(data.peernotes);
        }   
    }
    SchedulerMgmt.savePeerToggle(peer,enb,respBack);
}

function cellColorsGreen() {
	$j("#pa_matrix_tbl tr td").each(function(idx,el){                   
		 if($j(el).children().attr('checked')){
			 $j(el).addClass("peerTaskAssociated");
		 }else{
			 $j(el).removeClass("peerTaskAssociated");
		 }
	});
	$j("#pa_matrix_tbl tbody tr td.tasks").each(function(idx,el) {
		 var taskuid=$j(el).attr("taskuid");
		 var selector='div.ccount';                 
		 var count=$j('#pa_matrix_tbl tbody tr td input[taskuid="'+taskuid+'"]:checked').length;
		 $j(el).find(selector).html("["+count+"]");
	});
	
	$j("#pa_matrix_tbl thead tr td.peers").each(function(idx,el) {
		 var peer=$j(el).attr("peer");
		 
		 // var selector='span';
		 var selector='div.ccount';
		 
		 $j(el).children(selector).css("marginLeft","5px");
		 $j(el).children(selector).css("fontSize",".8em"); 
		 $j(el).children(selector).css("color","#006633");           
		 $j(el).children(selector).css("textAlign","right");
		 var count=$j('#pa_matrix_tbl tbody tr td input[peer="'+peer+'"]:checked').length;
		 $j(el).find(selector).html("["+count+"]");
	});			
}

function cellColors(thisobj){
                var taskuid=$j(thisobj).attr('taskuid');
                var peer=$j(thisobj).attr('peer');
                var ischecked=$j(thisobj).is(":checked");

                if(taskuid!=null && peer!=null){
                     var respBack=function(data){
                        progress_message();
                        if(data){
                            message_status("Peer association updated");
							cellColorsGreen();
                        }
						else {
							message_status("You don't have permission to update peer association");
							$j(thisobj).attr('checked', !ischecked);
						}
                     }
                     progress_message("Please wait...");
                     SchedulerMgmt.addRemovePeerTaskuid(taskuid,peer,ischecked,respBack);   
                }
}

function scd_showPeersInfo(){
     scd_showPeersData("statistics")
}

function scd_showPeerPackages(){
     scd_showPeersData("rpackages");
}

function scd_executeScriptPeersUpd(data1) {
    for (key in data1) {
                
        var stat = data1[key];
        var tble=$j("#dialog_box_pstat_data table")[0];
        if (tble != null) {
            var c1 = tble.rows[0].cells[1];
            var p1= $j("#dialog_box_pstat_data input[peer='"+key+"']");
            if(p1!=null && p1[0]!=null){}else{
                $j(c1).append('<input type="checkbox" value="'+key+'"  peer="'+key+'" />'+key+'<span id="peer_'+key+'_isonline"></span><br>');              
            }
            
            var p1= $j("#dialog_box_pstat_data input[peer='"+key+"']");
            if (p1 != null && p1[0] != null) {
                if (stat == 'BUSY') {
                    p1.attr('disabled', true);
                    p1.removeAttr('checked');
                    $j("#dialog_box_pstat_data span#peer_" + key + "_isonline").html("<img src='images/peerbusy.gif' border='0' valign='middle'>");
                }
                else {
                    p1.removeAttr('disabled');
                    $j("#dialog_box_pstat_data span#peer_" + key + "_isonline").html("");
                }
            }
        }
    }
}

function executeScriptAction(){
    var scriptname=$j("#dialog_box_pstat_data input[name='scriptname']").val();
    var script=$j("#dialog_box_pstat_data textarea[name='script']").val();
    var peers = [];
     $j("#dialog_box_pstat_data input[type='checkbox']:checked").each(function() {
        if ($j(this).attr("peer") != null) {
            peers.push($j(this).val());
        }
    });
    
    if(scriptname=='' || script=='' || peers.length==0 ){
        alert("Please input name of the script,R script and minimum one peer");
        return ;
    }else{
        
        var respBack=function(){
            $j("#dialog_box_pstat_data").html("R Script successfully sent to selected peers");
        }
        $j("#dialog_box_pstat_data").html("<img border='0' valign='middle' src='images/loading.gif'/> Please wait while communicating peers...");
        SchedulerMgmt.executeScript(scriptname,script,0,peers,respBack);        
    }
}

function executeScriptLogs(){   
    $j("#dialog_box_pstat_data").html("<img border='0' valign='middle' src='images/loading.gif'/> Please wait..");
    var respBack=function(data){
        $j("#dialog_box_pstat_data").html("");
        if(data!=null){
            
            var prevscrp=0;
            
            for(iab=0;iab<data.length;iab++){
                if(data[iab].script_id!=prevscrp){
                     $j("#dialog_box_pstat_data").append("<h3 style='border-bottom:2px solid grey'>"+data[iab].name+"</h3>");                    
                }
                
                
                
                if(data[iab].status=='success'){
                    $j("#dialog_box_pstat_data").append("<img src='images/task_executed.gif' alt='Excecuted' border='0'>");
                }
                if (data[iab].status == 'fail') {
                    $j("#dialog_box_pstat_data").append("<img src='images/task_failed.gif' alt='Failed' border='0'>");
                }
                $j("#dialog_box_pstat_data").append("<span>"+data[iab].peer+"</span>&nbsp;&nbsp;<span> Started:"+data[iab].started); // +"["+data[iab].duration+"]</span><br>");
                
                prevscrp=data[iab].script_id;
            }
        }
    }   
    SchedulerMgmt.execScriptLogs(respBack);
}

var executeScriptMode=false;
function scd_executeScript(){
    executeScriptMode=true;
    $j("#dialog_box_pstat").dialog("open");
    
    $j("#dialog_box_pstat").dialog({close: function(event, ui)
    {
       executeScriptMode=false;
    }
    });

    $j('#dialog_box_pstat').dialog('option', 'title', 'Execute Script');


    $j("#dialog_box_pstat_data").html("");
    var tble=document.createElement("table");
    var tblrow=tble.insertRow(0);
    var c1=tblrow.insertCell(0);
    var c2=tblrow.insertCell(1);

    var tblrow1=tble.insertRow(1);
    var c10=tblrow1.insertCell(0);
    var c20=tblrow1.insertCell(1);
    
    $j(c10).append("<input type='button' onclick='executeScriptLogs()' value='Show Logs'>");
    $j(c20).append("<input type='button' onclick='executeScriptAction()' value='Excecute'>");

    var txtbox1=document.createElement("input");    
    $j(txtbox1).attr("name","scriptname");
    $j(txtbox1).attr("type","text");    
    c1.appendChild(document.createElement("br"));
    c1.appendChild(document.createTextNode("Script Name:"));
    c1.appendChild(txtbox1);
    c1.appendChild(document.createElement("br"));
    c1.appendChild(document.createElement("hr"));
    
    c2.appendChild(document.createTextNode("Peers Online"));
    c2.appendChild(document.createElement("hr"));
    
    var txtbox=document.createElement("textarea");  
    $j(txtbox).attr("name","script");
    txtbox.cols="100";
    txtbox.rows="25";
    c1.appendChild(document.createTextNode("Script:"));
    c1.appendChild(document.createElement("br"));
    c1.appendChild(txtbox);
    
    $j("#dialog_box_pstat_data")[0].appendChild(tble);
}

function scd_showPeersData(what){   
    $j("#dialog_box_pstat").dialog("open");
    $j("#dialog_box_pstat").dialog({close: function(event, ui) {} });

    $j('#dialog_box_pstat').dialog('option', 'title', 'Peers Information');
    $j("#dialog_box_pstat_data").html("<img border='0' valign='middle' src='images/loading.gif'/> Please wait while communicating peers...");
    var respBack=function(data){
        $j("#dialog_box_pstat_data").html("");
        
        
        if(data!=null){
                var tb="<table id='scd_peer_stattab' class='display'  style='border:1px solid #c0c0c0;margin:10px 0px 20px 0px;font-family1:Arial' ><thead><tr>";
                for(iab=0;iab<data.col.length;iab++){
                    tb+="<th>"+replaceCapitalize(data.col[iab])+"</th>";
                }
                tb+="</tr></thead><tbody></table>";
                                
                $j("#dialog_box_pstat_data").append(tb);                
                var dtable=$j("#scd_peer_stattab").dataTable({bFilter: true,iDisplayLength:25,bInfo:false,bLengthChange:true,bAutoWidth:false});
                for (peer in data.data) {
                    var rowdata=data.data[peer];
                    var thisrow=new Array();                 
                    for(iab=0;iab<data.col.length;iab++){
                         var cont=rowdata[data.col[iab]];
                         if(iab==0){
                            cont=replaceCapitalize(cont)
                         }
                         if (cont == null) {
                            thisrow[iab] = "";                       
                         } else {
                            thisrow[iab] = cont;
                         }
                    }
                    dtable.fnAddData(thisrow);
                }
        }
    }
    SchedulerMgmt.getPeerData(what,respBack);
    return false;
}

function replaceCapitalize(val) {
        while (val.indexOf("_") >= 0) {
            val = val.replace('_', ' ');
        }
        val=val.toLowerCase();
        var dter="";
    
        newVal = '';
        val = val.split(' ');
        for(var c=0; c < val.length; c++) {
            newVal += val[c].substring(0,1).toUpperCase() +
            val[c].substring(1,val[c].length) + ' ';
        }
        return newVal;
}

var tempOpenData=null;
function scd_search(){
    if(false){
        
        var rtn=$j("#scd_searchTasks input.as-values").val();   
        if (rtn != '') {        
                    var data=rtn.split(","); 
                    tempOpenData=data;              
                    for(ibc=0;ibc<data.length;ibc++){
                        var thisdata=data[ibc];
                        var fn_id= data[ibc]; // thisdata.data.id;
                        if (thisdata != '') {
                            if ($j("#scd_tabs-" + fn_id).length > 0) {
                                var index = $j('#scd_tabs a[href="#scd_tabs-' + fn_id + '"]').parent().index()
                                scd_editorTab.tabs('select', index);
                                                        
                            }else {
                                setTimeout("scd_loadTask(" + thisdata + ")", ibc * 1500);
                            }
                        }
                        
                    }
                    $j("#scd_searchTasks li.as-selection-item").remove();
                    $j("#scd_searchTasks li.as-original input.as-values").val('');              
                    
                var readonly=true;
         
        }else {
            alert("Task name is not selected, minimum 1 task required");
        }
    }else{
        var data=$j('#scd_searchTasksInp').multiSelect("getSelected");
        if (data != '') {   
            tempOpenData=data;              
            for(ibc=0;ibc<data.length;ibc++){
                var thisdata=data[ibc];
                var fn_id= data[ibc]; // thisdata.data.id;
                if (thisdata != '') {
                    if ($j("#scd_tabs-" + fn_id).length > 0) {
                        var index = $j('#scd_tabs a[href="#scd_tabs-' + fn_id + '"]').parent().index()
                        scd_editorTab.tabs('select', index);                                                
                    }else {
                        setTimeout("scd_loadTask(" + thisdata + ")", ibc * 1500);
                    }
                }
            }
            $j('#scd_searchTasksInp').multiSelect("removeAllSelected");
            
        var readonly=true;
 
}else {
    alert("Task name is not selected, minimum 1 task required");
}
    }
}

function scd_getOpenedEditableScripts(){
    var ids=new Array();
    for(iab=0;iab<scd_editorTab.data().tabs.panels.length;iab++){        
        var activepanel=scd_editorTab.data().tabs.panels[iab];
        if($j(activepanel).attr("script_access")=="RWX"){
            var scd_id=$j(activepanel).attr("id")
            ids[ids.length]=parseInt(scd_id.replace("scd_tabs-",""));
        }
    }
    return ids; 
}

var myLog=function(msg){
    console.log(msg);
}

function scd_refreshFlowChart(){
    window.open ("image.jsp?file=/home/fileserv/Sharing/Public/Research/scheduler.svg&d="+new Date().toUTCString(),"mywindow","location=0,scrollbars=1,menubar=0,resizable=1,width=1024,height=800"); 
}

function scd_toggleTree(thisobj){
    
    $j(".td_folder_tree").toggleClass( "hide", 500);
    $j("#scd_taskpane_holder > .ui-tabs-panel").toggleClass( "leftbr", 500);
    
    return false;
}  

var scd_editorTab=null;
var scd_currentTabCotent="";
$j(function() {
        scd_editorTab = $j("#scd_taskpane_holder").tabs({
            tabTemplate: "<li><a href='#{href}'>#{label}</a> <span class='ui-icon ui-icon-close'>Remove Tab</span></li>",
            add: function( event, ui ) {
                  
                $j( ui.panel ).append(scd_currentTabCotent);
                scd_editorTab.tabs( "select", ui.index);
            },
            select: function(event, ui) { 
               setTimeout(scd_highLightFunc,200);
            }
        });     
        
        
        // close icon: removing the tab on click
        // note: closable tabs gonna be an option in the future - see
        // http://dev.jqueryui.com/ticket/3924
        $j( "#scd_taskpane_holder span.ui-icon-close" ).live( "click", function() {
            var index = $j( "li", scd_editorTab ).index( $j( this ).parent() );
            
            var activepanel=scd_editorTab.data().tabs.panels[index];
            var task_id=$j(activepanel).attr("id")
            task_id=task_id.replace("scd_tabs-","");
                    scd_editorTab.tabs( "remove", index );               
                    scd_highLightFunc(); 
                    myTriggerTableObj[task_id]=null;
                    SchedulerMgmt.unLockTask(parseInt(task_id),function(data){});
        });
});

function scd_direct_open(sc_id){
    
       $j(".fe-mainMenu").hide();
       $j("#tabface").hide();
       
       //scd_loadTask(sc_id);
       var data=sc_id.split(",");
       tempOpenData=data;               
       for(ibc=0;ibc<data.length;ibc++){
                var thisdata=data[ibc];
                var fn_id= data[ibc]; // thisdata.data.id;
                if (thisdata != '') {
                    if ($j("#scd_tabs-" + fn_id).length > 0) {
                        var index = $j('#scd_tabs a[href="#scd_tabs-' + fn_id + '"]').parent().index()
                        scd_editorTab.tabs('select', index);                                                
                    }else {
                        setTimeout("scd_loadTask(" + thisdata + ")", ibc * 1500);
                    }
                }
       }        
       $j("#scd_taskpane_holder").appendTo("body");
    
}

var tab_appendedToBody=false;
function listener(event){
       if(typeof event.data =='string' && event.data.indexOf("open_task:")>=0){
           // rf_open();
           if(!tab_appendedToBody){
               $j("#scd_taskpane_holder").appendTo("body");        
               $j(".fe-mainMenu").hide();
               $j("#tabface").hide();
               tab_appendedToBody=true;
           }
           var sc_id=event.data.split(":")[1];         
           scd_loadTask(+sc_id);
       }
    }
addEventListener("message", listener, false)

/*******************************************************************************
 * Local Time script- � Dynamic Drive (http://www.dynamicdrive.com) This notice
 * MUST stay intact for legal use Visit http://www.dynamicdrive.com/ for this
 * script and 100s more.
 ******************************************************************************/

var weekdaystxt=["Sun", "Mon", "Tues", "Wed", "Thurs", "Fri", "Sat"]
function showLocalTime(container, servermode, offsetMinutes, displayversion){
if (!document.getElementById || !document.getElementById(container)) return
this.container=document.getElementById(container)
this.displayversion=displayversion
//var servertimestring=
this.localtime=this.serverdate=new Date(servertimestring)
this.localtime.setTime(this.serverdate.getTime()+offsetMinutes*60*1000) // add
                                                                        // user
                                                                        // offset
                                                                        // to
                                                                        // server
                                                                        // time
this.updateTime()
this.updateContainer()
}

showLocalTime.prototype.updateTime=function(){
var thisobj=this
this.localtime=new Date(servertimestring);
this.localtime.setSeconds(this.localtime.getSeconds()+1)
setTimeout(function(){thisobj.updateTime()}, 1000) //update time every second
}

showLocalTime.prototype.updateContainer=function(){
var thisobj=this
if (this.displayversion=="long")
this.container.innerHTML=this.localtime.toLocaleString()
else{
var hour=this.localtime.getHours()
var minutes=this.localtime.getMinutes()
var seconds=this.localtime.getSeconds()
var ampm=(hour>=12)? "PM" : "AM"
var dayofweek=weekdaystxt[this.localtime.getDay()]
this.container.innerHTML=formatField(hour, 1)+":"+formatField(minutes)+":"+formatField(seconds)+" "+ampm ; // +"
                                                                                                            // ("+dayofweek+")"
}
setTimeout(function(){thisobj.updateContainer()}, 1000) // update container
                                                        // every second
}

function formatField(num, isHour){
if (typeof isHour!="undefined"){ //if this is the hour field
var hour=(num>12)? num-12 : num
return (hour==0)? 12 : hour
}
return (num<=9)? "0"+num : num//if this is minute or sec field
}