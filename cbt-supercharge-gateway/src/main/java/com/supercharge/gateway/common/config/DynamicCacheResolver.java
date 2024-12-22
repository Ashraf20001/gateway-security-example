package com.supercharge.gateway.common.config;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.cbt.supercharge.exception.core.ApplicationException;
import com.cbt.supercharge.transfer.objects.common.constants.Constants;
import com.cbt.supercharge.transfer.objects.entity.UserProfile;
import com.cbt.supercharge.utils.core.ApplicationUtils;
import com.supercharge.gateway.common.base.service.GatewayBaseService;
import com.supercharge.gateway.common.handlers.CustomException;
import com.supercharge.gateway.common.handlers.UserIdHolder;
import com.supercharge.gateway.filters.TemporaryStorageService;

public class DynamicCacheResolver implements org.springframework.cache.interceptor.CacheResolver {

	/**
	 * RestTemplate
	 */
	@Autowired
	private RestTemplate restTemplate;

	/**
	 * CacheManager
	 */
	private CacheManager cacheManager = null;

	/**
	 * updateCacheData
	 */
	@Value("${bulk.upload.url}")
	private String updateCacheData;

	/**
	 * 
	 * @param cacheManager
	 */
	public DynamicCacheResolver(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	/**
	 * BaseService
	 */
	@Autowired
	GatewayBaseService baseService;
	
	@Autowired
	private TemporaryStorageService temporaryStorageService;

	/**
	 * @return
	 * @throws ApplicationException
	 */
	public UserProfile getLoggedInUser() throws ApplicationException {
		return baseService.getLoggedInUser();
	}

	/**
	 * 
	 * @return
	 * @throws ApplicationException
	 */
	public String getCacheNameDynamic() throws CustomException {
		UserProfile userProfile = temporaryStorageService.get(UserIdHolder.getAppUserId());
//		UserIdHolder.clearLoggedInUserHolderId();
//		UserProfile userProfile = getLoggedInUser();
		return userProfile.get_id();
	}

	/**
	 * logic to determine cache name dynamically
	 */
	@Override
	public Collection<? extends Cache> resolveCaches(CacheOperationInvocationContext<?> context) {
		String cacheName = null;
		try {
			cacheName = getCacheNameDynamic();
		} catch (CustomException e) {
			e.printStackTrace();
		}
		return Collections.singletonList(cacheManager.getCache(cacheName));
	}

	/**
	 * Map<String, Map<String, List<HashMap<String, Object>>>>
	 */
	private final Map<String, Map<String, List<HashMap<String, Object>>>> optionTypeCache = new ConcurrentHashMap<>();

	/**
	 * Map<String, List<HashMap<String, HashMap<String, Object>>>>
	 */
	private final Map<String, HashMap<String, HashMap<String, Object>>> mdmCache = new ConcurrentHashMap<>();

	/**
	 * @param key
	 * @param value
	 * @param isAdded
	 * @param isRestartCache
	 */
	public void saveToCache(String key, HashMap<String, Object> value, boolean isAdded, boolean isRestartCache) {
		HashMap<String, HashMap<String, Object>> datas = mdmCache.get(key);
		Object id = isAdded ? value.get(Constants._ID) : value.get(Constants.PARENT_OBJECT_ID);
		HashMap<String, HashMap<String, Object>> subObjectData = new HashMap<>();
		if (!ApplicationUtils.isValidateObject(datas)) {
			datas = new HashMap<>();
		}
		subObjectData.put(id.toString(), value);
		datas.putAll(subObjectData);
		mdmCache.put(key, datas);
		if (isRestartCache) {
			updateCacheData();
		}
	}

	/**
	 * @param key
	 * @return
	 */
	// Get data from cache
	public HashMap<String, HashMap<String, Object>> getFromMDMCache(String key) {
		HashMap<String, HashMap<String, Object>> datas = mdmCache.get(key);
		return datas;
	}

	// Clear cache
	/**
	 * 
	 */
	public void clearCache() {
		mdmCache.clear();
	}

	/**
	 * @param key
	 * @param value
	 * @param isAdded
	 */
	// Delete data from cache
	public void deleteFromCache(String key, HashMap<String, Object> value, boolean isAdded) {
		HashMap<String, HashMap<String, Object>> datas = mdmCache.get(key);
		Object id = isAdded ? value.get(Constants._ID) : value.get(Constants.PARENT_OBJECT_ID);
		HashMap<String, HashMap<String, Object>> subObjectData = new HashMap<>();
		if (ApplicationUtils.isValidateObject(datas)) {
			datas.remove(id);
			mdmCache.put(key, datas);
			updateCacheData();
		}
	}

	/**
	 * @param subFileSelectOption
	 * @param subObject
	 */
	public void saveOptionInCache(String subFileSelectOption, Map<String, List<HashMap<String, Object>>> subObject) {
		optionTypeCache.put(subFileSelectOption, subObject);
	}

	/**
	 * @param key
	 * @return
	 */
	// Get Optional List data from cache
	public Map<String, List<HashMap<String, Object>>> getFromOptionCache(String key) {
		return optionTypeCache.get(key);
	}

	public HttpHeaders getPOSTHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		return headers;
	}

	public HttpHeaders getGETHeaders() {
		HttpHeaders headers = new HttpHeaders();
//	        headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		return headers;
	}

	public void updateCacheData() {
		try {
			HttpHeaders headers = getGETHeaders();
			HttpEntity<String> entity = new HttpEntity<>(headers);
			UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(updateCacheData);
			restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);
		} catch (Exception e) {
			e.getMessage();
		}

	}
}
