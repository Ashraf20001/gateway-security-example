package com.supercharge.gateway.common.handlers;

import com.cbt.supercharge.exception.core.codes.ErrorId;
import com.cbt.supercharge.exception.core.codes.ErrorIdList;

public class CustomException extends RuntimeException {
	
	/**
	 * The error id list.
	 */
	private ErrorIdList errorIdList = new ErrorIdList();
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CustomException(String message) {
		super(message);
	}
	
	public CustomException(ErrorId errorId) {
		super(new ErrorIdList(errorId).convertToJsonString());
		errorIdList.addError(errorId);
	}
}
