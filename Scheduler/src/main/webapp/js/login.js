function login(ex){
	
	showStatus("Validating...");
	$j("#wpLoginattempt").attr("disabled", "disabled");
	var usr=$j("#wpName1").val().toLowerCase();
	var pwd=$j("#wpPassword1").val();

	LoginMgmt.login(usr,pwd, function(data) {
		
		$j("#wpLoginattempt").removeAttr("disabled");
		$j(".mw-status-tr").hide();
		if(data!=null && data.loggedin){		
			
			if(data.fesessionuid!=null){
				
                $j.cookie('ALSID', data.ipclient, { expires: 1, path: '/', domain : 'alphien.com' });
                $j.cookie('ALUSER', usr, { expires: 1, path: '/', domain : 'alphien.com' });
				
				$j.cookie('4esessionuid', data.fesessionuid, { expires: 30, path: '/', domain : 'alphien.com' });
				$j.cookie('4euser', usr, { expires: 30, path: '/', domain : 'alphien.com' });
				
				if($j("#wpRemember").is(':checked')){
					$j.cookie('4eprevuser',usr, { expires: 30, path: '/', domain : 'alphien.com' });
				} else{
					$j.cookie('4eprevuser', null, { expires: 0, path: '/', domain : 'alphien.com' });
				}
			}
			
			if(referer==null ||(referer!=null && referer=='')){
				if(ex!=null) {
					referer=ex;
				} else if(ex==null){
					referer="StrategyLaunchpad";
				}
					
			}
			showStatus("Redirecting.....")
			
			if(referer.indexOf("http:")>=0 || referer.indexOf("https:")>=0){
				 window.location=referer;
			}else{
			    window.location.replace(referer);
			}
		}
		if(data!=null && !data.loggedin &&  !data.message!=null ){			 
			showMsg(data.message);
		}
		
	});
	
	return false;
}

$j(function(){
	 var usr=$j.cookie('4euser');	 
	 if(usr!=null){
	 	$j("#wpName1").val(usr);
	 	$j("#wpPassword1").val("");	 		 	
	 }
	 
	 $j.cookie('4esessionuid');
	 if(usr!=null){
	 	
	 }
	 
})

function showMsg(msg){
	$j(".mw-status-tr").show();
	$j(".mw-status").html(msg);
}


function showStatus(msg){
	$j(".mw-status-tr").show();
	$j(".mw-status").html("<img src='images/loading.gif' border='0'> "+msg);
}


DWREngine.setErrorHandler(errorHandleDWR);
function errorHandleDWR(message){
	
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
	//stopPolling();
}



var dialog_loginInit=false;
function showLoginDialog(){
	
	if(dialog_loginInit && $j( "#dialog_login" ).dialog( "isOpen" )){
		return;
	}
	
	$j( "#dialog_login" ).dialog({
		resizable: false,			 
		width:450,
		modal: true,		
		dialogClass: 'gen_dialog'		 
	});
	dialog_loginInit=true;
	
	 
	 var usr=$j.cookie('4euser');	 
	 if(usr!=null){
	 	$j("#wpName1").val(usr);
	 	$j("#wpPassword1").val("");
	 	$j("#wpName1").focus();	 		 	
	 }else{
	 	$j("#wpPassword1").focus();
	 }
	 if(typeof progress_message=='function'){
			progress_message(null);
	 }
	 showMsg("Your request failed! Please login and continue");
}

function loginDialog(){
	
	
	
	showStatus("Validating...");
	$j("#wpLoginattempt").attr("disabled", "disabled");
	var usr=$j("#wpName1").val();
	var pwd=$j("#wpPassword1").val();
	
	var respBack1=function(data){
		$j("#wpLoginattempt").removeAttr("disabled");
		$j(".mw-status-tr").hide();
		if(data!=null && data.loggedin){		
			
			if(data.fesessionuid!=null){
				//$j.cookie('ALSID', data.ipclient, { expires: 1, path: '/', domain : 'alphien.com' }); //gama_hapus
                //$j.cookie('ALUSER', usr, { expires: 1, path: '/', domain : 'alphien.com' });	//gama_hapus
				
				$j.cookie('4esessionuid', data.fesessionuid, { expires: 30, path: '/', domain : 'alphien.com' });
				$j.cookie('4euser', usr, { expires: 30, path: '/', domain : 'alphien.com' });
			}			
			$j("#dialog_login").dialog( "close" );
		}
		if(data!=null && !data.loggedin &&  !data.message!=null ){			 
			showMsg(data.message);
		}		
		
	}
	
	LoginMgmt.login(usr,pwd,respBack1);
	
	return false;
}
