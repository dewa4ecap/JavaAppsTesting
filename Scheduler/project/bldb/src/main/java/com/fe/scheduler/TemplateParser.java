/******************************************************************************
*
* Copyright: Intellectual Property of Four Elements Capital Pte Ltd, Singapore.
* All rights reserved.
*
******************************************************************************/

package com.fe.scheduler;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Template parser
 */	
public class TemplateParser {

    private String subject=null;

    private String template=null; 
    private Map data=null;

    /**
     * Constructor
     * @param template template
     * @param data data
     */	    
    public TemplateParser(String template, Map data){
 	   this.template=template;
 	   this.data=data;
    }
    

    private Logger log = LogManager.getLogger(TemplateParser.class.getName());

    /**
     * Parse template, set subject
     * @return message
     */	
    public String parseTemplate(){


             String message=parseNewSubject(this.template);
             
             message=replaceNewValues(message, this.data);
             this.subject=replaceNewValues(this.subject, this.data);


            return message;
    }

    /**
     * Get subject
     * @return subject
     */	    
    public String getSubject(){
            return this.subject;
    }

    /**
     * Replace new values
     * @param template template
     * @param values values
     * @return template with new values
     */	    
    protected  String replaceNewValues(final String template,
                final Map<String, String> values){

            if(template!=null){

                final StringBuffer sb = new StringBuffer();
                final Pattern pattern =      Pattern.compile("\\[\\[(.*?)\\]\\]", Pattern.DOTALL);
                final Matcher matcher = pattern.matcher(template);
                while(matcher.find()){
                    final String key = matcher.group(1);
                    final Object replacement = values.get(key);
                    if(replacement == null){
                            matcher.appendReplacement(sb, "");
                    }else{
                        //matcher.appendReplacement(sb, replacement.toString());
                        matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement.toString()));
                    }
                   // log.debug("key:"+key+" replacement:"+replacement);
                }
                //log.debug("~~~~~~~~~~~~ exiting replaceNewValues()1");
                matcher.appendTail(sb);
                //log.debug("~~~~~~~~~~~~ exiting replaceNewValues()2");
                return sb.toString();
            }else{
                    return "";
            }

    }




    /*
    protected String  parseBody(final String template){
            if(template!=null){
                     Pattern p1 = Pattern.compile("\\{\\{body\\}\\}(.*?)\\{\\{/body\\}\\}",Pattern.DOTALL);
                     final Matcher matcher = p1.matcher(template);
                     final StringBuffer sb = new StringBuffer();
                     String ke1y="";
                     if(matcher.find()){
                                ke1y = matcher.group(1);
                                matcher.appendReplacement(sb, "");
                     }
                     matcher.appendTail(sb);


                     if(ke1y!=null && !ke1y.equals("")){
                             //ke1y = ke1y.toString().replaceAll("\\<.*?\\>", "");
                             //ke1y = ke1y.replaceAll("\t|\n|\r|\f", "");
                             //ke1y=  ke1y.replace("&nbsp;", " ");
                             if(!ke1y.trim().equals("")){
                                     this.body=ke1y;
                             }
                     }
                     return sb.toString();
            }else{
                     return "";
            }
    }
	*/

    /**
     * Parse new subject
     * @param template template
     * @return template with new subject
     */	    
    protected String  parseNewSubject(final String template){
            if(template!=null){
                     Pattern p1 = Pattern.compile("\\{\\{subject\\}\\}(.*?)\\{\\{/subject\\}\\}",Pattern.DOTALL); 
                     
                     final Matcher matcher = p1.matcher(template);
                     final StringBuffer sb = new StringBuffer();
                     String ke1y="";
                     if(matcher.find()){
                                ke1y = matcher.group(1);
                                matcher.appendReplacement(sb, "");
                     }
                     matcher.appendTail(sb);
                     if(ke1y!=null && !ke1y.equals("")){
                             ke1y = ke1y.toString().replaceAll("\\<.*?\\>", "");
                             ke1y = ke1y.replaceAll("\t|\n|\r|\f", "");
                             ke1y=  ke1y.replace("&nbsp;", " ");
                             if(!ke1y.trim().equals("")){
                                     this.subject=ke1y;
                             }
                     }

                     return sb.toString();
            }else{
                    return "";
            }
    }

}



