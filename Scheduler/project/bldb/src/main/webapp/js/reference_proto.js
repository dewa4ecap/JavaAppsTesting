
var tree=null;
function treeInit() {
   tree = new YAHOO.widget.TreeView("treeDiv");
   var root = tree.getRoot();
   var tmpNode = new YAHOO.widget.TextNode({label: "Reference", expanded: false}, root);    
   var subnode1 = new YAHOO.widget.TextNode({label: "CL", expanded: false,commodity:true}, tmpNode);
   var ssubnode1 = new YAHOO.widget.TextNode({label: "CLZ9", expanded: false, contract:true}, subnode1);
   var ssubnode2 = new YAHOO.widget.TextNode({label: "CLF9", expanded: false, contract:true}, subnode1);
   
   var subnode2 = new YAHOO.widget.TextNode({label: "BO", expanded: false,commodity:true}, tmpNode);
   var ssubnode4 = new YAHOO.widget.TextNode({label: "BOZ9", expanded: false, contract:true}, subnode2);
   var ssubnode5 = new YAHOO.widget.TextNode({label: "BOF9", expanded: false, contract:true}, subnode2);


   var tmpNode1 = new YAHOO.widget.TextNode({label: "Trading Matrix", expanded: false}, root);
   
   
   
   var subnode3 = new YAHOO.widget.TextNode({label: "CL", expanded: false,matrix:true}, tmpNode1);
   var month1 = new YAHOO.widget.TextNode({label: "May 09", expanded: false, month:true}, subnode3);
   var month2 = new YAHOO.widget.TextNode({label: "Apr 09", expanded: false, month:true}, subnode3);   
   var month3 = new YAHOO.widget.TextNode({label: "Mar 09", expanded: false, month:true}, subnode3);
   var month4 = new YAHOO.widget.TextNode({label: "Feb 09", expanded: false, month:true}, subnode3);
   var month5 = new YAHOO.widget.TextNode({label: "Jan 09", expanded: false, month:true}, subnode3);
   
   
   var subnode4 = new YAHOO.widget.TextNode({label: "BO", expanded: false,matrix:true}, tmpNode1);
   var month1 = new YAHOO.widget.TextNode({label: "May 09", expanded: false, month:true}, subnode4);
   var month2 = new YAHOO.widget.TextNode({label: "Apr 09", expanded: false, month:true}, subnode4);   
   var month3 = new YAHOO.widget.TextNode({label: "Mar 09", expanded: false, month:true}, subnode4);
   var month4 = new YAHOO.widget.TextNode({label: "Feb 09", expanded: false, month:true}, subnode4);
   var month5 = new YAHOO.widget.TextNode({label: "Jan 09", expanded: false, month:true}, subnode4);


	tree.subscribe("labelClick", function(node) {  
				        
				$("tradingmx_stat").hide();
				$("reference_stat").hide();
				$("commodity_details").hide();
				//$("db_properties").hide();			
		  		if ( node.data.contract!=null && node.data.contract) {
					 var el = layout.getUnitByPosition('center').get('wrap'); 
					 $(el).show();
					 $("reference_stat").show();
					// $("db_properties").show();
					 layout.getUnitByPosition('center').set('header','Contract');		
				
				}else if ( node.data.commodity!=null && node.data.commodity) {
					 var el = layout.getUnitByPosition('center').get('wrap'); 
					 $(el).show();
					 $("commodity_details").show();
					 layout.getUnitByPosition('center').set('header','Commodity Details: CL');	
					 
				}else if ( node.data.month!=null && node.data.month) {
					 var el = layout.getUnitByPosition('center').get('wrap');
					 layout.getUnitByPosition('center').set('header','Trading Matrix');					 
					 $(el).show();
					 
					 $("tradingmx_stat").show(); 				 
				}else{
					 var el = layout.getUnitByPosition('center').get('wrap'); 
					 $(el).hide();					 
				}
	});			
					


   tree.render();
}



 var layout2;
  var layout;
function layoutInit(){
	
	
    layout = new YAHOO.widget.Layout({
    units: [        
        { position: 'left', width: 300,body:'one',gutter: '5px',collapse: true, header:'List of Contracts' , resize:true },     
        { position: 'center', body:'two', gutter: '5px', resize:true }
    ]
	});
	
	/*	
    layout.on('render', function() {
        var el = layout.getUnitByPosition('center').get('wrap');
        var layout2 = new YAHOO.widget.Layout(el, {
            parent: layout,
            units: [
                { position: 'left', header: 'Top 2',body:'prop1', height: 200, gutter: '2px' },
                { position: 'right', body:'prop2', gutter: '2px', height:200, scroll: true }
            ]
        });
        layout2.render();
    });
    */
	  layout.on('render', function() {   
             var el = layout.getUnitByPosition('center').get('wrap');   
             layout2 = new YAHOO.widget.Layout(el, {   
                 parent: layout,   
                 minWidth: 400,   
                 minHeight: 200,   
                 units: [   
                     //{ position: 'top', gutter: '2px', maxHeight: 80 },   
                     //{ position: 'right', header: 'Right 2', width: 90, resize: true, proxy: false, body: 'Right Content Data', collapse: true, gutter: '2px 2px 2px 5px', maxWidth: 200 },   
                     { position: 'left', header: 'Statistics', width: 650, resize: true, proxy: false, body: 'bbprop', gutter: '2px 5px 2px 2px'},   
                     { position: 'center',header: 'More Info', body: 'center2',body:'db_properties', gutter: '2px', scroll: true, resize:true }   
                 ]   
             });   
             layout2.render(); 
			 $(el).hide(); 
         });   

    layout.render();
	 

}
