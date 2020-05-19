/**
 * 
 */
package org.processmining.plugins.aggregatedactivitiesperformancediagram;

import javax.swing.JComponent;

import org.processmining.connections.aggregatedactivitiesperformancediagram.AAPDLogReplayConnection;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.connections.ConnectionManager;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.aggregatedactivitiesperformancediagram.AAPD;
import org.processmining.models.performancemeasurement.GlobalSettingsData;

/**
 * @author arya
 * @email arya.adriansyah@gmail.com
 * @version May 13, 2009
 */
@Plugin(name = "Visualize Aggregated Activities Performance Diagram (AAPD)", returnLabels = { "Visualized AAPD" }, returnTypes = { JComponent.class }, parameterLabels = {
		"Aggregated Activities Performance Diagram", "Global Settings" }, userAccessible = false)
@Visualizer
public class AAPDVisualization {
	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualize(PluginContext context, AAPD aapd) throws Exception {
		// check connection to FPD
		ConnectionManager cm = context.getConnectionManager();
		for (AAPDLogReplayConnection aapdLogReplayConnection : cm.getConnections(AAPDLogReplayConnection.class,
				context, aapd)) {
			GlobalSettingsData globalSettingsData = aapdLogReplayConnection
					.getObjectWithRole(AAPDLogReplayConnection.GLOBALSETTINGSDATA);
			return getVisualizationPanel(context, aapd, globalSettingsData);
		}
		return null;
	}

	@PluginVariant(requiredParameterLabels = { 0, 1 })
	public JComponent visualize(PluginContext context, AAPD aapd, GlobalSettingsData globalSettingsData)
			throws Exception {
		// check connection to FPD
		ConnectionManager cm = context.getConnectionManager();
		if (cm.getConnections(AAPDLogReplayConnection.class, context, aapd, globalSettingsData) != null) {
			return getVisualizationPanel(context, aapd, globalSettingsData);
		}
		return null;
	}

	private JComponent getVisualizationPanel(PluginContext context, AAPD aapd, GlobalSettingsData globalSettingsData) {
		AAPDInformationPanel informationPanel = new AAPDInformationPanel(context, context.getProgress(), aapd,
				globalSettingsData);
		return informationPanel;
	}
}
