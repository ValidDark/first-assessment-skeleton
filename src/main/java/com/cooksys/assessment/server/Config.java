package com.cooksys.assessment.server;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Config {
    @XmlElement
    private int port;

    @XmlElement
    private String MOTD;

    @XmlElement(name = "admin-file-path")
    private String adminFilePath;

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getAdminFilePath() {
		return adminFilePath;
	}

	public void setAdminFilePath(String adminFilePath) {
		this.adminFilePath = adminFilePath;
	}
   
	public String getMOTD() {
		return MOTD;
	}

	public void setMOTD(String motd) {
		MOTD = motd;
	}
}
