/*
 * @author codeboard
 */
package com.supercharge.gateway.common.base.dao;

import java.util.List;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.bson.Document;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;

import com.cbt.supercharge.constants.core.ApplicationConstants;
import com.cbt.supercharge.transfter.objects.core.entity.vo.DataRestrictionDto;
import com.cbt.supercharge.utils.core.ApplicationUtils;
import com.mongodb.DBRef;
import com.supercharge.gateway.enums.GatewayFilterNameEnum;

/**
 * The Interface DataFilter.
 */
public interface GatewayDataFilter {

	/**
	 * Gets the filter.
	 *
	 * @param root the root
	 * @param key  the key
	 * @return the filter
	 */
	Predicate getFilter(Root<?> root, Object key);

	/**
	 * Gets the predicate.
	 *
	 * @param dataFilterRegisty object to get filter column name
	 * @param root              Root for the entity class do we want to add filters
	 * @param filterNameEnum    which filter to get from data filter registry map
	 * @param args              profile id's to add filters
	 * @param key               entity class object for we want to add filters
	 * @return Predicate to add some restrictions in profile.
	 */
	default Predicate getPredicate(GatewayDataFilterRegistry dataFilterRegisty, Root<?> root,
			GatewayFilterNameEnum filterNameEnum, List<?> args, Object key) {
		String[] referenceNames = dataFilterRegisty.getDataFilters(key).get(filterNameEnum)
				.split(ApplicationConstants.DOT_REGEX);
		Path<String> expression = root.get(referenceNames[0]);
		if (referenceNames.length > 1) {
			expression = expression.get(referenceNames[1]);
		}
		Predicate predicate = null;
		if (ApplicationUtils.isValidList(args)) {
			predicate = expression.in(args);
		} else {
			predicate = expression.in(ApplicationConstants.MINUS_ONE);
		}
		return predicate;
	}

	/**
	 * @param key
	 * @param mappedBranches
	 * @return
	 */
	List<Document> genericFilterUsingDocuments(Object key, List<DBRef> mappedBranches, String tableName,
			List<Document> pipeline);

	/**
	 * @param root
	 * @param key
	 * @return
	 */
	List<AggregationOperation> genericFilterUsingAggregation(Object key, List<DBRef> mappedBranches, String tableName,
			List<AggregationOperation> aggregationOperationList);

	/**
	 * @param key
	 * @param aggregationOperations
	 * @param dataRestrictionDto
	 * @return
	 */
	List<AggregationOperation> genericFilterForCustomer(Object key, List<AggregationOperation> aggregationOperations,
			List<DataRestrictionDto> dataRestrictionDto);

	/**
	 * @param pipeline
	 * @param collectionName
	 * @param dataRestrictionDto
	 * @return
	 */
	List<Document> getFilterQuery(List<Document> pipeline, String collectionName,
			List<DataRestrictionDto> dataRestrictionDto, Object hintMap);

}
