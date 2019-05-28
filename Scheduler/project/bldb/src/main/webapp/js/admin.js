
function showLogin(){
	//$("username").value="";
	//$("adminpwd").value="";
	$("loginPane").show();
	var d=document.createElement('div');	
	$("loginstatusPane").style.zIndex="200";
	$("loginstatusPane").hide();
	return false;
}


var dateElemFilter="adm_datefilter";
function setCurrentDateFilter(elem){
	dateElemFilter=elem;
}



function loginAdmin(){
	var respback=function(data){
		if(data!=null && typeof data=='object'){
			$("loginstatusPane").show();
			$("loginPane").hide();
			$("username").value="";
			$("adminpwd").value="";
			
			if(data.success!=null && data.success){
				loggedInPane(true);
			}else{
				if(data.message!=null){									
					message_status(data.message);
				}
			}
			
		}
	}
		
	var login=$("username").value;
	var pwd=$("adminpwd").value;
	MigrationMgmt.loginAsAdmin(login,pwd,respback);
	return false;
}

function loggedInPane(loggiedinFlag){
	window.location.reload();
	if (false) {
		if (loggiedinFlag) {
			var anc = document.createElement('a');
			anc.href = "#";
			anc.onclick = changePwdAdmin;
			anc.appendChild(document.createTextNode('Change Password'));
			
			var anc1 = document.createElement('a');
			anc1.href = "#";
			anc1.appendChild(document.createTextNode('Logout'));
			anc1.onclick = logoutAdmin;
			
			$("loginstatusPane").innerHTML = "";
			$("loginstatusPane").appendChild(anc);
			var txt = document.createTextNode(" | ");
			$("loginstatusPane").appendChild(txt);
			$("loginstatusPane").appendChild(anc1);
		} else {
			var anc = document.createElement('a');
			anc.href = "#";
			anc.onclick = showLogin;
			anc.appendChild(document.createTextNode('Login As Admin'));
			$("loginstatusPane").innerHTML = "";
			$("loginstatusPane").appendChild(anc);
		}
	}
} 


function showChangePwdAdmin(){
		$("adminpwd1").value="";
		$("adminpwd2").value="";
		$("adminpwd3").value="";
		$("changePwdPane").show();	
		
		return false;
}

function changePwdAdmin(){	
	if($("adminpwd1").value==""){
		alert("New password is empty");
		return false;
	}
	
	if($("adminpwd2").value!=$("adminpwd3").value){
		alert("Both new passwords are not exactly the same");
		return false;
	}	
	var respback=function(data){
		if(data!=null && data){
			$("changePwdPane").hide();
			message_status("Your password has been changed now")
		}
	}	
	 
	AdminMgmt.changePassword($("adminpwd1").value,$("adminpwd2").value,respback); 
	return false;
}
function logoutAdmin() {
	var respback = function(data){
		if (data != null && typeof data == 'object') {
			try{
			message_status("You have been logout");
			}catch(e){}
			loggedInPane(false);
		}
	}
	MigrationMgmt.logoutAsAdmin(respback);
}


function adm_fieldclick(tobj, flag_dontcloseed) {
	//alert(tobj.getAttribute('db')+":"+tobj.getAttribute('myNodeId'));
	
	var  fieldname1=tobj.getAttribute("fieldname");
	var  commodity1=tobj.getAttribute("commodity");
	
	var respBack = function(data){
		progress_message(null);
		if (data.contracts != null) {
			$("adm_field_pane").show();
			if (flag_dontcloseed != null && flag_dontcloseed) {
			}
			else {
				$("adm_DataSourceEditor").hide();
			}
			if ($("adm_field_pane_bd") != null) {
				$("adm_field_pane_hd").innerHTML = commodity1 + "->" + fieldname1;
			}
			
			
			var children=$("adm_field_pane_bd").childElements();			 
			for (intb = 0; intb < children.length; intb++) {
				children[intb].remove();
			}
			
			var dv=document.createElement('div');
			dv.style.height="700px";
			dv.style.overflow="auto";
			dv.style.width="500px";
			var tbl=document.createElement("table");
			tbl.setAttribute("border","1px");
			tbl.style.width="100%";
			tbl.style.borderCollapse="collapse";			
			
			tbl.style.fontSize="1em";
			tbl.style.zIndex="200";
			
			var insertRw=function(crow,arry,hdrflg){
				for(iad=0;iad<arry.length;iad++){
					var cl=crow.insertCell(crow.cells.length);
					if (typeof arry[iad] == 'string' || typeof arry[iad] == 'number' ) {
						cl.appendChild(document.createTextNode(arry[iad]));
					}else if(typeof arry[iad] == 'object'){
						cl.appendChild(arry[iad]);
					}
					cl.style.padding="2px";
					if (hdrflg != null && hdrflg == true) {
						cl.style.backgroundColor = "#c0c0c0";
						cl.setAttribute("align","center");
					}else{
						if (iad == 1) {
							cl.setAttribute("align","right");
						}
					}
				}
			}			
			insertRw(tbl.insertRow(0),["Ticker","Records","Date Range","",""],true);
			
			
			for(iac=0;iac<data.contracts.length;iac++){
				//var lstitm=document.createElement("li");
				//lstitm.appendChild(document.createTextNode(data.contracts[iac]));
				//lst.appendChild(lstitm);
				var cont1=data.contracts[iac].contract;
				var anc= document.createElement('a');
				anc.href="#";
				anc.setAttribute('contract',cont1);
				anc.appendChild(document.createTextNode('Remove'));
				anc.onclick=function(){
															
					//alert(tobj.getAttribute("commodity"));
					//alert(tobj);
					//alert(this.getAttribute('contract'));
					//alert(tobj.respBack);
					
					if(confirm("You're about to remove all records related to this contract! Are you sure?")){
						AdminMgmt.deleteContract(tobj.getAttribute('db'),tobj.getAttribute('myNodeId'),this.getAttribute('contract'),tobj.respBack);	
					}
				}
				var anc1= document.createElement('a');
				anc1.href="#";
				anc1.setAttribute('contract',cont1);
				anc1.appendChild(document.createTextNode("Preview"));				
		
				
				$("adm_datefilterbtn").setAttribute("db",tobj.getAttribute('db'));
				$("adm_datefilterbtn").setAttribute("fieldtable",tobj.getAttribute('myNodeId'));
				$("adm_datefilterbtn").setAttribute("contract",cont1);
				
				$j("#adm_DataSourceAddBtn").attr("db",tobj.getAttribute('db'));
				$j("#adm_DataSourceAddBtn").attr("fieldtable",tobj.getAttribute('myNodeId'));
				$j("#adm_DataSourceAddBtn").attr("contract",cont1);
			 
				
									
				anc1.onclick=function(){
															
						//alert(tobj.getAttribute("commodity"));
						//alert(tobj);
						//alert(this.getAttribute('contract'));
						//alert(tobj.respBack);
						//var cont2=cont1;

			
						setCurrentDateFilter("adm_datefilter");
													
						//var proxydb=tobj.getAttribute('db');
						//var proxynid=tobj.getAttribute('myNodeId');
						//var proxycont=this.getAttribute('contract')
						
						
						var proxydb=$j("#adm_DataSourceAddBtn").attr("db");
						var proxynid=$j("#adm_DataSourceAddBtn").attr("fieldtable");
						var proxycont=$j("#adm_DataSourceAddBtn").attr("contract");
				
				        var proxycont1=this.getAttribute('contract');
						
						var proxy_respo=function(data){
							//var cont2=$("adm_datefilterbtn").getAttribute("contract");
							adm_buildRecordEditor("adm_dataPreviewPanel",data,proxydb,proxynid,proxycont1);
							progress_message(null);
						}
						//AdminMgmt.deleteContract(tobj.getAttribute('db'),tobj.getAttribute('myNodeId'),this.getAttribute('contract'),tobj.respBack);

						var dobj=getDateInputs(dateElemFilter,"cdate "); 
						if (dobj.query == null) {
							  alert("Please select valid period");
							  return false;
						}
						progress_message("Please wait...");			
						$("adm_datefilterbtn").setAttribute("contract",cont1);				
						AdminMgmt.getCommodityRawData(dobj.query,tobj.getAttribute('db'),tobj.getAttribute('myNodeId'),this.getAttribute('contract'),proxy_respo)
						//alert(tobj.getAttribute('db')+":"+tobj.getAttribute('myNodeId')+":"+this.getAttribute('contract'));	
					
					
				}
				insertRw(tbl.insertRow(iac+1),[data.contracts[iac].contract,data.contracts[iac].recs,data.contracts[iac].dates,anc,anc1]);
			
			}
	
			var removeBtn=document.createElement('input');
			removeBtn.setAttribute('type','button');
			removeBtn.setAttribute('value','Remove '+fieldname1);
			removeBtn.onclick=function(){
				if(currentFieldNode!=null){				
					if (currentFieldNode.data.myNodeId == tobj.getAttribute('myNodeId') && confirm("Above contracts will be removed as well!!! Are you sure you wish to remove a field?")) {
						progress_message("Please wait....");
						AdminMgmt.removeFieldTable(currentFieldNode.data.db, currentFieldNode.data.myNodeId, function(data){
							progress_message(null);
							if (data != null && data) {								
								message_status("Field has been removed from the database")
								tree.removeNode(currentFieldNode, true);
								$("adm_field_pane").hide();
							}
						});
					}
				}
			}

			dv.appendChild(tbl);
			dv.appendChild(document.createElement('br'));
			dv.appendChild(document.createElement('br'));
			dv.appendChild(removeBtn);	
			
			var renameBtn=removeBtn.cloneNode(true);
			renameBtn.setAttribute('value','Rename '+fieldname1);
			renameBtn.onclick=function(){
				if(currentFieldNode!=null){				
					if (currentFieldNode.data.myNodeId == tobj.getAttribute('myNodeId') && confirm("Are you sure you wish to rename this field?")) {
						var fname=prompt("Enter Fieldname:", fieldname1);
						if (fname != null && fname != '') {
							progress_message("Please wait....");
							AdminMgmt.renameFieldTable(currentFieldNode.data.db, currentFieldNode.data.myNodeId, fname, function(data){
								progress_message(null);
								if (data != null && data) {
									message_status("Field has been renamed in the database, Please referesh this page!")
									//tree.removeNode(currentFieldNode, true);
									//tree.set
									var el = $(currentFieldNode.getContentEl()).select("a[mynodeid='" + currentFieldNode.data.myNodeId + "']");
									if (el.length > 0) {
										$(el[0]).innerHTML = fname;
										$(el[0]).setAttribute('fieldname', fname);
									}
									$("adm_field_pane").hide();
								}
							});
						}
					}
				}
			}
			
			
			dv.appendChild(renameBtn);
			if ($("adm_field_pane_bd") != null) {
				$("adm_field_pane_bd").appendChild(dv);
			}
			
			if (previousepane != null) {
				rightpane[previousepane].hide();
			}
		}
	}
	
	progress_message("Please wait...");
	tobj.respBack=respBack;
	
	AdminMgmt.getFieldDetails(tobj.getAttribute('db'),tobj.getAttribute('myNodeId'),respBack)
	return false;
}


function adm_filterRecordEditor(thisobj){
 
 
    //var tablename1=thisobj.getAttribute('myNodeId');  
	//var dbname1=thisobj.getAttribute('db');
	var db1=$j("#adm_DataSourceAddBtn").attr("db");
	var fieldtable1=$j("#adm_DataSourceAddBtn").attr("fieldtable");
	var contract1=$j("#adm_DataSourceAddBtn").attr("contract");
	var prevpane=$j("#adm_DataSourceAddBtn").attr("prevpane");
	
	var proxy_respo=function(data){		
		adm_buildRecordEditor( ((prevpane!=null)?prevpane:"adm_dataPreviewPanel") ,data,db1,fieldtable1,contract1);
		progress_message(null);
	}
	
		var dobj=getDateInputs(dateElemFilter,"cdate "); 
	if (dobj.query == null) {
		  alert("Please select valid period");
		  return false;
	}
	progress_message("Please wait...");			
	
	var db1=$j("#adm_DataSourceAddBtn").attr("db");
	var fieldtable1=$j("#adm_DataSourceAddBtn").attr("fieldtable");
	var contract1=$j("#adm_DataSourceAddBtn").attr("contract");
	
	AdminMgmt.getCommodityRawData(dobj.query,db1,fieldtable1,contract1,proxy_respo)

}


function processCommoditiesTree(data,mainnode,key){
			if(data[key]!=null){
				
				for(comm in data[key]){
					 var msec1a = new YAHOO.widget.TextNode({ label: comm , myNodeId:comm,db:key ,commodity:true,commodityname:comm }, mainnode);
					 msec1a.labelStyle = "icon-table"; 
					 for(iab=0;iab<data[key][comm].length;iab++ ){
					 		var obj=data[key][comm][iab];
					 	 	
							//var ms_com1=new YAHOO.widget.TextNode({ label: obj.value,db:key,myNodeId:obj.key,field:true  }, msec1a);
							
							var ms_com1=new YAHOO.widget.HTMLNode({ 
								html:"<img src='images/fieldicon.jpg' style='margin-right:10px'><a style='font-size:.8em' href='#' name='"+key+obj.key+"' onclick='adm_fieldclick(this)' fieldname='"+obj.value+"' commodity='"+comm+"' db='"+key+"' myNodeId='"+obj.key+"' >"+obj.value+"</a>",
								db:key,
								myNodeId:obj.key,
								fieldname:obj.value,
								field:true  
							}, msec1a);
							
							ms_com1.labelStyle = "icon-field";   
					 }
				}
				
			}
			
			/*
			 if(data.market!=null){
				
				for(comm in data.market){
					 var msec1a = new YAHOO.widget.TextNode({ label: comm , myNodeId:comm,db:"market" ,commodity:true }, msec1);
					 msec1a.labelStyle = "icon-table"; 
					 for(iab=0;iab<data.market[comm].length;iab++ ){
					 		var obj=data.market[comm][iab];
					 	 	var ms_com1=new YAHOO.widget.TextNode({ label: obj.value,db:"market",myNodeId:obj.key,field:true  }, msec1a);
							ms_com1.labelStyle = "icon-field";   
					 }
				}
				
			}
			 */
}


function adm_fixDb(thisobj){
	 
		var ta=tree.getNodesByProperty("myNodeIdS",thisobj.getAttribute('nodeid'));
		//alert(ta.length);
		var nodeId=null;
		for (ia = 0; ia < ta.length; ia++) {
			ta[ia].expanded=!ta[ia].expanded;	
			nodeId=ta[ia].data.myNodeId;		
		}
		if(nodeId!=null && confirm("Are you sure you like to fix the database")){
			var respBack=function(data){
				message_status("Database error have been fixed")
				refreshDBStruct(data);
				
			}
			progress_message("Please wait while fixing the database errors")
			AdminMgmt.deleteOrphans(nodeId,respBack);	
		}
		return false;
}
					

var rightpane=new Array();
var  lastTreeNodeContentEL=null;
var previousepane=null;
var currentFieldNode=null;
var tree=null;
function refreshDBStruct(resdata){
	
	var respBack = function(data){
		if (data != null) {

		  if (tree == null) {
		  	tree = new YAHOO.widget.TreeView("dbtree");	
		  }else{
		  	tree.removeChildren(tree.getRoot());
		  }						
		  var root = tree.getRoot();			

		  rightpane=null;
		  rightpane=new Array();
			
		  if (data.db != null && data.order!=null) {
			 
				for(iac=0;iac<data.order.length;iac++){
					
					var db_lbl=data.db[data.order[iac]];
					var db_style="icon-db";
					
					if (data[[data.order[iac] + "_err"]] != null && data[[data.order[iac] + "_err"]].length>0) {
						db_lbl="<img src='images/dbicon_err.JPG' style='margin-right:6px'><span style='cursor:pointer'>"+data.db[data.order[iac]]+"</span><a href='#' style='margin-left:10px' nodeid='"+data.order[iac]+"' onclick='adm_fixDb(this);'><small style='color:#FF0000'>Fix("+data[[data.order[iac] + "_err"]].length+")</small></a>";
					}else{
						db_lbl="<img src='images/dbicon.jpg' style='margin-right:6px'><span style='cursor:pointer'>"+data.db[data.order[iac]]+"</span>";
					} 	
					 
					var msec1 = new YAHOO.widget.HTMLNode({						
						html: db_lbl,						 
						myNodeId: data.order[iac],
						myNodeIdS: data.order[iac]+"",
						
						root: true
					}, root);

					msec1.labelStyle=db_style;
                 
					var labl=msec1.label;
					var dbid=msec1.data.myNodeId;
					
					var pid=data.dbpane[dbid];
					
		
					if (data.db[data.order[iac]] != null && false) {
						$(pid + "hd").innerHTML = "Import " + data.db[data.order[iac]];
					}
					
				    rightpane[dbid] = $(pid);
						
					with (rightpane[dbid].style) {
						marginLeft = 20;
						border = "2px solid #c4c5c5";
						padding = "5px";
						width = "98%";
					}

					with ($(pid + "hd").style) {
						backgroundColor = "#c4c5c5";
						padding = "5px";
					}
					rightpane[dbid].hide();
				}
	
			}
			
			
			 
			tree.subscribe("clickEvent", function(evt) {
				 
				  if(evt.node.data.myNodeId!=null && evt.node.data.field!=null){				  	
				 	currentFieldNode=evt.node;
				  }
				  
				  var node=evt.node;
				  if ($(node.contentElId + "_link") == null && node.data.myNodeId != null && node.data.root != null && node.data.root) {
					 
					if(previousepane!=null) {
						rightpane[previousepane].hide();						
					}

					rightpane[node.data.myNodeId].show();
					previousepane=node.data.myNodeId;
					
					$("adm_field_pane").hide();
					
					var thisnodeId=node.data.myNodeId;
					var thisnode=node;					
					
					respBack1=function(data){		
						thisnode.data.processed=true;						
						progress_message(null);				
						processCommoditiesTree(data, thisnode, thisnodeId);
						thisnode.expand();  
						
					}
					if (!thisnode.data.processed) {
						progress_message("Please wait loading...");
						AdminMgmt.getNodeInfo(thisnodeId, respBack1);
					} 
					
				}
			});	
			
			 		
			tree.subscribe("labelClick", function(node) {

		  		if ($(node.contentElId + "_link") == null && node.data.myNodeId != null && node.data.root != null && node.data.root) {
					
					if(previousepane!=null) {
						rightpane[previousepane].hide();						
					}
					rightpane[node.data.myNodeId].show();
					previousepane=node.data.myNodeId;					
					$("adm_field_pane").hide(); 
				
				}
				
				if ($(node.contentElId + "_link") == null && node.data.myNodeId!=null && node.data.root==null) {
					if(lastTreeNodeContentEL!=null && node.contentElId!=lastTreeNodeContentEL){
						if($(lastTreeNodeContentEL+"_link")!=null) {$(lastTreeNodeContentEL+"_link").remove();}
					}
					var node1=node;
					var respBack = function(data){
						var hlink = document.createElement('a');
	 					hlink.href = "#";
						hlink.setAttribute('class', 'removeBtn');
						hlink.setAttribute('className', 'removeBtn');
						hlink.appendChild(document.createTextNode('[remove]'))
						
						hlink.onclick = function(){
			
							if (node1.data.commodity != null) {
								if (confirm("You are about to remove commodity and all the data related commodities will be deleted! Are you still want to remove?")) {
										progress_message("Please wait.....");
										AdminMgmt.removCommodity(node1.data.db,node1.data.myNodeId,
											function(data){
												progress_message(null);
												if(data!=null && data){
													message_status("Commodity has been removed from the database")
													tree.removeNode(node1, true);
												}
											}
										);
								}
							} else if (node1.data.field != null) {	}
							return false;
						}
						hlink.style.marginLeft = "5px";
						var td = document.createElement('td')
						td.id = node.contentElId + "_link";
						td.setAttribute('id', node.contentElId + "_link");
						td.style.padding = "10px";						
						if(data!=null && data>0){
							var spn=document.createElement('span');
							if(data>1){
								spn.appendChild(document.createTextNode('('+data+' days)'));
							}else{
								spn.appendChild(document.createTextNode('('+data+' day)'));
							}
							spn.setAttribute('class', 'removeBtn');
							spn.setAttribute('className', 'removeBtn');
							td.appendChild(spn);
						}			
						
									
						if (node1.data.commodity != null && node1.data.commodity) {
							td.appendChild(hlink);						
						}
						$(node.contentElId).parentNode.appendChild(td);
						
						lastTreeNodeContentEL = node.contentElId;
					}
					if(node1.data.commodity!=null){
						if(previousepane!=null) {
							rightpane[previousepane].hide();						
						}
						$("adm_field_pane").hide();
						respBack(null);
					}else if(node1.data.field){	}
				}
         });
		 
 
		 tree.render();	
			
		}
		
	}
	
	
 

	if(resdata!=null && typeof resdata=='object') {
		respBack(resdata);
		
		
	}else if(resdata!=null && typeof resdata=='function'){
		var resdata1=resdata;
		var respBackProxy=function(data){		
			respBack(data);
			resdata1.call(this);
		}		
		AdminMgmt.getMarketDBStruct(respBackProxy);
		
		 	
	}else{
		AdminMgmt.getMarketDBStruct(respBack);	
	}
	
	
	
	
}



var adm_myDataSource=null;
var adm_myDataTable=null;
 
var adm_deleteDataRow=function (thisobj){	  	 
	if(confirm("Are you sure you wish to delete record?")) {
		var record = adm_myDataTable.getRecord(thisobj.id)
		var data2del=record.getData();
		var id1=thisobj.id;
		AdminMgmt.deleteRawData(thisobj.getAttribute("dbname"),thisobj.getAttribute("tablename"), data2del,  function(data){
			adm_myDataTable.deleteRow(id1);		 
		}
		);
	}
	return false;
 }
 
function adm_buildRecordEditor(prevPaneDv, data,dbname1,tablename1,contract){
	if(data!=null){
		$(prevPaneDv).appendChild($("adm_DataSourceEditor"));
	    $j("#adm_DataSourceEditor").find("tr:eq(0)").show();
			
		$("adm_DataSourceEditor").show();
		$('adm_DataSourceAdd').hide();$("adm_DataSourceAddBtn").show();
        $("adm_DataSourceAddForm").contract.value=contract;
		 
		$j("#adm_DataSourceAddBtn").attr("db",dbname1);
		$j("#adm_DataSourceAddBtn").attr("fieldtable",tablename1);
		$j("#adm_DataSourceAddBtn").attr("contract",contract);
		$j("#adm_DataSourceAddBtn").attr("prevpane",prevPaneDv);
			 
		   				                                 
		var  formatBtn = function(elCell, oRecord, oColumn, oData) {
            elCell.innerHTML = " <a href='#' onclick='adm_deleteDataRow(this)' dbname='"+dbname1+"' tablename='"+tablename1+"' id='"+oRecord.getId()+"'><img border='0' src='images/remove_button.gif'/></a>";
        }; 

		var dateValidate=function(inpval, cval, e_ins){
			//alert(inpval+":"+cval+":"+e_ins);
			var dobj=isDate(inpval);
			if(dobj.success){
				return inpval;	
			}else{				
				return undefined;
			}
		} 

		var dbname2=dbname1;
		var tablename2=tablename1;
		var submitfun=function(callbck, newvale){			 
			var old_data=this.getRecord().getData();
			var newdata=new Object();
			newdata[this.getColumn().getKey()]=newvale;			 
			AdminMgmt.setUpdateRawData(dbname2,tablename2, old_data,newdata,  function(data){
				callbck(true,newvale);	
			   }
			);
		} 
		
		var stringvalue=false;
		for(i=0;i<data.length;i++){
		   if(data[i].sval!=null && data[i].sval!='' ){
		   	  stringvalue=true;
		   }	
		}
		
		var fobject=new Object();
		if(!stringvalue){
			var myColumnDefs = [
				{key:"cdate1", label:"Date", editor: new YAHOO.widget.TextboxCellEditor({validator:dateValidate,asyncSubmitter:submitfun})},			
	            {key:"contract",label:"Ticker", editor: new YAHOO.widget.TextareaCellEditor({asyncSubmitter: submitfun})},		
			    {key:"val", label:"Value",editor: new YAHOO.widget.TextboxCellEditor({validator:YAHOO.widget.DataTable.validateNumber,asyncSubmitter: submitfun})},			
				{key: "",formatter: formatBtn}			
	        ];
            fobject.fields=["cdate1","contract","val"];
       } else{
	   		var myColumnDefs = [
				{key:"cdate1", label:"Date", editor: new YAHOO.widget.TextboxCellEditor({validator:dateValidate,asyncSubmitter:submitfun})},			
	            {key:"contract",label:"Ticker", editor: new YAHOO.widget.TextareaCellEditor({asyncSubmitter: submitfun})},		
				{key:"sval", label:"SValue",editor: new YAHOO.widget.TextareaCellEditor({asyncSubmitter: submitfun})},
				{key: "",formatter: formatBtn}			
	        ];
			fobject.fields=["cdate1","contract","sval"];
	   } 
        adm_myDataSource = new YAHOO.util.DataSource(data);
        adm_myDataSource.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;
		
		
        //adm_myDataSource.responseSchema = {
        //    fields: ["cdate1","contract","val"]
        //};
		adm_myDataSource.responseSchema =fobject;
		
		
        //adm_myDataTable = new YAHOO.widget.DataTable("adm_DataSourceEditor", myColumnDefs, adm_myDataSource, {});
		adm_myDataTable = new YAHOO.widget.ScrollingDataTable("adm_DataSourceEditorTable", myColumnDefs, adm_myDataSource, {height:"600px",width:"100%"});

        // Set up editing flow
        var highlightEditableCell = function(oArgs) {
            var elCell = oArgs.target;
            if(YAHOO.util.Dom.hasClass(elCell, "yui-dt-editable")) {
                this.highlightCell(elCell);
            }
        };
        adm_myDataTable.subscribe("cellMouseoverEvent", highlightEditableCell);
        adm_myDataTable.subscribe("cellMouseoutEvent", adm_myDataTable.onEventUnhighlightCell);
        adm_myDataTable.subscribe("cellClickEvent", adm_myDataTable.onEventShowCellEditor);
		if (contract != null) {
			if ($("adm_field_pane_hd") != null) {
				$("adm_DataSourceEditorHead").show();
				$("adm_DataSourceEditorHead").innerHTML = $("adm_field_pane_hd").innerHTML + "->" + contract;
			}
		}
		
	}
}


/*
var deleteRow=function (thisobj){	  	 
		alert(thisobj.id)
		var record=myDataTable.getRecord(thisobj.id)
		myDataTable.deleteRow(thisobj.id);
 }
 var myDataSource=null; 
 var myDataTable=null;	
 */

function adm_testDataSourcefn() {
	
	
	YAHOO.example.Data = {
    addresses: [
        {name:"John A. Smith", address:"1236 Some Street", city:"San Francisco", state:"CA", amount:5, active:"yes", colors:["red"], fruit:["banana","cherry"], last_login:"4/19/2007"},
        {name:"Joan B. Jones", address:"3271 Another Ave", city:"New York", state:"NY", amount:3, active:"no", colors:["red","blue"], fruit:["apple"], last_login:"2/15/2006"},
        {name:"Bob C. Uncle", address:"9996 Random Road", city:"Los Angeles", state:"CA", amount:0, active:"maybe", colors:["green"], fruit:["cherry"], last_login:"1/23/2004"},
        {name:"John D. Smith", address:"1623 Some Street", city:"San Francisco", state:"CA", amount:5, active:"yes", colors:["red"], fruit:["cherry"], last_login:"4/19/2007"},
        {name:"Joan E. Jones", address:"3217 Another Ave", city:"New York", state:"NY", amount:3, active:"no", colors:["red","blue"], fruit:["apple","cherry"], last_login:"2/15/2006"},
        {name:"Bob F. Uncle", address:"9899 Random Road", city:"Los Angeles", state:"CA", amount:0, active:"maybe", colors:["green"], fruit:["banana"], last_login:"1/23/2004"},
        {name:"John G. Smith", address:"1723 Some Street", city:"San Francisco", state:"CA", amount:5, active:"yes", colors:["red"], fruit:["apple"], last_login:"4/10/2007"},
        {name:"Joan H. Jones", address:"3241 Another Ave", city:"New York", state:"NY", amount:3, active:"no", colors:["red","blue"], fruit:["kiwi"], last_login:"2/10/2006"},
        {name:"Bob I. Uncle", address:"9909 Random Road", city:"Los Angeles", state:"CA", amount:0, active:"maybe", colors:["green"], fruit:["apple","banana"], last_login:"1/23/2004"},
        {name:"John J. Smith", address:"1623 Some Street", city:"San Francisco", state:"CA", amount:5, active:"yes", colors:["red"], fruit:["apple","cherry"], last_login:"4/19/2007"},
        {name:"Joan K. Jones", address:"3721 Another Ave", city:"New York", state:"NY", amount:3, active:"no", colors:["red","blue"], fruit:["banana"], last_login:"2/15/2006"},
        {name:"Bob L. Uncle", address:"9989 Random Road", city:"Los Angeles", state:"CA", amount:0, active:"maybe", colors:["green"], fruit:["cherry"], last_login:"1/23/2004"},
        {name:"John M. Smith", address:"1293 Some Street", city:"San Francisco", state:"CA", amount:5, active:"yes", colors:["red"], fruit:["cherry"], last_login:"4/19/2007"},
        {name:"Joan N. Jones", address:"3621 Another Ave", city:"New York", state:"NY", amount:3, active:"no", colors:["red","blue"], fruit:["apple"], last_login:"2/15/2006"},
        {name:"Bob O. Uncle", address:"9959 Random Road", city:"Los Angeles", state:"CA", amount:0, active:"maybe", colors:["green"], fruit:["kiwi","cherry"], last_login:"1/23/2004"},
        {name:"John P. Smith", address:"6123 Some Street", city:"San Francisco", state:"CA", amount:5, active:"yes", colors:["red"], fruit:["banana"], last_login:"4/19/2007"},
        {name:"Joan Q. Jones", address:"3281 Another Ave", city:"New York", state:"NY", amount:3, active:"no", colors:["red","blue"], fruit:["apple"], last_login:"2/15/2006"},
        {name:"Bob R. Uncle", address:"9989 Random Road", city:"Los Angeles", state:"CA", amount:0, active:"maybe", colors:["green"], fruit:["apple"], last_login:"1/23/2004",button:"dele"}
    ]
	}


	
		
	var cellsaveEvent=function(editor , newData , oldData ){
		alert('a');		
	};
	
	

	   var  formatBtn = function(elCell, oRecord, oColumn, oData) {
            elCell.innerHTML = " <input type=\"button\" onclick='deleteRow(this)' value='Delete' id='"+oRecord.getId()+"'/>";
        }; 
		
	  var formatAddress = function(elCell, oRecord, oColumn, oData) {
            elCell.innerHTML = "<pre class=\"address\">" + oData + "</pre>";
        };
		
		var adm_saveData= function(elCell, oRecord, oColumn, oData) {
            alert('elcell');
        };
		var adm_saveData1= function( oData) {
            alert(oData);
			//return oData;
			 return undefined;
        };
		
		
		var submitfun=function(callbck, newvale){
			var newvale1=newvale; 
			var callbck1=callbck;
			AdminMgmt.getMarketDBStruct(function(data){
				callbck1(true,newvale1)	
			});
		} 
		
		var fnCallback=function(bSuccess, oNewValue){
			alert(oNewValue);
		} 
		

        var myColumnDefs = [
            {key:"uneditable"},
			{key:"name", editor: new YAHOO.widget.TextboxCellEditor({disableBtns:false})},
            {key:"address", formatter:formatAddress, editor: new YAHOO.widget.TextareaCellEditor()},			
            {key:"city", editor: new YAHOO.widget.TextboxCellEditor({disableBtns:false})},
            //{key:"state", editor: new YAHOO.widget.DropdownCellEditor({dropdownOptions:YAHOO.example.Data.stateAbbrs,disableBtns:true})},
            {key:"amount", editor: new YAHOO.widget.TextboxCellEditor({validator:YAHOO.widget.DataTable.validateNumber})},
            {key:"active", editor: new YAHOO.widget.RadioCellEditor({radioOptions:["yes","no","maybe"],disableBtns:true})},
            {key:"colors", editor: new YAHOO.widget.CheckboxCellEditor({checkboxOptions:["red","yellow","blue"]})},
            {key:"fruit", editor: new YAHOO.widget.DropdownCellEditor({multiple:true,dropdownOptions:["apple","banana","cherry"]})},			
            {key:"last_login", formatter:YAHOO.widget.DataTable.formatDate,disableBtns:true, editor: new YAHOO.widget.DateCellEditor({asyncSubmitter:submitfun})},
			{
				key: "button",
				formatter: formatBtn 
			}
			//{key:"state",  formatter:new YAHOO.widget.DataTable.formatButton,editor:new YAHOO.widget.ButtonEditor({asyncSubmitter:submitfun})},
			//{key:"state",  formatter:new YAHOO.widget.DataTable.formatButton,editor:new YAHOO.widget.CellEditor()}
			
        ];

         myDataSource = new YAHOO.util.DataSource(YAHOO.example.Data.addresses);
        myDataSource.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;
		


        myDataSource.responseSchema = {
            fields: ["name","address","city","state","amount","active","colors","fruit",{key:"last_login",parser:"date"}],
		
			//fields: ["address","city","state","amount","active","colors","fruit",{key:"last_login",parser:"date"}]
			
        };

        myDataTable = new YAHOO.widget.DataTable("adm_testDataSource", myColumnDefs, myDataSource, {});

        // Set up editing flow
        var highlightEditableCell = function(oArgs) {
            var elCell = oArgs.target;
            if(YAHOO.util.Dom.hasClass(elCell, "yui-dt-editable")) {
                this.highlightCell(elCell);
            }
        };
        myDataTable.subscribe("cellMouseoverEvent", highlightEditableCell);
        myDataTable.subscribe("cellMouseoutEvent", myDataTable.onEventUnhighlightCell);
        myDataTable.subscribe("cellClickEvent", myDataTable.onEventShowCellEditor);
        
        return {
            oDS: myDataSource,
            oDT: myDataTable
        };

	
}


function adm_selectRawData(db, tablename, contract,commodity){
	if(tree==null){
		var db1=db;
		var tablename1=tablename;
		var contract1=contract;		
		var commodity1=commodity;
		var fproxy=function(){
			progress_message(null);
			adm_selectRawData2(db1, tablename1, contract1,commodity1);
		}
		progress_message("Please wait...");		
		refreshDBStruct(fproxy)	;
	}else{
		adm_selectRawData2(db, tablename, contract,commodity);
	}
	
}


function adm_selectRawData2(db, fieldname, contract,commodity){
	//var t=tree.getNodeByProperty("myNodeId","000240_ks_shares_outstanding_histor");
	
	//var ta=tree.getNodesByProperty("myNodeId",fieldname);
	
	var ta=tree.getNodesByProperty("fieldname",fieldname);
	var found=false;	
	for(ia=0;ia<ta.length;ia++){
		var t=ta[ia];
		comname=(commodity!=null && commodity!='')?commodity:contract;
		//if(t.data.db==db && !t.parent.isRoot() && !t.parent.parent.isRoot() && t.data.field && t.parent.data.commodityname==comname){
		if(t.data.db==db && !t.parent.isRoot() && t.data.field && t.parent.data.commodityname==comname){
			
			found=true;
			t.parent.parent.expand();
			t.parent.expand();
			t.focus();			 
			var t1=t;
			
			//YAHOO.util.Dom.setY ( t.getContentEl(), 100); //scroll to view point of the user.
			//var d=document.createElement('div');
			//d.scrollTo
			    
			//var el=$("dbtree").select("a[fieldname='"+fieldname+"']")
			var el=$(t.getContentEl()).select("a[fieldname='"+fieldname+"']");
			var dontclosedata=true;
			adm_fieldclick(el[0],dontclosedata);
			
			var db1=db;
			var fieldname1=fieldname;
			var contract1=contract;
			var commodity1=commodity; 
			
			var proxy_respo=function(data){
								
				if (data.data != null) {
					var tabs = mainTabView.get('tabs');
					var chrtindx = -1;
					for (iad = 0; iad < tabs.length; iad++) {
						if (tabs[iad].get('label').indexOf('Administration') > 0) {
							chrtindx = iad;							
						}
					}
					if (chrtindx >= 0) {
						mainTabView.set('activeIndex', chrtindx);
						arrayLoadedTabs[chrtindx]=true;
						//#t1.getContentEl().scrollTo();
						
						window.location.href = "#"+db1+t1.data.myNodeId;
						try{
							$("dbtree").parentNode.scrollTop=$("dbtree").parentNode.scrollTop-200;
						}catch(ex){}
						
					}
					//var el = $("dbtree").select("a[fieldname='" + fieldname1 + "']")
					//adm_fieldclick(el[0])
					if (commodity1 != null && commodity1!='') {
						adm_buildRecordEditor("adm_dataPreviewPanel",data.data, db1, data.tablename, contract1,commodity1);
					}else{
						adm_buildRecordEditor("adm_dataPreviewPanel",data.data, db1, data.tablename, contract1);
					}
				}else{
					alert("No data found");
				}
				
			};
			
			var dobj=getDateInputs(dateElemFilter,"cdate "); 
			if (dobj.query == null) {
				  alert("Please select valid period");
				  return false;
			}
				
			//AdminMgmt.getCommodityRawData(dobj.query,db,tablename,contract,proxy_respo);
			if (commodity1 != null && commodity1 != '') {
				AdminMgmt.getCommodityRawData2Contrat(dobj.query, db, fieldname, contract, commodity1,proxy_respo);
			}
			else {
				AdminMgmt.getCommodityRawData2(dobj.query, db, fieldname, contract, proxy_respo);
			}
			
		}
	}
	if(!found){
		message_status("No Data found")
	}
	
 
}


function adm_DataSourceSave(){
	//var db=$("adm_datefilterbtn").getAttribute("db");
	//var tablename=$("adm_datefilterbtn").getAttribute("myNodeId");
	//var contract=$("adm_datefilterbtn").getAttribute("contract");
	
	
	var db=$j("#adm_DataSourceAddBtn").attr("db");
	var tablename=$j("#adm_DataSourceAddBtn").attr("fieldtable");
	var contract=$j("#adm_DataSourceAddBtn").attr("contract");
		
	
	var fm=$('adm_DataSourceAddForm');
	var dobj=isDate(fm.cdate.value);
	if(!dobj.success){
		return false;
	}	
	
	if(isNaN(fm.val.value)){
		alert("Please input valid data");
		return false;
	}
	//var contract=new Object();
	//contract.cdate=fm.cdate.value;
	//contract.val=fm.val.value;
	//contract.contract=fm.contract.value;
	
	var respBack=function(data){
		progress_message(null);
		
		if(data!=null && data){
			message_status("Data added")
			adm_filterRecordEditor($('adm_datefilterbtn'));
			$('adm_DataSourceAddBtn').show(); 
			$('adm_DataSourceAdd').hide();	
		}
		
	}
	progress_message("Please wait..")
	AdminMgmt.addRawData(db, tablename,fm.cdate.value,fm.val.value,fm.sval.value,contract,respBack);
}
function adm_showAddRecPane(){

   //var db=$("adm_datefilterbtn").getAttribute("db");
   //var tablename=$("adm_datefilterbtn").getAttribute("myNodeId");
   
   var db=$j("#adm_DataSourceAddBtn").attr("db");
	var tablename=$j("#adm_DataSourceAddBtn").attr("fieldtable");
	
   
   var respback=function(data){
   	   	//adm_DataAddFormV1
		if (data != null) {
			
		   var isnumer=true;
		   if(data==0){
		   	 if(!confirm("Table is empty, Press 'OK' to set numeric type field or 'Cancel' to set String characters field")){
			 	isnumer=false;
			 }
		   }
		   if(data==2){
		   	 isnumer=false;
		   }
		   
		   $("adm_DataAddFormV").hide();$("adm_DataAddFormV1").hide();
		   $("adm_DataAddFormSV").hide();$("adm_DataAddFormSV1").hide();
		   if(isnumer){
		     $("adm_DataAddFormV").show();$("adm_DataAddFormV1").show(); 	
		   }else{
		   	$("adm_DataAddFormSV").show();$("adm_DataAddFormSV1").show();
		   }
		   $("adm_DataSourceAddForm").val.value="";
		   $("adm_DataSourceAddForm").sval.value="";
		   
			//$('adm_DataSourceAddForm').contract.value = $('adm_datefilterbtn').getAttribute('contract');
			$('adm_DataSourceAdd').show();
			$("adm_DataSourceAddBtn").hide();
		}
   	
   }
   AdminMgmt.checkTableDataType(db,tablename,respback);
   

	
}


/**
 * this is old method to be removed later.
 */

function refreshDBStruct_old(resdata){
	
	var respBack = function(data){
		if (data != null) {
		 //tree = new YAHOO.widget.TreeView("dbtree");			
					
		  if (tree == null) {
		  	tree = new YAHOO.widget.TreeView("dbtree");	
		  }else{
		  	tree.removeChildren(tree.getRoot());
		  }
						
		  var root = tree.getRoot();			

		   rightpane=null;
		   rightpane=new Array();
			
			if (data.db != null && data.order!=null) {
			 
				for(iac=0;iac<data.order.length;iac++){
					
					var db_lbl=data.db[data.order[iac]];
					var db_style="icon-db";
					
					if (data[[data.order[iac] + "_err"]] != null && data[[data.order[iac] + "_err"]].length>0) {
						db_lbl="<img src='images/dbicon_err.JPG' style='margin-right:6px'><span style='cursor:pointer'>"+data.db[data.order[iac]]+"</span><a href='#' style='margin-left:10px' nodeid='"+data.order[iac]+"' onclick='adm_fixDb(this);'><small style='color:#FF0000'>Fix("+data[[data.order[iac] + "_err"]].length+")</small></a>";;
						//db_style="icon-db-err";
						
					}else{
						db_lbl="<img src='images/dbicon.jpg' style='margin-right:6px'><span style='cursor:pointer'>"+data.db[data.order[iac]]+"</span>";
					} 	
					//var msec1 = new YAHOO.widget.TextNode({
					var msec1 = new YAHOO.widget.HTMLNode({						
						html: db_lbl,
						//label:db_lbl,
						myNodeId: data.order[iac],
						myNodeIdS: data.order[iac]+"",
						
						root: true
					}, root);

					msec1.labelStyle=db_style;
                 
					var labl=msec1.label;
					var dbid=msec1.data.myNodeId;
					
					var pid=iac;

                    if (false) {
						var pid=data.dbpane[dbid];
						processCommoditiesTree(data, msec1, data.order[iac]);
					}						
					//rightpane[dbid] = new YAHOO.widget.Panel(pid, { width:"auto", visible:false, constraintoviewport:false,autofillheight:true } ); 
					//rightpane[dbid].setHeader("Import "+labl);  
	 				//rightpane[dbid].render();			
					if (data.db[data.order[iac]] != null && false) {
						$(pid + "hd").innerHTML = "Import " + data.db[data.order[iac]];
					}
					
					if (false) {
						rightpane[dbid] = $(pid);
						
						with (rightpane[dbid].style) {
							marginLeft = 20;
							border = "2px solid #c4c5c5";
							padding = "5px";
							width = "98%";
						}
						
						//var ddd=document.createElement('div');
						//ddd.style.backgroundColor
						with ($(pid + "hd").style) {
							backgroundColor = "#c4c5c5";
							padding = "5px";
						}
						//rightpane[dbid].style.width="auto";
						rightpane[dbid].hide();
					}
					
				}
	
			}
			
			
			 
			tree.subscribe("clickEvent", function(evt) {
				//alert(DWRUtil.toDescriptiveString(evt.node.data));
				  if(evt.node.data.myNodeId!=null && evt.node.data.field!=null){				  	
				 	currentFieldNode=evt.node;
				  }
				  
				  var node=evt.node;
				  if ($(node.contentElId + "_link") == null && node.data.myNodeId != null && node.data.root != null && node.data.root) {
					//rightpane.show();
					//for(pa_id in rightpane){
					//	rightpane[pa_id].hide();
					//}
					if(previousepane!=null) {
						rightpane[previousepane].hide();						
					}
						
					//rightpane[node.data.myNodeId].render();
					
					rightpane[node.data.myNodeId].show();
					previousepane=node.data.myNodeId;
					
					$("adm_field_pane").hide(); 
					
				}
			});
			
			tree.subscribe("labelClick", function(node) {
          
		  		
		  
		  		if ($(node.contentElId + "_link") == null && node.data.myNodeId != null && node.data.root != null && node.data.root) {
					//rightpane.show();
					//for(pa_id in rightpane){
					//	rightpane[pa_id].hide();
					//}
					if(previousepane!=null) {
						rightpane[previousepane].hide();						
					}
						
					//rightpane[node.data.myNodeId].render();
					
					rightpane[node.data.myNodeId].show();
					previousepane=node.data.myNodeId;
					
					$("adm_field_pane").hide(); 
					
				}
				if ($(node.contentElId + "_link") == null && node.data.myNodeId!=null && node.data.root==null) {
					if(lastTreeNodeContentEL!=null && node.contentElId!=lastTreeNodeContentEL){
						if($(lastTreeNodeContentEL+"_link")!=null) {$(lastTreeNodeContentEL+"_link").remove();}
					}
					
					var node1=node;
					 
					var respBack = function(data){
						var hlink = document.createElement('a');
						//hlink.setAttribute('type','button');
						//hlink.setAttribute('value','remove');
						
						
						
						hlink.href = "#";
						hlink.setAttribute('class', 'removeBtn');
						hlink.setAttribute('className', 'removeBtn');
						hlink.appendChild(document.createTextNode('[remove]'))
						
						hlink.onclick = function(){
						
							if (node1.data.commodity != null) {
								if (confirm("You are about to remove commodity and all the data related commodities will be deleted! Are you still want to remove?")) {
										progress_message("Please wait.....");
										AdminMgmt.removCommodity(node1.data.db,node1.data.myNodeId,
											function(data){
												progress_message(null);
												if(data!=null && data){
													message_status("Commodity has been removed from the database")
													tree.removeNode(node1, true);
												}
											}
										);
									//alert(node1.data.myNodeId)
								//tree.removeNode(node1, true);
								}
							}
							
							
							
							else					
							 
								if (node1.data.field != null) {
									
									
									/*
									if (confirm("Are you sure you wish to remove a field?")) {
										//tree.removeNode(node1, true);
										//alert(node1.data.db + ":" + node1.data.myNodeId)										
										AdminMgmt.removeFieldTable(node1.data.db,node1.data.myNodeId,
											function(data){
												if(data!=null && data){
													message_status("Field has been removed from the database")
													tree.removeNode(node1, true);
												}
											}
										);
									}
									*/
									
								}
							return false;
						}
						hlink.style.marginLeft = "5px";
						
						//$(node.contentElId).appendChild(hlink);						
						var td = document.createElement('td')
						td.id = node.contentElId + "_link";
						td.setAttribute('id', node.contentElId + "_link");
						
						//td.style.marginLeft = "20px";
						td.style.padding = "10px";						
						if(data!=null && data>0){
							var spn=document.createElement('span');
							if(data>1){
								spn.appendChild(document.createTextNode('('+data+' days)'));
							}else{
								spn.appendChild(document.createTextNode('('+data+' day)'));
							}
							spn.setAttribute('class', 'removeBtn');
							spn.setAttribute('className', 'removeBtn');
							td.appendChild(spn);
						}			
						
									
						if (node1.data.commodity != null && node1.data.commodity) {
							td.appendChild(hlink);						
						}
						$(node.contentElId).parentNode.appendChild(td);
						
						lastTreeNodeContentEL = node.contentElId;
					}
					if(node1.data.commodity!=null){
						
						if(previousepane!=null) {
							rightpane[previousepane].hide();						
						}
						$("adm_field_pane").hide();
						respBack(null);
					}else if(node1.data.field){
						
					 
						//AdminMgmt.getDays4FieldTable(node1.data.db,node1.data.myNodeId,respBack);
					}
				}
		   
		  
         });
			
		 tree.render();	
			
		}
		
	}
	
	
	 
	 //rightpane.hide();   
   

	if(resdata!=null && typeof resdata=='object') {
		respBack(resdata);
		
		
	}else if(resdata!=null && typeof resdata=='function'){
		var resdata1=resdata;
		var respBackProxy=function(data){		
			respBack(data);
			resdata1.call(this);
		}		
		AdminMgmt.getMarketDBStruct(respBackProxy);
		
		 	
	}else{
		AdminMgmt.getMarketDBStruct(respBack);	
	}
	
	
	
	
}
