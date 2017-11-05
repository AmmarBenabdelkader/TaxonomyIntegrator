/**
 * @author abenabdelkader
 *
 * matching_occupations_report.java
 * Sep 4, 2017
 */
package com.wccgroup.taxonomy.integrator;

import java.io.*;
import java.sql.*;
import java.util.*;

/**
 * @author abenabdelkader
 *
 */
public class onetSkills
{
	// JDBC driver name and database URL
	static String JDBC_DRIVER = "";
	static String DB_URL = "";

	//  Database credentials
	static String USER = "";
	static String PASS = "";

	public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException
	{
		readProperties(); //load dataGerator properties
		distributeSkills2();
	}
	public static void distributeSkills2() throws ClassNotFoundException, SQLException, IOException
	{
		Connection conn = null;
		Statement stmt = null;
		Statement stmt2 = null;
		Class.forName(JDBC_DRIVER);
		conn = DriverManager.getConnection(DB_URL, USER, PASS);
		stmt = conn.createStatement();
		stmt2 = conn.createStatement();
		Statement stmt3 = conn.createStatement();
		HashMap<String,String> skills=new HashMap<String,String>(); 


		System.out.println( "O*NET skills re-distribution: ");
		// STEP 1: Exact mapping
		int i = 0;
		//String query= "SELECT onetsoc_code, title FROM onet.occupation_data where onetsoc_code='15-1134.00'";
		//String query= "SELECT onetsoc_code, title FROM onet.occupation_data where onetsoc_code='15-1141.00'";
		String query= "SELECT onetsoc_code, title FROM onet.occupation_data where onetsoc_code='25-1021.00'";
		String jobText = "";
		ResultSet rs = stmt.executeQuery(query);
		for (; rs.next();)
		{
			System.out.println(rs.getString(1) + ": " + rs.getString(2));
			jobText = rs.getString(2);
			query= "SELECT * FROM onet.alternate_titles where onetsoc_code='" + rs.getString(1) + "'";
			ResultSet rs2 = stmt2.executeQuery(query);
			for (; rs2.next();)
			{
				jobText += rs2.getString(2);
			}
			rs2.close();
			query= "SELECT * FROM onet.sample_of_reported_titles where onetsoc_code='" + rs.getString(1) + "'";

			rs2 = stmt2.executeQuery(query);
			for (; rs2.next();)
			{
				System.out.println("\t" + rs2.getString(2));
				jobText += rs2.getString(2);
				query= "SELECT onetsoc_code, t2_example FROM onet.tools_and_technology where onetsoc_code='" + rs.getString(1) + "'";
				ResultSet rs3 = stmt3.executeQuery(query);
				//System.out.println(jobText);
				for (; rs3.next();)
				{
					String words[] = rs3.getString(2).split(" ");
					for (i=0; i<words.length; i++) {
						//System.out.println("\t"+words[i] + i + "/"+words.length);
						if (words[i].length()>1 && rs2.getString(2).contains(words[i]))
							System.out.println("\t\t"+rs3.getString(2));
					}  

				}
			} 
			rs2.close();

		}
		stmt.close();
		conn.close();

	}

	public static void distributeSkills() throws ClassNotFoundException, SQLException, IOException
	{
		Connection conn = null;
		Statement stmt = null;
		Statement stmt2 = null;
		Class.forName(JDBC_DRIVER);
		conn = DriverManager.getConnection(DB_URL, USER, PASS);
		stmt = conn.createStatement();
		stmt2 = conn.createStatement();
		Statement stmt3 = conn.createStatement();
		HashMap<String,String> skills=new HashMap<String,String>(); 


		System.out.println( "O*NET skills re-distribution: ");
		// STEP 1: Exact mapping
		int i = 0;
		String query= "SELECT onetsoc_code, title FROM onet.occupation_data";
		ResultSet rs = stmt.executeQuery(query);
		for (; rs.next();)
		{
			System.out.println(rs.getString(1) + ": " + rs.getString(2));
			//query= "SELECT onetsoc_code, t2_example FROM onet.tools_and_technology where t2_type='Tools' and onetsoc_code='" + rs.getString(1) + "'";
			query= "SELECT onetsoc_code, t2_example FROM onet.tools_and_technology where onetsoc_code='" + rs.getString(1) + "'";
			ResultSet rs2 = stmt2.executeQuery(query);
			skills=new HashMap<String,String>();
			for (; rs2.next();)
			{
				//System.out.println("\t"+rs2.getString(2));
				skills.put(rs2.getString(2), rs2.getString(2));
			}
			rs2.close();
			query= "SELECT * FROM onet.sample_of_reported_titles where onetsoc_code='" + rs.getString(1) + "'";
			rs2 = stmt2.executeQuery(query);
			int j=1;


			for (; rs2.next();)
			{
				System.out.println("\t" + rs2.getString(2));
				String words[] = rs2.getString(2).split(" ");
				for (i=0; i<words.length; i++) {
					//System.out.println("\t"+words[i] + i + "/"+words.length);
					for(Map.Entry skill:skills.entrySet()) {
						if (words[i].length()>1 && skill.getValue().toString().contains(words[i]))
							System.out.println("\t\t"+skill.getValue());
					}  

				}
			}
		}
		stmt.close();
		conn.close();

	}

	public static void readProperties()
	{
		Properties prop = new Properties();
		InputStream input = null;

		try
		{

			input = new FileInputStream("ssoc.properties");

			// load a properties file
			prop.load(input);
			JDBC_DRIVER = prop.getProperty("driver");
			DB_URL = prop.getProperty("url");
			USER = prop.getProperty("user");
			PASS = prop.getProperty("pass");

		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
	}


}
