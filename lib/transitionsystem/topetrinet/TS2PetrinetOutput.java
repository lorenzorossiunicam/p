package org.processmining.plugins.transitionsystem.topetrinet;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;

public class TS2PetrinetOutput {

	private Petrinet net;

	public TS2PetrinetOutput() {
		net = null;
	}

	public Petrinet setPetrinet(Petrinet newNet) {
		Petrinet oldNet = net;
		net = newNet;
		return oldNet;
	}

	public Petrinet getPetrinet() {
		return net;
	}
}
