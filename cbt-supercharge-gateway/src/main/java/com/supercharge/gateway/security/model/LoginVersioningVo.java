package com.supercharge.gateway.security.model;

import java.util.Date;

public class LoginVersioningVo {

	private String userName;
	private String ipAddress;
	private Date loginTime = new Date();
	private String status;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public Date getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(Date loginTime) {
		this.loginTime = loginTime;
	}

	@Override
	public String toString() {
		return "LoginVersioningVo [userName=" + userName + ", ipAddress=" + ipAddress + ", loginTime=" + loginTime
				+ "]";
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	

}
