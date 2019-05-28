/**
 * Plugin designed for test prupose. It add a button (that manage an alert) and a select (that allow to insert tags) in the toolbar.
 * This plugin also disable the "f" key in the editarea, and load a CSS and a JS file
 */  
var EditArea_reditorlite= {
	/**
	 * Get called once this file is loaded (editArea still not initialized)
	 *
	 * @return nothing	 
	 */	 	 	
	init: function(){	
		//	alert("test init: "+ this._someInternalFunction(2, 3));
		editArea.load_css(this.baseURL+"css/reditorlite.css");		
		//editArea.load_script(this.baseURL+"test2.js");
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
		//alert("test load");
		
		parent.$j("#textarea",document).bind("dblclick",{doc: document},function(event){
			
			//var obj= EditArea_reditorlite._get_selection("textarea");
			var txt=parent.editAreaLoader.getSelectedText(editArea.id);
			if(typeof parent.rf_open=='function' && txt!=null && txt!=''){
				parent.rf_open(txt);
			}
			
		});
		//
		
		
		
	

	}
	
	/**
	 * Is called each time the user touch a keyboard key.
	 *	 
	 * @param (event) e: the keydown event
	 * @return true - pass to next handler in chain, false - stop chain execution
	 * @type boolean	 
	 */
	,onkeydown: function(e){
		//var str= String.fromCharCode(e.keyCode);
		// desactivate the "f" character
		//if(str.toLowerCase()!=""){
		//	parent.rf_typeDetected(str);
		//	return true;
		//	
		//}
		//return false;  not editable
				
		return true;  //editablable
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
editArea.add_plugin("reditorlite", EditArea_reditorlite);
