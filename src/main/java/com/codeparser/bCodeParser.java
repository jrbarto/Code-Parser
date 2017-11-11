package com.codeparser;
/*
 * bCodeParser.java created by Brian Green
 *
 */


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class bCodeParser {
	public bCodeParser() {}
	//keywords that can be mistaken as a class initializer. eg. Class(){} vs if(){}
	public static final Map<String, Integer> code_keywords;
	static
	{
		code_keywords = new HashMap<>();
		code_keywords.put("if", 1);
		code_keywords.put("while", 1);
		code_keywords.put("switch", 1);
		code_keywords.put("try", 1);
		code_keywords.put("else if", 1);
		code_keywords.put("for", 1);
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
				if (Character.isJavaIdentifierStart(head.charAt(i))){ onWord = true;}
			}else if (!Character.isJavaIdentifierPart(head.charAt(i))) return head.substring(0, i);
		}
		return "";
	}

	static public boolean isMethod(String head){
		int words = 0;
		boolean onWord = false;
		char c;
		for (int i = 0; i < head.length(); i++){
			c = head.charAt(i);
			//System.out.println(head);
			if (c == '(') return words > 1 || !code_keywords.containsKey(firstWord(head));
			if (!onWord){
				if (Character.isJavaIdentifierStart(c)){ words++; onWord = true;}
			}else if (!Character.isJavaIdentifierPart(c)) onWord = false;
		}
		return false;
	}

	static public int totalChildren(String sourceCode){
		int bracket_num = 0, i, result = 0, maxdepth = 0;
		for (i = 0; i < sourceCode.length(); i++){
			switch(sourceCode.charAt(i)){
			case '{':
				bracket_num++;
				if (bracket_num > maxdepth) maxdepth = bracket_num;
				break;
			case '}':
				bracket_num--;
				if (bracket_num == 0) result++;;
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
		int i,ii;
		for (i = 0; i < str.length(); i++){
			if (str.charAt(i) == '/' && i + 1 < str.length()){
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
		return str;
	}



	static public String getHeader(String sourceCode, int pos){
		int inPar = 0;
		for (int i = pos; i > -1; i--){
			switch(sourceCode.charAt(i)){
			case ')':
				inPar++;
				continue;
			case '(':
				if (inPar < 1)
					return removeVoid(sourceCode.substring(i + 1, pos + 1));
				inPar--;
				continue;
			case '{':
			case '}':
			case ';':
				if (inPar < 1)
					return removeVoid(sourceCode.substring(i + 1, pos + 1));
			}
		}
		return removeVoid(sourceCode.substring(0, pos + 1));
	}

	static public String getArgument(String sourceCode, int pos){
		int inPar = 0;
		for (int i = pos; i > -1; i--){
			switch(sourceCode.charAt(i)){
			case ')':
				inPar++;
				continue;
			case '(':
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
				if (pos == i)
					return "";
				if (inPar < 1)
					return removeVoid(sourceCode.substring(i + 1, pos + 2));
			}
		}
		return removeVoid(sourceCode.substring(0, pos + 2));
	}
	static public int totalArgs(String body){
		int inBra = 0, index = 0, i;
		for (i = 0; i < body.length(); i++){
			switch(body.charAt(i)){
			case '{':
				inBra++;
				continue;
			case '}':
				inBra--;
				continue;
			case ';':
				if (inBra > 0) continue;
				index++;
			}
		}
		return index;
	}
	static public void setArgs(String body, String arguments[]){
		int inBra = 0, index = 0, i;
		for (i = 0; i < body.length(); i++){
			switch(body.charAt(i)){
			case '{':
				inBra++;
				continue;
			case '}':
				inBra--;
				continue;
			case ';':
				if (inBra > 0) continue;

				arguments[index++] = getArgument(body, i - 1);

			}
		}
	}
	static public String[] getBracket(String sourceCode, int index){
		int bracket_num = 0, i, ii;
		for (i = 0; i < sourceCode.length(); i++){
			if (bracket_num == 1) break;
			switch(sourceCode.charAt(i)){
			case '{':
				bracket_num++;
				break;
			case '}':
				bracket_num--;
				break;
			}
		}
		bracket_num = 1;
		for (ii = i; ii < sourceCode.length(); ii++){
			if (sourceCode.charAt(ii) == '{') {
				bracket_num++;}
			else if (sourceCode.charAt(ii) == '}') {bracket_num--;if (bracket_num == 0)break;}
		}
		if (index == 0) {
			return new String[]{ getHeader( sourceCode, i - 2), sourceCode.substring(i, ii)};
		}
		ii += 2;
		if (ii >= sourceCode.length()) return new String[]{"",""};

		return getBracket(sourceCode.substring(ii), --index);
	}
}
