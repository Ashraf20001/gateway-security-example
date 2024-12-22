package com.supercharge.gateway.security;


import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * PBKDF2Encoder class
 *
 * 
 */
@Component
public class PBKDF2Encoder implements PasswordEncoder {

    @Value("${springbootwebfluxjjwt.jjwt.secret}")
    private String secret;

    @Value("${springbootwebfluxjjwt.jjwt.iteration}")
    private Integer iteration;

    @Value("${springbootwebfluxjjwt.jjwt.keylength}")
    private Integer keylength;
    
    private static final Logger logger = LoggerFactory.getLogger(PBKDF2Encoder.class);
    /**
     * More info (https://www.owasp.org/index.php/Hashing_Java)
     * @param cs password
     * @return encoded password
     */
    @Override
    public String encode(CharSequence cs){
        try {
            byte[] result = SecretKeyFactory.getInstance(secret)
                    .generateSecret(new PBEKeySpec(cs.toString().toCharArray(), secret.getBytes(), iteration, keylength))
                    .getEncoded();

            return Base64.getEncoder()
                    .encodeToString(result);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
        	logger.error("Invalid key : "+secret);
        	throw new RuntimeException(ex);
        }
    }

    @Override
    public boolean matches(CharSequence cs, String string) {
        return encode(cs).equals(string);
    }
}
