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

@Plugin(name = "@1 Show Fuzzy Model", level = PluginLevel.PeerReviewed, returnLabels = { "Visualization for Fuzzy Model" }, returnTypes = { JComponent.class }, parameterLabels = { "Fuzzy Model", "Fuzzy Instance" }, userAccessible = true)
@Visualizer
public class FastTransformerVisualization {

	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualize(PluginContext context, MetricsRepository repository) {
		return new FastTransformerPanel(context, repository);
	}

//	@PluginVariant(requiredParameterLabels = { 1 })
	public JComponent visualize(PluginContext context, MutableFuzzyGraph mfg) {
		return new FastTransformerPanel(context, mfg);
	}

}
