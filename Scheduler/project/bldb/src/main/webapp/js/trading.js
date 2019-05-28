

function onDateOption(selobj){
	//$("allTablesxl").options[$("allTablesxl").selectedIndex]
	//alert(selobj.options[selobj.selectedIndex].getAttribute('type'));
	 
	//alert(selobj.parentNode.getElementsByTagName("div")); 
	
	
		
	//var dtoarea=$(selobj.parentNode.getElementsBy).select("div#dateOptionArea")[0];
	var dtoarea=selobj.parentNode.getElementsByTagName("div")[0];
	curDtFilterType=selobj.options[selobj.selectedIndex].getAttribute('type');
	
	curDtFilterValue=selobj.options[selobj.selectedIndex].value
	 
	dtoarea.innerHTML="";
	
		
	if(curDtFilterType=='daterange' || curDtFilterType=='datefrom' ){
		
		var date1=document.createElement('input');
		date1.setAttribute('type','text');
		date1.setAttribute('size','19');		//validate		
		date1.setAttribute('id','filterdate1');
		date1.style.fontSize=".8em";
		dtoarea.appendChild(document.createTextNode("Fr:"));
		dtoarea.appendChild(date1);
		if (curDtFilterType != 'datefrom') {
			var date2=date1.cloneNode(true);		
			date2.setAttribute('id','filterdate2');
			dtoarea.appendChild(document.createTextNode("To:"));
			dtoarea.appendChild(date2);
			$j(date2).datetimepicker({controlType: 'select',timeFormat: 'hh:mm TT',dateFormat:'dd/mm/yy'});
		}
		$j(date1).datetimepicker({controlType: 'select',timeFormat: 'hh:mm TT',dateFormat:'dd/mm/yy'});
	}else if(curDtFilterType=='number'){
		
		var ninp=document.createElement('input');
		ninp.setAttribute('type','text');
		ninp.setAttribute('id','filternumberrange');
		ninp.setAttribute('size','5');
		dtoarea.appendChild(ninp);
	}
	
}

function isDate1(ds){
	var rtn={};
	rtn.success=true;
	var d1=moment(ds, "DD/MM/YYYY h:mm A");
    rtn.day=d1.date();
    rtn.month=d1.month()+1;
    rtn.year=d1.year();    
    rtn.smonth=(rtn.month<10)?"0"+rtn.month:""+rtn.month;
	rtn.sday=(rtn.day<10)?"0"+rtn.day:""+rtn.day;;
	
	rtn.sqldate=d1.format("YYYY-MM-DD HH:mm:ss");
	return rtn;
	
}


function getDateInputs(sel_obj_id,cdfield){

	
	var rtnobj=new Object();
	
	var cdatefield="cdate";
	
	if(cdfield!=null){
		cdatefield=	cdfield;
	}
	
	//var dtoarea=$($(sel_obj_id).parentNode).select("#dateOptionArea")[0];
	var dtoarea=null;
	var pelem=(typeof sel_obj_id=='object') ? sel_obj_id.parentNode :document.getElementById(sel_obj_id).parentNode; 						
	var divs=pelem.getElementsByTagName("div"); 
	for(iab=0;iab<divs.length;iab++) {
		if(divs[iab].id=='dateOptionArea'){
			dtoarea=$(divs[iab]);
		}
	}
 
 	rtnobj.cdatefield=cdatefield;
    rtnobj.query=null;
    rtnobj.queryCheck=false;
	rtnobj.dobj1Success=false;
	rtnobj.dobj2Success=false;    
	rtnobj.filtertype=$(sel_obj_id).options[$(sel_obj_id).selectedIndex].getAttribute('type');
	rtnobj.filtervalue=$(sel_obj_id).options[$(sel_obj_id).selectedIndex].value;
	 
	if (rtnobj.filtertype == 'daterange') {
	
		//var dobj1=isDate(dtoarea.select("#filterdate1")[0].value);
		//var dobj2=isDate(dtoarea.select("#filterdate2")[0].value);
		
		var dobj1=isDate1($j(dtoarea).find("#filterdate1").val());
		var dobj2=isDate1($j(dtoarea).find("#filterdate2").val());
		if( dobj1.success && dobj2.success){			 
			rtnobj.queryCheck=true;
			rtnobj.dobj1Success=true;
			rtnobj.dobj2Success=true;
			rtnobj.dobj1Sqldate=dobj1.sqldate;
			rtnobj.dobj2Sqldate=dobj2.sqldate;
		}
		rtnobj.date1=dobj1;
		rtnobj.date2=dobj2;
		rtnobj.date1s=dtoarea.select("#filterdate1")[0].value;
		rtnobj.date2s=dtoarea.select("#filterdate2")[0].value;
		
	}else if (rtnobj.filtertype == 'datefrom' ) {
		//var dobj1=isDate(dtoarea.select("#filterdate1")[0].value);
		var dobj1=isDate1($j(dtoarea).find("#filterdate1").val());
		rtnobj.date1=dobj1; 
		rtnobj.dobj1Success=dobj1.success;
		if( dobj1.success){			 
			rtnobj.queryCheck=true;
			rtnobj.dobj1Sqldate = dobj1.sqldate;			
		}
		rtnobj.date1s=dtoarea.select("#filterdate1")[0].value;
		
	}else if(rtnobj.filtertype == 'number'){
		var numbr=parseInt(dtoarea.select("#filternumberrange")[0].value);
		rtnobj.number=numbr;
		if (isNaN(numbr)) {
			alert("Please input only number on Last Date/Week/Months/Years");
		}
		else{
			rtnobj.queryCheck=true;
		}
	}else{
		rtnobj.queryCheck=(rtnobj.filtervalue!=null && rtnobj.filtervalue!='')?true:false;
		if(rtnobj.filtervalue.indexOf('$')>=0 && rtnobj.filtervalue.split('$').length==2){
			rtnobj.number=rtnobj.filtervalue.split('$')[1];
			rtnobj.filtervalue=rtnobj.filtervalue.split('$')[0];
		} 
			
		
		
	}	
	return rtnobj;
	


}

