package edu.lu.oleconvert;

import java.util.Arrays;
import java.util.List;

public class Tokenizer {

	private int pos;
	private String str;
	private List<String> breakchars;
	private static List<String> defaultbreakchars = Arrays.asList(new String[]{ ",", "-", " ", ":", ".", "(", ")", ";"});
	
	public Tokenizer() {
		super();
		pos = 0;
		breakchars = defaultbreakchars;
	}
	
	public Tokenizer(String str) {
		this();
		this.setStr(str);
	}
	
	public Tokenizer(String str, int pos) {
		this(str);
		this.pos = pos;
	}
	
	public Tokenizer(String str, int pos, String[] breakchars) {
		this(str, pos);
		this.breakchars = Arrays.asList(breakchars);
	}
	
	
	public int getPos() {
		return pos;
	}

	public void setPos(int pos) {
		this.pos = pos;
	}

	public String getStr() {
		return str;
	}

	public void setStr(String newstr) {
		this.str = newstr;
		str.replaceFirst("Electronic conference: ", "");
		str.replaceFirst("Electronic resource: ", "");
		str.replaceFirst("Electronic journal: ", "");
		this.pos = 0;
	}

	public List<String> getBreakchars() {
		return breakchars;
	}

	public void setBreakchars(List<String> breakchars) {
		this.breakchars = breakchars;
	}

	public String nextToken() {
		String tok = "";
		String ch;
		boolean stopchar = false;
		//do {
		while (!stopchar && pos < str.length()) {
			ch = str.substring(pos, pos+1);
			pos++;
			stopchar = breakchars.contains(ch);
			if ( stopchar && (tok.length() > 0) ) {
				pos--;
			} else {
				tok += ch;
			}
		}
		//System.out.println("Token is: " + tok);
		return tok;			
	}
	
	public static boolean isNumber(String token) {
		return token.matches("\\d+");
	}
	
	public static int getNumber(String token) {
		int num = 0;
		try {
			num = Integer.parseInt(token);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return num;
	}
}
