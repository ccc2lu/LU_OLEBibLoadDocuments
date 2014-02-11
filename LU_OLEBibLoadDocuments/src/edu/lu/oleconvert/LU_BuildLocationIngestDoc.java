package edu.lu.oleconvert;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;

//import OLEBibLoadDocuments.edu.indiana.libraries.OLEBibLoadDocuments.classes.BuildRequestDocument;

import edu.lu.oleconvert.locationingest.IngestLocation;
import edu.lu.oleconvert.locationingest.LocationGroup;

public class LU_BuildLocationIngestDoc {

	public static void main(String args[]) {
		String locationsFilename = "/mnt/bigdrive/bibdata/LehighData/Lehigh Locations.csv"; // this won't change with each export
		String outputFilename = "/mnt/bigdrive/bibdata/LehighData/LehighLocations.xml";
		BufferedReader locationsReader;
		BufferedWriter output;
		XMLSerializer loc_serializer = null;
		Marshaller loc_marshaller = getMarshaller(LocationGroup.class);
		LocationGroup lg = new LocationGroup();
		try {
			locationsReader = new BufferedReader(new FileReader(locationsFilename));
			output = new BufferedWriter(new FileWriter(outputFilename));
			locationsReader.readLine(); // strip off the line of headers
			while (locationsReader.ready()) {
				String line = locationsReader.readLine();
				String parts[] = line.split(",");
				IngestLocation loc = new IngestLocation();
				loc.setLocationName(parts[0]);
				loc.setLocationCode(parts[1]);
				loc.setLocationLevelCode(parts[2]);
				if ( parts[2].equals("INSTITUTION") ) {
					loc.setParentLocationCode("");
				} else {
					loc.setParentLocationCode(parts[3]);
				}
				lg.getLocations().add(loc);

			}

        	OutputFormat of = new OutputFormat("xml", "ISO-8859-1", true);
        	of.setOmitXMLDeclaration(false);
            of.setPreserveSpace(true);
            of.setIndenting(true);
            of.setIndent(5);
            of.setLineWidth(80);
            of.setLineSeparator("\n");
            //of.set
			loc_serializer = getXMLSerializer(output);
			loc_serializer.setOutputFormat(of);
			
			marshallObjext(lg, loc_marshaller, loc_serializer);
			//LU_BuildOLELoadDocs.marshallObjext(lg, loc_marshaller, output);
			System.out.println("Done creating location ingest document");
		} catch(Exception e) {
			LU_DBLoadInstances.Log(System.err, "Unable to read in Lehigh locations: " + e.getMessage(), LU_DBLoadInstances.LOG_ERROR);
			e.printStackTrace(System.err);
		}	
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
    
}
