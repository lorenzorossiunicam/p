package org.processmining.plugins.transitionsystem.regions;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.Progress;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.events.Logger.MessageLevel;
import org.processmining.framework.util.search.MultiThreadedSearcher;
import org.processmining.framework.util.search.NodeExpander;
import org.processmining.models.connections.transitionsystem.GeneralizedExitationRegionConnection;
import org.processmining.models.connections.transitionsystem.MinimalRegionConnection;
import org.processmining.models.graphbased.directed.transitionsystem.State;
import org.processmining.models.graphbased.directed.transitionsystem.Transition;
import org.processmining.models.graphbased.directed.transitionsystem.TransitionSystem;
import org.processmining.models.graphbased.directed.transitionsystem.regions.GeneralizedExitationRegions;
import org.processmining.models.graphbased.directed.transitionsystem.regions.Region;
import org.processmining.models.graphbased.directed.transitionsystem.regions.RegionImpl;
import org.processmining.models.graphbased.directed.transitionsystem.regions.RegionSet;

public class RegionConstructor {

	//	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "B.F. van Dongen", email = "b.f.v.dongen@tue.nl")
	@Plugin(name = "GER Extractor", parameterLabels = "Transition System", returnTypes = GeneralizedExitationRegions.class, returnLabels = "GERs", userAccessible = true, help = "", mostSignificantResult = 1)
	public static GeneralizedExitationRegions getGeneralizedExcitationRegions(PluginContext context, TransitionSystem ts) {

		GeneralizedExitationRegions regions = new GeneralizedExitationRegions();

		Collection<? extends Object> identifiers = ts.getTransitions();

		context.getProgress().setMaximum(identifiers.size() + 1);
		context.getProgress().setMinimum(0);
		context.getProgress().setIndeterminate(false);

		for (Object identifier : identifiers) {
			context.getProgress().inc();

			Region ger = new RegionImpl();

			for (Transition t : ts.getEdges()) {
				if (t.getIdentifier().equals(identifier)) {
					ger.add(t.getSource());
				}
			}

			regions.put(identifier, ger);
		}
		context.getConnectionManager().addConnection(new GeneralizedExitationRegionConnection(ts, regions));
		context.getFutureResult(0).setLabel("GERs of " + ts.getLabel());

		return regions;
	}

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "B.F. van Dongen", email = "b.f.v.dongen@tue.nl")
	@Plugin(name = "Minimal Region Extractor", level = PluginLevel.Local, parameterLabels = "Transition System", returnTypes = RegionSet.class, returnLabels = "Minimal Regions", userAccessible = true, help = "", mostSignificantResult = 1)
	public static RegionSet constructMinimalRegions(PluginContext context, final TransitionSystem ts) throws Exception {

		GeneralizedExitationRegions gers = context.tryToFindOrConstructFirstObject(GeneralizedExitationRegions.class,
				GeneralizedExitationRegionConnection.class, GeneralizedExitationRegionConnection.TS, ts);

		context.getProgress().setMaximum(3);
		context.getProgress().setMinimum(0);
		context.getProgress().setIndeterminate(false);

		for (Transition t : ts.getEdges()) {
			if (t.getSource() == t.getTarget()) {
				throw new InvalidParameterException("The given transition system contains self-loops");
			}
		}

		Collection<Classification> resultCollection = new ArrayList<Classification>();

		RegionNodeExpander expander = new RegionNodeExpander(ts);
		Collection<Classification> classifications = new LinkedHashSet<Classification>();
		for (Map.Entry<Object, Region> ger : gers.entrySet()) {
			classifications.add(new Classification(new LinkedHashSet<State>(ger.getValue()), ts, ger.getKey()));
		}
		LinkedHashSet<State> endStates = new LinkedHashSet<State>();
		for (State s : ts.getNodes()) {
			if (ts.getOutEdges(s).isEmpty()) {
				endStates.add(s);
			}
		}
		classifications.add(new Classification(endStates, ts, (Object) null));

		MultiThreadedSearcher<Classification> searcher = new MultiThreadedSearcher<Classification>(expander,
				MultiThreadedSearcher.DEPTHFIRST);

		searcher.addInitialNodes(classifications);

		context.log("Starting search for minimal regions.", MessageLevel.DEBUG);
		searcher.startSearch(context.getExecutor(), context.getProgress(), resultCollection);
		context.getProgress().inc();

		RegionSet regions = new RegionSet();
		regions.addAll(resultCollection);

		context.getConnectionManager().addConnection(new MinimalRegionConnection(ts, regions));
		context.getFutureResult(0).setLabel("Minimal regions of " + ts.getLabel());

		return regions;
	}
}

class RegionNodeExpander implements NodeExpander<Classification> {

	private final TransitionSystem ts;

	public RegionNodeExpander(TransitionSystem ts) {
		this.ts = ts;
	}

	public Collection<Classification> expandNode(Classification cl, Progress progress,
			Collection<Classification> resultsSoFar) {

		synchronized (resultsSoFar) {
			for (Classification c : resultsSoFar) {
				if (c.isSmallerThan(cl)) {
					return Collections.emptySet();
				}
			}
		}

		cl.initialize(ts);

		if (cl.isValidRegion()) {
			// toExpand is a region!
			return Collections.emptySet();
		}

		if (!cl.isDis_ent_ex()) {
			// Since there are transitions which are both entering and exiting,
			// we need to make them internal.
			Set<Object> toMakeInternal = new LinkedHashSet<Object>(cl.getEntering());
			toMakeInternal.retainAll(cl.getExiting());
			for (Object identifier : toMakeInternal) {
				for (Transition t : ts.getEdges(identifier)) {
					if (cl.contains(t.getSource()) || cl.contains(t.getTarget())) {
						if (cl.excludeStates.contains(t.getSource()) || cl.excludeStates.contains(t.getTarget())) {
							// Cannot make this into a region.
							return Collections.emptySet();
						}
						cl.add(t.getSource());
						cl.add(t.getTarget());
					}
				}
			}
			assert (!toMakeInternal.isEmpty());
			//			System.out.println(cl.size() + "  1");
			return Collections.singleton(new Classification(new LinkedHashSet<State>(cl), cl.identifier,
					new LinkedHashSet<State>(cl.excludeStates)));
		}

		Collection<Classification> expanded = new LinkedHashSet<Classification>();

		if (!cl.isDis_ent_ext()) {
			LinkedHashSet<State> exclude = new LinkedHashSet<State>(cl.excludeStates);
			entering: for (Object identifier : cl.getEntering()) {
				if (cl.getExternal().contains(identifier) && !cl.getInternal().contains(identifier)) {
					// identifier is both entering and external, but not internal, hence
					LinkedHashSet<State> toConsider = new LinkedHashSet<State>(cl);
					LinkedHashSet<State> toBeExcluded = new LinkedHashSet<State>();
					// this getEntering() identifier is also not crossing
					for (Transition t : ts.getEdges(identifier)) {
						// check for all transitions referring to identifier
						if (!cl.contains(t.getTarget())) {
							// This transition has the target not in cl, hence add 
							// the target
							if (exclude.contains(t.getTarget())) {
								// Cannot make this into a region as there is a transition that
								// is getEntering() while it should be internal. However, the source
								// state has to be excluded in this potential region.
								continue entering;
							}
							toConsider.add(t.getTarget());
						}
						toBeExcluded.add(t.getTarget());
					}
					assert (!cl.equals(toConsider));
					Classification cl2 = new Classification(toConsider, cl.identifier, new LinkedHashSet<State>());
					cl2.excludeStates.addAll(exclude);
					expanded.add(cl2);
					exclude.addAll(toBeExcluded);
				}
			}
		}

		if (!cl.isDis_ex_ext()) {
			LinkedHashSet<State> exclude = new LinkedHashSet<State>(cl.excludeStates);
			exiting: for (Object identifier : cl.getExiting()) {
				if (cl.getExternal().contains(identifier) && !cl.getInternal().contains(identifier)) {
					// identifier is both exiting and external, but not internal, hence
					LinkedHashSet<State> toConsider = new LinkedHashSet<State>(cl);
					LinkedHashSet<State> toBeExcluded = new LinkedHashSet<State>();
					for (Transition t : ts.getEdges(identifier)) {
						// check for all transitions referring to identifier
						if (!cl.contains(t.getSource())) {
							// This transition has the source not in cl, hence add 
							// the source
							if (exclude.contains(t.getSource())) {
								// Cannot make this into a region as there is a transition that
								// is getEntering() while it should be internal. However, the source
								// state has to be excluded in this potential region.
								continue exiting;
							}
							toConsider.add(t.getSource());
						}
						toBeExcluded.add(t.getSource());
					}
					assert (!cl.equals(toConsider));
					Classification cl2 = new Classification(toConsider, cl.identifier, new LinkedHashSet<State>());
					cl2.excludeStates.addAll(exclude);
					expanded.add(cl2);
					exclude.addAll(toBeExcluded);
				}
			}
		}

		// For all transitions which are both entering and internal,
		// we need to add all source states of the getEntering() ones.
		if (!cl.isDis_ent_int() || !cl.isDis_ent_ext()) {
			LinkedHashSet<State> exclude = new LinkedHashSet<State>(cl.excludeStates);
			entering: for (Object identifier : cl.getEntering()) {
				if (cl.getInternal().contains(identifier) || cl.getExternal().contains(identifier)) {
					LinkedHashSet<State> toConsider = new LinkedHashSet<State>(cl);
					LinkedHashSet<State> toBeExcluded = new LinkedHashSet<State>();
					// this getEntering() identifier is also not crossing
					for (Transition t : ts.getEdges(identifier)) {
						// check for all transitions referring to identifier
						if (cl.contains(t.getTarget())) {
							// This transition has a target in cl.states, hence add 
							// the source
							if (exclude.contains(t.getSource())) {
								// Cannot make this into a region as there is a transition that
								// is getEntering() while it should be internal. However, the source
								// state has to be excluded in this potential region.
								continue entering;
							}
							toConsider.add(t.getSource());
						}
						toBeExcluded.add(t.getSource());
					}
					assert (!cl.equals(toConsider));
					Classification cl2 = new Classification(toConsider, cl.identifier, new LinkedHashSet<State>());
					cl2.excludeStates.addAll(exclude);
					expanded.add(cl2);
					exclude.addAll(toBeExcluded);
				}
			}
		}

		if (!cl.isDis_ex_int() || !cl.isDis_ex_ext()) {
			LinkedHashSet<State> exclude = new LinkedHashSet<State>(cl.excludeStates);
			// For all transitions which are both exiting and internal,
			// we need to add all source states of the getEntering() ones.
			exiting: for (Object identifier : cl.getExiting()) {
				if (cl.getInternal().contains(identifier) || cl.getExternal().contains(identifier)) {
					LinkedHashSet<State> toConsider = new LinkedHashSet<State>(cl);
					LinkedHashSet<State> toBeExcluded = new LinkedHashSet<State>();
					// this exiting identifier is also not crossing
					for (Transition t : ts.getEdges(identifier)) {
						// check for all transitions referring to identifier
						if (cl.contains(t.getSource())) {
							// This transition has a source in cl.states, hence add 
							// the target
							if (exclude.contains(t.getTarget())) {
								// Cannot make this into a region as there is a transition that
								// is exiting while it should be internal. However, the target
								// state has to be excluded
								continue exiting;
							}
							toConsider.add(t.getTarget());
						}
						toBeExcluded.add(t.getTarget());
					}
					assert (!cl.equals(toConsider));
					Classification cl2 = new Classification(toConsider, cl.identifier, new LinkedHashSet<State>());
					cl2.excludeStates.addAll(exclude);
					expanded.add(cl2);
					exclude.addAll(toBeExcluded);
				}
			}
		}
		return expanded;
	}

	public void processLeaf(Classification leaf, Progress progress, Collection<Classification> resultCollection) {
		synchronized (resultCollection) {
			if (!leaf.isEmpty() && leaf.isValidRegion()) {
				resultCollection.add(leaf);
				Iterator<Classification> it = resultCollection.iterator();
				while (it.hasNext()) {
					if (leaf.isSmallerThan(it.next())) {
						it.remove();
					}
				}
				resultCollection.add(leaf);
			}
		}
	}

}

class Classification extends RegionImpl {

	private static final long serialVersionUID = 8291926373563606233L;

	Object identifier;
	Set<State> excludeStates;

	public Classification(Set<State> states, TransitionSystem ts, Object identifier) {
		this(states, identifier, new LinkedHashSet<State>());
		if (identifier != null) {
			for (Transition t : ts.getEdges(identifier)) {
				excludeStates.add(t.getTarget());
			}
		}
		states.removeAll(excludeStates);
	}

	public boolean isSmallerThan(Classification cl) {
		return ((size() <= cl.size()) && cl.containsAll(this));
	}

	public Classification(Set<State> states, Object identifier, Set<State> excluded) {
		addAll(states);
		this.identifier = identifier;
		excludeStates = excluded;
	}

	public boolean equals(Object o) {
		if (o instanceof Classification) {
			Classification c = (Classification) o;
			return super.equals(c) && excludeStates.equals(c.excludeStates);
		}
		return false;
	}

}
