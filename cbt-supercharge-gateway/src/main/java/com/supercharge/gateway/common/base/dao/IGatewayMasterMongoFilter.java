/*
 * @author codeboard
 */
package com.supercharge.gateway.common.base.dao;

import org.bson.Document;

import com.cbt.supercharge.constants.core.ApplicationConstants;
import com.cbt.supercharge.exception.core.ApplicationException;
import com.cbt.supercharge.exception.core.codes.ErrorCodes;
import com.cbt.supercharge.transfter.objects.core.dto.FilterOrSortingVo;

/**
 * The Interface IMasterFilter.
 */
public interface IGatewayMasterMongoFilter {


	/**
	 * Gets the filter predicate.
	 *
	 * @param root     the root
	 * @param builder  the builder
	 * @param filterVo the filter vo
	 * @param criteria the criteria
	 * @return the filter predicate
	 * @throws ApplicationException the application exception
	 */
	Document getFilterPredicate(FilterOrSortingVo filterVo) throws ApplicationException;
	
	/**
	 * Gets the column names.
	 *
	 * @param columnName the column name
	 * @return the column names
	 * @throws ApplicationException the application exception
	 */
	default String[] getColumnNames(String columnName) throws ApplicationException {
		if (columnName == null || columnName.trim().equals("")) {
			throw new ApplicationException(ErrorCodes.INVALID_COLUMN_NAME);
		}
		return columnName.split(ApplicationConstants.DOT_REGEX);
	}
}
