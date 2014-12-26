package cn.fh.jpersistence;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;


public abstract class AbstractComponent<T> {
	/**
	 * The Class object of this entity
	 */
	private Class<T> instanceClass;
	protected String[] queryRestrictions;
	
	@PersistenceContext
	private EntityManager em;
	
	/**
	 * Indicating the type of JPA.
	 * Application managed of container managed
	 */
	protected TransactionType txType;
	
	
	protected enum TransactionType {
		APPLICATION,
		CONTAINER
	}
	
	protected Class<T> getInstanceClass() {
		return this.instanceClass;
	}
	protected EntityManager getEntityManager() {
		return this.em;
	}
	protected void setEntityManager(EntityManager entityManager) {
		this.em = entityManager;
	}
	
	/**
	 * Derived class should override this method to perform their own 
	 * clearance.
	 * <p> Note: You must call super.clear() first when overriding.
	 */
	protected void clear() {
		this.instanceClass = null;
		this.queryRestrictions = null;
		this.em = null;
		this.txType = null;
	}
	
	/**
	 * Determine running environment of JPA: Application managed or container managed
	 */
	protected final void determineJpaType() {
		// if the injection for EntityManager failed,
		// it indicating that this is an application managed JPA
		if (null == em) {
			this.txType = TransactionType.APPLICATION;
		} else {
			this.txType = TransactionType.CONTAINER;
		}
		
		System.out.println("status : " + this.txType.toString());
	}
	
	/**
	 * Obtain entity class by reflection.
	 */
	@SuppressWarnings("unchecked")
	protected final void getEntityType() {
		if (null == this.instanceClass) {
			Type type = getClass().getGenericSuperclass();

			if (type instanceof ParameterizedType) {
				ParameterizedType parmType = (ParameterizedType) type;
				this.instanceClass = (Class<T>) parmType.getActualTypeArguments()[0];
			} else {
				throw new RuntimeException("Cannot guess entity class by reflection");
			}
		}
	}
}
