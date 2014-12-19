package edu.lu.olemod;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import migration.SirsiItem;

import edu.lu.oleconvert.LU_BuildInstance;
import edu.lu.oleconvert.ole.Bib;

public class OLEDBUtil {
	
	public static EntityManagerFactory migration_emf;
	public static EntityManager migration_em;
	public static EntityManagerFactory ole_emf;
	public static EntityManager ole_em;
	public static EntityTransaction ole_tx;
	
	public OLEDBUtil() {
		ole_emf = Persistence.createEntityManagerFactory("ole");
		ole_em = ole_emf.createEntityManager();
		ole_tx = ole_em.getTransaction();

		migration_emf = Persistence.createEntityManagerFactory("olemigration");
		migration_em = migration_emf.createEntityManager();

	}
	
	public List<Bib> getBatchOfBibs(int offset, int batchsize) {
		ole_em.clear();
		TypedQuery<Bib> query = ole_em.createQuery("select b from Bib b", Bib.class);
		query.setFirstResult(offset);
		query.setMaxResults(batchsize);
		List<Bib> results = query.getResultList();
		return results;
	}
	
	public List<SirsiItem> getBatchOfMigrationItems(String querystr, int offset, int batchsize) {
		migration_em.clear();
		TypedQuery<SirsiItem> query = migration_em.createQuery(querystr, SirsiItem.class);
		query.setFirstResult(offset);
		query.setMaxResults(batchsize);
		List<SirsiItem> results = query.getResultList();
		return results;
	}
}
