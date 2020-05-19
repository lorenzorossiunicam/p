/**
 * 
 */
package org.processmining.connections.fuzzyperformancediagram;

import java.util.Map;

import org.processmining.framework.connections.impl.AbstractConnection;
import org.processmining.models.fuzzyperformancediagram.FPD;
import org.processmining.models.fuzzyperformancediagram.FPDEdge;
import org.processmining.models.fuzzyperformancediagram.FPDNode;
import org.processmining.models.simpleprecedencediagram.SPD;
import org.processmining.models.simpleprecedencediagram.SPDEdge;
import org.processmining.models.simpleprecedencediagram.SPDNode;

/**
 * @author arya
 * @email arya.adriansyah@gmail.com
 * @version Apr 27, 2009
 */
public class SPDFPDConnection extends AbstractConnection {
	public final static String SPD = "SPD";
	public final static String FPD = "FPD";

	// internal data
	private final Map<SPDNode, FPDNode> nodeMapping;
	private final Map<SPDEdge<? extends SPDNode, ? extends SPDNode>, FPDEdge<? extends FPDNode, ? extends FPDNode>> edgeMapping;

	public SPDFPDConnection(
			SPD spd,
			FPD fpd,
			Map<SPDNode, FPDNode> nodeMapping,
			Map<SPDEdge<? extends SPDNode, ? extends SPDNode>, FPDEdge<? extends FPDNode, ? extends FPDNode>> edgeMapping) {
		super("SPD " + spd.getLabel() + " and FPD " + fpd.getLabel() + " connection");
		assert (spd.getNodes().size() == fpd.getNodes().size());
		put(SPD, spd);
		put(FPD, fpd);

		// set the internal data
		this.nodeMapping = nodeMapping;
		this.edgeMapping = edgeMapping;
	}

	/**
	 * return FPD node which is mapped to spdNode
	 * 
	 * @param spdNode
	 * @return
	 */
	public FPDNode getFPDNodeOfSPDNode(SPDNode spdNode) {
		return nodeMapping.get(spdNode);
	}

	/**
	 * return FPD edge which is mapped to spdEdge
	 * 
	 * @param spdEdge
	 * @return
	 */
	public FPDEdge<? extends FPDNode, ? extends FPDNode> getFPDEdgeOfSPDEdge(
			SPDEdge<? extends SPDNode, ? extends SPDNode> spdEdge) {
		return edgeMapping.get(spdEdge);
	}

	/**
	 * return SPD node which is mapped to fpdNode
	 * 
	 * @param fpdNode
	 * @return
	 */
	public SPDNode getSPDNodeOfFPDNode(FPDNode fpdNode) {
		for (SPDNode spdNode : nodeMapping.keySet()) {
			if (nodeMapping.get(spdNode).equals(fpdNode)) {
				return spdNode;
			}
		}
		return null;
	}

	/**
	 * return SPD edge which is mapped to fpdEdge
	 * 
	 * @param fpdEdge
	 * @return
	 */
	public SPDEdge<? extends SPDNode, ? extends SPDNode> getSPDEdgeOfFPDEdge(
			FPDEdge<? extends FPDNode, ? extends FPDNode> fpdEdge) {
		for (SPDEdge<? extends SPDNode, ? extends SPDNode> spdEdge : edgeMapping.keySet()) {
			if (edgeMapping.get(spdEdge).equals(fpdEdge)) {
				return spdEdge;
			}
		}
		return null;
	}
}
