/**
 * @author abenabdelkader
 *
 * taxonomy.java
 * Nov 8, 2016
 */
package com.wccgroup.web.extrator;

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


public class NAMEconverter {
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
		

		System.out.println("\t*** Extracting JSON Data - ****");
		//OccupationConverter("\\\\savannah\\home\\abenabdelkader\\Documents\\projects\\taxonomy\\Name\\NAME3.json");  // ONET occupations
		OccupationDetailsConverter("\\\\savannah\\home\\abenabdelkader\\Documents\\projects\\taxonomy\\Name\\NAMEDetails3.json");  // ONET occupations
		

	}

	/* Reads the job titles from a taxonomy and mapps them to ontology functions/function_groups */
	public static void OccupationConverter(String path) throws SQLException, ClassNotFoundException, FileNotFoundException, IOException
	{
		Class.forName(JDBC_DRIVER);
		Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
		Statement stmt = conn.createStatement();
		int counter = 1;
		//int j = 0;
		System.out.print("\nExtracting JSON data, from: " + path + "\n\t" );

		try {
				 JSONParser parser = new JSONParser();
				 Object obj = parser.parse(new FileReader(path));

		            JSONObject jsonObject =  (JSONObject) obj;
		            
		            //Object object = parser.parse(stringBuilder);
			        //JSONObject jsonObject = (JSONObject) object;
	        JSONArray nodes = (JSONArray) jsonObject.get("meties");
			//System.out.println("\nnbre of results: \n" + nodes.size() );
			JSONObject object, object2, object3, object4, object5, object6;
			 for (int i = 0; i < nodes.size(); i++) {
				StringBuilder stringBuilder = new StringBuilder();
				stringBuilder.append("insert into name.occupation (code, name, parent) values ");
				 object = (JSONObject)nodes.get(i);
				System.out.println(object.get("Level") + "- " + object.get("Id") + ": "  +object.get("Intitule"));
				stringBuilder.append("(\"" + object.get("Id") + "\",\""  + object.get("Intitule") + "\",null),");
		        JSONArray nodes2 = (JSONArray) object.get("NameObjects");
				 if (nodes2==null) 
					 continue;
				 for (int j = 0; j < nodes2.size(); j++) {
					 object2 = (JSONObject)nodes2.get(j);
					System.out.println("\t" + object2.get("Level") + "- " + object2.get("Id") + ": "  +object2.get("Intitule") + " (Parent - " + object2.get("IdParent") + ": "  +object2.get("IntituleParent") + ")" );
					stringBuilder.append("(\"" + object2.get("Id") + "_"  + object2.get("IdParent") + "\",\""  + object2.get("Intitule") + "\",\""  + object2.get("IdParent") + "\"),");
			        JSONArray nodes3 = (JSONArray) object2.get("NameObjects");
					 if (nodes3==null) 
						 continue;
					 for (int k = 0; k < nodes3.size(); k++) {
						 object3 = (JSONObject)nodes3.get(k);
						System.out.println("\t\t" + object3.get("Level") + "- " + object3.get("Id") + ": "  +object3.get("Intitule") + " (Parent - " + object3.get("IdParent") + ": "  +object3.get("IntituleParent") + ")" );
						stringBuilder.append("(\"" + object3.get("Id") + "_"  + object3.get("IdParent")  + "\",\""  + object3.get("Intitule") + "\",\""  + object3.get("IdParent") + "\"),");
				        JSONArray nodes4 = (JSONArray) object3.get("NameObjects");
						 if (nodes4==null) 
							 continue;
						 for (int l = 0; l < nodes4.size(); l++) {
							 object4 = (JSONObject)nodes4.get(l);
							System.out.println("\t\t\t" + object4.get("Level") + "- " + object4.get("Id") + ": "  +object4.get("Intitule") + " (Parent - " + object4.get("IdParent") + ": "  +object4.get("IntituleParent") + ")" );
							stringBuilder.append("(\"" + object4.get("Id") + "_"  + object4.get("IdParent")  + "\",\""  + object4.get("Intitule") + "\",\""  + object4.get("IdParent") + "\"),");
					        JSONArray nodes5 = (JSONArray) object4.get("NameObjects");
							 if (nodes5==null) 
								 continue;
							 for (int m = 0; m < nodes5.size(); m++) {
								 object5 = (JSONObject)nodes5.get(m);
								System.out.println("\t\t\t\t" + object5.get("Level") + "- " + object5.get("Id") + ": "  +object5.get("Intitule") + " (Parent - " + object5.get("IdParent") + ": "  +object5.get("IntituleParent") + ")" );
								stringBuilder.append("(\"" + object5.get("Id") + "_"  + object5.get("IdParent")  + "\",\""  + object5.get("Intitule") + "\",\""  + object5.get("IdParent") + "\"),");
						        JSONArray nodes6 = (JSONArray) object5.get("NameObjects");
								 if (nodes6==null) 
									 continue;
								 for (int n = 0; n < nodes6.size(); n++) {
									 object6 = (JSONObject)nodes6.get(n);
									System.out.println("\t\t\t\t\t" + object6.get("Level") + "- " + object6.get("Id") + ": "  +object6.get("Intitule") + " (Parent - " + object6.get("IdParent") + ": "  + object6.get("IntituleParent") + ")" );
									stringBuilder.append("(\"" + object6.get("Id") + "_"  + object6.get("IdParent")  + "\",\""  + object6.get("Intitule") + "\",\""  + object6.get("IdParent") + "\"),");
								 }
							 }
						 }
					 }
				 }
					//System.out.println(stringBuilder.toString() );
				 stmt.executeUpdate(stringBuilder.toString().substring(0, stringBuilder.toString().length()-1));
			 }
			 stmt.close();
			 conn.close();

 			 }
			 catch (ParseException e)
			 {
				 e.printStackTrace();
			 }
			 counter++;


	}

	/* Reads the job titles from a taxonomy and mapps them to ontology functions/function_groups */
	public static void OccupationDetailsConverter(String path) throws SQLException, ClassNotFoundException, FileNotFoundException, IOException
	{
		Class.forName(JDBC_DRIVER);
		Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
		Statement stmt = conn.createStatement();
		int counter = 1;
		//int j = 0;
		System.out.print("\nExtracting JSON data, from: " + path + "\n\t" );

		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(new FileReader(path));

			JSONObject jsonObject =  (JSONObject) obj;

			//Object object = parser.parse(stringBuilder);
			//JSONObject jsonObject = (JSONObject) object;
			JSONArray nodes = (JSONArray) jsonObject.get("meties");
			//System.out.println("\nnbre of results: \n" + nodes.size() );
			JSONObject object, object2, object3, object4, object5, object6;
			String query ="";
			for (int i = 0; i < nodes.size(); i++) {
				StringBuilder stringBuilder = new StringBuilder();
				StringBuilder stringBuilder2 = new StringBuilder();
				StringBuilder appelations = new StringBuilder();
				StringBuilder condition_access = new StringBuilder();
				stringBuilder.append("insert into name.appelation (code, name) values ");
				stringBuilder2.append("insert into name.condition_access (code, name) values ");
				appelations.append("insert into name.occupation_appelation (occupation, appelation) values ");
				condition_access.append("insert into name.occupation_access (occupation, access) values ");
				object = (JSONObject)nodes.get(i);
				System.out.println(object.get("Code") + ": "  + object.get("Intitule") + " - "  + object.get("Definition"));
				query = "update name.occupation set description=\"" + object.get("Definition") + "\" where name=\"" + object.get("Intitule") + "\"" ;
				//stmt.executeUpdate(query);
				System.out.println("\tAppelations:");
				JSONArray nodes2 = (JSONArray) object.get("AppelationDetails");
				if (nodes2==null) 
					continue;
				for (int j = 0; j < nodes2.size(); j++) {
					object2 = (JSONObject)nodes2.get(j);
					System.out.println("\t\t" + object2.get("Code") + ": "  +object2.get("Intitule") );
					stringBuilder.append("(\"" + object2.get("Code") + "\",\""  + object2.get("Intitule") + "\"),");
					condition_access.append("(\"" + object.get("Intitule") + "\",\""  + object2.get("Code") + "\"),");
				}
				//stmt.executeUpdate(stringBuilder.toString().substring(0, stringBuilder.toString().length()-1));
				//stmt.executeUpdate(appelations.toString().substring(0, appelations.toString().length()-1));
				System.out.println("\tConditions Access:");
				nodes2 = (JSONArray) object.get("ConditionAccessDetails");
				if (nodes2==null) 
					continue;
				for (int j = 0; j < nodes2.size(); j++) {
					object2 = (JSONObject)nodes2.get(j);
					System.out.println("\t\t" + object2.get("Code") + ": "  +object2.get("Intitule") );
					stringBuilder2.append("(\"" + object2.get("Code") + "\",\""  + object2.get("Intitule") + "\"),");
					appelations.append("(\"" + object.get("Intitule") + "\",\""  + object2.get("Code") + "\"),");
				}
				stmt.executeUpdate(stringBuilder2.toString().substring(0, stringBuilder2.toString().length()-1));
				stmt.executeUpdate(condition_access.toString().substring(0, condition_access.toString().length()-1));
				System.out.println("\tConditions Exercice:");
				nodes2 = (JSONArray) object.get("ConditionExerciceDetails");
				if (nodes2==null) 
					continue;
				for (int j = 0; j < nodes2.size(); j++) {
					object2 = (JSONObject)nodes2.get(j);
					System.out.println("\t\t" + object2.get("Code") + ": "  +object2.get("Intitule") );
				}
				System.out.println("\tLieu de Travail:");
				nodes2 = (JSONArray) object.get("LieuTravailDetails");
				if (nodes2==null) 
					continue;
				for (int j = 0; j < nodes2.size(); j++) {
					object2 = (JSONObject)nodes2.get(j);
					System.out.println("\t\t" + object2.get("Code") + ": "  +object2.get("Intitule") );
				}
				System.out.println("\tActivite de Base:");
				nodes2 = (JSONArray) object.get("ActiviteBaseDetails");
				if (nodes2==null) 
					continue;
				for (int j = 0; j < nodes2.size(); j++) {
					object2 = (JSONObject)nodes2.get(j);
					System.out.println("\t\t" + object2.get("Code") + ": "  +object2.get("Intitule") );
				}
				System.out.println("\tCompetence de Base:");
				nodes2 = (JSONArray) object.get("CompetenceBaseDetails");
				if (nodes2==null) 
					continue;
				for (int j = 0; j < nodes2.size(); j++) {
					object2 = (JSONObject)nodes2.get(j);
					System.out.println("\t\t" + object2.get("Code") + ": "  +object2.get("Intitule") );
				}
				System.out.println("\tActivite Specifique:");
				nodes2 = (JSONArray) object.get("ActiviteSpecifiqueDetails");
				if (nodes2==null) 
					continue;
				for (int j = 0; j < nodes2.size(); j++) {
					object2 = (JSONObject)nodes2.get(j);
					System.out.println("\t\t" + object2.get("Code") + ": "  +object2.get("Intitule") );
					JSONArray nodes3 = (JSONArray) object2.get("CompetenceSpecifiqueDetails");
					if (nodes3==null) 
						continue;
					System.out.println("\t\tCompetence Specifique:");
					for (int k = 0; k < nodes3.size(); k++) {
						object3 = (JSONObject)nodes3.get(k);
						System.out.println("\t\t\t" + object3.get("Code") + ": "  +object3.get("Intitule") );
					}
				}
				//System.out.println(stringBuilder.toString() );
				//stmt.executeUpdate(stringBuilder.toString().substring(0, stringBuilder.toString().length()-1));
			}
			//query = "update name.occupation_appelation set occupation = (select code from name.occupation where name.occupation.name=name.occupation_appelation.occupation)";
			//stmt.executeUpdate(query);
			query = "update name.occupation_access set occupation = (select code from name.occupation where name.occupation.name=name.occupation_access.occupation)";
			stmt.executeUpdate(query);
			stmt.close();
			conn.close();

		}
		catch (ParseException e)
		{
			e.printStackTrace();
		}
		counter++;


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

		query = "truncate table " + DBname + ".occupation_onet";
		System.out.println("\tdeleting occupation_onet: " + stmt.executeUpdate(query) + " data objects");

		query = "truncate table " + DBname + ".occupation_esco";
		System.out.println("\tdeleting occupation_esco: " + stmt.executeUpdate(query) + " data objects");

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