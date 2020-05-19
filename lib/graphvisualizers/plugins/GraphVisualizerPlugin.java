package org.processmining.graphvisualizers.plugins;

import javax.swing.JComponent;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.plugins.VisualizeAcceptingPetriNetPlugin;
import org.processmining.activityclusterarray.models.ActivityClusterArray;
import org.processmining.activityclusterarray.models.graph.ActivityClusterArrayGraph;
import org.processmining.causalactivitygraph.models.CausalActivityGraph;
import org.processmining.causalactivitygraph.models.graph.CausalActivityGraphGraph;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.graphvisualizers.algorithms.GraphVisualizerAlgorithm;
import org.processmining.graphvisualizers.parameters.GraphVisualizerParameters;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.ViewSpecificAttributeMap;
import org.processmining.models.graphbased.directed.fuzzymodel.MutableFuzzyGraph;
import org.processmining.models.graphbased.directed.petrinet.InhibitorNet;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.ResetInhibitorNet;
import org.processmining.models.graphbased.directed.petrinet.ResetNet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.transitionsystem.TransitionSystem;
import org.processmining.models.jgraph.ProMJGraph;
import org.processmining.models.jgraph.visualization.ProMJGraphPanel;
import org.processmining.plugins.petrinet.PetriNetVisualization;
import org.processmining.plugins.transitionsystem.MinedTSVisualization;

public class GraphVisualizerPlugin extends GraphVisualizerAlgorithm{

	/*
	 * A bunch of visualizers for objects that use the JGraph visualization.
	 * The JGraph graph will be converted into a dot graph, and that dot graph will be shown.
	 */
	
	@Plugin(name = "Visualize Petri Net (Dot) [local]", level = PluginLevel.Local, returnLabels = { "Visualized Petri Net" }, returnTypes = { JComponent.class }, parameterLabels = { "Petri Net" }, userAccessible = true)
	@Visualizer
	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent runUI(UIPluginContext context, Petrinet net) {
		return runUI(context, net, new GraphVisualizerParameters());
	}

	@Plugin(name = "Visualize Petri Net (Dot)", level = PluginLevel.PeerReviewed, returnLabels = { "Visualized Petri Net" }, returnTypes = { JComponent.class }, parameterLabels = { "Petri Net" }, userAccessible = true)
	@Visualizer
	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent runUIShowLabels(UIPluginContext context, Petrinet net) {
		GraphVisualizerParameters parameters = new GraphVisualizerParameters();
		parameters.setAppendTooltipToLabel(true);
		return runUI(context, net, parameters);
	}

	@Plugin(name = "Visualize Inhibitor Net (Dot)", returnLabels = { "Visualized Inhibitor Net" }, returnTypes = { JComponent.class }, parameterLabels = { "Inhibitor Net" }, userAccessible = true)
	@Visualizer
	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent runUIShowLabels(UIPluginContext context, InhibitorNet net) {
		GraphVisualizerParameters parameters = new GraphVisualizerParameters();
		parameters.setAppendTooltipToLabel(true);
		return runUI(context, net, parameters);
	}

	@Plugin(name = "Visualize Petri Net (Dot)", returnLabels = { "Visualized Reset Net" }, returnTypes = { JComponent.class }, parameterLabels = { "Reset Net" }, userAccessible = true)
	@Visualizer
	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent runUIShowLabels(UIPluginContext context, ResetNet net) {
		GraphVisualizerParameters parameters = new GraphVisualizerParameters();
		parameters.setAppendTooltipToLabel(true);
		return runUI(context, net, parameters);
	}

	@Plugin(name = "Visualize Petri Net (Dot)", returnLabels = { "Visualized Reset/Inhibitor Net" }, returnTypes = { JComponent.class }, parameterLabels = { "Reset/Inhibitor Net" }, userAccessible = true)
	@Visualizer
	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent runUIShowLabels(UIPluginContext context, ResetInhibitorNet net) {
		GraphVisualizerParameters parameters = new GraphVisualizerParameters();
		parameters.setAppendTooltipToLabel(true);
		return runUI(context, net, parameters);
	}

	private JComponent runUI(UIPluginContext context, PetrinetGraph graph, GraphVisualizerParameters parameters) {
		if (graph instanceof Petrinet) {
			/*
			 * It's a proper Petri net. Get a hold on the view specific attributes.
			 */
			PetriNetVisualization visualizer = new PetriNetVisualization();
			ProMJGraphPanel panel = (ProMJGraphPanel) visualizer.visualize(context, (Petrinet) graph);
			ProMJGraph jGraph = panel.getGraph();
			ViewSpecificAttributeMap map = jGraph.getViewSpecificAttributes();
			Petrinet net = (Petrinet) graph;
			for (Place place : net.getPlaces()) {
				String label = place.getLabel();
				int max = 5;
				/*
				 * Avoid long place labels, as they tend to blow up up place out of proportion.
				 */
				if (label.length() > max) {
					label = label.substring(0, max - 1) + "...";
					map.putViewSpecific(place, AttributeMap.LABEL, label);
				}
			}
			/*
			 * Got it. Now create the dot panel.
			 */
			return apply(context, graph, map, parameters);
		}
		return apply(context, graph, parameters);
	}

	@Plugin(name = "Visualize Transition System (Dot)", level = PluginLevel.PeerReviewed, returnLabels = { "Visualized Transition System" }, returnTypes = { JComponent.class }, parameterLabels = { "Causal Activity Matrix" }, userAccessible = true)
	@Visualizer
	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent runUI(UIPluginContext context, TransitionSystem graph) {
		/*
		 * Get a hold on the view specific attributes.
		 */
		ProMJGraphPanel panel = (ProMJGraphPanel) (new MinedTSVisualization()).visualize(context, graph);
		ProMJGraph jGraph = panel.getGraph();
		ViewSpecificAttributeMap map = jGraph.getViewSpecificAttributes();
		/*
		 * Got it. Now create the dot panel.
		 */
		return apply(context, graph, map);
	}

	@Plugin(name = "Visualize Accepting Petri Net (Dot)", returnLabels = { "Visualized Accepting Petri Net" }, returnTypes = { JComponent.class }, parameterLabels = { "Accepting Petri Net" }, userAccessible = true)
	@Visualizer
	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent runUI(UIPluginContext context, AcceptingPetriNet graph) {
		/*
		 * Get a hold on the view specific attributes.
		 */
		ProMJGraphPanel panel = (ProMJGraphPanel) VisualizeAcceptingPetriNetPlugin.visualize(context, graph);
		ProMJGraph jGraph = panel.getGraph();
		ViewSpecificAttributeMap map = jGraph.getViewSpecificAttributes();
		/*
		 * Got it. Now create the dot panel.
		 */
		return apply(context, graph.getNet(), map);
	}
	
	@Plugin(name = "Visualize Activity Cluster Array (Dot)", returnLabels = { "Visualized Activity Cluster Array" }, returnTypes = { JComponent.class }, parameterLabels = { "Activity Cluster Array" }, userAccessible = true)
	@Visualizer
	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent runUI(UIPluginContext context, ActivityClusterArray clusters) {
		return apply(context, new ActivityClusterArrayGraph(context, clusters));
	}
	
	@Plugin(name = "Visualize Causal Activity Graph (Dot)", returnLabels = { "Visualized Causal Activity Graph" }, returnTypes = { JComponent.class }, parameterLabels = { "Causal Activity Graph" }, userAccessible = true)
	@Visualizer
	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent runUI(UIPluginContext context, CausalActivityGraph graph) {
		return apply(context, new CausalActivityGraphGraph(context, graph));
	}
	
	@Plugin(name = "Visualize Fuzzy Instance (Dot)", level = PluginLevel.PeerReviewed, returnLabels = { "Visualized Fuzzy Instance" }, returnTypes = { JComponent.class }, parameterLabels = { "Fuzzy Instance" }, userAccessible = true)
	@Visualizer
	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent runUI(UIPluginContext context, MutableFuzzyGraph graph) {
		return apply(context, graph);
	}

}
