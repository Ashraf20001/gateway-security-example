/*
 * @author codeboard
 */
package com.supercharge.gateway.common.base.dao;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.supercharge.gateway.enums.GatewayFilterNameEnum;

/**
 * The Class DataFilterRegistry.
 */
@Component
public class GatewayDataFilterRegistry {

	/**
	 * The Constant filterRegisty.
	 */
	private static final Map<Object, Map<GatewayFilterNameEnum, String>> filterRegisty = new HashMap<>();
	
	/**
	 * FILTERREGISTYMONGO
	 */
	private static final Map<Object, Map<GatewayFilterNameEnum, List<String>>> filterRegistyMongo = new HashMap<>();
	
	/**
	 * Adds the.
	 *
	 * @param key             current object
	 * @param filterNameEnum  which filter we want
	 * @param referenceColumn the reference column
	 */
	public void add(Object key, GatewayFilterNameEnum filterNameEnum, String referenceColumn) {
		if (key == null) {
			return;
		}

		if (filterRegisty.get(key) == null) {
			filterRegisty.put(key, new EnumMap<>(GatewayFilterNameEnum.class));
		}

		filterRegisty.get(key).put(filterNameEnum, referenceColumn);
	}
	
	/**
	 * @param key
	 * @param filterNameEnum
	 * @param reference
	 * @param localField
	 */
	public void addMongo(Object key, GatewayFilterNameEnum filterNameEnum, List<String> columnDetails) {
		if (key == null) {
			return;
		}
		
		if (filterRegistyMongo.get(key) == null) {
			filterRegistyMongo.put(key, new EnumMap<>(GatewayFilterNameEnum.class));
		}
		
		filterRegistyMongo.get(key).put(filterNameEnum,columnDetails);
	}

	/**
	 * Gets the data filters.
	 *
	 * @param key the key
	 * @return the data filters
	 */
	public Map<GatewayFilterNameEnum, String> getDataFilters(Object key) {
		return filterRegisty.get(key);
	}
	
	/**
	 * @param key
	 * @return
	 */
	public Map<GatewayFilterNameEnum, List<String>> getDataFiltersMongo(Object key) {
		return filterRegistyMongo.get(key);
	}

}
