package com.supercharge.gateway.filters;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.cbt.supercharge.constants.core.ApplicationConstants;
import com.cbt.supercharge.constants.core.SecurityConstants;

import reactor.core.publisher.Mono;

@Configuration
public class WebConfigContext implements WebFilter {
	private static final Logger logger = LoggerFactory.getLogger(WebConfigContext.class);

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		String originalUrl = exchange.getRequest().getURI().toString();
		String path = exchange.getRequest().getURI().getPath(); // Extract the path without the query string
		String query = exchange.getRequest().getURI().getQuery(); // Extract query string if present

		if (SecurityConstants.GATEWAY_URLS.contains(path) || path.contains(ApplicationConstants.SLASH_REPORTS)) {
			logger.info("Before URL: " + originalUrl);
			String newPath = path.replace(ApplicationConstants.SLASH_API, "");
			String encodedQuery = encodeQueryParams(query);
			String newUriString = exchange.getRequest().getURI().resolve(newPath).toString();
			if (query != null && !query.isEmpty()) {
				newUriString = newUriString + "?" + encodedQuery;
			}
			URI newUri = URI.create(newUriString);
			exchange = exchange.mutate().request(request -> request.uri(newUri)).build();
			String afterUrl = exchange.getRequest().getURI().toString();
			logger.info("After URL: " + afterUrl);
		}
		return chain.filter(exchange);
	}

	/**
	 * Encodes only the values in the query string while preserving the key-value
	 * structure.
	 */
	private String encodeQueryParams(String query) {
		if (query == null || query.isEmpty()) {
			return query;
		}

		return Arrays.stream(query.split("&")).map(param -> {
			String[] keyValue = param.split("=", 2);
			String key = keyValue[0];
			String value = keyValue.length > 1 ? URLEncoder.encode(keyValue[1], StandardCharsets.UTF_8) : "";
			return key + "=" + value;
		}).collect(Collectors.joining("&"));
	}

}