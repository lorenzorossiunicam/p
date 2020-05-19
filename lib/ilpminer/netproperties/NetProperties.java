package org.processmining.plugins.ilpminer.netproperties;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;

//@Plugin(name = "Net Average connector degree and Density", parameterLabels = { "Petri net" }, returnLabels = { "Net Properties" }, returnTypes = { NetPropertiesResult.class })
public class NetProperties {
	private double numConnectors, numPlaces, numTransitions, totalDegree, totalArcs;

//	@UITopiaVariant(uiLabel = "Net Average connector degree and Density", affiliation = UITopiaVariant.EHV, author = "T. van der Wiel", email = "t.v.d.wiel@student.tue.nl")
//	@PluginVariant(variantLabel = "Net Average connector degree and Density", requiredParameterLabels = { 0 })
	public NetPropertiesResult doTransitionFitness(UIPluginContext context, Petrinet net) {
		numConnectors = 0;
		totalDegree = 0;
		for(PetrinetNode node : net.getNodes()) {
			if(net.getInEdges(node).size() > 1) {
				numConnectors++;
				totalDegree += net.getInEdges(node).size();
			}
			if(net.getOutEdges(node).size() > 1) {
				numConnectors++;
				totalDegree += net.getOutEdges(node).size();
			}
		}
		numPlaces = net.getPlaces().size();
		numTransitions = net.getTransitions().size();
		totalArcs = net.getEdges().size();
		NetPropertiesResult result = new NetPropertiesResult(totalDegree/numConnectors, totalArcs/(numPlaces*numTransitions*2));
		NetPropertiesConnection connection = new NetPropertiesConnection(result, net);
		context.getConnectionManager().addConnection(connection);
		return result;
	}
}
