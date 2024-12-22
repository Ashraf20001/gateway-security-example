//package com.supercharge.gateway.user.password.dao;
//
//import java.util.Arrays;
//import java.util.List;
//
//import org.bson.Document;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.data.mongodb.core.query.Criteria;
//import org.springframework.data.mongodb.core.query.Query;
//import org.springframework.data.mongodb.core.query.Update;
//import org.springframework.stereotype.Repository;
//
//import com.cbt.supercharge.common.user.password.dao.IUserPasswordDao;
//import com.cbt.supercharge.constants.core.TableConstants;
//import com.cbt.supercharge.exception.core.ApplicationException;
//import com.cbt.supercharge.transfer.objects.entity.UserPassword;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.supercharge.gateway.utils.core.ApplicationUtils;
//
//@Repository
//@Qualifier("mongo")
//public class UserPasswordDaoImplMongo implements IUserPasswordDao{
//
//	@Autowired
//	MongoTemplate mT;
//	
//	
//	@Override
//	public UserPassword getPasswordByUserId(Integer userId) {
//	    List<Document> pipeline = Arrays.asList(
//	            new Document("$lookup", new Document("from", "user_managment_user_info")
//	                    .append("localField", "userProfile.$id")
//	                    .append("foreignField", "_id")
//	                    .append("as", "userProfile")),
//	            new Document("$match", new Document("userProfile.userId", userId)),
//				new Document("$addFields", new Document("userProfile", new Document("$arrayElemAt", Arrays.asList("$userProfile", 0))))
//	    );
//
// 	    Document results = mT.getCollection("user_managment_user_password_link_table")
//	            .aggregate(pipeline).first();
// 	    if(results != null ) {
// 	    	return convertDocumentToEntity(results);
// 	    }
//            return null;
//	    
//	}
//	
//	public UserPassword convertDocumentToEntity(Document document) {
//        return mT.getConverter().read(UserPassword.class, document);
//    }
//
//	
//	/**
//	 * @param userPassword
//	 */
//	@Override
//	public void savePassword(UserPassword userPassword) {
//		userPassword.setUmPwdId(ApplicationUtils.getNextSequenceForId(TableConstants._ID, ApplicationUtils.getMongoClient(),TableConstants.USER_MANAGMENT_USER_PASSWORD_LINK_TABLE));
//		mT.save(userPassword);
//		
//	}
//
//	@Override
//	public void updatePassword(UserPassword userPassword) throws ApplicationException, JsonProcessingException {
//		Query query = new Query(Criteria.where(TableConstants.UM_PWD_ID).is(userPassword.getUmPwdId()));
//
//		Update update = new Update();
//		Document doc = new Document();
//		this.mT.getConverter().write(userPassword, doc);
//		update = update.fromDocument(doc);
//		this.mT.upsert(query, update, TableConstants.USER_MANAGMENT_USER_PASSWORD_LINK_TABLE);
//	}
//
//}
