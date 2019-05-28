package com.fourelementscapital.entities;

import java.util.ArrayList;

public class Mail {
	
	private String subject;
	private String body;
	private String emailTo;
	private String emailCC;
	private String emailBcc;
	private String emailReplyTo;
	private String emailAttachment;
	private String emailImages;
	private String phoneNumbers;
	private Boolean ttsEnable;
	private String emailStatus;
	private ArrayList<String> themes;
	
	

	public ArrayList<String> getThemes() {
		return themes;
	}

	public void setThemes(ArrayList<String> themes) {
		this.themes = themes;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getEmailTo() {
		return emailTo;
	}

	public void setEmailTo(String emailTo) {
		this.emailTo = emailTo;
	}

	public String getEmailCC() {
		return emailCC;
	}

	public void setEmailCC(String emailCC) {
		this.emailCC = emailCC;
	}

	public String getEmailBcc() {
		return emailBcc;
	}

	public void setEmailBcc(String emailBcc) {
		this.emailBcc = emailBcc;
	}

	public String getEmailReplyTo() {
		return emailReplyTo;
	}

	public void setEmailReplyTo(String emailReplyTo) {
		this.emailReplyTo = emailReplyTo;
	}

	public String getEmailAttachment() {
		return emailAttachment;
	}

	public void setEmailAttachment(String emailAttachment) {
		this.emailAttachment = emailAttachment;
	}

	public String getEmailImages() {
		return emailImages;
	}

	public void setEmailImages(String emailImages) {
		this.emailImages = emailImages;
	}

	public String getEmailStatus() {
		return emailStatus;
	}
	
	public String getPhoneNumbers() {
		return phoneNumbers;
	}

	public void setPhoneNumbers(String phoneNumbers) {
		this.phoneNumbers = phoneNumbers;
	}
	
	
	public boolean isTtsEnable() {
		return ttsEnable;
	}

	public void setTtsEnable(boolean ttsEnable) {
		this.ttsEnable = ttsEnable;
	}

	public void setEmailStatus(String emailStatus) {
		this.emailStatus = emailStatus;
	}

	
	
	  

}
