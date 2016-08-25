package com.cooksys.assessment.model;

import java.net.Socket;

public class User {

	private String username;
	private Socket socket;
	private int adminLvl = 0; //what level admin they are, 0 for none,

	public User(String username, Socket socket) {
		super();
		this.username = username;
		this.socket = socket;
	}
	
	public int getAdminLvl() {
		return adminLvl;
	}

	public void setAdminLvl(int adminLvl) {
		this.adminLvl = adminLvl;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((username == null) ? 0 : username.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}

	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public Socket getSocket() {
		return socket;
	}
	public void setSocket(Socket socket) {
		this.socket = socket;
	}

}
