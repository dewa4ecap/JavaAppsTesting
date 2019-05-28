/* ===============================
| DROPDOWN.JS
| Author: Andy Croxall (mitya@mitya.co.uk)
|
| USAGE
| See http://www.mitya.co.uk/scripts/jQuery-drop-down-selector-124 for all documentation, demos, parameters info, etc etc.
=============================== */

Dropdown = function(jsdo) {


	/* ----------------------
	| PREP & INITIAL CHECKS
	---------------------- */

	var thiss = this, error = false;
	if (!jsdo || typeof jsdo != 'object' || !jsdo.data || !jsdo.data instanceof Array || !jsdo.mode || (jsdo.mode != 'form' && jsdo.mode != 'nav'))
		error = 'Invalid or insufficient parameters passed to Dropdown() - cannot continue';
	else if (jsdo.mode == 'form' && !jsdo.fieldName)
		error = "In 'form' mode you must specify the name of the field the dropdown will represent in your form";
	if (error) { alert('Dropdown plugin error: '+error); return; }
	var earlyIE = navigator.appVersion.match(/MSIE [6|7]\./);


	/* ----------------------
	| CONFIG
	---------------------- */

	this.numCols = jsdo.data.length > 5 ? 2 : 1;


	/* ----------------------
	| PREP - set up a marker element that will be used to ancnor our dropd-down so that, when we insert it, it goes there, not at end
	| of document as is default behaviour of appendChild. Give it a unique ID so that, if the page features more than one drop-down,
	| each is positioned after the right placeholder
	| Update - not if 'appendTo' or 'insertBefore' param passed
	---------------------- */

	if (!jsdo.appendTo && !jsdo.insertBefore) document.write("<div id='dropdownPlaceholder_"+jQuery('.mityadropdown').length+"'></div>");


	/* ----------------------
	| BUILD: BUTTON (master wrapper). First first option's text.
	---------------------- */

	(this.div = jQuery('<div>'))
		.addClass('dropdown')
		.css({position: 'relative'});
	jQuery('<span>')
		.html(jsdo.data[0].text)
		.addClass('selectedOption selected')
		.appendTo(this.div);
	if (jsdo.cssClass && typeof jsdo.cssClass == 'string') this.div.addClass(jsdo.cssClass);


	/* ----------------------
	| BUILD: ULS WRAPPER
	---------------------- */

	(this.ulsWrapper = jQuery('<div>'))
		.hide()
		.addClass('ulsWrapper')
		.appendTo(this.div);


	/* ----------------------
	| BUILD: ULS
	---------------------- */

	for(var t=0; t<this.numCols; t++) {
		var ul;
		(ul = jQuery('<ul>'))
			.css({borderTop: 'none', listStyle: 'none'})
			.css({width: jsdo.data.length > 5 ? '49%' : '100%', marginRight: jsdo.data.length > 5 ? '1%' : 0})
			.appendTo(this.ulsWrapper);
	}


	/* ----------------------
	| BUILD: LIS (i.e. options). Prepend start text as option, to mimic behaviour of real <select>
	---------------------- */

	//for(var f in jsdo.data) {
	for(f=0;f<jsdo.data.length;f++) {
			

		//li

		var li = jQuery('<li>')
		
		for(att in jsdo.data[f]) li.attr(att,jsdo.data[f][att]);
		 		 //.attr('alert_type',jsdo.data[f].val);
		if (jsdo.data[f].id) li.attr('id', jsdo.data[f].id);

		//img?
	  
	    
		if (jsdo.data[f].img) li.append(jQuery('<img>', {src: jsdo.data[f].img,title: jsdo.data[f].title}));
		

		//inner link
		var a = jQuery('<a>')
			.attr('href', jsdo.data[f].url && jsdo.mode == 'nav' ? jsdo.data[f].url : 'javascript:void(0);')			
			.appendTo(li);
			
		if (jsdo.data[f].text) a.text(jsdo.data[f].text);

		//clear
		li.append(jQuery('<div>'));

		//disabled / link callback
		if (jsdo.data[f].disabled)
			a.addClass('disabled');
		else if (jsdo.data[f].click && typeof jsdo.data[f].click == 'function')
			a.click(jsdo.data[f].click);

		//if form mode, do behaviour whereby clicked 'option' replaces curr-visible option and value of hidden field gets is set to its
		//value (or text, if none)
		if (jsdo.mode == 'form') {
			li.click(function() {
				//var thisOptIndex = jQuery(this).parent().index();
				
				//rams modified it.
				//thiss.div.children('.selectedOption').text(jQuery(this).text());
				thiss.div.children('.selectedOption').html("");
				//console.log("type:"+jQuery(this).attr("alert_type"));
				thiss.div.children('.selectedOption').append(jQuery(this).find("img")[0].cloneNode(true))
				//jsdo.func(jQuery(this).attr("alert_type"));
				jsdo.func(this);
				
				//jQuery('[name='+jsdo.fieldName+']').val(jsdo.data[thisOptIndex].val != null ? jsdo.data[thisOptIndex].val : jQuery(this).text());
			})

		}

		//append LI - if several cols, work out which col to append to
		//this.div.find('div ul:nth-child('+(!(f % this.numCols) ? 1 : 2)+')').append(li);
		this.div.find('div ul').append(li);
		
	}

    if(jsdo.selected!=null){
    	for(f=0;f<jsdo.data.length;f++) {
    		if(jsdo.selected==jsdo.data[f].val){
    			thiss.div.children('.selectedOption').html("<img src='"+jsdo.data[f].img+"'  title='"+jsdo.data[f].title+"'  >");
    		}
    	}
    }

	/* ----------------------
	| INSERT drop-down into DOM, either...
	| 	- at placeholder, then remove placeholder
	| 	- or, if insertInto selector passed, put it in there
	---------------------- */

	if (!jsdo.appendTo && !jsdo.insertBefore)
		with(jQuery('#dropdownPlaceholder_'+jQuery('.mityadropdown').length)) {
			after(this.div);
			remove();
		}
	else if (jsdo.appendTo)
		this.div.appendTo(jQuery(jsdo.appendTo));
	else
		this.div.insertBefore(jQuery(jsdo.insertBefore));

	//if 'form' mode, also add hidden field which store selected value of dropdown
	this.div.after(jQuery('<input>', {type: 'hidden', name: jsdo.fieldName}));


	/* ----------------------
	| EVENTS - set up mouseenter/leave/click events for this drop-down (excluding specific onclick handlers for links, which
	| are bound above)
	| Before unfurling, centre ULs wrapper underneath button. Do this each time, not once, becuase in the case of form mode
	| the width of the butotn itself keeps changing, as its text is updated with whatever option was clicked
	---------------------- */

	this.div.bind('mouseenter mouseleave', function() { jQuery(this).toggleClass('hover'); });
	this.div.click(function(e) {

		thiss.ulsWrapper.css({
			left: -(((thiss.ulsWrapper.width()+parseInt(thiss.ulsWrapper.css('paddingLeft'))+parseInt(thiss.ulsWrapper.css('paddingRight'))) - (thiss.div.width()+parseInt(thiss.div.css('paddingLeft'))+parseInt(thiss.div.css('paddingRight')))) / 2)
		});

	    if(jQuery(e.relatedTarget).parents(jQuery(thiss.div)).length == 0)
	    	jQuery(this).children('div').slideDown('fast');
	});
	this.div.bind('mouseleave click', function(e) {
		if (e.type == 'click' && (!jQuery(this).children('div').is(':visible') || jQuery(this).children('div').is(':animated'))) return;
        jQuery(this).children('div').slideUp('fast');
	});

}