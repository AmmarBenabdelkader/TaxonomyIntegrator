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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Month;
public class workExperinceExtrator extends DefaultHandler 
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
		//preapreInputData (path);
		computeWorkExp (path);
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
	public static void computeWorkExp(String path) 
	{
		try
		{
			path +="\\ManpowerCVs_parsed";
			File folder = new File(path);
			File[] listOfFiles = folder.listFiles();
			int i;
			String filename = null;
			Date date = new Date();
			int workExpCode = 0;

		    for (i = 0; i < listOfFiles.length && i < 10; i++) {
			      if (listOfFiles[i].isFile()) {
			  		date = new Date();
			    	filename = listOfFiles[i].getName();

			        workExpCode = computeWorkExpCode(path,filename);
			        System.out.print("\n" + (i) + ": " + filename + "\tworkExpCode: " + workExpCode);
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
	private static int computeWorkExpCode(String path, String filename) throws IOException, ClassNotFoundException, SQLException, ParseException
	{
		File tmpDir = new File(path + "\\" + filename);
		if (!tmpDir.exists())
			return 0;
		
		FileReader fileReader = new FileReader(path + "\\" + filename);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line = null;
        Date date1, date2;
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
        int totalMonths = 0;

        while((line = bufferedReader.readLine()) != null) {
        	//System.out.println(line);
        	//writer.write(line);
        	date1=date2=null;
        	if (line.contains("<YearMonth>")) { 
        		int index1= line.indexOf("<YearMonth>");
        		if (index1>0){
        			date1=dateformat.parse(line.substring(index1+11, line.indexOf("</YearMonth>")) + "-01");
        		}
        		line = line.substring(line.indexOf("</YearMonth>")+12, line.length());
        		int index2= line.indexOf("<YearMonth>");
        		if (index2>0){
        			date2=dateformat.parse(line.substring(index2+11, line.indexOf("</YearMonth>")) + "-01");
        		}
        		else {
        			date2 = dateformat.parse("2011-01-01"); 
        			//date2.setMonth((date1.getMonth()) + 3);
        		}
        		totalMonths +=  (((date2.getYear()-date1.getYear())*12) + (date2.getMonth()-date1.getMonth()));   		
            	System.out.print("\n" + date1 + "\t" + date2 + "\t-->\t"  + (((date2.getYear()-date1.getYear())*12) + (date2.getMonth()-date1.getMonth())) +  " months");

        	}
        	if (line.contains("<AnyDate>")) { 
        		int index1= line.indexOf("<AnyDate>");
        		if (index1>0){
        			date1=dateformat.parse(line.substring(index1+11, line.indexOf("</AnyDate>")));
        		}
        		line = line.substring(line.indexOf("</AnyDate>")+12, line.length());
        		int index2= line.indexOf("<AnyDate>");
        		if (index2>0){
        			date2=dateformat.parse(line.substring(index2+11, line.indexOf("</AnyDate>")));
        		}
        		else {
        			date2 = dateformat.parse("2011-01-01"); 
        			//date2.setMonth((date1.getMonth()) + 3);
        		}
        		totalMonths +=  (((date2.getYear()-date1.getYear())*12) + (date2.getMonth()-date1.getMonth()));   		
            	System.out.print("\n" + date1 + "\t" + date2 + "\t-->\t"  +  (((date2.getYear()-date1.getYear())*12) + (date2.getMonth()-date1.getMonth())) +  " months");

        	}
        }
         		
    	System.out.print("\n\t---> Total Months of Experience: " + totalMonths +  " months");
       bufferedReader.close();
       return (totalMonths==0?0:totalMonths==1?1:totalMonths<4?3:totalMonths<7?4:totalMonths<13?5:totalMonths<25?6:totalMonths<49?7:totalMonths<73?8:totalMonths<97?9:totalMonths<121?10:totalMonths>120?11:0);
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
