package com.fourelementscapital.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.logging.log4j.Level;


import com.fourelementscapital.api.FourEcapService;
import com.fourelementscapital.api.PhoneNumberService;
import com.fourelementscapital.entities.Mail;
//import com.fourelementscapital.marytts.MaryTTS;
import com.fourelementscapital.marytts.MaryTTS;

public class EmailService{
	
	public void sendEmailProcess(String host,Properties prop,String splitTo,String splitAttachment,
								 String splitImage,String splitPhone,String access_token,
								 String app_id_token,String genreVoices,String caller_id){

		 String subjectEmail;
		 String emailMessage="";
		 String receiptEmailTo;
		 String receiptEmailCc;
		 String receiptEmailBcc;
		 String receiptEmailReplyTo;
		 String receiptEmailAttachment;
		 String receiptEmailImages;
		 String receiptPhoneNumbers;
		 boolean ttsEnableMode;
		 
		ArrayList<String> senderThemes = new ArrayList<String>();
		int sizequeue = FourEcapService.queue.size();
		
		try{
		
		for(int xx =0; xx <sizequeue; xx++)
		{
			
			 Set<Map.Entry<String, Mail>> mapSet = FourEcapService.queue.entrySet();
		     Map.Entry<String, Mail> elementAt5 = (new ArrayList<Map.Entry<String, Mail>>(mapSet)).get(0);
		        
		     System.out.println("unique ID= " + elementAt5.getKey());
			
		    subjectEmail = FourEcapService.queue.get(elementAt5.getKey()).getSubject(); 
			emailMessage = FourEcapService.queue.get(elementAt5.getKey()).getBody();
			receiptEmailTo = FourEcapService.queue.get(elementAt5.getKey()).getEmailTo();
			receiptEmailCc = FourEcapService.queue.get(elementAt5.getKey()).getEmailCC();
			receiptEmailBcc = FourEcapService.queue.get(elementAt5.getKey()).getEmailBcc();
			receiptEmailReplyTo = FourEcapService.queue.get(elementAt5.getKey()).getEmailReplyTo();
			receiptEmailAttachment = FourEcapService.queue.get(elementAt5.getKey()).getEmailAttachment();
			receiptEmailImages =  FourEcapService.queue.get(elementAt5.getKey()).getEmailImages();
			receiptPhoneNumbers = FourEcapService.queue.get(elementAt5.getKey()).getPhoneNumbers();
			ttsEnableMode = FourEcapService.queue.get(elementAt5.getKey()).isTtsEnable();
			senderThemes = FourEcapService.queue.get(elementAt5.getKey()).getThemes();
			
			
		
			
			final String username = senderThemes.get(4).toString();
			 String senderEmail = senderThemes.get(1).toString();
			final String password = senderThemes.get(2).toString();
			 String senderEmailName = senderThemes.get(3).toString();
			

			//Instantiate mail session, compose email including subject, receipient & content
			Session session = Session.getInstance(prop, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication(){
			return new PasswordAuthentication(username, password);
			}
			});
			
			
			/*Begin 
			*this algorithm for handle sending mail from 
			*single to multiple recipients by delimitted ";"
			*
			* */
			String [] recipientList = receiptEmailTo.split(splitTo);
			InternetAddress[] recipientAddress = new InternetAddress[recipientList.length];
			int counter=0;
			for(String reciptEmailFinal : recipientList){
				recipientAddress[counter] = new InternetAddress(reciptEmailFinal.trim());
				counter++;
			}
			
			/*
			* 
			* End 
			* */
			
			Message message = new MimeMessage(session);
			message.setSentDate(new Date());
			message.setSubject(subjectEmail);                   
			message.setFrom(new InternetAddress(senderEmail,senderEmailName));
			
			message.setRecipients(Message.RecipientType.TO, recipientAddress);
			
			if(receiptEmailCc != null){
				message.addRecipient(Message.RecipientType.CC, new InternetAddress(receiptEmailCc));
			}
			if(receiptEmailBcc != null){
				message.addRecipient(Message.RecipientType.BCC, new InternetAddress(receiptEmailBcc));
			}
			
			if(receiptEmailReplyTo != null){
				
				message.setReplyTo(new javax.mail.Address[]
						{
						  new InternetAddress(receiptEmailReplyTo)
						});
				
				//message.setReplyTo(InternetAddress.parse(receiptEmailReplyTo, true));
			}
			
			MimeBodyPart mimeBodyPart = new MimeBodyPart();
			mimeBodyPart.setContent(emailMessage, "text/html");
			
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(mimeBodyPart);
			
			//if attachment file is not null
			if(receiptEmailAttachment != null){
			
				String[] emailAttachmentParamList = receiptEmailAttachment.split(splitAttachment);
				int ix =0;
				for(String emailAttachmentFinal : emailAttachmentParamList){
					
					MimeBodyPart attachmentBodyPart = new MimeBodyPart();
					emailAttachmentParamList[ix] = emailAttachmentFinal.trim();
					attachmentBodyPart.attachFile(new File(emailAttachmentParamList[ix]));
					multipart.addBodyPart(attachmentBodyPart);
					message.setContent(multipart);
					ix++;
				}
			}
			
			//if image file is not null
			if(receiptEmailImages != null){
				
				String[] emailImagesParamList = receiptEmailImages.split(splitImage);
				int x =0;
				for(String emailImagesFinal : emailImagesParamList){
					
					//String image ="/home/dewa/imonitorj/Tree.png";
					emailImagesParamList[x] = emailImagesFinal.trim();
					
					DataSource source = new FileDataSource(emailImagesParamList[x]);
					MimeBodyPart imageBodyPart = new MimeBodyPart();
				
					imageBodyPart.setDataHandler(new DataHandler(source));
					//imageBodyPart.setFileName(emailImagesParamList[x]);
				    imageBodyPart.setHeader("Content-ID", "image_id");
			      multipart.addBodyPart(imageBodyPart);
				    x++;
				} 
				
				 	mimeBodyPart = new MimeBodyPart();
			      mimeBodyPart.setContent("<br><h3>Find below attached image</h3>"
			              + "<img src='cid:image_id'>", "text/html");
			      multipart.addBodyPart(mimeBodyPart);
				    message.setContent(multipart);
			
			 }
			
			message.setContent(multipart);
				
				
			//Connection  to sender email & send 
			Transport transport = session.getTransport("smtp");
			transport.connect(host,username, password);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();
			
			
			//using hooio library 
			if(receiptPhoneNumbers != null){
				try{
				PhoneNumberService pns = new PhoneNumberService();
				pns.getPhoneNumberService(receiptPhoneNumbers, splitPhone, access_token, app_id_token, caller_id);
				}catch (Exception e) {
					e.printStackTrace();
					}
				}
			
			 //using lib-tts 4Ecap
		    if(ttsEnableMode == true){
		    	
		    	try {
		    		MaryTTS tts = new MaryTTS();
		    		//System.out.println("List of voices =====================================>" + tts.getAvailableVoices().toString());
			    	tts.setVoice(genreVoices);
			    	tts.speak(subjectEmail, 2.0f, false, true);
			    	tts.stopSpeaking();
				} catch (Exception e) {
					e.printStackTrace();
				}
		    }
		    
		    FourEcapService.queue.remove(elementAt5.getKey());
			String InfoMessageFinal ="Response: Email Sent Successfully...";
			FourEcapService.log.log(Level.INFO, InfoMessageFinal );
			
		}
		}catch(Exception e){
			e.printStackTrace();
		}
				
	}
}//end tag Class !!!