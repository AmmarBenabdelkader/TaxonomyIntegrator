/**
* @author abenabdelkader
*
* testing.java
* Oct 7, 2015
*/
package com.wccgroup.taxonomy.integrator;

import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.HttpsURLConnection;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.poi.POITextExtractor;
import org.apache.poi.extractor.ExtractorFactory;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
//import com.wccgroup.elise.testdata_ssoc.Parser_niriCVs_HRxml.UserHandler;
import org.json.simple.parser.ParseException;

public class VacancyParser
{
	private final String USER_AGENT = "Mozilla/5.0";

	static String JDBC_DRIVER = "";
	static String DB_URL = "";

	//  Database credentials
	static String USER = "";
	static String PASS = "";
	static Connection conn = null;
	static Statement stmt = null;
	static Statement stmt2 = null;

	public static void main(String[] args) throws IOException
	{
		JDBC_DRIVER = "com.mysql.jdbc.Driver";
		DB_URL = "jdbc:mysql://localhost/onet?useUnicode=true&characterEncoding=utf-8";
		USER = "root";
		PASS = "";
		try
		{
			Class.forName(JDBC_DRIVER);
			conn = DriverManager.getConnection(DB_URL, USER, PASS);
			stmt = conn.createStatement();
			stmt2 = conn.createStatement();

			//readProperties();
			//readDBProperties();
			String path = "\\\\savannah\\home\\abenabdelkader\\Documents\\projects\\Singapore\\data\\vacancies";
			File folder = new File(path);
			File[] listOfFiles = folder.listFiles();
			int i;
			String filename = null;

		    for (i = 0; i < listOfFiles.length; i++) {
			      if (listOfFiles[i].isFile()) {
			    	 filename = listOfFiles[i].getName();
			        System.out.println((i+1) + ": " + filename);
					if (filename.toLowerCase().endsWith(".xlsx")) {
						createVacancy(path,filename);
					}
					//parseCV(extractedText,path,filename);
					//enrichCV(path,filename);
			      }
			    }

			stmt.close();
			stmt2.close();
			conn.close();
		}
		catch (Exception e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	// HTTP POST request
	private static void parseCV(String data, String path, String filename) throws IOException
	{
		URL url = new URL("http://demos.savannah.wcc.nl:14080/profiles/v1/candidates/parse");
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setDoOutput(true);

		//String loginPassword = "nl:janzz";
		//String encoded = new String(Base64.encodeBase64(StringUtils.getBytesUtf8(loginPassword)));
		//conn.setRequestProperty("Authorization", "Basic " + encoded);

		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "text/plain");
		conn.setRequestProperty("charset", "UTF-8");

		OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());

		String line;
		writer.write(data);
		writer.flush();
		

		BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		
		BufferedWriter writer2 = new BufferedWriter(new FileWriter(new File(path + "_output\\" + filename + ".xml")));
			
		while ((line = reader.readLine()) != null)
		{
			//System.out.println(line);
			writer2.write(line.replaceAll("<Title>", "\n<Title>").replaceAll("<Description>", "\n<Description>"));

		}

		writer2.close();
		writer.close();
		reader.close();
	}

	public static String getEnrichment(String title) throws SQLException, ClassNotFoundException
	{
			String code = getCode(title);
			String content="\t\t<enriched_content>\n";
			String query = "SELECT distinct ssoc_code, skill, type,relationshipType FROM lssoc.occupation_skills_esco "
				+ "where ssoc_code='" + code + "'"; //relationshipType='essential' and ;
			//System.out.println(query);
			ResultSet rs=stmt.executeQuery(query);
			while (rs.next()) {
				content += "\t\t<wcc_competency>esco_skills;" + rs.getString(2) + ";occupation-" + rs.getString(4) + "-" + rs.getString(3) + "</wcc_competency>\n";
							
			}

			query = "SELECT distinct b.ssoc_code, concat(element_id,'.', category) education, CONVERT(avg(data_value),UNSIGNED INTEGER) score "
				+ "FROM onet.education_training_experience a, lssoc.ssoc2015_onet2015 b "
				+ "where a.onetsoc_code=b.onet_code_2015 and scale_id='RL' and data_value>1.5 "
				+ "and b.ssoc_code='" + code.substring(0, 4) + "' "
				+ "group by b.ssoc_code, element_id, category order by b.ssoc_code, score desc";
			//System.out.println(query);
			rs=stmt.executeQuery(query);
			while (rs.next()) {
				content += "\t\t<wcc_education>education_training_experience;" + rs.getString(2) + ";occupation-education</wcc_education>\n";
				//content += "\t\t<wcc_education>education_training_experience;occupation-education;" + rs.getString(2) + ";" + rs.getString(3) + "</wcc_education>\n";
							
			}


			query = "SELECT 'interests;', element_id, CONVERT(avg(data_value/7*100),UNSIGNED INTEGER) score, ';occupational-interests'"
				+ "FROM onet.interests a, lssoc.ssoc2015_onet2015 b where a.onetsoc_code=b.onet_code_2015 and scale_id='OI' "
				+ "and b.ssoc_code='" + code.substring(0, 4) + "' "
				+ "group by ssoc_code,element_id order by ssoc_code asc, score desc, element_id asc";
			//System.out.println(query);
			rs=stmt.executeQuery(query);
			while (rs.next()) {
				content += "\t\t<wcc_interest>" + rs.getString(1)  + rs.getString(2)  + rs.getString(4) + "</wcc_interest>\n";
				//content += "\t\t<wcc_interest>" + rs.getString(1)  + rs.getString(2) + ";" + rs.getString(3) + "</wcc_interest>\n";
							
			}


			query = "SELECT distinct 'interests;', a.element_id, CONVERT(avg(data_value/5*100),UNSIGNED INTEGER) score, ';occupation-work-context'"
				+ "FROM onet.work_context a, lssoc.ssoc2015_onet2015 b where a.onetsoc_code=b.onet_code_2015 and scale_id='CX' and  data_value>=3 "
				+ "and ssoc_code='" + code.substring(0, 4) + "' "
				+ "group by ssoc_code,element_id order by ssoc_code asc, score desc, element_id asc";
			//System.out.println(query);
			rs=stmt.executeQuery(query);
			while (rs.next()) {
				content += "\t\t<wcc_work_condition>" + rs.getString(1)  + rs.getString(2) + rs.getString(4) + "</wcc_work_condition>\n";
				//content += "\t\t<wcc_work_conditiont>" + rs.getString(1)  + rs.getString(2) + ";" + rs.getString(3) + "</wcc_work_conditiont>\n";
							
			}
			content +="\t\t</enriched_content>\n";

			return content;

	}

	public static String getCode(String title) throws SQLException, ClassNotFoundException
	{
		String code="";
		String name="";
		String score="";
		try
		{
		URL url = new URL("http://demos.savannah.wcc.nl:14080/semanticsearch/v1/occupationtitles/text?text=" + title.replaceAll(" ", "%20").trim() + "&sort=sort");
		System.out.println(url.toString());
		URLConnection yc = url.openConnection();
		BufferedReader in;
			in = new BufferedReader(new InputStreamReader(
				yc.getInputStream()));
		String line;
		while ((line = in.readLine()) != null) {
			if (line.contains("\"id\" :")){
				code = line.substring(12, line.length()-2);
				line = in.readLine();
			if (line.contains("\"name\" :"))
				name = line.substring(14, line.length()-2);
			line = in.readLine().trim();
			if (line.contains("\"score\" :"))
				score = line.substring(10, line.length());
			
			//System.out.println ("mapped " + title + " to " + name + "(" + score + ")");
			return code;
		}
		}
		in.close();
		}
		catch (IOException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return code;

	}


	/* Loads data generation properties into the system
	 * allows the connection to the database
	 */
	public static void readDBProperties()
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
	public static void createVacancy(String path, String filename)
	{

		try
		{
			FileInputStream file = new FileInputStream(new File(path + "//" + filename));

			//Create Workbook instance holding reference to .xlsx file
			XSSFWorkbook workbook = new XSSFWorkbook(file);

			//Get first/desired sheet from the workbook
			XSSFSheet sheet = workbook.getSheetAt(0);

			//Iterate through each rows one by one
			Iterator<Row> rowIterator = sheet.iterator();
			int i = 0;
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(path + "_output\\" + filename + "_enriched.xml")));
			writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<PositionOpening>\n");
			Row row = rowIterator.next();
			while (rowIterator.hasNext())
			{
				//rowIterator.next();
				row = rowIterator.next();
				//For each row, iterate through all the columns
				//if ( row.getCell(0).getCellType() == 1)  // String 
				writer.write("\t<PositionRecordInfo>\n\t\t<Id>\n\t\t\t<IdValue>" + row.getCell(0).getStringCellValue() + "</IdValue>\n\t\t</Id>\n");
				writer.write("\t\t<Status validFrom=\"\" validTo=\"\">Active</Status>\n\t</PositionRecordInfo>\n");
				writer.write("\t<PositionProfile xml:lang=\"en\">\n");
				writer.write("\t\t<Organization>\n");
				writer.write("\t\t\t<OrganizationName>" + row.getCell(5).getStringCellValue() + "</OrganizationName>\n");
				writer.write("\t\t\t<Description>" + row.getCell(6).getStringCellValue() + "</Description>\n");
				writer.write("\t\t</Organization>\n");
				writer.write("\t\t<PositionDetail>\n");
				writer.write("\t\t\t<JobCategory>\n\t\t\t\t<CategoryCode>" + row.getCell(7).getStringCellValue() + "</CategoryCode>\n\t\t\t</JobCategory>\n");
            
				writer.write("\t\t\t<PositionTitle>" + row.getCell(1).getStringCellValue() + "</PositionTitle>\n");
				writer.write("\t\t\t<PositionCode classificationName=\"SSOC\">" + row.getCell(2).getStringCellValue() + "</PositionCode>\n");
				writer.write("\t\t</PositionDetail>\n");
				writer.write("\t\t<FormattedPositionDescription>\n\t\t\t<Name>jobDescription</Name>\n");
				writer.write("\t\t\t\t<Value>" + row.getCell(3).getStringCellValue() + "</Value>\n");
				writer.write("\t\t</FormattedPositionDescription>\n");
				writer.write("\t\t<FormattedPositionDescription>\n\t\t\t<Name>requiredQualifications</Name>\n");
				writer.write("\t\t\t\t<Value>" + row.getCell(8).getStringCellValue() + "</Value>\n");
				writer.write("\t\t</FormattedPositionDescription>\n\t</PositionProfile>\n");
				String enrich = getEnrichment(row.getCell(1).getStringCellValue());
				writer.write("\t<UserArea>\n" + enrich + "\t</UserArea>\n");
			}
			writer.write("</PositionOpening>");
			file.close();
			workbook.close();
			writer.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	

}