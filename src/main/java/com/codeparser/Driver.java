/**
 * Copyright (c) 2017 Brian Green
 * Licensed under Apache 2.0 license
 * http://opensource.org/licenses/Apache-2.0
 */
package codesmell;

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
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String file_data = bCodeParser.removeComments(bCodeParser.fileToString("/Users/brian/Desktop/bCodeParser.java"));
		//The Bracket class handles zero length head/body without a problem, (but not nulls)
		Bracket rack = new Bracket("Global Scope", file_data);
		String lines[] = bCodeParser.fileToLines("/Users/brian/Desktop/bCodeParser.java");
		//rack.print();
		SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
        		new bCodeViewer(rack.getJTree(lines));
            }
        });
		m2ManyArgs(rack, lines, 1);
		
	}

}
