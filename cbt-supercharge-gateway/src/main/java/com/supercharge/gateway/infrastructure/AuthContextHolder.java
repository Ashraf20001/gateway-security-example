package com.supercharge.gateway.infrastructure;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

public class AuthContextHolder {

	private AuthContextHolder() {
		throw new IllegalStateException("Context holder class does not allows to create an instance");
	}

	private static final ThreadLocal<String> AuthTokenHolder = new ThreadLocal<>();

	private static final ThreadLocal<UsernamePasswordAuthenticationToken> SECURITYHOLDER = new ThreadLocal<>();

	public static void setAuthToken(String authToken) {
		AuthTokenHolder.set(authToken);
	}

	public static String getAuthToken() {
		return AuthTokenHolder.get();

	}

	public static void setSecurityHolder(UsernamePasswordAuthenticationToken user) {
		SECURITYHOLDER.set(user);

	}

	public static UsernamePasswordAuthenticationToken getSecurityHolder() {
		return SECURITYHOLDER.get();

	}
}
