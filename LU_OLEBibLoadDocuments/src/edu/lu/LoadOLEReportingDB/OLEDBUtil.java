package edu.lu.LoadOLEReportingDB;

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
	
	public static EntityManagerFactory ole_reporting_emf;
	public static EntityManager ole_reporting_em;
	public static EntityTransaction ole_reporting_tx;
	
	public OLEDBUtil() {
		ole_reporting_emf = Persistence.createEntityManagerFactory("ole_reporting");
		ole_reporting_em = ole_reporting_emf.createEntityManager();
		ole_reporting_tx = ole_reporting_em.getTransaction();
	}
	
	public List<Bib> getBatchOfBibs(int offset, int batchsize, String fromDate) {
		ole_reporting_em.clear();
		TypedQuery<Bib> query = ole_reporting_em.createQuery("select b from Bib b where b.dateUpdated>='" + fromDate + "'", Bib.class);
		query.setFirstResult(offset);
		query.setMaxResults(batchsize);
		List<Bib> results = query.getResultList();
		return results;
	}
	
	public List<Bib> getBatchOfBibs(int offset, int batchsize) {
		ole_reporting_em.clear();
		TypedQuery<Bib> query = ole_reporting_em.createQuery("select b from Bib b", Bib.class);
		query.setFirstResult(offset);
		query.setMaxResults(batchsize);
		List<Bib> results = query.getResultList();
		return results;
	}
	
}
