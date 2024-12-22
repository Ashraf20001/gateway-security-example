/*
 * @author codeboard
 */
package com.supercharge.gateway.common.base.dao;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import com.cbt.supercharge.constants.core.ApplicationConstants;
import com.cbt.supercharge.exception.core.ApplicationException;
import com.cbt.supercharge.exception.core.codes.ErrorCodes;
import com.cbt.supercharge.transfter.objects.core.dto.FilterOrSortingVo;

/**
 * The Interface IMasterFilter.
 */
public interface IGatewayMasterFilter {

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
	Predicate getFilterPredicate(From<?, ?> root, CriteriaBuilder builder, FilterOrSortingVo filterVo,
			CriteriaQuery<?> criteria) throws ApplicationException;

	/**
	 * Gets the path.
	 *
	 * @param <Y>        the generic type
	 * @param root       the root
	 * @param colunNames the colun names
	 * @return the path
	 */
	default <Y extends Comparable<? super Y>> Path<Y> getPath(From<?, ?> root, String[] colunNames) {
		Path<Y> path = root.get(colunNames[0]);
		for (int i = 1; i < colunNames.length && colunNames.length > 1; i++) {
			path = path.get(colunNames[i]);
		}
		return path;
	}
}
