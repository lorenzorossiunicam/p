package org.processmining.plugins.ilpminer.fitness;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.connections.annotations.ConnectionObjectFactory;
import org.processmining.framework.connections.impl.AbstractConnection;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;

@Plugin(name = "Transition Fitness Petrinet Log Connection Factory", parameterLabels = { "Transition Fitness", "Log",
		"Petrinet" }, returnTypes = ILPFitnessConnection.class, returnLabels = "Fitness Petrinet Log connection", userAccessible = false)
@ConnectionObjectFactory
public class ILPFitnessConnection extends AbstractConnection {

	public static String FITNESS = "Transition Fitness";
	public static String PNET = "Petrinet";
	public static String XLOG = "XLog";

	public ILPFitnessConnection(ILPFitnessResult result, XLog log, Petrinet net) {
		super("FitnessConnection");
		put(FITNESS, result);
		put(XLOG, log);
		put(PNET, net);
	}
}
