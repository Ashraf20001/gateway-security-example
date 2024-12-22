package com.supercharge.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder.Builder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GateWayConfiguration {
	
	/**
	 * APP_SERVER
	 */
	private static final String APP_SERVER = "/api/**";
	
	/**
	 * NOTIFICATION_SERVER
	 */
	private static final String NOTIFICATION_SERVER = "/notification/**";
	
	/**
	 * REPORT_SERVER
	 */
	private static final String REPORT_SERVER = "/reports/**";
	
	/**
	 * main-app
	 */
	@Value("${main-app.url}")
	private String mainAppUrl;
	
	/**
	 * notification
	 */
	@Value("${notification.url}")
	private String notificationUrl;
	
	/**
	 * report
	 */
	@Value("${report.url}")
	private String reportUrl;
	
	/**
	 * report
	 */
	@Value("${default.uri}")
	private String defaultUrl;
	 
	private static final String DEFAULT = "/**";
	
	/**
	 * @param routeLocatorBuilder
	 * @return
	 */
	@Bean
	public RouteLocator myRoutes(RouteLocatorBuilder routeLocatorBuilder) {
		Builder builder = routeLocatorBuilder.routes();
		addRoute(builder, APP_SERVER, mainAppUrl);
		addRoute(builder, NOTIFICATION_SERVER, notificationUrl);
		addRoute(builder, REPORT_SERVER, reportUrl);
		addRoute(builder, DEFAULT, defaultUrl);
		return builder.build();
	}

	/**
	 * @param builder
	 * @param predicates
	 * @param uri
	 */
	private void addRoute(Builder builder, String predicates, String uri) {
		builder.route(p -> p.path(predicates).uri(uri));
	}
	
//	/**
//	 * @return
//	 */
//	@Bean
//    public CorsWebFilter corsWebFilter() {
//        final CorsConfiguration corsConfig = new CorsConfiguration();
//        corsConfig.setAllowedOrigins(Collections.singletonList("*"));
//        corsConfig.setMaxAge(3600L);
//        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST"));
//        corsConfig.addAllowedHeader("*");
//        corsConfig.setAllowedOrigins(Arrays.asList("*"));
//        corsConfig.setAllowedMethods(Collections.singletonList("*"));
//        corsConfig.setAllowCredentials(false);
//        corsConfig.setAllowedHeaders(Collections.singletonList("*"));
//        corsConfig.setExposedHeaders(Arrays.asList("Authorization"));
//        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", corsConfig);
//        return new CorsWebFilter(source);
//    } 
//	
//	@Bean
//	public CorsWebFilter corsWebFilter() {
//	    final CorsConfiguration corsConfig = new CorsConfiguration();
//	    corsConfig.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
//	    corsConfig.setMaxAge(3600L);
//	    corsConfig.setAllowedMethods(Arrays.asList("GET", "POST"));
//	    corsConfig.addAllowedHeader("*");
//	    corsConfig.setAllowCredentials(false);
//	    corsConfig.setExposedHeaders(Arrays.asList("Authorization"));
//	    final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//	    source.registerCorsConfiguration("/**", corsConfig);
//	    return new CorsWebFilter(source);
//	}

} 
