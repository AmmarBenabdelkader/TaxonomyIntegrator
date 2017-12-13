/**
 * @author abenabdelkader
 *
 * URLReader.java
 * Aug 14, 2017
 */
package com.wccgroup.taxonomy.integrator;

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

public class RPExtractor {
	public static void main(String[] args) throws Exception {

		String baseURI="http://ec.europa.eu/growth/tools-databases/regprof/";
		String fullRUI="http://ec.europa.eu/growth/tools-databases/regprof/index.cfm?action=professions&quid=1&mode=asc&maxRows=*#top";
		//jobTitleMapper(baseURI, fullRUI);

		//String query = "SELECT distinct name_of_regulated_profession, translations FROM escov1.regulated_professions where WCC_code is null order by name_of_regulated_profession, translations";
		String query = "SELECT distinct name_of_regulated_profession, translations FROM escov1.regulated_professions where WCC_code is null and translations in (select translation FROM taxonomies.regulated_certication) order by name_of_regulated_profession, translations";
		getMappingCode(query);
	}
	public static void jobTitleMapper(String baseURI, String fullRUI) throws SQLException, ClassNotFoundException, IOException
	{

		URL url = new URL(fullRUI);
		BufferedReader in = new BufferedReader(
			new InputStreamReader(url.openStream()));

		String inputLine;
		int i = 1;
		String title,link, counts;
		int j=1;
		Map<String, String> professions = new HashMap<String, String>();

		while ((inputLine = in.readLine()) != null) {
			//System.out.println("i=" + i + "\n" + inputLine);
			if (inputLine.contains("<td class=\"odd\"><a href=\"") || inputLine.contains("<td class=\"even\"><a href=\"")) {
				int idx1 = inputLine.indexOf(" href=\"");
				while (idx1>0 && inputLine.contains("\">")) { 
					link = inputLine.substring(idx1+7, inputLine.indexOf("\">",idx1));
					int idx2=inputLine.indexOf("\">",idx1);
					title = inputLine.substring(idx2+2, inputLine.indexOf("</a>",idx2));
					inputLine = in.readLine();
					int idx3 = inputLine.indexOf("\">", idx2+2);
					counts = inputLine.substring(inputLine.indexOf("\">")+2, inputLine.indexOf("</td>",idx3));
					//System.out.println(j + "\t" + title + "\t" + baseURI+link + "\t" + counts);  
					idx1 = inputLine.indexOf("\" href=\"", idx2);
					professions.put(title,  baseURI+link);

					j++;
				}  

			}
			i++;
		}

		in.close();
		
		i = 1;
		for (Map.Entry<String, String> entry : professions.entrySet()) {

			//System.out.println(entry.getKey() + ": " + entry.getValue());
			url = new URL(entry.getValue().toString());
			in = new BufferedReader(new InputStreamReader(url.openStream()));
			

			j=1;

			while ((inputLine = in.readLine()) != null) {
				if (inputLine.contains("<td class=\"odd\"><a href=\"") || inputLine.contains("<td class=\"even\"><a href=\"")) { 
					link = inputLine.substring(inputLine.indexOf("\"><a href=\"")+11, inputLine.indexOf("=1\">"));
					title = inputLine.substring(inputLine.indexOf("=1\">")+4, inputLine.indexOf("</a>"));
					System.out.print(i + "\t" + entry.getKey() + "\t" + title);
					inputLine = in.readLine();
					System.out.print("\t" + inputLine.substring(inputLine.indexOf("\">")+2, inputLine.indexOf("</td>")));
					inputLine = in.readLine();
					System.out.print("\t" + inputLine.substring(inputLine.indexOf("\">")+2, inputLine.indexOf("</td>")));
					inputLine = in.readLine();
					System.out.print("\t" + inputLine.substring(inputLine.indexOf("\">")+2, inputLine.indexOf("</td>")));

					//System.out.println("\n\t\t" + baseURI+link);
					URL suburl = new URL(baseURI+link);
					BufferedReader subin = new BufferedReader(new InputStreamReader(suburl.openStream()));
					String line="";
					String trans="";
					while ((line = subin.readLine()) != null) {
						if (line.contains("Translation(s)") ) {
							line = subin.readLine();
							line = subin.readLine();
							while (!line.contains("</dd>")) {
								trans += line.substring(line.indexOf("<dd>")+4, line.length());
								line = subin.readLine();
							}
							
							trans += line.substring(line.indexOf("<dd>")+4, line.indexOf("</dd>"));
							
							System.out.print("\t" + trans.replaceAll("<br />", ""));
							trans ="";
							line = subin.readLine();

						}

							if (line.contains("Qualification level:") ) {
								line = subin.readLine();
								line = subin.readLine();
								line = subin.readLine();
								while (!line.contains("</dd>")) {
									trans += line.trim();
									line = subin.readLine();
								}
								
								//trans += line.substring(line.indexOf("<dd>")+4, line.indexOf("</dd>"));
								//trans.replaceAll("<br />", "");
								
								System.out.print("\t" + trans);
							break;
							}
						//if (line.contains("Translation(s)") ) 
						//	break;
					}
					subin.close();
					System.out.println();
					j++;
				}  

				//i++;
			}

			in.close();
			i++;
			
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

}
