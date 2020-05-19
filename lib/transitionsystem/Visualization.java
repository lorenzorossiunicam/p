package org.processmining.plugins.transitionsystem;

import javax.swing.JComponent;

import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.connections.ConnectionManager;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.util.ui.scalableview.ScalableViewPanel;
import org.processmining.models.connections.transitionsystem.TransitionSystemConnection;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.directed.DirectedGraphElementWeights;
import org.processmining.models.graphbased.directed.transitionsystem.AcceptStateSet;
import org.processmining.models.graphbased.directed.transitionsystem.StartStateSet;
import org.processmining.models.graphbased.directed.transitionsystem.State;
import org.processmining.models.graphbased.directed.transitionsystem.Transition;
import org.processmining.models.graphbased.directed.transitionsystem.TransitionSystem;
import org.processmining.models.jgraph.ProMJGraphVisualizer;

/**
 * Visualization support for transition systems. - State and transition weights
 * are visualized using line widths: the higher the weight, the wider the line.
 * - Start states are visualized using a green filling. - Accept states are
 * visualized using a red filling. - Start and accept states are visualized
 * using a yellow (=green+red) filling.
 * 
 * @author HVERBEEK
 * 
 */
public class Visualization {

	private static final float[] INITIAL_DASH_PATTERN = { 7f };
	private ScalableViewPanel mainPanel;

	@Plugin(name = "@1 Show Transition System", level = PluginLevel.PeerReviewed, returnLabels = { "Visualization for Transition System" }, returnTypes = { JComponent.class }, parameterLabels = { "Transition System" }, userAccessible = true)
	@Visualizer
	public JComponent visualize(PluginContext context, TransitionSystem ts) {
		/**
		 * Will hold the weights, start states, and accept states.
		 */
		DirectedGraphElementWeights weights = new DirectedGraphElementWeights();
		StartStateSet starts = new StartStateSet();
		AcceptStateSet accepts = new AcceptStateSet();

		/**
		 * 1. Tries to get connected transition weights from the framework.
		 */
		ConnectionManager cm = context.getConnectionManager();
		try {
			//			System.out.println("Checking for connection");
			TransitionSystemConnection tsc = cm.getFirstConnection(TransitionSystemConnection.class, context, ts);
			//			System.out.println("Checked for connection: " + settings);
			if (tsc.hasWeights()) {
				weights = tsc.getObjectWithRole(TransitionSystemConnection.WEIGHTS);
			}
			starts = tsc.getObjectWithRole(TransitionSystemConnection.STARTIDS);
			accepts = tsc.getObjectWithRole(TransitionSystemConnection.ACCEPTIDS);

		} catch (ConnectionCannotBeObtained e) {
			/**
			 * No connected transition weights found, no problem.
			 */
		}

		/**
		 * 2. Based on the connected objects found: updates visualization.
		 */
		if (!weights.isEmpty()) {
			/**
			 * Set the line widths according to the weights. To avoid getting
			 * ridiculous line widths: linewidth=ln(weight).
			 */
			for (State state : ts.getNodes()) {
				state.getAttributeMap().put(AttributeMap.LINEWIDTH,
						new Float(1 + Math.log(Math.E) * Math.log(weights.get(state.getIdentifier(), 1))));
			}
			for (Transition trans : ts.getEdges()) {
				trans.getAttributeMap().put(
						AttributeMap.LINEWIDTH,
						new Float(1
								+ Math.log(Math.E)
								* Math.log(weights.get(trans.getSource().getIdentifier(), trans.getTarget()
										.getIdentifier(), trans.getIdentifier(), 1))));
			}
		}
		if (!starts.isEmpty() || !accepts.isEmpty()) {
			for (State state : ts.getNodes()) {

				/**
				 * Note that, in fact, the set of start states is the the set of
				 * start state ids.
				 */
				if (starts.contains(state.getIdentifier())) {
					/**
					 * This state is a start state.
					 */
					state.getAttributeMap().put(AttributeMap.DASHPATTERN, INITIAL_DASH_PATTERN);
				}
				if (accepts.contains(state.getIdentifier())) {
					/**
					 * This state is an accept state.
					 */
					state.setAccepting(true);
				}
			}
		}

		mainPanel = ProMJGraphVisualizer.instance().visualizeGraph(context, ts);
		return mainPanel;
	}
}
