/**
 * @author abenabdelkader
 *
 * taxonomy.java
 * Sep 7, 2017
 */
package com.wccgroup.taxonomy.integrator;

/**
 * @author abenabdelkader
 *
 */
public class Taxonomy{ 
	private String type;
	private String name;
	private String nodeQuery;
	private String hierarchyQuery;
	private String nodeAttributes;
	
	public Taxonomy(String type, String name, String nodeQuery, String hierarchyQuery, String nodeAttributes) { 
		this.type = type;
		this.name = name;
		this.nodeQuery = nodeQuery;
		this.hierarchyQuery = hierarchyQuery;
		this.nodeAttributes = nodeAttributes;
	} 
	public String getType() { 
		return type;
	} 
	public String getName() { 
		return name;
	} 
	public String getNodeQuery() { 
		return nodeQuery;
	} 
	public String getHierarchyQuery() { 
		return hierarchyQuery;
	} 
	public String getNodeAttributes() { 
		return nodeAttributes;
	} 
	public void setName(String name) 
	{ 
		this.name = name;
	} 
	public void setNodeQuery(String nodeQuery) 
	{ 
		this.nodeQuery = nodeQuery;
	} 
	public void setHierarchyQuery(String hierarchyQuery) 
	{ 
		this.hierarchyQuery = hierarchyQuery;
	} 
	public void setNodeAttributes(String nodeAttributes) 
	{ 
		this.nodeAttributes = nodeAttributes;
	} 
	@Override public String toString() { 
		return "Taxonomy [name=" + name + ", attributes=" + nodeAttributes + "]";
	} 
}