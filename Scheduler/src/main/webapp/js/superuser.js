

var su={};



su.login=function(){
	
	
	
	
	//$j("#wpLoginattempt").attr("disabled", "disabled");
	var usr=$j("#wpName1").val();
	var pwd=$j("#wpPassword1").val();
	var changepwd=null;
	var respBack=function(data){
		$j("#wpLoginattempt").removeAttr("disabled");
		$j(".mw-status-tr").hide();
		if(data!=null && data.loggedin){		
			
	 
			if(su.referer==null ||(su.referer!=null && su.referer=='')){su.referer="scheduler.jsp"}
			su.showStatus("Redirecting.....");
			
			if(su.referer.indexOf("http:")>=0 || su.referer.indexOf("https:")>=0){
				 window.location=su.referer;
			}else{
			    window.location.replace(su.referer);
			}
		}
		if(data!=null && !data.loggedin &&  !data.message!=null ){			 
			su.showMsg(data.message);
		}		
		
	}
	if($j("#wpChangePwdCkbox").is(":checked") && $j("#wpNewPassword").val()==''){		
		su.showMsg("New Password is empty ");
		return false;	
	}
	if($j("#wpChangePwdCkbox").is(":checked") && $j("#wpNewPassword").val()!=''){
		changepwd=$j("#wpNewPassword").val();
	}
	if(usr!='' && pwd!=''){
		su.showStatus("Validating...");
		$j("#wpLoginattempt").attr("disabled", "disabled");	
		LoginMgmt.loginSuperUser(usr,pwd,changepwd,respBack);
	}else{
		su.showMsg('Username or Password empty');	
	}
	
	return false;
}

su.changepass=function(thisobj){
	$j("td#wpOldPassword").text("Your password:");
	if($j(thisobj).is(':checked')){
		$j('tr.mw-changepwd').show();	
		$j("td#wpOldPassword").text("Old password:");
	}else{
		$j('tr.mw-changepwd').hide();
	}	
	
}

su.showMsg=function(msg){
	$j(".mw-status-tr").show();
	$j(".mw-status").html(msg);
}


su.showStatus=function (msg){
	$j(".mw-status-tr").show();
	$j(".mw-status").html("<img src='images/loading.gif' border='0'> "+msg);
}






DWREngine.setErrorHandler(
	function (message){
		
		if(message.indexOf("SESSION LOGGED OUT:")>=0){
			if(typeof message_status=='function'){
				//message_status("Your request failed, Pleae redo again after logged in");
			}
			showLoginDialog();	
		}else{
			if(typeof message_status=='function'){
				message_status(message);
			}
		}
		
	}
);

 
$j(function(){
	su.changepass($j("#wpChangePwdCkbox"));
});


