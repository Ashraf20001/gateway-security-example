package com.supercharge.gateway.security;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.cbt.supercharge.constants.core.ApplicationConstants;
import com.supercharge.gateway.security.filters.verificationhandler.UserVerifyHandler;

import reactor.core.publisher.Mono;

@Component
public class LdapAuthManagerBuilderProvider implements ReactiveAuthenticationManager {

	private static final Logger logger = LoggerFactory.getLogger(LdapAuthManagerBuilderProvider.class);

	/**
	 * ldapTemplate
	 */
	private LdapTemplate ldapTemplate;

	/**
	 * ldap contextSource
	 */
	private LdapContextSource contextSource;

	/**
	 * ldap url
	 */
	@Value("${ldap.url}")
	private String ldapUrl;

	/**
	 * ldap userDn
	 */
	@Value("${ldap.userDn}")
	private String ldapUserDn;

	/**
	 * ldapPassword
	 */
	@Value("${ldap.userPassword}")
	private String ldapPassword;

	/**
	 * ldapUserName
	 */
	@Value("${ldap.userName}")
	private String ldapUserName;

	@Override
	public Mono<Authentication> authenticate(Authentication authentication) {
		return Mono.deferContextual(context -> {
			// Retrieve ServerWebExchange from the reactive context
			ServerWebExchange exchange = context.get(ServerWebExchange.class);

			// Check for LDAP_NOT flag from query parameters
			boolean attributes = Boolean
					.parseBoolean(exchange.getRequest().getQueryParams().getFirst(ApplicationConstants.LDAP_NOT));

			logger.info("Login type local or LDAP :: {}", attributes);

			if (!attributes) {
				logger.info("Local authentication bypass.");
				return Mono.empty(); // No authentication handled here
			}

			// Initialize LDAP context
			initContext();

			try {
				// Test LDAP connection
				ldapTemplate.lookup(ApplicationConstants.WITHOUT_SPACE);
				logger.info("LDAP connection test successful");
				com.supercharge.gateway.security.model.User user = new com.supercharge.gateway.security.model.User();
				UserVerifyHandler.VerificationResult principal = null;
				principal = (UserVerifyHandler.VerificationResult) authentication.getPrincipal();
				// Create LDAP filter for authentication
				EqualsFilter filter = new EqualsFilter(ApplicationConstants.SAMACCOUNTNAME, principal.userName);

				// Perform authentication
				boolean isAuthenticatedViaLDAP = ldapTemplate.authenticate(ApplicationConstants.WITHOUT_SPACE,
						filter.encode(), principal.password.toString());

				logger.info("LDAP user authenticated: {}", isAuthenticatedViaLDAP);

				if (isAuthenticatedViaLDAP) {
					user.setUsername(principal.userName);
					user.setPassword(principal.password);
					Authentication auth = new UsernamePasswordAuthenticationToken(user, principal.password.toString(),
							new ArrayList<>());
					return Mono.just(auth); // Return authenticated token
				}

				logger.info("LDAP authentication failed");
				return Mono.error(new BadCredentialsException("LDAP authentication failed"));

			} catch (Exception e) {
				logger.error("LDAP exception: {}", e.getMessage(), e);
				return Mono.error(new BadCredentialsException("LDAP authentication error", e));
			}
		});
	}

	private void initContext() {
		contextSource = new LdapContextSource();
		contextSource.setUrl(ldapUrl);
		contextSource.setBase(ldapUserDn);
		contextSource.setUserDn(ldapUserName);
		contextSource.setPassword(ldapPassword);
		contextSource.afterPropertiesSet();
		ldapTemplate = new LdapTemplate(contextSource);
	}

}
