package cn.fh.jpersistence;

import java.util.ArrayList;
import java.util.List;

public abstract class Query<T> extends AbstractComponent<T> {
	
	private List<T> resultList = new ArrayList<T>();
	
	private int maxResult = 10;
	private int curPage = 0;
	private boolean refresh = false;

	
	
	public List<T> getResultList() {
		setQueryString();

		if (null == this.resultList) {
			determineJpaType();
			getEntityType();

			fetchResultList();
		}
		
		if (true == refresh) {
			fetchResultList();
		}

		return this.resultList;
	}
	
	public void setPage(int page) {
		this.curPage = page - 1;
	}
	
	public void setMaxResult(int max) {
		this.maxResult = max;
	}
	
	/**
	 * Give derived class an opportunity to customize query operation.
	 * 
	 * <p> Derived class may override this method to return their own query String.
	 * Home object will use this array of String to generate WHERE statement.
	 * Every element of this array will be connected by 'AND'
	 * 
	 * <p> e.g.: { "obj.name = 'bruce'", "obj.age = '18'", "obj.gender = 'male'" } will 
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
	
	
	@Override
	public void clear() {
		super.clear();
		
		this.maxResult = 10;
		this.curPage = 0;
		this.refresh = false;
		this.resultList = null;
	}
	
	/**
	 * Set the refresh flag to true. Next invocation of getResultList() will issue 
	 * a query to database.
	 */
	public void refresh() {
		this.refresh = true;
	}
	
	private void fetchResultList() {
		StringBuilder bufQuery = new StringBuilder("SELECT ");
		bufQuery.append(OBJECT_ALIAS);
		bufQuery.append(" FROM ");
		bufQuery.append(getInstanceClass().getName());
		bufQuery.append(" ");
		bufQuery.append(OBJECT_ALIAS);

		//String queryString = "SELECT obj FROM " + getInstanceClass().getName() + " " + OBJECT_ALIAS; 
		String queryString = bufQuery.toString();
		this.resultList = getEntityManager().createQuery(queryString, getInstanceClass())
			.setFirstResult(this.maxResult * this.curPage)
			.setMaxResults(this.maxResult)
			.getResultList();
		
		this.refresh = false;
	}
	
}
