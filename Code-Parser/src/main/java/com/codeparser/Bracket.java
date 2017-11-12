/*
 * Bracket.java created by Brian Green
 *
 */
package com.codeparser;

public class Bracket{
	public String head;
	public String body;
	public int max_depth;
	boolean hasMethod;
	public Bracket children[];
	public String arguments[];

	public Bracket(){}

	public Bracket(String bracket[]){
		this.head = bracket[0];
		this.body = bracket[1];
		hasMethod = false;
		max_depth = bCodeParser.totalArgs(body);
		if (max_depth > 0){
			arguments = new String[max_depth];
			bCodeParser.setArgs(body, arguments);
		}else arguments = null;
		max_depth = bCodeParser.totalChildren(body); //returns two short values (maxdepth, total_children) represented as an int
		if (max_depth > 0) {
			children = new Bracket[max_depth & 0xFFFF];
			for (int i = 0; i < children.length; i++){
				children[i] = new Bracket(bCodeParser.getBracket(new String(body), i));
				if (!hasMethod && bCodeParser.isMethod(children[i].head))
					hasMethod = true;
			}
			max_depth >>= 16;
		}else children = null;
	}

	public void print(){
		int i;
		System.out.println("\nBracket Head  \""+head+"\"");
		if (arguments != null){
			System.out.println("Arguments:");
			for (i = 0; i < arguments.length; i++){
				System.out.println(arguments[i]);
			}
		}else System.out.println("NO Arguments!!");
		if (children != null){
			if (hasMethod){
				System.out.println("Methods:");
				for (i = 0; i < children.length; i++)
					if (bCodeParser.isMethod(children[i].head)) System.out.println(children[i].head + "{}");
			}else System.out.println("NO Methods!!");
			System.out.println("--------------"+head + "---Children Start------------");
			for (i = 0; i < children.length; i++)
				children[i].print();
			System.out.println("--------------"+head + "---Children End------------");
		}else System.out.println("NO Methods!!\nNO Children!!");
	}

}
