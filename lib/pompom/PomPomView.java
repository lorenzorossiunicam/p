package org.processmining.plugins.pompom;

import java.awt.Dimension;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.swing.JComponent;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.connections.petrinets.behavioral.InitialMarkingConnection;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.directed.DirectedGraph;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.jgraph.ProMJGraphVisualizer;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.petrinet.epcconversion.EpcConversion;
import org.processmining.plugins.petrinet.reduction.Murata;
import org.processmining.plugins.petrinet.reduction.MurataInput;
import org.processmining.plugins.petrinet.reduction.MurataOutput;

@Plugin(name = "Show PomPom View", level = PluginLevel.PeerReviewed, returnLabels = { "PomPom View" }, returnTypes = { PomPomView.class }, parameterLabels = { "Petri net", "Event log" }, userAccessible = true, help = PomPomHelp.TEXT)
public class PomPomView {

	private final Petrinet net;
	private final Marking initialMarking;

	private final XLog log;
	private final XEventClassifier classifier;

	private Map<Transition, Float> weights;

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl", pack = "PomPomView")
	@PluginVariant(variantLabel = "PomPom View, UI", requiredParameterLabels = { 0, 1 })
	public static PomPomView runUI(UIPluginContext context, Petrinet net, XLog log) {
		context.getFutureResult(0).setLabel("PomPom View for " + net.getLabel());
		PomPomParameters parameters = new PomPomParameters(log, net);
		PomPomDialog dialog = new PomPomDialog(log, parameters);
		InteractionResult result = context.showWizard("Configure PomPomView (classifier)", true, true, dialog);
		if (result != InteractionResult.FINISHED) {
			return null;
		}
		return new PomPomView(context, net, null, log, parameters.getClassifier());
	}

	@PluginVariant(variantLabel = "PomPom View, headless", requiredParameterLabels = { 0, 1 })
	public static PomPomView run(PluginContext context, Petrinet net, XLog log) {
		context.getFutureResult(0).setLabel("PomPom View for " + net.getLabel());
		return new PomPomView(context, net, null, log, null);
	}

	public PomPomView(PluginContext context, Petrinet net, Marking initialMarking, XLog log, XEventClassifier classifier) {
		this.net = net;
		if (initialMarking == null) {
			InitialMarkingConnection connection;
			try {
				connection = context.getConnectionManager().getFirstConnection(InitialMarkingConnection.class, context,
						net);
				initialMarking = connection.getObjectWithRole(InitialMarkingConnection.MARKING);
			} catch (ConnectionCannotBeObtained e) {
				// No initial marking found. Use empty marking by lack of alternatives.
				initialMarking = new Marking();
			}
		}
		this.initialMarking = initialMarking;
		this.log = log;
		if (classifier == null) {
			classifier = new XEventAndClassifier(new XEventNameClassifier(), new XEventLifeTransClassifier());
		}
		this.classifier = classifier;

		initializeWeights();
	}

	private void initializeWeights() {
		/*
		 * Map identities to transitions.
		 */
		Map<String, Collection<Transition>> table = new HashMap<String, Collection<Transition>>();
		for (Transition transition : net.getTransitions()) {
			String transId = transition.getLabel();
			if (!table.containsKey(transId)) {
				table.put(transId, new HashSet<Transition>());
			}
			Collection<Transition> transitions = table.get(transId);
			transitions.add(transition);
		}
		/*
		 * Build the weights table.
		 */
		Map<Transition, Float> tmpWeights = new HashMap<Transition, Float>();
		for (Transition transition : net.getTransitions()) {
			tmpWeights.put(transition, 0.0f);
		}
		for (XTrace trace : log) {
			for (XEvent event : trace) {
				String classId = classifier.getClassIdentity(event);
				if (table.containsKey(classId)) {
					Collection<Transition> transitions = table.get(classId);
					for (Transition transition : transitions) {
						tmpWeights.put(transition, tmpWeights.get(transition) + 1.0f / transitions.size());
					}
				}
			}
		}
		// System.out.println(tmpWeights);
		/*
		 * Normalize the weights table.
		 */
		weights = new HashMap<Transition, Float>();
		if (!tmpWeights.isEmpty()) {
			Float minWeight = Float.MAX_VALUE;
			Float maxWeight = Float.MIN_VALUE;
			for (Transition transition : tmpWeights.keySet()) {
				Float weight = tmpWeights.get(transition);
				if (weight < minWeight) {
					minWeight = weight;
				}
				if (weight > maxWeight) {
					maxWeight = weight;
				}
			}
			minWeight -= 1.0f;
			maxWeight += 1.0f;
			// System.out.println(minWeight + ", " + maxWeight);
			for (Transition transition : tmpWeights.keySet()) {
				Float weight = tmpWeights.get(transition);
				weights.put(transition, (weight - minWeight) / (maxWeight - minWeight));
			}
		}
		// System.out.println(weights);
	}

	public JComponent generateView(PluginContext context, final Float tresholdWeight, int viewType) {
		MurataInput murataInput = new MurataInput(net, initialMarking);
		for (Transition transition : net.getTransitions()) {
			Float weight = (weights.containsKey(transition) ? weights.get(transition) : 0.0f);
			if (weight >= tresholdWeight) {
				murataInput.addSacred(transition);
			}
		}
		Murata murata = new Murata();
		DirectedGraph<?, ?> graph = null;
		try {
			MurataOutput murataOutput = murata.run(null, murataInput);
			Map<Transition, Transition> map = murataOutput.getTransitionMapping();
			for (Transition transition : map.keySet()) {
				Float weight = (weights.containsKey(transition) ? weights.get(transition) : 0.0f);
				Transition displayTransition = map.get(transition);
				if (weight < tresholdWeight) {
					displayTransition.setInvisible(true);
				} else {
					displayTransition.setInvisible(false);
					Dimension size = (Dimension) displayTransition.getAttributeMap().get(AttributeMap.SIZE);
					size.setSize(size.getWidth() * (1 + weight), size.getHeight() * (1 + weight));
				}
			}
			Petrinet net = murataOutput.getNet();
			switch (viewType) {
				default :
					graph = net;
					break;
				case 1 :
					graph = EpcConversion.convert(net);
					break;
			}
			// return ProMJGraphVisualizer.getVisualizationPanel(graph, new
			// ViewSpecificAttributeMap(), null);
			// return new PetrinetInformationPanel(murataOutput.getNet(),
			// murataOutput.getMarking(), null);
			// return
			// PetriNetVisualization.getComponent(context.createChildContext(""),
			// murataOutput.getNet(), murataOutput.getMarking());
		} catch (ConnectionCannotBeObtained e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ProMJGraphVisualizer.instance().visualizeGraph(context, graph);
		// return new PetrinetInformationPanel(net, initialMarking, null);
	}
}
