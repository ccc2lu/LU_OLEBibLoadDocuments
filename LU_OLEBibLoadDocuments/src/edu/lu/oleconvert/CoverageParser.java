package edu.lu.oleconvert;

import java.util.ArrayList;
import java.util.List;

import edu.lu.oleconvert.ole.Coverage;

public class CoverageParser {

	private Tokenizer tokenizer;
	List<Coverage> coverages;
	Coverage currCoverage;
	String token = "";
	private boolean onYear;
	private String lastnum;
	
	public CoverageParser() {
		super();
		coverages = new ArrayList<Coverage>();
		onYear = false;
	}
	
	public CoverageParser(Tokenizer t) {
		this();
		tokenizer = t;
	}
	
	public Tokenizer getTokenizer() {
		return tokenizer;
	}

	public void setTokenizer(Tokenizer tokenizer) {
		this.tokenizer = tokenizer;
	}

	public List<Coverage> getCoverages() {
		return coverages;
	}

	public void setCoverages(List<Coverage> coverages) {
		this.coverages = coverages;
	}

	public void parse() {
		onYear = false;
		do {
			token = tokenizer.nextToken();
			currCoverage = new Coverage();
			SeriesExpr();
		} while(tokenizer.getPos() < tokenizer.getStr().length()); 
	}
	
	private void SeriesExpr() {
		if ( isNumber(token) ) {
			// Seems like a reasonable start year, we won't have anything from before then, right?
			if ( getNumber(token) > 1600 ) {
				onYear = true;	
			}
			lastnum = token;
		}
		token = tokenizer.nextToken();
		if ( token.equals("-") ) {
			token = tokenizer.nextToken();
			RangeExpr();
		} else if ( token.equals(",") ) {
			token = tokenizer.nextToken();
			if ( isNumber(token) ) {
				if ( onYear ) {
					currCoverage.setStartDate(lastnum);
					currCoverage.setEndDate(lastnum);
				} else {
					currCoverage.setStartVolume(lastnum);
					currCoverage.setEndVolume(lastnum);
				}
				coverages.add(currCoverage);
				currCoverage = new Coverage();
			}
			SeriesExpr();
		}
	}
	
	public void RangeExpr() {
		if ( onYear ) {
			currCoverage.setStartDate(lastnum);
		} else {
			currCoverage.setStartVolume(lastnum);
		}
		if ( isNumber(token) ) {
			if ( onYear) {
				currCoverage.setEndDate(token);
			} else {
				currCoverage.setEndVolume(token);
			}
		} 
		coverages.add(currCoverage);
		currCoverage = new Coverage();
	}
	
	private boolean isNumber(String token) {
		return token.matches("\\d+");
	}
	
	private int getNumber(String token) {
		int num = 0;
		try {
			num = Integer.parseInt(token);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return num;
	}
}
