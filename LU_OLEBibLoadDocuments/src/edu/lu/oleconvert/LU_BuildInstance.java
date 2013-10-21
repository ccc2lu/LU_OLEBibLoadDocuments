/* Go through the items and callnums files and use them to generate an ole ingestDocument of type "instance".
 * It should have ole:instance records for each holding, containing a holding record and 1 or more item records
 * See /home/ccc2/dev/OLE Conversion/bulk-ingest-instance.xml for a sample
 */

package edu.lu.oleconvert;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.xml.*;
import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;

import org.marc4j.marc.Record;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

//import edu.indiana.libraries.JPADriver.pojos.*;
import edu.lu.oleconvert.ole.*;

public class LU_BuildInstance {

	private HashMap<String, ArrayList<ArrayList<String>>> callNumberKeysToCallNumbers;
	private HashMap<String, ArrayList<ArrayList<String>>> itemKeysToItems;

	// These are indexed by what we have to link to them from the Bibliographic data
	// For the callNumbers, that's the "Item Number", which is in the 999 field's "a" subfield code
	// Librarians know is as the "call number", but Sirsi calls it the Item Number.  It is output by
	// the "D" option to selcallnum, and is in a separate file keyed by the callnumber key.
	// Items are linked by the "Item ID", which is 999 subfield code "i" in the MARC record.
	// It's output by the "B" option to selitem and is field 31 in the output of selitem
	private HashMap<String, ArrayList<ArrayList<String>>> callNumbers;
	private HashMap<String, ArrayList<ArrayList<String>>> items;
	
	public LU_BuildInstance() {
		super();
		callNumberKeysToCallNumbers = new HashMap<String, ArrayList<ArrayList<String>>>();
		itemKeysToItems = new HashMap<String, ArrayList<ArrayList<String>>>();
		callNumbers = new HashMap<String, ArrayList<ArrayList<String>>>();
		items = new HashMap<String, ArrayList<ArrayList<String>>>();
	}

	public LU_BuildInstance(String callNumbersFilename, String shelvingKeysFilename,
							String itemNumbersFilename, String analyticsFilename,
							String itemsFilename) {
		this();
		this.readSirsiFiles(callNumbersFilename, shelvingKeysFilename,
							itemNumbersFilename, analyticsFilename,
							itemsFilename);		
	}
	
	// The callnumbers data is broken up into 4 files because 3 of the fields contained
	// the pipe character in them and Sirsi had no way to intelligently change the
	// delimiter character between fields from pipe to something else -- it's selascii
	// program claims to change the delimiter, but it can't tell the difference between
	// pipes inside a field and one between fields, either.
	// So, those fields containing the pipe character had to be exported into separate
	// files by themselves.
	public void readSirsiFiles(String callNumbersFilename, String shelvingKeysFilename,
							   String itemNumbersFilename, String analyticsFilename,
							   String itemsFilename) {
		BufferedReader callNumbersReader, shelvingKeysReader, itemNumbersReader, analyticsReader, itemsReader;
		try {
        	callNumbersReader = new BufferedReader(new FileReader(callNumbersFilename));
        	shelvingKeysReader = new BufferedReader(new FileReader(shelvingKeysFilename));
        	itemNumbersReader = new BufferedReader(new FileReader(itemNumbersFilename));
        	analyticsReader = new BufferedReader(new FileReader(analyticsFilename));
        	itemsReader = new BufferedReader(new FileReader(itemsFilename));
        	System.out.println("Building hashmap of call number records by catalog key ...");
        	while(callNumbersReader.ready()) {
        		// There should be the same number of lines in all 4 files containing callnum data 
        		String line = callNumbersReader.readLine();
        		String shelvingKeysLine = shelvingKeysReader.readLine();
        		String itemNumbersLine = itemNumbersReader.readLine();
        		String analyticsLine = itemNumbersReader.readLine();
        		String fields[] = line.split("\\|");
        		ArrayList<String> callNumberFields = (ArrayList<String>) Arrays.asList(fields);
        		// TODO: now add the shelving key, item number, and analytics data to callNumbersFields
        		ArrayList<ArrayList<String>> callNumberStrs ;
        		if ( callNumbers.get(fields[2]) == null ) {
        			callNumberStrs = new ArrayList<ArrayList<String>>();
        			callNumberStrs.add(callNumberFields);
        			callNumbers.put(fields[2], callNumberStrs);
        		} else {
        			callNumbers.get(fields[2]).add(callNumberFields);
        		}
        		//TODO: now fill in the hashmap keyed by itemnumber ...
        	}
        	System.out.println("Building hashmap of call item records by catalog key ...");
        	while(itemsReader.ready()) {
        		String line = itemsReader.readLine();
        		String fields[] = line.split("\\|");
        		if ( items.get(fields[3]) == null ) {
        			ArrayList<String> itemStrs = new ArrayList<String>();
        			itemStrs.add(line);
        			items.put(fields[3], itemStrs);
        		} else {
        			items.get(fields[3]).add(line);
        		}
        	}
		} catch(Exception e) {
			System.err.println("Unable to read in call numbers and items: " + e.getMessage());
			e.printStackTrace(System.err);
		}
	}
	
	public static void main(String arguments[]) {
		
		// First read the files from Sirsi into my own classes, which mirror OLE structure, using my own logic
		
		InstanceCollection ic = new InstanceCollection();
		/* Test to make sure that the XML output looks good */
		testoutput(ic);
		//ReadInstance(ic, "/mnt/bigdrive/bibdata/allcallnums.txt", "/mnt/bigdrive/bibdata/allitems.txt");
		
		// Then marshal those classes to XML and output them
		try {
			JAXBContext context = JAXBContext.newInstance(InstanceCollection.class);
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.marshal(ic, System.out);
		} catch(Exception e) {
			System.err.println("Unable to marshall instance collection: " + e.getMessage());
			e.printStackTrace(System.err);
		}
	}
	
	public void buildInstance(Record record, InstanceCollection ic) {
		Instance inst = new Instance();

	    String catalogKey = record.getVariableField("001").toString().split(" ")[1];
		ArrayList<String> callNumberStrings = this.callNumbers.get(catalogKey);
		ArrayList<String> itemStrings = this.items.get(catalogKey);
		this.buildHoldingsDataFromCallNumber(record, inst, callNumberStrings);
		
		ic.getInstances().add(inst);
		//ic.setInstance(inst);
	}
	
	public void buildItemsData(Record record, Instance inst, ArrayList<String> itemStrings) {
		Items items = new Items();
		ArrayList<Item> itemsList = new ArrayList<Item>();
		for ( String itemString : itemStrings ) {
			// construct the item data from the item string
			Item item = new Item();
			// The contents of the items file was produced by this command:
			// selitem -oKabcdfhjlmnpqrstuvwyzA1234567Bk > /ExtDisk/allitems.txt
			// K actually includes the item key, the callnumber key, and the catalog key all in one
			// So the order of fields is:
			/* 0 (K) item key
			 * 1 (K) callnumber key
			 * 2 (K) catalog key
			 * 3 (a) last used date
			 * 4 (b) number of bills
			 * 5 (c) number of charges
			 * 6 (d) number of "total" charges, not sure what the difference is from (c)
			 * 7 (f) first created date
			 * 8 (h) number of holds
			 * 9 (j) house charge
			 * 10 (l) home location
			 * 11 (m) current location
			 * 12 (n) last changed date
			 * 13 (p) permanent flag (Y or N)
			 * 14 (q) price
			 * 15 (r) reserve type
			 * 16 (s) last user key
			 * 17 (t) type
			 * 18 (u) recirculation flags
			 * 19 (v) inventoried date
			 * 20 (w) number of times inventoried
			 * 21 (y) library of item
			 * 22 (z) hold key
			 * 23 (A) last discharge date
			 * 24 (1) "accountability"
			 * 25 (2) shadowed (Y or N)
			 * 26 (3) distribution key
			 * 27 (4) transit status
			 * 28 (5) reserve status -- NOT_ON_RES, FLAGGED, KEEP_DESK, PICKUP, or ON_RESERVE
			 * 29 (6) "pieces"
			 * 30 (7) "Media Desk" field
			 * 31 (B) item ID (NQ)
			 * 32 (k) number of comments
			 * TODO: we'd like to get the actual comments, but selitem on dewey
			 * claimed the -Z option was invalid.  Ask Mark or someone about this.
			 * The API manual says to use -Z to get the comments.
			 */
			
			Location location = new Location();
			LocationLevel locLevel1 = new LocationLevel();
			locLevel1.setLevel("UNIVERSITY");
			locLevel1.setName("Lehigh University");
			LocationLevel locLevel2 = new LocationLevel();
			locLevel2.setLevel("LIBRARY");
			
			locLevel2.setName(libraryName);
			locLevel2.setName(name)
			itemsList.add(item);
		}
		items.setItems(itemsList);
		inst.setItems(items);
	}
	
	public void buildHoldingsDataFromCallNumber(Record record, Instance inst, ArrayList<String> callNumberStrings) {
		// Use the first callNumber in the list to fill in some info 
		String fields[] = callNumberStrings.get(0).split("\\|");
		// The contents of the call numbers file was produced by this command:
		// selcallnum -iS -oKabchpqryz2 > /ExtDisk/allcallnums.txt
		// So the order of fields is:
		/* 0 callnum key, 
		 * 1 catalog key, 
		 * 2 analytic position
		 * 3 level (NONE, CHILD, or PARENT)
		 * 4 number of copies
		 * 5 number of "call" holds
		 * 6 classification
		 * 7 number of reserve control records
		 * 8 number of academic reserves
		 * 9 library
		 * 10 number of visible copies
		 * 11 shadowed
		 */
		String catalogKey = fields[2];
		inst.setInstanceIdentifier(fields[0]);
		inst.setResourceIdentifier(fields[2]);
		SourceHoldings sh = new SourceHoldings();
		sh.setPrimary(fields[16].equals("1") ? "true" : "false");
		inst.setSourceHoldings(sh);
		
		// TODO: many things now stored in the instance record may need to be
		// drawn from the catalog record.  Look in WorkFlows to see ...
		
		// Build up oleHoldings within instance
		OLEHoldings oh = new OLEHoldings();
		ExtentOfOwnership extentOfOwnership = new ExtentOfOwnership();
		extentOfOwnership.setType("public");
		// TODO: probably need to split this
		extentOfOwnership.setTextualHoldings(record.getVariableField("866").toString());
		oh.setExtentOfOwnership(extentOfOwnership);
		
		inst.setOleHoldings(oh);
	}
	
	public static void testoutput(InstanceCollection ic) {
		/* Test to make sure that the XML output looks good */
		
		// Build up instance
		Instance inst = new Instance();
		inst.setInstanceIdentifier("123");
		inst.setResourceIdentifier("3");
		SourceHoldings sh = new SourceHoldings();
		sh.setPrimary("false");
		inst.setSourceHoldings(sh);
		
		// Build up oleHoldings within instance
		OLEHoldings oh = new OLEHoldings();
		oh.setHoldingsIdentifier("string");
		oh.setReceiptStatus("4");
		oh.setPrimary("true");
		URI uri = new URI();
		uri.setResolvable("string");
		oh.setUri(uri);
		Note n = new Note();
		n.setType("public");
		oh.getNotes().add(n);
		n = new Note();
		n.setType("nonpublic");
		n.setNote("WNT:1 copy of vol.1, no.1 and cont.");
		oh.getNotes().add(n);
		Location loc1 = new Location();
		LocationLevel locLevel1 = new LocationLevel();
		locLevel1.setLevel("Institution");
		locLevel1.setName("Lehigh University");
		LocationLevel locLevel2 = new LocationLevel();
		locLevel2.setLevel("Library");
		locLevel2.setName("Linderman");
		LocationLevel locLevel3 = new LocationLevel();
		locLevel3.setLevel("Shelving");
		locLevel3.setName("1st floor stacks");
		locLevel2.setSubLocationLevel(locLevel3);
		locLevel1.setSubLocationLevel(locLevel2);
		loc1.setLocLevel(locLevel1);
		CallNumber cn = new CallNumber();
		cn.setType("LCC");
		cn.setPrefix("");
		cn.setNumber("BF199.A1J7");
		ShelvingScheme ss = new ShelvingScheme();
		ss.setCodeValue("LCC");
		ss.setFullValue("");
		TypeOrSource tos = new TypeOrSource();
		tos.setPointer("");
		tos.setText("");
		ss.setTypeOrSource(tos);
		ShelvingOrder so = new ShelvingOrder();
		so.setCodeValue("");
		so.setFullValue("");
		so.setTypeOrSource(tos);
		cn.setShelvingSchema(ss);
		cn.setShelvingOrder(so);
		oh.setCallNumber(cn);
		oh.setLocation(loc1);
		ExtentOfOwnership eoo = new ExtentOfOwnership();
		eoo.setTextualHoldings("v. 45 no. 1 (2012: Spring)");
		eoo.setType("Basic Bibliographic Unit");
		n = new Note();
		n.setType("public");
		eoo.getNotes().add(n);
		n = new Note();
		n.setType("nonpublic");
		eoo.getNotes().add(n);
		oh.setExtentOfOwnership(eoo);
		// Done building up oleHoldings
		
		// Build up Items
		Items items = new Items();
		Item item = new Item();
		item.setAnalytic("string");
		item.setResourceIdentifier("string");
		item.setItemIdentifier("123");
		item.setPurchaseOrderLineItemIdentifier("");
		item.setVendorLineItemIdentifier("");
		AccessInformation ai = new AccessInformation();
		uri = new URI();
		uri.setResolvable("string");
		uri.setUri("");
		ai.setUri(uri);
		item.setAccessInformation(ai);
		item.setBarcodeARSL("");
		FormerIdentifier fi = new FormerIdentifier();
		fi.setIdentifierType("");
		Identifier id = new Identifier();
		id.setSource("string");
		id.setIdentifierValue("");
		fi.setIdentifier(id);
		item.getFormerIdentifiers().add(fi);
		StatisticalSearchingCode ssc = new StatisticalSearchingCode();
		ssc.setCodeValue("");
		ssc.setFullValue("");
		ssc.setTypeOrSource(tos);
		item.getStatisticalSearchingCodes().add(ssc);
		ItemType it = new ItemType();
		it.setFullValue("");
		it.setTypeOrSource(tos);
		item.setItemType(it);
		loc1 = new Location();
		locLevel1 = new LocationLevel();
		locLevel1.setLevel("Library");
		locLevel1.setName("Linderman");
		locLevel2 = new LocationLevel();
		locLevel2.setLevel("Collection");
		locLevel2.setName("ONLINE");
		locLevel3 = new LocationLevel();
		locLevel3.setLevel("Storage");
		locLevel3.setName("Stacks");
		locLevel2.setSubLocationLevel(locLevel3);
		locLevel1.setSubLocationLevel(locLevel2);
		loc1.setLocLevel(locLevel1);
		item.setLocation(loc1);
		// copyNumber, copyNumberLabel, volumeNumber, and volumeNumberLabel are
		// all null in the sample data.  The constructors for the classes
		// should set them to the empty string, and they're required, so
		// they should show up as empty tags.
		n = new Note();
		n.setType("string");
		n.setNote("");
		item.getNotes().add(n);
		item.setEnumeration("v.1");
		item.setChronology("2013");
		HighDensityStorage hds = new HighDensityStorage();
		// All the subelements of HighDensityStorage are required and empty
		// in the sample data.  So again, they should show up in the output
		// as empty tags
		item.setHighDensityStorage(hds);
		TemporaryItemType tempType = new TemporaryItemType();
		// Also all blank and required, should show up as empty tags
		item.setTemporaryItemType(tempType);
		// fund, donorPublicDisplay, and donorNote are all also required,
		// nillable, and set to the empty string by the item class's
		// constructor, so they should show up as empty tags in the output.
		cn = new CallNumber();
		// itemPart and classificationPart are optional -- set them to the blank
		// string to make them show up as empty tags or else they won't be there
		cn.setClassificationPart("");
		cn.setItemPart("");
		item.setCallNumber(cn);
		// price, numberOfPieces, itemStatus, itemStatusEffectiveDate,
		// checkinNote, staffOnlyFlag, and fastAddFlag are all required,
		// nillable, and set to the empty string by item's constructor
		// so they'll show up as empty tags in the output
		// Done building up item
		
		items.getItems().add(item);
		
		inst.setOleHoldings(oh);
		inst.setItems(items);
		// Done building up instance
		
		//ic.setInstance(inst);
		ic.getInstances().add(inst);
		
	}
	
}
