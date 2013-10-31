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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

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

import org.apache.commons.lang3.StringUtils;
import org.marc4j.marc.Record;
import org.marc4j.marc.VariableField;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

//import edu.indiana.libraries.JPADriver.pojos.*;
import edu.lu.oleconvert.ole.*;

public class LU_BuildInstance {

	// These are indexed by what we have to link to them from the Bibliographic data
	// For the callNumbers, that's the "Item Number", which is in the 999 field's "a" subfield code
	// Librarians know is as the "call number", but Sirsi calls it the Item Number.  It is output by
	// the "D" option to selcallnum, and is in a separate file keyed by the callnumber key.
	// Items are linked by the "Item ID", which is 999 subfield code "i" in the MARC record.
	// It's output by the "B" option to selitem and is field 31 in the output of selitem
	private TreeMap<String, List<List<String>>> callNumbersByCatalogKey;
	private TreeMap<String, List<List<String>>> callNumbersByItemNumber;
	private TreeMap<String, List<List<String>>> itemsByCatalogKey;
	private TreeMap<String, List<List<String>>> itemsByID;
	private static int initSize = 2000000;
	private final String ELECTRONIC_RESOURCE = "WWW";
	// Once OLE has a way to ingest e-instance documents, then set this to true
	// and the code to generate e-instances will run
	private boolean eInstanceReady = false;
	
	public LU_BuildInstance() {
		super();
		callNumbersByCatalogKey = new TreeMap<String, List<List<String>>>();
		itemsByCatalogKey = new TreeMap<String, List<List<String>>>();
		callNumbersByItemNumber = new TreeMap<String, List<List<String>>>();
		itemsByID = new TreeMap<String, List<List<String>>>();
	}

	public LU_BuildInstance(String callNumbersFilename, String shelvingKeysFilename,
							String itemNumbersFilename, String analyticsFilename,
							String itemsFilename) {
		this();
		this.readSirsiFiles(callNumbersFilename, shelvingKeysFilename,
							itemNumbersFilename, analyticsFilename,
							itemsFilename);		
	}
	
	public void printHashMaps(int limit) {
		int i = 0;
		List<List<String>> callNumberStrings;
		List<List<String>> itemStrings;
		LU_BuildOLELoadDocs.Log("Printing hash maps ...");
		LU_BuildOLELoadDocs.Log("");
		LU_BuildOLELoadDocs.Log("Call numbers by catalog key ...");
		for ( String catkey : callNumbersByCatalogKey.keySet() ) {
			callNumberStrings = callNumbersByCatalogKey.get(catkey);
			LU_BuildOLELoadDocs.Log("Catalog key: " + catkey + ", number of callnumbers: " + callNumberStrings.size());
			for ( List<String> callNumberStr : callNumberStrings ) {
				LU_BuildOLELoadDocs.Log("Call number by catalog key " + catkey + ": " + 
			                        	StringUtils.join(callNumberStr.toArray(), ","));
			}
			LU_BuildOLELoadDocs.Log("");
			i++;
			if ( (limit > 0) && i >= limit ) 
				break;
		}
		
		i = 0;
		LU_BuildOLELoadDocs.Log("");
		LU_BuildOLELoadDocs.Log("Call numbers by item number (actual call number) ...");
		for ( String itemnumber : callNumbersByItemNumber.keySet() ) {
			callNumberStrings = callNumbersByItemNumber.get(itemnumber);
			LU_BuildOLELoadDocs.Log("Item number: " + itemnumber + ", number of callnumbers: " + callNumberStrings.size());
			for ( List<String> callNumberStr : callNumberStrings ) {
				LU_BuildOLELoadDocs.Log("Call number by itemnumber " + itemnumber + ": " + 
			                        	StringUtils.join(callNumberStr.toArray(), ","));
			}
			LU_BuildOLELoadDocs.Log("");
			i++;
			if ( (limit > 0) && i >= limit ) 
				break;
		}

		i = 0;
		LU_BuildOLELoadDocs.Log("");
		LU_BuildOLELoadDocs.Log("Items by catalog key ...");
		for ( String catkey : itemsByCatalogKey.keySet() ) {
			itemStrings = itemsByCatalogKey.get(catkey);
			LU_BuildOLELoadDocs.Log("Catalog key: " + catkey + ", number of items: " + itemStrings.size());
			for ( List<String> itemStr : itemStrings ) {
				LU_BuildOLELoadDocs.Log("Item by catalog key " + catkey + ": " + 
			                        	StringUtils.join(itemStr.toArray(), ","));
			}
			LU_BuildOLELoadDocs.Log("");
			i++;
			if ( (limit > 0) && i >= limit ) 
				break;
		}

		i = 0;
		LU_BuildOLELoadDocs.Log("");
		LU_BuildOLELoadDocs.Log("Items by Item ID ...");
		for ( String itemID : itemsByID.keySet() ) {
			itemStrings = itemsByID.get(itemID);
			LU_BuildOLELoadDocs.Log("Item ID: " + itemID + ", number of items: " + itemStrings.size());
			for ( List<String> itemStr : itemStrings ) {
				LU_BuildOLELoadDocs.Log("Item by ID " + itemID + ": " + 
			                        	StringUtils.join(itemStr.toArray(), ","));
			}
			LU_BuildOLELoadDocs.Log("");
			i++;
			if ( (limit > 0) && i >= limit ) 
				break;
		}		
		
	}
	
	public void readSirsiFiles(String callNumbersFilename, String shelvingKeysFilename,
							   String itemNumbersFilename, String analyticsFilename,
							   String itemsFilename) {
		this.readSirsiFiles(callNumbersFilename, shelvingKeysFilename, 
				            itemNumbersFilename, analyticsFilename, itemsFilename, -1);
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
							   String itemsFilename, int limit) {
		BufferedReader callNumbersReader, shelvingKeysReader, itemNumbersReader, analyticsReader, itemsReader;
		try {
        	callNumbersReader = new BufferedReader(new FileReader(callNumbersFilename));
        	shelvingKeysReader = new BufferedReader(new FileReader(shelvingKeysFilename));
        	itemNumbersReader = new BufferedReader(new FileReader(itemNumbersFilename));
        	analyticsReader = new BufferedReader(new FileReader(analyticsFilename));
        	itemsReader = new BufferedReader(new FileReader(itemsFilename));
        	LU_BuildOLELoadDocs.Log("Building hashmap of call number records by call number key " + 
        					   		"and by call number (called \"item number\" by Sirsi) ...");
        	int curr = 0, increment = 100000;
        	while(callNumbersReader.ready() && (limit < 0 || curr < limit)) {
        		// There should be the same number of lines in all 4 files containing callnum data,
        		// and they should all be sorted the same way to line 1 goes with line 1 goes with line 1, etc.
        		// I'm building those files myself, so I can guarantee it, heh
        		// With that in mind, we'll just read lines from all 4 files at once
        		String line = callNumbersReader.readLine();
        		String shelvingKeysLine = shelvingKeysReader.readLine();
        		String itemNumbersLine = itemNumbersReader.readLine();
        		String analyticsLine = analyticsReader.readLine();
        		String fields[] = line.split("\\|");
        		// Can't just assign the return value of Arrays.asList(fields) to callNumberFields --
        		// that method returns an immutable, fixed-size list
        		ArrayList<String> callNumberFields = new ArrayList<String>();
        		callNumberFields.addAll(Arrays.asList(fields));
        		
        		List<String> tmpfields;
        		String tmpStr;

        		// Now add the shelving key, item number, and analytics data to callNumberFields
        		// The shelving keys may also contain pipes, so they are exported into their own file
        		// The first two fields on each line of that file are the call number 
        		// and catalog keys, so we skip them, then add the rest onto the end
        		// of the callNumberFields array.
           		fields = shelvingKeysLine.split("\\|");
        		tmpfields = Arrays.asList(fields).subList(2, fields.length);
        		tmpStr = StringUtils.join(tmpfields, "|");
        		callNumberFields.add(tmpStr);
        		// Now we repeat that process for the itemNumbersLine and analyticsLine
        		fields = itemNumbersLine.split("\\|");
        		tmpfields = Arrays.asList(fields).subList(2, fields.length);
        		tmpStr = StringUtils.join(tmpfields, "|");
        		callNumberFields.add(tmpStr);
        		fields = analyticsLine.split("\\|");
        		tmpfields = Arrays.asList(fields).subList(2, fields.length);
        		tmpStr = StringUtils.join(tmpfields, "|");        		
        		callNumberFields.add(tmpStr);
        		if ( callNumbersByCatalogKey.get(callNumberFields.get(0)) == null ) {
        			List<List<String>> callNumberStrs = new ArrayList<List<String>>();
        			callNumberStrs.add(callNumberFields);
        			callNumbersByCatalogKey.put(callNumberFields.get(0), callNumberStrs);
        		} else {
        			callNumbersByCatalogKey.get(callNumberFields.get(0)).add(callNumberFields);
        		}
        		// Now fill in the hashmap keyed by itemnumber, which should be index 13 in the field list
        		if ( this.callNumbersByItemNumber.get(callNumberFields.get(13)) == null) {
        			List<List<String>> callNumberStrs = new ArrayList<List<String>>();
        			callNumberStrs.add(callNumberFields);
        			callNumbersByItemNumber.put(callNumberFields.get(13), callNumberStrs);        			
        		} else {
        			callNumbersByItemNumber.get(callNumberFields.get(13)).add(callNumberFields);
        		}
        		if ( ++curr % increment == 0 ) {
        			LU_BuildOLELoadDocs.Log(System.out, "On call number " + curr, LU_BuildOLELoadDocs.LOG_DEBUG);
        		}
        	}
        	LU_BuildOLELoadDocs.Log("Building hashmap of item records by catalog key and by Item ID ...");
        	curr = 0;
        	while(itemsReader.ready() && (limit < 0 || curr < limit)) {
        		// Only one file to read from this time
        		String line = itemsReader.readLine();
        		String fields[] = line.split("\\|");
        		List<String> itemNumberFields = Arrays.asList(fields);
        		// Fill in the hash keyed by catalog key, which should be index 2 in the list
        		if ( itemsByCatalogKey.get(itemNumberFields.get(0)) == null ) {
        			List<List<String>> itemStrs = new ArrayList<List<String>>();
        			itemStrs.add(itemNumberFields);
        			itemsByCatalogKey.put(itemNumberFields.get(0), itemStrs);
        		} else {
        			itemsByCatalogKey.get(itemNumberFields.get(0)).add(itemNumberFields);
        		}
        		// Now fill in the hash keyed by Item ID, which is index 31
        		if ( this.itemsByID.get(itemNumberFields.get(31)) == null ) {
        			List<List<String>> itemStrs = new ArrayList<List<String>>();
        			itemStrs.add(itemNumberFields);
        			itemsByID.put(itemNumberFields.get(31), itemStrs);        			
        		} else {
        			itemsByID.get(itemNumberFields.get(31)).add(itemNumberFields);
        		}
        		if ( ++curr % increment == 0 ) {
        			LU_BuildOLELoadDocs.Log(System.out, "On item number " + curr, LU_BuildOLELoadDocs.LOG_DEBUG);
        		}
        		
        	}
        	LU_BuildOLELoadDocs.Log("Done building hashmaps");
		} catch(Exception e) {
			LU_BuildOLELoadDocs.Log(System.err, "Unable to read in call numbers and items: " + e.getMessage(), LU_BuildOLELoadDocs.LOG_ERROR);
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
			LU_BuildOLELoadDocs.Log(System.err, "Unable to marshall instance collection: " + e.getMessage(), LU_BuildOLELoadDocs.LOG_ERROR);
			e.printStackTrace(System.err);
		}
	}
	
	public Map<String, List<String>> getSubfields(VariableField field) {
		HashMap<String, List<String>> subfields = new HashMap<String, List<String>>();
		String fieldStr = field.toString();
		// Take the tag off of there
		fieldStr = fieldStr.replaceFirst(field.getTag() + "\\s+", "");
		//System.out.println("After replacement, whole thing is: " + fieldStr);
		// Split on a $ followed by a lower case letter, since the price subfield will have $ signs in it,
		// and use the LookBehind feature (that's the ?=) to include the delimiter.
		// We can then make a hashmap out of that keyed by the subfield codes
		String[] subfieldsarray = fieldStr.split("(?=\\$[a-z])");
		String key = "", value = "";
		for ( String subfield : subfieldsarray ) {
			// After substition and split, there's an empty string element of
			// the array that we want to skip
			if ( subfield.length() == 0 ) 
				continue;
			key = subfield.substring(0, 2);
			value = subfield.substring(2);
			if ( subfields.get(key) == null ) {
				// There can be multiple subfields with the same subfield code,
				// such as the $o subfield for comments, so we make the values
				// a list
				List<String> sub = new ArrayList<String>();
				sub.add(value);
				subfields.put(key, sub);
			} else {
				subfields.get(key).add(value);
			}
		}
		return subfields;
	}
	
	public void buildInstanceCollection(Record record, InstanceCollection ic, ArrayList<Record> assocMFHDRecords) {

	    String catalogKey = record.getVariableField("001").toString().split(" ")[1];
		//List<List<String>> callNumberStrings = this.callNumbersByCatalogKey.get(catalogKey);
		//List<List<String>> itemStrings = this.itemsByCatalogKey.get(catalogKey);
		List<VariableField> itemsholdings = record.getVariableFields("999");
		//for( List<String> callNumberFields : callNumberStrings ) {
		if ( record.getVariableFields("856").size() > 1 && eInstanceReady ) {
			// multiple 856 fields -- generate e-instances for each one
			// no code to ingest these yet, so don't bother currently
			// TODO: 
			// When code to ingest e-instances exists, take the "&& false"
			// out up above
		}

		// Loop over the 999 fields of the bib record, which represent items
		for ( VariableField field : itemsholdings ) {
			Instance inst = new Instance();
			Map<String, List<String>> subfields = this.getSubfields(field);			
			List<String> itemnumber = subfields.get("$a"); // I think that's the subfield code, check that
			this.buildHoldingsData(record, inst, subfields, assocMFHDRecords); 		
			this.buildItemsData(record, inst, subfields);
			ic.getInstances().add(inst);
		}
		//ic.setInstance(inst);
	}
	
	/*
	 *  The subfields parameter is a hashmap containing all the subfields of a 999 field.
	 * There can be more than one of several of them, so each key returns a list.  The
	 * keys are the subfield codes, like "$a", "$e", etc.  What's in them is described at:
	 * http://carbon.sirsidynix.com/Helps/3.4/Workflows/English/Using_the_949_Entry_for_Cop.html
	 * $a => Call Number -- called "Item Number" in Sirsi databases and used to key into callNumbersByItemNumber hash
	 * $v => Volume Number, $w => Class scheme, $c => copy number, $h => holding code
	 * $i => barcode number (Item ID in Sirsi's items table), $m => Library, $d => last activity date
	 * $e => date last charged, $f => date inventoried, $g => times inventoried, $j => number of pieces
	 * $k => current location, $l => home location, $n => totale charges, $o => comments/notes
	 * $p => price, $q => in house charges, $r => circulate flag, $s => permanent flag
	 * $t => item type, $u => acquisition date, $x => item category 1, $z = item category 2
	 * 
	 */
	public void buildItemsData(Record record, Instance inst, Map<String, List<String>> subfields) {
		if ( inst.getItems() == null ) {
			// Constructor for Items class will initialize the ArrayList
			inst.setItems(new Items());
		}
		
	    Item item = new Item();		


	    // There should only be one subfield "i", as it's the item's barcode and should be unique
	    // And there should be only 1 item with that item ID, so we can just get the first
	    // element of each of those lists
		if ( subfields.get("$i").size() != 1 ||
			 itemsByID.get(subfields.get("$i").get(0)).size() != 1 ) {
			LU_BuildOLELoadDocs.Log(System.err, "Bar code number (item ID) not unique for item: " + subfields.toString(), 
									LU_BuildOLELoadDocs.LOG_ERROR);			
		}
	    List<String> itemString = this.itemsByID.get(subfields.get("$i").get(0)).get(0);
		// The field order of the itemString is described here:		
		// The contents of the items file was produced by this command:
		// selitem -oKabcdfhjlmnpqrstuvwyzA1234567Bk > /ExtDisk/allitems.txt
		// K actually includes the item key, the callnumber key, and the catalog key all in one
		// So the order of fields is:
		/* 0 (K) catalog key
		 * 1 (K) callnumber key
		 * 2 (K) item key
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
		 */
		item.setBarcodeARSL(""); // We don't use this, currently
		ArrayList<FormerIdentifier> fids = new ArrayList<FormerIdentifier>();
		FormerIdentifier fi = new FormerIdentifier();
		Identifier id = new Identifier();
		id.setIdentifierValue(itemString.get(0) + "|" + itemString.get(1) + "|" + itemString.get(2));
		id.setSource("SIRSI_ITEMKEY");
		fi.setIdentifier(id);
		fids.add(fi);
		item.setFormerIdentifiers(fids);
	    
		// TODO: statisticalSearchingCodes?  Not used yet
		ItemType type = new ItemType();
		type.setCodeValue(subfields.get("$t").get(0)); // should be only one of these
		type.setFullValue(subfields.get("$t").get(0));
		// Don't worry about the typeOrSource of the itemType, not sure what that would be
		item.setItemType(type);
		
		Location location = new Location();
		LocationLevel locLevel1 = new LocationLevel();
		locLevel1.setLevel("UNIVERSITY");
		locLevel1.setName("Lehigh University");
		LocationLevel locLevel2 = new LocationLevel();
		locLevel2.setLevel("LIBRARY");
			
			//locLevel2.setName(libraryName);
			//locLevel2.setName(name)
		
		// If we're creating e-instances, then we don't need to make multiple items here, as
		// there will be a totally separate instance for each URL
		// Until OLE can ingest e-instances, we need to make multiple items with different
		// values of the accessInformation for the items that are electronic resources
		if ( subfields.get("$l").equals(ELECTRONIC_RESOURCE) && !eInstanceReady) {
			List<VariableField> accessinfofields = record.getVariableFields("856");
			for ( VariableField accessinfofield : accessinfofields ) {
				Item itemcopy = new Item(item); // copy the item
				List<String> URLs = this.getSubfields(accessinfofield).get("$u");
				if ( URLs != null && URLs.size() > 0 ) {
					AccessInformation ai = new AccessInformation();
					ai.setUri(new URI(URLs.get(0)));
					itemcopy.setAccessInformation(ai);
					inst.getItems().getItems().add(itemcopy);
				}
			}
		} else {
			// not an electronic resource, or eInstances are ready
			inst.getItems().getItems().add(item);
		}
	}
	
	public void buildHoldingsData(Record record, Instance inst, Map<String, List<String>> subfields, ArrayList<Record> assocMFHDRecords) {
		// Use the first callNumber in the list to fill in some info 
		
	    // There should only be one subfield "a", as it's the item's call number and should be unique
	    // And there should be only 1 call number with that "item number" in Sirsi, so we can just get the first
	    // element of each of those lists
		if ( subfields.get("$a").size() != 1 ||
			 callNumbersByItemNumber.get(subfields.get("$a").get(0)).size() != 1 ) {
			LU_BuildOLELoadDocs.Log(System.err, "Call number (item number) not unique for item: " + subfields.toString(),
									LU_BuildOLELoadDocs.LOG_ERROR);			
		}
		List<String> callNumberFields = this.callNumbersByItemNumber.get(subfields.get("$a").get(0)).get(0);
		// The contents of the call numbers file was produced by this command:
		// selcallnum -iS -oKabchpqryz2 > /ExtDisk/allcallnums.txt
		// The shelving Key, output by -oA, the "item number", called call number at Lehigh
		// and output by -oD, and the analytics, output by -oZ, are then concatenated onto
		// the end of the callNumberFields list.  They are output separately since each
		// can contain pipe characters.
		// So the order of fields is:
		/* 0 catalog key, 
		 * 1 callnum key, 
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
		 * 12 shelving key
		 * 13 item number (call number at Lehigh)
		 * 14 analytics
		 */
		String catalogKey = callNumberFields.get(2);
		inst.setInstanceIdentifier(callNumberFields.get(0));
		inst.setResourceIdentifier(callNumberFields.get(2));
		SourceHoldings sh = new SourceHoldings();
		sh.setPrimary(callNumberFields.get(16).equals("1") ? "true" : "false");
		inst.setSourceHoldings(sh);
				
		// Build up oleHoldings within instance
		OLEHoldings oh = new OLEHoldings();
		List<String> nonpublicNoteType = Arrays.asList(".CIRCNOTE.", ".STAFF.");
		List<String> publicNoteType = Arrays.asList(".PUBLIC.");
	    List<String> commentfields = subfields.get("$o"); // TODO: figure out the split and regex, test this
	    for ( String comment : commentfields ) {
	    	// Keep the delimiter on the preceding element of the split array
	    	Note note = new Note();
	    	String[] pieces = comment.split("(?<=\\. )");
	    	if ( pieces.length != 2 ) {
	    		LU_BuildOLELoadDocs.Log(System.err, "Badly formatted comment: " + comment, LU_BuildOLELoadDocs.LOG_ERROR);
		    	for (String piece : pieces ) {
		    		LU_BuildOLELoadDocs.Log(System.err, "Piece: " + piece, LU_BuildOLELoadDocs.LOG_ERROR);
		    		System.out.println("Piece: " + piece);
		    	}
	    	}
	    	if ( nonpublicNoteType.contains(pieces[0].trim())) {
	    		System.out.println("Type is nonpublic");
	    		note.setType("nonpublic");
	    	} else if ( publicNoteType.contains(pieces[0].trim())) {
	    		note.setType("public");
	    	} else {
	    		LU_BuildOLELoadDocs.Log(System.err, "Unknown type of comment: " + comment, LU_BuildOLELoadDocs.LOG_WARN);
	    	}
	    	note.setNote(pieces[1]);
	    	oh.getNotes().add(note);
	    }

	    ExtentOfOwnership extentOfOwnership = new ExtentOfOwnership();
		extentOfOwnership.setType("public");
		// TODO: probably need to split this
		Map<String, List<String>> tmpsubfields = this.getSubfields(record.getVariableField("866"));
		// Should only be one 866 field with one "$a" subfield
		extentOfOwnership.setTextualHoldings(tmpsubfields.get("$a").get(0));
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
