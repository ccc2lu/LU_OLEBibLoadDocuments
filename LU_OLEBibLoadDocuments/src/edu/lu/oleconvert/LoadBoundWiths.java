package edu.lu.oleconvert;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import migration.SirsiCallNumber;
import edu.lu.oleconvert.ole.Bib;
import edu.lu.oleconvert.ole.BoundWith;
import edu.lu.oleconvert.ole.OLEHoldings;

public class LoadBoundWiths {

	public static void main(String args[]) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		EntityManagerFactory ole_emf = Persistence.createEntityManagerFactory("ole");
		EntityManager ole_em = ole_emf.createEntityManager();
		EntityTransaction ole_tx = ole_em.getTransaction();

		EntityManagerFactory migration_emf = Persistence.createEntityManagerFactory("olemigration");
		EntityManager migration_em = migration_emf.createEntityManager();

		TypedQuery<SirsiCallNumber> query = migration_em.createQuery("select scn from SirsiCallNumber scn where scn.level='CHILD' or scn.level='PARENT'", SirsiCallNumber.class);
		query.setHint("org.hibernate.cacheable", true);
		List<SirsiCallNumber> results = query.getResultList();
		Iterator it = results.iterator();
		while ( it.hasNext() ) {
			SirsiCallNumber scn = (SirsiCallNumber) it.next();
			// Check if this is a child record, and if so, create a bound-with
			try {
				if ( scn.getLevel().equals("CHILD") ) {
					// bib id will be the cat key for the parent, holdings id we'll have to retrieve
					// though using 
					TypedQuery<OLEHoldings> holdings_query = ole_em.createQuery("select oh from OLEHoldings oh where oh.formerId='" + scn.getParent_cat_key() + "|" + scn.getParent_callnum_key() + "'", OLEHoldings.class);
					List<OLEHoldings> holdings_results = holdings_query.getResultList();
					if ( holdings_results.size() > 0 ) {
						TypedQuery<Bib> bib_query = ole_em.createQuery("select b from Bib b where b.id=" + scn.getCat_key(), Bib.class);
						List<Bib> bib_results = bib_query.getResultList();
						if ( bib_results.size() > 0 ) {
							ole_tx.begin();
							Bib b = bib_results.get(0);
							OLEHoldings oh = holdings_results.get(0);
							BoundWith bw = new BoundWith();
							bw.setBibId(b.getId());
							bw.setHoldingsId(oh.getHoldingsIdentifier());
							ole_em.persist(bw);
							ole_tx.commit();
						} else {
							LU_DBLoadInstances.Log(System.err, "No parent bib record found for ID " + scn.getParent_cat_key(),
									LU_DBLoadInstances.LOG_WARN);

						}
					} else {
						LU_DBLoadInstances.Log(System.err, "No parent holdings record found for former ID " + scn.getParent_cat_key() + "|" + scn.getParent_callnum_key(),
								LU_DBLoadInstances.LOG_WARN);
					}
				} else if ( scn.getLevel().equals("PARENT") ) {
					// bib id will be the cat key for the parent, holdings id we'll have to retrieve
					// though using 
					TypedQuery<OLEHoldings> holdings_query = ole_em.createQuery("select oh from OLEHoldings oh where oh.formerId='" + scn.getCat_key() + "|" + scn.getCallnum_key() + "'", OLEHoldings.class);
					List<OLEHoldings> holdings_results = holdings_query.getResultList();
					if ( holdings_results.size() > 0 ) {
						TypedQuery<Bib> bib_query = ole_em.createQuery("select b from Bib b where b.id=" + scn.getCat_key(), Bib.class);
						List<Bib> bib_results = bib_query.getResultList();
						if ( bib_results.size() > 0 ) {
							ole_tx.begin();
							Bib b = bib_results.get(0);
							OLEHoldings oh = holdings_results.get(0);
							BoundWith bw = new BoundWith();
							bw.setBibId(b.getId());
							bw.setHoldingsId(oh.getHoldingsIdentifier());
							ole_em.persist(bw);
							ole_tx.commit();
						} else {
							LU_DBLoadInstances.Log(System.err, "No peer bib record found for ID " + scn.getCat_key(),
									LU_DBLoadInstances.LOG_WARN);								
						}

					} else {
						LU_DBLoadInstances.Log(System.err, "No peer-holdings record found for former ID " + scn.getCat_key() + "|" + scn.getCallnum_key(),
								LU_DBLoadInstances.LOG_WARN);
					}

				}
			} catch (Exception e) {
				LU_DBLoadInstances.Log(System.err, 
						"Unable to create bound-with for sirsi callnumber: " + scn.toString(), 
						LU_DBLoadInstances.LOG_WARN);
				LU_DBLoadInstances.Log(System.err, 
						"Exception: " + e.toString(), 
						LU_DBLoadInstances.LOG_WARN);
				e.printStackTrace(System.err);
			}
		}


		LU_DBLoadInstances.Log(System.out, 
				"Bound-withs loaded, time is: " + df.format(Calendar.getInstance().getTime()), 
				LU_DBLoadInstances.LOG_INFO);

		System.exit(0);

	}
}
