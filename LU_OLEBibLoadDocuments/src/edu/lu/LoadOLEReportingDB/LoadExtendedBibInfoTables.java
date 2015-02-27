package edu.lu.LoadOLEReportingDB;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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

import javax.persistence.TypedQuery;

import org.hibernate.Query;
import org.marc4j.MarcWriter;
import org.marc4j.MarcXmlReader;
import org.marc4j.MarcXmlWriter;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.xml.sax.InputSource;

import edu.lu.oleconvert.LU_DBLoadInstances;
import edu.lu.oleconvert.ole.Bib;
import edu.lu.oleconvert.ole.OLEHoldings;
import edu.lu.LoadOLEReportingDB.OLEDBUtil;

public class LoadExtendedBibInfoTables {

	public static final int max_subfield_len = 2048;

	public static OLEDBUtil db = new OLEDBUtil();

	public static void main(String args[]) {
		MarcFactory factory = MarcFactory.newInstance();
		String tmpfilename = "/home/ccc2/tmp.marcxml";
		//if ( args != null && args[0] != null && args[0].length() > 0 ) {
		//	tmpfilename = args[0]; // TODO: test this
		//}
		tmpfilename = args[0];
		int batchsize = 1000;
		if ( args.length >= 2 ) {
			try {
				batchsize = Integer.parseInt(args[1]);
			} catch (Exception e) {
				System.err.println("Unable to parse batchsize from parameter: " + args[1]);
			}
		}
		System.err.println("Using batch size " + batchsize);
		String fromDate = "";
		if ( args.length >= 3 ) {
			fromDate = args[2];
		}
		boolean allrecs = ( fromDate.length() == 0 );
		BigInteger total_bibs = new BigInteger("0");
		String querystr = "";
		if ( allrecs ) {
			System.err.println("Truncating existing extended bib info tables");
			querystr = "select count(*) from ole_ds_bib_t";
			// Truncate the tables
			db.ole_reporting_tx.begin();
			javax.persistence.Query query = db.ole_reporting_em.createNativeQuery("truncate table bib_leader_t;");
			query.executeUpdate();
			query = db.ole_reporting_em.createNativeQuery("truncate table bib_controlfield_t");
			query.executeUpdate();
			query = db.ole_reporting_em.createNativeQuery("truncate table bib_datafield_t");
			query.executeUpdate();
			query = db.ole_reporting_em.createNativeQuery("truncate table bib_subfield_t");
			query.executeUpdate();
			db.ole_reporting_tx.commit();
		} else {			
			querystr = "select count(*) from ole_ds_bib_t b where b.date_updated>='" + fromDate + "'";
		}
		javax.persistence.Query get_num_bibs_query = db.ole_reporting_em.createNativeQuery(querystr);
		total_bibs = (BigInteger) get_num_bibs_query.getResultList().get(0);
		System.out.println("Total bibs: " + total_bibs);
		if ( allrecs ) {
			System.err.println("Computing extended info for all " + total_bibs + " bib records");
		} else {
			System.err.println("Updating extended info for " + total_bibs + " records updated since " + fromDate);
		}
		ByteArrayOutputStream out;
		MarcWriter writer;
		String marcXML;


		System.out.println("Temporary MarcXML file:" + tmpfilename);
		int curr = 0;
		DateFormat dateformatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		MarcXmlReader reader;
		System.out.println("Starting load of extended bib info, time is: " + dateformatter.format(Calendar.getInstance().getTime()));

		while ( curr < total_bibs.intValue() ) {
			List<Bib> bib_batch;
			if ( allrecs ) {
				bib_batch = db.getBatchOfBibs(curr, batchsize);
			} else {
				bib_batch = db.getBatchOfBibs(curr, batchsize, fromDate);
			}
			db.ole_reporting_tx.begin();
			Iterator it = bib_batch.iterator();
			while ( it.hasNext() ) {
				Bib b = (Bib)it.next();
				//System.err.println("Updating extended info for bib " + b.getId());
				marcXML = b.getContent();

				try {
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
						if (!allrecs) {
							// if we are importing all records, then we truncated the tables earlier
							clearFields(b);
						}
						Leader leader = new Leader(b, rec.getLeader());
						db.ole_reporting_em.persist(leader);
						for ( org.marc4j.marc.ControlField cf : rec.getControlFields() ) {
							ControlField new_cf = new ControlField();
							new_cf.setBib(b);
							new_cf.setTag(cf.getTag());
							new_cf.setValue(cf.getData());
							db.ole_reporting_em.persist(new_cf);
						}

						for ( org.marc4j.marc.DataField df : rec.getDataFields() ) {
							DataField new_df = new DataField();
							new_df.setBib(b);
							new_df.setTag(df.getTag());
							new_df.setInd1(String.valueOf(df.getIndicator1()));
							new_df.setInd2(String.valueOf(df.getIndicator2()));
							db.ole_reporting_em.persist(new_df);
							for ( org.marc4j.marc.Subfield sub : df.getSubfields() ) {
								SubField new_sub = new SubField();
								new_sub.setDfield(new_df);
								new_sub.setCode(String.valueOf(sub.getCode()));
								// subfield value is the only one we truncate
								new_sub.setValue(sub.getData().substring(0, Math.min(sub.getData().length(), max_subfield_len)));
								db.ole_reporting_em.persist(new_sub);								
							}
						}

					} else {
						System.err.println("No record");
					}

					temp_in.close();
				} catch (Exception e) {
					System.err.println(e.getMessage());
					e.printStackTrace(System.err);
				}
			}

			db.ole_reporting_tx.commit();
			curr += batchsize;
			System.out.println("On bib " + curr);
		}
		System.out.println("Finished load of extended bib info, time is: " + dateformatter.format(Calendar.getInstance().getTime()));
	}

	public static void clearFields(Bib b) {
		javax.persistence.Query query = db.ole_reporting_em.createNativeQuery("select count(bib_id) from bib_leader_t where bib_leader_t.bib_id = ?");
		query.setParameter(1, b.getId());
		int numrows = ((BigInteger) query.getSingleResult()).intValue();
		if ( numrows > 0 ) {
			// If there's a leader, then check other tables.
			// If there's no leader, don't bother
			System.err.println("Deleting existing extended info for for bib " + b.getId());
			query = db.ole_reporting_em.createNativeQuery("delete from bib_leader_t where bib_leader_t.bib_id= ?");
			query.setParameter(1, b.getId());
			query.executeUpdate();

			TypedQuery<ControlField> cf_query = db.ole_reporting_em.createQuery("select cf from ControlField cf where cf.bib.id=" + b.getId(), ControlField.class);
			List<ControlField> cf_results = cf_query.getResultList();
			if ( cf_results.size() > 0 ) {
				for ( ControlField cf : cf_results ) {
					System.err.println("Removing existing " + cf.getTag() + " field for bib " + b.getId());
					db.ole_reporting_em.remove(cf);
				}
			}

			// repeat for datafields and subfields ...
			TypedQuery<DataField> df_query = db.ole_reporting_em.createQuery("select df from DataField df where df.bib.id=" + b.getId(), DataField.class);
			List<DataField> df_results = df_query.getResultList();
			if ( df_results.size() > 0 ) {
				for ( DataField df : df_results ) {
					TypedQuery<SubField> sub_query = db.ole_reporting_em.createQuery("select sub from SubField sub where sub.dfield.id=" + df.getId(), SubField.class);
					List<SubField> sub_results = sub_query.getResultList();
					if ( sub_results.size() > 0 ) {
						for ( SubField sub : sub_results ) {
							System.err.println("Removing existing " + df.getTag() + sub.getCode() + " field for bib " + b.getId());
							db.ole_reporting_em.refresh(sub);
						}
					}
					System.err.println("Removing existing " + df.getTag() + " field for bib " + b.getId());
					db.ole_reporting_em.remove(df);
				}
			}
		}

	}

	public static Leader fetchLeader(Bib b) {
		Leader leader = null;
		TypedQuery<Leader> leader_query = db.ole_reporting_em.createQuery("select l from Leader l where l.bib_id=" + b.getId(), Leader.class);
		List<Leader> leader_results = leader_query.getResultList();
		if ( leader_results.size() > 0 ) {
			System.err.println("Found leader for bib " + b.getId());
			leader = leader_results.get(0);
		}
		return leader;
	}
}
