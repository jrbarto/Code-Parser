/**
 * Copyright (c) 2017 Brian Green
 * Licensed under Apache 2.0 license
 * http://opensource.org/licenses/Apache-2.0
 */

package com.codeparser;

import java.util.Arrays;

import javax.swing.tree.DefaultMutableTreeNode;

public class Bracket{
	private String head;
	private String body;
	private int max_depth;
	private boolean hasMethod;
	private Bracket children[];
	private String arguments[];

	public Bracket(){}
	
	public Bracket(String bracket[]){
		this.head = bracket[0];
		this.body = bracket[1];
		this.init();
	}
	public Bracket(String filename){
		//The Bracket class handles zero length head/body without a problem, (but not nulls)
		this.head = "Global Scope";
		this.body = bCodeParser.removeComments(bCodeParser.fileToString(filename));
		this.init();
	}
	public String getHead(){
		return head;
	}
	public String getBody(){
		return body;
	}
	public int getMax_depth(){
		return max_depth;
	}
	public boolean getHasMethod(){
		return hasMethod;
	}
	public Bracket[] getChildren(){
		return Arrays.copyOf(children, children.length);
	}
	public String[] getArguments(){
		return Arrays.copyOf(arguments, arguments.length);
	}
	public Bracket(Bracket dub){
		this.head = dub.head;
		this.body = dub.body;
		this.max_depth = dub.max_depth;
		this.hasMethod = dub.hasMethod;
		this.children = dub.getChildren();
		this.arguments = dub.getArguments();
	}
	
	private void init(){
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
	
	public void totalMethods(int result[]){
		if (bCodeParser.isMethod(head)) result[0]++;
		if (children != null)
			for (int i = 0; i < children.length; i++)
				 children[i].totalMethods(result);
	}
	
	private void getMethods(String result[], int index[]){
		if (bCodeParser.isMethod(head)) result[index[0]++] = head;
		if (children != null)
			for (int i = 0; i < children.length; i++)
				children[i].getMethods(result, index);
	}
	
	public String[] listMethods(){
		int tm[] = new int[1];
		totalMethods(tm);
		String result[] = new String[tm[0]]; tm[0] = 0;
		getMethods(result, tm);
		return result;
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
	
	public DefaultMutableTreeNode getJTree(String lines[]){
		int i;
		DefaultMutableTreeNode this_tree = new DefaultMutableTreeNode("Bracket \""+head+"\""), bd = null, args = null, ch = null, med = null;
		if (body.length() > 0){
			bd = new DefaultMutableTreeNode("Body["+body.length()+"]");
			bd.add(new DefaultMutableTreeNode(body));
			this_tree.add(bd);
		}
		
		if (arguments != null){
			args = new DefaultMutableTreeNode("Arguments["+arguments.length+"]");
			for (i = 0; i < arguments.length; i++){
				args.add(new DefaultMutableTreeNode(arguments[i]));
			}
			this_tree.add(args);
		}
		
		if (children != null){
			ch = new DefaultMutableTreeNode("Children["+children.length+"]");
			if (hasMethod){
				med = new DefaultMutableTreeNode("Methods");
				for (i = 0; i < children.length; i++)
					if (bCodeParser.isMethod(children[i].head)) med.add(new DefaultMutableTreeNode(children[i].head + " {Line:"+bCodeParser.methodLineNumber(children[i].head, lines)+", Args:"+bCodeParser.totalArgsInMethod(children[i].head)+"}"));
				this_tree.add(med);
			}
			for (i = 0; i < children.length; i++)
				ch.add(children[i].getJTree(lines));
			this_tree.add(ch);
		}
		
        return this_tree;
	}
	
}
