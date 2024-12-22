/*
 * @author codeboard
 */
package com.supercharge.gateway.auth.controller;

import java.security.Principal;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import com.cbt.supercharge.constants.core.TableConstants;
import com.cbt.supercharge.exception.core.ApplicationException;
import com.cbt.supercharge.exception.core.codes.ErrorCodes;
import com.cbt.supercharge.transfer.objects.entity.UserProfile;
import com.cbt.supercharge.transfter.objects.core.ApplicationResponse;
import com.cbt.supercharge.utils.core.ApplicationUtils;
import com.cbt.supercharge.versioning.core.VersionMonitor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.supercharge.gateway.auth.service.IAuthService;
import com.supercharge.gateway.common.base.controller.GatewayBaseController;
import com.supercharge.gateway.common.handlers.CustomException;
import com.supercharge.gateway.models.AuthenticationResponse;

import reactor.core.publisher.Mono;
import reactor.util.context.Context;

/**
 * The Class AuthController.
 */
@RestController
@VersionMonitor
public class AuthController extends GatewayBaseController {

	/**
	 * The auth service impl.
	 */
	@Autowired
	private IAuthService iAuthServiceImpl;
	
	/**
	 * User.
	 *
	 * @param user           the user
	 * @param servletRequest the servlet request
	 * @return the application response
	 * @throws ApplicationException
	 * @throws JsonProcessingException 
	 */
	@RequestMapping(value = "/auth")
	public Mono<AuthenticationResponse> authenticateUser(Principal user, ServerWebExchange serverWebExchange,
			@RequestParam(required = false) boolean isldap) throws ApplicationException, JsonProcessingException {

		if (!ApplicationUtils.isValidateObject(user)) {
			throw new UsernameNotFoundException("User not found in the database.");
		}
		return Mono.deferContextual(ctx -> {
			UserProfile userProfile = (UserProfile) ctx.get(TableConstants.USER_PROFILE);
			if (!ApplicationUtils.isValidateObject(userProfile.getUserIdentificationNumber())) {
				return Mono.error(new IllegalStateException(ErrorCodes.INVALID_USER.getErrorMessage()));
			}
			AuthenticationResponse authResponse = null;
			try {
					authResponse = iAuthServiceImpl.generateAuthenticationToken(user, userProfile);
			} catch (JsonProcessingException | ApplicationException e) {
				e.printStackTrace();
			}
			return Mono.just(authResponse);
		});
	}


	/**
	 * Gets the class name.
	 *
	 * @return the class name
	 */
	@Override
	protected Class<?> getClassName() {
		return this.getClass();
	}

	/**
	 * Gets the vo.
	 *
	 * @param identity the identity
	 * @return the vo
	 * @throws ApplicationException the application exception
	 */
	@Override
	public Object getVo(String identity) throws ApplicationException {
		return null;
	}

	/**
	 * Logout.
	 *
	 * @return the application response
	 */
	@GetMapping(value = "/auth/logout")
	public Mono<ApplicationResponse> logout() {
//		AppUserHolder.clearLoggedInAppUserHolder();
		SecurityContextHolder.clearContext();
		SecurityContextHolder.getContext().setAuthentication(null);
		return Mono.deferContextual(ctx -> {
			return Mono.just(ctx).doOnTerminate(() -> {
				logger.info("UserProfile cleared from context.");
			}).contextWrite(Context.empty()) // Clears all context data
					.then(Mono.just(getDefaultApplicationResponse())); // Return the response after clearing context
		});
	}

	/**
	 * Refresh token.
	 *
	 * @param refreshToken the refresh token
	 * @return the application response
	 * @throws JsonProcessingException 
	 * @throws CustomException 
	 * @throws Exception the exception
	 */

	@GetMapping("/auth/refreshToken")
	public ApplicationResponse refreshToken(@RequestParam("refreshToken") String refreshToken)
			throws ApplicationException, JsonProcessingException, CustomException {
		return getApplicationResponse(iAuthServiceImpl.refreshAuthenticationToken(refreshToken));
	}
	
	/**
	 * @param propertyName
	 * @return
	 */
	@GetMapping("/configuration/get-config-name")
	public ApplicationResponse getConfigName(@RequestParam("ConfigName") String configName) {
		return getApplicationResponse(iAuthServiceImpl.getConfigName(configName));
	}
	
	@GetMapping("/clear-cache")
	public String clearCache() throws CustomException {
		return iAuthServiceImpl.clearCache();
	}

	/**
	 * Register interceptor.
	 */
	@Override
	protected void registerInterceptor() {
		//
	}

}
