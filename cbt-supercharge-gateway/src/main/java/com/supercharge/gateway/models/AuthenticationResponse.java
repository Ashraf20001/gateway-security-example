/*
 * @author codeboard
 */
package com.supercharge.gateway.models;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;

/**
 * Gets the token type.
 *
 * @return the token type
 */
@Getter

/**
 * Sets the token type.
 *
 * @param token_type the new token type
 */
@Setter
@JsonInclude(Include.NON_NULL)
public class AuthenticationResponse {


	/**
	 * The access token.
	 */
	private String accessToken;

	/**
	 * The api domain.
	 */
	private String apiDomain;

	/**
	 * The expires in.
	 */
	private int expiresIn;

	private String firstName;
	
	private String userName;

	private String institutionId;

	private Boolean isTwoFA;

	private String lastName;

	/** The token type. */
	private byte[] logo;

	/**
	 * The Force update page url. will be true once password got expired in login.
	 */
	private boolean pwdForceUrl;

	/**
	 * The refresh token.
	 */
	private String refreshToken;
	/**
	 * logOutTime
	 */
	private String logOutTime;

	/**
	 * The token type.
	 */
	private String tokenType;

	private String twoFaUrl;

	private String identity;
	
	private String isWarningUser;

	private Date lastLoginDate;

	private String emailId;
	
	private Integer userId;
	
	/**
	 * isCifMasking
	 */
	private boolean isCifMasking;
	
	/**
	 * userIdentificationNumber
	 */
	private String userIdentificationNumber;
	

}
