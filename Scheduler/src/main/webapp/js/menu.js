



function menu_profiletoggle(thisobj){
	$j(thisobj).find('img.btn').toggle();	
	$j("#user_profile").toggle('slow');
	
}

function menu_preference(thisobj) {
	var pref_id=$j(thisobj).attr('pref_item');
	if(pref_id!=null){
		var val=null;
		var message=null;
		var respBack=function(){
			message_status(message);
			menu_profiletoggle($j('#user_profile_a'));
		
		}
		if($j(thisobj).attr('type')=='checkbox'){
			
			val=$j(thisobj).is(":checked")?"true":"false";
			if(pref_id=='beta_ace_editor'){ 
				message='User preference saved, You need to referesh this page';			
				CODE_EDITOR=(val=='true')?CODE_ACE_EDITOR:CODE_EDITOR_DEFAULT;
			}

		}
		progress_message('Please wait while updating...');
		MenuMgmt.updateUserPreference(pref_id,val,respBack);
	}
}