
var ie_groupColor=null;
var treeSelector="#ie_treeMenu";
var treeMenuRendered=false;
var ie_strategies=new Array();
var areaEditOptions=new Object();
var prevCtxClk=null;

function ie_pageinit() {
	var respBack=function(data){
		progress_message(null);
		if(data!=null){			
			if(data.tags!=null){				
				for (ic = 0; ic < data.tags.length; ic++) {
					rf_TagColors[data.tags[ic].tagname]=data.tags[ic].background_color;		
				}	
			}
			ie_groupColor=data.group_colors;
			ie_treeListGen(data);
		}
	}
	
	progress_message("please wait...")
	IExecMgmt.getTreeViewData(respBack);
}

 

var ie_editorTab=null;
$j(function() {
	
	
		ie_editorTab = $j( "#ie_tabs").tabs({
			tabTemplate: "<li><a href='#{href}'>#{label}</a> <span class='ui-icon ui-icon-close'>Remove Tab</span></li>",
			add: function( event, ui ) {
				$j( ui.panel ).append(currentTabCotent);
				ie_editorTab.tabs( "select", ui.index);
			},
			select: function(event, ui) { 
			   var func_id=ui.panel.id;
			   if(func_id!=null) func_id=func_id.replace("ie_tabs-","");
			   setTimeout(ie_highLightFunc,200);
			   
			}
		});		
		
		
		// close icon: removing the tab on click
		// note: closable tabs gonna be an option in the future - see http://dev.jqueryui.com/ticket/3924
		
		$j( "#ie_tabs >ul >li  span.ui-icon-close" ).live( "click", function() {
			var index = $j( "li", ie_editorTab ).index( $j( this ).parent() );
			
			
			var activepanel=ie_editorTab.data().tabs.panels[index];
			var func_id=$j(activepanel).attr("id")
			func_id=func_id.replace("ie_tabs-","");
	
			if(ie_EditorTyped[func_id]!=null  && ie_EditorTyped[func_id]){
				if(confirm("Changes will be lost! Are you sure you want to close ?")){
					ie_editorTab.tabs( "remove", index );
					if (false) {
						IExecMgmt.unLockStrategyFromCache(parseInt(func_id), function(data){
						});
						ie_highLightFunc();
						ie_EditorTyped[func_id] = null;
						areaEditOptions[func_id] = null;
					}
				}
			}else{
				ie_editorTab.tabs( "remove", index );
				
					IExecMgmt.unLockStrategyFromCache(parseInt(func_id), function(data){
					});
					ie_highLightFunc();
					areaEditOptions[func_id] = null;
			}
				
			
			
		});
		
		
});



function ie_treeListGen(data1){

	if (data1 != null && typeof data1 == 'object') {
	
	
	
		/**
	 * creates group bar
	 */
		if (data1.groups != null) {
			$j(treeSelector).html(""); //removes if there any loading message.
			$j(treeSelector).append("<ul class='filetree'></ul>");
			var groupnodes = "";
			//for (uik in data1.groups) {
			for (iab = 0; iab < data1.groups.length; iab++) {
				uik = data1.groups[iab].key;
				groupname = data1.groups[iab].value;
				var htmlid = "root_task_" + uik;
				var stylecode = (ie_groupColor[uik] != null && ie_groupColor[uik] != '') ? " style=' background-image:none;background-color:" + ie_groupColor[uik] + "'" : "";
				var nodehtm = "<div class='groupBar' groupTarget='" + uik + "' id='" + htmlid + "' " + stylecode + " ><div style='display:inline'>";
				//nodehtm += "<img src='" + scd_groupIcons[uik] + "' border='0'>";

				nodehtm += "</div><div style='display:inline;vertical-align:top;' class='label_groupname'><b>" + groupname + "</b></div>";				
				nodehtm += "<div style=' display:inline;float:right'>";				
				nodehtm += "<input type='button'  class='groupBarBtn'  group_uid='" + uik + "' taskname='" + groupname + "' value='New Folder' onclick='ie_ctxCreateFolder(\"" + uik + "\")'><span style='margin-left:20px;'><img src='images/draghandle.png' class='dragholder' style='cursor:move'></span></div>";
				nodehtm += "</div>";
				var spn = "<span class='folder'>" + groupname + "</span>";
				var ulgrpid = "ul_groupname_" + uik;
				groupnodes += "<li class='closed' groupname='" + uik + "'>" + nodehtm + "<ul id='" + ulgrpid + "' groupname='" + uik + "'></ul></li>";
				
			}
			groupnodes += "";
			$j(treeSelector + " ul.filetree").append(groupnodes);
		}
		
 
		var idGen=function(folder,element,prefix){
			 
			folder=folder.replace(/-|\//g, "-");
			folder=folder.replace(/ /g,"_");
			folder=element+"_"+prefix+"_"+folder;	
			return folder;
		}
		var folderULs=new Array(); 
		if (data1.folders != null) {				
			for (ibc = 0; ibc < data1.folders.length; ibc++) {				
					var group_uid=data1.folders[ibc].group_uid;
					var folderpath=data1.folders[ibc].folder_name;
					var uniqfolder=data1.folders[ibc].group_uid+"_"+folderpath;
											
					if (folderpath.indexOf("/")>=0  ) {
																				
						var fsplit = folderpath.split("/");
						var current=fsplit[fsplit.length-1];
						var parent= folderpath.replace("/"+current,"");
							if ($j('span[uniquefid="' + uniqfolder + '"]').length == 0) {
						    var ulid=idGen(folderpath,'ul',group_uid);
							var liid=idGen(folderpath,'li',group_uid);
							var folder = '<li id="'+liid+'" class="closed" folderitem="yes" foldername="' + folderpath + '" group_uid="' + group_uid + '"><span uniquefid="' + uniqfolder + '" class="folder">' + current + '</span><ul id="'+ulid+'" foldername="' + data1.folders[ibc].folder_name + '"  folderid="' + data1.folders[ibc].id + '"></ul></li>';								
							folderULs[data1.folders[ibc].id]=ulid;
							var pulid=idGen(parent,'ul',group_uid);
							$j('#'+pulid).append(folder);
						}
						
					}else {
						if ($j('span[uniquefid="' + uniqfolder + '"]').length == 0) {	
						 	var ulid=idGen(folderpath,'ul',group_uid);
							var liid=idGen(folderpath,'li',group_uid);																		
							var folder='<li id="'+liid+'" folderitem="yes" class="closed" foldername="' + folderpath + '" group_uid="' + group_uid + '"><span class="folder" uniquefid="'+uniqfolder+'" >'+data1.folders[ibc].folder_name+'</span><ul id="'+ulid+'" foldername="'+data1.folders[ibc].folder_name+'" folderid="'+data1.folders[ibc].id+'"></ul></li>';
							folderULs[data1.folders[ibc].id]=ulid;																	
							var ulgrpid="ul_groupname_"+group_uid;
							$j('#'+ulgrpid).append(folder);
						}
							
					}
				}
		}
	 
		
		
		if(data1.strategies!=null ){
			
				for(iac=0;iac<data1.strategies.length;iac++){
													
							if ($j.inArray(data1.strategies[iac], ie_strategies) == -1) {
								ie_strategies[ie_strategies.length] = data1.strategies[iac];
							}
						
							var sc_id=data1.strategies[iac].id;						
 					
							 
							var spwidth="352px";
							
							 
							var folderid=data1.strategies[iac].folder_id;
							
							var htmlid="strategy_id_"+sc_id;

						    var oldclass="treeStrategyItem ";
							
							var domhtmlid= $j(' #' + htmlid);							
							if (domhtmlid.length > 0) {
								oldclass=domhtmlid.attr("class");
							}
							
					 		var nodehtml="<div  class='"+oldclass+"' folderid='"+folderid+"' id='"+htmlid+"' >";
							
							var ie_name=$j.trim(data1.strategies[iac].strategy_name);
							ie_name=(ie_name.length>=55) ? ie_name.substring(0,55)+"...":ie_name;
																				
							nodehtml+="<span class='file'>"+ie_name+"</span>";					
							nodehtml+="<div style='float:right;margin-right:7px'>";
							if(data1.strategies[iac].parent_strategy==null || (data1.strategies[iac].parent_strategy!=null && data1.strategies[iac].parent_strategy=='')){
								nodehtml+="<a href='#' onclick='ie_createChildStg(this)' strategy_id='"+sc_id+"' strategy_name='"+data1.strategies[iac].strategy_name+"'><img src='images/add_child_strategy.png'  style='cursor:pointer'></a>";
							}
							nodehtml+="<span style='margin-right:10px;margin-left:10px'><img src='images/draghandle.png' class='dragholder' style='cursor:move'></span>";		 					
							nodehtml+="<input type='button' class='ie_itembtn edit' flag='edit'  strategy_id='"+sc_id+"' strategy_name='"+data1.strategies[iac].strategy_name+"' value='Edit' onclick='ie_edit(this)'>";
							
			 
							nodehtml+="</div></div>";							
							var item="<li taskitem='yes' strategy_name='"+data1.strategies[iac].strategy_name+"' strategy_id='"+sc_id+"'>"+nodehtml+"</li>";
							

							var oldfid=domhtmlid.attr("folderid");							
							
							if(domhtmlid.length > 0 && oldfid==folderid){
									domhtmlid.parent().replaceWith(item);
							}else{
								domhtmlid.parent().remove();
								//if (folderid == null || (folderid != null && folderid == 0)) {
								//	var ulgrpid="ul_groupname_"+group_uid;
								//	$j("#"+ulgrpid).append(item);
								//}else {
									 
									 if(data1.strategies[iac].parent_strategy!=null && data1.strategies[iac].parent_strategy!=''){
									 	var selector=treeSelector + ' li[strategy_name="'+data1.strategies[iac].parent_strategy+'"][taskitem="yes"]';
									 	if($j(selector).find("ul[parent_strategy]").length>0){}else{										
											var sub="<ul parent_strategy='"+data1.strategies[iac].parent_strategy+"'></ul>";
											$j(selector).append(sub);
										}
										$j(selector+" ul[parent_strategy]").append(item);
										$j(selector).attr("has_child","yes");
										$j(selector+"[has_child='yes'] > .treeStrategyItem > span.file").removeClass("file");
										$j(selector+"[has_child='yes'] > .treeStrategyItem > span").addClass("filefolder");
										
									 }else{
										foldeulid=folderULs[folderid];
										if (foldeulid != null) {$j('#' + foldeulid).append(item);}
										else{$j(treeSelector + ' ul.filetree ul[folderid="' + folderid + '"]').append(item);}									 	
									 }
									
									
								//}

							}
							 
					  
				}	
		}
			
		
		
		
		
		if (!treeMenuRendered) {
			jqtree = $j(treeSelector + " ul.filetree").treeview({
				animated: 100,
				unique: false,
				persist: "cookie",
				cookieId: "navigationtree"
			
			});
			
			
			
			var menu2 = [
		
				  {'New Strategy':{onclick:function(menuItem,menu) {				  						 
					 	var tobj=this;
				  	 	var folderid=$j(this).parent().children(" ul[folderid]").attr('folderid');
						setTimeout(function(){ie_createStartegy(folderid);},100);
						
 
				  	},
				  	icon:'images/menu_newfunc.png'} 
				  },
				  $j.contextMenu.separator,				   
				  {'Rename':{onclick:function(menuItem,menu) {
					 if (confirm("Renaming will reload this page and unsaved data on this page will lost, Are you still want to do rename now?")) {
					 	var tobj=this;
				  	 	var folderid=$j(this).parent().children(" ul[folderid]").attr('folderid'); 
						var cfolder=$j(this).text();
						
						setTimeout(function(){
								var newname=prompt("Folder Name:",cfolder);
								if(newname==cfolder){alert("Not renamed")}
								else if(newname!=null && newname!='' && newname!=cfolder){
									//RFunctionMgmt.renameFolder(parseInt(folderid),$j.trim(newname),function(data){location.reload(true);});
								}
						},100);
					 	
						
					 }
				  	},
				  	icon:'images/menu_renamefunc.png'} 
				  }
			];

			var mOptions={
					theme:'vista',
					showSpeed:100, hideSpeed:100, showTransition:'fadeIn', hideTransition:'fadeOut', 
					beforeShow: function() {
						return true; 
					}, 
					showCallback:function(){						 
						if(prevCtxClk){
							$j(prevCtxClk).removeClass("treefldrHld");
						}
						$j(this.target).addClass("treefldrHld");
						prevCtxClk=this.target;
					},
					hideCallback:function(){
						$j(this.target).removeClass("treefldrHld");
					}
			}
			$j(treeSelector +' li[folderitem="yes"]:not([group_uid="trash"])').children("span.folder").contextMenu(menu2,mOptions);
				//$j(treeSelector +' li[folderitem="yes"][emptyfolder="yes"]').children("span.folder").contextMenu(menu3,mOptions);
				 
			
			
			//group re-arrangement...
			$j(treeSelector + ' ul.treeview').sortable({
				 	stop:function(event,ui){
						   var items=$j(treeSelector + ' ul.treeview li[groupname]');
						   var groupids=new Array();
						   for(iab=0;iab<items.length;iab++){						   	   
							   groupids[groupids.length]=$j(items[iab]).attr("groupname");
						   }
						   progress_message("Please wait...");
						   IExecMgmt.setGroupOrder(groupids,function(){progress_message()});
						   //console.debug("array:"+taskuids);
					},
					handle:'img.dragholder',
				}
			);
			
			
			
			//enable draggable feature for strategy name
				$j(treeSelector + '  li[folderitem="yes"]:not([group_uid="trash"]) li[taskitem="yes"]').draggable({
					revert: true,
					//handle:"div.treeFunctionItem",
					handle:'img.dragholder',
 				
				});
				
				
				
				//accept dragged strategy into the folder
				$j(treeSelector + ' li:not([group_uid="trash"])[foldername][folderitem="yes"]').droppable({					
					hoverClass:"ui-droppable-active",
					accept: 'li[taskitem="yes"]', 
					drop: function(event, ui){
						
						var tobj=this;
						var sobj=ui.draggable[0];
						var sel = $j(tobj).children("ul[folderid]");	
						var cb = function(data){
							progress_message();	
							if (data) {								
								message_status("Strategy has been moved successfully");
								sel.append($j(sobj));								  
							}
						}
						//console.log("on drop called");
						var new_folder_id=$j(sel).attr("folderid");
						var func_id=$j(sobj).attr("strategy_id");
 
						progress_message("Please wait...");
						IExecMgmt.moveFile2Folder(parseInt(func_id),parseInt(new_folder_id),cb);
 
					}
				});
				
			
			
			
			//enable folder draggable 				
				$j(treeSelector + ' li[foldername][folderitem="yes"]:not([group_uid="trash"])').draggable({
					revert: true,
					handle:"span.folder",
				});
				
				
				//accept dragged folder into group
				 $j(treeSelector + ' div[grouptarget]:not([grouptarget="trash"])').droppable({
					hoverClass: "ui-state-active",				
					accept: 'li[foldername][folderitem="yes"]',
					drop: function(event, ui){
						var tobj=this;
						var sobj=ui.draggable;
						var cb = function(data){
							if (data) {
								message_status("Folder has been moved successfully");
								$j(treeSelector + ' ul.filetree  li[groupname="' + $j(tobj).attr("grouptarget") + '"] ul[groupname="' + $j(tobj).attr("grouptarget") + '"]').append(sobj);
							}
						}
						var groupuid=$j(tobj).attr("grouptarget");
						var folder_id=$j(sobj).children("ul[folderid]").attr("folderid");
						
						progress_message("Please wait...");						
						IExecMgmt.moveFolder(parseInt(folder_id),groupuid,cb);
					
					}
				});
				
				
				
				$j(treeSelector+' ul[groupname]:not([groupname="trash"]) .treeStrategyItem >span').inlineEdit({
			    buttons:'<button class="save ie_itembtn">Rename</button> <button class="cancel ie_itembtn">cancel</button>',
			    save: function(el, data) {
			      //return confirm('Change name to '+ data.value +'?');
				  if(confirm("Are you sure you wish to rename this function ?")){
				  	  //var foldid=$j(this).parent().attr("folderid");
					var funid=$j(this).parent().attr("id");
					funid=funid.substring("strategy_id_".length);
					var respBack=function(data){
						progress_message();
						if (data != null && data) {
							$j('a[href="#ie_tabs-' + funid + '"]').html(data.value);
						}
					}
					progress_message("Please wait while renaming...");			  
					IExecMgmt.renameStrategy(data.value,parseInt(funid),respBack)			  
					  
				  }
			    }
			   });
			   
			
		} //(!treeMenuRendered)
		
		if (!treeMenuRendered) {
		   	$j("#ie_searchStrategy input").autoSuggest(ie_strategies, {
		   		startText: "Type Strategy Names",
		   		selectedItemProp: "strategy_name",
		   		searchObjProps: "strategy_name",
		   		selectedValuesProp: "id"
		   	});
		}
		treeMenuRendered=true;
		   
	}		
				
			 
}

var currentTabCotent=null;
var ie_EditorTyped=new Array();
var ie_refreshParameter=new Array();

function ie_typeDetected(d){
	//console.log("t:"+d); 
	ie_EditorTyped[ie_getActivePanelID()]=true;
}



function ie_edit(tobj,data) {
	if (data == null) {	
		var ie_id = $j(tobj).attr("strategy_id");
		var ie_name = $j(tobj).attr("strategy_name");
		var flag = $j(tobj).attr("flag"); 
	}else{
		var ie_id = data.data.id;
		var ie_name = data.data.strategy_name		
	}
	//alert(fn_id);
	var respBack=function(data,editrefresh){
		
		if(editrefresh==null){	
		   editrefresh=false; //by default 
		}
		progress_message();
		 

		ie_EditorTyped[ie_id]=false;

		if(data!=null ){
			//console.log(data);			
			 
			var toolbar1="reditor_save";
			var plugins1="iexec";
			var lockedMsg=null;
			
			if(data.isAuthorized!=null && data.isAuthorized){				
				toolbar1="reditor_save,file_open,reditor_delete";
				plugins1="iexec";
			}else{
				message_status("Readonly Access: Accessing from unknown computer, you can't modify this strategy");
				toolbar1="ReadOnly";
				plugins1="reditorlite";
			}
			if(data.data.lockedby!=null){
				if (data.authorizedUser == data.data.lockedby) {
					plugins1 = "iexec";					
					toolbar1="reditor_save,file_open,reditor_delete";					
				}else{
					toolbar1="reditor_lock1";
					plugins1 = "reditorlite";
					lockedMsg="Currently Edited by "+data.data.lockedby;
				}				
				
			}
			if(typeof data.readonly=='boolean' && data.readonly){
				toolbar1="reditor_lock1";
				plugins1 = "reditorlite";
			}
			
			var readonly="false";
			if (plugins1 == 'reditorlite') {
				readonly="true";
			}
			
			
 
			var htm='<div class="ie_lockedBy" id="ie_lockedBy_'+ie_id+'"></div><div id="ie_stabs_'+ie_id+'" readonly="'+readonly+'" class="ie_subtab">';
			htm+='	<ul>';
			if(data.data.parent_strategy!=null && data.data.parent_strategy!='') {}else{
				htm+='			<li><a  href="#ie_stabs1_'+ie_id+'">XML</a></li>';
			}
			htm+='			<li><a href="#ie_stabs2_'+ie_id+'">Parameters</a></li>';
            if(data.data.parent_strategy!=null && data.data.parent_strategy!='') {}else{		
			  htm+='			<li><a href="#ie_stabs3_'+ie_id+'">Revisions</a></li>';
			}
			htm+='			<li><a href="#ie_stabs4_'+ie_id+'">Wiki</a></li>';
			htm+='		</ul>';
			
			if(data.data.parent_strategy!=null && data.data.parent_strategy!='') {}else{	
			   htm+='		<div id="ie_stabs1_'+ie_id+'">';				
			   htm+='			<textarea id="ie_editor_area_'+ie_id+'" name="content" style="width:100%; height:88%;overflow:auto;"></textarea>';
			   htm+='		</div>';
			}
			htm+='		<div id="ie_stabs2_'+ie_id+'">';			 
			htm+='		  <div class="parameterTB"><input type="button" panel_id="ie_stabs2_'+ie_id+'" ie_name="'+ie_name+'" onclick="ie_saveParameters(this)" value="Save Parameters">  ';
			if(data.data.parent_strategy!=null && data.data.parent_strategy!='') {}else{
				htm+='		    <input type="button" panel_id="ie_stabs2_'+ie_id+'" ie_name="'+ie_name+'" onclick="ie_addContract(this)" value="Add Contract">';
			}
			htm+='		  </div> ';
			htm+='		  <div id="ie_stabs2_'+ie_id+'_para" class="para_tab"><ul></ul></div>	';		
 	 	    htm+='		</div>';
			
			if(data.data.parent_strategy!=null && data.data.parent_strategy!='') {}else{		
			  htm+='		<div id="ie_stabs3_'+ie_id+'">';
			  htm+='        <div class="rev_panel" style="height:89%"></div>';			
			  htm+='		</div>';
		    }
  			htm+='		<div id="ie_stabs4_'+ie_id+'">';
			htm+='        <div class="wiki_panel" style="height:89%"></div>';			
			htm+='		</div>';

			htm+='</div>';			
			
			if (!editrefresh) {
				currentTabCotent=htm;
				ie_editorTab.tabs("add", "#ie_tabs-" + ie_id, ie_name);
				var ie_name11 = ie_name;
				$j("#ie_stabs_" + ie_id).tabs({
					selected: 0,
					show:function(event,ui){
						if(ie_refreshParameter[ie_getActivePanelID()]!=null && ie_refreshParameter[ie_getActivePanelID()] ){
							//$j("#ie_stabs2_"+ie_id+" input.refresh").show();
							//if(confirm("XML file has been modified, would you like to refresh this tab?")){								
								setTimeout("ie_refereshParaTab('"+ie_name+"','"+ui.panel.id+"')",100);
							//}
						}
					},
					select: function(event, ui){
						//ui.index
						//$j("#ie_stabs2_"+ie_id+" input.refresh").hide();
						
						if (typeof ui.panel.isLoaded == 'undefined' && $j(ui.panel).find(".parameterTB").length>0 && !$j(ui.panel).parent().hasClass("child_strategy")) {  
						
							ui.panel.isLoaded = true;
							var arry=data.contracts;//["Default","ABCCC","abbb","agdfdf","sfss"];
							for (ib = 0; ib < arry.length; ib++) {
								var pm=(ib == arry.length - 1)? "progress_message()":"progress_message('please wait..')"; 
								  if (data.element_ph_found != null && data.element_ph_found) {
								  	var noRichEditor=false;
								  	setTimeout("ie_getParameterSetup('" + ie_name11 + "','" + ui.panel.id + "','" + arry[ib] + "',false);" + pm + ";", ib * 1500);
								  }else{
								  	var noRichEditor=true;
								  	ie_getParameterSetup(ie_name11 ,ui.panel.id ,arry[ib],noRichEditor);
								  }
								  ie_refreshParameter[ie_getActivePanelID()]=null;
								//ie_getParameterSetup(ie_name11, ui.panel.id, "ABCCC");
							}
							
						
						}
						if (typeof ui.panel.isLoaded == 'undefined' && $j(ui.panel).find(".rev_panel").length>0){  							 
							ie_getRevision(ie_name11, ui.panel.id);
							ui.panel.isLoaded = true;
						}
						if(typeof ui.panel.isLoaded=='undefined' && $j(ui.panel).find(".wiki_panel").length>0) {
							//alert("wiki clicked");
							ie_getWiki(ie_name11, ui.panel.id);
							ui.panel.isLoaded = true;
						}
					
					}
				});
				if(typeof data.isParent!='undefined' && data.isParent ) {
						//$j("#ie_stabs_" + ie_id).attr("parent_strategy","true");
						$j("#ie_stabs2_" + ie_id).attr("parent_strategy","true");
				}
				
				//incase of child strategy.
				if(data.data.parent_strategy!=null && data.data.parent_strategy!='' ) {	
					  $j("#ie_stabs_" + ie_id).addClass("child_strategy");				 
					  var noRichEditor=(data.element_ph_found != null && data.element_ph_found)?false:true;
					  ie_getParameterSetup(ie_name11 ,"ie_stabs2_" + ie_id,data.contracts[0],noRichEditor, ie_id );
				}
				
			}else{
				$j("#ie_stabs_"+ie_id).attr("readonly",readonly+"");
				//$j("#ie_stabs1_"+ie_id).html('<textarea id="ie_editor_area_'+ie_id+'" name="content" style="width:100%; height:88%;overflow:auto"></textarea>');
				editAreaLoader.delete_instance('ie_editor_area_'+ie_id);
				$j("#ie_stabs_" + ie_id).tabs({selected:0});				
			}

			
            if(data.data.parent_strategy!=null && data.data.parent_strategy!='') {}else{	
 	
	 		   if(lockedMsg!=null){
					$j("#ie_lockedBy_"+ie_id).html(lockedMsg);
					$j("#ie_lockedBy_"+ie_id).addClass("showOn");
				}
			
				var ea_id="ie_editor_area_"+ie_id;
				$j("#"+ea_id).val(data.content);
				var isEditable=(plugins1=='reditorlite')?false:true;
				areaEditOptions[ie_id]={
						id: ea_id // textarea id
						,syntax: "xml" // syntax to be uses for highgliting
						,start_highlight: true // to display with highlight mode on start-up
						//,toolbar: "save, |, search, go_to_line, undo, redo, |, select_font, |, highlight, reset_highlight, | , reditor_save"
						,toolbar:toolbar1
						,allow_toggle: false
						//,font_family: "monospace"
						//,font_size: 11
						,allow_resize: "both"
						,plugins:plugins1
						,font_size:9
						,font_family: "verdana, monospace"
						,is_editable:(plugins1=='reditorlite')?false:true
					    ,save_callback:"ie_saveXML"
					     
				};
				
				
				editAreaLoader.init(areaEditOptions[ie_id]);
				editor_initiated=true;			
				//editAreaLoader.setValue("ie_editor_area_"+ie_id,data.content);
			
			}			
			if(ie_editorTab.length==1){
				  ie_highLightFunc(ie_id);
			}
			
			//if(data.path!=null){
			//	setTimeout(function(){ie_editorSetPath(ea_id,data.path)},1000);
			//}
		}
	}
	
	
	 
	if(data!=null){
		respBack(data);
		return;
	}
	
	
	var readonly=true;
	if(flag!=null && flag=='edit'){
			var readonly=false;
	}
	if ($j("#ie_tabs-" + ie_id).length > 0) {
		processTb = false;		 
		var index=$j('#ie_tabs a[href="#ie_tabs-'+  ie_id+'"]').parent().index()
		ie_editorTab.tabs('select', index);		
		IExecMgmt.getStrategy(parseInt(ie_id),readonly, function(data){
			respBack(data,true);
		});		
	}else {
		progress_message("Please wait...");
		IExecMgmt.getStrategy(parseInt(ie_id),readonly, respBack);
	}
}





function ie_createStartegy(s_folder_id) {
	
	var name=prompt("Enter function name?");
	if (name != null && name!='') {
		var respBack=function(data){
			progress_message();
			if(data!=null){
				ie_treeListGen(data);
				ie_edit(0,data.strategy_data);
			}
		}
		progress_message("Please wait...");
		IExecMgmt.createStrategy(s_folder_id,name,"",respBack);
		//RFunctionMgmt.createRFunction(s_folder_id,name,"",respBack);
	}
	
	
}

function ie_createChildStg(thisobj) {
	
	var name=prompt("Enter function name?");
	if (name != null && name!='') {
		var respBack=function(data){
			progress_message();
			if(data!=null){
				ie_treeListGen(data);
				ie_edit(0,data.strategy_data);
			}
		}
		progress_message("Please wait...");
		var p_stname=$j(thisobj).attr("strategy_name");
		var p_stid=$j(thisobj).attr("strategy_id");
		IExecMgmt.createChildStrategy(p_stid,name,respBack);
		//RFunctionMgmt.createRFunction(s_folder_id,name,"",respBack);
	}
	
	
}


function ie_ctxCreateFolder(group_id) {
	var foldername=prompt("Folder name:");
	if(foldername!=null && foldername!=''){
		IExecMgmt.createFolder(foldername,group_id,function(data){ie_treeListGen(data); message_status("Folder created, However you have to refresh this page to support drag & drop feature for new field")	});
		
	}
}

var comm_cont_tree=null;
function ie_addContract(tobj){
	
	var panel_id=$j(tobj).attr("panel_id");
	var ie_name=$j(tobj).attr("ie_name");
	//var contract= prompt("Cotract Name:")
	
	var respBack = function(data){
		progress_message();
		$j("#dialog_tree_comm").dialog({
			resizable: false,
			width: 400,
			height:500,
			modal: true,
			dialogClass: 'ie_dialog',
			buttons: {
				"Cancel": function(){
					$j(this).dialog("close");
				}
			}
		});
		if(data!=null){
			
			if(comm_cont_tree!=null){
				//comm_cont_tree.destroy();	
				$j("#dialog_tree_comm_ul").html("");
			}	
			var tabs_l=new Array();
			$j("#"+panel_id+"_para ul li a").each(function(idx,el){
    			tabs_l[tabs_l.length]=$j(el).text();
			});
			
			$j("#dialog_tree_comm_inp .inputbtn").attr("panel_id",panel_id);
			$j("#dialog_tree_comm_inp .inputbtn").attr("ie_name",ie_name);
			
		
			for(ib=0;ib<data.commodity.length;ib++){
				var ky=$j.trim(data.commodity[ib]);
				var sub='';				
				if (data.contract[ky] != null) {
					for (iab = 0; iab < data.contract[ky].length; iab++) {
						if ($j.inArray(data.contract[ky][iab], tabs_l) == -1) {
							sub += (sub == '') ? '<ul>' : '';
							sub += '<li><span class="file">'+data.contract[ky][iab]+'<a href="#" contract="' + data.contract[ky][iab] + '" panel_id="' + panel_id + '"  ie_name="' + ie_name + '" onclick="ie_addContractSele(this)"><img style="margin-left:5px" src="images/db_insert.png" border="0"></a></span></li>'
						}
					}
					sub += (sub != '') ? '</ul>' : '';					
					var htm = '<li><span class="folder">' + ky + '<a href="#" contract="' + ky + '" panel_id="' + panel_id + '"  ie_name="' + ie_name + '" onclick="ie_addContractSele(this)" ><img style="margin-left:5px" src="images/db_insert.png" border="0"></a></span>' + sub + '</li>';
					$j("#dialog_tree_comm_ul").append(htm);
				}				
			}
			comm_cont_tree=	$j("#dialog_tree_comm_ul").treeview({
				animated: 100,
				unique: false,
				persist: "cookie",
				cookieId: "comm_cont_tree"			
			});
		}
	}
	 
		progress_message("Please wait...");
		IExecMgmt.getContractComm(respBack);
	 
	
	//if(contract!=null && $j.trim(contract)!=''){
	//	ie_getParameterSetup(ie_name, panel_id,contract);
	//}
}

function ie_addContractSele(tobj){
	var panel_id=$j(tobj).attr("panel_id");
	var ie_name=$j(tobj).attr("ie_name");
	var contract=$j(tobj).attr("contract");
	
	if($j("#"+panel_id+"_"+contract+"_para").length>0){
		$j("#dialog_tree_comm").dialog("close");
		message_status(contract+" tab already added");		
		return false;
	}else{	
		$j("#dialog_tree_comm").dialog("close");
		if(contract!=null && $j.trim(contract)!=''){
			ie_getParameterSetup(ie_name, panel_id,contract);
		}
	}
	
}

function ie_addContractSeleBtn(tobj){
	var panel_id=$j("#dialog_tree_comm_inp .inputbtn").attr("panel_id");
	var ie_name=$j("#dialog_tree_comm_inp .inputbtn").attr("ie_name");
	var contract=$j("#dialog_tree_comm_inp .inputbox ").val();
	$j("#dialog_tree_comm").dialog("close");
	if(contract!=null && $j.trim(contract)!=''){
		ie_getParameterSetup(ie_name, panel_id,contract);
	}
	
}


function ie_refereshParaTab(ie_name,panel_id){
	
	if(!confirm("XML file has been modified, would you like to refresh this tab?")){
		return;					
	}
	
	var respBack=function(data){
		
		$j("#"+panel_id+ "_para").tabs("destroy");
		$j("#"+panel_id+ "_para").html("<ul></ul>");
		
		var arry=data.contracts; 
		for (ib = 0; ib < arry.length; ib++) {
			var pm=(ib == arry.length - 1)? "progress_message()":"progress_message('please wait..')"; 
			  if (data.element_ph_found != null && data.element_ph_found) {
			  	setTimeout("ie_getParameterSetup('" + ie_name + "','" + panel_id+ "','" + arry[ib] + "');" + pm + ";", ib * 1500);
			  }else{
			  	ie_getParameterSetup(ie_name ,panel_id ,arry[ib]);
			  }
			  ie_refreshParameter[ie_getActivePanelID()]=null;
		 
		}
		ie_refreshParameter[ie_getActivePanelID()]=null;					
	}
	
	var readonly=true;
	var ie_id=ie_getActivePanelID();
	IExecMgmt.getStrategy(parseInt(ie_id),!readonly, respBack);
	
}

function ie_saveParameters(tobj){
	var panel_id=$j(tobj).attr("panel_id");
	var ie_name=$j(tobj).attr("ie_name");
	var data=new Array();
	$j("#"+panel_id+" .para_tab .parameters_pane1 input[contract]").each(function(idx,el){
		var val=$j(el).val();
		//if (val != null && $j.trim(val) != '') {
			data[data.length]={strategy_name:ie_name,contract: $j(el).attr("contract"),value:val,placeholder:$j(el).attr("name")};
		//}
	});
	$j("#"+panel_id+" .para_tab .parameters_pane2 textarea[contract]").each(function(idx,el){
		//var val=editAreaLoader.getValue($j(el).attr("id"));
		var val=$j(el).val();
		//if(val==null){$j(el).val();}
		//if (val != null && $j.trim(val) != '') {
			data[data.length] = {strategy_name: ie_name,contract: $j(el).attr("contract"),value: val,placeholder:$j(el).attr("name")};
		//}
	});
	var respBack=function(data){
		progress_message();
		if(data!=null && data){
			message_status("Parameters are saved successfully");
		}
	}
	progress_message("Please wait while saving....");
	IExecMgmt.updateParameter(data,ie_name,respBack);
}

function ie_getParameterSetup(st_name, panel_id,contract,noRichEditor, parent_st_id){
	
	
	 var para_tab_cont="";
	 var ui_id=panel_id ;// +"_"+contract;	 
	 var ui_id_uq=panel_id+"_"+contract.replace(/\s/g, "");
	 var para_tab=$j( "#"+ui_id+"_para").tabs({
			tabTemplate: "<li><a href='#{href}'>#{label}</a> <span class='ui-icon ui-icon-close'>Remove Tab</span></li>",
			add: function( event, ui ) {	
			   // alert("add called");			 
				$j( ui.panel ).append(para_tab_cont);
				//this.tabs( "select", ui.index);
			},
			select: function(event, ui) { 
			   //alert(ui);
			   
			   var func_id=ui.panel.id;
			   if(func_id!=null) func_id=func_id.replace("ie_tabs-","");
			   //ie_highLightFunc(func_id);
			   setTimeout(ie_highLightFunc,200);
			   
			}
	});	
	
	if( $j("#"+ui_id).attr("parent_strategy")!=null && $j("#"+ui_id).attr("parent_strategy")=="true"){
		   para_tab_cont="<div class='parameters_lnk' style='border:0px solid #FF1100;padding:margin:5px;padding:10px'>[No Preview Available for Parent Strategy]</div>";
	}else{	
		if(parent_st_id!=null){
		   para_tab_cont="<div class='parameters_lnk'><a href='#' strategy='"+st_name+"' contract='"+contract+"' onclick='ie_openXMLPopPrompt(this)'>Parsed XML</a></div>";
	    }else{	    	
	    	para_tab_cont="<div class='parameters_lnk'><a href='#' strategy='"+st_name+"' contract='"+contract+"' onclick='ie_openXMLPop(this)'>Parsed XML</a></div>";
	    }
	 }
	
    if (noRichEditor != null && noRichEditor) {
		para_tab_cont+= '<div class="parameters_panelist"><div class="parameters_pane1"></div></div>';
		
	}else {
		para_tab_cont+= '<table width="100%" cellspacing="3" cellpadding="3" class="parameters_panetable"><tr><td width="400" valign="top"> <div class="parameters_pane1"></div></td><td width="*" valign="top"><div class="parameters_pane2"></td></tr></table>';
	}
	para_tab.tabs("add", "#"+ui_id_uq+"_para", contract);
	
	$j("#"+ui_id+"_para span.ui-icon-close" ).die();
		
	$j("#"+ui_id+"_para span.ui-icon-close" ).live( "click", function() {
 		var contract=$j(">a" ,$j( this ).parent()).html(); 
		var st_name=ie_getActivePanelTitle();		
		var index = $j( "> ul > li", para_tab ).index( $j( this ).parent() );
		 
		var respBack=function(success){
			message_status("Contract Deleted!");
			if(success!=null && success){
				para_tab.tabs( "remove", index );	
			}
		}
		if (confirm("Are you sure you wish to delete contract " + contract + "?")) {
			progress_message("Deleting Contract....");
			IExecMgmt.removeContract(st_name, contract, respBack);
		}
	});
	
		
	var ppane1="#"+ui_id_uq+"_para .parameters_pane1";
	var ppane2="#"+ui_id_uq+"_para  .parameters_pane2";

	var t1=$j.template('<div  pid="${name}" class="ei_para_box"><span class="ei_para_label ${childclass}">${label}:</span><span class="ei_para_value"> <input type="text" value="${value}" name="${name}" st_name="${st_name}" contract="${contract}"></span></div>');	
	var t2=$j.template('<div  pid="${name}" class="ei_para_box"><span class="ei_para_label ${childclass}">${label}:</span><span class="ei_para_value"> <textarea style="width:100%;height:170px" id="${uid}" name="${name}" st_name="${st_name}" contract="${contract}">${value}</textarea></span></div>');
	
	var respBack=function(data){
		
		var last = $j("#"+ui_id+"_para" ).tabs( "length" )
		$j( "#"+ui_id+"_para").tabs( "option", "selected", last-1);

		 
		if(data!=null){			 
			if(data.att_ph!=null){
				for(iab=0;iab<data.att_ph.length;iab++){
					var obj=new Object();
					obj.name=data.att_ph[iab];					
					obj.value=data.ph_data!=null && data.ph_data[obj.name]!=null ? data.ph_data[obj.name] :"";
					
					obj.label=data.att_ph[iab];
					obj.st_name=st_name;
					obj.contract=contract;
					//change child default parameters red color
					if(data.att_ph_c!=null && $j.inArray(obj.name,data.att_ph_c)>=0){
						obj.childclass="ei_para_labelchild"
					}else{
						obj.childclass="";
					}
					$j(ppane1).append( t1, obj);					
				}
			}
			if(data.ele_ph!=null){
				for(iab=0;iab<data.ele_ph.length;iab++){
					var obj=new Object();
					obj.name=data.ele_ph[iab];
					obj.value=data.ph_data!=null && data.ph_data[obj.name]!=null ? data.ph_data[obj.name] :"";
					obj.label=data.ele_ph[iab];
					obj.uid=randomString(10);
					obj.st_name=st_name;
					obj.contract=contract;

					if(data.ele_ph_c!=null && $j.inArray(obj.name,data.ele_ph_c)>=0){
						obj.childclass="ei_para_labelchild"
					}else{
						obj.childclass="";
					}

					
					$j(ppane2).append( t2, obj);	
					
					var arobj={
						id: obj.uid // textarea id
						,syntax: "xml" // syntax to be uses for highgliting
						,start_highlight: true // to display with highlight mode on start-up
						//,toolbar: "save, |, search, go_to_line, undo, redo, |, select_font, |, highlight, reset_highlight, | , reditor_save"
						,toolbar:""
						,allow_toggle: false
						//,font_family: "monospace"
						//,font_size: 11						
						,plugins:"execlite"
						,font_size:9
						,font_family: "verdana, monospace"
						,is_editable:true					    
					};			 
					try{
						editAreaLoader.init(arobj);
						editAreaLoader.setValue(obj.uid,obj.value);
					}catch(exc){
						//error
					}		
				}
			}
		}
	}
	
	//alert(ui_id+":"+ie_getActivePanelID());
	var st_id=ie_getActivePanelID();
	IExecMgmt.getPlaceHolder(parseInt(st_id),contract,respBack);
}



function ie_highLightFunc(func_id){


	$j(".ie_tabopen").removeClass("ie_tabopen");
	$j(".ie_tabactive").removeClass("ie_tabactive");
	
	
		//in case drag and drop file, the drop area will disappear in hidden tab.
	
	
	
	$j("#ie_tabs div[id^='ie_tabs-']").each(function(idx,elem){
		var this_id=$j(elem).attr("id");
		var fid=this_id.replace("ie_tabs-","");
		
		$j("#strategy_id_" + fid).addClass("ie_tabopen");		
		var readonly=$j("#ie_stabs_"+fid).attr("readonly");
		
		 
		

		if (!$j(elem).hasClass("ui-tabs-hide")) {
			func_id=fid;
			$j("#strategy_id_" + func_id).addClass("ie_tabactive");			
			if (frames["frame_ie_editor_area_" + func_id] != null) {
				$j(".qq-upload-drop-area", frames["frame_ie_editor_area_" + func_id].document).hide();
			}
					
			var group_id=$j(treeSelector + ' li[strategy_id="' + func_id + '"]').parent().parent('li[folderitem="yes"]').attr("group_uid");
			$j(treeSelector + ' li.expandable[groupname="'+group_id+'"]').children('div.hitarea').trigger('click');				
			$j(treeSelector + ' li[strategy_id="' + func_id + '"]').parent().parent('li.expandable[folderitem="yes"]').children('div.hitarea').trigger('click');
			setTimeout(function(){		
				$j("#ie_treeMenu").scrollTo("#strategy_id_" + func_id,700, {offset: {top:-410}} );
			},300);
		}	
				
	});
	
}
function ie_openXMLPop(thisobj){
	var strategy=$j(thisobj).attr("strategy");
	var contract=$j(thisobj).attr("contract");
	var result =window.open("schedulerAPI?method=strategyXML&strategy="+strategy+"&contract="+contract, "XML", "width=800,height=600,center:yes,toolbar=no,scrollbars=yes,menubar=no,location=no");
}
function ie_openXMLPopPrompt(thisobj){
	var strategy=$j(thisobj).attr("strategy");
	var contract=prompt("Contract");
	var result =window.open("schedulerAPI?method=strategyXML&strategy="+strategy+"&contract="+contract, "XML", "width=800,height=600,center:yes,toolbar=no,scrollbars=yes,menubar=no,location=no");
	
}



function ie_deleteStrategy(){
	
	if (confirm("Are you sure you wish to delete this function ?")) {
	  var st_id=ie_getActivePanelID();		
	  //var respBack = 
	  progress_message("Please wait while deleting function...");
	  IExecMgmt.deleteStrategy(parseInt(st_id),function(success){
	  	progress_message();
	  	if (typeof success == 'boolean' && success) {			
			message_status("Strategy has been deleted");
			var st_id = ie_getActivePanelID();
			var index = $j("li", ie_editorTab).index($j(this).parent());
			ie_editorTab.tabs("remove", index);			
			
			var node_s = $j(treeSelector + ' li[taskitem="yes"][strategy_id="' + st_id + '"]');
			$j(node_s).find(".ie_tabopen").removeClass("ie_tabopen");
			$j(treeSelector + ' ul#ul_trash_Trash').append(node_s);
		}
	  });
		
		
		
		
	}
	
}


function ie_saveXML (){
	var ie_id=ie_getActivePanelID();
	
		//rf_editorTab.tabs( "remove", index );
	//alert("save pressed");
	//alert(rf_getActivePanelID());
	 
	var script=editAreaLoader.getValue("ie_editor_area_"+ie_id);	
	
	
	 $j("#dialog_commit_msg").val();
	 
	 $j( "#dialog_commit" ).dialog({
			resizable: false,			 
			width:700,
			modal: true,		
			dialogClass: 'ie_dialog',		
			buttons: {
				"Cancel": function() {
				$j( this ).dialog( "close" );
				},	 				
				"Save": function() {
					progress_message("Please wait...");					
					//$j( "#dialog_commit").dialog( "close" );
					$j( this ).dialog( "close" );
					IExecMgmt.modifyStrategy(parseInt(ie_id),script,$j("#dialog_commit_msg").val(),function(data){						
					    progress_message();
						if(data!=null){								
							if(ie_EditorTyped[ie_id]!=null && ie_EditorTyped[ie_id]){ ie_refreshParameter[ie_id]=true;	}
							//ie_treeListGen(data);							
							message_status("XML saved successfully")};
							$j("#dialog_commit_msg").val("");
							ie_EditorTyped[ie_getActivePanelID()]=false;
						
						}
					);
					
					
				}
			},
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
		$j('#dialog_commit_msg').focus();
        
		
}



function ie_getRevision(ie_name,panel_id) {
	//alert(ie_name+":"+panel_id);
	
	var respBack = function(rdata){
		progress_message();
		if (rdata != null) {
			$j("#"+panel_id+" div.rev_panel").append('<ul class="ie_revisionList" style="padding:5px;"></ul>');
			//for (rev in rdata) {
			for (rev=0;rev<rdata.length;rev++) {
				//li="<li><b>Rev: "+rdata[rev].revision+"</b> &nbsp;&nbsp;&nbsp; By:"+rdata[rev].author+" &nbsp;&nbsp;&nbsp;<small>("+rdata[rev].date+")</small>";
				li = "<li>" + rdata[rev].author + " &nbsp;&nbsp;&nbsp;<small>(" + rdata[rev].date + ")</small>";
				li+="<div class='message'>"+rdata[rev].message+"</div>";
				li += "<div style='float:right;vertical-align:top;margin-top:-1' class='svn_link_btn'>";				
				li += "<a href='#' onclick='ie_showRev(this,false); return false;' strategy_name='" + rdata[rev].strategy_name + "' path='" + rdata[rev].path + "' compare='yes' revision='" + rdata[rev].revision + "' >Diff</a>";
				//li += "<a  href='push_svndiff.jsp?scheduler_id=" + rdata[rev].scheduler_id + "&revision=" + rdata[rev].revision + "'><img src='images/external_link.gif' border='0'></a>";
				li += "<a href='#' onclick='ie_showRev(this,true); return false;' strategy_name='" + rdata[rev].strategy_name+ "'  path='" + rdata[rev].path + "'  revision='" + rdata[rev].revision + "' code='yes'  >Show Code</a>";
				li += "</div><div class='ie_inline_editor' style='display:none'></div>";
				
				$j("#"+panel_id+" div.rev_panel ul.ie_revisionList").append(li);
			}
		}
	}
	progress_message("please wait....");
	IExecMgmt.getSVNLogs(ie_name,respBack);
}

function ie_showRev(ancobj,isCode) {
	
	
	var fn=$j(ancobj).attr("strategy_name");
	var rev=$j(ancobj).attr("revision");
	var path=$j(ancobj).attr("path");
	
	var area= $j(ancobj).parent().parent().find("div.ie_inline_editor");
	if ($j(ancobj).text() == 'Hide Code') {
	  	$j(area).html("");
		$j(ancobj).html("Show Code");
		$j(ancobj).removeClass("lnkselected");
	  	$j(area).hide();
		return ;
	}
	
	if ($j(ancobj).text() == 'Hide Diff') {
	  	$j(area).html("");
		$j(ancobj).html("Diff");
		$j(ancobj).removeClass("lnkselected");
	  	$j(area).hide();
		return ;
	}
	
	//var isScript=true;
	var respBack=function(data){
		progress_message();
		if(data!=null){
			$j(ancobj).html(isCode?"Hide Code":"Hide Diff");
			$j(ancobj).addClass("lnkselected");			
			 if(isCode){
			 	$j(ancobj).parent().find("a[compare='yes']").html("Diff");
				$j(ancobj).parent().find("a[compare='yes']").removeClass("lnkselected");			 	
			 }else{
			 	$j(ancobj).parent().find("a[code='yes']").html("Show Code");			 	
				$j(ancobj).parent().find("a[code='yes']").removeClass("lnkselected");
			 }
			
			var areaid='rev_editor_'+fn+'_'+rev;
			var html="<textarea id='"+areaid+"' readonly='readonly' style='width:100%;height:430px;font-size:1.1em;'><textarea>";
			$j(area).show();
			$j(area).html(html);
			$j(area).find("textarea#"+areaid).val(data);
			//disabled because it shows junk characters on it.			
			if (isCode) {
				editAreaLoader.init({
					id: areaid // textarea id
					,
					syntax: isCode ? "xml" : "rdiff" // syntax to be uses for highgliting
					,
					start_highlight: true // to display with highlight mode on start-up
					//,toolbar: "save, |, search, go_to_line, undo, redo, |, select_font, |, highlight, reset_highlight, | , reditor_save"
					,
					toolbar: "|",
					allow_toggle: false,
					//font_family: "monospace",
					//font_size: 11,
					font_size:8,
					font_family: "verdana, monospace",
					allow_resize: "both",
					plugins: "iexeclite",
					is_editable:false
				
				});
			}
			editAreaLoader.setValue(areaid,data);
			 
		}
	}
	progress_message("please wait...");
	IExecMgmt.getScriptRev(fn,rev,isCode,path,respBack);
	
}

var tempOpenData=null;
function ie_search(){
	var rtn="";
	$j("#ie_searchStrategy li.as-selection-item").each(function(){
		var val=$j(this).html().replace('<a class="as-close">×</a>','');
		rtn+=(rtn=="")?val:","+val;		
	})
	
	
	if (rtn != '') {		
       
		
		var respBack=function(data){		 

			progress_message();
			if(data!=null){
				tempOpenData=data;				
				for(ibc=0;ibc<data.length;ibc++){
					var thisdata=data[ibc];
					var fn_id=thisdata.data.id;
					if ($j("#ie_tabs-" + fn_id).length > 0) {														
						var index=$j('#ie_tabs a[href="#ie_tabs-'+  fn_id+'"]').parent().index()
						rf_editorTab.tabs('select', index);		
					}else{											
						setTimeout("ie_edit(0,tempOpenData["+ibc+"])",(ibc*1500));						
					}
				}
				$j("#ie_searchStrategy li.as-selection-item").remove();
				$j("#ie_searchStrategy li.as-original input.as-values").val('');
				
			}
		}
	    progress_message("Please wait while opening functions...");
		var readonly=false;
		IExecMgmt.getStrategies(rtn,readonly,respBack);
	
	}else {
		alert("Strategy name is not selected, minimum 1 strategy required");
	}
}

 
function ie_getActivePanelID(){
	var activepanel=ie_editorTab.data().tabs.panels[ie_editorTab.tabs("option","selected")];
	var ie_id=$j(activepanel).attr("id")
	ie_id=ie_id.replace("ie_tabs-","");
	return ie_id; 
}

function ie_getActivePanelTitle(){
	var activepanel=ie_editorTab.data().tabs.panels[ie_editorTab.tabs("option","selected")];
	var ie_id=$j(activepanel).attr("id")
	var title=$j("a[href='#"+ie_id+"']",ie_editorTab).html();
	return title;
	//ie_id=ie_id.replace("ie_tabs-","");
	//return ie_id; 
}




function ie_getWiki(fn_name11,panel_id,exeCallBk){
	
	var respBack = function(rdata){
		progress_message();
		if (rdata != null) {
			
			$j("#"+panel_id+" div.wiki_panel").html('<div class="strategy_wiki"><div class="wiki_tb"><input  function_name="'+fn_name11+'" panel_id="'+panel_id+'" type="button" value="Edit This Wiki" onclick="wiki_inlineEditorObj(this)"></div>'+rdata+'</div>');
			
			if($j("#"+panel_id+" div.wiki_panel .strategy_wiki").text().trim().indexOf("There is currently no text in this page, you can search for this page title in other pages or edit this page")>=0){
			//no page found for this function.
				if($j("#"+panel_id+" div.wiki_panel .strategy_wiki a.external.text").html()=='edit this page'){
					var link=$j("#"+panel_id+" div.wiki_panel .strategy_wiki a.external.text").attr("href");
					//alert(link);
					$j("#"+panel_id+" div.wiki_panel").html('<div class="strategy_wiki"><center><div class="create_wiki">No documentation found for this function <a href="#">Add Wiki</a></div></center></div>');
					$j("#"+panel_id+" div.wiki_panel .strategy_wiki .create_wiki a").bind('click',function(){
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
						//wiki_inlineEditor($j(this).attr('href'),fn_name11,panel_id);
						
						return false;
				});	
				
				//replacing images...				
				$j("#"+panel_id+" div.wiki_panel img[src^='/']").each(function(){
					$j(this).attr("src","https://wiki.alphien.com"+$j(this).attr("src"));
				});
			}
			
		}else{
			$j("#"+panel_id+" div.wiki_panel").append('<div class="strategy_wiki"><h3>No Information found on Wiki about this Strategy</h3></div>');
		}
		if(exeCallBk!=null){
			exeCallBk.call(this);
		}
	}
	progress_message("please wait....");
	IExecMgmt.getStrategyWiki(fn_name11,respBack);
}

function wiki_help(lnk_p){

    var lnk="IExec";
	if(lnk_p!=null) lnk=lnk_p;
	var func = function(){
		$j("#wikiDialog").dialog({
			resizable: false,
			width: 1200,
			modal: true,
			title:"Help: iExec",
			dialogClass: 'ie_dialog ie_wikidialog'
		});
	}
	$j("#wikiDialogBdy").html("<div class='wiki_panel'></div>");
	ie_getWiki(lnk,"wikiDialogBdy",func);
}





function wiki_inlineEditorObj(thisobj){
	var func_name=$j(thisobj).attr("function_name")
	var panel_id=$j(thisobj).attr("panel_id");
	wiki_inlineEditor(func_name,panel_id);
}
function wiki_inlineEditor(function_name,panel_id){
	var url1="https://wiki.alphien.com/alphienwiki/index.php?title="+function_name+"&action=edit";
	$j("#"+panel_id+" div.wiki_panel").html('<div class="function_wiki_editor"><div class="wiki_tb"><input function_name="'+function_name+'" panel_id="'+panel_id+'" type="button" value="Finished Editing" onclick="ie_closeWikiEditor(this)"></div></div>');
	$j("#"+panel_id+" div.wiki_panel").append('<div class="wiki_editor"><iframe width="100%" height="94%" src="'+url1+'" scrolling="yes"></iframe></div>');	
}

function ie_closeWikiEditor(thisobj){
	var func_name=$j(thisobj).attr("function_name")
	var panel_id=$j(thisobj).attr("panel_id");
	ie_getWiki(func_name,panel_id);
}
 


/**
 * This function is used to show progress message while 
 * communicating to the server
 * @param {Object} msg
 */
function progress_message(msg,doc){
	
	 if (msg != null) {
	 	if(doc==null){
			doc=document;
		}
	 	var img     = doc.createElement('img');
      	img.setAttribute('src', 'images/loading.gif');
		img.style.width="auto";
		img.style.height="auto";
		var spn=doc.createElement('span');
		spn.appendChild(doc.createTextNode(msg));
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


function ie_toggleTree(){
			$j(".folder_view").toggleClass( "hide", 50);
			return false;
	
}


/**
 * To display status message on screen after the server responded for client request 
 * @param {Object} msg
 */
function message_status(msg){
	
	 if (msg != null) {
 		progress_message(null);
		var span=document.createElement('div');
		span.appendChild(document.createTextNode(msg));
		span.style.verticalAlign="middle";
		span.style.padding="2px 5px 2px 2px";
		$j(span).css("float","left");
		span.style.display="inline";
 		$("statusMessage").innerHTML = "";
		$("statusMessage").appendChild(span);
		var closeBtn=document.createElement('a');
		//closeBtn.setAttribute('type','button');
		//closeBtn.setAttribute('value','Close');
		//closeBtn.style.fontSize="9px";
		//closeBtn.style.verticalAlign="middle";
		//closeBtn.style.height="18px";
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
	//stopPolling();
	
}

function randomString(len, charSet) {
    charSet = charSet || 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    var randomString = '';
    for (var i = 0; i < len; i++) {
        var randomPoz = Math.floor(Math.random() * charSet.length);
        randomString += charSet.substring(randomPoz,randomPoz+1);
    }
    return randomString;
}

