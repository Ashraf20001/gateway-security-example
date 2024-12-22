/*
 * @author codeboard
 */
package com.supercharge.gateway.user.dao;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.SessionFactory;
import org.hibernate.query.NativeQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.HibernateTemplate;

import com.cbt.supercharge.constants.core.ApplicationConstants;
import com.cbt.supercharge.exception.core.ApplicationException;
import com.cbt.supercharge.exception.core.codes.ErrorCodes;
import com.cbt.supercharge.transfter.objects.core.dto.FilterOrSortingVo;
import com.cbt.supercharge.transfter.objects.core.dto.FilterSupportDto;
import com.supercharge.gateway.common.base.dao.GatewayDataFilterFactory;
import com.supercharge.gateway.common.base.dao.GatewayDataFilterRegistry;
import com.supercharge.gateway.common.base.dao.GatewayMasterFilterFactory;
import com.supercharge.gateway.common.filter.master.SortingFilterGateway;
import com.supercharge.gateway.enums.GatewayFilterNameEnum;

/**
 * The Class BaseDao.
 */
public abstract class GatewayBaseDao extends HibernateDAOSupport {

	/** The Constant IDENTITY_SET_METHOD. */
	private static final String IDENTITY_SET_METHOD = ApplicationConstants.SET_IDENTITY;

	/** The data filter registry. */
	@Autowired
	private GatewayDataFilterRegistry dataFilterRegistry;

	/** The filter factory. */
	@Autowired
	private GatewayDataFilterFactory filterFactory;

	/** The hibernate template. */
//	@Autowired
	protected HibernateTemplate hibernateTemplate;

	/** The logger. */
	Logger logger = LoggerFactory.getLogger(GatewayBaseDao.class);

	/** The master filter factory. */
	//@Autowired
	private GatewayMasterFilterFactory masterFilterFactory;

	/** The session factory. */
	//@Autowired
	private SessionFactory sessionFactory;

	/** The sorting filter. */
	//@Autowired
	private SortingFilterGateway sortingFilter;

	private void applyFilter(From<?, ?> root, CriteriaBuilder builder, CriteriaQuery<?> criteria,
			List<Predicate> predicates, FilterOrSortingVo filterVo) throws ApplicationException {
		if (filterVo.getCondition() != null && filterVo.getValue() != null) {
			Predicate predicate = masterFilterFactory.getFilterByName(filterVo.getCondition()).getFilterPredicate(root,
					builder, filterVo, criteria);
			if (predicate != null) {
				predicates.add(predicate);
			}
		}
	}

	private void applySorting(From<?, ?> root, CriteriaBuilder builder, CriteriaQuery<?> criteria,
			List<Predicate> predicates, FilterOrSortingVo filterVo) throws ApplicationException {
		if (filterVo.getColumnName() != null) {
			Predicate predicate = sortingFilter.getFilterPredicate(root, builder, filterVo, criteria);
			if (predicate != null) {
				predicates.add(predicate);
			}
		} else {
			criteria.orderBy(builder.desc(root));
		}
	}

	private void buildInnerFilter(List<FilterOrSortingVo> filterVos, String columnName,
			List<FilterOrSortingVo> rootFilter, List<FilterOrSortingVo> innerFilterList) {
		for (FilterOrSortingVo filterOrSortingVo : filterVos) {
			FilterOrSortingVo innerFilter = null;
			if (filterOrSortingVo.getColumnName() == null || filterOrSortingVo.getColumnName().isEmpty()) {
				continue;
			}
			if (filterOrSortingVo.getColumnName().contains(columnName)) {
				String joinColumn = getJoinColumn(filterOrSortingVo.getColumnName());
				innerFilter = filterOrSortingVo;
				innerFilter.setColumnName(joinColumn);
				innerFilterList.add(innerFilter);
			} else {
				rootFilter.add(filterOrSortingVo);
			}
		}
	}

	/**
	 * Call register data filters.
	 */
	@PostConstruct
	public void callRegisterDataFilters() {
		this.registerDataFilters();
	}

	/**
	 * Commit transaction.
	 */
	public void commitTransaction() {
		sessionFactory.getCurrentSession().getTransaction().commit();
	}

	/**
	 * Creates the query.
	 *
	 * @param builder    from child class
	 * @param criteria   from child class
	 * @param root       from child class
	 * @param predicates from child class
	 * @return TypedQuery for the child class if its register in data filter add
	 *         data filters else only return the TypedQuery
	 */
	protected TypedQuery<?> createQuery(CriteriaBuilder builder, CriteriaQuery<?> criteria, Root<?> root,
			List<Predicate> predicates) {

		if (dataFilterRegistry.getDataFilters(this) != null) {

			for (GatewayFilterNameEnum filter : dataFilterRegistry.getDataFilters(this).keySet()) {
				Predicate predicate = filterFactory.getFilterByName(filter).getFilter(root, this);
				if (predicate != null) {
					predicates.add(predicate);
				}
			}
		}
		if (!predicates.isEmpty()) {
			criteria.where(builder.and(predicates.toArray(new Predicate[predicates.size()])));
		}
		return getSession().createQuery(criteria);
	}

	/**
	 * Creates the query.
	 *
	 * @param builder    the builder
	 * @param update     the update
	 * @param predicates the predicates
	 * @return the typed query
	 */
	protected Query createQuery(CriteriaBuilder builder, CriteriaUpdate<?> update, List<Predicate> predicates) {
		update.where(builder.and(predicates.toArray(new Predicate[predicates.size()])));
		return getSession().createQuery(update);
	}

	/**
	 * Execute bulk update.
	 *
	 * @param typedQuery the typed query
	 */
	protected void executeBulkUpdate(TypedQuery<?> typedQuery) {
		typedQuery.executeUpdate();
	}

	/**
	 * Gets the criteria builder.
	 *
	 * @return the criteria builder
	 */
	protected CriteriaBuilder getCriteriaBuilder() {
		return getSession().getCriteriaBuilder();
	}

	/**
	 * Gets the filter prdicets.
	 *
	 * @param filterVos {@link FilterOrSortingVo} filter details which contains
	 *                  column name, column type, column condition and filter value
	 * @param root      {@link Root} for which entity we want search
	 * @param builder   the builder
	 * @param criteria
	 * @return {@link List<Predicate>}
	 * @throws ApplicationException the application exception
	 */
	protected List<Predicate> getFilterPrdicets(List<FilterOrSortingVo> filterVos, From<?, ?> root,
			CriteriaBuilder builder, CriteriaQuery<?> criteria) throws ApplicationException {
		List<Predicate> predicates = new ArrayList<>();
		if (filterVos == null || filterVos.isEmpty()) {
			return new ArrayList<>();
		}
		for (FilterOrSortingVo filterVo : filterVos) {

			if (filterVo.getFilterOrSortingType().equals(ApplicationConstants.FILTER)) {
				applyFilter(root, builder, criteria, predicates, filterVo);
			} else if (filterVo.getFilterOrSortingType().equals(ApplicationConstants.SORTING)) {
				applySorting(root, builder, criteria, predicates, filterVo);
			} else {
				criteria.orderBy(builder.desc(root));
			}
		}
		return predicates;

	}

	/**
	 * Gets the filter vos.
	 *
	 * @param filterVos  the filter vos
	 * @param columnName the column name
	 * @return the filter vos
	 */
	protected FilterSupportDto getfilterVos(List<FilterOrSortingVo> filterVos, String columnName) {
		FilterSupportDto filter = new FilterSupportDto();
		List<FilterOrSortingVo> rootFilter = new ArrayList<>();
		List<FilterOrSortingVo> innerFilterList = new ArrayList<>();
		buildInnerFilter(filterVos, columnName, rootFilter, innerFilterList);
		filter.setInnerFilter(innerFilterList);
		filter.setRootFilterList(rootFilter);

		return filter;

	}

	/**
	 * Gets the join column.
	 *
	 * @param columnName the column name
	 * @return the join column
	 */
	private String getJoinColumn(String columnName) {
		String[] columnList = columnName.split(ApplicationConstants.DOT_REGEX);
		StringBuilder joinColumn = new StringBuilder();
		for (int i = 1; i < columnList.length; i++) {
			if (i == 1) {
				joinColumn.append(columnList[i]);
			} else {
				joinColumn.append("." + columnList[i]);
			}
		}
		return joinColumn.toString();
	}

	public NativeQuery<?> getNativeQuery(String queryString) {
		return getSession().createNativeQuery(queryString);
	}

	/**
	 * Gets the next sequence.
	 *
	 * @param tableName the table name
	 * @return the next sequence
	 */
	public String getNextSequence() {
		return UUID.randomUUID().toString().replace("-", "");
	}

	/**
	 * Gets the query.
	 *
	 * @param queryString the query string
	 * @return the query
	 */
	protected Query getQuery(String queryString) {
		return getSession().createNativeQuery(queryString);
	}

	/**
	 * Gets the result list.
	 *
	 * @param typedQuery query for your entity
	 * @return returns the List<object> for its executes correctly else its throws
	 *         no result exception returns new ArrayList<>()
	 */
	protected List<?> getResultList(Query typedQuery) {
		List<?> list = null;

		try {
			list = typedQuery.getResultList();
		} catch (NoResultException e) {
			logger.info(e.getMessage());
			list = new ArrayList<>();
		}
		return list;

	}

	/**
	 * Gets the session factory.
	 *
	 * @return the session factory
	 */
	@Override
	public SessionFactory getSessionFactory() {

		return sessionFactory;

	}

	/**
	 * Gets the single result.
	 *
	 * @param typedQuery query for your entity
	 * @return returns the object for its executes correctly else its throws no
	 *         result exception returns null
	 */
	protected Object getSingleResult(TypedQuery<?> typedQuery) {
		Object object = null;
		try {
			object = typedQuery.getSingleResult();
		} catch (NoResultException e) {
			logger.info(e.getMessage());
			object = null;
		}
		return object;
	}

	/**
	 * Method invoke exhandler.
	 *
	 * @param e the e
	 * @throws ApplicationException the application exception
	 */
	protected void methodInvokeExhandler(Exception e) throws ApplicationException {
		logger.info("No such method error in given class {} {}", Object.class, e.getMessage());
		throw new ApplicationException(ErrorCodes.INVALID_METHOD_NAME);
	}

	/**
	 * override all DAO classes and register data filter registry to we want filters
	 * and use @Postconstruct in top of the method to call this method to load data
	 * filter registry after bean creation in runtime.
	 */
	public abstract void registerDataFilters();

	/**
	 * Save.
	 *
	 * @param entity    the entity
	 * @param tableName the table name
	 * @return the integer
	 * @throws ApplicationException the application exception
	 */
	public Integer save(Object entity, String tableName) throws ApplicationException {
		setIdentityValue(entity, tableName);
		return (Integer) hibernateTemplate.save(entity);

	}

	/**
	 * 
	 * @param entity
	 * @return id
	 * @throws ApplicationException
	 */
	protected Integer saveWithoutId(Object entity) {
		return (Integer) hibernateTemplate.save(entity);
	}

	/**
	 * Save without identity.
	 *
	 * @param entity the entity
	 * @return the integer
	 * @throws ApplicationException the application exception
	 */
	protected Integer saveWithoutIdentity(Object entity) throws ApplicationException {
		setIdentityValue(entity, null);
		return (Integer) hibernateTemplate.save(entity);

	}

	/**
	 * Save without identity without setter.
	 *
	 * @param entity the entity
	 * @return the integer
	 * @throws ApplicationException the application exception
	 */
	protected Integer saveWithoutIdentityWithoutSetter(Object entity) throws ApplicationException {
		return (Integer) hibernateTemplate.save(entity);

	}

	/**
	 * Sets the hibernate template.
	 *
	 * @param hibernateTemplate the new hibernate template
	 */
	public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}

	/**
	 * Sets the identity value.
	 *
	 * @param entity    the entity
	 * @param tableName the table name
	 * @throws ApplicationException the application exception
	 */
	private void setIdentityValue(Object entity, String tableName) throws ApplicationException {
		try {
			Method name = entity.getClass().getMethod(IDENTITY_SET_METHOD, String.class);
			name.invoke(entity, getNextSequence());
			logger.info(String.format("set Identity for ========> %s", tableName));
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			methodInvokeExhandler(e);
		}
	}

	/**
	 * Sets the session factory.
	 *
	 * @param sessionFactory the new session factory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/**
	 * Update.
	 *
	 * @param entity the entity
	 */
	public void update(Object entity) {
		hibernateTemplate.update(entity);
	}



}
