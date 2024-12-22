package com.supercharge.gateway.common.filter.master;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.cbt.supercharge.constants.core.ApplicationConstants;
import com.cbt.supercharge.exception.core.ApplicationException;
import com.cbt.supercharge.transfter.objects.core.dto.FilterOrSortingVo;
import com.supercharge.gateway.common.base.dao.DataTypeConvertorGateway;
import com.supercharge.gateway.common.base.dao.IGatewayMasterMongoFilter;

@Service
@Qualifier("GGTEMongoFilter")
public class GTEMongoFilterGateway implements IGatewayMasterMongoFilter {

	/**
	 * The data type convertor.
	 */
	@Autowired
	private DataTypeConvertorGateway dataTypeConvertor;
	
	/**
	 * Gets the filter predicate.
	 *
	 * @param filterVo the filter vo
	 * @throws ApplicationException the application exception
	 */
	@Override
	public Document getFilterPredicate(FilterOrSortingVo filterVo) throws ApplicationException {
		Object obj = dataTypeConvertor.converToRealDataType(filterVo.getValue(), filterVo.getType());
		return new Document(filterVo.getColumnName(), new Document(ApplicationConstants.GREATERTHAN_EQUALSTO, obj));
	}

}
