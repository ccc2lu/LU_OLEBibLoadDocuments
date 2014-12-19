package edu.lu.olemod;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.marc4j.MarcWriter;
import org.marc4j.MarcXmlReader;
import org.marc4j.MarcXmlWriter;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;
import org.xml.sax.InputSource;

import edu.lu.oleconvert.ole.Bib;
import edu.lu.oleconvert.ole.Item;
import edu.lu.oleconvert.ole.OLEHoldings;

public class Fix035 {

	public static void main(String args[]) throws IOException {

		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		String datestr = df.format(Calendar.getInstance().getTime());
		
		MarcFactory factory = MarcFactory.newInstance();
		OLEDBUtil db = new OLEDBUtil();
		BigInteger total_bibs = new BigInteger("0");
		javax.persistence.Query get_num_bibs_query = db.ole_em.createNativeQuery("select count(*) from ole_ds_bib_t");
		total_bibs = (BigInteger) get_num_bibs_query.getResultList().get(0);
		System.out.println("Total bibs: " + total_bibs);
		ByteArrayOutputStream out;
		MarcWriter writer;
		String marcXML;
		String tmpfilename = "/home/ccc2/tmp.marcxml";
		if ( args[0] != null && args[0].length() > 0 ) {
			tmpfilename = args[0]; // TODO: test this
		}
		System.out.println("Temporary MarcXML file:" + tmpfilename);
		int curr = 0;
		int batchsize = 10000;

		MarcXmlReader reader;
		while ( curr < total_bibs.intValue() ) {
			List<Bib> bib_batch = db.getBatchOfBibs(curr, batchsize);
			db.ole_tx.begin();
			Iterator it = bib_batch.iterator();
			while ( it.hasNext() ) {
				Bib b = (Bib)it.next();
				System.out.println("On bib " + b.getId());
				marcXML = b.getContent();
				//System.out.println("MarcXML: " + marcXML.length() + ", " + marcXML);
				FileOutputStream temp_out = new FileOutputStream(tmpfilename);
				Pattern p = Pattern.compile("^0+([1-9]\\d+)");
				Matcher m;
				byte[] bytes = marcXML.getBytes(Charset.forName("UTF-8"));
				//out.write(bytes, 0, bytes.length);
				temp_out.write(bytes);
				temp_out.close();

				FileInputStream temp_in = new FileInputStream(tmpfilename);
				InputSource inputsource = new InputSource(temp_in);
				//inputsource.setEncoding("ISO-8859-1");
				inputsource.setEncoding("UTF-8");
				reader = new MarcXmlReader(inputsource);
				if ( reader.hasNext() ) {
					Record rec = reader.next();
					//System.out.println("Record is " + rec.toString());
					List<VariableField> tcns = rec.getVariableFields("035");
					for ( VariableField tcn : tcns ) {
						DataField tcn_df = (DataField)tcn;
						List<Subfield> subfields = tcn_df.getSubfields();
						for ( Subfield subfield : subfields ) {
							String tcnstr = subfield.getData();
							System.out.println("TCN before changing: " + subfield.getData());
							if ( tcnstr.startsWith("(OCoLC") ) {
								tcnstr = tcnstr.substring(7);
								if ( tcnstr.length() > 12 ) {
									tcnstr = tcnstr.substring(0, tcnstr.length() - 6);
								}
								System.out.println("After removing (OCoLC) and potentially last 6 digits, tcn is: " + tcnstr);
								m = p.matcher(tcnstr);
								if ( m.matches() ) {
									String tcnpart = m.group(1);
									tcnstr = "(OCoLC)" + tcnpart;
									System.out.println("After changing: " + tcnstr);
									// TODO: write the change back to the bib record and persist to the database
									// NB: be sure to also change the DATE_UPDATED field of the bib records
									// updated this way so that they get reindexed by a delta-index
									subfield.setData(tcnstr);
									out = new ByteArrayOutputStream();
									writer = new MarcXmlWriter(out, "UTF-8");
									writer.write(rec);
									//out.flush();
									out.close();
									writer.close();
									//marcXML = out.toString("ISO-8859-1");
									marcXML = out.toString("UTF-8");
									String xmldecl = "<\\?xml version=\"1.0\" encoding=\"UTF-8\"\\?>";
									marcXML = marcXML.replaceFirst(xmldecl, "");
									b.setContent(marcXML);									
									b.setDateUpdated(datestr);
									db.ole_em.persist(b);
								} else {
									System.out.println("TCN does NOT match, not changing: " + subfield.getData());
								}
							} else {
								System.out.println("Not an OCoLC TCN, not changing: " + subfield.getData());								
							}
						}
					}
				} else {
					System.err.println("No record");
				}

				temp_in.close();

			}
			db.ole_tx.commit();
			curr += batchsize;
			System.out.println("On bib " + curr);
		}
	}

}
