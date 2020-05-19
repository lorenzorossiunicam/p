package org.processmining.plugins.tsml.exporting;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UIExportPlugin;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.connections.ConnectionManager;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.connections.GraphLayoutConnection;
import org.processmining.models.connections.transitionsystem.TransitionSystemConnection;
import org.processmining.models.graphbased.directed.DirectedGraphElementWeights;
import org.processmining.models.graphbased.directed.transitionsystem.AcceptStateSet;
import org.processmining.models.graphbased.directed.transitionsystem.StartStateSet;
import org.processmining.models.graphbased.directed.transitionsystem.TransitionSystem;
import org.processmining.plugins.tsml.Tsml;
import org.processmining.plugins.utils.ProvidedObjectHelper;

@Plugin(name = "TSML export (Transition system)", level = PluginLevel.PeerReviewed, returnLabels = {}, returnTypes = {}, parameterLabels = {
		"Transition system", "File" }, userAccessible = true)
@UIExportPlugin(description = "TSML files", extension = "tsml")
public class TsmlExportTS {

	@PluginVariant(variantLabel = "TSML export (Transition system)", requiredParameterLabels = { 0, 1 })
	public void export(UIPluginContext context, TransitionSystem ts, File file) throws IOException {
		StartStateSet starts = new StartStateSet();
		AcceptStateSet accepts = new AcceptStateSet();
		DirectedGraphElementWeights weights = new DirectedGraphElementWeights();

		ConnectionManager cm = context.getConnectionManager();
		try {
			TransitionSystemConnection tsc = cm.getFirstConnection(TransitionSystemConnection.class, context, ts);
			if (tsc.hasWeights()) {
				weights = tsc.getObjectWithRole(TransitionSystemConnection.WEIGHTS);
			}
			starts = tsc.getObjectWithRole(TransitionSystemConnection.STARTIDS);
			accepts = tsc.getObjectWithRole(TransitionSystemConnection.ACCEPTIDS);

		} catch (ConnectionCannotBeObtained e) {
			/**
			 * No connected transition weights found, no problem.
			 */
		}
		GraphLayoutConnection layout;
		try {
			layout = context.getConnectionManager().getFirstConnection(GraphLayoutConnection.class, context, ts);
		} catch (ConnectionCannotBeObtained e) {
			layout = new GraphLayoutConnection(ts);
		}

		Tsml tsml = new Tsml().marshall(ts, starts, accepts, weights, layout);
		updateName(context, tsml, ts);
		String text = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" + tsml.exportElement(tsml);

		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
		bw.write(text);
		bw.close();
	}

	private static void updateName(PluginContext context, Tsml tsml, TransitionSystem ts) {
		tsml.setName(ProvidedObjectHelper.getProvidedObjectLabel(context, ts));
	}
}
