package com.supercharge.gateway.security.filters;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.cbt.supercharge.utils.core.ApplicationUtils;
import com.supercharge.gateway.security.filters.verificationhandler.AuthVerifyHandler;

import reactor.core.publisher.Mono;

public class CurrentUserAuthenticationAuth {

	public static Mono<Authentication> create(AuthVerifyHandler.VerificationResult verificationResult) {
		UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = null;
		if (ApplicationUtils.isValidateObject(verificationResult)) {
			usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(verificationResult, null);
			SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
		}
		return Mono.justOrEmpty(usernamePasswordAuthenticationToken);
	}

}
