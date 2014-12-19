package edu.lu.olemod;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.xml.transform.Result;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.marc4j.MarcWriter;
import org.marc4j.MarcXmlReader;
import org.marc4j.MarcXmlWriter;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.xml.sax.InputSource;

import edu.lu.oleconvert.LU_BuildInstance;
import edu.lu.oleconvert.LU_DBLoadInstances;
import edu.lu.oleconvert.ole.AccessInformation;
import edu.lu.oleconvert.ole.Bib;
import edu.lu.oleconvert.ole.CallNumber;
import edu.lu.oleconvert.ole.FlatLocation;
import edu.lu.oleconvert.ole.FormerIdentifier;
import edu.lu.oleconvert.ole.Identifier;
import edu.lu.oleconvert.ole.Item;
import edu.lu.oleconvert.ole.ItemType;
import edu.lu.oleconvert.ole.OLEHoldings;
import edu.lu.oleconvert.ole.ShelvingOrder;

import migration.SirsiCallNumber;
import migration.SirsiItem;

public class CheckBibForItems {

	public static Map<Integer, Boolean> shadowedBibs = new HashMap<Integer, Boolean>();
	public static OLEDBUtil db = new OLEDBUtil();

	public static void main(String args[]) {
		
		String bibsdatafile = args[0];
		try {
			ReadShadowedBibs(bibsdatafile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
		
		int total_items = 0;
		String querystr = "select i from SirsiItem i where i.home_location!='PALCI' and i.barcode not like 'LEH-%'";
		String countquerystr = "select count(*) from items i where i.home_location!='PALCI' and i.barcode not like 'LEH-%'";
		javax.persistence.Query get_num_items_query = db.migration_em.createNativeQuery(countquerystr);
		total_items = (Integer) get_num_items_query.getResultList().get(0);
		int curr = 0;
		int start = 1000000;
		int batchsize = 500000;
		int updatesize = 10000;
		List<SirsiItem> item_batch;
		while ( (start + curr ) < total_items &&
				 curr < batchsize ) {
			item_batch = db.getBatchOfMigrationItems(querystr, start+curr, updatesize);			
			Iterator it = item_batch.iterator();
			while (it.hasNext()) {
				SirsiItem sirsiitem = (SirsiItem) it.next();
				//System.out.println("Processing item with barcode: " + sirsiitem.getBarcode());
				// Look for a bib with with this ID
				Long bibId = new Long(sirsiitem.getCat_key());
				Bib b = db.ole_em.find(Bib.class, bibId);
				if ( b == null ) {
					if ( shadowedBibs.get(bibId.intValue()) != null &&
						 shadowedBibs.get(bibId.intValue()) ) {
						//System.out.println(sirsiitem.getCat_key() + " (shadowed)");
					} else {
						System.out.println("Item with no bib, catalog key: " + sirsiitem.getCat_key());						
					}
				} else {

				}
			}
			curr += updatesize;
			System.err.println("Processed " + curr + " items");
			item_batch.clear();
		}
		System.err.println("Done with batch going from " + start + " to " + ((start + batchsize < total_items) ? (start + batchsize) : total_items));
	}

	public static void ReadShadowedBibs(String filename) throws FileNotFoundException, NumberFormatException, IOException {
		BufferedReader input = new BufferedReader(new FileReader(filename));
		while(input.ready()) {
			String line = input.readLine();
			String[] parts = line.split("\\|");
			boolean shadowed = parts[2].equals("1");
			if (shadowed) {
				String keystr = LU_DBLoadInstances.formatCatKey(parts[0]);
				int key = Integer.parseInt(keystr);
				shadowedBibs.put(key, shadowed);
				//System.err.println("Bib " + key + " is shadowed");
			}
		}
		input.close();
	}

}
