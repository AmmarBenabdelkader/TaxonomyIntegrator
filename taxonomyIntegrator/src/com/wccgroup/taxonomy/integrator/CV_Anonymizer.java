/**
 * @author abenabdelkader
 *
 * niriParser.java
 * Dec 17, 2015
 */
package com.wccgroup.taxonomy.integrator;

/**
 * @author abenabdelkader
 *
 */
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import java.util.*;
import java.util.Date;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.sql.*;
import java.text.SimpleDateFormat;
public class CV_Anonymizer extends DefaultHandler 
{
	// JDBC driver name and database URL
	static String JDBC_DRIVER = "";
	static String DB_URL = "";

	//  Database credentials
	static String USER = "";
	static String PASS = "";
	static Connection conn = null;
	static Statement stmt = null;
	static int counter;
	static String FullName;
	static String cv_id;
	static String outputFile = "";
	static BufferedWriter writer;
	static int i=1;
	static Random rn = new Random();
	static String[][] actors = new String [100][2];
	static String[][] adresses = new String [12][4];
	
	static public void main(String[] args)  throws Exception {
		String path = "C:\\data\\singapore\\CVs";
		readProperties();
		preapreInputData (path);
		anonymizeCVs (path);
	}
	
	
	
	static public void preapreInputData (String path) throws Exception
	{

		try
		{
			//STEP 2: Register JDBC driver
			Class.forName(JDBC_DRIVER);

			//STEP 3: Open a connection
			conn = DriverManager.getConnection(DB_URL, USER, PASS);
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT FullName, Gender FROM taxonomies.actors order by Gender;");
			int i=0;
			while (rs.next()) {
				actors[i][0] = rs.getString(1);
				actors[i][1] = rs.getString(2);
				i++;
				
			}

			rs = stmt.executeQuery("SELECT number, street, zipcode, country FROM taxonomies.addresses;");
			i=0;
			while (rs.next()) {
				adresses[i][0] = rs.getString(1);
				adresses[i][1] = rs.getString(2);
				adresses[i][2] = rs.getString(3);
				adresses[i][3] = rs.getString(4);
				i++;
				
			}

		stmt.close();
		conn.close();

	}
	catch (SQLException se)
	{
		//Handle errors for JDBC
		se.printStackTrace();
	}
	}
	public static void anonymizeCVs(String path) 
	{
		try
		{
			//path +="\\ManpowerCVs_enriched";
			File folder = new File(path + "\\ManpowerCVs_enriched");
			File[] listOfFiles = folder.listFiles();
			int i;
			String filename = null;
			Date date = new Date();
			int start = 1000;

		    for (i = 0; i < listOfFiles.length; i++) {
			      if (listOfFiles[i].isFile()) {
			  		date = new Date();
			    	filename = listOfFiles[i].getName();
			        System.out.print("\n" + (i) + ": " + filename);

					anonymizeCV(path,filename, (start+i));
					//System.out.println("\tenriched in: " + + (new Date().getTime() - date.getTime())/1000 + "s");
			      }
			    }
		}
		catch (Exception e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		}
	private static void anonymizeCV(String path, String filename, int fl) throws IOException, ClassNotFoundException, SQLException
	{
		File tmpDir = new File(path + "\\ManpowerCVs_enriched\\" + filename);
		if (!tmpDir.exists())
			return;
		
		tmpDir = new File(path + "\\ManpowerCVs_anonymized\\" + fl);
		if (tmpDir.exists()){
		    System.out.println("\talready anonymized");
			return;
		}
		
		FileReader fileReader = new FileReader(path + "\\ManpowerCVs_enriched\\" + filename);
		//System.out.println("_enriched\\sng" + filename.substring(3, filename.length()-4));
        BufferedReader bufferedReader = new BufferedReader(fileReader);
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(path + "\\ManpowerCVs_anonymized\\" + fl)));
		//BufferedWriter writer = new BufferedWriter(new FileWriter(new File(path + "2\\" + filename + ".xml")));
        String line = null;
        String content = null;
        int i = rn.nextInt(100);
        int j = rn.nextInt(12);

        while((line = bufferedReader.readLine()) != null) {
        	//System.out.println(line);
        	//writer.write(line);
        	if (line.contains("<PersonName>")) { 
        		int start = line.indexOf("<PersonName>");
        		int end = line.indexOf("</ContactInfo>");
        		content = "\t<PersonName>\n\t\t\t<GivenName>" + actors[i][0].subSequence(0, actors[i][0].indexOf(" ")) + "</GivenName>\n"
        			+ "\t\t\t<FamilyName>"  + actors[i][0].subSequence(actors[i][0].indexOf(" ")+1,actors[i][0].length()) + "</FamilyName>\n"
        			+ "\t\t\t<FormattedName>"  + actors[i][0] + "</FormattedName>\n"
        			+ "\t\t\t<sex>"  + actors[i][1] + "</sex>\n"
        			+ "\t\t</PersonName>\n\t\t<ContactMethod>\n\t\t\t<PostalAddress type=\"main\">\n"
        				+ "\t\t\t\t<Municipality>" + adresses[j][0] + "</Municipality>\n"
        				+ "\t\t\t\t<Region>" + adresses[j][1] + "</Region>\n"
        				+ "\t\t\t\t<CountryCode>SG</CountryCode>\n"
        				+ "\t\t\t\t<PostalCode>" + adresses[j][2] + "</PostalCode>\n\t\t\t</PostalAddress>\n"
        				+ "\t\t\t<Telephone type=\"home\"><FormattedNumber>+31 30 750 3200</FormattedNumber></Telephone>\n"
        				+ "\t\t\t<Mobile><FormattedNumber>+31 30 750 3200</FormattedNumber></Mobile>\n"
        				+ "\t\t\t<InternetEmailAddress type=\"main\">demo@wcc-group.com</InternetEmailAddress>\n\t\t</ContactMethod>\n"
       				+ "";
        		content = line.substring(0, start) 
        			+ content + 
        			line.substring(end, line.length());
        		//System.out.println(line + "\n\t" + content);
            	writer.write(content);
			} 
        	else
            	writer.write(line);
        		
				}
        bufferedReader.close();
        writer.close();
	}


	/* Loads data generation properties into the system
	 * allows the connection to the database
	 */
	public static void readProperties()
	{
		Properties prop = new Properties();
		InputStream input = null;

		try
		{

			input = new FileInputStream("dataGenerator.properties");

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
