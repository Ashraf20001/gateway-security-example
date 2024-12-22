package com.supercharge.gateway.common.base.dao;

import org.bson.Document;
import org.springframework.stereotype.Component;


@Component
public class CustomAggregationBuilderGateway {
	
	
	private static final String MATCH = "$match";
	private static final String GROUP = "$group";
	private static final String LOOKUP = "$lookup";
	private static final String LIMIT = "$limit";
	private static final String SKIP = "$skip";
	private static final String SORT = "$sort";
	private static final String PROJECT = "$project";
	private static final String UNWIND = "$unwind";
	private static final String ADDFILEDS = "$addFields";
	private static final String SET = "$set";
	private static final String REPLACE = "$replaceRoot";
	
	public CustomAggregationOperationGateway buildLookUp(Document lookupQuiery){
		return getAggregation( LOOKUP, lookupQuiery);
	}
	
	public CustomAggregationOperationGateway buildGroup(Document groupQuiery){
		return getAggregation( GROUP, groupQuiery);
	}
	
	public CustomAggregationOperationGateway buildFilter(Document filterQuiery){
		return getAggregation( MATCH, filterQuiery);
	}
	
	public CustomAggregationOperationGateway buildLimt(Integer limit){
		return getAggregation( LIMIT, limit);
	}
	
	public CustomAggregationOperationGateway buildSkip(Integer skip){
		return getAggregation( SKIP, skip);
	}

	public CustomAggregationOperationGateway buildSort(Document sortQuery){
		return getAggregation( SORT, sortQuery);
	}
	
	public CustomAggregationOperationGateway buildProject(Document projectQuery){
		return getAggregation( PROJECT, projectQuery);
	}
	
	public CustomAggregationOperationGateway buildCount(String totalQuery){
		return getAggregation( "$count", totalQuery);
	}
	
	public CustomAggregationOperationGateway buildUnwind(Object unwindObject) {
		return getAggregation(UNWIND, unwindObject);
	}
	
	public CustomAggregationOperationGateway buildAddFields(Object addFieldsObject) {
		return getAggregation(ADDFILEDS, addFieldsObject);
	}
	
	public CustomAggregationOperationGateway buildSet(Document setQuery){
		return getAggregation(SET, setQuery);
	}
	
	public CustomAggregationOperationGateway buildReplace(Document replaceQuery){
		return getAggregation(REPLACE, replaceQuery);
	}
	
	public CustomAggregationOperationGateway getAggregation(String aggrigator, Object aggrigatorObject){
		
		if(aggrigatorObject == null) {
			return null;
		}
		return new CustomAggregationOperationGateway( new Document( aggrigator, aggrigatorObject ));
	}
	
}
