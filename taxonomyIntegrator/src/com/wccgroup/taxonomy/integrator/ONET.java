package com.wccgroup.taxonomy.integrator;
/**
 * @author abenabdelkader
 *
 * taxonomy.java
 * Nov 8, 2016
 */


import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.logging.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.sql.*;
import java.util.*;
import java.util.Date;
/**
 * @author abenabdelkader
 *
 */


public class ONET {
	static String JDBC_DRIVER = "";
	static String DB_URL = "";
	static String USER = "";
	static String PASS = "";

	public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException {

		Date date = new Date();
		JDBC_DRIVER = "com.mysql.jdbc.Driver";
		DB_URL = "jdbc:mysql://localhost/onet?useUnicode=true&characterEncoding=utf-8";
		USER = "root";
		PASS = "";

		
		createLocalDB("onet", "lonet");
		//mergeJobs();
		//fixLevel5Competence();
		//distributeSkills();
		
/**/
		String path="\\\\savannah\\home\\abenabdelkader\\Documents\\projects\\TM\\TMP\\ONET\\";
		List<Taxonomy> taxonomies = readTaxonomyPara("ONET.parameters"); 
		System.out.println("\nGenerating ONET CSV data for TM");
		generateNodeData(taxonomies, path);
		generateLabelData(taxonomies, path);
		generateRelationsWithScoreData(taxonomies, path);
		generateRelationsData(taxonomies, path);


		System.out.println("\nTotal duration: " + (new Date().getTime() - date.getTime())/1000 + "s");



	}


	public static void distributeSkills() throws ClassNotFoundException, SQLException, IOException
	{
		Connection conn = null;
		Statement stmt = null;
		Statement stmt2 = null;
		Class.forName(JDBC_DRIVER);
		conn = DriverManager.getConnection(DB_URL, USER, PASS);
		stmt = conn.createStatement();
		stmt2 = conn.createStatement();
		Statement stmt3 = conn.createStatement();
		StringBuilder insertQuery = new StringBuilder();
		insertQuery.append("insert into lonet.occupation_competence (occupation, competence, type, hot_technology) values ");
		String stopwords = "#on#in#of#and#or#long#for#"; 


		System.out.println( "O*NET skills re-distribution: ");
		int i = 0;
		String query= "SELECT code, name FROM lonet.occupation";
		//String query= "SELECT code, name FROM lonet.occupation where code like '25-10%'";
		//String query= "SELECT onetsoc_code, title FROM onet.occupation_data where onetsoc_code='15-1141.00'";
		//String query= "SELECT code, name FROM lonet.occupation where parent is null";
		ResultSet rs = stmt.executeQuery(query);
		for (; rs.next();)
		{
			System.out.println(rs.getString(1) + ": " + rs.getString(2));
			query= "SELECT * FROM lonet.occupation where parent='" + rs.getString(1) + "'";
			ResultSet rs2 = stmt2.executeQuery(query);
			for (; rs2.next();)
			{
				System.out.println("\t" + rs2.getString(2));
				query= "SELECT commodity_code, t2_example, t2_type, hot_technology FROM onet.tools_and_technology where onetsoc_code='" + rs.getString(1) + "'";
				ResultSet rs3 = stmt3.executeQuery(query);
				for (; rs3.next();)
				{
					//String words[] = (rs3.getString(2).replaceAll("(", "").replaceAll(")", "")).split(" ");
					//System.out.println("\t'" + rs3.getString(2));
					String skill = rs3.getString(2).replace("(", "") + " ";
					skill.replace(")", "");
					//System.out.println("\t'" + skill);
					String words[] = skill.split(" ");
					boolean found = false;
					for (i=0; i<words.length; i++) {
						if (words[i].contains(")"))
							break;
						String pattern = "\\b"+words[i]+"\\b";
				         Pattern p=Pattern.compile(pattern);
				         Matcher m=p.matcher(rs2.getString(2));
							//if (words[i].length()>1 && rs2.getString(2).contains(words[i]))
				         if (m.find() && !stopwords.contains('#' + words[i].toLowerCase() + '#')) {
				        	 insertQuery.append("('" + rs2.getString(1) + "',\"" + rs3.getString(2) + "\",'" + rs3.getString(3)  + "','" + rs3.getString(4) + "'),");
				        	 System.out.println("\t\t"+rs3.getString(2));
							found = true;
				         }
					}

					//if (!found)
					//	System.out.println("\t\t\t"+rs3.getString(2) + " not found");
				}
			} 
			rs2.close();

		}
		query = "truncate table lonet.occupation_competence";
		System.out.println("clean table occupation_competence: " + stmt.executeUpdate(query) + " data objects");
		System.out.println(insertQuery.toString());
		System.out.println("inserting specific occupation_competence: " + stmt.executeUpdate(insertQuery.toString().substring(0, insertQuery.toString().length()-1)) + " data objects");
		;
		query = "insert into lonet.occupation_competence (SELECT distinct onetsoc_code, t2_example, t2_type, hot_technology FROM onet.tools_and_technology where t2_example not in (SELECT competence FROM lonet.occupation_competence));";
		stmt.executeUpdate(query);
		System.out.println("inserting specific occupation_competence: " + stmt.executeUpdate(query) + " data objects");
		
		query = "update lonet.occupation_competence set competence=(select distinct code from lonet.competence where lonet.competence.name=lonet.occupation_competence.competence limit 1)";
		System.out.println("inserting generic occupation_competence: " + stmt.executeUpdate(query) + " data objects");
		stmt.close();
		conn.close();

	}

	public static void createLocalDB(String source, String target) throws SQLException, IOException
	{
		try
		{
			Class.forName(JDBC_DRIVER);
			Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
			Statement stmt = conn.createStatement();
			String query;
			String[][] concepts = {
				{"education_training_experience","create","SELECT distinct a.element_id code, b.element_name name, null parent FROM onet.ete_categories a, onet.content_model_reference b where a.element_id=b.element_id"},
				{"education_training_experience","update","alter table lonet.education_training_experience CHANGE COLUMN parent parent varchar(45) NULL"},
				{"education_training_experience","update","alter table lonet.education_training_experience CHANGE COLUMN name name varchar(500) NULL"},
				{"education_training_experience","insert","SELECT distinct concat(element_id,'.', category), category_description, element_id parent FROM onet.ete_categories"},
				//{"occupation","create","select onetsoc_code code, title name, description, 'occupation' type from onet.occupation_data"},
				//{"reported_job_titles","create","SELECT onetsoc_code code, reported_job_title name, count(onetsoc_code) occupation_frequency FROM onet.sample_of_reported_titles group by reported_job_title"},
				//{"alternate_job_titles","create","SELECT onetsoc_code code, alternate_title name, count(onetsoc_code) occupation_frequency FROM onet.alternate_titles group by alternate_title"},
/*				{"competence","create","SELECT distinct commodity_code code, commodity_title name, 'Commodity' type, class_code parent, null occupation FROM onet.unspsc_reference"},
				{"competence","insert","SELECT distinct class_code, class_title, 'Class' type, family_code parent, null occupation FROM onet.unspsc_reference"},
				{"competence","insert","SELECT distinct family_code, family_title, 'Family' type, segment_code parent, null occupation FROM onet.unspsc_reference"},
				{"competence","insert","SELECT distinct segment_code, segment_title, 'Segment' type, null parent, null occupation FROM onet.unspsc_reference"},
				{"competence","insert","SELECT distinct commodity_code, t2_example, t2_type, commodity_code,onetsoc_code FROM onet.tools_and_technology order by commodity_code, t2_example"},
				{"competence","update","ALTER TABLE lonet.competence CHANGE COLUMN parent parent DECIMAL(8,0) NULL"},
				{"competence","update","ALTER TABLE lonet.competence CHANGE COLUMN type type varchar(45) NOT NULL"},
				{"competence","update","ALTER TABLE lonet.competence CHANGE COLUMN occupation occupation varchar(45) NULL"},
				{"occupation_tasks","create","select onetsoc_code occupation, task_id code, task name, task_type type, '' green_task, '' emerging_task, '' task_rating from onet.task_statements"},
				{"occupation_tasks","update","ALTER TABLE lonet.occupation_tasks CHANGE COLUMN green_task green_task varchar(45) NULL"},
				{"occupation_tasks","update","ALTER TABLE lonet.occupation_tasks CHANGE COLUMN emerging_task emerging_task varchar(45) NULL"},
				{"occupation_tasks","update","update lonet.occupation_tasks set green_task=(select green_task_type from onet.green_task_statements where onet.green_task_statements.task_id=lonet.occupation_tasks.code)"},
				{"occupation_tasks","update","update lonet.occupation_tasks set emerging_task=(select category from onet.emerging_tasks where onet.emerging_tasks.task=lonet.occupation_tasks.name)"},
				{"tasks","create","SELECT distinct b.element_id code, b.element_name name, b.description, '' parent FROM onet.green_dwa_reference a, onet.content_model_reference b where a.element_id=b.element_id"},
				{"tasks","insert","SELECT distinct green_dwa_id, green_dwa_title, '', a.element_id parent FROM onet.green_dwa_reference a"},
				{"tasks","insert","SELECT distinct b.task_id, b.task, '', a.green_dwa_id parent FROM onet.tasks_to_green_dwas a, onet.task_statements b where a.task_id=b.task_id"},
				{"tasks","insert","SELECT distinct b.task_id, b.task, '', 'unclassified' parent FROM onet.task_statements b where task_id not in (select code from lonet.tasks)"},
				{"tasks","update","ALTER TABLE lonet.tasks CHANGE COLUMN parent parent varchar(45) NULL"},
				{"tasks","update","ALTER TABLE lonet.tasks CHANGE COLUMN name name varchar(500) NULL"},
				{"tasks","update","insert into lonet.tasks values ('unclassified', 'Un-Classified', 'not yet classified in specific categories', '')"},
				{"career_change","insert","SELECT a.onetsoc_code code, a.related_onetsoc_code related_code, related_index, 'horizontal' type FROM onet.career_changers_matrix a where left(a.onetsoc_code,2)=left(a.related_onetsoc_code,2) order by a.onetsoc_code, related_index desc"},
				{"career_change","insert","SELECT a.onetsoc_code, a.related_onetsoc_code, related_index, 'vertical-up' type FROM onet.career_changers_matrix a where left(a.onetsoc_code,2)>left(a.related_onetsoc_code,2) order by a.onetsoc_code, related_index desc"},
				{"career_change","create","SELECT a.onetsoc_code code, a.related_onetsoc_code related_code, related_index, 'vertical-down' type FROM onet.career_changers_matrix a where left(a.onetsoc_code,2)<left(a.related_onetsoc_code,2) order by a.onetsoc_code, related_index desc"},
*/

			};

			for (int i=0; i<concepts.length; i++) {
				if (!concepts[i][1].equalsIgnoreCase("create"))
					continue;

				System.out.print((i+1) + "- creating table " + target + "." + concepts[i][0].toUpperCase() + ": \t"); 
				query = "drop table if exists " + target + "." + concepts[i][0];
				stmt.executeUpdate(query);
				query = "create table " + target + "." + concepts[i][0] + " (" + concepts[i][2] + ")";
				System.out.println(stmt.executeUpdate(query) + " data objects");
			}
			for (int i=0; i<concepts.length; i++) {
				if (!concepts[i][1].equalsIgnoreCase("update"))
					continue;

				System.out.print((i+1) + "- updating table " + target + "." + concepts[i][0].toUpperCase() + ": \t"); 
				query =  concepts[i][2] ;
				System.out.println(stmt.executeUpdate(query) + " data objects");
			}
			for (int i=0; i<concepts.length; i++) {
				if (!concepts[i][1].equalsIgnoreCase("insert"))
					continue;

				System.out.print((i+1) + "- inserting data into table " + target + "." + concepts[i][0].toUpperCase() + ": \t"); 
				query = "insert into " + target + "." + concepts[i][0] + " (" + concepts[i][2] + ")";
				System.out.println(stmt.executeUpdate(query) + " data objects");
			}
			conn.close();
			stmt.close();

		}
		catch (ClassNotFoundException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public static void mergeJobs() throws SQLException, IOException
	{
		try
		{
			Class.forName(JDBC_DRIVER);
			Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
			Statement stmt1 = conn.createStatement();
			Statement stmt2 = conn.createStatement();
			ResultSet rs1, rs2;
			String query;
			StringBuilder insertQuery = new StringBuilder();
			stmt1.executeUpdate("ALTER TABLE lonet.occupation ADD COLUMN parent VARCHAR(10)");
			stmt1.executeUpdate("ALTER TABLE lonet.occupation CHANGE COLUMN code code CHAR(15) NOT NULL");
			stmt1.executeUpdate("ALTER TABLE lonet.occupation CHANGE COLUMN type type varCHAR(25) NOT NULL");
			stmt1.executeUpdate("ALTER TABLE lonet.occupation CHANGE COLUMN description description VARCHAR(1000) NULL");
			insertQuery.append("insert into lonet.occupation (code, name, parent, type) values ");
			query = "SELECT code, name FROM lonet.occupation";
			rs1 = stmt1.executeQuery(query);
			for (int i=0; rs1.next(); i++) {
				query = "SELECT distinct name FROM lonet.reported_job_titles where code='" + rs1.getString(1) + "' and occupation_frequency=1";
				rs2 = stmt2.executeQuery(query);
				int j = 1;
				for (j=1; rs2.next(); j++) {
					insertQuery.append("('" + rs1.getString(1) + "-" + (j<=9?"0":"") + j + "', \"" + rs2.getString(1) + "\",'" + rs1.getString(1) + "','reported job title'),");
				}
				query = "SELECT distinct name FROM lonet.alternate_job_titles where code='" + rs1.getString(1) + "' and occupation_frequency=1";
				rs2 = stmt2.executeQuery(query);
				for (; rs2.next(); j++) {
					insertQuery.append("('" + rs1.getString(1) + "-" + (j<=9?"0":"") + j + "', \"" + rs2.getString(1) + "\",'" + rs1.getString(1) + "','alternate job title'),");
				}
			}
			stmt1.executeUpdate(insertQuery.toString().substring(0, insertQuery.toString().length()-1));
			System.out.println(insertQuery.toString());
			rs1.close();
			stmt1.close();
			stmt2.close();
			conn.close();

		}
		catch (ClassNotFoundException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public static void fixLevel5Competence() throws IOException
	{
		try
		{
			Class.forName(JDBC_DRIVER);
			Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
			Statement stmt1 = conn.createStatement();
			Statement stmt2 = conn.createStatement();
			Statement stmt3 = conn.createStatement();
			ResultSet rs1, rs2;
			String query;
			//StringBuilder updateQuery = new StringBuilder();
			//stmt1.executeUpdate("ALTER TABLE lonet.occupation ADD COLUMN parent VARCHAR(10)");
			stmt1.executeUpdate("ALTER TABLE lonet.competence CHANGE COLUMN code code CHAR(25) NOT NULL");
			//stmt1.executeUpdate("ALTER TABLE lonet.occupation CHANGE COLUMN description description VARCHAR(1000) NULL");
			//updateQuery.append("update lonet.competence set  (code, name, parent) values ");
			query = "SELECT code, name FROM lonet.competence where type ='Commodity'";
			rs1 = stmt1.executeQuery(query);
			for (; rs1.next(); ) {
				query = "SELECT distinct code, name FROM lonet.competence where parent='" + rs1.getString(1) + "' order by name";
				rs2 = stmt2.executeQuery(query);
				int j = 1;
				for (j=1; rs2.next(); j++) {
					query = "update lonet.competence set code='" + rs1.getString(1) + "-" + (j<=9?"0":"") + j + "' where parent='" + rs1.getString(1) + "' and name=\"" + rs2.getString(2) + "\"";
					System.out.println(query);
					stmt3.executeUpdate(query);
				}
			}
			//System.out.println(updateQuery.toString());
			rs1.close();
			stmt1.close();
			stmt2.close();
			stmt3.close();
			conn.close();

		}
		catch (ClassNotFoundException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void generateNodeData(List<Taxonomy> taxonomies, String path) throws SQLException, IOException
	{
		try
		{
			Class.forName(JDBC_DRIVER);
			Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
			Statement stmt = conn.createStatement();
			String query;
			ResultSet rs;
			BufferedWriter writer;
			for (Taxonomy concept : taxonomies) { 
				if (!concept.getType().equalsIgnoreCase("node"))
					continue;

				System.out.println("\t- " + concept.getName()); 
				query = concept.getNodeQuery();
				rs = stmt.executeQuery(query);
				ResultSetMetaData rsmd;
				int ColumnCounts;
				writer = new BufferedWriter(new FileWriter(new File(path + concept.getName() + "-nodes.csv")));
				rsmd = rs.getMetaData();
				ColumnCounts = rsmd.getColumnCount();
				writer.write(concept.getNodeAttributes());
				int i = 0;
				int j = 0;
				while (rs.next())
				{
					writer.write("\n" + rs.getString(1));
					for (j=2; j<=ColumnCounts; j++)
						writer.write("\t" + rs.getString(j));
					i++;
				}
				writer.close();
				System.out.println("\t\t* " + concept.getName() + "-nodes: " + i );

				query = concept.getHierarchyQuery();
				rs = stmt.executeQuery(query);
				rsmd = rs.getMetaData();
				ColumnCounts = rsmd.getColumnCount();
				writer = new BufferedWriter(new FileWriter(new File(path + concept.getName() + "-hierarchy.csv")));
				i = 0;
				StringBuilder st = new StringBuilder();
				while (rs.next())
				{
					if (i>0)
						st.append("\n");

					st.append(rs.getString(1));
					for (j=2; j<=ColumnCounts && rs.getString(j)!=null; j++)
						st.append("\t" + rs.getString(j));
					i++;
				}
				writer.write(st.toString());
				writer.close();
				System.out.println("\t\t* " + concept.getName() + "-hierarchy: " + i );
				rs.close();
			}
			conn.close();
			stmt.close();

		}
		catch (ClassNotFoundException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public static void generateLabelData(List<Taxonomy> taxonomies, String path) throws SQLException, IOException
	{
		try
		{
			Class.forName(JDBC_DRIVER);
			Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
			Statement stmt = conn.createStatement();
			String query;
			ResultSet rs;
			BufferedWriter writer;
			System.out.println("\t- Labels"); 
			for (Taxonomy concept : taxonomies) { 
				if (!concept.getType().equalsIgnoreCase("label"))
					continue;

				query = concept.getNodeQuery();
				rs = stmt.executeQuery(query);
				ResultSetMetaData rsmd;
				int ColumnCounts;
				query = concept.getNodeQuery();
				rs = stmt.executeQuery(query);
				writer = new BufferedWriter(new FileWriter(new File(path + concept.getName() + "-labels-" + concept.getNodeAttributes() + ".csv")));
				rsmd = rs.getMetaData();
				ColumnCounts = rsmd.getColumnCount();
				int i = 0;
				int j = 0;
				while (rs.next())
				{
					if (i>0)
						writer.write("\n");

					writer.write(rs.getString(1));
					for (j=2; j<=ColumnCounts; j++)
						writer.write("\t" + rs.getString(j));
					i++;
				}
				writer.close();
				System.out.println("\t\t* " + concept.getName() + "-labels-" + concept.getNodeAttributes() + ": " + i );
				rs.close();
			}

			conn.close();
			stmt.close();

		}
		catch (ClassNotFoundException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public static void generateRelationsData(List<Taxonomy> taxonomies, String path) throws SQLException, IOException
	{
		try
		{
			Class.forName(JDBC_DRIVER);
			Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
			Statement stmt = conn.createStatement();
			String query;
			ResultSet rs;
			BufferedWriter writer;
			System.out.println("\t- standard-relations"); 
			for (Taxonomy concept : taxonomies) { 
				if (!concept.getType().equalsIgnoreCase("standard-relations"))
					continue;

				query = concept.getNodeQuery();
				rs = stmt.executeQuery(query);
				ResultSetMetaData rsmd;
				int ColumnCounts;
				query = concept.getNodeQuery();
				rs = stmt.executeQuery(query);
				writer = new BufferedWriter(new FileWriter(new File(path + concept.getType() + "-" + concept.getNodeAttributes() + ".csv")));
				rsmd = rs.getMetaData();
				ColumnCounts = rsmd.getColumnCount();
				int i = 0;
				int j = 0;
				while (rs.next())
				{
					if (i>0)
						writer.write("\n");

					writer.write(rs.getString(1));
					for (j=2; j<=ColumnCounts; j++)
						writer.write("\t" + rs.getString(j));
					i++;
				}
				writer.close();
				System.out.println("\t\t* " + concept.getName() + ": " + i );
				rs.close();
			}

			conn.close();
			stmt.close();

		}
		catch (ClassNotFoundException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public static void generateRelationsWithScoreData(List<Taxonomy> taxonomies, String path) throws SQLException, IOException
	{
		try
		{
			Class.forName(JDBC_DRIVER);
			Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
			Statement stmt = conn.createStatement();
			String query;
			ResultSet rs;
			BufferedWriter writer;
			System.out.println("\t- relations-with-score"); 
			for (Taxonomy concept : taxonomies) { 
				if (!concept.getType().equalsIgnoreCase("relations-with-score"))
					continue;

				query = concept.getNodeQuery();
				rs = stmt.executeQuery(query);
				ResultSetMetaData rsmd;
				int ColumnCounts;
				query = concept.getNodeQuery();
				rs = stmt.executeQuery(query);
				writer = new BufferedWriter(new FileWriter(new File(path + concept.getType() + "-" + concept.getNodeAttributes() + ".csv")));
				rsmd = rs.getMetaData();
				ColumnCounts = rsmd.getColumnCount();
				int i = 0;
				int j = 0;
				while (rs.next())
				{
					if (i>0)
						writer.write("\n");

					writer.write(rs.getString(1));
					for (j=2; j<=ColumnCounts; j++)
						writer.write("\t" + rs.getString(j));
					i++;
				}
				writer.close();
				System.out.println("\t\t* " + concept.getName() + ": " + i );
				rs.close();
			}

			conn.close();
			stmt.close();

		}
		catch (ClassNotFoundException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	private static List<Taxonomy> readTaxonomyPara(String fileName) { 
		List<Taxonomy> taxonomies = new ArrayList<>(); 
		Path pathToFile = Paths.get(fileName); 
		try (BufferedReader br = Files.newBufferedReader(pathToFile, StandardCharsets.US_ASCII)) { 
			String line = br.readLine(); 
			while (line != null) { 
				String[] attributes = line.split("\t");
				String nodeAttributes="";
				if (attributes[0].equalsIgnoreCase("node")) {
					for (int i=4; i<attributes.length; i++)
						nodeAttributes += attributes[i] + "\t";
					Taxonomy concept = new Taxonomy (attributes[0], attributes[1], attributes[2], attributes[3], nodeAttributes.substring(0, nodeAttributes.length()-1)); 
					taxonomies.add(concept); 
				}
				if (attributes[0].equalsIgnoreCase("label")) {
					Taxonomy concept = new Taxonomy (attributes[0], attributes[1], attributes[3], null, attributes[2]); 
					taxonomies.add(concept); 
				}
				if (attributes[0].equalsIgnoreCase("relations-with-score")) {
					Taxonomy concept = new Taxonomy (attributes[0], attributes[1], attributes[3], null, attributes[2]); 
					taxonomies.add(concept); 
				}
				if (attributes[0].equalsIgnoreCase("standard-relations")) {
					Taxonomy concept = new Taxonomy (attributes[0], attributes[1], attributes[3], null, attributes[2]); 
					taxonomies.add(concept); 
				}
				line = br.readLine(); 
			} 
		} 
		catch (IOException ioe) { 
			ioe.printStackTrace(); 
		} 
		return taxonomies; 
	} 
}