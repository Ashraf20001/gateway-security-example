package com.supercharge.gateway.common.base.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.stereotype.Component;

import com.cbt.supercharge.constants.core.TableConstants;
import com.cbt.supercharge.transfter.objects.core.dto.MetaDataVo;
import com.cbt.supercharge.transfter.objects.core.entity.vo.DataRestrictionDto;
import com.cbt.supercharge.utils.core.ApplicationUtils;
import com.mongodb.DBRef;

@Component
@Qualifier("gcustomerFilter")
public class GatewayCustomerFilter extends GatewayPredicateBuilder implements GatewayDataFilter {

	/**
	 * @param root
	 * @param key
	 */
	@Override
	public Predicate getFilter(Root<?> root, Object key) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param pipeline
	 * @param collectionName
	 * @param dataRestrictionDtoList
	 */
	@Override
	public List<Document> getFilterQuery(List<Document> pipeline, String collectionName,
			List<DataRestrictionDto> dataRestrictionDtoList, Object hintMap) {
		List<String> applicableFields = new ArrayList<>();
		List<DataRestrictionDto> metadataList = new ArrayList<>();
		dataRestrictionDtoList.stream().filter(data -> data.getTargetEntityType().equals(TableConstants.CUSTOMER))
				.forEach(data -> {
					applicableFields.add(data.getSelectedFields());
					metadataList.add(data);
				});
		if (ApplicationUtils.isValidList(applicableFields)) {
			List<MetaDataVo> metaList = fetchMetaData(applicableFields);
			Map<String, MetaDataVo> metaMap = metaList.stream().collect(Collectors.toMap(x -> x.get_id(), meta -> meta));
			buildPredicates(pipeline, metadataList, metaMap);
		}
		return pipeline;
	}

	@Override
	public List<Document> genericFilterUsingDocuments(Object key, List<DBRef> mappedBranches, String tableName,
			List<Document> pipeline) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<AggregationOperation> genericFilterUsingAggregation(Object key, List<DBRef> mappedBranches,
			String tableName, List<AggregationOperation> aggregationOperationList) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<AggregationOperation> genericFilterForCustomer(Object key,
			List<AggregationOperation> aggregationOperations, List<DataRestrictionDto> dataRestrictionDto) {
		// TODO Auto-generated method stub
		return null;
	}

}
