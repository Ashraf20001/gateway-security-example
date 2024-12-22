package com.supercharge.gateway.common.base.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Collation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.cbt.supercharge.constants.core.ApplicationConstants;
import com.cbt.supercharge.constants.core.TableConstants;
import com.cbt.supercharge.exception.core.ApplicationException;
import com.cbt.supercharge.transfer.objects.common.entity.LoginCallCaptchaCheck;
import com.cbt.supercharge.transfer.objects.common.entity.ModuleRefForEnum;
import com.cbt.supercharge.transfer.objects.entity.Module;
import com.cbt.supercharge.transfer.objects.entity.PasswordPolicy;
import com.cbt.supercharge.transfer.objects.entity.ResetKey;
import com.cbt.supercharge.transfer.objects.entity.Role;
import com.cbt.supercharge.transfer.objects.entity.RoleMakerChecker;
import com.cbt.supercharge.transfer.objects.entity.RolesAndPrivilegeLinking;
import com.cbt.supercharge.transfer.objects.entity.SubModule;
import com.cbt.supercharge.transfer.objects.entity.SystemConfig;
import com.cbt.supercharge.transfer.objects.entity.UserAndInstitutionLinking;
import com.cbt.supercharge.transfer.objects.entity.UserPassword;
import com.cbt.supercharge.transfer.objects.entity.UserPasswordHistory;
import com.cbt.supercharge.transfer.objects.entity.UserProfile;
import com.cbt.supercharge.transfter.objects.core.dto.FilterOrSortingVo;
import com.cbt.supercharge.transfter.objects.core.dto.Modules;
import com.cbt.supercharge.transfter.objects.core.dto.RoleApis;
import com.cbt.supercharge.transfter.objects.core.entity.vo.MappedModulesDto;
import com.cbt.supercharge.transfter.objects.master.Menus;
import com.cbt.supercharge.utils.core.ApplicationUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.client.AggregateIterable;
import com.supercharge.gateway.common.handlers.CustomException;

@Repository
@Qualifier("mongo")
public class CommonUserDaoImpl {

	@Autowired
	private CustomAggregationBuilderGateway customAggregationBuilder;

	@Autowired
	private GatewayMasterFilterMongoFactory masterFilterFactory;

	@Autowired
	private GatewaySortingMongoFilter sortingMongoFilter;

	@Autowired
	MongoTemplate mT;

	public UserProfile getUserBySystemUserID(String userId) {
		if (ApplicationUtils.isValidString(userId)) {
			userId = userId.trim();
		}
		Criteria criteria = Criteria.where(TableConstants.SYSTEM_USER_ID).is(userId).and(TableConstants.IS_DELETED)
				.is(false);
		Query query = new Query(criteria);
		return mT.findOne(query, UserProfile.class);
	}

	public List<SystemConfig> getSystemPropertyGroup(String propertyGroup) {
		Criteria criteria = Criteria.where(TableConstants.PROPERTY_GROUP).is(propertyGroup);
		Query query = new Query(criteria);
		return mT.find(query, SystemConfig.class);
	}

	public List<SystemConfig> getSystemConfigDetails() {
		Query query = new Query();
		return mT.find(query, SystemConfig.class);
	}

	public String updateUser(UserProfile userProfile) throws JsonProcessingException, CustomException {
		Query query = new Query(Criteria.where(TableConstants._ID).is(userProfile.get_id()));
		Update update = new Update();
		Document doc = new Document();
		this.mT.getConverter().write(userProfile, doc);
		update = update.fromDocument(doc);
		mT.upsert(query, update, TableConstants.USER_MANAGMENT_USER_INFO);
		return userProfile.getIdentity();
	}

	public UserPassword getPasswordByUserId(Integer userId) {
		List<Document> pipeline = Arrays.asList(
				new Document("$lookup",
						new Document("from", "user_managment_user_info").append("localField", "userProfile.$id")
								.append("foreignField", "_id").append("as", "userProfile")),
				new Document("$match", new Document("userProfile.userId", userId)), new Document("$addFields",
						new Document("userProfile", new Document("$arrayElemAt", Arrays.asList("$userProfile", 0)))));

		Document results = mT.getCollection("user_managment_user_password_link_table").aggregate(pipeline).first();
		if (results != null) {
			return convertDocumentToEntity(results);
		}
		return null;

	}

	public UserPassword convertDocumentToEntity(Document document) {
		return mT.getConverter().read(UserPassword.class, document);
	}

	public UserProfile getUserByUserId(String userId) {
		Criteria criteria = Criteria.where(TableConstants.SYSTEM_USER_ID).is(userId).and(TableConstants.IS_DELETED)
				.is(false);
		Query query = new Query(criteria);
		return mT.findOne(query, UserProfile.class);
	}

	public List<UserAndInstitutionLinking> getUserAndInstitutionLinkingByUserId(UserProfile user)
			throws CustomException {
		ObjectId objectId = new ObjectId(user.get_id());
		List<Document> pipeline = Arrays.asList(
				new Document(ApplicationConstants.LOOKUP,
						new Document(ApplicationConstants.FROM, TableConstants.USER_MANAGMENT_USER_INFO)
								.append(ApplicationConstants.LOCAL_FIELD,
										TableConstants.USER_PROFILE + TableConstants.ID_$)
								.append(ApplicationConstants.FOREIGN_FIELD, TableConstants._ID)
								.append(ApplicationConstants.AS, TableConstants.USER_PROFILE)),
				new Document(ApplicationConstants.MATCH, new Document(TableConstants.USER_PROFILE_USERID_ID, objectId)),
				new Document(ApplicationConstants.MATCH, new Document(TableConstants.IS_DELETED, false)),
				new Document(ApplicationConstants.ADD_FIELDS,
						new Document(TableConstants.USER_PROFILE, new Document(ApplicationConstants.ARRAY_ELEMAT,
								Arrays.asList(ApplicationConstants.USER_PROFILE_$, 0)))));

		List<Document> results = mT.getCollection(TableConstants.USER_MANAGEMENT_INS_USR_INFO).aggregate(pipeline)
				.into(new ArrayList<>());
		return results.stream().map(this::convertDocumentToUserAndInstitutionLinking).collect(Collectors.toList());
	}

	private UserAndInstitutionLinking convertDocumentToUserAndInstitutionLinking(Document document) {
		return mT.getConverter().read(UserAndInstitutionLinking.class, document);
	}

	public SystemConfig getSystemConfigName(String configName) {
		Criteria criteria = Criteria.where(TableConstants.CONFIG_NAME).is(configName);
		Query query = new Query(criteria);
		return mT.findOne(query, SystemConfig.class);
	}

	public Long getRolesCount(List<FilterOrSortingVo> filterVos) throws ApplicationException {
		List<AggregationOperation> aggregationOperationsList = new ArrayList<>();
		Criteria criteria = new Criteria();
		criteria.and(TableConstants.ISDELETED).is(false);
		AggregationOperation matchOperation = Aggregation.match(criteria);
		aggregationOperationsList.add(matchOperation);
		Document sortDocument = new Document();
		if (ApplicationUtils.isValidList(filterVos)) {
			for (FilterOrSortingVo filter : filterVos) {
				if (filter.getColumnName().equals(TableConstants.CREATED_BY_USER_NAME)) {
					buildPredicateForUserName(aggregationOperationsList, TableConstants.CREATED_BY_USER,
							TableConstants.CREATED_BY_USER_NAME);
				} else if (filter.getColumnName().equals(TableConstants.MODIFIED_BY_USER_NAME)) {
					buildPredicateForModifiedUserName(aggregationOperationsList, TableConstants.MODIFIED_BY,
							TableConstants.MODIFIED_BY_USER_NAME);
				}
			}

			List<Document> conditions = new ArrayList<>();

			for (FilterOrSortingVo filterVo : filterVos) {
				Document document = null;
				document = getFilterPrdicets(filterVo);
				if (document != null && document.size() > 0) {
					conditions.add(document);
				}
			}

			if (ApplicationUtils.isValidList(conditions)) {
				// Build the $match stage with $and operator
				Document match = new Document(ApplicationConstants.MONGO_AND, conditions);
				// Build the aggregation pipeline
				AggregationOperation matchOp = customAggregationBuilder.buildFilter(match);
				aggregationOperationsList.add(matchOp);
			}

			FilterOrSortingVo sortFilters = filterVos.stream()
					.filter(a -> a.getFilterOrSortingType().equals(ApplicationConstants.SORTING)).findFirst()
					.orElse(null);

			if (ApplicationUtils.isValidateObject(sortFilters)
					&& sortFilters.getFilterOrSortingType().equals(ApplicationConstants.SORTING)) {
				sortDocument = applySorting(sortFilters);
				AggregationOperation sortAscOrDesc = customAggregationBuilder.buildSort(sortDocument);
				aggregationOperationsList.add(sortAscOrDesc);
			}
		}
		Aggregation aggregationFilter = Aggregation.newAggregation(aggregationOperationsList);
		List<Document> role = mT.aggregate(aggregationFilter, TableConstants.ROLE_INFO, Document.class)
				.getMappedResults();
		return role.stream().map(this::convertDocumentToRoleEntity).count();
	}

	/**
	 * ̥
	 * 
	 * @param aggregationOperationsList
	 * @param modifiedBy
	 * @param modifiedByUserName
	 */
	protected void buildPredicateForModifiedUserName(List<AggregationOperation> aggregationOperationsList,
			String modifiedBy, String modifiedByUserName) {
		Document lookupUsrModifiedBy = userLookupDto(modifiedBy, TableConstants.MOD_USR_ALIAS);
		AggregationOperation unwindUsrModifiedBy = Aggregation.unwind(TableConstants.MOD_USR_UNWIND, true);
		Document setModifiedByDoc = new Document(modifiedByUserName, TableConstants.MODIFIED_USR_SET);
		Document modifiedUsrProject = new Document(TableConstants.MOD_USR_ALIAS, 0);
		AggregationOperation lookupUsrModifiedByDoc = customAggregationBuilder.buildLookUp(lookupUsrModifiedBy);
		AggregationOperation buildModifiedBySet = customAggregationBuilder.buildSet(setModifiedByDoc);
		AggregationOperation buildUsrModProject = customAggregationBuilder.buildProject(modifiedUsrProject);
		aggregationOperationsList.add(lookupUsrModifiedByDoc);
		aggregationOperationsList.add(unwindUsrModifiedBy);
		aggregationOperationsList.add(buildModifiedBySet);
		aggregationOperationsList.add(buildUsrModProject);
	}

	/**
	 * @param aggregationOperationsList
	 * @param createdBy
	 * @param createdByUserName
	 */
	@SuppressWarnings("unused")
	protected void buildPredicateForUserName(List<AggregationOperation> aggregationOperationsList, String createdBy,
			String createdByUserName) {
		Document lookupUsrCreatedBy = userLookupDto(createdBy, TableConstants.USR_ALIAS);
		AggregationOperation unwindUsrCreatedBy = Aggregation.unwind(TableConstants.USR_UNWIND, true);
		Document setCreatedByDoc = new Document(createdByUserName, TableConstants.CREATED_USR_SET);
		Document createUsrProject = new Document(TableConstants.USR_ALIAS, 0);
		AggregationOperation lookupUsrCreatedByDoc = customAggregationBuilder.buildLookUp(lookupUsrCreatedBy);
		AggregationOperation buildCreatedBySet = customAggregationBuilder.buildSet(setCreatedByDoc);
		AggregationOperation buildUsrProject = customAggregationBuilder.buildProject(createUsrProject);
		aggregationOperationsList.add(lookupUsrCreatedByDoc);
		aggregationOperationsList.add(unwindUsrCreatedBy);
		aggregationOperationsList.add(buildCreatedBySet);
		aggregationOperationsList.add(buildUsrProject);
	}

	@SuppressWarnings("unused")
	protected Document userLookupDto(String createdByUser, String usrAlias) {
		Document lookupUsrCreatedBy = new Document(ApplicationConstants.MONGO_FROM,
				TableConstants.USER_MANAGMENT_USER_INFO).append(ApplicationConstants.MONGO_LOCALFIELD, createdByUser)
				.append(ApplicationConstants.MONGO_FORIEGNFIELD, TableConstants.USER_ID)
				.append(ApplicationConstants.MONGO_ALAIS, usrAlias);
		return lookupUsrCreatedBy;
	}

	/**
	 * @param filterVo
	 * @return
	 * @throws ApplicationException
	 */
	protected Document getFilterPrdicets(FilterOrSortingVo filterVo) throws ApplicationException {
		if (filterVo == null) {
			return new Document();
		}
		Document doc = null;
		if (filterVo.getFilterOrSortingType().equals(ApplicationConstants.FILTER)) {
			doc = applyFilter(filterVo);
		}
		return doc;

	}

	/**
	 * @param filterVo
	 * @return
	 * @throws ApplicationException
	 */
	protected Document applyFilter(FilterOrSortingVo filterVo) throws ApplicationException {
		if (filterVo.getCondition() != null && filterVo.getValue() != null) {
			Document predicate = masterFilterFactory.getFilterByName(filterVo.getCondition())
					.getFilterPredicate(filterVo);
			return predicate;
		}
		return null;
	}

	protected Document applySorting(FilterOrSortingVo filterVo) throws ApplicationException {
		if (filterVo.getColumnName() != null) {
			Document documentSort = sortingMongoFilter.getFilterPredicate(filterVo);
			if (documentSort != null) {
				return documentSort;
			}
		}
		return null;
	}

	public Role convertDocumentToRoleEntity(Document document) {
		return mT.getConverter().read(Role.class, document);
	}

	public List<RolesAndPrivilegeLinking> getRolePrivilegeList(String id) {
		Criteria criteria = Criteria.where(TableConstants.ROLEID_MONGO).is(id).and(TableConstants.IS_DELETED).is(false);
		Query query = new Query(criteria);
		return mT.find(query, RolesAndPrivilegeLinking.class);
	}

	public List<Modules> getMappedModulesByIdentities(List<String> modulesIdentities,
			List<String> mappedModuleIdentities) {

		List<AggregationOperation> aggregationOperations = new ArrayList<>();
		aggregationOperations.add(buildMatchOperationForNotIn(TableConstants.IDENTITY, mappedModuleIdentities,
				ApplicationConstants.NOT_IN));
		aggregationOperations.add(buildLookupOperation(TableConstants.MASTER_MODULE_INFO, TableConstants.MODULE_$ID,
				TableConstants.MONGO_ID, TableConstants.MODULE));
		aggregationOperations.add(buildUnwindOperation(ApplicationConstants.MODULE$));
		aggregationOperations.add(buildMatchOperation(ApplicationConstants.MODULE_IS_DELETED, false,
				ApplicationConstants.MODULE_IDENTITY, modulesIdentities, ApplicationConstants.IN));
		aggregationOperations.add(buildLookupOperation(TableConstants.MASTER_SUB_MODULE, TableConstants.SUB_MODULE_$ID,
				TableConstants.MONGO_ID, TableConstants.SUB_MODULE));
		aggregationOperations.add(buildUnwindOperation(ApplicationConstants.SUB_MODULE$));
		aggregationOperations.add(buildMatchOperation(ApplicationConstants.SUB_MODULE_DELETED, false));

		aggregationOperations.add(buildLookupOperation(TableConstants.MASTER_OPERATION_INFO, TableConstants.MONGO_ID,
				TableConstants.MONGO_ID, TableConstants.OPERATION));
		aggregationOperations.add(buildUnwindOperation(TableConstants.OPERATION_$));
		aggregationOperations.add(buildAddFieldsOperation(TableConstants.OPERATION, TableConstants.OPERATION_$));

		Aggregation aggregation = Aggregation.newAggregation(aggregationOperations);

		return mT.aggregate(aggregation, TableConstants.MASTER_OPERATION_INFO, Modules.class).getMappedResults()
				.stream().collect(Collectors.toList());
	}

	private AggregationOperation buildUnwindOperation(String path) {
		return customAggregationBuilder.buildUnwind(path);
	}

	private AggregationOperation buildMatchOperation(String key, Object value) {
		Document matchModuleStage = new Document(key, value);
		return customAggregationBuilder.buildFilter(matchModuleStage);
	}

	private AggregationOperation buildLookupOperation(String from, String localField, String foreignField, String as) {

		Document lookupStage = new Document(ApplicationConstants.FROM, from)
				.append(ApplicationConstants.LOCAL_FIELD, localField)
				.append(ApplicationConstants.FOREIGN_FIELD, foreignField).append(ApplicationConstants.AS, as);

		return customAggregationBuilder.buildLookUp(lookupStage);
	}

	private AggregationOperation buildMatchOperationForNotIn(String key2, List<?> values, String condition) {

		Document matchModuleStage = new Document(key2, new Document(condition, values));
		return customAggregationBuilder.buildFilter(matchModuleStage);
	}

	private AggregationOperation buildAddFieldsOperation(String field, String value) {
		Document addFieldDocument = new Document(field, value);
		return customAggregationBuilder.buildAddFields(addFieldDocument);
	}

	private AggregationOperation buildMatchOperation(String keyField, Object fieldValue, String conditionKey,
			List<?> conditionValues, String condition) {

		Document matchModuleStage = new Document(keyField, fieldValue).append(conditionKey,
				new Document(condition, conditionValues));
		return customAggregationBuilder.buildFilter(matchModuleStage);
	}

	public List<SubModule> getSubModuleByModulesIdentities(List<String> moduless) {
		List<AggregationOperation> aggregationOperations = new ArrayList<>();

		aggregationOperations.add(buildLookupOperation(TableConstants.MASTER_MODULE_INFO, TableConstants.MODULE_$ID,
				TableConstants.MONGO_ID, TableConstants.MODULE));
		aggregationOperations.add(buildUnwindOperation(ApplicationConstants.MODULE$));
		aggregationOperations.add(buildMatchOperation(ApplicationConstants.MODULE_IS_DELETED, false,
				ApplicationConstants.MODULE_IDENTITY, moduless, ApplicationConstants.IN));

		aggregationOperations.add(buildMatchOperation(ApplicationConstants.IS_DELETED, false));

		Aggregation aggregation = Aggregation.newAggregation(aggregationOperations);
		return mT.aggregate(aggregation, TableConstants.MASTER_SUB_MODULE, SubModule.class).getMappedResults().stream()
				.collect(Collectors.toList());
	}

	public List<Modules> getUnMappedModules(List<String> mappedModuleIdentities) {
		List<AggregationOperation> aggregationOperations = new ArrayList<>();

		aggregationOperations.add(buildLookupOperation(TableConstants.MASTER_MODULE_INFO, TableConstants.MODULE_$ID,
				TableConstants.MONGO_ID, TableConstants.MODULE));
		aggregationOperations.add(buildUnwindOperation(ApplicationConstants.MODULE$));
		aggregationOperations.add(buildMatchOperation(ApplicationConstants.MODULE_IS_DELETED, false,
				ApplicationConstants.MODULE_IDENTITY, mappedModuleIdentities, ApplicationConstants.NOT_IN));
		aggregationOperations.add(buildLookupOperation(TableConstants.MASTER_SUB_MODULE, TableConstants.SUB_MODULE_$ID,
				TableConstants.MONGO_ID, TableConstants.SUB_MODULE));
		aggregationOperations.add(buildUnwindOperation(ApplicationConstants.SUB_MODULE$));
		aggregationOperations.add(buildMatchOperation(ApplicationConstants.SUB_MODULE_DELETED, false));
		aggregationOperations.add(buildLookupOperation(TableConstants.MASTER_OPERATION_INFO, TableConstants.MONGO_ID,
				TableConstants.MONGO_ID, TableConstants.OPERATION));
		aggregationOperations.add(buildUnwindOperation(TableConstants.OPERATION_$));
		aggregationOperations.add(buildAddFieldsOperation(TableConstants.OPERATION, TableConstants.OPERATION_$));

		Aggregation aggregation = Aggregation.newAggregation(aggregationOperations);

		return mT.aggregate(aggregation, TableConstants.MASTER_OPERATION_INFO, Modules.class).getMappedResults()
				.stream().collect(Collectors.toList());
	}

	public List<Module> getUnMappedMenus() {
		Query query = new Query();
		query.addCriteria(Criteria.where(TableConstants.IS_DELETED).is(false));
		return mT.find(query, Module.class);
	}

	public List<Menus> getMenuListBySubModuleIds(List<Integer> submoduleIds) {
		Criteria criteria = new Criteria();
		criteria.and(TableConstants.IS_DELETED).is(false);
		if (ApplicationUtils.isValidList(submoduleIds)) {
			criteria.and(TableConstants.REF_MODULE_ID).in(submoduleIds).and(TableConstants.REF_FOR)
					.is(ModuleRefForEnum.SUB_MODULE.getType());
		}
		Query query = new Query(criteria);
		return mT.find(query, Menus.class);
	}

	public List<Menus> getMenuListModuleIds(List<Integer> moduleIds) {
		Criteria criteria = new Criteria();
		criteria.and(TableConstants.IS_DELETED).is(false);
		if (ApplicationUtils.isValidList(moduleIds)) {
			criteria.and(TableConstants.REF_MODULE_ID).in(moduleIds).and(TableConstants.REF_FOR)
					.is(ModuleRefForEnum.MODULE.getType());
		}
		Query query = new Query(criteria);
		query.with(Sort.by(Sort.Order.asc(TableConstants.NAME)));
		return mT.find(query, Menus.class);
	}

	public List<UserAndInstitutionLinking> getMappedUserRoleData(UserProfile user) throws CustomException {
		ObjectId userId = new ObjectId(user.get_id());
		List<Document> pipeline = Arrays.asList(
				new Document(ApplicationConstants.LOOKUP,
						new Document(ApplicationConstants.FROM, TableConstants.USER_MANAGMENT_USER_INFO)
								.append(ApplicationConstants.LOCAL_FIELD,
										TableConstants.USER_PROFILE + TableConstants.ID_$)
								.append(ApplicationConstants.FOREIGN_FIELD, TableConstants._ID)
								.append(ApplicationConstants.AS, TableConstants.USER_PROFILE)),
				new Document(ApplicationConstants.MATCH, new Document(TableConstants.USER_PROFILE_USERID_ID, userId)),
				new Document(ApplicationConstants.ADD_FIELDS,
						new Document(TableConstants.USER_PROFILE,
								new Document(ApplicationConstants.ARRAY_ELEMAT,
										Arrays.asList(ApplicationConstants.USER_PROFILE_$, 0)))),

				new Document("$match", new Document("isDeleted", false)));

		List<Document> results = mT.getCollection(TableConstants.USER_MANAGEMENT_INS_USR_INFO).aggregate(pipeline)
				.into(new ArrayList<>());
		List<UserAndInstitutionLinking> userAndInstitutionLinking = new ArrayList<>();
		for (Document userLinking : results) {
			UserAndInstitutionLinking role = convertDocumentToUserAndInstitutionLinking(userLinking);
			userAndInstitutionLinking.add(role);
		}
		return userAndInstitutionLinking;
	}

	@Cacheable(cacheResolver = ApplicationConstants.CACHE_DYNAMIC_BEAN)
	public List<RoleApis> getApiRoleMaps(List<Role> mappedRole) throws CustomException {
		List<ObjectId> roleIds = mappedRole.stream().map(a -> new ObjectId(a.get_id())).collect(Collectors.toList());
		AggregateIterable<Document> result = mT.getCollection(TableConstants.MASTER_API_DETAILS)
				.aggregate(
						Arrays.asList(
								new Document(ApplicationConstants.LOOKUP,
										new Document(ApplicationConstants.FROM,
												TableConstants.MASTER_OPERATION_API_LINK)
												.append(ApplicationConstants.MONGO_LOCALFIELD, TableConstants.MONGO_ID)
												.append(ApplicationConstants.FOREIGN_FIELD, ApplicationConstants.API_ID)
												.append(ApplicationConstants.MONGO_ALAIS, TableConstants.API_LIST)),
								new Document(ApplicationConstants.UNWIND, ApplicationConstants.$API_LIST),
								new Document(ApplicationConstants.MATCH,
										new Document(ApplicationConstants.APILIST_DELETE, false)),
								new Document(ApplicationConstants.LOOKUP,
										new Document(ApplicationConstants.FROM, TableConstants.MASTER_OPERATION_INFO)
												.append(ApplicationConstants.MONGO_LOCALFIELD,
														ApplicationConstants.APILIST_OPERATION_ID)
												.append(ApplicationConstants.FOREIGN_FIELD, TableConstants.MONGO_ID)
												.append(ApplicationConstants.MONGO_ALAIS, TableConstants.OPERATION)),
								new Document(ApplicationConstants.UNWIND, TableConstants.OPERATION_$),
								new Document(ApplicationConstants.MATCH,
										new Document(TableConstants.OPERATION_ISDELETED, false)),

								new Document(ApplicationConstants.LOOKUP,
										new Document(ApplicationConstants.FROM,
												TableConstants.USER_MANAGEMENT_ROLE_PRIVILAGE_LINKING)
												.append(ApplicationConstants.MONGO_LOCALFIELD,
														ApplicationConstants.APILIST_OPERATION_ID)
												.append(ApplicationConstants.FOREIGN_FIELD, TableConstants.OPR_$ID)
												.append(ApplicationConstants.MONGO_ALAIS,
														TableConstants.ROLES_AND_PRIVILEGE_LINKING)),
								new Document(ApplicationConstants.UNWIND, ApplicationConstants.USR_RLE_PRV$),
								new Document(ApplicationConstants.MATCH,
										new Document(ApplicationConstants.USR_PRV_LNK_DELETED, false)),

								new Document(ApplicationConstants.LOOKUP,
										new Document(ApplicationConstants.FROM, TableConstants.USER_MANAGMENT_ROLE_INFO)
												.append(ApplicationConstants.MONGO_LOCALFIELD,
														TableConstants.USR_PRV_LNK_$RLE_$ID)
												.append(ApplicationConstants.FOREIGN_FIELD, TableConstants.MONGO_ID)
												.append(ApplicationConstants.MONGO_ALAIS, ApplicationConstants.ROLE)),
								new Document(ApplicationConstants.UNWIND, ApplicationConstants.ROLE_$),
								new Document(ApplicationConstants.MATCH,
										new Document(TableConstants.ROLEID_MONGO,
												new Document(ApplicationConstants.IN, roleIds))),
								new Document(ApplicationConstants.$PROJECT,
										new Document(TableConstants.MONGO_ID, ApplicationConstants.ZERO)
												.append(ApplicationConstants.API, TableConstants.$APIVALUE)
												.append(ApplicationConstants.ACTIVITY, TableConstants.$ACTIVITY)
												.append(ApplicationConstants.ROLE, TableConstants.$ROLE_IDENTITY)
												.append(ApplicationConstants.PAGE, ApplicationConstants.$PAGE))));

		List<RoleApis> roleApiList = new ArrayList<>();
		for (Document document : result) {
			String apiName = document.getString(ApplicationConstants.API);
			String roleName = document.getString(ApplicationConstants.ROLE);
			Integer activity = document.getInteger(ApplicationConstants.ACTIVITY);
			String page = document.getString(ApplicationConstants.PAGE);

			RoleApis roleApi = new RoleApis();
			roleApi.setApi(apiName);
			roleApi.setRole(roleName);
			roleApi.setActivity(activity);
			roleApi.setPage(page);
			roleApiList.add(roleApi);
		}
		return roleApiList;
	}

	public List<RolesAndPrivilegeLinking> getRolePrivilegeListByMakerRoleId(String id) {
		Criteria criteria = Criteria.where(TableConstants.MAKER_ROLE).is(id).and(TableConstants.IS_DELETED).is(false);
		Query query = new Query(criteria);
		return mT.find(query, RolesAndPrivilegeLinking.class);
	}

	public List<Role> getRoles(List<FilterOrSortingVo> filterVos, Integer skip, Integer limit,
			Boolean functionalityAccess, String subModuleName) throws ApplicationException {
		List<AggregationOperation> aggregationOperationsList = new ArrayList<>();
		Criteria criteria = new Criteria();
		AggregationOperation buildProject = null;
		if (Boolean.TRUE.equals(functionalityAccess)) {
			Document rolePrivilegeLookup = new Document(ApplicationConstants.FROM,
					TableConstants.USER_MANAGEMENT_ROLE_PRIVILAGE_LINKING)
					.append(ApplicationConstants.LOCAL_FIELD, TableConstants._ID)
					.append(ApplicationConstants.FOREIGN_FIELD, TableConstants.ROLE_JOIN_COLLECTION_ID)
					.append(ApplicationConstants.AS, TableConstants.ROLES_AND_PRIVILEGE_LINKING);
			AggregationOperation unwindRolePrivilege = Aggregation.unwind(TableConstants.ROLE_PRIVILEGE_LINKING_$);

			Document operationLookup = new Document(ApplicationConstants.FROM, TableConstants.MASTER_OPERATION_INFO)
					.append(ApplicationConstants.LOCAL_FIELD, TableConstants.ROLESANDPRIVILEGELINKING_OPERATION_$ID)
					.append(ApplicationConstants.FOREIGN_FIELD, TableConstants._ID)
					.append(ApplicationConstants.AS, TableConstants.OPERATION);
			AggregationOperation unwindOperation = Aggregation.unwind(TableConstants.OPERATION_$);

			Document subModuleLookup = new Document(ApplicationConstants.FROM, TableConstants.MASTER_SUB_MODULE)
					.append(ApplicationConstants.LOCAL_FIELD, TableConstants.OPERATION_SUBMODULE_$ID)
					.append(ApplicationConstants.FOREIGN_FIELD, TableConstants._ID)
					.append(ApplicationConstants.AS, TableConstants.SUB_MODULE);
			AggregationOperation unwindSubModule = Aggregation.unwind(TableConstants.SUB_MODULE_$);

			criteria = Criteria.where(TableConstants.ROLES_PRIVILEGE_ISDELETED).is(false)
					.and(TableConstants.OPERATION_ISDELETED).is(false).and(TableConstants.SUBMODULE_ISDELETED).is(false)
					.and(TableConstants.SUBMODULE_NAME).is(subModuleName);

			Document exclude = new Document(TableConstants.ROLES_AND_PRIVILEGE_LINKING, 0)
					.append(TableConstants.OPERATION, 0).append(TableConstants.SUB_MODULE, 0);

			AggregationOperation rlrPrvlgeDocument = customAggregationBuilder.buildLookUp(rolePrivilegeLookup);
			AggregationOperation oprDocument = customAggregationBuilder.buildLookUp(operationLookup);
			AggregationOperation sbMdlDocument = customAggregationBuilder.buildLookUp(subModuleLookup);
			buildProject = customAggregationBuilder.buildProject(exclude);
			aggregationOperationsList.add(rlrPrvlgeDocument);
			aggregationOperationsList.add(unwindRolePrivilege);
			aggregationOperationsList.add(oprDocument);
			aggregationOperationsList.add(unwindOperation);
			aggregationOperationsList.add(sbMdlDocument);
			aggregationOperationsList.add(unwindSubModule);
		}
		criteria.and(TableConstants.ISDELETED).is(false);
		AggregationOperation matchOperation = Aggregation.match(criteria);
		aggregationOperationsList.add(matchOperation);
		buildPredicateForUserName(aggregationOperationsList, TableConstants.CREATED_BY_USER,
				TableConstants.CREATED_BY_USER_NAME);
		if (!ApplicationUtils.isValidateObject(filterVos)) {
			Document sortDocument = Document.parse("{\"modifiedDate\": -1}");
			AggregationOperation aggSortDocument = customAggregationBuilder.buildSort(sortDocument);
			aggregationOperationsList.add(aggSortDocument);
		} else {
			buildPredicateForModifiedUserName(aggregationOperationsList, TableConstants.MODIFIED_BY,
					TableConstants.MODIFIED_BY_USER_NAME);
			List<Document> conditions = new ArrayList<>();
			Document sortDocument = new Document();
			for (FilterOrSortingVo filterVo : filterVos) {
				Document document = null;
				document = getFilterPrdicets(filterVo);
				if (document != null && document.size() > 0) {
					conditions.add(document);
				}
			}
			if (ApplicationUtils.isValidList(conditions)) {
				// Build the $match stage with $and operator
				Document match = new Document(ApplicationConstants.MONGO_AND, conditions);
				// Build the aggregation pipeline
				AggregationOperation matchOp = customAggregationBuilder.buildFilter(match);
				aggregationOperationsList.add(matchOp);
			}

			buildFilterPredicates(filterVos, aggregationOperationsList, TableConstants.TEMPLATE_MODIFIED_DATE);

		}

		if (limit != 0) {
			aggregationOperationsList.add(Aggregation.skip(skip));
			aggregationOperationsList.add(Aggregation.limit(limit));
		}
		if (buildProject != null) {
			aggregationOperationsList.add(buildProject);
		}
		Collation collation = Collation.of(ApplicationConstants.EN).strength(Collation.ComparisonLevel.secondary());
		Aggregation aggregationFilter = Aggregation.newAggregation(aggregationOperationsList)
				.withOptions(Aggregation.newAggregationOptions().collation(collation).build());
		List<Document> role = mT.aggregate(aggregationFilter, TableConstants.ROLE_INFO, Document.class)
				.getMappedResults();
		return role.stream().map(this::convertDocumentToRoleEntity).collect(Collectors.toList());

	}

	protected void buildFilterPredicates(List<FilterOrSortingVo> filterVos,
			List<AggregationOperation> aggregationOperationsList, String defaultSortColumnName)
			throws ApplicationException {
		Document sortDocument = new Document(defaultSortColumnName, -1);
		List<Document> filterPredicates = new ArrayList<Document>();
		if (ApplicationUtils.isValidateObject(filterVos)) {
			buildPredicateForModifiedUserName(aggregationOperationsList, TableConstants.MODIFIED_BY,
					ApplicationConstants.MODIFIEDBY_USERNAME);
			for (FilterOrSortingVo filterVo : filterVos) {
				if (filterVo.getFilterOrSortingType().equals(ApplicationConstants.FILTER)) {
					filterPredicates.add(applyFilter(filterVo));
				} else if (filterVo.getFilterOrSortingType().equals(ApplicationConstants.SORTING)) {
					sortDocument = applySorting(filterVo);
				}
			}
		}
		if (ApplicationUtils.isValidList(filterPredicates)) {
			Document matchQuery = new Document(ApplicationConstants.MONGO_AND, filterPredicates);
			AggregationOperation matchAggregationOperation = customAggregationBuilder.buildFilter(matchQuery);
			aggregationOperationsList.add(matchAggregationOperation);
		}
		if (ApplicationUtils.isValidateObject(sortDocument)) {
			AggregationOperation sortAggregation = customAggregationBuilder.buildSort(sortDocument);
			aggregationOperationsList.add(sortAggregation);
		}
	}

	public List<Role> getRoleList() throws ApplicationException {
		Criteria criteria = Criteria.where(ApplicationConstants.IS_DELETED).is(false);
		Query query = new Query(criteria);
		return mT.find(query, Role.class);
	}

	public List<SubModule> getSubModules() {
		Criteria criteria = new Criteria().and(TableConstants.IS_DELETED).is(false).and(TableConstants.MS_PARENT_ID)
				.is(null);
		Query query = new Query(criteria);
		return mT.find(query, SubModule.class);
	}

	public List<SubModule> getSubModulePage(Integer subModuleId) {
		Criteria criteria = new Criteria();
		criteria.and(TableConstants.IS_DELETED).is(false);
		criteria.and(TableConstants.MS_PARENT_ID).is(subModuleId);
		Query query = new Query(criteria);
		return mT.find(query, SubModule.class);
	}

	public List<Role> getRoleByUser(UserProfile user) {
		List<AggregationOperation> aggregationOperationsList = new ArrayList<>();

		Document lookupStage = new Document(ApplicationConstants.MONGO_FROM, TableConstants.USER_MANAGMENT_USER_INFO)
				.append(ApplicationConstants.MONGO_LOCALFIELD, TableConstants.USER_JOIN_COLLECTION_ID)
				.append(ApplicationConstants.MONGO_FORIEGNFIELD, TableConstants.USER_PROFILE_ID)
				.append(ApplicationConstants.MONGO_ALAIS, TableConstants.USER_PROFILE);

		Document lookupStageRole = new Document(ApplicationConstants.MONGO_FROM,
				TableConstants.USER_MANAGMENT_ROLE_INFO)
				.append(ApplicationConstants.MONGO_LOCALFIELD, TableConstants.ROLE_JOIN_COLLECTION_ID)
				.append(ApplicationConstants.MONGO_FORIEGNFIELD, TableConstants.USER_PROFILE_ID)
				.append(ApplicationConstants.MONGO_ALAIS, TableConstants.ROLE);

		Document addFiledDocument = new Document(TableConstants.USER_PROFILE,
				new Document(TableConstants.ARRAY_ELEMENT, Arrays.asList(TableConstants.USERPROFILE_$, 0)));

		Document addFiledRoleDocument = new Document(TableConstants.ROLE,
				new Document(TableConstants.ARRAY_ELEMENT, Arrays.asList(TableConstants.ROLE_$, 0)));

		Document userDocument = new Document(TableConstants.USER_PROFILE_USERID, user.getUserId());

		Document projectDocument = new Document(TableConstants.MONGO_ID, 0).append(TableConstants.USER_PROFILE, 1)
				.append(TableConstants.IS_DELETED, 1).append(TableConstants.ROLE, TableConstants.ROLE_$);

		Document matchSubModuleStage = new Document(ApplicationConstants.IS_DELETED, false);

		AggregationOperation lookUpDocument = customAggregationBuilder.buildLookUp(lookupStage);
		AggregationOperation addFields = customAggregationBuilder.buildAddFields(addFiledDocument);
		AggregationOperation lookUpRoleDocument = customAggregationBuilder.buildLookUp(lookupStageRole);
		AggregationOperation addFieldsRole = customAggregationBuilder.buildAddFields(addFiledRoleDocument);
		AggregationOperation projectDoc = customAggregationBuilder.buildProject(projectDocument);
		AggregationOperation userAggregationOperation = customAggregationBuilder.buildFilter(userDocument);
		AggregationOperation matchSubModule = customAggregationBuilder.buildFilter(matchSubModuleStage);

		aggregationOperationsList.add(lookUpDocument);
		aggregationOperationsList.add(addFields);
		aggregationOperationsList.add(lookUpRoleDocument);
		aggregationOperationsList.add(addFieldsRole);
		aggregationOperationsList.add(projectDoc);
		aggregationOperationsList.add(userAggregationOperation);
		aggregationOperationsList.add(matchSubModule);

		Aggregation aggregationFilter = Aggregation.newAggregation(aggregationOperationsList);
		List<UserAndInstitutionLinking> roles = mT
				.aggregate(aggregationFilter, TableConstants.USER_MANAGEMENT_INS_USR_INFO, Document.class)
				.getMappedResults().stream().map(this::convertDocumentToEntit).collect(Collectors.toList());

		return roles.stream().map(UserAndInstitutionLinking::getRole).collect(Collectors.toList());

	}

	public UserAndInstitutionLinking convertDocumentToEntit(Document document) {
		return mT.getConverter().read(UserAndInstitutionLinking.class, document);
	}

	public RoleMakerChecker getMakerRoleByIdentity(String identity) {
		Criteria criteria = Criteria.where(TableConstants.IDENTITY).is(identity).and(TableConstants.IS_DELETED)
				.is(false);
		Query query = new Query(criteria);
		return mT.findOne(query, RoleMakerChecker.class);
	}

	public Role getRoleByIdentity(String identity) {
		Criteria criteria = Criteria.where(TableConstants.IDENTITY).is(identity).and(TableConstants.IS_DELETED)
				.is(false);
		Query query = new Query(criteria);
		return mT.findOne(query, Role.class);

	}

	public List<MappedModulesDto> getMappedMenusByRoleIdentities(List<String> roleIdentities) {
		List<AggregationOperation> aggregationOperations = new ArrayList<>();
		aggregationOperations.add(buildLookupOperation(TableConstants.MASTER_MODULE_INFO, TableConstants.MODULE_$ID,
				TableConstants.MONGO_ID, TableConstants.MODULE));
		aggregationOperations.add(buildMatchOperation(ApplicationConstants.MODULE_IS_DELETED, false));
		aggregationOperations.add(buildUnwindOperation(ApplicationConstants.MODULE$));
		aggregationOperations.add(buildLookupOperation(TableConstants.MASTER_SUB_MODULE, TableConstants.SUB_MODULE_$ID,
				TableConstants.MONGO_ID, TableConstants.SUB_MODULE));
		aggregationOperations.add(buildUnwindOperation(ApplicationConstants.SUB_MODULE$));
		aggregationOperations.add(buildMatchOperation(ApplicationConstants.SUB_MODULE_DELETED, false));
		aggregationOperations.add(buildLookupOperation(TableConstants.USER_MANAGEMENT_ROLE_PRIVILAGE_LINKING,
				TableConstants.MONGO_ID, TableConstants.OPR_$ID, TableConstants.ROLES_AND_PRIVILEGE));
		aggregationOperations.add(buildUnwindOperation(ApplicationConstants.USR_RLE_PRV$));
		aggregationOperations.add(buildMatchOperation(ApplicationConstants.USR_PRV_LNK_DELETED, false));
		aggregationOperations.add(buildLookupOperation(TableConstants.ROLE_INFO, TableConstants.USR_PRV_LNK_$RLE_$ID,
				TableConstants.MONGO_ID, TableConstants.ROLE));
		aggregationOperations.add(buildUnwindOperation(ApplicationConstants.ROLE$));
		aggregationOperations.add(buildMatchOperation(ApplicationConstants.ROLE_$ISDELETED, false,
				ApplicationConstants.ROLE_IDENTITY, roleIdentities, ApplicationConstants.IN));

		Document groupDoc = new Document(TableConstants.MONGO_ID, TableConstants.SUBMODULE_$SBMODULEID)
				.append(TableConstants.SUBMODULE_ID,
						new Document(TableConstants.$FIRST, TableConstants.SUBMODULE_$SBMODULEID))
				.append(TableConstants.MS_PARENT_ID,
						new Document(TableConstants.$FIRST, TableConstants.SUBMODULE_$MSPARENT_ID))
				.append(TableConstants.MOD_ID, new Document(TableConstants.$FIRST, TableConstants.MODULE_$MODULE_ID));

		aggregationOperations.add(customAggregationBuilder.buildGroup(groupDoc));

		Document projectDoc = new Document(TableConstants.MONGO_ID, 0).append(TableConstants.SUBMODULE_ID, 1)
				.append(TableConstants.MS_PARENT_ID, 1).append(TableConstants.MOD_ID, 1);

		aggregationOperations.add(customAggregationBuilder.buildProject(projectDoc));

		Aggregation aggregation = Aggregation.newAggregation(aggregationOperations);

		AggregationResults<MappedModulesDto> results = mT.aggregate(aggregation, TableConstants.MASTER_OPERATION_INFO,
				MappedModulesDto.class);

		return results.getMappedResults();
	}

	public List<SystemConfig> getSystemConfigDetail(String systemConfig) throws CustomException {
		Query query = new Query();
		return mT.find(query, SystemConfig.class);
	}

	public List<UserAndInstitutionLinking> getUserRoleById(UserProfile user) throws CustomException {
		ObjectId objectId = new ObjectId(user.get_id());
		List<Document> pipeline = Arrays.asList(
				new Document(ApplicationConstants.LOOKUP,
						new Document(ApplicationConstants.FROM, TableConstants.USER_MANAGMENT_USER_INFO)
								.append(ApplicationConstants.LOCAL_FIELD,
										TableConstants.USER_PROFILE + TableConstants.ID_$)
								.append(ApplicationConstants.FOREIGN_FIELD, TableConstants._ID)
								.append(ApplicationConstants.AS, TableConstants.USER_PROFILE)),
				new Document(ApplicationConstants.MATCH, new Document(TableConstants.USER_PROFILE_USERID_ID, objectId)),
				new Document(ApplicationConstants.MATCH, new Document(TableConstants.ISDELETED, false)),
				new Document(ApplicationConstants.ADD_FIELDS,
						new Document(TableConstants.USER_PROFILE, new Document(ApplicationConstants.ARRAY_ELEMAT,
								Arrays.asList(ApplicationConstants.USER_PROFILE_$, 0)))));

		List<Document> results = mT.getCollection(TableConstants.USER_MANAGEMENT_INS_USR_INFO).aggregate(pipeline)
				.into(new ArrayList<>());
		List<UserAndInstitutionLinking> userAndInstitutionLinking = new ArrayList<>();
		for (Document userLinking : results) {
			UserAndInstitutionLinking role = convertDocumentToUserAndInstitutionLinking(userLinking);
			userAndInstitutionLinking.add(role);
		}
		return userAndInstitutionLinking;
	}

	public UserProfile getUserByIdentity(String identity) {
		Criteria criteria = Criteria.where(TableConstants.IDENTITY).is(identity);
		Query query = new Query(criteria);
		return mT.findOne(query, UserProfile.class);
	}

	public UserProfile getUserByName(String userName) {
		Criteria criteria = Criteria.where(TableConstants.EMAIL_ID).is(userName).and(TableConstants.IS_DELETED)
				.is(false);
		Query query = new Query(criteria);
		return mT.findOne(query, UserProfile.class);
	}

	public UserProfile getUserByEmailId(String email) {
		Criteria criteria = Criteria.where(TableConstants.EMAIL_ID).is(email).and(TableConstants.IS_DELETED).is(false);
		Query query = new Query(criteria);
		return mT.findOne(query, UserProfile.class);
	}

	public ResetKey getResetKeyByKey(String resetKey) throws CustomException {
		Criteria criteria = Criteria.where(TableConstants.RESET_TOKEN).is(resetKey).and(TableConstants.IS_DELETED)
				.is(false);
		Query query = new Query(criteria);
		return mT.findOne(query, ResetKey.class);
	}

	public List<UserPasswordHistory> getlastFivePassword(Integer userId, int validateExistingHistory) {
		List<Document> pipeline = Arrays.asList(
				new Document(ApplicationConstants.MATCH,
						new Document(TableConstants.USER_JOIN_COLLECTION_ID,
								new Document("$exists", true).append("$ne", null))),
				new Document(ApplicationConstants.LOOKUP,
						new Document(ApplicationConstants.MONGO_FROM, TableConstants.USER_MANAGMENT_USER_INFO)
								.append(ApplicationConstants.MONGO_LOCALFIELD, TableConstants.USER_JOIN_COLLECTION_ID)
								.append(ApplicationConstants.MONGO_FORIEGNFIELD, TableConstants._ID)
								.append(ApplicationConstants.MONGO_ALAIS, TableConstants.USER_PROFILE)),
				new Document(ApplicationConstants.MATCH, new Document(TableConstants.USER_PROFILE_USER_ID, userId)),
				new Document(ApplicationConstants.SORT, new Document(TableConstants.UM_PWD_HT_ID, -1)),
				new Document(ApplicationConstants.$_LIMIT, validateExistingHistory),
				new Document(ApplicationConstants.ADD_FIELDS, new Document(TableConstants.USER_PROFILE,
						new Document("$arrayElemAt", Arrays.asList(TableConstants.USERPROFILE_$, 0)))));

		List<Document> results = mT.getCollection(TableConstants.USER_MANAGMENT_USER_PASSWORD_HISTORY)
				.aggregate(pipeline).into(new ArrayList<>());
		List<UserPasswordHistory> entities = results.stream().map(this::convertlastFivepwdDocumentToEntity)
				.collect(Collectors.toList());

		return entities;
	}

	public UserPasswordHistory convertlastFivepwdDocumentToEntity(Document document) {
		return mT.getConverter().read(UserPasswordHistory.class, document);
	}

	public List<UserPasswordHistory> getPasswordWithinDays(Integer userId, Date currentDate, Date tenDaysAgo) {
		List<Document> pipeline = Arrays.asList(
				new Document(ApplicationConstants.LOOKUP,
						new Document(ApplicationConstants.MONGO_FROM, TableConstants.USER_MANAGMENT_USER_INFO)
								.append(ApplicationConstants.MONGO_LOCALFIELD, TableConstants.USER_JOIN_COLLECTION_ID)
								.append(ApplicationConstants.MONGO_FORIEGNFIELD, TableConstants._ID)
								.append(ApplicationConstants.MONGO_ALAIS, TableConstants.USER_PROFILE)),
				new Document(ApplicationConstants.MATCH, new Document(TableConstants.USER_PROFILE_USER_ID, userId)),
				new Document(ApplicationConstants.MATCH,
						new Document(TableConstants.UM_MDY_DTE,
								new Document("$gte", tenDaysAgo).append("$lte", currentDate))),
				new Document(ApplicationConstants.ADD_FIELDS, new Document(TableConstants.USER_PROFILE,
						new Document("$arrayElemAt", Arrays.asList(TableConstants.USERPROFILE_$, 0)))));

		List<Document> results = mT.getCollection(TableConstants.USER_MANAGMENT_USER_PASSWORD_HISTORY)
				.aggregate(pipeline).into(new ArrayList<>());
		List<UserPasswordHistory> entities = results.stream().map(this::convertpwdDocumentToEntity)
				.collect(Collectors.toList());

		return entities;
	}

	public UserPasswordHistory convertpwdDocumentToEntity(Document document) {
		return mT.getConverter().read(UserPasswordHistory.class, document);
	}

	public void updateKey(ResetKey resetKey) throws CustomException {
		Query query = new Query(Criteria.where(TableConstants._ID).is(new ObjectId(resetKey.get_id())));
		Update update = new Update().set(TableConstants.MODIFIED_DATE, resetKey.getModifiedDate())
				.set(TableConstants.RESET_EXPIRED_DATE, resetKey.getExpiryDate());
		mT.updateFirst(query, update, ResetKey.class);
	}

	public PasswordPolicy getPassWordPolicyById(Integer id) throws CustomException {
		List<Document> pipeline = Arrays.asList(
				new Document("$lookup",
						new Document("from", TableConstants.INSTITUTION_INFO).append("localField", "institution.$id")
								.append("foreignField", TableConstants._ID).append("as", "institution")),
				new Document("$unwind", "$institution"),
				new Document("$match", new Document("institution.institutionId", id)),
				new Document("$match", new Document("institution.isActive", 1)));

		List<Document> passwordPolicyDocument = mT.getCollection(TableConstants.PASSWORD_POLICY).aggregate(pipeline)
				.into(new ArrayList<>());
		List<PasswordPolicy> collect = passwordPolicyDocument.stream().map(this::convertDocumentToSubListEntity)
				.collect(Collectors.toList());
		return collect.isEmpty() ? null : collect.get(0);
	}

	public PasswordPolicy convertDocumentToSubListEntity(Document document) {
		return mT.getConverter().read(PasswordPolicy.class, document);
	}
	
	public void savePasswordHistory(UserPasswordHistory history) {
		history.setUmPwdhtId(ApplicationUtils.getNextSequenceForId(TableConstants._ID, ApplicationUtils.getMongoClient(),TableConstants.USER_MANAGMENT_USER_PASSWORD_HISTORY));
		mT.save(history);
		
	}
	
	public void updatePassword(UserPassword userPassword) throws CustomException, JsonProcessingException {
		Query query = new Query(Criteria.where(TableConstants.UM_PWD_ID).is(userPassword.getUmPwdId()));

		Update update = new Update();
		Document doc = new Document();
		this.mT.getConverter().write(userPassword, doc);
		update = update.fromDocument(doc);
		mT.upsert(query, update, TableConstants.USER_MANAGMENT_USER_PASSWORD_LINK_TABLE);
	}
	
	public void savePassword(UserPassword userPassword) {
		userPassword.setUmPwdId(ApplicationUtils.getNextSequenceForId(TableConstants._ID, ApplicationUtils.getMongoClient(),TableConstants.USER_MANAGMENT_USER_PASSWORD_LINK_TABLE));
		mT.save(userPassword);
		
	}
	
	public List<String> getUserMappedInstitutionIds(Integer userId) throws CustomException {
		List<Document> pipeline = Arrays.asList(
                new Document(ApplicationConstants.LOOKUP,
                        new Document(ApplicationConstants.FROM, TableConstants.USER_MANAGMENT_USER_INFO)
                                .append(ApplicationConstants.LOCAL_FIELD,
                                        TableConstants.USER_PROFILE + TableConstants.ID_$)
                                .append(ApplicationConstants.FOREIGN_FIELD, TableConstants._ID)
                                .append(ApplicationConstants.AS, TableConstants.USER_PROFILE)),
                new Document(ApplicationConstants.MATCH, new Document(TableConstants.USER_PROFILE_USER_ID, userId)),
                new Document(ApplicationConstants.MATCH, new Document(TableConstants.ISDELETED, false)),
                new Document(ApplicationConstants.ADD_FIELDS,
                        new Document(TableConstants.USER_PROFILE, new Document(ApplicationConstants.ARRAY_ELEMAT,
                                Arrays.asList(ApplicationConstants.USER_PROFILE_$, 0)))));

        List<Document> results = mT.getCollection(TableConstants.USER_MANAGEMENT_INS_USR_INFO).aggregate(pipeline)
                .into(new ArrayList<>());
        List<UserAndInstitutionLinking> userAndInstitutionLinking = new ArrayList<>();
        for (Document userLinking : results) {
            UserAndInstitutionLinking role = convertDocumentToUserAndInstitutionLinking(userLinking);
            userAndInstitutionLinking.add(role);
        }
        return userAndInstitutionLinking.stream().map(x -> x.getInstitution().getIdentity()).toList();
	}
	
	public UserProfile getUserSecurityKeyByUserName(String userName) {
		Criteria criteria = Criteria.where(TableConstants.USER_NAME).in(userName).and(TableConstants.IS_DELETED)
				.is(Boolean.FALSE);
		Query query = new Query(criteria);
		return mT.findOne(query, UserProfile.class);
	}
	
	public LoginCallCaptchaCheck getLoginCallCaptchaCheck(Date minDate, String ip) {
		Query query = new Query();
		Criteria criteria = new Criteria();
		List<Criteria> conditions = new ArrayList<>();
		Date currentDate = new Date();
		criteria.and("time").lte(currentDate).gte(minDate);
		conditions.add(criteria);
		Criteria criteria1 = new Criteria();
		criteria1.and("ip").is(ip);
		conditions.add(criteria1);
		if (ApplicationUtils.isValidList(conditions)) {
			query.addCriteria(criteria.andOperator(conditions.toArray(new Criteria[(conditions.size())]))).fields()
					.include("Attempt");
		}
		return mT.findOne(query, LoginCallCaptchaCheck.class);
	}

	public UserPassword getPasswordByUserIdendification(String userId) {
		List<Document> pipeline = Arrays.asList(
				new Document("$lookup",
						new Document("from", "user_managment_user_info").append("localField", "userProfile.$id")
								.append("foreignField", "_id").append("as", "userProfile")),
				new Document("$match", new Document("userProfile.userIdentificationNumber", userId)),
				new Document("$addFields",
						new Document("userProfile", new Document("$arrayElemAt", Arrays.asList("$userProfile", 0)))));

		Document results = mT.getCollection("user_managment_user_password_link_table").aggregate(pipeline).first();
		if (results != null) {
			return convertDocumentToEntity(results);
		}
		return null;

	}
	
}
