package com.supercharge.gateway.filters;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.cbt.supercharge.constants.core.ApplicationConstants;
import com.cbt.supercharge.constants.core.SecurityConstants;
import com.cbt.supercharge.constants.core.TableConstants;
import com.cbt.supercharge.exception.core.ApplicationException;
import com.cbt.supercharge.exception.core.codes.ErrorCodes;
import com.cbt.supercharge.transfer.objects.entity.UserProfile;
import com.cbt.supercharge.transfter.objects.core.holder.InstitutionUserHolder;
import com.cbt.supercharge.utils.core.ApplicationUtils;
import com.supercharge.gateway.auth.service.UserDetailsServiceImpl;
import com.supercharge.gateway.common.base.dao.CommonUserDaoImpl;
import com.supercharge.gateway.common.handlers.CustomException;
import com.supercharge.gateway.common.utils.JwtUtils;
import com.supercharge.gateway.role.service.RoleServiceImpl;
import com.supercharge.gateway.user.service.IUserService;

import reactor.core.publisher.Mono;

@Component
public class CustomWebFilter implements WebFilter {

	private static final Logger logger = LoggerFactory.getLogger(CustomWebFilter.class);

	@Autowired
	private IUserService iUserServiceImpl;

	@Autowired
	private JwtUtils jwtUtils;

	@Autowired
	private CommonUserDaoImpl commonUserDaoImpl;

	private static final String ROLE = "ROLE_";

	private static final String EMPTY = "";

	@Autowired
	private RoleServiceImpl roleServiceImpl;

	@Autowired
	private JwtUtils jwtUtil;

	@Autowired
	UserDetailsServiceImpl userDetailsServiceImpl;

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		String urlFI = exchange.getRequest().getURI().getPath().toString();
		String url = exchange.getRequest().getURI().toString();
		String token = exchange.getRequest().getHeaders().getFirst(ApplicationConstants.AUTHORIZATION);
		logger.info("My Custom Web Filter for authorization.");
		logger.info("url." + url);
		String jwtToken = null;
		String role = null;
		String username = null;
		List<String> roleList = new ArrayList<>();
		List<ConfigAttribute> attributes = new ArrayList<ConfigAttribute>();
		List<String> urlList = SecurityConstants.OTPCONFIG_URLS;
		if (SecurityConstants.OPEN_URLS.contains(urlFI) && !ApplicationUtils.isValidString(token)) {
			attributes.add(new SecurityConfig(ApplicationConstants.IS_AUTHENTICATED_FULLY));
			return chain.filter(exchange);
		} else if (urlList.contains(urlFI) && ApplicationUtils.isValidString(token)) {
			username = getUserNameFromJwtToken(token);
		} else if (ApplicationUtils.isValidString(token) && token.startsWith("Basic ")) {
			username = getUserNameFromBasicAuthToken(token);
		} else {
			username = jwtUtil.getUsernameFromToken(token);
		}
		UserProfile appUser = commonUserDaoImpl.getUserByUserId(username);
		try {
			processToken(exchange, chain, urlFI, jwtToken, role, username, roleList, urlList, appUser);
		} catch (CustomException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Clean the URL before processing further
		if (urlFI.indexOf('?') != -1) {
			urlFI = urlFI.substring(0, urlFI.indexOf('?'));
		}
		urlFI = urlFI.trim();
		logger.info("current url checking" + urlFI + " username " + username);
		try {
			if (roleServiceImpl.checkRolesForApi(roleList, urlFI, exchange, appUser)) {
				logger.info("success api role mapped" + " " + urlFI);
				attributes.add(new SecurityConfig(ApplicationConstants.IS_AUTHENTICATED_FULLY));
			} else {
				logger.error("failed to get role not mapped forbiden" + " " + urlFI);
				return Mono.error(new AccessDeniedException(ApplicationConstants.FORBIDDEN));
			}
		} catch (CustomException e) {
			logger.error("failed to get role details".concat(e.getMessage()));
		}
		if (username != null && Boolean.FALSE.equals(jwtUtils.isTokenExpired(jwtToken))) {
			return chain.filter(exchange).contextWrite(ctx -> ctx.hasKey(TableConstants.USER_PROFILE) ? ctx
					: ctx.put(TableConstants.USER_PROFILE, appUser));
		} else {
			return chain.filter(exchange);
		}
	}

	/**
	 * @param exchange
	 * @param chain
	 * @param url
	 * @param jwtToken
	 * @param role
	 * @param username
	 * @param roleList
	 * @param urlList
	 * @param appUser
	 * @throws CustomException 
	 */
	private void processToken(ServerWebExchange exchange, WebFilterChain chain, String url, String jwtToken,
			String role, String username, List<String> roleList, List<String> urlList, UserProfile appUser) throws CustomException {
		String requestTokenHeader = exchange.getRequest().getHeaders().getFirst(ApplicationConstants.AUTHORIZATION);
		if (requestTokenHeader != null && requestTokenHeader.startsWith(ApplicationConstants.BEARER)) {
			jwtToken = requestTokenHeader;
			JsonObject json = getUserNameFromJwtToken1(jwtToken);
			String claim = json.getString("sub");
			role = json.get("auth").toString();
			if (ApplicationUtils.isValidateObject(json.get("role")))
				roleList.addAll((List<String>) json.get("role"));
			if (ApplicationUtils.isValidateObject(claim)) {
				try {
					setLoggedinInstitutionId(claim);
				} catch (ApplicationException e) {
					e.printStackTrace();
				}
			}
		} else if (ApplicationUtils.isNotBlank(requestTokenHeader)
				&& requestTokenHeader.startsWith(ApplicationConstants.BASIC)) {
			String authToken = requestTokenHeader.substring(ApplicationConstants.SIX);
			try {
				populateInstituteIdHolder(authToken);
			} catch (ApplicationException e) {
				e.printStackTrace();
			}
		}

		if (role != null) {
			if (role.equals("[role-short-living]") && !urlList.contains(url)) {
				throw new CustomException(ErrorCodes.INVALID_LOGIN.toString());
			} else {
				chain.filter(exchange).doFinally(signalType -> clearSecurityContext());
			}
		} else {
			chain.filter(exchange).doFinally(signalType -> clearSecurityContext());
		}
	}

	private void populateInstituteIdHolder(String token) throws CustomException, ApplicationException {
		String userId = iUserServiceImpl.getUserIdFromBasicAuthToken(token);
		String institutionId = iUserServiceImpl.getInstituteIdentityFromBaseToken(userId);
		if (ApplicationUtils.isNotBlank(institutionId) && ApplicationUtils.isNotBlank(userId)) {
			setLoggedinInstitutionId(userId);
		}
	}

	private void setLoggedinInstitutionId(String userId) throws ApplicationException {
		List<String> institutionIdentityList = iUserServiceImpl.getMappedInstitutionsBySystemUserID(userId);
		if (ApplicationUtils.isValidList(institutionIdentityList)) {
			InstitutionUserHolder.setLoggedInInstitutionIdentity(institutionIdentityList.get(0));
			InstitutionUserHolder.setUserMappedInstitutionList(institutionIdentityList);
		}
	}

	public JsonObject getUserNameFromJwtToken1(String token) {
		if (token == null || !token.startsWith("Bearer ")) {
			throw new IllegalArgumentException("Invalid Bearer token.");
		}
		String jwtToken = token.substring(7);
		String[] parts = jwtToken.split("\\.");

		if (parts.length != 3) {
			throw new IllegalArgumentException("Invalid JWT token format.");
		}
		String payload = parts[1];
		byte[] decodedBytes = Base64.getUrlDecoder().decode(payload);
		String decodedString = new String(decodedBytes, StandardCharsets.UTF_8);
		try {
			JsonObject jsonObject = Json.createReader(new StringReader(decodedString)).readObject();
			return jsonObject; // Assuming "sub" contains the username
		} catch (Exception e) {
			throw new IllegalArgumentException("Error decoding JWT payload", e);
		}
	}

	public String getUserNameFromBasicAuthToken(String token) {
		String userName = null;
		if (ApplicationUtils.isValidString(token) && token.startsWith("Basic ")) {
			String base64Token = token.substring(6);
			String decodedString = new String(Base64.getDecoder().decode(base64Token));
			userName = decodedString.split(":")[0];
		}
		return userName;
	}

	public String getUserNameFromJwtToken(String token) {
		String userName = null;
		if (ApplicationUtils.isValidString(token) && token.startsWith("Bearer ")) {
			String jwtToken = token.substring(7);
			String[] parts = jwtToken.split("\\.");
			if (parts.length != 3) {
				throw new IllegalArgumentException("Invalid JWT token format.");
			}
			String payload = parts[1];
			byte[] decodedBytes = Base64.getUrlDecoder().decode(payload);
			String decodedString = new String(decodedBytes, StandardCharsets.UTF_8);
			try {
				JsonObject jsonObject = Json.createReader(new StringReader(decodedString)).readObject();
				userName = jsonObject.getString("sub"); // Assuming "sub" contains the username
			} catch (Exception e) {
				throw new IllegalArgumentException("Error decoding JWT payload", e);
			}
		}
		return userName;
	}

	private void clearSecurityContext() {
		InstitutionUserHolder.clearLoggedInInstitutionHolder();
		InstitutionUserHolder.clearUserInstitutionHolderList();
	}
	
}
