
myTriggerTable=null;

$j(function() {
	
		$j( "#dlg-msg" ).dialog({
			modal: true,
			autoOpen: false,
			show: 'explode',
			width: 500, 
			buttons: {
				Ok: function() {
					$j( this ).dialog( "close" );
				}
			}
		});
	});

var trigger_data=[["","","","","","",""]];

$j(function() {
myTriggerTable=new MyTable("trigger_table",trigger_data);  //should be removed later also removes the references in scheduler.js
});

/*
$j(function() {
	myTriggerTable=new MyTable(
	    "trigger_table",
	    [
		  "Second <a tips=\"&lt;b&gt;Examples:&lt;/b&gt; &lt;br&gt;Every Second:* &lt;BR&gt;Range: 1-50 &lt;br&gt; Specific: 2,4,40,59 &lt;/small&gt; &lt;br&gt; Specific Intervals: 2/10 &lt;small&gt;(starting at 2 and every 10 seconds)&lt;/small&gt;&lt;br&gt;\" ttype=\"tips\" href=\"#\"> ?</a>",
		  "Minute <a tips=\"&lt;b&gt;Examples:&lt;/b&gt; &lt;br&gt;Every Minute:*&lt;br&gt;Range: 1-50 &lt;br&gt; Specific: 2,4,40,59 &lt;br&gt; Specific Intervals: 2/10 &lt;small&gt;(starting at 2 and every 10 minutes)&lt;/small&gt;\" ttype=\"tips\" href=\"#\"> ?</a>",
		  "Hour <a tips=\"&lt;b&gt;Examples:&lt;/b&gt;&lt;br&gt;Every Hour:*&lt;br&gt;Range: 0-23 &lt;br&gt; Specific: 2,4,6,17 &lt;br&gt; Specific Intervals: 2/5 &lt;small&gt;(starting at 2 and every 5 hours)&lt;/small&gt;\" ttype=\"tips\" href=\"#\"> ?</a>",
		  "Day of the Wk <a tips=\"&lt;b&gt;Examples:&lt;/b&gt; &lt;br&gt;&lt;br&gt;Every Week:*&lt;br&gt; Range: 1-7 or MON-FRI &lt;br&gt; Specific: 1,3,5 or SUN,MON,TUE &lt;br&gt; Specific Intervals: 2/2 &lt;small&gt;(starting from Monday  and every 2 days )&lt;/small&gt;&lt;br&gt;&lt;br&gt;&lt;span class='smalltips'&gt; The 'L' character is allowed for the day-of-week Field. This character is short-hand for 'last', it simply means '7' or 'SAT'. But if used after another value, it means 'the last xxx day of the month' - for example '6L' means 'the last friday of the month'. When using the 'L' option, it is important not to specify lists, or ranges of values, as you'll get confusing results. &lt;br&gt;&lt;br&gt; The '#' character is allowed for the day-of-week field. This character is used to specify 'the nth' XXX day of the month. For example, the value of '6#3' in the day-of-week field means the third Friday of the month (day 6 = Friday and '#3' = the 3rd one in the month). Other examples: '2#1' = the first Monday of the month and '4#5' = the fifth Wednesday of the month. Note that if you specify '#5' and there is not 5 of the given day-of-week in the month, then no firing will occur that month. If the '#' character is used, there can only be one expression in the day-of-week field ('3#1,6#3' is not valid, since there are two expressions). &lt;/span&gt; \" ttype=\"tips\" href=\"#\"> ?</a>",
		  "Day of the Mn <a tips=\"&lt;b&gt;Examples:&lt;/b&gt; &lt;br&gt;&lt;br&gt;Every Day:*&lt;br&gt;Range: 1-31 &lt;br&gt; Specific: 1,3,5,15,25 &lt;br&gt; Specific Intervals: 2/5 &lt;small&gt;(starting from 2nd of the month and every 5 days )&lt;/small&gt; &lt;br&gt;&lt;br&gt;&lt;span class='smalltips'&gt;The 'L' character is allowed for the day-of-month Field. This character is short-hand for 'last', means 'the last day of the month' - day 31 for January, day 28 for February on non-leap years.  When using the 'L' option, it is important not to specify lists, or ranges of values, as you'll get confusing results.&lt;br&gt;&lt;br&gt;The 'W' character is allowed for the day-of-month field. This character is used to specify the weekday (Monday-Friday) nearest the given day. As an example, if you were to specify '15W' as the value for the day-of-month field, the meaning is: 'the nearest weekday to the 15th of the month'. So if the 15th is a Saturday, the trigger will fire on Friday the 14th. If the 15th is a Sunday, the trigger will fire on Monday the 16th. If the 15th is a Tuesday, then it will fire on Tuesday the 15th. However if you specify '1W' as the value for day-of-month, and the 1st is a Saturday, the trigger will fire on Monday the 3rd, as it will not 'jump' over the boundary of a month's days. The 'W' character can only be specified when the day-of-month is a single day, not a range or list of days.&lt;br&gt;&lt;br&gt;The 'L' and 'W' characters can also be combined for the day-of-month expression to yield 'LW', which translates to 'last weekday of the month'.&lt;/span&gt; \" ttype=\"tips\" href=\"#\"> ?</a>",
		  "Month <a tips=\"&lt;b&gt;Examples:&lt;/b&gt; &lt;br&gt;&lt;br&gt;Every Month:*&lt;br&gt;Range: 1-12 or JAN-JUN&lt;br&gt; Specific: 1,3,5,12  or JAN,FEB,MAR &lt;br&gt; Specific Intervals: 2/5 &lt;small&gt;(starting from 2nd of the month and every 5 days )&lt;/small&gt;  \" ttype=\"tips\" href=\"#\"> ?</a>"
		]
	);
	
	var data=trigger_data;
	myTriggerTable.update(data);

});

*/




 




var MyTableInstances=new Array();

function getMyTableInstance(id){
	return MyTableInstances[id];
}


function addMyTableRow(id){
	var mi=getMyTableInstance(id);	
	mi.getData().splice(mi.getData().length,0,["","","","","","",""]);
	mi.update();
}

function removeMyTableRow(id, idx){	
	getMyTableInstance(id).getData().splice(idx,1);
	getMyTableInstance(id).update();
}

function getTriggerTime(id,idx){
	var respBack=function(data){
		
		//alert(data);
		$j("#dlg-msg-bd").html("");
		for(iab=0;iab<data.length;iab++){
			$j("#dlg-msg-bd").append("<div>"+data[iab]+"</div>");
		}		
		$j("#dlg-msg").dialog('open')

	}
	var data=getMyTableInstance(id).getData()[idx];
	var timezone=$j("#"+id).parent().find("input[name='timezone']").val();
	SchedulerMgmt.getNext10TriggerTimings(data[0], data[1], data[2], data[3], data[4], data[5],timezone,respBack);
	
}


function MyTable(divid1, headers1){
	this.divid=divid1;
	this.headers=headers1;
	this.data=null;
	this.multipletxt=new Array();
	MyTableInstances[divid1]=this;
}



MyTable.prototype.getData=function(){
	return this.data;
}



MyTable.prototype.getDBFormattedData=function(){
	//return this.data;
	var rtn=new Array();
	for(iab=0;iab<this.data.length;iab++){
		if(this.data[iab].length==7) {			
		      var v=new Object();
			  v.exp_second=this.data[iab][0]
			  v.exp_minute=this.data[iab][1]
			  v.exp_hour=this.data[iab][2]
			  v.exp_week=this.data[iab][3]
			  v.exp_day=this.data[iab][4]
			  v.exp_month=this.data[iab][5]
			  v.inject_code=this.data[iab][6]
			  rtn[rtn.length]=v;
		}
		
	}
	return rtn;
}



MyTable.prototype.update=function(data){
	if (data != null) {
		this.data = data;
	}
	var th="";
	for(iab=0;iab<this.headers.length;iab++){
		
		var ttl= this.headers[iab];
		var myRegexp = /(\[)(.*?)(\]+)/g;
		var match = myRegexp.exec(ttl);
		if(match!=null && match.length>=4 && match[1]=='[' && match[3]==']'){
			ttl=match[2];
			this.multipletxt[iab]=true;
		}
		th+='<th align="center">'+ttl+'</th>';
	}
	th+='<th></th>';
	
	$j("#"+this.divid).html('<table width="100%" id="table_'+this.divid+'" cellspacing="0"><thead><tr>'+th+'</tr></thead><tbody></tbody></table><a href="#" onclick="addMyTableRow(\''+this.divid+'\');return false;"><img src="images/plusButton.png" border="0" class="plusButton"></a>');
	var td='';
		
	for (iaa = 0; iaa < this.data.length; iaa++) {
		    td+='<tr>';
			for (iab = 0; iab < this.headers.length; iab++) {
				var val="";
				if (this.data[iaa][iab]!=null) {
					//td += '<td>' + this.data[iaa][iab] + '</td>';
					val=this.data[iaa][iab];
				}
				if(this.multipletxt[iab]!=null && this.multipletxt[iab]){
					td += '<td align="center"><textarea   divid="'+this.divid+'" idx="'+[iaa]+','+[iab]+'"  class="mytableTextArea">'+val+'</textarea></td>';					
				}else{
					td += '<td align="center"><input type="text" value="'+val+'" divid="'+this.divid+'" idx="'+[iaa]+','+[iab]+'"  class="mytableInput"></td>';
				}
			}
			td += '<td align="right"><a href="#" onclick="getTriggerTime(\''+this.divid+'\','+iaa+');return false;"><img src="images/timeButton.png" border="0"></a><a href="#" onclick="removeMyTableRow(\''+this.divid+'\','+iaa+');return false;"><img src="images/minusButton.png" border="0"></a></td>';
			td+='</tr>';
	}
	$j('table#table_'+this.divid+' tbody').append(td);
	$j('table#table_'+this.divid).addClass("myTableCSS");
	$j('table#table_'+this.divid+' a[ttype="tips"]').each(function(idx,ele){
		var myTooltip = new YAHOO.widget.Tooltip("myTooltip", {autodismissdelay:15000,width:400, context:ele, text:$j(ele).attr("tips") } );
	}); 


	
	$j('table#table_'+this.divid+' input.mytableInput[value=""]').addClass("empty");
	
	$j('table#table_'+this.divid+' input.mytableInput').focus(function(){
		$j(this).addClass("active");	
	});
	
	var enableDisablePlus=function(divid){
		var mi=getMyTableInstance(divid);
		var last=mi.getData()[mi.getData().length-1]
		var lastvalid=false;
		if(mi.getData().length<=0){
			lastvalid=true;
		}		
		if(last!=null){
			for(iabc=0;iabc<6;iabc++) {
				if(last[iabc]!=null && last[iabc].trim()!=''){
					lastvalid=true;		
				} 	
			}
		}
		if(lastvalid){
			$j('#'+divid+" .plusButton").removeClass("disabled");
		}else{
			$j('#'+divid+" .plusButton").addClass("disabled");
		}
	}
	enableDisablePlus(this.divid);
	$j('table#table_'+this.divid+' input.mytableInput').keyup(function(){
		var divid=$j(this).attr("divid");
		enableDisablePlus(divid);	
	});
	$j('table#table_'+this.divid+' input.mytableInput, table#table_'+this.divid+' textarea.mytableTextArea').blur(function(){
		$j(this).removeClass("active");
		if($j(this).val()==''){
			$j(this).addClass("empty");
		}else{
			$j(this).removeClass("empty");
		}
		var idx=$j(this).attr("idx").split(",");
		var divid=$j(this).attr("divid");
		var mi=getMyTableInstance(divid);		
		mi.getData()[parseInt(idx[0])][parseInt(idx[1])]=$j(this).val();        
		enableDisablePlus(divid);	
	
	});
					
	 
		
}

function getTriggerTable(divid) {
	var trigger_data1=[["","","","","","",""]];
	var rtn =new MyTable(
	    divid,
	    [
		  "Sec <a tips=\"&lt;b&gt;Examples:&lt;/b&gt; &lt;br&gt;Every Second:* &lt;BR&gt;Range: 1-50 &lt;br&gt; Specific: 2,4,40,59 &lt;/small&gt; &lt;br&gt; Specific Intervals: 2/10 &lt;small&gt;(starting at 2 and every 10 seconds)&lt;/small&gt;&lt;br&gt;\" ttype=\"tips\" href=\"#\"> ?</a>",
		  "Min <a tips=\"&lt;b&gt;Examples:&lt;/b&gt; &lt;br&gt;Every Minute:*&lt;br&gt;Range: 1-50 &lt;br&gt; Specific: 2,4,40,59 &lt;br&gt; Specific Intervals: 2/10 &lt;small&gt;(starting at 2 and every 10 minutes)&lt;/small&gt;\" ttype=\"tips\" href=\"#\"> ?</a>",
		  "Hr <a tips=\"&lt;b&gt;Examples:&lt;/b&gt;&lt;br&gt;Every Hour:*&lt;br&gt;Range: 0-23 &lt;br&gt; Specific: 2,4,6,17 &lt;br&gt; Specific Intervals: 2/5 &lt;small&gt;(starting at 2 and every 5 hours)&lt;/small&gt;\" ttype=\"tips\" href=\"#\"> ?</a>",
		  "Wk.Day <a tips=\"&lt;b&gt;Examples:&lt;/b&gt; &lt;br&gt;&lt;br&gt;Every Week:*&lt;br&gt; Range: 1-7 or MON-FRI &lt;br&gt; Specific: 1,3,5 or SUN,MON,TUE &lt;br&gt; Specific Intervals: 2/2 &lt;small&gt;(starting from Monday  and every 2 days )&lt;/small&gt;&lt;br&gt;&lt;br&gt;&lt;span class='smalltips'&gt; The 'L' character is allowed for the day-of-week Field. This character is short-hand for 'last', it simply means '7' or 'SAT'. But if used after another value, it means 'the last xxx day of the month' - for example '6L' means 'the last friday of the month'. When using the 'L' option, it is important not to specify lists, or ranges of values, as you'll get confusing results. &lt;br&gt;&lt;br&gt; The '#' character is allowed for the day-of-week field. This character is used to specify 'the nth' XXX day of the month. For example, the value of '6#3' in the day-of-week field means the third Friday of the month (day 6 = Friday and '#3' = the 3rd one in the month). Other examples: '2#1' = the first Monday of the month and '4#5' = the fifth Wednesday of the month. Note that if you specify '#5' and there is not 5 of the given day-of-week in the month, then no firing will occur that month. If the '#' character is used, there can only be one expression in the day-of-week field ('3#1,6#3' is not valid, since there are two expressions). &lt;/span&gt; \" ttype=\"tips\" href=\"#\"> ?</a>",
		  "Mth.Day<a tips=\"&lt;b&gt;Examples:&lt;/b&gt; &lt;br&gt;&lt;br&gt;Every Day:*&lt;br&gt;Range: 1-31 &lt;br&gt; Specific: 1,3,5,15,25 &lt;br&gt; Specific Intervals: 2/5 &lt;small&gt;(starting from 2nd of the month and every 5 days )&lt;/small&gt; &lt;br&gt;&lt;br&gt;&lt;span class='smalltips'&gt;The 'L' character is allowed for the day-of-month Field. This character is short-hand for 'last', means 'the last day of the month' - day 31 for January, day 28 for February on non-leap years.  When using the 'L' option, it is important not to specify lists, or ranges of values, as you'll get confusing results.&lt;br&gt;&lt;br&gt;The 'W' character is allowed for the day-of-month field. This character is used to specify the weekday (Monday-Friday) nearest the given day. As an example, if you were to specify '15W' as the value for the day-of-month field, the meaning is: 'the nearest weekday to the 15th of the month'. So if the 15th is a Saturday, the trigger will fire on Friday the 14th. If the 15th is a Sunday, the trigger will fire on Monday the 16th. If the 15th is a Tuesday, then it will fire on Tuesday the 15th. However if you specify '1W' as the value for day-of-month, and the 1st is a Saturday, the trigger will fire on Monday the 3rd, as it will not 'jump' over the boundary of a month's days. The 'W' character can only be specified when the day-of-month is a single day, not a range or list of days.&lt;br&gt;&lt;br&gt;The 'L' and 'W' characters can also be combined for the day-of-month expression to yield 'LW', which translates to 'last weekday of the month'.&lt;/span&gt; \" ttype=\"tips\" href=\"#\"> ?</a>",
		  "Mth <a tips=\"&lt;b&gt;Examples:&lt;/b&gt; &lt;br&gt;&lt;br&gt;Every Month:*&lt;br&gt;Range: 1-12 or JAN-JUN&lt;br&gt; Specific: 1,3,5,12  or JAN,FEB,MAR &lt;br&gt; Specific Intervals: 2/5 &lt;small&gt;(starting from 2nd of the month and every 5 days )&lt;/small&gt;  \" ttype=\"tips\" href=\"#\"> ?</a>",
		  "[Inject Code <a tips=\"This will be inserted between script parameters and your main code, so that this piece of code will override script parameter\"  ttype=\"tips\" href=\"#\">?</a>]",
		]
	);	
	rtn.update(trigger_data1);
	return rtn; 
}


