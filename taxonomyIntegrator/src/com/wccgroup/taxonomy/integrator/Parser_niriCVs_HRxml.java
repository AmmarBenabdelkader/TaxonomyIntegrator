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
import java.sql.*;
public class Parser_niriCVs_HRxml extends DefaultHandler 
{
	// JDBC driver name and database URL
	static String JDBC_DRIVER = "";
	static String DB_URL = "";

	//  Database credentials
	static String USER = "";
	static String PASS = "";
	static Connection conn = null;
	static Statement stmt = null;
	static Statement stmt2 = null;
	static String[][] CVs = {
		{"DocID","0"},
		{"FirstName","1"},
		{"LastName","2"},
		{"StreetAndNumber","3"},
		{"City","4"},
		{"Postcode","5"},
		{"Country","6"},
		{"EliseDate","7"},
		{"FromDate","8"},
		{"ToDate","9"},
		{"Title","10"},
		{"Organization","11"},
		{"Description","12"},
		};
	static String[][] education = {
		{"DegreeName",""},
		{"StartDate",""},
		{"EndDate",""},
		{"SchoolName",""},
		};
	static Map<String, Boolean> elmtStaus = new HashMap<String, Boolean>();
	static int counter;
	static String FullName;
	static String cv_id;
	static Map<String, String> skills = new HashMap<String, String>();
	static String top = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\t<Resume lang=\"en\"  xmlns=\"http://actonomy.com/hrxml/2.5\">\n";
	static String bottom = "\t</Resume>";
	static Map<String, String> jobtitles = new HashMap<String, String>();

	static StringBuilder cv = new StringBuilder();
	static StringBuilder workHistory = new StringBuilder();
	static StringBuilder competencies = new StringBuilder();
	static StringBuilder educations = new StringBuilder();
	static StringBuilder enrichedContent = new StringBuilder();
	static String outputFile = "";
	static BufferedWriter writer;
	static int i=1;
	static public void main(String[] args) throws Exception {
		String filename = null;

		JDBC_DRIVER = "com.mysql.jdbc.Driver";
		DB_URL = "jdbc:mysql://localhost/onet?useUnicode=true&characterEncoding=utf-8";
		USER = "root";
		PASS = "";
		Class.forName(JDBC_DRIVER);
		Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
		stmt = conn.createStatement();
		stmt2 = conn.createStatement();


/*		for (int i = 0; i < args.length; i++) {
			filename = args[i];
			if (i != args.length - 1) {
				usage();
			}
		}

		if (filename == null) {
			usage();
		} 
*/		for (int i=0; i<CVs.length; i++)
			elmtStaus.put(CVs[i][0], false);
		readProperties();
		try
		{
			//STEP 2: Register JDBC driver
			Class.forName(JDBC_DRIVER);

			//STEP 3: Open a connection
			//conn = DriverManager.getConnection(DB_URL, USER, PASS);
			//stmt = conn.createStatement();
			//Statement stmt = conn.createStatement();

		SAXParserFactory spf = SAXParserFactory.newInstance();
		spf.setNamespaceAware(true);
		SAXParser saxParser = spf.newSAXParser();
		XMLReader xmlReader = saxParser.getXMLReader();
		xmlReader.setContentHandler(new Parser_niriCVs_HRxml());
		String path = "\\\\savannah\\home\\abenabdelkader\\Documents\\projects\\Singapore\\data\\ManpowerCVs\\";
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();

	    for (i = 0; i < listOfFiles.length; i++) {
		      if (listOfFiles[i].isFile()) {
		    	  filename = listOfFiles[i].getName();
		        System.out.println((i+1) + ": " + filename);
		        UserHandler userhandler = new UserHandler();
		        outputFile = "\\\\savannah\\home\\abenabdelkader\\Documents\\projects\\Singapore\\data\\ManpowerCVs\\" + listOfFiles[i].getName();
		        if (!filename.contains("#")) {
					writer = new BufferedWriter(new FileWriter(new File("\\\\savannah\\home\\abenabdelkader\\Documents\\projects\\Singapore\\data\\ManpowerCVs_output" + String.valueOf(i+1))));
			        saxParser.parse(convertToFileURL(path + "\\" + filename), userhandler); 
			        writer.close();
		        }
		      }
		    }


		//STEP 6: Clean-up environment
		//stmt.close();
		conn.close();

	}
	catch (SQLException se)
	{
		//Handle errors for JDBC
		se.printStackTrace();
	}
	}
	private static class UserHandler extends DefaultHandler {

		boolean SkillNamePersonal = false;
		boolean educationEvent = false;
		boolean educationTitle = false;
		boolean educationStart = false;
		boolean educationEnd = false;
		boolean educationOrg = false;

		@Override
		public void startElement(String uri, 
			String localName, String qName, Attributes attributes)
				throws SAXException {

			if (qName.equalsIgnoreCase("SkillNamePersonal")) {
				SkillNamePersonal = true;
			}
			if (qName.equalsIgnoreCase("EducationEvent")) {
				educationEvent = true;
			}
			if (educationEvent && qName.equalsIgnoreCase("Title")) {
				educationTitle = true;education[0][1]="";
			}
			if (educationEvent && qName.equalsIgnoreCase("FromDate")) {
				educationStart = true;education[1][1]="";
			}
			if (educationEvent && qName.equalsIgnoreCase("ToDate")) {
				educationEnd = true;education[2][1]="";
			}
			if (educationEvent && qName.equalsIgnoreCase("Organization")) {
				educationOrg = true;education[3][1]="";
			}
			for (int i=0; i<CVs.length; i++) {
				if (qName.equalsIgnoreCase(CVs[i][0])) 
					elmtStaus.put(CVs[i][0], true);
			}
		}

		@Override
		public void endElement(String uri, 
			String localName, String qName) throws SAXException {
			try
			{
				if (qName.equalsIgnoreCase("root")) {
				competencies.append("\t\t<Qualifications>\n");
				for (Map.Entry<String, String> entry : skills.entrySet()) {
						competencies.append("\t\t\t<Competency name=\"" + entry.getKey() + "\">\n\t\t\t</Competency>\n");
					}
				competencies.append("\t\t</Qualifications>\n");
					cv.append(top);
					cv.append("\t\t<StructuredXMLResume>\n\t\t\t<ContactInfo>\n");
					cv.append("\t\t\t\t<id>" + (i+1) + "</id>\n");
					cv.append("\t\t\t\t<PersonName>\n\t\t\t\t\t<FormattedName>" + CVs[1][1]  + " " + CVs[2][1] + "</FormattedName>\n"
						+ "\t\t\t\t</PersonName>\n\t\t\t\t<ContactMethod />\n\t\t\t</ContactInfo>\n");
					cv.append("\t\t\t<EmploymentHistory>\n");
					cv.append(workHistory.toString());
					cv.append("\t\t</EmploymentHistory>\n");
					cv.append(competencies.toString());
					cv.append("\t\t<EducationHistories>\n");
					cv.append(educations.toString());
					cv.append("\t\t</EducationHistories>\n");
					cv.append("\t\t</StructuredXMLResume>\n");
					cv.append("\t\t\t<UserArea><PersonDescriptors><DemographicDescriptors>"
						+ "<BirthDate>" + CVs[7][1] + "</BirthDate>\n\t\t\t<Telephone>" + "</Telephone>\n"
						+ "\t\t\t<Email>" + "</Email>\n"
						+ "\t\t\t<Address>\n\t\t\t\t<Country>" + CVs[6][1] + "</Country>\n"
						+ "\t\t\t\t<PostalCode>" + CVs[5][1] + "</PostalCode>\n"
						+ "\t\t\t\t<City>" + CVs[4][1] + "</City>\n"
						+ "\t\t\t</Address>\n<Nationality/>"
						+ "</DemographicDescriptors>");
					cv.append("\t\t<BiologicalDescriptors>\n"
						+ "<DateOfBirth>" + CVs[7][1] + "</DateOfBirth></BiologicalDescriptors></PersonDescriptors></UserArea>");

			            cv.append(bottom);
					writer.write(cv.toString());
					cv = new StringBuilder();
					competencies = new StringBuilder();
					educations = new StringBuilder();
					workHistory = new StringBuilder();
					//System.out.println(cv);
			}
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				
			if (qName.equalsIgnoreCase("EmploymentEvent")) {
				jobtitles.put(CVs[10][1], null);
				enrichedContent = new StringBuilder();
				try
				{
					getTaxonomyContent(CVs[10][1]);
				}
				catch (ClassNotFoundException | SQLException | IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				workHistory.append("\t\t\t<EmployerOrg employerOrgType=\"soleEmployer\">\n\t\t\t\t"
					+ "<EmployerOrgName>" + CVs[11][1] + "</EmployerOrgName><PositionHistory>"
					+ "<StartDate><YearMonth>" + CVs[8][1] + "</YearMonth></StartDate>" 
					+ "<EndDate><YearMonth>" + CVs[9][1] + "</YearMonth></EndDate>" 
					+ "\t\t\t\t<Title>" + CVs[10][1] + "</Title>\n"
					+ "\t\t\t\t<Description>" + CVs[12][1].replaceAll("\n", " ") + "</Description>\n"
					+ enrichedContent.toString()
					+ "\t\t\t</PositionHistory></EmployerOrg>\n");
			}
			if (qName.equalsIgnoreCase("EducationEvent")) {
				educations.append("\t\t\t<EducationHistory><SchoolOrInstitution><SchoolName>\n\t\t\t\t" + education[3][1] + "</SchoolName></SchoolOrInstitution>\n");
				educations.append("\t\t\t<DegreeName><Degree>\n\t\t\t\t" + education[0][1] + "</Degree></DegreeName>\n");
				educations.append("\t\t\t</EducationHistory>\n");
				educationEvent = false;
			}
		}



		@Override
		public void characters(char ch[], 
			int start, int length) throws SAXException {
			String tmp="";
			for (int i=0; i<CVs.length; i++) {
				if (elmtStaus.get(CVs[i][0])) {
					tmp = new String(ch, start, length);
					CVs[i][1]= tmp;
					elmtStaus.put(CVs[i][0],false);
				}
			}
			if (SkillNamePersonal) {
				tmp = new String(ch, start, length);
				skills.put(tmp.replaceAll("\"", "'"), "skill");
				SkillNamePersonal = false;
			}
			
			if (educationEvent) {
				tmp = new String(ch, start, length).replaceAll("\"", "'");
				if (educationTitle) {
					education[0][1]= tmp;
					educationTitle = false;
				}
				if (educationStart) {
					education[1][1]= tmp;
					educationStart = false;
				}
				if (educationEnd) {
					education[2][1]= tmp;
					educationEnd = false;
				}
				if (educationOrg) {
					education[3][1]= tmp;
					educationOrg = false;
				}
			}
			
		}
	}
	private static void usage() {
		System.err.println("Usage: niriParser <file.xml>");
		System.err.println("       -usage or -help = this message");
		System.exit(1);
	}
	private static String convertToFileURL(String filename) {
		String path = new File(filename).getAbsolutePath();
		if (File.separatorChar != '/') {
			path = path.replace(File.separatorChar, '/');
		}

		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		return "file:" + path;
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
	public static void getTaxonomyContent(String jobTitle) throws ClassNotFoundException, SQLException, IOException
	{
		boolean content=false;
		String query = "select code from lssoc.occupation where name='" + jobTitle + "'";
		ResultSet rs = stmt.executeQuery(query);
		String tmp="";
		for (; rs.next();)
		{
			tmp += "\n\t\t\t\t\t<enriched_content>\n";
			query = "select ssoc_code, a.element_id, b.element_name, score, description, 'O*NET' from lssoc.occupational_iterests a, onet.content_model_reference b where a.element_id=b.element_id and ssoc_code='" + rs.getString(1).substring(0, 4) + "' order by score desc";
			ResultSet rs2 = stmt2.executeQuery(query);
			tmp += "\t\t\t\t\t\t<ExternalOccupationInterests>\n";
			for (; rs2.next();)
			{
				tmp += "\t\t\t\t\t\t\t<ExternalOccupationInterest>\n";
				tmp += "\t\t\t\t\t\t\t\t<id>" + rs2.getString(2) + "</id>\n";
				tmp += "\t\t\t\t\t\t\t\t<code>" + rs2.getString(2).substring(0, 1) + "</code>\n";
				tmp += "\t\t\t\t\t\t\t\t<Name>" + rs2.getString(3) + "</Name>\n";
				tmp += "\t\t\t\t\t\t\t\t<score>" + rs2.getString(4) + "</score>\n";
				tmp += "\t\t\t\t\t\t\t\t<description>" + rs2.getString(5) + "</description>\n";
				tmp += "\t\t\t\t\t\t\t\t<source>" + rs2.getString(6) + "</source>\n";
				tmp += "\t\t\t\t\t\t\t</ExternalOccupationInterest>\n";
				content=true;
			}
			tmp += "\t\t\t\t\t\t</ExternalOccupationInterests>\n";

			query = "select ssoc_code, a.education, b.name, score, 'O*NET' from lssoc.occupational_education a, lonet.education_training_experience b where a.education=b.code and ssoc_code='" + rs.getString(1).substring(0, 4) + "' order by score desc";
			rs2 = stmt2.executeQuery(query);
			tmp += "\t\t\t\t\t\t<ExternalOccupationEducations>\n";
			for (; rs2.next();)
			{
				tmp += "\t\t\t\t\t\t\t<ExternalOccupationEducation>\n";
				tmp += "\t\t\t\t\t\t\t\t<id>" + rs2.getString(2) + "</id>\n";
				tmp += "\t\t\t\t\t\t\t\t<code>" + rs2.getString(2) + "</code>\n";
				tmp += "\t\t\t\t\t\t\t\t<Name>" + rs2.getString(3) + "</Name>\n";
				tmp += "\t\t\t\t\t\t\t\t<score>" + rs2.getString(4) + "</score>\n";
				tmp += "\t\t\t\t\t\t\t\t<source>" + rs2.getString(5) + "</source>\n";
				tmp += "\t\t\t\t\t\t\t</ExternalOccupationEducation>\n";
				content=true;
			}
			tmp += "\t\t\t\t\t\t</ExternalOccupationEducations>\n";

			query = "SELECT code, name, description, type, `skill-type`, 'ESCO' FROM ssoc_temp.`occupation-esco-skills` a, escov08.skill b where a.skill=b.code and occupation='" + rs.getString(1) + "' order by name";
			rs2 = stmt2.executeQuery(query);
			tmp += "\t\t\t\t\t\t<ExternalCompetencies>\n";
			for (; rs2.next();)
			{
				tmp += "\t\t\t\t\t\t\t<ExternalCompetency>\n";
				tmp += "\t\t\t\t\t\t\t\t<id>" + rs2.getString(1) + "</id>\n";
				tmp += "\t\t\t\t\t\t\t\t<code>" + rs2.getString(1) + "</code>\n";
				tmp += "\t\t\t\t\t\t\t\t<Name>" + rs2.getString(2) + "</Name>\n";
				tmp += "\t\t\t\t\t\t\t\t<description>" + rs2.getString(3) + "</description>\n";
				tmp += "\t\t\t\t\t\t\t\t<type>" + rs2.getString(4) + "</type>\n";
				tmp += "\t\t\t\t\t\t\t\t<relationshipType>" + rs2.getString(5) + "</relationshipType>\n";
				tmp += "\t\t\t\t\t\t\t\t<source>" + rs2.getString(6) + "</source>\n";
				tmp += "\t\t\t\t\t\t\t</ExternalCompetency>\n";
				content=true;
			}
			tmp += "\t\t\t\t\t\t</ExternalCompetencies>\n";

			query = "select ssoc_code, a.element_id, b.element_name, score, description, 'O*NET' from lssoc.work_context a, onet.content_model_reference b where a.element_id=b.element_id and ssoc_code='" + rs.getString(1).substring(0, 4) + "' order by score desc";
			rs2 = stmt2.executeQuery(query);
			tmp += "\t\t\t\t\t\t<ExternalWorkConditions>\n";
			for (; rs2.next();)
			{
				tmp += "\t\t\t\t\t\t\t<ExternalWorkCondition>\n";
				tmp += "\t\t\t\t\t\t\t\t<id>" + rs2.getString(2) + "</id>\n";
				tmp += "\t\t\t\t\t\t\t\t<code>" + rs2.getString(2) + "</code>\n";
				tmp += "\t\t\t\t\t\t\t\t<Name>" + rs2.getString(3) + "</Name>\n";
				tmp += "\t\t\t\t\t\t\t\t<score>" + rs2.getString(4) + "</score>\n";
				tmp += "\t\t\t\t\t\t\t\t<description>" + rs2.getString(5) + "</description>\n";
				tmp += "\t\t\t\t\t\t\t\t<source>" + rs2.getString(6) + "</source>\n";
				tmp += "\t\t\t\t\t\t\t</ExternalWorkCondition>\n";
				content=true;
			}
			tmp += "\t\t\t\t\t\t</ExternalWorkConditions>\n";

		rs2.close();
		}
		tmp += "\t\t\t\t\t</enriched_content>\n";
		if (content) {
			enrichedContent.append(tmp);
			//System.out.print("\t\t\t ------>\n\t" + query);
		}
		rs.close();

	}


}
