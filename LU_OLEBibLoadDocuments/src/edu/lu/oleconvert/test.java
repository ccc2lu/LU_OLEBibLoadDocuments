package edu.lu.oleconvert;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

public class test {

	public static void main(String arguments[]) {
		HashMap<String, ArrayList<String>> callNumbers = new HashMap<String, ArrayList<String>>();
		HashMap<String, ArrayList<String>> items = new HashMap<String, ArrayList<String>>();
		
		String callNumbersFilename = "/mnt/bigdrive/bibdata/allcallnums.txt";
		String itemsFilename = "/mnt/bigdrive/bibdata/allitems.txt";
		BufferedReader callNumbersReader, itemsReader;
		int badfields = 0;
		
		try {
        	callNumbersReader = new BufferedReader(new FileReader(callNumbersFilename));
        	itemsReader = new BufferedReader(new FileReader(itemsFilename));

           	System.out.println("Building hashmap of call number records by catalog key ...");
        	while(callNumbersReader.ready()) {
        		String line = callNumbersReader.readLine();
        		String fields[] = line.split("\\|");
        		if ( fields.length != 15 ) {
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
        	System.err.println("Number of bad field strings: " + badfields);
        	System.exit(0);
        	
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
        	
        		//TODO: instead of reading the files again for each catalog record, we're
        		//      going to cache all the callnumber and item records in big hashmaps,
        		//      keyed by the catalog record
        		//ReadCallNumber(inst, callNumbersReader);
        	
		} catch(Exception e) {
			System.err.println("Unable to read in call numbers and items: " + e.getMessage());
			e.printStackTrace(System.err);
		}
	}
}
