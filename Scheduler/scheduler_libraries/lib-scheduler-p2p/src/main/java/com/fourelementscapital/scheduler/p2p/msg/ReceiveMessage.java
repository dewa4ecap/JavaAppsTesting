/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fourelementscapital.scheduler.p2p.msg;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

public class ReceiveMessage {

	private Map data;
	
	private Logger log = LogManager.getLogger(ReceiveMessage.class.getName());
	
	public ReceiveMessage(Map attachments){
		this.data=attachments;
	}
	
	
	
	public void process(){
		
		String classname=(String)this.data.get(MessageNames.MESSAGE_BEAN_NAME);
		String loc="1";
		if(classname!=null && !classname.equals("")){
			try{
				
				Class c=Class.forName(classname);
				Constructor ct = c.getConstructor();
				loc+=",2";
				MessageHandler mha=(MessageHandler)ct.newInstance();
				//this.data.remove("JxtaWireHeader"); //remove jxta msessage
				//this.data.remove("RendezVousPropagatejxta-NetGroup");
				loc+=",3";
				BeanUtils.populate(mha, this.data);
				//log.debug("msg recieved,mha.getResponseJSON():"+mha.getResponseJSON()+" mha getMsgCreator:"+mha.getMsgCreator()+ " receipient:"+mha.getMsgRecipient() );
				Map rtn=null;				
				loc+=",4";
				//log.debug("~~~~~~ executing at Destination: data:"+data.keySet());
				
				if(mha!=null && mha.getMsgCreator()!=null){
					new CommunicationTrace(mha.getMsgCreator()).incoming(this.data);	
				}
				
				if(this.data!=null && this.data.get(MessageNames.MESSAGE_TYPE_CALLBACK)!=null){
					//execute at source
					//log.debug("~~~~~~ callback:"+this.data.get(MessageNames.MESSAGE_TYPE_CALLBACK));
					loc+=",5";
					if(mha instanceof PostCallBack){
						HashMap h=new HashMap();
						if(mha.getResponseJSON()!=null){
							loc+=",6";
							JSONObject json1=new JSONObject(mha.getResponseJSON());
							loc+=",7";
							//log.debug("~~~~~~ json1:"+json1);
							for(Iterator it=json1.keys();it.hasNext();){
								Object key=(Object)it.next();
								//System.out.print("key:"+key+" value:"+json1.get((String)key));
								h.put(key, json1.get(key+""));
							}
						}
						//log.debug("callback data:"+h);					
						((PostCallBack)mha).callBack(h);
					}
				}else{
					//destination					
					//log.debug("~~~~~~ executing at Destination: mha:"+data+mha);
					loc+=",8";
					rtn=mha.executeAtDestination();
					loc+=",9";
					//log.debug("~~~~~~ executed : rtn:"+rtn);
					loc+=",10,rtn:"+rtn;
					if(rtn!=null){
						mha.setResponseJSON(new JSONObject(rtn).toString());
						loc+=",10a";
					}
					loc+="10b";
				}
				//loc+=",data:"+this.data;
				HashMap debugd=new HashMap();
				debugd.putAll(this.data);
				debugd.remove("RendezVousPropagatejxta-NetGroup");
				debugd.remove("JxtaWireHeader");
				
				//if(classname.contains("PeerOnlineStatus") || classname.contains("PeerOnlineStatus") ){
				//}else{
					log.debug("classname:"+classname+" data:"+debugd);
				//}
				
				
				if(mha instanceof PostCallBack && this.data.get(MessageNames.MESSAGE_TYPE_CALLBACK)==null && (rtn==null || (rtn!=null && rtn.get(PostCallBack.IGNORE_CALLBACK)==null))){
					
					   //PostClientTestMessage pctm=new PostClientTestMessage();
					   //pctm.setName("this name");
					   //pctm.setScript("this is my script");
					   //pctm.setTest("this is test");
						loc+=",11";
						PostMessage pm=new PostMessage(mha,mha.getMsgCreator());
						pm.setCallBack();
						loc+=",12";
					    pm.send();	
					    //log.debug("callback message sent");
				}
				loc+=",mha:"+mha;
			}catch(Exception e){
				e.printStackTrace();
				
				log.error("process() E:"+e.getMessage()+" classname:"+classname+" loc:"+loc);
			}
			
		}
		
	}
}


