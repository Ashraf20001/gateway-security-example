package com.supercharge.gateway.security.filters.verificationhandler;

import java.io.ByteArrayInputStream;

import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import com.cbt.supercharge.exception.core.ApplicationException;
import com.cbt.supercharge.exception.core.codes.ErrorCodes;
import com.cbt.supercharge.exception.core.codes.ErrorId;
import com.cbt.supercharge.exception.core.codes.ErrorId.Severity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.supercharge.gateway.security.model.UserSecurityToken;

import reactor.core.publisher.Mono;

public class AuthVerifyHandler {

	private static final Logger logger = LoggerFactory.getLogger(AuthVerifyHandler.class);

	public Mono<VerificationResult> check(String accessToken) throws ApplicationException {
		return Mono.just(verify(accessToken))
				.onErrorResume(
						e -> Mono.error(new ApplicationException(
								new ErrorId(ErrorCodes.INVALID_USER.getErrorCode(),
										ErrorCodes.INVALID_USER.getErrorMessage(), Severity.FATAL),
								HttpStatus.UNAUTHORIZED)));
	}

	private VerificationResult verify(String token) throws ApplicationException {
		UserSecurityToken userSecurityToken = parseToken(token);
		return new VerificationResult(userSecurityToken);
	}

	public UserSecurityToken parseToken(String token) throws ApplicationException {
		byte[] userBytes = null;
		UserSecurityToken user = null;
		try {
			userBytes = fromBase64(token);
			user = fromJSON(userBytes);
		} catch (Exception ex) {
			logger.error(ErrorCodes.INVALID_USER.getErrorMessage());
			throw new ApplicationException(new ErrorId(ErrorCodes.INVALID_USER.getErrorCode(),
					ErrorCodes.INVALID_USER.getErrorMessage(), Severity.FATAL), HttpStatus.UNAUTHORIZED);
		}
		return user;
	}

	private byte[] fromBase64(String content) {
		return DatatypeConverter.parseBase64Binary(content);
	}

	private UserSecurityToken fromJSON(final byte[] userBytes) throws ApplicationException {
		try {
			return new ObjectMapper().readValue(new ByteArrayInputStream(userBytes), UserSecurityToken.class);
		} catch (Exception e) {
			logger.error(ErrorCodes.INVALID_USER.getErrorMessage());
			throw new ApplicationException(new ErrorId(ErrorCodes.INVALID_USER.getErrorCode(),
					ErrorCodes.INVALID_USER.getErrorMessage(), Severity.FATAL), HttpStatus.UNAUTHORIZED);
		}
	}

	public class VerificationResult {

		public UserSecurityToken userSecurityToken;

		public VerificationResult(UserSecurityToken userSecurityToken) {
			this.userSecurityToken = userSecurityToken;
		}

	}

}
