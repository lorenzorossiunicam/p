package org.processmining.plugins.epml.exporting;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.epc.ConfigurableEPC;
import org.processmining.models.graphbased.directed.epc.EPCEdge;
import org.processmining.models.graphbased.directed.epc.EPCNode;
import org.processmining.models.graphbased.directed.epc.elements.ConfigurableConnector;
import org.processmining.models.graphbased.directed.epc.elements.ConfigurableFunction;
import org.processmining.models.graphbased.directed.epc.elements.Connector;
import org.processmining.models.graphbased.directed.epc.elements.Event;
import org.processmining.models.graphbased.directed.epc.elements.Function;

//@Plugin(name = "EPNML export", returnLabels = {}, returnTypes = {}, parameterLabels = { "EPC", "File" }, userAccessible = true)
//@UIExportPlugin(description = "EPNML TEST files", extension = "epml")
public class EpmlExportPluginHelper {

	private static Map<String, Integer> mapIds = new HashMap<String, Integer>();

//	@PluginVariant(variantLabel = "EPNML export", requiredParameterLabels = { 0, 1 })
	public synchronized static void exportToEPNMLFile(PluginContext context, ConfigurableEPC epc, File file)
			throws IOException {
		Integer idGen = 0;
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
		sb.append("\n");
		sb.append("<ns2:epml xmlns:ns2=\"http://www.epml.de\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.epml.de EPML_2.0.xsd\">");
		sb.append("\n");
		sb.append("<directory>");
		sb.append("\n");
		sb.append("<coordinates xOrigin=\"leftToRight\" yOrigin=\"topToBottom\"/>");
		sb.append("\n");
		sb.append("<epc epcId=\"" + 1 + "\" name=\"\">");
		sb.append("\n");

		idGen = getFunctions(sb, idGen, epc);
		idGen = getEvents(sb, idGen, epc);
		idGen = getConnectors(sb, idGen, epc);
		idGen = getEdges(sb, idGen, epc);
		
		sb.append("</epc>");
		sb.append("</directory>");
		sb.append("\n");
		sb.append("</ns2:epml>");

        mapIds.clear();

		PrintWriter out = null;
		try {
			out = new PrintWriter(file);
			out.print(sb.toString());
		} finally {
			out.close();
		}
	}

	private static Integer getEdges(StringBuilder sb, Integer idGen, ConfigurableEPC epc) {
		for (EPCEdge<? extends EPCNode, ? extends EPCNode> edge : epc.getEdges()) {
			sb.append("<arc id=\"" + (++idGen) + "\">");
			sb.append("\n");
			sb.append("<flow source=\"" + mapIds.get(edge.getSource().getId().toString()) + "\" target=\""
					+ mapIds.get(edge.getTarget().getId().toString()) + "\"/>");
			sb.append("\n");
			sb.append("</arc>");
			sb.append("\n");
		}
		return idGen;

	}

	private static Integer getConnectors(StringBuilder sb, Integer idGen, ConfigurableEPC epc) {
		Collection<Connector> connectors = epc.getConnectors();
		String connectorType;
		for (Connector connector : connectors) {
			mapIds.put(connector.getId().toString(), ++idGen);
			connectorType = connector.getType().getLongName().toLowerCase();
			sb.append(" <" + connectorType + " id=\"" + idGen + "\">");
			sb.append("\n");
			if (connector instanceof ConfigurableConnector) {
				ConfigurableConnector cc = (ConfigurableConnector) connector;
				if (cc.isConfigurable()) {
					sb.append("<configurableConnector>");
					sb.append("\n");
					sb.append("<configuration value=\"");

					sb.append(connectorType);

					sb.append("\"/>");
					sb.append("\n");
					sb.append("</configurableConnector>");
					sb.append("\n");
				}
			}
			sb.append("</" + connectorType + ">");
			sb.append("\n");
		}

		return idGen;

	}

	private static Integer getEvents(StringBuilder sb, Integer idGen, ConfigurableEPC epc) {
		for (Event event : epc.getEvents()) {
			mapIds.put(event.getId().toString(), ++idGen);
			sb.append(" <event id=\"" + idGen + "\">");
			sb.append("\n");
			sb.append("<name>" + event.getLabel() + "</name>");
			sb.append("\n");
			sb.append("<description></description>");
			sb.append("\n");
			sb.append("</event>");
			sb.append("\n");
		}
		return idGen;
	}

	private static Integer getFunctions(StringBuilder sb, Integer idGen, ConfigurableEPC epc) {
		Collection<Function> functions = epc.getFunctions();
		for (Function function : functions) {
			mapIds.put(function.getId().toString(), ++idGen);
			sb.append(" <function id=\"" + idGen + "\">");
			sb.append("\n");
			sb.append("<name>" + function.getLabel() + "</name>");
			sb.append("\n");
			sb.append("<description></description>");
			sb.append("\n");
			if (function instanceof ConfigurableFunction) {
				ConfigurableFunction cf = (ConfigurableFunction) function;
				if (cf.isConfigurable()) {
					sb.append("<configurableFuction>");
					sb.append("\n");
					sb.append("<configuration value=\"");

					sb.append("on");

					sb.append("\"/>");
					sb.append("\n");
					sb.append("</configurableFuction>");
					sb.append("\n");
				}
			}
			sb.append("</function>");
			sb.append("\n");
		}
		return idGen;
	}

}
