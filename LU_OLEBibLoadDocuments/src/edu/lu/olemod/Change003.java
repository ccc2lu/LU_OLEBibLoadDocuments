package edu.lu.olemod;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;

import org.marc4j.MarcXmlReader;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.xml.sax.InputSource;

import edu.lu.oleconvert.ole.Bib;
import edu.lu.oleconvert.ole.Item;
import edu.lu.oleconvert.ole.OLEHoldings;

public class Change003 {

	public static void main(String args[]) throws IOException {

		MarcFactory factory = MarcFactory.newInstance();
		OLEDBUtil db = new OLEDBUtil();
		BigInteger total_bibs = new BigInteger("0");
		javax.persistence.Query get_num_bibs_query = db.ole_em.createNativeQuery("select count(*) from ole_ds_bib_t");
		total_bibs = (BigInteger) get_num_bibs_query.getResultList().get(0);
		System.out.println("Total bibs: " + total_bibs);

		int curr = 0;
		int batchsize = 100;
		
		MarcXmlReader reader;
		while ( curr < total_bibs.intValue() ) {
			List<Bib> bib_batch = db.getBatchOfBibs(curr, batchsize);
			Iterator it = bib_batch.iterator();
			while ( it.hasNext() ) {
				Bib b = (Bib)it.next();
				System.out.println("On bib " + b.getId());
				String marcXML = b.getContent();
				PipedInputStream in = new PipedInputStream();
				PipedOutputStream out = new PipedOutputStream(in);
				InputSource inputsource = new InputSource(in);
				//inputsource.setEncoding("ISO-8859-1");
				inputsource.setEncoding("UTF-8");
				byte[] bytes = marcXML.getBytes();
				out.write(bytes, 0, bytes.length-1);
				out.flush();
				out.close();
				reader = new MarcXmlReader(inputsource);
				if ( reader.hasNext() ) {
					Record rec = reader.next();
					System.out.println("Record is " + rec.toString());
				} else {
					System.err.println("No record");
				}
				
				in.close();

			}
			curr += batchsize;
			System.out.println("On bib " + curr);
		}
	}

}
