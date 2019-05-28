package com.fourelementscapital.api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
//import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
//import java.security.cert.Certificate;
import java.security.cert.Certificate;

import javax.net.ssl.SSLPeerUnverifiedException;
//import javax.security.cert.X509Certificate;

import com.sun.net.ssl.HttpsURLConnection;

@SuppressWarnings("deprecation")

public class PhoneNumberService{
	
	/*private void printHttpsCert(HttpsURLConnection con) throws SSLPeerUnverifiedException{
		
		if(con != null){
			try{
				
				System.out.println("Response Code: " + con.getResponseCode());
				System.out.println("Chiper Suite: " + con.getCipherSuite() );
				System.out.println("\n");
				
				Certificate[] certs = con.getServerCertificates();
				for(Certificate cert : certs){
					//System.out.println("Cert Type : " + cert.getType());
					   System.out.println("Cert Hash Code : " + cert.hashCode());
					   System.out.println("Cert Public Key Algorithm : " 
				                                    + cert.getPublicKey().getAlgorithm());
					   System.out.println("Cert Public Key Format : " 
				                                    + cert.getPublicKey().getFormat());
					   System.out.println("\n");
				}
				
			}catch(Exception e){
				e.printStackTrace();
			}
		//}
		
	}*/
	
	/*private void printContent(HttpsURLConnection con){
		//if(con != null){
			try {
				
				BufferedReader in = new BufferedReader(
				        new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();
	
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				
				//print result
				System.out.println(response.toString());
				
				in.close();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		//}
	}*/
	
	
	
	
	public void getPhoneNumberService(String poParam, String poParamSplit, String poAccessToken, String poAppIDToken, String caller_id){
		
		String[] phoneNumberParamList = poParam.split(poParamSplit);
		int loop=0;
		
		try{
			
		
			for(String phoneNumberFinal : phoneNumberParamList){
				
				phoneNumberParamList[loop] = phoneNumberFinal.trim();
	
				//for running hoiio api call
				String urlAPICall ="https://secure.hoiio.com/open/ivr/start/dial?";
				  	   urlAPICall = urlAPICall + "dest=%2B"+phoneNumberParamList[loop]+"&";
			           urlAPICall = urlAPICall + "access_token="+poAccessToken+"&";
			           urlAPICall = urlAPICall + "app_id="+poAppIDToken+"&";
			           urlAPICall = urlAPICall + "caller_id="+ caller_id +"&"; 
				  
				URL obj = new URL(urlAPICall);
				URLConnection yc = obj.openConnection();
				BufferedReader in = new BufferedReader(
                        new InputStreamReader(
                        yc.getInputStream()));
				 String inputLine;
					
		            boolean isSuccess = false;
		            while ((inputLine = in.readLine()) != null) { 
		                //System.out.println(inputLine);
		    	        if (inputLine.contains("\"status\":\"success_ok\"")) {
		    	        	isSuccess = true;
		    	        	break;
		    	        }
		            }
		            in.close();
				
				/*HttpsURLConnection con = (HttpsURLConnection)obj.openConnection();
				con.connect();*/
				
				
				
				//dumpl all cert info
				//printHttpsCert(con);
				
				//dump all the content
				//printContent(con);
				
				System.out.println("\nSending 'GET' request to URL : " + urlAPICall);
					
			}
		}
		catch (Exception e) {
			// TODO: handle exception
		}
	}
	
  }//end class !!!
