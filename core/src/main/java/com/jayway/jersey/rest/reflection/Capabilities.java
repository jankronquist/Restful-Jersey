package com.jayway.jersey.rest.reflection;

import java.util.LinkedList;
import java.util.List;


public class Capabilities {
	private final String name;
	private final List<Capability> queries = new LinkedList<Capability>();
	private final List<Capability> commands = new LinkedList<Capability>();
	private final List<String> resources = new LinkedList<String>();
	private Object index;
	public void setIndex(Object index) {
		this.index = index;
	}
	public Capabilities(String name) {
		this.name = name;
	}
	public void addQuery(Capability method) {
		queries.add(method);
	}
	public void addCommand(Capability method) {
		commands.add(method);
	}
	public void addResource(String resource) {
		resources.add(resource);
	}
	public String getName() {
		return name;
	}
	public List<Capability> getQueries() {
		return queries;
	}
	public List<Capability> getCommands() {
		return commands;
	}
	public List<String> getResources() {
		return resources;
	}
	public Object getIndex() {
		return index;
	}
}
