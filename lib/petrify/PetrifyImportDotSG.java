package org.processmining.plugins.petrify;

import java.io.InputStream;

import org.processmining.contexts.uitopia.annotations.UIImportPlugin;
import org.processmining.framework.abstractplugins.AbstractImportPlugin;
import org.processmining.framework.connections.Connection;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.connections.transitionsystem.TransitionSystemConnection;
import org.processmining.models.graphbased.directed.transitionsystem.AcceptStateSet;
import org.processmining.models.graphbased.directed.transitionsystem.StartStateSet;
import org.processmining.models.graphbased.directed.transitionsystem.State;
import org.processmining.models.graphbased.directed.transitionsystem.TransitionSystem;
import org.processmining.models.graphbased.directed.transitionsystem.TransitionSystemFactory;

/**
 * Supports the import of a Petri net from a Petrify/Genet Petri net file.
 * 
 * @author HVERBEEK
 * 
 */
@Plugin(name = "Open Petrify .sg file", parameterLabels = { "Petrify.sg" }, returnLabels = { "Transition System",
		"Initial States", "Final States" }, returnTypes = { TransitionSystem.class, StartStateSet.class,
		AcceptStateSet.class })
@UIImportPlugin(description = "Petrify state graph files", extensions = { "g", "sg" })
public class PetrifyImportDotSG extends AbstractImportPlugin {

	/**
	 * Imports a Petri net from a Petrify/Genet Petri net file.
	 * 
	 * @param context
	 *            The current plug-in context.
	 * @param dotG
	 *            The handle to the given file.
	 * @return The Petri net and the marking as imported from the file.
	 * @throws Exception
	 */
	@PluginVariant(variantLabel = "Known file", requiredParameterLabels = { 0 })
	public Object[] importFile(PluginContext context, PetrifyDotSG dotSG) throws Exception {
		return importFromStream(context, dotSG.getInputStream(), dotSG.getName(), dotSG.getLength());
	}

	/**
	 * Imports a Petri net from a Petrify/Genet Petri net stream, given the
	 * associated file name and size.
	 * 
	 * @param context
	 *            The current plug-in context.
	 * @param input
	 *            The given stream.
	 * @param filename
	 *            The name of the file associated with the stream.
	 * @param fileSizeInBytes
	 *            The size of the file associated with the stream.
	 * @return The Petri net and the marking as imported from the stream.
	 * @throws Exception
	 */
	@Override
	protected Object[] importFromStream(PluginContext context, InputStream input, String filename, long fileSizeInBytes)
			throws Exception {
		TransitionSystem ts = TransitionSystemFactory.newTransitionSystem(filename);
		StartStateSet starts = new StartStateSet();
		AcceptStateSet accept = new AcceptStateSet();

		/**
		 * Sets the labels of both results.
		 */
		context.getFutureResult(0).setLabel(filename);
		context.getFutureResult(1).setLabel("Initial States of " + filename);
		context.getFutureResult(2).setLabel("Final States of " + filename);
		/**
		 * Gets an input stream for the given file, creates a new file reader,
		 * and reads the file.
		 */
		PetrifySGReader reader = new PetrifySGReader();
		reader.read(input, ts, starts);

		for (State s : ts.getNodes()) {
			if (ts.getOutEdges(s).isEmpty()) {
				accept.add(s);
			}
		}

		/**
		 * Creates a new connection between the read Petri net and the read
		 * marking, and add it.
		 */
		Connection c = new TransitionSystemConnection(ts, starts, accept);
		context.addConnection(c);
		/**
		 * Returns both the Petri net and the marking.
		 */
		return new Object[] { ts, starts, accept };

	}

}
