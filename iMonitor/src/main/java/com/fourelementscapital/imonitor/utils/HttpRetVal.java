package com.fourelementscapital.imonitor.utils;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

public class HttpRetVal {
	public enum HttpRetValStatus {
		SUCCESS,
		FAILURE_TOKEN_INVALIDATED,
		FAILURE_OTHER;
	}
	
	private HttpRetValStatus status;
	int statusCode;
	HttpEntity httpEntity;
	String stringEntity; 
	
	public HttpRetVal(HttpResponse response) throws Exception {
		if (response != null) {
			this.statusCode = response.getStatusLine().getStatusCode();
			this.httpEntity = response.getEntity();
			updateStatus();
		} else {
			this.status = HttpRetValStatus.FAILURE_OTHER;
			this.statusCode = 0;
		}
	}
	
	private void updateStatus() {
		switch (this.statusCode) {
		case HttpStatus.SC_OK:
		case HttpStatus.SC_ACCEPTED:
		case HttpStatus.SC_CREATED:
			this.status = HttpRetValStatus.SUCCESS;
			break;
		case HttpStatus.SC_UNAUTHORIZED:
		case HttpStatus.SC_FORBIDDEN:
			this.status = HttpRetValStatus.FAILURE_TOKEN_INVALIDATED;
			break;
		default:
			this.status = HttpRetValStatus.FAILURE_OTHER;
		}
	}
	public HttpRetValStatus getStatus() {
		return this.status;
	}
	
	public void setStatus(HttpRetValStatus status) {
		this.status = status;
	}
	
	public String getStringEntity() throws Exception {
		setStringEntity();
		return this.stringEntity;
	}

	public String setStringEntity() throws Exception {
		if ((this.stringEntity == null) && (this.httpEntity != null)) {
			try {
				this.stringEntity = EntityUtils.toString(this.httpEntity);
				EntityUtils.consume(this.httpEntity);
			} catch (ParseException e) {

			} catch (IOException e) {

			}
		}
		return this.stringEntity;
	}
	
	public JSONObject getJSONEntity() throws Exception {
		JSONObject retVal = null;
		String stringEntity = getStringEntity();
		if (!StringUtils.isNullOrEmpty(stringEntity)) {
			retVal = new JSONObject(stringEntity);
		}
		return retVal;
	}
}