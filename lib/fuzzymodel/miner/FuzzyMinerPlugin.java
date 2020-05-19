package org.processmining.plugins.fuzzymodel.miner;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.plugin.events.Logger.MessageLevel;
import org.processmining.models.graphbased.directed.fuzzymodel.attenuation.Attenuation;
import org.processmining.models.graphbased.directed.fuzzymodel.attenuation.NRootAttenuation;
import org.processmining.models.graphbased.directed.fuzzymodel.metrics.Metric;
import org.processmining.models.graphbased.directed.fuzzymodel.metrics.MetricsRepository;
import org.processmining.plugins.fuzzymodel.miner.ui.FuzzyMinerWizard;

@Plugin(name = "Mine Fuzzy Model", level = PluginLevel.PeerReviewed, categories = { PluginCategory.Discovery }, parameterLabels = { "Log", "Metrics", "Attenuation", "Max Distance" }, returnLabels = { "Fuzzy Model" }, returnTypes = { MetricsRepository.class }, help = FuzzyMinerHelp.TEXT)
public class FuzzyMinerPlugin {

	@PluginVariant(variantLabel = "Mine Fuzzy Model", requiredParameterLabels = { 0, 1, 2, 3 })
	public MetricsRepository mineGeneric(final PluginContext context, XLog log, MetricsRepository repository,
			Attenuation attenuation, Integer maxDistance) {
		MetricsRepository minedRepository = repository; //MetricsRepository.createRepository(logInfo);
		//		copySettings(metrics.getBinaryDerivateMetrics(), minedMetrics.getBinaryDerivateMetrics());
		//		copySettings(metrics.getBinaryLogMetrics(), minedMetrics.getBinaryLogMetrics());
		//		copySettings(metrics.getCorrelationBinaryLogMetrics(), minedMetrics.getCorrelationBinaryLogMetrics());
		//		copySettings(metrics.getCorrelationBinaryMetrics(), minedMetrics.getCorrelationBinaryMetrics());
		//		copySettings(metrics.getSignificanceBinaryLogMetrics(), minedMetrics.getSignificanceBinaryLogMetrics());
		//		copySettings(metrics.getSignificanceBinaryMetrics(), minedMetrics.getSignificanceBinaryMetrics());
		//		copySettings(metrics.getUnaryDerivateMetrics(), minedMetrics.getUnaryDerivateMetrics());
		//		copySettings(metrics.getUnaryLogMetrics(), minedMetrics.getUnaryLogMetrics());
		//		copySettings(metrics.getUnaryMetrics(), minedMetrics.getUnaryMetrics());
		long time = System.currentTimeMillis();
		minedRepository.apply(log, attenuation, maxDistance, context);
		time = System.currentTimeMillis() - time;
		String logStr = new String("Mine Fuzzy Model: Took " + time + " ms.");
		context.log(logStr, MessageLevel.NORMAL);
		return minedRepository;
	}

	@PluginVariant(variantLabel = "Mine Fuzzy Model using Default Settings", requiredParameterLabels = { 0 })
	public MetricsRepository mineDefault(final PluginContext context, XLog log) {
		XEventClassifier classifier = new XEventAndClassifier(new XEventNameClassifier(), new XEventLifeTransClassifier());
		XLogInfo logInfo = XLogInfoFactory.createLogInfo(log, classifier);
		MetricsRepository metrics = MetricsRepository.createRepository(logInfo);
		Attenuation attenuation = new NRootAttenuation(2.7, 5);
		int maxDistance = 4;
		return mineGeneric(context, log, metrics, attenuation, maxDistance);
	}

	@UITopiaVariant(uiLabel = "Mine for a Fuzzy Model", affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl", pack = "Fuzzy")
	@PluginVariant(variantLabel = "Mine Fuzzy Model using Wizard", requiredParameterLabels = { 0 })
	public MetricsRepository mineWizard(final UIPluginContext context, XLog log) {
		FuzzyMinerWizard wizard = new FuzzyMinerWizard(context, log);
		InteractionResult result = wizard.show();
		if (result == InteractionResult.FINISHED) {
			MetricsRepository metrics = wizard.getMetrics();
			Attenuation attenuation = wizard.getAttenuation();
			int maxDistance = wizard.getMaxDistance();
			return mineGeneric(context, log, metrics, attenuation, maxDistance);
		}
		return null;
	}

	private void copySettings(Collection<? extends Metric> fromMetrics, Collection<? extends Metric> toMetrics) {
		Map<String, Metric> metricMap = new HashMap<String, Metric>();
		for (Metric toMetric : toMetrics) {
			metricMap.put(toMetric.getName(), toMetric);
		}
		for (Metric fromMetric : fromMetrics) {
			Metric toMetric = metricMap.get(fromMetric.getName());
			if (toMetric != null) {
				toMetric.setNormalizationMaximum(fromMetric.getNormalizationMaximum());
				toMetric.setInvert(fromMetric.getInvert());
			}
		}
	}
}
