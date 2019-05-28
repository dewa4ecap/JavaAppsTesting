


var er_pageFocus=true;

function er_pageinit(){
	
	//var t1=$j.template('<tr  pid="${name}" class="ei_para_box"><span class="ei_para_label ${childclass}">${label}:</span><span class="ei_para_value"> <input type="text" value="${value}" name="${name}" st_name="${st_name}" contract="${contract}"></span></div>');
	var t1=$j.template('<tr id="${uid}" ><td></td><td>${queuefriendlytime}</td> <td>${requesthost}</td>  <td class="peer">${peername}</td> <td>${scriptPreview}</td></tr>');	
	
	var respBack=function(rdata){
		var arr=new Array();
		var allq=rdata.queue;
		var exec=rdata.executing;
	 
		for(ioa=0;ioa<allq.length;ioa++){			
			var obj=allq[ioa];
			if(obj.queuefriendlytime!=null || obj.requesthost!=null){
				//obj.peer=(obj.peer==null)?"":obj.peer;
				obj.peername=rdata.peernames[obj.peer]!=null?rdata.peernames[obj.peer]:obj.peer
				obj.peername=obj.peername==null?"":obj.peername;
				
				if($j("tr#"+obj.uid).length>0){
					$j("tr#"+obj.uid).find(".peer").html(obj.peername);
				}else{
					$j("#queueTable tbody").append( t1, obj);				
				}
				arr[arr.length]=obj.uid;				
			}
		}
		
		for(iob=0;iob<exec.length;iob++){		
			$j("tr#"+exec[iob]).addClass("executing");					
		}
		

		
		//finished flag.
		if(rdata.finished!=null){
			for(ioc=0;ioc<rdata.finished.length;ioc++){
				$j("tr#"+rdata.finished[ioc]).removeClass("executing");
				$j("tr#"+rdata.finished[ioc]).addClass("finished");					
			}
		}
		//finished flag.
		if(rdata.timedout!=null){
			for(iod=0;iod<rdata.timedout.length;iod++){
				$j("tr#"+rdata.timedout[iod]).removeClass("executing");
				$j("tr#"+rdata.timedout[iod]).addClass("timedout");					
			}
		}
		
		//items will disappear after 20 seconds if it not present in queue
		$j("#queueTable > tbody > tr").each(function(idx,el){
			var uid=$j(el).attr("id");
			if($j.inArray(uid,arr)==-1 && !$j(el).hasClass("executing")){	
				if($j("input#er_no_remexec").is(':checked')){
					$j(el).addClass("removelater");
				}else{
					$j(el).fadeOut(5000, function () {$j(el).remove();});
				}
				 
				//$j(el).fadeTo("slow", 0.9,function () {$j(this).remove();})
				//$j(el).hide("slow",function () {$j(this).remove();},10000);
			}
		});
		
		var fdin=function(sel,text){
			if($j(sel).text()!=text){
				$j(sel).fadeOut(function() {
				  $j(this).html(text).fadeIn();
				});
			}
		}
	 
		
		if(false){
		    
			$j("#er_queue_count").html(rdata.queued_count==0?"&nbsp;":rdata.queued_count);
			$j("#er_timedout_count").html(rdata.timedout.length==0?"&nbsp;":rdata.timedout.length);
			$j("#er_executing_count").html(rdata.executing_count==0?"&nbsp;":rdata.executing_count);
			$j("#er_finised_count").html(rdata.finished.length==0?"&nbsp;":rdata.finished.length);
			$j("#er_ave_delay").html(rdata.ave_delay==0?"&nbsp;":rdata.ave_delay);
 		
	
		}else{
			fdin("#er_queue_count",rdata.queued_count==0?"&nbsp;":rdata.queued_count);
			fdin("#er_timedout_count",rdata.timedout.length==0?"&nbsp;":rdata.timedout.length);
			fdin("#er_executing_count",rdata.executing_count==0?"&nbsp;":rdata.executing_count);
			fdin("#er_finised_count",rdata.finished.length==0?"&nbsp;":rdata.finished.length);
			fdin("#er_ave_delay",rdata.ave_delay==0?"&nbsp;":rdata.ave_delay);
			fdin("#er_peer_count",rdata.active_peers==0?"&nbsp;":rdata.active_peers);
			
		}
		
		if(er_pageFocus){
			if(allq.length>0){
				setTimeout("er_pageinit()", 100);			
			}else{
				setTimeout("er_pageinit()", 1000);
			}
		}else{
			setTimeout("er_pageinit()", 5000);
		}
		
	}	
	ExecuteRMgmt.getQueue(respBack);
 
}

function er_dontremove(){
	$j("#queueTable > tbody > tr.removelater").each(function(idx,el){		
		if($j.inArray(uid,arr)==-1 && !$j(el).hasClass("executing")){	
			if($j("input#er_no_remexec").is(':checked')){}else{
				$j(el).fadeOut(5000, function () {$j(el).remove();});
			}
		}
	});
}



if (/*@cc_on!@*/false) { // check for Internet Explorer
	document.onfocusin = er_onFocus;
	document.onfocusout = er_onBlur;
} else {
	//window.onfocus = er_onFocus;
	//window.onblur = er_onBlur;
}

function er_onFocus(){
	er_pageFocus=true;
	$j("#queueTable").removeClass("focused_out");
	$j(".er_counterbox").removeClass("focused_out");
}
function er_onBlur(){
	er_pageFocus=false;
	$j("#queueTable").addClass("focused_out");
	$j(".er_counterbox").addClass("focused_out");
}


var websocket=null;

function er_wsocketinit() {
    var sockurl=location.pathname.replace(/(.*?)\/([a-zA-Z0-9._-]+)$/g, "$1/ws/TomcatWSServer");
    var url="ws://"+location.host+"/"+sockurl;
    //console.log(url);

    var fdin=function(sel,text){
            if($j(sel).text()!=text){               
                $j(sel).fadeOut(200,function() {
                  $j(this).html(text).fadeIn();
               });
            }
        }
        

    websocket = new WebSocket(url); 
    websocket.onopen = function () {    };
    websocket.onmessage = function (evt) {
                
        var msg = JSON.parse(evt.data);
        if(msg.added!=null){
            var t1=$j.template('<tr id="${uid}" ><td></td><td>${queuefriendlytime}</td> <td>${requesthost}</td>  <td class="peer">${peername}</td> <td>${scriptPreview}</td></tr>');
            $j("#queueTable tbody").append( t1, msg.added);        
        }
        if(msg.started!=null){
            //var t1=$j.template('<tr id="${uid}" ><td></td><td>${queuefriendlytime}</td> <td>${requesthost}</td>  <td class="peer">${peername}</td> <td>${scriptPreview}</td></tr>');
            //$j("#queueTable tbody").append( t1, received_msg.added);
            var uid= msg.started.uid;
            var peer=msg.started.peer;
            $j("#queueTable tbody tr#"+uid).addClass("executing");
            $j("#queueTable tbody tr#"+uid).find(".peer").html(peer);
        }
        
        if(msg.finished!=null){                                                 
           var uid= msg.finished.uid;
           var el=$j("#queueTable tbody tr#"+uid);
           el.removeClass("executing");
           el.addClass("finished");
           if($j("input#er_no_remexec").is(':checked')){
                $j(el).addClass("removelater");
           }else{
               $j(el).fadeOut(500, function () {$j(el).remove();});
           }
        }

        if(msg.timedout!=null){                         
           var uid= msg.timedout.uid;
           var el=$j("#queueTable tbody tr#"+uid);
           el.removeClass("executing");
           el.addClass("timedout");           
           if($j("input#er_no_remexec").is(':checked')){
                $j(el).addClass("removelater");
           }else{
               $j(el).fadeOut(500, function () {$j(el).remove();});
           }
        } 
        
        if(msg.queue_size!=null){
            fdin("#er_queue_count",msg.queue_size==0?"&nbsp;":msg.queue_size);                   
        }
        if(msg.executing_size!=null){
            fdin("#er_executing_count",msg.executing_size==0?"&nbsp;":msg.executing_size);
        }       
        if(msg.timedout_count!=null){
              fdin("#er_timedout_count",msg.timedout_count==0?"&nbsp;":msg.timedout_count);
        }
        if(msg.ave_delay!=null){
               fdin("#er_ave_delay",msg.ave_delay==0?"&nbsp;":msg.ave_delay);
        }
        if(msg.active_peers!=null){
               fdin("#er_peer_count",msg.active_peers==0?"&nbsp;":msg.active_peers);
        }
         
        if(msg.finished_count!=null){
             fdin("#er_finised_count",msg.finished_count==0?"&nbsp;":msg.finished_count);
        }
        
         
         
    


             
          

        
            
            

    };
    
    websocket.onclose = function () {     
        //console.log("onclose");
        isConnected = false;
        //checkEvery10Sec();
    };
    websocket.onerror = function (e,args) {
        //console.log("onerror");
        //checkEvery10Sec();
        isConnected = false;
        
    };  



    
}
