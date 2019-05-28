

var treeSelector="div#rf_treeMenu";
var treeMenuRendered=false;
var jqtree=null;
var rf_groupColor=new Array();
var scd_groupIcons=new Array();
var full_version=false;
var editor_initiated=false;
var prevCtxClk=null;
var rf_TagColors=new Object();
var rf_functions=new Array();
var areaEditOptions=new Object();
var scd_TagColors={};

 
var CODE_EDITOR_DEFAULT="default";
var CODE_ACE_EDITOR="ace";
var CODE_MIRROR_EDITOR="codemirror";

var CODE_EDITOR= CODE_EDITOR_DEFAULT; //CODE_ACE_EDITOR;
var ACE_EDITORS=new Object();
var REDITOR_ADMIN=false;


var FUNCTION_TYPE_NORMAL=0;
var FUNCTION_TYPE_CLASS=1;
var FUNCTION_TYPE_METHOD=2;
    

function fr_pageinit(open_functions,readonly,ignoretreeint) {
	var respBack=function(data){
 
		progress_message(null);
		if(data!=null){
			//console.log(data);			
			if(data.tags!=null){				
				for (ic = 0; ic < data.tags.length; ic++) {
					rf_TagColors[data.tags[ic].tagname]=data.tags[ic].background_color;		
					scd_TagColors[data.tags[ic].tagname]=data.tags[ic].background_color;
				}	
			}
			rf_groupColor=data.group_colors;
			fr_treeListGen(data);
			if(data.lite!=null && data.lite){
				$j(".fe-mainMenu").hide();
				$j(".rf_toolBar").hide();
			}
			if(data.open_functions!=null){			
				rf_toggleTree();
				rf_open(data.open_functions);
			}			
		}
		if($j("body").attr("ext_ver")!=null){
			console.log("4E Chrome Extension Version:"+$j("body").attr("ext_ver"));
		}else{
			//console.log("4E Chrome Extension not found");
			if($j.cookie("extn_alerted")==null){
				message_status("<div style='background-color:#9EC0BD;border-radius:4px 0px 0px 4px; padding:10px;margin: -7px 3px -7px -7px;'><h4 style='margin:0px 0px 10px 0px'>4ECAP Chrome extesion available!</h4> <div><pre>\\\\10.153.64.10\\Public\\Infrastructure\\4E Chrome Extension\\4e_chrome.crx</pre>Go to Chrome Extension page (Tools->Extension), drag above file and drop anywhere in that page.</div></div>");
				$j.cookie("extn_alerted","yes",{expires:moment().add('days', 1).toDate()});
			}
			
		}
	}
	
	progress_message("please wait...");
	//var readonly=true;
	if(typeof readonly=='undefined'){
		var readonly=true;
	}	
	if(ignoretreeint==null){
		ignoretreeint=false;
	}
	
	
	try{
		window.postMessage({question:"version",url:document.location.href},document.location.href);
	}catch(ex){
		console.log(ex);
	}
	RFunctionMgmt.getFolderPanelData(open_functions,readonly,ignoretreeint,respBack);
	setInterval(function(){RFunctionMgmt.editorActiveDetected(function(){})},90000);	
	
	
	
	
	//$j(".folder_view_toggle").hide();
	//$j("#folder_view_toggle_btn").click(function() {
	//	    $j(this).val($j(this).val()==">"?"<":">");
	//		$j(".folder_view").toggleClass( "hide", 50);
	//		return false;
	//});
		
}




function rf_toggleTree(){
			$j(".folder_view").toggleClass( "hide", 50);
			return false;	
}



function fr_treeListGen(data1){

	if (data1 != null && typeof data1 == 'object') {
		
		console_log("got data");
			/**
			 * creates group bar
			 */
			if (data1.groups != null) {
				$j(treeSelector).html("");  //removes if there any loading message.
				$j(treeSelector).append("<ul class='filetree'></ul>");
				var groupnodes="";		
				//for (uik in data1.groups) {
				for (iab=0;iab<data1.groups.length;iab++) {
					uik=data1.groups[iab].key;
					groupname=data1.groups[iab].value;
					var htmlid = "root_task_" + uik;
		 			var stylecode=(rf_groupColor[uik]!=null && rf_groupColor[uik]!='')? " style=' background-image:none;background-color:"+rf_groupColor[uik]+"'":"";					
					var nodehtm = "<div class='FunctionGroupBar' groupTarget='" + uik + "' id='" + htmlid + "' "+stylecode+" ><div style='display:inline'>";
					//nodehtm += "<img src='" + scd_groupIcons[uik] + "' border='0'>";
				
					nodehtm += "</div><div style='display:inline;vertical-align:top;' class='label_groupname'><b>" + groupname + "</b></div>";
					
					nodehtm += "<div style=' display:inline;float:right'>";
					//nodehtm += "<input type='button'  class='FunctionGroupBarBtn' taskid='" + uik + "' taskname='" + groupname + "' value='New Function' onclick='scd_createNewTask(this)'>";
					nodehtm += "<input type='button'  class='FunctionGroupBarBtn'  group_uid='" + uik + "' taskname='" + groupname + "' value='New Folder' onclick='rf_ctxCreateFolder(\""+uik+"\")'>";
					nodehtm += REDITOR_ADMIN ? "<span style='margin-left:20px;'><img src='images/draghandle.png' class='dragholder' style='cursor:move'></span>" : "";					
					nodehtm += "</div></div>";
					var spn = "<span class='folder'>" + groupname + "</span>"; 
					var ulgrpid="ul_groupname_"+uik;
					groupnodes += "<li class='closed' groupname='" + uik + "'>" + nodehtm + "<ul id='"+ulgrpid+"' groupname='" + uik + "'></ul></li>";
					
				}
				groupnodes += "";
				$j(treeSelector + " ul.filetree").append(groupnodes);
			}
			
			console_log("groups done");
			
			/**
			* create tree menue folder items.
			*/
			
			var idGen=function(folder,element,prefix){
				 
				folder=folder.replace(/-|\//g, "-");
				folder=folder.replace(/ /g,"_");
 				folder=element+"_"+prefix+"_"+folder;	
				return folder;
			}
			var folderULs=new Array(); 
			if (data1.folders != null) {				
				for (ibc = 0; ibc < data1.folders.length; ibc++) {					
					
					   var folderclass="";
					   if(data1.folders[ibc].stags!=null && data1.folders[ibc].stags!=''){
					       folderclass="no_folder_axs";
					       data1.folders[ibc].stags.split(",").each(function(el){
					                var tg=$j.trim(el);					                                                    
                                    if(tg.indexOf("thm")>=0){
                                        var tg1=(tg.indexOf("-")>0)?tg.substring(tg.indexOf("-")+1):tg;
                                        if(data1.rwx_tags!=null && $j.inArray(tg1,data1.rwx_tags)>=0 ){
                                            folderclass="";
                                        }
                                        //folderclass=(data1.rwx_tags!=null && $j.inArray(tg1,data1.rwx_tags)>=0 )?"":"no_folder_axs";           
                                    }  
					       });
					   }
					 	//var taskuid=data1.folders[ibc].taskuid;
						var group_uid=data1.folders[ibc].group_uid;
												
						var folderpath=data1.folders[ibc].folder_name;
						var uniqfolder=data1.folders[ibc].taskuid+"_"+folderpath;
						
												
						if (folderpath.indexOf("/")>=0  ) {
																					
							var fsplit = folderpath.split("/");
							var current=fsplit[fsplit.length-1];
							var parent= folderpath.replace("/"+current,"");
	 						if ($j('span[uniquefid="' + uniqfolder + '"]').length == 0) {
							    var ulid=idGen(folderpath,'ul',group_uid);
								var liid=idGen(folderpath,'li',group_uid);
								var folder = '<li id="'+liid+'" class="closed '+folderclass+'" folderitem="yes" foldername="' + folderpath + '" group_uid="' + group_uid + '"><span uniquefid="' + uniqfolder + '" class="folder">' + current + '</span><ul id="'+ulid+'" foldername="' + data1.folders[ibc].folder_name + '"  folderid="' + data1.folders[ibc].id + '"></ul></li>';
								
								folderULs[data1.folders[ibc].id]=ulid;
								
								//$j(treeSelector + ' ul.filetree ul[groupname="' + group_uid + '"] ul[foldername="' + parent + '"]').append(folder);
								
								var pulid=idGen(parent,'ul',group_uid);
								$j('#'+pulid).append(folder);
							}
							
						}else {
							if ($j('span[uniquefid="' + uniqfolder + '"]').length == 0) {	
							 	var ulid=idGen(folderpath,'ul',group_uid);
								var liid=idGen(folderpath,'li',group_uid);
								
								var foldbuld="";
								if(data1.folder_2build[folderpath]>0) foldbuld="<span title='Number of functions to be build' class='fold_build'>"+data1.folder_2build[folderpath]+"</span>"
								
								
								var folder='<li id="'+liid+'" folderitem="yes" class="closed '+folderclass+'" foldername="' + folderpath + '" group_uid="' + group_uid + '"><span class="folder" style="display:inline" uniquefid="'+uniqfolder+'" >'+data1.folders[ibc].folder_name+'</span>';
								folder+=foldbuld;
								folder+=REDITOR_ADMIN ? '<span><img src="images/reorder.png" class="orderholder" style="cursor:s-resize;"></span>' : '';
								folder+='<ul id="'+ulid+'" foldername="'+data1.folders[ibc].folder_name+'" folderid="'+data1.folders[ibc].id+'"></ul></li>';
								folderULs[data1.folders[ibc].id]=ulid;																	
								//$j(treeSelector+' li ul[groupname="'+group_uid+'"]').append(folder);
																
								var ulgrpid="ul_groupname_"+group_uid;
								$j('#'+ulgrpid).append(folder);
							}
 							
						}
					}
			 }
			console_log("folders done");
			
			/**
			 * creates or updates folder tasks items.
			 */
			if(data1.rfunctions!=null ){
				for(iac=0;iac<data1.rfunctions.length;iac++){
					    var group_uid=data1.rfunctions[iac].group_id
												
						//rf_functions[rf_functions.length]=$j.trim(data1.rfunctions[iac].function_name);
						rf_functions[rf_functions.length]=data1.rfunctions[iac];
						
						var sc_id=data1.rfunctions[iac].id;						
 					
							//var parentnode=(scd_FolderMnArry[data1.rfunctions[iac].folder_id]!=null)?scd_FolderMnArry[data1.rfunctions[iac].folder_id]:scd_MainMnArry[group_uid];
							//var spwidth=(scd_FolderMnArry[data1.rfunctions[iac].folder_id]!=null)?"372px":"390px";
							var spwidth="352px";
							
							//var activimag=(data1.rfunctions[iac].active==-1)?"images/pause_button.gif":"images/running_button.gif";
							var folderid=data1.rfunctions[iac].folder_id;
							
							var htmlid="function_id_"+sc_id;

						    var oldclass="treeFunctionItem ";

 							/*
							if ($j(treeSelector + ' #' + htmlid).length > 0) {
								    oldclass=$j(treeSelector + ' #' + htmlid).attr("class");									
							}
							*/
							
							var domhtmlid= $j(' #' + htmlid);							
							if (domhtmlid.length > 0) {
								oldclass=domhtmlid.attr("class");
							}
							
						
							
							
							//tag manipulation start ~~~~~~
							var t1="";
							var leng_tag="";
							var more_tags="";
							
							var access_class="access_noaccess";							
							var themefound=false;
							
							if (data1.rfunctions[iac].stags != null && data1.rfunctions[iac].stags != '') {
							    var tags=data1.rfunctions[iac].stags.split(",");
								
								for(iae=0;iae<tags.length;iae++){
									var tg=$j.trim(tags[iae]);
									var owner_tg=data1.rfunctions[iac].owner_tag!=null ? $j.trim(data1.rfunctions[iac].owner_tag):null;

									if(tg.indexOf("thm")>=0) themefound=true;	
									tg1=(tg.indexOf("-")>0)?tg.substring(tg.indexOf("-")+1):tg;
									leng_tag+=tg1;
									if (tg != '') {
									 
										var clas = "function_tag ";
										if(data1.r_tags!=null && $j.inArray(tg1,data1.r_tags)>=0  && access_class=="access_noaccess") access_class="access_readonly";
										if((data1.rwx_tags!=null && $j.inArray(tg1,data1.rwx_tags)>=0) || (data1.rx_tags!=null && $j.inArray(tg1,data1.rx_tags)>=0)) access_class="access_readwrite";
									    if(data1.rwx_tags!=null && $j.inArray(tg1,data1.rwx_tags)>=0 && access_class.indexOf("access_rwx")==-1) access_class+=" access_rwx";
									    if(data1.rx_tags !=null && $j.inArray(tg1,data1.rx_tags)>=0 && access_class.indexOf("access_rx")==-1) access_class+=" access_rx";
									    if(data1.r_tags!=null && $j.inArray(tg1,data1.r_tags)>=0 && access_class.indexOf("access_r")==-1) access_class+=" access_r";
									    if(owner_tg!=null){																				
											owner_tg=(owner_tg.indexOf("-")>0)?owner_tg.substring(owner_tg.indexOf("-")+1):owner_tg;
											if(owner_tg==tg1){
												clas+="item_owner_tag ";
											}
										}
									    
										if (leng_tag.length < 45) {	
												
												if (rf_TagColors[tg] != null && rf_TagColors[tg] != '') {
													t1 += "<span class='" + clas + "' style='background-color:" + rf_TagColors[tg] + "'>" + tg1 + "</span>";
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
							if(!themefound)	access_class="access_readwrite ";							
							if(data1.superuser!=null && data1.superuser!='') access_class="access_readwrite";							
						
							t1+=more_tags;
							//t1+="<a href='#' function_id='"+sc_id+"'  onclick='scd_showTag4Tsk(this); return false;'><span class='task_tag task_tag_addPlus'><img src='images/plus.png'></span></a>";
											

					 		var nodehtml="<div  class='"+oldclass+"' folderid='"+folderid+"' id='"+htmlid+"' >";							
							var fn_name=$j.trim(data1.rfunctions[iac].function_name);
							fn_name=(fn_name.length>=55) ? fn_name.substring(0,55)+"...":fn_name;
													
							nodehtml+="<span access='"+access_class+"' class='fname' >"+fn_name+"</span>";	
							
							nodehtml+="<div class='fn_tag_holder' style='margin-left:10px'>"+t1+"</div>";
							
							nodehtml+=data1.sourced_functions!=null && data1.sourced_functions[sc_id]?"<img src='images/sourced.png' style='margin-left:5px' title='Loaded from source, Modifed after package built'>":"";
							if(data1.rfunctions[iac].following!=null && data1.rfunctions[iac].following!=''){
								nodehtml+="<img src='images/following.png' style='margin-left:5px' title='You are following this'>";
							}
							
							
							
							// tag manipulate end....							
							//nodehtml+="<div style='float:right;margin-right:7px'><small> "+sc_id+" </small>";
							var add_class="";
							nodehtml+="<div style='float:right;margin-right:7px' class='itmToolBar' trashedDays='"+data1.rfunctions[iac].deleteddays+"'>";
							//nodehtml+="<span style='margin-right:10px;'><img src='images/reorder.png' class='orderholder' style='cursor:s-resize;margin-right:0px'><img src='images/filefolder_move.png' class='dragholder' style='cursor:move'></span>";
							nodehtml+=REDITOR_ADMIN?"<span style='margin-right:10px;'><img src='images/reorder.png' class='orderholder' style='cursor:s-resize;margin-right:px'></span>" :"";
							
							nodehtml+=(data1.func_2build!=null && $j.inArray(data1.rfunctions[iac].function_name,data1.func_2build)>=0)?'<span title="This function modified recently and needs to be packaged" style="width:16px;height:16px;background:url(\'images/2package.png\') no-repeat;">&nbsp;&nbsp;&nbsp;&nbsp;</span>' :'';
							if(data1.rfunctions[iac].is_class!=null && data1.rfunctions[iac].is_class==FUNCTION_TYPE_CLASS){
							  //nodehtml+='<span title="This is a class definition" style="width:16px;height:16px;background:url(\'images/class_icon.png\') no-repeat;">&nbsp;&nbsp;&nbsp;&nbsp;</span>';
							  add_class+=" function_class ";  
							}
                            
                            if(data1.rfunctions[iac].is_class!=null && data1.rfunctions[iac].is_class==FUNCTION_TYPE_METHOD){
                                //nodehtml+='<span title="This is a method definition" style="width:16px;height:16px;background:url(\'images/method_icon.png\') no-repeat;">&nbsp;&nbsp;&nbsp;&nbsp;</span>'
                                add_class+=" function_method ";   
                            }
							
							
							
							//nodehtml+="<img function_id='"+sc_id+"' active_value='"+data1.rfunctions[iac].active+"'  title='Activate or Deactive' onclick='scd_toggleActiveate(this)' src='"+activimag+"' style='margin-right:5px;cursor:pointer;vertical-align:middle'>";
							nodehtml+="<input type='button' class='fn_itembtn open' flag='open' function_id='"+sc_id+"' function_name='"+data1.rfunctions[iac].function_name+"' value='View' onclick='fn_edit(this)'>";
							nodehtml+="<input type='button' class='fn_itembtn edit' flag='edit'  function_id='"+sc_id+"' function_name='"+data1.rfunctions[iac].function_name+"' value='Edit' onclick='fn_edit(this)'>";
							nodehtml+="<input type='button' class='fn_itembtn delete' flag='delete'  function_id='"+sc_id+"' function_name='"+data1.rfunctions[iac].function_name+"' value='Del' onclick='fn_deleteFunction(this)'>";
							
							if(data1.rfunctions[iac].is_wiki_done!=null && data1.rfunctions[iac].is_wiki_done){
								nodehtml+="<span class='nowiki_warn'><img src='images/white_box.png' border='0'  style='opacity:0.1'/></span>";
							}else{								
								nodehtml+="<span class='nowiki_warn' title='No wiki page for this function' style='background-image: url(images/wiki_warn.png);background-position: 3px 3px; background-repeat: no-repeat;'><img src='images/wiki.png' border='0' style='opacity:0.7'/></span>";
							}
							
							//nodehtml+="<input type='button' class='fn_itembtn' function_id='"+sc_id+"' value='Del' title='Delete Task' onclick='scd_deleteTask(this)'>";
							//nodehtml+="<input type='button' class='fn_itembtn' function_id='"+sc_id+"' value='Q' title='Show Queue'  onclick='scd_getQueue(this)'>";
							//nodehtml+="<input type='button' class='fn_itembtn' function_id='"+sc_id+"' value='Tags' title='Tag'  onclick='scd_showTag4Tsk(this)'>";						

							nodehtml+="</div></div>";
							
							
							var item="<li taskitem='yes' class='"+access_class+add_class+"' function_id='"+sc_id+"'>"+nodehtml+"</li>";
							
							//var oldfid=$j(treeSelector + ' #' + htmlid).attr("folderid");
							var oldfid=domhtmlid.attr("folderid");
							
							//if($j(treeSelector + ' #' + htmlid).length > 0 && oldfid==folderid){
							if(domhtmlid.length > 0 && oldfid==folderid){
									//$j(treeSelector + ' #' + htmlid).parent().replaceWith(item);
									domhtmlid.parent().replaceWith(item);
							}else{
								//$j(treeSelector + ' #' + htmlid).parent().remove();
								domhtmlid.parent().remove();
								if (folderid == null || (folderid != null && folderid == 0)) {
									
									//$j(treeSelector + ' ul.filetree  li[groupname="' + group_uid + '"] ul[groupname="' + group_uid + '"]').append(item);
									
									var ulgrpid="ul_groupname_"+group_uid;
									$j("#"+ulgrpid).append(item);
									
								}else {
										//$j(treeSelector + ' ul.filetree ul[folderid="' + folderid + '"]').append(item);
										
										foldeulid=folderULs[folderid];
										if (foldeulid != null) {
											$j('#' + foldeulid).append(item);
										}else{
											//incase of adding new task
											$j(treeSelector + ' ul.filetree ul[folderid="' + folderid + '"]').append(item);
										}
								}

							}
							 
					  
				}	
			}
			
			console_log("functions done");
			
			/**
			 * this block will be invoked only on load (first time)
			 */
			if (!treeMenuRendered) {
				 jqtree = $j(treeSelector + " ul.filetree").treeview({
					animated: 100,
					unique: false,
					persist: "cookie",
					cookieId: "navigationtree"					 

				});
				
				/**
				 * show greyed color folder if no content in the folder. 
				 */
				
				
				var refreshEmpty = function(){
					$j(treeSelector + " ul[folderid]").each(function(idx, el){
						if ($j(el).children("li").length == 0) {
							//$j(el).parent().css("color", "#D0D0D0");
							$j(el).parent().addClass("emptyfoldercls");
							$j(el).parent().attr("emptyfolder", "yes");
						}else{
							$j(el).parent().removeClass("emptyfoldercls");
							$j(el).parent().removeAttr("emptyfolder");
						}
					});	
				}
				refreshEmpty();
	 
				console_log("coloring done");
			  	
				var menu2 = [
		
				  {'New Function':{onclick:function(menuItem,menu) {					 
					 	var tobj=this;
				  	 	var folderid=$j(this).parent().children(" ul[folderid]").attr('folderid');				  	 	
						setTimeout(function(){createRFunction(folderid,FUNCTION_TYPE_NORMAL);},100);						
 
				  	},
				  	icon:'images/menu_newfunc.png'} 
				  },				 	
				  {'New S4 Class':{onclick:function(menuItem,menu) {					 
					 	var tobj=this;
				  	 	var folderid=$j(this).parent().children(" ul[folderid]").attr('folderid');				  	 	 
						setTimeout(function(){createRFunction(folderid,FUNCTION_TYPE_CLASS);},100);						

				  	},
				  	icon:'images/class_icon.png'} 
				  },
				  {'New S4 Method':{onclick:function(menuItem,menu) {                    
                        var tobj=this;
                        var folderid=$j(this).parent().children(" ul[folderid]").attr('folderid');
                        
                        setTimeout(function(){createRFunction(folderid,FUNCTION_TYPE_METHOD);},100);                     

                    },
                    icon:'images/method_icon.png'} 
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
									RFunctionMgmt.renameFolder(parseInt(folderid),$j.trim(newname),function(data){
										location.reload(true);
									});
								}
						},100);
					 }
				  	},
				  	icon:'images/menu_renamefunc.png'} 
				  },
				   $j.contextMenu.separator,				   
				  {'Delete Folder':{onclick:function(menuItem,menu) {
					 //if (confirm("Renaming will reload this page and unsaved data on this page will lost, Are you still want to do rename now?")) {
					 	var tobj=this;
				  	 	if($j(this).parent().find(" ul li[taskitem='yes']").length>0){
				  	 		message_status("This folder contain functions, Please move them before deleting")
				  	 	}else{
				  	 		//
			  	 		    var folderid=$j(this).parent().children(" ul[folderid]").attr('folderid'); 
							var cfolder=$j(this).text();
				  	 		setTimeout(function(){				  	 			
				  	 			var respBack=function(data){
				  	 			     message_status("Your task has been deleted");
				  	 			     $j(tobj).parent().fadeOut(300, function(){ $j(tobj).parent().remove();});				  	 			     	
				  	 			}
				  	 			RFunctionMgmt.deleteFolder(cfolder,parseInt(folderid),respBack);
				  	 		},100);


				  	 	} 

					 //}
				  	},
				  	icon:'images/menu_delete.png'} 
				  }

				]; 

				
				$j(treeSelector +" div.treeFunctionItem  [title],"+treeSelector +" [folderitem] [title]").qtip();
				
				
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
				$j(treeSelector +' li[folderitem="yes"]:not([group_uid="trash"]):not(.no_folder_axs)').children("span.folder").contextMenu(menu2,mOptions);
				$j(treeSelector +' li[folderitem="yes"].no_folder_axs').children("span.folder").contextMenu([{"Access Restricted, because you're not in the package themes":{icon:'images/noaccess.png'}}],mOptions);
				//$j(treeSelector +' li[folderitem="yes"][emptyfolder="yes"]').children("span.folder").contextMenu(menu3,mOptions);
				console_log("menu done");
				 
				//enable sortable of items within the folder
				
				//$j(treeSelector + '  li[folderitem="yes"]:not([group_uid="trash"]) ul[foldername]').sortable({
				
				
				//var drag_drop_enabled=false;
				if(REDITOR_ADMIN){				
					$j(treeSelector + '  li[folderitem="yes"] ul[foldername]').sortable({
	
						handle:'img.orderholder',
						stop:function(event,ui){						   
							   var folder_id=$j(this).attr("folderid");
							   var orders=new Array();
							   $j(this).find("li[function_id]").each(function(idx,el){
							   		var oid =$j(el).attr("function_id");
							   		orders[orders.length]=parseInt(oid); 
							   });						  
							   progress_message("Saving display order...");
							   RFunctionMgmt.updateFunctionOrder(parseInt(folder_id),orders,function(respData){progress_message()})
						},
					});				
					console_log("sortable done");
					//accept dragged items into the folder
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
									message_status("Function has been moved successfully");
									sel.append($j(sobj));								  
								}
							}
							//console.log("on drop called");
							
							var new_folder_id=$j(sel).attr("folderid");
							if($j(sobj).parent().attr("folderid")!=new_folder_id){
								var func_id=$j(sobj).attr("function_id"); 
								progress_message("Please wait...");						
								RFunctionMgmt.moveFile2Folder(parseInt(func_id),parseInt(new_folder_id),cb);
							}
							 
						}
					});
					
					
					console_log("droppable 1 done");
					
			 
					////enable folder draggable and sortable
					$j(treeSelector + '  li[groupname] ul[groupname]:not([group_uid="trash"])').sortable({		
								 
						handle:'img.orderholder',
						stop:function(event,ui){
							   //ui
							   //var u=ui;
							   //console.debug("array:"+taskuids);
							   var group_name=$j(this).attr("groupname");
							   var orders=new Array();
							   $j(this).find("li[folderitem]").each(function(idx,el){
							   		//orders[orders.length]=$j(el).find("ul[folderid]").attr("folderid");
							   		var oid =$j(el).find("ul[folderid]").attr("folderid")
							   		orders[orders.length]=parseInt(oid); 
							   		
							   });
							   progress_message("Saving display order...");
							   RFunctionMgmt.updateFolderOrder(orders,function(respData){progress_message()})
							   
							   //console.log("folder_id:"+group_name);
							   //console.log("orders:"+orders);
						},
					});
					
					
					console_log("sortable 2 done");
					
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
								RFunctionMgmt.moveFolder(parseInt(folder_id),groupuid,cb);
							 
						
						}
					});
					
					console_log("droppable 3 done");
					//group re-arrangement...
					$j(treeSelector + ' ul.treeview').sortable({
					 	stop:function(event,ui){
							   var items=$j(treeSelector + ' ul.treeview li[groupname]');
							   var groupids=new Array();
							   for(iab=0;iab<items.length;iab++){						   	   
								   groupids[groupids.length]=$j(items[iab]).attr("groupname");
							   }
							   progress_message("Please wait...");
							   RFunctionMgmt.setGroupOrder(groupids,function(){progress_message()});						 
						},
						handle:'img.dragholder',
					});
				}

			}  //if(!treeMenuRendered) 
			
		  
		   if (!treeMenuRendered) {
			   
			   if(false){
				   	$j("#rf_searchFunction input").autoSuggest(rf_functions, {
				   		startText: "Type Function Names",
				   		selectedItemProp: "function_name",
				   		searchObjProps: "function_name",
				   		selectedValuesProp: "id"
				   	});
			   }else{
				   

				   $j(treeSelector +" .treeFunctionItem").mouseenter(function(obj){	
					    var fname=$j(this).find(".fname").text();
				   		$j(this).find(".itmToolBar").prepend("<a fname='"+fname+"' class='usagebtn' onclick='rf_findRef(this)' href='#'>Find4E</a>");
				   		
				   		var hdiff=$j(this).find(".itmToolBar").position().top-$j(this).position().top
				   		if(hdiff>5) $j(this).find(".fn_tag_holder").hide();
				   						   	
				   });
				   $j(treeSelector +" .treeFunctionItem").mouseleave(function(obj){				       
				   	    $j(this).find(".itmToolBar .usagebtn").remove();
				   	    if(!$j(this).find(".fn_tag_holder").is(':visible')){ 
				   	        $j(this).find(".fn_tag_holder").show();
				   	    }
				   });

				   $j(treeSelector +" li[folderitem]").mouseenter(function(obj){
					    //span.folder	
					   if($j(this).find("> span.packageinfo_box").length>0){   }else{						  
						   var ul=$j(this).find("ul[folderid]");						  
						   ul.before("<span class='packageinfo_box'><a  class='packageinfo' href='#'>Package Info</a></span>");						   
						   var pname=$j(this).find("span.folder").text();										
						   $j(this).find("a.packageinfo").click(function(evt){
							   
 
							   
							   rf_packinfoShow(pname);
							   
						   });
					   }
				   });
				   
				   $j(treeSelector +" li[folderitem]").mouseleave(function(obj){				       
				     	$j(this).find("> span.packageinfo_box").remove();    
				   });
				   $j("#packgeInfoDialog").mouseleave(function(obj){	
					   $j(this).hide("slow");
				   });
					
				   $j('#rf_searchFunctionInp').qtip(); 
				   $j('#rf_searchFunctionInp').multiSelect({	
					    width:"100%",
					    postcall:rf_search,
						source: function( request, response ) {							
							var xhr = new XMLHttpRequest();
							xhr.open("GET", "autocomplete_scheduler.jsp?xhr_rfunction=yes&term="+request.term, true);
							xhr.onreadystatechange = function() {
							  if (xhr.readyState == 4) {			 
								//var resp = eval("var data=" + xhr.responseText + "");
								var data=JSON.parse(xhr.responseText);
								var respdata=[];
								var selected=$j('#rf_searchFunctionInp').multiSelect("getSelected");
								for(iab=0;iab<data.length;iab++){
								    var loc="<span class='broad'>"+data[iab].group_name+"</span> <span class='specific'> "+data[iab].folder_name+"</span>";
									var dt=data[iab].last_modified!=null
									                    ?moment(data[iab].last_modified, "YYYY-MM-DD HH:mm:ss").fromNow()
														:"";
				                    var themses="";
									if(data[iab].stags!=null){
										var arr=data[iab].stags.split(",");
										arr.forEach(function(ele){
											themses+=ele.indexOf("thm-")>=0 ? "<div class='tag'>"+ele.substring(ele.indexOf("thm-")+4,ele.length)+"</div>":"";
										});
									}      
									if($j.inArray(data[iab].function_name,selected)==-1){
										var obj={
											value:data[iab].function_name,						
											label:"<div class='autocomp_item'><div class='item_name'>"+data[iab].function_name+"</div>  <div class='item_foot'><div class='item_f1'>"+loc+"</div><div class='item_f3'>"+themses+"</div><div class='item_f2'>"+dt+"</div></div></div>"
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
		   	
		   	
		   }
		   
		 
		
		   
		  $j(treeSelector+' ul[groupname]:not([groupname="trash"]) .treeFunctionItem >span').inlineEdit({
		    buttons:'<button class="save fn_itembtn">Rename</button> <button class="cancel fn_itembtn">cancel</button>',
		    save: function(el, data) {
		      //return confirm('Change name to '+ data.value +'?');
			  if(confirm("Are you sure you wish to rename this function ?")){
			  	  //var foldid=$j(this).parent().attr("folderid");
				var funid=$j(this).parent().attr("id");
				funid=funid.substring("function_id_".length);
				var respBack=function(data){
					progress_message();
					if (data != null && data) {
						$j('a[href="#rf_tabs-' + funid + '"]').html(data.value);
					}
				}
				progress_message("Please wait while renaming...");			  
				RFunctionMgmt.renameFunction(data.value,parseInt(funid),respBack)			  
				  
			  }
		    }
		   });
		   treeMenuRendered=true;
		
		   console_log("inline edit done");
		  
		   
		   //adding put back and delete permanent buttons for trash items
		   
		   $j(treeSelector+' ul[groupname="trash"] .treeFunctionItem .itmToolBar').each(function(idx,el){
		   	         var fn_id=$j(el).find("input.fn_itembtn").attr("function_id");
		   	         if($j(el).find("input.putback").length==0){
		   	         	var daysdel=parseInt($j(el).attr("trasheddays"));
		   	         	$j(el).append("<input type='button' class='fn_itembtn putback' function_id='"+fn_id+"' value='Put Back' title='Delete Task' onclick='fn_putback(this)'>");
		   	         	if(daysdel>=7){		   	            
		   	         		$j(el).append("<input type='button' class='fn_itembtn purge' function_id='"+fn_id+"' value='Purge' title='Removes from Trash (Permenently)' onclick='fn_purge(this)'>");
		   	         	} 
		   	         }
		   	         	
		   	         	 
		   });
		   
		   console_log("adding trashed days ");
		
		
		    //$j("#rf_treeMenu").jScrollPane({dragMaxHeight:50,dragMinHeight:50,scrollbarWidth:5,scrollbarMargin:5});	
		  	  
			//{
			//	source: rf_functions
			//});
			
	}
}



var previous_logtime=new Date().getTime();
function console_log(msg){
	
	if(false){
		var delay=new Date().getTime()-previous_logtime
		console.log("delay:"+delay);
		console.log(msg);
		console.log("");		
		previous_logtime=new Date().getTime();
	}
	
	
}

function fn_purge(btnobj) {
	 var func_id=$j(btnobj).attr("function_id"); 
	 var respBack=function(flag){
	 	if(flag){
	 		message_status("Deleted Function has been purged successfully");
	 		var node_s = $j(treeSelector + ' li[taskitem="yes"][function_id="' + func_id + '"]');
	 		node_s.fadeOut(300, function(){ node_s.remove();});

	 	}else{
	 		message_status("Could not purge function!");
	 	}
	 }
	 if(confirm("This option will remove the function perminently, Are you really sure ?")) {
	 	RFunctionMgmt.purgeFunction(parseInt(func_id),respBack);
	 }
}


function fn_putback(btnobj) {
	 var func_id=$j(btnobj).attr("function_id"); 
	 $j( "#dialog_folders" ).dialog({
			resizable: false,			 
			width:400,
			modal: true,		
			dialogClass: 'rf_dialog',
			buttons: {
				Cancel: function() {
					$j( this ).dialog( "close" );
				}, 	 				
			}
	 });
	 $j("#dialog_foldersBdy").html("please wait...");
	 var treeID="#dialog_foldersBdy";
	 var respBack=function(data1){
	 	
	 		if (data1.groups != null) {
				$j(treeID).html("");  //removes if there any loading message.
				$j(treeID).append("<ul class='filetree'></ul>");
				var groupnodes="";		
			
				for (iab=0;iab<data1.groups.length;iab++) {
					uik=data1.groups[iab].key;
					groupname=data1.groups[iab].value;
					if(uik!='trash'){
						var htmlid = "folders_root_" + uik;
		 				var stylecode=(rf_groupColor[uik]!=null && rf_groupColor[uik]!='')? " style=' background-image:none;background-color:"+rf_groupColor[uik]+"'":"";					
						var nodehtm = "<div class='FunctionGroupBar' groupTarget='" + uik + "' id='" + htmlid + "' "+stylecode+" >";
						nodehtm += "<div style='display:inline;vertical-align:top;' class='label_groupname'><b>" + groupname + "</b></div></div>";
						var ulgrpid="folders_groupname_"+uik;
						groupnodes += "<li class='closed' groupname='" + uik + "'>" + nodehtm + "<ul id='"+ulgrpid+"' groupname='" + uik + "'></ul></li>";
					}
				}
				groupnodes += "";
				$j(treeID + " ul.filetree").append(groupnodes);
			}
			var idGen=function(folder,element,prefix){
				 
				folder=folder.replace(/-|\//g, "-");
				folder=folder.replace(/ /g,"_");
 				folder=element+"_"+prefix+"_"+folder;	
				return folder;
			}
			
			if (data1.folders != null) {				
				for (ibc = 0; ibc < data1.folders.length; ibc++) {					
						var group_uid=data1.folders[ibc].group_uid;
						
						var folderpath=data1.folders[ibc].folder_name;					
					 	//var ulid=idGen(folderpath,'folders_ul',group_uid);
						//var liid=idGen(folderpath,'folders_li',group_uid);
																		
						var folder='<li class="closed" function_id="'+func_id+'" foldername="'+data1.folders[ibc].folder_name+'" folderid="'+data1.folders[ibc].id+'" style="cursor:pointer" onclick="fn_putbackConf(this)" ><span class="folder" >'+data1.folders[ibc].folder_name+'</span></li>';
						var ulgrpid="folders_groupname_"+group_uid;
						$j('#'+ulgrpid).append(folder);
						
					}
			 }
			
			$j(treeID + " ul.filetree").treeview({
				animated: 100,
				unique: false,
				persist: "cookie",
				cookieId: "navigationtree"					 

			});
	 	
	 }
	 RFunctionMgmt.getFolderTree(respBack);
		
		
}

function fn_putbackConf(liobj){
	var folder_id=$j(liobj).attr("folderid");
	var func_id=$j(liobj).attr("function_id");
	var foldername=$j(liobj).attr("foldername");
	$j( "#dialog_folders" ).dialog("close");
    var respBack=function(data){
    	if(data!=null){
    		var fn_id = func_id
			//var index = $j("li", rf_editorTab).index($j(this).parent());
			//rf_editorTab.tabs("remove", index);			
			
			var node_s = $j(treeSelector + ' li[taskitem="yes"][function_id="' + fn_id + '"]');
			$j(node_s).find("input.putback").remove();
			$j(node_s).find(".rf_tabopen").removeClass("rf_tabopen");
			$j(treeSelector + ' ul[folderid="'+folder_id+'"][foldername="'+foldername+'"]').append(node_s);
    		message_status("Function has been put back to the folder, Please refresh this page");
    	}
    }	
	RFunctionMgmt.putbackFunction(parseInt(func_id),parseInt(folder_id),respBack);
	
}

 
var tempOpenData=null;
function rf_search(){
	var data=$j('#rf_searchFunctionInp').multiSelect("getSelected");       
	var rtn=data.join(",");	
	if (rtn != '') {	
		var respBack=function(data){		 

			progress_message();
			if(data!=null){
				
				tempOpenData=data;				
				for(ibc=0;ibc<data.length;ibc++){
					var thisdata=data[ibc];
					var fn_id=thisdata.data.id;
					if ($j("#rf_tabs-" + fn_id).length > 0) {														
						var index=$j('#rf_tabs a[href="#rf_tabs-'+  fn_id+'"]').parent().index()
						rf_editorTab.tabs('select', index);		
					}else{											
						setTimeout("fn_edit(0,tempOpenData["+ibc+"])",(ibc*1500));						
					}
					
				}
				$j('#rf_searchFunctionInp').multiSelect("removeAllSelected");
				
				
			}
		};
	    progress_message("Please wait while opening functions...");
		var readonly=true;
		RFunctionMgmt.getRFunctions(rtn,readonly,respBack);
	}else {
		alert("Function name is not selected, minimum 1 fuction required");
	}
}


function rf_search_old(){
	var rtn="";
	$j("#rf_searchFunction li.as-selection-item").each(function(){
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
					if ($j("#rf_tabs-" + fn_id).length > 0) {														
						var index=$j('#rf_tabs a[href="#rf_tabs-'+  fn_id+'"]').parent().index()
						rf_editorTab.tabs('select', index);		
					}else{											
						setTimeout("fn_edit(0,tempOpenData["+ibc+"])",(ibc*1500));						
					}
					
				}
				$j("#rf_searchFunction li.as-selection-item").remove();
				$j("#rf_searchFunction li.as-original input.as-values").val('');
				
				
			}
		}
	    progress_message("Please wait while opening functions...");
		var readonly=true;
		RFunctionMgmt.getRFunctions(rtn,readonly,respBack);
	
	}else {
		alert("Function name is not selected, minimum 1 fuction required");
	}
}

 
function rf_open(fn_name){
	 
	if (fn_name != '') {		
       
		
		var respBack=function(data){		 

			progress_message();
			if(data!=null){
				
				tempOpenData=data;				
				for(ibc=0;ibc<data.length;ibc++){
					var thisdata=data[ibc];
					var fn_id=thisdata.data.id;
					if ($j("#rf_tabs-" + fn_id).length > 0) {														
						var index=$j('#rf_tabs a[href="#rf_tabs-'+  fn_id+'"]').parent().index()
						rf_editorTab.tabs('select', index);		
					}else{											
						setTimeout("fn_edit(0,tempOpenData["+ibc+"])",(ibc*1500));						
					}
					
				}
				//$j("#rf_searchFunction li.as-selection-item").remove();
				//$j("#rf_searchFunction li.as-original input.as-values").val('');
				
				
			}
		}
	    progress_message("Please wait while opening functions...");
		var readonly=true;		
		if(typeof fn_name=='string'){
			RFunctionMgmt.getRFunctions(fn_name,readonly,respBack);
		}else{
			respBack(fn_name);
		}
	
	}else {
		alert("Function name is not selected, minimum 1 fuction required");
	}
}

function rf_ctxCreateFolder(group_id) {
	var foldername=prompt("Folder name:");
	if(foldername!=null && foldername!=''){
		RFunctionMgmt.createFolder(foldername,group_id,function(data){fr_treeListGen(data); message_status("Folder created, However you have to refresh this page to support drag & drop feature for new field")	});
		
	}
	
	
}


var currentTabCotent=null;

function fn_edit(tobj,data) {
	if (data == null) {	
		var fn_id = $j(tobj).attr("function_id");
		var fn_name = $j(tobj).attr("function_name");
		var flag = $j(tobj).attr("flag"); 
	}else{
		var fn_id = data.data.id;
		var fn_name = data.data.function_name;		
	}
 
	var respBack=function(data,editrefresh){
		
		if(editrefresh==null){	
		   editrefresh=false; //by default 
		}
		progress_message();
		$j(".folder_view_toggle").show();

		rf_EditorTyped[fn_id]=false;

		if(data!=null ){
			//console.log(data);
			
			 
			var toolbar1="";
			var plugins1="";
			var lockedMsg=null;
			if(data.isAuthorized!=null && data.isAuthorized){				
				//toolbar1="reditor_save,file_open,reditor_delete";
				toolbar1="file_open";				
				plugins1="reditor";
			}else{
				message_status("Readonly Access: Accessing from unknown computer, you can't modify this function");
				toolbar1="ReadOnly";
				plugins1="reditorlite";
			}
			if(data.data.lockedby!=null){
				if (data.authorizedUser == data.data.lockedby) {
					plugins1 = "reditor";					
					toolbar1="file_open";
					//toolbar1="";
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
			 
			var htm='<div class="rf_lockedBy" id="rf_lockedBy_'+fn_id+'"></div>';
			htm+='<div id="rf_stabs_'+fn_id+'" readonly="'+readonly+'" class="rf_subtab">';
			htm+=' <div align="right" style="display:inline;float:right;padding-top:0px;"><input id="fn_btn_save_'+fn_id+'" z-index="1000" style="display:none;font-size:1.1em;width:100px;height:26px" type="button" function_id="'+fn_id+'" onclick="saveRFunction()" value="Update"></div>';
			htm+='	<ul>';
			htm+='			<li><a  href="#rf_stabs1_'+fn_id+'">Code</a></li>';
			htm+='			<li><a href="#rf_stabs2_'+fn_id+'">Revisions</a></li>';
			htm+='			<li><a href="#rf_stabs3_'+fn_id+'">Themes & Notification</a></li>';
			htm+='			<li><a href="#rf_stabs4_'+fn_id+'">Wiki</a></li>';	
			htm+='		</ul>';
			htm+='		<div id="rf_stabs1_'+fn_id+'">';				
			htm+='			<textarea id="rf_editor_area_'+fn_id+'" name="content" style="width:100%; height:88%;overflow:auto;"></textarea>';
			htm+='		</div>';
			htm+='		<div id="rf_stabs2_'+fn_id+'">';
			htm+='        <div class="rev_panel" style="height:89%"></div>';			
			htm+='		</div>';
			htm+='		<div id="rf_stabs3_'+fn_id+'">';			 
			htm+='		     <h3 class="rf_subheader">Tags</h3>	';
			htm+='           <div class="tags_panel" style="width:95%;margin:20px;"></div>';
			htm+='		</div>';
			htm+='		<div id="rf_stabs4_'+fn_id+'">';
			htm+='        <div class="wiki_panel" style="height:89%;"></div>';			
			htm+='		</div>';
			htm+='</div>';			
			
			if(data.access!=null && ( data.access=='R' || data.access=='RX') ){				 
				//lockedMsg="<img src='images/lock.png'>";				
				toolbar1="reditor_lock1";
				plugins1 = "reditorlite";
			}
			
			
			if (!editrefresh) {
				currentTabCotent=htm;
				rf_editorTab.tabs("add", "#rf_tabs-" + fn_id, fn_name);
				var fn_name11 = fn_name;
				$j("#rf_stabs_" + fn_id).tabs({
					selected: 0,
					select: function(event, ui){
						//ui.index
						if (typeof ui.panel.isLoaded == 'undefined' && ui.index == 1) {
							fn_getRevision(fn_name11, ui.panel.id);
							ui.panel.isLoaded = true;
						}
						//if (typeof ui.panel.isLoaded == 'undefined' && ui.index == 2) {
							//rf_showTag4Function(fn_name11, ui.panel.id);
							//ui.panel.isLoaded = true;
						//}
						if (typeof ui.panel.isLoaded == 'undefined' && ui.index == 3) {
							fn_getWiki(fn_name11, ui.panel.id);
							ui.panel.isLoaded = true;
						}
					}
				});
			}else{
				$j("#rf_stabs_"+fn_id).attr("readonly",readonly+"");
				//$j("#rf_stabs1_"+fn_id).html('<textarea id="rf_editor_area_'+fn_id+'" name="content" style="width:100%; height:88%;overflow:auto"></textarea>');
				if(CODE_EDITOR==CODE_EDITOR_DEFAULT){
					editAreaLoader.delete_instance('rf_editor_area_'+fn_id);
				}else if(CODE_EDITOR==CODE_ACE_EDITOR){
					ACE_EDITORS['rf_editor_area_'+fn_id].destroy();					
					$j("#rf_stabs1_"+fn_id).children().remove();
					$j("#rf_stabs1_"+fn_id).html('<textarea id="rf_editor_area_'+fn_id+'" name="content" style="width:100%; height:88%;overflow:auto;"></textarea>');
				}				
				$j("#rf_stabs_" + fn_id).tabs({selected:0});				
			}

			if(lockedMsg!=null){
				$j("#rf_lockedBy_"+fn_id).html(lockedMsg);
				$j("#rf_lockedBy_"+fn_id).addClass("showOn");
			}
			
			
			if(data.tag_follow!=null ){
				data.tag_follow.authorizedUser=data.authorizedUser; 
				to_showTags4Item("#rf_stabs_"+fn_id ,data.tag_follow,fn_id,(plugins1=='reditorlite'?"R":""),"Selected Themes");
			}
			
			// to make the owner appears on top.
			if(data.data.owner_tag_id!=null && data.data.owner_tag_id>0){
				var otag=$j("#rf_stabs_"+fn_id+" .task_tag_owner li[tag_id='"+data.data.owner_tag_id+"']");
				if(otag!=null && otag.length>0 && otag.siblings().length>0){
					otag.insertBefore(otag.siblings(':eq(0)'));
				}				
			}
			
 
			var ea_id="rf_editor_area_"+fn_id;
			$j("#"+ea_id).val(data.content);
			var isEditable=(plugins1=='reditorlite')?false:true;
			
			if(CODE_EDITOR==CODE_EDITOR_DEFAULT){
				areaEditOptions[fn_id]={
						id: ea_id // textarea id
						,syntax: "r" // syntax to be uses for highgliting
						,start_highlight: true // to display with highlight mode on start-up
						//,toolbar: "save, |, search, go_to_line, undo, redo, |, select_font, |, highlight, reset_highlight, | , reditor_save"
						,toolbar:toolbar1
						,allow_toggle: false
						//,font_family: "monospace"
						//,font_size: 11
						,allow_resize: "both"
						,plugins:plugins1
						,font_size:8
						,font_family: "verdana, monospace"
						,is_editable:(plugins1=='reditorlite')?false:true
					    ,save_callback:"saveRFunction"
				};			
				editAreaLoader.init(areaEditOptions[fn_id]);
			}else if(CODE_EDITOR==CODE_ACE_EDITOR){
				    
					var parnt=$j("#"+ea_id).parent(); $j("#"+ea_id).remove();
					var ttl=fn_name;
					//var shortname=fn_name;
					
					//var toolbar_1=isEditable?"<div class='ace_toolbar'><div class='savebtn'><a href='#' onclick='saveRFunction();return false;'><img src='images/savebtn.png'></a><small> [Ctrl+S]</small></div><div class='deletebtn'><a href='#' onclick='fn_deleteFunction();return false;'><img src='images/deletebtn.png'></a></div></div>":"";
					var toolbar_1="";
					var max_1adjust=isEditable?"adjust_max":"";
					$j(parnt).append(toolbar_1+"<div class='icon_fullscreen "+max_1adjust+"'><a href='#' fid='"+ea_id+"' screentitle='"+ttl+"' onclick='rf_acefull(this); return false;'><img src='images/expand_fullscreen.png'></a></div><div class='ace_editor' style='display:none' id='"+ea_id+"'></div>");

				    var fieldval1=data.content==null?"":data.content				    		
					var tm = function(ea_id,fieldval1){
						$j("#"+ea_id).show();						 
						ACE_EDITORS[ea_id]= ace.edit(ea_id);
						ACE_EDITORS[ea_id].getSession().setValue(fieldval1);
						var is_readonly=!isEditable;
						setACE_Default(ACE_EDITORS[ea_id],is_readonly);						
					}
					setTimeout(tm,500,ea_id,fieldval1);
				
			}
			
			editor_initiated=true;			
			//editAreaLoader.setValue("rf_editor_area_"+fn_id,data.content);
			
			if(rf_editorTab.length==1){
				  rf_highLightFunc(fn_id);
			}
			if(data.path!=null){
				setTimeout(function(){rf_editorSetPath(ea_id,data.path)},1000);
			}
			
			if(plugins1=='reditorlite'){				
				$j("#fn_btn_save_"+fn_id).hide();
			}else{
				$j("#fn_btn_save_"+fn_id).show();
				
			}
			
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
	if ($j("#rf_tabs-" + fn_id).length > 0) {
		processTb = false;		 
		var index=$j('#rf_tabs a[href="#rf_tabs-'+  fn_id+'"]').parent().index()
		rf_editorTab.tabs('select', index);		
		RFunctionMgmt.getRFunction(parseInt(fn_id),readonly, function(data){
			respBack(data,true);
		});		
	}else {
		progress_message("Please wait...");
		RFunctionMgmt.getRFunction(parseInt(fn_id),readonly, respBack);
	}
	
}


 

function fn_deleteFunction(thisobj){
	
	if (confirm("Are you sure you wish to delete this function ?")) {
	
	  var fn_id=$j(thisobj).attr("function_id");
	  //var fn_id=rf_getActivePanelID();		
	  //var respBack = 
	  progress_message("Please wait while deleting function...");
	  RFunctionMgmt.deleteFunction(parseInt(fn_id),function(success){
	  	progress_message();
	  	if (typeof success == 'boolean' && success) {			
			message_status("Function has been deleted");
			var fn_id = rf_getActivePanelID();
			var index = $j("li", rf_editorTab).index($j(this).parent());
			rf_editorTab.tabs("remove", index);
			
			
			var node_s = $j(treeSelector + ' li[taskitem="yes"][function_id="' + fn_id + '"]');
			$j(node_s).find(".rf_tabopen").removeClass("rf_tabopen");
			$j(treeSelector + ' ul#ul_trash_Trash').append(node_s);
		}
	  });
		
		
		
		
	}
	
}




function fn_unlockFunction(){
	var fn_id=rf_getActivePanelID();
	//var script=editAreaLoader.getValue("rf_editor_area_"+fn_id);
	//alert("function "+fn_id+"to be unlocked");
	if (confirm("Are you sure you wish to unlock this function?")) {
	
		RFunctionMgmt.unLockFunction(parseInt(fn_id), function(data){
			progress_message();
			if (data != null) {
				message_status("Function unlocked successfully")
			};
			var ea_id = "rf_editor_area_" + fn_id;
			rf_editorExecCommand(ea_id, 'reditor_locked', false);
			$j("#rf_lockedBy_" + fn_id).html("");
			$j("#rf_lockedBy_" + fn_id).removeClass("showOn");
		});
	}				
						
	
}




function fn_lockFunction(){
	var fn_id=rf_getActivePanelID();
  
	
	$j( "#dialog_lockFunc" ).dialog({
			resizable: false,			 
			width:400,
			modal: true,		
			dialogClass: 'rf_dialog',
			buttons: {
				Cancel: function() {
					$j( this ).dialog( "close" );
				}, 	 				
				"Lock": function() {
					$j( this ).dialog( "close" );
					progress_message("Please wait...");			
					RFunctionMgmt.lockFunction(parseInt(fn_id),parseInt($j("#dialog_lockFunc_opt").val()),function(data){						
					    progress_message();
						if(data!=null){														
							message_status("Function locked successfully")};
							var ea_id="rf_editor_area_"+fn_id;
							rf_editorExecCommand(ea_id,'reditor_locked',true);
							$j("#rf_lockedBy_"+fn_id).html("Locked by you");
							$j("#rf_lockedBy_"+fn_id).addClass("showOn");						
						}
					);
					
					
					
				}
			}
		});
		
	
	
}

function updateItemPrivilgeNotification(themes, tags,itemid, respBack) {	
	RFunctionMgmt.getItemPrivilegeNotifications(themes,tags,respBack);
	rf_flagEdited(itemid);
}




function fn_getWiki(fn_name11,panel_id,exeCallBk){
	
	var respBack = function(rdata){
		progress_message();
		if (rdata != null) {
			
			$j("#"+panel_id+" div.wiki_panel").html('<div class="function_wiki"><div class="wiki_tb"><input  function_name="'+fn_name11+'" panel_id="'+panel_id+'" type="button" value="Edit This Wiki" onclick="wiki_inlineEditorObj(this)"></div>'+rdata+'</div>');
			
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
						//wiki_inlineEditor($j(this).attr('href'),fn_name11,panel_id);
						
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
	RFunctionMgmt.getFunctionWiki(fn_name11,respBack);
}

function wiki_help(lnk_p){

    var lnk="R_Functions_Editor";
	if(lnk_p!=null) lnk=lnk_p;
	var func = function(){
		$j("#wikiDialog").dialog({
			resizable: false,
			width: 1200,
			modal: true,
			title:"Help: R Function Editor",
			dialogClass: 'rf_dialog rf_wikidialog'
		});
	}
	$j("#wikiDialogBdy").html("<div class='wiki_panel'></div>");
	fn_getWiki(lnk,"wikiDialogBdy",func);
}





function wiki_inlineEditorObj(thisobj){
	var func_name=$j(thisobj).attr("function_name")
	var panel_id=$j(thisobj).attr("panel_id");
	wiki_inlineEditor(func_name,panel_id);
}
function wiki_inlineEditor(function_name,panel_id){

	var respBack = function(rdata){

		var is_wiki_empty = false;
		if(rdata.indexOf("There is currently no text in this page")>=0){
			is_wiki_empty = true;
		}
		
		var respBack2 = function(data){
			
			if (is_wiki_empty) {
		
				var cat = data[0].toUpperCase() + data.slice(1);
				var function_name_title = function_name[0].toUpperCase() + function_name.slice(1);
				var url_wiki_empty="https://wiki.4ecap.com/api/4Eapi.php?action=sendpage&text=[[Category:"+cat+"]]&title="+function_name_title;
				//var url_wiki_empty="https://wiki.4ecap.com/api/4Eapi_BETA.php?action=sendpage&text=[[Category:"+cat+"]]&title="+function_name_title;		

				var i = document.createElement("img");
				i.src = url_wiki_empty; // just execute url, img is dummy.
			
			}

			var url_wiki_exist="https://wiki.4ecap.com/4ecapwiki/index.php?title="+function_name+"&action=edit";
			//var url_wiki_exist="http://betawiki.4ecap.com/beta4ecapwiki/index.php?title="+function_name+"&action=edit";
			$j("#"+panel_id+" div.wiki_panel").html('<div class="function_wiki_editor"><div class="wiki_tb"><input function_name="'+function_name+'" panel_id="'+panel_id+'" type="button" value="Finished Editing" onclick="fn_closeWikiEditor(this)"></div></div>');
			$j("#"+panel_id+" div.wiki_panel").append('<div class="wiki_editor"><iframe width="100%" height="94%" src="'+url_wiki_exist+'" scrolling="yes"></iframe></div>');	
			
		};
	
		var fn_id=rf_getActivePanelID();
		RFunctionMgmt.getFolderThemeByRFunctionId(parseInt(fn_id),respBack2);

	}
	RFunctionMgmt.getFunctionWiki(function_name,respBack);


}

function fn_closeWikiEditor(thisobj){
	var func_name=$j(thisobj).attr("function_name")
	var panel_id=$j(thisobj).attr("panel_id");
	fn_getWiki(func_name,panel_id);
}

function fn_getRevision(fn_name11,panel_id,clbdata) {
	//alert(fn_name11+":"+panel_id);
	
	var respBack = function(rdata){
		progress_message();
		if (rdata != null) {
		    $j("#"+panel_id+" div.rev_panel").html("");
			$j("#"+panel_id+" div.rev_panel").append('<ul class="revisionList" style="padding:5px;"></ul>');
			//for (rev in rdata) {
			for (rev=0;rev<rdata.length;rev++) {
				//li="<li><b>Rev: "+rdata[rev].revision+"</b> &nbsp;&nbsp;&nbsp; By:"+rdata[rev].author+" &nbsp;&nbsp;&nbsp;<small>("+rdata[rev].date+")</small>";
				li = "<li>" + rdata[rev].author + " &nbsp;&nbsp;&nbsp;<span class='date'>(" + rdata[rev].date + ")</span>";
				li+="<div class='message'>"+rdata[rev].message+"</div>";
				li += "<div style='float:right;vertical-align:top;margin-top:-1' class='svn_link_btn'>";				
				li += "<a href='#' onclick='fn_showRev(this,false); return false;' function_name='" + rdata[rev].function_name + "' path='" + rdata[rev].path + "' compare='yes' revision='" + rdata[rev].revision + "' >Diff</a>";
				//li += "<a  href='push_svndiff.jsp?scheduler_id=" + rdata[rev].scheduler_id + "&revision=" + rdata[rev].revision + "'><img src='images/external_link.gif' border='0'></a>";
				li += "<a href='#' onclick='fn_showRev(this,true); return false;' function_name='" + rdata[rev].function_name+ "'  path='" + rdata[rev].path + "'  revision='" + rdata[rev].revision + "' code='yes'  >Show Code</a>";
				li += "</div><div class='fn_inline_editor' style='display:none'></div>";
				
				$j("#"+panel_id+" div.rev_panel ul.revisionList").append(li);
			}
		}
	}
	if(clbdata!=null){

	   respBack(clbdata);
	}else{
	   
       progress_message("please wait....");
       RFunctionMgmt.getSVNLogs(fn_name11,respBack);
	    
	}
	
}

function fn_showRev(ancobj,isCode) {
	
	
	var fn=$j(ancobj).attr("function_name");
	var rev=$j(ancobj).attr("revision");
	var path=$j(ancobj).attr("path");
	
	var area= $j(ancobj).parent().parent().find("div.fn_inline_editor");
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
					syntax: isCode ? "r" : "rdiff" // syntax to be uses for highgliting
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
					plugins: "reditorlite",
					is_editable:false
				
				});
			}
			editAreaLoader.setValue(areaid,data);
			 
		}
	}
	progress_message("please wait...");
	RFunctionMgmt.getScriptRev(fn,rev,isCode,path,respBack);
	
}

var rf_editorTab=null;
$j(function() {
	
	
		rf_editorTab = $j( "#rf_tabs").tabs({
			tabTemplate: "<li><a href='#{href}'>#{label}</a> <span class='ui-icon ui-icon-close'>Remove Tab</span></li>",
			add: function( event, ui ) {
				//var tab_content = $tab_content_input.val() || "Tab " + tab_counter + " content.";
				//$( ui.panel ).append( "<p>" + tab_content + "</p>" );
				$j( ui.panel ).append(currentTabCotent);
				rf_editorTab.tabs( "select", ui.index);
			},
			select: function(event, ui) { 
			   //alert(ui);
			   
			   var func_id=ui.panel.id;
			   if(func_id!=null) func_id=func_id.replace("rf_tabs-","");
			   //rf_highLightFunc(func_id);
			   setTimeout(rf_highLightFunc,200);			   
			}
		});		
		
		
		// close icon: removing the tab on click
		// note: closable tabs gonna be an option in the future - see http://dev.jqueryui.com/ticket/3924
		$j( "#rf_tabs span.ui-icon-close" ).live( "click", function() {
			var index = $j( "li", rf_editorTab ).index( $j( this ).parent() );
			
			
			var activepanel=rf_editorTab.data().tabs.panels[index];
			var func_id=$j(activepanel).attr("id")
			
			
			if(func_id.indexOf("rf_ptabs")>=0){
				rf_editorTab.tabs( "remove", index );
				return;

			}
			
			func_id=func_id.replace("rf_tabs-","");	
			
			var modified=false;
			if(CODE_EDITOR==CODE_ACE_EDITOR && $j( this ).parent().hasClass("ace_edited")){
				modified=true;  
			}else if(CODE_EDITOR==CODE_EDITOR_DEFAULT && rf_EditorTyped[func_id]!=null  && rf_EditorTyped[func_id]){
				modified=true;
			}
			
			
			//if(rf_EditorTyped[func_id]!=null  && rf_EditorTyped[func_id]){				
			if(modified){
				if(confirm("Changes will be lost! Are you sure you want to close ?")){
					rf_editorTab.tabs( "remove", index );
					RFunctionMgmt.unLockFunctionFromCache(parseInt(func_id),function(data){});
					//if(index==0) 
					rf_highLightFunc(); 
					rf_EditorTyped[func_id]=null;
					areaEditOptions[func_id]=null;
					
					if(typeof ACE_EDITORS["rf_editor_area_"+func_id]=='object' ){
						delete ACE_EDITORS["rf_editor_area_"+func_id];
					}					

				}
			}else{
				rf_editorTab.tabs( "remove", index );
					
				RFunctionMgmt.unLockFunctionFromCache(parseInt(func_id),function(data){});
				//if(index==0)
				rf_highLightFunc(); 
				areaEditOptions[func_id]=null;
				if(typeof ACE_EDITORS["rf_editor_area_"+func_id]=='object' ){
					delete ACE_EDITORS["rf_editor_area_"+func_id];
				}					

				
			}
				
			
			
		});
		
		
});


 
function saveRFunction(){
	//rf_editorTab.tabs( "remove", index );
	//alert("save pressed");
	//alert(rf_getActivePanelID());
	var fn_id=rf_getActivePanelID();
	var script=null;
	if(CODE_EDITOR==CODE_ACE_EDITOR){
		script=ACE_EDITORS["rf_editor_area_"+fn_id].getSession().getValue();		   
	}else if(CODE_EDITOR==CODE_EDITOR_DEFAULT){
		script=editAreaLoader.getValue("rf_editor_area_"+fn_id);
	}

	$j("#dialog_commit_msg").val();
	
	
	
	var tag_panel="#rf_tabs-"+fn_id+" .tags_panel";					
	var tagids=new Array();			 
	$j(tag_panel+" .task_tag_owner li[tag_id]").each(function(idx,el){	tagids[tagids.length]=$j(el).attr("tag_id");});			
	var followids=[];				 
	$j(tag_panel+" .task_tag_follower li[tag_id]").each(function(idx,el){		followids[followids.length]=$j(el).attr("tag_id");	});
	
	if(tagids.length<1){		
		
		$j("#rf_stabs_"+fn_id).tabs( "select" , 2);		
		alert("Failed: Minimum 1 tag required. Please select tag from tag tab");
		return ;
	}else{	
		//if(followids<1 && !confirm("Changes on this task in feature will not be notified anyone! Would you still wish to continue without any 'Notification of Changes' tags ?")){
		//	return ;
		//}
	}
	
	$j( "#dialog_commit" ).dialog({
		resizable: false,			 
		width:700,
		modal: true,		
		dialogClass: 'comment_dialog',		
		buttons: {
			"Cancel": function() {
			$j( this ).dialog( "close" );
			},	 				
			"Save": function() {
				progress_message("Please wait...");					
				//$j( "#dialog_commit").dialog( "close" );
				$j( this ).dialog( "close" );
				
						
				RFunctionMgmt.modifyRFunction(parseInt(fn_id),script,$j("#dialog_commit_msg").val(),tagids,followids,function(data){						
				    progress_message();
					if(data!=null){
						fr_treeListGen(data);
						if(data.revisions!=null){
						    fn_getRevision(data.function_name,"rf_stabs2_"+data.function_id,data.revisions);
						}
						rf_highLightFunc(fn_id);
						if(data.function_id!=null){
							$j("div#rf_tabs > ul a[href='#rf_tabs-"+data.function_id+"']").parent().removeClass("ace_edited");
						}
						message_status("Function saved successfully")};
						$j("#dialog_commit_msg").val("");
						rf_EditorTyped[rf_getActivePanelID()]=false;						
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
 
function rf_getActivePanelID(){
	
	var activepanel=rf_editorTab.data().tabs.panels[rf_editorTab.tabs("option","selected")];
 
	var func_id=$j(activepanel).attr("id")
	if(func_id!=null){
		func_id=func_id.replace("rf_tabs-","");
		return func_id; 
	}else{
		return null;
	}


}
var rf_EditorTyped=new Array();
function rf_typeDetected(d){
	//console.log("t:"+d);
	rf_EditorTyped[rf_getActivePanelID()]=true;
}

function rf_highLightFunc(func_id){	
	$j(".rf_tabopen").removeClass("rf_tabopen");
	$j(".rf_tabactive").removeClass("rf_tabactive");
	
	
	//in case drag and drop file, the drop area will disappear in hidden tab.
	
	$j("#rf_tabs div[id^='rf_tabs-']").each(function(idx,elem){
		var this_id=$j(elem).attr("id");
		var fid=this_id.replace("rf_tabs-","");
		
		$j("#function_id_" + fid).addClass("rf_tabopen");		
		var readonly=$j("#rf_stabs_"+fid).attr("readonly");
		
		if(readonly=='true'){
			$j("#function_id_" + fid+" input.fn_itembtn[value='Open']").addClass("hidebtn");
			$j("#function_id_" + fid+" input.fn_itembtn[value='Edit']").removeClass("hidebtn");
		}else{
			$j("#function_id_" + fid+" input.fn_itembtn[value='Open']").addClass("hidebtn");
			$j("#function_id_" + fid+" input.fn_itembtn[value='Edit']").addClass("hidebtn");
			$j("a[href='#rf_tabs-"+fid+"']").addClass("rf_editabletab");			
		}
		

		if (!$j(elem).hasClass("ui-tabs-hide")) {
			func_id=fid;
			$j("#function_id_" + func_id).addClass("rf_tabactive");			
			if (frames["frame_rf_editor_area_" + func_id] != null) {
				$j(".qq-upload-drop-area", frames["frame_rf_editor_area_" + func_id].document).hide();
			}
					
			var group_id=$j(treeSelector + ' li[function_id="' + func_id + '"]').parent().parent('li[folderitem="yes"]').attr("group_uid");
			$j(treeSelector + ' li.expandable[groupname="'+group_id+'"]').children('div.hitarea').trigger('click');				
			$j(treeSelector + ' li[function_id="' + func_id + '"]').parent().parent('li.expandable[folderitem="yes"]').children('div.hitarea').trigger('click');
			setTimeout(function(){		
				$j("#rf_treeMenu").scrollTo("#function_id_" + func_id,700, {offset: {top:-410}} );
			},300);
		}	
				
	});
	

 
}


function createRFunction(s_folder_id,func_type) {
	
	var fword="function";
	if(func_type==FUNCTION_TYPE_CLASS) fword="class";
	if(func_type==FUNCTION_TYPE_METHOD) fword="method";
	var name=prompt("Enter "+fword+" name");
	if (name != null && name!='') {
		 
		var respBack=function(data){
			progress_message();
			if(data!=null){
				fr_treeListGen(data);
				fn_edit(0,data.func_data);
			}
		}
		progress_message("Please wait...");
		RFunctionMgmt.createRFunction(s_folder_id,name,"",func_type,respBack);
	}
	
	
}


//function addTab(tid,tab_title) {
			//var tab_title = $tab_title_input.val() || "Tab " + tab_counter;
			
			//tab_counter++;
//}
		


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


/**
 * To display status message on screen after the server responded for client request 
 * @param {Object} msg
 */
function message_status(msg){
	
	 if (msg != null) {
 		progress_message(null);
		var span=document.createElement('div');
		//span.appendChild(document.createTextNode(msg));
		$j(span).append(msg);
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





function rf_showTag4Function(func_name,panel_id) {
	 
	var func_id=rf_getActivePanelID();
	respBack = function(data){
		progress_message();
		if (data.tags != null) {			
			
			//var ttl = "Tags (Task ID:" + sc_id + ")";
			//$j("#dialog_box_tags").dialog({close: function(event, ui){},modal: true,title: ttl,width: 600,position: 'top'});			
			//var htm="<form style='margin:10px' action='#' onsubmit='scd_saveTaskTags("+sc_id+")'><ul class='task_tag_dialog_ul'>"
			 
			var htm="<ul class='function_tag_dialog_ul'>"
			 
			for(ic=0;ic<data.tags.length;ic++){
				var styl=""; var checked=""; tg_class="";
				tg=$j.trim(data.tags[ic].tagname)
								
				if (rf_TagColors[tg] != null && rf_TagColors[tg] != '') {
					styl= "style='background-color:" + rf_TagColors[tg] + "'";
				}
				  
				if($j.inArray(data.tags[ic].id,data.tagids)>=0){
					checked+=" checked ";
				}
				if(tg.substring(0, 4)=="usr-"){
					tg_class="task_tag_dialog_usr";
					checked+=" disabled='disabled' ";
				}
				
				htm+="<li class='function_tag function_tag_dialog "+tg_class+"' " +styl+"><input function_id='"+func_id+"' onclick='rf_updateTaskTags(this)' type='checkbox' "+checked+" value='"+data.tags[ic].id+"'>"+tg+"</li>"
			}
			htm+="</ul>";
			//htm+="</ul><span style='text-align:right;margin-top:20px;border-top:1px solid grey; padding:3px; display:block'><input type='submit' value='Save'></span></form>";
			$j("#"+panel_id+" .tags_panel").html(htm);
		}
		rf_followThisFun(null,null,data,func_id,panel_id);
	}
    progress_message("Please wait loading tags.....");
  	RFunctionMgmt.getFunctionTags(func_id,respBack);
}


function rf_followThisFun(follow,thisobj,  data,func_id,panel_id){
	 
	var respBack=function(data,func_id,panel_id){
		var socialbtn="<input type='button' value='Follow This Function' onclick='rf_followThisFun(true,this)' panel_id='"+panel_id+"'  function_id='"+func_id+"' user='"+data.authorizedUser+"'>";
		var htm="";
		if (data.followers != null) {			
		
			for(ic=0;ic<data.followers .length;ic++){
			    htm+="<li>"+data.followers[ic]+"</li>";
				if(data.authorizedUser!=null && data.followers[ic]!=null&& data.authorizedUser.toLowerCase()==data.followers[ic].toLowerCase()){
					var socialbtn="<input type='button' value='Unfollow This Function' onclick='rf_followThisFun(false,this)' panel_id='"+panel_id+"'  function_id='"+func_id+"' user='"+data.authorizedUser+"'>";
				}
			}		
			htm="<ul>"+htm+"</ul>";			 
		}
		socialbtn=(data.authorizedUser!=null)?"<div class='rf_socialbtn'>"+socialbtn+"</div>":"";					
		$j("#"+panel_id+" .following_panel").html(htm+socialbtn);

	}
	
	if (data != null) {		
		respBack(data,func_id,panel_id);
		return;
	}else {			
		var func_id= $j(thisobj).attr("function_id");
		var usr = $j(thisobj).attr("user");
		var panel_id = $j(thisobj).attr("panel_id");			
		progress_message("Please wait...");
		RFunctionMgmt.followFunction(follow, func_id, usr, function(data){progress_message();respBack(data,func_id,panel_id);});
	}
}


function rf_updateTaskTags(thisobj){
	 var fn_id=$j(thisobj).attr("function_id");
	 var tag_id=$j(thisobj).val();
	 var checked=thisobj.checked;
	 //alert(sc_id+":"+tag_id+":"+checked);
	 var repBack=function(data){
	 	progress_message();
		fr_treeListGen(data);	
	 }
	 progress_message("Please wait..")
	 RFunctionMgmt.updateTags4Function(parseInt(fn_id),parseInt(tag_id),checked,repBack);
	 
}


function rf_findRef(thisobj){
	var fname=$j(thisobj).attr("fname");
	rf_searchWithinREnv(fname,fname);
}


function rf_searchFrURL(word) {
	
	setTimeout(function(){
		$j("#rf_treeMenu").hide();
		$j(".fe-mainMenu").hide();
		$j(".rf_toolBar").hide();
		var embed_into_body=true;
		rf_searchWithinREnv(word,null,embed_into_body);
	},500);
	
	
}



var search_ace_editor=null;
function rf_searchWithinREnv(word,ignore,embody) {
	
	if(embody==null) embody=false;
	//$j("#rf_search_dialogBdy").html("<div style='text-align:center;padding-top:100px'>Please wait....</div>");	
	var respBack=function(data){
		progress_message();
		var ts="#rf_search_dialogBdy ul.search_tree_ul";
		
		var htm="<div class='search_tbl'>" ;
		htm+="<div class='search_tree'><ul class='search_tree_ul filetree'></ul></div>";
		//htm+="<div class='search_navi'></div>";
		htm+="<div class='search_reditor'><div class='search_editorttitle'><span class='search_titlespan'></span><a href='#' title='Move to next result position' onclick='rf_searchMoveSelection(\"#rf_search_dialogBdy .search_tree_ul li[pfile][line]\",\".selected\",false);return true;' class='moveup'><img src='images/move_up.png' ></a><a href='#' title='Move to previous result position' onclick='rf_searchMoveSelection(\"#rf_search_dialogBdy .search_tree_ul li[pfile][line]\",\".selected\",true);return false;' class='movedown'><img src='images/move_down.png'></a></div><div id='search_reditortxt' style='width:100%; height:96%;overflow:auto;'></div></div>" ;
		htm+="</div>";	
		$j("#rf_search_dialogBdy").html(htm);
		var lcount=0;
		var fcount=0;
		var manNode=function(ts,title,sd,cls,name_fn){
			$j(ts).append("<li class='"+cls+"'><div class='search_group'>"+title+"</div><ul> </ul> </li>");
			for(f in sd){				 
				 var fname=f;
				 if(name_fn!=null){
					 fname=name_fn.call(this,fname);
				 }
				 if(fname!=null){
					 $j(ts).find("."+cls+" > ul").append("<li file='"+f+"' ><span class='file'>"+fname+"</span><ul></ul></li>");
					 var filenode=$j(ts).find("."+cls+" > ul li[file='"+f+"']");
					 fcount++;
					 for(iab=0;iab<sd[f].length;iab++){
						 var resu=sd[f][iab];
						 var snip=resu.split("|")[1].length>45?resu.split("|")[1].substring(0,45)+"...":resu.split("|")[1];
						 var line=(resu.split("|")[0]);	
						 snip=$j('<div/>').text(snip).html(); //escaping html
						 
						 filenode.find("ul").append("<li ttl='"+fname+"' pfile='"+f+"' line='"+line+"' >"+line+":<span class='result_snip'>"+snip+"</span></li>");
						 lcount++;
					 }
				 }
			}
		}
		if(data.r!=null){			
			manNode(ts,"R Functions",data.r,"node_r",function(f){ f=f.replace(/\.r/g,'');return (f==ignore && ignore!=null)?null:f; });
		}
		if(data.scd!=null){
			manNode(ts,"Scheduler Scripts",data.scd,"node_scd",function(f){ f=f.replace(/script_/g,'Script ').replace(/\.R/g,'');return (f==ignore && ignore!=null)?null:f; });
			//$j(ts).append("<li class='node_scd'>Scheduler Scripts</li>");		
		}
		if(lcount==0) {
			message_status(word+' not found in any function/scheduler');
			return ;
		}
		
		$j("#rf_search_dialog").commonDialog({width:"96%", height:$j(window).height()-30,modal:true,hidetitle:false});

		$j("#rf_search_dialogBdy").unbind("keyup");
		$j("#rf_search_dialogBdy").bind("keyup",function(e){
			 
		    if(e.keyCode==38) rf_searchMoveSelection(ts+" li[pfile][line]",".selected",false);
		    if(e.keyCode==40) rf_searchMoveSelection(ts+" li[pfile][line]",".selected",true);
		});	
		
		//if(!embody){
		$j("#rf_search_dialog").commonDialog( "option", "title", word+" found in "+fcount+" files and "+lcount+" times");
		//}
		$j(ts).treeview({animated: 100,unique: false});
		
		$j(ts).find("li[pfile][line]").click(function(){
			var pfile=$j(this).attr("pfile");
			var line=$j(this).attr("line");
			var ttl=$j(this).attr("ttl");
			$j(ts).find("li[pfile][line].selected").removeClass("selected");
			$j(ts).find("li[file].fileselected").removeClass("fileselected");
			$j(this).addClass("selected");
			$j(this).parents("li[file]").addClass("fileselected");
			
		
					
			
			var thisobj=this;
			setTimeout(function(){
				$j(ts).scrollTo(thisobj, {offset: {top:-110}} );
			},200);			
			
			if(search_ace_editor["currentdoc"]!=null && search_ace_editor["currentdoc"]==pfile){
				
				search_ace_editor.focus();
				search_ace_editor.findAll(word,{ backwards: false,   wrap: false,   caseSensitive: false,    wholeWord: false,   regExp: false},false);
				search_ace_editor.gotoLine(+line);
			}else{
				RFunctionMgmt.getSearchScript(pfile,function(scr_data){
					search_ace_editor.getSession().setValue(scr_data.script);
					if(scr_data.deleted!=null && scr_data.deleted){
						$j(".search_tbl .search_titlespan").html(ttl+" [Deleted]");
						$j(".search_tbl .ace_content").css("background","#F3DFDF"); 
					}else{
						if(scr_data.item_id!=null){
							$j(".search_tbl .search_titlespan").html(ttl+'<input value="Open" onclick="rf_openSearchedItem(this)" item_type='+scr_data.item_type+' item_id='+scr_data.item_id+'  type="button" style="font-size: .65em;padding: 1px 8px;border-radius: 4px;float: right;margin-right: 30;">');
						}
						$j(".search_tbl .ace_content").css("background","");
					}
					search_ace_editor["currentdoc"]=pfile;
				
					
					
					$j(search_ace_editor.container).find(".ace_gutter-layer .ace_found").each(function(idx,el){
						search_ace_editor.getSession().removeGutterDecoration((+$j(el).text()-1),"ace_found");
					});					
					search_ace_editor.getSession().addGutterDecoration(((+line)-1),"ace_found");
					$j(thisobj).siblings().each(function(idx,el){						
						search_ace_editor.getSession().addGutterDecoration((+$j(el).attr("line")-1),"ace_found");
					});
					 				
					search_ace_editor.focus();
					search_ace_editor.findAll(word,{ backwards: false,   wrap: false,   caseSensitive: false,    wholeWord: false,   regExp: false},false);
					search_ace_editor.gotoLine(+line);
					
				});
			}
		});
		
		$j(ts).find("li[file]").click(function(){
			if($j(this).find("li[pfile][line].selected").length==0){
				$j(this).find("li[pfile][line]:first-child").trigger('click');
			}
		});
		
		$j(ts+" span.file").unbind("click");
		
		
		/*
		$j(ts).find("li[pfile][line]").click(function(){
			var pfile=$j(this).attr("pfile");
			var line=$j(this).attr("line");
			$j(ts).find("li[pfile][line].selected").removeClass("selected");
			$j(this).addClass("selected");	
			var thisobj=this;
			setTimeout(function(){
				$j(ts).scrollTo(thisobj, {offset: {top:-110}} );
			},200);
			
			RFunctionMgmt.getSearchScript(pfile,function(scr_data){
				search_ace_editor.getSession().setValue(scr_data);
				search_ace_editor.gotoLine(+line);
				search_ace_editor.find(word,{ backwards: false,   wrap: false,   caseSensitive: false,    wholeWord: false,   regExp: false});
				
				
			});
		});
		 
        */

		if(search_ace_editor!=null) search_ace_editor.destroy();
		
		search_ace_editor=ace.edit("search_reditortxt");		
		search_ace_editor.setTheme("ace/theme/chrome");
		search_ace_editor.getSession().setMode("ace/mode/r");					
		search_ace_editor.setShowPrintMargin(false);
		search_ace_editor.setFontSize(".83em");
		search_ace_editor.session.setFoldStyle("markbegin");
		search_ace_editor.setShowFoldWidgets(true);
		search_ace_editor.setHighlightActiveLine(true);
		search_ace_editor.setReadOnly(true);

		setTimeout(function(){
			$j($j(ts).find("li[pfile][line]")[0]).trigger("click");
		},200);
	    
		if(embody){
			//
			$j(".ui-widget-overlay").hide();
			var dencl=$j("#rf_search_dialog").parent();
			dencl.find(".ui-dialog-titlebar-close").hide();			 
			dencl.css("margin","5px");
			dencl.css("top","0px");
			dencl.css("left","0px");
			dencl.css("height","98%");
			dencl.css("width","99%");
			
		}
		//$j("#search_reditortxt");
		
		
	}
	progress_message('Searching........');
	
	RFunctionMgmt.getSearchR(word,respBack);
	
}

function rf_openSearchedItem(thisobj) {
	var item_id=$j(thisobj).attr("item_id");
	var item_type=$j(thisobj).attr("item_type");
	if(item_type!=null && item_type=='scheduler'){
		window.open('scheduler.jsp?scheduler_id='+item_id);
	}
	if(item_type!=null && item_type=='function'){
		window.open('r.jsp?open_functions='+item_id);
	}
			
}

function rf_searchMoveSelection(selector, filter, isForward){
	var filter=$j(selector).filter(filter);
	if(isForward){
		var found=false;
		var clicked=false;
		$j(selector).each(function(idx,el){			
			if(found){
				$j(el).trigger('click'); found=false;
				clicked=true;
			}
			if(filter[0]==el)found=true;
		});
		if(!clicked) $j(selector).first().trigger('click');
	}else{

		var prev=null;var clicked=false;
		$j(selector).each(function(idx,el){					 
			if(filter[0]==el && prev!=null){
				$j(prev).trigger('click');  clicked=true;
			}
			prev=el;
		});
		if(!clicked) $j(selector).last().trigger('click');
	}
}


function rf_editorExecCommand(id,cmd, param){
	 if (window.frames["frame_" + id] && window.frames["frame_" + id].editArea) {
	 	var ea = window.frames["frame_" + id].editArea;
	 	if (param != undefined) 
	 		ea.execCommand(cmd, param);
	 	else 
	 		ea.execCommand(cmd);
	 }			
}

function rf_editorSetPath(id,path){
	 if (window.frames["frame_" + id]) {
	 	$j("#toolbar_2 table.statusbar tbody tr td:nth-child(3)")
		$j("#toolbar_2 table.statusbar tbody tr :nth-child(4)",window.frames["frame_" + id].document).after("<td>&nbsp;"+path+"</td>");		 
	 }			
}

 
function rf_acefull(tobj) {
	
	var fid=$j(tobj).attr("fid");	
	var title=$j(tobj).attr("screentitle");
	var dom = require("ace/lib/dom");
	dom.toggleCssClass(document.body, "fullScreen")
	dom.toggleCssClass(ACE_EDITORS[fid].container, "fullScreen-editor")
	ACE_EDITORS[fid].resize();
	if($j("div#ace_fullscreenclose").length>0){
		$j("div#ace_fullscreenclose").remove();
	}else{
		$j("body").append("<div id='ace_fullscreenclose'  class='ace_fullscreenclose'><span></span><a href='#' fid='"+fid+"' onclick='rf_acefull(this);return false;'><img src='images/collapse_fullscreen.png'></a><small>[Esc]</small></div>");
		if(title!=null && title!=''){
			$j("div#ace_fullscreenclose span").text(title);
		}
	}
	ACE_EDITORS[fid].focus();
}
 
function rf_packinfoShow(pinfo) {	
	var respBack=function(rdata) {
		progress_message();
		if(rdata.pinfo!=null){
			
			var pack_id=rdata.package_id;
			
			if($j("div#rf_ptabs-"+pack_id).length==0){
				var htm=""; 
				htm+='<div id="rf_pstabs_'+pack_id+'"  class="rf_subtab" >';				
				htm+='	<ul>';
				htm+='			<li><a  href="#rf_pstabs1_'+pack_id+'">Info</a></li>';
				htm+='			<li><a href="#rf_pstabs2_'+pack_id+'">Themes</a></li>';
				htm+='			<li><a href="#rf_pstabs3_'+pack_id+'">Source Loader</a></li>';					
				htm+='		</ul>';
				htm+='		<div id="rf_pstabs1_'+pack_id+'">';				
				htm+='           <div class="package_info chromeScroll">';
				htm+='              <h3 class="rf_subheader">Package Repository</h3><div class="pack_repo"></div>';
				htm+='              <h3 class="rf_subheader">Peer Packages</h3><div class="peers_packtable"></div>';
				htm+='              <h3 class="rf_subheader">Dependencies</h3><div class="required_packages tags_panel"></div>';
				htm+='           </div>';	 		
				htm+='		</div>';
				htm+='		<div id="rf_pstabs2_'+pack_id+'">';
				htm+='        <div class="rf_tagspane chromeScroll" style="height:90%"></div>';			
				htm+='		</div>';
				htm+='		<div id="rf_pstabs3_'+pack_id+'">';		
				htm+='		    <div id="rf_pstabs3_sl_'+pack_id+'" class="sourceloader ace_editor"></div>';
		 		htm+='		</div>'; 
				htm+='</div>';		
				
				currentTabCotent=htm;				
				
				rf_editorTab.tabs("add", "#rf_ptabs-" + pack_id, pinfo);
				$j("#rf_pstabs_" + pack_id).tabs({
					selected: 0,
					select: function(event, ui){
						 
						if (typeof ui.panel.isLoaded == 'undefined' && ui.index == 1) {
							rf_packageMembInfo(ui);
							ui.panel.isLoaded = true;
						}
						 
					}
				});
						
				
			}else{
				var index=$j('#rf_tabs a[href="#rf_ptabs-'+  pack_id+'"]').parent().index();
				rf_editorTab.tabs('select', index);
				return ;
			}
		 
			var buildPtable=function(d,ttl){
				var htm="<table width='45%' cellpadding='5' cellspacing='2' border='1' style='margin-right:20px;float:left;border-collapse:collapse;'><tr><td style='background-color:#efefef;text-align:center;' colspan='2'>"+ttl+"</td></tr>";
				//var dd=d.split("\n");
				for(dd in d){
					if(dd=="Version" || dd=="Packaged" || dd=="Depends" ){
						var value=d[dd];
						value=(dd=="Packaged")?moment(value,"YYYY-MM-DD HH:mm:ss").fromNow():value;
						//value=(dd=="Depends")?"<div class='depedends'>"+value.replace(/,/g,', ')+"<div>":value;
						
						value=(dd=="Depends")?"<ul class='depedends'>"+(value+",").replace(/(.*?),/g,'<li>$1 </li>')+"</ul>":value;
						
						
						htm+="<tr><td>"+dd+"</td><td>"+value+"</td></tr>";
					}					 
				}
				htm+="</table>";
				$j("div#rf_ptabs-"+pack_id+" .pack_repo").append(htm);
			}
			$j("div#rf_ptabs-"+pack_id+" .pack_repo").html("");
			if(rdata.unix) buildPtable(rdata.unix,"Unix");
			if(rdata.win) buildPtable(rdata.win,"Windows");
			
			$j("div#rf_ptabs-"+pack_id+" .pack_repo .depedends").expander({
				  slicePoint: 100,
				  widow: 2,
				  expandText: '<img src="images/icon_expand.gif"  border="0">',
				  expandPrefix:"",
				  expandEffect: 'fadeIn',
				  expandSpeed: 1000,
				  collapseEffect: 'fadeOut',				  
				  collapseSpeed: 2000,
				  userCollapseText: '<img src="images/icon_collapse.gif" border="0">'
			});
			
			
			
			var htm="<table width='100%' border='1' cellspacing='2' cellpadding='5' style='border-collapse:collapse;border-color:#c0c0c0;'><tr><td style='background-color:#efefef;' width='90'>Version</td><td style='background-color:#efefef;' width='*'>Peers</td></tr>";
			for(ver in rdata.pinfo) {
				//htm+="<tr><td>"+ver+"</td><td>"+rdata.pinfo[ver]+"</td></tr>";
				var p1=rdata.pinfo[ver]+",";
				htm+="<tr><td>"+ver+"</td><td><ul class='peers'>"+p1.replace(/(.*?),/g,'<li>$1 </li>')+"</ul></td></tr>";
				//value=(dd=="Depends")?"<ul class='depedends'>"+value.replace(/(.*?),/g,'<li>$1 </li>')+"</ul>":value;
				
			}
			$j("div#rf_ptabs-"+pack_id+" .peers_packtable").html(htm);			
			
			//var htm="<div style='display:inline-block;height:360px;margin-bottom:20px;padding:10px;'><div class='tag_box div_available'><ul class='task_tag_dialog_ul task_tag_available chromeScroll'>";
			var removeOpt="";
			var resetOpt="";
			if(rdata.access!=null && rdata.access=='RWX') {
                 removeOpt="<img onclick='rf_removeDraggedItem(this)' class='removeBtn' src='images/remove.png' border='0'>";
                 resetOpt="<a style='float:right;' href='#' onclick='rf_resetRequiredPack(this)' packageid='"+pack_id+"' packagename='"+pinfo+"' title='Reset to all function in the group and folders above in current order'><img src='images/refresh.png'></a>";
                 
            }
                    	 
			var htm="<div style='display:inline-block;height:360px;margin-bottom:20px;padding:10px;'><div class='tag_box div_available'><ul class='task_tag_dialog_ul task_tag_available chromeScroll'>";
		    var owner="<div class='tag_box'><div class='tag_title'>Required Packages "+resetOpt+" </div><ul class='task_tag_dialog_ul task_tag_owner chromeScroll'>";		    
		    for(ic=0;ic<rdata.folders.length;ic++){					
					//htm+="<li item_id='"+pack_id+"' tag_id='"+rdata.folders[ic].id+"' class='task_tag_dnd task_tag_dialog'>"+rdata.folders[ic].folder_name+"</li>";
		    	  if(rdata.folders[ic].folder_name!='Trash' && rdata.folders[ic].id!=pack_id){	    	    
		    	       	       
		    	    var sel="<li item_id='"+pack_id+"' tag_id='"+rdata.folders[ic].id+"' class='task_tag_dnd task_tag_dialog'>"+rdata.folders[ic].folder_name+removeOpt+"</li>";
					if($j.inArray(rdata.folders[ic].id,rdata.required_packages)>=0){			
						owner+=sel;				 
					}else{
						htm+=sel;						
					}
		    	  }
			}
		    		    
		    htm+="</ul></div>";
		    owner+="</ul></div></div>";
		    var tbdiv="div#rf_ptabs-"+pack_id;
		    $j(tbdiv+" .required_packages").html(htm+owner);		
		    
		    if(rdata.access!=null && rdata.access=='RWX') {
                $j(tbdiv+" .tags_panel .task_tag_available, "+tbdiv+" .tags_panel .task_tag_owner").sortable({              
                       connectWith: ".task_tag_dialog_ul",
                       placeholder: "task_tag_placeholder",              
                       receive: function(event, ui) {
                           rf_packageSelectedSync(ui.item.attr("item_id"));                    
                       }        
                });		        
		    }else{
		        $j(tbdiv+" .required_packages .div_available").hide();
		    }  	
		    
		    $j(tbdiv+" .required_packages .tag_title [title]").qtip();
		    
				 
						
			$j(tbdiv+" .task_tag_available,"+tbdiv+" .task_tag_owner").disableSelection();
			
			if(rdata.sourceloader!=null) {
				  $j(tbdiv+" .sourceloader").html(rdata.sourceloader);
			}
			
			
			var ea_id="rf_pstabs3_sl_"+pack_id;
			if(ACE_EDITORS[ea_id]!=null){
				ACE_EDITORS[ea_id].destroy();
				$j("#"+ea_id).children().remove();
			}	
			var tm = function(ea_id,fieldval1){
				$j("#"+ea_id).show();						 
				ACE_EDITORS[ea_id]= ace.edit(ea_id);
				ACE_EDITORS[ea_id].getSession().setValue(fieldval1);
				var is_readonly=true;
				setACE_Default(ACE_EDITORS[ea_id],is_readonly);						
			}
			setTimeout(tm,500,ea_id,(rdata.sourceloader!=null?rdata.sourceloader:""));
			 
			
			/*
			var tbl=$j("#packgeInfoDialog .peerpackinfo");			
			var htm="<table width='95%' border='1' cellspacing='0' cellspacing='3' style='border-collapse:collapse;'><tr><td style='background-color:#efefef;' width='90'>Version</td><td style='background-color:#efefef;' width='*'>Peers</td></tr>";
			for(ver in rdata.pinfo) {
				htm+="<tr><td>"+ver+"</td><td>"+rdata.pinfo[ver]+"</td></tr>";
			}
			htm+="</table>";
			tbl.html(htm);
			*/
			
			
			
		}

		
	}
	progress_message('Please wait...');
	RFunctionMgmt.getPackageInfo(pinfo,respBack);
	
	
}


function rf_packageMembInfo(ui){
	//console.log(ui);
	var pid=ui.panel.id.replace("rf_pstabs2_","");
	
	var respBack=function(data){
		if(data!=null){
			var htm="";
			htm+="<div class='rf_tagstable'><div class='rf_tagstable-row'>"; 
    		htm+="<div class='rf_tagstable-cell'><div class='tag_box div_available'><ul class='task_tag_dialog_ul task_tag_available chromeScroll'></ul></div></div>"; 
    		htm+="<div class='rf_tagstable-cell'>";
			htm+="<div class='rf_owntagspane'>";
            htm+=" <h3 class='rf_subheader'>Package Themes & Notification</h3>";
            htm+=" <div class='rf_membtab'>";            
            //htm+="   <div class='rf_memtags'><div class='tag_box div_available'><ul class='task_tag_dialog_ul package_tag_available chromeScroll'></ul></div></div>";
            htm+="   <div class='rf_membaction'>"
            htm+="       <div class='tag_box'><div class='tag_title'>Themes</div><ul package_id='"+pid+"' class='task_tag_dialog_ul package_tag_theme '></ul></div>";             
            htm+="       <div class='tag_box'><div class='tag_title'>Notification</div><ul package_id='"+pid+"' class='task_tag_dialog_ul package_tag_notification '></ul></div>";
            htm+="   </div>";
            htm+=" </div>";       
            htm+="</div>";            
			htm+="<div class='rf_memberspane'>";
			htm+=" <h3 class='rf_subheader'>Function Tags (Batch Update)</h3>";
			htm+=" <div class='rf_membtab'>";
			//htm+="   <div class='rf_memtags'><div class='tag_box div_available'><ul class='task_tag_dialog_ul task_tag_available chromeScroll'></ul></div></div>";
			htm+="   <div class='rf_membaction'>"
			htm+="       <div class='tag_box'><div class='tag_title'>Over-write Owner</div><ul class='task_tag_dialog_ul task_tag_owner '></ul></div>";				
			htm+="       <div class='tag_box'><div class='tag_title'>Add Tag</div><ul class='task_tag_dialog_ul task_tag_add '></ul></div>";
			htm+="       <div class='tag_box'><div class='tag_title'>Remove Tag</div><ul class='task_tag_dialog_ul task_tag_remove '></ul></div>";
			htm+="   </div>";
			htm+="   <div class='rf_notiaction'>"							
			htm+="       <div class='tag_box'><div class='tag_title'>Add Notification Tag</div><ul class='task_tag_dialog_ul task_noti_add '></ul></div>";
			htm+="       <div class='tag_box'><div class='tag_title'>Remove Notification Tag</div><ul class='task_tag_dialog_ul task_noti_remove '></ul></div>";
			htm+="   </div>";
			htm+=" </div>";			
			htm+="</div>";
			
			htm+="</div></div></div>"; //table & row & cell
			
			
			$j("div#rf_ptabs-"+pid+" .rf_tagspane").html(htm);
			for(ic=0;ic<data.tags.length;ic++){
				if(data.tags[ic]!=null && data.tags[ic].tagname!=null && data.tags[ic].tagname.indexOf("thm-")>=0){				
					var tag=data.tags[ic].tagname.replace(/^thm-(.*)$/g,"$1");
					var sel="<li item_id='"+pid+"' tag_id='"+data.tags[ic].id+"' class='task_tag_dnd task_tag_dialog'>"+tag+"</li>";
					$j("div#rf_ptabs-"+pid+" .rf_tagstable .task_tag_available").append(sel);
					//$j("div#rf_ptabs-"+pid+" .rf_owntagspane .package_tag_available").append(sel);
					
                   if(data.notification_tags.indexOf(data.tags[ic].id)>=0 || data.theme_tags.indexOf(data.tags[ic].id)>=0){
                       var ulid=null;
                       if(data.theme_tags.indexOf(data.tags[ic].id)>=0) ulid=".package_tag_theme";
                       if(data.notification_tags.indexOf(data.tags[ic].id)>=0) ulid=".package_tag_notification";
                       var removebtn="";
                       if(data.access!=null && data.access=='RWX') {                       
                         removebtn='<img onclick="rf_removeDraggedTheme(this)" class="removeBtn" src="images/remove.png" border="0">';
                       }
                       var sel="<li item_id='"+pid+"' tag_id='"+data.tags[ic].id+"' class='task_tag_dnd task_tag_dialog'>"+tag+removebtn+"</li>";                       
                       $j("div#rf_ptabs-"+pid+" .rf_owntagspane "+ulid).append(sel);
                    }


					//data.theme_tags.
					
				}
			}
			
			if(data.access!=null && data.access=='RWX') {      
			
    			$j("div#rf_ptabs-"+pid+" .rf_tagstable .task_tag_available, div#rf_ptabs-"+pid+" .rf_owntagspane .task_tag_dialog_ul").sortable({                
                       connectWith: ".task_tag_dialog_ul",
                       placeholder: "task_owerwrite_placeholder",      
                       out:function(event,ui){ if(!$j(event.target).hasClass("task_tag_available"))  $j(event.target).removeClass("focussed"); },
                       over:function(event,ui){ if(!$j(event.target).hasClass("task_tag_available")) $j(event.target).addClass("focussed");} ,
                       update:function(event,ui){
                         if($j(event.target).hasClass("task_tag_available")) {  
                             $j(this).sortable('cancel');
                             //console.log($j(event.target).attr("class"));                        
                         } else{
                             var tag_id=$(ui.item).attr("tag_id");
                             if($j(event.target).find("li[tag_id='"+tag_id+"']").length==0){
                                $(ui.item).clone().append('<img onclick="rf_removeDraggedTheme(this)" class="removeBtn" src="images/remove.png" border="0">').appendTo($j(event.target));   
                                rf_syncPackageThemes(event.target);
                             }
                         }
                       }
                });                
    			//sorting for memeber tag assingment 
    			$j("div#rf_ptabs-"+pid+" .rf_memberspane .task_tag_dialog_ul").sortable({				
    				   connectWith: ".task_tag_dialog_ul",
    				   placeholder: "task_owerwrite_placeholder",
    				   receive: function(event, ui) {
    					   var tag_id=$j(ui.item).attr("tag_id");
    					   var pid=$j(ui.item).attr("item_id");
    					   var tag=$j(ui.item).text();
    					   var act=""; 
    					   if($j(event.target).hasClass("task_tag_owner")) act="overwrite_owner";
    					   if($j(event.target).hasClass("task_tag_add")) act="add_tag";		   
    					   if($j(event.target).hasClass("task_tag_remove"))  act="remove_tag";
    					   if($j(event.target).hasClass("task_noti_add"))  act="add_noti";
    					   if($j(event.target).hasClass("task_noti_remove"))  act="remove_noti";    					   
    					   if(act!=''){
    						   rf_memAction(pid,tag_id,act,tag);
    					   }
    					   //return false;				   
    			       },
    			  	   update:function(event,ui){
    			  		 if($j(event.target).hasClass("task_tag_available"))   $j(this).sortable('cancel');
    			       }
    			});
			}//if(data.access!=null && data.access=='RWX') 
			else{
			   $j("div#rf_ptabs-"+pid+" .rf_tagstable .rf_tagstable-row .rf_tagstable-cell:first-child").hide();
			   $j("div#rf_ptabs-"+pid+" .rf_tagstable .rf_tagstable-row .rf_tagstable-cell:last-child").css("width","99%").css("padding-top","30px");
			   $j("div#rf_ptabs-"+pid+" .rf_tagstable .rf_tagstable-row .rf_tagstable-cell:first-child").hide();
			   $j("div#rf_ptabs-"+pid+" .rf_tagstable .rf_memberspane").hide();
			   
			}
		}
	}
	
	RFunctionMgmt.getPackageMembersInfo(+pid,respBack);
}

function rf_syncPackageThemes(tobj){
    
    
    var cl="";
    if($j(tobj).hasClass("package_tag_theme")) cl="folder_tags";
    if($j(tobj).hasClass("package_tag_notification")) cl="folder_followtags";
    
    if(cl!=''){        
        var tids=[];
        $j(tobj).find("li[tag_id]").each(function(idx,el){
            tids.push(+$j(this).attr("tag_id"));
        });
        var p_id=$j(tobj).attr("package_id");
        //int pack_id, List tagids, String tblname
        var respBack=function(data){
            progress_message();
            if(data!=null && data){
                message_status("Package info update");
            }
        }
        progress_message("Please wait while updating...");
        RFunctionMgmt.updatePackageThemes(+p_id,tids,cl,respBack);    
        //console.log("action:"+cl+" ids:"+tids.toString());
    }
    
}

function rf_removeDraggedTheme(tobj){
    var pid=$j(tobj).parent().attr("tag_id");
    //alert(pid);
    var parent0=$j(tobj).parents(".task_tag_dialog_ul")[0];
    $j(tobj).parent().fadeOut(600, function(){ 
        $j(this).remove();
        rf_syncPackageThemes(parent0);
    });
    
    
}


function rf_memAction(pid,tag_id,act,tag){
	
	 $j( "#dialog_packageAction").dialog({
			resizable: false,			 
			width:700,
			modal: true,		
			dialogClass: 'comment_dialog',		
			buttons: {
				"Cancel": function() {
					$j( this ).dialog( "close" );
				},	 				
				"Continue": function() {
					//console.log("action:"+act+" pid:"+pid+" tag_id:"+tag_id);
					var function_ids=[];
					$j("#dialog_packageAction .ul_list ul li[function_id]").each(function(idx,el){
						function_ids.push(+$j(el).attr("function_id"));
					});
					//console.log(function_ids);
					//return;
					
					var respBack=function(data){
						progress_message();
						if(data!=null && data)message_status("Member functions updated successfully");						
					}
					progress_message('Please wait...');
					if(function_ids.length>0){
						RFunctionMgmt.packageMemberAction(+pid,+tag_id,function_ids,act,respBack);				
					}else{
						message_status("Failed, Required minimun 1 function to complete this action!")
					}
					$j( this ).dialog( "close" );
				}
			}
	});
	var msg="";
	if(act=="overwrite_owner") msg="Dragged tag <span class='tag'>"+tag+"</span> will be over-written as owner for the following functions";
	if(act=="add_tag") msg="Dragged tag <span class='tag'>"+tag+"</span> will be added the following functions ";
	if(act=="remove_tag") msg="Dragged tag <span class='tag'>"+tag+"</span> will be removed from the following functions ";
	if(act=="add_noti") msg="Dragged tag <span class='tag'>"+tag+"</span> will be notified on changes of the following functions";
	if(act=="remove_noti") msg="Dragged tag <span class='tag'>"+tag+"</span> won't be notified on changes of the following functions";
	
	
	$j("#dialog_packageAction .label").html(msg); 
	$j("#dialog_packageAction .ul_list ul").children().remove();	
	$j("#rf_treeMenu ul[foldername][folderid='"+pid+"'] li").clone().each(function(idx,el){
		$j(el).find(".treeFunctionItem").append('<a  class="removeBtn" href="#" onclick="rf_removeTagSelected4Package(this);return false;"><img border="0" src="images/remove.png" border="0"></a>');
	}).appendTo("#dialog_packageAction .ul_list ul");
	
} 

function rf_removeTagSelected4Package(thisobj) {
	$j(thisobj).parents("li[function_id]").animate({width:0}, 300,"linear",function(){ $j(this).remove();  });
}

function rf_removeDraggedItem(tobj) {
	var pid=$j(tobj).parent().attr("item_id");
	var tbdiv="div#rf_ptabs-"+pid;	
	$j($j(tobj).parent()).appendTo(tbdiv+" .task_tag_available");
	//to_minusTag(tobj);
	rf_packageSelectedSync(pid);
}



function rf_packageSelectedSync(pid){
	var selected=[];
	$j("div#rf_ptabs-"+pid+" .task_tag_owner li[tag_id]").each(function(idx,elm){
		selected.push(+$j(elm).attr("tag_id"));
	});
	//alert(selected);
	var respBack=function(data){
		if(data)  message_status("Package dependencies updated")
	}
	progress_message('Please wait...');
	RFunctionMgmt.syncPackageHirDep((+pid),selected,respBack);
}


function rf_resetRequiredPack(tobj){
	if(!confirm("Are you sure you wish to reset?")) return;
		
	
	var pname=$j(tobj).attr("packagename");
	var pid=$j(tobj).attr("packageid");
	var respBack=function(data){		
		progress_message();
		if(data!=null){
			var tbdiv="div#rf_ptabs-"+pid;			
			$j(tbdiv+" .task_tag_owner li").each(function(idx,el){
				$j(el).appendTo(tbdiv+" .task_tag_available");				
			});
			for(iac=0;iac<data.length;iac++){
				$j(tbdiv+" .task_tag_available li[tag_id='"+data[iac]+"']").appendTo(tbdiv+" .task_tag_owner");
			}
			
		}
		
	}
	progress_message('Please wait...');
	RFunctionMgmt.resetPackageHirDep(pname,respBack);
}

function rf_flagEdited(fn_id) {
	$j("div#rf_tabs > ul a[href='#rf_tabs-"+fn_id+"']").parent().addClass("ace_edited");	
}

function setACE_Default(editor,ro_flag) {
	
	editor.on('change',function(obj){
		
		var fn_id=rf_getActivePanelID();
		rf_flagEdited(fn_id);
		//console.log('editor.change()');
	})

	//editor.getSession().on('change',function(obj){
	//	console.log('session.change()');
	//})

	
	editor.setTheme("ace/theme/chrome");
	editor.getSession().setMode("ace/mode/r");					
	editor.setShowPrintMargin(false);
	editor.setFontSize("10pt");	
	 
    
    
	//editor.setFadeFoldWidgets();
	if(ro_flag!=null){
		editor.setReadOnly(ro_flag);
	}
	editor.session.setFoldStyle("markbegin");
    editor.setShowFoldWidgets(true);
    
    /*
	editor.commands.addCommand({
		name: 'myCommand',
		bindKey: {win: 'Ctrl-1',  mac: 'Command-1'},
		exec: function(editor) {
			var start=editor.getSelectionRange().end;
			var range=editor.session.getWordRange(start.row,start.column);
			if(range!=null && range.start!=null){
				var token=editor.session.getTokenAt(range.start.row,range.start.column+1);			
				if(token.type=='userfunction'){
					var func=editor.session.getTextRange(range);					
					//scd_openRFunction(func);					
					window.open('./r.jsp?open_functions='+func);
				}else{					
				    //openRDoc(editor.session.getTextRange(range));
					alert("Function "+editor.session.getTextRange(range)+" is not available ");
				}
			}
			
		},
		readOnly: true // false if this command should not apply in readOnly mode
	});
	*/
	editor.commands.addCommand({
		name: 'save',
		bindKey: {win: 'Ctrl-S',  mac: 'Command-S'},
		exec: function(editor) {
			saveRFunction();
		},
		readOnly: false // false if this command should not apply in readOnly mode
	});
 
	editor.commands.addCommand({
		name: 'ctrlclick',
		bindKey: {win: 'Ctrl-DOWN',  mac: 'Command-DOWN'},
		exec: function(editor) {
			 alert("clicked");
		},
		readOnly: true // false if this command should not apply in readOnly mode
	});
	
	editor.commands.addCommand({
		name: 'fullscreen',
		bindKey: {win: 'Esc',  mac: 'Esc'},
		exec: function(editor) {
			//alert('escape')
			if($j(editor.container).hasClass('fullScreen-editor')){
				var anc="<a fid='"+$j(editor.container).attr("id")+"' screentitle=''></a>";
				rf_acefull($j(anc));
			}
			
			
		},
		readOnly: true // false if this command should not apply in readOnly mode
	});
	
	/*
	editor.commands.addCommand({
		name: 'codeassist',
		bindKey: {win: 'Ctrl-Space',  mac: 'Command-Space'},
		exec: function(editor) {                 
			var pos=editor.getCursorPosition();			
			var token=editor.session.getTokenAt(pos.row,pos.column);
			//console.log(token);			
			rf_codeAssist(editor,token);
			
		},
		readOnly: false // false if this command should not apply in readOnly mode
	});
	*/
	
	editor.on("click",function(ev){
		if(ev.domEvent!=null && ev.domEvent.ctrlKey ){
			//alert('clicked');
			var token=ev.editor.session.getTokenAt(ev.$pos.row,ev.$pos.column);
			if(token.type=='userfunction' && token.value!=null && token.value!=''){
				if(typeof rf_open=='function'){
					rf_open(token.value);
				}else{
					window.open('./r.jsp?open_functions='+token.value);
				}
				
			}
		}
	});
   
}

 
 
 
function listener(event){
   if(typeof event.data =='string' && event.data.indexOf("open_function:")>=0){
	   rf_open(event.data.split(":")[1]);
   }
   if(typeof event.data=='object' &&  event.data.answer!=null &&  event.data.version!=null) {
	   //console.log(event.data);
	   $j("body").attr("ext_ver",event.data.version);
   }
}
addEventListener("message", listener, false)

	