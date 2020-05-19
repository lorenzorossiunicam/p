/**
 * 
 */
package org.processmining.plugins.performancemeasurement.logreplayers.nodeidentifiers;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.processmining.framework.util.Pair;
import org.processmining.models.fuzzyperformancediagram.FPD;
import org.processmining.models.fuzzyperformancediagram.FPDEdge;
import org.processmining.models.fuzzyperformancediagram.FPDNode;
import org.processmining.models.graphbased.directed.utils.Node;

/**
 * Class to identify FPD node instances based on traces of event class
 * 
 * @author arya
 * @email arya.adriansyah@gmail.com
 * @version Apr 8, 2009
 */
public class FuzzyFPDNodeLogIdentifier implements IFPDNodeLogIdentifier {

	public int stateSpaceLimit = 5000; // maximum number of state that considered to be enough to decide randomly

	public FuzzyFPDNodeLogIdentifier(int stateSpaceNumberLimit) {
		// state space limit
		stateSpaceLimit = stateSpaceNumberLimit;
	}

	/**
	 * method to identify FPD nodes based on available mappings, FPD, and number
	 * of state space with default state space number limit
	 */
	public List<FPDNode> identifyFPDNodes(Queue<Set<FPDNode>> queueOfNodeMappings, FPD fpd) {
		return identifyFPDNodes(queueOfNodeMappings, fpd, stateSpaceLimit);
	}

	/**
	 * complete method to identify FPD nodes based on available mappings, FPD,
	 * and number of state space
	 * 
	 * @param queueOfNodeMappings
	 * @param fpd
	 * @param stateSpaceNumberLimit
	 * @return
	 */
	public List<FPDNode> identifyFPDNodes(Queue<Set<FPDNode>> queueOfNodeMappings, FPD fpd, int stateSpaceNumberLimit) {
		List<FPDNode> result = new LinkedList<FPDNode>(); // result of this method
		Set<FPDNode> enabledFPDNode = new HashSet<FPDNode>(); // enabled SPD nodes
		Set<FPDNode> hotNodes = new HashSet<FPDNode>(); // hot nodes  

		Set<FPDNode> candidateNodes = null;
		queueOfNodeMappings: while (queueOfNodeMappings.size() > 0) {
			if (candidateNodes == null) {
				candidateNodes = queueOfNodeMappings.remove();
			} // else, there is still something left

			if (candidateNodes.size() == 0) {
				//System.out.println("candidate nodes size == 0");
				result.add(null);
				candidateNodes = null;
			} else if (candidateNodes.size() == 1) {
				//System.out.println("candidate nodes size == 1");
				FPDNode nextToBeExpandedNode = candidateNodes.iterator().next();
				result.add(nextToBeExpandedNode);

				// remove enabled nodes/hot nodes whose successor are all hot nodes
				FPDNode tobeDeleted = null;
				inEdgeChecking: for (FPDEdge<? extends FPDNode, ? extends FPDNode> inEdge : fpd
						.getInEdges(nextToBeExpandedNode)) {
					// init to be deleted
					tobeDeleted = null;

					// if there is an element whose all successors are "hot nodes", remove it from tempEnabled
					for (FPDEdge<? extends FPDNode, ? extends FPDNode> outEdge : fpd.getOutEdges(inEdge.getSource())) {
						if (!hotNodes.contains(outEdge.getSource())) {
							continue inEdgeChecking;
						} else {
							tobeDeleted = outEdge.getSource();
						}
					}
					enabledFPDNode.remove(tobeDeleted);
					hotNodes.remove(tobeDeleted);
				}

				hotNodes.add(nextToBeExpandedNode); // need to be added twice in case it is removed previously 

				// add all successor of nextToBeExpandedNode
				for (FPDEdge<? extends FPDNode, ? extends FPDNode> outEdge : fpd.getOutEdges(nextToBeExpandedNode)) {
					enabledFPDNode.add(outEdge.getTarget());
				}
				;

				candidateNodes = null;
				continue queueOfNodeMappings;
			} else if (candidateNodes.size() > 1) {
				//System.out.println("candidate nodes size > 1");
				// try to filter out nodes which is not belong to either hot nodes or enabled nodes
				Set<FPDNode> tempHotAndEnabledNodes = new HashSet<FPDNode>();
				tempHotAndEnabledNodes.addAll(hotNodes);
				tempHotAndEnabledNodes.addAll(enabledFPDNode);
				tempHotAndEnabledNodes.retainAll(candidateNodes);
				if (tempHotAndEnabledNodes.size() == 1) {
					FPDNode nextToBeExpandedNode = tempHotAndEnabledNodes.iterator().next();
					result.add(nextToBeExpandedNode);

					hotNodes.add(nextToBeExpandedNode);

					// remove enabled nodes/hot nodes whose successor are all hot nodes
					FPDNode tobeDeleted = null;
					inEdgeChecking: for (FPDEdge<? extends FPDNode, ? extends FPDNode> inEdge : fpd
							.getInEdges(nextToBeExpandedNode)) {
						// init to be deleted
						tobeDeleted = null;

						// if there is an element whose all successors are "hot nodes", remove it from tempEnabled
						for (FPDEdge<? extends FPDNode, ? extends FPDNode> outEdge : fpd
								.getOutEdges(inEdge.getSource())) {
							if (!hotNodes.contains(outEdge.getSource())) {
								continue inEdgeChecking;
							} else {
								tobeDeleted = outEdge.getSource();
							}
						}
						enabledFPDNode.remove(tobeDeleted);
						hotNodes.remove(tobeDeleted);
					}

					hotNodes.add(nextToBeExpandedNode); // need to be added twice in case it is removed previously 

					// add all successor of nextToBeExpandedNode
					for (FPDEdge<? extends FPDNode, ? extends FPDNode> outEdge : fpd.getOutEdges(nextToBeExpandedNode)) {
						enabledFPDNode.add(outEdge.getTarget());
					}
					;
					candidateNodes = null;
					continue queueOfNodeMappings;
				} else if (tempHotAndEnabledNodes.size() > 1) {
					candidateNodes.clear();
					candidateNodes.addAll(tempHotAndEnabledNodes);
				}

				// create treeNodes in toBeExpanded to filter out candidate nodes
				Queue<Node<Pair<Pair<FPDNode, Pair<Integer, Boolean>>, Pair<Set<FPDNode>, Set<FPDNode>>>>> toBeExpanded = new LinkedList<Node<Pair<Pair<FPDNode, Pair<Integer, Boolean>>, Pair<Set<FPDNode>, Set<FPDNode>>>>>();

				for (FPDNode candidateNode : candidateNodes) {
					// check whether the node has a connection to hot nodes
					boolean connectedToHotNode = false;
					for (FPDNode hotNode : hotNodes) {
						if (fpd.getArc(hotNode, candidateNode) != null) {
							connectedToHotNode = true;
							break;
						}
					}

					// extends enabledFPDNode and hotNodes for each candidateNode
					// create tempEnabled Nodes
					Set<FPDNode> tempEnabled = new HashSet<FPDNode>();
					tempEnabled.addAll(enabledFPDNode);

					// create tempHotNodes 
					Set<FPDNode> tempHotNodes = new HashSet<FPDNode>();
					tempHotNodes.addAll(hotNodes);
					tempHotNodes.add(candidateNode);

					// update enabled nodes whose successor are all hot nodes
					FPDNode tobeDeleted = null;
					Set<FPDNode> setToBeDeleted = new HashSet<FPDNode>();
					inEdgeChecking: for (FPDEdge<? extends FPDNode, ? extends FPDNode> inEdge : fpd
							.getInEdges(candidateNode)) {
						// init to be deleted
						tobeDeleted = null;

						// if there is an element whose all successors are "hot nodes", remove it from tempEnabled
						for (FPDEdge<? extends FPDNode, ? extends FPDNode> outEdge : fpd
								.getOutEdges(inEdge.getSource())) {
							if (!hotNodes.contains(outEdge.getTarget())) {
								continue inEdgeChecking;
							} else {
								tobeDeleted = outEdge.getSource();
							}
						}
						setToBeDeleted.add(tobeDeleted);
						tempEnabled.remove(tobeDeleted);
					}

					// update hot nodes
					tobeDeleted = null;
					Set<FPDNode> unionSetToBeDeletedHotNodes = new HashSet<FPDNode>();
					unionSetToBeDeletedHotNodes.addAll(hotNodes);
					unionSetToBeDeletedHotNodes.addAll(setToBeDeleted);

					// remove hot nodes whose successor are all hot nodes (union with removed nodes)
					checkNode: for (FPDNode hotNodeToBeDeleted : hotNodes) {
						// init to be deleted
						tobeDeleted = null;
						for (FPDEdge<? extends FPDNode, ? extends FPDNode> outEdge : fpd
								.getOutEdges(hotNodeToBeDeleted)) {
							// if there is an element whose all successors are "hot nodes" or setToBeDeleted, remove it from tempHotNodes
							if (!unionSetToBeDeletedHotNodes.contains(outEdge.getTarget())) {
								continue checkNode;
							} else {
								tobeDeleted = outEdge.getTarget();
							}
						}
						tempHotNodes.remove(tobeDeleted);
					}

					tempHotNodes.add(candidateNode); // need to be added twice in case it is removed previously 

					// add all successor of nextToBeExpandedNode
					for (FPDEdge<? extends FPDNode, ? extends FPDNode> outEdge : fpd.getOutEdges(candidateNode)) {
						tempEnabled.add(outEdge.getTarget());
					}
					;
					Pair<Set<FPDNode>, Set<FPDNode>> setPair = new Pair<Set<FPDNode>, Set<FPDNode>>(tempEnabled,
							tempHotNodes);

					// create treenode
					Pair<Integer, Boolean> intBooleanPair = new Pair<Integer, Boolean>(candidateNode
							.getAggregatedEventClassFreq(), connectedToHotNode);
					Pair<FPDNode, Pair<Integer, Boolean>> nodeBooleanPair = new Pair<FPDNode, Pair<Integer, Boolean>>(
							candidateNode, intBooleanPair);
					Pair<Pair<FPDNode, Pair<Integer, Boolean>>, Pair<Set<FPDNode>, Set<FPDNode>>> newPair = new Pair<Pair<FPDNode, Pair<Integer, Boolean>>, Pair<Set<FPDNode>, Set<FPDNode>>>(
							nodeBooleanPair, setPair);
					Node<Pair<Pair<FPDNode, Pair<Integer, Boolean>>, Pair<Set<FPDNode>, Set<FPDNode>>>> candidateTreenode = new Node<Pair<Pair<FPDNode, Pair<Integer, Boolean>>, Pair<Set<FPDNode>, Set<FPDNode>>>>(
							newPair);
					toBeExpanded.add(candidateTreenode);
				}

				// to store previously expanded candidateTreenode
				Queue<Node<Pair<Pair<FPDNode, Pair<Integer, Boolean>>, Pair<Set<FPDNode>, Set<FPDNode>>>>> prevToBeExpanded = new LinkedList<Node<Pair<Pair<FPDNode, Pair<Integer, Boolean>>, Pair<Set<FPDNode>, Set<FPDNode>>>>>();

				/**
				 * BATCH OF TREENODE PROCESS
				 */
				// Initialization of candidate treenodes is done, next is to process them
				//int tobeDeleted = 0;
				Set<Node<Pair<Pair<FPDNode, Pair<Integer, Boolean>>, Pair<Set<FPDNode>, Set<FPDNode>>>>> alreadyExpandedNodes = new HashSet<Node<Pair<Pair<FPDNode, Pair<Integer, Boolean>>, Pair<Set<FPDNode>, Set<FPDNode>>>>>();

				while ((toBeExpanded.size() > 1) && (queueOfNodeMappings.size() > 0)
						&& (toBeExpanded.size() < stateSpaceNumberLimit)) {
					prevToBeExpanded.clear();

					// get next candidate node
					Set<FPDNode> nextCandidateNodes = queueOfNodeMappings.remove();

					int expansionCounter = toBeExpanded.size(); // counter of how many nodes should be expanded in a batch

					// prioritize intersection with enabled nodes
					while (expansionCounter > 0) {
						expansionCounter--;
						Node<Pair<Pair<FPDNode, Pair<Integer, Boolean>>, Pair<Set<FPDNode>, Set<FPDNode>>>> currTreenode = toBeExpanded
								.remove(); // currTreenode to be expanded

						// get intersection between set of FPDNode in currTreenode and queue of node mapping
						tempHotAndEnabledNodes.clear();
						tempHotAndEnabledNodes.addAll(currTreenode.getData().getSecond().getFirst()); // enabled nodes
						tempHotAndEnabledNodes.retainAll(nextCandidateNodes);

						if (tempHotAndEnabledNodes.size() > 0) { // there is an intersection from enabled nodes
							toBeExpanded.addAll(expandTreenode(tempHotAndEnabledNodes, currTreenode, fpd,
									alreadyExpandedNodes, stateSpaceNumberLimit));
						} else {
							prevToBeExpanded.add(currTreenode);
						}
					}

					// if there is no node whose enable nodes intersects nextCandidateNodes
					if (toBeExpanded.size() == 0) {
						// no nodes with enabled nodes intersect nextCandidateNodes
						toBeExpanded.addAll(prevToBeExpanded);

						// check if there is a node with enabled+hot nodes intersect nextCandidateNodes
						expansionCounter = toBeExpanded.size();
						while (expansionCounter > 0) {
							expansionCounter--;
							Node<Pair<Pair<FPDNode, Pair<Integer, Boolean>>, Pair<Set<FPDNode>, Set<FPDNode>>>> currTreenode = toBeExpanded
									.remove(); // currTreenode to be expanded

							// get intersection between set of FPDNode in currTreenode and queue of node mapping
							tempHotAndEnabledNodes.clear();
							tempHotAndEnabledNodes.addAll(currTreenode.getData().getSecond().getFirst()); // enabled nodes
							tempHotAndEnabledNodes.addAll(currTreenode.getData().getSecond().getSecond()); // hot nodes
							tempHotAndEnabledNodes.retainAll(nextCandidateNodes);

							if (tempHotAndEnabledNodes.size() > 0) { // there is an intersection from enabled nodes
								toBeExpanded.addAll(expandTreenode(tempHotAndEnabledNodes, currTreenode, fpd,
										alreadyExpandedNodes, stateSpaceNumberLimit));
							}
						} // end one batch
					} // else, some nodes intersect nextCandidateNodes
					candidateNodes = nextCandidateNodes;
				} // (toBeExpanded.size() <= 0), (queueOfNodeMappings.size() <= 0), or (toBeExpanded.size() >= stateSpaceNumberLimit)

				// special handling for stateSpaceNumberLimit problem
				if (toBeExpanded.size() >= stateSpaceNumberLimit) {
					candidateNodes = null; // because the next candidateNodes is already determined in toBeExpanded
				}

				/**
				 * END OF BATCH
				 */

				// to store selected Treenode
				Node<Pair<Pair<FPDNode, Pair<Integer, Boolean>>, Pair<Set<FPDNode>, Set<FPDNode>>>> selectedTreenode = null;

				if (toBeExpanded.size() == 1) { // best case, continue insert the nodes to result
					// no problem
					selectedTreenode = toBeExpanded.remove();
					candidateNodes = null;
				} else {
					if (toBeExpanded.size() == 0) {
						toBeExpanded.addAll(prevToBeExpanded);
					}

					int frequency = -1;
					boolean connectedToHotNode = false;

					// prioritize nodes which is connected to hot node, then a node with highest frequency on its root 
					for (Node<Pair<Pair<FPDNode, Pair<Integer, Boolean>>, Pair<Set<FPDNode>, Set<FPDNode>>>> newTreenode : toBeExpanded) {
						if (connectedToHotNode) {
							// at least need to be hot node
							if (newTreenode.getData().getFirst().getSecond().getSecond()) {
								if (newTreenode.getData().getFirst().getSecond().getFirst() > frequency) {
									selectedTreenode = newTreenode;
									frequency = newTreenode.getData().getFirst().getSecond().getFirst();
								}
							}
						} else { // no nodes connected to a hot node has been found
							// check hot node
							if (newTreenode.getData().getFirst().getSecond().getSecond()) {
								selectedTreenode = newTreenode;
								frequency = newTreenode.getData().getFirst().getSecond().getFirst();
							} else { // not hot node
								if (newTreenode.getData().getFirst().getSecond().getFirst() > frequency) {
									selectedTreenode = newTreenode;
									frequency = newTreenode.getData().getFirst().getSecond().getFirst();
								}
							}
						}
					}
				}

				// update enableFPDNode
				enabledFPDNode.clear();
				enabledFPDNode.addAll(selectedTreenode.getData().getSecond().getFirst());

				// update hot nodes
				hotNodes.clear();
				hotNodes.addAll(selectedTreenode.getData().getSecond().getSecond());

				// insert node to result
				int index = result.size();
				while (selectedTreenode != null) {
					result.add(index, selectedTreenode.getData().getFirst().getFirst());
					selectedTreenode = selectedTreenode.getParent();
				}
			} // end tempNodes.size() evaluation
		}
		return result;
	}

	/**
	 * Helping method to expand state space node
	 * 
	 * @param toBeExpandedNodes
	 * @param currTreenode
	 * @param fpd
	 * @param alreadyExpandedNodes
	 * @return
	 */
	private synchronized Collection<? extends Node<Pair<Pair<FPDNode, Pair<Integer, Boolean>>, Pair<Set<FPDNode>, Set<FPDNode>>>>> expandTreenode(
			Set<FPDNode> toBeExpandedNodes,
			Node<Pair<Pair<FPDNode, Pair<Integer, Boolean>>, Pair<Set<FPDNode>, Set<FPDNode>>>> currTreenode,
			FPD fpd,
			Set<Node<Pair<Pair<FPDNode, Pair<Integer, Boolean>>, Pair<Set<FPDNode>, Set<FPDNode>>>>> alreadyExpandedNodes,
			int stateSpaceNumberLimit) {

		// variable to hold the result
		Set<Node<Pair<Pair<FPDNode, Pair<Integer, Boolean>>, Pair<Set<FPDNode>, Set<FPDNode>>>>> result = new HashSet<Node<Pair<Pair<FPDNode, Pair<Integer, Boolean>>, Pair<Set<FPDNode>, Set<FPDNode>>>>>();

		int expansionCounter = 0;
		for (FPDNode nextToBeExpandedNode : toBeExpandedNodes) {
			if (expansionCounter >= stateSpaceNumberLimit) {
				break;
			} else {
				// append all enabled nodes
				// create tempEnabled Nodes
				Set<FPDNode> tempEnabled = new HashSet<FPDNode>();
				tempEnabled.addAll(currTreenode.getData().getSecond().getFirst()); // previously enabled and hot nodes
				tempEnabled.remove(nextToBeExpandedNode);

				// append all hot nodes
				// create tempHotNodes
				Set<FPDNode> tempHotNodes = new HashSet<FPDNode>();
				tempHotNodes.addAll(currTreenode.getData().getSecond().getSecond());
				tempHotNodes.add(nextToBeExpandedNode);

				// update enabled nodes whose successor are all hot nodes
				FPDNode tobeDeleted = null;
				Set<FPDNode> setToBeDeleted = new HashSet<FPDNode>();
				inEdgeChecking: for (FPDEdge<? extends FPDNode, ? extends FPDNode> inEdge : fpd
						.getInEdges(nextToBeExpandedNode)) {
					// init to be deleted
					tobeDeleted = null;

					// if there is an element whose all successors are "hot nodes", remove it from tempEnabled
					for (FPDEdge<? extends FPDNode, ? extends FPDNode> outEdge : fpd.getOutEdges(inEdge.getSource())) {
						if (!tempHotNodes.contains(outEdge.getTarget())) {
							continue inEdgeChecking;
						} else {
							tobeDeleted = outEdge.getSource();
						}
					}
					setToBeDeleted.add(tobeDeleted);
					tempEnabled.remove(tobeDeleted);
				}

				// update hot nodes
				tobeDeleted = null;
				Set<FPDNode> unionSetToBeDeletedHotNodes = new HashSet<FPDNode>();
				unionSetToBeDeletedHotNodes.addAll(tempHotNodes);
				unionSetToBeDeletedHotNodes.addAll(setToBeDeleted);

				// remove hot nodes whose successor are all hot nodes (union with removed nodes)
				Set<FPDNode> hotNodesToBeRemoved = new HashSet<FPDNode>();

				checkNode: for (FPDNode hotNodeToBeDeleted : tempHotNodes) {
					// init to be deleted
					tobeDeleted = null;
					for (FPDEdge<? extends FPDNode, ? extends FPDNode> outEdge : fpd.getOutEdges(hotNodeToBeDeleted)) {
						// if there is an element whose all successors are "hot nodes" or setToBeDeleted, remove it from tempHotNodes
						if (!unionSetToBeDeletedHotNodes.contains(outEdge.getTarget())) {
							continue checkNode;
						} else {
							tobeDeleted = outEdge.getTarget();
						}
					}
					hotNodesToBeRemoved.add(tobeDeleted);
				}
				tempHotNodes.removeAll(hotNodesToBeRemoved);

				tempHotNodes.add(nextToBeExpandedNode); // need to be added twice in case it is removed previously 

				// add all successor of nextToBeExpandedNode
				for (FPDEdge<? extends FPDNode, ? extends FPDNode> outEdge : fpd.getOutEdges(nextToBeExpandedNode)) {
					tempEnabled.add(outEdge.getTarget());
				}
				;

				// create new pair
				Pair<FPDNode, Pair<Integer, Boolean>> nodeBooleanPair = new Pair<FPDNode, Pair<Integer, Boolean>>(
						nextToBeExpandedNode, currTreenode.getData().getFirst().getSecond());
				Pair<Set<FPDNode>, Set<FPDNode>> setPair = new Pair<Set<FPDNode>, Set<FPDNode>>(tempEnabled,
						tempHotNodes);
				Pair<Pair<FPDNode, Pair<Integer, Boolean>>, Pair<Set<FPDNode>, Set<FPDNode>>> newPair = new Pair<Pair<FPDNode, Pair<Integer, Boolean>>, Pair<Set<FPDNode>, Set<FPDNode>>>(
						nodeBooleanPair, setPair);
				Node<Pair<Pair<FPDNode, Pair<Integer, Boolean>>, Pair<Set<FPDNode>, Set<FPDNode>>>> newTreenode = new Node<Pair<Pair<FPDNode, Pair<Integer, Boolean>>, Pair<Set<FPDNode>, Set<FPDNode>>>>(
						newPair);

				// check if the tree node already inside the treeset
				if (!alreadyExpandedNodes.contains(newTreenode)) {
					alreadyExpandedNodes.add(currTreenode);

					// next, do it as usual
					newTreenode.setParent(currTreenode);

					// add the node to tobeExpanded
					result.add(newTreenode);
				}
			}
		} // end of iteration for each nextToBeExpanded node
		return result;
	}
}
