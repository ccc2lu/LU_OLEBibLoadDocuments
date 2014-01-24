package edu.lu.oleconvert;

import static edu.indiana.libraries.LoadDocstore.classes.BuildOLEBibDocument.loadProps;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
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

import OLEBibLoadDocuments.edu.indiana.libraries.OLEBibLoadDocuments.classes.BuildRequestDocument;
import edu.indiana.libraries.JPADriver.classes.JPADriver;
import edu.indiana.libraries.LoadDocstore.jaxb.RequestType;
import edu.lu.oleconvert.ole.Instance;
import edu.lu.oleconvert.ole.InstanceCollection;

public class LU_DBLoadInstances {

	static EntityManagerFactory emf;
	static EntityManager em;

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

	static final int LOG_DEBUG = 0, LOG_INFO = 1, LOG_WARN = 2, LOG_ERROR = 3; 
	private static int currentLogLevel = LOG_INFO;
	private static int defaultLogLevel = LOG_INFO;

	public static void main(String args[]) {
		emf = Persistence.createEntityManagerFactory("default");
		em = emf.createEntityManager();
		EntityTransaction tx = em.getTransaction();
        BufferedReader inFile = null;
		Properties loadprops;
		Properties oracleprops;
		Properties instanceprops;

		String dumpdir = args[1];
		String lehighDataDir = "/mnt/bigdrive/bibdata/LehighData"; // this won't change with each export

		try {


			LU_BuildInstance instanceBuilder = new LU_BuildInstance(dumpdir + "/mod.allcallnums.txt", 
					dumpdir + "/mod.allcallnumsshelvingkeys.txt",
					dumpdir + "/mod.allcallnumsitemnumbers.txt",
					dumpdir + "/mod.allcallnumsanalytics.txt",        														
					dumpdir + "/mod.allitems.txt",
					lehighDataDir + "/Lehigh Locations.csv");
			InstanceCollection ic = new InstanceCollection();

			LU_BuildOLELoadDocs.Log("Starting ...");

			/* ccc2 -- not doing this anymore
	        try {
	            connection = MakeConnection.creatPool().getConnection();
	        } catch (SQLException e) {
	            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
	        }
			 */

			/*
			 * arguments
			 * 0 - properties file
			 * 1 - Sirsi dump directory
			 * 2 - map of catalog keys to dates/shadowed values/statuses
			 * 3 - MarcXML input file of bibs/items data
			 * 4 - bib ingest document output file
			 * 5 - instance ingest document output file
			 */
			LU_BuildOLELoadDocs.Log("Args: ");
			for ( int i = 0; i < args.length; i++ ) {
				LU_BuildOLELoadDocs.Log("arg " + i + "=" + args[i] + ", ");
			}

			loadprops = loadProps(args[0]);
			oracleprops = loadProps(loadprops.getProperty("oracle.properties"));
			instanceprops = loadProps(loadprops.getProperty("instance.properties"));
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
				LU_BuildOLELoadDocs.Log("Reading in map of catalog keys to dates, shadowed values, statuses ...");
				counter = 0;
				while(inFile.ready()) {
					line = inFile.readLine();
					parts = line.split("\\|");
					//key = "a" + parts[0];
					key = LU_BuildOLELoadDocs.formatCatKey(parts[0]);

					//System.err.println("K=" + key + ", V=" + line);
					KeyToDate.put(key, line);
					counter++;
					if ( counter % 100000 == 0 ) {
						LU_BuildOLELoadDocs.Log(System.out, counter + " records mapped ...", LOG_INFO);
					}
				}
				LU_BuildOLELoadDocs.Log("Done reading in catalog keys map");
				inFile.close();
			} catch(Exception e) {
				LU_BuildOLELoadDocs.Log(System.err, "Unable to read in key-to-date mapping: " + e.toString(), LOG_ERROR);
				e.printStackTrace(System.err);
			}

			//PrintWriter output = new PrintWriter(new BufferedWriter(outFile));
			try {

				LU_BuildOLELoadDocs.Log("Creating OLE ingest documents ...");
				// ccc2 -- new loop over all records, perhaps?
				// not sure this accounts for holdings in addition to bibs, though

				//   PrintStream outputprintstream = new PrintStream(output);

				Reader input = new FileReader(dumpdir + "/" + args[3]);
				InputSource inputsource = new InputSource(input);
				inputsource.setEncoding("ISO-8859-1");
				//MarcReader reader = new MarcStreamReader(input, "ISO-8859-1");
				//MarcXmlReader reader = new MarcXmlReader(new FileInputStream(dumpdir + "/" + args[3]), "UTF-8");
				MarcXmlReader reader = new MarcXmlReader(inputsource);

				int limit = -1;
				if ( args.length == 7 ) {
					limit = Integer.parseInt(args[6]);
					LU_BuildOLELoadDocs.Log(System.out, "Only creating ingest documents for the first " + limit + " bib records", LOG_INFO);
				}
				counter = 0;
				List<Character> holdingsTypes = Arrays.asList('u', 'v', 'x', 'y');
				Record xmlrecord, nextrecord;
				nextrecord = reader.next();
				ArrayList<Record> assocMFHDRecords = new ArrayList<Record>();

				do {
					tx.begin();

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


					//request=BuildRequestDocument.buildRequest(loadprops.getProperty("load.user"));

					//request=BuildRequestDocument.buildIngestDocument(request,Integer.toString(catalog.getCatalogKey()),BIBLIOGRAPHIC,MARC_FORMAT,CATEGORY_WORK,xmlrecord,catalog);

					// Build the instance data first, because we might be adding
					ic = new InstanceCollection();
					instanceBuilder.buildInstanceCollection(xmlrecord, ic, assocMFHDRecords);
					
					for ( Instance i : ic.getInstances() ) {
						em.persist(i);						
					}

					tx.commit();

					counter++;
					if ( counter % 10000 == 0 ) {
						LU_BuildOLELoadDocs.Log(System.out, counter + " instances loaded ...", LOG_INFO);
					}
				} while (nextrecord != null && (limit < 0 || counter < limit) );
				LU_BuildOLELoadDocs.Log("Done creating ingest documents");
				System.exit(0);
			} catch (IOException e) {
				LU_BuildOLELoadDocs.Log(System.err, e.getMessage(), LOG_ERROR);
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			} catch (org.marc4j.MarcException e) {
				LU_BuildOLELoadDocs.Log(System.err, e.getMessage(), LOG_ERROR);
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.        	
			} catch (Exception e) {
				LU_BuildOLELoadDocs.Log(System.err, e.getMessage(), LOG_ERROR);
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.        					
			}
		} catch (Exception e) {
			System.err.println("Something went wrong: " + e.toString());
			e.printStackTrace(System.err);
			tx.rollback();
		}
	}
}
