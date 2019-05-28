// JavaScript Document
 
function preHookLoading() {
	var loadingMessage;
 	//if (message) loadingMessage = message;
  	//else 
	loadingMessage = "<img src='images/loading.gif'>";
  
  
    var disabledZone = $('disabledZone');
    if (!disabledZone) {
      disabledZone = document.createElement('div');
      disabledZone.setAttribute('id', 'disabledZone');
      //disabledZone.style.position = "absolute";
	  disabledZone.style.position = "absolute";
      disabledZone.style.zIndex = "1000";
      disabledZone.style.left = "0px";
      disabledZone.style.top = "0px";
      disabledZone.style.width = "100%";
      disabledZone.style.height = "100%";
      document.body.appendChild(disabledZone);
      var messageZone = document.createElement('div');
      messageZone.setAttribute('id', 'messageZone');
      messageZone.style.position = "absolute";
      messageZone.style.top = "0px";
      messageZone.style.right = "0px";
	  // messageZone.style.left = "0px";
      //messageZone.style.background = "red";
     // messageZone.style.color = "white";
      messageZone.style.fontFamily = "Arial,Helvetica,sans-serif";
      messageZone.style.padding = "4px";
      disabledZone.appendChild(messageZone);
	  var img     = document.createElement('img');
      img.setAttribute('src', 'images/loading.gif');
		
      //var text = document.createTextNode(loadingMessage);
	  //var text = document.createTextNode("Loading");
      //messageZone.appendChild(text);
      messageZone.appendChild(img);
	  disabledZone.appendChild(messageZone);
	   
    }
    else {
	
	  var currTop=0;
	  try{
	 	 currTop=(document.all)?document.body.scrollTop:window.pageYOffset;
		
		// debug(document.body)
	  }	 catch(e){ }
	  
      $('messageZone').innerHTML = loadingMessage;
	  $('disabledZone').style.top=currTop+"px";
      disabledZone.style.visibility = 'visible';
    }
 }
 
 
var postHookLoading=function() {
	if ($('disabledZone') != null) {
		$('disabledZone').style.visibility = 'hidden';
	}
 }
function useLoadingMessage(message) {
  DWREngine.setPreHook(preHookLoading);
  DWREngine.setPostHook(postHookLoading);
}

 
//useLoadingMessage("Loading...");
useLoadingMessage("<img src='images/loading.gif'>");


 


 




