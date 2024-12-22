/*
 * @author codeboard
 */
package com.supercharge.gateway.common.base.dao;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cbt.supercharge.constants.core.ApplicationConstants;
import com.cbt.supercharge.constants.core.TableConstants;
import com.cbt.supercharge.exception.core.ApplicationException;
import com.cbt.supercharge.exception.core.codes.ErrorCodes;
import com.cbt.supercharge.utils.core.ApplicationDateUtils;

/**
 * The Class DataTypeConvertor.
 */
@Component
public class DataTypeConvertorGateway {

	/**
	 * The application data util.
	 */
	@Autowired
	private ApplicationDateUtils applicationDataUtil;
	/**
	 * The logger.
	 */
	protected Logger logger = LoggerFactory.getLogger(DataTypeConvertorGateway.class);

	/**
	 * Conver to real data type.
	 *
	 * @param <Y>  the generic type
	 * @param data the data
	 * @param type the type
	 * @return the y
	 * @throws ApplicationException the application exception
	 */
	@SuppressWarnings("unchecked")
	public <Y extends Comparable<? super Y>> Y converToRealDataType(String data, String type)
			throws ApplicationException {

		if (data == null || data.trim().isEmpty()) {
			data = "";
			return (Y) data;
		}

		data = data.trim();

		if (type.equals(ApplicationConstants.FILTER_TYPE_INTEGER)) {
			try {
				return (Y) Integer.valueOf(data);
			} catch (Exception e) {
				throw new ApplicationException(ErrorCodes.INVALID_DATA);
			}
		} else if (type.equals(ApplicationConstants.FILTER_TYPE_BOOLEAN)) {
			try {
				return (Y) Boolean.valueOf(data);
			} catch (Exception e) {
				throw new ApplicationException(ErrorCodes.INVALID_DATA);
			}
		} else if (type.equals(ApplicationConstants.FILTER_TYPE_DOUBLE) || type.equals(TableConstants.DECIMAL)) {
			try {
				return (Y) Double.valueOf(data);
			} catch (Exception e) {
				throw new ApplicationException(ErrorCodes.INVALID_DATA);
			}
		} else if (type.equals(ApplicationConstants.FILTER_TYPE_TEXT)) {
			return (Y) data.trim();
			
		} else if (type.equals(ApplicationConstants.FILTER_TYPE_DATE) || type.equals(ApplicationConstants.DATETIME) ) {
			Date date = applicationDataUtil.convertStrIntoDate(data, ApplicationConstants.DEFAULT_FULL_DATE_FORMATS,
					null);
			return (Y) date;
		} else {
			throw new ApplicationException(ErrorCodes.INVALID_DATA);
		}

	}

}
