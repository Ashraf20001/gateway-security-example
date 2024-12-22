package com.supercharge.gateway.propertyConfig;

public interface propertyConfigService {

	/**
	 * @param inputString
	 * @return
	 */
	Integer countUppercaseLetters(String inputString);

	/**
	 * @param inputString
	 * @return
	 */
	Integer countLowercaseLetters(String inputString);

	/**
	 * @param inputString
	 * @return
	 */
	Integer countSpecialCharacters(String inputString);

	/**
	 * @param inputString
	 * @return
	 */
	Integer countNumericCharacters(String inputString);

	/**
	 * @param inputString
	 * @return
	 */
	Integer countAlphabeticCharacters(String inputString);

	/**
	 * @param inputString
	 * @return
	 */
	Integer countRepeatedCharacters(String inputString);

	/**
	 * @param inputString
	 * @return
	 */
	Integer getCharacterLength(String inputString);


	/**
	 * @param username
	 * @param password
	 * @return
	 */
	Integer validateEmbeddedUserName(String username, String password);

	/**
	 * @param inputString
	 * @return
	 */
	Integer findCountOfHighestConsecutiveCharacter(String inputString); 
	
}
