package com.supercharge.gateway.security.filters;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;

import com.cbt.supercharge.constants.core.ApplicationConstants;
import com.cbt.supercharge.constants.core.SecurityConstants;
import com.cbt.supercharge.exception.core.ApplicationException;
import com.cbt.supercharge.utils.core.ApplicationUtils;
import com.supercharge.gateway.common.base.dao.GatewayEnvironmentProperties;
import com.supercharge.gateway.infrastructure.AuthContextHolder;
import com.supercharge.gateway.security.filters.verificationhandler.JwtVerifyHandler;

import reactor.core.publisher.Mono;

public class ServerHttpBearerAuthenticationConverter implements ServerAuthenticationConverter {
	
	private static final Predicate<String> MATCHBEARERLENGTH = authValue -> authValue.length() > ApplicationConstants.SUBSTRING_AT_BEARER_TOKEN;
	
	private static final Function<String, Mono<String>> ISOLATEBEARERVALUE = authValue -> Mono
			.justOrEmpty(authValue.substring(ApplicationConstants.SUBSTRING_AT_BEARER_TOKEN));
	
	private final JwtVerifyHandler jwtVerifier;
	
	private final GatewayEnvironmentProperties environmentProperties;
	
	private static final Logger logger = LoggerFactory.getLogger(ServerHttpBearerAuthenticationConverter.class);
	
	public static final int COOKIESUBSTRING = 2;

	public ServerHttpBearerAuthenticationConverter(JwtVerifyHandler jwtVerifier, GatewayEnvironmentProperties environmentProperties) {
		this.jwtVerifier = jwtVerifier;
		this.environmentProperties = environmentProperties;
	}

	@Override
	public Mono<Authentication> convert(ServerWebExchange serverWebExchange) {
		String headerToken = serverWebExchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
		String xAuthToken = ApplicationUtils
				.isNotBlank(serverWebExchange.getRequest().getHeaders().getFirst(ApplicationConstants.HEADERTOKEN))
						? serverWebExchange.getRequest().getHeaders().getFirst(ApplicationConstants.HEADERTOKEN)
						: SecurityConstants.BLANK;
		AuthContextHolder.setAuthToken(SecurityConstants.BLANK);
		if(ApplicationUtils.isNotBlank(xAuthToken)) {
			return Mono.empty();
		}
		if (ApplicationUtils.isNotBlank(headerToken)) {
			if (!(headerToken.startsWith(ApplicationConstants.BEARER))) {
				return Mono.empty();
			}
		}
		String requestUrl = serverWebExchange.getRequest().getPath().toString();
		logger.info("************* RequestUrl :-->" + requestUrl);
		try {
			if (ApplicationUtils.isBlank(AuthContextHolder.getAuthToken()) && ApplicationUtils.isBlank(headerToken)
					&& !(requestUrl.contains(ApplicationConstants.SOAP_URL))){
					extractCookieToken(serverWebExchange, headerToken);					
			}
		} catch (Exception e) {
			logger.error("error while fetching cookie" + e);
		}
		return Mono.justOrEmpty(serverWebExchange).flatMap(ServerHttpBearerAuthenticationConverter::extract)
				.filter(MATCHBEARERLENGTH).flatMap(ISOLATEBEARERVALUE).flatMap(t -> {
					try {
						return jwtVerifier.check(t,ApplicationConstants.ACCESS_TOKEN);
					} catch (ApplicationException e) {
						logger.error("Invalid Jwt token expired ");
					}
					return Mono.empty();
				}).flatMap(CurrentUserAuthenticationBearer::create);
	}

	private void extractCookieToken(ServerWebExchange serverWebExchange, String headerToken) {
		/* If browser open in new tab. it will skip interceptor so, for this flow take header token directly from cookies. For this case handle Header Name From 
		Environment Properties. TMS_DP For Dalko Portal and TMS_CP for Client Portal*/
		List<String> cookieString = ApplicationUtils.isBlank(headerToken)
				? (List<String>) resolveSessionIds(serverWebExchange,environmentProperties.getJwtHeaderName())
				: null;
		if (ApplicationUtils.isValidList(cookieString)) {
			AuthContextHolder.setAuthToken(SecurityConstants.BLANK);
			headerToken = (ApplicationUtils.isNotBlank(cookieString.get(ApplicationConstants.ZERO))
					&& cookieString.get(ApplicationConstants.ZERO).startsWith(ApplicationConstants.BEARER))
							? cookieString.get(ApplicationConstants.ZERO)
							: "";
			AuthContextHolder.setAuthToken(AuthContextHolder.getAuthToken()+headerToken);
			logger.info("************* token fetched from cookie successfully ******");
		}
	}

	public static Mono<String> extract(ServerWebExchange serverWebExchange) {
		String token = ApplicationUtils.isNotBlank(AuthContextHolder.getAuthToken())
				? AuthContextHolder.getAuthToken().substring(COOKIESUBSTRING)
				: serverWebExchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
		return Mono.justOrEmpty(token);
	}
	
	public static List<String> resolveSessionIds(ServerWebExchange exchange, String jwtHeaderName) {
		MultiValueMap<String, HttpCookie> cookieMap = null;
		cookieMap = ApplicationUtils.isValidateObject(exchange.getRequest().getCookies())
				? exchange.getRequest().getCookies() : null;
		List<HttpCookie> cookies = (ApplicationUtils.isValidateObject(cookieMap)) ? cookieMap.get(jwtHeaderName) : null;
		if (cookies == null) {
			return Collections.emptyList();
		}
		return cookies.stream().map(HttpCookie::getValue).collect(Collectors.toList());
	}


}
