package com.fourelementscapital.userapp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fourelementscapital.config.Config;
import com.fourelementscapital.restapi.UserService;

import edu.emory.mathcs.backport.java.util.Arrays;


@Path("/")
public class ApiController {
	
	// Check Valid Group
	/*@POST
	@Path("action/check/ldap/group")
	@Produces(MediaType.APPLICATION_JSON)
	public Response checkValidLDAPGroup() throws Exception {		
		UserService operation = new UserService();
		return operation.isLDAPGroupValid(Params.getAluserKey(),Params.getAlsidKey(),Config.getString("authorized_LDAP_group"));
	}*/
	
	
	// Change Detail User Profile
	@POST
	@Path("v1.2/action/edit/profile")
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
	@Path("v1.2/action/get/profile")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDetailProfile() throws Exception {
		UserService operation = new UserService();

		return operation.getDetailProfile(
				Params.getAluserKey(),
				Params.getAlsidKey());
	}
	
	// Register New User
	@POST
	@Path("v1.2/action/register/user")
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
	@Path("v1.2.1/action/insert/user")
	@Produces(MediaType.APPLICATION_JSON)
	public Response userInsertToDB() throws Exception {
		
		List<String> ldapList = new ArrayList<String>();		
		
		try {

            String rawList = Config.getString("authorized_LDAP_group");
            
            ldapList = Arrays.asList(rawList.split(","));
            
            for (int x=0; x < ldapList.size(); x++) {
            	ldapList.set(x, ldapList.get(x).trim());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
		
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
				ldapList
				);
	}
	
	// Insert New User
	@POST
	@Path("v1.2/action/insert/user")
	@Produces(MediaType.APPLICATION_JSON)
	public Response userInsertToDBFromInvite() throws Exception {
		UserService access = new UserService();
		
		return access.insertUserToDBFromInvite(
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
				Params.getUniversityKey()
				);
	}
	
	
	// Signup on alphachat only
	@POST
	@Path("v1.2/action/register/user-alphachat-only")
	@Produces(MediaType.APPLICATION_JSON)
	public Response signUpAlphachatOnly() throws Exception {
		UserService operation = new UserService();
		return operation.userRegisterChatOnly(
				Params.getAlphachatUsername(),
				Params.getAlphachatPassword());
	}
	
	// Accept essential cookies
	@POST
	@Path("v1.2/action/accept/essential/cookies")
	@Produces(MediaType.APPLICATION_JSON)
	public Response acceptEssentialCookies() throws Exception {
		UserService operation = new UserService();
		return operation.setEssentialCookies(
				Params.getAluserKey(),
				Params.getAlsidKey(),1);
	}
	
	// Reject essential cookies
	@POST
	@Path("v1.2/action/reject/essential/cookies")
	@Produces(MediaType.APPLICATION_JSON)
	public Response rejectEssentialCookies() throws Exception {
		UserService operation = new UserService();
		return operation.setEssentialCookies(
				Params.getAluserKey(),
				Params.getAlsidKey(),0);
	}
	
}