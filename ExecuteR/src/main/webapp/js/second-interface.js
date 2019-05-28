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
						
						var pos1 = data.search("<result>");
						$j("#script_result_html").empty();
						
						if (pos1 > 0) {
							var pos2 = data.search("</result>");
							var html = data.substring(pos1+8,pos2-4);
							var rep1 = html.replace("<string>","");
	                        var rep2 = rep1.replace("</string>","");
	                        $j("#script_result_html").append(rep2);
						}
					}
				}	
				});
		}, 500);
	}	
	});
}