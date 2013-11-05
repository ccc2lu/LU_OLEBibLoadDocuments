package edu.lu.oleconvert;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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
	
	public static void countMFHDRecords() {
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
        Record record = null;
        ByteArrayOutputStream out = null;
        MarcWriter writer;
        RequestType request;
        
		try {
        MarcXmlReader reader = new MarcXmlReader(new FileInputStream("/mnt/bigdrive/bibdata/catalog.07302013.plusholdings.mod.marcxml"));
        int limit = 50, curr = 0;
        List<Character> holdingsTypes = Arrays.asList('u', 'v', 'x', 'y');
        Record xmlrecord, nextrecord;
        nextrecord = reader.next();
    	ArrayList<Record> assocMFHDRecords = new ArrayList<Record>();;
        do {
        	assocMFHDRecords.clear();
        	xmlrecord = nextrecord;
        	nextrecord = reader.next();		
        	assocMFHDRecords.clear();
        	xmlrecord = nextrecord;
        	nextrecord = reader.next();
        	// The associated holdings records for a bib record should always come right after it
        	// So we keep looping and adding them to an ArrayList as we go
        	while ( holdingsTypes.contains(nextrecord.getLeader().getTypeOfRecord()) ) {
        		
        		assocMFHDRecords.add(nextrecord);
        		// TODO:
        		// Check if there's an 866 tag with a $a subfield
        		// I think there shouldn't be more than 1 of these per bib record
        		nextrecord = reader.next();
        	}

        	// ccc2 -- catalog was only needed to get some extra data elements, which we get from a 
        	// separate file that I generated using selcatalog
        	// Catalog catalog = (Catalog) JPADriver.getObject(resultSet,Catalog.class);

            out = new ByteArrayOutputStream();
            writer = new MarcXmlWriter(out,"UTF-8");
            writer.write(xmlrecord);
            writer.close();
            String marcXML=out.toString("UTF-8"); // ccc2 -- this is what we already have -- could be we just need
            									  // to process it one record at a time?  
        } while (nextrecord != null && curr++ < limit );
        
        outFile.close();
    } catch (IOException e) {
    	//Log(System.err, e.getMessage(), LOG_ERROR);
        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    } catch (org.marc4j.MarcException e) {
    	//Log(System.err, e.getMessage(), LOG_ERROR);
        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.        	
    }
		
	}
	
	public static void main(String arguments[]) {
		
		String callNumbersFilename = "/mnt/bigdrive/bibdata/allcallnums.txt";
		String itemsFilename = "/mnt/bigdrive/bibdata/allitems.txt";
		
	    LU_BuildInstance instanceBuilder = new LU_BuildInstance();		
		BufferedReader callNumbersReader, itemsReader;
		
		try {
        	callNumbersReader = new BufferedReader(new FileReader(callNumbersFilename));
        	itemsReader = new BufferedReader(new FileReader(itemsFilename));
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

         	splitTest();
		} catch(Exception e) {
			System.err.println("Unable to read in call numbers and items: " + e.getMessage());
			e.printStackTrace(System.err);
		}
	}
}
