//package com.supercharge.gateway.utils.core;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import org.springframework.stereotype.Component;
//
//@Component
//public class TimeZoneProvider {
//	private TimeZoneProvider() {
//		/**/}
//
//	private static Map<String, String> timeZone = new HashMap<>();
//
//	static {
//		timeZone.put("IST", "Asia/Calcutta");
//		timeZone.put("UTC", "UTC");
//		timeZone.put("EST", "America/New_York");
//		timeZone.put("CST", "America/Havana");
//	}
//
//	public static String getConventionalTimeZone(String timeZoneStr) {
//		return timeZone.get(timeZoneStr) == null ? timeZoneStr : timeZone.get(timeZoneStr);
//	}
//
//}
