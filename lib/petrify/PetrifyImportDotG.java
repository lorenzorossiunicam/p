package org.processmining.plugins.petrify;

import java.io.InputStream;

import org.processmining.contexts.uitopia.annotations.UIImportPlugin;
import org.processmining.framework.abstractplugins.AbstractImportPlugin;
import org.processmining.framework.connections.Connection;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.connections.petrinets.behavioral.InitialMarkingConnection;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.semantics.petrinet.Marking;

/**
 * Supports the import of a Petri net from a Petrify/Genet Petri net file.
 * 
 * @author HVERBEEK
 * 
 */
@Plugin(name = "Open Petrify .g file", parameterLabels = { "Petrify.g" }, returnLabels = { "Petrinet", "Marking" }, returnTypes = {
		Petrinet.class, Marking.class })
@UIImportPlugin(description = "Petrify petrinet files", extensions = { "g", "sg" })
public class PetrifyImportDotG extends AbstractImportPlugin {

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
	public Object[] importFile(PluginContext context, PetrifyDotG dotG) throws Exception {
		return importFromStream(context, dotG.getInputStream(), dotG.getName(), dotG.getLength());
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
		/**
		 * Creates a new Petri net and a new marking.
		 */
		Petrinet petrinet = PetrinetFactory.newPetrinet(filename);
		Marking marking = new Marking();
		/**
		 * Sets the labels of both results.
		 */
		context.getFutureResult(0).setLabel(filename);
		context.getFutureResult(1).setLabel("Initial Marking of " + filename);
		/**
		 * Creates a new file reader, and reads the file.
		 */
		PetrifyGReader reader = new PetrifyGReader();
		reader.read(input, petrinet, marking);
		/**
		 * Creates a new connection between the read Petri net and the read
		 * marking, and add it.
		 */
		Connection c = new InitialMarkingConnection(petrinet, marking);
		context.addConnection(c);
		/**
		 * Returns both the Petri net and the marking.
		 */
		return new Object[] { petrinet, marking };
	}

}
