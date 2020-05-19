/**
 * 
 */
package org.processmining.plugins.performancemeasurement;

import javax.swing.JComponent;

import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.performancemeasurement.GlobalSettingsData;

/**
 * @author arya
 * @email arya.adriansyah@gmail.com
 * @version Apr 23, 2009
 */
@Plugin(name = "Visualize Global Settings Data", returnLabels = { "Visualized Global Settings Data" }, returnTypes = { JComponent.class }, parameterLabels = { "Global Settings for Performance Measurement" }, userAccessible = false)
@Visualizer
public class GlobalSettingsDataVisualization {
	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualize(PluginContext context, GlobalSettingsData globalSettingsData) {
		GlobalSettingsDataPanel globalSettingsDataPanel = new GlobalSettingsDataPanel(globalSettingsData);
		return globalSettingsDataPanel;
	}
}
