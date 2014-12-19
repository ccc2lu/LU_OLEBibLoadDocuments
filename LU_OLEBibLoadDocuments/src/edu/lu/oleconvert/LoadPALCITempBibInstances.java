package edu.lu.oleconvert;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
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

public class LoadPALCITempBibInstances {

	public static EntityManagerFactory migration_emf;
	public static EntityManager migration_em;
	public static EntityManagerFactory ole_emf;
	public static EntityManager ole_em;
	public static LU_BuildInstance instbuilder;
	public static Map<Integer, Record> bibsByCatKey = new HashMap<Integer, Record>();

	public static void main(String args[]) {

		String palcibibsfilename = args[0];
		try {
			ReadPALCIBibs(palcibibsfilename);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}

		MarcFactory factory = MarcFactory.newInstance();

		instbuilder = new LU_BuildInstance();
		ole_emf = Persistence.createEntityManagerFactory("ole");
		ole_em = ole_emf.createEntityManager();
		EntityTransaction ole_tx = ole_em.getTransaction();

		migration_emf = Persistence.createEntityManagerFactory("olemigration");
		migration_em = migration_emf.createEntityManager();

		TypedQuery<SirsiItem> query = migration_em.createQuery("select i from SirsiItem i where i.home_location='PALCI' and i.barcode like 'LEH-%'", SirsiItem.class);
		List<SirsiItem> results = query.getResultList();
		Iterator it = results.iterator();
		int total_charges = 0;

		while ( it.hasNext() ) {
			SirsiItem sirsiitem = (SirsiItem) it.next();
			System.out.println("Processing item with barcode: " + sirsiitem.getBarcode());

			javax.persistence.Query get_num_charges_query = migration_em.createNativeQuery("select count(*) from charges, holds where charges.item_id='" + sirsiitem.getBarcode() + 
					                                                                        "' or holds.item_id='" + sirsiitem.getBarcode() + "'");
			total_charges = (Integer) get_num_charges_query.getResultList().get(0);
			if ( total_charges > 0 ) {
				Record record = bibsByCatKey.get(sirsiitem.getCat_key());
				if ( record == null ) {
					System.out.println("No record found for catalog key " + sirsiitem.getCat_key() + ", not creating bib");
				} else {
					// Look for a bib with with this ID
					Long bibId = new Long(sirsiitem.getCat_key());
					Bib b = ole_em.find(Bib.class, bibId);
					if ( b == null ) {
						// If we didn't find an existing one, then make a fake bib, item, and holding for this thing
						/*
					if ( record == null ) {
						record = factory.newRecord();
						record.addVariableField(factory.newControlField("001", Integer.toString(sirsiitem.getCat_key())));
						DataField title = factory.newDataField();
						title.setTag("245");
						Subfield titlefield = factory.newSubfield();
						titlefield.setCode('a');
						titlefield.setData("PALCI loan for item " + sirsiitem.getBarcode());
						title.addSubfield(titlefield);
						record.getDataFields().add(title);
					}
						 */
						b = buildBib(record);
						b.setDateCreated(sirsiitem.getFirst_created_date());
						b.setDateUpdated(sirsiitem.getLast_changed_date());

						// And a fake holdings attached to the bib
						OLEHoldings oh = buildHoldings(b, sirsiitem);

						// and an item attached to the holdings record
						Item oleitem = buildItem(b, oh, sirsiitem);
						System.out.println("Creating new bib for item with catkey " + sirsiitem.getCat_key());
					} else {
						// Look to see if there's already a holdings record attached to this bib with
						// home_location of PALCI.  If not, create one and attach this item to it
						System.out.println("Attaching to existing bib " + b.getId());
						Item oleitem = null;
						for ( OLEHoldings oh : b.getHoldings() ) {
							if ( oh.getFlatLocation().equals("LEHIGH/FM/PALCI_LEHIGH") ) {
								oleitem = buildItem(b, oh, sirsiitem);
							}
						}
						if ( oleitem == null ) {
							// No existing holding to attach the item to, create one
							OLEHoldings oh = buildHoldings(b, sirsiitem);
							oleitem = buildItem(b, oh, sirsiitem);
						}
					}
					ole_tx.begin();
					ole_em.persist(b);
					ole_tx.commit();
					System.out.println("Persisted bib with ID " + b.getId() + ", MARC record: " + record.toString());
				}
			} else {
				System.out.println("No charges found for barcode " + sirsiitem.getBarcode() + ", not creating bib");
			}
		}
		System.out.println("Number of items: " + results.size());
	}

	public static void ReadPALCIBibs(String filename) throws FileNotFoundException, NumberFormatException {
		Reader input = new FileReader(filename);
		InputSource inputsource = new InputSource(input);
		//inputsource.setEncoding("ISO-8859-1");
		inputsource.setEncoding("UTF-8");
		//MarcReader reader = new MarcStreamReader(input, "ISO-8859-1");
		//MarcXmlReader reader = new MarcXmlReader(new FileInputStream(dumpdir + "/" + args[2]), "UTF-8");
		MarcXmlReader reader = new MarcXmlReader(inputsource);

		while(reader.hasNext()) {
			Record rec = reader.next();
			bibsByCatKey.put(Integer.parseInt(rec.getControlNumber()), rec);
		}
	}

	public static OLEHoldings buildHoldings(Bib b, SirsiItem sirsiitem) {
		OLEHoldings oh = new OLEHoldings();
		oh.setBib(b);
		oh.setHoldingsType("print");
		b.getHoldings().add(oh);
		oh.setFormerId(sirsiitem.getCat_key() + "|1");
		CallNumber cn = new CallNumber();
		cn.setPrefix("");
		cn.setNumber(sirsiitem.getBarcode());
		oh.setCallNumber(cn);
		ShelvingOrder order = new ShelvingOrder();
		order.setShelvingOrder(sirsiitem.getBarcode());
		cn.setShelvingOrder(order);
		oh.setCallNumberType("OTHER", "Other schema", ole_em);
		oh.setCreatedDate(sirsiitem.getFirst_created_date());
		FlatLocation loc = new FlatLocation();
		loc.setLocCodeString("LEHIGH/FM/PALCI_LEHIGH");
		loc.setLevel("Institution/Library/Collection");
		oh.setFlatLocation(loc);
		return oh;
	}

	public static Item buildItem(Bib b, OLEHoldings oh, SirsiItem sirsiitem) {
		Item oleitem = new Item();
		CallNumber cn = oh.getCallNumber();
		AccessInformation ai = new AccessInformation();
		ai.setBarcode(sirsiitem.getBarcode());
		oleitem.setAccessInformation(ai);
		oleitem.setItemHoldings(oh);
		oleitem.setCallNumber(cn);
		oleitem.setCallNumberType(oh.getCallNumberType());
		oleitem.setUniqueIdPrefix("wio");
		oleitem.setClaimsReturnedFlag("N"); // Apparently there has to be a value here or the docstore
		oh.getItems().add(oleitem);
		oleitem.setBarcodeARSL(""); // We don't use this, currently

		List<FormerIdentifier> fids = new ArrayList<FormerIdentifier>();
		FormerIdentifier fi = new FormerIdentifier();
		Identifier id = new Identifier();
		id.setIdentifierValue(sirsiitem.getCat_key() + "|" + sirsiitem.getCallnum_key() + "|" + sirsiitem.getItem_key());

		FlatLocation loc;
		String locStr = sirsiitem.getCurr_location();
		// The "CHECKEDOUT" location has been removed
		if ( locStr.trim().equals("CHECKEDOUT") ||
				locStr.trim().equals("PALCI") ) {
			loc = new FlatLocation();
			loc.setLocCodeString("LEHIGH/FM/PALCI_LEHIGH");
			loc.setLevel("Institution/Library/Collection");
		} else if (locStr.trim().equals("HOLDS") ) {
			loc = new FlatLocation();
			loc.setLocCodeString("LEHIGH/HOLDS");
			loc.setLevel("Institution/Library");			
		} else {
			loc = instbuilder.getFlatLocation(locStr);
		}
		oleitem.setLocation(loc);

		fi.setIdentifierType("SIRSI_ITEMKEY");
		fi.setIdentifier(id);
		fi.setItem(oleitem);
		fids.add(fi);
		oleitem.setFormerIdentifiers(fids);
		ItemType type;
		String itemtypestr = sirsiitem.getType();
		oleitem.setItemType(itemtypestr, itemtypestr, ole_em);
		oleitem.setCreatedBy("BulkIngest-User");
		oleitem.setCreatedDate(sirsiitem.getFirst_created_date());
		oleitem.setStaffOnlyFlag(sirsiitem.getShadowed());
		String item_res_status = sirsiitem.getReserve_status();
		String num_charges = Integer.toString(sirsiitem.getNum_charges());
		String item_status_code, item_status_name;
		if ( item_res_status != null && instbuilder.itemReservedStatusCodeMap.get(item_res_status) != null ) {
			item_status_code = instbuilder.itemReservedStatusCodeMap.get(item_res_status);
			item_status_name = instbuilder.itemReservedStatusNameMap.get(item_res_status);
		} else if ( num_charges != null ) {
			if ( num_charges.equals("0") ) {
				item_status_code = "AVAILABLE";
				item_status_name = "Available";
			} else {
				item_status_code = "LOANED";
				item_status_name = "Loaned";
			}
		} else {
			//LU_DBLoadInstances.Log(System.err, "Neither item reserved status or number of charges set: " + itemString + ", not assigning status", 
			LU_DBLoadInstances.Log(System.err, "Neither item reserved status or number of charges set: " + sirsiitem.toString() + ", not assigning status",
					LU_DBLoadInstances.LOG_WARN);
			item_status_code = "UNAVAILABLE";
			item_status_name = "Unavailable";
		}
		oleitem.setItemStatus(item_status_code, item_status_name, ole_em);
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		oleitem.setItemStatusDateUpdated(df.format(Calendar.getInstance().getTime()));
		return oleitem;
	}

	public static Bib buildBib(Record record) {
		String catkey = LU_DBLoadInstances.formatCatKey(record.getControlNumber()); // need to set this to what's in 001 of the bib to link them
		Bib bib = new Bib(catkey);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		OutputFormat of = new OutputFormat("xml", "UTF-8", true);
		of.setOmitXMLDeclaration(true);
		XMLSerializer tmpserializer = new XMLSerializer(out, of);
		//tmpserializer.setOutputFormat(of);
		//tmpserializer.setOutputByteStream(out);
		Result result;
		MarcWriter writer;
		writer = new MarcXmlWriter(out, "UTF-8");
		String marcXML;
		String shadowed, status, dateCataloged, dateModified, titleControlNumber;
		shadowed = "Y";
		status = "Catalogued";
		dateCataloged = dateModified = "";
		try {
			writer.write(record);
			//out.flush();
			out.close();
			writer.close();
			//marcXML = out.toString("ISO-8859-1");
			marcXML = out.toString("UTF-8");
			String xmldecl = "<\\?xml version=\"1.0\" encoding=\"UTF-8\"\\?>";
			marcXML = marcXML.replaceFirst(xmldecl, "");
			bib.setContent(marcXML);
			// We want to keep the IDs for the Bibs the same
			// bib.setId(Long.parseLong(catkey));
			bib.setCreatedBy("BulkIngest-User");
			bib.setStatus(status);	        
			bib.setStaffOnly(shadowed);
			bib.setFastAdd("N");
			bib.setUniqueIdPrefix("wbm");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			bib = null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bib;
	}

}
