/**
 * @author rams kannan
 */
//map.importModule("table");
//dojo.require("dojo.collections.Dictionary");

function LinkTable(){
	
	this.linkedcols=new Array();
}

LinkTable.prototype=new Table();

 

LinkTable.prototype.addColumnLink=function(header,
											celldata,
											cw,
											statictxt,
											valuetopass,
											fname){
	
	var obj=new Object();
	obj.statictxt=statictxt;
	obj.valuetopass=valuetopass;
	obj.fname=fname;
	obj.celldata=celldata;
	this.linkedcols[this.linkedcols.length]=obj;
	this.addColumn(header,celldata,cw);

	
}




Table.prototype.updateData=function(data){
	this.setData(data);
	this.render();
}




LinkTable.prototype.setData=function(data){
	for(ia=0;ia<this.linkedcols.length;ia++){
		var colprop=this.linkedcols[ia];
		//alert(colprop+"111");
		for(ib=0;ib<data.length;ib++){
			if(data[ib]!=null && typeof data[ib]=='object'){
				if(data[ib][colprop.celldata]==null){
					data[ib][colprop.celldata]=colprop.statictxt;
				}else if(data[ib][colprop.celldata]!=null && colprop.statictxt!=null)
				    data[ib][colprop.celldata]=colprop.statictxt;
				
				var cc=data[ib][colprop.celldata];
				//alert(cc+"22");
				if(colprop.valuetopass!=null){
					 if(data[ib][colprop.valuetopass]==null){
					 	var idpass=colprop.valuetopass;
					 }else{
					 	var idpass=data[ib][colprop.valuetopass]
					 }
				}else idpass=null;
				
				//var idpass=(colprop.valuetopass!=null && data[ib][colprop.valuetopass]==null)?
				var link1=document.createElement('a');
				//link1.setAttribute('href','javascript:void(0);');
				link1.setAttribute('href','#');
				//IE doesn't support setAttribute('onClick') so it is a workaround solutions
				link1.setAttribute('funcname',colprop.fname);
				link1.setAttribute('funcparam',idpass);
				link1.setAttribute('funcparamtype',typeof idpass);
				link1.onclick=function(){
						if (this.getAttribute('funcparamtype') == 'number') {
							eval(this.getAttribute('funcname') + "(" + this.getAttribute('funcparam') + ")");
						}else {
							eval(this.getAttribute('funcname') + "('" + this.getAttribute('funcparam') + "')");
						}
					return false;
				}
		 
				if(idpass!=null && (typeof idpass=='number' || typeof idpass=='object')) {
				 		//var fn= colprop.fname+"("+idpass+")"; 
						 
					 	
						//link1.setAttribute("href","javascript:"+colprop.fname+"("+idpass+")");
			 
				 }else if(idpass!=null && typeof idpass=='string'){
				 		//link1.setAttribute("href","javascript:"+colprop.fname+"('"+idpass+"')");
		 		 }else{}
				var sp=document.createElement('span');
				sp.innerHTML=cc;
				link1.appendChild(sp);
				 
				
				data[ib][colprop.celldata]=link1;
			}
		}
	}
	this.data=data;
	//this.setData(data)

}



Table.prototype.updateDataNew=function(data){
	this.setDataNew(data);
	this.renderNew();
}


LinkTable.prototype.setDataNew=function(data){
	for(ia=0;ia<this.linkedcols.length;ia++){
		var colprop=this.linkedcols[ia];
		
		for(ib=0;ib<data.length;ib++){
			if(data[ib]!=null && typeof data[ib]=='object'){
				if(data[ib][colprop.celldata]==null){
					data[ib][colprop.celldata]=colprop.statictxt;
				}else if(data[ib][colprop.celldata]!=null && colprop.statictxt!=null)
				    data[ib][colprop.celldata]=colprop.statictxt;
				
				var cc=data[ib][colprop.celldata];
				//alert(cc+"22");
				if(colprop.valuetopass!=null){
					 if(data[ib][colprop.valuetopass]==null){
					 	var idpass=colprop.valuetopass;
					 }else{
					 	var idpass=data[ib][colprop.valuetopass]
					 }
				}else idpass=null;
				
				
				
				
				/*
				var link1=document.createElement('a');
				//link1.setAttribute('href','javascript:void(0);');
				link1.setAttribute('href','#');
				//IE doesn't support setAttribute('onClick') so it is a workaround solutions
				link1.setAttribute('funcname',colprop.fname);
				link1.setAttribute('funcparam',idpass);
				link1.setAttribute('funcparamtype',typeof idpass);
				link1.onclick=function(){
						if (this.getAttribute('funcparamtype') == 'number') {
							eval(this.getAttribute('funcname') + "(" + this.getAttribute('funcparam') + ")");
						}else {
							eval(this.getAttribute('funcname') + "('" + this.getAttribute('funcparam') + "')");
						}
					return false;
				}		 
		
				var sp=document.createElement('span');
				sp.innerHTML=cc;
				link1.appendChild(sp);				 
				data[ib][colprop.celldata]=link1;
				*/
				
				
				htm="";
				htm+="<a href='#' ";
				htm+=" funcname='"+colprop.fname+"' ";
				htm+=" funcparam='"+idpass+"' ";
				htm+=" funcparamtype='"+typeof idpass+"' ";
				htm+=">";
				htm+="<span>"+cc+"</span>";
				htm+="</a>";
				data[ib][colprop.celldata]=htm;
			}
		}
	}
	this.data=data;
	//this.setData(data)

}
