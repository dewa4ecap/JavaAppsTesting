package com.fourelementscapital.imonitor.utils;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.fourelementscapital.imonitor.config.PhoneCallConfiguration;
import com.fourelementscapital.imonitor.entities.Alarm;


/*Notes from Gama :
Lambda expression (-> sign) only exist on Java 8 and above.*/

public class PhoneCallUtils {

	private final static String MAIN_API_URL = "https://secure.hoiio.com/open";
	private final static String VOICE_CALL_URL = "/ivr/start/dial";
	private final static String CALLER_ID = "private";
	private final static int MAX_DURATION_IN_SECONDS = 60;
	
	private static final Logger log = LogManager.getLogger(PhoneCallUtils.class.getName());
	
	public static void makePhoneCall(Alarm alarm) {
		try {
			validateAlarm(alarm);
			List<String> phoneNumbers = StringUtils.getListFromCommaDelimatedString(alarm.getPhoneNumbers());
			
			// Java 8
			/*phoneNumbers.forEach(x -> {*/
			
			// Modified to Java 7
			for (String x : phoneNumbers) {
				makePhoneCallHelper(alarm.getSubject(), x);
			}
				
			//});
			
		} catch (Exception e) {
			log.log(Level.ERROR, e);
		}
	}

	private static void makePhoneCallHelper(String message, String phoneNumber) {
		HttpGet get = null;
		HttpRetVal httpRetVal = null;
		JSONObject jsonResponse = null;
		List<NameValuePair> params = null;
		try {
			params = getParams(message, phoneNumber);
			params.add(new BasicNameValuePair("caller_id", CALLER_ID));
			params.add(new BasicNameValuePair("max_duration", String.valueOf(MAX_DURATION_IN_SECONDS)));
			get = createHttpGet(params, VOICE_CALL_URL);
			httpRetVal = executeHttpCall(get);
			if (!httpRetVal.getStatus().equals(HttpRetVal.HttpRetValStatus.SUCCESS)) {
				log.log(Level.DEBUG, String.valueOf(httpRetVal));
			} else {
				jsonResponse = httpRetVal.getJSONEntity();
				parseResponse(jsonResponse);
			}
		} catch (Exception e) {
			log.log(Level.ERROR, e);
		}
	}

	private static void parseResponse(JSONObject jsonResponse) {
		//String status = JSONUtils.getString(jsonResponse, "status", "");
		//TODO: Add code to handle errors

	}

	private static void validateAlarm(Alarm alarm) throws Exception {
		if (alarm == null) {
			throw new Exception("alarm is null");
		} else if (StringUtils.isNullOrEmpty(alarm.getSubject())) {
			throw new Exception("phone call text is null");
		} else if (StringUtils.isNullOrEmpty(alarm.getPhoneNumbers())) {
			throw new Exception("phone call text is null");
		}
	}

	private static HttpGet createHttpGet(List<NameValuePair> params, String requestPath) throws Exception {
		HttpGet get = null;
		String url = null;
		String paramString = null;

		get = new HttpGet();
		get.setHeader("Content-Type", "application/json; charset=utf-8");

		if ((params != null) && (params.size() > 0)) {
			paramString = '?' + URLEncodedUtils.format(params, "UTF-8");
		} else {
			paramString = "";
		}

		url = MAIN_API_URL + requestPath + paramString;
		get.setURI(new URI(url));

		return get;
	}

	private static List<NameValuePair> getParams(String message, String phoneNumber) {
		List<NameValuePair> retVal = null;

		retVal = new ArrayList<NameValuePair>();
		retVal.add(new BasicNameValuePair("dest", phoneNumber));
		retVal.add(new BasicNameValuePair("msg", String.valueOf(message)));
		retVal.add(new BasicNameValuePair("access_token", PhoneCallConfiguration.getAccessToken()));
		retVal.add(new BasicNameValuePair("app_id", PhoneCallConfiguration.getAppId()));

		return retVal;
	}

	public static HttpRetVal executeHttpCall(HttpUriRequest request) throws Exception {
		//http://hc.apache.org/httpclient-3.x/performance.html
		HttpRetVal httpRetVal = null;
		CloseableHttpClient http = null;
		HttpResponse resp = null;
		try {
			http = HttpClients.createDefault();
			resp = http.execute(request);
			httpRetVal = new HttpRetVal(resp);
			httpRetVal.setStringEntity();
		} catch (Exception e) {
			throw e;
		} finally {
			closeHttpClient(http);
		}
		return httpRetVal;
	}

	public static void closeHttpClient(CloseableHttpClient http) {
		try {
			if (http != null) {
				http.close();
			}
		} catch (Exception ignore) {
			log.log(Level.ERROR, ignore);
		}
	}
}
