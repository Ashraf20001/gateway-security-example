package com.cbt.supercharge.gateway.test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import com.cbt.supercharge.constants.core.ApplicationConstants;
import com.cbt.supercharge.transfer.objects.entity.Institution;
import com.cbt.supercharge.transfer.objects.entity.PasswordPolicy;
import com.cbt.supercharge.transfer.objects.entity.Role;
import com.cbt.supercharge.transfer.objects.entity.SystemConfig;
import com.cbt.supercharge.transfer.objects.entity.UserAndInstitutionLinking;
import com.cbt.supercharge.transfer.objects.entity.UserPassword;
import com.cbt.supercharge.transfer.objects.entity.UserProfile;
import com.cbt.supercharge.transfers.objects.mock.MockDatas;
import com.supercharge.gateway.models.AuthenticationResponse;

public class MockData {

	public static UserProfile getUserForError6() {
		UserProfile user = new UserProfile();
		user.setUserId(58);
		user.setUserName("test@gmail.com");
		user.setEmailId(getRandomString());
		user.setMobileNumber(getRandomString());
		user.setFirstName(getRandomString());
		user.setLastName(getRandomString());
		user.setUserPassword(getPasswords());
		user.setIsTwoFactorAuthentication(false);
		user.setInstitutionLinkingFlag(true);
		user.setUserAccountStatus(false);
		user.setIsDeleted(false);
		user.setApprovedBy(1);
		user.setIdentity("29aa6429-d7b3-4986-bfab-55dba5717d91");
		user.setLogout(false);
		user.setFailedCount(1);
		user.setUserStatus(true);
		user.setUserBlockedDate(getLocalDateTime(2));
		return user;
	}

	public static LocalDateTime getLocalDateTime(int i) {
		Calendar today = Calendar.getInstance();
		today.add(Calendar.DAY_OF_MONTH, i);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(today.getTime());
		calendar.set(Calendar.HOUR_OF_DAY, 00);
		calendar.set(Calendar.MINUTE, 00);
		calendar.set(Calendar.SECOND, 00);
		calendar.set(Calendar.MILLISECOND, 00);
		Date legacyDate = calendar.getTime();
		Instant instant = legacyDate.toInstant();
		ZoneId zone = ZoneId.systemDefault();
		ZonedDateTime zonedDateTime = instant.atZone(zone);
		LocalDateTime localDateTime = zonedDateTime.toLocalDateTime();
		return localDateTime;
	}

	public static String getRandomString() {
		return UUID.randomUUID().toString();
	}

	public static List<UserPassword> getPasswords() {
		UserPassword userPassword = new UserPassword();
		userPassword.setUmPwdId(getRandomNumber());
		List<UserPassword> passwordList = new ArrayList<>();
		passwordList.add(userPassword);
		return passwordList;
	}

	public static Integer getRandomNumber() {
		Random rand = new Random();
		return rand.nextInt();
	}

	public static List<UserAndInstitutionLinking> getUserAndInstitutionLinkingList() {
		UserAndInstitutionLinking userAndInstitutionLinking = new UserAndInstitutionLinking();
		userAndInstitutionLinking.setRole(getRole());
		userAndInstitutionLinking.setCompanyLinkingFlag(1);
		userAndInstitutionLinking.setInstitution(getInstitution());
		userAndInstitutionLinking.setUserAndInstitutionLinkingId(1);
		userAndInstitutionLinking.setUserProfile(MockData.getUserForError6());
		List<UserAndInstitutionLinking> userAndInstitutionLinkingList = new ArrayList<>();
		userAndInstitutionLinkingList.add(userAndInstitutionLinking);
		return userAndInstitutionLinkingList;
	}

	public static Role getRole() {
		Role role = new Role();
		role.setRoleId(1);
		role.setUserRoleName("support");
		role.setIdentity("cascdsdc12");
		role.setIsActive(true);
		role.setIsDeleted(false);
		return role;
	}

	public static Institution getInstitution() {
		Institution institution = new Institution();
		institution.setIdentity("dwedwedwedwedew333sddscc");
		institution.setInstitutionId(3);
		return institution;
	}

	public static PasswordPolicy getPasswordPolicyObject1() {
		PasswordPolicy pwdPolicy = new PasswordPolicy();
		pwdPolicy.setFailureAttempts(3);
		pwdPolicy.setExpiryPeriod(0);
		return pwdPolicy;
	}

	public static UserPassword getUserPasswordObject4() {
		UserPassword pwd = new UserPassword();
		Date currentDate = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(currentDate);
		calendar.add(Calendar.DAY_OF_YEAR, -1);
		Date tomorrowDate = calendar.getTime();
		pwd.setModifiedDate(tomorrowDate);
		return pwd;
	}

	public static List<SystemConfig> getSystemConfigDetailsForAuth3() {
		List<SystemConfig> value = new ArrayList<>();
		SystemConfig systemConfig1 = new SystemConfig();
		systemConfig1.setConfigName(ApplicationConstants.LOGOUT_TIME);
		systemConfig1.setConfigValue("0");
		value.add(systemConfig1);

		SystemConfig systemConfig2 = new SystemConfig();
		systemConfig2.setConfigName(ApplicationConstants.MANNUAL);
		systemConfig2.setConfigValue("0");
		value.add(systemConfig2);

		SystemConfig systemConfig3 = new SystemConfig();
		systemConfig3.setConfigName(ApplicationConstants.UN_BLOCKED_MINUTE);
		systemConfig3.setConfigValue("75");
		value.add(systemConfig3);

		SystemConfig systemConfig4 = new SystemConfig();
		systemConfig4.setConfigName(ApplicationConstants.FAILURE_ATTEMPTS);
		systemConfig4.setConfigValue("0");
		value.add(systemConfig4);

		SystemConfig systemConfig5 = new SystemConfig();
		systemConfig5.setConfigName(ApplicationConstants.WARNING_PERIOD);
		systemConfig5.setConfigValue("7500000");
		value.add(systemConfig5);

		SystemConfig systemConfig6 = new SystemConfig();
		systemConfig6.setConfigName(ApplicationConstants.EXPIRY_PERIOD);
		systemConfig6.setConfigValue("-1");
		value.add(systemConfig6);

		SystemConfig systemConfig7 = new SystemConfig();
		systemConfig7.setConfigName(ApplicationConstants.N_DAYS_LOGIN);
		systemConfig7.setConfigValue("10");
		value.add(systemConfig7);
		return value;
	}

	public static UserPassword getUserPasswordObject() throws ParseException {
		UserPassword pwd = new UserPassword();
		String dateString = "2024-10-28 10:32:51";
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = formatter.parse(dateString);
		pwd.setModifiedDate(date);
		return pwd;
	}

	public static List<SystemConfig> getSystemConfigDetailsForAuth() {
		List<SystemConfig> value = new ArrayList<>();
		SystemConfig systemConfig1 = new SystemConfig();
		systemConfig1.setConfigName(ApplicationConstants.LOGOUT_TIME);
		systemConfig1.setConfigValue("0");
		value.add(systemConfig1);

		SystemConfig systemConfig2 = new SystemConfig();
		systemConfig2.setConfigName(ApplicationConstants.MANNUAL);
		systemConfig2.setConfigValue("7500000");
		systemConfig2.setPropertyGroup(ApplicationConstants.ACCOUNT_LOCKOUT);
		systemConfig2.setIsChecked(Boolean.TRUE);
		value.add(systemConfig2);

		SystemConfig systemConfig3 = new SystemConfig();
		systemConfig3.setConfigName(ApplicationConstants.UN_BLOCKED_MINUTE);
		systemConfig3.setConfigValue("7500000");
		systemConfig3.setPropertyGroup(ApplicationConstants.ACCOUNT_LOCKOUT);
		systemConfig3.setIsChecked(Boolean.TRUE);
		value.add(systemConfig3);

		SystemConfig systemConfig4 = new SystemConfig();
		systemConfig4.setConfigName(ApplicationConstants.FAILURE_ATTEMPTS);
		systemConfig4.setConfigValue("10");
		value.add(systemConfig4);

		SystemConfig systemConfig5 = new SystemConfig();
		systemConfig5.setConfigName(ApplicationConstants.WARNING_PERIOD);
		systemConfig5.setConfigValue("7500000");
		value.add(systemConfig5);

		SystemConfig systemConfig6 = new SystemConfig();
		systemConfig6.setConfigName(ApplicationConstants.EXPIRY_PERIOD);
		systemConfig6.setConfigValue("-1");
		value.add(systemConfig6);

		SystemConfig systemConfig7 = new SystemConfig();
		systemConfig7.setConfigName(ApplicationConstants.N_DAYS_LOGIN);
		systemConfig7.setConfigValue("10");
		value.add(systemConfig7);

		SystemConfig systemConfig8 = new SystemConfig();
		systemConfig8.setConfigName("OTP Config Time");
		systemConfig8.setConfigValue("60");
		value.add(systemConfig8);

		SystemConfig systemConfig9 = new SystemConfig();
		systemConfig9.setConfigName("otpExpiryTime");
		systemConfig9.setConfigValue("60");
		value.add(systemConfig9);

		SystemConfig systemConfig10 = new SystemConfig();
		systemConfig10.setConfigName(ApplicationConstants.OTP_LENGTH);
		systemConfig10.setConfigValue("6");
		value.add(systemConfig10);

		SystemConfig systemConfig11 = new SystemConfig();
		systemConfig11.setConfigName("OTP attempt");
		systemConfig11.setConfigValue("6");
		value.add(systemConfig11);

		return value;
	}

	public static PasswordPolicy getPasswordPolicyObject() {
		PasswordPolicy pwdPolicy = new PasswordPolicy();
		pwdPolicy.setFailureAttempts(3);
		pwdPolicy.setExpiryPeriod(5);
		return pwdPolicy;
	}

	public static UserProfile getUserProfileDetails() {
		UserProfile loggedInUser = new UserProfile();
		loggedInUser.setUserId(9483);
		loggedInUser.setUserName("prabu");
		loggedInUser.setLastName("raj");
		loggedInUser.setIdentity("e4c20dec06c3453a9bc28e7469b381ae");
		loggedInUser.setIdentity("asdf123");
		loggedInUser.setFirstName("Ram");
		loggedInUser.setEmailId("ram@gmail.com");
		loggedInUser.setUserAccountStatus(false);
		loggedInUser.setInstitutionLinkingFlag(true);
		loggedInUser.setUserStatus(true);
		loggedInUser.setIsTwoFactorAuthentication(true);
		loggedInUser.setFailedCount(0);
		return loggedInUser;
	}

	public static List<SystemConfig> getSystemConfigDetailsForAuth8() {
		List<SystemConfig> value = new ArrayList<>();
		SystemConfig systemConfig1 = new SystemConfig();
		systemConfig1.setConfigName(ApplicationConstants.LOGOUT_TIME);
		systemConfig1.setConfigValue("0");
		value.add(systemConfig1);

		SystemConfig systemConfig2 = new SystemConfig();
		systemConfig2.setConfigName(ApplicationConstants.MANNUAL);
		systemConfig2.setConfigValue("0");
		systemConfig2.setPropertyGroup(ApplicationConstants.ACCOUNT_LOCKOUT);
		systemConfig2.setIsChecked(Boolean.FALSE);
		value.add(systemConfig2);

		SystemConfig systemConfig3 = new SystemConfig();
		systemConfig3.setConfigName(ApplicationConstants.UN_BLOCKED_MINUTE);
		systemConfig3.setConfigValue("110");
		systemConfig3.setPropertyGroup(ApplicationConstants.ACCOUNT_LOCKOUT);
		systemConfig3.setIsChecked(Boolean.TRUE);
		value.add(systemConfig3);

		SystemConfig systemConfig4 = new SystemConfig();
		systemConfig4.setConfigName(ApplicationConstants.FAILURE_ATTEMPTS);
		systemConfig4.setConfigValue("10");
		value.add(systemConfig4);

		SystemConfig systemConfig5 = new SystemConfig();
		systemConfig5.setConfigName(ApplicationConstants.WARNING_PERIOD);
		systemConfig5.setConfigValue("2");
		value.add(systemConfig5);

		SystemConfig systemConfig6 = new SystemConfig();
		systemConfig6.setConfigName(ApplicationConstants.EXPIRY_PERIOD);
		systemConfig6.setConfigValue("365");
		value.add(systemConfig6);

		SystemConfig systemConfig7 = new SystemConfig();
		systemConfig7.setConfigName(ApplicationConstants.N_DAYS_LOGIN);
		systemConfig7.setConfigValue("10");
		value.add(systemConfig7);

		SystemConfig systemConfig8 = new SystemConfig();
		systemConfig8.setConfigName(ApplicationConstants.TWO_FACTOR_STATUS);
		systemConfig8.setConfigValue("true");
		value.add(systemConfig8);
		return value;
	}

	public static UserProfile getUserProfile_fail() {
		UserProfile loggedInUser = new UserProfile();
		loggedInUser.setUserId(9483);
		loggedInUser.setIdentity("asdf123");
		loggedInUser.setFirstName("Ram");
		loggedInUser.setEmailId("ram@gmail.com");
		loggedInUser.setUserAccountStatus(true);
		loggedInUser.setInstitutionLinkingFlag(true);
		loggedInUser.setIsTwoFactorAuthentication(false);
		return loggedInUser;
	}

	public static UserProfile getUserProfile() {
		UserProfile loggedInUser = new UserProfile();
		loggedInUser.setUserId(9483);
		loggedInUser.setUserName("prabu");
		loggedInUser.setLastName("raj");
		loggedInUser.setIdentity("e4c20dec06c3453a9bc28e7469b381ae");
		loggedInUser.setIdentity("asdf123");
		loggedInUser.setFirstName("Ram");
		loggedInUser.setEmailId("ram@gmail.com");
		loggedInUser.setUserAccountStatus(false);
		loggedInUser.setInstitutionLinkingFlag(true);
		loggedInUser.setUserStatus(true);
		loggedInUser.setIsTwoFactorAuthentication(false);
		loggedInUser.setFailedCount(0);
		return loggedInUser;
	}

	public static List<SystemConfig> getSystemConfigDetailsForAuth4() {
		List<SystemConfig> value = new ArrayList<>();
		SystemConfig systemConfig1 = new SystemConfig();
		systemConfig1.setConfigName(ApplicationConstants.LOGOUT_TIME);
		systemConfig1.setConfigValue("0");
		value.add(systemConfig1);

		SystemConfig systemConfig2 = new SystemConfig();
		systemConfig2.setConfigName(ApplicationConstants.MANNUAL);
		systemConfig2.setConfigValue("0");
		value.add(systemConfig2);

		SystemConfig systemConfig3 = new SystemConfig();
		systemConfig3.setConfigName(ApplicationConstants.UN_BLOCKED_MINUTE);
		systemConfig3.setConfigValue("75");
		value.add(systemConfig3);

		SystemConfig systemConfig4 = new SystemConfig();
		systemConfig4.setConfigName(ApplicationConstants.FAILURE_ATTEMPTS);
		systemConfig4.setConfigValue("10");
		value.add(systemConfig4);

		SystemConfig systemConfig5 = new SystemConfig();
		systemConfig5.setConfigName(ApplicationConstants.WARNING_PERIOD);
		systemConfig5.setConfigValue("-1");
		value.add(systemConfig5);

		SystemConfig systemConfig6 = new SystemConfig();
		systemConfig6.setConfigName(ApplicationConstants.EXPIRY_PERIOD);
		systemConfig6.setConfigValue("1");
		value.add(systemConfig6);

		SystemConfig systemConfig7 = new SystemConfig();
		systemConfig7.setConfigName(ApplicationConstants.N_DAYS_LOGIN);
		systemConfig7.setConfigValue("10");
		value.add(systemConfig7);
		return value;
	}

	public static UserPassword getUserPasswordObject3() {
		UserPassword pwd = new UserPassword();
		Calendar today = Calendar.getInstance();
		today.add(Calendar.DAY_OF_MONTH, 2);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(today.getTime());
		calendar.set(Calendar.HOUR_OF_DAY, 00);
		calendar.set(Calendar.MINUTE, 00);
		calendar.set(Calendar.SECOND, 00);
		calendar.set(Calendar.MILLISECOND, 00);
		Date modifiedDate = calendar.getTime();
		pwd.setModifiedDate(modifiedDate);
		return pwd;
	}

	public static List<UserAndInstitutionLinking> getUserAndInstitutionLinking(Boolean isActive, Boolean isDeleted) {
		Role role = new Role();
		role.setIsActive(isActive);
		role.setIsDeleted(isDeleted);

		UserAndInstitutionLinking userAndInstitutionLinking = new UserAndInstitutionLinking();
		userAndInstitutionLinking.setRole(role);
		List<UserAndInstitutionLinking> userAndInstitutionLinkings = new ArrayList<>();
		userAndInstitutionLinkings.add(userAndInstitutionLinking);
		return userAndInstitutionLinkings;

	}

	public static List<UserAndInstitutionLinking> getUserAndInstitutionLinkingList2() {
		UserAndInstitutionLinking userAndInstitutionLinking = new UserAndInstitutionLinking();
		userAndInstitutionLinking.setRole(getRole());
		userAndInstitutionLinking.setCompanyLinkingFlag(1);
		userAndInstitutionLinking.setInstitution(MockDatas.getInstitution());
		userAndInstitutionLinking.setUserAndInstitutionLinkingId(1);
		userAndInstitutionLinking.setUserProfile(getUser());

		List<UserAndInstitutionLinking> userAndInstitutionLinkingList = new ArrayList<UserAndInstitutionLinking>();
		userAndInstitutionLinkingList.add(userAndInstitutionLinking);

		return userAndInstitutionLinkingList;
	}

	private static UserProfile getUser() {
		UserProfile user = new UserProfile();
		user.setUserId(1);
		user.setUserName("test");
		user.setEmailId("test1@gmail.com");
		user.setMobileNumber("987654321");
		user.setFirstName("firstName");
		user.setLastName("lastname");
		user.setUserPassword(getPasswords());
		user.setIsTwoFactorAuthentication(false);
		user.setInstitutionLinkingFlag(true);
		user.setUserAccountStatus(true);
		user.setIsDeleted(false);
		user.setIdentity("cascdsdc12");
		user.setUserIdentificationNumber("123");
		return user;
	}

	public static List<SystemConfig> getSystemConfigDetailsForAuth5() {
		List<SystemConfig> value = new ArrayList<>();
		SystemConfig systemConfig1 = new SystemConfig();
		systemConfig1.setConfigName(ApplicationConstants.LOGOUT_TIME);
		systemConfig1.setConfigValue("0");
		value.add(systemConfig1);

		SystemConfig systemConfig2 = new SystemConfig();
		systemConfig2.setConfigName(ApplicationConstants.MANNUAL);
		systemConfig2.setConfigValue("0");
		value.add(systemConfig2);

		SystemConfig systemConfig3 = new SystemConfig();
		systemConfig3.setConfigName(ApplicationConstants.UN_BLOCKED_MINUTE);
		systemConfig3.setConfigValue("75");
		value.add(systemConfig3);

		SystemConfig systemConfig4 = new SystemConfig();
		systemConfig4.setConfigName(ApplicationConstants.FAILURE_ATTEMPTS);
		systemConfig4.setConfigValue("10");
		value.add(systemConfig4);

		SystemConfig systemConfig5 = new SystemConfig();
		systemConfig5.setConfigName(ApplicationConstants.WARNING_PERIOD);
		systemConfig5.setConfigValue("2");
		value.add(systemConfig5);

		SystemConfig systemConfig6 = new SystemConfig();
		systemConfig6.setConfigName(ApplicationConstants.EXPIRY_PERIOD);
		systemConfig6.setConfigValue("-1");
		value.add(systemConfig6);

		SystemConfig systemConfig7 = new SystemConfig();
		systemConfig7.setConfigName(ApplicationConstants.N_DAYS_LOGIN);
		systemConfig7.setConfigValue("10");
		value.add(systemConfig7);
		return value;
	}

	public static List<SystemConfig> getSystemConfigDetailsForAuth2() {
		List<SystemConfig> value = new ArrayList<>();
		SystemConfig systemConfig1 = new SystemConfig();
		systemConfig1.setConfigName(ApplicationConstants.LOGOUT_TIME);
		systemConfig1.setConfigValue("0");
		value.add(systemConfig1);

		SystemConfig systemConfig2 = new SystemConfig();
		systemConfig2.setConfigName(ApplicationConstants.MANNUAL);
		systemConfig2.setPropertyGroup(ApplicationConstants.ACCOUNT_LOCKOUT);
		systemConfig2.setConfigValue("0");
		systemConfig2.setIsChecked(Boolean.FALSE);
		value.add(systemConfig2);

		SystemConfig systemConfig3 = new SystemConfig();
		systemConfig3.setConfigName(ApplicationConstants.UN_BLOCKED_MINUTE);
		systemConfig3.setPropertyGroup(ApplicationConstants.ACCOUNT_LOCKOUT);
		systemConfig3.setConfigValue("75");
		systemConfig3.setIsChecked(Boolean.TRUE);
		value.add(systemConfig3);

		SystemConfig systemConfig4 = new SystemConfig();
		systemConfig4.setConfigName(ApplicationConstants.FAILURE_ATTEMPTS);
		systemConfig4.setConfigValue("10");
		value.add(systemConfig4);

		SystemConfig systemConfig5 = new SystemConfig();
		systemConfig5.setConfigName(ApplicationConstants.WARNING_PERIOD);
		systemConfig5.setConfigValue("-1");
		value.add(systemConfig5);

		SystemConfig systemConfig6 = new SystemConfig();
		systemConfig6.setConfigName(ApplicationConstants.EXPIRY_PERIOD);
		systemConfig6.setConfigValue("7500000");
		value.add(systemConfig6);

		SystemConfig systemConfig7 = new SystemConfig();
		systemConfig7.setConfigName(ApplicationConstants.N_DAYS_LOGIN);
		systemConfig7.setConfigValue("10");
		value.add(systemConfig7);

		SystemConfig systemConfig11 = new SystemConfig();
		systemConfig11.setConfigName("OTP attempt");
		systemConfig11.setConfigValue("0");
		value.add(systemConfig11);

		SystemConfig systemConfig12 = new SystemConfig();
		systemConfig12.setConfigName("otpExpiryTime");
		systemConfig12.setConfigValue("30");
		value.add(systemConfig12);

		return value;
	}

	public static UserProfile getUserForError5() {
		UserProfile user = new UserProfile();
		user.setUserId(58);
		user.setUserName("test@gmail.com");
		user.setEmailId(getRandomString());
		user.setMobileNumber(getRandomString());
		user.setFirstName(getRandomString());
		user.setLastName(getRandomString());
		user.setUserPassword(getPasswords());
		user.setIsTwoFactorAuthentication(false);
		user.setInstitutionLinkingFlag(true);
		user.setUserAccountStatus(false);
		user.setIsDeleted(false);
		user.setApprovedBy(1);
		user.setIdentity("29aa6429-d7b3-4986-bfab-55dba5717d91");
		user.setLogout(false);
		user.setFailedCount(1);
		user.setUserStatus(false);
		user.setUserBlockedDate(getLocalDateTime(2));
		return user;
	}

	public static UserProfile getUserForError4() {
		UserProfile user = new UserProfile();
		user.setUserId(58);
		user.setUserName("test@gmail.com");
		user.setEmailId(getRandomString());
		user.setMobileNumber(getRandomString());
		user.setFirstName(getRandomString());
		user.setLastName(getRandomString());
		user.setUserPassword(getPasswords());
		user.setIsTwoFactorAuthentication(false);
		user.setInstitutionLinkingFlag(true);
		user.setUserAccountStatus(true);
		user.setIsDeleted(false);
		user.setApprovedBy(1);
		user.setIdentity("29aa6429-d7b3-4986-bfab-55dba5717d91");
		user.setLogout(true);
		user.setFailedCount(1);
		user.setUserStatus(true);
		user.setUserBlockedDate(getLocalDateTime(2));
		return user;
	}

	public static UserProfile getUserForError3() {
		UserProfile user = new UserProfile();
		user.setUserId(58);
		user.setUserName("test@gmail.com");
		user.setEmailId(getRandomString());
		user.setMobileNumber(getRandomString());
		user.setFirstName(getRandomString());
		user.setLastName(getRandomString());
		user.setUserPassword(getPasswords());
		user.setIsTwoFactorAuthentication(false);
		user.setInstitutionLinkingFlag(true);
		user.setUserAccountStatus(true);
		user.setIsDeleted(false);
		user.setApprovedBy(1);
		user.setIdentity("29aa6429-d7b3-4986-bfab-55dba5717d91");
		user.setLogout(true);
		user.setFailedCount(5);
		user.setUserStatus(true);
		user.setUserBlockedDate(getLocalDateTime(2));
		return user;
	}

	public static UserProfile getUserForError() {
		UserProfile user = new UserProfile();
		user.setUserId(58);
		user.setUserName("test@gmail.com");
		user.setEmailId(getRandomString());
		user.setMobileNumber(getRandomString());
		user.setFirstName(getRandomString());
		user.setLastName(getRandomString());
		user.setUserPassword(getPasswords());
		user.setIsTwoFactorAuthentication(false);
		user.setInstitutionLinkingFlag(true);
		user.setUserAccountStatus(true);
		user.setIsDeleted(false);
		user.setApprovedBy(1);
		user.setIdentity("29aa6429-d7b3-4986-bfab-55dba5717d91");
		user.setLogout(false);
		user.setLastDayLogin(getDateTime(-100));
		user.setFailedCount(4);
		user.setUserStatus(true);
		return user;
	}

	public static Date getDateTime(int i) {
		Calendar today = Calendar.getInstance();
		today.add(Calendar.DAY_OF_MONTH, i);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(today.getTime());
		calendar.set(Calendar.HOUR_OF_DAY, 00);
		calendar.set(Calendar.MINUTE, 00);
		calendar.set(Calendar.SECOND, 00);
		calendar.set(Calendar.MILLISECOND, 00);
		return calendar.getTime();
	}

	public static SystemConfig getSystemConfig1() {
		SystemConfig systemConfig = new SystemConfig();
		systemConfig.setConfigName("abc");
		systemConfig.setConfigValue("test");
		systemConfig.setIsChecked(true);
		systemConfig.setPropertyGroup("test");
		return systemConfig;
	}

	public static AuthenticationResponse getAuthResponse() {
		AuthenticationResponse resp = new AuthenticationResponse();
		resp.setAccessToken("accessToken");
		resp.setExpiresIn(8);
		resp.setRefreshToken("refreshToken");
		return resp;
	}

	public static UserProfile getUserProfile_error_flow() {
		UserProfile loggedInUser = new UserProfile();
		loggedInUser.setUserId(9483);
		loggedInUser.setIdentity("asdf123");
		loggedInUser.setFirstName("Ram");
		loggedInUser.setEmailId("ram@gmail.com");
		loggedInUser.setUserAccountStatus(false);
		loggedInUser.setInstitutionLinkingFlag(true);
		loggedInUser.setIsTwoFactorAuthentication(false);
		loggedInUser.setFailedCount(1);
		return loggedInUser;
	}

	public static UserProfile getUserProfile_error_flow1() {
		UserProfile loggedInUser = new UserProfile();
		loggedInUser.setUserId(9483);
		loggedInUser.setIdentity("asdf123");
		loggedInUser.setFirstName("Ram");
		loggedInUser.setEmailId("ram@gmail.com");
		loggedInUser.setUserAccountStatus(false);
		loggedInUser.setInstitutionLinkingFlag(false);
		loggedInUser.setIsTwoFactorAuthentication(false);
		loggedInUser.setFailedCount(1);
		return loggedInUser;
	}

	public static UserProfile getUserProfile4() {
		UserProfile loggedInUser = new UserProfile();
		loggedInUser.setUserId(9483);
		loggedInUser.setUserName("prabu");
		loggedInUser.setLastName("raj");
		loggedInUser.setIdentity("e4c20dec06c3453a9bc28e7469b381ae");
		loggedInUser.setIdentity("asdf123");
		loggedInUser.setFirstName("Ram");
		loggedInUser.setEmailId("ram@gmail.com");
		loggedInUser.setUserAccountStatus(false);
		loggedInUser.setInstitutionLinkingFlag(true);
		loggedInUser.setUserStatus(true);
		loggedInUser.setIsTwoFactorAuthentication(true);
		loggedInUser.setFailedCount(15);
		return loggedInUser;

	}

	public static UserPassword getUserPassword() {
		UserPassword userPassword = new UserPassword();
		userPassword.setUmUsrPwd("test");
		return userPassword;
	}

}
