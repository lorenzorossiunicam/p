package org.processmining.plugins.transitionsystem.regions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.Progress;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.plugin.events.Logger.MessageLevel;
import org.processmining.framework.util.Pair;
import org.processmining.framework.util.search.MultiThreadedSearcher;
import org.processmining.framework.util.search.NodeExpander;
import org.processmining.models.connections.petrinets.behavioral.InitialMarkingConnection;
import org.processmining.models.connections.transitionsystem.GeneralizedExitationRegionConnection;
import org.processmining.models.connections.transitionsystem.MinimalRegionConnection;
import org.processmining.models.connections.transitionsystem.RegionConnection;
import org.processmining.models.connections.transitionsystem.TransitionSystemConnection;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.graphbased.directed.transitionsystem.AcceptStateSet;
import org.processmining.models.graphbased.directed.transitionsystem.StartStateSet;
import org.processmining.models.graphbased.directed.transitionsystem.State;
import org.processmining.models.graphbased.directed.transitionsystem.TransitionSystem;
import org.processmining.models.graphbased.directed.transitionsystem.TransitionSystemFactory;
import org.processmining.models.graphbased.directed.transitionsystem.regions.GeneralizedExitationRegions;
import org.processmining.models.graphbased.directed.transitionsystem.regions.Region;
import org.processmining.models.graphbased.directed.transitionsystem.regions.RegionImpl;
import org.processmining.models.graphbased.directed.transitionsystem.regions.RegionSet;
import org.processmining.models.semantics.petrinet.Marking;

@Plugin(name = "Transition System to Petrinet", level = PluginLevel.PeerReviewed, parameterLabels = "Transition System", returnTypes = {
		Petrinet.class, Marking.class }, returnLabels = { "Petrinet", "Marking" }, userAccessible = true, help = "", mostSignificantResult = 1)
public class TransitionSystem2Petrinet {

	@UITopiaVariant(uiLabel = "Convert to Petri Net using Regions", affiliation = UITopiaVariant.EHV, author = "B.F. van Dongen", email = "b.f.v.dongen@tue.nl")
	@PluginVariant(variantLabel = "Convert to Petri Net using Regions", requiredParameterLabels = { 0 })
	public Object[] convertToPetrinet(PluginContext context, TransitionSystem ts) throws ConnectionCannotBeObtained,
			InterruptedException, ExecutionException {

		StartStateSet initial = context.tryToFindOrConstructFirstObject(StartStateSet.class,
				TransitionSystemConnection.class, TransitionSystemConnection.STARTIDS, ts);

		AcceptStateSet accept = context.tryToFindOrConstructFirstObject(AcceptStateSet.class,
				TransitionSystemConnection.class, TransitionSystemConnection.ACCEPTIDS, ts);

		List<org.processmining.models.graphbased.directed.transitionsystem.Transition> selfLoops = new ArrayList<org.processmining.models.graphbased.directed.transitionsystem.Transition>();
		for (org.processmining.models.graphbased.directed.transitionsystem.Transition t : ts.getEdges()) {
			if (t.getSource() == t.getTarget()) {
				selfLoops.add(t);
			}
		}
		Map<Object, Object> toFuse = new HashMap<Object, Object>();
		if (!selfLoops.isEmpty()) {
			TransitionSystem newTS = TransitionSystemFactory.newTransitionSystem(ts.getLabel());

			for (State s : ts.getNodes()) {
				newTS.addState(s.getIdentifier());
			}

			int i = 0;
			for (org.processmining.models.graphbased.directed.transitionsystem.Transition t : ts.getEdges()) {
				if (t.getSource() == t.getTarget()) {
					final int newState = i++;
					Object endIdentifier = new Object() {
						public String toString() {
							return "fresh " + newState;
						}
					};
					newTS.addState(endIdentifier);
					SplitObject sl1 = new SplitObject(t.getIdentifier(), SplitObject.SL1);
					newTS.addTransition(t.getSource().getIdentifier(), endIdentifier, sl1);
					SplitObject sl2 = new SplitObject(t.getIdentifier(), SplitObject.SL2);
					newTS.addTransition(endIdentifier, t.getTarget().getIdentifier(), sl2);

					toFuse.put(sl1, sl2);
				} else {
					newTS
							.addTransition(t.getSource().getIdentifier(), t.getTarget().getIdentifier(), t
									.getIdentifier());
				}
			}

			context.getProvidedObjectManager().createProvidedObject(newTS.getLabel(), newTS, context);
			context.getProvidedObjectManager().createProvidedObject("Initial states of " + ts.getLabel(), initial,
					context);
			context.getProvidedObjectManager().createProvidedObject("Accept states of " + ts.getLabel(), accept,
					context);
			context.getConnectionManager().addConnection(new TransitionSystemConnection(newTS, initial, accept));

			ts = newTS;
		}

		context.log("Starting conversion of transition system to Petri net.");
		Pair<Petrinet, Marking> result = convertToPetrinet(context, ts.getLabel(), ts, initial, accept, 1, toFuse);
		if (result == null) {
			return null;
		}
		return new Object[] { result.getFirst(), result.getSecond() };

	}

	private Pair<Petrinet, Marking> convertToPetrinet(PluginContext context, String label, TransitionSystem ts,
			StartStateSet initial, AcceptStateSet accept, int split, Map<Object, Object> toFuse)
			throws ConnectionCannotBeObtained, InterruptedException, ExecutionException {
		

		RegionSet regions = context.tryToFindOrConstructFirstObject(RegionSet.class, MinimalRegionConnection.class,
				RegionConnection.REGIONS, ts);
		context.log("Minimal Regions identified.", MessageLevel.DEBUG);

		if (context.getProgress().isCancelled()) {
			return null;
		}
		
		GeneralizedExitationRegions gers = context.tryToFindOrConstructFirstObject(GeneralizedExitationRegions.class,
				GeneralizedExitationRegionConnection.class, GeneralizedExitationRegionConnection.GERS, ts);

		if (context.getProgress().isCancelled()) {
			return null;
		}
		
		MisMatch toResolve = checkForwardClosure(ts, gers, regions, context);

		if (context.getProgress().isCancelled()) {
			return null;
		}

		if (toResolve != null) {

			context.log("This transition system does not meet the forward-closure property.", MessageLevel.DEBUG);
			context.log("    Splitting transitions: " + toResolve.getToSplit(), MessageLevel.DEBUG);
			//			context.log("    Making a region of: " + toResolve.getStates(), MessageLevel.DEBUG);

			TransitionSystem newTS = TransitionSystemFactory.newTransitionSystem(label + " Split: " + split);

			for (State s : ts.getNodes()) {
				newTS.addState(s.getIdentifier());
				if (context.getProgress().isCancelled()) {
					return null;
				}
			}

			for (org.processmining.models.graphbased.directed.transitionsystem.Transition transition : ts.getEdges()) {
				if (context.getProgress().isCancelled()) {
					return null;
				}
				Object identifier;
				identifier = transition.getIdentifier();
				if (toResolve.getToSplit().contains(identifier)) {

					boolean sc = toResolve.getStates().contains(transition.getSource());
					boolean tc = toResolve.getStates().contains(transition.getTarget());

					SplitObject newIdentifier;
					if (sc == tc) {
						// not crossing
						newIdentifier = new SplitObject(identifier, SplitObject.NOTCROSS);
					} else if (sc) {
						newIdentifier = new SplitObject(identifier, SplitObject.EXIT);
					} else {
						newIdentifier = new SplitObject(identifier, SplitObject.ENTER);
					}
					// Check if identifier is part of a toFuse map.
					Iterator<Map.Entry<Object, Object>> it = toFuse.entrySet().iterator();
					while (it.hasNext()) {
						Entry<Object, Object> entry = it.next();
						if (entry.getKey().equals(identifier)) {
							// add the new entry, 
							toFuse.put(newIdentifier, new SplitObject(entry.getValue(), newIdentifier.getOpposed()));
							break;
						} else if (entry.getValue().equals(identifier)) {
							// add the new entry, 
							toFuse.put(new SplitObject(entry.getKey(), newIdentifier.getOpposed()), newIdentifier);
							break;
						}
					}
					identifier = newIdentifier;
				}
				newTS.addTransition(transition.getSource().getIdentifier(), transition.getTarget().getIdentifier(),
						identifier);
			}
			toFuse.keySet().removeAll(toResolve.getToSplit());
			context.getProvidedObjectManager().createProvidedObject(newTS.getLabel(), newTS, context);
			context.getProvidedObjectManager().createProvidedObject("Initial states of " + ts.getLabel(), initial,
					context);
			context.getProvidedObjectManager().createProvidedObject("Accept states of " + ts.getLabel(), accept,
					context);
			context.getConnectionManager().addConnection(new TransitionSystemConnection(newTS, initial, accept));
			ts = null;
			return convertToPetrinet(context, label, newTS, initial, accept, split + 1, toFuse);
		}

		context.log("This transition system satifies the forward-closure property.", MessageLevel.DEBUG);

		Petrinet pnet = PetrinetFactory.newPetrinet(ts.getLabel());
		Marking marking = new Marking();

		// Add transitions
		Map<Object, Transition> id2trans = new HashMap<Object, Transition>();

		Set<Object> identifiers = new HashSet<Object>(ts.getTransitions());

		// First handle the self loops.
		for (Map.Entry<Object, Object> fuse : toFuse.entrySet()) {
			Transition t = pnet.addTransition(fuse.getKey().toString());
			id2trans.put(fuse.getKey(), t);
			identifiers.remove(fuse.getKey());
			id2trans.put(fuse.getValue(), t);
			identifiers.remove(fuse.getValue());
		}

		for (Object identifier : identifiers) {

			id2trans.put(identifier, pnet.addTransition(identifier.toString()));
		}

		// Add Places
		Set<Place> toRemove = new HashSet<Place>();
		Map<Region, Place> reg2place = new HashMap<Region, Place>();
		for (Region r : regions) {
			Place p = pnet.addPlace(r.toString());
			reg2place.put(r, p);
			// now check for initial state

			for (State s : r) {
				if (initial.contains(s.getIdentifier())) {
					marking.add(p);
				}
			}

			for (Object identifier : ts.getTransitions()) {
				org.processmining.models.graphbased.directed.transitionsystem.Transition t = ts.getEdges(identifier)
						.iterator().next();
				if (!r.contains(t.getSource()) && r.contains(t.getTarget())) {
					// entering
					if (toFuse.containsKey(identifier)) {
						toRemove.add(p);
					} else {
						pnet.addArc(id2trans.get(identifier), p);
					}
				} else if (r.contains(t.getSource()) && !r.contains(t.getTarget())) {
					// exiting
					if (toFuse.containsValue(identifier)) {
						toRemove.add(p);
					} else {
						pnet.addArc(p, id2trans.get(identifier));
					}
				}
			}

		}
		for (Place p : toRemove) {
			pnet.removePlace(p);
		}

		context.getConnectionManager().addConnection(new InitialMarkingConnection(pnet, marking));

		context.getFutureResult(0).setLabel("Petrinet Synthesized from " + label);
		context.getFutureResult(1).setLabel("Initial marking of " + context.getFutureResult(0).getLabel());

		return new Pair<Petrinet, Marking>(pnet, marking);
	}

	@SuppressWarnings("unchecked")
	private MisMatch checkForwardClosure(TransitionSystem ts, GeneralizedExitationRegions gers, RegionSet regions,
			PluginContext context) throws InterruptedException, ExecutionException {

		MisMatch mismatchFound = null;
		for (Object identifier : ts.getTransitions()) {
			// get the ger
			Set<State> ger = new LinkedHashSet<State>(gers.get(identifier));

			// construct the intersection of all pre-regions.
			Set<State> inter = null;
			List<Region> pre = new ArrayList<Region>();
			for (Region r : regions) {
				if (r.getExiting().contains(identifier)) {
					pre.add(r);
					if (inter == null) {
						inter = new LinkedHashSet<State>(r);
					} else {
						inter.retainAll(r);
					}
				}
			}
			if (ger.equals(inter)) {
				// intersection equals ger
				continue;
			}
			if (inter == null) {
				inter = new LinkedHashSet<State>();
			}
			context.log("For transition " + identifier
					+ " the GER is not equal to the intersection of pre-regions: ger size=" + ger.size()
					+ " intersection size=" + inter.size(), MessageLevel.DEBUG);

			// check all proper subsets of inter that include ger
			int maxSize = inter.size() - 1;
			inter.removeAll(ger);

			MisMatch mismatch = new MisMatch(new LinkedHashSet<State>(ger), ts);
			if ((mismatchFound == null) || (mismatch.compareTo(mismatchFound) < 0)) {
				mismatchFound = mismatch;
			}

			//			extendMisMatches(ger, new ArrayList<State>(inter), 0, maxSize, mismatchFound, ts);

			MisMatchExpander expander = new MisMatchExpander(maxSize, new ArrayList<State>(inter), ts, mismatchFound);
			MultiThreadedSearcher<Pair<Set<State>, Integer>> searcher = new MultiThreadedSearcher<Pair<Set<State>, Integer>>(
					expander, MultiThreadedSearcher.BREADTHFIRST);
			searcher.addInitialNodes(new Pair<Set<State>, Integer>(mismatchFound.getStates(), 0));
			searcher.startSearch(context.getExecutor(), context.getProgress(), Collections
					.<Pair<Set<State>, Integer>>emptyList());

			mismatchFound = expander.getSelectedMisMatch();

			context.log("All proper subsets of the intersection of the pre-regions of " + identifier
					+ " have been investigated", MessageLevel.DEBUG);

		}

		return mismatchFound;
	}

}

class MisMatch implements Comparable<MisMatch> {

	private final Set<State> states;

	private final LinkedHashSet<Object> toSplit;

	private int splitCount;

	public MisMatch(Set<State> states, TransitionSystem ts) {
		this.states = states;

		Region r = new RegionImpl();
		r.addAll(states);

		r.initialize(ts);

		toSplit = new LinkedHashSet<Object>();
		splitCount = 0;
		for (Object id : ts.getTransitions()) {
			boolean entering = r.getEntering().contains(id);
			boolean exiting = r.getExiting().contains(id);
			boolean notCross = r.getInternal().contains(id) || r.getExternal().contains(id);

			boolean split = false;
			if (entering && exiting && notCross) {
				splitCount += 3;
				split = true;
			} else if (entering && exiting) {
				splitCount += 2;
				split = true;
			} else if (entering && notCross) {
				splitCount += 2;
				split = true;
			} else if (exiting && notCross) {
				splitCount += 2;
				split = true;
			}
			if (split) {
				getToSplit().add(id);
			}

		}
	}

	public Set<State> getStates() {
		return states;
	}

	public Set<Object> getToSplit() {
		return toSplit;
	}

	public boolean equals(Object o) {
		if (o instanceof MisMatch) {
			return (((MisMatch) o).states.equals(states));
		} else {
			return false;
		}
	}

	public int compareTo(MisMatch m) {
		if (toSplit.size() != m.toSplit.size()) {
			return toSplit.size() - m.toSplit.size();
		}

		if (states.size() != m.states.size()) {
			return states.size() - m.states.size();
		}

		if (splitCount != m.splitCount) {
			return splitCount - m.splitCount;
		}

		if (equals(m)) {
			return 0;
		}

		return m.hashCode() - hashCode();
	}

	public String toString() {
		return "states = {" + states.toString() + "}";
	}
}

class SplitObject extends Pair<Object, String> {

	public final static String ENTER = " [En]";
	public final static String EXIT = " [Ex]";
	public final static String NOTCROSS = " [NC]";
	public final static String SL1 = " [SL1]";
	public final static String SL2 = " [SL2]";
	private final Object parent;

	public SplitObject(Object parent, String type) {
		super(parent, type);
		if (parent instanceof SplitObject) {
			this.parent = ((SplitObject) parent).getParent();
		} else {
			this.parent = parent;
		}
	}

	public Object getParent() {
		return parent;
	}

	public String toString() {
		return getParent().toString();
	}

	public String getOpposed() {
		if (getSecond() == ENTER) {
			return EXIT;
		}
		if (getSecond() == EXIT) {
			return ENTER;
		}
		if (getSecond() == NOTCROSS) {
			return NOTCROSS;
		}
		return "";
	}
}

class MisMatchExpander implements NodeExpander<Pair<Set<State>, Integer>> {

	private MisMatch mismatchFound;
	private final int maxSize;
	private final List<State> from;
	private final TransitionSystem ts;

	public MisMatchExpander(int maxSize, List<State> from, TransitionSystem ts, MisMatch mismatchFound) {
		this.maxSize = maxSize;
		this.from = from;
		this.ts = ts;
		this.mismatchFound = mismatchFound;
	}

	public Collection<Pair<Set<State>, Integer>> expandNode(Pair<Set<State>, Integer> toExpand, Progress progress,
			Collection<Pair<Set<State>, Integer>> unmodifiableResultCollection) {

		Collection<Pair<Set<State>, Integer>> toExpandFurther = new ArrayList<Pair<Set<State>, Integer>>();

		Set<State> baseSet = toExpand.getFirst();
		int index = toExpand.getSecond();

		if (baseSet.size() >= maxSize) {
			return toExpandFurther;
		}

		State s = from.get(index);
		if (!baseSet.contains(s)) {
			LinkedHashSet<State> newSet = new LinkedHashSet<State>(baseSet);
			newSet.add(s);
			MisMatch mismatch = new MisMatch(newSet, ts);
			synchronized (mismatchFound) {
				if (mismatch.compareTo(mismatchFound) < 0) {
					mismatchFound = mismatch;
				}
			}
			if (index < from.size() - 1) {
				toExpandFurther.add(new Pair<Set<State>, Integer>(newSet, index + 1));
			}
		}
		if (index < from.size() - 1) {
			toExpandFurther.add(new Pair<Set<State>, Integer>(baseSet, index + 1));
		}

		return toExpandFurther;
	}

	public void processLeaf(Pair<Set<State>, Integer> leaf, Progress progress,
			Collection<Pair<Set<State>, Integer>> resultCollection) {
		// do nothing.
	}

	public MisMatch getSelectedMisMatch() {
		return mismatchFound;
	}
}