/*
 * @author codeboard
 */
package com.supercharge.gateway.common.utils;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.cbt.supercharge.constants.core.ApplicationConstants;
import com.cbt.supercharge.constants.core.TableConstants;
import com.cbt.supercharge.exception.core.ApplicationException;
import com.cbt.supercharge.exception.core.codes.ErrorCodes;
import com.cbt.supercharge.exception.core.codes.ErrorId;
import com.cbt.supercharge.exception.core.codes.ErrorId.Severity;
import com.cbt.supercharge.transfer.objects.entity.Role;
import com.cbt.supercharge.transfer.objects.entity.SystemConfig;
import com.cbt.supercharge.transfer.objects.entity.UserAndInstitutionLinking;
import com.cbt.supercharge.transfer.objects.entity.UserProfile;
import com.cbt.supercharge.utils.core.ApplicationDateUtils;
import com.cbt.supercharge.utils.core.ApplicationUtils;
import com.supercharge.gateway.common.base.dao.CommonUserDaoImpl;
import com.supercharge.gateway.common.base.dao.GatewayEnvironmentProperties;
import com.supercharge.gateway.security.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;

/**
 * The Class JwtUtils.
 */
@Component
public class JwtUtils {
	
	@Autowired
	private CommonUserDaoImpl commonUserDaoImpl;
	
	@Autowired
	private GatewayEnvironmentProperties environemntProperities;
	
	/**
	 * The Constant logger.
	 */
	private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

	/**
	 * The jwt secret.
	 */
	@Value("${jwtSecret}")
	private String jwtSecret;

	@Value("${springbootwebfluxjjwt.jjwt.secret}")
	private String secret;

	/**
	 * Can token be refreshed.
	 *
	 * @param token the token
	 * @return the boolean
	 */
	public Boolean canTokenBeRefreshed(String token) {
		return (!isTokenExpired(token) || ignoreTokenExpiration());
	}

	/**
	 * Token generation with a custom role
	 *
	 * @param user
	 * @param institutionId
	 * @return
	 * @throws ApplicationException 
	 */
	public String generateCustomRoleJwtToken(String userName, String institutionId, String customRoleName) throws ApplicationException {
		Claims claims = Jwts.claims().setSubject(userName);
		UserProfile loggedInUser = commonUserDaoImpl.getUserBySystemUserID(userName);
		if (!ApplicationUtils.isValidateObject(loggedInUser)) {
			return null;
		}
		List<String> roles = new ArrayList<>();
		roles.add(customRoleName);
		claims.put(ApplicationConstants.AUTH, roles);
		// Institution id
		List<SystemConfig> systemConfigs = commonUserDaoImpl.getSystemConfigDetail(ApplicationConstants.JWT_SESSION_EXPIRATIONMS);
		Long jwtExpirationMs = null;
		for (SystemConfig systemConfig : systemConfigs) {
			if (systemConfig.getConfigName().equals(ApplicationConstants.JWT_SESSION_EXPIRATIONMS)) {
				jwtExpirationMs = Long.parseLong(systemConfig.getConfigValue());
			}
		}
		SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
		claims.put(ApplicationConstants.INSTITUTION_ID, institutionId);
		return Jwts.builder().setSubject((userName)).setClaims(claims).setIssuedAt(new Date())
				.setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
				.signWith(secretKey).compact();
	}

	/**
	 * @return 
	 * 
	 */
	private SecretKeySpec generateSecreteKey() {
		byte[] decodedKey = Base64.getDecoder().decode(jwtSecret);
		if (decodedKey.length < 64) {
			decodedKey = Keys.secretKeyFor(SignatureAlgorithm.HS512).getEncoded();
		}
		return new SecretKeySpec(decodedKey, SignatureAlgorithm.HS512.getJcaName());
	}
	
    /**
     * Refresh token for 2fa
     *
     * @param user
     * @return
     * @throws ApplicationException 
     */
    public String generate2faJwtRefreshToken(Principal user) throws ApplicationException {
        Claims claims = Jwts.claims().setSubject(user.getName());
        //2FA role
        Role newRole = new Role();
        newRole.setUserRoleName(ApplicationConstants.ROLE_2FA);
        claims.put(ApplicationConstants.NEW_ROLE, newRole);
    	List<SystemConfig> systemConfigs = commonUserDaoImpl.getSystemConfigDetail(ApplicationConstants.JWT_REFRESH_SESSION_EXPIRATIONMS);
		Long jwtRefreshExpiration = null;
		for (SystemConfig systemConfig : systemConfigs) {
			if (systemConfig.getConfigName().equals(ApplicationConstants.JWT_SESSION_EXPIRATIONMS)) {
				jwtRefreshExpiration = Long.parseLong(systemConfig.getConfigValue());
			}
		}
		SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        return Jwts.builder().setSubject((user.getName())).setClaims(claims).setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtRefreshExpiration))
                .signWith(secretKey).compact();
    }

	/**
	 * Generate jwt refresh token.
	 *
	 * @param user the user
	 * @return the string
	 * @throws ApplicationException 
	 */
	public String generateJwtRefreshToken(String userName) throws ApplicationException {
		List<SystemConfig> systemConfigs = commonUserDaoImpl.getSystemConfigDetail(ApplicationConstants.JWT_REFRESH_SESSION_EXPIRATIONMS);
		UserProfile loggedInUser = commonUserDaoImpl.getUserBySystemUserID(userName);
        if (!ApplicationUtils.isValidateObject(loggedInUser)) {
            return null;
        }
		Long jwtRefreshExpiration = null;
		for (SystemConfig systemConfig : systemConfigs) {
			if (systemConfig.getConfigName().equals(ApplicationConstants.JWT_REFRESH_SESSION_EXPIRATIONMS)) {
				jwtRefreshExpiration = Long.parseLong(systemConfig.getConfigValue());
			}
		}
		return Jwts.builder().setIssuer(loggedInUser.getUserIdentificationNumber())
				.setSubject(loggedInUser.getUserIdentificationNumber()).setIssuedAt(new Date())
				.setId(ApplicationConstants.ACCESS_TOKEN)
				.setExpiration(new Date((new Date()).getTime() + jwtRefreshExpiration))
				.signWith(Keys.hmacShaKeyFor(secret.getBytes())).compact();
	}

	/**
	 * Generate jwt token.
	 *
	 * @param user the user
	 * @return the string
	 * @throws ApplicationException 
	 */
	public String generateJwtToken(String userName, String institutionId) throws ApplicationException {
		
		Claims claims = Jwts.claims().setSubject(userName);
        UserProfile loggedInUser = commonUserDaoImpl.getUserBySystemUserID(userName);
        if (!ApplicationUtils.isValidateObject(loggedInUser)) {
            return null;
        }
        //User Roles
        List<Role> roles = ApplicationUtils.isValidateObject(loggedInUser.getUserAndInstitutionLinking())
                ? loggedInUser.getUserAndInstitutionLinking().stream().map(UserAndInstitutionLinking::getRole)
                .collect(Collectors.toList())
                : new ArrayList<>();
        System.err.println("mapped roles to user (218) >>>>>>>>>   " + roles.stream().map(x -> x.get_id()).toList());
        
        if(!ApplicationUtils.isValidList(roles)) {
			roles = commonUserDaoImpl.getRoleByUser(loggedInUser);
			System.err.println("roles in line no: 228 >>>>    " + roles.stream().map(Role::get_id).toList());
		}
		claims.put(ApplicationConstants.AUTH,
				roles.stream().filter(a -> a != null).map(Role::getIdentity).collect(Collectors.toList()));
		claims.put(TableConstants.ROLE, roles.stream().filter(a -> a != null).map(Role::getUserRoleName).toList());
        //Institution id
//        i removed here.but i am unable to sign
        List<SystemConfig> systemConfigs = commonUserDaoImpl.getSystemConfigDetail(ApplicationConstants.JWT_SESSION_EXPIRATIONMS);
		Long jwtExpirationMs = null;
		for (SystemConfig systemConfig : systemConfigs) {
			if (systemConfig.getConfigName().equals(ApplicationConstants.JWT_SESSION_EXPIRATIONMS)) {
				jwtExpirationMs = Long.parseLong(systemConfig.getConfigValue());
			}
		}
		claims.put(ApplicationConstants.INSTITUTION_ID, institutionId);
		return Jwts.builder().setClaims(claims).setIssuer(loggedInUser.getUserIdentificationNumber())
				.setSubject(loggedInUser.getUserIdentificationNumber()).setIssuedAt(new Date())
				.setId(ApplicationConstants.ACCESS_TOKEN)
				.setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
				.signWith(Keys.hmacShaKeyFor(secret.getBytes())).compact();

	}
	
	/**
	 * Gets the all claims from token.
	 *
	 * @param token the token
	 * @return the all claims from token
	 */
	public Claims getAllClaimsFromToken(String token) {
		return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
	}
	
	public Claims getAllClaimsFromToken1(String token) {
		byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
		try {
			if (token.startsWith("Bearer ")) {
				token = token.substring(7);
			}
			return Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(bytes)).build().parseClaimsJws(token)
					.getBody();
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid token: " + e.getMessage());
		}
	}


	/**
	 * Gets the claim from token.
	 *
	 * @param <T>            the generic type
	 * @param token          the token
	 * @param claimsResolver the claims resolver
	 * @return the claim from token
	 */
	public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = getAllClaimsFromToken1(token);
		return claimsResolver.apply(claims);
	}

	/**
	 * Gets the expiration date from token.
	 *
	 * @param token the token
	 * @return the expiration date from token
	 */
	public Date getExpirationDateFromToken(String token) {
		return getClaimFromToken(token, Claims::getExpiration);
	}

	/**
	 * Gets the issued at date from token.
	 *
	 * @param token the token
	 * @return the issued at date from token
	 */
	public Date getIssuedAtDateFromToken(String token) {
		return getClaimFromToken(token, Claims::getIssuedAt);
	}

	/**
	 * Gets the user name from jwt token.
	 *
	 * @param token the token
	 * @return the user name from jwt token
	 */
	public String getUserNameFromJwtToken(String token) {
		return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody().getSubject();
	}
	
	/**
	 * Gets the username from token.
	 *
	 * @param token the token
	 * @return the username from token
	 */
	public String getUsernameFromToken(String token) {
		return getClaimFromToken(token, Claims::getSubject);
	}

	/**
	 * Ignore token expiration.
	 *
	 * @param token the token
	 * @return the boolean
	 */
	private Boolean ignoreTokenExpiration() {
		// here you specify tokens, for that the expiration is ignored
		return false;
	}

	/**
	 * Checks if is token expired.
	 *
	 * @param token the token
	 * @return the boolean
	 */
	public Boolean isTokenExpired(String token) {
		if (ApplicationUtils.isValidateObject(token)) {
			final Date expiration = getExpirationDateFromToken(token);
			return expiration.before(new Date());
		} else
			return false;
	}

	/**
	 * Validate jwt token.
	 *
	 * @param authToken the auth token
	 * @return true, if successful
	 */
	public boolean validateJwtToken(String authToken) {
		try {
			Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
			return true;
		} catch (MalformedJwtException e) {
			logger.error("Invalid JWT token: {}", e.getMessage());
		} catch (ExpiredJwtException e) {
			logger.error("JWT token is expired: {}", e.getMessage());
		} catch (UnsupportedJwtException e) {
			logger.error("JWT token is unsupported: {}", e.getMessage());
		} catch (IllegalArgumentException e) {
			logger.error("JWT claims string is empty: {}", e.getMessage());
		}

		return false;
	}

	/**
	 * Validate token.
	 *
	 * @param token       the token
	 * @param userDetails the user details
	 * @return the boolean
	 */
	public Boolean validateToken(String token, UserDetails userDetails) {
		final String username = getUsernameFromToken(token);
		return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
	}
	
	/**
	 * Gets the auth from jwt token.
	 * @param token the token
	 * @return the auth from jwt token
	 */
	@SuppressWarnings("unchecked")
	public List<String> getAuthFromJwtToken(String token) {
		return (List<String>) Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody().get("auth");
	}
	
	public String getUserIdFromToken(String token) {
		try {
			return getAllClaimsFromToken(token).getSubject();
		} catch (Exception e) {
			logger.error("Issue on decryptor for " + token + " Error detail ==>" + e.getMessage());
		}
		return null;
	}

	public String getUserNameFromJwtToken1(Map<String, Object> claims, String bearerToken, int expiredDateTime,
			User user) throws ApplicationException {
		claims = ApplicationUtils.isValidList(user.getRoles())
				? new HashMap<>(Map.of(ApplicationConstants.ROLE, user.getRoles())) : null;
            
		long expirationTimeInMilliseconds = 1l;
		expirationTimeInMilliseconds = ApplicationUtils.isValidateObject(expiredDateTime)
				? new Date().getTime() + expiredDateTime * ApplicationConstants.MILLISECS
				: 1l;
		Date expirationDate = expirationTimeInMilliseconds > 1l ? new Date(expirationTimeInMilliseconds) : new Date();
		Date createdDate = ApplicationDateUtils.getUTCZonedDate();
		if (!(ApplicationUtils.isValidateObject(expirationDate) && ApplicationUtils.isNotBlank(user.getUsername())
				&& ApplicationUtils.isValidateObject(createdDate) && ApplicationUtils.isValidateObject(claims)
				&& ApplicationUtils.isValidateObject(user.getId().toString()))) {
			logger.error("Invalid User");
			throw new ApplicationException(new ErrorId(ErrorCodes.INVALID_USER.getErrorCode(),
					ErrorCodes.INVALID_USER.getErrorMessage(), Severity.FATAL), HttpStatus.UNAUTHORIZED);
		}
		return Jwts.builder().setClaims(claims).setIssuer(user.getUsername())
				.setSubject(user.getId().toString()).setIssuedAt(createdDate)
				.setId(ApplicationConstants.ACCESS_TOKEN).setExpiration(expirationDate)
				.signWith(Keys.hmacShaKeyFor(secret.getBytes())).compact();
	}
	
}
