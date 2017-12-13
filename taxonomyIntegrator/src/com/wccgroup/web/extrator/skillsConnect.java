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
import java.sql.*;
import java.util.*;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.CellRangeAddressList;
import java.io.*;
// crawls site 'School of Digital Media and INFOCOM Technology'
public class skillsConnect {
static HashMap<String,String> descipline=new HashMap<String,String>(); 
	public static void main(String[] args) throws Exception {

		String baseURI="http://www.ssg.gov.sg";
		String fullRUI="http://www.ssg.gov.sg/wsq/wsq-for-individuals.html";
		educationMapper(baseURI, fullRUI);

		//String query = "SELECT distinct name_of_regulated_profession, translations FROM escov1.regulated_professions where WCC_code is null and translations in (select translation FROM taxonomies.regulated_certication) order by name_of_regulated_profession, translations";
		//getMappingCode(query);
	}
	public static void educationMapper(String baseURI, String fullRUI) throws SQLException, ClassNotFoundException, IOException
	{
/*		String JDBC_DRIVER = "com.mysql.jdbc.Driver";
		String DB_URL = "jdbc:mysql://localhost/onet?useUnicode=true&characterEncoding=utf-8";
		String USER = "root";
		String PASS = "";
		Class.forName(JDBC_DRIVER);
		Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
		Statement stmt = conn.createStatement();
*/
		 URL url = new URL(fullRUI);
		HttpURLConnection httpcon = (HttpURLConnection) url.openConnection(); 
		 httpcon.addRequestProperty("User-Agent", "Mozilla/4.76"); 
		BufferedReader in = new BufferedReader(
			new InputStreamReader(httpcon.getInputStream()));

		String inputLine;
		int i = 1;
		String title,overviewLink, counts, contentLink,major;
		title=major=null;
		int j=1;
		Map<String, String> professions = new HashMap<String, String>();
		int idx1=0;
		descipline=new HashMap<String,String>(); 
		while ((inputLine = in.readLine()) != null) {
			//System.out.println(inputLine);\
			if (inputLine.contains("\">Full-time Courses</a>")) {
				System.out.println("Full-time Courses\n");
				while (!inputLine.contains("\">Academic Schools</a>")) {
					inputLine = in.readLine();
					if (inputLine.contains("class=\"cursor \">")) {
						idx1 = inputLine.indexOf("class=\"cursor \">") + 16;
						major = inputLine.substring(idx1, inputLine.indexOf("</a>"));
						System.out.println("\t" + major);
					}
					if (inputLine.contains("?WCM_GLOBAL_CONTEXT=\">")) {
						idx1 = inputLine.indexOf("WCM_GLOBAL_CONTEXT=\">") + 21;
						title = inputLine.substring(idx1, inputLine.indexOf("</a>"));
						System.out.println("\t\t" + title);
						overviewLink = baseURI + inputLine.substring(inputLine.indexOf("a href=\"") + 8, inputLine.indexOf("\">"));
						descipline.put(title, overviewLink);
						//	System.out.println("\t\t\tLink: " + link);
					j++;
					}

				}  

			}

/*			if (inputLine.contains("\">Academic Schools</a>")) {
				System.out.println("Academic Schools\n");
				while (!inputLine.contains("\">Diploma-Plus</a>")) {
					inputLine = in.readLine();
					if (inputLine.contains("=\"_blank\">")) {
						idx1 = inputLine.indexOf("=\"_blank\">") + 10;
						System.out.println("\t" + inputLine.substring(idx1, inputLine.indexOf("</a>")));
					}

					j++;
				}  

			}
*/			i++;
		}

		in.close();
		String code, name, desc;
		Boolean ch=false;
		
		int k = 1;
		for (Map.Entry<String, String> entry : descipline.entrySet()) {
			if (entry.getKey().equalsIgnoreCase("Civil Engineering with Business"))
				continue;
			//i++;
			String query = "insert into taxonomies.education (name,type,link) values (\"" + major + "\",\"Full-time Courses\",\""  + fullRUI + "\")";
			//stmt.executeUpdate(query);
			url = new URL(entry.getValue().toString());
			
				httpcon = (HttpURLConnection) url.openConnection(); 
				 httpcon.addRequestProperty("User-Agent", "Mozilla/4.76"); 
				in = new BufferedReader(
					new InputStreamReader(httpcon.getInputStream()));


			j=1;

			while ((inputLine = in.readLine()) != null) {
				while (!inputLine.contains("<h1")) {
					inputLine = in.readLine();
				}
				desc="\t";
				ch=false;
				contentLink=null;
				inputLine = in.readLine();
				if (inputLine==null || inputLine.indexOf("(")<1)
					break;
				name=inputLine.substring(0,inputLine.indexOf("(")).trim();
				code = inputLine.substring(inputLine.indexOf("(")+1,inputLine.indexOf(")")).trim();
				System.out.println(k + "- Course: " + code + ": " + name + "(" + entry.getKey() + ")");
				inputLine = in.readLine();
				while (!inputLine.contains("<h1>")) {
					inputLine = in.readLine();
					if (inputLine.contains("Entry Requirements")) {
						inputLine = in.readLine();
						inputLine = in.readLine();
						inputLine = in.readLine();
						contentLink = inputLine.substring(9, inputLine.indexOf("\">")).replace("&amp;", "&");
						//System.out.println(contentLink);
					}
					
					
				}
				System.out.println("Overview:");
				desc += cleanTags(inputLine.substring(inputLine.indexOf("</h1>")+5,inputLine.length()));
				//inputLine = in.readLine();
				while (!inputLine.contains("</ul>") || !ch) {
					inputLine = in.readLine();
					if (inputLine.contains("Course Highlights"))
						ch=true;
					desc += cleanTags(inputLine);
					
				}
				
				desc += "\n";
				desc.replaceAll("\n\n", "\n");
				desc.replaceAll("\t\t\n", "");
				//desc.replaceAll("Course Highlights", "\n\tCourse Highlights:\n");
				System.out.println(desc);
				query = "insert into taxonomies.descipline (code,name,altName,description,major,link) values (\"" + code + "\",\""   + entry.getKey() + "\",\""   + name + "\",\""   + desc.replaceAll("\"", "'") + "\",\""     + major + "\",\""  + entry.getValue() + "\")";
				//stmt.executeUpdate(query);
				inputLine = in.readLine();
				if (contentLink!=null) { 

					URL suburl = new URL(baseURI+contentLink);
					
					httpcon = (HttpURLConnection) suburl.openConnection(); 
					 httpcon.addRequestProperty("User-Agent", "Mozilla/4.76"); 
					 BufferedReader subin = new BufferedReader(
						new InputStreamReader(httpcon.getInputStream()));

					String line="";
					String mcode,mname,mdesc,mhours;
					while ((line = subin.readLine()) != null) {
						while (!line.contains("Total Hours")) {
							line = subin.readLine();

						}
						subin.readLine();
						while (line!=null && !line.contains("</table>") ) {
							mcode=mname=mdesc=mhours="";
							while (!subin.readLine().contains("<td>") );
							mcode=subin.readLine();
							//System.out.print("\t\t" + subin.readLine() + ": ");
							
							//System.out.println(subin.readLine());
							
							while ((line = subin.readLine()) != null && !line.contains("<a target")  && !line.contains("<a href=") );
							if (line.contains("</a>"))
								mname = line.substring(line.indexOf("\">")+2, line.indexOf("</a>")).trim();
							else
								mname = subin.readLine();
							//System.out.print(subin.readLine() + ": ");
							
							if(line==null)
								break;
							
							while (!subin.readLine().contains("<div ") );
							mdesc = subin.readLine();
							//System.out.print(subin.readLine());
							
							while ((line = subin.readLine()) != null && !line.contains("<td>") );
							mhours = subin.readLine();
							//System.out.println(subin.readLine());
							
							if (mhours!=null) {
								System.out.println("\t\t" + mcode + ": " + mname + ": " + mdesc + ":" + mhours);
								query = "insert into taxonomies.course (code,name,description,hours,link) values (\"" + mcode + "\",\""   + mname + "\",\""  + mdesc + "\",\""  + mhours + "\",\""  + contentLink + "\")";
								//stmt.executeUpdate(query);
								query = "insert into taxonomies.descipline_courses values (\"" + code + "\",\""   + mcode + "\")";
								//stmt.executeUpdate(query);
							}
							line = subin.readLine();
						}
					}
					subin.close();
					System.out.println();
					j++;
				}  

				//i++;
				break;
			}

			in.close();
			k++;
			
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
/*		if (text.length()>500)
			text = text.substring(0, 500);

*/
		text = text.replaceAll("<li>", "\n\t\t . ").replaceAll("</li>", "");
		text = text.replaceAll("<h3>", "\n\t").replaceAll("</h3>", ":\n\t");
		String temp="";
        
        while (text.contains("<")) {
        	//System.out.println(text);;
        	temp += text.substring(0, text.indexOf("<"));
        	text = text.substring(text.indexOf(">")+1, text.length());
			   //text = text.substring(0, text.indexOf("<")) + text.substring(text.indexOf(">")+1, text.length()) ;
	}
        return temp.replaceAll("&nbsp;", "");
	}

}
