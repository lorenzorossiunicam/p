package org.processmining.plugins.pompom;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.log.dialogs.ClassifierPanel;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

public class PomPomDialog extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9060553585824462312L;

	public PomPomDialog(XLog log, PomPomParameters parameters) {
		double size[][] = { { TableLayoutConstants.FILL }, { TableLayoutConstants.FILL } };
		setLayout(new TableLayout(size));
		List<XEventClassifier> availableClassifiers = new ArrayList<XEventClassifier>();
		availableClassifiers.addAll(log.getClassifiers());
		if (availableClassifiers.isEmpty()) {
			availableClassifiers.add(new XEventAndClassifier(new XEventNameClassifier(), new XEventLifeTransClassifier()));
			availableClassifiers.add(new XEventNameClassifier());
		}
		add(new ClassifierPanel(availableClassifiers, parameters), "0, 0");
	}
}
