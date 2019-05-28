/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fe.util;

import java.util.Map;
import java.util.Vector;

import com.fourelementscapital.fileutils.StringPlaceHolder;

public class StringPlaceHolder4ChildStrategy {
	
	public static final String PATTERN = "\\{\\{(.*?)\\}\\}";
	public static final String PATTERN_ATTR = "\\\"\\{\\{(.*?)\\}\\}\\\"";
	
	public static Vector<String> getAttributePH(final String template) {
    	return StringPlaceHolder.getAttributePH(template, StringPlaceHolder4ChildStrategy.PATTERN_ATTR);
    }

	public static Vector<String> getElementPH(final String template) {
		return StringPlaceHolder.getElementPH(template, StringPlaceHolder4ChildStrategy.PATTERN);
    }    
    
    public static String parse(final String template, final Map values){ 
    	return StringPlaceHolder.parse(template, values, StringPlaceHolder4ChildStrategy.PATTERN);
    }

}



