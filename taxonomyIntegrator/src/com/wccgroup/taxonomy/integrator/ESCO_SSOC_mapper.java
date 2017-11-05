package com.wccgroup.taxonomy.integrator;
/**
 * @author abenabdelkader
 *
 * taxonomy.java
 * Nov 8, 2016
 */


import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.logging.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.sql.*;
import java.util.*;
import java.util.Date;
/**
 * @author abenabdelkader
 *
 */


public class ESCO_SSOC_mapper {
	static String JDBC_DRIVER = "";
	static String DB_URL = "";
	static String USER = "";
	static String PASS = "";

	public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException {

		Date date = new Date();
		JDBC_DRIVER = "com.mysql.jdbc.Driver";
		DB_URL = "jdbc:mysql://localhost/ssoc_temp?useUnicode=true&characterEncoding=utf-8";
		USER = "root";
		PASS = "";

		
		essco_ssoc_mapper();
		

		System.out.println("\nTotal duration: " + (new Date().getTime() - date.getTime())/1000 + "s");



	}


		public static void essco_ssoc_mapper() throws SQLException, IOException
	{
		try
		{
			Class.forName(JDBC_DRIVER);
			Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
			Statement stmt1 = conn.createStatement();
			Statement stmt2 = conn.createStatement();
			ResultSet rs1, rs2;
			String query;
			HashMap<String,Integer> counter6=new HashMap<String,Integer>(); 
			HashMap<String,Integer> counter5=new HashMap<String,Integer>(); 
			StringBuilder insertQuery = new StringBuilder();
/*			stmt1.executeUpdate("ALTER TABLE lonet.occupation ADD COLUMN parent VARCHAR(10)");
			stmt1.executeUpdate("ALTER TABLE lonet.occupation CHANGE COLUMN code code CHAR(15) NOT NULL");
			stmt1.executeUpdate("ALTER TABLE lonet.occupation CHANGE COLUMN type type varCHAR(25) NOT NULL");
			stmt1.executeUpdate("ALTER TABLE lonet.occupation CHANGE COLUMN description description VARCHAR(1000) NULL");
			insertQuery.append("insert into lonet.occupation (code, name, parent, type) values ");
*/
			String ssoc_code="";
			int j = 1;
			
			stmt1.executeUpdate("truncate table ssoc_temp.esco_ssoc_mapping");
			// *********** LEVEL 6 **********************************
			System.out.println("A- The following occupation from ESCO 5 are mapped as NEW SSOC level 6: ");
			insertQuery.append("insert into ssoc_temp.esco_ssoc_mapping values ");
			query = "SELECT distinct ESCO_ADDED_AS_SSOC_6 FROM ssoc_temp.esco_ssoc_mapping_data where length(ESCO_ADDED_AS_SSOC_6)=5";
			rs1 = stmt1.executeQuery(query);
			for (int i=0; rs1.next(); i++) {
				counter6.put(rs1.getString(1), 1);		
			}
				
			query = "SELECT distinct esco_uri, esco_5 FROM ssoc_temp.esco_ssoc_mapping_data where length(ESCO_ADDED_AS_SSOC_6)=5";
			rs1 = stmt1.executeQuery(query);
			for (int i=0; rs1.next(); i++) {
				query = "SELECT distinct esco_uri, ESCO_ADDED_AS_SSOC_6,SSOC_5 FROM ssoc_temp.esco_ssoc_mapping_data where length(ESCO_ADDED_AS_SSOC_6)=5 and esco_uri = \"" + rs1.getString(1) + "\"";
				//System.out.println(query);
				rs2 = stmt2.executeQuery(query);
				if (rs2.next()) {
					ssoc_code=rs2.getString(2) + "-" + formatnumber(counter6.get(rs2.getString(2))) + "-wcc";
						System.out.println("\t" + j + "- " + rs1.getString(2) + " --> " + ssoc_code + "\t(parent: " + rs2.getString(3) +")");
						counter6.put(rs2.getString(2), counter6.get(rs2.getString(2))+1);
						insertQuery.append ("('" + ssoc_code + "','" + rs2.getString(1) + "',6,'" + rs2.getString(2) + "',null,'new wcc/ssoc occupation from esco')," );
						j++;
						}

			}
			//System.out.println(insertQuery.toString());
			stmt1.executeUpdate(insertQuery.toString().substring(0, insertQuery.toString().length()-1));
			

			// *********** LEVEL 4 mapped **********************************
			System.out.println("\nB- The following occupation from ESCO 5 are mapped to EXISTING SSOC level 4: ");
			insertQuery = new StringBuilder();
			insertQuery.append("insert into ssoc_temp.esco_ssoc_mapping values ");
			query = "SELECT distinct esco_uri, esco_5 FROM ssoc_temp.esco_ssoc_mapping_data where length(ESCO_5_MAPPED_ISCO_4)=4";
			rs1 = stmt1.executeQuery(query);
			j = 1;
			for (int i=0; rs1.next(); i++) {
				query = "SELECT distinct esco_uri, ESCO_5_MAPPED_ISCO_4, isco_4 FROM ssoc_temp.esco_ssoc_mapping_data where length(ESCO_5_MAPPED_ISCO_4)=4 and esco_uri = \"" + rs1.getString(1) + "\"";
				//System.out.println(query);
				rs2 = stmt2.executeQuery(query);
				if (rs2.next()) {
					ssoc_code=rs2.getString(2);
						System.out.println("\t" + j + "- " + rs1.getString(2) + " --> " + ssoc_code + "\t(" + rs2.getString(3) +")");
						insertQuery.append ("('" + ssoc_code + "','" + rs2.getString(1) + "',5,'" + ssoc_code.substring(0, 4) + "',null,'manually mapped from esco')," );
						j++;
						}

			}
			//System.out.println(insertQuery.toString());
			stmt1.executeUpdate(insertQuery.toString().substring(0, insertQuery.toString().length()-1));
			
			// *********** LEVEL 5 mapped **********************************
			System.out.println("\nC- The following occupation from ESCO 5 are mapped to EXISTING SSOC level 5: ");
			insertQuery = new StringBuilder();
			insertQuery.append("insert into ssoc_temp.esco_ssoc_mapping values ");
			query = "SELECT distinct esco_uri, esco_5 FROM ssoc_temp.esco_ssoc_mapping_data where length(ESCO_5_MAPPED_SSOC_5)=5";
			rs1 = stmt1.executeQuery(query);
			j = 1;
			for (int i=0; rs1.next(); i++) {
				query = "SELECT distinct esco_uri, ESCO_5_MAPPED_SSOC_5, ssoc_5 FROM ssoc_temp.esco_ssoc_mapping_data where length(ESCO_5_MAPPED_SSOC_5)=5 and esco_uri = \"" + rs1.getString(1) + "\"";
				//System.out.println(query);
				rs2 = stmt2.executeQuery(query);
				if (rs2.next()) {
					ssoc_code=rs2.getString(2);
						System.out.println("\t" + j + "- " + rs1.getString(2) + " --> " + ssoc_code + "\t(" + rs2.getString(3) +")");
						insertQuery.append ("('" + ssoc_code + "','" + rs2.getString(1) + "',5,'" + ssoc_code.substring(0, 4) + "',null,'manually mapped from esco')," );
						j++;
						}

			}
			//System.out.println(insertQuery.toString());
			stmt1.executeUpdate(insertQuery.toString().substring(0, insertQuery.toString().length()-1));
			
			// *********** LEVEL 5 new **********************************
			System.out.println("\nD- The following occupation from ESCO 5 are mapped a NEW SSOC level 5: ");
			insertQuery = new StringBuilder();
			insertQuery.append("insert into ssoc_temp.esco_ssoc_mapping values ");
			query = "SELECT distinct left(WCC_SSOC_5_brother_SSOC_5,5) FROM ssoc_temp.esco_ssoc_mapping_data where length(WCC_SSOC_5_brother_SSOC_5)>=5";
			rs1 = stmt1.executeQuery(query);
			for (int i=0; rs1.next(); i++) {
				query = "select max(right(code_5,1))+1 FROM ssoc_temp.esco_ssoc_mapping_data where code_5 like '" + rs1.getString(1).substring(0, 4) +"%'";
				rs2 = stmt2.executeQuery(query);
				if (rs2.next()) 
					counter5.put(rs1.getString(1).substring(0, 4), rs2.getInt(1));		
			}

			j = 1;
			query = "SELECT distinct esco_uri, left(WCC_SSOC_5_brother_SSOC_5,5) FROM ssoc_temp.esco_ssoc_mapping_data where length(WCC_SSOC_5_brother_SSOC_5)>=5";
			rs1 = stmt1.executeQuery(query);
			for (int i=0; rs1.next(); i++) {
				//brother ssoc_5 causing issues
				//query = "SELECT distinct esco_uri, left(WCC_SSOC_5_brother_SSOC_5,5), brother_SSOC_5_WCC_SSOC_5,esco_5 FROM ssoc_temp.esco_ssoc_mapping_data where length(WCC_SSOC_5_brother_SSOC_5)>=5 and esco_uri = \"" + rs1.getString(1) + "\"";
				query = "SELECT distinct esco_uri, left(WCC_SSOC_5_brother_SSOC_5,5), null, esco_5 FROM ssoc_temp.esco_ssoc_mapping_data where length(WCC_SSOC_5_brother_SSOC_5)>=5 and esco_uri = \"" + rs1.getString(1) + "\"";
				//System.out.println(query);
				rs2 = stmt2.executeQuery(query);
				if (rs2.next()) {
					ssoc_code=rs2.getString(2).substring(0, 4) + "-" 
						+ formatnumber(counter5.get(rs2.getString(2).substring(0, 4))) + "-wcc";
						System.out.println("\t" + j + "- " + rs1.getString(2) + " --> " + ssoc_code + "\t(" + rs2.getString(4) +")");
						counter5.put(rs2.getString(2).substring(0, 4), counter5.get(rs2.getString(2).substring(0, 4))+1);
						insertQuery.append ("('" + ssoc_code + "','" + rs2.getString(1) + "',5,'" + rs2.getString(2).substring(0, 4) + "','" + rs2.getString(3) + "','new wcc/ssoc occupation from esco')," );
						j++;
						}

			}
			//System.out.println(insertQuery.toString());
			stmt1.executeUpdate(insertQuery.toString().substring(0, insertQuery.toString().length()-1));
			stmt1.executeUpdate("update ssoc_temp.esco_ssoc_mapping set closest_brother=null where closest_brother='null'");
			

			
			rs1.close();
			stmt1.close();
			stmt2.close();
			conn.close();

		}
		catch (ClassNotFoundException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

		public static String formatnumber (int number) {
			if (number<10)
				return "00"+number;
			if (number<100)
				return "0"+number;
			if (number<1000)
				return ""+number;
			return "";
		
		}
}