

$j(function(){
	
	 
	$j('#multiselect_test').multiSelect({	
		    width:"500px",
			source: function( request, response ) {
				var xhr = new XMLHttpRequest();
				xhr.open("GET", "http://localhost:9090/bldb/autocomplete_scheduler.jsp?xhr_rfunction=yes&term="+request.term, true);
				xhr.onreadystatechange = function() {
				  if (xhr.readyState == 4) {			 
					//var resp = eval("var data=" + xhr.responseText + "");
					var data=JSON.parse(xhr.responseText);
					var respdata=[];
					for(iab=0;iab<data.length;iab++){
					    var loc="<span class='broad'>"+data[iab].group_name+"</span> <span class='specific'> "+data[iab].folder_name+"</span>";
						//var dt=data[iab].last_modified!=null
						//                    ?moment(data[iab].last_modified, "YYYY-MM-DD HH:mm:ss").fromNow()
						//					:"";
					    var dt="2 seconds ago";
						var obj={
							value:data[iab].function_name,						
							label:"<div class='autocomp_item'><div class='item_name'>"+data[iab].function_name+"</div>  <div class='item_foot'><div class='item_f1'>"+loc+"</div><div class='item_f2'>"+dt+"</div></div></div>"
							//,uid:data[iab].function_name,		
						}
						respdata[respdata.length]=obj;
					}
	 
					response(respdata);				
				  }
				}
				xhr.send();
			}
	});
	
	 
	
});