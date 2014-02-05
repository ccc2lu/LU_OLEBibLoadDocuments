package edu.lu.oleconvert;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

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
		Marshaller loc_marshaller = LU_BuildOLELoadDocs.getMarshaller(LocationGroup.class);
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
			loc_serializer = LU_BuildOLELoadDocs.getXMLSerializer(output);
			loc_serializer.setOutputFormat(of);
			
			LU_BuildOLELoadDocs.marshallObjext(lg, loc_marshaller, loc_serializer);
			//LU_BuildOLELoadDocs.marshallObjext(lg, loc_marshaller, output);
			System.out.println("Done creating location ingest document");
		} catch(Exception e) {
			LU_DBLoadInstances.Log(System.err, "Unable to read in Lehigh locations: " + e.getMessage(), LU_DBLoadInstances.LOG_ERROR);
			e.printStackTrace(System.err);
		}	
	}
}
