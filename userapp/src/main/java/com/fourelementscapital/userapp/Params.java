package com.fourelementscapital.userapp;

import javax.ws.rs.FormParam;

public class Params {
	
	private static String aluserKey; 
	private static String alsidKey; 
	private static String location_countryKey;
	private static String hometown_countryKey;
	private static String birthdayKey;
	private static String majorKey;
	private static String universityKey;
	private static String highest_eduKey;
	private static String mobileKey;
	private static String genderKey;
	private static String firstnameKey;
	private static String lastnameKey;
	private static String linkedinKey;
	private static String usernameKey;
	private static String emailKey;
	private static String assetclassKey;
	
	private static String alphachatUsername;
	private static String alphachatPassword;
	
	private static String userPassword;
	private static String ldapGroup;
	
	public static void resetParams() {
		Params.ldapGroup = null;
		Params.userPassword = null;
		Params.alphachatUsername = null;
		Params.alphachatPassword = null;
		Params.aluserKey = null;
		Params.alsidKey = null;
		Params.location_countryKey = null;
		Params.hometown_countryKey = null;
		Params.birthdayKey = "0000-00-00";
		Params.majorKey = null;
		Params.universityKey = null;
		Params.highest_eduKey = null;
		Params.mobileKey = null;
		Params.genderKey = "0";
		Params.firstnameKey = null;
		Params.lastnameKey = null;
		Params.linkedinKey = null;
		Params.usernameKey = null;
		Params.emailKey = null;
		Params.assetclassKey = null;
	}
	
	public static String getLdapGroup() {
		return ldapGroup;
	}
	public static void setLdapGroup(String ldapGroup) {
		Params.ldapGroup = ldapGroup;
	}
	public static String getUserPassword() {
		return userPassword;
	}
	public static void setUserPassword(String userPassword) {
		Params.userPassword = userPassword;
	}
	public static String getAlphachatUsername() {
		return alphachatUsername;
	}
	public static void setAlphachatUsername(String alphachatUsername) {
		Params.alphachatUsername = alphachatUsername;
	}
	public static String getAlphachatPassword() {
		return alphachatPassword;
	}
	public static void setAlphachatPassword(String alphachatPassword) {
		Params.alphachatPassword = alphachatPassword;
	}
	public static String getAluserKey() {
		return aluserKey;
	}
	public static void setAluserKey(String aluserKey) {
		Params.aluserKey = aluserKey;
	}
	public static String getAlsidKey() {
		return alsidKey;
	}
	public static void setAlsidKey(String alsidKey) {
		Params.alsidKey = alsidKey;
	}
	public static String getLocation_countryKey() {
		return location_countryKey;
	}
	public static void setLocation_countryKey(String location_countryKey) {
		Params.location_countryKey = location_countryKey;
	}
	public static String getHometown_countryKey() {
		return hometown_countryKey;
	}
	public static void setHometown_countryKey(String hometown_countryKey) {
		Params.hometown_countryKey = hometown_countryKey;
	}
	public static String getBirthdayKey() {
		return birthdayKey;
	}
	public static void setBirthdayKey(String birthdayKey) {
		Params.birthdayKey = birthdayKey;
	}
	public static String getMajorKey() {
		return majorKey;
	}
	public static void setMajorKey(String majorKey) {
		Params.majorKey = majorKey;
	}
	public static String getUniversityKey() {
		return universityKey;
	}
	public static void setUniversityKey(String universityKey) {
		Params.universityKey = universityKey;
	}
	public static String getHighest_eduKey() {
		return highest_eduKey;
	}
	public static void setHighest_eduKey(String highest_eduKey) {
		Params.highest_eduKey = highest_eduKey;
	}
	public static String getMobileKey() {
		return mobileKey;
	}
	public static void setMobileKey(String mobileKey) {
		Params.mobileKey = mobileKey;
	}
	public static String getGenderKey() {
		return genderKey;
	}
	public static void setGenderKey(String genderKey) {
		Params.genderKey = genderKey;
	}
	public static String getFirstnameKey() {
		return firstnameKey;
	}
	public static void setFirstnameKey(String firstnameKey) {
		Params.firstnameKey = firstnameKey;
	}
	public static String getLastnameKey() {
		return lastnameKey;
	}
	public static void setLastnameKey(String lastnameKey) {
		Params.lastnameKey = lastnameKey;
	}
	public static String getLinkedinKey() {
		return linkedinKey;
	}
	public static void setLinkedinKey(String linkedinKey) {
		Params.linkedinKey = linkedinKey;
	}
	public static String getUsernameKey() {
		return usernameKey;
	}
	public static void setUsernameKey(String usernameKey) {
		Params.usernameKey = usernameKey;
	}
	public static String getEmailKey() {
		return emailKey;
	}
	public static void setEmailKey(String emailKey) {
		Params.emailKey = emailKey;
	}
	public static String getAssetclassKey() {
		return assetclassKey;
	}
	public static void setAssetclassKey(String assetclassKey) {
		Params.assetclassKey = assetclassKey;
	}
}