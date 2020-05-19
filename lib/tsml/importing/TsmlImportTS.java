package org.processmining.plugins.tsml.importing;

import java.io.InputStream;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.processmining.contexts.uitopia.annotations.UIImportPlugin;
import org.processmining.framework.abstractplugins.AbstractImportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.models.graphbased.directed.DirectedGraphElementWeights;
import org.processmining.models.graphbased.directed.transitionsystem.AcceptStateSet;
import org.processmining.models.graphbased.directed.transitionsystem.StartStateSet;
import org.processmining.models.graphbased.directed.transitionsystem.TransitionSystem;
import org.processmining.models.graphbased.directed.transitionsystem.TransitionSystemFactory;
import org.processmining.plugins.tsml.Tsml;

@Plugin(name = "Import Transition system from TSML file", level = PluginLevel.PeerReviewed, parameterLabels = { "Filename" }, returnLabels = {
		"Transition system", "Transition weights", "Start state set", "Accept state set" }, returnTypes = {
		TransitionSystem.class, DirectedGraphElementWeights.class, StartStateSet.class, AcceptStateSet.class })
@UIImportPlugin(description = "TSML Transition system files", extensions = { "tsml" })
public class TsmlImportTS extends AbstractImportPlugin {

	protected FileFilter getFileFilter() {
		return new FileNameExtensionFilter("TSML files", "tsml");
	}

	protected Object importFromStream(PluginContext context, InputStream input, String filename, long fileSizeInBytes)
			throws Exception {
		TsmlImportUtils utils = new TsmlImportUtils();
		Tsml tsml = utils.importTsmlFromStream(context, input, filename, fileSizeInBytes);
		if (tsml == null) {
			/*
			 * No TSML found in file. Fail.
			 */
			return null;
		}
		/*
		 * TSML file has been imported. Now we need to convert the contents to a
		 * regular Petri net.
		 */
		TransitionSystem ts = TransitionSystemFactory.newTransitionSystem(tsml.getLabel() + " (imported from "
				+ filename + ")");

		return utils.connectTS(context, tsml, ts);
	}
}