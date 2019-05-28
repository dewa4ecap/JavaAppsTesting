

function bbpl_jsNew(id) {	
	
	//$("dld_queryEditorForm1").reset();
	$(id).appendChild($("dld_queryEditorForm1").cloneNode(true));
	$(id).select("#dld_editor1")[0].show();	
	currentQueryId=0;
	currentQueryData=null;	 
	bbpl_resetFieldList(null,id);
	
}



function bbpl_updateFieldsDDList(selobj){
	if(field_mapping!=null){
		//var selobj=$("dld_blbfield");
		for(iab=selobj.options.length-1;iab>=0;iab--){
			$(selobj.options[iab]).remove();
		}		
		selobj.appendChild(document.createElement('option'));							
		
		//var selids= bbpl_collectQueryData(selobj.form.parentNode); //collectSelectedFields();
		var selids= bbpl_collectSelectedFieldsArry(selobj.form.parentNode);	 
			 
		for (iab = 0; iab < field_mapping.length; iab++) {
			if(typeof selids=='object' &&  selids.indexOf(field_mapping[iab].id+'')<0){
				var opt = document.createElement('option');
				opt.value = field_mapping[iab].id;
				var ttext = field_mapping[iab].db_field + "   (" + field_mapping[iab].bb_field + ")"
				opt.appendChild(document.createTextNode(ttext));
				selobj.appendChild(opt);				
			}
		}
	}
}


function bbpl_resetFieldList(selids,id) {
	
	var ftbl=$(id).select('#dld_fields1')[0];
	var selobj=$(id).select('#dld_blbfield1')[0];
	
	if (ftbl.rows.length > 1) {
		for (iac = ftbl.rows.length - 2; iac >= 0; iac--) {
			$(ftbl.rows[iac]).remove();
		}
	}
	if(selids!=null){
		for (iab = 0; iab < field_mapping.length; iab++) {			
				if (selids.indexOf(field_mapping[iab].id + '') >= 0) {
					var inps = $(ftbl.rows[ftbl.rows.length - 1]).select('input');
					
					var mi_button = inps[0].cloneNode(true);
					mi_button.src = "images/button_minus.gif";
					
					var r1=ftbl.rows[ftbl.rows.length-1];
					var r2 = ftbl.insertRow(r1.rowIndex + 1);
					r2.insertCell(r2.cells.length).appendChild(selobj)
					r2.insertCell(r2.cells.length).appendChild(inps[0])	
									
					var ttext = field_mapping[iab].db_field + "   (" + field_mapping[iab].bb_field + ")";					
					r1.cells[0].appendChild(document.createTextNode(ttext));
					r1.cells[1].appendChild(mi_button);
					r1.setAttribute('rowid', field_mapping[iab].id)
				}
				
		}
	}
	bbpl_updateFieldsDDList(selobj);
} 



function bbpl_field_UI_AddRemove(btnobj){	

		var r1=btnobj.parentNode.parentNode;
		var selobj=$(btnobj.form).select("#dld_blbfield1")[0];
		if (r1 != null && r1.rowIndex == ($(btnobj.form).select("#dld_fields1")[0].rows.length - 1)) {
			var inps = $(r1).select('input');
			if (inps.length >= 0 && selobj.options[selobj.selectedIndex].value!='' ) {
				
				//var r2 = r1.cloneNode(true);
				var mi_button=inps[0].cloneNode(true);				
				mi_button.src="images/button_minus.gif";
				
				var r2=$(btnobj.form).select("#dld_fields1")[0].insertRow(r1.rowIndex+1);
				r2.insertCell(r2.cells.length).appendChild(selobj)
				r2.insertCell(r2.cells.length).appendChild(inps[0])			
				
				r1.cells[0].appendChild(document.createTextNode(selobj.options[selobj.selectedIndex].text));
				r1.cells[1].appendChild(mi_button);
				r1.setAttribute('rowid',selobj.options[selobj.selectedIndex].value)
 
			}
	
			
		}else if (r1 != null && r1.rowIndex<r1.parentNode.rows.length) {
			$(r1).remove();			 
		} 
		bbpl_updateFieldsDDList(selobj);
		return false;
	
}


function bbpl_dld_suffixValidate(obj){
	$(obj.form).select("#dld_marketsector1")[0].value=obj.options[obj.selectedIndex].value;
}



function bbpl_collectSelectedFields(fieldid){
	var trows=$(fieldid).select("#dld_fields1")[0].rows;
	var selids=new Array();
	var selidss="";
	for(iac=0;iac<trows.length;iac++){
		if(trows[iac].getAttribute('rowid')!=null && trows[iac].getAttribute('rowid')!=''){
			selids[selids.length]=trows[iac].getAttribute('rowid');
			selidss+=(selidss=="")?trows[iac].getAttribute('rowid'):","+trows[iac].getAttribute('rowid');
		}
	}	
	return selidss;
}

function bbpl_collectSelectedFieldsArry(fieldid){
	var trows=$(fieldid).select("#dld_fields1")[0].rows;
	var selids=new Array(); 
	for(iac=0;iac<trows.length;iac++){
		if(trows[iac].getAttribute('rowid')!=null && trows[iac].getAttribute('rowid')!=''){
			selids[selids.length]=trows[iac].getAttribute('rowid');			 
		}
	}
	return selids;
}


function bbpl_jsCreate(fieldid) {
	return bbpl_collectQueryData(fieldid);
}


function bbpl_collectQueryData(fieldid){
	var data=new Object();
	data.mkt_securities=false;
	data.commodity="";
	
	if($(fieldid).select("#dld_mkt_securites1")[0].checked){
		data.mkt_securities=true;
	}else{
		//data.commodity=$("dld_commodity").value;
	}
	data.date=getDateInputs($(fieldid).select("#dld_datefilter1")[0]);	 
	data.contracts=$(fieldid).select("#dld_contracts1")[0].value;
	data.marketsector=$(fieldid).select("#dld_marketsector1")[0].value;
	data.fieldids=bbpl_collectSelectedFields(fieldid);
	data.filtervalue=data.date.filtervalue;
	data.date1s=data.date.date1s;
	data.date2s=data.date.date2s;
	data.number=data.date.number;
	
	//data.fieldids=data.fieldids.
	
			
	
	/*	
	tbrows=$("dld_fields").rows;
	data.blbfields=new Array();
	data.dbfields=new Array();
	for(iab=0;iab<tbrows.length;iab++){                
		if(tbrows[iab].cells.length>2){
	           var inpc=$(tbrows[iab]).select('input');
			   if(inpc.length<2){
			   		data.blbfields[data.blbfields.length]=tbrows[iab].cells[0].innerHTML;
					data.dbfields[data.dbfields.length]=tbrows[iab].cells[1].innerHTML;	
			   }else if (inpc.length>=2){
			   		if(inpc[0].value!='' && inpc[1].value!=''){
						data.blbfields[data.blbfields.length]=inpc[0].value;
						data.dbfields[data.dbfields.length]=inpc[1].value;	
					} 
			   } 		
		}
	}
	*/
		
	return data;
}


function bbpl_jsFetch(fieldid, data){
	
	
	
 
 	if(data!=null){
		
		currentQueryId=data.id;	
		currentQueryData=data;
		
		
		//$("dld_downloadBtn").show();
		//$("dld_deleteBtn").show();		
		//$("dld_queryEditorForm").reset();
		//$("dld_editor").show();
		//$("dld_editorHeader").hide();
		
		//$("dld_queryEditorForm1").reset();

	 	
		//if($(fieldid).select("#dld_queryEditorForm1")[0]==null){
			
		if(false){	
			if($j("#"+fieldid).has("#dld_queryEditorForm1").length>0){
				$(fieldid).select("#dld_queryEditorForm1")[0].reset();
			}else{
				$("dld_queryEditorForm1").reset();
				$(fieldid).appendChild($("dld_queryEditorForm1").cloneNode(true));			
			}		
		}
		$j("#"+fieldid+" #dld_queryEditorForm1")[0].reset();
		
		$(fieldid).select("#dld_editor1")[0].show();	
			 
		bbpl_resetFieldList(null,fieldid);
	
		

		if(data.is_mkt_securitydb!=null && data.is_mkt_securitydb=='true'){
			$(fieldid).select("#dld_mkt_securites1")[0].checked=true;
			$(fieldid).select("#dld_mkt_contracts1")[0].checked=false;	
		} else{
			$(fieldid).select("#dld_mkt_contracts1")[0].checked=true;
			$(fieldid).select("#dld_mkt_securites1")[0].checked=false;
		}		
 
	   	var dfilt = $(fieldid).select("#dld_datefilter1")[0];
	   	for (iab = 0; iab < dfilt.options.length; iab++) {
	   		if (dfilt.options[iab].value == data.date_option) {
	   			dfilt.options.selectedIndex = iab;
	   		}
	   	}
	   
		onDateOption(dfilt);
	    
	   
		 
		if(data.date_recentnumber!=null){
			try{
				var inp1=$($(fieldid).select("#dld_datefilter1")[0].parentNode).select('input#filternumberrange');	
				if(inp1.length>0){
					inp1[0].value=data.date_recentnumber;
				}
			}catch(e){}
		}
		  
		if(data.date_from!=null){
			try{
				var inp1=$($(fieldid).select("#dld_datefilter1")[0].parentNode).select('input#filterdate1');	
				if(inp1.length>0){
					inp1[0].value=data.date_from;
				}
			}catch(e){}
		}
		 
		if(data.date_to!=null){
			try{	
				var inp1=$($(fieldid).select("#dld_datefilter1")[0].parentNode).select('input#filterdate2');	
				if(inp1.length>0){
					inp1[0].value=data.date_to;
				}
			}catch(e){}
		}	

		
	 
		bbpl_resetFieldList(data.fieldsids,fieldid);		
		//refreshFieldTables(data.fieldsmap);
		

		$(fieldid).select("#dld_marketsector1")[0].value=data.marketsector;		
		$(fieldid).select("#dld_contracts1")[0].value=data.tickers;	
		
		//show logs....
		 

		
	}
	 
}


//field_mapping=rtndata.field_mapping;
//updateFieldsDDList();
			
