/*
 * @author codeboard
 */
package com.supercharge.gateway.common.filter.master;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.cbt.supercharge.exception.core.ApplicationException;
import com.cbt.supercharge.transfter.objects.core.dto.FilterOrSortingVo;
import com.supercharge.gateway.common.base.dao.DataTypeConvertorGateway;
import com.supercharge.gateway.common.base.dao.IGatewayMasterFilter;

/**
 * The Class RoundOffFilter.
 */
@Service
@Qualifier("GRoundOffFilter")
public class RoundOffFilterGateway implements IGatewayMasterFilter {

	/**
	 * The data type convertor.
	 */
	@Autowired
	private DataTypeConvertorGateway dataTypeConvertor;

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
		Expression<Comparable> path = getPath(root, columnNames);

		Double val = Double.parseDouble(filterVo.getValue());
		filterVo.setValue(String.format("%.2f", val));

		Comparable com1 = dataTypeConvertor.converToRealDataType((filterVo.getValue()), filterVo.getType());

		Double com1Double = (Double) com1;

		com1 = com1Double - 0.005;
		Comparable com2 = com1Double + 0.004;

		Predicate betWeenFilter = builder.conjunction();
		betWeenFilter.getExpressions().add(builder.lessThanOrEqualTo(path, com2));
		betWeenFilter.getExpressions().add(builder.greaterThanOrEqualTo(path, com1));
		return betWeenFilter;
	}

}
