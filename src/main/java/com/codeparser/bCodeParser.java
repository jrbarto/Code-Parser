/**
 * Copyright (c) 2017 Brian Green
 * Licensed under Apache 2.0 license
 * http://opensource.org/licenses/Apache-2.0
 */

package com.codeparser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;



public class bCodeParser {
	//keywords that can be mistaken as a class initializer. eg. Class(){} vs if(){}
	public static final Map<String, Integer> code_keywords;
	public static final Map<String, Integer> code_keywords2;
    static
    {
    	code_keywords = new HashMap<String, Integer>();
    	code_keywords2 = new HashMap<String, Integer>();
    	code_keywords.put("if", 1);
    	code_keywords.put("while", 1);
    	code_keywords.put("switch", 1);
    	code_keywords.put("catch", 1);
    	code_keywords.put("for", 1);
    	code_keywords2.put("case", 1);
    	code_keywords2.put("else", 1);
    }
    
	static public String[] fileToLines(String filename){
		//returns empty array for errors. Therefore, it will be no need to check for null pointer
		Path path = null;
		Object[] tmp = null;
		Stream<String> bytes = null;
		path = Paths.get(filename);
		try{
			bytes = Files.lines(path, StandardCharsets.UTF_8);
			tmp = bytes.toArray();
			bytes.close();
			}catch (IOException e) {
				System.out.println("Error occured while opening " + filename);
				//e.printStackTrace();
				return new String[]{};
			}
		return Arrays.copyOf(tmp, tmp.length, String[].class);	
	}
	static public String fileToString(String filename){
		//returns empty string for errors. Therefore, it will be no need to check for null pointer
		Path path = null;
		byte[] bytes = null;
		path = Paths.get(filename);
		try{
			bytes = Files.readAllBytes(path);
			}catch (IOException e) {
				System.out.println("Error occured while opening " + filename);
				//e.printStackTrace();
				return "";
			}
		return new String(bytes, StandardCharsets.UTF_8);		
	}
	
	static public String firstWord(String head){
		boolean onWord = false;
		for (int i = 0; i < head.length(); i++){
			if (!onWord){
				if (Character.isJavaIdentifierStart(head.charAt(i))) onWord = true;
			}else if (!Character.isJavaIdentifierPart(head.charAt(i))) return head.substring(0, i);
		}
		return "";
	}
	
	static public boolean isMethod(String head){
		int words = 0, inQuoteState = 0;
		boolean onWord = false;
		char c;
		for (int i = 0; i < head.length(); i++){
			c = head.charAt(i);
			switch(c){
				case '\'':
					if ((inQuoteState & 2) == 0)
						inQuoteState = ((~inQuoteState) & 1);
					continue;
				case '"':
					if ((inQuoteState & 1) == 0)
						inQuoteState = ((~inQuoteState) & 2);
					continue;
				case '\\':
					if (i + 1 < head.length()){
						switch(head.charAt(i + 1)){
							case '\\':
							case '"':
							case '\'':
								i++;
								continue;
						}
					}
					continue;
				case '(':
					if (inQuoteState != 0) continue;
					switch(words){
						case 0:
							return false;
						case 1:
							return !code_keywords.containsKey(firstWord(head));
						case 2:
							return !code_keywords2.containsKey(firstWord(head));
					}
					return true;
					default:
						if (inQuoteState != 0) continue;
						if (!onWord){
							if (Character.isJavaIdentifierStart(c)){ words++; onWord = true;}
						}else if (!Character.isJavaIdentifierPart(c)) onWord = false;
						continue;
			}
			
		}
		return false;
	}
	
	static public int totalChildren(String sourceCode){
		int bracket_num = 0, i, result = 0, maxdepth = 0, inQuoteState = 0;
		for (i = 0; i < sourceCode.length(); i++){
			switch(sourceCode.charAt(i)){
				case '\'':
					if ((inQuoteState & 2) == 0)
						inQuoteState = ((~inQuoteState) & 1);
					continue;
				case '"':
					if ((inQuoteState & 1) == 0)
						inQuoteState = ((~inQuoteState) & 2);
					continue;
				case '\\':
					if (i + 1 < sourceCode.length()){
						switch(sourceCode.charAt(i + 1)){
							case '"':
							case '\'':
							case '\\':
								i++;
								continue;
						}
					}
					continue;
				case '{':
					if (inQuoteState != 0) continue;
					bracket_num++;
					if (bracket_num > maxdepth) maxdepth = bracket_num;
					break;
				case '}':
					if (inQuoteState != 0) continue;
					bracket_num--;
					if (bracket_num == 0) result++;
					break;
			}
    	}
		//result and maxdepth will never reach signed short max_value ((0xFFFF / 2) - 1)
		return maxdepth << 16 | result;
	}
	
	static public String removeVoidForward(String str){
		switch(str.charAt(0)){
			case ' ':					
			case '\n':
			case '\t':
				return removeVoidForward(str.substring(1));	
			}
		return str;
	}
	
	static public String removeVoidReverse(String str){
		switch(str.charAt(str.length() - 1)){
			case ' ':					
			case '\n':
			case '\t':
				return removeVoidReverse(str.substring(0, str.length() - 1));	
			}
		return str;
	}
	static public String removeVoid(String str){
		return removeVoidReverse(removeVoidForward(str));
	}
	
	static public String removeComments(String str){
		int i,ii, inQuoteState = 0;
		for (i = 0; i < str.length(); i++){
			switch (str.charAt(i)){
			case '\'':
				if ((inQuoteState & 2) == 0)
				inQuoteState = ((~inQuoteState) & 1);
				continue;
			case '"':
				if ((inQuoteState & 1) == 0)
				inQuoteState = ((~inQuoteState) & 2);
				continue;
			case '\\':
				if (i + 1 < str.length()){
					switch(str.charAt(i + 1)){
						case '\\':
						case '"':
						case '\'':
							i++;
							continue;
					}
				}
				continue;
			case '/':
				if (inQuoteState == 0 && i + 1 < str.length()){
					switch(str.charAt(i + 1)){
						case '/':	
							for (ii = i + 2; ii < str.length(); ii++){
								if (str.charAt(ii) == '\n'){
									return (ii + 1 < str.length()) ? removeComments(str.substring(0, i) + str.substring(ii + 1, str.length())) : removeComments(str.substring(0, i));
								}
							}
						break;
						case '*':
							for (ii = i + 2; ii < str.length(); ii++){
								if (str.charAt(ii) == '*' && ii + 1 < str.length() && str.charAt(ii + 1) == '/'){
									return (ii + 2 < str.length()) ? removeComments(str.substring(0, i) + str.substring(ii + 2, str.length())) : removeComments(str.substring(0, i));
								}
						}
						break;
					}
				}
			}
    	}
		return str;
	}
	
	
	
	static public String getHeader(String sourceCode, int pos){
		int inPar = 0;
		int inQuoteState = 0;
		for (int i = pos; i > -1; i--){
			switch(sourceCode.charAt(i)){
				case '\'':
					if ((inQuoteState & 2) == 0)
					inQuoteState = ((~inQuoteState) & 1);
					continue;
				case '"':
					if ((inQuoteState & 1) == 0)
					inQuoteState = ((~inQuoteState) & 2);
					continue;
				case ')':
					if (inQuoteState != 0) continue;
					inPar++;
					continue;
				case '\\':
					if (i - 1 > -1){
						switch(sourceCode.charAt(i - 1)){
							case '\\':
							case '"':
							case '\'':
								i--;
								continue;
						}
					}
					continue;
				case '(':
					if (inQuoteState != 0) continue;
					if (inPar < 1) 
						return removeVoid(sourceCode.substring(i + 1, pos + 1));
					inPar--;
					continue;
				case '{':					
				case '}':
				case ';':
					if (inPar < 1 && inQuoteState == 0)
						return removeVoid(sourceCode.substring(i + 1, pos + 1));
			}
    	}
		return removeVoid(sourceCode.substring(0, pos + 1));
	}
	
	static public String getArgument(String sourceCode, int pos){
		int inPar = 0, inQuoteState = 0;
		for (int i = pos; i > -1; i--){
			switch(sourceCode.charAt(i)){
				case ')':
					if (inQuoteState != 0) continue;
					inPar++;
					continue;
				case '\\':
					if (i - 1 > -1){
						switch(sourceCode.charAt(i - 1)){
							case '\\':
							case '"':
							case '\'':
								i--;
								continue;
						}
					}
					continue;
				case '\'':
					if ((inQuoteState & 2) == 0)
					inQuoteState = ((~inQuoteState) & 1);
					continue;
				case '"':
					if ((inQuoteState & 1) == 0)
					inQuoteState = ((~inQuoteState) & 2);
					continue;
				case '(':
					if (inQuoteState != 0) continue;
					if (inPar < 1) {
						if (pos == i)
							return "";
						return removeVoid(sourceCode.substring(i + 1, pos + 2));
					}
					inPar--;
					continue;
				case '{':					
				case '}':
				case ';':
					if (pos == i) return "";
					if (inPar < 1 && inQuoteState == 0)
						return removeVoid(sourceCode.substring(i + 1, pos + 2));
			}
    	}
		return removeVoid(sourceCode.substring(0, pos + 2));
	}
	
	static public int methodLineNumber(String head, String[] lines){
		String[] parts = head.split("\n");
		for (int i = 0, ii, iii; i < lines.length; i++){
			for (ii = 0, iii = 0; ii < parts.length; ii++)
				if (i+ii < lines.length && lines[i+ii].indexOf(parts[ii]) != -1) iii++;
			if (iii == parts.length) return i + ii;
		}
		return 0;//returns 0 on error
	}
	static public String argsInMethod(String head){
		// boolean isMethod() makes sure this string will have at least a word and "(*)"
		for (int i = 0; i < head.length(); i++)
			if (head.charAt(i) == '(') return head.substring(i+1, head.length()-1);
		return "";
	}
	
	static public int totalArgsInMethod(String head){
		String args = argsInMethod(head);
		int words = 0, comma = 0, inQuoteState = 0;
		char c;
		boolean onWord = false;
		for (int i = 0; i < args.length(); i++){
			c = args.charAt(i);
			switch (c){
				case '\'':
					if ((inQuoteState & 2) == 0)
						inQuoteState = ((~inQuoteState) & 1);
					continue;
				case '"':
					if ((inQuoteState & 1) == 0)
						inQuoteState = ((~inQuoteState) & 2);
					continue;
				case '\\':
					if (i + 1 < args.length()){
						switch(args.charAt(i + 1)){
							case '\\':
							case '"':
							case '\'':
								i++;
								continue;
						}
					}
					continue;
				case ',':
					if (inQuoteState != 0) continue;
					comma++;
					continue;
				default:
					if (inQuoteState != 0) continue;
					if (!onWord){
						if (Character.isJavaIdentifierStart(c)){ words++; onWord = true;}
					}else if (!Character.isJavaIdentifierPart(c)) onWord = false;
					continue;
			}
		}

		if (words < 1) return 0;
		return comma + 1;
	}
	
	static public int totalArgs(String body){
		int inBra = 0, inPar = 0, index = 0, i, inQuoteState = 0;
		for (i = 0; i < body.length(); i++){
			switch(body.charAt(i)){
				case '\\':
					if (i + 1 < body.length()){
						switch(body.charAt(i + 1)){
							case '\\':
							case '"':
							case '\'':
								i++;
								continue;
						}
					}
					continue;
				case '\'':
					if ((inQuoteState & 2) == 0)
					inQuoteState = ((~inQuoteState) & 1);
					continue;
				case '"':
					if ((inQuoteState & 1) == 0)
					inQuoteState = ((~inQuoteState) & 2);
					continue;
				case ')':
					if (inQuoteState != 0) continue;
					inPar--;
					continue;
				case '(':
					if (inQuoteState != 0) continue;
					inPar++;
					continue;
				case '{':
					if (inQuoteState != 0) continue;
					inBra++;
					continue;
				case '}':
					if (inQuoteState != 0) continue;
					inBra--;
					continue;
				case ';':
					if (inQuoteState != 0 || inBra > 0 || inPar > 0) continue;
					index++;
			}
    	}
		return index;
	}
	
	static public void setArgs(String body, String arguments[]){
		int inBra = 0, index = 0, i, inPar = 0, inQuoteState = 0;
		for (i = 0; i < body.length(); i++){
			
			switch(body.charAt(i)){
				case '\'':
					if ((inQuoteState & 2) == 0)
						inQuoteState = ((~inQuoteState) & 1);
					continue;
				case '"':
					if ((inQuoteState & 1) == 0)
						inQuoteState = ((~inQuoteState) & 2);
					continue;
				case '\\':
					if ( i + 1 < body.length()){
						switch(body.charAt(i + 1)){
							case '\\':
							case '"':
							case '\'':
								i++;
								continue;
						}
					}
					continue;
				case ')':
					if (inQuoteState != 0) continue;
					inPar--;
					continue;
				case '(':
					if (inQuoteState != 0) continue;
					inPar++;
					continue;
				case '{':
					if (inQuoteState != 0) continue;
					inBra++;
					continue;
				case '}':
					if (inQuoteState != 0) continue;
					inBra--;
					continue;
				case ';':
					if (inQuoteState != 0 || inBra > 0 || inPar > 0) continue;
					arguments[index++] = getArgument(body, i - 1);
			}
    	}
	}
	
	static public String[] getBracket(String sourceCode, int index){
		int bracket_num = 0, i, ii, inQuoteState = 0;
		char c;
		for (i = 0; i < sourceCode.length(); i++){
			if (bracket_num == 1) break;
			switch(sourceCode.charAt(i)){
				case '\'':
					if ((inQuoteState & 2) == 0)
						inQuoteState = ((~inQuoteState) & 1);
					continue;
				case '"':
					if ((inQuoteState & 1) == 0)
						inQuoteState = ((~inQuoteState) & 2);
					continue;
				case '\\':
					if (i + 1 < sourceCode.length())
						switch(sourceCode.charAt(i + 1)){
							case '\\':
							case '"':
							case '\'':
								i++;
								continue;
						}
					continue;
				case '{':
					if (inQuoteState != 0) continue;
					bracket_num++;
					continue;
				case '}':
					if (inQuoteState != 0) continue;
					bracket_num--;
					continue;
			}
    	}
		
		for (ii = i; ii < sourceCode.length(); ii++){
			c = sourceCode.charAt(ii);
			switch(c){
				case '\'':
					if ((inQuoteState & 2) == 0)
						inQuoteState = ((~inQuoteState) & 1);
					continue;
				case '"':
					if ((inQuoteState & 1) == 0)
						inQuoteState = ((~inQuoteState) & 2);
					continue;
				case '\\':
					if (ii + 1 < sourceCode.length()){
						switch(sourceCode.charAt(ii + 1)){
							case '\\':
							case '"':
							case '\'':
								ii++;
								continue;
						}
					}
					continue;
			}
			if (inQuoteState != 0) continue;
    		if (c == '{') bracket_num++;
    		else if (c == '}') {bracket_num--; if (bracket_num == 0) break;}
    	}
		if (index == 0) return new String[]{ getHeader(sourceCode, i - 2), sourceCode.substring(i, ii)};
		ii += 2;
		if (ii >= sourceCode.length()) return new String[]{"",""};
		return getBracket(sourceCode.substring(ii - 1), index - 1);
	}
	
}
