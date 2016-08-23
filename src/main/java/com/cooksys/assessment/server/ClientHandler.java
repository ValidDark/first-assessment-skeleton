package com.cooksys.assessment.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cooksys.assessment.model.Message;
import com.cooksys.assessment.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientHandler implements Runnable {
	private Logger log = LoggerFactory.getLogger(ClientHandler.class);

	private Socket socket;
	
	static ArrayList<User> users = new ArrayList();

	public ClientHandler(Socket socket) {
		super();
		this.socket = socket;
	}

	public void run() {
		try {

			ObjectMapper mapper = new ObjectMapper();
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

			String timeStamp = new SimpleDateFormat("EEE, MMM d, yyyy, hh:mm:ss aa").format(new Date());
			
			while (!socket.isClosed()) {
				String raw = reader.readLine();
				Message message = mapper.readValue(raw, Message.class);

				timeStamp = new SimpleDateFormat("EEE, MMM d, yyyy, hh:mm:ss aa").format(new Date());
				
				log.info("user <{}> doing command: <{}>  :: {}", message.getUsername(), message.getCommand(), timeStamp);
				
				switch (message.getCommand()) {
					case "connect":
						log.info("user <{}> connected", message.getUsername());
						users.add(new User(message.getUsername(), this.socket));
						
						message.setContents(timeStamp + " <" + message.getUsername() + "> has connected.");
						
						String cMsg = mapper.writeValueAsString(message);
						
						for(int i = 0; i < users.size(); ++i)
						{
							writer = new PrintWriter(new OutputStreamWriter(users.get(i).getSocket().getOutputStream()));
							writer.write(cMsg);
							writer.flush();
						}						
						
						
						
						
						//debugging purposes only
						log.info("Users found: ");

						for(int i = 0; i < users.size(); ++i)
						{
							log.info(users.get(i).getUsername());
						}
						
						
						break;
					case "disconnect":
						log.info("user <{}> disconnected", message.getUsername());
						
						message.setContents(timeStamp + " <" + message.getUsername() + "> has disconnected.");
						
						String dcMsg = mapper.writeValueAsString(message);
						
						for(int i = 0; i < users.size(); ++i)
						{
							writer = new PrintWriter(new OutputStreamWriter(users.get(i).getSocket().getOutputStream()));
							writer.write(dcMsg);
							writer.flush();
						}
						
						
						
						this.socket.close();
						
						users.remove(message.getUsername());
						
						
						break;
					case "echo":
						log.info("user <{}> echoed message <{}>", message.getUsername(), message.getContents());
						String echoBuffer = message.getContents();
						message.setContents(timeStamp + " <" + message.getUsername() + "> (echo): " + echoBuffer);
						String response = mapper.writeValueAsString(message);
						writer.write(response);
						writer.flush();
						break;
						
					case "broadcast":
						log.info("user <{}> tried to broadcast <{}>", message.getUsername(), message.getContents());
						
						String msgBuffer = message.getContents();
						message.setContents(timeStamp + " <" + message.getUsername() + "> (all): " + msgBuffer);
						
						String bCast = mapper.writeValueAsString(message);
						
						for(int i = 0; i < users.size(); ++i)
						{
							writer = new PrintWriter(new OutputStreamWriter(users.get(i).getSocket().getOutputStream()));
							writer.write(bCast);
							writer.flush();
						}
						
						
						break;
					case "@":
						String toUser = message.getContents().substring(1, message.getContents().indexOf(' '));
						
						
						boolean userFound = false;
						
						log.info("DEBUGGING @ INFO: full msg:" + message.getContents() + " ---- user being sent to: " + toUser + "  actual msg: " + message.getContents().substring(message.getContents().indexOf(' ')));
						
						msgBuffer = message.getContents().substring(message.getContents().indexOf(' '));
						message.setContents(timeStamp + " <" + message.getUsername() + "> (whisper): " + msgBuffer);

						for(int i = 0; i < users.size(); ++i)
						{
							log.info("current user options, one should match the user being msg'd :  " + users.get(i).getUsername());
							if(users.get(i).getUsername().equals(toUser))
							{
								userFound = true;
								writer = new PrintWriter(new OutputStreamWriter(users.get(i).getSocket().getOutputStream()));
							}
						}
						
						if (!userFound)
						{
							message.setContents("User " + toUser + " not found!");    //if user not found
							writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream())); //set output to own socket
						}
						
						String pMsg = mapper.writeValueAsString(message);
						writer.write(pMsg);
						writer.flush();
						
						
						break;
					case "users":
						String usersOnline = "Users Online : \n";
						for(int i = 0; i < users.size(); ++i)
						{
							usersOnline += (users.get(i).getUsername()+"\n");	
						}
						
						log.info("user <{}> got list of users", message.getUsername());
						
						message.setContents(usersOnline);
						
						String checkUsers = mapper.writeValueAsString(message);
						writer.write(checkUsers);
						writer.flush();
						break;
						
					 default: 
						 log.info("user <{}> intered invalid command: <{}>", message.getUsername(), message.getCommand());
							
						 message.setContents("Invalid Command!");
						 response = mapper.writeValueAsString(message);
							writer.write(response);
							writer.flush();
                     break;

				}
			}

		} catch (IOException e) {
			log.error("Something went wrong :/", e);
		}
	}

}
