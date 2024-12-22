package com.supercharge.gateway.role.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;

import com.cbt.supercharge.constants.core.ApplicationConstants;
import com.cbt.supercharge.constants.core.SecurityConstants;
import com.cbt.supercharge.constants.core.TableConstants;
import com.cbt.supercharge.exception.core.ApplicationException;
import com.cbt.supercharge.exception.core.codes.ErrorCodes;
import com.cbt.supercharge.transfer.objects.entity.Role;
import com.cbt.supercharge.transfer.objects.entity.UserAndInstitutionLinking;
import com.cbt.supercharge.transfer.objects.entity.UserProfile;
import com.cbt.supercharge.transfter.objects.core.dto.RoleApis;
import com.cbt.supercharge.utils.core.ApplicationUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.supercharge.gateway.common.base.dao.CommonUserDaoImpl;
import com.supercharge.gateway.common.base.service.GatewayBaseService;
import com.supercharge.gateway.common.handlers.CustomException;
import com.supercharge.gateway.common.handlers.UserIdHolder;
import com.supercharge.gateway.filters.TemporaryStorageService;

import reactor.core.publisher.Mono;

@Service
public class RoleServiceImpl {

	@Autowired
	private CommonUserDaoImpl commonUserDaoImpl;

	Logger logger = LoggerFactory.getLogger(RoleServiceImpl.class);

	@Autowired
	private GatewayBaseService baseService;

	@Autowired
	private CacheManager cacheManager;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private TemporaryStorageService temporaryStorageService;

	public boolean checkRolesForApi(List<String> roles, String api, ServerWebExchange exchange, UserProfile loggedUser)
			throws CustomException {
		final String[] defaultAntMatchers = ArrayUtils.addAll(SecurityConstants.getOpenurlsget());
		String[] occurrences = Arrays.stream(defaultAntMatchers)
				.filter(value -> value.equals(api) || value.equals(ApplicationConstants.OPEN_URL))
				.toArray(String[]::new);
		clearAuthApiCache(api, loggedUser);
		if (occurrences.length <= 0) {
			HashMap<String, Map<String, String>> roleApis = new HashMap<>(getRoleApiMaps(loggedUser));
			roles = roleApis.entrySet().stream().map(entry -> entry.getKey()).collect(Collectors.toList());
			roleApis.keySet().retainAll(roles);
			getToFactorApis(roleApis);

			for (String role : roles) {
				Map<String, String> roleApiMap = roleApis.get(role);
				List<String> urlList = new ArrayList<>(roleApiMap.keySet());
				AntPathMatcher match = new AntPathMatcher();

				if (ApplicationUtils.isValidList(urlList)) {
					for (String urls : urlList) {
						if (match.match(urls, api)) {
							String pageActivity = roleApiMap.get(urls);
							logger.info("IN ROLE MAPPED API" + api);
							if (ApplicationUtils.isValidateObject(pageActivity)) {
								String[] splitedString = pageActivity.split(ApplicationConstants.ESCAPED_ASTERISK);
								int arrayLength = splitedString.length;

								// Setting attributes on ServerWebExchange
								exchange.getAttributes().put(ApplicationConstants.ACTIVITY,
										(arrayLength > ApplicationConstants.ZERO) ? splitedString[0]
												: ApplicationConstants.ACTIVITY_NOT_FOUND);
								exchange.getAttributes().put(ApplicationConstants.PAGE,
										(arrayLength > ApplicationConstants.ONE) ? splitedString[1]
												: ApplicationConstants.PAGENAME_NOT_FOUND);
							}
							return true;
						}
					}
				}
			}
		} else {
			AntPathMatcher match = new AntPathMatcher();
			for (String urls : Arrays.asList(defaultAntMatchers)) {
				if (match.match(urls, api)) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * @param api
	 * @param loggedUser
	 */
	private void clearAuthApiCache(String api, UserProfile loggedUser) {
		if(ApplicationConstants.AUTH_API.equals(api)) {
			ConcurrentHashMap<Object, Object> cacheMap = getCacheMap(loggedUser.get_id());
			cacheMap.clear();
		}
	}

	public Map<String, Map<String, String>> getRoleApiMaps(UserProfile loggedUser) throws CustomException {
		Map<String, Map<String, String>> roleMaps = new HashMap<>();
		temporaryStorageService.put(loggedUser.get_id(), loggedUser);
		UserIdHolder.setLoggedInUserId(loggedUser.get_id());
		List<RoleApis> apiWithRole = new ArrayList<>();
		apiWithRole = getCacheDataForRoleApiMap(apiWithRole, loggedUser.get_id());
		if (Boolean.FALSE.equals(ApplicationUtils.isValidList(apiWithRole))) {
			List<UserAndInstitutionLinking> mappedRole = commonUserDaoImpl.getMappedUserRoleData(loggedUser);
			List<Role> roleList = mappedRole.stream().map(x -> x.getRole()).collect(Collectors.toList());
			apiWithRole = commonUserDaoImpl.getApiRoleMaps(roleList);
		}

		if (ApplicationUtils.isValidList(apiWithRole)) {
			buildRolePageNameMap(apiWithRole, roleMaps);
		}

		return roleMaps;
	}

	private Map<String, Map<String, String>> buildRolePageNameMap(List<RoleApis> apiRoleMapList,
			Map<String, Map<String, String>> rolePageNameMap) {
		Map<String, String> apiList = new HashMap<>();
		for (RoleApis apiRole : apiRoleMapList) {
			if (rolePageNameMap.get(apiRole.getRole()) == null) {
				apiList.put(apiRole.getApi(),
						String.valueOf(apiRole.getActivity()) + ApplicationConstants.ASTERISK + apiRole.getPage());
				rolePageNameMap.put(apiRole.getRole(), apiList);
			} else {
				List<String> roleList = rolePageNameMap.entrySet().stream().map(entry -> apiRole.getRole())
						.collect(Collectors.toList());
				if ((roleList.indexOf(apiRole.getApi()) <= ApplicationConstants.MINUSONE)) {
					apiList.put(apiRole.getApi(),
							String.valueOf(apiRole.getActivity()) + ApplicationConstants.ASTERISK + apiRole.getPage());
					rolePageNameMap.put(apiRole.getRole(), apiList);
				}
			}
		}
		// logger.info("role page name map {}", rolePageNameMap);
		return rolePageNameMap;
	}

	private void getToFactorApis(HashMap<String, Map<String, String>> roleApis) {
		Map<String, String> shortLivingRoleApis = new HashMap<>();
		shortLivingRoleApis.put(ApplicationConstants.ROLE_PWD_EXPIRED_URL, "0");
		shortLivingRoleApis.put(ApplicationConstants.ROLE_2FA_GET_URL, "0");
		shortLivingRoleApis.put(ApplicationConstants.ROLE_2FA_VERIFY_URL, "0");

		roleApis.put(ApplicationConstants.ROLE_SHORT_LIVING, shortLivingRoleApis);
	}

	public Mono<UserProfile> getLoggedInUser1(ServerWebExchange exchange) {
		return Mono.deferContextual(ctx -> {
			UserProfile userProfile = (UserProfile) ctx.get(TableConstants.USER_PROFILE);
			if (!ApplicationUtils.isValidateObject(userProfile.getUserIdentificationNumber())) {
				return Mono.error(new IllegalStateException(ErrorCodes.INVALID_USER.getErrorMessage()));
			}
			return Mono.just(userProfile);
		});
	}

	public Mono<ResponseEntity<UserProfile>> getUserProfile(ServerWebExchange exchange) {
		return getLoggedInUser1(exchange).map(userProfile -> ResponseEntity.ok(userProfile))
				.onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build()));
	}

	public Mono<UserProfile> getUserProfileFromExchange(ServerWebExchange exchange) {
		return Mono.defer(() -> {
			// Check and retrieve the UserProfile
			if (exchange.getAttributes().containsKey(TableConstants.USER_PROFILE)) {
				UserProfile userProfile = (UserProfile) exchange.getAttributes().get(TableConstants.USER_PROFILE);
				if (ApplicationUtils.isValidateObject(userProfile)) {
					return Mono.just(userProfile);
				} else {
					return Mono.error(new IllegalStateException("Invalid UserProfile found in exchange attributes"));
				}
			} else {
				return Mono.error(new IllegalStateException("UserProfile is not present in exchange attributes"));
			}
		});
	}

	public UserProfile getLoggedInUser() throws ApplicationException {
		return baseService.getLoggedInUser();
	}

	/**
	 * 
	 * @param apiWithRole
	 * @return
	 */
	private List<RoleApis> getCacheDataForRoleApiMap(List<RoleApis> apiWithRole, String userName) {
		ConcurrentHashMap<Object, Object> concurrentMap = getCacheMap(userName);
		for (Map.Entry<Object, Object> concurrent : concurrentMap.entrySet()) {
			Object concurrentObject = concurrent.getValue();
			String json;
			try {
				json = objectMapper.writeValueAsString(concurrentObject);
				apiWithRole = objectMapper.readValue(json, new TypeReference<List<RoleApis>>() {
				});
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
			break;
		}
		return apiWithRole;
	}

	@SuppressWarnings("unchecked")
	public ConcurrentHashMap<Object, Object> getCacheMap(String cacheName) {
		Cache cache = cacheManager.getCache(cacheName);
		if (Boolean.FALSE.equals(ApplicationUtils.isValidateObject(cache))) {
			throw new IllegalArgumentException("Cache not found: " + cacheName);
		}
		Object nativeCache = cache.getNativeCache();
		if (nativeCache instanceof ConcurrentMap) {
			return (ConcurrentHashMap<Object, Object>) nativeCache;
		} else {
			throw new IllegalStateException("Cache is not a ConcurrentMap: " + cacheName);
		}
	}

}
