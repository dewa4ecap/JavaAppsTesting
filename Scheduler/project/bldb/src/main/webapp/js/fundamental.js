


function fu_downloadStaticSheet(chartopt) {
	var checkboxes=$('fu_fieldTablesxl').select('input[type="checkbox"]');
	var fn="";
	var ft="";
	
	
	fu_getInputRangesxl();
	
	if (curDtFilterQuery == null) {
		alert("Please select date range");
		return false;
	}
	
	for(iab=0;iab<checkboxes.length;iab++){		
		if(checkboxes[iab].checked){
			//alert(checkboxes[iab].id+" ::"+checkboxes[iab].value);
			//fnames[fnames.length]=checkboxes[iab].value;
			//ftables[ftables.length]=checkboxes[iab].id;
			if (checkboxes[iab].id != '$checkAllBx$') {
				fn += (iab == 0) ? checkboxes[iab].value : "," + checkboxes[iab].value;
				ft += (iab == 0) ? checkboxes[iab].id : "," + checkboxes[iab].id;
			}
		}
	}

	
	$("ftables").value=ft;
	$("fnames").value=fn;
	
	if (ft=="") {
		alert("Please select one or more fields ");
		return false;
	}
	
	$("commString").value=$("fu_market_comm_target1").value.strip()
	
	
	if(fu_mktCommDND.getSelected().length<=0 && $("commString").value==''){
		//alert("Please select minimum one commodity");
		//return false;
		fu_mktCommDND.selectAll();	
	}
	$("commodities").value=fu_mktCommDND.getSelectedAsString();

	
	$("chart").value="no";
	if(chartopt!=null && chartopt==true){
		$("chart").value="yes";
	}
	
	if (curDtFilterQuery != null) {
		$("dquery").value = curDtFilterQuery;
	}else{
		$("dquery").value="";
	}
	if(curContractFilter!=null){
		$("cquery").value=curContractFilter;	
	}else{
		$("cquery").value="";
	}
	$("dbkey").value="db_fundamental_name"; 
	$("allcommodities").value=fu_allCommoditiesSelected+"";
		
	$("downloadfile").submit();
	
}


/**
 * Generates fields for selected 
 * commodity on "Administration" widget
 * @param {Object} data
 */
//removed as admin features are in separate tab
/*
function fu_generateFields4Admin(data){
	if(data!=null && typeof data=='object'){
		$("fu_adminpanelftables").innerHTML="<hr>";
		for(ib=0;ib<data.length;ib++){
				var span=document.createElement('div')
				//span.setAttribute('tableid',data[ib].ftable);
				span.appendChild(document.createTextNode(data[ib].fname+" ("+data[ib].total+")"));			
				span.style.padding="3px";
				
				var txt=document.createElement("small");
				txt.style.marginLeft="10px";
				txt.style.color="red";
				txt.appendChild(document.createTextNode('Remove'));
				var anc=document.createElement('a');
				anc.appendChild(txt);					
				anc.setAttribute('href','#');
				anc.setAttribute('tableid',data[ib].ftable);
				anc.setAttribute('commodity',data[ib].commodity);
				anc.onclick = function(){
					if (confirm("It also removes data that imported into this field, would you still wish to remove this field?","Yes","No")) {
						//removeFTable(this.getAttribute('tableid'),mtableobj.id);
						//var mtable=$(mtableobj.id).options[$(selobjid).selectedIndex].value;
						//alert(this.getAttribute('tableid')+":"+this.getAttribute('commodity'))
						var respBackProxy = function(data){
							message_status("Field has been deleted");
							fu_generateFields4Admin(data);
						}
						progress_message("Please wait while deleting the field")
						MigrationMgmt.removeFieldTableFundamental(this.getAttribute('tableid'),this.getAttribute('commodity'),respBackProxy);
					}
				}
				span.appendChild(anc);
				$("fu_adminpanelftables").appendChild(span);
			 
		}
	}
}
*/


function fu_generateSQLQuery(tablename){
	fu_getInputRangesxl();
	if (curDtFilterQuery == null) {
		  if (!confirm("You haven't selected date range!! Would you still wish to generate SQL query","Yes","No")) {
		  	return false;
		  }
	}

	var add_comm=$("fu_market_comm_target1").value.strip();
	if(fu_mktCommDND.getSelected().length<=0 && add_comm==''){
		//alert("Please select minimum one commodity");
		//return false;
		
		fu_mktCommDND.selectAll();
	}	
 	var respoBackFunction=function(data){
	 	 progress_message(null);		 
		 if (window.clipboardData!=null && clipboardData.setData!=null) {
		 	clipboardData.setData("Text", data);
			 message_status("SQL Query copied to clipboard, please it on your Excel or Notepad");
		 }else{
		 	$("progressMessage").innerHTML="<pre>"+data+"</pre>";
		 	$("progressMessage").show();	
		 }
		
	 }
	progress_message("Please wait your request is processing now....");
	MigrationMgmt.generateSQLXLQueryFundamental(tablename,curDtFilterQuery,curContractFilter,fu_allCommoditiesSelected,fu_mktCommDND.getSelected(),add_comm,respoBackFunction);
	//MigrationMgmt.generateSQLXLQueryFundamental(tablename,curDtFilterQuery,curContractFilter,respoBackFunction);
}


function fu_generateContracts(tablename){
	 

	 var respoBackFunction=function(data){
	 	 progress_message(null);		 
		 if (window.clipboardData!=null && clipboardData.setData!=null) {
		 	 clipboardData.setData("Text", data);
			 message_status("Contracts copied to clipboard, paste it on your Security Box");
		 }else{
		 	$("progressMessage").innerHTML="<pre>"+data+"</pre>";
		 	$("progressMessage").show();	
		 }
		
	 }
	if (fu_allCommoditiesSelected) {
		sec_getInputRangesxl();
		if (curDtFilterQuery == null) {
			if (!confirm("You haven't selected date range!! Would you still wish to generate SQL query", "Yes", "No")) {
				return false;
			}
		}
	} 
	var add_comm=$("fu_market_comm_target1").value.strip();
	if(fu_mktCommDND.getSelected().length<=0 && add_comm==''){		 
		fu_mktCommDND.selectAll();
	} 
	progress_message("Please wait your request is processing now....");	 
	MigrationMgmt.generateFundamentalContracts(tablename,fu_allCommoditiesSelected,curDtFilterQuery,fu_mktCommDND.getSelected(),add_comm,respoBackFunction);
}


function fu_getInputRangesxl(){
	curDtFilterQuery=null;
	curContractFilter=null;
	
	//if($("fu_contractsxl").value!=null && $("fu_contractsxl").value.strip()!=''){
	//	curContractFilter=$("fu_contractsxl").value.strip();
	//}
	if (curDtFilterType == 'daterange' ) {
		var dobj1=isDate($('fu_date1').value);
		var dobj2=isDate($('fu_date2').value);
		if( dobj1.success && dobj2.success){			 
			curDtFilterQuery="cdate BETWEEN '"+dobj1.year+"-"+dobj1.month+"-"+dobj1.day+"' AND '"+dobj2.year+"-"+dobj2.month+"-"+dobj2.day+"'";
			 
		}
		
	}else if (curDtFilterType == 'datefrom' ) {
		var dobj1=isDate($('fu_date1').value);		 
		if( dobj1.success){			 
			curDtFilterQuery="cdate BETWEEN '"+dobj1.year+"-"+dobj1.month+"-"+dobj1.day+"' AND GETDATE() ";
			 
		}
		
	}else if(curDtFilterType == 'number'){
		var numbr=parseInt($('fu_numberrange').value);
		if (isNaN(numbr)) {
			alert("Please input only number on Last Date/Week/Months/Years");
		}else{
			if(curDtFilterValue=="ndays"){
				curDtFilterQuery=" cdate>=DATEADD(day,-"+numbr+",current_timestamp) ";	
			}else if(curDtFilterValue=="nweeks"){
			   curDtFilterQuery=" cdate>=DATEADD(week,-"+numbr+",current_timestamp) ";
			}else if(curDtFilterValue=="nmonths"){
			   curDtFilterQuery=" cdate>=DATEADD(month,-"+numbr+",current_timestamp) ";
			}else if(curDtFilterValue=="nyears"){
			   curDtFilterQuery=" cdate>=DATEADD(year,-"+numbr+",current_timestamp) ";
			}
		}		
	}else{
		curDtFilterQuery=(curDtFilterValue!=null && curDtFilterValue!='')?curDtFilterValue:null;
	}
	
}


 /**
  * this function imports current data
  */
function fu_importCurrent(){
	 
	 var respoBackFunction=function(data){
	 	 progress_message(null);
		 message_status("Current data imported from Excel speadsheet");
		 getFiles();
		 getAllTables();
	 }
	 progress_message("Please wait your request is processing now....");
	 MigrationMgmt.importFundamentalCurrentData(respoBackFunction);
} 



 /**
  * this function imports historical data
  */
function fu_importHistory(){
	 
	 var respoBackFunction=function(data){
	 	 stopPolling();
	 	 progress_message(null);
		 message_status("Historical data imported from Excel speadsheet");
		 getFiles();
		 getAllTables();
	 }
	 
 	 var ignoreDupStrategy=false;
	 if($("ignoreDupStrategy").checked){
	 	ignoreDupStrategy=true;
	 }	 
	 progress_message("Please wait your request is processing now....");
	 MigrationMgmt.importFundamentalHistoryData(ignoreDupStrategy,respoBackFunction);
	 startPolling();
}

function fu_onDateSelectxl(selobj){
	//$("allTablesxl").options[$("allTablesxl").selectedIndex]
	//alert(selobj.options[selobj.selectedIndex].getAttribute('type'));
	
	curDtFilterType=selobj.options[selobj.selectedIndex].getAttribute('type');
	curDtFilterValue=selobj.options[selobj.selectedIndex].value
	$("fu_dateInputArea").innerHTML="";
	
	if(curDtFilterType=='daterange' || curDtFilterType=='datefrom'){
		
		var date1=document.createElement('input');
		date1.setAttribute('type','text');
		date1.setAttribute('size','8');
		//validate
		
		
	
		date1.setAttribute('id','fu_date1');
		$("fu_dateInputArea").appendChild(document.createTextNode("From:"));
		$("fu_dateInputArea").appendChild(date1);
		if (curDtFilterType != 'datefrom') {
			var date2=date1.cloneNode(true);		
			date2.setAttribute('id','fu_date2');		
			$("fu_dateInputArea").appendChild(document.createTextNode("To:"));
			$("fu_dateInputArea").appendChild(date2);
		}
		
	}else if(curDtFilterType=='number'){
		
		var ninp=document.createElement('input');
		ninp.setAttribute('type','text');
		ninp.setAttribute('id','fu_numberrange');
		ninp.setAttribute('size','5');
		ninp.value='1'
		$("fu_dateInputArea").appendChild(ninp);
	}
	
	//dateInputArea
	
}

/**
 * function called while on change of commodity of "administration" widget 
 * @param {Object} mtableobj
 */		
 //removed as admin features are in separate tab
 /*
function fu_onSelectAdminTable(mtableobj){
	var mtable=mtableobj.options[mtableobj.selectedIndex].value;
	$("fu_adminpanelftables").innerHTML="<hr>";
	if (mtable != null && mtable != '') {
		
		MigrationMgmt.getFields4AdminFundamental(mtable, fu_generateFields4Admin);
	}	
}

*/


function fu_onSelectTablexl(){
	var mtable=$("fu_allTablesxl").options[$("fu_allTablesxl").selectedIndex].value;
	if(mtable!=null && mtable!=""){
		 var respBack=function(data){
		 	
		 	
			//$("fieldTablesxl").innerHTML="";
			if(data!=null && typeof data=='object'){
				getTableOptions(data,fu_generateSQLQuery,fu_generateContracts,fu_downloadStaticSheet,"fu_fieldTablesxl");
			}
		 }
		 
		 MigrationMgmt.getFundamentalFieldTables(mtable,respBack);
	}else{
		$("fu_fieldTablesxl").innerHTML="";
	}
}



/*
function fu_tickerinfo_close(){
	$("fu_ticker_info_pn").hide();
	var deleteChildren=function(divname){
					var children=$(divname).childElements();			 
					for (intb = 0; intb < children.length; intb++) {
						children[intb].remove();
					}
			
	}
	deleteChildren("fu_ticker_info_pnbdy");			
}
*/

function fu_itemDblClick(){
	var security=this.getAttribute("item");
	//sec_tickerinfo_close();	
	$("fu_ticker_info_pn").show();	
	//ref.js 
	ref_modifyFlexiData("fundamental_flexi_data","fu_ticker_info_pnbdy","ticker",security);	
}



function fu_searchTicker() {
	
	var keyword=$("fu_searchTicker").value
	if(keyword!=''){
		
		var respBack=function(data){
			progress_message(null);
			if (data != null && data.length > 0) {
				fu_mktCommDND.deSelectAll();
				//fu_mktCommDND.selectValue(data);
				fu_mktCommDND.selectNonExistingValue2(data);
				
			}else{
				alert("No security matched with keyword");
			}
			
		}
		
		
		progress_message("Please wait searching keyword....")
		MigrationMgmt.fundamentalTickerSearch(keyword,respBack);
	}
	return false;
}


function fu_goToChart(){
	
	if (fu_mktCommDND.getSelected().length > 0) {
	
	
		var tabs = mainTabView.get('tabs');
		var chrtindx = -1;
		for (iad = 0; iad < tabs.length; iad++) {
			if (tabs[iad].get('label').indexOf('Chart') > 0) {
				chrtindx = iad;
			}
		}
		if (chrtindx >= 0) {
			mainTabView.set('activeIndex', chrtindx);
		}
		chart_swich2(fu_mktCommDND.getSelectedAsString(),"fundamental");
	}else{
		alert("Please select minimum one security");
	}
}