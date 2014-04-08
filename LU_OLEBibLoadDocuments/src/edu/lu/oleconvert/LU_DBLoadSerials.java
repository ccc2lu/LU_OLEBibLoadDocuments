package edu.lu.oleconvert;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import migration.Serial;

import edu.lu.oleconvert.ole.Deliver_ItemType;



public class LU_DBLoadSerials {
	
	public static EntityManagerFactory ole_emf;
	public static EntityManager ole_em;
	public static EntityManagerFactory migration_emf;
	public static EntityManager migration_em;
	
	public static void main(String args[]) {
		ole_emf = Persistence.createEntityManagerFactory("ole");
		ole_em = ole_emf.createEntityManager();
		EntityTransaction ole_tx = ole_em.getTransaction();
		
		migration_emf = Persistence.createEntityManagerFactory("olemigration");
		migration_em = migration_emf.createEntityManager();
		EntityTransaction migration_tx = migration_em.getTransaction();
		
		TypedQuery<Serial> query = migration_em.createQuery("SELECT s FROM Serial s", Serial.class);
		query.setHint("org.hibernate.cacheable", true);
		List<Serial> results = query.getResultList();
		for ( Serial s : results ) {
			System.out.println("Processing serial " + s.getId() + ", with bib " + s.getBibid());
			// Need to transform that bib id from Sirsi into an OLE bib id
			// docstore SOLR search, maybe?
			// Probably better to look in olemigration database while filling in bib records
			// to see if former id of bib matches bib id of any serials
			// Then fill in serial information ...
		}

	}
}
