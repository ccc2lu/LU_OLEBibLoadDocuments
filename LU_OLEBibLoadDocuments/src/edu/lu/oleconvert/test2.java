package edu.lu.oleconvert;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.mutable.MutableInt;
import org.marc4j.MarcXmlReader;
import org.marc4j.marc.Record;
import org.marc4j.marc.VariableField;
import org.xml.sax.InputSource;

import edu.lu.oleconvert.ole.Bib;

public class test2 {

	public static void main(String args[]) {
		//testReplaceXML();
		//checkHoldingsRecords();
		testTokenizer();
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
