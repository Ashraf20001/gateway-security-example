/*
 * @author codeboard
 */
package com.supercharge.gateway.common.base.dao;

import java.util.EnumMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.supercharge.gateway.enums.GatewayFilterNameEnum;

/**
 * A factory for creating DataFilter objects.
 */
@Component
public class GatewayDataFilterFactory {

	/**
	 * The filter map.
	 */
	private Map<GatewayFilterNameEnum, GatewayDataFilter> filterMap = null;

	/**
	 * The profile filiter.	
	 */
	@Autowired
	@Qualifier("gprofileFilter")
	private GatewayDataFilter profileFiliter;
	
	/**
	 * The profile filiter.
	 */
	@Autowired
	@Qualifier("gprofileFilterMongo")
	private GatewayDataFilter profileFiliterMongo;
	
	/**
	 * The customer filiter.
	 */
	@Autowired
	@Qualifier("gcustomerFilter")
	private GatewayDataFilter customerFilter;
	
	/**
	 * The account filiter.
	 */
	@Autowired
	@Qualifier("gaccountFilter")
	private GatewayDataFilter accountFilter;

	/**
	 * Builds the map.
	 */
	@PostConstruct
	private void buildMap() {
		filterMap = new EnumMap<>(GatewayFilterNameEnum.class);
		filterMap.put(GatewayFilterNameEnum.PROFILE_FILTER, profileFiliter);
		filterMap.put(GatewayFilterNameEnum.GENERIC_FILTER, profileFiliterMongo);
		filterMap.put(GatewayFilterNameEnum.GENERIC_FILTER_CUSTOMER, profileFiliterMongo);
		filterMap.put(GatewayFilterNameEnum.CUSTOMER_FILTER, customerFilter);
		filterMap.put(GatewayFilterNameEnum.ACCOUNT_FILTER, accountFilter);
	}

	/**
	 * Gets the filter by name.
	 *
	 * @param filterName the filter name
	 * @return the filter by name
	 */
	public GatewayDataFilter getFilterByName(GatewayFilterNameEnum filterName) {
		return filterMap.get(filterName);
	}
}