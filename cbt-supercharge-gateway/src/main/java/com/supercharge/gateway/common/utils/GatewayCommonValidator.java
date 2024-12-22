package com.supercharge.gateway.common.utils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.cbt.supercharge.exception.core.ApplicationException;
import com.cbt.supercharge.exception.core.codes.ErrorId;
import com.cbt.supercharge.utils.core.ApplicationUtils;

/**
 * @author CBT
 */
@Component
public class GatewayCommonValidator {

	public static boolean isValidateObject(Object obj) {
		boolean expression = false;
		if (obj == null) {
			return expression;
		}
		expression = true;
		return expression;
	}

	public static boolean isValidList(List<?> list) {
		boolean expression = false;
		if (list == null || list.isEmpty()) {
			return expression;
		}
		expression = true;
		return expression;

	}

	public static boolean isValidString(String value) {
		boolean expression = false;
		if (ApplicationUtils.isBlank(value)) {
			return expression;
		}
		expression = true;
		return expression;
	}

	public static void validateObject(Object obj, ErrorId errorId) throws ApplicationException {
		if ((obj == null) || (obj instanceof List<?> && ((Collection<?>) obj).isEmpty())) {
			throw new ApplicationException(errorId);
		}
		if (obj instanceof Map<?, ?> && ((Map) obj).isEmpty()) {
			throw new ApplicationException(errorId);
		}
	}

	public static void validateString(String value, ErrorId errorId) throws ApplicationException {
		if (ApplicationUtils.isBlank(value) || value.equals("NaN")) {
			throw new ApplicationException(errorId);
		}
	}

	// This Utility class should not have public constructor.
	private GatewayCommonValidator() {

	}

}
