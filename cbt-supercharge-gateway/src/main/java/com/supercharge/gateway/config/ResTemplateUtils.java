package com.supercharge.gateway.config;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;


@Component
public class ResTemplateUtils {
	
    public static final String AUTHORIZATION = "Authorization";
    
    public static final String BEARER = "Bearer ";
    
    public static final String EMPTY_STRING = " ";
	
	  public HttpHeaders getPOSTHeaders(){
	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.APPLICATION_JSON);
	        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
	        return headers;
	    }
	    public HttpHeaders getGETHeaders(){
	        HttpHeaders headers = new HttpHeaders();
//	        headers.setContentType(MediaType.APPLICATION_JSON);
	        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
	        return headers;
	    }
	    
	    /**
	     * @param request
	     * @return
	     */
	    public HttpHeaders configureRestTemplate(HttpServletRequest request) {
			HttpHeaders httpHeaders = null;
			String authorizationHeader = request.getHeader(AUTHORIZATION);
			String authToken = authorizationHeader.replace(BEARER, EMPTY_STRING)
					.trim();
			if(HttpMethod.GET.toString().equals(request.getMethod())) {
				httpHeaders = getGETHeaders();
			}else {
				httpHeaders = getPOSTHeaders();
			}
			httpHeaders.setBearerAuth(authToken);
			return httpHeaders;
		}

}
