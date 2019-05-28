package com.fourelementscapital.api;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import org.json.JSONObject;
import io.swagger.annotations.Api;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Properties;
import javax.ws.rs.core.MediaType;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.fourelementscapital.controller.EmailService;
import com.fourelementscapital.db.ThemeEmailsDB;
import com.fourelementscapital.entities.Mail;

@Path("/")
@Api(value="/", description="Rest API")
public class FourEcapService  {
	
	public static final Logger log = LogManager.getLogger(FourEcapService.class.getName());
	JSONObject jsonObject = new JSONObject();
	ResourceBundle mybundle = ResourceBundle.getBundle("config");
	private String host = mybundle.getString("email.host");
	private String emailPort = mybundle.getString("email.port");
	private int port = Integer.parseInt(emailPort);
	private String sslTrust = mybundle.getString("email.ssl.trust");
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
	private String genreVoices= mybundle.getString("marytts.voices");
	private String caller_id = mybundle.getString("phone.caller.id");
	
	static ResourceBundle mybundle2 = ResourceBundle.getBundle("config");
	private static  String noThreadPool = mybundle2.getString("async.threadpool");
	public static int noOfQuickServiceThreads = Integer.parseInt(noThreadPool);
	
	//Creates a thread pool that reuses fixed number of threads(as specified by noOfThreads in this case).
	private ExecutorService quickService = Executors.newScheduledThreadPool(noOfQuickServiceThreads);
	
	//imonitorJ start
	
	private ArrayList<String> getThemeEmailsList = new ArrayList<String>();
	
	//create empty Queue
	 public static LinkedHashMap<String,Mail> queue = new LinkedHashMap<>();
	 
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
									 @FormParam("tts")Boolean tts,
									 @FormParam("themes")String themes) throws Exception{
		 
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
		 String senderThemes;
		 
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
		 }else if(themes == null){
				String errorMessageTheme;
				errorMessageTheme="Response: Themes cannot be empty";
				log.log(Level.ERROR, errorMessageTheme);
				jsonObject.put("success", false);
				jsonObject.put("message", errorMessageTheme);
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
				senderThemes = themes;
				
				//call void method send email
				String emailStatus="Processing";
				//create unique ID
				String uniqueID = UUID.randomUUID().toString();
				//System.out.println("Response Unique ID= " + uniqueID );
				

				ThemeEmailsDB teDB = new ThemeEmailsDB();
				setGetThemeEmailsList(teDB.getThemeEmailsFromDB(senderThemes));
				
				
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
				mail.setThemes(getGetThemeEmailsList());
				
				//add object to queue	
				queue.put(uniqueID,mail);
				jsonObject.put("Response",uniqueID);
				
				
				//get Theme
				System.out.println("Theme Emails : " + getGetThemeEmailsList().toString());
				
				//call send email method
				sendEmailAsync();

		 return Response.status(200).entity(jsonObject.toString() + "\n").build();
	 }
	 

	public void send() throws Exception{

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
				System.out.println("RestAPI Sending Email...");
				EmailService es = new EmailService();
				es.sendEmailProcess(host, prop, splitTo, splitAttachment, splitImage, 
									splitPhone, access_token, app_id_token, genreVoices, caller_id);
				
			} catch (Exception e) {
				e.printStackTrace();
			}		
	}
	
	
	
	@Produces(MediaType.APPLICATION_JSON)
	public void sendEmailAsync() throws Exception,IllegalThreadStateException{
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
		quickService.shutdown(); 
	}


	public ArrayList<String> getGetThemeEmailsList() {
		return getThemeEmailsList;
	}


	public void setGetThemeEmailsList(ArrayList<String> getThemeEmailsList) {
		this.getThemeEmailsList = getThemeEmailsList;
	}	
	
}// closing tag class !
	
	
	
	
	
	
	


