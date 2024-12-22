/*
 * @author codeboard
 */
package com.supercharge.gateway.user.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.cbt.supercharge.constants.core.ApplicationConstants;
import com.cbt.supercharge.constants.core.EmailConstants;
import com.cbt.supercharge.exception.core.codes.ErrorCodes;
import com.cbt.supercharge.exception.core.codes.ErrorId;
import com.cbt.supercharge.transfer.objects.entity.PasswordPolicy;
import com.cbt.supercharge.transfer.objects.entity.ResetKey;
import com.cbt.supercharge.transfer.objects.entity.SystemConfig;
import com.cbt.supercharge.transfer.objects.entity.UserAndInstitutionLinking;
import com.cbt.supercharge.transfer.objects.entity.UserPassword;
import com.cbt.supercharge.transfer.objects.entity.UserPasswordHistory;
import com.cbt.supercharge.transfer.objects.entity.UserProfile;
import com.cbt.supercharge.transfter.objects.core.dto.ChangePasswordDTO;
import com.cbt.supercharge.transfter.objects.core.dto.EmailSenderDto;
import com.cbt.supercharge.transfter.objects.core.dto.UserProfileDTO;
import com.cbt.supercharge.transfter.objects.core.entity.vo.RoleListDto;
import com.cbt.supercharge.transfter.objects.core.entity.vo.UserDto;
import com.cbt.supercharge.utils.core.ApplicationUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.supercharge.gateway.common.base.dao.CommonUserDaoImpl;
import com.supercharge.gateway.common.base.dao.GatewayEnvironmentProperties;
import com.supercharge.gateway.common.handlers.CustomException;
import com.supercharge.gateway.common.utils.GatewayCommonValidator;
import com.supercharge.gateway.propertyConfig.PropertyConfigServiceImpl;

/**
 * @author CBT
 */
@Service

public class UserServiceImpl implements IUserService {

	@Autowired
	private CommonUserDaoImpl commonUserDaoImpl;

	//    Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
	private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
	
	/**
	 * timeZone
	 */
	@Value("${time.zone}")
	private String timeZone;

	@Autowired
	private com.cbt.supercharge.crypto.core.OneWayEncryption oneWayEncryption;

	@Autowired
	private PropertyConfigServiceImpl propertyConfigServiceImpl;
	
	/**
	 * The Application date utils
	 */
	@Autowired
	private com.cbt.supercharge.utils.core.ApplicationDateUtils dateUtils;
	
	@Autowired
	private GatewayEnvironmentProperties environmentProperties;
	
	/**
	 * Dto wrapper.
	 *
	 * @param user the user
	 * @return the user DTO
	 * @throws CustomException
	 */
	private UserDto dtoWrapperForGetUsers(UserProfile userProfile) throws CustomException {
		UserDto userDto = new UserDto();
		userDto.setEmailId(userProfile.getEmailId());
		userDto.setFirstName(userProfile.getFirstName());
		userDto.setLastName(userProfile.getLastName());
		userDto.setMobileNumber(userProfile.getMobileNumber());
		List<UserAndInstitutionLinking> userRoleList = commonUserDaoImpl
				.getUserRoleById(userProfile);
		if (ApplicationUtils.isValidateObject(userRoleList)
				&& ApplicationUtils.isValidateObject(userRoleList.get(0).getRole())
				&& ApplicationUtils.isNotBlank(userRoleList.get(0).getRole().getUserRoleName())) {
			// possibility of adding linking multiple roles to single roles in future so we
			// are getting list and first index of the list for now
			
			List<RoleListDto>roleListDto=new ArrayList<RoleListDto>();
			for(UserAndInstitutionLinking institutionLinking :userRoleList) {
				RoleListDto listDto=new RoleListDto();
				listDto.setRoleName(institutionLinking.getRole().getUserRoleName());
				listDto.setRoleIdentity(institutionLinking.getRole().getIdentity());
				roleListDto.add(listDto);
			}
				
			userDto.setRoleList(roleListDto);
		}
		userDto.setUserAccountStatus(userProfile.getUserAccountStatus());
		userDto.setIdentity(userProfile.getIdentity());
		return userDto;
	}

	/**
	 * Gets the user by email.
	 *
	 * @param emailid the emailid
	 * @return the user by id
	 * @throws ApplicationException
	 */
	public UserProfile getUserByUserID(String userID) {
		UserProfile user = new UserProfile();
		if (ApplicationUtils.isNotBlank(userID)) {
			user = commonUserDaoImpl.getUserBySystemUserID(userID);
		}
		return user;
	}


	/**
	 * Update user.
	 *
	 * @param identity and userDto
	 * @param userDTO  the user DTO
	 * @throws CustomException
	 * @throws JsonProcessingException 
	 */
	public UserDto updateUserProfile(String identity, UserDto userDTO) throws CustomException, JsonProcessingException {
		commonUserDaoImpl.updateUser(updateProfileWrapper(identity, userDTO));
		UserProfile userProfile = commonUserDaoImpl.getUserByIdentity(identity);
		return dtoWrapperForGetUsers(userProfile);
	}

	/**
	 * @param identity
	 * @param institutionId
	 * @return UserDto
	 * @throws CustomException
	 */
	public UserDto getUserProfile(String identity) throws CustomException {
		UserProfile userProfile = commonUserDaoImpl.getUserByIdentity(identity);
		return updateProfileDtoWrapper(userProfile);
	}

	/**
	 * @param userProfile
	 * @return user dto
	 * @throws CustomException
	 */
	private UserDto updateProfileDtoWrapper(UserProfile userProfile) throws CustomException {
		UserDto userDto = new UserDto();
		userDto.setUserName(userProfile.getUserName());
		userDto.setEmailId(userProfile.getEmailId());
		userDto.setMobileNumber(userProfile.getMobileNumber());
		userDto.setFirstName(userProfile.getFirstName());
		userDto.setLastName(userProfile.getLastName());
		List<UserAndInstitutionLinking> userRoleList = commonUserDaoImpl
				.getUserRoleById(userProfile);
		if (ApplicationUtils.isValidateObject(userRoleList)
				&& ApplicationUtils.isNotBlank(userRoleList.get(0).getRole().getUserRoleName())) {
			// possibility of adding linking multiple roles to single roles in future so we
			// are getting list and first index of the list for now
			
			
			List<RoleListDto>roleListDto=new ArrayList<RoleListDto>();
			for(UserAndInstitutionLinking institutionLinking :userRoleList) {
				RoleListDto listDto=new RoleListDto();
				listDto.setRoleName(institutionLinking.getRole().getUserRoleName());
				listDto.setRoleIdentity(institutionLinking.getRole().getIdentity());
				roleListDto.add(listDto);
			}
				
			userDto.setRoleList(roleListDto);
			
			//old code
			//userDto.setRoleName(userRoleList.get(0).getRole().getUserRoleName());
		}
		userDto.setStatus(userProfile.getUserStatus());
		return userDto;
	}

	/**
	 * @param identity
	 * @param userDTO
	 * @return UserProfile
	 * @throws CustomException
	 * @throws JsonProcessingException 
	 */
	private UserProfile updateProfileWrapper(String identity, UserDto userDTO) throws CustomException, JsonProcessingException {
		UserProfile user = commonUserDaoImpl.getUserByIdentity(identity);
		user.setUserName(userDTO.getUserName());
		user.setMobileNumber(userDTO.getMobileNumber());
		user.setFirstName(userDTO.getFirstName());
		user.setLastName(userDTO.getLastName());
		commonUserDaoImpl.updateUser(user);
		return user;
	}

	
//	public ResponseEntity<byte[]> getUploadSummaryErrorLogListfinal(ErrorLogDto data,
//            HttpServletResponse response) {
//
//        int rowIndex = 0;
//
//
//		StringWriter sw = new StringWriter();
//        try (CSVWriter csvWriter = new CSVWriter(sw)) {
//        	LinkedHashMap<String, Object> headers = data.getMapValues().get(0);
//            csvWriter.writeNext(headers.keySet().toArray(new String[0]));
//            for (LinkedHashMap<String, Object> rowData : data.getMapValues()) {
//                csvWriter.writeNext(rowData.values().stream().map(Object::toString).toArray(String[]::new));
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            // Handle exceptions
//        }
//        byte[] csvBytes = sw.toString().getBytes();
//
//        // Set appropriate headers
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.parseMediaType("text/csv"));
//        headers.setContentDispositionFormData("filename", "data.csv");
//        headers.setContentLength(csvBytes.length);
//        return ResponseEntity.ok().headers(headers).body(csvBytes);
//		                     
//
//
//    }
	

			

	/**
	 * @param userProfile 
	 * @param password reset key, email
	 * @return boolean
	 */
	@Override
	public boolean sendForgotPasswordMail(String passwordResetKey, String email) throws CustomException {
		if (ApplicationUtils.isBlank(email)) {
			logger.error("Username or email is null");
			throw new CustomException(ErrorCodes.INVALID_USER);
		}
		UserProfile appUser = commonUserDaoImpl.getUserByName(email);
		if (Boolean.TRUE.equals(appUser.getUserAccountStatus())) {
			logger.error("user is blocked");
			throw new CustomException(ErrorCodes.USER_BLOCKED);
		}
		if (Boolean.FALSE.equals(appUser.getUserStatus())) {
			logger.error("Your account is inactive");
			throw new CustomException(ErrorCodes.USER_ACC_INACTIVE);
		}
		UserProfile existingUser = commonUserDaoImpl.getUserByEmailId(email);
		if (!ApplicationUtils.isValidateObject(existingUser)) {
			logger.error("User not found while send forgot password mail");
			throw new CustomException(ErrorCodes.EXISTING_USER);
		}
		sendPasswordResetLink(passwordResetKey, email, appUser);
		return true;
	}

	/**
	 * @param passwordResetKey
	 * @return reset password url
	 */
	private String generateResetPasswordUrl(String passwordResetKey) {
		String resetPasswordUrl = environmentProperties.getForgetPasswordUrl();
		if (ApplicationUtils.isNotBlank(resetPasswordUrl)) {
			resetPasswordUrl = resetPasswordUrl.concat(ApplicationConstants.SLASH).concat(passwordResetKey);
		}
		return resetPasswordUrl;
	}

	/**
	 * @param changePasswordDTO
	 * @param userProfile 
	 * @param userProfile 
	 * @param userProfile 
	 * @return boolean
	 * @throws CustomException
	 * @throws JsonProcessingException 
	 */
	@Override
	public boolean getChangePassword(ChangePasswordDTO changePasswordDTO
			) throws CustomException, JsonProcessingException {
				ResetKey resetKey = commonUserDaoImpl.getResetKeyByKey(changePasswordDTO.getResetPasswordKey());
				List<SystemConfig> systemConfigs = commonUserDaoImpl
						.getSystemConfigDetail(ApplicationConstants.PASSWORD_CHANGE_FREQUENCY);
				Integer existingHistory = 0;
				Integer lastNDays = 0;
				Long resetTime = null;
				SystemConfig systemCon = null;
				Integer passwordPolicyCount = null;
				for (SystemConfig systemConfig : systemConfigs) {
					if (systemConfig.getConfigName().equals(ApplicationConstants.PREVENT_REUSE_N_CHANGES)
							&& Boolean.TRUE.equals(systemConfig.getIsChecked())) {
						existingHistory = Integer.parseInt(systemConfig.getConfigValue());
					}
					if (systemConfig.getConfigName().equals(ApplicationConstants.PREVENT_REUSE_N_DAYS)
							&& Boolean.TRUE.equals(systemConfig.getIsChecked())) {
						lastNDays = Integer.parseInt(systemConfig.getConfigValue());
					}
					if (systemConfig.getConfigName().equals(ApplicationConstants.PASSWORD_CHANGE_FREQUENCY)) {
						resetTime = Long.parseLong(systemConfig.getConfigValue());
					}
					if (systemConfig.getConfigName().equals(ApplicationConstants.SATISFY_PASSWORD_POLICY)) {
						passwordPolicyCount = Integer.parseInt(systemConfig.getConfigValue());
					}

				}
			
			Optional<SystemConfig> findFirst = systemConfigs.stream()
					.filter(a -> a.getConfigName().equals(ApplicationConstants.PREVENT_REUSE)).findFirst();
			if (findFirst.isPresent()) {
				systemCon = findFirst.get();
			}

			Integer userId = resetKey.getUserId().getUserId();
			UserPassword passwordByUserId = commonUserDaoImpl.getPasswordByUserId(userId);
			ValidatePasswordPolicy(changePasswordDTO, userId, existingHistory, lastNDays, systemCon);

		
		// This is for reset password time interval
		
		Boolean forceReset = false;
		long minutesDifference = 0L;
		long Resetmilliseconds = 0L;
		long ResetMinutes = 0L;
		long ResetHour = 0L;
		long ResetDay = 0L;
		long ResetTime = 0L;
		String msgTime = null;
		if (ApplicationUtils.isValidateObject(passwordByUserId)) {
			Date modifiedDate = passwordByUserId.getModifiedDate();
			LocalDateTime resetKey1 = modifiedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
			LocalDateTime currentDateTime = LocalDateTime.now();
			minutesDifference = ChronoUnit.MINUTES.between(resetKey1, currentDateTime);
			Resetmilliseconds = TimeUnit.MINUTES.toMillis(minutesDifference);
			ResetMinutes = TimeUnit.MILLISECONDS.toMinutes(resetTime);
			ResetTime = ResetMinutes;
			if (ResetMinutes == 1) {
				msgTime = " minute";
			} else {
				msgTime = " minutes";
			}
			if (ResetMinutes >= 60) {
				ResetHour = TimeUnit.MINUTES.toHours(ResetMinutes);
				ResetTime = ResetHour;
				if (ResetHour == 1) {
					msgTime = " hour";
				} else {
					msgTime = " hours";
				}
			}
			if (ResetHour >= 24) {
				ResetDay = TimeUnit.HOURS.toDays(ResetHour);
				ResetTime = ResetDay;
				if (ResetDay == 1) {
					msgTime = " day";
				} else {
					msgTime = " days";
				}
			}
			forceReset = true;
		}
		
		//this for time interval part otherwise normal part
		if(Boolean.TRUE.equals(forceReset)) {
			if (Resetmilliseconds > resetTime || resetTime == 0) {
				savePasswordAndKey(changePasswordDTO, resetKey, systemCon.getPasswordPolicyOn(),
						systemCon.getSelectedPasswordPolicy(), passwordPolicyCount, lastNDays, systemConfigs);
			} else {
				String errMsg = ErrorCodes.RESET_PASSWORD_TIME.getErrorMessage().concat(String.valueOf(ResetTime))
						.concat(msgTime)
						.concat(String.valueOf(ErrorCodes.RESET_PASSWORD_TIME_GAP.getErrorMessage()));

				String jsonError = "{\"errorIds\":[{\"errorCode\":\"" + "E0068" + "\",\"errorMessage\":\"" + errMsg
						+ "\",\"hints\":[],\"severity\":\"ERROR\",\"hintType\":\"TEXT\"}]}";
				throw new CustomException(jsonError);	
			}
			
		} else {
			savePasswordAndKey(changePasswordDTO, resetKey, systemCon.getPasswordPolicyOn(),
					systemCon.getSelectedPasswordPolicy(), passwordPolicyCount, lastNDays, systemConfigs);
		}
		return true;



	}

	private void ValidatePasswordPolicy(ChangePasswordDTO changePasswordDTO, Integer userID, Integer existingHistory,
			Integer lastNDays, SystemConfig systemCon) throws CustomException {
		if (Boolean.TRUE.equals(systemCon.getIsChecked())) {
			if (ApplicationUtils.isValidId(existingHistory)) {
				BCryptPasswordEncoder bcryptEncoder = new BCryptPasswordEncoder();
				List<UserPasswordHistory> passwordDetails = commonUserDaoImpl
						.getlastFivePassword(userID, existingHistory);
				if (ApplicationUtils.isValidateObject(passwordDetails)) {
					for (UserPasswordHistory details : passwordDetails) {
	 					if (bcryptEncoder.matches(changePasswordDTO.getNewPassword(), details.getUmUsrPwd())) {
	 						String errMsg = nValueReplace(ErrorCodes.PASSWORD_ERR_MSG.getErrorMessage(), existingHistory);

	 						String jsonError = ApplicationConstants.ERROR_DESIGN + errMsg
									+ ApplicationConstants.ERROR_DESIGN1;
							throw new CustomException(jsonError);
						}
					}
				}

			} else if (ApplicationUtils.isValidId(lastNDays)) {
				Date currentDate = new Date();
				// Create a calendar instance
				Calendar calendar = Calendar.getInstance();
				// Set the current date to the calendar instance
				calendar.setTime(currentDate);
				// Subtract dynamic number of days (e.g., 10 days)
				calendar.add(Calendar.DAY_OF_MONTH, -lastNDays);
				// Get the date after subtraction
				Date tenDaysAgo = calendar.getTime();
				
//				Date tenDaysAgo = new Date(System.currentTimeMillis() - lastNDays);
//				long Days = TimeUnit.MILLISECONDS.toDays(lastNDays);
				BCryptPasswordEncoder bcryptEncoder = new BCryptPasswordEncoder();
				List<UserPasswordHistory> passwordDetails = commonUserDaoImpl
						.getPasswordWithinDays(userID, currentDate, tenDaysAgo);
				if (ApplicationUtils.isValidateObject(passwordDetails)) {
					for (UserPasswordHistory details : passwordDetails) {
						if (bcryptEncoder.matches(changePasswordDTO.getNewPassword(), details.getUmUsrPwd())) {
							String errMsg = nValueReplace(ErrorCodes.PASSWORD_ERR_MSG1.getErrorMessage(),lastNDays);

							String jsonError = ApplicationConstants.ERROR_DESIGN + errMsg
									+ ApplicationConstants.ERROR_DESIGN1;
							throw new CustomException(jsonError);	
						}
					}
				}
			}
		}
	}

	/**
	 * @param lastNDays
	 * @return
	 */
	private String nValueReplace(String value, Integer lastNDays) {
		return value.replace(ApplicationConstants.N_VALUE, String.valueOf(lastNDays));
	}

	/**
	 * @param changePasswordDTO
	 * @param resetKey 
	 * @param lastNDays 
	 * @param systemConfigs 
	 * @param checkOption 
	 * @throws CustomException
	 * @throws JsonProcessingException 
	 */
	private void savePasswordAndKey(ChangePasswordDTO changePasswordDTO, ResetKey resetKey, boolean passwordPolicyOn,
			boolean selectedPasswordPolicy, Integer passwordPolicyCount, Integer lastNDays, List<SystemConfig> systemConfigs)
			throws CustomException, JsonProcessingException {
		if (!ApplicationUtils.isValidateObject(resetKey) || !ApplicationUtils.isValidateObject(resetKey.getUserId())) {
			throw new CustomException(ErrorCodes.INVALID_KEY);
		}
		savePassword(resetKey.getUserId(), changePasswordDTO, passwordPolicyOn, selectedPasswordPolicy,
				passwordPolicyCount, lastNDays, systemConfigs);
//		resetKey.setIsDeleted(true);
		Integer expireTime = environmentProperties.getExpiryTime();
		resetKey.setExpiryDate(dateUtils.getResetExpiryTime(expireTime));
		resetKey.setModifiedDate(new Date());
		commonUserDaoImpl.updateKey(resetKey);
		
	}
	
	

	/**
	 * @param user
	 * @param lastNDays 
	 * @param systemConfigs 
	 * @param changePasswordDTO̥̥
	 * @param checkOption 
	 * @throws CustomException
	 * @throws JsonProcessingException 
	 */
	private void savePassword(UserProfile user, ChangePasswordDTO changePasswordDTO, boolean passwordPolicyOn,
			boolean selectedPasswordPolicy, Integer passwordPolicyCount, Integer lastNDays, List<SystemConfig> systemConfigs)
			throws CustomException, JsonProcessingException {

		PasswordPolicy passwordPolicy = null;
		Boolean isNewPwd = null;
		if (Boolean.TRUE.equals(user.getInstitutionLinkingFlag())) {
			logger.info("User Id................................"+ user.getUserId());
			List<UserAndInstitutionLinking> userAndInstitutionLinking = commonUserDaoImpl
					.getUserAndInstitutionLinkingByUserId(user);
			logger.info("Institution Id................................"+ userAndInstitutionLinking.get(0).getInstitution().getInstitutionId());
			if (ApplicationUtils.isValidateObject(userAndInstitutionLinking)
					&& ApplicationUtils.isValidateObject(userAndInstitutionLinking.get(0).getInstitution())) {
				passwordPolicy = commonUserDaoImpl
						.getPassWordPolicyById(userAndInstitutionLinking.get(0).getInstitution().getInstitutionId());
			}
		}

		if (!ApplicationUtils.isValidateObject(passwordPolicy)) {
			throw new CustomException(ErrorCodes.INVALID_PASSWORD_POLICY);
		}
		logger.info("Institution Id................................"+passwordPolicy.getPasswordPolicyId());
		UserPassword existingPassword = commonUserDaoImpl.getPasswordByUserId(user.getUserId());
		if (existingPassword != null) {	
			if (ApplicationUtils.isBcryptPasswordMatched(changePasswordDTO.getNewPassword(),
					existingPassword.getUmUsrPwd()) && ApplicationConstants.ONE >= lastNDays) {
				throw new CustomException(ErrorCodes.HISTORY_MATCHED);
			}
			validatePassword(changePasswordDTO, user, existingPassword, isNewPwd, passwordPolicyCount, systemConfigs);
		if(Boolean.TRUE.equals(selectedPasswordPolicy)) {	
			UserPasswordHistory historyForResetPassword = new UserPasswordHistory();
			historyForResetPassword.setUserProfile(user);
			if (ApplicationUtils.isValidateObject(existingPassword)) {
				historyForResetPassword.setUmMdyDte(existingPassword.getModifiedDate());
				historyForResetPassword.setUmUsrPwd(oneWayEncryption.convertToDatabaseColumn(changePasswordDTO.getNewPassword()));
				commonUserDaoImpl.savePasswordHistory(historyForResetPassword);
			}
		}else {
			UserPasswordHistory history = buildPasswordHistory(user, existingPassword);
			logger.info("history................................"+history.getUmUsrPwd());
			commonUserDaoImpl.savePasswordHistory(history);
		}
			existingPassword.setUmUsrPwd(oneWayEncryption.convertToDatabaseColumn(changePasswordDTO.getNewPassword()));
			existingPassword.setModifiedDate(new Date());
			logger.info("existingPassword................................"+existingPassword.getUmPwdId());
			commonUserDaoImpl.updatePassword(existingPassword);
		} else {
			isNewPwd = true;
			validatePassword(changePasswordDTO, user, existingPassword, isNewPwd, passwordPolicyCount, systemConfigs);
			UserPassword userPwd = new UserPassword();
			userPwd.setUserProfile(user);
			userPwd.setUmPwdFlg(ApplicationConstants.ONE); /* Active */
			userPwd.setUmUsrPwd(oneWayEncryption.convertToDatabaseColumn(changePasswordDTO.getNewPassword()));
			userPwd.setModifiedDate(new Date());
			logger.info("userPwd................................"+userPwd.getUmPwdId());
			commonUserDaoImpl.savePassword(userPwd);
			
			UserPasswordHistory historyForResetPassword = new UserPasswordHistory();
			historyForResetPassword.setUserProfile(user);
			historyForResetPassword.setUmMdyDte(new Date());
			historyForResetPassword.setUmUsrPwd(oneWayEncryption.convertToDatabaseColumn(changePasswordDTO.getNewPassword()));
			commonUserDaoImpl.savePasswordHistory(historyForResetPassword);
		}
	}

	/**
	 * @param isNewPwd 
	 * @param systemConfigs 
	 * @param changepassword dto, user,existing password
	 */
	@Override
	public void validatePassword(ChangePasswordDTO changePasswordDTO, UserProfile user, UserPassword existingPassword, Boolean isNewPwd,Integer passwordPolicyCount, List<SystemConfig> systemConfigs)
			throws CustomException {
		List<SystemConfig> systemConfigList = systemConfigs.stream()
				.filter(a -> ApplicationUtils.isValidString(a.getPropertyGroup())
						&& a.getPropertyGroup().equals(changePasswordDTO.getPropertyGroup()))
				.collect(Collectors.toList());
//		List<propertyConfigValueEntity> propertyConfigValues = propertyConfigDao.getPropertyConfigValues();

		if (!changePasswordDTO.getNewPassword().contentEquals(changePasswordDTO.getConfirmPassword())) {
			throw new CustomException(ErrorCodes.PASSWORD_NOT_MATCHED);
		}

		if (!ApplicationUtils.isValidateObject(existingPassword) && !ApplicationUtils.isValidateObject(isNewPwd)) {
			return;
		}

		if(ApplicationUtils.isValidateObject(existingPassword)) {
//			For change password and force update password, In Reset password flow, it will be null
			if (ApplicationUtils.isNotBlank(changePasswordDTO.getOldPassword()) && !ApplicationUtils
					.isBcryptPasswordMatched(changePasswordDTO.getOldPassword(), existingPassword.getUmUsrPwd())) {
				throw new CustomException(ErrorCodes.OLD_PASSWORD_NOT_MATCHED);
			}
		}
//			List<UserPasswordHistory> passHistories = userPasswordHistoryDaoImpl.getHistoryWithResetLimit(user.getUserId(),
//					ApplicationConstants.PASSWORD_HISTORY_LIMIT);
//			if (ApplicationUtils.isValidList(passHistories)) {
//				UserPasswordHistory history = passHistories.stream().filter(
//						a -> ApplicationUtils.isBcryptPasswordMatched(changePasswordDTO.getNewPassword(), a.getUmUsrPwd()))
//						.findFirst().orElse(null);
//				if (ApplicationUtils.isValidateObject(history)) {
//					throw new CustomException(ErrorCodes.HISTORY_MATCHED);
//				}

//		}

			validNewPassword(changePasswordDTO, user, systemConfigList,passwordPolicyCount);

//		}
	}

	private void validNewPassword(ChangePasswordDTO changePasswordDTO, UserProfile user,
			List<SystemConfig> systemConfigList,Integer passwordPolicyCount) throws CustomException {
		String newPassword = changePasswordDTO.getNewPassword();
		String userName = user.getUserName();
		Integer countAlphabeticCharacters = propertyConfigServiceImpl.countAlphabeticCharacters(newPassword);
		Integer countLowercaseLetters = propertyConfigServiceImpl.countLowercaseLetters(newPassword);
		Integer countNumericCharacters = propertyConfigServiceImpl.countNumericCharacters(newPassword);
		Integer countRepeatedCharacters = propertyConfigServiceImpl.findCountOfHighestConsecutiveCharacter(newPassword);
		
		Integer countSpecialCharacters = propertyConfigServiceImpl.countSpecialCharacters(newPassword);
		Integer countUppercaseLetters = propertyConfigServiceImpl.countUppercaseLetters(newPassword);
		Integer characterLength = propertyConfigServiceImpl.getCharacterLength(newPassword);
		Integer validatePassword = propertyConfigServiceImpl.validateEmbeddedUserName(userName, newPassword);
		Integer successCount = 0;
		List<SystemConfig> systemConfigEntityList = new ArrayList<>();
		for (SystemConfig config : systemConfigList) {
			String property = config.getConfigName();
			
			if (ApplicationConstants.MINIMUM_CHARACTERS.equals(property) && Boolean.TRUE.equals(config.getIsChecked())) {
				if ((characterLength < Integer.parseInt(config.getConfigValue()))) {
					systemConfigEntityList.add(config);
				} else {
					successCount = successCount + 1;
				}
			}
			if (ApplicationConstants.MAXIMUM_CHARACTERS.equals(property) && Boolean.TRUE.equals(config.getIsChecked())) {
				if((characterLength > Integer.parseInt(config.getConfigValue()))) {
					systemConfigEntityList.add(config);
				}else {
					successCount = successCount + 1;
				}
			}	
		}
		
		for (SystemConfig config : systemConfigList) {
			String property = config.getConfigName();
			if (ApplicationConstants.UPPERCASE_LETTERS.equals(property) && Boolean.TRUE.equals(config.getIsChecked())) {
				if((countUppercaseLetters < Integer.parseInt(config.getConfigValue()))) {
					systemConfigEntityList.add(config);
				}else {
					successCount = successCount + 1;
				}				
			}
			 if (ApplicationConstants.ALPHABETIC_CHARACTERS.equals(property) && Boolean.TRUE.equals(config.getIsChecked())) {
				if((countAlphabeticCharacters < Integer.parseInt(config.getConfigValue()))) {
					systemConfigEntityList.add(config);
				}else {
					successCount = successCount + 1;
				}
			}
			 if (ApplicationConstants.LOWERCASE_CHARACTERS.equals(property) && Boolean.TRUE.equals(config.getIsChecked())) {
				if((countLowercaseLetters < Integer.parseInt(config.getConfigValue()))) {
					systemConfigEntityList.add(config);
				}else {
					successCount = successCount + 1;
				}	
			}
			 if (ApplicationConstants.NUMERIC_CHARACTERS.equals(property) && Boolean.TRUE.equals(config.getIsChecked())) {
				if((countNumericCharacters < Integer.parseInt(config.getConfigValue()))) {
					systemConfigEntityList.add(config);
				}else {
					successCount = successCount + 1;
				}		
			}
			 if (ApplicationConstants.REPEATED_CHARACTERS.equals(property) && Boolean.TRUE.equals(config.getIsChecked())) {
				if((countRepeatedCharacters > Integer.parseInt(config.getConfigValue()))) {
					systemConfigEntityList.add(config);
				}else {
					successCount = successCount + 1;
				}	
			}
			 if (ApplicationConstants.SPECIAL_CHARACTERS.equals(property) && Boolean.TRUE.equals(config.getIsChecked())) {
				if((countSpecialCharacters < Integer.parseInt(config.getConfigValue()))) {
					systemConfigEntityList.add(config);
				}else {
					successCount = successCount + 1;
				}	
			}
			if (ApplicationConstants.EMBEDDED_USERNAME.equals(property) && Boolean.TRUE.equals(config.getIsChecked())) {
				if((validatePassword > Integer.parseInt(config.getConfigValue()))) {
					systemConfigEntityList.add(config);
				}else {
					successCount = successCount + 1;
				}
			}
		}
		validateSatifyPassword(systemConfigList, passwordPolicyCount, successCount, systemConfigEntityList);
	}

	private void validateSatifyPassword(List<SystemConfig> systemConfigList,
			Integer passwordPolicyCount, Integer successCount, List<SystemConfig> systemConfigEntityList)
			throws CustomException {
		if(passwordPolicyCount > systemConfigList.size()) {
			passwordPolicyCount = systemConfigList.size();
		}
		if(passwordPolicyCount != 0) {	
			if(successCount < passwordPolicyCount) {
				for (SystemConfig config : systemConfigEntityList) {
					String property = config.getConfigName();
					
					if (ApplicationConstants.MINIMUM_CHARACTERS.equals(property)) {
						throw new CustomException(
								new ErrorId(ErrorCodes.ALTEAST_N_MIN_CHAR_REQUIRED.getErrorCode(),
										ErrorCodes.ALTEAST_N_MIN_CHAR_REQUIRED.getErrorMessage()
												.replace(ApplicationConstants.N_VALUE, config.getConfigValue())));
					}
					else if (ApplicationConstants.MAXIMUM_CHARACTERS.equals(property)) {
						
						throw new CustomException(
								new ErrorId(ErrorCodes.ALTEAST_N_MAX_CHAR_REQUIRED.getErrorCode(),
										ErrorCodes.ALTEAST_N_MAX_CHAR_REQUIRED.getErrorMessage()
												.replace(ApplicationConstants.N_VALUE, config.getConfigValue())));
					}
					
					else if (ApplicationConstants.UPPERCASE_LETTERS.equals(property)) {
						
						throw new CustomException(
								new ErrorId(ErrorCodes.ALTEAST_N_UPPER_LETTER_REQUIRED.getErrorCode(),
										ErrorCodes.ALTEAST_N_UPPER_LETTER_REQUIRED.getErrorMessage()
												.replace(ApplicationConstants.N_VALUE, config.getConfigValue())));
					}
					
					else if (ApplicationConstants.ALPHABETIC_CHARACTERS.equals(property)) {

						throw new CustomException(
								new ErrorId(ErrorCodes.ALTEAST_N_ALPHA_CHARS_REQUIRED.getErrorCode(),
										ErrorCodes.ALTEAST_N_ALPHA_CHARS_REQUIRED.getErrorMessage()
												.replace(ApplicationConstants.N_VALUE, config.getConfigValue())));
					}
					
					else if (ApplicationConstants.LOWERCASE_CHARACTERS.equals(property)) {
						
						throw new CustomException(
								new ErrorId(ErrorCodes.ALTEAST_N_LOWER_LETTER_REQUIRED.getErrorCode(),
										ErrorCodes.ALTEAST_N_LOWER_LETTER_REQUIRED.getErrorMessage()
												.replace(ApplicationConstants.N_VALUE, config.getConfigValue())));
					}
					
					else if (ApplicationConstants.NUMERIC_CHARACTERS.equals(property)) {

						throw new CustomException(
								new ErrorId(ErrorCodes.ALTEAST_N_NUM_CHARS_REQUIRED.getErrorCode(),
										ErrorCodes.ALTEAST_N_NUM_CHARS_REQUIRED.getErrorMessage()
												.replace(ApplicationConstants.N_VALUE, config.getConfigValue())));
					}

					else if (ApplicationConstants.REPEATED_CHARACTERS.equals(property)) {

						throw new CustomException(
								new ErrorId(ErrorCodes.ALTEAST_N_REP_CHARS_REQUIRED.getErrorCode(),
										ErrorCodes.ALTEAST_N_REP_CHARS_REQUIRED.getErrorMessage()
												.replace(ApplicationConstants.N_VALUE, config.getConfigValue())));
					}
					
					else if (ApplicationConstants.SPECIAL_CHARACTERS.equals(property)) {

						throw new CustomException(
								new ErrorId(ErrorCodes.ALTEAST_N_CHARS_REQUIRED.getErrorCode(),
										ErrorCodes.ALTEAST_N_CHARS_REQUIRED.getErrorMessage()
												.replace(ApplicationConstants.N_VALUE, config.getConfigValue())));
					}
					else if (ApplicationConstants.EMBEDDED_USERNAME.equals(property)) {
						
						throw new CustomException(
								new ErrorId(ErrorCodes.EMBEDDED_USERNAME.getErrorCode(), ErrorCodes.EMBEDDED_USERNAME
										.getErrorMessage().concat(String.valueOf(config.getConfigValue()))));
					}
					
				}
			}
		}
	}

	/**
	 * @param user
	 * @param existingPassword
	 * @return password history
	 */
	private UserPasswordHistory buildPasswordHistory(UserProfile user, UserPassword existingPassword) {
		UserPasswordHistory history = new UserPasswordHistory();
		history.setUserProfile(user);
		if (ApplicationUtils.isValidateObject(existingPassword)) {
			history.setUmMdyDte(existingPassword.getModifiedDate());
			history.setUmUsrPwd(existingPassword.getUmUsrPwd());
		}
		return history;
	}

	/**
	 * @param changePasswordDTO
	 * @param user
	 * @throws CustomException
	 * @throws JsonProcessingException 
	 */
	@Override
	public boolean updateUserPassword(ChangePasswordDTO changePasswordDTO, UserProfile user)
			throws CustomException, JsonProcessingException { 
		if (!ApplicationUtils.isValidateObject(user)) {
			throw new CustomException(ErrorCodes.USER_NOT_FOUND);
		}
		Long resetTime = null;
		Integer existingHistory = 0;
		Integer lastNDays = 0;
		SystemConfig systemCon = null;
		Integer passwordPolicyCount = null;
		List<SystemConfig> systemConfigs = commonUserDaoImpl
				.getSystemConfigDetail(ApplicationConstants.PASSWORD_CHANGE_FREQUENCY);
		for (SystemConfig systemConfig : systemConfigs) {
			if (systemConfig.getConfigName().equals(ApplicationConstants.PASSWORD_CHANGE_FREQUENCY)) {
				resetTime = Long.parseLong(systemConfig.getConfigValue());
			}
			if (systemConfig.getConfigName().equals(ApplicationConstants.PREVENT_REUSE_N_CHANGES)
					&& Boolean.TRUE.equals(systemConfig.getIsChecked())) {
				existingHistory = Integer.parseInt(systemConfig.getConfigValue());
			}
			if (systemConfig.getConfigName().equals(ApplicationConstants.PREVENT_REUSE_N_DAYS)
					&& Boolean.TRUE.equals(systemConfig.getIsChecked())) {
				lastNDays = Integer.parseInt(systemConfig.getConfigValue());
			}
			if (systemConfig.getConfigName().equals(ApplicationConstants.SATISFY_PASSWORD_POLICY)) {
				passwordPolicyCount = Integer.parseInt(systemConfig.getConfigValue());
			}
		}
		
		Optional<SystemConfig> findFirst = systemConfigs.stream()
				.filter(a -> a.getConfigName().equals(ApplicationConstants.PREVENT_REUSE)).findFirst();
		if (findFirst.isPresent()) {
			systemCon = findFirst.get();
		}
		
		Integer userId = user.getUserId();
		ValidatePasswordPolicy(changePasswordDTO, userId, existingHistory, lastNDays, systemCon); 
		validateResetPasswordTime(changePasswordDTO, user, resetTime, passwordPolicyCount, lastNDays, systemConfigs);
		
	    Map<String, Object> message = new HashMap<>();
	    message.put("identity", user.getIdentity()); 
	    // websocket
//	    messagingTemplate.convertAndSend("/topic/password", message);
		return true;
	}
	
	private void validateResetPasswordTime(ChangePasswordDTO changePasswordDTO, UserProfile user, Long resetTime,
			Integer passwordPolicyCount, Integer lastNDays, List<SystemConfig> systemConfigs) throws CustomException, JsonProcessingException {
		long minutesDifference = 0L;
		long Resetmilliseconds = 0L;
		long ResetMinutes = 0L;
		long ResetHour = 0L;
		long ResetDay = 0L;
		long ResetTime = 0L;
		long resetInterval = 0L;
		String msgTime = null;
		
		UserPassword passwordByUserId = commonUserDaoImpl.getPasswordByUserId(user.getUserId());

		if (ApplicationUtils.isValidateObject(passwordByUserId)) {
			Date modifiedDate = passwordByUserId.getModifiedDate();
			LocalDateTime resetKey1 = modifiedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
			LocalDateTime currentDateTime = LocalDateTime.now();
			resetInterval = ChronoUnit.HOURS.between(resetKey1, currentDateTime);  // time diff between last reseted password
			minutesDifference = ChronoUnit.MINUTES.between(resetKey1, currentDateTime);
			Resetmilliseconds = TimeUnit.MINUTES.toMillis(minutesDifference);
			ResetMinutes = TimeUnit.MILLISECONDS.toMinutes(resetTime);
			ResetTime = ResetMinutes;
			if (ResetMinutes == 1) {
				msgTime = " minute";
			} else {
				msgTime = " minutes";
			}
			if (ResetMinutes >= 60) {
				ResetHour = TimeUnit.MINUTES.toHours(ResetMinutes);
				ResetTime = ResetHour;
				if (ResetHour == 1) {
					msgTime = " hour";
				} else {
					msgTime = " hours";
				}
			}
			if (ResetHour >= 24) {
				ResetDay = TimeUnit.HOURS.toDays(ResetHour);
				ResetTime = ResetDay;
				if (ResetDay == 1) {
					msgTime = " day";
				} else {
					msgTime = " days";
				}
			}
		}
		if (resetInterval >= resetTime) {
			savePassword(user, changePasswordDTO, false, false, passwordPolicyCount, lastNDays, systemConfigs);	
		} else {
			String errMsg = ErrorCodes.RESET_PASSWORD_TIME.getErrorMessage().concat(String.valueOf(resetTime))
					.concat(String.valueOf(ErrorCodes.RESET_PASSWORD_TIME_GAP.getErrorMessage()));

			String jsonError = "{\"errorIds\":[{\"errorCode\":\"" + "E0068" + "\",\"errorMessage\":\"" + errMsg
					+ "\",\"hints\":[],\"severity\":\"ERROR\",\"hintType\":\"TEXT\"}]}";
			throw new CustomException(jsonError);	
		}
	}

	/**
	 * @param userID
	 * @return mapped institution
	 */
	@Override
	public List<String> getMappedInstitutionsBySystemUserID(String userID) throws CustomException {
		List<String> userMappedInstitutions = new ArrayList<>();
		UserProfile user = getUserByUserID(userID);
		if (ApplicationUtils.isValidateObject(user)) {
			userMappedInstitutions = commonUserDaoImpl.getUserMappedInstitutionIds(user.getUserId());
		}
		return userMappedInstitutions;
	}

	/**
	 * @param userId
	 * @return user institution linking
	 */
	@Override
	public String getInstituteIdentityFromBaseToken(String userId) throws CustomException {
//		if (ApplicationUtils.isNotBlank(userId)) {
//			UserProfile user = userDaoWithoutFilterImpl.getUserByUserId(userId);
//			if (ApplicationUtils.isValidateObject(user) && Boolean.TRUE.equals(user.getInstitutionLinkingFlag())) {
//				List<UserAndInstitutionLinking> userAndInstitutionLinking = null;
//				try {
//					userAndInstitutionLinking = iUserInstitutionLinkDaoImpl
//							.getUserAndInstitutionLinkingByUserId(user.getUserId());
//					if (ApplicationUtils.isValidateObject(userAndInstitutionLinking)
//							&& ApplicationUtils.isValidateObject(userAndInstitutionLinking.get(0).getInstitution())) {
//						return userAndInstitutionLinking.get(0).getInstitution().getIdentity();
//					}
//				} catch (CustomException e) {
//					logger.error("No Institution is mapped to the logged user");
//				}
//			}
//		}
		return "44993a7c-9f2d-11ec-9774-c85b762b4884";
	}

	/**
	 * @param token
	 * @return credentials
	 */
	@Override
	public String getUserIdFromBasicAuthToken(String token) throws CustomException {
		GatewayCommonValidator.isValidString(token);
		byte[] decodedCredentials = Base64.getDecoder().decode(token);
		String credentials = new String(decodedCredentials, StandardCharsets.UTF_8);
		String[] values = credentials.split(ApplicationConstants.COLON, ApplicationConstants.SPLITTOKEN);
		if (ApplicationUtils.isValidateObject(values)) {
			return values[ApplicationConstants.ZERO];
		}
		return null;
	}

//	public void setiUserInstitutionLinkDaoImpl(IUserInstitutionLinkDao iUserInstitutionLinkDaoImpl) {
//		this.iUserInstitutionLinkDaoImpl = iUserInstitutionLinkDaoImpl;
//	}

	public List<UserProfileDTO> convertIntoUserProfileDto(List<UserProfile> userList){
		List<UserProfileDTO> resultDto = new ArrayList<>();
		for(UserProfile user: userList) {
			UserProfileDTO userdto = new UserProfileDTO();
			userdto.setUserName(user.getUserName());
			userdto.setEmailId(user.getEmailId());
			userdto.setFirstName(user.getFirstName());
			userdto.setLastName(user.getLastName());
			userdto.setMobile(user.getMobileNumber());
			userdto.setUserId(user.getUserId());
 
			resultDto.add(userdto);
		}
		return resultDto;
	}

	/**
	 * @param passwordResetKey
	 * @param email
	 * @param appUser
	 */
	private void sendPasswordResetLink(String passwordResetKey, String email, UserProfile appUser) {
		String resetPasswordUrl = generateResetPasswordUrl(passwordResetKey);
		EmailSenderDto emailSenderDto = new EmailSenderDto();
		String[] recepientList = {email};
		emailSenderDto.setRecipient(recepientList);
		emailSenderDto.setProcessType(EmailConstants.RESET_PWD_PROCESS_TYPE);
		Map<String, Object> placeHolderMap = new HashMap<String, Object>();
		placeHolderMap.put(EmailConstants.USER_NAME, appUser.getUserName());
		placeHolderMap.put(EmailConstants.RESET_LINK, resetPasswordUrl);
		emailSenderDto.setPlaceHolder(placeHolderMap);
//		try {
//			kafkaPublish.publishEmailDataNonAsync(emailSenderDto);
//		} catch (JsonProcessingException | CustomException e) {
//			e.printStackTrace();
//		}
	}

//	@Override
//	public boolean getChangePassword(ChangePasswordDTO changePasswordDTO, boolean passwordPolicyOn,
//			boolean selectedPasswordPolicy, UserProfile userProfile) throws CustomException {
//		// TODO Auto-generated method stub
//		return false;
//	}

//	public void setICompanyDao(ICompanyDao companyDaoMock) {
//		// TODO Auto-generated method stub
//		̥
//	}


//public String userRoleStatus(RoleDto roleDto) throws CustomException {
//	  Role role = userDaoImpl.getRolesByIdentity(roleDto.getIdentity());
////	  role.getU(!roleDto.getUserRoleStatus());
//	  role.setRoleBlockStatus(!roleDto.getUserRoleStatus());
//	  role.setuse
//   userDaoImpl.updateUser(user);
//   if (Boolean.FALSE.equals(roleDto.getUserRoleStatus())) {
//       return ApplicationConstants.USER_BLOCKED_SUCCESS;
//   }
//   return ApplicationConstants.USER_UNBLOCKED_SUCCESS;
//}

	


//	public void setUserRoleById(IUserInstitutionLinkDao userInstitutionLinkDaoMock) {
//		this.iUserInstitutionLinkDaoImpl = userInstitutionLinkDaoMock;
//		
//	}


//	public void setUserAndInstitutionLinkingByUserId(IUserInstitutionLinkDao iUserInstitutionLinkDaoImplMock) {
//     this.iUserInstitutionLinkDaoImpl=iUserInstitutionLinkDaoImplMock;
//	}

//	public void setUserByEmailId(IUserDao userDaoMock) {
//		this.userDaoImpl=userDaoMock;
//		
//
//	}

}


	