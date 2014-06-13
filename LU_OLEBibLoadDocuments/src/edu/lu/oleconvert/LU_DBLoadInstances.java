package edu.lu.oleconvert;

//import static edu.indiana.libraries.LoadDocstore.classes.BuildOLEBibDocument.loadProps;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.Result;
import javax.xml.transform.sax.SAXResult;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.marc4j.MarcWriter;
import org.marc4j.MarcXmlReader;
import org.marc4j.MarcXmlWriter;
import org.marc4j.marc.Record;
import org.xml.sax.InputSource;

//import OLEBibLoadDocuments.edu.indiana.libraries.OLEBibLoadDocuments.classes.BuildRequestDocument;
//import edu.indiana.libraries.JPADriver.classes.JPADriver;
//import edu.indiana.libraries.LoadDocstore.jaxb.RequestType;
import edu.lu.oleconvert.ole.Bib;
import edu.lu.oleconvert.ole.Instance;
import edu.lu.oleconvert.ole.InstanceCollection;
import edu.lu.oleconvert.ole.OLEHoldings;

public class LU_DBLoadInstances {

	public static EntityManagerFactory ole_emf;
	public static EntityManager ole_em;
	public static EntityManagerFactory migration_emf;
	public static EntityManager migration_em;
	
    static String[] booleanValues = {"false","true"};
    static HashMap<String, String> KeyToDate = new HashMap<String, String>();
    // Silly, dirty trick to initialize a map inline using an anonymous block
    static HashMap<String, String> StatusLookup = new HashMap<String, String>() {{ put("0", "NOTEXT"); put("1", "INTEXT"); put("4", "UPDTEXT"); 
                                                                                put("6", "LOCKTEXT"); put("1000", "USERLOCK");
    																	     }};
    																	     
	public static final String BIBLIOGRAPHIC = "bibliographic";
	public static final String INSTANCE = "instance";
	public static final String MARC_FORMAT = "marc";
	public static final String OLEML_FORMAT = "oleml";
	public static final String CATEGORY_WORK = "work";
	
    public static final int LOG_DEBUG = 0, LOG_INFO = 1, LOG_WARN = 2;
	public static final int LOG_ERROR = 3; 
    private static int currentLogLevel = LOG_INFO;
    private static int defaultLogLevel = LOG_INFO;

    public static void Log(PrintStream out, String message, int level) {
    	if ( level >= currentLogLevel ) {
    		out.println(message);
    	}
    }

    public static void Log(PrintWriter out, String message, int level) {
    	if ( level >= currentLogLevel ) {
    		out.println(message);
    	}
    }

    public static void Log(PrintWriter out, String message) {
    	Log(out, message, defaultLogLevel);
    }
    
    public static void Log(PrintStream out, String message) {
    	Log(out, message, defaultLogLevel);
    }
    
    public static void Log(String message) {
    	Log(System.out, message);
    }
    
    public static String formatCatKey(String key) {
    	// If there's an "a" at the beginning of what should be a numeric key,
    	// get rid of it.  Sirsi's MARC export put the "a" before the catalog
    	// keys in the MARC record's 001 fields.  luconvert.java should be
    	// removing that from the only place it ends up, but just in case, we 
    	// get rid of it here too
    	String newkey = key;
    	if ( key.substring(0, 1).equals("a") ) {
    		newkey = key.substring(1);
    	} 
    	// Then pad the key out to 11 characters with leading zeros -- that's how
    	// it will be in the MarcXML, and how OLE wants it in the database
    	//return StringUtils.leftPad(key, 11, "0");
    	/*
    	int startId = 10000000;
    	int keynum = 0;
    	try {
    		keynum = startId + Integer.parseInt(newkey);
        	newkey = Integer.toString(keynum);
    	} catch (Exception e) {
    		Log(System.err, "Unable to format catalog key: " + key);
    		newkey = key;
    	}
    	*/
    	return newkey;
    }
    
	public static void main(String args[]) {
		ole_emf = Persistence.createEntityManagerFactory("ole");
		ole_em = ole_emf.createEntityManager();
		EntityTransaction ole_tx = ole_em.getTransaction();
		
		migration_emf = Persistence.createEntityManagerFactory("olemigration");
		migration_em = migration_emf.createEntityManager();
		
        BufferedReader inFile = null;
		Properties loadprops;
		Properties oracleprops;
		Properties instanceprops;

		String dumpdir = args[0];
		String lehighDataDir = args[1];

		try {


			LU_BuildInstance instanceBuilder = new LU_BuildInstance(dumpdir + "/mod.allcallnums.txt", 
					dumpdir + "/mod.allcallnumsshelvingkeys.txt",
					dumpdir + "/mod.allcallnumsitemnumbers.txt",
					dumpdir + "/mod.allcallnumsanalytics.txt",        														
					dumpdir + "/mod.allitems.txt",
					lehighDataDir + "/Lehigh Locations.csv",
					lehighDataDir + "/sfx_export_portfolios.csv");
			InstanceCollection ic = new InstanceCollection();

			LU_DBLoadInstances.Log("Starting ...");

			/* ccc2 -- not doing this anymore
	        try {
	            connection = MakeConnection.creatPool().getConnection();
	        } catch (SQLException e) {
	            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
	        }
			 */

			/*
			 * arguments
			 * 0 - Sirsi dump directory
			 * 1 - Lehigh Data directory
			 * 2 - map of catalog keys to dates/shadowed values/statuses
			 * 3 - MarcXML input file of bibs/items data
			 * 4 (optional) - number of records to create
			 */
			LU_DBLoadInstances.Log("Args: ");
			for ( int i = 0; i < args.length; i++ ) {
				LU_DBLoadInstances.Log("arg " + i + "=" + args[i] + ", ");
			}

			//loadprops = loadProps(args[0]);
			//oracleprops = loadProps(loadprops.getProperty("oracle.properties"));
			//instanceprops = loadProps(loadprops.getProperty("instance.properties"));
			// OracleReader buildBib = new OracleReader(oracleprops); // ccc2 -- not using this
			int counter = 0;

			try {
				// ccc2 -- The dump I took from Sirsi and converted to MarcXML was
				// of ALL bibliographic records, shadowed and not, all statuses.
				// The data about whether or not each record was shadowed, what it's status was,
				// and when it was cataloged or modified doesn't come along in the MARC records.
				// So, I ended up running a separate dump of the catalog table using selcatalog
				// to get that information out.  Here's the command I ran on deweyii:
				// /sirsi/s/sirsi/Unicorn/Bin/selcatalog -n">0" -z">0" -iS -e008 -oCe6upqr > /sirsi/s/sirsi/Unicorn/Xfer/catalog-all.KeysAndDates 2> /sirsi/s/sirsi/Unicorn/Xfer/selcatalog.log
				// I then copied that catalog-allKeysAndDates file over to my workstation
				// to feed to this program as input.
				// For reference, the -o parameter specifies the order of the fields output.
				// C is the catalog key for the record in Sirsi, which shows up as field 001
				// in the MarcXML -- that's how we join the data together.
				// The output of -e008 comes next, that's the "Fixed Length Data Elements".
				// I don't use it here, but it's the only field I could see that appeared to be
				// defined and unique for everything.  Catalog keys repeat sometimes, it seems.
				// 6 means to output the shadowed value
				// u means to output the status
				// p means to output the date the catalog record was created, which is distinct from
				// q which outputs the date the record was cataloged
				// r outputs the date the record was last modified

				String line, key;
				String parts[];
				inFile = new BufferedReader(new FileReader(dumpdir + "/" + args[2]));
				LU_DBLoadInstances.Log("Reading in map of catalog keys to dates, shadowed values, statuses ...");
				counter = 0;
				while(inFile.ready()) {
					line = inFile.readLine();
					parts = line.split("\\|");
					//key = "a" + parts[0];
					key = LU_DBLoadInstances.formatCatKey(parts[0]);

					//System.err.println("K=" + key + ", V=" + line);
					KeyToDate.put(key, line);
					counter++;
					if ( counter % 100000 == 0 ) {
						LU_DBLoadInstances.Log(System.out, counter + " records mapped ...", LOG_INFO);
					}
				}
				LU_DBLoadInstances.Log("Done reading in catalog keys map");
				inFile.close();
			} catch(Exception e) {
				LU_DBLoadInstances.Log(System.err, "Unable to read in key-to-date mapping: " + e.toString(), LOG_ERROR);
				e.printStackTrace(System.err);
			}

			//PrintWriter output = new PrintWriter(new BufferedWriter(outFile));
			try {

				LU_DBLoadInstances.Log("Loading instances ...");
				// ccc2 -- new loop over all records, perhaps?
				// not sure this accounts for holdings in addition to bibs, though

				//   PrintStream outputprintstream = new PrintStream(output);

				Reader input = new FileReader(dumpdir + "/" + args[3]);
				InputSource inputsource = new InputSource(input);
				//inputsource.setEncoding("ISO-8859-1");
				inputsource.setEncoding("UTF-8");
				//MarcReader reader = new MarcStreamReader(input, "ISO-8859-1");
				//MarcXmlReader reader = new MarcXmlReader(new FileInputStream(dumpdir + "/" + args[2]), "UTF-8");
				MarcXmlReader reader = new MarcXmlReader(inputsource);

				int limit = -1;
				if ( args.length == 5 ) {
					limit = Integer.parseInt(args[4]);
					LU_DBLoadInstances.Log(System.out, "Only loading records for the first " + limit + " bib records", LOG_INFO);
				}
				counter = 0;
				List<Character> holdingsTypes = Arrays.asList('u', 'v', 'x', 'y');
				Record xmlrecord, nextrecord;
				nextrecord = reader.next();
				List<Record> assocMFHDRecords = new ArrayList<Record>();

				DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

				LU_DBLoadInstances.Log(System.out, 
						                "Beginning load, time is: " + df.format(Calendar.getInstance().getTime()), 
						                LOG_INFO);
                Bib bib;
                
				ole_tx.begin();

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
						/*
						 * I'm not inserting the bibs for MFHD records anymore
						Bib tmpbib = buildBib(nextrecord);
						if ( tmpbib != null ) {
							ole_em.persist(tmpbib);
						} else {
							LU_DBLoadInstances.Log(System.err, "Unable to persist bib for record " + nextrecord.getControlNumber(), LOG_ERROR);							
						}
						*/						
						nextrecord = reader.next();
					}
					// If subsequent records have the same bib ID in the 001 field,
					// then we're going to append all the 999 fields of those records to xmlrecord, 
					// and skip the records with the same bib ID
					while ( nextrecord != null &&
							nextrecord.getControlNumber().equals(xmlrecord.getControlNumber())) {
						LU_DBLoadInstances.Log(System.out, "Two record with same control number, first: ", LOG_INFO);
						LU_DBLoadInstances.Log(System.out, xmlrecord.toString(), LOG_INFO);
						LU_DBLoadInstances.Log(System.out, "Second: ", LOG_INFO);
						LU_DBLoadInstances.Log(System.out, nextrecord.toString(), LOG_INFO);
						LU_DBLoadInstances.Log(System.out, "Appending 999s from second record to first", LOG_INFO);
						LU_BuildInstance.append999fields(xmlrecord, nextrecord);
						LU_DBLoadInstances.Log(System.out, "First record is now: ", LOG_INFO);
						LU_DBLoadInstances.Log(System.out, xmlrecord.toString(), LOG_INFO);
						nextrecord = reader.next();
					}
					
					// Build a bib record from the xmlrecord
					bib = buildBib(xmlrecord);
					if ( bib != null ) {
						ole_em.persist(bib);
					} else {
						LU_DBLoadInstances.Log(System.err, "Unable to persist bib for record " + xmlrecord.getControlNumber(), LOG_ERROR);						
					}

					//request=BuildRequestDocument.buildRequest(loadprops.getProperty("load.user"));

					//request=BuildRequestDocument.buildIngestDocument(request,Integer.toString(catalog.getCatalogKey()),BIBLIOGRAPHIC,MARC_FORMAT,CATEGORY_WORK,xmlrecord,catalog);

					// Build the instance data first, because we might be adding
					//instanceBuilder.buildInstanceCollection(xmlrecord, bib, assocMFHDRecords);
					instanceBuilder.buildBibHoldingsData(xmlrecord, bib, assocMFHDRecords);
					
					// this method will check if the former ID of the bib record matches one
					// in the serials table of the olemigration database, then if it does it
					// will fill in ole serials receiving tables
					instanceBuilder.buildSerialsData(xmlrecord, bib, assocMFHDRecords);
					
					// now we don't loop over instances, we just let the bib cascade persisting all of its holdings,
					// which cascades to items, etc.
					/*
					for ( Instance i : ic.getInstances() ) {
						i.getOleHoldings().setBib(bib); // TODO: is this always right, or do we want to set the bib for some holdings records to the MFHD rec?  not sure
						System.err.println("Bib ID: " + i.getOleHoldings().getBib().getId() + ", former ID: " + i.getOleHoldings().getBib().getFormerId());
						em.persist(i.getOleHoldings());
					}
					*/
					
					if ( bib != null ) {
						//for ( OLEHoldings holdings : bib.getHoldings() ) {
						//	LU_DBLoadInstances.Log(System.err, "ID for holdings " + holdings.getHoldingsIdentifier() + " = " + holdings.getBibId(), LOG_INFO);
						//}
						// Now persist bib again after creating holdings/items
						ole_em.persist(bib);
					} else {
						LU_DBLoadInstances.Log(System.err, "Unable to persist bib for record " + xmlrecord.getControlNumber(), LOG_ERROR);						
					}


					counter++;
					if ( counter % 10 == 0 || ( limit > 0 && counter >= limit )) {
						ole_tx.commit();
						ole_em.clear(); // TODO: testing this to see if it fixes memory problems
						LU_DBLoadInstances.Log(System.out, counter + " records loaded ...", LOG_INFO);
						ole_tx.begin();
					}
				} while (nextrecord != null && (limit < 0 || counter < limit) );
				LU_DBLoadInstances.Log(System.out, 
		                "Done loading instances, time is: " + df.format(Calendar.getInstance().getTime()), 
		                LOG_INFO);

				System.exit(0);
			} catch (IOException e) {
				LU_DBLoadInstances.Log(System.err, e.getMessage(), LOG_ERROR);
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			} catch (org.marc4j.MarcException e) {
				LU_DBLoadInstances.Log(System.err, e.getMessage(), LOG_ERROR);
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.        	
			} catch (Exception e) {
				LU_DBLoadInstances.Log(System.err, e.getMessage(), LOG_ERROR);
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.        					
			}
		} catch (Exception e) {
			System.err.println("Something went wrong: " + e.toString());
			e.printStackTrace(System.err);
			ole_tx.rollback();
		}
	}
	
	public static Bib buildBib(Record record) {
		String catkey = LU_DBLoadInstances.formatCatKey(record.getControlNumber()); // need to set this to what's in 001 of the bib to link them
		Bib bib = new Bib(catkey);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
    	OutputFormat of = new OutputFormat("xml", "UTF-8", true);
    	of.setOmitXMLDeclaration(true);
    	XMLSerializer tmpserializer = new XMLSerializer(out, of);
    	//tmpserializer.setOutputFormat(of);
    	//tmpserializer.setOutputByteStream(out);
    	Result result;
    	MarcWriter writer;
		/*
    	try {
			result = new SAXResult(tmpserializer.asContentHandler());
	        writer = new MarcXmlWriter(result);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			LU_DBLoadInstances.Log(System.err, "Unable to create xml writer with custom output format", LOG_WARN);
			e1.printStackTrace(System.err);
			writer = new MarcXmlWriter(out, "ISO-8859-1");
		}
		*/
		//writer = new MarcXmlWriter(out, "ISO-8859-1");
    	writer = new MarcXmlWriter(out, "UTF-8");

        String marcXML;
        
        
        // Logic stolen from LU_BuildOLELoadDocs, the addAdditionalInfo method
        //String catkey = LU_DBLoadInstances.formatCatKey(record.getVariableField("001").toString().split(" ")[1]);

        //System.err.println("Looking for dates for record with catalog key " + catkey);
        String dateLine = (String) KeyToDate.get(catkey);
        
        // dateLine string should be of the form: 
        // <catalog key in Siris>|<MARC FIELD 008>|<shadowed>|<status>|<date catalog record created>|<date cataloged>|<date modified>|<flexible key>|
        // 1 means shadowed, 0 means unshadowed
        // status may be any of the following: 0 (NOTEXT), 1 (INTEXT), 4 (UPDTEXT), 6 (LOCKTEXT), 1000 (USERLOCK)
        // MARC field 008 is "fixed length data elements and always seems to be populated
        String shadowed, status, dateCataloged, dateModified, titleControlNumber;
        status = "Catalogued";
        if ( dateLine == null ) {
        	Log(System.err, "ERROR: No mapping found for key " + catkey, LOG_ERROR);
        	Log(System.err, "Filling in additional attributes with empty strings", LOG_ERROR);
        	dateCataloged = dateModified = shadowed = "";

        } else {
        	String[] dateParts = dateLine.split("\\|");
        	if ( dateParts.length < 8 ) {
        		System.err.println("ERROR: Can't get shadowed, status, date cataloged or modified, not enough fields in line: " + dateLine);
            	System.err.println("Filling in additional attributes with empty strings");
        		dateCataloged = dateModified = shadowed = status = "";
        	} else {

        		shadowed = dateParts[2].equals("1") ? "Y" : "N";
        		// The bib status values set by Sirsi don't seem to mean
        		// anything to OLE
        		//status = StatusLookup.get(dateParts[3]); 
        		if ( dateParts[5].equals("0") || dateParts[5].length() == 0 ) {
        			dateCataloged = "";
        		} else {
        			dateCataloged = dateParts[5];
        		}
        		if ( dateParts[6].equals("0") || dateParts[6].length() == 0 ) {
        			dateModified = "";
        		} else {
        			dateModified = dateParts[6];
        		}
        		titleControlNumber = dateParts[7];
        		LU_BuildInstance.checkTitleControlNumbers(record, titleControlNumber);
        	}
        }
        
		try {
	        writer.write(record);
	        //out.flush();
	        out.close();
	        writer.close();
			//marcXML = out.toString("ISO-8859-1");
	        marcXML = out.toString("UTF-8");
			String xmldecl = "<\\?xml version=\"1.0\" encoding=\"UTF-8\"\\?>";
			marcXML = marcXML.replaceFirst(xmldecl, "");
	        bib.setContent(marcXML);
	        // We want to keep the IDs for the Bibs the same
	        // bib.setId(Long.parseLong(catkey));
	        if ( !dateCataloged.equals("") ) {
	        	bib.setDateCreated(dateCataloged);
	        }
	        if ( !dateModified.equals("") ) {
	        	bib.setDateUpdated(dateModified);
	        }
	        bib.setCreatedBy("BulkIngest-User");
	        bib.setStatus(status);	        
	        bib.setStaffOnly(shadowed);
	        bib.setFastAdd("N");
	        bib.setUniqueIdPrefix("wbm");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			bib = null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bib;
	}
}
