package org.processmining.plugins.ywl.replayer.ui;

import info.clearthought.layout.TableLayout;

import com.fluxicon.slickerbox.factory.SlickerFactory;

/**
* @author David Piessens
* @email d.a.m.piessens@student.tue.nl
* @version May 29, 2010
*/
public class IntroductionStep extends ReplayStep {
	private static final long serialVersionUID = 1858631217943805055L;

	public IntroductionStep(){
		initComponents();
	}
	
	public boolean precondition() {
		return true;
	}

	public void readSettings() {}

	private void initComponents() {
    	double size[][] = {{TableLayout.FILL},{TableLayout.FILL}};
    	setLayout(new TableLayout(size));
    	String body = "<p>This wizard will guide you through the process of configuring this log replay.</p>";
    	body += "<p>The configuration options for this miner can be divided into three categories:<ol>";
    	body += "<li>mapping yawl net's nodes to event class (if there is no such mapping yet),</li>";
    	body += "<li>options for replay algorithm to be used, and</li>";
    	body += "<li>options for configuring replay parameters.</li></ol>";
    	body += "The wizard will allow you to configure these three categories in the given order.</p>";
    	add(SlickerFactory.instance().createLabel("<html><h1>Introduction</h1>" + body), "0, 0, l, t");
	}
}
