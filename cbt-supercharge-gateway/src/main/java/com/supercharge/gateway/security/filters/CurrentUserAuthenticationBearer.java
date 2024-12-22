package com.supercharge.gateway.security.filters;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.cbt.supercharge.utils.core.ApplicationUtils;
import com.supercharge.gateway.security.filters.verificationhandler.JwtVerifyHandler;
import com.supercharge.gateway.security.model.UserPrincipal;

import io.jsonwebtoken.Claims;
import reactor.core.publisher.Mono;

@Component
public class CurrentUserAuthenticationBearer {

	private static final Logger logger = LoggerFactory.getLogger(CurrentUserAuthenticationBearer.class);

	public static Mono<Authentication> create(JwtVerifyHandler.VerificationResult verificationResult) {
		Claims claims = null;
		if (ApplicationUtils.isValidateObject(verificationResult)) {
			claims = ApplicationUtils.isValidateObject(verificationResult.claims) ? verificationResult.claims : null;
			String subject;
			try {
				subject = ApplicationUtils.isValidateObject(claims.getSubject()) ? claims.getSubject() : null;

				List<String> roles = ApplicationUtils.isValidateObject(claims.get("role", List.class))
						? claims.get("role", List.class) : null;
				List<SimpleGrantedAuthority> authorities = null;
				if (ApplicationUtils.isValidList(roles)) {
					authorities = roles.stream().map(SimpleGrantedAuthority::new).toList();
				}
				String principalId = null;
				try {
					principalId = subject;
				} catch (NumberFormatException ignore) {
					logger.error("error while parsing user ID");
				}
				if (!ApplicationUtils.isValidateObject(principalId)) {
					return Mono.empty(); // invalid value for any of jwt auth parts
				}
				String name = null;
				name = ApplicationUtils.isNotBlank(claims.getIssuer()) ? claims.getIssuer() : "";
				UserPrincipal principal = new UserPrincipal(principalId, name);
				if (ApplicationUtils.isValidateObject(authorities) && ApplicationUtils.isValidateObject(principal)) {
					UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
							verificationResult, principal, authorities);
					return Mono.justOrEmpty(usernamePasswordAuthenticationToken);
				}
			} catch (Exception e) {
				logger.error("Invalid jwt token : " + e);
				return Mono.empty();
			}
		}
		return Mono.empty();
	}
}
