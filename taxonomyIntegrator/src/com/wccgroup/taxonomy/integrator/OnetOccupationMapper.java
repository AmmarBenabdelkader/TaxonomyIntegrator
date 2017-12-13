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

public class OnetOccupationMapper
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
			//classifyOccupations("");
			//genericOccupations("");
			groupedOccupations("");
/*			System.out.println(getEnrichment("25122-001-wcc"));
			String query = "SELECT code, name FROM lonet.alternate_job_titles where name not  in (select name from lssoc.all_occupation)"; 
			getCodes(query);
*/			stmt.close();
			conn.close();
		}
		catch (Exception e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}


	public static String classifyOccupations(String query) throws SQLException, ClassNotFoundException
	{
		Map<String, Double> termWeight = new HashMap<String, Double>();
		termWeight.put("Developer", .5);
		termWeight.put("Programmer", .5);
		termWeight.put("Application", .5);
		termWeight.put("Manager", .5);
		termWeight.put("Analyst", .7);
		Statement stmt3 = conn.createStatement();
		StringBuilder queryString = new StringBuilder();
		query = "SELECT onetsoc_code, title FROM onet.occupation_data where onetsoc_code like '15-11%'"; 
		ResultSet rs=stmt.executeQuery(query);
		int i = 1;
		double score=0.0;
		double weight=1;
		while (rs.next()) {
			queryString = new StringBuilder();
			queryString.append("insert into ssoc_temp.occupation_similarity values ");
			System.out.println(rs.getString(1) + ": " + rs.getString(2));
			query = "SELECT onetsoc_code, reported_job_title FROM onet.sample_of_reported_titles where onetsoc_code ='" + rs.getString(1) + "'"; 
			ResultSet rs2=stmt2.executeQuery(query);
			while (rs2.next()) {
				String words2[]=rs2.getString(2).split(" ");
				int l2=rs2.getString(2).length();
				//System.out.println("\t" + rs2.getString(1) + ": " + rs2.getString(2) + "\t(length: " + l2 + " ,Words: " + words2.length + ")");
				System.out.print("\n\t" + rs2.getString(1) + ": " + rs2.getString(2));
				query = "SELECT onetsoc_code, alternate_title FROM onet.alternate_titles where onetsoc_code ='" + rs2.getString(1) + "' and alternate_title<>'" + rs2.getString(2) + "'"; 
				ResultSet rs3=stmt3.executeQuery(query);
				while (rs3.next()) {
					String words3[]=rs3.getString(2).split(" ");
					int l3=rs3.getString(2).length();
					int score2=0;
					int score3=0;
					//System.out.println("\t\t" + rs3.getString(2) + "\t(length: " + l3 + " ,Words: " + words3.length + ")");
					for (int j=0; j<words2.length;j++){
						//System.out.println("\t\t\tif " + rs3.getString(2) + "\tcontains " + words2[j]);
						if (termWeight.get(words2[j])!=null)
							weight=termWeight.get(words2[j]);
						else
							weight = 1;
						if (rs3.getString(2).contains(words2[j]))
							score2+=words2[j].length()*weight;
					}
					for (int j=0; j<words3.length;j++){
						if (termWeight.get(words3[j])!=null)
							weight=termWeight.get(words3[j]);
						else
							weight = 1;
						if (rs2.getString(2).contains(words3[j]))
							score3+=words3[j].length()*weight;
					}
					score = (score2*100.00/l3+score3*100.00/l2)/2;
					//System.out.println("\t\t\tscore_1=" + score2 + "\tscore3= " + score3 + " ,Similarity: " + (score2*100.00/l3+score3*100.00/l2)/2 + ")");
					System.out.print("\n\t\t" + rs3.getString(2) + "\t: " + score);
					if (score>0)
						queryString.append("(\"" + rs2.getString(1) + "\", \"" + rs2.getString(2) + "\", \"" + rs3.getString(2) + "\"," + (score2*100.00/l3+score3*100.00/l2)/2 + "),");
				}
				rs3.close();
			}
			rs2.close();
			i++;
			stmt3.executeUpdate(queryString.substring(0, queryString.length()-1));

		}
		rs.close();
		return null;

	}

	public static String genericOccupations(String query) throws SQLException, ClassNotFoundException
	{
		String genericTerms = "#Developer#Programmer#Application#Applications#Manager#Analyst#Computer#Software#Systems#Engineer#";
		Statement stmt3 = conn.createStatement();
		StringBuilder queryString = new StringBuilder();
		query = "SELECT onetsoc_code, title FROM onet.occupation_data where onetsoc_code like '15-11%'"; 
		ResultSet rs=stmt.executeQuery(query);
		int i = 1;
		double score=0.0;
		double weight=1;
		while (rs.next() && i < 2) {
			System.out.println(rs.getString(1) + ": " + rs.getString(2));
			query = "SELECT onetsoc_code, alternate_title FROM onet.alternate_titles where onetsoc_code ='" + rs.getString(1) + "'"; 
				ResultSet rs3=stmt3.executeQuery(query);
				while (rs3.next()) {
					score = 0.0;
					String words3[]=rs3.getString(2).split(" ");
					int l3=rs3.getString(2).length() - (words3.length-1);
					for (int j=0; j<words3.length;j++){
						if (genericTerms.contains("#"+words3[j]+"#"))
							weight=1.0;
						else
							weight = 0.0;
							score+=words3[j].length()*weight;
					}
					score = (score*100.00)/l3;
					//if (score>0)
					System.out.print("\n\t\t" + rs3.getString(2) + "\t: " + score);
				}
				rs3.close();
			i++;

		}
		rs.close();
		return null;

	}


	public static String groupedOccupations(String query) throws SQLException, ClassNotFoundException
	{
		Map<Integer, String> occupations = new HashMap<Integer, String>();
		Map<String, String> occupations2 = new HashMap<String, String>();
		Statement stmt3 = conn.createStatement();
		query = "SELECT onetsoc_code, title FROM onet.occupation_data order by onetsoc_code"; // where onetsoc_code like '15-11%'"; 
		ResultSet rs=stmt.executeQuery(query);
		int i = 1;
		while (rs.next()) {
			StringBuilder queryString = new StringBuilder();
			queryString.append("insert into ssoc_temp.occupation_classified values ");
			occupations = new HashMap<Integer, String>();
			occupations2.put(rs.getString(2), null);
			System.out.println("\n" + rs.getString(1) + ": " + rs.getString(2));
			queryString.append("(\"" + rs.getString(1) + "\",\"" + rs.getString(2) + "\", null, null)," );
			query = "SELECT onetsoc_code, alternate_title FROM onet.alternate_titles "
				+ "where onetsoc_code ='" + rs.getString(1) + "' "
					+ "and alternate_title not in (SELECT distinct name FROM ssoc_temp.occupation_classified)"; 
			ResultSet rs2=stmt2.executeQuery(query);
			int j=0;
			while (rs2.next()) {
				occupations.put(j, rs2.getString(2));
				j++;
			}
				rs2.close();
			for (int m=0; m<j;m++) {
				for (int n=0; n<j;n++) {
						if (m!=n && occupations.get(m).contains(occupations.get(n))){
							System.out.print("\n\t\t" + m + "- " + occupations.get(m) + "\t: " + n + "- "  + occupations.get(n));
							//if (occupations2.get(occupations.get(n))==null){
								occupations2.put(occupations.get(n), occupations.get(m));
								queryString.append("(\"" + n + "\",\"" + occupations.get(n) + "\", \"" + rs.getString(1) + "\", null)," );
								queryString.append("(\"" + m + "\",\"" + occupations.get(m) + "\", \"" + occupations.get(n) + "\", \"" + rs.getString(1) + "\")," );
							//}
						}
					}
				}
			i++;

		 //System.out.println(queryString);
		 stmt3.executeUpdate(queryString.substring(0, queryString.length()-1));
		}
		rs.close();
		 for(Map.Entry occup:occupations2.entrySet()) {  
			 System.out.println(occup.getKey() + "\t<--\t" + occup.getValue());
		 }  
		
		return null;

	}

	/* Reads the job titles from a taxonomy and mapps them to ontology functions/function_groups */
	public static String getCodes(String query) throws SQLException, ClassNotFoundException
	{
		ResultSet rs=stmt.executeQuery(query);
		int i = 1;
		while (rs.next() && i<100) {
			System.out.println(rs.getString(1) + ": " + rs.getString(2) + "---->" + getCode(rs.getString(2)));
			i++;

		}
		rs.close();
		return null;

	}

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
			while ((line = in.readLine()) != null) {
				if (line.contains("\"id\" :")){
					code = line.substring(12, line.length()-2);
					line = in.readLine();
					if (line.contains("\"name\" :"))
						occupation = line.substring(14, line.length()-2).replaceAll("&", "&amp;");
					line = in.readLine().trim();
					if (line.contains("\"score\" :")){
						score=line.substring(10, line.length());
					}


					return code + ": " + occupation + " (" + score + ")";

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
		String content ="\t\t<enriched_content>\n";
		String query = "SELECT distinct ssoc_code, skill, type,relationshipType FROM lssoc.occupation_skills_esco "
			+ "where ssoc_code='" + code + "'"; //relationshipType='essential' and ;
		//System.out.println(query);
		ResultSet rs=stmt.executeQuery(query);
		while (rs.next()) {
			content += "\t\t<wcc_competency>esco_skills;" + rs.getString(2) + ";occupation-" + rs.getString(4) + "-" + rs.getString(3) + "</wcc_competency>\n";

		}

		query = "SELECT skill_level FROM lssoc.occupation_skill_level where code='" + code.substring(0, 4) + "'"; 
		rs=stmt.executeQuery(query);
		if (rs.next()) 
			content += "\t\t<wcc_competency_level>" + rs.getString(1) + "</wcc_competency_level>\n";

		query = "SELECT riasec FROM lssoc.occupation_interest_group where code='" + code.substring(0, 4) + "'"; 
		rs=stmt.executeQuery(query);
		if (rs.next()) 
			content += "\t\t<wcc_interest_raisec>" + (rs.getString(1)==null?"":rs.getString(1)) + "</wcc_interest_raisec>\n";

		query = "SELECT distinct b.ssoc_code, concat(element_id,'.', category) education, CONVERT(avg(data_value),UNSIGNED INTEGER) score "
			+ "FROM onet.education_training_experience a, lssoc.ssoc2015_onet2015 b "
			+ "where a.onetsoc_code=b.onet_code_2015 and scale_id='RL' and data_value>1.5 "
			+ "and b.ssoc_code='" + code.substring(0, 4) + "' "
			+ "group by b.ssoc_code, element_id, category having score >20 order by b.ssoc_code, score desc limit 2";
		//System.out.println(query);
		rs=stmt.executeQuery(query);
		while (rs.next()) {
			content += "\t\t<wcc_education>education_training_experience;" + rs.getString(2) + ";occupation-education</wcc_education>\n";
			//content += "\t\t<wcc_education>education_training_experience;occupation-education;" + rs.getString(2) + ";" + rs.getString(3) + "</wcc_education>\n";

		}


		query = "SELECT 'interests;', element_id, CONVERT(avg(data_value/7*100),UNSIGNED INTEGER) score, ';occupational-interests'"
			+ "FROM onet.interests a, lssoc.ssoc2015_onet2015 b where a.onetsoc_code=b.onet_code_2015 and scale_id='OI' "
			+ "and b.ssoc_code='" + code.substring(0, 4) + "' "
			+ "group by ssoc_code,element_id order by ssoc_code asc, score desc, element_id asc limit 3";
		//System.out.println(query);
		rs=stmt.executeQuery(query);
		while (rs.next()) {
			content += "\t\t<wcc_interest>" + rs.getString(1)  + rs.getString(2)  + rs.getString(4) + "</wcc_interest>\n";
			//content += "\t\t<wcc_interest>" + rs.getString(1)  + rs.getString(2) + ";" + rs.getString(3) + "</wcc_interest>\n";

		}


		query = "SELECT distinct 'interests;', a.element_id, CONVERT(avg(data_value/5*100),UNSIGNED INTEGER) score, ';occupation-work-context'"
			+ "FROM onet.work_context a, lssoc.ssoc2015_onet2015 b where a.onetsoc_code=b.onet_code_2015 and scale_id='CX' and  data_value>=3 "
			+ "and ssoc_code='" + code.substring(0, 4) + "' "
			+ "group by ssoc_code,element_id order by ssoc_code asc, score desc, element_id asc limit 30";
		//System.out.println(query);
		rs=stmt.executeQuery(query);
		while (rs.next()) {
			content += "\t\t<wcc_work_condition>" + rs.getString(1)  + rs.getString(2) + rs.getString(4) + "</wcc_work_condition>\n";
			//content += "\t\t<wcc_work_conditiont>" + rs.getString(1)  + rs.getString(2) + ";" + rs.getString(3) + "</wcc_work_conditiont>\n";

		}

		content +="\t\t</enriched_content>\n";

		return content;

	}



}