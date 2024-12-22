package com.supercharge.gateway.common.config;

import org.apache.commons.codec.language.DoubleMetaphone;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Java config to use Spring Data JPA alongside the Spring caching support.
 *
 * @author cbt
 */
@EnableCaching
@Configuration
class CachingConfiguration {

	@Bean
	public DynamicCacheResolver dynamicCacheResolver(CacheManager cacheManager) {
		return new DynamicCacheResolver(cacheManager);
	}

	@Bean
	public DoubleMetaphone doubleMetaphone() {
		return new DoubleMetaphone();
	}
	
	@Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
