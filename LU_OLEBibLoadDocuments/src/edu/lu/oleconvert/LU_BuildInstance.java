/* Go through the items and callnums files and use them to generate an ole ingestDocument of type "instance".
 * It should have ole:instance records for each holding, containing a holding record and 1 or more item records
 * See /home/ccc2/dev/OLE Conversion/bulk-ingest-instance.xml for a sample
 */

package edu.lu.oleconvert;

import java.io.BufferedReader;
import java.io.FileReader;
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


	private Record record;
	private HashMap<String, String> callNumbers;
	private HashMap<String, String[]> items;
	
	public LU_BuildInstance() {
		super();
	}
	
	public LU_BuildInstance(Record record) {
		this.record = record;
	}
	
	public Record getRecord() {
		return this.record;
	}
	
	public void setRecord(Record record) {
		this.record = record;
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
	
	public static void ReadInstance(InstanceCollection ic, String callNumbersFilename, String itemsFilename) {
		Instance inst = new Instance();
		BufferedReader callNumbersReader, itemsReader;
		try {
        	callNumbersReader = new BufferedReader(new FileReader(callNumbersFilename));
        	itemsReader = new BufferedReader(new FileReader(itemsFilename));
        	while(callNumbersReader.ready()) {
        		ReadCallNumber(inst, callNumbersReader);
        	}
		} catch(Exception e) {
			System.err.println("Unable to read in call numbers and items: " + e.getMessage());
			e.printStackTrace(System.err);
		}
		
		ic.getInstances().add(inst);
		//ic.setInstance(inst);
	}
	
	public static void ReadCallNumber(Instance inst, BufferedReader callNumbersReader) throws Exception {
		String line = callNumbersReader.readLine();
		String fields[] = line.split("\\|");
		// The contents of the call numbers file was produced by this command:
		// selcallnum -iS -oKNCADSZabchpqryz2 > /ExtDisk/allcallnums.txt
		// So the order of fields is:
		/* 0 callnum key,
		 * 1 callnum key, 
		 * 2 catalog key, 
		 * 3 shelving key, 
		 * 4 item number,
		 * 5 "input strings",
		 * 6 analytics
		 * 7 analytic position
		 * 8 level (NONE, CHILD, or PARENT)
		 * 9 number of copies
		 * 10 number of "call" holds
		 * 11 classification
		 * 12 number of reserve control records
		 * 13 number of academic reserves
		 * 14 library
		 * 15 number of visible copies
		 * 16 shadowed
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
