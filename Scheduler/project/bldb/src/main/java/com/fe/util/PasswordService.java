/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fe.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import sun.misc.BASE64Encoder;

/**
 * Password encrypting
 */
public class PasswordService {
	
	/**
	 * Private constructor
	 */		
	private PasswordService()
	{
	}
	
	/**
	 * Encrypt text
	 * @param plaintext plain text
	 * @return encrypted text
	 * @throws Exception
	 */		
	public static synchronized String encrypt(String plaintext) throws Exception {
		MessageDigest md = null;
		try		{
			md = MessageDigest.getInstance("SHA"); //step 2
		}catch(NoSuchAlgorithmException e)		{
			throw e;
		}
		
		try	{
			md.update(plaintext.getBytes("UTF-8")); //step 3
		}catch(UnsupportedEncodingException e)	{
			throw e;
		}
		
		byte raw[] = md.digest(); //step 4
		String hash = (new BASE64Encoder()).encode(raw); //step 5
		return hash; //step 6
	}
	
}


