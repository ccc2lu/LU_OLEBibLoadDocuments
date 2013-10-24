package edu.lu.oleconvert;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.marc4j.MarcXmlReader;
import org.marc4j.marc.Record;
import org.marc4j.marc.VariableField;

public class test {

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
	
	public static void main(String arguments[]) {
		
		String callNumbersFilename = "/mnt/bigdrive/bibdata/allcallnums.txt";
		String itemsFilename = "/mnt/bigdrive/bibdata/allitems.txt";
		
	    LU_BuildInstance instanceBuilder = new LU_BuildInstance();		
		BufferedReader callNumbersReader, itemsReader;
		
		try {
        	callNumbersReader = new BufferedReader(new FileReader(callNumbersFilename));
        	itemsReader = new BufferedReader(new FileReader(itemsFilename));
        	// test.testReadingFiles1(callNumbersReader, itemsReader);

        	test.testReadingXMLRecord(instanceBuilder, "/mnt/bigdrive/bibdata/catalog.07302013.plusholdings.mod.marcxml", 1000);
        	
        	/*
        	instanceBuilder.readSirsiFiles("/mnt/bigdrive/bibdata/allcallnums.txt", 
										   "/mnt/bigdrive/bibdata/allcallnumsshelvingkeys.txt",
										   "/mnt/bigdrive/bibdata/allcallnumsitemnumbers.txt",
										   "/mnt/bigdrive/bibdata/allcallnumsanalytics.txt",        														
										   "/mnt/bigdrive/bibdata/allitems.txt", -1);
        	instanceBuilder.printHashMaps(0);
        	*/

         	
		} catch(Exception e) {
			System.err.println("Unable to read in call numbers and items: " + e.getMessage());
			e.printStackTrace(System.err);
		}
	}
}
