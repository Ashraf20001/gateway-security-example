//package com.supercharge.gateway.user.institution.linking.dao;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.stream.Collectors;
//
//import org.bson.Document;
//import org.bson.conversions.Bson;
//import org.bson.types.ObjectId;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.data.mongodb.core.query.Criteria;
//import org.springframework.data.mongodb.core.query.Query;
//import org.springframework.data.mongodb.core.query.Update;
//import org.springframework.stereotype.Repository;
//
//import com.cbt.supercharge.common.user.institution.linking.dao.IUserInstitutionLinkDao;
//import com.cbt.supercharge.constants.core.ApplicationConstants;
//import com.cbt.supercharge.constants.core.TableConstants;
//import com.cbt.supercharge.exception.core.ApplicationException;
//import com.cbt.supercharge.transfer.objects.entity.Company;
//import com.cbt.supercharge.transfer.objects.entity.Institution;
//import com.cbt.supercharge.transfer.objects.entity.Operation;
//import com.cbt.supercharge.transfer.objects.entity.Role;
//import com.cbt.supercharge.transfer.objects.entity.SystemConfig;
//import com.cbt.supercharge.transfer.objects.entity.UserAndBusinessUnitLinking;
//import com.cbt.supercharge.transfer.objects.entity.UserAndInstitutionLinking;
//import com.cbt.supercharge.transfer.objects.entity.UserProfile;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.mongodb.DBRef;
//import com.mongodb.client.AggregateIterable;
//import com.mongodb.client.model.Filters;
//import com.supercharge.gateway.utils.core.ApplicationUtils;
//
///**
// * The Class UserInstitutionLinkDaoImpl.
// *
// * @author CBT
// */
//@Repository
//@Qualifier("mongo")
//public class UserInstitutionLinkDaoMongoImpl implements IUserInstitutionLinkDao {
//	
//	@Autowired
//	MongoTemplate mT;
//
//	@Override
//	public Long getRolesCountByInstitutionId(String institutionId) throws ApplicationException {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public UserAndInstitutionLinking getUserAndInstitutionLinking(Integer userId, Institution institution)
//			throws ApplicationException {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public List<UserAndInstitutionLinking> getUserAndInstitutionLinkingByUserId(UserProfile user)
//			throws ApplicationException {
//		ObjectId objectId = new ObjectId(user.get_id());
//		List<Document> pipeline = Arrays.asList(
//                new Document(ApplicationConstants.LOOKUP,
//                        new Document(ApplicationConstants.FROM, TableConstants.USER_MANAGMENT_USER_INFO)
//                                .append(ApplicationConstants.LOCAL_FIELD,
//                                        TableConstants.USER_PROFILE + TableConstants.ID_$)
//                                .append(ApplicationConstants.FOREIGN_FIELD, TableConstants._ID)
//                                .append(ApplicationConstants.AS, TableConstants.USER_PROFILE)),
//                new Document(ApplicationConstants.MATCH, new Document(TableConstants.USER_PROFILE_USERID_ID, objectId)),
//                new Document(ApplicationConstants.MATCH, new Document(TableConstants.IS_DELETED, false)),
//                new Document(ApplicationConstants.ADD_FIELDS,
//                        new Document(TableConstants.USER_PROFILE, new Document(ApplicationConstants.ARRAY_ELEMAT,
//                                Arrays.asList(ApplicationConstants.USER_PROFILE_$, 0)))));
//
//        List<Document> results = mT.getCollection(TableConstants.USER_MANAGEMENT_INS_USR_INFO).aggregate(pipeline)
//                .into(new ArrayList<>());
//		return results.stream().map(this::convertDocumentToUserAndInstitutionLinking).collect(Collectors.toList());
//	}  
//
//	@Override
//	public Long getUserCountByInstitutionId(String institutionId) throws ApplicationException {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public List<String> getUserMappedInstitutionIds(Integer userId) throws ApplicationException {
//		List<Document> pipeline = Arrays.asList(
//                new Document(ApplicationConstants.LOOKUP,
//                        new Document(ApplicationConstants.FROM, TableConstants.USER_MANAGMENT_USER_INFO)
//                                .append(ApplicationConstants.LOCAL_FIELD,
//                                        TableConstants.USER_PROFILE + TableConstants.ID_$)
//                                .append(ApplicationConstants.FOREIGN_FIELD, TableConstants._ID)
//                                .append(ApplicationConstants.AS, TableConstants.USER_PROFILE)),
//                new Document(ApplicationConstants.MATCH, new Document(TableConstants.USER_PROFILE_USER_ID, userId)),
//                new Document(ApplicationConstants.MATCH, new Document(TableConstants.ISDELETED, false)),
//                new Document(ApplicationConstants.ADD_FIELDS,
//                        new Document(TableConstants.USER_PROFILE, new Document(ApplicationConstants.ARRAY_ELEMAT,
//                                Arrays.asList(ApplicationConstants.USER_PROFILE_$, 0)))));
//
//        List<Document> results = mT.getCollection(TableConstants.USER_MANAGEMENT_INS_USR_INFO).aggregate(pipeline)
//                .into(new ArrayList<>());
//        List<UserAndInstitutionLinking> userAndInstitutionLinking = new ArrayList<>();
//        for (Document userLinking : results) {
//            UserAndInstitutionLinking role = convertDocumentToUserAndInstitutionLinking(userLinking);
//            userAndInstitutionLinking.add(role);
//        }
//        return userAndInstitutionLinking.stream().map(x -> x.getInstitution().getIdentity()).toList();
//	}
//
//	@Override
//	public List<UserAndInstitutionLinking> getUserRoleById(UserProfile user) throws ApplicationException {
//		ObjectId objectId = new ObjectId(user.get_id());
//		List<Document> pipeline = Arrays.asList(
//                new Document(ApplicationConstants.LOOKUP,
//                        new Document(ApplicationConstants.FROM, TableConstants.USER_MANAGMENT_USER_INFO)
//                                .append(ApplicationConstants.LOCAL_FIELD,
//                                        TableConstants.USER_PROFILE + TableConstants.ID_$)
//                                .append(ApplicationConstants.FOREIGN_FIELD, TableConstants._ID)
//                                .append(ApplicationConstants.AS, TableConstants.USER_PROFILE)),
//                new Document(ApplicationConstants.MATCH, new Document(TableConstants.USER_PROFILE_USERID_ID, objectId)),
//                new Document(ApplicationConstants.MATCH, new Document(TableConstants.ISDELETED, false)),
//                new Document(ApplicationConstants.ADD_FIELDS,
//                        new Document(TableConstants.USER_PROFILE, new Document(ApplicationConstants.ARRAY_ELEMAT,
//                                Arrays.asList(ApplicationConstants.USER_PROFILE_$, 0)))));
//
//        List<Document> results = mT.getCollection(TableConstants.USER_MANAGEMENT_INS_USR_INFO).aggregate(pipeline)
//                .into(new ArrayList<>());
//        List<UserAndInstitutionLinking> userAndInstitutionLinking = new ArrayList<>();
//        for (Document userLinking : results) {
//            UserAndInstitutionLinking role = convertDocumentToUserAndInstitutionLinking(userLinking);
//            userAndInstitutionLinking.add(role);
//        }
//        return userAndInstitutionLinking;
//	}
//
//	@Override
//	public List<UserAndInstitutionLinking> getUserRoleLinkingList(Role role) throws ApplicationException {
//		ObjectId roleObjectId = new ObjectId(role.get_id());
//		AggregateIterable<Document> result = mT.getCollection(TableConstants.USER_MANAGEMENT_INS_USR_INFO)
//				.aggregate(Arrays.asList(
//						new Document("$lookup",
//								new Document("from", TableConstants.ROLE_INFO)
//										.append("localField", TableConstants.ROLE_JOIN_COLLECTION_ID)
//										.append("foreignField", TableConstants._ID).append("as", TableConstants.ROLE)),
//						new Document("$unwind", TableConstants.ROLE_$),
//						new Document("$match", new Document("role._id", roleObjectId)),
//								new Document("$lookup",
//										new Document("from", TableConstants.USER_MANAGMENT_USER_INFO)
//												.append("localField", TableConstants.USER_JOIN_COLLECTION_ID)
//												.append("foreignField", TableConstants._ID).append("as", TableConstants.USER_PROFILE)),
//								new Document("$unwind", TableConstants.USERPROFILE_$ ),
//								new Document("$match",new Document(TableConstants.IS_DELETED, false))
//								));
//		List<UserAndInstitutionLinking> userAndInstitutionLinking = new ArrayList<>();
//
//		for (Document userAndInstitutionLnkg : result) {
//			UserAndInstitutionLinking userRole = convertDocumentToUserAndInstitutionLinking(userAndInstitutionLnkg);
//			userAndInstitutionLinking.add(userRole);
//		}
//		return userAndInstitutionLinking;
//	
//		
//	}
//
//	private UserAndInstitutionLinking convertDocumentToUserAndInstitutionLinking(Document document) {
//		return mT.getConverter().read(UserAndInstitutionLinking.class, document);
//	}
//
//	@Override
//	public Integer saveUserInstitutionLink(UserAndInstitutionLinking user) throws ApplicationException {
//		user.setUserAndInstitutionLinkingId(ApplicationUtils.getNextSequenceForId("_id", ApplicationUtils.getMongoClient(),TableConstants.USER_MANAGEMENT_INS_USR_INFO));
//		mT.save(user);
//		return user.getUserAndInstitutionLinkingId();
//	}
//
//	@Override
//	public void updateUserInstitutionLink(UserAndInstitutionLinking businessUnit)
//			throws ApplicationException, JsonProcessingException {
//		Query query = new Query(Criteria.where("_id").is(businessUnit.get_id()));
//		Update update = new Update();
//		Document doc = new Document();
//		this.mT.getConverter().write(businessUnit, doc);
//		update = update.fromDocument(doc);
//		this.mT.upsert(query, update, TableConstants.USER_MANAGEMENT_INS_USR_INFO);
//
//	}
//
//	@Override
//	public Company getcompanyIdByComapanyName(String comapnyname) throws ApplicationException {
//		Query query = new Query();
//		query.addCriteria(Criteria.where(TableConstants.COMPANY_Name).is(comapnyname));
//		return mT.findOne(query, Company.class);
//	}
//
//	@Override
//	public List<UserAndInstitutionLinking> getListUserAndInstitutionLinking(UserProfile userProfile, Institution institution)
//			throws ApplicationException {
//		ObjectId objectId = new ObjectId(userProfile.get_id());
//		ObjectId object_Id = new ObjectId(institution.get_id());
//		List<Document> pipeline = Arrays.asList(
//		    new Document("$lookup", new Document("from", "user_managment_user_info")
//		            .append("localField", "userProfile.$id")
//		            .append("foreignField", "_id")
//		            .append("as", "userProfile")),
//		    new Document("$addFields", new Document("userProfile", new Document("$arrayElemAt", Arrays.asList("$userProfile", 0)))),
//		    new Document("$match", new Document("userProfile._id", objectId)),
//		    new Document("$lookup", new Document("from", "master_institution_info")
//		            .append("localField", "institution.$id")
//		            .append("foreignField", "_id")
//		            .append("as", "institution")),
//		    new Document("$addFields", new Document("institution", new Document("$arrayElemAt", Arrays.asList("$institution", 0)))),
//		    new Document("$match", new Document("institution._id", object_Id)
//		            .append("isDeleted", false))
//		);
//		List<Document> results = mT.getCollection("user_management_user_institution_role_linking_table").aggregate(pipeline)
//		        .into(new ArrayList<>());
//
//		List<UserAndInstitutionLinking> entities = results.stream().map(this::convertDocumentToEntity)
//				.collect(Collectors.toList());
//		return entities;
//	}
//	
//	public UserAndInstitutionLinking convertDocumentToEntity(Document document) {
//		return mT.getConverter().read(UserAndInstitutionLinking.class, document);
//	}
//	
//
//	@Override
//	public List<SystemConfig> getSystemConfigDetails() {
//		Query query = new Query();
//		return mT.find(query, SystemConfig.class);
//	}
//
//	@Override
//	public void deleteUserInstitutionLink(UserAndInstitutionLinking userAndInstitutionLinking) {
//		
//		Bson filter = Filters.eq("userAndInstitutionLinkingId", userAndInstitutionLinking.getUserAndInstitutionLinkingId());
//		mT.getCollection(TableConstants.USER_MANAGEMENT_INS_USR_INFO).deleteOne(filter);
//	}
//
//	@Override
//	public List<UserAndInstitutionLinking> getUserMakerMappedRoles(Integer id) throws ApplicationException {
//		Criteria criteria = Criteria.where(TableConstants.MAKER_USER_ID).is(id);
//        Query query = new Query(criteria);
//		return mT.find(query, UserAndInstitutionLinking.class);
//	}
//
//
//	@Override
//	public void updateBusinessLink(UserAndBusinessUnitLinking userAndBusinessUnitLinking) throws ApplicationException, JsonProcessingException {
//		  Query query = new Query(Criteria.where("_id").is(userAndBusinessUnitLinking.get_id()));
//		    Update update = new Update();
//	        Document doc = new Document();
//	        this.mT.getConverter().write(userAndBusinessUnitLinking, doc);
//	        update = update.fromDocument(doc);
//	        this.mT.upsert(query, update, TableConstants.USER_MANAGEMENT_USER_BUSINESS_UNIT_LINKING);
//		
//	}
// 
//	@Override
//	public List<SystemConfig> getSystemConfigDetail(String systemConfig) throws ApplicationException {
//		Query query = new Query();
//		return mT.find(query, SystemConfig.class);
//	}
//
//	/**
//	 * @param operationNameList
//	 * @return
//	 */
//	@Override
//	public List<String> getOperationList(List<String> operationNameList) {
//		Criteria criteria = Criteria.where(TableConstants.OPERATION_NAME).in(operationNameList)
//				.and(TableConstants.IS_DELETED).is(false);
//		Query query = new Query(criteria);
//		query.fields().include(TableConstants._ID);
//		return mT.find(query, Operation.class).stream().map(Operation :: get_id).collect(Collectors.toList());
//		}
//
//		/**
//		 * @param userProfileMakerCheckerId
//		 * @param userData
//		 * @throws JsonProcessingException
//		 * @throws ApplicationException
//		 */
//		@Override
//		public void getUserMakerMappedRolesAndUpdate(Integer userProfileMakerCheckerId, UserProfile userData)
//				throws JsonProcessingException, ApplicationException {
//			Criteria criteria = Criteria.where(TableConstants.MAKER_USER_ID).is(userProfileMakerCheckerId);
//			Query query = new Query(criteria);
//			Update update = new Update().set(TableConstants.USER_PROFILE,
//							setDbrefValue(TableConstants.USER_MANAGMENT_USER_INFO, userData.get_id()))
//					.set(TableConstants.MAKER_USER_ID, null);
//			mT.updateMulti(query, update, TableConstants.USER_MANAGEMENT_INS_USR_INFO);
//		}
//		
//		/**
//		 * @param collectionName
//		 * @param id
//		 * @return
//		 */
//		private DBRef setDbrefValue(String collectionName, String id) {
//			return new DBRef(collectionName, new ObjectId(id));
//		}
//
//		/**
//		 * @param userAndInstitutionLinking
//		 * @param user
//		 * @param isDeleted
//		 * @param isMakerUserId
//		 * @throws ApplicationException
//		 * @throws JsonProcessingException
//		 */
//		@Override
//		public void updateMultiUserInstitutionLink(List<UserAndInstitutionLinking> userAndInstitutionLinking,
//				UserProfile user, Boolean isDeleted, Boolean isMakerUserId)
//				throws ApplicationException, JsonProcessingException {
//			List<ObjectId> objectIds = userAndInstitutionLinking.stream().map(a -> new ObjectId(a.get_id())).toList();
//			Criteria criteria = Criteria.where(TableConstants.MONGO_ID).in(objectIds);
//			Query query = new Query(criteria);
//			Update update = new Update();
//			if (ApplicationUtils.isValidateObject(user)) {
//				update.set(TableConstants.USER_PROFILE,
//						setDbrefValue(TableConstants.USER_MANAGMENT_USER_INFO, user.get_id()));
//			}
//			if (Boolean.TRUE.equals(isMakerUserId))
//				update.set(TableConstants.MAKER_USER_ID, null);
//			update.set(TableConstants.IS_DELETED, isDeleted);
//			mT.updateMulti(query, update, TableConstants.USER_MANAGEMENT_INS_USR_INFO);
//		}
//
//}
