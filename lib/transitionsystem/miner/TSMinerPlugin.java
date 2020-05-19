package org.processmining.plugins.transitionsystem.miner;

import java.util.Arrays;

import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.classification.XEventResourceClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.directed.DirectedGraphElementWeights;
import org.processmining.models.graphbased.directed.transitionsystem.AcceptStateSet;
import org.processmining.models.graphbased.directed.transitionsystem.StartStateSet;
import org.processmining.plugins.transitionsystem.miner.ui.TSMinerUI;

@Plugin(name = "Mine Transition System", level = PluginLevel.PeerReviewed, categories = { PluginCategory.Discovery }, parameterLabels = { "Log", "Event Classifiers", "Transition Classifier",
		"Settings" }, returnLabels = { "Mined Transition System", "Weights", "Start states", "Accept states" }, returnTypes = {
		TSMinerTransitionSystem.class, DirectedGraphElementWeights.class, StartStateSet.class, AcceptStateSet.class }, userAccessible = true, help = TSMinerHelp.TEXT)
public class TSMinerPlugin {
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl", pack = "TransitionSystems")
	@PluginVariant(variantLabel = "Mine Transition System using Wizard and Default Classifiers", requiredParameterLabels = { 0 })
	public static Object[] main(final UIPluginContext context, final XLog log) {
		/**
		 * No classifiers provided. use default classifiers.
		 */
		XEventClassifier[] classifiers;
		XEventClassifier transitionClassifier;
		if (log.getClassifiers().size() > 0) {
			classifiers = new XEventClassifier[log.getClassifiers().size()];
			int i = 0;
			for (XEventClassifier classifier : log.getClassifiers()) {
				classifiers[i++] = classifier;
			}
			transitionClassifier = classifiers[0];
		} else {
			classifiers = new XEventClassifier[3];
			classifiers[0] = new XEventNameClassifier();
			classifiers[1] = new XEventResourceClassifier();
			classifiers[2] = new XEventLifeTransClassifier();
			transitionClassifier = new XEventAndClassifier(new XEventNameClassifier(), new XEventLifeTransClassifier());

		}
		return main(context, log, classifiers, transitionClassifier);
	}

	@PluginVariant(variantLabel = "Mine Transition System using Wizard", requiredParameterLabels = { 0, 1, 2 })
	public static Object[] main(final UIPluginContext context, final XLog log, final XEventClassifier[] classifiers,
			final XEventClassifier transitionClassifier) {
		TSMinerUI miner = new TSMinerUI(context);
		return miner.mine(log, Arrays.asList(classifiers), transitionClassifier);
	}

	/**
	 * The default transition system mining plug-in. Mines the given log for a
	 * transition system using default settings.
	 * 
	 * @param context
	 *            The GUI context.
	 * @param log
	 *            The given log.
	 * @return The mining result.
	 */
	@PluginVariant(variantLabel = "Mine Transition System using Default Classifiers", requiredParameterLabels = { 0 })
	public static Object[] main(final PluginContext context, final XLog log) {
		/**
		 * No classifiers provided, use default classifiers.
		 */
		XEventClassifier[] classifiers = new XEventClassifier[3];
		classifiers[0] = new XEventNameClassifier();
		classifiers[1] = new XEventResourceClassifier();
		classifiers[2] = new XEventLifeTransClassifier();
		XEventClassifier transitionClassifier = new XEventAndClassifier(new XEventNameClassifier(),
				new XEventLifeTransClassifier());
		return main(context, log, classifiers, transitionClassifier);
	}
	
	@PluginVariant(variantLabel = "Mine Transition System using Default Classifiers with parameters", requiredParameterLabels = { 0, 3 })
	public static Object[] main(final PluginContext context, final XLog log, final TSMinerInput input) {
		/**
		 * No classifiers provided, use default classifiers.
		 */
		XEventClassifier[] classifiers = new XEventClassifier[3];
		classifiers[0] = new XEventNameClassifier();
		classifiers[1] = new XEventResourceClassifier();
		classifiers[2] = new XEventLifeTransClassifier();
		XEventClassifier transitionClassifier = new XEventAndClassifier(new XEventNameClassifier(),
				new XEventLifeTransClassifier());
		return main(context, log, classifiers, transitionClassifier, input);
	}

	@PluginVariant(variantLabel = "Mine Transition System", requiredParameterLabels = { 0, 1, 2 })
	public static Object[] main(final PluginContext context, final XLog log, final XEventClassifier[] classifiers,
			final XEventClassifier transitionClassifier) {
		TSMiner miner = new TSMiner(context);
		setLabels(context, log);
		TSMinerInput input = new TSMinerInput(context, log, Arrays.asList(classifiers), transitionClassifier);
		TSMinerOutput result = miner.mine(input);
		result.getTransitionSystem().getAttributeMap().put(AttributeMap.LABEL, context.getFutureResult(0).getLabel());

		return new Object[] { result.getTransitionSystem(), result.getWeights(), result.getStarts(),
				result.getAccepts() };
	}

	@PluginVariant(variantLabel = "Mine Transition System", requiredParameterLabels = { 0, 1, 2, 3 })
	public static Object[] main(final PluginContext context, final XLog log, final XEventClassifier[] classifiers,
			final XEventClassifier transitionClassifier, final TSMinerInput input) {
		TSMiner miner = new TSMiner(context);
		setLabels(context, log);
		TSMinerOutput result = miner.mine(input);
		result.getTransitionSystem().getAttributeMap().put(AttributeMap.LABEL, context.getFutureResult(0).getLabel());

		return new Object[] { result.getTransitionSystem(), result.getWeights(), result.getStarts(),
				result.getAccepts() };
	}

	/**
	 * Sets the labels for the pending future results.
	 * 
	 * @param context
	 *            The current context.
	 * @param log
	 *            The log used for this mining operation.
	 */
	public static void setLabels(PluginContext context, XLog log) {
		context.getFutureResult(0).setLabel("PTS (mined from " + XConceptExtension.instance().extractName(log) + ")");
		context.getFutureResult(1).setLabel(
				"PTS weights (mined from " + XConceptExtension.instance().extractName(log) + ")");
		context.getFutureResult(2).setLabel(
				"Start states (mined from " + XConceptExtension.instance().extractName(log) + ")");
		context.getFutureResult(3).setLabel(
				"Accept states (mined from " + XConceptExtension.instance().extractName(log) + ")");
	}
	
	/**
	 * 
	 * 
	 * These are access method that do not use futures, with and without UI
	 * 
	 * 
	 */
	public static Object[] main_NoFutures(final UIPluginContext context, final XLog log) {
		
		XEventClassifier[] classifiers;
		XEventClassifier transitionClassifier;
		if (log.getClassifiers().size() > 0) {
			classifiers = new XEventClassifier[log.getClassifiers().size()];
			int i = 0;
			for (XEventClassifier classifier : log.getClassifiers()) {
				classifiers[i++] = classifier;
			}
			transitionClassifier = classifiers[0];
		} else {
			classifiers = new XEventClassifier[3];
			classifiers[0] = new XEventNameClassifier();
			classifiers[1] = new XEventResourceClassifier();
			classifiers[2] = new XEventLifeTransClassifier();
			transitionClassifier = new XEventAndClassifier(new XEventNameClassifier(), new XEventLifeTransClassifier());

		}
		TSMinerUI miner = new TSMinerUI(context);
		return miner.mine(log, Arrays.asList(classifiers), transitionClassifier);
	}
	public static Object[] main_NoFutures(final PluginContext context, final XLog log) {
		
		XEventClassifier[] classifiers = new XEventClassifier[3];
		classifiers[0] = new XEventNameClassifier();
		classifiers[1] = new XEventResourceClassifier();
		classifiers[2] = new XEventLifeTransClassifier();
		XEventClassifier transitionClassifier = new XEventAndClassifier(new XEventNameClassifier(),
				new XEventLifeTransClassifier());

		TSMiner miner = new TSMiner(context);
		TSMinerInput input = new TSMinerInput(context, log, Arrays.asList(classifiers), transitionClassifier);
		TSMinerOutput result = miner.mine(input);
		return new Object[] { result.getTransitionSystem(), result.getWeights(), result.getStarts(),
				result.getAccepts() };
	}
	
}
