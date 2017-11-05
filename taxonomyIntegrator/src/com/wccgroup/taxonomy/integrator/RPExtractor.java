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
import java.util.*;
import java.io.*;

public class RPExtractor {
	public static void main(String[] args) throws Exception {

		String baseURI="http://ec.europa.eu/growth/tools-databases/regprof/";

		/*    	if (1==1)
    		return;

		 */        

		Map<String, String> descipline = new HashMap<String, String>();

		URL url = new URL("http://ec.europa.eu/growth/tools-databases/regprof/index.cfm?action=professions&quid=1&mode=asc&maxRows=*#top");
		BufferedReader in = new BufferedReader(
			new InputStreamReader(url.openStream()));

		String inputLine;
		int i = 1;
		String title,link, counts, country;
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
}
