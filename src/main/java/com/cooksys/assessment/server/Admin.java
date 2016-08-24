package com.cooksys.assessment.server;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import javax.xml.bind.annotation.XmlAccessType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "admin")
public class Admin {
	
	
	
	@XmlAttribute
	private String username;
	@XmlElement
	private String password;
	@XmlElement
	private int permission; //permission level, 0 for none, 1 for little 2 for all
							//going to figure these out more once I start implementing admin tools.
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public int getPermission() {
		return permission;
	}
	public void setPermission(int permission) {
		this.permission = permission;
	}	

}
