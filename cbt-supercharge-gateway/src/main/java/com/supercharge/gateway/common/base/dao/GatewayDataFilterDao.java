package com.supercharge.gateway.common.base.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.cbt.supercharge.constants.core.ApplicationConstants;
import com.cbt.supercharge.constants.core.TableConstants;
import com.cbt.supercharge.transfer.objects.common.entity.SelectOption;
import com.cbt.supercharge.transfer.objects.common.entity.SubFileMetaData;

@Repository
public class GatewayDataFilterDao {

	@Autowired
	CustomAggregationBuilderGateway customAggregationBuilder;

	@Autowired
	MongoTemplate mongoTemplate;

	/**
	 * @param sublistName
	 * @return
	 */
	public List<SubFileMetaData> getSublistMetaDataFileldsByListname(List<String> sublistName) {
		List<Document> pipeline = Arrays.asList(
				new Document(ApplicationConstants.LOOKUP,
						new Document(ApplicationConstants.FROM, TableConstants.SUB_LIST)
								.append(ApplicationConstants.LOCAL_FIELD, TableConstants.SUBLIST_ID)
								.append(ApplicationConstants.FOREIGN_FIELD, TableConstants.MONGO_ID)
								.append(ApplicationConstants.AS, TableConstants.SUBLIST)),
				new Document(ApplicationConstants.MATCH,
						new Document(TableConstants.SUBLIST_NAME, new Document(ApplicationConstants.IN, sublistName))),
				new Document(ApplicationConstants.MATCH, new Document(TableConstants.SUBLIST_ISDELTED, false)),
				new Document(ApplicationConstants.ADD_FIELDS,
						new Document(TableConstants.SUBLIST, new Document(ApplicationConstants.ARRAY_ELEMAT,
								Arrays.asList(ApplicationConstants.SUBLIST_$, 0)))));
		List<Document> results = mongoTemplate.getCollection(TableConstants.SUB_FILE_META_DATA).aggregate(pipeline)
				.into(new ArrayList<>());
		return results.stream().map(this::convertDocumentToEntity).collect(Collectors.toList());
	}

	/**
	 * @param columnNames
	 * @return
	 */
	public List<SubFileMetaData> getSublistMetaDataFilelds(List<String> columnNames) {
		Query query = new Query(Criteria.where("columnName").in(columnNames));
		return mongoTemplate.find(query, SubFileMetaData.class);
	}

	/**
	 * @param objectId
	 * @return
	 */
	public SelectOption getSubfileSelectOption(ObjectId objectId) {
		Query query = new Query(Criteria.where(TableConstants.MONGO_ID).is(objectId));
		return mongoTemplate.findOne(query, SelectOption.class);
	}

	/**
	 * @param document
	 * @return
	 */
	public SubFileMetaData convertDocumentToEntity(Document document) {
		return mongoTemplate.getConverter().read(SubFileMetaData.class, document);
	}
}
