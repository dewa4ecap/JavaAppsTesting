/**
 * Plugin designed for test prupose. It add a button (that manage an alert) and a select (that allow to insert tags) in the toolbar.
 * This plugin also disable the "f" key in the editarea, and load a CSS and a JS file
 */  
var EditArea_reditor= {
	/**
	 * Get called once this file is loaded (editArea still not initialized)
	 *
	 * @return nothing	 
	 */	 	 	
	init: function(){	
		//	alert("test init: "+ this._someInternalFunction(2, 3));
		//editArea.load_css(this.baseURL+"css/test.css");
		//editArea.load_script(this.baseURL+"test2.js");
		editArea.load_css(this.baseURL+"css/reditor.css");	
		
	}
	/**
	 * Returns the HTML code for a specific control string or false if this plugin doesn't have that control.
	 * A control can be a button, select list or any other HTML item to present in the EditArea user interface.
	 * Language variables such as {$lang_somekey} will also be replaced with contents from
	 * the language packs.
	 * 
	 * @param {string} ctrl_name: the name of the control to add	  
	 * @return HTML code for a specific control or false.
	 * @type string	or boolean
	 */	
	,get_control_html: function(ctrl_name){
		switch(ctrl_name){
			case "reditor_save":
				// Control id, button img, command
				return parent.editAreaLoader.get_button_html('reditor_save', 'savebtn.png', 'reditor_save_cmd', false, this.baseURL);
			case "reditor_delete":
				// Control id, button img, command
				return parent.editAreaLoader.get_button_html('reditor_delete', 'deletebtn.png', 'reditor_delete_cmd', false, this.baseURL);

			case "reditor_lock":
				// Control id, button img, command
				return parent.editAreaLoader.get_button_html('lock_icon', 'rf_lock.png', 'reditor_unlock_cmd', false, this.baseURL);
					

			case "reditor_unlock":
				// Control id, button img, command
				return parent.editAreaLoader.get_button_html('unlock_icon', 'rf_unlock.png', 'reditor_lock_cmd', false, this.baseURL);
			
			case "reditor_help":
				// Control id, button img, command
				return parent.editAreaLoader.get_button_html('reditor_help', 'help.png', 'reditor_help_cmd', false, this.baseURL);
			
			
			case "file_open":			
			    return "<div id='file-uploader' style='display:inline'></div>";	
				
			case "test_select":
				html= "<select id='test_select' onchange='javascript:editArea.execCommand(\"test_select_change\")' fileSpecific='no'>"
					+"			<option value='-1'>{$test_select}</option>"
					+"			<option value='h1'>h1</option>"
					+"			<option value='h2'>h2</option>"
					+"			<option value='h3'>h3</option>"
					+"			<option value='h4'>h4</option>"
					+"			<option value='h5'>h5</option>"
					+"			<option value='h6'>h6</option>"
					+"		</select>";
				return html;
		}
		return false;
	}
	/**
	 * Get called once EditArea is fully loaded and initialised
	 *	 
	 * @return nothing
	 */	 	 	
	,onload: function(){ 
	     
		//setTimeout(function(){ 
		
		parent.$j("#container textarea",document).css("width","5000px");  //fixed display issue fixed, set to 5000px, chrome bug.
		
		
		
		
		var uploader = new parent.qq.FileUploader({   
		    element: document.getElementById('file-uploader'),		    
		    action: './fileuploader',
			onProgress: function(id, fileName, loaded, total){parent.progress_message("Please wait while loading the file...",parent.document);}, 			
		    onComplete: function(id, fileName, responseJSON){
		            //console.dire(responseJSON);
		            //alert(responseJSON);
					parent.progress_message(null,parent.document);
					parent.editAreaLoader.hide(editArea.id);				
					parent.editAreaLoader.setValue(editArea.id, responseJSON);
					parent.editAreaLoader.show(editArea.id);	
					
					 //parent.fn_refreshEditAreaWithContent(responseJSON);
					
			
		    },
		
		});
		
		
		//parent.$j(".qq-upload-button",document).prepend("< src='images/file_open.png'>");
		//parent.$j(".qq-upload-button img",document).css("z-index","-1000");
		
		//},200);
		
	}
	
	/**
	 * Is called each time the user touch a keyboard key.
	 *	 
	 * @param (event) e: the keydown event
	 * @return true - pass to next handler in chain, false - stop chain execution
	 * @type boolean	 
	 */
	,onkeydown: function(e){
		
		var str= String.fromCharCode(e.keyCode);
		// desactivate the "f" character
		if(str.toLowerCase()!=""){
			parent.rf_typeDetected(str);
			return true;
			
		}
		return false;
	}
	
	/**
	 * Executes a specific command, this function handles plugin commands.
	 *
	 * @param {string} cmd: the name of the command being executed
	 * @param {unknown} param: the parameter of the command	 
	 * @return true - pass to next handler in chain, false - stop chain execution
	 * @type boolean	
	 */
	,execCommand: function(cmd, param){
		// Handle commands
		switch(cmd){
			
			
			case "reditor_save_cmd":
			    editArea.execCommand('save');				
				return false;
				
			case "reditor_delete_cmd":			    
				parent.fn_deleteFunction();				
				return false;
			
			case "reditor_lock_cmd":			    
				//alert("tobe locked");
				parent.fn_lockFunction();
				return false;	
			
			case "reditor_unlock_cmd":			    
				//alert("to be unlock");
				parent.fn_unlockFunction();
				return false;
				
			case "reditor_help_cmd":			    
				//alert("to be unlock");
				if(typeof parent.fn_helpFunction=='function'){
					parent.fn_helpFunction(editArea.id);
					return false;
				}
			
			case "reditor_locked":
			    if (param != null && param) {
					parent.$j(document.getElementById('a_lock_icon')).show();
					parent.$j(document.getElementById('a_unlock_icon')).hide();
				} else {
					parent.$j(document.getElementById('a_lock_icon')).hide();
					parent.$j(document.getElementById('a_unlock_icon')).show();
				}
				return false;			
			
			case "reditor_no_unlock":
			    
				parent.$j(document.getElementById('a_lock_icon')).hide();
				parent.$j(document.getElementById('a_unlock_icon')).hide();
				return false;			
			
					
			//case "reditor_lock1_cmd":			    
				
			//	return false;		
				
			case "test_select_change":
				var val= document.getElementById("test_select").value;
				if(val!=-1)
					parent.editAreaLoader.insertTags(editArea.id, "<"+val+">", "</"+val+">");
				document.getElementById("test_select").options[0].selected=true; 
				return false;
			case "test_cmd":
				alert("user clicked on test_cmd");
				return false;
		}
		// Pass to next handler in chain
		return true;
	}
	
	/**
	 * This is just an internal plugin method, prefix all internal methods with a _ character.
	 * The prefix is needed so they doesn't collide with future EditArea callback functions.
	 *
	 * @param {string} a Some arg1.
	 * @param {string} b Some arg2.
	 * @return Some return.
	 * @type unknown
	 */
	,_someInternalFunction : function(a, b) {
		return a+b;
	}
};

// Adds the plugin class to the list of available EditArea plugins
editArea.add_plugin("reditor", EditArea_reditor);

 
