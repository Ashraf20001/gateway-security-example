/*
 * @author codeboard
 */
package com.supercharge.gateway.auth.service;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.cbt.supercharge.transfer.objects.entity.UserPassword;
import com.cbt.supercharge.transfer.objects.entity.UserProfile;
import com.cbt.supercharge.utils.core.ApplicationUtils;
import com.supercharge.gateway.common.base.dao.CommonUserDaoImpl;

/**
 * The Class UserDetailsServiceImpl.
 */
@Service

public class UserDetailsServiceImpl implements UserDetailsService {
	
	@Autowired
	private CommonUserDaoImpl commonUserDaoImpl;

	/**
	 * The Constant logger.
	 */
	private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

	private String getPassword(UserPassword password) {
		return password != null ? password.getUmUsrPwd() : null;
	}

	/**
	 * Load user by username.
	 *
	 * @param username the username
	 * @return the user details
	 */
	@Override
	public UserDetails loadUserByUsername(String userId) {
		UserProfile user = commonUserDaoImpl.getUserBySystemUserID(userId);
		if (!ApplicationUtils.isValidateObject(user)) {
			logger.error("User not founded in the database.");
			throw new UsernameNotFoundException("User not found in the database.");
		}
		UserPassword password = null;
		if (ApplicationUtils.isValidateObject(user.getUserPassword())) {
			password = user.getUserPassword().stream().map(b -> b).findAny().orElse(null);
		}
		if (!ApplicationUtils.isValidateObject(password)) {
			try {
				password = commonUserDaoImpl.getPasswordByUserId(user.getUserId());
			} catch (Exception e) {
				logger.info("Exception..." + e.getLocalizedMessage());
			}
		}
		if (!ApplicationUtils.isValidateObject(password)) {
			logger.error("Password not found for this user.");
			throw new UsernameNotFoundException("Password not found for this user.");
		}
		return new org.springframework.security.core.userdetails.User(user.getUserIdentificationNumber(),
				getPassword(password), new ArrayList<>());
	}

	/**
	 * @param commonUserDaoImpl the commonUserDaoImpl to set
	 */
	public void setCommonUserDaoImpl(CommonUserDaoImpl commonUserDaoImpl) {
		this.commonUserDaoImpl = commonUserDaoImpl;
	}

}
