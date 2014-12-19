package edu.lu.olemod;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;

import javax.persistence.TypedQuery;

import edu.lu.oleconvert.ole.AccessInformation;
import edu.lu.oleconvert.ole.Bib;
import edu.lu.oleconvert.ole.Item;
import edu.lu.oleconvert.ole.OLEHoldings;
import edu.lu.oleconvert.ole.URI;

public class CreateItemsForEHoldings {

	public static void main(String args[]) {
		OLEDBUtil db = new OLEDBUtil();
		BigInteger total_bibs = new BigInteger("0");
		javax.persistence.Query get_num_bibs_query = db.ole_em.createNativeQuery("select count(*) from ole_ds_holdings_t where holdings_type='electronic'");
		total_bibs = (BigInteger) get_num_bibs_query.getResultList().get(0);
		System.out.println("Total eholdings: " + total_bibs);
		
		int curr = 0;
		int batchsize = 1000;
		while ( curr < total_bibs.intValue() ) {
			db.ole_em.clear();
			db.ole_tx.begin();
			TypedQuery<OLEHoldings> query = db.ole_em.createQuery("select h from OLEHoldings h where h.holdingsType='electronic'", OLEHoldings.class);
			query.setFirstResult(curr);
			query.setMaxResults(batchsize);
			List<OLEHoldings> eholdings_batch = query.getResultList();
			Iterator it = eholdings_batch.iterator();
			while ( it.hasNext() ) {
				OLEHoldings oh = (OLEHoldings)it.next();
				Item item = new Item();
				item.setItemHoldings(oh);
				item.setLocation(oh.getFlatLocation());
				item.setCallNumber(oh.getCallNumber());
				item.setCallNumberType(oh.getCallNumberType());
				item.setUniqueIdPrefix("wio");
				item.setClaimsReturnedFlag("N"); // Apparently there has to be a value here or the docstore
				item.setStaffOnlyFlag("N");
				AccessInformation ai = new AccessInformation();
				ai.setUri(new URI(oh.getAccessURIs().get(0).getUri()));
				ai.setBarcode("ONLINE");
				item.setAccessInformation(ai);
				item.setItemType("ONLINE", "ONLINE", db.ole_em);
				item.setItemStatus("AVAILABLE", "Available", db.ole_em);
				oh.getItems().add(item);
				db.ole_em.persist(oh);
				System.out.println("On eholdings " + oh.getHoldingsIdentifier() + " for bib " + oh.getBib().getId());
			}
			db.ole_tx.commit();
			curr += batchsize;
			System.out.println("Processed eholdings " + curr);
		}
	}
}
