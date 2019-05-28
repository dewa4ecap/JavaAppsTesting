package com.fourelementscapital.ssoapp;

public class Params {
	
	private static String alsidKey;
	private static String aluserKey;
	private static String userName;
	private static String password;
	
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
	public static String getUserName() {
		return userName;
	}
	public static void setUserName(String userName) {
		Params.userName = userName;
	}
	public static String getPassword() {
		return password;
	}
	public static void setPassword(String password) {
		Params.password = password;
	}
	
}