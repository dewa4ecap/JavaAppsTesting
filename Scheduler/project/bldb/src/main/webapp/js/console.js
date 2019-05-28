


var  websocket=null;
function connectWS(obj){
  
  
          
   if(websocket==null || (websocket!=null && websocket.readyState==3) ){
       //websocket = new WebSocket("ws://"+document.domain+":10123/chat");
       
       
        var sockurl=location.pathname.replace(/(.*?)\/([a-zA-Z0-9._-]+)$/g, "$1/ws/TomcatWSConsole");
        var url="ws://"+location.host+"/"+sockurl;
        websocket = new WebSocket(url);
       
       
       
       //websocket = new WebSocket("ws://10.153.64.10:10008");
        
       websocket.onopen = function(evt) { 
         //do nothing    
         $(".power input").addClass('poweron');
         $(".power input").removeClass('poweroff');
       }; 
       websocket.onclose = function(evt) { 
           //do nothing
         $(".power input").removeClass('poweron');
         $(".power input").addClass('poweroff');
    
       }; 
       
       websocket.onmessage = function(evt) { 
            if(evt.data!=null){
                var mdata=JSON.parse(evt.data);
                if(mdata.c!=null){
                    $(".outputlist").append("<li>"+mdata.c+"</li>");
                    $(".outputlist ").attr({ scrollTop: $(".outputlist ").attr("scrollHeight") });
                }
                if(mdata.rs!=null){
                     $("<li class='commanddone result'>"+mdata.rs+"</li>").insertBefore(".commandinput");
                     $(".commandlist ").attr({ scrollTop: $(".commandlist ").attr("scrollHeight") });
                }
            }
       };        
       websocket.onerror = function(evt) { console.log(evt); };
        
   }else{
     websocket.close();
   }
   //if(websocket!=null && websocket.readyState!=3){
       // websocket.close();   
   //}
}


var commandsEntered=[];
var currentCommand=-1;
$(function(){
        
    $(".commandinput input").autocomplete();    
    $(".command").click(function() {
        $(".commandinput input").focus();
    });
    
    $(".commandinput input").keyup(function(e){
        if(e.keyCode == 13 && $(this).val().trim()!='')  {
              var cmd=$(this).val();
              $(this).autocomplete("close");
                             
              if($.inArray(cmd, commandsEntered)==-1){ commandsEntered.push(cmd); }
              $(".commandinput input").autocomplete({source:commandsEntered});
                             
              if(cmd.trim()=='clear console'){
                  $(".outputlist li").remove();
                  $("<li class='commanddone'>~ "+cmd+"</li>").insertBefore(".commandinput");
                  
              }else if(cmd.trim()=='clear'){
                  $(".commandlist li.commanddone").remove();                 
              }else{                                                 
                $("<li class='commanddone'>~ "+cmd+"</li>").insertBefore(".commandinput");
                sendCommand(cmd);                                 
              }
              $(this).val("");
        }        
       
    });
    
   connectWS(); 
  // stressTest();
})




var connections=[];
var connections_msg=0;
function stressTest(){
    
    for(i=0;i<10000;i++){
        
       connections[i] = new WebSocket("ws://localhost:10008/");
       connections[i].connIndx=i;
       connections[i].onopen = function(evt) {
           connections_msg++;  
         //do nothing
          // $(".outputlist").append("<li> con:"+this.connIndx+" connected</li>"); 
       }; 
       connections[i].onclose = function(evt) { 
           //do nothing
          //$(".outputlist").append("<li> con:"+this.connIndx+" close</li>"); 
    
       }; 
       
       connections[i].onmessage = function(evt) {
            connections_msg++; 
            if(evt.data!=null){
                var mdata=JSON.parse(evt.data);
                if(mdata.c!=null){
                   // $(".outputlist").append("<li> msg to:"+this.connIndx+" "+mdata.c+"</li>");
                   // $(".outputlist ").attr({ scrollTop: $(".outputlist ").attr("scrollHeight") });
                }
            }
       };        
       connections[i].onerror = function(evt) {  };
    }
    
}


function sendCommand(cmd){
    if(websocket!=null && websocket.readyState==1){        
        websocket.send(cmd);
    }else{
        $("<li class='commanddone result'>No connection!</li>").insertBefore(".commandinput");
    }
}

