/**
 * 
 */
package org.processmining.plugins.fuzzyperformancediagram;

import javax.swing.JComponent;

import org.processmining.connections.fuzzyperformancediagram.FPDLogReplayConnection;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.connections.ConnectionManager;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.plugin.events.Logger.MessageLevel;
import org.processmining.models.fuzzyperformancediagram.FPD;
import org.processmining.models.performancemeasurement.GlobalSettingsData;
import org.processmining.models.performancemeasurement.dataelements.CaseKPIData;
import org.processmining.models.performancemeasurement.dataelements.FPDElementPerformanceMeasurementData;
import org.processmining.models.performancemeasurement.dataelements.TwoFPDNodesPerformanceData;

/**
 * @author arya
 * @email arya.adriansyah@gmail.com
 * @version Mar 19, 2009
 */
@Plugin(name = "Visualize Fuzzy Performance Diagram (FPD)", returnLabels = { "Visualized FPD" }, returnTypes = { JComponent.class }, parameterLabels = {
		"Fuzzy Performance Diagram", "Case KPI Data", "Global Settings", "Element Performance Measurement Data",
		"Accumulated Activity Instances" }, userAccessible = false)
@Visualizer
public class FPDVisualization {
	@PluginVariant(requiredParameterLabels = { 0, 1, 2, 3, 4 })
	public JComponent visualize(PluginContext context, FPD graph, CaseKPIData caseKPIData,
			GlobalSettingsData globalSettingsData,
			FPDElementPerformanceMeasurementData elementPerformanceMeasurementData,
			TwoFPDNodesPerformanceData twoNodesPerformanceData) {
		// check connection between all of these elements
		ConnectionManager cm = context.getConnectionManager();

		try {
			if (cm.getConnections(FPDLogReplayConnection.class, context, graph, caseKPIData, globalSettingsData,
					elementPerformanceMeasurementData, twoNodesPerformanceData) != null) {
				return getVisualizationPanel(context, graph, caseKPIData, globalSettingsData,
						elementPerformanceMeasurementData, twoNodesPerformanceData);
			}
			return null;
		} catch (ConnectionCannotBeObtained e) {
			// No connections available
			context.log("Connection is not exist", MessageLevel.DEBUG);
			return null;
		}
	}

	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualize(PluginContext context, FPD graph) {
		ConnectionManager cm = context.getConnectionManager();

		// Call the garbage collector. This makes sure that no
		// connections are shown that should have been removed
		System.gc();

		try {
			for (FPDLogReplayConnection globConnection : cm
					.getConnections(FPDLogReplayConnection.class, context, graph)) {
				// collect all connected objects
				GlobalSettingsData globalSettingsData = globConnection
						.getObjectWithRole(FPDLogReplayConnection.GLOBALSETTINGSDATA);
				CaseKPIData caseKPIData = globConnection.getObjectWithRole(FPDLogReplayConnection.CASEKPIDATA);
				FPDElementPerformanceMeasurementData elementPerformanceMeasurementData = globConnection
						.getObjectWithRole(FPDLogReplayConnection.ELEMENTPERFORMANCEMEASUREMENTDATA);
				TwoFPDNodesPerformanceData twoNodesPerformanceData = globConnection
						.getObjectWithRole(FPDLogReplayConnection.TWONODESPERFORMANCEDATA);

				return getVisualizationPanel(context, graph, caseKPIData, globalSettingsData,
						elementPerformanceMeasurementData, twoNodesPerformanceData);
			}
			return null;
		} catch (ConnectionCannotBeObtained e) {
			// No connections available
			context.log("Connection is not exist", MessageLevel.DEBUG);
			return null;
		}
	}

	/**
	 * This method only invoked after all connection checking is performed
	 * 
	 * @param context
	 * @param graph
	 * @param caseKPIData
	 * @param globalSettingsData
	 * @param elementPerformanceMeasurementData
	 * @param accumulatedActivityInstances
	 * @return
	 */
	private JComponent getVisualizationPanel(PluginContext context, FPD graph, CaseKPIData caseKPIData,
			GlobalSettingsData globalSettingsData,
			FPDElementPerformanceMeasurementData elementPerformanceMeasurementData,
			TwoFPDNodesPerformanceData twoNodesPerformanceData) {
		FPDInformationPanel fpdPanel = new FPDInformationPanel(context, graph, context.getProgress(), globalSettingsData);

		// set them to panel
		fpdPanel.setCaseKPIData(caseKPIData);
		fpdPanel.setElementPerformanceMeasurementData(elementPerformanceMeasurementData);
		fpdPanel.setTwoNodesPerformanceData(twoNodesPerformanceData);

		// generate all panels
		fpdPanel.generateAllPanel();

		// return result
		return fpdPanel;
	}

}
