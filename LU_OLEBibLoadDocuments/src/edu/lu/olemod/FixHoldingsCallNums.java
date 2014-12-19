package edu.lu.olemod;

import java.util.Iterator;
import java.util.List;
import java.math.BigInteger;
import javax.persistence.TypedQuery;

import org.hibernate.Query;

import edu.lu.oleconvert.LU_BuildInstance;
import edu.lu.oleconvert.ole.Bib;
import edu.lu.oleconvert.ole.CallNumber;
import edu.lu.oleconvert.ole.Item;
import edu.lu.oleconvert.ole.OLEHoldings;
import edu.lu.oleconvert.ole.ShelvingOrder;

import migration.SirsiItem;

public class FixHoldingsCallNums {
	
	public static void main(String args[]) {
		OLEDBUtil db = new OLEDBUtil();
		BigInteger total_bibs = new BigInteger("0");
		javax.persistence.Query get_num_bibs_query = db.ole_em.createNativeQuery("select count(*) from ole_ds_bib_t");
		total_bibs = (BigInteger) get_num_bibs_query.getResultList().get(0);
		System.out.println("Total bibs: " + total_bibs);
		
		int curr = 0;
		int batchsize = 1000;
		while ( curr < total_bibs.intValue() ) {
			List<Bib> bib_batch = db.getBatchOfBibs(curr, batchsize);
			Iterator it = bib_batch.iterator();
			while ( it.hasNext() ) {
				Bib b = (Bib)it.next();
				for ( OLEHoldings oh : b.getHoldings() ) {
					if ( oh.getCallNumber() != null && oh.getCallNumberType().getCode().equals("OTHER")) {
						String orderstr = LU_BuildInstance.normalizeCallNumber(oh.getCallNumber().getNumber(), oh.getCallNumberType().getCode());
						ShelvingOrder order = new ShelvingOrder();
						order.setShelvingOrder(orderstr);
						oh.getCallNumber().setShelvingOrder(order);
						db.ole_em.persist(oh);
					}
					List<Item> items = oh.getItems();
					for ( Item item : items ) {
						if ( item.getCallNumber() != null && item.getCallNumberType().getCode().equals("OTHER" )) {
							String orderstr = LU_BuildInstance.normalizeCallNumber(item.getCallNumber().getNumber(), item.getCallNumberType().getCode());
							ShelvingOrder order = new ShelvingOrder();
							order.setShelvingOrder(orderstr);
							item.getCallNumber().setShelvingOrder(order);
							db.ole_em.persist(item);
						}
					}
					/*
					if ( items.size() == 1 ) {
						Item item = items.get(0);
						if ( (oh.getCallNumber() != null && item.getCallNumber() != null) &&
							 !oh.getCallNumber().getNumber().equals(item.getCallNumber().getNumber()) ) {
							System.out.println("Bib ID: " + b.getId() + ", holding callnum: " + oh.getCallNumber().getNumber() + 
									", item callnum: " + item.getCallNumber().getNumber()); 
						}
					}
					*/
					
				}
			}
			curr += batchsize;
			System.out.println("On bib " + curr);
		}
	}
}
