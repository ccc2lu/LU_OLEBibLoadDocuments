package edu.lu.oleconvert;

import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import migration.SirsiCallNumber;

import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.time.DateUtils;
import org.marc4j.MarcStreamReader;
import org.marc4j.MarcXmlReader;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.marc4j.marc.VariableField;
import org.marc4j.marc.impl.ControlFieldImpl;
import org.xml.sax.InputSource;

//import org.solrmarc.*;
//import org.solrmarc.callnum.DeweyCallNumber;
//import org.solrmarc.callnum.LCCallNumber;

import edu.lu.oleconvert.ole.Bib;
import edu.lu.oleconvert.ole.BoundWith;
import edu.lu.oleconvert.ole.Coverage;
import edu.lu.oleconvert.ole.OLEHoldings;
import migration.*;

public class test2 {

	public static void main(String args[]) {
		//testReplaceXML();
		//checkHoldingsRecords();
		//testTokenizer();
		//testParseCoverage();
		//testReadSFXData();
		//matchHoldingsRecordsWithSFX();
		//testDateAdd();
		//buildCoverageData();
		//System.out.println(UUID.randomUUID().toString());
		/*
		String uriStr = "jkey=sigcsim & url2=http://portal.acm.org/toc.cfm?id=J915 & code=1";
		uriStr = "url2=http://portal.acm.org/browse_dl.cfm?linked=1%26part=series%26idx=SERIES307%26coll=portal & code=1";
		uriStr = uriStr.substring(uriStr.indexOf("http:"));
		if ( uriStr.indexOf(" & ") > 0 ) {
			uriStr = uriStr.substring(0, uriStr.indexOf(" & "));
		}
		System.out.println("Uri: " + uriStr);
		*/
		
		/*String testisbn = "192512951";
		System.out.println("New isbn: " + LU_BuildInstance.formatISBNString(testisbn));
		testisbn = "224613962";
		System.out.println("New isbn: " + LU_BuildInstance.formatISBNString(testisbn));
		*/
		//fixISBNs();
		/*
		try {
			testCheckTitleControlNumbers();
		} catch (Exception e) {
			System.err.println("Exception: " + e.getMessage());
			e.printStackTrace();
		}
		*/
		//testNormalizeCallNumbers();
		//testSplit();
		//testBWLoad();
		
		/*
		try {
			testRead2();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		try {
			checkCallNumberPattern();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void checkCallNumberPattern() throws FileNotFoundException {
		Reader input = new FileReader("/mnt/bigdrive/bibdata/sirsidump/20140712/mod.catalog.marcxml");
		InputSource inputsource = new InputSource(input);
		//inputsource.setEncoding("ISO-8859-1");
		inputsource.setEncoding("UTF-8");
		//MarcReader reader = new MarcStreamReader(input, "ISO-8859-1");
		//MarcXmlReader reader = new MarcXmlReader(new FileInputStream(dumpdir + "/" + args[2]), "UTF-8");
		MarcXmlReader reader = new MarcXmlReader(inputsource);
		Record record;
		Pattern vol_pattern = Pattern.compile(".*\\w+([A-Za-z]{1,2}\\.\\d+)$");
		Matcher m;
		do {
			record = reader.next();
			List<DataField> datafields = record.getDataFields();
			for ( DataField df : datafields ) {
				if ( df.getTag().equals("999") ) {
					String callnum = df.getSubfield('a').getData().trim();
					System.out.println("Testing callnumber " + callnum);
					m = vol_pattern.matcher(callnum);
					if ( m.find() ) {
						System.out.println("Callnumber matching pattern: " + callnum);
					}
				}
			}
		} while (reader.hasNext());
	}
	public static void testRead2() throws FileNotFoundException {
		Reader input = new FileReader("/mnt/bigdrive/bibdata/sirsidump/20140710/mod.catalog.marcxml");
		InputSource inputsource = new InputSource(input);
		//inputsource.setEncoding("ISO-8859-1");
		inputsource.setEncoding("UTF-8");
		//MarcReader reader = new MarcStreamReader(input, "ISO-8859-1");
		//MarcXmlReader reader = new MarcXmlReader(new FileInputStream(dumpdir + "/" + args[2]), "UTF-8");
		MarcXmlReader reader = new MarcXmlReader(inputsource);
		InputStream defaultkeysInput = new FileInputStream("/mnt/bigdrive/bibdata/sirsidump/20140710/20140710/catalog.defaultkey.mrc");
		MarcStreamReader defaultkeysReader = new MarcStreamReader(defaultkeysInput);
				
		int limit = -1;
		int counter = 0;
		List<Character> holdingsTypes = Arrays.asList('u', 'v', 'x', 'y');
		Record xmlrecord, nextrecord, defaultkeysrecord, dfknextrecord;
		nextrecord = reader.next();
		dfknextrecord = defaultkeysReader.next();
		List<Record> assocMFHDRecords = new ArrayList<Record>();

		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		do {
			
			assocMFHDRecords.clear();
			xmlrecord = nextrecord;
			defaultkeysrecord = dfknextrecord;
			LU_BuildInstance.fixISBN(xmlrecord);
			
			nextrecord = reader.next();
			if ( nextrecord != null ) {
				dfknextrecord = defaultkeysReader.next();
			}
			// The associated holdings records for a bib record should always come right after it
			// So we keep looping and adding them to an ArrayList as we go
			while ( nextrecord != null && 
					holdingsTypes.contains(nextrecord.getLeader().getTypeOfRecord()) ) {

				assocMFHDRecords.add(nextrecord);
				nextrecord = reader.next();
				if ( nextrecord != null ) {
					dfknextrecord = defaultkeysReader.next();
				}
			}
			// If subsequent records have the same bib ID in the 001 field,
			// then we're going to append all the 999 fields of those records to xmlrecord, 
			// and skip the records with the same bib ID
			while ( nextrecord != null &&
					nextrecord.getControlNumber().equals(xmlrecord.getControlNumber())) {
				LU_DBLoadInstances.Log(System.out, "Two record with same control number, first: ", LU_DBLoadInstances.LOG_INFO);
				LU_DBLoadInstances.Log(System.out, xmlrecord.toString(), LU_DBLoadInstances.LOG_INFO);
				LU_DBLoadInstances.Log(System.out, "Second: ", LU_DBLoadInstances.LOG_INFO);
				LU_DBLoadInstances.Log(System.out, nextrecord.toString(), LU_DBLoadInstances.LOG_INFO);
				LU_DBLoadInstances.Log(System.out, "Appending 999s from second record to first", LU_DBLoadInstances.LOG_INFO);
				LU_BuildInstance.append999fields(xmlrecord, nextrecord);
				LU_DBLoadInstances.Log(System.out, "First record is now: ", LU_DBLoadInstances.LOG_INFO);
				LU_DBLoadInstances.Log(System.out, xmlrecord.toString(), LU_DBLoadInstances.LOG_INFO);
				nextrecord = reader.next();
				if ( nextrecord != null ) {
					dfknextrecord = defaultkeysReader.next();
				}
			}
			


			counter++;
			if ( counter % 50000 == 0 || ( limit > 0 && counter >= limit )) {
				LU_DBLoadInstances.Log(System.out, counter + " records loaded ...", LU_DBLoadInstances.LOG_INFO);
				LU_DBLoadInstances.Log(System.out, "XML record: " + xmlrecord.toString(), LU_DBLoadInstances.LOG_INFO);
				LU_DBLoadInstances.Log(System.out, "Default keys record: " + defaultkeysrecord.toString(), LU_DBLoadInstances.LOG_INFO);
			}
		} while (nextrecord != null && (limit < 0 || counter < limit) );
		LU_DBLoadInstances.Log(System.out, "Last XML record: " + xmlrecord.toString(), LU_DBLoadInstances.LOG_INFO);
		LU_DBLoadInstances.Log(System.out, "Last default keys record: " + defaultkeysrecord.toString(), LU_DBLoadInstances.LOG_INFO);

	}
	
	public static void testRead() throws FileNotFoundException {

		Reader input = new FileReader("/mnt/bigdrive/bibdata/sirsidump/20140710/mod.catalog.marcxml");
		InputSource inputsource = new InputSource(input);
		//inputsource.setEncoding("ISO-8859-1");
		inputsource.setEncoding("UTF-8");
		//MarcReader reader = new MarcStreamReader(input, "ISO-8859-1");
		//MarcXmlReader reader = new MarcXmlReader(new FileInputStream(dumpdir + "/" + args[2]), "UTF-8");
		MarcXmlReader reader = new MarcXmlReader(inputsource);
		InputStream defaultkeysInput = new FileInputStream("/mnt/bigdrive/bibdata/sirsidump/20140710/20140710/catalog.defaultkey.mrc");
		MarcStreamReader defaultkeysReader = new MarcStreamReader(defaultkeysInput);

		Record record, dfkrecord;
		int count = 0;
		record = reader.next();
		dfkrecord = defaultkeysReader.next();
		do  {
			count++;
			if ( count % 50000 == 0 ) {
				System.out.println("Processed " + count + " record: " + record.toString());
			}
			record = reader.next();
			dfkrecord = defaultkeysReader.next();
		} while ( reader.hasNext() );
		System.out.println("Count is: " + count + ", last record: " + record.toString());		
	}
	
	public static void countRecs() throws FileNotFoundException {

		Reader input = new FileReader("/mnt/bigdrive/bibdata/sirsidump/20140710/mod.catalog.marcxml");
		InputSource inputsource = new InputSource(input);
		//inputsource.setEncoding("ISO-8859-1");
		inputsource.setEncoding("UTF-8");
		//MarcReader reader = new MarcStreamReader(input, "ISO-8859-1");
		//MarcXmlReader reader = new MarcXmlReader(new FileInputStream(dumpdir + "/" + args[2]), "UTF-8");
		MarcXmlReader reader = new MarcXmlReader(inputsource);
		Record record;
		int count = 0;
		record = reader.next();
		do  {
			count++;
			if ( count % 50000 == 0 ) {
				System.out.println("Processed " + count + " record: " + record.toString());
			}
			record = reader.next();
		} while ( reader.hasNext() );
		System.out.println("Count is: " + count + ", last record: " + record.toString());
		InputStream defaultkeysInput = new FileInputStream("/mnt/bigdrive/bibdata/sirsidump/20140710/20140710/catalog.defaultkey.mrc");
		MarcStreamReader defaultkeysReader = new MarcStreamReader(defaultkeysInput);
		count = 0;
		record = defaultkeysReader.next();
		do {
			count++;
			if ( count % 50000 == 0 ) {
				System.out.println("Processed " + count + " record: " + record.toString());
			}		
			record = defaultkeysReader.next();

		} while ( defaultkeysReader.hasNext() );
		System.out.println("Count is: " + count + ", last record: " + record.toString());
	}
	public static void testBWLoad() {

		EntityManagerFactory ole_emf;
		EntityManager ole_em;
		EntityManagerFactory migration_emf;
		EntityManager migration_em;
		
		ole_emf = Persistence.createEntityManagerFactory("ole");
		ole_em = ole_emf.createEntityManager();
		EntityTransaction ole_tx = ole_em.getTransaction();
		
		migration_emf = Persistence.createEntityManagerFactory("olemigration");
		migration_em = migration_emf.createEntityManager();
		
		TypedQuery<SirsiCallNumber> query = migration_em.createQuery("select scn from SirsiCallNumber scn where scn.level='CHILD'", SirsiCallNumber.class);
		query.setHint("org.hibernate.cacheable", true);
		List<SirsiCallNumber> results = query.getResultList();
		Iterator it = results.iterator();
		while ( it.hasNext() ) {
			SirsiCallNumber scn = (SirsiCallNumber) it.next();
			// Check if this is a child record, and if so, create a bound-with
			if ( scn.getLevel().equals("CHILD") ) {
				// bib id will be the cat key for the parent, holdings id we'll have to retrieve
				// though using 
				TypedQuery<OLEHoldings> holdings_query = ole_em.createQuery("select oh from OLEHoldings oh where oh.formerId='" + scn.getParent_cat_key() + "|" + scn.getParent_callnum_key() + "'", OLEHoldings.class);
				List<OLEHoldings> holdings_results = holdings_query.getResultList();
				if ( holdings_results.size() > 0 ) {
					ole_tx.begin();
					BoundWith bw = new BoundWith();
					bw.setBibId((long)scn.getParent_cat_key());
					OLEHoldings oh = holdings_results.get(0);
					bw.setHoldingsId(oh.getHoldingsIdentifier());
					ole_em.persist(bw);
					ole_tx.commit();
				} else {
					LU_DBLoadInstances.Log(System.err, "No holdings record found for former ID " + scn.getParent_cat_key() + "|" + scn.getParent_callnum_key(),
							LU_DBLoadInstances.LOG_WARN);
				}
			}
		}
	}
	public static void testSplit() {
		String line = "120710|6|315629|1|CATSU|20080102|LEHIGH|";
		String[] fields = line.split("\\|");
		for ( int i = 0; i < fields.length; i++ ) {
			System.out.println("Field " + i + " = " + fields[i]);
		}
	}
	public static void testNormalizeCallNumbers() {
		String ddc_callnumbers[] = { "901.934 S324m 1979", 
				"557 R113m v.1",
				"624.1513 Y54s",
				"624.152 S933",
				"C 13.29/2:122",
				"551.51 H966",
				"C 13.29/2:124",
				"624.1513 L322",
				"614.83 A799 v.1"
				}; 
		String sudoc_callnumbers[] = { "C 13.29/2:121",
				 "C 13.29/2:122",
				 "C 13.29/2:124",
				 "A 1.76:521/2",
				 "LC 41.9:Ar 1",
				 "HE 20.3173/2:CK 21/",
				 "JU 10.8:",
				 "FILM",
				 "TD 4.210: 1977-1981"
		};
		String lcc_callnumbers[] = { "ML410 .M9 O9 1980",
				"ML410 .M23 B23 1980",
				"ML1156 .R67 1980",
				"ML1700 .D75",
				"M2018 .M32 O6",
				"ML113 .H52 1980 v.1",
				"ML113 .H52 1980 v.2",
				"ML3780 .G74",
				"MT145 .M7 F67 1971b",
				"MT50.G643 H4 1965",
				"MT50.G643 H4 1965",
				"MT145 .D289 S3 1966",
				"ML3858 .M58",
				"ML1731 .F45",
				"M1500 .W15 T6 1973",
				"M1500 .W15 T6 1973",
				"M1500 .W15 T6 1973",
				"M1500 .W15 T6 1973",
				"PT2361 .Z5 N4 1965"
		};

		System.out.println("Normalizing Dewey callnumbers");
		for ( String ddc_cn : Arrays.asList(ddc_callnumbers)) {
			DeweyCallNumber ddc = new DeweyCallNumber(ddc_cn);
			System.out.println("Normal form of " + ddc_cn + " is " + LU_BuildInstance.normalizeCallNumber(ddc_cn, "DDC"));
		}
		
		System.out.println();
		System.out.println("Normalizing LoC callnumbers");
		for (String lcc_cn : Arrays.asList(lcc_callnumbers)) {
			System.out.println("Normal form of " + lcc_cn + " is " + LU_BuildInstance.normalizeCallNumber(lcc_cn, "LCC"));
		}
		
		System.out.println();
		System.out.println("Normalizing SuDoc callnumbers");
		for (String sudoc_cn : Arrays.asList(sudoc_callnumbers)) {
			System.out.println("Normal form of " + sudoc_cn + " is " + LU_BuildInstance.normalizeCallNumber(sudoc_cn, "SuDoc"));
		}
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
							sfxdata = LU_BuildInstance.findSFXData(catalog_issns, sfxdata_by_issn); 
							if (  sfxdata == null ) {
								//System.out.println("No SFX data by ISSN for catalog record " + xmlrecord.getControlNumber() + ", trying by eISSN ...");
								sfxdata = LU_BuildInstance.findSFXData(catalog_issns, sfxdata_by_eissn);
								if ( sfxdata == null ) {
									//System.out.println("No SFX data by eISSN for catalog record " + xmlrecord.getControlNumber() + ", trying by LCCN ...");
									sfxdata = LU_BuildInstance.findSFXData(catalog_lccns, sfxdata_by_lccn);
									if (  sfxdata == null ) {
										//System.out.println("No SFX data by LCCN  for catalog record " + xmlrecord.getControlNumber());	
										no_sfx_data++;
									} else {
										System.out.println("SFX data found by LCCN for catalog record " + xmlrecord.getControlNumber());										
										coverages = LU_BuildInstance.buildCoverages(sfxdata);
										printCoverages(coverages, System.out);
										has_sfx_data++;
									}
								} else {
									System.out.println("SFX data found by eISSN for catalog record " + xmlrecord.getControlNumber());
									coverages = LU_BuildInstance.buildCoverages(sfxdata);
									printCoverages(coverages, System.out);
									has_sfx_data++;
								}
							} else {
								System.out.println("SFX data found by ISSN for catalog record " + xmlrecord.getControlNumber());
								coverages = LU_BuildInstance.buildCoverages(sfxdata);
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
							sfxdata = LU_BuildInstance.findSFXData(catalog_issns, sfxdata_by_issn); 
							if (  sfxdata == null ) {
								//System.out.println("No SFX data by ISSN for catalog record " + xmlrecord.getControlNumber() + ", trying by eISSN ...");
								sfxdata = LU_BuildInstance.findSFXData(catalog_issns, sfxdata_by_eissn);
								if ( sfxdata == null ) {
									//System.out.println("No SFX data by eISSN for catalog record " + xmlrecord.getControlNumber() + ", trying by LCCN ...");
									sfxdata = LU_BuildInstance.findSFXData(catalog_lccns, sfxdata_by_lccn);
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

	public static void testReplaceXML() {
		String marcXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><collection xmlns=\"http://www.loc.gov/MARC21/slim\"><record><leader>01031pam  2200289Si 45 0</leader><controlfield tag=\"001\">a1</controlfield><controlfield tag=\"002\">00000003</controlfield><controlfield tag=\"003\">SIRSI</controlfield><controlfield tag=\"008\">781207s1979    enkac    b    001 0 eng  </controlfield><datafield tag=\"010\" ind1=\" \" ind2=\" \"><subfield code=\"a\">   78041047 </subfield></datafield><datafield tag=\"020\" ind1=\" \" ind2=\" \"><subfield code=\"a\">0192850830 :</subfield><subfield code=\"c\">?2.50</subfield></datafield><datafield tag=\"040\" ind1=\" \" ind2=\" \"><subfield code=\"a\">DLC</subfield><subfield code=\"c\">DLC</subfield><subfield code=\"d\">LYU</subfield></datafield><datafield tag=\"043\" ind1=\" \" ind2=\" \"><subfield code=\"a\">e------</subfield></datafield><datafield tag=\"049\" ind1=\" \" ind2=\" \"><subfield code=\"a\">LYUU</subfield></datafield><datafield tag=\"050\" ind1=\"1\" ind2=\" \"><subfield code=\"a\">CB203</subfield><subfield code=\"b\">.S33 1979</subfield></datafield><datafield tag=\"092\" ind1=\" \" ind2=\" \"><subfield code=\"a\">901.934</subfield><subfield code=\"b\">S324m, 1979</subfield></datafield><datafield tag=\"099\" ind1=\" \" ind2=\" \"><subfield code=\"a\">901.934 S324m, 1979</subfield></datafield><datafield tag=\"100\" ind1=\"1\" ind2=\" \"><subfield code=\"a\">Schenk, Hans Georg Artur Viktor,</subfield><subfield code=\"d\">1912-</subfield><subfield code=\"=\">^A372418</subfield></datafield><datafield tag=\"245\" ind1=\"1\" ind2=\"4\"><subfield code=\"a\">The mind of the European romantics :</subfield><subfield code=\"b\">an essay in cultural history /</subfield><subfield code=\"c\">by H. G. Schenk.</subfield></datafield><datafield tag=\"260\" ind1=\" \" ind2=\" \"><subfield code=\"a\">Oxford ;</subfield><subfield code=\"a\">New York :</subfield><subfield code=\"b\">Oxford University Press,</subfield><subfield code=\"c\">1979.</subfield></datafield><datafield tag=\"300\" ind1=\" \" ind2=\" \"><subfield code=\"a\">xxiv, 303 p., [7] leaves of plates :</subfield><subfield code=\"b\">ports ;</subfield><subfield code=\"c\">20 cm.</subfield></datafield><datafield tag=\"440\" ind1=\" \" ind2=\"0\"><subfield code=\"a\">Oxford paperbacks</subfield><subfield code=\"=\">^A421667</subfield></datafield><datafield tag=\"500\" ind1=\" \" ind2=\" \"><subfield code=\"a\">Includes index.</subfield></datafield><datafield tag=\"504\" ind1=\" \" ind2=\" \"><subfield code=\"a\">Bibliography: p. 248-284.</subfield></datafield><datafield tag=\"650\" ind1=\" \" ind2=\"0\"><subfield code=\"a\">Romanticism</subfield><subfield code=\"z\">Europe.</subfield><subfield code=\"=\">^A560516</subfield></datafield><datafield tag=\"651\" ind1=\" \" ind2=\"0\"><subfield code=\"a\">Europe</subfield><subfield code=\"x\">Intellectual life.</subfield><subfield code=\"=\">^A273975</subfield></datafield><datafield tag=\"999\" ind1=\" \" ind2=\" \"><subfield code=\"a\">901.934 S324m 1979</subfield><subfield code=\"w\">DEWEYSAN</subfield><subfield code=\"c\">1</subfield><subfield code=\"i\">39151002278923</subfield><subfield code=\"d\">5/25/1995</subfield><subfield code=\"l\">L-3-STACKS</subfield><subfield code=\"m\">LEHIGH</subfield><subfield code=\"p\">$2.50</subfield><subfield code=\"r\">Y</subfield><subfield code=\"s\">Y</subfield><subfield code=\"t\">BOOK</subfield><subfield code=\"u\">5/25/1995</subfield></datafield></record></collection>";
		String xmldecl = "<\\?xml version=\"1.0\" encoding=\"UTF-8\"\\?>";
		marcXML = marcXML.replaceFirst(xmldecl, "");
		System.out.println("XML is now: " + marcXML);		
	}
	
	public static void fixISBN(Record record) {
	    MarcFactory factory = MarcFactory.newInstance();
		List<VariableField> isbns = record.getVariableFields("020");
		Map<String, List<String>> subfields;		
		System.out.println("Before modification, record is: " + record.toString());
		for ( VariableField isbnentry :isbns ) {
			subfields = LU_BuildInstance.getSubfields(isbnentry);
			// if there are multiple "$a" subfields, break them out to create multiple 020s
			// with a single "$a" subfield, and all other fields identical
			if ( subfields.get("$a") != null && subfields.get("$a").size() > 1 ) {
				System.out.println("Multiple a subfields, removing the isbn record and creating multiple 020s instead");
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
					System.out.println("Adding new isbn: " + newisbn.toString());
					record.addVariableField(newisbn);
					System.out.println("Record is now: " + record.toString());
				}					
			} else {
				System.out.println("Zero or one subfields in record's ISBN, not modifying it");
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
				System.out.println("Formatting ISBN string " + isbnstr);
				String newisbnstr = LU_BuildInstance.formatISBNString(isbnstr);
				System.out.println("New ISBN string: " + newisbnstr);
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
				System.out.println("After formatting ISBN strings, record is now: " + record.toString());
			} else {
				System.out.println("No $a subfields to format in record's ISBN: " + record.toString());
			}
		}
				
	}
	
	public static void testCheckTitleControlNumbers() throws Exception {
		String filename = "/mnt/bigdrive/bibdata/sirsidump/20140509/mod.catalog.marcxml";
	    // create a factory instance
		Reader input;
		try {
			input = new FileReader(filename);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			input = null;
		}
		HashMap<String, String> KeyToDate = new HashMap<String, String>();
		BufferedReader inFile = new BufferedReader(new FileReader("/mnt/bigdrive/bibdata/sirsidump/20140509/catalog-all.KeysAndDates"));
		LU_DBLoadInstances.Log("Reading in map of catalog keys to dates, shadowed values, statuses ...");
		int counter = 0, limit = 50000;
		String line = "", key = "";
		String parts[];
		while(inFile.ready()) {
			line = inFile.readLine();
			parts = line.split("\\|");
			//key = "a" + parts[0];
			key = LU_DBLoadInstances.formatCatKey(parts[0]);

			//System.err.println("K=" + key + ", V=" + line);
			KeyToDate.put(key, line);
			counter++;
			if ( counter % 100000 == 0 ) {
				LU_DBLoadInstances.Log(System.out, counter + " records mapped ...", LU_DBLoadInstances.LOG_INFO);
			}
		}
		LU_DBLoadInstances.Log("Done reading in catalog keys map");
		inFile.close();
		InputSource inputsource = new InputSource(input);
		inputsource.setEncoding("UTF-8");
		//MarcReader reader = new MarcStreamReader(input, "ISO-8859-1");
		//MarcXmlReader reader = new MarcXmlReader(new FileInputStream(dumpdir + "/" + args[2]), "UTF-8");
		MarcXmlReader reader = new MarcXmlReader(inputsource);
		List<String> rectypes = new ArrayList<String>();
		
		List<Character> holdingsTypes = Arrays.asList('u', 'v', 'x', 'y');
		Record xmlrecord, nextrecord;
		nextrecord = reader.next();
		List<Record> assocMFHDRecords = new ArrayList<Record>();
		String titleControlNumber = "", dateLine = "";
		String[] dateParts;
		counter = 0;
		do {
			
			assocMFHDRecords.clear();
			xmlrecord = nextrecord;
			
			String catkey = LU_DBLoadInstances.formatCatKey(xmlrecord.getControlNumber()); // need to set this to what's in 001 of the bib to link them

	        dateLine = (String) KeyToDate.get(catkey);
	        dateParts = dateLine.split("\\|");
	        titleControlNumber = dateParts[7];
	        System.out.println("Record before checking control numbers: ");
	        System.out.println(xmlrecord.toString());
			LU_BuildInstance.checkTitleControlNumbers(xmlrecord, titleControlNumber);
			System.out.println();
	        System.out.println("Record after checking control numbers: ");
	        System.out.println(xmlrecord.toString());
			nextrecord = reader.next();
			// The associated holdings records for a bib record should always come right after it
			// So we keep looping and adding them to an ArrayList as we go
			while ( nextrecord != null && 
					holdingsTypes.contains(nextrecord.getLeader().getTypeOfRecord()) ) {

				assocMFHDRecords.add(nextrecord);
				nextrecord = reader.next();
			}

		} while(nextrecord != null && (limit < 0 || counter < limit) );
		System.out.println("Done processing records");
				
	}
	
	public static void fixISBNs() {
		String filename = "/mnt/bigdrive/bibdata/sirsidump/20140327/mod.catalog.marcxml";
	    // create a factory instance
		Reader input;
		try {
			input = new FileReader(filename);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			input = null;
		}
		InputSource inputsource = new InputSource(input);
		inputsource.setEncoding("UTF-8");
		//MarcReader reader = new MarcStreamReader(input, "ISO-8859-1");
		//MarcXmlReader reader = new MarcXmlReader(new FileInputStream(dumpdir + "/" + args[2]), "UTF-8");
		MarcXmlReader reader = new MarcXmlReader(inputsource);
		List<String> rectypes = new ArrayList<String>();
		int counter = 0, limit = 50000;
		List<Character> holdingsTypes = Arrays.asList('u', 'v', 'x', 'y');
		Record xmlrecord, nextrecord;
		nextrecord = reader.next();
		List<Record> assocMFHDRecords = new ArrayList<Record>();
		do {
			
			assocMFHDRecords.clear();
			xmlrecord = nextrecord;
			
			LU_BuildInstance.fixISBN(xmlrecord);
			nextrecord = reader.next();
			// The associated holdings records for a bib record should always come right after it
			// So we keep looping and adding them to an ArrayList as we go
			while ( nextrecord != null && 
					holdingsTypes.contains(nextrecord.getLeader().getTypeOfRecord()) ) {

				assocMFHDRecords.add(nextrecord);
				nextrecord = reader.next();
			}

		} while(nextrecord != null && (limit < 0 || counter < limit) );
		System.out.println("Done processing records");
		
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
