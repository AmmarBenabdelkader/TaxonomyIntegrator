/**
 * @author abenabdelkader
 *
 * URLReader.java
 * Aug 14, 2017
 */
package com.wccgroup.web.extrator;

/**
 * @author abenabdelkader
 *
 */
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.CellRangeAddressList;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import java.io.*;
// crawls site 'School of Digital Media and INFOCOM Technology'
public class learningTree {
	static HashMap<String,String> descipline=new HashMap<String,String>(); 
	public static void main(String[] args) throws Exception {

		String baseURI="https://www.learningtree.com";
		String fullRUI="https://www.learningtree.com/training-directory/";
		//mySkillsMapper(baseURI, fullRUI);
		courseExtractor();

		//String query = "SELECT distinct name_of_regulated_profession, translations FROM escov1.regulated_professions where WCC_code is null and translations in (select translation FROM taxonomies.regulated_certication) order by name_of_regulated_profession, translations";
		//getMappingCode(query);
	}
	public static void mySkillsMapper(String baseURI, String fullRUI) throws SQLException, ClassNotFoundException, IOException
	{
		String JDBC_DRIVER = "com.mysql.jdbc.Driver";
		String DB_URL = "jdbc:mysql://localhost/onet?useUnicode=true&characterEncoding=utf-8";
		String USER = "root";
		String PASS = "";
		Class.forName(JDBC_DRIVER);
		Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
		Statement stmt = conn.createStatement();
		
		URL url = new URL(fullRUI);
		HttpURLConnection httpcon = (HttpURLConnection) url.openConnection(); 
		httpcon.addRequestProperty("User-Agent", "Mozilla/4.76"); 
		BufferedReader in = new BufferedReader(new InputStreamReader(httpcon.getInputStream()));

		String inputLine;
		int i = 1;
		int j=1;
		int idx1=0;
		descipline=new HashMap<String,String>(); 

		StringBuffer query = new StringBuffer();
		String name,link, code, inst,sector,fee;
		name=inst=sector=fee=null;
		descipline=new HashMap<String,String>(); 
		int k=1;
		while ((inputLine = in.readLine()) != null && !inputLine.contains(">Technology Brands</h3>"));
		while ((inputLine = in.readLine()) != null && !inputLine.contains("<h2>")) {
			if (inputLine.contains("<li><a href=\"") && inputLine.contains("</a>")) {
				code=name=link=inst=sector=fee=null;
				//System.out.println(inputLine);
				name = inputLine.substring(inputLine.indexOf("\">") + 2, inputLine.indexOf("</a>"));
				link = baseURI + inputLine.substring(inputLine.indexOf("<a href=\"") + 9, inputLine.indexOf("\">"));
				System.out.println(j + "- " + name + ": " + link);
				descipline.put(name, link);
				j++;
				inputLine = in.readLine();
				//query.append("(\"" + code.replaceAll("\"", "'") + "\",\""   + name.replaceAll("\"", "'") + "\",\""  + link.replaceAll("\"", "'") + "\",\""  + inst.replaceAll("\"", "'") + "\",\""  + sector.replaceAll("\"", "'") + "\","  + (fee==null?"":fee.replaceAll(",", "")) + "),");
				//System.out.println(query.toString());
				String str = query.toString();
				//stmt.executeUpdate(str.substring(0, str.length()-1));

			}

		}
		

		in.close();
		HashMap<String,String> courses=new HashMap<String,String>(); 
		for (Map.Entry<String, String> entry : descipline.entrySet()) {
			query = new StringBuffer();
			query.append("insert into taxonomies.training_course_elt (code, name, descipline,link) values ");
			System.out.println(entry.getKey() + ": " + entry.getValue());
			url = new URL(entry.getValue().toString());
			
				httpcon = (HttpURLConnection) url.openConnection(); 
				 httpcon.addRequestProperty("User-Agent", "Mozilla/4.76"); 
				in = new BufferedReader(
					new InputStreamReader(httpcon.getInputStream()));



			while ((inputLine = in.readLine()) != null) {
				String contentLink=null;
				if (inputLine.contains("course-item")) {
					contentLink = baseURI + inputLine.substring(inputLine.indexOf("href=\"")+6,inputLine.indexOf("/\"")).trim();
					inputLine = in.readLine();
					inputLine = in.readLine();
					name=cleanTags(inputLine.trim());
					System.out.println("\t- " + name + ": " + contentLink );
					courses.put(name, contentLink);
					query.append("(\"" + k + "\", \"" + name + "\", \"" + entry.getKey() + "\", \"" + contentLink + "\")");
					//System.out.println("\n" + query.toString() );
					stmt.executeUpdate(query.toString());
					query = new StringBuffer();
					query.append("insert into taxonomies.training_course_elt (code, name, descipline,link) values ");
					k++;
				}
			}
		}
		in.close();
	}
	public static void courseExtractor() throws SQLException, ClassNotFoundException, IOException
	{
		String JDBC_DRIVER = "com.mysql.jdbc.Driver";
		String DB_URL = "jdbc:mysql://localhost/onet?useUnicode=true&characterEncoding=utf-8";
		String USER = "root";
		String PASS = "";
		Class.forName(JDBC_DRIVER);
		Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
		Statement stmt = conn.createStatement();
		Statement stmt2 = conn.createStatement();
		

		String inputLine;
		descipline=new HashMap<String,String>(); 

		String query;
		String name, code;
		query = "SELECT distinct name, link, descipline FROM taxonomies.training_course_elt limit 548";
		ResultSet rs = stmt.executeQuery(query);
		int k = 1;
		

		while (rs.next()) {
			//query = "insert into taxonomies.education (name,type,link) values (\"" + major + "\",\"Full-time Courses\",\""  + fullRUI + "\")";
			//stmt.executeUpdate(query);
			System.out.println(rs.getString(1) + ": " + rs.getString(2));
			URL url = new URL(rs.getString(2));
			
			HttpURLConnection httpcon = (HttpURLConnection) url.openConnection(); 
				 httpcon.addRequestProperty("User-Agent", "Mozilla/4.76"); 
				 BufferedReader in = new BufferedReader(
					new InputStreamReader(httpcon.getInputStream()));


			String desc,type,duration, min_req, content;
			code=name=desc=type=duration=min_req=content="";

			while ((inputLine = in.readLine()) != null) {
				if (inputLine.contains("<script")) {
					while ((inputLine = in.readLine()) != null && !inputLine.contains("</script>"));
				}
				if (inputLine!=null && inputLine.contains("<h1 class=\"coursename\">")) {

					name = inputLine.substring(inputLine.indexOf("<h1 class=\"coursename\">")+23,inputLine.indexOf("</h1>")).trim();
					System.out.println("\n\t" + k + "- Name: " + name); k++;
				}
				if (inputLine!=null && inputLine.contains("<h2>COURSE TYPE</h2>")) {

					inputLine = in.readLine();
					type = inputLine.substring(inputLine.indexOf("<h5>")+4,inputLine.indexOf("</h5>")).trim();
					System.out.println("\tType: " + type);
				}
				if (inputLine!=null && inputLine.contains("<h2>Course Number</h2>")) {
					inputLine = in.readLine();
					code = inputLine.substring(inputLine.indexOf("<h5>")+4,inputLine.indexOf("</h5>")).trim();
					System.out.println("\tCode: " + code );
				}
				if (inputLine!=null && inputLine.contains("<h2>Duration</h2>")) {
					inputLine = in.readLine();
					duration = inputLine.substring(inputLine.indexOf("<h5>")+4,inputLine.indexOf("</h5>")).trim();
					System.out.println("\tDuration: " + duration );
				}
				if (inputLine!=null && inputLine.contains("<p class=\"aboutcourse\">")) {
					while ((inputLine = in.readLine()) != null && !inputLine.contains("<div id=\"crsimp\">")) {
						desc += cleanTags(inputLine).trim();
					}
					System.out.println("\tDescription: " + desc );
				}
				if (inputLine!=null && (inputLine.contains("<h3>Requirements:</h3>") || inputLine.contains("Course Outline</h2>"))) {
					while ((inputLine = in.readLine()) != null && !inputLine.contains("</div>")) {
						min_req += cleanTags(inputLine).trim();
					}
					System.out.println("\tRequirements: " + min_req );
				}
				if (inputLine!=null && inputLine.contains("<h2>Course Content</h2>")) {
					while ((inputLine = in.readLine()) != null && !inputLine.contains("</div>")) {
						content += cleanTags(inputLine).trim();
					}
					System.out.println("\tContent: " + content );
				}
			}
			in.close();
			System.out.println("name.length: " + name.length() );
			if(name.length()>3){
				query = "insert into taxonomies.training_course_elt (code, name, type, descipline, link, duration, description, requirements,content) values ";
				query += "(\"" + code + "\", \"" + name + "\", \"" + type + "\", \"" +  rs.getString(3) 
				 + "\", \"" +  rs.getString(2) + "\", \"" +  duration + "\", \"" +  desc
				 + "\", \"" +  min_req + "\", \"" +  content
				 + "\")";
				System.out.println("\n" + query.toString() );
				stmt2.executeUpdate(query);
				//code=name=desc=type=duration=min_req=content="";
			}
		}
		rs.close();
		stmt.close();
		stmt2.close();
		conn.close();
		
	}
	public static void extractDesc(BufferedReader in, String major, int k) throws SQLException, ClassNotFoundException, IOException
	{
		String desc;

		desc="\t";
		String inputLine = in.readLine();
		while (inputLine!= null && !inputLine.contains("</div>") && !inputLine.contains(">WSQ Qualification Pathways<") && !inputLine.contains("<table ")) {
			if (!inputLine.contains("<a href="))
				desc += cleanTags(inputLine);

			inputLine = in.readLine();


		}
		desc.replaceAll("\n\t\n\t", "\n\t");
		desc.replaceAll("\t\n\t", "\t");
		desc.replaceAll("\t\n", "");
		System.out.println(desc);
		while (inputLine!= null && !inputLine.contains(">WSQ Qualification Pathways<")) {
			inputLine = in.readLine();			
		}
		if (inputLine!= null && inputLine.contains(">WSQ Qualification Pathways<")) {
			//System.out.println("\tWSQ Qualification Pathways:");
			while (inputLine!= null && !inputLine.contains("</tr></tbody></table>")) {
				if (inputLine.contains("style=\"width: 25.0%;\">")) {
					if (inputLine.contains("<p>"))
						System.out.print(major + "\t\t" + inputLine.substring(inputLine.indexOf("<p>")+3, inputLine.indexOf("</")));
					else
						System.out.print(major + "\t\t" + inputLine.substring(inputLine.indexOf(">")+1, inputLine.indexOf("</")));
				}
				if (inputLine.contains("style=\"width: 75.0%;\">")) {
					if (inputLine.contains("<p>"))
						System.out.println("\t" + inputLine.substring(inputLine.indexOf("<p>")+3, inputLine.indexOf("</")));
					else
						System.out.println("\t" + inputLine.substring(inputLine.indexOf(">")+1, inputLine.indexOf("</")));
				}


				inputLine = in.readLine();
			}					
		}

	}
	@SuppressWarnings("deprecation")
	public static void getMappingCode(String query) throws SQLException, ClassNotFoundException, IOException
	{
		String JDBC_DRIVER = "com.mysql.jdbc.Driver";
		String DB_URL = "jdbc:mysql://localhost/onet?useUnicode=true&characterEncoding=utf-8";
		String USER = "root";
		String PASS = "";
		Class.forName(JDBC_DRIVER);
		Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(query);
		int counter = 1;
		int j = 1;
		System.out.print("\nExtracting ontology data: " + "\n\t" );

		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet realSheet = workbook.createSheet("Sheet xls");
		FileOutputStream fos = new FileOutputStream("c:\\data\\regulated_professions_ontology-based_new.xls");



		while (rs.next()) {
			HSSFRow row = realSheet.createRow(j);
			HSSFCell cell1 = row.createCell(0); cell1.setCellValue(rs.getString(1));
			HSSFCell cell2 = row.createCell(2); cell2.setCellValue(rs.getString(2));

			try
			{
				System.out.println(rs.getString(1) + ": ");
				URL url = new URL("http://demos.savannah.wcc.nl:14080/semanticsearch/v1/occupationtitles/text?text=" + rs.getString(1).replaceAll(" ", "%20").trim() + "&sort=sort");
				URLConnection yc = url.openConnection();
				BufferedReader in;
				in = new BufferedReader(new InputStreamReader(
					yc.getInputStream()));
				String line;
				String name="";
				String code = "";
				int limits = 10;
				int i=3;
				while ((line = in.readLine()) != null && i<=limits) {
					if (line.contains("\"id\" :")){
						code = "#" + line.substring(12, line.length()-2);
						//enrich = getEnrichment(line.substring(12, line.length()-2));
						line = in.readLine();
						if (line.contains("\"name\" :"))
							name = line.substring(14, line.length()-2);
						line = in.readLine().trim();
						//System.out.println(line);
						if (line.contains("\"score\" :"))
							code += "#" + line.substring(10, line.length());

						name += " " + code;
						HSSFCell cell = row.createCell(i); cell.setCellValue(name);

						System.out.println("\t\t" + name);
						i++;

					}
				}

				in.close();
				if (rs.getString(2)!=null) {
					System.out.println(rs.getString(2) + ": ");
					url = new URL("http://demos.savannah.wcc.nl:14080/semanticsearch/v1/occupationtitles/text?text=" + rs.getString(2).replaceAll(" ", "%20").trim() + "&sort=sort");
					yc = url.openConnection();
					in = new BufferedReader(new InputStreamReader(
						yc.getInputStream()));
					name="";
					code = "";
					limits = 23;
					i=13;
					while ((line = in.readLine()) != null && i<=limits) {
						if (line.contains("\"id\" :")){
							code = "#" + line.substring(12, line.length()-2);
							//enrich = getEnrichment(line.substring(12, line.length()-2));
							line = in.readLine();
							if (line.contains("\"name\" :"))
								name = line.substring(14, line.length()-2);
							line = in.readLine().trim();
							//System.out.println(line);
							if (line.contains("\"score\" :"))
								code += "#" + line.substring(10, line.length());

							name += " " + code;
							HSSFCell cell = row.createCell(i); cell.setCellValue(name);

							System.out.println("\t\t" + name);
							i++;

						}
					}

					in.close();
				}
			}
			catch (IOException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			j++;

		}
		workbook.write(fos);
		fos.flush();
		fos.close();
	}

	public static String cleanTags(String text) throws SQLException, ClassNotFoundException, IOException
	{
		text = text.replaceAll("<li>", "\n\t\t . ").replaceAll("</li>", "");
		text = text.replaceAll("<p>", "\n\t").replaceAll("</p>", ":\n\t");
		text = text.replaceAll("<h2>", "\n\t").replaceAll("</h2>", "\n");
		String temp="";
		if (!text.contains("<"))
			return text;

		while (text.contains("<")) {
			//System.out.println(text);;
			temp += text.substring(0, text.indexOf("<"));
			text = text.substring(text.indexOf(">")+1, text.length());
			//text = text.substring(0, text.indexOf("<")) + text.substring(text.indexOf(">")+1, text.length()) ;
		}
		return temp.trim();
	}

}
