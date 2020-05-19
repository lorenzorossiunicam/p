package org.processmining.plugins.ywl.replayer.ui;

import java.awt.BorderLayout;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import com.fluxicon.slickerbox.factory.SlickerFactory;

/**
 * @author David Piessens
 * @email d.a.m.piessens@student.tue.nl
 * @version May 29, 2010
 */

public class ExplanationStep extends ReplayStep {

	private static final long serialVersionUID = -4314563984761515562L;

	private static final String EXPLANATION_IMAGE_LOCATION = "";
	private static final String EXPLANATION_IMAGE_NAME = "explanation";
	private static final String EXPLANATION_IMAGE_EXT = ".png";

	public ExplanationStep() {
		initComponents();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.plugins.flex.replayer.ui.ReplayStep#precondition()
	 */
	public boolean precondition() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.plugins.flex.replayer.ui.ReplayStep#readSettings()
	 */
	public void readSettings() {
		// TODO Auto-generated method stub

	}

	private void initComponents() {
		// init instance
		setLayout(new BorderLayout());
		String body = "<p>The information in the resulting Performance Diagram has the following meaning:</p>";
		add(SlickerFactory.instance().createLabel("<html><h1>Explanation</h1>" + body), BorderLayout.NORTH);
		JLabel label = new JLabel(new ImageIcon(ExplanationStep.class.getResource(EXPLANATION_IMAGE_LOCATION
				+ EXPLANATION_IMAGE_NAME + EXPLANATION_IMAGE_EXT), EXPLANATION_IMAGE_NAME));
		add(label, BorderLayout.CENTER);
	}

}
