package com.supercharge.gateway.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import org.springframework.web.server.WebExceptionHandler;
import org.springframework.web.server.WebSession;

import com.cbt.supercharge.constants.core.ApplicationConstants;
import com.cbt.supercharge.constants.core.SecurityConstants;
import com.supercharge.gateway.auth.service.UserDetailsServiceImpl;
import com.supercharge.gateway.common.base.dao.GatewayEnvironmentProperties;
import com.supercharge.gateway.common.handlers.CustomWebExceptionHandler;
import com.supercharge.gateway.common.handlers.GlobalExceptionHandler;
import com.supercharge.gateway.filters.AmlCache;
import com.supercharge.gateway.security.filters.AuthAuthenticationConverter;
import com.supercharge.gateway.security.filters.ServerHttpBearerAuthenticationConverter;
import com.supercharge.gateway.security.filters.UserAuthentication;
import com.supercharge.gateway.security.filters.verificationhandler.AuthVerifyHandler;
import com.supercharge.gateway.security.filters.verificationhandler.JwtVerifyHandler;
import com.supercharge.gateway.security.filters.verificationhandler.UserVerifyHandler;

import reactor.core.publisher.Mono;

@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class WebSecurityConfig {

	private final Logger logger = LoggerFactory.getLogger(WebSecurityConfig.class);

	@Value("${springbootwebfluxjjwt.jjwt.secret}")
	private String secret;

	@Autowired
	private GatewayEnvironmentProperties environmentProperties;

	@Autowired
	private AmlCache amlCache;
	
	/**
	 * The jwt secret.
	 */
	@Value("${jwtSecret}")
	private String jwtSecret;
	
	@Autowired
	private UserDetailsServiceImpl userDetailsServiceImpl;
	
	@Autowired
	private GlobalExceptionHandler globalExceptionHandler;
	
	
    @Bean
	public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http, AuthenticationManager authManager) {

		http.logout().logoutSuccessHandler(new ServerLogoutSuccessHandler() {
			@Override
			public Mono<Void> onLogoutSuccess(WebFilterExchange exchange, Authentication authentication) {
				amlCache.clearCache();
				ServerHttpResponse response = exchange.getExchange().getResponse();
				response.setStatusCode(HttpStatus.NO_CONTENT);
				response.getCookies().remove(ApplicationConstants.AUTHORIZATION);
				return exchange.getExchange().getSession().flatMap(WebSession::invalidate);
			}
		});

		return http.authorizeExchange().pathMatchers(HttpMethod.OPTIONS).permitAll()
				.pathMatchers(SecurityConstants.OpenURLsGET).permitAll().anyExchange().authenticated().and().csrf()
				.disable().headers().frameOptions().disable().and().httpBasic().disable().formLogin().disable()
				.exceptionHandling().authenticationEntryPoint((swe, e) -> {
					logger.info("[1] Authentication error: Unauthorized[401]: " + e.getMessage());

					return Mono.fromRunnable(() -> swe.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED));
				})
				.accessDeniedHandler(globalExceptionHandler).and()
				.addFilterAt(userAuthenticationFilter(authManager), SecurityWebFiltersOrder.AUTHENTICATION)
				.addFilterAt(bearerAuthenticationFilter(authManager), SecurityWebFiltersOrder.AUTHENTICATION)
				.addFilterAt(authAuthenticationFilter(authManager), SecurityWebFiltersOrder.AUTHENTICATION).build();
//				.addFilterAt(ldapAuthenticationFilter(authManager), SecurityWebFiltersOrder.AUTHENTICATION)
	}

	/**
	 * Spring security works by filter chaining. We need to add a JWT CUSTOM FILTER
	 * to the chain.
	 *
	 * what is AuthenticationWebFilter:
	 *
	 * A WebFilter that performs authentication of a particular request. An outline
	 * of the logic: A request comes in and if it does not match
	 * setRequiresAuthenticationMatcher(ServerWebExchangeMatcher), then this filter
	 * does nothing and the WebFilterChain is continued. If it does match then... An
	 * attempt to convert the ServerWebExchange into an Authentication is made. If
	 * the result is empty, then the filter does nothing more and the WebFilterChain
	 * is continued. If it does create an Authentication... The
	 * ReactiveAuthenticationManager specified in
	 * AuthenticationWebFilter(ReactiveAuthenticationManager) is used to perform
	 * authentication. If authentication is successful,
	 * ServerAuthenticationSuccessHandler is invoked and the authentication is set
	 * on ReactiveSecurityContextHolder, else ServerAuthenticationFailureHandler is
	 * invoked
	 *
	 */

	AuthenticationWebFilter userAuthenticationFilter(AuthenticationManager authManager) {
		AuthenticationWebFilter basicAuthenticationFilter = new AuthenticationWebFilter(authManager);
		basicAuthenticationFilter.setServerAuthenticationConverter(new UserAuthentication(new UserVerifyHandler()));
		return basicAuthenticationFilter;
	}

	AuthenticationWebFilter bearerAuthenticationFilter(AuthenticationManager authManager) {
		AuthenticationWebFilter bearerAuthenticationFilter = new AuthenticationWebFilter(authManager);
		bearerAuthenticationFilter.setServerAuthenticationConverter(
				new ServerHttpBearerAuthenticationConverter(new JwtVerifyHandler(secret), environmentProperties));
		return bearerAuthenticationFilter;
	}

	AuthenticationWebFilter authAuthenticationFilter(AuthenticationManager authManager) {
		AuthenticationWebFilter authAuthenticationFilter = new AuthenticationWebFilter(authManager);
		authAuthenticationFilter
				.setServerAuthenticationConverter(new AuthAuthenticationConverter(new AuthVerifyHandler()));
		return authAuthenticationFilter;
	}
	
    @Bean
    public ReactiveAuthenticationManager reactiveAuthenticationManager() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsServiceImpl);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return new ReactiveAuthenticationManager() {
            @Override
            public Mono<Authentication> authenticate(Authentication authentication) {
                return Mono.fromCallable(() -> authenticationProvider.authenticate(authentication));
            }
        };
    }
    
//	private AuthenticationWebFilter ldapAuthenticationFilter(AuthenticationManager authManager) {
//		AuthenticationWebFilter ldapAuthenticationFilter = new AuthenticationWebFilter(authManager);
//		ldapAuthenticationFilter.setServerAuthenticationConverter(
//				new ServerHttpBearerAuthenticationConverter(new JwtVerifyHandler(secret), environmentProperties));
//		return ldapAuthenticationFilter;
//	}
    
	@Bean
	public LdapAuthManagerBuilderProvider ldapAuthenticationManager() {
		return new LdapAuthManagerBuilderProvider(); // Ensure this is the same manager you're injecting
	}

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
    @Bean
    public WebSocketClient webSocketClient() {
        return new ReactorNettyWebSocketClient();
    }
    
    @Bean
    public WebExceptionHandler customWebExceptionHandler() {
        return new CustomWebExceptionHandler();
    }
    
}
