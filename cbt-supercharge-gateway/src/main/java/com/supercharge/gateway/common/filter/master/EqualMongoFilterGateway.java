/*
 * @author codeboard
 */
package com.supercharge.gateway.common.filter.master;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.cbt.supercharge.exception.core.ApplicationException;
import com.cbt.supercharge.transfter.objects.core.dto.FilterOrSortingVo;
import com.supercharge.gateway.common.base.dao.DataTypeConvertorGateway;
import com.supercharge.gateway.common.base.dao.IGatewayMasterMongoFilter;

/**
 * The Class EqualFilter.
 */
@Service
@Qualifier("GEqualMongoFilter")
public class EqualMongoFilterGateway implements IGatewayMasterMongoFilter {

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
	public Document getFilterPredicate(FilterOrSortingVo filterVo) throws ApplicationException {
		Object obj = dataTypeConvertor.converToRealDataType(filterVo.getValue(), filterVo.getType());
		return new Document(filterVo.getColumnName(), obj);
	}

}
