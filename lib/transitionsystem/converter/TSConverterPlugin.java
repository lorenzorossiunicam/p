package org.processmining.plugins.transitionsystem.converter;

import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.connections.ConnectionManager;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.connections.transitionsystem.TransitionSystemConnection;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.directed.DirectedGraphElementWeights;
import org.processmining.models.graphbased.directed.transitionsystem.AcceptStateSet;
import org.processmining.models.graphbased.directed.transitionsystem.StartStateSet;
import org.processmining.models.graphbased.directed.transitionsystem.payload.PayloadTransitionSystem;
import org.processmining.plugins.transitionsystem.miner.TSMinerTransitionSystem;

@Plugin(name = "Mollify Transition System", parameterLabels = { "Transition System" }, returnLabels = {
		"Transition System", "Weights", "Start States", "Accept States" }, returnTypes = {
		PayloadTransitionSystem.class, DirectedGraphElementWeights.class, StartStateSet.class, AcceptStateSet.class }, userAccessible = true, help = TSConverterHelp.TEXT)
public class TSConverterPlugin {
	/**
	 * Converts the given transition system using the default conversion
	 * settings.
	 * 
	 * @param context
	 *            The current plug-in context.
	 * @param ts
	 *            The transition system to convert.
	 * @return The converted transition system.
	 */
	@PluginVariant(variantLabel = "Use default conversions", requiredParameterLabels = { 0 })
	public static Object[] main(final PluginContext context, final TSMinerTransitionSystem ts) {
		/**
		 * First try to find all connected objects: 1. Weights 2. Start states
		 * 3. Accept states These object will be taken into account when doing
		 * the conversions. If needed, new versions will be computed.
		 */
		DirectedGraphElementWeights weights = new DirectedGraphElementWeights();
		StartStateSet starts = new StartStateSet();
		AcceptStateSet accepts = new AcceptStateSet();

		ConnectionManager cm = context.getConnectionManager();
		try {
			for (TransitionSystemConnection tsc : cm.getConnections(TransitionSystemConnection.class, context, ts)) {
				weights = tsc.getObjectWithRole(TransitionSystemConnection.WEIGHTS);
				starts = tsc.getObjectWithRole(TransitionSystemConnection.STARTIDS);
				accepts = tsc.getObjectWithRole(TransitionSystemConnection.ACCEPTIDS);
			}
		} catch (ConnectionCannotBeObtained e) {
			/**
			 * Use default weights etc.
			 */
		}
		/**
		 * Second, construct the generic input.
		 */
		TSConverter converter = new TSConverter(context);
		TSConverterInput input = new TSConverterInput();
		input.setTransitionSystem(ts);
		input.setWeights(weights);
		input.setStarts(starts);
		input.setAccepts(accepts);
		setLabels(context, ts);
		/**
		 * Third, do the actual conversion.
		 */
		TSConverterOutput result = converter.convert(input);
		result.getTransitionSystem().getAttributeMap().put(AttributeMap.LABEL, context.getFutureResult(0).getLabel());
		return new Object[] { result.getTransitionSystem(), result.getWeights(), result.getStarts(),
				result.getAccepts() };
	}

	/**
	 * Sets the labels for the pending future results.
	 * 
	 * @param context
	 *            The current plug-in context.
	 * @param ts
	 *            The transition system to convert.
	 */
	public static void setLabels(PluginContext context, TSMinerTransitionSystem ts) {
		context.getFutureResult(0).setLabel("TS (converted from " + ts.getLabel() + ")");
		context.getFutureResult(1).setLabel("TS weights (converted from " + ts.getLabel() + ")");
		context.getFutureResult(2).setLabel("Start states (converted from " + ts.getLabel() + ")");
		context.getFutureResult(3).setLabel("Accept states (converted from " + ts.getLabel() + ")");
	}
}
