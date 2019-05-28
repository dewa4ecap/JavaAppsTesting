
function js_excecuteScript(){
	var js=$("js_rhinoscript").value;
	var respBack=function(data){
		progress_message(null);
		if(data!=null){
			$("js_debugpane").innerHTML="";
			for(iab=0;iab<data.length;iab++){
				var dv=document.createElement('div');
				dv.innerHTML=data[iab];
				$("js_debugpane").appendChild(dv);
			}
		}		
	}
	if (js != null && js != '') {
		progress_message("Please wait while executing your script.....");
		SchedulerMgmt.executeRhino(js, respBack);
	}else{
		alert("script is empty");
	}
}





 
 
 
 Database.setMarketSecuritesDB(); 
 
 Database.setCommodity("CL"); 
 Database.setFieldname("Close Price");
 Database.createSchemaIfNotExist();
                                 
 
 Record.name='contract';
 Record.value=25.00;	
 Record.cdate=new Date();
 Database.createRecord(Record);
 
 