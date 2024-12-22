package com.supercharge.gateway.security;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.cbt.supercharge.constants.core.ApplicationConstants;
import com.cbt.supercharge.constants.core.SecurityConstants;
import com.cbt.supercharge.constants.core.TableConstants;
import com.cbt.supercharge.exception.core.ApplicationException;
import com.cbt.supercharge.exception.core.codes.ErrorCodes;
import com.cbt.supercharge.exception.core.codes.ErrorId;
import com.cbt.supercharge.exception.core.codes.ErrorId.Severity;
import com.cbt.supercharge.transfer.objects.entity.Role;
import com.cbt.supercharge.transfer.objects.entity.SystemConfig;
import com.cbt.supercharge.transfer.objects.entity.UserPassword;
import com.cbt.supercharge.transfer.objects.entity.UserProfile;
import com.cbt.supercharge.utils.core.ApplicationDateUtils;
import com.cbt.supercharge.utils.core.ApplicationUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.supercharge.gateway.auth.service.IAuthService;
import com.supercharge.gateway.common.base.dao.CommonUserDaoImpl;
import com.supercharge.gateway.common.handlers.CustomException;
import com.supercharge.gateway.common.utils.JwtUtils;
import com.supercharge.gateway.infrastructure.AuthContextHolder;
import com.supercharge.gateway.models.AuthenticationResponse;
import com.supercharge.gateway.security.filters.verificationhandler.AuthVerifyHandler;
import com.supercharge.gateway.security.filters.verificationhandler.JwtVerifyHandler;
import com.supercharge.gateway.security.filters.verificationhandler.UserVerifyHandler;
import com.supercharge.gateway.security.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.security.Keys;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationManager implements ReactiveAuthenticationManager {

	/**
	 * jwtUtil
	 */
	@Autowired
	private JwtUtils jwtUtil;

	/**
	 * commonUserDaoImpl
	 */
	@Autowired
	private CommonUserDaoImpl commonUserDaoImpl;

	/**
	 * secret
	 */
	@Value("${springbootwebfluxjjwt.jjwt.secret}")
	private String secret;

	/**
	 * ApplicationDateUtils
	 */
	@Autowired
	private ApplicationDateUtils applicationDateUtils;

	/**
	 * authServiceImpl
	 */
	@Autowired
	private IAuthService authServiceImpl;

	@Autowired
	private LdapAuthManagerBuilderProvider ldapAuthManagerBuilderProvider;

	private static final Logger logger = LoggerFactory.getLogger(AuthenticationManager.class);

	@Override
	public Mono<Authentication> authenticate(Authentication authentication) {

		if (!ApplicationUtils.isValidateObject(authentication)) {
			return Mono.error(new UsernameNotFoundException(ApplicationConstants.USER_NOT_FOUND));
		}
		/* authenticates JWT token */
		if (authentication.getPrincipal() instanceof JwtVerifyHandler.VerificationResult) {
			return jwtAuthentication(authentication);
		}
		/* authenticates basic auth token */
		if (authentication.getPrincipal() instanceof UserVerifyHandler.VerificationResult) {
			try {
				return basicAuthAuthentication(authentication);
			} catch (JsonProcessingException | ApplicationException e) {
				e.printStackTrace();
			}
		}
		/* authenticates x auth token token */
		if (authentication.getPrincipal() instanceof AuthVerifyHandler.VerificationResult) {
			return xAuthAuthentication(authentication);
		}
		if (authentication.getPrincipal() instanceof User) {
			return ldapAuthManagerBuilderProvider.authenticate(authentication);
		}
		return throwException();
	}

	private Mono<Authentication> basicAuthAuthentication(Authentication authentication)
			throws ApplicationException, JsonProcessingException {
		return Mono.deferContextual(ctx -> {
			ServerWebExchange exchange = ctx.get(ServerWebExchange.class);
			boolean isLdapNot = Boolean
					.parseBoolean(exchange.getRequest().getQueryParams().getFirst(ApplicationConstants.LDAP_NOT));

			logger.debug("Authentication initiated for path: " + exchange.getRequest().getPath());

			if (isLdapNot) {
				// Perform LDAP authentication
				return ldapAuthManagerBuilderProvider.authenticate(authentication).onErrorResume(e -> {
					logger.error("LDAP Authentication failed: ", e);
					return Mono.error(new AccessDeniedException(ApplicationConstants.BAD_CREDENTIALS));
				});
			} else {
				return basicAuth(authentication);
			}
		});
	}

	/**
	 * @param authentication
	 */
	private Mono<Authentication> basicAuth(Authentication authentication) {
		UserVerifyHandler.VerificationResult principal = null;
		principal = (UserVerifyHandler.VerificationResult) authentication.getPrincipal();
		if (ApplicationUtils.isValidateObject(principal)) {
			UserPassword userPassword = commonUserDaoImpl.getPasswordByUserIdendification(principal.userName);
			if (!ApplicationUtils.isValidateObject(userPassword)) {
				return Mono.error(new AccessDeniedException(ApplicationConstants.BAD_CREDENTIALS));
			}
			UserProfile appuser = userPassword.getUserProfile();
			if (ApplicationUtils.isValidateObject(appuser)) {
				User user = new User();
				user.setUsername(appuser.getUserIdentificationNumber());
				String password = null;
				try {
					password = ApplicationUtils.isNotBlank(principal.password) ? principal.password : "";
					user.setPassword(password);
					BCryptPasswordEncoder bcryptEncoder = new BCryptPasswordEncoder();
					if (!bcryptEncoder.matches(password, userPassword.getUmUsrPwd())) {
						authenticationFailureEventListener(principal, user);
						return Mono.error(new AccessDeniedException(ApplicationConstants.BAD_CREDENTIALS));
					}
					authenticationSuccessEventListener(principal);
				} catch (Exception e) {
					logger.error("Unable to Update the Security Logs" + e);
				}
				return buildPrincipalUser(appuser, user);

			}
		}
		return throwException();
	}

	/**
	 * @param principal
	 * @param user
	 */
	private void authenticationFailureEventListener(UserVerifyHandler.VerificationResult principal, User user) {
		try {
			UserProfile userProfile = getUserByUserName(principal.userName);
			if (user == null) {
				logger.error("User details invalid for given name : {}", userProfile);
				throw new UsernameNotFoundException(TableConstants.USER_NOT_FOUND_IN_DATABASE);
			}
			List<SystemConfig> systemConfigs = commonUserDaoImpl.getSystemConfigDetails();
			Integer expiryPeriod = null;
			Integer warningPeriod = null;
			Integer failureAttempts = null;
			String autoRelease = null;
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
				if (ApplicationConstants.ACCOUNT_LOCKOUT.equals(systemConfig.getPropertyGroup())
						&& ApplicationConstants.UN_BLOCKED_MINUTE.equals(systemConfig.getConfigName())
						&& Boolean.TRUE.equals(systemConfig.getIsChecked())) {
					autoRelease = systemConfig.getConfigValue();
				}
			}
			LocalDateTime userBlockedDate = userProfile.getUserBlockedDate();
			if (ApplicationUtils.isValidateObject(userBlockedDate)) {
				LocalDateTime autoReleaseMins = userBlockedDate.plusMinutes(Integer.parseInt(autoRelease));
				if (LocalDateTime.now().isAfter(autoReleaseMins)
						&& Boolean.TRUE.equals(userProfile.getUserAccountStatus())) {
					userProfile.setUserAccountStatus(false);
					userProfile.setFailedCount(ApplicationConstants.ZERO);
					logger.info("update user.................................");
				}
			}
			Integer totalCount = userProfile.getFailedCount() + 1;
			if (Boolean.FALSE.equals(userProfile.getUserAccountStatus())) {
				userProfile.setModifiedBy(userProfile.getUserId());
				userProfile.setModifiedDate(new Date());
				userProfile.setFailedCount(totalCount);
				commonUserDaoImpl.updateUser(userProfile);
			}
			authServiceImpl.isUserAuthenticated(userProfile, new AuthenticationResponse(), expiryPeriod, warningPeriod,
					failureAttempts, autoRelease);
		} catch (ApplicationException e) {
			logger.error("failed to update user");
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param principal
	 * @throws JsonProcessingException
	 * @throws ApplicationException
	 */
	private void authenticationSuccessEventListener(UserVerifyHandler.VerificationResult principal)
			throws JsonProcessingException, ApplicationException {
		try {
			UserProfile user = getUserByUserName(principal.userName);
			if (user == null) {
				logger.error("User details invalid for given name : {}", user);
				return;
			}
			if (ApplicationUtils.isValidateObject(user.getUserBlockedDate())) {
				LocalDateTime userBlockedDate = user.getUserBlockedDate();
				Date date = Date.from(userBlockedDate.atZone(ZoneId.systemDefault()).toInstant());
				SystemConfig unBlockMins = commonUserDaoImpl
						.getSystemConfigName(ApplicationConstants.UN_BLOCKED_MINUTE);
				String seconds = Long
						.toString(Long.parseLong(unBlockMins.getConfigValue()) * ApplicationConstants.SIXTY);
				if (Boolean.TRUE.equals(user.getUserAccountStatus())
						&& Boolean.TRUE.equals(autoUnblockNow(date, seconds))) {
					user.setModifiedBy(user.getUserId());
					user.setModifiedDate(new Date());
					user.setFailedCount(ApplicationConstants.ZERO);
					user.setUserAccountStatus(false);
					commonUserDaoImpl.updateUser(user);
				}
			}
		} catch (CustomException e) {
			logger.error("failed to update user");
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param userName
	 * @return
	 */
	private UserProfile getUserByUserName(String userName) {
		return commonUserDaoImpl.getUserBySystemUserID(userName);
	}

	private Mono<Authentication> throwException() {
		logger.error(ErrorCodes.INVALID_USER.getErrorMessage());
		return Mono.error(new ApplicationException(new ErrorId(ErrorCodes.INVALID_USER.getErrorCode(),
				ErrorCodes.INVALID_USER.getErrorMessage(), Severity.FATAL), HttpStatus.UNAUTHORIZED));
	}

	private Mono<Authentication> xAuthAuthentication(Authentication authentication) {
		AuthVerifyHandler.VerificationResult principal = null;
		UserProfile appuser = null;
		try {
			principal = (AuthVerifyHandler.VerificationResult) authentication.getPrincipal();
			appuser = commonUserDaoImpl.getUserSecurityKeyByUserName(principal.userSecurityToken.getUsername());
		} catch (Exception e) {
			return throwException();
		}
		if (ApplicationUtils.isValidateObject(appuser)) {
			User user = new User();
			user.setUsername(appuser.getUserName());
			return buildPrincipalUser(appuser, user);
		}
		return throwException();
	}

	private Mono<Authentication> buildPrincipalUser(UserProfile appUser, User user) {
		List<String> rolelist = commonUserDaoImpl.getRoleByUser(appUser).stream().map(Role::getUserRoleName).toList();
		if (ApplicationUtils.isValidList(rolelist)) {
			user.setId(appUser.getUserId());
			user.setRoles(rolelist);
			List<SimpleGrantedAuthority> authorities = null;
			authorities = rolelist.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
			UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
					user, null, authorities);
			AuthContextHolder.setSecurityHolder(usernamePasswordAuthenticationToken);
			SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
			Authentication isAuth = SecurityContextHolder.getContext().getAuthentication();
//			System.out.println(isAuth.getPrincipal());
			return Mono.justOrEmpty(usernamePasswordAuthenticationToken);
		}
		return throwException();
	}

	@SuppressWarnings("unchecked")
	private Mono<Authentication> jwtAuthentication(Authentication authentication) {
		try {
			User user = new User();
			JwtVerifyHandler.VerificationResult auth = (JwtVerifyHandler.VerificationResult) authentication
					.getPrincipal();
			String token = ApplicationUtils.isNotBlank(auth.token) ? auth.token : SecurityConstants.BLANK;
			Claims claims = getAllClaimsFromToken(token);
			String userName = jwtUtil.getUsernameFromToken(token);
			List<String> roles = (List<String>) claims.get(TableConstants.ROLE);
			List<SimpleGrantedAuthority> authorities = null;
			authorities = roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
			user.setUsername(userName);
			user.setRoles(roles);
			UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
					user, null, authorities);
			SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
			return Mono.justOrEmpty(usernamePasswordAuthenticationToken);
		} catch (Exception ex) {
			return throwException();
		}
	}

	public Claims getAllClaimsFromToken(String token) throws ApplicationException, DecodingException {
		Claims claims = null;
		try {
			claims = Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(secret.getBytes())).build()
					.parseClaimsJws(token).getBody();
		} catch (DecodingException ex) {
			throw new DecodingException(ErrorCodes.INVALID_TOKEN.getErrorMessage());
		} catch (MalformedJwtException e) {
			throw new DecodingException(ErrorCodes.INVALID_TOKEN.getErrorMessage());
		} catch (Exception ex) {
			throwUnauthorizedException(token);
		}
		return claims;
	}

	private void throwUnauthorizedException(String accessToken) throws ApplicationException {
		logger.error("Jwt token expired ");
		throw new ApplicationException(new ErrorId(ErrorCodes.INVALID_USER.getErrorCode(),
				ErrorCodes.INVALID_USER.getErrorMessage(), Severity.FATAL), HttpStatus.UNAUTHORIZED);
	}

	private Boolean autoUnblockNow(Date createdDate, String expiryTime) {
		Date expiryDate = applicationDateUtils.createResendOtpDateFromConfigValue(createdDate, expiryTime);
		boolean autoUnblockNow = false;
		if (!applicationDateUtils.isExpired(expiryDate)) {
			// do not unblock if it the expiry time is not over
			autoUnblockNow = false;
		} else {
			autoUnblockNow = true;
		}

		return autoUnblockNow;
	}

}
