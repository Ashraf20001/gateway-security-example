/*
 * @author codeboard
 */
package com.supercharge.gateway.auth.service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.cbt.supercharge.constants.core.ApplicationConstants;
import com.cbt.supercharge.constants.core.TableConstants;
import com.cbt.supercharge.exception.core.ApplicationException;
import com.cbt.supercharge.exception.core.codes.ErrorCodes;
import com.cbt.supercharge.exception.core.codes.ErrorId;
import com.cbt.supercharge.transfer.objects.entity.SystemConfig;
import com.cbt.supercharge.transfer.objects.entity.UserAndInstitutionLinking;
import com.cbt.supercharge.transfer.objects.entity.UserPassword;
import com.cbt.supercharge.transfer.objects.entity.UserProfile;
import com.cbt.supercharge.transfter.objects.core.entity.vo.SystemConfigVo;
import com.cbt.supercharge.utils.core.ApplicationDateUtils;
import com.cbt.supercharge.utils.core.ApplicationUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.supercharge.gateway.common.base.dao.CommonUserDaoImpl;
import com.supercharge.gateway.common.handlers.CustomException;
import com.supercharge.gateway.common.handlers.UserIdHolder;
import com.supercharge.gateway.common.utils.JwtUtils;
import com.supercharge.gateway.models.AuthenticationResponse;
import com.supercharge.gateway.security.model.User;

/**
 * The Class AuthServiceImpl.
 */
@Service

public class AuthServiceImpl implements IAuthService {

	/**
	 * commonUserDaoImpl
	 */
	@Autowired
	private CommonUserDaoImpl commonUserDaoImpl;

	/**
	 * applicationDateUtils
	 */
	@Autowired
	private ApplicationDateUtils applicationDateUtils;

	/**
	 * The jwt utils.
	 */
	@Autowired
	private JwtUtils jwtUtils;

	/**
	 * The logger.
	 */
	private final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

	/**
	 * The secret key.
	 */
	@Value("${secret.key}")
	private String secretKey;

	@Value("${springbootwebfluxjjwt.jjwt.secret}")
	private String secret;
	
	/**
	 * cacheManager
	 */
	@Autowired
	private CacheManager cacheManager;
	
	@Autowired
	private com.supercharge.gateway.role.service.RoleServiceImpl roleServiceImpl;

	/**
	 * @param userName
	 * @param loggedInUser
	 * @param resp
	 * @throws ApplicationException
	 */
	private void buildAuthenticationResponse(User userName, UserProfile loggedInUser, AuthenticationResponse resp)
			throws ApplicationException {
		resp.setFirstName(loggedInUser.getUserName());
		resp.setLastName(loggedInUser.getLastName());
//		resp.setLogo(
//				iCompanyDao.getAllCompaniesByUser(loggedInUser.getIdentity()).stream().findFirst().get().getLogo());
		resp.setRefreshToken(jwtUtils.generateJwtRefreshToken(userName.getUsername()));
		resp.setAccessToken(jwtUtils.generateCustomRoleJwtToken(userName.getUsername(), resp.getInstitutionId(),
				ApplicationConstants.ROLE_SHORT_LIVING));
		resp.setTwoFaUrl(ApplicationConstants.ROLE_2FA_GET_URL);
		resp.setIsTwoFA(true);
	}

	/**
	 * Generate authentication token.
	 *
	 * @param user         the user
	 * @param loggedInUser the logged in user̥
	 * @return the authentication response
	 * @throws ApplicationException    the application exception
	 * @throws JsonProcessingException
	 */
	@Override
	public AuthenticationResponse generateAuthenticationToken(Principal user, UserProfile loggedInUser)
			throws ApplicationException, JsonProcessingException {
		User userName = null;
//		UserProfile loggedInUser = commonUserDaoImpl.getUserBySystemUserID(user.getUsername());
//		if (!ApplicationUtils.isValidateObject(loggedInUser)) {
//			throw new ApplicationException(ErrorCodes.INVALID_USER);
//		}
		if (user instanceof UsernamePasswordAuthenticationToken) {
			userName = (User) ((UsernamePasswordAuthenticationToken) user).getPrincipal();
		}
		List<UserAndInstitutionLinking> roles = commonUserDaoImpl.getUserAndInstitutionLinkingByUserId(loggedInUser);
		Integer invalidRolesCount = (int) roles.stream()
				.filter(role -> role.getRole().getIsActive().equals(Boolean.FALSE)
						|| role.getRole().getIsDeleted().equals(Boolean.TRUE))
				.count();
		if (invalidRolesCount.equals(roles.size())) {
			throw new CustomException(ErrorCodes.USER_INACTIVE.getErrorMessage());
		}
		List<SystemConfig> systemConfigs = commonUserDaoImpl.getSystemConfigDetails();
		AuthenticationResponse resp = new AuthenticationResponse();
		String logOutTime = null;
		Integer nDaysLogin = null;
		Integer expiryPeriod = null;
		Integer warningPeriod = null;
		Integer failureAttempts = null;
		for (SystemConfig systemConfig : systemConfigs) {
			if (systemConfig.getConfigName().equals(ApplicationConstants.LOGOUT_TIME)) {
				logOutTime = systemConfig.getConfigValue();
			}
			if (systemConfig.getConfigName().equals(ApplicationConstants.N_DAYS_LOGIN)) {
				nDaysLogin = Integer.parseInt(systemConfig.getConfigValue());
			}
			if (systemConfig.getConfigName().equals(ApplicationConstants.EXPIRY_PERIOD)) {
				expiryPeriod = Integer.parseInt(systemConfig.getConfigValue());
			}
			if (systemConfig.getConfigName().equals(ApplicationConstants.WARNING_PERIOD)) {
				warningPeriod = Integer.parseInt(systemConfig.getConfigValue());
			}
			if (systemConfig.getConfigName().equals(ApplicationConstants.FAILURE_ATTEMPTS)) {
				failureAttempts = Integer.parseInt(systemConfig.getConfigValue());
			}
		}
		if (loggedInUser.getLastDayLogin() != null && loggedInUser.getLogout() == false) {
			Date blockedDate = loggedInUser.getLastDayLogin();
			if (ApplicationUtils.isValidateObject(nDaysLogin)) {
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(blockedDate);
				calendar.add(Calendar.DAY_OF_YEAR, nDaysLogin);
				Date afterNDaysDate = calendar.getTime();
				if (new Date().after(afterNDaysDate)) {
					loggedInUser.setUserStatus(false);
					loggedInUser.setLogout(true);
					commonUserDaoImpl.updateUser(loggedInUser);
				}
			}
		}
		if (loggedInUser.getUserStatus() == false && loggedInUser.getLogout() == true) {
			String errMsg = ErrorCodes.USER_BLOCKED_MESSAGE.getErrorMessage().concat(String.valueOf(nDaysLogin))
					.concat(String.valueOf(ErrorCodes.USER_BLOCKED_MESSAGES.getErrorMessage()));
			throw new CustomException(errMsg);
		}

		SystemConfig systemConfig = systemConfigs.stream()
				.filter(a -> ApplicationConstants.ACCOUNT_LOCKOUT.equals(a.getPropertyGroup()) && a.getIsChecked())
				.findFirst().orElse(null);
		String autoRelease = ApplicationUtils.isValidateObject(systemConfig) ? systemConfig.getConfigValue() : null;
		if (ApplicationUtils.isValidateObject(systemConfig)) {
			if (ApplicationConstants.UN_BLOCKED_MINUTE.equals(systemConfig.getConfigName())) {
				if (Boolean.TRUE.equals(loggedInUser.getUserAccountStatus())
						&& loggedInUser.getFailedCount() > failureAttempts) {
					LocalDateTime blockedDate = loggedInUser.getUserBlockedDate();
					LocalDateTime autoReleaseMins = blockedDate
							.plusMinutes(Integer.parseInt(systemConfig.getConfigValue()));
					if (LocalDateTime.now().isAfter(autoReleaseMins)) {
						loggedInUser.setUserAccountStatus(false);
						loggedInUser.setFailedCount(ApplicationConstants.ZERO);
						logger.info("update user.................................");
						commonUserDaoImpl.updateUser(loggedInUser);
					} else {
						String errMsg = ErrorCodes.BLOCKED_ATTEMPTS.getErrorMessage()
								.replace(ApplicationConstants.N_VALUE, autoRelease);
						throw new CustomException(errMsg);
					}
				}
			} else if (ApplicationConstants.MANNUAL.equals(systemConfig.getConfigName())) {
				throw new CustomException(ErrorCodes.USER_BLOCKED.getErrorMessage());
			}
		}

		if (Boolean.TRUE.equals(loggedInUser.getUserAccountStatus())) {
			logger.error("user is blocked");
			throw new CustomException(ErrorCodes.USER_BLOCKED.getErrorMessage());
		}

		if (Boolean.FALSE.equals(loggedInUser.getUserStatus())) {
			logger.error("Your account is inactive");
			throw new CustomException(ErrorCodes.USER_ACC_INACTIVE.getErrorMessage());
		}
		loggedInUser.setFailedCount(ApplicationConstants.ZERO);
		commonUserDaoImpl.updateUser(loggedInUser);
		boolean isExpired = false;
		String isWarningUser = null;
		isExpired = validateUserName(loggedInUser, resp, isExpired, isWarningUser, expiryPeriod, warningPeriod,
				failureAttempts, autoRelease);
		if (isExpired == true) {
			throw new CustomException(ErrorCodes.EXPIRED_PASSWORD_ERRORMSG.getErrorMessage());
		}
		resp.setLogOutTime(logOutTime);
		resp.setFirstName(loggedInUser.getFirstName());
		resp.setUserId(loggedInUser.getUserId());
		resp.setUserName(loggedInUser.getUserName());
		resp.setLastName(loggedInUser.getLastName());
		resp.setIdentity(loggedInUser.getIdentity());
		resp.setCifMasking(loggedInUser.isCifMarkedSts());
		resp.setUserIdentificationNumber(loggedInUser.getUserIdentificationNumber());
		SystemConfig is2FAEnabled = systemConfigs.stream()
				.filter(sys -> (sys.getConfigName().equals(ApplicationConstants.TWO_FACTOR_STATUS))
						&& Boolean.TRUE.equals(Boolean.parseBoolean(sys.getConfigValue().toLowerCase())))
				.findFirst().orElse(null);
		if (!ApplicationUtils.isValidateObject(is2FAEnabled)) {
			resp.setIsTwoFA(false);
			resp.setAccessToken(jwtUtils.generateJwtToken(userName.getUsername(), resp.getInstitutionId()));
			resp.setRefreshToken(jwtUtils.generateJwtRefreshToken(userName.getUsername()));
			resp.setLastLoginDate(
					ApplicationUtils.isValidateObject(loggedInUser.getLastDayLogin()) ? loggedInUser.getLastDayLogin()
							: new Date());
			loggedInUser.setLastDayLogin(new Date());
			loggedInUser.setLogout(false);
			commonUserDaoImpl.updateUser(loggedInUser);
			return resp;
		}
		buildAuthenticationResponse(userName, loggedInUser, resp);
		resp.setLastLoginDate(loggedInUser.getLastDayLogin());
		resp.setLastLoginDate(
				ApplicationUtils.isValidateObject(loggedInUser.getLastDayLogin()) ? loggedInUser.getLastDayLogin()
						: new Date());
		loggedInUser.setLogout(false);
		commonUserDaoImpl.updateUser(loggedInUser);
		resp.setEmailId(loggedInUser.getEmailId());
		return resp;
	}

	/**
	 * @param passwordPolicy
	 * @param password
	 * @param today
	 * @throws ApplicationException
	 */
	private void getCreateBefore(UserPassword password, Date today, AuthenticationResponse resp, Integer expiryPeriod,
			Integer warningPeriod) throws CustomException {

		int createdBefore = applicationDateUtils.getDateDifference(today, password.getModifiedDate());

		if (createdBefore < ApplicationConstants.ZERO || createdBefore >= expiryPeriod) {
			throw new CustomException(ErrorCodes.PWD_EXPIRED_ERRORMSG);
		} else if (createdBefore < ApplicationConstants.ZERO || createdBefore >= warningPeriod) {
			int isWarning = expiryPeriod - createdBefore;
			String days = null;
			if (isWarning > 1) {
				days = " days";
			} else {
				days = " day";
			}
			String errMsg = ErrorCodes.PWD_WARNING_ERRORMSG.getErrorMessage().concat(String.valueOf(isWarning))
					.concat(days);
			resp.setIsWarningUser(errMsg);
		}
	}

	@Override
	/**
	 * Checks if is user authenticated.
	 *
	 * @param user          the user
	 * @param warningPeriod
	 * @param expiryPeriod
	 * @param autoRelease
	 * @return true, if is user authenticated
	 * @throws ApplicationException the application exception
	 */
	public boolean isUserAuthenticated(UserProfile user, AuthenticationResponse resp, Integer expiryPeriod,
			Integer warningPeriod, Integer failureAttemts, String autoRelease)
			throws ApplicationException, JsonProcessingException {
		// DO-IT: need to add OTP & generate token for e-mail
		// if two factor is disabled
		// if user is linked to specific institution̥
//		if (Boolean.TRUE.equals(user.getInstitutionLinkingFlag())) {
//			List<UserAndInstitutionLinking> userAndInstitutionLinking = userInstitutionLinkDaoImpl
//					.getUserAndInstitutionLinkingByUserId(user);
//			if (ApplicationUtils.isValidateObject(userAndInstitutionLinking)) {
//				logger.info("userAndInstitutionLinking--------------...................."+userAndInstitutionLinking.get(0).getInstitution().getIdentity());
//				resp.setInstitutionId(userAndInstitutionLinking.get(0).getInstitution().getIdentity());
//				PasswordPolicy passwordPolicy = iPasswordPolicyDaoImpl
//						.getPassWordPolicyById(userAndInstitutionLinking.get(0).getInstitution().getInstitutionId());
//				if (ApplicationUtils.isValidateObject(passwordPolicy)) {
		if (user.getFailedCount() > failureAttemts && Boolean.FALSE.equals(user.getUserAccountStatus())) {
			// call to block user if he exceeds failure attempts
			user.setUserAccountStatus(true);
			LocalDateTime currentDateTime = LocalDateTime.now();
			user.setUserBlockedDate(currentDateTime);
			commonUserDaoImpl.updateUser(user);
			String errorMessage = ErrorCodes.BLOCKED_ATTEMPTS.getErrorMessage();
			throw new UsernameNotFoundException(errorMessage.replace(ApplicationConstants.N_VALUE, autoRelease));
		}
		UserPassword password = commonUserDaoImpl.getPasswordByUserId(user.getUserId());

		Date today = applicationDateUtils.convertDateWithTimeZone(ApplicationDateUtils.UTC_TIMEZONE, new Date());
		getCreateBefore(password, today, resp, expiryPeriod, warningPeriod);
		return true;
//				}
//			}
//		}
//		return false;
	}

	@Override
	/**
	 * Refresh authentication token.
	 *
	 * @param refreshToken the refresh token
	 * @return the authentication response
	 * @throws ApplicationException
	 */
	public AuthenticationResponse refreshAuthenticationToken(String refreshToken)
			throws ApplicationException, JsonProcessingException {
		AuthenticationResponse resp = new AuthenticationResponse();
		UserProfile loggedInUser = commonUserDaoImpl.getUserByUserId(jwtUtils.getUserNameFromJwtToken(refreshToken));
		List<SystemConfig> systemConfigs = commonUserDaoImpl.getSystemConfigDetails();
		Integer expiryPeriod = null;
		Integer warningPeriod = null;
		Integer failureAttempts = null;
		for (SystemConfig systemConfig : systemConfigs) {
			if (systemConfig.getConfigName().equals(ApplicationConstants.EXPIRY_PERIOD)) {
				expiryPeriod = Integer.parseInt(systemConfig.getConfigValue());
			}
			if (systemConfig.getConfigName().equals(ApplicationConstants.WARNING_PERIOD)) {
				warningPeriod = Integer.parseInt(systemConfig.getConfigValue());
			}
			if (systemConfig.getConfigName().equals(ApplicationConstants.FAILURE_ATTEMPTS)) {
				failureAttempts = Integer.parseInt(systemConfig.getConfigValue());
			}
		}
		isUserAuthenticated(loggedInUser, resp, expiryPeriod, warningPeriod, failureAttempts, null);
		resp.setAccessToken(
				jwtUtils.generateJwtToken(loggedInUser.getUserIdentificationNumber(), resp.getInstitutionId()));
		resp.setApiDomain(null);
		return resp;
	}

	/**
	 * @param loggedInUser
	 * @param resp
	 * @param isExpired
	 * @param warningPeriod
	 * @param expiryPeriod
	 * @param autoRelease
	 * @throws JsonProcessingException
	 */
	private boolean validateUserName(UserProfile loggedInUser, AuthenticationResponse resp, boolean isExpired,
			String isWarningUser, Integer expiryPeriod, Integer warningPeriod, Integer failureAttempts,
			String autoRelease) throws JsonProcessingException {
		try {
			isUserAuthenticated(loggedInUser, resp, expiryPeriod, warningPeriod, failureAttempts, autoRelease);
		} catch (ApplicationException e) {
			String errMsg = null;
			errMsg = ValidateErrMsg(e);
			if (ErrorCodes.PWD_EXPIRED_ERRORMSG.getErrorMessage().equals(errMsg)) {
				isExpired = true;
			} else if (ApplicationConstants.PWD_WARNING_ERROR.equals(e.getMessage())) {

			} else {
				logger.error("internal error");
				throw new UsernameNotFoundException(TableConstants.USER_NOT_FOUND_IN_DATABASE);
			}
		}
		return isExpired;
	}

	private String ValidateErrMsg(ApplicationException e) {
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode rootNode = null;
		try {
			rootNode = objectMapper.readTree(e.getMessage());
		} catch (JsonMappingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (JsonProcessingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		JsonNode errorIdsNode = rootNode.get("errorIds");
		JsonNode firstErrorNode = errorIdsNode.get(0); // Assuming there's only one error message

		String errorMessage = firstErrorNode.get("errorMessage").asText();
		return errorMessage;
	}

	@Override
	public SystemConfigVo getConfigName(String configName) {
		SystemConfigVo wrapSystemConfig = null;
		if (ApplicationUtils.isValidString(configName)) {
			SystemConfig systemConfig = commonUserDaoImpl.getSystemConfigName(configName);
			wrapSystemConfig = wrapSystemConfig(systemConfig);
		}
		return wrapSystemConfig;
	}

	/**
	 * @param systemConfig
	 * @return
	 */
	private SystemConfigVo wrapSystemConfig(SystemConfig systemConfig) {
		SystemConfigVo systemConfigVo = new SystemConfigVo();
		systemConfigVo.setPropertyGroup(systemConfig.getPropertyGroup());
		systemConfigVo.setIsChecked(systemConfig.getIsChecked());
		systemConfigVo.setConfigValue(systemConfig.getConfigValue());
		systemConfigVo.setConfigName(systemConfig.getConfigName());
		return systemConfigVo;
	}
	
	@Override
	public String clearCache() {
		cacheManager.getCacheNames().forEach(cacheName -> {
			logger.info("Clearing cache: {}", cacheName);
			cacheManager.getCache(cacheName).clear();
		});
		return "Cache cleared successfully.";
	}


	/**
	 * @param commonUserDaoImpl the commonUserDaoImpl to set
	 */
	public void setCommonUserDaoImpl(CommonUserDaoImpl commonUserDaoImpl) {
		this.commonUserDaoImpl = commonUserDaoImpl;
	}

	/**
	 * @param applicationDateUtils the applicationDateUtils to set
	 */
	public void setApplicationDateUtils(ApplicationDateUtils applicationDateUtils) {
		this.applicationDateUtils = applicationDateUtils;
	}

	/**
	 * @param jwtUtils
	 */
	public void setJwtUtils(JwtUtils jwtUtils) {
		this.jwtUtils = jwtUtils;
	}

}