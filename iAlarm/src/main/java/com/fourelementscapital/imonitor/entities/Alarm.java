
package com.fourelementscapital.imonitor.entities;


/**
 * @author abhisekshukla
 *
 */
public class Alarm {
	
	private String subject;
	private String theme;
	private String body;
	private Boolean sayIt;
	private Boolean emailIt;
	private Boolean phoneCall;
	private String emailRecipients;
	private String emailReplyTo;
	private String emailRecipientsBCC;
	private String emailRecipientsCC;
	private String emailFromAddress;
	private String emailPassword;
	private String emailFromDisplayName;
	private String pathImages;
	private String pathAttachments;
	private AlertLevel alertLevel;
	private String phoneNumbers;
	private String ipAddress;

	

	public Alarm() {
		// TODO Auto-generated constructor stub
	}
	
	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getTheme() {
		return theme;
	}

	public void setTheme(String theme) {
		this.theme = theme;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public Boolean getSayIt() {
		return sayIt;
	}

	public void setSayIt(Boolean sayIt) {
		this.sayIt = sayIt;
	}

	public Boolean getEmailIt() {
		return emailIt;
	}

	public void setEmailIt(Boolean emailIt) {
		this.emailIt = emailIt;
	}

	public Boolean getPhoneCall() {
		return phoneCall;
	}

	public void setPhoneCall(Boolean phoneCall) {
		this.phoneCall = phoneCall;
	}

	public String getEmailRecipients() {
		return emailRecipients;
	}

	public void setEmailRecipients(String emailRecipients) {
		this.emailRecipients = emailRecipients;
	}

	public String getEmailReplyTo() {
		return emailReplyTo;
	}

	public void setEmailReplyTo(String emailReplyTo) {
		this.emailReplyTo = emailReplyTo;
	}

	public String getPathImages() {
		return pathImages;
	}

	public void setPathImages(String pathImages) {
		this.pathImages = pathImages;
	}

	public String getPathAttachments() {
		return pathAttachments;
	}

	public void setPathAttachments(String pathAttachments) {
		this.pathAttachments = pathAttachments;
	}

	public AlertLevel getAlertLevel() {
		return alertLevel;
	}

	public void setAlertLevel(AlertLevel alertLevel) {
		this.alertLevel = alertLevel;
	}

	public String getPhoneNumbers() {
		return phoneNumbers;
	}

	public void setPhoneNumbers(String phoneNumbers) {
		this.phoneNumbers = phoneNumbers;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getEmailPassword() {
		return emailPassword;
	}

	public void setEmailPassword(String emailPassword) {
		this.emailPassword = emailPassword;
	}

	public String getEmailRecipientsBCC() {
		return emailRecipientsBCC;
	}

	public void setEmailRecipientsBCC(String emailRecipientsBCC) {
		this.emailRecipientsBCC = emailRecipientsBCC;
	}

	public String getEmailRecipientsCC() {
		return emailRecipientsCC;
	}

	public void setEmailRecipientsCC(String emailRecipientsCC) {
		this.emailRecipientsCC = emailRecipientsCC;
	}

	public String getEmailFromAddress() {
		return emailFromAddress;
	}

	public void setEmailFromAddress(String emailFromAddress) {
		this.emailFromAddress = emailFromAddress;
	}

	public String getEmailFromDisplayName() {
		return emailFromDisplayName;
	}

	public void setEmailFromDisplayName(String emailFromDisplayName) {
		this.emailFromDisplayName = emailFromDisplayName;
	}
	
}
