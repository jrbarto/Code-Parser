/**
 * Copyright (c) 2017 Brian Green
 * Licensed under Apache 2.0 license
 * http://opensource.org/licenses/Apache-2.0
 */
package com.codeparser;

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

public class bCodeViewer extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	JTree tree;
	JTextArea ta;
	public bCodeViewer() {}
	
	public bCodeViewer(DefaultMutableTreeNode tree) {
		this.ta = new JTextArea(40, 50);
		this.ta.setEditable(false);
		this.tree = new JTree(tree);
        add(new JScrollPane(this.tree));
        this.tree.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
              clicked(event);
            }
          });
        this.setMinimumSize(new Dimension(600,200));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("Bracket Viewer");       
        this.pack();
        this.setVisible(true);
	}
	
	void clicked(MouseEvent event) {
	    TreePath tp = tree.getPathForLocation(event.getX(), event.getY());
	    if (tp == null) return;
	    Object tarray[] = tp.getPath();
	    if (tarray.length > 1 && tarray[tarray.length - 2].toString().startsWith("Body[")){
	    	ta.setText(tp.getLastPathComponent().toString());
	    	switch (JOptionPane.showConfirmDialog(null, new JScrollPane(ta), "Bracket Viewer (Body)", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE)) {
	    		case JOptionPane.OK_OPTION:
	    			//System.out.println("Closing Confirm Dialog");
	    			break;
	    }
	    }
	  }
}
