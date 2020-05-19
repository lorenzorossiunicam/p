/**
 * 
 */
package org.processmining.plugins.performancemeasurement.logreplayers.calculators;

import java.util.List;
import java.util.Map;

import org.processmining.models.aggregatedactivitiesperformancediagram.AAPD;
import org.processmining.models.aggregatedactivitiesperformancediagram.AAPDElement;
import org.processmining.models.fuzzyperformancediagram.FPD;
import org.processmining.models.fuzzyperformancediagram.FPDEdge;
import org.processmining.models.fuzzyperformancediagram.FPDNode;
import org.processmining.models.performancemeasurement.dataelements.TwoFPDNodesPerformanceData;
import org.processmining.models.simpleprecedencediagram.SPDEdge;
import org.processmining.models.simpleprecedencediagram.SPDNode;
import org.processmining.plugins.log.transitionchecker.LifecycleTransitionChecker;

/**
 * @author arya
 * @email arya.adriansyah@gmail.com
 * @version Apr 10, 2009
 */
public abstract class AbstractFPDAAPDCalculator implements IFPDCalculator {
	// internal data
	protected FPD fpd;
	protected Map<SPDNode, FPDNode> mapSPDNodetoFPDNode;
	protected Map<SPDEdge<? extends SPDNode, ? extends SPDNode>, FPDEdge<? extends FPDNode, ? extends FPDNode>> mapSPDEdgetoFPDEdge;

	private int numOfCases = 0;

	// accumulator
	protected IFPDNodeInstanceAAPDCalculator fpdNodeInstanceAAPDCalculator;
	protected LifecycleTransitionChecker lifecycleTransitionChecker; // to determine which activity instances should be accounted
	protected TwoNodesPerformanceCalculator twoNodesPerformanceCalculator;

	// elements performance record
	protected Map<FPDNode, List<List<Double>>> nodePerformance; // store waiting time, synchronization time, and throughput time for each node 
	protected Map<FPDEdge<? extends FPDNode, ? extends FPDNode>, List<Double>> edgePerformance; // store moving time for each edge

	// for case performance calculation
	protected boolean lastCaseCalculationValidity = false;

	// for AAPD
	protected AAPD aapd;
	protected Map<FPDNode, AAPDElement> mapFPDNodeToAAPDFocusElements;

	/**
	 * @return the fpd
	 */
	public FPD getFpd() {
		return fpd;
	}

	/**
	 * @param fpd
	 *            the fpd to set
	 */
	public void setFpd(FPD fpd) {
		this.fpd = fpd;
	}

	/**
	 * @return nodePerformance the node performance
	 */
	public Map<FPDNode, List<List<Double>>> getNodePerformance() {
		return nodePerformance;
	}

	/**
	 * set node performance
	 * 
	 * @param nodePerformance
	 */
	public void setNodePerformance(Map<FPDNode, List<List<Double>>> nodePerformance) {
		this.nodePerformance = nodePerformance;
	}

	/**
	 * @return edgePerformance the edgePerformance
	 */
	public Map<FPDEdge<? extends FPDNode, ? extends FPDNode>, List<Double>> getEdgePerformance() {
		return edgePerformance;
	}

	/**
	 * set edge performance
	 * 
	 * @param edgePerformance
	 */
	public void setEdgePerformance(Map<FPDEdge<? extends FPDNode, ? extends FPDNode>, List<Double>> edgePerformance) {
		this.edgePerformance = edgePerformance;
	}

	/**
	 * @param lastCaseCalculationValidity
	 *            the lastCaseCalculationValidity to set
	 */
	public void setLastCaseCalculationValidity(boolean lastCalculationValidity) {
		lastCaseCalculationValidity = lastCalculationValidity;
	}

	/**
	 * @return lastCaseCalculationValidity the lastCaseCalculationValidity
	 */
	public boolean getLastCaseCalculationValidity() {
		return lastCaseCalculationValidity;
	}

	/**
	 * @return the activityInstanceAccumulator
	 */
	public IFPDNodeInstanceAAPDCalculator getAbstractFPDNodeInstanceAccumulator() {
		return fpdNodeInstanceAAPDCalculator;
	}

	/**
	 * @param activityInstanceAccumulator
	 *            the activityInstanceAccumulator to set
	 */
	public void setAbstractFPDNodeInstanceAccumulator(IFPDNodeInstanceAAPDCalculator fpdNodeInstanceAccumulator) {
		fpdNodeInstanceAAPDCalculator = fpdNodeInstanceAccumulator;
	}

	/**
	 * @return the lifecycleTransitionChecker
	 */
	public LifecycleTransitionChecker getLifecycleTransitionChecker() {
		return lifecycleTransitionChecker;
	}

	/**
	 * @param lifecycleTransitionChecker
	 *            the lifecycleTransitionChecker to set
	 */
	public void setLifecycleTransitionChecker(LifecycleTransitionChecker lifecycleTransitionChecker) {
		this.lifecycleTransitionChecker = lifecycleTransitionChecker;
	}

	/**
	 * set the number of cases which are involved on this calculation
	 * 
	 * @param numOfCases
	 */
	public void setNumOfCases(int numOfCases) {
		this.numOfCases = numOfCases;
	}

	/**
	 * get the number of cases which are involved on this calculation
	 * 
	 * @return
	 */
	public int getNumOfCases() {
		return numOfCases;
	}

	/**
	 * @return the mapSPDNodetoFPDNode
	 */
	public Map<SPDNode, FPDNode> getMapSPDNodetoFPDNode() {
		return mapSPDNodetoFPDNode;
	}

	/**
	 * @param mapSPDNodetoFPDNode
	 *            the mapSPDNodetoFPDNode to set
	 */
	public void setMapSPDNodetoFPDNode(Map<SPDNode, FPDNode> mapSPDNodetoFPDNode) {
		this.mapSPDNodetoFPDNode = mapSPDNodetoFPDNode;
	}

	/**
	 * @return the mapSPDEdgetoFPDEdge
	 */
	public Map<SPDEdge<? extends SPDNode, ? extends SPDNode>, FPDEdge<? extends FPDNode, ? extends FPDNode>> getMapSPDEdgetoFPDEdge() {
		return mapSPDEdgetoFPDEdge;
	}

	/**
	 * @param mapSPDEdgetoFPDEdge
	 *            the mapSPDEdgetoFPDEdge to set
	 */
	public void setMapSPDEdgetoFPDEdge(
			Map<SPDEdge<? extends SPDNode, ? extends SPDNode>, FPDEdge<? extends FPDNode, ? extends FPDNode>> mapSPDEdgetoFPDEdge) {
		this.mapSPDEdgetoFPDEdge = mapSPDEdgetoFPDEdge;
	}

	public TwoFPDNodesPerformanceData getTwoNodesPerformanceData() {
		return twoNodesPerformanceCalculator.getTwoNodesPerformanceData();
	}

	/**
	 * @return the aapd
	 */
	public AAPD getAAPD() {
		return aapd;
	}

	/**
	 * @param aapd
	 *            the aapd to set
	 */
	public void setAAPD(AAPD aapd) {
		this.aapd = aapd;
	}

	/**
	 * @return the mapFPDNodeToAAPDFocusElements
	 */
	public Map<FPDNode, AAPDElement> getMapFPDNodeToAAPDFocusElements() {
		return mapFPDNodeToAAPDFocusElements;
	}

	/**
	 * @param mapFPDNodeToAAPDFocusElements
	 *            the mapFPDNodeToAAPDFocusElements to set
	 */
	public void setMapFPDNodeToAAPDFocusElements(Map<FPDNode, AAPDElement> mapFPDNodeToAAPDFocusElements) {
		this.mapFPDNodeToAAPDFocusElements = mapFPDNodeToAAPDFocusElements;
	}

}
