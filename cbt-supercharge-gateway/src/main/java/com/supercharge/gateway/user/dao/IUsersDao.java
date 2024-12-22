package com.supercharge.gateway.user.dao;

import com.cbt.supercharge.exception.core.ApplicationException;
import com.cbt.supercharge.transfer.objects.entity.UserProfile;

import reactor.core.publisher.Mono;

public interface IUsersDao {

	/**
	 * @param userId
	 * @return
	 */
	public UserProfile getUserBySystemUserID(String userId);

	/**
	 * @param identity
	 * @return
	 */
	public UserProfile getUserByIdentity(String identity);

	/**
	 * @param userProfile
	 * @return
	 */
	public String updateUser(UserProfile userProfile);

	/**
	 * @param userName
	 * @return
	 */
	public UserProfile getUserByName(String userName);

	/**
	 * @param email
	 * @return
	 */
	public UserProfile getUserByEmailId(String email);

	/**
	 * @return
	 */
//	public Mono<String> getLoggedInUserName() throws ApplicationException;
	
	public String getLoggedInUserName() throws ApplicationException;

}
