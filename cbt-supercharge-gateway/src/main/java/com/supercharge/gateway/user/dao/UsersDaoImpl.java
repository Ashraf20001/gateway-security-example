package com.supercharge.gateway.user.dao;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.cbt.supercharge.constants.core.TableConstants;
import com.cbt.supercharge.exception.core.ApplicationException;
import com.cbt.supercharge.transfer.objects.entity.UserProfile;
import com.cbt.supercharge.utils.core.ApplicationUtils;

import reactor.core.publisher.Mono;

@Repository
public class UsersDaoImpl extends GatewayBaseDao implements IUsersDao {

	@Autowired
	MongoTemplate mT;

	/**
	 * @param userID
	 * @return
	 */
	@Override
	public UserProfile getUserBySystemUserID(String userId) {
		if (ApplicationUtils.isValidString(userId))
			userId = userId.trim();
		Criteria criteria = Criteria.where(TableConstants.SYSTEM_USER_ID).is(userId).and(TableConstants.IS_DELETED)
				.is(false);
		Query query = new Query(criteria);
		return mT.findOne(query, UserProfile.class);
	}

	/**
	 * @param identity
	 * @return
	 */
	@Override
	public UserProfile getUserByIdentity(String identity) {
		Criteria criteria = Criteria.where(TableConstants.IDENTITY).is(identity);
		Query query = new Query(criteria);
		return mT.findOne(query, UserProfile.class);
	}

	@Override
	public String updateUser(UserProfile userProfile) {
		Query query = new Query(Criteria.where(TableConstants._ID).is(userProfile.get_id()));
		Update update = new Update();
		Document doc = new Document();
		this.mT.getConverter().write(userProfile, doc);
		update = update.fromDocument(doc);
		mT.upsert(query, update, TableConstants.USER_MANAGMENT_USER_INFO);
		return userProfile.getIdentity();
	}

	/**
	 * @param userName
	 * @return
	 */
	@Override
	public UserProfile getUserByName(String userName) {
		Criteria criteria = Criteria.where(TableConstants.EMAIL_ID).is(userName).and(TableConstants.IS_DELETED)
				.is(false);
		Query query = new Query(criteria);
		return mT.findOne(query, UserProfile.class);
	}

	/**
	 * @param email
	 * @return
	 */
	@Override
	public UserProfile getUserByEmailId(String email) {
		Criteria criteria = Criteria.where(TableConstants.EMAIL_ID).is(email).and(TableConstants.IS_DELETED).is(false);
		Query query = new Query(criteria);
		return mT.findOne(query, UserProfile.class);
	}

//	@Override
//	public Mono<String> getLoggedInUserName() throws ApplicationException {
//		return super.getLoggedInUserName();
//	}
	
	@Override
	public String getLoggedInUserName() throws ApplicationException {
		return super.getLoggedInUserName();
	}
	
	@Override
	public void registerDataFilters() {
		// TODO Auto-generated method stub
		
	}

}
