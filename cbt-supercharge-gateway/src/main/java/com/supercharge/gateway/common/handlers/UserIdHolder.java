package com.supercharge.gateway.common.handlers;

public class UserIdHolder {

	private UserIdHolder() {
		/**/}

	/**
	 * The Constant appUserHolder.
	 */
	private static final ThreadLocal<String> userIdHolder = new ThreadLocal<>();

	/**
	 * Clear logged in user holder.
	 */
	public static void clearLoggedInUserHolderId() {
		userIdHolder.remove();
	}

	/**
	 * Gets the app user.
	 *
	 * @return the app user
	 */
	public static String getAppUserId() {
		return userIdHolder.get();
	}

	/**
	 * Sets the logged in user.
	 *
	 * @param loggedInUser the new logged in user
	 */
	public static void setLoggedInUserId(String userId) {
		if (userId == null) {
			throw new NullPointerException();
		}
		userIdHolder.set(userId);
	}

//	public  void setclearLoggedInUserHolder(LoggedInUserHolder loggedInUserHolder) {
//		// TODO Auto-generated method stub
//		this.loggedInUserHolder = loggedInUserHolder;
//	}
}
