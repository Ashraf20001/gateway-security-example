package com.supercharge.gateway.auth.service;

import java.security.Principal;

import com.cbt.supercharge.exception.core.ApplicationException;
import com.cbt.supercharge.transfer.objects.entity.UserProfile;
import com.cbt.supercharge.transfter.objects.core.entity.vo.SystemConfigVo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.supercharge.gateway.models.AuthenticationResponse;

/**
 * @author CBT
 */

public interface IAuthService {

	AuthenticationResponse generateAuthenticationToken(Principal user, UserProfile userProfile)
			throws ApplicationException, JsonProcessingException;

	boolean isUserAuthenticated(UserProfile user, AuthenticationResponse resp, Integer expiryPeriod,
			Integer warningPeriod, Integer failureAttempts, String autoRelease) throws ApplicationException, JsonProcessingException;

	AuthenticationResponse refreshAuthenticationToken(String refreshToken) throws ApplicationException, JsonProcessingException;

	public SystemConfigVo getConfigName(String configName);

	/**
	 * @param userId
	 * @return 
	 */
	String clearCache();

}
