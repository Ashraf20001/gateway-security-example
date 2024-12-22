package com.supercharge.gateway.security.model;

public class AppUserVo {
	
	private String userName;

	private String role;
	
	public String getUserName() {
		return userName;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public AppUserVo() {
		super();
	}

	public AppUserVo(String userName, String role) {
		super();
		this.userName = userName;
		this.role = role;
	}

}
