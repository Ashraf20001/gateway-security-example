//package com.supercharge.gateway.user.resetkey.dao;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Date;
//import java.util.List;
//import java.util.stream.Collectors;
//
//import org.bson.Document;
//import org.bson.types.ObjectId;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.data.mongodb.core.query.Criteria;
//import org.springframework.data.mongodb.core.query.Query;
//import org.springframework.data.mongodb.core.query.Update;
//import org.springframework.stereotype.Repository;
//
//import com.cbt.supercharge.common.resetkey.dao.IResetKeyDao;
//import com.cbt.supercharge.constants.core.ApplicationConstants;
//import com.cbt.supercharge.constants.core.TableConstants;
//import com.cbt.supercharge.exception.core.ApplicationException;
//import com.cbt.supercharge.transfer.objects.entity.ResetKey;
//import com.cbt.supercharge.transfer.objects.entity.UserPasswordHistory;
//import com.cbt.supercharge.transfer.objects.entity.propertyConfigValueEntity;
//import com.supercharge.gateway.utils.core.ApplicationUtils;
//
//@Repository
//@Qualifier("mongo")
//public class ResetKeyDaoImplMongo implements IResetKeyDao{
//
//	@Autowired
//	MongoTemplate mT;
//	
//	@Override
//	public Integer createKey(ResetKey resetKey) throws ApplicationException {
//		resetKey.setResetKeyId(ApplicationUtils.getNextSequenceForId(TableConstants._ID, ApplicationUtils.getMongoClient(),TableConstants.USER_MANAGEMENT_RESET_KEY));
//		mT.save(resetKey);
//		return resetKey.getResetKeyId();
//	}
//
//
//	/**
//	 *@param resetKey
//	 */
//	@Override
//	public void updateKey(ResetKey resetKey) throws ApplicationException {
//		Query query = new Query(Criteria.where(TableConstants._ID).is(new ObjectId(resetKey.get_id())));
//		Update update = new Update().set(TableConstants.MODIFIED_DATE, resetKey.getModifiedDate())
//				.set(TableConstants.RESET_EXPIRED_DATE, resetKey.getExpiryDate());
//		mT.updateFirst(query, update, ResetKey.class);
//	}
//
//	@Override
//	public Integer getPasswordChangeHourByinstitutionId(Integer institutionId) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public ResetKey getResetKeyByKey(String resetKey) throws ApplicationException {
//		Criteria criteria = Criteria.where(TableConstants.RESET_TOKEN).is(resetKey).and(TableConstants.IS_DELETED).is(false);
//        Query query = new Query(criteria);
//        return mT.findOne(query, ResetKey.class);
//	}
//
//
//	@Override
//	public List<UserPasswordHistory> getlastFivePassword(Integer userId, int validateExistingHistory) {
//		 List<Document> pipeline = Arrays.asList(
//		            new Document(ApplicationConstants.MATCH, new Document(TableConstants.USER_JOIN_COLLECTION_ID, new Document("$exists", true).append("$ne", null))),
//		            new Document(ApplicationConstants.LOOKUP, new Document(ApplicationConstants.MONGO_FROM, TableConstants.USER_MANAGMENT_USER_INFO)
//		                    .append(ApplicationConstants.MONGO_LOCALFIELD, TableConstants.USER_JOIN_COLLECTION_ID)
//		                    .append(ApplicationConstants.MONGO_FORIEGNFIELD, TableConstants._ID)
//		                    .append(ApplicationConstants.MONGO_ALAIS, TableConstants.USER_PROFILE)),
//		            new Document(ApplicationConstants.MATCH, new Document(TableConstants.USER_PROFILE_USER_ID , userId)),
//		            new Document(ApplicationConstants.SORT, new Document(TableConstants.UM_PWD_HT_ID, -1)),
//		            new Document(ApplicationConstants.$_LIMIT, validateExistingHistory),
//		            new Document(ApplicationConstants.ADD_FIELDS, new Document(TableConstants.USER_PROFILE, new Document("$arrayElemAt", Arrays.asList(TableConstants.USERPROFILE_$, 0))))
//		        );
//
//		        List<Document> results = mT.getCollection(TableConstants.USER_MANAGMENT_USER_PASSWORD_HISTORY).aggregate(pipeline).into(new ArrayList<>());
//		        List<UserPasswordHistory> entities = results.stream()
//		                .map(this::convertlastFivepwdDocumentToEntity)
//		                .collect(Collectors.toList());
//
//		        return entities;
//	}
//	 public UserPasswordHistory convertlastFivepwdDocumentToEntity(Document document) {
//	        return mT.getConverter().read(UserPasswordHistory.class, document);
//	    }
//	 
//	@Override
//	public List<UserPasswordHistory> getPasswordWithinDays(Integer userId, Date currentDate, Date tenDaysAgo) {
//		 List<Document> pipeline = Arrays.asList(
//		            new Document(ApplicationConstants.LOOKUP, new Document(ApplicationConstants.MONGO_FROM, TableConstants.USER_MANAGMENT_USER_INFO)
//		                    .append(ApplicationConstants.MONGO_LOCALFIELD, TableConstants.USER_JOIN_COLLECTION_ID)
//		                    .append(ApplicationConstants.MONGO_FORIEGNFIELD, TableConstants._ID)
//		                    .append(ApplicationConstants.MONGO_ALAIS, TableConstants.USER_PROFILE)),
//		            new Document(ApplicationConstants.MATCH, new Document(TableConstants.USER_PROFILE_USER_ID , userId)),		            
//		            new Document(ApplicationConstants.MATCH, new Document(TableConstants.UM_MDY_DTE,
//		            new Document("$gte", tenDaysAgo).append("$lte", currentDate))),
//		            new Document(ApplicationConstants.ADD_FIELDS, new Document(TableConstants.USER_PROFILE, new Document("$arrayElemAt", Arrays.asList(TableConstants.USERPROFILE_$, 0))))
//		        );
//
//		        List<Document> results = mT.getCollection(TableConstants.USER_MANAGMENT_USER_PASSWORD_HISTORY).aggregate(pipeline).into(new ArrayList<>());
//		        List<UserPasswordHistory> entities = results.stream()
//		                .map(this::convertpwdDocumentToEntity)
//		                .collect(Collectors.toList());
//
//		        return entities;
//	}
//	 public UserPasswordHistory convertpwdDocumentToEntity(Document document) {
//	        return mT.getConverter().read(UserPasswordHistory.class, document);
//	    }
//
//
//	@Override
//	public Integer getPasswordChangeHour(String property) {
//
//		List<Document> pipeline = Arrays.asList(
//				new Document("$lookup",
//						new Document("from", "property_config").append("localField", "propertyId.$id")
//								.append("foreignField", "_id").append("as", "property")),
//				new Document("$match", new Document("property.property", property)), new Document("$addFields",
//						new Document("property", new Document("$arrayElemAt", Arrays.asList("$property", 0)))));
//
//		List<Document> results = mT.getCollection("property_config_value").aggregate(pipeline).into(new ArrayList<>());
//		List<propertyConfigValueEntity> entities = results.stream().map(this::convertDocumentToEntity)
//				.collect(Collectors.toList());
//
//		if (!entities.isEmpty()) {
//			return entities.get(0).getPropertyValue();
//		} else {
//			return null;
//		}
//	}
//
//	public propertyConfigValueEntity convertDocumentToEntity(Document document) {
//		return mT.getConverter().read(propertyConfigValueEntity.class, document);
//	}
//
//}
