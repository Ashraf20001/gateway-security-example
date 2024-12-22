package com.supercharge.gateway.common.filter.master;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.cbt.supercharge.exception.core.ApplicationException;
import com.cbt.supercharge.transfter.objects.core.dto.FilterOrSortingVo;
import com.supercharge.gateway.common.base.dao.IGatewayMasterMongoFilter;

@Component
@Qualifier("GInFilter")
public class InMongoFilterFactoryGateway implements IGatewayMasterMongoFilter {

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
	@Override
	public Document getFilterPredicate(FilterOrSortingVo filterVo) throws ApplicationException {
		return new Document(filterVo.getColumnName(), new Document("$in", filterVo.getIntgerValueList()));
	}

}
