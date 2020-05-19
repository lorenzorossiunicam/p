/**
 * 
 */
package org.processmining.connections.aggregatedactivitiesperformancediagram;

import java.util.Map;

import org.processmining.framework.connections.impl.AbstractConnection;
import org.processmining.models.aggregatedactivitiesperformancediagram.AAPD;
import org.processmining.models.aggregatedactivitiesperformancediagram.AAPDElement;
import org.processmining.models.fuzzyperformancediagram.FPD;
import org.processmining.models.fuzzyperformancediagram.FPDNode;

/**
 * @author arya
 * @email arya.adriansyah@gmail.com
 * @version May 13, 2009
 */
public class FPDAAPDConnection extends AbstractConnection {
	public final static String FPD = "FuzzyPerformanceDiagram";
	public final static String AAPD = "AggregatedActivitiesPerformanceDiagram";

	// internal data
	private Map<FPDNode, AAPDElement> nodeMapping;

	/**
	 * Default constructor
	 * 
	 * @param fpd
	 * @param aapd
	 */
	public FPDAAPDConnection(FPD fpd, AAPD aapd) {
		super("AAPD from " + fpd.getLabel());

		put(FPD, fpd);
		put(AAPD, aapd);
	}

	/**
	 * set node mapping between aapdElement and FPDNode
	 * 
	 * @param mapping
	 */
	public void setNodeMapping(Map<FPDNode, AAPDElement> mapping) {
		nodeMapping = mapping;
	}

	/**
	 * get node mapping between aapdElement and FPDNode
	 * 
	 * @return
	 */
	public Map<FPDNode, AAPDElement> getNodeMapping() {
		return nodeMapping;
	}
}
