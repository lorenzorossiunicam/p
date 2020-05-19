package org.processmining.plugins.tsanalyzer;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.plugins.tsanalyzer.gui.TSTimeAnnotationGUI;

public class Visualization {

	@Plugin(name = "@0 Transition system annotation visualization", returnLabels = { "Visualized Transition System Annotation" }, returnTypes = { JComponent.class }, parameterLabels = { "Transition System Annotation to visualize" }, userAccessible = true)
	@Visualizer
	public static JComponent visualize(PluginContext context, AnnotatedTransitionSystem ts) {

		
			if (ts != null) {
				return new TSTimeAnnotationGUI(context, ts.getTransitionSystem(), ts.getTimeAnnotation());
			} else {
				return new JLabel("FAILED: transition system is no longer available!");
			}
	}
}
