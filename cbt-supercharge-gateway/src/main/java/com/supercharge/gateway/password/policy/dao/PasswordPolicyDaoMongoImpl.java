//package com.supercharge.gateway.password.policy.dao;
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
//import com.cbt.supercharge.common.password.policy.dao.IPasswordPolicyDao;
//import com.cbt.supercharge.constants.core.TableConstants;
//import com.cbt.supercharge.exception.core.ApplicationException;
//import com.cbt.supercharge.transfer.objects.entity.PasswordPolicy;
//
//@Repository
//@Qualifier("mongo")
//public class PasswordPolicyDaoMongoImpl implements IPasswordPolicyDao {
//	
//	@Autowired
//	MongoTemplate mT;
//
//	@Override
//	public PasswordPolicy getPassWordPolicyById(Integer id) throws ApplicationException {
//		List<Document> pipeline = Arrays.asList(
//				new Document("$lookup",
//						new Document("from", TableConstants.INSTITUTION_INFO).append("localField", "institution.$id")
//								.append("foreignField", TableConstants._ID).append("as", "institution")),
//				new Document("$unwind", "$institution"),
//				new Document("$match", new Document("institution.institutionId", id)),
//				new Document("$match", new Document("institution.isActive", 1)));
//
//		List<Document> passwordPolicyDocument = mT.getCollection(TableConstants.PASSWORD_POLICY).aggregate(pipeline).into(new ArrayList<>());
//		List<PasswordPolicy> collect = passwordPolicyDocument.stream().map(this::convertDocumentToSubListEntity).collect(Collectors.toList());
//		return collect.isEmpty() ? null : collect.get(0);
//	}
//	
//	public PasswordPolicy convertDocumentToSubListEntity(Document document) {
//	    return mT.getConverter().read(PasswordPolicy.class, document);
//	}
//
//}
