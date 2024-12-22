package com.supercharge.gateway.security.filters.verificationhandler;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import com.cbt.supercharge.exception.core.ApplicationException;
import com.cbt.supercharge.exception.core.codes.ErrorCodes;
import com.cbt.supercharge.exception.core.codes.ErrorId;
import com.cbt.supercharge.exception.core.codes.ErrorId.Severity;
import com.cbt.supercharge.utils.core.ApplicationUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.security.Keys;
import reactor.core.publisher.Mono;

/**
 * JwtVerifyHandler class
 *
 * 
 */
public class JwtVerifyHandler {

	private static final Logger logger = LoggerFactory.getLogger(JwtVerifyHandler.class);

	private final String secret;

	private static final String EMPTY = "";

	public JwtVerifyHandler(String secret) {
		this.secret = secret;
	}

	public Mono<VerificationResult> check(String token, String tokenType) throws ApplicationException {
		try {
			return Mono.just(verify(token, tokenType))
					.onErrorResume(e -> Mono.error(new ApplicationException(
							new ErrorId(ErrorCodes.INVALID_USER.getErrorCode(),
									ErrorCodes.INVALID_USER.getErrorMessage(), Severity.FATAL),
							HttpStatus.UNAUTHORIZED)));
		} catch (ApplicationException e) {
			throwUnauthorizedException(token);
		}
		return Mono.empty();
	}

	public VerificationResult checkRefreshToken(String token, String tokenType)
			throws ApplicationException, DecodingException {
		try {
			return verify(token, tokenType);
		} catch (DecodingException ex) {
			throw new DecodingException(
					com.cbt.supercharge.exception.core.codes.ErrorCodes.INVALID_TOKEN.getErrorMessage());
		} catch (ApplicationException e) {
			throwUnauthorizedException(token);
		}
		return null;
	}

	private void throwUnauthorizedException(String accessToken) throws ApplicationException {
		logger.error("Jwt token expired ");
		throw new ApplicationException(new ErrorId(ErrorCodes.INVALID_USER.getErrorCode(),
				ErrorCodes.INVALID_USER.getErrorMessage(), Severity.FATAL), HttpStatus.UNAUTHORIZED);
	}

	private VerificationResult verify(String token, String tokenType) throws ApplicationException, DecodingException {
		VerificationResult verifiedResult = null;
		if (ApplicationUtils.isBlank(token)) {
			throwUnauthorizedException(token);
		}
		try {
			Claims claims = getAllClaimsFromToken(token);
			if (ApplicationUtils.isValidateObject(claims)) {
				if (!claims.getId().equals(tokenType)) {
					throw new DecodingException(ErrorCodes.INVALID_TOKEN.getErrorMessage());
				}
				final long expiration = claims.getExpiration().getTime();
				if (expiration < new Date().getTime())
					throw new RuntimeException("Token expired");
				verifiedResult = new VerificationResult(claims, token);
				return verifiedResult;
			} else {
				throwUnauthorizedException(token);
			}
		} catch (DecodingException ex) {
			throw new DecodingException(ErrorCodes.INVALID_TOKEN.getErrorMessage());
		} catch (Exception ex) {
			throwUnauthorizedException(token);
		}
		return verifiedResult;
	}

	public Claims getAllClaimsFromToken(String token) throws ApplicationException, DecodingException {
		Claims claims = null;
		try {
			claims = Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(secret.getBytes())).build()
					.parseClaimsJws(token).getBody();
		} catch (DecodingException ex) {
			throw new DecodingException(ErrorCodes.INVALID_TOKEN.getErrorMessage());
		} catch (MalformedJwtException e) {
			throw new DecodingException(ErrorCodes.INVALID_TOKEN.getErrorMessage());
		} catch (Exception ex) {
			throwUnauthorizedException(token);
		}
		return claims;
	}

	public class VerificationResult {
		public Claims claims;
		public String token;

		public VerificationResult(Claims claims, String token) {
			this.claims = claims;
			this.token = token;
		}
	}
}
