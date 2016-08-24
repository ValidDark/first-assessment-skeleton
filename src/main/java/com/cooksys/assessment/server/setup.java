package com.cooksys.assessment.server;

import java.io.File;
import java.util.ArrayList;


import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

public class setup {
	

//Initialize the employees list
static Admins admins = new Admins();
static
{
  admins.setAdmins(new ArrayList<Admin>());
  //Create two admins 
  
	admins.setAdmins(new ArrayList<Admin>());
	
	Admin admin1 = new Admin();
	Admin admin2 = new Admin();

	admin1.setUsername("David");
	admin1.setPassword("password12345");
	admin1.setPermission(0);

	admin2.setUsername("admin");
	admin2.setPassword("admin");
	admin2.setPermission(1);
	
	admins.getAdmins().add(admin1);
	admins.getAdmins().add(admin2);
   
}
public static void main(String[] args) throws JAXBException
{
  JAXBContext jaxbContext = JAXBContext.newInstance(Admins.class);
  Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

  jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
   
  //Marshal the employees list in console
  jaxbMarshaller.marshal(admins, System.out);
   
  //Marshal the employees list in file
  jaxbMarshaller.marshal(admins, new File("aaaaaaaaa.xml"));
}







}
	

