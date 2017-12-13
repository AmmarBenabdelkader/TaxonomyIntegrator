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
import java.text.SimpleDateFormat;
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

public class CVparser
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
	static String code, occupation, score;
	static BufferedWriter writer2;
    static int workexp=0;


	public static void main(String[] args) 
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
			String path = "c:\\data\\singapore\\CVs\\ManpowerCVs";
			//parseCVs(path);
			enrichCVs(path);
/*
		    for (i = start; i < listOfFiles.length+start; i++) {
			      if (listOfFiles[i].isFile()) {
			    	filename = listOfFiles[i].getName();
			        System.out.println((i+1) + ": " + filename);
					POITextExtractor extractor;
					InputStream fis = new FileInputStream(listOfFiles[i]);
					if (filename.toLowerCase().endsWith(".docx")) {
					    XWPFDocument doc = new XWPFDocument(fis);
					    extractor = new XWPFWordExtractor(doc);
					} else 
						if (filename.toLowerCase().endsWith(".doc")) {
					    // if doc
					    POIFSFileSystem fileSystem = new POIFSFileSystem(fis);
					    extractor = ExtractorFactory.createExtractor(fileSystem);
					}
						else 
							continue;
					
					String extractedText = extractor.getText();
					parseCV(extractedText,path,filename, i);
					File a = new File(path + "\\" + filename);
					a.renameTo(new File(path +"_processed\\"+ filename));
					a.delete();

					enrichCV(path,filename, i);
			      }
			    }
*/
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

	public static void parseCVs(String path) 
	{
		try
		{
			File folder = new File(path);
			File[] listOfFiles = folder.listFiles();
			int i;
			String filename = null;
			Date date = new Date();
			writer2 = new BufferedWriter(new FileWriter(new File(path + "\\mapping_CV_titles.txt")));
			writer2.write("job title\toccupation code\toccupation title\tscore\n");

		    for (i = 0; i < listOfFiles.length; i++) {
			      if (listOfFiles[i].isFile()) {
			  		date = new Date();
			    	filename = listOfFiles[i].getName();
			        System.out.print((i) + ": " + filename);

			        
					File tmpDir = new File(path + "_parsed\\" + filename.substring(0, filename.length()-3) + "xml");
					if (tmpDir.exists()) {
					    System.out.println("\talready parsed");
						continue;
					}


			        int length = (int) listOfFiles[i].length();
			        BufferedInputStream reader = new BufferedInputStream(new FileInputStream(listOfFiles[i]));
			        byte[] bytes = new byte[length];
			        reader.read(bytes, 0, length);
			        reader.close();
			        
			        //byte[] decodedBytes = Base64.decodeBase64(bytes);
					parseCV(new String(bytes),path,filename);
			        System.out.println("\tparsed in: " + + (new Date().getTime() - date.getTime())/1000 + "s");
			      }
			    }
			writer2.close();
		}
		catch (Exception e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public static void enrichCVs(String path) 
	{
		try
		{
			File folder = new File(path + "_parsed");
			File[] listOfFiles = folder.listFiles();
			int i;
			String filename = null;
			Date date = new Date();
			writer2 = new BufferedWriter(new FileWriter(new File(path + "\\mapping_CV_titles.txt")));
			writer2.write("job title\toccupation code\toccupation title\tscore\n");

		    for (i = 0; i < listOfFiles.length; i++) {
			      if (listOfFiles[i].isFile()) {
			  		date = new Date();
			    	filename = listOfFiles[i].getName();
			        System.out.print(i + " " + filename);
			        
			        
					enrichCV(path ,filename);
			        System.out.println("\tenriched in: " + + (new Date().getTime() - date.getTime())/1000 + "s");
			      }
			    }
			writer2.close();
		}
		catch (Exception e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	private static int computeWorkExpCode(String line) throws java.text.ParseException
	{
        Date date1, date2;
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
        int totalMonths = 0;

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
            	//System.out.print("\n" + date1 + "\t" + date2 + "\t-->\t"  + (((date2.getYear()-date1.getYear())*12) + (date2.getMonth()-date1.getMonth())) +  " months");

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
            	//System.out.print("\n" + date1 + "\t" + date2 + "\t-->\t"  +  (((date2.getYear()-date1.getYear())*12) + (date2.getMonth()-date1.getMonth())) +  " months");

        	}
         		
    	//System.out.print("\n\t---> Total Months of Experience: " + totalMonths +  " months");
       return (totalMonths==0?0:totalMonths==1?1:totalMonths<4?3:totalMonths<7?4:totalMonths<13?5:totalMonths<25?6:totalMonths<49?7:totalMonths<73?8:totalMonths<97?9:totalMonths<121?10:totalMonths>120?11:0);
	}

	// HTTP POST request
	private static void parseCV(String data, String path, String filename)
	{
		try
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
        writer.write(data);
        writer.flush();
        writer.close();


/*		OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());

		writer.write(data);
		writer.flush();
*/		
		String line;

		BufferedReader reader;
			reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		
		BufferedWriter writer2;
			writer2 = new BufferedWriter(new FileWriter(new File(path + "_parsed\\" + filename.substring(0, filename.length()-3) + "xml")));
			while ((line = reader.readLine()) != null)
			{
				//System.out.println(line);
				writer2.write(line.replaceAll("<Title>", "\n<Title>").replaceAll("<Description>", "\n<Description>"));

			}
		writer2.close();
		writer.close();
		reader.close();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void enrichCV(String path, String filename) throws IOException, ClassNotFoundException, SQLException, java.text.ParseException
	{
		String title = null;
		File tmpDir = new File(path + "_parsed\\" + filename);
		if (!tmpDir.exists())
			return;
		
		tmpDir = new File(path + "_enriched\\" + filename);
		if (tmpDir.exists()) {
            System.out.print("\talready enriched");
			return;
		}
		
		FileReader fileReader = new FileReader(path + "_parsed\\" + filename);

        BufferedReader bufferedReader = new BufferedReader(fileReader);
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(path + "_enriched\\" + filename )));
		//BufferedWriter writer = new BufferedWriter(new FileWriter(new File(path + "_enriched\\" + i + "_enriched.xml")));
        String line = null;

        while((line = bufferedReader.readLine()) != null) {
            //System.out.println(line);
            //writer.write(line);
            if (line.contains("<PositionHistory>"))
            	workexp = computeWorkExpCode(line);
			if (line.contains("<Title>")) { 
	            writer.write(line.substring(0, line.indexOf("</Title>")+8));
				int index = line.indexOf("<Title>");
				while (index >= 0) {
					code = occupation=score=null;
					String enrich=null;
					title = line.substring(index+7, line.indexOf("</Title>", index));
					if (title.trim().length()>2)
						enrich = getCode(title.trim());
					writer2.write(title+"\t" + code + "\t" + occupation + "\t" + score + "\n");
					//System.out.println("\ttitle: " + title + " --> " + (getCode(title)==null?"":getCode(title)));
					//System.out.println("\ttitle: " + title + " --> " + enrich);
					if (enrich!=null)
						writer.write(enrich);
					//titles.put(title, getCode(title));
					index = line.indexOf("<Title>", index+1);
				}
	            writer.write(line.substring(line.indexOf("</Title>")+8, line.length()));
			} 
			else
	            writer.write(line);
				
        }
        bufferedReader.close();
        writer.close();
	}


	/* Reads the job titles from a taxonomy and mapps them to ontology functions/function_groups */
	public static String getCode(String title) throws SQLException, ClassNotFoundException
	{
			try
			{
			URL url = new URL("http://demos.savannah.wcc.nl:14080/semanticsearch/v1/occupationtitles/text?text=" + title.replaceAll(" ", "%20").trim() + "&sort=sort");
			URLConnection yc = url.openConnection();
			BufferedReader in;
				in = new BufferedReader(new InputStreamReader(
					yc.getInputStream()));
			String line;
			String code1 = "\n<PositionClassification   name=";
			String enrich = "";
			while ((line = in.readLine()) != null) {
				if (line.contains("\"id\" :")){
					code = line.substring(12, line.length()-2);
					//code1 += "id=\"" + line.substring(12, line.length()-2) + "\"";
					enrich = getEnrichment(line.substring(12, line.length()-2));
					line = in.readLine();
				if (line.contains("\"name\" :"))
					occupation = line.substring(14, line.length()-2).replaceAll("&", "&amp;");
				line = in.readLine().trim();
				//System.out.println(line);
				if (line.contains("\"score\" :")){
					score=line.substring(10, line.length());
					//code1 += " score=\"" + line.substring(10, line.length())+ "\">";
				}

				code1 += "\"" + occupation + "\" score=\"" + score + "\">" + code + "</PositionClassification>\n";

						return code1 + enrich;
							
				}
			}
			in.close();
			}
			catch (IOException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return null;

	}


	public static String getEnrichment(String code) throws SQLException, ClassNotFoundException
	{
		String content="\t\t<enriched_content>\n";
		String query = "SELECT distinct ssoc_code, skill, type,relationshipType FROM lssoc.occupation_skills_esco "
			+ "where ssoc_code='" + code + "'"; //relationshipType='essential' and ;
		//System.out.println(query);
		ResultSet rs=stmt.executeQuery(query);
		while (rs.next()) {
			content += "\t\t<wcc_competency>esco_skills;" + rs.getString(2) + ";occupation-" + rs.getString(4) + "-" + rs.getString(3) + "</wcc_competency>\n";
						
		}

		query = "SELECT convert(((jz*20)+(" + workexp + "*9.09)+(eljb*16.66))/3/20, unsigned int) FROM lssoc.occupation_skill_level where code='" + code.substring(0, 4) + "'"; 
		//System.out.println(query);
		rs=stmt.executeQuery(query);
		if (rs.next()) 
			content += "\t\t<wcc_competency_level>" + rs.getString(1) + "</wcc_competency_level>\n";

		query = "SELECT riasec FROM lssoc.occupation_interest_group where code='" + code.substring(0, 4) + "'"; 
		rs=stmt.executeQuery(query);
		if (rs.next()) 
			content += "\t\t<wcc_interest_raisec>" + rs.getString(1) + "</wcc_interest_raisec>\n";

		
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
	

}