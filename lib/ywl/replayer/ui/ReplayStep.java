package org.processmining.plugins.ywl.replayer.ui;

import javax.swing.JPanel;

/**
* @author David Piessens
* @email d.a.m.piessens@student.tue.nl
* @version May 29, 2010
*/
public abstract class ReplayStep extends JPanel {
	private static final long serialVersionUID = -6447646079319355783L;
	public abstract boolean precondition();
	public abstract void readSettings();
}