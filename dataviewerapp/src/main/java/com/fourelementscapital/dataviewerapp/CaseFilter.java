package com.fourelementscapital.dataviewerapp;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
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
                
                for (String key : f.asMap().keySet()) {
                	if (key.toLowerCase().equals("alsid")) {
                		Params.setAlsidKey(f.asMap().get(key).get(0));
                	}
                	
                	if (key.toLowerCase().equals("aluser")) {
                		Params.setAluserKey(f.asMap().get(key).get(0));
                	}
                	
                }
                
            }
        }
		
	}

}