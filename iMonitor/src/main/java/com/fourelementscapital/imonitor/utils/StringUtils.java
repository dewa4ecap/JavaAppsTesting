package com.fourelementscapital.imonitor.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StringUtils {
	private static final Logger log = LogManager.getLogger(StringUtils.class.getName());
	public static String getEmptyIfNull(String value) {
		return (value == null) ? "" : value;
	}

	public static boolean isNullOrEmpty(String value) {
		return value == null || value.length() == 0;
	}

	public static List<String> getListFromCommaDelimatedString(String value) {
		if (!isNullOrEmpty(value)) {
			return Arrays.asList(value.split("\\s*,\\s*"));
		}
		return null;
	}

	public static Map<String, String> parseMap(final String message) {
		final Map<String, String> map = new HashMap<String, String>();
		try {
			if (!StringUtils.isNullOrEmpty(message)) {
				final String[] pairs = message.replace("<EOF>", "").split("~");
				for (String pair : pairs) {
					final String[] kv = pair.split("\\$\\#\\=");
					if (kv.length > 1) {
						map.put(kv[0], kv[1]);
					} else {
						log.info("invalid message: " + kv);
					}
				}
			}
		} catch (Exception ex) {
			log.log(Level.ERROR, ex.getMessage(), message);
		}
		return map;
	}
}
