package com.supercharge.gateway.config;

public class DbContextHolder {
	/**
	 * The Constant contextHolder.
	 */
	private static final ThreadLocal<DbType> contextHolder = new ThreadLocal<>();

	/**
	 * Clear db type.
	 */
	public static void clearDbType() {
		contextHolder.remove();
	}

	/**
	 * Gets the db type.
	 *
	 * @return the db type
	 */
	public static DbType getDbType() {
		return contextHolder.get();
	}

	/**
	 * Sets the db type.
	 *
	 * @param dbType the new db type
	 */
	public static void setDbType(DbType dbType) {
		if (dbType == null) {
			throw new NullPointerException();
		}
		contextHolder.set(dbType);
	}

	// This Utility class should not have public constractor. We added this priavate
	// constractor manulay.
	private DbContextHolder() {

	}
}
