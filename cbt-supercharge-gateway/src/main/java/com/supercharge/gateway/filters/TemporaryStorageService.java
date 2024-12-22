package com.supercharge.gateway.filters;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.cbt.supercharge.transfer.objects.entity.UserProfile;

@Service
public class TemporaryStorageService {
	private final Map<String, UserProfile> tempStorage = new ConcurrentHashMap<>();

	public void put(String key, UserProfile value) {
		tempStorage.put(key, value);
	}

	public UserProfile get(String key) {
		return tempStorage.get(key);
	}

	public void remove(String key) {
		tempStorage.remove(key);
	}

}
