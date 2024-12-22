/*
 * @author codeboard
 */
package com.supercharge.gateway.common.base.dao;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.cbt.supercharge.constants.core.ApplicationConstants;

/**
 * A factory for creating MasterFilter objects.
 */
@Component
public class GatewayMasterFilterFactory {

	/**
	 * The bw filter.
	 */
	@Autowired
	@Qualifier("GBWFilter")
	private IGatewayMasterFilter bwFilter;

	/**
	 * The equals filter.
	 */
	@Autowired
	@Qualifier("GEqualFilter")
	private IGatewayMasterFilter equalsFilter;

	/**
	 * The gte filter.
	 */
	@Autowired
	@Qualifier("GGTEFilter")
	private IGatewayMasterFilter gteFilter;

	/**
	 * The gt filter.
	 */
	@Autowired
	@Qualifier("GGTFilter")
	private IGatewayMasterFilter gtFilter;

	/**
	 * The like filter.
	 */
	@Autowired
	@Qualifier("GLikeFilter")
	private IGatewayMasterFilter likeFilter;

	/**
	 * The lte filter.
	 */
	@Autowired
	@Qualifier("GLTEFilter")
	private IGatewayMasterFilter lteFilter;

	/**
	 * The lt filter.
	 */
	@Autowired
	@Qualifier("GLTFilter")
	private IGatewayMasterFilter ltFilter;

	/**
	 * The master filter factory map.
	 */
	private Map<String, IGatewayMasterFilter> masterFilterFactoryMap = null;

	/**
	 * The round off filter.
	 */
	@Autowired
	@Qualifier("GRoundOffFilter")
	private IGatewayMasterFilter roundOffFilter;

	/**
	 * The sorting filters.
	 */
	@Autowired
	@Qualifier("GSortingFilters")
	private IGatewayMasterFilter sortingFilters;

	/**
	 * Bulid map.
	 */
	@PostConstruct
	public void bulidMap() {
		masterFilterFactoryMap = new HashMap<>();
		masterFilterFactoryMap.put(ApplicationConstants.EQUAL, equalsFilter);
		masterFilterFactoryMap.put(ApplicationConstants.LIKE, likeFilter);
		masterFilterFactoryMap.put(ApplicationConstants.GTE, gteFilter);
		masterFilterFactoryMap.put(ApplicationConstants.GT, gtFilter);
		masterFilterFactoryMap.put(ApplicationConstants.LTE, lteFilter);
		masterFilterFactoryMap.put(ApplicationConstants.LT, ltFilter);
		masterFilterFactoryMap.put(ApplicationConstants.BETWEEN, bwFilter);
		masterFilterFactoryMap.put(ApplicationConstants.ORDERBY, sortingFilters);
		masterFilterFactoryMap.put(ApplicationConstants.ROUNDOFF, roundOffFilter);
	}

	/**
	 * Gets the filter by name.
	 *
	 * @param filterName the filter name
	 * @return the filter by name
	 */
	public IGatewayMasterFilter getFilterByName(String filterName) {
		return masterFilterFactoryMap.get(filterName);
	}

}
