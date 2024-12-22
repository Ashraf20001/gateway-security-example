package com.supercharge.gateway.filters;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;



@Component
public class AmlCache {
	
	private static final Logger logger = LoggerFactory.getLogger(AmlCache.class);

	private static Map<String, Map<String, String>> roleApiMaps;

	public static Map<String, Map<String, String>> getRoleApiMaps() {
		return roleApiMaps;
	}
	
	private enum CacheItem {
		roleApiMaps
	}

	public static void setRoleApiMaps(Map<String, Map<String, String>> map) {
		AmlCache.roleApiMaps = map;
	}
	
	
	public void porkCache(String cacheName) {

		logger.info("Pork Cache method invoked .....",cacheName);
		
		if (CacheItem.roleApiMaps.name().equalsIgnoreCase(cacheName)) {
			setRoleApiMaps(null);
			logger.info("Cache Cleared successfully ....."+cacheName);
		}
	}
	
	public void clearCache() {
		AmlCache.roleApiMaps = null;
	}
	
}

