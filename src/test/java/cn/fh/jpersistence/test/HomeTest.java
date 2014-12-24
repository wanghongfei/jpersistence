package cn.fh.jpersistence.test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.Test;

import cn.fh.jpersistence.test.domain.Member;
import cn.fh.jpersistence.test.domain.MemberHome;

public class HomeTest {
	@Test
	public void testJPA() {
		System.out.println("testing JPA");

		EntityManagerFactory emf = Persistence.createEntityManagerFactory("test");
		EntityManager em = emf.createEntityManager();
		
		Member m = new Member();
		m.setName("Neo");

		em.getTransaction().begin();
		em.persist(m);
		em.getTransaction().commit();
	}
	
	@Test
	public void testHome() {
		System.out.println("testing Home");
		MemberHome mHome = new MemberHome();
		
		Member m = new Member();
		m.setName("bruce");
		mHome.setInstance(m);
		mHome.setEntityManager(getEntityManager());
		mHome.persist();
	}
	
	private EntityManager getEntityManager() {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("test");
		EntityManager em = emf.createEntityManager();
		
		return em;
	}
}
