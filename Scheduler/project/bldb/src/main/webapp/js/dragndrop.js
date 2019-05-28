
var Dom = YAHOO.util.Dom;
var Event = YAHOO.util.Event;
var DDM = YAHOO.util.DragDropMgr;

//////////////////////////////////////////////////////////////////////////////
// example app
//////////////////////////////////////////////////////////////////////////////


function DragAndDrop(scr,tgt,arr
					){

	this.source=scr
	this.target=tgt;
	this.data=arr;
	this.dblclickfunc=null;
	this.clicklabel=null;
	this.tooltip=false;
	this.onchangecallbk=null;
	this.oninit=null;
}


DragAndDrop.prototype.setToolTip=function(mapdata){
	 
	this.tooltipdata=mapdata;
	this.tooltip=true;
}

DragAndDrop.prototype.setDblClick=function(funame){
	this.dblclickfunc=funame;	 
	
}


DragAndDrop.prototype.setOnChange=function(callbck){
	this.onchangecallbk=callbck;
}

DragAndDrop.prototype.setOnChange=function(callbck){
	this.onchangecallbk=callbck;
}

DragAndDrop.prototype.setOnInit=function(callbck){
	this.oninit=callbck;
}
DragAndDrop.prototype.init=function(){

		//activate both as target so that drag back to original in case of mistake
		new YAHOO.util.DDTarget(this.source);
	    new YAHOO.util.DDTarget(this.target);

		for(j=0;j<this.data.length;j++){
			this.addItem2List(this.data[j]);
		}
		if(this.oninit!=null){
			this.oninit.call(this);
		}		
 }
 
DragAndDrop.prototype.addItem2List=function (record){

	   var thisid="dnd_"+this.source+"_"+record;			
		var litm=document.createElement('li');
		litm.setAttribute('id',thisid);			
		
		litm.setAttribute('item',record);
		
		litm.setAttribute('class','dndlistitem');
		litm.setAttribute('className','dndlistitem'); //fo IE
		
		if(this.dblclickfunc!=null && typeof this.dblclickfunc=='function'){
			YAHOO.util.Event.addListener(litm, "dblclick", this.dblclickfunc); 
		}
		
 
		litm.appendChild(document.createTextNode(record));
		
		if(this.tooltip && this.tooltipdata!=null && this.tooltipdata[record]!=null && this.tooltipdata[record]!=''){
			 //tool tip
			
			var recobj=this.tooltipdata[record];
			 
			
			var sml=document.createElement('small'); 
			var spn1=document.createElement('a');
			spn1.setAttribute("href","#")			
			spn1.onclick=function(){return false;} 
			var spid="m_secu_"+record;			
			spn1.setAttribute("id",spid)
			spn1.setAttribute("parent_id",thisid);
								 
			var tdiv=document.createElement('div');
			tdiv.style.zIndex="1000";
			tdiv.style.width="400px";
			
			if(recobj!=null &&  recobj!=''){
				/*
				myTooltip = new YAHOO.widget.Tooltip(tdiv, { 
				context:spid, 
				text:tip,
				preventoverlap:false,
				showDelay:500 } );
				*/
				
				spn1.setAttribute('tip',recobj);
				
				//var spid1=spid;
				
				YAHOO.util.Event.addListener(spn1, "mouseover", function(){
					  $("sec_tips").show();
					 if(sec_tipsdg==null){							 	 
					 	sec_tipsdg=new YAHOO.widget.Overlay("sec_tips", { context:[this.id,"tl","bl", ["beforeShow", "windowResize"]], visible:true} );
						//sec_tipsdg=new YAHOO.widget.Overlay("sec_tips");
						sec_tipsdg.render();
													
					 }
					 //sec_tipsdg.configContext("context",[this.id,"tl","bl", ["beforeShow", "windowResize"]]);;
					 sec_tipsdg.cfg.context=[this.id,"tl","bl", ["beforeShow", "windowResize"]];
					 var tp1=this.getAttribute('tip');
					 
					 var tp2="<div style='width:auto;padding:5px'>"+tp1+"</div>";
					
					var pid=this.getAttribute("parent_id");							
					$(pid).style.color="red";
					
					sec_tipsdg.setBody(tp2);
					sec_tipsdg.show();
				 }); 
				 
				YAHOO.util.Event.addListener(spn1, "mouseout", function(){
					if(sec_tipsdg!=null){
						//progress_message(sec_tipsdg+"");
						sec_tipsdg.hide();
						//sec_tipsdg.hide();
						//sec_tipsdg.cancel();								
						//sec_tipsdg.destroy();  
						sec_tipsdg=null;
						var pid=this.getAttribute("parent_id");
						$(pid).style.color="";								
					}
				});
			}
			spn1.appendChild(document.createTextNode("?"));
			spn1.style.marginLeft="5px";
			sml.appendChild(spn1);
			litm.appendChild(sml);
								
		}else{
			
		}
		
		$(this.source).appendChild(litm);

		new YAHOO.example.DDList(thisid,this.onchangecallbk);
		return litm;
} 
 
 
var sec_tipsdg=null;
DragAndDrop.prototype.getSelected=function() {
	
        var parseList = function(ul) {
            var items = ul.getElementsByTagName("li");
            var out = new Array(); //title + ": ";
            for (i=0;i<items.length;i=i+1) {
                //out += items[i].id + " ";
				//out[out.length]=items[i].innerHTML;
				out[out.length]=items[i].getAttribute('item');
            }
            return out;
        };
		return parseList(Dom.get(this.target));
		
		//alert(parseList(Dom.get(this.target),"List 2"));
        //var ul1=Dom.get("ul1"), ul2=Dom.get("ul2");
        //alert(parseList(ul1, "List 1") + "\n" + parseList(ul2, "List 2"));

 }


DragAndDrop.prototype.getSelectedAsString=function() {
	
        var parseList = function(ul) {
            var items = ul.getElementsByTagName("li");
            var out = "";
            for (i=0;i<items.length;i=i+1) {
                //out +=(out=="")?items[i].innerHTML:","+items[i].innerHTML;
				out +=(out=="")?items[i].getAttribute('item'):","+items[i].getAttribute('item');
				//out[out.length]=items[i].innerHTML;
            }
            return out;
        };
		return parseList(Dom.get(this.target));
		
		//alert(parseList(Dom.get(this.target),"List 2"));
        //var ul1=Dom.get("ul1"), ul2=Dom.get("ul2");
        //alert(parseList(ul1, "List 1") + "\n" + parseList(ul2, "List 2"));

 }


DragAndDrop.prototype.selectAll=function() {
		
        var elm=$(this.source).select('li');
		for(j=0;j<elm.length;j++){
				$(this.target).appendChild(elm[j]);
		}
		if (this.onchangecallbk != null) {
			this.onchangecallbk.call(this, this.source);
		};
 }

DragAndDrop.prototype.selectValue=function(array) {

        var elm=$(this.source).select('li');
		for(j=0;j<elm.length;j++){
				var val=elm[j].getAttribute('item');
				if (val != null && array.indexOf(val)>=0) {
					$(this.target).appendChild(elm[j]);
				}
		}
 }



DragAndDrop.prototype.selectNonExistingValue2=function(array) {

		var selectedids=new Array();
		
        var elm=$(this.source).select('li');
		for(j=0;j<elm.length;j++){
				var val=elm[j].getAttribute('item');
				if (val != null && array.indexOf(val)>=0) {
					$(this.target).appendChild(elm[j]);
					selectedids[selectedids.length]=val;		
				}
		}
		
		
	 
		for (j = 0; j < array.length; j++) {
			if (selectedids.indexOf(array[j]) >= 0) {}else{
				var itm=this.addItem2List(array[j]);				
				$(this.target).appendChild(itm);			
			}
		}				
			
				


}




 



DragAndDrop.prototype.resetAvailable=function(array) {

        var elm=$(this.source).select('li');
		for(j=0;j<elm.length;j++){
				$(elm[j]).remove();
		}
		this.data=array;
		this.init();
		//for(j=0;j<elm.length;j++){
		//		var val=elm[j].getAttribute('item');
		//		if (val != null && array.indexOf(val)>=0) {
		//			$(this.source).appendChild(elm[j]);
		//		}
		//}
 }



DragAndDrop.prototype.deSelectAll=function() {
	
        var elm=$(this.target).select('li');
		for(j=0;j<elm.length;j++){
				$(this.source).appendChild(elm[j]);
		}
        if (this.onchangecallbk != null) {
			this.onchangecallbk.call(this, this.source);
		}; 

 }

 

//////////////////////////////////////////////////////////////////////////////
// custom drag and drop implementation
//////////////////////////////////////////////////////////////////////////////

YAHOO.example.DDList = function(id,onchangecallback, sGroup, config) {

    YAHOO.example.DDList.superclass.constructor.call(this, id, sGroup, config);

    this.logger = this.logger || YAHOO;
    var el = this.getDragEl();
    Dom.setStyle(el, "opacity", 0.67); // The proxy is slightly transparent

    this.goingUp = false;
    this.lastY = 0;
	this.onchange=null;
	if(onchangecallback!=null){
		this.onchange=onchangecallback;	
	}
	
};

YAHOO.extend(YAHOO.example.DDList, YAHOO.util.DDProxy, {

    startDrag: function(x, y) {
        this.logger.log(this.id + " startDrag");

        // make the proxy look like the source element
        var dragEl = this.getDragEl();
        var clickEl = this.getEl();
        Dom.setStyle(clickEl, "visibility", "hidden");

        dragEl.innerHTML = clickEl.innerHTML;

        Dom.setStyle(dragEl, "color", Dom.getStyle(clickEl, "color"));
        Dom.setStyle(dragEl, "backgroundColor", Dom.getStyle(clickEl, "backgroundColor"));
        Dom.setStyle(dragEl, "border", "2px solid gray");
    },

    endDrag: function(e) {

        var srcEl = this.getEl();
        var proxy = this.getDragEl();

        // Show the proxy element and animate it to the src element's location
        Dom.setStyle(proxy, "visibility", "");
        var a = new YAHOO.util.Motion( 
            proxy, { 
                points: { 
                    to: Dom.getXY(srcEl)
                }
            }, 
            0.2, 
            YAHOO.util.Easing.easeOut 
        )
        var proxyid = proxy.id;
        var thisid = this.id;

        // Hide the proxy and show the source element when finished with the animation
        a.onComplete.subscribe(function() {
                Dom.setStyle(proxyid, "visibility", "hidden");
                Dom.setStyle(thisid, "visibility", "");
            });
        a.animate();
		if (this.onchange != null) {
			this.onchange.call(this, this.id);
		}
    },

    onDragDrop: function(e, id) {

        // If there is one drop interaction, the li was dropped either on the list,
        // or it was dropped on the current location of the source element.
        if (DDM.interactionInfo.drop.length === 1) {

            // The position of the cursor at the time of the drop (YAHOO.util.Point)
            var pt = DDM.interactionInfo.point; 

            // The region occupied by the source element at the time of the drop
            var region = DDM.interactionInfo.sourceRegion; 

            // Check to see if we are over the source element's location.  We will
            // append to the bottom of the list once we are sure it was a drop in
            // the negative space (the area of the list without any list items)
            if (!region.intersect(pt)) {
                var destEl = Dom.get(id);
                var destDD = DDM.getDDById(id);
                destEl.appendChild(this.getEl());
                destDD.isEmpty = false;
                DDM.refreshCache();
            }

        }
    },

    onDrag: function(e) {

        // Keep track of the direction of the drag for use during onDragOver
        var y = Event.getPageY(e);

        if (y < this.lastY) {
            this.goingUp = true;
        } else if (y > this.lastY) {
            this.goingUp = false;
        }

        this.lastY = y;
    },

    onDragOver: function(e, id) {
    
        var srcEl = this.getEl();
        var destEl = Dom.get(id);

        // We are only concerned with list items, we ignore the dragover
        // notifications for the list.
        if (destEl.nodeName.toLowerCase() == "li") {
            var orig_p = srcEl.parentNode;
            var p = destEl.parentNode;

            if (this.goingUp) {
                p.insertBefore(srcEl, destEl); // insert above
            } else {
                p.insertBefore(srcEl, destEl.nextSibling); // insert below
            }

            DDM.refreshCache();
        }
    }
});

//Event.onDOMReady(YAHOO.example.DDApp.init, YAHOO.example.DDApp, true);
//YAHOO.example.DDApp.init("ul1","ul2");
