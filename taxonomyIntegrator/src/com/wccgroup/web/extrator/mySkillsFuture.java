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
public class mySkillsFuture {
	static HashMap<String,String> descipline=new HashMap<String,String>(); 
	public static void main(String[] args) throws Exception {

		String baseURI="https://www.myskillsfuture.sg";
		String fullRUI="https://www.myskillsfuture.sg/content/portal/en/training-exchange/course-directory.html?fq=Course_Supp_Period_To_1%3A%5B2017-12-12T00%3A00%3A00Z%20TO%20*%5D&fq=IsDisplaySFC%3Atrue&q=*%3A*&sort=Course_Title_facet%20asc%2CCourse_SEO_Name%20asc";
		mySkillsMapper(baseURI, fullRUI);

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

		File file = new File("C:\\data\\singapore\\courses\\SGTrainCourse.html");
		//File file = new File("C:\\data\\singapore\\courses\\myFutureSkills.html");
		FileReader fileReader = new FileReader(file);
		BufferedReader in = new BufferedReader(fileReader);
		StringBuffer query = new StringBuffer();
		String inputLine;
		String name,link, code, inst,sector,fee;
		name=inst=sector=fee=null;
		int j=1;
		Map<String, String> professions = new HashMap<String, String>();
		descipline=new HashMap<String,String>(); 
		query.append("insert into taxonomies.training_course3 (code,name,link, institution, sector, fee) values ");
		while ((inputLine = in.readLine()) != null) {
			if (inputLine.contains("<div data-bind=\"attr: { title: courseTitle }\"") && inputLine.contains("title=\"")) {
				query = new StringBuffer();
				query.append("insert into taxonomies.training_course3 (code,name,link, institution, sector, fee) values ");
				code=name=link=inst=sector=fee=null;
				//System.out.println(inputLine);
				name = inputLine.substring(inputLine.indexOf("title=\"") + 7, inputLine.indexOf("\">"));
				System.out.println(j + "- " + name);
				j++;
				inputLine = in.readLine();
				//System.out.println("Technical Skills and Competencies\n");
				while (!inputLine.contains("<div data-bind=\"attr: { title: courseTitle }\"")) {
					if (inputLine.contains("<a href=\"")) {
						link = baseURI + inputLine.substring(inputLine.indexOf("<a href=\"") + 9, inputLine.indexOf("\" target="));
						System.out.println("\t" + link);
					}
					if (inputLine.contains("EXT_Course_Ref_Nos[0]")) {
						//System.out.println(inputLine);
						code = inputLine.substring(inputLine.indexOf("EXT_Course_Ref_Nos[0]") + 23, 
							inputLine.indexOf("</span>"));
						System.out.println("\t" + code);
					}
					if (inputLine.contains("title : organisationNameTitles[0]")) {
						inst = inputLine.substring(inputLine.indexOf("\">") + 2, inputLine.indexOf("</a>"));
						System.out.println("\t" + inst);
					}
					if (inputLine.contains("attr:{title: areaOfTrainingsFull}")) {
						sector = inputLine.substring(inputLine.indexOf("\">") + 2, inputLine.indexOf("</div>"));
						System.out.println("\t" + sector);
					}
					if (inputLine.contains("text: Tol_Cost_of_Trn_Per_Trainee")) {
						fee = inputLine.substring(inputLine.indexOf("\">") + 2, inputLine.indexOf("</span>"));
						System.out.println("\t$" + fee);
					}
					inputLine = in.readLine();

				}  
				query.append("(\"" + code.replaceAll("\"", "'") + "\",\""   + name.replaceAll("\"", "'") + "\",\""  + link.replaceAll("\"", "'") + "\",\""  + inst.replaceAll("\"", "'") + "\",\""  + sector.replaceAll("\"", "'") + "\","  + (fee==null?"":fee.replaceAll(",", "")) + "),");
				System.out.println(query.toString());
				String str = query.toString();
				stmt.executeUpdate(str.substring(0, str.length()-1));

			}

		}
		

		in.close();
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
