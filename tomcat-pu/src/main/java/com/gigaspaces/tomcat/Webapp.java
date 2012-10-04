package com.gigaspaces.tomcat;

/**
 * A supporting class to allow simple definition of webapps
 * in the spring pu config
 * 
 * @author DeWayne
 *
 */
public class Webapp {
	private String name;
	private String path;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	
}
