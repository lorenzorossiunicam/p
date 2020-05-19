package org.processmining.plugins.fuzzymodel;

import javax.swing.JComponent;

import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.fuzzymodel.MutableFuzzyGraph;
import org.processmining.models.graphbased.directed.fuzzymodel.metrics.MetricsRepository;
import org.processmining.plugins.fuzzymodel.miner.ui.FastTransformerPanel;

/**
 * Visualization support for Fuzzy Model. *
 * 
 * @author Jiafei Li (jiafei@jlu.edu.cn)
 * 
 */
@Plugin(name = "@2 Show (Default) Fuzzy Instance", level = PluginLevel.PeerReviewed, returnLabels = { "Visualization for (Default) Fuzzy Instance" }, returnTypes = { JComponent.class }, parameterLabels = { "Fuzzy Model", "Fuzzy Instance" }, userAccessible = true)
@Visualizer
public class FuzzyModelVisualization {
	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualize(PluginContext context, MutableFuzzyGraph graph) {
		return new FastTransformerPanel(context, graph).getGraphPanel();
	}

	@PluginVariant(requiredParameterLabels = { 1 })
	public JComponent visualize(PluginContext context, MetricsRepository repository) {
		return new FastTransformerPanel(context, repository).getGraphPanel();
	}
}
