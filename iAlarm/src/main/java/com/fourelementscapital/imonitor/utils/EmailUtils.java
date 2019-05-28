package com.fourelementscapital.imonitor.utils;

import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fourelementscapital.imonitor.config.EmailConfiguration;
import com.fourelementscapital.imonitor.entities.Alarm;

/*Notes from Gama :
Lambda expression (-> sign) only exist on Java 8 and above.*/

public class EmailUtils {
	
	private static final Logger log = LogManager.getLogger(EmailUtils.class.getName());
    
	public static void sendMail(final Alarm alarm) {
		
		//Java 8 
		/*new Thread(() -> {
					try {
						Properties props = new Properties(); 
						props.put("mail.smtp.host", EmailConfiguration.getHost());
						props.put("mail.smtp.auth", "true");
						props.put("mail.smtp.starttls.enable", "true");
						props.put("mail.smtp.port", EmailConfiguration.getPort());
						props.put("mail.smtp.ssl.trust", EmailConfiguration.getHost());
						
						Authenticator auth = new Authenticator() {
							//override the getPasswordAuthentication method
							protected PasswordAuthentication getPasswordAuthentication() {
								return new PasswordAuthentication(EmailConfiguration.getEmailFrom(), EmailConfiguration.getPassword());
							}
						};
						Session session = Session.getInstance(props, auth);
						sendMailHelper(alarm, session, auth);
					} catch (Exception e) {
						log.log(Level.ERROR, e);
					}
		}).start();*/
		
		
		// Modified to Java 7
		Thread thread = new Thread() {
			public void run() {
				try {
					Properties props = new Properties(); 
					props.put("mail.smtp.host", EmailConfiguration.getHost());
					props.put("mail.smtp.auth", "true");
					props.put("mail.smtp.starttls.enable", "true");
					props.put("mail.smtp.port", EmailConfiguration.getPort());
					props.put("mail.smtp.ssl.trust", EmailConfiguration.getHost());
					
					Authenticator auth = new Authenticator() {
						//override the getPasswordAuthentication method
						protected PasswordAuthentication getPasswordAuthentication() {
							return new PasswordAuthentication(EmailConfiguration.getEmailFrom(), EmailConfiguration.getPassword());
						}
					};
					Session session = Session.getInstance(props, auth);
					sendMailHelper(alarm, session, auth);
				} catch (Exception e) {
					log.log(Level.ERROR, e);
				}
			}
		};
		thread.start();
		
	}


	private static void sendMailHelper(Alarm alarm, Session session, Authenticator auth) {
		try {
			StringBuilder images = new StringBuilder();
			String recipient = alarm.getEmailRecipients();
			String recipientCC = alarm.getEmailRecipientsCC();
			String recipientBCC = alarm.getEmailRecipientsBCC();
			String replyTo = alarm.getEmailReplyTo();
			String subject = alarm.getSubject();
			String emailFromAddress = alarm.getEmailFromAddress();
			String emailFromDisplayName = alarm.getEmailFromDisplayName();
			String body = StringUtils.getEmptyIfNull(alarm.getBody());
			List<String> imageList = StringUtils.getListFromCommaDelimatedString(alarm.getPathImages());
			List<String> attachmentList = StringUtils.getListFromCommaDelimatedString(alarm.getPathAttachments());
			
			MimeMessage message = new MimeMessage(session);
		
			if(!StringUtils.isNullOrEmpty(recipient)) {
				message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
			}
		
			if(!StringUtils.isNullOrEmpty(recipientCC)) {
				message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(recipientCC));
			}
			
			if(!StringUtils.isNullOrEmpty(recipientBCC)) {
				message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(recipientBCC));
			}
			
			if(!StringUtils.isNullOrEmpty(replyTo)) {
				message.setReplyTo(InternetAddress.parse(replyTo));
			}
			
			message.setSubject(subject);
			message.setFrom(new InternetAddress(EmailConfiguration.getEmailFrom(), emailFromDisplayName)); //TODO: we should update this with emailFromAddress once we start using themes
			
			BodyPart messageBodyPart = new MimeBodyPart();

			Multipart multipart = new MimeMultipart();

			if (imageList != null) {
				for (int i = 0; i < imageList.size(); i++) {
					 multipart.addBodyPart(addImages(imageList.get(i), i + ""));
					images.append("<img alt='imageId_" + i + "' src=cid:imageId_" + i + ">");
				}

				if (images.length() > 0) {
					body += "<br/>" + images.toString();
				}
			}

			messageBodyPart.setContent(body, "text/html");
			multipart.addBodyPart(messageBodyPart);
		
			if (attachmentList != null && attachmentList.size() > 0) {
				for (int i=0; i<attachmentList.size(); i++) {
					 multipart.addBodyPart(addAttachment(attachmentList.get(i)));
				}
			}

			message.setContent(multipart);
			
			Transport.send(message);
			
		} catch (Exception e) {
			log.log(Level.ERROR, e);
		}

	}
	
	private static BodyPart addAttachment(String filename) throws MessagingException {
	    DataSource source = new FileDataSource(filename);
	    BodyPart messageBodyPart = new MimeBodyPart();        
	    messageBodyPart.setDataHandler(new DataHandler(source));
	    messageBodyPart.setFileName(source.getName());
	    return messageBodyPart;
	}
	
	private static BodyPart addImages(String filename, String id) throws MessagingException {
	    DataSource source = new FileDataSource(filename);
	    BodyPart messageBodyPart = new MimeBodyPart();        
	    messageBodyPart.setDataHandler(new DataHandler(source));
	    messageBodyPart.setHeader("Content-ID", "<imageId_"+ id +">");
	    return messageBodyPart;
	}
}