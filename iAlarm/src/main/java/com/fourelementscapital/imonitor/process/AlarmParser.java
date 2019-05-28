package com.fourelementscapital.imonitor.process;

import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fourelementscapital.imonitor.entities.Alarm;
import com.fourelementscapital.imonitor.utils.StringUtils;

/*Notes from Gama :
Lambda expression (-> sign) only exist on Java 8 and above.*/

public class AlarmParser {
	private static final Logger log = LogManager.getLogger(AlarmParser.class.getName());
	public static Alarm createAlarm(String socketMessage, AsynchronousSocketChannel session) {
		Alarm alarm = new Alarm();
		try {
			Map<String, String> mapItems = StringUtils.parseMap(socketMessage);
			alarm.setIpAddress(((InetSocketAddress) session.getRemoteAddress()).getHostString());

			//Java 8 
			/*mapItems.forEach((key, value) -> {*/
			
			// Modified to Java 7
			for(Map.Entry<String,String> entry : mapItems.entrySet()) {
				
				String key = entry.getKey();
				String value = entry.getValue();

				switch (key.toLowerCase()) {
				case "theme":
					alarm.setTheme(value);
					break;
				case "subject":
					alarm.setSubject(value);
					break;
				case "body":
					alarm.setBody(value);
					break;
				case "recipients":
					alarm.setEmailRecipients(value);
					break;
				case "emailcc":
					alarm.setEmailRecipientsCC(value);
					break;
				case "emailbcc":
					alarm.setEmailRecipientsBCC(value);
					break;
				case "emailattachments":
					alarm.setPathAttachments(value);
					break;
				case "emailimages":
					alarm.setPathImages(value);
					break;
				case "emailreplyto":
					alarm.setEmailReplyTo(value);
					break;
				case "sayit":
					alarm.setSayIt(Boolean.parseBoolean(value));
					break;
				case "emailit":
					alarm.setEmailIt(Boolean.parseBoolean(value));
					break;
				case "phonecall":
					alarm.setPhoneCall(Boolean.parseBoolean(value));
					break;
				case "numbers":
					alarm.setPhoneNumbers(value);
					break;
				}
			}
			
			//});
			
		} catch (Exception e) {
			log.log(Level.ERROR, e);
		}
		return alarm;

	}
}
