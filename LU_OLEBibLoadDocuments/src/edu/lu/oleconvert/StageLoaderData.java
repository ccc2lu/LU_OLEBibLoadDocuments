package edu.lu.oleconvert;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import migration.SirsiCallNumber;
import migration.SirsiCallNumberID;
import migration.SirsiItem;

import org.apache.commons.lang3.StringUtils;

import edu.lu.oleconvert.LU_BuildInstance;
import edu.lu.oleconvert.LU_DBLoadInstances;

public class StageLoaderData {

	public static EntityManagerFactory ole_emf;
	public static EntityManager ole_em;
	public static EntityManagerFactory migration_emf;
	public static EntityManager migration_em;

	public static void main(String args[]) {

		migration_emf = Persistence.createEntityManagerFactory("olemigration");
		migration_em = migration_emf.createEntityManager();

		
		String dumpdir = args[0];
		stageSirsiFiles(dumpdir + "/mod.allcallnums.txt", dumpdir + "/mod.allcallnumsshelvingkeys.txt", dumpdir + "/mod.boundwiths.txt", 
		                dumpdir + "/mod.allcallnumsitemnumbers.txt", dumpdir + "/mod.allcallnumsanalytics.txt", 
		                dumpdir + "/mod.allitems.txt", -1);
	}
	
	public static void stageSirsiFiles(String callNumbersFilename, String shelvingKeysFilename,
			String boundWithsFileName, String itemNumbersFilename, 
			String analyticsFilename,  String itemsFilename, int limit) {
		BufferedReader callNumbersReader, boundWithsReader, shelvingKeysReader, 
		itemNumbersReader, analyticsReader, itemsReader;
		try {
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			LU_DBLoadInstances.Log("Clearing out callnumber and item tables in migration database ...");
			EntityTransaction migration_transaction = migration_em.getTransaction();
			migration_transaction.begin();
			javax.persistence.Query q = migration_em.createNativeQuery("DELETE FROM callnumbers");
			q.executeUpdate();
			q = migration_em.createNativeQuery("DELETE FROM items");
			q.executeUpdate();
			migration_transaction.commit();

			File callNumbersFile = new File(callNumbersFilename);
			callNumbersReader = new BufferedReader(new FileReader(callNumbersFilename));
			boundWithsReader = new BufferedReader(new FileReader(boundWithsFileName));
			shelvingKeysReader = new BufferedReader(new FileReader(shelvingKeysFilename));
			itemNumbersReader = new BufferedReader(new FileReader(itemNumbersFilename));
			analyticsReader = new BufferedReader(new FileReader(analyticsFilename));
			itemsReader = new BufferedReader(new FileReader(itemsFilename));
			LU_DBLoadInstances.Log("Staging call number records in migration database, time is: " + df.format(Calendar.getInstance().getTime()));
			//String workingdir = "/mnt/bigdrive/bibdata/sirsidump/20131211";
			String workingdir = callNumbersFile.getParent();
			PrintWriter writer = new PrintWriter(workingdir + "/testoutput.txt", "UTF-8");
			int curr = 0, increment = 50000;
			SirsiCallNumber scn;

			migration_transaction.begin();
			while(callNumbersReader.ready() && (limit < 0 || curr < limit)) {
				// There should be the same number of lines in all 4 files containing callnum data,
				// and they should all be sorted the same way to line 1 goes with line 1 goes with line 1, etc.
				// I'm building those files myself, so I can guarantee it, heh
				// With that in mind, we'll just read lines from all 4 files at once
				scn = new SirsiCallNumber();
				String line = callNumbersReader.readLine();
				String shelvingKeysLine = shelvingKeysReader.readLine();
				String itemNumbersLine = itemNumbersReader.readLine();
				writer.println("Item Numbers Line: " + itemNumbersLine);
				String analyticsLine = analyticsReader.readLine(); 
				String fields[] = line.split("\\|");
				// Can't just assign the return value of Arrays.asList(fields) to callNumberFields --
				// that method returns an immutable, fixed-size list
				ArrayList<String> callNumberFields = new ArrayList<String>();
				callNumberFields.addAll(Arrays.asList(fields));

				scn.setCat_key(Integer.parseInt(fields[0]));
				scn.setCallnum_key(Integer.parseInt(fields[1]));
				scn.setAnalytic_pos(Integer.parseInt(fields[2]));
				scn.setLevel(fields[3]);
				scn.setNum_copies(Integer.parseInt(fields[4]));
				scn.setNum_call_holds(Integer.parseInt(fields[5]));
				scn.setClassification(fields[6]);
				scn.setNum_reserve_control_recs(Integer.parseInt(fields[7]));
				scn.setNum_academic_reserves(Integer.parseInt(fields[8]));
				scn.setLibrary(fields[9]);
				scn.setNum_visible_copies(Integer.parseInt(fields[10]));
				scn.setShadowed(fields[11]);

				List<String> tmpfields;
				String tmpStr;
				// Now add the shelving key, item number, and analytics data to callNumberFields
				// The shelving keys may also contain pipes, so they are exported into their own file
				// The first two fields on each line of that file are the call number 
				// and catalog keys, so we skip them, then add the rest onto the end
				// of the callNumberFields array.
				fields = shelvingKeysLine.split("\\|");
				tmpfields = Arrays.asList(fields).subList(2, fields.length);
				tmpStr = StringUtils.join(tmpfields, "|");
				scn.setShelving_key(tmpStr.trim());

				// Now we repeat that process for the itemNumbersLine and analyticsLine
				fields = itemNumbersLine.split("\\|");
				// I found a few callnumbers which contained 4 fields
				// in the output from from selcallnum -iS -oKD.  This 
				// should have only been 3 fields, and it turns out this
				// was because the item number field, output by -D from selcallnum,
				// can have | characters in it.  Sirsi's own MARC export of the catalog
				// didn't include the info after the pipe in the "call number" subfield
				// of the 999 field for an item -- it went in subfield v.  So, I'm stripping
				// it off here by only using fields[2] as the callnumber.
				//tmpfields = Arrays.asList(fields).subList(2, fields.length);
				tmpfields = Arrays.asList(fields).subList(2, 3);
				tmpStr = StringUtils.join(tmpfields, "|");
				scn.setCall_number(tmpStr.trim());

				fields = analyticsLine.split("\\|");
				tmpfields = Arrays.asList(fields).subList(2, fields.length);
				tmpStr = StringUtils.join(tmpfields, "|");
				scn.setAnalytics(tmpStr.trim());

				migration_em.persist(scn);

				if ( ++curr % increment == 0 ) {
					migration_transaction.commit();
					migration_em.clear(); // TODO: testing this to see if it fixes memory problems
					LU_DBLoadInstances.Log(System.out, "Staged call number " + curr, LU_DBLoadInstances.LOG_INFO);
					migration_transaction.begin();
				}
			}
			migration_transaction.commit();

			LU_DBLoadInstances.Log("Staging bound-withs in migration database, time is: " + df.format(Calendar.getInstance().getTime()));
			curr = 0;
			int boundwith_increment = 1000;
			migration_transaction.begin();
			// boundwiths exported with the command 
			// selbound -oKPcdy > boundwiths.data
			// The fields are:
			// 0 : child record's catalog key
			// 1 : child record's callnum key
			// 2 : parent record's catalog key
			// 3 : parent record's callnum key
			// 4 : user access rights of creator, values like "CAT", "CATSERIALS", "ACQ"
			// 5 : create date
			// 6 : library
			while ( boundWithsReader.ready() ) {
				String boundWithsLine = boundWithsReader.readLine();
				String[] fields = boundWithsLine.split("\\|");
				if ( fields[0].length() == 0 || fields[1].length() == 0 ) {
					LU_DBLoadInstances.Log(System.err, "No cat key or call num key for boundwith: " + boundWithsLine,
							LU_DBLoadInstances.LOG_WARN);

				} else {
					//System.err.println("Selecting, fields[0] = " + fields[0] + ", fields[1] = " + fields[1] + ", ...");
					SirsiCallNumberID scnid = new SirsiCallNumberID(Integer.parseInt(fields[0]), Integer.parseInt(fields[1]));
					scn = migration_em.find(SirsiCallNumber.class, scnid);
					//TypedQuery<SirsiCallNumber> query = migration_em.createQuery("SELECT s FROM SirsiCallNumber s WHERE s.Id.cat_key=" + fields[0] + " and s.Id.callnum_key=" + fields[1], SirsiCallNumber.class);
					//query.setHint("org.hibernate.cacheable", true);
					//List<SirsiCallNumber> results = query.getResultList();
					if ( scn != null ) {
						//scn = results.get(0);
						scn.setParent_cat_key(Integer.parseInt(fields[2]));
						scn.setParent_callnum_key(Integer.parseInt(fields[3]));
						scn.setCreator_access(fields[4]);
						scn.setBound_create_date(fields[5]);
						// "library" is always the same, "LEHIGH", and we already have that in the callnumber
						// record, so we ignore it.
						migration_em.persist(scn);
						//System.err.println("Persisting ...");
						if ( ++curr % boundwith_increment == 0 ) {
							migration_transaction.commit();
							migration_em.clear(); // TODO: testing this to see if it fixes memory problems
							LU_DBLoadInstances.Log(System.out, "Staged call number " + curr, LU_DBLoadInstances.LOG_INFO);
							migration_transaction.begin();
						}
					}
				}	
			}
			migration_transaction.commit();

			LU_DBLoadInstances.Log("Staging item records in migration database, time is: " + df.format(Calendar.getInstance().getTime()));
			curr = 0;
			increment = 25000;
			String line = "";
			//String fields[];
			List<String> itemNumberFields;
			//SirsiItem item;
			migration_transaction.begin();
			while(itemsReader.ready() && (limit < 0 || curr < limit)) {
				// Only one file to read from this time
				line = itemsReader.readLine();
				SirsiItem item = new SirsiItem();
				String[] fields = line.split("\\|");

				item.setCat_key(Integer.parseInt(fields[0]));
				item.setCallnum_key(Integer.parseInt(fields[1]));
				item.setItem_key(Integer.parseInt(fields[2]));
				item.setLast_used_date(fields[3]);
				item.setNum_bills(Integer.parseInt(fields[4]));
				item.setNum_charges(Integer.parseInt(fields[5]));
				item.setNum_total_charges(Integer.parseInt(fields[6]));
				item.setFirst_created_date(fields[7]);
				item.setNum_holds(Integer.parseInt(fields[8]));
				item.setHouse_charge(Integer.parseInt(fields[9]));
				item.setHome_location(fields[10]);
				item.setCurr_location(fields[11]);
				item.setLast_changed_date(fields[12]);
				item.setPermanent(fields[13]);
				item.setPrice(Integer.parseInt(fields[14]));
				item.setRes_type(Integer.parseInt(fields[15]));
				item.setLast_user_key(Integer.parseInt(fields[16]));
				item.setType(fields[17]);
				item.setRecirc_flag(fields[18]);
				item.setInventoried_date(fields[19]);
				item.setTimes_inventoried(Integer.parseInt(fields[20]));
				item.setLibrary(fields[21]);
				item.setHold_key(Integer.parseInt(fields[22]));
				item.setLast_discharged_date(fields[23]);
				item.setAccountability(fields[24]);
				item.setShadowed(fields[25]);
				item.setDistribution_key(fields[26]);
				item.setTransit_status(fields[27]);
				item.setReserve_status(fields[28]);
				item.setPieces(Integer.parseInt(fields[29]));
				item.setMedia_desk(fields[30]);
				// There is only this one item-line that has an empty extra
				// field in it for some reason.  
				if ( fields[32].trim().equals("737988-1001") ) {
					item.setBarcode(fields[32].trim());
					item.setNum_comments(Integer.parseInt(fields[33]));
				} else {
					item.setBarcode(fields[31].trim());
					item.setNum_comments(Integer.parseInt(fields[32]));
				}

				migration_em.persist(item);

				if ( ++curr % increment == 0 ) {
					migration_transaction.commit();
					migration_em.clear(); // TODO: testing this to see if it fixes memory problems
					LU_DBLoadInstances.Log(System.out, "On item number " + curr, LU_DBLoadInstances.LOG_INFO);
					migration_transaction.begin();
				}

			}
			migration_transaction.commit();

			LU_DBLoadInstances.Log("Done staging callnumbers and items in migration database, time is: " + df.format(Calendar.getInstance().getTime()));

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
}
