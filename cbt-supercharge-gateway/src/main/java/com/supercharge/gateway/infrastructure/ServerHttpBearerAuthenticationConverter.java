package com.supercharge.gateway.infrastructure;

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

import com.cbt.supercharge.utils.core.ApplicationUtils;

import reactor.core.publisher.Mono;

public class ServerHttpBearerAuthenticationConverter  implements ServerAuthenticationConverter  {

	public static final String HEADERTOKEN = "X-AUTH-TOKEN";
	
	public static final String BEARER = "Bearer";

	public static final String BASIC = "Basic";
	
	public static final String BLANK = "";
	
	public static final String SOAP_URL = ".asmx";
	
	public static final int COOKIESUBSTRING = 2;
	
	public static final int SUBSTRING_AT_BASIC_TOKEN = 6;
	
	public static final int SUBSTRING_AT_BEARER_TOKEN = 7;
	
	public static final String JWT_HEADER_NAME = "jwt.cookie.header.name";
	
	private static final Predicate<String> MATCHBEARERLENGTH = authValue -> authValue.length() > SUBSTRING_AT_BEARER_TOKEN;
	
	private static final Function<String, Mono<String>> ISOLATEBEARERVALUE = authValue -> Mono
			.justOrEmpty(authValue.substring(SUBSTRING_AT_BEARER_TOKEN));
	
	private static final Logger logger = LoggerFactory.getLogger(ServerHttpBearerAuthenticationConverter.class);
	
	@Override
	public Mono<Authentication> convert(ServerWebExchange serverWebExchange) {
		String headerToken = serverWebExchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
		String xAuthToken = ApplicationUtils
				.isNotBlank(serverWebExchange.getRequest().getHeaders().getFirst(HEADERTOKEN))
						? serverWebExchange.getRequest().getHeaders().getFirst(HEADERTOKEN)
						: BLANK;
		AuthContextHolder.setAuthToken(BLANK);
		if(ApplicationUtils.isNotBlank(xAuthToken)) {
			return Mono.empty();
		}
		if (ApplicationUtils.isNotBlank(headerToken)) {
			if (!(headerToken.startsWith(BEARER))) {
				return Mono.empty();
			}
		}
		String requestUrl = serverWebExchange.getRequest().getPath().toString();
		logger.info("************* RequestUrl :-->" + requestUrl);
		try {
			if (ApplicationUtils.isBlank(AuthContextHolder.getAuthToken()) && ApplicationUtils.isBlank(headerToken)
					&& !(requestUrl.contains(SOAP_URL))){
					extractCookieToken(serverWebExchange, headerToken);					
			}
		} catch (Exception e) {
			logger.error("error while fetching cookie" + e);
		}
//		return Mono.justOrEmpty(serverWebExchange).flatMap(ServerHttpBearerAuthenticationConverter::extract)
//				.filter(MATCHBEARERLENGTH).flatMap(ISOLATEBEARERVALUE).flatMap(t -> {
//					try {
//						return jwtVerifier.check(t,TmsConstants.ACCESS_TOKEN);
//					} catch (TmsUnauthorizedException e) {
//						logger.error("Invalid Jwt token expired ");
//					}
//					return Mono.empty();
//				}).flatMap(CurrentUserAuthenticationBearer::create);
		return null;
	}
	
	private void extractCookieToken(ServerWebExchange serverWebExchange, String headerToken) {
		/* If browser open in new tab. it will skip interceptor so, for this flow take header token directly from cookies. For this case handle Header Name From 
		Environment Properties. TMS_DP For Dalko Portal and TMS_CP for Client Portal*/
		List<String> cookieString = ApplicationUtils.isBlank(headerToken)
				? (List<String>) resolveSessionIds(serverWebExchange,JWT_HEADER_NAME)
				: null;
		if (ApplicationUtils.isValidList(cookieString)) {
			AuthContextHolder.setAuthToken(BLANK);
			headerToken = (ApplicationUtils.isNotBlank(cookieString.get(0))
					&& cookieString.get(0).startsWith(BEARER))
							? cookieString.get(0)
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
	
	public static List<String> resolveSessionIds(ServerWebExchange exchange,String jwtHeaderName) {
		MultiValueMap<String, HttpCookie> cookieMap = null;
		cookieMap = ApplicationUtils.isValidateObject(exchange.getRequest().getCookies()) ? exchange.getRequest().getCookies()
				: null;
		List<HttpCookie> cookies = (ApplicationUtils.isValidateObject(cookieMap))?cookieMap.get(jwtHeaderName):null;
		if (cookies == null) {
			return Collections.emptyList();
		}
		return cookies.stream().map(HttpCookie::getValue).collect(Collectors.toList());
	}

}
