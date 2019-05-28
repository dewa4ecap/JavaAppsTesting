package com.fourelementscapital.ssoapp;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import com.fourelementscapital.config.Config;
import com.fourelementscapital.restapi.SSOService;


@Path("/")
public class ApiController {
	
	// Generate Token
	@POST
	@Path("action/generatetoken")
	@Produces(MediaType.APPLICATION_JSON)
	public Response generateToken() throws Exception {		
		SSOService access = new SSOService();
		return access.generateToken(Params.getAluserKey(),Params.getAlsidKey());
	}
	
	// Generate Password
	@POST
	@Path("action/generatepassword")
	@Produces(MediaType.APPLICATION_JSON)
	public Response generatePassword(@Context HttpServletRequest httpRequest) throws Exception {		
		SSOService access = new SSOService();
		return access.generateNewPassword(Params.getUserName(),httpRequest);
	}
	
	// Login Process
	@POST
	@Path("action/login")
	@Produces(MediaType.APPLICATION_JSON)
	public Response userLogin(@Context HttpServletRequest httpRequest) throws Exception {		
		SSOService access = new SSOService();
		return access.login(Params.getUserName(),Params.getPassword(),httpRequest);
	}
	
	// Logout Process 
	@POST
	@Path("action/logout")
	@Produces(MediaType.APPLICATION_JSON)
	public Response logoutUser() throws Exception {	
		SSOService access = new SSOService();
		return access.logout(Params.getAlsidKey(),Params.getAluserKey());
	}
	
	// Check Token Validity
	@POST
	@Path("action/checktoken")
	@Produces(MediaType.APPLICATION_JSON)
	public Response checkSSOToken() throws Exception {
		SSOService access = new SSOService();
		return access.isValidSSOToken(Params.getAlsidKey(),Params.getAluserKey());
	}
	
	// Change Password
	@POST
	@Path("action/changepassword")
	@Produces(MediaType.APPLICATION_JSON)
	public Response changePassword(@Context HttpServletRequest httpRequest, @FormParam("new-password") String newPass) throws Exception {
		SSOService access = new SSOService();
		return access.changePassword(Params.getAlsidKey(),Params.getAluserKey(),newPass,httpRequest);
	}
	
	// Check Token Validity 2
	@POST
	@Path("action/checktoken/user")
	@Produces(MediaType.APPLICATION_JSON)
	public Response checkToken(@CookieParam("ALSID") String sid,@CookieParam("ALUSER") String user) throws Exception {
		
		SSOService access = new SSOService();
		return access.isValidSSOToken2(sid,user);
	}
	
}