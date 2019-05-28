

function fn_asyncExec(){
	$j("#a_result").hide();
	$j.ajax({
  	type: 'POST',
  	url: "schedulerAPI",
  	data: {engine:"rserve", script:$j("#exe_script").val(),method:"execAsyncScript",executeAt:$j("#id_executeAt").val()},
	success: function(data){
		var token = $j.trim(data);
		$j("#getResultBtn").val("GET RESULT OF TOKEN ==> '"+token+"'");
		$j("#getResultBtn").attr("token",$j.trim(data));
		$j("#a_result").show();
		$j("#script_result").val("");
	}	
	});
}

function fn_asyncWeb() {
	$j("#a_result").hide();
	$j.ajax({
  	type: 'POST',
  	url: "schedulerAPI",
  	data: {engine:"rserve", script:$j("#exe_script").val(),method:"execAsyncScript"},
	success: function(data){
		var token = $j.trim(data);
		$j("#getResultBtn").val("GET RESULT OF TOKEN ==> '"+token+"'");
		$j("#getResultBtn").attr("token",$j.trim(data));
		$j("#a_result").show();
		$j("#script_result").val("");
		
		setTimeout(function writeResult() {
			$j.ajax({
			  	type: 'GET',
			  	url: "schedulerAPI",
			  	data: {token:token,method:"getScriptResult",responseType:"text/plain"},
				success: function(data){	
					$j("#script_result").val("Please wait...");	
					if (data.includes("<result>not ready</result>")) {
						writeResult();
					} else {
						$j("#script_result").val(data);
					}
				}	
				});
		}, 500);
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