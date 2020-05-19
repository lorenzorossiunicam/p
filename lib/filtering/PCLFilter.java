package org.processmining.plugins.filtering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

/**
 * This plugin does even log filtering based on Prefix-Closed Language
 * 
 * @author V. Kliger
 * 
 */
@Plugin(name = "Filter Log using Prefix-Closed Language (PCL)", parameterLabels = { "Log" }, returnLabels = { "Log" }, returnTypes = { XLog.class }, userAccessible = true)
public class PCLFilter {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "Vitaly Kliger", email = "v.kliger@gmail.com")
	@PluginVariant(variantLabel = "Default", requiredParameterLabels = { 0 })
	public XLog main(UIPluginContext context, XLog log) {

		// ask user to enter parameters
		PCLFilterUI ui = new PCLFilterUI();
		InteractionResult result = context.showConfiguration("Prefix-Closed Language Filter Parameters", ui);
		if (result.equals(InteractionResult.CANCEL)) {
			context.getFutureResult(0).cancel(true);
			return null;
		}
		// retrieve the entered parameters
		PCLFilterSettings pclfiltersettings = ui.getSettings();
		context.getProgress().setIndeterminate(true);

		XLogInfo summary = XLogInfoFactory.createLogInfo(log);
		XEventClasses classes = summary.getEventClasses();

		// create the mapping between the eventclasses and an integer for a
		// smaller ILP model
		Map<XEventClass, Integer> indices = new HashMap<XEventClass, Integer>();
		int i = 0;
		for (XEventClass evClass : classes.getClasses()) {
			indices.put(evClass, i);
			i++;
		}
		// we do filtering of the copy of the log
		// original log remains untouched 
		XLog newlog = (XLog) log.clone();

		filter(newlog, indices, classes, pclfiltersettings);

		//mark the new log with filtering parameters and the number of remaining traces  
		context.getFutureResult(0).setLabel(
				XConceptExtension.instance().extractName(log) + " (filtered with PCL, thresholds: abs="
						+ pclfiltersettings.getAbsThreshold() + ", rel="
						+ (int) (pclfiltersettings.getRelThreshold() * 100) + "%, retained " + newlog.size() + "/"
						+ log.size() + " traces)");
		return newlog;
	}

	protected void filter(XLog log, Map<XEventClass, Integer> indices, XEventClasses classes,
			PCLFilterSettings pclfilter) {
		Map<Trace, Integer> traces = new HashMap<Trace, Integer>();
		Trace startPlace = new Trace(); //empty trace corresponding to start place

		int index = 0;
		for (XTrace t : log) {
			// we want all prefixes of t (including t)
			// since traces is a set, duplicates should be removed automatically because the trace overrides the equals method
			for (int i = 1; i <= t.size(); i++) {
				Trace trace = new Trace();
				for (int j = 0; j < i; j++) {
					trace.addEvent(indices.get(classes.getClassOf(t.get(j))));
				}

				// Check if the trace is already in 'traces'. If not, add it there
				// We iterate over traces only once
				for (Trace tr : traces.keySet()) {
					if (tr.equals(trace)) {
						tr.addXtrace(t);
						trace = null;
						break;
					}
				}
				if (trace != null) {
					traces.put(trace, index);
					trace.addXtrace(t);
					if (i == 1)
						trace.predecessor = startPlace;
					else
						trace.predecessor = findPredecessor(trace, traces);
					trace.predecessor.addSuccessor(trace);
					index++;
				}
			}
		}
		if (pclfilter == null)
			return; // no filtering

		Set<XTrace> XTraceRemove = new HashSet<XTrace>();
		Set<Trace> TraceRemove = new HashSet<Trace>();

		// We delete traces that do not meet the filtering criteria
		// We delete the trace an all it descendants containing the current trace as a prefix  
		// Since we can't iterate over map and delete arbitrary traces in the same cycle,   
		// we add them to TraceRemove set and delete afterwards
		for (Trace trace : traces.keySet()) {

			//we don't check traces that have been already added to TraceRemove during previous iterations 
			if (TraceRemove.contains(trace))
				continue;

			// the main test condition
			// trace.predecessor == null corresponds to the shorted trace (word of prefix-closed language) consisting of only one start event 
			if (trace.getWeight() < pclfilter.getAbsThreshold()
					|| (double) trace.getWeight()
							/ trace.predecessor.getMaxSuccessor(trace.predecessor == startPlace ? log.size()
									: trace.predecessor.getWeight()) < pclfilter.getRelThreshold()) {
				XTraceRemove.addAll(trace.getXTraces());
				for (Trace tr : traces.keySet())
					if (tr.containsPrefix(trace))
						TraceRemove.add(tr);
			}
		}

		//actual removing
		log.removeAll(XTraceRemove);
		for (Trace trace : TraceRemove)
			traces.remove(trace);
	}

	/**
	 * returns the predecessor of the trace (word) in Prefix-closed language for
	 * the trace <a1><a2>...<aN-1><aN> it returns the trace <a1><a2>...<aN-1>
	 * for the trace consisting of only one/first event, it returns null
	 * 
	 * @return Trace
	 */
	private Trace findPredecessor(Trace trace, Map<Trace, Integer> traces) {
		for (Trace t : traces.keySet())
			if ((t.length() == trace.length() - 1) && trace.containsPrefix(t))
				return t;
		return null;
	}

	/**
	 * Internally used to represent a word in the language (consisting of an
	 * ordered list of letters)
	 * 
	 * @author V. Kliger (based on T. van der Wiel)
	 * 
	 */
	class Trace {
		private final ArrayList<Integer> events = new ArrayList<Integer>();
		protected String id = "id";
		protected Trace predecessor;
		private int maxSuccessor = -1;
		private final Set<Trace> successors = new HashSet<Trace>();
		private final ArrayList<XTrace> XTraces = new ArrayList<XTrace>();

		void addSuccessor(Trace tr) {
			successors.add(tr);
		}

		int getMaxSuccessor(int sumWeight) {
			if (maxSuccessor != -1)
				return maxSuccessor;
			int nullSuccessorWeight = sumWeight;
			for (Trace tr : successors) {
				int traceWeight = tr.getWeight();
				nullSuccessorWeight -= traceWeight;
				if (tr.getWeight() > maxSuccessor)
					maxSuccessor = tr.getWeight();
			}
			if (nullSuccessorWeight > maxSuccessor)
				maxSuccessor = nullSuccessorWeight;
			return maxSuccessor;
		}

		int getWeight() {
			return XTraces.size();
		}

		void addXtrace(XTrace xt) {
			XTraces.add(xt);
		}

		ArrayList<XTrace> getXTraces() {
			return XTraces;
		}

		boolean containsPrefix(Trace t) {
			if (id.startsWith(t.id))
				return true;
			return false;
		}

		void addEvent(int event) {
			events.add(event);
			id += "-" + event;
		}

		int length() {
			return events.size();
		}

		public boolean equals(Object trace) {
			if ((trace != null) && (trace.getClass() == Trace.class)) {
				return ((Trace) trace).id.equals(id);
			}
			return false;
		}

		public String toString() {
			return id;
		}
	}

}
