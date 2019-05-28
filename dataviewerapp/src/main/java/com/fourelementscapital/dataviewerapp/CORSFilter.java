package com.fourelementscapital.dataviewerapp;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

import javax.ws.rs.core.Context;
import javax.servlet.http.HttpServletRequest;
import java.net.URL;

@Provider
public class CORSFilter implements ContainerResponseFilter {
	
	@Context
	private HttpServletRequest servletRequest;

   public void filter(final ContainerRequestContext requestContext,
                      final ContainerResponseContext cres) throws IOException {
	  
	  String host = servletRequest.getHeader("Origin");
	  cres.getHeaders().add("Access-Control-Allow-Origin", host);
      cres.getHeaders().add("Access-Control-Allow-Credentials", "true");
      cres.getHeaders().add("Access-Control-Allow-Headers", "origin, content-type, accept, Authorization, Access-Control-Allow-Origin, X-FORWARDED-FOR");
      cres.getHeaders().add("Access-Control-Allow-Methods", "GET, POST");
      cres.getHeaders().add("Access-Control-Max-Age", "1209600");
   }

}