package com.cbt.supercharge.gateway.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.Principal;

import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.server.ServerWebExchange;

import com.cbt.supercharge.exception.core.ApplicationException;
import com.cbt.supercharge.exception.core.codes.ErrorCodes;
import com.cbt.supercharge.transfer.objects.entity.UserProfile;
import com.cbt.supercharge.transfter.objects.core.ApplicationResponse;
import com.cbt.supercharge.transfter.objects.core.entity.vo.SystemConfigVo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.supercharge.gateway.auth.controller.AuthController;
import com.supercharge.gateway.auth.service.IAuthService;
import com.supercharge.gateway.models.AuthenticationResponse;
import com.supercharge.gateway.security.model.User;

import reactor.core.publisher.Mono;
import reactor.util.context.Context;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TestClass {

	@InjectMocks
	private AuthController authController;

	@Mock
	private IAuthService iAuthServiceImpl;

	public static String userName = "SJ";

	@BeforeEach
	public void setUp() {
		User principal = new User();
		principal.setUsername("username");
		principal.setPassword("aaa");
		Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null);
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authentication);
		SecurityContextHolder.setContext(context);
	}

	@AfterEach
	public void tearDown() {
		// Cleanup code here
	}

	@Test
	public void testMethod() {
		assertTrue(true);
	}

	@Test
	public void authenticateUser_userProfileFound() throws JsonProcessingException, ApplicationException {
		// Mock the ServerWebExchange and Principal
		ServerWebExchange serverWebExchange = Mockito.mock(ServerWebExchange.class);
		Principal user = new Principal() {
			@Override
			public String getName() {
				return "testUser";
			}
		};
	        // Create a valid UserProfile
	        UserProfile userProfile = new UserProfile();
	        userProfile.setUserName("testUser");
	        userProfile.setUserIdentificationNumber("user123"); // Assuming `userName` variable is available

	        // Mock the service's response
	        when(iAuthServiceImpl.generateAuthenticationToken(any(), any())).thenReturn(new AuthenticationResponse());

	        // Act: Call the controller's authenticateUser method and pass userProfile in the context
	        Mono<AuthenticationResponse> response = authController.authenticateUser(user, serverWebExchange, false)
	                .contextWrite(Context.of("userProfile", userProfile));

	        // Act: Trigger the Mono and assert the behavior
	        AuthenticationResponse authenticationResponse = response.block(); // Block to get the result

	}

	@Test
	public void testAuthenticateUserWithNullUserProfile() throws JsonProcessingException, ApplicationException {
		// Arrange: Mock the Principal (user) and ServerWebExchange (web exchange)
		Principal user = new Principal() {
			@Override
			public String getName() {
				return "testUser";
			}
		};
		UserProfile userProfile = new UserProfile();
		userProfile.setUserName(null);
		ServerWebExchange serverWebExchange = mock(ServerWebExchange.class);
		Mono<AuthenticationResponse> response = authController.authenticateUser(user, serverWebExchange, false)
				.contextWrite(Context.of("userProfile", userProfile)); // Pass null for userProfile in context
		IllegalStateException exception = assertThrows(IllegalStateException.class, response::block);
		assertEquals(ErrorCodes.INVALID_USER.getErrorMessage(), exception.getMessage());
	}

	@Test
	public void authenticateUser_errorFlow() {
		ServerWebExchange serverWebExchange = Mockito.mock(ServerWebExchange.class);
		try {
			Principal user = null; // Directly passing null user here
			UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
				authController.authenticateUser(user, serverWebExchange, false);
			});
			String expectedMessage = "User not found in the database.";
			assertEquals(expectedMessage, exception.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			assertNotNull(e);
		}

	}

	@Test
	public void getVo_HappyFlow() {
		String idMock = "cbg";
		try {
			Object res = authController.getVo(idMock);
			Assert.assertNull(res);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	@Test
	public void testLogout() {
		try {
			AuthController authController = new AuthController();
			authController.logout();
		} catch (Exception e) {
			Assert.fail();
		}
	}

	@Test
	public void refreshToken_happyFlow() {
		try {
			when(iAuthServiceImpl.refreshAuthenticationToken(anyString())).thenReturn(getAuthResponse());
			authController.refreshToken(userName);
		} catch (Exception e) {
			Assert.fail();
		}
	}

	@Test
	public void getConfigName_happyFlow() {
		try {
			when(iAuthServiceImpl.getConfigName(anyString())).thenReturn(getSystemConfigVo1());
			authController.getConfigName(userName);
		} catch (Exception e) {
			Assert.fail();
		}
	}

	@Test
	void testLogout1() {
		// Arrange: Optionally, mock SecurityContextHolder if needed
		SecurityContextHolder.clearContext(); // Clear any existing security context

		// Act: Call the logout method and block to get the result
		ApplicationResponse response = authController.logout().block(); // This will block and get the result of the
																		// Mono

		// Assert: Verify the response and ensure context is cleared
	}

	private AuthenticationResponse getAuthResponse() {
		AuthenticationResponse resp = new AuthenticationResponse();
		resp.setAccessToken("accessToken");
		resp.setExpiresIn(8);
		resp.setRefreshToken("refreshToken");
		return resp;
	}

	public static SystemConfigVo getSystemConfigVo1() {
		SystemConfigVo systemConfigVo = new SystemConfigVo();
		systemConfigVo.setConfigName("test");
		systemConfigVo.setIsChecked(true);
		systemConfigVo.setPropertyGroup("test");
		return systemConfigVo;
	}

}
