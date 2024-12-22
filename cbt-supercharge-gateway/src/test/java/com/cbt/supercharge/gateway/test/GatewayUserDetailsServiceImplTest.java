package com.cbt.supercharge.gateway.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.cbt.supercharge.constants.core.ApplicationConstants;
import com.cbt.supercharge.exception.core.ApplicationException;
import com.cbt.supercharge.transfer.objects.entity.UserProfile;
import com.cbt.supercharge.transfer.objects.mock.MockDatas;
import com.supercharge.gateway.auth.service.UserDetailsServiceImpl;
import com.supercharge.gateway.common.base.dao.CommonUserDaoImpl;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class GatewayUserDetailsServiceImplTest {

	@InjectMocks
	private UserDetailsServiceImpl userDetailsServiceImpl;

	@Mock
	private CommonUserDaoImpl commonUserDaoImpl;

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		userDetailsServiceImpl.setCommonUserDaoImpl(commonUserDaoImpl);
	}

	@Test
	public void loadUserByUsername_FailFlow() {
		UserProfile userProfile = MockDatas.getUserForFail();
		String userId = ApplicationConstants.TEST;
		try {
//			when(userDaoMock.getUserByName(userId)).thenReturn(userProfile);
			when(commonUserDaoImpl.getUserBySystemUserID(userId)).thenReturn(userProfile);
			when(commonUserDaoImpl.getPasswordByUserId(1)).thenReturn(MockData.getUserPassword());
			userDetailsServiceImpl.loadUserByUsername(userId);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void loadUserByUsername_FailFlowTwo() {
		UserProfile userProfile = MockDatas.getUserForFailTwo();
		String userId = ApplicationConstants.TEST;
		try {
//			when(userDaoMock.getUserByName(userId)).thenReturn(userProfile);
			when(commonUserDaoImpl.getUserBySystemUserID(userId)).thenReturn(userProfile);
			when(commonUserDaoImpl.getPasswordByUserId(1)).thenReturn(MockData.getUserPassword());
			userDetailsServiceImpl.loadUserByUsername(userId);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void loadUserByUsername_HappyFlow() {
		UserProfile userProfile = MockDatas.getUser5();
		String userId = ApplicationConstants.TEST;
		try {
			when(commonUserDaoImpl.getUserBySystemUserID(userId)).thenReturn(userProfile);
			when(commonUserDaoImpl.getPasswordByUserId(1)).thenReturn(MockData.getUserPassword());
			UserDetails user = userDetailsServiceImpl.loadUserByUsername(userId);
			Assert.assertEquals(userProfile.getUserName(), user.getUsername());
		} catch (Exception e) {
			System.out.println(e);
			Assert.fail();
		}
	}

	@Test
	public void loadUserByUsername_exception() {
		UserProfile userProfile = MockDatas.getUser3();
		String userId = ApplicationConstants.TEST;
		try {
//			when(userDaoMock.getUserByName(anyString())).thenReturn(userProfile);
			when(commonUserDaoImpl.getUserBySystemUserID(userId)).thenReturn(userProfile);
			when(commonUserDaoImpl.getPasswordByUserId(1)).thenReturn(MockData.getUserPassword());
			UsernameNotFoundException ap = new UsernameNotFoundException("User not found in the database.");
			UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
				userDetailsServiceImpl.loadUserByUsername(null);
			});
			assertEquals(ap.toString(), exception.toString());
		} catch (Exception e) {
			System.out.println(e);
			Assert.fail();
		}
	}

	@Test
	public void Password_exception4() {
		UserProfile userProfile = MockDatas.getUser4();
		String userId = "test";
		try {
//			when(userDaoMock.getUserByName(anyString())).thenReturn(userProfile);
			when(commonUserDaoImpl.getUserBySystemUserID(userId)).thenReturn(userProfile);
			doThrow(new ApplicationException()).when(commonUserDaoImpl).getPasswordByUserId(1);
			userDetailsServiceImpl.loadUserByUsername(userId);
			Assert.fail();
		} catch (Exception e) {
			e.printStackTrace();
			assertNotNull(e);
		}
	}

	@Test
	public void Password_exception1() {
		UserProfile userProfile = MockDatas.getUser4();
		String userId = ApplicationConstants.TEST;
		try {
			when(commonUserDaoImpl.getUserBySystemUserID(userId)).thenReturn(userProfile);
			when(commonUserDaoImpl.getPasswordByUserId(1)).thenReturn(null);
			userDetailsServiceImpl.loadUserByUsername(userId);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void Password_exception2() {
		UserProfile userProfile = MockDatas.getUser4();
		String userId = ApplicationConstants.TEST;
		try {
			when(commonUserDaoImpl.getUserBySystemUserID(userId)).thenReturn(userProfile);
			when(commonUserDaoImpl.getPasswordByUserId(1)).thenThrow(new RuntimeException("Database error"));
			userDetailsServiceImpl.loadUserByUsername(userId);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
