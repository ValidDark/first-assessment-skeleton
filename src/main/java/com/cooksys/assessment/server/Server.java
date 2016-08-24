package com.cooksys.assessment.server;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cooksys.assessment.Main;
import com.cooksys.assessment.model.User;



public class Server implements Runnable {
	
	static List<Admin> adminList = new ArrayList();
	static Config serverConfig = new Config();
	{
	//Gets admin list from file.
	try {
    JAXBContext adContext = JAXBContext.newInstance(Admins.class);
    Unmarshaller adUnmarshaller = adContext.createUnmarshaller();
    
    JAXBContext conContext = JAXBContext.newInstance(Config.class);
    Unmarshaller conUnmarshaller = conContext.createUnmarshaller();
    
    serverConfig = (Config) conUnmarshaller.unmarshal( new File("src/main/java/com/cooksys/assessment/server/config.xml") );

    Admins ads = (Admins) adUnmarshaller.unmarshal( new File(serverConfig.getAdminFilePath()) );
    adminList = ads.getAdmins(); 
    
}
	catch (JAXBException e) {
		e.printStackTrace();
	}
	}


	//static List<Admin> adminList = 
	static List<User> users = new ArrayList();
	static Map<User, String> name = new HashMap();
	
	public static Boolean running = true;
	
	private Logger log = LoggerFactory.getLogger(Server.class);
	
	private int port = serverConfig.getPort();
	private ExecutorService executor;
	
	public Server(ExecutorService executor) {
		super();
		this.executor = executor;
	}

	public void run() {
		log.info("server started");
		
		
		for(int i = 0; i < Server.adminList.size(); ++i)
		{
			log.info(Server.adminList.get(i).getUsername());
			log.info(Server.adminList.get(i).getPassword());
		}
		
		
		ServerSocket ss;
		try {
			ss = new ServerSocket(this.port);
			while (running) {
				Socket socket = ss.accept();
				ClientHandler handler = new ClientHandler(socket);
				executor.execute(handler);
			}
			if(!running)
			{
				ss.close();
				Main.executor.shutdown();
			}
		} catch (IOException e) {
			log.error("Something went wrong :/", e);
		}
	}

}
