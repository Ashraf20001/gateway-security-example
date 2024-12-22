package com.supercharge.gateway.security.filters;

import java.util.function.Function;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.server.ServerWebExchange;

import com.cbt.supercharge.constants.core.ApplicationConstants;
import com.cbt.supercharge.exception.core.ApplicationException;
import com.cbt.supercharge.utils.core.ApplicationUtils;
import com.supercharge.gateway.security.filters.verificationhandler.UserVerifyHandler;

import reactor.core.publisher.Mono;

public class UserAuthentication implements  ServerAuthenticationConverter {
	
    private static final Predicate<String> MATCHBASICLENGTH = authValue -> authValue.length() > ApplicationConstants.SUBSTRING_AT_BASIC_TOKEN;
    
    private static final Function<String, Mono<String>> ISOLATEBASICVALUE = authValue -> Mono.justOrEmpty(authValue.substring(ApplicationConstants.SUBSTRING_AT_BASIC_TOKEN));
    private final UserVerifyHandler userVerifier;
    
    private static final Logger logger = LoggerFactory.getLogger(UserAuthentication.class);

    public UserAuthentication( UserVerifyHandler userVerifier) {
        this.userVerifier = userVerifier;
    }

    public static Mono<String> extract(ServerWebExchange serverWebExchange) {
        return Mono.justOrEmpty(serverWebExchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION));
    }

	@Override
	public Mono<Authentication> convert(ServerWebExchange exchange) {
		String headerToken = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
		String authToken = ApplicationUtils
				.isNotBlank(exchange.getRequest().getHeaders().getFirst(ApplicationConstants.HEADERTOKEN))
						? exchange.getRequest().getHeaders().getFirst(ApplicationConstants.HEADERTOKEN)
						: ApplicationConstants.BLANK;
		if(ApplicationUtils.isNotBlank(authToken)) {
			return Mono.empty();
		}
    	if(ApplicationUtils.isNotBlank(headerToken)) {
    		if(!(headerToken.startsWith(ApplicationConstants.BASIC))) {
    			return Mono.empty();
    		}
    	}
    	logger.info("************* RequestUrl :-->"+exchange.getRequest().getPath().toString());
        return Mono.justOrEmpty(exchange)
                .flatMap(UserAuthentication::extract)
                .filter(MATCHBASICLENGTH)
                .flatMap(ISOLATEBASICVALUE)
                .flatMap(t -> {
					try {
						return userVerifier.check(t);
					} catch (ApplicationException e) {
						logger.error("Invalid User : "+e);
					}
					return Mono.empty();
				})
                .flatMap(CurrentUserAuthenticationBasic::create);

	}
}
