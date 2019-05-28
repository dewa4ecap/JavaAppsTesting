

function ld_onDateSelectxl(selobj){
	//$("allTablesxl").options[$("allTablesxl").selectedIndex]
	//alert(selobj.options[selobj.selectedIndex].getAttribute('type'));
	
	curDtFilterType=selobj.options[selobj.selectedIndex].getAttribute('type');
	curDtFilterValue=selobj.options[selobj.selectedIndex].value
	$("ld_dateInputArea").innerHTML="";
	
	if(curDtFilterType=='daterange'){
		
		var date1=document.createElement('input');
		date1.setAttribute('type','text');
		date1.setAttribute('size','8');
		//validate
		
		
		var date2=date1.cloneNode(true);		
		date2.setAttribute('id','ld_date2');
		
		date1.setAttribute('id','ld_date1');
		$("ld_dateInputArea").appendChild(document.createTextNode("From:"));
		$("ld_dateInputArea").appendChild(date1);
		$("ld_dateInputArea").appendChild(document.createTextNode("To:"));
		$("ld_dateInputArea").appendChild(date2);
		
	}else if(curDtFilterType=='number'){
		
		var ninp=document.createElement('input');
		ninp.setAttribute('type','text');
		ninp.setAttribute('id','ld_numberrange');
		ninp.setAttribute('size','5');
		$("ld_dateInputArea").appendChild(ninp);
	}
	
}

var ld_req_outputmode=0;
function swapTxtBox(obj){
	
	$("ld_contractsxl").value="";
	$("ld_fieldname").value="";
	if(obj.value=='1'){
		$("ld_textArea").innerHTML="Field Names:";
		$("ld_fieldArea").innerHTML="Security Identifier:";
		ld_req_outputmode=1;
	}else{
		$("ld_textArea").innerHTML="Security Identifiers:";
		$("ld_fieldArea").innerHTML="Field Name:";
		ld_req_outputmode=0;
		
	}
}

var ld_outputto=0;
function swapOutputTo(obj){
	ld_outputto=0;
	if(obj.value=='1'){
		ld_outputto=1;		
	}
}



function ld_suffixValidate(obj){
	$("ld_marketsector").value=obj.options[obj.selectedIndex].value;
}


var ld_req_contracts=null;
var ld_req_datefrom=null;
var ld_req_dateto=null;
var ld_req_dtrange=null;
var ld_req_sector=null;
var ld_req_field=null;


function ld_getInputRangesxl(){
	
	var invalidate=null;
	if($("ld_contractsxl").value!=null && $("ld_contractsxl").value.strip()!=''){
		ld_req_contracts=$("ld_contractsxl").value.strip();
	}
	
	ld_req_sector=$("ld_marketsector").value;
	ld_req_field=$("ld_fieldname").value;
	
	if (curDtFilterType == 'daterange' ) {
		var dobj1=isDate($('ld_date1').value);
		var dobj2=isDate($('ld_date2').value);
		if( dobj1.success && dobj2.success){ 
			ld_req_datefrom=dobj1.year+""+dobj1.smonth+""+dobj1.sday;
			ld_req_dateto=dobj2.year+""+dobj2.smonth+""+dobj2.sday;
		}else{
			invalidate="Invalid Date Range";
		}
	}else if(curDtFilterType == 'number'){
		var numbr=parseInt($('ld_numberrange').value);
		if (isNaN(numbr)) {
			alert("Please input only number on Last Date/Week/Months/Years");
		}else{
			ld_req_dtrange=curDtFilterValue+","+numbr;	
		}
		
	}else{
		ld_req_dtrange=(curDtFilterValue!=null && curDtFilterValue!='')?curDtFilterValue:null;
	}
	

	
	var errMsg="";
	if(ld_req_sector!=null && ld_req_sector!=''){} else{		
		errMsg+="\n\rIndentifier Suffix";		
	}
	if(ld_req_field!=null && ld_req_field!=''){} else{		
		errMsg+="\n\rFieldname";		
	}
	if(ld_req_contracts!=null && ld_req_contracts!=''){} else{		
		errMsg+="\n\rSecurities";		
	}	
	
	if(curDtFilterType == 'daterange'){
		if (invalidate != null) {
			errMsg += "\n\r" + invalidate;
		}
	}else{
		if(ld_req_dtrange!=null && ld_req_dtrange!=''){}else{
			errMsg+="\n\rSecify Period";
		}
	}
	
	if(errMsg!=''){
		alert("Please check the followings fields are empty or invalid\n\r"+errMsg);	
	}else{
		progress_message("Please wait your request is processing now....");
		var respProxy=function(data){
			stopPolling();
			progress_message(null);			
			if (data.xlid != null) {
				downloadLiveData(data.xlid);
			}else {
				generateBlResult(data);
			}
			
		}	
		
		MigrationMgmt.getBloombergLive(ld_req_dtrange,ld_req_datefrom,ld_req_dateto,ld_req_contracts,ld_req_sector,ld_req_field,ld_req_outputmode,ld_outputto,respProxy);
		startPolling();
	}
}

function downloadLiveData(xlid){
	$("xlid").value=xlid;
	$("downloadlive").submit();
}

function generateBlResult(data){
			 
	row=data.row;
	rdata=data.data;
	col=data.col;
	var table=document.createElement('table');
	table.style.border="1px solid #CCCCCC";	
	table.style.borderCollapse="collapse";
	
	var hrow=table.insertRow(0);
	var hrcell=hrow.insertCell(0); //date cell
	hrcell.style.backgroundColor="#CCCCCC";
	hrcell.style.padding="4px";
	for(i=0;i<col.length;i++){
		var hcell=hrow.insertCell(hrow.cells.length);
		hcell.style.backgroundColor="#CCCCCC";
		hcell.style.padding="4px";
		hcell.appendChild(document.createTextNode(col[i]));
	}
	
	//for(i=0;i<rdata.length;i++){
	for(i=rdata.length-1;i>=0;i--){
		var drow=table.insertRow(table.rows.length);
		crowdata=rdata[i];
		fcol=drow.insertCell(0);
		fcol.style.backgroundColor="#CCCCCC";
		fcol.style.padding="4px";
		fcol.appendChild(document.createTextNode(row[i]));
		for (a = 0; a < crowdata.length; a++) {
			var crcell=drow.insertCell(drow.cells.length);
			crcell.setAttribute('align','right');
			crcell.style.border="1px solid #cccccc";
			crcell.style.padding="4px";
			crcell.appendChild(document.createTextNode(crowdata[a]));
		}
	}
	$("liveoutput").innerHTML="";
	$("liveoutput").appendChild(table);
	
}
