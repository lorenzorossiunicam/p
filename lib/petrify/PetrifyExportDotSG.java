package org.processmining.plugins.petrify;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;

import org.processmining.contexts.uitopia.annotations.UIExportPlugin;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.connections.transitionsystem.TransitionSystemConnection;
import org.processmining.models.graphbased.directed.transitionsystem.StartStateSet;
import org.processmining.models.graphbased.directed.transitionsystem.State;
import org.processmining.models.graphbased.directed.transitionsystem.Transition;
import org.processmining.models.graphbased.directed.transitionsystem.TransitionSystem;

/**
 * Supports the export of a transition system to a Petrify/Genet state graph
 * file.
 * 
 * @author HVERBEEK
 * 
 */

@Plugin(name = "Petrify export (sg file)", parameterLabels = { "Transition system", "File" }, returnLabels = { "Petrify.sg" }, returnTypes = { PetrifyDotSG.class })
@UIExportPlugin(description = "Petrify sg file", extension = "sg")
public class PetrifyExportDotSG {
	/**
	 * Write the given transition to a temp file and returns a handle to this
	 * file.
	 * 
	 * @param context
	 *            The current plug-in context.
	 * @param ts
	 *            The transition system to export.
	 * @return A handle to the written file.
	 */
	@PluginVariant(variantLabel = "Use temp file", requiredParameterLabels = {0})
	public PetrifyDotSG write(PluginContext context, TransitionSystem ts) {

		try {
			/**
			 * Opens a new temp file.
			 */
			File file = File.createTempFile("petrify", "." + PetrifyDotSG.getFileExtension());
			/**
			 * Writes the transition system to the opened file.
			 */
			write(context, ts, file);
			/**
			 * Returns the handle to the written file.
			 */
			return (new PetrifyDotSG(file.getAbsolutePath()));
		} catch (Exception e) {
			/**
			 * Uh-ooh. Log the exception.
			 */
			context.log(e.toString());
		}
		return null;
	}

	/**
	 * Writes the given transition system to the given file.
	 * 
	 * @param context
	 *            The current plug-in context.
	 * @param ts
	 *            The given transition system.
	 * @param file
	 *            The given file.
	 * @throws ConnectionCannotBeObtained
	 */
	@PluginVariant(variantLabel = "Petrify export (sg file)", requiredParameterLabels = {0,1})
	public void write(PluginContext context, TransitionSystem ts, File file) throws ConnectionCannotBeObtained {
		/**
		 * First check whether some set of start states is associated with this
		 * transition system. If so, the initial marking will be derived from
		 * this set. Otherwise, all states with empty influx will be used
		 * instead.
		 */
		StartStateSet starts;
		starts = context.tryToFindOrConstructFirstObject(StartStateSet.class, TransitionSystemConnection.class,
				TransitionSystemConnection.STARTIDS, ts);

		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
			/**
			 * States will be identified by 's' followed by a unique number.
			 * This map will map any state to its identifier.
			 */
			HashMap<Object, Integer> map = new HashMap<Object, Integer>();
			/**
			 * This set will contain all transition labels.
			 */
			HashSet<String> labels = new HashSet<String>();

			/**
			 * Generate identifiers for all states.
			 */
			int id = 0;
			for (State state : ts.getNodes()) {
				map.put(state.getIdentifier(), id);
				id++;
			}
			/**
			 * Store all transition labels.
			 */
			for (Transition trans : ts.getEdges()) {
				labels.add(trans.getLabel());
			}

			/**
			 * Write the file. 1. The name of the state graph.
			 */
			bw.write(".model " + PetrifyConstants.encode(ts.getLabel()) + "\n");
			/**
			 * 2. A list of all transition labels. Perhaps ".dummy" should be
			 * ".outputs".
			 */
			bw.write(".dummy");
			for (String label : labels) {
				if (label.length() > 0) {
					bw.write(" " + PetrifyConstants.encode(label));
				} else {
					/**
					 * Use "_" for silent transitions. Perhaps some other
					 * string?
					 */
					bw.write(" _");
				}
			}
			bw.write("\n");
			/**
			 * 3. All transitions (state id, trans id, state id).
			 */
			bw.write(".state graph\n");
			for (Transition trans : ts.getEdges()) {
				bw.write("s" + map.get(trans.getSource().getIdentifier()) + " ");
				if (trans.getLabel().length() > 0) {
					bw.write(PetrifyConstants.encode(trans.getLabel()) + " ");
				} else {
					bw.write("_ ");
				}
				bw.write("s" + map.get(trans.getTarget().getIdentifier()) + "\n");
			}
			/**
			 * 4. The initial 'marking'.
			 */
			bw.write(".marking {");
			for (Object start : starts) {
				bw.write(" s" + map.get(start));
			}
			bw.write(" }\n");
			/**
			 * 5. End.
			 */
			bw.write(".end\n");

			bw.close();

		} catch (Exception e) {
			/**
			 * uh-ooh. Log the exception.
			 */
			context.log(e.toString());
		}
	}
}
