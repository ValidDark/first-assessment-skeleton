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
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cooksys.assessment.model.Message;
import com.cooksys.assessment.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientHandler implements Runnable {
	private Logger log = LoggerFactory.getLogger(ClientHandler.class);

	private Socket socket;

	//static ArrayList<User> users = new ArrayList();
	//static HashMap<User, String> nameChanged = new HashMap();
	
	ObjectMapper mapper = new ObjectMapper();
	


	public ClientHandler(Socket socket) {
		super();
		this.socket = socket;
	}

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

				log.info("user <{}> doing command: <{}>  :: {}", message.getUsername(), message.getCommand(),
						timeStamp);

				if (message.getCommand().charAt(0) == '@') {
					toUser = message.getCommand().substring(1);
					message.setCommand("@");
				}

				switch (message.getCommand()) {
				case "connect":

					for (int i = 0; i < Server.users.size(); ++i) {
						if (!message.getUsername().equals(Server.users.get(i).getUsername())) {
							Server.nameChanged.put(Server.users.get(i),Server.users.get(i).getUsername());
						}
						else
						{
							String id = String.format("%04d", new Random().nextInt(10000));
							

							message.setUsername(message.getUsername() + id);

							Server.nameChanged.put(Server.users.get(i),message.getUsername());
							
							
							message.setContents(Server.users.get(i).getUsername() + " was taken.  Your name was set to: "
									+ message.getUsername());

							String conResponse = mapper.writeValueAsString(message);
							writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream())); // set
																										// writer
																										// to
																										// write
																										// to
																										// self
							writer.write(conResponse);
							writer.flush();

						}
					}

					log.info("user <{}> connected from: {}", message.getUsername(), socket.getRemoteSocketAddress().toString());
					Server.users.add(new User(message.getUsername(), this.socket));

					message.setContents(timeStamp + " <" + message.getUsername() + "> has connected.");

					String cMsg = mapper.writeValueAsString(message);

					for (int i = 0; i < Server.users.size(); ++i) {
						writer = new PrintWriter(new OutputStreamWriter(Server.users.get(i).getSocket().getOutputStream()));
						writer.write(cMsg);
						writer.flush();
					}

					// debugging purposes only
					log.info("Users found: ");

					for (int i = 0; i < Server.users.size(); ++i) {
						log.info(Server.users.get(i).getUsername());
					}

					break;
				case "disconnect":
					log.info("user <{}> disconnected", message.getUsername());

					message.setContents(timeStamp + " <" + message.getUsername() + "> has disconnected.");

					String dcMsg = mapper.writeValueAsString(message);

					for (int i = 0; i < Server.users.size(); ++i) {
						writer = new PrintWriter(new OutputStreamWriter(Server.users.get(i).getSocket().getOutputStream()));
						writer.write(dcMsg);
						writer.flush();

						if (message.getUsername().equals(Server.users.get(i).getUsername())) {
							this.socket.close();
							Server.users.remove(i);
						}

					}

					break;
				case "echo":
					log.info("user <{}> echoed message <{}>", message.getUsername(), message.getContents());
					String echoBuffer = message.getContents();
					message.setContents(timeStamp + " <" + message.getUsername() + "> (echo): " + echoBuffer);
					String response = mapper.writeValueAsString(message);
					writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream())); // set
																								// writer
																								// to
																								// write
																								// to
																								// self
					writer.write(response);
					writer.flush();
					break;

				case "broadcast":
					log.info("user <{}> tried to broadcast <{}>", message.getUsername(), message.getContents());

					String msgBuffer = message.getContents();
					message.setContents(timeStamp + " <" + message.getUsername() + "> (all): " + msgBuffer);

					String bCast = mapper.writeValueAsString(message);

					for (int i = 0; i < Server.users.size(); ++i) {
						writer = new PrintWriter(new OutputStreamWriter(Server.users.get(i).getSocket().getOutputStream()));
						writer.write(bCast);
						writer.flush();
					}

					break;
				case "@":
					String usersOnline = "Users Online : \n";
					message.setCommand("@" + toUser);

					boolean userFound = false;

					msgBuffer = message.getContents();
					message.setContents(timeStamp + " <" + message.getUsername() + "> (whisper): " + msgBuffer);

					for (int i = 0; i < Server.users.size(); ++i) {
						usersOnline += (Server.users.get(i).getUsername() + "\n");
						if (Server.users.get(i).getUsername().equals(toUser)) {
							userFound = true;
							writer = new PrintWriter(
									new OutputStreamWriter(Server.users.get(i).getSocket().getOutputStream()));
						}
					}

					if (!userFound) {
						message.setContents("User <" + toUser + "> not found!\n" + usersOnline); // if
																									// user
																									// not
																									// found
						writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream())); // set
																									// output																	// own
																									// socket
					}

					String pMsg = mapper.writeValueAsString(message);
					writer.write(pMsg);
					writer.flush();

					break;
				case "users":
					usersOnline = "Users Online : \n";
					for (int i = 0; i < Server.users.size(); ++i) {
						usersOnline += (Server.users.get(i).getUsername() + "\n");
					}

					log.info("user <{}> got list of users", message.getUsername());

					message.setContents(usersOnline);

					String checkUsers = mapper.writeValueAsString(message);

					writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

					writer.write(checkUsers);
					writer.flush();
					break;

				default:
					log.info("user <{}> intered invalid command: <{}>", message.getUsername(), message.getCommand());

					message.setContents("Invalid Command!");
					response = mapper.writeValueAsString(message);
					writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
					writer.write(response);
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
