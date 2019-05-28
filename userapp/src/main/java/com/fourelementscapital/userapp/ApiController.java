package com.fourelementscapital.userapp;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.PathParam;

import com.fourelementscapital.config.Config;
import com.fourelementscapital.restapi.SSOService;
import com.fourelementscapital.restapi.UserService;


@Path("/")
public class ApiController {
	
	// Check Valid Group
	@POST
	@Path("action/check/ldap/group")
	@Produces(MediaType.APPLICATION_JSON)
	public Response checkValidLDAPGroup() throws Exception {		
		UserService operation = new UserService();
		return operation.isLDAPGroupValid(Params.getAluserKey(),Params.getAlsidKey(),Config.getString("authorized_LDAP_group"));
	}
	
	
	// Change Detail User Profile
	@POST
	@Path("action/edit/profile")
	@Produces(MediaType.APPLICATION_JSON)
	public Response editDetailProfile() throws Exception {
		UserService operation = new UserService();
		return operation.editDetailProfile(
				Params.getAluserKey(),
				Params.getAlsidKey(),
				Params.getLocation_countryKey(),
				Params.getHometown_countryKey(),
				Params.getBirthdayKey(),
				Params.getMajorKey(),
				Params.getUniversityKey(),
				Params.getHighest_eduKey(),
				Params.getMobileKey(),
				Params.getGenderKey(),
				Params.getFirstnameKey(),
				Params.getLastnameKey(),
				Params.getLinkedinKey());
	}
	
	// Get Detail User Profile
	@POST
	@Path("action/get/profile")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDetailProfile() throws Exception {
		UserService operation = new UserService();

		/*System.out.println("aluser : " + Params.getAluserKey());
		System.out.println("alsid : " + Params.getAlsidKey());*/
		return operation.getDetailProfile(
				Params.getAluserKey(),
				Params.getAlsidKey());
	}
	
	// Register New User
	@POST
	@Path("action/register/user")
	@Produces(MediaType.APPLICATION_JSON)
	public Response userRegister() throws Exception {
		UserService access = new UserService();
		return access.userRegister(
				Params.getUsernameKey(),
				Params.getFirstnameKey(),
				Params.getLastnameKey(),
				Params.getEmailKey(),
				Params.getLinkedinKey(),
				Params.getAssetclassKey());
	}
	
	// Insert New User
	@POST
	@Path("action/insert/user")
	@Produces(MediaType.APPLICATION_JSON)
	public Response userInsertToDB() throws Exception {
		UserService access = new UserService();
		
		return access.insertUserToDB(
				Params.getUsernameKey(),
				Params.getUserPassword(),
				Params.getEmailKey(),
				Params.getLdapGroup(),
				Params.getFirstnameKey(),
				Params.getLastnameKey(),
				Params.getLinkedinKey(),
				Params.getHometown_countryKey(),
				Params.getLocation_countryKey(),
				Integer.valueOf(Params.getGenderKey()),
				Params.getBirthdayKey(),
				Params.getMobileKey(),
				Params.getHighest_eduKey(),
				Params.getMajorKey(),
				Params.getUniversityKey(),
				Params.getAluserKey(),
				Params.getAlsidKey(),
				Config.getString("authorized_LDAP_group")
				);
	}
	
	
	// Signup on alphachat only
	@POST
	@Path("action/register/user-alphachat-only")
	@Produces(MediaType.APPLICATION_JSON)
	public Response signUpAlphachatOnly() throws Exception {
		UserService operation = new UserService();
		return operation.userRegisterChatOnly(
				Params.getAlphachatUsername(),
				Params.getAlphachatPassword());
	}
	
	// Accept essential cookies
	@POST
	@Path("action/accept/essential/cookies")
	@Produces(MediaType.APPLICATION_JSON)
	public Response acceptEssentialCookies() throws Exception {
		UserService operation = new UserService();
		return operation.setEssentialCookies(
				Params.getAluserKey(),
				Params.getAlsidKey(),1);
	}
	
	// Reject essential cookies
	@POST
	@Path("action/reject/essential/cookies")
	@Produces(MediaType.APPLICATION_JSON)
	public Response rejectEssentialCookies() throws Exception {
		UserService operation = new UserService();
		return operation.setEssentialCookies(
				Params.getAluserKey(),
				Params.getAlsidKey(),0);
	}
	
}