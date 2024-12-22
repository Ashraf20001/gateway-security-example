package com.supercharge.gateway.security.filters.verificationhandler;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import com.cbt.supercharge.constants.core.ApplicationConstants;
import com.cbt.supercharge.exception.core.ApplicationException;
import com.cbt.supercharge.exception.core.codes.ErrorCodes;
import com.cbt.supercharge.exception.core.codes.ErrorId;
import com.cbt.supercharge.exception.core.codes.ErrorId.Severity;

import reactor.core.publisher.Mono;

/**
 * JwtVerifyHandler class
 *
 * 
 */
public class UserVerifyHandler {

	private static final Logger logger = LoggerFactory.getLogger(UserVerifyHandler.class);

	public Mono<VerificationResult> check(String accessToken) throws ApplicationException {
		return Mono.just(verify(accessToken))
				.onErrorResume(
						e -> Mono.error(new ApplicationException(
								new ErrorId(ErrorCodes.INVALID_USER.getErrorCode(),
										ErrorCodes.INVALID_USER.getErrorMessage(), Severity.FATAL),
								HttpStatus.UNAUTHORIZED)));
	}

	private VerificationResult verify(String token) throws ApplicationException {
		try {
			byte[] decodedCredentials = Base64.getDecoder().decode(token);
			String credentials = new String(decodedCredentials, StandardCharsets.UTF_8);
			String[] values = credentials.split(ApplicationConstants.COLON, ApplicationConstants.SPLITTOKEN);
			return new VerificationResult(values[0], values[1]);
		} catch (Exception ex) {
			logger.error("Invalid User : " + ex);
			throw new ApplicationException(new ErrorId(ErrorCodes.INVALID_USER.getErrorCode(),
					ErrorCodes.INVALID_USER.getErrorMessage(), Severity.FATAL), HttpStatus.UNAUTHORIZED);
		}
	}

	public class VerificationResult {
		public String userName;
		public String password;

		public VerificationResult(String userName, String password) {
			this.userName = userName;
			this.password = password;
		}
	}
}
