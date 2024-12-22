///*
//
// * @author codeboard
// */
//package com.supercharge.gateway.utils.core;
//
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.time.Instant;
//import java.time.ZoneId;
//import java.time.format.DateTimeFormatter;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collection;
//import java.util.Date;
//import java.util.List;
//import java.util.Locale;
//import java.util.Map;
//import java.util.Set;
//import java.util.UUID;
//import java.util.concurrent.ThreadLocalRandom;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//import java.util.stream.Collectors;
//
//import org.bson.Document;
//import org.bson.types.ObjectId;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.cache.Cache;
//import org.springframework.cache.CacheManager;
//import org.springframework.context.ApplicationContext;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.stereotype.Component;
//import org.springframework.web.multipart.MultipartFile;
//
//import com.cbt.supercharge.constants.core.ApplicationConstants;
//import com.cbt.supercharge.exception.core.ApplicationException;
//import com.cbt.supercharge.exception.core.codes.ErrorCodes;
//import com.cbt.supercharge.exception.core.codes.ErrorId;
//import com.cbt.supercharge.utils.core.ApplicationDateUtils;
//import com.mongodb.BasicDBObject;
//import com.mongodb.DBRef;
//import com.mongodb.client.MongoClient;
//import com.mongodb.client.MongoCollection;
//import com.mongodb.client.MongoDatabase;
//import com.mongodb.client.model.FindOneAndUpdateOptions;
//import com.mongodb.client.model.ReturnDocument;
//
///**
// * The Class ApplicationUtils.
// */
//@Component
//public class ApplicationUtils {
//	
//	private static final String RANGE = "0123456789";
//
//	private static final Logger logger = LoggerFactory.getLogger(ApplicationUtils.class);
//
//	private static String mongoServerUrl;
//
//	private static String mongoDatabase;
//
//	private static ApplicationContext context;
//
//	@Autowired
//	private static CacheManager cacheManager;
//	
//
//	public ApplicationUtils(CacheManager cacheManager) {
//		ApplicationUtils.cacheManager = cacheManager;
//	}
//
//	/**
//	 * ALLOWED_EXTENSIONS
//	 */
//	private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("pdf", "jpeg", "jpg", "ppt", "doc", "docx",
//			"xlsx", "csv", "tiff", "xls");
//
//	@Autowired
//	public void setContext(ApplicationContext context) {
//		ApplicationUtils.context = context;
//	}
//
//	public static MongoClient getMongoClient() {
//		return context.getBean(MongoClient.class);
//	}
//
//	@Value("${mongodb.database}")
//	public void setMongoDatabase(String mongoDatabase) {
//		this.mongoDatabase = mongoDatabase;
//	}
//
//	/**
//	 * Gets the unique id.
//	 *
//	 * @return the unique id
//	 */
//	public static String getUniqueId() {
//		return UUID.randomUUID().toString();
//	}
//
//	/**
//	 * Checks if is blank.
//	 *
//	 * @param input the input
//	 * @return true, if is blank
//	 */
//	public static boolean isBlank(String input) {
//		return input == null || input.trim().isEmpty();
//	}
//	
//	
//	/**
//	 * @param str
//	 * @return
//	 */
//	public static boolean isHexadecimal(String str) {
//		return str != null && str.matches("^[0-9a-fA-F]+$");
//	}
//
//	/**
//	 * @param email
//	 * @return
//	 */
//	public static boolean isValidEmailId(String email) {
//		// Regular expression to validate email format
//		String emailRegex = "[A-Za-z0-9\\._%+\\-]+@[A-Za-z0-9\\.\\-]+\\.[A-Za-z]{2,}";
//		Pattern pattern = Pattern.compile(emailRegex);
//		Matcher matcher = pattern.matcher(email);
//		return matcher.matches();
//	}
//	
//	/**
//	 * @param value
//	 * @param regex
//	 * @return
//	 */
//	public static boolean isValidCharacter(String value, String regex) {
//		Pattern pattern = Pattern.compile(regex);
//		Matcher matcher = pattern.matcher(value);
//		return matcher.matches();
//	}
//	
//
//	/**
//	 * @param value
//	 * @param minLength
//	 * @param maxLength
//	 * @return
//	 */
//	public static boolean validateLength(String value, Integer minLength, Integer maxLength) {
//		if (minLength == null) {
//			return false;
//		}
//		if (value.length() < minLength || value.length() > maxLength) {
//			return true;
//		}
//		return false;
//	}
//
//
//	/**
//	 * Checks if is not blank.
//	 *
//	 * @param input the input
//	 * @return true, if is not blank
//	 */
//	public static boolean isNotBlank(String input) {
//		return !isBlank(input);
//	}
//
//	/**
//	 * Checks if is not valid id.
//	 *
//	 * @param id the id
//	 * @return true, if is not valid id
//	 */
//	public static boolean isNotValidId(Integer id) {
//		return !isValidId(id);
//	}
//
//	/**
//	 * Checks if is validate object.
//	 *
//	 * @param obj the obj
//	 * @return true, if is validate object
//	 */
//	@SuppressWarnings("rawtypes")
//	public static boolean isValidateObject(Object obj) {
//		if (obj == null) {
//			return false;
//		}
//
//		if (obj instanceof List<?> && ((Collection<?>) obj).isEmpty()) {
//			return false;
//		}
//
//		if (obj instanceof Map<?, ?> && ((Map) obj).isEmpty()) {
//			return false;
//		}
//		
//		if (obj instanceof String && ((String) obj).isEmpty()) {
//			return false;
//		}
//
//		if (obj instanceof Set<?>) {
//			return !((Set) obj).isEmpty();
//		}
//
//		return true;
//	}
//
//	public static boolean isValidString(String value) {
//		return value != null && !value.isBlank();
//	}
//
//	public static void clearAllCaches() {
//		cacheManager.getCacheNames().forEach(cacheName -> cacheManager.getCache(cacheName).clear());
//	}
//	
//	/**
//	 * @param id
//	 */
//	public static void clearUserCache(String id) {
//		if (isValidateObject(id))
//		{
//			Cache cache = cacheManager.getCache(id);
//			cache.clear();
//			logger.info("Cache Cleared..........................");
//		}
//	}
//
//	/**
//	 * @param list
//	 * @return
//	 */
//	public static boolean isValidMap(Object list) {
//		if (list instanceof Map<?, ?>) {
//			return ((Map) list) != null && !((Map) list).isEmpty();
//		}
//		return false;
//	}
//
//	/**
//	 * Checks if is valid id.
//	 *
//	 * @param id the id
//	 * @return true, if is valid id
//	 */
//	public static boolean isValidId(Integer id) {
//		return id != null && id >= 1;
//	}
//
//	/**
//	 * Checks if is valid identity.
//	 *
//	 * @param identity the identity
//	 * @return true, if is valid identity
//	 */
//	public static boolean isValidIdentity(String identity) {
//		return !isBlank(identity) && !identity.trim().equals("-1") && !identity.equalsIgnoreCase("undefined")
//				&& !identity.equalsIgnoreCase("null");
//	}
//
//	/**
//	 * Checks if is valid list.
//	 *
//	 * @param list the list
//	 * @return true, if is valid list
//	 */
//	public static boolean isValidList(List<?> list) {
//		return list != null && !list.isEmpty();
//
//	}
//
//	/**
//	 * Checks if is valid long.
//	 *
//	 * @param value the value
//	 * @return true, if is valid long
//	 */
//	public static boolean isValidLong(Long value) {
//		return value != null && value >= 0;
//	}
//	
//	public static boolean isBcryptPasswordMatched(String password, String encryptedPasssword) {
//		return new BCryptPasswordEncoder().matches(password, encryptedPasssword);
//	}
//
//	public static boolean isValidObject(Object input) {
//		return (input != null);
//	}
//
//	/**
//	 * To Generate otp
//	 *
//	 * @throws
//	 */
//	public String generateOtp(Integer otpLength) {
//		String numbers = RANGE;
//		char[] otp = new char[otpLength];
//		for (int i = 0; i < otpLength; i++) {
//			otp[i] = numbers.charAt(ThreadLocalRandom.current().nextInt(numbers.length()));
//		}
//		return new String(otp);
//	}
//	
//	/**
//	 * 
//	 * @param objId
//	 * @return
//	 * @throws ApplicationException
//	 */
//	public static ObjectId validateObjectIAndReturn(String objId) throws ApplicationException {
//		ObjectId objectId = null;
//		try {
//			objectId = new ObjectId(objId);
//		} catch (Exception e) {
//			throw new ApplicationException(ErrorCodes.INVALID_OBJECT_ID);
//		}
//		return objectId;
//	}
//
////	public static MongoDatabase getMongoDatabase() {
////		logger.info("mongoServerURL..........!!!!!!!!!!!!!!!!!" + mongoServerUrl);
////		MongoClient mongoClient = MongoClients.create(mongoServerUrl);
////		return mongoClient.getDatabase("mdm_db");
////	}
//
////	public static int getNextSequence(String sequenceName, MongoDatabase database, String databaseName) {
////        MongoCollection<Document> collection = database.getCollection(databaseName);
////
////        // create seprate doc in collection as _id:sequence
////        // created id will as used as primary key id
////        Document query = new Document("_id", sequenceName);
////        Document update = new Document("$inc", new Document("seq", 1));
//////        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER).bypassDocumentValidation(true);
////
////        Document result = collection.findOneAndUpdate(query, update	);
////
////        if (result == null) {
////            collection.insertOne(new Document("_id", sequenceName).append("seq", 1));
////            return 1;
////        }
////
////        return result.getInteger("seq");
////    }
//
//	public static int getNextSequenceForId(String sequenceName, MongoClient mongoClient, String databaseName) {
//		MongoDatabase database = mongoClient.getDatabase(mongoDatabase);
//		MongoCollection<Document> collection = database.getCollection(databaseName);
//
//		// create seprate doc in collection as _id:sequence
//		// created id will as used as primary key id
//		Document query = new Document("_id", sequenceName);
//		Document update = new Document("$inc", new Document("seq", 1));
//
//		FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().upsert(true)
//				.returnDocument(ReturnDocument.AFTER).bypassDocumentValidation(true);
//		Document result = collection.findOneAndUpdate(query, update, options);
////        if (result == null) {
////            collection.insertOne(new Document("_id", sequenceName).append("seq", 1).append("options", new Document("bypassDocumentValidation", true)));
////            return 1;
////        }
//		return result.getInteger("seq");
//	}
//
//	/**
//	 * @param multipartFile
//	 * @return
//	 */
//	public static String extractSingleExtension(MultipartFile multipartFile) {
//		String fileExtension = null;
//		if (ApplicationUtils.isValidateObject(multipartFile)) {
//			if (multipartFile.getOriginalFilename() != null) {
//				String[] originalFileName = multipartFile.getOriginalFilename().split(ApplicationConstants.DOT_REGEX);
//				fileExtension = originalFileName[originalFileName.length - ApplicationConstants.ONE];
//			}
//		}
//		return fileExtension;
//	}
//
//	/**
//	 * @param file
//	 * @param fileSize
//	 * @param errorId
//	 * @return
//	 * @throws ApplicationException
//	 */
//	public static float validateFileSize(MultipartFile file, Long fileSize, ErrorId errorId)
//			throws ApplicationException {
//		float fileSizevalue = file.getSize();
//		float byteValue = ApplicationConstants.BYTES_INT * ApplicationConstants.BYTES_INT;
//		float fileValue = fileSizevalue / byteValue;
//		if (Boolean.TRUE.equals(fileValue > fileSize)) {
//			throw new ApplicationException(errorId);
//		}
//		return fileValue;
//	}
//
//	/**
//	 * @param multipartFile
//	 * @throws ApplicationException
//	 */
//	public static float validateFileSize(MultipartFile multipartFile, Integer maxFileSizeLimit)
//			throws ApplicationException {
//		Long maxFileSize = maxFileSizeLimit.longValue();
//		ErrorId maxSizeErrorId = new ErrorId(ErrorCodes.FILE_SIZE_EXCEEDED.getErrorCode(),
//				ErrorCodes.FILE_SIZE_EXCEEDED.getErrorMessage() + maxFileSizeLimit + ApplicationConstants.MB
//						+ ApplicationConstants.FOR + multipartFile.getOriginalFilename() + ApplicationConstants.FILE);
//		return ApplicationUtils.validateFileSize(multipartFile, maxFileSize, maxSizeErrorId);
//	}
//
//	/**
//	 * @param fileFormat
//	 * @return
//	 */
//	public static String getValidFileFormat(MultipartFile multipartFile) {
//		String fileExtension = extractSingleExtension(multipartFile);
//		return (ApplicationUtils.isValidString(fileExtension) && ALLOWED_EXTENSIONS.contains(fileExtension))
//				? fileExtension
//				: null;
//	}
//	
//	/**
//	 * @param value
//	 * @param splitValue
//	 * @return
//	 */
//	public static String[] splitValue(String value,String splitValue) {
//		return value.split(splitValue);
//	}
//	
//	/**
//	 * @param value
//	 * @param splitValue
//	 * @return
//	 */
//	public static String splitValueWithDot(String value, String splitValue) {
//	    String[] parts = value.split(splitValue);
//	    return parts.length > 0 ? parts[0] : value; // Return the first part or an empty string if no parts found
//	}
//
//	/**
//	 * @param collectionName
//	 * @param dbRef 
//	 * @return
//	 */
//	public static DBRef generateDBRef(String collectionName,String dbRef) {
//		return new DBRef(collectionName, dbRef);
//	}
//	
//	/**
//	 * @param value
//	 * @param toMaskLength
//	 * @return
//	 */
//	public static String maskCharacters(String value, Integer toMaskLength) {
//		StringBuilder maskedValue = new StringBuilder();
//		if (Boolean.FALSE.equals(ApplicationUtils.isValidString(value))) {
//			return value;
//		}
//		if (value.startsWith(ApplicationConstants.OPEN_BRACKET) && value.endsWith(ApplicationConstants.CLOSE_BRACKET)) {
//			String content = value.substring(1, value.length() - 1);
//			return Arrays.stream(content.split(ApplicationConstants.COMMA)).map(String::trim)
//					.map(val -> maskStringValue(val, toMaskLength))
//					.collect(Collectors.joining(ApplicationConstants.COMMA + ApplicationConstants.SPACE));
//		} else {
//			String[] parts = value.split(ApplicationConstants.COMMA);
//			for (int i = 0; i < parts.length; i++) {
//				String part = parts[i];
//				if (part.length() > toMaskLength)
//					maskedValue.append(maskStringValue(part, toMaskLength));
//				else
//					maskedValue.append(maskStringValue(part, part.length()));
//				if (i < parts.length - 1)
//					maskedValue.append(ApplicationConstants.COMMA);
//			}
//		}
//		return maskedValue.toString();
//	}
//
//	/**
//	 * @param value
//	 * @param toMaskLength
//	 * @return
//	 */
//	private static StringBuilder maskStringValue(String value, Integer toMaskLength) {
//		int strLength = value.length();
//		StringBuilder maskedValue = new StringBuilder();
//		if (toMaskLength >= strLength) {
//			mask(strLength, maskedValue);
//		} else {
//			mask(toMaskLength, maskedValue);
//			maskedValue.append(value.substring(toMaskLength));
//		}
//		return maskedValue;
//	}
//
//	/**
//	 * @param strLength
//	 * @param maskedValue
//	 */
//	private static void mask(int strLength, StringBuilder maskedValue) {
//		maskedValue.append(ApplicationConstants.ASTERISK.repeat(strLength));
//	}
//	
//	/**
//	 * 
//	 * @param columnType
//	 * @param columnValue
//	 * @param date 
//	 * @return
//	 */
//	public static Object dataTypeConverter(String columnType, Object columnValue, Date date) {
//		if (columnType.equals(ApplicationConstants.FILTER_TYPE_DATE)) {
//			columnValue = ApplicationDateUtils.convertObjectToDate(columnValue, columnType, date);
//			columnValue = parseDate(columnValue.toString());
//		} else if (columnType.equals(ApplicationConstants.NUMBER_)) {
//			columnValue = Integer.parseInt(columnValue.toString());
//		} else if (columnType.equals(ApplicationConstants.TEXT)) {
//			columnValue = String.valueOf(columnValue);
//		} else if (columnType.equals(ApplicationConstants.REFERENCE)) {
//			if (columnValue instanceof List<?>) {
//				List<ObjectId> objectIds = new ArrayList<>();
//				List<String> columnValueIds = (List<String>) columnValue;
//				for (String objectId : columnValueIds) {
//					objectIds.add(validateObjectId(String.valueOf(objectId)));
//					columnValue = objectIds;
//				}
//
//			} else {
//				columnValue = validateObjectId(String.valueOf(columnValue));
//			}
//		}
//		return columnValue;
//	}
//	
//	/**
//	 * @param dateString
//	 * @return
//	 */
//	public static Date parseDate(String dateString) {
//		try {
//			SimpleDateFormat dateFormat = new SimpleDateFormat(ApplicationConstants.DATE_FORMAT_ENGINE, Locale.ENGLISH);
//			Date dates = dateFormat.parse(dateString);
//			Instant instant = dates.toInstant();
//			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
//					.withZone(ZoneId.of("UTC"));
// 
//			return Date.from(Instant.parse(formatter.format(instant)));
//		} catch (ParseException e) {
//			logger.info("Error parsing date: " + dateString, e);
//		}
//		return null;
//	}
//	
//	/**
//	 * 
//	 * @param objId
//	 * @return
//	 * @throws ApplicationException
//	 */
//	public static ObjectId validateObjectId(String objId){
//		ObjectId objectId = null;
//		try {
//			objectId = new ObjectId(objId);
//		} catch (Exception e) {
//			logger.info("Invalid object Id...............");
//		}
//		return objectId;
//	}
//	
//	/**
//	 * @param obj
//	 * @return
//	 */
//	public static int convertToInt(Object obj) {
//		if (obj instanceof Integer) {
//			return (Integer) obj;
//		} else if (obj instanceof Number) {
//			return ((Number) obj).intValue();
//		} else if (obj instanceof String) {
//			try {
//				return Integer.parseInt((String) obj);
//			} catch (NumberFormatException e) {
//				System.err.println("Invalid number format: " + obj);
//				return 0;
//			}
//		} else {
//			System.err.println("Unexpected type: " + obj.getClass().getName());
//			return 0;
//		}
//	}
//	
//	/**
//	 * 
//	 * @param replaceValue
//	 * @param queryTemplate
//	 * @param replaceStringValue 
//	 * @return
//	 */
//	public static BasicDBObject replaceBasicDbQueryValues(Object replaceValue, String queryTemplate,
//			String replaceStringValue) {
//		String reduceBasicStringQuery = null;
//		if (ApplicationUtils.isValidateObject(replaceStringValue)) {
//			reduceBasicStringQuery = String.format(queryTemplate, replaceStringValue ,replaceValue);
//		} else {
//			reduceBasicStringQuery = String.format(queryTemplate, replaceValue, replaceValue);
//		}
//		return BasicDBObject.parse(reduceBasicStringQuery);
//	}
//
//}
//
