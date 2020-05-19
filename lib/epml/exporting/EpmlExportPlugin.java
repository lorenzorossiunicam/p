package org.processmining.plugins.epml.exporting;

import java.io.File;
import java.io.PrintWriter;

import org.processmining.contexts.uitopia.annotations.UIExportPlugin;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.epc.ConfigurableEPC;
import org.processmining.plugins.epml.Epml;

@Plugin(name = "EPNML export", returnLabels = {}, returnTypes = {}, parameterLabels = { "EPC", "File" }, userAccessible = true)
@UIExportPlugin(description = "EPNML files", extension = "epml")
public class EpmlExportPlugin {

	@PluginVariant(variantLabel = "EPNML export", requiredParameterLabels = { 0, 1 })
	public synchronized void exportToEPNMLFile(PluginContext context, ConfigurableEPC epc, File file) throws Exception {
		Epml toExport = null;
		PrintWriter out = null;
		try {
			for (EPCEpmlConnection c : context.getConnectionManager().getConnections(EPCEpmlConnection.class, context,
					epc)) {
				if (c.getObjectWithRole(EPCEpmlConnection.EPC_GRAPH) == epc) {
					toExport = c.getObjectWithRole(EPCEpmlConnection.EPML);
					break;
				}
			}
		} catch (ConnectionCannotBeObtained e) {
			toExport = null;
		}
		if (toExport == null) {
			//            throw new NoSuchElementException("No EPML found for " + epc.getLabel());
			EpmlExportPluginHelper.exportToEPNMLFile(context, epc, file);
			return;
		}

		try {
			out = new PrintWriter(file);
			out.print(toExport.exportElement());
		} finally {
			out.close();
		}
	}

}
