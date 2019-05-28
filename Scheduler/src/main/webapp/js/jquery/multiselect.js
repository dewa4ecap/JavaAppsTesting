




(function($){
	
	
	var methods = {
	    init : function( options ) { 
	      
			var defaults = {width:"auto",delay:500,postcall:null}, 
	        overrides = {
				select:function( event, ui ) {
		        	 
				 
		        	var uid=ui.item.uid!=null?ui.item.uid:ui.item.value;
		        	var value=ui.item.value;
		        	var par_id=$(event.target).attr("id")+"_wrap";
		        	var el=$("<li class='selected_item' uid='"+uid+"'>"+value+"<a href='#'>x</a></li>");	        	
		        	el.insertBefore("#"+par_id+" .input_item");
		        	el.find("a").click(function(){
		        		$(this).parent().hide('slow', function(){ $(this).remove(); });	        		
		        		//$(this).parent().remove();
		        		return false;
		        	});
		        	$(event.target).val("");
		        	$(event.target).focus();
		        	if(event.keyCode==13){
		        		//on selecting mouse click capture as enter pressed.
		        		$(event.target).attr("selectkey","13");
		        	}
		        	return false;	        	
				}
				,focus:function( event, ui ) {
					return false;
				}
			}, 
	        settings = $.extend({}, defaults, options, overrides);
	        
			
			
            if ($(this).hasClass('ui-autocomplete-input')) 
                $(this).autocomplete(options);
            else{ 
    			var par=$(this).parent();
    			var par_id=$(this).attr("id")+"_wrap";
    			var thisobj=this;
    			par.append("<div id='"+par_id+"' class='ui-multiselect'><ul class='selectbox'><li class='input_item'></li></ul><div class='searchautocomplete'></div></div>");
    			$(this).appendTo("#"+par_id+' li.input_item');
    			$("#"+par_id).css("width",settings.width);
                $(this).autocomplete(settings).data( "autocomplete" )._renderItem = function( ul, item ) {                	
                    return $( "<li uid='"+item.uid+"'></li>" )
                    .data( "item.autocomplete", item )
                    .append( "<a>"+ item.label + "</a>" )
                    .appendTo( ul );
                }; 
                $(".ui-autocomplete").appendTo("#"+par_id+" .searchautocomplete");
                $j("#"+par_id).click(function(){$j(thisobj).focus();});
                
                $(this).each(function(idx,el){
            		if ($(el).val()=="") { $(el).val($(el).attr("title"));$(this).addClass("greyed");}		
            	});
            	 
            	$(this).focus(function() {
            		$(this).removeClass("greyed");
            		if ($(this).val()==$(this).attr("title")) { $(this).val(""); }
            	}).blur(function() {		
            		if ($(this).val()=="") { $(this).val($(this).attr("title"));}		
            		if ($(this).val()==$(this).attr("title")) { $(this).addClass("greyed"); }
            	});
            	
            	$j(this).keydown(function(e){            		
            		if(e.keyCode == 8 && $j(this).val()==''){            			
            				$j("#"+par_id+" .selectbox .selected_item:last").hide('slow', function(){ $(this).remove(); });	    
            		}
               	}) ;            	
            	
            	if(settings.postcall!=null){
            		$j(this).keyup(function(e){ 
            			var code=e.keyCode;          			
            			if($j(this).attr("selectkey")=="13")	$j(this).removeAttr("selectkey");
            			else{
	                		if(code == 13 && $j(this).val().trim()=='' && $j(this).multiSelect("getSelected").length>0) {
	                			console.log("code:"+code);
	                			settings.postcall.call(this);
	                		}	                			
            			}
                	}) ;
            	}
            }
	     
	    },	    
	    getSelected:function(content) {
	    	var rtn=[];
			this.each(function(){
				var par_id=$(this).attr("id")+"_wrap";
	            $("#"+par_id).find('li.selected_item').each(function(idx,el){
	            	rtn.push($(this).attr("uid"));
	            });
	        });
			return rtn;
		},
	    removeAllSelected:function(content) {
	    	var rtn=[];
			this.each(function(){
				var par_id=$(this).attr("id")+"_wrap";
	            $("#"+par_id).find('li.selected_item').each(function(idx,el){
	            	$(this).hide('slow', function(){ $(this).remove(); });	        		
	            });
	        });
			return rtn;
		}
	     
  	};
	
	$.fn.multiSelect = function( method ) {
	    
	    // Method calling logic
	    if ( methods[method] ) {
	        return methods[ method ].apply( this, Array.prototype.slice.call( arguments, 1 ));
	    } else if ( typeof method === 'object' || ! method ) {
	        return methods.init.apply( this, arguments );
		}else if(typeof method=='string'){
			
			$(this).dialog.apply( this, arguments );
	    } else {
	        $.error( 'Method ' +  method + ' does not exist on jQuery.tooltip' );
	    }    
  
  	};
  
	
 
})(jQuery);

