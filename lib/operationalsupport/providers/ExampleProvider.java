package org.processmining.plugins.operationalsupport.providers;

import java.util.ArrayList;
import java.util.Collections;

import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.socket.ServiceEnvironment;
import org.processmining.models.operationalsupport.Analysis;
import org.processmining.models.operationalsupport.Attributes;
import org.processmining.models.operationalsupport.Prediction;
import org.processmining.models.operationalsupport.ProviderResponse;
import org.processmining.models.operationalsupport.Recommendation;
import org.processmining.models.operationalsupport.RecommendationItem;
import org.processmining.models.operationalsupport.RecommendationItemComparator;
import org.processmining.models.operationalsupport.Request;
import org.processmining.models.operationalsupport.net.provider.Provider;
import org.processmining.models.operationalsupport.net.provider.impl.AbstractProvider;
import org.processmining.models.operationalsupport.net.service.OSService;

/**
 * 
 * @author mschonen; joyce nakatumba This provider can be used as an example on
 *         how to create analysis, prediction or recommendation response for
 *         other providers
 * 
 */

@Plugin(name = "Example Provider", parameterLabels = { "Operational Support Service" }, returnLabels = { "Example Provider" }, returnTypes = { AbstractProvider.class }, userAccessible = true)
public class ExampleProvider extends AbstractProvider {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "M.H. Schonenberg", email = "m.h.schonenberg@tue.nl", uiLabel = UITopiaVariant.USEPLUGIN)
	@PluginVariant(variantLabel = "Default settings", requiredParameterLabels = { 0 })
	public static Provider registerServiceProviderAUI(final UIPluginContext context, OSService service) {
		return registerServiceProviderA(context, service);
	}

	@PluginVariant(variantLabel = "Default settings", requiredParameterLabels = { 0 })
	public static Provider registerServiceProviderA(final PluginContext context, OSService service) {

		ExampleProvider dummy = new ExampleProvider(service);

		context.getFutureResult(0).setLabel(context.getFutureResult(0).getLabel() + " on ports" + service);

		return dummy;
	}

	public ExampleProvider(OSService owner) {
		super(owner);
	}

	public void populateResponse(ServiceEnvironment environment, Request request, ProviderResponse response) {

		environment.log("Example Provider is processing request " + request.getId());

		// creating analysis response items
		Analysis a = new Analysis("The analysis of some plugin");
		a.getAttributes("Analysis").put("Average throughput time", Math.random());
		a.getAttributes("Analysis").put("Mean throughput time", Math.random());
		a.getAttributes("Analysis").put("Min throughput time", Math.random());
		a.getAttributes("Analysis").put("Max throughput time", Math.random());
		response.addAnalysis(a);

		Analysis a2 = new Analysis("The analysis of another plugin");
		a2.getAttributes("Analysis").put("Estimated time to end", Math.random());
		a2.getAttributes("Analysis").put("Average log time to end", Math.random());
		response.addAnalysis(a2);

		// creating prediction response items
		Prediction p = new Prediction("You can put a description of the prediction here: " + Math.random());
		p.getAttributes("Statistics").put("std. dev.", Math.random()); // you can put different attributes in the prediction item
		p.getAttributes("Statistics").put("mean", Math.random());
		p.getAttributes("Statistics").put("mode", Math.random());
		p.getAttributes("Statistics").put("whatever statistic", Math.random());
		response.addPrediction(p);

		// creating recommendation response items
		RecommendationItem item1 = createRecommendationItem("Program", "Helen");
		RecommendationItem item2 = createRecommendationItem("Program", "Joyce");
		RecommendationItem item3 = createRecommendationItem("Thesis", "Carmen");
		ArrayList<RecommendationItem> recItems = new ArrayList<RecommendationItem>();
		recItems.add(item1);
		recItems.add(item2);
		recItems.add(item3);
		Collections.sort(recItems, new RecommendationItemComparator());
		Recommendation recommendation = new Recommendation("My first recommendation", recItems);
		response.addRecommendation(recommendation);

		RecommendationItem item4 = createRecommendationItem("Sports", "Helen");
		RecommendationItem item5 = createRecommendationItem("Cook", "Joyce");
		RecommendationItem item6 = createRecommendationItem("Talk", "Carmen");
		ArrayList<RecommendationItem> recItems2 = new ArrayList<RecommendationItem>();
		recItems2.add(item4);
		recItems2.add(item5);
		recItems2.add(item6);
		Collections.sort(recItems2, new RecommendationItemComparator());
		Recommendation rec2 = new Recommendation("second recommendation", recItems2);
		response.addRecommendation(rec2);

	}

	/**
	 * Creates a recommendation item, with given task name and resource name,
	 * other attributes are randomly generated
	 * 
	 * @param task
	 *            the task name for the recommendation item
	 * @param resource
	 *            the resource name
	 * @return a recommendation item, with given task name and resource name,
	 *         other attributes are randomly generated
	 */
	protected RecommendationItem createRecommendationItem(String task, String resource) {
		Attributes attrs = new Attributes();
		attrs.put("task", task);
		attrs.put("transition", XLifecycleExtension.StandardModel.COMPLETE);
		attrs.put("authorized", resource);
		attrs.put("prediction", Math.random());
		attrs.put("confidence", Math.random());

		return new RecommendationItem(attrs);
	}

}