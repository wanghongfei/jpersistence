package cn.fh.jpersistence;

import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Derived class of Home<T> must be in the scope of "prototype"
 * @author whf
 *
 * @param <T>
 */
public abstract class Home<T> extends AbstractComponent<T> {
	/**
	 * The primary key of this entity
	 */
	private Integer id;
	private T instance;

	
	private HomeStatus status;

	

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
	
	protected void wire(Object[] objsToWire) {
		
	}
	
	/**
	 * Persist this entity.
	 */
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public void persist() {
		checkHomeStatus();
		
		if (isApplicationManaged()) {
			getEntityManager().getTransaction().begin();
		}

		wire(null);
		getEntityManager().persist(instance);

		if (isApplicationManaged()) {
			getEntityManager().getTransaction().commit();
		}
	}

	/**
	 * Update this entity.
	 */
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public void update() {
		checkHomeStatus();

		if (isApplicationManaged()) {
			getEntityManager().getTransaction().begin();
		}

		this.instance = getEntityManager().merge(this.instance);

		if (this.txType == TransactionType.APPLICATION) {
			getEntityManager().getTransaction().commit();
		}
	}

	/**
	 * Delete this entity.
	 */
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public void delete() {
		checkHomeStatus();
		
		if (isApplicationManaged()) {
			getEntityManager().getTransaction().begin();
		}

		getEntityManager().remove(this.instance);

		if (isApplicationManaged()) {
			getEntityManager().getTransaction().commit();
		}
	}

	/**
	 * Retrieve entity from database.
	 * <p> If there are many matched instances, only the first one will 
	 * be returned.
	 * 
	 * @return Return null of no entity is found
	 */
	public T getInstance() {
		if (null == this.instance) {
			getEntityType();
			
			// user does not specify customized restrictions.
			// use getEntityManager().find() by default.
			if (null == getRestrictions()) {
				this.instance = getEntityManager().find(getInstanceClass(), id);
			} else {
				// user has specified customized restrictions,
				// we should generate a JPQL statement with it.
				
				// construct WHERE statement
				String whereStatement = generateWhereStatement();
				
				
				String queryString = "SELECT obj FROM " + getInstanceClass().getName() + " " + OBJECT_ALIAS + " WHERE " + whereStatement;
				System.out.println("INFO: Generate Query String: " + queryString);
				List<T> insList = getEntityManager().createQuery(queryString, getInstanceClass())
						.getResultList();
				
				// get the first entity only if any
				this.instance = insList.isEmpty() ? null : insList.get(0);

			}
		}

		return this.instance;
	}

	@Override
	public void clear() {
		super.clear();
		
		this.id = null;
		this.status = null;
		this.instance = null;
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
		// set restrictions only when the restrictions have not been set
		if (null != getRestrictions()) {
			String[] res = customizeRestriction();
			setRestrictions(res);
		}
	}
	
	private void checkHomeStatus() {
		if (this.status == HomeStatus.INITIAL) {
			throw new IllegalStateException("You have not set instance for this home!");
		}
		
		if (this.status == HomeStatus.NEW_ENTITY) {
			if (null == this.instance || null == getInstanceClass()) {
				throw new IllegalStateException("You have not set instance for this home!");
			}
			
			return;
		}
		
		if (this.status == HomeStatus.OLD_ENTITY) {
			if (null == this.id || null == this.instance || null == getInstanceClass()) {
				throw new IllegalStateException("You have not set instance for this home!");
			}
			
			return;
		}
	}
	

	
	/**
	 * For test only!
	 */
	public void setEntityManager(EntityManager em) {
		super.setEntityManager(em);
	}
}
