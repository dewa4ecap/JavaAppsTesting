import com.fourelementscapital.db.SchedulerDB;
import com.fourelementscapital.sha.SHA;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Date;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import java.util.Scanner;
import java.io.Console;
import java.util.Properties;

import java.io.BufferedReader;
import java.io.FileReader;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.ResourceBundle;
import org.apache.commons.cli.*;

import com.opencsv.CSVWriter; 
import java.lang.System;
import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.io.IOException;

public class transferUserData {

	private Logger log=LogManager.getLogger(transferUserData.class.getName());

	public static void main(String[] args) {

		/*
		* Initialization
		*/
		Map<String, Object> rtn = new HashMap<String, Object>();

		SchedulerDB sdb=SchedulerDB.getSchedulerDB();

		ArrayList<Integer> userId = new ArrayList<Integer>();
		ArrayList<String> userName = new ArrayList<String>();
		ArrayList<String> userEmail = new ArrayList<String>();

		ArrayList<String> firstName = new ArrayList<String>();
		ArrayList<String> lastName = new ArrayList<String>();
		ArrayList<String> linkedin = new ArrayList<String>();
		ArrayList<String> nationality = new ArrayList<String>();
		ArrayList<String> location = new ArrayList<String>();
		ArrayList<Integer> gender = new ArrayList<Integer>();
		ArrayList<Date> dob = new ArrayList<Date>();
		ArrayList<String> mobile = new ArrayList<String>();
		ArrayList<String> highestEdu = new ArrayList<String>();
		ArrayList<String> major = new ArrayList<String>();
		ArrayList<String> university = new ArrayList<String>();

		ArrayList<String> newPassword = new ArrayList<String>();
		ArrayList<String> newEncryptedPassword = new ArrayList<String>();

		ArrayList<String> existUsers = new ArrayList<String>();

		String query;


		/*
		* Check command line parameters
		*/
		Options options = new Options();

        Option pUser = new Option("u", "username", true, "Username");
        pUser.setRequired(false);
        options.addOption(pUser);

        Option pDB = new Option("d", "database", true, "Choose database (live / beta)");
        pDB.setRequired(true);
        options.addOption(pDB);

        Option pCsv = new Option("c", "createcsv", true, "Generate CSV file");
        pCsv.setRequired(false);
        options.addOption(pCsv);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("please use this format", options);
            System.exit(1);
        }


        // transferUserData [-u username] --live/--beta -c file.csv
        // transferUserData [-u username] -d live -c file.csv

        String paramUser = cmd.getOptionValue("username");
        String paramDB = cmd.getOptionValue("database");
        String paramCsv = cmd.getOptionValue("createcsv");

        if (paramUser==null) { // process all user

        	if  ((!paramDB.equals("beta")) && (!paramDB.equals("live"))) {
        		System.out.println("!!! Invalid database");
	        	formatter.printHelp("please use this format", options);
	        	System.exit(1);
        	}

        	if (paramCsv==null) {
        		System.out.println("!!! To process all user, please include -c param");
	        	formatter.printHelp("please use this format", options);
	        	System.exit(1);	
        	}

        } else { // process only one specific user

        	if  ((!paramDB.equals("beta")) && (!paramDB.equals("live"))) {
        		System.out.println("!!! Invalid database");
	        	formatter.printHelp("please use this format", options);
	        	System.exit(1);
        	}

        	if (paramCsv!= null) {
        		System.out.println("!!! -c param will be ignored since this app will only process one user");
        	}
        }

        if (paramUser != null) {
        	paramUser = paramUser.toLowerCase();
        	paramUser = paramUser.substring(0,1).toUpperCase() + paramUser.substring(1);	
        } else {

        	Scanner scan = new Scanner(System.in);

        	System.out.print("\n !!! Without -u parameter, you are about to process all user data. Are you sure to continue [Y/n] ? ");
        	String iamSure = scan.nextLine();

        	if (!iamSure.toLowerCase().equals("y")) {
        		System.out.println ("\n Operation abort");
        		System.exit(0);
        	}
        }


        /*
		* Read from wiki database user table
		*/
		System.out.print("1. Read from wiki database user table ... ");

		try {

			sdb.connectDB("alwiki");

			query = "SELECT user_id, user_name, user_email FROM user";

			query += (paramUser != null ? " WHERE user_name = '"+paramUser+"'" : "");

			PreparedStatement ps = sdb.connection().prepareStatement(query);
			ResultSet rs=ps.executeQuery();

			if (rs.next()) { // check whether username given is valid or not 
				rs.previous();
			} else {
				System.out.println ("\n !!! Username not found\n");
        		System.exit(0);
			}

			while(rs.next()){

				userId.add(rs.getInt("user_id"));
				userName.add(rs.getString("user_name").toLowerCase());
				userEmail.add(rs.getString("user_email"));

				query = "SELECT up_hometown_country, up_location_country, up_birthday, up_major, up_university, up_highest_edu, up_mobile, up_gender, up_firstname, up_lastname, up_linkedin ";
				query+= "FROM user_profile WHERE up_user_id = ?";

				PreparedStatement ps2 = sdb.connection().prepareStatement(query);
				ps2.setInt(1, rs.getInt("user_id"));
				ResultSet rs2=ps2.executeQuery();

				if (rs2.next()) {

						firstName.add(rs2.getString("up_firstname"));
						lastName.add(rs2.getString("up_lastname"));
						linkedin.add(rs2.getString("up_linkedin"));
						nationality.add(rs2.getString("up_hometown_country"));
						location.add(rs2.getString("up_location_country"));
						dob.add(rs2.getDate("up_birthday"));
						mobile.add(rs2.getString("up_mobile"));
						highestEdu.add(rs2.getString("up_highest_edu"));
						major.add(rs2.getString("up_major"));
						university.add(rs2.getString("up_university"));

						try {
							String mGender = rs2.getString("up_gender").toLowerCase(); 
							gender.add(mGender.equals("male") ? 1 : mGender.equals("female") ? 2 : 0);	
						} catch (Exception e) {
							gender.add(0);
						}

				} else {
					firstName.add(null);
					lastName.add(null);
					linkedin.add(null);
					nationality.add(null);
					location.add(null);
					gender.add(null);
					dob.add(null);
					mobile.add(null);
					highestEdu.add(null);
					major.add(null);
					university.add(null);
				}
				
				rs2.close();
				ps2.close();
			}

			rs.close();
			ps.close();
		} catch (Exception e) {
		} finally {
			try {
				sdb.closeDB();	
			} catch (Exception e) {}
		}

		System.out.println("[ Done ]\n\n");



		/*
		* Create a random password of 14 chars and get Unix crypt(3) hash
		*/
		System.out.print("2. Create a random password of 14 chars and get Unix crypt(3) hash ... ");

		String tempPasswd;

		try {
			for (int a=0; a < userName.size(); a++) {

				tempPasswd = new SHA().getRandomString(14,"");
				
				newPassword.add(tempPasswd);
				newEncryptedPassword.add( new SHA().crypt3Hash(tempPasswd,"") );

			}	
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("[ Done ]\n\n");

		

		/*
		* Save user info with password hash to Alphien user table
		*/
		System.out.print("3. Save user info with password hash to Alphien (beta) user table ... ");

		String userExist = null;
		Integer countExist = 0;

		try {

			ResourceBundle resBundl = ResourceBundle.getBundle("config");

			String subjectEmail =  resBundl.getString("email.subject");

			sdb.connectDB(resBundl.getString((paramDB.equals("live") ? "live.db" : "beta.db")));

			for (int a=0; a < userName.size(); a++) {

				// Check if user already exist on new DB
				boolean isExist = true;
				query = "SELECT userid FROM user WHERE username = ?";
				PreparedStatement initPS = sdb.connection().prepareStatement(query);
				ResultSet initRS = null;
				initPS.setString(1, userName.get(a));
				
				initRS = initPS.executeQuery();
				if(initRS.next()) {
					System.out.println("\n !!! Username " + userName.get(a) + " already exist on new Database");
					existUsers.add(userExist);
					countExist+=1;
				} else {
					isExist = false;
				}
				initRS.close();
				initPS.close();	

				if (isExist == false) {
					try {
						query = "INSERT INTO user (username, password, email, firstName, lastName, linkedin, nationality, location, gender, dob, mobile, highestEdu, major, university ) ";
						query+= "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
						PreparedStatement ps = sdb.connection().prepareStatement(query);
						ps.setString(1, userName.get(a));
						ps.setString(2, newEncryptedPassword.get(a));

						try {
							ps.setString(3, userEmail.get(a));	
						} catch (Exception e) {
							ps.setString(3, null);
						}
						try {
							ps.setString(4, firstName.get(a));	
						} catch (Exception e) {
							ps.setString(4, null);
						}
						try {
							ps.setString(5, lastName.get(a));	
						} catch (Exception e) {
							ps.setString(5, null);
						}
						try {
							ps.setString(6, linkedin.get(a));
						} catch (Exception e) {
							ps.setString(6, null);
						}
						try {
							ps.setString(7, nationality.get(a));
						} catch (Exception e) {
							ps.setString(7, null);
						}
						try {
							ps.setString(8, location.get(a));
						} catch (Exception e) {
							ps.setString(8, null);
						}
						try {
							ps.setInt(9, gender.get(a));
						} catch (Exception e) {
							ps.setInt(9, 0);
						}
						try {
							ps.setDate(10, dob.get(a));
						} catch (Exception e) {
							ps.setDate(10, null);
						}
						try {
							ps.setString(11, mobile.get(a));
						} catch (Exception e) {
							ps.setString(11, null);
						}
						try {
							ps.setString(12, highestEdu.get(a));
						} catch (Exception e) {
							ps.setString(12, null);
						}
						try {
							ps.setString(13, major.get(a));
						} catch (Exception e) {
							ps.setString(13, null);
						}
						try {
							ps.setString(14, university.get(a));
						} catch (Exception e) {
							ps.setString(14, null);
						}
						
						userExist = userName.get(a);
						ps.executeUpdate();

						ps.close();	
					} catch (Exception e) {}
				}
			}

		} catch (Exception e) {
			
			e.printStackTrace();
			
			
		} finally {
			try {
				sdb.closeDB();	
			} catch (Exception e) {}
		}

		System.out.println("[ Done ]\n\n");


		/*
		* Create CSV file / Show to console
		*/
		System.out.println("4. Create CSV file / Show to Console ... ");
		if (paramUser== null) { // Create CSV
	        File file = new File(paramCsv);
	        Scanner sc = new Scanner(System.in); 
	        try { 
	            // create FileWriter object with file as parameter 
	            FileWriter outputfile = new FileWriter(file); 
	  
	            // create CSVWriter with ';' as separator 
	            CSVWriter writer = new CSVWriter(outputfile, ';', 
	                                             CSVWriter.NO_QUOTE_CHARACTER, 
	                                             CSVWriter.DEFAULT_ESCAPE_CHARACTER, 
	                                             CSVWriter.DEFAULT_LINE_END); 
	  
	            // create a List which contains Data 
	            List<String[]> data = new ArrayList<String[]>(); 

	            String row = "username firstname lastname email password";
	            String[] rowdata = row.split(" ");
	            data.add(rowdata);

	            for (int a=0 ; a < userName.size(); a++) {

					if (!existUsers.contains(userName.get(a))) {

						row = userName.get(a) + " " + firstName.get(a) + " " + lastName.get(a) + " " + userEmail.get(a) + " " + newPassword.get(a);
						rowdata = row.split(" ");
						data.add(rowdata);
					}
				}
	  
	            writer.writeAll(data); 
	            writer.close(); 

	            System.out.println(paramCsv + " created !!");
	        } 
	        catch (IOException e) { 
	            e.printStackTrace(); 
	        }

		} else { // Show to Console

			for (int a=0 ; a < userName.size(); a++) {

				if (!existUsers.contains(userName.get(a))) {

					System.out.println("Username : " + userName.get(a));
					System.out.println("First Name : " + firstName.get(a));
					System.out.println("Last Name : " + lastName.get(a));
					System.out.println("Email : " + userEmail.get(a));
					System.out.println("Password : " + newPassword.get(a));
				}
			}
		}

		System.out.println("All process done."); 
	}
}