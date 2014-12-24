package cn.fh.jpersistence;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public abstract class Home<T> {
	/**
	 * The primary key of this entity
	 */
	private Integer id;
	/**
	 * The entity object
	 */
	private T instance;
	/**
	 * The Class object of this entity
	 */
	private Class<T> instanceClass;
	private String[] queryRestrictions;
	
	private HomeStatus status;
	/**
	 * Indicating the type of JPA.
	 * Application managed of container managed
	 */
	private TransactionType txType;
	
	
	@PersistenceContext
	private EntityManager em;
	
	private enum TransactionType {
		APPLICATION,
		CONTAINER
	}
	private enum HomeStatus {
		/**
		 * This home is prepared to persist a new entity
		 */
		NEW_ENTITY,
		/**
		 * This home is prepared to update an existed entity
		 */
		OLD_ENTITY,
		/**
		 * This home is prepared for nothing.
		 */
		INITIAL
	}

	/**
	 * Put an existed entity to this home.
	 * <p> The invocation of this method will clear previous entity data.
	 * @param id
	 */
	public void setInstanceId(Integer id) {
		clear();
		determineJpaType();
		setQueryString();
		this.id = id;
		this.status = HomeStatus.OLD_ENTITY;
		
		getInstance();
	}
	
	/**
	 * Put a new entity to this home.
	 * <p> The invocation of this method will clear previous entity data.
	 * @param instance
	 */
	public void setInstance(T instance) {
		clear();
		determineJpaType();
		setQueryString();
		this.instance = instance;
		this.status = HomeStatus.NEW_ENTITY;

		getEntityType();
	}
	
	/**
	 * Persist this entity.
	 */
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public void persist() {
		checkHomeStatus();
		
		//System.out.println(this.txType);
		//System.out.println(this.status);
		//System.out.println(getClass());
		if (this.txType == TransactionType.APPLICATION) {
			this.em.getTransaction().begin();
			//System.out.println("begin transaction");
		}
		em.persist(instance);
		if (this.txType == TransactionType.APPLICATION) {
			this.em.getTransaction().commit();
		}
	}

	/**
	 * Update this entity.
	 */
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public void update() {
		checkHomeStatus();

		if (this.txType == TransactionType.APPLICATION) {
			this.em.getTransaction().begin();
		}
		this.instance = em.merge(this.instance);
		if (this.txType == TransactionType.APPLICATION) {
			this.em.getTransaction().commit();
		}
	}

	/**
	 * Delete this entity.
	 */
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public void delete() {
		checkHomeStatus();
		
		if (this.txType == TransactionType.APPLICATION) {
			this.em.getTransaction().begin();
		}
		em.remove(this.instance);
		if (this.txType == TransactionType.APPLICATION) {
			this.em.getTransaction().commit();
		}
	}

	/**
	 * Retrieve entity from database.
	 * @return
	 */
	public T getInstance() {
		if (null == this.instance) {
			getEntityType();
			
			// user does not specify customized restrictions.
			// use em.find() by default.
			if (null == this.queryRestrictions) {
				this.instance = em.find(instanceClass, id);
			} else {
				// user has specified customized restrictions,
				// we should generate a JPQL statement with it.
				
				// construct WHERE statement
				StringBuilder sb = new StringBuilder();
				for (int ix = 0 ; ix < this.queryRestrictions.length ; ++ix) {
					// not the last element
					if (ix != this.queryRestrictions.length - 1) {
						sb.append(" AND ");
					}

					sb.append(this.queryRestrictions[ix]);
				}
				
				this.instance = em.createQuery("SELECT obj FROM " + this.instanceClass.getName() + " obj WHERE " + sb.toString(), this.instanceClass)
						.getSingleResult();
			}
		}

		return this.instance;
	}

	public void clear() {
		id = null;
		instance = null;
		instanceClass = null;
		this.queryRestrictions = null;
		
		this.status = HomeStatus.INITIAL;
		this.txType = null;
	}
	
	/**
	 * Give derived class an opportunity to customize query operation.
	 * 
	 * <p> Derived class may override this method to return their own query String.
	 * Home object will use this array of String to generate WHERE statement.
	 * Every element of this array will be connected by 'AND'
	 * 
	 * <p> e.g.: { "obj.name = 'bruce', obj.age = '18', obj.gender = 'male'" } will 
	 * generate "WHERE obj.name = 'bruce' AND obj.age = 18 AND obj.gender = 'male'".
	 */
	protected String[] customizeRestriction() {
		return null;
	}
	private void setQueryString() {
		this.queryRestrictions = customizeRestriction();
	}
	
	private void checkHomeStatus() {
		if (this.status == HomeStatus.INITIAL) {
			throw new IllegalStateException("You have not set instance for this home!");
		}
		
		if (this.status == HomeStatus.NEW_ENTITY) {
			if (null == this.instance || null == this.instanceClass) {
				throw new IllegalStateException("You have not set instance for this home!");
			}
			
			return;
		}
		
		if (this.status == HomeStatus.OLD_ENTITY) {
			if (null == this.id || null == this.instance || null == this.instanceClass) {
				throw new IllegalStateException("You have not set instance for this home!");
			}
			
			return;
		}
	}
	
	/**
	 * Obtain entity class by reflection.
	 */
	@SuppressWarnings("unchecked")
	private void getEntityType() {
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
	
	/**
	 * Determine running environment of JPA: Application managed or container managed
	 */
	protected void determineJpaType() {
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
	 * For test only!
	 */
	public void setEntityManager(EntityManager em) {
		this.em = em;
	}
}
