package edu.lu.oleconvert;

import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.time.DateUtils;
import org.marc4j.MarcXmlReader;
import org.marc4j.marc.Record;
import org.marc4j.marc.VariableField;
import org.xml.sax.InputSource;

import edu.lu.oleconvert.ole.Bib;
import edu.lu.oleconvert.ole.Coverage;

public class test2 {

	public static void main(String args[]) {
		//testReplaceXML();
		//checkHoldingsRecords();
		//testTokenizer();
		//testParseCoverage();
		//testReadSFXData();
		//matchHoldingsRecordsWithSFX();
		//testDateAdd();
		buildCoverageData();
	}
	
	public static void testReadSFXData() {
		String sfx_export_file = "/mnt/bigdrive/bibdata/LehighData/sfx_export_portfolios.tsv";
		String line = "";
		int limit = -1, curr = 0, issn = 0;
		String headers[], pieces[];
		try {
			BufferedReader sfx_reader = new BufferedReader(new FileReader(sfx_export_file));
			line = sfx_reader.readLine();
			headers = line.split("\t");
			while(sfx_reader.ready() && (limit < 0 || curr++ < limit)) {
				line = sfx_reader.readLine();
				pieces = line.split("\t");
				if ( pieces[0].length() > 0 ) {
					System.out.println("ISSN found for " + pieces[8]);
					for ( int i = 0; i < pieces.length; i++ ) {
						System.out.println(headers[i] + ": " + pieces[i]);
					}
					issn++;
				}
				System.out.println();
				
			}
			System.out.println("Number of publications with ISSNs: " + issn);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

	public static void testDateAdd() {
		String datestr = "1992";
		System.out.println("Datestr starts out as: " + datestr);
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		String[] acceptedFormats = {"yyyyMMdd", "yyyy"};
		try {
			Date date = DateUtils.parseDate(datestr, acceptedFormats);
			date = DateUtils.addYears(date, 4);
			date = DateUtils.addMonths(date, 8);
			datestr = df.format(date);
			System.out.println("Datestr is now: " + datestr);
		} catch(Exception e) {
			LU_DBLoadInstances.Log(System.err, "Unable to set coverage record's start date from date string: " + datestr,
					LU_DBLoadInstances.LOG_ERROR);
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
		String[] acceptedFormats = {"yyyyMMdd", "yyyy"};

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

	public static List<Coverage> buildCoverages(List<Map<String, String>> sfxdata_list) {
		List<Coverage> coverages = new ArrayList<Coverage>();
		Coverage coverage;
		String datestr = "", token = "";
		Tokenizer tokenizer = new Tokenizer();
		tokenizer.setBreakchars(Arrays.asList(new String[]{"\"", "'", ",", "(", ")", " "}));
		for ( Map<String, String> sfxdata : sfxdata_list ) {
			datestr = sfxdata.get("THRESHOLD_GLOBAL");
			datestr = datestr.replaceAll("\\$obj->", "");
			// datestr should now be of the form parseDate('>=','2005','27','10') && parsedDate('<=','2009','33','1')
			// or parsedDate('>=',1979,1,4)
			// or parsedDate(">=",2001,48,1) && timediff('>=','1y')
			// etc.
			if ( datestr != null && datestr.length() > 0) {
				System.out.println("Datestr from THRESHOLD_GLOBAL is: " + datestr);
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
				System.out.println("Datestr from THRESHOLD_ACTIVE is: " + datestr);
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
	
	public static void printCoverages(List<Coverage> coverages, PrintStream out) {
		for ( Coverage coverage : coverages ) {
			out.println("coverage: " + coverage.toString());
		}
	}
	
	public static void buildCoverageData() {
		String sfx_export_file = "/mnt/bigdrive/bibdata/LehighData/sfx_export_portfolios.csv";
		String line = "";
		Map<String, List<Map<String, String>>> sfxdata_by_issn = new HashMap<String, List<Map<String, String>>>();
		Map<String, List<Map<String, String>>> sfxdata_by_eissn = new HashMap<String, List<Map<String, String>>>();
		Map<String, List<Map<String, String>>> sfxdata_by_lccn = new HashMap<String, List<Map<String, String>>>();
		Map<String, String> issn_map;
		int limit = -1, curr = 0, issn = 0, sfx_no_matching_data = 0;
		String headers[], pieces[];
		try {
			BufferedReader sfx_reader = new BufferedReader(new FileReader(sfx_export_file));
			line = sfx_reader.readLine();
			headers = line.split("\t");
			List<Map<String, String>> tmpList = new ArrayList<Map<String, String>>();
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
					if ( pieces[0].length() == 0 &&
						 pieces[1].length() == 0 &&
						 pieces[4].length() == 0 ) {
						sfx_no_matching_data++;
					}
				}
			}
			
			String filename = "/mnt/bigdrive/bibdata/sirsidump/20140215/mod.catalog.marcxml";
			final String ELECTRONIC_RESOURCE = "WWW";
			Reader input;
			try {
				input = new FileReader(filename);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				input = null;
			}
			InputSource inputsource = new InputSource(input);
			inputsource.setEncoding("ISO-8859-1");
			//MarcReader reader = new MarcStreamReader(input, "ISO-8859-1");
			//MarcXmlReader reader = new MarcXmlReader(new FileInputStream(dumpdir + "/" + args[2]), "UTF-8");
			MarcXmlReader reader = new MarcXmlReader(inputsource);
			List<String> rectypes = new ArrayList<String>();
			int counter = 0, no_sfx_data = 0, no_022_field = 0, has_sfx_data = 0;
			List<Character> holdingsTypes = Arrays.asList('u', 'v', 'x', 'y');
			Record xmlrecord, nextrecord;
			nextrecord = reader.next();
			List<Record> assocMFHDRecords = new ArrayList<Record>();
			List<Coverage> coverages;
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

				List<VariableField> itemsholdings = xmlrecord.getVariableFields("999");
				VariableField catalog_issns = xmlrecord.getVariableField("022");
				VariableField catalog_lccns = xmlrecord.getVariableField("010");
				List<VariableField> eholdings = new ArrayList<VariableField>();

				//for( List<String> callNumberFields : callNumberStrings ) {
				Map<String, List<String>> subfields;		
				List<Map<String, String>> sfxdata;
				
				for ( VariableField itemholdings : itemsholdings ) {
					subfields = LU_BuildInstance.getSubfields(itemholdings);
					List<String> locs = subfields.get("$l");
					if ( locs == null || locs.size() < 1 ) {
						System.err.println("Item with no l subfield: " + xmlrecord.getControlNumber() + ", " + itemholdings.toString());
					} else {
						if ( locs.get(0).equals(ELECTRONIC_RESOURCE) ) {
							eholdings.add(itemholdings);
							// Lookup the ISSNs to see if we have them from SFX
							//System.out.println("Searching for SFX data for catalog record " + xmlrecord.getControlNumber() + " by ISSN ...");
							sfxdata = findSFXData(catalog_issns, sfxdata_by_issn); 
							if (  sfxdata == null ) {
								//System.out.println("No SFX data by ISSN for catalog record " + xmlrecord.getControlNumber() + ", trying by eISSN ...");
								sfxdata = findSFXData(catalog_issns, sfxdata_by_eissn);
								if ( sfxdata == null ) {
									//System.out.println("No SFX data by eISSN for catalog record " + xmlrecord.getControlNumber() + ", trying by LCCN ...");
									sfxdata = findSFXData(catalog_lccns, sfxdata_by_lccn);
									if (  sfxdata == null ) {
										//System.out.println("No SFX data by LCCN  for catalog record " + xmlrecord.getControlNumber());	
										no_sfx_data++;
									} else {
										System.out.println("SFX data found by LCCN for catalog record " + xmlrecord.getControlNumber());										
										coverages = buildCoverages(sfxdata);
										printCoverages(coverages, System.out);
										has_sfx_data++;
									}
								} else {
									System.out.println("SFX data found by eISSN for catalog record " + xmlrecord.getControlNumber());
									coverages = buildCoverages(sfxdata);
									printCoverages(coverages, System.out);
									has_sfx_data++;
								}
							} else {
								System.out.println("SFX data found by ISSN for catalog record " + xmlrecord.getControlNumber());
								coverages = buildCoverages(sfxdata);
								printCoverages(coverages, System.out);
								has_sfx_data++;
							}
						}
					}
				}

				counter++;
				if ( counter % 100000 == 0 ) {
					System.err.println("Processed " + counter + " records");
				}
			} while(nextrecord != null && (limit < 0 || counter < limit) );
			System.out.println("Number of SFX records with ISSNs: " + sfxdata_by_issn.size());
			System.out.println("Number of SFX records with eISSNs: " + sfxdata_by_eissn.size());
			System.out.println("Number of SFX records with LCCNs: " + sfxdata_by_lccn.size());
			System.out.println("Number of SFX records with no data to match on: " + sfx_no_matching_data);
			System.out.println("Number of electronic catalog records with no SFX data: " + no_sfx_data);
			System.out.println("Number of electronic catalog records with SFX data: " + has_sfx_data);
			System.out.println("Done processing records");
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public static void matchHoldingsRecordsWithSFX() {
		String sfx_export_file = "/mnt/bigdrive/bibdata/LehighData/sfx_export_portfolios.csv";
		String line = "";
		Map<String, List<Map<String, String>>> sfxdata_by_issn = new HashMap<String, List<Map<String, String>>>();
		Map<String, List<Map<String, String>>> sfxdata_by_eissn = new HashMap<String, List<Map<String, String>>>();
		Map<String, List<Map<String, String>>> sfxdata_by_lccn = new HashMap<String, List<Map<String, String>>>();
		Map<String, String> issn_map;
		int limit = -1, curr = 0, issn = 0, sfx_no_matching_data = 0;
		String headers[], pieces[];
		try {
			PrintWriter no_sfx_matching_fields = new PrintWriter("/mnt/bigdrive/bibdata/LehighData/sfx_no_matching_fields.csv");
			BufferedReader sfx_reader = new BufferedReader(new FileReader(sfx_export_file));
			line = sfx_reader.readLine();
			headers = line.split("\t");
			no_sfx_matching_fields.println(line);
			List<Map<String, String>> tmpList = new ArrayList<Map<String, String>>();
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
					if ( pieces[0].length() == 0 &&
						 pieces[1].length() == 0 &&
						 pieces[4].length() == 0 ) {
						sfx_no_matching_data++;
						no_sfx_matching_fields.println(line);
					}
				}
			}
			
			String filename = "/mnt/bigdrive/bibdata/sirsidump/20140215/mod.catalog.marcxml";
			final String ELECTRONIC_RESOURCE = "WWW";
			Reader input;
			try {
				input = new FileReader(filename);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				input = null;
			}
			InputSource inputsource = new InputSource(input);
			inputsource.setEncoding("ISO-8859-1");
			//MarcReader reader = new MarcStreamReader(input, "ISO-8859-1");
			//MarcXmlReader reader = new MarcXmlReader(new FileInputStream(dumpdir + "/" + args[2]), "UTF-8");
			MarcXmlReader reader = new MarcXmlReader(inputsource);
			List<String> rectypes = new ArrayList<String>();
			int counter = 0, no_sfx_data = 0, no_022_field = 0, has_sfx_data = 0;
			List<Character> holdingsTypes = Arrays.asList('u', 'v', 'x', 'y');
			Record xmlrecord, nextrecord;
			nextrecord = reader.next();
			List<Record> assocMFHDRecords = new ArrayList<Record>();
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

				List<VariableField> itemsholdings = xmlrecord.getVariableFields("999");
				VariableField catalog_issns = xmlrecord.getVariableField("022");
				VariableField catalog_lccns = xmlrecord.getVariableField("010");
				List<VariableField> eholdings = new ArrayList<VariableField>();

				//for( List<String> callNumberFields : callNumberStrings ) {
				Map<String, List<String>> subfields;		
				List<Map<String, String>> sfxdata;
				
				for ( VariableField itemholdings : itemsholdings ) {
					subfields = LU_BuildInstance.getSubfields(itemholdings);
					List<String> locs = subfields.get("$l");
					if ( locs == null || locs.size() < 1 ) {
						System.err.println("Item with no l subfield: " + xmlrecord.getControlNumber() + ", " + itemholdings.toString());
					} else {
						if ( locs.get(0).equals(ELECTRONIC_RESOURCE) ) {
							eholdings.add(itemholdings);
							// Lookup the ISSNs to see if we have them from SFX
							//System.out.println("Searching for SFX data for catalog record " + xmlrecord.getControlNumber() + " by ISSN ...");
							sfxdata = findSFXData(catalog_issns, sfxdata_by_issn); 
							if (  sfxdata == null ) {
								//System.out.println("No SFX data by ISSN for catalog record " + xmlrecord.getControlNumber() + ", trying by eISSN ...");
								sfxdata = findSFXData(catalog_issns, sfxdata_by_eissn);
								if ( sfxdata == null ) {
									//System.out.println("No SFX data by eISSN for catalog record " + xmlrecord.getControlNumber() + ", trying by LCCN ...");
									sfxdata = findSFXData(catalog_lccns, sfxdata_by_lccn);
									if (  sfxdata == null ) {
										//System.out.println("No SFX data by LCCN  for catalog record " + xmlrecord.getControlNumber());	
										no_sfx_data++;
									} else {
										System.out.println("SFX data found by LCCN for catalog record " + xmlrecord.getControlNumber());
										printSFXData(sfxdata, xmlrecord, assocMFHDRecords, System.out);
										has_sfx_data++;
									}
								} else {
									System.out.println("SFX data found by eISSN for catalog record " + xmlrecord.getControlNumber());
									printSFXData(sfxdata, xmlrecord, assocMFHDRecords, System.out);
									has_sfx_data++;
								}
							} else {
								System.out.println("SFX data found by ISSN for catalog record " + xmlrecord.getControlNumber());
								printSFXData(sfxdata, xmlrecord, assocMFHDRecords, System.out);
								has_sfx_data++;
							}
						}
					}
				}

				counter++;
				if ( counter % 100000 == 0 ) {
					System.err.println("Processed " + counter + " records");
				}
			} while(nextrecord != null && (limit < 0 || counter < limit) );
			System.out.println("Number of SFX records with ISSNs: " + sfxdata_by_issn.size());
			System.out.println("Number of SFX records with eISSNs: " + sfxdata_by_eissn.size());
			System.out.println("Number of SFX records with LCCNs: " + sfxdata_by_lccn.size());
			System.out.println("Number of SFX records with no data to match on: " + sfx_no_matching_data);
			System.out.println("Number of electronic catalog records with no SFX data: " + no_sfx_data);
			System.out.println("Number of electronic catalog records with SFX data: " + has_sfx_data);
			System.out.println("Done processing records");
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public static void printSFXData(List<Map<String, String>> sfxdata_list, Record record, List<Record> assocMFHDRecords, PrintStream out) {
		out.println("Catalog record: " + record.getControlNumber());
		out.println(record.toString());
		out.println("Associated holdings records: ");
		for ( Record mfhdrec : assocMFHDRecords ) {
			out.println(mfhdrec.toString());
		}
		out.println(sfxdata_list.size() + " SFX records found.");
		for ( Map<String, String> sfxdata : sfxdata_list ) {
			out.println("SFX data: ");
			for ( String key : sfxdata.keySet() ) {
				if ( !key.equals("line") ) {
					out.println("\t" + key + ": " + sfxdata.get(key));
				}
			}
		}
		out.println();
		out.println();
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

	public static void testReplaceXML() {
		String marcXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><collection xmlns=\"http://www.loc.gov/MARC21/slim\"><record><leader>01031pam  2200289Si 45 0</leader><controlfield tag=\"001\">a1</controlfield><controlfield tag=\"002\">00000003</controlfield><controlfield tag=\"003\">SIRSI</controlfield><controlfield tag=\"008\">781207s1979    enkac    b    001 0 eng  </controlfield><datafield tag=\"010\" ind1=\" \" ind2=\" \"><subfield code=\"a\">   78041047 </subfield></datafield><datafield tag=\"020\" ind1=\" \" ind2=\" \"><subfield code=\"a\">0192850830 :</subfield><subfield code=\"c\">?2.50</subfield></datafield><datafield tag=\"040\" ind1=\" \" ind2=\" \"><subfield code=\"a\">DLC</subfield><subfield code=\"c\">DLC</subfield><subfield code=\"d\">LYU</subfield></datafield><datafield tag=\"043\" ind1=\" \" ind2=\" \"><subfield code=\"a\">e------</subfield></datafield><datafield tag=\"049\" ind1=\" \" ind2=\" \"><subfield code=\"a\">LYUU</subfield></datafield><datafield tag=\"050\" ind1=\"1\" ind2=\" \"><subfield code=\"a\">CB203</subfield><subfield code=\"b\">.S33 1979</subfield></datafield><datafield tag=\"092\" ind1=\" \" ind2=\" \"><subfield code=\"a\">901.934</subfield><subfield code=\"b\">S324m, 1979</subfield></datafield><datafield tag=\"099\" ind1=\" \" ind2=\" \"><subfield code=\"a\">901.934 S324m, 1979</subfield></datafield><datafield tag=\"100\" ind1=\"1\" ind2=\" \"><subfield code=\"a\">Schenk, Hans Georg Artur Viktor,</subfield><subfield code=\"d\">1912-</subfield><subfield code=\"=\">^A372418</subfield></datafield><datafield tag=\"245\" ind1=\"1\" ind2=\"4\"><subfield code=\"a\">The mind of the European romantics :</subfield><subfield code=\"b\">an essay in cultural history /</subfield><subfield code=\"c\">by H. G. Schenk.</subfield></datafield><datafield tag=\"260\" ind1=\" \" ind2=\" \"><subfield code=\"a\">Oxford ;</subfield><subfield code=\"a\">New York :</subfield><subfield code=\"b\">Oxford University Press,</subfield><subfield code=\"c\">1979.</subfield></datafield><datafield tag=\"300\" ind1=\" \" ind2=\" \"><subfield code=\"a\">xxiv, 303 p., [7] leaves of plates :</subfield><subfield code=\"b\">ports ;</subfield><subfield code=\"c\">20 cm.</subfield></datafield><datafield tag=\"440\" ind1=\" \" ind2=\"0\"><subfield code=\"a\">Oxford paperbacks</subfield><subfield code=\"=\">^A421667</subfield></datafield><datafield tag=\"500\" ind1=\" \" ind2=\" \"><subfield code=\"a\">Includes index.</subfield></datafield><datafield tag=\"504\" ind1=\" \" ind2=\" \"><subfield code=\"a\">Bibliography: p. 248-284.</subfield></datafield><datafield tag=\"650\" ind1=\" \" ind2=\"0\"><subfield code=\"a\">Romanticism</subfield><subfield code=\"z\">Europe.</subfield><subfield code=\"=\">^A560516</subfield></datafield><datafield tag=\"651\" ind1=\" \" ind2=\"0\"><subfield code=\"a\">Europe</subfield><subfield code=\"x\">Intellectual life.</subfield><subfield code=\"=\">^A273975</subfield></datafield><datafield tag=\"999\" ind1=\" \" ind2=\" \"><subfield code=\"a\">901.934 S324m 1979</subfield><subfield code=\"w\">DEWEYSAN</subfield><subfield code=\"c\">1</subfield><subfield code=\"i\">39151002278923</subfield><subfield code=\"d\">5/25/1995</subfield><subfield code=\"l\">L-3-STACKS</subfield><subfield code=\"m\">LEHIGH</subfield><subfield code=\"p\">$2.50</subfield><subfield code=\"r\">Y</subfield><subfield code=\"s\">Y</subfield><subfield code=\"t\">BOOK</subfield><subfield code=\"u\">5/25/1995</subfield></datafield></record></collection>";
		String xmldecl = "<\\?xml version=\"1.0\" encoding=\"UTF-8\"\\?>";
		marcXML = marcXML.replaceFirst(xmldecl, "");
		System.out.println("XML is now: " + marcXML);		
	}
	
	public static void checkHoldingsRecords() {
		String filename = "/mnt/bigdrive/bibdata/sirsidump/20131217/mod.catalog.marcxml";
		final String ELECTRONIC_RESOURCE = "WWW";
		Reader input;
		try {
			input = new FileReader(filename);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			input = null;
		}
		InputSource inputsource = new InputSource(input);
		inputsource.setEncoding("ISO-8859-1");
		//MarcReader reader = new MarcStreamReader(input, "ISO-8859-1");
		//MarcXmlReader reader = new MarcXmlReader(new FileInputStream(dumpdir + "/" + args[2]), "UTF-8");
		MarcXmlReader reader = new MarcXmlReader(inputsource);
		List<String> rectypes = new ArrayList<String>();
		int counter = 0, limit = -1;
		List<Character> holdingsTypes = Arrays.asList('u', 'v', 'x', 'y');
		Record xmlrecord, nextrecord;
		nextrecord = reader.next();
		List<Record> assocMFHDRecords = new ArrayList<Record>();
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

			List<VariableField> itemsholdings = xmlrecord.getVariableFields("999");
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
					System.err.println("Item with no l subfield: " + xmlrecord.getControlNumber() + ", " + itemholdings.toString());
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
				if ( !rectypes.contains(rectype) ) {
					System.err.println("New rectype for bib " + xmlrecord.getControlNumber() + ": " + rectype);
					rectypes.add(rectype);
				}
			}			
			
			if ( onlineMFHDRecords.size() > 1 ) {
				//System.err.println("There are " + onlineMFHDRecords.size() + " electronic MFHD records for bib " + xmlrecord.getControlNumber());
			}
			
			if ( eholdings.size() > 1 ) {
				//System.err.println("There are " + eholdings.size() + " electronic 999 records for bib " + xmlrecord.getControlNumber());
			}
			
			if ( printMFHDRecords.size() > 1 ) {
				System.err.println("There are " + printholdings.size() + " print MFHD records for bib " + xmlrecord.getControlNumber());
			}
			counter++;
			if ( counter % 100000 == 0 ) {
				System.out.println("Processed " + counter + " records");
			}
		} while(nextrecord != null && (limit < 0 || counter < limit) );
		System.out.println("Done processing records");
	}
	
	public static void testMatcher() {
		String url = "http://purl.access.gpo.gov/GPO/LPS83904";
		String pattern = "^http://purl.*";
		if ( url.matches(pattern) ) {
			System.out.println(url + " matches pattern");
		} else {
			System.out.println(url + " does NOT match pattern");
		}
		
		url = "http://a.purl.stuff";
		if ( url.matches(pattern) ) {
			System.out.println(url + " matches pattern");
		} else {
			System.out.println(url + " does NOT match pattern");
		}
		
	}
	
	public static void testParseCoverage() {
		String[] tests = new String[]{"Electronic journal: v.1-17 1972-1989",
                "1-17 (1972-1989)",
                "Electronic conference: 2- 1976-",
                "2,5 (1975-1979)",
                "3,6,9- (1978,1982,1987-)",
                "Electronic resource: 1982-",
                "(1975-1993)",
                "2 (1978)",
                "(1979)",
                "51-65 (1977-1991)",
                "1-4, 6-18 (1979-1982,1984-1996)",
                "Electronic journal: v.1- 1936-",
                "Electronic journal: v.27,no.10-v.33,no.1 2005-2009",
                "2-9,11-21 (1980-2006)",
                "3-10,12-18,20-21,23- (1974-1981,1986-1993,1995-1996,1998-)",
                "Electronic journal: v. 1-12;Ser.2: v. 1-12;Ser.3: v. 1-12;Ser.4:v.1-4; 43- 1849/50-1855;1856-1861;1862-1867;1868-1869; 1996-",
                };
		String token = "";
		Tokenizer tokenizer = new Tokenizer();
		CoverageParser cp = new CoverageParser();
		for( String ownershipstr : tests ) {
			System.out.println("Test string: " + ownershipstr);
			tokenizer.setStr(ownershipstr);
			cp.setTokenizer(tokenizer);
			cp.parse();
			for ( Coverage c : cp.getCoverages() ) {
				System.out.println("Coverage: " + c.toString());
			}
			cp.getCoverages().clear();
			System.out.println();
		}
	}

	public static void testTokenizer() {
		String[] tests = new String[]{"Electronic journal: v.1-17 1972-1989",
                "1-17 (1972-1989)",
                "Electronic conference: 2- 1976-",
                "2,5 (1975-1979)",
                "3,6,9- (1978,1982,1987-)",
                "Electronic resource: 1982-",
                "(1975-1993)",
                "2 (1978)",
                "(1979)",
                "51-65 (1977-1991)",
                "1-4, 6-18 (1979-1982,1984-1996)",
                "Electronic journal: v.1- 1936-",
                "Electronic journal: v.27,no.10-v.33,no.1 2005-2009",
                "2-9,11-21 (1980-2006)",
                "3-10,12-18,20-21,23- (1974-1981,1986-1993,1995-1996,1998-)",
                "Electronic journal: v. 1-12;Ser.2: v. 1-12;Ser.3: v. 1-12;Ser.4:v.1-4; 43- 1849/50-1855;1856-1861;1862-1867;1868-1869; 1996-",
                };
		String token = "";
		Tokenizer tokenizer = new Tokenizer();
		for ( String test : tests ) {

			System.out.println("String: " + test);
			System.out.print("Tokens: ");
			tokenizer.setStr(test);
			do {
				token = tokenizer.nextToken();
				System.out.print(token + "(" + tokenizer.getPos() + ")");
			} while(tokenizer.getPos() < test.length()); 
			System.out.println();
			
		}
	}
	
	public static void testMatcher2() {
		Matcher m;
		// Electronic journal: v.1-17 1972-1989
		// 1-17 (1972-1989)
		Pattern p1 = Pattern.compile(".*(\\d+)-(\\d+)\\s+\\(?(\\d+)-(\\d+)\\)?.*");
		// Electronic conference: 2- 1976-
		Pattern p2 = Pattern.compile(".*(\\d+)-\\s+(\\d+)-[\\s]*$");
		
		Pattern p3 = Pattern.compile("^(\\d+)-(\\d+)\\s+\\((\\d+)-(\\d+)\\)");
		// 2,5 (1975-1979)
		// 3,6,9- (1978,1982,1987-)
		// If this pattern matches, have to loop over the string,
		// can't match comma separated list and parse them out with a regex.
		// That's context-free, need a stack ...
		Pattern p4= Pattern.compile("^(\\d+[,-])+(\\d+)[-]?\\s+\\((\\d+[,-])+(\\d+)[-]?\\)");  
		// Electronic resource: 1982-
		// (1975-1993)
		// This one matches a lot, try it last
		Pattern p5 = Pattern.compile(".*[\\(]?(\\d+)-(\\d+)?\\)\\s+$");
		// 2 (1978)
		// (1979)
		Pattern p6 = Pattern.compile(".*(\\d+)?\\s+[\\(]?(\\d+)[\\)]?");
		
		Pattern patterns[] = { p1, p2, p3, p4, p5, p6 };
		String[] tests = new String[]{"Electronic journal: v.1-17 1972-1989",
		                              "1-17 (1972-1989)",
		                              "Electronic conference: 2- 1976-",
		                              "2,5 (1975-1979)",
		                              "3,6,9- (1978,1982,1987-)",
		                              "Electronic resource: 1982-",
		                              "(1975-1993)",
		                              "2 (1978)",
		                              "(1979)",
		                              "51-65 (1977-1991)",
		                              "1-4, 6-18 (1979-1982,1984-1996)",
		                              "Electronic journal: v.1- 1936-",
		                              "Electronic journal: v.27,no.10-v.33,no.1 2005-2009",
		                              "2-9,11-21 (1980-2006)",
		                              "3-10,12-18,20-21,23- (1974-1981,1986-1993,1995-1996,1998-)",
		                              "Electronic journal: v. 1-12;Ser.2: v. 1-12;Ser.3: v. 1-12;Ser.4:v.1-4; 43- 1849/50-1855;1856-1861;1862-1867;1868-1869; 1996-",
		                              };
		for ( String test : tests ) {
			
			for ( int i = 0; i < patterns.length; i++ ) {
				m = patterns[i].matcher(test);
				if ( m.find() ) {
					System.out.println("String \"" + test + "\" matches pattern " + (i+1) + ", groups: ");
					for ( int j = 0; j <= m.groupCount(); j++ ) {
						System.out.println((j+1) + ": " + m.group(j));
					}					
					break;
				} else {
					System.out.println("No match on pattern " + (i+1) + " for string \"" + test + "\"");
				}
				System.out.println();
			}
			System.out.println();

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
}
