package com.fourelementscapital.api;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import org.json.JSONObject;

import io.swagger.annotations.Api;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Session;
import javax.mail.PasswordAuthentication;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import javax.ws.rs.core.MediaType;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


//import io.swagger.v3.oas.annotations.media.Content;
//import io.swagger.v3.oas.annotations.media.Schema;
//import io.swagger.v3.oas.annotations.responses.ApiResponse;
//import io.swagger.v3.oas.annotations.tags.Tag;





@Path("/")
@Api(value="/", description="Rest API")
public class FourEcapService {
	
	private static final Logger log = LogManager.getLogger(FourEcapService.class.getName());
	JSONObject jsonObject = new JSONObject();
	
	ResourceBundle mybundle = ResourceBundle.getBundle("config");
	private String emailPort = mybundle.getString("email.port");
	private int port = Integer.parseInt(emailPort);
	private String host = mybundle.getString("email.host");
	private String sslTrust = mybundle.getString("email.ssl.trust");
	private String senderEmail= mybundle.getString("email.sender");
	private String username = mybundle.getString("email.username");
	private String password = mybundle.getString("email.password");
	private String senderEmailName = mybundle.getString("email.sender.name");
	private String propHost = mybundle.getString("email.prop.host");
	private String propAuth = mybundle.getString("email.prop.auth");
	private String propPort = mybundle.getString("email.prop.port");
	private String propStarttls = mybundle.getString("email.prop.starttls");
	private String propSslTrust= mybundle.getString("email.prop.ssl.trust");
	private String splitTo = mybundle.getString("email.split.to");
	private String splitAttachment = mybundle.getString("email.split.attachment");
	private String splitImage = mybundle.getString("email.split.image");
	
	private String access_token= mybundle.getString("phone.accessToken");
	private String app_id_token= mybundle.getString("phone.appId");
	private String splitPhone = mybundle.getString("phone.split.call");
	
	static ResourceBundle mybundle2 = ResourceBundle.getBundle("config");
	private static  String noThreadPool = mybundle2.getString("async.threadpool");
	public static int noOfQuickServiceThreads = Integer.parseInt(noThreadPool);
	
	//Creates a thread pool that reuses fixed number of threads(as specified by noOfThreads in this case).
	private ScheduledExecutorService quickService = Executors.newScheduledThreadPool(noOfQuickServiceThreads); 
	
	//imonitorJ start

	//create empty Queue
	 private static LinkedHashMap<String,Mail> queue = new LinkedHashMap<>();
	 //rest api to get queue size of email
	    @POST
		@Path("action/getEmailSize")
		public Response checkSize() throws Exception {		
			//System.out.println("Count Queue Email  = " + queue.size());
			jsonObject.put("Response:", "Count Queue Email  = " + queue.size());
			return Response.status(200).entity(jsonObject.toString() + "\n").build();
		}
		 
		 
		//Rest API to get the current status using the unique id
		@POST
		@Path("action/getEmailStatus")
		@Produces(MediaType.APPLICATION_JSON)
		public Response getStatus(@FormParam("emailUniqueId") String emailUniqueId){
			
			if(queue.containsKey(emailUniqueId)){
				jsonObject.put("Response:", "EmailId " + emailUniqueId + " is Processing");
			}else{
				jsonObject.put("Response:", "EmailId " + emailUniqueId + " is Delivered");
			}
			
			return Response.status(200).entity(jsonObject.toString() + "\n").build();
						
		}
	 
	  //add queue based on API Call Email
	  @POST
	  @Path("action/sendEmail")
	  @Produces(MediaType.APPLICATION_JSON)
	  public Response sendEmail(@FormParam("subject") String subject,
									 @FormParam("body") String body,
									 @FormParam("emailTo") String emailTo,
									 @FormParam("emailCc") String emailCc,
									 @FormParam("emailBcc") String emailBcc,
									 @FormParam("emailReplyTo") String emailReplyTo,
									 @FormParam("emailAttachment") String emailAttachment,
									 @FormParam("emailImages") String emailImages,
									 @FormParam("phoneNumbers") String phoneNumbers,
									 @FormParam("tts")Boolean tts) throws Exception{
		 
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
		 
		//check request param to log in log4j2
		 if (subject == null) {
				String errorMassagesubject;		
				errorMassagesubject="Response: The Subject cannot be empty";
				log.log(Level.ERROR, errorMassagesubject);
				jsonObject.put("success", false);
				jsonObject.put("message", errorMassagesubject);
				return Response.status(500).entity(jsonObject.toString()).build();
		 }
		 
		 else if(emailTo == null){
				String errorMessageEmailTo;
				errorMessageEmailTo="Response: The EmailTo cannot be empty";
				log.log(Level.ERROR, errorMessageEmailTo);
				jsonObject.put("success", false);
				jsonObject.put("message", errorMessageEmailTo);
				return Response.status(500).entity(jsonObject.toString()).build();
			
		 }else if(body == null){
			 
			    body =""; // because value first in body is null so we change to ""
			 
				try(BufferedReader br = new BufferedReader(new FileReader(
						(mybundle.getString("email.message"))
						))){
					
					String sCurrentLine="";
					while((sCurrentLine = br.readLine()) != null){
						body += sCurrentLine + "<BR>";
					}
					
					String InfoMessageElse;
					//get log request if param not null
					InfoMessageElse = "Request: subject=" + subject;
					InfoMessageElse = InfoMessageElse + ",body=" + body;
					InfoMessageElse = InfoMessageElse + ",EmailTo=" + emailTo;
					
					if(emailCc != null){
						InfoMessageElse = InfoMessageElse + ",EmailCc=" + emailCc;
					}
					if(emailBcc != null){
						InfoMessageElse = InfoMessageElse + ",EmailBcc=" + emailBcc;	
					}
					if(emailReplyTo != null){
						InfoMessageElse = InfoMessageElse + ",EmailReplyTo=" + emailReplyTo;
					}
					if(emailAttachment != null){
						InfoMessageElse = InfoMessageElse + ",EmailAttachment=" + emailAttachment;
					}
					if(emailImages != null){
						InfoMessageElse = InfoMessageElse + ",EmailImages=" + emailImages;
					}
					
					log.log(Level.INFO, InfoMessageElse );
					//System.out.println(InfoMessageElse);
					
				}catch(Exception e){
					String errorMsgEx;
					errorMsgEx="Response: Cannot find default message";
					
					log.log(Level.ERROR, errorMsgEx);
					jsonObject.put("success", false);
					jsonObject.put("message", errorMsgEx);

					e.printStackTrace();
				}
				
		   }else{
				
				String InfoMessageElse2;
				
				//get log request if param not null
				InfoMessageElse2 = "Request: subject=" + subject;
				InfoMessageElse2 = InfoMessageElse2 + ",body=" + body;
				InfoMessageElse2 = InfoMessageElse2 + ",EmailTo=" + emailTo;
				
				if(emailCc != null){
					InfoMessageElse2 = InfoMessageElse2 + ",EmailCc=" + emailCc;
					}
				if(emailBcc != null){
					InfoMessageElse2 = InfoMessageElse2 + ",EmailBcc=" + emailBcc;	
					}
				if(emailReplyTo != null){
					InfoMessageElse2 = InfoMessageElse2 + ",EmailReplyTo=" + emailReplyTo;
				}
				if(emailAttachment != null){
					InfoMessageElse2 = InfoMessageElse2 + ",EmailAttachment=" + emailAttachment;
				}
				if(emailImages != null){
					InfoMessageElse2 = InfoMessageElse2 + ",EmailImages=" + emailImages;
				}
				
				log.log(Level.INFO, InfoMessageElse2 );
				//System.out.println(InfoMessageElse2);
		   }
		
				subjectEmail = subject;
				emailMessage = body;
				receiptEmailTo = emailTo;
				receiptEmailCc = emailCc;
				receiptEmailBcc = emailBcc;
				receiptEmailReplyTo = emailReplyTo;
				receiptEmailAttachment = emailAttachment;
				receiptEmailImages = emailImages;
				receiptPhoneNumbers = phoneNumbers;
				ttsEnableMode = tts;
				
				//call void method send email
				String emailStatus="Processing";
				//create unique ID
				String uniqueID = UUID.randomUUID().toString();
				//System.out.println("Response Unique ID= " + uniqueID );
				
				//create Hash Map & Instantiation class Mail
				Mail mail = new Mail();
				mail.setSubject(subjectEmail);
				mail.setBody(emailMessage);
				mail.setEmailTo(receiptEmailTo);
				mail.setEmailCC(receiptEmailCc);
				mail.setEmailBcc(receiptEmailBcc);
				mail.setEmailReplyTo(receiptEmailReplyTo);
				mail.setEmailAttachment(receiptEmailAttachment);
				mail.setEmailImages(receiptEmailImages);
				mail.setPhoneNumbers(receiptPhoneNumbers);
				mail.setTtsEnable(ttsEnableMode);
				mail.setEmailStatus(emailStatus);
				
				//add object to queue	
				queue.put(uniqueID,mail);
				jsonObject.put("Response",uniqueID);
				//call send email method
				sendEmailAsync();

		 return Response.status(200).entity(jsonObject.toString() + "\n").build();
	 }
	 

       
	@Produces(MediaType.APPLICATION_JSON)
	public void send() throws Exception{
		//Rest API to get the current status using the unique id
		
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
		 

			Properties prop = new Properties();
			
			 // Setup mail server 
			prop.put(propHost, host);
			// enable authentication 
			prop.put(propAuth, true);
			
			 // SSL Port 
			prop.put(propPort, port);
			prop.put(propStarttls, true);
			prop.put(propSslTrust, sslTrust);
			
			try {
				

				//Instantiate mail session, compose email including subject, receipient & content
					Session session = Session.getInstance(prop, new Authenticator() {
					@Override
					protected PasswordAuthentication getPasswordAuthentication(){
					return new PasswordAuthentication(username, password);
				}
				});
				
				System.out.println("RestAPI Sending Email...");
				
				int sizequeue = queue.size();
				
				
				for(int xx =0; xx <sizequeue; xx++)
				{
					
					 Set<Map.Entry<String, Mail>> mapSet = queue.entrySet();
				     Map.Entry<String, Mail> elementAt5 = (new ArrayList<Map.Entry<String, Mail>>(mapSet)).get(0);
				        
				     System.out.println("unique ID= " + elementAt5.getKey());
					
				    subjectEmail = queue.get(elementAt5.getKey()).getSubject(); 
					emailMessage = queue.get(elementAt5.getKey()).getBody();
					receiptEmailTo = queue.get(elementAt5.getKey()).getEmailTo();
					receiptEmailCc = queue.get(elementAt5.getKey()).getEmailCC();
					receiptEmailBcc = queue.get(elementAt5.getKey()).getEmailBcc();
					receiptEmailReplyTo = queue.get(elementAt5.getKey()).getEmailReplyTo();
					receiptEmailAttachment = queue.get(elementAt5.getKey()).getEmailAttachment();
					receiptEmailImages =  queue.get(elementAt5.getKey()).getEmailImages();
					receiptPhoneNumbers = queue.get(elementAt5.getKey()).getPhoneNumbers();
					ttsEnableMode = queue.get(elementAt5.getKey()).isTtsEnable();
					
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
						message.setReplyTo(InternetAddress.parse(receiptEmailReplyTo));
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
					
					
					//String phoneNumber="6282233426082,685737040742";
					//String msg="Development Server is Down";
					//boolean tts_enabled=true;
					
					if(receiptPhoneNumbers != null){
					
						String[] phoneNumberParamList = receiptPhoneNumbers.split(splitPhone);
						int loop=0;
					
						for(String phoneNumberFinal : phoneNumberParamList){
							
							phoneNumberParamList[loop] = phoneNumberFinal.trim();

							//for running hoiio api call
							String urlAPICall ="https://secure.hoiio.com/open/ivr/start/dial?";
							  	   urlAPICall = urlAPICall + "dest=%2B"+phoneNumberParamList[loop]+"&";
						           urlAPICall = urlAPICall + "access_token="+access_token+"&";
						           urlAPICall = urlAPICall + "app_id="+app_id_token+"&";
							       
							    if( ttsEnableMode == true){
								       urlAPICall = urlAPICall + "ring_time=20&"; 
							    	   urlAPICall = urlAPICall + "msg="+ URLEncoder.encode(subjectEmail, "UTF-8");
							       }else{
								       urlAPICall = urlAPICall + "ring_time=20"; 
							       }
							       
							
							URL obj = new URL(urlAPICall);
							HttpURLConnection con = (HttpURLConnection) obj.openConnection();
							con.setRequestMethod("GET");
							//con.setRequestProperty("User-Agent", USER_AGENT);
							int responseCode = con.getResponseCode();
							
							System.out.println("\nSending 'GET' request to URL : " + urlAPICall);
							System.out.println("Response Code : " + responseCode);
							
							BufferedReader in = new BufferedReader(
							        new InputStreamReader(con.getInputStream()));
							String inputLine;
							StringBuffer response = new StringBuffer();

							while ((inputLine = in.readLine()) != null) {
								response.append(inputLine);
							}
							in.close();

							//print result
							System.out.println(response.toString());
							
							
						}
					}
					
					queue.remove(elementAt5.getKey());
					String InfoMessageFinal ="Response: Email Sent Successfully...";
					log.log(Level.INFO, InfoMessageFinal );
					
					
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}	
			
	}
	
	

	@Produces(MediaType.APPLICATION_JSON)
	public void sendEmailAsync() throws Exception{
		
		quickService.submit(new Runnable() {
			
			@Override
			public void run() {
				try {
						
					send();
				
				} catch (Exception e) {
					log.log(Level.ERROR, " Response: Exception occur while a mail : " + e );
				}
				
			}
		});
    }

	
	
	
}// closing tag class !
	
	
	
	
	
	
	


