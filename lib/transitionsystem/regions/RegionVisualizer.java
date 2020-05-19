package org.processmining.plugins.transitionsystem.regions;

import java.util.Collection;

import javax.swing.JLabel;

import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.transitionsystem.regions.GeneralizedExitationRegions;
import org.processmining.models.graphbased.directed.transitionsystem.regions.Region;
import org.processmining.models.graphbased.directed.transitionsystem.regions.RegionSet;

@Visualizer
@Plugin(name = "Region Visualizer", level = PluginLevel.Local, parameterLabels = "Regions", returnTypes = JLabel.class, returnLabels = "label", userAccessible = true, help = "", mostSignificantResult = 0)
public class RegionVisualizer {
	@PluginVariant(requiredParameterLabels = { 0 }, variantLabel = "Visualize GERS")
	public JLabel visualizeGers(PluginContext context, GeneralizedExitationRegions gers) {
		return makeLabel(gers.values());
	}

	@PluginVariant(requiredParameterLabels = { 0 }, variantLabel = "Visualize Regions")
	public JLabel visualizeRegions(PluginContext context, RegionSet regions) {
		return makeLabel(regions);
	}

	private JLabel makeLabel(Collection<Region> regions) {
		String s = "<html>";
		for (Region r : regions) {
			s += r.toString() + "<br>";
		}
		s += "</html>";

		return new JLabel(s);

	}

}
