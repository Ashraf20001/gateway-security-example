package com.supercharge.gateway.common.handlers;

import java.nio.charset.StandardCharsets;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;

import com.cbt.supercharge.constants.core.ApplicationConstants;

import reactor.core.publisher.Mono;

@Component
@Order(-2)
public class CustomWebExceptionHandler implements WebExceptionHandler {

	@Override
	public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
		ServerHttpResponse response = exchange.getResponse();
		if (ex instanceof CustomException) {
			response.setStatusCode(HttpStatus.BAD_REQUEST); // or BAD_REQUEST based on the scenario
			response.getHeaders().add(ApplicationConstants.CONTENT_TYPE, ApplicationConstants.APPLICATION_JSON);
			String requestPath = exchange.getRequest().getPath().toString();
			String errorMessage = "{ \"" + ApplicationConstants.ERROR_KEY + "\": \""
					+ ApplicationConstants.ERROR_VALUE_ACCESS_DENIED + "\", \"" + ApplicationConstants.PATH_KEY
					+ "\": \"" + requestPath + "\", \"" + ApplicationConstants.MESSAGE_KEY + "\": \"" + ex.getMessage()
					+ "\" }";
			return response
					.writeWith(Mono.just(response.bufferFactory().wrap(errorMessage.getBytes(StandardCharsets.UTF_8))));
		}
		return Mono.error(ex);
	}
}