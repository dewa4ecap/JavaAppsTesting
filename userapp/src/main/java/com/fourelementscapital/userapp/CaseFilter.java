package com.fourelementscapital.userapp;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.server.ContainerRequest;

import com.sun.jersey.core.header.MediaTypes;

@Provider
public class CaseFilter implements ContainerRequestFilter {
	
	public void filter(ContainerRequestContext requestContext) throws IOException {
		
		if (requestContext instanceof ContainerRequest)
        {
            ContainerRequest request = (ContainerRequest) requestContext;

            if ( requestContext.hasEntity() && MediaTypes.typeEquals(MediaType.APPLICATION_FORM_URLENCODED_TYPE,request.getMediaType()))
            {
                request.bufferEntity();
                Form f = request.readEntity(Form.class);
                
                Params.resetParams();
                
                for (String key : f.asMap().keySet()) {
                	if (key.toLowerCase().equals("aluser")) {
                		Params.setAluserKey(f.asMap().get(key).get(0));
                	}
                	if (key.toLowerCase().equals("alsid")) {
                		Params.setAlsidKey(f.asMap().get(key).get(0));
                	}
                	if (key.toLowerCase().equals("location_country")) {
                		Params.setLocation_countryKey(f.asMap().get(key).get(0));
                	}
                	if (key.toLowerCase().equals("hometown_country")) {
                		Params.setHometown_countryKey(f.asMap().get(key).get(0));
                	}
                	if (key.toLowerCase().equals("birthday")) {
                		Params.setBirthdayKey(f.asMap().get(key).get(0));
                	}
                	if (key.toLowerCase().equals("major")) {
                		Params.setMajorKey(f.asMap().get(key).get(0));
                	}
                	if (key.toLowerCase().equals("university")) {
                		Params.setUniversityKey(f.asMap().get(key).get(0));
                	}
                	if (key.toLowerCase().equals("highest_edu")) {
                		Params.setHighest_eduKey(f.asMap().get(key).get(0));
                	}
                	if (key.toLowerCase().equals("mobile")) {
                		Params.setMobileKey(f.asMap().get(key).get(0));
                	}
                	if (key.toLowerCase().equals("gender")) {
                		Params.setGenderKey(f.asMap().get(key).get(0));
                	}
                	if (key.toLowerCase().equals("firstname")) {
                		Params.setFirstnameKey(f.asMap().get(key).get(0));
                	}
                	if (key.toLowerCase().equals("lastname")) {
                		Params.setLastnameKey(f.asMap().get(key).get(0));
                	}
                	if (key.toLowerCase().equals("linkedin")) {
                		Params.setLinkedinKey(f.asMap().get(key).get(0));
                	}
                	if (key.toLowerCase().equals("username")) {
                		Params.setUsernameKey(f.asMap().get(key).get(0));
                	}
                	if (key.toLowerCase().equals("email")) {
                		Params.setEmailKey(f.asMap().get(key).get(0));
                	}
                	if (key.toLowerCase().equals("assetclass")) {
                		Params.setAssetclassKey(f.asMap().get(key).get(0));
                	}
                	if (key.toLowerCase().equals("chat-username")) {
                		Params.setAlphachatUsername(f.asMap().get(key).get(0));
                	}
                	if (key.toLowerCase().equals("chat-password")) {
                		Params.setAlphachatPassword(f.asMap().get(key).get(0));
                	}
                	if (key.toLowerCase().equals("password")) {
                		Params.setUserPassword(f.asMap().get(key).get(0));
                	}
                	if (key.toLowerCase().equals("ldapgroup")) {
                		Params.setLdapGroup(f.asMap().get(key).get(0));
                	}
                }
                
            }
        }
		
	}

}