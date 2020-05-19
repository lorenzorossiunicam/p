/**
 * 
 */
package org.processmining.plugins.performancemeasurement.logreplayers.nodeidentifiers;

import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.processmining.models.fuzzyperformancediagram.FPD;
import org.processmining.models.fuzzyperformancediagram.FPDNode;

/**
 * @author arya
 * @email arya.adriansyah@gmail.com
 * @version Apr 7, 2009
 */
public interface IFPDNodeLogIdentifier {
	public List<FPDNode> identifyFPDNodes(Queue<Set<FPDNode>> queueOfNodeMappings, FPD fpd);
}
