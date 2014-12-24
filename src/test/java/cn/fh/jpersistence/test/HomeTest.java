package cn.fh.jpersistence.test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.Test;

import cn.fh.jpersistence.test.domain.Member;

public class HomeTest {
	@Test
	public void testJPA() {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("test");
		EntityManager em = emf.createEntityManager();
		
		Member m = new Member();
		m.setName("Neo");

		em.getTransaction().begin();
		em.persist(m);
		em.getTransaction().commit();
	}
}
