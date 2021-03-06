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
public class individualWSQ {
	static HashMap<String,String> descipline=new HashMap<String,String>(); 
	public static void main(String[] args) throws Exception {

		String baseURI="http://www.ssg.gov.sg";
		String fullRUI="http://www.ssg.gov.sg/wsq/wsq-for-individuals.html";
		//educationMapper(baseURI, fullRUI);
		proccessHTTP(fullRUI);

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
			if (inputLine.contains("width=\"50%\"><ul>")) {
				System.out.println("Technical Skills and Competencies\n");
				while (!inputLine.contains("valign=\"top\"><ul>")) {
					inputLine = in.readLine();
					if (inputLine.contains("<li><a href=")) {
						idx1 = inputLine.indexOf("<li><a href=") + 13;
						overviewLink = baseURI + inputLine.substring(idx1, inputLine.indexOf("\">"));
						major = inputLine.substring(inputLine.indexOf("\">")+2, inputLine.indexOf("</a>"));
						descipline.put(major, overviewLink);
						System.out.println(major + "\t" + overviewLink);
						j++;
					}

				}  

			}

			i++;
		}

		in.close();
		String code, name, desc;
		Boolean ch=false;

		int k = 1;
		for (Map.Entry<String, String> entry : descipline.entrySet()) {
			String query = "insert into taxonomies.education (name,type,link) values (\"" + major + "\",\"Full-time Courses\",\""  + fullRUI + "\")";
			//stmt.executeUpdate(query);
			url = new URL(entry.getValue().toString());

			httpcon = (HttpURLConnection) url.openConnection(); 
			httpcon.addRequestProperty("User-Agent", "Mozilla/4.76"); 
			in = new BufferedReader(
				new InputStreamReader(httpcon.getInputStream()));


			j=1;
			System.out.println(k + "- " + entry.getKey());

			while ((inputLine = in.readLine()) != null) {
				if ( inputLine.contains("<b>Overview</b>") || inputLine.contains("<h5>Overview</h5>") )
					extractDesc(in,entry.getKey(), k);
			}
			in.close();
			k++;
		}
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

	public static void proccessHTTP(String fullURI) throws SQLException, ClassNotFoundException, IOException
	{
	      //URL url = new URL( "https://www.skillsconnect.gov.sg/sop/WebPageHandler" );

		WebClient webClient = new WebClient(BrowserVersion.CHROME);
		webClient.getOptions().setJavaScriptEnabled(true);

	    // Get the first page
	    HtmlPage page1 = webClient.getPage("https://www.skillsconnect.gov.sg/sop/WebPageHandler");
	    System.out.println("Getting page 1: '" + page1.getTitleText()+ "'");
	    System.out.println("Content\n" + page1.asXml());
/*	    // Get the form that we are dealing with and within that form, 
	    // find the submit button and the field that we want to change.
	    HtmlForm form = page1.getFormByName("f");
	    System.out.println("Form is: '" + form.getId() + "'");

	    //HtmlSubmitInput button = form.getInputByName("f");
	    //System.out.println("button is: '" + button.asText() + "'");
	    HtmlTextInput textField = form.getInputByName("q");
	    System.out.println("textField is: '" + textField.asText() + "'");

	    // Change the value of the text field
	    textField.setValueAttribute("mysql");
	    //form.click();

	    // Now submit the form by clicking the button and get back the second page.
	    HtmlPage page2 = form.click();
	    System.out.println(page2.asText());
*/
	    webClient.close();
	    }
}
