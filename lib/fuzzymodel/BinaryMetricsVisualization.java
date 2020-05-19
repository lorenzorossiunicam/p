package org.processmining.plugins.fuzzymodel;

import javax.swing.JComponent;

import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.fuzzymodel.metrics.MetricsRepository;
import org.processmining.plugins.fuzzymodel.miner.ui.BinaryMetricsComparisonPanel;

@Plugin(name = "@4 Show Fuzzy Binary Metrics", returnLabels = { "Visualization for Fuzzy Binary Metrics" }, returnTypes = { JComponent.class }, parameterLabels = { "Fuzzy Model" }, userAccessible = true)
@Visualizer
public class BinaryMetricsVisualization {

	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualize(PluginContext context, MetricsRepository repository) {
		return new BinaryMetricsComparisonPanel(repository);
	}
}
