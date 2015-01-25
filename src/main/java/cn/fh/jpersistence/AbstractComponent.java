package cn.fh.jpersistence;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * 
 * @author whf
 *
 * @param <T>
 */
public abstract class AbstractComponent<T> {
	/**
	 * The Class object of this entity
	 */
	private Class<T> instanceClass;
	private String[] queryRestrictions;
	
	/**
	 * The alias used in query string.
	 */
	protected final String OBJECT_ALIAS = "obj";
	
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
	
	protected final boolean isApplicationManaged() {
		return txType == TransactionType.APPLICATION;
	}
	protected final boolean isContainerManaged() {
		return txType == TransactionType.CONTAINER;
	}
	
	protected final String[] getRestrictions() {
		return this.queryRestrictions;
	}
	protected final void setRestrictions(String[] restrictions) {
		this.queryRestrictions = restrictions;
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
	 * Generate WHERE statement for query string.
	 * <p> Restrictions will be connected by 'AND' operator.
	 * 
	 * @return
	 */
	protected String generateWhereStatement() {
		// construct WHERE statement
		StringBuilder sb = new StringBuilder();
		String[] restrictions = getRestrictions();
		int len = restrictions.length;
		for (int ix = 0 ; ix < len ; ++ix) {
			// not the last element
			if (ix != len - 1) {
				sb.append(" AND ");
			}

			sb.append(restrictions[ix]);
		}
		
		return sb.toString();
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
