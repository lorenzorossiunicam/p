package it.unicam.pros.purple.simulation.utils;

import it.unicam.pros.purple.semanticengine.Configuration;
import it.unicam.pros.purple.simulation.SimulatorImpl.LabelledEdge;
import it.unicam.pros.purple.util.eventlogs.trace.Trace;
import it.unicam.pros.purple.util.eventlogs.trace.TraceImpl;
import it.unicam.pros.purple.util.eventlogs.trace.event.Event;
import it.unicam.pros.purple.util.eventlogs.utils.LogUtil;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DirectedWeightedPseudograph;

import java.util.*;

public class LTSUtil {

	public static Set<Configuration> findConf(Graph<Configuration, LabelledEdge> lts, Event e) {
		Set<Configuration> ret = new HashSet<Configuration>();
		for (LabelledEdge edge : lts.edgeSet()) {
			if (LogUtil.compareEvents(edge.getLabel(), e)) {
				ret.add(lts.getEdgeTarget(edge));
			}
		}
		return ret;

	}

	public static Map<String, Map<String, Trace>> getPrefix(
			DirectedWeightedPseudograph<Configuration, LabelledEdge> lts, Set<Configuration> dataConfigurations,
			Configuration s) {
		Map<String, Map<String, Trace>> t = new HashMap<String, Map<String, Trace>>();
		Stack<Event> stack = new Stack<Event>();
		List<Configuration> predecessors = Graphs.predecessorListOf(lts, s);
		Configuration initData = null, pred = null, curr = s;
		while (!dataConfigurations.contains(curr)) {
			pred = predecessors.get((int) (Math.random() * predecessors.size()));
			Event event = lts.getEdge(pred, curr).getLabel();
			stack.push(event);
			initData = curr;
			curr = pred;
			predecessors = Graphs.predecessorListOf(lts, curr);
		}

		for (String proc : initData.getProcesses()) {
			t.put(proc, new HashMap<String, Trace>());
			for (String inst : initData.getInstances(proc)) {
				t.get(proc).put(inst, new TraceImpl(initData.getLocalData()));
			}
		}
		while (!stack.isEmpty()) {
			Event e = stack.pop();
			if (e.isEmptyEvent())
				continue;
			t.get(e.getProcess()).get(e.getInstance()).appendEvent(e);
		}
		return t;
	}
}
