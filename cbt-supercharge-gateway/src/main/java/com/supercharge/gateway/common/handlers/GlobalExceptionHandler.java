package com.supercharge.gateway.common.handlers;

import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.web.server.ServerWebExchange;

import com.cbt.supercharge.constants.core.ApplicationConstants;
import com.cbt.supercharge.constants.core.TableConstants;

import reactor.core.publisher.Mono;

@Configuration
public class GlobalExceptionHandler implements ErrorWebExceptionHandler, ServerAccessDeniedHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@Override
	public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
		String isLdapParam = exchange.getRequest().getQueryParams().getFirst(ApplicationConstants.LDAP_NOT);
		Boolean isLdap = Boolean.parseBoolean(isLdapParam);
		ServerHttpResponse response = exchange.getResponse();
		if (ex instanceof AccessDeniedException) {
			if (ex.getMessage().equals(ApplicationConstants.BAD_CREDENTIALS)) {
				if (Boolean.TRUE.equals(isLdap)) {
					response.setStatusCode(HttpStatus.UNAUTHORIZED); // Set 401 Unauthorized
					return response.writeWith(
							Mono.just(response.bufferFactory().wrap(ApplicationConstants.USER_NOT_FOUND.getBytes())));
				} else {
					response.setStatusCode(HttpStatus.UNAUTHORIZED); // Set 401 Unauthorized
					return response.writeWith(Mono
							.just(response.bufferFactory().wrap(ApplicationConstants.INVALIDE_PASSWORD.getBytes())));
				}
			} else {
				// Handle for other cases
				if (Boolean.TRUE.equals(isLdap)) {
					response.setStatusCode(HttpStatus.UNAUTHORIZED); // Set 401 Unauthorized
					return response.writeWith(
							Mono.just(response.bufferFactory().wrap(ApplicationConstants.USER_NOT_FOUND.getBytes())));
				} else {
					response.setStatusCode(HttpStatus.UNAUTHORIZED); // Set 403 Forbidden
					return response.writeWith(Mono.just(response.bufferFactory().wrap(ex.getMessage().getBytes())));
				}
			}
		}
		return Mono.error(ex); // Propagate the error if necessary
	}

	@Override
	public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException denied) {
		String requestPath = exchange.getRequest().getPath().toString();
		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(HttpStatus.FORBIDDEN);
		response.getHeaders().add(ApplicationConstants.CONTENT_TYPE, ApplicationConstants.APPLICATION_JSON);
		String errorMessage = "{ \"" + ApplicationConstants.ERROR_KEY + "\": \""
				+ ApplicationConstants.ERROR_VALUE_ACCESS_DENIED + "\", \"" + ApplicationConstants.PATH_KEY + "\": \""
				+ requestPath + "\", \"" + ApplicationConstants.MESSAGE_KEY + "\": \"" + denied.getMessage() + "\" }";
		return response
				.writeWith(Mono.just(response.bufferFactory().wrap(errorMessage.getBytes(StandardCharsets.UTF_8))));
	}
}
