//package com.supercharge.gateway.user.passwordhistory.dao;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.stream.Collectors;
//
//import org.bson.Document;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.stereotype.Repository;
//
//import com.cbt.supercharge.common.user.passwordhistory.dao.IUserPasswordHistoryDao;
//import com.cbt.supercharge.constants.core.ApplicationConstants;
//import com.cbt.supercharge.constants.core.TableConstants;
//import com.cbt.supercharge.exception.core.ApplicationException;
//import com.cbt.supercharge.transfer.objects.entity.UserPasswordHistory;
//import com.supercharge.gateway.utils.core.ApplicationUtils;
//
//@Repository
//@Qualifier("mongo")
//public class UserPasswordHistoryDaoImplMongo  implements IUserPasswordHistoryDao {
//
//
//	@Autowired
//	MongoTemplate mT;
//	
//	@Override
//	public List<UserPasswordHistory> getHistoryWithResetLimit(Integer userId, Integer limit)
//			throws ApplicationException {
//	      List<Document> pipeline = Arrays.asList(
//	              new Document(ApplicationConstants.LOOKUP, new Document(ApplicationConstants.FROM, TableConstants.USER_MANAGMENT_USER_INFO)
//	                      .append(ApplicationConstants.LOCAL_FIELD, TableConstants.USER_PROFILE + TableConstants.I_D)
//	                      .append(ApplicationConstants.FOREIGN_FIELD, TableConstants._ID)
//	                      .append(ApplicationConstants.AS, TableConstants.USER)),
//	              new Document(ApplicationConstants.MATCH, new Document(TableConstants.USER + TableConstants.USERID, userId)),
//	              new Document(ApplicationConstants.SORT, new Document(TableConstants.UM_PWD_HT_ID, ApplicationConstants.MINUSONE))
//	      );
//	      
//	      List<Document> results = mT.getCollection(TableConstants.USER_MANAGMENT_USER_PASSWORD_HISTORY).aggregate(pipeline).into(new ArrayList<>());
//	      List<UserPasswordHistory> entities = results.stream()
//	                .map(this::convertDocumentToEntity)
//	                .collect(Collectors.toList());
//
//	            return entities;
//	}
//
//	public UserPasswordHistory convertDocumentToEntity(Document document) {
//        return mT.getConverter().read(UserPasswordHistory.class, document);
//    }
//	
//	/**
//	 * @param history
//	 */
//	@Override
//	public void savePasswordHistory(UserPasswordHistory history) {
//		history.setUmPwdhtId(ApplicationUtils.getNextSequenceForId(TableConstants._ID, ApplicationUtils.getMongoClient(),TableConstants.USER_MANAGMENT_USER_PASSWORD_HISTORY));
//		mT.save(history);
//		
//	}
//
//}
