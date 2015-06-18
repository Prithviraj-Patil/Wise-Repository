package edu.ucla.wise.shared.persistence;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.google.common.base.Strings;

public class GenericDAO<T, ID extends Serializable> {
  public static final int DEFAULT_BATCH_SIZE = 100;

  private final Class<T> persistentClass;

  private final HibernateUtil hibernateUtil;

  @SuppressWarnings("unchecked")
  public GenericDAO(HibernateUtil hibernateUtil) {
    this.hibernateUtil = hibernateUtil;
    this.persistentClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass())
        .getActualTypeArguments()[0];
  }

  public void save(T entity) {
    Session session = hibernateUtil.getCurrentSession();
    session.save(entity);
  }

  public void save(Collection<T> entities) {
    save(entities, DEFAULT_BATCH_SIZE);
  }

  public void save(Collection<T> entities, int batchSize) {
    Session session = hibernateUtil.getCurrentSession();
    int count = 0;
    for (T entity : entities) {
      session.save(entity);

      // persist objects BATCH_SIZE at a time
      if (count % batchSize == 0) {
        session.flush();
        session.clear();
      }

      count++;
    }
  }

  public void update(T entity) {
    Session session = hibernateUtil.getCurrentSession();
    session.update(entity);
  }

  public List<T> findAll() {
    return findByCriteria(Collections.<Criterion> emptyList());
  }

  /**
   * 
   * Use when you need to obtain a reference to the object without issuing extra SQL queries
   * 
   * @param id
   * @return
   */
  public T find(ID id) {
    Session session = hibernateUtil.getCurrentSession();
    @SuppressWarnings("unchecked")
    T entity = (T) session.load(getPersistentClass(), id);
    return entity;
  }

  /**
   * 
   * Use when you want to load an object, this will always get the value from the database
   * 
   * @param id
   * @return T
   */
  public T findNonProxied(ID id) {
    Session session = hibernateUtil.getCurrentSession();
    @SuppressWarnings("unchecked")
    T entity = (T) session.get(getPersistentClass(), id);
    return entity;
  }

  public void remove(T entity) {
    if (entity != null) {
      Session session = hibernateUtil.getCurrentSession();
      session.delete(entity);
    }
  }

  public boolean removeById(ID id) {
    if (id != null) {
      Session session = hibernateUtil.getCurrentSession();
      @SuppressWarnings("unchecked")
      T entity = (T) session.get(getPersistentClass(), id);
      if (entity != null) {
        session.delete(entity);
        return true;
      }
    }
    return false;
  }

  public Class<T> getPersistentClass() {
    return persistentClass;
  }

  protected List<T> findByCriteria(Collection<Criterion> criterion) {
    Criteria criteria = createCriteria();
    for (Criterion c : criterion) {
      criteria.add(c);
    }
    return findByCriteria(criteria);
  }

  protected List<T> findByOrder(Order order) {
    Criteria criteria = createCriteria();
    criteria.addOrder(order);
    return findByCriteria(criteria);
  }

  public List<T> findByExample(T exampleInstance, String... excludeProperty) {
    Criteria criteria = createCriteria();
    Example example = Example.create(exampleInstance);
    for (String exclude : excludeProperty) {
      example.excludeProperty(exclude);
    }
    criteria.add(example);
    return findByCriteria(criteria);
  }

  protected List<T> findByCriteria(Criteria criteria) {
    @SuppressWarnings("unchecked")
    List<T> entityList = criteria.list();
    return entityList;
  }

  public T getByUniqueColumn(String uniqueColumnName, String value) {
    if (Strings.isNullOrEmpty(value) || Strings.isNullOrEmpty(uniqueColumnName)) {
      throw new IllegalArgumentException("unique column value cannot be null");
    }
    Criteria criteria = createCriteria();
    // For the criteria query, use object's variable names, not database names.
    criteria.add(Restrictions.eq(uniqueColumnName, value));
    @SuppressWarnings("unchecked")
    T entity = (T) criteria.uniqueResult();
    return entity;
  }

  public HibernateUtil getHibernateUtil() {
    return hibernateUtil;
  }

  protected Criteria createCriteria() {
    Session session = hibernateUtil.getCurrentSession();
    Criteria criteria = session.createCriteria(getPersistentClass());
    criteria.setCacheable(true);
    return criteria;
  }
}