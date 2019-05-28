

function fn_asyncExec(){
	
	$j("#a_result").hide();
	$j.ajax({
  	type: 'POST',
  	url: "schedulerAPI",
  	data: {engine:$j("#id_engine").val(), script:$j("#exe_script").val(),method:"execAsyncScript",executeAt:$j("#id_executeAt").val()},
	success: function(data){
		//alert(data);
		$j("#getResultBtn").val("GET RESULT OF TOKEN: '"+$j.trim(data)+"'");
		$j("#getResultBtn").attr("token",$j.trim(data));
		$j("#a_result").show();
		$j("#script_result").val("");		
	}	
	});
  	

}

function get_result(tobj){
	$j.ajax({
  	type: 'GET',
  	url: "schedulerAPI",
  	data: {token:$j(tobj).attr("token"),method:"getScriptResult",responseType:"text/plain"},
	success: function(data){	
		$j("#script_result").val(data);		 
	}	
	});
	
}
