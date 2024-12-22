package com.supercharge.gateway.propertyConfig;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service

public class PropertyConfigServiceImpl implements propertyConfigService {

	
	/**
	 * @param inputString
	 * @return
	 */
	@Override
	public Integer countUppercaseLetters(String inputString) {
		int count = 0;
		for (char c : inputString.toCharArray()) {
			if (Character.isUpperCase(c)) {
				count++;
			}
		}
		return count;
	}

	/**
	 * @param inputString
	 * @return
	 */
	@Override
	public Integer countLowercaseLetters(String inputString) {
		int count = 0;
		for (char c : inputString.toCharArray()) {
			if (Character.isLowerCase(c)) {
				count++;
			}
		}
		return count;
	}
	
	
	/**
	 * @param inputString
	 * @return
	 */
	@Override
	public Integer countSpecialCharacters(String inputString) {
		int count = 0;
		for (char c : inputString.toCharArray()) {
			if (!Character.isLetter(c) && !Character.isDigit(c)) {
				count++;
			}
		}
		return count;
	}

	
	/**
	 * @param inputString
	 * @return
	 */
	@Override
	public Integer countNumericCharacters(String inputString) {
		int count = 0;
		for (char c : inputString.toCharArray()) {
			if (Character.isDigit(c)) {
				count++;
			}
		}
		return count;
	}

	
	/**
	 * @param inputString
	 * @return
	 */
	@Override
	public Integer countAlphabeticCharacters(String inputString) {
		int alphabeticCount = 0;

		for (char currentChar : inputString.toCharArray()) {
			if (Character.isLetter(currentChar)) {
				alphabeticCount++;
			}
		}

		return alphabeticCount;
	}

	
	/**
	 * @param inputString
	 * @return
	 */
	@Override
	public Integer countRepeatedCharacters(String inputString) {
		Map<Character, Integer> charCountMap = new HashMap<>();
		int repeatedCount = 0;

		for (char currentChar : inputString.toCharArray()) {
			if (Character.isLetterOrDigit(currentChar)) {
				int count = charCountMap.getOrDefault(currentChar, 0);
				if (count > 0) {
					repeatedCount++;
				}
				charCountMap.put(currentChar, count + 1);
			}
		}

		return repeatedCount;
	}
	@Override
	 public  Integer findCountOfHighestConsecutiveCharacter(String inputString) {
	        char characterWithHighestCount = '\0';  // Initialize to null character
	        int highestConsecutiveCount = 0;
	        int currentConsecutiveCount = 1;

	        for (int i = 1; i < inputString.length(); i++) {
	            char currentChar = inputString.charAt(i);
	            char previousChar = inputString.charAt(i - 1);

	            if (currentChar == previousChar) {
	                currentConsecutiveCount++;
	            } else {
	                if (currentConsecutiveCount > highestConsecutiveCount) {
	                    highestConsecutiveCount = currentConsecutiveCount;
	                    characterWithHighestCount = previousChar;
	                }
	                currentConsecutiveCount = 1;
	            }
	        }

	        // Check the last consecutive count
	        if (currentConsecutiveCount > highestConsecutiveCount) {
	            highestConsecutiveCount = currentConsecutiveCount;
	            characterWithHighestCount = inputString.charAt(inputString.length() - 1);
	        }

	        return highestConsecutiveCount;
	    }

	
	/**
	 * @param inputString
	 * @return
	 */
	@Override
	public Integer getCharacterLength(String inputString) {
		return inputString.length();
	}

	
	/**
	 * @param username
	 * @param password
	 * @return
	 */
	@Override
	public Integer validateEmbeddedUserName(String username, String password) {
		int longestConsecutive = 0;

		for (int consecutiveValue = 1; consecutiveValue <= username.length(); consecutiveValue++) {
			for (int i = 0; i <= username.length() - consecutiveValue; i++) {
				String consecutiveSubstring = username.substring(i, i + consecutiveValue);
				if (password.contains(consecutiveSubstring) && consecutiveSubstring.length() == consecutiveValue) {
					longestConsecutive = consecutiveSubstring.length();
				}
			}
		}

		return longestConsecutive;
	}


}
