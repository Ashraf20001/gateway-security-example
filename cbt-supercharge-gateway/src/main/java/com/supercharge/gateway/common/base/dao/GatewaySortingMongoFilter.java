/*
 * @author codeboard
 */
package com.supercharge.gateway.common.base.dao;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.cbt.supercharge.exception.core.ApplicationException;
import com.cbt.supercharge.transfter.objects.core.dto.FilterOrSortingVo;

/**
 * The Class SortingFilter.
 */
@Service
@Qualifier("SortingFilters")
public class GatewaySortingMongoFilter implements IGatewayMasterMongoFilter {

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
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Document getFilterPredicate(FilterOrSortingVo filterVo) throws ApplicationException {
		if (filterVo.isAscending()) {
			String projectionJson = "{\"" + filterVo.getColumnName() + "\": 1}";
			return Document.parse(projectionJson);
		} else {
			String projectionJson = "{\"" + filterVo.getColumnName() + "\": -1}";
			return Document.parse(projectionJson);
		}
	}

}
