package edu.ucla.wise.shared.persistence;

import org.apache.log4j.Logger;
import org.hibernate.CacheMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public class HibernateUtil {

  private static final Logger LOGGER = Logger.getLogger(HibernateUtil.class);

  private final SessionFactory sessionFactory;
  private final ThreadLocal<Session> threadLocalSession = new ThreadLocal<Session>();
  private final ThreadLocal<Transaction> threadLocalTransaction = new ThreadLocal<Transaction>();

  public HibernateUtil(HibernateConfiguration config) {
    this(config.getFactory());
  }

  public HibernateUtil(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  public SessionFactory getSessionFactory() {
    return sessionFactory;
  }

  public Session getCurrentSession() throws HibernateException {
    Session s = threadLocalSession.get();
    if (s == null) {
      LOGGER.trace("opening new session");
      s = sessionFactory.openSession();
      s.setCacheMode(CacheMode.NORMAL);
      threadLocalSession.set(s);
    }
    return s;
  }

  public void closeSession() throws HibernateException {
    Session s = threadLocalSession.get();
    threadLocalSession.remove();
    if (s != null && s.isOpen()) {
      LOGGER.trace("closing session");
      s.close();
    }
  }

  public Transaction getTransaction() {
    return threadLocalTransaction.get();
  }

  public void beginTransaction() {
    Transaction tx = threadLocalTransaction.get();
    if (tx == null) {
      tx = getCurrentSession().beginTransaction();
      threadLocalTransaction.set(tx);
    } else {
      LOGGER.trace("Reusing same transaction");
    }
  }

  public void commitTransaction() {
    Transaction tx = threadLocalTransaction.get();
    try {
      if (tx != null && !tx.wasCommitted() && !tx.wasRolledBack()) {
        LOGGER.trace("committing transaction");
        tx.commit();
      }
      threadLocalTransaction.remove();
    } catch (HibernateException ex) {
      LOGGER.error("Error committing transaction");
      rollbackTransaction();
      throw ex;
    }
  }

  public void rollbackTransaction() {
    Transaction tx = threadLocalTransaction.get();
    threadLocalTransaction.set(null);
    try {
      LOGGER.error("Trying to rollback database transaction");
      if (tx != null && tx.isActive() && !tx.wasCommitted() && !tx.wasRolledBack()) {
        tx.rollback();
        LOGGER.error("Transaction rolledback successfully");
      } else {
        LOGGER.error("Transaction was not rolledback. ");
      }
    } catch (HibernateException ex) {
      LOGGER.error("Could not rollback transaction", ex);
    } finally {
      closeSession();
    }

  }
}