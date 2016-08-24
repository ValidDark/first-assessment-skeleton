package com.cooksys.assessment.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cooksys.assessment.model.Message;
import com.cooksys.assessment.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientHandler implements Runnable {
	private Logger log = LoggerFactory.getLogger(ClientHandler.class);

	private Socket socket;

	//static ArrayList<User> users = new ArrayList();
	//static HashMap<User, String> nameChanged = new HashMap();
	
	ObjectMapper mapper = new ObjectMapper();
	
	String writeBuffer = "";
	
	
	String helpMsg = "--Current Commands--\n" +
			"echo help (shows help menu)\n" +
			"echo login <name> <pass> (logs into admin with name <name> and password <pass>\n" +
			"broadcast rollDice <#> (rolls a dice with <#> many sides)\n";
			
	String usersOnline = "Users Online : \n";
	
	

	public ClientHandler(Socket socket) {
		super();
		this.socket = socket;
	}
	
	public void SendToSelf(Message message) throws IOException
	{
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
		writer.write(mapper.writeValueAsString(message));
		writer.flush();
		sleepy(); // makes thread sleep for a bit, !important!
	}
	
	public void SendToAll(Message message) throws IOException
	{
		for (int i = 0; i < Server.users.size(); ++i) {  
				PrintWriter writer = new PrintWriter(new OutputStreamWriter(Server.users.get(i).getSocket().getOutputStream()));
				writer.write(mapper.writeValueAsString(message));
				writer.flush();
			}
		sleepy(); // makes thread sleep for a bit, !important!
	}
	
	public void sleepy()
	{
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	User thisClient;
	
	public void run() {
		try {

			String toUser = "";
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

			String timeStamp = new SimpleDateFormat("EEE, MMM d, yyyy, hh:mm:ss aa").format(new Date());

			while (!socket.isClosed()) {
				
				String raw = reader.readLine();
				Message message = mapper.readValue(raw, Message.class);		
				timeStamp = new SimpleDateFormat("EEE, MMM d, yyyy, hh:mm:ss aa").format(new Date());

				
				if(Server.name.get(thisClient) == null)
				{
				log.info("New user trying to connect as <{}> {}", message.getUsername(),timeStamp);	
				}
				else{
				log.info("user <{}> doing command: <{}> {}", Server.name.get(thisClient), message.getCommand(),
						timeStamp);
				}
				
				if ( message.getCommand().length() >= 1 && message.getCommand().charAt(0) == '@' ) {
					toUser = message.getCommand().substring(1);
					message.setCommand("@");
				}

				switch (message.getCommand()) {
				
				
				
				
				case "connect":
					
					synchronized(Server.class)
					{
					message.setUsername(message.getUsername().replaceAll("\\s+",""));
					
					if(message.getUsername().equals("")){
						log.info(message.getUsername());
						message.setUsername("_");
						log.info(message.getUsername());
					}
					
					
					
					for (int i = 0; i < Server.users.size(); ++i) {
						if (message.getUsername().equals(Server.users.get(i).getUsername())) {  
							String id = String.format("%04d", new Random().nextInt(10000));							
							message.setUsername(message.getUsername() + id);
							
							message.setContents(Server.users.get(i).getUsername() + " was taken.  Your name was set to: " + message.getUsername());
							writeBuffer = mapper.writeValueAsString(message);
							writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
							writer.write(writeBuffer);
							writer.flush();
						}	
						}
					
					sleepy(); // makes thread sleep for a bit, !important!
					
					thisClient = new User(message.getUsername(), this.socket); //setup user object
					
					Server.users.add(thisClient); //add user Object to users list
					
					
					Server.name.put(thisClient,message.getUsername()); //set the servers version of their name
					
					log.info("user <{}> connected from: {}", message.getUsername(), socket.getRemoteSocketAddress().toString()); //log to server
					
					message.setContents(timeStamp + ": <" + message.getUsername() + "> has connected.");
					writeBuffer = mapper.writeValueAsString(message);
					
					
					for (int i = 0; i < Server.users.size(); ++i) {
						writer = new PrintWriter(new OutputStreamWriter(Server.users.get(i).getSocket().getOutputStream()));
						writer.write(writeBuffer);
						writer.flush();

					}

					
					sleepy(); // makes thread sleep for a bit, !important!
					
					//send the MOTD to client when they connect
					
					
					message.setContents(Server.serverConfig.getMOTD());
					writeBuffer = mapper.writeValueAsString(message);
					writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
					writer.write(writeBuffer);
					writer.flush();
					
					
					
					
					
					// debugging purposes only
					log.info("Users found: ");

					for (int i = 0; i < Server.users.size(); ++i) {
						log.info(Server.users.get(i).getUsername());
					}

					break;
					}
					
					
					
					
				case "disconnect":
					
					log.info("user <{}> disconnected", Server.name.get(thisClient));

					message.setContents(timeStamp + " <" + Server.name.get(thisClient) + "> has disconnected.");

					writeBuffer = mapper.writeValueAsString(message);

					for (int i = 0; i < Server.users.size(); ++i) {
						writer = new PrintWriter(new OutputStreamWriter(Server.users.get(i).getSocket().getOutputStream()));
						writer.write(writeBuffer);
						writer.flush();
					}
					
					this.socket.close();
					Server.users.remove(thisClient);
					Server.name.remove(thisClient);

					break;
					
					
					
					
					//Going to piggyback most admin commands on echo.
				case "echo":
					synchronized(Server.class)
					{
					if(message.getContents().trim().length() >= 1)
					{
					
					message.setContents(message.getContents().trim() + " ");				
					String command = message.getContents().substring(0, message.getContents().indexOf(' '));
					Integer trim = message.getContents().indexOf(' ') + 1;
					log.info("echo command was: <" + command + ">");
					
					switch (command) {
					
					case "changeName":  
						message.setContents(message.getContents().substring(trim) + " ");
						command = message.getContents().substring(0, message.getContents().indexOf(' '));
						trim = message.getContents().indexOf(' ') + 1;
						
						boolean nameFound = false;
						
						log.info("name to change: " + command);
						
						
						for (Entry<User, String> entry : Server.name.entrySet()) {
				        	usersOnline += entry.getValue() + "\n";
				        	
				            if (entry.getValue().equals(command)) {
				            	
				            	nameFound = true;
								
				            	message.setContents(message.getContents().substring(trim) + " ");
								
				            	String msgBuffer = Server.name.get(thisClient) + " has changed " + command +"'s name to : ";
				            	
				            	command = message.getContents().substring(0, message.getContents().indexOf(' '));
								trim = message.getContents().indexOf(' ') + 1;
								
								message.setContents(msgBuffer + command);
				            	entry.setValue(command);
				            	
				            	SendToAll(message);
				            	
				            }
					}
						
					
						
						if(nameFound == false)
						{
							message.setCommand("connect");
							message.setContents("--Username does not exist--");
							SendToSelf(message);
							
							message.setContents(usersOnline);
							SendToSelf(message);
						}
						
						break;
					
					case "shutdown":  
						
						try {
							message.setCommand("connect");
							message.setContents("----------SERVER SHUTTING DOWN IN----------");
							SendToAll(message);
							message.setContents("----------5----------");
							SendToAll(message);
							Thread.sleep(1000);
							message.setContents("----------4----------");
							SendToAll(message);
							Thread.sleep(1000);
							message.setContents("----------3----------");
							SendToAll(message);
							Thread.sleep(1000);
							message.setContents("----------2----------");
							SendToAll(message);
							Thread.sleep(1000);
							message.setContents("----------1----------");
							SendToAll(message);
							Thread.sleep(1000);
							message.setContents("------Good-Bye-------");
							SendToAll(message);
							Server.running = false;
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						break;
					
		            case "help":  
						message.setCommand("connect");
						message.setContents(helpMsg);
						SendToSelf(message);
						break;
						
		            case "login":
		            {
						message.setContents(message.getContents().substring(trim) + " ");
						command = message.getContents().substring(0, message.getContents().indexOf(' '));
						trim = message.getContents().indexOf(' ') + 1;
						nameFound = false;
						boolean passFound = false;
						
						log.info("login to: " + command);
						for(int i = 0; i < Server.adminList.size(); ++i)
						{
							
							if(command.trim().equals(Server.adminList.get(i).getUsername()))
									{
										nameFound = true;
										
										message.setContents(message.getContents().substring(trim) + " ");
										command = message.getContents().substring(0, message.getContents().indexOf(' '));
										log.info("using password: " + command);
										if(command.trim().equals(Server.adminList.get(i).getPassword()))
										{
											passFound = true;
											thisClient.setAdminLvl(Server.adminList.get(i).getPermission());
											
											message.setCommand("connect"); //to set it as a system message, a compatable system message
											message.setContents("--"+Server.name.get(thisClient)+" has just logged in as an Admin!--");
											SendToAll(message);
											
											if(thisClient.getAdminLvl() > 0) //change the admins help message.
											{
												helpMsg = helpMsg +
												"echo changeName <old> <new> (changes a users name from <old> to <new>\n";
											}
											
											
											
											
										}
										
									}
							
						}
						
						if(nameFound == false)
						{
							message.setCommand("connect");
							message.setContents("--Admin name does not exist--");
							SendToSelf(message);
						}
						if(nameFound == true && passFound == false)
						{
							message.setCommand("connect");
							message.setContents("--Incorrect Password--");
							SendToSelf(message);
						}
		            }
						break;
					
				default:
					log.info("user <{}> echoed message <{}>", Server.name.get(thisClient), message.getContents());
					message.setContents(timeStamp + " <" + Server.name.get(thisClient) + "> (echo): " + message.getContents());
					SendToSelf(message);
					break;
					}
					
					}
					break;
					}
					
					
					
					
				case "broadcast":
					synchronized(Server.class)
					{
					if(message.getContents().trim().length() >= 1)
					{
						
						message.setContents(message.getContents().trim() + " ");				
						String command = message.getContents().substring(0, message.getContents().indexOf(' '));
						Integer trim = message.getContents().indexOf(' ') + 1;
						log.info("broadcast command was: <" + command + ">");
						
						
						if(command.equals("rollDice"))
						{
							
							message.setContents(message.getContents().substring(trim) + " ");
							command = message.getContents().substring(0, message.getContents().indexOf(' '));
							trim = message.getContents().indexOf(' ') + 1;
							
							log.info("<"+command+">");
							log.info(command);
							log.info(command);
							log.info(command);
							
							if (command.matches("[0-9]+") && command.length() >= 1)
							{
							int roll = new Random().nextInt(Integer.parseInt(command))+1;
							message.setCommand("connect");
							message.setContents(Server.name.get(thisClient)+ " rolls a " + command + " sided dice and gets a : " + roll); 
							SendToAll(message);
							}
							else
							{
								message.setCommand("connect");
								message.setContents(command + "is not a number!");
								SendToSelf(message);
							}
						}
						
						
						
						
					else{
					log.info("user <{}> tried to broadcast <{}>", Server.name.get(thisClient), message.getContents());

					message.setContents(timeStamp + " <" + Server.name.get(thisClient) + "> (all): " + message.getContents());
					SendToAll(message);
					}
					}
					break;
					}
					
					
					
					
					
					
					
					
				case "@":
					
					message.setCommand("@" + toUser);

					boolean userFound = false;

					message.setContents(timeStamp + " <" + Server.name.get(thisClient) + "> (whisper): " + message.getContents());

					

						        for (Entry<User, String> entry : Server.name.entrySet()) {
						        	usersOnline += entry.getValue() + "\n";
						        	
						            if (entry.getValue().equals(toUser)) {
						            	
						            	userFound = true;
										writer = new PrintWriter( new OutputStreamWriter( entry.getKey().getSocket().getOutputStream()));	
						            	
						                System.out.println(entry.getKey());
						            }


							}

					if (!userFound) {
						message.setContents("User <" + toUser + "> not found!\n" + usersOnline); 
																																										
						writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream())); 
																									
					}

					writeBuffer = mapper.writeValueAsString(message);
					writer.write(writeBuffer);
					writer.flush();

					break;
					
					
					
					
				case "users":
					usersOnline = "Users Online : \n";
					
					
					for (Entry<User, String> entry : Server.name.entrySet()) {
			        	usersOnline += entry.getValue() + "\n";
					}
					


					log.info("user <{}> got list of users", Server.name.get(thisClient));

					message.setContents(usersOnline);

					writeBuffer = mapper.writeValueAsString(message);

					writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

					writer.write(writeBuffer);
					writer.flush();
					break;

				default:
					log.info("user <{}> intered invalid command: <{}>", Server.name.get(thisClient), message.getCommand());

					message.setContents("Invalid Command!");
					writeBuffer = mapper.writeValueAsString(message);
					writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
					writer.write(writeBuffer);
					writer.flush();
					break;

				}
			}

		} catch (SocketException e) {
			log.error("Something went wrong with the sockets :/", e);
			try {
				
				String timeStamp = new SimpleDateFormat("EEE, MMM d, yyyy, hh:mm:ss aa").format(new Date());

				Message message = new Message();


				for (int i = 0; i < Server.users.size(); ++i) {
					if(Server.users.get(i).getSocket() == this.socket)
					{
						message.setUsername(Server.users.get(i).getUsername());
						message.setContents(timeStamp + " <" + message.getUsername() + "> has disconnected due to an error on their end.");
						log.info("user <{}> disconnected due to error on their end.", message.getUsername());
						String dcMsg = mapper.writeValueAsString(message);
						PrintWriter writer = new PrintWriter(new OutputStreamWriter(Server.users.get(i).getSocket().getOutputStream()));
						writer.write(dcMsg);
						writer.flush();
						this.socket.close();
						Server.users.remove(i);
					}
					

				}
				
				
				
				socket.close();
				
				
				
				
				
				
				
				
				
			} catch (IOException e1) {
				log.error("couldn't close the socket");
				e1.printStackTrace();
			}
		} catch (IOException e) {
			log.error("Something went wrong with the JSON :/", e);
		}
	}

}
