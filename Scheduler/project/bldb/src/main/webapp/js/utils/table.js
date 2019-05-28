/**
 * @author Rams Kannan
 * this file is to create and manipulate tables
 * for various purposes.
 */


//dojo.require("dojo.collections.ArrayList");
//dojo.require("dojo.collections.Dictionary");




//Object.extend(Array.prototype, {
   
Array.prototype.count=function(){
    return this.length
  }
Array.prototype.item= function(idx) {
    return this[idx];
  }

Array.prototype.add= function(elem) {
    this[this.length]=elem;
  }
 
//});








_checkedRowColor="#CBD0FE";
_checkboxPrefix="rowid_";

var _tableInstanceStack=new Hash();

/**
 * Constructor
 * @param {Object}  div element that to be populated as table
 */
function Table(element /*element*/){
    if(arguments.length>0)
        this.init(element);
}


Table.prototype.init=function(element){
    
    this.table=element; //document.getElementById(elementid);
    //this.cheader=new dojo.collections.ArrayList([]);
    //this.cdata=new dojo.collections.ArrayList([]);
    //this.cwidth=new dojo.collections.ArrayList([]);
    
    this.cheader=new Array();
    this.cdata=new Array();
    this.cwidth=new Array();
    
    this.stringdentifier=element.id+Math.random();
    this.classname="";
     
    this.selectedRecordIds=new Array(); //new dojo.collections.ArrayList([]);
 
    _tableInstanceStack.set(this.stringdentifier,this);
     
    this.selectedRecordCount=null;
    
    this.selbox=true;
    this.tablewidth=null;
    this.rowprefix="rowid_";
    this.htmlform=null;
    this.rowcheckbox=new Array();
    this.rowcheckbox.length=0; 
    this.selectallcheck=null;
    
    this.selectOneRowOnly=false;

    
}

/**
 * 
 * @param {Object} flag
 */
Table.prototype.selectionBox=function(flag) {this.selbox=flag;}

Table.prototype.setData=function(data){
    this.data=data;
}

Table.prototype.setTableWidth=function(w){
    this.tablewidth=w;
}

Table.prototype.getRowIdPrefix=function(){
    return this.rowprefix;
}

Table.prototype.getForm=function(){
    return this.htmlform;
}

Table.prototype.getRowCheckBoxes=function(){
    return this.rowcheckbox;
}

Table.prototype.getSelectAllBox=function(){
    return this.selectallcheck;
}
Table.prototype.setSelectOneRowOnly=function(flag){
    this.selectOneRowOnly=flag;
}


/**
 * this method is not working 
 * properly, kept it because used somewhere...
 * use getCheckedIds() function below instead
 */
Table.prototype.getSelectedIds=function(){
    //var arraylist=_tableInstanceStack.get(this.stringdentifier);
    //alert(arrayList.toArray());
    //return arraylist.toArray();
    return this.selectedRecordIds.toArray();
    
}

Table.prototype.getCheckedIds=function(){
    var cp=this.rowcheckbox;
    var ids=new Array();
    for (i = 0; i < cp.length; i++) {
        if(cp[i].checked){
            ids[ids.length]=cp[i].value;
        }
    }
    return ids;
}

    
Table.prototype.setRowId=function(idprop){this.rowid=idprop;}
Table.prototype.setRowClassProp=function(cprop){this.classprop=cprop;}


Table.prototype.setTableClass=function(cname){
    this.classname=cname;
    }

Table.prototype.setHeaderClass=function(hname){this.hcname=hname;}

Table.prototype.addColumn=function(header,celldata,cw){
    this.cheader.add(header);
    this.cdata.add(celldata);   
    if(cw!=null) this.cwidth.add(cw) 
    else this.cwidth.add("");
}

/**
 * arrids=array that contains selected ids,
 * this function should be invoked only after render() called 
 */
Table.prototype.setSelectedRows=function(arrids /*Array that contains selected ids */){
    MapUtil.defaultCheck(this.getForm(),this.getRowIdPrefix(),new dojo.collections.ArrayList(arrids));
}


Table.prototype.render=function(){
    
    if(this.rowid==null) throw "Error: rowId has not been set!";
     
    if(this.data != null && typeof this.data == 'object'){
         
        var ta=document.createElement('table');
        
     
        
        if(this.classname!="") {
            ta.setAttribute("class",this.classname);
            ta.setAttribute("className",this.classname);
        }
        if(this.tablewidth!=null) ta.setAttribute('width',this.tablewidth);
        
        //setting cell spacing for the table
        
        ta.setAttribute("cellspacing","0");
        
        //inserting header row;
                
        //var hrow=ta.insertRow(ta.rows.length);
        var hrow=ta.createTHead().insertRow(-1); 
 
        if(this.hcname!="") {
            hrow.setAttribute("class",this.hcname);
            hrow.setAttribute("className",this.hcname);
        }
        
        var startloop=-1;
        if(!this.selbox)startloop=0;
         
        for(var ia=startloop;ia<this.cheader.count();ia++){
             
            if(ia==-1){
                //var hcell=hrow.insertCell(ia+1);
                    var hcell=document.createElement('th');
                    hcell.setAttribute("width","20");
                    var chkbox=document.createElement('input');
                    chkbox.setAttribute('type','checkbox');
                    //chkbox.setAttribute('onClick','_table_CheckAllRows(this.form,this.checked,"'+this.stringdentifier+'");');
                    var p3=this.stringdentifier;
                    chkbox.onclick=function(){
                        _table_CheckAllRows(this.form,this.checked,p3);
                    };
                    this.selectallcheck=chkbox;
                    if(!this.selectOneRowOnly) {
                        hcell.appendChild(chkbox);
                    }
                    hrow.appendChild(hcell);
                 
            }else{
                var hcell=document.createElement('th');
                hcell.setAttribute('width',this.cwidth.item(ia));
                hcell.appendChild(document.createTextNode(this.cheader.item(ia)));
                hrow.appendChild(hcell);
            }
        }
 
        var tbody=document.createElement('tbody');
        ta.appendChild(tbody);  
        for(var ia=0;ia<this.data.length;ia++){
            var datarow=this.data[ia];
            
            //var brow=ta.insertRow(ta.rows.length);

            //var brow=ta.insertRow(-1);
            var brow=document.createElement("tr");
            tbody.appendChild(brow);
            var uid1=datarow[this.rowid];
            
            var cls=datarow[this.classprop]!=null?datarow[this.classprop]:"";
            
            brow.setAttribute("rowid",uid1);
            if (ia%2==0){
                brow.setAttribute("class","odd "+cls);
            }else{
                brow.setAttribute("class","even "+cls);
            }

            for(ib=startloop;ib<this.cdata.count();ib++){
                
                
                
                if(ib==-1){
                    var bcell=brow.insertCell(brow.cells.length);
                    bcell.setAttribute("width","20");
                    var chkbox=document.createElement('input');
                    chkbox.setAttribute('type','checkbox');
                    var p3=this.stringdentifier;
                    chkbox.onclick=function(){
                        _table_updateClickedRow(this,p3);
                    }
                    //chkbox.setAttribute('onClick','_table_updateClickedRow(this,"'+this.stringdentifier+'");');
                    //chkbox.onclick='_table_updateClickedRow(this,"'+this.stringdentifier+'");';
                    uid=datarow[this.rowid];
                    chkbox.setAttribute("id",this.rowprefix+uid);
                    chkbox.setAttribute("value",uid);
                    this.rowcheckbox[this.rowcheckbox.length]=chkbox;
                    bcell.appendChild(chkbox);
                }else{
                     
                    var bcell=brow.insertCell(brow.cells.length);
                    bcell.setAttribute('width',this.cwidth.item(ib));
                    /*AA : START
                     *Added this code to support '.' according to OOP.
                     *now value can be shown like Data.User.firstname :) 
                    */
                    var objectPropName=this.cdata.item(ib);
                    var objectValue=datarow[this.cdata.item(ib)];
                    if (objectPropName!=null && objectPropName.indexOf('.')>0){
                        objectPropName=objectPropName.split('.');
                        objectValue=datarow[objectPropName[0]];
                        for (var p = 1; p < objectPropName.length; p++) {
                            objectValue=objectValue[objectPropName[p]];
                        }
                        
                    }
                    //AA : END
                    
                    if(objectValue!=null){
                        //bcell.appendChild(document.createTextNode(datarow[this.cdata.item(ib)]));
                        //linktable.js links will be an object data
                        if(typeof objectValue=='object' && objectValue.tagName!=null)
                            bcell.appendChild(objectValue);
                        else
                            bcell.innerHTML=objectValue;
                    }
                }
            }
            
        }// end of inserting body cells
             
        //added by rams to hide column headers if there is no data 
        //added on 9-may-2008
        if (ta.rows!=null && ta.rows.length < 2) {
            Element.hide(ta);
        }
        //end of new insertion.
             
        var form1=document.createElement('form');
        this.htmlform=form1;
        form1.appendChild(ta);
        
        
            this.selectedRecordCount=document.createElement('p');
            this.selectedRecordCount.setAttribute("align","right");
        if(this.selbox){
            form1.appendChild(this.selectedRecordCount);
        }
     
        this.initialCheck(form1);
         
        //remove child node from the table
         
        if(this.table.hasChildNodes())
            this.table.removeChild(this.table.firstChild);
        
        //remove everything before it updates
        //this is to make sure when you can this method subsequently it clears up previous content;
        this.table.innerHTML="";
         
        this.table.appendChild(form1);
         
    }
}

Table.prototype.renderNew=function(){
    
    if(this.rowid==null) throw "Error: rowId has not been set!";
    
    var htm="";
     
    if(this.data != null && typeof this.data == 'object'){
         
        //var ta=document.createElement('table');
        htm+="<table class='"+(this.classname!=null?this.classname:"")+"' ";        
        htm+=" cellspacing='0' ";
        if(this.tablewidth!=null) htm+=" width='"+this.tablewidth+"' ";
        htm+="' >";
     
        
        htm+="<thead><tr class='"+(this.hcname!=null?this.hcname:"")+"'>";
        
        
        var startloop=-1;
        if(!this.selbox)startloop=0;
         
        for(var ia=startloop;ia<this.cheader.count();ia++){
             
            if(ia==-1){

                    htm+="<th>";    
                    //var hcell=document.createElement('th');
                    //hcell.setAttribute("width","20");
                    //var chkbox=document.createElement('input');
                    //chkbox.setAttribute('type','checkbox');
                    //chkbox.setAttribute('onClick','_table_CheckAllRows(this.form,this.checked,"'+this.stringdentifier+'");');
                    //var p3=this.stringdentifier;
                    //chkbox.onclick=function(){
                    //  _table_CheckAllRows(this.form,this.checked,p3);
                    //};
                    //this.selectallcheck=chkbox;
                    //if(!this.selectOneRowOnly) {
                    //  hcell.appendChild(chkbox);
                    //}
                    //hrow.appendChild(hcell);
                 
            }else{              
                htm+="<th width='"+this.cwidth.item(ia)+"'>"+this.cheader.item(ia)+"</th>";
                
            }
        }
        htm+="</tr></thead>";
    
        //var tbody=document.createElement('tbody');
        //ta.appendChild(tbody);
        htm+="<tbody>"  ;
        for(var ia=0;ia<this.data.length;ia++){
            var datarow=this.data[ia];
            
            //var brow=ta.insertRow(ta.rows.length);

            //var brow=ta.insertRow(-1);
            //var brow=document.createElement("tr");
            //tbody.appendChild(brow);
            
            htm+="<tr rowid='"+datarow[this.rowid]+"' "
            var cls=datarow[this.classprop]!=null?" "+datarow[this.classprop]:"";
             
            if (ia%2==0){
                htm+=" class='odd"+cls+"' ";
            }else{              
                htm+=" class='even"+cls+"' ";
            }
            htm+=">";
            
            for(ib=startloop;ib<this.cdata.count();ib++){               
                
                
                if(ib==-1){
                    
                    uid=datarow[this.rowid];
                    
                    htm+="<td width='20'> "
                    htm+="<input type='checkbox' id='"+this.rowprefix+uid+"' value='"+uid+"' >";
                    htm+="</td>"                    

                    //var p3=this.stringdentifier;
                    //chkbox.onclick=function(){
                    //  _table_updateClickedRow(this,p3);
                    //}
                    
                }else{
                     

                    var objectPropName=this.cdata.item(ib);
                    var objectValue=datarow[this.cdata.item(ib)];
                    if (objectPropName!=null && objectPropName.indexOf('.')>0){
                        objectPropName=objectPropName.split('.');
                        objectValue=datarow[objectPropName[0]];
                        for (var p = 1; p < objectPropName.length; p++) {
                            objectValue=objectValue[objectPropName[p]];
                        }
                        
                    }
                    //AA : END
                    htm+="<td width='"+this.cwidth.item(ib)+"'>";                   
                    if(objectValue!=null) htm+=objectValue;                 
                    htm+="</td>"
                }
            }
            htm+="</tr>";
        }// end of inserting body cells
        
        htm+="</tbody>"
        htm+="</table>";
             
             
        //var form1=document.createElement('form');
        //this.htmlform=form1;
        //form1.appendChild(ta);
        
        
        this.selectedRecordCount=document.createElement('p');
        this.selectedRecordCount.setAttribute("align","right");
        if(this.selbox){
            form1.appendChild(this.selectedRecordCount);
        }
     
        //this.initialCheck(form1);      
        
        //remove everything before it updates
        //this is to make sure when you can this method subsequently it clears up previous content;
        this.table.innerHTML=htm;
        
        jQuery(this.table).find("td a[funcname]").each(function(idx,elem){          
            elem.onclick=function(){
                    if (this.getAttribute('funcparamtype') == 'number') {
                        eval(this.getAttribute('funcname') + "(" + this.getAttribute('funcparam') + ")");
                    }else {
                        eval(this.getAttribute('funcname') + "('" + this.getAttribute('funcparam') + "')");
                    }
                return false;
            }        
        });
        //this.table.appendChild(form1);
    }
}





_table_CheckAllRows=function(wotForm /*form*/,
                                  wotState /*state true/false */,
                                  thisidentifier
                                                    ) {
 

 
    var ti=_tableInstanceStack.get(thisidentifier);             
    for (a=0; a<wotForm.elements.length; a++) {
        if (wotForm.elements[a].id.indexOf(_checkboxPrefix) == 0) {
            wotForm.elements[a].checked = wotState;
            var id=wotForm.elements[a].value;
            if(wotState){
                if(!ti.selectedRecordIds.contains(id)) {ti.selectedRecordIds.add(id);}
            }else{
                if(ti.selectedRecordIds.contains(id)) {ti.selectedRecordIds.remove(id);}
            }
             
                _table_changeCheckedRowColor(wotForm.elements[a]);
             
        }
    }

    ti.selectedRecordCount.innerHTML="["+ti.selectedRecordIds.count+"]";
 
}

_table_changeCheckedRowColor=function(chkbox){
    //$("debug").innerHTML="<pre>"+DWRUtil.toDescriptiveString(chkbox, 2)+"</pre>";
    
     
    
    var bgcol="#FFFFFF";
    if(chkbox.checked) bgcol=_checkedRowColor; //it gets from maputil.js fle
    if(chkbox.parentNode.tagName=="TD"){
        var ccell=chkbox.parentNode;
        while(ccell!=null){
             ccell.style.backgroundColor=bgcol;
             /*
             with(ccel.style){
                if(chkbox.checked){
                        backgroundColor="#D7E5FF";
                        border="1px inset blue";
                }else{
                        backgroundColor="#FFFFFF";
                        border="0px inset blue";
                }
             }*/
             ccell=ccell.nextSibling; 
        }
    }
}


 
_table_updateClickedRow=function(/*id /*id of ticked checkbox*/
                       chkbox /*checkbox probably this object witin checkbox*/,
                       thisidentifier /*dojo.collection.ArrayList object*/ ) {
     var ti=_tableInstanceStack.get(thisidentifier);
    
     var id=chkbox.value;
     if(chkbox.checked){
        if(ti.selectOneRowOnly) {
                ti.selectedRecordIds.clear();
                _table_CheckAllRows(chkbox.form,false,thisidentifier);
                chkbox.checked=true;
                ti.selectedRecordIds.add(id);
        }else{
            if(!ti.selectedRecordIds.contains(id)) {
                
                ti.selectedRecordIds.add(id);
            }
        }
     }else{
        if(ti.selectedRecordIds.contains(id)) {ti.selectedRecordIds.remove(id);}
     }
     _table_changeCheckedRowColor(chkbox);
     
     //alert(selectedRecordIds.count);
    ti.selectedRecordCount.innerHTML="["+ti.selectedRecordIds.count+"]";
}



Table.prototype.initialCheck= function(wotForm /*form element */ ){
    
    
    var ti=_tableInstanceStack.get(this.stringdentifier);
    for (a=0; a<wotForm.elements.length; a++) {
        if (wotForm.elements[a].id.indexOf(_checkboxPrefix) == 0) {
            var id=wotForm.elements[a].value;
            if(ti.selectedRecordIds.contains(id)){
                wotForm.elements[a].checked=true;
                _table_changeCheckedRowColor(wotForm.elements[a]);
            }
            
        }
    }
     ti.selectedRecordCount.innerHTML="["+ti.selectedRecordIds.count()+"]";
}
