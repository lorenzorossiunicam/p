package org.processmining.plugins.ywl.replayer;

import java.util.Set;

import org.yawlfoundation.yawl.editor.elements.model.YAWLVertex;

/**
* @author David Piessens
* @email d.a.m.piessens@student.tue.nl
* @version May 29, 2010
*/
public class YawlState {
	/**
	 * Internal data structure
	 */
	private int weight;
	private YAWLVertex yawlNode;
	private Set<YAWLVertex> prevNodes;
	private Set<YAWLVertex> nextNodes;
	
	/**
	 * @return the weight
	 */
	public int getWeight() {
		return weight;
	}
	/**
	 * @param weight the weight to set
	 */
	public void setWeight(int weight) {
		this.weight = weight;
	}
	/**
	 * @return the yawlNode
	 */
	public YAWLVertex getYawlNode() {
		return yawlNode;
	}
	/**
	 * @param yawlNode the yawlNode to set
	 */
	public void setYawlNode (YAWLVertex yawlNode) {
		this.yawlNode = yawlNode;
	}
	/**
	 * @return the prevNodes
	 */
	public Set<YAWLVertex> getNextNodes() {
		return nextNodes;
	}
	/**
	 * @param nextNodes the nextNodes to set
	 */
	public void setNextNodes(Set<YAWLVertex> nextNodes) {
		this.nextNodes = nextNodes;
	}
	/**
	 * @return the prevNodes
	 */
	public Set<YAWLVertex> getPrevNodes() {
		return prevNodes;
	}
	/**
	 * @param prevNodes the prevNodes to set
	 */
	public void setPrevNodes(Set<YAWLVertex> prevNodes) {
		this.prevNodes = prevNodes;
	}

	
}
