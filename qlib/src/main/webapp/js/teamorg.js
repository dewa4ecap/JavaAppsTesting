



function to_showTags4Item(tbdiv, data,sc_id,access,thmetitle,istom) {
	
    var htm="<div style='display:inline-block;height:360px;margin-bottom:20px;padding:10px;'><div class='tag_box div_available'><ul class='task_tag_dialog_ul task_tag_available chromeScroll'>";		 
    //var owner="<div class='tag_box'><div class='tag_title'>Theme / Alert / Notification</div><ul class='task_tag_dialog_ul task_tag_owner chromeScroll'>";
    var owner="<div class='tag_box'><div class='tag_title'>"+thmetitle+"</div><ul class='task_tag_dialog_ul task_tag_owner chromeScroll'>";
    var foll="<div class='tag_box'><div class='tag_title'>Also Notify Changes Others</div><ul class='task_tag_dialog_ul task_tag_follower chromeScroll'>";
    
    var tagnames=new Array();
	for(ic=0;ic<data.tags.length;ic++){
	        var styl=""; var checked=""; tg_class="";
			tg=$j.trim(data.tags[ic].tagname)
							
			if (scd_TagColors[tg] != null && scd_TagColors[tg] != '') {
				styl= "style='background-color:" + scd_TagColors[tg] + "'";
			}		
			
			if(tg.substring(0, 4)!="usr-"){
				var tgplain=(tg.substring(0, 4)=="thm-") ?tg.substring(4):tg;
				tagnames[tagnames.length]=tgplain;
				
				var selected="<li dragged='true'   item_id='"+sc_id+"' tag_id='"+data.tags[ic].id+"' class='task_tag_dnd task_tag_dialog "+tg_class+"' " +styl+">"+tgplain+"<img onclick='to_minusTag(this)' class='removeBtn' src='images/remove.png' border='0'></li>";
				if($j.inArray(data.tags[ic].id,data.tagids)>=0){
					// checked+=" checked ";
					owner+=selected;				 
				}
				if($j.inArray(data.tags[ic].id,data.follow_tagids)>=0){
					foll+=selected;
				}
				
				
				htm+="<li item_id='"+sc_id+"' tag_id='"+data.tags[ic].id+"' class='task_tag_dnd task_tag_dialog "+tg_class+"' " +styl+">"+tgplain+"</li>";
				
							
			}
	}
	
	htm+="</ul></div>";
	owner+="</ul></div>";
	foll+="</ul></div></div>";
	var up="<div style='display:inline-block;width:96%;'>";
	
	up+="<div class='user_notifications tag_box' style='display:inline-block'><div class='tag_title'>Changes Notified To</div>";
	up+="<ul class='task_tag_dialog_ul'>" ;
	for(user in data.notice_escalated){
	    var titl="";    
           
            var escl=data.notice_escalated[user];
            if(user!=null && user!='theme'){
                cls="esc_"+escl;
                if (istom) {
                	up+="<li class=' task_tag_dnd "+cls+"'>"+user+"</li>"
                } else {
                	if (user === user.toUpperCase()) {
                		up+="<li class=' task_tag_dnd "+cls+"'>"+user+"</li>"
                	}
                }
            }
         
    
        up+="";
    }
    up+="</ul>";    
    up+="</div>";

	
	up+="<div class='user_privileges tag_box' style='display:inline-block'><div class='tag_title'>Access Privilege</div><ul class='task_tag_dialog_ul'>"
	for(user in data.user_privileges) {
	  	
		var cls= data.user_privileges[user];
		if(cls!=null) cls=cls.replace(/([a-z])/g, 'p_$1 ');
		else cls="";
		if(user!=null && user!='theme'){
			if (istom) {
	                   up+="<li class='"+cls+" task_tag_dnd'>"+user+"</li>"
	        } else {
	                if (user === user.toUpperCase()) {
	                        up+="<li class='"+cls+" task_tag_dnd'>"+user+"</li>"
	                }
	        }
		}
	}
	up+="</ul></div>";	
	
	up+="</div>";	
 	$j(tbdiv+" .tags_panel").html(htm+owner+foll+up);	
	
	
	//organize them
	$j(tbdiv+" .task_tag_dialog_ul .esc_rx").each(function(idx,el){$j(el).parent().append(el);});
	$j(tbdiv+" .task_tag_dialog_ul .esc_r").each(function(idx,el){$j(el).parent().append(el);});
	
    if(access=='R' || access=='RX' ){
    	
    }else{
		
		$j(tbdiv+" .tags_panel .task_tag_available,.task_tag_owner,.task_tag_follower").sortable({
				
		   connectWith: ".task_tag_dialog_ul",
		   placeholder: "task_tag_placeholder",
		   update: function(event, ui) {
			   // console.log("this:"+$j(this).attr("class")+"
				// ui.item.parent:"+$j(ui.item.parent()).attr("class")) ;
			   if($j(ui.item.parent()).attr("class")==$j(this).attr("class")){
				   // console.log("@@@@@@@@@@@@@@@@@@@@@");
		           // console.log("this class:"+$j(this).attr("class"));
		            console.log("ui.item.parent class:"+$j(ui.item.parent()).attr("class"));
		           // console.log(ui.item.parent()[0]==this);
		           
		            if($j(ui.item).attr("dragged")=="true" && $j(ui.item.parent()).hasClass("task_tag_available")){					 
						$j(ui.item).remove();
					}
		           
			   }else{   
				   if($j(this).hasClass("task_tag_available")){
					   if($j(this).attr("class")==$j(ui.item.parent()).attr("class")) {
					   // if($j(this)==$j(ui.item.parent())) {
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
							   // ui.item.appendTo(this);
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
	
	//var parent=$j(anychild).parents("[id^='scd_tsktabs-']");
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

