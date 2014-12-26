package cn.fh.jpersistence;

import java.util.ArrayList;
import java.util.List;

public abstract class Query<T> extends AbstractComponent<T> {
	
	private List<T> resultList = new ArrayList<T>();
	
	private int maxResult = 10;
	private int curPage = 0;
	private boolean refresh = false;
	
	public List<T> getResultList() {
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
		this.resultList = this.em.createQuery("SELECT obj FROM " + this.instanceClass.getName() + " obj", this.instanceClass)
			.setFirstResult(this.maxResult * this.curPage)
			.setMaxResults(this.maxResult)
			.getResultList();
		
		this.refresh = false;
	}
	
}
