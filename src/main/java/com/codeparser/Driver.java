package com.codeparser;

public class Driver {

	public Driver() {
		// TODO Auto-generated constructor stub
	}

	public static void printGlobal(String fileArg) {
		// TODO Auto-generated method stub
		String file_data = bCodeParser.removeComments(bCodeParser.fileToString(fileArg));
		Bracket rack = new Bracket(new String[]{"global scope", file_data});
		rack.print();
	}

	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("[Error] You must specify a file argument to parse.");
			System.exit(1);
		}

		printGlobal(args[0]);
	}
}