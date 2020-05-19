/*
 * Copyright (c) 2007 Christian W. Guenther (christian@deckfour.org)
 * 
 * LICENSE:
 * 
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA
 * 
 * EXEMPTION:
 * 
 * License to link and use is also granted to open source programs which are not
 * licensed under the terms of the GPL, given that they satisfy one or more of
 * the following conditions: 1) Explicit license is granted to the ProM and
 * ProMimport programs for usage, linking, and derivative work. 2) Carte blance
 * license is granted to all programs developed at Eindhoven Technical
 * University, The Netherlands, or under the umbrella of STW Technology
 * Foundation, The Netherlands. For further exemptions not covered by the above
 * conditions, please contact the author of this code.
 */
package org.processmining.plugins.fuzzymodel.miner.replay;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.models.graphbased.directed.fuzzymodel.FMClusterNode;
import org.processmining.models.graphbased.directed.fuzzymodel.FMNode;
import org.processmining.models.graphbased.directed.fuzzymodel.FuzzyGraph;
import org.processmining.models.graphbased.directed.fuzzymodel.util.FMLogEvents;


/**
 * @author christian
 * @author Jiafei Li (jiafei@jlu.edu.cn)
 * 
 */
public class TraceReplay {

	public enum MatchType {
		REMOVED, INCLUSTER, INVALID, VALID;
	}

	protected FuzzyGraph graph;
	protected XLog log;
	protected XTrace instance;
	protected double coverage;
	protected MatchType[] matches;
	protected int countRemoved = 0;
	protected int countIncluster = 0;
	protected int countIncorrect = 0;
	protected int countCorrect = 0;
	protected FMLogEvents events;

	public TraceReplay(FuzzyGraph graph, XLog log, FMLogEvents events, int traceIndex)
			throws IndexOutOfBoundsException, IOException {
		this.graph = graph;
		this.log = log;
		this.instance = log.get(traceIndex);
		this.coverage = 0.0;
		this.matches = new MatchType[instance.size()];
		this.events = events;
		coverage = replayTrace();
	}

	public double getCoverage() {
		return coverage;
	}

	public MatchType[] getMatches() {
		return matches;
	}

	public MatchType getMatch(int index) {
		return matches[index];
	}

	public int size() {
		return instance.size();
	}

	public XTrace getInstance() {
		return instance;
	}

	/**
	 * @throws IOException  
	 */
	public double replayTrace() throws IndexOutOfBoundsException, IOException {
		// initialize data structures
		// calculate result
		///AuditTrailEntryList ateList = instance.getAuditTrailEntryList();
		int eventSize = instance.size();
		//JF add start. to deal with the pattern based transformed log which has 0 events included in the process instance
		if(eventSize == 0){
			return 0;
		}
		//JF add end
		int deviations = 0;
		countRemoved = 0;
		countIncluster = 0;
		countIncorrect = 0;
		countCorrect = 0;
		HashMap<FMNode, Set<FMNode>> followers = new HashMap<FMNode, Set<FMNode>>();
		HashMap<FMNode, Set<FMNode>> possibleFollowers = new HashMap<FMNode, Set<FMNode>>();
		for (int i = 0; i < graph.getNumberOfInitialNodes(); i++) {
			FMNode node = graph.getNodeMappedTo(i);
			if (node != null && followers.keySet().contains(node) == false) {
				followers.put(node, node.getSuccessors());
				possibleFollowers.put(node, new HashSet<FMNode>());
			}
		}
		//LogEvents events = log.getLogSummary().getLogEvents();
		//FMLogEvents events = FuzzyMinerLog.getLogEvents(log);

		int initialIndex = 0;
		int lastIndex = 0;
		FMNode lastNode = null;
		XEvent lastAte = null;
		// find first, valid event (mapped to valid node)
		while (lastNode == null) {
			lastAte = instance.get(initialIndex);
			lastIndex = events.findLogEventNumber(lastAte);
			if (lastIndex < 0) //remove by editor 
			{
				matches[initialIndex] = MatchType.REMOVED;
				deviations++;
				countRemoved++;
			} else {
				lastNode = graph.getNodeMappedTo(lastIndex);
				if (lastNode != null) {
					matches[initialIndex] = MatchType.VALID;
				} else {//remove by abstract
					matches[initialIndex] = MatchType.REMOVED;
					deviations++;
					countRemoved++;
				}
			}
			initialIndex++;
			if (initialIndex >= eventSize) {
				// no valid events in this trace, return
				return (double) (eventSize - deviations + 1) / (double) (eventSize + 1);
			}
		}
		// check first event whether it corresponds to a 'real' start node,
		// i.e. if there are no incoming arcs.
		if (lastNode.getPredecessors().size() > 0) {
			deviations++; // no real start node, punish.
			matches[initialIndex - 1] = MatchType.INVALID;
			countRemoved++;
		}
		// update node activation counter
		possibleFollowers.put(lastNode, new HashSet<FMNode>(followers.get(lastNode)));
		XEvent currentAte = null;
		int currentIndex;
		FMNode currentNode = null;
		// replay
		for (int i = 1; i < eventSize; i++) {
			currentAte = instance.get(i);
			currentIndex = events.findLogEventNumber(currentAte);
			//if (currentIndex >=0){
			if (currentIndex < 0) { //removed by editor
				deviations++;
				matches[i] = MatchType.REMOVED;
				countRemoved++;
				continue;
			} else {
				currentNode = graph.getNodeMappedTo(currentIndex);
				// check for valid node
				if (currentNode == null) {
					// node had been removed, punish
					deviations++;
					matches[i] = MatchType.REMOVED;
					countRemoved++;
					continue;
				}
				// analyze state transition
				else if (currentNode.equals(lastNode)) {
					if (currentNode instanceof FMClusterNode) {
						// transition within one cluster, valid.
						// no need to change state
						matches[i] = MatchType.INCLUSTER;
						countIncluster++;
					} else {
						// the same node in succession, check if valid
						if (graph.getBinarySignificance(currentNode.getIndex(), currentNode.getIndex()) > 0.0) {
							// valid succession, graph has self-loop for node
							// no need to change state
							matches[i] = MatchType.VALID;
							countCorrect++;
						} else {
							// invalid succession, no self-loop for node
							deviations++;
							matches[i] = MatchType.INVALID;
							countIncorrect++;
						}
					}
				} else if (checkNode(currentNode, possibleFollowers) == false) {
					// invalid transition
					deviations++;
					matches[i] = MatchType.INVALID;
					countIncorrect++;
				} else {
					// valid transition;
					// update activation for current node
					possibleFollowers.put(currentNode, new HashSet<FMNode>(followers.get(currentNode)));
					matches[i] = MatchType.VALID;
					countCorrect++;
				}
			}
			lastNode = currentNode;
			lastAte = currentAte;
			lastIndex = currentIndex;
		}
		// calculate result
		return (double) (eventSize - deviations + 1) / (double) (eventSize + 1);
	}

	protected boolean checkNode(FMNode current, HashMap<FMNode, Set<FMNode>> possibleFollowers) {
		boolean isValid = false;
		// check all active nodes if they are valid predecessors
		for (FMNode node : possibleFollowers.keySet()) {
			if (possibleFollowers.get(node).contains(current)) {
				// valid possible predecessor found, remove from set
				possibleFollowers.get(node).remove(current);
				isValid = true;
			}
		}
		return isValid;
	}
}
