/**
 * 
 */
package org.processmining.plugins.flex.exporting;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UIExportPlugin;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.connections.ConnectionManager;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.Pair;
import org.processmining.models.connections.flexiblemodel.FlexEndTaskNodeConnection;
import org.processmining.models.connections.flexiblemodel.FlexStartTaskNodeConnection;
import org.processmining.models.flexiblemodel.EndTaskNodesSet;
import org.processmining.models.flexiblemodel.Flex;
import org.processmining.models.flexiblemodel.FlexEdge;
import org.processmining.models.flexiblemodel.FlexNode;
import org.processmining.models.flexiblemodel.SetFlex;
import org.processmining.models.flexiblemodel.StartTaskNodesSet;

/**
 * @author aadrians
 * 
 */
@Plugin(name = "Export as Flexible Model (.flex)", returnLabels = {}, returnTypes = {}, parameterLabels = { "Flexible Model", "File" }, userAccessible = true)
@UIExportPlugin(description = "Export as Flexible Model (.flex)", extension = "flex")
public class ExportFlex {
	@PluginVariant(variantLabel = "Export as Flexible Model (.flex)", requiredParameterLabels = { 0, 1 })
	public void exportFlex(UIPluginContext context, Flex net, File file) throws IOException {
		exportFlexToFlexFile(context, net, file);
	}

	private void exportFlexToFlexFile(UIPluginContext context, Flex net, File file) throws IOException {
		ConnectionManager connManager = context.getConnectionManager();

		// get all starting task nodes
		StartTaskNodesSet startTaskNodesSet = null;
		try {
			Collection<FlexStartTaskNodeConnection> conns = connManager.getConnections(
					FlexStartTaskNodeConnection.class, context, net);
			if (conns != null) {
				startTaskNodesSet = conns.iterator().next()
						.getObjectWithRole(FlexStartTaskNodeConnection.STARTTASKNODES);
			}
		} catch (ConnectionCannotBeObtained excConn) {
			// no start task nodes
		}

		// get end task nodes
		EndTaskNodesSet endTaskNodesSet = null;
		try {
			Collection<FlexEndTaskNodeConnection> conns = connManager.getConnections(FlexEndTaskNodeConnection.class,
					context, net);
			if (conns != null) {
				endTaskNodesSet = conns.iterator().next().getObjectWithRole(FlexEndTaskNodeConnection.ENDTASKNODES);
			}
		} catch (ConnectionCannotBeObtained excConn) {
			// no start task nodes
		}

		// export  to file
		String text = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>"
				+ createStringRep(net, startTaskNodesSet, endTaskNodesSet);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
		bw.write(text);
		bw.close();

	}

	private String createStringRep(Flex net, StartTaskNodesSet startTaskNodesSet, EndTaskNodesSet endTaskNodesSet) {
		StringBuilder sb = new StringBuilder();
		sb.append("<cnet>");
		sb.append("<net type=\"http://www.processmining.org\" id=\"");
		sb.append(net.getLabel());
		sb.append("\" />");
		sb.append("<name>");
		sb.append(net.getLabel());
		sb.append("</name>");

		// utility variables
		int id = 0;
		Map<FlexNode, Integer> mapFlexNodeToId = new HashMap<FlexNode, Integer>();
		Map<Pair<FlexNode, FlexNode>, Integer> mapArcToId = new HashMap<Pair<FlexNode,FlexNode>, Integer>();

		for (FlexNode node : net.getNodes()) {
			mapFlexNodeToId.put(node, id);

			sb.append("<node id=\"");
			sb.append(id);
			sb.append("\" isInvisible=\"");
			sb.append(node.isInvisible() ? "true" : "false");
			sb.append("\">");
			sb.append("<name>");
			sb.append(node.getLabel());
			sb.append("</name>");
			sb.append("</node>");

			id++;
		}

		FlexNode startNode = null;
		if (startTaskNodesSet != null) {
			SetFlex setFlex = startTaskNodesSet.iterator().next();
			sb.append("<startTaskNode id=\"");
			startNode = setFlex.iterator().next();
			sb.append(mapFlexNodeToId.get(startNode));
			sb.append("\"/>");
		}

		FlexNode endNode = null;
		if (endTaskNodesSet != null) {
			SetFlex setFlex = endTaskNodesSet.iterator().next();
			sb.append("<endTaskNode id=\"");
			endNode = setFlex.iterator().next();
			sb.append(mapFlexNodeToId.get(endNode));
			sb.append("\"/>");
		}

		// set flex 
		for (FlexNode node : net.getNodes()) {
			// input first
			if (!node.equals(startNode)) {
				sb.append("<inputNode id=\"");
				sb.append(mapFlexNodeToId.get(node));
				sb.append("\">");
				for (SetFlex setFlex : node.getInputNodes()) {
					sb.append("<inputSet>");
					for (FlexNode nodeInput : setFlex) {
						sb.append("<node id=\"");
						sb.append(mapFlexNodeToId.get(nodeInput));
						sb.append("\" />");
					}
					sb.append("</inputSet>");
				}
				sb.append("</inputNode>");
			}

			// output first
			if (!node.equals(endNode)) {
				sb.append("<outputNode id=\"");
				sb.append(mapFlexNodeToId.get(node));
				sb.append("\">");
				for (SetFlex setFlex : node.getOutputNodes()) {
					sb.append("<outputSet>");
					for (FlexNode nodeOutput : setFlex) {
						sb.append("<node id=\"");
						sb.append(mapFlexNodeToId.get(nodeOutput));
						sb.append("\" />");
					}
					sb.append("</outputSet>");
				}
				sb.append("</outputNode>");
			}
		}

		// add arcs
		for (FlexEdge<? extends FlexNode, ? extends FlexNode> edge : net.getEdges()) {
			sb.append("<arc id=\"");
			sb.append(id);
			sb.append("\" source=\"");
			sb.append(mapFlexNodeToId.get(edge.getSource()));
			sb.append("\" target=\"");
			sb.append(mapFlexNodeToId.get(edge.getTarget()));
			sb.append("\" />");
			mapArcToId.put(new Pair<FlexNode, FlexNode>(edge.getSource(), edge.getTarget()), id);
			id++;
		}

		sb.append("</cnet>");
		return sb.toString();
	}
}
