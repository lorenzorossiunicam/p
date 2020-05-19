package org.processmining.plugins.tsanalyzer;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.transitionsystem.payload.event.EventPayloadTransitionSystem;

@Plugin(name = "Analyze Transition System", parameterLabels = { "Transition System with Event Payload", "Log" }, returnLabels = { "Time Annotated Transition System" }, returnTypes = { AnnotatedTransitionSystem.class }, userAccessible = true)
public class TSAnalyzerPlugin {

	/**
	 * Shows a dialog where the user can select which conversions to use, then
	 * does the conversions.
	 * 
	 * @param context
	 *            The current plug-in context.
	 * @param ts
	 *            The transition system to convert.
	 * @return The converted transition system.
	 */
//	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "M. Pesic", email = "m.pesic@tue.nl", uiLabel = UITopiaVariant.USEPLUGIN)
//	@PluginVariant(variantLabel = "Select conversions to use", requiredParameterLabels = { 0, 1 })
//	public static AnnotatedTransitionSystem simple(final UIPluginContext context, final EventPayloadTransitionSystem ts,
//			final XLog log) {
//		return simple(context, ts, log);
//	}

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
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "M. Pesic", email = "m.pesic@tue.nl", uiLabel = UITopiaVariant.USEPLUGIN)
	@PluginVariant(variantLabel = "Use default conversions", requiredParameterLabels = { 0, 1 })
	public static AnnotatedTransitionSystem simple(final PluginContext context, final EventPayloadTransitionSystem ts,
			final XLog log) {
		
		TSAnalyzer analyzer = new TSAnalyzer(context, ts, log);
		AnnotatedTransitionSystem annotation = analyzer.annotate();
		return annotation;
	}
}