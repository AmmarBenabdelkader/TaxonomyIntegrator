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

public class LOIReader {
	public static void main(String[] args) throws Exception {

		String baseURI="https://www.loi.nl";

		/*    	if (1==1)
    		return;

		 */        

		Map<String, String> descipline = new HashMap<String, String>();

		URL loi = new URL("https://www.loi.nl");
		BufferedReader in = new BufferedReader(
			new InputStreamReader(loi.openStream()));

		String inputLine;
		int i = 1;
		String jobtitle,joblink;
		int j=1;
		Map<String, String> discipline = new HashMap<String, String>();

		while ((inputLine = in.readLine()) != null) {
			if (i>2300 && inputLine.contains("<!--BEGIN-NOINDEX-->")) 
				break;
			if (i>2117 && inputLine.contains("\" href=\"")) {
				//System.out.println("i=" + i + "\n" + inputLine);
				int idx1 = inputLine.indexOf("\" href=\"");
				while (idx1>0 && inputLine.contains("\">")) { 
					joblink = inputLine.substring(idx1+8, inputLine.indexOf("\">",idx1));
					int idx2=inputLine.indexOf("\">",idx1);
					jobtitle = inputLine.substring(idx2+2, inputLine.indexOf("</a>",idx2));
					System.out.println(j + "\t" + jobtitle + "\t" + baseURI+joblink);  
					idx1 = inputLine.indexOf("\" href=\"", idx2);
					discipline.put(jobtitle,  baseURI+joblink);

					j++;
				}  

			}
			i++;
		}

		in.close();
		
		for (Map.Entry<String, String> entry : discipline.entrySet()) {

			System.out.println(entry.getKey() + ": " + entry.getValue());
			loi = new URL(entry.getValue().toString());
			in = new BufferedReader(
				new InputStreamReader(loi.openStream()));

			i = 1;
			j=1;

			while ((inputLine = in.readLine()) != null) {
				if (i>2300 && inputLine.contains("<!--BEGIN-NOINDEX-->")) 
					break;
				if (inputLine.indexOf("large\">")>0 && inputLine.contains("</h4>")) { 
					jobtitle = inputLine.substring(inputLine.indexOf("large\">")+7, inputLine.indexOf("</h4>"));
					System.out.println("\t" + jobtitle);

					j++;
				}  

				if (inputLine.indexOf("<h5>")>0 && inputLine.contains("</h5>")) { 
					jobtitle = inputLine.substring(inputLine.indexOf("<h5>")+4, inputLine.indexOf("</h5>"));
					System.out.println("\t\t" + jobtitle);

					j++;
				}  

				if (inputLine.indexOf("data-product-name=\"")>0) { 
					jobtitle = inputLine.substring(inputLine.indexOf("data-product-name=\"")+19, inputLine.length());
					System.out.println("\t\t\t" + jobtitle);

					j++;
				}  

				i++;
			}

			in.close();
			
		}
	}
}
