/*
 * @author codeboard
 */
package com.supercharge.gateway.infrastructure;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.cbt.supercharge.constants.core.ApplicationConstants;

/**
 * The Class RestAuthenticationEntryPoint.
 */
@Component(ApplicationConstants.REST_AUTHENTICATION_ENTRY_POINT)
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

	/**
	 * Commence.
	 *
	 * @param request       the request
	 * @param response      the response
	 * @param authException the auth exception
	 * @throws IOException      Signals that an I/O exception has occurred.
	 * @throws ServletException the servlet exception
	 */
	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,AuthenticationException authException)
			throws IOException, ServletException {
		Boolean ldapAuthentication = Boolean
				.parseBoolean(((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest()
						.getParameter(ApplicationConstants.LDAP_NOT));
		response.setContentType("text/plain;charset=UTF-8");
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		String message = authException.getMessage();
		if (message.equals(ApplicationConstants.BAD_CREDENTIALS)) {
			if(Boolean.TRUE.equals(ldapAuthentication)) {
				response.getWriter().write(ApplicationConstants.USER_NOT_FOUND);
			}else {
				response.getWriter().write(ApplicationConstants.INVALIDE_PASSWORD);
			}
		} else {
			if(Boolean.TRUE.equals(ldapAuthentication)) {
				response.getWriter().write(ApplicationConstants.USER_NOT_FOUND);
			}else {
				response.getWriter().write(message);
			}
		}
	}
}