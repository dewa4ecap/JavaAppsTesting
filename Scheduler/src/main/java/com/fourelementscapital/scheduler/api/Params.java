package com.fourelementscapital.scheduler.api;

public class Params {
	
	private static String scriptKey;
	private static String alsidKey;
	private static String aluserKey;
	
	public static String getScriptKey() {
		return scriptKey;
	}
	public static void setScriptKey(String scriptKey) {
		Params.scriptKey = scriptKey;
	}
	public static String getAlsidKey() {
		return alsidKey;
	}
	public static void setAlsidKey(String alsidKey) {
		Params.alsidKey = alsidKey;
	}
	public static String getAluserKey() {
		return aluserKey;
	}
	public static void setAluserKey(String aluserKey) {
		Params.aluserKey = aluserKey;
	}
	
}