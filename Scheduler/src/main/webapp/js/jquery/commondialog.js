




(function($){
	
	
	var methods = {
	    init : function( options ) { 
	      
			var defaults = {             
				verticalScroll:false,
				horizontalScroll:false,
				hidetitle:false,
				dialogClass:'commdialog-dialog'      }, 
	        overrides = {    }, 
	        settings = $.extend({}, defaults, options, overrides);
	        
 
			if($(this).children().length<=0) {
				$(this).append("<div class='commdialog-dialog-body'></div>");
				if(settings.verticalScroll) $(this).find('commdialog-dialog-body').addClass('commdialog-dialog-vscroll');
				if(settings.horizontalScroll) $(this).find('commdialog-dialog-body').addClass('commdialog-dialog-hscroll');
			}
			
			
            if ($(this).hasClass('ui-dialog-content')) 
                $(this).dialog(options);
            else 
                $(this).dialog(settings);
            
            if(settings.hidetitle!=null && settings.hidetitle){
            	$(this).parent().find(".ui-dialog-titlebar").hide();
            }
	     
	    },
	    
	    bodyHTML:function(content) {
			this.each(function(){
	            if ($(this).find('.commdialog-dialog-body').length>0) {
					 $(this).find('.commdialog-dialog-body').html("");
					 $(this).find('.commdialog-dialog-body').append(content);
	            };
	        });
			
		}
	     
  	};
	
	$.fn.commonDialog = function( method ) {
	    
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

