function to_showAccess(tbdiv, data,sc_id,access) {
	
	var up="<div style='display:inline-block;width:96%;height:85%'>";
	
/*	up+="<div class='user_notifications tag_box' style='display:inline-block'><div class='tag_title'>Changes Notified To</div>";
	up+="<ul class='task_tag_dialog_ul'>" ;
	for( var  y = 0; y < data.length; y++){

        up+="<li class='task_tag_dnd esc_rwx'>"+data[y]+"</li>";
        up+="";
    }
    up+="</ul>";    
    up+="</div>";*/

	
	up+="<div class='user_privileges tag_box' style='display:inline-block'><div class='tag_title'>Access Privilege</div><ul class='task_tag_dialog_ul'>"
	for( var  y = 0; y < data.length; y++){
		up+="<li class='p_r p_w p_x task_tag_dnd'>"+data[y]+"</li>";
	}
	up+="</ul></div>";	
	
	up+="</div>";	
 	$j(tbdiv+" .tags_panel").html(up);
	
	
	//organize them
	$j(tbdiv+" .task_tag_dialog_ul .esc_rx").each(function(idx,el){$j(el).parent().append(el);});
	$j(tbdiv+" .task_tag_dialog_ul .esc_r").each(function(idx,el){$j(el).parent().append(el);});
	
    if(access=='R' || access=='RX' ){
    	
    }else{
		
		$j(tbdiv+" .tags_panel .task_tag_available,.task_tag_owner,.task_tag_follower").sortable({
				
		   connectWith: ".task_tag_dialog_ul",
		   placeholder: "task_tag_placeholder",
		   update: function(event, ui) {
			   if($j(ui.item.parent()).attr("class")==$j(this).attr("class")){
		            console.log("ui.item.parent class:"+$j(ui.item.parent()).attr("class"));
		           
		            if($j(ui.item).attr("dragged")=="true" && $j(ui.item.parent()).hasClass("task_tag_available")){					 
						$j(ui.item).remove();
					}
		           
			   }else{   
				   if($j(this).hasClass("task_tag_available")){
					   if($j(this).attr("class")==$j(ui.item.parent()).attr("class")) {
						   $j(this).sortable('cancel');
					   }else{
						   var tagid=$j(ui.item).attr("tag_id");
						   console.log("item:"+tagid);
						   if($j(ui.item.parent()).find("li[tag_id='"+tagid+"']").length<2 && $j(ui.item).attr("dragged")!="true"){
							   var selitm=ui.item.clone();
							   $j(selitm).attr("dragged","true");
							   $j(selitm).append("<img onclick='to_minusTag(this)' class='removeBtn' src='images/remove.png' border='0'>");
							   selitm.appendTo( ui.item.parent());
							   $j(this).sortable('cancel');
							   to_updateXS_Notification(ui.item.parent(),ui.item);
						   }else{
							   $j(this).sortable('cancel');
						   }					   
						   
					   }
				   }else{				    
						if($j(this).hasClass("task_tag_owner") || $j(this).hasClass("task_tag_follower")){
							$j(this).sortable('cancel');
				 
						}
				   }
				  
	 		   }
			   
	
	       }			
		
		});
		 
				
		$j(".task_tag_available,.task_tag_owner").disableSelection();
    }
	
	if(sc_id>0){
		// scd_followThisFun(null,null,data,sc_id,tbdiv);
	}
}

function to_updateXS_Notification(anychild,li_item){
	
	var item_id=$j(li_item).attr("item_id");
	var parent=$j(anychild).parents(".tags_panel");
	
	var themes=new Array();
	$j(parent).find(".task_tag_owner li.task_tag_dnd").each(function(id,el){
		themes[themes.length]=$j(el).text();
	});
	
	var tags=new Array();
	$j(parent).find(".task_tag_follower li.task_tag_dnd").each(function(id,el){
		tags[tags.length]=$j(el).text();
	});
	
 

	
	var pr_sel=".user_privileges ul.task_tag_dialog_ul";
	$j(parent).find(pr_sel).find("li").remove();
	
	var fo_sel=".user_notifications ul.task_tag_dialog_ul";
	$j(parent).find(fo_sel).find("li").remove();
	
	
	
	var respBack=function(data){
		progress_message();
		for(user in data.user_privileges) {
		  	
			var cls= data.user_privileges[user];
			if(cls!=null) cls=cls.replace(/([a-z])/g, 'p_$1 ');
			else cls="";
			if(user!=null && user!='theme'){
				$j(parent).find(pr_sel).append("<li class='"+cls+" task_tag_dnd'>"+user+"</li>")				
			}
		}
		
 		for(user in data.notice_escalated){           
                    var escl=data.notice_escalated[user];
                    if(user!=null && user!='theme'){
                        cls1="esc_"+escl+ " task_tag_dnd "+cls;
                        $j(parent).find(fo_sel).append("<li class='"+cls1+" task_tag_dnd'>"+user+"</li>")                        
                    }
        }
    	
    	$j(parent).find(fo_sel).find(".esc_rx").each(function(idx,el){$j(el).parent().append(el);});
        $j(parent).find(fo_sel).find(".esc_r").each(function(idx,el){$j(el).parent().append(el);});
    		
    	$j(parent).find(pr_sel).find("li").hide().fadeIn(2500);		
		$j(parent).find(fo_sel).find("li").hide().fadeIn(2500);
	}
	 
	progress_message("Please wait updating..");	
	updateItemPrivilgeNotification(themes, tags, item_id, respBack);
}




function to_minusTag(obj) {	 
 
	$j($j(obj).parent()).animate({height: 0,width:0}, 300,"linear",function(){
		 var thisparent=$j(this).parent();
         $j(this).remove();
         $j(thisparent).attr("user_deleted","true");
         to_updateXS_Notification(thisparent);
    });
}

