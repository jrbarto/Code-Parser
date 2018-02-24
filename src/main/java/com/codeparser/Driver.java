/**
 * Copyright (c) 2017 Brian Green
 * Licensed under Apache 2.0 license
 * http://opensource.org/licenses/Apache-2.0
 */
package com.codeparser;

import javax.swing.SwingUtilities;
public class Driver{

	public Driver() {}


	public static void m2ManyArgs(Bracket rack, String lines[], int max){
		String l[] = rack.listMethods();
		for(int i = 0, totalArgs; i < l.length; i++){
			totalArgs = bCodeParser.totalArgsInMethod(l[i]);
			if (totalArgs > max){
				System.out.println(bCodeParser.methodLineNumber(l[i], lines));
			}
		}
	}

	public static void mTooManyArgs(String filename, int max){
		//Bracket removes comments from source. Therefore, the line numbers are changed and can't be used.
		Bracket rack = new Bracket(filename);
		//Note: bCodeParser.fileToLines() should not be looped if the filename is not changed in the loop
		//This will force the program to read from file multiple times (exponentially slower)
		String lines[] = bCodeParser.fileToLines(filename);
		//listMethods() creates a list of all methods. Note: This includes the methods inside all children, if any.
		String l[] = rack.listMethods();
		for(int i = 0, totalArgs; i < l.length; i++){
			totalArgs = bCodeParser.totalArgsInMethod(l[i]);
			if (totalArgs > max){
				System.out.println(bCodeParser.methodLineNumber(l[i], lines));
			}
		}
	}

	public static void printGlobal(String filename) {
		//Filename should be checked for null before calling this method
		Bracket rack = new Bracket(filename);
		rack.print();
	}

	public static void displayGlobal(String filename){
		//Filename should be checked for null before calling this method
		Bracket rack = new Bracket(filename);
		String lines[] = bCodeParser.fileToLines(filename);
		//rack.print();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new bCodeViewer(rack.getJTree(lines));
			}
		});
		//m2ManyArgs(rack, lines, 1);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		//I'm using eclipse to debug. Therefore, there are no args supplied to main(String[])
		/*if (args.length < 1) {
			System.out.println("[Error] You must specify a file argument to parse.");
			System.exit(1);
		}*/
		//mTooManyArgs("/Users/brian/Desktop/bCodeParser.java", -1);
		displayGlobal("/Users/brian/Desktop/bCodeParser.java");
	}

}
