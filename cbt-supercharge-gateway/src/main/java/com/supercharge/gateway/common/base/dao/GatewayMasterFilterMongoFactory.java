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
public class GatewayMasterFilterMongoFactory {


	/**
	 * The equals filter.
	 */
	@Autowired
	@Qualifier("GEqualMongoFilter")
	private IGatewayMasterMongoFilter equalsFilter;
	
	/**
	 * The Like Mongo filter.
	 */
	@Autowired
	@Qualifier("GLikeMongoFilter")
	private IGatewayMasterMongoFilter likeMongoFilter;

	/**
	 * The sorting filters.
	 */
	@Autowired
	@Qualifier("SortingFilters")
	private IGatewayMasterMongoFilter sortingFilters;


	/**
	 * The between filters.
	 */
	@Autowired
	@Qualifier("GBWMongoFilter")
	private IGatewayMasterMongoFilter betweenFilters;
	
	/**
	 * The NotEqual Filter
	 */
	@Autowired
	@Qualifier("GNotEqualMongoFilter")
	private IGatewayMasterMongoFilter notEqualFilter;
	
	/**
	 * The StartWith Filter
	 */
	@Autowired
	@Qualifier("GStartWithMongoFilter")
	private IGatewayMasterMongoFilter startWithFilter;
	
	/**
	 * The EndWith Filter
	 */
	@Autowired
	@Qualifier("GEndWithMongoFilter")
	private IGatewayMasterMongoFilter endWithFilter;
	
	/**
	 * The GTMongo Filter
	 */
	@Autowired
	@Qualifier("GGTMongoFilter")
	private IGatewayMasterMongoFilter gtMongoFilter;
	
	/**
	 * The GTEMongo Filter
	 */
	@Autowired
	@Qualifier("GGTEMongoFilter")
	private IGatewayMasterMongoFilter gteMongoFilter;
	
	/**
	 * The LTMongo Filter
	 */
	@Autowired
	@Qualifier("GLTMongoFilter")
	private IGatewayMasterMongoFilter ltMongoFilter;
	
	/**
	 * The LTEMongo Filter
	 */
	@Autowired
	@Qualifier("GLTEMongoFilter")
	private IGatewayMasterMongoFilter lteMongoFilter;
	
	@Autowired
	@Qualifier("GInFilter")
	private IGatewayMasterMongoFilter inMongoFilter;
	
	/**
	 * The master filter factory map.
	 */
	private Map<String, IGatewayMasterMongoFilter> masterFilterFactoryMap = null;


	/**
	 * Bulid map.
	 */
	@PostConstruct
	public void bulidMap() {
		masterFilterFactoryMap = new HashMap<>();
		masterFilterFactoryMap.put(ApplicationConstants.EQUAL, equalsFilter);
		masterFilterFactoryMap.put(ApplicationConstants.LIKE, likeMongoFilter);
		masterFilterFactoryMap.put(ApplicationConstants.ORDERBY, sortingFilters);
		masterFilterFactoryMap.put(ApplicationConstants.BETWEEN, betweenFilters);
		masterFilterFactoryMap.put(ApplicationConstants.NOT_EQUAL, notEqualFilter);
		masterFilterFactoryMap.put(ApplicationConstants.START_WITH, startWithFilter);
		masterFilterFactoryMap.put(ApplicationConstants.END_WITH, endWithFilter);
		masterFilterFactoryMap.put(ApplicationConstants.CONTAINS, likeMongoFilter);
		masterFilterFactoryMap.put(ApplicationConstants.GT, gtMongoFilter);
		masterFilterFactoryMap.put(ApplicationConstants.GTE, gteMongoFilter);
		masterFilterFactoryMap.put(ApplicationConstants.LT, ltMongoFilter);
		masterFilterFactoryMap.put(ApplicationConstants.LTE, lteMongoFilter);
		masterFilterFactoryMap.put(ApplicationConstants.in, inMongoFilter);
	}

	/**
	 * Gets the filter by name.
	 *
	 * @param filterName the filter name
	 * @return the filter by name
	 */
	public IGatewayMasterMongoFilter getFilterByName(String filterName) {
		return masterFilterFactoryMap.get(filterName);
	}

}
