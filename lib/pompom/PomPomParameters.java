package org.processmining.plugins.pompom;

import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.log.parameters.ClassifierParameter;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

public class PomPomParameters implements ClassifierParameter {

	private XEventClassifier classifier;
	
	public PomPomParameters(XLog log, Petrinet net) {
		if (log.getClassifiers().isEmpty()) {
			setClassifier(new XEventAndClassifier(new XEventNameClassifier(), new XEventLifeTransClassifier()));
		} else {
			/*
			 * Guess a classifier based on the first 10 events in the log.
			 * The classifier that returns the most transition labels, wins.
			 */
			Set<String> transitionLabels = new HashSet<String>();
			for (Transition transition : net.getTransitions()) {
				if (!transition.isInvisible()) {
					transitionLabels.add(transition.getLabel());
				}
			}
			setClassifier(log.getClassifiers().get(0));
			int maxMatch = 0;
			for (XEventClassifier classifier : log.getClassifiers()) {
				int match = 0;
				int n = 0;
				for (XTrace trace : log) {
					if (n > 10) {
						continue;
					}
					for (XEvent event : trace) {
						if (n > 10) {
							continue;
						}
						String label = classifier.getClassIdentity(event);
						if (transitionLabels.contains(label)) {
							match++;
						}
						n++;
					}
				}
				if (match > maxMatch) {
					setClassifier(classifier);
					maxMatch = match;
				}
			}
		}
	}

	public XEventClassifier getClassifier() {
		return classifier;
	}

	public void setClassifier(XEventClassifier classifier) {
		this.classifier = classifier;
	}
}
