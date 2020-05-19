package org.processmining.plugins.tsanalyzer.gui;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.processmining.models.graphbased.directed.transitionsystem.State;
import org.processmining.models.graphbased.directed.transitionsystem.Transition;
import org.processmining.plugins.tsanalyzer.annotation.Annotation;
import org.processmining.plugins.tsanalyzer.annotation.StateAnnotation;
import org.processmining.plugins.tsanalyzer.annotation.TransitionAnnotation;

public class StatisticsAnnotationPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private final StatisticsAnnotationTable table;

	public StatisticsAnnotationPanel() {
		super(new BorderLayout());
		table = new StatisticsAnnotationTable();
		add(new JScrollPane(table), BorderLayout.CENTER);
	}

	public void showStateAnnotation(StateAnnotation annotation) {
		if (annotation != null) {
			setBorder(BorderFactory.createTitledBorder(getString(annotation.getState())));
			showAnnotation(annotation);
		} else {
			setBorder(BorderFactory.createTitledBorder("no annotations"));
			table.clear();
			validate();
		}
	}

	public void showTransitionAnnotation(TransitionAnnotation annotation) {
		if (annotation != null) {
			setBorder(BorderFactory.createTitledBorder(getString(annotation.getTransition())));
			showAnnotation(annotation);
		} else {
			setBorder(BorderFactory.createTitledBorder("no annotations"));
			table.clear();
			validate();
		}
	}

	private void showAnnotation(Annotation annotation) {
		table.setAnnotation(annotation);
		validate();
	}

	private String getString(State state) {
		String result = "state ";
		if (state != null) {
			result += state.getLabel();
		} else {
			result += "unknown";
		}
		return result;
	}

	private String getString(Transition transition) {
		String result = "transition ";
		if (transition != null) {
			String source = getString(transition.getSource());
			String target = getString(transition.getTarget());
			if ((source != null) && (target != null)) {
				String label = transition.getLabel();
				if (label == null) {
					label = "unknown";
				}
				result = "[" + source + "]  " + label + "  [" + target + "]";

			}
		} else {
			result += "unknown";
		}
		return result;
	}
}
