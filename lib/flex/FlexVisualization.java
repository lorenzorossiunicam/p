package org.processmining.plugins.flex;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JComponent;

import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.Pair;
import org.processmining.models.connections.flexiblemodel.FlexCancellationRegionConnection;
import org.processmining.models.connections.flexiblemodel.FlexEndTaskNodeConnection;
import org.processmining.models.connections.flexiblemodel.FlexStartTaskNodeConnection;
import org.processmining.models.flexiblemodel.CancellationRegion;
import org.processmining.models.flexiblemodel.EndTaskNodesSet;
import org.processmining.models.flexiblemodel.Flex;
import org.processmining.models.flexiblemodel.FlexNode;
import org.processmining.models.flexiblemodel.SetFlex;
import org.processmining.models.flexiblemodel.StartTaskNodesSet;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.ViewSpecificAttributeMap;
import org.processmining.models.jgraph.ProMJGraphVisualizer;

/**
 * @author arya
 * @email arya.adriansyah@gmail.com
 * @version Nov 19, 2009
 */
@Plugin(name = "Visualize Flexible model", returnLabels = { "Visualized Flexible model" }, returnTypes = { JComponent.class }, parameterLabels = {
		"Flexible model", "Start Task Node", "End Task Node" }, userAccessible = false)
@Visualizer
public class FlexVisualization {
	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualize(PluginContext context, Flex graph) {
		System.gc();

		// check the connection between graph and start Task node
		StartTaskNodesSet startTaskNode = null;
		EndTaskNodesSet endTaskNode = null;
		CancellationRegion cancellationRegion = null;
		try {
			FlexStartTaskNodeConnection conn = context.getConnectionManager().getFirstConnection(
					FlexStartTaskNodeConnection.class, context, graph);
			startTaskNode = (StartTaskNodesSet) conn.getObjectWithRole(FlexStartTaskNodeConnection.STARTTASKNODES);
		} catch (ConnectionCannotBeObtained e) {
			//no start task node
		}

		try {
			FlexEndTaskNodeConnection conn = context.getConnectionManager().getFirstConnection(
					FlexEndTaskNodeConnection.class, context, graph);
			endTaskNode = (EndTaskNodesSet) conn.getObjectWithRole(FlexEndTaskNodeConnection.ENDTASKNODES);
		} catch (ConnectionCannotBeObtained e) {
			// no end task node
		}

		try {
			FlexCancellationRegionConnection conn = context.getConnectionManager().getFirstConnection(
					FlexCancellationRegionConnection.class, context, graph);
			cancellationRegion = (CancellationRegion) conn
					.getObjectWithRole(FlexCancellationRegionConnection.CANCELLATIONREGION);
		} catch (ConnectionCannotBeObtained e) {
			// no cancellation region
		}

		return getVisualizationPanel(context, graph, startTaskNode, endTaskNode, cancellationRegion);
	}

	private JComponent getVisualizationPanel(PluginContext context, Flex graph, StartTaskNodesSet startTaskNodes,
			EndTaskNodesSet endTaskNodes, CancellationRegion cancellationRegion) {
		ViewSpecificAttributeMap viewSpecificMap = new ViewSpecificAttributeMap();

		Set<FlexNode> possibleStartTaskNodes = new HashSet<FlexNode>();
		if (startTaskNodes != null) {
			for (SetFlex setFlex : startTaskNodes) {
				for (FlexNode node : setFlex) {
					possibleStartTaskNodes.add(node);
				}
			}
		}

		Set<FlexNode> possibleEndTaskNodes = new HashSet<FlexNode>();
		if (endTaskNodes != null) {
			for (SetFlex setFlex : endTaskNodes) {
				for (FlexNode node : setFlex) {
					possibleEndTaskNodes.add(node);
				}
			}
		}

		if (cancellationRegion != null) {
			for (FlexNode node : cancellationRegion.keySet()) {
				String cancelString = node.getToolTipText()
						+ "<tr><td colspan=\"2\"><strong>Cancelation Set:</strong></td></tr><tr><td colspan=\"2\">";
				String limiter = "";
				for (Pair<FlexNode, FlexNode> pair : cancellationRegion.get(node)) {
					cancelString += limiter;
					cancelString += pair.getFirst().getLabel();
					cancelString += "->";
					cancelString += pair.getSecond().getLabel();
					limiter = ",";
				}
				Set<FlexNode> canceledNodes = cancellationRegion.getNodeCancellationFor(node);
				if (canceledNodes != null){
					if (limiter.equals(",")){
						cancelString += limiter;
					}
					limiter = "";
					for (FlexNode canceledNode: canceledNodes){
						cancelString += limiter;
						cancelString += canceledNode.getLabel();
						limiter = ",";
					}
				}
				cancelString += "</td></tr>";
				viewSpecificMap.putViewSpecific(node, AttributeMap.TOOLTIP, cancelString);
			}
		}

		// change the shape of start/end task node
		for (FlexNode node : graph.getNodes()) {
			if (possibleStartTaskNodes.contains(node)) {
				viewSpecificMap.putViewSpecific(node, AttributeMap.STROKECOLOR, Color.GREEN);
				viewSpecificMap.putViewSpecific(node, AttributeMap.BORDERWIDTH, 3);
			}

			if (possibleEndTaskNodes.contains(node)) {
				viewSpecificMap.putViewSpecific(node, AttributeMap.STROKECOLOR, Color.RED);
				viewSpecificMap.putViewSpecific(node, AttributeMap.BORDERWIDTH, 3);
			}
		}
		;

		return ProMJGraphVisualizer.instance().visualizeGraph(context, graph, viewSpecificMap);
	}
}
