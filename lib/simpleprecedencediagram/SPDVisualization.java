/**
 * 
 */
package org.processmining.plugins.simpleprecedencediagram;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.connections.simpleprecedencediagram.LogSPDConnection;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.connections.ConnectionManager;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.connections.AbstractLogModelConnection;
import org.processmining.models.simpleprecedencediagram.SPD;
import org.processmining.models.simpleprecedencediagram.SPDNode;

/**
 * @author arya
 * @email arya.adriansyah@gmail.com
 * @version Mar 16, 2009
 */
@Plugin(name = "Visualize Simple Precedence Diagram (SPD)", returnLabels = { "Visualized SPD" }, returnTypes = { JComponent.class }, parameterLabels = {
		"Simple Precedence Diagram", "Log File" }, userAccessible = false)
@Visualizer
public class SPDVisualization {
	/**
	 * Variant of having only log
	 * 
	 * @param context
	 * @param graph
	 * @return
	 */
	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualize(PluginContext context, SPD graph) {
		// find a log 
		try {
			ConnectionManager cm = context.getConnectionManager();
			LogSPDConnection conn = cm.getFirstConnection(LogSPDConnection.class, context, graph);

			// connection found. Create all necessary component to instantiate inactive visualization panel
			XLog log = conn.getObjectWithRole(AbstractLogModelConnection.LOG);
			XEventClasses classes = conn.getObjectWithRole(AbstractLogModelConnection.CLASSES);

			Map<SPDNode, Set<XEventClass>> relations = new HashMap<SPDNode, Set<XEventClass>>();
			for (SPDNode node : graph.getNodes()) {
				relations.put(node, conn.getActivitiesFor(node));
			}

			return getVisualizationPanel(graph, log, classes, relations, context, false);
		} catch (Exception exc) {
			// there is no connected log
			context.log("Connection between " + graph.getLabel()
					+ " and a log is not found. Please select a log together with the SPD");
			return getErrorMessagePanel("Connection between " + graph.getLabel()
					+ " and a log is not found. Please select a log together with the SPD");
		}
	}

	/**
	 * Variant of having log and SPD
	 * 
	 * @param context
	 * @param graph
	 * @param log
	 * @return
	 */
	@PluginVariant(requiredParameterLabels = { 0, 1 })
	public JComponent visualize(PluginContext context, SPD graph, XLog log) {
		// check connection between SPD and log
		try {
			ConnectionManager cm = context.getConnectionManager();
			LogSPDConnection conn = cm.getFirstConnection(LogSPDConnection.class, context, graph, log);

			// connection found. Create all necessary component to instantiate inactive visualization panel
			XEventClasses classes = conn.getObjectWithRole(AbstractLogModelConnection.CLASSES);

			Map<SPDNode, Set<XEventClass>> relations = new HashMap<SPDNode, Set<XEventClass>>();
			for (SPDNode node : graph.getNodes()) {
				relations.put(node, conn.getActivitiesFor(node));
			}

			return getVisualizationPanel(graph, log, classes, relations, context, false);
		} catch (Exception exc) {
			// if there is no connection, an active visualization panel is shown

			// extract all event classes in the log
			XEventNameClassifier classifier = new XEventNameClassifier();
			XLogInfo summary = XLogInfoFactory.createLogInfo(log, classifier);
			XEventClasses classes = summary.getEventClasses();

			Map<SPDNode, Set<XEventClass>> relations = new HashMap<SPDNode, Set<XEventClass>>(graph.getNodes().size());
			for (SPDNode node : graph.getNodes()) {
				Set<XEventClass> set = new HashSet<XEventClass>();
				relations.put(node, set);
			}

			return getVisualizationPanel(graph, log, classes, relations, context, true);
		}
	}

	/**
	 * This method create main GUI to map EventClasses to SPD node / just to
	 * show mapping between EventClasses and SPD node
	 * 
	 * @param graph
	 * @param log
	 * @param classes
	 * @param relations
	 * @param context
	 * @param isEditable
	 * @return
	 */
	private JComponent getVisualizationPanel(SPD graph, XLog log, XEventClasses classes,
			Map<SPDNode, Set<XEventClass>> relations, PluginContext context, boolean isEditable) {
		SPDEditorPanel newPanel = new SPDEditorPanel(graph, log, classes, relations, context, isEditable);
		return newPanel;
	}

	private JComponent getErrorMessagePanel(String message) {
		JLabel errorMessage = new JLabel();
		errorMessage.setText(message);

		JPanel panel = new JPanel();
		panel.add(errorMessage);

		return panel;
	}
}
