/*
 * @author codeboard
 */
package com.supercharge.gateway.common.filter.master;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.cbt.supercharge.exception.core.ApplicationException;
import com.cbt.supercharge.transfter.objects.core.dto.FilterOrSortingVo;
import com.supercharge.gateway.common.base.dao.IGatewayMasterFilter;

/**
 * The Class SortingFilter.
 */
@Service
@Qualifier("GSortingFilters")
public class SortingFilterGateway implements IGatewayMasterFilter {

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
	public Predicate getFilterPredicate(From<?, ?> root, CriteriaBuilder builder, FilterOrSortingVo filterVo,
			CriteriaQuery<?> criteria) throws ApplicationException {
		String[] columnNames = getColumnNames(filterVo.getColumnName());
		Path<Comparable> path = getPath(root, columnNames);

		if (filterVo.isAscending()) {
			criteria.orderBy(builder.asc(path));
		} else {
			criteria.orderBy(builder.desc(path));
		}

		return null;
	}

}
