package com.supercharge.gateway.common.filter.master;

import java.util.regex.Pattern;

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
@Qualifier("GEndWithMongoFilter")
public class EndWithMongoFilterGateway implements IGatewayMasterMongoFilter {


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
		Pattern pattern = Pattern.compile(obj.toString() + ApplicationConstants.END_WITH_REGEX,
				Pattern.CASE_INSENSITIVE);
		return new Document(filterVo.getColumnName(), new Document(ApplicationConstants.REGEX, pattern.pattern())
				.append(ApplicationConstants.OPTIONS, ApplicationConstants.I));
	}

}
