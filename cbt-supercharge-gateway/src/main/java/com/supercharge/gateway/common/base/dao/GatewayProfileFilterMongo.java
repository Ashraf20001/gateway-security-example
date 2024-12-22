package com.supercharge.gateway.common.base.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.stereotype.Component;

import com.cbt.supercharge.constants.core.ApplicationConstants;
import com.cbt.supercharge.constants.core.TableConstants;
import com.cbt.supercharge.transfer.objects.common.entity.SelectOption;
import com.cbt.supercharge.transfer.objects.common.entity.SubFileMetaData;
import com.cbt.supercharge.transfer.objects.common.entity.SubList;
import com.cbt.supercharge.transfter.objects.core.dto.LookUpDto;
import com.cbt.supercharge.transfter.objects.core.entity.vo.DataRestrictionDto;
import com.cbt.supercharge.utils.core.ApplicationUtils;
import com.mongodb.DBRef;
import com.supercharge.gateway.enums.GatewayFilterNameEnum;

@Component
@Qualifier("gprofileFilterMongo")
public class GatewayProfileFilterMongo implements GatewayDataFilter {

	/**
	 * The data filter registy.
	 */
	@Autowired
	private GatewayDataFilterRegistry dataFilterRegisty;
	
	/**
	 * CUSTOMAGGREGATIONBUILDER
	 */
	@Autowired
	private CustomAggregationBuilderGateway customAggregationBuilder;
	
	/**
	 * DATAFILTERDAO
	 */
	@Autowired
	private GatewayDataFilterDao dataFilterDao;
	
	/**
	 * Gets the filter.
	 *
	 * @param root the root
	 * @param key  the key
	 * @return the filter
	 */
	@Override
	public Predicate getFilter(Root<?> root, Object key) {
		return null;
	}

	/**
	 * @param root
	 * @param key
	 * @return
	 */
	@Override
	public List<Document> genericFilterUsingDocuments(Object key, List<DBRef> mappedBranches, String tableName,
			List<Document> pipeline) {
		List<ObjectId> objectIds = buildObjectId(mappedBranches);
		List<String> registryNames = dataFilterRegisty.getDataFiltersMongo(key)
				.get(GatewayFilterNameEnum.GENERIC_FILTER);
		List<Document> dataFilterPipeline = new ArrayList<>(pipeline);
		if (ApplicationConstants.USER.equals(registryNames.get(0))) {
			dataFilterPipeline.addAll(Arrays.asList(
					new Document("$lookup",
							new Document(ApplicationConstants.MONGO_FROM, tableName)
									.append(ApplicationConstants.MONGO_LOCALFIELD, registryNames.get(1))
									.append(ApplicationConstants.MONGO_FORIEGNFIELD, TableConstants.MONGO_ID)
									.append(ApplicationConstants.MONGO_ALAIS, ApplicationConstants.BRANCHES)),
					new Document("$addFields",
							new Document(ApplicationConstants.BRANCHES,
									new Document(TableConstants.ARRAY_ELEMENT,
											Arrays.asList(ApplicationConstants.$BRANCHES, 0)))),
					new Document("$match",
							new Document(ApplicationConstants.BRANCHES_ID, new Document("$in", objectIds)))));
		}
		return dataFilterPipeline;
	}
	
	/**
	 * @param root
	 * @param key
	 * @return
	 */
	@Override
	public List<AggregationOperation> genericFilterUsingAggregation(Object key, List<DBRef> mappedBranches, String tableName,
			List<AggregationOperation> aggregationOperations) {
		List<ObjectId> objectIds = buildObjectId(mappedBranches);
		List<String> registryNames = dataFilterRegisty.getDataFiltersMongo(key)
				.get(GatewayFilterNameEnum.GENERIC_FILTER);
		if (ApplicationConstants.USER.equals(registryNames.get(0))) {
			Document lookupStage = new Document(ApplicationConstants.MONGO_FROM, tableName)
					.append(ApplicationConstants.MONGO_LOCALFIELD, registryNames.get(1))
					.append(ApplicationConstants.MONGO_FORIEGNFIELD, TableConstants.MONGO_ID)
					.append(ApplicationConstants.MONGO_ALAIS, ApplicationConstants.BRANCHES);
			Document addFiledDocument = new Document(ApplicationConstants.BRANCHES,
					new Document(TableConstants.ARRAY_ELEMENT, Arrays.asList(ApplicationConstants.$BRANCHES, 0)));

			Document matchDocument = new Document(ApplicationConstants.BRANCHES_ID, new Document("$in", objectIds));

			AggregationOperation lookUpDocument = customAggregationBuilder.buildLookUp(lookupStage);
			AggregationOperation addFields = customAggregationBuilder.buildAddFields(addFiledDocument);
			AggregationOperation match = customAggregationBuilder.buildFilter(matchDocument);
			aggregationOperations.add(lookUpDocument);
			aggregationOperations.add(addFields);
			aggregationOperations.add(match);
		}
		return aggregationOperations;
	}

	/**
	 * @param key
	 * @param aggregationOperations
	 * @param dataRestrictionDto
	 * @return
	 */
	public List<AggregationOperation> genericFilterForCustomer(Object key,
			List<AggregationOperation> aggregationOperations, List<DataRestrictionDto> dataRestrictionDto) {
		List<String> registryNames = dataFilterRegisty.getDataFiltersMongo(key)
				.get(GatewayFilterNameEnum.GENERIC_FILTER_CUSTOMER);
		List<SubFileMetaData> sublistMetaDataFilelds = dataFilterDao.getSublistMetaDataFilelds(registryNames);
		for (SubFileMetaData metaData : sublistMetaDataFilelds) {
			DataRestrictionDto restrictionDetail = dataRestrictionDto.stream()
					.filter(a -> a.getSelectedFields().equals(metaData.get_id())
							|| a.getSelectedFields().equals(metaData.getColumnName()))
					.findFirst().orElse(null);
			if (ApplicationUtils.isValidateObject(metaData.getColumnType())
					&& metaData.getColumnType().getColumnTypeName().equals("Reference")
					&& ApplicationUtils.isValidateObject(restrictionDetail)) {
				SubFileMetaData refMetaData = metaData.getReferenceMetaData();
				SubList subList = refMetaData.getSubList();
				String from = subList.getCollectionName();
				String localField = metaData.getMongoFieldName();
				buildDtoForReference(aggregationOperations, restrictionDetail, subList, from, localField);
			} else if (ApplicationUtils.isValidateObject(metaData.getColumnType())
					&& (metaData.getColumnType().getColumnTypeName().equals("DropDown")
							|| metaData.getColumnType().getColumnTypeName().equals("AutoComplete"))
					&& ApplicationUtils.isValidateObject(restrictionDetail)) {
				String value = (String) restrictionDetail.getValue();
				ObjectId objectId = new ObjectId(value);
				SelectOption subfileSelectOption = dataFilterDao.getSubfileSelectOption(objectId);
				restrictionDetail.setValue(subfileSelectOption.getOptionName());
				restrictionDetail.setSelectedFields("optionName");
				String from = TableConstants.SUB_FILE_SELECT_OPTION;
				String localField = metaData.get_id() + ApplicationConstants.DOT + ".$id";
				buildDtoForSelectoption(aggregationOperations, restrictionDetail, from, localField);
			} else {
				if (ApplicationUtils.isValidateObject(restrictionDetail)) {
					buildMatchDetails(restrictionDetail, aggregationOperations, "");
				}
			}
		}
		return aggregationOperations;
	}

	/**
	 * @param aggregationOperations
	 * @param restrictionDetail
	 * @param subList
	 * @param from
	 * @param localField
	 */
	private void buildDtoForReference(List<AggregationOperation> aggregationOperations,
			DataRestrictionDto restrictionDetail, SubList subList, String from, String localField) {
		LookUpDto lookUpDto = lookUpDtoWrapper(from, localField, subList.getCollectionName());
		buildLookup(lookUpDto, aggregationOperations);
		buildMatchDetails(restrictionDetail, aggregationOperations, lookUpDto.getAlias()+ ApplicationConstants.DOT);
	}

	/**
	 * @param aggregationOperations
	 * @param restrictionDetail
	 * @param from
	 * @param localField
	 */
	private void buildDtoForSelectoption(List<AggregationOperation> aggregationOperations, DataRestrictionDto restrictionDetail,
			String from, String localField) {
		LookUpDto lookUpDto = lookUpDtoWrapper(from, localField, from);
		buildLookup(lookUpDto, aggregationOperations);
		buildMatchDetails(restrictionDetail, aggregationOperations, lookUpDto.getAlias()+ ApplicationConstants.DOT);
	}

	/**
	 * @param restrictionDetail
	 * @param aggregationOperations
	 * @param alias
	 */
	private void buildMatchDetails(DataRestrictionDto restrictionDetail,
			List<AggregationOperation> aggregationOperations, String alias) {
		if (restrictionDetail.getCondition().equals(1)) {
			Document matchDocument = new Document(
					alias + restrictionDetail.getSelectedFields(),
					restrictionDetail.getValue());
			AggregationOperation matchOperation = customAggregationBuilder.buildFilter(matchDocument);
			aggregationOperations.add(matchOperation);
		} else {
			Document matchDocument = new Document(
					alias + restrictionDetail.getSelectedFields(),
					new Document(ApplicationConstants.NOT_IN_MONGO, restrictionDetail.getValue()));
			AggregationOperation matchOperation = customAggregationBuilder.buildFilter(matchDocument);
			aggregationOperations.add(matchOperation);
		}
	}

	/**
	 * @param lookUpCollection
	 * @param localField
	 * @param alias
	 * @return
	 */
	private LookUpDto lookUpDtoWrapper(String lookUpCollection, String localField, String alias) {
		LookUpDto tempateTypeLookUpDto = new LookUpDto();
		tempateTypeLookUpDto.setLookUpCollection(lookUpCollection);
		tempateTypeLookUpDto.setLocalField(localField);
		tempateTypeLookUpDto.setForeignField(TableConstants._ID);
		tempateTypeLookUpDto.setAlias(alias);
		return tempateTypeLookUpDto;
	}

	/**
	 * @param lookUpDto
	 * @param aggregationOperations
	 */
	protected void buildLookup(LookUpDto lookUpDto, List<AggregationOperation> aggregationOperations) {
		Document lookUpDocument = new Document(ApplicationConstants.MONGO_FROM, lookUpDto.getLookUpCollection())
				.append(ApplicationConstants.MONGO_LOCALFIELD, lookUpDto.getLocalField())
				.append(ApplicationConstants.MONGO_FORIEGNFIELD, lookUpDto.getForeignField())
				.append(ApplicationConstants.MONGO_ALAIS, lookUpDto.getAlias());
		AggregationOperation lookUpAggregationOperation = customAggregationBuilder.buildLookUp(lookUpDocument);
		AggregationOperation unWindAggregationOperation = Aggregation
				.unwind(ApplicationConstants.DOLLAR + lookUpDto.getAlias());
		aggregationOperations.add(lookUpAggregationOperation);
		aggregationOperations.add(unWindAggregationOperation);
	}
	
	/**
	 * @param identityList
	 * @return
	 */
	private List<ObjectId> buildObjectId(List<DBRef> identityList) {
		return identityList.stream().map(a -> a.getId().toString()).collect(Collectors.toList()).stream()
				.map(ObjectId::new).collect(Collectors.toList());
	}

	/**
	 * @param dataFilterRegisty
	 */
	public void setDataFilterRegisty(GatewayDataFilterRegistry dataFilterRegisty) {
		this.dataFilterRegisty = dataFilterRegisty;
	}

	/**
	 * @param dataFilterDao
	 */
	public void setDataFilterDao(GatewayDataFilterDao dataFilterDao) {
		this.dataFilterDao = dataFilterDao;
	}

	@Override
	public List<Document> getFilterQuery(List<Document> pipeline, String collectionName,
			List<DataRestrictionDto> dataRestrictionDto, Object hintMap) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
