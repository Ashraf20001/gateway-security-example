/*
 * @author codeboard
 */
package com.supercharge.gateway.common.base.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import com.cbt.supercharge.constants.core.ApplicationConstants;
import com.cbt.supercharge.exception.core.ApplicationException;
import com.cbt.supercharge.exception.core.ApplicationUnauthorizedException;
import com.cbt.supercharge.exception.core.codes.ErrorCodes;
import com.cbt.supercharge.exception.core.codes.ErrorId;
import com.cbt.supercharge.exception.core.codes.ErrorId.Severity;
import com.cbt.supercharge.transfer.objects.common.entity.SubFileMetaData;
import com.cbt.supercharge.transfer.objects.entity.SystemConfig;
import com.cbt.supercharge.transfer.objects.entity.UserProfile;
import com.cbt.supercharge.transfter.objects.core.ApplicationResponse;
import com.cbt.supercharge.transfter.objects.core.holder.LoggedInUserHolder;
import com.cbt.supercharge.utils.core.ApplicationUtils;
import com.supercharge.gateway.common.base.dao.CommonUserDaoImpl;
import com.supercharge.gateway.user.dao.UsersDaoImpl;

/**
 * The Class BaseService.
 */
@Service

public class GatewayBaseService {
	
	@Autowired
	private UsersDaoImpl usersDao;

	/**
	 * The Constant logger.
	 */
	private static final Logger logger = LoggerFactory.getLogger(GatewayBaseService.class);

	@Autowired
	@Qualifier("mongo")
	private CommonUserDaoImpl userDaoMongo;

	/**
	 * @return
	 */
	public List<SystemConfig> fetchCifMaskingConfiguration() {
		List<SystemConfig> cifMaskingConfig = userDaoMongo.getSystemPropertyGroup(ApplicationConstants.MASKING_LENGTH_GROUP);
		return cifMaskingConfig;
	}
	/**
	 * Gets the logged in user.
	 *
	 * @return the logged in user
	 * @throws ApplicationException 
	 */
	public UserProfile getLoggedInUser() throws ApplicationException {
		UserProfile appUser = null;
		if (ApplicationUtils.isValidateObject(LoggedInUserHolder.getAppUser())) {
			appUser = LoggedInUserHolder.getAppUser();
			logger.info("Returning LoggedIn User From ThreadLocal : {}", appUser.getIdentity());
			return appUser;
		}
		String userName =  usersDao.getLoggedInUserName();
		if (ApplicationUtils.isNotBlank(userName)) {
			//			appUser = userDaoMongo.getUserByName(userName);
			appUser = userDaoMongo.getUserBySystemUserID(userName);
		}
		if (appUser == null) {
			logger.error("App user not found for given user name. Given user name is :: {} ", userName);
			throw new ApplicationUnauthorizedException(new ErrorId(ErrorCodes.INVALID_USER.getErrorCode(),
					ErrorCodes.INVALID_USER.getErrorMessage(), Severity.FATAL), HttpStatus.UNAUTHORIZED);
		}
		logger.info("Returning LoggedIn User From Database");
		LoggedInUserHolder.setLoggedInUser(appUser);
		return appUser;
	}

	/**
	 * @param subListId
	 * @param list
	 * @param subFileMetaList
	 * @return
	 * @throws ApplicationException
	 */
	public ApplicationResponse getMaskedApplicationResponse(String subListId, List<Map<String, Object>> list,
			List<SubFileMetaData> subFileMetaList) throws ApplicationException {
		boolean isMasked = getLoggedInUser().isCifMarkedSts();
		if (Boolean.TRUE.equals(isMasked) && ApplicationUtils.isValidList(list)) {
			List<SystemConfig> cifMaskingConfig = fetchCifMaskingConfiguration();
			List<String> cifMaskableFields = filterCifMaskableFields(subFileMetaList);
			List<Map<String, Object>> maskedContent = maskContent(list, cifMaskableFields, cifMaskingConfig);
			return getApplicationResponse(maskedContent);
		}
		return getApplicationResponse(list);
	}

	/**
	 * @param response
	 * @param toMaskFields
	 * @param maskLength
	 * @return
	 */
	private List<Map<String, Object>> maskResponse(List<Map<String, Object>> response, List<String> toMaskFields,
			Integer maskLength) {
		return response.stream().map(map -> {
			Map<String, Object> maskedMap = map.entrySet().stream().filter(
					entry -> ApplicationUtils.isValidString(entry.getKey()) && toMaskFields.contains(entry.getKey()))
					.collect(Collectors.toMap(Map.Entry::getKey, entry -> {
						String value = ApplicationUtils.isValidateObject(entry.getValue()) ? entry.getValue().toString()
								: ApplicationConstants.WITHOUT_SPACE;
						return ApplicationUtils.maskCharacters(value, maskLength != null ? maskLength : value.length());
					}));
			map.putAll(maskedMap);
			return map;
		}).toList();
	}

	/**
	 * @param response
	 * @param toAskFields
	 * @param configList
	 * @return
	 */
	public List<Map<String, Object>> maskContent(List<Map<String, Object>> response, List<String> toAskFields,
			List<SystemConfig> configList) {
		SystemConfig cifMaskingConfiguration = configList.stream()
				.filter(config -> config.getConfigName().equals("CifMasking")).findFirst().orElse(null);
		SystemConfig maskingLenthConfig = configList.stream()
				.filter(a -> a.getConfigName().equals(ApplicationConstants.CIF_MASKING_LENGTH)).findFirst()
				.orElse(null);
		if (!ApplicationUtils.isValidateObject(cifMaskingConfiguration)) {
			logger.info("CIF Config not available");
			return response;
		}
		if (cifMaskingConfiguration.getConfigValue().equals(ApplicationConstants.CONFIG_FALSE)) {
			return applyFullMasking(response, toAskFields);
		} else {
			return applyHalfMasking(response, toAskFields, maskingLenthConfig);
		}
	}


	/**
	 * @param subFileMetaList
	 * @return
	 */
	public List<String> filterCifMaskableFields(List<SubFileMetaData> subFileMetaList) {
		return subFileMetaList.stream()
				.filter(metaData -> Boolean.TRUE.equals(metaData.isCifMasking()))
				.map(SubFileMetaData::getColumnName)
				.collect(Collectors.toList());
	}

	/**
	 * @param content
	 * @return
	 */
	public ApplicationResponse getApplicationResponse(Object content) {
		ApplicationResponse applicationResponse = new ApplicationResponse();
		if (content != null) {
			applicationResponse.setContent(content);
		}
		return applicationResponse;
	}

	/**
	 * @param response
	 * @param toMaskFields
	 * @return
	 */
	private List<Map<String, Object>> applyFullMasking(List<Map<String, Object>> response, List<String> toMaskFields) {
		return maskResponse(response, toMaskFields, null);
	}


	/**
	 * @param response
	 * @param toFieldsMask
	 * @param maskingLengthConfig
	 * @return
	 */
	private List<Map<String, Object>> applyHalfMasking(List<Map<String, Object>> response, List<String> toFieldsMask,
			SystemConfig maskingLengthConfig) {
		if(!ApplicationUtils.isValidateObject(maskingLengthConfig)) {
			logger.info("CIF Config Length not available");
			return response;
		}
		return maskResponse(response, toFieldsMask, Integer.parseInt(maskingLengthConfig.getConfigValue()));
	}

	public String getLoggedInUserName() throws ApplicationUnauthorizedException {

//		Boolean attributes = Boolean
//				.parseBoolean(((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest()
//						.getParameter(ApplicationConstants.LDAP_NOT));
		Authentication isAuth = SecurityContextHolder.getContext().getAuthentication();
		if (SecurityContextHolder.getContext().getAuthentication() == null) {
			return null;
		}

		System.out.println(isAuth);
		Object obj = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (obj instanceof User) {
			User user = (User) obj;
			return user.getUsername();
		}
		throw new ApplicationUnauthorizedException(new ErrorId(ErrorCodes.INVALID_USER.getErrorCode(),
				ErrorCodes.INVALID_USER.getErrorMessage(), Severity.FATAL), HttpStatus.UNAUTHORIZED);

	}

	/**
	 * @param userDaoMongo
	 */
	public void setUserDaoMongo(CommonUserDaoImpl userDaoMongo) {
		this.userDaoMongo = userDaoMongo;
	}
}
