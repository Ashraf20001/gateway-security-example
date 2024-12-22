package com.supercharge.gateway.security.filters;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.cbt.supercharge.utils.core.ApplicationUtils;
import com.supercharge.gateway.security.filters.verificationhandler.UserVerifyHandler;

import reactor.core.publisher.Mono;

@Component
public class CurrentUserAuthenticationBasic {

	public static Mono<Authentication> create(UserVerifyHandler.VerificationResult verificationResult) {
		UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = null;
		if (ApplicationUtils.isValidateObject(verificationResult)) {
			usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(verificationResult, null);
			SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
		}
		return Mono.justOrEmpty(usernamePasswordAuthenticationToken);
	}
}
