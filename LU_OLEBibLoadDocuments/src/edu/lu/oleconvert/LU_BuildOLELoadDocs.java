package edu.lu.oleconvert;

import edu.indiana.libraries.JPADriver.classes.JPADriver;
import edu.indiana.libraries.JPADriver.classes.MakeConnection;
import edu.indiana.libraries.JPADriver.pojos.Catalog;
import edu.indiana.libraries.LoadDocstore.jaxb.AdditionalAttributesType;
import edu.indiana.libraries.LoadDocstore.jaxb.IngestDocumentType;
import edu.indiana.libraries.LoadDocstore.jaxb.ObjectFactory;
import edu.indiana.libraries.LoadDocstore.jaxb.RequestDocumentsType;
import edu.indiana.libraries.LoadDocstore.jaxb.RequestType;
//import edu.indiana.libraries.OracleConnection.classes.OracleReader; // ccc2 -- not using this, ourselves
import edu.indiana.libraries.ole.classes.NameSpaceMapper;

import org.marc4j.MarcReader;
import org.marc4j.MarcStreamReader;
import org.marc4j.MarcWriter;
import org.marc4j.MarcXmlWriter;
import org.marc4j.MarcXmlReader; // ccc2 added
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.marc4j.marc.VariableField;
import org.marc4j.marc.Leader;
import org.xml.sax.InputSource;

import OLEBibLoadDocuments.edu.indiana.libraries.OLEBibLoadDocuments.classes.BuildRequestDocument;
import edu.indiana.libraries.LoadDocstore.jaxb.*;
import edu.lu.oleconvert.ole.InstanceCollection;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.sax.SAXResult;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;

import static edu.indiana.libraries.LoadDocstore.classes.BuildOLEBibDocument.loadProps;

/**
 * Author: ccc2@lehigh.edu
 * Date: 7/29/2013
 * Adapted from code written by John Pillans, jpillan@indiana.edu
 */
public class LU_BuildOLELoadDocs {
	
    static ObjectFactory objectFactory=new ObjectFactory();
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
    
    protected static XMLSerializer getXMLSerializer(BufferedWriter out) {
        // configure an OutputFormat to handle CDATA
        OutputFormat of = new OutputFormat();

        // specify which of your elements you want to be handled as CDATA.
        // The use of the '^' between the namespaceURI and the localname
        // seems to be an implementation detail of the xerces code.
        // When processing xml that doesn't use namespaces, simply omit the
        // namespace prefix as shown in the third CDataElement below.
        of.setCDataElements(
            new String[] { "^content" } ); 
            		//"ns1^foo",   // <ns1:foo>
                   //"ns2^bar",   // <ns2:bar>
                   //"^baz" });   // <baz>

        // set any other options you'd like
        of.setPreserveSpace(true);
        of.setIndenting(true);
        //of.setOmitXMLDeclaration(true);
        // create the serializer
        XMLSerializer l_serializer = new XMLSerializer(of);
        //serializer.setOutputByteStream(out);        
        l_serializer.setOutputCharStream(out);
        return l_serializer;
    }

    private static XMLSerializer serializer = null;
    
    protected static void marshallObjext(Object object, Marshaller marshaller, XMLSerializer serializer) {
    	try {
    		marshaller.marshal(object, serializer);
    	} catch (JAXBException e) {
    		e.printStackTrace();
    	}
    }
    
    protected static void marshallObjext(Object object, Marshaller marshaller, BufferedWriter out){
        //StringWriter writer = new StringWriter();
    	//XMLSerializer serializer = getXMLSerializer(out);
        try {
        	if ( serializer == null ) {
        		serializer = getXMLSerializer(out);
        	}
            marshaller.marshal(object,serializer);
        } catch (JAXBException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
/*
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return writer.toString();
        */
    }
    
    protected static Marshaller getMarshaller(Class classObject){
        JAXBContext jc = null;
        try {
            jc = JAXBContext.newInstance(classObject);
        } catch (JAXBException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        //XMLSerializer serial;
        Marshaller marshaller = null;
        try {
            marshaller = jc.createMarshaller();
            //marshaller.setProperty("com.sun.xml.internal.bind.namespacePrefixMapper", new NameSpaceMapper());
            //marshaller.setProperty("com.sun.xml.bind.marshaller.NamespacePrefixMapper", new LU_NamespacePrefixMapper());
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "ISO-8859-1");
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,true);
 
            marshaller.setProperty("com.sun.xml.bind.marshaller.CharacterEscapeHandler",
            	    			   new NullCharacterEscapeHandler());
        } catch (JAXBException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return marshaller;
    }
    
    public static String formatCatKey(String key) {
    	// If there's an "a" at the beginning of what should be a numeric key,
    	// get rid of it.  Sirsi's MARC export put the "a" before the catalog
    	// keys in the MARC record's 001 fields.  luconvert.java should be
    	// removing that from the only place it ends up, but just in case, we 
    	// get rid of it here too
    	if ( key.substring(0, 1).equals("a") ) {
    		key = key.substring(1);
    	}
    	// Then pad the key out to 11 characters with leading zeros -- that's how
    	// it will be in the MarcXML, and how OLE wants it in the database
    	//return StringUtils.leftPad(key, 11, "0");
    	int startId = 10000000;
    	int keynum = startId + Integer.parseInt(key);
    	key = Integer.toString(keynum);
    	return key;
    }
    
    public static void main(String[] args) {
        Marshaller marshaller, instance_marshaller;
        Properties loadprops;
        Properties oracleprops;
        Properties instanceprops;
        JPADriver jpaDriver = new JPADriver();
        PreparedStatement statement;
        Connection connection=null;
        ResultSet resultSet;
        BufferedWriter outFile = null;
        BufferedWriter instance_outFile = null;
        XMLSerializer bib_serializer = null, instance_serializer = null;
        BufferedReader inFile = null;
        //Record record = null;
        ByteArrayOutputStream out = null;
        MarcWriter writer;
        RequestType bib_request;
        RequestType inst_request;
        String dumpdir = args[1];
        String lehighDataDir = "/mnt/bigdrive/bibdata/LehighData"; // this won't change with each export
        
        LU_BuildInstance instanceBuilder = new LU_BuildInstance(dumpdir + "/mod.allcallnums.txt", 
        														dumpdir + "/mod.allcallnumsshelvingkeys.txt",
        														dumpdir + "/mod.allcallnumsitemnumbers.txt",
        														dumpdir + "/mod.allcallnumsanalytics.txt",        														
        		                                                dumpdir + "/mod.allitems.txt",
        		                                                lehighDataDir + "/Lehigh Locations.csv");
        InstanceCollection ic = new InstanceCollection();
        
        Log("Starting ...");
        
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
        Log("Args: ");
        for ( int i = 0; i < args.length; i++ ) {
        	Log("arg " + i + "=" + args[i] + ", ");
        }
        
        loadprops = loadProps(args[0]);
        oracleprops = loadProps(loadprops.getProperty("oracle.properties"));
        instanceprops = loadProps(loadprops.getProperty("instance.properties"));
        // OracleReader buildBib = new OracleReader(oracleprops); // ccc2 -- not using this
        marshaller = getMarshaller(RequestType.class);
        instance_marshaller = getMarshaller(InstanceCollection.class);
        int counter = 0;

        try {
            outFile = new BufferedWriter(new FileWriter(dumpdir + "/" + args[4]));
            instance_outFile = new BufferedWriter(new FileWriter(dumpdir + "/" + args[5]));
            bib_serializer = getXMLSerializer(outFile);
            instance_serializer = getXMLSerializer(instance_outFile);
        } catch (IOException e) {
        	Log(System.err, e.getMessage(), LOG_ERROR);
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

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
        	Log("Reading in map of catalog keys to dates, shadowed values, statuses ...");
        	counter = 0;
        	while(inFile.ready()) {
        		line = inFile.readLine();
        		parts = line.split("\\|");
        		//key = "a" + parts[0];
        		key = formatCatKey(parts[0]);
                
        		//System.err.println("K=" + key + ", V=" + line);
        		KeyToDate.put(key, line);
        		counter++;
        		if ( counter % 100000 == 0 ) {
        			Log(System.out, counter + " records mapped ...", LOG_INFO);
        		}
        	}
        	Log("Done reading in catalog keys map");
        	inFile.close();
        } catch(Exception e) {
        	Log(System.err, "Unable to read in key-to-date mapping: " + e.toString(), LOG_ERROR);
        	e.printStackTrace(System.err);
        }
        
        //PrintWriter output = new PrintWriter(new BufferedWriter(outFile));
        try {
        	/*
        	// ccc2 -- old, Oracle driven loop
        	// This drives everything by fetching all the records from the catalog
        	// the jpaload.sql property from the load.properties file just says 
        	// select * from catalog where catalog_key < 1000
            statement = connection.prepareStatement(loadprops.getProperty("jpaload.sql"));
            resultSet = statement.executeQuery();
            while(resultSet.next()){
            	// ccc2 -- getObject calls all the methods of the class passed in, Catalog in this case,
            	// by calling all the methods of the class that start with "set", then getting the column
            	// named in that method's annotation and using that to get data out of the ResultSet.  
            	// The bib record is built up using the OracleReader class's getRecord method, which
            	// uses its own SQL query to build the record, which it then outputs as XML.
                Catalog catalog = (Catalog) JPADriver.getObject(resultSet,Catalog.class);
                record=buildBib.getRecord(catalog.getCatalogKey());
                out = new ByteArrayOutputStream();
                writer = new MarcXmlWriter(out,"UTF-8");
                writer.write(record);
                writer.close();
                String marcXML=out.toString("UTF-8"); // ccc2 -- this is what we already have -- could be we just need
                									  // to process it one record at a time?  
                request=BuildRequestDocument.buildRequest(loadprops.getProperty("load.user"));
                request=BuildRequestDocument.buildIngestDocument(request,Integer.toString(catalog.getCatalogKey()),BIBLIOGRAPHIC,MARC_FORMAT,CATEGORY_WORK,marcXML,catalog);
                //marshaller = getMarshaller(RequestType.class);
                System.out.print(marshallObjext(request,marshaller));
            }                        
            outFile.close();
	        */
        	
        	Log("Creating OLE ingest documents ...");
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
            	Log(System.out, "Only creating ingest documents for the first " + limit + " bib records", LOG_INFO);
            }
            counter = 0;
            List<Character> holdingsTypes = Arrays.asList('u', 'v', 'x', 'y');
            Record xmlrecord, nextrecord;
            nextrecord = reader.next();
        	ArrayList<Record> assocMFHDRecords = new ArrayList<Record>();

        	bib_request=BuildRequestDocument.buildRequest(loadprops.getProperty("load.user"));
        	inst_request=BuildRequestDocument.buildRequest(loadprops.getProperty("load.user"));
        	OutputFormat of = new OutputFormat("xml", "ISO-8859-1", true);
        	of.setOmitXMLDeclaration(true);
            of.setPreserveSpace(true);
            of.setIndenting(true);
            of.setCDataElements(
                    new String[] { "^content" } ); 
            XMLSerializer tmpserializer;
            Result result;

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

            	// ccc2 -- catalog was only needed to get some extra data elements, which we get from a 
            	// separate file that I generated using selcatalog
            	// Catalog catalog = (Catalog) JPADriver.getObject(resultSet,Catalog.class);

                out = new ByteArrayOutputStream();
            	//tmpserializer = new XMLSerializer();
            	//tmpserializer.setOutputFormat(of);
            	//tmpserializer.setOutputByteStream(out);
            	//result = new SAXResult(tmpserializer.asContentHandler());
                //writer = new MarcXmlWriter(result);
                writer = new MarcXmlWriter(out, "ISO-8859-1");
                writer.write(xmlrecord);
                //out.close();
                writer.close();
                String marcXML=out.toString("ISO-8859-1"); // ccc2 -- this is what we already have -- could be we just need
                									  // to process it one record at a time?
                
            	//request=BuildRequestDocument.buildRequest(loadprops.getProperty("load.user"));
                
                //request=BuildRequestDocument.buildIngestDocument(request,Integer.toString(catalog.getCatalogKey()),BIBLIOGRAPHIC,MARC_FORMAT,CATEGORY_WORK,xmlrecord,catalog);

            	// Build the instance data first, because we might be adding
            	ic = new InstanceCollection();
            	instanceBuilder.buildInstanceCollection(xmlrecord, ic, assocMFHDRecords);
                
            	bib_request=buildIngestDocument(bib_request,
            								//xmlrecord.getLeader().toString(),
            								xmlrecord.getControlNumber(),
            								BIBLIOGRAPHIC,MARC_FORMAT,CATEGORY_WORK,
            								xmlrecord, marcXML);
            	
                
            	marshallObjext(bib_request, marshaller, bib_serializer);

            	// Here would be a great place to generate instance records too, since we already have the catalog record in hand
            	
            	//  marshaller = getMarshaller(RequestType.class);
            	
            	// output.print(marshallObjext(request,marshaller));      
            	//marshallObjext(request,marshaller, outFile);
            	
            	
            	//instance_marshaller.marshal(ic, instance_outFile);
            	
            	out = new ByteArrayOutputStream();
            	tmpserializer = new XMLSerializer(out, of);
            	result = new SAXResult(tmpserializer.asContentHandler());
            	instance_marshaller.marshal(ic, tmpserializer);
            	String ingestxml = out.toString("ISO-8859-1");
            	
               	inst_request=buildIngestDocument(inst_request,
						//xmlrecord.getLeader().toString(),
						xmlrecord.getControlNumber(),
						INSTANCE,OLEML_FORMAT,CATEGORY_WORK,
						ingestxml);
            	marshallObjext(inst_request, marshaller, instance_serializer);

        		counter++;
        		if ( counter % 10000 == 0 ) {
        			Log(System.out, counter + " ingest documents created ...", LOG_INFO);
        		}
            } while (nextrecord != null && (limit < 0 || counter < limit) );
            Log("Done creating ingest documents");
            outFile.close();
            System.exit(0);
        } catch (IOException e) {
        	Log(System.err, e.getMessage(), LOG_ERROR);
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (org.marc4j.MarcException e) {
        	Log(System.err, e.getMessage(), LOG_ERROR);
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.        	
        } catch (JAXBException e) {
			Log(System.err, e.getMessage(), LOG_ERROR);
			e.printStackTrace();
		}
    }
    
    public static RequestType buildIngestDocument(RequestType request, String id, String type, String format, String category, String XML){
        RequestDocumentsType requestDocuments = objectFactory.createRequestDocumentsType();
        IngestDocumentType ingestDocument = objectFactory.createIngestDocumentType();
        ingestDocument.setContent(XML);
        ingestDocument.setId(id);
        ingestDocument.setFormat(format);
        ingestDocument.setCategory(category);
        ingestDocument.setType(type);
        requestDocuments.getIngestDocument().add(ingestDocument);
        request.setRequestDocuments(requestDocuments);
        return request;
    }
        
    public static RequestType buildIngestDocument(RequestType request, String id, String type, String format, String category, Record record, String marcXML){
        RequestDocumentsType requestDocuments = objectFactory.createRequestDocumentsType();
        IngestDocumentType ingestDocument = objectFactory.createIngestDocumentType();
        ingestDocument.setContent(marcXML);
        ingestDocument.setId(id);
        ingestDocument.setFormat(format);
        ingestDocument.setCategory(category);
        ingestDocument.setType(type);
        /* ccc2 -- we won't have a catalog object when reading from MarcXML,
         * so we get the additionAttributes from a separate file that comes
         * from selcatalog
         */
         
        ingestDocument.setAdditionalAttributes(addAdditionalInfo(record));
        requestDocuments.getIngestDocument().add(ingestDocument);
        request.setRequestDocuments(requestDocuments);
        return request;
    }
    
    public static AdditionalAttributesType addAdditionalInfo(Record record) {
        AdditionalAttributesType additionalAttributesType = new AdditionalAttributesType();
        
        // Looks like the date information is in 999 fields
        // Date cataloged in the first one, last modified in the last, potentially
        // TODO: there has GOT to be a better way to get the data from 
        // the 001 field in one line than this:
        String catkey = LU_BuildOLELoadDocs.formatCatKey(record.getVariableField("001").toString().split(" ")[1]);
        //System.err.println("Looking for dates for record with catalog key " + catkey);
        String dateLine = (String) KeyToDate.get(catkey);
        
        // dateLine string should be of the form: 
        // <catalog key in Siris>|<MARC FIELD 008>|<shadowed>|<status>|<date catalog record created>|<date cataloged>|<date modified>|
        // 1 means shadowed, 0 means unshadowed
        // status may be any of the following: 0 (NOTEXT), 1 (INTEXT), 4 (UPDTEXT), 6 (LOCKTEXT), 1000 (USERLOCK)
        // MARC field 008 is "fixed length data elements and always seems to be populated
        String shadowed, status, dateCataloged, dateModified;
        if ( dateLine == null ) {
        	System.err.println("ERROR: No mapping found for key " + catkey);
        	System.err.println("Filling in additional attributes with empty strings");
        	dateCataloged = dateModified = shadowed = status = "";

        } else {
        	String[] dateParts = dateLine.split("\\|");
        	if ( dateParts.length < 7 ) {
        		System.err.println("ERROR: Can't get shadowed, status, date cataloged or modified, not enough fields in line: " + dateLine);
            	System.err.println("Filling in additional attributes with empty strings");
        		dateCataloged = dateModified = shadowed = status = "";
        	} else {

        		shadowed = dateParts[2].equals("1") ? "true" : "false";
        		status = StatusLookup.get(dateParts[3]);
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

        	}
        }
        /* Old code for getting the date -- it's all wrong */
        /*
		List<VariableField> optFields = record.getVariableFields("999");
		String dateCataloged = null;
		String dateModified = null;
		if ( optFields.size() > 0 ) {
			dateCataloged = optFields.get(0).toString();
			dateModified = dateCataloged;
		}
		System.out.println("Date cataloged: " + dateCataloged);
		// dateCataloged is the first date field, dateModified the last one

		// TODO: All the below is wrong.
		// We can't get the date cataloged and modified from here, it's not in
		// this data.  Need to use selcatalog to get more info in the dump, then
		// join it with this data as we output the XML records.
		if ( dateCataloged != null ) {
			int start = dateModified.indexOf("$d");
			int end = dateModified.indexOf("$l");
			if ( start > 0 ) {
				dateModified = dateModified.substring(start+2, end);
			}

			start = dateCataloged.indexOf("$u");
			if ( start > 0 ) {
				// this field seems to be last all the time
				dateCataloged = dateCataloged.substring(start+2);
			}
		}
		*/
        
        additionalAttributesType.setDateEntered(dateCataloged);
        additionalAttributesType.setLastUpdated(dateModified);
        additionalAttributesType.setHarvestable("false");
        additionalAttributesType.setFastAddFlag("false");
  
        additionalAttributesType.setSupressFromPublic(shadowed);
        additionalAttributesType.setStatus(status);    	
        return additionalAttributesType;
    }
    
}
