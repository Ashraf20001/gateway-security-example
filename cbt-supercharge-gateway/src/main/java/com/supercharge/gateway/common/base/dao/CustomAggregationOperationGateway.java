package com.supercharge.gateway.common.base.dao;

import org.bson.Document;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperationContext;

public class CustomAggregationOperationGateway implements AggregationOperation {
    private Document operation;

    public CustomAggregationOperationGateway (Document operation) {
        this.operation = operation;
    }
	@Override
	public Document toDocument(AggregationOperationContext context) {
		return context.getMappedObject(operation);
	}
}