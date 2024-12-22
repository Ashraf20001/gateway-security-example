/*
 * @author codeboard
 */
package com.supercharge.gateway.common.base.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import com.cbt.supercharge.exception.core.ApplicationException;
import com.cbt.supercharge.transfer.objects.common.entity.SubFileMetaData;
import com.cbt.supercharge.transfer.objects.entity.UserProfile;
import com.cbt.supercharge.transfter.objects.core.ApplicationResponse;
import com.cbt.supercharge.transfter.objects.core.holder.InstitutionUserHolder;
import com.cbt.supercharge.utils.core.ApplicationUtils;
import com.supercharge.gateway.common.base.service.GatewayBaseService;

/**
 * The Class BaseController.
 */
@Component
public abstract class GatewayBaseController {

	/**
	 * The Constant SUCCESS.
	 */
	private static final String SUCCESS = " Success!";
	/**
	 * The base service.
	 */
	@Autowired
	private GatewayBaseService baseService;

	/**
	 * The json object.
	 */
	private final Map<String, String> jsonObject = new HashMap<>();

	/**
	 * The logger.
	 */
	protected Logger logger = LoggerFactory.getLogger(getClassName());

	/**
	 * Gets the application response.
	 *
	 * @param httpStatus the http status
	 * @return the application response
	 */
	public ApplicationResponse getApplicationResponse(HttpStatus httpStatus) {
		ApplicationResponse applicationResponse = new ApplicationResponse();
		applicationResponse.setStatus(httpStatus);
		return applicationResponse;
	}

	/**
	 * Gets the application response.
	 *
	 * @param content the content
	 * @return the application response
	 * @throws ApplicationException 
	 */
	public ApplicationResponse getApplicationResponse(Object content) {
		ApplicationResponse applicationResponse = new ApplicationResponse();
		if (content != null) {
			applicationResponse.setContent(content);
		}
		return applicationResponse;
	}

	/**
	 * @param subListId
	 * @param list
	 * @param subFileMetaList
	 * @return
	 * @throws ApplicationException
	 */
	public ApplicationResponse getMaskedApplicationResponse(String subListId, List<Map<String, Object>> list,
			List<SubFileMetaData> subFileMetaList) throws ApplicationException {
		return baseService.getMaskedApplicationResponse(subListId, list, subFileMetaList);
	}

	/**
	 * Gets the class name.
	 *
	 * @return the class name
	 */
	protected abstract Class<?> getClassName();

	/**
	 * Gets the default application response.
	 *
	 * @return the default application response
	 */
	public ApplicationResponse getDefaultApplicationResponse() {
		ApplicationResponse applicationResponse = new ApplicationResponse();
		applicationResponse.setStatus(HttpStatus.OK);
		applicationResponse.setContent(SUCCESS);
		return applicationResponse;
	}

	/**
	 * Gets the default response entity.
	 *
	 * @return the default response entity
	 */
	public ResponseEntity<ApplicationResponse> getDefaultResponseEntity() {
		return getResponseEntity(jsonObject, SUCCESS, HttpStatus.OK);
	}

	/**
	 * Gets the logged in user.
	 *
	 * @return the logged in user
	 * @throws ApplicationException the application exception
	 */
	public UserProfile getLoggedInUser() throws ApplicationException {
		return baseService.getLoggedInUser();
	}

	public String getLoggedInUserInstitutionId() {
		return InstitutionUserHolder.getAppInstitutionIdentity();
	}

	/**
	 * Gets the response entity.
	 *
	 * @param content the content
	 * @return the response entity
	 */
	public ResponseEntity<ApplicationResponse> getResponseEntity(Object content) {
		return getResponseEntity(content, SUCCESS, HttpStatus.OK);
	}

	/**
	 * Gets the response entity.
	 *
	 * @param content    the content
	 * @param httpStatus the http status
	 * @return the response entity
	 */
	public ResponseEntity<ApplicationResponse> getResponseEntity(Object content, HttpStatus httpStatus) {
		return getResponseEntity(content, SUCCESS, httpStatus);
	}

	/**
	 * Gets the response entity.
	 *
	 * @param content the content
	 * @param status  the status
	 * @return the response entity
	 */
	public ResponseEntity<ApplicationResponse> getResponseEntity(Object content, String status) {
		return getResponseEntity(content, status, HttpStatus.OK);
	}

	/**
	 * Gets the response entity.
	 *
	 * @param content    the content
	 * @param status     the status
	 * @param httpStatus the http status
	 * @return the response entity
	 */
	public ResponseEntity<ApplicationResponse> getResponseEntity(Object content, String status, HttpStatus httpStatus) {
		ApplicationResponse applicationResponse = new ApplicationResponse();
		applicationResponse.setContent(content);
		if (ApplicationUtils.isBlank(status)) {
			applicationResponse.setStatus(status);
		}
		return new ResponseEntity<>(applicationResponse, httpStatus);
	}

	/**
	 * Gets the response entity.
	 *
	 * @param status the status
	 * @return the response entity
	 */
	public ResponseEntity<ApplicationResponse> getResponseEntity(String status) {
		return getResponseEntity(jsonObject, status, HttpStatus.OK);
	}

	/**
	 * Gets the response entity.
	 *
	 * @param status     the status
	 * @param httpStatus the http status
	 * @return the response entity
	 */
	public ResponseEntity<ApplicationResponse> getResponseEntity(String status, HttpStatus httpStatus) {
		return getResponseEntity(jsonObject, status, httpStatus);
	}

	/**
	 * Gets the vo.
	 *
	 * @param identity the identity
	 * @return the vo
	 * @throws ApplicationException the application exception
	 */
	public abstract Object getVo(String identity) throws ApplicationException;

	/**
	 * In this method we want to add our class to {@link InterceptorRegistry}, this
	 * InterceptorRegistry is track our save and update methods and change the
	 * request body object to support data restriction and change response object to
	 * support data restriction.
	 */
	protected abstract void registerInterceptor();
}
