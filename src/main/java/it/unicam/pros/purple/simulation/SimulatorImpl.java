package it.unicam.pros.purple.simulation;

import java.util.*;

import it.unicam.pros.purple.PURPLE;
import it.unicam.pros.purple.semanticengine.SemanticEngine;
import it.unicam.pros.purple.evaluator.Delta;
import it.unicam.pros.purple.util.Couple;
import it.unicam.pros.purple.simulation.utils.LTSUtil;
import it.unicam.pros.purple.util.eventlogs.utils.LogUtil;
import it.unicam.pros.purple.util.deepcopy.DeepCopy;
import it.unicam.pros.purple.util.eventlogs.EventLog;
import it.unicam.pros.purple.util.eventlogs.EventLogImpl;
import it.unicam.pros.purple.util.eventlogs.trace.Trace;
import it.unicam.pros.purple.util.eventlogs.trace.event.Event;
import it.unicam.pros.purple.util.eventlogs.trace.event.EventImpl;
import it.unicam.pros.purple.semanticengine.Configuration;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedWeightedPseudograph;

/**
 * 
 * This class represents the guided simulator for behavioral models @see
 * {@link SemanticEngine}. The simulation is guided through a precise path by a
 * delta parameter @see {@link Delta}, in case delta is null the simulator runs
 * a random execution of the model.
 * 
 * 
 * @author Lorenzo Rossi
 *
 */
public class SimulatorImpl implements Simulator {



	private DirectedWeightedPseudograph<Configuration, LabelledEdge> lts;
	private Configuration root;
	private EventLog log;
	private Set<Configuration> dataConfigurations = new HashSet<Configuration>();
	private SemanticEngine semanticEngine;
	private Delta lastDelta = null;
	//private static final GSimLogger LOGGER = GSimLogger.getLogger(SimulatorImpl.class);

	/**
	 * 
	 * This method constructs a new simulator for a given behavioral model.
	 * 
	 * @param e the semantic engine.
	 */
	public SimulatorImpl(SemanticEngine e) {
		this.semanticEngine = e;
		this.lts = new DirectedWeightedPseudograph<Configuration, LabelledEdge>(LabelledEdge.class);
		root = e.getInitConf();
		lts.addVertex(root);
		log = new EventLogImpl(e.getModelName(), root.getGlobalData());
		//LOGGER.log("Done");
	}

	@Override
	public EventLog simulate(Delta delta) {
		if (delta == null || delta.getMissings() == null || delta.getMissings().getTraces().isEmpty() ){//|| delta.equals(lastDelta)) {
			for (Map<String, Trace> p : randomSim().values()) {
				log.addTraces(p.values());
			}
			return log;
		}

		lastDelta = (Delta) DeepCopy.copy(delta);
		for (Trace d : delta.getMissings().getTraces()) {
			if(PURPLE.isInterrupted()) break;
			Set<Configuration> starts = find(d.get(0));
			if (starts.isEmpty()) {
				for (Map<String, Trace> p : randomSim().values()) {
					log.addTraces(p.values());
				}
			} else {
				d.getTrace().remove(0);
				for (Configuration s : starts) {
					Map<String, Map<String, Trace>> prefix = getPrefix(s);
					if(d.getTrace().isEmpty()){
						for (Map<String, Trace> p : finalizeSim(prefix, s).values()) {
							log.addTraces(p.values());
						}
					}else{
					for (Map<String, Trace> p : guidedSim(prefix, s, d).values()) {
						log.addTraces(p.values());
					}}
				}
			}
		}
		return log;
	}

	/**
	 * 
	 * 
	 * 
	 * @param prefix
	 * @param conf   The starting configuration.
	 * @param guide  The next events to find.
	 * @return
	 */
	private Map<String, Map<String, Trace>> guidedSim(Map<String, Map<String, Trace>> prefix, Configuration conf,
			Trace guide) {
		Couple<Event, Configuration> c = dfs(conf, guide.get(0));
		System.out.println("mmm"+guide);
		if (c != null) {
			Event event = c.getE();
			prefix.get(event.getProcess()).get(event.getInstance()).appendEvent(event);
			guide.remove(0);
			if (guide.getTrace().isEmpty()) {
				//return prefix;
				return finalizeSim(prefix, c.getV());
			}
			return guidedSim(prefix, c.getV(), guide);
		} else {
			return finalizeSim(prefix, conf);
		}


	}

	private Couple<Event, Configuration> dfs(Configuration start, Event toFind) {
		Set<Configuration> successors = new HashSet<Configuration>();
		Map<Configuration, Set<Event>> nexts = new HashMap<Configuration, Set<Event>>();
		List<Configuration> tmp = Graphs.successorListOf(lts, start);
		if (tmp.isEmpty()) {
			try {
				nexts = semanticEngine.getNexts(start);
			} catch (Exception e) {
				e.printStackTrace();
			}
			successors = nexts.keySet();
			for (Configuration n : successors) {
				lts.addVertex(n);
				for (Event ev : nexts.get(n)) {
					lts.addEdge(start, n, new LabelledEdge(ev));
				}
			}
		} else {
			successors.addAll(tmp);
			for (Configuration c : successors) {
				if (!nexts.containsKey(c)) {
					nexts.put(c, new HashSet<Event>());
				}
				nexts.get(c).add(lts.getEdge(start, c).getLabel());
			}
		}
		if (successors.isEmpty()) {// nothing found
			return null;
		}
		Set<Configuration> toTry = new HashSet<Configuration>();
		for (Configuration nextConf : nexts.keySet()) {
			for(Event nextEv : nexts.get(nextConf)) {
				if (LogUtil.compareEvents(nextEv, toFind)) {// Event found
					return new Couple<Event, Configuration>(nextEv, nextConf);
				} else if (nextEv.isEmptyEvent()) {// possible path
					toTry.add(nextConf);
				}
			}
		}
		for (Configuration c : toTry) {
			Couple<Event, Configuration> e = dfs(c, toFind);
			if (e != null) {
				return e;
			}
		}
		return null;
	}

	private Map<String, Map<String, Trace>> getPrefix(Configuration s) {
		return LTSUtil.getPrefix(lts, dataConfigurations, s);
	}

	private Map<String, Map<String, Trace>> randomSim() {
		Configuration dataInit = semanticEngine.initData(root);
		lts.addVertex(dataInit);
		lts.addEdge(root, dataInit, new LabelledEdge(EventImpl.emptyEvent()));
		dataConfigurations.add(dataInit);
		Configuration instancesInit = null;
		try {
			instancesInit = semanticEngine.initInstances(dataInit);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (Configuration c : lts.vertexSet()){
			if(c.equals(instancesInit)){
				instancesInit = c;
				break;
			}
		}
		lts.addVertex(instancesInit);
		lts.addEdge(dataInit, instancesInit, new LabelledEdge(EventImpl.emptyEvent()));
		return finalizeSim(getPrefix(instancesInit), instancesInit);
	}

	private Map<String, Map<String, Trace>> finalizeSim(Map<String, Map<String, Trace>> prefix, Configuration conf) {
		Map<Configuration, Set<Event>> nexts = null;
		try {
			nexts = semanticEngine.getNexts(conf);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (nexts.isEmpty()) {
			return prefix;
		}
		for (Configuration n : nexts.keySet()) {
			lts.addVertex(n);
			for (Event ev : nexts.get(n)) {
				lts.addEdge(conf, n, new LabelledEdge(ev));
			} 
		}
		List<Configuration> nextConf = new ArrayList<Configuration>(nexts.keySet());
		Collections.shuffle(nextConf);
		Configuration next = nextConf.iterator().next();
		Event e = nexts.get(next).iterator().next();
		if (!e.isEmptyEvent()) {
			prefix.get(e.getProcess()).get(e.getInstance()).appendEvent(e);
		}
		return finalizeSim(prefix, next);
	}

	public DirectedWeightedPseudograph<Configuration, LabelledEdge> getLts() {
		return lts;
	}

	private Set<Configuration> find(Event event) {
		return LTSUtil.findConf(lts, event);
	}

	@SuppressWarnings("serial")
	public class LabelledEdge extends DefaultEdge {

		private Event label;

		/**
		 * Constructs a relationship edge
		 *
		 * @param label the label of the new edge.
		 * 
		 */
		public LabelledEdge(Event label) {
			this.label = label;
		}

		/**
		 * Gets the label associated with this edge.
		 *
		 * @return edge label
		 */
		public Event getLabel() {
			return label;
		}

		@Override
		public String toString() {
			return "(" + getSource() + " : " + getTarget() + " : " + label + ")";
		}
	}
}