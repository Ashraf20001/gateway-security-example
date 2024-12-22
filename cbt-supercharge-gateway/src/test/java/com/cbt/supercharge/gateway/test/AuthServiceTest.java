package com.cbt.supercharge.gateway.test;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.cbt.supercharge.constants.core.ApplicationConstants;
import com.cbt.supercharge.exception.core.ApplicationException;
import com.cbt.supercharge.exception.core.codes.ErrorCodes;
import com.cbt.supercharge.transfer.objects.entity.SystemConfig;
import com.cbt.supercharge.transfer.objects.entity.UserProfile;
import com.cbt.supercharge.transfers.objects.mock.MockDatas;
import com.cbt.supercharge.transfter.objects.core.entity.vo.SystemConfigVo;
import com.cbt.supercharge.utils.core.ApplicationDateUtils;
import com.supercharge.gateway.auth.service.AuthServiceImpl;
import com.supercharge.gateway.common.base.dao.CommonUserDaoImpl;
import com.supercharge.gateway.common.utils.JwtUtils;
import com.supercharge.gateway.models.AuthenticationResponse;
import com.supercharge.gateway.security.model.User;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AuthServiceTest {

	@InjectMocks
	private AuthServiceImpl authServiceImpl;

	@Mock
	private CommonUserDaoImpl commonUserDaoImpl;

	@Mock
	private ApplicationDateUtils applicationDateUtils;

	@Mock
	private JwtUtils jwtUtils;

	@Mock
	private UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken;

	@Mock
	private Authentication authentication;

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		authServiceImpl.setApplicationDateUtils(applicationDateUtils);
		authServiceImpl.setCommonUserDaoImpl(commonUserDaoImpl);
		authServiceImpl.setJwtUtils(jwtUtils);
	}

	@Test
	public void generateAuthenticationToken_errorFlow() {
		try {
			Principal user = new Principal() {
				@Override
				public String getName() {
					return "asd";
				}
			};
			Principal principal = getPrincipal(user);
			User userDetails = new User();
			userDetails.setUsername("aaa");
			userDetails.setRoles(Arrays.asList("Role"));
			when(commonUserDaoImpl.getUserBySystemUserID(anyString())).thenReturn(MockData.getUserForError6());
			when(commonUserDaoImpl.updateUser(any())).thenReturn("abc");
			when(commonUserDaoImpl.getUserAndInstitutionLinkingByUserId(any()))
					.thenReturn(MockData.getUserAndInstitutionLinkingList());
			when(commonUserDaoImpl.getPassWordPolicyById(ArgumentMatchers.anyInt()))
					.thenReturn(MockData.getPasswordPolicyObject1());
			when(commonUserDaoImpl.getPasswordByUserId(ArgumentMatchers.anyInt()))
					.thenReturn(MockData.getUserPasswordObject4());
			when(applicationDateUtils.getDateDifference(any(), any())).thenReturn(1);
			when(commonUserDaoImpl.getSystemConfigDetails()).thenReturn(MockData.getSystemConfigDetailsForAuth3());
			authServiceImpl.generateAuthenticationToken(principal, MockData.getUserForError6());
			Assert.fail();
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	/**
	 * @param user
	 * @return
	 */
	private Principal getPrincipal(Principal user) {
		UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
				user, null);
		SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
		Principal principal = (Principal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return principal;
	}

	@Test
	public void generateAuthenticationToken_happy_isexpiredTrue() {
		try {
			AuthenticationResponse expectedValue = null;
			Principal user = new Principal() {
				@Override
				public String getName() {
					return "asd";
				}
			};
			Principal principal = getPrincipal(user);
			User loggedInUser = getUserDetails();
			when(commonUserDaoImpl.getUserBySystemUserID(anyString())).thenReturn(MockData.getUserProfile());
			when(commonUserDaoImpl.updateUser(any())).thenReturn("abc");
			when(commonUserDaoImpl.getUserAndInstitutionLinkingByUserId(any()))
					.thenReturn(MockData.getUserAndInstitutionLinkingList());
			when(commonUserDaoImpl.getPassWordPolicyById(ArgumentMatchers.anyInt()))
					.thenReturn(MockData.getPasswordPolicyObject());
			when(commonUserDaoImpl.getPasswordByUserId(ArgumentMatchers.anyInt()))
					.thenReturn(MockData.getUserPasswordObject());
			when(commonUserDaoImpl.getSystemConfigDetails()).thenReturn(MockData.getSystemConfigDetailsForAuth());
			AuthenticationResponse actualValue = authServiceImpl.generateAuthenticationToken(principal,
					MockData.getUserProfile());
			Assert.fail();
		} catch (Exception exception) {
			assertNotNull(exception);
		}
	}

	@Test
	public void generateAuthenticationToken_fail() {
		try {
			Principal user = new Principal() {
				@Override
				public String getName() {
					return "asd";
				}
			};
			Principal principal = getPrincipal(user);
			User loggedInUser = getUserDetails();
			when(commonUserDaoImpl.getUserBySystemUserID(anyString())).thenReturn(MockData.getUserProfile_fail());
			when(commonUserDaoImpl.updateUser(any())).thenReturn("abc");
			when(commonUserDaoImpl.getUserAndInstitutionLinkingByUserId(any()))
					.thenReturn(MockData.getUserAndInstitutionLinkingList());
			when(commonUserDaoImpl.getPassWordPolicyById(ArgumentMatchers.anyInt()))
					.thenReturn(MockData.getPasswordPolicyObject());
			when(commonUserDaoImpl.getPasswordByUserId(ArgumentMatchers.anyInt()))
					.thenReturn(MockData.getUserPasswordObject());
			when(commonUserDaoImpl.getSystemConfigDetails()).thenReturn(MockData.getSystemConfigDetailsForAuth());
			when(commonUserDaoImpl.updateUser(any())).thenReturn("abc");
			authServiceImpl.generateAuthenticationToken(principal, MockData.getUserProfile_fail());
			Assert.fail();
		} catch (Exception exception) {
			exception.printStackTrace();
			assertNotNull(exception);
		}
	}

	@Test
	public void generateAuthenticationToken_happyflow() {
		UserProfile userProfileDetails = MockData.getUserProfileDetails();
		userProfileDetails.setUserStatus(Boolean.TRUE);
		userProfileDetails.setUserBlockedDate(LocalDateTime.now());
		userProfileDetails.setFailedCount(15);
		try {
			Principal user = new Principal() {
				@Override
				public String getName() {
					return "asd";
				}
			};
			User loggedInUser = getUserDetails();
			UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = userPasswordAuth(loggedInUser);
			when(commonUserDaoImpl.getUserBySystemUserID(anyString())).thenReturn(userProfileDetails);
//			User loggedInUser = getUserDetails();
			when(commonUserDaoImpl.updateUser(MockDatas.getUser())).thenReturn("5ebe7c98328178b8e7e3b3a1");
			when(commonUserDaoImpl.getUserAndInstitutionLinkingByUserId(any()))
					.thenReturn(MockData.getUserAndInstitutionLinkingList());
			when(commonUserDaoImpl.getPassWordPolicyById(ArgumentMatchers.anyInt()))
					.thenReturn(MockData.getPasswordPolicyObject());
			when(commonUserDaoImpl.getPasswordByUserId(ArgumentMatchers.anyInt()))
					.thenReturn(MockData.getUserPasswordObject());
			when(commonUserDaoImpl.getSystemConfigDetails()).thenReturn(MockData.getSystemConfigDetailsForAuth8());
			when(commonUserDaoImpl.updateUser(MockDatas.getUser())).thenReturn("5ebe7c98328178b8e7e3b3a1");
			authServiceImpl.generateAuthenticationToken(usernamePasswordAuthenticationToken, userProfileDetails);
		} catch (Exception exception) {
			exception.printStackTrace();
			Assert.fail();
		}
	}

	@Test
	public void generateAuthenticationToken_happyFlow2() {
		try {
			Principal user = new Principal() {
				@Override
				public String getName() {
					return "asd";
				}
			};
			User loggedInUser = getUserDetails();
			UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = userPasswordAuth(loggedInUser);
			Principal principal = getPrincipal(user);
			UserProfile userProfileDetails = MockData.getUserProfileDetails();
			userProfileDetails.setUserStatus(Boolean.TRUE);
			userProfileDetails.setUserAccountStatus(Boolean.TRUE);
			userProfileDetails.setUserBlockedDate(LocalDateTime.now().minusMinutes(300));
			userProfileDetails.setFailedCount(15);
			when(commonUserDaoImpl.getUserBySystemUserID(anyString())).thenReturn(userProfileDetails);
			when(commonUserDaoImpl.updateUser(MockDatas.getUser())).thenReturn("5ebe7c98328178b8e7e3b3a1");
			when(commonUserDaoImpl.getUserAndInstitutionLinkingByUserId(any()))
					.thenReturn(MockData.getUserAndInstitutionLinkingList());
			when(commonUserDaoImpl.getPassWordPolicyById(ArgumentMatchers.anyInt()))
					.thenReturn(MockData.getPasswordPolicyObject());
			when(commonUserDaoImpl.getPasswordByUserId(ArgumentMatchers.anyInt()))
					.thenReturn(MockData.getUserPasswordObject());
			when(commonUserDaoImpl.getSystemConfigDetails()).thenReturn(MockData.getSystemConfigDetailsForAuth8());
			when(commonUserDaoImpl.updateUser(MockDatas.getUser())).thenReturn("5ebe7c98328178b8e7e3b3a1");
			authServiceImpl.generateAuthenticationToken(usernamePasswordAuthenticationToken, userProfileDetails);
		} catch (Exception exception) {
			exception.printStackTrace();
			Assert.fail();
		}
	}

	@Test
	public void generateAuthenticationToken_errorflow34() {
		try {
			Principal user = new Principal() {
				@Override
				public String getName() {
					return "asd";
				}
			};
			Principal principal = getPrincipal(user);
			User loggedInUser = getUserDetails();
			UserProfile userProfileDetails = MockData.getUserProfileDetails();
			userProfileDetails.setUserStatus(Boolean.TRUE);
			userProfileDetails.setUserAccountStatus(Boolean.TRUE);
			userProfileDetails.setUserBlockedDate(LocalDateTime.now());
			userProfileDetails.setFailedCount(15);
			when(commonUserDaoImpl.getUserBySystemUserID(anyString())).thenReturn(userProfileDetails);
			when(commonUserDaoImpl.updateUser(any())).thenReturn("abc");
			when(commonUserDaoImpl.getUserAndInstitutionLinkingByUserId(any()))
					.thenReturn(MockData.getUserAndInstitutionLinkingList());
			when(commonUserDaoImpl.getPassWordPolicyById(ArgumentMatchers.anyInt()))
					.thenReturn(MockData.getPasswordPolicyObject());
			when(commonUserDaoImpl.getPasswordByUserId(ArgumentMatchers.anyInt()))
					.thenReturn(MockData.getUserPasswordObject());
			when(commonUserDaoImpl.getSystemConfigDetails()).thenReturn(MockData.getSystemConfigDetailsForAuth8());
			when(commonUserDaoImpl.updateUser(any())).thenReturn("abc");
			authServiceImpl.generateAuthenticationToken(principal, userProfileDetails);
			Assert.fail();
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	@Test
	public void generateAuthenticationToken_errorflow33() {
		try {
			Principal user = new Principal() {
				@Override
				public String getName() {
					return "asd";
				}
			};
			User loggedInUser = getUserDetails();
			UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = userPasswordAuth(loggedInUser);
			Principal principal = getPrincipal(user);
			UserProfile userProfileDetails = MockData.getUserProfileDetails();
			userProfileDetails.setUserStatus(Boolean.TRUE);
			userProfileDetails.setUserBlockedDate(LocalDateTime.now());
			userProfileDetails.setFailedCount(15);
			when(commonUserDaoImpl.getUserBySystemUserID(anyString())).thenReturn(userProfileDetails);
			when(commonUserDaoImpl.updateUser(MockDatas.getUser())).thenReturn("5ebe7c98328178b8e7e3b3a1");
			when(commonUserDaoImpl.getUserAndInstitutionLinkingByUserId(any()))
					.thenReturn(MockData.getUserAndInstitutionLinkingList());
			when(commonUserDaoImpl.getPassWordPolicyById(ArgumentMatchers.anyInt()))
					.thenReturn(MockData.getPasswordPolicyObject());
			when(commonUserDaoImpl.getPasswordByUserId(ArgumentMatchers.anyInt()))
					.thenReturn(MockData.getUserPasswordObject());
			when(commonUserDaoImpl.getSystemConfigDetails()).thenReturn(MockData.getSystemConfigDetailsForAuth8());
			when(commonUserDaoImpl.updateUser(MockDatas.getUser())).thenReturn("5ebe7c98328178b8e7e3b3a1");
			authServiceImpl.generateAuthenticationToken(usernamePasswordAuthenticationToken, userProfileDetails);
		} catch (Exception exception) {
			exception.printStackTrace();
			Assert.fail();
		}
	}

	@Test
	public void generateAuthenticationToken_errorflow31() {
		try {
			Principal user = new Principal() {
				@Override
				public String getName() {
					return "asd";
				}
			};
			Principal principal = getPrincipal(user);
			User loggedInUser = getUserDetails();
			UserProfile userProfileDetails = MockData.getUserProfileDetails();
			userProfileDetails.setLogout(false);
			userProfileDetails.setLastDayLogin(new Date());
			userProfileDetails.setUserAccountStatus(true);
			userProfileDetails.setFailedCount(15);
			userProfileDetails.setUserBlockedDate(LocalDateTime.now().minusMinutes(105));
			SystemConfig systemConfig8 = new SystemConfig();
			systemConfig8.setConfigName(ApplicationConstants.TWO_FACTOR_STATUS);
			systemConfig8.setConfigValue("true");
			List<SystemConfig> systemConfigs = new ArrayList<SystemConfig>();
			systemConfigs = MockData.getSystemConfigDetailsForAuth4();
			systemConfigs.add(systemConfig8);
			when(commonUserDaoImpl.getUserBySystemUserID(anyString())).thenReturn(userProfileDetails);
			when(commonUserDaoImpl.updateUser(any())).thenReturn("abc");
			when(commonUserDaoImpl.getUserAndInstitutionLinkingByUserId(any()))
					.thenReturn(MockData.getUserAndInstitutionLinkingList());
			when(commonUserDaoImpl.getPassWordPolicyById(ArgumentMatchers.anyInt()))
					.thenReturn(MockData.getPasswordPolicyObject1());
			when(commonUserDaoImpl.getPasswordByUserId(ArgumentMatchers.anyInt()))
					.thenReturn(MockData.getUserPasswordObject3());
			when(applicationDateUtils.getDateDifference(any(), any())).thenReturn(-1);
			when(commonUserDaoImpl.getSystemConfigDetails()).thenReturn(systemConfigs);
			authServiceImpl.generateAuthenticationToken(principal, userProfileDetails);
			Assert.fail();
		} catch (Exception exception) {
			exception.printStackTrace();
			assertNotNull(exception);
		}
	}

	@Test
	public void generateAuthenticationToken_errorflow29() {
		try {
			Principal user = new Principal() {
				@Override
				public String getName() {
					return "asd";
				}
			};
			User loggedInUser = getUserDetails();
			Principal principal = getPrincipal(user);
			UserProfile userProfileDetails = MockData.getUserForError6();
			when(commonUserDaoImpl.getUserBySystemUserID(anyString())).thenReturn(userProfileDetails);
			when(commonUserDaoImpl.updateUser(any())).thenReturn("abc");
			when(commonUserDaoImpl.getUserAndInstitutionLinkingByUserId(any()))
					.thenReturn(MockData.getUserAndInstitutionLinking(false, false));
			authServiceImpl.generateAuthenticationToken(principal, userProfileDetails);
			Assert.fail();
		} catch (Exception exception) {
			exception.printStackTrace();
			assertNotNull(exception);
		}
	}

	@Test
	public void generateAuthenticationToken_errorflow27() {
		try {
			Principal user = new Principal() {
				@Override
				public String getName() {
					return "asd";
				}
			};
			User loggedInUser = getUserDetails();
			Principal principal = getPrincipal(user);
			UserProfile userProfileDetails = MockData.getUserProfileDetails();
			when(commonUserDaoImpl.getUserBySystemUserID(anyString())).thenReturn(userProfileDetails);
			when(commonUserDaoImpl.getUserAndInstitutionLinkingByUserId(any()))
					.thenReturn(MockData.getUserAndInstitutionLinkingList2());
			when(commonUserDaoImpl.updateUser(any())).thenReturn("abc");
			when(commonUserDaoImpl.getPassWordPolicyById(ArgumentMatchers.anyInt()))
					.thenReturn(MockData.getPasswordPolicyObject1());
			when(commonUserDaoImpl.getPasswordByUserId(ArgumentMatchers.anyInt()))
					.thenReturn(MockData.getUserPasswordObject3());
			when(applicationDateUtils.getDateDifference(any(), any())).thenReturn(-1);
			when(commonUserDaoImpl.getSystemConfigDetails()).thenReturn(MockData.getSystemConfigDetailsForAuth4());
			doThrow(new ApplicationException(ErrorCodes.BAD_REQUEST)).when(commonUserDaoImpl)
					.getUserAndInstitutionLinkingByUserId(any());
			authServiceImpl.generateAuthenticationToken(principal, userProfileDetails);
			Assert.fail();
		} catch (Exception exception) {
			exception.printStackTrace();
			assertNotNull(exception);
		}
	}

	@Test
	public void generateAuthenticationToken_errorflow26() {
		try {
			Principal user = new Principal() {
				@Override
				public String getName() {
					return "asd";
				}
			};
			User loggedInUser = getUserDetails();
			Principal principal = getPrincipal(user);
			UserProfile userProfileDetails = MockData.getUserProfileDetails();
			userProfileDetails.setLogout(false);
			userProfileDetails.setLastDayLogin(new Date());
			when(commonUserDaoImpl.getUserBySystemUserID(anyString())).thenReturn(userProfileDetails);
			when(commonUserDaoImpl.updateUser(MockDatas.getUser())).thenReturn("5ebe7c98328178b8e7e3b3a1");
			when(commonUserDaoImpl.getUserAndInstitutionLinkingByUserId(any()))
					.thenReturn(MockData.getUserAndInstitutionLinkingList());
			when(commonUserDaoImpl.getPassWordPolicyById(ArgumentMatchers.anyInt()))
					.thenReturn(MockData.getPasswordPolicyObject1());
			when(commonUserDaoImpl.getPasswordByUserId(ArgumentMatchers.anyInt()))
					.thenReturn(MockData.getUserPasswordObject3());
			when(applicationDateUtils.getDateDifference(any(), any())).thenReturn(-1);
			when(commonUserDaoImpl.getSystemConfigDetails()).thenReturn(MockData.getSystemConfigDetailsForAuth4());
			authServiceImpl.generateAuthenticationToken(principal, userProfileDetails);
			Assert.fail();
		} catch (Exception exception) {
			exception.printStackTrace();

		}
	}

	@Test
	public void generateAuthenticationToken_errorflow25() {
		try {
			Principal user = new Principal() {
				@Override
				public String getName() {
					return "asd";
				}
			};
			User loggedInUser = getUserDetails();
			Principal principal = getPrincipal(user);
			UserProfile userProfileDetails = MockData.getUserForError6();
			when(commonUserDaoImpl.getUserBySystemUserID(anyString())).thenReturn(userProfileDetails);
			when(commonUserDaoImpl.updateUser(MockDatas.getUser())).thenReturn("5ebe7c98328178b8e7e3b3a1");
			when(commonUserDaoImpl.getUserAndInstitutionLinkingByUserId(any()))
					.thenReturn(MockData.getUserAndInstitutionLinkingList());
			when(commonUserDaoImpl.getPassWordPolicyById(ArgumentMatchers.anyInt()))
					.thenReturn(MockData.getPasswordPolicyObject1());
			when(commonUserDaoImpl.getPasswordByUserId(ArgumentMatchers.anyInt()))
					.thenReturn(MockData.getUserPasswordObject3());
			when(applicationDateUtils.getDateDifference(any(), any())).thenReturn(-1);
			when(commonUserDaoImpl.getSystemConfigDetails()).thenReturn(MockData.getSystemConfigDetailsForAuth4());
			authServiceImpl.generateAuthenticationToken(principal, userProfileDetails);
			Assert.fail();
		} catch (Exception exception) {
			exception.printStackTrace();

		}
	}

	@Test
	public void generateAuthenticationToken_errorflow24() {
		try {
			Principal user = new Principal() {
				@Override
				public String getName() {
					return "asd";
				}
			};
			User loggedInUser = getUserDetails();
			Principal principal = getPrincipal(user);
			UserProfile userProfileDetails = MockData.getUserForError6();
			when(commonUserDaoImpl.getUserBySystemUserID(anyString())).thenReturn(userProfileDetails);
			when(commonUserDaoImpl.getUserAndInstitutionLinkingByUserId(any()))
					.thenReturn(MockData.getUserAndInstitutionLinkingList2());
			when(commonUserDaoImpl.updateUser(MockDatas.getUser())).thenReturn("5ebe7c98328178b8e7e3b3a1");
			when(commonUserDaoImpl.getUserAndInstitutionLinkingByUserId(any()))
					.thenReturn(MockData.getUserAndInstitutionLinkingList());
			when(commonUserDaoImpl.getPassWordPolicyById(ArgumentMatchers.anyInt()))
					.thenReturn(MockData.getPasswordPolicyObject1());
			when(commonUserDaoImpl.getPasswordByUserId(ArgumentMatchers.anyInt()))
					.thenReturn(MockData.getUserPasswordObject3());
			when(applicationDateUtils.getDateDifference(any(), any())).thenReturn(-1);
			when(commonUserDaoImpl.getSystemConfigDetails()).thenReturn(MockData.getSystemConfigDetailsForAuth4());
			authServiceImpl.generateAuthenticationToken(principal, userProfileDetails);
			Assert.fail();
		} catch (Exception exception) {
			exception.printStackTrace();

		}
	}

	@Test
	public void generateAuthenticationToken_errorflow23() {
		try {
			Principal user = new Principal() {
				@Override
				public String getName() {
					return "asd";
				}
			};
			User loggedInUser = getUserDetails();
			Principal principal = getPrincipal(user);
			UserProfile userProfileDetails = MockData.getUserForError6();
			when(commonUserDaoImpl.getUserBySystemUserID(anyString())).thenReturn(userProfileDetails);
			when(commonUserDaoImpl.updateUser(any())).thenReturn("abc");
			when(commonUserDaoImpl.getUserAndInstitutionLinkingByUserId(any()))
					.thenReturn(MockData.getUserAndInstitutionLinkingList());
			when(commonUserDaoImpl.getPassWordPolicyById(ArgumentMatchers.anyInt()))
					.thenReturn(MockData.getPasswordPolicyObject1());
			when(commonUserDaoImpl.getPasswordByUserId(ArgumentMatchers.anyInt()))
					.thenReturn(MockData.getUserPasswordObject3());
			when(applicationDateUtils.getDateDifference(any(), any())).thenReturn(-1);
			when(commonUserDaoImpl.getSystemConfigDetails()).thenReturn(MockData.getSystemConfigDetailsForAuth5());
			authServiceImpl.generateAuthenticationToken(principal, userProfileDetails);
			Assert.fail();
		} catch (Exception exception) {
			exception.printStackTrace();
			assertNotNull(exception);
		}
	}

	@Test
	public void generateAuthenticationToken_errorflow22() {
		try {
			Principal user = new Principal() {
				@Override
				public String getName() {
					return "asd";
				}
			};
			User loggedInUser = getUserDetails();
			Principal principal = getPrincipal(user);
			UserProfile userProfileDetails = MockData.getUserForError5();
			when(commonUserDaoImpl.getUserBySystemUserID(anyString())).thenReturn(userProfileDetails);
			when(commonUserDaoImpl.updateUser(any())).thenReturn("abc");
			when(commonUserDaoImpl.getUserAndInstitutionLinkingByUserId(any()))
					.thenReturn(MockData.getUserAndInstitutionLinkingList());
			when(commonUserDaoImpl.getPassWordPolicyById(ArgumentMatchers.anyInt()))
					.thenReturn(MockData.getPasswordPolicyObject1());
			when(commonUserDaoImpl.getPasswordByUserId(ArgumentMatchers.anyInt()))
					.thenReturn(MockData.getUserPasswordObject4());
			when(applicationDateUtils.getDateDifference(any(), any())).thenReturn(1);
			when(commonUserDaoImpl.getSystemConfigDetails()).thenReturn(MockData.getSystemConfigDetailsForAuth2());
			authServiceImpl.generateAuthenticationToken(principal, userProfileDetails);
			Assert.fail();
		} catch (Exception exception) {
			exception.printStackTrace();
			assertNotNull(exception);
		}
	}

	@Test
	public void generateAuthenticationToken_errorflow21() {
		try {
			Principal user = new Principal() {
				@Override
				public String getName() {
					return "asd";
				}
			};
			User loggedInUser = getUserDetails();
			Principal principal = getPrincipal(user);
			UserProfile userProfileDetails = MockData.getUserForError4();
			when(commonUserDaoImpl.getUserBySystemUserID(anyString())).thenReturn(userProfileDetails);
			when(commonUserDaoImpl.updateUser(any())).thenReturn("abc");
			when(commonUserDaoImpl.getUserAndInstitutionLinkingByUserId(any()))
					.thenReturn(MockData.getUserAndInstitutionLinkingList());
			when(commonUserDaoImpl.getPassWordPolicyById(ArgumentMatchers.anyInt()))
					.thenReturn(MockData.getPasswordPolicyObject1());
			when(commonUserDaoImpl.getPasswordByUserId(ArgumentMatchers.anyInt()))
					.thenReturn(MockData.getUserPasswordObject4());
			when(applicationDateUtils.getDateDifference(any(), any())).thenReturn(1);
			when(commonUserDaoImpl.getSystemConfigDetails()).thenReturn(MockData.getSystemConfigDetailsForAuth2());
			authServiceImpl.generateAuthenticationToken(principal, userProfileDetails);
			Assert.fail();
		} catch (Exception exception) {
			exception.printStackTrace();
			assertNotNull(exception);
		}
	}

	@Test
	public void generateAuthenticationToken_errorflow20() {
		try {
			Principal user = new Principal() {
				@Override
				public String getName() {
					return "asd";
				}
			};
			User loggedInUser = getUserDetails();
			Principal principal = getPrincipal(user);
			UserProfile userProfileDetails = MockData.getUserForError3();
			when(commonUserDaoImpl.getUserBySystemUserID(anyString())).thenReturn(userProfileDetails);
			when(commonUserDaoImpl.updateUser(any())).thenReturn("abc");
			when(commonUserDaoImpl.getUserAndInstitutionLinkingByUserId(any()))
					.thenReturn(MockData.getUserAndInstitutionLinkingList());
			when(commonUserDaoImpl.getPassWordPolicyById(ArgumentMatchers.anyInt()))
					.thenReturn(MockData.getPasswordPolicyObject1());
			when(commonUserDaoImpl.getPasswordByUserId(ArgumentMatchers.anyInt()))
					.thenReturn(MockData.getUserPasswordObject4());
			when(applicationDateUtils.getDateDifference(any(), any())).thenReturn(1);
			when(commonUserDaoImpl.getSystemConfigDetails()).thenReturn(MockData.getSystemConfigDetailsForAuth2());
			authServiceImpl.generateAuthenticationToken(principal, userProfileDetails);
			Assert.fail();
		} catch (Exception exception) {
			exception.printStackTrace();
			assertNotNull(exception);
		}
	}

	@Test
	public void generateAuthenticationToken_errorflow19() {
		try {
			Principal user = new Principal() {
				@Override
				public String getName() {
					return "asd";
				}
			};
			User loggedInUser = getUserDetails();
			UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = userPasswordAuth(loggedInUser);
			UserProfile userProfileDetails = MockData.getUserForError6();
			when(commonUserDaoImpl.getUserBySystemUserID(anyString())).thenReturn(userProfileDetails);
			when(commonUserDaoImpl.getUserAndInstitutionLinkingByUserId(any()))
					.thenReturn(MockData.getUserAndInstitutionLinkingList2());
			when(commonUserDaoImpl.updateUser(MockDatas.getUser())).thenReturn("5ebe7c98328178b8e7e3b3a1");
			when(commonUserDaoImpl.getPassWordPolicyById(ArgumentMatchers.anyInt()))
					.thenReturn(MockData.getPasswordPolicyObject1());
			when(commonUserDaoImpl.getPasswordByUserId(ArgumentMatchers.anyInt()))
					.thenReturn(MockData.getUserPasswordObject4());
			when(applicationDateUtils.getDateDifference(any(), any())).thenReturn(1);
			when(commonUserDaoImpl.getSystemConfigDetails()).thenReturn(MockData.getSystemConfigDetailsForAuth2());
			assertNotNull(
					authServiceImpl.generateAuthenticationToken(usernamePasswordAuthenticationToken, userProfileDetails));
		} catch (Exception exception) {
			exception.printStackTrace();
			Assert.fail();
		}
	}

	/**
	 * @param loggedInUser
	 * @return
	 */
	private UsernamePasswordAuthenticationToken userPasswordAuth(User loggedInUser) {
		UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
				loggedInUser, null);
		SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
		return usernamePasswordAuthenticationToken;
	}

	@Test
	public void generateAuthenticationToken_errorflow18() {
		try {
			Principal user = new Principal() {
				@Override
				public String getName() {
					return "asd";
				}
			};
			User loggedInUser = getUserDetails();
			Principal principal = getPrincipal(user);
			UserProfile userProfileDetails = MockData.getUserForError();
			when(commonUserDaoImpl.getUserBySystemUserID(anyString())).thenReturn(userProfileDetails);
			when(commonUserDaoImpl.updateUser(any())).thenReturn("abc");
			when(commonUserDaoImpl.getUserAndInstitutionLinkingByUserId(any()))
					.thenReturn(MockData.getUserAndInstitutionLinkingList());
			when(commonUserDaoImpl.getPassWordPolicyById(ArgumentMatchers.anyInt()))
					.thenReturn(MockData.getPasswordPolicyObject1());
			when(commonUserDaoImpl.getPasswordByUserId(ArgumentMatchers.anyInt()))
					.thenReturn(MockData.getUserPasswordObject4());
			when(applicationDateUtils.getDateDifference(any(), any())).thenReturn(1);
			when(commonUserDaoImpl.getSystemConfigDetails()).thenReturn(MockData.getSystemConfigDetailsForAuth());
			authServiceImpl.generateAuthenticationToken(principal, userProfileDetails);
			Assert.fail();
		} catch (Exception exception) {
			exception.printStackTrace();
			assertNotNull(exception);
		}
	}

	@Test
	public void refreshAuthTokenTest_happy_flow() {
		try {
			when(commonUserDaoImpl.getUserByName(any())).thenReturn(MockDatas.getUserProfile());
			when(commonUserDaoImpl.getUserAndInstitutionLinkingByUserId(any()))
					.thenReturn(MockDatas.getUserAndInstitutionLinkingList());
			when(commonUserDaoImpl.getPassWordPolicyById(anyInt())).thenReturn(MockDatas.getPasswordPolicy());
			when(commonUserDaoImpl.getPasswordByUserId(ArgumentMatchers.anyInt()))
					.thenReturn(MockDatas.getUserPassword());
			when(commonUserDaoImpl.getUserByUserId(any())).thenReturn(MockData.getUserProfileDetails());
			when(commonUserDaoImpl.updateUser(MockDatas.getUser())).thenReturn("5ebe7c98328178b8e7e3b3a1");
			when(commonUserDaoImpl.getSystemConfigDetails()).thenReturn(MockData.getSystemConfigDetailsForAuth8());
			authServiceImpl.refreshAuthenticationToken(anyString());
		} catch (Exception e) {
			Assert.fail();
		}
	}

	@Test
	public void refreshAuthTokenTest_Error_flow() {
		try {
			when(commonUserDaoImpl.getUserByName(any())).thenReturn(MockDatas.getUserProfile());
			when(commonUserDaoImpl.getUserAndInstitutionLinkingByUserId(any()))
					.thenReturn(MockDatas.getUserAndInstitutionLinkingList2());
			when(commonUserDaoImpl.getPassWordPolicyById(anyInt())).thenReturn(MockDatas.getPasswordPolicy());
			when(commonUserDaoImpl.getPasswordByUserId(ArgumentMatchers.anyInt()))
					.thenReturn(MockDatas.getUserPassword());
			when(commonUserDaoImpl.getUserByUserId(any())).thenReturn(MockDatas.getUser());
			when(commonUserDaoImpl.updateUser(any())).thenReturn("abc");
			when(commonUserDaoImpl.getSystemConfigDetails()).thenReturn(MockData.getSystemConfigDetailsForAuth());
			authServiceImpl.refreshAuthenticationToken(anyString());
			Assert.fail();
		} catch (Exception e) {
			assertNotNull(e);
		}
	}

	@Test
	public void refreshAuthTokenTest_Error_flow1() {
		try {

			when(commonUserDaoImpl.getUserByName(any())).thenReturn(MockData.getUserProfile_error_flow());
			when(commonUserDaoImpl.getUserAndInstitutionLinkingByUserId(any()))
					.thenReturn(MockDatas.getUserAndInstitutionLinkingList());
			when(commonUserDaoImpl.getPassWordPolicyById(anyInt())).thenReturn(MockDatas.getPasswordPolicy());
			when(commonUserDaoImpl.getPasswordByUserId(ArgumentMatchers.anyInt()))
					.thenReturn(MockDatas.getUserPassword());
			when(commonUserDaoImpl.updateUser(any())).thenReturn("abc");
			when(commonUserDaoImpl.getSystemConfigDetails()).thenReturn(MockData.getSystemConfigDetailsForAuth());
			authServiceImpl.refreshAuthenticationToken(anyString());
			Assert.fail();
		} catch (Exception e) {
			assertNotNull(e);
		}
	}

	@Test
	public void refreshAuthTokenTest_Error_flow2() {
		try {

			when(commonUserDaoImpl.getUserByName(any())).thenReturn(MockData.getUserProfile_error_flow1());
			when(commonUserDaoImpl.getUserAndInstitutionLinkingByUserId(any()))
					.thenReturn(MockDatas.getUserAndInstitutionLinkingList());
			when(commonUserDaoImpl.getPassWordPolicyById(anyInt())).thenReturn(MockDatas.getPasswordPolicy());
			when(commonUserDaoImpl.getPasswordByUserId(ArgumentMatchers.anyInt()))
					.thenReturn(MockDatas.getUserPassword());
			when(commonUserDaoImpl.updateUser(any())).thenReturn("abc");
			when(commonUserDaoImpl.getSystemConfigDetails()).thenReturn(MockData.getSystemConfigDetailsForAuth());
			authServiceImpl.refreshAuthenticationToken(anyString());
			Assert.fail();
		} catch (Exception e) {
			assertNotNull(e);
		}
	}

	@Test
	public void refreshAuthTokenTest_Error_flow3() {
		try {
			when(commonUserDaoImpl.getUserByName(any())).thenReturn(MockData.getUserProfile4());
			when(commonUserDaoImpl.getUserAndInstitutionLinkingByUserId(any()))
					.thenReturn(MockDatas.getUserAndInstitutionLinkingList());
			when(commonUserDaoImpl.getPassWordPolicyById(anyInt())).thenReturn(MockDatas.getPasswordPolicy());
			when(commonUserDaoImpl.getPasswordByUserId(ArgumentMatchers.anyInt()))
					.thenReturn(MockDatas.getUserPassword());
			when(commonUserDaoImpl.getUserByUserId(any())).thenReturn(MockData.getUserProfile4());
			when(commonUserDaoImpl.updateUser(any())).thenReturn("abc");
			when(commonUserDaoImpl.getSystemConfigDetails()).thenReturn(MockData.getSystemConfigDetailsForAuth8());
			authServiceImpl.refreshAuthenticationToken(anyString());
			Assert.fail();
		} catch (Exception e) {
			e.printStackTrace();
			assertNotNull(e);
		}
	}

	@Test
	public void getSystemConfigName_HappyFlow() {
		try {
			when(commonUserDaoImpl.getSystemConfigName("test")).thenReturn(MockData.getSystemConfig1());
			SystemConfigVo response = authServiceImpl.getConfigName("test");
			assertNotNull(response);
		} catch (Exception e) {
			Assert.fail();
		}
	}

	/**
	 * @return
	 */
	private User getUserDetails() {
		User userDetails = new User();
		userDetails.setUsername("aaa");
		userDetails.setRoles(Arrays.asList("Role"));
		return userDetails;
	}

}
