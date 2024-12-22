package com.supercharge.gateway.filters;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import com.cbt.supercharge.exception.core.ApplicationException;
import com.cbt.supercharge.exception.core.codes.ErrorCodes;
import com.cbt.supercharge.exception.core.codes.ErrorId;
import com.cbt.supercharge.exception.core.codes.ErrorId.Severity;

public abstract class GatewayUserGroupSupport {

	public String getLoggedInUserName() throws ApplicationException {

		if (SecurityContextHolder.getContext().getAuthentication() == null) {
			return null;
		}
		Object obj = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (obj instanceof User user)
			return user.getUsername();
		throw new ApplicationException(new ErrorId(ErrorCodes.INVALID_USER.getErrorCode(),
				ErrorCodes.INVALID_USER.getErrorMessage(), Severity.FATAL), HttpStatus.UNAUTHORIZED);
	}
	
//	public Mono<String> getLoggedInUserName() throws ApplicationException {
//	    return Mono.deferContextual(ctx -> {
//	    	 Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//	         
//	         if (authentication == null) {
//	             // Return empty Mono if authentication is not available
//	             return Mono.justOrEmpty(null);
//	         }
//
//	        // Get the principal (user) from the authentication
//	        Object obj = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//	        if (obj instanceof User user) {
//	            // If the principal is a User, return the username wrapped in Mono
//	            return Mono.justOrEmpty(user.getUsername());
//	        }
//
//	        // If the principal is not a User, throw an error in Mono
//	        return Mono.error(new ApplicationException(new ErrorId(
//	                ErrorCodes.INVALID_USER.getErrorCode(),
//	                ErrorCodes.INVALID_USER.getErrorMessage(), 
//	                Severity.FATAL), HttpStatus.UNAUTHORIZED));
//	    });
//	}

//	public Mono<String> getLoggedInUserName() throws ApplicationException {
//		return Mono.deferContextual(ctx -> {
//			return ReactiveSecurityContextHolder.getContext().flatMap(securityContext -> {
//				Authentication authentication = securityContext.getAuthentication();
//				if (authentication == null) {
//					// If no authentication, return empty Mono
//					return Mono.empty();
//				}
//				// Get the principal (user) from the authentication
//				Object principal = authentication.getPrincipal();
//				if (principal instanceof User user) {
//					// Return the username wrapped in Mono
//					return Mono.just(user.getUsername());
//				}
//				// If principal is not a User, return an error Mono
//				return Mono.error(new ApplicationException(new ErrorId(ErrorCodes.INVALID_USER.getErrorCode(),
//						ErrorCodes.INVALID_USER.getErrorMessage(), Severity.FATAL), HttpStatus.UNAUTHORIZED));
//			});
//		});
//	}



}
