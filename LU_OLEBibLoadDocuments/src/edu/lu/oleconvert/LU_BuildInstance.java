/* Go through the items and callnums files and use them to generate an ole ingestDocument of type "instance".
 * It should have ole:instance records for each holding, containing a holding record and 1 or more item records
 * See /home/ccc2/dev/OLE Conversion/bulk-ingest-instance.xml for a sample
 */

package edu.lu.oleconvert;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javassist.bytecode.Descriptor.Iterator;

import javax.management.Query;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
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

import migration.Issue;
import migration.Serial;
import migration.SerialName;
import migration.SerialNote;
import migration.SerialPhysform;
import migration.SirsiCallNumber;
import migration.SirsiCallNumberID;
import migration.SirsiItem;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;
//import org.solrmarc.callnum.DeweyCallNumber;
//import org.solrmarc.callnum.LCCallNumber;
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
	public TreeMap<String, List<List<String>>> callNumbersByItemNumber;
	private TreeMap<String, List<List<String>>> itemsByCatalogKey;
	private HashMap<String, List<List<String>>> itemsByID;
	private Map<String, List<Map<String, String>>> sfxdata_by_issn;
	private Map<String, List<Map<String, String>>> sfxdata_by_eissn;
	private Map<String, List<Map<String, String>>> sfxdata_by_lccn;
    private Map<String, String> callNumberTypeCodes;
    private Map<String, String> callNumberTypeNames;
    private static List<String> alphaNumCallNums = new ArrayList<String>();
    private static List<String> alphaNumCallNumPrefixes = new ArrayList<String>();
	private Map<String, String> itemReservedStatusCodeMap;
	private Map<String, String> itemReservedStatusNameMap;
	private static Pattern oclctcnpattern = Pattern.compile("\\(OCoLC\\)\\s*(\\d+)");
	private static Pattern sirsitcnpattern = Pattern.compile("\\(Sirsi\\)\\s*o(\\d+)");
	
    private static int initSize = 2000000;
	private final String ELECTRONIC_RESOURCE = "WWW";
	// Once OLE has a way to ingest e-instance documents, then set this to true
	// and the code to generate e-instances will run
	private boolean eInstanceReady = false;
	private final String nonpublicStr = "nonPublic";
	private final String publicStr = "public";
	
	private static final int LOC_INSTITUTION = 1;
	private static final int LOC_LIBRARY = 2;
	private static final int LOC_COLLECTION = 3;
	private static final int LOC_SHELVING = 4;

	Map<String, String> locationCodeToLibraryCode = new HashMap<String, String>();
	Map<String, String> locationCodeToShelvingString = new HashMap<String, String>();
	Map<String, String> libraryCodeToName = new HashMap<String, String>();
	Map<String, String> collectionCodeToName = new HashMap<String, String>();
	Map<String, String> collectionCodeToLibraryCode = new HashMap<String, String>();
	Map<String, String> locationCodeToCollectionCode = new HashMap<String, String>();
	// All renamed locations are shelving-level locations
	Map<String, String> renamedLocations = new HashMap<String, String>(){{
		put("LMC_FLAT", "LMC-A");
		put("LMC_BOOKS", "LMC-B");
		put("LMC_OVRSIZ", "LMC-C");
		put("LMCJOURNAL", "LMC-D");
		put("LMC_JRNL_2", "LMC-E");
		put("LMC1ALCOVE", "LMC-F");
		put("LMC_GOVDOC", "LMC-G");
		put("NEWBOOKS_F", "FM-NEWBKS");
	}};
	
	public static Map<String, String> subscriptionStatusMap = new HashMap<String, String>(){{
		put("ACTIVE", "6");
		put("CANCELLED", "4");
	}};
	
	public static List<String> removedLocations = new ArrayList<String>(){{
		add("CHECKEDOUT");
		add("WITHDRAWN");
		add("LOST");
		add("LOST-ASSUM");
		add("INTRANSIT");
		add("TRANST_LMC");
		add("STORCOMPCT");
		add("OLD_EDUC");
		add("HOLDS");
		add("LOST-CLAIM");
	}};
	
	/*
	Map<String, String> libraryCodeToName = new HashMap<String, String>(){{
		put("FM", "Fairchild-Martindale");
		put("L", "Linderman");
		put("LIND", "Linderman");
		put("Z", "Zoellner");
		put("LMC", "Library Materials Center");
	}};

	String floorToName(String floor) {
		String name = "";
		if ( floor.equals("G") ) {
			name = "Ground";
		} else {
			switch(Integer.parseInt(floor)) {
				case 1: name = "1st";
						break;
				case 2: name = "2nd";
						break;
				case 3: name = "3rd";
						break;
				default: name = floor + "th";
						break;
			}
		}
		return name + " " + "Floor";
	}
	*/
	
	public LU_BuildInstance() {
		super();
		callNumbersByCatalogKey = new TreeMap<String, List<List<String>>>();
		itemsByCatalogKey = new TreeMap<String, List<List<String>>>();
		callNumbersByItemNumber = new TreeMap<String, List<List<String>>>();
		itemsByID = new HashMap<String, List<List<String>>>();
		sfxdata_by_issn = new HashMap<String, List<Map<String, String>>>();
		sfxdata_by_eissn = new HashMap<String, List<Map<String, String>>>();
		sfxdata_by_lccn = new HashMap<String, List<Map<String, String>>>();
		callNumberTypeCodes = new HashMap<String, String>();
		callNumberTypeNames = new HashMap<String, String>();
		// These values for code/value of callnumber types are taken from
		// ole-common/ole-utility/src/main/java/org/kuali/ole/utility/callnumber/CallNumberType.java
		
	    callNumberTypeCodes.put("DEWEYSAN", "DDC");
	    callNumberTypeNames.put("DEWEYSAN", "DDC - Dewey Decimal classification");
	    callNumberTypeCodes.put("DEWPERSAN", "DDC");
	    callNumberTypeNames.put("DEWPERSAN", "DDC - Dewey Decimal classification");
	    callNumberTypeCodes.put("ATDEWEY", "DDC");
	    callNumberTypeNames.put("ATDEWEY", "DDC - Dewey Decimal classification");
	    callNumberTypeCodes.put("DEWEY", "DDC");
	    callNumberTypeNames.put("DEWEY", "DDC - Dewey Decimal classification");
	    callNumberTypeCodes.put("DEWEYPER", "DDC");
	    callNumberTypeNames.put("DEWEYPER", "DDC - Dewey Decimal classification");

	    callNumberTypeCodes.put("LC", "LCC");
	    callNumberTypeNames.put("LC", "LCC - Library of Congress classification");
	    callNumberTypeCodes.put("LCPER", "LCC");
	    callNumberTypeNames.put("LCPER", "LCC - Library of Congress classification");

	    callNumberTypeCodes.put("NLM", "NLM");
	    callNumberTypeNames.put("NLM", "National Library of Medicine Classification (NLM)");

	    callNumberTypeCodes.put("SUDOC", "SUDOC");
	    callNumberTypeNames.put("SUDOC", "Superintendent of Documents classification (SUDOC)");
	    
	    callNumberTypeCodes.put("SCN", "SCN");
	    callNumberTypeNames.put("SCN", "Shelving Control Number (SCN)");
	    
	    callNumberTypeCodes.put(ELECTRONIC_RESOURCE, "OTHER");
	    callNumberTypeNames.put(ELECTRONIC_RESOURCE, "Other schema");
	    callNumberTypeCodes.put("ALPHANUM", "OTHER");
	    callNumberTypeNames.put("ALPHANUM", "Other schema");
	    callNumberTypeCodes.put("ASIS", "OTHER");
	    callNumberTypeNames.put("ASIS", "Other schema");
	    callNumberTypeCodes.put("ATDEWEYLOC", "OTHER");
	    callNumberTypeNames.put("ATDEWEYLOC", "Other schema");
	    callNumberTypeCodes.put("AUTO", "OTHER");
	    callNumberTypeNames.put("AUTO", "Other schema");
	 
	    alphaNumCallNums.add("Current periodical");
	    alphaNumCallNums.add("Current periodicals");
	    alphaNumCallNums.add("Current Periodical");
	    alphaNumCallNums.add("Current Periodicals");
	    alphaNumCallNums.add("FILM");
	    alphaNumCallNums.add("MICROFICHE");
	    alphaNumCallNums.add("MICROCARD");
	    alphaNumCallNums.add("Electronic Book");
	    alphaNumCallNums.add("Electronic book");
	    
	    alphaNumCallNumPrefixes.add("DISS");
	    alphaNumCallNumPrefixes.add("THESIS");
	    alphaNumCallNumPrefixes.add("SC Asteroid");
	    alphaNumCallNumPrefixes.add("SC ALS");
	    alphaNumCallNumPrefixes.add("SC B878");
	    alphaNumCallNumPrefixes.add("SC Bas");
	    alphaNumCallNumPrefixes.add("SC Bay");
	    alphaNumCallNumPrefixes.add("SC Berman");
	    alphaNumCallNumPrefixes.add("SC Bir");
	    alphaNumCallNumPrefixes.add("SC CD");
	    alphaNumCallNumPrefixes.add("SC Col");
	    alphaNumCallNumPrefixes.add("SC Cowper");
	    alphaNumCallNumPrefixes.add("SC FF");
	    alphaNumCallNumPrefixes.add("SC FI");
	    alphaNumCallNumPrefixes.add("SC Flat case");
	    alphaNumCallNumPrefixes.add("SC FR xxx");
	    alphaNumCallNumPrefixes.add("SC GI"); 
	    alphaNumCallNumPrefixes.add("SC GSP 2SER");                                                                                                                                                                                                                                                          
	    alphaNumCallNumPrefixes.add("SC Hen");
	    alphaNumCallNumPrefixes.add("SC Hom");
	    alphaNumCallNumPrefixes.add("SC Ize");
	    alphaNumCallNumPrefixes.add("SC LEC");
	    alphaNumCallNumPrefixes.add("SC Lehigh Photos");
	    alphaNumCallNumPrefixes.add("SC LETTER");
	    alphaNumCallNumPrefixes.add("SC Letter DG");
	    alphaNumCallNumPrefixes.add("SC Linz");
	    alphaNumCallNumPrefixes.add("SC LPub");
	    alphaNumCallNumPrefixes.add("SC LSer");
	    alphaNumCallNumPrefixes.add("SC LUP");
	    alphaNumCallNumPrefixes.add("SC LVF");
	    alphaNumCallNumPrefixes.add("SC Min");
	    alphaNumCallNumPrefixes.add("SC MS");
	    alphaNumCallNumPrefixes.add("SC Pam");
	    alphaNumCallNumPrefixes.add("SC Photo");
	    alphaNumCallNumPrefixes.add("SC Stereo 001");
	    alphaNumCallNumPrefixes.add("SC Storage Office");
	    alphaNumCallNumPrefixes.add("SC T Galleria");
	    alphaNumCallNumPrefixes.add("SC T"); 
	    alphaNumCallNumPrefixes.add("SC TechPhoto");
	    alphaNumCallNumPrefixes.add("SC Text");
	    alphaNumCallNumPrefixes.add("SC Trx");
	    alphaNumCallNumPrefixes.add("SC TVF");
	    alphaNumCallNumPrefixes.add("SC VKm");
	    alphaNumCallNumPrefixes.add("SC Wyl");


	    // We only ever use the ON_RESERVE status to mark items
	    // that are on course reserve as ONHOLD
	    // If an item isn't on course reserve, we derive its status
	    // from other fields.  
	    // "Flagged" I don't know what to do with at all.
		itemReservedStatusCodeMap = new HashMap<String, String>();
		itemReservedStatusNameMap = new HashMap<String, String>();
		//itemStatusCodeMap.put("NOT_ON_RES", "AVAILABLE");
		//itemStatusNameMap.put("NOT_ON_RES", "Available");
		itemReservedStatusCodeMap.put("ON_RESERVE", "ONHOLD");
		itemReservedStatusNameMap.put("ON_RESERVE", "On Hold");
		//itemStatusCodeMap.put("FLAGGED", "UNAVAILABLE");
		//itemStatusNameMap.put("FLAGGED", "Unavailable");
	}

	public LU_BuildInstance(String callNumbersFilename, String shelvingKeysFilename,
							String itemNumbersFilename, String analyticsFilename,
							String itemsFilename, String LocationsFileName,
							String sfxExportFileName) {
		this();
		this.readSirsiFiles(callNumbersFilename, shelvingKeysFilename,
							itemNumbersFilename, analyticsFilename,
							itemsFilename);
		this.readLehighData(LocationsFileName, sfxExportFileName);
	}
	
	public LU_BuildInstance(boolean stage, String callNumbersFilename, 
			String shelvingKeysFilename, String boundWithsFilename,
			String itemNumbersFilename, String analyticsFilename,
			String itemsFilename, String LocationsFileName,
			String sfxExportFileName) {
		this();
		if ( stage ) {
			this.stageSirsiFiles(callNumbersFilename, shelvingKeysFilename,
					boundWithsFilename, itemNumbersFilename, analyticsFilename,
					itemsFilename);
		}
		this.readLehighData(LocationsFileName, sfxExportFileName);
	}
	
	public void printHashMaps(int limit, PrintWriter output) {
		int i = 0;
		List<List<String>> callNumberStrings;
		List<List<String>> itemStrings;
		LU_DBLoadInstances.Log(output, "Printing hash maps ...");
		LU_DBLoadInstances.Log(output, "");
		LU_DBLoadInstances.Log(output, "Call numbers by catalog key ...");
		for ( String catkey : callNumbersByCatalogKey.keySet() ) {
			callNumberStrings = callNumbersByCatalogKey.get(catkey);
			LU_DBLoadInstances.Log(output, "Catalog key: " + catkey + ", number of callnumbers: " + callNumberStrings.size());
			for ( List<String> callNumberStr : callNumberStrings ) {
				LU_DBLoadInstances.Log(output, "Call number by catalog key " + catkey + ": " + 
			                        	StringUtils.join(callNumberStr.toArray(), ","));
			}
			LU_DBLoadInstances.Log(output, "");
			i++;
			if ( (limit > 0) && i >= limit ) 
				break;
		}
		
		i = 0;
		LU_DBLoadInstances.Log(output, "");
		LU_DBLoadInstances.Log(output, "Call numbers by item number (actual call number) ...");
		for ( String itemnumber : callNumbersByItemNumber.keySet() ) {
			callNumberStrings = callNumbersByItemNumber.get(itemnumber);
			LU_DBLoadInstances.Log(output, "Item number: " + itemnumber + ", number of callnumbers: " + callNumberStrings.size());
			for ( List<String> callNumberStr : callNumberStrings ) {
				LU_DBLoadInstances.Log(output, "Call number by itemnumber " + itemnumber + ": " + 
			                        	StringUtils.join(callNumberStr.toArray(), ","));
			}
			LU_DBLoadInstances.Log(output, "");
			i++;
			if ( (limit > 0) && i >= limit ) 
				break;
		}

		i = 0;
		LU_DBLoadInstances.Log(output, "");
		LU_DBLoadInstances.Log(output, "Items by catalog key ...");
		for ( String catkey : itemsByCatalogKey.keySet() ) {
			itemStrings = itemsByCatalogKey.get(catkey);
			LU_DBLoadInstances.Log(output, "Catalog key: " + catkey + ", number of items: " + itemStrings.size());
			for ( List<String> itemStr : itemStrings ) {
				LU_DBLoadInstances.Log(output, "Item by catalog key " + catkey + ": " + 
			                        	StringUtils.join(itemStr.toArray(), ","));
			}
			LU_DBLoadInstances.Log(output, "");
			i++;
			if ( (limit > 0) && i >= limit ) 
				break;
		}

		i = 0;
		LU_DBLoadInstances.Log(output, "");
		LU_DBLoadInstances.Log(output, "Items by Item ID ...");
		for ( String itemID : itemsByID.keySet() ) {
			itemStrings = itemsByID.get(itemID);
			LU_DBLoadInstances.Log(output, "Item ID: " + itemID + ", number of items: " + itemStrings.size());
			for ( List<String> itemStr : itemStrings ) {
				LU_DBLoadInstances.Log(output, "Item by ID " + itemID + ": " + 
			                        	StringUtils.join(itemStr.toArray(), ","));
			}
			LU_DBLoadInstances.Log(output, "");
			i++;
			if ( (limit > 0) && i >= limit ) 
				break;
		}		
		
	}
	
	public static String normalizeCallNumber(String callnum, String type) {
		String normcn = "";
		/*
		if ( type.toUpperCase().equals("SUDOC") ) {
			normcn = CallNumUtils.getSuDocShelfKey(callnum);
		} else if ( type.toUpperCase().equals("LCC") ) {
			normcn = CallNumUtils.getLCShelfkey(callnum, callnum);
		} else if ( type.toUpperCase().equals("DDC") ) {
			normcn = CallNumUtils.getDeweyShelfKey(callnum);
		} else {
			LU_DBLoadInstances.Log(System.out, "Callnumber type is neither SuDoc, LCC, or DDC, not normalizing: " 
                    + callnum + ", type: " + type,
                    LU_DBLoadInstances.LOG_WARN);
			normcn = callnum;						
		}
		*/
		


		if ( type.toUpperCase().equals("SUDOC") ) {
			// No "isValid" method for SuDoc
			normcn = CallNumUtils.getSuDocShelfKey(callnum);
			/*
			String SUDOC_REGEX = "[^A-Z0-9]+|(?<=[A-Z])(?=[0-9])|(?<=[0-9])(?=[A-Z])";
			String upcaseSuDoccallnum = callnum.toUpperCase();
			StringBuffer shelfKey = new StringBuffer();
			//split the call number based on numbers and alphabets
			String[] cNumSub = upcaseSuDoccallnum.split(SUDOC_REGEX);
			for (String str : cNumSub) {
				if (StringUtils.isNumeric(str)) {   // numbers
					// append zeros to sort Ordinal
					str = StringUtils.leftPad(str, 5, "0"); // constant length 5
					shelfKey.append(str);
					shelfKey.append(" ");
				} else {                     // alphabets
					// append spaces to sort Lexicographic
					str = StringUtils.rightPad(str, 5);  // constant length 5
					shelfKey.append(str);
					shelfKey.append(" ");
				}
			}
			normcn = shelfKey.toString().trim();
			*/
			/*
			LU_DBLoadInstances.Log(System.out, "Callnumber type is SuDoc, not normalizing: " 
                    + callnum + ", type: " + type,
                    LU_DBLoadInstances.LOG_INFO);
			normcn = callnum;
			*/
			/*
			 * 		
			 * // Regular expression stolen from SuDoc.pm:
			 * // https://metacpan.org/pod/Text::SuDocs
			String sudocpattern = "^(\\p{IsAlpha}+)\\s*(\\p{IsDigit}+)\\s*\\.\\s*(?:(\\p{IsAlpha}+)\\s+)?(\\p{IsDigit}+)(?:/(\\p{IsAlnum}+)(-\\p{IsAlnum}+)?)?\\s*(?::\\s*(.*))?$";
			Pattern p;
			Matcher m;
			String agency = "", subagency = "", committee = "", series = "", relseries = "", document = "";
			p = Pattern.compile(sudocpattern);
			m = p.matcher(callnum);
			if ( m.find() ) {
				agency = ( m.group(1) != null) ? m.group(1) : "";
				subagency = ( m.group(2) != null )  ? m.group(2) : "";
				committee = ( m.group(3) != null ) ? m.group(3) : "";
				if ( committee != null && committee.length() > 0 ) {
					committee += " ";
				}
				series = ( m.group(4) != null ) ? m.group(4) : "";
				relseries = ( m.group(5) != null ) ? m.group(5) : "";
				if ( m.group(6) != null && m.group(6).length() > 0 ) {
					relseries = relseries + "." + m.group(6);
				}
				if ( relseries != null && relseries.length() > 0 ) {
					relseries = "/" + relseries;
				}
				document = ( m.group(7) != null ) ? m.group(7) : "";
				normcn = String.format("%s %s.%s%s%s", agency, subagency, committee, series, relseries);
				String format = "%08d";
				//normcn = normcn.replaceAll("\\b(\\d+)\\b", String.format(format, Integer.parseInt("$1")));
				p = Pattern.compile("\\b(\\d{1,6})\\b");
				m = p.matcher(normcn);
				//StringBuffer new_normcn = new StringBuffer();
				//m.replaceAll(String.format(format, Integer.parseInt("5")));
				int index = 0;
				while ( m.find(index) ) {
					
					int num = Integer.parseInt(m.group(1));
					String repl = String.format(format, num);
					System.out.println("Normalizing sudoc, matching group: " + m.group(1) + ", replacing with " + repl);
					index = normcn.indexOf(m.group(1)) + 8;
					normcn = m.replaceFirst(String.format(format, num));
					m = p.matcher(normcn);
					System.out.println("String is now: " + normcn);
					
				}
				//normcn = normcn.replaceAll("\\b(\\d+)\\b", "$1");
				
				normcn = normcn.replaceAll("\\s", "_");
				
			} else {
				LU_DBLoadInstances.Log(System.out, "SuDoc callnumber didn't match pattern: " + callnum,
									   LU_DBLoadInstances.LOG_WARN);
				normcn = callnum;
			}
			*/
		} else if ( type.toUpperCase().equals("LCC") ) {
			//LCCallNumber lcc_num = new LCCallNumber(callnum);
			//normcn = lcc_num.getShelfKey();
			if ( CallNumUtils.isValidLC(callnum) ) {
				normcn = CallNumUtils.getLCShelfkey(callnum, null);
			} else {
				LU_DBLoadInstances.Log(System.out, "Callnumber is not valid LCC, not normalizing: " 
                        + callnum + ", type: " + type,
                        LU_DBLoadInstances.LOG_WARN);
				normcn = callnum;			
			}
		} else if ( type.toUpperCase().equals("DDC") ) {
			//DeweyCallNumber ddc_num = new DeweyCallNumber(callnum);
			//normcn = ddc_num.getShelfKey();
			if ( CallNumUtils.isValidDewey(callnum) ||
				 CallNumUtils.isValidDeweyWithCutter(callnum) ) {
				normcn = CallNumUtils.getDeweyShelfKey(callnum);
			} else {
				LU_DBLoadInstances.Log(System.out, "Callnumber is not valid DDC, not normalizing: " 
						+ callnum + ", type: " + type,
						LU_DBLoadInstances.LOG_WARN);
				normcn = callnum;			
			}
		} else {
			LU_DBLoadInstances.Log(System.out, "Callnumber type is neither SuDoc, LCC, or DDC, not normalizing: " 
					+ callnum + ", type: " + type,
					LU_DBLoadInstances.LOG_WARN);
			normcn = callnum;			
		}
		
		return normcn;
	}
	
	public static String computeISBN10CheckDigit(String isbn) {
		int digit = 0;
		for ( int i = 0; i < 9; i++ ) {
			digit += Integer.parseInt(isbn.substring(i, i+1)) * (i+1);
		}
		digit = digit % 11;
		if ( digit == 10 ) {
			return "X";
		} else {
			return Integer.toString(digit);
		}
	}
	
	public static String formatISBNString(String isbn) {
		String newisbn = isbn;
		Matcher m;
		// If it's a 9-digit ISBN, add a check digit and a note explaining
		// that's what we did, in case it proves to be wrong later
		// Examples: "192112953", "224613960"
		Pattern p = Pattern.compile("^(\\d{9})\\s+[A-Za-z\\.\\:]*$");
		Pattern p2 = Pattern.compile("^(\\d{9})$");
		Matcher m2 = p2.matcher(newisbn);
		m = p.matcher(newisbn);
		if ( m.find() || m2.find() ) {
			LU_DBLoadInstances.Log(System.out, "Nine digit ISBN, adding check digit and annotation", LU_DBLoadInstances.LOG_DEBUG);
			newisbn += computeISBN10CheckDigit(newisbn) + " (Check digit added on load into OLE)";
		}
		// Lots of ISBNs with trailing "." or "pbk." or "(pbk. :"
		// For such instances we want to enclose the trailing sequence
		// in parentheses (or close the unclosed open paren)
		// Examples: "0819110264 (pbk. :", "0521222303.", "0702211451 pbk."
		p = Pattern.compile("^(\\d+)\\s+(\\()?([A-Za-z\\.\\:\\s]+)$");
		m = p.matcher(newisbn);
		if ( m.find() ) {
			LU_DBLoadInstances.Log(System.out, "Trailing characters found, enclosing in parens", LU_DBLoadInstances.LOG_DEBUG);
			for ( int i = 1; i <= m.groupCount(); i++ ) {
				LU_DBLoadInstances.Log(System.out, "Match group " + i + ", " + m.group(i), LU_DBLoadInstances.LOG_DEBUG);
			}
			// group 2 would be the mismatched opening paren, if it's there
			newisbn = m.group(1) + " (" + m.group(3) + ")"; 
		}

		// Special case for the trailing dot with no space preceding it, which
		// seems to be all over the place in our data
		p = Pattern.compile("^(\\d{10})\\.\\s*$");
		m = p.matcher(newisbn);
		if ( m.find() ) {
			LU_DBLoadInstances.Log(System.out, "Trailing dot found, removing it", LU_DBLoadInstances.LOG_DEBUG);
			for ( int i = 1; i <= m.groupCount(); i++ ) {
				LU_DBLoadInstances.Log(System.out, "Match group " + i + ", " + m.group(i), LU_DBLoadInstances.LOG_DEBUG);
			}
			newisbn = m.group(1); 
		}
		return newisbn;
	}
	
	public static String formatTitleControlNumber(String tcn) {
		//tcn = tcn.replaceAll("(Sirsi)", "");
		if ( tcn != null ) { 
			LU_DBLoadInstances.Log(System.out, "Formatting title control number: " + tcn,
					LU_DBLoadInstances.LOG_DEBUG);
			tcn = tcn.trim();
			if ( tcn.length() >= 3 ) {
				String pref = tcn.substring(0, 3);
				String rest = tcn.substring(3);
				if ( pref.equals("ocm") ||
						pref.equals("ocn") ) {
					tcn = "(OCoLC)" + StringUtils.leftPad(rest, 8, "0");
				} else if ( pref.equals("ebr") ) {
					tcn = "(CaPaEBR)" + rest;
				} else if ( pref.equals("aas") || 
						tcn.substring(0, 1).equals("i") ) { 
					tcn = "(Sirsi)" + tcn;
				} else if ( pref.equals("tmp") ) {
					// do nothing, just return tcn unmodified
				} else if ( pref.equals("999") ) {
					tcn = "(Sirsi)sc" + tcn;
				} 
			}
			LU_DBLoadInstances.Log(System.out, "Formatted to: " + tcn,
					LU_DBLoadInstances.LOG_DEBUG);
		} else {
			LU_DBLoadInstances.Log(System.out, "Not formatting null  title control number",
					LU_DBLoadInstances.LOG_DEBUG);
		}
		return tcn;
	}
	
	// Returns the index in the record's collection of fields of the first
	// field with the tag in the "field" parameter, or if there aren't any instances
	// of that field, then the index of the first field that is lexicographically
	// greater than that field
	public static int getFirstFieldIndex(Record record, String field) {
		int i = 0;
		//boolean none = (record.getVariableFields(field).size() == 0);
		boolean none = (record.getVariableFields(field).size() == 0);
		List<DataField> fields = record.getDataFields();
		i = 0;
		while ( ( i < fields.size() ) &&
				!( (none && (fields.get(i).getTag().compareTo(field) > 0 ) ) ||		
			        fields.get(i).getTag().equals(field) ) ) {
			i++;
		}
		return i;
	}
	
	// Subfields with tag "?" and data "UNAUTHORIZED" appear all over 
	// in Sirsi's bib export.  OLE complains about them, so we remove
	// them all here.
	public static void removeUnauthorizedFields(Record record) {
		List<DataField> datafields = record.getDataFields();
		Subfield s;
		for ( DataField df : datafields ) {
			do {
				s = df.getSubfield('?');
				if ( s != null && s.getData().trim().equals("UNAUTHORIZED") ) {
					df.removeSubfield(s);
				}
			} while ( s!= null );
		}
	}
	
	public static void checkTitleControlNumbers(Record record, String titleControlNumber) {
	    MarcFactory factory = MarcFactory.newInstance();
	    int first035 = getFirstFieldIndex(record, "035");
	    boolean oclctcnfound = false;
	    List<VariableField> controlnumbers = record.getVariableFields("035");
	    String tcnPrefix = null, tcnNum = null;
	    Matcher m;
	    List<String> existingTCNs = new ArrayList<String>();
	    //Map<String, List<String>> subfields;
	    DataField newtcn;
	    String oclcnum = "";
	    String sirsinum = "";
	    int oclcnumindex = 0, sirsinumindex = 0;
	    if ( titleControlNumber != null && titleControlNumber.length() > 3 ) {
	    	tcnPrefix = titleControlNumber.substring(0, 3);
	    	tcnNum = titleControlNumber.substring(3);
	    	// No 035's, add the titleControlNumber
	    	LU_DBLoadInstances.Log(System.out, "Adding title control number at index " + first035, LU_DBLoadInstances.LOG_DEBUG);
	    	newtcn = factory.newDataField("035", ' ', ' ');
	    	titleControlNumber = formatTitleControlNumber(titleControlNumber);
	    	newtcn.addSubfield(factory.newSubfield('a', titleControlNumber));
	    	record.getDataFields().add(first035, newtcn);
	    	//existingTCNs.add(newtcn.toString().trim());
	    	oclctcnfound = ( tcnPrefix.equals("ocm") || tcnPrefix.equals("ocn") );
	    	if ( oclctcnfound ) {
	    		oclcnum = tcnNum;
	    	}
	    }
	    // There could be multiple 035 fields.  One of them may already be in the form "(OCoLC)<number>"
	    // If so, swap that one in to be first.
	    // If there isn't one like that, but there is one of the form 
	    // (\(.*\))\s*?o(\d+)
	    // Then make that one first and change it to be
	    // (OCoLC)$2
	    // First we'll look for one that starts with (OCoLC)
	    
	    for ( int i = 0; i < record.getDataFields().size(); i++ ) {
	    	DataField tcn_df = record.getDataFields().get(i); 
	    	if ( tcn_df.getTag().equals("035")) {
	    		if ( existingTCNs.contains(tcn_df.toString().trim()) ) {
	    			LU_DBLoadInstances.Log(System.out, "Removing 035 with only duplicate TCN data " + 
	    					tcn_df.toString().trim() + " at index " + i, 
	    					LU_DBLoadInstances.LOG_DEBUG);
	    			record.getDataFields().remove(i);
	    			i--; // Everything past this in the array has been left-shifted one now, so we decrement i
	    			continue;
	    		} else {
	    			existingTCNs.add(tcn_df.toString().trim());
	    			LU_DBLoadInstances.Log(System.out, "Added data to existing TCNs: " + tcn_df.toString(),
	    					LU_DBLoadInstances.LOG_DEBUG);

	    			List<Subfield> subfields = tcn_df.getSubfields('a');
	    			LU_DBLoadInstances.Log(System.out, "TCN found at index " + i, 
	    					LU_DBLoadInstances.LOG_DEBUG);
	    			if ( (oclcnum.length() > 0 || sirsinum.length() > 0) ) {
	    				if ( tcn_df.getSubfields().size() == 1 ) {
	    					// We already found a TCN for this record, and this 035 only has
	    					// a single subfield.  If the number matches, remove this field
	    					String data = tcn_df.getSubfields().get(0).getData().trim();
	    					LU_DBLoadInstances.Log(System.out, "Checking for data in existing OCLC and Sirsi TCNs: " + data,
	    							LU_DBLoadInstances.LOG_DEBUG);
	    					if ( (oclcnum.length() > 0 && data.contains(oclcnum)) || // just the number part of OCLC TCN matches
	    							(sirsinum.length()> 0 && data.contains(sirsinum)) ) { // exact match for any other TCN type
	    						LU_DBLoadInstances.Log(System.out, "Removing 035 with only duplicate TCN data " + data + " at index " + i, 
	    								LU_DBLoadInstances.LOG_DEBUG);
	    						record.getDataFields().remove(i);
	    						i--; // Everything past this in the array has been left-shifted one now, so we decrement i
	    						continue;
	    					}
	    				}
	    			}
	    			for ( Subfield sub : subfields ) {
	    				String data = sub.getData().trim();
	    				m = oclctcnpattern.matcher(data);
	    				if ( m.find() ) {
	    					// There already is an OCoLC number, make it first and remove any dups
	    					oclcnum = m.group(1);
	    					oclcnumindex = i;
	    				}
	    				m = sirsitcnpattern.matcher(data);
	    				if ( m.find() ) {
	    					// May want to remove this one, if there is also an
	    					// OCoLCtcnstr and the value of the number matched by \\d+ is the same 
	    					sirsinum = m.group(1);
	    					sirsinumindex = i;
	    				}
	    			}
	    		}
	    	} else {
	    		LU_DBLoadInstances.Log(System.out, "Skipping data field with tag " + tcn_df.getTag(), 
	    				LU_DBLoadInstances.LOG_DEBUG);
	    	}
	    }
	    LU_DBLoadInstances.Log(System.out, "Existing OCLC num: " + oclcnum + " at index " + oclcnumindex + 
	    		", existing sirsi num: " + sirsinum + " at index " + sirsinumindex, 
	    		LU_DBLoadInstances.LOG_DEBUG);
	    if ( oclcnum.length() > 0 ) {
	    	// Find and remove the OCoLC 035 entry, and insert it to be the first 035 entry
	    	if ( sirsinum.length() > 0 && sirsinum.equals(oclcnum)) {
	    		// There's an identical Sirsi number, remove it
	    		if ( oclcnumindex > sirsinumindex ) {
	    			oclcnumindex--;
	    		}
	    		record.getDataFields().remove(sirsinumindex);
	    	}
	    	LU_DBLoadInstances.Log(System.out, "Moving OCLC control number to index " + first035, LU_DBLoadInstances.LOG_DEBUG);
	    	DataField oclcnumfield = record.getDataFields().get(oclcnumindex);
	    	record.getDataFields().remove(oclcnumindex);
	    	record.getDataFields().add(first035, oclcnumfield);
	    } else if ( sirsinum.length() > 0 && titleControlNumber != null ) {
	    	LU_DBLoadInstances.Log(System.out, "Existing sirsi num: " + sirsinum + " at index " + sirsinumindex, 
	    			LU_DBLoadInstances.LOG_DEBUG);

	    	// No OCLC number, but there is a Sirsi number.  Remove it
	    	record.getDataFields().remove(sirsinumindex);

	    	// Then add the formatted 035 from the Sirsi number
	    	newtcn = factory.newDataField("035", ' ', ' ');
	    	titleControlNumber = formatTitleControlNumber(sirsinum);
	    	newtcn.addSubfield(factory.newSubfield('a', titleControlNumber));
	    	record.getDataFields().add(first035, newtcn);
	    } else if ( titleControlNumber != null ) {
	    	LU_DBLoadInstances.Log(System.out, "No existing OCLC or Sirsi TCN, adding new one at index " + first035, 
	    			LU_DBLoadInstances.LOG_DEBUG);
	    	newtcn = factory.newDataField("035", ' ', ' ');
	    	titleControlNumber = formatTitleControlNumber(titleControlNumber);
	    	newtcn.addSubfield(factory.newSubfield('a', titleControlNumber));
	    	record.getDataFields().add(first035, newtcn);

	    }
	    
	}
	
	public static void append999fields(Record destrec, Record sourcerec) {
		MarcFactory factory = MarcFactory.newInstance();
		List<VariableField> source999s = sourcerec.getVariableFields("999");
		for( VariableField source999vf : source999s ) {
			DataField source999 = (DataField) source999vf;
			//DataField dest999 = factory.newDataField("999", ' ', ' ');
			//dest999.
			destrec.addVariableField(source999);
			//destrec.getDataFields().add(source999);
		}
	}
	
	public static void fixISBN(Record record) {
	    MarcFactory factory = MarcFactory.newInstance();
		List<VariableField> isbns = record.getVariableFields("020");
		Map<String, List<String>> subfields;		
		LU_DBLoadInstances.Log(System.out, "Before modification, record is: " + record.toString(), LU_DBLoadInstances.LOG_DEBUG);
		for ( VariableField isbnentry :isbns ) {
			subfields = LU_BuildInstance.getSubfields(isbnentry);
			// if there are multiple "$a" subfields, break them out to create multiple 020s
			// with a single "$a" subfield, and all other fields identical
			if ( subfields.get("$a") != null && subfields.get("$a").size() > 1 ) {
				LU_DBLoadInstances.Log(System.out, "Multiple a subfields, removing the isbn record and creating multiple 020s instead", 
						               LU_DBLoadInstances.LOG_DEBUG);
				record.removeVariableField(isbnentry);
				for ( String subfieldaval : subfields.get("$a") ) {
					//ControlFieldImpl newisbn = new ControlFieldImpl();
					DataField newisbn = factory.newDataField("020", ' ', ' ');

					// Add just the one "$a" subfield
					newisbn.addSubfield(factory.newSubfield('a', subfieldaval));
					// Then add all the other values of all the other subfields 
					// of isbnentry to newisbn
					for ( String subfield : subfields.keySet() ) {
						List<String> fieldvals = subfields.get(subfield);
						if ( !subfield.equals("$a") ) {
							for ( String fieldval : fieldvals ) {
								newisbn.addSubfield(factory.newSubfield(subfield.charAt(1), fieldval));
							}
						}
					}
					LU_DBLoadInstances.Log(System.out, "Adding new isbn: " + newisbn.toString(), LU_DBLoadInstances.LOG_DEBUG);
					record.addVariableField(newisbn);
					LU_DBLoadInstances.Log(System.out, "Record is now: " + record.toString(), LU_DBLoadInstances.LOG_DEBUG);
				}					
			} else {
				LU_DBLoadInstances.Log(System.out, "Zero or one subfields in record's ISBN, not modifying it", LU_DBLoadInstances.LOG_DEBUG);
			}
		}

		// Now there should only be 1 "$a" subfield per 020, but we want to
		// make sure they're all valid.  Add a "check digit" to the 9 character
		// ones.  Surround instances of "pbk.", "." etc with parentheses.
		isbns = record.getVariableFields("020");
		for ( VariableField isbnentry : isbns ) {
			subfields = LU_BuildInstance.getSubfields(isbnentry);
			if ( subfields.get("$a") != null && subfields.get("$a").size() > 0 ) { 
				String isbnstr = subfields.get("$a").get(0);
				LU_DBLoadInstances.Log(System.out, "Formatting ISBN string " + isbnstr, LU_DBLoadInstances.LOG_DEBUG);
				String newisbnstr = LU_BuildInstance.formatISBNString(isbnstr);
				LU_DBLoadInstances.Log(System.out, "New ISBN string: " + newisbnstr, LU_DBLoadInstances.LOG_DEBUG);
				if ( !newisbnstr.equals(isbnstr) ) {
					record.removeVariableField(isbnentry);
					DataField newisbn = factory.newDataField("020", ' ', ' ');
					newisbn.addSubfield(factory.newSubfield('a', newisbnstr));
					for ( String subfield : subfields.keySet() ) {
						List<String> fieldvals = subfields.get(subfield);
						if ( !subfield.equals("$a") ) {
							for ( String fieldval : fieldvals ) {
								newisbn.addSubfield(factory.newSubfield(subfield.charAt(1), fieldval));
							}
						}
					}					
					record.addVariableField(newisbn);
				}
				LU_DBLoadInstances.Log(System.out, "After formatting ISBN strings, record is now: " + record.toString(),
						               LU_DBLoadInstances.LOG_DEBUG);
			} else {
				LU_DBLoadInstances.Log(System.out, "No $a subfields to format in record's ISBN: " + record.toString(), 
						               LU_DBLoadInstances.LOG_DEBUG);
			}
		}
				
	}
	
	public void readLehighData(String locationsFilename, String sfxExportFileName) {
		BufferedReader locationsReader, sfx_reader;
		String line = "";
		String pieces[] = null, headers[] = null;
		int curr = 0, limit = -1;
		try {
			
			TypedQuery<Location> query = LU_DBLoadInstances.ole_em.createQuery("select l from Location l", Location.class);
			List<Location> locations = query.getResultList();
			for ( Location loc : locations ) {
				if ( loc.getLevel().getCode().equals("SHELVING") ) {
					this.locationCodeToShelvingString.put(loc.getCode(), loc.getName());
					// TODO: locations won't necessarily come in the right order for this to work
					// rethink how it's done
					if ( loc.getParentLocation().getLevel().getCode().equals("LIBRARY") ) {
						this.locationCodeToLibraryCode.put(loc.getCode(), loc.getParentLocation().getCode());
					} else if (loc.getParentLocation().getLevel().getCode().equals("COLLECTION") ){
						this.locationCodeToCollectionCode.put(loc.getCode(), loc.getParentLocation().getCode());
					}
				} else if ( loc.getLevel().getCode().equals("LIBRARY") ) {
					this.libraryCodeToName.put(loc.getCode(), loc.getName());
				} else if ( loc.getLevel().getCode().equals("COLLECTION") ) {
					this.collectionCodeToName.put(loc.getCode(), loc.getName());
					this.collectionCodeToLibraryCode.put(loc.getCode(), loc.getParentLocation().getCode());
				}
			}
			/*
			locationsReader = new BufferedReader(new FileReader(locationsFilename));
			locationsReader.readLine(); // strip off the line of headers
			while (locationsReader.ready()) {
				line = locationsReader.readLine();
				pieces = line.split(",");
				// NB: the LIBRARY and COLLECTION level locations must be listed first
				if (pieces[2].equals("SHELVING")) {
					this.locationCodeToShelvingString.put(pieces[1], pieces[0]);
					if ( libraryCodeToName.get(pieces[3]) != null ) {
						this.locationCodeToLibraryCode.put(pieces[1], pieces[3]);
					} else if ( collectionCodeToName.get(pieces[3]) != null ) {
						this.locationCodeToCollectionCode.put(pieces[1], pieces[3]);
					}
				} else if (pieces[2].equals("LIBRARY")) {
					this.libraryCodeToName.put(pieces[1], pieces[0]);
				} else if (pieces[2].equals("COLLECTION")) {
					this.collectionCodeToName.put(pieces[1], pieces[0]);
					this.collectionCodeToLibraryCode.put(pieces[1], pieces[3]);
				}
			}
			*/
			
			sfx_reader = new BufferedReader(new FileReader(sfxExportFileName));
			line = sfx_reader.readLine();
			headers = line.split("\t");
			List<Map<String, String>> tmpList = new ArrayList<Map<String, String>>();
			Map<String, String> issn_map = null;
			while(sfx_reader.ready() && (limit < 0 || curr++ < limit)) {
				line = sfx_reader.readLine();
				pieces = line.split("\t");
				if ( pieces.length > 0 ) {
					issn_map = new HashMap<String, String>();
					for ( int i = 0; i < pieces.length; i++ ) {
						issn_map.put(headers[i], pieces[i]);
					}
					issn_map.put("line", line);
					if ( pieces[0].length() > 0 ) {
						if ( sfxdata_by_issn.get(pieces[0].trim()) != null ) {
							sfxdata_by_issn.get(pieces[0].trim()).add(issn_map);
						} else {
							tmpList = new ArrayList<Map<String, String>>();
							tmpList.add(issn_map);
							sfxdata_by_issn.put(pieces[0].trim(), tmpList);
						}
					}
					if ( pieces[1].length() > 0 ) {
						if ( sfxdata_by_eissn.get(pieces[0].trim()) != null ) {
							sfxdata_by_eissn.get(pieces[0].trim()).add(issn_map);
						} else {
							tmpList = new ArrayList<Map<String, String>>();
							tmpList.add(issn_map);
							sfxdata_by_eissn.put(pieces[0].trim(), tmpList);
						}						
					}
					if ( pieces[4].length() > 0 ) {
						if ( sfxdata_by_lccn.get(pieces[0].trim()) != null ) {
							sfxdata_by_lccn.get(pieces[0].trim()).add(issn_map);
						} else {
							tmpList = new ArrayList<Map<String, String>>();
							tmpList.add(issn_map);
							sfxdata_by_lccn.put(pieces[0].trim(), tmpList);
						}
					}
					/*
					if ( pieces[0].length() == 0 &&
						 pieces[1].length() == 0 &&
						 pieces[4].length() == 0 ) {
						sfx_no_matching_data++;
					}
					*/
				}
			}
		} catch(Exception e) {
			LU_DBLoadInstances.Log(System.err, "Unable to read in Lehigh data: " + e.getMessage(), LU_DBLoadInstances.LOG_ERROR);
			e.printStackTrace(System.err);
		}
	}
	
	public void stageSirsiFiles(String callNumbersFilename, String shelvingKeysFilename, 
								String boundWithsFileName,  String itemNumbersFilename, 
								String analyticsFilename,   String itemsFilename) {
		this.stageSirsiFiles(callNumbersFilename, shelvingKeysFilename, boundWithsFileName,
	            itemNumbersFilename, analyticsFilename, itemsFilename, -1);
	}
	
	public void stageSirsiFiles(String callNumbersFilename, String shelvingKeysFilename,
			   					String boundWithsFileName, String itemNumbersFilename, 
			   					String analyticsFilename,  String itemsFilename, int limit) {
		BufferedReader callNumbersReader, boundWithsReader, shelvingKeysReader, 
		               itemNumbersReader, analyticsReader, itemsReader;
		try {
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        	LU_DBLoadInstances.Log("Clearing out callnumber and item tables in migration database ...");
			EntityTransaction migration_transaction = LU_DBLoadInstances.migration_em.getTransaction();
			migration_transaction.begin();
			javax.persistence.Query q = LU_DBLoadInstances.migration_em.createNativeQuery("DELETE FROM callnumbers");
			q.executeUpdate();
			q = LU_DBLoadInstances.migration_em.createNativeQuery("DELETE FROM items");
			q.executeUpdate();
			migration_transaction.commit();
			
			File callNumbersFile = new File(callNumbersFilename);
        	callNumbersReader = new BufferedReader(new FileReader(callNumbersFilename));
        	boundWithsReader = new BufferedReader(new FileReader(boundWithsFileName));
        	shelvingKeysReader = new BufferedReader(new FileReader(shelvingKeysFilename));
        	itemNumbersReader = new BufferedReader(new FileReader(itemNumbersFilename));
        	analyticsReader = new BufferedReader(new FileReader(analyticsFilename));
        	itemsReader = new BufferedReader(new FileReader(itemsFilename));
        	LU_DBLoadInstances.Log("Staging call number records in migration database, time is: " + df.format(Calendar.getInstance().getTime()));
        	//String workingdir = "/mnt/bigdrive/bibdata/sirsidump/20131211";
        	String workingdir = callNumbersFile.getParent();
        	PrintWriter writer = new PrintWriter(workingdir + "/testoutput.txt", "UTF-8");
        	int curr = 0, increment = 50000;
        	SirsiCallNumber scn;
        	
        	migration_transaction.begin();
        	while(callNumbersReader.ready() && (limit < 0 || curr < limit)) {
        		// There should be the same number of lines in all 4 files containing callnum data,
        		// and they should all be sorted the same way to line 1 goes with line 1 goes with line 1, etc.
        		// I'm building those files myself, so I can guarantee it, heh
        		// With that in mind, we'll just read lines from all 4 files at once
        		scn = new SirsiCallNumber();
        		String line = callNumbersReader.readLine();
        		String shelvingKeysLine = shelvingKeysReader.readLine();
        		String itemNumbersLine = itemNumbersReader.readLine();
        		writer.println("Item Numbers Line: " + itemNumbersLine);
        		String analyticsLine = analyticsReader.readLine(); 
        		String fields[] = line.split("\\|");
        		// Can't just assign the return value of Arrays.asList(fields) to callNumberFields --
        		// that method returns an immutable, fixed-size list
        		ArrayList<String> callNumberFields = new ArrayList<String>();
        		callNumberFields.addAll(Arrays.asList(fields));
        		
        		scn.setCat_key(Integer.parseInt(fields[0]));
        		scn.setCallnum_key(Integer.parseInt(fields[1]));
        		scn.setAnalytic_pos(Integer.parseInt(fields[2]));
        		scn.setLevel(fields[3]);
        		scn.setNum_copies(Integer.parseInt(fields[4]));
        		scn.setNum_call_holds(Integer.parseInt(fields[5]));
        		scn.setClassification(fields[6]);
        		scn.setNum_reserve_control_recs(Integer.parseInt(fields[7]));
        		scn.setNum_academic_reserves(Integer.parseInt(fields[8]));
        		scn.setLibrary(fields[9]);
        		scn.setNum_visible_copies(Integer.parseInt(fields[10]));
        		scn.setShadowed(fields[11]);
        		
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
        		scn.setShelving_key(tmpStr.trim());
        		
        		// Now we repeat that process for the itemNumbersLine and analyticsLine
        		fields = itemNumbersLine.split("\\|");
        		// I found a few callnumbers which contained 4 fields
        		// in the output from from selcallnum -iS -oKD.  This 
        		// should have only been 3 fields, and it turns out this
        		// was because the item number field, output by -D from selcallnum,
        		// can have | characters in it.  Sirsi's own MARC export of the catalog
        		// didn't include the info after the pipe in the "call number" subfield
        		// of the 999 field for an item -- it went in subfield v.  So, I'm stripping
        		// it off here by only using fields[2] as the callnumber.
        		//tmpfields = Arrays.asList(fields).subList(2, fields.length);
        		tmpfields = Arrays.asList(fields).subList(2, 3);
        		tmpStr = StringUtils.join(tmpfields, "|");
        		scn.setCall_number(tmpStr.trim());

        		fields = analyticsLine.split("\\|");
        		tmpfields = Arrays.asList(fields).subList(2, fields.length);
        		tmpStr = StringUtils.join(tmpfields, "|");
        		scn.setAnalytics(tmpStr.trim());
        		
        		LU_DBLoadInstances.migration_em.persist(scn);
 
        		if ( ++curr % increment == 0 ) {
            		migration_transaction.commit();
					LU_DBLoadInstances.migration_em.clear(); // TODO: testing this to see if it fixes memory problems
        			LU_DBLoadInstances.Log(System.out, "Staged call number " + curr, LU_DBLoadInstances.LOG_INFO);
        			migration_transaction.begin();
        		}
        	}
    		migration_transaction.commit();

        	LU_DBLoadInstances.Log("Staging bound-withs in migration database, time is: " + df.format(Calendar.getInstance().getTime()));
        	curr = 0;
        	int boundwith_increment = 1000;
        	migration_transaction.begin();
        	// boundwiths exported with the command 
        	// selbound -oKPcdy > boundwiths.data
        	// The fields are:
        	// 0 : child record's catalog key
        	// 1 : child record's callnum key
        	// 2 : parent record's catalog key
        	// 3 : parent record's callnum key
        	// 4 : user access rights of creator, values like "CAT", "CATSERIALS", "ACQ"
        	// 5 : create date
        	// 6 : library
        	while ( boundWithsReader.ready() ) {
        		String boundWithsLine = boundWithsReader.readLine();
        		String[] fields = boundWithsLine.split("\\|");
        		if ( fields[0].length() == 0 || fields[1].length() == 0 ) {
                	LU_DBLoadInstances.Log(System.err, "No cat key or call num key for boundwith: " + boundWithsLine,
                						   LU_DBLoadInstances.LOG_WARN);
                	
        		} else {
        			//System.err.println("Selecting, fields[0] = " + fields[0] + ", fields[1] = " + fields[1] + ", ...");
        			SirsiCallNumberID scnid = new SirsiCallNumberID(Integer.parseInt(fields[0]), Integer.parseInt(fields[1]));
        			scn = LU_DBLoadInstances.migration_em.find(SirsiCallNumber.class, scnid);
        			//TypedQuery<SirsiCallNumber> query = LU_DBLoadInstances.migration_em.createQuery("SELECT s FROM SirsiCallNumber s WHERE s.Id.cat_key=" + fields[0] + " and s.Id.callnum_key=" + fields[1], SirsiCallNumber.class);
        			//query.setHint("org.hibernate.cacheable", true);
        			//List<SirsiCallNumber> results = query.getResultList();
        			if ( scn != null ) {
        				//scn = results.get(0);
        				scn.setParent_cat_key(Integer.parseInt(fields[2]));
        				scn.setParent_callnum_key(Integer.parseInt(fields[3]));
        				scn.setCreator_access(fields[4]);
        				scn.setBound_create_date(fields[5]);
        				// "library" is always the same, "LEHIGH", and we already have that in the callnumber
        				// record, so we ignore it.
        				LU_DBLoadInstances.migration_em.persist(scn);
        				//System.err.println("Persisting ...");
                		if ( ++curr % boundwith_increment == 0 ) {
                    		migration_transaction.commit();
        					LU_DBLoadInstances.migration_em.clear(); // TODO: testing this to see if it fixes memory problems
                			LU_DBLoadInstances.Log(System.out, "Staged call number " + curr, LU_DBLoadInstances.LOG_INFO);
                			migration_transaction.begin();
                		}
        			}
        		}	
        	}
			migration_transaction.commit();

        	LU_DBLoadInstances.Log("Staging item records in migration database, time is: " + df.format(Calendar.getInstance().getTime()));
        	curr = 0;
        	increment = 25000;
        	String line = "";
        	//String fields[];
        	List<String> itemNumberFields;
        	//SirsiItem item;
    		migration_transaction.begin();
        	while(itemsReader.ready() && (limit < 0 || curr < limit)) {
        		// Only one file to read from this time
        		line = itemsReader.readLine();
        		SirsiItem item = new SirsiItem();
        		String[] fields = line.split("\\|");
        		        		
        		item.setCat_key(Integer.parseInt(fields[0]));
        		item.setCallnum_key(Integer.parseInt(fields[1]));
        		item.setItem_key(Integer.parseInt(fields[2]));
        		item.setLast_used_date(fields[3]);
        		item.setNum_bills(Integer.parseInt(fields[4]));
        		item.setNum_charges(Integer.parseInt(fields[5]));
        		item.setNum_total_charges(Integer.parseInt(fields[6]));
        		item.setFirst_created_date(fields[7]);
        		item.setNum_holds(Integer.parseInt(fields[8]));
        		item.setHouse_charge(Integer.parseInt(fields[9]));
        		item.setHome_location(fields[10]);
        		item.setCurr_location(fields[11]);
        		item.setLast_changed_date(fields[12]);
        		item.setPermanent(fields[13]);
        		item.setPrice(Integer.parseInt(fields[14]));
        		item.setRes_type(Integer.parseInt(fields[15]));
        		item.setLast_user_key(Integer.parseInt(fields[16]));
        		item.setType(fields[17]);
        		item.setRecirc_flag(fields[18]);
        		item.setInventoried_date(fields[19]);
        		item.setTimes_inventoried(Integer.parseInt(fields[20]));
        		item.setLibrary(fields[21]);
        		item.setHold_key(Integer.parseInt(fields[22]));
        		item.setLast_discharged_date(fields[23]);
        		item.setAccountability(fields[24]);
        		item.setShadowed(fields[25]);
        		item.setDistribution_key(fields[26]);
        		item.setTransit_status(fields[27]);
        		item.setReserve_status(fields[28]);
        		item.setPieces(Integer.parseInt(fields[29]));
        		item.setMedia_desk(fields[30]);
        		// There is only this one item-line that has an empty extra
        		// field in it for some reason.  
        		if ( fields[32].trim().equals("737988-1001") ) {
        			item.setBarcode(fields[32].trim());
        			item.setNum_comments(Integer.parseInt(fields[33]));
        		} else {
        			item.setBarcode(fields[31].trim());
        			item.setNum_comments(Integer.parseInt(fields[32]));
        		}
        	
        		LU_DBLoadInstances.migration_em.persist(item);

        		if ( ++curr % increment == 0 ) {
            		migration_transaction.commit();
					LU_DBLoadInstances.migration_em.clear(); // TODO: testing this to see if it fixes memory problems
        			LU_DBLoadInstances.Log(System.out, "On item number " + curr, LU_DBLoadInstances.LOG_INFO);
        			migration_transaction.begin();
        		}
        		
        	}
        	LU_DBLoadInstances.Log("Done staging callnumbers and items in migration database, time is: " + df.format(Calendar.getInstance().getTime()));

		} catch (Exception e) {
			e.printStackTrace();
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
	// TODO: change this to populate sqlite olemigration tables, rather than hashmaps
	public void readSirsiFiles(String callNumbersFilename, String shelvingKeysFilename,
							   String itemNumbersFilename, String analyticsFilename,
							   String itemsFilename, int limit) {
		BufferedReader callNumbersReader, shelvingKeysReader, itemNumbersReader, analyticsReader, itemsReader;

		try {
			File callNumbersFile = new File(callNumbersFilename);
        	callNumbersReader = new BufferedReader(new FileReader(callNumbersFilename));
        	shelvingKeysReader = new BufferedReader(new FileReader(shelvingKeysFilename));
        	itemNumbersReader = new BufferedReader(new FileReader(itemNumbersFilename));
        	analyticsReader = new BufferedReader(new FileReader(analyticsFilename));
        	itemsReader = new BufferedReader(new FileReader(itemsFilename));
        	LU_DBLoadInstances.Log("Building hashmap of call number records by call number key " + 
        					   		"and by call number (called \"item number\" by Sirsi) ...");
        	//String workingdir = "/mnt/bigdrive/bibdata/sirsidump/20131211";
        	String workingdir = callNumbersFile.getParent();
        	PrintWriter writer = new PrintWriter(workingdir + "/testoutput.txt", "UTF-8");
        	int curr = 0, increment = 100000;

        	/* Not used anymore
        	while(callNumbersReader.ready() && (limit < 0 || curr < limit)) {
        		// There should be the same number of lines in all 4 files containing callnum data,
        		// and they should all be sorted the same way to line 1 goes with line 1 goes with line 1, etc.
        		// I'm building those files myself, so I can guarantee it, heh
        		// With that in mind, we'll just read lines from all 4 files at once
        		String line = callNumbersReader.readLine();
        		String shelvingKeysLine = shelvingKeysReader.readLine();
        		String itemNumbersLine = itemNumbersReader.readLine();
        		writer.println("Item Numbers Line: " + itemNumbersLine);
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
        		// I found a few callnumbers which contained 4 fields
        		// in the output from from selcallnum -iS -oKD.  This 
        		// should have only been 3 fields, and it turns out this
        		// was because the item number field, output by -D from selcallnum,
        		// can have | characters in it.  Sirsi's own MARC export of the catalog
        		// didn't include the info after the pipe in the "call number" subfield
        		// of the 999 field for an item -- it went in subfield v.  So, I'm stripping
        		// it off here by only using fields[2] as the callnumber.
        		//tmpfields = Arrays.asList(fields).subList(2, fields.length);
        		tmpfields = Arrays.asList(fields).subList(2, 3);
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
        		String callnumber = callNumberFields.get(13).trim();

        		LU_DBLoadInstances.Log(writer, "Putting call number into hash, key is " + callnumber, LU_DBLoadInstances.LOG_DEBUG);
        		if ( this.callNumbersByItemNumber.get(callnumber) == null) {
        			List<List<String>> callNumberStrs = new ArrayList<List<String>>();
        			callNumberStrs.add(callNumberFields);
        			callNumbersByItemNumber.put(callnumber, callNumberStrs);        			
        		} else {
        			callNumbersByItemNumber.get(callnumber).add(callNumberFields);
        		}
        		if ( ++curr % increment == 0 ) {
        			LU_DBLoadInstances.Log(System.out, "On call number " + curr, LU_DBLoadInstances.LOG_INFO);
        		}
        	
        	}
        	*/
        	
        	LU_DBLoadInstances.Log("Building hashmap of item records by catalog key and by Item ID ...");
        	curr = 0;
        	String line = "";
        	String fields[];
        	List<String> itemNumberFields;
        	while(itemsReader.ready() && (limit < 0 || curr < limit)) {
        		// Only one file to read from this time
        		line = itemsReader.readLine();
        		fields = line.split("\\|");
        		itemNumberFields = Arrays.asList(fields);
        		// Fill in the hash keyed by catalog key, which should be index 2 in the list
        		/*
        		if ( itemsByCatalogKey.get(itemNumberFields.get(0)) == null ) {
        			List<List<String>> itemStrs = new ArrayList<List<String>>();
        			itemStrs.add(itemNumberFields);
        			itemsByCatalogKey.put(itemNumberFields.get(0), itemStrs);
        		} else {
        			itemsByCatalogKey.get(itemNumberFields.get(0)).add(itemNumberFields);
        		}
        		*/
        		
        		// Now fill in the hash keyed by Item ID, which is index 31
        		String itemID = itemNumberFields.get(31).trim();
        		if ( this.itemsByID.get(itemID) == null ) {
        			List<List<String>> itemStrs = new ArrayList<List<String>>();
        			itemStrs.add(itemNumberFields);
        			itemsByID.put(itemID, itemStrs);        			
        		} else {
        			itemsByID.get(itemID).add(itemNumberFields);
        		}
        		if ( ++curr % increment == 0 ) {
        			LU_DBLoadInstances.Log(System.out, "On item number " + curr, LU_DBLoadInstances.LOG_INFO);
        		}
        		
        	}
        	LU_DBLoadInstances.Log("Done building hashmaps");
		} catch(Exception e) {
			LU_DBLoadInstances.Log(System.err, "Unable to read in call numbers and items: " + e.getMessage(), LU_DBLoadInstances.LOG_ERROR);
			e.printStackTrace(System.err);
		}
	}
	
	
	public static void main(String arguments[]) {
		
		// First read the files from Sirsi into my own classes, which mirror OLE structure, using my own logic
		
		InstanceCollection ic = new InstanceCollection();
		/* Test to make sure that the XML output looks good */
		//testoutput(ic);
		//ReadInstance(ic, "/mnt/bigdrive/bibdata/allcallnums.txt", "/mnt/bigdrive/bibdata/allitems.txt");
		
		// Then marshal those classes to XML and output them
		try {
			JAXBContext context = JAXBContext.newInstance(InstanceCollection.class);
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.marshal(ic, System.out);
		} catch(Exception e) {
			LU_DBLoadInstances.Log(System.err, "Unable to marshall instance collection: " + e.getMessage(), LU_DBLoadInstances.LOG_ERROR);
			e.printStackTrace(System.err);
		}
	}
	
	public static Map<String, List<String>> getSubfields(VariableField field) {
		HashMap<String, List<String>> subfields = new HashMap<String, List<String>>();
		String fieldStr = field.toString();
		// Take the tag off of there
		fieldStr = fieldStr.replaceFirst(field.getTag() + "\\s+", "");
		//System.out.println("After replacement, whole thing is: " + fieldStr);
		// Split on a $ followed by a lower case letter (and once or twice an = sign), 
		// since the price subfield will have $ signs in it,
		// and use the LookBehind feature (that's the ?=) to include the delimiter.
		// We can then make a hashmap out of that keyed by the subfield codes
		String[] subfieldsarray = fieldStr.split("(?=\\$[a-z=])");
		String key = "", value = "";
		for ( String subfield : subfieldsarray ) {
			// After substition and split, between the tag and the first subfield, there are 3 spots
			// for "index" fields that we don't want.  They won't contain a $, though.
			if ( subfield.length() < 2 || !subfield.contains("$") ) {
				continue;
			}
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
	
	public void buildSerialsData(Record record, Bib bib, List<Record> assocMFHDRecords) {
		String bibFormerId = bib.getFormerId();
		TypedQuery<Serial> query = LU_DBLoadInstances.migration_em.createQuery("SELECT s FROM Serial s where s.bibid='" + bibFormerId + "'", Serial.class);
		query.setHint("org.hibernate.cacheable", true);
		List<Serial> results = query.getResultList();
		EntityTransaction ole_tx = LU_DBLoadInstances.ole_em.getTransaction();
		for ( Serial s : results ) {

			LU_DBLoadInstances.Log(System.out, "Creating serials receiving control record in OLE for serial with migration ID " + 
					s.getId() + " and Sirsi bib ID " + s.getBibid(), LU_DBLoadInstances.LOG_INFO);
			for ( OLEHoldings oh : bib.getHoldings() ) {
				//if ( !ole_tx.isActive() ) {
				//	ole_tx.begin();
				//}
				if ( !oh.getHoldingsType().equals("electronic") ) {
					SerialsReceiving sr = new SerialsReceiving(s.getSerialControlId());
					sr.setBibId(bib.getUniqueIdPrefix() + "-" + bib.getId());
					sr.setInstanceId(oh.getUniqueIdPrefix() + "-" + oh.getHoldingsIdentifier());
					//sr.setRecType(s.getCategory1()+":"+s.getCategory2());
					sr.setCreateDate(s.getDateCreated());
					sr.setRecType("Main");
					String notestr = "Publication cycle definition: " + s.getPublicationCycleDefinition();
					if ( s.getSerial_notes() != null && s.getSerial_notes().size() > 0 ) {
						notestr += ", Notes: ";
						for ( SerialNote note : s.getSerial_notes()) { 
							notestr += note.getNote() + " ";
						}
					}
					if ( s.getSerial_names() != null && s.getSerial_names().size() > 0 ) {
						if ( notestr.length() > 0 ) {
							notestr += ", ";
						}
						notestr += ", Names: ";
						for ( SerialName name : s.getSerial_names() ) {
							notestr += name + " ";
						}
					}
					if ( s.getCategory1() != null && s.getCategory1().length() > 0 ) {
						notestr += ", Category 1: " +  s.getCategory1();
					}
					if( s.getCategory2() != null && s.getCategory2().length() > 0 ) {
						notestr += ", Category 2: " + s.getCategory2();
					}
					sr.setGenReceivedNote(notestr.substring(0, Math.min(notestr.length(), 499)));
					if ( s.getSerial_physforms() != null && s.getSerial_physforms().size() > 0 ) {
						notestr = "";
						//notestr += "Physforms: ";
						for ( SerialPhysform form : s.getSerial_physforms() ) {
							notestr += form.getPhysform() + ", ";
						}
						sr.setTreatmentInstrNote(notestr);
					}

					sr.setPublicDisplay("Y");
					sr.setActive("Y");
					sr.setPrintLabel("Y");
					sr.setCreateItem("N");
					//sr.setSubscriptionStatus("4"); // not sure what to put here
					sr.setReceiptLocation(s.getLibrary());
					if ( oh.getFlatLocation() != null ) {
						sr.setUnboundLocation(oh.getFlatLocation().getLocCodeString());
					} else {
						sr.setUnboundLocation("");
					}
					if ( s.getSubscriptionStatus() == null ||
						 s.getSubscriptionStatus().trim() == "" ||
						 subscriptionStatusMap.get(s.getSubscriptionStatus().trim()) == null ) {
						sr.setSubscriptionStatus("0"); // For subscription status "unknown"
					} else {
						sr.setSubscriptionStatus(subscriptionStatusMap.get(s.getSubscriptionStatus().trim()));
					}
					sr.setVendor(s.getLinkedVendorId());

					LU_DBLoadInstances.ole_em.persist(sr);

					SerialsReceivingRecType srtype = new SerialsReceivingRecType();
					srtype.setSerialsReceiving(sr);
					srtype.setRecType("Main");
					//srtype.setRecType(s.getCategory1() + ":" + s.getCategory2());
					srtype.setActionInterval(s.getClaimPeriod());
					if ( s.getNameType().equals("CUSTOM") ) {
						srtype.setChronCaptionLvl1(s.getCustomIssueNames().substring(0, Math.min(s.getCustomIssueNames().length(), 39)));
					} else {
						srtype.setChronCaptionLvl1(s.getNameType().substring(0, Math.min(s.getNameType().length(), 39)));
					}
					srtype.setChronCaptionLvl2(s.getFormSubdiv1());
					srtype.setChronCaptionLvl3(s.getFormSubdiv2());
					srtype.setChronCaptionLvl4(s.getFormSubdiv3());
					srtype.setEnumCaptionLvl1(s.getLabelSubdiv1());
					srtype.setEnumCaptionLvl2(s.getLabelSubdiv2());
					srtype.setEnumCaptionLvl3(s.getLabelSubdiv3());

					/*
				srtype.setChronCaptionLvl2(s.getLabelSubdiv1());
				srtype.setChronCaptionLvl3(s.getLabelSubdiv2());
				srtype.setChronCaptionLvl4(s.getLabelSubdiv3());
				srtype.setEnumCaptionLvl1(s.getFormSubdiv1());
				srtype.setEnumCaptionLvl2(s.getFormSubdiv2());
				srtype.setEnumCaptionLvl3(s.getFormSubdiv3());
					 */
					LU_DBLoadInstances.ole_em.persist(srtype);

					TypedQuery<Issue> query2 = LU_DBLoadInstances.migration_em.createQuery("SELECT i FROM Issue i where i.serialControlId='" + s.getSerialControlId() + "'", Issue.class);
					query.setHint("org.hibernate.cacheable", true);
					List<Issue> results2 = query2.getResultList();
					for ( Issue i : results2 ) {
						// Fill in ole_ser_rcv_his_rec from issue data
						SerialsReceivingHisRec srhr = new SerialsReceivingHisRec(sr);
						// put PRED_NAME into hisrec's CHRON_LVL_1
						srhr.setRecType("Main");
						srhr.setChronLvl1(i.getPredictionName().substring(0, Math.min(i.getPredictionName().length(), 39)));
						srhr.setEnumLvl1(i.getPredictionNumeration().substring(0, Math.min(i.getPredictionNumeration().length(),  39)));
						srhr.setClaimCount(i.getClaimNumber());
						srhr.setClaimDate(i.getClaimDateCreated());
						srhr.setClaimType(i.getClaimReason());

						LU_DBLoadInstances.ole_em.persist(srhr);
					}
					//System.err.println("Committing serials receiving record");
					//ole_tx.commit(); // commit after every serials receiving rec

					// Need to transform that bib id from Sirsi into an OLE bib id
					// docstore SOLR search, maybe?
					// Probably better to look in olemigration database while filling in bib records
					// to see if former id of bib matches bib id of any serials
					// Then fill in serial information ...
				}
			}

		}
		//if ( !ole_tx.isActive() ) {
		//	ole_tx.begin();
		//}
	}
	
	public void buildBibHoldingsData(Record record, Bib bib, List<Record> assocMFHDRecords) {
	    //String catalogKey = record.getVariableField("001").toString().split(" ")[1];
	    String catalogKey = LU_DBLoadInstances.formatCatKey(record.getControlNumber());
		//List<List<String>> callNumberStrings = this.callNumbersByCatalogKey.get(catalogKey);
		//List<List<String>> itemStrings = this.itemsByCatalogKey.get(catalogKey);
		List<VariableField> itemsholdings = record.getVariableFields("999");
		List<VariableField> eholdings = new ArrayList<VariableField>();
		List<VariableField> printholdings = new ArrayList<VariableField>();
		List<Record> onlineMFHDRecords = new ArrayList<Record>();
		List<Record> printMFHDRecords = new ArrayList<Record>();
		//for( List<String> callNumberFields : callNumberStrings ) {
		Map<String, List<String>> subfields;		
		
		for ( VariableField itemholdings : itemsholdings ) {
			subfields = LU_BuildInstance.getSubfields(itemholdings);
			List<String> locs = subfields.get("$l");
			if ( locs == null || locs.size() < 1 ) {
				LU_DBLoadInstances.Log(System.err, "Item with no l subfield: " + record.getControlNumber() + ", " + itemholdings.toString(), LU_DBLoadInstances.LOG_WARN);
			} else {
				if ( locs.get(0).equals(ELECTRONIC_RESOURCE) ) {
					eholdings.add(itemholdings);
				} else {
					printholdings.add(itemholdings);
				}
			}
		}
		
		
		for ( Record MFHDRec : assocMFHDRecords ) {
			VariableField eightfivetwo = getValidEightFiveTwo(MFHDRec);
			String rectype = "";
			if ( eightfivetwo != null ) {
				subfields = LU_BuildInstance.getSubfields(eightfivetwo);
				rectype = subfields.get("$c").get(0);
			} else {
				rectype = "LEHIGH"; // seems like a sensible default, given what's in the data
			}
			if ( rectype.equals(ELECTRONIC_RESOURCE) ) {
				onlineMFHDRecords.add(MFHDRec);
			} else {
				printMFHDRecords.add(MFHDRec);
			}
		}					
		
		if ( eholdings != null && eholdings.size() > 0 ) { 
			buildEHoldingsData(record, bib, eholdings, onlineMFHDRecords);
		}
		if ( printholdings != null && printholdings.size() > 0 ) {
			buildPrintHoldingsData(record, bib, printholdings, printMFHDRecords);
		}
	}
	
	public void buildInstanceCollection(Record record, Bib bib, List<Record> assocMFHDRecords) {

	    String catalogKey = record.getVariableField("001").toString().split(" ")[1];
		//List<List<String>> callNumberStrings = this.callNumbersByCatalogKey.get(catalogKey);
		//List<List<String>> itemStrings = this.itemsByCatalogKey.get(catalogKey);
		List<VariableField> itemsholdings = record.getVariableFields("999");
		List<VariableField> eholdings = new ArrayList<VariableField>();
		List<VariableField> printholdings = new ArrayList<VariableField>();
		List<Record> onlineMFHDRecords = new ArrayList<Record>();
		List<Record> printMFHDRecords = new ArrayList<Record>();
		//for( List<String> callNumberFields : callNumberStrings ) {
		Map<String, List<String>> subfields;		
		
		if ( record.getVariableFields("856").size() > 1 && eInstanceReady ) {
			// multiple 856 fields -- generate e-instances for each one
			// no code to ingest these yet, so don't bother currently
			// TODO: 
			// When code to ingest e-instances exists, change eInstanceReady to true
		}

		if ( assocMFHDRecords.size() > 0 ) {
			for ( Record MFHDrec : assocMFHDRecords ) {
				// Instance inst = new Instance();
				VariableField eightfivetwo = MFHDrec.getVariableField("852");

				String rectype = "";
				if ( eightfivetwo != null ) {
					subfields = this.getSubfields(eightfivetwo);
					if ( subfields.get("$c") != null && subfields.get("$c").size() > 0 ) {
						rectype = subfields.get("$c").get(0);
					} else {
						LU_DBLoadInstances.Log(System.err, "MFHD Record with 852 field and no $c subfield: " + MFHDrec.toString(), 
								LU_DBLoadInstances.LOG_WARN);
						rectype = "LEHIGH"; // seems like a sensible default, given what's in the data
					}
				} else {
					rectype = "LEHIGH"; // seems like a sensible default, given what's in the data
				}
				List<VariableField> mfhd_itemsholdings = new ArrayList<VariableField>();
				// Find 999 records that go with this MFHD rec
				// Search through 999 fields looking for those where the record type matches 
				// this MFHD's record type.  Values should just be "WWW" or "LEHIGH"
				for ( VariableField field : itemsholdings ) {
					subfields  = getSubfields(field);
					// If the MFHD is an electronic resource, then MFHD field 852 subfield c and 999 field
					// subfield l should both be "WWW".  Otherwise, physical resource, and all those
					// go in same instance record
					if ( !rectype.equals(ELECTRONIC_RESOURCE) || 
						  subfields.get("$l").get(0).equals(ELECTRONIC_RESOURCE) ) {
						mfhd_itemsholdings.add(field);
					}
				}
				// TODO: subfields refers to field 852 here, assumed to be a 999 field further down, doesn't make sense ...
				// this.buildHoldingsData(record, inst, subfields, MFHDrec);					

				// TODO: But we only want one oleHoldings object.  Do we need a wholely separate instance for every MFHD-999 field pairing?
				// That's what the code below creates -- separate instances, each with only 1 item, and an oleHoldings object that 
				// will be almost identical
				
				// Now loop over the mfhd_itemsholdings and call buildHoldingsData and buildItemsData, like below
				for ( VariableField field : mfhd_itemsholdings ) {
					//Instance inst = new Instance(LU_DBLoadInstances.formatCatKey(record.getControlNumber()));

					subfields = this.getSubfields(field);			
					this.buildHoldingsData(record, bib, subfields, MFHDrec); // this will be based on the MFHDrec, and will
					// be almost identical for each separate instance created here.  Is that necessary?
					//this.buildItemsData(record, bib, subfields);
					//ic.getInstances().add(inst);					
				}

			}
		} else {
			// No MFHD records, just loop over the 999 fields of the bib record, which represent items
			for ( VariableField field : itemsholdings ) {
				Instance inst = new Instance(LU_DBLoadInstances.formatCatKey(record.getControlNumber()));
				
				subfields = this.getSubfields(field);			
				//List<String> itemnumber = subfields.get("$a"); // I think that's the subfield code, check that
				this.buildHoldingsData(record, bib, subfields, null); 		
				//this.buildItemsData(record, bib, subfields);

				//ic.getInstances().add(inst);
			}
		}
		//ic.setInstance(inst);
	}
	
	public void buildHoldingsNotes(OLEHoldings oh, List<String> commentfields) {
		List<String> nonpublicNoteType = Arrays.asList(".CIRCNOTE.", ".STAFF.");
		List<String> publicNoteType = Arrays.asList(".PUBLIC.");
    	LU_DBLoadInstances.Log(System.out, "Adding " + commentfields.size() + " comments to instance", LU_DBLoadInstances.LOG_DEBUG);
    	for ( String comment : commentfields ) {
    		// Keep the delimiter on the preceding element of the split array
    		OLEHoldingsNote note = new OLEHoldingsNote();
    		String[] pieces = comment.split("(?<=\\. )");
			// People put periods in their comments sometimes, and that's the delimiter Sirsi uses between
			// the comment type and the comment itself, so it blows up the comments array.
			// *sigh*
			// So, we just join all the pieces after element 0 together 
    		String commentstr = StringUtils.join(Arrays.asList(pieces).subList(1, pieces.length));
    		/*
    		if ( pieces.length != 2 ) {
    			LU_DBLoadInstances.Log(System.err, "Badly formatted comment: " + comment, LU_DBLoadInstances.LOG_ERROR);
    			for (String piece : pieces ) {
    				LU_DBLoadInstances.Log(System.err, "Piece: " + piece, LU_DBLoadInstances.LOG_ERROR);
    				System.out.println("Piece: " + piece);
    			}
    			
    		} else {
    			commentstr = pieces[1];
    		} 
    		*/
    		if ( nonpublicNoteType.contains(pieces[0].trim())) {
    			note.setType(nonpublicStr);
    		} else if ( publicNoteType.contains(pieces[0].trim())) {
				note.setType(publicStr);
    		} else {
    			LU_DBLoadInstances.Log(System.err, "Unknown type of comment: " + comment, LU_DBLoadInstances.LOG_WARN);
    		}
    		note.setNote(commentstr);
    		note.setOLEHoldings(oh);
    		oh.getNotes().add(note);
    	}

	}
	
	public boolean startsWithAlphaNumPrefix(String callnum) {
		for ( String prefix : alphaNumCallNumPrefixes ) {
			if ( callnum.startsWith(prefix) ) {
				return true;
			}
		}
		return false;
	}
	
	public void buildCommonHoldingsData(Record rec, Bib bib, VariableField holding, Map<String, List<String>> subfields, Record assocMFHDRec, OLEHoldings oh) {
		String callnumberstr = subfields.get("$a").get(0).trim();
		Map<String, List<String>> tmpsubfields;

		SirsiCallNumber scn = null;
		if ( oh.getHoldingsType().equals("print") ) {
			TypedQuery<SirsiCallNumber> query = LU_DBLoadInstances.migration_em.createQuery("select scn from SirsiCallNumber scn where scn.call_number = :cnstr", SirsiCallNumber.class);
			query.setParameter("cnstr", callnumberstr);
			List<SirsiCallNumber> results = query.getResultList();
			if ( results.size() > 0 ) {
				scn = results.get(0);
				oh.setFormerId(scn.getCat_key() + "|" + scn.getCallnum_key());
			} else {
				LU_DBLoadInstances.Log(System.err, "No call number in migration table exists for call number string: " + callnumberstr,
						LU_DBLoadInstances.LOG_ERROR);				
			}
		}
		/* not used anymore
		List<String> callNumberFields = new ArrayList<String>();
		if ( callNumbersByItemNumber.get(callnumberstr) == null ) {
			LU_DBLoadInstances.Log(System.err, "No call number exists for item: " + subfields.toString(),
					LU_DBLoadInstances.LOG_ERROR);
		} else {
			callNumberFields = this.callNumbersByItemNumber.get(callnumberstr).get(0);			
		}
		if ( subfields.get("$a").size() != 1 ||
			// TODO: not sure how to handle this -- it comes up a lot
				(callNumbersByItemNumber.get(callnumberstr) != null && 
				 callNumbersByItemNumber.get(callnumberstr).size() != 1) ) {
			LU_DBLoadInstances.Log(System.err, "Call number (item number) not unique for item: " + subfields.toString(),
									LU_DBLoadInstances.LOG_DEBUG);
			// TODO: print list of callNumbers for this item number
		}
		*/
		
	   	LU_DBLoadInstances.Log(System.out, "Adding callnumber info to instance holdings data", LU_DBLoadInstances.LOG_DEBUG);
	    CallNumber cn = new CallNumber();
	    // Same as the shelvingscheme for now
	    String cntype = subfields.get("$w").get(0);
	    				    
	    //cn.setPrefix(subfields.get("$a").get(0));
	    //cn.setNumber(subfields.get("$i").get(0));
	    cn.setPrefix("");
	    cn.setNumber(subfields.get("$a").get(0), oh.getFlatLocation());

	    
	    // Not used: callNumberType, callNumberPrefix, itemPart,
	    // They make reference to MFHD 852 codes i, h, and k
	    // Those don't appear to be in our data anywhere
	    // TODO: run that by Doreen, et al ^^^
	    // Also not used currently: shelving scheme
		String cntypecode = callNumberTypeCodes.get(cntype);
		String cntypename = callNumberTypeNames.get(cntype);
		if ( cntypecode == null || cntypecode.length() == 0) {
		   	LU_DBLoadInstances.Log(System.out, "Unrecognized cntype: " + cntype + ", unable to set cntypecode", LU_DBLoadInstances.LOG_WARN);
			cntypecode = "N/A";
		}
		if ( cntypename == null || cntypename.length() == 0) {
			LU_DBLoadInstances.Log(System.out, "Unrecognized cntype: " + cntype + ", unable to set cntypename", LU_DBLoadInstances.LOG_WARN);
			cntypename = "N/A";
		}
		if ( alphaNumCallNums.contains(cn.getNumber()) ||
			 ( oh.getFlatLocation().getLocCodeString().startsWith("LEHIGH/LIND/SPCOLL") &&
			   startsWithAlphaNumPrefix(cn.getNumber()) ) ) {
			cntypecode = "OTHER";
			cntypename = "Other schema";
		}
		oh.setCallNumberType(cntypecode, cntypename);

		// It should be a "normalized" version of the call number, with
	    // spaces and other stuff removed
	    ShelvingOrder shelvingOrder = new ShelvingOrder();
	    shelvingOrder.setShelvingOrder(LU_BuildInstance.normalizeCallNumber(cn.getNumber(), cntypecode));
	    cn.setShelvingOrder(shelvingOrder);

	    oh.setCallNumber(cn);
	    
		oh.setStaffOnly(bib.getStaffOnly());
	    List<String> commentfields = subfields.get("$o"); // TODO: figure out the split and regex, test this
	    if ( commentfields != null && commentfields.size() > 0 ) {
	    	buildHoldingsNotes(oh, commentfields);
	    }
	    
		if ( assocMFHDRec != null ) {
			VariableField eightsixsix = assocMFHDRec.getVariableField("866");
			if ( eightsixsix != null ) {
			    ExtentOfOwnership extentOfOwnership = new ExtentOfOwnership();
				extentOfOwnership.setType("public", "public");
				String ownershipstr = "";

		    	LU_DBLoadInstances.Log(System.out, "Adding extent of ownership info to instance holdings data", LU_DBLoadInstances.LOG_DEBUG);				
				tmpsubfields = this.getSubfields(eightsixsix);
				LU_DBLoadInstances.Log(System.out, "Subfields of 866 for MFHD record: ", LU_DBLoadInstances.LOG_DEBUG);
				for ( String key : tmpsubfields.keySet() ) {
					List<String> fields = tmpsubfields.get(key);
					for ( String value : fields ) {
						LU_DBLoadInstances.Log(System.out, "	" + key + ": " + value, LU_DBLoadInstances.LOG_DEBUG);
					}
				}			
				// Should only be one 866 field with one "$a" subfield
				if ( tmpsubfields.get("$a") != null ) {
					ownershipstr = tmpsubfields.get("$a").get(0);
				}
				if ( tmpsubfields.get("$z") != null ) {
					ExtentOfOwnershipNote n = new ExtentOfOwnershipNote();
					n.setNote(tmpsubfields.get("$z").get(0));
					if ( ownershipstr.length() == 0 ) {
						ownershipstr = n.getNote();
					}
					n.setType("public");
					n.setExtentOfOwnership(extentOfOwnership);
					extentOfOwnership.getNotes().add(n);
					
				}
				extentOfOwnership.setTextualHoldings(ownershipstr);
				VariableField pattern = assocMFHDRec.getVariableField("853");
				List<VariableField> issues = assocMFHDRec.getVariableFields("863");
				if ( pattern != null && issues != null && issues.size() > 0) {
					String notestr = "";
					if ( pattern != null ) {
						notestr = "Pattern: ";
						tmpsubfields = this.getSubfields(pattern);
						ExtentOfOwnershipNote n = new ExtentOfOwnershipNote();
						n.setType(nonpublicStr);
						java.util.Iterator iter = tmpsubfields.keySet().iterator();
						while ( iter.hasNext() ) {
							String subfield = (String) iter.next();
							notestr += subfield + ": " + tmpsubfields.get(subfield);
							if ( iter.hasNext() ) {
								notestr += ", ";
							}
						}
						n.setNote(notestr);
						n.setExtentOfOwnership(extentOfOwnership);
						extentOfOwnership.getNotes().add(n);
					}
					if ( issues != null && issues.size() > 0 ) {
						for ( VariableField issue : issues ) {
							notestr = "";
							tmpsubfields = this.getSubfields(issue);
							ExtentOfOwnershipNote n = new ExtentOfOwnershipNote();
							n.setType(nonpublicStr);
							java.util.Iterator iter = tmpsubfields.keySet().iterator();
							while ( iter.hasNext() ) {
								String subfield = (String) iter.next();
								notestr += subfield + ": " + tmpsubfields.get(subfield);
								if ( iter.hasNext() ) {
									notestr += ", ";
								}
							}
							n.setNote(notestr);
							n.setExtentOfOwnership(extentOfOwnership);
							extentOfOwnership.getNotes().add(n);							
						}
					}
				}
				extentOfOwnership.setOLEHoldings(oh);
				oh.getExtentOfOwnership().add(extentOfOwnership);
				
				// Not ready yet ...
				//oh.setCoverage(parseCoverage(ownershipstr));
				
				// Here we add the ownership string, and its note if
				// there is one, to the holdings record's regular notes.
				// This won't be necessary later, once stuff tied to
				// ole_ds_ext_ownership_t shows up in the UI somewhere.
				OLEHoldingsNote n2 = new OLEHoldingsNote();
				n2.setNote("Extent of ownership: " + ownershipstr);
				n2.setType("public");
				n2.setOLEHoldings(oh);
				oh.getNotes().add(n2);				
				if ( extentOfOwnership.getNotes().size() > 0 ) {
					n2 = new OLEHoldingsNote();
					n2.setNote("Ownership note: " + extentOfOwnership.getNotes().get(0).getNote());
					n2.setType("public");
					n2.setOLEHoldings(oh);
					oh.getNotes().add(n2);
				}
				
			}
			
			// TODO: receptStatus comes from the associated holdings record's
			// 008 field, position 6 (counting from 0 or 1, not sure, probably 1 given context)
		    // There will need to be separate instances for each MFHD record, probably -- should
		    // be either 1 or 2 MFHD records, if any, one for electronic version and one for physical
			String receiptStatus = assocMFHDRec.getVariableField("008").toString().substring(6, 7);
			oh.setReceiptStatus(receiptStatus, receiptStatus);
		} else {
			// TODO: no holdings record, where does the extent of ownership and receipt status come from?
		}
		
	}
	
	public List<Coverage> parseCoverage(String ownershipstr) {
		List<Coverage> coverage = new ArrayList<Coverage>();
		Tokenizer tokenizer = new Tokenizer();
		tokenizer.setStr(ownershipstr);
		String token = "";
		do {
			token = tokenizer.nextToken();
			// Now ... do something with the token according to rules of production
			// that I haven't written yet ...
		} while(tokenizer.getPos() < tokenizer.getStr().length()); 				
		return coverage;
	}
	
	public void buildEHoldingsData(Record record, Bib bib, List<VariableField> eholdings, List<Record> onlineMFHDRecords) {
		Record MFHDRec = null;
		if ( onlineMFHDRecords != null && onlineMFHDRecords.size() > 0 ) {
			MFHDRec = onlineMFHDRecords.get(0); // there's only ever 1 of these per bib record in our data, 
												// as evidenced by test2.java's checkHoldingsRecords method
		}
		Map<String, List<String>> subfields;
		Map<String, List<String>> tmpsubfields;
		List<String> callNumberFields;
		List<Map<String, String>> sfxdata;
		VariableField catalog_issns = record.getVariableField("022");
		VariableField catalog_lccns = record.getVariableField("010");
		
		String callnumberstr;
		String purlpattern = "^http://purl.*";
		List<OLEHoldings> holdings = new ArrayList<OLEHoldings>();
		List<VariableField> eightfivesixes = record.getVariableFields("856");
		FlatLocation electronic_location = new FlatLocation();
		electronic_location.setLevel("Insitution/Library");
		electronic_location.setLocCodeString("LEHIGH/ELECTRONIC");
		for ( VariableField eightfivesix : eightfivesixes ) {
			OLEHoldings oh = new OLEHoldings();
			oh.setHoldingsType("electronic");
			oh.setFlatLocation(electronic_location);
			tmpsubfields = LU_BuildInstance.getSubfields(eightfivesix);
			String note = "", uriStr = "";
    		if ( tmpsubfields.get("$z") != null ) {
    			note = tmpsubfields.get("$z").get(0);
    		}
    		// TODO: look in the "$3" subfield for notes, too
    		// Often $z and $3 are specified.  Cat them together, I guess.
			if ( tmpsubfields.get("$u") != null ) {
	    		AccessURI uri = new AccessURI();
	    		uriStr = tmpsubfields.get("$u").get(0).trim();
	    		if ( uriStr.matches(purlpattern) ) {
	    			oh.setLocalPersistentURI(uriStr);
	    		}
	    		uri.setUri(uriStr);
	    		uri.setText(note + ": " + uriStr);
	    		uri.setOleHoldings(oh);
    			oh.getAccessURIs().add(uri);
    		} else if ( tmpsubfields.get("$a") != null ) {
	    		AccessURI uri = new AccessURI();
	    		uriStr = tmpsubfields.get("$a").get(0).trim();
	    		if ( uriStr.matches(purlpattern) ) {
	    			oh.setLocalPersistentURI(uriStr);
	    		}
	    		uri.setUri(uriStr);
	    		uri.setText(note + ": " + uriStr);
	    		uri.setOleHoldings(oh);
    			oh.getAccessURIs().add(uri);			    			
    		}
    		
			// Now there should only be 1 URI per holdings record
			
			sfxdata = findSFXData(catalog_issns, sfxdata_by_issn); 
			if (  sfxdata == null ) {
				//System.out.println("No SFX data by ISSN for catalog record " + xmlrecord.getControlNumber() + ", trying by eISSN ...");
				sfxdata = findSFXData(catalog_issns, sfxdata_by_eissn);
				if ( sfxdata == null ) {
					//System.out.println("No SFX data by eISSN for catalog record " + xmlrecord.getControlNumber() + ", trying by LCCN ...");
					sfxdata = findSFXData(catalog_lccns, sfxdata_by_lccn);
					if (  sfxdata == null ) {
						LU_DBLoadInstances.Log(System.out, "No SFX data by LCCN  for catalog record " + record.getControlNumber(),
								               LU_DBLoadInstances.LOG_DEBUG);	
						//no_sfx_data++;
					} else {
						LU_DBLoadInstances.Log(System.out, "SFX data found by LCCN for catalog record " + record.getControlNumber(),
					               LU_DBLoadInstances.LOG_DEBUG);
						oh.setCoverage(buildCoverages(sfxdata, oh));
						//has_sfx_data++;
					}
				} else {
					LU_DBLoadInstances.Log(System.out, "SFX data found by eISSN for catalog record " + record.getControlNumber(),
				               LU_DBLoadInstances.LOG_DEBUG);
					oh.setCoverage(buildCoverages(sfxdata, oh));
					//has_sfx_data++;
				}
			} else {
				LU_DBLoadInstances.Log(System.out, "SFX data found by ISSN for catalog record " + record.getControlNumber(),
			               LU_DBLoadInstances.LOG_DEBUG);				
				oh.setCoverage(buildCoverages(sfxdata, oh));
				//has_sfx_data++;
			}
			
			
			// This also shouldn't be necessary once stuff from
			// ole_ds_holdings_access_uri_t shows up in the
			// user interface somewhere
			/*
			if ( oh.getAccessURIs() != null && oh.getAccessURIs().size() > 0 ) {
				oh.setLink(oh.getAccessURIs().get(0).getUri());
				oh.setLinkText(oh.getAccessURIs().get(0).getText());
			}
			*/
			
			/*
			 * Librarians asked that we put the call number from 999 $a and type from 999 $w
			 * into the holdings call number fields for electronic records, too
			 * 
			String domainstr = "";
			try {
				domainstr = this.getDomain(oh.getAccessURIs().get(0).getUri());
			} catch (Exception e) {
				LU_DBLoadInstances.Log(System.err, "Unable to get a URI from record's 856: " + record.toString(),
			               LU_DBLoadInstances.LOG_WARN);
				domainstr = "N/A";
			}
			
			
			 
			CallNumber cn = new CallNumber();
			cn.setNumber("E-Resource: " + domainstr);
			
			String cntypecode = callNumberTypeCodes.get(ELECTRONIC_RESOURCE);
			String cntypename = callNumberTypeNames.get(ELECTRONIC_RESOURCE);
			oh.setCallNumberType(cntypecode, cntypename);
			oh.setCallNumber(cn);
			*/
			
			oh.setBib(bib);
			bib.getHoldings().add(oh);
			holdings.add(oh);
			
		}
		
		for ( OLEHoldings oh : holdings ) {
			for ( VariableField eholding : eholdings ) {
				subfields = LU_BuildInstance.getSubfields(eholding);
				buildCommonHoldingsData(record, bib, eholding, subfields, MFHDRec, oh);
			}
		}
	}
	
	public static String getDomain(String uriStr) {
		String domainstr = "";
		try {
			java.net.URI uri = new java.net.URI(uriStr);
			domainstr = uri.getHost();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			LU_DBLoadInstances.Log(System.err, "Unable to parse domain from URI: " + uriStr,
		               LU_DBLoadInstances.LOG_INFO);
			//e.printStackTrace();
			domainstr = "";
		}
		return domainstr;
	}
	
	public static List<Coverage> buildCoverages(List<Map<String, String>> sfxdata_list, OLEHoldings oh) {
		List<Coverage> coverages = new ArrayList<Coverage>();
		Coverage coverage;
		String datestr = "", token = "";
		Tokenizer tokenizer = new Tokenizer();
		tokenizer.setBreakchars(Arrays.asList(new String[]{"\"", "'", ",", "(", ")", " "}));

		
		String uriStr = oh.getAccessURIs().get(0).getUri();
		String holdings_uri_domain = getDomain(uriStr);
		String sfx_uri_domain = "";
		// Now look in the list to see if any of the elements have a URL with domain that matches
		// If so, add them to the new list to use for determining coverage
		// If not, just use the original list
		List<Map<String, String>> sfxdata_list_mod = new ArrayList<Map<String, String>>();
		for ( Map<String, String> sfxdata : sfxdata_list ) {
			uriStr = sfxdata.get("PARSE_PARAM");
			if ( uriStr != null && uriStr.length() > 0 && uriStr.indexOf("http:") > 0) {
				uriStr = uriStr.substring(uriStr.indexOf("http:"));
				if ( uriStr.indexOf(" & ") > 0 ) {
					uriStr = uriStr.substring(0, uriStr.indexOf(" & "));
				}
				uriStr = uriStr.trim();
				sfx_uri_domain = getDomain(uriStr);
				if ( sfx_uri_domain.equals(holdings_uri_domain) ) {
					sfxdata_list_mod.add(sfxdata);
					LU_DBLoadInstances.Log(System.out, "URI found in SFX data: " + uriStr, 
					           LU_DBLoadInstances.LOG_INFO);
				}
			}
		}
		// No URIs matched from the SFX data passed in, just use the whole list for coverage
		if ( sfxdata_list_mod.size() == 0 ) {
			sfxdata_list_mod = sfxdata_list;
			LU_DBLoadInstances.Log(System.out, "No match for URI found in SFX data: " + oh.getAccessURIs().get(0).getUri(), 
			           LU_DBLoadInstances.LOG_INFO);
		}
		for ( Map<String, String> sfxdata : sfxdata_list_mod ) {
			datestr = sfxdata.get("THRESHOLD_GLOBAL");
			datestr = datestr.replaceAll("\\$obj->", "");
			// datestr should now be of the form parseDate('>=','2005','27','10') && parsedDate('<=','2009','33','1')
			// or parsedDate('>=',1979,1,4)
			// or parsedDate(">=",2001,48,1) && timediff('>=','1y')
			// etc.
			if ( datestr != null && datestr.length() > 0) {
				LU_DBLoadInstances.Log(System.out, "Datestr from THRESHOLD_GLOBAL is: " + datestr, 
						           LU_DBLoadInstances.LOG_DEBUG);
				tokenizer.setStr(datestr);
				coverage = new Coverage();
				token = tokenizer.nextToken();
				if ( token.equals("parsedDate") ) {
					parseDate(tokenizer, coverage, "start");
				}
				token = tokenizer.nextToken(); // should be a space or end of the line
				token = tokenizer.nextToken();
				if ( token.equals("&&") ) {
					token = tokenizer.nextToken(); // should be a space
					token = tokenizer.nextToken(); // should be the beginning of a new expression
					if ( token.equals("parsedDate") ) {
						parseDate(tokenizer, coverage, "end");
					} else if ( token.equals("timediff") ) {
						parseTimeDiff(tokenizer, coverage);
					}
				}
				coverages.add(coverage);
			}
			datestr = sfxdata.get("THRESHOLD_ACTIVE");
			datestr = datestr.replaceAll("\\$obj->", "");
			if ( datestr != null && datestr.length() > 0 ) {
				LU_DBLoadInstances.Log(System.out, "Datestr from THRESHOLD_ACTIVE is: " + datestr, 
				           LU_DBLoadInstances.LOG_DEBUG);
				tokenizer.setStr(datestr);
				coverage = new Coverage();
				token = tokenizer.nextToken();
				if ( token.equals("parsedDate") ) {
					parseDate(tokenizer, coverage, "start");
				}
				token = tokenizer.nextToken(); // should be a space or end of the line
				token = tokenizer.nextToken();
				if ( token.equals("&&") ) {
					token = tokenizer.nextToken(); // should be a space
					token = tokenizer.nextToken(); // should be the beginning of a new expression
					if ( token.equals("parsedDate") ) {
						parseDate(tokenizer, coverage, "end");
					} else if ( token.equals("timediff") ) {
						parseTimeDiff(tokenizer, coverage);
					}
				}
				coverages.add(coverage);
			}				
		}
		return coverages;
	}
	
	// Numbers can be inside single or double quotes
	public static String parseNumber(Tokenizer tokenizer) {
		String numstr = "";
		String token = tokenizer.nextToken();
		if ( Tokenizer.isNumber(token) ) {
			numstr = token;
		} else if ( token.equals("'") || token.equals("\"") ) {
			token = tokenizer.nextToken();
			numstr = token;
			token = tokenizer.nextToken(); // should be closing "'" or "\""
		}
		return numstr;
	}
	
	public static void parseDate(Tokenizer tokenizer, Coverage coverage, String startend) {
		String token = tokenizer.nextToken(); // should be "("
		token = tokenizer.nextToken(); // should be "'" or "\""
		token = tokenizer.nextToken();
		String range  = token;
		String numstr = "";
		if ( startend.equals("start") ) {
			// range should be >= or ==

				token = tokenizer.nextToken(); // should be "'" or "\""
				token = tokenizer.nextToken(); // should be ","
				numstr = parseNumber(tokenizer);
				if ( Tokenizer.isNumber(numstr) ) {
					coverage.setStartDate(numstr);
				}
				token = tokenizer.nextToken(); // should be ","
				numstr = parseNumber(tokenizer);
				if ( Tokenizer.isNumber(numstr) ) {
					coverage.setStartVolume(numstr);
				}
				token = tokenizer.nextToken(); // should be ","
				numstr = parseNumber(tokenizer); // should be start issue, if there is one
				if ( Tokenizer.isNumber(numstr) ) {
					coverage.setStartIssue(numstr);
				}
				token = tokenizer.nextToken(); // should be ")"
				if ( range.equals("==") ) {
					// Then just set the end date/volume/issue equal to the start/volume/issue
					coverage.setEndDate(coverage.getStartDate());
					coverage.setEndVolume(coverage.getEndVolume());
					coverage.setEndIssue(coverage.getStartIssue());
				} 
		} else {
			// range should be <=
			token = tokenizer.nextToken(); // should be "'" or "\""
			token = tokenizer.nextToken(); // should be ","
			numstr = parseNumber(tokenizer);
			if ( Tokenizer.isNumber(numstr) ) {
				coverage.setEndDate(numstr);
			}
			token = tokenizer.nextToken(); // should be ","
			numstr = parseNumber(tokenizer);
			if ( Tokenizer.isNumber(numstr) ) {
				coverage.setEndVolume(numstr);
			}
			token = tokenizer.nextToken(); // should be ","
			numstr = parseNumber(tokenizer); // should be start issue, if there is one
			if ( Tokenizer.isNumber(numstr) ) {
				coverage.setEndIssue(numstr);
			}
			token = tokenizer.nextToken(); // should be ")"
		}
	}
	
	public static void parseTimeDiff(Tokenizer tokenizer, Coverage coverage) {
		String token = tokenizer.nextToken(); // should be "("
		token = tokenizer.nextToken(); // should be "'" or "\""
		token = tokenizer.nextToken();
		String range  = token;
		String years = "", months = "";
		Pattern yearmonthpat = Pattern.compile("(\\d+)y(\\d+)m");
		Pattern yearpat = Pattern.compile("(\\d+)y");
		Matcher m = yearmonthpat.matcher(range);
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		String[] acceptedFormats = {"yyyyMMdd", "yyyy", "yyyy-MM-dd"};

		if ( coverage.getStartDate() != null ) { // if there's no start date, then a timediff doesn't make sense
			Date date;
			try {
				date = DateUtils.parseDate(coverage.getStartDate(), acceptedFormats);
				if ( m.matches() ) {
					years = m.group(0);
					months = m.group(1);
					date = DateUtils.addYears(date, Integer.parseInt(years));
					date = DateUtils.addMonths(date, Integer.parseInt(months));
				} else {
					m = yearpat.matcher(range);
					if ( m.matches() ) {
						years = m.group(0);
						date = DateUtils.addYears(date, Integer.parseInt(years));
					}
				}
				coverage.setEndDate(df.format(date));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
	
	public static List<Map<String, String>> findSFXData(VariableField catalog_data, Map<String, List<Map<String, String>>> sfxdata_map) {
		List<Map<String, String>> sfxdata = null;
		if ( catalog_data != null ) {
			Map<String, List<String>> tmpsubfields = LU_BuildInstance.getSubfields(catalog_data);
			for ( String key : tmpsubfields.keySet() ) {
				List<String> values = tmpsubfields.get(key);
				for ( String value : values ) {
				//	System.out.println("Searching by value " + value.trim());
					sfxdata = sfxdata_map.get(value.trim()); 
					if (  sfxdata != null ) {
						return sfxdata;
					} 
				}
			}
		} else {
			//System.out.println("No catalog data apparently, not actually searching");
		}
		return sfxdata;
	}
	
	public void buildPrintHoldingsData(Record record, Bib bib, List<VariableField> printholdings, List<Record> printMFHDRecords) {
		// Lots of the same stuff here, but print holdings get items attached to them,
		// while electronic ones don't
		Map<String, List<String>> subfields;
		Map<String, List<String>> tmpsubfields;
		List<String> callNumberFields;
		String callnumberstr;
		List<String> nonpublicNoteType = Arrays.asList(".CIRCNOTE.", ".STAFF.");
		List<String> publicNoteType = Arrays.asList(".PUBLIC.");
		Map<Record, List<VariableField>> MFHD_to_printholdings = new HashMap<Record, List<VariableField>>();
		if ( printMFHDRecords != null && printMFHDRecords.size() > 0 ) {
			for ( Record MFHDRec : printMFHDRecords ) {
				// There can be many print MFHDRecords for a Bib, each of which 
				// will have some or all of the printholdings assigned to it

				// TODO: How to tell which printholdings goes with which printMHFDRec?
				// Use the 852 $c subfield -- the type should help.  Compare to 
				// 999 field's $t subfield.  See a12120.
				// One of the 999's is MICROFORM, has MFORM in a holdings rec's 852 $c
				
				// Look in 853's "$a" subfield, compare to "$l" of 999, maybe?  LMC in both for a8032
				
				// TODO: what should happen here is a loop over the print holdings, adding to a list for each
				// MFHDRec, then add that list to the MFHD_to_printholdings map for the MFHDRec
				// But I have no way of telling which printholdings (999 fields from the Bib record) go with
				// which MFHDRecords
				// Lacking a way to map these, I'm just going to associated every printholdings with every print MFHDRec
				
				MFHD_to_printholdings.put(MFHDRec, printholdings);

			}
			
			// TODO: rather than one holdings per 999 field, use the Schema described by Lisa:
			for ( Record MFHDRec : MFHD_to_printholdings.keySet() ) {
				List<VariableField> assocPrintHoldings = MFHD_to_printholdings.get(MFHDRec);
				for ( VariableField printholding : assocPrintHoldings ) {
					OLEHoldings oh = new OLEHoldings();
					oh.setHoldingsType("print");
					
					subfields = LU_BuildInstance.getSubfields(printholding);
					
					String locStr = subfields.get("$l").get(0);
					oh.setFlatLocation(this.getFlatLocation(locStr));
					
					// Callnumber and type setting code used to be here, moved into 
					// buildCommonHoldingsData since e-holdings will use the same info now
				    
					this.buildCommonHoldingsData(record, bib, printholding, subfields, MFHDRec, oh);
					// print holdings get a location and "access location"

					
					// changed on 2014-04-23 -- ccc2
					//oh.setLocationStr(getLocationName(locStr));
					//oh.setLocationLevelStr("SHELVING");
					
					oh.setBib(bib);
					bib.getHoldings().add(oh);
					
					// print holdings also get an item record, unlike electronic ones
					this.buildItemsData(record, bib, oh, subfields);
				}
			}
		} else {
			// No MFHD records, just loop over the printholdings passed in
			for ( VariableField printholding : printholdings ) {
				OLEHoldings oh = new OLEHoldings();
				oh.setHoldingsType("print");
				subfields = LU_BuildInstance.getSubfields(printholding);
				
				// print holdings get a location and "access location"
				String locStr = subfields.get("$l").get(0);
				oh.setFlatLocation(this.getFlatLocation(locStr));
				//oh.setLocationStr(getLocationName(locStr));
				//oh.setLocationLevelStr("SHELVING");
				
				// Callnumber and type setting code used to be here, moved into
			    // buildCommonHoldingsData since electronic holdings now use the
			    // same info

			    // Not used: callNumberType, callNumberPrefix, itemPart,
			    // They make reference to MFHD 852 codes i, h, and k
			    // Those don't appear to be in our data anywhere
			    // TODO: run that by Doreen, et al ^^^
			    // Also not used currently: shelving scheme
			   			    
				this.buildCommonHoldingsData(record, bib, printholding, subfields, null, oh);


				oh.setBib(bib);
				bib.getHoldings().add(oh);
				
				// print holdings also get an item record, unlike electronic ones
				this.buildItemsData(record, bib, oh, subfields);
			}
		}
	}
	
	public static VariableField getValidEightFiveTwo(Record MFHDRec) {
		VariableField eightfivetwo = null;
		Map<String, List<String>> subfields;
		List<VariableField> eightfivetwos = MFHDRec.getVariableFields("852"); // Usually there's only one of these, but
		// in a few cases there are multiple of them.  We only want the one that has a $c subfield.
		for ( VariableField curr : eightfivetwos ) {
			subfields = LU_BuildInstance.getSubfields(curr);
			if ( subfields.get("$c") != null && subfields.get("$c").size() > 0 ) {
				return curr;
			} else {
				LU_DBLoadInstances.Log(System.err, "MFHD Record with 852 field and no $c subfield: " + MFHDRec.toString(), 
						LU_DBLoadInstances.LOG_WARN);
			}
		}
		return null;
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
	public void buildItemsData(Record record, Bib bib, OLEHoldings holdings, Map<String, List<String>> subfields) {
		if ( holdings.getItems() == null ) {
			// Constructor for Items class will initialize the ArrayList
			//inst.setItems(new Items());
			holdings.setItems(new ArrayList<Item>());
		}
		
		String locStr;
	    Item item = new Item();
	    item.setUniqueIdPrefix("wio");
	    item.setClaimsReturnedFlag("N"); // Apparently there has to be a value here or the docstore
	    // REST API gives a NullPointerException ...
	    
	    item.setCallNumber(holdings.getCallNumber());
	    item.setCallNumberType(holdings.getCallNumberType());
	    
	    // There should only be one subfield "i", as it's the item's barcode and should be unique
	    // And there should be only 1 item with that item ID, so we can just get the first
	    // element of each of those lists
	    String itemID = subfields.get("$i").get(0).trim();
	    if ( itemID.equals("$737988-1001") ) { // found this extra $ in one item, it's a typo -- it's not in the output from selitem
	    	itemID = itemID.substring(1); // chop the $ off of there
	    }
		
	    //TypedQuery<SirsiItem> query = LU_DBLoadInstances.migration_em.createQuery("SELECT i FROM SirsiItem i WHERE i.barcode='" + itemID + "'", SirsiItem.class);
		//query.setHint("org.hibernate.cacheable", true);
		//List<SirsiItem> results = query.getResultList();
		SirsiItem sirsiItem = null;
		sirsiItem = LU_DBLoadInstances.migration_em.find(SirsiItem.class, itemID);
		//if ( results.size() > 0 ) {
		//	sirsiItem = results.get(0);
		//} else {
		if ( sirsiItem == null ) {
	    	LU_DBLoadInstances.Log(System.err, "No item in migration database for ID " + itemID + ", record: " + record.toString(), LU_DBLoadInstances.LOG_ERROR);
		}
		if ( (subfields.get("$i").size() != 1) ) {
				//|| results.size() > 1 ) {
			 //( itemsByID.get(itemID) != null && 
			 //  itemsByID.get(itemID).size() != 1 ) ) {
			LU_DBLoadInstances.Log(System.err, "Bar code number (item ID) not unique for item: " + subfields.toString(), 
									LU_DBLoadInstances.LOG_ERROR);			
		}
		LU_DBLoadInstances.Log(System.out, "Looking for item !" + itemID + "!", LU_DBLoadInstances.LOG_DEBUG);
		LU_DBLoadInstances.Log(System.out, "Subfields: ", LU_DBLoadInstances.LOG_DEBUG);
		for ( String key : subfields.keySet() ) {
			List<String> fields = subfields.get(key);
			for ( String value : fields ) {
				LU_DBLoadInstances.Log(System.out, "	" + key + ": " + value, LU_DBLoadInstances.LOG_DEBUG);
			}
		}
		/*
		List<String> itemString = null;
	    if ( this.itemsByID.get(itemID) == null || itemsByID.get(itemID).size() == 0 ) {
	    	LU_DBLoadInstances.Log(System.err, "No item in itemsByID map for ID " + itemID + ", record: " + record.toString(), LU_DBLoadInstances.LOG_ERROR);
	    	itemString = new ArrayList<String>();
	    } else {
	    	itemString = this.itemsByID.get(itemID).get(0);
	    }
		*/
	    
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
		
		List<FormerIdentifier> fids = new ArrayList<FormerIdentifier>();
		FormerIdentifier fi = new FormerIdentifier();
		Identifier id = new Identifier();
		if ( sirsiItem != null ) {
			id.setIdentifierValue(sirsiItem.getCat_key() + "|" + sirsiItem.getCallnum_key() + "|" + sirsiItem.getItem_key());
			locStr = sirsiItem.getCurr_location();
			if ( removedLocations.contains(locStr) ) {
				locStr = subfields.get("$l").get(0);
			}
		} else {
			id.setIdentifierValue("N/A");
			// Inherit location from home location
			locStr = subfields.get("$l").get(0);
		}
		/*
		if ( itemString.size() >= 3 ) {
			id.setIdentifierValue(itemString.get(0) + "|" + itemString.get(1) + "|" + itemString.get(2));
		} else {
			id.setIdentifierValue("N/A");
		}
		*/
		//id.setSource("SIRSI_ITEMKEY");
		fi.setIdentifierType("SIRSI_ITEMKEY");
		fi.setIdentifier(id);
		fi.setItem(item);
		fids.add(fi);
		item.setFormerIdentifiers(fids);
		
		ItemType type;
		String itemtypestr = subfields.get("$t").get(0);
		item.setItemType(itemtypestr, itemtypestr);
		
		// 	Commenting out setting the itemType's codeValue, since OLE's bulk ingest
		// choked on the field
		//type.setCodeValue(subfields.get("$t").get(0)); // should be only one of these
		// Don't worry about the typeOrSource of the itemType, not sure what that would be
		
		// should also only be one of these
		item.setCopyNumber(subfields.get("$c").get(0));
		
		// Status could mean any number of things.  From Sirsi, we've got transit status and reserve status
		// Could also be "current location".  I'm going to assume it's current location here, based on the data I've seen

		// This has been wrong forever -- there is no subfield "035" of a 999 field, this was probably meant to get
		// VariableField 035 from the record, but we don't need to do that
		//String itemstatus = subfields.get("035").get(0);

		String item_status_code = "";
		String item_status_name = "";
		//if ( itemString.size() > 0 ) {
		if ( sirsiItem != null ) {
			// Use the "shadowed" attribute to set the staffOnlyFlag
			//item.setStaffOnlyFlag(itemString.get(25));
			item.setStaffOnlyFlag(sirsiItem.getShadowed());

			//String item_res_status = itemString.get(28);
			//String num_charges = itemString.get(5);
			String item_res_status = sirsiItem.getReserve_status();
			String num_charges = Integer.toString(sirsiItem.getNum_charges());
			
			if ( item_res_status != null && itemReservedStatusCodeMap.get(item_res_status) != null ) {
				item_status_code = itemReservedStatusCodeMap.get(item_res_status);
				item_status_name = itemReservedStatusNameMap.get(item_res_status);
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
				LU_DBLoadInstances.Log(System.err, "Neither item reserved status or number of charges set: " + sirsiItem.toString() + ", not assigning status",
						LU_DBLoadInstances.LOG_WARN);
				item_status_code = "UNAVAILABLE";
				item_status_name = "Unavailable";
			}
		} else {
			LU_DBLoadInstances.Log(System.err, "No itemstring for item " + itemID + ", not assigning status", 
					LU_DBLoadInstances.LOG_WARN);
			item_status_code = "UNAVAILABLE";
			item_status_name = "Unavailable";
			item.setStaffOnlyFlag("N");
		}
		item.setItemStatus(item_status_code, item_status_name);

		// If it were "reserve status", then we'd use this:
		//item.setItemStatus(itemString.get(28));
		// Nothing to put here from Sirsi, just going to make it today
		//DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		item.setItemStatusDateUpdated(df.format(Calendar.getInstance().getTime()));
		
		// Should also only be one of these
		if ( subfields.get("$j") != null ) {
			item.setNumberOfPieces(subfields.get("$j").get(0));
		}
		if ( subfields.get("$p") != null ) {
			String pricestr = subfields.get("$p").get(0);
			// it's a numerical field now, so now $ signs
			pricestr = pricestr.replaceAll("\\$", "");
			try {
				double price = Double.parseDouble(pricestr);
				// if there's no exception, no problem
				item.setPrice(price);
			} catch (Exception e) {
				LU_DBLoadInstances.Log(System.err, "Not setting price from invalid string: " + pricestr,
						LU_DBLoadInstances.LOG_WARN);
			}
			
		}	
		
	    //String locStr = subfields.get("$l").get(0);
		// Items can override the location from the containing OLE Holdings
	    
	    if ( locStr.equals(ELECTRONIC_RESOURCE) ) {
	    	// No location to fill in ...
	    } else {
	    	LU_DBLoadInstances.Log(System.out, "Adding location information to instance holdings data", LU_DBLoadInstances.LOG_DEBUG);
	    	/*
	    	String[] locPieces = locStr.split("-|_");
	    	if ( locPieces.length == 3 ) {
	    		libraryName = libraryCodeToName.get(locPieces[0]);
	    		if ( locPieces[1].equals("SPCOLL") ) {
	    			shelvingStr = "Special Collections, " + locPieces[2];
	    		} else if ( locPieces[1].equals("JRNL") ) {
	    			shelvingStr = "Journal " + locPieces[2];
	    		} else {
	    			if ( locPieces[2].equals("O") ) {
	    				// Not sure what to do with location code L-3ROTND-O or L-4STACK-O yet,
	    				// so I'm just throwing away the "-O" part and assuming it should have been
	    				// L-3-ROTNDA and L-4-STACKS
	    				locPieces[1] = locPieces[1].substring(0, 1);
	    				locPieces[2] = locPieces[1].substring(1) + "Oversize";
	    			}
	    			shelvingStr = floorToName(locPieces[1]) + " " + locPieces[2];
	    		}
	    	} else if ( locPieces.length == 2 ) {
	    		libraryName = libraryCodeToName.get(locPieces[0]);
	    		shelvingStr = locPieces[1];
	    	} else if ( locPieces[0].equals("LMCJOURNAL") ){
	    		libraryName = "LMC";
	    		shelvingStr = "JOURNAL";
	    	} else {
	    		libraryName = locStr;
	    	}
			*/

	    	FlatLocation loc = this.getFlatLocation(locStr);
	    	//loc.setLevel("SHELVING");
	    	// ccc2 -- changed 2014-04-23
	    	//loc.setName(getLocationName(locStr));
	    	//loc.setName(locStr);
		    item.setLocation(loc);
	    }
		// TODO: go over these with Doreen
		// Fields not used:
		// copyNumberLabel, volumeNumber, volumneNumberLabel, enumeration
		// callNumber (inherited from OLEHOldings) and all its pieces -- callNumberPrefix,
		// callNumberType, classificationPart, itemPart		
		// statisticalSearchingCodes?

		// item.setChronology(chronology) // TODO: this might be a date at the end of the call 
                                          // number (subfield $a), but doesn't appear to be there for most
		
			
		// If we're creating e-instances, then we don't need to make multiple items here, as
		// there will be a totally separate instance for each URL
		// Until OLE can ingest e-instances, we need to make multiple items with different
		// values of the accessInformation for the items that are electronic resources
		if ( subfields.get("$l").equals(ELECTRONIC_RESOURCE) && !eInstanceReady) {
			List<VariableField> accessinfofields = record.getVariableFields("856");
			for ( VariableField accessinfofield : accessinfofields ) {
				Item itemcopy = new Item(item); // copy the item
				List<String> URLs = this.getSubfields(accessinfofield).get("$u"); // TODO: sometimes URI is in $a or $z
				if ( URLs != null && URLs.size() > 0 ) {
					AccessInformation ai = new AccessInformation();
					ai.setUri(new URI(URLs.get(0)));
					itemcopy.setAccessInformation(ai);
					// itemcopy.setItemInstance(inst);
					// inst.getItems().add(itemcopy);
					itemcopy.setItemHoldings(holdings);
					holdings.getItems().add(itemcopy);
				}
			}
		} else {
			// not an electronic resource, or eInstances are ready,
			// so just set the barcode
			AccessInformation ai = new AccessInformation();
			ai.setBarcode(itemID);
			item.setAccessInformation(ai);
			//item.setItemInstance(inst);
			//inst.getItems().add(item);
			item.setItemHoldings(holdings);
			holdings.getItems().add(item);
		}
	}
	
	public void buildHoldingsData(Record record, Bib bib, Map<String, List<String>> subfields, Record assocMFHDRec) {
		// Use the first callNumber in the list to fill in some info 
		
	    // There should only be one subfield "a", as it's the item's call number and should be unique
	    // And there should be only 1 call number with that "item number" in Sirsi, so we can just get the first
	    // element of each of those lists
		String callnumberstr = subfields.get("$a").get(0).trim();
		LU_DBLoadInstances.Log(System.out, "Building holdings data for item !" + callnumberstr + "!", LU_DBLoadInstances.LOG_DEBUG);
		LU_DBLoadInstances.Log(System.out, "Subfields: ", LU_DBLoadInstances.LOG_DEBUG);
		for ( String key : subfields.keySet() ) {
			List<String> fields = subfields.get(key);
			for ( String value : fields ) {
				LU_DBLoadInstances.Log(System.out, "	" + key + ": " + value, LU_DBLoadInstances.LOG_DEBUG);
			}
		}
		if ( assocMFHDRec != null ) {
			LU_DBLoadInstances.Log(System.out, "Associated MFHD record: " + assocMFHDRec.toString(), LU_DBLoadInstances.LOG_DEBUG);
		}
		Pattern p = Pattern.compile("<U\\+([0-9|a-z]+)>");
		Matcher m;		
		int numval = 0;
		String replacement = "";

		/*
		m = p.matcher(callnumberstr);
		while ( m.find() ) {
			numval = Integer.parseInt(m.group(1), 16);
			replacement = Character.toString((char)numval);
			callnumberstr = m.replaceAll(replacement);
		}
		*/

		/* not used anymore
		List<String> callNumberFields = new ArrayList<String>();
		if ( callNumbersByItemNumber.get(callnumberstr) == null ) {
			LU_DBLoadInstances.Log(System.err, "No call number exists for item: " + subfields.toString(),
					LU_DBLoadInstances.LOG_ERROR);
		} else {
			callNumberFields = this.callNumbersByItemNumber.get(callnumberstr).get(0);			
		}
		if ( subfields.get("$a").size() != 1 ||
			// TODO: not sure how to handle this -- it comes up a lot
				(callNumbersByItemNumber.get(callnumberstr) != null && 
				 callNumbersByItemNumber.get(callnumberstr).size() != 1) ) {
			LU_DBLoadInstances.Log(System.err, "Call number (item number) not unique for item: " + subfields.toString(),
									LU_DBLoadInstances.LOG_DEBUG);
			// TODO: print list of callNumbers for this item number
		}
		*/
		
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
		
		Map<String, List<String>> tmpsubfields;
		//inst.setInstanceIdentifier(callNumberFields.get(0));
	
		
		//inst.setResourceIdentifier(subfields.get("$a").get(0));
		//inst.setResourceIdentifier(LU_DBLoadInstances.formatCatKey(record.getControlNumber())); // need to set this to what's in 001 of the bib to link them
		//inst.setResourceIdentifier(LU_DBLoadInstances.formatCatKey(callNumberFields.get(0)));

		// Some records may have multiple 035 fields, ex "British Pacific Fleet experience and legacy, 1944-50"
		/* Taking this out, as it looks like there isn't a former identifier set associated with an instance anymore
		List<String> formerIDs = subfields.get("035");
		FormerIdentifier fi;
		Identifier id;
		ArrayList<FormerIdentifier> fids = new ArrayList<FormerIdentifier>();
		if ( formerIDs != null && 
				formerIDs.size() > 0 ) {
			for ( String fid : formerIDs ) {
				fi = new FormerIdentifier();
				id = new Identifier();
				id.setSource("SIRIS_MARC_035");
				id.setIdentifierValue(fid);
				fi.setIdentifier(id);
				fids.add(fi);
			}
			inst.setFormerResourceIdentifiers(fids);
		}
		*/

		/*
		SourceHoldings sh = new SourceHoldings();
		sh.setPrimary("false");
		inst.setSourceHoldings(sh);
		*/
		
		// Build up oleHoldings within instance
		OLEHoldings oh = new OLEHoldings(bib);
		oh.setStaffOnly(bib.getStaffOnly());
		//oh.setHoldingsType(holdingsType); // TODO: should we be doing this here?
		// the way i've been interpreting it, holdings don't have types.  items do.
		// it's at the item level that we say whether something is electronic or physical
		// we could create separate holdings for the electronic ones, but aren't currently
		
		List<String> nonpublicNoteType = Arrays.asList(".CIRCNOTE.", ".STAFF.");
		List<String> publicNoteType = Arrays.asList(".PUBLIC.");
	    List<String> commentfields = subfields.get("$o"); // TODO: figure out the split and regex, test this
	    if ( commentfields != null && commentfields.size() > 0 ) {
	    	LU_DBLoadInstances.Log(System.out, "Adding " + commentfields.size() + " comments to instance", LU_DBLoadInstances.LOG_DEBUG);
	    	for ( String comment : commentfields ) {
	    		// Keep the delimiter on the preceding element of the split array
	    		OLEHoldingsNote note = new OLEHoldingsNote();
	    		String[] pieces = comment.split("(?<=\\. )");
    			// People put periods in their comments sometimes, and that's the delimiter Sirsi uses between
    			// the comment type and the comment itself, so it blows up the comments array.
    			// *sigh*
    			// So, we just join all the pieces after element 0 together 
	    		String commentstr = StringUtils.join(Arrays.asList(pieces).subList(1, pieces.length));
	    		/*
	    		if ( pieces.length != 2 ) {
	    			LU_DBLoadInstances.Log(System.err, "Badly formatted comment: " + comment, LU_DBLoadInstances.LOG_ERROR);
	    			for (String piece : pieces ) {
	    				LU_DBLoadInstances.Log(System.err, "Piece: " + piece, LU_DBLoadInstances.LOG_ERROR);
	    				System.out.println("Piece: " + piece);
	    			}
	    			
	    		} else {
	    			commentstr = pieces[1];
	    		} 
	    		*/
	    		if ( nonpublicNoteType.contains(pieces[0].trim())) {
	    			note.setType(nonpublicStr);
	    		} else if ( publicNoteType.contains(pieces[0].trim())) {
	    			note.setType(publicStr);
	    		} else {
	    			LU_DBLoadInstances.Log(System.err, "Unknown type of comment: " + comment, LU_DBLoadInstances.LOG_WARN);
	    		}
	    		note.setNote(commentstr);
	    		note.setOLEHoldings(oh);
	    		oh.getNotes().add(note);
	    	}
	    }
	    
	    ArrayList<VariableField> uriFields = (ArrayList<VariableField>) record.getVariableFields("856");
	    if ( uriFields != null && uriFields.size() > 0 ){
	    	LU_DBLoadInstances.Log(System.out, "Adding " + uriFields.size() + " URIs to instance holdings data", LU_DBLoadInstances.LOG_DEBUG);
	    	for ( VariableField uriField : uriFields ) {
	    		tmpsubfields = this.getSubfields(uriField);
	    		if ( tmpsubfields.get("$u") != null ) {
		    		AccessURI uri = new AccessURI();
		    		uri.setText(tmpsubfields.get("$u").get(0));
	    			// TODO: what to do with the "z" subfields?  They would provide the coverage information in an e-instance
	    			// Not sure how to handle them here.
		    		uri.setOleHoldings(oh);
	    			oh.getAccessURIs().add(uri);
	    		} 
	    		if ( tmpsubfields.get("$a") != null ) {
		    		AccessURI uri = new AccessURI();
	    			uri.setText(tmpsubfields.get("$a").get(0));
	    			// TODO: what to do with the "z" subfields?  They would provide the coverage information in an e-instance
	    			// Not sure how to handle them here.
	    			uri.setOleHoldings(oh);
	    			oh.getAccessURIs().add(uri);	    				    			
	    		}
	    		if ( oh.getAccessURIs().size() == 0 ) {
	    			LU_DBLoadInstances.Log(System.err, "856 with no $u or $a subfields for record " + record.getControlNumber() + 
	    						        ", 856 field is" + uriField.toString(), LU_DBLoadInstances.LOG_WARN);	    			
	    		}
	    	}
	    	
	    }

		// Items can override the location from the containing OLE Holdings
	    String locStr = subfields.get("$l").get(0);
	    if ( locStr.equals(ELECTRONIC_RESOURCE) ) {
	    	// No location to fill in ...
	    } else {
	    	LU_DBLoadInstances.Log(System.out, "Adding location information to instance holdings data", LU_DBLoadInstances.LOG_DEBUG);
	    	/*
	    	String[] locPieces = locStr.split("-|_");
	    	if ( locPieces.length == 3 ) {
	    		libraryName = libraryCodeToName.get(locPieces[0]);
	    		if ( locPieces[1].equals("SPCOLL") ) {
	    			shelvingStr = "Special Collections, " + locPieces[2];
	    		} else if ( locPieces[1].equals("JRNL") ) {
	    			shelvingStr = "Journal " + locPieces[2];
	    		} else {
	    			if ( locPieces[2].equals("O") ) {
	    				// Not sure what to do with location code L-3ROTND-O or L-4STACK-O yet,
	    				// so I'm just throwing away the "-O" part and assuming it should have been
	    				// L-3-ROTNDA and L-4-STACKS
	    				locPieces[1] = locPieces[1].substring(0, 1);
	    				locPieces[2] = locPieces[1].substring(1) + "Oversize";
	    			}
	    			shelvingStr = floorToName(locPieces[1]) + " " + locPieces[2];
	    		}
	    	} else if ( locPieces.length == 2 ) {
	    		libraryName = libraryCodeToName.get(locPieces[0]);
	    		shelvingStr = locPieces[1];
	    	} else if ( locPieces[0].equals("LMCJOURNAL") ){
	    		libraryName = "LMC";
	    		shelvingStr = "JOURNAL";
	    	} else {
	    		libraryName = locStr;
	    	}
			*/

	    	oh.setFlatLocation(this.getFlatLocation(locStr));
		    //oh.setLocationStr(getLocationName(locStr));
		    //oh.setLocationLevelStr("SHELVING");
	    	
	    	/* old code 
	    	LocationLevel locLevel1 = new LocationLevel();
	    	locLevel1.setLevel("UNIVERSITY");
	    	locLevel1.setName("Lehigh University");
	    	LocationLevel locLevel2 = new LocationLevel();
	    	locLevel2.setLevel("LIBRARY");
	    	locLevel2.setName(libraryName);
	    	locLevel1.setSubLocationLevel(locLevel2);
	    	if ( shelvingStr != null && shelvingStr.length() > 0 ) {
	    		LocationLevel locLevel3 = new LocationLevel();
	    		locLevel3.setLevel("Shelving");
	    		locLevel3.setName(shelvingStr);
	    		locLevel2.setSubLocationLevel(locLevel3);
	    	}
	    	location.setLocLevel(locLevel1);
	    	
    		// Also old code ..
	    	FlatLocation flatLoc = new FlatLocation();
	    	flatLoc.setLevel("SHELVING");
	    	flatLoc.setName(shelving.getName());
		    oh.setLocation(flatLoc);
		    
		    */
	    }		
	    
    	LU_DBLoadInstances.Log(System.out, "Adding callnumber info to instance holdings data", LU_DBLoadInstances.LOG_DEBUG);
	    CallNumber cn = new CallNumber();
	    // Same as the shelvingscheme for now
	    String cntype = subfields.get("$w").get(0);
	    
	    cn.setPrefix("");
	    cn.setNumber(subfields.get("$a").get(0));

	    // Not used: callNumberType, callNumberPrefix, itemPart,
	    // They make reference to MFHD 852 codes i, h, and k
	    // Those don't appear to be in our data anywhere
	    // TODO: run that by Doreen, et al ^^^
	    // Also not used currently: shelving scheme
		String cntypecode = callNumberTypeCodes.get(cntype);
		String cntypename = callNumberTypeNames.get(cntype);
		if ( cntypecode == null || cntypecode.length() == 0) {
			cntypecode = "N/A";
		}
		if ( cntypename == null || cntypename.length() == 0) {
			cntypename = "N/A";
		}
		oh.setCallNumberType(cntypecode, cntypename);
		// It should be a "normalized" version of the call number, with
	    // spaces and other stuff removed
	    ShelvingOrder shelvingOrder = new ShelvingOrder();
	    shelvingOrder.setShelvingOrder(LU_BuildInstance.normalizeCallNumber(cn.getNumber(), cntypecode));
	    cn.setShelvingOrder(shelvingOrder);
	    oh.setCallNumber(cn);
	    
	    
	    ExtentOfOwnership extentOfOwnership = new ExtentOfOwnership();
		extentOfOwnership.setType("public", "public");
		if ( assocMFHDRec != null ) {
			VariableField eightsixsix = assocMFHDRec.getVariableField("866");
			if ( eightsixsix != null ) {
		    	LU_DBLoadInstances.Log(System.out, "Adding extent of ownership info to instance holdings data", LU_DBLoadInstances.LOG_DEBUG);				
				tmpsubfields = this.getSubfields(eightsixsix);
				LU_DBLoadInstances.Log(System.out, "Subfields of 866 for MFHD record: ", LU_DBLoadInstances.LOG_DEBUG);
				for ( String key : tmpsubfields.keySet() ) {
					List<String> fields = tmpsubfields.get(key);
					for ( String value : fields ) {
						LU_DBLoadInstances.Log(System.out, "	" + key + ": " + value, LU_DBLoadInstances.LOG_DEBUG);
					}
				}			
				// Should only be one 866 field with one "$a" subfield
				if ( tmpsubfields.get("$a") != null ) {
					extentOfOwnership.setTextualHoldings(tmpsubfields.get("$a").get(0));
				}
				if ( tmpsubfields.get("$z") != null ) {
					ExtentOfOwnershipNote n = new ExtentOfOwnershipNote();
					n.setNote(tmpsubfields.get("$z").get(0));
					n.setType("public");
					n.setExtentOfOwnership(extentOfOwnership);
					extentOfOwnership.getNotes().add(n);
				}
			}

			// TODO: receptStatus comes from the associated holdings record's
			// 008 field, position 6 (counting from 0 or 1, not sure, probably 1 given context)
		    // There will need to be separate instances for each MFHD record, probably -- should
		    // be either 1 or 2 MFHD records, if any, one for electronic version and one for physical
			String receiptStatus = assocMFHDRec.getVariableField("008").toString().substring(6, 7);
			oh.setReceiptStatus(new ReceiptStatus(receiptStatus, receiptStatus));
		} else {
			// TODO: no holdings record, where does the extent of ownership and receipt status come from?
		}
		extentOfOwnership.setOLEHoldings(oh);
		oh.getExtentOfOwnership().add(extentOfOwnership);
    	LU_DBLoadInstances.Log(System.out, "Done creating holdings data, adding to instance collection", LU_DBLoadInstances.LOG_DEBUG);

    	bib.getHoldings().add(oh);
    	
    	this.buildItemsData(record, bib, oh, subfields);
		//inst.setOleHoldings(oh);
		//oh.setInstance(inst);
	}

	public FlatLocation getFlatLocation(String locStr) {
		String libraryName = "", libraryCode = "", shelvingStr = "", collectionCode = "", collectionName = "";
		FlatLocation flatLoc = new FlatLocation();
		String newlocStr = "";
		String levelStr = "";
		if (libraryCodeToName.keySet().contains(locStr)) {
			levelStr = "Institution/Library";
			newlocStr = "LEHIGH/" + locStr;
		} else {
			collectionCode = locationCodeToCollectionCode.get(locStr);
			if (collectionCode != null) {
				collectionName = collectionCodeToName.get(collectionCode);
				libraryName = this.libraryCodeToName.get(collectionCodeToLibraryCode.get(collectionCode));
				libraryCode = this.collectionCodeToLibraryCode.get(collectionCode);
			} else {
				libraryName = libraryCodeToName.get(locationCodeToLibraryCode.get(locStr));
				libraryCode = locationCodeToLibraryCode.get(locStr);
			}
			shelvingStr = locationCodeToShelvingString.get(locStr);

			//Location institution = new Location();
			//institution.setLevelId((long)LOC_INSTITUTION);
			//institution.setCode("LEHIGH");
			//institution.setName("Lehigh University");
			//newlocStr = institution.getCode();
			newlocStr = "LEHIGH";
			levelStr = "Institution";

			//Location library = new Location();
			//library.setLevelId((long)LOC_LIBRARY);
			//library.setName(libraryName);
			//library.setCode(libraryCode);
			//library.setParentLocation(institution);
			//newlocStr += "/" + library.getCode();
			newlocStr += "/" + libraryCode;
			levelStr += "/Library";

			//Location shelving = new Location();
			//shelving.setLevelId((long)LOC_SHELVING);
			//shelving.setName(shelvingStr);
			//shelving.setCode(locStr);

			if ( collectionCode != null ) {
				//Location collection = new Location();
				//collection.setLevelId((long)LOC_COLLECTION);
				//collection.setName(collectionName);
				//collection.setCode(collectionCode);
				//collection.setParentLocation(library);
				//shelving.setParentLocation(collection);
				//newlocStr += "/" + collection.getCode();
				newlocStr += "/" + collectionCode;
				levelStr += "/Collection";
			} else {
				//shelving.setParentLocation(library);
			}
			//return shelving.getName();
			//newlocStr += "/" + shelving.getCode();
			
			// All renamed locations are shelving level locations
			if ( renamedLocations.get(locStr) != null ) {
				newlocStr += "/" + renamedLocations.get(locStr);
			} else {
				newlocStr += "/" + locStr;
			}
			levelStr += "/Shelving";
		}
		flatLoc.setLevel(levelStr);
		flatLoc.setLocCodeString(newlocStr);
		return flatLoc;
	}
	
	/*
	 * Not relevant anymore in directdb branch
	public static void testoutput(InstanceCollection ic) {
		// Test to make sure that the XML output looks good 
		
		// Build up instance
		Instance inst = new Instance();
		inst.setInstanceIdentifier("123");
		inst.setResourceIdentifier("3");
		SourceHoldings sh = new SourceHoldings();
		sh.setPrimary("false");
		//inst.setSourceHoldings(sh);
		
		// Build up oleHoldings within instance
		OLEHoldings oh = new OLEHoldings();
		oh.setHoldingsIdentifier("string");
		oh.setReceiptStatus(new ReceiptStatus("4", "4"));
		oh.setPrimary("true");
		URI uri = new URI();
		uri.setResolvable("string");
		oh.getUri().add(uri);
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
		oh.getExtentOfOwnership().add(eoo);
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
	*/
	
}
