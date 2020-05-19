package org.processmining.plugins.tsanimation;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.transitionsystem.payload.event.EventPayloadTransitionSystem;

@Plugin(name = "Animate Transition System", level = PluginLevel.PeerReviewed, parameterLabels = {
		"Transition System with Event Payload", "Log" }, returnLabels = { "Transition System Animation" }, returnTypes = { TSAnimation.class }, userAccessible = true, help = TSAnimationHelp.TEXT)
public class TSAnimationPlugin {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H. Verbeek", email = "h.m.w.verbeek@tue.nl", uiLabel = UITopiaVariant.USEPLUGIN, pack = "TransitionSystems")
	@PluginVariant(variantLabel = "Select conversions to use", requiredParameterLabels = {
			0, 1 })
	public TSAnimation options(final PluginContext context,
			final EventPayloadTransitionSystem ts, final XLog log) {
		TSAnimation animation = new TSAnimation(context, ts, log);
		animation.initialize(context, ts, log);
		return animation;
	}

}
