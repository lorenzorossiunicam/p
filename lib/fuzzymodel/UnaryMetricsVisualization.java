package org.processmining.plugins.fuzzymodel;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JComponent;

import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.fuzzymodel.metrics.MetricsRepository;
import org.processmining.models.graphbased.directed.fuzzymodel.metrics.unary.UnaryMetric;
import org.processmining.models.graphbased.directed.fuzzymodel.util.FuzzyMinerLog;
import org.processmining.plugins.fuzzymodel.miner.ui.UnaryMetricsPanel;

import com.fluxicon.slickerbox.components.RoundedPanel;

@Plugin(name = "@3 Show Fuzzy Unary Metrics", returnLabels = { "Visualization for Unary Fuzzy Metrics" }, returnTypes = { JComponent.class }, parameterLabels = { "Fuzzy Model" }, userAccessible = true)
@Visualizer
public class UnaryMetricsVisualization {

	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualize(PluginContext context, MetricsRepository repository) {
		UnaryMetricsPanel unaryMetricsPanel = new UnaryMetricsPanel(FuzzyMinerLog.getLogEvents(repository
				.getLogReader()));
		for (UnaryMetric unary : repository.getUnaryMetrics()) {
			unaryMetricsPanel.addMetric(unary);
		}
		unaryMetricsPanel.addMetric(repository.getAggregateUnaryMetric());
		RoundedPanel unaryMetricsEnclosure = new RoundedPanel(10, 5, 0);
		unaryMetricsEnclosure.setBackground(Color.black);
		unaryMetricsEnclosure.setLayout(new BorderLayout());
		unaryMetricsEnclosure.add(unaryMetricsPanel, BorderLayout.CENTER);
		return unaryMetricsEnclosure;
	}

}
