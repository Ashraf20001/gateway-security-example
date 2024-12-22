package com.supercharge.gateway.user.service;

import java.util.List;

import com.cbt.supercharge.exception.core.ApplicationException;
import com.cbt.supercharge.transfer.objects.entity.SystemConfig;
import com.cbt.supercharge.transfer.objects.entity.UserPassword;
import com.cbt.supercharge.transfer.objects.entity.UserProfile;
import com.cbt.supercharge.transfter.objects.core.dto.ChangePasswordDTO;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface IUserService {

	String getInstituteIdentityFromBaseToken(String userId) throws ApplicationException;

	List<String> getMappedInstitutionsBySystemUserID(String email) throws ApplicationException;

	String getUserIdFromBasicAuthToken(String token) throws ApplicationException;

	boolean updateUserPassword(ChangePasswordDTO changePasswordDTO, UserProfile user)
			throws ApplicationException, JsonProcessingException;

	boolean sendForgotPasswordMail(String passwordResetKey, String email) throws ApplicationException;

	/**
	 * @param changePasswordDTO
	 * @param userProfile
	 * @param userProfile
	 * @param userProfile
	 * @return boolean
	 * @throws JsonProcessingException
	 * @throws ApplicationException̥̥
	 */
	boolean getChangePassword(ChangePasswordDTO changePasswordDTO) throws ApplicationException, JsonProcessingException;

	void validatePassword(ChangePasswordDTO changePasswordDTO, UserProfile user, UserPassword existingPassword,
			Boolean isNewPwd, Integer passwordPolicyCount, List<SystemConfig> systemConfigList)
			throws ApplicationException;

}
