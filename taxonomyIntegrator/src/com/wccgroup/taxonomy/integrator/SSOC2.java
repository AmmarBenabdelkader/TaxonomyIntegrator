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
//import com.wccgroup.elise.testdata_ssoc.Parser_niriCVs_HRxml.UserHandler;
import java.sql.*;
import java.util.*;
import java.util.Date;
/**
 * @author abenabdelkader
 *
 */


public class SSOC2 {
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

		
		//createLocalDB("ssoc", "lssoc");
		//mergeJobs();
		//fixLevel5Competence();
		//distributeSkills();
		//occupationActonomyInput("\\\\savannah\\home\\abenabdelkader\\Documents\\projects\\Singapore\\data\\occupationIndex\\");
		//generateActonomyInput_esco("\\\\savannah\\home\\abenabdelkader\\Documents\\projects\\Singapore\\OntologyInput\\");
		//removeFilesExtension("C:\\data\\CVs\\Jobs\\");
		//relatedOccupation_skills();   // generate related occupations based on common ESCO skills

		
/**/
		String path="\\\\savannah\\home\\abenabdelkader\\Documents\\projects\\TM\\TMP\\SSOC2\\";
		List<Taxonomy> taxonomies = readTaxonomyPara("SSOC2.parameters"); 
		System.out.println("\nGenerating SSOC CSV data for TM");
		generateNodeData(taxonomies, path);
		generateRelationsData(taxonomies, path);


		System.out.println("\nTotal duration: " + (new Date().getTime() - date.getTime())/1000 + "s");



	}


	public static void occupationActonomyInput(String outputFile) throws ClassNotFoundException, SQLException, IOException
	{
		Connection conn = null;
		Statement stmt = null;
		Statement stmt2 = null;
		Class.forName(JDBC_DRIVER);
		conn = DriverManager.getConnection(DB_URL, USER, PASS);
		stmt = conn.createStatement();
		stmt2 = conn.createStatement();
		Statement stmt3 = conn.createStatement();
		StringBuilder document;
		BufferedWriter writer;


		System.out.println( "Actonomy Input generation: ");
		int i = 1;
		//*** Case 1 *** SSOC occupations which are mapped to ESCO
		//String query= "SELECT distinct code, name, description, 'SSOC' FROM ssoc.occupation where type='SSOC Original' and description is not null";
		//*** Case 2 *** SSOC occupations which are not mapped to ESCO
		String query= "SELECT distinct code, name, description, case when right(code,3)='wcc' then 'WCC/ESCO' else 'SSOC' end type FROM lssoc.occupation where length(code)>3";
		ResultSet rs = stmt.executeQuery(query);
		for (; rs.next();)
		{
			document = new StringBuilder();
			document.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			System.out.println( "\t-" + (i++) + ":\t" + rs.getString(1));
			writer = new BufferedWriter(new FileWriter(new File(outputFile + rs.getString(1))));
			document.append("\t<Occupation lang=\"EN\">\n");
			document.append("\t\t<SSOC_code>" + rs.getString(1) + "</SSOC_code>\n");
			document.append("\t\t<Title>" + rs.getString(2).replaceAll("&", "&amp;") + "</Title>\n");
			document.append("\t\t<Source>" + rs.getString(4) + "</Source>\n");
			if (rs.getString(3)!=null)
				document.append("\t\t<Description>" + rs.getString(3).replaceAll("&", "&amp;") + "</Description>\n");

			query= "SELECT distinct altLabel FROM escov1.occupation_altLabels a,  ssoc_temp.esco_ssoc_mapping b where a.code=b.esco_code and b.ssoc_code='" + rs.getString(1) + "'";
			ResultSet rs2 = stmt2.executeQuery(query);
			for (; rs2.next();)
			{
				document.append("\t\t<AltTitle>" + rs2.getString(1).replaceAll("&", "&amp;") + "</AltTitle>\n");
			}
			query= "SELECT distinct b.code, b.name, skilltype, relationtype, description, 'ESCO' source FROM escov1.occupationskills  a, escov1.skills b, ssoc_temp.esco_ssoc_mapping c where a.occupation=c.esco_code and a.skill=b.code and c.ssoc_code ='" + rs.getString(1) + "' order by skilltype, relationtype";
			rs2 = stmt2.executeQuery(query);
			document.append("\t\t<Competencies>\n");
			for (; rs2.next();)
			{
				document.append("\t\t\t<Competency>\n");
				document.append("\t\t\t\t<Name>" + rs2.getString(2).replaceAll("&", "&amp;") + "</Name>\n");
				document.append("\t\t\t\t<code>" + rs2.getString(1) + "</code>\n");
				document.append("\t\t\t\t<skill_type>" + rs2.getString(3) + "</skill_type>\n");
				document.append("\t\t\t\t<relation_type>" + rs2.getString(4) + "</relation_type>\n");
				document.append("\t\t\t\t<Description>" + (rs2.getString(5)!=null?rs2.getString(5).replaceAll("&", "&amp;"):"") + "</Description>\n");
				document.append("\t\t\t\t<Source>" + rs2.getString(6) + "</Source>\n");
				document.append("\t\t\t</Competency>\n");
			}
			document.append("\t\t</Competencies>\n");
			document.append("\t</Occupation>\n");
			rs2.close();
			writer.write(document.toString());
			writer.close();

		}
		stmt.close();
		conn.close();

	}

	public static void generateActonomyInput_esco(String outputFile) throws ClassNotFoundException, SQLException, IOException
	{
		Connection conn = null;
		Statement stmt = null;
		Statement stmt2 = null;
		Class.forName(JDBC_DRIVER);
		conn = DriverManager.getConnection(DB_URL, USER, PASS);
		stmt = conn.createStatement();
		stmt2 = conn.createStatement();
		Statement stmt3 = conn.createStatement();
		StringBuilder document;
		BufferedWriter writer;


		System.out.println( "Actonomy Input generation: ");
		int i = 1;
		//*** Case 1 *** SSOC occupations which are mapped to ESCO
		//String query= "SELECT distinct code, name, description, 'SSOC' FROM ssoc.occupation where type='SSOC Original' and description is not null";
		//*** Case 2 *** SSOC occupations which are not mapped to ESCO
		//String query= "SELECT distinct code, name, description, 'SSOC' FROM ssoc.occupation where length(code)>3 and type='SSOC Original' and description is null";
		//*** Case 3 *** SSOC occupations entirely copied from ESCO
		String query= "SELECT distinct code, name, description, 'ESCO' FROM lssoc.occupation where type='ESCO Occupation'";
		ResultSet rs = stmt.executeQuery(query);
		for (; rs.next();)
		{
			document = new StringBuilder();
			document.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			System.out.println( "\t-" + (i++) + ":\t" + rs.getString(1));
			writer = new BufferedWriter(new FileWriter(new File(outputFile + rs.getString(1))));
			document.append("\t<Occupation lang=\"EN\">\n");
			document.append("\t\t<SSOC_code>" + rs.getString(1) + "</SSOC_code>\n");
			document.append("\t\t<Title>" + rs.getString(2).replaceAll("&", "&amp;") + "</Title>\n");
			document.append("\t\t<Source>" + rs.getString(4) + "</Source>\n");
			if (rs.getString(3)!=null)
				document.append("\t\t<Description>" + rs.getString(3).replaceAll("&", "&amp;") + "</Description>\n");

			query= "SELECT distinct alttitle FROM esco2017.alternativetitles where occupation='" + rs.getString(1) + "'";
			ResultSet rs2 = stmt2.executeQuery(query);
			for (; rs2.next();)
			{
				document.append("\t\t<AltTitle>" + rs2.getString(1).replaceAll("&", "&amp;") + "</AltTitle>\n");
			}
			query= "SELECT distinct a.skillURI, a.skill, a.skillType, a.Relationshiptype, b.description, 'ESCO'  FROM esco2017.occupationskills a, esco2017.skill b where a.skillURI=b.skill and a.occupationURI='" + rs.getString(1) + "'";
			rs2 = stmt2.executeQuery(query);
			document.append("\t\t<Competencies>\n");
			for (; rs2.next();)
			{
				document.append("\t\t\t<Competency>\n");
				document.append("\t\t\t\t<Name>" + rs2.getString(2).replaceAll("&", "&amp;") + "</Name>\n");
				document.append("\t\t\t\t<code>" + rs2.getString(1) + "</code>\n");
				document.append("\t\t\t\t<skill_type>" + rs2.getString(3) + "</skill_type>\n");
				document.append("\t\t\t\t<relation_type>" + rs2.getString(4) + "</relation_type>\n");
				document.append("\t\t<Description>" + rs2.getString(5).replaceAll("&", "&amp;") + "</Description>\n");
				document.append("\t\t<Source>" + rs2.getString(6) + "</Source>\n");
				document.append("\t\t\t</Competency>\n");
			}
			document.append("\t\t</Competencies>\n");
			document.append("\t</Occupation>\n");
			rs2.close();
			writer.write(document.toString());
			writer.close();

		}
		stmt.close();
		conn.close();

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


	public static void relatedOccupation_skills() throws ClassNotFoundException, SQLException, IOException
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
		insertQuery.append("insert into lssoc.related_occupation_skills_esco (code, related_code, score, skill_count, match_count) values ");
		//Map<String, Integer> matchedOccupations = new HashMap<String, Integer>();
		stmt3.executeUpdate("delete from lssoc.related_occupation_skills_esco");


		System.out.println( "Related Occupations based on ESCO Skills: ");
		int i = 0;
		String query= "SELECT code, name FROM lssoc.occupation";
		ResultSet rs = stmt.executeQuery(query);
		int n=0;
		for (; rs.next();)  // for each occupation
		{
			i++;
			n=0;
			System.out.print(i + ": "+ rs.getString(1) + ": " + rs.getString(2));
			query= "SELECT count(a.skill) FROM lssoc.occupation_skills_esco a where a.ssoc_code='" + rs.getString(1) + "'";
			ResultSet rs2 = stmt2.executeQuery(query);
			if (rs2.next())  // for each occupation skill
				n=rs2.getInt(1);
			if(n==0)
				System.out.println();
			
				query= "SELECT a.ssoc_code code, b.ssoc_code related_code, count(a.skill)/" + n + "*100 score FROM lssoc.occupation_skills_esco a, lssoc.occupation_skills_esco b "
					+ "where a.skill=b.skill and a.ssoc_code!=b.ssoc_code and a.ssoc_code='" + rs.getString(1) + "' "
						+ "group by a.ssoc_code, b.ssoc_code having score>=30 order by a.ssoc_code, score desc limit 30";
				insertQuery = new StringBuilder();
				insertQuery.append("insert into lssoc.related_occupation_skills_esco (code, related_code, score, skill_count, match_count) values ");
				ResultSet rs3 = stmt3.executeQuery(query);
				for (; rs3.next();)
				{
					insertQuery.append("('" + rs3.getString(1) + "','" + rs3.getString(2) + "'," + rs3.getInt(3) + "," + n + ",null),");
						
				}
				if (insertQuery.toString().length()>110)
					System.out.println("\t----> " + stmt3.executeUpdate(insertQuery.toString().substring(0, insertQuery.toString().length()-1)) +  "/" +  n + " matches");
				rs3.close();
			} 
		rs.close();
		stmt.close();
		stmt2.close();
		stmt3.close();
		conn.close();

	}

	public static void createLocalDB(String source, String target) throws IOException
	{
		try
		{
			Class.forName(JDBC_DRIVER);
			Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
			Statement stmt = conn.createStatement();
			String query;
			String[][] concepts = {
/*				{"occupation","create","select distinct code, name, type, description from ssoc.occupation"},
				{"occupation","update","update ssoc.occupation set type='SSOC Original'"},
				{"occupation","update","alter table lssoc.occupation add column parent varchar(45)"},
				{"occupation","update","update lssoc.occupation set parent = left(code,length(code)-1)"},
				{"occupation","update","update lssoc.occupation set parent = 'X' where code like 'X%'"},
				{"occupation","update","update lssoc.occupation set parent = null where length(code)=1"},
				{"occupation","update","ALTER TABLE lssoc.occupation CHANGE COLUMN code code varchar(245) NULL"},
				//{"occupation","insert","SELECT distinct code, name, 'ESCO Occupation' type, description, iscogroup parent FROM escov08.occupation a, ssoc.ssoc2015_isco08 b where a.iscogroup=b.isco_code and b.isco_code is not null  and b.ssoc_code is not null and code not in (select code from lssoc.occupation)"},
				{"occupation","insert","SELECT distinct b.ssoc_code, a.name, b.remark, description, b.parent FROM escov08.occupation a, ssoc_temp.esco_ssoc_mapping b where a.code=b.esco_code and b.remark= 'new wcc/ssoc occupation from esco'"},
				{"occupation","update","update lssoc.occupation set description = (SELECT distinct description FROM escov08.occupation a, ssoc_temp.esco_ssoc_mapping b where a.code=b.esco_code and b.ssoc_code=lssoc.occupation.code and remark='manually mapped from esco' limit 1) where description is null"},
				{"occupation_synonyms","create","SELECT a.occupation, b.occupation related_occupation, count(a.alttitle) score, 'common synonyms' remark FROM esco2017.alternativetitles a, esco2017.alternativetitles b where a.occupation!=b.occupation and a.alttitle=b.alttitle group by a.alttitle order by a.occupation, score desc"},
				{"occupation_synonyms","insert","SELECT a.occupationURI, b.occupationURI, count(a.skillURI) score, 'common skills' remark FROM esco2017.occupationskills a, esco2017.occupationskills b where a.occupationURI!=b.occupationURI and a.skillURI=b.skillURI group by a.skillURI order by a.occupationURI, score desc;"},
				{"alternativetitles","create","SELECT distinct occupation code, alttitle name FROM esco2017.alternativetitles where occupation in (select code from lssoc.occupation)"},
				{"alternativeskilltitles","create","SELECT distinct occupation code, alttitle name FROM esco2017.alternativeskilltitles where occupation in (select code from lssoc.occupation)"},
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
				{"occupation_skills_esco","create","SELECT distinct c.ssoc_code, a.skill, skilltype, relationtype FROM escov1.occupationskills  a, lssoc.occupation b, ssoc_temp.esco_ssoc_mapping c where a.occupation=c.esco_code and b.code=c.ssoc_code"},
*/
				{"career_changer_matrix","create","SELECT distinct b.ssoc_code, c.ssoc_code related_ssoc_code, CONVERT(avg(related_index*10),UNSIGNED INTEGER) score1, 0 score2, 'vertical-down' type FROM onet.career_changers_matrix a, lssoc.ssoc2015_onet2015 b, lssoc.ssoc2015_onet2015 c where a.onetsoc_code=b.onet_code_2015 and a.related_onetsoc_code=c.onet_code_2015 and b.ssoc_code!=c.ssoc_code group by b.ssoc_code, c.ssoc_code order by b.ssoc_code,score1 desc, c.ssoc_code"},
				{"career_changer_matrix","update","update lssoc.career_changer_matrix set type='horizontal' where left(ssoc_code,3)=left(related_ssoc_code,3)"},
				{"career_changer_matrix","update","update lssoc.career_changer_matrix set type='vertical-down' where left(ssoc_code,3)<left(related_ssoc_code,3)"},
				{"career_changer_matrix","update","update lssoc.career_changer_matrix set type='vertical-up' where left(ssoc_code,3)>left(related_ssoc_code,3)"},
				{"career_changer_matrix","update","ALTER TABLE lssoc.career_changer_matrix CHANGE COLUMN score2 score2 INT(11) NULL DEFAULT '0'"},

				{"career_changer_matrix_cpy","create", "select * from lssoc.career_changer_matrix"},
				{"career_changer_matrix","update","update lssoc.career_changer_matrix set score2=(select score1 from lssoc.career_changer_matrix_cpy b where lssoc.career_changer_matrix.related_ssoc_code=b.ssoc_code and lssoc.career_changer_matrix.ssoc_code=b.related_ssoc_code)"},
				{"career_changer_matrix","update","update lssoc.career_changer_matrix set score2=0 where score2 is null"},
				//{"career_changer_matrix","update","update lssoc.career_changer_matrix set type='horizontal' where left(ssoc_code,3)=left(related_ssoc_code,3)"},
				//{"career_changer_matrix","update","update lssoc.career_changer_matrix set type='vertical-down' where left(ssoc_code,3)<left(related_ssoc_code,3)"},
				//{"career_changer_matrix","update","update lssoc.career_changer_matrix set type='vertical-up' where left(ssoc_code,3)>left(related_ssoc_code,3)"},

			};

			for (int i=0; i<concepts.length; i++) {
				if (concepts[i][1].equalsIgnoreCase("create")) {

				System.out.print((i+1) + "- creating table " + target + "." + concepts[i][0].toUpperCase() + ": \t"); 
				query = "drop table if exists " + target + "." + concepts[i][0];
				stmt.executeUpdate(query);
				query = "create table " + target + "." + concepts[i][0] + " (" + concepts[i][2] + ")";
				System.out.println(stmt.executeUpdate(query) + " data objects");
				}
				if (concepts[i][1].equalsIgnoreCase("update")) {

				System.out.print((i+1) + "- updating table " + target + "." + concepts[i][0].toUpperCase() + ": \t"); 
				query =  concepts[i][2] ;
				System.out.println(stmt.executeUpdate(query) + " data objects");
				}
				if (concepts[i][1].equalsIgnoreCase("insert")) {

				System.out.print((i+1) + "- inserting data into table " + target + "." + concepts[i][0].toUpperCase() + ": \t"); 
				query = "insert into " + target + "." + concepts[i][0] + " (" + concepts[i][2] + ")";
				System.out.println(stmt.executeUpdate(query) + " data objects");
				}
			}
			conn.close();
			stmt.close();

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

				System.out.print("\t- " + concept.getName()); 
				query = concept.getNodeQuery();
				rs = stmt.executeQuery(query);
				ResultSetMetaData rsmd;
				int ColumnCounts;
				writer = new BufferedWriter(new FileWriter(new File(path + concept.getName() + "-" + concept.getType() + ".csv")));
				rsmd = rs.getMetaData();
				ColumnCounts = rsmd.getColumnCount();
				writer.write(concept.getNodeAttributes());
				int i = 0;
				int j = 0;
				while (rs.next())
				{
					writer.write("\n\"" + rs.getString(1).replaceAll("\"", "'") + "\"");
					for (j=2; j<=ColumnCounts; j++)
						writer.write(",\"" + ((rs.getString(j)!=null)?rs.getString(j).replaceAll("\"", "'"):"") + "\"");
					i++;
				}
				System.out.println(": " + i );
				writer.close();
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
			System.out.println("\t- relations"); 
			for (Taxonomy concept : taxonomies) { 
				if (!concept.getType().equalsIgnoreCase("relation"))
					continue;

				query = concept.getNodeQuery();
				rs = stmt.executeQuery(query);
				ResultSetMetaData rsmd;
				int ColumnCounts;
				query = concept.getNodeQuery();
				rs = stmt.executeQuery(query);
				writer = new BufferedWriter(new FileWriter(new File(path + concept.getName() + "-" + concept.getType() + ".csv")));
				writer.write(concept.getNodeAttributes() + "\n");
				rsmd = rs.getMetaData();
				ColumnCounts = rsmd.getColumnCount();
				int i = 0;
				int j = 0;
				while (rs.next())
				{
					if (i>0)
						writer.write("\n");

					writer.write("\"" + rs.getString(1).replaceAll("\"", "'") + "\"");
					for (j=2; j<=ColumnCounts; j++)
						writer.write(",\"" + rs.getString(j).replaceAll("\"", "'") + "\"");
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


    private static void removeFilesExtension(String path) { 
	File folder = new File(path);
	File[] listOfFiles = folder.listFiles();
	String filename = null;
	String outputFile = null;

    for (int i = 0; i < listOfFiles.length; i++) {
	      if (listOfFiles[i].isFile()) {
	    	  File file = listOfFiles[i];
	    	  filename = file.getName();
	    	  String filename2 = filename.substring(0, filename.length()-4);
	    	  File file2 = new File(path + filename.substring(0, filename.length()-4));

	        System.out.println(i + ": " + filename);
	        file.renameTo(file2);
	        //outputFile = "C:\\data\\Cvs\\Actonomy\\" + listOfFiles[i].getName();
	      }
	    }
    }
    private static List<Taxonomy> readTaxonomyPara(String fileName) { 
		List<Taxonomy> taxonomies = new ArrayList<>(); 
		Path pathToFile = Paths.get(fileName); 
		try (BufferedReader br = Files.newBufferedReader(pathToFile, StandardCharsets.US_ASCII)) { 
			String line = br.readLine(); 
			while (line != null) { 
				String[] attributes = line.split("\t");
				if (attributes[0].equalsIgnoreCase("node")) {
					Taxonomy concept = new Taxonomy (attributes[0], attributes[1], attributes[2], null, attributes[3]); 
					taxonomies.add(concept); 
				}
				if (attributes[0].equalsIgnoreCase("relation")) {
					Taxonomy concept = new Taxonomy (attributes[0], attributes[1], attributes[2], null, attributes[3]); 
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