package com.jayway.jersey.rest.reflection;

import java.util.LinkedList;
import java.util.List;

import com.jayway.jersey.rest.resource.ResourceMethod;

public class Capabilities {
	private final String name;
	private final List<ResourceMethod> queries = new LinkedList<ResourceMethod>();
	private final List<ResourceMethod> commands = new LinkedList<ResourceMethod>();
	private final List<String> resources = new LinkedList<String>();
	private Object index;
	public void setIndex(Object index) {
		this.index = index;
	}
	public Capabilities(String name) {
		this.name = name;
	}
	public void addQuery(ResourceMethod method) {
		queries.add(method);
	}
	public void addCommand(ResourceMethod method) {
		commands.add(method);
	}
	public void addResource(String resource) {
		resources.add(resource);
	}
	public String getName() {
		return name;
	}
	public List<ResourceMethod> getQueries() {
		return queries;
	}
	public List<ResourceMethod> getCommands() {
		return commands;
	}
	public List<String> getResources() {
		return resources;
	}
	public Object getIndex() {
		return index;
	}
}
