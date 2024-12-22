package com.supercharge.gateway.common.base.dao;

import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.cbt.supercharge.constants.core.ApplicationConstants;
import com.cbt.supercharge.constants.core.TableConstants;
import com.cbt.supercharge.transfer.objects.common.constants.Constants;
import com.cbt.supercharge.transfter.objects.core.dto.MetaDataVo;
import com.cbt.supercharge.transfter.objects.core.entity.DataRestrictionConfig;
import com.cbt.supercharge.transfter.objects.core.entity.vo.DataRestrictionDto;
import com.cbt.supercharge.utils.core.ApplicationUtils;

public class GatewayPredicateBuilder {

	/**
	 * MongoTemplate
	 */
	@Autowired
	protected MongoTemplate mongoTemplate;

	/**
	 * CustomAggregationBuilder
	 */
	@Autowired
	protected CustomAggregationBuilderGateway customAggregationBuilder;

	/**
	 * Logger
	 */
	private static final Logger logger = LoggerFactory.getLogger(GatewayPredicateBuilder.class);

	/**
	 * @param pipeline
	 * @param dataRestrictionConfigList
	 * @param dataRestrictionDtoList
	 * @param metaMap
	 */
	protected void buildPredicates(List<Document> pipeline, List<DataRestrictionDto> dataRestrictionDtoList,
			Map<String, MetaDataVo> metaMap) {
		for (DataRestrictionDto dataRestriction : dataRestrictionDtoList) {
			MetaDataVo metaData = metaMap.get(dataRestriction.getSelectedFields());
			if (metaData != null && metaData.getMongoFieldName() != null) {
				String[] split = ApplicationUtils.splitValue(metaData.getMongoFieldName(),
						ApplicationConstants.DOT_REGEX);
				buildPredicates(dataRestriction, metaData, null, split, pipeline,
						split.length > 1 ? true : false);
			}
		}
		logger.info("document query >>>>>>>>>> " + pipeline);
	}

	/**
	 * @param dataRestriction
	 * @param metaData
	 * @param dataConfig
	 * @param split
	 * @param pipeline
	 * @param isArray
	 */
	private void buildPredicates(DataRestrictionDto dataRestriction, MetaDataVo metaData, DataRestrictionConfig dataConfig,
			String[] split, List<Document> pipeline, boolean isArray) {
		Document condition = new Document();
		switch (metaData.getColumnType()) {
		case Constants.REFERENCE:
		case Constants.DROP_DOWN:
			if (isArray) {
				ObjectId objectId = new ObjectId(dataRestriction.getValue().toString());
				Document referenceDocument = new Document(ApplicationConstants.$REF, dataConfig.getFrom())
						.append(TableConstants.DOLLAR_ID, objectId);
				buildPredicateForArrayOfObjects(dataRestriction, split, referenceDocument, condition);
			} else {
				if (!ApplicationUtils.isValidateObject(dataRestriction.getValue())) {
					logger.error("invalid data restriction value for field ID  :  " + metaData.get_id());
					break;
				}
				dataRestriction.setValue(new ObjectId(dataRestriction.getValue().toString()));
				buildMatchDocumentForFlatObject(dataRestriction, condition, metaData.getMongoFieldName() + TableConstants.ID_$);
			}
			break;
		case Constants.TEXT_TYPE:
		case Constants.FIELD_NUMBER:
		case Constants.DECIMAL:
		case Constants.DATE:
			if (isArray) {
				buildPredicateForArrayOfObjects(dataRestriction, split, dataRestriction.getValue(), condition);
			} else {
				buildMatchDocumentForFlatObject(dataRestriction, condition, metaData.getMongoFieldName());
			}
			break;
		default:
			System.err.println("invalid column type");
			break;
		}
		pipeline.add(new Document(TableConstants.$MATCH, condition));
	}

	/**
	 * @param dataRestriction
	 * @param split
	 * @param condition
	 */
	private void buildPredicateForArrayOfObjects(DataRestrictionDto dataRestriction, String[] split, Object value,
			Document condition) {
		switch (dataRestriction.getCondition()) {
		case 1:
			condition.append(split[0], new Document(TableConstants.$NOT, new Document(TableConstants.$ELE_MATCH,
					new Document(split[1], new Document(TableConstants.$NE, value)))));
			break;
		case 2:
			condition.append(split[0], new Document(TableConstants.$ELE_MATCH,
					new Document(split[1], new Document(TableConstants.$EQ, value))));
			break;
		default:
			throw new IllegalArgumentException("invalid condition : " + dataRestriction.getCondition());
		}
	}

	/**
	 * Builds a $match document based on the condition in DataRestrictionDto.
	 */
	private void buildMatchDocumentForFlatObject(DataRestrictionDto object, Document condition, String fieldName) {
		switch (object.getCondition()) {
		case 1:
			condition.append(fieldName, new Document(TableConstants.$NE, object.getValue()));
			break;
		case 2:
			condition.append(fieldName, new Document(TableConstants.$EQ, object.getValue()));
			break;
		default:
			throw new IllegalArgumentException("invalid condition : " + object.getCondition());
		}
	}

	/**
	 * @param selectedFields
	 * @return
	 */
	protected List<MetaDataVo> fetchMetaData(List<String> selectedFields) {
		AggregationOperation matchOperation = Aggregation.match(Criteria.where(TableConstants._ID).in(selectedFields));
		Document columnTypeLkp = new Document(ApplicationConstants.MONGO_FROM, TableConstants.COLUMN_TYPE)
				.append(ApplicationConstants.MONGO_LOCALFIELD, TableConstants.COLUMNTYPE_ID)
				.append(ApplicationConstants.MONGO_FORIEGNFIELD, TableConstants._ID)
				.append(ApplicationConstants.MONGO_ALAIS, TableConstants.COLUMN_TYPE);
		AggregationOperation clmTypeOperation = customAggregationBuilder.buildLookUp(columnTypeLkp);
		AggregationOperation unWindOperation = Aggregation.unwind(TableConstants.$CLM_TYPE);
		AggregationOperation projectOperation = Aggregation
				.project(TableConstants.MONGO_FIELD_NAME, TableConstants.DISPLAYNAME)
				.andExpression(TableConstants.TOSTRING_ID).as(TableConstants._ID).and(TableConstants.CLM_TYPE_NAME)
				.as(TableConstants.COLUMNTYPE);
		Aggregation aggregation = Aggregation.newAggregation(matchOperation, clmTypeOperation, unWindOperation,
				projectOperation);
		List<MetaDataVo> metaList = (List<MetaDataVo>) mongoTemplate
				.aggregate(aggregation, TableConstants.SUB_FILE_META_DATA, MetaDataVo.class).getMappedResults();
		return metaList;
	}

	/**
	 * @param metaMap
	 * @return
	 */
	protected List<DataRestrictionConfig> fetchRestrictionConfig(Map<String, MetaDataVo> metaMap) {
		Query query = new Query();
		List<String> idMap = metaMap.entrySet().stream().map(x -> x.getValue().get_id()).toList();
		query.addCriteria(Criteria.where(TableConstants.METAID).in(idMap));
		List<DataRestrictionConfig> dataRestrictionConfigList = mongoTemplate.find(query, DataRestrictionConfig.class);
		return dataRestrictionConfigList;
	}

}
