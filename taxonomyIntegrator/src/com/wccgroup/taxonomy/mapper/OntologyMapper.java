/**
 * @author abenabdelkader
 *
 * taxonomy.java
 * Nov 8, 2016
 */
package com.wccgroup.taxonomy.mapper;

import java.io.*;
import java.net.*;

import java.sql.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.util.*;
import java.util.Date;
/**
 * @author abenabdelkader
 *
 */


public class OntologyMapper {
	static String JDBC_DRIVER = "";
	static String DB_URL = "";
	static String USER = "";
	static String PASS = "";

	public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException {

		Date date = new Date();
		JDBC_DRIVER = "com.mysql.jdbc.Driver";
		DB_URL = "jdbc:mysql://localhost/ssoc2?useUnicode=true&characterEncoding=utf-8";
		USER = "root";
		PASS = "";
		

/*		resetOntologydata("onet");
		jobTitleMapper("http://demos.savannah.wcc.nl:14080/ontology/v1", "onet", "SELECT onetsoc_code, title FROM onet.occupation_data", "occupation", 0, 10000);  // ONET occupations
		jobTitleMapper("http://demos.savannah.wcc.nl:14080/ontology/v1", "onet", "SELECT distinct onetsoc_code, reported_job_title FROM onet.sample_of_reported_titles where reported_job_title not in (select jobtitle from onet.ontology_occupation)", "reported job title", 0, 10000);  // ONET Reported job titles
		jobTitleMapper("http://demos.savannah.wcc.nl:14080/ontology/v1", "onet", "SELECT distinct onetsoc_code, alternate_title FROM onet.alternate_titles where alternate_title not in (select jobtitle from onet.ontology_occupation)", "alternate job title", 0, 10000);  // ONET Alternate job titles
		jobTitleMapper("http://demos.savannah.wcc.nl:14080/ontology/v1", "onet", "SELECT distinct onetsoc_code, alternate_title FROM onet.alternate_titles where alternate_title not in (select jobtitle from onet.ontology_occupation)", "alternate job title", 10001, 20000);  // ONET Alternate job titles
		jobTitleMapper("http://demos.savannah.wcc.nl:14080/ontology/v1", "onet", "SELECT distinct onetsoc_code, alternate_title FROM onet.alternate_titles where alternate_title not in (select jobtitle from onet.ontology_occupation)", "alternate job title", 20001, 30000);  // ONET Alternate job titles
		jobTitleMapper("http://demos.savannah.wcc.nl:14080/ontology/v1", "onet", "SELECT distinct onetsoc_code, alternate_title FROM onet.alternate_titles where alternate_title not in (select jobtitle from onet.ontology_occupation)", "alternate job title", 30001, 40000);  // ONET Alternate job titles
		jobTitleMapper("http://demos.savannah.wcc.nl:14080/ontology/v1", "onet", "SELECT distinct onetsoc_code, alternate_title FROM onet.alternate_titles where alternate_title not in (select jobtitle from onet.ontology_occupation)", "alternate job title", 40001, 50000);  // ONET Alternate job titles
		
		deduplicateOntologydata("onet");
*/		
		//resetOntologydata_competence("onet");
		//competencyMapper("http://demos.savannah.wcc.nl:14080/ontology/v1", "onet", "SELECT distinct onetsoc_code, t2_example FROM onet.tools_and_technology where t2_type='Tools' and  t2_example not in (select jobtitle from onet.ontology_competence)", "Tools", 0, 10000);  // ONET Tools
		competencyMapper_course("https://api-demo.wcc-group.com/semanticsearch/v1/competences/text?text=", "taxonomies", "SELECT distinct code, name FROM taxonomies.training_course", "training_course");  // ONET Tools
		
		System.out.println("\nTotal duration: " + (new Date().getTime() - date.getTime())/1000 + "s");

		//jobTitleMapper("http://demos.savannah.wcc.nl:14080", "ssoc_temp", "SELECT id, jobtitle FROM ssoc2.jobtitles where length(jobtitle)>3");  // SSOC


	}

	/* Reads the job titles from a taxonomy and mapps them to ontology functions/function_groups */
	public static void jobTitleMapper(String baseURL, String DBname, String jobsQuery, String conceptType, int offset, int limit) throws SQLException, ClassNotFoundException
	{
		Class.forName(JDBC_DRIVER);
		Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
		Statement stmt = conn.createStatement();
		HashMap<String,String> fGroups=new HashMap<String,String>(); 
		HashMap<String,String> functions=new HashMap<String,String>(); 
		HashMap<String,String> competences=new HashMap<String,String>(); 
		HashMap<String,String> functionLabels=new HashMap<String,String>(); 
		HashMap<String,String> functionGroups=new HashMap<String,String>(); 
			StringBuilder links = new StringBuilder();
			StringBuilder actonomySynonyms = new StringBuilder();
			StringBuilder function_Groups = new StringBuilder();
			function_Groups.append("insert into " + DBname + ".function_groups values ");
			actonomySynonyms.append("insert into " + DBname + ".actonomy_synonyms values ");
			links.append("insert into " + DBname + ".ontology_occupation values ");
		ResultSet rs = stmt.executeQuery(jobsQuery);
		int counter = 1;
		int j = 0;
		System.out.print("\nExtracting ontology data: " + DBname.toUpperCase() + " " + conceptType + "\n\t" );
		while (rs.next() && counter<=limit) {
			while (counter<offset) {
				rs.next();
				counter++;
			}
			
			String file = baseURL + "/terms/"+ rs.getString(2).replaceAll(" ", "%20").replaceAll("/", "%20").trim() + "?categories=FUNCTION";
			//System.out.println(counter + ": " + rs.getString(2) + (" (id: " + rs.getString(1)));
			if (counter%50==0)
				System.out.print(counter + "," );
			if (counter%500==0)
				System.out.print("\n\t");
			StringBuilder stringBuilder = new StringBuilder();
			try
			{
			URL url = new URL(file);
			URLConnection yc = url.openConnection();
			BufferedReader in;
				in = new BufferedReader(new InputStreamReader(
					yc.getInputStream()));
			String line;
			stringBuilder.append("{\n\"nodes\":\n");
			while ((line = in.readLine()) != null) {
				stringBuilder.append(line + '\n');
			}
			stringBuilder.append("\n}");
			in.close();
			}
			catch (IOException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			//System.out.println(stringBuilder);
			/*		if (1==1)
			return;
			 */		try {
				 JSONParser parser = new JSONParser();
				 Object obj;
				 obj = parser.parse(stringBuilder.toString());
				 JSONObject jsonObject = (JSONObject) obj;


				 JSONArray nodes = (JSONArray) jsonObject.get("nodes");
				 JSONObject object1;
				 //System.out.println("\nA- Creating right part");
				 if (nodes==null) 
					 return;
				 for (int i = 0; i < nodes.size(); i++) {
					 object1 = (JSONObject)nodes.get(i);
					 if(object1.get("category").toString().equalsIgnoreCase("FUNCTION") || object1.get("category").toString().equalsIgnoreCase("ABSTRACT_FUNCTION")) {
						 functions.put(object1.get("id").toString(), object1.get("label").toString());
						 links.append("(\"" + rs.getString(2) + "\", '" + object1.get("id") + "', " + (int) (Float.parseFloat(object1.get("score").toString())*100) + ",'" + rs.getString(1) + "','" + conceptType + "'),");
					 }
					 /*				 else {
					 if (object1.get("category").toString().equalsIgnoreCase("COMPETENCE")) {
						 competences.put(object1.get("id").toString(), object1.get("label").toString() );
						 links.append("('" + object1.get("code") + "', '" + object1.get("id") + "', " + (int) (Float.parseFloat(object1.get("score").toString())*100) + "),");
					 }
					 else {
						 others.append(object1.get("id") + "\t" + object1.get("label") + "\n");
						 links.append("('" + object1.get("code") + "', '" + object1.get("id") + "', " + (int) (Float.parseFloat(object1.get("score").toString())*100) + "),");
					 }
				 }
					  */
					 JSONArray groups = (JSONArray) object1.get("groups");
					 if (groups==null) 
						 return;
					 for (j = 0; j < groups.size(); j++) {
						 //i++;
						 JSONObject group = (JSONObject)groups.get(j);
						 //System.out.print("\t" + group.get("id") + "#" + group.get("keyword") + "#" + group.get("category"));
						 if(group.get("category").toString().equalsIgnoreCase("DOMAIN") && object1.get("category").toString().equalsIgnoreCase("FUNCTION")) 
							 functionLabels.put(object1.get("id").toString(), group.get("keyword").toString() );
						 //else 
						 //labels.append("function-other-labels\t" + right.get("id") + "\t" + group.get("keyword") + "\n");
						 if(group.get("category").toString().equalsIgnoreCase("FUNCTION_GROUP") && object1.get("category").toString().equalsIgnoreCase("FUNCTION")) {
							 fGroups.put(group.get("id").toString(), group.get("keyword").toString());
							 functionGroups.put(object1.get("id").toString() + "_#_" + group.get("id").toString(), group.get("keyword").toString());
							 function_Groups.append("('" + object1.get("id")+ "','" +  group.get("id") + "'),");
						 }
					 }

					 JSONObject labelObjs = (JSONObject) object1.get("labelsSynonyms");
					 JSONArray synonyms = (JSONArray) labelObjs.get("ENG");
					 if (synonyms!=null) {
						 //System.out.println("Size of lables for '" + right.get("id") + "': " + synonyms.size());
						 for (int l = 0; l < synonyms.size(); l++) {
							 synonyms.get(l);
							 //functionLabels.put(right.get("id").toString(), synonyms.get(l).toString() );
							 actonomySynonyms.append("('" + object1.get("id") + "',\"" + synonyms.get(l).toString() + "\"),");
						 }
					 }
				 }
			 }
			 catch (ParseException e)
			 {
				 e.printStackTrace();
			 }
			 counter++;
		}

				 //System.out.println("Deleting Actonomy Terms: " + stmt.executeUpdate("delete from " + DBname + ".actonomy_terms") + " Data objects deleted");

				 //Actonomy Function Groups
				 StringBuilder query = new StringBuilder();
				 query.append("insert into " + DBname + ".actonomy_terms (code, name, parent) values ");
				 query.append("('FunctionGroup','Function Groups', null)");
				 for(Map.Entry group:fGroups.entrySet()) {  
					 query.append(",('" + group.getKey() + "',\"" + group.getValue() + "\",'FunctionGroup')");
				 }  
				 System.out.println("\n\tGenerating Actonomy Terms: " + stmt.executeUpdate(query.toString()) + " Function Groups");
				 //System.out.println("\tGenerating Actonomy Terms: \n" + query.toString());

				 //Actonomy Functions
				 query = new StringBuilder();
				 query.append("insert into " + DBname + ".actonomy_terms (code, name, parent) values ");
				 query.append("('Function','Functions', null)");
				 for(Map.Entry function:functions.entrySet()) { 
					 query.append(",('" + function.getKey() + "',\"" + function.getValue() + "\",'Function')");
				 }  
				 System.out.println("\tGenerating Actonomy Terms: " + stmt.executeUpdate(query.toString()) + " Functions");
				 //System.out.println("\tGenerating Actonomy Terms: \n" + query.toString());

				 //Actonomy Competence
				 query = new StringBuilder();
				 query.append("insert into " + DBname + ".actonomy_terms (code, name, parent) values ");
				 query.append("('Competence','Competence',null)");
				 for(Map.Entry competence:competences.entrySet()) {  
					 query.append(",('" + competence.getKey() + "',\"" + competence.getValue() + "\",'Competence')");
				 }  
				 System.out.println("\tGenerating Actonomy Terms: " + stmt.executeUpdate(query.toString()) + " Competencies");
				 //System.out.println("\tGenerating Actonomy Terms: \n" + query.toString());


				 // Links from functions to functionGroups
				 //System.out.println("Deleting Relations: " + stmt.executeUpdate("delete from ssoc2.function_groups") + " function_groups deleted");
				 query = new StringBuilder();
				 query.append("insert into " + DBname + ".function_groups values ");
				 for(Map.Entry functionGroup:functionGroups.entrySet()) {  
					 String key1 = functionGroup.getKey().toString().substring(0, functionGroup.getKey().toString().indexOf("_#_"));
					 String key2 = functionGroup.getKey().toString().substring(functionGroup.getKey().toString().indexOf("_#_")+3,functionGroup.getKey().toString().length());
					 query.append("('" + key1 + "','" + key2 + "'),");
				 }  
				 System.out.println("\tGenerating Relations: " + stmt.executeUpdate(query.toString().substring(0, query.length()-1)) + " Functions Groups Links");					
				 //System.out.println("\tGenerating Relations: \n" + query.toString().substring(0, query.length()-1));					

				 //System.out.println("Deleting Relations: " + stmt.executeUpdate("delete from " + DBname + ".actonomy_synonyms") + " actonomy_synonyms deleted");
				 System.out.println("\tGenerating Relations: " + stmt.executeUpdate(actonomySynonyms.toString().substring(0, actonomySynonyms.length()-1)) + " Actonomy Synonyms");					
				 //System.out.println("\tGenerating Relations: \n" + actonomySynonyms.toString().substring(0, actonomySynonyms.length()-1));					

				 //System.out.println(links);
				 //System.out.println("Deleting Relations: " + stmt.executeUpdate("delete from " + DBname + ".ontology_occupation") + " ssoc_actonomy_occupation deleted");
				 //System.out.println("\tGenerating Actonomy Synonyms: \n" + links.toString().substring(0, links.length()-1));					
				 System.out.println("\tGenerating Relations: " + stmt.executeUpdate(links.toString().substring(0, links.length()-1)) + " Actonomy Synonyms");					

			 conn.close();
			 stmt.close();
		

	}


	/* Reads the job titles from a taxonomy and mapps them to ontology functions/function_groups */
	public static void competencyMapper(String baseURL, String DBname, String jobsQuery, String conceptType, int offset, int limit) throws SQLException, ClassNotFoundException
	{
		Class.forName(JDBC_DRIVER);
		Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
		Statement stmt = conn.createStatement();
		HashMap<String,String> fGroups=new HashMap<String,String>(); 
		//HashMap<String,String> functions=new HashMap<String,String>(); 
		HashMap<String,String> competences=new HashMap<String,String>(); 
		HashMap<String,String> functionLabels=new HashMap<String,String>(); 
		HashMap<String,String> functionGroups=new HashMap<String,String>(); 
			StringBuilder links = new StringBuilder();
			StringBuilder actonomySynonyms = new StringBuilder();
			StringBuilder function_Groups = new StringBuilder();
			function_Groups.append("insert into " + DBname + ".competence_groups values ");
			actonomySynonyms.append("insert into " + DBname + ".competence_synonyms values ");
			links.append("insert into " + DBname + ".ontology_competence values ");
		ResultSet rs = stmt.executeQuery(jobsQuery);
		int counter = 1;
		int j = 0;
		System.out.print("\nExtracting ontology data: " + DBname.toUpperCase() + " " + conceptType + "\n\t" );
		while (rs.next() && counter<=limit) {
			while (counter<offset) {
				rs.next();
				counter++;
			}
			
			String file = baseURL + "/terms/"+ rs.getString(2).replaceAll(" ", "%20").replaceAll("/", "%20").trim() + "?categories=COMPETENCE";
			//System.out.println(counter + ": " + rs.getString(2) + (" (id: " + rs.getString(1)));
			if (counter%50==0)
				System.out.print(counter + "," );
			if (counter%500==0)
				System.out.print("\n\t");
			StringBuilder stringBuilder = new StringBuilder();
			try
			{
			URL url = new URL(file);
			URLConnection yc = url.openConnection();
			BufferedReader in;
				in = new BufferedReader(new InputStreamReader(
					yc.getInputStream()));
			String line;
			stringBuilder.append("{\n\"nodes\":\n");
			while ((line = in.readLine()) != null) {
				stringBuilder.append(line + '\n');
			}
			stringBuilder.append("\n}");
			in.close();
			}
			catch (IOException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			//System.out.println(stringBuilder);
			/*		if (1==1)
			return;
			 */		try {
				 JSONParser parser = new JSONParser();
				 Object obj;
				 obj = parser.parse(stringBuilder.toString());
				 JSONObject jsonObject = (JSONObject) obj;


				 JSONArray nodes = (JSONArray) jsonObject.get("nodes");
				 JSONObject object1;
				 //System.out.println("\nA- Creating right part");
				 if (nodes==null) 
					 return;
				 for (int i = 0; i < nodes.size(); i++) {
					 object1 = (JSONObject)nodes.get(i);
					 if(object1.get("category").toString().equalsIgnoreCase("COMPETENCE") || object1.get("category").toString().equalsIgnoreCase("ABSTRACT_COMPETENCE")) {
						 competences.put(object1.get("id").toString(), object1.get("label").toString());
						 links.append("(\"" + rs.getString(2) + "\", '" + object1.get("id") + "', " + (int) (Float.parseFloat(object1.get("score").toString())*100) + ",'" + rs.getString(1) + "','" + conceptType + "'),");
					 }
					 /*				 else {
					 if (object1.get("category").toString().equalsIgnoreCase("COMPETENCE")) {
						 competences.put(object1.get("id").toString(), object1.get("label").toString() );
						 links.append("('" + object1.get("code") + "', '" + object1.get("id") + "', " + (int) (Float.parseFloat(object1.get("score").toString())*100) + "),");
					 }
					 else {
						 others.append(object1.get("id") + "\t" + object1.get("label") + "\n");
						 links.append("('" + object1.get("code") + "', '" + object1.get("id") + "', " + (int) (Float.parseFloat(object1.get("score").toString())*100) + "),");
					 }
				 }
					  */
					 JSONArray groups = (JSONArray) object1.get("groups");
					 if (groups==null) 
						 return;
					 for (j = 0; j < groups.size(); j++) {
						 //i++;
						 JSONObject group = (JSONObject)groups.get(j);
						 //System.out.print("\t" + group.get("id") + "#" + group.get("keyword") + "#" + group.get("category"));
						 if(group.get("category").toString().equalsIgnoreCase("DOMAIN") && object1.get("category").toString().equalsIgnoreCase("COMPETENCE")) 
							 functionLabels.put(object1.get("id").toString(), group.get("keyword").toString() );
						 //else 
						 //labels.append("function-other-labels\t" + right.get("id") + "\t" + group.get("keyword") + "\n");
						 if(group.get("category").toString().equalsIgnoreCase("COMPETENCE_GROUP") && object1.get("category").toString().equalsIgnoreCase("COMPETENCE")) {
							 fGroups.put(group.get("id").toString(), group.get("keyword").toString());
							 functionGroups.put(object1.get("id").toString() + "_#_" + group.get("id").toString(), group.get("keyword").toString());
							 function_Groups.append("('" + object1.get("id")+ "','" +  group.get("id") + "'),");
						 }
					 }

					 JSONObject labelObjs = (JSONObject) object1.get("labelsSynonyms");
					 JSONArray synonyms = (JSONArray) labelObjs.get("ENG");
					 if (synonyms!=null) {
						 //System.out.println("Size of lables for '" + right.get("id") + "': " + synonyms.size());
						 for (int l = 0; l < synonyms.size(); l++) {
							 synonyms.get(l);
							 //functionLabels.put(right.get("id").toString(), synonyms.get(l).toString() );
							 actonomySynonyms.append("('" + object1.get("id") + "',\"" + synonyms.get(l).toString() + "\"),");
						 }
					 }
				 }
			 }
			 catch (ParseException e)
			 {
				 e.printStackTrace();
			 }
			 counter++;
		}

				 //System.out.println("Deleting Actonomy Terms: " + stmt.executeUpdate("delete from " + DBname + ".actonomy_terms") + " Data objects deleted");

				 //Actonomy Function Groups
				 StringBuilder query = new StringBuilder();
				 query.append("insert into " + DBname + ".actonomy_terms (code, name, parent) values ");
				 query.append("('CompetenceGroup','Competence Groups', null)");
				 for(Map.Entry group:fGroups.entrySet()) {  
					 query.append(",('" + group.getKey() + "',\"" + group.getValue() + "\",'CompetenceGroup')");
				 }  
				 System.out.println("\n\tGenerating Actonomy Terms: " + stmt.executeUpdate(query.toString()) + " Competence Groups");
				 //System.out.println("\tGenerating Actonomy Terms: \n" + query.toString());

				 //Actonomy Competence
				 query = new StringBuilder();
				 query.append("insert into " + DBname + ".actonomy_terms (code, name, parent) values ");
				 query.append("('Competence','Competence',null)");
				 for(Map.Entry competence:competences.entrySet()) {  
					 query.append(",('" + competence.getKey() + "',\"" + competence.getValue() + "\",'Competence')");
				 }  
				 System.out.println("\tGenerating Actonomy Terms: " + stmt.executeUpdate(query.toString()) + " Competencies");
				 //System.out.println("\tGenerating Actonomy Terms: \n" + query.toString());


				 // Links from functions to functionGroups
				 //System.out.println("Deleting Relations: " + stmt.executeUpdate("delete from ssoc2.function_groups") + " function_groups deleted");
				 query = new StringBuilder();
				 query.append("insert into " + DBname + ".competence_groups values ");
				 for(Map.Entry functionGroup:functionGroups.entrySet()) {  
					 String key1 = functionGroup.getKey().toString().substring(0, functionGroup.getKey().toString().indexOf("_#_"));
					 String key2 = functionGroup.getKey().toString().substring(functionGroup.getKey().toString().indexOf("_#_")+3,functionGroup.getKey().toString().length());
					 query.append("('" + key1 + "','" + key2 + "'),");
				 }  
				 System.out.println("\tGenerating Relations: " + stmt.executeUpdate(query.toString().substring(0, query.length()-1)) + " Competence Groups Links");					
				 //System.out.println("\tGenerating Relations: \n" + query.toString().substring(0, query.length()-1));					

				 //System.out.println("Deleting Relations: " + stmt.executeUpdate("delete from " + DBname + ".actonomy_synonyms") + " actonomy_synonyms deleted");
				 System.out.println("\tGenerating Relations: " + stmt.executeUpdate(actonomySynonyms.toString().substring(0, actonomySynonyms.length()-1)) + " Competence Synonyms");					
				 //System.out.println("\tGenerating Relations: \n" + actonomySynonyms.toString().substring(0, actonomySynonyms.length()-1));					

				 //System.out.println(links);
				 //System.out.println("Deleting Relations: " + stmt.executeUpdate("delete from " + DBname + ".ontology_occupation") + " ssoc_actonomy_occupation deleted");
				 //System.out.println("\tGenerating Actonomy Synonyms: \n" + links.toString().substring(0, links.length()-1));					
				 System.out.println("\tGenerating Relations: " + stmt.executeUpdate(links.toString().substring(0, links.length()-1)) + " Competence Synonyms");					

			 conn.close();
			 stmt.close();
		

	}

	/* Reads the course titles from a taxonomy and mapps them to ontology competence/abstract_competence */
	public static void competencyMapper_course(String baseURL, String DBname, String jobsQuery, String conceptType) throws SQLException, ClassNotFoundException
	{
		Class.forName(JDBC_DRIVER);
		Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
		Statement stmt = conn.createStatement();
		Statement stmt2 = conn.createStatement();
		ResultSet rs = stmt.executeQuery(jobsQuery);
		int counter = 1;
		System.out.print("\nExtracting ontology data related to: " + DBname + "." + conceptType + "\n\t" );
		while (rs.next()) {
			
			String file = baseURL + rs.getString(2).replaceAll(" ", "%20").replaceAll("/", "%20").trim();
			System.out.println(counter + "- " + rs.getString(2));
			StringBuilder stringBuilder = new StringBuilder();
			try
			{
			URL url = new URL(file);
			URLConnection yc = url.openConnection();
			BufferedReader in;
				in = new BufferedReader(new InputStreamReader(
					yc.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				stringBuilder.append(line + '\n');
			}
			in.close();
			}
			catch (IOException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			//System.out.println(stringBuilder);
			try {
				 JSONParser parser = new JSONParser();
				 Object obj;
				 obj = parser.parse(stringBuilder.toString());
				 JSONObject jsonObject = (JSONObject) obj;


				 JSONArray nodes = (JSONArray) jsonObject.get("results");
				 JSONObject object1;
				 if (nodes==null) 
					 return;
				 for (int i = 0; i < nodes.size(); i++) {
				 //if (nodes.size()>0) {
					 object1 = (JSONObject)nodes.get(i);
					 System.out.println("\t--> " + object1.get("id").toString() + ": " + object1.get("name").toString() + "\tscore:" + object1.get("score").toString());
					 stmt2.executeUpdate("insert into " + DBname + "." + conceptType + "_ontology values ('" + rs.getString(1) + "','" + object1.get("id").toString() + "', '" + object1.get("name").toString() + "')");

				 }
			 }
			 catch (ParseException e)
			 {
				 e.printStackTrace();
			 }
			 counter++;
		}

			 
		 rs.close();
		 stmt.close();
		 stmt2.close();
		conn.close();
		

	}

	public static void resetOntologydata(String DBname) throws SQLException, ClassNotFoundException
	{
		Class.forName(JDBC_DRIVER);
		Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
		Statement stmt = conn.createStatement();
		String query = "truncate table " + DBname + ".actonomy_terms";
		System.out.println("\tdeleting actonomy_terms: " + stmt.executeUpdate(query) + " data objects");					

		query = "truncate table " + DBname + ".function_groups";
		System.out.println("\tdeleting function_groups: " + stmt.executeUpdate(query) + " data objects");					

		query = "truncate table " + DBname + ".actonomy_synonyms";
		System.out.println("\tdeleting actonomy_synonyms values: " + stmt.executeUpdate(query) + " data objects");

		query = "truncate table " + DBname + ".ontology_occupation";
		System.out.println("\tdeleting ontology_occupation: " + stmt.executeUpdate(query) + " data objects");

		conn.close();
		stmt.close();


	}

	public static void resetOntologydata_competence(String DBname) throws SQLException, ClassNotFoundException
	{
		Class.forName(JDBC_DRIVER);
		Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
		Statement stmt = conn.createStatement();
		String query = "delete from " + DBname + ".actonomy_terms where code like 'CO#%'";
		System.out.println("\tdeleting actonomy_terms: " + stmt.executeUpdate(query) + " data objects");					

		query = "truncate table " + DBname + ".competence_groups";
		System.out.println("\tdeleting competence_groups: " + stmt.executeUpdate(query) + " data objects");					

		query = "truncate table " + DBname + ".competence_synonyms";
		System.out.println("\tdeleting competence_synonyms values: " + stmt.executeUpdate(query) + " data objects");

		query = "truncate table " + DBname + ".ontology_competence";
		System.out.println("\tdeleting ontology_competence: " + stmt.executeUpdate(query) + " data objects");

		conn.close();
		stmt.close();


	}

	public static void deduplicateOntologydata(String DBname) throws SQLException, ClassNotFoundException
	{
		Class.forName(JDBC_DRIVER);
		Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
		Statement stmt = conn.createStatement();
		String tables[]={"actonomy_terms","function_groups","actonomy_synonyms","ontology_occupation"};
		System.out.println("\nde-duplicating ontology data:");
		for (int i=0; i<tables.length; i++) {
			System.out.println("\t- " + tables[i]);
			String query = "create table " + DBname + ".tmp (select distinct * from " + DBname + "." + tables[i] + ")";
			stmt.executeUpdate(query);
			query = "drop table " + DBname + "." + tables[i];
			stmt.executeUpdate(query);
			query = "create table " + DBname + "." + tables[i] + " (select * from " + DBname + ".tmp)";
			stmt.executeUpdate(query);
			query = "drop table " + DBname + ".tmp";
			stmt.executeUpdate(query);
		}

		conn.close();
		stmt.close();


	}

}