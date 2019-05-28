


var gMap=null;
var geocoder;


google.maps.Map.prototype.clearMarkers = function() {
    for(var i=0; i < this.markers.length; i++){
        this.markers[i].setMap(null);
    }
    this.markers = new Array();
};


$j(function() {
	 
		$j("#gm_reportdate").datepicker({
			numberOfMonths: 1,
			showButtonPanel: true,
			dateFormat:'dd/mm/yy',
			changeYear: true
			
		});
});

var myLatlng =null;
function gm_initialize() {
  myLatlng = new google.maps.LatLng(-34.397, 150.644);
  geocoder = new google.maps.Geocoder();

  var myOptions = {
    zoom: 3,
    center: myLatlng,
	navigationControl:false,
	streetViewControl:false,
	mapTypeControlOptions:{position:google.maps.ControlPosition.TOP_RIGHT,style: google.maps.MapTypeControlStyle.DROPDOWN_MENU},	
    mapTypeId: google.maps.MapTypeId.ROADMAP
  }
  gMap = new google.maps.Map(document.getElementById("googlemap"), myOptions);
  google.maps.event.addListener(gMap,"bounds_changed",function(){
  	     gm_stateChanged();
	   	   
  });
  
  
  progress_message("please wait...");
  WeatherNewMgmt.getLastGMap(function(data){
  	progress_message(null);
  	if(data!=null && typeof data=='object'){		 					
		myLatlng = new google.maps.LatLng(data.last_latitude, data.last_longitude);
		gMap.setCenter(myLatlng);
		gMap.setZoom(data.last_zoomlevel);				
	}
  });
  
  
  
  
  var homeControlDiv = document.createElement('DIV');
  var homeControl = new HomeControl(homeControlDiv,'Show Data','Show the output on the Map',show_markers );
  homeControlDiv.index = 1;  
  gMap.controls[google.maps.ControlPosition.TOP_RIGHT].push(homeControlDiv);

  var homeControlDiv1 = document.createElement('DIV');
  var homeControl1 = new HomeControl(homeControlDiv1,'Hide Data','Hide the output on the Map',clear_markers );
  homeControlDiv1.index = 2;  
  gMap.controls[google.maps.ControlPosition.TOP_RIGHT].push(homeControlDiv1);

  var searchControlDiv1 = document.createElement('DIV');
  var searchControl = new SearchControl(searchControlDiv1);
  searchControl.index = 3;  
  gMap.controls[google.maps.ControlPosition.TOP_RIGHT].push(searchControlDiv1);



	   
}
 
function clearMarkers()  {
    for(var i=0; i < gMap.markers.length; i++){
        gMap.markers[i].setMap(null);
    }
    
};

  
function gm_stateChanged(){
	//$j("#gMapDebug").append("NE:"+gMap.getBounds().getNorthEast()+" SW:"+gMap.getBounds().getSouthWest());
	//if (myLatlng.lat() != gMap.getCenter().lat() && myLatlng.lng()!= gMap.getCenter().lng()) {
		//progress_message("Saving state...");
		WeatherNewMgmt.saveLastGMap(gMap.getZoom(), gMap.getCenter().lat(), gMap.getCenter().lng(), function(data){
			//progress_message(null);
		});
		 
		if ($j('#gm_showDataOnMap:checked').val() != null) {
 			test_gmVP();
		}
		
	//}
} 



function test_gmMarker(){

    var obj=new Array();
	
	var i=0;
	obj[i]=new Object();obj[i].la=9.984720;obj[i].lo=-10.909423;obj[i].da=35;	
	i=1;obj[i]=new Object();obj[i].la=10.503656;obj[i].lo=-8.953857;obj[i].da=36;
	i=2;obj[i]=new Object();obj[i].la=9.503656;obj[i].lo=-7.953857;obj[i].da=37;
	  	 
    for (a = 0; a < obj.length; a++) {
		var point = new google.maps.LatLng(obj[a].la, obj[a].lo);
		var url = "http://www.cnrfc.noaa.gov/data/icons/temp_icons/" + obj[a].da + ".png";	
		marker=createMarker(point,url,"Data:"+obj[a].da);
	}
	
} 
function gm_searchLocation(){
	if($j("#searchLocation").val()!=''){
		var txt=$j("#searchLocation").val();
		 geocoder.geocode( { 'address': txt}, function(results, status) {
	      if (status == google.maps.GeocoderStatus.OK) {
	        gMap.setCenter(results[0].geometry.location);
			gm_stateChanged();
			 
	      } else {
	        alert("Geocode was not successful for the following reason: " + status);
	      }
	    });
	}
	
}


 
var currentMarkers=new Array();
 
function test_gmVP(){
	//alert(gMap.getBounds().getNorthEast());
	//alert(gMap.getBounds().getSouthWest());
	//$j("#gMapDebug").append("NE:"+gMap.getBounds().getNorthEast()+" SW:"+gMap.getBounds().getSouthWest());
	var lat1=gMap.getBounds().getNorthEast().lat();
	var lng1=gMap.getBounds().getNorthEast().lng();
	var lat2=gMap.getBounds().getSouthWest().lat();
	var lng2=gMap.getBounds().getSouthWest().lng();
	
	var repdate=$j("#gm_reportdate").val();
	var timestep=$j("#timestep").val();
	
	respBack=function(data){
		 
		if(data!=null){
			clear_markers();
			currentMarkers.legnth=0;
			//$j("#gMapDebug").html("found:"+data.length+" stations");
			//$j("#gMapDebug").append(lat1+","+lng1+" ="+lat2+","+lng2+"<br>");
					
			for(iab=0;iab<data.length;iab++){
					var point = new google.maps.LatLng(data[iab].lat, data[iab].lng);
					var url = "http://www.cnrfc.noaa.gov/data/icons/temp_icons/" + data[iab].data + ".png";	
					marker=createMarker(point,url,data[iab]);			
					currentMarkers[iab]=marker;
					//$j("#gMapDebug").append(data[iab].lat+","+data[iab].lng+" ="+data[iab].data+"<br>");
					//if(iab>2000){
						//iab=data.length;
					//}
			}
			//message_status("Found "+data.length+" stations");
			$j("#gm_resultInfo").show();
			$j("#gm_nostations").html(data.length+"");
			$j("#gm_zoomlevel").html(gMap.getZoom()+"");
		}
		progress_message(null);	
	}
	progress_message("Please wait...");	
	WeatherNewMgmt.getGMapData(lat1,lng1,lat2,lng2,repdate,timestep,$j("#gm_tablename").val(),respBack);
	
} 

function test_clear(){
		$j("#gMapDebug").html("");
}

function clear_markers(){
	$j.each(currentMarkers, function(idx, mkr){
		mkr.setMap(null);
	});
}

function show_markers(){
	$j.each(currentMarkers, function(idx, mkr){
		mkr.setMap(gMap);
	});
}
 
 function createMarker(point,url1,data){
       var icon = new google.maps.MarkerImage();
	   var data1=data;
	           
	   icon.icon= url1 ;
       icon.size = new google.maps.Size(32,32);
       icon.anchor = new google.maps.Point(17,32);
	   icon.position=point;
	   icon.opacity=0.5;
	   icon.map=gMap;
	  
       var marker = new google.maps.Marker(icon);	 
	   
	   
       google.maps.event.addListener(marker,"click",function(){
	   	  //infowindow.setCotent({content: "Please Wait"});		  	   		    
	   	  //infowindow.open(gMap,marker);
		  getStData(data1,marker);
	   });
	   
       return marker;
 }
 
 function getStData(data1,marker){
 	var repdate=$j("#gm_reportdate").val();	
	var respBack=function(data){
		progress_message(null)				
		if(data!=null){
			var tbl=document.createElement('table');
			tbl.style.fontSize=".9em";
			//tbl.border="1px";
			tbl.style.border="1px solid grey";
			tbl.cellpadding="0";
			tbl.cellspacing="1";
			
			/*
			for(iad=0;iad<data.length;iad++){
				row=tbl.insertRow(tbl.rows.length);
				c1=row.insertCell(row.cells.length);
				c2=row.insertCell(row.cells.length);
				$j(c1).html(""+data[iad].timestep);
				$j(c2).html(""+data[iad]['c'+data1.latcol]);
			}
			*/

		
			
			var lp=(data.length+(data.length%2))/2;
			
			for (iad = 0; iad < lp; iad++) {
			    col1=data[iad];
				col2=((iad+lp)<data.length)?data[iad+lp]:null;
				
				row=tbl.insertRow(tbl.rows.length);
				
				c1=row.insertCell(row.cells.length);
				c2=row.insertCell(row.cells.length);
				$j(c1).html(""+col1.timestep);
				$j(c2).html(""+col1['c'+data1.latcol]);
		
				c3=row.insertCell(row.cells.length);
				c4=row.insertCell(row.cells.length);
				if(col2!=null){
					$j(c3).html(""+col2.timestep);
					$j(c4).html(""+col2['c'+data1.latcol]);
				}
				c1.style.background="#c0c0c0";
				c3.style.background="#c0c0c0";
			    
			}
			
			var infowindow = new google.maps.InfoWindow({
        		content: tbl
   	   		});
			infowindow.open(gMap,marker);	 
		}
	}
	progress_message("Please wait...")
 	WeatherNewMgmt.getStData(data1.latcol,data1.lngrow,repdate,$j("#gm_tablename").val(),respBack);
	
 }
 
 
 function HomeControl(controlDiv,lbl,title,fnctn ) {

  // Set CSS styles for the DIV containing the control
  // Setting padding to 5 px will offset the control
  // from the edge of the map
  controlDiv.style.padding = '5px';

  // Set CSS for the control border
  var controlUI = document.createElement('DIV');
  controlUI.style.backgroundColor = 'white';
  controlUI.style.borderStyle = 'solid';
  controlUI.style.borderWidth = '2px';
  controlUI.style.cursor = 'pointer';
  controlUI.style.textAlign = 'center';
  controlUI.title = title;
  controlDiv.appendChild(controlUI);

  // Set CSS for the control interior
  var controlText = document.createElement('DIV');
  controlText.style.fontFamily = 'Arial,sans-serif';
  controlText.style.fontSize = '12px';
  controlText.style.paddingLeft = '4px';
  controlText.style.paddingRight = '4px';
  controlText.innerHTML = '<b>'+lbl+'</b>';
  controlUI.appendChild(controlText);

  // Setup the click event listeners: simply set the map to
  // Chicago
  google.maps.event.addDomListener(controlUI, 'click',
    function(){
		progress_message("please wait...");
		fnctn.call(this);
		progress_message(null);	
	}
   );

}



 function SearchControl(controlDiv) {

  // Set CSS styles for the DIV containing the control
  // Setting padding to 5 px will offset the control
  // from the edge of the map
  controlDiv.style.padding = '5px';

  // Set CSS for the control border
  var controlUI = document.createElement('DIV');
  controlUI.style.backgroundColor = 'white';
  controlUI.style.borderStyle = 'solid';
  controlUI.style.borderWidth = '2px';
  controlUI.style.cursor = 'pointer';
  controlUI.style.textAlign = 'center';  
  controlUI.style.width="200px";
  controlDiv.appendChild(controlUI);


  
  
  var inputBox = document.createElement('input');
  inputBox.style.fontFamily = 'Arial,sans-serif';
  inputBox.style.fontSize = '12px';
  inputBox.style.paddingLeft = '4px';
  inputBox.style.paddingRight = '4px';
  inputBox.id="searchLocation";  
  inputBox.value="";
  controlUI.appendChild(inputBox);
   

  // Set CSS for the control interior
  var controlText = document.createElement('DIV');
  controlText.style.fontFamily = 'Arial,sans-serif';
  controlText.style.fontSize = '12px';
  controlText.style.paddingLeft = '4px';
  controlText.style.paddingRight = '4px';
  controlText.style.display="inline";
  controlText.innerHTML = 'Search';
  controlUI.appendChild(controlText);

  // Setup the click event listeners: simply set the map to
  // Chicago
  google.maps.event.addDomListener(controlUI, 'click',
    function(){
		progress_message("please wait...");
		gm_searchLocation();
		progress_message(null);	
	}
   );

}
