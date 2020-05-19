package org.processmining.plugins.ilpminer;

import java.util.Map;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.model.XLog;
import org.processmining.connections.logmodel.LogPetrinetConnectionImpl;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

public class ILPMinerLogPetrinetConnection extends LogPetrinetConnectionImpl {
	public final static String SETTINGS = "Miner Settings";

	public ILPMinerLogPetrinetConnection(XLog log, XEventClasses classes,
			Petrinet net,
			Map<Transition, XEventClass> mapping,
			ILPMinerSettings settings) {
		super(log, classes, net, mapping);
		put(SETTINGS, settings);
	}
}
