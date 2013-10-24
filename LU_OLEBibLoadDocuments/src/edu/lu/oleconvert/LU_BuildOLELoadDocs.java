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

import org.marc4j.MarcWriter;
import org.marc4j.MarcXmlWriter;
import org.marc4j.MarcXmlReader; // ccc2 added
import org.marc4j.marc.Record;
import org.marc4j.marc.VariableField;
import org.marc4j.marc.Leader;

import OLEBibLoadDocuments.edu.indiana.libraries.OLEBibLoadDocuments.classes.BuildRequestDocument;
import edu.indiana.libraries.LoadDocstore.jaxb.*;
import edu.lu.oleconvert.ole.InstanceCollection;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;
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
    public static final String MARC_FORMAT = "marc";
    public static final String CATEGORY_WORK = "work";

    private static XMLSerializer getXMLSerializer(BufferedWriter out) {
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

        // create the serializer
        XMLSerializer l_serializer = new XMLSerializer(of);
        //serializer.setOutputByteStream(out);        
        l_serializer.setOutputCharStream(out);
        return l_serializer;
    }

    private static XMLSerializer serializer = null;
    
    private static void marshallObjext(Object object,Marshaller marshaller, BufferedWriter out){
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
    
    private static Marshaller getMarshaller(Class classObject){
        JAXBContext jc = null;
        try {
            jc = JAXBContext.newInstance(classObject);
        } catch (JAXBException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        XMLSerializer serial;
        Marshaller marshaller = null;
        try {
            marshaller = jc.createMarshaller();
            //marshaller.setProperty("com.sun.xml.internal.bind.namespacePrefixMapper", new NameSpaceMapper());
            marshaller.setProperty("com.sun.xml.bind.marshaller.NamespacePrefixMapper", new LU_NamespacePrefixMapper());
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,true);
            // TODO: I need to get the <content> tags to have CDATA wrappers around their values
            // Create a CDataContentHandler or set 
            // I probably should actual
            marshaller.setProperty("com.sun.xml.bind.marshaller.CharacterEscapeHandler",
            	    			   new NullCharacterEscapeHandler());
        } catch (JAXBException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return marshaller;
    }



    public static void main(String[] args) {
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
        LU_BuildInstance instanceBuilder = new LU_BuildInstance("/mnt/bigdrive/bibdata/allcallnums.txt", 
        														"/mnt/bigdrive/bibdata/allcallnumsshelvingkeys.txt",
        														"/mnt/bigdrive/bibdata/allcallnumsitemnumbers.txt",
        														"/mnt/bigdrive/bibdata/allcallnumsanalytics.txt",        														
        		                                                "/mnt/bigdrive/bibdata/allitems.txt");
        InstanceCollection ic = new InstanceCollection();
        
        System.out.println("Starting ...");
        
        /* ccc2 -- not doing this anymore
        try {
            connection = MakeConnection.creatPool().getConnection();
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
         */

        System.out.println("Args: " + args[0] + ", " + args[1]);
        
        loadprops = loadProps(args[0]);
        oracleprops = loadProps(loadprops.getProperty("oracle.properties"));
        instanceprops = loadProps(loadprops.getProperty("instance.properties"));
        // OracleReader buildBib = new OracleReader(oracleprops); // ccc2 -- not using this
        marshaller = getMarshaller(RequestType.class);
        int counter = 0;

        try {
            outFile = new BufferedWriter(new FileWriter(args[1]));
            
        } catch (IOException e) {
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
        	inFile = new BufferedReader(new FileReader(args[2]));
        	System.out.println("Reading in map of catalog keys to dates, shadowed values, statuses ...");
        	counter = 0;
        	while(inFile.ready()) {
        		line = inFile.readLine();
        		parts = line.split("\\|");
        		key = "a" + parts[0];
        		//System.err.println("K=" + key + ", V=" + line);
        		KeyToDate.put(key, line);
        		counter++;
        		if ( counter % 100000 == 0 ) {
        			System.out.println(counter + " records mapped ...");
        		}
        	}
        	System.out.println("Done reading in catalog keys map");
        	inFile.close();
        } catch(Exception e) {
        	System.err.println("Unable to read in key-to-date mapping: " + e.toString());
        	e.printStackTrace(System.err);
        }
        PrintWriter output = new PrintWriter(new BufferedWriter(outFile));
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
        	
        	System.out.println("Creating OLE ingest documents ...");
        	counter = 0;
            // ccc2 -- new loop over all records, perhaps?
            // not sure this accounts for holdings in addition to bibs, though
            MarcXmlReader reader = new MarcXmlReader(new FileInputStream(args[3]));
            int limit = 50, curr = 0;
            while (reader.hasNext() && curr++ < limit ) {
            	Record xmlrecord = reader.next();
            	//System.err.println("ID is: " + xmlrecord.getLeader());

            	// ccc2 -- catalog was only needed to get some extra data elements, which we get from a 
            	// separate file that I generated using selcatalog
            	// Catalog catalog = (Catalog) JPADriver.getObject(resultSet,Catalog.class);

                out = new ByteArrayOutputStream();
                writer = new MarcXmlWriter(out,"UTF-8");
                writer.write(xmlrecord);
                writer.close();
                String marcXML=out.toString("UTF-8"); // ccc2 -- this is what we already have -- could be we just need
                									  // to process it one record at a time?  
            	request=BuildRequestDocument.buildRequest(loadprops.getProperty("load.user"));
            	//request=BuildRequestDocument.buildIngestDocument(request,Integer.toString(catalog.getCatalogKey()),BIBLIOGRAPHIC,MARC_FORMAT,CATEGORY_WORK,xmlrecord,catalog);
            	request=buildIngestDocument(request,
            								xmlrecord.getLeader().toString(),
            								BIBLIOGRAPHIC,MARC_FORMAT,CATEGORY_WORK,
            								xmlrecord, marcXML);
            	instanceBuilder.buildInstanceCollection(record, ic);
            	// Here would be a great place to generate instance records too, since we already have the catalog record in hand
            	
            	//  marshaller = getMarshaller(RequestType.class);
            	
            	// output.print(marshallObjext(request,marshaller));      
            	marshallObjext(request,marshaller, outFile);
        		counter++;
        		if ( counter % 10000 == 0 ) {
        			System.out.println(counter + " ingest documents created ...");
        		}
            }
            System.out.println("Done creating ingest documents");
            outFile.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (org.marc4j.MarcException e) {
        	
        }
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
        String catkey = record.getVariableField("001").toString().split(" ")[1];
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
