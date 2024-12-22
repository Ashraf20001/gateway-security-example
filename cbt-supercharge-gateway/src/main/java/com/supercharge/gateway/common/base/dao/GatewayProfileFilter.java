/*
 * @author codeboard
 */
package com.supercharge.gateway.common.base.dao;

import java.util.List;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.stereotype.Component;

import com.cbt.supercharge.constants.core.ApplicationConstants;
import com.cbt.supercharge.transfer.objects.entity.Role;
import com.cbt.supercharge.transfer.objects.entity.UserAndInstitutionLinking;
import com.cbt.supercharge.transfer.objects.entity.UserProfile;
import com.cbt.supercharge.transfter.objects.core.entity.vo.DataRestrictionDto;
import com.cbt.supercharge.transfter.objects.core.holder.InstitutionUserHolder;
import com.cbt.supercharge.utils.core.ApplicationUtils;
import com.mongodb.DBRef;
import com.supercharge.gateway.enums.GatewayFilterNameEnum;

/**
 * The Class ProfileFilter.
 */
@Component
@Qualifier("gprofileFilter")
public class GatewayProfileFilter implements GatewayDataFilter {

	/**
	 * The data filter registy.
	 */
	@Autowired
	private GatewayDataFilterRegistry dataFilterRegisty;

	Logger logger = LoggerFactory.getLogger(GatewayProfileFilter.class);

	/**
	 * Gets the filter.
	 *
	 * @param root the root
	 * @param key  the key
	 * @return the filter
	 */
	@Override
	public Predicate getFilter(Root<?> root, Object key) {
		List<String> userMappedInstitutions = InstitutionUserHolder.getUserInstitutionList();

		String[] registryNames = dataFilterRegisty.getDataFilters(key).get(GatewayFilterNameEnum.PROFILE_FILTER)
				.split(ApplicationConstants.COLON_REGEX);

		Predicate predicate = null;
		if (registryNames[0].equals(ApplicationConstants.USER)) {
			Join<UserProfile, UserAndInstitutionLinking> userInstitutionRoot = root.join("userAndInstitutionLinking",
					JoinType.INNER);
			Path<String> expression = userInstitutionRoot.get("institution").get("identity");
			predicate = getFilterPredicate(expression, userMappedInstitutions);
		} else if (registryNames[0].equals(ApplicationConstants.ROLE)) {
			Join<Role, UserAndInstitutionLinking> roleInstitutionRoot = root.join("userAndInstitutionLinking",
					JoinType.INNER);
			Path<String> expression = roleInstitutionRoot.get("institution").get("identity");
			predicate = getFilterPredicate(expression, userMappedInstitutions);
		}
		return predicate;
	}

	private Predicate getFilterPredicate(Path<String> expression, List<String> institutionList) {
		Predicate predicate = null;
		if (ApplicationUtils.isValidList(institutionList)) {
			predicate = expression.in(institutionList);
		}
		return predicate;
	}

	/**
	 * @param key
	 * @param mappedBranches
	 * @return
	 */
	@Override
	public List<Document> genericFilterUsingDocuments(Object key, List<DBRef> mappedBranches, String tableName,
			List<Document> pipeline) {
		return null;
	}

	/**
	 * @param root
	 * @param key
	 * @return
	 */
	@Override
	public List<AggregationOperation> genericFilterUsingAggregation(Object key, List<DBRef> mappedBranches,
			String tableName, List<AggregationOperation> aggregationOperationList) {
		return null;
	}

	/**
	 * @param key
	 * @param aggregationOperations
	 * @param dataRestrictionDto
	 * @return
	 */
	@Override
	public List<AggregationOperation> genericFilterForCustomer(Object key,
			List<AggregationOperation> aggregationOperations, List<DataRestrictionDto> dataRestrictionDto) {
		return null;
	}

	@Override
	public List<Document> getFilterQuery(List<Document> pipeline, String collectionName,
			List<DataRestrictionDto> dataRestrictionDto, Object hintMap) {
		// TODO Auto-generated method stub
		return null;
	}

}
