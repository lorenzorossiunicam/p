package org.processmining.plugins.fuzzymodel.adapter;

import java.io.IOException;
import java.util.ArrayList;

import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.models.graphbased.directed.fuzzymodel.FuzzyGraph;
import org.processmining.models.graphbased.directed.fuzzymodel.util.FMLogEvents;
import org.processmining.plugins.fuzzymodel.miner.replay.TraceReplay;

public class Conformance {
	protected FuzzyGraph graph;
	protected XLog log;
	protected ArrayList<TraceReplay> traceReplays;
	protected double value;

	public Conformance(FuzzyGraph graph, XLog log) throws IndexOutOfBoundsException, IOException {
		this.graph = graph;
		this.log = log;
		replay(this.log);
	}

	public double getValue() {
		return value;
	}

	public int numberOfTraces() {
		return traceReplays.size();
	}

	public TraceReplay getTraceReplay(int traceIndex) {
		return traceReplays.get(traceIndex);
	}

	public void replay(XLog reader) throws IndexOutOfBoundsException, IOException {

		XLog log = this.log;
		if (reader != null) {
			log = reader;
		}
		XLogInfo logInfo = XLogInfoFactory.createLogInfo(log);
		int numberOfInstances = logInfo.getNumberOfTraces();
		// replay all instances
		double aggregated = 0.0;
		this.traceReplays = new ArrayList<TraceReplay>();
		//	FMLogEvents events = FuzzyMinerLog.getLogEvents(log);
		FMLogEvents events = graph.getLogEvents();

		for (int i = 0; i < numberOfInstances; i++) {
			TraceReplay replay = new TraceReplay(this.graph, log, events, i);
			aggregated += replay.getCoverage();
			traceReplays.add(replay);
		}

		// rectify aggregated value
		value = aggregated / traceReplays.size();
	}
}
