/*
 * @author codeboard
 */
package com.supercharge.gateway.user.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.supercharge.gateway.filters.GatewayUserGroupSupport;

/**
 * The Class HibernateDAOSupport.
 */
public abstract class HibernateDAOSupport extends GatewayUserGroupSupport {

	/**
	 * The Constant logger.
	 */
	private static final Logger logger = LoggerFactory.getLogger(HibernateDAOSupport.class);

	/**
	 * Gets the all.
	 *
	 * @param <T>   the generic type
	 * @param clazz the clazz
	 * @return the all
	 */
	public <T> List<T> getAll(Class<?> clazz) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<?> criteria = builder.createQuery(clazz);
		TypedQuery<?> query = getSession().createQuery(criteria);
		return (List<T>) query.getResultList();
	}

	/**
	 * Gets the by field.
	 *
	 * @param <T>   the generic type
	 * @param clazz the clazz
	 * @param field the field
	 * @param id    the id
	 * @return the by field
	 */
	@SuppressWarnings("unchecked")
	public <T> T getByField(Class<?> clazz, String field, Object id) {
		return (T) getTypedQueryWithField(clazz, field, id).getSingleResult();
	}

	/**
	 * Gets the list of records.
	 *
	 * @param <T>           the generic type
	 * @param criteriaQuery the criteria query
	 * @return the list of records
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> getListOfRecords(CriteriaQuery<?> criteriaQuery) {
		return (List<T>) getSession().createQuery(criteriaQuery).getResultList();
	}

	/**
	 * Gets the list of records by field.
	 *
	 * @param <T>   the generic type
	 * @param clazz the clazz
	 * @param field the field
	 * @param id    the id
	 * @return the list of records by field
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> getListOfRecordsByField(Class<?> clazz, String field, Object id) {
		return (List<T>) getTypedQueryWithField(clazz, field, id).getResultList();
	}

	/**
	 * Gets the session.
	 *
	 * @return the session
	 */
	protected Session getSession() {
		Session currentSession = null;
		try {
			currentSession = getSessionFactory().getCurrentSession();
		} catch (Exception e) {
			logger.error("Error while getting current session!", e);
		}
		return currentSession;
	}

	/**
	 * Gets the session factory.
	 *
	 * @return the session factory
	 */
	public abstract SessionFactory getSessionFactory();

	/**
	 * Gets the typed query with field.
	 *
	 * @param clazz the clazz
	 * @param field the field
	 * @param id    the id
	 * @return the typed query with field
	 */
	private TypedQuery<?> getTypedQueryWithField(Class<?> clazz, String field, Object id) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<?> criteria = builder.createQuery(clazz);
		Root<?> root = criteria.from(clazz);
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(builder.equal(root.get(field), id));
		criteria.where(builder.and(predicates.toArray(new Predicate[predicates.size()])));
		return getSession().createQuery(criteria);
	}

}
