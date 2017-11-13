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
				System.out.print(l[i] + " totalargs = " + totalArgs);
				System.out.println(" LineNumber = " + bCodeParser.methodLineNumber(l[i], lines));
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
		displayGlobal("/Users/brian/Desktop/bCodeParser.java");
	}

}
