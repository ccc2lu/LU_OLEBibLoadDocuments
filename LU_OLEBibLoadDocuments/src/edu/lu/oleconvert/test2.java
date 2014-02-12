package edu.lu.oleconvert;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.marc4j.MarcXmlReader;
import org.marc4j.marc.Record;
import org.marc4j.marc.VariableField;
import org.xml.sax.InputSource;

import edu.lu.oleconvert.ole.Bib;

public class test2 {

	public static void main(String args[]) {
		//testReplaceXML();
		//checkHoldingsRecords();
		testMatcher();
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
