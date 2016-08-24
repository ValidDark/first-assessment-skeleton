package com.cooksys.assessment.server;


import java.util.List;
 
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
 
@XmlRootElement(name = "admins")
@XmlAccessorType (XmlAccessType.FIELD)
public class Admins
{
    @XmlElement(name = "admin")
    private List<Admin> admins = null;
 
    public List<Admin> getAdmins() {
        return admins;
    }
 
    public void setAdmins(List<Admin> admins) {
        this.admins = admins;
    }
}