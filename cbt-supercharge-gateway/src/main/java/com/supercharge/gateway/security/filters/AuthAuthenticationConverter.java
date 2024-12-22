package com.supercharge.gateway.security.filters;

import java.util.function.Function;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.server.ServerWebExchange;

import com.cbt.supercharge.constants.core.ApplicationConstants;
import com.cbt.supercharge.constants.core.SecurityConstants;
import com.cbt.supercharge.exception.core.ApplicationException;
import com.cbt.supercharge.utils.core.ApplicationUtils;
import com.supercharge.gateway.security.filters.verificationhandler.AuthVerifyHandler;

import reactor.core.publisher.Mono;

public class AuthAuthenticationConverter implements ServerAuthenticationConverter {

	private final AuthVerifyHandler authVerifyHandler;

	public AuthAuthenticationConverter(AuthVerifyHandler authVerifyHandler) {
		this.authVerifyHandler = authVerifyHandler;
	}

	private static final Function<String, Mono<String>> ISOLATEAUTHVALUE = authValue -> Mono
			.justOrEmpty(authValue.substring(ApplicationConstants.ZERO));

	private static final Predicate<String> MATCHAUTHLENGTH = authValue -> authValue
			.length() > ApplicationConstants.ZERO;

	private static final Logger logger = LoggerFactory.getLogger(AuthAuthenticationConverter.class);

	@Override
	public Mono<Authentication> convert(ServerWebExchange serverWebExchange) {
		String headerToken = ApplicationUtils
				.isNotBlank(serverWebExchange.getRequest().getHeaders().getFirst(ApplicationConstants.HEADERTOKEN))
						? serverWebExchange.getRequest().getHeaders().getFirst(ApplicationConstants.HEADERTOKEN)
						: SecurityConstants.BLANK;
		if (ApplicationUtils.isBlank(headerToken)) {
			return Mono.empty();
		}
		logger.info("************* RequestUrl :-->" + serverWebExchange.getRequest().getPath().toString());
		return Mono.justOrEmpty(serverWebExchange).flatMap(AuthAuthenticationConverter::extract).filter(MATCHAUTHLENGTH)
				.flatMap(ISOLATEAUTHVALUE).flatMap(t -> {
					try {
						return authVerifyHandler.check(t);
					} catch (ApplicationException e) {
						logger.error("Invalid X-auth token");
					}
					return Mono.empty();
				}).flatMap(CurrentUserAuthenticationAuth::create);
	}

	public static Mono<String> extract(ServerWebExchange serverWebExchange) {
		return Mono.justOrEmpty(serverWebExchange.getRequest().getHeaders().getFirst(ApplicationConstants.HEADERTOKEN));
	}

}
