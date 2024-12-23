package com.supercharge.gateway.security.model;

import org.springframework.stereotype.Component;

@Component
public class UserSecurityToken {

	private String username;

	private String token;

	public UserSecurityToken() {
	}

	public UserSecurityToken(String username, String token) {
		this.username = username;
		this.token = token;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

}
