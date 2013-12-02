package edu.lu.oleconvert;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.bind.Marshaller;

import org.marc4j.MarcWriter;
import org.marc4j.MarcXmlReader;
import org.marc4j.MarcXmlWriter;
import org.marc4j.marc.Record;
import org.marc4j.marc.VariableField;

import OLEBibLoadDocuments.edu.indiana.libraries.OLEBibLoadDocuments.classes.BuildRequestDocument;
import edu.indiana.libraries.JPADriver.classes.JPADriver;
import edu.indiana.libraries.LoadDocstore.jaxb.RequestType;
import edu.lu.oleconvert.ole.Note;

public class test {

public static final String BIBLIOGRAPHIC = "bibliographic";
public static final String MARC_FORMAT = "marc";
public static final String CATEGORY_WORK = "work";

	private static HashMap<String, ArrayList<String>> callNumbers = new HashMap<String, ArrayList<String>>();
	private static HashMap<String, ArrayList<String>> items = new HashMap<String, ArrayList<String>>();

	
	public static void testReadingFiles1(BufferedReader callNumbersReader, BufferedReader itemsReader) throws Exception {
		int badfields = 0;
       	System.out.println("Building hashmap of call item records by catalog key ...");
    	while(itemsReader.ready()) {
    		String line = itemsReader.readLine();
    		String fields[] = line.split("\\|");
    		if ( fields.length != 33 ) {
    			badfields++;
    			System.err.println("Wrong number of fields: ");
    			for( int i = 0; i < fields.length; i++ ) {
    				System.err.println(i + ": " + fields[i]);
    			}
    		}
    		
    		if ( items.get(fields[3]) == null ) {
    			ArrayList<String> itemStrs = new ArrayList<String>();
    			itemStrs.add(line);
    			items.put(fields[3], itemStrs);
    		} else {
    			items.get(fields[3]).add(line);
    		}
    	}
    	System.err.println("Number of bad field strings in item records: " + badfields);
    	badfields = 0;
       	System.out.println("Building hashmap of call number records by catalog key ...");
    	while(callNumbersReader.ready()) {
    		String line = callNumbersReader.readLine();
    		String fields[] = line.split("\\|");
    		if ( fields.length != 12 ) {
    			badfields++;
    			System.err.println("Wrong number of fields: ");
    			for( int i = 0; i < fields.length; i++ ) {
    				System.err.println(i + ": " + fields[i]);
    			}
    		}
    		if ( callNumbers.get(fields[2]) == null ) {
    			ArrayList<String> callNumberStrs = new ArrayList<String>();
    			callNumberStrs.add(line);
    			callNumbers.put(fields[2], callNumberStrs);
    		} else {
    			callNumbers.get(fields[2]).add(line);
    		}
    	}
    	System.err.println("Number of bad field strings in call number records: " + badfields);
    	System.exit(0);
    	
    		//TODO: instead of reading the files again for each catalog record, we're
    		//      going to cache all the callnumber and item records in big hashmaps,
    		//      keyed by the catalog record
    		//ReadCallNumber(inst, callNumbersReader);
		
	}
	

	
	public static void testReadingXMLRecord(LU_BuildInstance instanceBuilder, String filename, int limit) {
		
		Record record;
        MarcXmlReader reader;
        Map<String, List<String>> subfieldsmap;
		try {
			reader = new MarcXmlReader(new FileInputStream(filename));
	        int curr = 0;
	        while (reader.hasNext() && (curr++ < limit) ) {
	        	record = reader.next();
	        	List<VariableField> recitems = record.getVariableFields("999"); 
	    	    String catalogKey = record.getVariableField("001").toString().split(" ")[1];
	    	    System.out.println("999 fields for record with catalog key " + catalogKey);
	        	for ( VariableField field : recitems ) {
	        		System.out.println("Field ID: " + field.getId() + ", tag: " + field.getTag() + 
	        				           ", whole thing: " + field.toString());
	        		subfieldsmap = instanceBuilder.getSubfields(field);
	        		for ( Object key : subfieldsmap.keySet().toArray() ) {
	        			List<String> subfields = subfieldsmap.get(key);
	        			for ( String value : subfields ) {
	        				System.out.println("Subfield " + key + ", " + value);
	        			}
	        		}
	        	}
	        	System.out.println();
	        }
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void splitTest() {
		List<String> nonpublicNoteType = Arrays.asList(".CIRCNOTE.", ".STAFF.");
		List<String> publicNoteType = Arrays.asList(".PUBLIC.");
	    List<String> commentfields = Arrays.asList(".CIRCNOTE. test1", ".PUBLIC. test2", ".STAFF. test3"); 
	    for ( String comment : commentfields ) {
	    	System.out.println();
	    	//String[] pieces = comment.split("\\.[A-Z]+\\.");
	    	// Keep the delimiter on the preceding element of the split array
	    	String[] pieces = comment.split("(?<=\\. )");
	    	if ( pieces.length != 2 ) {
	    		System.err.println("Badly formatted comment: " + comment);
	    	}
	    	for (String piece : pieces ) {
	    		System.out.println("Piece: " + piece);
	    	}
	    	if ( nonpublicNoteType.contains(pieces[0].trim())) {
	    		System.out.println("Type is nonpublic");
	    	} else if ( publicNoteType.contains(pieces[0].trim())) {
	    		System.out.println("Type is public");
	    	} else {
	    		System.out.println("Unknown type");
	    	}
	    	System.out.println("Type is " + pieces[1]);
	    }
	    
	    String locStr = "FM-4-NORTH";
	    String[] locPieces = locStr.split("-|_");
	    for ( String piece : locPieces ) {
	    	System.out.println(piece);
	    }
	}
	
	public static void count999Fields() {
		Marshaller marshaller;
		Properties loadprops;
		Properties oracleprops;
		Properties instanceprops;
		JPADriver jpaDriver = new JPADriver();
		PreparedStatement statement;
		Connection connection=null;
		ResultSet resultSet;
		BufferedWriter outFile = null;
		BufferedReader inFile = null;
		ByteArrayOutputStream out = null;
		MarcWriter writer;
		RequestType request;
		int num999s = 0, numbibs = 0, numholdings = 0;
		try {
			MarcXmlReader reader = new MarcXmlReader(new FileInputStream("/Users/ccc2/dev/bibdata/catalog.20131113.mod.marcxml"));
			int limit = -1, curr = 0;
			Record xmlrecord;
			int counter = 0, showprogress = 10000;
			while (reader.hasNext() && (limit <= 0 || counter < limit) ) {
				xmlrecord = reader.next();

				List<VariableField> items = xmlrecord.getVariableFields("999");
				if ( items != null && items.size() > 0 ) {
					num999s += items.size();
					numbibs++;
				} else {
					numholdings++;
					//System.err.println("No items for record " + xmlrecord.toString());
				}
				// ccc2 -- catalog was only needed to get some extra data elements, which we get from a 
				// separate file that I generated using selcatalog
				// Catalog catalog = (Catalog) JPADriver.getObject(resultSet,Catalog.class);

				if ( counter++ % showprogress == 0 ) {
					System.out.println("On record " + counter);
				}
			}  		
			System.out.println("Num bibs: " + numbibs + ", num items: " + num999s + ", num holdings: " + numholdings);
		} catch(Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace(System.err);
		}		
	}
	
	public static void countMFHDRecsMoreThanTwo() {
		Marshaller marshaller;
		Properties loadprops;
		Properties oracleprops;
		Properties instanceprops;
		JPADriver jpaDriver = new JPADriver();
		PreparedStatement statement;
		Connection connection=null;
		ResultSet resultSet;
		BufferedWriter outFile = null;
		BufferedReader inFile = null;
		ByteArrayOutputStream out = null;
		MarcWriter writer;
		RequestType request;

		try {
			PrintWriter output = new PrintWriter("/Users/ccc2/dev/bibdata/MFHDReport.txt");
			MarcXmlReader reader = new MarcXmlReader(new FileInputStream("/Users/ccc2/dev/bibdata/catalog.20131113.mod.marcxml"));
			int limit = -1, curr = 0, mfhdcount = 0;
			List<Character> holdingsTypes = Arrays.asList('u', 'v', 'x', 'y');
			Record xmlrecord, nextrecord;
			nextrecord = reader.next();
			int counter = 0, showprogress = 10000;
			ArrayList<Record> assocMFHDRecords = new ArrayList<Record>();                
			do {
				assocMFHDRecords.clear();
				xmlrecord = nextrecord;
				nextrecord = reader.next();
				// The associated holdings records for a bib record should always come right after it
				// So we keep looping and adding them to an ArrayList as we go
				while ( nextrecord != null && 
						holdingsTypes.contains(nextrecord.getLeader().getTypeOfRecord()) ) {

					assocMFHDRecords.add(nextrecord);
					nextrecord = reader.next();
				}
				if ( assocMFHDRecords.size() > 2 ) {
					output.println("Found a record with " + assocMFHDRecords.size() + " MFHD records: " + xmlrecord.toString());
					for ( Record tmpRecord : assocMFHDRecords ) {
						output.println("MFHD record: " + tmpRecord.toString());
					}
					mfhdcount++;
				}
				// ccc2 -- catalog was only needed to get some extra data elements, which we get from a 
				// separate file that I generated using selcatalog
				// Catalog catalog = (Catalog) JPADriver.getObject(resultSet,Catalog.class);

				if ( counter++ % showprogress == 0 ) {
					System.out.println("On record " + counter);
				}
			} while (nextrecord != null && (limit <= 0 || counter < limit) );                
			System.out.println(mfhdcount + " records found with more than 2 MFHD records");
		} catch(Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace(System.err);
		}                
	}

	public static void countMFHDRecordsAnd999Fields() {
		Marshaller marshaller;
		Properties loadprops;
		Properties oracleprops;
		Properties instanceprops;
		JPADriver jpaDriver = new JPADriver();
		PreparedStatement statement;
		Connection connection=null;
		ResultSet resultSet;
		BufferedWriter outFile = null;
		BufferedReader inFile = null;
		ByteArrayOutputStream out = null;
		MarcWriter writer;
		RequestType request;

		try {
			MarcXmlReader reader = new MarcXmlReader(new FileInputStream("/mnt/bigdrive/bibdata/catalog.07302013.plusholdings.mod.marcxml"));
			int limit = -1, curr = 0;
			List<Character> holdingsTypes = Arrays.asList('u', 'v', 'x', 'y');
			Record xmlrecord, nextrecord;
			nextrecord = reader.next();
			int counter = 0, showprogress = 10000;
			ArrayList<Record> assocMFHDRecords = new ArrayList<Record>();		
			do {
				assocMFHDRecords.clear();
				xmlrecord = nextrecord;
				nextrecord = reader.next();
				// The associated holdings records for a bib record should always come right after it
				// So we keep looping and adding them to an ArrayList as we go
				while ( nextrecord != null && 
						holdingsTypes.contains(nextrecord.getLeader().getTypeOfRecord()) ) {

					assocMFHDRecords.add(nextrecord);
					nextrecord = reader.next();
				}
				if ( assocMFHDRecords.size() > 0 &&
					 assocMFHDRecords.size() != xmlrecord.getVariableFields("999").size() ) {
					System.err.println("Found a record with different number of MFHD records and 999 fields: " + xmlrecord.toString());
				}
				// ccc2 -- catalog was only needed to get some extra data elements, which we get from a 
				// separate file that I generated using selcatalog
				// Catalog catalog = (Catalog) JPADriver.getObject(resultSet,Catalog.class);

				if ( counter++ % showprogress == 0 ) {
					System.out.println("On record " + counter);
				}
			} while (nextrecord != null && (limit <= 0 || counter < limit) );		
		} catch(Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace(System.err);
		}
	}
	
	public static void countMFHDRecords866Fields() {
		Marshaller marshaller;
		Properties loadprops;
		Properties oracleprops;
		Properties instanceprops;
		JPADriver jpaDriver = new JPADriver();
		PreparedStatement statement;
		Connection connection=null;
		ResultSet resultSet;
		BufferedWriter outFile = null;
		BufferedReader inFile = null;
		ByteArrayOutputStream out = null;
		MarcWriter writer;
		RequestType request;

		try {
			MarcXmlReader reader = new MarcXmlReader(new FileInputStream("/mnt/bigdrive/bibdata/catalog.07302013.plusholdings.mod.marcxml"));
			int limit = -1, curr = 0;
			List<Character> holdingsTypes = Arrays.asList('u', 'v', 'x', 'y');
			Record xmlrecord, nextrecord;
			nextrecord = reader.next();
			int count = 0, showprogress = 10000;
			ArrayList<Record> assocMFHDRecords = new ArrayList<Record>();
			do {
				assocMFHDRecords.clear();
				xmlrecord = nextrecord;
				nextrecord = reader.next();
				count = 0;
				// The associated holdings records for a bib record should always come right after it
				// So we keep looping and adding them to an ArrayList as we go
				while ( nextrecord != null &&
						holdingsTypes.contains(nextrecord.getLeader().getTypeOfRecord()) ) {
					
					//System.out.println("MFHD record: " + nextrecord.toString());
					
					// For each MFHD record:
					// Check if there's an 866 tag with a $a subfield
					// I think there shouldn't be more than 1 of these per bib record
					List<VariableField> fields = nextrecord.getVariableFields("866");
					for ( VariableField field : fields ) {
						Map<String, List<String>> subfields = LU_BuildInstance.getSubfields(field);
						if ( subfields.get("$a") != null ) {
							count++;
							//break;
							//System.out.println("MFHD record with $a subfield: " + nextrecord.toString());
						}
						if ( count >= 2 ) {
							System.err.println("More than 1 MFHD record with $a subfield for record " + nextrecord.toString());
						}
					}
					nextrecord = reader.next();
				}

				// ccc2 -- catalog was only needed to get some extra data elements, which we get from a 
				// separate file that I generated using selcatalog
				// Catalog catalog = (Catalog) JPADriver.getObject(resultSet,Catalog.class);
				if ( ++curr % showprogress == 0 ) {
					System.out.println("On record " + curr);
				}
				if ( curr == 51 ) {
					System.out.println("here is where it gets stuck ... " );
				}
			} while (nextrecord != null && (limit < 0  || curr++ < limit ));

		} catch (IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		} catch (org.marc4j.MarcException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.        	
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.        				
		}

	}

	public static void readMFHDRec() {
		try {
			MarcXmlReader reader = new MarcXmlReader(new FileInputStream("/mnt/bigdrive/bibdata/sampleMFHDRec.marcxml"));
			Record mfhdrec = reader.next();
			String receiptStatus = mfhdrec.getVariableField("008").toString().substring(6, 7);
			System.out.println("Whole record: " + mfhdrec.toString() + ", receipt status: " + receiptStatus);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}
	public static void main(String arguments[]) {
		
		String callNumbersFilename = "/mnt/bigdrive/bibdata/allcallnums.txt";
		String itemsFilename = "/mnt/bigdrive/bibdata/allitems.txt";
		
	    LU_BuildInstance instanceBuilder = new LU_BuildInstance();		
		BufferedReader callNumbersReader, itemsReader;
		
		try {
        	//callNumbersReader = new BufferedReader(new FileReader(callNumbersFilename));
        	//itemsReader = new BufferedReader(new FileReader(itemsFilename));
        	// test.testReadingFiles1(callNumbersReader, itemsReader);

        	//test.testReadingXMLRecord(instanceBuilder, "/mnt/bigdrive/bibdata/catalog.07302013.plusholdings.mod.marcxml", 1000);
        	
        	/*
        	instanceBuilder.readSirsiFiles("/mnt/bigdrive/bibdata/allcallnums.txt", 
										   "/mnt/bigdrive/bibdata/allcallnumsshelvingkeys.txt",
										   "/mnt/bigdrive/bibdata/allcallnumsitemnumbers.txt",
										   "/mnt/bigdrive/bibdata/allcallnumsanalytics.txt",        														
										   "/mnt/bigdrive/bibdata/allitems.txt", -1);
        	instanceBuilder.printHashMaps(0);
        	*/

         	//splitTest();
        	
         	//countMFHDRecords866Fields();
         	//countMFHDRecordsAnd999Fields();
			//readMFHDRec();
			countMFHDRecsMoreThanTwo();
			
			
		} catch(Exception e) {
			System.err.println("Unable to read in call numbers and items: " + e.getMessage());
			e.printStackTrace(System.err);
		} catch(Throwable t) {
			System.err.println("Throwable thrown" + t.getMessage());
			t.printStackTrace(System.err);
		}
	}
}
