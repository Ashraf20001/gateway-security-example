package com.supercharge.gateway.common.utils;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class GatewayDomainUtils {

	public Boolean isDomainAllowed(String inputDomain, List<String> domainList) {

		boolean domainAllowed = false;

		for (String domain : domainList) {
			if (domain.equals(inputDomain)) {
				domainAllowed = true;
				break;
			}

		}

		return domainAllowed;

	}

}
