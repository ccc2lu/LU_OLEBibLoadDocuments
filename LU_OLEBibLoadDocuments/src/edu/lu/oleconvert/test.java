package edu.lu.oleconvert;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

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
	
	public static void main(String arguments[]) {
		
		String callNumbersFilename = "/mnt/bigdrive/bibdata/allcallnums.txt";
		String itemsFilename = "/mnt/bigdrive/bibdata/allitems.txt";
		
	    LU_BuildInstance instanceBuilder = new LU_BuildInstance();		
		BufferedReader callNumbersReader, itemsReader;
		
		try {
        	callNumbersReader = new BufferedReader(new FileReader(callNumbersFilename));
        	itemsReader = new BufferedReader(new FileReader(itemsFilename));
        	// test.testReadingFiles1(callNumbersReader, itemsReader);
        	instanceBuilder.readSirsiFiles("/mnt/bigdrive/bibdata/allcallnums.txt", 
										   "/mnt/bigdrive/bibdata/allcallnumsshelvingkeys.txt",
										   "/mnt/bigdrive/bibdata/allcallnumsitemnumbers.txt",
										   "/mnt/bigdrive/bibdata/allcallnumsanalytics.txt",        														
										   "/mnt/bigdrive/bibdata/allitems.txt", 10000);
        	instanceBuilder.printHashMaps(10);
         	
		} catch(Exception e) {
			System.err.println("Unable to read in call numbers and items: " + e.getMessage());
			e.printStackTrace(System.err);
		}
	}
}
