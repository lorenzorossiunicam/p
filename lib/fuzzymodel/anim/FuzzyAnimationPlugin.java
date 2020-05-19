package org.processmining.plugins.fuzzymodel.anim;

import java.util.Iterator;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.fuzzymodel.MutableFuzzyGraph;
import org.processmining.plugins.fuzzymodel.miner.filter.FMEventEditor;
import org.processmining.plugins.fuzzymodel.miner.filter.FMEventTimeInjectionFilter;
import org.processmining.plugins.fuzzymodel.miner.filter.FMTraceEditor;
import org.processmining.plugins.log.logfilters.LogFilter;
import org.processmining.plugins.log.logfilters.LogFilterException;

@Plugin(name = "Animate Event Log in Fuzzy Instance", level = PluginLevel.PeerReviewed, parameterLabels = { "Fuzzy Instance", "Log" }, returnLabels = { "Fuzzy Animation" }, returnTypes = { FuzzyAnimation.class }, userAccessible = true, help = FuzzyAnimationHelp.TEXT)
public class FuzzyAnimationPlugin {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H. Verbeek", email = "h.m.w.verbeek@tue.nl", uiLabel = UITopiaVariant.USEPLUGIN, pack = "Fuzzy")
	@PluginVariant(variantLabel = "Select conversions to use", requiredParameterLabels = { 0, 1 })
	public FuzzyAnimation options(final UIPluginContext context, final MutableFuzzyGraph mfg, final XLog log) {
		FuzzyAnimationWizard wizard = new FuzzyAnimationWizard(context, log);
		InteractionResult result = wizard.show();
		if (result == InteractionResult.FINISHED) {
			FMEventEditor fmEventEditor = new FMEventEditor(context, mfg);
			FMTraceEditor fmTraceEditor = new FMTraceEditor();
			try {
				XLog filteredLog = LogFilter.filter(context.getProgress(), 1, log, null, fmEventEditor, fmTraceEditor);
				if (wizard.getDiscreteAnimation()) {
					FMEventTimeInjectionFilter tsFilter = new FMEventTimeInjectionFilter();
					Iterator<XTrace> it = filteredLog.iterator();
					while (it.hasNext()) {
						XTrace oldTrace = it.next();
						tsFilter.doFiltering(oldTrace);
					}
				}
				FuzzyAnimation animation = new FuzzyAnimation(context, mfg, filteredLog, wizard.getLookAhead(), wizard.getExtraLookAhead());
				animation.initialize(context, mfg, filteredLog);
				return animation;
			} catch (LogFilterException filterException) {
				filterException.printStackTrace();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		return null;
	}

}
